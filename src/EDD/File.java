/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EDD;

import java.awt.Color;

/**
 *
 * @author Eddy
 */
public class File {
    private String name;
    private int id;
    private int size;
    private Lista fileBlocks = new Lista();
    private boolean isPublic;
    private Color color;
    private boolean isFile;

    public File(int id, String name, int size, boolean isPublic) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.isPublic = isPublic;
        this.color = selectColors();
        this.isFile = true;
    }

    public File(int id, String name, boolean isPublic) {
        this.name = name;
        this.id = id;
        this.isPublic = isPublic;
        this.isFile = false;
    }
    
    public Color selectColors(){
        int r = (int)(Math.random() * 256); // 0–255
        int g = (int)(Math.random() * 256);
        int b = (int)(Math.random() * 256);
        return new Color(r, g, b);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Lista getFileBlocks() {
        return fileBlocks;
    }

    public void setFileBlocks(Lista fileBlocks) {
        this.fileBlocks = fileBlocks;
    }

    public boolean isPublic() {
        return isIsPublic();
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
    
    @Override
    public String toString() {
        return name; // lo que se mostrará en el JTree
    }
    
    public String read(){
        String txt = "";
        
        for (int i = 0; i < fileBlocks.count(); i++){
            Block aux = (Block) fileBlocks.get(i);
            txt += aux.getPosition()+  ": " + aux.getContent() +"\n";
        }
        return txt;
    }

    /**
     * @return the isPublic
     */
    public boolean isIsPublic() {
        return isPublic;
    }

    /**
     * @param isPublic the isPublic to set
     */
    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
    
}
