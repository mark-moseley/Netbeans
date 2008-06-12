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

package org.netbeans.nbbuild;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;

/** This task implements the module dependencies verification proposal
 * that is described at
 * http://openide.netbeans.org/proposals/arch/clusters.html#verify-solution
 */
public class ModuleDependencies extends Task {
    private List<Input> inputs = new ArrayList<Input>();
    private List<Output> outputs = new ArrayList<Output>();
    private Set<ModuleInfo> modules;
    private Pattern regexp;
    
    public ModuleDependencies () {
    }
    
    public void setGenerate(String regexpList) {
        regexp = Pattern.compile(regexpList);
    }
    
    public Input createInput() throws BuildException {
        Input input = new Input ();
        inputs.add (input);
        return input;
    }
    
    public Output createOutput() throws BuildException {
        Output output = new Output ();
        outputs.add (output);
        return output;
    }

    public @Override void execute() throws BuildException {
        if (outputs.size () == 0) throw new BuildException ("At least one <output> tag has to be specified");

        try {
            readModuleInfo ();

            for (Output o : outputs) {
                if (o.type == null) throw new BuildException ("<output> needs attribute type");
                if (o.file == null) throw new BuildException ("<output> needs attribute file");
                
                getProject ().log ("Generating " + o.type + " to " + o.file);
                
                if ("public-packages".equals (o.type.getValue ())) {
                    generatePublicPackages (o.file, true, false);
                    continue;
                }
                if ("friend-packages".equals (o.type.getValue ())) {
                    generatePublicPackages (o.file, false, false);
                    continue;
                }
                if ("shared-packages".equals (o.type.getValue ())) {
                    generateSharedPackages (o.file);
                    continue;
                }
                if ("modules".equals (o.type.getValue ())) {
                    generateListOfModules (o.file);                    
                    continue;
                }
                if ("dependencies".equals (o.type.getValue ())) {
                    generateDependencies (o.file, false);                    
                    continue;
                }
                if ("implementation-dependencies".equals (o.type.getValue ())) {
                    generateDependencies (o.file, true);                    
                    continue;
                }
                if ("group-dependencies".equals (o.type.getValue ())) {
                    generateGroupDependencies (o.file, false);                    
                    continue;
                }
                if ("group-implementation-dependencies".equals (o.type.getValue ())) {
                    generateGroupDependencies (o.file, true);                    
                    continue;
                }
                if ("group-friend-packages".equals (o.type.getValue ())) {
                    generatePublicPackages(o.file, false, true);                    
                    continue;
                }
                if ("kits".equals(o.type.getValue())) {
                    generateKits(o.file);
                    continue;
                }
                if ("kit-dependencies".equals(o.type.getValue())) {
                    generateKitDependencies(o.file);
                    continue;
                }
            }
        
        } catch (IOException ex) {
            throw new BuildException (ex);
        }
    }
    
    private void readModuleInfo () throws IOException {
        modules = new TreeSet<ModuleInfo>();
        
        if (inputs.isEmpty()) {
            throw new BuildException ("At least one <input> tag is needed");
        }
        for (Input input : inputs) {
            if (input.jars == null) throw new BuildException ("<input> needs a subelement <jars>");
            if (input.name == null) throw new BuildException ("<input> needs attribute name");
            
            Project p = getProject();
            DirectoryScanner scan = input.jars.getDirectoryScanner(p);
            for (String incl : scan.getIncludedFiles()) {
                File f = new File(scan.getBasedir(), incl);
                getProject().log("Processing " + f, Project.MSG_VERBOSE);
                JarFile file = new JarFile (f);
                
                Manifest manifest = file.getManifest();
                if (manifest == null) {
                    // process only manifest files
                    continue;
                }
                
                String module = manifest.getMainAttributes ().getValue ("OpenIDE-Module");
                
                
                if (module == null) {
                    // skip this one
                    continue;
                }


                ModuleInfo m;
                {
                    String codebasename;
                    int majorVersion;
                    // base name
                    int slash = module.indexOf ('/');
                    if (slash == -1) {
                        codebasename = module;
                        majorVersion = -1;
                    } else {
                        codebasename = module.substring (0, slash);
                        majorVersion = Integer.valueOf(module.substring(slash + 1));
                    }
                    m = new ModuleInfo (input.name, f, codebasename);
                    m.majorVersion = majorVersion;
                }

                String showInAutoUpdate = file.getManifest().getMainAttributes().getValue("AutoUpdate-Show-In-Client");
                if (showInAutoUpdate == null) {
                    m.showInAutoupdate = true;
                } else {
                    m.showInAutoupdate = Boolean.parseBoolean(showInAutoUpdate);
                }

                m.publicPackages = file.getManifest ().getMainAttributes ().getValue ("OpenIDE-Module-Public-Packages");

                {
                    m.specificationVersion = file.getManifest ().getMainAttributes ().getValue ("OpenIDE-Module-Specification-Version");
                }

                m.implementationVersion = file.getManifest ().getMainAttributes ().getValue ("OpenIDE-Module-Implementation-Version");

                TreeSet<Dependency> depends = new TreeSet<Dependency>();
                TreeSet<Dependency> provides = new TreeSet<Dependency>();
                addDependencies (depends, file.getManifest (), Dependency.REQUIRES, "OpenIDE-Module-Requires");
                addDependencies (provides, file.getManifest (), Dependency.PROVIDES, "OpenIDE-Module-Provides");
                {
                    String ideDeps = file.getManifest ().getMainAttributes ().getValue ("OpenIDE-Module-IDE-Dependencies"); // IDE/1 > 4.25
                    if (ideDeps != null) {
                        throw new BuildException("OpenIDE-Module-IDE-Dependencies is obsolete in " + f);
                    }
                }
                addDependencies (depends, file.getManifest (), Dependency.REQUIRES, "OpenIDE-Module-Module-Dependencies");
                /* org.netbeans.api.java/1,org.netbeans.modules.queries/0,
                 org.netbeans.modules.javacore/1,org.netbeans.jmi.javamodel/1 > 1.11,org.netbeans.api.mdr/1,
                 org.netbeans.modules.mdr/1= 1.0.0,org.netbeans.modules.
                 jmiutils/1 = 1.0.0,javax.jmi.reflect/1,
                 org.openide.loaders,org.openide.src > 1.0
                 */
                m.depends = depends;
                m.provides = provides;
                {
                    String friends = file.getManifest ().getMainAttributes ().getValue ("OpenIDE-Module-Friends"); 
                    if (friends != null) {
			TreeSet<String> set = new TreeSet<String>();
                        StringTokenizer tok = new StringTokenizer(friends, ", ");
			while (tok.hasMoreElements()) {
			    set.add(tok.nextToken());
			}
			m.friends = set;
                    }
                }
                String essential = file.getManifest ().getMainAttributes ().getValue ("AutoUpdate-Essential-Module");
                m.isEssential = essential == null ? 
                    false : 
                    Boolean.parseBoolean(file.getManifest().getMainAttributes().getValue("AutoUpdate-Essential-Module"));
                m.isAutoload = determineParameter(f, "autoload");
                m.isEager = determineParameter(f, "eager");
                modules.add (m);
            }
        }
    }
    
    private boolean determineParameter(File moduleFile, String parameter) throws IOException {
        String name = moduleFile.getName();
        name = name.substring(0, name.length() - 3) + "xml";
        File configFile = new File(moduleFile.getParentFile().getParentFile(), "config/Modules/" + name);
        log ("config " + configFile, Project.MSG_DEBUG);
        if (!configFile.exists())
            return true; // probably a classpath module, treat like autoload
        final String fragment = "<param name=\"" + parameter + "\">true</param>";
        BufferedReader br = new BufferedReader (new FileReader (configFile));
        try {
            String line;
            while ((line = br.readLine ()) != null) {
                if (line.indexOf (fragment) != -1) {
                    log ("autoload module: " + moduleFile, Project.MSG_DEBUG);
                    return true;
                }
            }
        } finally {
            br.close ();
        }
        return false;
    }
    

    private void generatePublicPackages(File output, boolean justPublic, boolean justInterCluster) throws BuildException, IOException {
        TreeSet<String> packages = new TreeSet<String>();
        TreeMap<ModuleInfo,TreeSet<String>> friendExports = new TreeMap<ModuleInfo,TreeSet<String>>();
        
        {
            for (ModuleInfo m : modules) {
                if (justPublic) {
                    if (m.friends != null) {
                        continue;
                    }
                }
                if (regexp != null && !regexp.matcher(m.group).matches()) {
                    continue;
                }

                String s = m.publicPackages;
                HashMap<String,Boolean> pkgs = null;
                if (s != null) {
                    pkgs = new HashMap<String,Boolean>();
                    StringTokenizer tok = new StringTokenizer(s, ",");
                    while (tok.hasMoreElements()) {
                        String p = tok.nextToken().trim();
                        if (p.equals("-")) {
                            continue;
                        }

                        if (p.endsWith(".*")) {
                            pkgs.put(p.substring(0, p.length() - 2).replace('.', '/'), Boolean.FALSE);
                            continue;
                        }
                        if (p.endsWith(".**")) {
                            pkgs.put(p.substring(0, p.length() - 3).replace('.', '/'), Boolean.TRUE);
                            continue;
                        }
                        throw new BuildException("Unknown package format: " + p + " in " + m.file);
                    }
                }

                if (justPublic) {
                    iterateThruPackages(m.file, pkgs, packages);
                    if (pkgs != null && packages.size() < pkgs.size()) {
                        throw new BuildException("Not enough packages found. The declared packages are: " + s + " but only " + packages + " were found in " + m.file);
                    }
                } else {
                    TreeSet<String> modulePkgs = new TreeSet<String>();
                    iterateThruPackages(m.file, pkgs, modulePkgs);
                    friendExports.put(m, modulePkgs);
                }

            }
        }
        
        PrintWriter w = new PrintWriter(new FileWriter(output));
        if (justPublic) {
            for (String out : packages) {
                w.println(out.replace('/', '.'));
            }
        } else {
            int maxFriends = Integer.MAX_VALUE;
            if (justInterCluster) {
                String maxFriendsString = this.getProject().getProperty("deps.max.friends");
                if (maxFriendsString != null) {
                    maxFriends = Integer.parseInt(maxFriendsString);
                }
            }
            
            for (Map.Entry<ModuleInfo,TreeSet<String>> entry : friendExports.entrySet()) {
                ModuleInfo info = entry.getKey();
                if (info.friends == null) {
                    continue;
                }
                log("Friends for " + info.getName(false), Project.MSG_DEBUG);
                int cntFriends = 0;
                boolean printed = false;
                for (String n : info.friends) {
                    ModuleInfo friend = findModuleInfo(n);
                    if (justInterCluster && friend != null && friend.group.equals(info.group)) {
                        continue;
                    }
                    
                    if (!printed) {
                        w.print("MODULE ");
                        w.println(info.getName(false));
                        printed = true;
                    }
                    
                    if (friend != null) {
                        w.print("  FRIEND ");
                        w.println(friend.getName(false));
                    } else {
                        w.print("  EXTERNAL ");
                        w.println(n);
                    }
                    cntFriends++;
                }
                if (cntFriends > maxFriends) {
                    throw new BuildException("Too many intercluster friends (" + cntFriends + ") for module " + info.getName(false));
                }
                
                if (cntFriends > 0) {
                    for (String out : entry.getValue()) {
                        w.print("  PACKAGE ");
                        w.println(out.replace('/', '.'));
                    }
                }
            }
        }
        w.close();
    }
    
    private void iterateThruPackages (File f, HashMap pkgs, TreeSet<String> packages) throws IOException {
        JarFile file = new JarFile (f);
        Enumeration en = file.entries ();
        LOOP: while (en.hasMoreElements ()) {
            JarEntry e = (JarEntry)en.nextElement ();
            if (e.getName ().endsWith (".class")) {
                int last = e.getName ().lastIndexOf ('/');
                if (last == -1) {
                    // skip default pkg
                    continue;
                }
                String p = e.getName ().substring (0, last);

                if (pkgs == null) {
                   packages.add (p);
                   continue;
                }

                Boolean b = (Boolean)pkgs.get (p);
                if (b != null) {
                    packages.add (p);
                    continue;
                }

                String parent = p;
                while (parent.length () > 0) {
                    int prev = parent.lastIndexOf ('/');
                    if (prev == -1) {
                        parent = "";
                    } else {
                        parent = parent.substring (0, prev);
                    }

                    b = (Boolean)pkgs.get (parent);
                    if (Boolean.TRUE.equals (b)) {
                        packages.add (p);
                        continue LOOP;
                    }
                }
            }
        }
        
        java.util.jar.Manifest m = file.getManifest ();
        if (m != null) {
            String value = m.getMainAttributes ().getValue ("Class-Path");
            if (value != null) {
                StringTokenizer tok = new StringTokenizer (value, " ");
                while (tok.hasMoreElements ()) {
                    File sub = new File (f.getParentFile (), tok.nextToken ());
                    if (sub.isFile ()) {
                        iterateThruPackages (sub, pkgs, packages);
                    }
                }
            }
        }
        
        file.close ();
    }

    private void generateListOfModules (File output) throws BuildException, IOException {
        PrintWriter w = new PrintWriter (new FileWriter (output));
        for (ModuleInfo m : modules) {
            if (regexp != null && !regexp.matcher(m.group).matches()) {
                continue;
            }
            w.print ("MODULE ");
            w.print(m.getName(true));
            w.println ();
        }
        w.close ();
    }
    
    private void generateKits(File output) throws BuildException, IOException {
        PrintWriter w = new PrintWriter(new FileWriter(output));
        // calculate transitive closure of kits
        TreeMap<String, TreeSet<String>> allKitDeps = transitiveClosureOfKits();
        // calculate transitive closure of modules
        TreeMap<String, TreeSet<String>> allModuleDeps = transitiveClosureOfModules();
        // create a map of <module, kits that depend on it>
        TreeMap<ModuleInfo, Set<String>> dependingKits = new TreeMap<ModuleInfo, Set<String>>();
        for (ModuleInfo m : modules) {
            if (regexp != null && !regexp.matcher(m.group).matches()) {
                continue;
            }
            if (m.showInAutoupdate) {
                // this is a kit
                Set<String> dep = allModuleDeps.get(m.codebasename);
                for (String ds : dep) {
                    if (regexp != null && !regexp.matcher(m.group).matches()) {
                        continue;
                    }
                    //log ("ds " + dep);
                    ModuleInfo theModuleOneIsDependingOn = findModuleInfo(ds);
                    if (!theModuleOneIsDependingOn.showInAutoupdate && 
                        !theModuleOneIsDependingOn.isAutoload &&
                        !theModuleOneIsDependingOn.isEager &&
                        !theModuleOneIsDependingOn.isEssential) {
                        // regular module, not a kit
                        Set<String> kits = dependingKits.get(theModuleOneIsDependingOn);
                        if (kits == null) {
                            kits = new TreeSet<String>();
                            dependingKits.put(theModuleOneIsDependingOn, kits);
                        }
                        kits.add(m.getName(false));
//                            w.print("  REQUIRES " + theModuleOneIsDependingOn.getName());
//                            w.println();
                    }
                }
            }
        }
        // now check that there is one canonical kit that "contains" the module
        // at the same time create a map of <kit, set of <module>>
        TreeMap<String, TreeSet<String>> allKits = new TreeMap<String, TreeSet<String>>();
        for (ModuleInfo module : dependingKits.keySet()) {
            Set<String> kits = dependingKits.get(module);
            // candidate for the lowest kit
            String lowestKitCandidate = null;
            for (String kit : kits) {
                if (lowestKitCandidate == null) {
                    lowestKitCandidate = kit;
                    log ("  initial lowest kit candidate for " + module.getName(false) + " : " +
                            lowestKitCandidate, Project.MSG_DEBUG);
                }
                else {
                    if (dependsOnTransitively(lowestKitCandidate, kit, allKitDeps)) {
                        lowestKitCandidate = kit;
                        log ("  new lowest kit candidate for " + module.getName(false) + " : " +
                            lowestKitCandidate, Project.MSG_DEBUG);
                    }
                }
            }
            // check that all kits depend on the lowest kit candidate
            boolean passed = true;
            for (String kit : kits) {
                if (!kit.equals(lowestKitCandidate) && 
                    !dependsOnTransitively(kit, lowestKitCandidate, allKitDeps)) {
                    log ("lowest kit not found for " + module.getName(false) + " : " +
                            lowestKitCandidate + ", " + kit + " do not have a dependency", Project.MSG_VERBOSE);
                    passed = false;
                    break;
                }
            }
            if (passed) {
                dependingKits.put(module, Collections.singleton(lowestKitCandidate));
                registerModuleInKit(module, lowestKitCandidate, allKits);
            } else {
                w.print("Warning: ambiguous module ownership - module ");
                w.print(module.getName(false));
                w.print(" is contained in kits ");
                w.println();
                for (String kit : kits) {
                    registerModuleInKit(module, kit, allKits);
                    w.print("  " + kit);
                    w.println();
                }
                w.println("No dependency between ");
                for (String kit : kits) {
                    if (!kit.equals(lowestKitCandidate) && 
                        !dependsOnTransitively(kit, lowestKitCandidate, allKitDeps)) {
                        w.println ("  " + lowestKitCandidate + ", " + kit);
                    }
                }
            }
        }
        // now actually print out the kit contents
        for (String kit : allKits.keySet()) {
            w.print("KIT ");
            w.print(kit);
            w.println();
            for (String m : allKits.get(kit)) {
                w.print("  CONTAINS " + m);
                w.println();
            }
        }
        
        w.close();
    }

    private boolean dependsOnTransitively(String kit1, String kit2, 
        TreeMap<String, TreeSet<String>> dependingKits) {
        TreeSet kits = dependingKits.get(kit1);
        if (kits == null) {
            return false;
        }
        return kits.contains(kit2);
    }

    private static void registerModuleInKit(ModuleInfo module, String kit, TreeMap<String, TreeSet<String>> allKits) {
        TreeSet<String> modules = allKits.get(kit);
        if (modules == null) {
            modules = new TreeSet<String>();
            allKits.put(kit, modules);
        }
        modules.add(module.getName(false));
    }

    private TreeMap<String, TreeSet<String>> transitiveClosureOfModules() {
        TreeMap<String, TreeSet<String>> moduleDepsAll = new TreeMap<String, TreeSet<String>>();
        // populate with modules first
        for (ModuleInfo m : modules) {
            TreeSet<String> deps = new TreeSet<String>();
            moduleDepsAll.put(m.codebasename, deps);
            for (Dependency d : m.depends) {
                if (!d.isSpecial()) {
                    ModuleInfo theModuleOneIsDependingOn = findModuleInfo(d, m);
                    deps.add(theModuleOneIsDependingOn.codebasename);
                }
            }
        }
        transitiveClosure(moduleDepsAll);
        return moduleDepsAll;
    }
    
    
    private TreeMap<String, TreeSet<String>> transitiveClosureOfKits() {
        TreeMap<String, TreeSet<String>> kitDepsAll = new TreeMap<String, TreeSet<String>>();
        // populate with kits first
        for (ModuleInfo m : modules) {
            if (m.showInAutoupdate) {
                TreeSet<String> deps = new TreeSet<String>();
                kitDepsAll.put(m.getName(false), deps);
                for (Dependency d : m.depends) {
                    if (!d.isSpecial()) {
                        ModuleInfo theModuleOneIsDependingOn = findModuleInfo(d, m);
                        if (theModuleOneIsDependingOn.showInAutoupdate) {
                            deps.add(theModuleOneIsDependingOn.getName(false));
                        }
                    }
                }
            }
        }
        transitiveClosure(kitDepsAll);
        return kitDepsAll;
    }
    
    /** Computes the transitive closure of the dependency map passed as a parameter.
     * 
     * @param deps the dependency map, will contain the transitive closure when the method exits
     */
    private void transitiveClosure(TreeMap<String, TreeSet<String>> allDeps) {
        // add transitively all dependencies
        boolean needAnotherIteration = true;
        while (needAnotherIteration) {
            needAnotherIteration = false;
            for (String m: allDeps.keySet()) {
                TreeSet<String> deps = allDeps.get(m);
                for (String d : new TreeSet<String>(deps)) {
                    for (String d2: allDeps.get(d)) {
                        if (!deps.contains(d2)) {
                            log ("transitive closure: need to add " + d2 + " to " + m, Project.MSG_DEBUG);
                            deps.add(d2);
                            needAnotherIteration = true;
                        }
                    }
                }

            }
        }       
    }

    private void generateKitDependencies(File output) throws BuildException, IOException {
        PrintWriter w = new PrintWriter(new FileWriter(output));
        for (ModuleInfo m : modules) {
            if (regexp != null && !regexp.matcher(m.group).matches()) {
                continue;
            }
            if (m.showInAutoupdate) {
                w.print("KIT ");
                w.print(m.getName(false));
                w.println();
                for (Dependency d : m.depends) {
                    if (regexp != null && !regexp.matcher(m.group).matches()) {
                        continue;
                    }
                    if (!d.isSpecial()) {
                        ModuleInfo theModuleOneIsDependingOn = findModuleInfo(d, m);
                        if (theModuleOneIsDependingOn.showInAutoupdate) {
                            w.print("  REQUIRES " + theModuleOneIsDependingOn.getName(false));
                            w.println();
                        }
                    }
                }
            }
        }
        w.close();
    }

    private void generateSharedPackages (File output) throws BuildException, IOException {
        TreeMap<String,List<ModuleInfo>> packages = new TreeMap<String,List<ModuleInfo>>();
        
        for (ModuleInfo m : modules) {
            HashSet<String> pkgs = new HashSet<String>();
            iterateSharedPackages(m.file, pkgs);
            for (String s : pkgs) {
                List<ModuleInfo> l = packages.get(s);
                if (l == null) {
                    l = new ArrayList<ModuleInfo>();
                    packages.put(s, l);
                }
                l.add(m);
            }
        }
        
        PrintWriter w = new PrintWriter (new FileWriter (output));
        for (Map.Entry<String,List<ModuleInfo>> entry : packages.entrySet()) {
            String out = entry.getKey();
            List<ModuleInfo> cnt = entry.getValue();
            if (cnt.size() > 1) {
                log("Package " + out + " is shared between:", Project.MSG_VERBOSE);
                boolean doPrint = regexp == null;
                for (ModuleInfo m : cnt) {
                    log ("   " + m.codebasename, Project.MSG_VERBOSE);
                    if (regexp != null && regexp.matcher(m.group).matches()) {
                        doPrint = true;
                    }
                }
                if (doPrint) {
                    w.println (out.replace ('/', '.'));
                }
            }
        }
        w.close ();
    }
    
    private void iterateSharedPackages (File f, Set<String> myPkgs) throws IOException {
        JarFile file = new JarFile (f);
        Enumeration<JarEntry> en = file.entries ();
        LOOP: while (en.hasMoreElements ()) {
            JarEntry e = en.nextElement ();
            if (e.getName ().endsWith ("/")) {
                continue;
            }
            if (e.getName ().startsWith ("META-INF/")) {
                continue;
            }
            
            int last = e.getName ().lastIndexOf ('/');
            String pkg = last == -1 ? "" : e.getName ().substring (0, last);
            myPkgs.add (pkg);
            log("Found package " + pkg + " in " + f, Project.MSG_DEBUG);
        }
        
        Manifest m = file.getManifest();
        if (m != null) {
            String value = m.getMainAttributes ().getValue ("Class-Path");
            if (value != null) {
                StringTokenizer tok = new StringTokenizer (value, " ");
                while (tok.hasMoreElements ()) {
                    File sub = new File (f.getParentFile (), tok.nextToken ());
                    if (sub.isFile ()) {
                        iterateSharedPackages (sub, myPkgs);
                    }
                }
            }
        }
        
        file.close ();
    }
    
    private void generateDependencies (File output, boolean implementationOnly) throws BuildException, IOException {
        PrintWriter w = new PrintWriter (new FileWriter (output));
        for (ModuleInfo m : modules) {
            boolean first = true;
            for (Dependency d : m.depends) {
                if (d.getName().startsWith("org.openide.modules.ModuleFormat")) {
                    continue; // just clutter
                }
                String print = "  REQUIRES ";
                if (d.exact && d.compare != null) {
                    // ok, impl deps
                } else {
                    if (implementationOnly) {
                        continue;
                    }
                }
                if (regexp != null && !regexp.matcher(m.group).matches()) {
                    continue;
                }
                
                if (first) {
                    w.print ("MODULE ");
                    w.print (m.getName (false));
                    w.println ();
                    first = false;
                }
                w.print (print);
                if (d.isSpecial ()) {
                    w.print (d.getName ());
                } else {
                    ModuleInfo theModuleOneIsDependingOn = findModuleInfo(d, m);
                    w.print (theModuleOneIsDependingOn.getName (false));
                }
                w.println ();
            }
        }
        w.close ();
    }
    
    private void generateGroupDependencies (File output, boolean implementationOnly) throws BuildException, IOException {
        PrintWriter w = new PrintWriter (new FileWriter (output));

        Map<Dependency,Set<ModuleInfo>> referrers = new HashMap<Dependency,Set<ModuleInfo>>();
        
        TreeMap<String, Set<Dependency>> groups = new TreeMap<String, Set<Dependency>>();
        for (ModuleInfo m : modules) {
            if (regexp != null && !regexp.matcher(m.group).matches()) {
                continue;
            }
            Set<Dependency> l = groups.get(m.group);
            if (l == null) {
                l = new TreeSet<Dependency>();
                groups.put(m.group, l);
            }
            l.addAll(m.depends);
            for (Dependency d : m.depends) {
                Set<ModuleInfo> r = referrers.get(d);
                if (r == null) {
                    r = new HashSet<ModuleInfo>();
                    referrers.put(d, r);
                }
                r.add(m);
            }
        }

        for (Map.Entry<String,Set<Dependency>> e : groups.entrySet()) {
            String groupName = e.getKey();
            Set<Dependency> depends = e.getValue();
            
            boolean first = true;
            for (Dependency d : depends) {
                String print = "  REQUIRES ";
                if (d.exact && d.compare != null) {
                    // ok, impl deps
                } else {
                    if (implementationOnly) {
                        continue;
                    }
                }
                
                // special dependencies are ignored
                if (d.isSpecial ()) {
                    continue;
                }
                // dependencies within one group are not important
                Set<ModuleInfo> r = referrers.get(d);
                ModuleInfo ref = findModuleInfo(d, r.size() == 1 ? r.iterator().next() : null);
                if (groupName.equals (ref.group)) {
                    continue;
                }
                
                if (first) {
                    w.print ("GROUP ");
                    w.print (groupName);
                    w.println ();
                    first = false;
                }
                w.print (print);
                w.print (ref.getName (false));
                w.println ();
            }
        }
        w.close ();
    }
    
    /** For a given dependency finds the module that this dependency refers to.
     */
    private ModuleInfo findModuleInfo(Dependency dep, ModuleInfo referrer) throws BuildException {
        for (ModuleInfo info : modules) {
            if (dep.isDependingOn (info)) {
                return info;
            }
        }
        
        throw new BuildException ("Cannot find module that satisfies dependency: " + dep + (referrer != null ? " from: " + referrer : ""));
    }
    /** For a given codebasename finds module that we depend on
     */
    private ModuleInfo findModuleInfo (String cnb) throws BuildException {
        for (ModuleInfo info : modules) {
            if (info.codebasename.equals(cnb)) {
                return info;
            }
        }
        
        return null;
    }
    
    private static void addDependencies (TreeSet<Dependency> addTo, java.util.jar.Manifest man, int dependencyType, String attrName) throws BuildException {
        String value = man.getMainAttributes ().getValue (attrName);
        if (value == null) {
            return;
        }
        
        StringTokenizer tok = new StringTokenizer (value, ",");
        while (tok.hasMoreElements ()) {
            String nextDep = tok.nextToken ();
            StringTokenizer dep = new StringTokenizer (nextDep, "=>", true);
            if (dep.countTokens () == 1) {
                addTo.add (new Dependency (dep.nextToken ().trim (), dependencyType, false, null));
                continue;
            } 
                
            if (dep.countTokens () == 3) {
                String name = dep.nextToken ().trim ();
                String equal = dep.nextToken ().trim ();
                String comp = dep.nextToken ().trim ();
                addTo.add (new Dependency (name, dependencyType, equal.equals ("="), comp));
                continue;
            }
            
            throw new BuildException ("Cannot parse dependency: " + value);
        }
    }
    
    public static final class Input extends Object {
        public FileSet jars;
        public String name;
        
        public FileSet createJars() {
            if (jars != null) throw new BuildException ();
            jars = new FileSet();
            return jars;
        }
        
        public void setName (String name) {
            this.name = name;
        }
    }
    
    public static final class Output extends Object {
        public OutputType type;
        public File file;
        
        public void setType (OutputType type) {
            this.type = type;
        }
        
        public void setFile (File file) {
            this.file = file;
        }
    }
    
    public static final class OutputType extends EnumeratedAttribute {
        public String[] getValues () {
            return new String[] { 
                "public-packages",
                "friend-packages",
                "shared-packages",
                "modules",
                "dependencies",
                "implementation-dependencies",
                "group-dependencies",
                "group-implementation-dependencies",
                "group-friend-packages",
                "external-libraries",
                "kits",
                "kit-dependencies",
            };
        }
    }
    
    private static final class ModuleInfo extends Object implements Comparable<ModuleInfo> {
        public final String group;
        public final File file;
        public final String codebasename;
        public String publicPackages;
	public Set<String> friends;
        public int majorVersion;
        public String specificationVersion;
        public String implementationVersion;
        public Set<Dependency> depends;
        public Set<Dependency> provides;
        public boolean showInAutoupdate;
        public boolean isEssential;
        public boolean isAutoload;
        public boolean isEager;
        
        public ModuleInfo (String g, File f, String a) {
            this.group = g;
            this.file = f;
            this.codebasename = a;
        }

        public int compareTo(ModuleInfo m) {
            return codebasename.compareTo (m.codebasename);
        }

        public @Override boolean equals(Object obj) {
            if (obj instanceof ModuleInfo) {
                return codebasename.equals(((ModuleInfo) obj).codebasename);
            }
            return false;
        }

        public @Override int hashCode() {
            return codebasename.hashCode ();
        }
        
        public String getName(boolean includeMajorVersion) {
            if (!includeMajorVersion || majorVersion == -1) {
                return codebasename + " (" + group + ")";
            } else {
                return codebasename + "/" + majorVersion + " (" + group + ")";
            }
        }

        public @Override String toString() {
            return "ModuleInfo[" + getName (false) + "]";
        }
    } // end of ModuleInfo
    
    private static final class Dependency extends Object implements Comparable<Dependency> {
        public static final int PROVIDES = 1;
        public static final int REQUIRES = 2;
        
        public final String token;
        public final int majorVersionFrom;
        public final int majorVersionTo;
        public final int type;
        public final boolean exact;
        public final String compare;
        
        
        public Dependency (String token, int type, boolean exact, String compare) {
            // base name
            int slash = token.indexOf ('/');
            if (slash == -1) {
                this.token = token;
                this.majorVersionFrom = -1;
                this.majorVersionTo = -1;
            } else {
                this.token = token.substring (0, slash);
                
                String major = token.substring (slash + 1);
                int range = major.indexOf ('-');
                if (range == -1) {
                    this.majorVersionFrom = Integer.valueOf(major);
                    this.majorVersionTo = majorVersionFrom;
                } else {
                    this.majorVersionFrom = Integer.valueOf(major.substring(0, range));
                    this.majorVersionTo = Integer.valueOf(major.substring(range + 1));
                }
            }
            this.type = type;
            this.exact = exact;
            this.compare = compare;
        }
        public int compareTo(Dependency m) {
            return token.compareTo (m.token);
        }

        public @Override boolean equals(Object obj) {
            if (obj instanceof Dependency) {
                return token.equals(((Dependency) obj).token);
            }
            return false;
        }

        public @Override int hashCode() {
            return token.hashCode ();
        }
        
        /** These dependencies do not represent deps on real modules or
         * tokens provided by real modules.
         */
        public boolean isSpecial () {
            return token.startsWith ("org.openide.modules.os") ||
                   token.startsWith ("org.openide.modules.ModuleFormat");
        }
        
        public boolean isDependingOn (ModuleInfo info) {
            if (info.codebasename.equals (token)) {
                return (majorVersionFrom == -1 || majorVersionFrom <= info.majorVersion) &&
                        (majorVersionTo == -1 || info.majorVersion <= majorVersionTo);
            } 
            
            for (Dependency d : info.provides) {
                if (d.equals (this)) {
                    return true;
                }
            }
            
            return false;
        }
        
        public String getName () {
            return token;
        }
        
        public @Override String toString() {
            String t;
            switch (type) {
                case REQUIRES: t = "requires "; break;
                case PROVIDES: t = "provides "; break;
                default:
                    throw new IllegalStateException ("Unknown type: " + type);
            }
            
            return "Dependency[" + t + getName () + "]";
        }

    } // end of Dependency
}
