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
package org.netbeans.modules.options.indentation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.netbeans.modules.options.editor.spi.PreviewProvider;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Dusan Balek
 */
public final class FormattingPanel extends JPanel implements PropertyChangeListener {
    
    /** Creates new form FormattingPanel */
    public FormattingPanel() {
        initComponents();
        
        if ("Windows".equals(UIManager.getLookAndFeel().getID())) { //NOI18N
            setOpaque(false);
        }

        // Languages combobox renderer
        languageCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof String) {
                    value = ((String)value).length() > 0
                            ? EditorSettings.getDefault().getLanguageName((String)value)
                            : org.openide.util.NbBundle.getMessage(FormattingPanel.class, "LBL_AllLanguages"); //NOI18N                                
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }            
        });
        
        // Category combobox renderer
        categoryCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof PreferencesCustomizer) {
                    value = ((PreferencesCustomizer) value).getDisplayName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }            
        });

    }

    public void setSelector(CustomizerSelector selector) {
        if (this.selector != null) {
            this.selector.removePropertyChangeListener(weakListener);
        }

        this.selector = selector;

        if (this.selector != null) {
            this.weakListener = WeakListeners.propertyChange(this, this.selector);
            this.selector.addPropertyChangeListener(weakListener);
        
            // Languages combobox model
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            ArrayList<String> mimeTypes = new ArrayList<String>();
            mimeTypes.addAll(selector.getMimeTypes());
            Collections.sort(mimeTypes, new LanguagesComparator());

            for (String mimeType : mimeTypes) {
                model.addElement(mimeType);
            }
            languageCombo.setModel(model);

            // Pre-select a language
            JTextComponent pane = EditorRegistry.lastFocusedComponent();
            String preSelectMimeType = pane != null ? (String)pane.getDocument().getProperty("mimeType") : ""; // NOI18N
            languageCombo.setSelectedItem(preSelectMimeType);
            if (preSelectMimeType != languageCombo.getSelectedItem()) {
                languageCombo.setSelectedIndex(0);
            }
        } else {
            languageCombo.setModel(new DefaultComboBoxModel());
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == null || CustomizerSelector.PROP_MIMETYPE.equals(evt.getPropertyName())) {
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            List<? extends PreferencesCustomizer> nue = selector.getCustomizers(selector.getSelectedMimeType());
            for(PreferencesCustomizer c : nue) {
                model.addElement(c);
            }
            categoryCombo.setModel(model);
            categoryCombo.setSelectedIndex(0);
        }

        if (evt.getPropertyName() == null || CustomizerSelector.PROP_CUSTOMIZER.equals(evt.getPropertyName())) {
            // remove the category customizer and its preview
            categoryPanel.setVisible(false);
            categoryPanel.removeAll();
            previewScrollPane.setVisible(false);

            // get the new category customizer
            PreferencesCustomizer c = selector.getSelectedCustomizer();
            if (c != null) {
                // there can be no category selected
                categoryPanel.add(c.getComponent(), BorderLayout.CENTER);
            }
            categoryPanel.setVisible(true);  

            // get the category customizer's preview component
            JComponent previewComponent;
            if (c instanceof PreviewProvider) {
                previewComponent = ((PreviewProvider) c).getPreviewComponent();
                previewComponent.setDoubleBuffered(true);
                if (previewComponent instanceof JTextComponent) {
                    Document doc = ((JTextComponent) previewComponent).getDocument();
                    // This is here solely for the purpose of previewing changes in formatting settings
                    // in Tools-Options. This is NOT, repeat NOT, to be used by anybody else!
                    // The name of this property is also hardcoded in editor.indent/.../CodeStylePreferences.java
                    doc.putProperty("Tools-Options->Editor->Formatting->Preview - Preferences", selector.getCustomizerPreferences(c)); //NOI18N
                }
            } else {
                JLabel noPreviewLabel = new JLabel(NbBundle.getMessage(FormattingPanel.class, "MSG_no_preview_available")); //NOI18N
                noPreviewLabel.setOpaque(true);
                noPreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
                noPreviewLabel.setBorder(new EmptyBorder(new Insets(11, 11, 11, 11)));
                noPreviewLabel.setVisible(true);
                previewComponent = new JPanel(new BorderLayout());
                previewComponent.add(noPreviewLabel, BorderLayout.CENTER);
            }

            // add the preview component to the preview area
            previewScrollPane.setViewportView(previewComponent);
            previewScrollPane.setVisible(true);

            if (c instanceof PreviewProvider) {
                final PreviewProvider pp = (PreviewProvider) c;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        pp.refreshPreview();
                    }
                });
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        optionsPanel = new javax.swing.JPanel();
        languageLabel = new javax.swing.JLabel();
        languageCombo = new javax.swing.JComboBox();
        categoryLabel = new javax.swing.JLabel();
        categoryCombo = new javax.swing.JComboBox();
        categoryPanel = new javax.swing.JPanel();
        previewPanel = new javax.swing.JPanel();
        previewLabel = new javax.swing.JLabel();
        previewScrollPane = new javax.swing.JScrollPane();

        setLayout(new java.awt.GridBagLayout());

        optionsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 8));
        optionsPanel.setOpaque(false);

        languageLabel.setLabelFor(languageCombo);
        org.openide.awt.Mnemonics.setLocalizedText(languageLabel, org.openide.util.NbBundle.getMessage(FormattingPanel.class, "LBL_Language")); // NOI18N

        languageCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        languageCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                languageChanged(evt);
            }
        });

        categoryLabel.setLabelFor(categoryCombo);
        org.openide.awt.Mnemonics.setLocalizedText(categoryLabel, org.openide.util.NbBundle.getMessage(FormattingPanel.class, "LBL_Category")); // NOI18N

        categoryCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        categoryCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                categoryChanged(evt);
            }
        });

        categoryPanel.setOpaque(false);
        categoryPanel.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout optionsPanelLayout = new org.jdesktop.layout.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(optionsPanelLayout.createSequentialGroup()
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, categoryPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, optionsPanelLayout.createSequentialGroup()
                        .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(categoryLabel)
                            .add(languageLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(languageCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(categoryCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        optionsPanelLayout.linkSize(new java.awt.Component[] {categoryCombo, languageCombo}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(optionsPanelLayout.createSequentialGroup()
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(languageLabel)
                    .add(languageCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(categoryLabel)
                    .add(categoryCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(categoryPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        add(optionsPanel, gridBagConstraints);

        previewPanel.setMinimumSize(new java.awt.Dimension(150, 100));
        previewPanel.setOpaque(false);
        previewPanel.setPreferredSize(new java.awt.Dimension(150, 100));
        previewPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(previewLabel, org.openide.util.NbBundle.getMessage(FormattingPanel.class, "LBL_Preview")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        previewPanel.add(previewLabel, gridBagConstraints);

        previewScrollPane.setDoubleBuffered(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        previewPanel.add(previewScrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(previewPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void languageChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_languageChanged
        selector.setSelectedMimeType((String)languageCombo.getSelectedItem());
    }//GEN-LAST:event_languageChanged

    private void categoryChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_categoryChanged
        selector.setSelectedCustomizer(((PreferencesCustomizer)categoryCombo.getSelectedItem()).getId());
    }//GEN-LAST:event_categoryChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox categoryCombo;
    private javax.swing.JLabel categoryLabel;
    private javax.swing.JPanel categoryPanel;
    private javax.swing.JComboBox languageCombo;
    private javax.swing.JLabel languageLabel;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JScrollPane previewScrollPane;
    // End of variables declaration//GEN-END:variables
 
    private CustomizerSelector selector;
    private PropertyChangeListener weakListener;

    private static final class LanguagesComparator implements Comparator<String> {
        public int compare(String mimeType1, String mimeType2) {
            if (mimeType1.length() == 0)
                return mimeType2.length() == 0 ? 0 : -1;
            if (mimeType2.length() == 0)
                return 1;
            
            String langName1 = EditorSettings.getDefault().getLanguageName(mimeType1);
            String langName2 = EditorSettings.getDefault().getLanguageName(mimeType2);
            
            return langName1.compareTo(langName2);
        }
    }
}
