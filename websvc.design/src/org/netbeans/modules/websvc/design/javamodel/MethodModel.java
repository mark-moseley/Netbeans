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

package org.netbeans.modules.websvc.design.javamodel;

import java.util.List;

/**
 *
 * @author mkuchtiak
 */
public class MethodModel {
    
    private String operationName;
    private String action;
    private ResultModel result;
    private List<ParamModel> params;
    private boolean oneWay;
    private JavadocModel javadoc;
    private List<FaultModel> faults;
    
    /** Creates a new instance of MethodModel */
    MethodModel(String operationName) {
        this.operationName=operationName;
    }
    /** Creates a new instance of MethodModel */
    MethodModel() {
    }
    
    public String getOperationName() {
        return operationName;
    }
    
    void setOperationName(String operationName) {
        this.operationName=operationName;
    }

    public ResultModel getResult() {
        return result;
    }

    void setResult(ResultModel result) {
        this.result = result;
    }

    public String getAction() {
        return action;
    }

    void setAction(String action) {
        this.action = action;
    }

    public List<ParamModel> getParams() {
        return params;
    }

    void setParams(List<ParamModel> params) {
        this.params = params;
    }

    public boolean isOneWay() {
        return oneWay;
    }

    void setOneWay(boolean oneWay) {
        this.oneWay = oneWay;
    }

    public JavadocModel getJavadoc() {
        return javadoc;
    }
    
    void setJavadoc(JavadocModel javadoc) {
        this.javadoc=javadoc;
    }
    
    public List<FaultModel> getFaults() {
        return faults;
    }
    
    void setFaults(List<FaultModel> faults) {
        this.faults=faults;
    }
    
    public boolean isEqualTo(MethodModel model) {
        if (!operationName.equals(model.operationName)) return false;
        if (!result.isEqualTo(model.result)) return false;
        if (oneWay!=model.oneWay) return false;
        if (!Utils.isEqualTo(action, model.action)) return false;
        if (javadoc!=null) {
            if (!javadoc.isEqualTo(model.javadoc)) return false;
        } else if (model.javadoc!=null) return false;
        if (params.size()!=model.params.size()) return false;
        for(int i = 0;i<params.size();i++) {
            if (!params.get(i).isEqualTo(model.params.get(i))) return false;
        }
        if (faults.size()!=model.faults.size()) return false;
        for(int i = 0;i<faults.size();i++) {
            if (!faults.get(i).isEqualTo(model.faults.get(i))) return false;
        }
        return true;
    }

}
