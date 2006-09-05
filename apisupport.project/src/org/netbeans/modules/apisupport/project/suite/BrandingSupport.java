/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.apisupport.project.suite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Provide set of helper methods for branding purposes.
 * @author Radek Matous
 */
public final class BrandingSupport {
    
    private final SuiteProject suiteProject;
    private final SuiteProperties suiteProperties;    
    private Set brandedModules;
    private Set brandedBundleKeys;
    private Set brandedFiles;
    
    private NbPlatform platform;
    private final File brandingDir;
    
    public static final String BRANDING_DIR_PROPERTY = "branding.dir"; // NOI18N
    private static final String BUNDLE_NAME = "Bundle.properties"; //NOI18N

    public static BrandingSupport getInstance(final SuiteProperties suiteProperties) throws IOException {
        return new BrandingSupport(suiteProperties);
    }
        
    private BrandingSupport(final SuiteProperties suiteProperties) throws IOException {
        this.suiteProperties = suiteProperties;
        this.suiteProject = suiteProperties.getProject();
        File suiteDir = suiteProject.getProjectDirectoryFile();
        assert suiteDir != null && suiteDir.exists();
        brandingDir = new File(suiteDir, getNameOfBrandingFolder());//NOI18N
        init();        
    }        
    
    /**
     * @return the project directory beneath which everything in the project lies
     */
    public File getProjectDirectory() {
        return suiteProject.getProjectDirectoryFile();
    }
    
    /**
     * @return the top-level branding directory
     */
    public File getBrandingRoot() {
        return new File(getProjectDirectory(), getNameOfBrandingFolder());
    }
    
    /**
     * @return the branding directory for NetBeans module represented as
     * <code>ModuleEntry</code>
     */
    public File getModuleEntryDirectory(ModuleEntry mEntry) {
        String relativePath;
        relativePath = PropertyUtils.relativizeFile( mEntry.getClusterDirectory(),
                mEntry.getJarLocation());
        return new File(getBrandingRoot(),relativePath);
    }
    
    /**
     * @return the file representing localizing bundle for NetBeans module
     */
    public  File getLocalizingBundle(final ModuleEntry mEntry) {
        ManifestManager mfm = ManifestManager.getInstanceFromJAR(mEntry.getJarLocation());
        File bundle = null;
        if (mfm != null) {
            String bundlePath = mfm.getLocalizingBundle();
            if (bundlePath != null) {
                bundle = new File(getModuleEntryDirectory(mEntry),bundlePath);
            }
        }
        return bundle;
    }
    
    public boolean isBranded(final BundleKey key) {
        boolean retval = getBrandedBundleKeys().contains(key);
        return retval;
        
    }
    
    public boolean isBranded(final BrandedFile bFile) {
        boolean retval = getBrandedFiles().contains(bFile);
        return retval;
        
    }
    
    
    /**
     * @return true if NetBeans module is already branded
     */
    public boolean isBranded(final ModuleEntry entry) {
        boolean retval = getBrandedModules().contains(entry);
        assert (retval == getModuleEntryDirectory(entry).exists());
        return retval;
    }
    
    public Set getBrandedModules() {
        return brandedModules;
    }
    
    public Set getBrandedBundleKeys() {
        return brandedBundleKeys;
    }
    
    public Set getBrandedFiles() {
        return brandedFiles;
    }
    
    public Set getLocalizingBundleKeys(final String moduleCodeNameBase, final Set keys) {
        ModuleEntry foundEntry = getModuleEntry(moduleCodeNameBase);
        return (foundEntry != null) ? getLocalizingBundleKeys(foundEntry, keys) : null;
    }
    
    public Set getLocalizingBundleKeys(final ModuleEntry moduleEntry, final Set keys) {
        Set retval = new HashSet();
        for (Iterator it = getBrandedBundleKeys().iterator();
        it.hasNext() && retval.size() != keys.size();) {
            BundleKey bKey = (BundleKey)it.next();
            if (keys.contains(bKey.getKey())) {
                retval.add(bKey);
            }
        }
        
        if (retval.size() != keys.size()) {
            loadLocalizedBundlesFromPlatform(moduleEntry, keys, retval);
        }
        return (retval.size() != keys.size()) ? null : retval;
    }
    
    public BrandedFile getBrandedFile(final String moduleCodeNameBase, final String entryPath) {
        ModuleEntry foundEntry = getModuleEntry(moduleCodeNameBase);
        return (foundEntry != null) ? getBrandedFile(foundEntry,entryPath) : null;
    }
    
    public BrandedFile getBrandedFile(final ModuleEntry moduleEntry, final String entryPath) {
        BrandedFile retval = null;
        try {
            retval = new BrandedFile(moduleEntry, entryPath);
            for (Iterator it = getBrandedFiles().iterator();it.hasNext() ;) {
                BrandedFile bFile = (BrandedFile)it.next();
                
                if (retval.equals(bFile)) {
                    retval = bFile;
                    
                }
            }
        } catch (MalformedURLException ex) {
            retval = null;
        }
        return retval;
    }
    
    public BundleKey getBundleKey(final String moduleCodeNameBase,
            final String bundleEntry,final String key) {
        Set keys = new HashSet();
        keys.add(key);
        keys = getBundleKeys(moduleCodeNameBase,bundleEntry, keys);
        return (keys == null) ? null : (BrandingSupport.BundleKey) keys.toArray()[0];
    }
    
    public Set getBundleKeys(final String moduleCodeNameBase, final String bundleEntry,final Set keys) {
        ModuleEntry foundEntry = getModuleEntry(moduleCodeNameBase);
        return (foundEntry != null) ? getBundleKeys(foundEntry,bundleEntry,  keys) : null;
    }
    
    public Set getBundleKeys(final ModuleEntry moduleEntry, final String bundleEntry, final Set keys) {
        Set retval = new HashSet();
        for (Iterator it = getBrandedBundleKeys().iterator();
        it.hasNext() && retval.size() != keys.size();) {
            BundleKey bKey = (BundleKey)it.next();
            if (keys.contains(bKey.getKey())) {
                retval.add(bKey);
            } 
        }
        
        if (retval.size() != keys.size()) {
            try {
                loadLocalizedBundlesFromPlatform(moduleEntry, bundleEntry, keys, retval);
            } catch (IOException ex) {
                //ex.printStackTrace();
                throw new IllegalStateException();
            }
        }
                    
        return (retval.size() != keys.size()) ? null : retval;
    }
    
    private ModuleEntry getModuleEntry(final String moduleCodeNameBase) {
        NbPlatform platform;
        platform = getActivePlatform();
        for (Iterator it = Arrays.asList(platform.getModules()).iterator(); it.hasNext();) {
            ModuleEntry entry = (ModuleEntry)it.next();
            if (entry.getCodeNameBase().equals(moduleCodeNameBase)) {
                return entry;
            }
        }
        
        return null;
    }

    private NbPlatform getActivePlatform() {
        NbPlatform retval = suiteProperties.getActivePlatform();
        if (retval != null) {
            return retval;
        } else {
            return NbPlatform.getDefaultPlatform();
        }
    }
    
    public void brandFile(final BrandedFile bFile) throws IOException {
        if (!bFile.isModified()) return;
        
        File target = bFile.getFileLocation();
        if (!target.exists()) {
            target.getParentFile().mkdirs();
            target.createNewFile();
        }
        
        assert target.exists();
        
        InputStream is = null;
        OutputStream os = null;
        try {
            is = bFile.getBrandingSource().openStream();
            os = new FileOutputStream(target);
            FileUtil.copy(is, os);
        } finally {
            if (is != null) {
                is.close();
            }
            
            if (os != null) {
                os.close();
            }
            
            brandedFiles.add(bFile);
            bFile.modified = false;            
        }
    }
    
    public void brandFile(final BrandedFile bFile, final Runnable saveTask) throws IOException {
        if (!bFile.isModified()) return;
        
        saveTask.run();
        brandedFiles.add(bFile);
        bFile.modified = false;
    }
    
    public void brandBundleKey(final BundleKey bundleKey) throws IOException {
        if (bundleKey == null) {
            return;
        }
        Set keys = new HashSet();
        keys.add(bundleKey);
        brandBundleKeys(keys);
    }
    
    public void brandBundleKeys(final Set bundleKeys) throws IOException {
        init();
        Map mentryToEditProp = new HashMap();
        for (Iterator it = bundleKeys.iterator();it.hasNext();) {
            BundleKey bKey = (BundleKey)it.next();
            if (bKey.isModified()) {
                EditableProperties ep = (EditableProperties)mentryToEditProp.get(bKey.getBrandingBundle());
                if (ep == null) {
                    File bundle = bKey.getBrandingBundle();
                    if (!bundle.exists()) {
                        bundle.getParentFile().mkdirs();
                        bundle.createNewFile();
                    }
                    ep = getEditableProperties(bundle);
                    mentryToEditProp.put(bKey.getBrandingBundle(), ep);
                }
                ep.setProperty(bKey.getKey(), bKey.getValue());
            }
        }
        
        for (Iterator it = mentryToEditProp.entrySet().iterator();it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            File bundle = (File) entry.getKey();
            assert bundle.exists();
            storeEditableProperties((EditableProperties) entry.getValue(), bundle);
            for (Iterator it2 = bundleKeys.iterator();it2.hasNext();) {
                BundleKey bKey = (BundleKey)it2.next();
                File bundle2 = bKey.getBrandingBundle();
                if (bundle2.equals(bundle)) {
                    brandedBundleKeys.add(bKey);
                    bKey.modified = false;
                    brandedModules.add(bKey.getModuleEntry());
                }
            }
        }
    }
    
    private void init() throws IOException {
        NbPlatform newPlatform = getActivePlatform();
        
        if (brandedModules == null || !newPlatform.equals(platform)) {
            brandedModules = new HashSet();
            brandedBundleKeys = new HashSet();
            brandedFiles = new HashSet();
            platform = newPlatform;
            
            if (brandingDir.exists()) {
                assert brandingDir.isDirectory();
                scanModulesInBrandingDir(brandingDir, platform.getModules());
            }
        }
    }
    
    private  void scanModulesInBrandingDir(final File srcDir, final ModuleEntry[] platformModules) throws IOException  {
        if (srcDir.getName().endsWith(".jar")) {//NOI18N
            ModuleEntry foundEntry = null;
            for (int i = 0; i < platformModules.length; i++){
                if (isBrandingForModuleEntry(srcDir, platformModules[i])) {
                    scanBrandedFiles(srcDir, platformModules[i]);
                    
                    foundEntry = platformModules[i];
                    break;
                }
            }
            if (foundEntry != null) {
                brandedModules.add(foundEntry);
            }
        } else {
            String[] kids = srcDir.list();
            assert (kids != null);
            
            for (int i = 0; i < kids.length; i++) {
                File kid = new File(srcDir, kids[i]);
                if (!kid.isDirectory()) {
                    continue;
                }
                scanModulesInBrandingDir(kid, platformModules);
            }
        }
    }
    
    private void scanBrandedFiles(final File srcDir, final ModuleEntry mEntry) throws IOException {
        String[] kids = srcDir.list();
        assert (kids != null);
        
        for (int i = 0; i < kids.length; i++) {
            File kid = new File(srcDir, kids[i]);
            if (!kid.isDirectory()) {
                if (kid.getName().endsWith(BUNDLE_NAME)) {
                    loadBundleKeys(mEntry, kid);
                } else {
                    loadBrandedFiles(mEntry, kid);
                }
                
                continue;
            }
            scanBrandedFiles(kid, mEntry);
        }
    }
    
    private void loadBundleKeys(final ModuleEntry mEntry,
            final File bundle) throws IOException {
        
        EditableProperties p = getEditableProperties(bundle);
        
        for (Iterator it = p.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            brandedBundleKeys.add(new BundleKey(mEntry, bundle,(String)entry.getKey(), (String)entry.getValue()));
        }
    }
    
    private void loadBrandedFiles(final ModuleEntry mEntry,
            final File file) throws IOException {
        
        String entryPath = PropertyUtils.relativizeFile(getModuleEntryDirectory(mEntry),file);
        BrandedFile bf = new BrandedFile(mEntry, file.toURI().toURL(), entryPath);
        brandedFiles.add(bf);
    }
    
    
    private static EditableProperties getEditableProperties(final File bundle) throws IOException {
        EditableProperties p = new EditableProperties(true);
        InputStream is;
        is = new FileInputStream(bundle);
        try {
            p.load(is);
        } finally {
            is.close();
        }
        
        
        return p;
    }
    
    private static void storeEditableProperties(final EditableProperties p, final File bundle) throws IOException {
        OutputStream os;
        os = new FileOutputStream(bundle);
        try {
            p.store(os);
        } finally {
            os.close();
        }
    }
    
    
    private void loadLocalizedBundlesFromPlatform(final ModuleEntry moduleEntry, final Set keys, final Set bundleKeys) {
        EditableProperties p;
        p = ModuleList.loadBundleInfo(moduleEntry.getSourceLocation()).toEditableProperties();
        for (Iterator it = p.keySet().iterator(); it.hasNext(); ) {
            String key = (String)it.next();
            if (keys.contains(key)) {
                String value = (String)p.getProperty(key);
                bundleKeys.add(new BundleKey(moduleEntry, key, value));
            }
        }
    }
    
    private void loadLocalizedBundlesFromPlatform(final ModuleEntry moduleEntry,
            final String bundleEntry, final Set keys, final Set bundleKeys) throws IOException {
        Properties p = new Properties();
        JarFile module = new JarFile(moduleEntry.getJarLocation());
        JarEntry je = module.getJarEntry(bundleEntry);
        InputStream is = module.getInputStream(je);
        File bundle = new File(getModuleEntryDirectory(moduleEntry),bundleEntry);
        try {
            
            p.load(is);
        } finally {
            is.close();
        }
        for (Iterator it = p.keySet().iterator(); it.hasNext(); ) {
            String key = (String)it.next();
            if (keys.contains(key)) {
                String value = (String)p.getProperty(key);
                bundleKeys.add(new BundleKey(moduleEntry, bundle, key, value));
            } 
        }
    }
    
    
    private boolean isBrandingForModuleEntry(final File srcDir, final ModuleEntry mEntry) {
        boolean retval = mEntry.getJarLocation().getName().equals(srcDir.getName());
        if (retval) {
            String relPath1 = PropertyUtils.relativizeFile( mEntry.getClusterDirectory(), mEntry.getJarLocation().getParentFile());
            String relPath2 = PropertyUtils.relativizeFile(brandingDir, srcDir.getParentFile());
            
            retval = relPath1.equals(relPath2);
        }
        return retval;
    }
    
    public final class BundleKey {
        private final File brandingBundle;
        private final ModuleEntry moduleEntry;
        private final String key;
        private String value;
        private boolean modified = false;
        
        private BundleKey(final ModuleEntry moduleEntry, final File brandingBundle, final String key, final String value) {
            this.moduleEntry = moduleEntry;
            this.key = key;
            this.value = value;
            this.brandingBundle = brandingBundle;
        }
        
        private BundleKey(final ModuleEntry mEntry, final String key, final String value) {
            this(mEntry, getLocalizingBundle(mEntry), key,value);
        }
        
        public ModuleEntry getModuleEntry() {
            return moduleEntry;
        }
        
        public String getKey() {
            return key;
        }
        
        public String getValue() {
            return value;
        }
        
        public void setValue(final String value) {
            if (!this.value.equals(value)) {
                modified = true;
            }
            this.value = value;
        }
        
        public boolean equals(Object obj) {
            boolean retval = false;
            
            if (obj instanceof BundleKey) {
                BundleKey bKey = (BundleKey)obj;
                retval = getKey().equals(bKey.getKey())
                && getModuleEntry().equals(bKey.getModuleEntry())
                && getBrandingBundle().equals(bKey.getBrandingBundle());
            }
            
            return  retval;
        }
        
        public int hashCode() {
            return 0;
        }
        
        boolean isModified() {
            return modified;
        }
        
        public File getBrandingBundle() {
            return brandingBundle;
        }

    }
    
    public class BrandedFile {
        private final ModuleEntry moduleEntry;
        private final String entryPath;
        private URL brandingSource;
        private boolean modified = false;
        
        private BrandedFile(final ModuleEntry moduleEntry, final String entry) throws MalformedURLException {
            this(moduleEntry, null, entry);
        }
        
        private BrandedFile(final ModuleEntry moduleEntry, final URL source, final String entry) throws MalformedURLException {
            this.moduleEntry = moduleEntry;
            this.entryPath = entry;
            if (source == null) {
                brandingSource = moduleEntry.getJarLocation().toURI().toURL();
                brandingSource =  new URL("jar:" + brandingSource + "!/" + entryPath); // NOI18N
            } else {
                brandingSource = source;
            }
            
        }
        
        public ModuleEntry getModuleEntry() {
            return moduleEntry;
        }
        
        public String getEntryPath() {
            return entryPath;
        }
        
        public File getFileLocation() {
            return new File(getModuleEntryDirectory(getModuleEntry()), getEntryPath());
        }
        
        public URL getBrandingSource()  {
            return brandingSource;
        }
        
        public void setBrandingSource(URL brandingSource) {
            if (!Utilities.compareObjects(brandingSource, this.brandingSource)) {
                modified = true;
            }
            this.brandingSource = brandingSource;
        }
        
        public void setBrandingSource(File brandingFile) throws MalformedURLException {
            setBrandingSource(brandingFile.toURI().toURL());
        }
        
        public boolean isModified() {
            return modified;
        }
        
        public boolean equals(Object obj) {
            boolean retval = false;
            
            if (obj instanceof BrandedFile) {
                BrandedFile bFile = (BrandedFile)obj;
                retval = getModuleEntry().equals(bFile.getModuleEntry())
                && getFileLocation().equals(bFile.getFileLocation());
            }
            
            //if ()
            return  retval;
        }

        public int hashCode() {
            return 0;
        }
        
    }

    public String getNameOfBrandingFolder() {
        return suiteProject.getEvaluator().getProperty(BRANDING_DIR_PROPERTY);
    }
    
}
