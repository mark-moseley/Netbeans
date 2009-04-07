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

package org.netbeans.modules.bugtracking.util;

import java.net.PasswordAuthentication;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.spi.UIUtils;

/**
 *
 * @author Tomas Stupka
 */
public class KenaiUtil {

    /**
     * Returns true if logged into kenai, otherwise false.
     *
     * @return
     */
    public static boolean isLoggedIn() {
        return Kenai.getDefault().getPasswordAuthentication() != null;
    }

    /**
     * Returns true if the given url belongs to a kenai project
     * 
     * @param url
     * @return
     */
    public static boolean isKenai(String url) {
        try {
            return KenaiProject.forRepository(url) != null;
        } catch (KenaiException ex) { }
        return false;
    }

    /**
     * Returns an instance of PasswordAuthentication holding the actuall
     * Kenai credentials.
     *
     * @param forceLogin - forces a login if user not logged in
     * @return PasswordAuthentication
     */
    public static PasswordAuthentication getPasswordAuthentication(boolean forceLogin) {
        PasswordAuthentication a = Kenai.getDefault().getPasswordAuthentication();
        if(a != null) {
            return a;
        } 
        
        if(!forceLogin) {
            return null;
        }

        if(!showLogin()) {
            return null;
        }
        
        return Kenai.getDefault().getPasswordAuthentication();
    }

    /**
     * Opens the kenai login dialog.
     * @return true if login successfull, otherwise false
     */
    public static boolean showLogin() {
        return UIUtils.showLogin();
    }
}
