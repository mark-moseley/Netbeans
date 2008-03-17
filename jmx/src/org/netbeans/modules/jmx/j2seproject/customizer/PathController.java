/*
 * PathController.java
 *
 * Created on January 26, 2007, 4:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.jmx.j2seproject.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Enumeration;
import java.util.StringTokenizer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author jfdenise
 */
public class PathController implements ActionListener, ListSelectionListener, ListDataListener {
    private JList l;
    private JButton add;
    private JButton remove;
    private JButton up;
    private JButton down;
    private JFileChooser chooser;
    private DefaultListModel model;
    private JLabel label;
    private ListDataListener lstnr;
    public PathController(JList l, JLabel label, JButton add, JFileChooser chooser, JButton remove, JButton up, JButton down, ListDataListener lstnr) {
        this(l,label, createModel(""),add,chooser,remove,up,down, lstnr);
    }
    public PathController(JList l, JLabel label, String items, JButton add, JFileChooser chooser, JButton remove, JButton up, JButton down, ListDataListener lstnr) {
        this(l,label,createModel(items),add,chooser,remove,up,down, lstnr);
    }
    
    /** Creates a new instance of PathController */
    public PathController(JList l, JLabel label, DefaultListModel model, JButton add, JFileChooser chooser, JButton remove, JButton up, JButton down, ListDataListener lstnr) {
        this.l = l;
        this.label = label;
        this.model = model;
        this.add = add;
        this.remove = remove;
        this.up = up;
        this.down = down;
        this.chooser = chooser;
        
        this.lstnr = lstnr;
        
        l.setModel(model);
        if(model != null) {
            model.addListDataListener(this);
        }
        add.setActionCommand("add");// NOI18N
        remove.setActionCommand("remove");// NOI18N
        up.setActionCommand("up");// NOI18N
        down.setActionCommand("down");// NOI18N
        add.addActionListener(this);
        remove.addActionListener(this);
        up.addActionListener(this);
        down.addActionListener(this);
        l.addListSelectionListener(this);
        
        remove.setEnabled(false);
        up.setEnabled(false);
        down.setEnabled(false);
    }
    
    public void setEnabled(boolean b) {
        l.setEnabled(b);
        label.setEnabled(b);
        add.setEnabled(b);
        remove.setEnabled(remove.isEnabled() && b);
        up.setEnabled(up.isEnabled() && b);
        down.setEnabled(down.isEnabled() && b);
    }
    public void setVisible(boolean b) {
        l.setVisible(b);
        label.setVisible(b);
        add.setVisible(b);
        remove.setVisible(b);
        up.setVisible(b);
        down.setVisible(b);
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals( "add")) {// NOI18N
            int returnVal = chooser.showOpenDialog(null);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File[] selection = chooser.getSelectedFiles();
                int size = selection.length;
                StringBuffer buff = new StringBuffer();
                int end = l.getModel().getSize();
                for(int i = 0; i < size; i++) {
                    String path = selection[i].getAbsolutePath();
                    if(!model.contains(path))
                        model.add(end + i, path);
                }
            }
            return;
        }
        if (e.getActionCommand().equals( "remove")) {// NOI18N
            Object[] values = l.getSelectedValues();
            
            for(int i = 0; i < values.length; i++) {
                model.removeElement(values[i]);
            }
            if(model.getSize() == 0) {
                up.setEnabled(false);
                down.setEnabled(false);
                remove.setEnabled(false);
            }
            l.setSelectedIndex(0);
        }
        
        if (e.getActionCommand().equals("up")) {// NOI18N
            int selectedI = l.getSelectedIndex();
            Object selected = l.getSelectedValue();
            int newIndex = selectedI - 1;
            Object previous = model.getElementAt(newIndex);
            model.setElementAt(selected, newIndex);
            model.setElementAt(previous, selectedI);
            l.setSelectedIndex(newIndex);
            return;
        }
        
        if (e.getActionCommand().equals("down")) {// NOI18N
            int selectedI = l.getSelectedIndex();
            Object selected = l.getSelectedValue();
            int newIndex = selectedI + 1;
            Object next = model.getElementAt(newIndex);
            model.setElementAt(selected, newIndex);
            model.setElementAt(next, selectedI);
            l.setSelectedIndex(newIndex);
            return;
        }
    }
    
    // return the list of selected items
    public String toString() {
        Enumeration pluginsPath = model.elements();
        StringBuffer buffer = new StringBuffer();
        while(pluginsPath.hasMoreElements()) {
            Object path = pluginsPath.nextElement();
            buffer.append(path.toString());
            if(pluginsPath.hasMoreElements())
                buffer.append(File.pathSeparator);
        }
        return buffer.toString();
    }
    
    public synchronized void updateModel(String items) {
        if(items == null)
            return;
        ListModel m = l.getModel();
        if(m != null) {
            m.removeListDataListener(this);
        }
        
        model = createModel(items);
        model.addListDataListener(this);
        l.setModel(model);
    }
    
    public static DefaultListModel createModel(String items) {
        StringTokenizer tk = new StringTokenizer(items,File.pathSeparator);
        DefaultListModel model = new DefaultListModel();
        while(tk.hasMoreTokens()) {
            String path = tk.nextToken();
            model.addElement(path);
        }
        return model;
    }
    
    public void valueChanged(ListSelectionEvent e) {
        int[] indices = l.getSelectedIndices();
        if(indices.length != 1) {
            up.setEnabled(false);
            down.setEnabled(false);
            return;
        }
        int single = l.getSelectedIndex();
        up.setEnabled(true);
        down.setEnabled(true);
        
        if(model.getSize() > 0)
            remove.setEnabled(true);
        
        if(single == 0) {
            up.setEnabled(false);
            if(model.getSize() == 1)
                down.setEnabled(false);
        }
        
        if(single == model.getSize() -1)
            down.setEnabled(false);
    }
    
    public void intervalAdded(ListDataEvent arg0) {
        if(lstnr == null)
            return;
        lstnr.intervalAdded(arg0);
    }
    
    public void intervalRemoved(ListDataEvent arg0) {
        if(lstnr == null)
            return;
        lstnr.intervalRemoved(arg0);
    }
    
    public void contentsChanged(ListDataEvent arg0) {
        if(lstnr == null)
            return;
        lstnr.contentsChanged(arg0);
    }
    
}
