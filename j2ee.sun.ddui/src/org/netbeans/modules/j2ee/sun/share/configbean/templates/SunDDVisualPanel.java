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
package org.netbeans.modules.j2ee.sun.share.configbean.templates;

import java.io.File;
import javax.swing.JPanel;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;


/*
 *
 * @author Peter Williams
 */
public final class SunDDVisualPanel extends JPanel {
    
    private Project project;
    private String sunDDFileName;
    private File sunDDFile;
    private File sunDDLocation;

    public SunDDVisualPanel() {
        initComponents();
    }

    void setProject(final Project project) {
        this.project = project;

        // get j2ee module provider
        // get list of config files for this module type
        // figure out which ones exist already
        // 
        Lookup lookup = project.getLookup();
        J2eeModuleProvider provider = (J2eeModuleProvider) lookup.lookup(J2eeModuleProvider.class);
        J2eeModule j2eeModule = provider.getJ2eeModule();
        sunDDFileName = getConfigFileName(j2eeModule);

        // Calculate location:
        sunDDFile = (sunDDFileName != null) ? j2eeModule.getDeploymentConfigurationFile(sunDDFileName) : null;
        sunDDLocation = (sunDDFile != null) ? sunDDFile.getParentFile() : null;
        
        // initialize visual components
        textFileName.setText(sunDDFileName); // NOI18N
        textProjectName.setText(ProjectUtils.getInformation(project).getDisplayName());

        File projectFolder = FileUtil.toFile(project.getProjectDirectory());
        textLocation.setText((sunDDLocation != null) ? getRelativePath(sunDDLocation, projectFolder) : null);
        // only fill 'created file' in if location is valid.
        textCreatedFile.setText((sunDDLocation != null) ? getRelativePath(sunDDFile, projectFolder) : null);
    }
    
    String getFileName() {
        return sunDDFileName;
    }
    
    File getFile() {
        return sunDDFile;
    }
    
    File getSelectedLocation() {
        return sunDDLocation;
    }
    
    public String getName() {
        return NbBundle.getMessage(SunDDVisualPanel.class, "LBL_CreateSunDeploymentDescriptor"); // NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        labelFileName = new javax.swing.JLabel();
        textFileName = new javax.swing.JTextField();
        labelProjectName = new javax.swing.JLabel();
        textProjectName = new javax.swing.JTextField();
        labelLocation = new javax.swing.JLabel();
        textLocation = new javax.swing.JTextField();
        labelCreatedFile = new javax.swing.JLabel();
        textCreatedFile = new javax.swing.JTextField();
        filler1 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        labelFileName.setLabelFor(textFileName);
        org.openide.awt.Mnemonics.setLocalizedText(labelFileName, org.openide.util.NbBundle.getMessage(SunDDVisualPanel.class, "LBL_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(labelFileName, gridBagConstraints);

        textFileName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(textFileName, gridBagConstraints);
        textFileName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SunDDVisualPanel.class, "ASCN_Name")); // NOI18N
        textFileName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SunDDVisualPanel.class, "ASCD_Name")); // NOI18N

        labelProjectName.setLabelFor(textProjectName);
        org.openide.awt.Mnemonics.setLocalizedText(labelProjectName, org.openide.util.NbBundle.getMessage(SunDDVisualPanel.class, "LBL_Project")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(labelProjectName, gridBagConstraints);

        textProjectName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(textProjectName, gridBagConstraints);
        textProjectName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SunDDVisualPanel.class, "ASCN_Project")); // NOI18N
        textProjectName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SunDDVisualPanel.class, "ASCD_Project")); // NOI18N

        labelLocation.setLabelFor(textLocation);
        org.openide.awt.Mnemonics.setLocalizedText(labelLocation, org.openide.util.NbBundle.getMessage(SunDDVisualPanel.class, "LBL_Location")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(labelLocation, gridBagConstraints);

        textLocation.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(textLocation, gridBagConstraints);
        textLocation.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SunDDVisualPanel.class, "ASCN_Location")); // NOI18N
        textLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SunDDVisualPanel.class, "ASCD_Location")); // NOI18N

        labelCreatedFile.setLabelFor(textCreatedFile);
        org.openide.awt.Mnemonics.setLocalizedText(labelCreatedFile, org.openide.util.NbBundle.getMessage(SunDDVisualPanel.class, "LBL_CreatedFile")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 11, 0);
        add(labelCreatedFile, gridBagConstraints);

        textCreatedFile.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 11, 0);
        add(textCreatedFile, gridBagConstraints);
        textCreatedFile.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SunDDVisualPanel.class, "ASCN_CreatedFile")); // NOI18N
        textCreatedFile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SunDDVisualPanel.class, "ASCD_CreatedFile")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(filler1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel filler1;
    private javax.swing.JLabel labelCreatedFile;
    private javax.swing.JLabel labelFileName;
    private javax.swing.JLabel labelLocation;
    private javax.swing.JLabel labelProjectName;
    private javax.swing.JTextField textCreatedFile;
    private javax.swing.JTextField textFileName;
    private javax.swing.JTextField textLocation;
    private javax.swing.JTextField textProjectName;
    // End of variables declaration//GEN-END:variables
    

    private String getConfigFileName(J2eeModule j2eeModule) {
        String result = null;
        Object moduleType = j2eeModule.getModuleType();
        if(J2eeModule.WAR.equals(moduleType)) {
            result = "sun-web.xml"; // NOI18N
        } else if(J2eeModule.EJB.equals(moduleType)) {
            result = "sun-ejb-jar.xml"; // NOI18N
        } else if(J2eeModule.EAR.equals(moduleType)) {
            result = "sun-application.xml"; // NOI18N
        } else if(J2eeModule.CLIENT.equals(moduleType)) {
            result = "sun-application-client.xml"; // NOI18N
        }
        return result;
    }
    
    private static String getRelativePath(File file, File base) {
        String basePath = base.getAbsolutePath();
        String filePath = file.getAbsolutePath();
        
        if(filePath.startsWith(basePath)) {
            String prefix = "";
            int baseIndex = basePath.lastIndexOf(File.separatorChar);
            if(baseIndex < 0 || baseIndex >= filePath.length()) {
                baseIndex = 0;
            } else {
                prefix = "..."; // only apply prefix if we're calculating a true substring.
            }
            return prefix + filePath.substring(baseIndex);
        }
        return filePath;
    }
    
}
