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

package org.netbeans.nbbuild;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Scans for known modules.
 * Precise algorithm summarized in issue #42681 and issue #58966.
 * @author Jesse Glick
 */
final class ModuleListParser {

    private static Map<File,Map<String,Entry>> SOURCE_SCAN_CACHE = new HashMap<File,Map<String,Entry>>();
    private static Map<File,Map<String,Entry>> SUITE_SCAN_CACHE = new HashMap<File,Map<String,Entry>>();
    private static Map<File,Entry> STANDALONE_SCAN_CACHE = new HashMap<File,Entry>();
    private static Map<File,Map<String,Entry>> BINARY_SCAN_CACHE = new HashMap<File,Map<String,Entry>>();
    
    /** Clear caches. Cf. #71130. */
    public static void resetCaches() {
        SOURCE_SCAN_CACHE.clear();
        SUITE_SCAN_CACHE.clear();
        STANDALONE_SCAN_CACHE.clear();
        BINARY_SCAN_CACHE.clear();
    }
    
    /** Synch with org.netbeans.modules.apisupport.project.universe.ModuleList.FOREST: */
    private static final String[] FOREST = {
        /*root*/null,
        "contrib",
        // do not scan in misc; any real modules would have been put in contrib
        // Will there be other subtrees in the future (not using suites)?
    };
    /**
     * Find all NBM projects in a root, possibly from cache.
     */
    private static Map<String,Entry> scanNetBeansOrgSources(File root, Map<String,String> properties, Project project) throws IOException {
        Map<String,Entry> entries = SOURCE_SCAN_CACHE.get(root);
        if (entries == null) {
            // Similar to #62221: if just invoked from a module in standard clusters, only scan those clusters (faster):
            Set<String> standardModules = new HashSet<String>();
            boolean doFastScan = false;
            String basedir = properties.get("basedir");
            if (basedir != null) {
                File basedirF = new File(basedir);
                String clusterList = properties.get("nb.clusters.list");
                if (clusterList == null) {
                    String config = properties.get("cluster.config");
                    if (config != null) {
                        clusterList = properties.get("clusters.config." + config + ".list");
                    }
                }
                if (clusterList != null) {
                    StringTokenizer tok = new StringTokenizer(clusterList, ", ");
                    while (tok.hasMoreTokens()) {
                        String clusterName = tok.nextToken();
                        String moduleList = properties.get(clusterName);
                        if (moduleList != null) {
                            StringTokenizer tok2 = new StringTokenizer(moduleList, ", ");
                            while (tok2.hasMoreTokens()) {
                                String module = tok2.nextToken();
                                standardModules.add(module);
                                doFastScan |= new File(root, module.replace('/', File.separatorChar)).equals(basedirF);
                            }
                        }
                    }
                }
            }
            File scanCache = new File(root, "nbbuild" + File.separatorChar + "nbproject" + File.separatorChar +
                    "private" + File.separatorChar + "scan-cache-" + (doFastScan ? "standard" : "full") + ".ser");
            if (scanCache.isFile()) {
                if (project != null) {
                    project.log("Loading module list from " + scanCache);
                }
                try {
                    InputStream is = new FileInputStream(scanCache);
                    try {
                        ObjectInput oi = new ObjectInputStream(new BufferedInputStream(is));
                        @SuppressWarnings("unchecked") Map<File,Long[]> timestampsAndSizes = (Map) oi.readObject();
                        boolean matches = true;
                        for (Map.Entry<File,Long[]> entry : timestampsAndSizes.entrySet()) {
                            File f = entry.getKey();
                            if (f.lastModified() != entry.getValue()[0] || f.length() != entry.getValue()[1]) {
                                if (project != null) {
                                    project.log("Cache ignored due to modifications in " + f);
                                }
                                matches = false;
                                break;
                            }
                        }
                        if (doFastScan) {
                            @SuppressWarnings("unchecked") Set<String> storedStandardModules = (Set) oi.readObject();
                            if (!standardModules.equals(storedStandardModules)) {
                                Set<String> added = new TreeSet<String>(standardModules);
                                added.removeAll(storedStandardModules);
                                Set<String> removed = new TreeSet<String>(storedStandardModules);
                                removed.removeAll(standardModules);
                                project.log("Cache ignored due to changes in modules among standard clusters: + " + added + " - " + removed);
                                matches = false;
                            }
                        }
                        File myProjectXml = project.resolveFile("nbproject/project.xml");
                        if (myProjectXml.isFile() && !timestampsAndSizes.containsKey(myProjectXml)) {
                            project.log("Cache ignored since it has no mention of " + myProjectXml);
                            matches = false; // #118098
                        }
                        if (matches) {
                            @SuppressWarnings("unchecked") Map<String,Entry> _entries = (Map) oi.readObject();
                            entries = _entries;
                            if (project != null) {
                                project.log("Loaded modules: " + entries.keySet(), Project.MSG_VERBOSE);
                            }
                        }
                    } finally {
                        is.close();
                    }
                } catch (Exception x) {
                    if (project != null) {
                        project.log("Error loading " + scanCache + ": " + x, Project.MSG_WARN);
                    }
                }
            }
            if (entries == null) {
                entries = new HashMap<String,Entry>();
                Map<File,Long[]> timestampsAndSizes = new HashMap<File,Long[]>();
                registerTimestampAndSize(new File(root, "nbbuild" + File.separatorChar + "cluster.properties"), timestampsAndSizes);
                registerTimestampAndSize(new File(root, "nbbuild" + File.separatorChar + "build.properties"), timestampsAndSizes);
                registerTimestampAndSize(new File(root, "nbbuild" + File.separatorChar + "user.build.properties"), timestampsAndSizes);
                if (doFastScan) {
                    if (project != null) {
                        project.log("Scanning for modules in " + root + " among standard clusters");
                    }
                    for (String module : standardModules) {
                        scanPossibleProject(new File(root, module.replace('/', File.separatorChar)), entries, properties, module, ParseProjectXml.TYPE_NB_ORG, project, timestampsAndSizes);
                    }
                } else {
                    // Might be an extra module (e.g. something in contrib); need to scan everything.
                    if (project != null) {
                        project.log("Scanning for modules in " + root);
                        project.log("Quick scan mode disabled since " + basedir + " not among standard modules of " + root + " which are " + standardModules, Project.MSG_VERBOSE);
                    }
                    for (String tree : FOREST) {
                        File dir = tree == null ? root : new File(root, tree);
                        File[] kids = dir.listFiles();
                        if (kids == null) {
                            continue;
                        }
                        for (File kid : kids) {
                            if (!kid.isDirectory()) {
                                continue;
                            }
                            String name = kid.getName();
                            String path = tree == null ? name : tree + "/" + name;
                            scanPossibleProject(kid, entries, properties, path, ParseProjectXml.TYPE_NB_ORG, project, timestampsAndSizes);
                        }
                        
                    }
                }
                if (project != null) {
                    project.log("Found modules: " + entries.keySet(), Project.MSG_VERBOSE);
                    project.log("Cache depends on files: " + timestampsAndSizes.keySet(), Project.MSG_DEBUG);
                }
                scanCache.getParentFile().mkdirs();
                OutputStream os = new FileOutputStream(scanCache);
                try {
                    ObjectOutput oo = new ObjectOutputStream(os);
                    oo.writeObject(timestampsAndSizes);
                    if (doFastScan) {
                        oo.writeObject(standardModules);
                    }
                    oo.writeObject(entries);
                    oo.flush();
                } finally {
                    os.close();
                }
            }
            SOURCE_SCAN_CACHE.put(root, entries);
        }
        return entries;
    }

    private static void registerTimestampAndSize(File f, Map<File,Long[]> timestampsAndSizes) {
        if (timestampsAndSizes != null) {
            timestampsAndSizes.put(f, new Long[] {f.lastModified(), f.length()});
        }
    }
    
    /**
     * Check a single dir to see if it is an NBM project, and if so, register it.
     */
    private static boolean scanPossibleProject(File dir, Map<String,Entry> entries, Map<String,String> properties,
            String path, int moduleType, Project project, Map<File,Long[]> timestampsAndSizes) throws IOException {
        File nbproject = new File(dir, "nbproject");
        File projectxml = new File(nbproject, "project.xml");
        if (!projectxml.isFile()) {
            return false;
        }
        registerTimestampAndSize(projectxml, timestampsAndSizes);
        Document doc;
        try {
            doc = XMLUtil.parse(new InputSource(projectxml.toURI().toString()),
                                     false, true, /*XXX*/null, null);
        } catch (Exception e) { // SAXException, IOException (#60295: e.g. encoding problem in XML)
            // Include \n so that following line can be hyperlinked
            throw (IOException) new IOException("Error parsing project file\n" + projectxml + ": " + e.getMessage()).initCause(e);
        }
        Element typeEl = XMLUtil.findElement(doc.getDocumentElement(), "type", ParseProjectXml.PROJECT_NS);
        if (!XMLUtil.findText(typeEl).equals("org.netbeans.modules.apisupport.project")) {
            return false;
        }
        Element configEl = XMLUtil.findElement(doc.getDocumentElement(), "configuration", ParseProjectXml.PROJECT_NS);
        Element dataEl = ParseProjectXml.findNBMElement(configEl, "data");
        if (dataEl == null) {
            if (project != null) {
                project.log(projectxml.toString() + ": warning: module claims to be a NBM project but is missing <data xmlns=\"" + ParseProjectXml.NBM_NS3 + "\">; maybe an old NB 4.[01] project?", Project.MSG_WARN);
            }
            return false;
        }
        Element cnbEl = ParseProjectXml.findNBMElement(dataEl, "code-name-base");
        String cnb = XMLUtil.findText(cnbEl);
        if (moduleType == ParseProjectXml.TYPE_NB_ORG && project != null) {
            String expectedDirName = abbreviate(cnb);
            String actualDirName = dir.getName();
            if (!actualDirName.equals(expectedDirName)) {
                throw new IOException("Expected module to be in dir named " + expectedDirName + " but was actually found in dir named " + actualDirName);
            }
        }
        // Clumsy but the best way I know of to evaluate properties.
        Project fakeproj = new Project();
        if (project != null) {
            // Try to debug any problems in the following definitions (cf. #59849).
            Iterator it = project.getBuildListeners().iterator();
            while (it.hasNext()) {
                fakeproj.addBuildListener((BuildListener) it.next());
            }
        }
        fakeproj.setBaseDir(dir); // in case ${basedir} is used somewhere
        Property faketask = new Property();
        faketask.setProject(fakeproj);
        switch (moduleType) {
        case ParseProjectXml.TYPE_NB_ORG:
            // do nothing here
            break;
        case ParseProjectXml.TYPE_SUITE:
            faketask.setFile(new File(nbproject, "private/suite-private.properties"));
            faketask.execute();
            faketask.setFile(new File(nbproject, "suite.properties"));
            faketask.execute();
            faketask.setFile(new File(fakeproj.replaceProperties("${suite.dir}/nbproject/private/platform-private.properties")));
            faketask.execute();
            faketask.setFile(new File(fakeproj.replaceProperties("${suite.dir}/nbproject/platform.properties")));
            faketask.execute();
            break;
        case ParseProjectXml.TYPE_STANDALONE:
            faketask.setFile(new File(nbproject, "private/platform-private.properties"));
            faketask.execute();
            faketask.setFile(new File(nbproject, "platform.properties"));
            faketask.execute();
            break;
        default:
            assert false : moduleType;
        }
        File privateProperties = new File(nbproject, "private/private.properties".replace('/', File.separatorChar));
        registerTimestampAndSize(privateProperties, timestampsAndSizes);
        faketask.setFile(privateProperties);
        faketask.execute();
        File projectProperties = new File(nbproject, "project.properties");
        registerTimestampAndSize(projectProperties, timestampsAndSizes);
        faketask.setFile(projectProperties);
        faketask.execute();
        faketask.setFile(null);
        faketask.setName("module.jar.dir");
        faketask.setValue("modules");
        faketask.execute();
        assert fakeproj.getProperty("module.jar.dir") != null : fakeproj.getProperties();
        faketask.setName("module.jar.basename");
        faketask.setValue(cnb.replace('.', '-') + ".jar");
        faketask.execute();
        faketask.setName("module.jar");
        faketask.setValue(fakeproj.replaceProperties("${module.jar.dir}/${module.jar.basename}"));
        faketask.execute();
        switch (moduleType) {
        case ParseProjectXml.TYPE_NB_ORG:
            assert path != null;
            // Find the associated cluster.
            for (Map.Entry<String,String> entry : properties.entrySet()) {
                String val = entry.getValue();
                String[] modules = val.split(", *");
                if (Arrays.asList(modules).contains(path)) {
                    String key = entry.getKey();
                    String clusterDir = properties.get(key + ".dir");
                    if (clusterDir != null) {
                        faketask.setName("cluster.dir");
                        faketask.setValue(clusterDir);
                        faketask.execute();
                        break;
                    }
                }
            }
            faketask.setName("cluster.dir");
            faketask.setValue("extra"); // fallback
            faketask.execute();
            faketask.setName("netbeans.dest.dir");
            faketask.setValue(properties.get("netbeans.dest.dir"));
            faketask.execute();
            faketask.setName("cluster");
            faketask.setValue(fakeproj.replaceProperties("${netbeans.dest.dir}/${cluster.dir}"));
            faketask.execute();
            break;
        case ParseProjectXml.TYPE_SUITE:
            assert path == null;
            faketask.setName("suite.dir");
            faketask.setValue(properties.get("suite.dir"));
            faketask.execute();
            faketask.setName("cluster");
            faketask.setValue(fakeproj.replaceProperties("${suite.dir}/build/cluster"));
            faketask.execute();
            break;
        case ParseProjectXml.TYPE_STANDALONE:
            assert path == null;
            faketask.setName("cluster");
            faketask.setValue(fakeproj.replaceProperties("${basedir}/build/cluster"));
            faketask.execute();
            break;
        default:
            assert false : moduleType;
        }
        File jar = fakeproj.resolveFile(fakeproj.replaceProperties("${cluster}/${module.jar}"));
        List<File> exts = new ArrayList<File>();
        for (Element ext : XMLUtil.findSubElements(dataEl)) {
            if (!ext.getLocalName().equals("class-path-extension")) {
                continue;
            }
            Element binaryOrigin = ParseProjectXml.findNBMElement(ext, "binary-origin");
            File origBin = null;
            if (binaryOrigin != null) {
                String reltext = XMLUtil.findText(binaryOrigin);
                String nball = properties.get("nb_all");
                if (nball != null) {
                    faketask.setName("nb_all");
                    faketask.setValue(nball);
                    faketask.execute();
                }
                fakeproj.setBaseDir(dir);
                origBin = fakeproj.resolveFile(fakeproj.replaceProperties(reltext));
            } 

            File resultBin = null;
            if (origBin == null || !origBin.exists()) {
                Element runtimeRelativePath = ParseProjectXml.findNBMElement(ext, "runtime-relative-path");
                if (runtimeRelativePath == null) {
                    throw new IOException("Have malformed <class-path-extension> in " + projectxml);
                }
                String reltext = XMLUtil.findText(runtimeRelativePath);
                // No need to evaluate property refs in it - it is *not* substitutable-text in the schema.
                resultBin = new File(jar.getParentFile(), reltext.replace('/', File.separatorChar));
            }

            if (origBin != null) {
                exts.add(origBin);
            }

            if (resultBin != null) {
                exts.add(resultBin);
            }
        }
        List<String> prereqs = new ArrayList<String>();
        List<String> rundeps = new ArrayList<String>();
        Element depsEl = ParseProjectXml.findNBMElement(dataEl, "module-dependencies");
        if (depsEl == null) {
            throw new IOException("Malformed project file " + projectxml);
        }
        Element testDepsEl = ParseProjectXml.findNBMElement(dataEl,"test-dependencies");
         //compileDeps = Collections.emptyList();
        Map<String,String[]> compileTestDeps = new HashMap<String,String[]>();
        if (testDepsEl != null) {
            for (Element depssEl : XMLUtil.findSubElements(testDepsEl)) {
                String testtype = ParseProjectXml.findTextOrNull(depssEl,"name") ;
                if (testtype == null) {
                    throw new IOException("Must declare <name>unit</name> (e.g.) in <test-type> in " + projectxml);
                }
                List<String> compileDepsList = new ArrayList<String>();
                for (Element dep : XMLUtil.findSubElements(depssEl)) {
                    if (dep.getTagName().equals("test-dependency")) {
                        if (ParseProjectXml.findNBMElement(dep,"test") != null)  {
                            compileDepsList.add(ParseProjectXml.findTextOrNull(dep, "code-name-base"));
                        } 
                    }
                }
                compileTestDeps.put(testtype, compileDepsList.toArray(new String[0]));
            }
        } 
        for (Element dep : XMLUtil.findSubElements(depsEl)) {
            Element cnbEl2 = ParseProjectXml.findNBMElement(dep, "code-name-base");
            if (cnbEl2 == null) {
                throw new IOException("Malformed project file " + projectxml);
            }
            String cnb2 = XMLUtil.findText(cnbEl2);
            rundeps.add(cnb2);
            if (ParseProjectXml.findNBMElement(dep, "build-prerequisite") == null) {
                continue;
            }
            prereqs.add(cnb2);
        }
        String cluster = fakeproj.getProperty("cluster.dir"); // may be null
        Entry entry = new Entry(cnb, jar, exts.toArray(new File[exts.size()]), dir, path,
                prereqs.toArray(new String[prereqs.size()]), 
                cluster, 
                rundeps.toArray(new String[rundeps.size()]),
                compileTestDeps
                );
        if (entries.containsKey(cnb)) {
            throw new IOException("Duplicated module " + cnb + ": found in " + entries.get(cnb) + " and " + entry);
        } else {
            entries.put(cnb, entry);
        }
        return true;
    }
    static String abbreviate(String cnb) {
        return cnb.replaceFirst("^org\\.netbeans\\.modules\\.", ""). // NOI18N
                   replaceFirst("^org\\.netbeans\\.(libs|lib|api|spi|core)\\.", "$1."). // NOI18N
                   replaceFirst("^org\\.netbeans\\.", "o.n."). // NOI18N
                   replaceFirst("^org\\.openide\\.", "openide."). // NOI18N
                   replaceFirst("^org\\.", "o."). // NOI18N
                   replaceFirst("^com\\.sun\\.", "c.s."). // NOI18N
                   replaceFirst("^com\\.", "c."); // NOI18N
    }
    
    /**
     * Find all modules in a binary build, possibly from cache.
     */
    private static Map<String,Entry> scanBinaries(Project project, File[] clusters) throws IOException {
        Map<String,Entry> allEntries = new HashMap<String,Entry>();

        for (File cluster : clusters) {
            Map<String, Entry> entries = BINARY_SCAN_CACHE.get(cluster);
            if (entries == null) {
                if (project != null) {
                    project.log("Scanning for modules in " + cluster);
                }
                entries = new HashMap<String, Entry>();
                doScanBinaries(cluster, entries);
                if (project != null) {
                    project.log("Found modules: " + entries.keySet(), Project.MSG_VERBOSE);
                }
                BINARY_SCAN_CACHE.put(cluster, entries);
            }
            allEntries.putAll(entries);
        }
        return allEntries;
    }
    
    private static final String[] MODULE_DIRS = {
        "modules",
        "modules/eager",
        "modules/autoload",
        "lib",
        "core",
    };
    /**
     * Look for all possible modules in a NB build.
     * Checks modules/{,autoload/,eager/}*.jar as well as well-known core/*.jar and lib/boot.jar in each cluster.
     * XXX would be slightly more precise to check config/Modules/*.xml rather than scan for module JARs.
     */
    private static void doScanBinaries(File cluster, Map<String,Entry> entries) throws IOException {
            for (int j = 0; j < MODULE_DIRS.length; j++) {
                File dir = new File(cluster, MODULE_DIRS[j].replace('/', File.separatorChar));
                if (!dir.isDirectory()) {
                    continue;
                }
                File[] jars = dir.listFiles();
                if (jars == null) {
                    throw new IOException("Cannot examine dir " + dir);
                }
                for (int k = 0; k < jars.length; k++) {
                    File m = jars[k];
                    if (!m.getName().endsWith(".jar")) {
                        continue;
                    }
                    JarFile jf = new JarFile(m);
                    try {
                        Attributes attr = jf.getManifest().getMainAttributes();
                        String codename = attr.getValue("OpenIDE-Module");
                        if (codename == null) {
                            continue;
                        }
                        String codenamebase;
                        int slash = codename.lastIndexOf('/');
                        if (slash == -1) {
                            codenamebase = codename;
                        } else {
                            codenamebase = codename.substring(0, slash);
                        }
                        
                        String cp = attr.getValue("Class-Path");
                        File[] exts;
                        if (cp == null) {
                            exts = new File[0];
                        } else {
                            String[] pieces = cp.split(" +");
                            exts = new File[pieces.length];
                            for (int l = 0; l < pieces.length; l++) {
                                exts[l] = new File(dir, pieces[l].replace('/', File.separatorChar));
                            }
                        }
                        String moduleDependencies = attr.getValue("OpenIDE-Module-Module-Dependencies");
                        
                        
                        Entry entry = new Entry(codenamebase, m, exts,dir, null, null, cluster.getName(),
                                parseRuntimeDependencies(moduleDependencies), Collections.<String,String[]>emptyMap());
                        if (entries.containsKey(codenamebase)) {
                            throw new IOException("Duplicated module " + codenamebase + ": found in " + entries.get(codenamebase) + " and " + entry);
                        } else {
                            entries.put(codenamebase, entry);
                        }
                    } finally {
                        jf.close();
                    }
                }
            }
    }
    
    private static Map<String,Entry> scanSuiteSources(Map<String,String> properties, Project project) throws IOException {
        File basedir = new File(properties.get("basedir"));
        String suiteDir = properties.get("suite.dir");
        if (suiteDir == null) {
            throw new IOException("No definition of suite.dir in " + basedir);
        }
        File suite = FileUtils.getFileUtils().resolveFile(basedir, suiteDir);
        if (!suite.isDirectory()) {
            throw new IOException("No such suite " + suite);
        }
        Map<String,Entry> entries = SUITE_SCAN_CACHE.get(suite);
        if (entries == null) {
            if (project != null) {
                project.log("Scanning for modules in suite " + suite);
            }
            entries = new HashMap<String,Entry>();
            doScanSuite(entries, suite, properties, project);
            if (project != null) {
                project.log("Found modules: " + entries.keySet(), Project.MSG_VERBOSE);
            }
            SUITE_SCAN_CACHE.put(suite, entries);
        }
        return entries;
    }
    
    private static void doScanSuite(Map<String,Entry> entries, File suite, Map<String,String> properties, Project project) throws IOException {
        Project fakeproj = new Project();
        fakeproj.setBaseDir(suite); // in case ${basedir} is used somewhere
        Property faketask = new Property();
        faketask.setProject(fakeproj);
        faketask.setFile(new File(suite, "nbproject/private/private.properties".replace('/', File.separatorChar)));
        faketask.execute();
        faketask.setFile(new File(suite, "nbproject/project.properties".replace('/', File.separatorChar)));
        faketask.execute();
        String modulesS = fakeproj.getProperty("modules");
        if (modulesS == null) {
            throw new IOException("No definition of modules in " + suite);
        }
        String[] modules = Path.translatePath(fakeproj, modulesS);
        for (int i = 0; i < modules.length; i++) {
            File module = new File(modules[i]);
            if (!module.isDirectory()) {
                throw new IOException("No such module " + module + " referred to from " + suite);
            }
            if (!scanPossibleProject(module, entries, properties, null, ParseProjectXml.TYPE_SUITE, project, null)) {
                throw new IOException("No valid module found in " + module + " referred to from " + suite);
            }
        }
    }
    
    private static Entry scanStandaloneSource(Map<String,String> properties, Project project) throws IOException {
        if (properties.get("project") == null) return null; //Not a standalone module
        File basedir = new File(properties.get("project"));
        Entry entry = STANDALONE_SCAN_CACHE.get(basedir);
        if (entry == null) {
            Map<String,Entry> entries = new HashMap<String,Entry>();
            if (!scanPossibleProject(basedir, entries, properties, null, ParseProjectXml.TYPE_STANDALONE, project, null)) {
                throw new IOException("No valid module found in " + basedir);
            }
            assert entries.size() == 1;
            entry = entries.values().iterator().next();
            STANDALONE_SCAN_CACHE.put(basedir, entry);
        }
        return entry;
    }
    
    /** all module entries, indexed by cnb */
    private final Map<String,Entry> entries;
    
    /**
     * Initiates scan if not already parsed.
     * Properties interpreted:
     * <ol>
     * <li> ${nb_all} - location of NB sources (used only for netbeans.org modules)
     * <li> ${netbeans.dest.dir} - location of NB build (used only for NB.org modules)
     * <li> ${cluster.path} - location of clusters to build against (used only for suite and standalone modules)
     * <li> ${basedir} - directory of the project initiating the scan (most significant for standalone modules)
     * <li> ${suite.dir} - directory of the suite (used only for suite modules)
     * <li> ${nb.cluster.TOKEN} - list of module paths included in cluster TOKEN (comma-separated) (used only for netbeans.org modules)
     * <li> ${nb.cluster.TOKEN.dir} - directory in ${netbeans.dest.dir} where cluster TOKEN is built (used only for netbeans.org modules)
     * <li> ${project} - basedir for standalone modules
     * </ol>
     * @param properties some properties to be used (see above)
     * @param type the type of project
     * @param project a project ref, only for logging (may be null with no loss of semantics)
     */
    public ModuleListParser(Map<String,String> properties, int type, Project project) throws IOException {
        String nball = properties.get("nb_all");
        File basedir = new File(properties.get("basedir"));
        final FileUtils fu = FileUtils.getFileUtils();

        if (type != ParseProjectXml.TYPE_NB_ORG) {
            // add extra clusters
            String suiteDirS = properties.get("suite.dir");
            boolean hasSuiteDir = suiteDirS != null && suiteDirS.length() > 0;
            String clusterPath = properties.get("cluster.path");
            File[] clusters = null;

            if (clusterPath != null) {
                String[] clustersS;
                if (hasSuiteDir) {
                    // resolve suite modules against fake suite project
                    Project fakeproj = new Project();
                    fakeproj.setBaseDir(new File(suiteDirS));
                    clustersS = Path.translatePath(fakeproj, clusterPath);
                } else {
                    clustersS = Path.translatePath(project, clusterPath);
                }
                clusters = new File[clustersS.length];
                if (clustersS != null && clustersS.length > 0) {
                    for (int j = 0; j < clustersS.length; j++) {
                        File cluster = new File(clustersS[j]);
                        if (! cluster.isDirectory()) {
                            throw new IOException("No such cluster " + cluster + " referred to from ${cluster.path}: " + clusterPath);
                        }
                        clusters[j] = cluster;
                    }
                }
            }

            if (clusters == null || clusters.length == 0)
                throw new IOException("Invalid ${cluster.path}: " + clusterPath);

            // External module.
            if (nball != null && project != null) {
                project.log("You must *not* declare <suite-component/> or <standalone/> for a netbeans.org module in " + basedir + "; fix project.xml to use the /2 schema", Project.MSG_WARN);
            }
            entries = scanBinaries(project, clusters);
            if (type == ParseProjectXml.TYPE_SUITE) {
                entries.putAll(scanSuiteSources(properties, project));
            } else {
                assert type == ParseProjectXml.TYPE_STANDALONE;
                Entry e = scanStandaloneSource(properties, project);
                entries.put(e.getCnb(), e);
            }
        } else {
            // netbeans.org module.
            String buildS = properties.get("netbeans.dest.dir");
            if (buildS == null) {
                throw new IOException("No definition of netbeans.dest.dir in " + basedir);
            }
            // Resolve against basedir, and normalize ../ sequences and so on in case they are used.
            // Neither operation is likely to be needed, but just in case.
            File build = fu.normalize(fu.resolveFile(basedir, buildS).getAbsolutePath());
            if (!build.isDirectory()) {
                throw new IOException("No such netbeans.dest.dir: " + build);
            }

            // expand clusters in build
            File[] clusters = build.listFiles();
            if (clusters == null) {
                throw new IOException("Cannot examine dir " + build);
            }

            if (nball == null) {
                throw new IOException("You must declare either <suite-component/> or <standalone/> for an external module in " + new File(properties.get("basedir")));
            }
            if (!build.equals(new File(new File(nball, "nbbuild"), "netbeans"))) {
                // Potentially orphaned module to be built against specific binaries, plus perhaps other source deps.
                entries = scanBinaries(project, clusters);
                // Add referenced module in case it does not appear otherwise.
                Entry e = scanStandaloneSource(properties, project);
                if (e != null) {
                    entries.put(e.getCnb(), e);
                }
                entries.putAll(scanNetBeansOrgSources(new File(nball), properties, project));
            } else {
                entries = scanNetBeansOrgSources(new File(nball), properties, project);
            }
        }
    }
    /**
     * Find all entries in this list.
     * @return a set of all known entries
     */
    public Set<Entry> findAll() {
        return new HashSet<Entry>(entries.values());
    }
    
    /**
     * Find one entry by code name base.
     * @param cnb the desired code name base
     * @return the matching entry or null
     */
    public Entry findByCodeNameBase(String cnb) {
        return entries.get(cnb);
    }

    
    /** parse Openide-Module-Module-Dependencies entry
     * @return array of code name bases
     */
    private static String[] parseRuntimeDependencies(String moduleDependencies) {
        if (moduleDependencies == null) {
            return new String[0];
        }
        List<String> cnds = new ArrayList<String>();
        StringTokenizer toks = new StringTokenizer(moduleDependencies,",");
        while (toks.hasMoreTokens()) {
            String token = toks.nextToken().trim();
            // substring cnd/x
            int slIdx = token.indexOf('/');
            if (slIdx != -1) {
                token = token.substring(0,slIdx);
            }
            // substring cnd' 'xx
            slIdx = token.indexOf(' ');
            if (slIdx != -1) {
                token = token.substring(0,slIdx);
            }
            // substring cnd > 
            slIdx = token.indexOf('>');
            if (slIdx != -1) {
                token = token.substring(0,slIdx);
            }
            token = token.trim();
            if (token.length() > 0) {
               cnds.add(token);
            }
        }
        return cnds.toArray(new String[cnds.size()]);
    }
    
    /**
     * One entry in the file.
     */
    @SuppressWarnings("serial") // really want it to be incompatible if format changes
    public static final class Entry implements Serializable {

        // Synch with org.netbeans.modules.apisupport.project.universe.ModuleList:
        private final String cnb;
        private final File jar;
        private final File[] classPathExtensions;
        private final File sourceLocation;
        private final String netbeansOrgPath;
        private final String[] buildPrerequisites;
        private final String clusterName;
        private final String[] runtimeDependencies; 
        // dependencies on other tests
        private final Map<String,String[]> testDependencies;
        
        Entry(String cnb, File jar, File[] classPathExtensions, File sourceLocation, String netbeansOrgPath,
                String[] buildPrerequisites, String clusterName,String[] runtimeDependencies, Map<String,String[]> testDependencies) {
            this.cnb = cnb;
            this.jar = jar;
            this.classPathExtensions = classPathExtensions;
            this.sourceLocation = sourceLocation;
            this.netbeansOrgPath = netbeansOrgPath;
            this.buildPrerequisites = buildPrerequisites;
            this.clusterName = clusterName;
            this.runtimeDependencies = runtimeDependencies;
            assert testDependencies != null;
            this.testDependencies = testDependencies;
        }
        
        /**
         * Get the code name base, e.g. org.netbeans.modules.ant.grammar.
         */
        public String getCnb() {
            return cnb;
        }
        
        /**
         * Get the absolute JAR location, e.g. .../ide5/modules/org-netbeans-modules-ant-grammar.jar.
         */
        public File getJar() {
            return jar;
        }
        
        /**
         * Get a list of extensions to the class path of this module (may be empty).
         */
        public File[] getClassPathExtensions() {
            return classPathExtensions;
        }

        /**
         * @return the sourceLocation, may be null.
         */
        public File getSourceLocation() {
            return sourceLocation;
        }
        
        /**
         * Get the path within netbeans.org, if this is a netbeans.org module (else null).
         */
        public String getNetbeansOrgPath() {
            return netbeansOrgPath;
        }
        
        /**
         * Get a list of declared build prerequisites (or null for sourceless entries).
         * Each entry is a code name base.
         */
        public String[] getBuildPrerequisites() {
            return buildPrerequisites;
        }
        /** Get runtime dependencies, OpenIDE-Module-Dependencies entry. 
         */
        public String[] getRuntimeDependencies() {
            return runtimeDependencies;
        }
        
        /**
         * Return the name of the cluster in which this module resides.
         * If this entry represents an external module in source form,
         * then the cluster will be null. If the module represents a netbeans.org
         * module or a binary module in a platform, then the cluster name will
         * be the (base) name of the directory containing the "modules" subdirectory
         * (sometimes "lib" or "core") where the JAR is.
         */
        public String getClusterName() {
            return clusterName;
        }
        
        public Map<String,String[]> getTestDependencies() {
            return testDependencies;
        }
        public @Override String toString() {
            return (sourceLocation != null ? sourceLocation : jar).getAbsolutePath();
        }
        
    }

}
