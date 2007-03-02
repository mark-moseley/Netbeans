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
import org.openide.util.NbBundle;

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
        if (JPDABreakpoint.PROP_VALIDITY.equals(evt.getPropertyName())) {
            if (JPDABreakpoint.VALIDITY.INVALID.equals(evt.getNewValue())) {
                JPDABreakpoint bp = (JPDABreakpoint) evt.getSource();
                String msg = bp.getValidityMessage();
                synchronized (lock) {
                    if (ioManager == null) {
                        lookupIOManager ();
                        if (ioManager == null) return;
                    }
                    String printText = (msg != null) ?
                                       NbBundle.getMessage(BreakpointOutput.class, "MSG_InvalidBreakpointWithReason", bp.toString(), msg) :
                                       NbBundle.getMessage(BreakpointOutput.class, "MSG_InvalidBreakpoint", bp.toString());
                    IOManager.Line line = null;
                    if (bp instanceof LineBreakpoint) {
                        line = new IOManager.Line (
                            ((LineBreakpoint) bp).getURL(),
                            ((LineBreakpoint) bp).getLineNumber(),
                            debugger
                        );
                    }
                    ioManager.println (printText, null, true);
                    if (line != null) {
                        ioManager.println(
                                NbBundle.getMessage(BreakpointOutput.class, "Link_InvalidBreakpoint", bp.toString()),
                                line, true);
                    }
                }
            } else if (JPDABreakpoint.VALIDITY.VALID.equals(evt.getNewValue())) {
                JPDABreakpoint bp = (JPDABreakpoint) evt.getSource();
                synchronized (lock) {
                    if (ioManager == null) {
                        lookupIOManager ();
                        if (ioManager == null) return;
                    }
                    String printText = NbBundle.getMessage(BreakpointOutput.class, "MSG_ValidBreakpoint", bp.toString());
                    IOManager.Line line = null;
                    if (bp instanceof LineBreakpoint) {
                        line = new IOManager.Line (
                            ((LineBreakpoint) bp).getURL(),
                            ((LineBreakpoint) bp).getLineNumber(),
                            debugger
                        );
                    }
                    ioManager.println (printText, line, false);
                }
            }
            return ;
        }
        synchronized (lock) {
            if (debugger == null ||
                evt.getPropertyName () != debugger.PROP_STATE ||
                debugger.getState () == debugger.STATE_STOPPED) {
                
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
        Session session = null;
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        for (int i = 0; i < sessions.length; i++) {
            if (sessions[i].lookupFirst(null, JPDADebugger.class) == debugger) {
                session = sessions[i];
                break;
            }
        }
        String language = (session != null) ? session.getCurrentLanguage() : null;
        String methodName = t.getMethodName ();
        if ("".equals (methodName)) methodName = "?";
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
                JPDADebugger theDebugger;
                synchronized (lock) {
                    if (debugger == null) {
                        return value; // The debugger is gone
                    }
                    theDebugger = debugger;
                }
                value = theDebugger.evaluate (expression).getValue ();
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
            jpdaBreakpoint.addPropertyChangeListener(JPDABreakpoint.PROP_VALIDITY, this);
        }
    }

    private void unhookBreakpoint (Breakpoint breakpoint) {
        if (breakpoint instanceof JPDABreakpoint) {
            JPDABreakpoint jpdaBreakpoint = (JPDABreakpoint) breakpoint;
            jpdaBreakpoint.removeJPDABreakpointListener (this);
            jpdaBreakpoint.removePropertyChangeListener(JPDABreakpoint.PROP_VALIDITY, this);
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
