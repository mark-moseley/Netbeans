/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result;
import org.netbeans.modules.apisupport.project.NbModuleProjectType;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Able to find sources in the NetBeans sources zip.
 *
 * @author Martin Krauskopf
 */
public final class GlobalSourceForBinaryImpl implements SourceForBinaryQueryImplementation {
    
    /** Default constructor for lookup. */
    public GlobalSourceForBinaryImpl() {}
    
    public Result findSourceRoots(URL binaryRoot) {
        try {
            NbPlatform supposedPlaf = null;
            for (Iterator it = NbPlatform.getPlatforms().iterator(); it.hasNext(); ) {
                NbPlatform plaf = (NbPlatform) it.next();
                // XXX more robust condition?
                if (binaryRoot.toExternalForm().indexOf(plaf.getDestDir().toURI().toURL().toExternalForm()) != -1) {
                    supposedPlaf = plaf;
                    break;
                }
            }
            if (supposedPlaf == null) {
                return null;
            }
            if (!binaryRoot.getProtocol().equals("jar")) { // NOI18N
                Util.err.log(binaryRoot + " is not an archive file."); // NOI18N
                return null;
            }
            File binaryRootF = new File(URI.create(FileUtil.getArchiveFile(binaryRoot).toExternalForm()));
            FileObject fo = FileUtil.toFileObject(binaryRootF);
            if (fo == null) {
                Util.err.log("Cannot found FileObject for " + binaryRootF + "(" + binaryRoot + ")"); // NOI18N
                return null;
            }
            String cnb = fo.getName().replace('-', '.');
            
            final List/*<FileObject>*/ candidates = new ArrayList();
            URL[] roots = supposedPlaf.getSourceRoots();
            for (int i = 0; i < roots.length; i++) {
                if (roots[i].getProtocol().equals("jar")) { // NOI18N
                    // suppose zipped sources
                    File nbSrcF = new File(URI.create(FileUtil.getArchiveFile(roots[i]).toExternalForm()));
                    if (!nbSrcF.exists()) {
                        continue;
                    }
                    String pathInZip = NetBeansSourcesParser.getInstance(nbSrcF).findSourceRoot(cnb);
                    if (pathInZip == null) {
                        continue;
                    }
                    URL u = new URL(roots[i], pathInZip);
                    FileObject entryFO = URLMapper.findFileObject(u);
                    if (entryFO != null) {
                        candidates.add(entryFO);
                    }
                } // dirs are currently resolved by o.n.m.apisupport.project.queries.SourceForBinaryImpl
            }
            return new Result() {
                public FileObject[] getRoots() {
//                    return new FileObject[0];
                    return (FileObject[]) candidates.toArray(new FileObject[candidates.size()]);
                }
                public void addChangeListener(ChangeListener l) {}
                public void removeChangeListener(ChangeListener l) {}
            };
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new AssertionError(ex);
        }
    }
    
    private static class NetBeansSourcesParser {
        
        /** Zip file to instance map. */
        private static final Map/*<File, NetBeansSourcesParser>*/ instances = new HashMap();
        
        private static final String NBBUILD_ENTRY = "nbbuild/"; // NOI18N
        
        private Map/*<String,String>*/ cnbToPrjDir;
        private ZipFile nbSrcZip;
        private String zipNBCVSRoot;
        
        static NetBeansSourcesParser getInstance(File nbSrcZip) throws ZipException, IOException {
            NetBeansSourcesParser nbsp = (NetBeansSourcesParser) instances.get(nbSrcZip);
            if (nbsp == null) {
                ZipFile nbSrcZipFile = new ZipFile(nbSrcZip);
                String zipNBCVSRoot = NetBeansSourcesParser.findNBCVSRoot(nbSrcZipFile);
                nbsp = new NetBeansSourcesParser(nbSrcZipFile, zipNBCVSRoot);
                instances.put(nbSrcZip, nbsp);
            }
            return nbsp;
        }
        
        NetBeansSourcesParser(ZipFile nbSrcZip, String zipNBCVSRoot) {
            this.nbSrcZip = nbSrcZip;
            this.zipNBCVSRoot = zipNBCVSRoot;
        }
        
        String findSourceRoot(final String cnb) {
            if (cnbToPrjDir == null) {
                try {
                    doScanZippedNetBeansOrgSources();
                } catch (IOException ex) {
                    Util.err.notify(ErrorManager.WARNING, ex);
                }
            }
            return (String) cnbToPrjDir.get(cnb);
        }
        
        private static String findNBCVSRoot(final ZipFile nbSrcZip) {
            String nbRoot = null;
            for (Enumeration/*<ZipEntry>*/ en = nbSrcZip.entries(); en.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) en.nextElement();
                if (!entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName();
                if (!name.equals(NBBUILD_ENTRY) &&
                        !(name.endsWith(NBBUILD_ENTRY) && name.substring(name.indexOf('/') + 1).equals(NBBUILD_ENTRY))) {
                    continue;
                }
                ZipEntry xmlEntry = nbSrcZip.getEntry(name + "nbproject/project.xml"); // NOI18N
                if (xmlEntry != null) {
                    nbRoot = name.substring(0, name.length() - NBBUILD_ENTRY.length());
                    break;
                }
            }
            return nbRoot;
        }
        
        private static boolean isValidNbBuild(String name) {
            return name.equals(NBBUILD_ENTRY) ||
                    (name.endsWith(NBBUILD_ENTRY) && name.substring(name.indexOf('/') + 1).equals(NBBUILD_ENTRY));
        }
        
        private void doScanZippedNetBeansOrgSources() throws IOException {
            cnbToPrjDir = new HashMap();
            for (Enumeration/*<ZipEntry>*/ en = nbSrcZip.entries(); en.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) en.nextElement();
                if (!entry.isDirectory()) {
                    continue;
                }
                String path = entry.getName().substring(0, entry.getName().length() - 1); // remove last slash
                if (this.zipNBCVSRoot != null && (!path.startsWith(this.zipNBCVSRoot) || path.equals(this.zipNBCVSRoot))) {
                    continue;
                }
                StringTokenizer st = new StringTokenizer(path, "/"); // NOI18N
                if (st.countTokens() > ModuleList.DEPTH_NB_ALL) {
                    continue;
                }
                String name = path.substring(path.lastIndexOf('/') + 1, path.length());
                if (ModuleList.EXCLUDED_DIR_NAMES.contains(name)) {
                    // #61579: known to not be project dirs, so skip to save time.
                    continue;
                }
                // XXX should read src.dir from properties
                ZipEntry src = nbSrcZip.getEntry(entry.getName() + "src/"); // NOI18N
                if (src == null || !src.isDirectory()) {
                    continue;
                }
                
                ZipEntry projectXML = nbSrcZip.getEntry(entry.getName() + "nbproject/project.xml"); // NOI18N
                if (projectXML == null) {
                    continue;
                }
                String cnb = parseCNB(projectXML);
                if (cnb != null) {
                    cnbToPrjDir.put(cnb, entry.getName() + "src/"); // NOI18N
                }
            }
        }
        
        private String parseCNB(final ZipEntry projectXML) throws IOException {
            Document doc;
            InputStream is = nbSrcZip.getInputStream(projectXML);
            try {
                doc = XMLUtil.parse(new InputSource(is), false, true, null, null);
            } catch (SAXException e) {
                throw (IOException) new IOException(projectXML + ": " + e.toString()).initCause(e); // NOI18N
            } finally {
                is.close();
            }
            Element docel = doc.getDocumentElement();
            Element type = Util.findElement(docel, "type", "http://www.netbeans.org/ns/project/1"); // NOI18N
            String cnb = null;
            if (Util.findText(type).equals("org.netbeans.modules.apisupport.project")) { // NOI18N
                Element cfg = Util.findElement(docel, "configuration", "http://www.netbeans.org/ns/project/1"); // NOI18N
                Element data = Util.findElement(cfg, "data", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
                if (data != null) {
                    cnb = Util.findText(Util.findElement(data, "code-name-base", NbModuleProjectType.NAMESPACE_SHARED)); // NOI18N
                }
            }
            return cnb;
        }
        
    }
    
}
