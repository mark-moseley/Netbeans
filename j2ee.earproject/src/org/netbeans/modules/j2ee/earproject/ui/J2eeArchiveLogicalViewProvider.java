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

package org.netbeans.modules.j2ee.earproject.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.common.project.ui.ProjectProperties;
import org.netbeans.modules.j2ee.common.ui.BrokenServerSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.ui.actions.AddModuleAction;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.j2ee.spi.ejbjar.support.J2eeProjectView;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.actions.FindAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;

/**
 * Support for creating logical views.
 */
public class J2eeArchiveLogicalViewProvider implements LogicalViewProvider {
    
    private final EarProject project;
    protected final UpdateHelper helper;
    private final PropertyEvaluator evaluator;
    protected final ReferenceHelper resolver;
    private final List<? extends Action> specialActions =
            Collections.singletonList(SystemAction.get(AddModuleAction.class));
    private final AntBasedProjectType abpt;
    
    public J2eeArchiveLogicalViewProvider(EarProject project, UpdateHelper helper,
            PropertyEvaluator evaluator, ReferenceHelper resolver,
            AntBasedProjectType abpt) {
        this.project = project;
        assert project != null;
        this.helper = helper;
        assert helper != null;
        this.evaluator = evaluator;
        assert evaluator != null;
        this.resolver = resolver;
        this.abpt = abpt;
    }
    
    public Node createLogicalView() {
        return new ArchiveLogicalViewRootNode();
    }
    
    public Node findPath(Node root, Object target) {
        Project project = root.getLookup().lookup(Project.class);
        if (project == null) {
            return null;
        }
        
        // XXX this is incorrect - must handle case that target instanceof DataObject
        // (since that is likely to be what is in lookup)
        if (target instanceof FileObject) {
            FileObject fo = (FileObject) target;
            Project owner = FileOwnerQuery.getOwner(fo);
            if (!project.equals(owner)) {
                return null; // Don't waste time if project does not own the fo
            }
            // trying to find node in docbase
            Node result = findNodeUnderConfiguration(root, fo);
            if (result != null) {
                return result;
            }
            // trying to find node in sources
            Node[] nodes = root.getChildren().getNodes(true);
            for (int i = nodes.length-1; i >= 0; i--) {
                result = PackageView.findPath(nodes[i], target);
                if (result!=null) {
                    return result;
                }
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
        if (idx != -1) {
            relPath = relPath.substring(0, idx);
        }
        StringTokenizer st = new StringTokenizer(relPath, "/"); //NOI18N
        Node result = root.getChildren().findChild(J2eeProjectView.CONFIG_FILES_VIEW_NAME);
        while (result != null && st.hasMoreTokens()) {
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
        EarProjectProperties.DEBUG_CLASSPATH,
        EarProjectProperties.JAR_CONTENT_ADDITIONAL,
    };
    
    public static boolean hasBrokenLinks(AntProjectHelper helper, ReferenceHelper resolver) {
        return BrokenReferencesSupport.isBroken(helper, resolver, BREAKABLE_PROPERTIES,
                new String[] { EarProjectProperties.JAVA_PLATFORM});
    }
    
    private String getIconBase() {
        IconBaseProvider ibp = project.getLookup().lookup(IconBaseProvider.class);
        return (null == ibp)
                ? "org/netbeans/modules/j2ee/earproject/ui/resources/" // NOI18N
                : ibp.getIconBase();
    }
    
    /** Package private for unit test only. */
    final class ArchiveLogicalViewRootNode extends AbstractNode {
        
        private Action brokenLinksAction;
        private final BrokenServerAction brokenServerAction;
        private boolean broken;
        
        public ArchiveLogicalViewRootNode() {
            super(NodeFactorySupport.createCompositeChildren(project, "Projects/org-netbeans-modules-j2ee-earproject/Nodes"), // NOI18N
                    createLookup(project, helper.getAntProjectHelper()));
            setIconBaseWithExtension(getIconBase() + "projectIcon.gif"); // NOI18N
            super.setName( ProjectUtils.getInformation( project ).getDisplayName() );
            if (hasBrokenLinks(helper.getAntProjectHelper(), resolver)) {
                broken = true;
            }
            brokenServerAction = new BrokenServerAction();
            J2eeModuleProvider moduleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
            moduleProvider.addInstanceListener(WeakListeners.create(InstanceListener.class, brokenServerAction, moduleProvider));
        }
        
        private synchronized void checkProjectValidity() {
            boolean old = broken;
            broken = hasBrokenLinks(helper.getAntProjectHelper(), resolver);
            if (old != broken) {
                getBrokenLinksAction().setEnabled(broken);
                fireIconChange();
                fireOpenedIconChange();
                fireDisplayNameChange(null, null);
            }
        }
        
        public Action getBrokenLinksAction() {
            if (broken && brokenLinksAction == null) {
                brokenLinksAction = new BrokenLinksAction();
            }
            return brokenLinksAction;
        }
        
        public String getHtmlDisplayName() {
            String dispName = super.getDisplayName();
            try {
                dispName = XMLUtil.toElementContent(dispName);
            } catch (CharConversionException ex) {
                // ignore
            }
            return broken || brokenServerAction.isEnabled() ? "<font color=\"#A40000\">" + dispName + "</font>" : null; //NOI18N
        }
        
        @Override
        public String getShortDescription() {
            String prjDirDispName = FileUtil.getFileDisplayName(project.getProjectDirectory());
            return NbBundle.getMessage(J2eeArchiveLogicalViewProvider.class, "HINT_project_root_node", prjDirDispName); // NO18N
        }

        public Action[] getActions( boolean context ) {
            return context ? super.getActions(true) : getAdditionalActions();
        }
        
        public boolean canRename() {
            return true;
        }
        
        public void setName(String s) {
            DefaultProjectOperations.performDefaultRenameOperation(project, s);
        }
        
        public Image getIcon(int type) {
            Image original = super.getIcon( type );
            return broken || brokenServerAction.isEnabled()
            ? Utilities.mergeImages(original, ProjectProperties.ICON_BROKEN_BADGE.getImage(), 8, 0)
            : original;
        }
        
        public Image getOpenedIcon(int type) {
            Image original = super.getOpenedIcon(type);
            return broken || brokenServerAction.isEnabled()
            ? Utilities.mergeImages(original, ProjectProperties.ICON_BROKEN_BADGE.getImage(), 8, 0)
            : original;
        }
        
        public HelpCtx getHelpCtx() {
            return new HelpCtx(ArchiveLogicalViewRootNode.class);
        }
        
        // Private methods -------------------------------------------------
        
        private Action[] getAdditionalActions() {
            
            ResourceBundle bundle = NbBundle.getBundle(J2eeArchiveLogicalViewProvider.class);
            
            J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
            List<Action> actions = new ArrayList<Action>();
            actions.addAll(specialActions);
            actions.add(CommonProjectActions.newFileAction());
            actions.add(null);
            
            boolean cos = Boolean.parseBoolean(project.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(EarProjectProperties.DEPLOY_ON_SAVE));
            actions.add(null);
            if (!cos) {
                actions.add(ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_BUILD, bundle.getString( "LBL_BuildAction_Name" ), null ));
            }
            actions.add(ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_REBUILD, bundle.getString( "LBL_RebuildAction_Name" ), null ));
            if (!cos) {
                actions.add(ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_CLEAN, bundle.getString( "LBL_CleanAction_Name" ), null ));
            }
//            actions.addAll(Arrays.asList(new Action[] {
//                null,
//                ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_BUILD, bundle.getString( "LBL_BuildAction_Name" ), null ), // NOI18N
//                ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_REBUILD, bundle.getString( "LBL_RebuildAction_Name" ), null ), // NOI18N
//                ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_CLEAN, bundle.getString( "LBL_CleanAction_Name" ), null ), // NOI18N
//            }));
            if (provider != null && provider.hasVerifierSupport()) {
                actions.add(ProjectSensitiveActions.projectCommandAction( "verify", bundle.getString( "LBL_VerifyAction_Name" ), null )); // NOI18N
            }
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_RUN, bundle.getString( "LBL_RunAction_Name" ), null )); // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction( EjbProjectConstants.COMMAND_REDEPLOY, bundle.getString( "LBL_DeployAction_Name" ), null));
            actions.add(ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_DEBUG, bundle.getString( "LBL_DebugAction_Name" ), null )); // NOI18N
            actions.addAll(Utilities.actionsForPath("Projects/Profiler_Actions_temporary")); //NOI18N
                
            actions.add(null);
            actions.add(CommonProjectActions.setAsMainProjectAction());
            actions.add(CommonProjectActions.openSubprojectsAction());
            actions.add(CommonProjectActions.closeProjectAction());
            actions.add(null);
            actions.add(CommonProjectActions.renameProjectAction());
            actions.add(CommonProjectActions.moveProjectAction());
            actions.add(CommonProjectActions.copyProjectAction());
            actions.add(CommonProjectActions.deleteProjectAction());
            actions.add(null);
            actions.add(SystemAction.get( FindAction.class ));
            
            actions.addAll(Utilities.actionsForPath("Projects/Actions")); //NOI18N

            actions.add(null);
            
            if (broken) {
                actions.add(getBrokenLinksAction());
            }
            if (brokenServerAction.isEnabled()) {
                actions.add(brokenServerAction);
            }
            actions.add(CommonProjectActions.customizeProjectAction());
            return actions.toArray(new Action[actions.size()]);
        }
        
        /** This action is created only when project has broken references.
         * Once these are resolved the action is disabled.
         */
        private class BrokenLinksAction extends AbstractAction implements PropertyChangeListener, Runnable {
            private static final long serialVersionUID = 1L;
            
            private RequestProcessor.Task task = null;
            private final PropertyChangeListener weakPCL;
            
            public BrokenLinksAction() {
                evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
                putValue(Action.NAME, NbBundle.getMessage(J2eeArchiveLogicalViewProvider.class, "LBL_Fix_Broken_Links_Action"));
                weakPCL = WeakListeners.propertyChange( this, JavaPlatformManager.getDefault() );
                JavaPlatformManager.getDefault().addPropertyChangeListener( weakPCL );
            }
            
            public void actionPerformed(ActionEvent e) {
                BrokenReferencesSupport.showCustomizer(helper.getAntProjectHelper(), resolver, BREAKABLE_PROPERTIES, new String[]{ EarProjectProperties.JAVA_PLATFORM});
                checkProjectValidity();
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
            
            public void run() {
                checkProjectValidity();
            }
            
        }
        
        private class BrokenServerAction extends AbstractAction implements InstanceListener, PropertyChangeListener {
            private static final long serialVersionUID = 1L;
            
            private boolean brokenServer;
            
            public BrokenServerAction() {
                putValue(Action.NAME, NbBundle.getMessage(J2eeArchiveLogicalViewProvider.class, "LBL_Fix_Missing_Server_Action")); // NOI18N
                evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
                checkMissingServer();
            }
            
            public boolean isEnabled() {
                return brokenServer;
            }
            
            public void actionPerformed(ActionEvent e) {
                String j2eeSpec = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).
                        getProperty(EarProjectProperties.J2EE_PLATFORM);
                if (j2eeSpec == null) {
                    j2eeSpec = ProjectProperties.JAVA_EE_5; // NOI18N
                    Logger.getLogger(J2eeArchiveLogicalViewProvider.class.getName()).warning(
                            "project ["+project.getProjectDirectory()+"] is missing "+EarProjectProperties.J2EE_PLATFORM+". " + // NOI18N
                            "default value will be used instead: "+j2eeSpec); // NOI18N
                    updateJ2EESpec(project, project.getAntProjectHelper(), j2eeSpec);
                }
                String instance = BrokenServerSupport.selectServer(j2eeSpec, J2eeModule.EAR);
                if (instance != null) {
                    EarProjectProperties.setServerInstance(
                            project, helper, instance);
                }
                checkMissingServer();
            }
            
            private void updateJ2EESpec(final Project project, final AntProjectHelper helper, final String j2eeSpec) {
                ProjectManager.mutex().postWriteRequest(new Runnable() {
                    public void run() {
                        try {
                            EditableProperties projectProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            projectProps.put(EarProjectProperties.J2EE_PLATFORM, j2eeSpec);
                            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProps);
                            ProjectManager.getDefault().saveProject(project);
                        } catch (IOException e) {
                            Exceptions.printStackTrace(e);
                        }
                    }
                });
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
            private static final long serialVersionUID = 1L;
            
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
                
                Project project = context.lookup( Project.class );
                ActionProvider ap = project.getLookup().lookup( ActionProvider.class);
                
                ap.invokeAction( command, context );
            }
            
            public Action createContextAwareInstance( Lookup lookup ) {
                return new ActionImpl( command, name, lookup );
            }
        }
        
    }
    
}
