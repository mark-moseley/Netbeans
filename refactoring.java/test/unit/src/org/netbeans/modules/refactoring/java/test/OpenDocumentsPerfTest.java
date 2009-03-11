/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.test;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import junit.framework.Test;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.WhereUsedQueryConstants;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Test that all java-source instances are disposed at the end.
 *
 * @author Pavel Flaska
 */
public class OpenDocumentsPerfTest extends RefPerfTestCase {

    public OpenDocumentsPerfTest(String name) {
        super(name);
    }

    /**
     * Set-up the services and project
     */
//    @Override
//    protected void setUp() throws IOException, InterruptedException {
//        clearWorkDir();
//        String work = getWorkDirPath();
//        System.setProperty("netbeans.user", work);
//        projectDir = openProject("SimpleJ2SEApp", getDataDir());
//        File projectSourceRoot = new File(getWorkDirPath(), "SimpleJ2SEApp.src".replace('.', File.separatorChar));
//        FileObject fo = FileUtil.toFileObject(projectSourceRoot);
//
//        boot = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
//        source = createSourcePath(projectDir);
//        compile = createEmptyPath();
//    }

    public void testOpenDocuments()
            throws IOException, InterruptedException, ExecutionException {
        // logging is used to obtain data about consumed time
        Logger timer = Logger.getLogger("TIMER.RefactoringSession");
        timer.setLevel(Level.FINE);
        timer.addHandler(handler);

        timer = Logger.getLogger("TIMER.RefactoringPrepare");
        timer.setLevel(Level.FINE);
        timer.addHandler(handler);

        Log.enableInstances(Logger.getLogger("TIMER"), "JavacParser", Level.FINEST);

        FileObject testFile = getProjectDir().getFileObject("/src/bsh/This.java");
        JavaSource src = JavaSource.forFileObject(testFile);
        
        final WhereUsedQuery wuq = new WhereUsedQuery(Lookup.EMPTY);
        src.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement klass = controller.getElements().getTypeElement("bsh.This");
                TypeMirror mirror = klass.getSuperclass();
                Element object = controller.getTypes().asElement(mirror);
                wuq.setRefactoringSource(Lookups.singleton(TreePathHandle.create(object, controller)));
                ClasspathInfo cpi = RetoucheUtils.getClasspathInfoFor(TreePathHandle.create(klass, controller));
                wuq.getContext().add(cpi);
            }
        }, false);
        
        wuq.putValue(WhereUsedQueryConstants.FIND_SUBCLASSES, true);
        RefactoringSession rs = RefactoringSession.create("Session");
        wuq.prepare(rs);
        rs.doRefactoring(false);
        Collection<RefactoringElement> elems = rs.getRefactoringElements();
        System.err.println(elems.size());
        for (RefactoringElement e : elems) {
            System.err.println(e.getText());
        }
        src = null;
        System.gc(); System.gc();
        Log.assertInstances("Some instances of parser were not GCed");
    }

    public static Test suite() throws InterruptedException {
        return NbModuleSuite.create(NbModuleSuite.emptyConfiguration().clusters(".*").addTest(OpenDocumentsPerfTest.class, "testOpenDocuments").gui(false));
    }
}