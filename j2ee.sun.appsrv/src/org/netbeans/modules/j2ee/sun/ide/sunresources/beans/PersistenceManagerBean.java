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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
/*
 * PersistenceManagerBean.java
 *
 * Created on September 17, 2003, 1:29 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import java.util.Vector;

import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;
import org.netbeans.modules.j2ee.sun.share.serverresources.PersistenceManagerResource;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.*;

/**
 *
 * @author  nityad
 */
public class PersistenceManagerBean extends PersistenceManagerResource implements java.io.Serializable {
    
    /** Creates a new instance of PersistenceManagerBean */
    public PersistenceManagerBean() {
    }
    
    public String getName() {
        return super.getName();
    }
    
    public String getJndiName(){
        return super.getJndiName();
    }
    
    public static PersistenceManagerBean createBean(PersistenceManagerFactoryResource pmfresource) {
        PersistenceManagerBean bean = new PersistenceManagerBean();
        //name attribute in bean is for studio display purpose. 
        //It is not part of the persistence-manager-factory-resource dtd.
        bean.setName(pmfresource.getJndiName());
        bean.setDescription(pmfresource.getDescription());
        bean.setJndiName(pmfresource.getJndiName());
        bean.setFactoryClass(pmfresource.getFactoryClass());
        bean.setDatasourceJndiName(pmfresource.getJdbcResourceJndiName());
        bean.setIsEnabled(pmfresource.getEnabled());
        
        PropertyElement[] extraProperties = pmfresource.getPropertyElement();
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
        return getBeanInGraph(res);
    }    
    
    public Resources getBeanInGraph(Resources res){
        PersistenceManagerFactoryResource pmfresource = res.newPersistenceManagerFactoryResource();
        pmfresource.setDescription(getDescription());
        pmfresource.setJndiName(getJndiName());
        pmfresource.setFactoryClass(getFactoryClass());
        pmfresource.setJdbcResourceJndiName(getDatasourceJndiName());
        pmfresource.setEnabled(getIsEnabled());
        
        // set properties
        NameValuePair[] params = getExtraParams();
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                NameValuePair pair = params[i];
                PropertyElement prop = pmfresource.newPropertyElement();
                prop = populatePropertyElement(prop, pair);
                pmfresource.addPropertyElement(prop);
            }
        }
        
        res.addPersistenceManagerFactoryResource(pmfresource);
        return res;
    }
    
}
