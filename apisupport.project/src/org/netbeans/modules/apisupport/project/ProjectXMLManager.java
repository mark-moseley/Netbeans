/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Convenient class for managing project's <em>project.xml</em> file.
 *
 * @author mkrauskopf
 */
public final class ProjectXMLManager {
    
    /** Equal to AntProjectHelper.PROJECT_NS which is package private. */
    // XXX is there a better way? (impact of imposibility to use ProjectGenerator)
    private static final String PROJECT_NS =
            "http://www.netbeans.org/ns/project/1"; // NOI18N
    
    // elements constants
    private static final String DATA = "data"; // NOI18N
    private static final String CODE_NAME_BASE = "code-name-base"; // NOI18N
    private static final String STANDALONE = "standalone"; // NOI18N
    private static final String MODULE_DEPENDENCIES = "module-dependencies"; // NOI18N
    private static final String DEPENDENCY = "dependency"; // NOI18N
    private static final String RUN_DEPENDENCY = "run-dependency"; // NOI18N
    private static final String COMPILE_DEPENDENCY = "compile-dependency"; // NOI18N
    private static final String RELEASE_VERSION = "release-version"; // NOI18N
    private static final String SPECIFICATION_VERSION = "specification-version"; // NOI18N
    private static final String BUILD_PREREQUISITE = "build-prerequisite"; // NOI18N
    private static final String IMPLEMENTATION_VERSION = "implementation-version"; // NOI18N
    private static final String PUBLIC_PACKAGES= "public-packages"; // NOI18N
    
    private AntProjectHelper helper;
    private Project project;
    
    private Set directDeps;
    
    /** Creates a new instance of ProjectXMLManager */
    public ProjectXMLManager(AntProjectHelper helper, Project project) {
        this.helper = helper;
        this.project = project;
    }
    
    /** Returns direct module dependencies. */
    public Set/*<ModuleDependency>*/ getDirectDependencies() throws IOException {
        if (directDeps != null) {
            return directDeps;
        }
        directDeps = new TreeSet();
        Element confData = helper.getPrimaryConfigurationData(true);
        Element moduleDependencies = findModuleDependencies(confData);
        List/*<Element>*/ deps = Util.findSubElements(moduleDependencies);
        ModuleList ml = getModuleList();
        for (Iterator it = deps.iterator(); it.hasNext(); ) {
            Element depEl = (Element)it.next();
            
            Element cnbEl = Util.findElement(depEl,
                    ProjectXMLManager.CODE_NAME_BASE,
                    NbModuleProjectType.NAMESPACE_SHARED);
            String cnb = Util.findText(cnbEl);
            ModuleList.Entry me = ml.getEntry(cnb);
            
            Element runDepEl = Util.findElement(depEl,
                    ProjectXMLManager.RUN_DEPENDENCY,
                    NbModuleProjectType.NAMESPACE_SHARED);
            if (runDepEl == null) {
                directDeps.add(new ModuleDependency(me));
                continue;
            }
            
            Element relVerEl = Util.findElement(runDepEl,
                    ProjectXMLManager.RELEASE_VERSION,
                    NbModuleProjectType.NAMESPACE_SHARED);
            String relVer = null;
            if (relVerEl != null) {
                relVer = Util.findText(relVerEl);
            }
            
            Element specVerEl = Util.findElement(runDepEl,
                    ProjectXMLManager.SPECIFICATION_VERSION,
                    NbModuleProjectType.NAMESPACE_SHARED);
            String specVer = null;
            if (specVerEl != null) {
                specVer = Util.findText(specVerEl);
            }
            
            Element compDepEl = Util.findElement(depEl,
                    ProjectXMLManager.COMPILE_DEPENDENCY,
                    NbModuleProjectType.NAMESPACE_SHARED);
            
            Element impleVerEl = Util.findElement(runDepEl,
                    ProjectXMLManager.IMPLEMENTATION_VERSION,
                    NbModuleProjectType.NAMESPACE_SHARED);
            
            directDeps.add(new ModuleDependency(
                    me, relVer, specVer, compDepEl != null, impleVerEl != null));
        }
        return directDeps;
    }
    
    /** Remove given dependency from the configuration data. */
    public void removeDependency(String cnbToRemove) {
        Element confData = helper.getPrimaryConfigurationData(true);
        Element moduleDependencies = findModuleDependencies(confData);
        List/*<Element>*/ currentDeps = Util.findSubElements(moduleDependencies);
        for (Iterator it = currentDeps.iterator(); it.hasNext(); ) {
            Element dep = (Element)it.next();
            Element cnbEl = Util.findElement(dep, ProjectXMLManager.CODE_NAME_BASE,
                    NbModuleProjectType.NAMESPACE_SHARED);
            String cnb = Util.findText(cnbEl);
            if (cnbToRemove.equals(cnb)) {
                moduleDependencies.removeChild(dep);
            }
        }
        helper.putPrimaryConfigurationData(confData, true);
    }
    
    /**
     * Use this for removing more than one dependencies. It's faster then
     * iterating and using <code>removeDependency</code> for every entry.
     */
    public void removeDependencies(Collection/*<ModuleDependency>*/ depsToDelete) {
        Set cnbsToDelete = new HashSet(depsToDelete.size());
        for (Iterator it = depsToDelete.iterator(); it.hasNext(); ) {
            cnbsToDelete.add(((ModuleDependency) it.next()).
                    getModuleEntry().getCodeNameBase());
        }
        removeDependenciesByCNB(cnbsToDelete);
    }
    
    /**
     * Use this for removing more than one dependencies. It's faster then
     * iterating and using <code>removeDependency</code> for every entry.
     */
    public void removeDependenciesByCNB(Collection/*<String>*/ cnbsToDelete) {
        Element confData = helper.getPrimaryConfigurationData(true);
        Element moduleDependencies = findModuleDependencies(confData);
        List/*<Element>*/ currentDeps = Util.findSubElements(moduleDependencies);
        for (Iterator it = currentDeps.iterator(); it.hasNext(); ) {
            Element dep = (Element)it.next();
            Element cnbEl = Util.findElement(dep, ProjectXMLManager.CODE_NAME_BASE,
                    NbModuleProjectType.NAMESPACE_SHARED);
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
        helper.putPrimaryConfigurationData(confData, true);
    }
    
    public void editDependency(ModuleDependency origDep, ModuleDependency newDep) {
        Element confData = helper.getPrimaryConfigurationData(true);
        Element moduleDependencies = findModuleDependencies(confData);
        List/*<Element>*/ currentDeps = Util.findSubElements(moduleDependencies);
        for (Iterator it = currentDeps.iterator(); it.hasNext(); ) {
            Element dep = (Element) it.next();
            Element cnbEl = Util.findElement(dep, ProjectXMLManager.CODE_NAME_BASE,
                    NbModuleProjectType.NAMESPACE_SHARED);
            String cnb = Util.findText(cnbEl);
            if (cnb.equals(origDep.getModuleEntry().getCodeNameBase())) {
                moduleDependencies.removeChild(dep);
                Element nextDep = it.hasNext() ? (Element) it.next() : null;
                createModuleDependencyElement(moduleDependencies, newDep, nextDep);
                break;
            }
        }
        helper.putPrimaryConfigurationData(confData, true);
    }
    
    /**
     * Adds given modules as module-dependencies for the project.
     */
    public void addDependencies(Set/*<ModuleDependency>*/ toAdd) {
        Element confData = helper.getPrimaryConfigurationData(true);
        Element moduleDependencies = findModuleDependencies(confData);
        Document doc = moduleDependencies.getOwnerDocument();
        for (Iterator it = toAdd.iterator(); it.hasNext(); ) {
            ModuleDependency md = (ModuleDependency) it.next();
            createModuleDependencyElement(moduleDependencies, md, null);
        }
        helper.putPrimaryConfigurationData(confData, true);
    }
    
    public void replaceDependencies(Set/*<ModuleDependency>*/ newDeps) {
        Element confData = helper.getPrimaryConfigurationData(true);
        Document doc = confData.getOwnerDocument();
        Element moduleDependencies = findModuleDependencies(confData);
        Element publicPackages = Util.findElement(confData,
                ProjectXMLManager.PUBLIC_PACKAGES, NbModuleProjectType.NAMESPACE_SHARED);
        confData.removeChild(moduleDependencies);
        moduleDependencies = createModuleElement(doc, ProjectXMLManager.MODULE_DEPENDENCIES);
        confData.insertBefore(moduleDependencies, publicPackages);
        for (Iterator it = newDeps.iterator(); it.hasNext(); ) {
            ModuleDependency md = (ModuleDependency) it.next();
            createModuleDependencyElement(moduleDependencies, md, null);
        }
        helper.putPrimaryConfigurationData(confData, true);
    }
    
    private void createModuleDependencyElement(
            Element moduleDependencies, ModuleDependency md, Element nextSibling) {
        
        Document doc = moduleDependencies.getOwnerDocument();
        Element modDepEl = createModuleElement(doc, ProjectXMLManager.DEPENDENCY);
        moduleDependencies.insertBefore(modDepEl, nextSibling);
        
        modDepEl.appendChild(createModuleElement(doc, ProjectXMLManager.CODE_NAME_BASE,
                md.getModuleEntry().getCodeNameBase()));
        modDepEl.appendChild(createModuleElement(doc, ProjectXMLManager.BUILD_PREREQUISITE));
        if (md.hasCompileDependency()) {
            modDepEl.appendChild(createModuleElement(doc, ProjectXMLManager.COMPILE_DEPENDENCY));
        }
        
        Element runDepEl = createModuleElement(doc, ProjectXMLManager.RUN_DEPENDENCY);
        modDepEl.appendChild(runDepEl);
        
        String rv = md.getReleaseVersion();
        if (rv != null) {
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
    
    private Element findModuleDependencies(Element confData) {
        return Util.findElement(confData, ProjectXMLManager.MODULE_DEPENDENCIES,
                NbModuleProjectType.NAMESPACE_SHARED);
    }
    
    private static Element createModuleElement(Document doc, String name) {
        return doc.createElementNS(NbModuleProjectType.NAMESPACE_SHARED, name);
    }
    
    private static Element createModuleElement(Document doc, String name, String innerText) {
        Element el = createModuleElement(doc, name);
        el.appendChild(doc.createTextNode(innerText));
        return el;
    }
    
    /**
     * Helper method to get the <code>ModuleList</code> for the project this
     * instance manage.
     */
    private ModuleList getModuleList() throws IOException {
        return ModuleList.getModuleList(FileUtil.toFile(project.getProjectDirectory()));
    }
    
    /** Utility method for finding public packages. */
    static ManifestManager.PackageExport[] findPublicPackages(final Element confData) {
        Element ppEl = Util.findElement(confData, ProjectXMLManager.PUBLIC_PACKAGES,
                NbModuleProjectType.NAMESPACE_SHARED);
        Set/*<ManifestManager.PackageExport>*/ pps = null;
        if (ppEl != null) {
            pps = new HashSet();
            List/*<Element>*/ pkgEls = Util.findSubElements(ppEl);
            for (Iterator it = pkgEls.iterator(); it.hasNext(); ) {
                Element pkgEl = (Element) it.next();
                pps.add(new ManifestManager.PackageExport(Util.findText(pkgEl), false));
            }
        }
        return pps == null ? ManifestManager.EMPTY_EXPORTED_PACKAGES :
            (ManifestManager.PackageExport[]) pps.toArray(new ManifestManager.PackageExport[pps.size()]);
    }
    
    /**
     * Generates a basic <em>project.xml</em> templates into the given
     * <code>projectXml</code>.
     */
    static void generateEmptyTemplate(FileObject projectXml, String cnb,
            boolean standalone) throws IOException {
        
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
        if (standalone) {
            dataEl.appendChild(createModuleElement(dataDoc, STANDALONE));
        }
        dataEl.appendChild(createModuleElement(dataDoc, MODULE_DEPENDENCIES));
        dataEl.appendChild(createModuleElement(dataDoc, PUBLIC_PACKAGES));
        
        // store document to disk
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
}