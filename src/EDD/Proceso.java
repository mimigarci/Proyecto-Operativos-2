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
    //private int fileId;


    public Proceso(int id, String name, int crud) {
        this.pcb = new PCB(id, name);
        this.crud = crud;
    }

    public Proceso(PCB pcb, int crud) {
        this.pcb = pcb;
        this.crud = crud;
        //this.fileId = 1000000000;
    }

    public int getCrud() {
        return crud;
    }

    public void setCrud(int crud) {
        this.crud = crud;
    }

    /*
    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }  
    */
    public PCB getPcb() {
        return pcb;
    }

    // proceso.start() ----> Empieza a ejecutar el proceso. Este método hará que se ejecute el run() dentro de la clase
    // ¿Cómo podemos simular la ejecución de un proceso? ¿Qué podemos meter en run?

}
