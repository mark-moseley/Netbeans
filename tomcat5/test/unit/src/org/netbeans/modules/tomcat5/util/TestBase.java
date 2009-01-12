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

package org.netbeans.modules.tomcat5.util;

import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Common ancestor for all test classes.
 *
 * @author sherold
 */
public class TestBase extends NbTestCase {

    static {
        // set the lookup which will be returned by Lookup.getDefault()
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
        assertEquals("Unable to set the default lookup!", Lkp.class, Lookup.getDefault().getClass());
        
        ((Lkp)Lookup.getDefault()).setRepository(new RepositoryImpl());
        assertEquals("The default Repository is not our repository!", RepositoryImpl.class, Repository.getDefault().getClass());
    }
    
    public TestBase(String name) {
        super(name);
    }
    
    public static final class Lkp extends ProxyLookup {
        public Lkp() {
            setProxyLookups(new Lookup[0]);
        }
        
        private void setProxyLookups(Lookup[] lookups) {
            Lookup[] allLookups = new Lookup[lookups.length + 2];
            ClassLoader l = TestBase.class.getClassLoader();
            allLookups[allLookups.length - 1] = Lookups.metaInfServices(l);
            allLookups[allLookups.length - 2] = Lookups.singleton(l);
            System.arraycopy(lookups, 0, allLookups, 0, lookups.length);
            setLookups(allLookups);
        }
        
        private void setRepository(Repository repository) {
            // must set out repository first
            setProxyLookups(new Lookup[] {
                Lookups.singleton(repository),
            });
            
            DataFolder services = DataFolder.findFolder(FileUtil.getConfigFile("Services"));
            FolderLookup lookup = new FolderLookup(services);
            setProxyLookups(new Lookup[] {
                Lookups.singleton(repository),
                new FolderLookup(services).getLookup()
            });
        }
    }
}
