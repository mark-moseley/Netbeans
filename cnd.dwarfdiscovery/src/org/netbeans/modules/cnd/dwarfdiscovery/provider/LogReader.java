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
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.DiscoveryUtils;
import org.netbeans.modules.cnd.discovery.api.PkgConfigManager;
import org.netbeans.modules.cnd.discovery.api.PkgConfigManager.PackageConfiguration;
import org.netbeans.modules.cnd.discovery.api.PkgConfigManager.PkgConfig;
import org.netbeans.modules.cnd.discovery.api.Progress;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class LogReader {
    private static boolean TRACE = Boolean.getBoolean("cnd.dwarfdiscovery.trace.read.log"); // NOI18N
    
    private String workingDir;
    private String guessWorkingDir;
    private String baseWorkingDir;
    private final String root;
    private final String fileName;
    private List<SourceFileProperties> result;
    
    public LogReader(String fileName, String root){
        if (root.length()>0) {
            this.root = CndFileUtils.normalizeFile(new File(root)).getAbsolutePath();
        } else {
            this.root = root;
        }
        this.fileName = fileName;
       
        // XXX
        setWorkingDir(root);
    }
    
    private void run(Progress progress, AtomicBoolean isStoped) {
        if (TRACE) {System.out.println("LogReader is run for " + fileName);} //NOI18N
        Pattern pattern = Pattern.compile(";|\\|\\||&&"); // ;, ||, && //NOI18N
        result = new ArrayList<SourceFileProperties>();
        File file = new File(fileName);
        if (file.exists() && file.canRead()){
            try {
                PkgConfig pkgConfig = PkgConfigManager.getDefault().getPkgConfig(null);
                BufferedReader in = new BufferedReader(new FileReader(file));
                long length = file.length();
                long read = 0;
                int done = 0;
                if (length <= 0){
                    progress = null;
                }
                if (progress != null) {
                    progress.start(100);
                }
                int nFoundFiles = 0;
                while(true){
                    if (isStoped.get()) {
                        break;
                    }
                    String line = in.readLine();
                    if (line == null){
                        break;
                    }
                    read += line.length()+1;
                    line = line.trim();
                    while (line.endsWith("\\")) { // NOI18N
                        String oneMoreLine = in.readLine();
                        if (oneMoreLine == null) {
                            break;
                        }
                        line = line.substring(0, line.length() - 1) + " " + oneMoreLine.trim(); //NOI18N
                    }
                    line = trimBackApostropheCalls(line, pkgConfig);

                    String[] cmds = pattern.split(line);
                    for (int i = 0; i < cmds.length; i++) {
                        if (parseLine(cmds[i])){
                            nFoundFiles++;
                        }
                    }
                    if (read*100/length > done && done < 100){
                        done++;
                        if (progress != null) {
                            progress.increment();
                        }
                    }
                }
                if (progress != null) {
                    progress.done();
                }
                if (TRACE) {
                    System.out.println("Files found: " + nFoundFiles); //NOI18N
                    System.out.println("Files included in result: "+ result.size()); //NOI18N
                }
                in.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public List<SourceFileProperties> getResults(Progress progress, AtomicBoolean isStoped) {
        if (result == null) {
            run(progress, isStoped);
        }
        return result;
    }
    
    private static final String CURRENT_DIRECTORY = "Current working directory"; //NOI18N
    private static final String ENTERING_DIRECTORY = "Entering directory"; //NOI18N

    private boolean checkDirectoryChange(String line) {
        String workDir = null, message = null;

        if (line.startsWith(CURRENT_DIRECTORY)) {
            workDir = line.substring(CURRENT_DIRECTORY.length() + 1).trim();
            if (TRACE) {message = "**>> by [" + CURRENT_DIRECTORY + "] ";} //NOI18N
        } else if (line.indexOf(ENTERING_DIRECTORY) >= 0) {
            String dirMessage = line.substring(line.indexOf(ENTERING_DIRECTORY) + ENTERING_DIRECTORY.length() + 1).trim();
            workDir = dirMessage.replaceAll("`|'|\"", ""); //NOI18N
            if (TRACE) {message = "**>> by [" + ENTERING_DIRECTORY + "] ";} //NOI18N
            baseWorkingDir = workDir;
        } else if (line.startsWith(LABEL_CD)) {
            int end = line.indexOf(MAKE_DELIMITER);
            workDir = (end == -1 ? line : line.substring(0, end)).substring(LABEL_CD.length()).trim();
            if (TRACE) {message = "**>> by [ " + LABEL_CD + "] ";} //NOI18N
            if (workDir.startsWith("/")){ // NOI18N
                baseWorkingDir = workDir;
            }
        } else if (line.startsWith("/") && line.indexOf(" ") < 0) {  //NOI18N
            workDir = line.trim();
            if (TRACE) {message = "**>> by [just path string] ";} //NOI18N
        }

        if (workDir == null) {
            return false;
        }

        if (Utilities.isWindows() && workDir.startsWith("/cygdrive/") && workDir.length()>11){ // NOI18N
            workDir = ""+workDir.charAt(10)+":"+workDir.substring(11); // NOI18N
        }

        if (!workDir.startsWith(".") && (new File(workDir).exists())) { // NOI18N
            if (TRACE) {System.err.print(message);}
            setWorkingDir(workDir);
            return true;
        } else {
            String dir = workingDir + File.separator + workDir;
            if (new File(dir).exists()) {
                if (TRACE) {System.err.print(message);}
                setWorkingDir(dir);
                return true;
            }
            if (Utilities.isWindows() && workDir.length()>3 &&
                workDir.charAt(0)=='/' &&
                workDir.charAt(2)=='/'){
                String d = ""+workDir.charAt(1)+":"+workDir.substring(2); // NOI18N
                if (new File(d).exists()) {
                    if (TRACE) {System.err.print(message);}
                    setWorkingDir(d);
                    return true;
                }
            }
            if (baseWorkingDir != null) {
                dir = baseWorkingDir + File.separator + workDir;
                if (new File(dir).exists()) {
                    if (TRACE) {System.err.print(message);}
                    setWorkingDir(dir);
                    return true;
                }
            }
        }
        return false;
    }
    
    /*package-local*/ enum CompilerType {
        CPP, C, UNKNOWN;
    };
    
    /*package-local*/ static class LineInfo {
        public String compileLine;
        public CompilerType compilerType = CompilerType.UNKNOWN;
        
        LineInfo(String line) {
            compileLine = line;
        }
    }
    
    private static final String LABEL_CD = "cd "; //NOI18N
    private static final String INVOKE_SUN_C = "cc "; //NOI18N
    private static final String INVOKE_SUN_CC = "CC "; //NOI18N
    private static final String INVOKE_GNU_C = "gcc "; //NOI18N
    //private static final String INVOKE_GNU_XC = "xgcc "; //NOI18N
    private static final String INVOKE_GNU_CC = "g++ "; //NOI18N
    private static final String INVOKE_MSVC = "cl "; //NOI18N
    private static final String MAKE_DELIMITER = ";"; //NOI18N
    
    /*package-local*/ static LineInfo testCompilerInvocation(String line) {
        LineInfo li = new LineInfo(line);
        int start = 0, end = -1;
//        if (li.compilerType == CompilerType.UNKNOWN) {
//            start = line.indexOf(INVOKE_GNU_XC);
//            if (start>=0) {
//                li.compilerType = CompilerType.C;
//                end = start + INVOKE_GNU_XC.length();
//            }
//        } 
        if (li.compilerType == CompilerType.UNKNOWN) {
            start = line.indexOf(INVOKE_GNU_C);
            //TODO: can fail on gcc calls with -shared-libgcc
            if (start>=0) {
                li.compilerType = CompilerType.C;
                end = start + INVOKE_GNU_C.length();
            }
        } 
        if (li.compilerType == CompilerType.UNKNOWN) {
            start = line.indexOf(INVOKE_GNU_CC);
            if (start>=0) {
                li.compilerType = CompilerType.CPP;
                end = start + INVOKE_GNU_CC.length();
            }
        } 
        if (li.compilerType == CompilerType.UNKNOWN) {
            start = line.indexOf(INVOKE_SUN_C);
            if (start>=0) {
                li.compilerType = CompilerType.C;
                end = start + INVOKE_SUN_C.length();
            }
        } 
        if (li.compilerType == CompilerType.UNKNOWN) {
            start = line.indexOf(INVOKE_SUN_CC);
            if (start>=0) {
                li.compilerType = CompilerType.CPP;
                end = start + INVOKE_SUN_CC.length();
            }
        }
        if (li.compilerType == CompilerType.UNKNOWN) {
            start = line.indexOf(INVOKE_MSVC);
            if (start>=0) {
                li.compilerType = CompilerType.CPP;
                end = start + INVOKE_MSVC.length();
            }
        }
       
        if (li.compilerType != CompilerType.UNKNOWN) {
            li.compileLine = line.substring(start);
            while(end < line.length() && (line.charAt(end) == ' ' || line.charAt(end) == '\t')){
                end++;
            }
            if (end >= line.length() || line.charAt(end)!='-') {
                // suspected compiler invocation has no options or a part of a path?? -- noway
                li.compilerType =  CompilerType.UNKNOWN;
//            } else if (start > 0 && line.charAt(start-1)!='/') {
//                // suspected compiler invocation is not first command in line?? -- noway
//                String prefix = line.substring(0, start - 1).trim();
//                // wait! maybe it's called in condition?
//                if (!(line.charAt(start - 1) == ' ' &&
//                        ( prefix.equals("if") || prefix.equals("then") || prefix.equals("else") ))) { //NOI18N
//                    // or it's a lib compiled by libtool?
//                    int ltStart = line.substring(0, start).indexOf("libtool"); //NOI18N
//                        if (!(ltStart >= 0 && line.substring(ltStart, start).indexOf("compile") >= 0)) { //NOI18N
//                            // no, it's not a compile line
//                            li.compilerType = CompilerType.UNKNOWN;
//                            // I hope
//                            if (TRACE) {System.err.println("Suspicious line: " + line);}
//                        }
//                    }
            }
        }
        return li;
    }
    
    private void setWorkingDir(String workingDir) {
        if (TRACE) {System.err.println("**>> new working dir: " + workingDir);}
        this.workingDir = CndFileUtils.normalizeFile(new File(workingDir)).getAbsolutePath();
    }

    private void setGuessWorkingDir(String workingDir) {
        if (TRACE) {System.err.println("**>> alternative guess working dir: " + workingDir);}
        this.guessWorkingDir = CndFileUtils.normalizeFile(new File(workingDir)).getAbsolutePath();
    }
    
    private boolean parseLine(String line){
       if (checkDirectoryChange(line)) {
           return false;
       }
//       if (line.startsWith(CURRENT_DIRECTORY)) {
//           workingDir= line.substring(CURRENT_DIRECTORY.length()+1).trim();
//           return false;
//       }
       if (workingDir == null) {
           return false;
       }
       if (!workingDir.startsWith(root)){
           return false;
       }
       
       LineInfo li = testCompilerInvocation(line);
       if (li.compilerType != CompilerType.UNKNOWN) {
           gatherLine(li.compileLine, line.startsWith("+"), li.compilerType == CompilerType.CPP); // NOI18N
           return true;
       }
       return false;
    }

    private static final String PKG_CONFIG_PATTERN = "pkg-config "; //NOI18N
    private static final String ECHO_PATTERN = "echo "; //NOI18N
    /*package-local*/ static String trimBackApostropheCalls(String line, PkgConfig pkgConfig) {
        int i = line.indexOf('`'); //NOI18N
        if (line.lastIndexOf('`') == i) {  //NOI18N // do not trim unclosed `quotes`
            return line;
        }
        if (i < 0 || i == line.length() - 1) {
            return line;
        } else {
            StringBuilder out = new StringBuilder();
            if (i > 0) {
                out.append(line.substring(0, i));
            }
            line = line.substring(i+1);
            int j = line.indexOf('`'); //NOI18N
            if (j < 0) {
                return line;
            }
            String pkg = line.substring(0,j);
            if (pkg.startsWith(PKG_CONFIG_PATTERN)) { //NOI18N
                pkg = pkg.substring(PKG_CONFIG_PATTERN.length());
                StringTokenizer st = new StringTokenizer(pkg);
                boolean readFlags = false;
                String findPkg = null;
                while(st.hasMoreTokens()) {
                    String aPkg = st.nextToken();
                    if (aPkg.equals("--cflags")) { //NOI18N
                        readFlags = true;
                        continue;
                    }
                    if (aPkg.startsWith("-")) { //NOI18N
                        readFlags = false;
                        continue;
                    }
                    findPkg = aPkg;
                }
                if (readFlags && pkgConfig != null && findPkg != null) {
                    PackageConfiguration pc = pkgConfig.getPkgConfig(findPkg);
                    if (pc != null) {
                        for(String p : pc.getIncludePaths()){
                            out.append(" -I"+p); //NOI18N
                        }
                        for(String p : pc.getMacros()){
                            out.append(" -D"+p); //NOI18N
                        }
                        out.append(" "); //NOI18N
                    }
                }
            } else if (pkg.startsWith(ECHO_PATTERN)) {
                pkg = pkg.substring(ECHO_PATTERN.length());
                if (pkg.startsWith("'") && pkg.endsWith("'")) { //NOI18N
                    out.append(pkg.substring(1, pkg.length()-1));
                } else {
                    StringTokenizer st = new StringTokenizer(pkg);
                    boolean first = true;
                    if (st.hasMoreTokens()) {
                        if (!first) {
                            out.append(" "); //NOI18N
                        }
                        first = false;
                        out.append(st.nextToken());
                    }
                }
            } else if (pkg.contains(ECHO_PATTERN)) {
                pkg = pkg.substring(pkg.indexOf(ECHO_PATTERN)+ECHO_PATTERN.length());
                if (pkg.startsWith("'") && pkg.endsWith("'")) { //NOI18N
                    out.append(pkg.substring(1, pkg.length()-1)); //NOI18N
                } else {
                    StringTokenizer st = new StringTokenizer(pkg);
                    boolean first = true;
                    if (st.hasMoreTokens()) {
                        if (!first) {
                            out.append(" "); //NOI18N
                        }
                        first = false;
                        out.append(st.nextToken());
                    }
                }
            }
            out.append(line.substring(j+1));
            return trimBackApostropheCalls(out.toString(), pkgConfig);
        }
    }

    private boolean gatherLine(String line, boolean isScriptOutput, boolean isCPP) {
        // /set/c++/bin/5.9/intel-S2/prod/bin/CC -c -g -DHELLO=75 -Idist  main.cc -Qoption ccfe -prefix -Qoption ccfe .XAKABILBpivFlIc.
        // /opt/SUNWspro/bin/cc -xO3 -xarch=amd64 -Ui386 -U__i386 -Xa -xildoff -errtags=yes -errwarn=%all
        // -erroff=E_EMPTY_TRANSLATION_UNIT -erroff=E_STATEMENT_NOT_REACHED -xc99=%none -W0,-xglobalstatic
        // -D_ELF64 -DTEXT_DOMAIN="SUNW_OST_OSCMD" -D_TS_ERRNO -I/export/opensolaris/testws77/proto/root_i386/usr/include
        // -I/export/opensolaris/testws77/usr/src/uts/common/inet/ipf -I/export/opensolaris/testws77/usr/src/uts/common/inet/pfil
        // -DSUNDDI -DUSE_INET6 -DSOLARIS2=11 -I. -DIPFILTER_LOOKUP -DIPFILTER_LOG -c ../ipmon_l.c -o ipmon_l.o
        List<String> userIncludes = new ArrayList<String>();
        Map<String, String> userMacros = new HashMap<String, String>();
        String what = DiscoveryUtils.gatherCompilerLine(line, isScriptOutput, userIncludes, userMacros,null);
        if (what == null){
            return false;
        }
        String file = null;
        if (what.startsWith("/")){  //NOI18N
            file = what;
        } else {
            file = workingDir+"/"+what;  //NOI18N
        }
        List<String> userIncludesCached = new ArrayList<String>(userIncludes.size());
        for(String s : userIncludes){
            userIncludesCached.add(PathCache.getString(s));
        }
        Map<String, String> userMacrosCached = new HashMap<String, String>(userMacros.size());
        for(Map.Entry<String,String> e : userMacros.entrySet()){
            if (e.getValue() == null) {
                userMacrosCached.put(PathCache.getString(e.getKey()), null);
            } else {
                userMacrosCached.put(PathCache.getString(e.getKey()), PathCache.getString(e.getValue()));
            }
        }
        File f = new File(file);
        if (f.exists() && f.isFile()) {
            if (TRACE) {System.err.println("**** Gotcha: " + file);}
            result.add(new CommandLineSource(isCPP, workingDir, what, userIncludesCached, userMacrosCached));
            return true;
        }
        if (guessWorkingDir != null && !what.startsWith("/")) { //NOI18N
            f = new File(guessWorkingDir+"/"+what);  //NOI18N
            if (f.exists() && f.isFile()) {
                if (TRACE) {System.err.println("**** Gotcha guess: " + file);}
                result.add(new CommandLineSource(isCPP, guessWorkingDir, what, userIncludesCached, userMacrosCached));
                return true;
            }
        }
        if (TRACE)  {System.err.println("**** Not found "+file);} //NOI18N
        if (!what.startsWith("/")){  //NOI18N
            try {
                String[] out = new String[1];
                boolean areThereOnlyOne = findFiles(new File(root), what, out);
                if (out[0] == null) {
                    if (TRACE) {System.err.println("** And there is no such file under root");}
                } else {
                    if (areThereOnlyOne) {
                        result.add(new CommandLineSource(isCPP, out[0], what, userIncludes, userMacros));
                        if (TRACE) {System.err.println("** Gotcha: " + out[0] + File.separator + what);}
                        // kinda adventure but it works
                        setGuessWorkingDir(out[0]);
                        return true;
                    } else {
                        if (TRACE) {System.err.println("**There are several candidates and I'm not clever enough yet to find correct one.");}
                    }
                }
            } catch (IOException ex) {
                if (TRACE) {Exceptions.printStackTrace(ex);}
            }
            if (TRACE) {System.err.println(""+ (line.length() > 120 ? line.substring(0,117) + ">>>" : line) + " [" + what + "]");} //NOI18N
            return false;
        }
        return false;
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
                sourceName = DiscoveryUtils.getRelativePath(compilePath, sourceName);
            } else {
                fullName = compilePath+"/"+sourceName; //NOI18N
            }
            File file = new File(fullName);
            fullName = CndFileUtils.normalizeFile(file).getAbsolutePath();
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

    // java -cp main/nbbuild/netbeans/cnd2/modules/org-netbeans-modules-cnd-dwarfdiscovery.jar:main/nbbuild/netbeans/cnd2/modules/org-netbeans-modules-cnd-discovery.jar:main/nbbuild/netbeans/cnd2/modules/org-netbeans-modules-cnd-apt.jar:main/nbbuild/netbeans/cnd2/modules/org-netbeans-modules-cnd-utils.jar:main/nbbuild/netbeans/platform8/core/org-openide-filesystems.jar:main/nbbuild/netbeans/platform8/lib/org-openide-util.jar org.netbeans.modules.cnd.dwarfdiscovery.provider.LogReader filename root
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Not enough parameters. Format: bla-bla-bla filename root");
            return;
        }
        String objFileName = args[0];
        String root = args[1];
        LogReader.TRACE = true;
        LogReader clrf = new LogReader(objFileName, root);
        List<SourceFileProperties> list = clrf.getResults(null, new AtomicBoolean(false));
        System.err.print("\n*** Results: ");
        for (SourceFileProperties sourceFileProperties : list) {
            String fileName = sourceFileProperties.getItemName();
            while (fileName.indexOf("../") == 0) { //NOI18N
                fileName = fileName.substring(3);
            }
            System.err.print(fileName + " ");
        }
        System.err.println();
    }
    
    private static boolean findFiles(File file, String relativePath, String[] out) throws IOException {
        File f = new File(file.getAbsolutePath() + File.separator + relativePath);
        //System.err.println("# " + file.getAbsolutePath());
        if (f.exists() && f.isFile()) {
            if (out[0] != null && !new File(out[0] + File.separator + relativePath).getCanonicalPath().equals(f.getCanonicalPath()) ) {
                return false;
            }
            out[0] = file.getAbsolutePath();
        }
        for (File subs : file.listFiles(dirFilter)) {
            if (!findFiles(subs, relativePath, out)) {
                return false;
            }
        }
        return true;
    }
    
    private final static FileFilter dirFilter = new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        };
}
