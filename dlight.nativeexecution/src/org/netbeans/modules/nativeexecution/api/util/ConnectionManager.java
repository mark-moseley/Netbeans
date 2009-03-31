/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.api.util;

import java.awt.event.ActionEvent;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.NativeTaskExecutorService;
import org.netbeans.modules.nativeexecution.support.RemoteUserInfoProvider;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;

/**
 * Manages connections that are needed for remote {@link NativeProcess}
 * execution.
 * It is a singleton and should be accessed via static {@link #getInstance()}
 * method.
 *
 * @see ExecutionEnvironment
 */
public final class ConnectionManager {

    private final static Object lock = new String(ConnectionManager.class.getName());
    private final static java.util.logging.Logger log = Logger.getInstance();

    // Instance of the ConnectionManager
    private final static ConnectionManager instance;

    // Map that contains all connected sessions;
    private final Map<String, Session> sessions;

    // Actual sessions pool
    private final JSch jsch;


    static {
        instance = new ConnectionManager();
        ConnectionManagerAccessor.setDefault(new ConnectionManagerAccessorImpl());
    }

    private ConnectionManager() {
        jsch = new JSch();

        try {
            jsch.setKnownHosts(System.getProperty("user.home") + // NOI18N
                    "/.ssh/known_hosts"); // NOI18N
        } catch (JSchException ex) {
            log.warning("Unable to setKnownHosts for jsch. " + ex.getMessage()); // NOI18N
        }

        sessions = new HashMap<String, Session>();
    }

    /**
     * Returns instance of <tt>ConnectionManager</tt>.
     * @return instance of <tt>ConnectionManager</tt>
     */
    public static ConnectionManager getInstance() {
        return instance;
    }

    /**
     * Returns the ssh session for requested <tt>ExecutionEnvironment</tt>
     * or <tt>null</tt> if no active session exists.
     *
     * @param execEnv - execution environment to get <tt>Session</tt> for.
     *
     * @return <tt>null</tt> if no active (connected) session exist; <br>
     *         Already existent <tt>Session</tt> for specified
     *         <tt>execEnv</tt> on success.
     */

    /* package-visible */
    Session getSession(final ExecutionEnvironment env) {
        final String sessionKey = env.toString();

        synchronized (lock) {
            Session session = sessions.get(sessionKey);

            if (session != null && !session.isConnected()) {
                sessions.remove(sessionKey);
                return null;
            }

            return session;
        }
    }

    /**
     *
     * @param env <tt>ExecutionEnvironment</tt> to connect to.
     * @param password password to be used for identification
     * @param storePassword indicates whether to store the password (in
     * encrypted form) for further refference or not
     * @return <tt>true</tt> if this call to the function has initiated a new
     * connection to the <tt>env</tt>
     * @throws java.lang.Throwable
     */
    public boolean connectTo(
            final ExecutionEnvironment env,
            char[] password,
            boolean storePassword) throws IOException, CancellationException {

        if (env.isLocal()) {
            return true;
        }
        
        synchronized (lock) {
            if (getSession(env) != null) {
                // just return if already connected ...
                return false;
            }

            if (password != null) {
                PasswordManager.getInstance().put(env, password, storePassword);
            }

            return doConnect(env, RemoteUserInfoProvider.getUserInfo(env, false));
        }
    }

    /**
     *
     * @param env
     * @return  true only if call to this method initiated new connection...
     * @throws java.lang.Throwable
     */
    public boolean connectTo(
            final ExecutionEnvironment env) throws IOException, CancellationException {
        synchronized (lock) {
            boolean result = false;
            /*
            try {
                result = connectTo(env, PasswordManager.getInstance().get(env), false);
            } catch (ConnectException ex) {
                if (ex.getMessage().equals("Auth fail")) { // NOI18N
                    // Try with user-interaction
                    result = doConnect(env, RemoteUserInfoProvider.getUserInfo(env, true));
                } else {
                    throw ex;
                }
            }
            */
            final char[] passwd = PasswordManager.getInstance().get(env);
            if (passwd == null || passwd.length == 0) {
                // I don't know the password: trying with user-interaction
                result = doConnect(env, RemoteUserInfoProvider.getUserInfo(env, true));
            } else {
                try {
                    result = connectTo(env, PasswordManager.getInstance().get(env), false);
                } catch (ConnectException ex) {
                    if (ex.getMessage().equals("Auth fail")) { // NOI18N
                        // Try with user-interaction
                        result = doConnect(env, RemoteUserInfoProvider.getUserInfo(env, true));
                    } else {
                        throw ex;
                    }
                }
            }
            return result;
        }
    }

    private boolean doConnect(
            final ExecutionEnvironment env,
            final UserInfo userInfo) throws IOException, CancellationException {
        synchronized (lock) {
            Callable<Session> connectRunnable = new Callable<Session>() {

                public Session call() throws Exception {
                    final String user = env.getUser();
                    final String host = env.getHost();
                    final int sshPort = env.getSSHPort();

                    try {
                        synchronized (jsch) {
                            Session session = jsch.getSession(user, host, sshPort);
                            session.setUserInfo(userInfo);
                            session.connect();
                            return session;
                        }
                    } catch (JSchException e) {
                        if (e.getMessage().equals("Auth fail")) { // NOI18N
                            throw new ConnectException(e.getMessage());
                        } else if (e.getMessage().equals("Auth cancel")) { // NOI18N
                            throw new CancellationException(e.getMessage());
                        }

                        Throwable cause = e.getCause();
                        
                        if (cause != null && cause instanceof IOException) {
                            throw (IOException)cause;
                        }

                        // Should not happen
                        throw new IOException(e.getMessage());
                    }
                }
            };

            final Future<Session> connectResult = NativeTaskExecutorService.submit(
                    connectRunnable, "Connect to " + env.toString()); // NOI18N

            final Cancellable cancelConnection = new Cancellable() {

                public boolean cancel() {
                    return connectResult.cancel(true);
                }
            };

            ProgressHandle ph = ProgressHandleFactory.createHandle(
                    loc("ConnectionManager.Connecting", // NOI18N
                    env.toString()), cancelConnection);

            ph.start();

            Session session = null;

            try {
                session = connectResult.get();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException ex) {
                Throwable cause = ex.getCause();
                if (cause != null) {
                    if (cause instanceof IOException) {
                        throw (IOException)cause;
                    }

                    if (cause instanceof CancellationException) {
                        throw (CancellationException)cause;
                    }
                }
                // Should not happen
                throw new IOException(ex.getMessage());
            } finally {
                ph.finish();
            }

            if (session != null) {
                sessions.put(env.toString(), session);
                HostInfoUtils.updateHostInfo(env);
                
                log.info("New connection established: " + env.toString()); // NOI18N
                return true;
            }

            return false;
        }
    }

    /**
     * Tests whether the connection with the <tt>execEnv</tt> is established or
     * not.
     * @param execEnv execution environment to test connection with.
     * @return true if connection is established or if execEnv refers to the
     * localhost environment. false otherwise.
     */
    public boolean isConnectedTo(ExecutionEnvironment execEnv) {
        synchronized (lock) {
            if (execEnv.isLocal()) {
                return true;
            }

            if (sessions.containsKey(execEnv.toString())) {
                return sessions.get(execEnv.toString()).isConnected();
            }

            return false;
        }
    }

    /**
     * Returns {@link Action javax.swing.Action} that can be used
     * to get connected to the {@link ExecutionEnvironment}.
     * It is guaranteed that the same Action is returned for equal execution
     * environments.
     *
     * @param execEnv - {@link ExecutionEnvironment} to connect to.
     * @param onConnect - Runnable that is executed when connection is
     *        established.
     * @return action to be used to connect to the <tt>execEnv</tt>.
     * @see Action
     */
    public AsynchronousAction getConnectToAction(
            final ExecutionEnvironment execEnv, final Runnable onConnect) {

        return new ConnectToAction(execEnv, onConnect);
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(ConnectionManager.class, key, params);
    }

    /**
     * onConnect will be invoked ONLY if this action has initiated a new
     * connection.
     */
    private static class ConnectToAction
            extends AbstractAction implements AsynchronousAction {

        private final static ConnectionManager cm = ConnectionManager.getInstance();
        private final ExecutionEnvironment env;
        private final Runnable onConnect;

        private ConnectToAction(ExecutionEnvironment execEnv, Runnable onConnect) {
            this.env = execEnv;
            this.onConnect = onConnect;
        }

        public void actionPerformed(ActionEvent e) {
            NativeTaskExecutorService.submit(new Runnable() {

                public void run() {
                    try {
                        invoke();
                    } catch (Throwable ex) {
                        log.warning(ex.getMessage());
                    }
                }
            }, "Connecting to " + env.toString()); // NOI18N
        }

        public void invoke() throws IOException, CancellationException {
            boolean newConnectionEstablished = cm.connectTo(env);

            if (newConnectionEstablished) {
                onConnect.run();
            }
        }
    }

    private static final class ConnectionManagerAccessorImpl
            extends ConnectionManagerAccessor {

        @Override
        public Session getConnectionSession(ConnectionManager mgr, ExecutionEnvironment env) {
            return mgr.getSession(env);
        }
    }
}
