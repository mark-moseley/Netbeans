/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.api.compilers;

import java.io.File;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;

public class Tool {
    
    // Compiler types
    public static int CCompiler = 0;
    public static int CCCompiler = 1;
    public static int FortranCompiler = 2;
    public static int CustomTool = 3;

    private static final String[] TOOL_NAMES = {
        getString("CCompiler"), // NOI18N
        getString("CCCompiler"), // NOI18N
        getString("FortranCompiler"), // NOI18N
        getString("CustomBuildTool"), // NOI18N
    };
    
    private int kind;
    private String name;
    private String displayName;
    private String path;
    
    /** Creates a new instance of GenericCompiler */
    public Tool(int kind, String name, String displayName, String path) {
        this.kind = kind;
        this.name = name;
        this.displayName = displayName;
        this.path = path + File.separator + name;
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
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getGenericName() {
        return TOOL_NAMES[getKind()];
    }
    
    public String toString() {
        return getDisplayName();
    }
    
    private static ResourceBundle bundle = null;
    protected static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(Tool.class);
        }
        return bundle.getString(s);
    }
}
