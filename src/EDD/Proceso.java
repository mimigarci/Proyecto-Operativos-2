/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EDD;

/**
 *
 * @author Eddy
 */
public class Proceso{
    private PCB pcb;
    private int crud;
    private String updtMsg;
    private String file;
    private String directory;


    public Proceso(int id, String name, int crud, String file, String directory) {
        this.pcb = new PCB(id, name);
        this.crud = crud;
        this.file = file;
        this.directory = directory;
    }

    public Proceso(int id, String name, int crud, String file, String directory, String updtMsg) {
        this.pcb = new PCB(id, name);
        this.crud = crud;
        this.file = file;
        this.updtMsg = updtMsg;
        this.directory = directory;
    }

    public int getCrud() {
        return crud;
    }

    public void setCrud(int crud) {
        this.crud = crud;
    }

    
    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }  
    
    public PCB getPcb() {
        return pcb;
    }

    public String getUpdtMsg() {
        return updtMsg;
    }

    public void setUpdtMsg(String updtMsg) {
        this.updtMsg = updtMsg;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

}
