/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.dlg;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import javax.swing.DefaultComboBoxModel;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.ListSelectionModel;

import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

import org.netbeans.modules.db.util.DriverListUtil;

public class AddDriverDialog extends javax.swing.JPanel {
    
    private DefaultListModel dlm;
    private List drvs;
    
    private final String BUNDLE = "org.netbeans.modules.db.resources.Bundle"; //NOI18N

    /** Creates new form AddDriverDialog1 */
    public AddDriverDialog() {
        initComponents();
        initAccessibility();
        dlm = (DefaultListModel) drvList.getModel();
        drvs = new LinkedList();
    }
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(BUNDLE).getString("ACS_AddDriverDialogA11yDesc")); //NOI18N
        drvListLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(BUNDLE).getString("ACS_AddDriverDriverFileA11yDesc")); //NOI18N
        drvList.getAccessibleContext().setAccessibleName(NbBundle.getBundle(BUNDLE).getString("ACS_AddDriverDriverFileListA11yName")); //NOI18N
        drvClassLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(BUNDLE).getString("ACS_AddDriverDriverDriverClassA11yDesc")); //NOI18N
        drvClassComboBox.getAccessibleContext().setAccessibleName(NbBundle.getBundle(BUNDLE).getString("ACS_AddDriverDriverDriverClassComboBoxA11yName")); //NOI18N
        nameLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(BUNDLE).getString("ACS_AddDriverDriverNameA11yDesc")); //NOI18N
        nameTextField.getAccessibleContext().setAccessibleName(NbBundle.getBundle(BUNDLE).getString("ACS_AddDriverDriverNameTextFieldA11yName")); //NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(BUNDLE).getString("ACS_AddDriverAddButtonA11yDesc")); //NOI18N
        findButton.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(BUNDLE).getString("ACS_AddDriverRemoveButtonA11yDesc")); //NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(BUNDLE).getString("ACS_AddDriverFindButtonA11yDesc")); //NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        drvListLabel = new javax.swing.JLabel();
        drvListScrollPane = new javax.swing.JScrollPane();
        drvList = new javax.swing.JList();
        browseButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        drvClassLabel = new javax.swing.JLabel();
        drvClassComboBox = new javax.swing.JComboBox();
        findButton = new javax.swing.JButton();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        drvListLabel.setDisplayedMnemonic(NbBundle.getBundle(BUNDLE).getString("AddDriverDriverFile_Mnemonic").charAt(0));
        drvListLabel.setLabelFor(drvList);
        drvListLabel.setText(NbBundle.getBundle(BUNDLE).getString("AddDriverDriverFile"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        add(drvListLabel, gridBagConstraints);

        drvList.setModel(new DefaultListModel());
        drvListScrollPane.setViewportView(drvList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 12);
        gridBagConstraints.weighty = 1.0;
        add(drvListScrollPane, gridBagConstraints);

        browseButton.setMnemonic(NbBundle.getBundle(BUNDLE).getString("AddDriverDriverAdd_Mnemonic").charAt(0));
        browseButton.setText(NbBundle.getBundle(BUNDLE).getString("AddDriverDriverAdd"));
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        add(browseButton, gridBagConstraints);

        removeButton.setMnemonic(NbBundle.getBundle(BUNDLE).getString("AddDriverDriverRemove_Mnemonic").charAt(0));
        removeButton.setText(NbBundle.getBundle(BUNDLE).getString("AddDriverDriverRemove"));
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        add(removeButton, gridBagConstraints);

        drvClassLabel.setDisplayedMnemonic(NbBundle.getBundle(BUNDLE).getString("AddDriverDriverClass_Mnemonic").charAt(0));
        drvClassLabel.setLabelFor(drvClassComboBox);
        drvClassLabel.setText(NbBundle.getBundle(BUNDLE).getString("AddDriverDriverClass"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 12);
        add(drvClassLabel, gridBagConstraints);

        drvClassComboBox.setEditable(true);
        drvClassComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                drvClassComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 12);
        add(drvClassComboBox, gridBagConstraints);

        findButton.setMnemonic(NbBundle.getBundle(BUNDLE).getString("AddDriverDriverFind_Mnemonic").charAt(0));
        findButton.setText(NbBundle.getBundle(BUNDLE).getString("AddDriverDriverFind"));
        findButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        add(findButton, gridBagConstraints);

        nameLabel.setDisplayedMnemonic(NbBundle.getBundle(BUNDLE).getString("AddDriverDriverName_Mnemonic").charAt(0));
        nameLabel.setLabelFor(nameTextField);
        nameLabel.setText(NbBundle.getBundle(BUNDLE).getString("AddDriverDriverName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 11, 12);
        add(nameLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 11, 12);
        gridBagConstraints.weightx = 1.0;
        add(nameTextField, gridBagConstraints);

    }//GEN-END:initComponents

    private void drvClassComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drvClassComboBoxActionPerformed
//        if (nameTextField.getText() == null || nameTextField.getText().equals(""))
            nameTextField.setText(DriverListUtil.findFreeName(DriverListUtil.getName((String) drvClassComboBox.getSelectedItem())));
    }//GEN-LAST:event_drvClassComboBoxActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        ListSelectionModel lsm = drvList.getSelectionModel();
        int count = dlm.getSize();
        int i = 0;
        
        if (count < 1)
            return;
        
        do {
            if (lsm.isSelectedIndex(i)) {
                dlm.remove(i);
                drvs.remove(i);
                count--;
                continue;
            }
            i++;
        } while (count != i);
    }//GEN-LAST:event_removeButtonActionPerformed

    private void findButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findButtonActionPerformed
        JarFile jf;
        Enumeration e;
        String className;
        Class c;
        Class[] cls;
        URLClassLoader loader = new URLClassLoader((URL[]) drvs.toArray(new URL[drvs.size()]));
        
        for (int i = 0; i < drvs.size(); i++) {
            try {
                jf = new JarFile(new File(((URL) drvs.get(i)).getFile()));
                e = jf.entries();
                while (e.hasMoreElements()) {
                    className = e.nextElement().toString();
                    if (className.endsWith(".class")) {
                        className = className.replace('/', '.');
                        className = className.substring(0, className.length() - 6);
                        try {
                            c = Class.forName(className, true, loader);
                            cls = c.getInterfaces();
                            for (int j = 0; j < cls.length; j++)
                                if (cls[j].equals(java.sql.Driver.class))
                                    addDriverClass(className);
                        } catch (ClassNotFoundException exc) {
                            //PENDING
                        } catch (ExceptionInInitializerError exc) {
                            //PENDING
                        } catch (NoClassDefFoundError exc) {
                            //PENDING
                        } catch (UnsatisfiedLinkError err) {
                            //PENDING
                        }
                    }
                }
                jf.close();
            } catch (IOException exc) {
                //PENDING
            }
        }

    }//GEN-LAST:event_findButtonActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        JFileChooser fc = new JFileChooser();
//        fc.setDialogTitle(bundle.getString("RecreateTableFileOpenDialogTitle")); //NOI18N
        fc.setDialogTitle("Select Driver"); //NOI18N
        fc.setMultiSelectionEnabled(true);
        fc.setAcceptAllFileFilterUsed(false);
        
        //.jar and .zip file filter
        fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return (f.isDirectory() || f.getName().endsWith(".jar") || f.getName().endsWith(".zip")); //NOI18N
            }
            
            public String getDescription() {
                return "Archive Files (*.jar, *.zip)";
//                return bundle.getString("Archive Files"); //NOI18N
            }
        });
        
        if (fc.showDialog(WindowManager.getDefault().getMainWindow(), "Add") == JFileChooser.APPROVE_OPTION) { //NOI18N
            File[] files = fc.getSelectedFiles();            
            for (int i = 0; i < files.length; i++)
                if (files[i] != null && files[i].isFile()) {
                    dlm.addElement(files[i].toString());
                    try {
                        drvs.add(files[i].toURL());
                    } catch (MalformedURLException exc) {
                        //PENDING
                    }
                    findDriverClass(files[i]);
                }
        }
    }//GEN-LAST:event_browseButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JComboBox drvClassComboBox;
    private javax.swing.JLabel drvClassLabel;
    private javax.swing.JList drvList;
    private javax.swing.JLabel drvListLabel;
    private javax.swing.JScrollPane drvListScrollPane;
    private javax.swing.JButton findButton;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
    
    public String getName() {
        return nameTextField.getText();
    }
    
    public List getDriverLocation() {
        return drvs;
    }
    
    public String getDriverClass() {
        return (String) drvClassComboBox.getSelectedItem();
    }
    
    private void findDriverClass(File drvFile) {
        JarFile jf;
        String drv;

        try {
            jf = new JarFile(drvFile);
            Set drvs = DriverListUtil.getDrivers();
            Iterator it = drvs.iterator();
            while (it.hasNext()) {
                drv = (String) it.next();
                if (jf.getEntry(drv.replace('.', '/') + ".class") != null) //NOI18N
                    addDriverClass(drv);
            }        
            jf.close();
        } catch (IOException exc) {
            //PENDING
        }
    }
    
    private void addDriverClass(String drv) {
        if (((DefaultComboBoxModel) drvClassComboBox.getModel()).getIndexOf(drv) < 0)
            drvClassComboBox.addItem(drv);
    }
    
}
