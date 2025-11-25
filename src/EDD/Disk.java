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
    private Block[] spaces;
    private int totalBlocks;
    private int availableBlocks;
    private int headerPosition;
    private int direction;
    private int planification;
    private Cola requests;

    public Disk() {
        this.spaces = new Block[64];
        this.totalBlocks = 64;
        this.availableBlocks = totalBlocks;
        this.headerPosition = 0;
        this.direction = 0;
        this.planification = 0;
        this.requests = new Cola();
    }

    public Block[] getSpaces() {
        return spaces;
    }

    public void setSpaces(Block[] spaces) {
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
    
    public void switchDirection() {
        if (getDirection() == 1){
            setDirection(-1);
        } else {
            setDirection(1);
        }
    }
    
    public Request manageRequests(){

        Request requestToAttend = null;
        switch (getPlanification()) {
        //FIFO
            case 0:
                requestToAttend = getNextFifo();
                break;
        //SCAN
            case 1:
                requestToAttend = getClosestScan();
                break;
        //C-SCAN
            case 2:
                requestToAttend = getClosestCscan();
                break;
        //SSTF
            case 3:
                requestToAttend = getClosestSsft();
                break;
        }
        return requestToAttend;
    }
    
    public Request getClosestSsft() {
        int i = 0;
        int index = -1;
        int closest = 100;
        int current;
        Request closestRequest = new Request(10000, "", 10000);
        Request currentRequest;
        while (i < getRequests().getCount()) {
            if (getHeaderPosition() < ((Request) getRequests().get(i)).getFileAdd()){
                current = ((Request) getRequests().get(i)).getFileAdd() - getHeaderPosition();
                currentRequest = (Request) getRequests().get(i);
                if (current < closest){
                    closest = current;
                    closestRequest = currentRequest;
                    index = i;
                }
            } else {
                current = getHeaderPosition() - ((Request) getRequests().get(i)).getFileAdd();
                currentRequest = (Request) getRequests().get(i);
                if (current < closest){
                    closest = current;
                    closestRequest = currentRequest;
                    index = i;
                }
            }
            i++;
        }
        if (closestRequest.getFileAdd() == 10000){
            return null;
        }
        getRequests().removeAt(index);
        return closestRequest;
    }
    
    public Request getClosestScan(){
        int i = 0;
        int index = -1;
        int closest = 100;
        int current;
        Request closestRequest = new Request(10000, "", 10000);
        Request currentRequest;
        while (i < getRequests().getCount()) {
            if (getDirection() == 1) {
                if (getHeaderPosition() < ((Request) getRequests().get(i)).getFileAdd()){
                    current = ((Request) getRequests().get(i)).getFileAdd() - getHeaderPosition();
                    currentRequest = (Request) getRequests().get(i);
                    if (current < closest){
                        closest = current;
                        closestRequest = currentRequest;
                        index = i;
                    }
                }
            } else {
                if (getHeaderPosition() > ((Request) getRequests().get(i)).getFileAdd()){
                    current = getHeaderPosition() - ((Request) getRequests().get(i)).getFileAdd();
                    currentRequest = (Request) getRequests().get(i);
                    if (current < closest){
                        closest = current;
                        closestRequest = currentRequest;
                        index = i;
                    }
                }
            }
            i++;
        }
        if (closestRequest.getFileAdd() == 10000){
            switchDirection();
            i = 0;
            while (i < getRequests().getCount()) {
            if (getDirection() == 1) {
                if (getHeaderPosition() < ((Request) getRequests().get(i)).getFileAdd()){
                    current = ((Request) getRequests().get(i)).getFileAdd() - getHeaderPosition();
                    currentRequest = (Request) getRequests().get(i);
                    if (current < closest){
                        closest = current;
                        closestRequest = currentRequest;
                        index = i;
                    }
                }
            } else {
                if (getHeaderPosition() > ((Request) getRequests().get(i)).getFileAdd()){
                    current = getHeaderPosition() - ((Request) getRequests().get(i)).getFileAdd();
                    currentRequest = (Request) getRequests().get(i);
                    if (current < closest){
                        closest = current;
                        closestRequest = currentRequest;
                        index = i;
                    }
                }
            }
            i++;
            }
        }
        if (closestRequest.getFileAdd() == 10000){
            return null;
        }
        getRequests().removeAt(index);
        return closestRequest;
    }  
    
    public Request getClosestCscan(){
        int i = 0;
        int index = -1;
        int closestFront = 100;
        int farthestBack = -1;
        int current;
        Request closestRequest = new Request(10000, "", 10000);
        Request currentRequest;
        while (i < getRequests().getCount()) {
            if (getHeaderPosition() < ((Request) getRequests().get(i)).getFileAdd()){
                current = ((Request) getRequests().get(i)).getFileAdd() - getHeaderPosition();
                currentRequest = (Request) getRequests().get(i);
                if (current < closestFront){
                    closestFront = current;
                    closestRequest = currentRequest;
                    index = i;
                }
            }
            i++;
        }
        if (closestRequest.getFileAdd() == 10000){
            i = 0;
            while (i < getRequests().getCount()) {
            if (getHeaderPosition() > ((Request) getRequests().get(i)).getFileAdd()){
                current = getHeaderPosition() - ((Request) getRequests().get(i)).getFileAdd();
                currentRequest = (Request) getRequests().get(i);
                if (current > farthestBack){
                    farthestBack = current;
                    closestRequest = currentRequest;
                    index = i;
                }
            }
            i++;
            }
        }
        if (closestRequest.getFileAdd() == 10000){
            return null;
        }
        getRequests().removeAt(index);
        return closestRequest;
    }
    
    public Request getNextFifo(){
        Request next = (Request) getRequests().dequeue();
        return next;
    }
}
