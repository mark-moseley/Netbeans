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

package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.actions.ShellRunAction;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.execution.ShellExecSupport;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.cnd.makeproject.MakeProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.SourceFolderInfo;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.QmakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension;
import org.netbeans.modules.cnd.makeproject.ui.utils.PathPanel;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Make project.
 */
public class NewMakeProjectWizardIterator implements WizardDescriptor.InstantiatingIterator {
    private static final long serialVersionUID = 1L;
    static final boolean USE_SIMPLE_IMPORT_PROJECT = CndUtils.getBoolean("cnd.makeproject.simple.import", true); // NOI18N

    public static final String APPLICATION_PROJECT_NAME = "Application"; // NOI18N
    public static final String DYNAMICLIBRARY_PROJECT_NAME = "DynamicLibrary";  // NOI18N
    public static final String STATICLIBRARY_PROJECT_NAME = "StaticLibrary"; // NOI18N
    public static final String MAKEFILEPROJECT_PROJECT_NAME = "MakefileProject"; // NOI18N
    public static final String QTAPPLICATION_PROJECT_NAME = "QtApplication"; // NOI18N
    public static final String QTDYNAMICLIBRARY_PROJECT_NAME = "QtDynamicLibrary"; // NOI18N
    public static final String QTSTATICLIBRARY_PROJECT_NAME = "QtStaticLibrary"; // NOI18N

    static final String PROP_NAME_INDEX = "nameIndex"; // NOI18N
    
    // Wizard types
    public static final int TYPE_MAKEFILE = 0;
    public static final int TYPE_APPLICATION = 1;
    public static final int TYPE_DYNAMIC_LIB = 2;
    public static final int TYPE_STATIC_LIB = 3;
    public static final int TYPE_QT_APPLICATION = 4;
    public static final int TYPE_QT_DYNAMIC_LIB = 5;
    public static final int TYPE_QT_STATIC_LIB = 6;

    private int wizardtype;
    private String name;
    private String wizardTitle;
    private String wizardACSD;
    
    private NewMakeProjectWizardIterator(int wizardtype, String name, String wizardTitle, String wizardACSD) {
        this.wizardtype = wizardtype;
        this.name = name;
        this.wizardTitle = wizardTitle;
        this.wizardACSD = wizardACSD;
    }
    
    public static NewMakeProjectWizardIterator newApplication() {
        String name = APPLICATION_PROJECT_NAME; //getString("NativeNewApplicationName"); // NOI18N
        String wizardTitle = getString("Templates/Project/Native/newApplication.xml"); // NOI18N
        String wizardACSD = getString("NativeNewLibraryACSD"); // NOI18N
        return new NewMakeProjectWizardIterator(TYPE_APPLICATION, name, wizardTitle, wizardACSD);
    }
    
    public static NewMakeProjectWizardIterator newDynamicLibrary() {
        String name = DYNAMICLIBRARY_PROJECT_NAME; //getString("NativeNewDynamicLibraryName"); // NOI18N
        String wizardTitle = getString("Templates/Project/Native/newDynamicLibrary.xml"); // NOI18N
        String wizardACSD = getString("NativeNewDynamicLibraryACSD"); // NOI18N
        return new NewMakeProjectWizardIterator(TYPE_DYNAMIC_LIB, name, wizardTitle, wizardACSD);
    }
    
    public static NewMakeProjectWizardIterator newStaticLibrary() {
        String name = STATICLIBRARY_PROJECT_NAME; //getString("NativeNewStaticLibraryName");
        String wizardTitle = getString("Templates/Project/Native/newStaticLibrary.xml");
        String wizardACSD = getString("NativeNewStaticLibraryACSD");
        return new NewMakeProjectWizardIterator(TYPE_STATIC_LIB, name, wizardTitle, wizardACSD);
    }
    
    public static NewMakeProjectWizardIterator newQtApplication() {
        String name = QTAPPLICATION_PROJECT_NAME;
        String wizardTitle = getString("Templates/Project/Native/newQtApplication.xml");
        String wizardACSD = getString("NativeNewQtApplicationACSD");
        return new NewMakeProjectWizardIterator(TYPE_QT_APPLICATION, name, wizardTitle, wizardACSD);
    }

    public static NewMakeProjectWizardIterator newQtDynamicLibrary() {
        String name = QTDYNAMICLIBRARY_PROJECT_NAME;
        String wizardTitle = getString("Templates/Project/Native/newQtDynamicLibrary.xml");
        String wizardACSD = getString("NativeNewQtDynamicLibraryACSD");
        return new NewMakeProjectWizardIterator(TYPE_QT_DYNAMIC_LIB, name, wizardTitle, wizardACSD);
    }

    public static NewMakeProjectWizardIterator newQtStaticLibrary() {
        String name = QTSTATICLIBRARY_PROJECT_NAME;
        String wizardTitle = getString("Templates/Project/Native/newQtStaticLibrary.xml");
        String wizardACSD = getString("NativeNewQtStaticLibraryACSD");
        return new NewMakeProjectWizardIterator(TYPE_QT_STATIC_LIB, name, wizardTitle, wizardACSD);
    }

    public static NewMakeProjectWizardIterator makefile() {
        String name = MAKEFILEPROJECT_PROJECT_NAME; //getString("NativeMakefileName"); // NOI18N
        String wizardTitle = getString("Templates/Project/Native/makefile.xml"); // NOI18N
        String wizardACSD = getString("NativeMakefileNameACSD"); // NOI18N
        return new NewMakeProjectWizardIterator(TYPE_MAKEFILE, name, wizardTitle, wizardACSD);
    }
    
    private WizardDescriptor.Panel[] createPanels(String name) {
        if (wizardtype == TYPE_APPLICATION || wizardtype == TYPE_DYNAMIC_LIB
                || wizardtype == TYPE_STATIC_LIB || wizardtype == TYPE_QT_APPLICATION
                || wizardtype == TYPE_QT_DYNAMIC_LIB || wizardtype == TYPE_QT_STATIC_LIB) {
            return new WizardDescriptor.Panel[] {
                new PanelConfigureProject(name, wizardtype, wizardTitle, wizardACSD, true)
            };
        } else if (wizardtype == TYPE_MAKEFILE) {
            if (USE_SIMPLE_IMPORT_PROJECT) {
                return new WizardDescriptor.Panel[] {
                    new SelectModeDescriptorPanel(),
                    new MakefileOrConfigureDescriptorPanel(),
                    new BuildActionsDescriptorPanel(),
                    new SourceFoldersDescriptorPanel(),
                    new ParserConfigurationDescriptorPanel(),
                    new PanelConfigureProject(name, wizardtype, wizardTitle, wizardACSD, false),
                };
            } else {
                return new WizardDescriptor.Panel[] {
                    new MakefileOrConfigureDescriptorPanel(),
                    new BuildActionsDescriptorPanel(),
                    new SourceFoldersDescriptorPanel(),
                    new ParserConfigurationDescriptorPanel(),
                    new PanelConfigureProject(name, wizardtype, wizardTitle, wizardACSD, false),
                };
            }
        }
        return null; // FIXUP
    }
    
    private String[] createSteps(WizardDescriptor.Panel[] panels) {
        String[] steps = new String[panels.length];
        for (int i = 0; i < panels.length; i++) {
            if (panels[i] instanceof Name) {
                steps[i] = ((Name)panels[i]).getName();
            } else {
                steps[i] = panels[i].getComponent().getName();
            }
        }
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }
        return steps;
    }

    public Set<FileObject> instantiate() throws IOException {
        Set<FileObject> resultSet = new HashSet<FileObject>();
        File dirF = (File)wiz.getProperty("projdir"); // NOI18N
        if (dirF != null) {
            dirF = FileUtil.normalizeFile(dirF);
        }
        String projectName = (String)wiz.getProperty("name"); // NOI18N
        String makefileName = (String)wiz.getProperty("makefilename"); // NOI18N
        if (isSimple()){
            IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
            if (extension != null) {
                SelectModeDescriptorPanel importPanel = (SelectModeDescriptorPanel)simplePanels[0];
                resultSet.addAll(extension.createProject(new SelectModeDescriptorPanel.WizardDescriptorAdapter(importPanel.getWizardStorage())));
            }
        } else if (wizardtype == TYPE_MAKEFILE) { // thp
            if (USE_SIMPLE_IMPORT_PROJECT) {
                IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
                if (extension != null) {
                    resultSet.addAll(extension.createProject(wiz));
                }
            } else {
                MakeConfiguration extConf = new MakeConfiguration(dirF.getPath(), "Default", MakeConfiguration.TYPE_MAKEFILE); // NOI18N
                String workingDir = (String)wiz.getProperty("buildCommandWorkingDirTextField"); // NOI18N
                String workingDirRel;
                if (PathPanel.getMode() == PathPanel.REL_OR_ABS) {
                    workingDirRel = IpeUtils.toAbsoluteOrRelativePath(dirF.getPath(), FilePathAdaptor.naturalize(workingDir));
                } else if (PathPanel.getMode() == PathPanel.REL) {
                    workingDirRel = IpeUtils.toRelativePath(dirF.getPath(), FilePathAdaptor.naturalize(workingDir));
                } else {
                    workingDirRel = IpeUtils.toAbsolutePath(dirF.getPath(), FilePathAdaptor.naturalize(workingDir));
                }
                workingDirRel = FilePathAdaptor.normalize(workingDirRel);
                extConf.getMakefileConfiguration().getBuildCommandWorkingDir().setValue(workingDirRel);
                extConf.getMakefileConfiguration().getBuildCommand().setValue((String)wiz.getProperty("buildCommandTextField")); // NOI18N
                extConf.getMakefileConfiguration().getCleanCommand().setValue((String)wiz.getProperty("cleanCommandTextField")); // NOI18N
                // Build result
                String buildResult = (String)wiz.getProperty("outputTextField"); // NOI18N
                if (buildResult != null && buildResult.length() > 0) {
                    if (PathPanel.getMode() == PathPanel.REL_OR_ABS) {
                        buildResult = IpeUtils.toAbsoluteOrRelativePath(dirF.getPath(), FilePathAdaptor.naturalize(buildResult));
                    } else if (PathPanel.getMode() == PathPanel.REL) {
                        buildResult = IpeUtils.toRelativePath(dirF.getPath(), FilePathAdaptor.naturalize(buildResult));
                    } else {
                        buildResult = IpeUtils.toAbsolutePath(dirF.getPath(), FilePathAdaptor.naturalize(buildResult));
                    }
                    buildResult = FilePathAdaptor.normalize(buildResult);
                    extConf.getMakefileConfiguration().getOutput().setValue(buildResult);
                }
                // Include directories
                String includeDirectories = (String)wiz.getProperty("includeTextField"); // NOI18N
                if (includeDirectories != null && includeDirectories.length() > 0) {
                    StringTokenizer tokenizer = new StringTokenizer(includeDirectories, ";"); // NOI18N
                    Vector<String> includeDirectoriesVector = new Vector<String>();
                    while (tokenizer.hasMoreTokens()) {
                        String includeDirectory = tokenizer.nextToken();
                        includeDirectory = IpeUtils.toRelativePath(dirF.getPath(), FilePathAdaptor.naturalize(includeDirectory));
                        includeDirectory = FilePathAdaptor.normalize(includeDirectory);
                        includeDirectoriesVector.add(includeDirectory);
                    }
                    extConf.getCCompilerConfiguration().getIncludeDirectories().setValue(includeDirectoriesVector);
                    extConf.getCCCompilerConfiguration().getIncludeDirectories().setValue(includeDirectoriesVector);
                }
                // Macros
                String macros = (String)wiz.getProperty("macroTextField"); // NOI18N
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
                String makefilePath = (String)wiz.getProperty("makefileName"); // NOI18N
                File makefileFile = new File(makefilePath);
                if (makefilePath != null && makefilePath.length() > 0) {
                    if (PathPanel.getMode() == PathPanel.REL_OR_ABS) {
                        makefilePath = IpeUtils.toAbsoluteOrRelativePath(dirF.getPath(), FilePathAdaptor.naturalize(makefilePath));
                    } else if (PathPanel.getMode() == PathPanel.REL) {
                        makefilePath = IpeUtils.toRelativePath(dirF.getPath(), FilePathAdaptor.naturalize(makefilePath));
                    } else {
                        makefilePath = IpeUtils.toAbsolutePath(dirF.getPath(), FilePathAdaptor.naturalize(makefilePath));
                    }
                    makefilePath = FilePathAdaptor.normalize(makefilePath);
                    importantItems.add(makefilePath);
                }
                String configurePath = (String)wiz.getProperty("configureName"); // NOI18N
                if (configurePath != null && configurePath.length() > 0) {
                    File configureFile = new File(configurePath);
                    if (PathPanel.getMode() == PathPanel.REL_OR_ABS) {
                        configurePath = IpeUtils.toAbsoluteOrRelativePath(dirF.getPath(), FilePathAdaptor.naturalize(configurePath));
                    } else if (PathPanel.getMode() == PathPanel.REL) {
                        configurePath = IpeUtils.toRelativePath(dirF.getPath(), FilePathAdaptor.naturalize(configurePath));
                    } else {
                        configurePath = IpeUtils.toAbsolutePath(dirF.getPath(), FilePathAdaptor.naturalize(configurePath));
                    }
                    configurePath = FilePathAdaptor.normalize(configurePath);
                    importantItems.add(configurePath);

                    try {
                        FileObject configureFileObject = FileUtil.toFileObject(configureFile);
                        DataObject dObj = DataObject.find(configureFileObject);
                        Node node = dObj.getNodeDelegate();

                        // Add arguments to configure script?
                        String configureArguments = (String)wiz.getProperty("configureArguments"); // NOI18N
                        if (configureArguments != null) {
                            ShellExecSupport ses = node.getCookie(ShellExecSupport.class);
                            // Keep user arguments as is in args[0]
                            ses.setArguments(new String[] {configureArguments});
                        }

                        // Possibly run the configure script
                        String runConfigure = (String)wiz.getProperty("runConfigure"); // NOI18N
                        if (runConfigure != null && runConfigure.equals("true")) { // NOI18N
                            // If no makefile, create empty one so it shows up in Interesting Files
                            if (!makefileFile.exists()) {
                                makefileFile.createNewFile();
                            }

                            ShellRunAction.performAction(node);
                        }
                    }
                    catch (DataObjectNotFoundException e) {
                    }
                }
                Iterator<String> importantItemsIterator = importantItems.iterator();
                if (!importantItemsIterator.hasNext()) {
                    importantItemsIterator = null;
                }
                @SuppressWarnings("unchecked")
                Iterator<SourceFolderInfo> it = (Iterator)wiz.getProperty("sourceFolders"); // NOI18N
                final MakeProject makeProject = MakeProjectGenerator.createProject(dirF, projectName, makefileName, new MakeConfiguration[] {extConf}, it, importantItemsIterator);
                FileObject dir = FileUtil.toFileObject(dirF);
                resultSet.add(dir);
                final IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
                if (extension != null && makeProject != null) {
                    final Project p = ProjectManager.getDefault().findProject(dir);
                    final Map<String,Object> map = extension.clone(wiz);
                    makeProject.addOpenedTask(new Runnable(){
                        public void run() {
                            // Discovery require a fully completed project
                            // Make sure that descriptor was stored and readed
                            ConfigurationDescriptorProvider provider = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
                            provider.getConfigurationDescriptor(true);
                            if (extension.canApply(map, p)){
                                try {
                                    extension.apply(map, p);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    });
                }
            }
        } else if (wizardtype == TYPE_APPLICATION || wizardtype == TYPE_DYNAMIC_LIB
                || wizardtype == TYPE_STATIC_LIB || wizardtype == TYPE_QT_APPLICATION
                || wizardtype == TYPE_QT_DYNAMIC_LIB || wizardtype == TYPE_QT_STATIC_LIB) {
            int conftype = -1;
            if (wizardtype == TYPE_APPLICATION) {
                conftype = MakeConfiguration.TYPE_APPLICATION;
            } else if (wizardtype == TYPE_DYNAMIC_LIB) {
                conftype = MakeConfiguration.TYPE_DYNAMIC_LIB;
            } else if (wizardtype == TYPE_STATIC_LIB) {
                conftype = MakeConfiguration.TYPE_STATIC_LIB;
            } else if (wizardtype == TYPE_QT_APPLICATION) {
                conftype = MakeConfiguration.TYPE_QT_APPLICATION;
            } else if (wizardtype == TYPE_QT_DYNAMIC_LIB) {
                conftype = MakeConfiguration.TYPE_QT_DYNAMIC_LIB;
            } else if (wizardtype == TYPE_QT_STATIC_LIB) {
                conftype = MakeConfiguration.TYPE_QT_STATIC_LIB;
            }
            MakeConfiguration debug = new MakeConfiguration(dirF.getPath(), "Debug", conftype); // NOI18N
            debug.getCCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_DEBUG);
            debug.getCCCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_DEBUG);
            debug.getFortranCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_DEBUG);
            debug.getQmakeConfiguration().getBuildMode().setValue(QmakeConfiguration.DEBUG_MODE);
            MakeConfiguration release = new MakeConfiguration(dirF.getPath(), "Release", conftype); // NOI18N
            release.getCCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_RELEASE);
            release.getCCCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_RELEASE);
            release.getFortranCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_RELEASE);
            release.getQmakeConfiguration().getBuildMode().setValue(QmakeConfiguration.RELEASE_MODE);
            MakeConfiguration[] confs = new MakeConfiguration[] {debug, release};
            MakeProjectGenerator.createProject(dirF, projectName, makefileName, confs, null, null);
            FileObject dir = FileUtil.toFileObject(dirF);
            resultSet.add(dir);
        }
        return resultSet;
    }
    
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor.Panel[] simplePanels;
    private transient WizardDescriptor wiz;
    
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels(name.replaceAll(" ", "")); // NOI18N
        // Make sure list of steps is accurate.
        String[] steps = createSteps(panels);
        if (wizardtype == TYPE_MAKEFILE && USE_SIMPLE_IMPORT_PROJECT) {
            simplePanels = new WizardDescriptor.Panel[]{
                panels[0]
                //,new ImportProjectDescriptorPanel()
            };
            steps = createSteps(simplePanels);
            String[] advanced = new String[] {steps[0], "..."}; // NOI18N
            Component c = panels[0].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, advanced);
            }
        }
    }

    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty("projdir",null); // NOI18N
        this.wiz.putProperty("name",null); // NOI18N
        this.wiz.putProperty("mainClass",null); // NOI18N
        if (wizardtype == TYPE_MAKEFILE) {
            this.wiz.putProperty("sourceRoot",null); // NOI18N
        }
        this.wiz = null;
        panels = null;
        simplePanels = null;
    }
    
    public String name() {
        return MessageFormat.format(NbBundle.getMessage(NewMakeProjectWizardIterator.class,"LAB_IteratorName"), // NOI18N
                new Object[] {Integer.valueOf(index + 1), Integer.valueOf(panels.length) });
    }
    
    private boolean isSimple(){
        return wizardtype == TYPE_MAKEFILE && USE_SIMPLE_IMPORT_PROJECT && Boolean.TRUE.equals(wiz.getProperty("simpleMode")); // NOI18N
    }

    public boolean hasNext() {
        if (isSimple()) {
            return index < simplePanels.length - 1;
        } else {
            return index < panels.length - 1;
        }
    }
    public boolean hasPrevious() {
        return index > 0;
    }
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    public WizardDescriptor.Panel current() {
        if (isSimple()) {
            return simplePanels[index];
        } else {
            return panels[index];
        }
    }
    
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    interface Name {
        public String getName();
    }
    
    /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(NewMakeProjectWizardIterator.class);
        }
        return bundle.getString(s);
    }
}
