/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.debugger.jpda.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.WeakHashMap;

import org.netbeans.api.debugger.Breakpoint;
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
public class BreakpointsReader implements Properties.Reader, PropertyChangeListener {
    
    private Map<JPDABreakpoint, String> cachedClassNames = new WeakHashMap<JPDABreakpoint, String>();
    private Map<JPDABreakpoint, String> cachedSourceRoots = new WeakHashMap<JPDABreakpoint, String>();
    
    
    public String [] getSupportedClassNames () {
        return new String[] {
            JPDABreakpoint.class.getName (), 
        };
    }
    
    synchronized String findCachedClassName(JPDABreakpoint b) {
        return cachedClassNames.get(b);
    }
    
    synchronized String findCachedSourceRoot(JPDABreakpoint b) {
        return cachedSourceRoots.get(b);
    }
    
    void storeCachedClassName(JPDABreakpoint b, String className) {
        synchronized (this) {
            if (b instanceof LineBreakpoint && !cachedClassNames.containsKey(b)) {
                // Line breakpoint, class name is cached for the first time.
                // We need to listen on URL changes and clear the cache then.
                b.addPropertyChangeListener(LineBreakpoint.PROP_URL, this);
            }
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
            lb.setPreferredClassName(
                properties.getString(LineBreakpoint.PROP_PREFERRED_CLASS_NAME, null)
            );
            synchronized (this) {
                cachedClassNames.put(lb, properties.getString("className", null));
                // We need to listen on URL changes and clear the cache then.
                lb.addPropertyChangeListener(LineBreakpoint.PROP_URL, this);
                cachedSourceRoots.put(lb, properties.getString("sourceRoot", null));
            }
            b = lb;
        }
        if (typeID.equals (MethodBreakpoint.class.getName ()) ||
                typeID.equals (MethodBreakpoint.class.getName ()+"$MethodBreakpointImpl")) {
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
            mb.setMethodSignature(
                properties.getString (MethodBreakpoint.PROP_METHOD_SIGNATURE, null)
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
            String[] classFilters = (String[]) properties.getArray(ExceptionBreakpoint.PROP_CLASS_FILTERS, null);
            if (classFilters != null) {
                eb.setClassFilters(classFilters);
            }
            String[] classExclusionFilters = (String[]) properties.getArray(ExceptionBreakpoint.PROP_CLASS_EXCLUSION_FILTERS, null);
            if (classExclusionFilters != null) {
                eb.setClassExclusionFilters(classExclusionFilters);
            }
            synchronized (this) {
                cachedSourceRoots.put(eb, properties.getString("sourceRoot", null));
            }
            b = eb;
        }
        if (typeID.equals (FieldBreakpoint.class.getName ()) ||
                typeID.equals (FieldBreakpoint.class.getName ()+"$FieldBreakpointImpl")) {
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
        int hitCountFilter = properties.getInt(JPDABreakpoint.PROP_HIT_COUNT_FILTER, 0);
        Breakpoint.HIT_COUNT_FILTERING_STYLE hitCountFilteringStyle;
        if (hitCountFilter > 0) {
            hitCountFilteringStyle = Breakpoint.HIT_COUNT_FILTERING_STYLE.values()
                    [properties.getInt(JPDABreakpoint.PROP_HIT_COUNT_FILTER+"_style", 0)]; // NOI18N
        } else {
            hitCountFilteringStyle = null;
        }
        b.setHitCountFilter(hitCountFilter, hitCountFilteringStyle);
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
        properties.setInt(JPDABreakpoint.PROP_HIT_COUNT_FILTER, b.getHitCountFilter());
        Breakpoint.HIT_COUNT_FILTERING_STYLE style = b.getHitCountFilteringStyle();
        properties.setInt(JPDABreakpoint.PROP_HIT_COUNT_FILTER+"_style", style != null ? style.ordinal() : 0); // NOI18N
        
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
            properties.setString(
                LineBreakpoint.PROP_PREFERRED_CLASS_NAME,
                lb.getPreferredClassName()
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
                MethodBreakpoint.PROP_METHOD_SIGNATURE, 
                mb.getMethodSignature()
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
            properties.setArray(
                    ExceptionBreakpoint.PROP_CLASS_FILTERS,
                    eb.getClassFilters());
            properties.setArray(
                    ExceptionBreakpoint.PROP_CLASS_EXCLUSION_FILTERS,
                    eb.getClassExclusionFilters());
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

    public void propertyChange(PropertyChangeEvent evt) {
        if (LineBreakpoint.PROP_URL.equals(evt.getPropertyName())) {
            LineBreakpoint lb = (LineBreakpoint) evt.getSource();
            storeCachedClassName(lb, null);
        }
    }
    
}
