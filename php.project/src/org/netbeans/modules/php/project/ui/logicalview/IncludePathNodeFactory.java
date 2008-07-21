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
package org.netbeans.modules.php.project.ui.logicalview;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.php.project.*;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.modules.php.project.ui.customizer.CompositePanelProviderImpl;
import org.netbeans.modules.php.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Radek Matous
 */
public class IncludePathNodeFactory implements NodeFactory {

    /** Creates a new instance of SourcesNodeFactory */
    public IncludePathNodeFactory() {
    }

    public NodeList createNodes(Project p) {
        final PhpProject project = p.getLookup().lookup(PhpProject.class);
        return NodeFactorySupport.fixedNodeList(new DummyNode(new IncludePathRootNode(project)) {
            @Override
            public Action[] getActions(boolean context) {
                return new Action[]{new CustomizeIncludePathAction(project)};
            }
        });
    }

    private static class IncludePathRootNode extends AbstractNode implements PropertyChangeListener {

        private PhpProject project;
        private static final String RESOURCE_ICON_CLASSPATH = "org/netbeans/modules/php/project/ui/resources/referencedClasspath.gif"; //NOI18N
        private static final ImageIcon ICON_CLASSPATH = new ImageIcon(ImageUtilities.loadImage(RESOURCE_ICON_CLASSPATH));

        public IncludePathRootNode(PhpProject project) {
            super(createChildren(project));
            this.project = project;
            project.getEvaluator().addPropertyChangeListener(this);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(IncludePathNodeFactory.class, "LBL_IncludePath");//NOI18N
        }

        @Override
        public Image getIcon(int type) {
            return ICON_CLASSPATH.getImage();
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            setChildren(createChildren(project));
        }

        private static Children createChildren(PhpProject project) {
            return Children.create(new IncludePathChildFactory(project), false);
        }
    }

    private static class IncludePathChildFactory extends ChildFactory<Node> {

        private PhpProject project;

        public IncludePathChildFactory(PhpProject project) {
            this.project = project;
            assert project != null;
        }

        @Override
        protected boolean createKeys(List<Node> toPopulate) {
            toPopulate.addAll(createNodeList().keys());
            return true;
        }

        @Override
        protected Node createNodeForKey(Node key) {
            return key;
        }

        @SuppressWarnings("unchecked")
        NodeList<Node> createNodeList() {
            List<Node> list = new ArrayList<Node>();
            assert project != null;
            List<FileObject> includePath = PhpSourcePath.getIncludePath(project.getProjectDirectory());
            for (FileObject fileObject : includePath) {
                if (fileObject != null && fileObject.isFolder()) {
                    DataFolder df = DataFolder.findFolder(fileObject);
                    list.add(new IncludePathNode(df, project));
                }
            }
            Node[] nodes = list.toArray(new Node[list.size()]);
            return NodeFactorySupport.fixedNodeList(nodes);
        }
    }

    private static class IncludePathNode extends DummyNode {

        private static final String ICON_PATH = "org/netbeans/modules/php/project/ui/resources/libraries.gif"; //NOI18N
        private static final ImageIcon ICON = new ImageIcon(ImageUtilities.loadImage(ICON_PATH));

        public IncludePathNode(DataObject dobj, PhpProject project) {
            super(dobj.getNodeDelegate(), (dobj instanceof DataFolder) ?
                new DummyChildren(new DummyNode(dobj.getNodeDelegate()), new PhpSourcesFilter(project)) :
                Children.LEAF);
        }

        @Override
        public String getDisplayName() {
            FileObject fo = getOriginal().getLookup().lookup(FileObject.class);
            return fo != null ? FileUtil.getFileDisplayName(fo) : super.getDisplayName();
        }

        @Override
        public Image getIcon(int type) {
            return ICON.getImage();
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
    }

    private static class DummyNode extends FilterNode {
        public DummyNode(Node original) {
            super(original);
        }

        public DummyNode(Node original, org.openide.nodes.Children children) {
            super(original, children);
        }

        @Override
        public boolean canCopy() {
            return true;
        }

        @Override
        public boolean canCut() {
            return false;
        }

        @Override
        public boolean canDestroy() {
            return false;
        }

        @Override
        public boolean canRename() {
            return false;
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[]{};
        }

        @Override
        public boolean hasCustomizer() {
            return false;
        }
    }

    private static class DummyChildren extends FilterNode.Children {
        private DataFilter filter;
        DummyChildren(final Node originalNode, DataFilter filter) {
            super(originalNode);
            this.filter = filter;
        }

        @Override
        protected Node[] createNodes(Node key) {
            DataObject dobj = key.getLookup().lookup(DataObject.class);
            return (dobj != null && filter.acceptDataObject(dobj)) ? super.createNodes(key) : new Node[0];
        }

        @Override
        protected Node copyNode(final Node originalNode) {
            DataObject dobj = originalNode.getLookup().lookup(DataObject.class);
            return (dobj instanceof DataFolder) ? new DummyNode(dobj.getNodeDelegate(), new DummyChildren(originalNode, filter)) :
                new DummyNode(dobj.getNodeDelegate());
        }
    }

    private static class CustomizeIncludePathAction extends AbstractAction {
        private PhpProject project;
        CustomizeIncludePathAction(PhpProject project) {
            super(NbBundle.getMessage(CustomizeIncludePathAction.class, "LBL_CustomizeIncludePath"));//NOI18N
            this.project = project;
        }
        public void actionPerformed(ActionEvent e) {
            project.getLookup().lookup(CustomizerProviderImpl.class).
                    showCustomizer(CompositePanelProviderImpl.PHP_INCLUDE_PATH);
        }

    }

}
