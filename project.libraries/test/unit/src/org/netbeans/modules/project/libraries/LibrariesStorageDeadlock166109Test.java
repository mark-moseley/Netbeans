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

package org.netbeans.modules.project.libraries;

import java.beans.Customizer;
import java.util.logging.Logger;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.project.libraries.LibraryManagerTest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.libraries.LibrariesStorageTest.TestEntityCatalog;
import org.netbeans.modules.project.libraries.LibrariesStorageTest.TestLibraryTypeProvider;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex.Action;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class LibrariesStorageDeadlock166109Test extends NbTestCase {
    static final Logger LOG = Logger.getLogger(LibrariesStorageDeadlock166109Test.class.getName());
    private FileObject storageFolder;

    public LibrariesStorageDeadlock166109Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        MockLookup.setInstances(new TestEntityCatalog());
        storageFolder = FileUtil.getConfigFile("org-netbeans-api-project-libraries/Libraries");
        assertNotNull("storageFolder found", storageFolder);
    }

    public void testDeadlock() throws Exception {
        Library[] arr = LibraryManager.getDefault().getLibraries();
        assertEquals("Empty", 0, arr.length);

        LibrariesStorageTest.createLibraryDefinition(storageFolder,"Library1");

        Library[] arr0 = LibraryManager.getDefault().getLibraries();
        assertEquals("Still Empty", 0, arr0.length);

        LibrariesStorageTest.registerLibraryTypeProvider(TestMutexLibraryTypeProvider.class);

        Thread.sleep(100);

        // TBD: There is another problem in the code. When a provider is added,
        // but it is not yet processed, the getLibraries() method uses cache and
        // thus can yield wrong results. To workaround that (and simulate the
        // deadlock) here is direct call to reset the cache.
        // Ideally it shall not be necessary for arr1 to have length 1
        LibraryManagerTest.resetCache();
        Library[] arr1 = LibraryManager.getDefault().getLibraries();
        assertEquals("One", 1, arr1.length);
    }

    public static final class TestMutexLibraryTypeProvider extends TestLibraryTypeProvider {
        public TestMutexLibraryTypeProvider() {
            LOG.info("TestMutexLibraryTypeProvider created");
        }

        @Override
        public LibraryImplementation createLibrary() {
            assertFalse("No Hold lock", Thread.holdsLock(LibraryManager.getDefault()));
            assertFalse("No mutex", ProjectManager.mutex().isReadAccess());
            assertFalse("No mutex write", ProjectManager.mutex().isWriteAccess());
            try {
                LibrariesStorageTest.registerLibraryTypeProvider();
                Thread.sleep(500);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            return ProjectManager.mutex().writeAccess(new Action<LibraryImplementation>() {
                public LibraryImplementation run() {
                    return TestMutexLibraryTypeProvider.super.createLibrary();
                }
            });
        }

    }


}
