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

package org.netbeans.modules.apisupport.project.ui;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JSeparator;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleTypeProvider;
import org.netbeans.modules.apisupport.project.NbModuleTypeProvider.NbModuleType;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.actions.FindAction;
import org.openide.actions.ToolsAction;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.FolderLookup;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

public final class ModuleActions implements ActionProvider {
    
    static Action[] getProjectActions(NbModuleProject project) {
        AntProjectHelper h = project.getHelper();
        List/*<Action>*/ actions = new ArrayList();
        actions.add(CommonProjectActions.newFileAction());
        actions.add(null);
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, NbBundle.getMessage(ModuleActions.class, "ACTION_build"), null));
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD, NbBundle.getMessage(ModuleActions.class, "ACTION_rebuild"), null));
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, NbBundle.getMessage(ModuleActions.class, "ACTION_clean"), null));
        actions.add(null);
        boolean isNetBeansOrg = ((NbModuleTypeProvider) project.getLookup().lookup(NbModuleTypeProvider.class)).getModuleType() == NbModuleTypeProvider.NETBEANS_ORG;
        if (isNetBeansOrg) {
            String path = project.getPathWithinNetBeansOrg();
            actions.add(createMasterAction(project, new String[] {"init", "all-" + path}, NbBundle.getMessage(ModuleActions.class, "ACTION_build_with_deps")));
            actions.add(createMasterAction(project, new String[] {"init", "all-" + path, "tryme"}, NbBundle.getMessage(ModuleActions.class, "ACTION_build_with_deps_tryme")));
        } else {
            actions.add(createSimpleAction(project, new String[] {"run"}, NbBundle.getMessage(ModuleActions.class, "ACTION_run")));
        }
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG, NbBundle.getMessage(ModuleActions.class, "ACTION_debug"), null));
        actions.add(null);
        boolean testactions = false;
        if (project.supportsUnitTests()) {
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_TEST, NbBundle.getMessage(ModuleActions.class, "ACTION_test"), null));
            if (findTestBuildXml(project) != null) { // hide for external modules w/o XTest infrastructure
                Properties props = new Properties();
                props.setProperty("xtest.testtype", "unit"); // NOI18N
                actions.add(createTestAction(project, new String[] {"cleanresults", "runtests", "show-results-nb"}, props, NbBundle.getMessage(ModuleActions.class, "ACTION_xtest")));
                actions.add(createTestAction(project, new String[] {"cleanresults", "coverage", "show-coverage-results-nb"}, props, NbBundle.getMessage(ModuleActions.class, "ACTION_coverage")));
            }
            testactions = true;
        }
        if (project.getFunctionalTestSourceDirectory() != null) {
            Properties props = new Properties();
            props.setProperty("xtest.testtype", "qa-functional"); // NOI18N
            actions.add(createTestAction(project, new String[] {"buildtests"}, props, NbBundle.getMessage(ModuleActions.class, "ACTION_build_func_tests")));
            props = new Properties();
            props.setProperty("xtest.testtype", "qa-functional"); // NOI18N
            actions.add(createTestAction(project, new String[] {"cleanresults", "runtests", "show-results-nb"}, props, NbBundle.getMessage(ModuleActions.class, "ACTION_xtest_functional")));
            testactions = true;
        }
        if (project.getPerformanceTestSourceDirectory() != null) {
            Properties props = new Properties();
            props.setProperty("xtest.testtype", "qa-performance"); // NOI18N
            actions.add(createTestAction(project, new String[] {"buildtests"}, props, NbBundle.getMessage(ModuleActions.class, "ACTION_build_perf_tests")));
            props = new Properties();
            props.setProperty("xtest.testtype", "qa-performance"); // NOI18N
            actions.add(createTestAction(project, new String[] {"cleanresults", "runtests", "show-results-nb"}, props, NbBundle.getMessage(ModuleActions.class, "ACTION_xtest_perf")));
            testactions = true;
        }
        if (testactions) {
            actions.add(null);
        }
        actions.add(ProjectSensitiveActions.projectCommandAction(JavaProjectConstants.COMMAND_JAVADOC, NbBundle.getMessage(ModuleActions.class, "ACTION_javadoc"), null));
        actions.add(createArchAction(project));
        actions.add(null);
        if (isNetBeansOrg) {
            actions.add(createCheckBundleAction(project, NbBundle.getMessage(ModuleActions.class, "ACTION_unused_bundle_keys")));
            actions.add(null);
        }
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN, NbBundle.getMessage(ModuleActions.class, "ACTION_reload"), null));
        actions.add(createReloadInIDEAction(project, new String[] {"reload-in-ide"}, NbBundle.getMessage(ModuleActions.class, "ACTION_reload_in_ide")));
        actions.add(createSimpleAction(project, new String[] {"nbm"}, NbBundle.getMessage(ModuleActions.class, "ACTION_nbm")));
        actions.add(null);
        actions.add(CommonProjectActions.setAsMainProjectAction());
        actions.add(CommonProjectActions.openSubprojectsAction());
        actions.add(CommonProjectActions.closeProjectAction());
        actions.add(null);
        actions.add(SystemAction.get(FindAction.class));
        /*
        actions.add(null);
        actions.add(SystemAction.get(DeleteAction.class));
         */
        
        // Honor #57874 contract:
        try {
            FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
            FileObject fo = sfs.findResource("Projects/Actions"); // NOI18N
            if (fo != null) {
                DataObject dobj = DataObject.find(fo);
                FolderLookup actionRegistry = new FolderLookup((DataFolder) dobj);
                Lookup.Template query = new Lookup.Template(Object.class);
                Lookup lookup = actionRegistry.getLookup();
                Iterator it = lookup.lookup(query).allInstances().iterator();
                if (it.hasNext()) {
                    actions.add(null);
                }
                while (it.hasNext()) {
                    Object next = it.next();
                    if (next instanceof Action) {
                        actions.add(next);
                    } else if (next instanceof JSeparator) {
                        actions.add(null);
                    }
                }
            }
        } catch (DataObjectNotFoundException ex) {
            assert false : ex;
        }
        
        actions.add(null);
        actions.add(SystemAction.get(ToolsAction.class));
        actions.add(null);
        actions.add(CommonProjectActions.customizeProjectAction());
        return (Action[])actions.toArray(new Action[actions.size()]);
    }
    
    private final NbModuleProject project;
    private final Map/*<String,String>*/ globalCommands = new HashMap();
    private final String[] supportedActions;
    
    public ModuleActions(NbModuleProject project, NbModuleTypeProvider.NbModuleType moduleType) {
        this.project = project;
        boolean isNetBeansOrg = moduleType == NbModuleTypeProvider.NETBEANS_ORG;
        Set/*<String>*/ supportedActionsSet = new HashSet();
        globalCommands.put(ActionProvider.COMMAND_BUILD, new String[] {"netbeans"}); // NOI18N
        globalCommands.put(ActionProvider.COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
        globalCommands.put(ActionProvider.COMMAND_REBUILD, new String[] {"clean", "netbeans"}); // NOI18N
        globalCommands.put(ActionProvider.COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
        globalCommands.put(ActionProvider.COMMAND_RUN, new String[] {"reload"}); // NOI18N
        globalCommands.put("profile", new String[] {"profile"}); // NOI18N
        globalCommands.put(JavaProjectConstants.COMMAND_JAVADOC, new String[] {"javadoc-nb"}); // NOI18N
        if (project.supportsUnitTests()) {
            globalCommands.put(ActionProvider.COMMAND_TEST, new String[] {"test"}); // NOI18N
        }
        supportedActionsSet.addAll(globalCommands.keySet());
        supportedActionsSet.add(ActionProvider.COMMAND_COMPILE_SINGLE);
        supportedActionsSet.add(JavaProjectConstants.COMMAND_DEBUG_FIX); // #47012
        if (project.supportsUnitTests()) {
            supportedActionsSet.add(ActionProvider.COMMAND_TEST_SINGLE);
            supportedActionsSet.add(ActionProvider.COMMAND_DEBUG_TEST_SINGLE);
            supportedActionsSet.add(ActionProvider.COMMAND_RUN_SINGLE);
            supportedActionsSet.add(ActionProvider.COMMAND_DEBUG_SINGLE);
        }
        if (project.getFunctionalTestSourceDirectory() != null) {
            supportedActionsSet.add(ActionProvider.COMMAND_RUN_SINGLE);
        }
        if (project.getPerformanceTestSourceDirectory() != null) {
            supportedActionsSet.add(ActionProvider.COMMAND_RUN_SINGLE);
        }
        supportedActions = (String[])supportedActionsSet.toArray(new String[supportedActionsSet.size()]);
    }
    
    public String[] getSupportedActions() {
        return supportedActions;
    }
    
    private static FileObject findBuildXml(NbModuleProject project) {
        return project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
    }
    
    private static FileObject findTestBuildXml(NbModuleProject project) {
        return project.getProjectDirectory().getFileObject("test/build.xml"); // NOI18N
    }
    
    private static FileObject findMasterBuildXml(NbModuleProject project) {
        return project.getNbrootFileObject("nbbuild/build.xml"); // NOI18N
    }
    
    public boolean isActionEnabled(String command, Lookup context) {
        if (command.equals(COMMAND_COMPILE_SINGLE)) {
            return findBuildXml(project) != null &&
                    (findSources(context) != null || findTestSources(context, false) != null);
        } else if (command.equals(COMMAND_TEST_SINGLE)) {
            return findBuildXml(project) != null &&  findTestSourcesForSources(context) != null;
        } else if (command.equals(COMMAND_DEBUG_TEST_SINGLE)) {
            FileObject[] files =  findTestSourcesForSources(context);
            return findBuildXml(project) != null && files != null && files.length == 1;
        } else if (command.equals(COMMAND_RUN_SINGLE)) {
            FileObject[] files = findFunctionalTestSources(context);
            if (files != null && files.length == 1 && findTestBuildXml(project) != null) {
                return true;
            }
            files = findPerformanceTestSources(context);
            if (files != null && files.length == 1 && findTestBuildXml(project) != null) {
                return true;
            }
            files = findTestSources(context, false);
            return files != null && files.length == 1;
        } else if (command.equals(COMMAND_DEBUG_SINGLE)) {
            FileObject[] files = findTestSources(context, false);
            return files != null && files.length == 1;
        } else if (command.equals(JavaProjectConstants.COMMAND_DEBUG_FIX)) {
            FileObject[] files = findSources(context);
            if (files != null && files.length == 1 && findBuildXml(project) != null) {
                return true;
            }
            files = findTestSources(context, false);
            return files != null && files.length == 1 && findBuildXml(project) != null;
        } else {
            // other actions are global
            return findBuildXml(project) != null;
        }
    }
    
    private static final Pattern SRCDIRJAVA = Pattern.compile("\\.java$"); // NOI18N
    private static final String SUBST = "Test.java"; // NOI18N
    
    private FileObject[] findSources(Lookup context) {
        FileObject srcDir = project.getSourceDirectory();
        if (srcDir != null) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, srcDir, ".java", true); // NOI18N
            //System.err.println("findSources: srcDir=" + srcDir + " files=" + (files != null ? java.util.Arrays.asList(files) : null) + " context=" + context);
            return files;
        } else {
            return null;
        }
    }
    
    private FileObject[] findTestSources(Lookup context, boolean checkInSrcDir) {
        FileObject testSrcDir = project.getTestSourceDirectory();
        if (testSrcDir != null) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, testSrcDir, ".java", true); // NOI18N
            if (files != null) {
                return files;
            }
        }
        //System.err.println("fTS: testSrcDir=" + testSrcDir + " checkInSrcDir=" + checkInSrcDir + " context=" + context);
        if (checkInSrcDir && testSrcDir != null) {
            FileObject srcDir = project.getSourceDirectory();
            //System.err.println("  srcDir=" + srcDir);
            if (srcDir != null) {
                FileObject[] files = ActionUtils.findSelectedFiles(context, srcDir, ".java", true); // NOI18N
                //System.err.println("  files=" + files);
                if (files != null) {
                    FileObject[] files2 = ActionUtils.regexpMapFiles(files, srcDir, SRCDIRJAVA, testSrcDir, SUBST, true);
                    //System.err.println("  files2=" + files2);
                    if (files2 != null) {
                        return files2;
                    }
                }
            }
        }
        return null;
    }
    
    /** Find tests corresponding to selected sources.
     */
    private FileObject[] findTestSourcesForSources(Lookup context) {
        FileObject[] sourceFiles = findSources(context);
        if (sourceFiles == null) {
            return null;
        }
        FileObject testSrcDir = project.getTestSourceDirectory();
        FileObject srcDir = project.getSourceDirectory();
        return ActionUtils.regexpMapFiles(sourceFiles, srcDir, SRCDIRJAVA, testSrcDir, SUBST, true);
    }
    
    private FileObject[] findFunctionalTestSources(Lookup context) {
        FileObject srcDir = project.getFunctionalTestSourceDirectory();
        if (srcDir != null) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, srcDir, ".java", true); // NOI18N
            return files;
        } else {
            return null;
        }
    }
    
    private FileObject[] findPerformanceTestSources(Lookup context) {
        FileObject srcDir = project.getPerformanceTestSourceDirectory();
        if (srcDir != null) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, srcDir, ".java", true); // NOI18N
            return files;
        } else {
            return null;
        }
    }
    
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        Properties p;
        String[] targetNames;
        FileObject buildScript = null;
        if (command.equals(COMMAND_COMPILE_SINGLE)) {
            FileObject[] files = findSources(context);
            p = new Properties();
            if (files != null) {
                p.setProperty("javac.includes", ActionUtils.antIncludesList(files, project.getSourceDirectory())); // NOI18N
                targetNames = new String[] {"compile-single"}; // NOI18N
            } else {
                files = findTestSources(context, false);
                p.setProperty("javac.includes", ActionUtils.antIncludesList(files, project.getTestSourceDirectory())); // NOI18N
                targetNames = new String[] {"compile-test-single"}; // NOI18N
            }
        } else if (command.equals(COMMAND_TEST_SINGLE)) {
            p = new Properties();
            FileObject[] files = findTestSourcesForSources(context);
            targetNames = setupTestSingle(p, files);
        } else if (command.equals(COMMAND_DEBUG_TEST_SINGLE)) {
            p = new Properties();
            FileObject[] files = findTestSourcesForSources(context);
            targetNames = setupDebugTestSingle(p, files);
        } else if (command.equals(COMMAND_RUN_SINGLE)) {
            FileObject[] files = findFunctionalTestSources(context);
            if (files != null) {
                String path = FileUtil.getRelativePath(project.getFunctionalTestSourceDirectory(), files[0]);
                p = new Properties();
                p.setProperty("xtest.testtype", "qa-functional"); // NOI18N
                p.setProperty("classname", path.substring(0, path.length() - 5).replace('/', '.')); // NOI18N
                targetNames = new String[] {"internal-execution"}; // NOI18N
                buildScript = findTestBuildXml(project);
            } else if ((files = findPerformanceTestSources(context)) != null) {
                String path = FileUtil.getRelativePath(project.getPerformanceTestSourceDirectory(), files[0]);
                p = new Properties();
                p.setProperty("xtest.testtype", "qa-performance"); // NOI18N
                p.setProperty("classname", path.substring(0, path.length() - 5).replace('/', '.')); // NOI18N
                targetNames = new String[] {"internal-execution"}; // NOI18N
                buildScript = findTestBuildXml(project);
            }  else {
                files = findTestSources(context, false);
                p = new Properties();
                targetNames = setupTestSingle(p, files);
            }
        } else if (command.equals(COMMAND_DEBUG_SINGLE)) {
            FileObject[] files = findTestSources(context, false);
            p = new Properties();
            targetNames = setupDebugTestSingle(p, files);
        } else if (command.equals(JavaProjectConstants.COMMAND_DEBUG_FIX)) {
            FileObject[] files = findSources(context);
            String path = null;
            if (files != null) {
                path = FileUtil.getRelativePath(project.getSourceDirectory(), files[0]);
                assert path != null;
                assert path.endsWith(".java");
                targetNames = new String[] {"debug-fix-nb"}; // NOI18N
            } else {
                files = findTestSources(context, false);
                path = FileUtil.getRelativePath(project.getTestSourceDirectory(), files[0]);
                assert path != null;
                assert path.endsWith(".java");
                targetNames = new String[] {"debug-fix-test-nb"}; // NOI18N
            }
            String clazzSlash = path.substring(0, path.length() - 5);
            p = new Properties();
            p.setProperty("fix.class", clazzSlash); // NOI18N
            buildScript = findBuildXml(project);
        } else if (command.equals(JavaProjectConstants.COMMAND_JAVADOC) && !project.supportsJavadoc()) {
            promptForPublicPackagesToDocument();
            return;
        } else {
            p = null;
            targetNames = (String[])globalCommands.get(command);
            if (targetNames == null) {
                throw new IllegalArgumentException(command);
            }
        }
        if (buildScript == null) {
            buildScript = findBuildXml(project);
        }
        try {
            ActionUtils.runTarget(buildScript, targetNames, p);
        } catch (IOException e) {
            Util.err.notify(e);
        }
    }
    
    private void promptForPublicPackagesToDocument() {
        // #61372: warn the user, rather than disabling the action.
        String msg = NbBundle.getMessage(ModuleActions.class, "ERR_javadoc_disabled");
        DialogDescriptor d = new DialogDescriptor(msg, NbBundle.getMessage(ModuleActions.class, "TITLE_javadoc_disabled"));
        d.setModal(true);
        JButton configure = new JButton();
        Mnemonics.setLocalizedText(configure, NbBundle.getMessage(ModuleActions.class, "LBL_configure_pubpkg"));
        configure.setDefaultCapable(true);
        d.setOptions(new Object[] {
            configure,
                    NotifyDescriptor.CANCEL_OPTION,
        });
        d.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
        if (DialogDisplayer.getDefault().notify(d).equals(configure)) {
            CustomizerProviderImpl cpi = ((CustomizerProviderImpl) project.getLookup().lookup(CustomizerProviderImpl.class));
            cpi.showCustomizer(CustomizerProviderImpl.CATEGORY_VERSIONING, CustomizerProviderImpl.SUBCATEGORY_VERSIONING_PUBLIC_PACKAGES);
        }
    }
    
    private String[] setupTestSingle(Properties p, FileObject[] files) {
        p.setProperty("test.includes", ActionUtils.antIncludesList(files, project.getTestSourceDirectory())); // NOI18N
        return new String[] {"test-single"}; // NOI18N
    }
    
    private String[] setupDebugTestSingle(Properties p, FileObject[] files) {
        String path = FileUtil.getRelativePath(project.getTestSourceDirectory(), files[0]);
        // Convert foo/FooTest.java -> foo.FooTest
        p.setProperty("test.class", path.substring(0, path.length() - 5).replace('/', '.')); // NOI18N
        return new String[] {"debug-test-single-nb"}; // NOI18N
    }
    
    private static Action createSimpleAction(final NbModuleProject project, final String[] targetNames, String displayName) {
        return new AbstractAction(displayName) {
            public boolean isEnabled() {
                return findBuildXml(project) != null;
            }
            public void actionPerformed(ActionEvent ignore) {
                try {
                    ActionUtils.runTarget(findBuildXml(project), targetNames, null);
                } catch (IOException e) {
                    Util.err.notify(e);
                }
            }
        };
    }
    
    private static Action createMasterAction(final NbModuleProject project, final String[] targetNames, String displayName) {
        return new AbstractAction(displayName) {
            public boolean isEnabled() {
                return findMasterBuildXml(project) != null;
            }
            public void actionPerformed(ActionEvent ignore) {
                try {
                    ActionUtils.runTarget(findMasterBuildXml(project), targetNames, null);
                } catch (IOException e) {
                    Util.err.notify(e);
                }
            }
        };
    }
    
    private static Action createTestAction(final NbModuleProject project, final String[] targetNames, final Properties props, String displayName) {
        return new AbstractAction(displayName) {
            public boolean isEnabled() {
                return findTestBuildXml(project) != null;
            }
            public void actionPerformed(ActionEvent ignore) {
                try {
                    ActionUtils.runTarget(findTestBuildXml(project), targetNames, props);
                } catch (IOException e) {
                    Util.err.notify(e);
                }
            }
        };
    }
    
    private static Action createCheckBundleAction(final NbModuleProject project, String displayName) {
        return new AbstractAction(displayName) {
            public boolean isEnabled() {
                return findMonitorXml() != null && project.getPathWithinNetBeansOrg() != null;
            }
            public void actionPerformed(ActionEvent ignore) {
                Properties props = new Properties();
                props.put("modules", project.getPathWithinNetBeansOrg()); // NOI18N
                props.put("fixedmodules", ""); // NOI18N
                try {
                    ActionUtils.runTarget(findMonitorXml(), new String[] {"check-bundle-usage"}, props); // NOI18N
                } catch (IOException e) {
                    Util.err.notify(e);
                }
            }
            private FileObject findMonitorXml() {
                return project.getNbrootFileObject("nbbuild/monitor.xml"); // NOI18N
            }
        };
    }
    
    private static Action createReloadInIDEAction(final NbModuleProject project, final String[] targetNames, String displayName) {
        return new AbstractAction(displayName) {
            public boolean isEnabled() {
                if (findBuildXml(project) == null) {
                    return false;
                }
                NbModuleTypeProvider.NbModuleType type = ((NbModuleTypeProvider) project.getLookup().lookup(NbModuleTypeProvider.class)).getModuleType();
                if (type == NbModuleTypeProvider.NETBEANS_ORG) {
                    return true;
                } else if (type == NbModuleTypeProvider.STANDALONE) {
                    NbPlatform p = project.getPlatform(false);
                    return p != null && p.isDefault();
                } else {
                    assert type == NbModuleTypeProvider.SUITE_COMPONENT : type;
                    try {
                        SuiteProject suite = SuiteUtils.findSuite(project);
                        if (suite == null) {
                            return false;
                        }
                        NbPlatform p = suite.getPlatform(false);
                        if (/* #67148 */p == null || !p.isDefault()) {
                            return false;
                        }
                        return SuiteProperties.getArrayProperty(suite.getEvaluator(), SuiteProperties.DISABLED_CLUSTERS_PROPERTY).length == 0 &&
                                SuiteProperties.getArrayProperty(suite.getEvaluator(), SuiteProperties.DISABLED_MODULES_PROPERTY).length == 0;
                    } catch (IOException e) {
                        Util.err.notify(ErrorManager.INFORMATIONAL, e);
                        return false;
                    }
                }
            }
            public void actionPerformed(ActionEvent ignore) {
                if (ModuleUISettings.getDefault().getConfirmReloadInIDE()) {
                    NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                            NbBundle.getMessage(ModuleActions.class, "LBL_reload_in_ide_confirm"),
                            NbBundle.getMessage(ModuleActions.class, "LBL_reload_in_ide_confirm_title"),
                            NotifyDescriptor.OK_CANCEL_OPTION);
                    if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.OK_OPTION) {
                        return;
                    }
                    ModuleUISettings.getDefault().setConfirmReloadInIDE(false); // do not ask again
                }
                try {
                    ActionUtils.runTarget(findBuildXml(project), targetNames, null);
                } catch (IOException e) {
                    Util.err.notify(e);
                }
            }
        };
    }
    
    private static Action createArchAction(final NbModuleProject project) {
        return new AbstractAction(NbBundle.getMessage(ModuleActions.class, "ACTION_arch")) {
            public boolean isEnabled() {
                return findBuildXml(project) != null;
            }
            public void actionPerformed(ActionEvent ignore) {
                ProjectManager.mutex().writeAccess(new Mutex.Action() {
                    public Object run() {
                        String prop = "javadoc.arch"; // NOI18N
                        if (project.evaluator().getProperty(prop) == null) {
                            // User has not yet configured an arch desc. Assume we should just do it for them.
                            EditableProperties props = project.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            props.setProperty(prop, "${basedir}/arch.xml"); // NOI18N
                            project.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                            try {
                                ProjectManager.getDefault().saveProject(project);
                            } catch (IOException e) {
                                Util.err.notify(e);
                            }
                        }
                        return null;
                    }
                });
                try {
                    ActionUtils.runTarget(findBuildXml(project), new String[] {"arch-nb"}, null); // NOI18N
                } catch (IOException e) {
                    Util.err.notify(e);
                }
            }
        };
    }
    
}
