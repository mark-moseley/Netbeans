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

package org.netbeans.api.java.source;

import java.io.File;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class TypeMirrorHandleTest extends NbTestCase {
    
    private FileObject testSource;
    
    public TypeMirrorHandleTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        this.clearWorkDir();
        File workDir = getWorkDir();
        File cacheFolder = new File (workDir, "cache"); //NOI18N
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
    }

    private TypeMirror parse(CompilationInfo info, String name) {
        TypeElement string = info.getElements().getTypeElement("test.Test");
        
        assertNotNull(string);
        
        return info.getTreeUtilities().parseType(name, string);
    }
    
    //TODO: cannot handle wildcards, as Types.isSameType returns false for wildcards:
    private void testCase(CompilationInfo info, String name) {
        TypeMirror tm = parse(info, name);
        TypeMirrorHandle th = TypeMirrorHandle.create(tm);
        
        assertTrue(info.getTypes().isSameType(th.resolve(info), tm));
        assertTrue(info.getTypes().isSameType(tm, th.resolve(info)));
    }
    
    private void writeIntoFile(FileObject file, String what) throws Exception {
        FileLock lock = file.lock();
        OutputStream out = file.getOutputStream(lock);
        
        try {
            out.write(what.getBytes());
        } finally {
            out.close();
            lock.releaseLock();
        }
    }
    
    private void prepareTest() throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        
        testSource = fs.getRoot().createData("Test.java");
        assertNotNull(testSource);
    }
    
    public void testTypeMirrorHandle() throws Exception {
        prepareTest();
        writeIntoFile(testSource, "package test; public class Test<T> {}");
        ClassPath empty = ClassPathSupport.createClassPath(new URL[0]);
        JavaSource js = JavaSource.create(ClasspathInfo.create(ClassPathSupport.createClassPath(SourceUtilsTestUtil.getBootClassPath().toArray(new URL[0])), empty, empty), testSource);
        
        js.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {
            }
            public void run(CompilationController info) throws Exception {
                info.toPhase(Phase.RESOLVED);
                testCase(info, "java.util.Map");
                testCase(info, "java.util.Map<java.lang.Object, java.util.List>");
                testCase(info, "java.util.Map<java.lang.Object, java.util.List<java.lang.String>>");
                testCase(info, "int[]");
            }
        }, true);
    }

    public void testTypeMirrorHandleCannotResolve() throws Exception {
        prepareTest();
        writeIntoFile(testSource, "package test; public class Test {} class Test1{}");
        ClassPath empty = ClassPathSupport.createClassPath(new URL[0]);
        JavaSource js = JavaSource.create(ClasspathInfo.create(ClassPathSupport.createClassPath(SourceUtilsTestUtil.getBootClassPath().toArray(new URL[0])), empty, empty), testSource);
        final List<TypeMirrorHandle> handles = new ArrayList<TypeMirrorHandle>();
        
        js.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {
            }
            public void run(CompilationController info) throws Exception {
                info.toPhase(Phase.RESOLVED);
                handles.add(TypeMirrorHandle.create(parse(info, "test.Test1")));
                handles.add(TypeMirrorHandle.create(parse(info, "java.util.List<test.Test1>")));
                handles.add(TypeMirrorHandle.create(parse(info, "test.Test1[]")));
            }
        }, true);
        writeIntoFile(testSource, "package test; public class Test {}");
        js.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {
            }
            public void run(CompilationController info) throws Exception {
                info.toPhase(Phase.RESOLVED);
                
                int count = 0;
                
                for (TypeMirrorHandle h : handles) {
                    assertNull(String.valueOf(count++), h.resolve(info));
                }
            }
        }, true);
    }
    
}
