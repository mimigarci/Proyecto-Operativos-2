package JSON;

import java.util.List;

public class FileDTO {
    private int id;
    private String name;
    private int size;
    private boolean isPublic;
    private String colorRGB;
    private boolean isFile;
    private List<Integer> blockIndices; // Indices of blocks in the disk array

    public FileDTO() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isIsPublic() {
        return isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getColorRGB() {
        return colorRGB;
    }

    public void setColorRGB(String colorRGB) {
        this.colorRGB = colorRGB;
    }

    public boolean isIsFile() {
        return isFile;
    }

    public void setIsFile(boolean isFile) {
        this.isFile = isFile;
    }

    public List<Integer> getBlockIndices() {
        return blockIndices;
    }

    public void setBlockIndices(List<Integer> blockIndices) {
        this.blockIndices = blockIndices;
    }
}
