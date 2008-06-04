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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Increments specification versions of all specified modules,
 * in the trunk or in a branch, in a regulated manner.
 * @author Jesse Glick
 */
public final class RefreshDependencyVersions extends Task {
    
    private File nbroot;
    private String codenameBase;
    private boolean dryRun = false;
    private final Set<Dep> injectDeps = new HashSet<Dep>();
    
    public RefreshDependencyVersions() {}
    
    public void setNbroot(File f) {
        nbroot = f;
    }
    
    public void setModule(String codenameBase) {
        this.codenameBase = codenameBase;
    }

    public void setDryRun(boolean b) {
        dryRun = b;
    }
    
    public Dep createInject() {
        Dep dep = new Dep();
        injectDeps.add(dep);
        return dep;
    }
    
    public @Override void execute() throws BuildException {
        log("RefreshDependencyVersions parameters: nbroot = '" + nbroot.getAbsolutePath() + "'"
                + ", module = " + codenameBase + ", dryrun = " + dryRun + ", inject = " + injectDeps, Project.MSG_VERBOSE);

        if (nbroot == null || codenameBase == null) {
            throw new BuildException("Missing params 'nbroot' or 'modules'", getLocation());
        }

        validateInjectedDependencies(injectDeps);
        
        ModuleListParser listParser;
        try {
            listParser = new ModuleListParser(getProject().getProperties(), ParseProjectXml.TYPE_NB_ORG, getProject());
        } catch (IOException ioe) {
            throw new BuildException("Can't read module list!", ioe, getLocation());
        }
        Set<ModuleListParser.Entry> allModules = listParser.findAll();
        
        Dep sourceModuleVersion = null;
        
        // find the versions of the source module
        for(ModuleListParser.Entry moduleEntry: allModules) {
            String path = moduleEntry.getNetbeansOrgPath();
            File projectDir = new File(nbroot, path.replace('/', File.separatorChar));
            if (!projectDir.isDirectory()) {
                log("No such directory " + projectDir + "; skipping", Project.MSG_WARN);
                continue;
            }
            
            Dep mv = loadDepFrom(projectDir);
            if (mv == null) {
                log("Not a module: " + projectDir + ", skipping!", Project.MSG_WARN);
                continue;
            }
            
            log("Loading module versions: " + mv.getCodenameBase()
                    + ", release = " + mv.getRelease()
                    + ", spec = " + mv.getSpecification(),
                Project.MSG_VERBOSE);
            
            if (codenameBase.equals(mv.getCodenameBase())) {
                sourceModuleVersion = mv;
                break;
            }
        }
        if (sourceModuleVersion == null) {
            throw new BuildException("Can't find '" + codenameBase + "' module!", getLocation());
        }
        
        // update all modules that depend on the source module, but not the source module itself
        // and not the modules we are injecting
        NBPRJ:
        for(ModuleListParser.Entry moduleEntry: allModules) {
            String path = moduleEntry.getNetbeansOrgPath();
            File projectFile = new File(new File(nbroot, path.replace('/', File.separatorChar)), "nbproject" + File.separatorChar + "project.xml");
            if (!projectFile.exists()) {
                log("Non-existent project.xml: " + path + ", skipping!", Project.MSG_WARN);
            }

            Document nbprj = null;
            try {
                InputStream is = new FileInputStream(projectFile);
                try {
                    nbprj = XMLUtil.parse(new InputSource(is), false, true, null, null);
                } finally {
                    is.close();
                }
            } catch (Exception ioe) {
                throw new BuildException("Can't parse " + projectFile, ioe, getLocation());
            }

            // check that nbprj is not the source module or one of the injects
            Node cnb = findChild(findChild(findChild(nbprj.getDocumentElement(), "configuration"), "data"), "code-name-base");
            if (cnb.getTextContent().equals(codenameBase)) {
                log("Won't touch dependencies of the source module (" + codenameBase + ")", Project.MSG_VERBOSE);
                continue;
            }
            for(Dep inject : injectDeps) {
                if (cnb.getTextContent().equals(inject.getCodenameBase())) {
                    log("Won't touch dependencies of the injected module (" + inject.getCodenameBase() + ")", Project.MSG_VERBOSE);
                    continue NBPRJ;
                }
            }
            
            boolean updated = false;
            StringBuilder refreshMsg = new StringBuilder();
            StringBuilder injectMsg = new StringBuilder();
            
            Node sourceDep = findDependencyFor(nbprj, codenameBase);
            if (sourceDep != null) {
                updated |= updateVersions(sourceDep, sourceModuleVersion, false, refreshMsg);
                
                for(Dep inject : injectDeps) {
                    if (injectMsg.length() > 0) {
                        injectMsg.append(", ");
                    }
                    Node injectedDep = findDependencyFor(nbprj, inject.getCodenameBase());
                    if (injectedDep != null) {
                        updated |= updateVersions(injectedDep, inject, true, injectMsg);
                    } else {
                        addDependency(sourceDep.getParentNode(), inject, injectMsg);
                        updated = true;
                    }
                }
            }
            
            if (updated) {
                try {
                    if (!dryRun) {
                        OutputStream os = new FileOutputStream(projectFile);
                        try {
                            XMLUtil.write(nbprj, os);
                        } finally {
                            os.close();
                        }
                    } else {
                        if (!projectFile.canWrite()) {
                            log("Would need to update " + projectFile + ", but it's readonly!", Project.MSG_ERR);
                        }
                    }
                } catch (IOException ioe) {
                    throw new BuildException("Can't write " + projectFile, ioe, getLocation());
                }
            }
            
            if (refreshMsg.length() > 0) {
                log("Dependencies refreshed in " + projectFile.getAbsolutePath() + ": " + refreshMsg.toString(), Project.MSG_INFO);
            }
            if (injectMsg.length() > 0) {
                log("Dependencies injected to " + projectFile.getAbsolutePath() + ": " + injectMsg.toString(), Project.MSG_INFO);
            }
        }
    }

    private static Node findChild(Node node, String childName) {
        NodeList kids = node.getChildNodes();
        for(int i = 0; i < kids.getLength(); i++) {
            if (kids.item(i).getNodeName().equals(childName)) {
                return kids.item(i);
            }
        }
        return null;
    }
    
    private static Node findDependencyFor(Document nbprj, String codenameBase) {
        NodeList allDeps = nbprj.getElementsByTagName("dependency");
        for(int i = 0; i < allDeps.getLength(); i++) {
            Node dep = allDeps.item(i);
            Node cnb = findChild(dep, "code-name-base");

            if (cnb.getTextContent().equals(codenameBase)) {
                return dep;
            }
        }
        return null;
    }

    private static boolean updateVersions(Node dep, Dep sourceDep, boolean injecting, StringBuilder log) {
        StringBuilder s = new StringBuilder();
        Node runDep = findChild(dep, "run-dependency");
        if (injecting) {
            if (runDep == null) {
                runDep = dep.getOwnerDocument().createElement("run-dependency");
                dep.appendChild(runDep);
                s.append(" adding <run-dependency/>");
            }
            
            Node buildPrerequisite = findChild(dep, "build-prerequisite");
            if (buildPrerequisite == null) {
                buildPrerequisite = dep.getOwnerDocument().createElement("build-prerequisite");
                dep.appendChild(buildPrerequisite);
                s.append(" adding <build-prerequisite/>");
            }

            Node compileDep = findChild(dep, "compile-dependency");
            if (compileDep == null) {
                compileDep = dep.getOwnerDocument().createElement("compile-dependency");
                dep.appendChild(compileDep);
                s.append(" adding <compile-dependency/>");
            }
        }
        
        if (runDep != null) {
            // update the release version
            Node releaseVersion = findChild(runDep, "release-version");
            if (releaseVersion != null && sourceDep.getRelease() != null) {
                String nue = checkReleaseVersion(sourceDep.getRelease());
                if (!releaseVersion.getTextContent().equals(nue)) {
                    releaseVersion.setTextContent(nue);
                    s.append(" release = ").append(nue);
                }
            }

            // update the specification version
            Node specVersion = findChild(runDep, "specification-version");
            if (!injecting) {
                assert sourceDep.getSpecification() != null : "Need specification version when refreshing dependencies"; //NOI18N
                if (specVersion == null) {
                    Node implVersion = findChild(runDep, "implementation-version");
                    if (implVersion == null) {
                        // umm, no spec version and not an impl. dependency
                        specVersion = dep.getOwnerDocument().createElement("specification-version");
                        specVersion.setTextContent("");
                        runDep.appendChild(specVersion);
                    }
                }                
                if (specVersion != null) {
                    String nue = checkSpecificationVersion(sourceDep.getSpecification());
                    if (!specVersion.getTextContent().equals(nue)) {
                        specVersion.setTextContent(nue);
                        s.append(" spec = ").append(nue);
                    }
                }
            } else {
                Node implVersion = findChild(runDep, "implementation-version");
                if (sourceDep.isImplementation()) {
                    if (specVersion != null) {
                        runDep.removeChild(specVersion);
                        s.append(" removing spec = " + specVersion.getTextContent());
                    }
                    if (implVersion == null) {
                        implVersion = dep.getOwnerDocument().createElement("implementation-version");
                        runDep.appendChild(implVersion);
                        s.append(" adding impl");
                    }
                } else {
                    if (sourceDep.getSpecification() != null) {
                        String nue = checkSpecificationVersion(sourceDep.getSpecification());
                        if (specVersion == null) {
                            specVersion = dep.getOwnerDocument().createElement("specification-version");
                            specVersion.setTextContent(nue);
                            runDep.appendChild(specVersion);
                            s.append(" adding spec = ").append(nue);
                        } else if (!specVersion.getTextContent().equals(nue)) {
                            specVersion.setTextContent(nue);
                            s.append(" updating spec = ").append(nue);
                        }
                    }
                    if (implVersion != null) {
                        runDep.removeChild(implVersion);
                        s.append(" removing impl");
                    }
                }
            }
        }
        
        log.append(sourceDep.getCodenameBase()).append(':');
        if (s.length() > 0) {
            log.append(s);
            return true;
        } else {
            log.append(" up-to-date");
            return false;
        }
    }

    private void addDependency(Node moduleDependencies, Dep inject, StringBuilder log) {
        Document nbprj = moduleDependencies.getOwnerDocument();
        Element nueDep = nbprj.createElement("dependency");
        moduleDependencies.appendChild(nueDep);

        Element nueCnb = nbprj.createElement("code-name-base");
        nueCnb.setTextContent(inject.getCodenameBase());
        nueDep.appendChild(nueCnb);
        log.append(inject.getCodenameBase());

        Element nueRunDep = nbprj.createElement("run-dependency");
        nueDep.appendChild(nueRunDep);

        Element nueRelease = nbprj.createElement("release-version");
        String nue = checkReleaseVersion(inject.getRelease());
        nueRelease.setTextContent(nue);
        nueRunDep.appendChild(nueRelease);
        log.append('/').append(nue);

        if (inject.isImplementation()) {
            nueRunDep.appendChild(nbprj.createElement("implementation-version"));
            log.append(" = impl");
        } else {
            Element nueSpec = nbprj.createElement("specification-version");
            nue = checkSpecificationVersion(inject.getSpecification());
            nueSpec.setTextContent(nue);
            nueRunDep.appendChild(nueSpec);
            log.append(" >= " + nue);
        }
        
        Element nueBuildPrerequisite = nbprj.createElement("build-prerequisite");
        nueDep.appendChild(nueBuildPrerequisite);

        Element nueCompileDep = nbprj.createElement("compile-dependency");
        nueDep.appendChild(nueCompileDep);
    }
    
    private static String[] gulp(File file, String enc) throws IOException {
        InputStream is = new FileInputStream(file);
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(is, enc));
            List<String> l = new ArrayList<String>();
            String line;
            while ((line = r.readLine()) != null) {
                l.add(line);
            }
            return l.toArray(new String[l.size()]);
        } finally {
            is.close();
        }
    }
    
//    private static void spit(File file, String enc, String[] lines) throws IOException {
//        OutputStream os = new FileOutputStream(file);
//        try {
//            PrintWriter w = new PrintWriter(new OutputStreamWriter(os, enc));
//            for (String line : lines) {
//                w.println(line);
//            }
//            w.flush();
//        } finally {
//            os.close();
//        }
//    }

    private void validateInjectedDependencies(Set<Dep> deps) throws BuildException {
        for(Dep dep : deps) {
            if (dep.getCodenameBase() == null || dep.getCodenameBase().length() == 0) {
                throw new BuildException("The 'codenamebase' attribute of injected dependency can't be empty.", getLocation());
            }
            if (dep.getRelease() == null || dep.getRelease().length() == 0) {
                throw new BuildException("The 'release' attribute of injected dependency can't be empty.", getLocation());
            }
            if (!dep.isImplementation()) {
                if (dep.getSpecification() == null || dep.getSpecification().length() == 0) {
                    throw new BuildException("The 'specification' attribute of injected dependency can't be empty. Or implementation='true' must be specified.", getLocation());
                }
            }
        }
    }

    private static String checkReleaseVersion(String v) {
        if (v.equals("0")) {
            return "0-1";
        } else {
            return v;
        }
    }
    
    private static String checkSpecificationVersion(String v) {
        if (v.endsWith(".0") && v.indexOf('.') != v.length() - 2) {
            // ends up with '.0' and it's not just 'x.0', but eg. x.x.0
            v = v.substring(0, v.length() - 2);
        }
        return v;
    }

    private Dep loadDepFrom(File projectDir) throws BuildException {
        Dep dep = new Dep();
        try {
            File pp = new File(projectDir, "nbproject" + File.separatorChar + "project.properties");
            if (pp.isFile()) {
                String[] lines = gulp(pp, "ISO-8859-1");
                for (int i = 0; i < lines.length; i++) {
                    Matcher m1 = Pattern.compile("(spec\\.version\\.base=)(.+)").matcher(lines[i]);
                    if (m1.matches()) {
                        dep.setSpecification(m1.group(2));
                    }
                }
            } else {
                if (!new File(projectDir, "nbproject" + File.separatorChar + "project.xml").isFile()) {
                    log("No such file " + pp + "; unprojectized module?", Project.MSG_VERBOSE);
                    return null;
                }
            }
            File mf = new File(projectDir, "manifest.mf");
            if (mf.isFile()) {
                String[] lines = gulp(mf, "UTF-8");
                for (int i = 0; i < lines.length; i++) {
                    Matcher m1 = Pattern.compile("(OpenIDE-Module: )(.+)").matcher(lines[i]);
                    if (m1.matches()) {
                        String fullName = m1.group(2);
                        int idx = fullName.lastIndexOf('/');
                        if (idx != -1) {
                            dep.setCodenameBase(fullName.substring(0, idx));
                            dep.setRelease(fullName.substring(idx + 1));
                        } else {
                            dep.setCodenameBase(fullName);
                            dep.setCodenameBase("0");
                        }
                    }
                    m1 = Pattern.compile("(OpenIDE-Module-Specification-Version: )(.+)").matcher(lines[i]);
                    if (m1.matches()) {
                        dep.setSpecification(m1.group(2));
                    }
                }
                return dep;
            } else {
                log("No such file " + mf + "; not a real module?", Project.MSG_VERBOSE);
                return null;
            }
        } catch (IOException e) {
            throw new BuildException("While processing project files in " + projectDir + ": " + e, e, getLocation());
        }
    }
    
//    private class ModuleVersion {
//        private String codebaseName;
//        private String release;
//        private String specification;
//        private String implementation;
//        
//        public ModuleVersion() {
//        }
//
//        public String getCodebaseName() {
//            return codebaseName;
//        }
//
//        public String getImplementation() {
//            return implementation;
//        }
//
//        public String getRelease() {
//            return release;
//        }
//
//        public String getSpecification() {
//            return specification;
//        }
//        
//    } // End of ModuleVersion class

    public static final class Dep {
        
        private String codenameBase;
        private String release;
        private String specification;
        private boolean implementation;

        public Dep() {
            
        }
        
        public String getCodenameBase() {
            return codenameBase;
        }

        public void setCodenameBase(String codenameBase) {
            this.codenameBase = codenameBase == null || codenameBase.length() == 0 ? null : codenameBase;
        }

        public boolean isImplementation() {
            return implementation;
        }

        public void setImplementation(boolean implementation) {
            if (implementation && specification != null) {
                throw new IllegalArgumentException("Can't use implementation dependency when specification version is set.");
            }
            this.implementation = implementation;
        }

        public String getRelease() {
            return release;
        }

        public void setRelease(String release) {
            this.release = release == null || release.length() == 0 ? null : release;
        }

        public String getSpecification() {
            return specification;
        }

        public void setSpecification(String specification) {
            if (implementation && specification != null && specification.length() > 0) {
                throw new IllegalArgumentException("Can't specification version is set when using implementation dependency.");
            }
            this.specification = specification == null || specification.length() == 0 ? null : specification;
        }

        public @Override boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Dep other = (Dep) obj;
            if (this.codenameBase != other.codenameBase && (this.codenameBase == null || !this.codenameBase.equals(other.codenameBase))) {
                return false;
            }
            if (this.release != other.release && (this.release == null || !this.release.equals(other.release))) {
                return false;
            }
            if (this.specification != other.specification && (this.specification == null || !this.specification.equals(other.specification))) {
                return false;
            }
            if (this.implementation != other.implementation) {
                return false;
            }
            return true;
        }

        public @Override int hashCode() {
            int hash = 7;
            hash = 79 * hash + (this.codenameBase != null ? this.codenameBase.hashCode() : 0);
            hash = 79 * hash + (this.release != null ? this.release.hashCode() : 0);
            hash = 79 * hash + (this.specification != null ? this.specification.hashCode() : 0);
            hash = 79 * hash + (this.implementation ? 1 : 0);
            return hash;
        }

        public @Override String toString() {
            if (specification != null) {
                return codenameBase + "/" + release + " >= " + specification;
            } else {
                return codenameBase + "/" + release + " = impl";
            }
        }

    } // End of Dep class
}
