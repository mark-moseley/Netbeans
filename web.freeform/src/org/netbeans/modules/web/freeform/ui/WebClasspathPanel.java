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

package org.netbeans.modules.web.freeform.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.web.freeform.WebProjectGenerator;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Petr Pisl
 */
public class WebClasspathPanel extends javax.swing.JPanel implements HelpCtx.Provider {

    private DefaultListModel listModel;
    /** Original project folder (not nbproject folder) */
    private File projectFolder = null;
    /** Freeform Project base folder */
    private File nbProjectFolder;
    private File lastChosenFile = null;
    private boolean isSeparateClasspath = true;
    private boolean ignoreEvent;
    private static String JAVA_SOURCES_CLASSPATH 
            = org.openide.util.NbBundle.getMessage(WebClasspathPanel.class, "LBL_JAVA_SOURCE_CLASSPATH");
    //private ProjectModel model;
    
    /** Creates new form ClasspathPanel */
    public WebClasspathPanel() {
        this(true);
    }
    
    public WebClasspathPanel(boolean isWizard) {
        initComponents();
        jTextArea1.setBackground(getBackground());
        listModel = new DefaultListModel();
        listModel.add(0,JAVA_SOURCES_CLASSPATH);
        classpath.setModel(listModel);
        if (!isWizard) {
            jTextArea1.setText(org.openide.util.NbBundle.getMessage(WebClasspathPanel.class, "LBL_ClasspathPanel_Explanation"));
        }
        
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx( WebClasspathPanel.class );
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel3 = new javax.swing.JLabel();
        addClasspath = new javax.swing.JButton();
        removeClasspath = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        classpath = new javax.swing.JList();
        jPanel1 = new javax.swing.JPanel();
        moveUp = new javax.swing.JButton();
        moveDown = new javax.swing.JButton();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jTextArea2 = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        jLabel3.setLabelFor(classpath);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(WebClasspathPanel.class, "LBL_ClasspathPanel_jLabel3")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebClasspathPanel.class, "ACSD_ClasspathPanel_jLabel3")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addClasspath, org.openide.util.NbBundle.getMessage(WebClasspathPanel.class, "BTN_ClasspathPanel_addClasspath")); // NOI18N
        addClasspath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addClasspathActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(addClasspath, gridBagConstraints);
        addClasspath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebClasspathPanel.class, "ACSD_ClasspathPanel_addClasspath")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeClasspath, org.openide.util.NbBundle.getMessage(WebClasspathPanel.class, "BTN_ClasspathPanel_removeClasspath")); // NOI18N
        removeClasspath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeClasspathActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(removeClasspath, gridBagConstraints);
        removeClasspath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebClasspathPanel.class, "ACSD_ClasspathPanel_removeClasspath")); // NOI18N

        classpath.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                classpathValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(classpath);
        classpath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebClasspathPanel.class, "ACSD_ClasspathPanel_classpath")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jScrollPane1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jPanel1, gridBagConstraints);

        moveUp.setMnemonic(org.openide.util.NbBundle.getMessage(WebClasspathPanel.class, "LBL_MoveUp_MNE").charAt(0));
        moveUp.setText(org.openide.util.NbBundle.getMessage(WebClasspathPanel.class, "LBL_ClasspathPanel_Move_Up")); // NOI18N
        moveUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(moveUp, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/web/freeform/ui/Bundle"); // NOI18N
        moveUp.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_ClasspathPanel_noveUp")); // NOI18N

        moveDown.setMnemonic(org.openide.util.NbBundle.getMessage(WebClasspathPanel.class, "LBL_MoveDown_MNE").charAt(0));
        moveDown.setText(org.openide.util.NbBundle.getMessage(WebClasspathPanel.class, "LBL_ClasspathPanel_Move_Down")); // NOI18N
        moveDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(moveDown, gridBagConstraints);
        moveDown.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_ClasspathPanel_moveDown")); // NOI18N

        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setText(org.openide.util.NbBundle.getMessage(WebClasspathPanel.class, "MSG_ClasspathPanel_jTextArea")); // NOI18N
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setDisabledTextColor(java.awt.Color.black);
        jTextArea1.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jTextArea1, gridBagConstraints);
        jTextArea1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WebClasspathPanel.class, "ACSN_ClasspathPanel_jTextArea")); // NOI18N
        jTextArea1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WebClasspathPanel.class, "ACSD_ClasspathPanel_jTextArea")); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Label.disabledForeground")));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/freeform/resources/alert_32.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 0);
        jPanel2.add(jLabel5, gridBagConstraints);

        jTextArea2.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jTextArea2.setEditable(false);
        jTextArea2.setLineWrap(true);
        jTextArea2.setText(org.openide.util.NbBundle.getMessage(WebClasspathPanel.class, "Freeform_Warning_Message")); // NOI18N
        jTextArea2.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 4, 4);
        jPanel2.add(jTextArea2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        add(jPanel2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void classpathValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_classpathValueChanged
        updateButtons();
    }//GEN-LAST:event_classpathValueChanged

    private void moveDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownActionPerformed
        int indices[] = classpath.getSelectedIndices();
        if (indices.length == 0 ||
                indices[indices.length - 1] == listModel.getSize() - 1) {
            return;
        }
        for (int i = 0; i < indices.length; i++) {
            int index = indices[i];
            Object o = listModel.remove(index);
            index++;
            listModel.add(index, o);
            indices[i] = index;
        }
        classpath.setSelectedIndices(indices);
        updateButtons();
    }//GEN-LAST:event_moveDownActionPerformed

    private void moveUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpActionPerformed
        int indices[] = classpath.getSelectedIndices();
        if (indices.length == 0 || indices[0] == 0) {
            return;
        }
        for (int i = 0; i < indices.length; i++) {
            int index = indices[i];
            Object o = listModel.remove(index);
            index--;
            listModel.add(index, o);
            indices[i] = index;
        }
        classpath.setSelectedIndices(indices);
        updateButtons();
    }//GEN-LAST:event_moveUpActionPerformed

    void updateButtons() {
        int indices[] = classpath.getSelectedIndices();
        removeClasspath.setEnabled(listModel.getSize() > 0 && indices.length != 0 && indices[0] != 0);
        moveUp.setEnabled(indices.length > 0 && indices[0] > 1);
        moveDown.setEnabled(indices.length > 0 && indices[indices.length - 1] != listModel.getSize() - 1 && indices[0] != 0);
    }
    
    private void removeClasspathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeClasspathActionPerformed
        int entries[] = classpath.getSelectedIndices();
        for (int i = 0; i < entries.length; i++) {
            listModel.remove(entries[i] - i);
        }
        updateButtons();
    }//GEN-LAST:event_removeClasspathActionPerformed

    private void addClasspathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addClasspathActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(true);
        if (lastChosenFile != null) {
            chooser.setSelectedFile(lastChosenFile);
        }
        else {
            if (projectFolder!= null) {
                File files[] = projectFolder.listFiles();
                if (files != null && files.length > 0) {
                    chooser.setSelectedFile(files[0]);
                } else {
                    chooser.setSelectedFile(projectFolder);
                }
            }
        }
        chooser.setDialogTitle(NbBundle.getMessage(WebClasspathPanel.class, "LBL_Browse_Classpath"));
        
        //#77911: prevent adding a non-folder element on the classpath:
        FileFilter fileFilter = new SimpleFileFilter(NbBundle.getMessage(WebClasspathPanel.class, "LBL_ZipJarFolderFilter")); // NOI18N
        chooser.setFileFilter(fileFilter);                                                                 
        chooser.setAcceptAllFileFilterUsed( false );

        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File files[] = chooser.getSelectedFiles();
            for (int i=0; i<files.length; i++) {
                File file = FileUtil.normalizeFile(files[i]);
                
                //Check if the file is acceted by the FileFilter,
                //user may enter the name of non displayed file into JFileChooser
                if (!fileFilter.accept(file)) {
                    continue;
                }

                listModel.addElement(file.getAbsolutePath());
                lastChosenFile = file;
            }
            updateButtons();
        }
    }//GEN-LAST:event_addClasspathActionPerformed
   
    /** Called from WizardDescriptor.Panel and ProjectCustomizer.Panel
     * to set base folder. Panel will use this for default position of JFileChooser.
     * @param baseFolder original project base folder
     * @param nbProjectFolder Freeform Project base folder
     */
    public void setProjectFolders(File baseFolder, File nbProjectFolder) {
        this.projectFolder = baseFolder;
        this.nbProjectFolder = nbProjectFolder;
    }
    
    public String getClasspath(){
        StringBuffer sf = new StringBuffer();
        for (int i = 1; i < listModel.getSize(); i++){
            File f = new File((String)listModel.get(i));
            String path = org.netbeans.modules.ant.freeform.spi.support.Util
                    .relativizeLocation(projectFolder, nbProjectFolder, f);
            sf.append(path);
            if (i < listModel.getSize()-1)
                sf.append(File.pathSeparatorChar);
        }
        return sf.toString();
    }
    
    void setClasspath(String classpath, PropertyEvaluator evaluator){
        if (classpath == null)
            return;
        listModel.clear();
        listModel.addElement(JAVA_SOURCES_CLASSPATH);
        
        String[] cpa = PropertyUtils.tokenizePath(evaluator.evaluate(classpath));
        for (int i=0; i<cpa.length; i++) {
            String path = cpa[i];
            path = PropertyUtils.resolveFile(nbProjectFolder, path).getAbsolutePath();
            if (path != null) {
                listModel.addElement(path);
            }
        }
    }
    
    ActionListener getCustomizerOkListener(final AntProjectHelper projectHelper) {
        return new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                AuxiliaryConfiguration aux = Util.getAuxiliaryConfiguration(projectHelper);
                List<WebProjectGenerator.WebModule> l = WebProjectGenerator.getWebmodules(projectHelper, aux);
                if (l != null){
                    WebProjectGenerator.WebModule wm = (WebProjectGenerator.WebModule)l.get(0);
                    wm.classpath = getClasspath();
                    WebProjectGenerator.putWebModules(projectHelper, aux, l);
                }
                //mkleint: why updating buttons on saving??
                updateButtons();
            }
        };
    }

  
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addClasspath;
    private javax.swing.JList classpath;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JButton moveDown;
    private javax.swing.JButton moveUp;
    private javax.swing.JButton removeClasspath;
    // End of variables declaration//GEN-END:variables

    private static class SimpleFileFilter extends FileFilter {

        private String description;

        public SimpleFileFilter (String description) {
            this.description = description;
        }

        public boolean accept(File f) {
            if (f.isDirectory())
                return true;            
            try {
                return FileUtil.isArchiveFile(f.toURI().toURL());
            } catch (MalformedURLException mue) {
                Exceptions.printStackTrace(mue);
                return false;
            }
        }

        public String getDescription() {
            return this.description;
        }
    }

}
