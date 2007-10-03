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
package org.netbeans.modules.j2ee.persistence.sourcetestsupport;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * A base class for unit tests.
 *
 * @author Erno Mononen
 */
public abstract class SourceTestSupport extends NbTestCase {
    static {
        // set the lookup which will be returned by Lookup.getDefault()
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
        assertEquals("Unable to set the default lookup!", Lkp.class, Lookup.getDefault().getClass());
        setLookups();
        assertEquals(RepositoryImpl.class, Lookup.getDefault().lookup(Repository.class).getClass());
        assertEquals("The default Repository is not our repository!", RepositoryImpl.class, Repository.getDefault().getClass());
    }
    
    
    public SourceTestSupport(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        ClassPathProviderImpl classPathProvider = new ClassPathProviderImpl(new FileObject[]{FileUtil.toFileObject(getWorkDir())});
        setLookups(
                classPathProvider,
                new FakeJavaDataLoaderPool(),
                new TestSourceLevelQueryImplementation()
                );
        initTemplates();
        setCacheFolder();
    }
    
    protected void tearDown() throws Exception{
        super.tearDown();
        getSystemFs().reset();
    }
    
    private RepositoryImpl.MultiFileSystemImpl getSystemFs(){
        return (RepositoryImpl.MultiFileSystemImpl)Repository.getDefault().getDefaultFileSystem();
    }
    
    private void setCacheFolder() throws IOException{
        File cacheFolder = new File(getWorkDir(),"cache");
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
    }
    
    private void initTemplates() throws Exception{
        RepositoryImpl.MultiFileSystemImpl systemFS = getSystemFs();
        FileObject interfaceTemplate = systemFS.getRoot().getFileObject("Templates/Classes/Interface.java");
        TestUtilities.copyStringToFileObject(interfaceTemplate,
                "package ${package};" +
                "public interface ${name} {\n" +
                "}");
        FileObject classTemplate = systemFS.getRoot().getFileObject("Templates/Classes/Class.java");
        TestUtilities.copyStringToFileObject(classTemplate,
                "package ${package};" +
                "public class ${name} {\n" +
                "}");
    }
    
    protected void assertFile(FileObject result){
        assertFile( getGoldenFile(), FileUtil.toFile(result));
    }
    
    // temporary methods for debugging
    
    protected void print(FileObject fo) throws IOException {
        print(FileUtil.toFile(fo));
    }
    
    protected void print(File file) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file));
        PrintStream out = System.out;
        String str;
        while ((str = in.readLine()) != null) {
            out.println(str);
        }
        in.close();
    }
    
    private static void setLookups(Object... lookups) {
        ((Lkp)Lookup.getDefault()).setProxyLookups(Lookups.fixed(lookups));
    }
    
    public static final class Lkp extends ProxyLookup {
        
        private final Repository repository = new RepositoryImpl();
        
        public Lkp() {
            setProxyLookups(new Lookup[0]);
        }
        
        private void setProxyLookups(Lookup... lookups) {
            Lookup[] allLookups = new Lookup[lookups.length + 3];
            ClassLoader classLoader = SourceTestSupport.class.getClassLoader();
            allLookups[0] = Lookups.singleton(classLoader);
            allLookups[1] = Lookups.singleton(repository);
            System.arraycopy(lookups, 0, allLookups, 2, lookups.length);
            allLookups[allLookups.length - 1] = Lookups.metaInfServices(classLoader);
            setLookups(allLookups);
        }
    }
    
    public static final class TestSourceLevelQueryImplementation implements SourceLevelQueryImplementation {
        
        public String getSourceLevel(FileObject javaFile) {
            return "1.5";
        }
        
    }
}