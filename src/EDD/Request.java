/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EDD;

/**
 *
 * @author Eddy
 */
public class Request {
    private int processId;
    private int fileId;
    private int fileAdd;
    private String updtMsg;

    public Request(int processId, int fileId, int fileAdd) {
        this.processId = processId;
        this.fileId = fileId;
        this.fileAdd = fileAdd;
    }

    public Request(int processId, int fileId, int fileAdd, String updtMsg) {
        this.processId = processId;
        this.fileId = fileId;
        this.fileAdd = fileAdd;
        this.updtMsg = updtMsg;
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public int getFileAdd() {
        return fileAdd;
    }

    public void setFileAdd(int fileAdd) {
        this.fileAdd = fileAdd;
    }

    public String getUpdtMsg() {
        return updtMsg;
    }

    public void setUpdtMsg(String updtMsg) {
        this.updtMsg = updtMsg;
    }
    
}
