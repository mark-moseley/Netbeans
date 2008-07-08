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
package org.netbeans.modules.cnd.api.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.api.compilers.PlatformTypes;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;

/**
 * This class evolutioned from cnd.api.utils.Path
 * Now it handles all development host related information
 *
 * @author gordonp
 * @author Sergey Grinev
 */
public final class PlatformInfo {

    private ArrayList<String> list = new ArrayList<String>();
    private String pathName = null;
    private final String host;
    private final int platform;

    public PlatformInfo(String host, int platform) {
        this.host = host;
        this.platform = platform;

        String path = getEnv().get("PATH"); // NOI18N
        if (Boolean.getBoolean("cnd.debug.use_altpath")) { // NOI18N
            // Its very hard to debug path problems on Windows because changing PATH is so hard. So these
            // properties let me do it without changing my real path
            path = System.getProperty("cnd.debug.altpath", path); // NOI18N
        }
        if (path != null) {
            StringTokenizer st = new StringTokenizer(path, File.pathSeparator); // NOI18N

            while (st.hasMoreTokens()) {
                String dir = st.nextToken();
                list.add(dir);
            }
        } else {
            if (isUnix()) {
                list.add("/bin"); // NOI18N
                list.add("/usr/bin"); // NOI18N
                list.add("/sbin"); // NOI18N
                list.add("/usr/sbin"); // NOI18N
            } else if (isWindows()) {
                list.add("C:/WINDOWS/System32"); // NOI18N
                list.add("C:/WINDOWS"); // NOI18N
                list.add("C:/WINDOWS/System32/WBem"); // NOI18N
            }
        }

    }

    /**
     * Replace the current path with this new one. We should validate but currently aren't.
     * 
     * @param newPath A list of directories to use as a replacement path
     */
    public void setPath(ArrayList<String> newPath) {
        list = newPath;
    }

    /**
     * Read the PATH from the environment and make an array from it.
     * 
     * @return A list of all path directories
     */
    public ArrayList<String> getPath() {
        return list;
    }

    /**
     * Return the path with the correct path separator character.
     * This would be named toString() if it weren't a method.
     * 
     * @return Path as a string (with OS specific directory separators)
     */
    public String getPathAsString() {
        StringBuffer buf = new StringBuffer();

        for (String dir : list) {
            buf.append(dir);
            buf.append(File.pathSeparator);
        }
        return buf.substring(0, buf.length() - 1); // remove the trailing pathSeparator...
    }

    /**
     * Add a directory to the path.
     * 
     * @param pos Position where dir should be added
     * @param dir New directory to add to path
     * @throws IndexOutOfBoundsException
     */
    public void add(int pos, String dir) throws IndexOutOfBoundsException {
        list.add(pos, dir);
    }

    /**
     * Remove a directory (by index) from the path.
     * 
     * @param pos Position where dir should be added
     * @throws IndexOutOfBoundsException
     */
    public void remove(int pos) throws IndexOutOfBoundsException {
        list.remove(pos);
    }

    /**
     * This utility method makes it easier (on Windows) to replace PATH with one with
     * the same case. IZ 103016 updated PATH but it wasn't foud because Path wasn't
     * replaced. This will let us add a path using the exact same name.
     */
    public String getPathName() {
        if (pathName == null) {
            if (isWindows()) {
                for (String key : getEnv().keySet()) {
                    if (key.toLowerCase().equals("path")) { // NOI18N
                        pathName = key.substring(0, 4);
                        return pathName;
                    }
                }
            }
            pathName = "PATH"; // NOI18N
        }
        return pathName;
    }

    public String findCommand(String cmd) {
        String cmd2 = null;
        ArrayList<String> dirlist = getPath();

        if (cmd.length() > 0) {
            if (isWindows() && !cmd.endsWith(".exe")) { // NOI18N
                cmd2 = cmd + ".exe"; // NOI18N
            }

            for (String dir : dirlist) {
                String path = dir + separator() + cmd;
                if (fileExists(path)) {
                    return path;
                }
                if (cmd2 != null) {
                    path = dir + separator() + cmd;
                    if (fileExists(path)) {
                        return path;
                    }
                }
            }
        }
        return null;
    }
    
    public String separator() {
        return isWindows() ? "\\" : "/"; // NOI18N
    }
    
    public String pathSeparator() {
        return isWindows() ? ";" : ":"; // NOI18N
    }
    
    // utility
    private boolean isWindows() {
        return platform == PlatformTypes.PLATFORM_WINDOWS;
    }

    private boolean isUnix() {
        return platform == PlatformTypes.PLATFORM_SOLARIS_INTEL || platform == PlatformTypes.PLATFORM_SOLARIS_SPARC || platform == PlatformTypes.PLATFORM_LINUX || platform == PlatformTypes.PLATFORM_MACOSX;
    }

    private Map<String, String> getEnv() {
        return HostInfoProvider.getDefault().getEnv(host);
    }
    
    private boolean fileExists(String path) {
        return HostInfoProvider.getDefault().fileExists(host, path);
    }
    
}
