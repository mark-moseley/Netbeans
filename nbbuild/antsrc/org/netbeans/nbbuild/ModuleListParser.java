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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
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

    /** Synch with org.netbeans.modules.apisupport.project.ModuleList.DEPTH_NB_ALL */
    private static final int DEPTH_NB_ALL = 3;
    
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
    
    /**
     * Find all NBM projects in a root, possibly from cache.
     */
    private static Map<String,Entry> scanNetBeansOrgSources(File root, Hashtable<String,String> properties, Project project) throws IOException {
        Map<String,Entry> entries = SOURCE_SCAN_CACHE.get(root);
        if (entries == null) {
            entries = new HashMap<String,Entry>();
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
            if (doFastScan) {
                if (project != null) {
                    project.log("Scanning for modules in " + root + " among standard clusters");
                }
                Iterator it = standardModules.iterator();
                while (it.hasNext()) {
                    String module = (String) it.next();
                    scanPossibleProject(new File(root, module.replace('/', File.separatorChar)), entries, properties, module, ParseProjectXml.TYPE_NB_ORG, project);
                }
            } else {
                // Might be an extra module (e.g. something in contrib); need to scan everything.
                if (project != null) {
                    project.log("Scanning for modules in " + root);
                    project.log("Quick scan mode disabled since " + basedir + " not among standard modules of " + root + " which are " + standardModules, project.MSG_VERBOSE);
                }
                doScanNetBeansOrgSources(entries, root, DEPTH_NB_ALL, properties, null, project);
            }
            if (project != null) {
                project.log("Found modules: " + entries.keySet(), Project.MSG_VERBOSE);
            }
            SOURCE_SCAN_CACHE.put(root, entries);
        }
        return entries;
    }
    
    /** Borrowed from org.netbeans.modules.apisupport.project.universe.ModuleList; cf. #61579 */
    private static final String[] EXCLUDED_DIR_NAMES = {
        "CVS", // NOI18N
        "nbproject", // NOI18N
        "www", // NOI18N
        "test", // NOI18N
        "build", // NOI18N
        "src", // NOI18N
        "org", // NOI18N
    };
    /**
     * Scan a root for all NBM projects.
     */
    private static void doScanNetBeansOrgSources(Map<String,Entry> entries, File dir, int depth, Hashtable<String,String> properties, String pathPrefix, Project project) throws IOException {
        if (depth == 0) {
            return;
        }
        File[] kids = dir.listFiles();
        if (kids == null) {
            return;
        }
        KIDS: for (File kid : kids) {
            if (!kid.isDirectory()) {
                continue;
            }
            String name = kid.getName();
            for (String n : EXCLUDED_DIR_NAMES) {
                if (name.equals(n)) {
                    continue KIDS;
                }
            }
            String newPathPrefix = (pathPrefix != null) ? pathPrefix + "/" + name : name;
            scanPossibleProject(kid, entries, properties, newPathPrefix, ParseProjectXml.TYPE_NB_ORG, project);
            doScanNetBeansOrgSources(entries, kid, depth - 1, properties, newPathPrefix, project);
        }
    }
    
    /**
     * Check a single dir to see if it is an NBM project, and if so, register it.
     */
    private static boolean scanPossibleProject(File dir, Map<String,Entry> entries, Hashtable<String,String> properties, String path, int moduleType, Project project) throws IOException {
        File nbproject = new File(dir, "nbproject");
        File projectxml = new File(nbproject, "project.xml");
        if (!projectxml.isFile()) {
            return false;
        }
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
        faketask.setFile(new File(nbproject, "private/private.properties".replace('/', File.separatorChar)));
        faketask.execute();
        faketask.setFile(new File(nbproject, "project.properties"));
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
        String compileTestDeps[] = null;
        if (testDepsEl != null) {
            for (Element depssEl : XMLUtil.findSubElements(testDepsEl)) {
                String testtype = ParseProjectXml.findTextOrNull(depssEl,"name") ;
                
                if (testtype == null || testtype.equals("unit")) {
                    List<String> compileDepsList = new ArrayList<String>();
                    for (Element dep : XMLUtil.findSubElements(depssEl)) {
                        if (dep.getTagName().equals("test-dependency")) {
                            if (ParseProjectXml.findNBMElement(dep,"test") != null)  {
                                compileDepsList.add(ParseProjectXml.findTextOrNull(dep, "code-name-base"));
                            } 
                        }
                    }
                    compileTestDeps = new String[compileDepsList.size()];
                    compileDepsList.toArray(compileTestDeps);
                }

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
    
    /**
     * Find all modules in a binary build, possibly from cache.
     */
    private static Map<String,Entry> scanBinaries(Hashtable<String,String> properties, Project project) throws IOException {
        String buildS = properties.get("netbeans.dest.dir");
        File basedir = new File(properties.get("basedir"));
        if (buildS == null) {
            throw new IOException("No definition of netbeans.dest.dir in " + basedir);
        }
        // Resolve against basedir, and normalize ../ sequences and so on in case they are used.
        // Neither operation is likely to be needed, but just in case.
        File build = FileUtils.getFileUtils().normalize(FileUtils.getFileUtils().resolveFile(basedir, buildS).getAbsolutePath());
        if (!build.isDirectory()) {
            throw new IOException("No such netbeans.dest.dir: " + build);
        }
        Map<String,Entry> entries = BINARY_SCAN_CACHE.get(build);
        if (entries == null) {
            if (project != null) {
                project.log("Scanning for modules in " + build);
            }
            entries = new HashMap<String,Entry>();
            doScanBinaries(build, entries);
            if (project != null) {
                project.log("Found modules: " + entries.keySet(), Project.MSG_VERBOSE);
            }
            BINARY_SCAN_CACHE.put(build, entries);
        }
        return entries;
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
    private static void doScanBinaries(File build, Map<String,Entry> entries) throws IOException {
        File[] clusters = build.listFiles();
        if (clusters == null) {
            throw new IOException("Cannot examine dir " + build);
        }
        for (int i = 0; i < clusters.length; i++) {
            for (int j = 0; j < MODULE_DIRS.length; j++) {
                File dir = new File(clusters[i], MODULE_DIRS[j].replace('/', File.separatorChar));
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
                        
                        
                        Entry entry = new Entry(codenamebase, m, exts,dir, null, null, clusters[i].getName(),parseRuntimeDependencies(moduleDependencies), null);
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
    }
    
    private static Map<String,Entry> scanSuiteSources(Hashtable<String,String> properties, Project project) throws IOException {
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
    
    private static void doScanSuite(Map<String,Entry> entries, File suite, Hashtable<String,String> properties, Project project) throws IOException {
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
            if (!scanPossibleProject(module, entries, properties, null, ParseProjectXml.TYPE_SUITE, project)) {
                throw new IOException("No valid module found in " + module + " referred to from " + suite);
            }
        }
    }
    
    private static Entry scanStandaloneSource(Hashtable<String,String> properties, Project project) throws IOException {
        File basedir = new File(properties.get("project"));
        Entry entry = STANDALONE_SCAN_CACHE.get(basedir);
        if (entry == null) {
            Map<String,Entry> entries = new HashMap<String,Entry>();
            if (!scanPossibleProject(basedir, entries, properties, null, ParseProjectXml.TYPE_STANDALONE, project)) {
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
     * <li> ${netbeans.dest.dir} - location of NB build
     * <li> ${basedir} - directory of this project (used only for standalone modules)
     * <li> ${suite.dir} - directory of the suite (used only for suite modules)
     * <li> ${nb.cluster.TOKEN} - list of module paths included in cluster TOKEN (comma-separated) (used only for netbeans.org modules)
     * <li> ${nb.cluster.TOKEN.dir} - directory in ${netbeans.dest.dir} where cluster TOKEN is built (used only for netbeans.org modules)
     * <li> ${project} - basedir for standalone modules
     * </ol>
     * @param properties some properties to be used (see above)
     * @param type the type of project
     * @param project a project ref, only for logging (may be null with no loss of semantics)
     */
    public ModuleListParser(Hashtable<String,String> properties, int type, Project project) throws IOException {
        String nball = properties.get("nb_all");
        if (type != ParseProjectXml.TYPE_NB_ORG) {
            // External module.
            File basedir = new File(properties.get("basedir"));
            if (nball != null && project != null) {
                project.log("You must *not* declare <suite-component/> or <standalone/> for a netbeans.org module in " + basedir + "; fix project.xml to use the /2 schema", Project.MSG_WARN);
            }
            entries = scanBinaries(properties, project);
            if (type == ParseProjectXml.TYPE_SUITE) {
                entries.putAll(scanSuiteSources(properties, project));
            } else {
                assert type == ParseProjectXml.TYPE_STANDALONE;
                Entry e = scanStandaloneSource(properties, project);
                entries.put(e.getCnb(), e);
            }
        } else {
            // netbeans.org module.
            if (nball == null) {
                throw new IOException("You must declare either <suite-component/> or <standalone/> for an external module in " + new File(properties.get("basedir")));
            }
            // If scan.binaries property is set or it runs from tests we scan binaries otherwise sources.
            boolean xtest = properties.get("xtest.home") != null && properties.get("xtest.testtype") != null;
            if (properties.get("scan.binaries") != null || xtest) {
                entries = scanBinaries(properties, project);
                // module itself has to be added because it doesn't have to be in binaries
                    Entry e = scanStandaloneSource(properties, project);
                    // xtest gets module jar and cluster from binaries
                    if (e.clusterName == null && xtest) {
                         Entry oldEntry = entries.get(e.getCnb());
                         if (oldEntry != null) {
                             e = new Entry(e.getCnb(),oldEntry.getJar(),
                                          e.getClassPathExtensions(),e.sourceLocation,
                                          e.netbeansOrgPath,e.buildPrerequisites,
                                          oldEntry.getClusterName(),
                                          e.runtimeDependencies,
                                          e.getTestDependencies());  
                         }
                    }
                    entries.put(e.getCnb(), e);
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
    public static final class Entry {
        
        private final String cnb;
        private final File jar;
        private final File[] classPathExtensions;
        private final File sourceLocation;
        private final String netbeansOrgPath;
        private final String[] buildPrerequisites;
        private final String clusterName;
        private final String[] runtimeDependencies; 
        // dependencies on other tests
        private final String[] testDepencies;
        
        Entry(String cnb, File jar, File[] classPathExtensions, File sourceLocation, String netbeansOrgPath, String[] buildPrerequisites, String clusterName,String[] runtimeDependencies,String[] testDepencies) {
            this.cnb = cnb;
            this.jar = jar;
            this.classPathExtensions = classPathExtensions;
            this.sourceLocation = sourceLocation;
            this.netbeansOrgPath = netbeansOrgPath;
            this.buildPrerequisites = buildPrerequisites;
            this.clusterName = clusterName;
            this.runtimeDependencies = runtimeDependencies;
            this.testDepencies = testDepencies;
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
        
        public String [] getTestDependencies() {
            return testDepencies;
        }
        public @Override String toString() {
            return (sourceLocation != null ? sourceLocation : jar).getAbsolutePath();
        }
        
    }

}
