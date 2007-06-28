
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
 * ParamModel.java
 *
 * Created on March 23, 2007, 3:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.design.view.actions;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;

/**
 *
 * @author mkuchtiak
 */
public class ParamModel {
        private String paramName;
        private ReferenceableSchemaComponent paramType;
        
        ParamModel(String paramName) {
            this.paramName=paramName;
        }
        
        ParamModel() {
        }
        
        public void setParamName(String paramName) {
            this.paramName = paramName;
        }        

        public String getParamName() {
            return paramName;
        }
        
        public void setParamType(ReferenceableSchemaComponent paramType) {
            this.paramType = paramType;
        }
        
        public ReferenceableSchemaComponent getParamType() {
            return paramType;
        }
        
        public String getDisplayName() {
            return Utils.getDisplayName(paramType);
        }
        

    
}
