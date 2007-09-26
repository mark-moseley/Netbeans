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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.api.autoupdate;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.netbeans.ModuleManager;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.core.startup.Main;
import org.netbeans.spi.autoupdate.CustomInstaller;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateLicense;
import org.netbeans.spi.autoupdate.UpdateProvider;
/**
 *
 * @author Radek Matous, Jirka Rechtacek
 */
public class TestUtils {
    
    private static UpdateItem item = null;
    private static ModuleManager mgr = null;
        
    public static void setUserDir(String path) {    
        System.setProperty ("netbeans.user", path);
    }
    
    /** Returns the platform installatiion directory.
     * @return the File directory.
     */
    public static File getPlatformDir () {
        return new File (System.getProperty ("netbeans.home")); // NOI18N
    }
    
    public static void setPlatformDir (String path) {    
        System.setProperty ("netbeans.home", path);
    }
        
    public static void testInit() {
        mgr = Main.getModuleSystem().getManager();
        assert mgr != null;
    }
    
    public static class CustomItemsProvider implements UpdateProvider {
        public String getName() {
            return "items-with-custom-installer";
        }

        public String getDisplayName() {
            return "Provides item with own custom installer";
        }

        public String getDescription () {
            return null;
        }

        public Map<String, UpdateItem> getUpdateItems() {
            return Collections.singletonMap ("hello-installer", getUpdateItemWithCustomInstaller ());
        }

        public boolean refresh(boolean force) {
            return true;
        }

        public CATEGORY getCategory() {
            return CATEGORY.COMMUNITY;
        }
    }
    
    private static CustomInstaller customInstaller = new CustomInstaller () {
        public boolean install (String codeName, String specificationVersion, ProgressHandle handle) throws OperationException {
            assert false : "Don't call unset installer";
            return false;
        }
    };
    
    
    public static void setCustomInstaller (CustomInstaller installer) {
        customInstaller = installer;
    }

    public static UpdateItem getUpdateItemWithCustomInstaller () {
        if (item != null) return item;
        String codeName = "hello-installer";
        String specificationVersion = "0.1";
        String displayName = "Hello Component";
        String description = "Hello I'm a component with own installer";
        URL distribution = null;
        try {
            distribution = new URL ("nbresloc:/org/netbeans/api/autoupdate/data/org-yourorghere-engine-1-1.nbm");
            //distribution = new URL ("nbresloc:/org/netbeans/api/autoupdate/data/executable-jar.jar");
        } catch (MalformedURLException ex) {
            assert false : ex;
        }
        String author = "Jiri Rechtacek";
        String downloadSize = "2815";
        String homepage = "http://netbeans.de";
        Manifest manifest = new Manifest ();
        Attributes mfAttrs = manifest.getMainAttributes ();
        CustomInstaller ci = createCustomInstaller ();
        assert ci != null;
        UpdateLicense license = UpdateLicense.createUpdateLicense ("none-license", "no-license");
        item = UpdateItem.createNativeComponent (
                                                    codeName,
                                                    specificationVersion,
                                                    downloadSize,
                                                    null, // dependencies
                                                    displayName,
                                                    description,
                                                    false, false, "my-cluster",
                                                    ci,
                                                    license);
        return item;
    }
    
    private static CustomInstaller createCustomInstaller () {
        return new CustomInstaller () {
            public boolean install (String codeName, String specificationVersion, ProgressHandle handle) throws OperationException {
                assert item != null;
                return customInstaller.install (codeName, specificationVersion, handle);
            }
        };
    }
}
