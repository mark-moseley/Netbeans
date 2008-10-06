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
 * The Original Software is NetBeans.
 * The Initial Developer of the Original Software is Sun Microsystems, Inc.
 * Portions created by Sun Microsystems, Inc. are Copyright (C) 2003
 * All Rights Reserved.
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
 *
 * Contributor(s): Sun Microsystems, Inc.
 */

package org.netbeans.debuggercore;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.KeyStroke;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.DebugProjectAction;
import org.netbeans.jellytools.modules.debugger.actions.NewBreakpointAction;
import org.netbeans.jellytools.modules.debugger.actions.ToggleBreakpointAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.util.PNGEncoder;
import org.openide.awt.StatusDisplayer;




public class Utilities {

    public static String windowMenu = Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle", "Menu/Window");
    public static String runMenu = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject");
    public static String debugMenu = Bundle.getStringTrimmed("org.netbeans.modules.debugger.resources.Bundle", "Menu/Window/Debug");
    public static String runFileMenu = Bundle.getStringTrimmed("org.netbeans.modules.java.project.Bundle", "LBL_RunFile_Action");
    public static String debugToolbarLabel = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.ui.Bundle", "Toolbars/Debug");

    public static String toggleBreakpointItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Toggle_breakpoint");
    public static String newBreakpointItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_AddBreakpoint");
    public static String newWatchItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_New_Watch");
    public static String debugMainProjectItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "LBL_DebugMainProjectAction_Name");
    public static String stepIntoItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Step_into_action_name");
    public static String stepOverItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.ui.Bundle", "CTL_Step_Over");
    public static String stepOutItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.ui.Bundle", "CTL_Step_Out");
    public static String stepOverExpresItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Step_operation_action_name");
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
    //public static String classesItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.heapwalk.views.Bundle", "CTL_Classes_view");
    public static String classesItem= "Loaded Classes";
//    public static String sourcesItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.ui.actions.Bundle", "CTL_SourcesViewAction");
    public static String sourcesItem = "Sources";
    public static String breakpointsItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_BreakpointsAction");
    public static String sessionsItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_SessionsAction");
    public static String threadsItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_ThreadsAction");

    public static String localVarsViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Variables_view");
    public static String watchesViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Watches_view");
    public static String callStackViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Call_stack_view");
    //public static String classesViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.heapwalk.views.Bundle", "CTL_Classes_view");
    public static String classesViewTitle = "Loaded Classes";
//    public static String sourcesViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.ui.views.Bundle", "CTL_Sourcess_view");
    public static String sourcesViewTitle = "Sources";
    public static String breakpointsViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Breakpoints_view");
    public static String sessionsViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Sessions_view");
    public static String threadsViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Threads_view");

    public static String customizeBreakpointTitle = Bundle.getString("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Breakpoint_Customizer_Title");
    public static String newBreakpointTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Breakpoint_Title");
    public static String newWatchTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_WatchDialog_Title");
//    public static String debuggerConsoleTitle = Bundle.getString("org.netbeans.modules.debugger.jpda.ui.Bundle", "CTL_DebuggerConsole_Title");
    public static String debuggerConsoleTitle = "Debugger Console";


//    public static String runningStatusBarText = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.ui.Bundle", "CTL_Debugger_running");
    public static String runningStatusBarText = "User program running";
//    public static String stoppedStatusBarText = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.ui.Bundle", "CTL_Debugger_stopped");
    public static String stoppedStatusBarText = "User program stopped";
//    public static String finishedStatusBarText = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.ui.Bundle", "CTL_Debugger_finished");
    public static String finishedStatusBarText = "User program finished";
    public static String buildCompleteStatusBarText = "Finished building";
    public static String evaluatingPropertyText = Bundle.getString("org.netbeans.modules.viewmodel.Bundle", "EvaluatingProp");

    public static String openSourceAction = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
    public static String setMainProjectAction = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_SetAsMainProjectAction_Name");
    public static String projectPropertiesAction = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_CustomizeProjectAction_Popup_Name");
    public static String projectPropertiesTitle = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.Bundle", "LBL_Customizer_Title");
    //    public static String runningProjectTreeItem = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.customizer.Bundle", "LBL_Config_Run");
    public static String testProjectName = "debugTestProject";

    public static KeyStroke toggleBreakpointShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F8, KeyEvent.CTRL_MASK);
    public static KeyStroke newBreakpointShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F8, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);
    public static KeyStroke newWatchShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);
    public static KeyStroke debugProjectShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
    public static KeyStroke debugFileShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);
    public static KeyStroke runToCursorShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);
    public static KeyStroke stepIntoShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
    public static KeyStroke stepOutShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.CTRL_MASK);
    public static KeyStroke stepOverShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
    public static KeyStroke continueShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.CTRL_MASK);
    public static KeyStroke killSessionShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.SHIFT_MASK);
    public static KeyStroke buildProjectShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0);
    public static KeyStroke openBreakpointsShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_5, KeyEvent.ALT_MASK | KeyEvent.SHIFT_MASK);

    public static boolean verifyMainMenu(String actionPath, boolean expected) {
        if (expected == MainWindowOperator.getDefault().menuBar().showMenuItem(actionPath).isEnabled()) {
            return MainWindowOperator.getDefault().menuBar().showMenuItem(actionPath).isEnabled();
        } else {
            for (int i = 0; i < 10; i++) {
                if (MainWindowOperator.getDefault().menuBar().showMenuItem(actionPath).isEnabled() == expected) {
                    MainWindowOperator.getDefault().menuBar().closeSubmenus();
                    return expected;
                }
                MainWindowOperator.getDefault().menuBar().closeSubmenus();
                new EventTool().waitNoEvent(500);
                System.err.println("waiting on " + actionPath);
            }
            return MainWindowOperator.getDefault().menuBar().showMenuItem(actionPath).isEnabled();
        }
    }

    public static boolean verifyPopup(Node node, String[] menus) {
        //invocation of root popup
        JPopupMenuOperator popup = node.callPopup();
        return verifyPopup(popup, menus);
    }

    public static boolean verifyPopup(final JPopupMenuOperator popup, String[] menus) {
        for (int i = 0; i < menus.length; i++) {
            try {
                popup.showMenuItem(menus[i]);
            } catch (NullPointerException npe) {
                throw new JemmyException("Popup path [" + menus[i] + "] not found.");
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
        new EventTool().waitNoEvent(500);
        Object[] annotations = operator.getAnnotations(line);
        boolean found = false;
        JemmyProperties.getProperties().getOutput().print(">>>>> Annotations on line: " + line + "\n");
        for (int i = 0; i < annotations.length; i++) {
            JemmyProperties.getProperties().getOutput().print("    " + operator.getAnnotationType(annotations[i]) + "\n");
            if (annotationType.equals(operator.getAnnotationType(annotations[i]))) {
                found = true;
            }
        }
        return found;
    }

    public static void deleteAllBreakpoints() {
        showDebuggerView(breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(breakpointsViewTitle));
        if (jTableOperator.getRowCount() > 0) {
            new JPopupMenuOperator(jTableOperator.callPopupOnCell(0, 0)).pushMenu(Bundle.getString("org.netbeans.modules.debugger.ui.models.Bundle", "CTL_BreakpointAction_DeleteAll_Label"));
        }
    }

    public static void deleteAllWatches() {
        showDebuggerView(watchesViewTitle);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(watchesViewTitle));
        if (jTableOperator.getRowCount() > 0) {
            new JPopupMenuOperator(jTableOperator.callPopupOnCell(0, 0)).pushMenu(Bundle.getString("org.netbeans.modules.debugger.ui.models.Bundle", "CTL_WatchAction_DeleteAll"));
        }
    }

    public static void showDebuggerView(String viewName) {
        new Action(windowMenu + "|" + debugMenu + "|" + viewName, null).perform();
        new TopComponentOperator(viewName);
        new EventTool().waitNoEvent(500);
    }

    public static String removeTags(String in) {
        String out = "";
        in = in.trim();
        if (in.indexOf('<') == -1) {
            out = in;
        } else {
            while (in.indexOf('<') >= 0) {
                if (in.indexOf('<') == 0) {
                    in = in.substring(in.indexOf('>') + 1, in.length());
                } else {
                    out += in.substring(0, in.indexOf('<'));
                    in = in.substring(in.indexOf('<'), in.length());
                }
            }
        }
        return out;
    }

    public static void endAllSessions() {
        showDebuggerView(sessionsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(sessionsViewTitle));
        if (jTableOperator.getRowCount() > 0) {
            new JPopupMenuOperator(jTableOperator.callPopupOnCell(0, 0)).pushMenu(Bundle.getString("org.netbeans.modules.debugger.ui.models.Bundle", "CTL_SessionAction_FinishAll_Label"));
        }
    }

    public static void startDebugger() {
        // "Set as Main Project"
        String setAsMainProjectItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_SetAsMainProjectAction_Name");
        new Action(null, setAsMainProjectItem).perform(new ProjectsTabOperator().getProjectRootNode(testProjectName));
        new DebugProjectAction().performShortcut();
//        getDebugToolbar().waitComponentVisible(true);
    }

    public static ContainerOperator getDebugToolbar() {
        return MainWindowOperator.getDefault().getToolbar(debugToolbarLabel);
    }

    public static void setCaret(EditorOperator eo, final int line) {
        eo.makeComponentVisible();
        eo.setCaretPositionToLine(line);
        new EventTool().waitNoEvent(500);

        try {
            new Waiter(new Waitable() {

                public Object actionProduced(Object editorOper) {
                    EditorOperator op = (EditorOperator) editorOper;
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
        EditorOperator eo = new EditorOperator("MemoryView.java");
        eo.setCaretPosition(line, column);
        new NewBreakpointAction().perform();
        NbDialogOperator dialog = new NbDialogOperator(newBreakpointTitle);
        new EventTool().waitNoEvent(500);
        return dialog;
    }

    public static NbDialogOperator newBreakpoint(int line) {
        Node projectNode = ProjectsTabOperator.invoke().getProjectRootNode(testProjectName);
        EditorOperator eo = new EditorOperator("MemoryView.java");
        setCaret(eo, line);
        new NewBreakpointAction().perform();
        NbDialogOperator dialog = new NbDialogOperator(newBreakpointTitle);
        new EventTool().waitNoEvent(500);
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
                    boolean found = false;
                    for (int i = 0; i < annotations.length; i++) {
                        if (((EditorOperator) editorOper).getAnnotationType(annotations[i]).startsWith("Breakpoint")) {
                            found = true;
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
            MainWindowOperator.getDefault().waitStatusText("Finished building " + projectName + " (" + target + ")");
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
                    JemmyProperties.getProperties().getOutput().print(">>>>> status text: " + StatusDisplayer.getDefault().getStatusText() + " > " + anObject + "\n");
                    if (StatusDisplayer.getDefault().getStatusText().startsWith(text)) {
                        return Boolean.TRUE;
                    }
                    return null;
                }

                public String getDescription() {
                    return "Wait status text prefix: " + text;
                }
            }).waitAction(StatusDisplayer.getDefault());
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public static int getDebuggerConsoleStatus() {
        return new OutputTabOperator(debuggerConsoleTitle).getLineCount();
    }

/*    public static int waitDebuggerConsole(final String text, final int status) {
        OutputTabOperator op = new OutputTabOperator(debuggerConsoleTitle);
        ConsoleChooser cch = new ConsoleChooser(op, text, status);
        JemmyProperties.getCurrentOutput().printLine("Waiting on text in debugger console '" + text + "' from line " + status);
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", 30000);
        try {
            op.waitState(cch);
        } catch (TimeoutExpiredException ex) {
            JemmyProperties.getCurrentOutput().printLine("Not found text in debugger console:");
            JemmyProperties.getCurrentOutput().printLine(op.getText());
            throw ex;
        }
        JemmyProperties.getCurrentOutput().printLine("Found text in debugger console '" + text + "' at line " + cch.getLastIndex());
        return cch.getLastIndex();
    }*/

    public static boolean checkConsoleForText(String text, int startLine) {
        OutputTabOperator op = new OutputTabOperator(debuggerConsoleTitle);
        for (int i = startLine; i < op.getLineCount(); i++) {
            if (op.getLine(i).startsWith(text)) {
                return true;
            }
        }
        return false;
    }

    public static int checkConsoleForNumberOfOccurrences(String text, int startLine) {
        OutputTabOperator op = new OutputTabOperator(debuggerConsoleTitle);
        int number = 0;
        for (int i = startLine; i < op.getLineCount(); i++) {
            if (op.getLine(i).startsWith(text)) {
                number++;
            }
        }
        return number;
    }

    public static void captureScreen(JellyTestCase testCase) {
        try {
            PNGEncoder.captureScreen(testCase.getWorkDir().getAbsolutePath() + File.separator + "screenBeforeCleanup.png");
        } catch (Exception e1) {
            // ignore it
        }
    }

    static class ConsoleChooser implements ComponentChooser {

        int lastIndex = 0;
        OutputTabOperator op;
        String text;
        int status;

        public ConsoleChooser(OutputTabOperator op, String text, int status) {
            this.op = op;
            this.text = text;
            this.status = status;
        }

        public boolean checkComponent(Component comp) {
            for (int i = status; i < op.getLineCount(); i++) {
                if (op.getLine(i).startsWith(text)) {
                    lastIndex = i;
                    return true;
                }
            }
            return false;
        }

        public String getDescription() {
            return "\"" + text + "\" text";
        }

        public int getLastIndex() {
            return lastIndex;
        }
    }
}
