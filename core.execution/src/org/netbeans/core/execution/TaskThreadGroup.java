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

package org.netbeans.core.execution;


/**
*
* @author Ales Novak
* @version 0.10 Mar 19, 1998
*/
final class TaskThreadGroup extends ThreadGroup {
    /** lock for waitFor/wakeUpProcesses */
    private Object TIMER = new Object();

    /** true if RunClass thread entered waitFor - out of main method */
    private boolean runClassThreadOut = false;

    /** true iff the ThreadGroup can be finalized - that is after all threads die */
    private boolean finalizable;

    /** Is the process dead? The group can be marked as dead but still have running threads */
    private boolean dead;
    
    /** reference to a RunClassThread */
    private RunClassThread runClassThread;

    TaskThreadGroup(ThreadGroup parent, String name) {
        super(parent, name);

        dead = false;
    }

    /**
    * @return true iff there is not any window there is not more than two threads
    * and first one if any must be waiting for next event in the queue (EventDispatchThread) and
    * the second must be instanceof ExecutionEngine$RunClass thread and must wait
    * external processes are never "dead"
    */
    private boolean isProcessDead() {

        if (dead) {
            return true;
        }

        int count;
        // first System.out.println must be before sync block
        if (! finalizable) {
            return false;
        }
        synchronized (this) {
            count = activeCount();

            /* Following lines patch bug in implementaion of java.lang.-Thread/ThreadGroup
               The bug - if new Thread(...) call is issued and the thread is not started.
               The thread is added as a member to its thread group - such threads are counted in
               calls activeCount() and enumerate(Thread[] ts, ...) although they do not live - e.g
               call destroy() to threadgroup always throws an exception. Solution is to start suspicious
               threads. I added try - catch block for already started threads.
               This is patch for some code used inside Swing - code like this " ... = new Thread().getThreadGroup();"
               see - javax.swing.SystemEventQueueUtilities$RunnableCanvas.maybeRegisterEventDispatchThread()
               Original error was that TaskThreadGroups that had not any threads at thread dump were not removed.
               !!! remove spagetti code if the bug is repaired in JDK.
            */
            if (count > 1) {
                int active = 0;
                Thread[] threads =
                    new Thread[count];
                enumerate(threads);
                for (int i = threads.length; --i >= 0;) {
                    // #33630 - ignore daemon threads
                    if ((threads[i] != null) &&
                            (threads[i].isAlive() && !threads[i].isDaemon())
                       ) {
                        if (++active > 1) {
                            return false;
                        }
                    }
                }
                count = active;
            }

            if (ExecutionEngine.hasWindows(this)) {
                return false;
            }

            if (count == 0) {
                return true; // no thread, no wins
            }

            // one thread remains - RunClass
            if (runClassThreadOut) {
                return true;
            }
            return false;
        }
    }

    /** blocks until this ThreadGroup die - isProcessDead = true
    */
    void waitFor() throws InterruptedException {
        synchronized (TIMER) {
            try {
                while (! isProcessDead()) {
                    if (Thread.currentThread() instanceof RunClassThread) {
                        runClassThreadOut = true;
                    }
                    TIMER.wait(1000);
                }
            } finally {
                TIMER.notifyAll();
                dead = true;
            }
        }
    }
    
    /** Marks this group as finalizable. */
    void setFinalizable() {
        finalizable = true;
    }

    /** sets the group as dead - called from DefaultSysProcess */
    void kill() {
        synchronized (TIMER) {
            if (! dead) {
                dead = true;
                TIMER.notifyAll();
                try {
                    TIMER.wait(3000);
                } catch (InterruptedException e) {
                }
            }
        }
    }
    
    public void setRunClassThread(RunClassThread t) {
        runClassThread = t;
    }
    
    public RunClassThread getRunClassThread() {
        return runClassThread;
    }
}
