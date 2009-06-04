/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.repository;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.apache.lucene.search.BooleanQuery;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.QueryField;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  mkleint
 */
public class FindResultsPanel extends javax.swing.JPanel implements ExplorerManager.Provider {

    private BeanTreeView btv;
    private ExplorerManager manager;
    private ActionListener close;
    private DialogDescriptor dd;

    /** Creates new form FindResultsPanel */
    private FindResultsPanel() {
        initComponents();
        btv = new BeanTreeView();
        btv.setRootVisible(false);
        manager = new ExplorerManager();
        manager.setRootContext(new AbstractNode(Children.LEAF));
        add(btv, BorderLayout.CENTER);
    }

    FindResultsPanel(ActionListener actionListener, DialogDescriptor d) {
        this();
        close = actionListener;
        dd = d;
    }

    void find(final List<QueryField> fields) {
        Node loadingNode = GroupListChildren.createLoadingNode();
        Children.Array array = new Children.Array();
        array.add(new Node[]{loadingNode});
        manager.setRootContext(new AbstractNode(array));
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                List<NBVersionInfo> infos = null;
                try {
                    infos = RepositoryQueries.find(fields);
                } catch (BooleanQuery.TooManyClauses exc) {
                    List<QueryField> withoutClasses = new ArrayList<QueryField>(fields);
                    Iterator<QueryField> it = withoutClasses.iterator();
                    while (it.hasNext()) {
                        QueryField qf = it.next();
                        if (qf.getField().equals(QueryField.FIELD_CLASSES)) {
                            it.remove();
                            break;
                        }
                    }
                    try {
                        infos = RepositoryQueries.find(withoutClasses);
                    } catch (BooleanQuery.TooManyClauses exc2) {
                        infos = Collections.<NBVersionInfo>emptyList();
                        //TODO report as problem..
                    }
                }

                final List<NBVersionInfo> finalInfos = infos;

                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        manager.setRootContext(createRootNode(finalInfos));
                    }
                });
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        btnModify = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(btnModify, org.openide.util.NbBundle.getMessage(FindResultsPanel.class, "FindResultsPanel.btnModify.text")); // NOI18N
        btnModify.setFocusable(false);
        btnModify.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnModify.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnModify.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModifyActionPerformed(evt);
            }
        });
        jToolBar1.add(btnModify);

        org.openide.awt.Mnemonics.setLocalizedText(btnClose, org.openide.util.NbBundle.getMessage(FindResultsPanel.class, "FindResultsPanel.btnClose.text")); // NOI18N
        btnClose.setFocusable(false);
        btnClose.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnClose.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });
        jToolBar1.add(btnClose);

        add(jToolBar1, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
    if (close != null) {
        close.actionPerformed(evt);
    }
}//GEN-LAST:event_btnCloseActionPerformed

private void btnModifyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModifyActionPerformed
    Object ret = DialogDisplayer.getDefault().notify(dd);
    if (ret == DialogDescriptor.OK_OPTION) {
        find(((FindInRepoPanel) dd.getMessage()).getQuery());
    }
}//GEN-LAST:event_btnModifyActionPerformed

    public ExplorerManager getExplorerManager() {
        return manager;
    }

    public static Node createEmptyNode() {
        AbstractNode nd = new AbstractNode(Children.LEAF) {

            @Override
            public Image getIcon(int arg0) {
                return ImageUtilities.loadImage("org/netbeans/modules/maven/repository/empty.gif"); //NOI18N
            }

            @Override
            public Image getOpenedIcon(int arg0) {
                return getIcon(arg0);
            }
        };
        nd.setName("Empty"); //NOI18N

        nd.setDisplayName(NbBundle.getMessage(FindResultsPanel.class, "LBL_Node_Empty"));
        return nd;
    }

    private Node createRootNode(List<NBVersionInfo> infos) {
        Node node = null;
        Map<String, List<NBVersionInfo>> map = new HashMap<String, List<NBVersionInfo>>();

        for (NBVersionInfo nbvi : infos) {
            String key = nbvi.getGroupId() + " : " + nbvi.getArtifactId(); //NOI18n
            List<NBVersionInfo> get = map.get(key);
            if (get == null) {
                get = new ArrayList<NBVersionInfo>();
                map.put(key, get);
            }
            get.add(nbvi);
        }
        Set<String> keySet = map.keySet();
        if (keySet.size() > 0) {
            Children.Array array = new Children.Array();

            List<String> keyList = new ArrayList<String>(keySet);
            Collections.sort(keyList);
            node = new AbstractNode(array);
            for (String key : keyList) {
                array.add(new Node[]{new ArtifactNode(key, map.get(key))});
            }
        } else {
            Node empty = createEmptyNode();
            Children.Array array = new Children.Array();
            array.add(new Node[]{empty});
            node = new AbstractNode(array);
        }
        return node;

    }

    private static class ArtifactNode extends AbstractNode {

        private List<NBVersionInfo> versionInfos;

        public ArtifactNode(String name, final List<NBVersionInfo> list) {
            super(new Children.Keys<NBVersionInfo>() {

                @Override
                protected Node[] createNodes(NBVersionInfo info) {
                    RepositoryInfo rinf = RepositoryPreferences.getInstance().getRepositoryInfoById(info.getRepoId());
                    return new Node[]{new VersionNode(rinf, info, info.isJavadocExists(),
                                info.isSourcesExists(), true)
                            };
                }

                @Override
                protected void addNotify() {

                    setKeys(list);
                }
            });
            this.versionInfos = list;
            setName(name);
            setDisplayName(name);
        }

        @Override
        public Image getIcon(int arg0) {
            Image badge = ImageUtilities.loadImage("org/netbeans/modules/maven/repository/ArtifactBadge.png", true); //NOI18N

            return badge;
        }

        @Override
        public Image getOpenedIcon(int arg0) {
            return getIcon(arg0);
        }

        public List<NBVersionInfo> getVersionInfos() {
            return new ArrayList<NBVersionInfo>(versionInfos);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnModify;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables
}
