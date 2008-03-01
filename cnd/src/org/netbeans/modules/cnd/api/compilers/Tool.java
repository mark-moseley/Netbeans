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

package org.netbeans.modules.cnd.api.compilers;

import java.io.File;
import java.util.ResourceBundle;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class Tool {
    
    // Compiler types
    public static int CCompiler = 0;
    public static int CCCompiler = 1;
    public static int FortranCompiler = 2;
    public static int CustomTool = 3;
    public static int Assembler = 4;
    public static int MakeTool = 5;
    public static int DebuggerTool = 6;

    private static final String[] TOOL_NAMES = {
        getString("CCompiler"), // NOI18N
        getString("CCCompiler"), // NOI18N
        getString("FortranCompiler"), // NOI18N
        getString("CustomBuildTool"), // NOI18N
        getString("Assembler"), // NOI18N
        getString("MakeTool"), // NOI18N
        getString("DebuggerTool"), // NOI18N
    };
    
    private static final String[] COMPILER_TOOL_NAMES = {
        getString("CCompiler"), // NOI18N
        getString("CCCompiler"), // NOI18N
        getString("FortranCompiler"), // NOI18N
        getString("Assembler"), // NOI18N
        getString("CustomBuildTool"), // NOI18N
    };
    
    private CompilerFlavor flavor;
    private int kind;
    private String name;
    private String displayName;
    private String path;
    private String includeFilePrefix = null;
    
    /** Creates a new instance of GenericCompiler */
    public Tool(CompilerFlavor flavor, int kind, String name, String displayName, String path) {
        this.flavor = flavor;
        this.kind = kind;
        this.name = name;
        this.displayName = displayName;
        this.path = name.length() > 0 ? path + File.separator + name : path;
    }
    
    public CompilerFlavor getFlavor() {
        return flavor;
    }
    
    public int getKind() {
        return kind;
    }
    
    public String getName() {
        return name;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String p) {
        if (p == null) {
            
        }
        else {
            path = p;
            name = IpeUtils.getBaseName(path);
        }
    }
    
    public static String[] getCompilerToolNames() {
        return COMPILER_TOOL_NAMES;
    }
    
    public static int getTool(String name) {
        for (int i = 0; i < TOOL_NAMES.length; i++) {
            if (TOOL_NAMES[i].equals(name)) {
                return i;
            }
        }
        return 0; // ????
    }
    
    public static String getName(int kind) {
        if (kind >= 0 && kind <= TOOL_NAMES.length) {
            return TOOL_NAMES[kind];
        }
        else {
            return null;
        }
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
//    public String getGenericName() {
//        String name = getName();
//        if (name.length() > 0) {
//            return TOOL_NAMES[getKind()] + " - " + getName(); // NOI18N
//        } else {
//           return TOOL_NAMES[getKind()]; 
//        }
//    }
    
    public static String getToolDisplayName(int kind) {
        return TOOL_NAMES[kind];
    }
    
    public String toString() {
        String name = getName();
        if (Utilities.isWindows() && name.endsWith(".exe")) { // NOI18N
            return name.substring(0, name.length() - 4);
        } else {
            return name;
        }
    }
    
    public String getIncludeFilePathPrefix() {
        if (includeFilePrefix == null) {
            includeFilePrefix = ""; // NOI18N
            if (getFlavor() == CompilerFlavor.Cygwin ||
                    getFlavor() == CompilerFlavor.MinGW ||
                    getFlavor() == CompilerFlavor.DJGPP ||
                    getFlavor() == CompilerFlavor.Interix) {
                int i = getPath().toLowerCase().indexOf("\\bin"); // NOI18N
                if (i < 0)
                    i = getPath().toLowerCase().indexOf("/bin"); // NOI18N
                if (i > 0) {
                    includeFilePrefix = getPath().substring(0, i);
                    includeFilePrefix = includeFilePrefix.replaceAll("\\\\", "/"); // NOI18N
                    //includeFilePrefix = FilePathAdaptor.normalize(includeFilePrefix);
                }
            }
        }
        return includeFilePrefix;
    }
    
    public boolean exists() {
        if (getPath() == null || getPath().length() == 0)
            return false;
        return new File(getPath()).exists();
    }
    
    private static ResourceBundle bundle = null;
    protected static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(Tool.class);
        }
        return bundle.getString(s);
    }
}
