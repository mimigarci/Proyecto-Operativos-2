package JSON;

import java.util.List;

public class ProjectStateDTO {
    private List<BlockDTO> diskBlocks;
    private FileSystemNodeDTO rootNode;

    public ProjectStateDTO() {
    }

    public List<BlockDTO> getDiskBlocks() {
        return diskBlocks;
    }

    public void setDiskBlocks(List<BlockDTO> diskBlocks) {
        this.diskBlocks = diskBlocks;
    }

    public FileSystemNodeDTO getRootNode() {
        return rootNode;
    }

    public void setRootNode(FileSystemNodeDTO rootNode) {
        this.rootNode = rootNode;
    }
}
