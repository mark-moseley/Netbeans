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

package org.netbeans.modules.apisupport.project.ui.platform;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.ui.ModuleUISettings;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.NotifyDescriptor;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 * Offers to upgrade old harnesses to the new version.
 * @author Jesse Glick
 * @see "issue #71630"
 */
class HarnessUpgrader {
    
    private HarnessUpgrader() {}
    
    public static void checkForUpgrade() {
        if (ModuleUISettings.getDefault().getHarnessesUpgraded()) {
            return;
        }
        ModuleUISettings.getDefault().setHarnessesUpgraded(true);
        final Set<NbPlatform> toUpgrade = new HashSet<NbPlatform>();
        for (NbPlatform p : NbPlatform.getPlatforms()) {
            if (p.isDefault() && !p.isValid()) {
                continue;
            }
            if (p.getHarnessVersion() >= NbPlatform.HARNESS_VERSION_50u1) {
                continue;
            }
            if (!p.getHarnessLocation().equals(p.getBundledHarnessLocation())) {
                // Somehow custom, forget it.
                continue;
            }
            toUpgrade.add(p);
        }
        if (!toUpgrade.isEmpty()) {
            Mutex.EVENT.readAccess(new Runnable() {
                public void run() {
                    promptForUpgrade(toUpgrade);
                }
            });
        }
    }
    
    private static void promptForUpgrade(Set<NbPlatform> platforms) {
        if (UIUtil.showAcceptCancelDialog(
                NbBundle.getMessage(HarnessUpgrader.class, "HarnessUpgrader.title"),
                NbBundle.getMessage(HarnessUpgrader.class, "HarnessUpgrader.text"),
                NbBundle.getMessage(HarnessUpgrader.class, "HarnessUpgrader.upgrade"),
                NbBundle.getMessage(HarnessUpgrader.class, "HarnessUpgrader.skip"),
                NotifyDescriptor.QUESTION_MESSAGE)) {
            try {
                doUpgrade(platforms);
            } catch (IOException e) {
                Util.err.notify(e);
            }
        }
    }
    
    private static void doUpgrade(Set<NbPlatform> platforms) throws IOException {
        File defaultHarness = NbPlatform.getDefaultPlatform().getHarnessLocation();
        Iterator it = platforms.iterator();
        while (it.hasNext()) {
            NbPlatform p = (NbPlatform) it.next();
            p.setHarnessLocation(defaultHarness);
        }
    }
    
}
