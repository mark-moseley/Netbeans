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

package org.netbeans.api.editor.mimelookup;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.mimelookup.EditorTestLookup;
import org.netbeans.modules.editor.mimelookup.TestUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author Martin Roskanin, Vita Stejskal
 */
public class MimeLookupMemoryTest extends NbTestCase {

    public MimeLookupMemoryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
        // Set up the default lookup, repository, etc.
        EditorTestLookup.setLookup(
            new String[] {
                "Services/org-netbeans-modules-editor-mimelookup-DummyMimeDataProvider.instance"
            },
            getWorkDir(),
            new Object[] {},
            getClass().getClassLoader()
        );
    }
    
    public void testSimple1() {
        MimePath path = MimePath.get("text/x-java");
        Lookup lookupA = MimeLookup.getLookup(path);
        Lookup lookupB = MimeLookup.getLookup(path);
        assertSame("Lookups are not reused", lookupA, lookupB);
    }

    public void testSimple2() {
        MimePath path = MimePath.get("text/x-java");
        int idA = System.identityHashCode(MimeLookup.getLookup(path));
        
        TestUtilities.consumeAllMemory();
        TestUtilities.gc();
        
        int idB = System.identityHashCode(MimeLookup.getLookup(path));
        assertEquals("Lookup instance was lost", idA, idB);
    }

    public void testSimple3() {
        int idA = System.identityHashCode(MimeLookup.getLookup(MimePath.get("text/x-java")));
        
        TestUtilities.consumeAllMemory();
        TestUtilities.gc();
        
        int idB = System.identityHashCode(MimeLookup.getLookup(MimePath.get("text/x-java")));
        assertEquals("Lookup instance was lost", idA, idB);
    }
    
    public void testLookupsRelease() {
        int idA = System.identityHashCode(MimeLookup.getLookup(MimePath.get("text/x-java")));
        
        TestUtilities.consumeAllMemory();
        TestUtilities.gc();
        
        int idB = System.identityHashCode(MimeLookup.getLookup(MimePath.get("text/x-java")));
        assertEquals("Lookup instance was lost", idA, idB);
        
        // Force the MimePath instance to be dropped from the list of recently used
        for (int i = 0; i < MimePath.MAX_LRU_SIZE; i++) {
            MimePath.get("text/x-nonsense-" + i);
        }
        
        TestUtilities.consumeAllMemory();
        TestUtilities.gc();
        
        int idC = System.identityHashCode(MimeLookup.getLookup(MimePath.get("text/x-java")));
        assertTrue("Lookup instance was not released", idA != idC);
    }

    public void testLookupResultHoldsTheLookup() {
        MimePath path = MimePath.get("text/x-java");
        Lookup lookup = MimeLookup.getLookup(path);
        Lookup.Result lr = lookup.lookupResult(Object.class);
        
        int pathIdA = System.identityHashCode(path);
        int lookupIdA = System.identityHashCode(lookup);
        
        path = null;
        lookup = null;
        
        // Force the MimePath instance to be dropped from the list of recently used
        for (int i = 0; i < MimePath.MAX_LRU_SIZE; i++) {
            MimePath.get("text/x-nonsense-" + i);
        }
        
        TestUtilities.consumeAllMemory();
        TestUtilities.gc();

        int pathIdB = System.identityHashCode(MimePath.get("text/x-java"));
        int lookupIdB = System.identityHashCode(MimeLookup.getLookup(MimePath.get("text/x-java")));
        
        assertEquals("MimePath instance was lost", pathIdA, pathIdB);
        assertEquals("Lookup instance was lost", lookupIdA, lookupIdB);
    }
}
