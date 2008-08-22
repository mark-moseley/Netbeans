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

package org.netbeans.modules.cnd.api.compilers;

import org.openide.util.Utilities;

/**
 * Moved platform types from cnd.makeproject to here so CompilerSetManager can use them. Because
 * its an interface, cnd.makeproject's Platform class can implement this interface and still have
 * the same platform types/values.
 * 
 * @author gordonp
 */
public class PlatformTypes {
    
    // Platform id's
    public static final int PLATFORM_SOLARIS_SPARC = 0;
    public static final int PLATFORM_SOLARIS_INTEL = 1;
    public static final int PLATFORM_LINUX = 2;
    public static final int PLATFORM_WINDOWS = 3;
    public static final int PLATFORM_MACOSX = 4;
    public static final int PLATFORM_GENERIC = 5;
    public static final int PLATFORM_NONE = 6;

    private static int defaultPlatform = -1;

    protected PlatformTypes(){
    }
    
    public static int getDefaultPlatform() {
        if (defaultPlatform <= 0) {
            String arch = System.getProperty("os.arch"); // NOI18N
            if (Utilities.isWindows())
                defaultPlatform = PlatformTypes.PLATFORM_WINDOWS;
            else if (Utilities.getOperatingSystem() == Utilities.OS_LINUX)
                defaultPlatform = PlatformTypes.PLATFORM_LINUX;
            else if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS && arch.indexOf("86") >= 0) // NOI18N
                defaultPlatform = PlatformTypes.PLATFORM_SOLARIS_INTEL;
            else if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS)
                defaultPlatform = PlatformTypes.PLATFORM_SOLARIS_SPARC;
            else if (Utilities.getOperatingSystem() == Utilities.OS_MAC)
                defaultPlatform = PlatformTypes.PLATFORM_MACOSX;
            else 
                defaultPlatform = PlatformTypes.PLATFORM_GENERIC;
        }
        return defaultPlatform;
    }

    public static String toString(int platform) {
        String out;
        switch (platform) {
            case PLATFORM_SOLARIS_SPARC:
                out = "PLATFORM_SOLARIS_SPARC";
                break;
            case PLATFORM_SOLARIS_INTEL:
                out = "PLATFORM_SOLARIS_INTEL";
                break;
            case PLATFORM_LINUX:
                out = "PLATFORM_LINUX";
                break;
            case PLATFORM_WINDOWS:
                out = "PLATFORM_WINDOWS";
                break;
            case PLATFORM_MACOSX:
                out = "PLATFORM_MACOSX";
                break;
            case PLATFORM_GENERIC:
                out = "PLATFORM_GENERIC";
                break;
            case PLATFORM_NONE:
                out = "PLATFORM_NONE";
                break;
            default:
                 out = "Error";
        }
        return out;
    }
}
