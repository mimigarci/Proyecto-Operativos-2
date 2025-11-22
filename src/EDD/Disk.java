/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EDD;

/**
 *
 * @author Eddy
 */
public class Disk {
    private Object[] spaces;
    private int totalBlocks;
    private int availableBlocks;
    private int headerPosition;
    private int direction;
    private int planification;
    private Cola requests;
    private Directory root;

    public Disk() {
        this.spaces = new Object[64];
        this.totalBlocks = 64;
        this.availableBlocks = totalBlocks;
        this.headerPosition = 0;
        this.direction = 0;
        this.planification = 0;
        this.requests = new Cola();
        this.root = new Directory(0, new Lista());
    }

    public Object[] getSpaces() {
        return spaces;
    }

    public void setSpaces(Object[] spaces) {
        this.spaces = spaces;
    }

    public int getTotalBlocks() {
        return totalBlocks;
    }

    public void setTotalBlocks(int totalBlocks) {
        this.totalBlocks = totalBlocks;
    }

    public int getAvailableBlocks() {
        return availableBlocks;
    }

    public void setAvailableBlocks(int availableBlocks) {
        this.availableBlocks = availableBlocks;
    }

    public int getHeaderPosition() {
        return headerPosition;
    }

    public void setHeaderPosition(int headerPosition) {
        this.headerPosition = headerPosition;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getPlanification() {
        return planification;
    }

    public void setPlanification(int planification) {
        this.planification = planification;
    }

    public Cola getRequests() {
        return requests;
    }

    public void setRequests(Cola requests) {
        this.requests = requests;
    }    
    
    public void getClosest() {
        
    }
    
    public int getClosestScan(){
        int i = 0;
        int closest = 100;
        int current;
        Request closestRequest = new Request(10000, 10000, 10000);
        Request currentRequest;
        while (i < getRequests().getCount()) {
            if (getDirection() == 1) {
                if (getHeaderPosition() < ((Request) getRequests().get(i)).getFileAdd()){
                    current = ((Request) getRequests().get(i)).getFileAdd() - getHeaderPosition();
                    currentRequest = (Request) getRequests().get(i);
                    if (current < closest){
                        closest = current;
                        closestRequest = currentRequest;
                    }
                }
            } else {
                if (getHeaderPosition() > ((Request) getRequests().get(i)).getFileAdd()){
                    current = getHeaderPosition() - ((Request) getRequests().get(i)).getFileAdd();
                    currentRequest = (Request) getRequests().get(i);
                    if (current < closest){
                        closest = current;
                        closestRequest = currentRequest;
                    }
                }
            }
        }
        return closestRequest.getFileId();
    }       
}
