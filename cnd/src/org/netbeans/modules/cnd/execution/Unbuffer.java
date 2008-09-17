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

package org.netbeans.modules.cnd.execution;

import java.io.File;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.PlatformTypes;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author eu155513
 */
public class Unbuffer {
    protected static final Logger log = Logger.getLogger("cnd.execution.logger"); // NOI18N

    private Unbuffer() {
    }

    public static String getPath(String hkey) {
        if (hkey == null || CompilerSetManager.LOCALHOST.equals(hkey)) {
            return Unbuffer.getLocalPath();
        } else {
            return Unbuffer.getRemotePath(hkey);
        }
    }
    
    public static String getLocalPath() {
        String unbufferName = getLibName(PlatformInfo.localhost().getPlatform());
        if (unbufferName == null) {
            return null;
        }
        File file = InstalledFileLocator.getDefault().locate("bin/" + unbufferName, null, false); // NOI18N
        if (file != null && file.exists()) {
            return fixPath(file.getAbsolutePath());
        } else {
            log.warning("unbuffer: " + unbufferName + " not found");
            return null;
        }
    }

    /*
     * Not implemented yet
     */
    public static String getRemotePath(String host) {
        String path = HostInfoProvider.getDefault().getLibDir(host);
        if (path == null) {
            return null;
        }
        String unbufferName = getLibName(PlatformInfo.getDefault(host).getPlatform());
        if (unbufferName != null) {
            path += unbufferName;
            // check file existence
            if (HostInfoProvider.getDefault().fileExists(host, path)) {
                return path;
            } else {
                log.warning("unbuffer: " + path + " does not exist");
            }
        }
        return null;
    }
    
    private static String fixPath(String path) {
        // TODO: implement
        /*
        if (isCygwin() && path.charAt(1) == ':') {
            return "/cygdrive/" + path.charAt(0) + path.substring(2).replace("\\", "/"); // NOI18N
        } else if (isMinGW() && path.charAt(1) == ':') {
            return "/" + path.charAt(0) + path.substring(2).replace("\\", "/"); // NOI18N
        } else {
            return path;
        }*/
        return path;
    }
    
    public static String getLibName(int platform) {
        switch (platform) {
            case PlatformTypes.PLATFORM_LINUX : return "unbuffer-Linux-x86.so"; // NOI18N
            case PlatformTypes.PLATFORM_SOLARIS_SPARC : return "unbuffer-SunOS-sparc.so"; // NOI18N
            case PlatformTypes.PLATFORM_SOLARIS_INTEL : return "unbuffer-SunOS-x86.so"; // NOI18N
            case PlatformTypes.PLATFORM_WINDOWS : return "unbuffer-Windows_XP-x86.dll"; // NOI18N
            case PlatformTypes.PLATFORM_MACOSX : return "unbuffer-Mac_OS_X-x86.dylib"; // NOI18N
            default: log.warning("unbuffer search: unknown platform number " + platform); return null;
        }
    }
}
