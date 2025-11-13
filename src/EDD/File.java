/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EDD;

/**
 *
 * @author Eddy
 */
public class File {
    private String name;
    private int id;
    private int depth;
    private int size;
    private Lista fileBlocks;
    private boolean isPublic;
    private Lista path;

    public File(int id, String name, int size, boolean isPublic, Lista path) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.isPublic = isPublic;
        this.path = path;
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

    public Lista getPath() {
        return path;
    }

    public void setPath(Lista path) {
        this.path = path;
    }

    public boolean isPublic() {
        return isPublic;
    }
    
}
