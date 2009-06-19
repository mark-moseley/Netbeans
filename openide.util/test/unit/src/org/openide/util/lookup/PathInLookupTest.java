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

package org.openide.util.lookup;

import java.util.logging.Level;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.openide.util.NamedServicesProvider;
import org.openide.util.Lookup;

/** 
 * @author Jaroslav Tulach
 */
public class PathInLookupTest extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup.paths", "MyServices:YourServices");
        MockServices.setServices(P.class);
        Lookup.getDefault();
    }

    public PathInLookupTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    public void testInterfaceFoundInMyServices() throws Exception {
        assertNull("not found", Lookup.getDefault().lookup(Shared.class));
        Shared v = new Shared();
        P.ic1.add(v);
        assertNotNull("found", Lookup.getDefault().lookup(Shared.class));
        P.ic1.remove(v);
        assertNull("not found again", Lookup.getDefault().lookup(Shared.class));
    }
    public void testInterfaceFoundInMyServices2() throws Exception {
        assertNull("not found", Lookup.getDefault().lookup(Shared.class));
        Shared v = new Shared();
        P.ic2.add(v);
        assertNotNull("found", Lookup.getDefault().lookup(Shared.class));
        P.ic2.remove(v);
        assertNull("not found again", Lookup.getDefault().lookup(Shared.class));
    }

    static final class Shared extends Object {}

    public static final class P extends NamedServicesProvider {
        static InstanceContent ic1 = new InstanceContent();
        static InstanceContent ic2 = new InstanceContent();
        static AbstractLookup[] arr = {
            new AbstractLookup(ic1), new AbstractLookup(ic2)
        };


        @Override
        public Lookup create(String path) {
            int indx = -1;
            if (path.equals("MyServices/")) {
                indx = 0;
            }
            if (path.equals("YourServices/")) {
                indx = 1;
            }
            if (indx == -1) {
                fail("Unexpected lookup query: " + path);
            }
            return arr[indx];
        }
    }

}
