/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EDD;

/**
 *
 * @author miche_ysmoa6e
 */
public class Scheduler {
    
    // Tomando que 1 ciclo de ejecución del CPU son 0.00001ms
    
    private Lista processList; //Lista en la cual se guardan todos los procesos a ejecutar.
                                     //Se agregan desde la interfaz

    public Scheduler(Lista processList) {
        this.processList = processList;
    }
    
    public void manageProcess (Cola readyQueue, Cola blockedQueue, Cola requestsQueue, Lista files){
         
        if (readyQueue.getCount() > 0){

            var processToActivate = readyQueue.dequeue();
            if (!(processToActivate instanceof PCB)) {
                // defensive: if the queue didn't contain a PCB, nothing to do
                return;
            }
            PCB pcbOfActiveProcess = (PCB) processToActivate;
            int i = 0;
            Proceso toRun = null;
            while (i < processList.count()){
                toRun = (Proceso)processList.get(i);
                if (pcbOfActiveProcess.getId() == ((Proceso)processList.get(i)).getPcb().getId()){
                    toRun = (Proceso)processList.get(i);
                    break;
                } else {
                    i++;
                } 
            }
        
            toRun.getPcb().setStatus("running");
            
            if (toRun == null) return;
            
            try {
                Thread.sleep(1000);
            } 
            catch(InterruptedException e) {
                 // honor interruption — set flag and return
                 Thread.currentThread().interrupt();
                 return;
            }
            
            i = 0;
            File actFile = null;
            while (i < files.count()){
                actFile = (File) files.get(i);
                if (toRun.getFile().equals(((File)files.get(i)).getName())){
                    actFile = (File)files.get(i);
                    break;
                } else {
                    i++;
                } 
            }
            
            toRun.getPcb().setStatus("blocked");
            blockedQueue.enqueue(toRun.getPcb());
            Request newRequest = generateRequest(toRun, actFile);           
            requestsQueue.enqueue(newRequest);
        }
    }
    
    public Request generateRequest(Proceso toRun, File actFile) {
        Request newRequest;
        if (toRun.getCrud() == 1){
            newRequest = new Request(toRun.getPcb().getId(), actFile.getName(), ((Block)actFile.getFileBlocks().get(0)).getPosition(), toRun.getUpdtMsg(), toRun.getSize());
        } else if (toRun.getCrud() == 0){
            newRequest = new Request(toRun.getPcb().getId(), actFile.getName(), -1, toRun.getSize());
        } else {
            if (actFile.getSize() > 0){
                newRequest = new Request(toRun.getPcb().getId(), actFile.getName(), ((Block)actFile.getFileBlocks().get(0)).getPosition(), toRun.getSize());
            } else {
                newRequest = new Request(toRun.getPcb().getId(), actFile.getName(), 0, toRun.getSize());
            }
        }
        return newRequest;
    }
    
}
