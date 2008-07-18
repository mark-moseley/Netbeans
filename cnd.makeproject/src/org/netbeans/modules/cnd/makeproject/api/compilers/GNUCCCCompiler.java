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

package org.netbeans.modules.cnd.makeproject.api.compilers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * A common base class for GNU C and C++  compilers
 * @author vk155633
 */
public abstract class GNUCCCCompiler extends CCCCompiler {

    private PersistentList systemIncludeDirectoriesList = null;
    private PersistentList systemPreprocessorSymbolsList = null;
    private String includeFilePrefix = null;
    
    public GNUCCCCompiler(String hkey, CompilerFlavor flavor, int kind, String name, String displayName, String path) {
        super(hkey, flavor, kind, name, displayName, path);
    }

    @Override
    public boolean setSystemIncludeDirectories(List values) {
        assert values != null;
        if (values.equals(systemIncludeDirectoriesList)) {
            return false;
        }
        systemIncludeDirectoriesList = new PersistentList(values);
        normalizePaths(systemIncludeDirectoriesList);
        saveSystemIncludesAndDefines();
        return true;
    }
    
    @Override
    public boolean setSystemPreprocessorSymbols(List values) {
        assert values != null;
        if (values.equals(systemPreprocessorSymbolsList)) {
            return false;
        }
        systemPreprocessorSymbolsList = new PersistentList(values);
        saveSystemIncludesAndDefines();
        return true;
    }
    
    @Override
    public List getSystemPreprocessorSymbols() {
        if (systemPreprocessorSymbolsList != null)
            return systemPreprocessorSymbolsList;
        
        getSystemIncludesAndDefines();
        return systemPreprocessorSymbolsList;
    }
    
    @Override
    public List getSystemIncludeDirectories() {
        if (systemIncludeDirectoriesList != null)
            return systemIncludeDirectoriesList;
        
        getSystemIncludesAndDefines();
        return systemIncludeDirectoriesList;
    }
    
    @Override
    public void saveSystemIncludesAndDefines() {
        if (systemIncludeDirectoriesList != null)
            systemIncludeDirectoriesList.saveList(getUniqueID() + "systemIncludeDirectoriesList"); // NOI18N
        if (systemPreprocessorSymbolsList != null)
            systemPreprocessorSymbolsList.saveList(getUniqueID() + "systemPreprocessorSymbolsList"); // NOI18N
    }
    
    private void restoreSystemIncludesAndDefines() {
        systemIncludeDirectoriesList = PersistentList.restoreList(getUniqueID() + "systemIncludeDirectoriesList"); // NOI18N
        systemPreprocessorSymbolsList = PersistentList.restoreList(getUniqueID() + "systemPreprocessorSymbolsList"); // NOI18N
    }
    
    private void getSystemIncludesAndDefines() {
        restoreSystemIncludesAndDefines();
        if (systemIncludeDirectoriesList == null || systemPreprocessorSymbolsList == null) {
            getFreshSystemIncludesAndDefines();
        }
    }
    
    protected String getDefaultPath() {
        CompilerDescriptor compiler = getCompilerDescription();
        if (compiler != null && compiler.getNames().length > 0){
            return compiler.getNames()[0];
        }
        return ""; // NOI18N
    }
    
    protected String getCompilerStderrCommand() {
        CompilerDescriptor compiler = getCompilerDescription();
        if (compiler != null){
            return " "+compiler.getIncludeFlags();
        }
        return ""; // NOI18N
    }

    protected String getCompilerStdoutCommand() {
        CompilerDescriptor compiler = getCompilerDescription();
        if (compiler != null){
            return " "+compiler.getMacroFlags();
        }
        return ""; // NOI18N
    }
    
    private void getFreshSystemIncludesAndDefines() {
        systemIncludeDirectoriesList = new PersistentList();
        systemPreprocessorSymbolsList = new PersistentList();
        if (!getHostKey().endsWith(CompilerSetManager.LOCALHOST)) {
            // TODO: this is temporary to test CA for remote projects
            String storagePrefix = System.getProperty("user.home") + "\\.netbeans\\remote-inc\\" + getHostKey() + "\\"; //NOI18N //TODO
            systemIncludeDirectoriesList.add(storagePrefix + "\\usr\\include");
            systemIncludeDirectoriesList.add(storagePrefix + "\\usr\\local\\include");
            systemIncludeDirectoriesList.add(storagePrefix + "\\usr\\sfw\\include");
//            systemIncludeDirectoriesList.add(storagePrefix + "\\usr\\sfw\\include\\c++\\3.4.3");
//            systemIncludeDirectoriesList.add(storagePrefix + "\\usr\\sfw\\include\\c++\\3.4.3\\i386-pc-solaris2.10");

            //systemPreprocessorSymbolsList.add("__cplusplus=1"); // NOI18N
            return;
        } 
        String path = getPath();
        if (path != null && path.length() == 0) {
            return;
        }
        if (path == null || !new File(path).exists()) {
            path = getDefaultPath();
        }
        try {
            getSystemIncludesAndDefines(IpeUtils.getDirName(path), path + getCompilerStderrCommand(), false);
            getSystemIncludesAndDefines(IpeUtils.getDirName(path), path + getCompilerStdoutCommand(), true);
            // a workaround for gcc bug - see http://gcc.gnu.org/ml/gcc-bugs/2006-01/msg00767.html
            if (!containsMacro(systemPreprocessorSymbolsList, "__STDC__")) { // NOI18N
                systemPreprocessorSymbolsList.add("__STDC__=1"); // NOI18N
            }
            saveSystemIncludesAndDefines();
        } catch (IOException ioe) {
            System.err.println("IOException " + ioe);
            String errormsg = NbBundle.getMessage(getClass(), "CANTFINDCOMPILER", path); // NOI18N
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
        }
    }
    
    @Override
    public void resetSystemIncludesAndDefines() {
        getFreshSystemIncludesAndDefines();
    }
    
    private boolean startsWithPath(String line) {
        line = line.trim();
        if( line.startsWith("/") ) {  // NOI18N
            return true;
        }
        else if( System.getProperty("os.name").indexOf("Windows") >= 0 ) {
            if( Character.isLetter(line.charAt(0)) && line.charAt(1) == ':' ) {
                return true;
            }
        }
        return false;
    }

    protected String cutIncludePrefix(String line) {
        CompilerDescriptor compiler = getCompilerDescription();
        if (compiler != null && compiler.getRemoveIncludeOutputPrefix() != null) {
            String remove = compiler.getRemoveIncludeOutputPrefix();
            if (line.toLowerCase().startsWith(getIncludeFilePathPrefix().toLowerCase())) {
                line = line.substring(getIncludeFilePathPrefix().length());
            } else if (line.toLowerCase().startsWith(remove)) {
                line = line.substring(remove.length());
            }
        }
        return line;
    }

    @Override
    public String getIncludeFilePathPrefix() {
        if (includeFilePrefix == null) {
            includeFilePrefix = ""; // NOI18N
            CompilerDescriptor compiler = getCompilerDescription();
            if (compiler != null) {
                String path = getPath().replaceAll("\\\\", "/"); // NOI18N
                if (compiler.getRemoveIncludePathPrefix() != null) {
                    int i = path.toLowerCase().indexOf("/bin"); // NOI18N
                    if (i > 0) {
                        includeFilePrefix = path.substring(0, i);
                    }
                }
            }
        }
        return includeFilePrefix;
    }

    @Override
    protected void parseCompilerOutput(BufferedReader reader) {
        
        try {
            String line;
            boolean startIncludes = false;
            while ((line = reader.readLine()) != null) {
                //System.out.println(line);
                if (line.contains("#include <...>")) { // NOI18N
                    startIncludes = true;
                    continue;
                }
		if (startIncludes) {
                    if (line.startsWith("End of search") || ! startsWithPath(line)) { // NOI18N
                        startIncludes = false;
                        continue;
                    }
		}
                if (startIncludes) {
                    line = cutIncludePrefix(line.trim());
                    systemIncludeDirectoriesList.addUnique(normalizePath(getIncludeFilePathPrefix() + line));
                    if (getIncludeFilePathPrefix().length() > 0 && line.startsWith("/usr/lib")) // NOI18N
                        systemIncludeDirectoriesList.addUnique(normalizePath(getIncludeFilePathPrefix() + line.substring(4)));
                    continue;
                }
                parseUserMacros(line, systemPreprocessorSymbolsList);
                if (line.startsWith("#define ")) { // NOI18N
                    int i = line.indexOf(' ', 8);
                    if (i > 0) {
                        String token = line.substring(8, i) + "=" + line.substring(i+1); // NOI18N
                        systemPreprocessorSymbolsList.add(token);
                    }
                }
            }
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
