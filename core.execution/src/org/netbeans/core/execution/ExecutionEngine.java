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

package org.netbeans.core.execution;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;

import org.openide.execution.NbClassPath;
import org.openide.execution.ExecutorTask;
import org.openide.util.Lookup;
import org.openide.windows.InputOutput;

import org.netbeans.core.NbTopManager;

/** Execution that provides support for starting a class with main
*
* @author Ales Novak
*
* The class handles redirecting of out/in/err for all tasks in the system.
* First instance of TaskIO is created for Corona. So call System.out.println()
* from Corona is redirected to a window.
* Situation for executed task is following it uses System.out/err/in - because
* the task is in some threadgroup it will be recognized and new panel in window
* for that task will be created. Further System.out.println() means that
* (in System.out is our class now - SysOut) calling thread is found and its threadgroup
* is examined - SysOut propagates call to taskIOs in ExecutionEngine. IOTable
* look for mapping the threadgroup to TaskIO class (it may create that). TaskIO
* is created uninitialized. So if only out is used, err/in are never initialized.
* Initializing is lazy - for request. TaskIO.out is an instance of SysPrintStream,
* that is redirected to OutputWriter that is redirected to a window.
*/
@SuppressWarnings("deprecation") // createLibraryPath
@org.openide.util.lookup.ServiceProvider(service=org.openide.execution.ExecutionEngine.class)
public final class
    ExecutionEngine extends org.openide.execution.ExecutionEngine {

    /** base group for all running tasks */
    public static final ThreadGroup base = new ThreadGroup("base"); // NOI18N

    /** used for naming groups */
    private int number = 1;

    /** IO class for corona */
    public static final TaskIO systemIO = new TaskIO();

    /** maps ThreadGroups to TaskIO */
    private static IOTable taskIOs;

    /* table of window:threadgrp */
    static private WindowTable wtable = new WindowTable();

    /** list of ExecutionListeners */
    private HashSet<ExecutionListener> executionListeners = new HashSet<ExecutionListener>();

    /** List of running executions */
    private List<ExecutorTask> runningTasks = Collections.synchronizedList(new ArrayList<ExecutorTask>( 5 ));

    static {
        systemIO.out = new OutputStreamWriter(System.out);
        systemIO.err = new OutputStreamWriter(System.err);
        systemIO.in = new java.io.InputStreamReader(System.in);
    }

    static final long serialVersionUID =9072488605180080803L;

    public ExecutionEngine () {
        /* SysIn is a class that redirects System.in of some running task to
           a window (probably OutWindow).
           SysOut/Err are classes that redirect out/err to the window
        */
        System.setIn(new SysIn());
        System.setOut(createPrintStream(true));
        System.setErr(createPrintStream(false));        
    }

    /** Get the default ExecutionEngine instance.
     * @return the instance, or null if none could be found
     */
    public static ExecutionEngine getExecutionEngine() {
        ExecutionEngine ee = Lookup.getDefault().lookup(ExecutionEngine.class);
        if (ee != null) return ee;
        org.openide.execution.ExecutionEngine ee2 = Lookup.getDefault().lookup(org.openide.execution.ExecutionEngine.class);
        if (ee2 instanceof ExecutionEngine) return (ExecutionEngine)ee2;
        return null;
    }
    
    /** Returns a snapshot of a collection of tasks which did not ended yet */
    public Collection<ExecutorTask> getRunningTasks() {
        // toArray is atomic on synchronized list, contrary to just passing
        // the list to a Collection constructor.
        return Arrays.asList(runningTasks.toArray(new ExecutorTask[0]));
    }
    
    /** Returns name of running task */
    public String getRunningTaskName( ExecutorTask task ) {
        if ( !runningTasks.contains( task ) ||
             !(task instanceof DefaultSysProcess) ) {
            return null;
        }
        else {
            return ((DefaultSysProcess)task).getName();
        }
    }

    /** Should prepare environment for Executor and start it. Is called from
    * Executor.execute method.
    *
    * @param executor to start
    * @param info about class to start
    */
    public ExecutorTask execute(String name, Runnable run, InputOutput inout) { 
        TaskThreadGroup g = new TaskThreadGroup(base, "exec_" + name + "_" + number); // NOI18N
        g.setDaemon(true);
        ExecutorTaskImpl task = new ExecutorTaskImpl();
        synchronized (task.lock) {
            try {
                new RunClassThread(g, name, number++, inout, this, task, run);
                task.lock.wait();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e.getMessage());
            }
        }
        return task;
    }

    /** Method that allows implementor of the execution engine to provide
    * class path to all libraries that one could find useful for development
    * in the system.
    *
    * @return class path to libraries
    */
    protected NbClassPath createLibraryPath() {
        List<File> l = NbTopManager.getModuleJars();
        return new NbClassPath (l.toArray (new File[l.size ()]));
    }

    /** adds a listener */
    public final void addExecutionListener (ExecutionListener l) {
        synchronized (executionListeners) {
            executionListeners.add(l);
        }
    }

    /** removes a listener */
    public final void removeExecutionListener (ExecutionListener l) {
        synchronized (executionListeners) {
            executionListeners.remove(l);
        }
    }

    /** Creates new PermissionCollection for given CodeSource and given PermissionCollection.
     * @param cs a CodeSource
     * @param io an InputOutput
     * @return PermissionCollection for given CodeSource and InputOutput
     */
    protected final PermissionCollection createPermissions(CodeSource cs, InputOutput io) {
        PermissionCollection pc = Policy.getPolicy().getPermissions(cs);
        ThreadGroup grp = Thread.currentThread().getThreadGroup();
        return new IOPermissionCollection(io, pc, (grp instanceof TaskThreadGroup ? (TaskThreadGroup) grp: null));
    }

    /** fires event that notifies about new process */
    protected final void fireExecutionStarted (ExecutionEvent ev) {
        runningTasks.add( ev.getProcess() );
	@SuppressWarnings("unchecked") 
        Iterator<ExecutionListener> iter = ((HashSet<ExecutionListener>) executionListeners.clone()).iterator();
        while (iter.hasNext()) {
            ExecutionListener l = iter.next();
            l.startedExecution(ev);
        }
    }

    /** fires event that notifies about the end of a process */
    protected final void fireExecutionFinished (ExecutionEvent ev) {
        runningTasks.remove( ev.getProcess() );
	@SuppressWarnings("unchecked") 
        Iterator<ExecutionListener> iter = ((HashSet<ExecutionListener>) executionListeners.clone()).iterator();
        while (iter.hasNext()) {
            ExecutionListener l = iter.next();
            l.finishedExecution(ev);
        }
        ev.getProcess().destroyThreadGroup(base);
    }

    static void putWindow(java.awt.Window w, TaskThreadGroup tg) {
        wtable.putTaskWindow(w, tg);
    }
    static void closeGroup(ThreadGroup tg) {
        wtable.closeGroup(tg);
    }
    static boolean hasWindows(ThreadGroup tg) {
        return wtable.hasWindows(tg);
    }

    /**
    * @return IOTable with couples ThreadGroup:TaskIO
    */
    static IOTable getTaskIOs() {
        if (taskIOs == null) {
            taskIOs = new IOTable(base, systemIO);
        }
        return taskIOs;
    }

    /** finds top thread group of the calling thread
    * @return null iff the calling thread is not in any exec group
    * or exec group of calling thread
    */
    public static ThreadGroup findGroup () {
        ThreadGroup g = Thread.currentThread().getThreadGroup ();
        ThreadGroup old = null;
        while (g != null && g != base) {
            old = g;
            g = g.getParent ();
        }
        return (g == null) ? null : old;
    }

    /** The OutputStream redirects output on behalf of task */
    static class SysOut extends OutputStream {

        /** Is it err or std out? */
        boolean std;

        /** Creates new Stream */
        SysOut(boolean std) {
            this.std = std;
        }

        public void write(int b) throws IOException {
            if (std) {
                getTaskIOs().getOut().write(b);
            } else {
                getTaskIOs().getErr().write (b);
            }
        }

        @Override
        public void write(byte[] buff, int off, int len) throws IOException {
            String s = new String (buff, off, len);
            if (std) {
                getTaskIOs().getOut().write(s.toCharArray(), 0, s.length());
            } else {
                getTaskIOs().getErr().write(s.toCharArray(), 0, s.length());
            }
        }

        @Override
        public void flush() throws IOException {
            if (std) {
                getTaskIOs().getOut().flush();
            } else {
                getTaskIOs().getErr().flush();
            }
        }

        @Override
        public void close() throws IOException {
            if (std) {
                getTaskIOs().getOut().close();
            } else {
                getTaskIOs().getErr().close();
            }
        }
    }

    static PrintStream createPrintStream(boolean stdOut) {
        return new WriterPrintStream(new SysOut(stdOut), stdOut);
    }
   
}
