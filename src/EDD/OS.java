/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EDD;

/**
 *
 * @author Eddy
 */
public class OS {

    private Lista processList = new Lista();
    private Scheduler scheduler;
    private Cola readyQueue = new Cola();
    private Cola blockedQueue = new Cola();
    private Lista terminatedProcessList = new Lista();


    
    public Cola fillReadyQueue(){
        // Llena readyQueue con los procesos cuyo PCB.status == "ready"
        // Corregido: usar equals en lugar de == y no incrementar i en else (evitaba elementos)
        for (int i = 0; i < getProcessList().count(); i++){
            Object obj = getProcessList().get(i);
            if (!(obj instanceof Proceso)) continue;
            Proceso proc = (Proceso) obj;
            PCB auxProcessPCB = proc.getPcb();
            if ("ready".equals(auxProcessPCB.getStatus())){
                getReadyQueue().enqueue(proc);
            }
        }
        return getReadyQueue();
    }

    public OS() {
        this.scheduler = new Scheduler(processList);
        
    }
    
    public Proceso getActiveProcess(){
    
        for (int i = 0; i < getProcessList().count(); i++){
            Proceso obj = (Proceso) getProcessList().get(i);
            if ("running".equals(obj.getPcb().getStatus())){
                return obj;
            }
        }
        return null;
    }
    
        
    /**
     * @return the processList
     */
    public Lista getProcessList() {
        return processList;
    }

    /**
     * @param processList the processList to set
     */
    public void setProcessList(Lista processList) {
        this.processList = processList;
    }
    
    /**
     * @return the scheduler
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * @param scheduler the scheduler to set
     */
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * @return the readyQueue
     */
    public Cola getReadyQueue() {
        return readyQueue;
    }

    public void setReadyQueue(Cola readyQueue) {
        this.readyQueue = readyQueue;
    }

    public Cola getBlockedQueue() {
        return blockedQueue;
    }

    public void setBlockedQueue(Cola blockedQueue) {
        this.blockedQueue = blockedQueue;
    }

    public Lista getTerminatedProcessList() {
        return terminatedProcessList;
    }

    public void setTerminatedProcessList(Lista terminatedProcessList) {
        this.terminatedProcessList = terminatedProcessList;
    }

}