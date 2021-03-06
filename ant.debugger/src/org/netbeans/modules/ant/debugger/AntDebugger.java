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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.ant.debugger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.support.TargetLister;
import org.apache.tools.ant.module.spi.AntEvent;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.ant.debugger.breakpoints.AntBreakpoint;
import org.netbeans.modules.ant.debugger.breakpoints.BreakpointModel;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotatable;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;
import org.w3c.dom.Element;

/**
 * Ant debugger.
 *
 * @author  Honza
 */
public class AntDebugger extends ActionsProviderSupport {

    private static final Logger logger = Logger.getLogger(AntDebugger.class.getName());
    
    /** The ReqeustProcessor used by action performers. */
    private static RequestProcessor     actionsRequestProcessor;
    
    private AntProjectCookie            antCookie;
    private AntDebuggerEngineProvider   engineProvider;
    private ContextProvider             contextProvider;
    private ExecutorTask                execTask;
    private final Object                LOCK = new Object ();
    private final Object                LOCK_ACTIONS = new Object();
    private boolean                     actionRunning = false;
    private IOManager                   ioManager;
    private Object                      currentLine;
    private LinkedList                  callStackList = new LinkedList();
    private File                        currentFile;
    private String                      currentTargetName;
    private String                      currentTaskName;
    private int                         originatingIndex = -1; // Current index of the virtual originating target in the call stack
    
    private VariablesModel              variablesModel;
    private WatchesModel                watchesModel;
    private BreakpointModel             breakpointModel;
    
    public AntDebugger (
        ContextProvider contextProvider
    ) {
        
        this.contextProvider = contextProvider;
        
        // init antCookie
        antCookie = contextProvider.lookupFirst(null, AntProjectCookie.class);
        
        // init engineProvider
        engineProvider = (AntDebuggerEngineProvider) contextProvider.lookupFirst 
            (null, DebuggerEngineProvider.class);
                
        // init actions
        for (Iterator it = actions.iterator(); it.hasNext(); ) {
            setEnabled (it.next(), true);
        }
                
        ioManager = new IOManager (antCookie.getFile ().getName ());
    }
    
    void setExecutor(ExecutorTask execTask) {
        this.execTask = execTask;
        if (execTask != null) {
            execTask.addTaskListener(new TaskListener() {
                public void taskFinished(org.openide.util.Task task) {
                    // The ANT task was finished
                    finish();
                }
            });
        }
    }
    
    
    // ActionsProvider .........................................................
    
    private static final Set actions = new HashSet ();
    static {
        actions.add (ActionsManager.ACTION_KILL);
        actions.add (ActionsManager.ACTION_CONTINUE);
        actions.add (ActionsManager.ACTION_START);
        actions.add (ActionsManager.ACTION_STEP_INTO);
        actions.add (ActionsManager.ACTION_STEP_OVER);
        actions.add (ActionsManager.ACTION_STEP_OUT);
    }
    
    public Set getActions () {
        return actions;
    }
        
    public void doAction (Object action) {
        synchronized (LOCK_ACTIONS) {
            actionRunning = true;
        }
        logger.fine("AntDebugger.doAction("+action+"), is kill = "+(action == ActionsManager.ACTION_KILL));
        if (action == ActionsManager.ACTION_KILL) {
            finish ();
        } else
        if (action == ActionsManager.ACTION_CONTINUE) {
            doContinue ();
        } else
        if (action == ActionsManager.ACTION_START) {
            return ;
        } else
        if ( action == ActionsManager.ACTION_STEP_INTO ||
             action == ActionsManager.ACTION_STEP_OUT ||
             action == ActionsManager.ACTION_STEP_OVER
        ) {
            doStep (action);
        }
        synchronized (LOCK_ACTIONS) {
            if (actionRunning) {
                try {
                    LOCK_ACTIONS.wait();
                } catch (InterruptedException iex) {}
            }
        }
    }
    
    @Override
    public void postAction(final Object action, final Runnable actionPerformedNotifier) {
        for (Iterator it = actions.iterator(); it.hasNext(); ) {
            setEnabled (it.next(), false);
        }
        synchronized (AntDebugger.class) {
            if (actionsRequestProcessor == null) {
                actionsRequestProcessor = new RequestProcessor("Ant debugger actions RP", 1);
            }
        }
        actionsRequestProcessor.post(new Runnable() {
            public void run() {
                try {
                    doAction(action);
                } finally {
                    actionPerformedNotifier.run();
                    for (Iterator it = actions.iterator(); it.hasNext(); ) {
                        setEnabled (it.next(), true);
                    }
                }
            }
        });
    }
    
    
    // other methods ...........................................................
    
    private AntEvent lastEvent;
    
    /**
     * Called from DebuggerAntLogger.
     */
    void taskStarted (AntEvent event) {
        if (finished) return ;
        if (logger.isLoggable(Level.FINE)) logger.fine("AntDebugger.taskStarted("+event+")");
        Object taskLine = Utils.getLine (event);
        callStackList.addFirst(
                new Task (event.getTaskStructure (), 
                          taskLine, 
                          event.getScriptLocation ()));
        currentTaskName = event.getTaskStructure().getName();
        originatingIndex = 0;
        elementStarted(event);
    }
    
    private void elementStarted(AntEvent event) {
        if (logger.isLoggable(Level.FINE)) logger.fine("AntDebugger.elementStarted("+event+"), doStop = "+doStop);
        if (finished) return ;
        if (!doStop) {
            if (!onBreakpoint ()) {
                logger.fine(" Not on breakpoint, continuing...");
                return ; // continue
            } else logger.fine(" Is on breakpoint.");
        }
        logger.fine("AntDebugger.elementStarted() stopping...");
        stopHere(event);
        logger.fine("AntDebugger.elementStarted() finished.");
    }
    
    private void stopHere(AntEvent event) {
        synchronized (this) {
            lastEvent = event;
        }
        updateUI();
        currentFile = event.getScriptLocation();
        // update variable values
        Set properties = event.getPropertyNames ();
        variables = (String[]) properties.toArray 
            (new String [properties.size ()]);
        fireVariables ();
        fireWatches ();
        fireBreakpoints ();
        
        // enable actions
        synchronized (LOCK_ACTIONS) {
            actionRunning = false;
            LOCK_ACTIONS.notifyAll();
        }
        
        // wait for next stepping orders
        synchronized (LOCK) {
            try {
                if (logger.isLoggable(Level.FINE)) logger.fine("stopHere(): waiting in thread '"+Thread.currentThread()+"' ...");
                LOCK.wait ();
                if (logger.isLoggable(Level.FINE)) logger.fine("stopHere(): wait in thread '"+Thread.currentThread()+"' notified.");
            } catch (InterruptedException ex) {
                logger.fine("AntDebugger.stopHere() was interrupted.");
                Thread.currentThread().interrupt();
            }
        }
        synchronized (this) {
            lastEvent = null;
        }
    }
    
    void taskFinished (AntEvent event) {
        if (finished) return ;
        callStackList.remove(0);//(callStackList.size() - 1);
        if (taskEndToStopAt != null &&
            taskEndToStopAt.equals(event.getTaskStructure().getName()) &&
            event.getScriptLocation().equals(fileToStopAt)) {
            
            if (targetEndToStopAt != null) {
                if (targetEndToStopAt.equals(event.getTargetName())) {
                    targetEndToStopAt = null;
                    taskEndToStopAt = null;
                    fileToStopAt = null;
                    doStop = true;
                }
            } else {
                taskEndToStopAt = null;
                fileToStopAt = null;
                doStop = true;
            }
        }
    }
    
    /**
     * Called from DebuggerAntLogger.
     */
    void buildFinished (AntEvent event) {
        engineProvider.getDestructor ().killEngine ();
        ioManager.closeStream ();
        Utils.unmarkCurrent ();
        // finish actions
        synchronized (LOCK_ACTIONS) {
            actionRunning = false;
            LOCK_ACTIONS.notifyAll();
        }
    }
    
    void targetStarted(AntEvent event) {
        if (finished) return ;
        String targetName = event.getTargetName();
        //updateTargetsByName(event.getScriptLocation());
        TargetLister.Target target = findTarget(targetName, event.getScriptLocation());
        
        List originatingTargets = null;
        if (callStackList.size() > 0) {
            Object topFrame = callStackList.get(0);
            if (topFrame instanceof Task) {
                Task t1 = (Task) topFrame;
                String startingTargetName = t1.getTaskStructure().getAttribute("target");
                if (startingTargetName != null && !targetName.equals(startingTargetName)) {
                    originatingTargets = findPath(event.getScriptLocation(), startingTargetName, targetName);
                }
            } else if (topFrame instanceof TargetLister.Target) {
                String start = ((TargetLister.Target) topFrame).getName();
                List path = findPath (event.getScriptLocation(), start, targetName);
                if (path != null) {
                    callStackList.removeFirst();
                    originatingTargets = path;
                }
            } else if (topFrame instanceof TargetOriginating) {
                String start = ((TargetOriginating) topFrame).getOriginatingTarget().getName();
                if (start.equals(targetName)) {
                    callStackList.removeFirst();
                    originatingIndex--;
                } else {
                    List path = findPath (event.getScriptLocation(), start, targetName);
                    if (path != null) {
                        callStackList.removeFirst();
                        originatingTargets = path;
                    }
                }
            }
        } else {
            String[] sessionOriginatingTargets = event.getSession ().getOriginatingTargets();
            int l = sessionOriginatingTargets.length;
            for (int i = 0; i < l; i++) {
                String start = sessionOriginatingTargets [i];
                if (start.equals(targetName)) continue;
                List path = findPath (event.getScriptLocation(), start, targetName);
                if (path != null) {
                    originatingTargets = path;
                    break;
                }
            //originatingTargets = getOriginatingTargets(null, target);
            }
        }
        if (originatingTargets != null) {
            originatingIndex = originatingTargets.size();
            callStackList.addAll(0, originatingTargets);
        } else {
            originatingIndex = 0;
        }
        //callStackList.add(getOriginatingTargets(start, target));
        
        Object topFrame = (callStackList.size()) > 0 ? callStackList.getFirst() : null;
        if (topFrame instanceof TargetOriginating) {
            if (((TargetOriginating) topFrame).getOriginatingTarget().getName().equals(targetName)) {
                callStackList.removeFirst();
                originatingIndex--;
            }
        }
        
        callStackList.addFirst(target);
        currentTargetName = targetName;
        currentTaskName = null;
        elementStarted(event);
    }
    
    void targetFinished(AntEvent event) {
        if (finished) return ;
        callStackList.remove(0);//(callStackList.size() - 1);
        if (targetEndToStopAt != null && targetEndToStopAt.equals(event.getTargetName()) &&
            fileToStopAt.equals(event.getScriptLocation())) {
                targetEndToStopAt = null;
                taskEndToStopAt = null;
                fileToStopAt = null;
                doStop = true;
        }
        currentTargetName = null;
    }
    
    private Object getTopFrame() {
        Object topFrame;
        if (originatingIndex > 0) {
            topFrame = callStackList.get(originatingIndex);
        } else {
            topFrame = callStackList.get(0);
        }
        if (topFrame instanceof TargetOriginating) {
            topFrame = ((TargetOriginating) topFrame).getOriginatingTarget();
        }
        return topFrame;
    }
    
    private void updateUI () {
        /*TargetLister.Target nextTarget = getNextTarget ();
        String nextTargetName = nextTarget == null ?
            null : nextTarget.getName ();*/
        Object topFrame;
        String nextTargetName = null;
        if (originatingIndex > 0) {
            topFrame = callStackList.get(originatingIndex);
        } else {
            topFrame = callStackList.get(0);
        }
        if (topFrame instanceof TargetOriginating) {
            TargetLister.Target nextTarget = ((TargetOriginating) topFrame).getDependentTarget();
            nextTargetName = nextTarget.getName();
            topFrame = ((TargetOriginating) topFrame).getOriginatingTarget();
        }
        currentLine = topFrame instanceof Task ?
            ((Task) topFrame).getLine () :
            Utils.getLine (
                (TargetLister.Target) topFrame, 
                nextTargetName
            );
        updateOutputWindow (currentLine);
        Utils.markCurrent (currentLine);
        getCallStackModel ().fireChanges ();
    }
    
    private void updateOutputWindow (Object currentLine) {
        Object topFrame = getTopFrame();
        if (topFrame instanceof Task) {
            Task task = (Task) topFrame;
            ioManager.println (
                task.getFile ().getName () + ":" + 
                    (Utils.getLineNumber (currentLine) + 1) + 
                    ": Task " + getStackAsString (), 
                currentLine
             );
        } else {
            TargetLister.Target target = (TargetLister.Target) topFrame;
            ioManager.println (
                target.getScript ().getFile ().getName () + ":" + 
                    (Utils.getLineNumber (currentLine) + 1) + 
                    ": Target " + getStackAsString (), 
                currentLine
             );
        }
    }
    
    private String getStackAsString () {
        StringBuffer sb = new StringBuffer ();
        int i = callStackList.size() - 1;
        sb.append (getFrameName (callStackList.get(i--)));
        int end = Math.max(0, originatingIndex);
        while (i >= end)
            sb.append ('.').append (getFrameName (callStackList.get(i--)));
        return new String (sb);
    }
    
    private static String getFrameName (Object frame) {
        if (frame instanceof TargetOriginating) {
            frame = ((TargetOriginating) frame).getOriginatingTarget();
        }
        return frame instanceof Task ?
            ((Task) frame).getTaskStructure ().getName () :
            ((TargetLister.Target) frame).getName ();
    }
    
    private Map watches = new HashMap ();
    
    private boolean onBreakpoint () {
        // 1) stop on watch value change
        Watch[] ws = DebuggerManager.getDebuggerManager ().
            getWatches ();
        int j, jj = ws.length;
        for (j = 0; j < jj; j++) {
            Object value = getVariableValue (ws [j].getExpression ());
            if (value == null) value = new Integer (0);
            if ( watches.containsKey (ws [j].getExpression ()) &&
                 !watches.get (ws [j].getExpression ()).equals (value)
            ) {
                /* SOME NONSENSE ???
                callStack = new Object [jj - j];
                System.arraycopy 
                    (callStackInternal, j, callStack, 0, jj - j);
                 */
                watches.put (
                    ws [j].getExpression (), 
                    value
                );
                return true;
            } else
                watches.put (
                    ws [j].getExpression (), 
                    value
                );
        }
        
        // 2) check line breakpoints
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager ().
            getBreakpoints ();
        jj = callStackList.size();
        if (jj >= 1) {
            Object frame = callStackList.getFirst();
            if (frame instanceof TargetOriginating) {
                frame = ((TargetOriginating) frame).getOriginatingTarget();
            }
            Object line = frame instanceof Task ?
                ((Task) frame).getLine () :
                Utils.getLine (
                    (TargetLister.Target) frame, 
                    null
                );
            if (line != null) {
                line = new Annotatable[] { ((Annotatable[]) line)[0] };
            }
            int i, k = breakpoints.length;
            for (i = 0; i < k; i++)
                if ( breakpoints [i] instanceof AntBreakpoint &&
                     breakpoints [i].isEnabled() &&
                     Utils.contains (
                         line,
                         ((AntBreakpoint) breakpoints [i]).getLine ()

                     )
                ) {
                    //callStack = new Object [jj - j];
                    //callStackList.subList(0, jj - j).toArray(callStack);
                    //System.arraycopy 
                    //    (callStackInternal, j, callStack, 0, jj - j);
                    return true;
                }
        }
        return false;
    }

    public Object getCurrentLine () {
        return currentLine;
    }
    
    
    // stepping hell ...........................................................
    
    private Object      lastAction;
    
    private String      targetEndToStopAt = null;
    private String      taskEndToStopAt = null;
    private File        fileToStopAt = null;
    private volatile boolean doStop = true; // stop on the next task/target
    private volatile boolean finished = false; // When the debugger has finished.
    
    private void doContinue () {
        Utils.unmarkCurrent ();
        //lastAction = ActionsManager.ACTION_CONTINUE;
        doStop = false;
        targetEndToStopAt = null;
        taskEndToStopAt = null;
        fileToStopAt = null;
        doEngineStep ();
    }

    /**
     * should define callStack based on callStackInternal & action.
     */
    private void doStep (Object action) {
        if (action == ActionsManager.ACTION_STEP_INTO) {
            if (originatingIndex > 0) {
                originatingIndex--;
                updateUI();
                // enable actions
                synchronized (LOCK_ACTIONS) {
                    actionRunning = false;
                    LOCK_ACTIONS.notifyAll();
                }
                return ;
            }
            doStop = true;
        } else if (action == ActionsManager.ACTION_STEP_OVER) {
            if (originatingIndex > 0) {
                Object frame = callStackList.get(originatingIndex);
                TargetLister.Target dep = ((TargetOriginating) frame).getDependentTarget();
                targetEndToStopAt = dep.getName();
                taskEndToStopAt = null;
                fileToStopAt = currentFile;
                doStop = false;
            } else {
                taskEndToStopAt = currentTaskName;
                targetEndToStopAt = currentTargetName;
                fileToStopAt = currentFile;
                doStop = false;
            }
        } else if (action == ActionsManager.ACTION_STEP_OUT) {
            if (originatingIndex > 1) {
                Object frame = callStackList.get(originatingIndex - 1);
                TargetLister.Target dep = ((TargetOriginating) frame).getDependentTarget();
                targetEndToStopAt = dep.getName();
                taskEndToStopAt = null;
                fileToStopAt = currentFile;
                doStop = false;
            }
            if (callStackList.size() > 1) {
                Object frame = callStackList.get(1);
                if (frame instanceof Task) {
                    taskEndToStopAt = ((Task) frame).getTaskStructure().getName();
                    for (int i = 2; i < callStackList.size(); i++) {
                        frame = callStackList.get(i);
                        if (frame instanceof String) {
                            targetEndToStopAt = ((TargetLister.Target) frame).getName();
                            break;
                        }
                    }
                } else {
                    if (frame instanceof TargetOriginating) {
                        targetEndToStopAt = ((TargetOriginating) frame).getOriginatingTarget().getName();
                    } else {
                        targetEndToStopAt = ((TargetLister.Target) frame).getName();
                    }
                }
                fileToStopAt = currentFile;
                doStop = false;
            }
        } else {
            throw new IllegalArgumentException(action.toString());
        }
        doEngineStep();
        //S ystem.out.println("doStep - end");
    }
    
    
    private void doEngineStep () {
        //S ystem.out.println("doEngineStep " + doNotStopInTarget);
        synchronized (LOCK) {
            LOCK.notify ();
        }
    }
    
    private void finish () {
        logger.fine("AntDebugger.finish()");
        if (finished) {
            logger.fine("finish(): already finished.");
            return ;
        }
        if (execTask != null) {
            execTask.stop();
        }
        logger.fine("finish(): task stopped.");
        Utils.unmarkCurrent ();
        doStop = false;
        finished = true;
        taskEndToStopAt = null;
        targetEndToStopAt = null;
        fileToStopAt = null;
        synchronized (LOCK) {
            LOCK.notify ();
        }
        logger.fine("finish(): notify called.");
        buildFinished(null);
        logger.fine("finish() done, build finished.");
    }
    
    
    // support for call stack ..................................................
    
    private CallStackModel              callStackModel;

    private CallStackModel getCallStackModel () {
        if (callStackModel == null)
            callStackModel = (CallStackModel) contextProvider.lookupFirst 
                ("CallStackView", TreeModel.class);
        return callStackModel;
    }
    
    
    Object[] getCallStack () {
        //System.out.println("Orig call stack = "+java.util.Arrays.asList(callStack));
        //System.out.println("NEW call stack  = "+callStackList);
        Object[] callStack;
        if (originatingIndex > 0) {
            callStack = callStackList.subList(originatingIndex, callStackList.size()).toArray();
        } else {
            callStack = callStackList.toArray();
        }
        for (int i = 0; i < callStack.length; i++) {
            if (callStack[i] instanceof TargetOriginating) {
                callStack[i] = ((TargetOriginating) callStack[i]).getOriginatingTarget();
            }
        }
        return callStack;
    }
    
    private LinkedList findPath (
        File file,
        String start,
        String end
    ) {
        TargetLister.Target t = findTarget(start, file);
        if (t == null) {
            return null; // A non-existing target referenced
        }
        if (start.equals (end)) {
            LinkedList ll = new LinkedList ();
            ll.addFirst (new TargetOriginating(null, t));
            return ll;
        }
        String depends = t.getElement ().getAttribute ("depends");
        StringTokenizer st = new StringTokenizer (depends, ",");
        while (st.hasMoreTokens ()) {
            String newStart = st.nextToken ().trim();
            LinkedList ll = findPath (
                file,
                newStart,
                end
            );
            if (ll == null) continue;
            TargetOriginating to = (TargetOriginating) ll.getLast();
            if (to.getOriginatingTarget() == null) {
                to.setOriginatingTarget(findTarget(start, file));
            } else {
                ll.addLast(new TargetOriginating(findTarget(start, file), to.getOriginatingTarget()));
            }
            return ll;
        }
        return null;
    }
    
    
    
    /**
     * File as a script location is a key. Values are maps of name to Target.
     */
    private Map nameToTargetByFiles = new HashMap();
    /**
     * File as a script location is a key, values are project names.
     */
    private Map projectNamesByFiles = new HashMap();
    
    private synchronized TargetLister.Target findTarget(String name, File file) {
        Map nameToTarget = (Map) nameToTargetByFiles.get(file);
        if (nameToTarget == null) {
            nameToTarget = new HashMap ();
            FileObject fo = FileUtil.toFileObject(file);
            DataObject dob;
            try {
                dob = DataObject.find (fo);
            } catch (DataObjectNotFoundException donfex) {
                throw new IllegalStateException(donfex.getLocalizedMessage());
            }
            AntProjectCookie ant = (AntProjectCookie) dob.getCookie 
                (AntProjectCookie.class);
            Element proj = ant.getProjectElement();
            if (proj != null) {
                String projName = proj.getAttribute("name");
                projectNamesByFiles.put(file, projName);
            }
            try {
                Set targets = TargetLister.getTargets (ant);
                Iterator it = targets.iterator ();
                while (it.hasNext ()) {
                    TargetLister.Target t = (TargetLister.Target) it.next ();
                    nameToTarget.put (t.getName (), t);
                }
            } catch (IOException ioex) {
                // Ignore - we'll have an empty map
            }
            nameToTargetByFiles.put(file, nameToTarget);
        }
        TargetLister.Target target = (TargetLister.Target) nameToTarget.get(name);
        if (target == null) {
            String projName = (String) projectNamesByFiles.get(file);
            if (name.startsWith(projName+".")) {
                name = name.substring(projName.length() + 1);
                target = (TargetLister.Target) nameToTarget.get(name);
            }
        }
        return target;
    }
    
    
    // support for variables ...................................................
    
    synchronized void setVariablesModel(VariablesModel variablesModel) {
        this.variablesModel = variablesModel;
    }

    synchronized void setWatchesModel(WatchesModel watchesModel) {
        this.watchesModel = watchesModel;
    }

    private void fireVariables () {
        synchronized(this) {
            if (variablesModel == null) {
                return ;
            }
        }
        variablesModel.fireChanges();
    }
    
    private void fireWatches () {
        synchronized(this) {
            if (watchesModel == null) {
                return ;
            }
        }
        watchesModel.fireChanges();
    }
    
    private void fireBreakpoints () {
        synchronized(this) {
            if (breakpointModel == null) {
                Iterator it = DebuggerManager.getDebuggerManager ().lookup 
                        ("BreakpointsView", NodeModel.class).iterator ();
                while (it.hasNext ()) {
                    NodeModel model = (NodeModel) it.next ();
                    if (model instanceof BreakpointModel) {
                        breakpointModel = (BreakpointModel) model;
                        break;
                    }
                }
            }
        }
        breakpointModel.fireChanges();
    }
    
    String evaluate (String expression) {
        String value = getVariableValue (expression);
        if (value != null) return value;
        synchronized (this) {
            if (lastEvent == null) return null;
            return lastEvent.evaluate (expression);
        }
    }

    private String[] variables = new String [0];
    
    String[] getVariables () {
        return variables;
    }
    
    String getVariableValue (String variableName) {
        synchronized (this) {
            if (lastEvent == null) return null;
            return lastEvent.getProperty (variableName);
        }
    }

    /**
     * The originating target, that was not entered yet, but is causing another
     * target to be entered.
     *
     * @author  Martin Entlicher
     */
    private static class TargetOriginating {

        private TargetLister.Target target;
        private TargetLister.Target dependent;

        /**
         * Creates a new TargetOriginating object.
         * @param target the originating target
         * @param dependent the target depending upon the originating one
         */
        TargetOriginating (
            TargetLister.Target   target,
            TargetLister.Target dependent
        ) {
            this.target = target;
            this.dependent = dependent;
        }

        TargetLister.Target getOriginatingTarget () {
            return target;
        }

        void setOriginatingTarget (TargetLister.Target target) {
            this.target = target;
        }

        TargetLister.Target getDependentTarget () {
            return dependent;
        }

    }

}
