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
package org.netbeans.modules.subversion.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.logging.Level;
import org.netbeans.libs.svnclientadapter.spi.JavahlSupport;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.config.SvnConfigFiles;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNPromptUserPassword;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapter;
import org.netbeans.modules.subversion.client.cli.CommandlineClient;

/**
 * A SvnClient factory
*
 * @author Tomas Stupka
 */
public class SvnClientFactory {

    /** the only existing SvnClientFactory instance */
    private static SvnClientFactory instance;
    /** the only existing ClientAdapterFactory instance */
    private static ClientAdapterFactory factory;
    /** if an exception occured */
    private static SVNClientException exception = null;
    /** possible executable locations; fallback alternatives to $PATH */
    private static final String[] CMDLINE_LOCATIONS = new String[] {"/usr/local/bin"};
    /** indicates that something went terribly wrong with javahl init during the previous nb session */
    private static boolean javahlCrash = false;
    private final static int JAVAHL_INIT_SUCCESS = 1;
    private final static int JAVAHL_INIT_STOP_REPORTING = 2;

    public enum ConnectionType {
        javahl,
        cli
    }

    /** Creates a new instance of SvnClientFactory */
    private SvnClientFactory() {
    }

    /**
     * Returns the only existing SvnClientFactory instance
     *
     * @return the SvnClientFactory instance
     */
    public synchronized static SvnClientFactory getInstance() {
        init();
        return instance;
    }

    /**
     * Initializes the SvnClientFactory instance
     */
    public synchronized static void init() {
        if(instance == null) {
            instance = new SvnClientFactory();
            instance.setup();
        }
    }

    /**
     * Resets the SvnClientFactory instance
     */
    public synchronized static void reset() {
        if(instance == null) {
            init(); // calls setup
        } else {
            instance.setup();
        }
    }

    public static boolean isCLI() {
        assert factory != null;
        return factory.connectionType() == ConnectionType.cli;
    }

    public static boolean isJavaHl() {
        assert factory != null;
        return factory.connectionType() == ConnectionType.javahl;
    }

    /**
     * Returns a SvnClient, which isn't configured in any way.
     * Knows no username, password, has no SvnProgressSupport<br/>
     * Such an instance isn't supposed to work properly when calling remote svn commands.
     *
     * @return the SvnClient
     */
    public SvnClient createSvnClient() throws SVNClientException {
        if(exception != null) {
            throw exception;
        }
        return factory.createSvnClient();
    }

    /**
     *
     * Returns a SvnClient which is configured with the given <tt>username</tt>,
     * <tt>password</tt>, <tt>repositoryUrl</tt> and the <tt>support</tt>.<br>
     * In case a http proxy was given via <tt>pd</tt> an according entry for the <tt>repositoryUrl</tt>
     * will be created in the svn config file.
     * The mask <tt>handledExceptions</tt> specifies which exceptions are to be handled.
     *
     * @param repositoryUrl
     * @param support
     * @param username
     * @param password
     * @param handledExceptions
     *
     * @return the configured SvnClient
     *
     */
    public SvnClient createSvnClient(SVNUrl repositoryUrl, SvnProgressSupport support, String username, String password, int handledExceptions) throws SVNClientException {
        if(exception != null) {
            throw exception;
        }
        return factory.createSvnClient(repositoryUrl, support, username, password, handledExceptions);
    }

    /**
     * A SVNClientAdapterFactory will be setup, according to the svnClientAdapterFactory property.<br>
     * The CommandlineClientAdapterFactory is default as long no value is set for svnClientAdapterFactory.
     *
     */
    private void setup() {
        try {
            String factoryType = System.getProperty("svnClientAdapterFactory");

            if(factoryType == null ||
               factoryType.trim().equals("") ||
               factoryType.trim().equals(JhlClientAdapterFactory.JAVAHL_CLIENT))
            {
                setupJavaHl();
            } else if(factoryType.trim().equals(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT)) {
                setupCommandline();
            } else {
                throw new SVNClientException("Unknown factory: " + factoryType);
            }
        } catch (SVNClientException e) {
            exception = e;
        }
    }

    /**
     * Throws an exception if no SvnClientAdapter is available.
     */
    public static void checkClientAvailable() throws SVNClientException {
        if(exception != null) throw exception;
    }

    public static boolean wasJavahlCrash() {
        if(javahlCrash) {
            javahlCrash = false;
            return true;
        }
        return false;
    }

    private void setupJavaHl () {

        String jhlInitFile = System.getProperty("netbeans.user") + "/config/svn/jhlinit";
        File initFile = new File(jhlInitFile);

        if(checkJavahlCrash(initFile)) {
            setupCommandline();
            return;
        }
        try {
            if(!initFile.exists()) initFile.createNewFile();
        } catch (IOException ex) {
            Subversion.LOG.log(Level.INFO, null, ex);
        }

        try {
            JavahlSupport.setup();
        } catch (Throwable t) {
            String jhlErorrs = JhlClientAdapter.getLibraryLoadErrors();
            // something went wrong - fallback on the commandline
            Subversion.LOG.log(Level.INFO, t.getMessage());     
            Subversion.LOG.warning(jhlErorrs + "\n");
            Subversion.LOG.log(Level.INFO, "Could not setup JavaHl. Falling back on commandline!");
            setupCommandline();
            return;
        } finally {
            writeJavahlInitFlag(initFile, JAVAHL_INIT_SUCCESS);
        }
        factory = new ClientAdapterFactory() {
            protected ISVNClientAdapter createAdapter() {
                return SVNClientAdapterFactory.createSVNClient(JhlClientAdapterFactory.JAVAHL_CLIENT);
            }
            protected SvnClientInvocationHandler getInvocationHandler(ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions) {
                return new SvnClientInvocationHandler(adapter, desc, support, handledExceptions);
            }
            protected ISVNPromptUserPassword createCallback(SVNUrl repositoryUrl, int handledExceptions) {
                return new SvnClientCallback(repositoryUrl, handledExceptions);
            }
            protected ConnectionType connectionType() {
                return ConnectionType.javahl;
            }
        };
        Subversion.LOG.info("running on javahl");
    }

    private boolean checkJavahlCrash(File initFile) {
        if(!initFile.exists()) return false;
        FileReader r = null;
        try {
            r = new FileReader(initFile);
            int i = r.read();
            try { r.close(); r = null; } catch(IOException e) {}
            switch(i) {
                case -1: // empty means we crashed
                    writeJavahlInitFlag(initFile, JAVAHL_INIT_STOP_REPORTING);
                    javahlCrash = true;
                    Subversion.LOG.log(Level.WARNING, "It appears that subversion javahl initialization caused trouble in a previous Netbeans session. Please report.");
                    return true;
                case JAVAHL_INIT_STOP_REPORTING:
                    return true;
                case JAVAHL_INIT_SUCCESS:
                    return false;
            }
        } catch (IOException ex) {
            Subversion.LOG.log(Level.INFO, null, ex);
        } finally {
            try { if(r != null) r.close(); } catch (IOException ex) { }
        }
        return false;  // optimistic attitude
    }

    private void writeJavahlInitFlag(File initFile, int flag) {
        FileWriter w = null;
        try {
            w = new FileWriter(initFile);
            w.write(flag);
            w.flush();
        } catch (IOException ex) {
            Subversion.LOG.log(Level.INFO, null, ex);
        } finally {
            try { if(w != null) w.close(); } catch (IOException ex) { }
        }
    }

    /*
    private void setupJavaSvn () throws SVNClientException {
        JavaSvnClientAdapterFactory.setup();
        factory = new ClientAdapterFactory() {
            protected ISVNClientAdapter createAdapter() {
                return SVNClientAdapterFactory.createSVNClient(JavaSvnClientAdapterFactory.JAVASVN_CLIENT);
            }
            protected SvnClientInvocationHandler getInvocationHandler(ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions) {
                return new SvnClientInvocationHandler(adapter, desc, support, handledExceptions);
            }
            protected ISVNPromptUserPassword createCallback(SVNUrl repositoryUrl, int handledExceptions) {
                return new SvnClientCallback(repositoryUrl, handledExceptions);
            }
        };
        Subversion.LOG.info("svnClientAdapter running on javasvn");
    }
    */

    public void setupCommandline () {
        exception = null;
        CommandlineClient cc = new CommandlineClient();
        try {
            cc.checkSupportedVersion();
        } catch (SVNClientException ex) {
            exception = ex;
            return;
        }
        factory = new ClientAdapterFactory() {
            protected ISVNClientAdapter createAdapter() {
                return new CommandlineClient(); //SVNClientAdapterFactory.createSVNClient(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT);
            }
            protected SvnClientInvocationHandler getInvocationHandler(ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions) {
                return new SvnCmdLineClientInvocationHandler(adapter, desc, support, handledExceptions);
            }
            protected ISVNPromptUserPassword createCallback(SVNUrl repositoryUrl, int handledExceptions) {
                return null;
            }
            protected ConnectionType connectionType() {
                return ConnectionType.cli;
            }
        };
        Subversion.LOG.info("running on commandline");
    }

    private abstract class ClientAdapterFactory {

        abstract protected ISVNClientAdapter createAdapter();
        abstract protected SvnClientInvocationHandler getInvocationHandler(ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions);
        abstract protected ISVNPromptUserPassword createCallback(SVNUrl repositoryUrl, int handledExceptions);
        abstract protected ConnectionType connectionType();

        SvnClient createSvnClient() {
            SvnClientInvocationHandler handler = getInvocationHandler(createAdapter(), createDescriptor(null), null, -1);
            return createSvnClient(handler);
        }

        /**
         *
         * Returns a SvnClientInvocationHandler instance which is configured with the given <tt>adapter</tt>,
         * <tt>support</tt> and a SvnClientDescriptor for <tt>repository</tt>.
         *
         * @param adapter
         * @param support
         * @param repository
         *
         * @return the created SvnClientInvocationHandler instance
         *
         */
        public SvnClient createSvnClient(SVNUrl repositoryUrl, SvnProgressSupport support, String username, String password, int handledExceptions) {
            ISVNClientAdapter adapter = createAdapter();
            SvnClientInvocationHandler handler = getInvocationHandler(adapter, createDescriptor(repositoryUrl), support, handledExceptions);
            setupAdapter(adapter, username, password, createCallback(repositoryUrl, handledExceptions));
            return createSvnClient(handler);
        }

        private SvnClientDescriptor createDescriptor(final SVNUrl repositoryUrl) {
            return new SvnClientDescriptor() {
                public SVNUrl getSvnUrl() {
                    return repositoryUrl;
                }
            };
        }

        private SvnClient createSvnClient(SvnClientInvocationHandler handler) {
            Class proxyClass = Proxy.getProxyClass(SvnClient.class.getClassLoader(), new Class[]{ SvnClient.class } );
            Subversion.getInstance().cleanupFilesystem();
            try {
               return (SvnClient) proxyClass.getConstructor( new Class[] { InvocationHandler.class } ).newInstance( new Object[] { handler } );
            } catch (Exception e) {
                Subversion.LOG.log(Level.SEVERE, null, e);
            }
            return null;
        }

        protected void setupAdapter(ISVNClientAdapter adapter, String username, String password, ISVNPromptUserPassword callback) {
            if(callback != null) {
                adapter.addPasswordCallback(callback);
            }
            try {
                File configDir = FileUtil.normalizeFile(new File(SvnConfigFiles.getNBConfigPath()));
                adapter.setConfigDirectory(configDir);
                adapter.setUsername(username);
                adapter.setPassword(password);
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(ex, false, false);
            }
        }
    }
}
