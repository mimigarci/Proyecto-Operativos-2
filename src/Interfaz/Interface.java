/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Interfaz;

import EDD.Cola;
import EDD.Lista;
import EDD.PCB;
import EDD.Proceso;
import EDD.OS;
import EDD.QueueChangeListener;
import java.awt.Choice;
import java.awt.Color;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

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
    int time;
   // private Lista devices = operativeSystem.getDeviceTable();    //---> No creo que sea necesario, se accede directamente a lo que está dentro del sistema operativo
   // private Lista processList = operativeSystem.getProcessList();
    private int prevTerminatedCount = 0;
    private int busyTicks = 0;
    private int totalTicks = 0;
    
    private javax.swing.JPanel readyContainer;           // for jScrollPane3 (Cola de Listos)
    private javax.swing.JPanel blockedContainer;         // for jScrollPane5 (Cola de Bloqueados)
        
    /**
     * Creates new form Interfacesp
     */
    public Interface() {
        initComponents();
  
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
        
        process_type.add("I/O Bound");
        process_type.add("CPU Bound");
        
        planification_choose.add("Round Robin"); //0
        planification_choose.add("Priority Planification"); //1
        planification_choose.add("SPN"); //2
        planification_choose.add("Feedback"); //3
        planification_choose.add("FSS"); //4
        planification_choose.add("SRT"); //5
        
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
    
    
    /*
    
    public void addProcessToSystem(Proceso process){
    
        if (operativeSystem.canBeReady(process) == true){
            operativeSystem.getReadyQueue().enqueue(process.getPcb());
        } else {
            operativeSystem.getLongTermQueue().enqueue(process.getPcb());
        }
        
        Lista aux = operativeSystem.getProcessList();
        aux.add(process);
        
        operativeSystem.setProcessList(aux);
        /*
        System.out.println(operativeSystem.getReadyQueue().getCount());
        System.out.println(operativeSystem.getSuspendedReadyQueue().getCount());
        }
    
    public void runQuickAddDemo() {
    // Make sure called on EDT
    javax.swing.SwingUtilities.invokeLater(() -> {
        // create test Proceso objects using the same constructor you already used in create_processActionPerformed
        // Adjust arguments to match your Proceso constructor if needed
        // public Proceso(int id, String name, String bound, int instructions, int ioCicles, int satisfyCicles, int deviceToUse, int priority) {
        
        Proceso p1 = new Proceso(getId(), "Proceso "+getId(), "I/O Bound", 10, 0, 0, 1,1);   // expected Ready
        addProcessToSystem(p1);
        
        Proceso p2 = new Proceso(getId(), "Proceso "+getId(), "CPU Bound", 20, 0, 0, 1,1); // expected Blocked
        addProcessToSystem(p2);
        
        Proceso p3 = new Proceso(getId(), "Proceso "+getId(), "I/O Bound", 15, 0, 0, 2,1); // expected Suspended
        addProcessToSystem(p3);
        
        Proceso p4 = new Proceso(getId(), "Proceso "+getId(), "CPU Bound", 30, 0, 0, 3,1); // expected Suspended Blocked
        addProcessToSystem(p4);
        
        Proceso p5 = new Proceso(getId(), "Proceso "+getId(), "CPU Bound", 10, 0, 0, 1,2); // expected Suspended Blocked
        addProcessToSystem(p5);
        
        Proceso p6 = new Proceso(getId(), "Proceso "+getId(), "CPU Bound", 20, 0, 0, 2,2); // expected Suspended Blocked
        addProcessToSystem(p6);
        
        Proceso p7 = new Proceso(getId(), "Proceso "+getId(), "CPU Bound", 30, 0, 0, 3,2); // expected Suspended Blocked
        addProcessToSystem(p7);
        
        Proceso p8 = new Proceso(getId(), "Proceso "+getId(), "CPU Bound", 40, 0, 0, 1,2); // expected Suspended Blocked
        addProcessToSystem(p8);
        
        Proceso p9 = new Proceso(getId(), "Proceso "+getId(), "CPU Bound", 10, 0, 0, 2,3); // expected Suspended Blocked
        addProcessToSystem(p9);
        
        Proceso p10 = new Proceso(getId(), "Proceso "+getId(), "CPU Bound", 20, 0, 0, 3,3); // expected Suspended Blocked
        addProcessToSystem(p10);
        
        Proceso p11 = new Proceso(getId(), "Proceso "+getId(), "CPU Bound", 30, 0, 0, 1,3); // expected Suspended Blocked
        addProcessToSystem(p11);
        
        Proceso p12 = new Proceso(getId(), "Proceso "+getId(), "CPU Bound", 40, 0, 0, 2,3); // expected Suspended Blocked
        addProcessToSystem(p12);
        
        Proceso p13 = new Proceso(getId(), "Proceso "+getId(), "CPU Bound", 10, 0, 0, 3,4); // expected Suspended Blocked
        addProcessToSystem(p13);
        
        Proceso p14 = new Proceso(getId(), "Proceso "+getId(), "CPU Bound", 20, 0, 0, 1,4); // expected Suspended Blocked
        addProcessToSystem(p14);
        
        Proceso p15 = new Proceso(getId(), "Proceso "+getId(), "CPU Bound", 30, 0, 0, 2,4); // expected Suspended Blocked
        addProcessToSystem(p15);
        
        Proceso p16 = new Proceso(getId(), "Proceso "+getId(), "CPU Bound", 40, 0, 0, 3,4); // expected Suspended Blocked
        addProcessToSystem(p16);

    });

    }*/
    
    public int getReadyContainerCount()       { return readyContainer == null ? 0 : readyContainer.getComponentCount(); }
    public int getBlockedContainerCount()     { return blockedContainer == null ? 0 : blockedContainer.getComponentCount(); }
    
    /*
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
    */
    
    /**
    * Update the terminated-processes text area from the OS terminated list.
    * This runs on the EDT because javax.swing.Timer events are delivered on the EDT.
    */
    
    /*
    private void updateActualProcess() {
        if (show_actual == null) return;

        String txt = "";
        try {
            synchronized (operativeSystem) {
                if (operativeSystem != null && operativeSystem.getProcessList().count() > 0) {
                    Proceso active = operativeSystem.getDispatcher().getActiveProcess(operativeSystem.getProcessList());
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
*/
    
    /*
    private void startSchedulerBackground() {
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
    // only after scheduler finishes, post minimal UI updates to EDT:
    javax.swing.SwingUtilities.invokeLater(() -> {
        // refresh all relevant lists with the correct queues
        refreshReadyList(operativeSystem.getReadyQueue());
        refreshBlockedList(operativeSystem.getBlockedQueue());
        refreshSuspendedReadyList(operativeSystem.getSuspendedReadyQueue());
        refreshSuspendedBlockedList(operativeSystem.getSuspendedBlockedQueue());
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

    */
    
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
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        selection = new JTabbedPane();
        sim_panel = new Panel();
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
        interrupt_handled = new JSpinner();
        process_type = new Choice();
        create_process = new JButton();
        jLabel3 = new JLabel();
        jLabel14 = new JLabel();
        panel3 = new Panel();
        jLabel5 = new JLabel();
        planification_choose = new Choice();
        save_policy = new JButton();
        global_clock = new JLabel();
        generate_processes = new JButton();
        global_clock1 = new JLabel();
        jScrollPane3 = new JScrollPane();
        jScrollPane9 = new JScrollPane();
        sim_panel1 = new Panel();
        panel6 = new Panel();
        jLabel7 = new JLabel();
        jScrollPane2 = new JScrollPane();
        show_terminated1 = new JTextArea();
        jLabel12 = new JLabel();
        jScrollPane7 = new JScrollPane();
        show_actual1 = new JTextArea();
        panel7 = new Panel();
        jLabel4 = new JLabel();
        label7 = new Label();
        label13 = new Label();
        label15 = new Label();
        interrupt_handled1 = new JSpinner();
        process_type2 = new Choice();
        create_process1 = new JButton();
        label17 = new Label();
        label18 = new Label();
        interrupt_cicle1 = new JSpinner();
        label19 = new Label();
        process_device1 = new Choice();
        set_process_priority1 = new JSpinner();
        process_type3 = new Choice();
        jLabel8 = new JLabel();
        panel8 = new Panel();
        jLabel13 = new JLabel();
        planification_choose1 = new Choice();
        save_policy1 = new JButton();
        global_clock2 = new JLabel();
        generate_processes1 = new JButton();
        global_clock3 = new JLabel();
        jScrollPane4 = new JScrollPane();
        config_panel = new Panel();
        panel4 = new Panel();
        jLabel9 = new JLabel();
        cicles_duration = new JSpinner();
        label12 = new Label();
        save_cicles = new JButton();
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
            .addGroup(GroupLayout.Alignment.TRAILING, panel1Layout.createSequentialGroup()
                .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 306, GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(GroupLayout.Alignment.LEADING, panel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel11, GroupLayout.PREFERRED_SIZE, 365, GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(GroupLayout.Alignment.LEADING, panel1Layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(jScrollPane6, GroupLayout.PREFERRED_SIZE, 306, GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
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
        label10.setText("Nombre del archivo");

        process_type.setForeground(new Color(51, 51, 51));

        create_process.setBackground(new Color(72, 149, 125));
        create_process.setForeground(new Color(255, 255, 255));
        create_process.setText("    Ejecutar    ");
        create_process.setBorder(null);
        create_process.setBorderPainted(false);
        create_process.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                create_processActionPerformed(evt);
            }
        });

        GroupLayout panel2Layout = new GroupLayout(panel2);
        panel2.setLayout(panel2Layout);
        panel2Layout.setHorizontalGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, panel2Layout.createSequentialGroup()
                .addGap(143, 143, 143)
                .addComponent(create_process, GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                .addGap(130, 130, 130))
            .addComponent(jLabel2, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panel2Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(label9, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(label10, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(interrupt_handled, GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
                    .addComponent(process_type, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(36, 36, 36))
        );
        panel2Layout.setVerticalGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(panel2Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel2)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(label9, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(process_type, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(label10, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(interrupt_handled, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addComponent(create_process, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
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
            .addComponent(jLabel5, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panel3Layout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(save_policy, GroupLayout.PREFERRED_SIZE, 136, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(GroupLayout.Alignment.TRAILING, panel3Layout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(planification_choose, GroupLayout.PREFERRED_SIZE, 273, GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41))
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

        global_clock.setFont(new Font("Segoe UI", 0, 48)); // NOI18N
        global_clock.setText("100");

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

        global_clock1.setFont(new Font("Segoe UI", 0, 24)); // NOI18N
        global_clock1.setText("segundos");

        GroupLayout sim_panelLayout = new GroupLayout(sim_panel);
        sim_panel.setLayout(sim_panelLayout);
        sim_panelLayout.setHorizontalGroup(sim_panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, sim_panelLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(sim_panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(panel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(sim_panelLayout.createSequentialGroup()
                        .addGroup(sim_panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                            .addComponent(panel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGroup(sim_panelLayout.createSequentialGroup()
                                .addComponent(global_clock, GroupLayout.PREFERRED_SIZE, 82, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(global_clock1, GroupLayout.PREFERRED_SIZE, 116, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(generate_processes, GroupLayout.PREFERRED_SIZE, 136, GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(177, 177, 177)
                .addGroup(sim_panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane3)
                    .addComponent(jLabel14, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane9, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 793, GroupLayout.PREFERRED_SIZE))
                .addGap(39, 39, 39))
        );
        sim_panelLayout.setVerticalGroup(sim_panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(sim_panelLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(sim_panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(sim_panelLayout.createSequentialGroup()
                        .addComponent(panel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(panel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(sim_panelLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel14)
                        .addGap(7, 7, 7)
                        .addComponent(jScrollPane9, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE)))
                .addGap(27, 27, 27)
                .addGroup(sim_panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(global_clock, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
                    .addGroup(sim_panelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(global_clock1, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
                        .addComponent(generate_processes, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(88, Short.MAX_VALUE))
        );

        selection.addTab("Simulación", sim_panel);

        panel6.setBackground(new Color(201, 255, 238));

        jLabel7.setFont(new Font("Century Gothic", 1, 12)); // NOI18N
        jLabel7.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel7.setText("Procesos terminados");

        show_terminated1.setEditable(false);
        show_terminated1.setColumns(20);
        show_terminated1.setRows(5);
        jScrollPane2.setViewportView(show_terminated1);

        jLabel12.setFont(new Font("Century Gothic", 1, 12)); // NOI18N
        jLabel12.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel12.setText("Proceso actual");

        show_actual1.setEditable(false);
        show_actual1.setColumns(20);
        show_actual1.setRows(5);
        jScrollPane7.setViewportView(show_actual1);

        GroupLayout panel6Layout = new GroupLayout(panel6);
        panel6.setLayout(panel6Layout);
        panel6Layout.setHorizontalGroup(panel6Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, panel6Layout.createSequentialGroup()
                .addGroup(panel6Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panel6Layout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, 306, GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(GroupLayout.Alignment.LEADING, panel6Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel12, GroupLayout.PREFERRED_SIZE, 365, GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(GroupLayout.Alignment.LEADING, panel6Layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(jScrollPane7, GroupLayout.PREFERRED_SIZE, 306, GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panel6Layout.setVerticalGroup(panel6Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(panel6Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel12)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane7, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        panel7.setBackground(new Color(201, 255, 238));

        jLabel4.setFont(new Font("Century Gothic", 1, 12)); // NOI18N
        jLabel4.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel4.setText("Crear Archivo");

        label7.setFont(new Font("Segoe UI", 0, 12)); // NOI18N
        label7.setForeground(new Color(51, 51, 51));
        label7.setText("Accion a ejecutar");

        label13.setFont(new Font("Segoe UI", 0, 12)); // NOI18N
        label13.setForeground(new Color(51, 51, 51));
        label13.setText("Privacidad");

        label15.setFont(new Font("Segoe UI", 0, 12)); // NOI18N
        label15.setForeground(new Color(51, 51, 51));
        label15.setText("Archivo");

        process_type2.setForeground(new Color(51, 51, 51));

        create_process1.setBackground(new Color(72, 149, 125));
        create_process1.setForeground(new Color(255, 255, 255));
        create_process1.setText("  Crear Proceso  ");
        create_process1.setBorder(null);
        create_process1.setBorderPainted(false);
        create_process1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                create_process1ActionPerformed(evt);
            }
        });

        label17.setFont(new Font("Segoe UI", 0, 12)); // NOI18N
        label17.setForeground(new Color(51, 51, 51));
        label17.setText("Prioridad");

        label18.setFont(new Font("Segoe UI", 0, 12)); // NOI18N
        label18.setForeground(new Color(51, 51, 51));
        label18.setText("Ciclo donde se interrumpe");

        label19.setFont(new Font("Segoe UI", 0, 12)); // NOI18N
        label19.setForeground(new Color(51, 51, 51));
        label19.setText("Dispositivo");

        process_device1.setForeground(new Color(51, 51, 51));

        process_type3.setForeground(new Color(51, 51, 51));

        GroupLayout panel7Layout = new GroupLayout(panel7);
        panel7.setLayout(panel7Layout);
        panel7Layout.setHorizontalGroup(panel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, panel7Layout.createSequentialGroup()
                .addGap(143, 143, 143)
                .addComponent(create_process1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(130, 130, 130))
            .addComponent(jLabel4, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panel7Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(panel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(panel7Layout.createSequentialGroup()
                        .addGroup(panel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(label7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(label13, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(label15, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(label17, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(panel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(panel7Layout.createSequentialGroup()
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(panel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                    .addComponent(interrupt_handled1, GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
                                    .addComponent(process_type2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(process_type3, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(panel7Layout.createSequentialGroup()
                                .addGap(59, 59, 59)
                                .addComponent(set_process_priority1))))
                    .addGroup(GroupLayout.Alignment.TRAILING, panel7Layout.createSequentialGroup()
                        .addGroup(panel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(label18, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(label19, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(panel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                            .addComponent(process_device1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(interrupt_cicle1, GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE))))
                .addGap(36, 36, 36))
        );
        panel7Layout.setVerticalGroup(panel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(panel7Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel4)
                .addGap(22, 22, 22)
                .addGroup(panel7Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(label7, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
                    .addComponent(process_type3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(label13, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(process_type2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(label15, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(interrupt_handled1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(panel7Layout.createSequentialGroup()
                        .addComponent(label17, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12))
                    .addGroup(GroupLayout.Alignment.TRAILING, panel7Layout.createSequentialGroup()
                        .addComponent(set_process_priority1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(panel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(label18, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(interrupt_cicle1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(label19, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(process_device1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(create_process1, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        jLabel8.setFont(new Font("Century Gothic", 1, 12)); // NOI18N
        jLabel8.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel8.setText("Cola de Procesos");

        panel8.setBackground(new Color(201, 255, 238));

        jLabel13.setFont(new Font("Century Gothic", 1, 12)); // NOI18N
        jLabel13.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel13.setText("Cambiar politica de planificación del disco");

        planification_choose1.setForeground(new Color(51, 51, 51));

        save_policy1.setBackground(new Color(72, 149, 125));
        save_policy1.setForeground(new Color(255, 255, 255));
        save_policy1.setText("Guardar Cambios");
        save_policy1.setBorder(null);
        save_policy1.setBorderPainted(false);
        save_policy1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                save_policy1ActionPerformed(evt);
            }
        });

        GroupLayout panel8Layout = new GroupLayout(panel8);
        panel8.setLayout(panel8Layout);
        panel8Layout.setHorizontalGroup(panel8Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jLabel13, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panel8Layout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(save_policy1, GroupLayout.PREFERRED_SIZE, 136, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(GroupLayout.Alignment.TRAILING, panel8Layout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(planification_choose1, GroupLayout.PREFERRED_SIZE, 273, GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41))
        );
        panel8Layout.setVerticalGroup(panel8Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(panel8Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel13)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(planification_choose1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(save_policy1, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        global_clock2.setFont(new Font("Segoe UI", 0, 48)); // NOI18N
        global_clock2.setText("100");

        generate_processes1.setBackground(new Color(72, 149, 125));
        generate_processes1.setForeground(new Color(255, 255, 255));
        generate_processes1.setText("  Crear 20 procesos  ");
        generate_processes1.setBorder(null);
        generate_processes1.setBorderPainted(false);
        generate_processes1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                generate_processes1ActionPerformed(evt);
            }
        });

        global_clock3.setFont(new Font("Segoe UI", 0, 24)); // NOI18N
        global_clock3.setText("segundos");

        GroupLayout sim_panel1Layout = new GroupLayout(sim_panel1);
        sim_panel1.setLayout(sim_panel1Layout);
        sim_panel1Layout.setHorizontalGroup(sim_panel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, sim_panel1Layout.createSequentialGroup()
                .addContainerGap(131, Short.MAX_VALUE)
                .addGroup(sim_panel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(sim_panel1Layout.createSequentialGroup()
                        .addComponent(global_clock2, GroupLayout.PREFERRED_SIZE, 82, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(global_clock3, GroupLayout.PREFERRED_SIZE, 116, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(generate_processes1, GroupLayout.PREFERRED_SIZE, 136, GroupLayout.PREFERRED_SIZE))
                    .addComponent(panel8, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panel7, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(sim_panel1Layout.createSequentialGroup()
                        .addComponent(panel6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(112, 112, 112)
                .addGroup(sim_panel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel8, GroupLayout.DEFAULT_SIZE, 793, Short.MAX_VALUE)
                    .addComponent(jScrollPane4))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        sim_panel1Layout.setVerticalGroup(sim_panel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(sim_panel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sim_panel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addGroup(GroupLayout.Alignment.LEADING, sim_panel1Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane4, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE))
                    .addGroup(GroupLayout.Alignment.LEADING, sim_panel1Layout.createSequentialGroup()
                        .addComponent(panel7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panel8, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panel6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(sim_panel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(global_clock2, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
                            .addGroup(sim_panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(global_clock3, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
                                .addComponent(generate_processes1, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        selection.addTab("Tabla de memoria", sim_panel1);

        panel4.setBackground(new Color(201, 255, 238));

        jLabel9.setFont(new Font("Century Gothic", 1, 12)); // NOI18N
        jLabel9.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel9.setText("Duración de ciclos de ejecución");

        label12.setFont(new Font("Segoe UI", 0, 12)); // NOI18N
        label12.setForeground(new Color(51, 51, 51));
        label12.setText("segundos");

        save_cicles.setBackground(new Color(72, 149, 125));
        save_cicles.setForeground(new Color(255, 255, 255));
        save_cicles.setText("  Guardar cambios  ");
        save_cicles.setBorder(null);
        save_cicles.setBorderPainted(false);
        save_cicles.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                save_ciclesActionPerformed(evt);
            }
        });

        GroupLayout panel4Layout = new GroupLayout(panel4);
        panel4.setLayout(panel4Layout);
        panel4Layout.setHorizontalGroup(panel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jLabel9, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(GroupLayout.Alignment.TRAILING, panel4Layout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .addGroup(panel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(GroupLayout.Alignment.TRAILING, panel4Layout.createSequentialGroup()
                        .addGroup(panel4Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(label12, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(cicles_duration, GroupLayout.PREFERRED_SIZE, 271, GroupLayout.PREFERRED_SIZE))
                        .addGap(36, 36, 36))
                    .addGroup(GroupLayout.Alignment.TRAILING, panel4Layout.createSequentialGroup()
                        .addComponent(save_cicles)
                        .addGap(111, 111, 111))))
        );
        panel4Layout.setVerticalGroup(panel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(panel4Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel9)
                .addGap(22, 22, 22)
                .addComponent(cicles_duration, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label12, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                .addComponent(save_cicles, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );

        panel5.setBackground(new Color(93, 154, 135));

        jLabel10.setFont(new Font("Century Gothic", 1, 12)); // NOI18N
        jLabel10.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel10.setText("Modo de ejecucion actual");

        execution_mode.setAlignment(Label.CENTER);
        execution_mode.setFont(new Font("Segoe UI", 1, 48)); // NOI18N
        execution_mode.setForeground(new Color(51, 51, 51));
        execution_mode.setText("1");

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
                .addContainerGap(798, Short.MAX_VALUE))
        );
        config_panelLayout.setVerticalGroup(config_panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(config_panelLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(config_panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(panel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(panel5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(462, Short.MAX_VALUE))
        );

        selection.addTab("Configuración", config_panel);

        GroupLayout graphics_panelLayout = new GroupLayout(graphics_panel);
        graphics_panel.setLayout(graphics_panelLayout);
        graphics_panelLayout.setHorizontalGroup(graphics_panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 1421, Short.MAX_VALUE)
        );
        graphics_panelLayout.setVerticalGroup(graphics_panelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 678, Short.MAX_VALUE)
        );

        selection.addTab("Gráficos", graphics_panel);

        getContentPane().add(selection);
        selection.getAccessibleContext().setAccessibleName("Gestion de archivos");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void save_ciclesActionPerformed(ActionEvent evt) {//GEN-FIRST:event_save_ciclesActionPerformed
        
    }//GEN-LAST:event_save_ciclesActionPerformed

    private void create_process1ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_create_process1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_create_process1ActionPerformed

    private void generate_processes1ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_generate_processes1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_generate_processes1ActionPerformed

    private void save_policy1ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_save_policy1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_save_policy1ActionPerformed

    private void create_processActionPerformed(ActionEvent evt) {//GEN-FIRST:event_create_processActionPerformed
        // create Process (use the correct text field for the name; set_process_name is the JTextField)
        

    }//GEN-LAST:event_create_processActionPerformed

    private void save_policyActionPerformed(ActionEvent evt) {//GEN-FIRST:event_save_policyActionPerformed
        // TODO add your handling code here:
        planification = planification_choose.getSelectedIndex();
        //startSchedulerBackground();
    }//GEN-LAST:event_save_policyActionPerformed

    private void generate_processesActionPerformed(ActionEvent evt) {//GEN-FIRST:event_generate_processesActionPerformed
        // TODO add your handling code here:
        //runQuickAddDemo();
    }//GEN-LAST:event_generate_processesActionPerformed

    
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
    private JSpinner cicles_duration;
    private Panel config_panel;
    private JButton create_process;
    private JButton create_process1;
    private Label execution_mode;
    private JButton generate_processes;
    private JButton generate_processes1;
    private JLabel global_clock;
    private JLabel global_clock1;
    private JLabel global_clock2;
    private JLabel global_clock3;
    private Panel graphics_panel;
    private JSpinner interrupt_cicle1;
    private JSpinner interrupt_handled;
    private JSpinner interrupt_handled1;
    private JLabel jLabel10;
    private JLabel jLabel11;
    private JLabel jLabel12;
    private JLabel jLabel13;
    private JLabel jLabel14;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JLabel jLabel7;
    private JLabel jLabel8;
    private JLabel jLabel9;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private JScrollPane jScrollPane3;
    private JScrollPane jScrollPane4;
    private JScrollPane jScrollPane6;
    private JScrollPane jScrollPane7;
    private JScrollPane jScrollPane9;
    private Label label10;
    private Label label12;
    private Label label13;
    private Label label15;
    private Label label17;
    private Label label18;
    private Label label19;
    private Label label7;
    private Label label9;
    private Panel panel1;
    private Panel panel2;
    private Panel panel3;
    private Panel panel4;
    private Panel panel5;
    private Panel panel6;
    private Panel panel7;
    private Panel panel8;
    private Choice planification_choose;
    private Choice planification_choose1;
    private Choice process_device1;
    private Choice process_type;
    private Choice process_type2;
    private Choice process_type3;
    private JButton save_cicles;
    private JButton save_policy;
    private JButton save_policy1;
    private JTabbedPane selection;
    private JSpinner set_process_priority1;
    private JTextArea show_actual;
    private JTextArea show_actual1;
    private JTextArea show_terminated;
    private JTextArea show_terminated1;
    private Panel sim_panel;
    private Panel sim_panel1;
    // End of variables declaration//GEN-END:variables

}
