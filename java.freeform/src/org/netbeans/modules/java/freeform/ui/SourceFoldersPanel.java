/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.freeform.ui;

import java.awt.FontMetrics;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.ant.freeform.spi.ProjectConstants;
import org.netbeans.modules.ant.freeform.spi.ProjectPropertiesPanel;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.java.freeform.JavaProjectGenerator;
import org.netbeans.modules.java.freeform.JavaProjectNature;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  David Konecny
 */
public class SourceFoldersPanel extends javax.swing.JPanel implements HelpCtx.Provider {
    
    private SourcesModel sourceFoldersModel;
    private ChangeListener listener;
    private boolean isWizard;
    private PropertyEvaluator evaluator;
    private ProjectModel model;
    private AntProjectHelper projectHelper;

    /** Creates new form SourceFoldersPanel */
    public SourceFoldersPanel() {
        this(true);
    }
    
    public SourceFoldersPanel(boolean isWizard) {
        this.isWizard = isWizard;
        initComponents();
        sourceFoldersModel = new SourcesModel();
        sourceFolders.setModel(sourceFoldersModel);
        initSourceLevel();
        updateColumnWidths();
        jLabel1.setVisible(!isWizard);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx( SourceFoldersPanel.class );
    }
    
    /** WizardDescriptor.Panel can set one change listener 
     * to be notified about changes in the panel. */
    public void setChangeListener(ChangeListener listener) {
        this.listener = listener;
    }
    
    private void initSourceLevel() {
        sourceLevel.addItem(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "LBL_SourceFoldersPanel_JDK13")); // NOI18N
        sourceLevel.addItem(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "LBL_SourceFoldersPanel_JDK14")); // NOI18N
        sourceLevel.addItem(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "LBL_SourceFoldersPanel_JDK15")); // NOI18N
    }
    
    private void updateButtons() {
        removeFolder.setEnabled(model.getSourceFoldersCount() > 0);
    }
    
    private void updateSourceLevelCombo(String sourceLevelValue) {
        if (sourceLevelValue.equals("1.3")) { // NOI18N
            sourceLevel.setSelectedIndex(0);
        } else if (sourceLevelValue.equals("1.4")) { // NOI18N
            sourceLevel.setSelectedIndex(1);
        } else if (sourceLevelValue.equals("1.5")) { // NOI18N
            sourceLevel.setSelectedIndex(2);
        } else {
            // user specified some other value in project.xml
            sourceLevel.addItem(sourceLevelValue);
            sourceLevel.setSelectedIndex(3);
        }
    }
    
    private String getSourceLevelValue(int index) {
        switch (index) {
            case 0: return "1.3"; // NOI18N
            case 1: return "1.4"; // NOI18N
            case 2: return "1.5"; // NOI18N
            default: return null;
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        sourceLevel = new javax.swing.JComboBox();
        addFolder = new javax.swing.JButton();
        removeFolder = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        sourceFolders = new javax.swing.JTable();

        setLayout(new java.awt.GridBagLayout());

        setMinimumSize(new java.awt.Dimension(200, 100));
        setPreferredSize(new java.awt.Dimension(247, 251));
        jLabel1.setLabelFor(this);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "LBL_SourceFoldersPanel_jLabel1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "ACSD_SourceFoldersPanel_jLabel1"));

        jLabel2.setLabelFor(sourceFolders);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "LBL_SourceFoldersPanel_jLabel2"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel2, gridBagConstraints);
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "ACSD_SourceFoldersPanel_jLabel2"));

        jLabel3.setLabelFor(sourceLevel);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "LBL_SourceFoldersPanel_jLabel3"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "ACSD_SourceFoldersPanel_jLabel3"));

        sourceLevel.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                sourceLevelItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(sourceLevel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addFolder, org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "BTN_SourceFoldersPanel_addFolder"));
        addFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFolderActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(addFolder, gridBagConstraints);
        addFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "ACSD_SourceFoldersPanel_addFolder"));

        org.openide.awt.Mnemonics.setLocalizedText(removeFolder, org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "BTN_SourceFoldersPanel_removeFolder"));
        removeFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeFolderActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(removeFolder, gridBagConstraints);
        removeFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "ACSD_SourceFoldersPanel_removeFolder"));

        jScrollPane1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jScrollPane1ComponentResized(evt);
            }
        });

        sourceFolders.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jScrollPane1.setViewportView(sourceFolders);
        sourceFolders.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "ACSD_SourceFoldersPanel_sourceFolders"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 12);
        add(jScrollPane1, gridBagConstraints);
        jScrollPane1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "ACSD_SourceFoldersPanel_jScrollPanel1"));

    }//GEN-END:initComponents

    private void jScrollPane1ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jScrollPane1ComponentResized
        updateColumnWidths();
    }//GEN-LAST:event_jScrollPane1ComponentResized

    private void sourceLevelItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_sourceLevelItemStateChanged
        if (sourceLevel.getSelectedIndex() != -1 && model != null) {
            String sl = getSourceLevelValue(sourceLevel.getSelectedIndex());
            if (sl == null) {
                sl = (String)sourceLevel.getSelectedItem();
            }
            model.setSourceLevel(sl);
        }
    }//GEN-LAST:event_sourceLevelItemStateChanged

    private void removeFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeFolderActionPerformed
        int index = sourceFolders.getSelectedRow();
        if (index == -1) {
            return;
        }
        String location = getItem(index).location;
        model.removeSourceFolder(index);
        sourceFoldersModel.fireTableDataChanged();        
        if (listener != null) {
            listener.stateChanged(null);
        }
        updateButtons();
    }//GEN-LAST:event_removeFolderActionPerformed

    private void addFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFolderActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        if (model.getBaseFolder() != null) {
            File files[] = model.getBaseFolder().listFiles();
            if (files != null && files.length > 0) {
                chooser.setSelectedFile(files[0]);
            } else {
                chooser.setSelectedFile(model.getBaseFolder());
            }
        }
        chooser.setDialogTitle(NbBundle.getMessage(SourceFoldersPanel.class, "LBL_Browse_Source_Folder"));
        chooser.setMultiSelectionEnabled(true);
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File files[] = chooser.getSelectedFiles();
            for (int i=0; i<files.length; i++) {
                File sourceLoc = FileUtil.normalizeFile(files[i]);
                JavaProjectGenerator.SourceFolder sf = new JavaProjectGenerator.SourceFolder();
                sf.location = Util.relativizeLocation(model.getBaseFolder(), model.getNBProjectFolder(), sourceLoc);
                sf.type = ProjectModel.TYPE_JAVA;
                sf.style = JavaProjectNature.STYLE_PACKAGES;
                sf.label = getDefaultLabel(sf.location);
                model.addSourceFolder(sf);
            }
            sourceFoldersModel.fireTableDataChanged();
            if (listener != null) {
                listener.stateChanged(null);
            }
            updateColumnWidths();
            updateButtons();
        }
    }//GEN-LAST:event_addFolderActionPerformed
    
    private String getDefaultLabel(String location) {
        // #47386 - remove "${project.dir}/" from label
        String relloc = location;
        if (relloc.startsWith(ProjectConstants.PROJECT_LOCATION_PREFIX)) {
            relloc = relloc.substring(ProjectConstants.PROJECT_LOCATION_PREFIX.length());
        }
        return relloc.replace('/', File.separatorChar); // NOI18N // Fix for #45816 sourceLoc.getName();
    }
    
    /**
     * Convert given string value (e.g. "${project.dir}/src" to a file
     * and try to relativize it.
     */
    public static String getLocationDisplayName(PropertyEvaluator evaluator, File base, String val) {
        File f = Util.resolveFile(evaluator, base, val);
        if (f == null) {
            return val;
        }
        String location = f.getAbsolutePath();
        if (CollocationQuery.areCollocated(base, f)) {
            location = PropertyUtils.relativizeFile(base, f).replace('/', File.separatorChar); // NOI18N
        }
        return location;
    }
    
    private JavaProjectGenerator.SourceFolder getItem(int index) {
        return (JavaProjectGenerator.SourceFolder)model.getSourceFolder(index);
    }

    public boolean hasSomeSourceFolder() {
        return model.getSourceFoldersCount() > 0;
    }
    
    public static class Panel implements ProjectPropertiesPanel {
        
        private SourceFoldersPanel panel;
        private ProjectModel model;
        private AntProjectHelper projectHelper;
        
        public Panel(ProjectModel model, AntProjectHelper projectHelper) {
            this.model = model;
            this.projectHelper = projectHelper;
        }
    
        /* ProjectCustomizer.Panel save */
        public void storeValues() {
            // store changes always because model could be modified in ClasspathPanel or OutputPanel
            ProjectModel.saveProject(projectHelper, model);
        }    

        public String getDisplayName() {
            return NbBundle.getMessage(ClasspathPanel.class, "LBL_ProjectCustomizer_Category_Sources");
        }

        public JComponent getComponent() {
            if (panel == null) {
                panel = new SourceFoldersPanel(false);
                panel.setModel(model, projectHelper);
            }
            return panel;
        }

        public int getPreferredPosition() {
            return 300;
        }
        
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFolder;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton removeFolder;
    private javax.swing.JTable sourceFolders;
    private javax.swing.JComboBox sourceLevel;
    // End of variables declaration//GEN-END:variables

    public void setModel(ProjectModel model, AntProjectHelper projectHelper) {
        this.model = model;
        this.projectHelper = projectHelper;
        updateSourceLevelCombo(model.getSourceLevel());
        updateButtons();
        sourceFoldersModel.fireTableDataChanged();
        updateColumnWidths();
    }

    private void updateColumnWidths() {
        FontMetrics fm = sourceFolders.getFontMetrics(sourceFolders.getFont());
        TableColumnModel tcm = sourceFolders.getColumnModel();
        
        // calc the size so that columns take whole horizontal area
        int preferedWidthLabel = (int)(0.2f * jScrollPane1.getWidth());
        int preferedWidthFolder = jScrollPane1.getWidth()-preferedWidthLabel;
        if (isWizard) {
            preferedWidthFolder = jScrollPane1.getWidth();
        }
        // XXX: shorten it a bit to prevent horizontal scrollbar.
        preferedWidthFolder -= 5;
        
        if (updateColumnWidth(tcm, 0, preferedWidthFolder, fm) || updateColumnWidth(tcm, 1, preferedWidthLabel, fm)) {
            sourceFolders.doLayout();
        }
    }
    
    private boolean updateColumnWidth(TableColumnModel tcm, int index, int minSize, FontMetrics fm) {
        if (index >= sourceFoldersModel.getColumnCount()) {
            return false;
        }
        int widest = minSize;
        for (int i=0; i<sourceFoldersModel.getRowCount(); i++) {
            String val = (String)sourceFoldersModel.getValueAt(i, index);
            int width = fm.stringWidth(val);
            if (width > widest) {
                // XXX: enlarge width a bit
                widest = width + 5;
            }
        }
        TableColumn tc = tcm.getColumn(index);
        if (tc.getPreferredWidth() < widest) {
            tc.setPreferredWidth(widest);
            return true;
        }
        return false;
    }

    private class SourcesModel extends AbstractTableModel {
        
        public SourcesModel() {
        }
        
        public int getColumnCount() {
            return isWizard ? 1 : 2;
        }
        
        public String getColumnName(int column) {
            switch (column) {
                case 0: return org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "LBL_SourceFoldersPanel_Package");
                default: return org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "LBL_SourceFoldersPanel_Label");
            }
        }
        
        public int getRowCount() {
            return model == null ? 0 : model.getSourceFoldersCount();
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                String loc = getItem(rowIndex).location;
                loc = getLocationDisplayName(model.getEvaluator(), model.getNBProjectFolder(), loc);
                return loc;
            } else {
                return getItem(rowIndex).label;
            }
        }
        
        public boolean isCellEditable(int row, int column) {
            if (column == 1) {
                return true;
            } else {
                return false;
            }
        }
        
        public Class getColumnClass(int column) {
            return String.class;
        }
        
        public void setValueAt(Object val, int rowIndex, int columnIndex) {
            JavaProjectGenerator.SourceFolder sf = getItem(rowIndex);
            sf.label = (String)val;
            if (sf.label.length() == 0) {
                sf.label = getDefaultLabel(sf.location);
            }
        }
        
    }
    
}
