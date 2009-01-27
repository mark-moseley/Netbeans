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


package org.netbeans.modules.maven.repository.ui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.maven.embedder.DependencyTreeFactory;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 *
 * @author mkleint
 */
public class DependencyPanel extends TopComponent implements MultiViewElement, LookupListener {
    private MultiViewElementCallback callback;
    private Result<MavenProject> result;
    private final static Icon dirIcon;
    private final static Icon trIcon;

    static {
        dirIcon = ImageUtilities.image2Icon(ImageUtilities.loadImage("org/netbeans/modules/maven/repository/ui/DependencyIcon.png", true));
        trIcon = ImageUtilities.image2Icon(ImageUtilities.loadImage("org/netbeans/modules/maven/repository/ui/TransitiveDependencyIcon.png", true));
    }

    DependencyPanel(Lookup lookup) {
        super(lookup);
        initComponents();
        Rend r = new Rend();
        lstTest.setCellRenderer(r);
        lstRuntime.setCellRenderer(r);
        lstCompile.setCellRenderer(r);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblCompile = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstCompile = new javax.swing.JList();
        lblRuntime = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstRuntime = new javax.swing.JList();
        lblTest = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        lstTest = new javax.swing.JList();

        lblCompile.setText(org.openide.util.NbBundle.getMessage(DependencyPanel.class, "DependencyPanel.lblCompile.text")); // NOI18N

        jScrollPane1.setViewportView(lstCompile);

        lblRuntime.setText(org.openide.util.NbBundle.getMessage(DependencyPanel.class, "DependencyPanel.lblRuntime.text")); // NOI18N

        jScrollPane2.setViewportView(lstRuntime);

        lblTest.setText(org.openide.util.NbBundle.getMessage(DependencyPanel.class, "DependencyPanel.lblTest.text")); // NOI18N

        jScrollPane3.setViewportView(lstTest);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblCompile)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                    .add(lblRuntime))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblTest)
                    .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE))
                .add(15, 15, 15))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblCompile)
                    .add(lblRuntime)
                    .add(lblTest))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblCompile;
    private javax.swing.JLabel lblRuntime;
    private javax.swing.JLabel lblTest;
    private javax.swing.JList lstCompile;
    private javax.swing.JList lstRuntime;
    private javax.swing.JList lstTest;
    // End of variables declaration//GEN-END:variables

    public JComponent getVisualRepresentation() {
        return this;
    }

    public JComponent getToolbarRepresentation() {
        return new JPanel();
    }


    @Override
    public void componentOpened() {
        super.componentOpened();
        result = getLookup().lookup(new Lookup.Template<MavenProject>(MavenProject.class));
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                populateFields();
            }
        });
        result.addLookupListener(this);
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
        result.removeLookupListener(this);
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
    }

    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
    }


    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
    }

    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    private void populateFields() {
        boolean loading = true;
        Iterator<? extends MavenProject> iter = result.allInstances().iterator();
        if (iter.hasNext()) {
            loading = false;
            MavenProject prj = iter.next();
            final DependencyNode root = DependencyTreeFactory.createDependencyTree(prj, EmbedderFactory.getOnlineEmbedder(), "test");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setDepModel(lstCompile, root, Arrays.asList(new String[]{ Artifact.SCOPE_COMPILE, Artifact.SCOPE_PROVIDED}));
                    setDepModel(lstRuntime, root, Arrays.asList(new String[]{ Artifact.SCOPE_RUNTIME}));
                    setDepModel(lstTest, root, Arrays.asList(new String[]{ Artifact.SCOPE_TEST}));
                }
            });
        } else {

        }
    }

    public void resultChanged(LookupEvent ev) {
        populateFields();
    }

    private void setDepModel(JList lst, DependencyNode root, List<String> scopes) {
        DefaultListModel dlm = new DefaultListModel();
        NodeVisitor vis = new NodeVisitor(scopes);
        root.accept(vis);
        for (DependencyNode d : vis.getDirects()) {
            dlm.addElement(d);
        }
        for (DependencyNode d : vis.getTransitives()) {
            dlm.addElement(d);
        }
        lst.setModel(dlm);
        lst.putClientProperty("directs", vis.getDirects());
        lst.putClientProperty("trans", vis.getTransitives());
    }

    private static class Rend extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component cmp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof DependencyNode) {
                DependencyNode d = (DependencyNode)value;
                JLabel lbl = (JLabel)cmp;
                lbl.setText(d.getArtifact().getArtifactId() + ":" + d.getArtifact().getVersion());
                @SuppressWarnings("unchecked")
                List<DependencyNode> dirs = (List<DependencyNode>)list.getClientProperty("directs");
                if (dirs.contains(d)) {
                    lbl.setIcon(dirIcon);
                } else {
                    lbl.setIcon(trIcon);
                }
            }
            return cmp;
        }

    }

    private static class NodeVisitor implements DependencyNodeVisitor {
        private List<DependencyNode> directs;
        private List<DependencyNode> trans;
        private List<String> scopes;
        private DependencyNode root;
        private Stack<DependencyNode> path;

        private NodeVisitor(List<String> scopes) {
            this.scopes = scopes;
        }

    public boolean visit(DependencyNode node) {
        if (root == null) {
            root = node;
            directs = new ArrayList<DependencyNode>();
            trans = new ArrayList<DependencyNode>();
            path = new Stack<DependencyNode>();
            return true;
        }
        if (node.getState() == DependencyNode.INCLUDED &&
                scopes.contains(node.getArtifact().getScope())) {
            if (path.empty()) {
                directs.add(node);
            } else {
                trans.add(node);
            }
        }
        path.push(node);
        return true;
    }

    public boolean endVisit(DependencyNode node) {
        if (root == node) {
            root = null;
            path = null;
            return true;
        }
        path.pop();
        return true;
    }

        private Iterable<DependencyNode> getDirects() {
            return directs;
        }

        private Iterable<DependencyNode> getTransitives() {
            return trans;
        }

    }


}
