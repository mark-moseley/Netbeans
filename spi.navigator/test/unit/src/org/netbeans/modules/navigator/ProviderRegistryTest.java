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

package org.netbeans.modules.navigator;

import java.util.Collection;
import javax.swing.JComponent;
import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.util.Lookup;


/**
 *
 * @author Dafe Simonek
 */
public class ProviderRegistryTest extends NbTestCase {

    /** test data type contants */
    private static final String MARVELOUS_DATA_TYPE_NAME = "MarvelousDataType";
    private static final String MARVELOUS_DATA_TYPE = "text/marvelous/data_type";
    
    /** Creates a new instance of ProviderRegistryTest */
    public ProviderRegistryTest() {
        super("");
    }
    
    public ProviderRegistryTest(String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTest suite() {
        NbTestSuite suite = new NbTestSuite(ProviderRegistryTest.class);
        return suite;
    }
    
    public void testGetProviders () throws Exception {
        UnitTestUtils.prepareTest(new String [] { "/org/netbeans/modules/navigator/resources/testGetProvidersLayer.xml" });
        
        ProviderRegistry providerReg = ProviderRegistry.getInstance();
        
        System.out.println("Asking for non-existent type...");
        assertEquals(0, providerReg.getProviders("image/non_existent_type").size());
        
        System.out.println("Asking for non-existent class...");
        assertEquals(0, providerReg.getProviders("text/plain").size());
        
        System.out.println("Asking for valid type and provider...");
        Collection<? extends NavigatorPanel> result = providerReg.getProviders(MARVELOUS_DATA_TYPE);
        assertEquals(1, result.size());
        NavigatorPanel np = result.iterator().next();
        assertTrue(np instanceof MarvelousDataTypeProvider);
        MarvelousDataTypeProvider provider = (MarvelousDataTypeProvider)np;
        assertEquals(MARVELOUS_DATA_TYPE_NAME, provider.getDisplayName());
    }
    

    /** Dummy navigator panel provider, just to test right loading and instantiating
     * for certain data type
     */ 
    public static final class MarvelousDataTypeProvider implements NavigatorPanel {
        
        public String getDisplayName () {
            return MARVELOUS_DATA_TYPE_NAME;
        }
    
        public String getDisplayHint () {
            return null;
        }

        public JComponent getComponent () {
            return null;
        }

        public void panelActivated (Lookup context) {
        }

        public void panelDeactivated () {
        }
        
        public Lookup getLookup () {
            return null;
        }
        
    }
    
    
}
