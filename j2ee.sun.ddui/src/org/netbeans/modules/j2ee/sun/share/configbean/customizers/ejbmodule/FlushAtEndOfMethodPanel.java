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
/*
 * FlushAtEndOfMethodPanel.java
 *
 * Created on February 5, 2005, 7:27 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.enterprise.deploy.model.DDBean;
import javax.swing.event.TableModelListener;

import org.netbeans.modules.j2ee.sun.dd.api.ejb.FlushAtEndOfMethod;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Method;
import org.netbeans.modules.j2ee.sun.share.configbean.CmpEntityEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.MultiMethodTableModel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.MultiMethodTablePanel;

import org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.DummyMethod;


/**
 *
 * @author Rajeshwar Patil
 */
public class FlushAtEndOfMethodPanel extends MultiMethodTablePanel{
    
    private CmpEntityEjb cmpEntityEjb;
    private CmpEntityEjbCustomizer customizer;
    private FlushAtEndOfMethodModel model;

    public static final String HOME = "Home";                           //NOI18N
    public static final String REMOTE = "Remote";                       //NOI18N
    public static final String LOCAL = "Local";                         //NOI18N
    public static final String LOCAL_HOME = "LocalHome";                //NOI18N

    static final ResourceBundle bundle = 
        ResourceBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.Bundle"); // NOI18N

    /** Creates a new instance of FlushAtEndOfMethodPanel */
    public FlushAtEndOfMethodPanel(CmpEntityEjb cmpEntityEjb, CmpEntityEjbCustomizer customizer) {
        this.cmpEntityEjb = cmpEntityEjb;
        this.customizer = customizer;
    }


    protected MultiMethodTableModel getMultiMethodTableModel(){
        Map selectionToMethodsMap = getSelectionToMethodsMap();
        Map selectionToDDMethodsMap = getSelectionToDDMethodsMap();
        model = new FlushAtEndOfMethodModel(cmpEntityEjb, selectionToMethodsMap, selectionToDDMethodsMap);
        model.addTableModelListener((TableModelListener)customizer);
        return model;
    }


    public void setData(CmpEntityEjb cmpEntityEjb){
        this.cmpEntityEjb = cmpEntityEjb;
        Map selectionToMethodsMap = getSelectionToMethodsMap();
        Map selectionToDDMethodsMap = getSelectionToDDMethodsMap();
        model.setData(cmpEntityEjb, selectionToMethodsMap, selectionToDDMethodsMap);
        setData();
    }


    protected String getSelectionLabelText(){
        return bundle.getString("LBL_Interface");                       //NOI18N
    }


    protected String getSelectionLabelAcsblName(){
        return bundle.getString("Interface_Acsbl_Name");                //NOI18N
    }


    protected String getSelectionLabelAcsblDesc(){
        return bundle.getString("Interface_Acsbl_Desc");                //NOI18N
    }


    protected String getSelectionComboAcsblName(){
        return bundle.getString("Interface_Acsbl_Name");                //NOI18N
    }


    protected String getSelectionComboAcsblDesc(){
        return bundle.getString("Interface_Acsbl_Desc");                //NOI18N
    }


    protected String getTablePaneAcsblName(){
        return bundle.getString("Method_Acsbl_Name");                   //NOI18N
    }


    protected String getTablePaneAcsblDesc(){
        return bundle.getString("Flush_Method_Acsbl_Desc");             //NOI18N
    }


    protected char getSelectionLabelMnemonic(){
        return bundle.getString("MNC_Interface").charAt(0);             //NOI18N
    }


    protected String getSelectionComboToolTip(){
        return bundle.getString("Interface_Tool_Tip");                  //NOI18N
    }


    protected String getTablePaneToolTip(){
        return bundle.getString("Method_Tool_Tip");                     //NOI18N
    }


    protected String getToolTip(int row, int col){
        if(col == 0){
            //ConfigQuery.MethodData method = (ConfigQuery.MethodData) model.getMethods().get(row); //revert later
            DummyMethod method = (DummyMethod) model.getMethods().get(row);
            if(method != null){
                //return method.getOperationName(); //XXX -- return the signature of method  //revert later
                return method.getName(); //XXX -- return the signature of method 
            }
        }
        return null;
    }


    protected String getToolTip(int col){
        if(col == 0){
            return bundle.getString("Flush_Method_Header_Tool_Tip");    //NOI18N
        }
        if(col == 1){
            return bundle.getString("Flush_Header_Tool_Tip");           //NOI18N
        }
        if(col == 2){
            return bundle.getString("Desc_Header_Tool_Tip");            //NOI18N
        }
        return null;
    }


    //Contruct a map of Interface Types to Methods
    //Get the interface types from the standard DD amd 
    //construct this map by getting all the Methods in the Interfaces
    private Map getSelectionToMethodsMap(){
        //XXX -- you may need to change this; use api instead to query for supported interfaces
        Hashtable selectionToMethodsMap = new Hashtable();
        if(cmpEntityEjb != null){
            DDBean ddBean = cmpEntityEjb.getDDBean();
            //xpath - ejb-jar/enterprise-beans/entity
            //System.out.println("xpath: " + ddBean.getXpath());            //NOI18N
            //DDBeanRoot ddBeanRoot = getDDBean().getRoot();
            DDBean[] childBeans = ddBean.getChildBean("./home");            //NOI18N
            if(childBeans.length > 0){
                //System.out.println("xpath: " + childBeans[0].getXpath()); //NOI18N
                //xpath - ejb-jar/enterprise-beans/entity/local
                selectionToMethodsMap.put(HOME, getMethods(HOME));
            }

            childBeans = ddBean.getChildBean("./remote");                   //NOI18N
            if(childBeans.length > 0){
                //xpath - ejb-jar/enterprise-beans/entity/remote
                //System.out.println("xpath: " + childBeans[0].getXpath()); //NOI18N
                selectionToMethodsMap.put(REMOTE, getMethods(REMOTE));
            }

            childBeans = ddBean.getChildBean("./local-home");               //NOI18N
            if(childBeans.length > 0){
                //System.out.println("xpath: " + childBeans[0].getXpath());   //NOI18N
                selectionToMethodsMap.put(LOCAL_HOME, getMethods(LOCAL_HOME));
            }

            childBeans = ddBean.getChildBean("./local");                    //NOI18N
            if(childBeans.length > 0){
                //System.out.println("xpath: " + childBeans[0].getXpath());   //NOI18N
                selectionToMethodsMap.put(LOCAL, getMethods(LOCAL));
            }

        }
        return selectionToMethodsMap;
    }


    //Contruct a map of Interface Types to DD Method elements
    //Get the Method elements from sun DD and contruct this map
    private Map getSelectionToDDMethodsMap(){
        Map selectionToDDMethodsMap = new Hashtable();
        if(cmpEntityEjb != null){
            FlushAtEndOfMethod flushAtEndOfMethod = cmpEntityEjb.getFlushAtEndOfMethod();
            if(flushAtEndOfMethod != null){
                Method[] ddMethods = flushAtEndOfMethod.getMethod();
                if(ddMethods != null){
                    ArrayList homeMethods = null;
                    ArrayList remoteMethods = null;
                    ArrayList localMethods = null;
                    ArrayList localHomeMethods = null;

                    for(int i=0; i<ddMethods.length; i++){
                        String interfaceType = ddMethods[i].getMethodIntf();
                        if(HOME.equals(interfaceType)){
                            if(homeMethods == null){
                                homeMethods = new ArrayList();
                            }
                            homeMethods.add(ddMethods[i]);
                        }

                        if(REMOTE.equals(interfaceType)){
                            if(remoteMethods == null){
                                remoteMethods = new ArrayList();
                            }
                            remoteMethods.add(ddMethods[i]);
                        }

                        if(LOCAL_HOME.equals(interfaceType)){
                            if(localHomeMethods == null){
                                localHomeMethods = new ArrayList();
                            }
                            localHomeMethods.add(ddMethods[i]);
                        }

                        if(LOCAL.equals(interfaceType)){
                            if(localMethods == null){
                                localMethods = new ArrayList();
                            }
                            localMethods.add(ddMethods[i]);
                        }
                    }

                    if(homeMethods != null){
                        selectionToDDMethodsMap.put(HOME, homeMethods);
                    }
                    if(remoteMethods != null){
                        selectionToDDMethodsMap.put(REMOTE, remoteMethods);
                    }
                    if(localHomeMethods != null){
                        selectionToDDMethodsMap.put(LOCAL_HOME, localHomeMethods);
                    }
                    if(localMethods != null){
                        selectionToDDMethodsMap.put(LOCAL, localMethods);
                    }
                }
            }
        }
        return selectionToDDMethodsMap;
    }


    //get all the mehods in the given interface
    private List getMethods(String interfaceType){
        if(interfaceType.equals(LOCAL_HOME)){
            //XXX dummy impementation; replace by real one
            //returning a list of java.lang.reflect.Method objects
            //change later to return a list of real *Method* objects.
            //*Method* objects could be netbeans Method object
            ArrayList methods = new ArrayList();
            DummyMethod method = new DummyMethod();
            method.setName("FirstMethod");
            String[] params = {"int"};
            method.setParameterTypes(params);
            methods.add(method);

            DummyMethod method2 = new DummyMethod();
            method2.setName("SecondMethod");
            String[] params2 = {"int", "String"};
            method2.setParameterTypes(params2);
            methods.add(method2);
            return methods;
        }else{
            ArrayList methods = new ArrayList();
            DummyMethod method = new DummyMethod();
            method.setName("FirstMethod");
            String[] params = {"String"};
            method.setParameterTypes(params);
            methods.add(method);
            return methods;
        }
    }
    
    public void addTableModelListener(TableModelListener listner){
        model.addTableModelListener(listner);
    }
}
