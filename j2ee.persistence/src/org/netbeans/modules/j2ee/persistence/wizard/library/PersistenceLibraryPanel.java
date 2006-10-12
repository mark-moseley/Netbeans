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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.persistence.wizard.library;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.metadata.Utils;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Martin Adamek
 */
public class PersistenceLibraryPanel extends javax.swing.JPanel {
    
    public static final String IS_VALID = "PersistenceLibraryPanel_isValid"; //NOI18N
    
    private LibraryImplementation libImpl;
    private Color nbErrorForeground;
    private Color nbWarningForeground;
    
    public PersistenceLibraryPanel(LibraryImplementation libImpl) {
        initComponents();
        this.libImpl = libImpl;
        J2SEVolumeCustomizer classPathCustomizer = new J2SEVolumeCustomizer(PersistenceLibrarySupport.VOLUME_TYPE_CLASSPATH);
        classPathCustomizer.setObject(libImpl);
        tabbedPane.add(classPathCustomizer);
        J2SEVolumeCustomizer srcCustomizer = new J2SEVolumeCustomizer(PersistenceLibrarySupport.VOLUME_TYPE_SRC);
        srcCustomizer.setObject(libImpl);
        tabbedPane.add(srcCustomizer);
        J2SEVolumeCustomizer javadocCustomizer = new J2SEVolumeCustomizer(PersistenceLibrarySupport.VOLUME_TYPE_JAVADOC);
        javadocCustomizer.setObject(libImpl);
        tabbedPane.add(javadocCustomizer);
        tabbedPane.setMnemonicAt(0, NbBundle.getMessage(PersistenceLibraryPanel.class, "MNE_ClasspathTab").charAt(0)); // NOI18N
        tabbedPane.setMnemonicAt(1, NbBundle.getMessage(PersistenceLibraryPanel.class, "MNE_SourcesTab").charAt(0)); // NOI18N
        tabbedPane.setMnemonicAt(2, NbBundle.getMessage(PersistenceLibraryPanel.class, "MNE_JavadocTab").charAt(0)); // NOI18N
        // set foreground color for error messages
        nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (nbErrorForeground == null) {
            nbErrorForeground = new Color(255, 0, 0); // RGB suggested by jdinga in #65358
        }
        // set foreground color for warning messages
        nbWarningForeground = UIManager.getColor("nb.warningForeground"); //NOI18N
        if (nbWarningForeground == null) {
            nbWarningForeground = new Color(51, 51, 51); // Label.foreground
        }
        // create default name for new library
        LibraryManager lm = LibraryManager.getDefault();
        String libraryName = "PersistenceLibrary";
        int index = 1;
        while (lm.getLibrary(libraryName + index) != null) {
            index++;
        }
        libraryNameTextField.setText(libraryName + index);
        // listen on libray name changes
        libraryNameTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                checkValidity();
            }
            public void insertUpdate(DocumentEvent e) {
                checkValidity();
            }
            public void removeUpdate(DocumentEvent e) {
                checkValidity();
            }
        });
        // listen on library changes (added/removed content)
        libImpl.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                checkValidity();
            }
        });
    }
    
    public void apply() {
        libImpl.setName(libraryNameTextField.getText().trim());
        PersistenceLibrarySupport.getDefault().addLibrary(libImpl);
    }
    
    void checkValidity() {
        String libraryName = libraryNameTextField.getText();
        if (libraryName.trim().equals("")) {
            setErrorMessage(NbBundle.getMessage(PersistenceLibrarySupport.class, "ERR_EmptyName"), false); //NOI18N
            firePropertyChange(IS_VALID, true, false);
        } else if (LibraryManager.getDefault().getLibrary(libraryName) != null) {
            setErrorMessage(NbBundle.getMessage(PersistenceLibrarySupport.class, "ERR_LibraryExists"), false); //NOI18N
            firePropertyChange(IS_VALID, true, false);
        } else if (!containsEntityManager()) {
            setErrorMessage(NbBundle.getMessage(PersistenceLibrarySupport.class, "ERR_NoEntityManager"), false); //NOI18N
            firePropertyChange(IS_VALID, true, false);
        } else if (!containsPersistenceProvider()) {
            setErrorMessage(NbBundle.getMessage(PersistenceLibrarySupport.class, "ERR_NoPersistenceProvider"), false); //NOI18N
            firePropertyChange(IS_VALID, true, false);
        } else {
            setErrorMessage("", true);
            firePropertyChange(IS_VALID, false, true);
        }
    }
    
    private void setErrorMessage(String msg, Boolean canContinue) {
        errorMessage.setForeground(nbErrorForeground);
        if (msg != null && msg.trim().length() > 0 && canContinue != null) {
            if (canContinue.booleanValue()) {
                errorMessage.setIcon(new ImageIcon(Utilities.loadImage("org/openide/resources/warning.gif"))); // NOI18N
                errorMessage.setForeground(nbWarningForeground);
            } else {
                errorMessage.setIcon(new ImageIcon(Utilities.loadImage("org/openide/resources/error.gif"))); // NOI18N
            }
            errorMessage.setToolTipText(msg);
        } else {
            errorMessage.setIcon(null);
            errorMessage.setToolTipText(null);
        }
        
        errorMessage.setText(msg);
    }
    
    private boolean containsEntityManager() {
        return Utils.containsClass(libImpl, "javax.persistence.EntityManager"); //NOI18N
    }
    
    private boolean containsPersistenceProvider() {
        return Utils.containsService(libImpl, "javax.persistence.spi.PersistenceProvider"); //NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        libraryNameTextField = new javax.swing.JTextField();
        tabbedPane = new javax.swing.JTabbedPane();
        errorMessage = new javax.swing.JLabel();

        jLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/library/Bundle").getString("MNE_LibraryName").charAt(0));
        jLabel1.setLabelFor(libraryNameTextField);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(PersistenceLibraryPanel.class, "LBL_LibraryName"));

        libraryNameTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/library/Bundle").getString("LBL_LibraryName"));
        libraryNameTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/library/Bundle").getString("AD_LibraryName"));

        errorMessage.setText(" ");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, tabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(libraryNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, errorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(libraryNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(errorMessage)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel errorMessage;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField libraryNameTextField;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
    
}
