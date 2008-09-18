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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.apisupport.project.ui.wizard.options;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.util.Utilities;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Wizard for generating OptionsPanel
 *
 * @author Radek Matous
 * @author Max Sauer
 */
final class NewOptionsIterator extends BasicWizardIterator {
    
    private NewOptionsIterator.DataModel data;
    
    private NewOptionsIterator() {  /* Use factory method. */ }
    
    public static NewOptionsIterator createIterator() {
        return new NewOptionsIterator();
    }
    
    public Set instantiate() throws IOException {
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        cmf.run();
        return getCreatedFiles(cmf, data.getProject());
    }
    
    protected BasicWizardIterator.Panel[] createPanels(WizardDescriptor wiz) {
        data = new NewOptionsIterator.DataModel(wiz);
        return new BasicWizardIterator.Panel[] {
            new OptionsPanel0(wiz, data),
            new OptionsPanel(wiz, data)
        };
    }
    
    public @Override void uninitialize(WizardDescriptor wiz) {
        super.uninitialize(wiz);
        data = null;
    }
    
    static final class DataModel extends BasicWizardIterator.BasicDataModel {
        
        // use code > 0 && < 1024 for error messages, >= 1024 for info messages
        // and < 0 for warnings. See is...Message() methods
        private static final int SUCCESS = 0;
        private static final int ERR_INVALID_CLASSNAME_PREFIX = 1;
        private static final int MSG_BLANK_SECONDARY_PANEL_TITLE = 1024;
        private static final int MSG_BLANK_TOOLTIP = 1025;
        private static final int MSG_BLANK_PRIMARY_PANEL = 1026;
        private static final int MSG_BLANK_PRIMARY_PANEL_TITLE = 1027;
        private static final int MSG_BLANK_CATEGORY_NAME = 1028;
        private static final int MSG_BLANK_ICONPATH = 1029;
        private static final int MSG_BLANK_PACKAGE_NAME = 1030;
        private static final int MSG_BLANK_CLASSNAME_PREFIX = 1031;
        private static final int MSG_BLANK_KEYWORDS = 1032;
        
        
        private static final int WARNING_INCORRECT_ICON_SIZE = -1;
        
        private static final String[] CATEGORY_BUNDLE_KEYS = {
            "OptionsCategory_Title", // NOI18N
            "OptionsCategory_Name", // NOI18N
            "OptionsCategory_Keywords"
        };
        
        private static final String[] ADVANCED_BUNDLE_KEYS = {
            "AdvancedOption_DisplayName", // NOI18N
            "AdvancedOption_Tooltip", // NOI18N
            "AdvancedOption_Keywords" // NOI18N
        };
        
        private static final String[] TOKENS = {
            "PACKAGE_NAME", // NOI18N
            "AdvancedOption_CLASS_NAME", // NOI18N
            "OptionsCategory_CLASS_NAME", // NOI18N
            "Panel_CLASS_NAME", // NOI18N
            "OptionsPanelController_CLASS_NAME", // NOI18N
            "OptionsPanelController_INSTANCE", // NOI18N
            "ICON_PATH", // NOI18N
            ADVANCED_BUNDLE_KEYS[0],
            ADVANCED_BUNDLE_KEYS[1],
            CATEGORY_BUNDLE_KEYS[0],
            CATEGORY_BUNDLE_KEYS[1]
        };
                
        private static final String ADVANCED_OPTION = "AdvancedOption"; //NOI18N
        private static final String OPTIONS_CATEGORY = "OptionsCategory"; //NOI18N
        private static final String PANEL = "Panel"; //NOI18N
        private static final String OPTIONS_PANEL_CONTROLLER = "OptionsPanelController"; //NOI18N
        
        private static final String JAVA_TEMPLATE_PREFIX = "template_myplugin"; // NOI18N
        private static final String FORM_TEMPLATE_PREFIX = "template_myplugin_form"; // NOI18N
        
        static final String MISCELLANEOUS_LABEL = "Miscellaneous"; //NOI18N
        
        private CreatedModifiedFiles files;
        private String codeNameBase;
        private boolean advanced;
        
        //Advanced panel
        private String primaryPanel;
        private String secondaryPanelTitle;
        private String tooltip;
        private String primaryKeywords;
        
        //OptionsCategory
        private String primaryPanelTitle;
        private String categoryName;
        private String iconPath;
        private String secondaryKeywords;
        private boolean allowAdvanced;
        
        private String classNamePrefix;
        
        DataModel(WizardDescriptor wiz) {
            super(wiz);
        }

        int setDataForSecondaryPanel(final String primaryPanel, final String secondaryPanelTitle, final String tooltip, final String secondaryKeywords) {
            this.advanced = true;
            this.primaryPanel = primaryPanel;
            this.secondaryPanelTitle = secondaryPanelTitle;
            this.tooltip = tooltip;
            this.secondaryKeywords = secondaryKeywords;
            return checkFirstPanel();
        }
        
        int setDataForPrimaryPanel(final String primaryPanelTitle,
                final String categoryName, final String iconPath, final boolean allowAdvanced, final String primaryKeywords) {
            this.advanced = false;
            this.primaryPanelTitle = primaryPanelTitle;
            this.categoryName = categoryName;
            this.iconPath = iconPath;
            this.allowAdvanced = allowAdvanced;
            this.primaryKeywords = primaryKeywords;
            return checkFirstPanel();
        }
        
        public @Override String getPackageName() {
            String retValue;
            retValue = super.getPackageName();
            if (retValue == null) {
                retValue = getCodeNameBase();
                super.setPackageName(retValue);
            }
            return retValue;
        }
        
        public int setPackageAndPrefix(String packageName, String classNamePrefix) {
            setPackageName(packageName);
            this.classNamePrefix = classNamePrefix;
            int errCode = checkFinalPanel();
            if (isSuccessCode(errCode)) {
                generateCreatedModifiedFiles();
            }
            return errCode;
        }

        private String getAbsoluteIconPath() {
            return getProject().getProjectDirectory() + "/src/" + getIconPath();
        }
        
        private Map<String, String> getTokenMap() {
            Map<String, String> retval = new HashMap<String, String>();
            for (int i = 0; i < TOKENS.length; i++) {
                if (isAdvanced() && "ICON_PATH".equals(TOKENS[i])) { // NOI18N
                    continue;
                }
                retval.put(TOKENS[i], getReplacement(TOKENS[i]));
            }
            return retval;
        }
        
        private String getReplacement(String key) {
            if ("PACKAGE_NAME".equals(key)) {// NOI18N
                return getPackageName();
            } else if ("AdvancedOption_CLASS_NAME".equals(key)) {// NOI18N
                return getAdvancedOptionClassName();
            } else if ("OptionsCategory_CLASS_NAME".equals(key)) {// NOI18N
                return getOptionsCategoryClassName();
            } else if ("Panel_CLASS_NAME".equals(key)) {// NOI18N
                return getPanelClassName();
            } else if ("OptionsPanelController_CLASS_NAME".equals(key)) {// NOI18N
                return getOptionsPanelControllerClassName();
            } else if ("OptionsPanelController_INSTANCE".equals(key)) {// NOI18N
                return getOptionsPanelControllerInstance();
            } else if ("ICON_PATH".equals(key)) {// NOI18N
                return addCreateIconOperation(new CreatedModifiedFiles(getProject()), getAbsoluteIconPath());
            } else {
                return key + "_" + getClassNamePrefix();
            }
            
        }
        
        
        private String getBundleValue(String key) {
            if (key.startsWith("OptionsCategory_Title")) {// NOI18N
                return getPrimaryPanelTitle();
            } else if (key.startsWith("OptionsCategory_Name")) {// NOI18N
                return getCategoryName();
            } else if (key.startsWith("AdvancedOption_DisplayName")) {// NOI18N
                return getSecondaryPanelTitle();
            } else if (key.startsWith("AdvancedOption_Tooltip")) {// NOI18N
                return getTooltip();
            } else if (key.startsWith("OptionsCategory_Keywords")) {// NOI18N
                return getPrimaryKeywords();
            } else if (key.startsWith("AdvancedOption_Keywords")) {// NOI18N
                return getSecondaryKeywords();
            } else {
                throw new AssertionError(key);
            }
        }
        
        String getMessage(int code) {
            String field = null;
            switch(code) {
                case SUCCESS:
                    return "";
                case MSG_BLANK_SECONDARY_PANEL_TITLE:
                    field = "FIELD_SecondaryPanelTitle";//NOI18N
                    break;
                case MSG_BLANK_TOOLTIP:
                    field = "FIELD_Tooltip";//NOI18N
                    break;
                case MSG_BLANK_PRIMARY_PANEL:
                    field = "FIELD_PrimaryPanel"; //NOI18N
                    break;
                case MSG_BLANK_KEYWORDS:
                    field = "FIELD_Keywords"; // NOI18N
                    break;
                case MSG_BLANK_PRIMARY_PANEL_TITLE:
                    field = "FIELD_PrimaryPanelTitle";//NOI18N
                    break;
                case MSG_BLANK_CATEGORY_NAME:
                    field = "FIELD_CategoryName";//NOI18N
                    break;
                case MSG_BLANK_ICONPATH:
                    field = "FIELD_IconPath";//NOI18N
                    break;
                case MSG_BLANK_PACKAGE_NAME:
                    field = "FIELD_PackageName";//NOI18N
                    break;
                case MSG_BLANK_CLASSNAME_PREFIX:
                    field = "FIELD_ClassNamePrefix";//NOI18N
                    break;
                case ERR_INVALID_CLASSNAME_PREFIX:
                    field = "FIELD_ClassNamePrefix";//NOI18N
                    break;
                case WARNING_INCORRECT_ICON_SIZE:
                    File icon = new File(getAbsoluteIconPath());
                    assert icon.exists();
                    return UIUtil.getIconDimensionWarning(icon, 32, 32);
                default:
                    assert false : "Unknown code: " + code;
            }
            field = NbBundle.getMessage(NewOptionsIterator.class, field);
            if (isErrorCode(code)) {
                return NbBundle.getMessage(NewOptionsIterator.class, "ERR_FieldInvalid", field);    // NOI18N
            }
            if (isInfoCode(code)) {
                return NbBundle.getMessage(NewOptionsIterator.class, "MSG_FieldEmpty", field);    // NOI18N
            }
            return "";//NOI18N
        }
        
        static boolean isSuccessCode(int code) {
            return code == 0;
        }
        
        static boolean isErrorCode(int code) {
            return 0 < code && code < 1024;
        }
        
        static boolean isWarningCode(int code) {
            return code < 0;
        }
        
        
        static boolean isInfoCode(int code) {
            return code >= 1024;
        }

        private int checkFirstPanel() {
            if (advanced) {
                if (getPrimaryPanel().length() == 0) {
                    return MSG_BLANK_PRIMARY_PANEL;
                } else if (getSecondaryPanelTitle().length() == 0) {
                    return MSG_BLANK_SECONDARY_PANEL_TITLE;
                } else if (getTooltip().length() == 0) {
                    return MSG_BLANK_TOOLTIP;
                } else if (getSecondaryKeywords().length() == 0) {
                    return MSG_BLANK_KEYWORDS;
                }
            } else {
                if (getPrimaryPanelTitle().length() == 0) {
                    return MSG_BLANK_PRIMARY_PANEL_TITLE;
                } else if (getCategoryName().length() == 0) {
                    return MSG_BLANK_CATEGORY_NAME;
                } else if (getIconPath().length() == 0) {
                    return MSG_BLANK_ICONPATH;
                } else if (getPrimaryKeywords().length() == 0)  {
                    return MSG_BLANK_KEYWORDS;
                } else {
                    File icon = new File(getAbsoluteIconPath());
                    if (!icon.exists()) {
                        return MSG_BLANK_ICONPATH;
                    }
                }
                //warnings should go at latest
                File icon = new File(getAbsoluteIconPath());
                assert icon.exists();
                if (!UIUtil.isValidIcon(icon, 32, 32)) {
                    return WARNING_INCORRECT_ICON_SIZE;
                }
            }
            return 0;
        }
        
        private int checkFinalPanel() {
            if (getPackageName().length() == 0) {
                return MSG_BLANK_PACKAGE_NAME;
            } else if (getClassNamePrefix().length() == 0) {
                return MSG_BLANK_CLASSNAME_PREFIX;
            } else if (!Utilities.isJavaIdentifier(getClassNamePrefix())) {
                return ERR_INVALID_CLASSNAME_PREFIX;
        }
            
            return 0;
        }
        
        public CreatedModifiedFiles getCreatedModifiedFiles() {
            if (files == null) {
                files = generateCreatedModifiedFiles();
            }
            return files;
        }
        
        private CreatedModifiedFiles generateCreatedModifiedFiles() {
            assert isSuccessCode(checkFirstPanel()) || isWarningCode(checkFirstPanel());
            assert isSuccessCode(checkFinalPanel());
            files = new CreatedModifiedFiles(getProject());
            generateFiles();
            generateBundleKeys();
            generateDependencies();
            generateLayerEntry();
            if (!isAdvanced()) {
                addCreateIconOperation(files, getAbsoluteIconPath());
            }
            return files;
        }
    
        private void generateFiles() {
            if(isAdvanced()) {
                files.add(createJavaFileCopyOperation(OPTIONS_PANEL_CONTROLLER));
                files.add(createJavaFileCopyOperation(PANEL));
                files.add(createFormFileCopyOperation(PANEL));
            } else {
                if(!isAdvancedCategory()) {
                    files.add(createJavaFileCopyOperation(OPTIONS_PANEL_CONTROLLER));                     
                    files.add(createJavaFileCopyOperation(PANEL));
                    files.add(createFormFileCopyOperation(PANEL));
                }
            }
        }
        
        private void generateBundleKeys() {
            String[] bundleKeys = (isAdvanced()) ? ADVANCED_BUNDLE_KEYS : CATEGORY_BUNDLE_KEYS;
            for (int i = 0; i < bundleKeys.length; i++) {
                String key = getReplacement(bundleKeys[i]);
                String value = getBundleValue(key);
                files.add(files.bundleKey(getDefaultPackagePath("Bundle.properties", true),key,value));// NOI18N                        
            }
        }
        
        private void generateDependencies() {
            files.add(files.addModuleDependency("org.openide.util")); // NOI18N
            files.add(files.addModuleDependency("org.netbeans.modules.options.api","1",null,true));// NOI18N
            files.add(files.addModuleDependency("org.openide.awt")); // NOI18N
            files.add(files.addModuleDependency("org.jdesktop.layout")); // NOI18N
        }

        private void generateLayerEntry() {
            if(isAdvanced()) {
                String resourcePathPrefix = "OptionsDialog/"+getPrimaryPanel()+"/";  //NOI18N
                String instanceName = getAdvancedOptionClassName();
                String instanceFullPath = resourcePathPrefix + getPackageName().replace('.','-') + "-" + instanceName + ".instance";//NOI18N

                files.add(files.createLayerEntry(instanceFullPath, null, null, null, null));
                files.add(files.createLayerAttribute(instanceFullPath, "instanceCreate", "methodvalue:org.netbeans.spi.options.AdvancedOption.createSubCategory")); // NOI18N
                files.add(files.createLayerAttribute(instanceFullPath, "controller", "newvalue:" + getPackageName() + "." + getOptionsPanelControllerClassName())); // NOI18N
                files.add(files.createLayerAttribute(instanceFullPath, "displayName", "bundlevalue:" + getPackageName() + ".Bundle#" + ADVANCED_BUNDLE_KEYS[0] + "_" + getClassNamePrefix())); // NOI18N
                files.add(files.createLayerAttribute(instanceFullPath, "toolTip", "bundlevalue:" + getPackageName() + ".Bundle#" + ADVANCED_BUNDLE_KEYS[1] + "_" + getClassNamePrefix())); // NOI18N
                files.add(files.createLayerAttribute(instanceFullPath, "keywords", "bundlevalue:" + getPackageName() + ".Bundle#" + ADVANCED_BUNDLE_KEYS[2] + "_" + getClassNamePrefix())); // NOI18N
                files.add(files.createLayerAttribute(instanceFullPath, "keywordsCategory", getPrimaryPanel() + "/" + getCategoryName()));
            } else {
                String resourcePathPrefix = "OptionsDialog/"; //NOI18N
                String instanceName = getOptionsCategoryClassName();
                String instanceFullPath = resourcePathPrefix + instanceName + ".instance"; //NOI18N
                Map<String, Object> attrsMap = new HashMap<String, Object>(7);
                attrsMap.put("iconBase", iconPath); // NOI18N
                attrsMap.put("keywordsCategory", getClassNamePrefix()); //NOI18N

                files.add(files.createLayerEntry(instanceFullPath, null, null, null, attrsMap));
                files.add(files.createLayerAttribute(instanceFullPath, "instanceCreate", "methodvalue:org.netbeans.spi.options.OptionsCategory.createCategory")); //NOI18N
                files.add(files.createLayerAttribute(instanceFullPath, "title", "bundlevalue:" + getPackageName() + ".Bundle#" + CATEGORY_BUNDLE_KEYS[0] + "_" + getClassNamePrefix())); //NOI18N
                files.add(files.createLayerAttribute(instanceFullPath, "categoryName", "bundlevalue:" + getPackageName() + ".Bundle#" + CATEGORY_BUNDLE_KEYS[1] + "_" + getClassNamePrefix())); //NOI18N
                if (allowAdvanced) {
                    files.add(files.createLayerAttribute(instanceFullPath, "advancedOptionsFolder", resourcePathPrefix + instanceName)); //NOI18N
                } else {
                    files.add(files.createLayerAttribute(instanceFullPath, "controller", "newvalue:" + getPackageName() + "." + getOptionsPanelControllerClassName())); //NOI18N
                }
                files.add(files.createLayerAttribute(instanceFullPath, "keywords", "bundlevalue:" + getPackageName() + ".Bundle#" + CATEGORY_BUNDLE_KEYS[2] + "_" + getClassNamePrefix())); //NOI18N
            }
        }

        private CreatedModifiedFiles.Operation createJavaFileCopyOperation(final String templateSuffix) {
            FileObject template = CreatedModifiedFiles.getTemplate(JAVA_TEMPLATE_PREFIX + templateSuffix + ".java");
            assert template != null : JAVA_TEMPLATE_PREFIX+templateSuffix;
            return files.createFileWithSubstitutions(getFilePath(templateSuffix), template, getTokenMap());
        }
        
        private String getFilePath(final String templateSuffix) {
            String fileName = getClassNamePrefix()+templateSuffix+ ".java"; // NOI18N
            return getDefaultPackagePath(fileName, false);//NOI18N
        }
        
        private CreatedModifiedFiles.Operation createFormFileCopyOperation(final String templateSuffix) {
            FileObject template = CreatedModifiedFiles.getTemplate(FORM_TEMPLATE_PREFIX + templateSuffix + ".form");
            assert template != null : JAVA_TEMPLATE_PREFIX+templateSuffix;
            String fileName = getClassNamePrefix()+templateSuffix+ ".form";// NOI18N
            String filePath = getDefaultPackagePath(fileName, false);
            return files.createFile(filePath, template);
        }
        
        private String getCodeNameBase() {
            if (codeNameBase == null) {
                NbModuleProvider mod = getProject().getLookup().lookup(NbModuleProvider.class);
                codeNameBase = mod.getCodeNameBase();
            }
            return codeNameBase;
        }
        
        private String getPrimaryPanel() {
            // map Miscellaneous category to its ID
            if(primaryPanel.equals(MISCELLANEOUS_LABEL)) {
                return "Advanced"; //NOI18N
            } else {
                return primaryPanel;
            }
        }
        
        private String getSecondaryPanelTitle() {
            assert !isAdvanced() || secondaryPanelTitle != null;
            return secondaryPanelTitle;
        }
        
        private String getTooltip() {
            assert !isAdvanced() || tooltip != null;
            return tooltip;
        }
        
        private String getPrimaryPanelTitle() {
            assert isAdvanced() || primaryPanelTitle != null;
            return primaryPanelTitle;
        }

        private String getPrimaryKeywords() {
            assert isAdvanced() || primaryKeywords != null;
            return primaryKeywords;
        }

        private String getSecondaryKeywords() {
            assert !isAdvanced() || secondaryKeywords != null;
            return secondaryKeywords;
        }
        
        private String getCategoryName() {
            assert isAdvanced() || categoryName != null;
            return categoryName;
        }
        
        private String getIconPath() {
            assert isAdvanced() || iconPath != null;
            return iconPath;
        }
        
        String getClassNamePrefix() {
            if (classNamePrefix == null) {
                classNamePrefix = isAdvanced() ? getSecondaryPanelTitle() : getCategoryName();
                classNamePrefix = classNamePrefix.trim().replaceAll(" ", "");
                if (!Utilities.isJavaIdentifier(classNamePrefix)) {
                    classNamePrefix = "";
                }
            }
            return classNamePrefix;
        }
        
        private boolean isAdvanced() {
            return advanced;
        }
        
        private boolean isAdvancedCategory() {
            return allowAdvanced;
        }
        
        private String getAdvancedOptionClassName() {
            return getClassName(ADVANCED_OPTION);
        }
        
        private String getOptionsCategoryClassName() {
            return getClassName(OPTIONS_CATEGORY);
        }
        
        private String getPanelClassName() {
            return getClassName(PANEL);
        }
        
        private String getOptionsPanelControllerClassName() {
            return getClassName(OPTIONS_PANEL_CONTROLLER);
        }
        
        private String getOptionsPanelControllerInstance() {
            if(isAdvancedCategory()) {
                // "OptionsPanelController.createAdvanced("MyOptionsCategory")"
                return "OptionsPanelController.createAdvanced(\""+getOptionsCategoryClassName()+"\")";//NOI18N
            } else {
                // "new MyOptionsPanelController();"
                return "new "+getOptionsPanelControllerClassName()+"()"; //NOI18N
            }
        }
        
        private String getClassName(String suffix) {
            return getClassNamePrefix() + suffix;
        }
        
    }
}
