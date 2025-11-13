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



    public Proceso(int id, String name, int crud) {
        this.pcb = new PCB(id, name);
        this.crud = crud;
    }

    
    
    public PCB getPcb() {
        return pcb;
    }

    // proceso.start() ----> Empieza a ejecutar el proceso. Este método hará que se ejecute el run() dentro de la clase
    // ¿Cómo podemos simular la ejecución de un proceso? ¿Qué podemos meter en run?

}
