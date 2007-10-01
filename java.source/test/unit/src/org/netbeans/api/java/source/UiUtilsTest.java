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

package org.netbeans.api.java.source;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import junit.framework.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Tomas Zezula
 */
public class UiUtilsTest extends NbTestCase {    
    
    private static final String JTABLE_DATA = "jdk/JTable.java";    //NOI18N
    
    public UiUtilsTest(String testName) {
	super(testName);
	
    }

    protected void setUp() throws Exception {
	this.clearWorkDir();	
	File f = new File (this.getWorkDir(),"cache");	//NOI18N
	f.mkdirs();
	IndexUtil.setCacheFolder(f);	
	SharedClassObject loader = JavaDataLoader.findObject(JavaDataLoader.class, true);
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[] {
	    loader,
	    new DummyClassPathProvider ()
	});
    }

    protected void tearDown() throws Exception {
    }

    

    public void testOpen() throws IOException {
	FileObject workDir = FileUtil.toFileObject(this.getWorkDir());
	assertNotNull (workDir);
	FileObject dataDir = FileUtil.toFileObject(this.getDataDir());
	assertNotNull (dataDir);
	FileObject srcFile = createSource (dataDir, workDir);	
	JavaSource js = JavaSource.forFileObject (srcFile);
        ClasspathInfo cpInfo = js.getClasspathInfo();
	CompilationInfo ci = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);	
        Elements elements = ci.getElements ();
	Element ce = elements.getTypeElement("javax.swing.JTable");
	assertNotNull(ce);	
        Object[] result = UiUtils.getOpenInfo(cpInfo, ce);
        assertNotNull(result);
	assertTrue (result[0] instanceof FileObject);
	assertTrue (result[1] instanceof Integer);
	assertEquals (srcFile, result[0]);
        assertEquals (5924, ((Integer) result[1]).intValue());
    }
    
    private static FileObject getSrcRoot (FileObject wrkRoot) throws IOException {
	FileObject src = wrkRoot.getFileObject("src");	//NOI18N
	if (src == null) {
	    src = wrkRoot.createFolder("src");		//NOI18N
	}
	return src;
    }
    
    private static FileObject createSource (FileObject dataRoot, FileObject wrkRoot) throws IOException {
	FileObject data = dataRoot.getFileObject(JTABLE_DATA);
	assertNotNull(data);
	FileObject srcRoot = getSrcRoot (wrkRoot);
	assertNotNull (srcRoot);
	FileObject pkg = FileUtil.createFolder(srcRoot,"javax/swing");	    //NOI18N
	FileObject src = pkg.createData("JTable.java");			    //NOI18N
	FileLock lock = src.lock ();
	try {
	    BufferedReader in = new BufferedReader ( new InputStreamReader (data.getInputStream()));
	    try {
		PrintWriter out = new PrintWriter ( new OutputStreamWriter (src.getOutputStream(lock)));	    
		try {
		    String line;
		    while ((line = in.readLine()) != null) {
			out.println(line);
		    }
		} finally {
		    out.close ();
		}
	    } finally {
		in.close ();
	    }
	} finally {
	    lock.releaseLock();
	}
	return src;
    }
    
    private static ClassPath createBootClassPath () throws IOException {
	String bcp = System.getProperty ("sun.boot.class.path");	//NOI18N
	assertNotNull (bcp);
	StringTokenizer tk = new StringTokenizer (bcp,File.pathSeparator);
	List<URL> roots = new ArrayList<URL>();
	while (tk.hasMoreTokens()) {
	    String token = tk.nextToken();
	    File f = new File (token);
	    URL url = f.toURI().toURL();
	    if (FileUtil.isArchiveFile(url)) {
		url = FileUtil.getArchiveRoot(url);
	    }
	    else if (!f.exists()) {
		url = new URL (url.toExternalForm()+'/');
	    }
	    roots.add (url);
	}
	return ClassPathSupport.createClassPath(roots.toArray(new URL[roots.size()]));
    }
    
    private static ClassPath createSourcePath (FileObject wrkRoot) throws IOException {
	return ClassPathSupport.createClassPath(new FileObject[] {getSrcRoot(wrkRoot)});
    }
    
    private class DummyClassPathProvider implements ClassPathProvider {
	
        public ClassPath findClassPath(FileObject file, String type) {
	    try {
		if (type == ClassPath.SOURCE) {
		    return createSourcePath (FileUtil.toFileObject(getWorkDir()));
		}
		else if (type == ClassPath.BOOT) {
		    return createBootClassPath ();
		}
	    } catch (IOException ioe) {
		//Skeep it
	    }
	    return ClassPathSupport.createClassPath (Collections.<PathResourceImplementation>emptyList());
        }			
    }
    
}
