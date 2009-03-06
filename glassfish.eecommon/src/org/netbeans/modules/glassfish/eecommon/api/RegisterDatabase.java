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

package org.netbeans.modules.glassfish.eecommon.api;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.derby.api.DerbyDatabases;
import org.netbeans.modules.derby.spi.support.DerbySupport;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Nitya Doraisamy
 */
public class RegisterDatabase {

    private static RegisterDatabase reg = null;
    
    public static RegisterDatabase getDefault(){
        if (reg == null) {
            reg = new RegisterDatabase();
        }
        return reg;
    }

    public void setupDerby(String serverinstall) {
        String location = DerbySupport.getLocation();
        if (null != location && location.trim().length() > 0) {
            return;
        }
        File dbloc = new File(serverinstall, "javadb"); //NOI18N
        if (dbloc.exists() && dbloc.isDirectory() && dbloc.canRead()) {
            DerbySupport.setLocation(dbloc.getAbsolutePath());
            location = DerbySupport.getSystemHome();
            if (null != location && location.trim().length() > 0) {
                return;
            }else{
                File dbdir = new File(DerbySupport.getDefaultSystemHome());
                if (dbdir.exists() == false) {
                    dbdir.mkdirs();
                }
            }
            DerbySupport.setSystemHome(DerbySupport.getDefaultSystemHome());
        }
        configureDatabase();
    }

    public void configureDatabase(){
        String location = DerbySupport.getLocation();
        File dbInstall = new File(location);
        if (dbInstall != null && dbInstall.exists()){
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        DerbyDatabases.createSampleDatabase();
                    } catch (DatabaseException ex) {
                        Logger.getLogger("glassfish-javaee").log(Level.INFO, ex.getLocalizedMessage(), ex);
                    } catch (IOException ex) {
                        Logger.getLogger("glassfish-javaee").log(Level.INFO, ex.getLocalizedMessage(), ex);
                    }
                }
            });
        }
    }
    
}
