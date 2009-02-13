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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.makeproject.api;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.api.remote.CommandProvider;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.BuildActionsProvider.BuildAction;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.DebuggerChooserConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.ui.MakeLogicalViewProvider;
import org.netbeans.modules.cnd.makeproject.ui.SelectExecutablePanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * Most of the code here came from DefaultProjectActionHandler
 * as result of refactoring.
 */
public class ProjectActionSupport {

    private static ProjectActionSupport instance;
    private final List<ProjectActionHandlerFactory> handlerFactories;

    private ProjectActionSupport() {
        handlerFactories = new ArrayList<ProjectActionHandlerFactory>(
                Lookup.getDefault().lookupAll(ProjectActionHandlerFactory.class));
    }

    /**
     * Singleton pattern: instance getter.
     *
     * @return singleton instance
     */
    public static synchronized ProjectActionSupport getInstance() {
        if (instance == null) {
            instance = new ProjectActionSupport();
        }
        return instance;
    }

    /**
     * Checks if given action type can be handled. All registered
     * handler factories are asked.
     *
     * @param conf
     * @param type
     * @return
     */
    public boolean canHandle(MakeConfiguration conf, ProjectActionEvent.Type type) {
        if (conf != null) {
            DebuggerChooserConfiguration chooser = conf.getDebuggerChooserConfiguration();
            CustomizerNode node = chooser.getNode();
            if (node instanceof ProjectActionHandlerFactory) {
                if (((ProjectActionHandlerFactory) node).canHandle(type)) {
                    return true;
                }
            }
        }
        for (ProjectActionHandlerFactory factory : handlerFactories) {
            if (factory.canHandle(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Executes an array of project actions asynchronously.
     *
     * @param paes  project actions
     */
    public void fireActionPerformed(ProjectActionEvent[] paes) {
        new HandleEvents(paes, null).go();
    }

    public void fireActionPerformed(ProjectActionEvent[] paes, ProjectActionHandler preferredHandler) {
        new HandleEvents(paes, preferredHandler).go();
    }

////////////////////////////////////////////////////////////////////////////////

    private InputOutput mainTab = null;
    private HandleEvents mainTabHandler = null;
    private ArrayList<String> tabNames = new ArrayList<String>();

    private class HandleEvents implements ExecutionListener {

        private InputOutput ioTab = null;
        private ProjectActionEvent[] paes;
        private String tabName;
        private String tabNameSeq;
        int currentAction = 0;
        private StopAction sa = null;
        private RerunAction ra = null;
        List<BuildAction> additional;
        private ProgressHandle progressHandle = null;
        private final Object lock = new Object();
        private ProjectActionHandler customHandler = null;
        private ProjectActionHandler currentHandler = null;

        public HandleEvents(ProjectActionEvent[] paes, ProjectActionHandler customHandler) {
            this.paes = paes;
            this.customHandler = customHandler;
            currentAction = 0;

            if (MakeOptions.getInstance().getReuse()) {
                synchronized (lock) {
                    if (mainTabHandler == null && mainTab != null /*&& !mainTab.isClosed()*/) {
                        mainTab.closeInputOutput();
                        mainTab = null;
                    }
                    tabName = getTabName(paes);
                    tabNameSeq = tabName;
                    if (tabNames.contains(tabName)) {
                        int seq = 2;
                        while (true) {
                            tabNameSeq = tabName + " #" + seq; // NOI18N
                            if (!tabNames.contains(tabNameSeq)) {
                                break;
                            }
                            seq++;
                        }
                    }
                    tabNames.add(tabNameSeq);
                    ioTab = getIOTab(tabNameSeq, true);
                    if (mainTabHandler == null) {
                        mainTab = ioTab;
                        mainTabHandler = this;
                    }
                }
            } else {
                tabName = getTabName(paes);
                tabNameSeq = tabName;
                ioTab = getIOTab(tabName, false);
            }
        }

        private String getTabName(ProjectActionEvent[] paes) {
            String projectName = ProjectUtils.getInformation(paes[0].getProject()).getDisplayName();
            StringBuilder name = new StringBuilder(projectName);
            name.append(" ("); // NOI18N
            for (int i = 0; i < paes.length; i++) {
                if (i >= 2) {
                    name.append("..."); // NOI18N
                    break;
                }
                name.append(paes[i].getActionName());
                if (i < paes.length - 1) {
                    name.append(", "); // NOI18N
                }
            }
            name.append(")"); // NOI18N
            if (paes.length > 0) {
                MakeConfiguration conf = (MakeConfiguration) paes[0].getConfiguration();
                if (!conf.getDevelopmentHost().isLocalhost()) {
                    String hkey = conf.getDevelopmentHost().getName();
                    name.append(" - ").append(hkey); //NOI18N
                }
            }
            return name.toString();
        }

        private InputOutput getTab() {
            return ioTab;
        }

        private ProgressHandle createProgressHandle() {
            ProgressHandle handle = ProgressHandleFactory.createHandle(tabNameSeq, new Cancellable() {
                public boolean cancel() {
                    sa.actionPerformed(null);
                    return true;
                }
            }, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    getTab().select();
                }
            });
            handle.setInitialDelay(0);
            return handle;
        }

        private ProgressHandle createProgressHandleNoCancel() {
            ProgressHandle handle = ProgressHandleFactory.createHandle(tabNameSeq,
                    new AbstractAction() {
                        public void actionPerformed(ActionEvent e) {
                            getTab().select();
                        }
                    });
            handle.setInitialDelay(0);
            return handle;
        }

        private InputOutput getIOTab(String name, boolean reuse) {
            sa = new StopAction(this);
            ra = new RerunAction(this);
            List<Action> list = new ArrayList<Action>();
            list.add(sa);
            list.add(ra);
            additional = BuildActionsProvider.getDefault().getActions(name, paes);
            list.addAll(additional);
            InputOutput tab;
            if (reuse) {
                tab = IOProvider.getDefault().getIO(name, false); // This will (sometimes!) find an existing one.
                tab.closeInputOutput(); // Close it...
            }
            tab = IOProvider.getDefault().getIO(name, list.toArray(new Action[list.size()])); // Create a new ...
            try {
                tab.getOut().reset();
            } catch (IOException ioe) {
            }

            progressHandle = createProgressHandle();
            progressHandle.start();

            return tab;
        }

        public void reRun() {
            currentAction = 0;
            getTab().closeInputOutput();
            synchronized (lock) {
                tabNames.add(tabNameSeq);
            }
            try {
                getTab().getOut().reset();
            } catch (IOException ioe) {
            }
            progressHandle = createProgressHandle();
            progressHandle.start();
            go();
        }

        public void go() {
            currentHandler = null;
            sa.setEnabled(false);
            ra.setEnabled(false);
            if (currentAction >= paes.length) {
                return;
            }

            final ProjectActionEvent pae = paes[currentAction];

            // Validate executable
            switch (pae.getType()) {
                case RUN:
                case DEBUG:
                case DEBUG_LOAD_ONLY:
                case DEBUG_STEPINTO:
                case CHECK_EXECUTABLE:
                case CUSTOM_ACTION:
                if (!checkExecutable(pae) || pae.getType() == ProjectActionEvent.Type.CHECK_EXECUTABLE) {
                    progressHandle.finish();
                    return;
                }
            }

            if (pae.getType() == ProjectActionEvent.Type.CUSTOM_ACTION && customHandler != null) {
                initHandler(customHandler, pae);
                customHandler.execute(ioTab);
            } else {

                for (ProjectActionHandlerFactory factory : handlerFactories) {
                    if (factory.canHandle(pae.getType())) {
                        ProjectActionHandler handler = currentHandler = factory.createHandler();
                        initHandler(handler, pae);
                        handler.execute(ioTab);
                        break;
                    }
                }

            }

        }

        private void initHandler(ProjectActionHandler handler, ProjectActionEvent pae) {
            handler.init(pae);
            progressHandle.finish();
            progressHandle = handler.canCancel()? createProgressHandle() : createProgressHandleNoCancel();
            progressHandle.start();
            sa.setEnabled(handler.canCancel());
            handler.addExecutionListener(this);
        }

        public ProjectActionHandler getCurrentHandler() {
            return currentHandler;
        }

        public void executionStarted() {
            if (additional != null) {
                for (BuildAction action : additional) {
                    action.setStep(currentAction);
                    action.executionStarted();
                }
            }
        }

        public void executionFinished(int rc) {
            if (additional != null) {
                for (Action action : additional) {
                    ((ExecutionListener) action).executionFinished(rc);
                }
            }
            if (paes[currentAction].getType() == ProjectActionEvent.Type.BUILD || paes[currentAction].getType() == ProjectActionEvent.Type.CLEAN) {
                // Refresh all files
                try {
                    FileObject projectFileObject = paes[currentAction].getProject().getProjectDirectory();
                    projectFileObject.getFileSystem().refresh(false);
                    MakeLogicalViewProvider.refreshBrokenItems(paes[currentAction].getProject());
                } catch (Exception e) {
                }
            }
            if (currentAction >= paes.length - 1 || rc != 0) {
                synchronized (lock) {
                    if (mainTabHandler == this) {
                        mainTabHandler = null;
                    }
                    tabNames.remove(tabNameSeq);
                }
                sa.setEnabled(false);
                ra.setEnabled(true);
                progressHandle.finish();
                return;
            }

            // This code is executed in finishing ProjectActionHandler's thread.
            // Starting next handler in this thread may lead to problems, such as
            // new threads being created in old handler's thread group, and NetBeans
            // thinking that old handler has not completed.
            // So the call to go() is posted to RequestProcessor.
            if (rc == 0) {
                currentAction++;
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        go();
                    }
                });
            }
        }

        private boolean checkExecutable(ProjectActionEvent pae) {
            // Check if something is specified
            String executable = pae.getExecutable();
            if (executable.length() == 0) {
                String errormsg;
                if (((MakeConfiguration) pae.getConfiguration()).isMakefileConfiguration()) {
                    SelectExecutablePanel panel = new SelectExecutablePanel((MakeConfiguration) pae.getConfiguration());
                    DialogDescriptor descriptor = new DialogDescriptor(panel, getString("SELECT_EXECUTABLE"));
                    panel.setDialogDescriptor(descriptor);
                    DialogDisplayer.getDefault().notify(descriptor);
                    if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
                        // Set executable in configuration
                        MakeConfiguration makeConfiguration = (MakeConfiguration) pae.getConfiguration();
                        executable = panel.getExecutable();
                        executable = FilePathAdaptor.naturalize(executable);
                        executable = IpeUtils.toRelativePath(makeConfiguration.getBaseDir(), executable);
                        executable = FilePathAdaptor.normalize(executable);
                        makeConfiguration.getMakefileConfiguration().getOutput().setValue(executable);
                        // Mark the project 'modified'
                        ConfigurationDescriptorProvider pdp = pae.getProject().getLookup().lookup(ConfigurationDescriptorProvider.class);
                        if (pdp != null) {
                            pdp.getConfigurationDescriptor().setModified();
                        }
                        // Set executable in pae
                        if (pae.getType() == ProjectActionEvent.Type.RUN) {
                            // Next block is commented out due to IZ120794
                            /*CompilerSet compilerSet = CompilerSetManager.getDefault(makeConfiguration.getDevelopmentHost().getName()).getCompilerSet(makeConfiguration.getCompilerSet().getValue());
                            if (compilerSet != null && compilerSet.getCompilerFlavor() != CompilerFlavor.MinGW) {
                            // IZ 120352
                            executable = FilePathAdaptor.naturalize(executable);
                            }*/
                            pae.setExecutable(executable);
                        } else {
                            pae.setExecutable(makeConfiguration.getMakefileConfiguration().getAbsOutput());
                        }
                    } else {
                        return false;
                    }
                } else {
                    errormsg = getString("NO_BUILD_RESULT"); // NOI18N
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
                    return false;
                }
            }
            // Check existence of executable
            if (!IpeUtils.isPathAbsolute(executable) && (executable.startsWith(".") || executable.indexOf('/') > 0)) { // NOI18N
                //executable is relative to project root - convert to absolute and check. Should be safe (?).
                executable = IpeUtils.toAbsolutePath(pae.getConfiguration().getBaseDir(), executable);
            }
            if (IpeUtils.isPathAbsolute(executable)) {
                Configuration conf = pae.getConfiguration();
                boolean ok = true;

                if (conf instanceof MakeConfiguration && !((MakeConfiguration) conf).getDevelopmentHost().isLocalhost()) {
                    ok = verifyRemoteExecutable(((MakeConfiguration) conf).getDevelopmentHost().getName(), executable);
                } else {
                    // FIXUP: getExecutable should really return fully qualified name to executable including .exe
                    // but it is too late to change now. For now try both with and without.
                    File file = new File(executable);
                    if (!file.exists()) {
                        file = new File(executable + ".exe"); // NOI18N
                    }
                    if (!file.exists() || file.isDirectory()) {
                        ok = false;
                    }
                }
                if (!ok) {
                    String errormsg = getString("EXECUTABLE_DOESNT_EXISTS", pae.getExecutable()); // NOI18N
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
                    return false;
                }
            }
            return true;
        }

    }

    /**
     * Verify a remote executable exists, is executable, and is not a directory.
     *
     * @param hkey The remote host
     * @param executable The file to remotely check
     * @return true if executable exists and is an executable, otherwise false
     */
    private static boolean verifyRemoteExecutable(String hkey, String executable) {
        PathMap mapper = HostInfoProvider.getDefault().getMapper(hkey);
        String remoteExecutable = mapper.getRemotePath(executable);
        CommandProvider cmd = Lookup.getDefault().lookup(CommandProvider.class);
        if (cmd != null) {
            return cmd.run(hkey, "test -x " + remoteExecutable + " -a -f " + remoteExecutable, null) == 0; // NOI18N
        }
        return false;
    }

    private static final class StopAction extends AbstractAction {

        HandleEvents handleEvents;

        public StopAction(HandleEvents handleEvents) {
            this.handleEvents = handleEvents;
        //System.out.println("handleEvents 1 " + handleEvents);
        //setEnabled(false); // initially, until ready
        }

        @Override
        public Object getValue(String key) {
            if (key.equals(Action.SMALL_ICON)) {
                return new ImageIcon(DefaultProjectActionHandler.class.getResource("/org/netbeans/modules/cnd/makeproject/ui/resources/stop.png"));
            } else if (key.equals(Action.SHORT_DESCRIPTION)) {
                return getString("TargetExecutor.StopAction.stop");
            } else {
                return super.getValue(key);
            }
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) {
                return;
            }
            setEnabled(false);
            if (handleEvents.getCurrentHandler() != null) {
                handleEvents.getCurrentHandler().cancel();
            }
        }
    }

    private static final class RerunAction extends AbstractAction {

        HandleEvents handleEvents;

        public RerunAction(HandleEvents handleEvents) {
            this.handleEvents = handleEvents;
        }

        @Override
        public Object getValue(String key) {
            if (key.equals(Action.SMALL_ICON)) {
                return new ImageIcon(DefaultProjectActionHandler.class.getResource("/org/netbeans/modules/cnd/makeproject/ui/resources/rerun.png"));
            } else if (key.equals(Action.SHORT_DESCRIPTION)) {
                return getString("TargetExecutor.RerunAction.rerun");
            } else {
                return super.getValue(key);
            }
        }

        public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            handleEvents.reRun();
        }
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getBundle(ProjectActionSupport.class).getString(s);
    }

    private static String getString(String s, String arg) {
        return NbBundle.getMessage(ProjectActionSupport.class, s, arg);
    }
}
