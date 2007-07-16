/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.ui;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ConfigurationFilesListener;
import org.netbeans.modules.web.api.webmodule.WebFrameworkSupport;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.project.ProjectWebModule;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.actions.FindAction;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.CookieAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mkleint
 */
public final class ConfFilesNodeFactory implements NodeFactory {
    
    /** Creates a new instance of ConfFilesNodeFactory */
    public ConfFilesNodeFactory() {
    }

    public NodeList createNodes(Project p) {
        WebProject project = (WebProject)p.getLookup().lookup(WebProject.class);
        assert project != null;
        return new ConfFilesNodeList(project);
    }

    private static class ConfFilesNodeList implements NodeList<String>, PropertyChangeListener {
        private static final String CONF_FILES = "confFiles"; //NOI18N

        private final WebProject project;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        ConfFilesNodeList(WebProject proj) {
            project = proj;
            WebLogicalViewProvider logView = (WebLogicalViewProvider) project.getLookup().lookup(WebLogicalViewProvider.class);
            assert logView != null;
        }
        
        public List<String> keys() {
            List<String> result = new ArrayList<String>();
            result.add(CONF_FILES);
            return result;
        }

        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        public Node node(String key) {
            if (key == CONF_FILES) {
                return new ConfFilesNode(project);
            }
            assert false: "No node for key: " + key;
            return null;
        }

        public void addNotify() {
        }

        public void removeNotify() {
        }

        public void propertyChange(PropertyChangeEvent evt) {
            // The caller holds ProjectManager.mutex() read lock
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    changeSupport.fireChange();
                }
            });
        }
        
    }
    
    private static Lookup createLookup(Project project) {
        DataFolder rootFolder = DataFolder.findFolder(project.getProjectDirectory());
        // XXX Remove root folder after FindAction rewrite
        return Lookups.fixed(new Object[] {project, rootFolder});
    }

    private static final class ConfFilesNode extends org.openide.nodes.AbstractNode implements Runnable, FileStatusListener, ChangeListener, PropertyChangeListener {
        private static final Image CONFIGURATION_FILES_BADGE = Utilities.loadImage( "org/netbeans/modules/web/project/ui/resources/config-badge.gif", true ); // NOI18N
        
        private Node projectNode;
        
        // icon badging >>>
        private Set files;
        private Map fileSystemListeners;
        private RequestProcessor.Task task;
        private final Object privateLock = new Object();
        private boolean iconChange;
        private boolean nameChange;        
        private ChangeListener sourcesListener;
        private Map groupsListeners;
	private final Project project;
        // icon badging <<<
        
        private String iconbase = "org/openide/loaders/defaultFolder";
	
        public ConfFilesNode(Project prj) {
            super(ConfFilesChildren.forProject(prj), createLookup(prj));
	    this.project = prj;
            setName("configurationFiles"); // NOI18N
            setIconBase(iconbase);
                        
            FileObject projectDir = prj.getProjectDirectory();
            try {
                DataObject projectDo = DataObject.find(projectDir);
                if (projectDo != null)
                    projectNode = projectDo.getNodeDelegate();
            }
            catch (DataObjectNotFoundException e) {}
        }
        
        public Image getIcon(int type) {
            Image img = computeIcon(false, type);
            return (img != null) ? img: super.getIcon(type);
        }
        
        public Image getOpenedIcon(int type) {
            Image img = computeIcon(true, type);
            return (img != null) ? img: super.getIcon(type);
        }
        
        private Image computeIcon(boolean opened, int type) {
            if (projectNode == null)
                return null;
            Image image = opened ? icon2image("Tree.openIcon"):icon2image("Tree.closedIcon"); //NOI18N
            if(null==image)
                image= opened ? super.getOpenedIcon(type):super.getIcon(type);
            image = Utilities.mergeImages(image, CONFIGURATION_FILES_BADGE, 7, 7);
            return image;
        }
        
        private static Image icon2image(String key) {
        Object obj = UIManager.get(key);
        if (obj instanceof Image) {
            return (Image)obj;
        }
        
        if (obj instanceof Icon) {
            Icon icon = (Icon)obj;
            return Utilities.icon2Image(icon);
        }
        
        return null;
    }  
         
        public String getDisplayName() {
            return NbBundle.getMessage(ConfFilesNodeFactory.class, "LBL_Node_Config"); //NOI18N
        }
        
        public javax.swing.Action[] getActions(boolean context) {
            return new javax.swing.Action[] {
                SystemAction.get(FindAction.class),
            };
        }

	public void run() {
            boolean fireIcon;
            boolean fireName;
            synchronized (privateLock) {
                fireIcon = iconChange;
                fireName = nameChange;
                iconChange = false;
                nameChange = false;
            }
            if (fireIcon) {
                fireIconChange();
                fireOpenedIconChange();
            }
            if (fireName) {
                fireDisplayNameChange(null, null);
            }
	}

	public void annotationChanged(FileStatusEvent event) {
            if (task == null) {
                task = RequestProcessor.getDefault().create(this);
            }

            synchronized (privateLock) {
                if ((!iconChange && event.isIconChange())  || (!nameChange && event.isNameChange())) {
                    Iterator it = files.iterator();
                    while (it.hasNext()) {
                        FileObject fo = (FileObject) it.next();
                        if (event.hasChanged(fo)) {
                            iconChange |= event.isIconChange();
                            nameChange |= event.isNameChange();
                        }
                    }
                }
            }

            task.schedule(50);  // batch by 50 ms
	}

	public void stateChanged(ChangeEvent e) {
            setProjectFiles(project);
	}

	public void propertyChange(PropertyChangeEvent evt) {
            setProjectFiles(project);
	}
	
        protected void setProjectFiles(Project project) {
            Sources sources = ProjectUtils.getSources(project);  // returns singleton
            if (sourcesListener == null) {                
                sourcesListener = WeakListeners.change(this, sources);
                sources.addChangeListener(sourcesListener);                                
            }
            setGroups(Arrays.asList(sources.getSourceGroups(Sources.TYPE_GENERIC)));
        }

        private void setGroups(Collection groups) {
            if (groupsListeners != null) {
                Iterator it = groupsListeners.keySet().iterator();
                while (it.hasNext()) {
                    SourceGroup group = (SourceGroup) it.next();
                    PropertyChangeListener pcl = (PropertyChangeListener) groupsListeners.get(group);
                    group.removePropertyChangeListener(pcl);
                }
            }
            groupsListeners = new HashMap();
            Set roots = new HashSet();
            Iterator it = groups.iterator();
            while (it.hasNext()) {
                SourceGroup group = (SourceGroup) it.next();
                PropertyChangeListener pcl = WeakListeners.propertyChange(this, group);
                groupsListeners.put(group, pcl);
                group.addPropertyChangeListener(pcl);
                FileObject fo = group.getRootFolder();
                roots.add(fo);
            }
            setFiles(roots);
        }

        protected void setFiles(Set files) {
            if (fileSystemListeners != null) {
                Iterator it = fileSystemListeners.keySet().iterator();
                while (it.hasNext()) {
                    FileSystem fs = (FileSystem) it.next();
                    FileStatusListener fsl = (FileStatusListener) fileSystemListeners.get(fs);
                    fs.removeFileStatusListener(fsl);
                }
            }
                        
            fileSystemListeners = new HashMap();
            this.files = files;
            if (files == null) return;

            Iterator it = files.iterator();
            Set hookedFileSystems = new HashSet();
            while (it.hasNext()) {
                FileObject fo = (FileObject) it.next();
                try {
                    FileSystem fs = fo.getFileSystem();
                    if (hookedFileSystems.contains(fs)) {
                        continue;
                    }
                    hookedFileSystems.add(fs);
                    FileStatusListener fsl = FileUtil.weakFileStatusListener(this, fs);
                    fs.addFileStatusListener(fsl);
                    fileSystemListeners.put(fs, fsl);
                } catch (FileStateInvalidException e) {
                    Exceptions.printStackTrace(Exceptions.attachMessage(e, "Can not get " + fo + " filesystem, ignoring..."));  // NO18N
                }
            }
        }
    }        

    private static final class ConfFilesChildren extends Children.Keys {
        
        private final static String[] wellKnownFiles = { 
            "web.xml", 
            "webservices.xml", 
            "struts-config.xml", 
            "faces-config.xml",
            "portlet.xml",
             // Creator-specific JSF config files
            "navigator.xml", 
            "managed-beans.xml" 
        }; //NOI18N
        
        private final ProjectWebModule pwm;
        private final HashSet keys;
        private final java.util.Comparator comparator = new NodeComparator();
        
        private final FileChangeListener webInfListener = new FileChangeAdapter() {
            public void fileDataCreated(FileEvent fe) {
                if (isWellKnownFile(fe.getFile().getNameExt()))
                    addKey(fe.getFile());
            }

            public void fileRenamed(FileRenameEvent fe) {
                // if the old file name was in keys, the new file name 
                // is now there (since it's the same FileObject)
                if (keys.contains(fe.getFile())) {
                    // so we need to remove it if it's not well-known
                    if (!isWellKnownFile(fe.getFile().getNameExt()))
                        removeKey(fe.getFile());
                    else 
                        // this causes resorting of the keys
                        doSetKeys();
                } else {
                    // the key is not contained, so add it if it's well-known
                    if (isWellKnownFile(fe.getFile().getNameExt()))
                        addKey(fe.getFile());
                }
            }
            
            public void fileDeleted(FileEvent fe) {
                if (isWellKnownFile(fe.getFile().getNameExt())) {
                    removeKey(fe.getFile());
                }
            }
        };
        
        private final FileChangeListener anyFileListener = new FileChangeAdapter() {
            public void fileDataCreated(FileEvent fe) {
                addKey(fe.getFile());
            }

            public void fileFolderCreated(FileEvent fe) {
                addKey(fe.getFile());
            }

            public void fileRenamed(FileRenameEvent fe) {
                addKey(fe.getFile());
            }

            public void fileDeleted(FileEvent fe) {
                removeKey(fe.getFile());
            }
        };
        
        private final ConfigurationFilesListener serverSpecificFilesListener = new ConfigurationFilesListener() {
            public void fileCreated(FileObject fo) {
                addKey(fo);
            }
            
            public void fileDeleted(FileObject fo) {
                removeKey(fo);
            }
        };
        
        private ConfFilesChildren(ProjectWebModule pwm) {
            this.pwm = pwm;
            keys = new HashSet();
        }
        
        public static Children forProject(Project project) {
            ProjectWebModule pwm = (ProjectWebModule)project.getLookup().lookup(ProjectWebModule.class);
            return new ConfFilesChildren(pwm);
        }
                
        protected void addNotify() {
            createKeys();
            doSetKeys();
        }
        
        protected void removeNotify() {
            removeListeners();
        }
        
        public Node[] createNodes(Object key) {
            Node n = null;
            
            if (keys.contains(key)) {
                FileObject fo = (FileObject)key;
                try {
                    DataObject dataObject = DataObject.find(fo);
                    n = dataObject.getNodeDelegate().cloneNode();
                }
                catch (DataObjectNotFoundException dnfe) {}
            }
                                    
            return (n == null) ? new Node[0] : new Node[] { n };            
        }
        
        public synchronized void refreshNodes() {
            addNotify();
        }
        
        private synchronized void addKey(FileObject key) {
            if (VisibilityQuery.getDefault().isVisible(key)) {
                //System.out.println("Adding " + key.getPath());
                keys.add(key);
                doSetKeys();
            }
        }
        
        private synchronized void removeKey(FileObject key) {
            //System.out.println("Removing " + key.getPath());
            keys.remove(key);
            doSetKeys();
        }
        
        private synchronized void createKeys() {
            keys.clear();
            
            addWellKnownFiles();
            addConfDirectoryFiles();
            addServerSpecificFiles();
            addFrameworkFiles();
        }
        
        private void doSetKeys() {
            final Object[] result = keys.toArray();
            java.util.Arrays.sort(result, comparator);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setKeys(result);
                }
            });
        }
                
        private void addWellKnownFiles() {            
            FileObject webInf = pwm.getWebInf(true);
            if (webInf == null)
                return;
            
            for (int i = 0; i < wellKnownFiles.length; i++) {
                FileObject fo = webInf.getFileObject(wellKnownFiles[i]);
                if (fo != null)
                    keys.add(fo);
            }
            
            webInf.addFileChangeListener(webInfListener);
        }
        
        private void addConfDirectoryFiles() {
            FileObject conf = pwm.getConfDir();
            if (conf == null)
                return;
            
            FileObject[] children = conf.getChildren();
            for (int i = 0; i < children.length; i++) {
                if (VisibilityQuery.getDefault().isVisible(children[i]))
                    keys.add(children[i]);
            }
            
            conf.addFileChangeListener(anyFileListener);
        }
        
        private void addServerSpecificFiles() {
            FileObject[] files = pwm.getConfigurationFiles();
            
            for (int i = 0; i < files.length; i++) {
                keys.add(files[i]);
            }
            
            pwm.addConfigurationFilesListener(serverSpecificFilesListener);
        }
        
        private void addFrameworkFiles(){
            List providers = WebFrameworkSupport.getFrameworkProviders();
            for (int i = 0; i < providers.size(); i++){
                WebFrameworkProvider provider = (WebFrameworkProvider)providers.get(i);
                FileObject wmBase = pwm.getDocumentBase();
                File files[] = null;
                if (wmBase != null) {
                    files = provider.getConfigurationFiles(WebModule.getWebModule(wmBase));
                }
                if (files != null){
                    for (int j = 0; j < files.length; j++){
                        FileObject fo = FileUtil.toFileObject(files[j]);
                        if (fo != null){
                            keys.add(fo);
                            // XXX - do we need listeners on these files?
                            //fo.addFileChangeListener(anyFileListener);
                        }
                    }
                }
            }
        }
        
        private void removeListeners() {
            pwm.removeConfigurationFilesListener(serverSpecificFilesListener);
            
            FileObject webInf = pwm.getWebInf(true);
            if (webInf != null)
                pwm.getWebInf().removeFileChangeListener(webInfListener);
            
            FileObject conf = pwm.getConfDir();
            if (conf != null)
                conf.removeFileChangeListener(anyFileListener);
        }
        
        private boolean isWellKnownFile(String name) {
            for (int i = 0; i < wellKnownFiles.length; i++) 
                if (name.equals(wellKnownFiles[i]))
                    return true;
            
            return false;
        }
                        
        private static final class NodeComparator implements java.util.Comparator {
            public int compare(Object o1, Object o2) {
                FileObject fo1 = (FileObject)o1;
                FileObject fo2 = (FileObject)o2;
                
                int result = compareType(fo1, fo2);
                if (result == 0)
                    result = compareNames(fo1, fo2);
                if (result == 0)
                    return fo1.getPath().compareTo(fo2.getPath());
                
                return result;
            }
            
            private int compareType(FileObject fo1, FileObject fo2) {
                int folder1 = fo1.isFolder() ? 0 : 1;
                int folder2 = fo2.isFolder() ? 0 : 1;
                
                return folder1 - folder2;
            }
            
            private int compareNames(FileObject do1, FileObject do2) {
                return do1.getNameExt().compareTo(do2.getNameExt());
            }
            
            public boolean equals(Object o) {
                return (o instanceof NodeComparator);
            }
        }
    }
    
    private static class ConfFilesRefreshAction extends CookieAction {
        
        protected Class[] cookieClasses() {
            return new Class[] { RefreshCookie.class };
        }
        
        protected boolean enable(Node[] activatedNodes) {
            return true;
        }
        
        protected int mode() {
            return CookieAction.MODE_EXACTLY_ONE;
        }
        
        protected boolean asynchronous() {
            return false;
        }
        
        public String getName() {
            return NbBundle.getMessage(ConfFilesNodeFactory.class, "LBL_Refresh"); //NOI18N
        }
        
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }
        
        public void performAction(Node[] selectedNodes) {
            for (int i = 0; i < selectedNodes.length; i++) {
                RefreshCookie cookie = (RefreshCookie)selectedNodes[i].getCookie(RefreshCookie.class);
                cookie.refresh();
            }
        }
        
        private interface RefreshCookie extends Node.Cookie {
            public void refresh();
        }
    }

}
