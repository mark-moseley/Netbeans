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

package org.netbeans.modules.groovy.grails.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.grails.KillableProcess;
import org.netbeans.modules.groovy.grails.RuntimeHelper;
import org.netbeans.modules.groovy.grails.server.GrailsInstanceProvider;
import org.netbeans.modules.groovy.grails.settings.GrailsSettings;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * Class providing the access to basic Grails runtime routines.
 * The class may not be configured and the method {@link #isConfigured()} can
 * be used to find out the state.
 *
 * @author Petr Hejl
 */
// TODO instance should be always configured in future
// TODO more appropriate would be getDefault and forProject
public final class GrailsPlatform {

    public static final String IDE_RUN_COMMAND = "run-app"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(GrailsPlatform.class.getName());

    private static final AtomicLong UNIQUE_MARK = new AtomicLong();

    private static final ClassPath EMPTY_CLASSPATH = ClassPathSupport.createClassPath(new URL[] {});

    private static final Set<String> GUARDED_COMMANDS = new HashSet<String>();

    static {
        Collections.addAll(GUARDED_COMMANDS, "run-app", "run-app-https", "run-war", "shell"); //NOI18N
    }

    private static GrailsPlatform instance;

    private Version version;

    private ClassPath classpath;

    private GrailsPlatform() {
        super();
    }

    /**
     * Return the instance representing the IDE configured Grails runtime.
     *
     * @return the instance representing the IDE configured Grails runtime
     */
    public static synchronized GrailsPlatform getDefault() {
        if (instance == null) {
            instance = new GrailsPlatform();
            GrailsSettings.getInstance().addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    if (GrailsSettings.GRAILS_BASE_PROPERTY.equals(evt.getPropertyName())) {
                        instance.reload();
                        GrailsInstanceProvider.getInstance().runtimeChanged();
                    }
                }
            });
            instance.reload();
        }
        return instance;
    }

    /**
     * Creates the callable spawning the command (process) described
     * by the command descriptor. Usually you don't need to use this method
     * directly as most of use cases can be solved with {@link ExecutionSupport}.
     *
     * @param descriptor descriptor of the command and its environment
     * @return the callable spawning the command (process)
     * @throws IllegalStateException if the runtime is not configured
     *
     * @see #isConfigured()
     * @see ExecutionSupport
     */
    public Callable<Process> createCommand(CommandDescriptor descriptor) {
        Parameters.notNull("descriptor", descriptor);

        if (!isConfigured()) {
            throw new IllegalStateException("Grails not configured"); // NOI18N
        }
        return new GrailsCallable(descriptor);
    }

    /**
     * Returns <code>true</code> if the runtime is configured (usable).
     *
     * @return <code>true</code> if the runtime is configured (usable)
     */
    public boolean isConfigured() {
        String grailsBase = GrailsSettings.getInstance().getGrailsBase();
        if (grailsBase == null) {
            return false;
        }

        return RuntimeHelper.isValidRuntime(new File(grailsBase));
    }

    public ClassPath getClassPath() {
        synchronized (this) {
            if (classpath != null) {
                return classpath;
            }

            if (!isConfigured()) {
                classpath = EMPTY_CLASSPATH;
                return classpath;
            }

            File grailsHome = getGrailsHome();
            if (!grailsHome.exists()) {
                classpath = EMPTY_CLASSPATH;
                return classpath;
            }

            List<File> jars = new ArrayList<File>();

            File distDir = new File(grailsHome, "dist"); // NOI18N
            File[] files = distDir.listFiles();
            if (files != null) {
                jars.addAll(Arrays.asList(files));
            }

            File libDir = new File(grailsHome, "lib"); // NOI18N
            files = libDir.listFiles();
            if (files != null) {
                jars.addAll(Arrays.asList(files));
            }

            List<URL> urls = new ArrayList<URL>(jars.size());

            for (File f : jars) {
                try {
                    if (f.isFile()) {
                        URL entry = f.toURI().toURL();
                        if (FileUtil.isArchiveFile(entry)) {
                            entry = FileUtil.getArchiveRoot(entry);
                            urls.add(entry);
                        }
                    }
                } catch (MalformedURLException mue) {
                    assert false : mue;
                }
            }

            classpath = ClassPathSupport.createClassPath(urls.toArray(new URL[urls.size()]));
            return classpath;
        }
    }

    // TODO not public API unless it is really needed
    public Version getVersion() {
        synchronized (this) {
            if (version != null) {
                return version;
            }

            String grailsBase = GrailsSettings.getInstance().getGrailsBase();
            try {
                if (grailsBase != null) {
                    String stringVersion = RuntimeHelper.getRuntimeVersion(new File(grailsBase));
                    if (stringVersion != null) {
                        version = Version.valueOf(stringVersion);
                    } else {
                        version = Version.VERSION_DEFAULT;
                    }
                } else {
                    version = Version.VERSION_DEFAULT;
                }
            } catch (IllegalArgumentException ex) {
                version = Version.VERSION_DEFAULT;
            }

            return version;
        }
    }

    /**
     * Reloads the runtime instance variables.
     */
    private void reload() {
        synchronized (this) {
            version = null;
            classpath = null;
        }

        // figure out the version on background
        // default executor as general purpose should be enough for this
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                synchronized (GrailsPlatform.this) {
                    if (version != null) {
                        return;
                    }

                    String grailsBase = GrailsSettings.getInstance().getGrailsBase();
                    try {
                        if (grailsBase != null) {
                            String stringVersion = RuntimeHelper.getRuntimeVersion(new File(grailsBase));
                            if (stringVersion != null) {
                                version = Version.valueOf(stringVersion);
                            } else {
                                version = Version.VERSION_DEFAULT;
                            }
                        } else {
                            version = Version.VERSION_DEFAULT;
                        }
                    } catch (IllegalArgumentException ex) {
                        version = Version.VERSION_DEFAULT;
                    }
                }
            }
        });
    }

    /**
     * Returns the grails home of the configured runtime.
     *
     * @return the grails home
     * @throws IllegalStateException if the runtime is not configured
     */
    public File getGrailsHome() {
        String grailsBase = GrailsSettings.getInstance().getGrailsBase();
        if (grailsBase == null || !RuntimeHelper.isValidRuntime(new File(grailsBase))) {
            throw new IllegalStateException("Grails not configured"); // NOI18N
        }

        return new File(grailsBase);
    }

    private static String createJvmArguments(Properties properties) {
        StringBuilder builder = new StringBuilder();
        int i = 0;

        for (Enumeration e = properties.propertyNames(); e.hasMoreElements();) {
            String key = e.nextElement().toString();
            String value = properties.getProperty(key);
            if (value != null) {
                if (i > 0) {
                    builder.append(" "); // NOI18N
                }
                builder.append("-D").append(key); // NOI18N
                builder.append("="); // NOI18N
                builder.append(value);
                i++;
            }
        }
        return builder.toString();
    }

    private static String createCommandArguments(String[] arguments) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < arguments.length; i++) {
            if (i > 0) {
                builder.append(" "); // NOI18N
            }
            builder.append(arguments[i]);
        }
        return builder.toString();
    }

    private static void checkForServer(CommandDescriptor descriptor, Process process) {
        if (IDE_RUN_COMMAND.equals(descriptor.getName())) { // NOI18N
            Project project = FileOwnerQuery.getOwner(
                    FileUtil.toFileObject(descriptor.getDirectory()));
            if (project != null) {
                GrailsInstanceProvider.getInstance().serverStarted(project, process);
            }
        }
    }

    /**
     * Class describing the command to invoke and its environment.
     *
     * This class is <i>Immutable</i>.
     */
    public static final class CommandDescriptor {

        private final String name;

        private final File directory;

        private final GrailsProjectConfig config;

        private final String[] arguments;

        private final Properties props;

        public static CommandDescriptor forProject(String name, File directory,
                GrailsProjectConfig config, String[] arguments, Properties props) {

            return new CommandDescriptor(name, directory, config, arguments, props);
        }

        /**
         * Creates the full customizable command descriptor.
         *
         * @param name command name
         * @param directory working directory
         * @param env grails environment
         * @param arguments command arguments
         * @param props environment properties
         */
        private CommandDescriptor(String name, File directory, GrailsProjectConfig config,
                String[] arguments, Properties props) {
            this.name = name;
            this.directory = directory;
            this.config = config;
            this.arguments = arguments.clone();
            this.props = props != null ? new Properties(props) : new Properties();
        }

        /**
         * Returns the command name.
         *
         * @return the command name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the working directory.
         *
         * @return the working directory
         */
        public File getDirectory() {
            return directory;
        }

        public GrailsProjectConfig getProjectConfig() {
            return config;
        }

        /**
         * Returns the command arguments.
         *
         * @return the command arguments
         */
        public String[] getArguments() {
            return arguments.clone();
        }

        /**
         * Returns the environment properties.
         *
         * @return the environment properties
         */
        public Properties getProps() {
            return new Properties(props);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CommandDescriptor other = (CommandDescriptor) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 41 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }


    }

    public static final class Version implements Comparable<Version> {

        public static final Version VERSION_DEFAULT = new Version(1, null, null, null, null);

        public static final Version VERSION_1_1 = new Version(1, 1, null, null, null);

        private final int major;

        private final Integer minor;

        private final Integer micro;

        private final Integer update;

        private final String qualifier;

        private String asString;

        protected Version(int major, Integer minor, Integer micro, Integer update, String qualifier) {
            this.major = major;
            this.minor = minor;
            this.micro = micro;
            this.update = update;
            this.qualifier = qualifier;
        }

        public static Version valueOf(String version) {
            String[] stringParts = version.split("-"); // NOI18N

            String qualifier = null;
            if (stringParts.length > 2) {
                throw new IllegalArgumentException(version);
            }
            if (stringParts.length == 2) {
                qualifier = stringParts[1];
            }


            String[] numberParts = stringParts[0].split("\\."); // NOI18N
            if (numberParts.length < 1 || numberParts.length > 4) {
                throw new IllegalArgumentException(version);
            }
            try {
                Integer[] parsed = new Integer[4];
                for (int i = 0; i < numberParts.length; i++) {
                    parsed[i] = Integer.valueOf(numberParts[i]);
                }
                return new Version(parsed[0], parsed[1], parsed[2], parsed[3], qualifier);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(version, ex);
            }
        }

        public int getMajor() {
            return major;
        }

        public int getMinor() {
            return minor == null ? 0 : minor.intValue();
        }

        public int getMicro() {
            return micro == null ? 0 : micro.intValue();
        }

        public int getUpdate() {
            return update == null ? 0 : update.intValue();
        }

        public String getQualifier() {
            return qualifier == null ? "" : qualifier; // NOI18N
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Version other = (Version) obj;
            if (this.getMajor() != other.getMajor()) {
                return false;
            }
            if (this.getMinor() != other.getMinor()) {
                return false;
            }
            if (this.getMicro() != other.getMicro()) {
                return false;
            }
            if (this.getUpdate() != other.getUpdate()) {
                return false;
            }
            if (!this.getQualifier().equals(other.getQualifier())) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 71 * hash + this.getMajor();
            hash = 71 * hash + this.getMinor();
            hash = 71 * hash + this.getMicro();
            hash = 71 * hash + this.getUpdate();
            hash = 71 * hash + this.getQualifier().hashCode();
            return hash;
        }

        public int compareTo(Version o) {
            if (this == o) {
                return 0;
            }

            int result = this.getMajor() - o.getMajor();
            if (result != 0) {
                return result;
            }

            result = this.getMinor() - o.getMinor();
            if (result != 0) {
                return result;
            }

            result = this.getMicro() - o.getMicro();
            if (result != 0) {
                return result;
            }

            result = this.getUpdate() - o.getUpdate();
            if (result != 0) {
                return result;
            }

            return this.getQualifier().compareTo(o.getQualifier());
        }

        @Override
        public String toString() {
            if (asString == null) {
                StringBuilder builder = new StringBuilder();
                builder.append(major);

                if (minor != null || micro != null || update != null) {
                    appendSeparator(builder);
                    builder.append(minor == null ? 0 : minor);
                }
                if (micro != null || update != null) {
                    appendSeparator(builder);
                    builder.append(micro == null ? 0 : micro);
                }
                if (update != null) {
                    appendSeparator(builder);
                    builder.append(update == null ? 0 : update);
                }
                if (qualifier != null) {
                    builder.append('-'); // NOI18N
                    builder.append(qualifier);
                }

                asString = builder.toString();
            }
            return asString;
        }

        private void appendSeparator(StringBuilder builder) {
            if (builder.length() > 0 && builder.charAt(builder.length() - 1) != '.') { // NOI18N
                builder.append('.'); // NOI18N
            }
        }

    }

    private static class GrailsCallable implements Callable<Process> {

        // FIXME: get rid of those proxy constants as soon as some NB Proxy API is available
        private static final String USE_PROXY_AUTHENTICATION = "useProxyAuthentication"; // NOI18N

        private static final String PROXY_AUTHENTICATION_USERNAME = "proxyAuthenticationUsername"; // NOI18N

        private static final String PROXY_AUTHENTICATION_PASSWORD = "proxyAuthenticationPassword"; // NOI18N

        private final CommandDescriptor descriptor;

        public GrailsCallable(CommandDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        public Process call() throws Exception {
            String executable =  Utilities.isWindows() ? RuntimeHelper.WIN_EXECUTABLE : RuntimeHelper.NIX_EXECUTABLE;
            File grailsExecutable = null;
            if (RuntimeHelper.isDebian(new File(GrailsSettings.getInstance().getGrailsBase()))) {
                grailsExecutable = new File(RuntimeHelper.DEB_EXECUTABLE);
            } else {
                grailsExecutable = new File(GrailsSettings.getInstance().getGrailsBase(), executable);
            }

            if (!grailsExecutable.exists()) {
                LOGGER.log(Level.WARNING, "Executable doesn't exist: "
                        + grailsExecutable.getAbsolutePath());

                return null;
            }

            LOGGER.log(Level.FINEST, "About to run: {0}", descriptor.getName());

            Properties props = new Properties(descriptor.getProps());
            GrailsEnvironment env = descriptor.getProjectConfig() != null
                    ? descriptor.getProjectConfig().getEnvironment()
                    : null;

            if (env != null && env.isCustom()) {
                props.setProperty("grails.env", env.toString()); // NOI18N
            }

            if (descriptor.getProjectConfig() != null && IDE_RUN_COMMAND.equals(descriptor.getName())) {
                String port = descriptor.getProjectConfig().getPort();
                if (port != null) {
                    props.setProperty("server.port", port); // NOI18N
                }
            }

            // XXX this is workaround for jline bug (native access to console on windows) used by grails
            props.setProperty("jline.WindowsTerminal.directConsole", "false"); // NOI18N

            String proxyString = getNetBeansHttpProxy(props);

            StringBuilder command = new StringBuilder();
            if (env != null && !env.isCustom()) {
                command.append(" ").append(env.toString());
            }
            command.append(" ").append(descriptor.getName());
            command.append(" ").append(createCommandArguments(descriptor.getArguments()));

            // FIXME fix this hack - needed for proper process tree kill
            // see KillableProcess
            String mark = "";
            if (Utilities.isWindows() && GUARDED_COMMANDS.contains(descriptor.getName())) {
                mark = UNIQUE_MARK.getAndIncrement() + descriptor.getDirectory().getAbsolutePath();
                command.append(" ").append("REM NB:" + mark); // NOI18N
            }

            LOGGER.log(Level.FINEST, "Command is: {0}", command.toString());

            NbProcessDescriptor grailsProcessDesc = new NbProcessDescriptor(
                    grailsExecutable.getAbsolutePath(), command.toString());

            String javaHome = null;
            JavaPlatform javaPlatform;
            if (descriptor.getProjectConfig() != null) {
                javaPlatform = descriptor.getProjectConfig().getJavaPlatform();
            } else {
                javaPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
            }

            Collection<FileObject> dirs = javaPlatform.getInstallFolders();
            if (dirs.size() == 1) {
                File file = FileUtil.toFile(dirs.iterator().next());
                if (file != null) {
                    javaHome = file.getAbsolutePath();
                }
            }

            String[] envp = new String[] {
                "GRAILS_HOME=" + GrailsSettings.getInstance().getGrailsBase(), // NOI18N
                "JAVA_HOME=" + javaHome, // NOI18N
                "http_proxy=" + proxyString, // NOI18N
                "HTTP_PROXY=" + proxyString, // NOI18N
                "JAVA_OPTS=" + createJvmArguments(props)
            };

            // no executable check before java6
            Process process = null;
            try {
                process = new KillableProcess(
                        grailsProcessDesc.exec(null, envp, true, descriptor.getDirectory()),
                        descriptor.getName(), mark);
            } catch (IOException ex) {
                NotifyDescriptor desc = new NotifyDescriptor.Message(
                        NbBundle.getMessage(GrailsPlatform.class, "MSG_StartFailedIOE",
                                grailsExecutable.getAbsolutePath()), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(desc);
                throw ex;
            }

            checkForServer(descriptor, process);
            return process;
        }

        /**
         * FIXME: get rid of the whole method as soon as some NB Proxy API is
         * available.
         */
        private static String getNetBeansHttpProxy(Properties props) {
            String host = System.getProperty("http.proxyHost"); // NOI18N
            if (host == null) {
                return null;
            }

            String portHttp = System.getProperty("http.proxyPort"); // NOI18N
            int port;

            try {
                port = Integer.parseInt(portHttp);
            } catch (NumberFormatException e) {
                port = 8080;
            }

            Preferences prefs = NbPreferences.root().node("org/netbeans/core"); // NOI18N
            boolean useAuth = prefs.getBoolean(USE_PROXY_AUTHENTICATION, false);

            String auth = "";
            if (useAuth) {
                String username = prefs.get(PROXY_AUTHENTICATION_USERNAME, "");
                String password = prefs.get(PROXY_AUTHENTICATION_PASSWORD, "");

                auth = username + ":" + password + '@'; // NOI18N

                if (!props.contains("http.proxyUser")) { // NOI18N
                    props.setProperty("http.proxyUser", prefs.get(PROXY_AUTHENTICATION_USERNAME, "")); // NOI18N
                }
                if (!props.contains("http.proxyPassword")) { // NOI18N
                    props.setProperty("http.proxyPassword", prefs.get(PROXY_AUTHENTICATION_PASSWORD, "")); // NOI18N
                }
            }

            if (!props.contains("http.proxyHost")) { // NOI18N
                props.setProperty("http.proxyHost", host); // NOI18N
            }
            if (!props.contains("http.proxyPort")) { // NOI18N
                props.setProperty("http.proxyPort", Integer.toString(port)); // NOI18N
            }

            // Gem requires "http://" in front of the port name if it's not already there
            if (host.indexOf(':') == -1) {
                host = "http://" + auth + host; // NOI18N
            }

            return host + ":" + port; // NOI18N
        }

    }
}
