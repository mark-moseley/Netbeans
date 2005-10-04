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

package org.netbeans.modules.debugger.jpda.ui;

import java.beans.PropertyChangeListener;
import java.util.List;
import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.*;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.modules.debugger.jpda.ui.models.BreakpointsNodeModel;
import org.netbeans.spi.debugger.ContextProvider;

import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.netbeans.spi.viewmodel.NodeModel;

/**
 * Listener on all breakpoints and prints text specified in the breakpoint when a it hits.
 *
 * @see JPDABreakpoint#setPrintText(java.lang.String)
 * @author Maros Sandor
 */
public class BreakpointOutput extends LazyActionsManagerListener
implements DebuggerManagerListener, JPDABreakpointListener,
PropertyChangeListener {

    private static final Pattern dollarEscapePattern = Pattern.compile
        ("\\$");
    private static final Pattern backslashEscapePattern = Pattern.compile
        ("\\\\");
    private static final Pattern threadNamePattern = Pattern.compile
        ("\\{threadName\\}");
    private static final Pattern classNamePattern = Pattern.compile
        ("\\{className\\}");
    private static final Pattern methodNamePattern = Pattern.compile
        ("\\{methodName\\}");
    private static final Pattern lineNumberPattern = Pattern.compile
        ("\\{lineNumber\\}");
    private static final Pattern expressionPattern = Pattern.compile
        ("\\{=(.*?)\\}");

    private IOManager               ioManager;
    private JPDADebugger            debugger;
    private ContextProvider         contextProvider;
    private Object                  lock = new Object();

    
    public BreakpointOutput (ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
        this.debugger = (JPDADebugger) contextProvider.lookupFirst 
            (null, JPDADebugger.class);
        debugger.addPropertyChangeListener (
            debugger.PROP_STATE,
            this
        );
        hookBreakpoints ();
        DebuggerManager.getDebuggerManager ().addDebuggerListener
            (DebuggerManager.PROP_BREAKPOINTS, this);
    }
    
    
    // LazyActionsManagerListener ..............................................
    
    protected void destroy () {
        DebuggerManager.getDebuggerManager ().removeDebuggerListener
            (DebuggerManager.PROP_BREAKPOINTS, this);
        unhookBreakpoints ();
        synchronized (lock) {
            ioManager = null;
            debugger = null;
        }
    }

    public String[] getProperties () {
        return new String[] { ActionsManagerListener.PROP_ACTION_PERFORMED };
    }
    
    
    // JPDABreakpointListener ..................................................

    public void breakpointReached (JPDABreakpointEvent event) {
        synchronized (lock) {
            if (event.getDebugger () != debugger) return;
        }
        if (event.getConditionResult () == event.CONDITION_FALSE) return;
        JPDABreakpoint breakpoint = (JPDABreakpoint) event.getSource ();
        getBreakpointsNodeModel ().setCurrentBreakpoint (breakpoint);
        synchronized (lock) {
            if (ioManager == null) {
                lookupIOManager ();
                if (ioManager == null) return;
            }
        }
        String printText = breakpoint.getPrintText ();
        if (printText == null || printText.length  () == 0) return;
        printText = substitute(printText, event);
        synchronized (lock) {
            if (ioManager != null) {
                ioManager.println (printText, null);
            }
        }
    }

    
    // DebuggerManagerListener .................................................

    public void breakpointAdded  (Breakpoint breakpoint) {
        hookBreakpoint (breakpoint);
    }

    public void breakpointRemoved (Breakpoint breakpoint) {
        unhookBreakpoint (breakpoint);
    }
    
    public Breakpoint[] initBreakpoints () {return new Breakpoint[0];}
    public void initWatches () {}
    public void watchAdded (Watch watch) {}
    public void watchRemoved (Watch watch) {}
    public void sessionAdded (Session session) {}
    public void sessionRemoved (Session session) {}
    public void engineAdded (DebuggerEngine engine) {}
    public void engineRemoved (DebuggerEngine engine) {}

    
    // PropertyChangeListener ..................................................
    
    public void propertyChange (PropertyChangeEvent evt) {
        synchronized (lock) {
            if (debugger == null ||
                evt.getPropertyName () != debugger.PROP_STATE ||
                debugger.getState () != debugger.STATE_RUNNING) {
                
                return ;
            }
        }
        getBreakpointsNodeModel ().setCurrentBreakpoint (null);
            
    }

    
    // private methods .........................................................
    
    /**
     *   threadName      name of thread where breakpoint ocurres
     *   className       name of class where breakpoint ocurres
     *   methodName      name of method where breakpoint ocurres
     *   lineNumber      number of line where breakpoint ocurres
     *
     * @param printText
     * @return
     */
    private String substitute (String printText, JPDABreakpointEvent event) {
        
        // 1) replace {threadName} by the name of current thread
        JPDAThread t = event.getThread ();
        if (t != null) {
            // replace \ by \\
            String name = backslashEscapePattern.matcher (t.getName ()).
                replaceAll ("\\\\\\\\");
            // replace $ by \$
            name = dollarEscapePattern.matcher (name).replaceAll ("\\\\\\$");
            printText = threadNamePattern.matcher (printText).replaceAll (name);
        }
        else
            printText = threadNamePattern.matcher (printText).replaceAll ("?");
        
        // 2) replace {className} by the name of current class
        if (event.getReferenceType () != null) {
            // replace $ by \$
            String name = dollarEscapePattern.matcher 
                (event.getReferenceType ().name ()).replaceAll ("\\\\\\$");
            printText = classNamePattern.matcher (printText).replaceAll (name);
        } else
            printText = classNamePattern.matcher (printText).replaceAll ("?");

        // 3) replace {methodName} by the name of current method
        String language = DebuggerManager.getDebuggerManager ().
            getCurrentSession ().getCurrentLanguage ();
        String methodName = t.getMethodName ();
        if (methodName.equals ("")) methodName = "?";
        // replace $ by \$
        methodName = dollarEscapePattern.matcher (methodName).replaceAll 
            ("\\\\\\$");
        printText = methodNamePattern.matcher (printText).replaceAll 
            (methodName);
        
        // 4) replace {lineNumber} by the current line number
        int lineNumber = t.getLineNumber (language);
        if (lineNumber < 0)
            printText = lineNumberPattern.matcher (printText).replaceAll 
                ("?");
        else
            printText = lineNumberPattern.matcher (printText).replaceAll 
                (String.valueOf (lineNumber));
             
        // 5) resolve all expressions {=expression}
        for (;;) {
            Matcher m = expressionPattern.matcher (printText);
            if (!m.find ()) break;
            String expression = m.group (1);
            String value = "";
            try {
                synchronized (lock) {
                    if (debugger == null) {
                        return value; // The debugger is gone
                    }
                    value = debugger.evaluate (expression).getValue ();
                }
                value = backslashEscapePattern.matcher (value).
                    replaceAll ("\\\\\\\\");
                value = dollarEscapePattern.matcher (value).
                    replaceAll ("\\\\\\$");
            } catch (InvalidExpressionException e) {
                // expression is invalid or cannot be evaluated
                String msg = e.getCause () != null ? 
                    e.getCause ().getMessage () : e.getMessage ();
                synchronized (lock) {
                    if (ioManager != null) {
                        ioManager.println (
                            "Cannot evaluate expression '" + expression + "' : " + msg, 
                            null
                        );
                    }
                }
            }
            printText = m.replaceFirst (value);
        }
        Throwable thr = event.getConditionException();
        if (thr != null) {
            printText = printText + "\n***\n"+ thr.getLocalizedMessage()+"\n***\n";
        }
        return printText;
    }

    private void lookupIOManager () {
        List lamls = contextProvider.lookup 
            (null, LazyActionsManagerListener.class);
        for (Iterator i = lamls.iterator (); i.hasNext ();) {
            Object o = i.next();
            if (o instanceof DebuggerOutput) {
                ioManager = ((DebuggerOutput) o).getIOManager ();
                break;
            }
        }
    }
    
    private void hookBreakpoints () {
        Breakpoint [] bpts = DebuggerManager.getDebuggerManager ().
            getBreakpoints ();
        for (int i = 0; i < bpts.length; i++) {
            Breakpoint bpt = bpts [i];
            hookBreakpoint (bpt);
        }
    }

    private void unhookBreakpoints () {
        Breakpoint [] bpts = DebuggerManager.getDebuggerManager ().
            getBreakpoints ();
        for (int i = 0; i < bpts.length; i++) {
            Breakpoint bpt = bpts [i];
            unhookBreakpoint (bpt);
        }
    }

    private void hookBreakpoint (Breakpoint breakpoint) {
        if (breakpoint instanceof JPDABreakpoint) {
            JPDABreakpoint jpdaBreakpoint = (JPDABreakpoint) breakpoint;
            jpdaBreakpoint.addJPDABreakpointListener (this);
        }
    }

    private void unhookBreakpoint (Breakpoint breakpoint) {
        if (breakpoint instanceof JPDABreakpoint) {
            JPDABreakpoint jpdaBreakpoint = (JPDABreakpoint) breakpoint;
            jpdaBreakpoint.removeJPDABreakpointListener (this);
        }
    }
    
    private BreakpointsNodeModel breakpointsNodeModel;
    private BreakpointsNodeModel getBreakpointsNodeModel () {
        if (breakpointsNodeModel == null) {
            List l = DebuggerManager.getDebuggerManager ().lookup
                ("BreakpointsView", NodeModel.class);
            Iterator it = l.iterator ();
            while (it.hasNext ()) {
                NodeModel nm = (NodeModel) it.next ();
                if (nm instanceof BreakpointsNodeModel) {
                    breakpointsNodeModel = (BreakpointsNodeModel) nm;
                    break;
                }
            }
        }
        return breakpointsNodeModel;
    }
}
