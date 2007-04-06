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

package org.netbeans.modules.form;

import java.lang.reflect.*;
import java.util.*;
import org.netbeans.modules.form.FormUtils.TypeHelper;

/**
 * Descriptor of binding property/one segment in the binding path.
 *
 * @author Jan Stola, Tomas Pavek.
 */
public class BindingDescriptor {
    /** Generified value type of the binding. */
    private TypeHelper genericValueType;
    /** Value type of the binding. */
    private Class valueType;
    /** Name of the binding property/path segment. */
    private String path;

    /** Display name of this binding. */
    private String propertyDisplayName;
    /** Short description of this binding. */
    private String propertyShortDescription;

    /**
     * Creates new <code>BindingDescriptor</code>.
     *
     * @param path name of the binding property/path segment.
     * @param genericValueType value type of the binding. 
     */
    public BindingDescriptor(String path, Type genericValueType) {
        this(path, new TypeHelper(genericValueType));
    }

    /**
     * Creates new <code>BindingDescriptor</code>.
     *
     * @param path name of the binding property/path segment.
     * @param genericValueType value type of the binding. 
     */    
    public BindingDescriptor(String path, TypeHelper genericValueType) {
        this.path = path;
        this.valueType = FormUtils.typeToClass(genericValueType);
        this.genericValueType = genericValueType;
    }

    /**
     * Returns generified value type of the binding. May return <code>null</code>
     * if the type of the binding depends on the context. In such a case the
     * type should be determined using BindingDesignSupport.determineType() method.
     *
     * @return generified value type of the binding or <code>null</code>.
     */
    public TypeHelper getGenericValueType() {
        return genericValueType;
    }

    /**
     * Returns value type of the binding.
     *
     * @return value type of the binding.
     */
    public Class getValueType() {
        return valueType;
    }

    /**
     * Returns name of the binding property/path segment.
     *
     * @return name of the binding property/path segment.
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns display name of this binding.
     *
     * @return display name of this binding.
     */
    public String getDisplayName() {
        return propertyDisplayName;
    }

    /**
     * Sets the display name of the binding.
     *
     * @param displayName display name of the binding.
     */
    public void setDisplayName(String displayName) {
        propertyDisplayName = displayName;
    }

    /**
     * Returns description of the binding.
     *
     * @return description of the binding.
     */
    public String getShortDescription() {
        return propertyShortDescription;
    }

    /**
     * Sets the description of the binding.
     *
     * @param description description of the binding.
     */
    public void setShortDescription(String description) {
        propertyShortDescription = description;
    }

    /**
     * Marks the value type of this binding as relative. Type of such a binding
     * may depend on the context and should be determined using
     * <code>BindingDesignSupport.determineType()</code> method.
     */
    void markTypeAsRelative() {
        genericValueType = null;
    }

    /**
     * Determines whether the value type of this binding depends on the context
     * and should be determined using <code>BindingDesignSupport.determineType()</code> method.
     * 
     * @return <code>true</code> if the value type is relative,
     * returns <code>false</code> otherwise.
     */
    boolean isValueTypeRelative() {
        return (genericValueType == null);
    }

}
