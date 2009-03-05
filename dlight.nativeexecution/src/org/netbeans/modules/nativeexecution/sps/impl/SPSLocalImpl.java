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
package org.netbeans.modules.nativeexecution.sps.impl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SignatureException;
import java.security.acl.NotOwnerException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.support.Encrypter;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.support.MacroExpanderFactory.MacroExpander;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

public final class SPSLocalImpl extends SPSCommonImpl {

    private static final Map<String, Long> csums = new HashMap<String, Long>();
    private final String privp;
    private String pid = null;


    static {
        csums.put("SunOS-x86", 2839716019L); // NOI18N
    }

    private SPSLocalImpl(ExecutionEnvironment execEnv, String privp) {
        super(execEnv);
        this.privp = privp;
    }

    public static SPSLocalImpl getNewInstance(ExecutionEnvironment execEnv)
            throws SignatureException, MissingResourceException {
        String privpCmd = null;

        MacroExpander macroExpander = MacroExpanderFactory.getExpander(execEnv);
        String path = "$osname-$platform";
        try {
            path = macroExpander.expandPredefinedMacros(path); // NOI18N
        } catch (ParseException ex) {
        }

        privpCmd = "bin/nativeexecution/" + path + "/privp"; // NOI18N
        InstalledFileLocator fl = InstalledFileLocator.getDefault();
        File file = fl.locate(privpCmd, null, false);

        if (file == null || !file.exists()) {
            throw new MissingResourceException(privpCmd, null, null);
        }

        privpCmd = file.getAbsolutePath();

        // Will not pass any password to unknown program...
        if (!Encrypter.checkCRC32(privpCmd, csums.get(path))) {
            throw new SignatureException("Wrong privp executable! CRC check failed!"); // NOI18N
        }

        Process p = null;

        try {
            // Set execution privileges ...
            p = new ProcessBuilder("/bin/chmod", "755", privpCmd).start(); // NOI18N
            p.waitFor();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return new SPSLocalImpl(execEnv, privpCmd);
    }

    public void requestPrivileges(List<String> requestedPrivileges, String user, char[] passwd) throws NotOwnerException {
        // Construct privileges list
        StringBuffer sb = new StringBuffer();

        for (String priv : requestedPrivileges) {
            sb.append(priv).append(","); // NOI18N
        }

        String requestedPrivs = sb.toString();

        Process p = null;

        try {
            p = new ProcessBuilder(privp, user, requestedPrivs, getPID()).start();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (p == null) {
            return;
        }

        PrintWriter w = new PrintWriter(p.getOutputStream());
        w.println(passwd);
        w.flush();

        int result = -1;

        try {
            result = p.waitFor();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (result != 0) {
            Logger.getInstance().severe("doRequestLocal failed! privp returned " + result); // NOI18N
            throw new NotOwnerException();
        }
    }

    synchronized String getPID() {
        if (pid != null) {
            return pid;
        }

        try {
            File self = new File("/proc/self"); // NOI18N
            pid = self.getCanonicalFile().getName();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return pid;
    }
}
