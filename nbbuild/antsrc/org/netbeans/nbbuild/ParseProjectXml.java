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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Parse a projectized module's <code>nbproject/project.xml</code> and
 * define various useful Ant properties based on the result.
 * @author Jesse Glick
 */
public final class ParseProjectXml extends Task {

    static final String PROJECT_NS = "http://www.netbeans.org/ns/project/1";
    static final String NBM_NS2 = "http://www.netbeans.org/ns/nb-module-project/2";
    static final String NBM_NS3 = "http://www.netbeans.org/ns/nb-module-project/3";
    
    static final int TYPE_NB_ORG = 0;
    static final int TYPE_SUITE = 1;
    static final int TYPE_STANDALONE = 2;

    private File moduleProject;
    /**
     * Set the NetBeans module project to work on.
     */
    public void setProject(File f) {
        moduleProject = f;
    }
    private File projectFile;
    /**
     * Another option is to directly point to project file.
     * Used only in unit testing.
     */
    public void setProjectFile (File f) {
        projectFile = f;
    }
    private File getProjectFile () {
        if (projectFile != null) {
            return projectFile;
        }
        return new File(new File(moduleProject, "nbproject"), "project.xml");
    }

    private String publicPackagesProperty;
    /**
     * Set the property to set a list of
     * OpenIDE-Module-Public-Packages to.
     */
    public void setPublicPackagesProperty(String s) {
        publicPackagesProperty = s;
    }
    
    private String friendsProperty;
    /**
     * Set the property to set a list of
     * OpenIDE-Module-Friends to.
     */
    public void setFriendsProperty(String s) {
        friendsProperty = s;
    }

    private String javadocPackagesProperty;
    /**
     * Set the property to set a list of public packages for Javadoc
     * to.
     */
    public void setJavadocPackagesProperty(String s) {
        javadocPackagesProperty = s;
    }

    private String moduleDependenciesProperty;
    /**
     * Set the property to set a list of
     * OpenIDE-Module-Module-Dependencies to, based on the list of
     * stated run-time dependencies.
     */
    public void setModuleDependenciesProperty(String s) {
        moduleDependenciesProperty = s;
    }

    private String codeNameBaseDashesProperty;
    /**
     * Set the property to set the module code name base (separated by
     * dashes not dots) to.
     */
    public void setCodeNameBaseDashesProperty(String s) {
        codeNameBaseDashesProperty = s;
    }

    private String codeNameBaseSlashesProperty;
    /**
     * Set the property to set the module code name base (separated by
     * slashes not dots) to.
     */
    public void setCodeNameBaseSlashesProperty(String s) {
        codeNameBaseSlashesProperty = s;
    }

    private String domainProperty;
    /**
     * Set the property to set the module's netbeans.org domain to.
     * Only applicable to modules in netbeans.org (i.e. no <path>).
     */
    public void setDomainProperty(String s) {
        domainProperty = s;
    }

    private String moduleClassPathProperty;
    /**
     * Set the property to set the computed module class path to,
     * based on the list of stated compile-time dependencies.
     */
    public void setModuleClassPathProperty(String s) {
        moduleClassPathProperty = s;
    }

    private String moduleRunClassPathProperty;
    /**
     * Set the property to set the computed module runtime class path to.
     * Currently identical to the regular class path with the exception
     * that original JARs are used, never public-package-only JARs.
     * XXX In the future should however reflect &lt;run-dependency/&gt;
     * rather than &lt;compile-dependency/&gt; 
     */
    public void setModuleRunClassPathProperty(String s) {
        moduleRunClassPathProperty = s;
    }
    
    private File publicPackageJarDir;
    /**
     * Set the location of a directory in which to look for and create
     * JARs containing just the public packages of appropriate
     * compile-time dependencies.
     */
    public void setPublicPackageJarDir(File d) {
        publicPackageJarDir = d;
    }
    
    private String classPathExtensionsProperty;
    /**
     * Set the property to set the declared Class-Path attribute to.
     */
    public void setClassPathExtensionsProperty(String s) {
        classPathExtensionsProperty = s;
    }

    // test distribution path 
    private static String cachedTestDistLocation;

    public static class TestType {
        private String name;
        private String folder;
        private String runtimeCP;
        private String compileCP;
        /** compilation dependency supported only unit tests
         */
        private String compileDep;

        public TestType() {}
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFolder() {
            return folder;
        }

        public void setFolder(String folder) {
            this.folder = folder;
        }

        public String getRuntimeCP() {
            return runtimeCP;
        }

        public void setRuntimeCP(String runtimeCP) {
            this.runtimeCP = runtimeCP;
        }

        public String getCompileCP() {
            return compileCP;
        }

        public void setCompileCP(String compileCP) {
            this.compileCP = compileCP;
        }

        public String getCompileDep() {
            return compileDep;
        }

        public void setCompileDep(String compileDep) {
            this.compileDep = compileDep;
        }
     }
      List<TestType> testTypes = new LinkedList<TestType>();
      
      public void addTestType(TestType testType) {
          testTypes.add(testType);
      }
      public void add(TestType testType) {
          testTypes.add(testType);
      }
  
      
      private TestType getTestType(String name) {
          for (TestType testType : testTypes) {
              if (testType.getName().equals(name)) {
                  return testType;
              }
          }
          return null;
      }
 
 
    private void define(String prop, String val) {
        log("Setting " + prop + "=" + val, Project.MSG_VERBOSE);
        String old = getProject().getProperty(prop);
        if (old != null && !old.equals(val)) {
            getProject().log("Warning: " + prop + " was already set to " + old, Project.MSG_WARN);
        }
        getProject().setNewProperty(prop, val);
    }
    
    
    public @Override void execute() throws BuildException {
        try {
            if (getProjectFile() == null) {
                throw new BuildException("You must set 'project' or 'projectfile'", getLocation());
            }
            // XXX validate against nbm-project{,2}.xsd; does this require JDK 1.5?
            // Cf.: ant/project/eg/ValidateAllBySchema.java
            // XXX share parse w/ ModuleListParser
            Document pDoc = XMLUtil.parse(new InputSource(getProjectFile ().toURI().toString()),
                                          false, true, /*XXX*/null, null);
            if (publicPackagesProperty != null || javadocPackagesProperty != null) {
                PublicPackage[] pkgs = getPublicPackages(pDoc);
                if (publicPackagesProperty != null) {
                    String val;
                    if (pkgs.length > 0) {
                        String sep = "";
                        StringBuffer b = new StringBuffer();
                        for (PublicPackage p : pkgs) {
                            b.append(sep);
                            
                            String name = p.name;
                            if (name.indexOf (',') >= 0) {
                                throw new BuildException ("Package name cannot contain ',' as " + p, getLocation ());
                            }
                            if (name.indexOf ('*') >= 0) {
                                throw new BuildException ("Package name cannot contain '*' as " + p, getLocation ());
                            }
                            
                            b.append(name);
                            if (p.subpackages) {
                                b.append (".**");
                            } else {
                                b.append(".*");
                            }
                            sep = ", ";
                        }
                        val = b.toString();
                    } else {
                        val = "-";
                    }
                    define(publicPackagesProperty, val);
                }
                NO_JAVA_DOC_PROPERTY_SET: if (javadocPackagesProperty != null) {
                    if (pkgs.length > 0) {
                        String sep = "";
                        StringBuffer b = new StringBuffer();
                        for (PublicPackage p : pkgs) {
                            b.append(sep);
                            if (p.subpackages) {
                                if (getProject().getProperty(javadocPackagesProperty) == null) {
                                    String msg = javadocPackagesProperty + " cannot be set as <subpackages> does not work for Javadoc (see <subpackages>" + p.name + "</subpackages> tag in " + getProjectFile () + "). Set the property in project.properties if you want to build Javadoc.";
                                    // #52135: do not halt the build, just leave it.
                                    getProject().log("Warning: " + msg, Project.MSG_WARN);
                                }
                                break NO_JAVA_DOC_PROPERTY_SET;
                            }
                            b.append(p.name);
                            sep = ", ";
                        }
                        define(javadocPackagesProperty, b.toString());
                    }
                }
            }
            if (friendsProperty != null) {
                String[] friends = getFriends(pDoc);
                if (friends != null) {
                    StringBuffer b = new StringBuffer();
                    for (String f : friends) {
                        if (b.length() > 0) {
                            b.append(", ");
                        }
                        b.append(f);
                    }
                    define(friendsProperty, b.toString());
                }
            }
            ModuleListParser modules = null;
            Dep[] deps = null;
            if (moduleDependenciesProperty != null || 
                    moduleClassPathProperty != null || 
                    moduleRunClassPathProperty != null ||
                    testTypes.size() > 0) {
                @SuppressWarnings("unchecked")
                Hashtable<String,String> properties = getProject().getProperties();
                properties.put("project", moduleProject.getAbsolutePath());
                modules = new ModuleListParser(properties, getModuleType(pDoc), getProject());
                ModuleListParser.Entry myself = modules.findByCodeNameBase(getCodeNameBase(pDoc));
                if (myself == null) { // #71130
                    ModuleListParser.resetCaches();
                    modules = new ModuleListParser(properties, getModuleType(pDoc), getProject());
                    String cnb = getCodeNameBase(pDoc);
                    myself = modules.findByCodeNameBase(cnb);
                    assert myself != null : "Cannot find myself as " + cnb;
                }
                deps = getDeps(pDoc, modules);
            }
            if (moduleDependenciesProperty != null) {
                if (moduleDependenciesProperty != null) {
                    StringBuffer b = new StringBuffer();
                    for (Dep d : deps) {
                        if (!d.run) {
                            continue;
                        }
                        if (b.length() > 0) {
                            b.append(", ");
                        }
                        b.append(d);
                    }
                    if (b.length() > 0) {
                        define(moduleDependenciesProperty, b.toString());
                    }
                }
            }
            if (codeNameBaseDashesProperty != null) {
                String cnb = getCodeNameBase(pDoc);
                define(codeNameBaseDashesProperty, cnb.replace('.', '-'));
            }
            if (codeNameBaseSlashesProperty != null) {
                String cnb = getCodeNameBase(pDoc);
                define(codeNameBaseSlashesProperty, cnb.replace('.', '/'));
            }
            if (moduleClassPathProperty != null) {
                String cp = computeClasspath(pDoc, modules, deps, false);
                define(moduleClassPathProperty, cp);
            }
            if (moduleRunClassPathProperty != null) {
                String cp = computeClasspath(pDoc, modules, deps, true);
                define(moduleRunClassPathProperty, cp);
            }
            if (domainProperty != null) {
                if (getModuleType(pDoc) != TYPE_NB_ORG) {
                    throw new BuildException("Cannot set " + domainProperty + " for a non-netbeans.org module", getLocation());
                }
                File nball = new File(getProject().getProperty("nb_all"));
                File basedir = getProject().getBaseDir();
                Pattern p = Pattern.compile("([^/]+)(/([^/]+))*//([^/]+)/" + Pattern.quote(basedir.getName()));
                Reader r = new FileReader(new File(nball, "nbbuild/translations"));
                try {
                    BufferedReader br = new BufferedReader(r);
                    String line;
                    while ((line = br.readLine()) != null) {
                        Matcher m = p.matcher(line);
                        if (m.matches()) {
                            define(domainProperty, m.group(1));
                            break;
                        }
                    }
                } finally {
                    r.close();
                }
            }
            if (classPathExtensionsProperty != null) {
                String val = computeClassPathExtensions(pDoc);
                if (val != null) {
                    define(classPathExtensionsProperty, val);
                }
            }
            
            // Test dependecies
            //
            if (modules != null) {
               String testDistLocation = getProject().getProperty(TestDeps.TEST_DIST_VAR);
               if (testDistLocation == null) {
                   testDistLocation = "${" + TestDeps.TEST_DIST_VAR + "}";
               }
               ParseProjectXml.cachedTestDistLocation = testDistLocation;
    
               for (TestDeps td : getTestDeps(pDoc, modules, getCodeNameBase(pDoc))) {
                   // unit tests
                   TestType testType = getTestType(td.testtype);
                   if (testType!= null ) {
                       if (testType.getFolder() != null) {
                           define(testType.getFolder(),td.getTestFolder());
                       }
                       if (testType.getCompileCP() != null) {
                           String cp = td.getCompileClassPath();
                           if (cp != null && cp.trim().length() > 0) {
                               define(testType.getCompileCP(), cp);
                           }
                       }
                       if (testType.getRuntimeCP() != null) {
                           String cp = td.getRuntimeClassPath();
                           if (cp != null && cp.trim().length() > 0) {
                               define(testType.getRuntimeCP(), cp);
                           }
                       }
                       String testCompileDep = td.getTestCompileDep();
                       if (testType.getCompileDep() != null && testCompileDep != null) {
                           define(testType.getCompileDep(), testCompileDep);
                       }
                   }
               }
            }
        } catch (BuildException e) {
            throw e;
        } catch (Exception e) {
            throw new BuildException(e, getLocation());
        }
    }

    private Element getConfig(Document pDoc) throws BuildException {
        Element e = pDoc.getDocumentElement();
        Element c = XMLUtil.findElement(e, "configuration", PROJECT_NS);
        if (c == null) {
            throw new BuildException("No <configuration>", getLocation());
        }
        Element d = findNBMElement(c, "data");
        if (d == null) {
            throw new BuildException("No <data> in " + getProjectFile(), getLocation());
        }
        return d;
    }
    
    private static final class PublicPackage extends Object {
        public final String name;
        public boolean subpackages;
        
        public PublicPackage (String name, boolean subpackages) {
            this.name = name;
            this.subpackages = subpackages;
        }
    }

    private PublicPackage[] getPublicPackages(Document d) throws BuildException {
        Element cfg = getConfig(d);
        Element pp = findNBMElement(cfg, "public-packages");
        if (pp == null) {
            pp = findNBMElement(cfg, "friend-packages");
        }
        if (pp == null) {
            throw new BuildException("No <public-packages>", getLocation());
        }
        List<PublicPackage> pkgs = new ArrayList<PublicPackage>();
        for (Element p : XMLUtil.findSubElements(pp)) {
            boolean sub = false;
            if ("friend".equals(p.getNodeName())) {
                continue;
            }
            if (!"package".equals (p.getNodeName ())) {
                if (!("subpackages".equals (p.getNodeName ()))) {
                    throw new BuildException ("Strange element name, should be package or subpackages: " + p.getNodeName (), getLocation ());
                }
                sub = true;
            }
            
            String t = XMLUtil.findText(p);
            if (t == null) {
                throw new BuildException("No text in <package>", getLocation());
            }
            pkgs.add(new PublicPackage(t, sub));
        }
        return pkgs.toArray(new PublicPackage[pkgs.size()]);
    }
    
    private String[] getFriends(Document d) throws BuildException {
        Element cfg = getConfig(d);
        Element pp = findNBMElement(cfg, "friend-packages");
        if (pp == null) {
            return null;
        }
        List<String> friends = new ArrayList<String>();
        boolean other = false;
        for (Element p : XMLUtil.findSubElements(pp)) {
            if ("friend".equals(p.getNodeName())) {
                String t = XMLUtil.findText(p);
                if (t == null) {
                    throw new BuildException("No text in <friend>", getLocation());
                }
                friends.add(t);
            } else {
                other = true;
            }
        }
        if (friends.isEmpty()) {
            throw new BuildException("Must have at least one <friend> in <friend-packages>", getLocation());
        }
        if (!other) {
            throw new BuildException("Must have at least one <package> in <friend-packages>", getLocation());
        }
        return friends.toArray(new String[friends.size()]);
    }

    private final class Dep {
        private final ModuleListParser modules;
        /** will be e.g. org.netbeans.modules.form */
        public String codenamebase;
        public String release = null;
        public String spec = null;
        public boolean impl = false;
        public boolean compile = false;
        public boolean run = false;
        
        public Dep(ModuleListParser modules) {
            this.modules = modules;
        }
        
        public @Override String toString() throws BuildException {
            StringBuffer b = new StringBuffer(codenamebase);
            if (release != null) {
                b.append('/');
                b.append(release);
            }
            if (spec != null) {
                b.append(" > ");
                b.append(spec);
                assert !impl;
            }
            if (impl) {
                b.append(" = "); // NO18N
                String implVers = implementationVersionOf(modules, codenamebase);
                if (implVers == null) {
                    throw new BuildException("No OpenIDE-Module-Implementation-Version found in " + codenamebase);
                }
                if (implVers.equals(getProject().getProperty("buildnumber"))) {
                    throw new BuildException("Cannot depend on module " + codenamebase + " using build number as an implementation version");
                }
                b.append(implVers);
            }
            return b.toString();
        }
        
        private String implementationVersionOf(ModuleListParser modules, String cnb) throws BuildException {
            File jar = computeClasspathModuleLocation(modules, cnb, null, null, null);
            if (!jar.isFile()) {
                throw new BuildException("No such classpath entry: " + jar, getLocation());
            }
            try {
                JarFile jarFile = new JarFile(jar, false);
                try {
                    return jarFile.getManifest().getMainAttributes().getValue("OpenIDE-Module-Implementation-Version");
                } finally {
                    jarFile.close();
                }
            } catch (IOException e) {
                throw new BuildException(e, getLocation());
            }
        }

        private boolean matches(Attributes attr) {
            String givenCodeName = attr.getValue("OpenIDE-Module");
            int slash = givenCodeName.indexOf('/');
            int givenRelease = -1;
            if (slash != -1) {
                assert codenamebase.equals(givenCodeName.substring(0, slash));
                givenRelease = Integer.parseInt(givenCodeName.substring(slash + 1));
            }
            if (release != null) {
                int dash = release.indexOf('-');
                if (dash == -1) {
                    if (Integer.parseInt(release) != givenRelease) {
                        return false;
                    }
                } else {
                    int lower = Integer.parseInt(release.substring(0, dash));
                    int upper = Integer.parseInt(release.substring(dash + 1));
                    if (givenRelease < lower || givenRelease > upper) {
                        return false;
                    }
                }
            } else if (run && givenRelease != -1) {
                return false;
            }
            if (spec != null) {
                String givenSpec = attr.getValue("OpenIDE-Module-Specification-Version");
                if (givenSpec == null) {
                    return false;
                }
                // XXX cannot use org.openide.modules.SpecificationVersion from here
                int[] specVals = digitize(spec);
                int[] givenSpecVals = digitize(givenSpec);
                int len1 = specVals.length;
                int len2 = givenSpecVals.length;
                int max = Math.max(len1, len2);
                for (int i = 0; i < max; i++) {
                    int d1 = ((i < len1) ? specVals[i] : 0);
                    int d2 = ((i < len2) ? givenSpecVals[i] : 0);
                    if (d1 < d2) {
                        break;
                    } else if (d1 > d2) {
                        return false;
                    }
                }
            }
            if (impl) {
                if (attr.getValue("OpenIDE-Module-Implementation-Version") == null) {
                    return false;
                }
            }
            return true;
        }
        private int[] digitize(String spec) throws NumberFormatException {
            StringTokenizer tok = new StringTokenizer(spec, ".");
            int len = tok.countTokens();
            int[] digits = new int[len];
            for (int i = 0; i < len; i++) {
                digits[i] = Integer.parseInt(tok.nextToken());
            }
            return digits;
        }
        
    }

    private Dep[] getDeps(Document pDoc, ModuleListParser modules) throws BuildException {
        Element cfg = getConfig(pDoc);
        Element md = findNBMElement(cfg, "module-dependencies");
        if (md == null) {
            throw new BuildException("No <module-dependencies>", getLocation());
        }
        List<Dep> deps = new ArrayList<Dep>();
        for (Element dep : XMLUtil.findSubElements(md)) {
            Dep d = new Dep(modules);
            Element cnb = findNBMElement(dep, "code-name-base");
            if (cnb == null) {
                throw new BuildException("No <code-name-base>", getLocation());
            }
            String t = XMLUtil.findText(cnb);
            if (t == null) {
                throw new BuildException("No text in <code-name-base>", getLocation());
            }
            d.codenamebase = t;
            Element rd = findNBMElement(dep, "run-dependency");
            if (rd != null) {
                d.run = true;
                Element rv = findNBMElement(rd, "release-version");
                if (rv != null) {
                    t = XMLUtil.findText(rv);
                    if (t == null) {
                        throw new BuildException("No text in <release-version>", getLocation());
                    }
                    d.release = t;
                }
                Element sv = findNBMElement(rd, "specification-version");
                if (sv != null) {
                    t = XMLUtil.findText(sv);
                    if (t == null) {
                        throw new BuildException("No text in <specification-version>", getLocation());
                    }
                    d.spec = t;
                }
                Element iv = findNBMElement(rd, "implementation-version");
                if (iv != null) {
                    d.impl = true;
                }
            }
            d.compile = findNBMElement(dep, "compile-dependency") != null;
            deps.add(d);
        }
        return deps.toArray(new Dep[deps.size()]);
    }

    private String getCodeNameBase(Document d) throws BuildException {
        Element data = getConfig(d);
        Element name = findNBMElement(data, "code-name-base");
        if (name == null) {
            throw new BuildException("No <code-name-base>", getLocation());
        }
        String t = XMLUtil.findText(name);
        if (t == null) {
            throw new BuildException("No text in <code-name-base>", getLocation());
        }
        return t;
    }

    private int getModuleType(Document d) throws BuildException {
        Element data = getConfig(d);
        if (findNBMElement(data, "suite-component") != null) {
            return TYPE_SUITE;
        } else if (findNBMElement(data, "standalone") != null) {
            return TYPE_STANDALONE;
        } else {
            return TYPE_NB_ORG;
        }
    }

    private String computeClasspath(Document pDoc, ModuleListParser modules, Dep[] deps, boolean runtime) throws BuildException, IOException, SAXException {
        String myCnb = getCodeNameBase(pDoc);
        StringBuffer cp = new StringBuffer();
        String includedClustersProp = getProject().getProperty("enabled.clusters");
        Set<String> includedClusters = includedClustersProp != null ?
            new HashSet<String>(Arrays.asList(includedClustersProp.split(" *, *"))) :
            null;
        // Compatibility:
        String excludedClustersProp = getProject().getProperty("disabled.clusters");
        Set<String> excludedClusters = excludedClustersProp != null ?
            new HashSet<String>(Arrays.asList(excludedClustersProp.split(" *, *"))) :
            null;
        String excludedModulesProp = getProject().getProperty("disabled.modules");
        Set<String> excludedModules = excludedModulesProp != null ?
            new HashSet<String>(Arrays.asList(excludedModulesProp.split(" *, *"))) :
            null;
        for (Dep dep : deps) { // XXX should operative transitively if runtime
            if (!dep.compile) { // XXX should be sensitive to runtime
                continue;
            }
            String cnb = dep.codenamebase;
            File depJar = computeClasspathModuleLocation(modules, cnb, includedClusters, excludedClusters, excludedModules);
            
            Attributes attr;
            if (!depJar.isFile()) {
                throw new BuildException("No such classpath entry: " + depJar, getLocation());
            }
            JarFile jarFile = new JarFile(depJar, false);
            try {
                attr = jarFile.getManifest().getMainAttributes();
            } finally {
                jarFile.close();
            }
            
            if (!dep.matches(attr)) { // #68631
                throw new BuildException("Cannot compile against a module: " + depJar + " because of dependency: " + dep, getLocation());
            }

            if (!runtime && Boolean.parseBoolean(attr.getValue("OpenIDE-Module-Deprecated"))) {
                log("The module " + cnb + " has been deprecated", Project.MSG_WARN);
            }

            List<File> additions = new ArrayList<File>();
            additions.add(depJar);
            if (runtime) {
                Set<String> skipCnb = new HashSet<String>();
                addRecursiveDeps(additions, modules, cnb, includedClusters, excludedClusters, excludedModules, skipCnb);
            }
            
            // #52354: look for <class-path-extension>s in dependent modules.
            ModuleListParser.Entry entry = modules.findByCodeNameBase(cnb);
            if (entry != null) {
                additions.addAll(Arrays.asList(entry.getClassPathExtensions()));
            }
            
            if (!dep.impl && /* #71807 */ dep.run) {
                String friends = attr.getValue("OpenIDE-Module-Friends");
                if (friends != null && !Arrays.asList(friends.split(" *, *")).contains(myCnb)) {
                    throw new BuildException("The module " + myCnb + " is not a friend of " + depJar, getLocation());
                }
                String pubpkgs = attr.getValue("OpenIDE-Module-Public-Packages");
                if ("-".equals(pubpkgs)) {
                    throw new BuildException("The module " + depJar + " has no public packages and so cannot be compiled against", getLocation());
                } else if (pubpkgs != null && !runtime && publicPackageJarDir != null) {
                    File splitJar = createPublicPackageJar(additions, pubpkgs, publicPackageJarDir, cnb);
                    additions.clear();
                    additions.add(splitJar);
                }
            }
            
            for (File f : additions) {
                if (cp.length() > 0) {
                    cp.append(':');
                }
                cp.append(f.getAbsolutePath());
            }
        }
        // Also look for <class-path-extension>s for myself and put them in my own classpath.
        ModuleListParser.Entry entry = modules.findByCodeNameBase(myCnb);
        if (entry == null) {
            throw new IllegalStateException("Cannot find myself as " + myCnb);
        }
        for (File f : entry.getClassPathExtensions()) {
            cp.append(':');
            cp.append(f.getAbsolutePath());
        }
        return cp.toString();
    }
    
    private void addRecursiveDeps(List<File> additions, ModuleListParser modules, String cnb, Set<String> includedClusters, 
        Set<String> excludedClusters, Set<String> excludedModules, Set<String> skipCnb
    ) {
        if (!skipCnb.add(cnb)) {
            return;
        }
        log("Processing for recursive deps: " + cnb, Project.MSG_VERBOSE); // NO18N
        for (String nextModule : modules.findByCodeNameBase(cnb).getRuntimeDependencies()) {
            log("  Added dep: " + nextModule, Project.MSG_VERBOSE); // NO18N
            File depJar = computeClasspathModuleLocation(modules, nextModule, includedClusters, excludedClusters, excludedModules);
            
            if (!depJar.isFile()) {
                log("No such classpath entry: " + depJar, Project.MSG_WARN);
            }

            if (!additions.contains(depJar)) {
                additions.add(depJar);
            }
            
            ModuleListParser.Entry entry = modules.findByCodeNameBase(cnb);
            if (entry != null) {
                for (File f : entry.getClassPathExtensions()) {
                    if (!additions.contains(f)) {
                        additions.add(f);
                    }
                }
            }
            
            addRecursiveDeps(additions, modules, nextModule, includedClusters, excludedClusters, excludedModules, skipCnb);
        }
    }
    
    private File computeClasspathModuleLocation(ModuleListParser modules, String cnb,
            Set<String> includedClusters, Set<String> excludedClusters, Set<String> excludedModules) throws BuildException {
        ModuleListParser.Entry module = modules.findByCodeNameBase(cnb);
        if (module == null) {
            throw new BuildException("No dependent module " + cnb, getLocation());
        }
        String cluster = module.getClusterName();
        if (cluster != null) { // #68716
            if ((includedClusters != null && !includedClusters.isEmpty() && ! ModuleSelector.clusterMatch(includedClusters, cluster)) ||
                    ((includedClusters == null || includedClusters.isEmpty()) && excludedClusters != null && excludedClusters.contains(cluster))) {
                throw new BuildException("The module " + cnb + " cannot be compiled against because it is part of the cluster " + cluster +
                                         " which has been excluded from the target platform in your suite configuration", getLocation());
            }
            if (excludedModules != null && excludedModules.contains(cnb)) { // again #68716
                throw new BuildException("Module " + cnb + " excluded from the target platform", getLocation());
            }
        }
        return module.getJar();
    }
 
  final class TestDeps {
      public static final String UNIT = "unit";
      public static final String QA_FUNCTIONAL = "qa-functional";
      // unit, qa-functional, performance
      final String testtype;
      // all dependecies for the testtype
      final  List<TestDep> dependencies = new ArrayList<TestDep>();
      // code name base of tested module
      final String cnb;
      final ModuleListParser modulesParser;
      boolean fullySpecified;
      
      private Set<String> missingEntries;
  
      public  static final String TEST_DIST_VAR = "test.dist.dir";
      public TestDeps(String testtype,String cnb,ModuleListParser modulesParser) {
          assert modulesParser != null;
          this.testtype = testtype;
          this.cnb = cnb;
          this.modulesParser = modulesParser;
      }

       @Override
       public String toString() {
           return cnb + "/" + testtype + ":" + dependencies;
       }
      
      public List<String> getFiles(boolean compile) {
          List<String> files = new ArrayList<String>();
          for (TestDep d : dependencies) {
              files.addAll(d.getFiles(compile));
          }
          return files;
      }
      public void addDependency(TestDep dep) {
          dependencies.add(dep);
          fullySpecified |= dep.cnb.equals("org.netbeans.libs.junit4");
      }
      public void addOptionalDependency(TestDep dep) {
          if (dep.modulesParser.findByCodeNameBase(dep.cnb) != null) {
              dependencies.add(dep);
          }
      }

        private String getTestFolder() {
            ModuleListParser.Entry entry = modulesParser.findByCodeNameBase(cnb);
            String sep = "/";
            
            String cluster = entry.getClusterName(); 
            if (cluster == null) {
                // no cluster name is specified for standalone or module in module suite
                cluster = "cluster";
            }
            return ParseProjectXml.cachedTestDistLocation + sep + testtype + sep + cluster + sep + cnb.replace('.','-');
        }

        String getCompileClassPath() {
            return getPath(getFiles(true)) + getMissingEntries();
        }
        private String getPath(List<String> files) {
            StringBuffer path = new StringBuffer();
            Set<String> filesSet = new HashSet<String>();
            for (String filePath : files) {
                if (!filesSet.contains(filePath)) {
                    if (path.length() > 0) {
                        path.append(File.pathSeparatorChar);
                    } 
                    filesSet.add(filePath);
                    path.append(filePath);
                }
            }
            return path.toString().replace(File.separatorChar,'/');    
        }

        String getRuntimeClassPath() {
            return getPath(getFiles(false)) + getMissingEntries();
        }
        
    /** construct test compilation compilation dependencies.
     * Use case: unit tests of masterfs depends on tests of fs
     * @return relative project folder paths separated by comma
     */
    public  String getTestCompileDep() {
        Set<String> cnbs = new HashSet<String>();
        StringBuilder builder = new StringBuilder();
        computeCompileDep(cnb,cnbs,builder);
        return (builder.length() > 0) ? builder.toString() : null;
    }
    
    private void computeCompileDep(String cnb,Set<String> cnbs,StringBuilder sb) {
        if (cnbs.contains(cnb)) {
            return;
        }
        ModuleListParser.Entry entry = modulesParser.findByCodeNameBase(cnb);
        if (!cnbs.isEmpty() && entry != null) {
            // check if is tests are already built
            for (String othertesttype : new String[] {"unit", "qa-functional"}) {
                // don't compile already compiled tests dependencies
                String p = testJarPath(entry, othertesttype);
                if (p != null && new File(p).exists()) {
                    return;
                }
            }
            if (sb.length() > 0) {
                sb.append(File.pathSeparator);
            }
            File srcPath = entry.getSourceLocation();
            if (srcPath != null) {
                sb.append(srcPath.getAbsolutePath());
            }
        }
        cnbs.add(cnb);
        if (entry != null) {
            for (String othertesttype : new String[] {"unit", "qa-functional"}) {
                String testDeps[] = entry.getTestDependencies().get(othertesttype);
                if (testDeps != null) {
                    for (String cnb2 : testDeps) {
                        computeCompileDep(cnb2,cnbs,sb);
                    }
                }
            }
        }
    }
    
   private void addMissingEntry(String cnb) {
        if (missingEntries == null) {
            missingEntries = new HashSet<String>();
        }
        missingEntries.add(cnb);
    }
    
   private String getMissingEntries() {
       if ( missingEntries != null) {
           StringBuilder builder = new StringBuilder();
           builder.append("\n-missing-Module-Entries-: ");
           for (String cnd : missingEntries) {
               builder.append(cnd);
               builder.append("\n");
           }
           return builder.toString();
       }
       return "";
    }
  }
   /** Test dependency for module and type
    */ 
   final class TestDep {
       final ModuleListParser modulesParser;
       // code name base
       final String cnb;
       // dependencies on tests of modules
       final boolean recursive;
       final boolean test;
       // runtime classpath
       final boolean compile;
       TestDeps testDeps;
       
       TestDep (String cnb,ModuleListParser modules, boolean recursive,boolean test, boolean compile,TestDeps testDeps) {   
           this.modulesParser = modules;
           this.cnb = cnb;
           this.recursive = recursive;
           this.test = test;
           this.testDeps = testDeps;
           this.compile = compile;
       }

       @Override
       public String toString() {
           return cnb + (recursive ? "/recursive" : "") + (test ? "/test" : "") + (compile ? "/compile" : "");
       }
       /* get modules dependecies
        */
       List<ModuleListParser.Entry> getModules() {
           List<ModuleListParser.Entry> entries = new ArrayList<ModuleListParser.Entry>();
           if (recursive ) {
               Map<String,ModuleListParser.Entry> entriesMap = new HashMap<String,ModuleListParser.Entry>();
               addRecursiveModules(cnb,entriesMap);
               entries.addAll(entriesMap.values());
           } else {
               ModuleListParser.Entry entry = modulesParser.findByCodeNameBase(cnb);
               if (entry == null) {
                   //throw new BuildException("Module "  + cnb + " doesn't exist.");
                   testDeps.addMissingEntry(cnb);
               } else {
                    entries.add(modulesParser.findByCodeNameBase(cnb));
               }
           }
           return entries;      
           
       } 
       
       private void addRecursiveModules(String cnb, Map<String,ModuleListParser.Entry> entriesMap) {
           if (!entriesMap.containsKey(cnb)) {
               ModuleListParser.Entry entry = modulesParser.findByCodeNameBase(cnb);
               if (entry == null) {
//                   throw new BuildException("Module "  + cnd + " doesn't exist.");
                   testDeps.addMissingEntry(cnb);
               } else {
                   entriesMap.put(cnb,entry);
                   String cnbs[] = entry.getRuntimeDependencies();
                   // cnbs can be null
                   if (cnbs != null) {
                       for (String c : cnbs) {
                           addRecursiveModules(c, entriesMap);
                       }
                   }
               }
           }
       }
       List<String> getFiles(boolean compile) {
           List<String> files = new ArrayList<String>();
           if (!compile ||  ( compile && this.compile)) {
               List<ModuleListParser.Entry> modules = getModules();
               for (ModuleListParser.Entry entry : getModules()) {
                   if (entry != null) {
                       files.add(entry.getJar().getAbsolutePath());
                   } else {
                       log("Entry doesn't exist.");
                   }
               }
               // get tests files
               if (test) {
                   // get test folder
                   String jarPath = getTestJarPath(false);
                   if (jarPath != null) {
                      files.add(jarPath);
                   }
                   jarPath = getTestJarPath(true);
                   if (jarPath != null) {
                      files.add(jarPath);
                   }
               }
           }
           return files;
       }
       /**
        * @param useUnit if true, try unit tests, even if this is of another type (so we can use unit test utils in any kind of tests)
        */
       public String getTestJarPath(boolean useUnit) {
           ModuleListParser.Entry entry = modulesParser.findByCodeNameBase(cnb);
           if (entry == null) {
               testDeps.addMissingEntry(cnb);
               return null;
           } else {
               String type = testDeps.testtype;
               if (useUnit) {
                   if (type.equals("unit")) {
                       return null;
                   } else {
                       type = "unit";
                   }
               }
               return testJarPath(entry, type);
           }
       }
   }

    private static String testJarPath(ModuleListParser.Entry entry, String testType) {
        String sep = File.separator;
        String cluster = entry.getClusterName();
        if (cluster == null) {
            cluster = "cluster";
        }
        return ParseProjectXml.cachedTestDistLocation + sep + testType + sep + cluster + sep + entry.getCnb().replace('.', '-') + sep + "tests.jar";
    }

    private String computeClassPathExtensions(Document pDoc) {
        Element data = getConfig(pDoc);
        StringBuffer list = null;
        for (Element ext : XMLUtil.findSubElements(data)) {
            if (!ext.getLocalName().equals("class-path-extension")) {
                continue;
            }
            Element runtimeRelativePath = findNBMElement(ext, "runtime-relative-path");
            if (runtimeRelativePath == null) {
                throw new BuildException("Have malformed <class-path-extension> in " + getProjectFile(), getLocation());
            }
            String reltext = XMLUtil.findText(runtimeRelativePath);
            if (list == null) {
                list = new StringBuffer();
            } else {
                list.append(' ');
            }
            list.append(reltext);
        }
        return list != null ? list.toString() : null;
    }

    /**
     * Create a compact JAR containing only classes in public packages.
     * Forces the compiler to honor public package restrictions.
     * @see "#59792"
     */
    private File createPublicPackageJar(List<File> jars, String pubpkgs, File dir, String cnb) throws IOException {
        if (!dir.isDirectory()) {
            throw new IOException("No such directory " + dir);
        }
        File ppjar = new File(dir, cnb.replace('.', '-') + ".jar");
        if (ppjar.exists()) {
            // Check if it is up to date first. Must be as new as any input JAR.
            boolean uptodate = true;
            long stamp = ppjar.lastModified();
            for (File jar : jars) {
                if (jar.lastModified() > stamp) {
                    uptodate = false;
                    break;
                }
            }
            if (uptodate) {
                log("Distilled " + ppjar + " was already up to date", Project.MSG_VERBOSE);
                return ppjar;
            }
        }
        log("Distilling " + ppjar + " from " + jars);
        String corePattern = pubpkgs.
                replaceAll(" +", "").
                replaceAll("\\.", "/").
                replaceAll(",", "|").
                replaceAll("\\*\\*", "(.+/)?").
                replaceAll("\\*", "");
        Pattern p = Pattern.compile("(" + corePattern + ")[^/]+\\.class");
        boolean foundAtLeastOneEntry = false;
        // E.g.: (org/netbeans/api/foo/|org/netbeans/spi/foo/)[^/]+\.class
        OutputStream os = new FileOutputStream(ppjar);
        try {
            ZipOutputStream zos = new ZipOutputStream(os);
            Set<String> addedPaths = new HashSet<String>();
            for (File jar : jars) {
                if (!jar.isFile()) {
                    log("Classpath entry " + jar + " does not exist; skipping", Project.MSG_WARN);
                }
                InputStream is = new FileInputStream(jar);
                try {
                    ZipInputStream zis = new ZipInputStream(is);
                    ZipEntry inEntry;
                    while ((inEntry = zis.getNextEntry()) != null) {
                        String path = inEntry.getName();
                        if (!addedPaths.add(path)) {
                            continue;
                        }
                        if (!p.matcher(path).matches()) {
                            continue;
                        }
                        foundAtLeastOneEntry = true;
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buf = new byte[4096];
                        int read;
                        while ((read = zis.read(buf)) != -1) {
                            baos.write(buf, 0, read);
                        }
                        byte[] data = baos.toByteArray();
                        ZipEntry outEntry = new ZipEntry(path);
                        outEntry.setSize(data.length);
                        CRC32 crc = new CRC32();
                        crc.update(data);
                        outEntry.setCrc(crc.getValue());
                        zos.putNextEntry(outEntry);
                        zos.write(data);
                    }
                } finally {
                    is.close();
                }
            }
            zos.close();
        } finally {
            os.close();
        }
        if (!foundAtLeastOneEntry) {
            ppjar.delete();
            throw new BuildException("The JARs " + jars + " contain no classes in the supposed public packages " +
                    pubpkgs + " and so cannot be compiled against", getLocation());
        }
        return ppjar;
    }

    private TestDeps[] getTestDeps(Document pDoc,ModuleListParser modules,String testCnb) {
        assert modules != null;
        Element cfg = getConfig(pDoc);
        List<TestDeps> testDepsList = new ArrayList<TestDeps>();
        Element pp = findNBMElement(cfg, "test-dependencies");
        boolean existsUnitTests = false;
        boolean existsQaFunctionalTests = false;
        if (pp != null) {
            for (Element depssEl : XMLUtil.findSubElements(pp)) {
                String testType = findTextOrNull(depssEl,"name");
                if (testType == null) {
                    testType = TestDeps.UNIT; // default variant
                    existsUnitTests = true;
                } else if (testType.equals(TestDeps.UNIT)) {
                    existsUnitTests = true;
                } else if (testType.equals(TestDeps.QA_FUNCTIONAL)) {
                    existsQaFunctionalTests = true;
                }
                TestDeps testDeps = new TestDeps(testType,testCnb,modules);
                testDepsList.add(testDeps);
                for (Element el : XMLUtil.findSubElements(depssEl)) {
                    if (el.getTagName().equals("test-dependency")) {
                        // parse test dep
                        boolean  test =   (findNBMElement(el,"test") != null);
                        String cnb =  findTextOrNull(el,"code-name-base");
                        boolean  recursive = (findNBMElement(el,"recursive") != null);
                        boolean  compile = (findNBMElement(el,"compile-dependency") != null);
                        testDeps.addDependency(new TestDep(cnb,
                                                         modules,
                                                         recursive,
                                                         test,
                                                         compile,
                                                         testDeps)); 
                    }

                }
            }
        }
        // #82204 intialize default testtypes when are not  in project.xml
        if (!existsUnitTests) {
            log("Default TestDeps for unit", Project.MSG_VERBOSE);
            testDepsList.add(new TestDeps(TestDeps.UNIT,testCnb,modules));
        }
        if (!existsQaFunctionalTests) {
            log("Default TestDeps for qa-functional", Project.MSG_VERBOSE);
            testDepsList.add(new TestDeps(TestDeps.QA_FUNCTIONAL,testCnb,modules));
        }
        for (TestDeps testDeps : testDepsList) {
            if (testDeps.fullySpecified) {
                continue;
            }
            if (new File(moduleProject, "test/" + testDeps.testtype + "/src").isDirectory()) {
                log("Warning: " + testCnb + " lacks a " + testDeps.testtype +
                        " test dependency on org.netbeans.libs.junit4; using default dependencies for compatibility", Project.MSG_WARN);
            }
            for (String library : new String[]{"org.netbeans.libs.junit4", "org.netbeans.modules.nbjunit", "org.netbeans.insane"}) {
                testDeps.addOptionalDependency(new TestDep(library, modules, false, false, true, testDeps));
            }
            if (testDeps.testtype.startsWith("qa-")) {
                // ProjectSupport moved from the old nbjunit.ide:
                testDeps.addOptionalDependency(new TestDep("org.netbeans.modules.java.j2seproject", modules, false, true, true, testDeps));
                // Need to include transitive deps of j2seproject in CP:
                testDeps.addOptionalDependency(new TestDep("org.netbeans.modules.java.j2seproject", modules, true, false, false, testDeps));
                // Common GUI testing tools:
                for (String library : new String[]{"org.netbeans.modules.jemmy", "org.netbeans.modules.jellytools"}) {
                    testDeps.addOptionalDependency(new TestDep(library, modules, false, false, true, testDeps));
                }
                // For NbModuleSuite, which needs to find the platform:
                testDeps.addOptionalDependency(new TestDep("org.openide.util", modules, false, false, false, testDeps));
            }
        }
        return testDepsList.toArray(new TestDeps[testDepsList.size()]);
    }
    static String findTextOrNull(Element parentElement,String elementName) {
        Element el = findNBMElement(parentElement,elementName);
        return (el == null) ? null :
                              XMLUtil.findText(el);
                
    }
    private static String NBM_NS_CACHE = NBM_NS3;
    static Element findNBMElement(Element el,String name) {
        Element retEl = XMLUtil.findElement(el,name,NBM_NS_CACHE) ;
        if (retEl == null) {
            NBM_NS_CACHE = (NBM_NS_CACHE.equals(NBM_NS3)) ? NBM_NS2 :NBM_NS3;
            retEl = XMLUtil.findElement(el,name,NBM_NS_CACHE) ;            
        }
        return retEl;
    }
 
}
