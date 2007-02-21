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

package org.netbeans.modules.bpel.project.ui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;

import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
// import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.modules.bpel.project.IcanproConstants;
import org.netbeans.modules.bpel.project.ProjectConstants;
import org.netbeans.modules.bpel.project.ui.customizer.IcanproProjectProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Support for creating logical views.
 * @author Petr Hrebejk
 */
public class IcanproLogicalViewProvider implements LogicalViewProvider {

    private final Project project;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SubprojectProvider spp;
    private final ReferenceHelper resolver;


    public IcanproLogicalViewProvider(Project project, AntProjectHelper helper, PropertyEvaluator evaluator, SubprojectProvider spp, ReferenceHelper resolver) {
        this.project = project;
        assert project != null;
        this.helper = helper;
        assert helper != null;
        this.evaluator = evaluator;
        assert evaluator != null;
        this.spp = spp;
        assert spp != null;
        this.resolver = resolver;
    }

    public Node createLogicalView() {
        return new IcanLogicalViewRootNode();
    }

    /**
     * {@inheritDoc}
     * Fix for #83576.
     * @author ads
     */
    public Node findPath( Node root, Object target ) {
        // Check each child node in turn.
        Node[] children = root.getChildren().getNodes(true);
        for (Node node : children) {
            if (target instanceof DataObject || target instanceof FileObject) {
                DataObject d = (DataObject) node.getLookup().
                    lookup(DataObject.class);
                if (d == null) {
                    continue;
                }
                // Copied from org.netbeans.spi.java.project.support.ui.TreeRootNode.PathFinder.findPath:
                FileObject kidFO = d.getPrimaryFile();
                FileObject targetFO = target instanceof DataObject ?
                        ((DataObject) target).getPrimaryFile() :
                            (FileObject) target;
                if (kidFO == targetFO) {
                    return node;
                } else if (FileUtil.isParentOf(kidFO, targetFO)) {
                    String relPath = FileUtil.getRelativePath(kidFO, targetFO);
                    List/*<String>*/ path = Collections.list(
                            new StringTokenizer(relPath, "/")); // NOI18N
                    // XXX see original code for justification
                    path.set(path.size() - 1, targetFO.getName());
                    try {
                        Node found =  NodeOp.findPath( node,
                                Collections.enumeration(path));

                        // The code below is fix for #84948. 
                        if ( hasObject( found , target ) ){
                            return found;
                        }
                        Node parent = found.getParentNode();
                        Children kids = parent.getChildren();
                        children = kids.getNodes();
                        for (Node child : children) {
                            if ( hasObject( child, target )){
                                return child;
                            }
                        }
                        
                    } catch (NodeNotFoundException e) {
                        return null;
                    }
                }
            }
        }
        return null;

    }
    
    private boolean hasObject( Node node, Object obj ){
        if ( obj == null ){
            return false;
        }
        DataObject dataObject = (DataObject)node.getLookup().lookup( 
                DataObject.class );
        if ( dataObject == null ) {
            return false;
        }
        if (obj instanceof DataObject) {
            if ( dataObject.equals( obj ) ){
                return true;
            }
            FileObject fileObject = ((DataObject)obj).getPrimaryFile();
            return hasObject( node, fileObject );
        }
        else if ( obj instanceof FileObject) {
            FileObject fileObject = dataObject.getPrimaryFile();
            return obj.equals( fileObject );
        }
        else {
            return false;
        }
    }
   private static Lookup createLookup( Project project ) {
        DataFolder rootFolder = DataFolder.findFolder( project.getProjectDirectory() );
        // XXX remove after SimpleTargetChooserPanel rewrite (suggestion if default dir is project dir then it's source dir)
        Sources sources = ProjectUtils.getSources(project);
        List<SourceGroup> roots = new ArrayList<SourceGroup>();
        SourceGroup[] javaRoots = 
            sources.getSourceGroups(ProjectConstants.SOURCES_TYPE_BPELPRO);
        roots.addAll(Arrays.asList(javaRoots));
        if (roots.isEmpty()) {
            SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            roots.addAll(Arrays.asList(sourceGroups));
        }
        DataFolder folder = DataFolder.findFolder(roots.get(0).getRootFolder());
        rootFolder = folder != null ? folder : rootFolder;
        // \XXX remove after SimpleTargetChooserPanel rewrite (suggestion if default dir is project dir then it's source dir)
        
        // XXX Remove root folder after FindAction rewrite
        return Lookups.fixed( new Object[] { project, rootFolder } );
    }

    // Private innerclasses ----------------------------------------------------

    private static final String[] BREAKABLE_PROPERTIES = new String[] {
        IcanproProjectProperties.JAVAC_CLASSPATH,
        IcanproProjectProperties.DEBUG_CLASSPATH,
        IcanproProjectProperties.SRC_DIR,
    };

    public static boolean hasBrokenLinks(AntProjectHelper helper, ReferenceHelper resolver) {
        /*
        return BrokenReferencesSupport.isBroken(helper, resolver, BREAKABLE_PROPERTIES,
            new String[] {IcanproProjectProperties.JAVA_PLATFORM});
        */
        return false;
    }

    /** Filter node containin additional features for the J2SE physical
     */
    private final class IcanLogicalViewRootNode extends AbstractNode {

        private Action brokenLinksAction;
        private boolean broken;


        public IcanLogicalViewRootNode() {
            super( new IcanproViews.LogicalViewChildren( helper, evaluator, project ), createLookup( project ) );
            setIconBase("org/netbeans/modules/bpel/project/ui/resources/icanproProjectIcon"); // NOI18N
            super.setName( ProjectUtils.getInformation( project ).getDisplayName() );
            if (hasBrokenLinks(helper, resolver)) {
                broken = true;
                brokenLinksAction = new BrokenLinksAction();
            }
        }
        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public HelpCtx getHelpCtx() {
            return new HelpCtx(IcanproLogicalViewProvider.class);
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
        
        // Private methods -------------------------------------------------

        private Action[] getAdditionalActions() {

            ResourceBundle bundle = NbBundle.getBundle(IcanproLogicalViewProvider.class);

            return new Action[] {
                
                CommonProjectActions.newFileAction(),
                null,
                ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_BUILD, bundle.getString( "LBL_BuildAction_Name" ), null ), // NOI18N
                ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_REBUILD, bundle.getString( "LBL_RebuildAction_Name" ), null ), // NOI18N
                ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_CLEAN, bundle.getString( "LBL_CleanAction_Name" ), null ), // NOI18N
//                null,
//                ProjectSensitiveActions.projectCommandAction( IcanproConstants.COMMAND_REDEPLOY, bundle.getString( "LBL_RedeployAction_Name" ), null ), // NOI18N
//                ProjectSensitiveActions.projectCommandAction( IcanproConstants.COMMAND_DEPLOY, bundle.getString( "LBL_DeployAction_Name" ), null ), // NOI18N
                null,
                getProjectDebuggerAction(),
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
//                null,
//                SystemAction.get(org.openide.actions.OpenLocalExplorerAction.class),
                null,
                ProjectSensitiveActions.projectCommandAction( IcanproConstants.POPULATE_CATALOG, bundle.getString( "LBL_Populate_Catalog" ), null ), // NOI18N
                null,
                brokenLinksAction,
                CommonProjectActions.customizeProjectAction(),
            };
        }
        
        /**
         * Find a registered BPELProjectDebuggerAction if available and add
         * to context menu.
         */
        private Action getProjectDebuggerAction( ) {
            Action action = null;
            FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource("BPELProjectDebuggerAction");
            
            if (fo != null) {
                DataFolder df = DataFolder.findFolder(fo);
                DataObject[] as = df.getChildren();
                fo = as[0].getPrimaryFile();
                for (Enumeration e = fo.getAttributes(); e.hasMoreElements() ;) {
                    String cmd = (String) e.nextElement();
                    String cmdClass = (String) fo.getAttribute(cmd);
                    try {
                        // get system class loader
                        ClassLoader classLoader = (ClassLoader) Lookup.getDefault().lookup(ClassLoader.class);
                        if (classLoader != null) {
                            // get the action to add
                            Class attachDebuggerClass = classLoader.loadClass(cmdClass);
                            Object objAction = SystemAction.get(attachDebuggerClass);
                            if (objAction instanceof Action) {
                                action = (Action)  objAction;
                                break;
                            }
                        }
                    } catch (ClassNotFoundException ex) {
                        break;
                    }
                }
            }
            return action;
        }

        /** This action is created only when project has broken references.
         * Once these are resolved the action is disabled.
         */
        private class BrokenLinksAction extends AbstractAction implements PropertyChangeListener {

            public BrokenLinksAction() {
                evaluator.addPropertyChangeListener(this);
                putValue(Action.NAME, NbBundle.getMessage(IcanproLogicalViewProvider.class, "LBL_Fix_Broken_Links_Action"));
            }

            public void actionPerformed(ActionEvent e) {
                /*
                BrokenReferencesSupport.showCustomizer(helper, resolver, BREAKABLE_PROPERTIES, new String[]{IcanproProjectProperties.JAVA_PLATFORM});
                if (!hasBrokenLinks(helper, resolver)) {
                    disable();
                }
                */
                // do nothing...
            }

            public void propertyChange(PropertyChangeEvent evt) {
                if (!broken) {
                    disable();
                    return;
                }
                broken = hasBrokenLinks(helper, resolver);
                if (!broken) {
                    disable();
                }
            }

            private void disable() {
                broken = false;
                setEnabled(false);
                evaluator.removePropertyChangeListener(this);
                fireIconChange();
                fireOpenedIconChange();
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
