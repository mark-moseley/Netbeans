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

package org.openide.execution;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

import org.openide.filesystems.*;
import org.openide.*;
import org.openide.util.Lookup;

/**
 * Base class for scripting interpreters.
 * @author dstrupl
 */
public abstract class ScriptType extends org.openide.ServiceType {

    /** generated Serialized Version UID */
    private static final long serialVersionUID = 4893207884933024341L;
    
    /**
     * The script type can decide whether it will be able to execute
     * the given file object.
     * @param fo a file to test
     * @return true if the script can operate on this file
     */
    public abstract boolean acceptFileObject(FileObject fo);
    
    /**
     * Evaluate the script given in the form of a Reader.
     * @param r 
     * @param context
     * @return whatever is the result of the script. It can be null.
     */
    public abstract Object eval(java.io.Reader r, Context context) throws InvocationTargetException;
    
    /** Calls eval(Reader, Context) with getDefaultContext() as
     * the second argument.
     */
    public final Object eval(java.io.Reader r) throws InvocationTargetException {
        return eval(r, getDefaultContext());
    }
    
    /**
     * Evaluate the script given in the form of a string.
     * @param script
     * @param context
     * @return whatever is the result of the script. It can be null.
     */
    public abstract Object eval(String script, Context context) throws InvocationTargetException;
    
    /** Calls eval(String, Context) with getDefaultContext() as
     * the second argument.
     */
    public final Object eval(String script) throws InvocationTargetException {
        return eval(script, getDefaultContext());
    }
    
    /**
     * Execute the script given in the form of a Reader.
     * @param r the contents of the script
     * @param context the context in which to evaluate it
     * @return whatever is the result of the script. It can be null.
     */
    public abstract void exec(java.io.Reader r, Context context) throws InvocationTargetException;
    
    /** Calls exec(Reader, Context) with getDefaultContext() as
     * the second argument.
     */
    public final void exec(java.io.Reader r) throws InvocationTargetException {
        exec(r, getDefaultContext());
    }
    
    /**
     * Execute the script given in the form of a string.
     * @param script
     * @param context
     * @return whatever is the result of the script. It can be null.
     */
    public abstract void exec(String script, Context context) throws InvocationTargetException;
    
    /** Calls exec(String, Context) with getDefaultContext() as
     * the second argument.
     */
    public final void exec(String script) throws InvocationTargetException {
        exec(script, getDefaultContext());
    }

    /**
     * Adds variable with name to the variables known by the script type.
     * @param name the name for the newly created variable
     * @param value initial value variable value (can be null).
     */
    public abstract void addVariable(String name, Object value);
    
    /** Get all registered script types.
    * @return enumeration of <code>ScriptType</code>s
    * @deprecated Please use {@link org.openide.util.Lookup} instead.
    */
    public static java.util.Enumeration scriptTypes () {
        return Collections.enumeration(Lookup.getDefault().lookup(new Lookup.Template(ScriptType.class)).allInstances());
    }

    /** 
     * Find the script type implemented as a given class.
     * @param clazz the class of the script type looked for
     * @return the desired script type or <code>null</code> if it does not exist
    * @deprecated Please use {@link org.openide.util.Lookup} instead.
     */
    public static ScriptType find (Class clazz) {
        return (ScriptType)Lookup.getDefault().lookup(clazz);
    }

    /** 
     * Find the script type with requested name.
     * @param name (display) name of script type to find
     * @return the desired script type or <code>null</code> if it does not exist
     */
    public static ScriptType find (String name) {
        ServiceType t = ((ServiceType.Registry)Lookup.getDefault().lookup(ServiceType.Registry.class)).find (name);
        if (t instanceof ScriptType) {
            return (ScriptType)t;
        } else {
            return null;
        }
    }

    /** Get the default script type.
    * @return the default script type
    * @deprecated Probably meaningless, find all available types and filter with {@link #acceptFileObject} instead.
    */
    public static ScriptType getDefault () {
        java.util.Enumeration en = scriptTypes ();
        if (en.hasMoreElements()) {
            return (ScriptType)en.nextElement ();
        } else {
            throw new RuntimeException("No script type registered."); // NOI18N
        }
    }
    
    static Context getDefaultContext() {
        return new Context();
    }

    /** Scripting context.
     * Using instances of this class you can add additional parameters
     * to the script execution using methods eval or exec.
     */
    public static class Context {
    }    
}
