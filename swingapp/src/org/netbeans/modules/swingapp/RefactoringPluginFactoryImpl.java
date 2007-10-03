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

package org.netbeans.modules.swingapp;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.form.FormRefactoringUpdate;
import org.netbeans.modules.form.RefactoringInfo;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SingleCopyRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Refactoring support for classes that use Swing App Framework. If such a class
 * is renamed, moved, or copied, the corresponding properties files with
 * resources are kept in sync (i.e. renamed, moved or copied as necessary). Also
 * takes care of renamed packages (renames the resources package as well).
 * 
 * @author Tomas Pavek
 */
public class RefactoringPluginFactoryImpl implements RefactoringPluginFactory {

    public RefactoringPluginFactoryImpl() {
    }

    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        if (refactoring instanceof RenameRefactoring
                || refactoring instanceof MoveRefactoring
                || refactoring instanceof SingleCopyRefactoring) {
            Lookup sourceLookup = refactoring.getRefactoringSource();
            FileObject srcFile = sourceLookup.lookup(FileObject.class);
            NonRecursiveFolder pkg = sourceLookup.lookup(NonRecursiveFolder.class);
            if ((srcFile != null && isJavaFile(srcFile)) || pkg != null) {
                return new RefactoringPluginImpl(refactoring);
            }
        }
        return null;
    }

    // -----

    private static class RefactoringPluginImpl implements RefactoringPlugin {
        private AbstractRefactoring refactoring;

        RefactoringPluginImpl(AbstractRefactoring refactoring) {
            this.refactoring = refactoring;
        }

        public Problem preCheck() {
            return null;
        }

        public Problem checkParameters() {
            return null;
        }

        public Problem fastCheckParameters() {
            return null;
        }

        public void cancelRequest() {
        }

        public Problem prepare(RefactoringElementsBag refactoringElements) {
            Lookup sourceLookup = refactoring.getRefactoringSource();
            FileObject srcFile = sourceLookup.lookup(FileObject.class);
            NonRecursiveFolder pkg = sourceLookup.lookup(NonRecursiveFolder.class);
            if (srcFile != null && pkg == null && !srcFile.isFolder()) {
                // renaming or moving a source file - rename/move the resources accordingly
                DataObject propertiesDO;
                if (AppFrameworkSupport.isFrameworkEnabledProject(srcFile)
                        && (propertiesDO = ResourceUtils.getPropertiesDataObject(srcFile)) != null) {
                    // there is a valid properties file for a resource map
                    RefactoringElementImplementation previewElement = null;
                    if (refactoring instanceof RenameRefactoring) {
                        String displayText = "Rename file " + propertiesDO.getPrimaryFile().getNameExt();
                        previewElement = new PreviewElement(srcFile, displayText);
                    } else if (refactoring instanceof MoveRefactoring) {
                        URL targetURL = ((MoveRefactoring)refactoring).getTarget().lookup(URL.class);
                        try {
                            File f = FileUtil.normalizeFile(new File(targetURL.toURI()));
                            if (f.isDirectory()) {
                                String displayText = "Move file " + propertiesDO.getPrimaryFile().getNameExt(); // + " to "
                                previewElement = new PreviewElement(srcFile, displayText);
                            }
                        } catch (URISyntaxException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    } else if (refactoring instanceof SingleCopyRefactoring) {
                        // TBD
                    }

                    if (previewElement != null) {
                        refactoringElements.add(refactoring, previewElement);

                        ResourceMapUpdate update = new ResourceMapUpdate(refactoring, previewElement, propertiesDO);

                        // we need to rename/move the resource map before form gets updated
                        RefactoringInfo refInfo = refactoring.getContext().lookup(RefactoringInfo.class);
                        if (refInfo != null && refInfo.isForm()) {
                            assert isJavaFileOfForm(srcFile) && srcFile.equals(refInfo.getPrimaryFile());
                            refInfo.getUpdateForFile(srcFile).addPrecedingFileChange(update);
                        } else { // the source file is not a form - but we still do the update
                            refactoringElements.addFileChange(refactoring, update);
                        }
                    }
                }
            } else if (pkg != null && refactoring instanceof RenameRefactoring) { // [can't package be also moved via MoveRefactoring???]
                // renaming a package - not renaming a folder, but non-recursive move to another folder
                // we need to move the resources folder as well
                FileObject pkgFolder = pkg.getFolder();
                FileObject resFolder;
                if (AppFrameworkSupport.isFrameworkEnabledProject(pkgFolder)
                        && (resFolder = pkgFolder.getFileObject("resources")) != null) { // NOI18N
                    // there is an app frmework's resources folder
                    RefactoringElementImplementation previewElement = new PreviewElement(
                            resFolder, "Rename resources package");
                    refactoringElements.add(refactoring, previewElement);

                    ResourcePackageUpdate update = new ResourcePackageUpdate(
                            (RenameRefactoring) refactoring, previewElement, resFolder);

                    FormRefactoringUpdate formUpdate = null;
                    RefactoringInfo refInfo = refactoring.getContext().lookup(RefactoringInfo.class);
                    if (refInfo != null) {
                        for (FileObject fo : pkgFolder.getChildren()) {
                            if (isJavaFileOfForm(fo)) {
                                formUpdate = refInfo.getUpdateForFile(fo);
                                break;
                            }
                        }
                    }
                    if (formUpdate != null) {
                        formUpdate.addPrecedingFileChange(update);
                    } else {
                        refactoringElements.addFileChange(refactoring, update);
                    }
                }
            }

            return null;
        }
    }

    // -----

    private static class PreviewElement extends SimpleRefactoringElementImplementation {
        private FileObject file;
        private String displayText;

        PreviewElement(FileObject file, String displayText) {
            this.file = file;
            this.displayText = displayText;
        }

        public String getText() {
            return "Resources update";
        }

        public String getDisplayText() {
            return displayText;
        }

        public void performChange() {
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        public FileObject getParentFile() {
            return file;
        }

        public PositionBounds getPosition() {
            return null;
        }
    }

    // -----

    /**
     * Updates properties files for a resource map (rename/move/copy) according
     * to a change of the java source file.
     */
    private static class ResourceMapUpdate extends SimpleRefactoringElementImplementation {
        private AbstractRefactoring refactoring;
        private RefactoringElementImplementation refElement;

        private DataObject propertiesDO;
        private String oldName; // if renamed
        private FileObject oldFolder; // if moved
        private FileObject srcFileBefore;
        private FileObject srcFileAfter;

        ResourceMapUpdate(AbstractRefactoring refactoring, RefactoringElementImplementation previewElement, DataObject propertiesDO) {
            this.refactoring = refactoring;
            this.refElement = previewElement;
            this.propertiesDO = propertiesDO;
            oldName = propertiesDO.getName();
            oldFolder = propertiesDO.getFolder().getPrimaryFile();
            srcFileBefore = refactoring.getRefactoringSource().lookup(FileObject.class);
        }

        public void performChange() {
            if (!refElement.isEnabled()) {
                return;
            }

            // This is supposed to run after the form is renamed/moved but before it
            // does its own update - so when it does it has the resource map in place.

            if (refactoring instanceof RenameRefactoring) {
                // source file renaming within the same package
                srcFileAfter = srcFileBefore; // FileObject survives renaming
                String newName = ((RenameRefactoring)refactoring).getNewName();
                try {
                    propertiesDO.rename(newName);
                    ResourceUtils.unregisterDesignResourceMap(srcFileBefore);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else if (refactoring instanceof MoveRefactoring) {
                // source file moving to another package, but with the same name
                URL targetURL = ((MoveRefactoring)refactoring).getTarget().lookup(URL.class);
                FileObject targetFolder = null;
                try {
                    File f = FileUtil.normalizeFile(new File(targetURL.toURI()));
                    targetFolder = FileUtil.toFileObject(f);
                } catch (URISyntaxException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (targetFolder != null && targetFolder.isFolder()) {
                    srcFileAfter = targetFolder.getFileObject(oldName);
                    try {
                        targetFolder = FileUtil.createFolder(targetFolder, "resources"); // NOI18N
                        propertiesDO.move(DataFolder.findFolder(targetFolder));
                        // TODO: Also analyze the resource map and copy relatively referenced
                        // images (stored under the same resources folder). Probably we should
                        // not just move the image files - that would require to analyze if
                        // not used from elsewhere. Or if staying in the same project we could
                        // maybe just change the relative names to complete cp names.
                        ResourceUtils.unregisterDesignResourceMap(srcFileBefore);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }

        public void undoChange() {
            if (!refElement.isEnabled()) {
                return;
            }

            // This is supposed to run after the form is renamed/moved back but
            // before it does its own undo update - so when it does it has the
            // resource map in place.

            if (refactoring instanceof RenameRefactoring) {
                // source file renaming within the same package
                try {
                    propertiesDO.rename(oldName);
                    ResourceUtils.unregisterDesignResourceMap(srcFileAfter);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else if (refactoring instanceof MoveRefactoring) {
                // source file moving to another package, but with the same name
                try {
                    propertiesDO.move(DataFolder.findFolder(oldFolder));
                    ResourceUtils.unregisterDesignResourceMap(srcFileAfter);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                srcFileBefore = oldFolder.getParent().getFileObject(oldName);
            }
        }

        public String getText() {
            return "Resources update";
        }

        public String getDisplayText() {
            return "Post-refactoring: Resources update";
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        public FileObject getParentFile() {
            return propertiesDO.getPrimaryFile();
        }

        public PositionBounds getPosition() {
            return null;
        }
    }

    // -----

    /**
     * Moves resources folder according to package rename (i.e. non-recursive
     * folder move).
     */
    private static class ResourcePackageUpdate extends SimpleRefactoringElementImplementation {
        private RenameRefactoring refactoring;
        private RefactoringElementImplementation refElement;

        private String oldPkgName;
        private DataFolder resFolder; // DataFolder survives the move

        ResourcePackageUpdate(RenameRefactoring refactoring, RefactoringElementImplementation refElement, FileObject resFolder) {
            this.refactoring = refactoring;
            this.refElement = refElement;
            this.resFolder = DataFolder.findFolder(resFolder);
            oldPkgName = ClassPath.getClassPath(resFolder, ClassPath.SOURCE)
                    .getResourceName(resFolder.getParent(), '.', false);
        }

        public void performChange() {
            if (!refElement.isEnabled()) {
                return;
            }

            // This is supposed to run after the original package is renamed but
            // before the forms are updated - so when the forms are loaded they
            // have the resources in place.

            ClassPath cp = ClassPath.getClassPath(resFolder.getPrimaryFile(), ClassPath.SOURCE);
            DataFolder targetFolder = DataFolder.findFolder(
                    cp.findResource(refactoring.getNewName().replace('.', '/')));
            try {
                resFolder.move(targetFolder);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public void undoChange() {
            if (!refElement.isEnabled()) {
                return;
            }

            // This is suposed to run after the package is renamed back but
            // before the forms perform their undo update - so when the forms
            // loaded they have the resources in place.

            ClassPath cp = ClassPath.getClassPath(resFolder.getPrimaryFile(), ClassPath.SOURCE);
            DataFolder targetFolder = DataFolder.findFolder(
                    cp.findResource(oldPkgName.replace('.', '/')));
            try {
                resFolder.move(targetFolder);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public String getText() {
            return "Resources update";
        }

        public String getDisplayText() {
            return "Post-refactoring: Resources update";
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        public FileObject getParentFile() {
            return resFolder.getPrimaryFile();
        }

        public PositionBounds getPosition() {
            return null;
        }
    }

    // -----

    private static boolean isJavaFile(FileObject fo) {
        return "text/x-java".equals(fo.getMIMEType()); // NOI18N
    }

    static boolean isJavaFileOfForm(FileObject fo) {
        return isJavaFile(fo) && fo.existsExt("form"); // NOI18N
    }
}
