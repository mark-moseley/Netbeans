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

package org.netbeans.modules.websvc.rest.projects;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author  nam
 */
public class RestWebCustomizerPanel extends javax.swing.JPanel implements ActionListener {

    private static final String RESTAPT_PREFIX = "rest.apt.";
    private static final int COLUMN_KEY = 0;
    private static final int COLUMN_VALUE = 1;
    public static final String[] COLUMN_NAMES = {NbBundle.getMessage(RestWebCustomizerPanel.class, "LBL_Key"),
            NbBundle.getMessage(RestWebCustomizerPanel.class, "LBL_Value")};
    private static TreeMap<String, Object> defaults = new TreeMap<String, Object>();
    static {
        defaults.put("redirect", Boolean.TRUE);
        defaults.put("normalizeURI", Boolean.TRUE);
        defaults.put("canonicalizeURIPath", Boolean.TRUE);
        defaults.put("ignoreMatrixParams", Boolean.TRUE);
    }

    private TreeMap<String, Object> options = new TreeMap<String, Object>();
    private RestSupport support;

    /** Creates new form RestWebCustomizerPanel */
    public RestWebCustomizerPanel(RestSupport support) {
        initComponents();
        this.support = support;
        initOptions();
        optionsTable.setModel(new OptionsModel());
    }

    private String getKey(int row) {
        return (String) defaults.keySet().toArray()[row];
    }

    private class OptionsModel extends DefaultTableModel {

        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        @Override
        public int getRowCount() {
            return defaults.size();
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == 1 ? Boolean.class : super.getColumnClass(column);
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (column == COLUMN_KEY) {
                return getKey(row);
            } else if (column == COLUMN_VALUE) {
                String key = getKey(row);
                Object value = options.get(key);
                if (value == null) {
                    return defaults.get(key);
                }
                return value;
            }
            return super.getValueAt(row, column);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 1;
        }

        @Override
        public void setValueAt(Object v, int row, int column) {
            if (column == 1) {
                String key = getKey(row);
                if (defaults.get(key).getClass().isAssignableFrom(v.getClass())) {
                    options.put(key, v);
                }
            }
            super.setValueAt(v, row, column);
        }
    }

    private void initOptions() {
        for (String key : defaults.keySet()) {
            String name = RESTAPT_PREFIX + key;
            String v = support.getProjectProperty(name);
            if (v != null) {
                if (defaults.get(key) instanceof Boolean) {
                    options.put(key,Boolean.valueOf(v));
                } else if (defaults.get(key) instanceof String) {
                    options.put(key,v);
                }
            }
        }
        
    }
    
    public void actionPerformed(ActionEvent e) {
        for (Map.Entry<String, Object> entry : options.entrySet()) {
            String name = RESTAPT_PREFIX + entry.getKey();
            support.setProjectProperty(name, entry.getValue().toString());
        }
        try {
            new AntFilesHelper(support).refreshRestBuildXml();
            ProjectManager.getDefault().saveProject(support.getProject());

        } catch(IOException ex) {
            Logger.getLogger(getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        optionsTable = new javax.swing.JTable();
        aptOptionsLabel = new javax.swing.JLabel();

        optionsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Key", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        optionsTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(optionsTable);

        aptOptionsLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/rest/wizard/Bundle").getString("MNE_RestAptOptions").charAt(0));
        aptOptionsLabel.setLabelFor(optionsTable);
        aptOptionsLabel.setText(org.openide.util.NbBundle.getMessage(RestWebCustomizerPanel.class, "RestWebCustomizerPanel.aptOptionsLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(aptOptionsLabel)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 279, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(92, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(aptOptionsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 195, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel aptOptionsLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable optionsTable;
    // End of variables declaration//GEN-END:variables

}
