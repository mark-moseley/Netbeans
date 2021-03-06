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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.uml.project.ui.wizards;

import java.io.File;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.NbBundle;

/** First panel in the NewProject wizard. Used for filling in
 * name, and directory of the project.
 *
 * @author Mike Frisino
 */
public class PanelConfigureProjectVisual extends JPanel
{
    private PanelConfigureProject panel;
    private boolean ignoreProjectDirChanges;
    private boolean ignoreAntProjectNameChanges;
    private boolean noDir = true;
    private SettingsPanel projectLocationPanel;
    private PanelOptionsVisual optionsPanel;
    private int type;
    
    
    public PanelConfigureProjectVisual(PanelConfigureProject panel, int type)
    {
        this.panel = panel;
        initComponents();
        this.type = type;
        
        setName(NbBundle.getMessage(
            PanelConfigureProjectVisual.class, "TXT_NameAndLoc")); // NOI18N
        
        // Platform-Independent Model
        if (type == NewUMLProjectWizardIterator.TYPE_UML)
        {
            projectLocationPanel = new ProjectLocationVisualPanel(panel, type);

            putClientProperty("NewProjectWizard_Title",// NOI18N
                NbBundle.getMessage(PanelConfigureProjectVisual.class,
                    "TXT_NewUMLProject")); // NOI18N
            
            jSeparator1.setVisible(true);
            
            getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(PanelConfigureProjectVisual.class,
                    "TXT_NewUMLProject")); // NOI18N
            getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(PanelConfigureProjectVisual.class,
                    "ACSD_NewUMLProject")); // NOI18N
        }
        
        // Java-Platform Model
        else if (type == NewUMLProjectWizardIterator.TYPE_UML_JAVA)
        {
            projectLocationPanel = new ProjectLocationVisualPanel(panel, type);

            putClientProperty("NewProjectWizard_Title", // NOI18N
                NbBundle.getMessage(PanelConfigureProjectVisual.class,
                    "TXT_NewUMLJavaProject")); // NOI18N
            
            jSeparator1.setVisible(true);
            
            getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(PanelConfigureProjectVisual.class,
                    "TXT_NewUMLJavaProject")); // NOI18N
            getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(PanelConfigureProjectVisual.class,
                    "ACSD_NewUMLJavaProject")); // NOI18N
        }
        
        // Reverse Engineere Java Project Model
        else if (type == NewUMLProjectWizardIterator.TYPE_REVERSE_ENGINEER)
        {
            projectLocationPanel = new ProjectLocationVisualPanel(panel, type);
            
            putClientProperty("NewProjectWizard_Title", // NOI18N
                NbBundle.getMessage(PanelConfigureProjectVisual.class,
                    "TXT_NewReverseEngineering")); // NOI18N

            jSeparator1.setVisible(true);
            
            getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(PanelConfigureProjectVisual.class,
                    "TXT_NewReverseEngineering")); // NOI18N
            getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(PanelConfigureProjectVisual.class,
                    "ACSD_NewReverseEngineering")); // NOI18N
        }
        
//        else if (type == NewUMLProjectWizardIterator.TYPE_ROSE_IMPORT)
//        {
//            projectLocationPanel = new ProjectLocationVisualPanel(panel, type);
//
//            putClientProperty("NewProjectWizard_Title", // NOI18N
//                NbBundle.getMessage(PanelConfigureProjectVisual.class,
//                    "TXT_NewRoseImport")); // NOI18N
//            
//            jSeparator1.setVisible(true);
//            
//            getAccessibleContext().setAccessibleName(
//                NbBundle.getMessage(PanelConfigureProjectVisual.class,
//                    "TXT_NewRoseImport")); // NOI18N
//            getAccessibleContext().setAccessibleDescription(
//                NbBundle.getMessage(PanelConfigureProjectVisual.class,
//                    "ACSD_NewRoseImport")); // NOI18N
//        }
        
        locationContainer.add(
            projectLocationPanel, java.awt.BorderLayout.CENTER);
        
        optionsPanel = new PanelOptionsVisual(panel, type);
        optionsContainer.add(optionsPanel, java.awt.BorderLayout.CENTER );
    }
    
    boolean valid( WizardDescriptor wizardDescriptor )
    {
        wizardDescriptor.putProperty(
            NewUMLProjectWizardIterator.PROP_WIZARD_ERROR_MESSAGE, "" ); //NOI18N
        
        return projectLocationPanel.valid(wizardDescriptor) && 
                optionsPanel.valid(wizardDescriptor);
    }
    
    void read(WizardDescriptor wizDesc)
    {
        Integer lastType = (Integer)wizDesc.getProperty(
            NewUMLProjectWizardIterator.PROP_WIZARD_TYPE);
        
        if (lastType == null || lastType.intValue() != this.type)
        {
            //bugfix #46387 The type of project changed, reset values to defaults
            wizDesc.putProperty(
                NewUMLProjectWizardIterator.PROP_PROJECT_NAME, null);
            
            wizDesc.putProperty(
                NewUMLProjectWizardIterator.PROP_PROJECT_DIR, null);
        }
        
        projectLocationPanel.read(wizDesc);
        optionsPanel.read(wizDesc);
    }
    
    void store(WizardDescriptor wizDesc)
    {
        wizDesc.putProperty(NewUMLProjectWizardIterator.PROP_WIZARD_TYPE,
            new Integer(this.type));

        projectLocationPanel.store(wizDesc);
        optionsPanel.store(wizDesc);
    }
    
    void validate(WizardDescriptor wizDesc) 
        throws WizardValidationException
    {
        projectLocationPanel.validate(wizDesc);
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        locationContainer = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        optionsContainer = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleName("");
        getAccessibleContext().setAccessibleDescription("");
        locationContainer.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(locationContainer, gridBagConstraints);
        locationContainer.getAccessibleContext().setAccessibleName(null);
        locationContainer.getAccessibleContext().setAccessibleDescription(null);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jSeparator1, gridBagConstraints);
        jSeparator1.getAccessibleContext().setAccessibleName("");
        jSeparator1.getAccessibleContext().setAccessibleDescription("");

        optionsContainer.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(optionsContainer, gridBagConstraints);
        optionsContainer.getAccessibleContext().setAccessibleName("");
        optionsContainer.getAccessibleContext().setAccessibleDescription("");

    }// </editor-fold>//GEN-END:initComponents
    
    /** Currently only handles the "Browse..." button
     */
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel locationContainer;
    private javax.swing.JPanel optionsContainer;
    // End of variables declaration//GEN-END:variables
    
    
}
