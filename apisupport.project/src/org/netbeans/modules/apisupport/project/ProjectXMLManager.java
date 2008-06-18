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
package org.netbeans.modules.apisupport.project;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider.NbModuleType;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectType;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.apisupport.project.universe.TestModuleDependency;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Convenience class for managing project's <em>project.xml</em> file. You
 * should explicitly enclose a <em>complete</em> operation within write access
 * to prevent race conditions. Use {@link ProjectManager#saveProject} to apply
 * changes <em>physically</em>.
 */
public final class ProjectXMLManager {

    /** Equal to AntProjectHelper.PROJECT_NS which is package private. */
    // XXX is there a better way? (impact of imposibility to use ProjectGenerator)
    private static final String PROJECT_NS =
            "http://www.netbeans.org/ns/project/1"; // NOI18N
    // elements constants
    private static final String BINARY_ORIGIN = "binary-origin"; // NOI18N
    private static final String BUILD_PREREQUISITE = "build-prerequisite"; // NOI18N
    private static final String CLASS_PATH_BINARY_ORIGIN = "binary-origin"; //NOI18N
    private static final String CLASS_PATH_EXTENSION = "class-path-extension"; // NOI18N
    private static final String CLASS_PATH_RUNTIME_PATH = "runtime-relative-path"; //NOI18N
    private static final String CODE_NAME_BASE = "code-name-base"; // NOI18N
    private static final String COMPILE_DEPENDENCY = "compile-dependency"; // NOI18N
    private static final String DATA = "data"; // NOI18N
    private static final String DEPENDENCY = "dependency"; // NOI18N
    private static final String EXTRA_COMPILATION_UNIT = "extra-compilation-unit"; // NOI18N
    private static final String FRIEND = "friend"; // NOI18N
    private static final String FRIEND_PACKAGES = "friend-packages"; // NOI18N
    private static final String IMPLEMENTATION_VERSION = "implementation-version"; // NOI18N
    private static final String MODULE_DEPENDENCIES = "module-dependencies"; // NOI18N
    private static final String PACKAGE = "package"; // NOI18N
    private static final String PUBLIC_PACKAGES = "public-packages"; // NOI18N
    private static final String RELEASE_VERSION = "release-version"; // NOI18N
    private static final String RUN_DEPENDENCY = "run-dependency"; // NOI18N
    private static final String SPECIFICATION_VERSION = "specification-version"; // NOI18N
    private static final String STANDALONE = "standalone"; // NOI18N
    private static final String SUBPACKAGES = "subpackages"; // NOI18N
    private static final String SUITE_COMPONENT = "suite-component"; // NOI18N
    private static final String TEST_DEPENDENCIES = "test-dependencies"; // NOI18N
    private static final String TEST_TYPE_NAME = "name"; // NOI18N
    private static final String TEST_DEPENDENCY = "test-dependency"; // NOI18N
    private static final String TEST_DEPENDENCY_CNB = "code-name-base"; // NOI18N
    private static final String TEST_DEPENDENCY_RECURSIVE = "recursive"; // NOI18N
    private static final String TEST_DEPENDENCY_COMPILE = "compile-dependency"; // NOI18N
    private static final String TEST_DEPENDENCY_TEST = "test"; // NOI18N
    private static final String TEST_TYPE = "test-type"; //NOI18N
    private final NbModuleProject project;
    private NbPlatform customPlaf;
    private String cnb;
    private SortedSet<ModuleDependency> directDeps;
    private ManifestManager.PackageExport[] publicPackages;
    private String[] cpExtensions;
    private String[] friends;    // cached confData element for easy access with getConfData
    private Element confData;

    /** Creates a new instance of {@link ProjectXMLManager}. */
    public ProjectXMLManager(final NbModuleProject project) {
        this.project = project;
    }

    /**
     * Utility mehtod for getting the {@link ProjectXMLManager instance}
     * associated with a project in the given directory.
     *
     * @throws IOException if the project under a given <code>projectDir</code>
     *         was recognized but could not be loaded (see {@link ProjectManager#findProject}).
     */
    public static ProjectXMLManager getInstance(final File projectDir) throws IOException {
        FileObject dir = FileUtil.toFileObject(projectDir);
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(dir);
        return new ProjectXMLManager(p);
    }

    public void setModuleType(NbModuleType moduleType) {
        Element confData = getConfData();
        Document doc = confData.getOwnerDocument();

        Element standaloneEl = findElement(confData, ProjectXMLManager.STANDALONE);
        if (standaloneEl != null && moduleType == NbModuleProvider.STANDALONE) {
            // nothing needs to be done - standalone is already set
            return;
        }

        Element suiteCompEl = findElement(confData, ProjectXMLManager.SUITE_COMPONENT);
        if (suiteCompEl != null && moduleType == NbModuleProvider.SUITE_COMPONENT) {
            // nothing needs to be done - suiteCompEl is already set
            return;
        }

        if (suiteCompEl == null && standaloneEl == null && moduleType == NbModuleProvider.NETBEANS_ORG) {
            // nothing needs to be done - nb.org modules don't have any element
            return;
        }

        // Ok, we get here. So clean up....
        if (suiteCompEl != null) {
            confData.removeChild(suiteCompEl);
        }
        if (standaloneEl != null) {
            confData.removeChild(standaloneEl);
        }

        // ....and create element for new module type.
        Element newModuleType = createTypeElement(doc, moduleType);
        if (newModuleType != null) {
            confData.insertBefore(newModuleType, findModuleDependencies(confData));
        }
        project.putPrimaryConfigurationData(confData);
    }

    /**
     * Returns direct module dependencies using default module's platform. See
     * {@link #getDirectDependencies(NbPlatform)} for more details to which
     * this method delegates.
     */
    public SortedSet<ModuleDependency> getDirectDependencies() throws IOException {
        return getDirectDependencies(null);
    }

    /**
     * Returns sorted direct module dependencies using {@link
     * ModuleDependency#CNB_COMPARATOR} allowing to pass a custom platform.
     * Since no two modules with the same code name base may be set as a
     * dependency. Also this is ordering used in the <em>project.xml</em>.
     */
    public SortedSet<ModuleDependency> getDirectDependencies(final NbPlatform customPlaf) throws IOException {
        if (this.customPlaf == customPlaf && this.directDeps != null) {
            return this.directDeps;
        }
        this.customPlaf = customPlaf;
        SortedSet<ModuleDependency> directDeps = new TreeSet<ModuleDependency>(ModuleDependency.CNB_COMPARATOR);
        Element moduleDependencies = findModuleDependencies(getConfData());
        assert moduleDependencies != null : "Cannot find <module-dependencies> for: " + project;
        File prjDirF = project.getProjectDirectoryFile();
        ModuleList ml;
        if (customPlaf != null) {
            ml = ModuleList.getModuleList(prjDirF, customPlaf.getDestDir());
        } else {
            ml = ModuleList.getModuleList(prjDirF);
        }
        for (Element depEl : Util.findSubElements(moduleDependencies)) {
            Element cnbEl = findElement(depEl, ProjectXMLManager.CODE_NAME_BASE);
            String cnb = Util.findText(cnbEl);
            ModuleDependency depToAdd = getModuleDependency(cnb, ml, depEl);
            if (depToAdd == null) {
                continue;
            }
            if (!directDeps.add(depToAdd)) {
                String errMessage = "Corrupted project metadata (project.xml). " + // NOI18N
                        "Duplicate dependency entry found: " + depToAdd; // NOI18N
                Util.err.log(ErrorManager.WARNING, errMessage);
                throw new IllegalStateException(errMessage);
            }
            if (findElement(depEl, ProjectXMLManager.RUN_DEPENDENCY) == null) {
                continue;
            }

        }
        this.directDeps = Collections.unmodifiableSortedSet(directDeps);
        return this.directDeps;
    }

    private ModuleDependency getModuleDependency(String cnb, ModuleList ml, Element depEl) {
        ModuleEntry me = ml.getEntry(cnb);
        if (me == null) {
            // XXX might be e.g. shown in nb.errorForreground and "disabled"
            Util.err.log(ErrorManager.WARNING,
                    "Detected dependency on module which cannot be found in " + // NOI18N
                    "the current module's universe (platform, suite): " + cnb); // NOI18N
            return null;
        }

        Element runDepEl = findElement(depEl, ProjectXMLManager.RUN_DEPENDENCY);
        if (runDepEl == null) {
            return new ModuleDependency(me);
        }

        Element relVerEl = findElement(runDepEl, ProjectXMLManager.RELEASE_VERSION);
        String relVer = null;
        if (relVerEl != null) {
            relVer = Util.findText(relVerEl);
        }

        Element specVerEl = findElement(runDepEl, ProjectXMLManager.SPECIFICATION_VERSION);
        String specVer = null;
        if (specVerEl != null) {
            specVer = Util.findText(specVerEl);
        }

        Element compDepEl = findElement(depEl, ProjectXMLManager.COMPILE_DEPENDENCY);
        Element impleVerEl = findElement(runDepEl, ProjectXMLManager.IMPLEMENTATION_VERSION);

        ModuleDependency newDep = new ModuleDependency(
                me, relVer, specVer, compDepEl != null, impleVerEl != null);
        return newDep;
    }

    public ModuleDependency getModuleDependency(String cnb) throws IOException {
        return getModuleDependency(cnb, null);
    }

    public ModuleDependency getModuleDependency(String cnb, final NbPlatform customPlaf) throws IOException {
        this.customPlaf = customPlaf;
        Element moduleDependencies = findModuleDependencies(getConfData());
        assert moduleDependencies != null : "Cannot find <module-dependencies> for: " + project;
        File prjDirF = project.getProjectDirectoryFile();
        ModuleList ml;
        if (customPlaf != null) {
            ml = ModuleList.getModuleList(prjDirF, customPlaf.getDestDir());
        } else {
            ml = ModuleList.getModuleList(prjDirF);
        }
        for (Element dep : Util.findSubElements(moduleDependencies)) {
            Element cnbEl = findElement(dep, ProjectXMLManager.CODE_NAME_BASE);
            String depCnb = Util.findText(cnbEl);
            if (depCnb.equals(cnb)) {
                return getModuleDependency(cnb, ml, dep);
            }
        }
        return null;
    }

    /** Remove given dependency from the configuration data. */
    public void removeDependency(String cnbToRemove) {
        Element confData = getConfData();
        Element moduleDependencies = findModuleDependencies(confData);
        List<Element> currentDeps = Util.findSubElements(moduleDependencies);
        for (Iterator it = currentDeps.iterator(); it.hasNext();) {
            Element dep = (Element) it.next();
            Element cnbEl = findElement(dep, ProjectXMLManager.CODE_NAME_BASE);
            String cnb = Util.findText(cnbEl);
            if (cnbToRemove.equals(cnb)) {
                moduleDependencies.removeChild(dep);
            }
        }
        project.putPrimaryConfigurationData(confData);
    }

    /**
     * Use this for removing more than one dependencies. It's faster then
     * iterating and using <code>removeDependency</code> for every entry.
     */
    public void removeDependencies(Collection<ModuleDependency> depsToDelete) {
        Set<String> cnbsToDelete = new HashSet<String>(depsToDelete.size());
        for (ModuleDependency dep : depsToDelete) {
            cnbsToDelete.add(dep.getModuleEntry().getCodeNameBase());
        }
        removeDependenciesByCNB(cnbsToDelete);
    }

    /**
     * Use this for removing more than one dependencies. It's faster then
     * iterating and using <code>removeDependency</code> for every entry.
     */
    public void removeDependenciesByCNB(Collection<String> cnbsToDelete) {
        Element confData = getConfData();
        Element moduleDependencies = findModuleDependencies(confData);
        for (Element dep : Util.findSubElements(moduleDependencies)) {
            Element cnbEl = findElement(dep, ProjectXMLManager.CODE_NAME_BASE);
            String cnb = Util.findText(cnbEl);
            if (cnbsToDelete.remove(cnb)) {
                moduleDependencies.removeChild(dep);
            }
            if (cnbsToDelete.size() == 0) {
                break; // everything was deleted
            }
        }
        if (cnbsToDelete.size() != 0) {
            Util.err.log(ErrorManager.WARNING,
                    "Some modules weren't deleted: " + cnbsToDelete); // NOI18N
        }
        project.putPrimaryConfigurationData(confData);
    }

    public void editDependency(ModuleDependency origDep, ModuleDependency newDep) {
        Element confData = getConfData();
        Element moduleDependencies = findModuleDependencies(confData);
        List<Element> currentDeps = Util.findSubElements(moduleDependencies);
        for (Iterator<Element> it = currentDeps.iterator(); it.hasNext();) {
            Element dep = it.next();
            Element cnbEl = findElement(dep, ProjectXMLManager.CODE_NAME_BASE);
            String cnb = Util.findText(cnbEl);
            if (cnb.equals(origDep.getModuleEntry().getCodeNameBase())) {
                moduleDependencies.removeChild(dep);
                Element nextDep = it.hasNext() ? it.next() : null;
                createModuleDependencyElement(moduleDependencies, newDep, nextDep);
                break;
            }
        }
        project.putPrimaryConfigurationData(confData);
    }

    /**
     * Adds given dependency.
     */
    public void addDependency(ModuleDependency md) throws IOException {
        addDependencies(Collections.singleton(md));
    }

    /**
     * Adds given modules as module-dependencies for the project.
     */
    public void addDependencies(final Set<ModuleDependency> toAdd) throws IOException {
        SortedSet<ModuleDependency> deps = new TreeSet<ModuleDependency>(getDirectDependencies());
        if (deps.addAll(toAdd)) {
            replaceDependencies(deps);
        }
    }

    /**
     * Replaces all original dependencies with the given <code>newDeps</code>.
     */
    public void replaceDependencies(final Set<ModuleDependency> newDeps) {
        Element confData = getConfData();
        Document doc = confData.getOwnerDocument();
        Element moduleDependencies = findModuleDependencies(confData);
        confData.removeChild(moduleDependencies);
        moduleDependencies = createModuleElement(doc, ProjectXMLManager.MODULE_DEPENDENCIES);
        Element before = findTestDependenciesElement(confData);
        if (before == null) {
            before = findPublicPackagesElement(confData);
        }
        if (before == null) {
            before = findFriendsElement(confData);
        }
        assert before != null : "There must be " + PUBLIC_PACKAGES + " or " // NOI18N
                + FRIEND_PACKAGES + " element according to XSD"; // NOI18N
        confData.insertBefore(moduleDependencies, before);
        SortedSet<ModuleDependency> sortedDeps = new TreeSet<ModuleDependency>(newDeps);
        for (Iterator it = sortedDeps.iterator(); it.hasNext();) {
            ModuleDependency md = (ModuleDependency) it.next();
            createModuleDependencyElement(moduleDependencies, md, null);
        }
        project.putPrimaryConfigurationData(confData);
        this.directDeps = sortedDeps;
    }

    public void removeClassPathExtensions() {
        Element confData = getConfData();
        NodeList nl = confData.getElementsByTagNameNS(NbModuleProjectType.NAMESPACE_SHARED,
                ProjectXMLManager.CLASS_PATH_EXTENSION);
        for (int i = 0; i < nl.getLength(); i++) {
            confData.removeChild(nl.item(i));
        }
        project.putPrimaryConfigurationData(confData);
    }

    /**
     * Removes test dependency under type <code>testType</code>, indentified
     * by <code>cnbToRemove</code>. Does not remove whole 610test type even if
     * removed test dependency was the last one.
     */
    public boolean removeTestDependency(String testType, String cnbToRemove) {
        boolean wasRemoved = false;
        Element confData = getConfData();
        Element testModuleDependenciesEl = findTestDependenciesElement(confData);
        Element testTypeRemoveEl = null;
        for (Element type : Util.findSubElements(testModuleDependenciesEl)) {
            Element nameEl = findElement(type, TEST_TYPE_NAME);
            String nameOfType = Util.findText(nameEl);
            if (testType.equals(nameOfType)) {
                testTypeRemoveEl = type;
            }
        }
        //found such a test type
        if (testTypeRemoveEl != null) {
            for (Element el : Util.findSubElements(testTypeRemoveEl)) {
                Element cnbEl = findElement(el, TEST_DEPENDENCY_CNB);
                if (cnbEl == null) {
                    continue;   //name node, continue
                }
                String cnb = Util.findText(cnbEl);
                if (cnbToRemove.equals(cnb)) {
                    // found test dependency with desired CNB
                    testTypeRemoveEl.removeChild(el);
                    wasRemoved = true;
                    project.putPrimaryConfigurationData(confData);
                }
            }
        }
        return wasRemoved;
    }

    /**
     * Adds new test dependency to <code>project.xml</code>. Currently only two test types are
     * supported - <code>UNIT</code> and <code>QA_FUNCTIONAL</code>. Test dependencies under 
     * test types are sorted by CNB.
     */
    public void addTestDependency(String testType, TestModuleDependency newTestDep) throws IOException {
        final String UNIT = TestModuleDependency.UNIT;
        final String QA_FUNCTIONAL = TestModuleDependency.QA_FUNCTIONAL;
        assert (UNIT.equals(testType) || QA_FUNCTIONAL.equals(testType)) : "Current impl.supports only " + QA_FUNCTIONAL +
                " or " + UNIT + " tests"; // NOI18N
        File projectDir = FileUtil.toFile(project.getProjectDirectory());
        ModuleList ml = ModuleList.getModuleList(projectDir);
        Map<String, Set<TestModuleDependency>> map = new HashMap<String, Set<TestModuleDependency>>(getTestDependencies(ml));
        Set<TestModuleDependency> testDependenciesSet = map.get(testType);
        if (testDependenciesSet == null) {
            testDependenciesSet = new TreeSet<TestModuleDependency>();
            map.put(testType, testDependenciesSet);
        } else {
            // XXX necessary to clone?
            testDependenciesSet = new TreeSet<TestModuleDependency>(testDependenciesSet);
        }
        if (!testDependenciesSet.add(newTestDep)) {
            return; //nothing new to add, dep is already there, finished
        }
        Element confData = getConfData();
        Document doc = confData.getOwnerDocument();
        Element testModuleDependenciesEl = findTestDependenciesElement(confData);
        if (testModuleDependenciesEl == null) {      // test dependencies element does not exist, create it
            Element before = findPublicPackagesElement(confData);
            if (before == null) {
                before = findFriendsElement(confData);
            }
            assert before != null : "There must be " + PUBLIC_PACKAGES + " or " // NOI18N
                    + FRIEND_PACKAGES + " element according to XSD"; // NOI18N
            testModuleDependenciesEl = createModuleElement(doc, TEST_DEPENDENCIES);
            confData.insertBefore(testModuleDependenciesEl, before);
        }
        Element testTypeEl = null;
        //iterate through test types to determine if testType exist
        for (Element tt : Util.findSubElements(testModuleDependenciesEl)) {
            Node nameNode = findElement(tt, "name"); // NOI18N
            assert nameNode != null : "should be some child with name";
            //Node nameNode = tt.getFirstChild();
            //nameNode.getNodeName()
            assert (TEST_TYPE_NAME.equals(nameNode.getLocalName())) : "name node should be first child, but was:" + nameNode.getLocalName() +
                    "or" + nameNode.getNodeName(); //NOI18N
//equals
            if (nameNode.getTextContent().equals(testType)) {
                testTypeEl = tt;
            }
        }
        //? new or existing test type?
        if (testTypeEl == null) {
            //this test type, does not exist, create it, and add new test dependency
            Element newTestTypeEl = createNewTestTypeElement(doc, testType);
            testModuleDependenciesEl.appendChild(newTestTypeEl);
            createTestModuleDependencyElement(newTestTypeEl, newTestDep);
            project.putPrimaryConfigurationData(confData);
            return;
        } else {
            //testtype exists, refresh it
            Node beforeWhat = testTypeEl.getNextSibling();
            testModuleDependenciesEl.removeChild(testTypeEl);
            Element refreshedTestTypeEl = createNewTestTypeElement(doc, testType);
            if (beforeWhat == null) {
                testModuleDependenciesEl.appendChild(refreshedTestTypeEl);
            } else {
                testModuleDependenciesEl.insertBefore(refreshedTestTypeEl, beforeWhat);
            }
            for (Iterator it = testDependenciesSet.iterator(); it.hasNext();) {
                TestModuleDependency tmd = (TestModuleDependency) it.next();
                createTestModuleDependencyElement(refreshedTestTypeEl, tmd);
                project.putPrimaryConfigurationData(confData);
            }
        }
    }

    private Element createNewTestTypeElement(Document doc, String testTypeName) {
        Element newTestTypeEl = createModuleElement(doc, TEST_TYPE);
        Element nameOfTestTypeEl = createModuleElement(doc, TEST_TYPE_NAME, testTypeName);
        newTestTypeEl.appendChild(nameOfTestTypeEl);
        return newTestTypeEl;
    }

    private void createTestModuleDependencyElement(Element testTypeElement, TestModuleDependency tmd) {
        Document doc = testTypeElement.getOwnerDocument();
        Element tde = createModuleElement(doc, TEST_DEPENDENCY);
        testTypeElement.appendChild(tde);
        tde.appendChild(createModuleElement(doc, TEST_DEPENDENCY_CNB, tmd.getModule().getCodeNameBase()));
        if (tmd.isRecursive()) {
            tde.appendChild(createModuleElement(doc, TEST_DEPENDENCY_RECURSIVE));
        }
        if (tmd.isCompile()) {
            tde.appendChild(createModuleElement(doc, TEST_DEPENDENCY_COMPILE));
        }
        if (tmd.isTest()) {
            tde.appendChild(createModuleElement(doc, TEST_DEPENDENCY_TEST));
        }
    }

    /**
     * Gives a map from test type (e.g. <em>unit</em> or <em>qa-functional</em>)
     * to the set of {@link TestModuleDependency dependencies} belonging to it.
     */
    public Map<String, Set<TestModuleDependency>> getTestDependencies(final ModuleList ml) {
        Element testDepsEl = findTestDependenciesElement(getConfData());

        Map<String, Set<TestModuleDependency>> testDeps = new HashMap<String, Set<TestModuleDependency>>();

        if (testDepsEl != null) {
            for (Element typeEl : Util.findSubElements(testDepsEl)) {
                Element testTypeEl = findElement(typeEl, TEST_TYPE_NAME);
                String testType = null;
                if (testTypeEl != null) {
                    testType = Util.findText(testTypeEl);
                }
                if (testType == null) {
                    testType = TestModuleDependency.UNIT; // default variant
                }
                Set<TestModuleDependency> directTestDeps = new TreeSet<TestModuleDependency>();
                for (Element depEl : Util.findSubElements(typeEl)) {
                    if (depEl.getTagName().equals(TEST_DEPENDENCY)) {
                        // parse test dep
                        Element cnbEl = findElement(depEl, TEST_DEPENDENCY_CNB);
                        boolean test = findElement(depEl, TEST_DEPENDENCY_TEST) != null;
                        String cnb = null;
                        if (cnbEl != null) {
                            cnb = Util.findText(cnbEl);
                        }
                        boolean recursive = findElement(depEl, TEST_DEPENDENCY_RECURSIVE) != null;
                        boolean compile = findElement(depEl, TEST_DEPENDENCY_COMPILE) != null;
                        if (cnb != null) {
                            ModuleEntry me = ml.getEntry(cnb);
                            if (me != null) {
                                TestModuleDependency tmd = new TestModuleDependency(me, test, recursive, compile);
                                if (!directTestDeps.add(tmd)) {
                                    // testdependency already exists
                                    String path = project.getPathWithinNetBeansOrg();
                                    if (path == null) {
                                        path = project.getProjectDirectoryFile().getAbsolutePath();
                                    }
                                    String msg = "Invalid project.xml (" + path + "); testdependency " // NOI18N
                                            + tmd.getModule().getCodeNameBase() + " is duplicated!"; // NOI18N
                                    Util.err.log(ErrorManager.WARNING, msg);
                                }
                            }
                        }
                    }
                }
                testDeps.put(testType, directTestDeps);
            }
        }
        return testDeps;
    }

    /**
     * Replace existing classpath extensions with new values.
     * @param newValues &lt;key=runtime-path(String), value=binary-path(String)&gt;
     */
    public void replaceClassPathExtensions(final Map newValues) {
        removeClassPathExtensions();
        if (newValues != null && newValues.size() > 0) {
            Element confData = getConfData();
            Document doc = confData.getOwnerDocument();
            Iterator it = newValues.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                Element cpel = createModuleElement(doc, ProjectXMLManager.CLASS_PATH_EXTENSION);
                Element runtime = createModuleElement(doc, ProjectXMLManager.CLASS_PATH_RUNTIME_PATH,
                        (String) entry.getKey());
                Element binary = createModuleElement(doc, ProjectXMLManager.CLASS_PATH_BINARY_ORIGIN,
                        (String) entry.getValue());
                cpel.appendChild(runtime);
                cpel.appendChild(binary);
                confData.appendChild(cpel);

            }
            project.putPrimaryConfigurationData(confData);
        }
    }

    /**
     * Replaces all original public packages with the given
     * <code>newPackages</code>. Also removes friend packages if there are any
     * since those two mutually exclusive.
     */
    public void replacePublicPackages(String[] newPackages) {
        removePublicAndFriends();
        Element confData = getConfData();
        Document doc = confData.getOwnerDocument();
        Element publicPackagesEl = createModuleElement(doc, ProjectXMLManager.PUBLIC_PACKAGES);

        insertPublicOrFriend(publicPackagesEl);

        for (int i = 0; i < newPackages.length; i++) {
            publicPackagesEl.appendChild(
                    createModuleElement(doc, ProjectXMLManager.PACKAGE, newPackages[i]));
        }
        project.putPrimaryConfigurationData(confData);
        publicPackages = null; // XXX cleaner would be to listen on changes in helper
    }

    /** Position public-packages or friend-packages according to XSD. */
    private void insertPublicOrFriend(Element packagesEl) {
        Element beforeEl = findElement(getConfData(), ProjectXMLManager.CLASS_PATH_EXTENSION);
        if (beforeEl == null) {
            beforeEl = findElement(getConfData(), ProjectXMLManager.EXTRA_COMPILATION_UNIT);
        }
        getConfData().insertBefore(packagesEl, beforeEl);
    }

    /**
     * Replaces all original friends with the given <code>friends</code> with
     * <code>packagesToExpose</code> as exposed packages to those friends. Also
     * removes public packages if there are any since those two are mutually
     * exclusive.
     */
    public void replaceFriends(String[] friends, String[] packagesToExpose) {
        removePublicAndFriends();
        Element confData = getConfData();
        Document doc = confData.getOwnerDocument();
        Element friendPackages = createModuleElement(doc, ProjectXMLManager.FRIEND_PACKAGES);
        insertPublicOrFriend(friendPackages);
        for (int i = 0; i < friends.length; i++) {
            friendPackages.appendChild(
                    createModuleElement(doc, ProjectXMLManager.FRIEND, friends[i]));
        }
        for (int i = 0; i < packagesToExpose.length; i++) {
            friendPackages.appendChild(
                    createModuleElement(doc, ProjectXMLManager.PACKAGE, packagesToExpose[i]));
        }
        project.putPrimaryConfigurationData(confData);
        publicPackages = null;
    }

    /**
     * Returns an array of {@link ManifestManager.PackageExport}s of all
     * exposed public packages. Method considers both <em>package</em> and
     * <em>subpackages</em> elements with the recursivity flag set
     * appropriately for returned entries.
     *
     * @return array of {@link ManifestManager.PackageExport}. May be empty but
     *         not <code>null</code>.
     */
    public ManifestManager.PackageExport[] getPublicPackages() {
        if (publicPackages == null) {
            publicPackages = ProjectXMLManager.findPublicPackages(getConfData());
        }
        return publicPackages;
    }

    /** Returns all friends or <code>null</code> if there are none. */
    public String[] getFriends() {
        if (friends == null) {
            friends = ProjectXMLManager.findFriends(getConfData());
        }
        return friends;
    }

    /**
     * Returns paths of all libraries bundled within a project this
     * <em>manager</em> manage. So the result should be an array of
     * <code>String</code>s each representing a relative path to the project's
     * external library (jar/zip).
     * @return an array of strings (may be empty)
     */
    public String[] getBinaryOrigins() {
        if (cpExtensions != null) {
            return cpExtensions;
        }
        Set<String> binaryOrigs = new TreeSet<String>();
        for (Element cpExtEl : Util.findSubElements(getConfData())) {
            if (CLASS_PATH_EXTENSION.equals(cpExtEl.getTagName())) {
                Element binOrigEl = findElement(cpExtEl, BINARY_ORIGIN);
                if (binOrigEl != null) {
                    binaryOrigs.add(Util.findText(binOrigEl));
                }
            }
        }
        return cpExtensions = binaryOrigs.toArray(new String[binaryOrigs.size()]);
    }

    /** Returns code-name-base. */
    public String getCodeNameBase() {
        if (cnb == null) {
            Element cnbEl = findElement(getConfData(), ProjectXMLManager.CODE_NAME_BASE);
            cnb = Util.findText(cnbEl);
        }
        return cnb;
    }

    /** Package-private for unit tests only. */
    static void createModuleDependencyElement(
            Element moduleDependencies, ModuleDependency md, Element nextSibling) {

        Document doc = moduleDependencies.getOwnerDocument();
        Element modDepEl = createModuleElement(doc, ProjectXMLManager.DEPENDENCY);
        moduleDependencies.insertBefore(modDepEl, nextSibling);

        modDepEl.appendChild(createModuleElement(doc, ProjectXMLManager.CODE_NAME_BASE,
                md.getModuleEntry().getCodeNameBase()));
        if (md.hasCompileDependency()) {
            modDepEl.appendChild(createModuleElement(doc, ProjectXMLManager.BUILD_PREREQUISITE));
            modDepEl.appendChild(createModuleElement(doc, ProjectXMLManager.COMPILE_DEPENDENCY));
        }

        Element runDepEl = createModuleElement(doc, ProjectXMLManager.RUN_DEPENDENCY);
        modDepEl.appendChild(runDepEl);

        String rv = md.getReleaseVersion();
        if (rv != null && !rv.trim().equals("")) {
            runDepEl.appendChild(createModuleElement(
                    doc, ProjectXMLManager.RELEASE_VERSION, rv));
        }
        if (md.hasImplementationDepedendency()) {
            runDepEl.appendChild(createModuleElement(
                    doc, ProjectXMLManager.IMPLEMENTATION_VERSION));
        } else {
            String sv = md.getSpecificationVersion();
            if (sv != null && !"".equals(sv)) { // NOI18N
                runDepEl.appendChild(createModuleElement(
                        doc, ProjectXMLManager.SPECIFICATION_VERSION, sv));
            }
        }
    }

    /** Removes public-packages and friend-packages elements. */
    private void removePublicAndFriends() {
        Element friendPackages = findFriendsElement(getConfData());
        if (friendPackages != null) {
            getConfData().removeChild(friendPackages);
        }
        Element publicPackages = findPublicPackagesElement(getConfData());
        if (publicPackages != null) {
            getConfData().removeChild(publicPackages);
        }
    }

    private static Element findElement(Element parentEl, String elementName) {
        return Util.findElement(parentEl, elementName, NbModuleProjectType.NAMESPACE_SHARED);
    }

    /** Package-private for unit tests only. */
    static Element findModuleDependencies(Element parentEl) {
        return findElement(parentEl, ProjectXMLManager.MODULE_DEPENDENCIES);
    }

    private static Element findTestDependenciesElement(Element parentEl) {
        return findElement(parentEl, ProjectXMLManager.TEST_DEPENDENCIES);
    }

    private static Element findPublicPackagesElement(Element parentEl) {
        return findElement(parentEl, ProjectXMLManager.PUBLIC_PACKAGES);
    }

    private static Element findFriendsElement(Element parentEl) {
        return findElement(parentEl, ProjectXMLManager.FRIEND_PACKAGES);
    }

    private static Element createModuleElement(Document doc, String name) {
        return doc.createElementNS(NbModuleProjectType.NAMESPACE_SHARED, name);
    }

    private static Element createModuleElement(Document doc, String name, String innerText) {
        Element el = createModuleElement(doc, name);
        el.appendChild(doc.createTextNode(innerText));
        return el;
    }

    private static Element createSuiteElement(Document doc, String name) {
        return doc.createElementNS(SuiteProjectType.NAMESPACE_SHARED, name);
    }

    private static Element createSuiteElement(Document doc, String name, String innerText) {
        Element el = createSuiteElement(doc, name);
        el.appendChild(doc.createTextNode(innerText));
        return el;
    }

    /**
     * Find packages in public-packages or friend-packages section. Method
     * considers both <em>package</em> and <em>subpackages</em> elements with
     * the recursivity flag set appropriately for returned entries.
     */
    private static Set<ManifestManager.PackageExport> findAllPackages(Element parent) {
        Set<ManifestManager.PackageExport> packages = new HashSet<ManifestManager.PackageExport>();
        for (Element pkgEl : Util.findSubElements(parent)) {
            if (PACKAGE.equals(pkgEl.getTagName())) {
                packages.add(new ManifestManager.PackageExport(Util.findText(pkgEl), false));
            } else if (SUBPACKAGES.equals(pkgEl.getTagName())) {
                packages.add(new ManifestManager.PackageExport(Util.findText(pkgEl), true));
            }
        }
        return packages;
    }

    /**
     * Utility method for finding public packages. Method considers both
     * <em>package</em> and <em>subpackages</em> elements with the recursivity
     * flag set appropriately for returned entries.
     *
     * @return array of {@link ManifestManager.PackageExport}. May be empty but
     *         not <code>null</code>.
     */
    public static ManifestManager.PackageExport[] findPublicPackages(final Element confData) {
        Element ppEl = findPublicPackagesElement(confData);
        Set<ManifestManager.PackageExport> pps = new HashSet<ManifestManager.PackageExport>();
        if (ppEl != null) {
            pps.addAll(findAllPackages(ppEl));
        }
        ppEl = findFriendsElement(confData);
        if (ppEl != null) {
            pps.addAll(findAllPackages(ppEl));
        }
        return pps.isEmpty() ? ManifestManager.EMPTY_EXPORTED_PACKAGES : pps.toArray(new ManifestManager.PackageExport[pps.size()]);
    }

    /** Utility method for finding friend. */
    public static String[] findFriends(final Element confData) {
        Element friendsEl = findFriendsElement(confData);
        if (friendsEl != null) {
            Set<String> friends = new TreeSet<String>();
            for (Element friendEl : Util.findSubElements(friendsEl)) {
                if (FRIEND.equals(friendEl.getTagName())) {
                    friends.add(Util.findText(friendEl));
                }
            }
            return friends.toArray(new String[friends.size()]);
        }
        return null;
    }

    /**
     * Generates a basic <em>project.xml</em> templates into the given
     * <code>projectXml</code> for <em>standalone</em> or <em>module in
     * suite</em> module.
     */
    static void generateEmptyModuleTemplate(FileObject projectXml, String cnb,
            NbModuleType moduleType) throws IOException {

        Document prjDoc = XMLUtil.createDocument("project", PROJECT_NS, null, null); // NOI18N

        // generate general project elements
        Element typeEl = prjDoc.createElementNS(PROJECT_NS, "type"); // NOI18N
        typeEl.appendChild(prjDoc.createTextNode(NbModuleProjectType.TYPE));
        prjDoc.getDocumentElement().appendChild(typeEl);
        Element confEl = prjDoc.createElementNS(PROJECT_NS, "configuration"); // NOI18N
        prjDoc.getDocumentElement().appendChild(confEl);

        // generate NB Module project type specific elements
        Element dataEl = createModuleElement(confEl.getOwnerDocument(), DATA);
        confEl.appendChild(dataEl);
        Document dataDoc = dataEl.getOwnerDocument();
        dataEl.appendChild(createModuleElement(dataDoc, CODE_NAME_BASE, cnb));
        Element moduleTypeEl = createTypeElement(dataDoc, moduleType);
        if (moduleTypeEl != null) {
            dataEl.appendChild(moduleTypeEl);
        }
        dataEl.appendChild(createModuleElement(dataDoc, MODULE_DEPENDENCIES));
        dataEl.appendChild(createModuleElement(dataDoc, PUBLIC_PACKAGES));

        // store document to disk
        ProjectXMLManager.safelyWrite(projectXml, prjDoc);
    }

    /**
     * Create a library wrapper project.xml.
     *
     * @param publicPackages set of <code>String</code>s representing the packages
     * @param extensions &lt;key=runtime path(String), value=binary path (String)&gt;
     */
    static void generateLibraryModuleTemplate(FileObject projectXml, String cnb,
            NbModuleType moduleType, Set publicPackages, Map extensions) throws IOException {

        Document prjDoc = XMLUtil.createDocument("project", PROJECT_NS, null, null); // NOI18N

        // generate general project elements
        Element typeEl = prjDoc.createElementNS(PROJECT_NS, "type"); // NOI18N
        typeEl.appendChild(prjDoc.createTextNode(NbModuleProjectType.TYPE));
        prjDoc.getDocumentElement().appendChild(typeEl);
        Element confEl = prjDoc.createElementNS(PROJECT_NS, "configuration"); // NOI18N
        prjDoc.getDocumentElement().appendChild(confEl);

        // generate NB Module project type specific elements
        Element dataEl = createModuleElement(confEl.getOwnerDocument(), DATA);
        confEl.appendChild(dataEl);
        Document dataDoc = dataEl.getOwnerDocument();
        dataEl.appendChild(createModuleElement(dataDoc, CODE_NAME_BASE, cnb));
        Element moduleTypeEl = createTypeElement(dataDoc, moduleType);
        if (moduleTypeEl != null) {
            dataEl.appendChild(moduleTypeEl);
        }
        dataEl.appendChild(createModuleElement(dataDoc, MODULE_DEPENDENCIES));
        Element packages = createModuleElement(dataDoc, PUBLIC_PACKAGES);
        dataEl.appendChild(packages);
        Iterator it = publicPackages.iterator();
        while (it.hasNext()) {
            packages.appendChild(createModuleElement(dataDoc, PACKAGE, (String) it.next()));
        }
        it = extensions.entrySet().iterator();
        while (it.hasNext()) {
            Element cp = createModuleElement(dataDoc, CLASS_PATH_EXTENSION);
            dataEl.appendChild(cp);
            Map.Entry entry = (Map.Entry) it.next();
            cp.appendChild(createModuleElement(dataDoc, CLASS_PATH_RUNTIME_PATH, (String) entry.getKey()));
            cp.appendChild(createModuleElement(dataDoc, CLASS_PATH_BINARY_ORIGIN, (String) entry.getValue()));
        }

        // store document to disk
        ProjectXMLManager.safelyWrite(projectXml, prjDoc);
    }

    private static Element createTypeElement(Document dataDoc, NbModuleType type) {
        Element result = null;
        if (type == NbModuleProvider.STANDALONE) {
            result = createModuleElement(dataDoc, STANDALONE);
        } else if (type == NbModuleProvider.SUITE_COMPONENT) {
            result = createModuleElement(dataDoc, SUITE_COMPONENT);
        }
        return result;
    }

    /**
     * Generates a basic <em>project.xml</em> templates into the given
     * <code>projectXml</code> for <em>Suite</em>.
     */
    public static void generateEmptySuiteTemplate(FileObject projectXml, String name) throws IOException {
        // XXX this method could be moved in a future (depends on how complex
        // suite's project.xml will be) to the .suite package dedicated class
        Document prjDoc = XMLUtil.createDocument("project", PROJECT_NS, null, null); // NOI18N

        // generate general project elements
        Element typeEl = prjDoc.createElementNS(PROJECT_NS, "type"); // NOI18N
        typeEl.appendChild(prjDoc.createTextNode(SuiteProjectType.TYPE));
        prjDoc.getDocumentElement().appendChild(typeEl);
        Element confEl = prjDoc.createElementNS(PROJECT_NS, "configuration"); // NOI18N
        prjDoc.getDocumentElement().appendChild(confEl);

        // generate NB Suite project type specific elements
        Element dataEl = createSuiteElement(confEl.getOwnerDocument(), DATA);
        confEl.appendChild(dataEl);
        Document dataDoc = dataEl.getOwnerDocument();
        dataEl.appendChild(createSuiteElement(dataDoc, "name", name)); // NOI18N

        // store document to disk
        ProjectXMLManager.safelyWrite(projectXml, prjDoc);
    }

    private static void safelyWrite(FileObject projectXml, Document prjDoc) throws IOException {
        FileLock lock = projectXml.lock();
        try {
            OutputStream os = projectXml.getOutputStream(lock);
            try {
                XMLUtil.write(prjDoc, os, "UTF-8"); // NOI18N
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }

    private Element getConfData() {
        if (confData == null) {
            confData = project.getPrimaryConfigurationData();
        }
        return confData;
    }
}
