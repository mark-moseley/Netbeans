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

package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.io.File;
import org.netbeans.modules.cnd.actions.AbstractExecutorRunAction;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.execution.ShellExecSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public final class ConfigureUtils {
    private ConfigureUtils() {
    }

    public static String findConfigureScript(String folder){
        String pattern[] = new String[]{"configure"}; // NOI18N
        File file = new File(folder);
        if (!(file.isDirectory() && file.canRead() && file.canWrite())) {
            return null;
        }
        for (String name : pattern) {
            file = new File(folder+File.separator+name); // NOI18N
            if (isRunnable(file)){
                return file.getAbsolutePath();
            }
        }
        String res = detectQTProject(new File(folder));
        if (res != null) {
            return res;
        }
        res = detectCMake(folder);
        if (res != null) {
            return res;
        }
        return null;
    }

    private static String detectQTProject(File folder){
        for(File file : folder.listFiles()){
            if (file.getAbsolutePath().endsWith(".pro")){ // NOI18N
                if (AbstractExecutorRunAction.findTools("qmake") != null){ // NOI18N
                    return file.getAbsolutePath();
                }
                break;
            }
        }
        return null;
    }

    private static String detectCMake(String path){
        File configure = new File(path+File.separator+"CMakeLists.txt"); // NOI18N
        if (configure.exists()) {
            if (AbstractExecutorRunAction.findTools("cmake") != null) { // NOI18N
                return configure.getAbsolutePath();
            }
        }
        return null;
    }

    public static boolean isRunnable(File file) {
        if (file.exists() && file.isFile() && file.canRead()) {
            FileObject configureFileObject = FileUtil.toFileObject(file);
            if (configureFileObject == null || !configureFileObject.isValid()) {
                return false;
            }
            DataObject dObj;
            try {
                dObj = DataObject.find(configureFileObject);
            } catch (DataObjectNotFoundException ex) {
                return false;
            }
            if (dObj == null) {
                return false;
            }
            Node node = dObj.getNodeDelegate();
            if (node == null) {
                return false;
            }
            ShellExecSupport ses = node.getCookie(ShellExecSupport.class);
            if (ses != null) {
                return true;
            }
            if (file.getAbsolutePath().endsWith("CMakeLists.txt")){ // NOI18N
                return AbstractExecutorRunAction.findTools("cmake") != null; // NOI18N
            }
            if (file.getAbsolutePath().endsWith(".pro")){ // NOI18N
                return AbstractExecutorRunAction.findTools("qmake") != null; // NOI18N
            }
        }
        return false;
    }

    public static String findMakefile(String folder){
        String pattern[] = new String[]{"GNUmakefile", "makefile", "Makefile",}; // NOI18N
        File file = new File(folder);
        if (!(file.isDirectory() && file.canRead() && file.canWrite())) {
            return null;
        }
        for (String name : pattern) {
            file = new File(folder+File.separator+name); // NOI18N
            if (file.exists() && file.isFile() && file.canRead()) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    public static String getDefaultC(){
        CompilerSet def = CompilerSetManager.getDefault().getDefaultCompilerSet();
        String cCompiler = "gcc"; // NOI18N
        if (def != null) {
            CompilerFlavor flavor = def.getCompilerFlavor();
            if (flavor.isSunStudioCompiler()) {
                cCompiler = "cc"; // NOI18N
            }
        }
        return cCompiler;
    }

    public static String getDefaultCpp(){
        CompilerSet def = CompilerSetManager.getDefault().getDefaultCompilerSet();
        String cppCompiler = "g++"; // NOI18N
        if (def != null) {
            CompilerFlavor flavor = def.getCompilerFlavor();
            if (flavor.isSunStudioCompiler()) {
                cppCompiler = "CC"; // NOI18N
            }
        }
        return cppCompiler;
    }

    private static final String PREDEFINED_FLAGS_GNU = "\"-g3 -gdwarf-2\""; // NOI18N
    private static final String PREDEFINED_FLAGS_SUN = "-g"; // NOI18N
    public static String getConfigureArguments(String configure, String flags) {
        String cCompiler = ConfigureUtils.getDefaultC();
        String cppCompiler = ConfigureUtils.getDefaultCpp();
        StringBuilder buf = new StringBuilder(flags);
        String cCompilerFlags;
        if ("cc".equals(cCompiler)) { // NOI18N
            cCompilerFlags = PREDEFINED_FLAGS_SUN;
        } else {
            cCompilerFlags = PREDEFINED_FLAGS_GNU;
        }
        String cppCompilerFlags;
        if ("CC".equals(cppCompiler)) { // NOI18N
            cppCompilerFlags = PREDEFINED_FLAGS_SUN;
        } else {
            cppCompilerFlags = PREDEFINED_FLAGS_GNU;
        }
        if (configure.endsWith("CMakeLists.txt")){ // NOI18N
            appendIfNeed("-G ", flags, buf, "\"Unix Makefiles\""); // NOI18N
            appendIfNeed("-DCMAKE_BUILD_TYPE=", flags, buf, "Debug"); // NOI18N
            if ("cc".equals(cCompiler)) { // NOI18N
                appendIfNeed("-DCMAKE_C_COMPILER=", flags, buf, "cc"); // NOI18N
            }
            if ("CC".equals(cppCompiler)) { // NOI18N
                appendIfNeed("-DCMAKE_CXX_COMPILER=", flags, buf, "CC"); // NOI18N
            }
            appendIfNeed("-DCMAKE_C_FLAGS_DEBUG=", flags, buf, cCompilerFlags); // NOI18N
            appendIfNeed("-DCMAKE_CXX_FLAGS_DEBUG=", flags, buf, cppCompilerFlags); // NOI18N
        } else if (configure.endsWith(".pro")){ // NOI18N
            if ("CC".equals(cppCompiler) && Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) { // NOI18N
                appendIfNeed("-spec ", flags, buf, "solaris-cc"); // NOI18N
                appendIfNeed("QMAKE_CC=", flags, buf, "cc"); // NOI18N
                appendIfNeed("QMAKE_CXX=", flags, buf, "CC"); // NOI18N
            } else {
                appendIfNeed("QMAKE_CFLAGS=", flags, buf, cCompilerFlags); // NOI18N
                appendIfNeed("QMAKE_CXXFLAGS=", flags, buf, cppCompilerFlags); // NOI18N
            }
        } else {
            if ("cc".equals(cCompiler)) { // NOI18N
                appendIfNeed("CC=", flags, buf, "cc"); // NOI18N
            }
            if ("CC".equals(cppCompiler)) { // NOI18N
                appendIfNeed("CXX=", flags, buf, "CC"); // NOI18N
            }
            appendIfNeed("CFLAGS=", flags, buf, cCompilerFlags); // NOI18N
            appendIfNeed("CXXFLAGS=", flags, buf, cppCompilerFlags); // NOI18N
        }
        return buf.toString();
    }

    private static void appendIfNeed(String key, String flags, StringBuilder buf, String flag){
        if (flags.indexOf(key) < 0 ){
            if (buf.length() > 0) {
                buf.append(' '); // NOI18N
            }
            buf.append(key + flag);
        }
    }
}
