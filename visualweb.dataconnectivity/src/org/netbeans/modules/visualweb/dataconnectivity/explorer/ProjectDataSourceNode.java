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
package org.netbeans.modules.visualweb.dataconnectivity.explorer;

import org.netbeans.modules.visualweb.dataconnectivity.datasource.BrokenDataSourceSupport;
import javax.swing.Action;
import org.netbeans.modules.visualweb.insync.Model;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.netbeans.modules.visualweb.dataconnectivity.actions.ResolveProjectDataSourceAction;
import org.netbeans.modules.visualweb.dataconnectivity.project.datasource.ProjectDataSourcesListener;
import org.netbeans.modules.visualweb.dataconnectivity.project.datasource.ProjectDataSourceTracker;
import org.netbeans.modules.visualweb.dataconnectivity.project.datasource.ProjectDataSourcesChangeEvent;
import java.awt.Image;
import java.io.CharConversionException;
import javax.naming.NamingException;
import org.netbeans.api.db.explorer.ConnectionListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.modules.visualweb.dataconnectivity.actions.RefreshProjectDataSourceAction;
import org.netbeans.modules.visualweb.dataconnectivity.datasource.CurrentProject;
import org.netbeans.modules.visualweb.dataconnectivity.utils.ImportDataSource;
import org.netbeans.modules.visualweb.insync.ModelSet;
import org.netbeans.modules.visualweb.insync.ModelSetListener;
import org.netbeans.modules.visualweb.insync.ModelSetsListener;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.xml.XMLUtil;


/**
 * Parent node for the data sources in the project navigator
 * @author Winston Prakash
 * @author jfbrown - conversion to NB4 projects
 */
public class ProjectDataSourceNode extends AbstractNode implements Node.Cookie, ProjectDataSourcesListener, ConnectionListener  {

    org.netbeans.api.project.Project nbProject = null ;
    private static Image brokenDsReferenceBadge = Utilities.loadImage( "org/netbeans/modules/visualweb/dataconnectivity/resources/disconnected.png" ); // NOI18N
    private static Image dSContainerImage = Utilities.loadImage( "org/netbeans/modules/visualweb/dataconnectivity/resources/datasource_container.png" ); // NOI18N
    protected WaitForModelingListener modelingListener = new WaitForModelingListener() ;
    private volatile boolean firstTimeShowAlert = false;
    private volatile boolean isModeled;

    public ProjectDataSourceNode(org.netbeans.api.project.Project project) {
        super(new ProjectDataSourceNodeChildren(project));
        nbProject = project;
        CurrentProject.getInstance().setProject(nbProject);
        // Create a weak listener so that the connection listener can be GC'd when listener for a project is no longer referenced
        ConnectionManager.getDefault().addConnectionListener(WeakListeners.create(ConnectionListener.class, this, ConnectionManager.getDefault()));
        isModeled = ProjectDataSourceTracker.isProjectModeled(project);
        initPuppy() ;
    }

    protected void addListener() {
        ProjectDataSourceTracker.addListener(nbProject,this);
    }

    protected void removeListener() {
        ProjectDataSourceTracker.refreshDataSources(nbProject);
        ProjectDataSourceTracker.removeListener(nbProject,this);
    }

    // This allows us to get a reference to ourselves if we're called
    // from a FilterNode.
    public Node.Cookie getCookie(Class clazz) {
        if ( clazz == ProjectDataSourceNode.class ) {
            return this ;
        }
        else return super.getCookie(clazz) ;
    }

    private void initPuppy() {
        setIconBaseWithExtension("org/netbeans/modules/visualweb/dataconnectivity/resources/datasource_container.png"); // NOI18N
        setName(NbBundle.getMessage(ProjectDataSourceNode.class, "PROJECT_DATA_SOURCES"));
        setDisplayName(NbBundle.getMessage(ProjectDataSourceNode.class, "PROJECT_DATA_SOURCES"));
        setShortDescription(NbBundle.getMessage(ProjectDataSourceNode.class, "PROJECT_DATA_SOURCES"));
        setValue("propertiesHelpID", "projrave_ui_elements_project_nav_node_prop_sheets_data_source_node_props"); // NOI18N
        addListener();      
    }

    // Create the popup menu:
    public Action[] getActions(boolean context) {
        if (ImportDataSource.isLegacyProject(nbProject)) {
            return new Action[] {
                SystemAction.get(ResolveProjectDataSourceAction.class),
                SystemAction.get(RefreshProjectDataSourceAction.class)
            };
        } else {
            return new Action[] {         
                SystemAction.get(RefreshProjectDataSourceAction.class)
            };
        }
    }

    public Action getPreferredAction() {
        return SystemAction.get(ResolveProjectDataSourceAction.class);
    }

//    public HelpCtx getHelpCtx() {
//        return new HelpCtx("projrave_ui_elements_project_nav_data_source_ref_node"); // NOI18N
//    }

    public org.netbeans.api.project.Project getNbProject(){
        return nbProject;
    }

    // For icon badging
    public Image getIcon(int type) {
        String dispName = super.getDisplayName();
        try {
            dispName = XMLUtil.toElementContent(dispName);
        } catch (CharConversionException ex) {
            // ignore
        }
        
        boolean isBroken = false;         
        
        if (ImportDataSource.isLegacyProject(nbProject)) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (!firstTimeShowAlert) {
                        if (!ProjectDataSourceTracker.isProjectModeled(nbProject) || BrokenDataSourceSupport.isBroken(nbProject) ) {
                            ImportDataSource.showAlert();
                        }                                                  
                        firstTimeShowAlert = true;                        
                    }
                }
            });
        }    
                
        // Mark node as broken if the legacy project hasn't been modeled
        if (ImportDataSource.isLegacyProject(nbProject)) {            
            isBroken = !ProjectDataSourceTracker.isProjectModeled(nbProject);
        }
        
        // Check if Data Source Reference node has any child nodes, if it does, check if any data sources are missing
        if (this.getChildren().getNodes().length > 0) {
            if (BrokenDataSourceSupport.isBroken(nbProject)) {
                isBroken = true;
            } else {
                isBroken = false;
            }
        } 
        
        if (isBroken){
            Image brokenBadge = Utilities.mergeImages(dSContainerImage, brokenDsReferenceBadge, 8, 0);
            return brokenBadge;
        } else{
            return dSContainerImage;
        }
    }
                     
    public String getHtmlDisplayName() {
        String dispName = super.getDisplayName();
        try {
            dispName = XMLUtil.toElementContent(dispName);
        } catch (CharConversionException ex) {
            // ignore
        }
       
        boolean isBroken = false;
        
         // Mark display name as broken if the legacy project hasn't been modeled
        if (ImportDataSource.isLegacyProject(nbProject)) {            
            isBroken = !ProjectDataSourceTracker.isProjectModeled(nbProject);
        }
        
        // Check if Data Source Reference node has any child nodes, if it does, check if any data sources are missing
        if (this.getChildren().getNodes().length > 0) {
            if (BrokenDataSourceSupport.isBroken(nbProject)) {
                isBroken = true;
            } else {
                isBroken = false;
            }
        }
        
        return isBroken ? "<font color=\"#A40000\">" + dispName + "</font>" : null; //NOI18N;
    }
    
    public void dataSourcesChange(ProjectDataSourcesChangeEvent evt) {
        fireIconChange(); 
        fireDisplayNameChange(null, null);
    }         

    public void connectionsChanged() {
        fireIconChange(); 
        fireDisplayNameChange(null, null);
    }
    
    public class WaitForModelingListener implements ModelSetsListener, ModelSetListener {
        
        /*---------- ModelSetsListener------------*/
        
        public void modelSetAdded(ModelSet modelSet) {
            modelSet.addModelSetListener(modelingListener) ;                      
        }
        
        public void modelSetRemoved(ModelSet modelSet) {
            modelSet.removeModelSetListener(modelingListener) ;            
        }
        
        public void modelProjectChanged() {
            // not implemented 
        }        
        
        /*---------- ModelSetListener------------*/
        public void modelAdded(Model model) {
            isModeled = true;
            fireIconChange();
            fireDisplayNameChange(null, null);
        }
        
        public void modelChanged(Model model) {
            isModeled = true;
            fireIconChange();
            fireDisplayNameChange(null, null);
        }
        
        public void modelRemoved(Model model) {
            ModelSet.removeModelSetsListener(this) ;
        }
                       
        /*---------- end of interface implements ------------*/
        
    }
}
