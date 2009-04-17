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

import hidden.org.codehaus.plexus.util.StringUtils;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.ListSelectionModel;
import javax.swing.text.html.HTMLEditorKit;
import org.apache.maven.model.Plugin;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.indexer.api.PluginIndexManager;
import org.netbeans.modules.maven.options.MavenSettings;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
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
    private Pattern COMPLETE = Pattern.compile("(.+)[:](.+)[:](.+)[:](.+)"); //NOI18N
    private Pattern SHORT = Pattern.compile("(.+)[:](.+)"); //NOI18N

    /** Creates new form AddPropertyDialog */
    public AddPropertyDialog(NbMavenProjectImpl prj, String goalsText) {
        initComponents();
        manager = new ExplorerManager();
        project = prj;
        okbutton = new JButton(NbBundle.getMessage(AddPropertyDialog.class, "BTN_OK"));
        manager.setRootContext(Node.EMPTY);
        tpDesc.setEditorKit(new HTMLEditorKit());
        manager.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                Node[] nds = getExplorerManager().getSelectedNodes();
                if (nds.length != 1) {
                    okbutton.setEnabled(false);
                } else {
                    PluginIndexManager.ParameterDetail plg = nds[0].getLookup().lookup(PluginIndexManager.ParameterDetail.class);
                    if (plg != null) {
                        okbutton.setEnabled(true);
                        tpDesc.setText(plg.getHtmlDetails(false));
                    } else {
                        okbutton.setEnabled(false);
                        tpDesc.setText("");
                    }
                }
            }
        });
        ((BeanTreeView)tvExpressions).setRootVisible(false);
        ((BeanTreeView)tvExpressions).setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.goalsText = goalsText;
        RequestProcessor.getDefault().post(new Loader());
    }

    public JButton getOkButton() {
        return okbutton;
    }

    String getSelectedExpression() {
        Node[] nds = getExplorerManager().getSelectedNodes();
        if (nds.length == 1) {
            PluginIndexManager.ParameterDetail hld =  nds[0].getLookup().lookup(PluginIndexManager.ParameterDetail.class);
            if (hld != null) {
                return hld.getExpression();
            }
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
        jScrollPane2 = new javax.swing.JScrollPane();
        tpDesc = new javax.swing.JTextPane();

        lblPropertyExpressions.setText(org.openide.util.NbBundle.getMessage(AddPropertyDialog.class, "AddPropertyDialog.lblPropertyExpressions.text")); // NOI18N

        tpDesc.setEditable(false);
        jScrollPane2.setViewportView(tpDesc);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, tvExpressions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, lblPropertyExpressions)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(lblPropertyExpressions)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tvExpressions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblPropertyExpressions;
    private javax.swing.JTextPane tpDesc;
    private javax.swing.JScrollPane tvExpressions;
    // End of variables declaration//GEN-END:variables

    public ExplorerManager getExplorerManager() {
        return manager;
    }

    private class Loader implements Runnable {
        public void run() {
            Children.Array rootChilds = new Children.Array();

            //groupId | artifactId | mojo
            Set<String> pluginKeys = new TreeSet<String>();

            String[] goals = StringUtils.split(goalsText, " "); //NOI18N
            for (String goal : goals) {
                String groupId = null;
                String artifactid = null;
                String version = null;
                String mojo = null;
                Matcher m1 = COMPLETE.matcher(goal);
                if (m1.matches()) {
                    groupId = m1.group(1);
                    artifactid = m1.group(2);
                    version = m1.group(3);
                    mojo = m1.group(4);
                } else {
                    Matcher m2 = SHORT.matcher(goal);
                    if (m2.matches()) {
                        String prefix = m2.group(1);
                        try {
                            Set<String> plgs = PluginIndexManager.getPluginsForGoalPrefix(prefix);
                            if (plgs != null) {
                                mojo = m2.group(2);
                                for (String plg : plgs) {
                                    pluginKeys.add(plg + "|" + mojo); //NOI18N
                                }
                            }
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
                addPluginNode(groupId, artifactid, version, mojo, rootChilds);
            }

            Set<String> extensionsids = new HashSet<String>();
            @SuppressWarnings("unchecked")
            List<Plugin> plgns = project.getOriginalMavenProject().getBuildPlugins();
            if (plgns != null) {
                for (Plugin plg : plgns) {
                    if (plg != null && plg.isExtensions()) {
                        extensionsids.add(plg.getGroupId() + ":" + plg.getArtifactId() + ":" + plg.getVersion()); //NOI18N
                        continue;
                    }
                    //only add those with executions and goals..
                }
            }
            String mvnVersion = MavenSettings.getCommandLineMavenVersion();
            String packaging = project.getOriginalMavenProject().getPackaging();

            if (packaging != null) {
                try {
                    Map<String, List<String>> cycle = PluginIndexManager.getLifecyclePlugins(packaging, mvnVersion, extensionsids.toArray(new String[0]));
                    if (cycle != null) {
                        for (List<String> phase : cycle.values()) {
                            for (String mapping : phase) {
                                String[] split = StringUtils.split(mapping, ":"); //NOI18N
                                String version = findVersion(split[0], split[1]);
                                addPluginNode(split[0], split[1], version, split[2], rootChilds);
                            }
                        }
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            AbstractNode root = new AbstractNode(rootChilds);
            root.setName("root");
            getExplorerManager().setRootContext(root);
        }

    }


    private void addPluginNode(String groupId, String artifactId, String version, String mojo, Children.Array rootChilds) {
        if (version == null || groupId == null || artifactId == null) {
            return;
        }
        assert rootChilds != null;
        Children.Array pluginChilds = new Children.Array();
        try {
            Set<PluginIndexManager.ParameterDetail> exprs = PluginIndexManager.getPluginParameters(groupId, artifactId, version, mojo);
            if (exprs != null) {
                for (PluginIndexManager.ParameterDetail el : exprs) {
                    if (el.getExpression() == null) {
                        continue;
                    }
                    AbstractNode param = new AbstractNode(Children.LEAF, Lookups.singleton(el));
                    param.setIconBaseWithExtension("org/netbeans/modules/maven/customizer/param.png");
                    param.setDisplayName(el.getExpression() + " (" + el.getName() + ")"); //NOI18N
                    pluginChilds.add(new Node[]{param});
                }
            }
        } catch (Exception exception) {
            Logger.getLogger(AddPropertyDialog.class.getName()).log(Level.INFO, "Error while retrieving list of expressions", exception); //NOI18N
        }
        AbstractNode plugin = new AbstractNode(pluginChilds);
        plugin.setIconBaseWithExtension("org/netbeans/modules/maven/customizer/mojo.png");
        plugin.setDisplayName(groupId + ":" + artifactId + (mojo != null ? (" [" + mojo + "]") : "")); //NOI18N
        rootChilds.add(new Node[]{plugin});
    }


    private String findVersion(String groupId, String artifactId) {
        String key = groupId + ":" + artifactId;
        List<Plugin> plugins = new ArrayList<Plugin>();
        @SuppressWarnings("unchecked")
        List<Plugin> bld = project.getOriginalMavenProject().getBuildPlugins();
        if (bld != null) {
            plugins.addAll(bld);
        }
        if (project.getOriginalMavenProject().getPluginManagement() != null) {
            @SuppressWarnings("unchecked")
            List<Plugin> pm = project.getOriginalMavenProject().getPluginManagement().getPlugins();
            if (pm != null) {
                plugins.addAll(pm);
            }
        }

        for (Plugin plg : plugins) {
            if (key.equals(plg.getKey())) {
                return plg.getVersion();
            }
        }
        return null;
    }


}
