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

package org.netbeans.modules.ant.freeform.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.ant.freeform.FreeformProject;
import org.netbeans.modules.ant.freeform.FreeformProjectType;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.ant.freeform.spi.ProjectNature;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PathMatcher;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.actions.OpenAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class FolderNodeFactory implements NodeFactory {
    
    /** Creates a new instance of FolderNodeFactory */
    public FolderNodeFactory() {
    }
    
    public NodeList createNodes(Project p) {
        FreeformProject project = p.getLookup().lookup(FreeformProject.class);
        assert project != null;
        return new RootChildren(project);
    }

    
    static boolean synchronous = false; // for ViewTest
    private static final class RootChildren implements NodeList<Element>, AntProjectListener, PropertyChangeListener {
        
        private final FreeformProject p;
        private List<Element> keys = new ArrayList<Element>();
        private ChangeSupport cs = new ChangeSupport(this);
        
        public RootChildren(FreeformProject p) {
            this.p = p;
        }
        
        public void addNotify() {
            updateKeys(false);
            p.helper().addAntProjectListener(this);
            p.evaluator().addPropertyChangeListener(this);
        }
        
        public void removeNotify() {
            keys = null;
            p.helper().removeAntProjectListener(this);
            p.evaluator().removePropertyChangeListener(this);
        }
        
        private void updateKeys(boolean fromListener) {
            Element genldata = p.getPrimaryConfigurationData();
            Element viewEl = Util.findElement(genldata, "view", FreeformProjectType.NS_GENERAL); // NOI18N
            if (viewEl != null) {
                Element itemsEl = Util.findElement(viewEl, "items", FreeformProjectType.NS_GENERAL); // NOI18N
                keys = Util.findSubElements(itemsEl);
            } else {
                keys = Collections.<Element>emptyList();
            }
            if (fromListener && !synchronous) {
                // #50328, #58491 - post setKeys to different thread to prevent deadlocks
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        cs.fireChange();
                    }
                });
            } else {
                cs.fireChange();
            }
        }
        

        public void configurationXmlChanged(AntProjectEvent ev) {
            updateKeys(true);
        }

        public void propertiesChanged(AntProjectEvent ev) {
            // ignore
        }
        
        public void propertyChange(PropertyChangeEvent ev) {
            updateKeys(true);
        }

        public List<Element> keys() {
            return keys;
        }

        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }
        
        public Node node(Element itemEl) {
            
            Element locationEl = Util.findElement(itemEl, "location", FreeformProjectType.NS_GENERAL); // NOI18N
            String location = Util.findText(locationEl);
            String locationEval = p.evaluator().evaluate(location);
            if (locationEval == null) {
                return null;
            }
            FileObject file = p.helper().resolveFileObject(locationEval);
            if (file == null) {
                // Not there... skip this node.
                return null;
            }
            String label;
            Element labelEl = Util.findElement(itemEl, "label", FreeformProjectType.NS_GENERAL); // NOI18N
            if (labelEl != null) {
                label = Util.findText(labelEl);
            } else {
                label = null;
            }
            if (itemEl.getLocalName().equals("source-folder")) { // NOI18N
                if (!file.isFolder()) {
                    // Just a file. Skip it.
                    return null;
                }
                String includes = null;
                Element includesEl = Util.findElement(itemEl, "includes", FreeformProjectType.NS_GENERAL); // NOI18N
                if (includesEl != null) {
                    includes = p.evaluator().evaluate(Util.findText(includesEl));
                    if (includes.matches("\\$\\{[^}]+\\}")) { // NOI18N
                        // Clearly intended to mean "include everything".
                        includes = null;
                    }
                }
                String excludes = null;
                Element excludesEl = Util.findElement(itemEl, "excludes", FreeformProjectType.NS_GENERAL); // NOI18N
                if (excludesEl != null) {
                    excludes = p.evaluator().evaluate(Util.findText(excludesEl));
                }
                String style = itemEl.getAttribute("style"); // NOI18N
                for (ProjectNature nature : Lookup.getDefault().lookupAll(ProjectNature.class)) {
                    if (nature.getSourceFolderViewStyles().contains(style)) {
                        return nature.createSourceFolderView(p, file, includes, excludes, style, location, label);
                    }
                }
                if (style.equals("subproject")) { // NOI18N
                    try {
                        Project subproject = ProjectManager.getDefault().findProject(file);
                        if (subproject != null) {
                            return new SubprojectNode(subproject, label);
                        }
                    } catch (IOException x) {
                        Exceptions.printStackTrace(x);
                    }
                    return null;
                }
                if (!style.equals("tree")) { // NOI18N
                    Logger.getLogger(FolderNodeFactory.class.getName()).log(Level.WARNING, "Unrecognized <source-folder> style {0} on {1}", new Object[] {style, file});  //NOI18N
                    // ... but show it as a tree anyway (at least ViewTest cares)
                }
                DataObject fileDO;
                try {
                    fileDO = DataObject.find(file);
                } catch (DataObjectNotFoundException e) {
                    throw new AssertionError(e);
                }
                return new ViewItemNode((DataFolder) fileDO, includes, excludes, location, label);
            } else {
                assert itemEl.getLocalName().equals("source-file") : itemEl; // NOI18N
                    DataObject fileDO;
                    try {
                        fileDO = DataObject.find(file);
                    } catch (DataObjectNotFoundException e) {
                        throw new AssertionError(e);
                    }
                return new ViewItemNode(fileDO.getNodeDelegate(), location, label);
            }
        }
    }
    
    
    private static final class GroupDataFilter implements ChangeListener, ChangeableDataFilter, DataFilter.FileBased {
        
        private final ChangeSupport cs = new ChangeSupport(this);
        private final FileObject root;
        private final PathMatcher matcher;
        
        public GroupDataFilter(FileObject root, String includes, String excludes) {
            this.root = root;
            matcher = new PathMatcher(includes, excludes, FileUtil.toFile(root));
            VisibilityQuery.getDefault().addChangeListener( this );
        }
                
        public boolean acceptFileObject(FileObject fo) {
            String path = FileUtil.getRelativePath(root, fo);
            assert path != null : fo + " not in " + root;  //NOI18N
            if (fo.isFolder()) {
                path += "/"; // NOI18N
            }
            if (!matcher.matches(path, true)) {
                return false;
            }
            return VisibilityQuery.getDefault().isVisible( fo );
        }
        
        public void stateChanged( ChangeEvent e) {            
            cs.fireChange();
        }        
    
        public void addChangeListener( ChangeListener listener ) {
            cs.addChangeListener(listener);
        }        
                        
        public void removeChangeListener( ChangeListener listener ) {
            cs.removeChangeListener(listener);
        }

        public boolean acceptDataObject(DataObject d) {
            return acceptFileObject(d.getPrimaryFile());
        }
        
    }
    
     private static final class ViewItemNode extends FilterNode {
        
        private final String name;
        
        private final String displayName;
       
        public ViewItemNode(Node orig, String name, String displayName) {
            super(orig);
            this.name = name;
            this.displayName = displayName;
        }
        
        public ViewItemNode(DataFolder folder, String includes, String excludes, String name, String displayName) {
            super(folder.getNodeDelegate(), folder.createNodeChildren(new GroupDataFilter(folder.getPrimaryFile(), includes, excludes)));
            this.name = name;
            this.displayName = displayName;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public String getDisplayName() {
            if (displayName != null) {
                return displayName;
            } else {
                // #50425: show original name incl. annotations
                return super.getDisplayName();
            }
        }
        
        @Override
        public boolean canRename() {
            return false;
        }
        
        @Override
        public boolean canDestroy() {
            return false;
        }
        
        @Override
        public boolean canCut() {
            return false;
        }
        
    }    

     private static final class SubprojectNode extends AbstractNode { // #97442

         private final Project p;
         private final String label;
         private final ProjectInformation info;

         public SubprojectNode(Project p, String label) {
             super(Children.LEAF, Lookups.singleton(p));
             this.p = p;
             this.label = label;
             info = ProjectUtils.getInformation(p);
         }

         @Override
         public String getName() {
             return info.getName();
         }

         @Override
         public String getDisplayName() {
             return label != null ? label : info.getDisplayName();
         }

         @Override
         public Image getIcon(int type) {
             return ImageUtilities.icon2Image(info.getIcon());
         }

         @Override
         public Image getOpenedIcon(int type) {
             return getIcon(type);
         }

         private static final class OpenProjectAction extends AbstractAction implements ContextAwareAction {
             public void actionPerformed(ActionEvent ev) {
                 assert false;
             }
             public Action createContextAwareInstance(Lookup selection) {
                 final Project[] projects = selection.lookupAll(Project.class).toArray(new Project[0]);
                 return new AbstractAction(SystemAction.get(OpenAction.class).getName()) {
                     public void actionPerformed(ActionEvent ev) {
                         OpenProjects.getDefault().open(projects, false);
                     }
                     @Override
                     public boolean isEnabled() {
                         return !Arrays.asList(OpenProjects.getDefault().getOpenProjects()).containsAll(Arrays.asList(projects));
                     }
                 };
             }
         }
         private static final Action OPEN = new OpenProjectAction();
         @Override
         public Action[] getActions(boolean context) {
             return new Action[] {OPEN};
         }

     }

}
