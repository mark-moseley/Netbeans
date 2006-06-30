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

package org.netbeans.modules.debugger.jpda.breakpoints;

import java.util.Map;
import java.util.WeakHashMap;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.ExceptionBreakpoint;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ThreadBreakpoint;


/**
 *
 * @author Jan Jancura
 */
public class BreakpointsReader implements Properties.Reader {
    
    private Map cachedClassNames = new WeakHashMap();
    private Map cachedSourceRoots = new WeakHashMap();
    
    
    public String [] getSupportedClassNames () {
        return new String[] {
            JPDABreakpoint.class.getName (), 
        };
    }
    
    synchronized String findCachedClassName(JPDABreakpoint b) {
        return (String) cachedClassNames.get(b);
    }
    
    synchronized String findCachedSourceRoot(JPDABreakpoint b) {
        return (String) cachedSourceRoots.get(b);
    }
    
    void storeCachedClassName(JPDABreakpoint b, String className) {
        synchronized (this) {
            cachedClassNames.put(b, className);
        }
        PersistenceManager.storeBreakpoints();
    }
    
    void storeCachedSourceRoot(JPDABreakpoint b, String sourceRoot) {
        synchronized (this) {
            cachedSourceRoots.put(b, sourceRoot);
        }
        PersistenceManager.storeBreakpoints();
    }
    
    public Object read (String typeID, Properties properties) {
        JPDABreakpoint b = null;
        // Read both LineBreakpoint and LineBreakpoint$LineBreakpointImpl
        if (typeID.equals (LineBreakpoint.class.getName ()) ||
                typeID.equals (LineBreakpoint.class.getName ()+"$LineBreakpointImpl")) {
            LineBreakpoint lb = LineBreakpoint.create (
                properties.getString (LineBreakpoint.PROP_URL, null),
                properties.getInt (LineBreakpoint.PROP_LINE_NUMBER, 1)
            );
            lb.setCondition (
                properties.getString (LineBreakpoint.PROP_CONDITION, "")
            );
            synchronized (this) {
                cachedClassNames.put(lb, properties.getString("className", null));
                cachedSourceRoots.put(lb, properties.getString("sourceRoot", null));
            }
            b = lb;
        }
        if (typeID.equals (MethodBreakpoint.class.getName ())) {
            MethodBreakpoint mb = MethodBreakpoint.create ();
            mb.setClassFilters (
                (String[]) properties.getArray (
                    MethodBreakpoint.PROP_CLASS_FILTERS, 
                    new String [0]
                )
            );
            mb.setClassExclusionFilters (
                (String[]) properties.getArray (
                    MethodBreakpoint.PROP_CLASS_EXCLUSION_FILTERS, 
                    new String [0]
                )
            );
            mb.setMethodName (
                properties.getString (MethodBreakpoint.PROP_METHOD_NAME, "")
            );
            mb.setCondition (
                properties.getString (MethodBreakpoint.PROP_CONDITION, "")
            );
            mb.setBreakpointType (
                properties.getInt (
                    MethodBreakpoint.PROP_BREAKPOINT_TYPE, 
                    MethodBreakpoint.TYPE_METHOD_ENTRY
                )
            );
            synchronized (this) {
                cachedSourceRoots.put(mb, properties.getString("sourceRoot", null));
            }
            b = mb;
        }
        if (typeID.equals (ClassLoadUnloadBreakpoint.class.getName ())) {
            ClassLoadUnloadBreakpoint cb = ClassLoadUnloadBreakpoint.create (
                properties.getInt (
                    ClassLoadUnloadBreakpoint.PROP_BREAKPOINT_TYPE, 
                    ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED
                )
            );
            cb.setClassFilters (
                (String[]) properties.getArray (
                    ClassLoadUnloadBreakpoint.PROP_CLASS_FILTERS, 
                    new String [0]
                )
            );
            cb.setClassExclusionFilters (
                (String[]) properties.getArray (
                    ClassLoadUnloadBreakpoint.PROP_CLASS_EXCLUSION_FILTERS, 
                    new String [0]
                )
            );
            synchronized (this) {
                cachedSourceRoots.put(cb, properties.getString("sourceRoot", null));
            }
            b = cb;
        }
        if (typeID.equals (ExceptionBreakpoint.class.getName ())) {
            ExceptionBreakpoint eb = ExceptionBreakpoint.create (
                properties.getString (
                    ExceptionBreakpoint.PROP_EXCEPTION_CLASS_NAME, 
                    null
                ),
                properties.getInt (
                    ExceptionBreakpoint.PROP_CATCH_TYPE, 
                    ExceptionBreakpoint.TYPE_EXCEPTION_CATCHED_UNCATCHED
                )
            );
            eb.setCondition (
                properties.getString (ExceptionBreakpoint.PROP_CONDITION, "")
            );
            synchronized (this) {
                cachedSourceRoots.put(eb, properties.getString("sourceRoot", null));
            }
            b = eb;
        }
        if (typeID.equals (FieldBreakpoint.class.getName ())) {
            FieldBreakpoint fb = FieldBreakpoint.create (
                properties.getString (FieldBreakpoint.PROP_CLASS_NAME, null),
                properties.getString (FieldBreakpoint.PROP_FIELD_NAME, null),
                properties.getInt (
                    FieldBreakpoint.PROP_BREAKPOINT_TYPE, 
                    FieldBreakpoint.TYPE_ACCESS
                )
            );
            fb.setCondition (
                properties.getString (FieldBreakpoint.PROP_CONDITION, "")
            );
            synchronized (this) {
                cachedSourceRoots.put(fb, properties.getString("sourceRoot", null));
            }
            b = fb;
        }
        if (typeID.equals (ThreadBreakpoint.class.getName ())) {
            ThreadBreakpoint tb = ThreadBreakpoint.create (
            );
            tb.setBreakpointType (
                properties.getInt (
                    ThreadBreakpoint.PROP_BREAKPOINT_TYPE, 
                    ThreadBreakpoint.TYPE_THREAD_STARTED_OR_DEATH
                )
            );
            b = tb;
        }
        assert b != null: "Unknown breakpoint type: \""+typeID+"\"";
        b.setPrintText (
            properties.getString (JPDABreakpoint.PROP_PRINT_TEXT, "")
        );
        b.setGroupName(
            properties.getString (JPDABreakpoint.PROP_GROUP_NAME, "")
        );
        b.setSuspend (
            properties.getInt (
                JPDABreakpoint.PROP_SUSPEND, 
                JPDABreakpoint.SUSPEND_ALL
            )
        );
        if (properties.getBoolean (JPDABreakpoint.PROP_ENABLED, true))
            b.enable ();
        else
            b.disable ();
        return b;
    }
    
    public void write (Object object, Properties properties) {
        JPDABreakpoint b = (JPDABreakpoint) object;
        properties.setString (
            JPDABreakpoint.PROP_PRINT_TEXT, 
            b.getPrintText ()
        );
        properties.setString (
            JPDABreakpoint.PROP_GROUP_NAME, 
            b.getGroupName ()
        );
        properties.setInt (JPDABreakpoint.PROP_SUSPEND, b.getSuspend ());
        properties.setBoolean (JPDABreakpoint.PROP_ENABLED, b.isEnabled ());
        
        if (object instanceof LineBreakpoint) {
            LineBreakpoint lb = (LineBreakpoint) object;
            properties.setString (LineBreakpoint.PROP_URL, lb.getURL ());
            properties.setInt (
                LineBreakpoint.PROP_LINE_NUMBER, 
                lb.getLineNumber ()
            );
            properties.setString (
                LineBreakpoint.PROP_CONDITION, 
                lb.getCondition ()
            );
            properties.setString("className", findCachedClassName(lb));
            properties.setString("sourceRoot", findCachedSourceRoot(lb));
            return;
        } else 
        if (object instanceof MethodBreakpoint) {
            MethodBreakpoint mb = (MethodBreakpoint) object;
            properties.setArray (
                MethodBreakpoint.PROP_CLASS_FILTERS, 
                mb.getClassFilters ()
            );
            properties.setArray (
                MethodBreakpoint.PROP_CLASS_EXCLUSION_FILTERS, 
                mb.getClassExclusionFilters ()
            );
            properties.setString (
                MethodBreakpoint.PROP_METHOD_NAME, 
                mb.getMethodName ()
            );
            properties.setString (
                MethodBreakpoint.PROP_CONDITION, 
                mb.getCondition ()
            );
            properties.setInt (
                MethodBreakpoint.PROP_BREAKPOINT_TYPE, 
                mb.getBreakpointType ()
            );
            properties.setString("sourceRoot", findCachedSourceRoot(mb));
            return;
        } else 
        if (object instanceof ClassLoadUnloadBreakpoint) {
            ClassLoadUnloadBreakpoint cb = (ClassLoadUnloadBreakpoint) object;
            properties.setArray (
                ClassLoadUnloadBreakpoint.PROP_CLASS_FILTERS, 
                cb.getClassFilters ()
            );
            properties.setArray (
                ClassLoadUnloadBreakpoint.PROP_CLASS_EXCLUSION_FILTERS, 
                cb.getClassExclusionFilters ()
            );
            properties.setInt (
                ClassLoadUnloadBreakpoint.PROP_BREAKPOINT_TYPE, 
                cb.getBreakpointType ()
            );
            properties.setString("sourceRoot", findCachedSourceRoot(cb));
            return;
        } else 
        if (object instanceof ExceptionBreakpoint) {
            ExceptionBreakpoint eb = (ExceptionBreakpoint) object;
            properties.setString (
                ExceptionBreakpoint.PROP_EXCEPTION_CLASS_NAME, 
                eb.getExceptionClassName ()
            );
            properties.setInt (
                ExceptionBreakpoint.PROP_CATCH_TYPE, 
                eb.getCatchType ()
            );
            properties.setString (
                ExceptionBreakpoint.PROP_CONDITION, 
                eb.getCondition ()
            );
            properties.setString("sourceRoot", findCachedSourceRoot(eb));
            return;
        } else 
        if (object instanceof FieldBreakpoint) {
            FieldBreakpoint fb = (FieldBreakpoint) object;
            properties.setString (
                FieldBreakpoint.PROP_CLASS_NAME, 
                fb.getClassName ()
            );
            properties.setString (
                FieldBreakpoint.PROP_FIELD_NAME, 
                fb.getFieldName ()
            );
            properties.setString (
                FieldBreakpoint.PROP_CONDITION, 
                fb.getCondition ()
            );
            properties.setInt (
                FieldBreakpoint.PROP_BREAKPOINT_TYPE, 
                fb.getBreakpointType ()
            );
            properties.setString("sourceRoot", findCachedSourceRoot(fb));
            return;
        } else 
        if (object instanceof ThreadBreakpoint) {
            ThreadBreakpoint tb = (ThreadBreakpoint) object;
            properties.setInt (
                ThreadBreakpoint.PROP_BREAKPOINT_TYPE, 
                tb.getBreakpointType ()
            );
            return;
        }
        return;
    }
}
