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

package org.netbeans.core.startup.layers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;

public class NbinstURLMapperTest extends NbTestCase {

    private static final String FILE_NAME = "test.txt";     //NOI18N
    private static final String FOLDER_NAME = "modules";    //NOI18N
    
    private File testFile;
    private int expectedLength;

    public NbinstURLMapperTest (String testName) throws IOException {
        super (testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        MockServices.setServices(
                TestInstalledFileLocator.class,
                NbinstURLStreamHandlerFactory.class,
                NbinstURLMapper.class);

        org.netbeans.core.startup.Main.initializeURLFactory ();
        
        File f = this.getWorkDir();
        this.clearWorkDir();
        Lookup.Result result = Lookup.getDefault().lookupResult(InstalledFileLocator.class);
        boolean found = false;
        for (java.util.Iterator it = result.allInstances().iterator(); it.hasNext();) {
            Object locator = it.next();
            if (locator instanceof TestInstalledFileLocator) {
                ((TestInstalledFileLocator)locator).setRoot(f);
                found = true;
            }
        }
        assertTrue("No TestInstalledFileLocator can be found in " + Lookup.getDefault(), found);
        f = new File (f,FOLDER_NAME);
        f.mkdir();
        f = new File (f,FILE_NAME);
        f.createNewFile();
        testFile = f;
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(f));
            pw.println(FILE_NAME);
        } finally {
            if (pw!=null) {
                pw.close ();
            }
        }
        this.expectedLength = (int) f.length();
    }

    public void testFindFileObject () throws MalformedURLException, IOException {
        URL url = new URL ("nbinst:///modules/test.txt");  //NOI18N
        FileObject fo = URLMapper.findFileObject (url);
        assertNotNull ("The nbinst URL was not resolved.",fo);
        assertEquals("URLMapper returned wrong file.",FileUtil.toFile(fo),testFile);
        url = new URL ("nbinst://test-module/modules/test.txt");
        fo = URLMapper.findFileObject (url);
        assertNotNull ("The nbinst URL was not resolved.",fo);
        assertEquals("URLMapper returned wrong file.",FileUtil.toFile(fo),testFile);
        url = new URL ("nbinst://foo-module/modules/test.txt");
        fo = URLMapper.findFileObject (url);
        assertNull ("The nbinst URL was resolved.",fo);
    }

    public void testURLConnection() throws MalformedURLException, IOException {
        URL url = new URL ("nbinst:///modules/test.txt");                //NOI18N
        URLConnection connection = url.openConnection();
        assertEquals ("URLConnection returned wrong content length.",connection.getContentLength(),expectedLength);
        BufferedReader in = null;
        try {
            in = new BufferedReader  ( new InputStreamReader (connection.getInputStream()));
            String line = in.readLine();
            assertTrue("URLConnection returned invalid InputStream",line.equals(FILE_NAME));
        } finally {
            if (in != null) {
                in.close ();
            }
        }
    }

    public static class TestInstalledFileLocator extends InstalledFileLocator {

        private File root;

        public TestInstalledFileLocator() {}

        public void setRoot (File root) {
            this.root = root;
        }

        public File locate(String relativePath, String codeNameBase, boolean localized) {
            assert relativePath != null;
            if (root == null) {
                return null;
            }
            if (codeNameBase!= null && !"test-module".equals(codeNameBase)) {
                return null;
            }
            StringTokenizer tk = new StringTokenizer(relativePath,"/");
            File f = this.root;
            while (tk.hasMoreTokens()) {
                String part = tk.nextToken();
                f = new File (f,part);
                if (!f.exists()) {
                    return null;
                }
            }
            return f;
        }
    }

}
