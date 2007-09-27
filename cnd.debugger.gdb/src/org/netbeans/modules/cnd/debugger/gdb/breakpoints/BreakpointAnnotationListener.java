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
 * Software is Sun Micro//S ystems, Inc. Portions Copyright 1997-2006 Sun
 * Micro//S ystems, Inc. All Rights Reserved.
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

/*
 * BreakpointAnnotationListener.java
 *
 * @author Gordon Prieur (copied from Jan Jancura's JPDA implementation)
 */

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;

import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.GdbBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.FunctionBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.LineBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.EditorContextBridge;
import org.openide.util.Utilities;

/**
 * Listens on {@org.netbeans.api.debugger.DebuggerManager} on
 * {@link org.netbeans.api.debugger.DebuggerManager#PROP_BREAKPOINTS}
 * property and annotates GDB Debugger line breakpoints in NetBeans editor.
 * It manages list of line breakpoint annotations for ToggleBreakpointPerformer
 * and BreakpointsUpdated too.
 */
public class BreakpointAnnotationListener extends DebuggerManagerAdapter {
    
    private HashMap breakpointToAnnotation = new HashMap();
    private boolean listen = true;
    
    
    public String[] getProperties() {
        return new String[] {DebuggerManager.PROP_BREAKPOINTS};
    }
    
    /**
     * Listens on breakpoint.
     */
    public void propertyChange(PropertyChangeEvent e) {
        if (listen) {
            String propertyName = e.getPropertyName();
            if ( (!propertyName.equals(LineBreakpoint.PROP_CONDITION)) &&
                    (!propertyName.equals(LineBreakpoint.PROP_URL)) &&
                    (!propertyName.equals(LineBreakpoint.PROP_LINE_NUMBER)) &&
                    (!propertyName.equals(GdbBreakpoint.PROP_ENABLED))
                    ) {
                return;
            }
            if (e.getSource() instanceof LineBreakpoint) {
                LineBreakpoint lb = (LineBreakpoint) e.getSource();
                annotate(lb);
            }  else if (e.getSource() instanceof FunctionBreakpoint) {
                FunctionBreakpoint fb = (FunctionBreakpoint) e.getSource();
                annotate(fb);
            }
        }
    }
    
    /**
     * Called when some breakpoint is added.
     *
     * @param b breakpoint
     */
    public void breakpointAdded(Breakpoint b) {
        b.addPropertyChangeListener(this);
        if (b instanceof GdbBreakpoint) {
            annotate((GdbBreakpoint) b);
        }
    }
    
    /**
     * Called when some breakpoint is removed.
     *
     * @param breakpoint
     */
    public void breakpointRemoved(Breakpoint b) {
        b.removePropertyChangeListener(this);
        removeAnnotation(b);
    }
    
    public GdbBreakpoint findBreakpoint(String url, int lineNumber) {
        Iterator i = breakpointToAnnotation.keySet().iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof LineBreakpoint) {
                LineBreakpoint lb = (LineBreakpoint) o;
                if (!lb.getURL().equals(url)) continue;
                Object annotation = breakpointToAnnotation.get(lb);
                int ln = EditorContextBridge.getContext().getLineNumber(annotation, null);
                if (ln == lineNumber) return lb;
            }
            if (o instanceof FunctionBreakpoint) {
                FunctionBreakpoint fb = (FunctionBreakpoint) o;
                if (Utilities.isWindows()) {
                    boolean found = false;
                    if (fb.getURL().equals(url)) {
                        found = true;
                    } else {
                        // Drive letter is not case sensitive - let's try to ignore case
                        String fb_url = fb.getURL();
                        if ((fb_url.startsWith("file:/")) && (url.startsWith("file:/"))) { // NOI18N
                            if((fb_url.charAt(7) == ':') && (url.charAt(7) == ':')) {
                                String url_lc = url.substring(0, 8);
                                url_lc = url_lc.toLowerCase() + url.substring(8);
                                String fb_url_lc = fb_url.substring(0, 8);
                                fb_url_lc = fb_url_lc.toLowerCase() + fb_url.substring(8);
                                if (fb_url_lc.equals(url_lc)) 
                                    found = true;
                            }
                        }
                    }
                    if (!found) continue;
                } else {
                    if (!fb.getURL().equals(url)) continue;
                }
                Object annotation = breakpointToAnnotation.get(fb);
                int ln = EditorContextBridge.getContext().getLineNumber(annotation, null);
                if (ln == lineNumber) return fb;
            }
        }
        return null;
    }
    
    private void annotate(GdbBreakpoint b) {
        // remove old annotation
        Object annotation = breakpointToAnnotation.get(b);
        if (annotation != null) {
            EditorContextBridge.getContext().removeAnnotation(annotation);
        }
        if (b.isHidden()) {
            return;
        }

        // check line number (optimization)
        int lineNumber = b.getLineNumber();
        if (lineNumber < 1) {
            return;
        }

        // add new one
        annotation = EditorContextBridge.annotate(b);
        if (annotation == null) {
            return;
        }
        breakpointToAnnotation.put(b, annotation);
        
        // update timestamp
        DebuggerEngine dm = DebuggerManager.getDebuggerManager().getCurrentEngine();
        Object timeStamp = null;
        if (dm != null) {
            timeStamp = dm.lookupFirst(null, GdbDebugger.class);
        }
        update(b, timeStamp);
    }
    
    /**
     * Method updateBreakpoints() is called from BreakpointsUpdater
     * when a new debugging session starts.
     */
    public void updateBreakpoints() {
        Iterator it = breakpointToAnnotation.keySet().iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof GdbBreakpoint) {
                update((GdbBreakpoint) o, null);
            }
        }
    }
    
    private void update(GdbBreakpoint b, Object timeStamp) {
        Object annotation = breakpointToAnnotation.get(b);
        int ln = EditorContextBridge.getContext().getLineNumber(annotation, timeStamp);
        listen = false;
        b.setLineNumber(ln);
        listen = true;
    }
    
    private void removeAnnotation(Breakpoint b) {
        Object annotation = breakpointToAnnotation.remove(b);
        if (annotation != null)
            EditorContextBridge.getContext().removeAnnotation(annotation);
    }
}
