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

package org.netbeans.modules.maven.navigator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.build.model.ModelLineage;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.model.pom.CiManagement;
import org.netbeans.modules.maven.model.pom.Contributor;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.Developer;
import org.netbeans.modules.maven.model.pom.IssueManagement;
import org.netbeans.modules.maven.model.pom.License;
import org.netbeans.modules.maven.model.pom.MailingList;
import org.netbeans.modules.maven.model.pom.Organization;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.maven.model.pom.POMQNames;
import org.netbeans.modules.maven.model.pom.Project;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.maven.model.pom.Repository;
import org.netbeans.modules.maven.model.pom.Scm;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author  mkleint
 */
public class POMModelPanel extends javax.swing.JPanel implements ExplorerManager.Provider, Runnable {

    private static final String NAVIGATOR_SHOW_UNDEFINED = "navigator.showUndefined"; //NOi18N
    private transient ExplorerManager explorerManager = new ExplorerManager();
    
    private BeanTreeView treeView;
    private DataObject current;
    private FileChangeAdapter adapter = new FileChangeAdapter(){
            @Override
            public void fileChanged(FileEvent fe) {
                showWaitNode();
                RequestProcessor.getDefault().post(POMModelPanel.this);
            }
        };
    private TapPanel filtersPanel;

    private boolean filterIncludeUndefined;

    /** Creates new form POMInheritancePanel */
    public POMModelPanel() {
        initComponents();
        filterIncludeUndefined = NbPreferences.forModule(POMModelPanel.class).getBoolean(NAVIGATOR_SHOW_UNDEFINED, false);

        treeView = (BeanTreeView)jScrollPane1;
        // filters
        filtersPanel = new TapPanel();
        filtersPanel.setOrientation(TapPanel.DOWN);
        // tooltip
        KeyStroke toggleKey = KeyStroke.getKeyStroke(KeyEvent.VK_T,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        String keyText = Utilities.keyToString(toggleKey);
        filtersPanel.setToolTipText(NbBundle.getMessage(POMModelPanel.class, "TIP_TapPanel", keyText)); //NOI18N

        JComponent buttons = createFilterButtons();
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
        filtersPanel.add(buttons);
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) //NOI18N
            filtersPanel.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N

        add(filtersPanel, BorderLayout.SOUTH);

    }
    
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    void navigate(DataObject d) {
        if (current != null) {
            current.getPrimaryFile().removeFileChangeListener(adapter);
        }
        current = d;
        current.getPrimaryFile().addFileChangeListener(adapter);
        showWaitNode();
        RequestProcessor.getDefault().post(this);
    }
    
    public void run() {
        if (current != null) {
            File file = FileUtil.toFile(current.getPrimaryFile());
            // can be null for stuff in jars?
            if (file != null) {
                try {
                    ModelLineage lin = EmbedderFactory.createModelLineage(file, EmbedderFactory.createOnlineEmbedder(), false);
                    @SuppressWarnings("unchecked")
                    Iterator<File> it = lin.fileIterator();
                    POMModelVisitor.POMCutHolder hold = new POMModelVisitor.POMCutHolder();
                    POMQNames names = null;
                    

                    while (it.hasNext()) {
                        File pom = it.next();
                        FileUtil.refreshFor(pom);
                        FileObject fo = FileUtil.toFileObject(pom);
                        if (fo != null) {
                            ModelSource ms = org.netbeans.modules.maven.model.Utilities.createModelSource(fo);
                            POMModel mdl = POMModelFactory.getDefault().getModel(ms);
                            if (mdl != null) {
                                hold.addCut(mdl.getProject());
                                names = mdl.getPOMQNames();
                            } else {
                                System.out.println("no model for " + pom);
                            }
                        } else {
                            System.out.println("no fileobject for " + pom);
                        }
                    }
                    final POMModelVisitor.PomChildren childs = new POMModelVisitor.PomChildren(hold, names, Project.class);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                           treeView.setRootVisible(false);
                           explorerManager.setRootContext(new AbstractNode(childs));
                        } 
                    });
                } catch (ProjectBuildingException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.FINE, "Error reading model lineage", ex);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                           treeView.setRootVisible(true);
                           explorerManager.setRootContext(createErrorNode());
                        }
                    });
                }
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                       treeView.setRootVisible(false);
                       explorerManager.setRootContext(createEmptyNode());
                    } 
                });
            }
        }
    }

    /**
     * 
     */
    void release() {
        if (current != null) {
            current.getPrimaryFile().removeFileChangeListener(adapter);
        }
        current = null;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               treeView.setRootVisible(false);
               explorerManager.setRootContext(createEmptyNode());
            } 
        });
    }

    /**
     * 
     */
    public void showWaitNode() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               treeView.setRootVisible(true);
               explorerManager.setRootContext(createWaitNode());
            } 
        });
    }

    private JComponent createFilterButtons() {
        Box box = new Box(BoxLayout.X_AXIS);
        box.setBorder(new EmptyBorder(1, 2, 3, 5));

            // configure toolbar
        JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL) {
            @Override
            protected void paintComponent(Graphics g) {
            }
        };
            toolbar.setFloatable(false);
            toolbar.setRollover(true);
            toolbar.setBorderPainted(false);
            toolbar.setBorder(BorderFactory.createEmptyBorder());
            toolbar.setOpaque(false);
            toolbar.setFocusable(false);
            JToggleButton tg1 = new JToggleButton(new ShowUndefinedAction());
            tg1.setSelected(filterIncludeUndefined);
            toolbar.add(tg1);
            Dimension space = new Dimension(3, 0);
            toolbar.addSeparator(space);

            box.add(toolbar);
            return box;

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new BeanTreeView();

        setLayout(new java.awt.BorderLayout());
        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    
    private static Node createWaitNode() {
        AbstractNode an = new AbstractNode(Children.LEAF);
        an.setIconBaseWithExtension("org/netbeans/modules/maven/navigator/wait.gif");
        an.setDisplayName(NbBundle.getMessage(POMInheritancePanel.class, "LBL_Wait"));
        return an;
    }

    private static Node createEmptyNode() {
        AbstractNode an = new AbstractNode(Children.LEAF);
        return an;
    }

    private static Node createErrorNode() {
        AbstractNode an = new AbstractNode(Children.LEAF);
        an.setDisplayName(NbBundle.getMessage(POMInheritancePanel.class, "LBL_Error"));
        return an;
    }

//    protected void addSingleFieldNode(List<POMModel> key, String[] vals, String dispName, List<Node> nds) {
//        if (!filterIncludeUndefined || definesValue(vals)) {
//            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, key, dispName, vals));
//        }
//    }
//
//    private void addObjectNode(List<POMModel> key, List sMan, Children sueManagementChildren, String displayName, List<Node> nds) {
//        if (!filterIncludeUndefined || definesValue(sMan.toArray())) {
//            nds.add(new ObjectNode(Lookup.EMPTY, sueManagementChildren, key, displayName, sMan));
//        }
//    }
//
//    private void addListNode(List<POMModel> key, List<List> sMan, ChildrenCreator chc, String displayName, List<Node> nds) {
//        if (!filterIncludeUndefined || definesValue(sMan.toArray())) {
//            nds.add(new ListNode(Lookup.EMPTY, chc, key, displayName, sMan));
//        }
//    }

    
    // <editor-fold defaultstate="collapsed" desc="POM Children">
    private class PomChildren extends Children.Keys<Object> {
        private List<POMModel> lin;
        public PomChildren(List<POMModel> lineage) {
            setKeys(new Object[] {lineage} );
            this.lin = lineage;
        }

        public void reshow() {
            this.refreshKey(lin);
        }
        
        @Override
        protected Node[] createNodes(Object key) {
            @SuppressWarnings("unchecked")
            List<POMModel> mods = (List<POMModel>) key;
            Project[] models = new Project[mods.size()];
            int index = 0;
            for (POMModel md : mods) {
                models[index] = md.getProject();
                index++;
            }

            List<Node> nds = new ArrayList<Node>();

//            @SuppressWarnings("unchecked")
//            List<Properties> props = getValue(models, "getProperties", Project.class);
//            addObjectNode(mods, props, new PropsChildren(props, mods), "Properties", nds);
            return nds.toArray(new Node[0]);
        }

        
    }
    // </editor-fold>

 

//    // <editor-fold defaultstate="collapsed" desc="Properties Children">
//    private static class PropsChildren extends Children.Keys<List<Properties>> {
//        private List<POMModel> lineage;
//        public PropsChildren(List<Properties> list, List<POMModel> lin) {
//            setKeys(new List[] {list});
//            lineage = lin;
//        }
//
//        @Override
//        protected Node[] createNodes(List<Properties> key) {
//            Properties[] models = key.toArray(new Properties[key.size()]);
//            List<Node> nds = new ArrayList<Node>();
//            java.util.Map<String, List<String>> properties = getPropertyValues(models);
//            for (java.util.Map.Entry<String, List<String>> entry : properties.entrySet()) {
//                String[] vals = entry.getValue().toArray(new String[0]);
//                nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, entry.getKey(), vals));
//            }
//            return nds.toArray(new Node[0]);
//        }
//    }
//    // </editor-fold>


//    // <editor-fold defaultstate="collapsed" desc="Repository Children">
//    private static class RepositoryChildren extends Children.Keys<List<Repository>> {
//        private List<POMModel> lineage;
//        public RepositoryChildren(List<Repository> list, List<POMModel> lin) {
//            setKeys(new List[] {list});
//            lineage = lin;
//        }
//
//        @Override
//        protected Node[] createNodes(List<Repository> key) {
//            Repository[] models = key.toArray(new Repository[key.size()]);
//            List<Node> nds = new ArrayList<Node>();
//            String[] vals = getStringValue(models, "getId", Repository.class);
//            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Id", vals));
//            vals = getStringValue(models, "getName", Repository.class);
//            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Name", vals));
//            vals = getStringValue(models, "getUrl", Repository.class);
//            nds.add(new SingleFieldNode(Lookup.EMPTY, Children.LEAF, lineage, "Url", vals));
//            return nds.toArray(new Node[0]);
//        }
//    }
//    // </editor-fold>

    static Map<String, List<String>> getPropertyValues(Properties[] models) {
        TreeMap<String, List<String>> toRet = new TreeMap<String, List<String>>();
        int nulls = 0;
        for (Properties prop : models) {
            for (Object keyProp : prop.getProperties().keySet()) {
                String k = (String) keyProp;
                List<String> vals = toRet.get(k);
                if (vals == null) {
                    vals = new ArrayList<String>();
                    toRet.put(k, vals);
                }
                if (vals.size() < nulls) {
                    vals.addAll(Arrays.asList(new String[nulls - vals.size()]));
                }
                vals.add(prop.getProperty(k));
            }
            nulls = nulls + 1;
        }
        for (List<String> vals : toRet.values()) {
            if (vals.size() < models.length) {
                vals.addAll(Arrays.asList(new String[models.length - vals.size()]));
            }
        }
        return toRet;
    }

    /**
     * returns true if the value is defined in current pom. Assuming the first index is the
     * current pom and the next value is it's parent, etc.
     */
    static boolean isValueDefinedInCurrent(Object[] values) {
        return values[0] != null;
    }

    /**
     * returns true if the value is defined in current pom
     * and one of the parent poms as well.
     */
    static boolean overridesParentValue(Object[] values) {
        if (values.length <= 1) {
            return false;
        }
        boolean curr = values[0] != null;
        boolean par = false;
        for (int i = 1; i < values.length; i++) {
            if (values[i] != null) {
                par = true;
                break;
            }
        }
        return curr && par;

    }

    /**
     * returns true if the value is defined in in any pom. Assuming the first index is the
     * current pom and the next value is it's parent, etc.
     */
    static boolean definesValue(Object[] values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                return true;
            }
        }
        return false;
    }


    /**
     * gets the first defined value from the list. Assuming the first index is the
     * current pom and the next value is it's parent, etc.
     */
    static String getValidValue(String[] values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                return values[i];
            }
        }
        return null;
    }
    

    private class ShowUndefinedAction extends AbstractAction {

        public ShowUndefinedAction() {
            putValue(SMALL_ICON, ImageUtilities.image2Icon(ImageUtilities.loadImage("org/netbeans/modules/maven/navigator/filterHideFields.gif")));
            putValue(SHORT_DESCRIPTION, "Show only POM elements defined in at least one place.");
        }


        public void actionPerformed(ActionEvent e) {
            filterIncludeUndefined = !filterIncludeUndefined;
            NbPreferences.forModule(POMModelPanel.class).putBoolean( NAVIGATOR_SHOW_UNDEFINED, filterIncludeUndefined);

            POMModelVisitor.PomChildren keys = (POMModelVisitor.PomChildren) explorerManager.getRootContext().getChildren();
            keys.reshow();
        }
        
    }
}

