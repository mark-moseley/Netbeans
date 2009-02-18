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

package org.netbeans.modules.php.project.ui.actions.tests;

import java.util.Collection;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.api.PhpProjectUtils;
import org.netbeans.modules.php.project.spi.PhpUnitSupport;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.util.PhpUnit;
import org.netbeans.spi.gototest.TestLocator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Support for PHP Unit.
 * @author Tomas Mysik
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.gototest.TestLocator.class)
public class GoToTest implements TestLocator {
    private static final Logger LOGGER = Logger.getLogger(GoToTest.class.getName());

    public GoToTest() {
    }

    public boolean appliesTo(FileObject fo) {
        return CommandUtils.isPhpFile(fo);
    }

    public boolean asynchronous() {
        return false;
    }

    public void findOpposite(FileObject fo, int caretOffset, LocationListener callback) {
        throw new UnsupportedOperationException("GotoTest is synchronous");
    }

    public LocationResult findOpposite(FileObject fo, int caretOffset) {
        PhpProject project = findPhpProject(fo);
        if (project == null) {
            // XXX what to do now??
            LOGGER.info("PHP project was not found for file" + fo);
            return null;
        }

        if (CommandUtils.isUnderTests(project, fo, false)) {
            return findSource(project, fo);
        } else if (CommandUtils.isUnderSources(project, fo)) {
            return findTest(project, fo);
        }
        return null;
    }


    public FileType getFileType(FileObject fo) {
        PhpProject project = findPhpProject(fo);
        if (project == null) {
            LOGGER.info("PHP project was not found for file" + fo);
            return FileType.NEITHER;
        }

        if (CommandUtils.isUnderTests(project, fo, false)) {
            String name = fo.getNameExt();
            if (!name.equals(PhpUnit.TEST_FILE_SUFFIX) && name.endsWith(PhpUnit.TEST_FILE_SUFFIX)) {
                return FileType.TEST;
            }
        } else if (CommandUtils.isUnderSources(project, fo)) {
            return FileType.TESTED;
        }
        return FileType.NEITHER;
    }

    private LocationResult findSource(PhpProject project, FileObject testFo) {
        FileObject sources = getSources(project);
        assert sources != null : "Project sources must be found";
        PhpUnitSupport unitSupport = Lookup.getDefault().lookup(PhpUnitSupport.class);
        assert unitSupport != null : "PhpUnitSupport must be found in default lookup";
        Collection<? extends String> classNames = unitSupport.getClassNames(testFo);
        for (String clsName : classNames) {
            if (clsName.endsWith(PhpUnit.TEST_CLASS_SUFFIX)) {
                int lastIndexOf = clsName.lastIndexOf(PhpUnit.TEST_CLASS_SUFFIX);
                assert lastIndexOf != -1;
                String srcClassName = clsName.substring(0, lastIndexOf);
                Collection<? extends FileObject> files = unitSupport.filesForClassName(testFo, srcClassName);
                for (FileObject fileObject : files) {
                    if (CommandUtils.isPhpFile(fileObject)
                            && FileUtil.isParentOf(sources, fileObject)) {
                        return new LocationResult(fileObject, -1);
                    }
                }
            }
        }
        return new LocationResult(NbBundle.getMessage(GoToTest.class, "MSG_SrcNotFound", testFo.getName()));
    }

    public static LocationResult findTest(PhpProject project, FileObject srcFo) {
        FileObject tests = getTests(project);
        if (tests != null) {
            PhpUnitSupport unitSupport = Lookup.getDefault().lookup(PhpUnitSupport.class);
            assert unitSupport != null : "PhpUnitSupport must be found in default lookup";
            Collection<? extends String> classNames = unitSupport.getClassNames(srcFo);
            for (String clsName : classNames) {
                String testClsName = clsName + PhpUnit.TEST_CLASS_SUFFIX;
                Collection<? extends FileObject> files = unitSupport.filesForClassName(srcFo, testClsName);
                for (FileObject fileObject : files) {
                    if (CommandUtils.isPhpFile(fileObject)
                            && FileUtil.isParentOf(tests, fileObject)) {
                        return new LocationResult(fileObject, -1);
                    }
                }
            }
        }
        return new LocationResult(NbBundle.getMessage(GoToTest.class, "MSG_TestNotFound", srcFo.getName()));
    }

    private PhpProject findPhpProject(FileObject fo) {
        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null
                || !PhpProjectUtils.isPhpProject(project)) {
            return null;
        }
        return (PhpProject) project;
    }

    public static FileObject getSources(PhpProject project) {
        return ProjectPropertiesSupport.getSourcesDirectory(project);
    }

    public static FileObject getTests(PhpProject project) {
        return ProjectPropertiesSupport.getTestDirectory(project, false);
    }
}
