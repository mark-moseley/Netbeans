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
package org.netbeans.modules.ide.ergonomics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.server.ServerRegistry;
import org.netbeans.spi.server.ServerWizardProvider;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Pavel Flaska
 */
public class AvailableJ2EEServerCheck extends NbTestCase {

    public AvailableJ2EEServerCheck(final String name) {
        super(name);
    }
    
    Comparator<ServerWizardProvider> comparator = new Comparator<ServerWizardProvider>() {

        public int compare(ServerWizardProvider arg0, ServerWizardProvider arg1) {
            return arg0.getDisplayName().compareTo(arg1.getDisplayName());
        }
    };

    public void testGetAllJ2eeServersReal() {
        int cnt = 0;
        List<ServerWizardProvider> providers = new ArrayList<ServerWizardProvider>(Lookups.forPath(ServerRegistry.SERVERS_PATH).lookupAll(ServerWizardProvider.class));
        for (ServerWizardProvider w : providers.toArray(new ServerWizardProvider[0])) {
            if (w.getInstantiatingIterator() == null) {
                providers.remove(w);
            }
        }
        Collections.sort(providers, comparator); // ?
        for (ServerWizardProvider wizard : providers.toArray(new ServerWizardProvider[0])) {
           System.setProperty("wizard." + ++cnt, wizard.getDisplayName());
           System.err.println("ergo: " + wizard.getDisplayName());
        }
    }

    public void testGetAllJ2eeServersErgo() {
        int cnt = 0;
        List<ServerWizardProvider> providers = new ArrayList<ServerWizardProvider>(Lookups.forPath(ServerRegistry.SERVERS_PATH).lookupAll(ServerWizardProvider.class));
        for (ServerWizardProvider w : providers) {
            if (w.getInstantiatingIterator() == null) {
                providers.remove(w);
            }
        }
        Collections.sort(providers, comparator);
        for (ServerWizardProvider wizard : providers) {
           String name = System.getProperty("wizard." + ++cnt);
           System.err.println("full: " + wizard.getDisplayName());
           assertEquals(name, wizard.getDisplayName());
        }
    }
}
