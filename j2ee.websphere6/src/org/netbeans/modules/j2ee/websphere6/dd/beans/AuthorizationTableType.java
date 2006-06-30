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
package org.netbeans.modules.j2ee.websphere6.dd.beans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class AuthorizationTableType extends org.netbeans.modules.schema2beans.BaseBean implements DDXmiConstants {

    static Vector comparators = new Vector();
    static private final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);
    public AuthorizationTableType() {
        this(Common.USE_DEFAULT_VALUES);
    }
    
    public AuthorizationTableType(int options) {
        super(comparators, runtimeVersion);
        // Properties (see root bean comments for the bean graph)
        initPropertyTables(1);
        this.createProperty(AUTHORIZATIONS,
                AUTHORIZATION,
                Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY,
                AuthorizationsType.class);
        this.createAttribute(AUTHORIZATION, XMI_ID_ID, AUTH_ID,
                AttrProp.CDATA | AttrProp.IMPLIED,
                null, null);
        this.initialize(options);
    }
    
    // Setting the default values of the properties
    void initialize(int options) {
        
    }
    
    // This attribute is an array, possibly empty
    public void setAuthorization(int index, AuthorizationsType value) {
        this.setValue(AUTHORIZATION, index, value);
    }
    
    //
    public AuthorizationsType getAuthorization(int index) {
        return (AuthorizationsType)this.getValue(AUTHORIZATION, index);
    }
    
    // Return the number of properties
    public int sizeAuthorizations() {
        return this.size(AUTHORIZATION);
    }
    
    // This attribute is an array, possibly empty
    public void setAuthorization(AuthorizationsType[] value) {
        this.setValue(AUTHORIZATION, value);
    }
    
    //
    public AuthorizationsType[] getAuthorizations() {
        return (AuthorizationsType[])this.getValues(AUTHORIZATION);
    }
    
    // Add a new element returning its index in the list
    public int addAuthorization(AuthorizationsType value) {
        int positionOfNewItem = this.addValue(AUTHORIZATION, value);
        return positionOfNewItem;
    }
    
    //
    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removeAuthorization(AuthorizationsType value) {
        return this.removeValue(AUTHORIZATION, value);
    }
    
    /**
     * Create a new bean using it's default constructor.
     * This does not add it to any bean graph.
     */
    public AuthorizationsType newApplicationType() {
        return new AuthorizationsType();
    }
    
    
    //
    public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.add(c);
    }
    
    //
    public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.remove(c);
    }
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
        boolean restrictionFailure = false;
        boolean restrictionPassed = false;
        // Validating property parameter
        for (int _index = 0; _index < sizeAuthorizations(); ++_index) {
            AuthorizationsType element = getAuthorization(_index);
            if (element != null) {
                element.validate();
            }
        }
       
    }
    
    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent){
        String s;
        Object o;
        org.netbeans.modules.schema2beans.BaseBean n;
        str.append(indent);
        str.append("Authorizations["+this.sizeAuthorizations()+"]");	// NOI18N
        for(int i=0; i<this.sizeAuthorizations(); i++) {
            str.append(indent+"\t");
            str.append("#"+i+":");
            n = (org.netbeans.modules.schema2beans.BaseBean) this.getAuthorization(i);
            if (n != null)
                n.dump(str, indent + "\t");	// NOI18N
            else
                str.append(indent+"\tnull");	// NOI18N
            this.dumpAttributes(AUTHORIZATION, i, str, indent);
        }
        
    }
    public String dumpBeanNode(){
        StringBuffer str = new StringBuffer();
        str.append(getClass().getName());	// NOI18N
        this.dump(str, "\n  ");	// NOI18N
        return str.toString();
    }}

// END_NOI18N

