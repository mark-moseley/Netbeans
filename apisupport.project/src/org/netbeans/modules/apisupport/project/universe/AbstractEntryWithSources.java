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

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectType;
import org.netbeans.modules.apisupport.project.Util;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Element;

abstract class AbstractEntryWithSources extends AbstractEntry {
    
    private LocalizedBundleInfo bundleInfo;
    
    protected LocalizedBundleInfo getBundleInfo() {
        if (bundleInfo == null) {
            bundleInfo = ModuleList.loadBundleInfo(getSourceLocation());
        }
        return bundleInfo;
    }
    
    protected Set/*<String>*/ computePublicClassNamesInMainModule() throws IOException {
        Set/*<String>*/ result = new HashSet();
        File src = new File(getSourceLocation(), "src"); // XXX hardcoding src.dir // NOI18N
        ManifestManager.PackageExport[] pkgs = getPublicPackages();
        for (int i = 0; i < pkgs.length; i++) {
            String pkg = pkgs[i].getPackage();
            scanForClasses(result, pkg, new File(src, pkg.replace('.', File.separatorChar)), pkgs[i].isRecursive());
        }
        return result;
    }
    
    private void scanForClasses(Set/*<String>*/ result, String pkg, File dir, boolean recurse) throws IOException {
        if (!dir.isDirectory()) {
            return;
        }
        File[] kids = dir.listFiles();
        if (kids == null) {
            throw new IOException(dir.getAbsolutePath());
        }
        for (int i = 0; i < kids.length; i++) {
            String name = kids[i].getName();
            if (name.endsWith(".java")) { // NOI18N
                String basename = name.substring(0, name.length() - 5);
                result.add(pkg + '.' + basename);
                // no inner classes scanned, too slow
            }
            if (recurse && kids[i].isDirectory()) {
                scanForClasses(result, pkg + '.' + name, kids[i], true);
            }
        }
    }

    public String[] getRunDependencies() {
        Set/*<String>*/ deps = new TreeSet();
        FileObject source = FileUtil.toFileObject(getSourceLocation());
        if (source == null) { // ??
            return new String[0];
        }
        NbModuleProject project;
        try {
            project = (NbModuleProject) ProjectManager.getDefault().findProject(source);
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
            return new String[0];
        }
        Element data = project.getPrimaryConfigurationData();
        Element moduleDependencies = Util.findElement(data,
            "module-dependencies", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
        List/*<Element>*/ depEls = Util.findSubElements(moduleDependencies);
        Iterator it = depEls.iterator();
        while (it.hasNext()) {
            Element dep = (Element) it.next();
            if (Util.findElement(dep, "run-dependency", // NOI18N
                    NbModuleProjectType.NAMESPACE_SHARED) == null) {
                continue;
            }
            Element cnbEl = Util.findElement(dep, "code-name-base", // NOI18N
                NbModuleProjectType.NAMESPACE_SHARED);
            String cnb = Util.findText(cnbEl);
            deps.add(cnb);
        }
        return (String[]) deps.toArray(new String[deps.size()]);
    }

    public String getSpecificationVersion() {
        FileObject source = FileUtil.toFileObject(getSourceLocation());
        if (source != null) {
            NbModuleProject project;
            try {
                project = (NbModuleProject) ProjectManager.getDefault().findProject(source);
                if (project != null) {
                    return project.getSpecVersion();
                }
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return null;
    }
    
}
