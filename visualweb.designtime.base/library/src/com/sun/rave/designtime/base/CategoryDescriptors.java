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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.sun.rave.designtime.base;

import com.sun.rave.designtime.CategoryDescriptor;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Defines the component property categories used by Creator design-time
 * implementations.
 */

public class CategoryDescriptors {
    
    private static final ResourceBundle bundle =
            ResourceBundle.getBundle("com.sun.rave.designtime.base.Bundle", // NOI18N
            Locale.getDefault(),
            CategoryDescriptors.class.getClassLoader());
    
    public static final CategoryDescriptor ACCESSIBILITY = new CategoryDescriptor(
            bundle.getString("accessibility"), bundle.getString("accessibilityCatDesc"), false); //NOI18N
    
    public static final CategoryDescriptor ADVANCED = new CategoryDescriptor(
            bundle.getString("adv"), bundle.getString("advCatDesc"), false); //NOI18N
    
    public static final CategoryDescriptor APPEARANCE = new CategoryDescriptor(
            bundle.getString("appear"), bundle.getString("appearCatDesc"), true); //NOI18N
    
    public static final CategoryDescriptor BEHAVIOR = new CategoryDescriptor(
            bundle.getString("behavior"), bundle.getString("behaviorCatDesc"), false); //NOI18N
    
    public static final CategoryDescriptor DATA = new CategoryDescriptor(
            bundle.getString("data"), bundle.getString("dataCatDesc"), true); //NOI18N
    
    public static final CategoryDescriptor EVENTS = new CategoryDescriptor(
            bundle.getString("ev"), bundle.getString("evCatDesc"), true); //NOI18N
    
    public static final CategoryDescriptor GENERAL = new CategoryDescriptor(
            bundle.getString("gen"), bundle.getString("genCatDesc"), true); //NOI18N
    
    public static final CategoryDescriptor INTERNAL = new CategoryDescriptor(
            bundle.getString("intern"), bundle.getString("internCatDesc"), false); //NOI18N
    
    public static final CategoryDescriptor JAVASCRIPT = new CategoryDescriptor(
            bundle.getString("js"), bundle.getString("jsCatDesc"), false); //NOI18N
    
    public static final CategoryDescriptor LAYOUT = new CategoryDescriptor(
            bundle.getString("layout"), bundle.getString("layoutCatDesc"), false); //NOI18N
    
    public static final CategoryDescriptor NAVIGATION = new CategoryDescriptor(
            bundle.getString("navigation"), bundle.getString("navigationCatDesc"), false); //NOI18N
    
    private static CategoryDescriptor defaultCategoryDescriptors[] = {
        GENERAL, APPEARANCE, LAYOUT, DATA, EVENTS, NAVIGATION, BEHAVIOR, ACCESSIBILITY,
        JAVASCRIPT, ADVANCED, INTERNAL
    };
    
    protected static HashMap categoryHash;
    
    public static CategoryDescriptor getCategoryDescriptor(String categoryName) {
        Object[] pair;

        if (categoryName == null) {
            return null;
        }
        pair = (Object[])categoryHash.get(categoryName.toLowerCase());
        if (pair == null) {
            pair = new Object[] {
                null, new CategoryDescriptor(categoryName, "")}; //NOI18N
            categoryHash.put(categoryName, pair);
        }
        return (CategoryDescriptor)pair[1];
    }

    public static String getCategoryDescriptorConstantName(String categoryName) {
        Object[] pair;

        if (categoryName == null) {
            return null;
        }
        pair = (Object[])categoryHash.get(categoryName.toLowerCase());
        if (pair == null) {
            return null;
        }
        return (String)pair[0];
    }
    
    /**
     * <p>Return an array of <code>CategoryDescriptor</code> instances
     * describing the property categories supported by this library.</p>
     */
    public static CategoryDescriptor[] getDefaultCategoryDescriptors() {
        return defaultCategoryDescriptors;
    }
    
    
}
