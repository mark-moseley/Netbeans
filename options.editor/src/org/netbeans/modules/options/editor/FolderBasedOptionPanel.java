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
package org.netbeans.modules.options.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.spi.options.OptionsPanelController;

/**
 *
 * @author Dusan Balek
 */
public class FolderBasedOptionPanel extends JPanel implements ActionListener {
    
    private FolderBasedController controller;
    
    /** Creates new form FolderBasedOptionPanel */
    FolderBasedOptionPanel(FolderBasedController controller) {
        this.controller = controller;

        initComponents();
        
        if( "Windows".equals(UIManager.getLookAndFeel().getID()) ) //NOI18N
            setOpaque( false );

        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (String mimeType : controller.getMimeTypes())
            model.addElement(mimeType);
        languageCombo.setModel(model);        
        ListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof String)
                    value = EditorSettings.getDefault().getLanguageName((String)value);
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }            
        };
        languageCombo.setRenderer(renderer);
        languageCombo.addActionListener(this);

        // Pre-select a language
        JTextComponent pane = EditorRegistry.lastFocusedComponent();
        String preSelectMimeType = pane != null ? (String)pane.getDocument().getProperty("mimeType") : ""; // NOI18N
        languageCombo.setSelectedItem(preSelectMimeType);
        if (preSelectMimeType != languageCombo.getSelectedItem() && model.getSize() > 0)
            languageCombo.setSelectedIndex(0);
    }

    void update () {
        JTextComponent pane = EditorRegistry.lastFocusedComponent();
        String preSelectMimeType = pane != null ? (String)pane.getDocument().getProperty("mimeType") : ""; // NOI18N
        languageCombo.setSelectedItem(preSelectMimeType);
        ComboBoxModel model = languageCombo.getModel();
        if (!preSelectMimeType.equals (languageCombo.getSelectedItem()) && model.getSize() > 0)
            languageCombo.setSelectedIndex(0);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        languageLabel = new javax.swing.JLabel();
        languageCombo = new javax.swing.JComboBox();
        optionsPanel = new javax.swing.JPanel();

        languageLabel.setLabelFor(languageCombo);
        org.openide.awt.Mnemonics.setLocalizedText(languageLabel, org.openide.util.NbBundle.getMessage(FolderBasedOptionPanel.class, "LBL_Language")); // NOI18N

        languageCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        optionsPanel.setOpaque(false);
        optionsPanel.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(languageLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(languageCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(211, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, optionsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(languageLabel)
                    .add(languageCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(optionsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox languageCombo;
    private javax.swing.JLabel languageLabel;
    private javax.swing.JPanel optionsPanel;
    // End of variables declaration//GEN-END:variables
 
    // Change in the combos
    public void actionPerformed(ActionEvent e) {
        optionsPanel.setVisible(false);
        optionsPanel.removeAll();
        String mimeType = (String)languageCombo.getSelectedItem();
        OptionsPanelController opc = controller.getController(mimeType);
        JComponent component = opc.getComponent(controller.getLookup());
        optionsPanel.add(component, BorderLayout.CENTER); 
        optionsPanel.setVisible(true);  
    }
}
