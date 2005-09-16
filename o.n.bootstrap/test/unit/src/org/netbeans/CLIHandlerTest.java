/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans;

import java.io.*;
import junit.textui.TestRunner;
import org.netbeans.junit.*;
import java.util.*;
import junit.framework.AssertionFailedError;
import org.openide.util.RequestProcessor;

/**
 * Test the command-line-interface handler.
 * @author Jaroslav Tulach
 */
public class CLIHandlerTest extends NbTestCase {
    
    private static ByteArrayInputStream nullInput = new ByteArrayInputStream(new byte[0]);
    private static ByteArrayOutputStream nullOutput = new ByteArrayOutputStream();
    
    private StringBuffer sb;
    
    public CLIHandlerTest(String name) {
        super(name);
    }
    
    public static junit.framework.Test suite() {
        //return new CLIHandlerTest("testServerCanBeStopped");
        return new NbTestSuite(CLIHandlerTest.class);
    }
    
    protected void setUp() throws Exception {
        super.setUp();

        // all handlers shall be executed immediatelly
        CLIHandler.finishInitialization (false);
        
        // setups a temporary file
        String p = getWorkDirPath ();
        if (p == null) {
            p = System.getProperty("java.io.tmpdir");
        }
        String tmp = p;
        assertNotNull(tmp);
        System.getProperties().put("netbeans.user", tmp);
        
        File f = new File(tmp, "lock");
        if (f.exists()) {
            assertTrue("Clean up previous mess", f.delete());
            assertTrue(!f.exists());
        }
    }
    
    protected void runTest () throws Throwable {
        sb = new StringBuffer ();
        CLIHandler.registerDebug (sb);
        try {
            super.runTest ();
        } catch (AssertionFailedError ex) {
            AssertionFailedError ne = new AssertionFailedError ("Failed as " + ex.getMessage () + " with text:\n" + sb.toString ());
            ne.setStackTrace (ex.getStackTrace ());
            throw ne;
        } finally {
            // clean the debug
            CLIHandler.registerDebug (null);
        }
    }    
    
    
    public void testFileExistsButItCannotBeRead() throws Exception {
        // just creates the file and blocks
        InitializeRunner runner = new InitializeRunner(10);
        
        // blocks when operation fails
        InitializeRunner second = new InitializeRunner(85);
        
        for (int i = 0; i < 3; i++) {
            second.next();
        }
        
        // finishes the code
        runner.next();
        
        assertNotNull("Runner succeeded to allocate the file", runner.resultFile());
        
        // let the second code go on as well
        second.next();
        
        assertEquals("The same file has been allocated", runner.resultFile(), second.resultFile());
    }
    
    public void testFileExistsButTheServerCannotBeContacted() throws Exception {
        // start the server and block
        InitializeRunner runner = new InitializeRunner(65);
        
        assertNotNull("File created", runner.resultFile());
        assertTrue("Port allocated", runner.resultPort() != 0);
        
        // blocks when operation fails
        InitializeRunner second = new InitializeRunner(85);
        // second try should be ok
        second.next();
        
        assertNotNull("Previous file deleted and new one created", second.resultFile());
        assertTrue("Another port allocated", second.resultPort() != runner.resultPort());
        
        
    }
    
    public void testFileExistsButTheServerCannotBeContactedAndWeDoNotWantToCleanTheFileOnSameHost() throws Exception {
        // start the server and block
        InitializeRunner runner = new InitializeRunner(65);
        
        assertNotNull("File created", runner.resultFile());
        assertTrue("Port allocated", runner.resultPort() != 0);
        
        CLIHandler.Status res = CLIHandler.initialize(
            new CLIHandler.Args(new String[0], nullInput, nullOutput, ""), 
            null, Collections.EMPTY_LIST, false, false, null
        );
        
        assertNotNull("Previous file deleted and new one created", res.getLockFile());
        assertTrue("Another port allocated", res.getServerPort() != runner.resultPort());
    }
    
    public void testFileExistsButTheServerCannotBeContactedAndWeDoNotWantToCleanTheFileOnOtherHost() throws Exception {
        // start the server and block
        InitializeRunner runner = new InitializeRunner(65);
        
        assertNotNull("File created", runner.resultFile());
        assertTrue("Port allocated", runner.resultPort() != 0);
        
        File f = runner.resultFile();
        byte[] arr = new byte[(int)f.length()];
        assertEquals("We know that the size of the file should be int + key_length + 4 for ip address: ", 18, arr.length);
        FileInputStream is = new FileInputStream(f);
        assertEquals("Fully read", arr.length, is.read(arr));
        is.close();
        
        for (int i = 0; i < 4; i++) {
            arr[14 + i]++;
        }
        
        // change the IP at the end of the file
        FileOutputStream os = new FileOutputStream(f);
        os.write(arr);
        os.close();
        
        CLIHandler.Status res = CLIHandler.initialize(
            new CLIHandler.Args(new String[0], nullInput, nullOutput, ""), 
            null, Collections.EMPTY_LIST, false, false, null
        );
        
        assertEquals ("Cannot connect because the IP is different", CLIHandler.Status.CANNOT_CONNECT, res.getExitCode());
    }
    
    public void testFileExistsButTheKeyIsNotRecognized() throws Exception {
        // start the server be notified when it accepts connection
        InitializeRunner runner = new InitializeRunner(65);
        
        assertNotNull("File created", runner.resultFile());
        assertTrue("Port allocated", runner.resultPort() != 0);
        
        int s = (int)runner.resultFile().length();
        byte[] copy = new byte[s];
        FileInputStream is = new FileInputStream(runner.resultFile());
        assertEquals("Read fully", s, is.read(copy));
        is.close();
        
        // change one byte in the key
        copy[4 + 2]++;
        
        FileOutputStream os = new FileOutputStream(runner.resultFile());
        os.write(copy);
        os.close();
        
        // try to connect to previous server be notified as soon as it
        // sends request
        InitializeRunner second = new InitializeRunner(30);
        
        // handle the request, say NO
        runner.next();
        
        // read the reply and allocate new port
        second.next();
        
        assertNotNull("Previous file deleted and new one created", second.resultFile());
        assertTrue("Another port allocated", second.resultPort() != runner.resultPort());
    }
    
    public void testCLIHandlersCanChangeLocationOfLockFile() throws Exception {
        File f = File.createTempFile("suffix", "tmp").getParentFile();
        final File dir = new File(f, "subdir");
        dir.delete();
        assertTrue(dir.exists() || dir.mkdir());
        
        class UserDir extends CLIHandler {
            private int cnt;
            
            public UserDir() {
                super(WHEN_BOOT);
            }
            
            protected int cli(Args args) {
                cnt++;
                System.setProperty("netbeans.user", dir.toString());
                return 0;
            }
            
            protected void usage(PrintWriter w) {}
        }
        UserDir ud = new UserDir();
        
        CLIHandler.Status res = cliInitialize(new String[0], ud, nullInput, nullOutput, null);
        
        assertEquals("Our command line handler is called once", 1, ud.cnt);
        assertEquals("Lock file is created in dir", dir, res.getLockFile().getParentFile());
        
        dir.delete();
    }
    
    public void testCLIHandlerCanStopEvaluation() throws Exception {
        class H extends CLIHandler {
            private int cnt;
            
            public H() {
                super(WHEN_INIT);
            }
            
            protected int cli(Args args) {
                cnt++;
                return 1;
            }
            
            protected void usage(PrintWriter w) {}
        }
        H h1 = new H();
        H h2 = new H();
        
        
        CLIHandler.Status res = cliInitialize(new String[0], new H[] {
            h1, h2
        }, nullInput, nullOutput);
        
        assertEquals("CLI evaluation failed with return code of h1", 1, res.getExitCode());
        assertEquals("First one executed", 1, h1.cnt);
        assertEquals("Not the second one", 0, h2.cnt);
    }
    
    public void testWhenInvokedTwiceParamsGoToTheFirstHandler() throws Exception {
        final String[] template = { "Ahoj", "Hello" };
        final String currentDir = "MyDir";
        
        class H extends CLIHandler {
            private int cnt;
            
            public H() {
                super(WHEN_INIT);
            }
            
            protected int cli(Args args) {
                String[] a = args.getArguments();
                String[] t = template;
                
                assertEquals("Same length", t.length, a.length);
                assertEquals("First is same", t[0], a[0]);
                assertEquals("Second is same", t[1], a[1]);
                assertEquals("Current dir is fine", currentDir, args.getCurrentDirectory().toString());
                return ++cnt;
            }
            
            protected void usage(PrintWriter w) {}
        }
        H h1 = new H();
        
        
        CLIHandler.Status res = cliInitialize(template, h1, nullInput, nullOutput, null, currentDir);
        
        assertEquals("First one executed", 1, h1.cnt);
        assertEquals("CLI evaluation failed with return code of h1", 1, res.getExitCode());
        
        res = cliInitialize(template, java.util.Collections.EMPTY_LIST, nullInput, nullOutput, null, currentDir);
        assertEquals("But again executed h1", 2, h1.cnt);
        assertEquals("Now the result is 2 as cnt++ was increased", 2, res.getExitCode());
        
    }

    public void testServerWaitsBeforeFinishInitializationIsCalledOn () throws Exception {
        // this tests will not execute handlers immediatelly
        CLIHandler.finishInitialization (true);
        
        class H extends CLIHandler implements Runnable {
            public volatile int cnt;
            public volatile int afterFinish = -1;
            
            public H () {
                super (CLIHandler.WHEN_INIT);
            }
            
            protected int cli (Args args) {
                cnt++;
                sb.append ("Increased cnt to: ");
                sb.append (cnt);
                sb.append (" by thread ");
                sb.append (Thread.currentThread ());
                return afterFinish;
            }
            
            public void run () {
                // cnt will be two as once the first cliInitialize will
                // invoke the handler and once the second cliInitialize 
                // using the Server            
                afterFinish = 2;
                CLIHandler.finishInitialization (false);
            }
            
            protected void usage (PrintWriter w) {}
        }
        H h = new H ();
        
        CLIHandler.Status res = cliInitialize (new String[0], h, nullInput, nullOutput, null);
        assertEquals ("Returns 0 as no finishInitialization is called", 0, res.getExitCode ());
        // after two seconds it calls finishInitialization
        RequestProcessor.Task task = RequestProcessor.getDefault ().post (h, 7000); // 7s is higher than socket timeout
        res = cliInitialize (new String[0], h, nullInput, nullOutput, null);
        
        assertEquals ("Returns 2 as afterFinish needed to be set to 2 before" +
        " calling finishInitialization", 2, res.getExitCode ());

        long time = System.currentTimeMillis ();
        task.waitFinished ();
        time = System.currentTimeMillis () - time;
        
        if (time > 1000) {
            fail ("The waitFinished should return almost immediatelly. But was: " + time);
        }
        
        if (h.afterFinish != h.cnt) {
            // in order to find out whether the failures in issue #44833
            // are not caused just by threading issues, let's wait another
            // few seconds and print the results of h.afterFinish and h.cnt
            // if they will be the same then just replace the initial condition
            // by say that h.afterFinish == 2 and h.cnt > 1 is ok
            Thread.sleep (5000);
            fail ("H is not executed before finishInitialization is called :" + h.afterFinish + " cnt: " + h.cnt);
        }
        
    }
    
    public void testServerIsNotBlockedByLongRequests() throws Exception {
        class H extends CLIHandler {
            private int cnt = -1;
            public int toReturn;
            
            public H() {
                super(CLIHandler.WHEN_INIT);
            }
            
            protected synchronized int cli(Args args) {
                try {
                    // this simulates really slow, but computing task
                    Thread.sleep (6555);
                } catch (InterruptedException ex) {
                    throw new IllegalStateException ();
                }
                notifyAll();
                cnt++;
                return toReturn;
            }
            
            protected void usage(PrintWriter w) {}
        }
        H h = new H();
        
        h.toReturn = 7;
        final Integer blockOn = new Integer(99);
        CLIHandler.Status res = cliInitialize(new String[0], h, nullInput, nullOutput, blockOn);
        assertEquals("Called once, increased -1 to 0", 0, h.cnt);
        assertEquals("Result is provided by H", 7, res.getExitCode());
        
        // blocks after connection established, before returning the result
        class R implements Runnable {
            CLIHandler.Status res;
            public void run() {
                res = cliInitialize(new String[0], Collections.EMPTY_LIST, nullInput, nullOutput, blockOn);
            }
        }
        R r = new R();
        RequestProcessor.Task task;
        synchronized (h) {
            h.toReturn = 5;
            task = new org.openide.util.RequestProcessor("Blocking request").post(r);
            h.wait();
            assertEquals("Connects to the h", 1, h.cnt);
            if (r.res != null) {
                fail ("The handler should not be finished, as it blocks in '99' but it is and the result is " + r.res.getExitCode ());
            }
        }
        
        // while R is blocked, run another task
        h.toReturn = 0;
        res = cliInitialize(new String[0], Collections.EMPTY_LIST, nullInput, nullOutput, null);
        assertEquals("Called once, increased to 2", 2, h.cnt);
        assertEquals("Result is provided by H, H gives 0, changes into -1 right now", -1, res.getExitCode());
        
        synchronized (blockOn) {
            // let the R task go on
            blockOn.notifyAll();
        }
        task.waitFinished();
        assertNotNull("Now it is finished", r.res);
        assertEquals("Result is -1, if this fails: this usually means that the server is blocked by some work and the task R started new server to handle its request",
            5, r.res.getExitCode());
        assertEquals("H called three times (but counting from -1)", 2, h.cnt);
    }
    
    public void testReadingOfInputWorksInHandler() throws Exception {
        final byte[] template = { 1, 2, 3, 4 };
        
        class H extends CLIHandler {
            private byte[] arr;
            
            public H() {
                super(WHEN_INIT);
            }
            
            protected int cli(Args args) {
                try {
                    InputStream is = args.getInputStream();
                    arr = new byte[is.available() / 2];
                    if (arr.length > 0) {
                        assertEquals("Read amount is the same", arr.length, is.read(arr));
                    }
                    is.close();
                } catch (IOException ex) {
                    fail("There is an exception: " + ex);
                }
                return 0;
            }
            
            protected void usage(PrintWriter w) {}
        }
        H h1 = new H();
        H h2 = new H();
        
        // why twice? first attempt is direct, second thru the socket server
        for (int i = 0; i < 2; i++) {
            CLIHandler.Status res = cliInitialize(
                new String[0], new H[] { h1, h2 }, new ByteArrayInputStream(template), nullOutput);
            
            assertNotNull("Attempt " + i + ": " + "Can be read", h1.arr);
            assertEquals("Attempt " + i + ": " + "Read two bytes", 2, h1.arr.length);
            assertEquals("Attempt " + i + ": " + "First is same", template[0], h1.arr[0]);
            assertEquals("Attempt " + i + ": " + "Second is same", template[1], h1.arr[1]);
            
            assertNotNull("Attempt " + i + ": " + "Can read as well", h2.arr);
            assertEquals("Attempt " + i + ": " + "Just one char", 1, h2.arr.length);
            assertEquals("Attempt " + i + ": " + "And is the right one", template[2], h2.arr[0]);
            
            h1.arr = null;
            h2.arr = null;
        }
    }
    
    public void testReadingMoreThanAvailableIsOk () throws Exception {
        final byte[] template = { 1, 2, 3, 4 };
        
        class H extends CLIHandler {
            private byte[] arr;
            
            public H() {
                super(WHEN_INIT);
            }
            
            protected int cli(Args args) {
                try {
                    InputStream is = args.getInputStream();
                    arr = new byte[8];
                    assertEquals("Read amount is the maximum of template", template.length, is.read(arr));
                    is.close();
                } catch (IOException ex) {
                    fail("There is an exception: " + ex);
                }
                return 0;
            }
            
            protected void usage(PrintWriter w) {}
        }
        H h1 = new H();
        
        // why twice? first attempt is direct, second thru the socket server
        for (int i = 0; i < 2; i++) {
            CLIHandler.Status res = cliInitialize(
                new String[0], new H[] { h1 }, new ByteArrayInputStream(template), nullOutput);
            
            assertNotNull("Attempt " + i + ": " + "Can be read", h1.arr);
            assertEquals("Attempt " + i + ": " + "First is same", template[0], h1.arr[0]);
            assertEquals("Attempt " + i + ": " + "Second is same", template[1], h1.arr[1]);
            assertEquals("Attempt " + i + ": " + "3rd is same", template[2], h1.arr[2]);
            assertEquals("Attempt " + i + ": " + "4th is same", template[3], h1.arr[3]);
            
            h1.arr = null;
        }
    }
    
    
    public void testWritingToOutputIsFine() throws Exception {
        final byte[] template = { 1, 2, 3, 4 };
        
        class H extends CLIHandler {
            public H() {
                super(WHEN_INIT);
            }
            
            protected int cli(Args args) {
                try {
                    OutputStream os = args.getOutputStream();
                    os.write(template);
                    os.close();
                } catch (IOException ex) {
                    fail("There is an exception: " + ex);
                }
                return 0;
            }
            
            protected void usage(PrintWriter w) {}
        }
        H h1 = new H();
        H h2 = new H();
        
        // why twice? first attempt is direct, second thru the socket server
        for (int i = 0; i < 2; i++) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            
            CLIHandler.Status res = cliInitialize(
                new String[0], new H[] { h1, h2 }, nullInput, os);
            
            byte[] arr = os.toByteArray();
            assertEquals("Double size of template", template.length * 2, arr.length);
            
            for (int pos = 0; pos < arr.length; pos++) {
                assertEquals(pos + ". is the same", template[pos % template.length], arr[pos]);
            }
        }
    }

    
    public void testServerCanBeStopped () throws Exception {
        class H extends CLIHandler {
            private int cnt = -1;
            public int toReturn;
            
            public H() {
                super(CLIHandler.WHEN_INIT);
            }
            
            protected synchronized int cli(Args args) {
                notifyAll();
                cnt++;
                return toReturn;
            }
            
            protected void usage(PrintWriter w) {}
        }
        H h = new H();
        
        h.toReturn = 7;
        CLIHandler.Status res = cliInitialize(new String[0], h, nullInput, nullOutput, null);
        assertEquals("Called once, increased -1 to 0", 0, h.cnt);
        assertEquals("Result is provided by H", 7, res.getExitCode());
        
        CLIHandler.stopServer ();
        
        h.toReturn = 5;
        h.cnt = -1;
        res = cliInitialize(new String[0], Collections.EMPTY_LIST, nullInput, nullOutput, null);
        assertEquals("Not called -1", -1, h.cnt);
        // right now the handler will not be called, if there is anything else
        // to do, let's wait for such requirements
    }
    
    //
    // Utility methods
    //
    
    private static CLIHandler.Status cliInitialize(String[] args, CLIHandler handler, InputStream is, OutputStream os, Integer lock) {
        return cliInitialize(args, handler, is, os, lock, System.getProperty ("user.dir"));
    }
    private static CLIHandler.Status cliInitialize(String[] args, CLIHandler handler, InputStream is, OutputStream os, Integer lock, String currentDir) {
        return cliInitialize(args, Collections.nCopies(1, handler), is, os, lock, currentDir);
    }
    private static CLIHandler.Status cliInitialize(String[] args, CLIHandler[] arr, InputStream is, OutputStream os) {
        return cliInitialize(args, Arrays.asList(arr), is, os, null);
    }
    private static CLIHandler.Status cliInitialize(String[] args, List coll, InputStream is, OutputStream os, Integer lock) {
        return cliInitialize (args, coll, is, os, lock, System.getProperty ("user.dir"));
    }
    private static CLIHandler.Status cliInitialize(String[] args, List coll, InputStream is, OutputStream os, Integer lock, String currentDir) {
        return CLIHandler.initialize(new CLIHandler.Args(args, is, os, currentDir), lock, coll, false, true, null);
    }
    
    private static final class InitializeRunner extends Object implements Runnable {
        private Integer block;
        private String[] args;
        private CLIHandler handler;
        private CLIHandler.Status result;
        
        public InitializeRunner(int till) throws InterruptedException {
            this(new String[0], null, till);
        }
        
        public InitializeRunner(String[] args, CLIHandler h, int till) throws InterruptedException {
            this(args, h, new Integer(till));
        }
        public InitializeRunner(String[] args, CLIHandler h, Integer till) throws InterruptedException {
            this.args = args;
            this.block = till;
            this.handler = h;
            
            synchronized (block) {
                new RequestProcessor("InitializeRunner blocks on " + till).post(this);
                block.wait();
            }
        }
        
        public void run() {
            synchronized (block) {
                result = CLIHandler.initialize(
                    new CLIHandler.Args(args, nullInput, nullOutput, ""),
                    block,
                    handler == null ? java.util.Collections.EMPTY_LIST : java.util.Collections.nCopies(1, handler),
                    false,
                    true,
                    null
                );
                // we are finished, wake up guys in next() if any
                block.notifyAll();
            }
        }
        
        /** Executes the code to next invocation */
        public void next() throws InterruptedException {
            synchronized (block) {
                block.notify();
                block.wait();
            }
        }
        
        /** Has already the resutl?
         */
        public boolean hasResult() {
            return result != null;
        }
        
        /** Gets the resultFile, if there is some,
         */
        public File resultFile() {
            if (result == null) {
                fail("No result produced");
            }
            return result.getLockFile();
        }
        
        /** Gets the port, if there is some,
         */
        public int resultPort() {
            if (result == null) {
                fail("No result produced");
            }
            return result.getServerPort();
        }
    } // end of InitializeRunner
    
}
