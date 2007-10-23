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

package org.netbeans.modules.apisupport.project.ui.wizard.moduleinstall;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 * Data model used across the <em>New Module Installer</em>.
 */
final class DataModel extends BasicWizardIterator.BasicDataModel {

    static final String OPENIDE_MODULE_INSTALL = "OpenIDE-Module-Install"; // NOI18N
    private static final String INSTALLER_CLASS_NAME = "Installer"; // NOI18N
    
    private CreatedModifiedFiles cmf;
    
    DataModel(final WizardDescriptor wiz) {
        super(wiz);
    }
    
    CreatedModifiedFiles getCreatedModifiedFiles() {
        if (cmf == null) {
            regenerate();
        }
        return cmf;
    }
    
    private void regenerate() {
        cmf = new CreatedModifiedFiles(getProject());
        
        // obtain unique class name
        String className = INSTALLER_CLASS_NAME;
        String path = getDefaultPackagePath(className + ".java", false); // NOI18N
        int i = 0;
        while (alreadyExist(path)) {
            className = INSTALLER_CLASS_NAME + '_' + ++i;
            path = getDefaultPackagePath(className + ".java", false); // NOI18N
        }
        
        // generate .java file for ModuleInstall
        Map<String, String> basicTokens = new HashMap<String, String>();
        basicTokens.put("PACKAGE_NAME", getPackageName()); // NOI18N
        basicTokens.put("CLASS_NAME", className); // NOI18N
        // XXX use nbresloc URL protocol rather than
        // DataModel.class.getResource(...) and all such a cases below
        FileObject template = CreatedModifiedFiles.getTemplate("moduleInstall.java"); // NOI18N
        cmf.add(cmf.createFileWithSubstitutions(path, template, basicTokens));
        
        cmf.add(cmf.addModuleDependency("org.openide.modules")); // NOI18N
        cmf.add(cmf.addModuleDependency("org.openide.util")); // NOI18N
        
        // add manifest attribute
        Map<String, String> attribs = new HashMap<String, String>();
        attribs.put(OPENIDE_MODULE_INSTALL, getPackageName().replace('.','/') + '/' + className + ".class"); // NOI18N
        cmf.add(cmf.manifestModification(null, attribs));
    }
    
    private void reset() {
        cmf = null;
    }
    
    public @Override void setPackageName(String packageName) {
        super.setPackageName(packageName);
        reset();
    }
    
    private boolean alreadyExist(String relPath) {
        return getProject().getProjectDirectory().getFileObject(relPath) != null;
    }
    
}
