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
    private Lista requests;

    public Disk() {
        this.spaces = new Object[64];
        this.totalBlocks = 64;
        this.availableBlocks = totalBlocks;
        this.headerPosition = 0;
        this.direction = 0;
        this.planification = 0;
        this.requests = new Lista();
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

    public Lista getRequests() {
        return requests;
    }

    public void setRequests(Lista requests) {
        this.requests = requests;
    }    
    
}
