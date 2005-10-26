/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard.winsys;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * the first panel in TopComponent wizard
 *
 * @author Milos Kleint
 */
final class BasicSettingsPanel extends BasicWizardIterator.Panel {
    
    private NewTCIterator.DataModel data;
    private boolean listenersAttached = false;
    /**
     * Creates new form BasicSettingsPanel
     */
    public BasicSettingsPanel(WizardDescriptor setting, NewTCIterator.DataModel data) {
        super(setting);
        this.data = data;
        initComponents();
        initAccessibility();
        setupCombo();
        putClientProperty("NewFileWizard_Title", getMessage("LBL_TCWizardTitle"));
    }
    
    private void checkValidity() {
        //TODO: probably nothing...
        setErrorMessage(null);
    }
    
//    public void addNotify() {
//        super.addNotify();
//        attachDocumentListeners();
//        checkValidity();
//    }
//
//    public void removeNotify() {
//        // prevent checking when the panel is not "active"
//        removeDocumentListeners();
//        super.removeNotify();
//    }
//
//    private void attachDocumentListeners() {
//        if (!listenersAttached) {
//            listenersAttached = true;
//        }
//    }
//
//    private void removeDocumentListeners() {
//        if (listenersAttached) {
//            listenersAttached = false;
//        }
//    }
    
    private void setupCombo() {
        //TODO get dynamically from layers??
        String[] modes = null;
        try {
            FileSystem fs = LayerUtils.getEffectiveSystemFilesystem(data.getProject());
            FileObject foRoot = fs.getRoot().getFileObject("Windows2/Modes"); //NOI18N
            FileObject[] fos = foRoot.getChildren();
            Collection col = new ArrayList();
            for (int i=0; i < fos.length; i++) {
                if (fos[i].isData() && "wsmode".equals(fos[i].getExt())) { //NOI18N
                    col.add(fos[i].getName());
                }
            }
            modes = (String[])col.toArray(new String[col.size()]);
        } catch (IOException exc) {
            modes = new String[] {
                "editor", //NOI18N
                "explorer",//NOI18N
                "commonpalette",//NOI18N
                "output",//NOI18N
                "debugger",//NOI18N
                "properties",//NOI18N
                "navigator",//NOI18N
                "bottomSlidingSide",//NOI18N
                "leftSlidingSide",//NOI18N
                "rightSlidingSide"//NOI18N
            };
        }
        
        comMode.setModel(new DefaultComboBoxModel(modes));
    }
    
    protected void storeToDataModel() {
        data.setOpened(cbOpenedOnStart.isSelected());
        data.setMode((String)comMode.getSelectedItem());
    }
    
    protected void readFromDataModel() {
        cbOpenedOnStart.setSelected(data.isOpened());
        if (data.getMode() != null) {
            comMode.setSelectedItem(data.getMode());
        } else {
            comMode.setSelectedItem("output");//NOI18N
        }
        checkValidity();
    }
    
    protected String getPanelName() {
        return getMessage("LBL_BasicSettings_Title");
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(BasicSettingsPanel.class);
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(BasicSettingsPanel.class, key);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblMode = new javax.swing.JLabel();
        comMode = new javax.swing.JComboBox();
        cbOpenedOnStart = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        lblMode.setLabelFor(comMode);
        org.openide.awt.Mnemonics.setLocalizedText(lblMode, org.openide.util.NbBundle.getMessage(BasicSettingsPanel.class, "LBL_Mode"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(lblMode, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 6);
        add(comMode, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cbOpenedOnStart, org.openide.util.NbBundle.getMessage(BasicSettingsPanel.class, "LBL_OpenOnStart"));
        cbOpenedOnStart.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        cbOpenedOnStart.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(cbOpenedOnStart, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbOpenedOnStart;
    private javax.swing.JComboBox comMode;
    private javax.swing.JLabel lblMode;
    // End of variables declaration//GEN-END:variables
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(getMessage("ACS_BasicSettingsPanel"));
        cbOpenedOnStart.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_OpenOnStart"));
        comMode.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_Mode"));
    }
    
}
