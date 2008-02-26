/*
 * ShareableServerVisual.java
 *
 * Created on February 11, 2008, 9:33 AM
 */

package org.netbeans.modules.j2ee.common.project.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.common.project.ui.PanelSharability;
import org.netbeans.modules.j2ee.common.SharabilityUtility;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.spi.java.project.support.ui.SharableLibrariesUtils;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author  Petr Hejl
 */
final class PanelSharabilityVisual extends javax.swing.JPanel {

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private final PanelSharability panel;

    private String currentLibrariesLocation;

    private String preselectedLibraryName;

    private File projectLocation;

    public PanelSharabilityVisual(PanelSharability panel) {
        initComponents();
        setName(NbBundle.getMessage(PanelSharabilityVisual.class, "PanelSharabilityVisual.label"));

        this.panel = panel;
        currentLibrariesLocation = ".." + File.separatorChar + "libraries"; // NOI18N
        librariesLocation.setText(currentLibrariesLocation);
        librariesLocation.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                changeSupport.fireChange();
            }

            public void removeUpdate(DocumentEvent e) {
                changeSupport.fireChange();
            }

            public void changedUpdate(DocumentEvent e) {
                changeSupport.fireChange();
            }
        });

        libraryRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                changeSupport.fireChange();
            }
        });
        serverRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                changeSupport.fireChange();
            }
        });
        libraryNameComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                changeSupport.fireChange();
            }
        });
        libraryNameComboBox.getEditor().getEditorComponent().addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
                changeSupport.fireChange();
            }

            public void keyPressed(KeyEvent e) {
                changeSupport.fireChange();
            }

            public void keyReleased(KeyEvent e) {
                changeSupport.fireChange();
            }
        });
    }

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public JCheckBox getSharableProject() {
        return sharableProject;
    }
    
    public JRadioButton getLibraryRadioButton() {
        return libraryRadioButton;
    }

    public JComboBox getLibraryNameComboBox() {
        return libraryNameComboBox;
    }

    public void setProjectLocation(File projectLocation) {
        this.projectLocation = projectLocation;
        updateLibraryNameComboBox();
    }

    public void setServerInstance(String serverInstanceId) {
        Deployment deployment = Deployment.getDefault();
        String name = deployment.getServerDisplayName(deployment.getServerID(serverInstanceId));
        // null can occur only if the server was removed somehow
        name = (name == null) ? "" : PropertyUtils.getUsablePropertyName(name); // NOI18N

        preselectedLibraryName = name;
        libraryNameComboBox.setSelectedItem(preselectedLibraryName);
    }

    public String getSharedLibarariesLocation() {
        return sharableProject.isSelected() ? librariesLocation.getText() : null;
    }

    public String getServerLibraryName() {
        return libraryRadioButton.isSelected() ? (String) libraryNameComboBox.getSelectedItem() : null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        messageLabel = new javax.swing.JLabel();
        serverRadioButton = new javax.swing.JRadioButton();
        libraryRadioButton = new javax.swing.JRadioButton();
        sharableProject = new javax.swing.JCheckBox();
        librariesLabel = new javax.swing.JLabel();
        librariesLocation = new javax.swing.JTextField();
        browseLibraries = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        libraryNameComboBox = new javax.swing.JComboBox();
        libraryNameLabel = new javax.swing.JLabel();

        messageLabel.setText(org.openide.util.NbBundle.getMessage(PanelSharabilityVisual.class, "PanelSharabilityVisual.messageLabel.text")); // NOI18N

        buttonGroup1.add(serverRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(serverRadioButton, org.openide.util.NbBundle.getMessage(PanelSharabilityVisual.class, "PanelSharabilityVisual.serverRadioButton.text")); // NOI18N
        serverRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(libraryRadioButton);
        libraryRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(libraryRadioButton, org.openide.util.NbBundle.getMessage(PanelSharabilityVisual.class, "PanelSharabilityVisual.libraryRadioButton.text")); // NOI18N
        libraryRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                libraryRadioButtonActionPerformed(evt);
            }
        });

        sharableProject.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(sharableProject, org.openide.util.NbBundle.getMessage(PanelSharabilityVisual.class, "PanelSharabilityVisual.sharableProject.text")); // NOI18N
        sharableProject.setMargin(new java.awt.Insets(0, 0, 0, 0));
        sharableProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sharableProjectActionPerformed(evt);
            }
        });

        librariesLabel.setLabelFor(librariesLocation);
        org.openide.awt.Mnemonics.setLocalizedText(librariesLabel, org.openide.util.NbBundle.getMessage(PanelSharabilityVisual.class, "PanelSharabilityVisual.librariesLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseLibraries, org.openide.util.NbBundle.getMessage(PanelSharabilityVisual.class, "PanelSharabilityVisual.browseLibraries.text")); // NOI18N
        browseLibraries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseLibrariesActionPerformed(evt);
            }
        });

        libraryNameComboBox.setEditable(true);

        libraryNameLabel.setLabelFor(libraryNameComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(libraryNameLabel, org.openide.util.NbBundle.getMessage(PanelSharabilityVisual.class, "PanelSharabilityVisual.libraryNameLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(sharableProject)
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(librariesLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(librariesLocation, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(browseLibraries))
            .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(messageLabel)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(serverRadioButton)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(21, 21, 21)
                .add(libraryNameLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(libraryNameComboBox, 0, 254, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(libraryRadioButton)
                .addContainerGap(167, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(sharableProject)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(librariesLabel)
                    .add(browseLibraries)
                    .add(librariesLocation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(messageLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(serverRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(libraryRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(libraryNameLabel)
                    .add(libraryNameComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void libraryRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_libraryRadioButtonActionPerformed
        updateServerShareableFields();
    }//GEN-LAST:event_libraryRadioButtonActionPerformed

    private void serverRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverRadioButtonActionPerformed
        updateServerShareableFields();
    }//GEN-LAST:event_serverRadioButtonActionPerformed

    private void sharableProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sharableProjectActionPerformed
        librariesLocation.setEnabled(sharableProject.isSelected());
        browseLibraries.setEnabled(sharableProject.isSelected());
        if (sharableProject.isSelected()) {
           librariesLocation.setText(currentLibrariesLocation);
        } else {
            librariesLocation.setText("");
        }
        serverRadioButton.setEnabled(sharableProject.isSelected());
        libraryRadioButton.setEnabled(sharableProject.isSelected());
        updateServerShareableFields();
    }//GEN-LAST:event_sharableProjectActionPerformed

    private void browseLibrariesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseLibrariesActionPerformed
        // below folder is used just for relativization:
        File f = FileUtil.normalizeFile(new File(projectLocation.getParent(), "project_folder")); // NOI18N
        String curr = SharableLibrariesUtils.browseForLibraryLocation(librariesLocation.getText().trim(), this, f);
        if (curr != null) {
            currentLibrariesLocation = curr;
            if (sharableProject.isSelected()) {
                librariesLocation.setText(currentLibrariesLocation);
            }
            updateLibraryNameComboBox();
        }
    }//GEN-LAST:event_browseLibrariesActionPerformed

    private void updateServerShareableFields() {
        boolean enabled = libraryRadioButton.isEnabled() && libraryRadioButton.isSelected();
        libraryNameComboBox.setEnabled(enabled);
        libraryNameComboBox.setEditable(enabled);
        if (!libraryRadioButton.isSelected()) {
            libraryNameComboBox.setSelectedItem(""); // NOI18N
        } else {
            libraryNameComboBox.setSelectedItem(preselectedLibraryName);
        }
    }

    private void updateLibraryNameComboBox() {
        DefaultComboBoxModel model = (DefaultComboBoxModel) libraryNameComboBox.getModel();
        model.removeAllElements();

        if (projectLocation == null) {
            return;
        }

        // FIXME how to do this cleanly ?
        File location = FileUtil.normalizeFile(
                PropertyUtils.resolveFile(projectLocation,
                currentLibrariesLocation + File.separator + SharabilityUtility.DEFAULT_LIBRARIES_FILENAME));
        if (location == null || !location.exists()) {
            return;
        }
        for (Library lib : SharabilityUtility.getSharedServerLibraries(location)) {
            model.addElement(lib.getName());
        }
        model.setSelectedItem(preselectedLibraryName);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseLibraries;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel librariesLabel;
    private javax.swing.JTextField librariesLocation;
    private javax.swing.JComboBox libraryNameComboBox;
    private javax.swing.JLabel libraryNameLabel;
    private javax.swing.JRadioButton libraryRadioButton;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JRadioButton serverRadioButton;
    private javax.swing.JCheckBox sharableProject;
    // End of variables declaration//GEN-END:variables

}
