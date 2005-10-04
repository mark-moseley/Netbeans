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

package org.openide.util;

import java.lang.ref.*;
import java.util.*;
import org.openide.ErrorManager;
import junit.framework.*;
import org.netbeans.junit.*;
import org.openide.util.Task;

public class RequestProcessorTest extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup", "org.openide.util.RequestProcessorTest$Lkp");
    }
    
    private ErrorManager log;
    
    public RequestProcessorTest(java.lang.String testName) {
        super(testName);
    }
    
    protected void setUp () throws Exception {
        super.setUp();
        
        log = ErrorManager.getDefault().getInstance(getName());
    }

    protected void runTest() throws Throwable {
        assertNotNull ("ErrManager has to be in lookup", org.openide.util.Lookup.getDefault ().lookup (ErrManager.class));
        ErrManager.messages.setLength(0);
        
        try {
            super.runTest();
        } catch (Throwable ex) {
            throw new junit.framework.AssertionFailedError (
                ex.getMessage() + "\n" + ErrManager.messages.toString()
            ).initCause(ex);
        }
    }
    
    
    /** A test to check that objects are executed in the right order.
     */
    public void testOrder () throws Exception {
        final int[] count = new int[1];
        final String[] fail = new String[1];
        
        class X extends Object 
        implements Runnable, Comparable {
            public int order;
            
            public void run () {
                if (order != count[0]++) {
                    if (fail[0] == null) {
                        fail[0] = "Executing task " + order + " instead of " + count[0];
                    }
                }
            }
            
            public int compareTo (Object o) {
                X x = (X)o;
                
                return System.identityHashCode (x) - System.identityHashCode (this);
            }
            
            public String toString () {
                return "O: " + order;
            }
        };
        
        // prepare the tasks 
        X[] arr = new X[10];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = new X ();
        }
        
        // sort it
//        Arrays.sort (arr);
        
        for (int i = 0; i < arr.length; i++) {
            arr[i].order = i;
        }
        
        // execute the task as quickly as possible (only those with the same time
        // can have wrong order
        RequestProcessor.Task[] wait = new RequestProcessor.Task[arr.length];
        for (int i = 0; i < arr.length; i++) {
            wait[i] = RequestProcessor.postRequest (arr[i]);
        }
        
        // wait to all tasks to finish
        for (int i = 0; i < arr.length; i++) {
            wait[i].waitFinished ();
        }
        
        if (fail[0] != null) {
            fail (fail[0]);
        }
            
    }
    
    public void testScheduleAndIsFinished() throws InterruptedException {
        class Run implements Runnable {
            public boolean run;
            public boolean second;
            
            public synchronized void run() {
                if (run) {
                    second = true;
                    return;
                }
                
                try {
                    notifyAll();
                    wait();
                } catch (InterruptedException ex) {
                    fail(ex.getMessage());
                }
                run = true;
            }
        }
        
        
        Run r = new Run();
        RequestProcessor.Task task;
        synchronized (r) {
            task = new RequestProcessor(getName()).post(r);
            r.wait();
            task.schedule(200);
            r.notifyAll();
        }

        Thread.sleep(100);
        assertTrue("Run successfully", r.run);
        assertFalse("Not for the second time1", r.second);
        assertFalse("Not finished as it is scheduled", task.isFinished());
        assertFalse("Not for the second time2", r.second);
        
        task.waitFinished();
        assertTrue("Finished now", task.isFinished());
        assertTrue("Run again", r.second);
        
    }

    /**
     * A test that check that priorities are handled well.
     */
    public void testPriorityQueue() throws Exception {
        
        final Runnable[] arr = new Runnable[5];
        
        class R implements Runnable {
            
            public int index;
            
            public R (int i) {
                index = i;
            }
            
            public synchronized void run () {
                for (int i = 0; /*i < arr.length*/; i++) {
                    if (arr[i] == null) {
                        arr[i] = this;
                        break;
                    }
                }
                
            }
            
            public String toString () {
                return " R index " + index;
            }
        }       

        Runnable r[] = new Runnable[5];
        // expected order of execution
        for (int i = 0; i<5; i++) {
            r[i] = new R(i);
        }
        
        RequestProcessor rp = new RequestProcessor("PrioriyTest");
        
        RequestProcessor.Task t[] = new RequestProcessor.Task[5];
        synchronized (r[0]) {
            t[4] = rp.post(r[0], 0, 3);
            t[0] = rp.post(r[4], 0, 1);
            t[1] = rp.post(r[2], 0, 2);
            t[2] = rp.post(r[1], 0, 2);
            t[3] = rp.post(r[3], 0, 2);            
            t[2].setPriority(3);
        }
        
        for (int i = 0; i<5; i++) {
            t[i].waitFinished();
        }
        
        for (int i = 0; i<5; i++) {
            R next = (R) arr[i];
            if (next.index != i) fail("Expected at " + i + " but was " +  next.index);
        }
    }
    
    /** Test bug http://www.netbeans.org/issues/show_bug.cgi?id=31906
     */
    public void testBug31906_SimulateDataFolderTest () {
        RequestProcessor rp = new RequestProcessor ("dataFolderTest");
        
        class X implements Runnable {
            private RequestProcessor.Task wait;
            private int cnt;
            
            public synchronized void run () {
                if (wait != null) {
                    wait.waitFinished ();
                    cnt++;
                } else {
                    cnt++;
                }
            }
            
            public synchronized void assertCnt (String msg, int cnt) {
                assertEquals (msg, cnt, this.cnt);
                this.cnt = 0;
            }
            
            public synchronized void waitFor (RequestProcessor.Task t) {
                wait = t;
            }
            
        }
        X[] arr = { new X(), new X() };
        RequestProcessor.Task[] tasks = { 
            rp.create (arr[0]), 
            rp.create (arr[1])
        };
        tasks[0].setPriority(Thread.NORM_PRIORITY - 1);
        tasks[1].setPriority(Thread.NORM_PRIORITY + 1);
        
        tasks[0].schedule(0);
        tasks[1].schedule(0);

        tasks[0].waitFinished();
        arr[0].assertCnt (" Once", 1);
        tasks[1].waitFinished ();
        arr[1].assertCnt (" Once as well", 1);

        tasks[0].schedule(100);
        tasks[1].schedule(100);
        tasks[0].schedule(10);
        tasks[1].schedule(10);

        tasks[0].waitFinished();
        tasks[1].waitFinished();

        arr[0].assertCnt (" 1a", 1);
        arr[1].assertCnt (" 1b", 1);

        arr[0].waitFor (tasks[1]);
        tasks[1].schedule(100);
        tasks[0].schedule(10);
        tasks[0].waitFinished ();
        arr[0].assertCnt (" task 0 is executed", 1);
        arr[1].assertCnt (" but it also executes task 1", 1);

        tasks[0].schedule(10);
        tasks[0].waitFinished ();
        arr[0].assertCnt (" task O is executed", 1);
        arr[1].assertCnt (" but it does not execute 1", 0);
    }
    
    
    /** Test priority inversion and whether it is properly notified
     */
    public void testPriorityInversionProblemAndItsDiagnosis () throws Exception {
        RequestProcessor rp = new RequestProcessor ("testPriorityInversionProblemAndItsDiagnosis");
        
        final Runnable[] arr = new Runnable[3];
        
        class R implements Runnable {
            
            public int index;
            public Task t;
            
            public R (int i) {
                index = i;
            }
            
            public synchronized void run () {
                for (int i = 0; /*i < arr.length*/; i++) {
                    if (arr[i] == null) {
                        arr[i] = this;
                        break;
                    }
                }
                
                if (t != null) {
                    t.waitFinished ();
                }
            }
            
            public String toString () {
                return " R index " + index;
            }
        }
         
        R r1 = new R (1);
        R r2 = new R (2);
        R r3 = new R (3);
        
        Task t1;
        Task t2;
        Task t3;
        
        synchronized (r1) {
            t1 = rp.post (r1);
            t2 = rp.post (r2);
            
            // r1 will call the waitFinished of r3
            r1.t = t3 = rp.post (r3);
        }
        
        t1.waitFinished ();
        t2.waitFinished ();
        t3.waitFinished ();
        
        assertEquals ("First started is t1", r1, arr[0]);
        assertEquals ("Second started is t3", r3, arr[1]);
        assertEquals ("Last started is t2", r2, arr[2]);
        
        // now we should ensure that the RP warned everyone about the 
        // priority inheritance and all its possible complications (t2 running
        // later than t3)
    }
    
    /** Test of finalize method, of class org.openide.util.RequestProcessor. */
    public void testFinalize() throws Exception {
        RequestProcessor rp = new RequestProcessor ("toGarbageCollect");
        Reference ref = new WeakReference (rp);
        Reference task;
        
        final Object lock = new Object ();
        
        
        synchronized (lock) {
            task = new WeakReference (rp.post (new Runnable () {
                public void run () {
                    synchronized (lock) {
                        lock.notify ();
                    }
                }
            }));
            
            
            rp = null;

            doGc (10, null);
            
            if (ref.get () == null) {
                fail ("garbage collected even a task is planed."); // NOI18N
            }
            
            // run the task
            lock.wait ();
            
        }
        
        doGc (1000, task);
        
        if (task.get () != null) {
            fail ("task is not garbage collected.");
        }
        
        doGc (1000, ref);
        if (ref.get () != null) {
            fail ("not garbage collected at all."); // NOI18N
        }
        
    }
    
    /** Check whether task is finished when it should be.
     */
    public void testCheckFinished () {
        RequestProcessor rp = new RequestProcessor ("Finish");
        
        class R extends Object implements Runnable {
            RequestProcessor.Task t;
            
            public void run () {
                if (t.isFinished ()) {
                    fail ("Finished when running");
                }
            }
        }
        
        R r = new R ();
        RequestProcessor.Task task = rp.create (r);
        r.t = task;

        if (task.isFinished ()) {
            fail ("Finished after creation");
        }
     
        task.schedule (200);
        
        if (task.isFinished ()) {
            fail ("Finished when planed");
        }
        
        task.waitFinished ();
        
        if (!task.isFinished ()) {
            fail ("Not finished after waitFinished");
        }
        
        task.schedule (200);
        
        if (task.isFinished ()) {
            fail ("Finished when planed");
        }
    }
        

    /** Test to check the waiting in request processor.
    */
    public void testWaitFinishedOnNotStartedTask () throws Exception {
        Counter x = new Counter ();
        final RequestProcessor.Task task = RequestProcessor.getDefault().create (x);
        
        //
        // Following code tests whether the RP.create().waitFinished really blocks
        // until somebody schedules the task.
        //
        class WaitThread extends Thread {
            public boolean finished;
            
            public void run () {
                task.waitFinished ();
                synchronized (this) {
                    finished = true;
                    notifyAll ();
                }
            }
            
            public synchronized void w (int timeOut) throws Exception {
                if (!finished) {
                    wait (timeOut);
                }
            }
        }
        WaitThread wt = new WaitThread ();
        wt.start ();
        wt.w (100);
        assertTrue ("The waitFinished has not ended, because the task has not been planned", !wt.finished);
        task.schedule (0);
        wt.w (0);
        assertTrue ("The waitFinished finished, as the task is now planned", wt.finished);
        x.assertCnt ("The task has been executed", 1);
    }
    
    /** Test to check the waiting in request processor.
    */
    public void testWaitFinishedOnNotStartedTaskFromRPThread () throws Exception {
        Counter x = new Counter ();
        RequestProcessor rp = new RequestProcessor ("testWaitFinishedOnNotStartedTaskFromRPThread");
        final RequestProcessor.Task task = rp.create (x);
        
        //
        // Following code tests whether the RP.create().waitFinished really blocks
        // until somebody schedules the task.
        //
        class WaitTask implements Runnable {
            public boolean finished;
            
            public synchronized void run () {
                task.waitFinished ();
                finished = true;
                notifyAll ();
            }
            
            public synchronized void w (int timeOut) throws Exception {
                if (!finished) {
                    wait (timeOut);
                }
            }
        }
        WaitTask wt = new WaitTask ();
        rp.post (wt);
        wt.w (0);
        assertTrue ("The task.waitFinished has to finish, otherwise the RequestProcessor thread will stay occupied forever", wt.finished);
        x.assertCnt ("The task has been executed - wait from RP made it start", 1);
    }
        
    public void testWaitFinished2 () {        
        Counter x = new Counter ();
        final RequestProcessor.Task task = RequestProcessor.getDefault().create (x);
        task.schedule (500);
        if (task.cancel ()) {
            // ok, task is canceled
            task.waitFinished ();
        }

        // does a task that is scheduled means that it is not finished?
        task.schedule (200);
        task.waitFinished();
        x.assertCnt ("Wait does not wait for finish of scheduled tasks, that already has been posted", 1);
    }
    
    /** Ensure that it is safe to call schedule() while the task is running
     * (should finish the task and run it again).
     */
    public void testScheduleWhileRunning() throws Exception {
        class X implements Runnable {
            public synchronized void run() {
                try {
                    if (cnt == 0) {
                        this.notify(); // #1
                        this.wait(9999); // #2
                        cnt++;
                    } else {
                        cnt++;
                        this.notify(); // #3
                    }
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
            public int cnt = 0;
        }
        X x = new X();
        synchronized (x) {
            RequestProcessor.Task task = RequestProcessor.postRequest(x);
            x.wait(9999); // #1
            assertEquals(0, x.cnt);
            task.schedule(0);
            x.notify(); // #2
            x.wait(9999); // #3
            assertEquals(2, x.cnt);
        }
    }
    
    /** Make sure it is safe to call waitFinished() on a task from within
     * a task listener.
     */
    public void testWaitFinishedFromNotification() throws Exception {
        class X implements Runnable {
            private Task task;
            private int cnt;
            public synchronized Task start() {
                if (task == null) {
                    task = RequestProcessor.postRequest(this);
                }
                return task;
            }
            public void run() {
                cnt++;
            }
            public int getCount() {
                return cnt;
            }
            public void block() {
                start().waitFinished();
            }
        }
        final X x = new X();
        final Object lock = "wait for task to finish";
        final boolean[] finished = new boolean[1];
        x.start().addTaskListener(new TaskListener() {
            public void taskFinished(Task t) {
                x.block();
                finished[0] = true;
                synchronized (lock) {
                    lock.notify();
                }
            }
        });
        synchronized (lock) {
            lock.wait(5000);
        }
        assertTrue(finished[0]);
        assertEquals(1, x.getCount());
    }

    /** Make sure that successfully canceled task is not performed.
     */
    public void testCancel() throws Exception {
        class X implements Runnable {
            public boolean performed = false;
            public void run() {
                performed = true;
            }
        }
        
        X x = new X();
        final boolean[] finished = new boolean[1];
        finished[0] = false;
        
        // post task with some delay
        RequestProcessor.Task task = RequestProcessor.postRequest(x, 1000);
        task.addTaskListener(new TaskListener() {
            public void taskFinished(Task t) {
                finished[0] = true;
            }
        });

        boolean canceled = task.cancel();
        assertTrue("Task was not canceled", canceled);
        assertTrue("The taskFinished was not called for canceled task", finished[0]);
        Thread.sleep(1500); // wait longer than task delay
        assertTrue("Task was performed even if it is canceled", !x.performed);
    }
    
    public void testWaitWithTimeOutCanFinishEvenTheTaskHasNotRun () throws Exception {
        class Run implements Runnable {
            public boolean runned;
            public synchronized void run () {
                runned = true;
            }
        }
        
        Run run = new Run ();
        
        synchronized (run) {
            RequestProcessor.Task task = RequestProcessor.getDefault ().post (run);
            task.waitFinished (100);
            assertFalse ("We are here and the task has not finished", run.runned);
            assertFalse ("Not finished", task.isFinished ());
        }
    }
    
    public void testWhenWaitingForALimitedTimeFromTheSameProcessorThenInterruptedExceptionIsThrownImmediatelly () throws Exception {
        Counter x = new Counter ();
        RequestProcessor rp = new RequestProcessor ("testWaitFinishedOnNotStartedTaskFromRPThread");
        final RequestProcessor.Task task = rp.create (x);
        
        class WaitTask implements Runnable {
            public boolean finished;
            
            public synchronized void run () {
                long time = System.currentTimeMillis ();
                try {
                    task.waitFinished (1000);
                    fail ("This should throw an exception. Btw time was: " + (System.currentTimeMillis () - time));
                } catch (InterruptedException ex) {
                    // ok, this is expected
                } finally {
                    time = System.currentTimeMillis () - time;
                    notifyAll ();
                }
                if (time > 100) {
                    fail ("Exception should be thrown quickly. Was: " + time);
                }
                finished = true;
            }
            
        }
        WaitTask wt = new WaitTask ();
        synchronized (wt) {
            rp.post (wt);
            wt.wait ();
        }
        assertTrue ("The task.waitFinished has to finish", wt.finished);
        x.assertCnt ("The task has NOT been executed", 0);
    }
    
    public void testWhenWaitingForAlreadyFinishedTaskWithTimeOutTheResultIsGood () throws Exception {
        Counter x = new Counter ();
        RequestProcessor rp = new RequestProcessor ("testWaitFinishedOnStartedTaskFromRPThread");
        final RequestProcessor.Task task = rp.post (x);
        task.waitFinished ();
        x.assertCnt ("The task has been executed before", 1);
        
        class WaitTask implements Runnable {
            public boolean finished;
            
            public synchronized void run () {
                notifyAll ();
                try {
                    assertTrue ("The task has been already finished", task.waitFinished (1000));
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    fail ("Should not happen");
                }
                finished = true;
            }
            
        }
        WaitTask wt = new WaitTask ();
        synchronized (wt) {
            rp.post (wt);
            wt.wait ();
        }
        assertTrue ("The task.waitFinished has to finish", wt.finished);
    }
    
    /**
     * A processing thread must survive throwable thrown during
     * execution of given taks. RuntimeException
     */
    public void testSurvivesException() throws Exception {
        doSurviveTest(false); // NPE
        doSurviveTest(true);  // AssertionError
    }


    private void doSurviveTest(final boolean error) throws Exception {
        RequestProcessor rp = new RequestProcessor("SurvivesTest");
        Counter x = new Counter ();
        
        final Locker lock = new Locker();
        
        rp.post (new Runnable() {
            public void run() {
                lock.waitOn();
                
                if (error) {
                    throw new AssertionError();
                } else {
                    throw new NullPointerException();
                }
            }
        });
        
        rp.post(x);
        lock.notifyOn();
        
        x.assertCntWaiting("Second task not performed after " +
                     (error ? "error" : "exception"), 1);
    }
    
    public void testCancelInterruptsTheRunningThread () throws Exception {
        RequestProcessor rp = new RequestProcessor ("Cancellable", 1, true);
        
        class R implements Runnable {
            private String name;
            
            public boolean checkBefore;
            public boolean checkAfter;
            public boolean interrupted;
            
            public R (String n) {
                this.name = n;
            }
            
            public synchronized void run () {
                checkBefore = Thread.interrupted();
                
                log.log("in runnable " + name + " check before: " + checkBefore);
                
                notifyAll ();

                log.log("in runnable " + name + " after notify");
                
                try {
                    wait ();
                    log.log("in runnable " + name + " after wait, not interrupted");
                    interrupted = false;
                } catch (InterruptedException ex) {
                    interrupted = true;
                    log.log("in runnable " + name + " after wait, interrupted");
                }
                
                notifyAll ();
                
                log.log("in runnable " + name + " after notifyAll");

                try {
                    wait ();
                    log.log("in runnable " + name + " after second wait, not interrupted");
                    checkAfter = Thread.interrupted();
                } catch (InterruptedException ex) {
                    log.log("in runnable " + name + " after second wait, interrupted");
                    checkAfter = true;
                }
                
                log.log("in runnable " + name + " checkAfter: " + checkAfter);
                
                notifyAll ();
            }
        }
        
        R r = new R ("First");
        RequestProcessor.Task t;
        synchronized (r) {
            t = rp.post (r);
            r.wait ();
            assertTrue ("The task is already running", !t.cancel ());
            log.log("Main checkpoint1");
            r.wait ();
            log.log("Main checkpoint2");
            r.notifyAll ();
            log.log("Main checkpoint3");
            r.wait ();
            log.log("Main checkpoint4");
            assertTrue ("The task has been interrupted", r.interrupted);
            assertTrue ("Not before", !r.checkBefore);
            assertTrue ("Not after - as the notification was thru InterruptedException", !r.checkAfter);
        }
        log.log("Main checkpoint5");
        t.waitFinished();
        log.log("Main checkpoint6");
        /*
        try {
            assertGC("no", new java.lang.ref.WeakReference(this));
        } catch (Error e) {
            // ok
        }
         */
        
        // interrupt after the task has finished
        r = new R ("Second");
        synchronized (r) {
            t = rp.post (r);
            log.log("Second checkpoint1");
            r.wait ();
            r.notifyAll ();
            log.log("Second checkpoint2");
            r.wait ();
            log.log("Second checkpoint3");
            assertTrue ("The task is already running", !t.cancel ());
            log.log("Second checkpoint4");
            r.notifyAll ();
            log.log("Second checkpoint5");
            r.wait ();
            assertTrue ("The task has not been interrupted by exception", !r.interrupted);
            assertTrue ("Not interupted before", !r.checkBefore);
            assertTrue ("But interupted after", r.checkAfter);
        }
        log.log("Second checkpoint6");
        t.waitFinished();
        log.log("Second checkpoint7");
    }

    public void testCancelDoesNotInterruptTheRunningThread () throws Exception {
        RequestProcessor rp = new RequestProcessor ("Not Cancellable", 1, false);
        
        class R implements Runnable {
            public boolean checkBefore;
            public boolean checkAfter;
            public boolean interrupted;
            
            public synchronized void run () {
                checkBefore = Thread.interrupted();
                
                notifyAll ();
                
                try {
                    wait ();
                    interrupted = false;
                } catch (InterruptedException ex) {
                    interrupted = true;
                }
                
                notifyAll ();
                
                try {
                    wait ();
                } catch (InterruptedException ex) {
                }
                
                checkAfter = Thread.interrupted();
                
                notifyAll ();
            }
        }
        
        R r = new R ();
        synchronized (r) {
            RequestProcessor.Task t = rp.post (r);
            r.wait ();
            assertTrue ("The task is already running", !t.cancel ());
            r.notifyAll ();
            r.wait ();
            r.notifyAll ();
            r.wait ();
            assertFalse ("The task has not been interrupted", r.interrupted);
            assertTrue ("Not before", !r.checkBefore);
            assertTrue ("Not after - as the notification was thru InterruptedException", !r.checkAfter);
        }
        
        // interrupt after the task has finished
        r = new R ();
        synchronized (r) {
            RequestProcessor.Task t = rp.post (r);
            r.wait ();
            r.notifyAll ();
            r.wait ();
            assertTrue ("The task is already running", !t.cancel ());
            r.notifyAll ();
            r.wait ();
            assertTrue ("The task has not been interrupted by exception", !r.interrupted);
            assertFalse ("Not interupted before", r.checkBefore);
            assertFalse ("Not interupted after", r.checkAfter);
        }
    }
    
    public void testInterruptedStatusIsClearedBetweenTwoTaskExecution () throws Exception {
        RequestProcessor rp = new RequestProcessor ("testInterruptedStatusIsClearedBetweenTwoTaskExecution", 1, true);
        
        final RequestProcessor.Task[] task = new RequestProcessor.Task[1];
        // test interrupted status is cleared after task ends
        class Fail implements Runnable {
            public boolean checkBefore;
            public Thread runIn;
            public boolean goodThread;
            
            public synchronized void run () {
                if (runIn == null) {
                    runIn = Thread.currentThread();
                    task[0].schedule (0);
                    
                    // wait to make sure the task is scheduled
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    goodThread = Thread.currentThread () == runIn;
                }
                    
                checkBefore = runIn.isInterrupted();
                // set the flag for next execution
                runIn.interrupt();
                
                notifyAll ();
            }
        }
        
        Fail f = new Fail ();
        synchronized (f) {
            task[0] = rp.post (f);
            
            // wait for the first execution
            f.wait ();
        }
        // wait for the second
        task[0].waitFinished ();
        
        /* Shall be true, but sometimes the threads get GCed, so we cannot really check that.
        assertTrue ("This shall be always true, but if not, than it does not mean too much"
            + " just that the tasks were not executed in the same thread. In such case it "
            + " this test does not do anything useful as it needs to execute the task twice "
            + " in the same thread", f.goodThread);
        */
        
        if (f.goodThread) {
            assertTrue ("Interrupted state has been cleared between two executions of the task", !f.checkBefore);
        }
    }
    
    public void testInterruptedStatusWorksInInversedTasks() throws Exception {
        RequestProcessor rp = new RequestProcessor ("testInterruptedStatusWorksInInversedTasks", 1, true);
        
        class Fail implements Runnable {
            public Fail (String n) {
                name = n;
            }
            
            private String name;
            public RequestProcessor.Task wait;
            public Object lock;
            
            public boolean checkBefore;
            public boolean checkAfter;
            
            public void run () {
                synchronized (this) {
                    checkBefore = Thread.interrupted();
                    notifyAll();
                }
                if (lock != null) {
                    synchronized (lock) {
                        lock.notify();
                        try {
                            lock.wait();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                            fail ("No InterruptedException");
                        }
                    }
                }
                
                if (wait != null) {
                    wait.schedule(100);
                    wait.waitFinished();
                }
                
                synchronized (this) {
                    checkAfter = Thread.interrupted();
                    notifyAll();
                }
            }
            
            public String toString () {
                return name;
            }
        }
        
        Object initLock = new Object();
        
        Fail smaller = new Fail("smaller");
        smaller.lock = initLock;
        Fail bigger = new Fail("BIGGER");
        RequestProcessor.Task smallerTask, biggerTask;
        
        
        smallerTask = rp.create (smaller);
        biggerTask = rp.create (bigger);
        
        bigger.wait = smallerTask;
        
        synchronized (initLock) {
            biggerTask.schedule(0);
            initLock.wait();
            initLock.notifyAll();
            assertFalse ("Already running", biggerTask.cancel());
        }

        biggerTask.waitFinished();
        
        assertFalse("bigger not interrupted at begining", bigger.checkBefore);
        assertFalse("smaller not interrupted at all", smaller.checkBefore);
        assertFalse("smaller not interrupted at all2", smaller.checkAfter);
        assertTrue("bigger interrupted at end", bigger.checkAfter);
        
    }

    public void testInterruptedStatusWorksInInversedTasksWhenInterruptedSoon() throws Exception {
        RequestProcessor rp = new RequestProcessor ("testInterruptedStatusWorksInInversedTasksWhenInterruptedSoon", 1, true);
        
        class Fail implements Runnable {
            public RequestProcessor.Task wait;
            public Object lock;
            
            public boolean checkBefore;
            public boolean checkAfter;
            
            public void run () {
                synchronized (this) {
                    checkBefore = Thread.interrupted();
                    notifyAll();
                }
                if (lock != null) {
                    synchronized (lock) {
                        lock.notify();
                    }
                }
                
                if (wait != null) {
                    // we cannot call Thread.sleep, so lets slow things own 
                    // in other way
                    for (int i = 0; i < 10; i++) {
                        System.gc ();
                    }
                    
                    wait.waitFinished();
                }
                
                synchronized (this) {
                    checkAfter = Thread.interrupted();
                    notifyAll();
                }
            }
        }
        
        Object initLock = new Object();
        
        Fail smaller = new Fail();
        Fail bigger = new Fail();
        RequestProcessor.Task smallerTask, biggerTask;
        
        
        smallerTask = rp.create (smaller);
        biggerTask = rp.create (bigger);

        
        bigger.lock = initLock;
        bigger.wait = smallerTask;
        
        synchronized (initLock) {
            biggerTask.schedule(0);
            initLock.wait();
            assertFalse ("Already running", biggerTask.cancel());
        }

        biggerTask.waitFinished();
        
        assertFalse("bigger not interrupted at begining", bigger.checkBefore);
        assertFalse("smaller not interrupted at all", smaller.checkBefore);
        assertFalse("smaller not interrupted at all2", smaller.checkAfter);
        assertTrue("bigger interrupted at end", bigger.checkAfter);
        
    }
    
    private static void doGc (int count, Reference toClear) {
        java.util.ArrayList l = new java.util.ArrayList (count);
        while (count-- > 0) {
            if (toClear != null && toClear.get () == null) break;
            
            l.add (new byte[1000]);
            System.gc ();
            System.runFinalization();
	    try {
	        Thread.sleep(10);
	    } catch (InterruptedException e) {}
        }
    }

    private static class Counter extends Object implements Runnable {
        private int count = 0;

        public synchronized void run () {
            count++;
        }
        
        public synchronized void assertCnt (String msg, int cnt) {
            assertEquals (msg, cnt, this.count);
            this.count = 0;
        }

        public synchronized void assertCntWaiting(String msg, int cnt) {
            // have to wait actively to recognize starvation :-(
            for (int i=1; i<10; i++) {
                try { wait(20*i*i); } catch (InterruptedException e) {}
                if (count == cnt) { // passed
                    count = 0;
                    return;
                }
            }
            assertEquals (msg, cnt, count); // let it fail
        }
    }

    private static class Locker {
        boolean ready = false;
        
        public synchronized void waitOn() {
            while (ready == false) {
                try {
                    wait();
                } catch (InterruptedException e) {}
            }
        }
        
        public synchronized void notifyOn() {
            ready = true;
            notifyAll();
        }
    }
    
    //
    // Our fake lookup
    //
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        private ErrManager err = new ErrManager ();
        private org.openide.util.lookup.InstanceContent ic;
        
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            ic.add (err);
            this.ic = ic;
        }
        
        public static void turn (boolean on) {
            Lkp lkp = (Lkp)org.openide.util.Lookup.getDefault ();
            if (on) {
                lkp.ic.add (lkp.err);
            } else {
                lkp.ic.remove (lkp.err);
            }
        }
    }
    
    //
    // Manager to delegate to
    //
    public static final class ErrManager extends org.openide.ErrorManager {
        public static final StringBuffer messages = new StringBuffer ();
        
        private String prefix;
        
        public ErrManager () {
            this (null);
        }
        public ErrManager (String prefix) {
            this.prefix = prefix;
        }
        
        public static ErrManager get () {
            return (ErrManager)org.openide.util.Lookup.getDefault ().lookup (ErrManager.class);
        }
        
        public Throwable annotate (Throwable t, int severity, String message, String localizedMessage, Throwable stackTrace, java.util.Date date) {
            return t;
        }
        
        public Throwable attachAnnotations (Throwable t, org.openide.ErrorManager.Annotation[] arr) {
            return t;
        }
        
        public org.openide.ErrorManager.Annotation[] findAnnotations (Throwable t) {
            return null;
        }
        
        public org.openide.ErrorManager getInstance (String name) {
            if (
                name.startsWith ("org.openide.util.RequestProcessor") ||
                name.startsWith("test")
            ) {
                return new ErrManager ('[' + name + ']');
            } else {
                // either new non-logging or myself if I am non-logging
                return new ErrManager ();
            }
        }
        
        public void log (int severity, String s) {
            lastSeverity = severity;
            lastText = s;
            if (this != get()) {
                messages.append(prefix);
                messages.append(s);
                messages.append('\n');
            }
        }
        
        public void notify (int severity, Throwable t) {
            lastThrowable = t;
            lastSeverity = severity;
        }
        private static int lastSeverity;
        private static Throwable lastThrowable;
        private static String lastText;

        public static void assertNotify (int sev, Throwable t) {
            assertEquals ("Severity is same", sev, lastSeverity);
            assertSame ("Throwable is the same", t, lastThrowable);
            lastThrowable = null;
            lastSeverity = -1;
        }
        
        public static void assertLog (int sev, String t) {
            assertEquals ("Severity is same", sev, lastSeverity);
            assertEquals ("Text is the same", t, lastText);
            lastText = null;
            lastSeverity = -1;
        }
        
    } 
    
}
