/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.customizer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.ListSelectionModel;
import org.apache.maven.model.Plugin;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.spi.grammar.GoalsProvider;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mkleint
 */
public class AddPropertyDialog extends javax.swing.JPanel implements ExplorerManager.Provider {
    private ExplorerManager manager;
    private NbMavenProjectImpl project;
    private JButton okbutton;
    private String goalsText;

    /** Creates new form AddPropertyDialog */
    public AddPropertyDialog(NbMavenProjectImpl prj, String goalsText) {
        initComponents();
        manager = new ExplorerManager();
        project = prj;
        okbutton = new JButton("OK");
        manager.setRootContext(Node.EMPTY);
        manager.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                Node[] nds = getExplorerManager().getSelectedNodes();
                if (nds.length != 1) {
                    okbutton.setEnabled(false);
                } else {
                    String str = nds[0].getLookup().lookup(String.class);
                    if (str != null) {
                        okbutton.setEnabled(true);
                    } else {
                        okbutton.setEnabled(false);
                    }
                }
            }
        });
        ((BeanTreeView)tvExpressions).setRootVisible(false);
        ((BeanTreeView)tvExpressions).setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        RequestProcessor.getDefault().post(new Loader());
        this.goalsText = goalsText;
    }

    public JButton getOkButton() {
        return okbutton;
    }

    String getSelectedExpression() {
        Node[] nds = getExplorerManager().getSelectedNodes();
        if (nds.length == 1) {
            return nds[0].getLookup().lookup(String.class);
        }
        return null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tvExpressions = new BeanTreeView();
        lblPropertyExpressions = new javax.swing.JLabel();

        lblPropertyExpressions.setText(org.openide.util.NbBundle.getMessage(AddPropertyDialog.class, "AddPropertyDialog.lblPropertyExpressions.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(tvExpressions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .add(lblPropertyExpressions))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(lblPropertyExpressions)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tvExpressions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblPropertyExpressions;
    private javax.swing.JScrollPane tvExpressions;
    // End of variables declaration//GEN-END:variables

    public ExplorerManager getExplorerManager() {
        return manager;
    }

    private class Loader implements Runnable {
        public void run() {
            GoalsProvider provider = Lookup.getDefault().lookup(GoalsProvider.class);
            Children.Array rootChilds = new Children.Array();
            @SuppressWarnings("unchecked")
            List<Plugin> plgns = project.getOriginalMavenProject().getBuildPlugins();
            for (Plugin plg : plgns) {
                Children.Array pluginChilds = new Children.Array();
                Set<String[]> exprs = provider.getPluginPropertyExpression(plg.getGroupId(), plg.getArtifactId(), plg.getVersion(), null);
                for (String[] el : exprs) {
                    AbstractNode param = new AbstractNode(Children.LEAF, Lookups.singleton(el[1]));
                    param.setDisplayName(el[0] + "->" + el[1]);
                    pluginChilds.add(new Node[] {param});
                }
                AbstractNode plugin = new AbstractNode(pluginChilds);
                plugin.setDisplayName(plg.getKey());
                rootChilds.add(new Node[] {plugin});
            }
            //TODO add also lifecycle plugins

            //TODO add plugins from the action command line..
            AbstractNode root = new AbstractNode(rootChilds);
            root.setName("root");
            getExplorerManager().setRootContext(root);
        }

    }


}
