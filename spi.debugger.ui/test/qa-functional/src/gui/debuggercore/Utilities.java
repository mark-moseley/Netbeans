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
 * The Original Software is NetBeans.
 * The Initial Developer of the Original Software is Sun Microsystems, Inc.
 * Portions created by Sun Microsystems, Inc. are Copyright (C) 2003
 * All Rights Reserved.
 *
 * Contributor(s): Sun Microsystems, Inc.
 */

package gui.debuggercore;

import java.awt.Component;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.DebugProjectAction;
import org.netbeans.jellytools.modules.debugger.actions.DebugAction;
import org.netbeans.jellytools.modules.debugger.actions.FinishDebuggerAction;
import org.netbeans.jellytools.modules.debugger.actions.NewBreakpointAction;
import org.netbeans.jellytools.modules.debugger.actions.ToggleBreakpointAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.openide.awt.StatusDisplayer;




public class Utilities {
    
    public static String windowMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Window");
    public static String runMenu = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject");
    public static String debugMenu = Bundle.getStringTrimmed("org.netbeans.modules.debugger.resources.Bundle", "Menu/Window/Debug");
    public static String runFileMenu = Bundle.getString("org.netbeans.modules.java.project.Bundle", "LBL_RunFile_Action");
    public static String debugToolbarLabel = Bundle.getString("org.netbeans.modules.debugger.jpda.ui.Bundle", "Toolbars/Debug");
    
    public static String toggleBreakpointItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Toggle_breakpoint");
    public static String newBreakpointItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_AddBreakpoint");
    public static String newWatchItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_New_Watch");
    public static String debugMainProjectItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_DebugMainProjectAction_Name");
    public static String stepIntoItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Step_into_action_name");
    public static String stepOverItem = Bundle.getString("org.netbeans.modules.debugger.jpda.ui.Bundle", "CTL_Step_Over");
    public static String stepOutItem = Bundle.getString("org.netbeans.modules.debugger.jpda.ui.Bundle", "CTL_Step_Out");
    public static String runIntoMethodItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Run_into_method_action_name");
    public static String continueItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Continue_action_name");
    public static String pauseItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Pause_action_name");
    public static String finishSessionsItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_KillAction_name");
    public static String runToCursorItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Run_to_cursor_action_name");
    public static String applyCodeChangesItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Fix_action_name");
    public static String evaluateExpressionItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.ui.actions.Bundle", "CTL_Evaluate");
    
    public static String localVarsItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_LocalVariablesAction");
    public static String watchesItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_WatchesAction");
    public static String callStackItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_CallStackAction");
    public static String classesItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.ui.actions.Bundle", "CTL_ClassesAction");
    public static String sourcesItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.ui.actions.Bundle", "CTL_SourcesViewAction");
    public static String breakpointsItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_BreakpointsAction");
    public static String sessionsItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_SessionsAction");
    public static String threadsItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_ThreadsAction");
    
    public static String localVarsViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Variables_view");
    public static String watchesViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Watches_view");
    public static String callStackViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Call_stack_view");
    public static String classesViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.ui.views.Bundle", "CTL_Classes_view");
    public static String sourcesViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.ui.views.Bundle", "CTL_Sourcess_view");
    public static String breakpointsViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Breakpoints_view");
    public static String sessionsViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Sessions_view");
    public static String threadsViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Threads_view");
    
    public static String customizeBreakpointTitle = Bundle.getString("org.netbeans.modules.debugger.jpda.ui.models.Bundle", "CTL_Breakpoint_Customizer_Title");
    public static String newBreakpointTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Breakpoint_Title");
    public static String newWatchTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_WatchDialog_Title");
    
    public static String runningStatusBarText = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.ui.Bundle", "CTL_Debugger_running");
    public static String stoppedStatusBarText = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.ui.Bundle", "CTL_Debugger_stopped");
    public static String finishedStatusBarText = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.ui.Bundle", "CTL_Debugger_finished");
    public static String buildCompleteStatusBarText = "Finished building";
    
    public static String openSourceAction = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
    public static String setMainProjectAction = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_SetAsMainProjectAction_Name");
    public static String projectPropertiesAction = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_CustomizeProjectAction_Popup_Name");
    public static String projectPropertiesTitle = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.Bundle", "LBL_Customizer_Title");
    //    public static String runningProjectTreeItem = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.customizer.Bundle", "LBL_Config_Run");
    
    public static String testProjectName = "debugTestProject";
    
    public static KeyStroke toggleBreakpointShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F8, KeyEvent.CTRL_MASK);
    public static KeyStroke newBreakpointShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F8, KeyEvent.CTRL_MASK|KeyEvent.SHIFT_MASK);
    public static KeyStroke newWatchShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.CTRL_MASK|KeyEvent.SHIFT_MASK);
    public static KeyStroke debugProjectShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
    public static KeyStroke debugFileShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.CTRL_MASK|KeyEvent.SHIFT_MASK);
    public static KeyStroke runToCursorShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);
    public static KeyStroke stepIntoShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
    public static KeyStroke stepOutShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.CTRL_MASK);
    public static KeyStroke stepOverShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
    public static KeyStroke continueShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.CTRL_MASK);
    public static KeyStroke killSessionShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.SHIFT_MASK);
    public static KeyStroke buildProjectShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0);
    public static KeyStroke openBreakpointsShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_5, KeyEvent.ALT_MASK|KeyEvent.SHIFT_MASK);
    
    
    
    public Utilities() {}
    
    public static boolean verifyPopup(Node node, String[] menus) {
        //invocation of root popup
        JPopupMenuOperator popup=node.callPopup();
        return verifyPopup(popup, menus);
    }
    
    public static boolean verifyPopup(final JPopupMenuOperator popup, String[] menus) {
        for (int i=0;i < menus.length;i++) {
            try {
                popup.showMenuItem(menus[i]);
            } catch (NullPointerException npe) {
                throw new JemmyException("Popup path ["+menus[i]+"] not found.");
            }
        }
        //close popup and wait until is not visible
        popup.waitState(new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                popup.pushKey(KeyEvent.VK_ESCAPE);
                return !popup.isVisible();
            }
            public String getDescription() {
                return "Popup menu closed";
            }
        });
        return true;
    }
    
    public static boolean checkAnnotation(EditorOperator operator, int line, String annotationType) {
        Object[] annotations = operator.getAnnotations(line);
        boolean found = false;
        for (int i=0;i < annotations.length;i++) {
            if (annotationType.equals(operator.getAnnotationType(annotations[i]))) found=true;
        }
        return found;
    }
    
    public static void deleteAllBreakpoints() {
        showBreakpointsView();
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(breakpointsViewTitle));
        if (jTableOperator.getRowCount() > 0)
            new JPopupMenuOperator(jTableOperator.callPopupOnCell(0, 0)).pushMenu("Delete All");
    }
    
    public static void deleteAllWatches() {
        showWatchesView();
        sleep(500);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(watchesViewTitle));
        if (jTableOperator.getRowCount() > 0)
            new JPopupMenuOperator(jTableOperator.callPopupOnCell(0, 0)).pushMenu("Delete All");
    }
    
    public static void closeZombieSessions() {
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        showSessionsView();
        sleep(500);
        TopComponentOperator sessionsOper = new TopComponentOperator(sessionsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(sessionsOper);
        
        for (int i = 0; i < jTableOperator.getRowCount(); i++) {
            //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.killSessionsItem).toString(), null).perform();
            new Action(null, null, Utilities.killSessionShortcut).performShortcut();
            Utilities.sleep(1000);
        }
        
        jTableOperator = new JTableOperator(sessionsOper);
        if (jTableOperator.getRowCount() > 0) {
            for (int i = 0; i < jTableOperator.getRowCount(); i++) {
                jTableOperator.selectCell(i,0);
                javax.swing.JPopupMenu jPopupMenu = jTableOperator.callPopupOnCell(jTableOperator.getRowCount() - 1,0);
                new JPopupMenuOperator(jPopupMenu).pushMenu("Finish");
                mwo.waitStatusText("User program finished");
            }
        }
        sessionsOper.close();
    }
    
    public static void showDebuggerView(String viewName) {
        new Action(windowMenu + "|" + debugMenu + "|" + viewName, null).perform();
    }
    
    public static void showLocalVariablesView() {
        //new Action(windowMenu + "|" + debugMenu + "|" + localVarsItem, null).perform();
        new Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.ALT_MASK|KeyEvent.SHIFT_MASK)).performShortcut();
    }
    
    public static void showWatchesView() {
        //new Action(windowMenu + "|" + debugMenu + "|" + watchesItem, null).perform();
        new Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.ALT_MASK|KeyEvent.SHIFT_MASK)).performShortcut();
    }
    
    public static void showCallStackView() {
        //new Action(windowMenu + "|" + debugMenu + "|" + callStackItem, null).perform();
        new Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.ALT_MASK|KeyEvent.SHIFT_MASK)).performShortcut();
    }
    
    public static void showClassesView() {
        //new Action(windowMenu + "|" + debugMenu + "|" + classesItem, null).perform();
        new Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.ALT_MASK|KeyEvent.SHIFT_MASK)).performShortcut();
    }
    
    public static void showBreakpointsView() {
        new Action(windowMenu + "|" + debugMenu + "|" + breakpointsItem, null).perform();
        //new Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_5, KeyEvent.ALT_MASK|KeyEvent.SHIFT_MASK)).performShortcut();
    }
    
    public static void showSessionsView() {
        //new Action(windowMenu + "|" + debugMenu + "|" + sessionsItem, null).perform();
        new Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_6, KeyEvent.ALT_MASK|KeyEvent.SHIFT_MASK)).performShortcut();
    }
    
    public static void showThreadsView() {
        //new Action(windowMenu + "|" + debugMenu + "|" + threadsItem, null).perform();
        new Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_7, KeyEvent.ALT_MASK|KeyEvent.SHIFT_MASK)).performShortcut();
    }
    
    public static void showSourcesView() {
        //new Action(windowMenu + "|" + debugMenu + "|" + sourcesItem, null).perform();
        new Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_8, KeyEvent.ALT_MASK|KeyEvent.SHIFT_MASK)).performShortcut();
    }
    
    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ex) {};
    }
    
    public static String removeTags(String in) {
        String out = "";
        in = in.trim();
        if (in.indexOf('<') == -1) {
            out = in;
        } else {
            while (in.indexOf('<') >= 0) {
                if (in.indexOf('<') == 0) {
                    in = in.substring(in.indexOf('>')+1, in.length());
                } else {
                    out += in.substring(0, in.indexOf('<'));
                    in = in.substring(in.indexOf('<'), in.length());
                }
            }
        }
        return out;
    }
    
    public static void endSession() {
        ContainerOperator debugToolbarOper = getDebugToolbar();
        new FinishDebuggerAction().perform();
        // wait until Debug toolbar dismiss
        debugToolbarOper.waitComponentVisible(false);
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.killSessionsItem).toString(), null).perform();
        //new Action(null, null, Utilities.killSessionShortcut).performShortcut();
        //MainWindowOperator.getDefault().waitStatusText(Utilities.finishedStatusBarText);
    }
    
    public static void startDebugger(String statusText) {
        new DebugProjectAction().performShortcut();
        waitStatusText(statusText);
    }
    
    public static ContainerOperator getDebugToolbar() {
        return MainWindowOperator.getDefault().getToolbar(debugToolbarLabel);
    }
    
    public static void setCaret(EditorOperator eo, final int line) {
        eo.setCaretPositionToLine(line);
        eo.requestFocus();
        
        try {
            new Waiter(new Waitable() {
                public Object actionProduced(Object editorOper) {
                    EditorOperator op = (EditorOperator)editorOper;
                    System.err.print("get line "+op.hasFocus()+" "+op.getLineNumber());
                    if (op.getLineNumber() == line) {
                        return Boolean.TRUE;
                    }
                    return null;
                }
                
                public String getDescription() {
                    return "Wait caret position on line " + line;
                }
            }).waitAction(eo);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    public static NbDialogOperator newBreakpoint(int line, int column) {
        Node projectNode = ProjectsTabOperator.invoke().getProjectRootNode(testProjectName);
        EditorOperator eo = new EditorOperator("MemoryView.java");
        eo.setCaretPosition(line, column);
        new NewBreakpointAction().perform();
        NbDialogOperator dialog = new NbDialogOperator(newBreakpointTitle);
        return dialog;
    }
    
    public static NbDialogOperator newBreakpoint(int line) {
        Node projectNode = ProjectsTabOperator.invoke().getProjectRootNode(testProjectName);
        EditorOperator eo = new EditorOperator("MemoryView.java");
        setCaret(eo, line);
        new NewBreakpointAction().perform();
        NbDialogOperator dialog = new NbDialogOperator(newBreakpointTitle);
        return dialog;
    }
    
    public static void toggleBreakpoint(EditorOperator eo, final int line) {
        toggleBreakpoint(eo, line, true);
    }
    
    public static void toggleBreakpoint(EditorOperator eo, final int line, final boolean newBreakpoint) {
        setCaret(eo, line);
        
        new ToggleBreakpointAction().perform();
        try {
            new Waiter(new Waitable() {
                public Object actionProduced(Object editorOper) {
                    Object[] annotations = ((EditorOperator) editorOper).getAnnotations(line);
                    boolean found =false;
                    for (int i = 0; i < annotations.length; i++) {
                        if ("Breakpoint".equals(((EditorOperator) editorOper).getAnnotationType(annotations[i]))) {
                            found=true;
                            if (newBreakpoint) {
                                return Boolean.TRUE;
                            }
                        }
                    }
                    if (!found && !newBreakpoint) {
                        return Boolean.TRUE;
                    }
                    return null;
                }
                
                public String getDescription() {
                    return "Wait breakpoint established on line " + line;
                }
            }).waitAction(eo);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    public static void waitFinished(JellyTestCase test, String projectName, String target) {
        long oldTimeout = MainWindowOperator.getDefault().getTimeouts().getTimeout("Waiter.WaitingTime");
        try {
            // increase time to wait to 240 second (it fails on slower machines)
            MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 240000);
            MainWindowOperator.getDefault().waitStatusText("Finished building "+projectName+" ("+target+")");
        } finally {
            // start status text tracer again because we use it further
            MainWindowOperator.getDefault().getStatusTextTracer().start();
            // restore default timeout
            MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", oldTimeout);
            // log messages from output
            test.getLog("RunOutput").print(new OutputTabOperator(projectName).getText()); // NOI18N
        }
    }
    
    public static void waitStatusText(String text) {
        long oldTimeout = MainWindowOperator.getDefault().getTimeouts().getTimeout("Waiter.WaitingTime");
        try {
            // increase time to wait to 240 second (it fails on slower machines)
            MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 240000);
            MainWindowOperator.getDefault().waitStatusText(text);
        } finally {
            // start status text tracer again because we use it further
            MainWindowOperator.getDefault().getStatusTextTracer().start();
            // restore default timeout
            MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", oldTimeout);
        }
    }
    
    public static void waitStatusTextPrefix(final String text) {
        try {
            new Waiter(new Waitable() {
                public Object actionProduced(Object anObject) {
                    if (StatusDisplayer.getDefault().getStatusText().startsWith(text))
                        return Boolean.TRUE;
                    return null;
                }
                public String getDescription() {
                    return("Wait status text prefix to "+text);
                }
            }).waitAction(StatusDisplayer.getDefault());
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}

