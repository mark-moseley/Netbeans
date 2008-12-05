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

package org.netbeans.modules.web.project.ui;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ui.ProjectUISupport;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.FileSystemAction;
import org.openide.actions.FindAction;
import org.openide.actions.PasteAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author mkleint
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-web-project",position=200)
public final class DocBaseNodeFactory implements NodeFactory {
    
    /** Creates a new instance of LibrariesNodeFactory */
    public DocBaseNodeFactory() {
    }

    public NodeList createNodes(Project p) {
        WebProject project = p.getLookup().lookup(WebProject.class);
        assert project != null;
        return new DocBaseNodeList(project);
    }

    private static class DocBaseNodeList implements NodeList<String>, PropertyChangeListener {
        private static final String DOC_BASE = "docBase"; //NOI18N
        private static final String WEB_INF = "webInf"; //NOI18N

        private final WebProject project;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        private final PropertyEvaluator evaluator;
        private final UpdateHelper helper;
        
        private SourceGroup webDocRoot;
        
        DocBaseNodeList(WebProject proj) {
            project = proj;
            evaluator = project.evaluator();
            helper = project.getUpdateHelper();
            Sources s = project.getLookup().lookup(Sources.class);
            assert s != null;
//            assert s.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT).length > 0;

            if(s.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT).length > 0)
            {
                webDocRoot = s.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT)[0];
            }
            else
            {
                String name = ProjectUtils.getInformation( proj ).getDisplayName();

                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(DocBaseNodeList.class, "LBL_No_Source_Groups_Found", name), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
        }
        
        public List<String> keys() {
            FolderHolder nodeFolders = getNodeFolders();
            List<String> result = new ArrayList<String>();
            result.add(DOC_BASE + getFolderPath(nodeFolders.getWebDocBaseDir()));
            if (!nodeFolders.hasCorrectStructure()) {
                result.add(WEB_INF + getFolderPath(nodeFolders.getWebInfDir()));
            }
            return result;
        }

        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        public Node node(String key) {
            FolderHolder nodeFolders = getNodeFolders();
            if (key.startsWith(DOC_BASE)) {
                FileObject webDocBaseDir = nodeFolders.getWebDocBaseDir();
                DataFolder webFolder = getFolder(webDocBaseDir);
                if (webFolder != null) {
                    return new DocBaseNode(webFolder, project, new VisibilityQueryDataFilter(webDocRoot));
                }
                return null;
            } else if (key.startsWith(WEB_INF)) {
                if (nodeFolders.hasCorrectStructure()) {
                    return null;
                }
                FileObject webInfDir = nodeFolders.getWebInfDir();
                DataFolder webInfFolder = getFolder(webInfDir);
                if (webInfFolder != null) {
                    return new WebInfNode(webInfFolder, project, new VisibilityQueryDataFilter(null));
                }
                return null;
            }
            assert false: "No node for key: " + key; // NOI18N
            return null;
        }

        public void addNotify() {
            evaluator.addPropertyChangeListener(this);
        }

        public void removeNotify() {
            evaluator.removePropertyChangeListener(this);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            // The caller holds ProjectManager.mutex() read lock
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    changeSupport.fireChange();
                }
            });
        }
        
        private DataFolder getFolder(FileObject folder) {
            if (folder != null) {
                return DataFolder.findFolder(folder);
            }
            return null;
        }
        
        // # 114402
        private String getFolderPath(FileObject folder) {
            if (folder == null) {
                return "";
            }
            return folder.getPath();
        }
        
        private FileObject getFileObject(String propName) {
            String foName = evaluator.getProperty(propName);
            if (foName == null) {
                return null;
            }
            FileObject fo = helper.getAntProjectHelper().resolveFileObject(foName);
            // when the project is deleted externally, the sources change could
            // trigger a call to thid method before the project directory is 
            // notified about the deletion - invalid FileObject-s could be returned
            return fo != null && fo.isValid() ? fo : null;
        }
        
        private FolderHolder getNodeFolders() {
            return ProjectManager.mutex().readAccess(new Mutex.Action<FolderHolder>() {
                public FolderHolder run() {
                    FileObject webDocBaseDir = getFileObject(WebProjectProperties.WEB_DOCBASE_DIR);
                    FileObject webInf = getFileObject(WebProjectProperties.WEBINF_DIR);
                    
                    return new FolderHolder(webDocBaseDir, webInf);
                }
            });
        }
        
        private static final class FolderHolder {
            private final FileObject webDocBaseDir;
            private final FileObject webInfDir;

            public FolderHolder(FileObject webDocBaseDir, FileObject webInfDir) {
                this.webDocBaseDir = webDocBaseDir;
                this.webInfDir = webInfDir;
            }

            public FileObject getWebDocBaseDir() {
                return webDocBaseDir;
            }

            public FileObject getWebInfDir() {
                return webInfDir;
            }
            
            /**
             * Return <code>true</code> if <tt>WEB-INF<tt> folder
             * is located inside <tt>web</tt> folder.
             * Return <code>false</code> if any of these folders
             * is <code>null</code>.
             */
            public boolean hasCorrectStructure() {
                if (webDocBaseDir == null
                        || webInfDir == null) {
                    return false;
                }
                return FileUtil.isParentOf(webDocBaseDir, webInfDir);
            }
        }
    }
    
    private static final class DocBaseNode extends BaseNode {
        
        DocBaseNode (DataFolder folder, WebProject project, VisibilityQueryDataFilter filter) {
            super(folder, project, filter);
        }
        
        @Override
        public String getDisplayName () {
            return NbBundle.getMessage(DocBaseNodeFactory.class, "LBL_Node_DocBase"); //NOI18N
        }
    }

    private static final class WebInfNode extends BaseNode {
        
        WebInfNode (DataFolder folder, WebProject project, VisibilityQueryDataFilter filter) {
            super (folder, project, filter);
        }
        
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(DocBaseNodeFactory.class, "LBL_Node_WebInf"); //NOI18N
        }
    }

    private static abstract class BaseNode extends FilterNode {
        private static Image WEB_PAGES_BADGE = ImageUtilities.loadImage( "org/netbeans/modules/web/project/ui/resources/webPagesBadge.gif" ); //NOI18N
        /**
         * The MIME type of Java files.
         */
        private static final String JAVA_MIME_TYPE = "text/x-java"; //NO18N
        private Action actions[];
        protected final WebProject project;
        
        BaseNode(final DataFolder folder, WebProject project, VisibilityQueryDataFilter filter) {
            super(folder.getNodeDelegate(), folder.createNodeChildren(filter));
            this.project = project;
        }

        @Override
        public Image getIcon(int type) {        
            return computeIcon(false, type);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return computeIcon(true, type);
        }

        private Node getDataFolderNodeDelegate() {
            return getLookup().lookup(DataFolder.class).getNodeDelegate();
        }

        private Image computeIcon(boolean opened, int type) {
            Image image;

            image = opened ? getDataFolderNodeDelegate().getOpenedIcon(type) : getDataFolderNodeDelegate().getIcon(type);
            image = ImageUtilities.mergeImages(image, WEB_PAGES_BADGE, 7, 7);

            return image;        
        }
        
        @Override
        public boolean canRename() {
            return false;
        }
        
        @Override
        public Action[] getActions(boolean context) {
            if (actions == null) {
                actions = new Action[9];
                actions[0] = CommonProjectActions.newFileAction();
                actions[1] = null;
                actions[2] = SystemAction.get(FindAction.class);
                actions[3] = null;
                actions[4] = SystemAction.get(PasteAction.class);
                actions[5] = null;
                actions[6] = SystemAction.get(FileSystemAction.class);
                actions[7] = null;
                actions[8] = ProjectUISupport.createPreselectPropertiesAction(project, "Sources", null); //NOI18N
            }
            return actions;
        }

        @Override
        public PasteType getDropType(Transferable t, int action, int index) {
            try {
                if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
                    Object data = t.getTransferData(DataFlavor.javaFileListFlavor);
                    if (data != null) {
                        List list = (List) data;
                        for (Object each : list) {
                            FileObject file = FileUtil.toFileObject((File) each);
                            if (file != null && JAVA_MIME_TYPE.equals(file.getMIMEType())) { //NO18N
                                // don't allow java files, see #119968
                                return null;
                            }
                        }
                    }
                }
            } catch (UnsupportedFlavorException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return super.getDropType(t, action, index);
        }
    }

    static final class VisibilityQueryDataFilter implements ChangeListener, ChangeableDataFilter, PropertyChangeListener {
        private static final long serialVersionUID = 1L;
        
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private SourceGroup sourceGroup;
        
        public VisibilityQueryDataFilter(SourceGroup sourceGroup) {
            this.sourceGroup = sourceGroup;
            if (this.sourceGroup != null) {
                this.sourceGroup.addPropertyChangeListener(this);
            }
            VisibilityQuery.getDefault().addChangeListener( this );
        }
                
        public boolean acceptDataObject(DataObject obj) {                
            FileObject fo = obj.getPrimaryFile();                
            boolean show = true;
            try {
                if (sourceGroup != null && !sourceGroup.contains(fo)) {
                    show = false;
                }
            } catch (IllegalArgumentException ex) {
                // sourceGroup is not parent of fo -> do not show file:
                show = false;
            }
            return show && VisibilityQuery.getDefault().isVisible(fo);
        }
        
        public void stateChanged( ChangeEvent e) {            
            changeSupport.fireChange();
        }        
    
        public void addChangeListener( ChangeListener listener ) {
            changeSupport.addChangeListener(listener);
        }        
                        
        public void removeChangeListener( ChangeListener listener ) {
            changeSupport.removeChangeListener(listener);
        }

        public void propertyChange(PropertyChangeEvent arg0) {
            changeSupport.fireChange();
        }
        
    }

}
