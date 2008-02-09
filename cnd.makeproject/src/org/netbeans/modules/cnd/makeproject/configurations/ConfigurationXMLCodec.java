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

package org.netbeans.modules.cnd.makeproject.configurations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import java.util.List;
import org.netbeans.modules.cnd.api.utils.CppUtils;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.configurations.ArchiverConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.CustomToolConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibrariesConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.LinkerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.api.xml.VersionException;
import org.netbeans.modules.cnd.makeproject.api.configurations.FolderConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.FortranCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.RequiredProjectsConfiguration;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platforms;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.Attributes;

/**
 * was: DescriptorSaxParser
 */

class ConfigurationXMLCodec extends CommonConfigurationXMLCodec {
    
    private String tag;
    private FileObject projectDirectory;
    
    private int descriptorVersion = -1;
    private ConfigurationDescriptor projectDescriptor;
    private Vector confs = new Vector();
    private Configuration currentConf = null;
    private ItemConfiguration currentItemConfiguration = null;
    private FolderConfiguration currentFolderConfiguration = null;
    private CCCCompilerConfiguration currentCCCCompilerConfiguration = null;
    private BasicCompilerConfiguration currentBasicCompilerConfiguration = null;
    private CCompilerConfiguration currentCCompilerConfiguration = null;
    private CCCompilerConfiguration currentCCCompilerConfiguration = null;
    private FortranCompilerConfiguration currentFortranCompilerConfiguration = null;
    private CustomToolConfiguration currentCustomToolConfiguration = null;
    private LinkerConfiguration currentLinkerConfiguration = null;
    private ArchiverConfiguration currentArchiverConfiguration = null;
    private LibrariesConfiguration currentLibrariesConfiguration = null;
    private RequiredProjectsConfiguration currentRequiredProjectsConfiguration = null;
    private List currentList = null;
    private int defaultConf = 0;
    private Stack /*<Folder>*/ currentFolderStack = new Stack();
    private Folder currentFolder = null;
    private String relativeOffset;
    private Map<String,String> cache = new HashMap<String,String>();
    
    public ConfigurationXMLCodec(String tag,
            FileObject projectDirectory,
            ConfigurationDescriptor projectDescriptor,
            String relativeOffset) {
        super(projectDescriptor, true);
        this.tag = tag;
        this.projectDirectory = projectDirectory;
        this.projectDescriptor = projectDescriptor;
        this.relativeOffset = relativeOffset;
    }
    
    // interface XMLDecoder
    public String tag() {
        return tag;
    }
    
    // interface XMLDecoder
    public void start(Attributes atts) throws VersionException {
        String what = "project configuration"; // NOI18N
        checkVersion(atts, what, CURRENT_VERSION);
        String versionString = atts.getValue("version");        // NOI18N
        if (versionString != null) {
            descriptorVersion = new Integer(versionString).intValue();
            projectDescriptor.setVersion(descriptorVersion);
        }
    }
    
    // interface XMLDecoder
    public void end() {
        Configuration[] confsA = new Configuration[confs.size()];
        confsA = (Configuration[]) confs.toArray(confsA);
        projectDescriptor.init(confsA, defaultConf);
    }
    
    // interface XMLDecoder
    public void startElement(String element, Attributes atts) {
        if (element.equals(CONF_ELEMENT)) {
            int index = atts.getIndex(VERSION_ATTR);
            index = atts.getIndex(TYPE_ATTR);
            int confType = 0;
            if (index < 0) {
                // Old type. Only makefile was really working...
                confType = MakeConfiguration.TYPE_MAKEFILE;
            } else if (atts.getValue(index).equals("0")) // FIXUP // NOI18N
                confType = MakeConfiguration.TYPE_MAKEFILE;
            else if (atts.getValue(index).equals("1")) // FIXUP // NOI18N
                confType = MakeConfiguration.TYPE_APPLICATION;
            else if (atts.getValue(index).equals("2")) // FIXUP // NOI18N
                confType = MakeConfiguration.TYPE_DYNAMIC_LIB;
            else if (atts.getValue(index).equals("3")) // FIXUP // NOI18N
                confType = MakeConfiguration.TYPE_STATIC_LIB;
            else {
                // FIXUP
            }
            currentConf = new MakeConfiguration(FileUtil.toFile(projectDirectory).getPath(), getString(atts.getValue(0)), confType);
            
        } else if (element.equals(NEO_CONF_ELEMENT)) {
            currentConf = new MakeConfiguration(FileUtil.toFile(projectDirectory).getPath(), getString(atts.getValue(0)), MakeConfiguration.TYPE_APPLICATION);
        } else if (element.equals(EXT_CONF_ELEMENT)) {
            currentConf = new MakeConfiguration(FileUtil.toFile(projectDirectory).getPath(), getString(atts.getValue(0)), MakeConfiguration.TYPE_MAKEFILE);
        } else if (element.equals(SOURCE_FOLDERS_ELEMENT)) { // FIXUP:  < version 5
            currentFolder = new Folder(projectDescriptor, ((MakeConfigurationDescriptor)projectDescriptor).getLogicalFolders(), "ExternalFiles", "Important Files", false); // NOI18N
            ((MakeConfigurationDescriptor)projectDescriptor).setExternalFileItems(currentFolder);
            ((MakeConfigurationDescriptor)projectDescriptor).getLogicalFolders().addFolder(currentFolder);
        } else if (element.equals(LOGICAL_FOLDER_ELEMENT)) {
            if (currentFolderStack.size() == 0) {
                currentFolder = ((MakeConfigurationDescriptor)projectDescriptor).getLogicalFolders();
                currentFolderStack.push(currentFolder);
            } else {
                String name = getString(atts.getValue(NAME_ATTR));
                String displayName = getString(atts.getValue(DISPLAY_NAME_ATTR));
                if (displayName == null)
                    displayName = name;
                boolean projectFiles = atts.getValue(PROJECT_FILES_ATTR).equals(TRUE_VALUE);
                currentFolder = currentFolder.addNewFolder(name, displayName, projectFiles);
                currentFolderStack.push(currentFolder);
                if (!projectFiles)
                    ((MakeConfigurationDescriptor)projectDescriptor).setExternalFileItems(currentFolder);
            }
        } else if (element.equals(SOURCE_ROOT_LIST_ELEMENT)) {
            currentList = ((MakeConfigurationDescriptor)projectDescriptor).getSourceRootsRaw();
        } else if (element.equals(ItemXMLCodec.ITEM_ELEMENT)) {
            String path = atts.getValue(0);
            path = getString(adjustOffset(path));
            //Item item = ((MakeConfigurationDescriptor)projectDescriptor).getLogicalFolders().findItemByPath(path);
            Item item = ((MakeConfigurationDescriptor)projectDescriptor).findProjectItemByPath(path);
            if (item != null) {
                ItemConfiguration itemConfiguration = new ItemConfiguration(currentConf, item);
                currentItemConfiguration = itemConfiguration;
                currentConf.addAuxObject(itemConfiguration);
            } else {
                // FIXUP
            }
        } else if (element.equals(FolderXMLCodec.FOLDER_ELEMENT)) {
            String path = getString(atts.getValue(0));
            Folder folder = ((MakeConfigurationDescriptor)projectDescriptor).findFolderByPath(path);
            if (folder != null) {
                FolderConfiguration folderConfiguration = folder.getFolderConfiguration(currentConf);
                currentFolderConfiguration = folderConfiguration;
            } else {
                // FIXUP
            }
        } else if (element.equals(COMPILERTOOL_ELEMENT)) {
        } else if (element.equals(CCOMPILERTOOL_ELEMENT) || element.equals(SUN_CCOMPILERTOOL_OLD_ELEMENT)) { // FIXUP: <= 23
            if (currentItemConfiguration != null)
                currentCCompilerConfiguration = currentItemConfiguration.getCCompilerConfiguration();
            else if (currentFolderConfiguration != null)
                currentCCompilerConfiguration = currentFolderConfiguration.getCCompilerConfiguration();
            else
                currentCCompilerConfiguration = ((MakeConfiguration)currentConf).getCCompilerConfiguration();
            currentCCCCompilerConfiguration = currentCCompilerConfiguration;
            currentBasicCompilerConfiguration = currentCCompilerConfiguration;
        } else if (element.equals(CCCOMPILERTOOL_ELEMENT) || element.equals(SUN_CCCOMPILERTOOL_OLD_ELEMENT)) { // FIXUP: <= 23
            if (currentItemConfiguration != null)
                currentCCCompilerConfiguration = currentItemConfiguration.getCCCompilerConfiguration();
            else if (currentFolderConfiguration != null)
                currentCCCompilerConfiguration = currentFolderConfiguration.getCCCompilerConfiguration();
            else
                currentCCCompilerConfiguration = ((MakeConfiguration)currentConf).getCCCompilerConfiguration();
            currentCCCCompilerConfiguration = currentCCCompilerConfiguration;
            currentBasicCompilerConfiguration = currentCCCompilerConfiguration;
        } else if (element.equals(FORTRANCOMPILERTOOL_ELEMENT)) {
            if (currentItemConfiguration != null)
                currentFortranCompilerConfiguration = currentItemConfiguration.getFortranCompilerConfiguration();
            else
                currentFortranCompilerConfiguration = ((MakeConfiguration)currentConf).getFortranCompilerConfiguration();
            currentCCCCompilerConfiguration = null;
            currentBasicCompilerConfiguration = currentFortranCompilerConfiguration;
        } else if (element.equals(CUSTOMTOOL_ELEMENT)) {
            if (currentItemConfiguration != null)
                currentCustomToolConfiguration = currentItemConfiguration.getCustomToolConfiguration();
            else
                ; // FIXUP: ERROR
        } else if (element.equals(LINKERTOOL_ELEMENT)) {
            currentLinkerConfiguration = ((MakeConfiguration)currentConf).getLinkerConfiguration();
        } else if (element.equals(ARCHIVERTOOL_ELEMENT)) {
            currentArchiverConfiguration = ((MakeConfiguration)currentConf).getArchiverConfiguration();
        } else if (element.equals(INCLUDE_DIRECTORIES_ELEMENT)) {
            if (currentCCCCompilerConfiguration != null)
                currentList = currentCCCCompilerConfiguration.getIncludeDirectories().getValue();
        } else if (element.equals(PREPROCESSOR_LIST_ELEMENT)) {
            if (currentCCCCompilerConfiguration != null)
                currentList = currentCCCCompilerConfiguration.getPreprocessorConfiguration().getValue();
        } else if (element.equals(LINKER_ADD_LIB_ELEMENT)) {
            if (currentLinkerConfiguration != null)
                currentList = currentLinkerConfiguration.getAdditionalLibs().getValue();
        } else if (element.equals(LINKER_DYN_SERCH_ELEMENT)) {
            if (currentLinkerConfiguration != null)
                currentList = currentLinkerConfiguration.getDynamicSearch().getValue();
        } else if (element.equals(LINKER_LIB_ITEMS_ELEMENT)) {
            currentLibrariesConfiguration = ((MakeConfiguration)currentConf).getLinkerConfiguration().getLibrariesConfiguration();
        } else if (element.equals(REQUIRED_PROJECTS_ELEMENT)) {
            currentRequiredProjectsConfiguration = ((MakeConfiguration)currentConf).getRequiredProjectsConfiguration();
        } else if (element.equals(MAKE_ARTIFACT_ELEMENT)) {
            String pl = atts.getValue("PL");        // NOI18N
            pl = getString(adjustOffset(pl));
            String ct = getString(atts.getValue("CT"));        // NOI18N
            String cn = getString(atts.getValue("CN"));        // NOI18N
            String ac = getString(atts.getValue("AC"));        // NOI18N
            String bl = getString(atts.getValue("BL"));        // NOI18N
            String wd = atts.getValue("WD");        // NOI18N
            wd = getString(adjustOffset(wd));
            String bc = getString(atts.getValue("BC"));        // NOI18N
            String cc = getString(atts.getValue("CC"));        // NOI18N
            String op = getString(atts.getValue("OP"));        // NOI18N

            LibraryItem.ProjectItem projectItem = new LibraryItem.ProjectItem(new MakeArtifact(
                    pl,
                    new Integer(ct).intValue(),
                    cn,
                    ac.equals(TRUE_VALUE),
                    bl != null ? bl.equals(TRUE_VALUE) : true,
                    wd,
                    bc,
                    cc,
                    op));
            if (currentLibrariesConfiguration != null)
                currentLibrariesConfiguration.add(projectItem);
            else if (currentRequiredProjectsConfiguration != null)
                currentRequiredProjectsConfiguration.add(projectItem);
        }
    }
    
    // interface XMLDecoder
    public void endElement(String element, String currentText) {
        if (element.equals(CONF_ELEMENT)) {
            confs.add(currentConf);
            currentConf = null;
        } else if (element.equals(NEO_CONF_ELEMENT)) {
            confs.add(currentConf);
            currentConf = null;
        } else if (element.equals(EXT_CONF_ELEMENT)) {
            confs.add(currentConf);
            currentConf = null;
        } else if (element.equals(COMPILER_SET_ELEMENT)) {
	    if (descriptorVersion <= 33) {
		currentText = currentText.equals("1") ? "GNU" : "Sun"; // NOI18N
            }
            ((MakeConfiguration) currentConf).getCompilerSet().setValue(currentText);
        } else if (element.equals(C_REQUIRED_ELEMENT)) {
            ((MakeConfiguration) currentConf).getCRequired().setValue(currentText.equals(TRUE_VALUE));
        } else if (element.equals(CPP_REQUIRED_ELEMENT)) {
            ((MakeConfiguration) currentConf).getCppRequired().setValue(currentText.equals(TRUE_VALUE));
        } else if (element.equals(FORTRAN_REQUIRED_ELEMENT)) {
            ((MakeConfiguration) currentConf).getFortranRequired().setValue(currentText.equals(TRUE_VALUE));
        } else if (element.equals(PLATFORM_ELEMENT)) {
            int set = new Integer(currentText).intValue();
            if (descriptorVersion <= 37 && set == 4) {
                set = Platform.PLATFORM_GENERIC;
            }
            ((MakeConfiguration)currentConf).getPlatform().setValue(set);
        } else if (element.equals(DEPENDENCY_CHECKING)) {
            boolean ds = currentText.equals(TRUE_VALUE);
            ((MakeConfiguration)currentConf).getDependencyChecking().setValue(ds);
        } else if (element.equals(DEFAULT_CONF_ELEMENT)) {
            defaultConf = new Integer(currentText).intValue();
        } else if (element.equals(PROJECT_MAKEFILE_ELEMENT)) {
            ((MakeConfigurationDescriptor)projectDescriptor).setProjectMakefileName(currentText);
        } else if (element.equals(OPTIMIZATION_LEVEL_ELEMENT)) { // FIXUP <= version 21
            int ol = new Integer(currentText).intValue();
            if (currentCCCCompilerConfiguration != null)  {
                if (ol == 0)
                    currentCCCCompilerConfiguration.getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_DEBUG);
                else if (ol == 1)
                    currentCCCCompilerConfiguration.getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_RELEASE_DIAG);
                else
                    currentCCCCompilerConfiguration.getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_RELEASE);
            }
        } else if (element.equals(DEBUGGING_SYMBOLS_ELEMENT)) { // FIXUP <= version 21
            boolean ds = currentText.equals(TRUE_VALUE);
            if (currentCCCCompilerConfiguration != null)  {
                if (ds)
                    currentCCCCompilerConfiguration.getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_DEBUG);
                else
                    currentCCCCompilerConfiguration.getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_RELEASE);
            }
        } else if (element.equals(DEVELOPMENT_MODE_ELEMENT)) {
            int ol = new Integer(currentText).intValue();
            if (currentBasicCompilerConfiguration != null)  {
                currentBasicCompilerConfiguration.getDevelopmentMode().setValue(ol);
            }
        } else if (element.equals(BUILD_COMMAND_WORKING_DIR_ELEMENT)) {
            String path = currentText;
            path = getString(adjustOffset(path));
            ((MakeConfiguration)currentConf).getMakefileConfiguration().getBuildCommandWorkingDir().setValue(path);
        } else if (element.equals(BUILD_COMMAND_ELEMENT)) {
            ((MakeConfiguration)currentConf).getMakefileConfiguration().getBuildCommand().setValue(currentText);
        } else if (element.equals(CLEAN_COMMAND_ELEMENT)) {
            ((MakeConfiguration)currentConf).getMakefileConfiguration().getCleanCommand().setValue(currentText);
        } else if (element.equals(EXECUTABLE_PATH_ELEMENT)) {
            String path = currentText;
            path = getString(adjustOffset(path));
            ((MakeConfiguration)currentConf).getMakefileConfiguration().getOutput().setValue(path);
        } else if (element.equals(FOLDER_PATH_ELEMENT)) { // FIXUP: < version 5
            currentFolder.addItem(new Item(getString(currentText)));
        } else if (element.equals(SOURCE_FOLDERS_ELEMENT)) { // FIXUP: < version 5
            //((MakeConfigurationDescriptor)projectDescriptor).setExternalFileItems(currentList);
        } else if (element.equals(LOGICAL_FOLDER_ELEMENT)) {
            currentFolderStack.pop();
            if (currentFolderStack.size() > 0) {
                currentFolder = (Folder)currentFolderStack.peek();
            } else {
                currentFolder = null;
            }
        } else if (element.equals(PREPROCESSOR_LIST_ELEMENT)) {
            currentList = null;
        } else if (element.equals(ITEM_PATH_ELEMENT)) {
            String path = currentText;
            path = getString(adjustOffset(path));
            currentFolder.addItem(new Item(path));
        } else if (element.equals(ItemXMLCodec.ITEM_EXCLUDED_ELEMENT) || element.equals(ItemXMLCodec.EXCLUDED_ELEMENT)) {
            currentItemConfiguration.getExcluded().setValue(currentText.equals(TRUE_VALUE));
        } else if (element.equals(ItemXMLCodec.ITEM_TOOL_ELEMENT) || element.equals(ItemXMLCodec.TOOL_ELEMENT)) {
            int tool = new Integer(currentText).intValue();
            currentItemConfiguration.setTool(tool);
        } else if (element.equals(CONFORMANCE_LEVEL_ELEMENT)) { // FIXUP: <= 21
        } else if (element.equals(COMPATIBILITY_MODE_ELEMENT)) { // FIXUP: <= 21
        } else if (element.equals(LIBRARY_LEVEL_ELEMENT)) {
            int ol = new Integer(currentText).intValue();
            currentCCCompilerConfiguration.getLibraryLevel().setValue(ol);
        } else if (element.equals(CUSTOMTOOL_COMMANDLINE_ELEMENT)) {
            currentCustomToolConfiguration.getCommandLine().setValue(getString(currentText));
        } else if (element.equals(CUSTOMTOOL_DESCRIPTION_ELEMENT)) {
            currentCustomToolConfiguration.getDescription().setValue(getString(currentText));
        } else if (element.equals(CUSTOMTOOL_OUTPUTS_ELEMENT)) {
            currentCustomToolConfiguration.getOutputs().setValue(getString(currentText));
        } else if (element.equals(CUSTOMTOOL_ADDITIONAL_DEP_ELEMENT)) {
            currentCustomToolConfiguration.getAdditionalDependencies().setValue(getString(currentText));
        } else if (element.equals(ItemXMLCodec.ITEM_ELEMENT)) {
            currentItemConfiguration.clearChanged();
            currentItemConfiguration = null;
        } else if (element.equals(FolderXMLCodec.FOLDER_ELEMENT)) {
            currentFolderConfiguration.clearChanged();
            currentFolderConfiguration = null;
        } else if (element.equals(COMPILERTOOL_ELEMENT)) { // FIXUP: < 10
        } else if (element.equals(CCOMPILERTOOL_ELEMENT) || element.equals(SUN_CCOMPILERTOOL_OLD_ELEMENT)) { // FIXUP: <=23
            currentCCompilerConfiguration = null;
            currentCCCCompilerConfiguration = null;
            currentBasicCompilerConfiguration = null;
        } else if (element.equals(CCCOMPILERTOOL_ELEMENT) || element.equals(SUN_CCCOMPILERTOOL_OLD_ELEMENT)) { // FIXUP: <= 23
            currentCCCompilerConfiguration = null;
            currentCCCCompilerConfiguration = null;
            currentBasicCompilerConfiguration = null;
        } else if (element.equals(FORTRANCOMPILERTOOL_ELEMENT)) {
            currentFortranCompilerConfiguration = null;
            currentBasicCompilerConfiguration = null;
        } else if (element.equals(CUSTOMTOOL_ELEMENT)) {
            currentCustomToolConfiguration = null;
        } else if (element.equals(LINKERTOOL_ELEMENT)) {
            if (descriptorVersion <= 27 && !currentLinkerConfiguration.getOutput().getModified())
                currentLinkerConfiguration.getOutput().setValue(currentLinkerConfiguration.getOutputDefault27());
            currentLinkerConfiguration = null;
        } else if (element.equals(ARCHIVERTOOL_ELEMENT)) {
            if (descriptorVersion <= 27 && !currentArchiverConfiguration.getOutput().getModified())
                currentArchiverConfiguration.getOutput().setValue(currentArchiverConfiguration.getOutputDefault27());
            currentArchiverConfiguration = null;
        } else if (element.equals(INCLUDE_DIRECTORIES_ELEMENT)) {
            currentList = null;
        } else if (element.equals(PREPROCESSOR_LIST_ELEMENT)) {
            currentList = null;
        } else if (element.equals(LINKER_ADD_LIB_ELEMENT)) {
            currentList = null;
        } else if (element.equals(LINKER_DYN_SERCH_ELEMENT)) {
            currentList = null;
        } else if (element.equals(DIRECTORY_PATH_ELEMENT)) {
            if (currentList != null) {
                String path = getString(adjustOffset(currentText));
                currentList.add(path);
            }
        } else if (element.equals(LIST_ELEMENT)) {
            if (currentList != null) {
                currentList.add(getString(currentText));
            }
        } else if (element.equals(COMMAND_LINE_ELEMENT)) {
            if (currentBasicCompilerConfiguration != null)
                currentBasicCompilerConfiguration.getCommandLineConfiguration().setValue(getString(currentText));
            if (currentLinkerConfiguration != null)
                currentLinkerConfiguration.getCommandLineConfiguration().setValue(getString(currentText));
            if (currentArchiverConfiguration != null)
                currentArchiverConfiguration.getCommandLineConfiguration().setValue(getString(currentText));
        } else if (element.equals(COMMANDLINE_TOOL_ELEMENT)) {
            if (currentBasicCompilerConfiguration != null)
                currentBasicCompilerConfiguration.getTool().setValue(getString(currentText));
            if (currentLinkerConfiguration != null)
                currentLinkerConfiguration.getTool().setValue(getString(currentText));
            if (currentArchiverConfiguration != null)
                currentArchiverConfiguration.getTool().setValue(getString(currentText));
        } else if (element.equals(PREPROCESSOR_ELEMENT)) {
            // Old style preprocessor list
            if (currentCCCCompilerConfiguration != null) {
                List<String> list = CppUtils.tokenizeString(currentText);
                List res = new ArrayList<String>();
                for(String val : list){
                    res.add(this.getString(val));
                }
                currentCCCCompilerConfiguration.getPreprocessorConfiguration().getValue().addAll(res);
            }
        } else if (element.equals(STRIP_SYMBOLS_ELEMENT)) {
            boolean ds = currentText.equals(TRUE_VALUE);
            if (currentBasicCompilerConfiguration != null)
                currentBasicCompilerConfiguration.getStrip().setValue(ds);
            if (currentLinkerConfiguration != null)
                currentLinkerConfiguration.getStripOption().setValue(ds);
        } else if (element.equals(SIXTYFOUR_BITS_ELEMENT)) {
            boolean ds = currentText.equals(TRUE_VALUE);
            if (currentBasicCompilerConfiguration != null)
                currentBasicCompilerConfiguration.getSixtyfourBits().setValue(ds ? BasicCompilerConfiguration.BITS_64 : BasicCompilerConfiguration.BITS_DEFAULT);
        } else if (element.equals(ARCHITECTURE_ELEMENT)) {
            int val = new Integer(currentText).intValue();
            if (currentBasicCompilerConfiguration != null)
                currentBasicCompilerConfiguration.getSixtyfourBits().setValue(val);
        } else if (element.equals(INHERIT_INC_VALUES_ELEMENT)) {
            boolean ds = currentText.equals(TRUE_VALUE);
            if (currentCCCCompilerConfiguration != null)
                currentCCCCompilerConfiguration.getInheritIncludes().setValue(ds);
        } else if (element.equals(INHERIT_PRE_VALUES_ELEMENT)) {
            boolean ds = currentText.equals(TRUE_VALUE);
            if (currentCCCCompilerConfiguration != null)
                currentCCCCompilerConfiguration.getInheritPreprocessor().setValue(ds);
        } else if (element.equals(SUPRESS_WARNINGS_ELEMENT)) { // FIXUP: <= 21
            boolean ds = currentText.equals(TRUE_VALUE);
            if (currentCCCCompilerConfiguration != null) {
                if (ds)
                    currentCCCCompilerConfiguration.getWarningLevel().setValue(BasicCompilerConfiguration.WARNING_LEVEL_NO);
            }
        } else if (element.equals(WARNING_LEVEL_ELEMENT)) {
            int ol = new Integer(currentText).intValue();
            if (currentBasicCompilerConfiguration != null)  {
                currentBasicCompilerConfiguration.getWarningLevel().setValue(ol);
            }
        } else if (element.equals(MT_LEVEL_ELEMENT)) {
            int ol = new Integer(currentText).intValue();
            if (currentCCCCompilerConfiguration != null)  {
                currentCCCCompilerConfiguration.getMTLevel().setValue(ol);
            }
        } else if (element.equals(STANDARDS_EVOLUTION_ELEMENT)) {
            int ol = new Integer(currentText).intValue();
            if (currentCCCCompilerConfiguration != null)  {
                currentCCCCompilerConfiguration.getStandardsEvolution().setValue(ol);
            }
        } else if (element.equals(LANGUAGE_EXTENSION_ELEMENT)) {
            int ol = new Integer(currentText).intValue();
            if (currentCCCCompilerConfiguration != null)  {
                currentCCCCompilerConfiguration.getLanguageExt().setValue(ol);
            }
        } else if (element.equals(CPP_STYLE_COMMENTS_ELEMENT)) { // FIXUP: <= 21
        } else if (element.equals(OUTPUT_ELEMENT)) {
            if (currentLinkerConfiguration != null)
                currentLinkerConfiguration.getOutput().setValue(getString(currentText));
            if (currentArchiverConfiguration != null)
                currentArchiverConfiguration.getOutput().setValue(getString(currentText));
        } else if (element.equals(LINKER_KPIC_ELEMENT)) {
            boolean ds = currentText.equals(TRUE_VALUE);
            if (currentLinkerConfiguration != null)
                currentLinkerConfiguration.getPICOption().setValue(ds);
        } else if (element.equals(LINKER_NORUNPATH_ELEMENT)) {
            boolean ds = currentText.equals(TRUE_VALUE);
            if (currentLinkerConfiguration != null)
                currentLinkerConfiguration.getNorunpathOption().setValue(ds);
        } else if (element.equals(LINKER_ASSIGN_ELEMENT)) {
            boolean ds = currentText.equals(TRUE_VALUE);
            if (currentLinkerConfiguration != null)
                currentLinkerConfiguration.getNameassignOption().setValue(ds);
        } else if (element.equals(ADDITIONAL_DEP_ELEMENT)) {
            if (currentLinkerConfiguration != null)
                currentLinkerConfiguration.getAdditionalDependencies().setValue(getString(currentText));
            if (currentArchiverConfiguration != null)
                currentArchiverConfiguration.getAdditionalDependencies().setValue(getString(currentText));
            if (currentBasicCompilerConfiguration != null)
                currentBasicCompilerConfiguration.getAdditionalDependencies().setValue(getString(currentText));
        } else if (element.equals(ARCHIVERTOOL_VERBOSE_ELEMENT)) {
            boolean ds = currentText.equals(TRUE_VALUE);
            if (currentArchiverConfiguration != null)
                currentArchiverConfiguration.getVerboseOption().setValue(ds);
        } else if (element.equals(ARCHIVERTOOL_RUN_RANLIB_ELEMENT)) {
            boolean ds = currentText.equals(TRUE_VALUE);
            if (currentArchiverConfiguration != null)
                currentArchiverConfiguration.getRunRanlib().setValue(ds);
        } else if (element.equals(ARCHIVERTOOL_SUPRESS_ELEMENT)) {
            boolean ds = currentText.equals(TRUE_VALUE);
            if (currentArchiverConfiguration != null)
                currentArchiverConfiguration.getSupressOption().setValue(ds);
        } else if (element.equals(LINKER_LIB_ITEMS_ELEMENT)) {
            currentLibrariesConfiguration = null;
        } else if (element.equals(REQUIRED_PROJECTS_ELEMENT)) {
            currentRequiredProjectsConfiguration = null;
        } else if (element.equals(LINKER_LIB_OPTION_ITEM_ELEMENT)) {
            if (currentLibrariesConfiguration != null)
                currentLibrariesConfiguration.add(new LibraryItem.OptionItem(getString(currentText)));
        } else if (element.equals(LINKER_LIB_FILE_ITEM_ELEMENT)) {
            if (currentLibrariesConfiguration != null)
                currentLibrariesConfiguration.add(new LibraryItem.LibFileItem(getString(currentText)));
        } else if (element.equals(LINKER_LIB_LIB_ITEM_ELEMENT)) {
            if (currentLibrariesConfiguration != null)
                currentLibrariesConfiguration.add(new LibraryItem.LibItem(getString(currentText)));
        } else if (element.equals(LINKER_LIB_STDLIB_ITEM_ELEMENT)) {
            LibraryItem.StdLibItem stdLibItem = Platforms.getPlatform(((MakeConfiguration)currentConf).getPlatform().getValue()).getStandardLibrarie(currentText);
            if (currentLibrariesConfiguration != null && stdLibItem != null)
                currentLibrariesConfiguration.add(stdLibItem);
        }
    }
    
    private String adjustOffset(String path) {
        if (relativeOffset != null && path.startsWith("..")) // NOI18N
            path = IpeUtils.trimDotDot(relativeOffset + path);
        return path;
    }
    
    private String getString(String s){
        String res = cache.get(s);
        if (res == null){
            cache.put(s, s);
            return s;
        }
        return res;
    }
}
