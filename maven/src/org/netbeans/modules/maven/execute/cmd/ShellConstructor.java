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

package org.netbeans.modules.maven.execute.cmd;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Utilities;

/**
 *
 * @author mkleint
 */
public class ShellConstructor implements Constructor {
    private final File mavenHome;

    public ShellConstructor(File mavenHome) {
        this.mavenHome = mavenHome;
    }

    public List<String> construct() {
        //#164234
        //if maven.bat file is in space containing path, we need to quote with simple quotes.
        String quote = "\"";
        // the command line parameters with space in them need to be quoted and escaped to arrive
        // correctly to the java runtime on windows
        String escaped = "\\" + quote;
        List<String> toRet = new ArrayList<String>();
        String ex = Utilities.isWindows() ? "mvn.bat" : "mvn"; //NOI18N
        if (mavenHome != null) {
            File bin = new File(mavenHome, "bin" + File.separator + ex);//NOI18N
            if (bin.exists()) {
                toRet.add(quoteSpaces(bin.getAbsolutePath(), quote));
            } else {
                toRet.add(ex);
            }
        } else {
            toRet.add(ex);
        }

        if (Utilities.isWindows() && Boolean.getBoolean("maven.run.cmd")) { //#153101
            toRet.add(0, "/c"); //NOI18N
            toRet.add(0, "cmd"); //NOI18N
        }
        return toRet;
    }

    // we run the shell/bat script in the process, on windows we need to quote any spaces
    //once/if we get rid of shell/bat execution, we might need to remove this
    //#164234
    private static String quoteSpaces(String val, String quote) {
        if (val.indexOf(' ') != -1) { //NOI18N
            if (Utilities.isWindows()) {
                return quote + val + quote; //NOI18N
            }
        }
        return val;
    }


}
