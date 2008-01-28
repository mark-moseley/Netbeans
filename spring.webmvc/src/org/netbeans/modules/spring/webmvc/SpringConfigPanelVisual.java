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
 *
 * Portions Copyrighted 2008 Craig MacKay.
 */

package org.netbeans.modules.spring.webmvc;

import java.util.regex.Pattern;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * Provides the user interface for configuring a Spring Framework web application
 * Also implements the AtomicAction fired off when the web framework providers
 * extend method is called.
 *
 * @author Craig MacKay
 */
public class SpringConfigPanelVisual extends javax.swing.JPanel {
    
    private final SpringWebModuleExtender extender;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final DocumentListener docListener = new DocumentListener() {

        public void insertUpdate(DocumentEvent e) {
            fireChange();
        }

        public void removeUpdate(DocumentEvent e) {
            fireChange();
        }

        public void changedUpdate(DocumentEvent e) {
            fireChange();
        }
    };
    
    public SpringConfigPanelVisual(SpringWebModuleExtender extender) {
        this.extender = extender;
        initComponents();
        nameText.setText(extender.getDispatcherName());
        nameText.getDocument().addDocumentListener(docListener);
        mappingText.setText(extender.getDispatcherMapping());
        mappingText.getDocument().addDocumentListener(docListener);
        includeJstlCheckBox.setSelected(extender.getIncludeJstl());
        // Only add the listener at the end to make sure no events are
        // fired while initializing the UI.
        changeSupport.addChangeListener(extender);
    }
    
    public String getDispatcherName() {
        return nameText.getText();
    }
    
    public String getDispatcherMapping() {
        return mappingText.getText();
    }
    
    public boolean getIncludeJstl() {
        return includeJstlCheckBox.isSelected();
    }
    
    boolean valid() {        
        ExtenderController controller = extender.getController();
        String namePattern = getDispatcherName();
        String mappingPattern = getDispatcherMapping();
        if (namePattern == null || namePattern.trim().length() == 0){
            controller.setErrorMessage(NbBundle.getMessage(SpringConfigPanelVisual.class, "MSG_NamePatternIsEmpty")); // NOI18N
            return false;
        }
        if (!isNamePatternValid(namePattern)){
            controller.setErrorMessage(NbBundle.getMessage(SpringConfigPanelVisual.class, "MSG_NamePatternIsNotValid")); // NOI18N
            return false;
        }
        if (mappingPattern == null || mappingPattern.trim().length() == 0) {
            controller.setErrorMessage(NbBundle.getMessage(SpringConfigPanelVisual.class, "MSG_MappingPatternIsEmpty")); // NOI18N
            return false;
        }
        if (!isMappingPatternValid(mappingPattern)){
            controller.setErrorMessage(NbBundle.getMessage(SpringConfigPanelVisual.class, "MSG_MappingPatternIsNotValid")); // NOI18N
            return false;
        }
        controller.setErrorMessage(null);
        return true;
    }
    
    private boolean isNamePatternValid(String pattern) {        
        return Pattern.matches("\\w+", pattern);
    }
    
    private boolean isMappingPatternValid(String pattern){
        // mapping validation based on the Servlet 2.4 specification,section SRV.11.2
        if (pattern.startsWith("*.")){ // NOI18N
            String p = pattern.substring(2);
            if (p.indexOf('.') == -1 && p.indexOf('*') == -1  
                    && p.indexOf('/') == -1 && !p.trim().equals("")) { // NOI18N
                return true;
            }
        }
        
        if ((pattern.length() > 3) && pattern.endsWith("/*") && pattern.startsWith("/")) // NOI18N
            return true;
        
        if (pattern.matches("/")){ // NOI18N
            return true;
        }
               
        return false;
    }
    
    private void fireChange() {
        changeSupport.fireChange();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPanel = new javax.swing.JTabbedPane();
        standardPanel = new javax.swing.JPanel();
        nameText = new javax.swing.JTextField();
        nameLabel = new javax.swing.JLabel();
        mappingLabel = new javax.swing.JLabel();
        mappingText = new javax.swing.JTextField();
        libPanel = new javax.swing.JPanel();
        includeJstlCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(SpringConfigPanelVisual.class, "LBL_DispatcherName")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(mappingLabel, org.openide.util.NbBundle.getMessage(SpringConfigPanelVisual.class, "LBL_DispatcherMapping")); // NOI18N

        org.jdesktop.layout.GroupLayout standardPanelLayout = new org.jdesktop.layout.GroupLayout(standardPanel);
        standardPanel.setLayout(standardPanelLayout);
        standardPanelLayout.setHorizontalGroup(
            standardPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(standardPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(standardPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(nameLabel)
                    .add(mappingLabel))
                .add(8, 8, 8)
                .add(standardPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(nameText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE)
                    .add(mappingText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE))
                .addContainerGap())
        );
        standardPanelLayout.setVerticalGroup(
            standardPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(standardPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(standardPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameLabel)
                    .add(nameText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(standardPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(mappingLabel)
                    .add(mappingText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(337, Short.MAX_VALUE))
        );

        tabbedPanel.addTab(org.openide.util.NbBundle.getMessage(SpringConfigPanelVisual.class, "LBL_Configuration"), standardPanel); // NOI18N

        libPanel.setAlignmentX(0.2F);
        libPanel.setAlignmentY(0.2F);

        org.openide.awt.Mnemonics.setLocalizedText(includeJstlCheckBox, org.openide.util.NbBundle.getMessage(SpringConfigPanelVisual.class, "LBL_IncludeJstl")); // NOI18N
        includeJstlCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                includeJstlCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout libPanelLayout = new org.jdesktop.layout.GroupLayout(libPanel);
        libPanel.setLayout(libPanelLayout);
        libPanelLayout.setHorizontalGroup(
            libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(libPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(includeJstlCheckBox)
                .addContainerGap(500, Short.MAX_VALUE))
        );
        libPanelLayout.setVerticalGroup(
            libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(libPanelLayout.createSequentialGroup()
                .add(15, 15, 15)
                .add(includeJstlCheckBox)
                .addContainerGap(354, Short.MAX_VALUE))
        );

        tabbedPanel.addTab(org.openide.util.NbBundle.getMessage(SpringConfigPanelVisual.class, "LBL_Libraries"), libPanel); // NOI18N

        add(tabbedPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void includeJstlCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_includeJstlCheckBoxActionPerformed
        fireChange();
    }//GEN-LAST:event_includeJstlCheckBoxActionPerformed

    public void enableComponents(boolean enabled) {
        standardPanel.setEnabled(enabled);
        mappingLabel.setEnabled(enabled);
        mappingText.setEnabled(enabled);
        nameLabel.setEnabled(enabled);
        nameText.setEnabled(enabled);
        tabbedPanel.setEnabled(enabled);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox includeJstlCheckBox;
    private javax.swing.JPanel libPanel;
    private javax.swing.JLabel mappingLabel;
    private javax.swing.JTextField mappingText;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameText;
    private javax.swing.JPanel standardPanel;
    private javax.swing.JTabbedPane tabbedPanel;
    // End of variables declaration//GEN-END:variables
    
}
