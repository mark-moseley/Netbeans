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

package org.netbeans.api.project.libraries;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryProvider;
import org.openide.util.lookup.Lookups;
/**
 *
 * @author  Tomas Zezula
 */
public class LibraryTest extends NbTestCase {

    private LibraryManagerTest.TestLibraryProvider lp;

    /** Creates a new instance of LibraryManagerTest */
    public LibraryTest (String testName) {
        super (testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        lp = new LibraryManagerTest.TestLibraryProvider ();
        TestUtil.setLookup (Lookups.fixed(new Object[] {lp}));
    }
    
    public void testGetLibraries () throws Exception {        
        LibraryManager lm = LibraryManager.getDefault();
        Library[] libs = lm.getLibraries();
        LibraryImplementation[] impls = LibraryManagerTest.createTestLibs ();
        lp.setLibraries(impls);        
        libs = lm.getLibraries();
        assertEquals ("Libraries count", 2, libs.length);
        LibraryManagerTest.assertLibsEquals (libs, impls);
        LibraryManagerTest.TestListener tl = new LibraryManagerTest.TestListener ();
        libs[0].addPropertyChangeListener(tl);
        impls[0].setName("NewLibrary1");
        LibraryManagerTest.assertEventsEquals(tl.getEventNames(), new String[] {Library.PROP_NAME});
        tl.reset();
        LibraryManagerTest.assertLibsEquals (new Library[] {libs[0]}, new LibraryImplementation[] {impls[0]});
        
        impls[0].setDescription("NewLibrary1Description");
        LibraryManagerTest.assertEventsEquals(tl.getEventNames(), new String[] {Library.PROP_DESCRIPTION});
        tl.reset();
        LibraryManagerTest.assertLibsEquals (new Library[] {libs[0]}, new LibraryImplementation[] {impls[0]});
        List<URL> urls = new ArrayList<URL>();
        urls.add (new URL ("file:/lib/libnew1.so"));
        urls.add (new URL ("file:/lib/libnew2.so"));
        impls[0].setContent ("bin",urls);        
        LibraryManagerTest.assertEventsEquals(tl.getEventNames(), new String[] {Library.PROP_CONTENT});
        tl.reset();
        LibraryManagerTest.assertLibsEquals (new Library[] {libs[0]}, new LibraryImplementation[] {impls[0]});
        urls = new ArrayList<URL>();
        urls.add (new URL ("file:/src/new/src/"));
        impls[0].setContent ("src",urls);        
        LibraryManagerTest.assertEventsEquals(tl.getEventNames(), new String[] {Library.PROP_CONTENT});
        tl.reset();
        LibraryManagerTest.assertLibsEquals (new Library[] {libs[0]}, new LibraryImplementation[] {impls[0]});
    }    
    
}
