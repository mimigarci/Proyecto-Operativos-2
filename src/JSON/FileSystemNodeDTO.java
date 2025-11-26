package JSON;

import java.util.List;

public class FileSystemNodeDTO {
    private boolean isFileObject;
    private FileDTO fileData;
    private String stringData;
    private List<FileSystemNodeDTO> children;

    public FileSystemNodeDTO() {
    }

    public boolean isIsFileObject() {
        return isFileObject;
    }

    public void setIsFileObject(boolean isFileObject) {
        this.isFileObject = isFileObject;
    }

    public FileDTO getFileData() {
        return fileData;
    }

    public void setFileData(FileDTO fileData) {
        this.fileData = fileData;
    }

    public String getStringData() {
        return stringData;
    }

    public void setStringData(String stringData) {
        this.stringData = stringData;
    }

    public List<FileSystemNodeDTO> getChildren() {
        return children;
    }

    public void setChildren(List<FileSystemNodeDTO> children) {
        this.children = children;
    }
}
