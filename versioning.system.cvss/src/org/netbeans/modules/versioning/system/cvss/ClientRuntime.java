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

import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.event.TerminationEvent;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.BasicCommand;
import org.netbeans.lib.cvsclient.command.log.RlogCommand;
import org.netbeans.lib.cvsclient.command.importcmd.ImportCommand;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.add.AddCommand;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.ui.selectors.ProxyDescriptor;
import org.netbeans.modules.versioning.system.cvss.settings.CvsRootSettings;
import org.netbeans.modules.proxy.ClientSocketFactory;
import org.netbeans.modules.proxy.ConnectivitySettings;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;
import org.openide.util.Task;
import org.openide.ErrorManager;
import org.openide.windows.InputOutput;
import org.openide.windows.IOProvider;

import javax.net.SocketFactory;
import java.io.File;
import java.io.IOException;

/**
 * Defines a runtime environment for one CVSRoot. Everytime a command is executed for a new CVSRoot,
 * a new instance of ClientRuntime is created and cached. This objects is then responsible for running
 * commands for that CVS root.
 * 
 * @author Maros Sandor
 */
public class ClientRuntime {

    /**
     * The CVS Root this class manages.
     */ 
    private final String        cvsRoot; 
    
    /**
     * Processor to use when posting commands to given CVSRoot. It has a throughput of 1.
     */ 
    private RequestProcessor    requestProcessor;

    /**
     * Holds server communication log for associated cvs root.
     */
    private InputOutput log;

    ClientRuntime(String root) {
        cvsRoot = root;
        requestProcessor = new RequestProcessor("CVS: " + cvsRoot);
        log = IOProvider.getDefault().getIO(cvsRoot, false);
    }

    private void ensureValidCommand(File [] files) throws IllegalCommandException {
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            try {
                String root = Utils.getCVSRootFor(file);
                if (!root.equals(cvsRoot)) throw new IllegalCommandException("#63547 command includes files from different CVS root.\n Expected: " + cvsRoot + "\nGot:     " + root);
            } catch (IOException e) {
                throw new IllegalCommandException("Missing or invalid CVS/Root for: " + file);
            }
        }
    }

    /**
     * Creates a task that will execute the given command.
     *  
     * @param cmd command to schedule
     * @param globalOptions options to use when running the command
     * @param mgr listener for command events
     * @return RequestProcessor.Task a task ready to execute the command 

     * @throws IllegalCommandException if the command is not valid, e.g. it contains files that cannot be
     * processed by a single command (they do not have a common filesystem root OR their CVS Roots differ) 
     */ 
    public RequestProcessor.Task createTask(Command cmd, GlobalOptions globalOptions, final ExecutorSupport mgr)
            throws IllegalCommandException {
        
        File [] files = getCommandFiles(cmd);
        if ((cmd instanceof CheckoutCommand) == false && !(cmd instanceof RlogCommand)) {    // XXX
            ensureValidCommand(files);
        }

        if (globalOptions.getCVSRoot() == null) {
            globalOptions = (GlobalOptions) globalOptions.clone();
            globalOptions.setCVSRoot(cvsRoot);
        }

        Client client = createClient();
        if ((cmd instanceof RlogCommand)) {    // XXX
        }
        else if ((cmd instanceof CheckoutCommand)) {    // XXX
            BasicCommand bc = (BasicCommand) cmd;
            if (bc.getFiles() != null) {
                String path = bc.getFiles()[0].getAbsolutePath();
                client.setLocalPath(path);
            }
        } else if (cmd instanceof ImportCommand) {
            client.setLocalPath(((ImportCommand)cmd).getImportDirectory());
        } else {
            setLocalDirectory(client, files);
        }

        client.getEventManager().addCVSListener(mgr);
        final CommandRunnable cr = new CommandRunnable(client, globalOptions, cmd, mgr);
        mgr.commandEnqueued(cr);
        RequestProcessor.Task task = requestProcessor.create(cr);
        task.addTaskListener(new TaskListener() {
            public void taskFinished(Task task) {
                try {
                    // There are times when 'commandTerminated()' is not the last method called, therefore I introduced
                    // this event that really marks the very end of a command (thread end)
                    mgr.commandTerminated(new TerminationEvent(new Result(cr)));
                } catch (Throwable e) {
                    ErrorManager.getDefault().notify(ErrorManager.WARNING, e);                    
                } finally {
                    log.getOut().close();
                    log = IOProvider.getDefault().getIO(cvsRoot, false);
                }
            }
        });
        return task;
    }

    /**
     * Logs given message to associated console. The message
     * is appended at the end.
     */
    public void log(String message) {
        if (log.isClosed()) {
            log = IOProvider.getDefault().getIO(cvsRoot, false);
            try {
                // XXX workaround, otherwise it writes to nowhere
                log.getOut().reset();
            } catch (IOException e) {
                ErrorManager err = ErrorManager.getDefault();
                err.notify(e);
            }
            //log.select();
        }
        log.getOut().write(message);
    }

    public void logError(Throwable e) {
        e.printStackTrace(log.getOut());
    }

    /**
     * TODO: This method does not work
     */ 
    public void focusLog() {
        log.setFocusTaken(true);
        log.getOut().write("\n");
        log.setFocusTaken(false);
    }

    public void flushLog() {
        log.getOut().close();
    }


    private File[] getCommandFiles(Command cmd) {
        if (cmd instanceof AddCommand) {
            AddCommand c = (AddCommand) cmd;
            return c.getFiles();
        } else if (cmd instanceof BasicCommand) {
            BasicCommand c = (BasicCommand) cmd;
            return c.getFiles();
        } else {
            return new File[0];
        }
    }

    private void setLocalDirectory(Client client, File [] files) throws IllegalCommandException {
        if (files.length == 0) {
            return;
        }

        File commonParent;
        // TODO: revisit: how to determine local work dir for a command ?
        if (files[0].isDirectory()) {    // XXX it does not work for checkout
            commonParent = files[0].getParentFile(); 
        } else {
            commonParent = files[0].getParentFile();                                
        }
        
        for (int i = 1; i < files.length; i++) {
            if (!Utils.isParentOrEqual(commonParent, files[i])) {
                for (;;) {
                    commonParent = commonParent.getParentFile();
                    if (commonParent == null) throw new IllegalCommandException("Files do not have common parent!");
                    if (Utils.isParentOrEqual(commonParent, files[i])) {
                        break;
                    }
                }
            }
        }
        
        client.setLocalPath(commonParent.getAbsolutePath());
    }

    /**
     * Creates a new Client that will handle CVS operations.
     * 
     * @return a Client instance
     */ 
    private Client createClient() {
        Connection connection = setupConnection(CVSRoot.parse(cvsRoot), null);
        Client client = new Client(connection, CvsVersioningSystem.getInstance().getAdminHandler());
        return client;
    }
    
    /**
     * Sets up connection to a given CVS root including any proxies on route.
     * 
     * @param cvsRoot root to connect to
     * @return Connection object ready to connect to the given CVS root
     * @throws IllegalArgumentException if the 'method' part of the supplied CVS Root is not recognized
     */ 
    public static Connection setupConnection(CVSRoot cvsRoot, ProxyDescriptor proxy) throws IllegalArgumentException {
        if (cvsRoot.isLocal()) {
            LocalConnection con = new LocalConnection();
            con.setRepository(cvsRoot.getRepository());
            return con;
        }

        if (proxy == null ) proxy = CvsRootSettings.getProxyFor(cvsRoot);

        SocketFactory factory = SocketFactory.getDefault();
        if (proxy.isEffective()) {
            factory = new ClientSocketFactory(toConnectivitySettings(proxy));
        }

        String method = cvsRoot.getMethod();
        if (CVSRoot.METHOD_PSERVER.equals(method)) {
            PServerConnection con = new PServerConnection(cvsRoot, factory);
            String password = PasswordsFile.findPassword(cvsRoot.toString());                    
            con.setEncodedPassword(password);
            return con;
        } else if (CVSRoot.METHOD_EXT.equals(method)) {
            CvsRootSettings.ExtSettings extSettings = CvsRootSettings.getExtSettingsFor(cvsRoot);
            String userName = cvsRoot.getUserName();
            String host = cvsRoot.getHostName();
            if (extSettings.extUseInternalSsh) {
                int port = cvsRoot.getPort();
                port = port == 0 ? 22 : port;  // default port
                String password = extSettings.extPassword;
                if (password == null) {
                    password = "\n";  // NOI18N    user will be asked later on
                }
                SSHConnection sshConnection = new SSHConnection(factory, host, port, userName, password);
                sshConnection.setRepository(cvsRoot.getRepository());
                return sshConnection;
            } else {
                // What do we want to achieve here?
                // It's possible to mimics ordinary cvs or cvsnt behaviour:
                // Ordinary cvs style (CVS_RSH):
                //   command += " $hostname [-l$username] $CVS_SERVER"
                // cvsnt style (CVS_EXT and CVS_RSH):
                //   command += " cvs server"
                // I prefer the cvs style, see issue #62683 for details.

                String command = extSettings.extCommand;
                String cvs_server = System.getProperty("Env-CVS_SERVER", "cvs") + " server";  // NOI18N
                String userOption = ""; // NOI18N
                if ( userName != null ) {
                    userOption = " -l " + userName;  // NOI18N
                }
                command += " " + host + userOption + " " + cvs_server; // NOI18N
                ExtConnection connection = new ExtConnection(command);
                connection.setRepository(cvsRoot.getRepository());
                return connection;
            }
        }
        
        throw new IllegalArgumentException("Unrecognized CVS Root: " + cvsRoot);
    }

    public static ConnectivitySettings toConnectivitySettings(ProxyDescriptor pd) {
        ConnectivitySettings cs = new ConnectivitySettings();
        String pasword = pd.getPassword();
        int port = pd.getPort();
        switch (pd.getType()) {
            case ProxyDescriptor.TYPE_DIRECT:
                break;
            case ProxyDescriptor.TYPE_HTTP:
                if (port <= 0) {
                    ErrorManager.getDefault().log("Assuming default port 8080 for " + pd.getHost() + " HTTP proxy.");  // NOI18N
                    port = 8080;  // could be also 3127, 80, anyway user can specify exact value
                }
                cs.setProxy(ConnectivitySettings.CONNECTION_VIA_HTTPS, pd.getHost(), port, pd.getUserName(), pasword == null ? null : pasword.toCharArray());
                break;
            case ProxyDescriptor.TYPE_SOCKS:
                if (port <= 0) {
                    ErrorManager.getDefault().log("Assuming default port 1080 for " + pd.getHost() + " SOCKS proxy.");  // NOI18N
                    port = 1080;
                }
                cs.setProxy(ConnectivitySettings.CONNECTION_VIA_SOCKS, pd.getHost(), port, pd.getUserName(), pasword == null ? null : pasword.toCharArray());
                break;
            default:
                break;
        }
        return cs;
    }

    public String toString() {
        return "ClientRuntime queue=" + cvsRoot + " processor=" + requestProcessor;  // NOI18N
    }

    /**
     * Encapsulates result of a finished command. If the command succeeded, the 'error' field is null. If it failed,
     * it contains a throwable, cause of the error.
     */ 
    public static class Result  {
        
        private final CommandRunnable runnable;

        public Result(CommandRunnable runnable) {
            this.runnable = runnable;
        }

        /**
         * Get reason why command has not finished succesfully.
         * For user cancels return an exception too.
         */
        public Throwable getError() {
            return runnable.getFailure();
        }

        /** Has it been stopped by user's cancel? */
        public boolean isAborted() {
            return runnable.isAborted();
        }
    }
}
