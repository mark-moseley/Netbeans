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

package org.netbeans.modules.mobility.e2e.classdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Michal Skvor
 */
public class ClassData {
    
    private String packageName;
    private String className;
    private boolean primitive;
    private boolean array;

    private ClassData parent;
    
    private List<FieldData> fields;
    private List<MethodData> methods;

    private ClassData componentType;
    
    public static final ClassData java_lang_Object = new ClassData( "java.lang", "Object", false, false );
    
    public ClassData( String packageName, String className, boolean primitive, boolean array ) {
        this.packageName = packageName;
        this.className = className;
        this.primitive = primitive;
        this.array = array;

        this.fields = new ArrayList();
        this.methods = new ArrayList();
        
        parent = java_lang_Object;
    }

    public ClassData( String packageName, String className, boolean primitive, boolean array, 
            List<FieldData> fields, List<MethodData> methods ) 
    {
        this( packageName, className, primitive, array );
        this.fields = fields;
        this.methods = methods;
    }

    public String getPackage() {
        return packageName;
    }
    
    public String getClassName() {
        return className;
    }
    
    public String getName() {
        int arrayDepth = 0;
        ClassData t = this;
        while( t.isArray()) {
            t = t.getComponentType();
            arrayDepth++;
        }
        String arrayBrackets = "";
        if( arrayDepth > 0 ) {
            for( int i = 0; i < arrayDepth; i++ ) {
                arrayBrackets += "[]";
            }
        }
        return className + arrayBrackets;
    }
    
    /**
     * Return fully qualified name of the ClassData
     * 
     * @return fully qualified name
     */
    public String getFullyQualifiedName() {
        if( packageName == "" ) {
            return className;
        }
        return packageName + "." + className;
    }
    
    public void setParent( ClassData parent ) {
        this.parent = parent;
    }
    
    public ClassData getParent() {
        return parent;
    }
    
    /**
     * Returns true when the ClassData structure represents
     * primitive type
     * 
     * @return true when the ClassData structure represents primitive type
     */
    public boolean isPrimitive() {
        return primitive;
    }

    public boolean isArray() {
        return array;
    }

    public void setComponentType( ClassData type ) {
        this.componentType = type;
    }
    
    public ClassData getComponentType() {
        return componentType;
    }

    public void addField( FieldData field ) {
        fields.add( field );
    }

    public List<FieldData> getFields() {
        return Collections.unmodifiableList( fields );
    }

    public void addMethod( MethodData method ) {
        methods.add( method );
    }

    public List<MethodData> getMethods() {
        return Collections.unmodifiableList( methods );
    }

    public String toString() {
        String result = getName() + " ";
        if( fields.size() > 0 ) {
            result += "[";
            for( FieldData field : fields ) {
                result += field.getType().toString() + ", ";
            }
            result += "]";                
        }
        return result;
    }    
    
    public static enum Modifier {
        PUBLIC, PRIVATE
    }    
    
    @Override
    public boolean equals( Object o ) {
        if( o instanceof ClassData ) {
            ClassData cd = (ClassData) o;
            if( !getFullyQualifiedName().equals( cd.getFullyQualifiedName())) return false;
            if( primitive != cd.isPrimitive()) return false;
            if( array != cd.isArray()) return false;
            
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return packageName.hashCode() * 37 +
            className.hashCode() * 37 +
            ( primitive ? 7 : 3 ) + 
            ( array ? 7 : 3 );
    }
}
