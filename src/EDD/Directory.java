/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EDD;

/**
 *
 * @author Eddy
 */
public class Directory {
    private String name;
    private Lista contents;
    
    public Directory(String name) {
        this.name = name;
        this.contents = new Lista();
    }

    public Lista getContents() {
        return contents;
    }

    public void setContents(Lista contents) {
        this.contents = contents;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
       
    @Override
    public String toString() {
        return name; // lo que se mostrará en el JTree
    }
}
