/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss;

import org.openide.util.Cancellable;
import org.openide.ErrorManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

import javax.swing.*;
import java.util.*;

/**
 * Support for actions that run multiple commands.
 * Represents context that carry data shared by
 * action commands executors. It can manage execution
 * in multiple ClientRuntimes (threads).
 *
 * <p>Implements shared progress, logging support
 * and cancelling.
 *
 * TODO add consolidated error reporting, nowadays, multiple errors from backgroud thread can popup
 *
 * @author Petr Kuzel
 */
public final class ExecutorGroup implements Cancellable {

    private final String name;
    public boolean executed;
    private boolean cancelled;
    private List listeners = new ArrayList(2);
    private List executors = new ArrayList(2);
    private List cleanups = new ArrayList(2);
    /** ClientRuntime => CommandRunnale*/
    private Map queues = new HashMap();
    /** ClientRuntimes*/
    private Set started = new HashSet();
    private ProgressHandle progressHandle;
    private long dataCounter;
    private boolean hasBarrier;
    private boolean failed;
    private boolean executingCleanup;

    /**
     * Creates new group.
     *
     * @param displayName
     * Defines prefered display name - localized string that should highlight
     * group purpose (i.e. in English use verb in gerund).
     * E.g. <code>UpdateCommand</code> used to refresh statuses should
     * be named "Refreshing Status" rather than "cvs -N update",
     * "Updating" or "Status Refresh".
     */
    public ExecutorGroup(String displayName) {
        name = displayName;
    }

    /**
     * Defines group display name.
     */
    public String getDisplayName() {
        return name;
    }

    /**
     * Starts associated progress if not yet started. Allows to share
     * progress with execution preparation phase (cache ops).
     *
     * @param details progress detail messag eor null
     */
    public void progress(String details) {
        if (progressHandle == null) {
            progressHandle = ProgressHandleFactory.createHandle(name + "...", this);
            progressHandle.start();
        }

        if (details != null) {
            progressHandle.progress(details);
        }
    }


    /**
     * Called by ExecutorSupport on enqueue.
     * Pairs with finished.
     *
     * @param queue processign queue or null for all
     * @param id identifier paired with {@link #finished}
     */
    synchronized void enqueued(ClientRuntime queue, Object id) {
        progress(null);

        Collection keys;
        if (queue == null) {
            keys = queues.keySet();
        } else {
            keys = Collections.singleton(queue);
        }

        Iterator it = keys.iterator();
        while (it.hasNext()) {
            Object key = (Object) it.next();
            Set commands = (Set) queues.get(key);
            if (commands == null) {
                commands = new HashSet();
            }
            commands.add(id);
            queues.put(key, commands);
        }
    }

    /**
     * Called by ExecutorSupport on start.
     * @return true for the first command in given queue
     */
    synchronized boolean started(ClientRuntime queue) {
        return started.add(queue);
    }

    /**
     * Called by ExecutorSupport after processing.
     *
     * @param queue processign queue or null for all
     * @param id identifier paired with {@link #enqueued(ClientRuntime, Object)}
     * @return true for last id in given queue
     */
    synchronized boolean finished(ClientRuntime queue, Object id) {

        Collection keys;
        if (queue == null) {
            keys = queues.keySet();
        } else {
            keys = Collections.singleton(queue);
        }

        boolean finished = executed; // TODO how to tip true for non-executed?
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            Object key = (Object) it.next();

            Set commands = (Set) queues.get(key);
            commands.remove(id);
            if (commands.isEmpty()) {
                queues.remove(key);
                if (executed && queues.isEmpty() && progressHandle != null) {
                    progressHandle.finish();
                    progressHandle = null;
                }
            }
            finished &= commands.isEmpty();
        }
        return finished;
    }

    boolean isCancelled() {
        return cancelled;
    }

    /**
     * User cancel comming from Progress UI.
     * Must not be called by internals.
     */
    public boolean cancel() {
        cancelled = true;
        fail();
        return true;
    }

    /**
     * A command in group failed. Stop all pending commands.
     */
    public void fail() {
        failed = true;
        Iterator it;
        synchronized(listeners) {
            it = new ArrayList(listeners).iterator();
        }
        while (it.hasNext()) {
            try {
                Cancellable cancellable = (Cancellable) it.next();
                cancellable.cancel();
            } catch (RuntimeException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        synchronized(this) {
            if (progressHandle != null) {
                progressHandle.finish();
                progressHandle = null;
            }
        }
    }

    /**
     * Add a cancelaable in chain of cancellable performers.
     */
    public void addCancellable(Cancellable cancellable) {
        synchronized(listeners) {
            listeners.add(cancellable);
        }
    }

    public void removeCancellable(Cancellable cancellable) {
        synchronized(listeners) {
            listeners.remove(cancellable);
        }
    }

    /**
     * Add executor into this group.
     */
    public synchronized void addExecutor(ExecutorSupport executor) {
        assert executed == false;
        executor.joinGroup(this);  // XXX third party code executed under lock
        executors.add(executor);
    }

    /**
     * Add executors into this group.
     * @param executors groupable or <code>null</code>
     */
    public final synchronized void addExecutors(ExecutorSupport[] executors) {
        if (executors == null) {
            return;
        } else {
            for (int i = 0; i < executors.length; i++) {
                ExecutorSupport support = executors[i];
                addExecutor(support);
            }
        }
    }

    /**
     * Group execution blocks on this barier until
     * all previously added Groupable finishes (succesfuly or with fail).
     *
     * <p>Warning: Groups with barries have blocking {@link #execute},
     * there is assert banning to execute such group from UI thread.
     */
    public synchronized void addBarrier(Runnable action) {
        assert executed == false;
        ExecutorGroupBar bar = new ExecutorGroupBar(executors, action);
        bar.joinGroup(this);
        executors.add(bar);
        hasBarrier = true;
    }

    /**
     * Can be added only from barrier action!
     */
    public synchronized void addCleanups(ExecutorSupport[] executors) {
        if (executors == null) {
            return;
        } else {
            for (int i = 0; i < executors.length; i++) {
                ExecutorSupport support = executors[i];
                addCleanup(support);
            }
        }
    }

    /**
     * Can be added only from barrier action!
     */
    public synchronized void addCleanup(ExecutorSupport executor) {
        assert executingCleanup == false;
        executor.joinGroup(this);
        cleanups.add(executor);
    }

    /**
     * Asynchronously executes all added executors. Executors
     * are grouped according to CVSRoot and serialized in
     * particular ClientRuntime (thread) queue.
     *
     * <p>Warning:
     * <ul>
     * <li>It becomes blocking if group contains barriers (there is UI thread assert).
     * <li>Do not call {@link ExecutorSupport#execute} if you
     * use grouping.
     * </ul>
     */
    public void execute() {
        assert (SwingUtilities.isEventDispatchThread() && hasBarrier) == false;

        synchronized(this) {
            executed = true;
        }
        Iterator it = executors.iterator();
        while (it.hasNext()) {
            Groupable support = (Groupable) it.next();
            support.execute();
            if (failed) break;
        }

        // cleanup actions

        synchronized(this) {
            executingCleanup = true;
        }
        it = cleanups.iterator();
        while (it.hasNext()) {
            Groupable support = (Groupable) it.next();
            support.execute();
        }

    }

    /**
     * Allows client to communicate it's assumtion that
     * group was executed. If it's not something went wrong,
     * it's time for cleanup.  
     */
    public synchronized void executed() {
        if (executed == false) {
            if (progressHandle != null) {
                progressHandle.finish();
                progressHandle = null;
            }
        }
    }

    void increaseDataCounter(long bytes) {
        dataCounter += bytes;
        if (progressHandle != null) {  // dangling event from zombie worker thread
            progressHandle.progress("" + name + " " + format(dataCounter));
        }
    }

    private static String format(long counter) {
        if (counter < 1024*16) {
            return "" + counter + " bytes";
        }
        counter /= 1024;
        return "" + counter + " kbytes";

        // do not go to megabytes as user want to see CHANGING number
        // it can be solved by average speed in last 5sec, as it drops to zero
        // something is wrong
    }



    public static interface Groupable {

        /**
         * Notifies the Groupable that it is a part of
         * given execution chain.
         *
         * <p> Must be called before {@link #execute}
         */
        void joinGroup(ExecutorGroup group);

        /** Execute custom code. */
        void execute();
    }
}
