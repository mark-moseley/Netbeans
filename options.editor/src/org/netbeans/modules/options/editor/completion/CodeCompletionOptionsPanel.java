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

package org.netbeans.modules.options.editor.completion;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.openide.util.WeakListeners;

/**
 * @author Dusan Balek
 */
public class CodeCompletionOptionsPanel extends JPanel implements PropertyChangeListener {
    
    private CodeCompletionOptionsSelector selector;
    private PropertyChangeListener weakListener;
    
    /** 
     * Creates new form CodeCompletionOptionsPanel.
     */
    public CodeCompletionOptionsPanel () {
        initComponents ();

        // Languages combobox renderer
        cbLanguage.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof String) {
                    value = ((String)value).length() > 0
                            ? EditorSettings.getDefault().getLanguageName((String)value)
                            : org.openide.util.NbBundle.getMessage(CodeCompletionOptionsPanel.class, "LBL_AllLanguages"); //NOI18N
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

    }
    
    public void setSelector(CodeCompletionOptionsSelector selector) {
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
            cbLanguage.setModel(model);
            cbLanguage.setSelectedIndex(0);
        } else {
            cbLanguage.setModel(new DefaultComboBoxModel());
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        panel.setVisible(false);
        panel.removeAll();
        PreferencesCustomizer c = selector.getSelectedCustomizer();
        if (c != null)
            panel.add(c.getComponent(), BorderLayout.CENTER);
        panel.setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lLanguage = new javax.swing.JLabel();
        cbLanguage = new javax.swing.JComboBox();
        panel = new javax.swing.JPanel();

        lLanguage.setLabelFor(cbLanguage);
        lLanguage.setText("Language:");

        cbLanguage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                languageChanged(evt);
            }
        });

        panel.setOpaque(false);
        panel.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(lLanguage)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbLanguage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .add(panel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lLanguage)
                    .add(cbLanguage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void languageChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_languageChanged
        selector.setSelectedMimeType((String)cbLanguage.getSelectedItem());
    }//GEN-LAST:event_languageChanged
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbLanguage;
    private javax.swing.JLabel lLanguage;
    private javax.swing.JPanel panel;
    // End of variables declaration//GEN-END:variables

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
