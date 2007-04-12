/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.freeform;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Reads/writes project.xml.
 * Handling of /1 vs. /2 namespace: either namespace can be read;
 * when writing, attempts to keep existing namespace when possible, but
 * will always write a /2 namespace when it is necessary (for isTests or javadoc).
 * @author  Jesse Glick, David Konecny, Pavel Buzek
 */
public class JavaProjectGenerator {

    /** Keep root elements in the order specified by project's XML schema. */
    private static final String[] rootElementsOrder = new String[]{"name", "properties", "folders", "ide-actions", "export", "view", "subprojects"}; // NOI18N
    private static final String[] viewElementsOrder = new String[]{"items", "context-menu"}; // NOI18N
    
    // this order is not required by schema, but follow it to minimize randomness a bit
    private static final String[] folderElementsOrder = new String[]{"source-folder", "build-folder"}; // NOI18N
    private static final String[] viewItemElementsOrder = new String[]{"source-folder", "source-file"}; // NOI18N
    
    /**
     * Structure describing source folder.
     * Data in the struct are in the same format as they are stored in XML.
     * Beware that when used in <folders> you must specify label, location, and optional type;
     * in <view><items>, you must specify label, location, and style. So if you are switching
     * from the latter to the former you must add style; if vice-versa, you may need to add type.
     */
    public static final class SourceFolder {
        public SourceFolder() {}
        public String label;
        public String type;
        public String location;
        public String style;
        public String includes;
        public String excludes;
        public String encoding;
        public String toString() {
            return "FPG.SF[label=" + label + ",type=" + type + ",location=" + location + ",style=" + style + ",includes=" + includes + ",excludes=" + excludes + ",encoding=" + encoding + "]"; // NOI18N
        }
    }

    /**
     * Read source folders from the project.
     * @param helper AntProjectHelper instance
     * @param type type of source folders to be read. Can be null in which case
     *    all types will be read. Useful for reading one type of source folders.
     *    Source folders without type are read only when type == null.
     * @return list of SourceFolder instances; style value will be always null
     */
    public static List<SourceFolder> getSourceFolders(AntProjectHelper helper, String type) {
        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        List<SourceFolder> list = new ArrayList<SourceFolder>();
        Element data = Util.getPrimaryConfigurationData(helper);
        Element foldersEl = Util.findElement(data, "folders", Util.NAMESPACE); // NOI18N
        if (foldersEl == null) {
            return list;
        }
        for (Element sourceFolderEl : Util.findSubElements(foldersEl)) {
            if (!sourceFolderEl.getLocalName().equals("source-folder")) { // NOI18N
                continue;
            }
            SourceFolder sf = new SourceFolder();
            Element el = Util.findElement(sourceFolderEl, "label", Util.NAMESPACE); // NOI18N
            if (el != null) {
                sf.label = Util.findText(el);
            }
            el = Util.findElement(sourceFolderEl, "type", Util.NAMESPACE); // NOI18N
            if (el != null) {
                sf.type = Util.findText(el);
            }
            el = Util.findElement(sourceFolderEl, "location", Util.NAMESPACE); // NOI18N
            if (el != null) {
                sf.location = Util.findText(el);
            }
            el = Util.findElement(sourceFolderEl, "includes", Util.NAMESPACE); // NOI18N
            if (el != null) {
                sf.includes = Util.findText(el);
            }
            el = Util.findElement(sourceFolderEl, "excludes", Util.NAMESPACE); // NOI18N
            if (el != null) {
                sf.excludes = Util.findText(el);
            }
            el = Util.findElement(sourceFolderEl, "encoding", Util.NAMESPACE); // NOI18N
            if (el != null) {
                sf.encoding = Util.findText(el);
            }
            if (type == null || type.equals(sf.type)) {
                if (sf.label == null || sf.label.length() == 0) {
                    throw new IllegalArgumentException("label element is empty or not specified. "+helper.getProjectDirectory()); // NOI18N
                }
                if (sf.location == null || sf.location.length() == 0) {
                    throw new IllegalArgumentException("location element is empty or not specified. "+helper.getProjectDirectory()); // NOI18N
                }
                list.add(sf);
            }
        }
        return list;
    }

    /**
     * Update source folders of the project. Project is left modified and you 
     * must save it explicitely.
     * @param helper AntProjectHelper instance
     * @param sources list of SourceFolder instances
     * @param type type of source folders to update. 
     *    Can be null in which case all types will be overriden.
     *    Useful for overriding just one type of source folders. Source folders
     *    without type are overriden only when type == null.
     */
    public static void putSourceFolders(AntProjectHelper helper, List<SourceFolder> sources, String type) {
        //assert ProjectManager.mutex().isWriteAccess();
        Element data = Util.getPrimaryConfigurationData(helper);
        Document doc = data.getOwnerDocument();
        Element foldersEl = Util.findElement(data, "folders", Util.NAMESPACE); // NOI18N
        if (foldersEl == null) {
            foldersEl = doc.createElementNS(Util.NAMESPACE, "folders"); // NOI18N
            Util.appendChildElement(data, foldersEl, rootElementsOrder);
        } else {
            for (Element sourceFolderEl : Util.findSubElements(foldersEl)) {
                if (!sourceFolderEl.getLocalName().equals("source-folder")) { // NOI18N
                    continue;
                }
                if (type == null) {
                    foldersEl.removeChild(sourceFolderEl);
                } else {
                    Element typeEl = Util.findElement(sourceFolderEl, "type", Util.NAMESPACE); // NOI18N
                    if (typeEl != null) {
                        String typeElValue = Util.findText(typeEl);
                        if (type.equals(typeElValue)) {
                            foldersEl.removeChild(sourceFolderEl);
                        }
                    }
                }
            }
        }
        for (SourceFolder sf : sources) {
            Element sourceFolderEl = doc.createElementNS(Util.NAMESPACE, "source-folder"); // NOI18N
            Element el;
            if (sf.label != null && sf.label.length() > 0) {
                el = doc.createElementNS(Util.NAMESPACE, "label"); // NOI18N
                el.appendChild(doc.createTextNode(sf.label)); // NOI18N
                sourceFolderEl.appendChild(el);
            } else {
                throw new IllegalArgumentException("label cannot be empty. "+helper.getProjectDirectory()); // NOI18N
            }
            if (sf.type != null) {
                el = doc.createElementNS(Util.NAMESPACE, "type"); // NOI18N
                el.appendChild(doc.createTextNode(sf.type)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            if (sf.location != null && sf.location.length() > 0) {
                el = doc.createElementNS(Util.NAMESPACE, "location"); // NOI18N
                el.appendChild(doc.createTextNode(sf.location)); // NOI18N
                sourceFolderEl.appendChild(el);
            } else {
                throw new IllegalArgumentException("location cannot be empty. "+helper.getProjectDirectory()); // NOI18N
            }
            if (sf.includes != null) {
                el = doc.createElementNS(Util.NAMESPACE, "includes"); // NOI18N
                el.appendChild(doc.createTextNode(sf.includes)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            if (sf.excludes != null) {
                el = doc.createElementNS(Util.NAMESPACE, "excludes"); // NOI18N
                el.appendChild(doc.createTextNode(sf.excludes)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            if (sf.encoding != null) {
                el = doc.createElementNS(Util.NAMESPACE, "encoding"); // NOI18N
                el.appendChild(doc.createTextNode(sf.encoding)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            Util.appendChildElement(foldersEl, sourceFolderEl, folderElementsOrder);
        }
        Util.putPrimaryConfigurationData(helper, data);
    }
    
    /**
     * Read source views from the project. At the moment only source-folder
     * elements are read and source-file ones are ignored.
     * @param helper AntProjectHelper instance
     * @param style style of source folders to be read. Can be null in which case
     *    all styles will be read. Useful for reading one style of source folders.
     * @return list of SourceFolder instances; type value will be always null
     */
    public static List getSourceViews(AntProjectHelper helper, String style) {
        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        List<SourceFolder> list = new ArrayList<SourceFolder>();
        Element data = Util.getPrimaryConfigurationData(helper);
        Element viewEl = Util.findElement(data, "view", Util.NAMESPACE); // NOI18N
        if (viewEl == null) {
            return list;
        }
        Element itemsEl = Util.findElement(viewEl, "items", Util.NAMESPACE); // NOI18N
        if (itemsEl == null) {
            return list;
        }
        for (Element sourceFolderEl : Util.findSubElements(itemsEl)) {
            if (!sourceFolderEl.getLocalName().equals("source-folder")) { // NOI18N
                continue;
            }
            SourceFolder sf = new SourceFolder();
            sf.style = sourceFolderEl.getAttribute("style"); // NOI18N
            assert sf.style != null && sf.style.length() > 0 : "Bad style attr on <source-folder> in " + helper; // NOI18N
            Element el = Util.findElement(sourceFolderEl, "label", Util.NAMESPACE); // NOI18N
            if (el != null) {
                sf.label = Util.findText(el);
            }
            el = Util.findElement(sourceFolderEl, "location", Util.NAMESPACE); // NOI18N
            if (el != null) {
                sf.location = Util.findText(el);
            }
            el = Util.findElement(sourceFolderEl, "includes", Util.NAMESPACE); // NOI18N
            if (el != null) {
                sf.includes = Util.findText(el);
            }
            el = Util.findElement(sourceFolderEl, "excludes", Util.NAMESPACE); // NOI18N
            if (el != null) {
                sf.excludes = Util.findText(el);
            }
            if (style == null || style.equals(sf.style)) {
                list.add(sf);
            }
        }
        return list;
    }
    
    /**
     * Update source views of the project. 
     * This method should be called always after the putSourceFolders method
     * to keep views and folders in sync.
     * Project is left modified and you must save it explicitely.
     * @param helper AntProjectHelper instance
     * @param sources list of SourceFolder instances
     * @param style style of source views to update. 
     *    Can be null in which case all styles will be overriden.
     *    Useful for overriding just one style of source view.
     */
    public static void putSourceViews(AntProjectHelper helper, List<SourceFolder> sources, String style) {
        //assert ProjectManager.mutex().isWriteAccess();
        ArrayList list = new ArrayList();
        Element data = Util.getPrimaryConfigurationData(helper);
        Document doc = data.getOwnerDocument();
        Element viewEl = Util.findElement(data, "view", Util.NAMESPACE); // NOI18N
        if (viewEl == null) {
            viewEl = doc.createElementNS(Util.NAMESPACE, "view"); // NOI18N
            Util.appendChildElement(data, viewEl, rootElementsOrder);
        }
        Element itemsEl = Util.findElement(viewEl, "items", Util.NAMESPACE); // NOI18N
        if (itemsEl == null) {
            itemsEl = doc.createElementNS(Util.NAMESPACE, "items"); // NOI18N
            Util.appendChildElement(viewEl, itemsEl, viewElementsOrder);
        }
        List<Element> sourceViews = Util.findSubElements(itemsEl);
        Iterator it = sourceViews.iterator();
        while (it.hasNext()) {
            Element sourceViewEl = (Element)it.next();
            if (!sourceViewEl.getLocalName().equals("source-folder")) { // NOI18N
                continue;
            }
            String sourceStyle = sourceViewEl.getAttribute("style"); // NOI18N
            if (style == null || style.equals(sourceStyle)) {
                itemsEl.removeChild(sourceViewEl);
            }
        }
        Iterator it2 = sources.iterator();
        while (it2.hasNext()) {
            SourceFolder sf = (SourceFolder)it2.next();
            if (sf.style == null || sf.style.length() == 0) {
                // perhaps this is principal source folder?
                continue;
            }
            Element sourceFolderEl = doc.createElementNS(Util.NAMESPACE, "source-folder"); // NOI18N
            sourceFolderEl.setAttribute("style", sf.style); // NOI18N
            Element el;
            if (sf.label != null) {
                el = doc.createElementNS(Util.NAMESPACE, "label"); // NOI18N
                el.appendChild(doc.createTextNode(sf.label)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            if (sf.location != null) {
                el = doc.createElementNS(Util.NAMESPACE, "location"); // NOI18N
                el.appendChild(doc.createTextNode(sf.location)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            if (sf.includes != null) {
                el = doc.createElementNS(Util.NAMESPACE, "includes"); // NOI18N
                el.appendChild(doc.createTextNode(sf.includes)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            if (sf.excludes != null) {
                el = doc.createElementNS(Util.NAMESPACE, "excludes"); // NOI18N
                el.appendChild(doc.createTextNode(sf.excludes)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            Util.appendChildElement(itemsEl, sourceFolderEl, viewItemElementsOrder);
        }
        Util.putPrimaryConfigurationData(helper, data);
    }
    
    
    /**
     * Read Java compilation units from the project.
     * @param helper AntProjectHelper instance
     * @param aux AuxiliaryConfiguration instance
     * @return list of JavaCompilationUnit instances; never null;
     */
    public static List<JavaCompilationUnit> getJavaCompilationUnits(
            AntProjectHelper helper, AuxiliaryConfiguration aux) {
        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        List<JavaCompilationUnit> list = new ArrayList<JavaCompilationUnit>();
        Element data = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_2, true);
        if (data == null) {
            data = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_1, true);
        }
        if (data == null) {
            return list;
        }
        for (Element cuEl : Util.findSubElements(data)) {
            JavaCompilationUnit cu = new JavaCompilationUnit();
            List<String> outputs = new ArrayList<String>();
            List<String> javadoc = new ArrayList<String>();
            List<JavaCompilationUnit.CP> cps = new ArrayList<JavaCompilationUnit.CP>();
            List<String> packageRoots = new ArrayList<String>();
            for (Element el : Util.findSubElements(cuEl)) {
                if (el.getLocalName().equals("package-root")) { // NOI18N
                    packageRoots.add(Util.findText(el));
                    continue;
                }
                if (el.getLocalName().equals("classpath")) { // NOI18N
                    JavaCompilationUnit.CP cp = new JavaCompilationUnit.CP();
                    cp.classpath = Util.findText(el);
                    cp.mode = el.getAttribute("mode"); // NOI18N
                    if (cp.mode != null && cp.classpath != null) {
                        cps.add(cp);
                    }
                    continue;
                }
                if (el.getLocalName().equals("built-to")) { // NOI18N
                    outputs.add(Util.findText(el));
                    continue;
                }
                if (el.getLocalName().equals("javadoc-built-to")) { // NOI18N
                    javadoc.add(Util.findText(el));
                    continue;
                }
                if (el.getLocalName().equals("source-level")) { // NOI18N
                    cu.sourceLevel = Util.findText(el);
                }
                if (el.getLocalName().equals("unit-tests")) { // NOI18N
                    cu.isTests = true;
                }
            }
            cu.output = outputs.size() > 0 ? outputs : null;
            cu.javadoc = javadoc.size() > 0 ? javadoc : null;
            cu.classpath = cps.size() > 0 ? cps: null;
            cu.packageRoots = packageRoots.size() > 0 ? packageRoots: null;
            list.add(cu);
        }
        return list;
    }

    /**
     * Update Java compilation units of the project. Project is left modified
     * and you must save it explicitely.
     * @param helper AntProjectHelper instance
     * @param aux AuxiliaryConfiguration instance
     * @param compUnits list of JavaCompilationUnit instances
     */
    public static void putJavaCompilationUnits(AntProjectHelper helper, 
            AuxiliaryConfiguration aux, List<JavaCompilationUnit> compUnits) {
        //assert ProjectManager.mutex().isWriteAccess();
        // First check whether we need /2 data.
        boolean need2 = false;
        for (JavaCompilationUnit unit : compUnits) {
            if (unit.isTests || (unit.javadoc != null && !unit.javadoc.isEmpty())) {
                need2 = true;
                break;
            }
        }
        String namespace;
        // Look for existing /2 data.
        Element data = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_2, true);
        if (data != null) {
            // Fine, use it as is.
            namespace = JavaProjectNature.NS_JAVA_2;
        } else {
            // Or, for existing /1 data.
            namespace = need2 ? JavaProjectNature.NS_JAVA_2 : JavaProjectNature.NS_JAVA_1;
            data = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_1, true);
            if (data != null) {
                if (need2) {
                    // Have to upgrade.
                    aux.removeConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_1, true);
                    data = Util.getPrimaryConfigurationData(helper).getOwnerDocument().
                        createElementNS(JavaProjectNature.NS_JAVA_2, JavaProjectNature.EL_JAVA);
                } // else can use it as is
            } else {
                // Create /1 or /2 data acc. to need.
                data = Util.getPrimaryConfigurationData(helper).getOwnerDocument().
                    createElementNS(namespace, JavaProjectNature.EL_JAVA);
            }
        }
        Document doc = data.getOwnerDocument();
        for (Element cuEl : Util.findSubElements(data)) {
            data.removeChild(cuEl);
        }
        for (JavaCompilationUnit cu : compUnits) {
            Element cuEl = doc.createElementNS(namespace, "compilation-unit"); // NOI18N
            data.appendChild(cuEl);
            Element el;
            if (cu.packageRoots != null) {
                for (String packageRoot : cu.packageRoots) {
                    el = doc.createElementNS(namespace, "package-root"); // NOI18N
                    el.appendChild(doc.createTextNode(packageRoot));
                    cuEl.appendChild(el);
                }
            }
            if (cu.isTests) {
                assert namespace.equals(JavaProjectNature.NS_JAVA_2);
                cuEl.appendChild(doc.createElementNS(namespace, "unit-tests")); // NOI18N
            }
            if (cu.classpath != null) {
                for (JavaCompilationUnit.CP cp : cu.classpath) {
                    el = doc.createElementNS(namespace, "classpath"); // NOI18N
                    el.appendChild(doc.createTextNode(cp.classpath));
                    el.setAttribute("mode", cp.mode); // NOI18N
                    cuEl.appendChild(el);
                }
            }
            if (cu.output != null) {
                Iterator it3 = cu.output.iterator();
                while (it3.hasNext()) {
                    String output = (String)it3.next();
                    el = doc.createElementNS(namespace, "built-to"); // NOI18N
                    el.appendChild(doc.createTextNode(output));
                    cuEl.appendChild(el);
                }
            }
            if (cu.javadoc != null) {
                Iterator it3 = cu.javadoc.iterator();
                while (it3.hasNext()) {
                    String javadoc = (String) it3.next();
                    assert namespace.equals(JavaProjectNature.NS_JAVA_2);
                    el = doc.createElementNS(namespace, "javadoc-built-to"); // NOI18N
                    el.appendChild(doc.createTextNode(javadoc));
                    cuEl.appendChild(el);
                }
            }
            if (cu.sourceLevel != null) {
                el = doc.createElementNS(namespace, "source-level"); // NOI18N
                el.appendChild(doc.createTextNode(cu.sourceLevel));
                cuEl.appendChild(el);
            }
        }
        aux.putConfigurationFragment(data, true);
    }

    
    /**
     * Structure describing compilation unit.
     * Data in the struct are in the same format as they are stored in XML.
     */
    public static final class JavaCompilationUnit {
        public List<String> packageRoots;
        public List<CP> classpath;
        public List<String> output;
        public List<String> javadoc;
        public String sourceLevel;
        public boolean isTests;
        
        public String toString() {
            return "FPG.JCU[packageRoots=" + packageRoots + ", classpath=" + classpath + ", output=" + output + ", javadoc=" + javadoc + ", sourceLevel=" + sourceLevel + ",isTests=" + isTests + "]"; // NOI18N
        }
        
        public static final class CP {
            public String classpath;
            public String mode;
            
            public String toString() {
                return "FPG.JCU.CP:[classpath="+classpath+", mode="+mode+", this="+super.toString()+"]"; // NOI18N
            }
            
        }
        
    }
    
    /**
     * Structure describing one export record.
     * Data in the struct are in the same format as they are stored in XML.
     */
    public static final class Export {
        public String type;
        public String location;
        public String script; // optional
        public String buildTarget;
        public String cleanTarget; // optional
    }

    /**
     * Try to guess project's exports. See issue #49221 for more details.
     */
    public static List<Export> guessExports(PropertyEvaluator evaluator, File baseFolder,
            List<TargetMapping> targetMappings, List<JavaCompilationUnit> javaCompilationUnits) {
        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        List<Export> exports = new ArrayList<Export>();
        String targetName = null;
        String scriptName = null;
        for (TargetMapping tm : targetMappings) {
            if (tm.name.equals("build")) { // NOI18N
                if (tm.targets.size() == 1) {
                    targetName = tm.targets.get(0);
                    scriptName = tm.script;
                } else {
                    return new ArrayList<Export>();
                }
            }
        }
        if (targetName == null) {
            return new ArrayList<Export>();
        }
        for (JavaCompilationUnit cu : javaCompilationUnits) {
            if (cu.output != null) {
                for (String output : cu.output) {
                    String output2 = evaluator.evaluate(output);
                    if (output2.endsWith(".jar")) { // NOI18N
                        Export e = new Export();
                        e.type = JavaProjectConstants.ARTIFACT_TYPE_JAR;
                        e.location = output;
                        e.script = scriptName;
                        e.buildTarget = targetName;
                        exports.add(e);
                    }
                    else if (isFolder(evaluator, baseFolder, output2)) {
                        Export e = new Export();
                        e.type = JavaProjectConstants.ARTIFACT_TYPE_FOLDER;
                        e.location = output;
                        e.script = scriptName;
                        e.buildTarget = targetName;
                        exports.add(e);
                    }
                }
            }
        }
        return exports;
    }
    
    /**
     * Update exports of the project. 
     * Project is left modified and you must save it explicitely.
     * @param helper AntProjectHelper instance
     * @param exports list of Export instances
     */
    public static void putExports(AntProjectHelper helper, List<Export> exports) {
        //assert ProjectManager.mutex().isWriteAccess();
        ArrayList list = new ArrayList();
        Element data = Util.getPrimaryConfigurationData(helper);
        Document doc = data.getOwnerDocument();
        Iterator it = Util.findSubElements(data).iterator();
        while (it.hasNext()) {
            Element exportEl = (Element)it.next();
            if (!exportEl.getLocalName().equals("export")) { // NOI18N
                continue;
            }
            data.removeChild(exportEl);
        }
        Iterator it2 = exports.iterator();
        while (it2.hasNext()) {
            Export export = (Export)it2.next();
            Element exportEl = doc.createElementNS(Util.NAMESPACE, "export"); // NOI18N
            Element el;
            el = doc.createElementNS(Util.NAMESPACE, "type"); // NOI18N
            el.appendChild(doc.createTextNode(export.type)); // NOI18N
            exportEl.appendChild(el);
            el = doc.createElementNS(Util.NAMESPACE, "location"); // NOI18N
            el.appendChild(doc.createTextNode(export.location)); // NOI18N
            exportEl.appendChild(el);
            if (export.script != null) {
                el = doc.createElementNS(Util.NAMESPACE, "script"); // NOI18N
                el.appendChild(doc.createTextNode(export.script)); // NOI18N
                exportEl.appendChild(el);
            }
            el = doc.createElementNS(Util.NAMESPACE, "build-target"); // NOI18N
            el.appendChild(doc.createTextNode(export.buildTarget)); // NOI18N
            exportEl.appendChild(el);
            if (export.cleanTarget != null) {
                el = doc.createElementNS(Util.NAMESPACE, "clean-target"); // NOI18N
                el.appendChild(doc.createTextNode(export.cleanTarget)); // NOI18N
                exportEl.appendChild(el);
            }
            Util.appendChildElement(data, exportEl, rootElementsOrder);
        }
        Util.putPrimaryConfigurationData(helper, data);
    }
    
    /**
     * Try to guess project's subprojects. See issue #49640 for more details.
     */
    public static List<String> guessSubprojects(PropertyEvaluator evaluator,
            List<JavaCompilationUnit> javaCompilationUnits, File projectBase, File freeformBase) {
        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        Set<String> subprojs = new HashSet<String>();
        for (JavaCompilationUnit cu : javaCompilationUnits) {
            if (cu.classpath != null) {
                for (JavaCompilationUnit.CP cp : cu.classpath) {
                    if (!"compile".equals(cp.mode))  { // NOI18N
                        continue;
                    }
                    String classpath = evaluator.evaluate(cp.classpath);
                    if (classpath == null) {
                        continue;
                    }
                    for (String s : PropertyUtils.tokenizePath(classpath)) {
                        File file = FileUtil.normalizeFile(new File(s));
                        AntArtifact aa = AntArtifactQuery.findArtifactFromFile(file);
                        if (aa != null) {
                            File proj = FileUtil.toFile(aa.getProject().getProjectDirectory());
                            String p = Util.relativizeLocation(projectBase, freeformBase, proj);
                            subprojs.add(p);
                        }
                    }
                }
            }
        }
        return new ArrayList<String>(subprojs);
    }
    
    /**
     * Update subprojects of the project. 
     * Project is left modified and you must save it explicitely.
     * @param helper AntProjectHelper instance
     * @param subprojects list of paths to subprojects
     */
    public static void putSubprojects(AntProjectHelper helper, List<String> subprojects) {
        //assert ProjectManager.mutex().isWriteAccess();
        ArrayList list = new ArrayList();
        Element data = Util.getPrimaryConfigurationData(helper);
        Document doc = data.getOwnerDocument();
        Element subproject = Util.findElement(data, "subprojects", Util.NAMESPACE); // NOI18N
        if (subproject != null) {
            data.removeChild(subproject);
        }
        subproject = doc.createElementNS(Util.NAMESPACE, "subprojects"); // NOI18N
        Util.appendChildElement(data, subproject, rootElementsOrder);
        
        Iterator it = subprojects.iterator();
        while (it.hasNext()) {
            String proj = (String)it.next();
            Element projEl = doc.createElementNS(Util.NAMESPACE, "project"); // NOI18N
            projEl.appendChild(doc.createTextNode(proj));
            subproject.appendChild(projEl);
        }
        Util.putPrimaryConfigurationData(helper, data);
    }
    
    /**
     * Try to guess project's build folders. See issue #50934 for more details.
     */
    public static List<String> guessBuildFolders(PropertyEvaluator evaluator,
            List<JavaCompilationUnit> javaCompilationUnits, File projectBase, File freeformBase) {
        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        List<String> buildFolders = new ArrayList<String>();
        for (JavaCompilationUnit cu : javaCompilationUnits) {
            if (cu.output != null) {
                for (String output : cu.output) {
                    File f = Util.resolveFile(evaluator, freeformBase, output);
                    if (f.exists()) {
                        if (f.isFile()) {
                            f = f.getParentFile();
                        }
                    } else {
                        // guess: if name contains dot then it is probably file
                        if (f.getName().indexOf('.') != -1) {
                            f = f.getParentFile();
                        }
                    }
                    output = f.getAbsolutePath();
                    if (!output.endsWith(File.separator)) {
                        output += File.separatorChar;
                    }

                    if (output.startsWith(projectBase.getAbsolutePath()+File.separatorChar) ||
                        output.startsWith(freeformBase.getAbsolutePath()+File.separatorChar)) {
                        // ignore output which lies below project base or freeform base
                        continue;
                    }
                    boolean add = true;
                    Iterator<String> it = buildFolders.iterator();
                    while (it.hasNext()) {
                        String path = it.next();
                        if (!path.endsWith(File.separator)) {
                            path += File.separatorChar;
                        }
                        if (path.equals(output)) {
                            // such a path is already there
                            add = false;
                            break;
                        } else if (output.startsWith(path)) {
                            // such a patch is already there
                            add = false;
                            break;
                        } else if (path.startsWith(output)) {
                            it.remove();
                        }
                    }
                    if (add) {
                        buildFolders.add(f.getAbsolutePath());
                    }
                }
            }
        }
        return buildFolders;
    }
    
    /**
     * Update build folders of the project. 
     * Project is left modified and you must save it explicitely.
     * @param helper AntProjectHelper instance
     * @param buildFolders list of build folder locations
     */
    public static void putBuildFolders(AntProjectHelper helper, List<String> buildFolders) {
        //assert ProjectManager.mutex().isWriteAccess();
        ArrayList list = new ArrayList();
        Element data = Util.getPrimaryConfigurationData(helper);
        Document doc = data.getOwnerDocument();
        Element foldersEl = Util.findElement(data, "folders", Util.NAMESPACE); // NOI18N
        if (foldersEl == null) {
            foldersEl = doc.createElementNS(Util.NAMESPACE, "folders"); // NOI18N
            Util.appendChildElement(data, foldersEl, rootElementsOrder);
        } else {
            List<Element> folders = Util.findSubElements(foldersEl);
            Iterator it = folders.iterator();
            while (it.hasNext()) {
                Element buildFolderEl = (Element)it.next();
                if (!buildFolderEl.getLocalName().equals("build-folder")) { // NOI18N
                    continue;
                }
                foldersEl.removeChild(buildFolderEl);
            }
        }
        Iterator it = buildFolders.iterator();
        while (it.hasNext()) {
            String location = (String)it.next();
            Element buildFolderEl = doc.createElementNS(Util.NAMESPACE, "build-folder"); // NOI18N
            Element locationEl = doc.createElementNS(Util.NAMESPACE, "location"); // NOI18N
            locationEl.appendChild(doc.createTextNode(location));
            buildFolderEl.appendChild(locationEl);
            Util.appendChildElement(foldersEl, buildFolderEl, folderElementsOrder);
        }
        Util.putPrimaryConfigurationData(helper, data);
    }

    // XXX: copy&pasted from FreeformProjectGenerator
    /**
     * Read target mappings from project.
     * @param helper AntProjectHelper instance
     * @return list of TargetMapping instances
     */
    public static List<TargetMapping> getTargetMappings(AntProjectHelper helper) {
        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        List<TargetMapping> list = new ArrayList<TargetMapping>();
        Element genldata = Util.getPrimaryConfigurationData(helper);
        Element actionsEl = Util.findElement(genldata, "ide-actions", Util.NAMESPACE); // NOI18N
        if (actionsEl == null) {
            return list;
        }
        for (Element actionEl : Util.findSubElements(actionsEl)) {
            TargetMapping tm = new TargetMapping();
            tm.name = actionEl.getAttribute("name"); // NOI18N
            List<String> targetNames = new ArrayList<String>();
            EditableProperties props = new EditableProperties(false);
            for (Element subEl : Util.findSubElements(actionEl)) {
                if (subEl.getLocalName().equals("target")) { // NOI18N
                    targetNames.add(Util.findText(subEl));
                    continue;
                }
                if (subEl.getLocalName().equals("script")) { // NOI18N
                    tm.script = Util.findText(subEl);
                    continue;
                }
                if (subEl.getLocalName().equals("context")) { // NOI18N
                    TargetMapping.Context ctx = new TargetMapping.Context();
                    for (Element contextSubEl : Util.findSubElements(subEl)) {
                        if (contextSubEl.getLocalName().equals("property")) { // NOI18N
                            ctx.property = Util.findText(contextSubEl);
                            continue;
                        }
                        if (contextSubEl.getLocalName().equals("format")) { // NOI18N
                            ctx.format = Util.findText(contextSubEl);
                            continue;
                        }
                        if (contextSubEl.getLocalName().equals("folder")) { // NOI18N
                            ctx.folder = Util.findText(contextSubEl);
                            continue;
                        }
                        if (contextSubEl.getLocalName().equals("pattern")) { // NOI18N
                            ctx.pattern = Util.findText(contextSubEl);
                            continue;
                        }
                        if (contextSubEl.getLocalName().equals("arity")) { // NOI18N
                            Element sepFilesEl = Util.findElement(contextSubEl, "separated-files", Util.NAMESPACE); // NOI18N
                            if (sepFilesEl != null) {
                                ctx.separator = Util.findText(sepFilesEl);
                            }
                            continue;
                        }
                    }
                    tm.context = ctx;
                }
                if (subEl.getLocalName().equals("property")) { // NOI18N
                    readProperty(subEl, props);
                    continue;
                }
            }
            tm.targets = targetNames;
            if (props.keySet().size() > 0) {
                tm.properties = props;
            }
            list.add(tm);
        }
        return list;
    }
    
    
    /*package private*/ static boolean isFolder (PropertyEvaluator eval, File baseFolder, String folder) {
        File f = Util.resolveFile(eval, baseFolder, folder);
        if (f != null && f.isDirectory()) {
            return true;
        }
        int dotIndex = folder.lastIndexOf('.');    //NOI18N
        int slashIndex = folder.lastIndexOf('/');  //NOI18N
        return dotIndex == -1 || (dotIndex < slashIndex) ;
    }
    
    /**
     * Structure describing target mapping.
     * Data in the struct are in the same format as they are stored in XML.
     */
    public static final class TargetMapping {
        public String script;
        public List<String> targets;
        public String name;
        public EditableProperties properties;
        public Context context; // may be null
        
        public static final class Context {
            public String property;
            public String format;
            public String folder;
            public String pattern; // may be null
            public String separator; // may be null
        }
    }
    
    private static void readProperty(Element propertyElement, EditableProperties props) {
        String key = propertyElement.getAttribute("name"); // NOI18N
        String value = Util.findText(propertyElement);
        props.setProperty(key, value);
    }

}
