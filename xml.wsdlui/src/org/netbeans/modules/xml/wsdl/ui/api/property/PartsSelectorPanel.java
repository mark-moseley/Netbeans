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

package org.netbeans.modules.xml.wsdl.ui.api.property;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;

import org.openide.explorer.propertysheet.PropertyEnv;

/**
 *
 * @author  skini
 */
public class PartsSelectorPanel extends javax.swing.JPanel {
    
    /**
     * 
     */
    private static final long serialVersionUID = 8696139559577226547L;
    private int[] selectedIndices;
    
    /** Creates new form PartsSelectorPanel */
    public PartsSelectorPanel(String[] parts, String[] selectedPartNames, PropertyEnv env) {
        mEnv = env;
        mEnv.setState(PropertyEnv.STATE_INVALID);
        mParts = parts;
        if (selectedPartNames != null) {
            if (selectedPartNames.length == 1 && selectedPartNames[0].trim().length() == 0) {
                selectedIndices = null;
            } else {
                selectedIndices = new int[selectedPartNames.length];
                Map<String, Integer> map = new HashMap<String, Integer>();
                int j = 0;
                for (String part : parts) {
                    map.put(part, new Integer(j++));
                }

                for (int i = 0; i < selectedPartNames.length; i++) {
                    String partName = selectedPartNames[i];
                    if (map.containsKey(partName)) {
                        selectedIndices[i] = map.get(partName).intValue();
                    } else {
                        selectedIndices[i] = -1;
                    }
                }
            }
        }
        initComponents();
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();

        ComboBoxModel model = new DefaultComboBoxModel(mParts);
        jList1.setModel(model);
        if (selectedIndices != null)
        jList1.setSelectedIndices(selectedIndices);
        jList1.setToolTipText(org.openide.util.NbBundle.getMessage(PartsSelectorPanel.class, "PartsSelectorPanel.jList1.toolTipText")); // NOI18N
        jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList1ValueChanged(evt);
            }
        });

        jScrollPane1.setViewportView(jList1);
        jList1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PartsSelectorPanel.class, "PartsSelectorPanel.jList1.AccessibleContext.accessibleName")); // NOI18N
        jList1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PartsSelectorPanel.class, "PartsSelectorPanel.jList1.AccessibleContext.accessibleDescription")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 220, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList1ValueChanged
        enableOK();
        JList list = (JList) evt.getSource();
        if (!list.getValueIsAdjusting()) {
            Object[] sv = list.getSelectedValues();
            if (sv != null && sv.length > 0) {
                
            }
        }
    }//GEN-LAST:event_jList1ValueChanged
    
    private void enableOK() {
        if (!okEnabled) {
            mEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            okEnabled = true;
        }
        
    }
    
    public String getParts() {
        Object[] selectedValues  = jList1.getSelectedValues();
        StringBuffer strBuf = new StringBuffer();
        for (Object selectedValue : selectedValues) {
            strBuf.append((String) selectedValue).append(" ");
        }
        return strBuf.toString().trim();
    }
    
    private boolean okEnabled = false;
    private PropertyEnv mEnv;
    private String[] mParts;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    
}
