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

package org.netbeans.modules.db.mysql.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.mysql.installations.BundledInstallation;
import org.openide.util.lookup.Lookups;

/**
 * Supporting methods to work with the registered implementations of Installation
 * 
 * @author David Van Couvering
 */
public class InstallationManager {    
    private static Logger LOGGER = 
            Logger.getLogger(InstallationManager.class.getName());
    
    private static ArrayList<Installation> INSTALLATIONS = null;
    private static volatile boolean isInstalled = false;
    
    private static final String INSTALLATION_PROVIDER_PATH = 
            "Databases/MySQL/Installations"; // NOI18N


    public static List<Installation> getInstallations() {
        if (!isInstalled) {
            // First see if we're bundled with MySQL.  If so, just return
            // the bundled installation
            Installation bundled = BundledInstallation.getDefault();
            if (bundled.isInstalled()) {
                ArrayList<Installation> bundledList = new ArrayList<Installation>();
                bundledList.add(bundled);
                return bundledList;
            }

            Collection loadedInstallations = 
                    Lookups.forPath(INSTALLATION_PROVIDER_PATH)
                        .lookupAll(Installation.class);

            // Now order them so that the stack-based installations come first.
            // See the javadoc for Installation.isStackInstall() for the reasoning 
            // behind this.
            ArrayList<Installation> stackInstalls = new ArrayList<Installation>();
            ArrayList<Installation> stdInstalls = new ArrayList<Installation>();

            for ( Iterator it = loadedInstallations.iterator() ; it.hasNext() ; ) {
                Installation installation = (Installation)it.next();

                if ( installation.isStackInstall() ) {
                    stackInstalls.add(installation);                
                } else {
                    stdInstalls.add(installation);
                }
            }

            INSTALLATIONS = new ArrayList<Installation>();
            INSTALLATIONS.addAll(stackInstalls);
            INSTALLATIONS.addAll(stdInstalls);
            isInstalled = true;
        }
        
        return INSTALLATIONS;
    }
    
    /**
     * See if we can detect the paths to the various admin tools
     * 
     * @return a valid installation if detected, null otherwise.  Returns the
     *      first installation found, so if there are multiple installations
     *      the other ones available will not be detected.
     */
    public static Installation detectInstallation() {
        List<Installation> installationCopy = new CopyOnWriteArrayList<Installation>();
        installationCopy.addAll(InstallationManager.getInstallations());
        
        for ( Iterator it = installationCopy.iterator() ; it.hasNext() ; ) {
            Installation installation = (Installation)it.next();
            
            LOGGER.log(Level.FINE, "Looking for MySQL installation " + 
                    installation.getStartCommand()[0] + 
                    installation.getStartCommand()[1]);
            
            if ( installation.isInstalled() ) {
                LOGGER.log(Level.FINE, "Installation is installed");
                return installation;
            }
        }
        
        return null;
    }        
}
