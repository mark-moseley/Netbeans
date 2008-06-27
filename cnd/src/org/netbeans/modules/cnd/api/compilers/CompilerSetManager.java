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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.utils.Path;
import org.netbeans.modules.cnd.compilers.DefaultCompilerProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 * Manage a set of CompilerSets. The CompilerSets are dynamically created based on which compilers
 * are found in the user's $PATH variable.
 */
public class CompilerSetManager {
    
    private static final String gcc_pattern = "([a-zA-z][a-zA-Z0-9_]*-)*gcc(([-.]\\d){2,4})?(\\.exe)?"; // NOI18N
    private static final String gpp_pattern = "([a-zA-z][a-zA-Z0-9_]*-)*g\\+\\+(([-.]\\d){2,4})?(\\.exe)?$"; // NOI18N
    private static final String cc_pattern = "([a-zA-z][a-zA-Z0-9_]*-)*cc(([-.]\\d){2,4})?(\\.exe)?$"; // NOI18N
    private static final String CC_pattern = "([a-zA-z][a-zA-Z0-9_]*-)*CC(([-.]\\d){2,4})?$"; // NOI18N
    private static final String fortran_pattern = "([a-zA-z][a-zA-Z0-9_]*-)*[fg](77|90|95|fortran)(([-.]\\d){2,4})?(\\.exe)?"; // NOI18N
    private static final String make_pattern = "([a-zA-z][a-zA-Z0-9_]*-)*([dg]make|make)(([-.]\\d){2,4})?(\\.exe)?$"; // NOI18N
    private static final String debugger_pattern = "([a-zA-z][a-zA-Z0-9_]*-)*(gdb|dbx)(([-.]\\d){2,4})?(\\.exe)?$"; // NOI18N
    
    /* Legacy defines for CND 5.5 compiler set definitions */
    public static final int SUN_COMPILER_SET = 0;
    public static final int GNU_COMPILER_SET = 1;
    
    public static final String Sun12 = "SunStudio_12"; // NOI18N
    public static final String Sun11 = "SunStudio_11"; // NOI18N
    public static final String Sun10 = "SunStudio_10"; // NOI18N
    public static final String Sun = "SunStudio"; // NOI18N
    public static final String GNU = "GNU"; // NOI18N
    
    private static CompilerFilenameFilter gcc_filter;
    private static CompilerFilenameFilter gpp_filter;
    private static CompilerFilenameFilter cc_filter;
    private static CompilerFilenameFilter CC_filter;
    private static CompilerFilenameFilter fortran_filter;
    private static CompilerFilenameFilter make_filter;
    private static CompilerFilenameFilter debugger_filter;
    
    private ArrayList<CompilerSet> sets = new ArrayList();
    
    private static HashMap<String, CompilerSetManager> managers = new HashMap<String, CompilerSetManager>();
    
    public CompilerSetManager() {
        this("localhost"); // NOI18N
    }
    
    public CompilerSetManager(String key) {
        if (key.equals("localhost")) { // NOI18N
            initCompilerFilters();
            initCompilerSets(Path.getPath());
        } else {
            initRemoteCompilerSets(key);
        }
    }
    
    public CompilerSetManager(ArrayList<CompilerSet> sets) {
        this.sets = sets;
    }
    
    public static CompilerSetManager getDefault() {
	return getDefault("localhost"); // NOI18N
    }
    
    /** @deprecated User CompilerSetManger.getDefault() instead */
    public static CompilerSetManager getDefault(boolean doCreate) {
        return getDefault(null);
    }
    
    /**
     * Find or create a default CompilerSetManager for the given key. A default
     * CSM is one which is active in the system. A non-default is one which gets
     * created but has no affect unless its made default.
     * 
     * For instance, the Build Tools tab (on C/C++ Tools->Options) creates a non-Default
     * CSM and only makes it default if the OK button is pressed. If Cancel is pressed,
     * it never becomes default.
     * 
     * @param key Either user@host or localhost
     * @return A default CompilerSetManager for the given key
     */
    public static synchronized CompilerSetManager getDefault(String key) {
        CompilerSetManager csm = managers.get(key);
        if (csm == null) {
            if (key.equals("localhost")) { // NOI18N
                csm = restoreFromDisk();
                if (csm == null) {
                    csm = new CompilerSetManager();
                    if (csm.getCompilerSets().size() > 0 && !(csm.getCompilerSets().get(0).getName().equals(CompilerSet.None))) {
                        csm.saveToDisk();
                    } else {
                        DialogDescriptor dialogDescriptor = new DialogDescriptor(
                            new NoCompilersPanel(),
                            getString("NO_COMPILERS_FOUND_TITLE"),
                            true,
                            new Object[]{DialogDescriptor.OK_OPTION},
                            DialogDescriptor.OK_OPTION,
                            DialogDescriptor.BOTTOM_ALIGN,
                            null,
                            null);
                        DialogDisplayer.getDefault().notify(dialogDescriptor);
                    }
                }
            } else {
                csm = new CompilerSetManager(key);
            }
            if (csm != null && csm.getCompilerSets().size() == 0) { // No compilers found
                csm.add(CompilerSet.createEmptyCompilerSet());
            }
            managers.put(key, csm);
        }
        return csm;
    }
    
    /**
     * Replace the default CompilerSetManager. Let registered listeners know its been updated.
     */
    public static synchronized void setDefault(CompilerSetManager csm) {
        if (csm.getCompilerSets().size() == 0) { // No compilers found
            csm.add(CompilerSet.createEmptyCompilerSet());
        }
        managers.put("localhost", csm); // NOI18N
    }
    
    public CompilerSetManager deepCopy() {
        // FIXUP: need a real deep copy..
        CompilerSetManager copy = new CompilerSetManager(new ArrayList<CompilerSet>());
        for (CompilerSet set : getCompilerSets()) {
            copy.add(set.createCopy());
        }
        return copy;
    }
    
    public String getUniqueCompilerSetName(String baseName) {
        int n = 0;
        String suggestedName = baseName;
        while (true) {
            suggestedName = baseName + (n > 0 ? ("_" + n) : ""); // NOI18N
            if (getCompilerSet(suggestedName) != null) {
                n++;
            }
            else {
                break;
            }
        }
        return suggestedName;
    }
    
    /** Search $PATH for all desired compiler sets and initialize cbCompilerSet and spCompilerSets */
    public void initCompilerSets(ArrayList<String> dirlist) {
        HashSet flavors = new HashSet();
        
        for (String path : dirlist) {
            if (path.equals("/usr/ucb")) { // NOI18N
                // Don't look here.
                continue;
            }
            if (!IpeUtils.isPathAbsolute(path)) {
                path = FileUtil.normalizeFile(new File(path)).getAbsolutePath();
            }
            File dir = new File(path);
            if (dir.isDirectory()&& isACompilerSetFolder(dir)) {
                ArrayList<String> list = new ArrayList<String>();
                if (new File(dir, "cc").exists()) // NOI18N
                    list.add("cc"); // NOI18N
                if (new File(dir, "gcc").exists()) // NOI18N
                    list.add("gcc"); // NOI18N
                CompilerSet.CompilerFlavor flavor = CompilerSet.getCompilerSetFlavor(dir.getAbsolutePath(), (String[])list.toArray(new String[list.size()]));
                if (flavors.contains(flavor)) {
                    // Skip ...
                    continue;
                }
                flavors.add(flavor);
                CompilerSet cs = null;
                if (path.contains("msys")) { // NOI18N)
                    cs = getCompilerSet(CompilerFlavor.MinGW);
                    if (cs != null          )
                        initCompilerSet(path, cs);
                }
                if (cs == null) {
                    cs = CompilerSet.getCustomCompilerSet(dir.getAbsolutePath(), flavor, flavor.toString());
                    cs.setAutoGenerated(true);
                    initCompilerSet(path, cs);
                    add(cs);
                }
            }
        }
        completeCompilerSets();
    }
    
    /** Initialize remote CompilerSets */
    private void initRemoteCompilerSets(String key) {
        CompilerSetProvider provider = (CompilerSetProvider) Lookup.getDefault().lookup(CompilerSetProvider.class);
        if (provider != null) {
            provider.init(key);
            while (provider.hasMoreCompilerSets()) {
                String data = provider.getNextCompilerSetData();
                int i1 = data.indexOf(';');
                int i2 = data.indexOf(';', i1 + 1);
                String flavor = data.substring(0, i1);
                String path = data.substring(i1 + 1, i2);
                String tools = data.substring(i2 + 1);
                CompilerSet cs = new CompilerSet(CompilerFlavor.toFlavor(flavor), path, flavor);
                StringTokenizer st = new StringTokenizer(tools, ";"); // NOI18N
                while (st.hasMoreTokens()) {
                    String name = st.nextToken();
                    int kind;
                    if (flavor.startsWith("Sun")) { // NOI18N
                        kind = name.equals("CC") ? Tool.CCompiler : Tool.CCCompiler; // NOI18N
                    } else {
                        kind = name.equals("gcc") ? Tool.CCompiler : Tool.CCCompiler; // NOI18N
                    }
                    cs.addTool(name, path + '/' + name, kind);
                }
                add(cs);
            }
        } else {
            throw new IllegalStateException();
        }
    }
    
    public void initCompilerSet(CompilerSet cs) {
        initCompilerSet(cs.getDirectory(), cs);
        completeCompilerSet(cs);
    }
    
    public void reInitCompilerSet(CompilerSet cs, String path) {
        cs.reparent(path);
        initCompilerSet(cs);
    }
    
    private void initCompilerSet(String path, CompilerSet cs) {
        initCompiler(gcc_filter, "gcc", Tool.CCompiler, path, cs); // NOI18N
        initCompiler(gpp_filter, "g++", Tool.CCCompiler, path, cs); // NOI18N
        initCompiler(cc_filter, "cc", Tool.CCompiler, path, cs); // NOI18N
        initFortranCompiler(fortran_filter, Tool.FortranCompiler, path, cs); // NOI18N
        if (Utilities.isUnix()) {  // CC and cc are the same on Windows, so skip this step on Windows
            initCompiler(CC_filter, "CC", Tool.CCCompiler, path, cs); // NOI18N
        }
        initMakeTool(make_filter, Tool.MakeTool, path, cs); // NOI18N
        initDebuggerTool(debugger_filter, Tool.DebuggerTool, path, cs); // NOI18N
    }
    
    /**
     * Check whether folder is a compilerset. It needs at least one C or C++ compiler.
     * @param folder
     * @return
     */
    private boolean isACompilerSetFolder(File folder) {
        String[] compilerNames = new String[] {"gcc", "g++", "cc", "CC"}; // NOI18N
        if (folder.getPath().contains("msys") && new File(folder, "make.exe").exists()) { // NOI18N
            return true;
        }
        for (int i = 0; i < compilerNames.length; i++) {
            if (new File(folder, compilerNames[i]).exists() || new File(folder, compilerNames[i] + ".exe").exists()) // NOI18N
                return true;
        }
        return false;
    }
    
    private void initCompiler(CompilerFilenameFilter filter, String best, int kind, String path, CompilerSet cs) {
        File dir = new File(path);
        String[] list = dir.list(filter);

        if (list != null && list.length > 0) {
            if (cs == null) {
                CompilerFlavor flavor = CompilerSet.getCompilerSetFlavor(dir.getAbsolutePath(), list);
                cs = getCompilerSet(flavor);
                if (cs != null && !cs.getDirectory().equals(path))
                    return;
                cs = CompilerSet.getCompilerSet(dir.getAbsolutePath(), list);
                add(cs);
                if (cs.findTool(kind) != null) {
                    // Only one tool of each kind in a cs
                    return;
                }
            }
            for (String name : list) {
                File file = new File(dir, name);
                if (file.exists() && !file.isDirectory() && (name.equals(best) || name.equals(best + ".exe"))) { // NOI18N
                    cs.addTool(name, path, kind);
                    break;
                }
            }
        }
    }
    
    private void initFortranCompiler(CompilerFilenameFilter filter, int kind, String path, CompilerSet cs) {
        File dir = new File(path);
        String[] list = dir.list(filter);
        String[] best = {
            "gfortran", // NOI18N
            "g95", // NOI18N
            "g90", // NOI18N
            "g77", // NOI18N
            "ffortran", // NOI18N
            "f95", // NOI18N
            "f90", // NOI18N
            "f77", // NOI18N
        };

        if (list != null && list.length > 0) {
            if (cs == null) {
                CompilerFlavor flavor = CompilerSet.getCompilerSetFlavor(dir.getAbsolutePath(), list);
                cs = getCompilerSet(flavor);
                if (cs != null && !cs.getDirectory().equals(path))
                    return;
                cs = CompilerSet.getCompilerSet(dir.getAbsolutePath(), list);
                add(cs);
        //            for (String name : list) {
        //                File file = new File(dir, name);
        //                if (file.exists()) {
        //                    for (int i = 0; i < best.length; i++) {
        //                        if (name.equals(best[i]) || name.equals(best[i] + ".exe")) { // NOI18N
        //                            cs.addTool(name, path, kind);
        //                        }
        //                    }
        //                }
        //            }
            }
            for (int i = 0; i < best.length; i++) {
                String name = best[i];
                if (Utilities.isWindows()) {
                    name = name + ".exe"; // NOI18N
                }
                if (new File(dir, name).exists() && !new File(dir, name).isDirectory()) { // NOI18N
                    cs.addTool(name, path, kind);
                    return;
                }
            }
        }
    }
    
    private void initMakeTool(CompilerFilenameFilter filter, int kind, String path, CompilerSet cs) {
        File dir = new File(path);
        String[] list = dir.list(filter);
        String[] best = {
            "dmake", // NOI18N
            "gmake", // NOI18N
            "make", // NOI18N
        };

        if (list != null && list.length > 0) {
            if (cs == null) {
                cs = null;
                if (path.contains("msys")) { // NOI18N
                    // use minGW cs
                    cs = getCompilerSet(CompilerFlavor.MinGW);
                }
                if (cs == null) {
                    CompilerFlavor flavor = CompilerSet.getCompilerSetFlavor(dir.getAbsolutePath(), list);
                    cs = getCompilerSet(flavor);
                    if (cs != null && !cs.getDirectory().equals(path))
                        return;
                    cs = CompilerSet.getCompilerSet(dir.getAbsolutePath(), list);
                        add(cs);
                }
            }
            for (int i = 0; i < best.length; i++) {
                String name = best[i];
                if (Utilities.isWindows()) {
                    name = name + ".exe"; // NOI18N
                }
                if (new File(dir, name).exists() && !new File(dir, name).isDirectory()) { // NOI18N
                    cs.addTool(name, path, kind);
                    return;
                }
            }
        }
    }
    
    private void initDebuggerTool(CompilerFilenameFilter filter, int kind, String path, CompilerSet cs) {
        File dir = new File(path);
        String[] list = dir.list(filter);
        String[] best;
        if (IpeUtils.isGdbEnabled()) {
            best = new String[] {
                "gdb", // NOI18N
            };
        }
        else {
            // Assume dbxgui
            best = new String[] {
                "dbx", // NOI18N
            };
        }

        if (list != null && list.length > 0) {
            if (cs == null) {
                CompilerFlavor flavor = CompilerSet.getCompilerSetFlavor(dir.getAbsolutePath(), list);
                cs = getCompilerSet(flavor);
                if (cs != null && !cs.getDirectory().equals(path))
                    return;
                cs = CompilerSet.getCompilerSet(dir.getAbsolutePath(), list);
                add(cs);
            }
            for (int i = 0; i < best.length; i++) {
                String name = best[i];
                if (Utilities.isWindows()) {
                    name = name + ".exe"; // NOI18N
                }
                if (new File(dir, name).exists() && !new File(dir, name).isDirectory()) { // NOI18N
                    cs.addTool(name, path, kind);
                    return;
                }
            }
        }
    }
    
    /**
     * If a compiler set doesn't have one of each compiler types, add a "No compiler"
     * tool. If selected, this will tell the build validation things are OK.
     */
    private void completeCompilerSets() {
        CompilerSet sun = getCompilerSet(CompilerFlavor.Sun);
        if (sun == null) {
            // find 'best' Sun set and copy it
            sun = getCompilerSet(CompilerFlavor.Sun12);
            if (sun == null)
                sun = getCompilerSet(CompilerFlavor.Sun11);
            if (sun == null)
                sun = getCompilerSet(CompilerFlavor.Sun10);
            if (sun == null)
                sun = getCompilerSet(CompilerFlavor.Sun9);
            if (sun == null)
                sun = getCompilerSet(CompilerFlavor.Sun8);
            if (sun != null) {
                sun = sun.createCopy();
                sun.setName(CompilerFlavor.Sun.toString());
                sun.setFlavor(CompilerFlavor.Sun);
                sun.setAsDefault(false);
                sun.setAutoGenerated(true);
                add(sun);
            }
        }
        
        for (CompilerSet cs : sets) {
            completeCompilerSet(cs);
        }
        
        if (sets.size() == 0) { // No compilers found
            add(CompilerSet.createEmptyCompilerSet());
        }
    }
    
    private static void completeCompilerSet(CompilerSet cs) {
        if (cs.getTool(Tool.CCompiler) == null) {
            cs.addTool("", "", Tool.CCompiler); // NOI18N
        }
        if (cs.getTool(Tool.CCCompiler) == null) {
            cs.addTool("", "", Tool.CCCompiler); // NOI18N
        }
        if (cs.getTool(Tool.FortranCompiler) == null) {
            cs.addTool("", "", Tool.FortranCompiler); // NOI18N
        }
//        if (cs.getTool(Tool.CustomTool) == null) {
//            cs.addTool("", "", Tool.CustomTool); // NOI18N
//        }
        if (cs.findTool(Tool.MakeTool) == null) {
            String path = Path.findCommand("make"); // NOI18N
            if (path != null)
                cs.addNewTool(IpeUtils.getBaseName(path), IpeUtils.getDirName(path), Tool.MakeTool); // NOI18N
        }
        if (cs.getTool(Tool.MakeTool) == null) {
                cs.addTool("", "", Tool.MakeTool); // NOI18N
        }
        if (cs.findTool(Tool.DebuggerTool) == null) {
            String path;
            if (IpeUtils.isGdbEnabled()) {
                path = Path.findCommand("gdb"); // NOI18N
            }
            else {
                path = Path.findCommand("dbx"); // NOI18N
            }
            if (path != null)
                cs.addNewTool(IpeUtils.getBaseName(path), IpeUtils.getDirName(path), Tool.DebuggerTool); // NOI18N
        }
        if (cs.getTool(Tool.DebuggerTool) == null) {
                cs.addTool("", "", Tool.DebuggerTool); // NOI18N
        }
        
    }
    
    private void initCompilerFilters() {
        gcc_filter = new CompilerFilenameFilter(gcc_pattern);
        gpp_filter = new CompilerFilenameFilter(gpp_pattern);
        cc_filter = new CompilerFilenameFilter(cc_pattern);
        fortran_filter = new CompilerFilenameFilter(fortran_pattern);
        if (Utilities.isUnix()) {
            CC_filter = new CompilerFilenameFilter(CC_pattern);
        }
        make_filter = new CompilerFilenameFilter(make_pattern);
        debugger_filter = new CompilerFilenameFilter(debugger_pattern);
    }
    
    /**
     * Add a CompilerSet to this CompilerSetManager. Make sure it doesn't get added multiple times.
     *
     * @param cs The CompilerSet to (possibly) add
     */
    public void add(CompilerSet cs) {
//        String csdir = cs.getDirectory();
        
        if (sets.size() == 1 && sets.get(0).getName().equals(CompilerSet.None)) {
            sets.remove(0);
        }
//        if (cs.isAutoGenerated()) {
//            for (CompilerSet cs2 : sets) {
//                if (cs2.getDirectory().equals(csdir)) {
//                    return;
//                }
//            }
//        }
        sets.add(cs);
        if (sets.size() == 1) {
            setDefault(cs);
        }
    }
    
    /**
     * Remove a CompilerSet from this CompilerSetManager. Use caution with this method. Its primary
     * use is to remove temporary CompilerSets which were added to represent missing compiler sets. In
     * that context, they're removed immediately after showing the ToolsPanel after project open.
     *
     * @param cs The CompilerSet to (possibly) remove
     */
    public void remove(CompilerSet cs) {
        if (sets.contains(cs)) {
            sets.remove(cs);
//            CompilerSet.removeCompilerSet(cs); // has it's own cache!!!!!!
//            if (CppSettings.getDefault().getCompilerSetName().equals(cs.getName())) {
//                CppSettings.getDefault().setCompilerSetName("");
//            }
//            if (this == instance) {
//                fireCompilerSetChangeNotification(instance);
//            }
        }
//        if (sets.size() == 0) { // No compilers found
//            add(CompilerSet.createEmptyCompilerSet());
//        }
    }
    
    public CompilerSet getCompilerSet(CompilerFlavor flavor) {
        return getCompilerSet(flavor.toString());
    }
    
    public CompilerSet getCompilerSet(String name) {
        for (CompilerSet cs : sets) {
            if (cs.getName().equals(name)) {
                return cs;
            }
        }
        return null;
    }
    
    public CompilerSet getCompilerSetByDisplayName(String name) {
        for (CompilerSet cs : sets) {
            if (cs.getDisplayName().equals(name)) {
                return cs;
            }
        }
        return null;
    }
    
    public CompilerSet getCompilerSetByPath(String path) {
        for (CompilerSet cs : sets) {
            if (cs.getDirectory().equals(path)) {
                return cs;
            }
        }
        return null;
    }
        
    public CompilerSet getCompilerSet(String name, String dname) {
        for (CompilerSet cs : sets) {
            if (cs.getName().equals(name) && cs.getDisplayName().equals(dname)) {
                return cs;
            }
        }
        return null;
    }

    public CompilerSet getCompilerSet(int idx) {
        if (idx >= 0 && idx < sets.size())
            return sets.get(idx);
        else
            return null;
    }
    
    public List<CompilerSet> getCompilerSets() {
        return sets;
    }
    
    public List<String> getCompilerSetDisplayNames() {
        ArrayList<String> names = new ArrayList();
        for (CompilerSet cs : getCompilerSets()) {
            names.add(cs.getDisplayName());
        }
        return names;
    }
    
    public List<String> getCompilerSetNames() {
        ArrayList<String> names = new ArrayList();
        for (CompilerSet cs : getCompilerSets()) {
            names.add(cs.getName());
        }
        return names;
    }
    
    public void setDefault(CompilerSet newDefault) {
        boolean set = false;
        for (CompilerSet cs : getCompilerSets()) {
            cs.setAsDefault(false);
            if (cs == newDefault) {
                newDefault.setAsDefault(true);
                set = true;
            }
        }
        if (!set && sets.size() > 0) {
            getCompilerSet(0).setAsDefault(true);
        }
    }
    
    public CompilerSet getDefaultCompilerSet() {
        for (CompilerSet cs : getCompilerSets()) {
            if (cs.isDefault())
                return cs;
        }
        return null;
    }
    
    /**
     * Check if the gdb module is enabled. Don't show the gdb line if it isn't.
     *
     * @return true if the gdb module is enabled, false if missing or disabled
     */
    protected boolean isGdbEnabled() {
        Iterator iter = Lookup.getDefault().lookup(new Lookup.Template(ModuleInfo.class)).allInstances().iterator();
        while (iter.hasNext()) {
            ModuleInfo info = (ModuleInfo) iter.next();
            if (info.getCodeNameBase().equals("org.netbeans.modules.cnd.debugger.gdb") && info.isEnabled()) { // NOI18N
                return true;
            }
        }
        return false;
    }
//    
//    public static void addCompilerSetChangeListener(CompilerSetChangeListener l) {
//        listeners.add(l);
//    }
//    
//    public static void removeCompilerSetChangeListener(CompilerSetChangeListener l) {
//        listeners.remove(l);
//    }
//    
//    private static void fireCompilerSetChangeNotification(CompilerSetManager csm) {
//        for (CompilerSetChangeListener l : listeners) {
//            l.compilerSetChange(new CompilerSetEvent(csm));
//        }
//    }
    
    /** Special FilenameFilter which should recognize different variations of supported compilers */
    private class CompilerFilenameFilter implements FilenameFilter {
        
        Pattern pc = null;
        
        public CompilerFilenameFilter(String pattern) {
            try {
                pc = Pattern.compile(pattern);
            } catch (PatternSyntaxException ex) {
            }
        }
        
        public boolean accept(File dir, String name) {
            return pc != null && pc.matcher(name).matches();
        }
    }
    
    
    
    /*
     * Persistence ...
     */
    
    private static Preferences getPreferences() {
        return NbPreferences.forModule(CompilerSetManager.class);
    }
    
    private static String NO_SETS = "csm.noOfSets"; // NOI18N
    private static String SET_NAME = "csm.setName"; // NOI18N
    private static String SET_FLAVOR = "csm.setFlavor"; // NOI18N
    private static String SET_DIRECTORY = "csm.setDirectory"; // NOI18N
    private static String SET_AUTO = "csm.autoGenerated"; // NOI18N
    private static String NO_TOOLS = "csm.noOfTools"; // NOI18N
    private static String TOOL_NAME = "csm.toolName"; // NOI18N
    private static String TOOL_DISPLAYNAME = "csm.toolDisplayName"; // NOI18N
    private static String TOOL_KIND = "csm.toolKind"; // NOI18N
    private static String TOOL_PATH = "csm.toolPath"; // NOI18N
    private static String TOOL_FLAVOR = "csm.toolFlavor"; // NOI18N
    
    public void saveToDisk() {
        getPreferences().putInt(NO_SETS, sets.size());
        int setCount = 0;
        for (CompilerSet cs : getCompilerSets()) {
            getPreferences().put(SET_NAME + '.' + setCount, cs.getName());
            getPreferences().put(SET_FLAVOR + '.' + setCount, cs.getCompilerFlavor().toString());
            getPreferences().put(SET_DIRECTORY + '.' + setCount, cs.getDirectory());
            getPreferences().putBoolean(SET_AUTO + '.' + setCount, cs.isAutoGenerated());
            List<Tool> tools = cs.getTools();
            getPreferences().putInt(NO_TOOLS + '.' + setCount, tools.size());
            int toolCount = 0;
            for (Tool tool : tools) {
                getPreferences().put(TOOL_NAME + '.' + setCount+ '.' + toolCount, tool.getName());
                getPreferences().put(TOOL_DISPLAYNAME + '-' + setCount+ '.' + toolCount, tool.getDisplayName());
                getPreferences().putInt(TOOL_KIND + '.' + setCount+ '.' + toolCount, tool.getKind());
                getPreferences().put(TOOL_PATH + '.' + setCount+ '.' + toolCount, tool.getPath());
                getPreferences().put(TOOL_FLAVOR + '.' + setCount+ '.' + toolCount, tool.getFlavor().toString());
                toolCount++;
            }
            setCount++;
        }
    }
    
    private static CompilerProvider compilerProvider = null;
    private static CompilerProvider getCompilerProvider() {
        if (compilerProvider == null) {
            compilerProvider = (CompilerProvider) Lookup.getDefault().lookup(CompilerProvider.class);
        }
        if (compilerProvider == null) {
            compilerProvider = new DefaultCompilerProvider();
        }
        return compilerProvider;
    }
        
    public static CompilerSetManager restoreFromDisk() {
        ArrayList<CompilerSet> css = new ArrayList<CompilerSet>();
        int noSets = getPreferences().getInt(NO_SETS, -1);
        if (noSets < 0) {
            return null;
        } 
        for (int setCount = 0; setCount < noSets; setCount++) {
            String setName = getPreferences().get(SET_NAME + '.' + setCount, null);
            String setFlavorName = getPreferences().get(SET_FLAVOR + '.' + setCount, null);
            CompilerFlavor flavor = null;
            if (setFlavorName != null) {
                flavor = CompilerFlavor.toFlavor(setFlavorName);
            }
            String setDirectory = getPreferences().get(SET_DIRECTORY + '.' + setCount, null);
            if (setName == null || setFlavorName == null || flavor == null) {
                // FIXUP: error
                continue;
            }
            Boolean auto = getPreferences().getBoolean(SET_AUTO + '.' + setCount, false);
            CompilerSet cs = new CompilerSet(flavor, setDirectory, setName);
            cs.setAutoGenerated(auto);
            int noTools = getPreferences().getInt(NO_TOOLS + '.' + setCount, -1);
            for (int toolCount = 0; toolCount < noTools; toolCount++) {
                String toolName = getPreferences().get(TOOL_NAME + '.' + setCount + '.' + toolCount, null);
                String toolDisplayName = getPreferences().get(TOOL_DISPLAYNAME + '-' + setCount+ '.' + toolCount, null);
                int toolKind = getPreferences().getInt(TOOL_KIND + '.' + setCount + '.' + toolCount, -1);
                String toolPath = getPreferences().get(TOOL_PATH + '.' + setCount + '.' + toolCount, null);
                String toolFlavorName = getPreferences().get(TOOL_FLAVOR + '.' + setCount + '.' + toolCount, null);
                CompilerFlavor toolFlavor = null;
                if (toolFlavorName != null) {
                    toolFlavor = CompilerFlavor.toFlavor(toolFlavorName);
                }
                Tool tool = getCompilerProvider().createCompiler(toolFlavor, toolKind, "", toolDisplayName, toolPath);
                tool.setName(toolName);
                cs.addTool(tool);
            }
            completeCompilerSet(cs);
            css.add(cs);
        }
        
        return new CompilerSetManager(css);
    }
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(CompilerSetManager.class, s);
    }
}
