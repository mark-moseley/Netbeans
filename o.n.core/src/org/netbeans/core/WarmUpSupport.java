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

package org.netbeans.core;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.startup.StartLog;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.cookies.InstanceCookie;
import org.openide.util.RequestProcessor;

/**
 * This class controls "warm-up" initialization after IDE startup (some time
 * after main window is shown). It scans WarmUp folder for individual tasks
 * to be performed. The tasks should be instance objects implementing Runnable.
 *
 * The tasks may be provided by modules via xml layer.
 *
 * @author Tomas Pavek
 */

class WarmUpSupport implements Runnable {

    private static final String WARMUP_FOLDER = "WarmUp"; // NOI18N
    private static final int WARMUP_DELAY = 1500; // 1.5 sec after main window is shown
    
    static boolean finished = false;    // usefull for testability

    private Logger err = Logger.getLogger("org.netbeans.core.WarmUpSupport");

    static void warmUp() {
        RequestProcessor.getDefault().post(new WarmUpSupport(), WARMUP_DELAY);
    }

    // -------

    public void run() {
        boolean willLog = err.isLoggable(Level.FINE) || StartLog.willLog();
        if (willLog){
            err.fine("Warmup starting..."); // NOI18N
            StartLog.logStart("Warmup"); // NOI18N
        }

        FileObject fo = FileUtil.getConfigFile(WARMUP_FOLDER);
        DataObject[] warmObjects =
            fo != null ? DataFolder.findFolder(fo).getChildren() : new DataObject[0];

        if (willLog) {
            err.log(Level.FINE, "Found {0} warm up task(s)", warmObjects.length); // NOI18N
        }

        for (int i = 0; i < warmObjects.length; i++) {
            try {
                InstanceCookie ic = warmObjects[i].getCookie(InstanceCookie.class);
                Object warmer = ic.instanceCreate();

                if (warmer instanceof Runnable) {
                    ((Runnable) warmer).run();
                }
                if (willLog) {
                    StartLog.logProgress("Warmup task executed " + // NOI18N
                                         ic.instanceName());
                }
            }
            catch (Exception ex) {
                Logger.getLogger(WarmUpSupport.class.getName()).log(Level.WARNING, null, ex);
            }
        }
        if (willLog){
            err.fine("Warmup done."); // NOI18N
            StartLog.logEnd("Warmup"); // NOI18N
        }
        
        finished = true;
    }
}
