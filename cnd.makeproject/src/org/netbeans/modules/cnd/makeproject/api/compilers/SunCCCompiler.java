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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.api.compilers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Vector;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

public class SunCCCompiler extends CCCCompiler {
    private static final String compilerStderrCommand = "CC -xdryrun -E"; // NOI18N
    private static final String compilerStderrCommand2 = "CC -xdumpmacros=defs,sys -E"; // NOI18N
    private PersistentList systemIncludeDirectoriesList = null;
    private PersistentList systemPreprocessorSymbolsList = null;
    private boolean saveOK = true;
    
    private static final String[] DEVELOPMENT_MODE_OPTIONS = {
        "",  // Fast Build // NOI18N
        "-g", // Debug" // NOI18N
        "-g0 -xO3 -xhwcprof", // Performance Debug" // NOI18N
        "-xprofile=tcov +d -xinline=", // Test Coverage // NOI18N
        "-g0 -xO2", // Dianosable Release // NOI18N
        "-xO3", // Release // NOI18N
        "-xO5 -xipo=1 -xdepend -fsimple=1 -xlibmil -xlibmopt -xvector -xbuiltin -sync_stdio=no -xalias_level=simple -sync_stdio=no", // Performance Release // NOI18N
    };
    
    private static final String[] WARNING_LEVEL_OPTIONS = {
        "-w", // No Warnings // NOI18N
        "", // Default // NOI18N
        "+w", // More Warnings // NOI18N
        "-xwe", // Convert Warnings to Errors // NOI18N
    };
    
    private static final String[] LIBRARY_LEVEL_OPTIONS = {
        "-library=no%Cstd,no%Crun -filt=no%stdlib", // NOI18N
        "-library=no%Cstd -filt=no%stdlib", // NOI18N
        "-library=iostream,no%Cstd -filt=no%stdlib", // NOI18N
        "", // NOI18N
        "-library=stlport4,no%Cstd", // NOI18N
    };
    
    private static final String[] MT_LEVEL_OPTIONS = {
        "", // None // NOI18N
        "-mt", // Safe // NOI18N
        "-xautopar -xvector -xreduction -xloopinfo", // Automatic // NOI18N
        "-xopenmp", // Open MP // NOI18N
    };
    
    private static final String[] STANDARD_OPTIONS = {
        "-compat", // Old // NOI18N
        "-features=no%localfor,no%extinl,no%conststrings", // Legacy // NOI18N
        "", // Default // NOI18N
        "-features=no%anachronisms,no%transitions,tmplife", // Modern // NOI18N
    };
    
    private static final String[] LANGUAGE_EXT_OPTIONS = {
        "-features=no%longlong", // None // NOI18N
        "", // Default // NOI18N
        "-features=extensions,tmplrefstatic,iddollar", // All // NOI18N
    };
    
    /** Creates a new instance of SunCCompiler */
    public SunCCCompiler() {
        super(CCCompiler, "CC", "Sun C++ Compiler"); // NOI18N
    }
    
    public String getDevelopmentModeOptions(int value) {
        return DEVELOPMENT_MODE_OPTIONS[value];
    }
    
    public String getWarningLevelOptions(int value) {
        if (value < WARNING_LEVEL_OPTIONS.length)
            return WARNING_LEVEL_OPTIONS[value];
        else
            return ""; // NOI18N
    }
    
    public String getSixtyfourBitsOption(boolean value) {
        return value ? "-xarch=generic64" : ""; // NOI18N
    }
    
    public String getStripOption(boolean value) {
        return value ? "-s" : ""; // NOI18N
    }
    
    public void setSystemIncludeDirectories(Platform platform, List values) {
        systemIncludeDirectoriesList = new PersistentList(values);
    }
    
    public void setSystemPreprocessorSymbols(Platform platform, List values) {
        systemPreprocessorSymbolsList = new PersistentList(values);
    }
    
    public List getSystemPreprocessorSymbols(Platform platform) {
        if (systemPreprocessorSymbolsList != null)
            return systemPreprocessorSymbolsList;
        
        getSystemIncludesAndDefines(platform);
        return systemPreprocessorSymbolsList;
    }
    
    public List getSystemIncludeDirectories(Platform platform) {
        if (systemIncludeDirectoriesList != null)
            return systemIncludeDirectoriesList;
        
        getSystemIncludesAndDefines(platform);
        return systemIncludeDirectoriesList;
    }
    
    // To be overridden
    public String getMTLevelOptions(int value) {
        return MT_LEVEL_OPTIONS[value];
    }
    
    // To be overridden
    public String getLibraryLevelOptions(int value) {
        return LIBRARY_LEVEL_OPTIONS[value];
    }
    
    // To be overridden
    public String getStandardsEvolutionOptions(int value) {
        return STANDARD_OPTIONS[value];
    }
    
    // To be overridden
    public String getLanguageExtOptions(int value) {
        return LANGUAGE_EXT_OPTIONS[value];
    }
    
    public void saveSystemIncludesAndDefines() {
        if (systemIncludeDirectoriesList != null && saveOK)
            systemIncludeDirectoriesList.saveList(getClass().getName() + "." + "systemIncludeDirectoriesList"); // NOI18N
        if (systemPreprocessorSymbolsList != null && saveOK)
            systemPreprocessorSymbolsList.saveList(getClass().getName() + "." + "systemPreprocessorSymbolsList"); // NOI18N
    }
    
    private void restoreSystemIncludesAndDefines(Platform platform) {
        systemIncludeDirectoriesList = PersistentList.restoreList(getClass().getName() + "." + "systemIncludeDirectoriesList"); // NOI18N
        systemPreprocessorSymbolsList = PersistentList.restoreList(getClass().getName() + "." + "systemPreprocessorSymbolsList"); // NOI18N
    }
    
    private void getSystemIncludesAndDefines(Platform platform) {
        restoreSystemIncludesAndDefines(platform);
        if (systemIncludeDirectoriesList == null || systemPreprocessorSymbolsList == null) {
            getFreshSystemIncludesAndDefines(platform);
        }
    }
    
    private void getFreshSystemIncludesAndDefines(Platform platform) {
        try {
            systemIncludeDirectoriesList = new PersistentList();
            systemPreprocessorSymbolsList = new PersistentList();
            getSystemIncludesAndDefines(platform, compilerStderrCommand, false);
            getSystemIncludesAndDefines(platform, compilerStderrCommand2, false);
            systemIncludeDirectoriesList.add("/usr/include"); // NOI18N
            saveOK = true;
        } catch (IOException ioe) {
            System.err.println("IOException " + ioe);
            String errormsg = NbBundle.getMessage(getClass(), "CANTFINDCOMPILER", getName()); // NOI18N
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
            saveOK = false;
        }
    }
    
    public void resetSystemIncludesAndDefines(Platform platform) {
        getFreshSystemIncludesAndDefines(platform);
    }
    
    protected void parseCompilerOutput(Platform platform, InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                //System.out.println(line);
                int includeIndex = line.indexOf("-I"); // NOI18N
                while (includeIndex > 0) {
                    String token;
                    int spaceIndex = line.indexOf(" ", includeIndex + 1); // NOI18N
                    if (spaceIndex > 0) {
                        token = line.substring(includeIndex+2, spaceIndex);
                        systemIncludeDirectoriesList.add(token);
                        if (token.endsWith("Cstd")) { // NOI18N
                            // See 89872 "Parser Settings" for Sun Compilers Collection are incorrect
                            systemIncludeDirectoriesList.add(token.substring(0, token.length()-4) + "std"); // NOI18N
                        }
                        // Hack to handle -compat flag. If this flag is added,
                        // the compiler looks in in CC4 and not in CC. Just adding CC4 doesn't
                        // fix this problem but it may work for some include files
//                        if (token.endsWith("include/CC")) // NOI18N
//                            systemIncludeDirectoriesList.add(token + "4"); // NOI18N
                        includeIndex = line.indexOf("-I", spaceIndex); // NOI18N
                    }
                }
                
                int defineIndex = line.indexOf("-D"); // NOI18N
                while (defineIndex > 0) {
                    String token;
                    int spaceIndex = line.indexOf(" ", defineIndex + 1); // NOI18N
                    if (spaceIndex > 0) {
                        token = line.substring(defineIndex+2, spaceIndex);
                        systemPreprocessorSymbolsList.add(token);
                        defineIndex = line.indexOf("-D", spaceIndex); // NOI18N
                    }
                }
                
                if (line.startsWith("#define ")) { // NOI18N
                    int i = line.indexOf(' ', 8);
                    if (i > 0) {
                        String token = line.substring(8, i) + "=" + line.substring(i+1); // NOI18N
                        systemPreprocessorSymbolsList.add(token);
                    }
                }
            }
            is.close();
            reader.close();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe); // FIXUP
        }
    }
    
    private void dumpLists() {
        System.out.println("==================================" + getDisplayName()); // NOI18N
        for (int i = 0; i < systemIncludeDirectoriesList.size(); i++) {
            System.out.println("-I" + systemIncludeDirectoriesList.get(i)); // NOI18N
        }
        for (int i = 0; i < systemPreprocessorSymbolsList.size(); i++) {
            System.out.println("-D" + systemPreprocessorSymbolsList.get(i)); // NOI18N
        }
    }
}
