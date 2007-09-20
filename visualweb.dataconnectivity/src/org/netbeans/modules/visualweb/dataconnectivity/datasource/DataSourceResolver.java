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
 *//*
 * DataSourceResolver.java
 *
 * Created on September 6, 2006, 10:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.visualweb.dataconnectivity.datasource;

import java.io.IOException;
import org.netbeans.modules.visualweb.api.j2ee.common.RequestedJdbcResource;
import org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfo;
import org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfoListener;
import org.netbeans.modules.visualweb.dataconnectivity.model.ProjectDataSourceManager;
import org.netbeans.modules.visualweb.project.jsf.services.DesignTimeDataSourceService;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.visualweb.dataconnectivity.model.ProjectDataSourceManager;
import org.netbeans.modules.visualweb.dataconnectivity.naming.DatabaseSettingsImporter;
import org.netbeans.modules.visualweb.dataconnectivity.project.datasource.ProjectDataSourceTracker;
import org.netbeans.modules.visualweb.dataconnectivity.sql.DesignTimeDataSourceHelper;
import org.netbeans.modules.visualweb.dataconnectivity.utils.ImportDataSource;
import org.netbeans.modules.visualweb.insync.Model;
import org.netbeans.modules.visualweb.insync.ModelSet;
import org.netbeans.modules.visualweb.insync.ModelSetsListener;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 *
 * @author John Baker
 */
public class DataSourceResolver implements DataSourceInfoListener, Runnable {
    private static final String DRIVER_CLASS_NET = "org.apache.derby.jdbc.ClientDriver"; // NOI18N    
    private static final String DATASOURCE_PREFIX = "java:comp/env/"; // NOI18N
    private static DataSourceResolver dataSourceResolver;
    private String dataSourceInfo = null;
    protected WaitForModelingListener modelingListener = new WaitForModelingListener();
    private Project project;
    private ProgressHandle handle = null;
    private TopComponent topComponent;
    private RequestProcessor.Task task = null;
    private final RequestProcessor WAIT_FOR_MODELING_RP = new RequestProcessor("DataSourceResolver.WAIT_FOR_MODELING_RP"); //NOI18N    
    private Model[] modelSets = null;
    
    /** Creates a new instance of DataSourceResolver */
    private DataSourceResolver() {
    }

    public static DataSourceResolver getInstance() {
        if (dataSourceResolver == null) {
            dataSourceResolver = new DataSourceResolver();
        }
        return dataSourceResolver;
    }



    public void dataSourceInfoModified(org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfoEvent modelEvent) {
        dataSourceInfo = modelEvent.getDataSourceInfoId();
    }

    public void datasourceInfoAdded(org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfoEvent modelEvent) {
        dataSourceInfo = modelEvent.getDataSourceInfoId();
    }

    public void dataSourceInfoRemoved(org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfoEvent modelEvent) {
        ; // no-op
    }

    // if a project data source does not have a corresponding connetion then return true
    public boolean isDataSourceMissing(Project project, String prjDsName) {
        DesignTimeDataSourceService dataSourceService = Lookup.getDefault().lookup(DesignTimeDataSourceService.class);
        Set<RequestedJdbcResource> problemDatasources = dataSourceService.getBrokenDatasources(project);

        boolean missing = false;
        Iterator it = problemDatasources.iterator();
        while (it.hasNext()) {
            RequestedJdbcResource reqRes = (RequestedJdbcResource) it.next();
            if (("jdbc/" + prjDsName).equals(reqRes.getJndiName())) { //NOI18N
                missing = true;
                break; // data source match made, stop checking
            }
        }

        return missing;
    }
    
    public Set<RequestedJdbcResource> getProjectDataSources(Project project) {
        DesignTimeDataSourceService dataSourceService = Lookup.getDefault().lookup(DesignTimeDataSourceService.class);
        return dataSourceService.getProjectDataSources(project);
    }

    public boolean isDataSourceUnique(Project currentProj, String dsName, String url) {        
        String[] dynamicDataSources = ProjectDataSourceTracker.getDynamicDataSources(currentProj);
                
        for (String name : dynamicDataSources) {
            if (name.equals((DATASOURCE_PREFIX + dsName))) {
                if (!getDataSourceUrl(dsName).equals(url)) {
                    return true;
                }
            } else {
                return true;
            }
        }
        
        return false;
    }

     private String getDataSourceUrl(String dsName) {
        String url = null;
        DataSourceInfo dsInfo = null;

        List<DataSourceInfo> dataSourcesInfo = DatabaseSettingsImporter.getInstance().getDataSourcesInfo();
        Iterator it = dataSourcesInfo.iterator();
        while (it.hasNext()) {
            dsInfo = (DataSourceInfo) it.next();
            if (dsName.equals(dsInfo.getName())) {
                url = dsInfo.getUrl();
            }
        }

        return url;
    }
     
    public void updateSettings() {
        doCopying();
        registerConnections();
    }
    
    public void update(Project currentProj) {
        updateProject(currentProj);                
    }


    // Find a matching driver registered with the IDE
    public JDBCDriver findMatchingDriver(String driverClass) {
        int i = 0;
        JDBCDriver[] newDrivers;
        newDrivers = JDBCDriverManager.getDefault().getDrivers();       

        for (i = 0; i < newDrivers.length; i++) {
            if (newDrivers[i].getClassName().equals(driverClass)) {
                return newDrivers[i];
            }
        }

        return null;
    }

    private boolean updateProject(Project project, DataSourceInfo dsInfo) {
        boolean needAdd = false;
        ProjectDataSourceManager projectDataSourceManager = new ProjectDataSourceManager(project);

        // if project's datasource hasn't been added, then add it
        if (projectDataSourceManager.getDataSourceWithName(dsInfo.getName()) == null) {
            projectDataSourceManager.addDataSource(dsInfo);
            needAdd = true;
        } else {
            needAdd = false;
        }
        return needAdd;
    }

    private void doCopying() {
        try {
            ImportDataSource.prepareCopy();
        } catch (IOException ioe) {
            Logger.getLogger("copy").info("Migrating user settings failed " + ioe); //NOI18N
        }
    }

    private void registerConnections() {
        JDBCDriver[] drvsArray = JDBCDriverManager.getDefault().getDrivers(DRIVER_CLASS_NET);
        if (drvsArray.length > 0) {
            DatabaseSettingsImporter.getInstance().locateAndRegisterDrivers();
            DatabaseSettingsImporter.getInstance().locateAndRegisterConnections(false);
        }
    }

    private void updateProject(Project project) {
        // Update Project's datasources
        try {
            new DesignTimeDataSourceHelper().updateDataSource(project);
            checkConnections(project);
        } catch (NamingException ne) {
            Logger.getLogger("copy").info("Migrating user settings failed " + ne); //NOI18N
        }
    }
    
    // Check if any database connections needed by the project are missing
    private void checkConnections(Project project) {
        DesignTimeDataSourceService dataSourceService = Lookup.getDefault().lookup(DesignTimeDataSourceService.class);
        Set<RequestedJdbcResource> problemDatasources = dataSourceService.getBrokenDatasources(project);
        if (!problemDatasources.isEmpty()) {
            ImportDataSource.showAlert();
        }
    }


    public void modelProjectForDataSources(Project currentProj) {
        project = currentProj;
        ModelSet.addModelSetsListener(modelingListener);
        topComponent = TopComponent.getRegistry().getActivated();
        topComponent.setCursor(Utilities.createProgressCursor(topComponent));
        String progressBarLabel = org.openide.util.NbBundle.getMessage(DataSourceResolver.class, "ProgressBarLabel"); //NOI18N
        
        try {
            // model project 
            FacesModelSet modelSet = FacesModelSet.startModeling(project);

            if (modelSet == null) {
                handle = ProgressHandleFactory.createHandle(progressBarLabel);
                handle.start();
                handle.switchToIndeterminate();
            }

            // If modeling has been completed then terminate the progress cursor and update the project
            if (modelSet != null) {
                if (handle != null) {
                    handle.finish();
                }

                ModelSet.removeModelSetsListener(modelingListener);
                ProjectDataSourceTracker.refreshDataSourceReferences(project);
            }
        } finally {
            topComponent.setCursor(null);
        }
    }
    
    /*
     * Schedule update task
     */
    public synchronized void updateTask() {
        if (task == null) {
            task = WAIT_FOR_MODELING_RP.create(this);
        }
        task.schedule(50);
    }

    /*
     * Update data sources in the project
     */
    public synchronized void run() {        
        // make sure the sources are modeled
        for (Model mModel : modelSets) {
            String filenameExt = mModel.getFile().getExt();
            if (filenameExt.equals("java")) {
                FacesModelSet.getFacesModelIfAvailable(mModel.getFile());
            }
        }

        // Refresh data sources in a project
        ProjectDataSourceTracker.refreshDataSourceReferences(project);
        // Update the resource references in the project
        update(project);
        
        // Terminate the progress cursor when done
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (handle != null) {
                    handle.finish();
                }
            }
        });
        
        // clean up
        modelSets = null;
        ModelSet.removeModelSetsListener(modelingListener);
    }
    
    public boolean isDatasourceCreationSupported(Project project) {
        DesignTimeDataSourceService dataSourceService = Lookup.getDefault().lookup(DesignTimeDataSourceService.class);
        return dataSourceService.isDatasourceCreationSupported(project);        
    }

    public class WaitForModelingListener implements ModelSetsListener {        
        
        /*---------- ModelSetsListener------------*/

        public void modelSetAdded(ModelSet modelSet) {
            try {
                // update data sources, if necessary
                modelSets = modelSet.getModels();
                updateTask();                
            } finally {
                topComponent.setCursor(null);
            }
        }

        public void modelSetRemoved(ModelSet modelSet) {
            // not implemented
        }

        public void modelProjectChanged() {
            // not implemented
        }

        /*---------- end of interface implements ------------*/        
    }            
}
