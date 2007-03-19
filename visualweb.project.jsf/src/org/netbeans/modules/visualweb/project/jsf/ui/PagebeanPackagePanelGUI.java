/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.project.jsf.ui;

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.openide.util.NbBundle;

/**
 *
 * @author  Po-Ting Wu
 */
public class PagebeanPackagePanelGUI extends javax.swing.JPanel implements DocumentListener {

    private Project project;
    private final List/*<ChangeListener>*/ listeners = new ArrayList();

    /** Creates new form SimpleTargetChooserGUI */
    public PagebeanPackagePanelGUI(Project project) {
        this.project = project;

        initComponents();
        initValues(project);

        packageTextField.getDocument().addDocumentListener(this);
    }

    public void initValues(Project project) {
        String packageName = JsfProjectUtils.getProjectProperty(project, JsfProjectConstants.PROP_JSF_PAGEBEAN_PACKAGE);
        if (packageName == null || packageName.length() == 0) {
            packageName = JsfProjectUtils.deriveSafeName(project.getProjectDirectory().getName());
        }
        packageTextField.setText(packageName);

        String version = JsfProjectUtils.getProjectVersion(project);
        packageTextField.setEditable(version == null || version.length() == 0);
    }

    public String getPackageName() {
        String text = packageTextField.getText().trim();
        
        if (text.length() == 0) {
            return null;
        } else {
            return text;
        }
    }

    public synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    public synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        List templist;
        synchronized (this) {
            templist = new ArrayList (listeners);
        }
        Iterator it = templist.iterator();
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
        }
    }
        
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        packageLabel = new javax.swing.JLabel();
        packageTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PagebeanPackagePanelGUI.class).getString("AD_PagebeanPackagePanelGUI"));
        packageLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(PagebeanPackagePanelGUI.class, "MNE_PagebeanPackage_Label").charAt(0));
        packageLabel.setLabelFor(packageTextField);
        packageLabel.setText(org.openide.util.NbBundle.getMessage(PagebeanPackagePanelGUI.class, "LBL_PagebeanPackage_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(packageLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(packageTextField, gridBagConstraints);
        packageTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PagebeanPackagePanelGUI.class).getString("AD_packageTextField"));

    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel packageLabel;
    private javax.swing.JTextField packageTextField;
    // End of variables declaration//GEN-END:variables

    // DocumentListener implementation -----------------------------------------
    
    public void changedUpdate(javax.swing.event.DocumentEvent e) {
        fireChange();
    }    
    
    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        fireChange();
    }
    
    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        fireChange();
    }
}
