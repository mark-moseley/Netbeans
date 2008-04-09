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

package org.netbeans.modules.java.source.parsing;

import com.sun.tools.javac.model.JavacElements;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import junit.framework.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hrebejk
 */
public class ClasspathInfoTest extends NbTestCase {
    
    private File workDir;
    private File rtJar;
    private ClassPath bootPath;
    private ClassPath classPath;
    
    private final String SOURCE =
                "package some;" +
                "public class MemoryFile<K,V> extends javax.swing.JTable {" +
                "    public java.util.Map.Entry<K,V> entry;" +                       
                "}";
    
    public ClasspathInfoTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        this.clearWorkDir();
        File workDir = getWorkDir();
        File cacheFolder = new File (workDir, "cache"); //NOI18N
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
        TestUtil.copyFiles( TestUtil.getJdkDir(), workDir, TestUtil.RT_JAR );
        rtJar = FileUtil.normalizeFile(new File( workDir, TestUtil.RT_JAR ));
        URL url = FileUtil.getArchiveRoot (rtJar.toURI().toURL());
        this.bootPath = ClassPathSupport.createClassPath (new URL[] {url});
        this.classPath = ClassPathSupport.createClassPath(new URL[0]);
    }

    protected void tearDown() throws Exception {
        //Delete unneeded rt.jar
        rtJar.delete();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ClasspathInfoTest.class);        
        return suite;
    }

    public void testCreate() {
        ClasspathInfo ci = ClasspathInfo.create( bootPath, classPath, null);
        assertNotNull( "Classpath Info should be created", ci );
    }
    
    
    public void testGetTypeDeclaration() throws Exception {
        ClasspathInfo ci = ClasspathInfo.create( bootPath, classPath, null);
	JavacElements elements = (JavacElements) JavaSourceAccessor.getINSTANCE().createJavacTask(ci,  (DiagnosticListener) null, (String) null).getElements();
	
        List<String> notFound = new LinkedList<String>();
        JarFile jf = new JarFile( rtJar );       
        for( Enumeration entries = jf.entries(); entries.hasMoreElements(); ) {
            JarEntry je = (JarEntry)entries.nextElement();
            String jeName = je.getName();
            if ( !je.isDirectory() && jeName.endsWith( ".class" ) ) {
                String typeName = jeName.substring( 0, jeName.length() - ".class".length() );

                typeName = typeName.replace( "/", "." ); //.replace( "$", "." );
                TypeElement te = elements.getTypeElementByBinaryName( typeName );
//                assertNotNull( "Declaration for " + typeName + " should not be null.", td );
                if ( te == null ) {
                    notFound.add( typeName );
                }
            }
        }
        
        assertTrue( "Should be empty " + notFound, notFound.isEmpty() );
        
    }    
    
    public void testGetPackageDeclaration() throws Exception {
        ClasspathInfo ci = ClasspathInfo.create( bootPath, classPath, null);
        JavaFileManager fm = ClasspathInfoAccessor.getINSTANCE().getFileManager(ci);
        JarFile jf = new JarFile( rtJar );
        for( Enumeration entries = jf.entries(); entries.hasMoreElements(); ) {
            JarEntry je = (JarEntry)entries.nextElement();
            String jeName = je.getName();
            if ( je.isDirectory() ) {
                String packageName = jeName.replace( "/", "." );
                if ( !fm.list( StandardLocation.PLATFORM_CLASS_PATH,packageName, EnumSet.of( JavaFileObject.Kind.CLASS ), false).iterator().hasNext() ) {
                    // empty package
                    continue;
                }
                PackageElement pd = JavaSourceAccessor.getINSTANCE().createJavacTask(ci,  (DiagnosticListener) null, (String) null).getElements().getPackageElement( packageName );
                assertNotNull( "Declaration for " + packageName + " should not be null.", pd );
            }
        }
    }
    
    
    private static ClassPath createSourcePath (FileObject testBase) throws IOException {
        FileObject root = testBase.createFolder("src");        
        return ClassPathSupport.createClassPath(new FileObject[]{root});
    }
    
    private static FileObject createJavaFile (FileObject root, String path, String content) throws IOException {
        FileObject fo = FileUtil.createData(root, path);
        final FileLock lock = fo.lock();
        try {
            PrintWriter out = new PrintWriter (new OutputStreamWriter (fo.getOutputStream()));
            try {
                out.print(content);
            } finally {
                out.close();
            }
        } finally {
            lock.releaseLock();
        }
        return fo;
    }
    
    private static void assertEquals (final String[] binNames,
            final Iterable<JavaFileObject> jfos, final JavacFileManager fm) {
        final Set<String> bs = new HashSet<String>();
        bs.addAll(Arrays.asList(binNames));
        for (JavaFileObject jfo : jfos) {
            final String bn = fm.inferBinaryName (jfo);
            assertNotNull(bn);
            assertTrue(bs.remove(bn));
        }
        assertTrue(bs.isEmpty());
        
    }
    
    public void testMemoryFileManager () throws Exception {
        final ClassPath scp = createSourcePath(FileUtil.toFileObject(this.getWorkDir()));
        createJavaFile(scp.getRoots()[0], "org/me/Lib/java", "package org.me;\n class Lib {}\n");
        final ClasspathInfo cpInfo = ClasspathInfo.create( bootPath, classPath, scp);
        final JavacFileManager fm = ClasspathInfoAccessor.getINSTANCE().getFileManager(cpInfo);
        Iterable<JavaFileObject> jfos = fm.list(StandardLocation.SOURCE_PATH, "org.me", EnumSet.of(JavaFileObject.Kind.SOURCE), false);
        assertEquals (new String[] {"org.me.Lib"}, jfos);
        ClasspathInfoAccessor.getINSTANCE().registerVirtualSource(cpInfo, "org.me.Main", "package org.me;\n class Main{}\n");        
        jfos = fm.list(StandardLocation.SOURCE_PATH, "org.me", EnumSet.of(JavaFileObject.Kind.SOURCE), false);
        assertEquals (new String[] {"org.me.Lib","org.me.Main"}, jfos);
        ClasspathInfoAccessor.getINSTANCE().unregisterVirtualSource(cpInfo, "org.me.Main");
        jfos = fm.list(StandardLocation.SOURCE_PATH, "org.me", EnumSet.of(JavaFileObject.Kind.SOURCE), false);
        assertEquals (new String[] {"org.me.Lib"}, jfos, fm);
    }


    
}
