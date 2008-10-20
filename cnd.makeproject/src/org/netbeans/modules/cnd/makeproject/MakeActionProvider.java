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

package org.netbeans.modules.cnd.makeproject;

import org.netbeans.modules.cnd.utils.ui.ModalMessageDlg;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.actions.BuildToolsAction;
import org.netbeans.modules.cnd.actions.ShellRunAction;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionSupport;
import org.netbeans.modules.cnd.makeproject.api.ProjectSupport;
import org.netbeans.modules.cnd.makeproject.api.RunDialogPanel;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.CustomToolConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platforms;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.makeproject.ui.utils.ConfSelectorPanel;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.utils.Path;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.api.utils.RemoteUtils;
import org.netbeans.modules.cnd.execution.ShellExecSupport;
import org.netbeans.modules.cnd.makeproject.api.CustomProjectActionHandler;
import org.netbeans.modules.cnd.makeproject.api.DefaultProjectActionHandler;
import org.netbeans.modules.cnd.makeproject.api.MakeCustomizerProvider;
import org.netbeans.modules.cnd.makeproject.api.PackagerManager;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompilerSet2Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.FortranCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.netbeans.modules.cnd.ui.options.LocalToolsPanelModel;
import org.netbeans.modules.cnd.ui.options.ToolsPanel;
import org.netbeans.modules.cnd.ui.options.ToolsPanelModel;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.WindowManager;

/** Action provider of the Make project. This is the place where to do
 * strange things to Make actions. E.g. compile-single.
 */
public class MakeActionProvider implements ActionProvider {

    // Commands available from Make project
    public static final String COMMAND_BATCH_BUILD = "batch_build"; // NOI18N
    public static final String COMMAND_BUILD_PACKAGE = "build_packages"; // NOI18N
    public static final String COMMAND_DEBUG_LOAD_ONLY = "debug.load.only"; // NOI18N
    public static final String COMMAND_CUSTOM_ACTION = "custom.action"; // NOI18N
    private static final String[] supportedActions = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_STEP_INTO,
        COMMAND_DEBUG_LOAD_ONLY,
        COMMAND_DEBUG_SINGLE,
        COMMAND_BATCH_BUILD,
        COMMAND_BUILD_PACKAGE,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,
        COMMAND_CUSTOM_ACTION,
    };

    // Project
    MakeProject project;

    // Project Descriptor
    ConfigurationDescriptor projectDescriptor = null;

    /** Map from commands to ant targets */
    Map<String,String[]> commands;
    Map<String,String[]> commandsNoBuild;

    private boolean lastValidation = false;

    public MakeActionProvider( MakeProject project) {

        commands = new HashMap<String,String[]>();
        commands.put(COMMAND_BUILD, new String[] {"save", "build"}); // NOI18N
        commands.put(COMMAND_BUILD_PACKAGE, new String[] {"save", "build", "build-package"}); // NOI18N
        commands.put(COMMAND_CLEAN, new String[] {"save", "clean"}); // NOI18N
        commands.put(COMMAND_REBUILD, new String[] {"save", "clean", "build"}); // NOI18N
        commands.put(COMMAND_RUN, new String[] {"save", "build", "run"}); // NOI18N
        commands.put(COMMAND_DEBUG, new String[] {"save", "build", "debug"}); // NOI18N
        commands.put(COMMAND_DEBUG_STEP_INTO, new String[] {"save", "build", "debug-stepinto"}); // NOI18N
        commands.put(COMMAND_DEBUG_LOAD_ONLY, new String[] {"save", "build", "debug-load-only"}); // NOI18N
        commands.put(COMMAND_RUN_SINGLE, new String[] {"run-single"}); // NOI18N
        commands.put(COMMAND_DEBUG_SINGLE, new String[] {"debug-single"}); // NOI18N
        commands.put(COMMAND_COMPILE_SINGLE, new String[] {"save", "compile-single"}); // NOI18N
        commands.put(COMMAND_CUSTOM_ACTION, new String[] {"save", "build", "custom-action"}); // NOI18N
        commandsNoBuild = new HashMap<String,String[]>();
        commandsNoBuild.put(COMMAND_BUILD, new String[] {"save", "build-package"}); // NOI18N
        commandsNoBuild.put(COMMAND_BUILD_PACKAGE, new String[] {"save", "build"}); // NOI18N
        commandsNoBuild.put(COMMAND_CLEAN, new String[] {"save", "clean"}); // NOI18N
        commandsNoBuild.put(COMMAND_REBUILD, new String[] {"save", "clean", "build"}); // NOI18N
        commandsNoBuild.put(COMMAND_RUN, new String[] {"run"}); // NOI18N
        commandsNoBuild.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
        commandsNoBuild.put(COMMAND_DEBUG_STEP_INTO, new String[] {"debug-stepinto"}); // NOI18N
        commandsNoBuild.put(COMMAND_DEBUG_LOAD_ONLY, new String[] {"debug-load-only"}); // NOI18N
        commandsNoBuild.put(COMMAND_CUSTOM_ACTION, new String[] {"save", "custom-action"}); // NOI18N

        this.project = project;
    }

    private FileObject findBuildXml() {
        return project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
    }

    private boolean isProjectDescriptorLoaded() {
        if (projectDescriptor == null) {
            ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            return pdp.gotDescriptor();
        } else {
            return true;
        }
    }
    private MakeConfigurationDescriptor getProjectDescriptor() {
        if (projectDescriptor == null) {
            ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            projectDescriptor = pdp.getConfigurationDescriptor();
        }
        return (MakeConfigurationDescriptor)projectDescriptor;
    }

    public String[] getSupportedActions() {
        return supportedActions;
    }

    public void invokeAction( final String command, final Lookup context) throws IllegalArgumentException {
        if (COMMAND_DELETE.equals(command)) {
            DefaultProjectOperations.performDefaultDeleteOperation(project);
            return ;
        }

        if (COMMAND_COPY.equals(command)) {
            DefaultProjectOperations.performDefaultCopyOperation(project);
            return ;
        }

        if (COMMAND_MOVE.equals(command)) {
            DefaultProjectOperations.performDefaultMoveOperation(project);
            return ;
        }

        if (COMMAND_RENAME.equals(command)) {
            DefaultProjectOperations.performDefaultRenameOperation(project, null);
            return ;
        }

        if (COMMAND_RUN_SINGLE.equals(command)) {
            Node node = context.lookup(Node.class);
            if (node != null) {
                ShellRunAction.performAction(node);
            }
            return;
        }

        // Basic info
        ProjectInformation info = project.getLookup().lookup(ProjectInformation.class);
        final String projectName = info.getDisplayName();
        final MakeConfigurationDescriptor pd = getProjectDescriptor();
        final MakeConfiguration conf = (MakeConfiguration) pd.getConfs().getActive();

        // vv: leaving all logic to be later called from EDT
        // (although I'm not sure all of below need to be done in EDT)
        Runnable actionWorker = new Runnable() {
            public void run() {
                // Add actions to do
                ArrayList actionEvents = new ArrayList();
                if (command.equals(COMMAND_BATCH_BUILD)) {
                    BatchConfigurationSelector batchConfigurationSelector = new BatchConfigurationSelector(pd.getConfs().getConfs());
                    String batchCommand = batchConfigurationSelector.getCommand();
                    Configuration[] confs = batchConfigurationSelector.getSelectedConfs();
                    if (batchCommand != null && confs != null) {
                        for (int i = 0; i < confs.length; i++)
                            addAction(actionEvents, projectName, pd, (MakeConfiguration)confs[i], batchCommand, context);
                    } else {
                        // Close button
                        return;
                    }
                } else {
                    addAction(actionEvents, projectName, pd, conf, command, context);
                }

                // Execute actions
                if (actionEvents.size() > 0) {
                    ProjectActionSupport.fireActionPerformed((ProjectActionEvent[])actionEvents.toArray(new ProjectActionEvent[actionEvents.size()]));
                }
            }
        };
        runActionWorker(conf.getDevelopmentHost().getName(), actionWorker);
    }

    private static void runActionWorker(String hkey, Runnable actionWorker) {
        if (RemoteUtils.isLocalhost(hkey)) {
            actionWorker.run();
        } else {
            ServerList registry = Lookup.getDefault().lookup(ServerList.class);
            assert registry != null;
            ServerRecord record = registry.get(hkey);
            assert record != null;
            invokeRemoteHostAction(record, actionWorker);
        }
    }

    public void invokeCustomAction(final String projectName, final MakeConfigurationDescriptor pd, final MakeConfiguration conf, final CustomProjectActionHandler customProjectActionHandler) {
        Runnable actionWorker = new Runnable() {
            public void run() {
                ArrayList actionEvents = new ArrayList();
                addAction(actionEvents, projectName, pd, conf, MakeActionProvider.COMMAND_CUSTOM_ACTION, null);
                ActionEvent ae = new ActionEvent((ProjectActionEvent[])actionEvents.toArray(new ProjectActionEvent[actionEvents.size()]), 0, null);
                DefaultProjectActionHandler defaultProjectActionHandler = new DefaultProjectActionHandler();
                defaultProjectActionHandler.setCustomActionHandlerProvider(customProjectActionHandler);
                defaultProjectActionHandler.actionPerformed(ae);
            }
        };
        runActionWorker(conf.getDevelopmentHost().getName(), actionWorker);
    }

    private static void invokeRemoteHostAction(final ServerRecord record, final Runnable actionWorker) {
        if (!record.isDeleted() && record.isOnline()) {
            actionWorker.run();
        } else {
            String message;
            int res = JOptionPane.NO_OPTION;
            if (record.isDeleted()) {
                message = MessageFormat.format(getString("ERR_RequestingDeletedConnection"), record.getName());
                res = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), message, getString("DLG_TITLE_DeletedConnection"), JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION) {
                    ServerList registry = Lookup.getDefault().lookup(ServerList.class);
                    assert registry != null;
                    registry.addServer(record.getName(), false, true);
                }
            } else if (!record.isOnline()) {
                message = MessageFormat.format(getString("ERR_NeedToInitializeRemoteHost"), record.getName());
                res = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), message, getString("DLG_TITLE_Connect"), JOptionPane.YES_NO_OPTION);
            }
            if (res == JOptionPane.YES_OPTION) {
                // start validation phase
                final Frame mainWindow = WindowManager.getDefault().getMainWindow();
                Runnable csmWorker = new Runnable() {
                    public void run() {
                        try {
                            record.validate(true);
                            // initialize compiler sets for remote host if needed
                            CompilerSetManager csm = CompilerSetManager.getDefault(record.getName());
                            csm.initialize(true);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                Runnable edtWorker = new Runnable() {
                    public void run() {
                        if (record.isOnline()) {
                            actionWorker.run();
                        }
                    }
                };
                String msg = NbBundle.getMessage(MakeActionProvider.class, "MSG_Configure_Host_Progress", record.getName());
                ModalMessageDlg.runLongTask(mainWindow, csmWorker, edtWorker, NbBundle.getMessage(MakeActionProvider.class, "DLG_TITLE_Configure_Host"), msg);
            }
        }
    }

    class BatchConfigurationSelector implements ActionListener {
        private JButton buildButton = new JButton(getString("BuildButton"));
        private JButton rebuildButton = new JButton(getString("CleanBuildButton"));
        private JButton cleanButton = new JButton(getString("CleanButton"));
        private JButton closeButton = new JButton(getString("CloseButton"));
        private ConfSelectorPanel confSelectorPanel;
        private String command = null;
        private Dialog dialog = null;

        BatchConfigurationSelector(Configuration[] confs) {
            confSelectorPanel = new ConfSelectorPanel(getString("CheckLabel"), getString("CheckLabelMn").charAt(0), confs, new JButton[] {buildButton, rebuildButton, cleanButton});

            String dialogTitle = MessageFormat.format(getString("BatchBuildTitle"), // NOI18N
                    new Object[] { ProjectUtils.getInformation(project).getDisplayName()});

            buildButton.setMnemonic(getString("BuildButtonMn").charAt(0));
            buildButton.getAccessibleContext().setAccessibleDescription(getString("BuildButtonAD"));
            buildButton.addActionListener(this);
            rebuildButton.setMnemonic(getString("CleanBuildButtonMn").charAt(0));
            rebuildButton.addActionListener(this);
            rebuildButton.getAccessibleContext().setAccessibleDescription(getString("CleanBuildButtonAD"));
            cleanButton.setMnemonic(getString("CleanButtonMn").charAt(0));
            cleanButton.addActionListener(this);
            cleanButton.getAccessibleContext().setAccessibleDescription(getString("CleanButtonAD"));
            closeButton.getAccessibleContext().setAccessibleDescription(getString("CloseButtonAD"));
            // Show the dialog
            DialogDescriptor dd = new DialogDescriptor(confSelectorPanel, dialogTitle, true, new Object[] {closeButton}, closeButton, 0, null, null);
            //DialogDisplayer.getDefault().notify(dd);
            dialog = DialogDisplayer.getDefault().createDialog(dd);
            dialog.getAccessibleContext().setAccessibleDescription(getString("BatchBuildDialogAD"));
            dialog.setVisible(true);
        }

        public Configuration[] getSelectedConfs() {
            return confSelectorPanel.getSelectedConfs();
        }

        public String getCommand() {
            return command;
        }

        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == buildButton)
                command = COMMAND_BUILD;
            else if (evt.getSource() == rebuildButton)
                command = COMMAND_REBUILD;
            else if (evt.getSource() == cleanButton)
                command = COMMAND_CLEAN;
            else
                assert false;
            dialog.dispose();
        }
    }

    public void addAction(ArrayList actionEvents, String projectName, MakeConfigurationDescriptor pd, MakeConfiguration conf, String command, Lookup context) throws IllegalArgumentException {
        String[] targetNames;
        boolean validated = false;
        lastValidation = false;

        targetNames = getTargetNames(command, context);
        if (targetNames == null) {
            return;
        }
        if (targetNames.length == 0) {
            targetNames = null;
        }

        for (int i = 0; i < targetNames.length; i++) {
            String targetName = targetNames[i];
            int actionEvent;
            if (targetName.equals("build")) { // NOI18N
                actionEvent = ProjectActionEvent.BUILD;
            } else if (targetName.equals("build-package")) { // NOI18N
                actionEvent = ProjectActionEvent.BUILD;
            } else if (targetName.equals("clean")) { // NOI18N
                actionEvent = ProjectActionEvent.CLEAN;
            } else if (targetName.equals("compile-single")) { // NOI18N
                actionEvent = ProjectActionEvent.BUILD;
            } else if (targetName.equals("run")) { // NOI18N
                actionEvent = ProjectActionEvent.RUN;
            } else if (targetName.equals("run-single")) { // NOI18N
                actionEvent = ProjectActionEvent.RUN;
            } else if (targetName.equals("debug")) { // NOI18N
                actionEvent = ProjectActionEvent.DEBUG;
            } else if (targetName.equals("debug-stepinto")) { // NOI18N
                actionEvent = ProjectActionEvent.DEBUG_STEPINTO;
            } else if (targetName.equals("debug-load-only")) { // NOI18N
                actionEvent = ProjectActionEvent.DEBUG_LOAD_ONLY;
            } else if (targetName.equals("custom-action")) { // NOI18N
                actionEvent = ProjectActionEvent.CUSTOM_ACTION;
            } else {
                // All others
                actionEvent = ProjectActionEvent.RUN;
            }

            PlatformInfo pi = conf.getPlatformInfo();

            if (targetName.equals("save")) { // NOI18N
                // Save all files and projects
                if (MakeOptions.getInstance().getSave())
                    LifecycleManager.getDefault().saveAll();
                if (!ProjectSupport.saveAllProjects(getString("NeedToSaveAllText"))) // NOI18N
                    return;
            } else if (targetName.equals("run") || targetName.equals("debug") || targetName.equals("debug-stepinto") || targetName.equals("debug-load-only")) { // NOI18N
                if (!validateBuildSystem(pd, conf, validated)) {
                    return;
                }
                validated = true;
                if (conf.isMakefileConfiguration()) {
                    String path;
                    if (targetName.equals("run")) { // NOI18N
                        path = conf.getMakefileConfiguration().getOutput().getValue();
                        if (path.length() > 0 && !IpeUtils.isPathAbsolute(path)) {
                            // make path relative to run working directory
                            // path here should always be in unix style, see issue 149404
                            path = conf.getMakefileConfiguration().getAbsOutput();
                            path = IpeUtils.toRelativePath(conf.getProfile().getRunDirectory(), path);
                        }
                    } else {
                        // Always absolute
                        path = conf.getMakefileConfiguration().getAbsOutput();
                    }
                    ProjectActionEvent projectActionEvent = new ProjectActionEvent(
                            project,
                            actionEvent,
                            getActionName(projectName, targetName, conf),
                            path,
                            conf,
                            null,
                            false);
                    actionEvents.add(projectActionEvent);
                    RunDialogPanel.addElementToExecutablePicklist(path);
                } else if (conf.isLibraryConfiguration()) {
                    // Should never get here...
                    assert false;
                    return;
                } else if (conf.isCompileConfiguration()) {
                    RunProfile runProfile = null;
                    if (conf.getPlatform().getValue() == Platform.PLATFORM_WINDOWS) {
                        // On Windows we need to add paths to dynamic libraries from subprojects to PATH
                        runProfile = conf.getProfile().cloneProfile();
                        Set subProjectOutputLocations = conf.getSubProjectOutputLocations();
                        String path = ""; // NOI18N
                        Iterator iter = subProjectOutputLocations.iterator();
                        while (iter.hasNext()) {
                            String location = FilePathAdaptor.naturalize((String)iter.next());
                            path = location + ";" + path; // NOI18N
                        }
                        String userPath = runProfile.getEnvironment().getenv(pi.getPathName());
                        if (userPath == null)
                            userPath = HostInfoProvider.getDefault().getEnv(conf.getDevelopmentHost().getName()).get(pi.getPathName());
                        path = path + ";" + userPath; // NOI18N
                        runProfile.getEnvironment().putenv(pi.getPathName(), path);
                    } else if (Platforms.getPlatform(conf.getPlatform().getValue()).getId() == Platform.PLATFORM_MACOSX) {
                        // On Mac OS X we need to add paths to dynamic libraries from subprojects to DYLD_LIBRARY_PATH
                        Set subProjectOutputLocations = conf.getSubProjectOutputLocations();
                        Iterator iter = subProjectOutputLocations.iterator();
                        if (iter.hasNext()) {
                            String extPath = HostInfoProvider.getDefault().getEnv(conf.getDevelopmentHost().getName()).get("DYLD_LIBRARY_PATH"); // NOI18N
                            runProfile = conf.getProfile().cloneProfile();
                            StringBuffer path = new StringBuffer();
                            while (iter.hasNext()) {
                                String location = FilePathAdaptor.naturalize((String)iter.next());
                                if (path.length() > 0)
                                    path.append(":"); // NOI18N
                                path.append(location);
                            }
                            if (extPath != null)
                                path.append(":" + extPath); // NOI18N
                            runProfile.getEnvironment().putenv("DYLD_LIBRARY_PATH", path.toString()); // NOI18N
                        }
                        // Make sure DISPLAY variable has been set
                        if (HostInfoProvider.getDefault().getEnv(conf.getDevelopmentHost().getName()).get("DISPLAY") == null && conf.getProfile().getEnvironment().getenv("DISPLAY") == null) { // NOI18N
                            // DISPLAY hasn't been set
                            if (runProfile == null)
                                runProfile = conf.getProfile().cloneProfile();
                            runProfile.getEnvironment().putenv("DISPLAY", ":0.0"); // NOI18N
                        }
                    }

                    MakeArtifact makeArtifact = new MakeArtifact(pd, conf);
                    String path;
                    if (targetName.equals("run")) { // NOI18N
                        // naturalize if relative
                        path = makeArtifact.getOutput();
                        //TODO: we also need remote aware IpeUtils..........
                        if (conf.getDevelopmentHost().isLocalhost()) {
                            if (!IpeUtils.isPathAbsolute(path)) {
                                // make path relative to run working directory
                                path = makeArtifact.getWorkingDirectory() + "/" + path; // NOI18N
                                path = FilePathAdaptor.naturalize(path);
                                path = IpeUtils.toRelativePath(conf.getProfile().getRunDirectory(), path);
                                path = FilePathAdaptor.naturalize(path);
                            }
                        }
                    } else {
                        // Always absolute
                        path = IpeUtils.toAbsolutePath(conf.getBaseDir(), makeArtifact.getOutput());
                    }
                    ProjectActionEvent projectActionEvent = new ProjectActionEvent(
                            project,
                            actionEvent,
                            getActionName(projectName, targetName, conf),
                            path,
                            conf,
                            runProfile,
                            false);
                    actionEvents.add(projectActionEvent);
                    RunDialogPanel.addElementToExecutablePicklist(path);
                } else {
                    assert false;
                }
            } else if (targetName.equals("run-single") || targetName.equals("debug-single")) { // NOI18N
                // FIXUP: not sure this is used...
                if (conf.isMakefileConfiguration()) {
                    Iterator it = context.lookup(new Lookup.Template(DataObject.class)).allInstances().iterator();
                    DataObject d = (DataObject)it.next();
                    String path = FileUtil.toFile(d.getPrimaryFile()).getPath();
                    ProjectActionEvent projectActionEvent = new ProjectActionEvent(
                            project,
                            actionEvent,
                            getActionName(projectName, "run", conf), // NOI18N
                            path,
                            conf,
                            null,
                            false);
                    actionEvents.add(projectActionEvent);
                    RunDialogPanel.addElementToExecutablePicklist(path);
                } else {
                    assert false;
                }
            } else if (targetName.equals("build")) { // NOI18N
                if (conf.isCompileConfiguration() && !validateProject(conf)) {
                    break;
                }
                if (validateBuildSystem(pd, conf, validated)) {
                    MakeArtifact makeArtifact = new MakeArtifact(pd, conf);
                    String buildCommand = makeArtifact.getBuildCommand(getMakeCommand(pd, conf), "");
                    String args = "";
                    int index = buildCommand.indexOf(' ');
                    if (index > 0) {
                        args = buildCommand.substring(index+1);
                        buildCommand = buildCommand.substring(0, index);
                    }
                    RunProfile profile = new RunProfile(makeArtifact.getWorkingDirectory(), conf.getPlatform().getValue());
                    profile.setArgs(args);
                    ProjectActionEvent projectActionEvent = new ProjectActionEvent(
                            project,
                            actionEvent,
                            getActionName(projectName, targetName, conf),
                            buildCommand,
                            conf,
                            profile,
                            true);
                    actionEvents.add(projectActionEvent);
                }
                else {
                    return; // Stop here
                }
                validated = true;
            } else if (targetName.equals("build-package")) { // NOI18N
                if (!validatePackaging(conf)) {
                    actionEvents.clear();
                    break;
                }
                String buildCommand;
                String args;
                if (conf.getPlatform().getValue() == Platform.PLATFORM_WINDOWS) {
                    buildCommand = "cmd.exe"; // NOI18N
                    args = "/c sh "; // NOI18N
                } else {
                    buildCommand = "bash"; // NOI18N
                    args = "";
                }
                if (conf.getPackagingConfiguration().getVerbose().getValue()) {
                    args += " -x "; // NOI18N
                }
                args += "nbproject/Package-" + conf.getName() + ".bash"; // NOI18N
                RunProfile profile = new RunProfile(conf.getBaseDir(), conf.getPlatform().getValue());
                profile.setArgs(args);
                ProjectActionEvent projectActionEvent = new ProjectActionEvent(
                        project,
                        actionEvent,
                        getActionName(projectName, targetName, conf),
                        buildCommand,
                        conf,
                        profile,
                        true);
                actionEvents.add(projectActionEvent);
            } else if (targetName.equals("clean")) { // NOI18N
//                if (conf.isCompileConfiguration() && !validateProject(conf)) {
//                    break;
//                }
                if (validateBuildSystem(pd, conf, validated)) {
                    MakeArtifact makeArtifact = new MakeArtifact(pd, conf);
                    String buildCommand = makeArtifact.getCleanCommand(getMakeCommand(pd, conf), ""); // NOI18N
                    String args = ""; // NOI18N
                    int index = buildCommand.indexOf(' '); // NOI18N
                    if (index > 0) {
                        args = buildCommand.substring(index+1);
                        buildCommand = buildCommand.substring(0, index);
                    }
                    RunProfile profile = new RunProfile(makeArtifact.getWorkingDirectory(), conf.getPlatform().getValue());
                    profile.setArgs(args);
                    ProjectActionEvent projectActionEvent = new ProjectActionEvent(
                            project,
                            actionEvent,
                            getActionName(projectName, targetName, conf),
                            buildCommand,
                            conf,
                            profile,
                            true);
                    actionEvents.add(projectActionEvent);
                }
                else {
                    return; // Stop here
                }
                validated = true;
            } else if (targetName.equals("compile-single")) { // NOI18N
                if (validateBuildSystem(pd, conf, validated)) {
                    Iterator it = context.lookup(new Lookup.Template(Node.class)).allInstances().iterator();
                    while (it.hasNext()) {
                        Node node = (Node)it.next();
                        Item item = getNoteItem(node); // NOI18N
                        if (item == null)
                            return;
                        ItemConfiguration itemConfiguration = item.getItemConfiguration(conf);//ItemConfiguration)conf.getAuxObject(ItemConfiguration.getId(item.getPath()));
                        if (itemConfiguration == null)
                            return;
                        if (itemConfiguration.getExcluded().getValue())
                            return;
                            if (itemConfiguration.getTool() == Tool.CustomTool && !itemConfiguration.getCustomToolConfiguration().getModified())
                                return;
                            MakeArtifact makeArtifact = new MakeArtifact(pd, conf);
                            String outputFile = null;
                            if (itemConfiguration.getTool() == Tool.CCompiler) {
                                CCompilerConfiguration cCompilerConfiguration = itemConfiguration.getCCompilerConfiguration();
                                outputFile = cCompilerConfiguration.getOutputFile(item, conf, true);
                            } else if (itemConfiguration.getTool() == Tool.CCCompiler) {
                                CCCompilerConfiguration ccCompilerConfiguration = itemConfiguration.getCCCompilerConfiguration();
                                outputFile = ccCompilerConfiguration.getOutputFile(item, conf, true);
                            } else if (itemConfiguration.getTool() == Tool.FortranCompiler) {
                                FortranCompilerConfiguration fortranCompilerConfiguration = itemConfiguration.getFortranCompilerConfiguration();
                                outputFile = fortranCompilerConfiguration.getOutputFile(item, conf, true);
                            } else if (itemConfiguration.getTool() == Tool.CustomTool) {
                                CustomToolConfiguration customToolConfiguration = itemConfiguration.getCustomToolConfiguration();
                                outputFile = customToolConfiguration.getOutputs().getValue();
                            }
                            outputFile = conf.expandMacros(outputFile);
                            // Clean command
                            String commandLine;
                            String args;
                            if (conf.getPlatform().getValue() == Platform.PLATFORM_WINDOWS) {
                                commandLine = "cmd.exe"; // NOI18N
                                args = "/c rm -rf " + outputFile; // NOI18N
                            } else {
                                commandLine = "rm"; // NOI18N
                                args = "-rf " + outputFile; // NOI18N
                            }
                            RunProfile profile = new RunProfile(makeArtifact.getWorkingDirectory(), conf.getPlatform().getValue());
                            profile.setArgs(args);
                            ProjectActionEvent projectActionEvent = new ProjectActionEvent(
                                    project,
                                    ProjectActionEvent.CLEAN,
                                    getActionName(projectName, "clean", conf), // NOI18N
                                    commandLine,
                                    conf,
                                    profile,
                                    true);
                            actionEvents.add(projectActionEvent);
                            // Build commandLine
                            commandLine = getMakeCommand(pd, conf) + " -f nbproject" + '/' + "Makefile-" + conf.getName() + ".mk " + outputFile; // Unix path // NOI18N
                            args = ""; // NOI18N
                            int index = commandLine.indexOf(' '); // NOI18N
                            if (index > 0) {
                                args = commandLine.substring(index+1);
                                commandLine = commandLine.substring(0, index);
                            }
                            // Add the build commandLine
                            profile = new RunProfile(makeArtifact.getWorkingDirectory(), conf.getPlatform().getValue());
                            profile.setArgs(args);
                            projectActionEvent = new ProjectActionEvent(
                                    project,
                                    actionEvent,
                                    getActionName(projectName, targetName, conf),
                                    commandLine,
                                    conf,
                                    profile,
                                    true);
                            actionEvents.add(projectActionEvent);
                    }
                }
                else {
                    return; // Stop here
                }
                validated = true;
            } else if (targetName.equals("custom-action")) { // NOI18N
                String exe = ""; // NOI18N
                if (conf.isMakefileConfiguration()) {
                    exe = conf.getMakefileConfiguration().getOutput().getValue();
                } else if (conf.isApplicationConfiguration()) {
                    exe = conf.getLinkerConfiguration().getOutputValue();
                }
                exe = conf.expandMacros(exe);
                // Always absolute
                if (exe.length() > 0)
                    exe = IpeUtils.toAbsolutePath(conf.getBaseDir(), exe);
                ProjectActionEvent projectActionEvent = new ProjectActionEvent(
                        project,
                        actionEvent,
                        getActionName(projectName, targetName, conf),
                        exe,
                        conf,
                        null,
                        true);
                actionEvents.add(projectActionEvent);
            }
        }
    }

    private static String getActionName(String projectName, String targetName, MakeConfiguration conf) {
        StringBuilder actionName = new StringBuilder(projectName);
        actionName.append(" (").append(targetName); // NOI18N
        if (!conf.getDevelopmentHost().isLocalhost()) {
            actionName.append(" - ").append( conf.getDevelopmentHost().getName() ); // NOI18N
        }
        actionName.append(")"); // NOI18N
        return actionName.toString();
    }


    private boolean validateProject(MakeConfiguration conf) {
        boolean ret = false;

        if (getProjectDescriptor().getProjectItems().length == 0) {
            ret = false;
        } else {
            for (int i = 0; i < getProjectDescriptor().getProjectItems().length; i++) {
                Item item = getProjectDescriptor().getProjectItems()[i];
                ItemConfiguration itemConfiguration = item.getItemConfiguration(conf);
                if (!itemConfiguration.getExcluded().getValue() &&
                        (itemConfiguration.getTool() !=  Tool.CustomTool || itemConfiguration.getCustomToolConfiguration().getCommandLine().getValue().length() > 0)) {
                    ret = true;
                    break;
                }
            }
        }

        if (!ret)
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(getString("ERR_EMPTY_PROJECT"), NotifyDescriptor.ERROR_MESSAGE));

        return ret;
    }

    /**
     * @return array of targets or null to stop execution; can return empty array
     */
    String[] getTargetNames(String command, Lookup context) throws IllegalArgumentException {
        String[] targetNames = new String[0];
        if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            targetNames = commands.get(command);
        } else if (command.equals(COMMAND_RUN) ||
                command.equals(COMMAND_DEBUG) ||
                command.equals(COMMAND_DEBUG_STEP_INTO) ||
                command.equals(COMMAND_DEBUG_LOAD_ONLY) ||
                command.equals(COMMAND_CUSTOM_ACTION)) {
            ConfigurationDescriptor pd = getProjectDescriptor();
            MakeConfiguration conf = (MakeConfiguration)pd.getConfs().getActive();
            RunProfile profile = (RunProfile) conf.getAuxObject(RunProfile.PROFILE_ID);
            if (profile == null) // See IZ 89349
                return null;
            if (profile.getBuildFirst())
                targetNames = commands.get(command);
            else
                targetNames = commandsNoBuild.get(command);
            if (targetNames == null) {
                throw new IllegalArgumentException(command);
            }
        } else if (command.equals(COMMAND_RUN_SINGLE) || command.equals(COMMAND_DEBUG_SINGLE)) {
            targetNames = commands.get(command);
            if (targetNames == null) {
                throw new IllegalArgumentException(command);
            }
        } else {
            targetNames = commands.get(command);
            if (targetNames == null) {
                throw new IllegalArgumentException(command);
            }
        }
        return targetNames;
    }


    public boolean isActionEnabled( String command, Lookup context ) {
        if (!isProjectDescriptorLoaded()) {
            return false;
        }
        if (!(getProjectDescriptor().getConfs().getActive() instanceof MakeConfiguration)) {
            return false;
        }
        MakeConfiguration conf = (MakeConfiguration)getProjectDescriptor().getConfs().getActive();
        if (command.equals(COMMAND_CLEAN)) {
            return true;
        } else if (command.equals(COMMAND_BUILD)) {
            return true;
        } else if (command.equals(COMMAND_BUILD_PACKAGE)) {
            return true;
        } else if (command.equals(COMMAND_BATCH_BUILD)) {
            return true;
        } else if (command.equals(COMMAND_REBUILD)) {
            return true;
        } else if (command.equals(COMMAND_RUN)) {
            return !conf.isLibraryConfiguration();
        } else if (command.equals(COMMAND_DEBUG)) {
            return hasDebugger() && !conf.isLibraryConfiguration();
        } else if (command.equals(COMMAND_DEBUG_STEP_INTO)) {
            return hasDebugger() && !conf.isLibraryConfiguration();
        } else if (command.equals(COMMAND_DEBUG_LOAD_ONLY)) {
            return hasDebugger() && !conf.isLibraryConfiguration();
        } else if (command.equals(COMMAND_COMPILE_SINGLE)) {
            boolean enabled = true;
            Iterator it = context.lookup(new Lookup.Template(Node.class)).allInstances().iterator();
            while (it.hasNext()) {
                Node node = (Node)it.next();
                Item item = getNoteItem(node);
                if (item == null)
                    return false;
                ItemConfiguration itemConfiguration = item.getItemConfiguration(conf);//ItemConfiguration)conf.getAuxObject(ItemConfiguration.getId(item.getPath()));
                if (itemConfiguration == null)
                    return false;
                if (itemConfiguration.getExcluded().getValue())
                    return false;
                if (itemConfiguration.getTool() == Tool.CustomTool && !itemConfiguration.getCustomToolConfiguration().getModified())
                    return false;
                if (conf.isMakefileConfiguration())
                    return false;
            }
            return enabled;
        } else if (command.equals(COMMAND_DELETE) ||
                command.equals(COMMAND_COPY) ||
                command.equals(COMMAND_MOVE) ||
                command.equals(COMMAND_RENAME)) {
            return true;
        } else if (command.equals(COMMAND_RUN_SINGLE)) {
            Node node = context.lookup(Node.class);
            return (node != null) && (node.getCookie(ShellExecSupport.class) != null);
        } else {
            return false;
        }
    }

    private Item getNoteItem(Node node) {
        Item item = (Item) node.getValue("Item"); // NOI18N
        if (item == null) {
            // try to find Item in associated data object if any
            try {
                DataObject dao = node.getCookie(DataObject.class);
                if (dao != null) {
                    File file = FileUtil.toFile(dao.getPrimaryFile());
                    item = getProjectDescriptor().findItemByFile(file);
                }
            } catch (NullPointerException ex) {
                // not found item
            }
        }
        return item;
    }

    private static boolean hasDebugger() {
        return DefaultProjectActionHandler.getInstance().getCustomDebugActionHandlerProvider() != null;
    }

    private static String getMakeCommand(MakeConfigurationDescriptor pd, MakeConfiguration conf) {
        String cmd = null;
        CompilerSet cs = conf.getCompilerSet().getCompilerSet();
        if (cs != null) {
            cmd = cs.getTool(Tool.MakeTool).getPath();
        } else {
            assert false;
            cmd = "make"; // NOI18N
        }
        //cmd = cmd + " " + MakeOptions.getInstance().getMakeOptions(); // NOI18N
        return cmd;
    }

    public boolean validateBuildSystem(MakeConfigurationDescriptor pd, MakeConfiguration conf, boolean validated) {
        CompilerSet2Configuration csconf = conf.getCompilerSet();
        String hkey = conf.getDevelopmentHost().getName();
        ArrayList<String> errs = new ArrayList<String>();
        CompilerSet cs;
        String csname;
        File file;
        ServerList serverList = Lookup.getDefault().lookup(ServerList.class);
        boolean cRequired = conf.hasCFiles(pd);
        boolean cppRequired = conf.hasCPPFiles(pd);
        boolean fRequired = CppSettings.getDefault().isFortranEnabled() && conf.hasFortranFiles(pd);
        boolean runBTA = false;

        if (validated) {
            return lastValidation;
        }

        if (!conf.getDevelopmentHost().isLocalhost()) {
            assert serverList != null;
            ServerRecord record = serverList.get(hkey);
            assert record != null;
            record.validate(false);
            if (!record.isOnline()) {
                lastValidation = false;
                runBTA = true;
            }
            // TODO: all validation below works, but it may be more efficient to make a verifying script
        }

        boolean unknownCompilerSet = false;
        if (csconf.getFlavor() != null && csconf.getFlavor().equals("Unknown")) { // NOI18N
            // Confiiguration was created with unknown tool set. Use the now default one.
            unknownCompilerSet = true;
            csname = csconf.getOption();
            cs = CompilerSetManager.getDefault(hkey).getCompilerSet(csname);
            if (cs == null) {
                cs = CompilerSetManager.getDefault(hkey).getDefaultCompilerSet();
            }
            runBTA = true;
        } else if (csconf.isValid()) {
            csname = csconf.getOption();
            cs = CompilerSetManager.getDefault(hkey).getCompilerSet(csname);
        } else {
            csname = csconf.getOldName();
            CompilerFlavor flavor = null;
            if (csconf.getFlavor() != null) {
                flavor = CompilerFlavor.toFlavor(csconf.getFlavor(), conf.getPlatformInfo().getPlatform());
            }
            if (flavor == null) {
                flavor = CompilerFlavor.getUnknown(conf.getPlatformInfo().getPlatform());
            }
            cs = CompilerSet.getCustomCompilerSet("", flavor, csconf.getOldName());
            CompilerSetManager.getDefault(hkey).add(cs);
            csconf.setValid();
        }

        Tool cTool = cs.getTool(Tool.CCompiler);
        Tool cppTool = cs.getTool(Tool.CCCompiler);
        Tool fTool = cs.getTool(Tool.FortranCompiler);
        Tool makeTool = cs.getTool(Tool.MakeTool);

        // Check for a valid make program
        if (conf.getDevelopmentHost().isLocalhost()) {
            file = new File(makeTool.getPath());
            if ((!file.exists() && Path.findCommand(makeTool.getPath()) == null) || !ToolsPanel.supportedMake(file.getPath())) {
                runBTA = true;
            }
        } else {
            if(serverList != null && !unknownCompilerSet) {
                if (!serverList.isValidExecutable(hkey, makeTool.getPath())) {
                    runBTA=true;
                }
            }
        }

        // Check compilers

        PlatformInfo pi = conf.getPlatformInfo();

        if (cRequired && !exists(cTool.getPath(), pi)) {
            errs.add(NbBundle.getMessage(MakeActionProvider.class, "ERR_MissingCCompiler", csname, cTool.getDisplayName())); // NOI18N
            runBTA = true;
        }
        if (cppRequired && !exists(cppTool.getPath(), pi)) {
            errs.add(NbBundle.getMessage(MakeActionProvider.class, "ERR_MissingCppCompiler", csname, cppTool.getDisplayName())); // NOI18N
            runBTA = true;
        }
        if (fRequired && !exists(fTool.getPath(), pi)) {
            errs.add(NbBundle.getMessage(MakeActionProvider.class, "ERR_MissingFortranCompiler", csname, fTool.getDisplayName())); // NOI18N
            runBTA = true;
        }

        if (conf.getDevelopmentHost().isLocalhost() && Boolean.getBoolean("netbeans.cnd.always_show_bta")) { // NOI18N
            runBTA = true;
        }

        if (runBTA) {
            if (conf.getDevelopmentHost().isLocalhost()) {
                BuildToolsAction bt = SystemAction.get(BuildToolsAction.class);
                bt.setTitle(NbBundle.getMessage(BuildToolsAction.class, "LBL_ResolveMissingTools_Title")); // NOI18N
                ToolsPanelModel model = new LocalToolsPanelModel();
                model.setSelectedDevelopmentHost(hkey); // only localhost until BTA becomes more functional for remote sets
                model.setEnableDevelopmentHostChange(false);
                model.setCompilerSetName(null); // means don't change
                model.setSelectedCompilerSetName(csname);
                model.setMakeRequired(true);
                model.setGdbRequired(false);
                model.setCRequired(cRequired);
                model.setCppRequired(cppRequired);
                model.setFortranRequired(fRequired);
                model.setShowRequiredBuildTools(true);
                model.setShowRequiredDebugTools(false);
                model.SetEnableRequiredCompilerCB(conf.isMakefileConfiguration());
                if (bt.initBuildTools(model, errs) && pd.okToChange()) {
                    String name = model.getSelectedCompilerSetName();
                    CppSettings.getDefault().setCompilerSetName(name);
                    conf.getCRequired().setValue(model.isCRequired());
                    conf.getCppRequired().setValue(model.isCppRequired());
                    conf.getFortranRequired().setValue(model.isFortranRequired());
                    conf.getCompilerSet().setValue(name);
                    pd.setModified();
                    pd.save();
                    lastValidation = true;
                } else {
                    lastValidation = false;
                }
            } else {
                // User can't change anything in BTA for remote host yet,
                // so showing above dialog will only confuse him
                NotifyDescriptor nd = new NotifyDescriptor.Message(
                        NbBundle.getMessage(MakeActionProvider.class, "ERR_INVALID_COMPILER_SET", csname, conf.getDevelopmentHost().getName()));
                DialogDisplayer.getDefault().notify(nd);
                lastValidation = false;
            }
        } else {
            lastValidation = true;
        }
        return lastValidation;
    }

    private boolean validatePackaging(MakeConfiguration conf) {
        String errormsg = null;

        if (conf.getPackagingConfiguration().getFiles().getValue().size() == 0) {
            errormsg = getString("ERR_EMPTY_PACKAGE");
        }
        
        if (PackagerManager.getDefault().getPackager(conf.getPackagingConfiguration().getType().getValue()) == null) {
            errormsg = errormsg = NbBundle.getMessage(MakeActionProvider.class, "ERR_MISSING_TOOL4", conf.getPackagingConfiguration().getType().getValue()); // NOI18N
        }

        if (errormsg == null) {
            String tool = conf.getPackagingConfiguration().getToolValue();
            if (conf.getDevelopmentHost().isLocalhost()) {
                if (!IpeUtils.isPathAbsolute(tool) && Path.findCommand(tool) == null) {
                    errormsg = NbBundle.getMessage(MakeActionProvider.class, "ERR_MISSING_TOOL1", tool); // NOI18N
                }
                else if (IpeUtils.isPathAbsolute(tool) && !(new File(tool).exists())) {
                    errormsg = NbBundle.getMessage(MakeActionProvider.class, "ERR_MISSING_TOOL2", tool); // NOI18N
                }
            } else {
                String hkey = conf.getDevelopmentHost().getName();
                ServerList serverList = Lookup.getDefault().lookup(ServerList.class);
                if(serverList != null) {
                    if (!serverList.isValidExecutable(hkey, tool)) {
                        errormsg = NbBundle.getMessage(MakeActionProvider.class, "ERR_MISSING_TOOL3", tool, hkey); // NOI18N
                    }
                }
            }
        }

        if (errormsg != null) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
            if (conf.getPackagingConfiguration().getFiles().getValue().size() == 0) {
                MakeCustomizerProvider makeCustomizerProvider = project.getLookup().lookup(MakeCustomizerProvider.class);
                if (makeCustomizerProvider != null) {
                    makeCustomizerProvider.showCustomizer("Packaging"); // NOI18N
                }
            }
            return false;
        }

        return true;
    }

    private static boolean exists(String path, PlatformInfo pi) {
        return pi.fileExists(path) || pi.findCommand(path)!=null;
    }

    // Private methods -----------------------------------------------------
    /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(MakeActionProvider.class);
        }
        return bundle.getString(s);
    }
}
