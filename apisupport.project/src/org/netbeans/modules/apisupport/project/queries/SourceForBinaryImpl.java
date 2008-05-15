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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.universe.TestEntry;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Element;

/**
 * Provides sources for module build products.
 * XXX can we also translate xtest/lib/nbjunit.jar -> xtest/nbjunit/src/?
 */
public final class SourceForBinaryImpl implements SourceForBinaryQueryImplementation {
    
    private final NbModuleProject project;
    private URL classesUrl;
    private URL testClassesUrl;
    private Map<URL,SourceForBinaryQuery.Result> cache = new HashMap<URL,SourceForBinaryQuery.Result>();
    
    public SourceForBinaryImpl(NbModuleProject project) {
        this.project = project;
    }
    
    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        //System.err.println("findSourceRoot: " + binaryRoot);
        SourceForBinaryQuery.Result res = cache.get(binaryRoot);
        if (res == null) {
            URL binaryJar = FileUtil.getArchiveFile(binaryRoot);
            if (binaryJar != null) {
                File binaryJarF = new File(URI.create(binaryJar.toExternalForm()));
                FileObject srcDir = null;
                if (binaryJarF.getAbsolutePath().endsWith(project.evaluator().getProperty("module.jar").replace('/', File.separatorChar))) {
                    srcDir = project.getSourceDirectory();
                } else {
                    // maybe tests.jar in testdistribution
                    TestEntry entry = TestEntry.get(binaryJarF);
                    if (entry != null && project.getCodeNameBase().equals(entry.getCodeNameBase())) {
                        srcDir = ( entry.isUnit() ) ? project.getTestSourceDirectory() : 
                               project.getFunctionalTestSourceDirectory();
                    }
                }
                if (srcDir != null) {
                    res = new Result(srcDir);
                    return res;
                }
            }
            if (binaryRoot.equals(getClassesUrl())) {
                FileObject srcDir = project.getSourceDirectory();
                if (srcDir != null) {
                    res = new Result(srcDir);
                }
            } else if (binaryRoot.equals(getTestClassesUrl())) {
                FileObject testSrcDir = project.getTestSourceDirectory();
                if (testSrcDir != null) {
                    res = new Result(testSrcDir);
                }
            } else {
                // Check extra compilation units.
                ECUS: for (Map.Entry<FileObject,Element> entry : project.getExtraCompilationUnits().entrySet()) {
                    for (Element kid : Util.findSubElements(entry.getValue())) {
                        if (!kid.getLocalName().equals("built-to")) { // NOI18N
                            continue;
                        }
                        String rawtext = Util.findText(kid);
                        assert rawtext != null : "Null content for <built-to> in " + project;
                        String text = project.evaluator().evaluate(rawtext);
                        if (text == null) {
                            continue;
                        }
                        File loc = project.getHelper().resolveFile(text);
                        URL u = FileUtil.urlForArchiveOrDir(loc);
                        if (u == null) { // #135163
                            Logger.getLogger(SourceForBinaryImpl.class.getName()).log(Level.WARNING,
                                    "In " + FileUtil.getFileDisplayName(project.getProjectDirectory()) +
                                    " " + loc + " is neither a directory nor a JAR");
                        } else if (u.equals(binaryRoot)) {
                            res = new Result(entry.getKey());
                            break ECUS;
                        }
                    }
                }
            }
            if (res != null) {
                cache.put(binaryRoot,res);
            }
        }
        return res;
    }
    
    private URL getClassesUrl() {
        if (classesUrl == null) {
            File classesDir = project.getClassesDirectory();
            classesUrl = FileUtil.urlForArchiveOrDir(classesDir);
        }
        return classesUrl;
    }
    
    private URL getTestClassesUrl() {
        if (testClassesUrl == null && project.supportsUnitTests()) {
            File testClassesDir = project.getTestClassesDirectory();
            testClassesUrl = FileUtil.urlForArchiveOrDir(testClassesDir);
        }
        return testClassesUrl;
    }
    
    
    private static class Result implements SourceForBinaryQuery.Result {
               
        private FileObject res;
        
        public Result(FileObject res) {
            assert res != null;
            this.res = res;
        }
        
        public FileObject[] getRoots () {
            return new FileObject[] {res};
        }
        
        public void addChangeListener (ChangeListener l) {
            //Not needed, do not suppose the source root to be changed in nbproject
        }
        
        public void removeChangeListener (ChangeListener l) {
            //Not needed, do not suppose the source root to be changed in nbproject
        }
        
    }
    
}
