/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.*;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.tools.ant.BuildException;

/** This task implements the module dependencies verification proposal 
 * that is described at
 * http://openide.netbeans.org/proposals/arch/clusters.html#verify-solution
 *
 *
 */
public class ModuleDependencies extends org.apache.tools.ant.Task {
    private List inputs = new ArrayList ();
    private List outputs = new ArrayList ();
    private Set modules;
    
    public ModuleDependencies () {
    }
    
    public Input createInput () throws org.apache.tools.ant.BuildException {
        Input input = new Input ();
        inputs.add (input);
        return input;
    }
    
    public Output createOutput () throws org.apache.tools.ant.BuildException {
        Output output = new Output ();
        outputs.add (output);
        return output;
    }

    public void execute () throws BuildException {
        if (outputs.size () == 0) throw new BuildException ("At least one <output> tag has to be specified");

        try {
            modules = readModuleInfo ();

            Iterator it = outputs.iterator ();
            while (it.hasNext ()) {
                Output o = (Output)it.next ();
                if (o.type == null) throw new BuildException ("<output> needs attribute type");
                if (o.file == null) throw new BuildException ("<output> needs attribute file");
                
                getProject ().log ("Generating " + o.type + " to " + o.file);
                
                if ("public-packages".equals (o.type.getValue ())) {
                    generatePublicPackages (o.file);
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
            }
        
        } catch (IOException ex) {
            throw new BuildException (ex);
        }
    }
    
    private Set readModuleInfo () throws IOException {
        TreeSet modules = new TreeSet ();
        
        Iterator it = inputs.iterator (); 
        if (!it.hasNext ()) throw new BuildException ("At least one <input> tag is needed");
        while (it.hasNext ()) {
            Input input = (Input)it.next ();
            if (input.jars == null) throw new BuildException ("<input> needs a subelement <jars>");
            if (input.name == null) throw new BuildException ("<input> needs attribute name");
            
            org.apache.tools.ant.Project p = getProject ();
            org.apache.tools.ant.DirectoryScanner scan = input.jars.getDirectoryScanner (p);
            String[] arr = scan.getIncludedFiles ();
            for (int i = 0; i < arr.length; i++) {
                File f = new File (scan.getBasedir (), arr[i]);
                getProject ().log ("Processing " + f, getProject ().MSG_VERBOSE);
                JarFile file = new JarFile (f);
                
                java.util.jar.Manifest manifest = file.getManifest ();
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
                        majorVersion = Integer.valueOf (module.substring (slash + 1)).intValue ();
                    }
                    m = new ModuleInfo (input.name, f, codebasename);
                    m.majorVersion = majorVersion;
                }

                m.publicPackages = file.getManifest ().getMainAttributes ().getValue ("OpenIDE-Module-Public-Packages");

                {
                    m.specificationVersion = file.getManifest ().getMainAttributes ().getValue ("OpenIDE-Module-Specification-Version");
                }

                m.implementationVersion = file.getManifest ().getMainAttributes ().getValue ("OpenIDE-Module-Implementation-Version");

                TreeSet depends = new TreeSet ();
                TreeSet provides = new TreeSet ();
                addDependencies (depends, file.getManifest (), Dependency.REQUIRES, "OpenIDE-Module-Requires");
                addDependencies (provides, file.getManifest (), Dependency.PROVIDES, "OpenIDE-Module-Provides");
                {
                    String ideDeps = file.getManifest ().getMainAttributes ().getValue ("OpenIDE-Module-IDE-Dependencies"); // IDE/1 > 4.25
                    if (ideDeps != null) {
                        StringTokenizer tok = new StringTokenizer (ideDeps, "> ");
                        if (tok.countTokens () != 2 || !tok.nextToken ().equals ("IDE/1")) {
                            throw new BuildException ("Wrong OpenIDE-Module-IDE-Dependencies: " + ideDeps);
                        }

                        depends.add (new Dependency ("org.openide/1", Dependency.REQUIRES, false, tok.nextToken ()));

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

                modules.add (m);
            }
        }
        
        return modules;
    }
    
    
    private void generatePublicPackages (File output) throws BuildException, IOException {
        TreeSet packages = new TreeSet ();
        
        Iterator it = modules.iterator ();
        while (it.hasNext ()) {
            ModuleInfo m = (ModuleInfo)it.next ();

            String s = m.publicPackages;
            HashMap pkgs = null;
            if (s != null) {
                pkgs = new HashMap ();
                StringTokenizer tok = new StringTokenizer (s, ",");
                while (tok.hasMoreElements ()) {
                    String p = tok.nextToken ().trim ();
                    if (p.equals ("-")) {
                        continue;
                    }
                    
                    if (p.endsWith (".*")) {
                        pkgs.put (p.substring (0, p.length () - 2).replace ('.', '/'), Boolean.FALSE);
                        continue;
                    }
                    if (p.endsWith (".**")) {
                        pkgs.put (p.substring (0, p.length () - 3).replace ('.', '/'), Boolean.TRUE);
                        continue;
                    }
                    throw new BuildException ("Unknown package format: " + p + " in " + m.file);
                }
            }

            iterateThruPackages (m.file, pkgs, packages);

            if (pkgs != null && packages.size () < pkgs.size ()) {
                throw new BuildException ("Not enough packages found. The declared packages are: " + s + " but only " + packages + " were found in " + m.file);
            }
        }

        PrintWriter w = new PrintWriter (new FileWriter (output));
        it = packages.iterator ();
        while (it.hasNext ()) {
            String out = (String)it.next ();
            w.println (out.replace ('/', '.'));
        }
        w.close ();
    }
    
    private void iterateThruPackages (File f, HashMap pkgs, TreeSet packages) throws IOException {
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
        Iterator it = modules.iterator ();
        while (it.hasNext ()) {
            ModuleInfo m = (ModuleInfo)it.next ();
            w.print ("MODULE ");
            w.print (m.getName ());
            w.println ();
        }
        w.close ();
    }

    private void generateDependencies (File output, boolean implementationOnly) throws BuildException, IOException {
        PrintWriter w = new PrintWriter (new FileWriter (output));
        Iterator it = modules.iterator ();
        while (it.hasNext ()) {
            ModuleInfo m = (ModuleInfo)it.next ();

            boolean first = true;
            Iterator deps = m.depends.iterator ();
            while (deps.hasNext ()) {
                Dependency d = (Dependency)deps.next ();
                String print = "  REQUIRES ";
                if (d.exact && d.compare != null) {
                    // ok, impl deps
                } else {
                    if (implementationOnly) {
                        continue;
                    }
                }
                if (first) {
                    w.print ("MODULE ");
                    w.print (m.getName ());
                    w.println ();
                    first = false;
                }
                w.print (print);
                if (d.isSpecial ()) {
                    w.print (d.getName ());
                } else {
                    ModuleInfo theModuleOneIsDependingOn = findModuleInfo (d);
                    w.print (theModuleOneIsDependingOn.getName ());
                }
                w.println ();
            }
        }
        w.close ();
    }
    
    private void generateGroupDependencies (File output, boolean implementationOnly) throws BuildException, IOException {
        PrintWriter w = new PrintWriter (new FileWriter (output));
        
        // <String, Set<Dependency>>
        TreeMap groups = new TreeMap ();
        {
            Iterator it = modules.iterator ();

            while (it.hasNext ()) {
                ModuleInfo m = (ModuleInfo)it.next ();
                Set l = (Set)groups.get (m.group);
                if (l == null) {
                    l = new TreeSet ();
                    groups.put (m.group, l);
                }

                l.addAll (m.depends);
            }
        }

        Iterator it = groups.entrySet ().iterator ();
        while (it.hasNext ()) {
            Map.Entry e = (Map.Entry)it.next ();
            String groupName = (String)e.getKey ();
            Set depends = (Set)e.getValue ();
            
            boolean first = true;
            Iterator deps = depends.iterator ();
            while (deps.hasNext ()) {
                Dependency d = (Dependency)deps.next ();

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
                ModuleInfo ref = findModuleInfo (d);
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
                w.print (ref.getName ());
                w.println ();
            }
        }
        w.close ();
    }
    
    /** For a given dependency finds the module that this dependency refers to.
     */
    private ModuleInfo findModuleInfo (Dependency dep) throws BuildException {
        Iterator it = modules.iterator ();
        while (it.hasNext ()) {
            ModuleInfo info = (ModuleInfo)it.next ();
            if (dep.isDependingOn (info)) {
                return info;
            }
        }
        
        throw new BuildException ("Cannot find module that satisfies dependency: " + dep);
    }
    
    private static void addDependencies (TreeSet addTo, java.util.jar.Manifest man, int dependencyType, String attrName) throws BuildException {
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
        public org.apache.tools.ant.types.FileSet jars;
        public String name;
        
        public org.apache.tools.ant.types.FileSet createJars () {
            if (jars != null) throw new BuildException ();
            jars = new org.apache.tools.ant.types.FileSet ();
            return jars;
        }
        
        public void setName (String name) {
            this.name = name;
        }
    }
    
    public static final class Output extends Object {
        public OutputType type;
        public java.io.File file;
        
        public void setType (OutputType type) {
            this.type = type;
        }
        
        public void setFile (File file) {
            this.file = file;
        }
    }
    
    public static final class OutputType extends org.apache.tools.ant.types.EnumeratedAttribute {
        public String[] getValues () {
            return new String[] { 
                "public-packages",
                "modules",
                "dependencies",
                "implementation-dependencies",
                "group-dependencies",
                "group-implementation-dependencies",
            };
        }
    }
    
    private static final class ModuleInfo extends Object implements Comparable {
        public final String group;
        public final File file;
        public final String codebasename;
        public String publicPackages;
        public int majorVersion;
        public String specificationVersion;
        public String implementationVersion;
        public Set/*<Dependency>*/ depends;
        public Set/*<Dependency>*/ provides;
        
        public ModuleInfo (String g, File f, String a) {
            this.group = g;
            this.file = f;
            this.codebasename = a;
        }

        public int compareTo (Object o) {
            ModuleInfo m = (ModuleInfo)o;
            return codebasename.compareTo (m.codebasename);
        }

        public boolean equals (Object obj) {
            if (obj instanceof ModuleInfo) {
                return compareTo (obj) == 0;
            }
            return false;
        }

        public int hashCode () {
            return codebasename.hashCode ();
        }
        
        public String getName () {
            if (majorVersion == -1) {
                return codebasename + " (" + group + ")";
            } else {
                return codebasename + "/" + majorVersion + " (" + group + ")";
            }
        }

        public String toString () {
            return "ModuleInfo[" + getName () + "]";
        }
    } // end of ModuleInfo
    
    private static final class Dependency extends Object implements Comparable {
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
                    this.majorVersionFrom = Integer.valueOf (major).intValue ();
                    this.majorVersionTo = majorVersionFrom;
                } else {
                    this.majorVersionFrom = Integer.valueOf (major.substring (0, range)).intValue ();
                    this.majorVersionTo = Integer.valueOf (major.substring (range + 1)).intValue ();
                }
            }
            this.type = type;
            this.exact = exact;
            this.compare = compare;
        }
        public int compareTo (Object o) {
            Dependency m = (Dependency)o;
            return token.compareTo (m.token);
        }

        public boolean equals (Object obj) {
            if (obj instanceof Dependency) {
                return compareTo (obj) == 0;
            }
            return false;
        }

        public int hashCode () {
            return token.hashCode ();
        }
        
        /** These dependencies do not represent deps on real modules or
         * tokens provided by real modules.
         */
        public boolean isSpecial () {
            return token.startsWith ("org.openide.modules.os");
        }
        
        public boolean isDependingOn (ModuleInfo info) {
            if (info.codebasename.equals (token)) {
                return majorVersionTo <= info.majorVersion && majorVersionFrom <= info.majorVersion;
            } 
            
            Iterator it = info.provides.iterator ();
            while (it.hasNext ()) {
                Dependency d = (Dependency)it.next ();
                if (d.equals (this)) {
                    return true;
                }
            }
            
            return false;
        }
        
        public String getName () {
            if (majorVersionFrom == -1 && majorVersionTo == -1) {
                return token;
            } else {
                if (majorVersionTo == majorVersionFrom) {
                    return token + "/" + majorVersionFrom;
                } else {
                    return token + "/" + majorVersionFrom + "-" + majorVersionTo;
                }
            }
            
        }
        
        public String toString () {
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
