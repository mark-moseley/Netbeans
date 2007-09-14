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

package org.netbeans.modules.cnd.makeproject.api.compilers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;

public class CCCCompiler extends BasicCompiler {
    
    public CCCCompiler(CompilerFlavor flavor, int kind, String name, String displayName, String path) {
        super(flavor, kind, name, displayName, path);
    }
    
    // To be overridden
    public String getMTLevelOptions(int value) {
        return ""; // NOI18N
    }
    
    // To be overridden
    public String getLibraryLevelOptions(int value) {
        return ""; // NOI18N
    }
    
    // To be overridden
    public String getStandardsEvolutionOptions(int value) {
        return ""; // NOI18N
    }
    
    // To be overridden
    public String getLanguageExtOptions(int value) {
        return ""; // NOI18N
    }
    
    protected void getSystemIncludesAndDefines(String command, boolean stdout) throws IOException {
            Process process;
            process = Runtime.getRuntime().exec(command + " " + "/dev/null"); // NOI18N
            if (stdout)
                parseCompilerOutput(process.getInputStream());
            else
                parseCompilerOutput(process.getErrorStream());
        }
    
    // To be overridden
    public void saveSystemIncludesAndDefines() {
    }
    
    // To be overridden
    public void resetSystemIncludesAndDefines() {
    }
    
    // To be overridden
    protected void parseCompilerOutput(InputStream is) {
    }
    
    /**
     * Determines whether the given macro presents in the list
     * @param macrosList list of macros strings (in the form "macro=value" or just "macro")
     * @param macroToFind the name of the macro to search for
     * @return true if macro with the given name is found, otherwise false
     */
    protected boolean containsMacro(List macrosList, String macroToFind) {
	int len = macroToFind.length();
	for (Iterator it = macrosList.iterator(); it.hasNext();) {
	    String macro = (String) it.next();
	    if (macro.startsWith(macroToFind) ) {
		if( macro.length() == len ) {
		    return true; // they are just equal
		}
		if( macro.charAt(len) == '=' ) {
		    return true; // it presents in the form macro=value
		}
	    }
	}
	return false;
    }

    protected void parseUserMacros(final String line, final PersistentList preprocessorList) {
        int defineIndex = line.indexOf("-D"); // NOI18N
        while (defineIndex > 0) {
            String token;
            int spaceIndex = line.indexOf(" ", defineIndex + 1); // NOI18N
            if (spaceIndex > 0) {
                token = line.substring(defineIndex+2, spaceIndex);
                preprocessorList.add(token);
                defineIndex = line.indexOf("-D", spaceIndex); // NOI18N
            } else {
                token = line.substring(defineIndex+2);
                preprocessorList.add(token);
                break;
            }
        }
    }
}
