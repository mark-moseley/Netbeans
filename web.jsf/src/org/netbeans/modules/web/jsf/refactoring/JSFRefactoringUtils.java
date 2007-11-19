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

package org.netbeans.modules.web.jsf.refactoring;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
/**
 *
 * @author Petr Pisl
 */
public class JSFRefactoringUtils {

    private static final Logger LOGGER = Logger.getLogger(JSFRefactoringUtils.class.getName());
    
    private JSFRefactoringUtils() {
    }
    
    //TODO this is copy from org.netbeans.modules.refactoring.java.RetoucheUtils
    //Probably this methods will be moved to an api 
    public static String getPackageName(FileObject folder) {
        assert folder.isFolder() : "argument must be folder";  //NOI18N
        return ClassPath.getClassPath(
                folder, ClassPath.SOURCE)
                .getResourceName(folder, '.', false);
    }
    
    //TODO this is copy from org.netbeans.modules.refactoring.java.RetoucheUtils
    //Probably this methods will be moved to an api 
    public static String getPackageName(URL url) {
        File file = null;
        try {
            file = FileUtil.normalizeFile(new File(url.toURI()));
        } catch (URISyntaxException uRISyntaxException) {
            throw new IllegalArgumentException("Cannot create package name for url " + url);  //NOI18N
        }
        String suffix = "";
        
        do {
            FileObject fileObject = FileUtil.toFileObject(file);
            if (fileObject != null) {
                if ("".equals(suffix))
                    return getPackageName(fileObject);
                String prefix = getPackageName(fileObject);
                return prefix + ("".equals(prefix)?"":".") + suffix;
            }
            if (!"".equals(suffix)) {
                suffix = "." + suffix;
            }
            suffix = URLDecoder.decode(file.getPath().substring(file.getPath().lastIndexOf(File.separatorChar)+1)) + suffix;
            file = file.getParentFile();
        } while (file!=null);
        throw new IllegalArgumentException("Cannot create package name for url " + url);  //NOI18N
    }

    
    public static boolean containsRenamingPackage(String oldFQCN, String oldPackage, boolean renameSubpackages){
        boolean contains = false;
        if (oldFQCN != null && oldPackage != null) {
            if (!renameSubpackages){
                if (oldFQCN.startsWith(oldPackage)
                        && oldFQCN.substring(oldPackage.length()+1).indexOf('.') < 0
                        && oldFQCN.substring(oldPackage.length()).charAt(0) == '.'){
                    contains = true;
                }
            }
            else {
                if (oldFQCN.startsWith(oldPackage) 
                        && oldFQCN.substring(oldPackage.length()).charAt(0) == '.'){
                    contains = true;
                }
            }
        }
        return contains;
    }
    
    public static void renamePackage(AbstractRefactoring refactoring, RefactoringElementsBag refactoringElements, 
            FileObject folder, String oldFQPN, String newFQPN, boolean recursive){
        WebModule webModule = WebModule.getWebModule(folder);
        if (webModule != null){
            List <Occurrences.OccurrenceItem> items = Occurrences.getPackageOccurrences(webModule, oldFQPN, newFQPN, recursive);
            Modifications modification = new Modifications();
            for (Occurrences.OccurrenceItem item : items) {
                Modifications.Difference difference = new Modifications.Difference(
                                Modifications.Difference.Kind.CHANGE, item.getChangePosition().getBegin(),
                                item.getChangePosition().getEnd(), item.getOldValue(), item.getNewValue(), item.getRenamePackageMessage());
                modification.addDifference(item.getFacesConfig(), difference);
                refactoringElements.add(refactoring, new DiffElement.ChangeFQCNElement(difference, item, modification));
            }
        }
    }

    private static final String JAVA_MIME_TYPE = "text/x-java"; //NOI18N

    public static boolean isJavaFile(FileObject f) {
        return JAVA_MIME_TYPE.equals(f.getMIMEType()); 
    }
    
    public static CompilationInfo getCompilationInfo(final AbstractRefactoring refactoring, final FileObject fileObject) {
        CompilationInfo compilationInfo = refactoring.getContext().lookup(CompilationInfo.class);
        
        if (compilationInfo == null && fileObject != null) {
            final ClasspathInfo cpInfo = refactoring.getContext().lookup(ClasspathInfo.class);
            JavaSource source = JavaSource.create(cpInfo, new FileObject[]{fileObject});
            try {
                source.runUserActionTask(new Task<CompilationController>() {

                    public void run(CompilationController compilationController) throws Exception {
                        compilationController.toPhase(JavaSource.Phase.RESOLVED);
                        refactoring.getContext().add(compilationController);
                    }
                }, false);
            } catch (IOException exception) {
                LOGGER.log(Level.WARNING, "Exception in JSFSafeDeletePlugin", exception); //NOI18NN
            }
            compilationInfo = refactoring.getContext().lookup(CompilationInfo.class);
        }
        return compilationInfo;
    }
}
