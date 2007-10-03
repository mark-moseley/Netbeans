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

package org.netbeans.modules.derby;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.derby.api.DerbyDatabases;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 * @author Andrei Badea
 */
public class Installer extends ModuleInstall {

    private static final Logger LOGGER = Logger.getLogger(Installer.class.getName());

    public void restored() {
        WindowManager.getDefault().invokeWhenUIReady(new RegisterJDKDerby());
    }

    private static final class RegisterJDKDerby implements Runnable {

        private final JDKDerbyHelper helper = JDKDerbyHelper.forDefaultPlatform();

        public void run() {
            if (SwingUtilities.isEventDispatchThread()) {
                RequestProcessor.getDefault().post(this);
                return;
            }

            if (!helper.canBundleDerby()) {
                LOGGER.fine("Default platform cannot bundle Derby"); // NOI18N
                return;
            }

            ProgressHandle handle = ProgressHandleFactory.createSystemHandle(NbBundle.getMessage(Installer.class, "MSG_RegisterJavaDB"));
            handle.start();
            try {
                if (registerJDKDerby()) {
                    registerSampleDatabase();
                }
            } finally {
                handle.finish();
            }
        }

        private boolean registerJDKDerby() {
            if (DerbyOptions.getDefault().getLocation().length() > 0) {
                return false;
            }
            String derbyLocation = helper.findDerbyLocation();
            if (derbyLocation != null) {
                LOGGER.log(Level.FINE, "Registering JDK Derby at {0}", derbyLocation); // NOI18N
                return DerbyOptions.getDefault().trySetLocation(derbyLocation);
            }
            return false;
        }

        private void registerSampleDatabase() {
            try {
                DerbyDatabases.createSampleDatabase();
            } catch (DatabaseException e) {
                Logger.getLogger(Installer.class.getName()).log(Level.WARNING, null, e);
            } catch (IOException e) {
                Logger.getLogger(Installer.class.getName()).log(Level.WARNING, null, e);
            }
        }
    }
}
