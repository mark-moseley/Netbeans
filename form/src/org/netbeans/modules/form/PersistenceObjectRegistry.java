/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.form;

import java.util.Map;
import java.util.HashMap;
import org.openide.util.Utilities;

/**
 *
 * @author Tran Duc Trung
 */

public class PersistenceObjectRegistry
{

    private static Map _nameToClassname = new HashMap();
    private static Map _classToPrimaryName = new HashMap();

    private PersistenceObjectRegistry() {
    }

    public static void registerPrimaryName(String classname, String name) {
        _classToPrimaryName.put(classname, name);
        _nameToClassname.put(name, classname);
    }

    public static void registerPrimaryName(Class clazz, String name) {
        _classToPrimaryName.put(clazz.getName(), name);
        _nameToClassname.put(name, clazz.getName());
    }

    public static void registerAlias(String classname, String alias) {
        _nameToClassname.put(alias, classname);
    }

    public static void registerAlias(Class clazz, String alias) {
        _nameToClassname.put(alias, clazz.getName());
    }

    public static Object createInstance(String name)
        throws InstantiationException, IllegalAccessException, ClassNotFoundException
    {
        return loadClass(name).newInstance();
    }

    public static Class loadClass(String name) throws ClassNotFoundException {
        name = Utilities.translate(name);
        String classname =(String) _nameToClassname.get(name);
        if (classname == null)
            classname = name;
        return FormUtils.getClassLoader().loadClass(classname);
    }

    public static String getPrimaryName(Object instance) {
        return getPrimaryName(instance.getClass());
    }

    public static String getPrimaryName(Class clazz) {
        return getPrimaryName(clazz.getName());
    }

    static String getPrimaryName(String className) {
        String name = (String) _classToPrimaryName.get(className);
        return name != null ? name : className;
    }

    static String getClassName(String primaryName) {
        primaryName = Utilities.translate(primaryName);
        String classname = (String) _nameToClassname.get(primaryName);
        return classname != null ? classname : primaryName;
    }

//    static {
        //      registerPrimaryName("org.netbeans.modules.form.compat2.layouts.DesignBorderLayout$ConstraintsDesc",
        //                          "org.netbeans.modules.form.compat2.layouts.DesignBorderLayout$BorderConstraintsDescription");
//    }
}
