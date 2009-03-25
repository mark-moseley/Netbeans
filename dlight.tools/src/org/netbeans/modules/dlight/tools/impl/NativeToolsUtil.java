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

package org.netbeans.modules.dlight.tools.impl;

import java.io.File;
import java.text.ParseException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.openide.modules.InstalledFileLocator;

/**
 * An utility class that helps working with native prof_monitor and prof_agent
 * @author Vladimir Kvashin
 */
// package-local
class NativeToolsUtil {

    private NativeToolsUtil() {}

    public static String getLdPreloadName(String osname) {
        if ("Mac_OS_X".equals(osname)) { // NOI18N
            return "DYLD_INSERT_LIBRARIES"; // NOI18N
        } else {
            return "LD_PRELOAD"; // NOI18N
        }
    }

    public static String getExecutable(String name) {
        return getPlatformBinary(name, ""); // NOI18N
    }

    public static String getSharedLibrary(String name) {
        return getPlatformBinary(name, ".${soext}"); //NOI18N
    }

    private static String getPlatformBinary(String name, String suffix) {
        return "bin" + File.separator + name + "-${osname}-${platform}${_isa}" + suffix; //NOI18N
    }

    public static File locateFile(ExecutionEnvironment env, String relativePathWithMacros) {
        MacroExpander mef = MacroExpanderFactory.getExpander(env);
        try {
            String relativePath = mef.expandPredefinedMacros(relativePathWithMacros);
            return InstalledFileLocator.getDefault().locate(relativePath, null, false);
        } catch (ParseException ex) {
            return null;
        }
    }

}
