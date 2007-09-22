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

import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.visualweb.dataconnectivity.project.datasource.ProjectDataSourceTracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.netbeans.api.project.Project;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.netbeans.modules.visualweb.dataconnectivity.project.datasource.ProjectDataSourceListener;
import org.netbeans.modules.visualweb.dataconnectivity.project.datasource.ProjectDataSourceChangeEvent;
import java.awt.Image;
import java.beans.PropertyEditor;
import java.io.CharConversionException;
import java.util.logging.Logger;
import javax.naming.NamingException;
import org.netbeans.modules.visualweb.api.j2ee.common.RequestedJdbcResource;
import org.netbeans.modules.visualweb.dataconnectivity.datasource.BrokenDataSourceSupport;
import org.netbeans.modules.visualweb.dataconnectivity.model.ProjectDataSourceManager;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;

/**
 * This class builds the visual representation of data sources (list) in the Project Navigator
 * @author Winston Prakash
 */
public class ProjectDataSourceNodeChildren extends Children.Keys implements Node.Cookie, ProjectDataSourceListener {

    Project nbProject = null;
    String url;
    
    private final String DYNAMIC_DATA_SOURCES = "dynamic_data_sources"; // NOI18N
    private final String HARD_CODED_DATA_SOURCES = "hard_coded_data_sources"; // NOI18N

    public ProjectDataSourceNodeChildren(Project project) {
        nbProject = project;
    }
    protected void addNotify() {
        super.addNotify();
        updateKeys();
        ProjectDataSourceTracker.addListener(nbProject, this);
    }

    protected void removeNotify() {
        ProjectDataSourceTracker.removeListener( nbProject,this);

        setKeys(Collections.EMPTY_SET);
        super.removeNotify();
    }

    private void updateKeys() {
        List dataSourceTypes = new ArrayList();

        dataSourceTypes.add(DYNAMIC_DATA_SOURCES);
        refreshKey(DYNAMIC_DATA_SOURCES);
        dataSourceTypes.add(HARD_CODED_DATA_SOURCES);
        refreshKey(HARD_CODED_DATA_SOURCES);

        if (!dataSourceTypes.isEmpty()) {
            setKeys(dataSourceTypes);
        }else{
            setKeys(Collections.EMPTY_SET);
        }
    }

    protected Node[] createNodes(Object key) {
        String type = (String) key;
        Node[] nodeArray = null;
        if (type.equals(DYNAMIC_DATA_SOURCES)) {

            String[] dynamicDataSources
                    = ProjectDataSourceTracker.getDynamicDataSources( nbProject );

            if(dynamicDataSources != null && dynamicDataSources.length > 0){
                nodeArray = new Node[dynamicDataSources.length];
                for(int i=0; i< dynamicDataSources.length; i++){
                    nodeArray[i] = createDatasourceNode(dynamicDataSources[i]);
                }
            }
        }else if (type.equals(HARD_CODED_DATA_SOURCES)) {

            String[] hardCodedDataSources
                    = ProjectDataSourceTracker.getHardcodedDataSources( nbProject);

            if(hardCodedDataSources != null && hardCodedDataSources.length > 0){
                nodeArray = new Node[hardCodedDataSources.length];
                for(int i=0; i< hardCodedDataSources.length; i++){
                    nodeArray[i] = createDatasourceNode(hardCodedDataSources[i]);
                }
            }
        }
        return nodeArray;
    }

    private Node createDatasourceNode(String datasourceName){
        AbstractNode node = new AbstractNode(Children.LEAF){
            final Image icon =  Utilities.loadImage("org/netbeans/modules/visualweb/dataconnectivity/resources/datasource_container.png"); // NOI18N
            final Image disconnectedIcon =  Utilities.loadImage("org/netbeans/modules/visualweb/dataconnectivity/resources/disconnected.png"); // NOI18N            
            Image brokenBadgedImage = Utilities.mergeImages(icon, disconnectedIcon, 8, 0);
            Image datasourceImage = Utilities.loadImage("org/netbeans/modules/visualweb/dataconnectivity/resources/datasource.png");  // NOI18N                        
            String dispName;
            
            public Image getIcon(int type) {
                
                dispName = super.getDisplayName();
                try {
                     if (BrokenDataSourceSupport.isBroken(nbProject)) {
                        dispName = "<font color=\"#A40000\">" + dispName + "</font>"; //NOI18N;
                    } else {
                        dispName = XMLUtil.toElementContent(dispName);
                    }
                } catch (CharConversionException ex) {
                    // ignore
                }
                
                if (BrokenDataSourceSupport.isBroken(nbProject)) {
                    return brokenBadgedImage;
                } else {
                    return datasourceImage;
                }                
            }
            
            public Image getOpenedIcon(int type){
                return getIcon(type);
            }     
            
            protected Sheet createSheet() {
                Sheet result = super.createSheet();
                Sheet.Set set = result.createPropertiesSet();
                set.put(new DataSourceURLProperty());
                result.put(set);
                return result;
            }

            final class DataSourceURLProperty extends PropertySupport.ReadOnly {
                private String url;
                
                DataSourceURLProperty() {
                    super("URL", DataSourceURLProperty.class, NbBundle.getMessage(DataSourceURLProperty.class, "LBL_URL"), NbBundle.getMessage(DataSourceURLProperty.class, "DESC_URL") + ", " + dispName);
                    try {
                        url = (String) getValue();
                    } catch (IllegalAccessException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (InvocationTargetException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

                public Object getValue() throws IllegalAccessException, InvocationTargetException {                    
                    ProjectDataSourceManager projectDataSourceManager = new ProjectDataSourceManager(nbProject);
                    RequestedJdbcResource jdbcResource = projectDataSourceManager.getDataSourceWithName(dispName);
                    url = jdbcResource.getUrl();                                     
                    return url;
                }

                public PropertyEditor getPropertyEditor() {
                    return new DataSourcePropertyEditor(url);
                }
            }                                          
        };
        

        String hoser = "java:comp/env/jdbc/" ;
        int pos = datasourceName.indexOf(hoser) ;
        String dsName = datasourceName.substring(hoser.length()) ;
        node.setName(datasourceName);
        node.setDisplayName(dsName);
        node.setShortDescription(dsName);
        
        Image icon = Utilities.loadImage("org/netbeans/modules/visualweb/dataconnectivity/resources/datasource.png");  // NOI18N
        node.setIconBaseWithExtension("org/netbeans/modules/visualweb/dataconnectivity/resources/datasource.png"); //NOI18N
        
        new ProjectDataSourceNode(nbProject);
        
        return node;
    }
    
    public void dataSourceChange(ProjectDataSourceChangeEvent evt) {
        updateKeys();                
    }
    
}
