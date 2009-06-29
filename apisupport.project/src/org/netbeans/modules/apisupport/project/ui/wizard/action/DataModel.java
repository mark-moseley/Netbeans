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

package org.netbeans.modules.apisupport.project.ui.wizard.action;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;

/**
 * Data model used across the <em>New Action Wizard</em>.
 */
final class DataModel extends BasicWizardIterator.BasicDataModel {
    
    static final String[] PREDEFINED_COOKIE_CLASSES;
    
    private static final String[] HARDCODED_IMPORTS = new String[] {
        "org.openide.nodes.Node", // NOI18N
        "org.openide.util.HelpCtx", // NOI18N
        "org.openide.util.NbBundle", // NOI18N
        "org.openide.util.actions.CookieAction" // NOI18N
    };
    
    /** Maps FQCN to CNB. */
    private static final Map<String,String> CLASS_TO_CNB;
    
    static {
        Map<String,String> map = new HashMap<String,String>(5);
        map.put("org.openide.loaders.DataObject", "org.openide.loaders"); // NOI18N
        map.put("org.openide.cookies.EditCookie", "org.openide.nodes"); // NOI18N
        map.put("org.openide.cookies.OpenCookie", "org.openide.nodes"); // NOI18N
        map.put("org.netbeans.api.project.Project", "org.netbeans.modules.projectapi"); // NOI18N
        map.put("org.openide.cookies.EditorCookie", "org.openide.text"); // NOI18N
        CLASS_TO_CNB = Collections.unmodifiableMap(map);
        PREDEFINED_COOKIE_CLASSES = new String[5];
        DataModel.CLASS_TO_CNB.keySet().toArray(PREDEFINED_COOKIE_CLASSES);
    }
    
    private static final String NEW_LINE = System.getProperty("line.separator"); // NOI18N
    
    /** Default indent. (four spaces hardcoded currently). */
    private static final String INDENT = "    "; // NOI18N
    /** Double {@link #INDENT}. */
    private static final String INDENT_2X = INDENT + INDENT; // NOI18N
    
    private CreatedModifiedFiles cmf;
    
    // first panel data (Action Type)
    private boolean alwaysEnabled;
    private String[] cookieClasses;
    private boolean multiSelection;
    
    // second panel data (GUI Registration)
    private String category;
    
    // global menu item fields
    private boolean globalMenuItemEnabled;
    private String gmiParentMenuPath;
    private Position gmiPosition;
    private boolean gmiSeparatorAfter;
    private boolean gmiSeparatorBefore;
    
    // global toolbar button fields
    private boolean toolbarEnabled;
    private String toolbar;
    private Position toolbarPosition;
    
    // global keyboard shortcut
    private boolean kbShortcutEnabled;
    private final Set<String> keyStrokes = new HashSet<String>();
    
    // file type context menu item
    private boolean ftContextEnabled;
    private String ftContextType;
    private Position ftContextPosition;
    private boolean ftContextSeparatorAfter;
    private boolean ftContextSeparatorBefore;
    
    // editor context menu item
    private boolean edContextEnabled;
    private String edContextType;
    private Position edContextPosition;
    private boolean edContextSeparatorAfter;
    private boolean edContextSeparatorBefore;
    
    // third panel data (Name, Icon, and Location)
    private String className;
    private String displayName;
    private String origIconPath;
    private String largeIconPath;
    
    DataModel(WizardDescriptor wiz) {
        super(wiz);
    }
    
    private void regenerate() {
        String dashedPkgName = getPackageName().replace('.', '-');
        String dashedFqClassName = dashedPkgName + '-' + className;
        String shadow = dashedFqClassName + ".shadow"; // NOI18N
        
        cmf = new CreatedModifiedFiles(getProject());

        boolean actionProxy;
        boolean actionContext;
        try {
            SpecificationVersion current = getModuleInfo().getDependencyVersion("org.openide.awt");
            actionProxy = current.compareTo(new SpecificationVersion("7.3")) >= 0; // NOI18N
            actionContext = current.compareTo(new SpecificationVersion("7.10")) >= 0; // NOI18N
        } catch (IOException ex) {
            Logger.getLogger(DataModel.class.getName()).log(Level.INFO, null, ex);
            actionProxy = false;
            actionContext = false;
        }
        
        String actionPath = getDefaultPackagePath(className + ".java", false); // NOI18N
        // XXX use nbresloc URL protocol rather than DataModel.class.getResource(...):
        FileObject template = CreatedModifiedFiles.getTemplate(
            alwaysEnabled ? (
                actionProxy ?
                "actionListener.java" 
                :
                "callableSystemAction.java"
            ):
            (
                actionContext ?
                "contextAction.java"
                :
                "cookieAction.java"
            )
        ); // NOI18N
        assert template != null;
        String actionNameKey = "CTL_" + className; // NOI18N
        Map<String,String> replaceTokens = new HashMap<String,String>();
        replaceTokens.put("CLASS_NAME", className); // NOI18N
        replaceTokens.put("PACKAGE_NAME", getPackageName()); // NOI18N
        replaceTokens.put("DISPLAY_NAME_KEY", actionNameKey); // NOI18N
        replaceTokens.put("MODE", getSelectionMode()); // NOI18N
        Set<String> imports = new TreeSet<String>();
        String cName = parseClassName(cookieClasses[0]);
        String cNameVar = Character.toLowerCase(cName.charAt(0)) + cName.substring(1);
        if (!actionContext) {
            imports.addAll(Arrays.asList(HARDCODED_IMPORTS));
        }
        Set<String> addedFQNCs = new TreeSet<String>();
        StringBuffer cookieSB = new StringBuffer();
        if (!alwaysEnabled) {
            for (String cookieClass : cookieClasses) {
                // imports for predefined chosen cookie classes
                if (CLASS_TO_CNB.containsKey(cookieClass)) {
                    addedFQNCs.add(cookieClass);
                }
                // cookie block
                if (cookieSB.length() > 0) {
                    cookieSB.append(", ");
                }
                cookieSB.append(parseClassName(cookieClass) + ".class"); // NOI18N
            }
            replaceTokens.put("COOKIE_CLASSES_BLOCK", cookieSB.toString()); // NOI18N
            replaceTokens.put("CONTEXT_TYPE", multiSelection ? "List<" + cName + ">" : cName);
            String impl;
            if (cookieClasses.length == 1) {
                if (actionContext) {
                    if (multiSelection) {
                        impl = "for (" + cName + ' ' + cNameVar + " : context) {\n" // NOI18N
                                + INDENT_2X + "// TODO use " + cNameVar + "\n" // NOI18N
                                + INDENT + "}"; // NOI18N
                        imports.add("java.util.List"); // NOI18N
                    } else {
                        impl = "// TODO use context";
                    }
                } else {
                    impl = cName + ' ' + cNameVar + " = activatedNodes[0].getLookup().lookup(" + cName + ".class);\n" // NOI18N
                        + INDENT_2X + "// TODO use " + cNameVar; // NOI18N
                }
            } else {
                impl = "// TODO implement action body"; // NOI18N
            }
            replaceTokens.put("PERFORM_ACTION_CODE", impl); // NOI18N
        }
        // imports
        imports.addAll(addedFQNCs);
        StringBuffer importsBuffer = new StringBuffer();
        for (String imprt : imports) {
            importsBuffer.append("import " + imprt + ';' + NEW_LINE); // NOI18N
        }
        replaceTokens.put("IMPORTS", importsBuffer.toString()); // NOI18N
        cmf.add(cmf.createFileWithSubstitutions(actionPath, template, replaceTokens));
        
        // Bundle.properties for localized action name
        String bundlePath = getDefaultPackagePath("Bundle.properties", true);
        cmf.add(cmf.bundleKey(bundlePath, actionNameKey, displayName)); // NOI18N
        
        // Copy action icon
        String relativeIconPath = null;
        if (origIconPath != null && FileUtil.toFileObject(new File(origIconPath)) != null) {
            relativeIconPath = addCreateIconOperation(cmf, origIconPath);
            replaceTokens.put("ICON_RESOURCE_METHOD", DataModel.generateIconResourceMethod(relativeIconPath)); // NOI18N
            replaceTokens.put("INITIALIZE_METHOD", ""); // NOI18N
        } else {
            replaceTokens.put("ICON_RESOURCE_METHOD", ""); // NOI18N
            replaceTokens.put("INITIALIZE_METHOD", DataModel.generateNoIconInitializeMethod()); // NOI18N
        }
        
        if (isToolbarEnabled() && largeIconPath != null) {
            addCreateIconOperation(cmf, largeIconPath);
        }
        
        // add layer entry about the action
        String instanceFullPath = category + "/" // NOI18N
                + dashedFqClassName + ".instance"; // NOI18N
        if (!alwaysEnabled || !actionProxy) {
            if (!actionContext) {
                cmf.add(cmf.createLayerEntry(instanceFullPath, null, null, null, null));
            } else {
                Map<String, Object> attrs = new HashMap<String, Object>();
                attrs.put("instanceCreate", "methodvalue:org.openide.awt.Actions.context"); // NOI18N
                attrs.put("delegate", "methodvalue:org.openide.awt.Actions.inject"); // NOI18N
                attrs.put("injectable", getPackageName() + '.' + className); // NOI18N
                attrs.put("selectionType", multiSelection ? "ANY" : "EXACTLY_ONE"); // NOI18N
                attrs.put("type", fullClassName(cName)); // NOI18N
                attrs.put("noIconInMenu", Boolean.FALSE); // NOI18N
                if (relativeIconPath != null) {
                    attrs.put("iconBase", relativeIconPath); // NOI18N
                }
                attrs.put("displayName", "bundlevalue:" + getPackageName() + ".Bundle#" + actionNameKey); // NOI18N
                cmf.add(
                    cmf.createLayerEntry(
                        instanceFullPath,
                        null,
                        null,
                        null,
                        attrs
                    )
                );
            }
        } else {
            Map<String,Object> attrs = new HashMap<String,Object>();
            attrs.put("instanceCreate", "methodvalue:org.openide.awt.Actions.alwaysEnabled"); // NOI18N
            attrs.put("delegate", "newvalue:" + getPackageName() + '.' + className); // NOI18N
            attrs.put("noIconInMenu", Boolean.FALSE); // NOI18N
            if (relativeIconPath != null) {
                attrs.put("iconBase", relativeIconPath); // NOI18N
            }
            attrs.put("displayName", "bundlevalue:" + getPackageName() + ".Bundle#" + actionNameKey); // NOI18N
            cmf.add(
                cmf.createLayerEntry(
                    instanceFullPath,
                    null,
                    null,
                    null,
                    attrs
                )
            );
        }
        
        // add dependency on util to project.xml
        cmf.add(cmf.addModuleDependency("org.openide.util")); // NOI18N
        if (!alwaysEnabled) {
            cmf.add(cmf.addModuleDependency("org.openide.nodes")); // NOI18N
            for (String fqn : addedFQNCs) {
                cmf.add(cmf.addModuleDependency(CLASS_TO_CNB.get(fqn)));
            }
        }
        
        // create layer entry for global menu item
        if (globalMenuItemEnabled) {
            generateShadowWithOrderAndSeparator(gmiParentMenuPath, shadow,
                    dashedFqClassName, instanceFullPath, gmiSeparatorBefore,
                    gmiSeparatorAfter, gmiPosition);
        }
        
        // create layer entry for toolbar button
        if (toolbarEnabled) {
            generateShadow(toolbar + "/" + shadow, instanceFullPath); // NOI18N
            generateOrder(toolbar, toolbarPosition.getBefore(), shadow, toolbarPosition.getAfter());
        }
        
        // create layer entry for keyboard shortcut
        if (kbShortcutEnabled) {
            String parentPath = "Shortcuts"; // NOI18N
            for (String keyStroke : keyStrokes) {
                generateShadow(parentPath + "/" + keyStroke + ".shadow", instanceFullPath); // NOI18N                
            }
        }
        
        // create file type context menu item
        if (ftContextEnabled) {
            generateShadowWithOrderAndSeparator(ftContextType, shadow,
                    dashedFqClassName, instanceFullPath, ftContextSeparatorBefore,
                    ftContextSeparatorAfter, ftContextPosition);
        }
        
        // create editor context menu item
        if (edContextEnabled) {
            generateShadowWithOrderAndSeparator(edContextType, shadow,
                    dashedFqClassName, instanceFullPath, edContextSeparatorBefore,
                    edContextSeparatorAfter, edContextPosition);
        }
    }
    
    private void generateShadowWithOrderAndSeparator(
            final String parentPath,
            final String shadow,
            final String dashedPkgName,
            final String instanceFullPath,
            final boolean separatorBefore,
            final boolean separatorAfter,
            final Position position) {
        generateShadow(parentPath + "/" + shadow, instanceFullPath); // NOI18N
        generateOrder(parentPath, position.getBefore(), shadow, position.getAfter());
        if (separatorBefore) {
            String sepName = dashedPkgName + "-separatorBefore.instance"; // NOI18N
            generateSeparator(parentPath, sepName);
            generateOrder(parentPath, position.getBefore(), sepName, shadow);
        }
        if (separatorAfter) {
            String sepName = dashedPkgName + "-separatorAfter.instance"; // NOI18N
            generateSeparator(parentPath, sepName);
            generateOrder(parentPath, shadow, sepName, position.getAfter());
        }
    }
    
    /**
     * Just a helper convenient method for cleaner code.
     */
    private void generateOrder(String layerPath, String before, String nue, String after) {
        cmf.add(cmf.orderLayerEntry(layerPath, before, nue, after));
    }
    
    /** Checks whether a proposed class exists. */
    boolean classExists() {
        FileObject classFO = getProject().getProjectDirectory().getFileObject(
                getDefaultPackagePath(className + ".java", false)); // NOI18N
        return classFO != null;
    }
    
    private void generateShadow(final String itemPath, final String origInstance) {
        cmf.add(cmf.createLayerEntry(itemPath, null, null, null, null));
        cmf.add(cmf.createLayerAttribute(itemPath, "originalFile", origInstance)); // NOI18N
    }
    
    CreatedModifiedFiles getCreatedModifiedFiles() {
        if (cmf == null) {
            regenerate();
        }
        return cmf;
    }
    
    private void reset() {
        cmf = null;
    }
    
    void setAlwaysEnabled(boolean alwaysEnabled) {
        this.alwaysEnabled = alwaysEnabled;
    }
    
    boolean isAlwaysEnabled() {
        return alwaysEnabled;
    }
    
    void setCookieClasses(String[] cookieClasses) {
        this.cookieClasses = cookieClasses;
    }
    
    void setMultiSelection(boolean multiSelection) {
        this.multiSelection = multiSelection;
    }
    
    private String getSelectionMode() {
        return multiSelection ? "MODE_ALL" : "MODE_EXACTLY_ONE"; // NOI18N
    }
    
    void setCategory(String category) {
        this.category = category;
    }
    
    void setClassName(String className) {
        reset();
        this.className = className;
    }
    
    public String getClassName() {
        return className;
    }
    
    void setDisplayName(String display) {
        this.displayName = display;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    void setIconPath(String origIconPath) {
        reset();
        this.origIconPath = origIconPath;
    }
    
    public String getIconPath() {
        return origIconPath;
    }
    
    @Override
    public void setPackageName(String pkg) {
        super.setPackageName(pkg);
        reset();
    }
    
    void setGlobalMenuItemEnabled(boolean globalMenuItemEnabled) {
        this.globalMenuItemEnabled = globalMenuItemEnabled;
    }
    
    void setGMIParentMenu(String gmiParentMenuPath) {
        this.gmiParentMenuPath = gmiParentMenuPath;
    }
    
    void setGMISeparatorAfter(boolean gmiSeparatorAfter) {
        this.gmiSeparatorAfter = gmiSeparatorAfter;
    }
    
    void setGMISeparatorBefore(boolean gmiSeparatorBefore) {
        this.gmiSeparatorBefore = gmiSeparatorBefore;
    }
    
    void setGMIPosition(Position position) {
        this.gmiPosition = position;
    }
    
    void setToolbarEnabled(boolean toolbarEnabled) {
        this.toolbarEnabled = toolbarEnabled;
    }
    
    boolean isToolbarEnabled() {
        return toolbarEnabled;
    }
    
    void setToolbar(String toolbar) {
        this.toolbar = toolbar;
    }
    
    void setToolbarPosition(Position position) {
        this.toolbarPosition = position;
    }
    
    void setKeyboardShortcutEnabled(boolean kbShortcutEnabled) {
        this.kbShortcutEnabled = kbShortcutEnabled;
    }
    
    void setKeyStroke(String keyStroke) {
        keyStrokes.add(keyStroke);
    }
    
    void setFileTypeContextEnabled(boolean contextEnabled) {
        this.ftContextEnabled = contextEnabled;
    }
    
    void setFTContextType(String contextType) {
        this.ftContextType = contextType;
    }
    
    void setFTContextPosition(Position position) {
        this.ftContextPosition = position;
    }
    
    void setFTContextSeparatorAfter(boolean separator) {
        this.ftContextSeparatorAfter = separator;
    }
    
    void setFTContextSeparatorBefore(boolean separator) {
        this.ftContextSeparatorBefore = separator;
    }
    
    void setEditorContextEnabled(boolean contextEnabled) {
        this.edContextEnabled = contextEnabled;
    }
    
    void setEdContextType(String contextType) {
        this.edContextType = contextType;
    }
    
    void setEdContextPosition(Position position) {
        this.edContextPosition = position;
    }
    
    void setEdContextSeparatorAfter(boolean separator) {
        this.edContextSeparatorAfter = separator;
    }
    
    void setEdContextSeparatorBefore(boolean separator) {
        this.edContextSeparatorBefore = separator;
    }
    
    static final class Position {
        
        private String before;
        private String after;
        private String beforeName;
        private String afterName;
        
        Position(String before, String after) {
            this(before, after, null, null);
        }
        
        Position(String before, String after, String beforeName, String afterName) {
            this.before = before;
            this.after = after;
            this.beforeName = beforeName;
            this.afterName = afterName;
        }
        
        String getBefore() {
            return before;
        }
        
        String getAfter() {
            return after;
        }
        
        String getBeforeName() {
            return beforeName;
        }
        
        String getAfterName() {
            return afterName;
        }
    }
    
    private void generateSeparator(final String parentPath, final String sepName) {
        String sepPath = parentPath + "/" + sepName; // NOI18N
        cmf.add(cmf.createLayerEntry(sepPath,
                null, null, null, null));
        cmf.add(cmf.createLayerAttribute(sepPath, "instanceClass", // NOI18N
                "javax.swing.JSeparator")); // NOI18N
    }

    static String fullClassName(String type) {
        if (type.contains(".")) {
            return type;
        }
        for (String s : PREDEFINED_COOKIE_CLASSES) {
            if (s.endsWith(type)) {
                return s;
            }
        }
        return type;
    }
    
    /**
     * Parse class name from a fully qualified class name. If the given name
     * doesn't contain dot (<em>.</em>), given parameter is returned.
     */
    static String parseClassName(final String name) {
        int lastDot = name.lastIndexOf('.');
        return lastDot == -1 ? name : name.substring(lastDot + 1);
    }
    
    private static String generateIconResourceMethod(final String relativeIconPath) {
        return NEW_LINE +
                INDENT + "@Override" + NEW_LINE + // NOI18N
                INDENT + "protected String iconResource() {" + NEW_LINE + // NOI18N
                INDENT_2X + "return \"" + relativeIconPath + "\";" + NEW_LINE + // NOI18N
                INDENT + "}"; // NOI18N
    }
    
    private static String generateNoIconInitializeMethod() {
        return "@Override" + NEW_LINE + // NOI18N
                INDENT + "protected void initialize() {" + NEW_LINE + // NOI18N
                INDENT_2X + "super.initialize();" + NEW_LINE + // NOI18N
                INDENT_2X + "// see org.openide.util.actions.SystemAction.iconResource() Javadoc for more details" + NEW_LINE + // NOI18N
                INDENT_2X + "putValue(\"noIconInMenu\", Boolean.TRUE);" + NEW_LINE + // NOI18N
                INDENT + "}" + NEW_LINE; // NOI18N
    }

    public void setLargeIconPath(String largeIconPath) {
        this.largeIconPath = largeIconPath;
    }
    
}

