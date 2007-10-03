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
 * PrefetchDisabledModel.java
 *
 * Created on February 23, 2005, 11:46 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.List;
import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.dd.api.ejb.QueryMethod;
import org.netbeans.modules.j2ee.sun.dd.api.common.MethodParams;
import org.netbeans.modules.j2ee.sun.share.configbean.ConfigQuery;
import org.netbeans.modules.j2ee.sun.share.configbean.CmpEntityEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.MethodTableModel;
import org.netbeans.modules.j2ee.sun.share.configbean.StorageBeanFactory;


/**
 *
 * @author Rajeshwar Patil
 */
public class PrefetchDisabledModel extends MethodTableModel {
    
    static final ResourceBundle bundle = 
        ResourceBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.Bundle"); //NOI18N

    private CmpEntityEjb cmpEntityEjb;

    private static final String[] columnNames = {
        bundle.getString("LBL_Method"),                                 //NOI18N
        bundle.getString("LBL_Disable"),                                //NOI18N
    };


    /** Creates a new instance of PrefetchDisabledModel */
    public PrefetchDisabledModel(CmpEntityEjb cmpEntityEjb, List methodList,
            List prefetchedMethodList) {
        super(methodList, prefetchedMethodList);
        this.cmpEntityEjb = cmpEntityEjb;
    }


    public void setData(CmpEntityEjb cmpEntityEjb, List methodList, 
            List prefetchedMethodList){
        this.cmpEntityEjb = cmpEntityEjb;
        setData(methodList, prefetchedMethodList);
    }


    public PrefetchDisabledModel() {
    }


    protected String[] getColumnNames(){
        return columnNames;
    }


    protected Class getColumnType(int column){
        return Object.class;
    }


    protected Object getValueAt(int column, Object object, int row){
        if(column == 0){
            ConfigQuery.MethodData method = (ConfigQuery.MethodData) object;
            return method.getOperationName( );
        } else {
            //Control should never reach here.
            assert(false);
            return null;
        }
    }


    protected Object getDDValueAt(int column, Object ddObject){
        QueryMethod queryMethod = (QueryMethod) ddObject;
        switch(column){
            default: {
                //Control should never reach here.
                assert(false);
                return null;
            }
        }
    }


    protected void setDDValueAt(int column, Object ddObject, Object value){
        QueryMethod queryMethod = (QueryMethod) ddObject;
        switch(column){
            default: {
                //Control should never reach here.
                assert(false);
            }
        }
    }


    //convert the given Method object into the schama2beans DD (Method) object
    protected Object getDDMethod(Object object){
        StorageBeanFactory storageFactory = cmpEntityEjb.getConfig().getStorageFactory();
        
        ConfigQuery.MethodData method = (ConfigQuery.MethodData) object;
        QueryMethod queryMethod = storageFactory.createQueryMethod();

        queryMethod.setMethodName(method.getOperationName());
        List params = method.getParameters();
        MethodParams methodParams = queryMethod.newMethodParams();
        for(int i=0; i<params.size(); i++){
            methodParams.addMethodParam((String)params.get(i));
        }
        queryMethod.setMethodParams(methodParams);

        //printMethod(queryMethod);
        return queryMethod;
    }


    protected void addDDMethod(Object ddObject){
        QueryMethod ddMethod = (QueryMethod)ddObject;
        //printMethod(ddMethod);
        cmpEntityEjb.addQueryMethod(ddMethod);
    }


    protected void removeDDMethod(Object ddObject){
        QueryMethod ddMethod = (QueryMethod)ddObject;
        cmpEntityEjb.removeQueryMethod(ddMethod);
    }


    //determine whether the given schema2bean DD (Method) object is equal to 
    //the given Method object
    protected boolean areEqual(Object ddObject, Object object){
        QueryMethod ddMethod = (QueryMethod)ddObject;
        ConfigQuery.MethodData method = (ConfigQuery.MethodData) object;
        boolean returnValue = false;


        String ddMethodName = ddMethod.getMethodName();
        String methodName = method.getOperationName();
        if(ddMethodName.equals(methodName)){
            //check for parameters
            MethodParams methodParams = ddMethod.getMethodParams();
            List params = method.getParameters();

            String ddParam;
            String param;
            for(int i=0; i<params.size(); i++){
                param = (String)params.get(i);
                ddParam = methodParams.getMethodParam(i);
                if(!param.equals(ddParam)){
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }


    private void printMethod(QueryMethod queryMethod){
        System.out.println("PrefetchDisabledModel queryMethod:" + queryMethod);                         //NOI18N
        System.out.println("PrefetchDisabledModel queryMethod:" + queryMethod.toString());              //NOI18N
        System.out.println("PrefetchDisabledModel name :" + queryMethod.getMethodName() );              //NOI18N
        System.out.println("PrefetchDisabledModel params :" + queryMethod.getMethodParams() );          //NOI18N
    }
}
