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
package org.netbeans.modules.vmd.api.model.utils;

import org.netbeans.modules.vmd.api.model.PrimitiveDescriptor;
import org.netbeans.modules.vmd.api.model.PrimitiveDescriptorFactory;

import static org.netbeans.modules.vmd.api.model.utils.TestTypes.*;


public final class TestPrimitiveDescriptor implements PrimitiveDescriptorFactory {

    private static final IntPD intPD = new IntPD();
    private static final LongPD longPD = new LongPD();
    private static final StringPD stringPD = new StringPD();
    private static final BooleanPD booleanPD = new BooleanPD();
    
    public PrimitiveDescriptor getDescriptorForTypeIDString(String string) {
        if (TYPEID_INT.getString().equals(string))
            return intPD;
        if (TYPEID_LONG.getString().equals(string))
            return longPD;
        if (TYPEID_BOOLEAN.getString().equals(string))
            return booleanPD;
        if (TYPEID_JAVA_LANG_STRING.getString().equals(string))
            return stringPD;
        if (TYPEID_JAVA_CODE.getString().equals(string))
            return stringPD;
        //TODO
        return null;
    }

    public String getProjectType() {
        return "PrimitiveDescriptorTest"; //NOI18N
    }
    
    private static class IntPD implements PrimitiveDescriptor {
        
        public String serialize(Object value) {
            return value.toString();
        }
        
        public Object deserialize(String serialized) {
            return Integer.parseInt(serialized);
        }
        
        public boolean isValidInstance(Object object) {
            return object instanceof Integer;
        }
        
    }
    
    private static class LongPD implements PrimitiveDescriptor {
        
        public String serialize(Object value) {
            return value.toString();
        }
        
        public Object deserialize(String serialized) {
            return Long.parseLong(serialized);
        }
        
        public boolean isValidInstance(Object object) {
            return object instanceof Long;
        }
        
    }
    
    private static class BooleanPD implements PrimitiveDescriptor {
        
        public String serialize(Object value) {
            return value.toString();
        }
        
        public Object deserialize(String serialized) {
            return Integer.parseInt(serialized);
        }
        
        public boolean isValidInstance(Object object) {
            return object instanceof Boolean;
        }
        
    }
    
    private static class StringPD implements PrimitiveDescriptor {
        
        public String serialize(Object value) {
            return (String) value;
        }
        
        public Object deserialize(String serialized) {
            return serialized;
        }
        
        public boolean isValidInstance(Object object) {
            return object instanceof String;
        }
    }
}
