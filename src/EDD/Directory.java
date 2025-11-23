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
    private int id;
    private int depth;
    private Lista contents;
    //private Lista path;

    public Directory(int id, Lista path) {
        this.id = id;
        this.contents = new Lista();
        //this.path = path;
    }
    
    public Directory(String name) {
        this.name = name;
        this.contents = new Lista();
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Lista getContents() {
        return contents;
    }

    public void setContents(Lista contents) {
        this.contents = contents;
    }

    /*
    public Lista getPath() {
        return path;
    }

    public void setPath(Lista path) {
        this.path = path;
    }
    */

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
