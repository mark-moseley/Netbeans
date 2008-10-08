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

package org.netbeans.modules.apisupport.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Provides a logical view of a NetBeans module project.
 * @author Jesse Glick
 */
public final class ModuleLogicalView implements LogicalViewProvider {
    
    
    private final NbModuleProject project;
    
    public ModuleLogicalView(NbModuleProject project) {
        this.project = project;
    }
    
    public Node createLogicalView() {
        return new RootNode(project);
    }
    
    /** cf. #45952 */
    public Node findPath(Node root, Object target) {
        if (root.getLookup().lookup(NbModuleProject.class) != project) {
            // Not intended for this project. Should not normally happen anyway.
            return null;
        }
        
        Node[] rootChildren = root.getChildren().getNodes(true);
        DataObject file;
        
        if (target instanceof FileObject) {
            try {
                file = DataObject.find((FileObject) target);
            } catch (DataObjectNotFoundException e) {
                throw new AssertionError(e);
            }
        } else if (target instanceof DataObject) {
            file = (DataObject) target;
        } else {
            // What is it?
            return null;
        }
        
        for (int i = 0; i < rootChildren.length; i++) {
            Node found = PackageView.findPath(rootChildren[i], target);
            //System.err.println("found " + found + " for " + target + " in " + rootChildren[i]);
            if (found != null) {
                return found;
            }
            // For Important Files node:
            if (rootChildren[i].getName().equals(ImportantFilesNodeFactory.IMPORTANT_FILES_NAME)) {
                Node[] ifChildren = rootChildren[i].getChildren().getNodes(true);
                for (int j = 0; j < ifChildren.length; j++) {
                    if (ifChildren[j].getCookie(DataObject.class) == file) {
                        return ifChildren[j];
                    }
                }
            }
        }
        
        return null;
    }
    
    private static final class RootNode extends AbstractNode {
        
        private final NbModuleProject project;
        
        public RootNode(NbModuleProject project) {
            
            // XXX add a NodePathResolver impl to lookup
            super(NodeFactorySupport.createCompositeChildren(project, "Projects/org-netbeans-modules-apisupport-project/Nodes"), 
                  Lookups.fixed(new Object[] {project}));
            this.project = project;
            setIconBaseWithExtension(NbModuleProject.NB_PROJECT_ICON_PATH);
            ProjectInformation pi = ProjectUtils.getInformation(project);
            setDisplayName(pi.getDisplayName());
            setShortDescription(NbBundle.getMessage(ModuleLogicalView.class, "HINT_project_root_node", FileUtil.getFileDisplayName(project.getProjectDirectory())));
            pi.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(final PropertyChangeEvent evt) {
                    ImportantFilesNodeFactory.getNodesSyncRP().post(new Runnable() {

                        public void run() {
                            if (ProjectInformation.PROP_DISPLAY_NAME.equals(evt.getPropertyName())) {
                                RootNode.this.setDisplayName((String) evt.getNewValue());
                            } else if (ProjectInformation.PROP_NAME.equals(evt.getPropertyName())) {
                                RootNode.this.setName((String) evt.getNewValue());
                            }
                        }
                    });
                }
            });
        }
        
        public Action[] getActions(boolean ignore) {
            return ModuleActions.getProjectActions(project);
        }
        
        public boolean canRename() {
            return true;
        }
        
        public String getName() {
            return ProjectUtils.getInformation(project).getDisplayName();
        }
        
        public void setName(String name) {
            DefaultProjectOperations.performDefaultRenameOperation(project, name);
        }
        
    }
}
