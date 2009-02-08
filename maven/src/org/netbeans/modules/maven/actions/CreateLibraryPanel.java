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


package org.netbeans.modules.maven.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.dependencies.CheckNode;
import org.netbeans.modules.maven.dependencies.CheckNodeListener;
import org.netbeans.modules.maven.dependencies.CheckRenderer;
import org.netbeans.modules.maven.embedder.DependencyTreeFactory;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkleint
 */
public class CreateLibraryPanel extends javax.swing.JPanel {
    private MavenProject project;
    private DependencyNode rootnode;

    /** Creates new form CreateLibraryPanel */
    public CreateLibraryPanel(MavenProject prj) {
        initComponents();
        project = prj;
        DefaultComboBoxModel mdl = new DefaultComboBoxModel();

        for (LibraryManager manager : LibraryManager.getOpenManagers()) {
            mdl.addElement(manager);
        }
        comManager.setModel(mdl);
        comManager.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkLibraryName();
            }

        });
        comManager.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                LibraryManager man = (LibraryManager) value;
                return super.getListCellRendererComponent(list, man.getDisplayName(), index, isSelected, cellHasFocus);
            }

        });
        trDeps.setCellRenderer(new CheckRenderer(false));
        CheckNodeListener l = new CheckNodeListener(false);
        trDeps.addMouseListener(l);
        trDeps.addKeyListener(l);
        trDeps.setToggleClickCount(0);
        trDeps.setRootVisible(false);
        trDeps.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));

        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                rootnode = DependencyTreeFactory.createDependencyTree(project, EmbedderFactory.getOnlineEmbedder(), Artifact.SCOPE_TEST);
                trDeps.setModel(new DefaultTreeModel(createDependenciesList()));
            }
        });
        checkLibraryName();
    }

    private TreeNode createDependenciesList() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(null, true);
        @SuppressWarnings("unchecked")
        Set<Artifact> artifacts = project.getArtifacts();
        Icon icn = ImageUtilities.image2Icon(ImageUtilities.loadImage("org/netbeans/modules/maven/TransitiveDependencyIcon.png", true));
        Icon icn2 = ImageUtilities.image2Icon(ImageUtilities.loadImage("org/netbeans/modules/maven/DependencyIcon.png", true));
        Artifact rootA = project.getArtifact();
        String label = rootA.getGroupId() + ":" + rootA.getArtifactId();
        CheckNode nd = new CheckNode(rootA, label, icn2);
        nd.setSelected(true);
        root.add(nd);

        Set<Artifact> artifacts2 = new TreeSet<Artifact>(new Comp());
        artifacts2.addAll(artifacts);

        for (Artifact a : artifacts2) {
            label = a.getGroupId() + ":" + a.getArtifactId() + " [" + a.getScope() + "]";
            nd = new CheckNode(a, label, a.getDependencyTrail().size() > 2 ? icn : icn2);
            nd.setSelected(getScopeOrder(a.getScope()) > 3); //don't include tests and provided/system items
            root.add(nd);
        }
        return root;
    }

    private void checkLibraryName() {
        LibraryManager manager = (LibraryManager) comManager.getSelectedItem();
        String currentName = txtName.getText();
        int index = 0;
        while (currentName.trim().length() == 0 || manager.getLibrary(currentName.trim()) != null) {
            currentName = project.getArtifactId();
            if (index > 0) {
                currentName = currentName + index;
            }
            index++;
        }
        if (!currentName.equals(txtName.getText())) {
            txtName.setText(currentName);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblManager = new javax.swing.JLabel();
        comManager = new javax.swing.JComboBox();
        lblName = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        cbCopy = new javax.swing.JCheckBox();
        txtCopy = new javax.swing.JTextField();
        btnCopy = new javax.swing.JButton();
        lblIncludes = new javax.swing.JLabel();
        cbJavadocSource = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        trDeps = new javax.swing.JTree();

        lblManager.setLabelFor(comManager);
        org.openide.awt.Mnemonics.setLocalizedText(lblManager, org.openide.util.NbBundle.getMessage(CreateLibraryPanel.class, "CreateLibraryPanel.lblManager.text")); // NOI18N

        comManager.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        lblName.setLabelFor(txtName);
        org.openide.awt.Mnemonics.setLocalizedText(lblName, org.openide.util.NbBundle.getMessage(CreateLibraryPanel.class, "CreateLibraryPanel.lblName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbCopy, org.openide.util.NbBundle.getMessage(CreateLibraryPanel.class, "CreateLibraryPanel.cbCopy.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnCopy, org.openide.util.NbBundle.getMessage(CreateLibraryPanel.class, "CreateLibraryPanel.btnCopy.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblIncludes, org.openide.util.NbBundle.getMessage(CreateLibraryPanel.class, "CreateLibraryPanel.lblIncludes.text")); // NOI18N

        cbJavadocSource.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(cbJavadocSource, org.openide.util.NbBundle.getMessage(CreateLibraryPanel.class, "CreateLibraryPanel.cbJavadocSource.text")); // NOI18N

        jScrollPane1.setViewportView(trDeps);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(cbJavadocSource)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblManager)
                            .add(lblName)
                            .add(cbCopy))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(txtCopy, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(btnCopy))
                            .add(txtName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                            .add(comManager, 0, 258, Short.MAX_VALUE)))
                    .add(lblIncludes))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblManager)
                    .add(comManager, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblName)
                    .add(txtName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cbCopy)
                    .add(txtCopy, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(btnCopy))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblIncludes)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 175, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(cbJavadocSource)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCopy;
    private javax.swing.JCheckBox cbCopy;
    private javax.swing.JCheckBox cbJavadocSource;
    private javax.swing.JComboBox comManager;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblIncludes;
    private javax.swing.JLabel lblManager;
    private javax.swing.JLabel lblName;
    private javax.swing.JTree trDeps;
    private javax.swing.JTextField txtCopy;
    private javax.swing.JTextField txtName;
    // End of variables declaration//GEN-END:variables

    LibraryManager getLibraryManager() {
        return (LibraryManager) comManager.getSelectedItem();
    }

    String getLibraryName() {
        return txtName.getText();
    }

    boolean isAllSourceAndJavadoc() {
        return cbJavadocSource.isSelected();
    }

    File getCopyDirectory() {
        if (!cbCopy.isSelected()) {
            return null;
        }
        String dir = txtCopy.getText().trim();
        //TODO resolve relatively to library manager..
        return null;
    }

    List<Artifact> getIncludeArtifacts() {
        Object root = trDeps.getModel().getRoot();
        int count = trDeps.getModel().getChildCount(root);
        List<Artifact> toRet = new ArrayList<Artifact>();
        for (int i =0; i < count; i++) {
            CheckNode chn = (CheckNode) trDeps.getModel().getChild(root, i);
            if (chn.isSelected()) {
                Artifact art = (Artifact) chn.getUserObject();
                toRet.add(art);
            }
        }
        return toRet;
    }

    private static int getScopeOrder(String scope) {
        if (scope == null) {
            return 10;
        }
        if (scope.equals(Artifact.SCOPE_COMPILE)) {
            return 5;
        }
        if (scope.equals(Artifact.SCOPE_RUNTIME)) {
            return 4;
        }
        if (scope.equals(Artifact.SCOPE_TEST)) {
            return 3;
        }
        return 0;
    }

    private static class Comp implements Comparator<Artifact> {

        public int compare(Artifact a1, Artifact a2) {
            String scope1 = a1.getScope();
            String scope2 = a2.getScope();
            int order1 = getScopeOrder(scope1);
            int order2 = getScopeOrder(scope2);
            if (order1 > order2) {
                return -1;
            }
            if (order2 > order1) {
                return 1;
            }
            int trail1 = a1.getDependencyTrail().size();
            int trail2 = a2.getDependencyTrail().size();
            if (trail1 > trail2) {
                return -1;
            }
            if (trail2 > trail1) {
                return 1;
            }

            return a1.getId().compareTo(a2.getId());
        }


    }

}
