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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * Command Line Interface and User Directory Locker support class.
 * Subclasses may be registered into the system to handle special command-line options.
 * To be registered, use <samp>META-INF/services/org.netbeans.CLIHandler</code>
 * in a JAR file in the startup or dynamic class path (e.g. <samp>lib/ext/</samp>
 * or <samp>lib/</samp>).
 * @author Jaroslav Tulach
 * @since org.netbeans.core/1 1.18
 * @see "#32054"
 * @see <a href="http://openide.netbeans.org/proposals/arch/cli.html">Specification</a>
 */
public abstract class CLIHandler extends Object {
    /** lenght of the key used for connecting */
    private static final int KEY_LENGTH = 10;
    /** ok reply */
    private static final int REPLY_OK = 1;
    /** sends exit code */
    private static final int REPLY_EXIT = 2;
    /** fail reply */
    private static final int REPLY_FAIL = 0;
    
    /** request to read from input stream */
    private static final int REPLY_READ = 10;
    /** request to write */
    private static final int REPLY_WRITE = 11;
    /** request to find out how much data is available */
    private static final int REPLY_AVAILABLE = 12;
    
    /**
     * Used during bootstrap sequence. Should only be used by core, not modules.
     */
    public static final int WHEN_BOOT = 1;
    /**
     * Used during later initialization or while NetBeans is up and running.
     */
    public static final int WHEN_INIT = 2;
    
    private int when;
    
    /**
     * Create a CLI handler and indicate its preferred timing.
     * @param when when to run the handler: {@link #WHEN_BOOT} or {@link #WHEN_INIT}
     */
    protected CLIHandler(int when) {
        this.when = when;
    }
    
    /**
     * Process some set of command-line arguments.
     * Unrecognized or null arguments should be ignored.
     * Recognized arguments should be nulled out.
     * @param args arguments
     * @return error value or 0 if everything is all right
     */
    protected abstract int cli(Args args);
    
    private static void showHelp(PrintWriter w, List handlers) {
        w.println("-?");
        w.println("-help");
        w.println("  Show this help information.");
        Iterator it = handlers.iterator();
        while (it.hasNext()) {
            ((CLIHandler)it.next()).usage(w);
        }
    }
    
    /**
     * Print usage information for this handler.
     * @param w a writer to print to
     */
    protected abstract void usage(PrintWriter w);
    
    /** For testing purposes we can block the
     * algorithm in any place in the initialize method.
     */
    private static void enterState(int state, Integer block) {
        if (block == null) return;
        
        // for easier debugging of CLIHandlerTest
        //System.err.println("state: " + state + " block: " + block + " thread: " + Thread.currentThread());
        
        synchronized (block) {
            if (state == block.intValue()) {
                block.notifyAll();
                try {
                    block.wait();
                } catch (InterruptedException ex) {
                    throw new IllegalStateException();
                }
            }
        }
    }
    
    /** Notification of available handlers.
     * @return non-zero if one of the handlers fails
     */
    private static int notifyHandlers(Args args, List handlers, int when, boolean failOnUnknownOptions) {
        try {
            //System.err.println("notifyHandlers: handlers=" + handlers + " when=" + when + " args=" + Arrays.asList(args.getArguments()));
            if (failOnUnknownOptions) {
                String[] argv = args.getArguments();
                for (int i = 0; i < argv.length; i++) {
                    assert argv[i] != null;
                    if (argv[i].equals("-?") || argv[i].equals("-help")) { // NOI18N
                        PrintWriter w = new PrintWriter(args.getOutputStream());
                        showHelp(w, handlers);
                        w.flush();
                        return 2;
                    }
                }
            }
            int r = 0;
            Iterator it = handlers.iterator();
            while (it.hasNext()) {
                CLIHandler h = (CLIHandler)it.next();
                if (h.when != when) continue;

                r = h.cli(args);
                //System.err.println("notifyHandlers: exit code " + r + " from " + h);
                if (r != 0) {
                    return r;
                }
            }
            if (failOnUnknownOptions) {
                String[] argv = args.getArguments();
                for (int i = 0; i < argv.length; i++) {
                    if (argv[i] != null) {
                        // Unhandled option.
                        PrintWriter w = new PrintWriter(args.getOutputStream());
                        w.println("Unknown option: " + argv[i]); // NOI18N
                        showHelp(w, handlers);
                        w.flush();
                        return 2;
                    }
                }
            }
            return 0;
        } finally {
            args.reset(failOnUnknownOptions);
        }
    }
    
    /**
     * Represents result of initialization.
     * @see #initialize(String[], ClassLoader)
     * @see #initialize(Args, Integer, List)
     */
    static final class Status {
        private final File lockFile;
        private final int port;
        private final int exitCode;
        /**
         * General failure.
         */
        Status() {
            this(0);
        }
        /**
         * Failure due to a parse problem.
         * @param c bad status code (not 0)
         * @see #cli(Args)
         */
        Status(int c) {
            this(null, 0, c);
        }
        /**
         * Some measure of success.
         * @param l the lock file (not null)
         * @param p the server port (not 0)
         * @param c a status code (0 or not)
         */
        Status(File l, int p, int c) {
            lockFile = l;
            port = p;
            exitCode = c;
        }
        /**
         * Get the lock file, if available.
         * @return the lock file, or null if there is none
         */
        public File getLockFile() {
            return lockFile;
        }
        /**
         * Get the server port, if available.
         * @return a port number for the server, or 0 if there is no port open
         */
        public int getServerPort() {
            return port;
        }
        /**
         * Get the CLI parse status.
         * @return 0 for success, some other value for error conditions
         */
        public int getExitCode() {
            return exitCode;
        }
    }
    
    
    /** Initializes the system by creating lock file.
     *
     * @param args the command line arguments to recognize
     * @param classloader to find command CLIHandlers in
     * @param doAllInit if true, run all WHEN_INIT handlers now; else wait for {@link #finishInitialization}
     * @param failOnUnknownOptions if true, fail (status 2) if some options are not recognized (also checks for -? and -help)
     * @return the file to be used as lock file or null parsing of args failed
     */
    static Status initialize(String[] args, ClassLoader loader, boolean doAllInit, boolean failOnUnknownOptions) {
        return initialize(new Args(args, System.in, System.err, System.getProperty ("user.dir")), (Integer)null, allCLIs(loader), doAllInit, failOnUnknownOptions);
    }
    
    /**
     * What to do later when {@link #finishInitialization} is called.
     * May remain null.
     */
    private static Runnable doLater = null;
    
    /** Initializes the system by creating lock file.
     *
     * @param args the command line arguments to recognize
     * @param block the state we want to block in
     * @param handlers all handlers to use
     * @param doAllInit if true, run all WHEN_INIT handlers now; else wait for {@link #finishInitialization}
     * @param failOnUnknownOptions if true, fail (status 2) if some options are not recognized (also checks for -? and -help)
     * @return a status summary
     */
    static Status initialize(final Args args, Integer block, final List handlers, boolean doAllInit, final boolean failOnUnknownOptions) {
        // initial parsing of args
        {
            int r = notifyHandlers(args, handlers, WHEN_BOOT, failOnUnknownOptions);
            if (r != 0) {
                return new Status(r);
            }
        }
        
        // get the value
        String home = System.getProperty("netbeans.user"); // NOI18N
        if (home == null) {
            home = System.getProperty("user.home"); // NOI18N
        }
        File lockFile = new File(home, "lock"); // NOI18N
        
        for (int i = 0; i < 5; i++) {
            // try few times to succeed
            try {
                if (lockFile.exists()) {
                    enterState(5, block);
                    throw new IOException("EXISTS"); // NOI18N
                }
                lockFile.getParentFile().mkdirs();
                lockFile.createNewFile();
                lockFile.deleteOnExit();
                try {
                    // try to make it only user-readable (on Unix)
                    // since people are likely to leave a+r on their userdir
                    File chmod = new File("/bin/chmod"); // NOI18N
                    if (!chmod.isFile()) {
                        // Linux uses /bin, Solaris /usr/bin, others hopefully one of those
                        chmod = new File("/usr/bin/chmod"); // NOI18N
                    }
                    if (chmod.isFile()) {
                        int chmoded = Runtime.getRuntime().exec(new String[] {
                            chmod.getAbsolutePath(),
                            "go-rwx", // NOI18N
                            lockFile.getAbsolutePath()
                        }).waitFor();
                        if (chmoded != 0) {
                            throw new IOException("could not run " + chmod + " go-rwx " + lockFile); // NOI18N
                        }
                    }
                } catch (InterruptedException e) {
                    throw (IOException)new IOException(e.toString()).initCause(e);
                }
                
                enterState(10, block);
                
                byte[] arr = new byte[KEY_LENGTH];
                try {
                    SecureRandom.getInstance("SHA1PRNG").nextBytes(arr); // NOI18N
                } catch (NoSuchAlgorithmException e) {
                    assert false : e;
                }
                
                Server server = new Server(arr, block, handlers, failOnUnknownOptions);
                
                DataOutputStream os = new DataOutputStream(new FileOutputStream(lockFile));
                int p = server.getLocalPort();
                os.writeInt(p);
                
                enterState(20, block);
                
                os.write(arr);
                os.close();
                
                int exitCode;
                if (doAllInit) {
                    exitCode = notifyHandlers(args, handlers, WHEN_INIT, failOnUnknownOptions);
                } else {
                    doLater = new Runnable() {
                        public void run() {
                            int r = notifyHandlers(args, handlers, WHEN_INIT, failOnUnknownOptions);
                            if (r != 0) {
                                // Not much to do about it.
                                System.err.println("Post-initialization command-line options could not be run."); // NOI18N
                                //System.err.println("r=" + r + " args=" + java.util.Arrays.asList(args.getArguments()));
                            }
                        }
                    };
                    exitCode = 0;
                }
                
                enterState(0, block);
                return new Status(lockFile, server.getLocalPort(), exitCode);
                
            } catch (IOException ex) {
                if (!"EXISTS".equals(ex.getMessage())) { // NOI18N
                    ex.printStackTrace();
                }
                // already exists, try to read
                byte[] key = null;
                int port = -1;
                try {
                    DataInputStream is = new DataInputStream(new FileInputStream(lockFile));
                    port = is.readInt();
                    key = new byte[KEY_LENGTH];
                    is.readFully(key);
                    is.close();
                } catch (IOException ex2) {
                    // ok, try to read it once more
                }
                
                if (key != null && port != -1) {
                    try {
                        // ok, try to connect
                        enterState(28, block);
                        Socket socket = new Socket(InetAddress.getLocalHost(), port);
                        // wait max of 1s for reply
                        socket.setSoTimeout(5000);
                        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                        os.write(key);
                        os.flush();
                        
                        enterState(30, block);
                        
                        DataInputStream replyStream = new DataInputStream(socket.getInputStream());
                        COMMUNICATION: for (;;) {
                            enterState(32, block);
                            int reply = replyStream.read();
                            //System.err.println("reply=" + reply);
                            enterState(34, block);
                            
                            switch (reply) {
                                case REPLY_FAIL:
                                    enterState(36, block);
                                    break COMMUNICATION;
                                case REPLY_OK:
                                    enterState(38, block);
                                    // write the arguments
                                    String[] arr = args.getArguments();
                                    os.writeInt(arr.length);
                                    for (int a = 0; a < arr.length; a++) {
                                        os.writeUTF(arr[a]);
                                    }
                                    os.writeUTF (args.getCurrentDirectory().toString()); 
                                    os.flush();
                                    break;
                                case REPLY_EXIT:
                                    int exitCode = replyStream.readInt();
                                    if (exitCode == 0) {
                                        // to signal end of the world
                                        exitCode = -1;
                                    }
                                    
                                    os.close();
                                    replyStream.close();
                                    
                                    enterState(0, block);
                                    return new Status(lockFile, port, exitCode);
                                case REPLY_READ: {
                                    enterState(42, block);
                                    int howMuch = replyStream.readInt();
                                    byte[] byteArr = new byte[howMuch];
                                    args.getInputStream().read(byteArr);
                                    os.write(byteArr);
                                    os.flush();
                                    break;
                                }
                                case REPLY_WRITE: {
                                    enterState(44, block);
                                    int howMuch = replyStream.readInt();
                                    byte[] byteArr = new byte[howMuch];
                                    replyStream.read(byteArr);
                                    args.getOutputStream().write(byteArr);
                                    break;
                                }
                                case REPLY_AVAILABLE:
                                    enterState(46, block);
                                    os.writeInt(args.getInputStream().available());
                                    os.flush();
                                    break;
                                case -1:
                                    // EOF. Why does this happen?
                                    break;
                                default:
                                    assert false : reply;
                            }
                        }
                        
                        // connection ok, but secret key not recognized
                        // delete the lock file
                        
                    } catch (IOException ex2) {
                        ex2.printStackTrace();
                        // connection failed, the port is dead
                        enterState(33, block);
                    }
                    // remove the file and try once more
                    lockFile.delete();
                }
            }
            
            try {
                enterState(83, block);
                Thread.sleep((int)(Math.random() * 1000.00));
                enterState(85, block);
            } catch (InterruptedException ex) {
                // means nothing
            }
        }
        
        // failure
        return new Status();
    }

    /**
     * Run any {@link #WHEN_INIT} handlers that were passed to the original command line.
     * Should be called when the system is up and ready.
     * Cancels any existing actions, in case it is called twice.
     */
    static void finishInitialization() {
        if (doLater != null) {
            doLater.run();
            doLater = null;
        }
    }
    
    /** For a given classloader finds all registered CLIHandlers.
     */
    private static List allCLIs(ClassLoader loader) {
        /* should be, but we cannot use it yet, as openide is not separated:
        return new ArrayList(Lookups.metaInfServices(loader).lookup(new Lookup.Template(CLIHandler.class)).allInstances());
         */
        List res = new ArrayList();
        Enumeration en;
        try {
            en = loader.getResources("META-INF/services/org.netbeans.CLIHandler"); // NOI18N
        } catch (IOException ex) {
            ex.printStackTrace();
            return Collections.EMPTY_LIST;
        }
        while (en.hasMoreElements()) {
            URL url = (URL)en.nextElement();
            try {
                InputStream is = url.openStream();
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); // NOI18N
                    while (true) {
                        String line = reader.readLine();
                        if (line == null) break;
                        
                        // Ignore blank lines and comments.
                        line = line.trim();
                        if (line.length() == 0) continue;
                        
                        boolean remove = false;
                        if (line.charAt(0) == '#') {
                            if (line.length() == 1 || line.charAt(1) != '-') {
                                continue;
                            }
                            
                            // line starting with #- is a sign to remove that class from lookup
                            remove = true;
                            line = line.substring(2);
                        }
                        Class inst = Class.forName(line, false, loader);
                        
                        Object obj = inst.newInstance();
                        res.add((CLIHandler)obj);
                    }
                } finally {
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return res;
    }
    
    /** Class that represents available arguments to the CLI
     * handlers.
     */
    public static final class Args extends Object {
        private String[] args;
        private final String[] argsBackup;
        private InputStream is;
        private OutputStream os;
        private File currentDir;
        
        Args(String[] args, InputStream is, OutputStream os, String currentDir) {
            argsBackup = args;
            reset(false);
            this.is = is;
            this.os = os;
            this.currentDir = new File (currentDir);
        }
        
        /**
         * Restore the arguments list to a clean state.
         * If not consuming arguments, it is just set to the original list.
         * If consuming arguments, any nulled-out arguments are removed from the list.
         */
        void reset(boolean consume) {
            if (consume) {
                String[] a = args;
                if (a == null) {
                    a = argsBackup;
                }
                List l = new ArrayList(Arrays.asList(a));
                l.removeAll(Collections.singleton(null));
                args = (String[])l.toArray(new String[l.size()]);
            } else {
                args = (String[])argsBackup.clone();
            }
        }
        
        /**
         * Get the command-line arguments.
         * You may not modify the returned array except to set some elements
         * to null as you recognize them.
         * @return array of string arguments, may contain nulls
         */
        public String[] getArguments() {
            return args;
        }
        
        /**
         * Get an output stream to which data may be sent.
         * @return stream to write to
         */
        public OutputStream getOutputStream() {
            return os;
        }
        
        public File getCurrentDirectory () {
            return currentDir;
        }
        
        /**
         * Get an input stream that may supply additional data.
         * @return stream to read from
         */
        public InputStream getInputStream() {
            return is;
        }
    } // end of Args
    
    /** Server that creates local socket and communicates with it.
     */
    private static final class Server extends Thread {
        private byte[] key;
        private ServerSocket socket;
        private Integer block;
        private List handlers;
        private Socket work;
        private static volatile int counter;
        private final boolean failOnUnknownOptions;
        
        public Server(byte[] key, Integer block, List handlers, boolean failOnUnknownOptions) throws IOException {
            super("CLI Requests Server"); // NOI18N
            this.key = key;
            this.setDaemon(true);
            this.block = block;
            this.handlers = handlers;
            this.failOnUnknownOptions = failOnUnknownOptions;
            
            socket = new ServerSocket(0);
            
            start();
        }
        
        public Server(Socket request, byte[] key, Integer block, List handlers, boolean failOnUnknownOptions) throws IOException {
            super("CLI Handler Thread Handler: " + ++counter); // NOI18N
            this.key = key;
            this.setDaemon(true);
            this.block = block;
            this.handlers = handlers;
            this.work = request;
            this.failOnUnknownOptions = failOnUnknownOptions;
            
            start();
        }
        
        public int getLocalPort() {
            return socket.getLocalPort();
        }
        
        public void run() {
            if (work != null) {
                // I am a worker not listener server
                try {
                    handleConnect(work);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return;
            }
            
            
            while (true) {
                try {
                    enterState(65, block);
                    Socket s = socket.accept();
                    
                    // spans new request handler
                    new Server(s, key, block, handlers, failOnUnknownOptions);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        
        
        private void handleConnect(Socket s) throws IOException {
            
            byte[] check = new byte[key.length];
            DataInputStream is = new DataInputStream(s.getInputStream());
            
            enterState(70, block);
            
            is.readFully(check);
            
            enterState(90, block);
            
            DataOutputStream os = new DataOutputStream(s.getOutputStream());
            
            if (Arrays.equals(check, key)) {
                enterState(93, block);
                os.write(REPLY_OK);
                os.flush();
                
                // continue with arguments
                int numberOfArguments = is.readInt();
                String[] args = new String[numberOfArguments];
                for (int i = 0; i < args.length; i++) {
                    args[i] = is.readUTF();
                }
                String currentDir = is.readUTF ();
                
                Args arguments = new Args(args, new IS(is, os), new OS(is, os), currentDir);
                int res = notifyHandlers(arguments, handlers, WHEN_INIT, failOnUnknownOptions);
                
                if (res == 0) {
                    enterState(98, block);
                } else {
                    enterState(99, block);
                }
                
                os.write(REPLY_EXIT);
                os.writeInt(res);
            } else {
                enterState(103, block);
                os.write(REPLY_FAIL);
            }
            
            
            enterState(120, block);
            
            os.close();
            is.close();
        }
        
        private static final class IS extends InputStream {
            private DataInputStream is;
            private DataOutputStream os;
            /** serving requests only from this thread */
            private Thread thread;
            
            public IS(DataInputStream is, DataOutputStream os) {
                this.is = is;
                this.os = os;
                this.thread = Thread.currentThread();
            }
            
            public int read() throws IOException {
                byte[] arr = new byte[1];
                if (read(arr) == 1) {
                    return arr[0];
                } else {
                    return -1;
                }
            }
            
            public void close() throws IOException {
                super.close();
            }
            
            public int available() throws IOException {
                // ask for data
                os.write(REPLY_AVAILABLE);
                os.flush();
                // read provided data
                return is.readInt();
            }
            
            public int read(byte[] b) throws IOException {
                return read(b, 0, b.length);
            }
            
            public int read(byte[] b, int off, int len) throws IOException {
                // ask for data
                os.write(REPLY_READ);
                os.writeInt(len);
                os.flush();
                // read provided data
                return is.read(b, off, len);
            }
            
        } // end of IS
        
        private static final class OS extends OutputStream {
            private DataInputStream is;
            private DataOutputStream os;
            /** serving requests only from this thread */
            private Thread thread;
            
            public OS(DataInputStream is, DataOutputStream os) {
                this.is = is;
                this.os = os;
                this.thread = Thread.currentThread();
            }
            
            public void write(int b) throws IOException {
                byte[] arr = { (byte)b };
                write(arr);
            }
            
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
            
            public void close() throws IOException {
                super.close();
            }
            
            public void flush() throws IOException {
                os.flush();
            }
            
            public void write(byte[] b, int off, int len) throws IOException {
                os.write(REPLY_WRITE);
                os.writeInt(len);
                os.write(b, off, len);
            }
            
        } // end of OS
        
    } // end of Server
    
    
}
