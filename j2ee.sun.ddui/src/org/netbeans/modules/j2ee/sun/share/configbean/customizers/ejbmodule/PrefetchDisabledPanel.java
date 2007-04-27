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
 * PrefetchDisabledPanel.java
 *
 * Created on February 24, 2005, 12:55 AM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.event.TableModelListener;

import org.netbeans.modules.j2ee.sun.share.configbean.ConfigQuery;
import org.netbeans.modules.j2ee.sun.share.configbean.CmpEntityEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.MethodTableModel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.MethodTablePanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.CmpEntityEjbCustomizer;

/**
 *
 * @author Rajeshwar Patil
 */
public class PrefetchDisabledPanel extends MethodTablePanel {

    private CmpEntityEjb cmpEntityEjb;
    private CmpEntityEjbCustomizer customizer;
    
    /** Creates a new instance of PrefetchDisabledPanel */
    public PrefetchDisabledPanel() {
    }


    private PrefetchDisabledModel model;

    static final ResourceBundle bundle = 
        ResourceBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.Bundle"); // NOI18N


    /** Creates a new instance of PrefetchDisabledPanel */
    public PrefetchDisabledPanel(CmpEntityEjb cmpEntityEjb, CmpEntityEjbCustomizer customizer) {
        this.cmpEntityEjb = cmpEntityEjb;
        this.customizer = customizer;
    }

    protected java.awt.GridBagConstraints getTableGridBagConstraints(){
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        return gridBagConstraints;
    }

    protected MethodTableModel getMethodTableModel(){
        List methodList = getMethodList();
        List prefetchedMethodList = getPrefetchedMethodList();
        //printList(methodList);
        //printList(prefetchedMethodList);
        model = new PrefetchDisabledModel(cmpEntityEjb, methodList, prefetchedMethodList);
        model.addTableModelListener((TableModelListener)customizer);
        return model;
    }


    protected String getTablePaneAcsblName(){
        return bundle.getString("Prefetched_Method_Acsbl_Name");        //NOI18N
    }


    protected String getTablePaneAcsblDesc(){
        return bundle.getString("Prefetched_Method_Acsbl_Desc");        //NOI18N
    }


    protected String getTablePaneToolTip(){
        return bundle.getString("Prefetched_Method_Tool_Tip");          //NOI18N
    }


    protected String getToolTip(int row, int col){
        if(col == 0){
            ConfigQuery.MethodData method = (ConfigQuery.MethodData) getMethodList().get(row);
            if(method != null){
                return getMethodSignature(method);
            }
        }
        return null;
    }


    protected String getToolTip(int col){
        if(col == 0){
            return bundle.getString("Finder_Method");                   //NOI18N
        }
        if(col == 1){
            return bundle.getString("Disable_Prefetching");             //NOI18N
        }
        return null;
    }


    public void setData(CmpEntityEjb cmpEntityEjb){
        this.cmpEntityEjb = cmpEntityEjb;
        List methodList = getMethodList();
        List prefetchedMethodList = getPrefetchedMethodList();
        model.setData(cmpEntityEjb, methodList, prefetchedMethodList);
        setData();
    }


    private List getMethodList(){
        //List of all the finder methods of cmp bean
        if(cmpEntityEjb != null){
            return cmpEntityEjb.getFinderMethods();
        }
        return null;
    }


    private List getPrefetchedMethodList(){
        //List of all the QueryMethod elements(elements from DD)
        if(cmpEntityEjb != null){
            return cmpEntityEjb.getPrefetchedMethods();
        }
        return null;
    }


    public void addTableModelListener(TableModelListener listner){
        model.addTableModelListener(listner);
    }


    protected JPanel getPanel() {
        return null;
    }


    private void printList(List list){
       if(list != null){
           System.out.println("printList list " + list);                                             //NOI18N
           System.out.println("printList list toString -- " + list.toString());                      //NOI18N
           int size = list.size();
           for(int i=0; i<size; i++){
               System.out.println("printList item no: i -- " + list.get(i));                         //NOI18N
               System.out.println("printList item no: i toSring() -- " + list.get(i).toString());    //NOI18N
           }
       }
    }


    private String getMethodSignature(ConfigQuery.MethodData method){
        String signature = method.getOperationName();
        if(signature != null){
            StringBuilder signatureBuilder = new StringBuilder(150);
            signatureBuilder.append(signature);
            signatureBuilder.append('('); //NOI18N
            List params = method.getParameters();
            if(params != null){
                for(int i=0; i<params.size(); i++){
                    if(i > 0 ){
                        signatureBuilder.append(','); //NOI18N
                    }
                    signatureBuilder.append(params.get(i)); //NOI18N
                }
            }
            signatureBuilder.append(')'); //NOI18N
            signature = signatureBuilder.toString();
        }
        return signature;
    }
}
