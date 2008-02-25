/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.mysql.installations;

import org.netbeans.modules.db.mysql.Installation;
import org.openide.util.Utilities;

/**
 * Defines the AMP stack distribution called "MAMP" for Mac
 * See <a href="http://sourceforge.net/projects/mamp">
 * http://sourceforge.net/projects/mamp</a>
 * 
 * @author David Van Couvering
 */
public class MAMPInstallation extends AbstractInstallation {
    private static final String DEFAULT_BASE_PATH = "/Applications/MAMP"; // NOI8N
    private static final String START_PATH = "/bin/startMysql.sh"; // NOI18N
    private static final String STOP_PATH = "/bin/stopMysql.sh"; // NOI18N
    private static final String DEFAULT_PORT = "8889";
    
    private String basePath = DEFAULT_BASE_PATH;
    
    private static final MAMPInstallation DEFAULT = 
            new MAMPInstallation(DEFAULT_BASE_PATH);
    
    public static final MAMPInstallation getDefault() {
        return DEFAULT;
    }
    
    private MAMPInstallation(String basePath) {
        this.basePath = basePath;
    }

    public boolean isStackInstall() {
        return true;
    }

    public boolean isValidOnCurrentOS() {
        return Utilities.isMac();
    }

    public String[] getAdminCommand() {
        return new String[] {
            "http://localhost:8888/MAMP/frame.php?src=%2FphpMyAdmin%2F%3F",
            ""
        };
    }

    public String[] getStartCommand() {
        String command = basePath + START_PATH;
        return new String[] { command, "" };
    }

    public String[] getStopCommand() {
        String command = basePath + STOP_PATH;
        return new String[] { command, "" };
    }
    
    public String getDefaultPort() {
        return DEFAULT_PORT;
    }

    @Override
    protected String getBasePath() {
        return basePath;
    }

    @Override
    protected String getStartPath() {
        return START_PATH;
    }

    @Override
    protected String getStopPath() {
        return STOP_PATH;
    }

    @Override
    protected Installation createInstallation(String basePath) {
        return new MAMPInstallation(basePath);
    }

}
