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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.gotodeclaration.element.providers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.junit.Manager;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.gotodeclaration.element.providers.BaseProvider.ProviderDelegate;
import org.netbeans.modules.cnd.gotodeclaration.element.spi.ElementDescriptor;
import org.netbeans.modules.cnd.modelimpl.test.ProjectBasedTestCase;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import org.netbeans.spi.jumpto.type.SearchType;

/**
 *
 * @author Nick Krasilnikov
 */
public class MacroElementProviderTestCase extends ProjectBasedTestCase {

    public MacroElementProviderTestCase(String testName) {
        super(testName);
    }

    @Override
    protected File getTestCaseDataDir() {
        return getQuoteDataDir();
    }

     public void testMacroAllRegexp() throws Exception {
        peformTest(".*", SearchType.REGEXP);
    }

    public void testMacroDotRegexp() throws Exception {
        peformTest("CPU.H", SearchType.REGEXP);
    }

    public void testMacroCaseInsensitiveRegexp() throws Exception {
        peformTest("_cUs.*r_h", SearchType.CASE_INSENSITIVE_REGEXP);
    }
    
    public void testMacroCamelCase() throws Exception {
        peformTest("DH", SearchType.CAMEL_CASE);
    }

    public void testMacroPrefix() throws Exception {
        peformTest("M", SearchType.PREFIX);
    }

    public void testMacroCaseInsensitivePrefix() throws Exception {
        peformTest("m", SearchType.CASE_INSENSITIVE_PREFIX);
    }

    public void testMacroExactName() throws Exception {
        peformTest("DISK_H", SearchType.EXACT_NAME);
    }

    public void testMacroCaseInsensitiveExactName() throws Exception {
        peformTest("disk_h", SearchType.CASE_INSENSITIVE_EXACT_NAME);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    protected final File getQuoteDataDir() {
        String dataPath = getDataDir().getAbsolutePath().replaceAll("cnd.gotodeclaration", "cnd.modelimpl"); // NOI18N
        String filePath = "common/quote_nosyshdr"; // NOI18N
        return Manager.normalizeFile(new File(dataPath, filePath));
    }

    protected void peformTest(String text, SearchType type) throws Exception {
        MacroProvider fvp = new MacroProvider();
        ProviderDelegate pd = fvp.createDelegate();

        CsmProject project = getProject();
        assertNotNull(project);

        Collection elems = pd.getElements(project, text, type, true);
        assertNotNull(elems);

        List<ElementDescriptor> items = new ArrayList<ElementDescriptor>();
        items.addAll(elems);

        Collections.sort(items, new TypeComparator());

        for (ElementDescriptor elementDescriptor : items) {
            ref(elementDescriptor.getProjectName() + " " + elementDescriptor.getDisplayName()); // NOI18N
        }

        File output = new File(getWorkDir(), getName() + ".ref"); // NOI18N
        File goldenDataFile = getGoldenFile(getName() + ".ref");


        if (!goldenDataFile.exists()) {
            fail("No golden file " + goldenDataFile.getAbsolutePath() + "\n to check with output file " + output.getAbsolutePath()); // NOI18N
        }
        if (CndCoreTestUtils.diff(output, goldenDataFile, null)) {
            // copy golden
            File goldenCopyFile = new File(getWorkDir(), getName() + ".ref.golden"); // NOI18N
            CndCoreTestUtils.copyToWorkDir(goldenDataFile, goldenCopyFile); // NOI18N
            fail("OUTPUT Difference between diff " + output + " " + goldenCopyFile); // NOI18N
        }
    }

    private class TypeComparator implements Comparator<ElementDescriptor> {

        public int compare(ElementDescriptor t1, ElementDescriptor t2) {
            int result = compareStrings(t1.getDisplayName(), t2.getDisplayName());
            if (result == 0) {
                result = compareStrings(t1.getContextName(), t2.getContextName());
                if (result == 0) {
                    result = compareStrings(t1.getProjectName(), t2.getProjectName());
                    if (result == 0) {
                        result = compareStrings(t1.getAbsoluteFileName(), t2.getAbsoluteFileName());
                    }
                }
            }
            return result;
        }
    }

    private int compareStrings(String s1, String s2) {
        if (s1 == null) {
            s1 = ""; // NOI18N
        }
        if (s2 == null) {
            s2 = ""; // NOI18N
        }
        return s1.compareTo(s2);
    }
}
