/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.debug.breakpoints;

import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;

import org.netbeans.modules.web.debug.util.Utils;
import java.util.*;
import org.openide.util.NbBundle;


/**
 *
 * @author Martin Grebac
 */
public class JspLineBreakpoint extends Breakpoint {
    
    /** Property name for enabled status of the breakpoint. */
    public static final String          PROP_ENABLED = JPDABreakpoint.PROP_ENABLED;

    public static final String          PROP_SUSPEND = JPDABreakpoint.PROP_SUSPEND;
    public static final String          PROP_HIDDEN = JPDABreakpoint.PROP_HIDDEN;
    public static final String          PROP_PRINT_TEXT = JPDABreakpoint.PROP_PRINT_TEXT;

    public static final int             SUSPEND_ALL = JPDABreakpoint.SUSPEND_ALL;
    public static final int             SUSPEND_EVENT_THREAD = JPDABreakpoint.SUSPEND_EVENT_THREAD;
    public static final int             SUSPEND_NONE = JPDABreakpoint.SUSPEND_NONE;

    public static final String          PROP_LINE_NUMBER = LineBreakpoint.PROP_LINE_NUMBER;
    public static final String          PROP_URL = LineBreakpoint.PROP_URL;
    public static final String          PROP_CONDITION = LineBreakpoint.PROP_CONDITION;
    
    private boolean                     enabled = true;
    private boolean                     hidden = false;
    private int                         suspend = SUSPEND_ALL;
    private String                      printText;    

    private String                      url = "";       // NOI18N
    private int                         lineNumber;
    private String                      condition = ""; // NOI18N
    
    private LineBreakpoint javalb;
        
    /** Creates a new instance of JspLineBreakpoint */
    public JspLineBreakpoint() { }
    
    /** Creates a new instance of JspLineBreakpoint with url, linenumber*/
    public JspLineBreakpoint(String url, int lineNumber) {
        super();
        
        this.url = url;
        this.lineNumber = lineNumber;
        String pt = NbBundle.getMessage(JspLineBreakpoint.class, "CTL_Default_Print_Text");
        this.printText = org.openide.util.Utilities.replaceString(pt, "{jspName}", Utils.getJspName(url));  
        
        DebuggerManager d = DebuggerManager.getDebuggerManager();
        
        Utils.getEM().log("jsp url: " + url);

        String filter = Utils.getClassFilter(url);
        Utils.getEM().log("filter: " + filter);
        
        javalb = LineBreakpoint.create(filter, lineNumber);
        javalb.setStratum("JSP"); // NOI18N
        javalb.setSourceName(Utils.getJspName(url));
        javalb.setHidden(true);
        javalb.setPrintText(printText);
        
        String context = Utils.getContextPath(url);
        String condition = "request.getContextPath().equals(\"" + context + "\")"; // NOI18N
        javalb.setCondition(condition);
        Utils.getEM().log("condition: " + condition);
        
        d.addBreakpoint(javalb);

        this.setURL(url);
        this.setLineNumber(lineNumber);
    }

    /**
     * Creates a new breakpoint for given parameters.
     *
     * @param url a url
     * @param lineNumber a line number
     * @return a new breakpoint for given parameters
     */
    public static JspLineBreakpoint create(String url, int lineNumber) {
        return new JspLineBreakpoint(url, lineNumber);
    }
    
    /**
     * Gets value of suspend property.
     *
     * @return value of suspend property
     */
    public int getSuspend () {
        return suspend;
    }

    /**
     * Sets value of suspend property.
     *
     * @param s a new value of suspend property
     */
    public void setSuspend (int s) {
        if (s == suspend) return;
        int old = suspend;
        suspend = s;
        if (javalb != null) {
            javalb.setSuspend(s);
        }
        firePropertyChange(PROP_SUSPEND, new Integer(old), new Integer(s));
    }
    
    /**
     * Gets value of hidden property.
     *
     * @return value of hidden property
     */
    public boolean isHidden () {
        return hidden;
    }
    
    /**
     * Sets value of hidden property.
     *
     * @param h a new value of hidden property
     */
    public void setHidden (boolean h) {
        if (h == hidden) return;
        boolean old = hidden;
        hidden = h;
        firePropertyChange(PROP_HIDDEN, Boolean.valueOf(old), Boolean.valueOf(h));
    }
    
    /**
     * Gets value of print text property.
     *
     * @return value of print text property
     */
    public String getPrintText () {
        return printText;
    }

    /**
     * Sets value of print text property.
     *
     * @param printText a new value of print text property
     */
    public void setPrintText (String printText) {
        if (this.printText == printText) return;
        String old = this.printText;
        this.printText = printText;
        if (javalb != null) {
            javalb.setPrintText(printText);
        }
        firePropertyChange(PROP_PRINT_TEXT, old, printText);
    }
    
    /**
     * Called when breakpoint is removed.
     */
    protected void dispose() {
        if (javalb != null) {
            DebuggerManager.getDebuggerManager().removeBreakpoint(javalb);
        }
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
        if (!enabled) return;
        enabled = false;
        if (javalb != null) {
            javalb.disable();
        }
        firePropertyChange(PROP_ENABLED, Boolean.TRUE, Boolean.FALSE);
    }
    
    /**
     * Enables the breakpoint.
     */
    public void enable() {
        if (enabled) return;
        enabled = true;
        if (javalb != null) {
            javalb.enable();
        }
        firePropertyChange(PROP_ENABLED, Boolean.FALSE, Boolean.TRUE);
    }

    /**
     * Sets name of class to stop on.
     *
     * @param cn a new name of class to stop on
     */
    public void setURL (String url) {
        if ( (url == this.url) ||
             ((url != null) && (this.url != null) && url.equals (this.url))
        ) return;
        String old = url;
        this.url = url;
        firePropertyChange(PROP_URL, old, url);
    }

    /**
     * Gets name of class to stop on.
     *
     * @return name of class to stop on
     */
    public String getURL () {
        return url;
    }
    
    /**
     * Gets number of line to stop on.
     *
     * @return line number to stop on
     */
    public int getLineNumber () {
        return lineNumber;
    }
    
    /**
     * Sets number of line to stop on.
     *
     * @param ln a line number to stop on
     */
    public void setLineNumber (int ln) {
        if (ln == lineNumber) return;
        int old = lineNumber;
        lineNumber = ln;
        if (javalb != null) {
            javalb.setLineNumber(ln);
        }
        firePropertyChange(PROP_LINE_NUMBER, new Integer(old), new Integer(getLineNumber()));
    }
    
    /**
     * Sets condition.
     *
     * @param c a new condition
     */
    public void setCondition (String c) {
        if (c != null) c = c.trim ();
        if ( (c == condition) ||
             ((c != null) && (condition != null) && condition.equals (c))
        ) return;
        String old = condition;
        condition = c;
        if (javalb != null) {
            javalb.setCondition(c);
        }        
        firePropertyChange(PROP_CONDITION, old, condition);
    }
    
    /**
     * Returns condition.
     *
     * @return cond a condition
     */
    public String getCondition () {
        return condition;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return  a string representation of the object
     */
    public String toString () {
        return "JspLineBreakpoint " + url + " : " + lineNumber;
    }    
            
    /**
     * Getter for property javalb.
     * @return Value of property javalb.
     */
    public LineBreakpoint getJavalb() {
        return javalb;
    }
    
    /**
     * Setter for property javalb.
     * @param javalb New value of property javalb.
     */
    public void setJavalb(LineBreakpoint javalb) {
        this.javalb = javalb;
    }
    
    /**
     * Sets group name of this JSP breakpoint and also sets the same group name for underlying Java breakpoint.
     * 
     * @param newGroupName name of the group
     */ 
    public void setGroupName(String newGroupName) {
        super.setGroupName(newGroupName);
        javalb.setGroupName(newGroupName);
    }
}
