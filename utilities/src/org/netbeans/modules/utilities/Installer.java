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

package org.netbeans.modules.utilities;

import org.netbeans.modules.openfile.RecentFiles;
import org.openide.modules.ModuleInstall;
import org.openide.util.SharedClassObject;

/** Module install class for Utilities module.
 *
 * @author Jesse Glick, Petr Kuzel, Martin Ryzl
 */
public class Installer extends ModuleInstall {

    /** Installation instance for &quot;sub-module&quot; Search.  */
    private final org.netbeans.modules.search.Installer searchInstaller;

    /** Constructs modules installer. */
    public Installer() {
        searchInstaller = SharedClassObject.findObject(
                                  org.netbeans.modules.search.Installer.class,
                                  true);
    }
    
    /**
     * Restores module. Restores &quot;sub-module&quot; Search.
     */
    public void restored() {
        searchInstaller.restored();
        RecentFiles.init();
    }
    
    /**
     * Uninstalls module. Uninstalls
     * the Search &quot;sub-module&quot;.
     */
    public void uninstalled() {
        searchInstaller.uninstalled();
    }
    
    /**
     */
    public void close() {
        searchInstaller.close();
    }
    
    /**
     */
    public boolean closing() {
        return searchInstaller.closing();
    }

}
