/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.openfile;


import java.io.File;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import java.awt.event.*;


// XXX This entire class should be refactored using form.
/**
 * Panel offering mounting points to user, when opening .java file.
 */
public class PackagePanel extends JPanel {

    private File f;
    
    private int pkgLevel;
    
    private List dirs;
    
    private List pkgs;

    
    /** Creates new form PackagePanel */
    public PackagePanel(File f, int pkgLevel, List dirs, List pkgs) {
        this.f = f;
        this.pkgLevel = pkgLevel;
        this.dirs = dirs;
        this.pkgs = pkgs;
        
        initComponents2();
        
        initAccessibility();
    }
    
    
    JButton getOKButton() {
        return okButton;
    }
    
    JButton getCancelButton() {
        return cancelButton;
    }
    
    JList getList() {
        return list;
    }

    /** */
    private void initComponents2() {
        okButton = new JButton (SettingsBeanInfo.getString ("LBL_okButton"));
        cancelButton = new JButton (SettingsBeanInfo.getString ("LBL_cancelButton"));
        list = new JList(pkgs.toArray());
        
        setLayout (new BorderLayout (0, 5));
        setBorder (new javax.swing.border.EmptyBorder (8, 8, 8, 8));

        textArea = new JTextArea ();
        //textArea.setBackground (new Color(204, 204, 204));
        textArea.setDisabledTextColor (javax.swing.UIManager.getColor ("Label.foreground"));
        //textArea.setFont (new Font ("SansSerif", Font.PLAIN, 11)); // NOI18N
        textArea.setFont (javax.swing.UIManager.getFont ("Label.font"));
        textArea.setText (SettingsBeanInfo.getString (pkgLevel == -1 ? "TXT_whereMountNoSuggest" : "TXT_whereMountSuggest", f.getName ()));        
        textArea.setEditable (false);
        textArea.setEnabled (false);
        textArea.setOpaque (false);
        textArea.setLineWrap (true);
        textArea.setWrapStyleWord (true);
        add (textArea, BorderLayout.NORTH);        
        
        list.setVisibleRowCount (5);
        list.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        if (pkgLevel != -1) list.setSelectedIndex (pkgLevel);
        list.setCellRenderer (new ListCellRenderer () {
            private Icon folderIcon = new ImageIcon (OpenFile.class.getResource ("folder.gif")); // NOI18N
            private Icon rootFolderIcon = new ImageIcon (OpenFile.class.getResource ("rootFolder.gif")); // NOI18N
            private final JLabel lab = new JLabel();
            
            public Component getListCellRendererComponent (JList lst, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String pkg2 = (String) value;
                if (pkg2.equals ("")) { // NOI18N
                    lab.setText (SettingsBeanInfo.getString ("LBL_packageWillBeDefault"));
                    lab.setIcon (rootFolderIcon);
                } else {
                    lab.setText (SettingsBeanInfo.getString ("LBL_packageWillBe", pkg2));
                    lab.setIcon (folderIcon);
                }
                if (isSelected) {
                    lab.setBackground (lst.getSelectionBackground ());
                    lab.setForeground (lst.getSelectionForeground ());
                } else {
                    lab.setBackground (lst.getBackground ());
                    lab.setForeground (lst.getForeground ());
                }
                lab.setEnabled (lst.isEnabled ());
                lab.setFont (lst.getFont ());
                lab.setOpaque (true);
                return lab;
            }
        });
        add (new JScrollPane (list), BorderLayout.CENTER);

        // Name of mount point:
        //final JLabel label = new JLabel ();
        final JTextField field = new JTextField ();
        //label.setFont (new Font ("Monospaced", Font.PLAIN, 12)); // NOI18N
        //add (label, BorderLayout.SOUTH);
        field.setEditable(false);
        field.setEnabled(true);
	//Accessibility
        field.getAccessibleContext ().setAccessibleDescription (SettingsBeanInfo.getString ("ACS_Field"));
        field.selectAll();
        field.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                field.selectAll();
            }
            
            public void focusLost(java.awt.event.FocusEvent e){
            }            
	});        
        add (field, BorderLayout.SOUTH);
                
        setPreferredSize (new Dimension (450, 300));

        list.addListSelectionListener (new ListSelectionListener () {
                                           public void valueChanged (ListSelectionEvent ev) {
                                               updateLabelEtcFromList (field, list, dirs, okButton);
                                           }
                                       });
        updateLabelEtcFromList (field, list, dirs, okButton);
    }

    private void initAccessibility() {        
        this.getAccessibleContext ().setAccessibleDescription (textArea.getText ());
        okButton.getAccessibleContext ().setAccessibleDescription (SettingsBeanInfo.getString ("ACS_LBL_okButton"));
        cancelButton.getAccessibleContext ().setAccessibleDescription (SettingsBeanInfo.getString ("ACS_LBL_cancelButton"));
        list.getAccessibleContext().setAccessibleName(SettingsBeanInfo.getString ("ACSN_List"));
        list.getAccessibleContext ().setAccessibleDescription (SettingsBeanInfo.getString ("ACSD_List"));       
    }
        
    /** Updates label and enables/disables ok button. */
    private static void updateLabelEtcFromList (JTextField field, JList list, List dirs, JButton okButton) {
        int idx = list.getSelectedIndex ();
        if (idx == -1) {
            field.setText (" "); // NOI18N
            field.getAccessibleContext().setAccessibleName(" ");
            okButton.setEnabled (false);
        } else {
            File dir = (File) dirs.get(idx);
            field.setText (SettingsBeanInfo.getString ("LBL_dirWillBe", dir.getAbsolutePath ()));
            field.getAccessibleContext().setAccessibleName(SettingsBeanInfo.getString ("LBL_dirWillBe", dir.getAbsolutePath ()));
            okButton.setEnabled (true);
        }
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        setLayout(new java.awt.BorderLayout());

    }//GEN-END:initComponents


    private JButton okButton;
    private JButton cancelButton;
    private JList list;
    private JTextArea textArea;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
