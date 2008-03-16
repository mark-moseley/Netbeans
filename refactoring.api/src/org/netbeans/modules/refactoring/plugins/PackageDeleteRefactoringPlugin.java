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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.refactoring.plugins;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Bharath Ravi Kumar
 */
public class PackageDeleteRefactoringPlugin implements RefactoringPlugin{

    private final SafeDeleteRefactoring refactoring;
    static final String JAVA_EXTENSION = "java";//NOI18N
    
    public PackageDeleteRefactoringPlugin(SafeDeleteRefactoring safeDeleteRefactoring) {
       refactoring = safeDeleteRefactoring;
    }

    public Problem prepare(RefactoringElementsBag refactoringElements) {
        Lookup lkp = refactoring.getRefactoringSource();
        NonRecursiveFolder folder = lkp.lookup(NonRecursiveFolder.class);
        if (folder != null) {
            return preparePackageDelete(folder, refactoringElements);
        }
        
        FileObject fileObject = lkp.lookup(FileObject.class);
        if (fileObject != null && fileObject.isFolder()) {
            return prepareFolderDelete(fileObject, refactoringElements);
        }
        return null;
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

    //Private methods
    
    private Problem prepareFolderDelete(FileObject fileObject, RefactoringElementsBag refactoringElements) {
            addDataFilesInFolder(fileObject, refactoringElements);
            refactoringElements.addFileChange(refactoring, new FolderDeleteElem(fileObject));
            return null;
    }

    private Problem preparePackageDelete(NonRecursiveFolder folder, RefactoringElementsBag refactoringElements) {
        DataFolder dataFolder = DataFolder.findFolder(folder.getFolder());
        // First; delete all files except packages
        DataObject children[] = dataFolder.getChildren();
        boolean empty = true;
        for( int i = 0; children != null && i < children.length; i++ ) {
            FileObject fileObject = children[i].getPrimaryFile();
            if ( !fileObject.isFolder() ) {
                refactoringElements.addFileChange(refactoring, new FileDeletePlugin.DeleteFile(fileObject, refactoringElements));
            }
            else {
                empty = false;
            }
        }

        // If empty delete itself
        if ( empty ) {
            refactoringElements.addFileChange(refactoring, new PackageDeleteElem(folder));
        }
            
        return null;
    }

    private void addDataFilesInFolder(FileObject folderFileObject, RefactoringElementsBag refactoringElements) {
        for (FileObject childFileObject : folderFileObject.getChildren()) {
            if (!childFileObject.isFolder()) {
                refactoringElements.addFileChange(refactoring, new FileDeletePlugin.DeleteFile(childFileObject, refactoringElements));
            }
            else if (childFileObject.isFolder()) {
                addDataFilesInFolder(childFileObject, refactoringElements);
            }
        }
    }
    
    //Copied from BackupFacility
    private static void createNewFolder(File f) throws IOException {
        if (!f.exists()) {
            File parent = f.getParentFile();
            if (parent != null) {
                createNewFolder(parent);
            }
            f.mkdir();
        }
    }

    private static class FolderDeleteElem extends SimpleRefactoringElementImplementation{
        
        private final FileObject dirFileObject;
        private File dir;
        
        private FolderDeleteElem(FileObject folder){
            dirFileObject = folder;
            dir = FileUtil.toFile(dirFileObject);
        }

        public void performChange() {
            try {
                dirFileObject.delete();
            } catch (IOException ioException) {
                ErrorManager.getDefault().notify(ioException);
            }
        }

        @Override
        public void undoChange() {
            try {
                createNewFolder(dir);
            } catch (IOException ioException) {
                ErrorManager.getDefault().notify(ioException);
            }
        }
        
        public String getText() {
            return NbBundle.getMessage(FileDeletePlugin.class, "TXT_DeleteFolder", 
                    dirFileObject.getNameExt());
        }

        public String getDisplayText() {
            return getText();
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        public FileObject getParentFile() {
            try {
                return URLMapper.findFileObject(dirFileObject.getURL());
            } catch (FileStateInvalidException ex) {
                ErrorManager.getDefault().notify(ex);
                throw new IllegalStateException(ex);
            }
        }

        public PositionBounds getPosition() {
            return null;
        }

    }
    
    private static class PackageDeleteElem extends SimpleRefactoringElementImplementation{

        private final URL res;
        private final NonRecursiveFolder folder;
        
        private File dir;
        private final SourceGroup srcGroup;
        
        private PackageDeleteElem(NonRecursiveFolder folder) {
            this.folder = folder;
            dir = FileUtil.toFile(folder.getFolder());
            try {
                res = folder.getFolder().getURL();
            } catch (FileStateInvalidException fileStateInvalidException) {
                throw new IllegalStateException(fileStateInvalidException);
            }
            srcGroup = getJavaSourceGroup(folder.getFolder());
        }
        
        public void performChange() {
            FileObject root = srcGroup.getRootFolder();
            FileObject parent = folder.getFolder().getParent();
            dir = FileUtil.toFile(folder.getFolder());
            try {
                folder.getFolder().delete();
                while( !parent.equals( root ) && parent.getChildren().length == 0  ) {
                    FileObject newParent = parent.getParent();
                    parent.delete();
                    parent = newParent;
                }
            } catch (IOException ioException) {
                ErrorManager.getDefault().notify(ioException);
            }
        }

        @Override
        public void undoChange() {
            try {
                createNewFolder(dir);
            } catch (IOException ioException) {
                ErrorManager.getDefault().notify(ioException);
            }
        }
        
        public String getText() {
            return NbBundle.getMessage(FileDeletePlugin.class, "TXT_DeletePackage", dir.getName());
        }

        public String getDisplayText() {
            return getText();
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        public FileObject getParentFile() {
            return URLMapper.findFileObject(res);
        }

        public PositionBounds getPosition() {
            return null;
        }

        private SourceGroup getJavaSourceGroup(FileObject file) {
            Project prj = FileOwnerQuery.getOwner(file);
            if (prj == null)
                return null;
            Sources src = ProjectUtils.getSources(prj);
            SourceGroup[] javagroups = src.getSourceGroups(JAVA_EXTENSION);

            for(SourceGroup javaSourceGroup : javagroups) {
                if (javaSourceGroup.getRootFolder().equals(file) || FileUtil.isParentOf(javaSourceGroup.getRootFolder(), file))
                    return javaSourceGroup;
            }
            return null;
        }
        
    }

}
