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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbjarproject;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import org.netbeans.modules.j2ee.common.DatasourceUIHelper;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSource;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourcePopulator;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourceProvider;

/**
 * Implements the SPI interfaces providing support for data source handling.
 *
 * @author Erno Mononen
 */
public class EjbJarJPASupport implements JPADataSourcePopulator, JPADataSourceProvider{
    
    private final EjbJarProject project;
    
    /** Creates a new instance of EjbJarJPASupport */
    public EjbJarJPASupport(EjbJarProject project) {
        this.project = project;
    }
    
    public void connect(JComboBox comboBox) {
        DatasourceUIHelper.connect(project.getEjbModule(), comboBox);
        // a bit of a hack. needs rethinking when j2ee/persistence doesn't 
        // have a dependency to j2ee/utilities anymore (then the DatasourceUIHelper
        // may be changed to use JPADataSources directly).
        int size = comboBox.getItemCount();
        for (int i = 0; i < size; i++){
            Object item = comboBox.getItemAt(i);
            if (item instanceof Datasource){
                comboBox.insertItemAt(new DatasourceWrapper((Datasource)item), i);
            }
        }
    }
    
    public List<JPADataSource> getDataSources() {
        
        List<Datasource> datasources = new ArrayList<Datasource>();
        try {
            datasources.addAll(project.getEjbModule().getModuleDatasources());
        } catch (ConfigurationException e) {
            // TODO: it would be reasonable to rethrow this exception, see #96791
        }
        try {
            datasources.addAll(project.getEjbModule().getServerDatasources());
        } catch (ConfigurationException e) {
            // TODO: it would be reasonable to rethrow this exception, see #96791
        }

        List<JPADataSource> result = new ArrayList<JPADataSource>(datasources.size());
        for(Datasource each : datasources){
            result.add(new DatasourceWrapper(each));
        }
        return result;
    }

/**
 * Provides <code>JPADataSource</code> interface for <code>Datasource</code>s.
 */ 
private static class DatasourceWrapper implements Datasource, JPADataSource{
    
    private Datasource delegate;
    
    DatasourceWrapper(Datasource datasource){
        this.delegate = datasource;
    }
    
    public String getJndiName() {
        return delegate.getJndiName();
    }
    
    public String getUrl() {
        return delegate.getUrl();
    }
    
    public String getUsername() {
        return delegate.getUsername();
    }
    
    public String getPassword() {
        return delegate.getPassword();
    }
    
    public String getDriverClassName() {
        return delegate.getDriverClassName();
    }
    
    public String getDisplayName() {
        return delegate.getDisplayName();
    }
    
    public String toString(){
        return delegate.toString();
    }
}
    
}
