/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbjarproject.ui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.LogicalViewChildren;

import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.modules.j2ee.ejbjarproject.UpdateHelper;
import org.openide.loaders.DataFolder;
import org.openide.util.lookup.Lookups;

import org.netbeans.modules.j2ee.api.common.J2eeProjectConstants;

/**
 * Support for creating logical views.
 * @author Petr Hrebejk
 */
public class EjbJarLogicalViewProvider implements LogicalViewProvider {
    
    private final Project project;
    private final AntProjectHelper helper;    
    private final UpdateHelper updateHelper;
    private final PropertyEvaluator evaluator;
    private final SubprojectProvider spp;
    private final ReferenceHelper resolver;
    
    
    public EjbJarLogicalViewProvider(Project project, UpdateHelper updateHelper, PropertyEvaluator evaluator, SubprojectProvider spp, ReferenceHelper resolver) {
        this.project = project;
        assert project != null;
        this.updateHelper = updateHelper;
        assert updateHelper != null;
        this.helper = updateHelper.getAntProjectHelper();
        this.evaluator = evaluator;
        assert evaluator != null;
        this.spp = spp;
        assert spp != null;
        this.resolver = resolver;
    }
        
    public Node createLogicalView() {
        return new WebLogicalViewRootNode();
    }
    
    public Node findPath( Node root, Object target ) {
        // XXX implement
        return null;
    }
            
   private static Lookup createLookup( Project project ) {
        DataFolder rootFolder = DataFolder.findFolder( project.getProjectDirectory() );
        // XXX Remove root folder after FindAction rewrite
        return Lookups.fixed( new Object[] { project, rootFolder } );
    }

    // Private innerclasses ----------------------------------------------------
   
    private static final String[] BREAKABLE_PROPERTIES = new String[] {
        EjbJarProjectProperties.JAVAC_CLASSPATH,  
        EjbJarProjectProperties.DEBUG_CLASSPATH,
        EjbJarProjectProperties.SRC_DIR,
    };

    public static boolean hasBrokenLinks(AntProjectHelper helper, ReferenceHelper resolver) {
        return BrokenReferencesSupport.isBroken(helper, resolver, BREAKABLE_PROPERTIES, 
            new String[] {EjbJarProjectProperties.JAVA_PLATFORM});
    }

    /** Filter node containin additional features for the J2SE physical
     */
    private final class WebLogicalViewRootNode extends AbstractNode {

        private Action brokenLinksAction;
        private boolean broken;
        
        
        public WebLogicalViewRootNode() {
            super( new LogicalViewChildren( project, updateHelper, evaluator, resolver ), createLookup( project ) ); 
            setIconBase( "org/netbeans/modules/j2ee/ejbjarproject/ui/resources/ejbjarProjectIcon" ); // NOI18N
            setName( ProjectUtils.getInformation( project ).getDisplayName() );            
            if (hasBrokenLinks(helper, resolver)) {
                broken = true;
                brokenLinksAction = new BrokenLinksAction();
            }
        }

        public Action[] getActions( boolean context ) {
            if ( context )
                return super.getActions( true );
            else
                return getAdditionalActions();
        }

        public boolean canRename() {
            return false;
        }
        
        // Private methods -------------------------------------------------    

        private Action[] getAdditionalActions() {

            ResourceBundle bundle = NbBundle.getBundle(EjbJarLogicalViewProvider.class);
            
            J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
            if (provider != null && provider.hasVerifierSupport()) {
                return new Action[] {
                    CommonProjectActions.newFileAction(),
                    null,                
                    ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_BUILD, bundle.getString( "LBL_BuildAction_Name" ), null ), // NOI18N
                    ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_REBUILD, bundle.getString( "LBL_RebuildAction_Name" ), null ), // NOI18N
                    ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_CLEAN, bundle.getString( "LBL_CleanAction_Name" ), null ), // NOI18N
                    ProjectSensitiveActions.projectCommandAction( "verify", bundle.getString( "LBL_VerifyAction_Name" ), null ), // NOI18N
                    ProjectSensitiveActions.projectCommandAction( JavaProjectConstants.COMMAND_JAVADOC, bundle.getString( "LBL_JavadocAction_Name" ), null ), // NOI18N                
                    null,
                    ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_RUN, bundle.getString( "LBL_RunAction_Name" ), null ), // NOI18N
                    ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_DEBUG, bundle.getString( "LBL_DebugAction_Name" ), null ), // NOI18N
                    //ProjectSensitiveActions.projectCommandAction( J2eeProjectConstants.COMMAND_REDEPLOY, bundle.getString( "LBL_RedeployAction_Name" ), null ), // NOI18N
                    ProjectSensitiveActions.projectCommandAction( J2eeProjectConstants.COMMAND_DEPLOY, bundle.getString( "LBL_DeployAction_Name" ), null ), // NOI18N
                    null,
                    CommonProjectActions.setAsMainProjectAction(),
                    CommonProjectActions.openSubprojectsAction(),
                    CommonProjectActions.closeProjectAction(),
                    null,
                    SystemAction.get( org.openide.actions.FindAction.class ),
                    null,
                    SystemAction.get(org.openide.actions.OpenLocalExplorerAction.class),
                    SystemAction.get( org.openide.actions.ToolsAction.class ),
                    null,
                    brokenLinksAction,
                    CommonProjectActions.customizeProjectAction(),
                };
            } else {
                return new Action[] {
                    CommonProjectActions.newFileAction(),
                    null,                
                    ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_BUILD, bundle.getString( "LBL_BuildAction_Name" ), null ), // NOI18N
                    ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_REBUILD, bundle.getString( "LBL_RebuildAction_Name" ), null ), // NOI18N
                    ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_CLEAN, bundle.getString( "LBL_CleanAction_Name" ), null ), // NOI18N
                    ProjectSensitiveActions.projectCommandAction( JavaProjectConstants.COMMAND_JAVADOC, bundle.getString( "LBL_JavadocAction_Name" ), null ), // NOI18N                
                    null,
                    ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_RUN, bundle.getString( "LBL_RunAction_Name" ), null ), // NOI18N
                    ProjectSensitiveActions.projectCommandAction( ActionProvider.COMMAND_DEBUG, bundle.getString( "LBL_DebugAction_Name" ), null ), // NOI18N
                    //ProjectSensitiveActions.projectCommandAction( J2eeProjectConstants.COMMAND_REDEPLOY, bundle.getString( "LBL_RedeployAction_Name" ), null ), // NOI18N
                    ProjectSensitiveActions.projectCommandAction( J2eeProjectConstants.COMMAND_DEPLOY, bundle.getString( "LBL_DeployAction_Name" ), null ), // NOI18N
                    null,
                    CommonProjectActions.setAsMainProjectAction(),
                    CommonProjectActions.openSubprojectsAction(),
                    CommonProjectActions.closeProjectAction(),
                    null,
                    SystemAction.get( org.openide.actions.FindAction.class ),
                    null,
                    SystemAction.get(org.openide.actions.OpenLocalExplorerAction.class),
                    SystemAction.get( org.openide.actions.ToolsAction.class ),
                    null,
                    brokenLinksAction,
                    CommonProjectActions.customizeProjectAction(),
                };
            }
        }
        
        /** This action is created only when project has broken references.
         * Once these are resolved the action is disabled.
         */
        private class BrokenLinksAction extends AbstractAction implements PropertyChangeListener {

            public BrokenLinksAction() {
                evaluator.addPropertyChangeListener(this);
                putValue(Action.NAME, NbBundle.getMessage(EjbJarLogicalViewProvider.class, "LBL_Fix_Broken_Links_Action"));
            }

            public void actionPerformed(ActionEvent e) {
                BrokenReferencesSupport.showCustomizer(helper, resolver, BREAKABLE_PROPERTIES, new String[]{EjbJarProjectProperties.JAVA_PLATFORM});
                if (!hasBrokenLinks(helper, resolver)) {
                    disable();
                }
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
