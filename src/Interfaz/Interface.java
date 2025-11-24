/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Interfaz;

import EDD.Block;
import EDD.Cola;
import EDD.Directory;
import EDD.Disk;
import EDD.File;
import EDD.Lista;
import EDD.PCB;
import EDD.Proceso;
import EDD.OS;
import EDD.QueueChangeListener;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.LayoutStyle;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author miche_ysmoa6e
 */
public class Interface extends javax.swing.JFrame {

    private OS operativeSystem = new OS(4000, 4);
    private Timer terminatedTimer, timeTimer;
    private int planification;
    private boolean isSchedulerActive = false;
    private Thread schedulerThread;
    private Disk disk = new Disk();
    private int actual_mode = 0; //0 ---> administrador, 1 ---> usuario
    
   // private Lista devices = operativeSystem.getDeviceTable();    //---> No creo que sea necesario, se accede directamente a lo que está dentro del sistema operativo
   // private Lista processList = operativeSystem.getProcessList();
    private int prevTerminatedCount = 0;
    private int busyTicks = 0;
    private int totalTicks = 0;
    
    private JTable table_files;
    private Lista allNodes = new Lista();
    private DefaultMutableTreeNode root;
    private DefaultTreeModel tree;
    private javax.swing.JPanel readyContainer;           // for jScrollPane3 (Cola de Listos)
    private javax.swing.JPanel blockedContainer;         // for jScrollPane5 (Cola de Bloqueados)
    
    
    /**
     * Creates new form Interfacesp
     */
    public Interface() {
        initComponents();
        setupScrollContainers();
        buildTable();
        buildTree();
        updateTree();
        updateTable();
    }
    
    
    private void setupScrollContainers() {
        // Use FlowLayout left-aligned so panels are placed side-by-side and aligned left
        readyContainer = new javax.swing.JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        readyContainer.setOpaque(true);

        blockedContainer = new javax.swing.JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        blockedContainer.setOpaque(true);


        if (jScrollPane3 != null) {
            jScrollPane3.setViewportView(readyContainer);
            jScrollPane3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            jScrollPane3.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        }
        if (jScrollPane9 != null) {
            jScrollPane9.setViewportView(blockedContainer);
            jScrollPane9.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            jScrollPane9.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        }
        
        crud_selection.add("Crear");
        crud_selection.add("Actualizar");
        crud_selection.add("Eliminar");
        
        privacy_selection.add("N/A");
        privacy_selection.add("Privado");
        privacy_selection.add("Publico");
        
        execution_mode_select.add("Modo usuario");
        execution_mode_select.add("Modo administrador");
        
        planification_choose.add("FIFO"); //0
        planification_choose.add("SCAN"); //1
        planification_choose.add("C-SCAN"); //2
        planification_choose.add("SSTF"); //3
        
        
    }
    
    private void registerQueueListeners() {
        if (operativeSystem == null) return;

        Cola ready = operativeSystem.getReadyQueue();
        Cola blocked = operativeSystem.getBlockedQueue();

        if (ready != null) {
            ready.addListener(new QueueChangeListener() {
                @Override
                public void queueChanged(Cola queue) {
                    refreshReadyList(queue);
                }
            });
        }

        if (blocked != null) {
            blocked.addListener(new QueueChangeListener() {
                @Override
                public void queueChanged(Cola queue) {
                    refreshBlockedList(queue);
                }
            });
        }

        // initial render (optional)
        refreshReadyList(ready);
        refreshBlockedList(blocked);
    }
    
    
    /**
     * Generic refresh helper to rebuild a container from a Cola (queue) that stores PCB objects.
     * This rebuilds the whole container (removeAll + add each item) using an atomic snapshot.
     */
    private void refreshContainerFromQueue(final Cola queue, final JPanel container, final JScrollPane pane) {
        if (queue == null || container == null) {
            return;
        }

        // Take an atomic snapshot off the shared queue to avoid concurrent modification problems
        final Object[] items = queue.snapshot();

        // Now perform all UI updates on the EDT
        javax.swing.SwingUtilities.invokeLater(() -> {
            container.removeAll();

            if (items == null || items.length == 0) {
                container.revalidate();
                container.repaint();
                if (pane != null) {
                    pane.revalidate();
                    pane.repaint();
                }
                return;
            }

            for (int i = 0; i < items.length; i++) {
                Object o = items[i];
                if (o == null) continue;

                PCB pcb;
                if (o instanceof PCB) {
                    pcb = (PCB) o;
                } else {
                    // If stored object is a Proceso, get its PCB
                    if (o instanceof Proceso) {
                        pcb = ((Proceso) o).getPcb();
                    } else {
                        // unknown type: skip
                        continue;
                    }
                }

                PanelProceso p = new PanelProceso(pcb.getId(), pcb.getName(), pcb.getStatus());
                container.add(p);
                container.add(Box.createRigidArea(new Dimension(0, 8)));
            }

            container.revalidate();
            container.repaint();
            if (pane != null) {
                pane.revalidate();
                pane.repaint();
            }
        });
       
    }

    // Convenience methods to refresh specific queues
    public void refreshReadyList(Cola readyQueue) {
        refreshContainerFromQueue(readyQueue, readyContainer, jScrollPane3);
        System.out.println("Ready: "+readyQueue.getCount());
    }

    public void refreshBlockedList(Cola blockedQueue) {
        refreshContainerFromQueue(blockedQueue, blockedContainer, jScrollPane9);
        System.out.println("Blocked: "+blockedQueue.getCount());
        System.out.println("Procesos: "+operativeSystem.getProcessList().count());
    }

    
    public void addPanelProceso(Proceso nodo) {
        if (nodo == null) return;

        javax.swing.SwingUtilities.invokeLater(() -> {
            PCB pcb = nodo.getPcb();
            if (pcb == null) return;

            PanelProceso p = new PanelProceso(pcb.getId(), pcb.getName(), pcb.getStatus());
            p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));

            // decide which container to add to
            JPanel targetContainer = getContainerForStatus(pcb.getStatus());
            JScrollPane targetScroll = getScrollPaneForContainer(targetContainer);

            targetContainer.add(p);
            targetContainer.add(Box.createRigidArea(new Dimension(0, 8)));

            targetContainer.revalidate();
            targetContainer.repaint();

            if (targetScroll != null) {
                targetScroll.revalidate();
                targetScroll.repaint();
            }
        });
    }
    
    private JPanel getContainerForStatus(String status) {
        
        /*
        boolean suspendedReady = status.contains("suspendedReady") ;
        boolean suspendedBlocked = status.contains("suspendedBlocked") ;
        boolean blocked = status.contains("blocked");
        boolean ready = status.contains("ready") ;
        */
        

        // blocked (but not suspended) -> blockedContainer
        if (status.equals("blocked")) {
            if (blockedContainer == null) setupScrollContainers();
            return blockedContainer;
        }

        // ready/listo OR unknown -> readyContainer (default)
        if (status.equals("ready")) {
            if (readyContainer == null) setupScrollContainers();
            return readyContainer;
        }
        return null;
    }
    
    private JScrollPane getScrollPaneForContainer(JPanel container) {
        if (container == null) return null;
        if (container == readyContainer) return jScrollPane3;
        if (container == blockedContainer) return jScrollPane9;
        return null;
    }
    
    

    public int getId(){
        int id;

        if (operativeSystem.getProcessList().count() > 0){
            id = operativeSystem.getProcessList().count() +1;
        } else {
            id = 1;
        }
        return id;
    }
    
    
    public int getReadyContainerCount()       { return readyContainer == null ? 0 : readyContainer.getComponentCount(); }
    public int getBlockedContainerCount()     { return blockedContainer == null ? 0 : blockedContainer.getComponentCount(); }
    
    
    private void updateTerminatedArea() {
        
        if (show_terminated == null) return; // defensive
        // printListProcess() returns a String (list of process names). If it returns null, guard it.
        String txt = "";
        if (operativeSystem != null && operativeSystem.getTerminatedProcessList() != null) {
            txt = operativeSystem.getTerminatedProcessList().printListProcess();
            if (txt == null) txt = "";
        }
        show_terminated.setText(txt);
        show_terminated.revalidate();
        show_terminated.repaint();
        
        updateActualProcess();
    }
    
    
    /**
    * Update the terminated-processes text area from the OS terminated list.
    * This runs on the EDT because javax.swing.Timer events are delivered on the EDT.
    */
    
    
    private void updateActualProcess() {
        if (show_actual == null) return;

        String txt = "";
        try {
            synchronized (operativeSystem) {
                if (operativeSystem != null && operativeSystem.getProcessList().count() > 0) {
                    Proceso active = operativeSystem.getActiveProcess();
                    if (active != null && active.getPcb() != null) {
                        PCB pcb = active.getPcb();
                        String name = pcb.getName();
                        txt = name == null ? "" : name;
                    } else {
                        txt = "";
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("updateActualProcess error: " + ex);
            txt = "";
        }
        // This must run on the EDT; if already called by Swing Timer / invokeLater it's fine.
        show_actual.setText(txt);
        show_actual.revalidate();
        show_actual.repaint();
    }

    
    
    private void startSchedulerBackground() {
        /*
        int selected = planification; // read atomic/volatile if planification can change concurrently
        //System.out.println(selected);
        if (operativeSystem.getReadyQueue().getCount() > 0) {
            switch (selected) {
                case 0 -> {
                    operativeSystem.executeRoundRobin();
                    //System.out.println("xddddddddddd");
                }
                case 1 -> operativeSystem.executePriorityPlanification();
                case 2 -> operativeSystem.executeSPN();
                case 3 -> operativeSystem.executeFeedback();
                case 4 -> operativeSystem.executeFSS();
                case 5 -> operativeSystem.executeSRT();
            }
        }
        */
        // only after scheduler finishes, post minimal UI updates to EDT:
        javax.swing.SwingUtilities.invokeLater(() -> {
            // refresh all relevant lists with the correct queues
            refreshReadyList(operativeSystem.getReadyQueue());
            refreshBlockedList(operativeSystem.getBlockedQueue());
            updateActualProcess();
            updateTerminatedArea();
        });
    }
    
    private void startSchedulerThread() {
        
        if (schedulerThread != null && schedulerThread.isAlive()) return;
        schedulerThread = new Thread(() -> {
            // run until interrupted (dispose() will interrupt)
            //System.out.println("ssss");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    startSchedulerBackground();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                // sleep between scheduler ticks, allow interruption to break early
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    // preserve interrupt status and exit loop
                    Thread.currentThread().interrupt();
                }
            }
        }, "SchedulerThread");
        schedulerThread.setDaemon(true);
        schedulerThread.start();
        isSchedulerActive = true;
    }

    
    @Override
    public void dispose() {
        // stop scheduler thread
        if (schedulerThread != null && schedulerThread.isAlive()) {
            schedulerThread.interrupt();
            try {
                schedulerThread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        super.dispose();
    }
    
    private void executeCrud(){
    
        if (null != crud_selection.getSelectedItem())
            switch (crud_selection.getSelectedItem()) {
            case "Crear" -> {
                if (searchNodeByName(root, file_name.toString()) == null){
                    int size = Integer.parseInt(JOptionPane.showInputDialog("Introduzca en numeros el tamaño del archivo"));
                    Boolean privacy = null;
                    switch (privacy_selection.getSelectedIndex()) {
                        case 1 -> privacy = false;
                        case 2 -> privacy = true;
                        default -> System.out.println("Debe seleccionar una privacidad para el archivo");
                    }
                    
                    if (privacy != null){
                        File file = new File(allNodes.count()+1, file_name.toString(), size, privacy);
                        
                        if ((searchNodeByName(root, file_directory.toString())) != null){
                            DefaultMutableTreeNode father = searchNodeByName(root, file_directory.toString());
                            DefaultMutableTreeNode child = new DefaultMutableTreeNode(file); 
                            father.add(child);
                            
                            assignSpaceInDisk(size, file.getFileBlocks()); // Llena la lista
                            selectSpacesInTable(file.getFileBlocks()); //Asigna las posiciones en la tabla
                            addToTable(file.getFileBlocks(), file.getColor());   //Pinta dentro de la tabla
                            // Falta agregar al disco
                            System.out.println("creado");
                        } else {
                            System.out.println("Directorio inválido");
                        }
                    }
                } else {
                    System.out.println("No se puede crear un archivo bajo ese nombre");
                }
            }
            case "Actualizar" -> {
                String new_name = JOptionPane.showInputDialog("Introduzca el nuevo nombre del archivo: ");
                Boolean can_update = true;
                
                if (!"".equals(new_name)){
                    for (int i = 0; i < allNodes.count(); i++){
                            DefaultMutableTreeNode aux = (DefaultMutableTreeNode)allNodes.get(i);
                            if (aux.toString().equals(new_name)){
                                JOptionPane.showMessageDialog(rootPane, "Ya existe un archivo con ese nombre, escoja otro.");
                                can_update = false;
                                break;
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(rootPane, "Debe introducir un nombre válido");
                }
                
                if (can_update == true){
                    file_name.toString();
                    file_directory.toString(); // Realmente este no es necesario pero bueno
                    
                    if (searchNodeByName(root, file_name.toString()) != null){
                        // Cambio de nombre en disco
                        System.out.println("actualizado");
                    } else {
                        System.out.println("No se encontro el archivo");
                    }
                    
                }
            }
            case "Eliminar" -> {
                
                if (searchNodeByName(root, file_name.toString()) != null){
                        DefaultMutableTreeNode child = searchNodeByName(root, file_name.toString());
                        DefaultMutableTreeNode parent = searchNodeByName(root, file_directory.toString());
                        
                        if (parent != null && child != null){
                            tree.removeNodeFromParent(child); //Esto elimina el nodo y sus hijos
                            // Función de eliminar archivo/directorio del disco
                        } else {
                            System.out.println("El archivo o el padre no existe. revise los datos");
                        }
                        
                        System.out.println("eliminado");
                } else {
                    System.out.println("No se encontro el archivo");
                }
            }
        }
        
        updateTree();
        updateTable();
    }
    
    
    public void buildTable(){
        int filas = 8;
        int columnas = 8;

        DefaultTableModel modelo = new DefaultTableModel(filas, columnas);
        JTable tabla = new JTable(modelo);
        
        // Inicializar la tabla con bloques vacíos
        for (int row = 0; row < filas; row++) {
            for (int col = 0; col < columnas; col++) {
                modelo.setValueAt(new Block(row * columnas + col), row, col);
            }
        }
        
        // Renderizador personalizado
        TableCellRenderer renderer = new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                JPanel panel = new JPanel();

                // Si el valor es un Color, úsalo
                if (value instanceof Block) {
                    panel.setBackground((((Block) value).getColor()));
                } else {
                    panel.setBackground(Color.WHITE); // por defecto
                }

                return panel;
            }
        };

        // Asignar el renderizador a todas las columnas
        for (int i = 0; i < columnas; i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        
        tabla.setRowHeight(80); // altura de cada row
        for (int i = 0; i < columnas; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(40);
        }
        
        // Crear bloques en disco
        for (int i = 0; i < 64; i++){
            Block newBlock = new Block(i);
            disk.getSpaces()[i] = newBlock;
        }
        
        jScrollPane2.setViewportView(tabla);
        table_files = tabla;
    }
    
    private void buildTree(){
        Directory main_dir = new Directory("Archivos");
        Directory dir1 = new Directory("Proyecto1");
        Directory dir2 = new Directory("Proyecto2");
        File file1 = new File(0, "main1", 5, true); 
        File file2 = new File(1, "main2", 5, false); 
        
        assignSpaceInDisk(file1.getSize(), file1.getFileBlocks()); // Llena la lista
        selectSpacesInTable(file1.getFileBlocks()); //Asigna las posiciones en la tabla
        addToTable(file1.getFileBlocks(), file1.getColor());  
        
        assignSpaceInDisk(file2.getSize(), file2.getFileBlocks()); // Llena la lista
        selectSpacesInTable(file2.getFileBlocks()); //Asigna las posiciones en la tabla
        addToTable(file2.getFileBlocks(), file2.getColor());  
         
        DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode(main_dir); // Aparece como si fuese un archivo pero realmente es un directorio
        DefaultMutableTreeNode child1 = new DefaultMutableTreeNode(dir1); 
        DefaultMutableTreeNode child2 = new DefaultMutableTreeNode(dir2); 
        DefaultMutableTreeNode file_1 = new DefaultMutableTreeNode(file1); 
        DefaultMutableTreeNode file_2= new DefaultMutableTreeNode(file2); 
        
        newRoot.add(child1);
        newRoot.add(child2);
        child1.add(file_1);
        child2.add(file_2);
        
        root = newRoot;
        DefaultTreeModel model = new DefaultTreeModel(root);
        tree = model;
        allNodes = getAllNodes(root);
        
        jTree1.setModel(model);
    }
    
    public DefaultMutableTreeNode searchNodeByName(DefaultMutableTreeNode root, String nombre) {
        // Si el nodo actual coincide, lo retornamos
        if (root.getUserObject().toString().equals(nombre)) {
            return root;
        }

        // Recorrer hijos recursivamente
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode hijo = (DefaultMutableTreeNode) root.getChildAt(i);
            DefaultMutableTreeNode resultado = searchNodeByName(hijo, nombre);
            if (resultado != null) {
                return resultado;
            }
        }
        
        return null;
    }
    
    public int countNodes(DefaultMutableTreeNode node) {
        int total = 1; // contar el node actual

        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode hijo = (DefaultMutableTreeNode) node.getChildAt(i);
            total += countNodes(hijo); // sumar los hijos recursivamente
        }

        return total;
    }

    public Lista getAllNodes(DefaultMutableTreeNode root) {
        Lista lista = new Lista();
        findNodes(root, lista);
        return lista;
    }

    private void findNodes(DefaultMutableTreeNode node, Lista lista) {
        // Agregar el node actual
        lista.add(node);

        // Recorrer hijos
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            findNodes(child, lista);
        }
    }

    
    private void updateTable(){
        allNodes = getAllNodes(root);
        String txt = ""; 
        
        // Limpia la tabla
        for (int row = 0; row < table_files.getRowCount(); row++) {
            for (int column = 0; column < table_files.getColumnCount(); column++) {
                Block aux = new Block(row);
                table_files.setValueAt(aux, row, column);
            }
        }
        
        System.out.println("sdwd");
        // Vuelve a pintar la tabla
        for (int i = 0; i < allNodes.count(); i++){
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) allNodes.get(i);
            if (node.getUserObject() instanceof File file){
                addToTable(file.getFileBlocks(), file.getColor());
                Block start = (Block) file.getFileBlocks().get(0);
                Block end = (Block) file.getFileBlocks().get((file.getFileBlocks().count()-1));
                
                txt += "\n" + file.getName() + ". Inicio: " + start.getX() + ", "+ start.getY() + ", Fin: " + end.getX() + ", " + end.getY(); 
            }
        }
        
        // Mostrar archivos registrados
        show_actual1.setText(txt);
    }
    
    
    
    public void addToTable(Lista blockList, Color color){
        for (int i = 0; i < blockList.count(); i++){
            Block aux = (Block) blockList.get(i);
            aux.setColor(color);
            table_files.setValueAt(aux, aux.getX(), aux.getY());
        }
    }
    
    public void assignSpaceInDisk(int amount, Lista fileList){
        int assigned = 0;
        Block[] spaces = disk.getSpaces();
        for (int i = 0; i < spaces.length && assigned < amount; i++){
            Block aux = spaces[i];
            if ("Empty".equals(aux.getContent())){
                aux.setContent("Occupied");
                fileList.add(aux);
                assigned++;
            }
        }
        
        if (assigned < amount) {
            System.out.println("Not enough space in disk!");
        }
    }
    
    public void selectSpacesInTable(Lista blockList){
        int count = 0;
        for (int row = 0; row < table_files.getRowCount(); row++) {
            for (int column = 0; column < table_files.getColumnCount(); column++) {
                Object valor = table_files.getValueAt(row, column);
                if (valor instanceof Block block) {
                    if (block.getColor().equals(Color.WHITE)){
                        if (count < blockList.count()){
                        Block aux = (Block) blockList.get(count);
                        aux.setX(row);
                        aux.setY(column);
                        count++;
                        } else {
                            break;
                        }      
                    }
                }
            }
        }
    }
    
    public Color selectColors(){
        int r = (int)(Math.random() * 256); // 0–255
        int g = (int)(Math.random() * 256);
        int b = (int)(Math.random() * 256);
        return new Color(r, g, b);
    }
    
    public void updateTree(){
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        selection = new JTabbedPane();
        archive = new Panel();
        panel1 = new Panel();
        jLabel6 = new JLabel();
        jScrollPane1 = new JScrollPane();
        show_terminated = new JTextArea();
        jLabel11 = new JLabel();
        jScrollPane6 = new JScrollPane();
        show_actual = new JTextArea();
        panel2 = new Panel();
        jLabel2 = new JLabel();
        label9 = new Label();
        label10 = new Label();
        crud_selection = new Choice();
        execute_crud = new JButton();
        file_name = new JTextField();
        file_directory = new JTextField();
        label11 = new Label();
        label13 = new Label();
        privacy_selection = new Choice();
        jLabel3 = new JLabel();
        jLabel14 = new JLabel();
        panel3 = new Panel();
        jLabel5 = new JLabel();
        planification_choose = new Choice();
        save_policy = new JButton();
        generate_processes = new JButton();
        jScrollPane3 = new JScrollPane();
        jScrollPane9 = new JScrollPane();
        panel9 = new Panel();
        jTree1 = new JTree();
        memory_table = new Panel();
        panel7 = new Panel();
        jScrollPane2 = new JScrollPane();
        jScrollPane7 = new JScrollPane();
        show_actual1 = new JTextArea();
        label12 = new Label();
        config_panel = new Panel();
        panel4 = new Panel();
        jLabel9 = new JLabel();
        save_mode = new JButton();
        execution_mode_select = new Choice();
        panel5 = new Panel();
        jLabel10 = new JLabel();
        execution_mode = new Label();
        graphics_panel = new Panel();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.LINE_AXIS));

        selection.setBackground(new Color(70, 202, 161));
        selection.setForeground(new Color(255, 255, 255));
        selection.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        selection.setFocusable(false);

        panel1.setBackground(new Color(201, 255, 238));

        jLabel6.setFont(new Font("Century Gothic", 1, 12)); // NOI18N
        jLabel6.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel6.setText("Procesos terminados");

        show_terminated.setEditable(false);
        show_terminated.setColumns(20);
        show_terminated.setRows(5);
        jScrollPane1.setViewportView(show_terminated);

        jLabel11.setFont(new Font("Century Gothic", 1, 12)); // NOI18N
        jLabel11.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel11.setText("Proceso actual");

        show_actual.setEditable(false);
        show_actual.setColumns(20);
        show_actual.setRows(5);
        jScrollPane6.setViewportView(show_actual);

        GroupLayout panel1Layout = new GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jLabel11, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel6, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panel1Layout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 306, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane6, GroupLayout.PREFERRED_SIZE, 306, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panel1Layout.setVerticalGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel11)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane6, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        panel2.setBackground(new Color(201, 255, 238));

        jLabel2.setFont(new Font("Century Gothic", 1, 12)); // NOI18N
        jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel2.setText("Gestionar archivos");

        label9.setFont(new Font("Segoe UI", 0, 12)); // NOI18N
        label9.setForeground(new Color(51, 51, 51));
        label9.setText("Accion a ejecutar");

        label10.setFont(new Font("Segoe UI", 0, 12)); // NOI18N
        label10.setForeground(new Color(51, 51, 51));
        label10.setText("Nombre del archivo o directorio");

        crud_selection.setForeground(new Color(51, 51, 51));

        execute_crud.setBackground(new Color(72, 149, 125));
        execute_crud.setForeground(new Color(255, 255, 255));
        execute_crud.setText("    Ejecutar    ");
        execute_crud.setBorder(null);
        execute_crud.setBorderPainted(false);
        execute_crud.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                execute_crudActionPerformed(evt);
            }
        });

        file_name.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                file_nameActionPerformed(evt);
            }
        });

        file_directory.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                file_directoryActionPerformed(evt);
            }
        });

        label11.setFont(new Font("Segoe UI", 0, 12)); // NOI18N
        label11.setForeground(new Color(51, 51, 51));
        label11.setText("Nombre del directorio");

        label13.setFont(new Font("Segoe UI", 0, 12)); // NOI18N
        label13.setForeground(new Color(51, 51, 51));
        label13.setText("Privacidad");

        privacy_selection.setForeground(new Color(51, 51, 51));

        GroupLayout panel2Layout = new GroupLayout(panel2);
        panel2.setLayout(panel2Layout);
        panel2Layout.setHorizontalGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, panel2Layout.createSequentialGroup()
                .addGap(143, 143, 143)
                .addComponent(execute_crud, GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                .addGap(130, 130, 130))
            .addComponent(jLabel2, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panel2Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(panel2Layout.createSequentialGroup()
                        .addComponent(label13, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(137, 137, 137)
                        .addComponent(privacy_selection, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(panel2Layout.createSequentialGroup()
                        .addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(label9, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(label10, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(label11, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                            .addComponent(file_directory, GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                            .addComponent(file_name, GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                            .addComponent(crud_selection, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(36, 36, 36))
        );
        panel2Layout.setVerticalGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(panel2Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel2)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 61, Short.MAX_VALUE)
                .addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(label9, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(crud_selection, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(label10, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(file_name, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(label11, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(file_directory, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(label13, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(privacy_selection, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addComponent(execute_crud, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        jLabel3.setFont(new Font("Century Gothic", 1, 12)); // NOI18N
        jLabel3.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel3.setText("Cola de Listos");

        jLabel14.setFont(new Font("Century Gothic", 1, 12)); // NOI18N
        jLabel14.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel14.setText("Cola de Bloqueados");

        panel3.setBackground(new Color(201, 255, 238));

        jLabel5.setFont(new Font("Century Gothic", 1, 12)); // NOI18N
        jLabel5.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel5.setText("Cambiar politica de planificación del disco");

        planification_choose.setForeground(new Color(51, 51, 51));

        save_policy.setBackground(new Color(72, 149, 125));
        save_policy.setForeground(new Color(255, 255, 255));
        save_policy.setText("Guardar Cambios");
        save_policy.setBorder(null);
        save_policy.setBorderPainted(false);
        save_policy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                save_policyActionPerformed(evt);
            }
        });

        GroupLayout panel3Layout = new GroupLayout(panel3);
        panel3.setLayout(panel3Layout);
        panel3Layout.setHorizontalGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jLabel5, GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
            .addGroup(panel3Layout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(save_policy, GroupLayout.PREFERRED_SIZE, 136, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(GroupLayout.Alignment.TRAILING, panel3Layout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(planification_choose, GroupLayout.PREFERRED_SIZE, 273, GroupLayout.PREFERRED_SIZE)
                .addGap(68, 68, 68))
        );
        panel3Layout.setVerticalGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(panel3Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel5)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(planification_choose, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(save_policy, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        generate_processes.setBackground(new Color(72, 149, 125));
        generate_processes.setForeground(new Color(255, 255, 255));
        generate_processes.setText("  Crear 10 archivos");
        generate_processes.setBorder(null);
        generate_processes.setBorderPainted(false);
        generate_processes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                generate_processesActionPerformed(evt);
            }
        });

        panel9.setBackground(new Color(201, 255, 238));

        GroupLayout panel9Layout = new GroupLayout(panel9);
        panel9.setLayout(panel9Layout);
        panel9Layout.setHorizontalGroup(panel9Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 793, Short.MAX_VALUE)
            .addGroup(panel9Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(panel9Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jTree1, GroupLayout.PREFERRED_SIZE, 771, GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        panel9Layout.setVerticalGroup(panel9Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 263, Short.MAX_VALUE)
            .addGroup(panel9Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(panel9Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jTree1, GroupLayout.PREFERRED_SIZE, 236, GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        GroupLayout archiveLayout = new GroupLayout(archive);
        archive.setLayout(archiveLayout);
        archiveLayout.setHorizontalGroup(archiveLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, archiveLayout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(archiveLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(archiveLayout.createSequentialGroup()
                        .addGroup(archiveLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(panel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(panel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(panel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(90, 90, 90))
                    .addGroup(archiveLayout.createSequentialGroup()
                        .addComponent(generate_processes, GroupLayout.PREFERRED_SIZE, 136, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(archiveLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addGroup(archiveLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane3)
                        .addComponent(jLabel14, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane9, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 793, GroupLayout.PREFERRED_SIZE))
                    .addComponent(panel9, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(72, 72, 72))
        );
        archiveLayout.setVerticalGroup(archiveLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(archiveLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(archiveLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addGroup(archiveLayout.createSequentialGroup()
                        .addComponent(panel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(panel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(generate_processes, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
                    .addGroup(archiveLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel14)
                        .addGap(7, 7, 7)
                        .addComponent(jScrollPane9, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panel9, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(66, Short.MAX_VALUE))
        );

        selection.addTab("Administrador de archivos", archive);

        panel7.setBackground(new Color(201, 255, 238));

        show_actual1.setEditable(false);
        show_actual1.setColumns(20);
        show_actual1.setFont(new Font("Century Gothic", 0, 14)); // NOI18N
        show_actual1.setRows(5);
        jScrollPane7.setViewportView(show_actual1);

        label12.setAlignment(Label.CENTER);
        label12.setFont(new Font("Century Gothic", 1, 24)); // NOI18N
        label12.setForeground(new Color(51, 51, 51));
        label12.setText("Archivos guardados");

        GroupLayout panel7Layout = new GroupLayout(panel7);
        panel7.setLayout(panel7Layout);
        panel7Layout.setHorizontalGroup(panel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(panel7Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, 972, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 53, Short.MAX_VALUE)
                .addGroup(panel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane7)
                    .addComponent(label12, GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE))
                .addContainerGap(34, Short.MAX_VALUE))
        );
        panel7Layout.setVerticalGroup(panel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(panel7Layout.createSequentialGroup()
                .addGroup(panel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(panel7Layout.createSequentialGroup()
                        .addGap(53, 53, 53)
                        .addComponent(label12, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(48, 48, 48)
                        .addComponent(jScrollPane7, GroupLayout.PREFERRED_SIZE, 466, GroupLayout.PREFERRED_SIZE))
                    .addGroup(panel7Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, 602, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        GroupLayout memory_tableLayout = new GroupLayout(memory_table);
        memory_table.setLayout(memory_tableLayout);
        memory_tableLayout.setHorizontalGroup(memory_tableLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(memory_tableLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(panel7, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        memory_tableLayout.setVerticalGroup(memory_tableLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(memory_tableLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(panel7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(65, Short.MAX_VALUE))
        );

        selection.addTab("Tabla de memoria", memory_table);

        panel4.setBackground(new Color(201, 255, 238));

        jLabel9.setFont(new Font("Century Gothic", 1, 12)); // NOI18N
        jLabel9.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel9.setText("Modo de ejecución");

        save_mode.setBackground(new Color(72, 149, 125));
        save_mode.setForeground(new Color(255, 255, 255));
        save_mode.setText("  Guardar cambios  ");
        save_mode.setBorder(null);
        save_mode.setBorderPainted(false);
        save_mode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                save_modeActionPerformed(evt);
            }
        });

        execution_mode_select.setForeground(new Color(51, 51, 51));

        GroupLayout panel4Layout = new GroupLayout(panel4);
        panel4.setLayout(panel4Layout);
        panel4Layout.setHorizontalGroup(panel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jLabel9, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(GroupLayout.Alignment.TRAILING, panel4Layout.createSequentialGroup()
                .addContainerGap(115, Short.MAX_VALUE)
                .addComponent(save_mode)
                .addGap(110, 110, 110))
            .addGroup(panel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, panel4Layout.createSequentialGroup()
                    .addContainerGap(58, Short.MAX_VALUE)
                    .addComponent(execution_mode_select, GroupLayout.PREFERRED_SIZE, 208, GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(61, Short.MAX_VALUE)))
        );
        panel4Layout.setVerticalGroup(panel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(panel4Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel9)
                .addGap(62, 62, 62)
                .addComponent(save_mode, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
            .addGroup(panel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, panel4Layout.createSequentialGroup()
                    .addContainerGap(55, Short.MAX_VALUE)
                    .addComponent(execution_mode_select, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(55, Short.MAX_VALUE)))
        );

        panel5.setBackground(new Color(93, 154, 135));

        jLabel10.setFont(new Font("Century Gothic", 1, 12)); // NOI18N
        jLabel10.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel10.setText("Modo de ejecucion actual");

        execution_mode.setAlignment(Label.CENTER);
        execution_mode.setFont(new Font("Segoe UI", 1, 48)); // NOI18N
        execution_mode.setForeground(new Color(51, 51, 51));
        execution_mode.setText("Admin");

        GroupLayout panel5Layout = new GroupLayout(panel5);
        panel5.setLayout(panel5Layout);
        panel5Layout.setHorizontalGroup(panel5Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jLabel10, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
            .addComponent(execution_mode, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panel5Layout.setVerticalGroup(panel5Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(panel5Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel10)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(execution_mode, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        GroupLayout config_panelLayout = new GroupLayout(config_panel);
        config_panel.setLayout(config_panelLayout);
        config_panelLayout.setHorizontalGroup(config_panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(config_panelLayout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(panel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addComponent(panel5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(818, Short.MAX_VALUE))
        );
        config_panelLayout.setVerticalGroup(config_panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(config_panelLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(config_panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(panel5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(panel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(551, Short.MAX_VALUE))
        );

        selection.addTab("Configuración", config_panel);

        GroupLayout graphics_panelLayout = new GroupLayout(graphics_panel);
        graphics_panel.setLayout(graphics_panelLayout);
        graphics_panelLayout.setHorizontalGroup(graphics_panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 1441, Short.MAX_VALUE)
        );
        graphics_panelLayout.setVerticalGroup(graphics_panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 723, Short.MAX_VALUE)
        );

        selection.addTab("Gráficos", graphics_panel);

        getContentPane().add(selection);
        selection.getAccessibleContext().setAccessibleName("Gestion de archivos");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void save_modeActionPerformed(ActionEvent evt) {//GEN-FIRST:event_save_modeActionPerformed
        if ("Modo usuario".equals(execution_mode_select.getSelectedItem())){
            setActual_mode(1);
            execution_mode.setText("User");
        } else {
            setActual_mode(0);
            execution_mode.setText("Admin");
        }
    }//GEN-LAST:event_save_modeActionPerformed

    private void execute_crudActionPerformed(ActionEvent evt) {//GEN-FIRST:event_execute_crudActionPerformed
        // create Process (use the correct text field for the name; set_process_name is the JTextField)
        updateTable();
        DefaultMutableTreeNode found = searchNodeByName(root, "main1");
        System.out.println(found.toString());
        if (actual_mode == 1){
            JOptionPane.showMessageDialog(rootPane, "No se encuentra en modo administrador, esta accción no puede ser ejecutada.");
        } else {
            executeCrud();
        }

    }//GEN-LAST:event_execute_crudActionPerformed

    private void save_policyActionPerformed(ActionEvent evt) {//GEN-FIRST:event_save_policyActionPerformed
        // TODO add your handling code here:
        planification = planification_choose.getSelectedIndex();
        //startSchedulerBackground();
    }//GEN-LAST:event_save_policyActionPerformed

    private void generate_processesActionPerformed(ActionEvent evt) {//GEN-FIRST:event_generate_processesActionPerformed
        // TODO add your handling code here:
        //runQuickAddDemo();
    }//GEN-LAST:event_generate_processesActionPerformed

    private void file_nameActionPerformed(ActionEvent evt) {//GEN-FIRST:event_file_nameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_file_nameActionPerformed

    private void file_directoryActionPerformed(ActionEvent evt) {//GEN-FIRST:event_file_directoryActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_file_directoryActionPerformed

    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Interface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Interface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Interface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Interface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Interface().setVisible(true);
            }
        });
               
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private Panel archive;
    private Panel config_panel;
    private Choice crud_selection;
    private JButton execute_crud;
    private Label execution_mode;
    private Choice execution_mode_select;
    private JTextField file_directory;
    private JTextField file_name;
    private JButton generate_processes;
    private Panel graphics_panel;
    private JLabel jLabel10;
    private JLabel jLabel11;
    private JLabel jLabel14;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JLabel jLabel9;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private JScrollPane jScrollPane3;
    private JScrollPane jScrollPane6;
    private JScrollPane jScrollPane7;
    private JScrollPane jScrollPane9;
    private JTree jTree1;
    private Label label10;
    private Label label11;
    private Label label12;
    private Label label13;
    private Label label9;
    private Panel memory_table;
    private Panel panel1;
    private Panel panel2;
    private Panel panel3;
    private Panel panel4;
    private Panel panel5;
    private Panel panel7;
    private Panel panel9;
    private Choice planification_choose;
    private Choice privacy_selection;
    private JButton save_mode;
    private JButton save_policy;
    private JTabbedPane selection;
    private JTextArea show_actual;
    private JTextArea show_actual1;
    private JTextArea show_terminated;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the actual_mode
     */
    public int getActual_mode() {
        return actual_mode;
    }

    /**
     * @param actual_mode the actual_mode to set
     */
    public void setActual_mode(int actual_mode) {
        this.actual_mode = actual_mode;
    }

}
