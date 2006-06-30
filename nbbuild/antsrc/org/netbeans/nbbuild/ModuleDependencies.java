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
    private Set external;
    
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
            readModuleInfo ();

            Iterator it = outputs.iterator ();
            while (it.hasNext ()) {
                Output o = (Output)it.next ();
                if (o.type == null) throw new BuildException ("<output> needs attribute type");
                if (o.file == null) throw new BuildException ("<output> needs attribute file");
                
                getProject ().log ("Generating " + o.type + " to " + o.file);
                
                if ("public-packages".equals (o.type.getValue ())) {
                    generatePublicPackages (o.file, true);
                    continue;
                }
                if ("friend-packages".equals (o.type.getValue ())) {
                    generatePublicPackages (o.file, false);
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
                if ("external-libraries".equals (o.type.getValue ())) {
                    generateExternalLibraries (o.file);                    
                    continue;
                }
            }
        
        } catch (IOException ex) {
            throw new BuildException (ex);
        }
    }
    
    private void readModuleInfo () throws IOException {
        modules = new TreeSet ();
        
        class Comp implements java.util.Comparator {
            public int compare (Object o1, Object o2) {
                File f1 = (File)o1;
                File f2 = (File)o2;

                return f1.getName ().compareTo (f2.getName ());
            }
        }
        external = new TreeSet (new Comp ());
        
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
                    external.add (f);
                    continue;
                }
                
                String module = manifest.getMainAttributes ().getValue ("OpenIDE-Module");
                
                
                if (module == null) {
                    // skip this one
                    if (manifest.getMainAttributes ().getValue ("NetBeans-Own-Library") == null) {
                        external.add (f);
                    }
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
			TreeSet set = new TreeSet();
                        StringTokenizer tok = new StringTokenizer (friends, ", ");
			while (tok.hasMoreElements()) {
			    set.add(tok.nextElement());
			}
			m.friends = set;
                    }
                }

                modules.add (m);
            }
        }
    }
    
    
    private void generatePublicPackages (File output, boolean justPublic) throws BuildException, IOException {
        TreeSet packages = new TreeSet ();
	TreeMap friendExports = new TreeMap();
        
        Iterator it = modules.iterator ();
        while (it.hasNext ()) {
            ModuleInfo m = (ModuleInfo)it.next ();
	    if (justPublic) {
		if (m.friends != null) {
		    continue;
		}
	    }

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
	    
	    if (justPublic) {
		iterateThruPackages (m.file, pkgs, packages);
		if (pkgs != null && packages.size () < pkgs.size ()) {
		    throw new BuildException ("Not enough packages found. The declared packages are: " + s + " but only " + packages + " were found in " + m.file);
		}
	    } else {
		TreeSet modulePkgs = new TreeSet();
		iterateThruPackages (m.file, pkgs, modulePkgs);
		friendExports.put(m, modulePkgs);
	    }

        }

        PrintWriter w = new PrintWriter (new FileWriter (output));
	if (justPublic) {
	    it = packages.iterator ();
	    while (it.hasNext ()) {
		String out = (String)it.next ();
		w.println (out.replace ('/', '.'));
	    }
	} else {
            int maxFriends = Integer.MAX_VALUE;
            String maxFriendsString = this.getProject().getProperty("deps.max.friends");
            if (maxFriendsString != null) {
                maxFriends = Integer.parseInt(maxFriendsString);
            }
            
	    it = friendExports.entrySet().iterator();
	    while (it.hasNext()) {
		Map.Entry entry = (Map.Entry)it.next();
		ModuleInfo info = (ModuleInfo)entry.getKey();
		if (info.friends == null) {
		    continue;
		}
		log("Friends for " + info.getName(), org.apache.tools.ant.Project.MSG_DEBUG);
		w.print("MODULE ");
		w.println(info.getName());
		Iterator iterFrnd = info.friends.iterator();
		while(iterFrnd.hasNext()) {
		    String n = (String)iterFrnd.next();
		    ModuleInfo friend = findModuleInfo(n);
		    if (friend != null) {
			w.print("  FRIEND ");
			w.println(friend.getName());
		    } else {
			w.print("  EXTERNAL ");
			w.println(n);
		    }
		}
                if (info.friends.size() > maxFriends) {
                    throw new BuildException("Too many friends (" + info.friends.size() + ") for module " + info.getName());
                }
                
		Set/*<String>*/ pkgs = (Set/*<String>*/)entry.getValue();
		Iterator iterPkgs = pkgs.iterator();
		while (iterPkgs.hasNext()) {
		    String out = (String)iterPkgs.next ();
		    w.print("  PACKAGE ");
		    w.println (out.replace ('/', '.'));
		}
	    }
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
    
    private void generateExternalLibraries (File output) throws BuildException, IOException {
        PrintWriter w = new PrintWriter (new FileWriter (output));
        Iterator it = external.iterator ();
        
        String SPACES = "                                                     ";
        while (it.hasNext ()) {
            File f = (File)it.next ();
            
            java.security.MessageDigest dig;
            
            try {
                dig = java.security.MessageDigest.getInstance ("MD5");
            } catch (java.security.NoSuchAlgorithmException ex) {
                throw new BuildException (ex);
            }
            InputStream is = new BufferedInputStream (new FileInputStream (f));
            byte[] arr = new byte[4092];
            for (;;) {
                int len = is.read (arr);
                if (len == -1) {
                    break;
                }
                dig.update (arr, 0, len);
            }
            
            byte[] res = dig.digest ();
            is.close ();
            
            w.print ("LIBRARY ");
            w.print ((f.getName () + SPACES).substring (0, 50));
            String size = SPACES + f.length ();
            w.print (size.substring (size.length () - 15));
            w.print (" ");
            for (int i = 0; i < res.length; i++) {
                String hex = "00" + Integer.toHexString (res[i]);
                w.print (hex.substring (hex.length () - 2));
            }
            w.println ();
        }
        w.close ();
    }

    private void generateSharedPackages (File output) throws BuildException, IOException {
        TreeMap packages = new TreeMap ();
        
        Iterator it = modules.iterator ();
        while (it.hasNext ()) {
            ModuleInfo m = (ModuleInfo)it.next ();

            HashSet pkgs = new HashSet ();
            iterateSharedPackages (m.file, pkgs);
            
            Iterator j = pkgs.iterator ();
            while (j.hasNext ()) {
                String s = (String)j.next ();
                List l = (List)packages.get(s);
                if (l == null) {
                    l = new ArrayList();
                    packages.put(s, l);
                }
                l.add (m);
            }
        }

        PrintWriter w = new PrintWriter (new FileWriter (output));
        it = packages.entrySet ().iterator ();
        while (it.hasNext ()) {
            Map.Entry entry = (Map.Entry)it.next ();
            String out = (String)entry.getKey ();
            List cnt = (List)entry.getValue ();
            if (cnt.size() > 1) {
                w.println (out.replace ('/', '.'));
                log("Package " + out + " is shared between:", org.apache.tools.ant.Project.MSG_VERBOSE);
                Iterator j = cnt.iterator ();
                while (j.hasNext ()) {
                    ModuleInfo m = (ModuleInfo)j.next ();
                    log ("   " + m.codebasename, org.apache.tools.ant.Project.MSG_VERBOSE);
                }
            }
        }
        w.close ();
    }
    
    private void iterateSharedPackages (File f, Set myPkgs) throws IOException {
        JarFile file = new JarFile (f);
        Enumeration en = file.entries ();
        LOOP: while (en.hasMoreElements ()) {
            JarEntry e = (JarEntry)en.nextElement ();
            if (e.getName ().endsWith ("/")) {
                continue;
            }
            if (e.getName ().startsWith ("META-INF/")) {
                continue;
            }
            
            int last = e.getName ().lastIndexOf ('/');
            String pkg = last == -1 ? "" : e.getName ().substring (0, last);
            myPkgs.add (pkg);
            log("Found package " + pkg + " in " + f, getProject().MSG_DEBUG);
        }
        
        java.util.jar.Manifest m = file.getManifest ();
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
    /** For a given codebasename finds module that we depend on
     */
    private ModuleInfo findModuleInfo (String cnb) throws BuildException {
        Iterator it = modules.iterator ();
        while (it.hasNext ()) {
            ModuleInfo info = (ModuleInfo)it.next ();
            if (info.codebasename.equals(cnb)) {
                return info;
            }
        }
        
        return null;
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
                "friend-packages",
                "shared-packages",
                "modules",
                "dependencies",
                "implementation-dependencies",
                "group-dependencies",
                "group-implementation-dependencies",
                "external-libraries",
            };
        }
    }
    
    private static final class ModuleInfo extends Object implements Comparable {
        public final String group;
        public final File file;
        public final String codebasename;
        public String publicPackages;
	public Set/*String*/ friends;
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
            return token.startsWith ("org.openide.modules.os") ||
                   token.startsWith ("org.openide.modules.ModuleFormat");
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
