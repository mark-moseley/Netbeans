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

package org.netbeans.modules.autoupdate.services;

import java.text.ParseException;
import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.core.startup.Main;
import org.netbeans.core.startup.TopLogging;
import org.netbeans.spi.autoupdate.KeyStoreProvider;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.updater.ModuleDeactivator;
import org.netbeans.updater.ModuleUpdater;
import org.netbeans.updater.UpdateTracking;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Dependency;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Jiri Rechtacek, Radek Matous
 */
public class Utilities {

    private Utilities() {}

    public static final String UPDATE_DIR = "update"; // NOI18N
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String DOWNLOAD_DIR = UPDATE_DIR + FILE_SEPARATOR + "download"; // NOI18N
    public static final String NBM_EXTENTSION = ".nbm";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat ("yyyy/MM/dd"); // NOI18N
    public static final String ATTR_VISIBLE = "AutoUpdate-Show-In-Client";
    public static final String ATTR_ESSENTIAL = "AutoUpdate-Essential-Module";
    
    
    private static Lookup.Result<KeyStoreProvider> result;
    private static Logger err = null;
    private static ModuleManager mgr = null;
    
    
    public static Collection<KeyStore> getKeyStore () {
        if (result == null) {            
            result = Lookup.getDefault ().lookup (
                    new Lookup.Template<KeyStoreProvider> (KeyStoreProvider.class));
            result.addLookupListener (new KeyStoreProviderListener ());
        }
        Collection<? extends KeyStoreProvider> c = result.allInstances ();
        if (c == null || c.isEmpty ()) {
            return Collections.emptyList ();
        }
        List<KeyStore> kss = new ArrayList<KeyStore> ();
        
        for (KeyStoreProvider provider : c) {
            KeyStore ks = provider.getKeyStore ();
            if (ks != null) {
                kss.add (ks);
            }
        }
        
        return kss;
    }
    
    static private class KeyStoreProviderListener implements LookupListener {
        private KeyStoreProviderListener () {
        }
        
        public void resultChanged (LookupEvent ev) {
            result = null;
        }
    }
    
    private static final String ATTR_NAME = "name"; // NOI18N
    private static final String ATTR_SPEC_VERSION = "specification_version"; // NOI18N
    private static final String ATTR_SIZE = "size"; // NOI18N
    private static final String ATTR_NBM_NAME = "nbm_name"; // NOI18N
    
    private static File getInstallLater(File root) {
        File file = new File(root.getPath() + FILE_SEPARATOR + DOWNLOAD_DIR + FILE_SEPARATOR + ModuleUpdater.LATER_FILE_NAME);
        return file;
    }

    public static void deleteAllDoLater() {
        List<File> clusters = UpdateTracking.clusters(true);
        assert clusters != null : "Clusters cannot be empty."; // NOI18N
        for (File cluster : clusters) {
            for (File doLater : findDoLater (cluster)) {
                doLater.delete ();
            }
        }                                
    }
    
    private static Collection<File> findDoLater (File cluster) {
        if (! cluster.exists ()) {
            return Collections.emptySet ();
        } else {
            Collection<File> res = new HashSet<File> ();
            if (getInstallLater (cluster).exists ()) {
                res.add (getInstallLater (cluster));
            }
            if (ModuleDeactivator.getDeactivateLater (cluster).exists ()) {
                res.add (ModuleDeactivator.getDeactivateLater (cluster));
            }
            return res;
        }
    }
    
    public static void writeInstallLater (Map<UpdateElementImpl, File> updates) {
        // loop for all clusters and write if needed
        List<File> clusters = UpdateTracking.clusters(true);
        assert clusters != null : "Clusters cannot be empty."; // NOI18N
        for (File cluster : clusters) {
            writeInstallLaterToCluster (cluster, updates);
        }
    }
    
    private static void writeInstallLaterToCluster (File cluster, Map<UpdateElementImpl, File> updates) {
        Document document = XMLUtil.createDocument(UpdateTracking.ELEMENT_MODULES, null, null, null);                
        
        Element root = document.getDocumentElement();

        if (updates.isEmpty ()) {
            return ;
        }
        
        boolean isEmpty = true;
        for (UpdateElementImpl elementImpl : updates.keySet ()) {
            File c = updates.get(elementImpl);
            // pass this module to given cluster ?
            if (cluster.equals (c)) {
                Element module = document.createElement(UpdateTracking.ELEMENT_MODULE);
                module.setAttribute(UpdateTracking.ATTR_CODENAMEBASE, elementImpl.getCodeName());
                module.setAttribute(ATTR_NAME, elementImpl.getDisplayName());
                module.setAttribute(ATTR_SPEC_VERSION, elementImpl.getSpecificationVersion().toString());
                module.setAttribute(ATTR_SIZE, Long.toString(elementImpl.getDownloadSize()));
                module.setAttribute(ATTR_NBM_NAME, InstallSupportImpl.getDestination(cluster, elementImpl.getCodeName(), true).getName());

                root.appendChild( module );
                isEmpty = false;
            }
        }
        
        if (isEmpty) {
            return ;
        }
        
        writeXMLDocumentToFile (document, getInstallLater (cluster));
    }
    
    private static void writeXMLDocumentToFile (Document doc, File dest) {
        doc.getDocumentElement ().normalize ();

        dest.getParentFile ().mkdirs ();
        InputStream is = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream ();
        OutputStream fos = null;
        try {
            try {
                XMLUtil.write (doc, bos, "UTF-8"); // NOI18N
                if (bos != null) {
                    bos.close ();
                }
                fos = new FileOutputStream (dest);
                is = new ByteArrayInputStream (bos.toByteArray ());
                FileUtil.copy (is, fos);
            } finally {
                if (is != null) {
                    is.close ();
                }
                if (fos != null) {
                    fos.close ();
                }
                if (bos != null) {
                    bos.close ();
                }
            }
        } catch (java.io.FileNotFoundException fnfe) {
            Exceptions.printStackTrace (fnfe);
        } catch (java.io.IOException ioe) {
            Exceptions.printStackTrace (ioe);
        } finally {
            if (bos != null) {
                try {
                    bos.close ();
                } catch (Exception x) {
                    Exceptions.printStackTrace (x);
                }
            }
        }
    }

    public static void writeDeactivateLater (Collection<File> files) {
        File userdir = InstallManager.getUserDir ();
        assert userdir != null && userdir.exists (): "Userdir " + userdir + " found and exists."; // NOI18N
        writeMarkedFilesToFile (files, ModuleDeactivator.getDeactivateLater (userdir));
    }
    
    public static void writeFileMarkedForDelete (Collection<File> files) {
        writeMarkedFilesToFile (files, ModuleDeactivator.getControlFileForMarkedForDelete (InstallManager.getUserDir ()));
    }
    
    public static void writeFileMarkedForDisable (Collection<File> files) {
        writeMarkedFilesToFile (files, ModuleDeactivator.getControlFileForMarkedForDisable (InstallManager.getUserDir ()));
    }
    
    private static void writeMarkedFilesToFile (Collection<File> files, File dest) {
        
        // don't forget for content written before
        String content = "";
        if (dest.exists ()) {
            content += ModuleDeactivator.readStringFromFile (dest);
        }
        
        for (File f : files) {
            content += f.getAbsolutePath () + UpdateTracking.PATH_SEPARATOR;
        }
        
        if (content == null || content.length () == 0) {
            return ;
        }
        
        dest.getParentFile ().mkdirs ();
        assert dest.getParentFile ().exists () && dest.getParentFile ().isDirectory () : "Parent of " + dest + " exists and is directory.";
        InputStream is = null;
        OutputStream fos = null;            
        
        try {
            try {
                fos = new FileOutputStream (dest);
                is = new ByteArrayInputStream (content.getBytes());
                FileUtil.copy (is, fos);
            } finally {
                if (is != null) is.close();
                if (fos != null) fos.close();
            }                
        } catch (java.io.FileNotFoundException fnfe) {
            Exceptions.printStackTrace(fnfe);
        } catch (java.io.IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }

    }
    
    public static void writeAdditionalInformation (Map<UpdateElementImpl, File> updates) {
        // loop for all clusters and write if needed
        List<File> clusters = UpdateTracking.clusters (true);
        assert clusters != null : "Clusters cannot be empty."; // NOI18N
        for (File cluster : clusters) {
            writeAdditionalInformationToCluster (cluster, updates);
        }
    }
    
    public static File locateUpdateTracking (ModuleInfo m) {
        String fileNameToFind = UpdateTracking.TRACKING_FILE_NAME + '/' + m.getCodeNameBase ().replace ('.', '-') + ".xml"; // NOI18N
        return InstalledFileLocator.getDefault ().locate (fileNameToFind, m.getCodeNameBase (), false);
    }
    
    public static String readSourceFromUpdateTracking (ModuleInfo m) {
        String res = null;
        File ut = locateUpdateTracking (m);
        if (ut != null) {
            Node n = getModuleConfiguration (ut);
            if (n != null) {
                Node attrOrigin = n.getAttributes ().getNamedItem (UpdateTracking.ATTR_ORIGIN);
                assert attrOrigin != null : "ELEMENT_VERSION must contain ATTR_ORIGIN attribute.";
                if (! (UpdateTracking.UPDATER_ORIGIN.equals (attrOrigin.getNodeValue ()) ||
                        UpdateTracking.INSTALLER_ORIGIN.equals (attrOrigin.getNodeValue ()))) {
                    // ignore default value
                    res = attrOrigin.getNodeValue ();
                }
            }
        }
        return res;
    }
    
    public static Date readInstallTimeFromUpdateTracking (ModuleInfo m) {
        Date res = null;
        String time = null;
        File ut = locateUpdateTracking (m);
        if (ut != null) {
            Node n = getModuleConfiguration (ut);
            if (n != null) {
                Node attrInstallTime = n.getAttributes ().getNamedItem (UpdateTracking.ATTR_INSTALL);
                assert attrInstallTime != null : "ELEMENT_VERSION must contain ATTR_INSTALL attribute.";
                time = attrInstallTime.getNodeValue ();
            }
        }
        if (time != null) {
            try {
                long lTime = Long.parseLong (time);
                res = new Date (lTime);
            } catch (NumberFormatException nfe) {
                getLogger ().log (Level.INFO, nfe.getMessage (), nfe);
            }
        }
        return res;
    }
    
    static Module toModule(UpdateUnit uUnit) {
        return getModuleInstance(uUnit.getCodeName(), null); // XXX
    }
    
    public static Module toModule(String codeNameBase, String specificationVersion) {
        return getModuleInstance(codeNameBase, specificationVersion);
    }
    
    public static Module toModule (ModuleInfo info) {
        return getModuleInstance (info.getCodeNameBase(), info.getSpecificationVersion ().toString ());
    }
    
    public static boolean isFixed (ModuleInfo info) {
        Module m = toModule (info);
        assert ! info.isEnabled () || m != null : "Module found for enabled " + info;
        return m == null ? false : m.isFixed ();
    }
    
    public static boolean isValid (ModuleInfo info) {
        Module m = toModule (info);
        assert ! info.isEnabled () || m != null : "Module found for enabled " + info;
        return m == null ? false : m.isValid ();
    }
    
    static UpdateUnit toUpdateUnit(Module m) {
        return UpdateManagerImpl.getInstance().getUpdateUnit(m.getCodeNameBase());
    }
    
    static UpdateUnit toUpdateUnit(String codeNameBase) {
        return UpdateManagerImpl.getInstance().getUpdateUnit(codeNameBase);
    }
    
    private static Set<Dependency> takeDependencies(UpdateElement el) {
        UpdateElementImpl i = Trampoline.API.impl(el);
        assert UpdateManager.TYPE.MODULE == i.getType () || UpdateManager.TYPE.KIT_MODULE == i.getType () : "Only for UpdateElement for modules.";
        return takeModuleInfo (el).getDependencies();
    }
    
    private static UpdateElement findRequiredModule (Dependency dep, Collection<ModuleInfo> installedModules) {
        switch (dep.getType ()) {
            case (Dependency.TYPE_NEEDS) :
            case (Dependency.TYPE_RECOMMENDS) :
            case (Dependency.TYPE_REQUIRES) :
                // find if some module fit the dependency
                ModuleInfo info = DependencyChecker.findModuleMatchesDependencyRequires (dep, installedModules);
                if (info != null) {
                    // it's Ok, no module is required
                } else {
                    // find corresponding UpdateUnit
                    for (UpdateUnit unit : UpdateManagerImpl.getInstance ().getUpdateUnits (UpdateManager.TYPE.MODULE)) {
                        assert unit != null : "UpdateUnit for " + info.getCodeName() + " found.";
                        // find correct UpdateElement
                        // installed module can ignore here
                        if (unit.getAvailableUpdates ().size () > 0) {
                            for (UpdateElement el : unit.getAvailableUpdates ()) {
                                UpdateElementImpl impl = Trampoline.API.impl (el);
                                List<ModuleInfo> moduleInfos = impl.getModuleInfos ();
                                for (ModuleInfo moduleInfo : moduleInfos) {
                                    if (Arrays.asList (moduleInfo.getProvides ()).contains (dep.getName ())) {
                                        return el;
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                break;
            case (Dependency.TYPE_MODULE) :
                String moduleName = dep.getName();
                UpdateUnit unit = UpdateManagerImpl.getInstance ().getUpdateUnit(moduleName);

                // there is no fit module
                if (unit == null) {
                    return null;
                }

                if (unit.getInstalled () != null) {
                    // check if version match
                    UpdateElement installedEl = unit.getInstalled ();
                    UpdateElementImpl installedElImpl = Trampoline.API.impl(installedEl);
                    List<ModuleInfo> installedModuleInfos = installedElImpl.getModuleInfos ();
                    for (ModuleInfo installedModuleInfo : installedModuleInfos) {
                        if (DependencyChecker.checkDependencyModule (dep, installedModuleInfo)) {
                            return null;
                        }
                    }

                }

                // find available modules
                List<UpdateElement> elements = unit.getAvailableUpdates();
                for (UpdateElement el : elements) {
                    UpdateElementImpl impl = Trampoline.API.impl(el);
                    if (impl instanceof ModuleUpdateElementImpl) {
                        ModuleInfo moduleInfo = impl.getModuleInfos ().get (0);
                        if (DependencyChecker.checkDependencyModule (dep, moduleInfo)) {
                            return el;
                        }
                    } else {
                        // XXX: maybe useful later, now I don't need it
                        assert false : "Not implemented yet.";
                        //FeatureItem i = (FeatureItem) impl.getUpdateItemImpl();
                        //i.getDependenciesToModules();
                    }
                }

                break;
            case (Dependency.TYPE_JAVA) :
                getLogger ().log (Level.FINE, "Check dependency on Java platform. Dependency: " + dep);
                break;
            default:
                //assert false : "Unknown type of Dependency, was " + dep.getType ();
                getLogger ().log (Level.FINE, "Uncovered Dependency " + dep);                    
                break;
        }
        return null;
    }
    
    static Set<UpdateElement> findRequiredModules(Set<Dependency> deps, Collection<ModuleInfo> installedModules) {
        Set<UpdateElement> requiredElements = new HashSet<UpdateElement> ();
        for (Dependency dep : deps) {
            UpdateElement el = findRequiredModule (dep, installedModules);
            if (el != null) {
                UpdateElementImpl elImpl = Trampoline.API.impl(el);
                List<ModuleInfo> mInfos = elImpl.getModuleInfos ();
                assert mInfos != null;
                if (!installedModules.containsAll (mInfos)) {
                    requiredElements.add(el);
                    installedModules.add(takeModuleInfo(el));
        }
            }
        }
        // check dependencies of extended modules as well
        for (UpdateElement el : new HashSet<UpdateElement> (requiredElements)) {
            requiredElements.addAll(findRequiredModules(takeDependencies(el), installedModules));
        }
        
        return requiredElements;
    }
    
    private static List<ModuleInfo> getInstalledModules() {
        return new ArrayList<ModuleInfo> (InstalledModuleProvider.getInstalledModules().values());
    }
    
    public static Set<UpdateElement> findRequiredUpdateElements (UpdateElement element, List<ModuleInfo> infos) {
        UpdateElementImpl el = Trampoline.API.impl(element);
        Set<UpdateElement> retval = new HashSet<UpdateElement> ();
        switch (el.getType ()) {
        case KIT_MODULE :
        case MODULE :
            final Set<Dependency> deps = ((ModuleUpdateElementImpl) el).getModuleInfo ().getDependencies ();
            final List<ModuleInfo> extendedModules = getInstalledModules ();
            extendedModules.addAll (infos);
            final Set<Dependency> brokenDeps = DependencyChecker.findBrokenDependencies (deps, extendedModules);
            retval = findRequiredModules (brokenDeps, extendedModules);
            break;
        case STANDALONE_MODULE :
        case FEATURE :
            FeatureUpdateElementImpl feature = (FeatureUpdateElementImpl) el;
            for (ModuleUpdateElementImpl module : feature.getContainedModuleElements ()) {
                retval.addAll (findRequiredUpdateElements (module.getUpdateElement (), infos));
            }
            break;
        default:
            assert false : "Not implement for type " + el.getType () + " of UpdateElement " + el;
        }
        return retval;
    }
    
    public static Set<Dependency> findBrokenDependencies(UpdateElement element, List<ModuleInfo> infos) {
        List<ModuleInfo> extendedModules = getInstalledModules();
        extendedModules.addAll(infos);
        Set<Dependency> deps = collectAllDependencies (element);
        Set<Dependency> retval = Collections.emptySet ();
        final Set<Dependency> brokenDeps = DependencyChecker.findBrokenDependencies(deps, extendedModules);
        Set<UpdateElement> reqs = findRequiredModules(brokenDeps, extendedModules);
        extendedModules.addAll (getModuleInfos (reqs));
        retval = DependencyChecker.findBrokenDependencies(deps, extendedModules);
        return retval;
    }
    
    private static Set<Dependency> collectAllDependencies (UpdateElement element) {
        UpdateElementImpl el = Trampoline.API.impl (element);
        assert el != null : "UpdateElementImpl found for UpdateElement " + element;
        List<ModuleInfo> mInfos = null;
        switch (el.getType ()) {
        case KIT_MODULE :
        case MODULE :
            mInfos = el.getModuleInfos ();
            break;
        case STANDALONE_MODULE :
        case FEATURE :
            mInfos = el.getModuleInfos ();
            break;
        case CUSTOM_HANDLED_COMPONENT : // XXX: CUSTOM_HANDLED_COMPONENT should support UpdateItem<->UpdateItem dependencies
            mInfos = Collections.emptyList ();
            getLogger ().log (Level.INFO, "CUSTOM_HANDLED_COMPONENT should support UpdateItem<->UpdateItem dependencies.");
            break;
        default:
            assert false : "Unsupported for " + element + "[impl: " + el.getClass() + "]";
        }
        final Set<Dependency> deps = new HashSet<Dependency> ();
        for (ModuleInfo info : mInfos) {
            deps.addAll (filterTypeRecommends (info.getDependencies ()));
        }
        return deps;
    }
    
    private static Set<Dependency> filterTypeRecommends (Collection<Dependency> deps) {
        Set<Dependency> res = new HashSet<Dependency> ();
        for (Dependency dep : deps) {
            if (Dependency.TYPE_RECOMMENDS != dep.getType ()) {
                res.add (dep);
            }
        }
        return res;
    }
    
    private static Set<Dependency> getNeedsAndRequiresOnly (Collection<Dependency> deps) {
        Set<Dependency> res = new HashSet<Dependency> ();
        for (Dependency dep : deps) {
            if (Dependency.TYPE_REQUIRES == dep.getType () || Dependency.TYPE_NEEDS == dep.getType ()) {
                res.add (dep);
            }
        }
        return res;
    }
    
    static Set<String> getBrokenDependencies (UpdateElement element, List<ModuleInfo> infos) {
        assert element != null : "UpdateElement cannot be null";
        Set<String> retval = new HashSet<String> ();
        for (Dependency dep : findBrokenDependencies (element, infos)) {
            retval.add (dep.toString ());
        }
        return retval;
    }
    
    static Set<String> getBrokenDependenciesInInstalledModules (UpdateElement element) {
        assert element != null : "UpdateElement cannot be null";
        Set<Dependency> deps = new HashSet<Dependency> ();
        for (ModuleInfo m : getModuleInfos (Collections.singleton (element))) {
            deps.addAll (DependencyChecker.findBrokenDependenciesTransitive (m,
                    InstalledModuleProvider.getInstalledModules ().values (),
                    new HashSet<ModuleInfo> ()));
        }
        Set<String> retval = new HashSet<String> ();
        for (Dependency dep : deps) {
            retval.add (dep.toString ());
        }
        return retval;
    }
    
    static List<ModuleInfo> getModuleInfos (Collection<UpdateElement> elements) {
        List<ModuleInfo> infos = new ArrayList<ModuleInfo> (elements.size ());
        for (UpdateElement el : elements) {
            UpdateElementImpl impl = Trampoline.API.impl (el);
            infos.addAll (impl.getModuleInfos ());
        }
        return infos;
    }
    
    private static Module getModuleInstance(String codeNameBase, String specificationVersion) {
        if (mgr == null) {
            mgr = Main.getModuleSystem().getManager();
        }
        assert mgr != null;
        if (mgr == null || specificationVersion == null) {
            return mgr != null ? mgr.get(codeNameBase) : null;
        } else {
            Module m = mgr.get(codeNameBase);
            if (m == null) {
                return null;
            } else {
                return m.getSpecificationVersion ().compareTo (new SpecificationVersion (specificationVersion)) >= 0 ? m : null;
            }
        }
    }
    
    public static boolean isAutomaticallyEnabled(String codeNameBase) {
        Module m = getModuleInstance(codeNameBase, null);
        return m != null ? (m.isAutoload() || m.isEager() || m.isFixed()) : false;
    }
    
    public static ModuleInfo takeModuleInfo (UpdateElement el) {
        UpdateElementImpl impl = Trampoline.API.impl (el);
        assert impl instanceof ModuleUpdateElementImpl;
        return ((ModuleUpdateElementImpl) impl).getModuleInfo ();
    }
    
    private static String productVersion = null;
    
    public static String getProductVersion () {
        if (productVersion == null) {
            String buildNumber = System.getProperty ("netbeans.buildnumber"); // NOI18N
            productVersion = NbBundle.getMessage (TopLogging.class, "currentVersion", buildNumber); // NOI18N
        }
        return productVersion;
    }
    
    private static Node getModuleConfiguration (File moduleUpdateTracking) {
        Document document = null;
        InputStream is;
        try {
            is = new FileInputStream (moduleUpdateTracking);
            InputSource xmlInputSource = new InputSource (is);
            document = XMLUtil.parse (xmlInputSource, false, false, null, org.openide.xml.EntityCatalog.getDefault ());
            if (is != null) {
                is.close ();
            }
        } catch (SAXException saxe) {
            getLogger ().log (Level.WARNING, null, saxe);
            return null;
        } catch (IOException ioe) {
            getLogger ().log (Level.WARNING, null, ioe);
        }

        assert document.getDocumentElement () != null : "File " + moduleUpdateTracking + " must contain <module> element.";
        return getModuleElement (document.getDocumentElement ());
    }
    
    private static Node getModuleElement (Element element) {
        Node lastElement = null;
        assert UpdateTracking.ELEMENT_MODULE.equals (element.getTagName ()) : "The root element is: " + UpdateTracking.ELEMENT_MODULE + " but was: " + element.getTagName ();
        NodeList listModuleVersions = element.getElementsByTagName (UpdateTracking.ELEMENT_VERSION);
        for (int i = 0; i < listModuleVersions.getLength (); i++) {
            lastElement = getModuleLastVersion (listModuleVersions.item (i));
            if (lastElement != null) {
                break;
            }
        }
        return lastElement;
    }
    
    private static Node getModuleLastVersion (Node version) {
        Node attrLast = version.getAttributes ().getNamedItem (UpdateTracking.ATTR_LAST);
        assert attrLast != null : "ELEMENT_VERSION must contain ATTR_LAST attribute.";
        if (Boolean.valueOf (attrLast.getNodeValue ()).booleanValue ()) {
            return version;
        } else {
            return null;
        }
    }
    
    private static File getAdditionalInformation (File root) {
        File file = new File (root.getPath () + FILE_SEPARATOR + DOWNLOAD_DIR + 
                FILE_SEPARATOR + UpdateTracking.ADDITIONAL_INFO_FILE_NAME);
        return file;
    }

    private static void writeAdditionalInformationToCluster (File cluster, Map<UpdateElementImpl, File> updates) {
        if (updates.isEmpty ()) {
            return ;
        }
        
        Document document = XMLUtil.createDocument (UpdateTracking.ELEMENT_ADDITIONAL, null, null, null);                
        Element root = document.getDocumentElement ();
        boolean isEmpty = true;
        
        for (UpdateElementImpl impl : updates.keySet ()) {
            File c = updates.get (impl);
            // pass this module to given cluster ?
            if (cluster.equals (c)) {
                Element module = document.createElement (UpdateTracking.ELEMENT_ADDITIONAL_MODULE);
                module.setAttribute(ATTR_NBM_NAME,
                        InstallSupportImpl.getDestination (cluster, impl.getCodeName(), true).getName ());
                module.setAttribute (UpdateTracking.ATTR_ADDITIONAL_SOURCE, impl.getSource ());
                root.appendChild( module );
                isEmpty = false;
            }
        }
        
        if (isEmpty) {
            return ;
        }
        
        writeXMLDocumentToFile (document, getAdditionalInformation (cluster));
    }
    
    public static UpdateItem createUpdateItem (UpdateItemImpl impl) {
        assert Trampoline.SPI != null;
        return Trampoline.SPI.createUpdateItem (impl);
    }
    
    public static UpdateItemImpl getUpdateItemImpl (UpdateItem item) {
        assert Trampoline.SPI != null;
        return Trampoline.SPI.impl (item);
    }
    
    public static boolean canDisable (Module m) {
        return m != null &&  m.isEnabled () && ! isEssentialModule (m) && ! m.isAutoload () && ! m.isEager ();
    }
    
    public static boolean canEnable (Module m) {
        return m != null && !m.isEnabled () && ! m.isAutoload () && ! m.isEager ();
    }
    
    public static boolean isElementInstalled (UpdateElement el) {
        assert el != null : "Invalid call isElementInstalled with null parameter.";
        if (el == null) {
            return false;
        }
        return el.equals (el.getUpdateUnit ().getInstalled ());
    }
    
    public static boolean isKitModule (ModuleInfo mi) {
        // XXX: it test can break simple modules mode
        // should find corresponing UpdateElement and check its type
        Object o = mi.getAttribute (ATTR_VISIBLE);
        return o == null || Boolean.parseBoolean (o.toString ());
    }
    
    public static boolean isEssentialModule (ModuleInfo mi) {
        Object o = mi.getAttribute (ATTR_ESSENTIAL);
        return isFixed (mi) || (o != null && Boolean.parseBoolean (o.toString ()));
    }
    
    private static Logger getLogger () {
        if (err == null) {
            err = Logger.getLogger (Utilities.class.getName ());
        }
        return err;
    }
    
    /** Finds modules depending on given module.
     * @param m a module to start from; may be enabled or not, but must be owned by this manager
     * @return a set (possibly empty) of modules managed by this manager, never including m
     */
    public static Set<Module> findRequiredModules (Module m, ModuleManager mm) {
        return mm.getModuleInterdependencies(m, false, false);
    }
    
    /** Finds for modules given module depends upon.
     * @param m a module to start from; may be enabled or not, but must be owned by this manager
     * @return a set (possibly empty) of modules managed by this manager, never including m
     */
    public static Set<Module> findDependingModules (Module m, ModuleManager mm) {
        return mm.getModuleInterdependencies(m, true, false);
    }

    public static String formatDate(Date date) {
        synchronized(DATE_FORMAT) {
            return DATE_FORMAT.format(date);
        }
    }

    public static Date parseDate(String date) throws ParseException {
        synchronized(DATE_FORMAT) {
            return DATE_FORMAT.parse(date);
        }
    }    
}
