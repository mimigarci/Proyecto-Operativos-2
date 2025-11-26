package JSON;

import EDD.Block;
import EDD.Disk;
import EDD.File;
import EDD.Lista;
import Interfaz.Interface;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class JsonManager {

    private static final String FILE_PATH = "filesystem_state.json";

    public static void save(Interface context) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            ProjectStateDTO state = new ProjectStateDTO();

            // Save Disk
            Disk disk = context.getDisk();
            List<BlockDTO> blockDTOs = new ArrayList<>();
            Block[] spaces = disk.getSpaces();
            for (Block block : spaces) {
                if (block != null) {
                    String colorStr = "";
                    if (block.getColor() != null) {
                        colorStr = block.getColor().getRed() + "," + block.getColor().getGreen() + "," + block.getColor().getBlue();
                    }
                    blockDTOs.add(new BlockDTO(block.getContent(), block.getPosition(), block.getX(), block.getY(), colorStr));
                }
            }
            state.setDiskBlocks(blockDTOs);

            // Save Tree
            DefaultMutableTreeNode root = context.getRoot();
            state.setRootNode(convertNode(root));

            mapper.writeValue(new java.io.File(FILE_PATH), state);
            JOptionPane.showMessageDialog(context, "State saved successfully to " + FILE_PATH);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(context, "Error saving state: " + e.getMessage());
        }
    }

    private static FileSystemNodeDTO convertNode(DefaultMutableTreeNode node) {
        FileSystemNodeDTO nodeDTO = new FileSystemNodeDTO();
        Object userObject = node.getUserObject();

        if (userObject instanceof File) {
            File file = (File) userObject;
            nodeDTO.setIsFileObject(true);
            FileDTO fileDTO = new FileDTO();
            fileDTO.setId(file.getId());
            fileDTO.setName(file.getName());
            fileDTO.setSize(file.getSize());
            fileDTO.setIsPublic(file.isPublic());
            fileDTO.setIsFile(file.getSize() > 0); // Heuristic based on size or logic? File class has isFile field but no getter? 
            // Wait, File.java has isFile field but getter is not shown in view_file output?
            // Let's check File.java again. It has isFile field.
            // It doesn't seem to have a public getter for isFile. 
            // But we can infer: if it has blocks, it's a file. If it's a directory, size might be 0?
            // Actually, in Interface.create(), directories have size 0.
            
            if (file.getColor() != null) {
                fileDTO.setColorRGB(file.getColor().getRed() + "," + file.getColor().getGreen() + "," + file.getColor().getBlue());
            }

            List<Integer> indices = new ArrayList<>();
            Lista blocks = file.getFileBlocks();
            for (int i = 0; i < blocks.count(); i++) {
                Block b = (Block) blocks.get(i);
                indices.add(b.getPosition());
            }
            fileDTO.setBlockIndices(indices);
            nodeDTO.setFileData(fileDTO);
        } else {
            nodeDTO.setIsFileObject(false);
            nodeDTO.setStringData(userObject.toString());
        }

        List<FileSystemNodeDTO> children = new ArrayList<>();
        for (int i = 0; i < node.getChildCount(); i++) {
            children.add(convertNode((DefaultMutableTreeNode) node.getChildAt(i)));
        }
        nodeDTO.setChildren(children);

        return nodeDTO;
    }

    public static void load(Interface context) {
        try {
            java.io.File f = new java.io.File(FILE_PATH);
            if (!f.exists()) {
                JOptionPane.showMessageDialog(context, "No saved state found.");
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            ProjectStateDTO state = mapper.readValue(f, ProjectStateDTO.class);

            // Reconstruct Disk
            Lista filesList = new Lista(); // We will rebuild this
            Disk newDisk = new Disk(filesList);
            Block[] newSpaces = new Block[64];
            
            // Initialize with empty blocks first to be safe
            for(int i=0; i<64; i++) {
                newSpaces[i] = new Block(i);
            }

            for (BlockDTO bDTO : state.getDiskBlocks()) {
                int pos = bDTO.getPosition();
                if (pos >= 0 && pos < 64) {
                    Block block = newSpaces[pos];
                    block.setContent(bDTO.getContent());
                    block.setX(bDTO.getX());
                    block.setY(bDTO.getY());
                    if (bDTO.getColorRGB() != null && !bDTO.getColorRGB().isEmpty()) {
                        String[] parts = bDTO.getColorRGB().split(",");
                        if (parts.length == 3) {
                            block.setColor(new Color(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
                        }
                    }
                }
            }
            newDisk.setSpaces(newSpaces);
            // newDisk.setFiles(filesList); // Already passed in constructor

            // Reconstruct Tree
            DefaultMutableTreeNode newRoot = reconstructNode(state.getRootNode(), newDisk, filesList);

            // Update Interface
            context.setDisk(newDisk);
            context.setRoot(newRoot);
            context.setTreeModel(new DefaultTreeModel(newRoot));
            context.getJTreeComponent().setModel(context.getTreeModel());
            
            // Rebuild files list in context (filesList is populated during reconstruction)
            context.setFiles(filesList);
            
            // Also update allNodes which seems to be a flat list of all nodes
            context.setAllNodes(getAllNodes(newRoot));

            context.updateTable();
            // context.updateTree(); // Already set model
            
            JOptionPane.showMessageDialog(context, "State loaded successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(context, "Error loading state: " + e.getMessage());
        }
    }

    private static DefaultMutableTreeNode reconstructNode(FileSystemNodeDTO nodeDTO, Disk disk, Lista filesList) {
        DefaultMutableTreeNode node;

        if (nodeDTO.isIsFileObject()) {
            FileDTO fDTO = nodeDTO.getFileData();
            File file;
            if (fDTO.getSize() > 0) {
                 file = new File(fDTO.getId(), fDTO.getName(), fDTO.getSize(), fDTO.isIsPublic());
            } else {
                 file = new File(fDTO.getId(), fDTO.getName(), fDTO.isIsPublic());
            }
            
            if (fDTO.getColorRGB() != null && !fDTO.getColorRGB().isEmpty()) {
                String[] parts = fDTO.getColorRGB().split(",");
                if (parts.length == 3) {
                    file.setColor(new Color(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
                }
            }

            // Link blocks
            Lista fileBlocks = new Lista();
            Block[] spaces = disk.getSpaces();
            if (fDTO.getBlockIndices() != null) {
                for (int idx : fDTO.getBlockIndices()) {
                    if (idx >= 0 && idx < spaces.length) {
                        fileBlocks.add(spaces[idx]);
                    }
                }
            }
            file.setFileBlocks(fileBlocks);
            
            // Add to global files list
            filesList.add(file);
            
            node = new DefaultMutableTreeNode(file);
        } else {
            node = new DefaultMutableTreeNode(nodeDTO.getStringData());
        }

        if (nodeDTO.getChildren() != null) {
            for (FileSystemNodeDTO childDTO : nodeDTO.getChildren()) {
                node.add(reconstructNode(childDTO, disk, filesList));
            }
        }

        return node;
    }
    
    // Helper to rebuild allNodes list (flat list of nodes)
    private static Lista getAllNodes(DefaultMutableTreeNode root) {
        Lista list = new Lista();
        java.util.Enumeration e = root.preorderEnumeration();
        while(e.hasMoreElements()){
            list.add(e.nextElement());
        }
        return list;
    }
}
