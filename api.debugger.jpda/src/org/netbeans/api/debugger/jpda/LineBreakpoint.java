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

package org.netbeans.api.debugger.jpda;


/**
 * Notifies about line breakpoint events.
 *
 * <br><br>
 * <b>How to use it:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    DebuggerManager.addBreakpoint (LineBreakpoint.create (
 *        "examples.texteditor.Ted",
 *        27
 *    ));</pre>
 * This breakpoint stops in Ted class on 27 line number.
 *
 * @author Jan Jancura
 */
public final class LineBreakpoint extends JPDABreakpoint {

    /** Property name constant */
    public static final String          PROP_LINE_NUMBER = "lineNumber"; // NOI18N
    /** Property name constant */
    public static final String          PROP_CLASS_NAME = "className"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_CONDITION = "condition"; // NOI18N
    
    
    private String                      className = "";
    private int                         lineNumber;
    private String                      condition = ""; // NOI18N

    private LineBreakpoint () {
    }
    
    /**
     * Creates a new breakpoint for given parameters.
     *
     * @param className a class name filter
     * @param lineNumber a line number
     * @return a new breakpoint for given parameters
     */
    public static LineBreakpoint create (
        String className,
        int lineNumber
    ) {
        LineBreakpoint b = new LineBreakpoint ();
        b.setClassName (className);
        b.setLineNumber (lineNumber);
        return b;
    }
    
    /**
     * Sets name of class to stop on.
     *
     * @param cn a new name of class to stop on
     */
    public void setClassName (String cn) {
        if (cn != null) cn = cn.trim ();
        if ( (cn == className) ||
             ((cn != null) && (className != null) && className.equals (cn))
        ) return;
        Object old = className;
        className = cn;
        firePropertyChange (PROP_CLASS_NAME, old, className);
    }

    /**
     * Gets name of class to stop on.
     *
     * @return name of class to stop on
     */
    public String getClassName () {
        return className;
    }
    
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
    public void setLineNumber (int ln) {
        if (ln == lineNumber) return;
        int old = lineNumber;
        lineNumber = ln;
        firePropertyChange (
            PROP_LINE_NUMBER,
            new Integer (old),
            new Integer (getLineNumber ())
        );
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
        firePropertyChange (PROP_CONDITION, old, condition);
    }
    
    /**
     * Returns condition.
     *
     * @return cond a condition
     */
    public String getCondition () {
        return condition;
    }
}