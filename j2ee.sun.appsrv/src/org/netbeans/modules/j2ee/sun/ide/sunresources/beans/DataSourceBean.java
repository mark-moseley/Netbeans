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
/*
 * DataSourceBean.java
 *
 * Created on September 16, 2003, 11:26 AM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import java.util.Vector;

import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;
import org.netbeans.modules.j2ee.sun.share.serverresources.JdbcDS;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.*;

/**
 *
 * @author  nityad
 */
public class DataSourceBean extends JdbcDS implements java.io.Serializable {
        
    /** Creates a new instance of DataSourceBean */
    public DataSourceBean() {
    
    }
    
    public String getName() {
        return super.getName();
    }
    
    public String getJndiName(){
        return super.getJndiName();
    }
    
    public static DataSourceBean createBean(JdbcResource datasource) {
        DataSourceBean bean = new DataSourceBean();
        //name attribute in bean is for studio display purpose. 
        //It is not part of the jdbc-resource dtd.
        bean.setName(datasource.getJndiName());
        bean.setDescription(datasource.getDescription());
        bean.setJndiName(datasource.getJndiName());
        bean.setConnPoolName(datasource.getPoolName());
        bean.setResType(datasource.getObjectType());
        bean.setIsEnabled(datasource.getEnabled());
        
        PropertyElement[] extraProperties = datasource.getPropertyElement();
        Vector vec = new Vector();       
        for (int i = 0; i < extraProperties.length; i++) {
            NameValuePair pair = new NameValuePair();
            pair.setParamName(extraProperties[i].getName());
            pair.setParamValue(extraProperties[i].getValue());
            vec.add(pair);
        }
        
        if (vec != null && vec.size() > 0) {
            NameValuePair[] props = new NameValuePair[vec.size()];
            bean.setExtraParams((NameValuePair[])vec.toArray(props));
        }    
        
        return bean;
    }
    
    public Resources getGraph(){
        Resources res = getResourceGraph();
        JdbcResource datasource = res.newJdbcResource();
        datasource.setDescription(getDescription());
        datasource.setJndiName(getJndiName());
        datasource.setPoolName(getConnPoolName());
        datasource.setObjectType(getResType());
        datasource.setEnabled(getIsEnabled());
        
        // set properties
        NameValuePair[] params = getExtraParams();
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                NameValuePair pair = params[i];
                PropertyElement prop = datasource.newPropertyElement();
                prop = populatePropertyElement(prop, pair);
                datasource.addPropertyElement(prop);
            }
        }
        
        res.addJdbcResource(datasource);
        return res;
    }
    
}
