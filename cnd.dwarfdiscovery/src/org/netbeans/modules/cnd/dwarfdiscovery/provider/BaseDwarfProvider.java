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

package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.ProjectUtil;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.LANG;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public abstract class BaseDwarfProvider implements DiscoveryProvider {
    
    private static final boolean TRACE_READ_EXCEPTIONS = Boolean.getBoolean("cnd.dwarfdiscovery.trace.read.errors"); // NOI18N
    private static final boolean FULL_TRACE = Boolean.getBoolean("cnd.dwarfdiscovery.trace.read.source"); // NOI18N
    public static final String RESTRICT_SOURCE_ROOT = "restrict_source_root"; // NOI18N
    public static final String RESTRICT_COMPILE_ROOT = "restrict_compile_root"; // NOI18N
    protected boolean isStoped = false;
    
    public BaseDwarfProvider() {
    }
    
    public boolean isApplicable(ProjectProxy project) {
        return true;
    }
    
    public boolean canAnalyze(ProjectProxy project) {
        return true;
    }
    
    public void stop() {
        isStoped = true;
    }
    
    protected List<ProjectProperties> divideByLanguage(List<SourceFileProperties> sources){
        DwarfProject cProp = null;
        DwarfProject cppProp = null;
        for (SourceFileProperties source : sources) {
            ItemProperties.LanguageKind lang = source.getLanguageKind();
            DwarfProject current = null;
            if (lang == ItemProperties.LanguageKind.C){
                if (cProp == null) {
                    cProp = new DwarfProject(lang);
                }
                current = cProp;
            } else {
                if (cppProp == null) {
                    cppProp = new DwarfProject(lang);
                }
                current = cppProp;
            }
            current.update(source);
        }
        List<ProjectProperties> languages = new ArrayList<ProjectProperties>();
        if (cProp != null) {
            languages.add(cProp);
        }
        if (cppProp != null) {
            languages.add(cppProp);
        }
        return languages;
    }
    
    protected List<SourceFileProperties> getSourceFileProperties(String[] objFileName){
        try{
            HashMap<String,SourceFileProperties> map = new HashMap<String,SourceFileProperties>();
            for (String file : objFileName) {
                if (isStoped) {
                    break;
                }
                for(SourceFileProperties f : getSourceFileProperties(file, map)){
                    if (isStoped) {
                        break;
                    }
                    ProviderProperty p = getProperty(RESTRICT_SOURCE_ROOT);
                    if (p != null) {
                        String s = (String)p.getValue();
                        if (!f.getItemPath().startsWith(s)){
                            continue;
                        }
                    }
                    p = getProperty(RESTRICT_COMPILE_ROOT);
                    if (p != null) {
                        String s = (String)p.getValue();
                        if (!f.getCompilePath().startsWith(s)){
                            continue;
                        }
                    }
                    
                    String name = f.getItemPath();
                    if (new File(name).exists()){
                        SourceFileProperties existed = map.get(name);
                        if (existed == null) {
                            map.put(name,f);
                        } else {
                            // Duplicated
                            if (existed.getUserInludePaths().size() < f.getUserInludePaths().size()){
                                map.put(name,f);
                            }
                        }
                    }
                }
            }
            List<SourceFileProperties> list = new ArrayList<SourceFileProperties>();
            list.addAll(map.values());
            return list;
        } finally {
            PathCache.dispose();
        }
    }
    
    protected int sizeComilationUnit(String objFileName){
        int res = 0;
        Dwarf dump = null;
        try{
            dump = new Dwarf(objFileName);
            List <CompilationUnit> units = dump.getCompilationUnits();
            if (units != null && units.size() > 0) {
                for (CompilationUnit cu : units) {
                    if (cu.getRoot() == null || cu.getSourceFileName() == null) {
                        continue;
                    }
                    String lang = cu.getSourceLanguage();
                    if (lang == null) {
                        continue;
                    }
                    if (LANG.DW_LANG_C.toString().equals(lang) ||
                            LANG.DW_LANG_C89.toString().equals(lang) ||
                            LANG.DW_LANG_C99.toString().equals(lang)) {
                        res++;
                    } else if (LANG.DW_LANG_C_plus_plus.toString().equals(lang)) {
                        res++;
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            // Skip Exception
        } catch (WrongFileFormatException ex) {
            // Skip Exception
        } catch (IOException ex) {
            // Skip Exception
        } catch (Exception ex) {
            // Skip Exception
        } finally {
            if (dump != null) {
                dump.dispose();
            }
        }
        return res;
    }
    
    private List<SourceFileProperties> getSourceFileProperties(String objFileName, HashMap<String,SourceFileProperties> map){
        List<SourceFileProperties> list = new ArrayList<SourceFileProperties>();
        Dwarf dump = null;
        try{
            if (FULL_TRACE) System.out.println("Process file "+objFileName);  // NOI18N
            dump = new Dwarf(objFileName);
            List <CompilationUnit> units = dump.getCompilationUnits();
            if (units != null && units.size() > 0) {
                for (CompilationUnit cu : units) {
                    if (isStoped) {
                        break;
                    }
                    if (cu.getRoot() == null || cu.getSourceFileName() == null) {
                        if (TRACE_READ_EXCEPTIONS) System.out.println("Compilation unit has broken name in file "+objFileName);  // NOI18N
                        continue;
                    }
                    String lang = cu.getSourceLanguage();
                    if (lang == null) {
                        if (TRACE_READ_EXCEPTIONS) System.out.println("Compilation unit has unresolved language in file "+objFileName);  // NOI18N
                        continue;
                    }
                    DwarfSource source = null;
                    if (LANG.DW_LANG_C.toString().equals(lang) ||
                            LANG.DW_LANG_C89.toString().equals(lang) ||
                            LANG.DW_LANG_C99.toString().equals(lang)) {
                        source = new DwarfSource(cu,false,getCommpilerSettings(), grepBase);
                    } else if (LANG.DW_LANG_C_plus_plus.toString().equals(lang)) {
                        source = new DwarfSource(cu,true,getCommpilerSettings(), grepBase);
                    } else {
                        if (FULL_TRACE) System.out.println("Unknown language: "+lang);  // NOI18N
                        // Ignore other languages
                    }
                    if (source != null) {
                        String name = source.getItemPath();
                        SourceFileProperties old = map.get(name);
                        if (old != null && old.getUserInludePaths().size() > 0) {
                            if (FULL_TRACE) System.out.println("Compilation unit already exist. Skip "+name);  // NOI18N
                            // do not process processed item
                            continue;
                        }
                        source.process(cu);
                        list.add(source);
                    }
                }
            } else {
                if (TRACE_READ_EXCEPTIONS) System.out.println("There are no compilation units in file "+objFileName);  // NOI18N
            }
        } catch (FileNotFoundException ex) {
            // Skip Exception
            if (TRACE_READ_EXCEPTIONS) System.out.println("File not found "+objFileName+": "+ex.getMessage());  // NOI18N
        } catch (WrongFileFormatException ex) {
            if (TRACE_READ_EXCEPTIONS) System.out.println("Unsuported format of file "+objFileName+": "+ex.getMessage());  // NOI18N
            list = new LogReader(objFileName).getResults();
        } catch (IOException ex) {
            if (TRACE_READ_EXCEPTIONS){
                System.err.println("Exception in file "+objFileName);  // NOI18N
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            if (TRACE_READ_EXCEPTIONS){
                System.err.println("Exception in file "+objFileName);  // NOI18N
                ex.printStackTrace();
            }
        } finally {
            if (dump != null) {
                dump.dispose();
            }
        }
        return list;
    }

    private Map<String,List<String>> grepBase = new HashMap<String,List<String>>();
    
    public CompilerSettings getCommpilerSettings(){
        return myCommpilerSettings;
    }
    
    public void setCommpilerSettings(ProjectProxy project) {
        myCommpilerSettings = new CompilerSettings(project);
    }
    private CompilerSettings myCommpilerSettings;

    public static class CompilerSettings{
        private List<String> systemIncludePathsC;
        private List<String> systemIncludePathsCpp;
        private Map<String,String> systemMacroDefinitionsC;
        private Map<String,String> systemMacroDefinitionsCpp;
        private Map<String,String> normalizedPaths = new HashMap<String,String>();
        private String compileFlavor;
        private String compileDirectory;
        
        public CompilerSettings(ProjectProxy project){
            systemIncludePathsCpp = ProjectUtil.getSystemIncludePaths(project, true);
            systemIncludePathsC = ProjectUtil.getSystemIncludePaths(project,false);
            systemMacroDefinitionsCpp = ProjectUtil.getSystemMacroDefinitions(project, true);
            systemMacroDefinitionsC = ProjectUtil.getSystemMacroDefinitions(project,false);
            compileFlavor = ProjectUtil.getCompilerFlavor(project);
            compileDirectory = ProjectUtil.getCompilerDirectory(project);
        }
        
        public List<String> getSystemIncludePaths(boolean isCPP) {
            if (isCPP) {
                return systemIncludePathsCpp;
            } else {
                return systemIncludePathsC;
            }
        }
        
        public Map<String,String> getSystemMacroDefinitions(boolean isCPP) {
            if (isCPP) {
                return systemMacroDefinitionsCpp;
            } else {
                return systemMacroDefinitionsC;
            }
        }
        
        public String getNormalizedPath(String path){
            String res = normalizedPaths.get(path);
            if (res == null) {
                res = normalizePath(path);
                normalizedPaths.put(path,res);
            }
            return res;
        }

        private String normalizePath(String path){
            path = FileUtil.normalizeFile(new File(path)).getAbsolutePath();
            if (Utilities.isWindows()) {
                path = path.replace('\\', '/');
            }
            return path;
        }

        public String getCompileFlavor() {
            return compileFlavor;
        }

        public String getCompileDirectory() {
            return compileDirectory;
        }
    }
}
