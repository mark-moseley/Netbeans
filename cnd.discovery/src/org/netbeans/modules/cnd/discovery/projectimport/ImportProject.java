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
package org.netbeans.modules.cnd.discovery.projectimport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.actions.MakeAction;
import org.netbeans.modules.cnd.actions.ShellRunAction;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.api.utils.AllSourceFileFilter;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.wizard.ConsolidationStrategyPanel;
import org.netbeans.modules.cnd.discovery.wizard.DiscoveryWizardDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.SelectConfigurationPanel;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.api.FileConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.api.ProjectConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.bridge.DiscoveryProjectGenerator;
import org.netbeans.modules.cnd.discovery.wizard.bridge.ProjectBridge;
import org.netbeans.modules.cnd.execution.ShellExecSupport;
import org.netbeans.modules.cnd.makeproject.api.ProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.SourceFolderInfo;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension;
import org.netbeans.modules.cnd.makeproject.ui.utils.PathPanel;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander Simon
 */
public class ImportProject implements PropertyChangeListener {
    private static boolean TRACE = Boolean.getBoolean("cnd.discovery.trace.projectimport"); // NOI18N
    private Logger logger = Logger.getLogger("org.netbeans.modules.cnd.discovery.projectimport.ImportProject"); // NOI18N

    private File nativeProjectFolder;
    private File projectFolder;
    private String projectName;
    private String makefileName = "Makefile";  // NOI18N
    private String makefilePath;
    private String configurePath;
    private String configureArguments;
    private boolean runConfigure = false;
    private boolean manualCA = false;
    private boolean setAsMain;
    private String workingDir;
    private String buildCommand = "$(MAKE) -f Makefile";  // NOI18N
    private String cleanCommand = "$(MAKE) -f Makefile clean";  // NOI18N
    private String buildResult = "";  // NOI18N
    private Project makeProject;
    private boolean runMake;
    private String includeDirectories = "";
    private String macros = "";
    private String consolidationStrategy = ConsolidationStrategyPanel.FILE_LEVEL;
    private Iterator<SourceFolderInfo> sources;
    private File configureFile;
    private File makefileFile;
    private Map<Step,State> importResult = new HashMap<Step,State>();

    public ImportProject(WizardDescriptor wizard) {
        if (TRACE) {logger.setLevel(Level.ALL);}
        if (Boolean.TRUE.equals(wizard.getProperty("simpleMode"))){
            simpleSetup(wizard);
        } else {
            customSetup(wizard);
        }
    }

    private void simpleSetup(WizardDescriptor wizard) {
        String path = (String) wizard.getProperty("path");  // NOI18N
        projectFolder = new File(path);
        nativeProjectFolder = projectFolder;
        projectName = projectFolder.getName();
        makefileName = "Makefile-"+projectName+".mk"; // NOI18N
        workingDir = path;
        makefilePath = (String) wizard.getProperty("makefileName");  // NOI18N
        if (makefilePath == null) {
            configurePath = (String) wizard.getProperty("configureName");  // NOI18N
            configureArguments = (String) wizard.getProperty("realFlags");  // NOI18N
            runConfigure = true;
            // the best guess
            File file = new File(path + "/Makefile"); // NOI18N
            makefilePath = file.getAbsolutePath();
        }
        runMake = Boolean.TRUE.equals(wizard.getProperty("buildProject"));  // NOI18N
        setAsMain = Boolean.TRUE.equals(wizard.getProperty("setMain"));  // NOI18N

        List<SourceFolderInfo> list =new ArrayList<SourceFolderInfo>();
        list.add(new SourceFolderInfo() {
            public File getFile() {
                return projectFolder;
            }
            public String getFolderName() {
                return projectFolder.getName();
            }
            public boolean isAddSubfoldersSelected() {
                return true;
            }

            public FileFilter getFileFilter() {
                return AllSourceFileFilter.getInstance();
           }
        });
        sources = list.iterator();
    }

    private void customSetup(WizardDescriptor wizard) {
        String path = (String) wizard.getProperty("simpleModeFolder");  // NOI18N
        nativeProjectFolder = new File(path);
        projectFolder = (File)wizard.getProperty("projdir"); // NOI18N
        projectName = (String)wizard.getProperty("name"); // NOI18N
        makefileName = (String)wizard.getProperty("makefilename"); // NOI18N
        workingDir = (String) wizard.getProperty("buildCommandWorkingDirTextField"); // NOI18N
        buildCommand = (String) wizard.getProperty("buildCommandTextField"); // NOI18N
        cleanCommand = (String) wizard.getProperty("cleanCommandTextField"); // NOI18N
        buildResult = (String) wizard.getProperty("outputTextField"); // NOI18N
        includeDirectories = (String) wizard.getProperty("includeTextField"); // NOI18N
        macros = (String) wizard.getProperty("macroTextField"); // NOI18N
        makefilePath = (String) wizard.getProperty("makefileName"); // NOI18N
        configurePath = (String) wizard.getProperty("configureName"); // NOI18N
        configureArguments = (String) wizard.getProperty("configureArguments"); // NOI18N
        runConfigure = "true".equals(wizard.getProperty("runConfigure")); // NOI18N
        consolidationStrategy = (String) wizard.getProperty("consolidationLevel"); // NOI18N
        @SuppressWarnings("unchecked")
        Iterator<SourceFolderInfo> it = (Iterator) wizard.getProperty("sourceFolders"); // NOI18N
        sources = it;
        runConfigure = "true".equals(wizard.getProperty("runConfigure")); // NOI18N
        if (runConfigure) {
            runMake = true;
        } else {
            runMake = "true".equals(wizard.getProperty("makeProject")); // NOI18N
        }
        manualCA = "true".equals(wizard.getProperty("manualCA")); // NOI18N
        setAsMain = Boolean.TRUE.equals(wizard.getProperty("setAsMain"));  // NOI18N
    }

    public Set<FileObject> create() throws IOException {
        Set<FileObject> resultSet = new HashSet<FileObject>();
        projectFolder = FileUtil.normalizeFile(projectFolder);
        MakeConfiguration extConf = new MakeConfiguration(projectFolder.getPath(), "Default", MakeConfiguration.TYPE_MAKEFILE); // NOI18N
        String workingDirRel;
        if (PathPanel.getMode() == PathPanel.REL_OR_ABS) {
            workingDirRel = IpeUtils.toAbsoluteOrRelativePath(projectFolder.getPath(), FilePathAdaptor.naturalize(workingDir));
        } else if (PathPanel.getMode() == PathPanel.REL) {
            workingDirRel = IpeUtils.toRelativePath(projectFolder.getPath(), FilePathAdaptor.naturalize(workingDir));
        } else {
            workingDirRel = IpeUtils.toAbsolutePath(projectFolder.getPath(), FilePathAdaptor.naturalize(workingDir));
        }
        workingDirRel = FilePathAdaptor.normalize(workingDirRel);
        extConf.getMakefileConfiguration().getBuildCommandWorkingDir().setValue(workingDirRel);
        extConf.getMakefileConfiguration().getBuildCommand().setValue(buildCommand);
        extConf.getMakefileConfiguration().getCleanCommand().setValue(cleanCommand);
        // Build result
        if (buildResult != null && buildResult.length() > 0) {
            if (PathPanel.getMode() == PathPanel.REL_OR_ABS) {
                buildResult = IpeUtils.toAbsoluteOrRelativePath(projectFolder.getPath(), FilePathAdaptor.naturalize(buildResult));
            } else if (PathPanel.getMode() == PathPanel.REL) {
                buildResult = IpeUtils.toRelativePath(projectFolder.getPath(), FilePathAdaptor.naturalize(buildResult));
            } else {
                buildResult = IpeUtils.toAbsolutePath(projectFolder.getPath(), FilePathAdaptor.naturalize(buildResult));
            }
            buildResult = FilePathAdaptor.normalize(buildResult);
            extConf.getMakefileConfiguration().getOutput().setValue(buildResult);
        }
        // Include directories
        if (includeDirectories != null && includeDirectories.length() > 0) {
            StringTokenizer tokenizer = new StringTokenizer(includeDirectories, ";"); // NOI18N
            List<String> includeDirectoriesVector = new ArrayList<String>();
            while (tokenizer.hasMoreTokens()) {
                String includeDirectory = tokenizer.nextToken();
                includeDirectory = IpeUtils.toRelativePath(projectFolder.getPath(), FilePathAdaptor.naturalize(includeDirectory));
                includeDirectory = FilePathAdaptor.normalize(includeDirectory);
                includeDirectoriesVector.add(includeDirectory);
            }
            extConf.getCCompilerConfiguration().getIncludeDirectories().setValue(includeDirectoriesVector);
            extConf.getCCCompilerConfiguration().getIncludeDirectories().setValue(new ArrayList<String>(includeDirectoriesVector));
        }
        // Macros
        if (macros != null && macros.length() > 0) {
            StringTokenizer tokenizer = new StringTokenizer(macros, "; "); // NOI18N
            ArrayList<String> list = new ArrayList<String>();
            while (tokenizer.hasMoreTokens()) {
                list.add(tokenizer.nextToken());
            }
            // FIXUP
            extConf.getCCompilerConfiguration().getPreprocessorConfiguration().getValue().addAll(list);
            extConf.getCCCompilerConfiguration().getPreprocessorConfiguration().getValue().addAll(list);
        }
        // Add makefile and configure script to important files
        ArrayList<String> importantItems = new ArrayList<String>();
        if (makefilePath != null && makefilePath.length() > 0) {
            makefileFile = FileUtil.normalizeFile(new File(makefilePath));
            if (PathPanel.getMode() == PathPanel.REL_OR_ABS) {
                makefilePath = IpeUtils.toAbsoluteOrRelativePath(projectFolder.getPath(), FilePathAdaptor.naturalize(makefilePath));
            } else if (PathPanel.getMode() == PathPanel.REL) {
                makefilePath = IpeUtils.toRelativePath(projectFolder.getPath(), FilePathAdaptor.naturalize(makefilePath));
            } else {
                makefilePath = IpeUtils.toAbsolutePath(projectFolder.getPath(), FilePathAdaptor.naturalize(makefilePath));
            }
            makefilePath = FilePathAdaptor.normalize(makefilePath);
            importantItems.add(makefilePath);
        }
        if (configurePath != null && configurePath.length() > 0) {
            configureFile = FileUtil.normalizeFile(new File(configurePath));
            if (PathPanel.getMode() == PathPanel.REL_OR_ABS) {
                configurePath = IpeUtils.toAbsoluteOrRelativePath(projectFolder.getPath(), FilePathAdaptor.naturalize(configurePath));
            } else if (PathPanel.getMode() == PathPanel.REL) {
                configurePath = IpeUtils.toRelativePath(projectFolder.getPath(), FilePathAdaptor.naturalize(configurePath));
            } else {
                configurePath = IpeUtils.toAbsolutePath(projectFolder.getPath(), FilePathAdaptor.naturalize(configurePath));
            }
            configurePath = FilePathAdaptor.normalize(configurePath);
            importantItems.add(configurePath);
        }
        Iterator<String> importantItemsIterator = importantItems.iterator();
        if (!importantItemsIterator.hasNext()) {
            importantItemsIterator = null;
        }
        makeProject = ProjectGenerator.createProject(projectFolder, projectName, makefileName, new MakeConfiguration[]{extConf}, sources, importantItemsIterator);
        FileObject dir = FileUtil.toFileObject(projectFolder);
        importResult.put(Step.Project, State.Successful);
        switchModel(false);
        resultSet.add(dir);
        OpenProjects.getDefault().addPropertyChangeListener(this);
        return resultSet;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(OpenProjects.PROPERTY_OPEN_PROJECTS)) {
            OpenProjects.getDefault().removePropertyChangeListener(this);
            //if (setAsMain) {
            //    OpenProjects.getDefault().setMainProject(makeProject);
            //}
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    doWork();
                }
            });
        }
    }


    private void doWork(){
        //OpenProjects.getDefault().open(new Project[]{makeProject}, false);
        //if (setAsMain) {
        //    OpenProjects.getDefault().setMainProject(makeProject);
        //}
        ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        pdp.getConfigurationDescriptor();
        if (pdp.gotDescriptor()) {
            if (configurePath != null && configurePath.length() > 0) {
                postConfigure();
            } else {
                if (runMake) {
                    makeProject(true);
                } else {
                    RequestProcessor.getDefault().post(new Runnable(){
                        public void run() {
                            discovery(0, null);
                        }
                    });
                }
            }
        }
    }

//    private void parseConfigureLog(File configureLog){
//        try {
//            BufferedReader reader = new BufferedReader(new FileReader(configureLog));
//            while (true) {
//                String line;
//                line = reader.readLine();
//                if (line == null) {
//                    break;
//                }
//            }
//            reader.close();
//        } catch (FileNotFoundException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//    }

    private File createTempFile(String prefix) {
        try {
            File file = File.createTempFile(prefix, ".log"); // NOI18N
            file.deleteOnExit();
            return file;
        } catch (IOException ex) {
            return null;
        }
    }

    private void postConfigure() {
        try {
            FileObject configureFileObject = FileUtil.toFileObject(configureFile);
            DataObject dObj = DataObject.find(configureFileObject);
            Node node = dObj.getNodeDelegate();
            // Add arguments to configure script?
            if (configureArguments != null) {
                ShellExecSupport ses = node.getCookie(ShellExecSupport.class);
                try {
                    // Keep user arguments as is in args[0]
                    ses.setArguments(new String[]{configureArguments});
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            // Possibly run the configure script
            if (runConfigure) {
                // If no makefile, create empty one so it shows up in Interesting Files
                //if (!makefileFile.exists()) {
                //    makefileFile.createNewFile();
                //}
                //final File configureLog = createTempFile("configure");
                ExecutionListener listener = new ExecutionListener() {
                    public void executionStarted() {
                    }
                    public void executionFinished(int rc) {
                        if (rc == 0) {
                            importResult.put(Step.Configure, State.Successful);
                        } else {
                            importResult.put(Step.Configure, State.Fail);
                        }
                        if (runMake && rc == 0) {
                            //parseConfigureLog(configureLog);
                            makeProject(false);
                        } else {
                            switchModel(true);
                            postModelDiscovery(true);
                        }
                    }
                };
                if (TRACE) {logger.log(Level.INFO, "#configure " + configureArguments); } // NOI18N
                ShellRunAction.performAction(node, listener, null, makeProject); //, new BufferedWriter(new FileWriter(configureLog)));
            }
        } catch (DataObjectNotFoundException e) {
        }
    }

    private void makeProject(boolean doClean){
        if (makefileFile != null && makefileFile.exists()) {
            FileObject makeFileObject = FileUtil.toFileObject(makefileFile);
            DataObject dObj;
            try {
                dObj = DataObject.find(makeFileObject);
                Node node = dObj.getNodeDelegate();
                if (doClean) {
                    postClean(node);
                } else {
                    postMake(node);
                }
            } catch (DataObjectNotFoundException ex) {
            }
        } else {
            String path = nativeProjectFolder.getAbsolutePath();
            File file = new File(path + "/Makefile"); // NOI18N
            if (file.exists() && file.isFile() && file.canRead()) {
                makefilePath = file.getAbsolutePath();
            } else {
                file = new File(path + "/makefile"); // NOI18N
                if (file.exists() && file.isFile() && file.canRead()) {
                    makefilePath = file.getAbsolutePath();
                }
            }
            switchModel(true);
            postModelDiscovery(true);
        }
    }

    private void postClean(final Node node){
        ExecutionListener listener = new ExecutionListener() {
            public void executionStarted() {
            }
            public void executionFinished(int rc) {
                if (rc == 0) {
                    importResult.put(Step.MakeClean, State.Successful);
                } else {
                    importResult.put(Step.MakeClean, State.Fail);
                }
                postMake(node);
            }
        };
        if (TRACE) {logger.log(Level.INFO, "#make clean");} // NOI18N
        MakeAction.execute(node, "clean", listener, null, makeProject); // NOI18N
    }

    private void postMake(Node node){
        final File makeLog = createTempFile("make"); // NOI18N
        ExecutionListener listener = new ExecutionListener() {
            public void executionStarted() {
            }
            public void executionFinished(int rc) {
                if (rc == 0) {
                    importResult.put(Step.Make, State.Successful);
                } else {
                    importResult.put(Step.Make, State.Fail);
                }
                discovery(rc, makeLog);
            }
        };
        Writer outputListener = null;
        if (makeLog != null){
            try {
                outputListener = new BufferedWriter(new FileWriter(makeLog));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (TRACE) {logger.log(Level.INFO, "#make > "+makeLog.getAbsolutePath());} // NOI18N
        MakeAction.execute(node, "", listener, outputListener, makeProject); // NOI18N
    }

    private DiscoveryProvider getProvider(String id){
       Lookup.Result<DiscoveryProvider> providers = Lookup.getDefault().lookup(new Lookup.Template<DiscoveryProvider>(DiscoveryProvider.class));
        for(DiscoveryProvider provider : providers.allInstances()){
            provider.clean();
            if (id.equals(provider.getID())) {
                return provider;
            }
        }
        return null;
    }

    private void waitConfigurationDescriptor() {
        // Discovery require a fully completed project
        // Make sure that descriptor was stored and readed
        ConfigurationDescriptorProvider provider = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        provider.getConfigurationDescriptor(true);
    }


    private void discovery(int rc, File makeLog) {
        waitConfigurationDescriptor();
        boolean done = false;
        if (!manualCA) {
            final IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
            if (rc == 0) {
                if (extension != null) {
                    final Map<String, Object> map = new HashMap<String, Object>();
                    map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, nativeProjectFolder.getAbsolutePath());
                    map.put(DiscoveryWizardDescriptor.CONSOLIDATION_STRATEGY, consolidationStrategy);
                    if (extension.canApply(map, makeProject)) {
                        DiscoveryProvider provider = (DiscoveryProvider) map.get(DiscoveryWizardDescriptor.PROVIDER);
                        if (provider != null && "make-log".equals(provider.getID())){ // NOI18N
                            if (TRACE) {logger.log(Level.INFO, "#start discovery by log file "+ provider.getProperty("make-log-file").getValue());} // NOI18N
                        } else {
                            if (TRACE) {logger.log(Level.INFO, "#start discovery by object files");} // NOI18N
                        }
                        try {
                            done = true;
                            extension.apply(map, makeProject);
                            if (provider != null && "make-log".equals(provider.getID())){ // NOI18N
                                importResult.put(Step.DiscoveryLog, State.Successful);
                            } else {
                                importResult.put(Step.DiscoveryDwarf, State.Successful);
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        if (TRACE) {logger.log(Level.INFO, "#no dwarf information found in object files");} // NOI18N
                    }
                }
            }
            if (!done && makeLog != null){
                if (extension != null) {
                    final Map<String, Object> map = new HashMap<String, Object>();
                    map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, nativeProjectFolder.getAbsolutePath());
                    map.put(DiscoveryWizardDescriptor.LOG_FILE, makeLog.getAbsolutePath());
                    map.put(DiscoveryWizardDescriptor.CONSOLIDATION_STRATEGY, consolidationStrategy);
                    if (extension.canApply(map, makeProject)) {
                        if (TRACE) {logger.log(Level.INFO, "#start discovery by log file "+makeLog.getAbsolutePath());} // NOI18N
                        try {
                            done = true;
                            extension.apply(map, makeProject);
                            importResult.put(Step.DiscoveryLog, State.Successful);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        if (TRACE) {logger.log(Level.INFO, "#discovery cannot be done by log file "+makeLog.getAbsolutePath());} // NOI18N
                    }
                }
            } else if (done && makeLog != null){
                if (extension != null) {
                    final Map<String, Object> map = new HashMap<String, Object>();
                    map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, nativeProjectFolder.getAbsolutePath());
                    map.put(DiscoveryWizardDescriptor.LOG_FILE, makeLog.getAbsolutePath());
                    map.put(DiscoveryWizardDescriptor.CONSOLIDATION_STRATEGY, consolidationStrategy);
                    if (extension.canApply(map, makeProject)) {
                        if (TRACE) {logger.log(Level.INFO, "#start fix macros by log file "+makeLog.getAbsolutePath());} // NOI18N
                        @SuppressWarnings("unchecked")
                        List<ProjectConfiguration> confs = (List) map.get(DiscoveryWizardDescriptor.CONFIGURATIONS);
                        fixMacros(confs);
                        importResult.put(Step.FixMacros, State.Successful);
                    } else {
                        if (TRACE) {logger.log(Level.INFO, "#fix macros cannot be done by log file "+makeLog.getAbsolutePath());} // NOI18N
                    }
                }
            }
        }
        switchModel(true);
        if (!done){
            postModelDiscovery(true);
        } else {
            postModelDiscovery(false);
        }
    }

    private void fixMacros(List<ProjectConfiguration> confs) {
        NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
        for (ProjectConfiguration conf : confs) {
            List<FileConfiguration> files = conf.getFiles();
            for (FileConfiguration fileConf : files) {
                if (fileConf.getUserMacros().size() > 0) {
                    NativeFileItem item = np.findFileItem(new File(fileConf.getFilePath()));
                    if (item instanceof Item) {
                        if (TRACE) {logger.log(Level.FINE, "#fix macros for file "+fileConf.getFilePath());} // NOI18N
                        ProjectBridge.fixFileMacros(fileConf.getUserMacros(), (Item) item);
                    }
                }
            }
        }
        saveMakeConfigurationDescriptor();
    }

    private void saveMakeConfigurationDescriptor(){
        ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        final MakeConfigurationDescriptor makeConfigurationDescriptor = (MakeConfigurationDescriptor)pdp.getConfigurationDescriptor();
        makeConfigurationDescriptor.setModified();
        makeConfigurationDescriptor.save();
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                makeConfigurationDescriptor.checkForChangedItems(makeProject, null, null);
                if (TRACE) {logger.log(Level.INFO, "#save configuration descriptor");} // NOI18N
           }
        });
    }

    private void postModelDiscovery(final boolean isFull) {
        CsmModel model = CsmModelAccessor.getModel();
        if (model instanceof ModelImpl && makeProject != null) {
            final NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            final CsmProject p = model.getProject(np);
            if (p == null) {
                if (TRACE) {logger.log(Level.INFO, "#discovery cannot be done by model");} // NOI18N
            }
            CsmProgressListener listener = new CsmProgressAdapter() {

                @Override
                public void projectParsingFinished(CsmProject project) {
                    if (project.equals(p)) {
                        ImportProject.listeners.remove(p);
                        CsmListeners.getDefault().removeProgressListener(this);
                        if (TRACE) {logger.log(Level.INFO, "#start discovery by model");} // NOI18N
                        if (isFull) {
                            modelDiscovery();
                        } else {
                            fixExcludedHeaderFiles();
                        }
                        showFollwUp(np);
                    }
                }
            };
            CsmListeners.getDefault().addProgressListener(listener);
            ImportProject.listeners.put(p, listener);
        }
    }

    private void showFollwUp(final NativeProject project){
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                FollowUp.showFollowUp(ImportProject.this, project);
            }
        });
    }

    Map<Step,State> getImportResult(){
        return importResult;
    }

    // remove wrong "exclude from project" flags
    private void fixExcludedHeaderFiles(){
        CsmModel model = CsmModelAccessor.getModel();
        if (model instanceof ModelImpl && makeProject != null) {
            NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            final CsmProject p = model.getProject(np);
            if (p != null && np != null) {
                if (TRACE) {logger.log(Level.INFO, "#start fixing excluded header files by model");} // NOI18N
                for(CsmFile file : p.getAllFiles()){
                    if (file instanceof FileImpl){
                        FileImpl impl = (FileImpl)file;
                        NativeFileItem item = impl.getNativeFileItem();
                        if (item == null) {
                            item = np.findFileItem(impl.getFile());
                        }
                        if (item != null && np.equals(item.getNativeProject()) && item.isExcluded()) {
                            if (item instanceof Item){
                                if (TRACE) {logger.log(Level.FINE, "#fix excluded header for file "+impl.getAbsolutePath());} // NOI18N
                                ProjectBridge.setExclude((Item)item, false);
                            }
                        }
                    }
                }
                saveMakeConfigurationDescriptor();
                importResult.put(Step.FixExcluded, State.Successful);
            }
        }
    }

    private void modelDiscovery() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, nativeProjectFolder.getAbsolutePath());
        map.put(DiscoveryWizardDescriptor.INVOKE_PROVIDER, Boolean.TRUE);
        map.put(DiscoveryWizardDescriptor.CONSOLIDATION_STRATEGY, consolidationStrategy);
        boolean does = false;
        if (!manualCA) {
            IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
            if (extension != null) {
                if (extension.canApply(map, makeProject)) {
                    if (TRACE) {logger.log(Level.INFO, "#start discovery by object files");} // NOI18N
                    try {
                        extension.apply(map, makeProject);
                        importResult.put(Step.DiscoveryDwarf, State.Successful);
                        does = true;
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    if (TRACE) {logger.log(Level.INFO, "#no dwarf information found in object files");} // NOI18N
                }
            }
        }
        if (!does) {
            if (TRACE) {logger.log(Level.INFO, "#start discovery by model");} // NOI18N
            map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, nativeProjectFolder.getAbsolutePath());
            DiscoveryProvider provider = getProvider("model-folder"); // NOI18N
            provider.getProperty("folder").setValue(nativeProjectFolder.getAbsolutePath()); // NOI18N
            map.put(DiscoveryWizardDescriptor.PROVIDER, provider);
            map.put(DiscoveryWizardDescriptor.INVOKE_PROVIDER, Boolean.TRUE);
            DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(map);
            descriptor.setProject(makeProject);
            SelectConfigurationPanel.buildModel(descriptor);
            try {
                DiscoveryProjectGenerator generator = new DiscoveryProjectGenerator(descriptor);
                generator.makeProject();
                importResult.put(Step.DiscoveryModel, State.Successful);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private void switchModel(boolean state) {
        CsmModel model = CsmModelAccessor.getModel();
        if (model instanceof ModelImpl && makeProject != null) {
            NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            if (state) {
                if (TRACE) {logger.log(Level.INFO, "#enable model for "+np.getProjectDisplayName());} // NOI18N
                ((ModelImpl) model).enableProject(np);
            } else {
                if (TRACE) {logger.log(Level.INFO, "#disable model for "+np.getProjectDisplayName());} // NOI18N
                ((ModelImpl) model).disableProject(np);
            }
        }
    }

    private static final Map<CsmProject, CsmProgressListener> listeners = new WeakHashMap<CsmProject, CsmProgressListener>();

    static enum State {Successful, Fail, Skiped}
    static enum Step {Project, Configure, MakeClean, Make, DiscoveryDwarf, DiscoveryLog, FixMacros, DiscoveryModel, FixExcluded}
}
