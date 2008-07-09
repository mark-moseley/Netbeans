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

package org.netbeans.modules.groovy.grailsproject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.extexecution.api.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.modules.extexecution.api.ExecutionDescriptor;
import org.netbeans.modules.extexecution.api.ExecutionService;
import org.netbeans.modules.extexecution.api.input.InputProcessor;
import org.netbeans.modules.extexecution.api.input.InputProcessors;
import org.netbeans.modules.extexecution.api.input.LineProcessor;
import org.netbeans.modules.groovy.grails.api.ExecutionSupport;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grails.api.GrailsRuntime;
import org.netbeans.modules.groovy.grailsproject.actions.ConfigSupport;
import org.netbeans.modules.groovy.grailsproject.actions.RefreshProjectRunnable;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.LifecycleManager;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Hejl
 */
public class GrailsActionProvider implements ActionProvider {

    public static final String COMMAND_GRAILS_SHELL = "grails-shell"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(GrailsActionProvider.class.getName());

    private static final String[] supportedActions = {
        COMMAND_RUN,
        COMMAND_TEST,
        COMMAND_CLEAN,
        COMMAND_DELETE,
        COMMAND_GRAILS_SHELL
    };

    private final GrailsProject project;

    public GrailsActionProvider(GrailsProject project) {
        this.project = project;
    }


    public String[] getSupportedActions() {
        return supportedActions.clone();
    }

    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        final GrailsRuntime runtime = GrailsRuntime.getInstance();
        if (!runtime.isConfigured()) {
            ConfigSupport.showConfigurationWarning(runtime);
            return;
        }

        if (COMMAND_RUN.equals(command)) {
            LifecycleManager.getDefault().saveAll();
            executeRunAction();
        } else if (COMMAND_GRAILS_SHELL.equals(command)) {
            executeShellAction();
        } else if (COMMAND_TEST.equals(command)) {
            executeSimpleAction("test-app"); // NOI18N
        } else if (COMMAND_CLEAN.equals(command)) {
            executeSimpleAction("clean"); // NOI18N
        } else if (COMMAND_DELETE.equals(command)) {
            DefaultProjectOperations.performDefaultDeleteOperation(project);
        }
    }

    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        return true;
    }

    private void executeRunAction() {
        final GrailsServerState serverState = project.getLookup().lookup(GrailsServerState.class);
        if (serverState != null && serverState.isRunning()) {
            URL url = serverState.getRunningUrl();
            if (url != null) {
                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
            }
            return;
        }

        Callable<Process> callable = new Callable<Process>() {

            public Process call() throws Exception {
                Callable<Process> inner = ExecutionSupport.getInstance().createRunApp(GrailsProjectConfig.forProject(project));
                Process process = inner.call();
                final GrailsServerState serverState = project.getLookup().lookup(GrailsServerState.class);
                if (serverState != null) {
                    serverState.setProcess(process);
                }
                return process;
            }
        };

        ProjectInformation inf = project.getLookup().lookup(ProjectInformation.class);
        String displayName = inf.getDisplayName() + " (run-app)"; // NOI18N
        Runnable runnable = new Runnable() {
            public void run() {
                final GrailsServerState serverState = project.getLookup().lookup(GrailsServerState.class);
                if (serverState != null) {
                    serverState.setProcess(null);
                    serverState.setRunningUrl(null);
                }
            }
        };

        ExecutionDescriptor.Builder builder = new ExecutionDescriptor.Builder();
        builder.controllable(true).frontWindow(true).inputVisible(true).showProgress(true).showSuspended(true);
        builder.outProcessorFactory(new InputProcessorFactory() {
            public InputProcessor newInputProcessor() {
                return InputProcessors.bridge(new ServerURLProcessor(project));
            }
        });
        builder.postExecution(runnable);
        builder.optionsPath("org-netbeans-modules-groovy-support-options-GroovyOptionsCategory"); // NOI18N

        ExecutionService service = ExecutionService.newService(callable, builder.create(), displayName);
        service.run();
    }

    private void executeShellAction() {
        String command = "shell"; // NOI18N

        GrailsProjectConfig config = GrailsProjectConfig.forProject(project);
        File directory = FileUtil.toFile(config.getProject().getProjectDirectory());

        // XXX this is workaround for jline bug (native access to console on windows) used by grails
        Properties props = new Properties();
        props.setProperty("jline.WindowsTerminal.directConsole", "false"); // NOI18N

        GrailsRuntime.CommandDescriptor descriptor = new GrailsRuntime.CommandDescriptor(
                        command, directory, config.getEnvironment(), new String[] {}, props);
        Callable<Process> callable = GrailsRuntime.getInstance().createCommand(descriptor);

        ProjectInformation inf = project.getLookup().lookup(ProjectInformation.class);
        String displayName = inf.getDisplayName() + " (shell)"; // NOI18N

        ExecutionDescriptor.Builder builder = new ExecutionDescriptor.Builder();
        builder.controllable(true).frontWindow(true).inputVisible(true).showProgress(true).showSuspended(true);
        builder.postExecution(new RefreshProjectRunnable(project));
        builder.optionsPath("org-netbeans-modules-groovy-support-options-GroovyOptionsCategory"); // NOI18N

        ExecutionService service = ExecutionService.newService(callable, builder.create(), displayName);
        service.run();
    }

    private void executeSimpleAction(String command) {
        ProjectInformation inf = project.getLookup().lookup(ProjectInformation.class);
        String displayName = inf.getDisplayName() + " (" + command + ")"; // NOI18N

        Callable<Process> callable = ExecutionSupport.getInstance().createSimpleCommand(
                command, GrailsProjectConfig.forProject(project));

        ExecutionDescriptor.Builder builder = new ExecutionDescriptor.Builder();
        builder.controllable(true).frontWindow(true).inputVisible(true).showProgress(true);
        builder.postExecution(new RefreshProjectRunnable(project));
        builder.optionsPath("org-netbeans-modules-groovy-support-options-GroovyOptionsCategory"); // NOI18N

        ExecutionService service = ExecutionService.newService(callable, builder.create(), displayName);
        service.run();
    }

    private static class ServerURLProcessor implements LineProcessor {

        private final GrailsProject project;

        public ServerURLProcessor(GrailsProject project) {
            this.project = project;
        }

        public void processLine(String line) {
            if (line.contains("Browse to http:/")) {
                String urlString = line.substring(line.indexOf("http://"));

                URL url;
                try {
                    url = new URL(urlString);
                } catch (MalformedURLException ex) {
                    LOGGER.log(Level.WARNING, "Could not start browser", ex);
                    return;
                }

                GrailsServerState state = project.getLookup().lookup(GrailsServerState.class);
                if (state != null) {
                    state.setRunningUrl(url);
                }

                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
            }
        }

        public void reset() {
            // noop
        }

        public void close() {
            // noop
        }
    }
}
