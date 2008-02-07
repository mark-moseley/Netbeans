/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.netbeans.modules.java.j2seproject.ui.wizards;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.StringTokenizer;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import org.netbeans.spi.java.project.support.ui.SharableLibrariesUtils;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author  phrebejk
 */
public class PanelOptionsVisual extends SettingsPanel implements ActionListener, PropertyChangeListener {
    
    private static boolean lastMainClassCheck = true; // XXX Store somewhere
    
    public static final String SHARED_LIBRARIES = "sharedLibraries";
    
    private PanelConfigureProject panel;
    private boolean valid;
    private String currentLibrariesLocation;
    private String projectLocation;
    
    public PanelOptionsVisual(PanelConfigureProject panel, NewJ2SEProjectWizardIterator.WizardType type) {
        initComponents();
        this.panel = panel;
        currentLibrariesLocation = "libraries";   //#126366 ".."+File.separatorChar+"libraries"; // NOI18N
        librariesLocation.setText(currentLibrariesLocation);

        switch (type) {
            case LIB:
                setAsMainCheckBox.setVisible( false );
                createMainCheckBox.setVisible( false );
                mainClassTextField.setVisible( false );
                break;
            case APP:
                createMainCheckBox.addActionListener( this );
                createMainCheckBox.setSelected( lastMainClassCheck );
                mainClassTextField.setEnabled( lastMainClassCheck );
                break;
            case EXT:
                setAsMainCheckBox.setVisible( true );
                createMainCheckBox.setVisible( false );
                mainClassTextField.setVisible( false );
                break;
        }
        
        this.mainClassTextField.getDocument().addDocumentListener( new DocumentListener () {
            
            public void insertUpdate(DocumentEvent e) {
                mainClassChanged ();
            }
            
            public void removeUpdate(DocumentEvent e) {
                mainClassChanged ();
            }
            
            public void changedUpdate(DocumentEvent e) {
                mainClassChanged ();
            }
            
        });
    }

    public void actionPerformed( ActionEvent e ) {        
        if ( e.getSource() == createMainCheckBox ) {
            lastMainClassCheck = createMainCheckBox.isSelected();
            mainClassTextField.setEnabled( lastMainClassCheck );        
            this.panel.fireChangeEvent();
        }                
    }
    
    public void propertyChange (PropertyChangeEvent event) {
        if (PanelProjectLocationVisual.PROP_PROJECT_NAME.equals(event.getPropertyName())) {
            String newProjectName = NewJ2SEProjectWizardIterator.getPackageName((String) event.getNewValue());
            if (!Utilities.isJavaIdentifier(newProjectName)) {
                newProjectName = NbBundle.getMessage (PanelOptionsVisual.class, "TXT_PackageNameSuffix", newProjectName); 
            }
            this.mainClassTextField.setText (MessageFormat.format(
                NbBundle.getMessage (PanelOptionsVisual.class,"TXT_ClassName"), new Object[] {newProjectName}
            ));
        }
        if (PanelProjectLocationVisual.PROP_PROJECT_LOCATION.equals(event.getPropertyName())) {
            projectLocation = (String)event.getNewValue();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setAsMainCheckBox = new javax.swing.JCheckBox();
        createMainCheckBox = new javax.swing.JCheckBox();
        mainClassTextField = new javax.swing.JTextField();
        sharableProject = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        librariesLocation = new javax.swing.JTextField();
        browseLibraries = new javax.swing.JButton();

        setAsMainCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(setAsMainCheckBox, org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("LBL_setAsMainCheckBox")); // NOI18N
        setAsMainCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        createMainCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(createMainCheckBox, org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("LBL_createMainCheckBox")); // NOI18N
        createMainCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        mainClassTextField.setText("com.myapp.Main");

        sharableProject.setMnemonic('P');
        sharableProject.setSelected(true);
        sharableProject.setText(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_SharableProject_Checkbox")); // NOI18N
        sharableProject.setMargin(new java.awt.Insets(0, 0, 0, 0));
        sharableProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sharableProjectActionPerformed(evt);
            }
        });

        jLabel1.setText(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_Location_Label")); // NOI18N

        librariesLocation.setEditable(false);

        browseLibraries.setMnemonic('B');
        browseLibraries.setText(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "LBL_PanelOptions_Browse_Button")); // NOI18N
        browseLibraries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseLibrariesActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(setAsMainCheckBox)
            .add(layout.createSequentialGroup()
                .add(createMainCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainClassTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(sharableProject)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(librariesLocation, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(browseLibraries))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(setAsMainCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(createMainCheckBox)
                    .add(mainClassTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sharableProject)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(browseLibraries)
                    .add(librariesLocation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        setAsMainCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSN_setAsMainCheckBox")); // NOI18N
        setAsMainCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSD_setAsMainCheckBox")); // NOI18N
        createMainCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSN_createMainCheckBox")); // NOI18N
        createMainCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSD_createMainCheckBox")); // NOI18N
        mainClassTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ASCN_mainClassTextFiled")); // NOI18N
        mainClassTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ASCD_mainClassTextFiled")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSN_PanelOptionsVisual")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_PanelOptionsVisual")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void sharableProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sharableProjectActionPerformed
        librariesLocation.setEnabled(sharableProject.isSelected());
        browseLibraries.setEnabled(sharableProject.isSelected());
        if (sharableProject.isSelected()) {
            librariesLocation.setText(currentLibrariesLocation);
        } else {
            librariesLocation.setText("");
        }
    }//GEN-LAST:event_sharableProjectActionPerformed

    private void browseLibrariesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseLibrariesActionPerformed
        // below folder is used just for relativization:
        File f = FileUtil.normalizeFile(new File(projectLocation + 
                File.separatorChar + "project_folder")); // NOI18N
        String curr = SharableLibrariesUtils.browseForLibraryLocation(librariesLocation.getText().trim(), this, f);
        if (curr != null) {
            currentLibrariesLocation = curr;
            if (sharableProject.isSelected()) {
                librariesLocation.setText(currentLibrariesLocation);
            }
        }
    }//GEN-LAST:event_browseLibrariesActionPerformed
    

    
    boolean valid(WizardDescriptor settings) {
        
        // TODO: check whether libraries file is property file and is collocated
        
        if (mainClassTextField.isVisible () && mainClassTextField.isEnabled ()) {
            if (!valid) {
                settings.putProperty( "WizardPanel_errorMessage", // NOI18N
                    NbBundle.getMessage(PanelOptionsVisual.class,"ERROR_IllegalMainClassName")); //NOI18N
            }
            return this.valid;
        }
        else {
            return true;
        }
    }
    
    void read (WizardDescriptor d) {
        //TODO:
    }
    
    void validate (WizardDescriptor d) throws WizardValidationException {
        // nothing to validate
    }

    void store( WizardDescriptor d ) {
        d.putProperty( /*XXX Define somewhere */ "setAsMain", setAsMainCheckBox.isSelected() && setAsMainCheckBox.isVisible() ? Boolean.TRUE : Boolean.FALSE ); // NOI18N
        d.putProperty( /*XXX Define somewhere */ "mainClass", createMainCheckBox.isSelected() && createMainCheckBox.isVisible() ? mainClassTextField.getText() : null ); // NOI18N
        d.putProperty( SHARED_LIBRARIES, sharableProject.isSelected() ? librariesLocation.getText() : null ); // NOI18N
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseLibraries;
    private javax.swing.JCheckBox createMainCheckBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField librariesLocation;
    private javax.swing.JTextField mainClassTextField;
    private javax.swing.JCheckBox setAsMainCheckBox;
    private javax.swing.JCheckBox sharableProject;
    // End of variables declaration//GEN-END:variables
    
    private void mainClassChanged () {
        String mainClassName = this.mainClassTextField.getText ();
        StringTokenizer tk = new StringTokenizer (mainClassName, "."); //NOI18N
        boolean valid = true;
        while (tk.hasMoreTokens()) {
            String token = tk.nextToken();
            if (token.length() == 0 || !Utilities.isJavaIdentifier(token)) {
                valid = false;
                break;
            }            
        }
        this.valid = valid;
        this.panel.fireChangeEvent();
    }
}

