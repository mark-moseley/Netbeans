/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.utils.system;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;

/**
 *
 * @author Kirill Sorokin
 */
public class LinuxNativeUtils extends UnixNativeUtils {
    public static final String LIBRARY_PREFIX_LINUX =
            NATIVE_JNILIB_RESOURCE_SUFFIX +
            "linux/" ; //NOI18N
    
    public static final String LIBRARY_I386 =
            "linux.so"; //NO18N
    public static final String LIBRARY_AMD64 =
            "linux-amd64.so"; //NO18N
    
    private static final String PROC_MOUNTS_FILE = "/proc/mounts";
    
    public static final String[] FORBIDDEN_DELETING_FILES_LINUX = {};
    
    LinuxNativeUtils() {
        String library = System.getProperty("os.arch").equals("amd64") ?
            LIBRARY_AMD64 : LIBRARY_I386;
        
        loadNativeLibrary(LIBRARY_PREFIX_LINUX + library);
        
        initializeForbiddenFiles(FORBIDDEN_DELETING_FILES_LINUX);
    }
    
    public File getDefaultApplicationsLocation() {
        File usrlocal = new File("/usr/local");
        
        if (usrlocal.exists() &&
                usrlocal.isDirectory() &&
                FileUtils.canWrite(usrlocal)) {
            return usrlocal;
        } else {
            return SystemUtils.getUserHomeDirectory();
        }
    }
    @Override
    public List<File> getFileSystemRoots() throws IOException {
        List <File> roots = super.getFileSystemRoots();
        final File mounts = new File(PROC_MOUNTS_FILE);
        try {
            if(FileUtils.exists(mounts) && FileUtils.canRead(mounts)) {
                List <String> strings = FileUtils.readStringList(mounts);
                for(int i=0;i<strings.size();i++ ) {
                    String line = strings.get(i).trim();
                    final int firstSpaceIndex = line.indexOf(StringUtils.SPACE);
                    if(firstSpaceIndex!=-1) {
                        String mountPoint = line.substring(firstSpaceIndex + 1).trim();
                        final int nextSpaceIndex = mountPoint.indexOf(StringUtils.SPACE);
                        if(nextSpaceIndex!=-1) {
                            mountPoint = mountPoint.substring(0, nextSpaceIndex);
                            if(mountPoint.startsWith(File.separator)) {
                                final File mountFile = new File(mountPoint);
                                if(!roots.contains(mountFile)) {
                                    LogManager.log("... adding mount point from /proc/mounts: " + mountFile);
                                    roots.add(mountFile);
                                }
                            }
                        }
                        
                    }
                }
            }
        } catch (IOException e) {
            LogManager.log("... cannot read " + mounts, e);
        }
        return roots;
    }
    
}
