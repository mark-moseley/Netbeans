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

package org.netbeans.modules.cnd.makeproject.api.compilers;

import java.io.File;
import java.util.List;
import java.util.Vector;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.compilers.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.cnd.api.utils.RemoteUtils;
import org.openide.filesystems.FileUtil;

public abstract class BasicCompiler extends Tool {

    /** Creates a new instance of GenericCompiler */
    public BasicCompiler(String hkey, CompilerFlavor flavor, int kind, String name, String displayName, String path) {
        super(hkey, flavor, kind, name, displayName, path);
        storagePrefix += RemoteUtils.getHostName(getHostKey()) + "/"; //NOI18N
    }

    private String storagePrefix = System.getProperty("user.home") + "/.netbeans/6.5/cnd2/includes-cache/"; //NOI18N

    public String getStoragePrefix() {
        return storagePrefix;
    }

    @Override
    public abstract CompilerDescriptor getDescriptor();

    public String getDevelopmentModeOptions(int value) {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getDevelopmentModeFlags() != null && compiler.getDevelopmentModeFlags().length > value){
            return compiler.getDevelopmentModeFlags()[value];
        }
        return ""; // NOI18N
    }

    public String getWarningLevelOptions(int value) {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getWarningLevelFlags() != null && compiler.getWarningLevelFlags().length > value){
            return compiler.getWarningLevelFlags()[value];
        }
        return ""; // NOI18N
    }

    public String getSixtyfourBitsOption(int value) {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getArchitectureFlags() != null && compiler.getArchitectureFlags().length > value){
            return compiler.getArchitectureFlags()[value];
        }
        return ""; // NOI18N
    }

    public String getStripOption(boolean value) {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && value){
            return compiler.getStripFlag();
        }
        return ""; // NOI18N
    }

    public String getDependencyGenerationOption() {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getDependencyGenerationFlags() != null) {
            return compiler.getDependencyGenerationFlags();
        }
        return ""; // NOI18N
    }

    public List getSystemPreprocessorSymbols() {
        return new Vector();
    }

    public List getSystemIncludeDirectories() {
        return new Vector();
    }

    /**
     * @return true if settings were really replaced by new one
     */
    public boolean setSystemPreprocessorSymbols(List values) {
        return false;
    }

    /**
     * @return true if settings were really replaced by new one
     */
    public boolean setSystemIncludeDirectories(List values) {
        return false;
    }

    protected void normalizePaths(List<String> paths) {
        for (int i = 0; i < paths.size(); i++) {
            paths.set(i, normalizePath(paths.get(i)));
        }
    }

 
    protected String normalizePath(String path) {
        if (RemoteUtils.isLocalhost(getHostKey())) {
            return FileUtil.normalizeFile(new File(path)).getAbsolutePath();
        } else {
            //TODO: this hardly can be called normalization but this place is too handy for such kind of conversion
            return storagePrefix + path;
        }
    }
}