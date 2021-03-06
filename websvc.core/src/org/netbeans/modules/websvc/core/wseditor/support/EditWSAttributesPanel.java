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

package org.netbeans.modules.websvc.core.wseditor.support;

import java.awt.Component;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeMap;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.core.wseditor.spi.WSEditor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author  Roderico Cruz
 */
public class EditWSAttributesPanel extends javax.swing.JPanel {
    
    private static final long serialVersionUID = 1L;
    private StringBuffer description;
    private TreeMap<String, WSEditor> treeMap;
    
    /** Creates new form EditWSAttributesPanel */
    public EditWSAttributesPanel() {
        initComponents();
        initComponents();
        treeMap = new TreeMap<String, WSEditor>(new AlphabeticalComparator());
        description = new StringBuffer("");
        descLabel.setText(NbBundle.getMessage(EditWSAttributesPanel.class, "MSG_NO_EDITORS"));
    }
    
    public void addTabs(Set<WSEditor> editors, Node node, JaxWsModel jaxWsModel){
        jTabbedPane1.removeAll();
        treeMap.clear();
        
        //Display tabs in alphabetical order
        for (WSEditor editor : editors){
            treeMap.put(editor.getTitle(), editor);
        }
        
        Set<String> titles = treeMap.keySet();
        for(String title : titles){
            WSEditor editor = treeMap.get(title);
            Component c = editor.createWSEditorComponent(node, jaxWsModel);
            jTabbedPane1.addTab(title, c);
            String desc = editor.getDescription();
            if(desc != null && !desc.trim().equals("")){
                c.getAccessibleContext().setAccessibleDescription(desc);
                description.append(" " );
                description.append(desc);
            }
        }
        String descText = description.toString();
        String helpText = NbBundle.getMessage(EditWSAttributesPanel.class, "MSG_PRESS_F1");
        if(!descText.equals("")){
            descText = "<html>" + descText + " " + helpText + " </html>";
            descLabel.setText(descText);
        }
    }
    class AlphabeticalComparator implements Comparator {
        public int compare(Object obj1, Object obj2) {
            String str1 = (String)obj1;
            String str2 = (String)obj2;
            return str1.toLowerCase().compareTo(str2.toLowerCase());
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        descLabel = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();

        descLabel.setText(org.openide.util.NbBundle.getMessage(EditWSAttributesPanel.class, "jLabel1.text")); // NOI18N
        descLabel.setPreferredSize(new java.awt.Dimension(261, 16));

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(descLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 597, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(descLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 621, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 481, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables
    
}
