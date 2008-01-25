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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Alexander Simon
 */
public class LogReader {
    private static final String CURRENT_DIRECTORY = "Current working directory";
    private static final String INVOKE_SUN_C = "cc -";
    private static final String INVOKE_SUN_CC = "CC -";
    private static final String INVOKE_GNU_C = "gcc -";
    private static final String INVOKE_GNU_CC = "g++ -";
    
    private String workingDir;
    private String root;
    private List<SourceFileProperties> result = new ArrayList<SourceFileProperties>();
    
    public LogReader(String fileName, String root){
        this.root = root;
        File file = new File(fileName);
        if (file.exists() && file.canRead()){
            try {
                BufferedReader in = new BufferedReader(new FileReader(file));
                int i = 0;
                while(true){
                    String line = in.readLine();
                    if (line == null){
                        break;
                    }
                    if (parseLine(line)){
                        i++;
                    }
                }
                System.out.println("Totally found "+i);
                System.out.println("Result contains "+result.size());
                in.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public List<SourceFileProperties> getResults(){
        return result;
    }
    
    private boolean parseLine(String line){
       if (line.startsWith(CURRENT_DIRECTORY)) {
           workingDir= line.substring(CURRENT_DIRECTORY.length()+1).trim();
           return false;
       }
       if (line.startsWith("/")){
           if (line.indexOf(" ")<0){
               if (new File(line).exists()){
                   workingDir= line.trim();
                   return false;
               }
           }
       }
       if (workingDir == null) {
           return false;
       }
       if (!workingDir.startsWith(root)){
           return false;
       }
       int i = line.indexOf(INVOKE_GNU_C);
       if (i < 0) {
           i = line.indexOf(INVOKE_SUN_C);
       }
       if (i >= 0) {
           if (i > 0){
               if (line.charAt(i-1)!='/'){
                   return false;
               }
           }
           gatherLine(line, false);
           
           return true;
       } else {
           i = line.indexOf(INVOKE_SUN_CC);
           if (i < 0) {
               i = line.indexOf(INVOKE_GNU_CC);
           }
           if (i >= 0) {
               if (i > 0){
                   if (line.charAt(i-1)!='/'){
                       return false;
                   }
               }
               if (gatherLine(line, true)) {
                  return true;
               }
           }
       }
       return false;
    }

    private boolean gatherLine(String line, boolean isCPP) {
        // /set/c++/bin/5.9/intel-S2/prod/bin/CC -c -g -DHELLO=75 -Idist  main.cc -Qoption ccfe -prefix -Qoption ccfe .XAKABILBpivFlIc.
        // /opt/SUNWspro/bin/cc -xO3 -xarch=amd64 -Ui386 -U__i386 -Xa -xildoff -errtags=yes -errwarn=%all
        // -erroff=E_EMPTY_TRANSLATION_UNIT -erroff=E_STATEMENT_NOT_REACHED -xc99=%none -W0,-xglobalstatic
        // -D_ELF64 -DTEXT_DOMAIN="SUNW_OST_OSCMD" -D_TS_ERRNO -I/export/opensolaris/testws77/proto/root_i386/usr/include
        // -I/export/opensolaris/testws77/usr/src/uts/common/inet/ipf -I/export/opensolaris/testws77/usr/src/uts/common/inet/pfil
        // -DSUNDDI -DUSE_INET6 -DSOLARIS2=11 -I. -DIPFILTER_LOOKUP -DIPFILTER_LOG -c ../ipmon_l.c -o ipmon_l.o
        List<String> list = DwarfSource.scanCommandLine(line);
        boolean hasQuotes = false;
        for(String s : list){
            if (s.startsWith("\"")){
                hasQuotes = true;
                break;
            }
        }
        if (hasQuotes) {
            List<String> newList = new ArrayList<String>();
            for(int i = 0; i < list.size();) {
                String s = list.get(i);
                if (s.startsWith("-D") && i+1 < list.size() && list.get(i+1).startsWith("\"")){ // NOI18N
                    String longString = null;
                    for(int j = i+1; j < list.size() && list.get(j).startsWith("\""); j++){
                        if (longString != null) {
                            longString += " " + list.get(j);
                        } else {
                            longString = list.get(j);
                        }
                        i = j;
                    }
                    newList.add(s+"'"+longString+"'");
                } else {
                    newList.add(s);
                }
                i++;
            }
            list = newList;
        }
        String what = null;
        List<String> userIncludes = new ArrayList<String>();
        Map<String, String> userMacros = new HashMap<String, String>();
        Iterator<String> st = list.iterator();
        String option = null;
        if (st.hasNext()) {
            option = st.next();
            if (option.equals("+") && st.hasNext()) {
                option = st.next();
            }
        }
        while(st.hasNext()){
            option = st.next();
            if (option.startsWith("-D")){ // NOI18N
                if (option.equals("-D") && st.hasNext()){
                    option = st.next();
                }
                String macro = option.substring(2);
                int i = macro.indexOf('=');
                if (i>0){
                    String value = macro.substring(i+1).trim();
                    if (value.length() >= 2 &&
                       (value.charAt(0) == '\'' && value.charAt(value.length()-1) == '\'' || // NOI18N
                        value.charAt(0) == '"' && value.charAt(value.length()-1) == '"' )) { // NOI18N
                        value = value.substring(1,value.length()-1);
                    }
                    userMacros.put(PathCache.getString(macro.substring(0,i)), PathCache.getString(value));
                } else {
                    userMacros.put(PathCache.getString(macro), null);
                }
            } else if (option.startsWith("-I")){ // NOI18N
                String path = option.substring(2);
                if (path.length()==0 && st.hasNext()){
                    path = st.next();
                }
                String include = PathCache.getString(path);
                userIncludes.add(include);
            } else if (option.startsWith("-isystem")){ // NOI18N
                String path = option.substring(8);
                if (path.length()==0 && st.hasNext()){
                    path = st.next();
                }
                String include = PathCache.getString(path);
                userIncludes.add(include);
            } else if (option.startsWith("-Y")){ // NOI18N
                String defaultSearchPath = option.substring(2);
                if (defaultSearchPath.length()==0 && st.hasNext()){
                    defaultSearchPath = st.next();
                }
                if (defaultSearchPath.startsWith("I,")){ // NOI18N
                    defaultSearchPath = defaultSearchPath.substring(2);
                    String include = PathCache.getString(defaultSearchPath);
                    userIncludes.add(include);
                }
            } else if (option.equals("-K")){ // NOI18N
                // Skip pic
                if (st.hasNext()){
                    st.next();
                }
            } else if (option.equals("-R")){ // NOI18N
                // Skip runtime search path 
                if (st.hasNext()){
                    st.next();
                }
            } else if (option.equals("-l")){ // NOI18N
                // Skip link with library
                if (st.hasNext()){
                    st.next();
                }
            } else if (option.equals("-L")){ // NOI18N
                // Skip library search path
                if (st.hasNext()){
                    st.next();
                }
            } else if (option.equals("-M")){ // NOI18N
                // Skip library search path
                if (st.hasNext()){
                    st.next();
                }
            } else if (option.equals("-h")){ // NOI18N
                // Skip generated dynamic shared library
                if (st.hasNext()){
                    st.next();
                }
            } else if (option.equals("-o")){ // NOI18N
                // Skip result
                if (st.hasNext()){
                    st.next();
                }
            } else if (option.equals("-fopenmp")){ // NOI18N
                    userMacros.put("_OPENMP", null); // NOI18N
            } else if (option.startsWith("-")){ // NOI18N
                // Skip option
            } else if (option.startsWith("ccfe")){ // NOI18N
                // Skip option
            } else if (option.startsWith(">")){ // NOI18N
                // Skip redurect
                break;
            } else {
                if (option.endsWith(".il") || option.endsWith(".o") || option.endsWith(".a") ||
                    option.endsWith(".so") || option.endsWith(".so.1")) {
                    continue;
                }
                if (what == null) {
                    what = option;
                } else {
                    System.out.println("What is this "+option + " previous "+what);
                    System.out.println("   in "+line);
                }
            }
        }
        if (what == null){
            return false;
        }
        String file = null;
        if (what.startsWith("/")){
            file = what;
        } else {
            file = workingDir+"/"+what;
        }
        File f = new File(file);
        if (!f.exists() || !f.isFile()) {
            if (true){
                System.out.println("Not found "+file);
                System.out.println("    "+line);
            }
            return false;
        }
        result.add(new CommandLineSource(isCPP, workingDir, what, userIncludes, userMacros));
        return true;
    }

    private static class CommandLineSource implements SourceFileProperties {

        private String compilePath;
        private String sourceName;
        private String fullName;
        private ItemProperties.LanguageKind language;
        private List<String> userIncludes;
        private List<String> systemIncludes = Collections.<String>emptyList();
        private Map<String, String> userMacros;
        private Map<String, String> systemMacros = Collections.<String, String>emptyMap();
        private Set<String> includedFiles = Collections.<String>emptySet();

        private CommandLineSource(boolean isCPP, String compilePath, String sourcePath, 
                List<String> userIncludes, Map<String, String> userMacros) {
            if (isCPP) {
                language = ItemProperties.LanguageKind.CPP;
            } else {
                language = ItemProperties.LanguageKind.C;
            }
            this.compilePath =compilePath;
            sourceName = sourcePath;
            if (sourceName.startsWith("/")) { // NOI18N
                fullName = sourceName;
                sourceName = DwarfSource.getRelativePath(compilePath, sourceName);
            } else {
                fullName = compilePath+"/"+sourceName;
            }
            File file = new File(fullName);
            fullName = FileUtil.normalizeFile(file).getAbsolutePath();
            fullName = PathCache.getString(fullName);
            this.userIncludes = userIncludes;
            this.userMacros = userMacros;
        }

        public String getCompilePath() {
            return compilePath;
        }

        public String getItemPath() {
            return fullName;
        }

        public String getItemName() {
            return sourceName;
        }

        public List<String> getUserInludePaths() {
            return userIncludes;
        }

        public List<String> getSystemInludePaths() {
            return systemIncludes;
        }

        public Set<String> getIncludedFiles() {
            return includedFiles;
        }

        public Map<String, String> getUserMacros() {
            return userMacros;
        }

        public Map<String, String> getSystemMacros() {
            return systemMacros;
        }

        public ItemProperties.LanguageKind getLanguageKind() {
            return language;
        }
    }
}
