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

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;

import org.netbeans.api.debugger.Breakpoint;

import org.netbeans.modules.cnd.debugger.gdb.event.GdbBreakpointEvent;
import org.netbeans.modules.cnd.debugger.gdb.event.GdbBreakpointListener;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.utils.GdbUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Utilities;

/**
 * Abstract definition of Cnd breakpoint.
 *
 * @author   Gordon Prieur (copied from Jan Jancura's JPDABreakpoint)
 */
public abstract class GdbBreakpoint extends Breakpoint {
    
    public static final String          PROP_SUSPEND = "suspend"; // NOI18N
    public static final String          PROP_HIDDEN = "hidden"; // NOI18N
    public static final String          PROP_PRINT_TEXT = "printText"; // NOI18N
    public static final String          PROP_BREAKPOINT_STATE = "breakpointState"; // NOI18N
    public static final String          PROP_LINE_NUMBER = "lineNumber"; // NOI18N
    public static final String          PROP_URL = "url"; // NOI18N
    public static final String          PROP_CONDITION = "condition"; // NOI18N
    
    private int                         lineNumber;
    private boolean                     enabled = true;
    private boolean                     hidden = false;
    private int                         suspend = 0; // Not fully implemented yet!
    private String                      printText;
    private HashSet                     breakpointListeners = new HashSet();
    private GdbDebugger                 debugger;
    private String                      condition = ""; // NOI18N
    private String                      url = "";       // NOI18N
    private String                      path = "";      // NOI18N
    
    /**
     * Gets number of line to stop on.
     *
     * @return line number to stop on
     */
    public int getLineNumber() {
        return lineNumber;
    }
    
    /**
     * Sets number of line to stop on.
     *
     * @param ln a line number to stop on
     */
    public void setLineNumber(int ln) {
        int old;
        synchronized (this) {
            if (ln == lineNumber) {
                return;
            }
            old = lineNumber;
            lineNumber = ln;
        }
        firePropertyChange(PROP_LINE_NUMBER, new Integer(old), new Integer(ln));
    }
    
    /**
     *  Return a path based on this breakpoints URL. The path is not necessarily the
     *  same as the URL with the "File:/" removed. This is because Windows often substitues
     *  "%20" for spaces. It also puts a "/" before the drive specifier.
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Gets file name in URL format.
     *
     * @return file name in URL format or empty string
     */
    public String getURL() {
        return url;
    }
    
    /**
     * Sets file name in URL format.
     *
     * @param file name
     */
    public void setURL(String url) {
        synchronized (this) {
            if (url != null && this.url != null && url.equals(this.url)) {
                return;
            }
            // The code below is a protection against "invalid" URL values.
            url = url.replace(" ", "%20"); // NOI18N
            if (!url.startsWith("file:/")) { // NOI18N
                if (url.startsWith("/")) { // NOI18N
                    url = "file:" + url; // NOI18N
                } else {
                    url = "file:/" + url; // NOI18N
                }
            }
            // We need consistent slashes for compairing an existing breakpoint's url
            // to a proposed breakpoint's url.
            url = url.replace("\\", "/"); // NOI18N
            
            // Also set the path variable, based on the URL.
            try {
                assert(!(url == null && Boolean.getBoolean("gdb.assertions.enabled"))); // NOI18N
                FileObject fo = URLMapper.findFileObject(new URL(url));
                if (fo != null) {
                    if (Utilities.isWindows()) {
                        path = fo.getPath();
                    } else {
                        path = "/" + fo.getPath(); // NOI18N
                    }
                }
            } catch (MalformedURLException mue) {
                assert !Boolean.getBoolean("gdb.assertions.enabled"); // NOI18N
                return;
            } catch (Exception ex) {
                assert !Boolean.getBoolean("gdb.assertions.enabled"); // NOI18N
            }
            this.url = url;
        }
//        firePropertyChange(PROP_URL, old, url);
    }
    
    /**
     * Returns condition.
     *
     * @return cond a condition
     */
    public String getCondition() {
        return condition;
    }
    
    /**
     * Sets condition.
     *
     * @param c a new condition
     */
    public void setCondition(String c) {
        String old;
        synchronized (this) {
            if (c == null) {
                c = ""; // NOI18N
            }
            c = c.trim();
            if ((c.equals(condition)) ||
                    ((c != null) && (condition != null) && condition.equals(c))) {
                return;
            }
            old = condition;
            condition = c;
        }
        firePropertyChange(PROP_CONDITION, old, c);
    }
    
    /**
     * Gets value of suspend property.
     *
     * @return value of suspend property
     */
    public int getSuspend() {
        return suspend;
    }
    
    /**
     * Sets value of suspend property.
     *
     * @param s a new value of suspend property
     */
    public void setSuspend(int s) {
        if (s == suspend) {
            return;
        }
        int old = suspend;
        suspend = s;
        firePropertyChange(PROP_SUSPEND, new Integer(old), new Integer(s));
    }
    
    /**
     * Gets value of hidden property.
     *
     * @return value of hidden property
     */
    public boolean isHidden() {
        return hidden;
    }
    
    /**
     * Sets value of hidden property.
     *
     * @param h a new value of hidden property
     */
    public void setHidden(boolean h) {
        if (h == hidden) {
            return;
        }
        boolean old = hidden;
        hidden = h;
        firePropertyChange(PROP_HIDDEN, Boolean.valueOf(old), Boolean.valueOf(h));
    }
    
    /**
     * Gets value of print text property.
     *
     * @return value of print text property
     */
    public String getPrintText() {
        return printText;
    }
    
    /**
     * Sets value of print text property.
     *
     * @param printText a new value of print text property
     */
    public void setPrintText(String printText) {
        if (this.printText == null || this.printText.equals(printText)) {
            return;
        }
        String old = this.printText;
        this.printText = printText;
        firePropertyChange(PROP_PRINT_TEXT, old, printText);
    }
    
    /**
     * Test whether the breakpoint is enabled.
     *
     * @return <code>true</code> if so
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Disables the breakpoint.
     */
    public void disable() {
        if (!enabled) {
            return;
        }
        enabled = false;
        firePropertyChange(PROP_ENABLED, Boolean.TRUE, Boolean.FALSE);
    }
    
    /**
     * Enables the breakpoint.
     */
    public void enable() {
        if (enabled) {
            return;
        }
        enabled = true;
        firePropertyChange(PROP_ENABLED, Boolean.FALSE, Boolean.TRUE);
    }
    
    /**
     *
     * Adds a GdbBreakpointListener.
     *
     * @param listener the listener to add
     */
    public synchronized void addGdbBreakpointListener(GdbBreakpointListener listener) {
        breakpointListeners.add(listener);
    }
    
    /**
     *
     * Removes a GdbBreakpointListener.
     *
     * @param listener the listener to remove
     */
    public synchronized void removeGdbBreakpointListener(GdbBreakpointListener listener){
        breakpointListeners.remove(listener);
    }
    
    /**
     * Fire GdbBreakpointEvent.
     *
     * @param event a event to be fired
     */
    public void fireGdbBreakpointChange(GdbBreakpointEvent event) {
        Iterator i = ((HashSet) breakpointListeners.clone()).iterator();
        while (i.hasNext()) {
            ((GdbBreakpointListener) i.next()).breakpointReached(event);
        }
    }
    
    protected void setDebugger(GdbDebugger debugger) {
	this.debugger = debugger;
    }
    
    public GdbDebugger getDebugger() {
	return debugger;
    }
}
