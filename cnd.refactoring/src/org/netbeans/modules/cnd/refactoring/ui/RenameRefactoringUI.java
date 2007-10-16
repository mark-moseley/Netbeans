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
package org.netbeans.modules.cnd.refactoring.ui;

import java.io.IOException;
import java.text.MessageFormat;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Vladimir Voskresensky
 */
public class RenameRefactoringUI implements RefactoringUI, RefactoringUIBypass {
    private final AbstractRefactoring refactoring;
    private String oldName = null;
    private String dispOldName;
    private String newName;
    private RenamePanel panel;
    private boolean fromListener = false;
//    private TreePathHandle handle;
    private final CsmObject origObject;
    private FileObject byPassFolder;
    private boolean byPassPakageRename;
    private boolean pkgRename = true;
    
    public RenameRefactoringUI(CsmObject csmObject) {
        this.origObject = csmObject;
        this.oldName = getSearchElementName(this.origObject);  
        this.refactoring = new RenameRefactoring(Lookups.singleton(csmObject));
    }
    
    private String getSearchElementName(CsmObject csmObj) {
        assert csmObj != null;
        String objName;
        if (csmObj instanceof CsmReference) {
            objName = ((CsmReference)csmObj).getText();
        } else if (CsmKindUtilities.isNamedElement(csmObj)) {
            objName = ((CsmNamedElement)csmObj).getName();
        } else if (csmObj != null) {
            objName = "<UNNAMED ELEMENT>"; // NOI18N
        } else {
            objName = "<UNRESOLVED ELEMENT>"; // NOI18N
        }
        return objName;
    }    
//    public RenameRefactoringUI(TreePathHandle handle, CompilationInfo info) {
//        this.handle = handle;
//        this.refactoring = new RenameRefactoring(Lookups.singleton(handle));
//        Element element = handle.resolveElement(info);
//        oldName = element.getSimpleName().toString();
//        if (element.getModifiers().contains(Modifier.PRIVATE)) {
//            refactoring.getContext().add(RetoucheUtils.getClasspathInfoFor(false, handle.getFileObject()));
//        } else {
//            refactoring.getContext().add(RetoucheUtils.getClasspathInfoFor(handle));
//        }
//        dispOldName = oldName;
//    }
    
//    public RenameRefactoringUI(FileObject file, TreePathHandle handle, CompilationInfo info) {
//        if (handle!=null) {
//            this.handle = handle;
//            this.refactoring = new RenameRefactoring(Lookups.fixed(file, handle));
//            oldName = handle.resolveElement(info).getSimpleName().toString();
//        } else {
//            this.refactoring = new RenameRefactoring(Lookups.fixed(file));
//            oldName = file.getName();
//        }
//        dispOldName = oldName;
//        ClasspathInfo cpInfo = handle==null?RetoucheUtils.getClasspathInfoFor(file):RetoucheUtils.getClasspathInfoFor(handle);
//        refactoring.getContext().add(cpInfo);
//    }

//    public RenameRefactoringUI(NonRecursiveFolder file) {
//        this.refactoring = new RenameRefactoring(Lookups.singleton(file));
//        oldName = RetoucheUtils.getPackageName(file.getFolder());
//        refactoring.getContext().add(RetoucheUtils.getClasspathInfoFor(file.getFolder()));
//        dispOldName = oldName;
//        pkgRename = true;
//    }

//    RenameRefactoringUI(FileObject jmiObject, String newName, TreePathHandle handle, CompilationInfo info) {
//        if (handle!=null) {
//            this.refactoring = new RenameRefactoring(Lookups.fixed(jmiObject, handle));
//        } else {
//            this.refactoring = new RenameRefactoring(Lookups.fixed(jmiObject));
//        }
//        
//        oldName = newName;
//        //[FIXME] this should be oldName of refactored object
//        this.dispOldName = newName;
//        ClasspathInfo cpInfo = handle==null?RetoucheUtils.getClasspathInfoFor(jmiObject):RetoucheUtils.getClasspathInfoFor(handle);
//        refactoring.getContext().add(cpInfo);
//        fromListener = true;
//    }
    
//    RenameRefactoringUI(NonRecursiveFolder jmiObject, String newName) {
//        this.refactoring = new RenameRefactoring(Lookups.singleton(jmiObject));
//        refactoring.getContext().add(RetoucheUtils.getClasspathInfoFor(jmiObject.getFolder()));
//        oldName = newName;
//        //[FIXME] this should be oldName of refactored object
//        this.dispOldName = newName;
//        fromListener = true;
//        pkgRename = true;
//    }
       
    public boolean isQuery() {
        return false;
    }

    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            String name = oldName;
            String suffix = "";
//            if (handle != null) {
//                ElementKind kind = RetoucheUtils.getElementKind(handle);
//                if (kind.isClass() || kind.isInterface()) {
//                    suffix  = kind.isInterface() ? getString("LBL_Interface") : getString("LBL_Class");
//                } else if (kind == ElementKind.METHOD) {
//                    suffix = getString("LBL_Method");
//                } else if (kind == ElementKind.FIELD) {
//                    suffix = getString("LBL_Field");
//                } else if (kind == ElementKind.LOCAL_VARIABLE) {
//                    suffix = getString("LBL_LocalVar");
//                } else if (kind == ElementKind.PACKAGE || (handle == null && fromListener)) {
//                    suffix = pkgRename ? getString("LBL_Package") : getString("LBL_Folder");
//                } else if (kind == ElementKind.PARAMETER) {
//                    suffix = getString("LBL_Parameter");
//                }
//            }
            suffix = suffix + " " + name; // NOI18N
            panel = new RenamePanel(name, parent, NbBundle.getMessage(RenamePanel.class, "LBL_Rename") + " " + suffix, !fromListener, fromListener && !byPassPakageRename);
        }
        return panel;
    }
    
    private static String getString(String key) {
        return NbBundle.getMessage(RenameRefactoringUI.class, key);
    }

    public org.netbeans.modules.refactoring.api.Problem setParameters() {
        newName = panel.getNameValue();
        if (refactoring instanceof RenameRefactoring) {
            ((RenameRefactoring) refactoring).setNewName(newName);
            ((RenameRefactoring) refactoring).setSearchInComments(panel.searchJavadoc());            
        }
        return refactoring.checkParameters();
    }
    
    public org.netbeans.modules.refactoring.api.Problem checkParameters() {
        if (!panel.isUpdateReferences()) 
            return null;
        newName = panel.getNameValue();
        if (refactoring instanceof RenameRefactoring) {
            ((RenameRefactoring) refactoring).setNewName(newName);
        }
        return refactoring.fastCheckParameters();
    }

    public org.netbeans.modules.refactoring.api.AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    public String getDescription() {
        return new MessageFormat(NbBundle.getMessage(RenamePanel.class, "DSC_Rename")).format (
                    new Object[] {dispOldName, newName}
                );
    }

    public String getName() {
        return NbBundle.getMessage(RenamePanel.class, "LBL_Rename");
    }

    public boolean hasParameters() {
        return true;
    }

    public HelpCtx getHelpCtx() {
        String postfix = "";
//        if (handle==null) {
//            postfix = ".JavaPackage";//NOI18N
//        } else {
//            ElementKind k = RetoucheUtils.getElementKind(handle);
//            
//            if (k.isClass() || k.isInterface())
//                postfix = ".JavaClass";//NOI18N
//            else if (k == ElementKind.METHOD)
//                postfix = ".Method";//NOI18N
//            else if (k.isField())
//                postfix = ".Field";//NOI18N
//            else
//                postfix = "";
//        }
        
        return new HelpCtx(RenameRefactoringUI.class.getName() + postfix);
    }
    
    public boolean isRefactoringBypassRequired() {
        return !panel.isUpdateReferences();
    }
    
    public void doRefactoringBypass() throws IOException {
        DataObject dob = null;
        if (byPassFolder != null) {
            dob = DataFolder.findFolder(byPassFolder);
        } else {
            dob = DataObject.find(refactoring.getRefactoringSource().lookup(FileObject.class));
        }
        dob.rename(panel.getNameValue());
    }
}
