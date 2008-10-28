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

package org.netbeans.modules.cnd.modeldiscovery.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.project.NativeFileItem.Language;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.ItemProperties.LanguageKind;
import org.netbeans.modules.cnd.discovery.api.PkgConfigManager.PackageConfiguration;
import org.netbeans.modules.cnd.discovery.api.PkgConfigManager.PkgConfig;
import org.netbeans.modules.cnd.discovery.api.PkgConfigManager.ResolvedPath;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class ModelSource implements SourceFileProperties {
    private static final boolean TRACE_AMBIGUOUS = Boolean.getBoolean("cnd.modeldiscovery.trace.ambiguous"); // NOI18N
    private Item item;
    private CsmFile file;
    private Map<String,List<String>> searchBase;
    private PkgConfig pkgConfig;
    private String itemPath;
    private List<String> userIncludePaths;
    private Set<String> includedFiles = new HashSet<String>();
    
    public ModelSource(Item item, CsmFile file, Map<String,List<String>> searchBase, PkgConfig pkgConfig){
        this.item = item;
        this.file = file;
        this.searchBase = searchBase;
        this.pkgConfig = pkgConfig;
    }

    public Set<String> getIncludedFiles() {
        if (userIncludePaths == null) {
            getUserInludePaths();
        }
        return includedFiles;
    }

    public String getCompilePath() {
        return new File( getItemPath()).getParentFile().getAbsolutePath();
    }
    
    public String getItemPath() {
        if (itemPath == null) {
            itemPath = item.getAbsPath();
            itemPath = itemPath.replace('\\','/');
            itemPath = cutLocalRelative(itemPath);
            if (Utilities.isWindows()) {
                itemPath = itemPath.replace('/', File.separatorChar);
            }
        }
        return itemPath;
    }
    
    private static final String PATTERN = "/../"; // NOI18N
    public static String cutLocalRelative(String path){
        String pattern = PATTERN;
        while(true) {
            int i = path.indexOf(pattern);
            if (i < 0){
                break;
            }
            int k = -1;
            for (int j = i-1; j >= 0; j-- ){
                if ( path.charAt(j)=='/'){
                    k = j;
                    break;
                }
            }
            if (k<0) {
                break;
            }
            path = path.substring(0,k+1)+path.substring(i+pattern.length());
        }
        return path;
    }
    
    public String getItemName() {
        return item.getFile().getName();
    }
    
    public List<String> getUserInludePaths() {
        if (userIncludePaths == null) {
            List includePaths = item.getUserIncludePaths();
            Set<String> res = new LinkedHashSet<String>();
            for(Object o : includePaths){
                String path = (String)o;
                path = getRelativepath(path);
                res.add(path);
            }
            analyzeUnresolved(res,file, 0);
            userIncludePaths = new ArrayList<String>(res);
        }
        return userIncludePaths;
    }
    
    private String getRelativepath(String path){
        if (Utilities.isWindows()) {
            path = path.replace('/', File.separatorChar);
        }
        path = IpeUtils.toRelativePath(getCompilePath(), path);
        path = FilePathAdaptor.mapToRemote(path);
        path = FilePathAdaptor.normalize(path);
        return path;
    }
    
    private void analyzeUnresolved(Set<String> res, CsmFile what, int level){
        if (what == null) {
            return;
        }
        for (CsmInclude include : what.getIncludes()){
            CsmFile resolved = include.getIncludeFile();
            if (resolved == null){
                // unresolved include
                String path = guessPath(include);
                if (path != null) {
                    resolved = file.getProject().findFile(path+File.separatorChar+include.getIncludeName());
                    path = getRelativepath(path);
                    res.add(path);
                    if (level < 5 && resolved != null) {
                        analyzeUnresolved(res, resolved, level+1);
                    }
                } else {
                    if (pkgConfig != null) {
                        ResolvedPath rp = pkgConfig.getResolvedPath(include.getIncludeName().toString());
                        if (rp != null) {
                            res.add(rp.getIncludePath());
                            for(PackageConfiguration pc : rp.getPackages()){
                                for(String p : pc.getIncludePaths()){
                                    if (!getSystemInludePaths().contains(p)){
                                        res.add(p);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                includedFiles.add(resolved.getAbsolutePath().toString());
                if (level < 5) {
                    analyzeUnresolved(res, resolved, level+1);
                }
            }
        }
    }
    
    private String guessPath(CsmInclude include){
        String name = include.getIncludeName().toString();
        String found = name.replace('\\','/');
        String prefix = null;
        String back = null;
        int i = found.lastIndexOf('/');
        if(i >= 0){
            prefix = found.substring(0,i+1);
            found = found.substring(i+1);
            i = prefix.lastIndexOf("./"); // NOI18N
            if (i >= 0) {
                back = prefix.substring(0,i+2);
                prefix = prefix.substring(i+2);
                if (prefix.length()==0) {
                    prefix = null;
                    name = found;
                } else {
                    name = prefix+'/'+found;
                }
            }
        }
        List<String> result = searchBase.get(found);
        if (result != null && result.size()>0){
            int pos = -1;
            //TODO: resolve ambiguously
            for(int j = 0; j < result.size(); j++){
                if (result.get(j).endsWith(name)){
                    if (pos >= 0) {
                        if (TRACE_AMBIGUOUS) {
                            System.out.println("Ambiguous name for item: "+getItemPath()); // NOI18N
                            System.out.println("  name1: "+result.get(pos)); // NOI18N
                            System.out.println("  name2: "+result.get(j)); // NOI18N
                        }
                    } else {
                        pos = j;
                    }
                }
            }
            if (pos >=0) {
                String path = result.get(pos);
                path = path.substring(0,path.length()-name.length()-1);
                return path;
            }
        }
        if (TRACE_AMBIGUOUS) {
            System.out.println("Unresolved name for item: "+getItemPath()); // NOI18N
            System.out.println("  from: "+include.getContainingFile().getAbsolutePath()); // NOI18N
            System.out.println("  name: "+include.getIncludeName()); // NOI18N
            if (result != null && result.size()>0){
                for(int j = 0; j < result.size(); j++){
                    System.out.println("  candidate: "+result.get(j)); // NOI18N
                }
            }
        }
        return null;
    }
    
    public List<String> getSystemInludePaths() {
        List includePaths = item.getSystemIncludePaths();
        List<String> res = new ArrayList<String>();
        for(Object o : includePaths){
            String path = (String)o;
            res.add(path);
        }
        return res;
    }
    
    public Map<String, String> getUserMacros() {
        List macros = item.getUserMacroDefinitions();
        Map<String, String> res = new HashMap<String,String>();
        for(Object o : macros){
            String macro = (String)o;
            int i = macro.indexOf('=');
            if (i>0){
                res.put(macro.substring(0,i).trim(),macro.substring(i+1).trim());
            } else {
                res.put(macro,null);
            }
        }
        return res;
    }
    
    public Map<String, String> getSystemMacros() {
        return null;
    }
    
    public ItemProperties.LanguageKind getLanguageKind() {
        if (item.getLanguage() == Language.C){
            return LanguageKind.C;
        } else if (item.getLanguage() == Language.CPP){
            return LanguageKind.CPP;
        }
        return LanguageKind.Unknown;
    }
}

