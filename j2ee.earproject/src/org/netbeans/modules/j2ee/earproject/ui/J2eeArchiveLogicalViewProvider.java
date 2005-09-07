/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.earproject.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JSeparator;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;

import org.netbeans.modules.j2ee.earproject.UpdateHelper;
import org.openide.loaders.DataFolder;
import org.openide.util.lookup.Lookups;

import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.FolderLookup;
import org.openide.xml.XMLUtil;

/**
 * Support for creating logical views.
 * @author Petr Hrebejk
 */
public class J2eeArchiveLogicalViewProvider implements LogicalViewProvider {
    
    private final Project project;
    protected final UpdateHelper helper;
    private final PropertyEvaluator evaluator;
    private final SubprojectProvider spp;
    protected final ReferenceHelper resolver;
    private java.util.List specialActions;
    private AntBasedProjectType abpt;
    
    
    public J2eeArchiveLogicalViewProvider(Project project, UpdateHelper helper, PropertyEvaluator evaluator, SubprojectProvider spp, ReferenceHelper resolver, java.util.List specialActions, AntBasedProjectType abpt) {
        this.project = project;
        assert project != null;
        this.helper = helper;
        assert helper != null;
        this.evaluator = evaluator;
        assert evaluator != null;
        this.spp = spp;
        assert spp != null;
        this.resolver = resolver;
        this.specialActions = specialActions;
        this.abpt = abpt;
    }
    
    public Node createLogicalView() {
        return new ArchiveLogicalViewRootNode();
    }
    
    public Node findPath(Node root, Object target) {
        Project project = (Project) root.getLookup().lookup(Project.class);
        if (project == null)
            return null;
        
        if (target instanceof FileObject) {
            FileObject fo = (FileObject) target;
            Project owner = FileOwnerQuery.getOwner(fo);
            if (!project.equals(owner))
                return null; // Don't waste time if project does not own the fo
            // trying to find node in docbase
            Node result = findNodeUnderConfiguration(root, fo);
            if (result!=null) return result;
            // trying to find node in sources
            Node[] nodes = root.getChildren().getNodes(true);
            for (int i = nodes.length-1; i >= 0; i--) {
                result = PackageView.findPath(nodes[i], target);
                if (result!=null) return result;
            }
            
        }
        return null;
    }
    
    private Node findNodeUnderConfiguration(Node root, FileObject fo) {
        FileObject rootfo = helper.getAntProjectHelper().resolveFileObject(evaluator.getProperty(EarProjectProperties.META_INF));
        String relPath = FileUtil.getRelativePath(rootfo, fo);
        if (relPath == null) {
            return null;
        }
        int idx = relPath.indexOf('.'); //NOI18N
        if (idx != -1)
            relPath = relPath.substring(0, idx);
        StringTokenizer st = new StringTokenizer(relPath, "/"); //NOI18N
        Node result = NodeOp.findChild(root,rootfo.getName());
        while (st.hasMoreTokens()) {
            result = NodeOp.findChild(result, st.nextToken());
        }
        
        return result;
    }
    
    private static Lookup createLookup( Project project, AntProjectHelper c ) {
        DataFolder rootFolder = DataFolder.findFolder( project.getProjectDirectory() );
        // XXX Remove root folder after FindAction rewrite
        Lookup ret = null;
        if (null == c) {
            ret = Lookups.fixed( new Object[] { project, rootFolder });
        } else {
            ret = Lookups.fixed( new Object[] { project, rootFolder, c } );
        }
        return ret;
    }
    
    // Private innerclasses ----------------------------------------------------
    
    private static final String[] BREAKABLE_PROPERTIES = new String[] {
        EarProjectProperties.JAVAC_CLASSPATH,
                EarProjectProperties.DEBUG_CLASSPATH,
                EarProjectProperties.JAR_CONTENT_ADDITIONAL,
                EarProjectProperties.SRC_DIR,
    };
    
    public static boolean hasBrokenLinks(AntProjectHelper helper, ReferenceHelper resolver) {
        return BrokenReferencesSupport.isBroken(helper, resolver, BREAKABLE_PROPERTIES,
                new String[] { EarProjectProperties.JAVA_PLATFORM});
    }
    
    
    private String getIconBase() {
        IconBaseProvider ibp =
                (IconBaseProvider) project.getLookup().lookup(IconBaseProvider.class);
        if (null == ibp) {
            
            return "org/netbeans/modules/j2ee/earproject/ui/resources/defaultProjectIcon";
        }
        return ibp.getIconBase();
    }
    
    /** Filter node containin additional features for the J2SE physical
     */
    private final class ArchiveLogicalViewRootNode extends AbstractNode  implements Runnable, FileStatusListener, ChangeListener, PropertyChangeListener {
        
        private Image icon;
        private Action brokenLinksAction;
        private BrokenServerAction brokenServerAction;
        private boolean broken;
        private static final String BROKEN_PROJECT_BADGE = "org/netbeans/modules/j2ee/earproject/ui/resources/brokenProjectBadge.gif"; // NOI18N
        
        // icon badging >>>
        private Set files;
        private Map fileSystemListeners;
        private RequestProcessor.Task task;
        private final Object privateLock = new Object();
        private boolean iconChange;
        private boolean nameChange;
        private ChangeListener sourcesListener;
        private Map groupsListeners;
        // icon badging <<<
        
        public ArchiveLogicalViewRootNode() {
            super( new ArchiveViews.LogicalViewChildren( project, helper.getAntProjectHelper(), evaluator ), createLookup( project, helper.getAntProjectHelper() ) );
            setIconBase( getIconBase() + "projectIcon" ); // NOI18N
            super.setName( ProjectUtils.getInformation( project ).getDisplayName() );
            if (hasBrokenLinks(helper.getAntProjectHelper(), resolver)) {
                broken = true;
                brokenLinksAction = new BrokenLinksAction();
            }
            brokenServerAction = new BrokenServerAction();
            J2eeModuleProvider moduleProvider = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
            moduleProvider.addInstanceListener((InstanceListener)WeakListeners.create(
                    InstanceListener.class, brokenServerAction, moduleProvider));
            setProjectFiles(project);
        }
        
        protected final void setProjectFiles(Project project) {
            Sources sources = ProjectUtils.getSources(project);  // returns singleton
            if (sourcesListener == null) {
                sourcesListener = WeakListeners.change(this, sources);
                sources.addChangeListener(sourcesListener);
            }
            setGroups(Arrays.asList(sources.getSourceGroups(Sources.TYPE_GENERIC)));
        }
        
        
        private final void setGroups(Collection groups) {
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
        
        protected final void setFiles(Set files) {
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
                    ErrorManager err = ErrorManager.getDefault();
                    err.annotate(e, "Can not get " + fo + " filesystem, ignoring...");  // NO18N
                    err.notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }
        
        public String getDisplayName() {
            String s = super.getDisplayName();
            
            if (files != null && files.iterator().hasNext()) {
                try {
                    FileObject fo = (FileObject) files.iterator().next();
                    s = fo.getFileSystem().getStatus().annotateName(s, files);
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            
            return s;
        }
        
        public String getHtmlDisplayName() {
            if (files != null && files.iterator().hasNext()) {
                try {
                    FileObject fo = (FileObject) files.iterator().next();
                    FileSystem.Status stat = fo.getFileSystem().getStatus();
                    if (stat instanceof FileSystem.HtmlStatus) {
                        FileSystem.HtmlStatus hstat = (FileSystem.HtmlStatus) stat;
                        
                        String result = hstat.annotateNameHtml(
                                getMyHtmlDisplayName(), files);
                        
                        //Make sure the super string was really modified
                        if (result != null && !result.equals(getMyHtmlDisplayName())) {
                            return result;
                        }
                    }
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            return getMyHtmlDisplayName();
        }
        
        public java.awt.Image getIcon(int type) {
            java.awt.Image img = getMyIcon(type);
            
            if (files != null && files.iterator().hasNext()) {
                try {
                    FileObject fo = (FileObject) files.iterator().next();
                    img = fo.getFileSystem().getStatus().annotateIcon(img, type, files);
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            
            return img;
        }
        
        public java.awt.Image getOpenedIcon(int type) {
            java.awt.Image img = getMyOpenedIcon(type);
            
            if (files != null && files.iterator().hasNext()) {
                try {
                    FileObject fo = (FileObject) files.iterator().next();
                    img = fo.getFileSystem().getStatus().annotateIcon(img, type, files);
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            
            return img;
        }
        
        
        public Action[] getActions( boolean context ) {
            if ( context )
                return super.getActions( true );
            else
                return getAdditionalActions();
        }
        
        public boolean canRename() {
            return true;
        }
        
        public void setName(String s) {
            DefaultProjectOperations.performDefaultRenameOperation(project, s);
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
                if ((iconChange == false && event.isIconChange())  || (nameChange == false && event.isNameChange())) {
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
        
        // sources change
        public void stateChanged(ChangeEvent e) {
            setProjectFiles(project);
        }
        
        // group change
        public void propertyChange(PropertyChangeEvent evt) {
            setProjectFiles(project);
        }
        
        
        public Image getMyIcon(int type) {
            Image original = super.getIcon( type );
            return broken || brokenServerAction.isEnabled()
            ? Utilities.mergeImages(original, Utilities.loadImage(BROKEN_PROJECT_BADGE), 8, 0)
            : original;
        }
        
        public Image getMyOpenedIcon(int type) {
            Image original = super.getOpenedIcon(type);
            return broken || brokenServerAction.isEnabled()
            ? Utilities.mergeImages(original, Utilities.loadImage(BROKEN_PROJECT_BADGE), 8, 0)
            : original;
        }
        
        public String getMyHtmlDisplayName() {
            String dispName = super.getDisplayName();
            try {
                dispName = XMLUtil.toElementContent(dispName);
            } catch (CharConversionException ex) {
                // ignore
            }
            return broken || brokenServerAction.isEnabled() ? "<font color=\"#A40000\">" + dispName + "</font>" : null; //NOI18N
        }
        
        // Private methods -------------------------------------------------
        
        private Action[] getAdditionalActions() {
            
            ResourceBundle bundle = NbBundle.getBundle(J2eeArchiveLogicalViewProvider.class);
            
            J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
            ArrayList actions = new ArrayList();
            actions.addAll(specialActions);
            actions.addAll(java.util.Arrays.asList((Object[])new Action[] {
                null,
                        ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_BUILD, bundle.getString( "LBL_BuildAction_Name" ), null ), // NOI18N
                        ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_REBUILD, bundle.getString( "LBL_RebuildAction_Name" ), null ), // NOI18N
                        ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_CLEAN, bundle.getString( "LBL_CleanAction_Name" ), null ), // NOI18N
            }));
            if (provider != null && provider.hasVerifierSupport()) {
                actions.add(ProjectSensitiveActions.projectCommandAction( "verify", bundle.getString( "LBL_VerifyAction_Name" ), null )); // NOI18N
            }
            actions.addAll(java.util.Arrays.asList((Object[])new Action[] {
                null,
                        ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_RUN, bundle.getString( "LBL_RunAction_Name" ), null ), // NOI18N
                        ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_DEBUG, bundle.getString( "LBL_DebugAction_Name" ), null ), // NOI18N
                        ProjectSensitiveActions.projectCommandAction( EjbProjectConstants.COMMAND_REDEPLOY, bundle.getString( "LBL_DeployAction_Name" ), null ), // NOI18N
                        null,
                        CommonProjectActions.setAsMainProjectAction(),
                        CommonProjectActions.openSubprojectsAction(),
                        CommonProjectActions.closeProjectAction(),
                        null,
                        CommonProjectActions.renameProjectAction(),
                        CommonProjectActions.moveProjectAction(),
                        CommonProjectActions.copyProjectAction(),
                        CommonProjectActions.deleteProjectAction(),
                        null,
                        SystemAction.get( org.openide.actions.FindAction.class ),
                        null,
            }));
            
           try {
                Repository repository  = Repository.getDefault();
                FileSystem sfs = repository.getDefaultFileSystem();
                FileObject fo = sfs.findResource("Projects/Actions");  // NOI18N
                if (fo != null) {
                    DataObject dobj = DataObject.find(fo);
                    FolderLookup actionRegistry = new FolderLookup((DataFolder)dobj);
                    Lookup.Template query = new Lookup.Template(Object.class);
                    Lookup lookup = actionRegistry.getLookup();
                    Iterator it = lookup.lookup(query).allInstances().iterator();
                    if (it.hasNext()) {
                        actions.add(null);
                    }
                    while (it.hasNext()) {
                        Object next = it.next();
                        if (next instanceof Action) {
                            actions.add(next);
                        } else if (next instanceof JSeparator) {
                            actions.add(null);
                        }
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                // data folder for existing fileobject expected
                ErrorManager.getDefault().notify(ex);
            }
            
            actions.add(null);
            
            if (brokenLinksAction != null) {
                actions.add(brokenLinksAction);
            }
            if (brokenServerAction.isEnabled()) {
                actions.add(brokenServerAction);
            }
            actions.add(CommonProjectActions.customizeProjectAction());
            return (Action[])actions.toArray(new Action[actions.size()]);
        }
        
        /** This action is created only when project has broken references.
         * Once these are resolved the action is disabled.
         */
        private class BrokenLinksAction extends AbstractAction implements PropertyChangeListener, Runnable {
            
            private RequestProcessor.Task task = null;
            
            private PropertyChangeListener weakPCL;
            
            public BrokenLinksAction() {
                evaluator.addPropertyChangeListener(this);
                putValue(Action.NAME, NbBundle.getMessage(J2eeArchiveLogicalViewProvider.class, "LBL_Fix_Broken_Links_Action"));
                weakPCL = WeakListeners.propertyChange( this, JavaPlatformManager.getDefault() );
                JavaPlatformManager.getDefault().addPropertyChangeListener( weakPCL );
            }
            
            public void actionPerformed(ActionEvent e) {
                BrokenReferencesSupport.showCustomizer(helper.getAntProjectHelper(), resolver, BREAKABLE_PROPERTIES, new String[]{ EarProjectProperties.JAVA_PLATFORM});
                run();
            }
            
            public void propertyChange(PropertyChangeEvent evt) {
                // check project state whenever there was a property change
                // or change in list of platforms.
                // Coalesce changes since they can come quickly:
                if (task == null) {
                    task = RequestProcessor.getDefault().create(this);
                }
                task.schedule(100);
            }
            
            public synchronized void run() {
                boolean old = broken;
                broken = hasBrokenLinks(helper.getAntProjectHelper(), resolver);
                if (old != broken) {
                    setEnabled(broken);
                    fireIconChange();
                    fireOpenedIconChange();
                    fireDisplayNameChange(null, null);
                    ((EarProject)project).getProjectProperties().store();
                }
            }
            
        }
        
        private class BrokenServerAction extends AbstractAction implements
                InstanceListener, PropertyChangeListener {
            
            private RequestProcessor.Task task = null;
            private boolean brokenServer;
            
            public BrokenServerAction() {
                putValue(Action.NAME, NbBundle.getMessage(J2eeArchiveLogicalViewProvider.class, "LBL_Fix_Missing_Server_Action")); // NOI18N
                evaluator.addPropertyChangeListener(this);
                checkMissingServer();
            }
            
            public boolean isEnabled() {
                return brokenServer;
            }
            
            public void actionPerformed(ActionEvent e) {
                EarProjectProperties app = new EarProjectProperties(project, helper.getAntProjectHelper(), resolver, abpt);
                BrokenServerSupport.showCustomizer(app);
                checkMissingServer();
            }
            
            public void propertyChange(PropertyChangeEvent evt) {
                if (EarProjectProperties.J2EE_SERVER_INSTANCE.equals(evt.getPropertyName())) {
                    checkMissingServer();
                }
            }
            
            public void changeDefaultInstance(String oldServerInstanceID, String newServerInstanceID) {
            }
            
            public void instanceAdded(String serverInstanceID) {
                checkMissingServer();
            }
            
            public void instanceRemoved(String serverInstanceID) {
                checkMissingServer();
            }
            
            private void checkMissingServer() {
                boolean old = brokenServer;
                String serverInstanceID = helper.getAntProjectHelper().getStandardPropertyEvaluator().getProperty(EarProjectProperties.J2EE_SERVER_INSTANCE);
                brokenServer = BrokenServerSupport.isBroken(serverInstanceID);
                if (old != brokenServer) {
                    fireIconChange();
                    fireOpenedIconChange();
                    fireDisplayNameChange(null, null);
                }
            }
        }
        
    }
    
    /** Factory for project actions.<BR>
     * XXX This class is a candidate for move to org.netbeans.spi.project.ui.support
     */
    public static class Actions {
        
        private Actions() {} // This is a factory
        
        public static Action createAction( String key, String name, boolean global ) {
            return new ActionImpl( key, name, global ? Utilities.actionsGlobalContext() : null );
        }
        
        private static class ActionImpl extends AbstractAction implements ContextAwareAction {
            
            Lookup context;
            String name;
            String command;
            
            public ActionImpl( String command, String name, Lookup context ) {
                super( name );
                this.context = context;
                this.command = command;
                this.name = name;
            }
            
            public void actionPerformed( ActionEvent e ) {
                
                Project project = (Project)context.lookup( Project.class );
                ActionProvider ap = (ActionProvider)project.getLookup().lookup( ActionProvider.class);
                
                ap.invokeAction( command, context );
                
            }
            
            public Action createContextAwareInstance( Lookup lookup ) {
                return new ActionImpl( command, name, lookup );
            }
        }
        
    }
    
}
