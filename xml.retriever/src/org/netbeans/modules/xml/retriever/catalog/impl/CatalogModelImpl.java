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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

/*
 * CatalogModelImpl.java
 *
 * Created on March 29, 2006, 6:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.retriever.catalog.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.helpers.Debug;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.retriever.XMLCatalogProvider;
import org.netbeans.modules.xml.retriever.catalog.ProjectCatalogSupport;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModelFactory;
import org.netbeans.modules.xml.retriever.catalog.ProjectCatalogSupport;
import org.netbeans.modules.xml.retriever.impl.Util;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author girix
 */
public class CatalogModelImpl implements CatalogModel {
    protected FileObject catalogFileObject = null;
    private static Logger logger = Logger.getLogger(CatalogModelImpl.class.getName());
    /** Creates a new instance of CatalogModelImpl */
    public CatalogModelImpl(Project myProject) throws IOException{
        assert(myProject != null);
        this.catalogFileObject = Util.getProjectCatalogFileObject(myProject, false);
    }
    
    
    /** Creates a new instance of CatalogModelImpl */
    public CatalogModelImpl(FileObject catalogFileObject) throws IOException{
        assert(catalogFileObject != null);
        this.catalogFileObject = catalogFileObject;
    }
    
    
    public CatalogModelImpl(){
    }
    
    
    /**
     * This constructor is for unit testing purpose only
     */
    public CatalogModelImpl(File tempFolder) throws IOException{
        tempFolder = FileUtil.normalizeFile(tempFolder);
        FileObject fo = FileUtil.toFileObject(tempFolder);
        String fileName = CatalogWriteModel.PUBLIC_CATALOG_FILE_NAME+
                CatalogWriteModel.CATALOG_FILE_EXTENSION;
        this.catalogFileObject = FileUtil.createData(fo, fileName);
    }
    
    private boolean doFetch = true;
    private boolean fetchSynchronous = false;
    public synchronized ModelSource getModelSourceSynchronous(URI locationURI,
        ModelSource modelSourceOfSourceDocument, boolean fetch) throws CatalogModelException {
        ModelSource ms = null;
        doFetch = fetch;
        fetchSynchronous = true;
        try {
            ms = getModelSource(locationURI, modelSourceOfSourceDocument);
        } catch (CatalogModelException ex) {
            throw ex;
        } finally {
            //reset flags
            doFetch = true;
            fetchSynchronous = false;
        }
        return ms;
    }

    
    public synchronized ModelSource getModelSource(URI locationURI,
            ModelSource modelSourceOfSourceDocument) throws CatalogModelException {
        logger.entering("CatalogModelImpl", "getModelSource", locationURI);
        Exception exn = null;
        ModelSource result = null;
        //selects the correct cataog for use.
        useSuitableCatalogFile(modelSourceOfSourceDocument);
        if(isOrphan() && isLocalFile(locationURI)) {
            return tryOrphanResolution(locationURI, modelSourceOfSourceDocument);
        }
        File absResourceFile = null;
        FileObject fob = null;
        if(modelSourceOfSourceDocument != null)
            fob = (FileObject) modelSourceOfSourceDocument.getLookup().lookup(FileObject.class);
        try {
            //try to resolve using project wide catalog
            absResourceFile = resolveUsingCatalog(locationURI, fob);
        } catch (IOException ex) {
            exn = ex;
        } catch(CatalogModelException ex){
            exn = ex;
        }
        if( (absResourceFile == null) || (exn != null) ){
            //means there was no entry found in catalog or relative path resolution            
            ModelSource rms = getModelSourceFromSystemWideCatalog(locationURI, modelSourceOfSourceDocument);
            if (rms != null) {
                return rms;
            }
            try {
                if(doFetch) {
                    //we did not get any matching entry by conventional way..So try retrieve and cache
                    absResourceFile = retrieveCacheAndLookup(locationURI, fob);
                }
            } catch (IOException ex) {
                throw new CatalogModelException(ex);
            }
        }
        if(absResourceFile != null){
            logger.finer("Found  abs file res:"+absResourceFile);
            File normalizedFile = org.openide.filesystems.FileUtil.normalizeFile(absResourceFile);
            FileObject thisFileObj = org.openide.filesystems.FileUtil.toFileObject(normalizedFile);
            boolean editable = isEditable(absResourceFile);
            result = createModelSource(thisFileObj, editable);
        }else if(exn!= null) {
            throw new CatalogModelException(exn);
        }
        logger.exiting("CatalogModelImpl", "getModelSource", result);
        return result;
    }
    
    private void useSuitableCatalogFile(ModelSource modelSourceOfSourceDocument) {
        // if the modelSource's project has XMLCatalogProvider then use that to
        // see which catalog file to use for this modelSource
        if(modelSourceOfSourceDocument != null){
            FileObject msfo = (FileObject) modelSourceOfSourceDocument.getLookup().
                    lookup(FileObject.class);
            if(msfo == null)
                return;
            Project prj = FileOwnerQuery.getOwner(msfo);
            if(prj == null)
                return;
            XMLCatalogProvider catPovider = (XMLCatalogProvider) prj.getLookup().
                    lookup(XMLCatalogProvider.class);
            if(catPovider == null)
                return;
            URI caturi = catPovider.getCatalog(msfo);
            if(caturi == null)
                return;
            URI prjuri = FileUtil.toFile(prj.getProjectDirectory()).toURI();
            URI catFileURI = prjuri.resolve(caturi);
            if(catFileURI == null)
                return;
            File catFile = new File(catFileURI);
            if(!catFile.isFile()){
                try {
                    catFile.createNewFile();
                } catch (IOException ex) {
                    return;
                }
            }
            FileObject catFO = FileUtil.toFileObject(FileUtil.normalizeFile(catFile));
            if(catFO == null)
                return;
            //assign new catalog file that needs to be used for resolution
            this.catalogFileObject = catFO;
        }
    }
    
    
    public ModelSource getModelSource(URI locationURI) throws CatalogModelException{
        if(isOrphan()){
            //the originating file does not belong to a project so dont use catalog lookup
            //just use file resolution instead
            return tryOrphanResolution(locationURI, null);
        }
        //just look in to the project catalog
        return getModelSource(locationURI, null);
    }
    
    
    /**
     * This method must be overridden by the Unit testcase to return a special
     * Document object for a FileObject.
     */
    private Document getDocument(FileObject modelSourceFileObject) throws CatalogModelException{
        Document result = null;
        try {
            DataObject dObject = DataObject.find(modelSourceFileObject);
            EditorCookie ec = (EditorCookie)dObject.getCookie(EditorCookie.class);
            Document doc = ec.openDocument();
            assert(doc instanceof BaseDocument);
            result = doc;
        } catch (Exception dObjEx) {
            throw new CatalogModelException(dObjEx);
        }
        return result;
    }
    
    
    /**
     * This method could be overridden by the Unit testcase to return a special
     * ModelSource object for a FileObject with custom impl of classes added to the lookup.
     * This is optional if both getDocument(FO) and createCatalogModel(FO) are overridden.
     */
    protected ModelSource createModelSource(final FileObject thisFileObj, boolean editable) throws CatalogModelException{
        final ModelSource ms = Utilities.getModelSource(thisFileObj,editable);
        return ms;
    }
    
    
    protected CatalogModel createCatalogModel(FileObject fo) throws CatalogModelException{
        return new CatalogModelFactoryImpl().getCatalogModel(fo);
    }
    
    
    private ModelSource tryOrphanResolution(URI locationURI, ModelSource modelSource){
        logger.entering("CatalogModelImpl", "getModelSource", locationURI);
        if(catalogFileObject == null){
            try{
                if(locationURI.isAbsolute()){
                    //may be a local file URI so try creating a file
                    File file = new File(locationURI);
                    if(file.isFile()){
                        file = FileUtil.normalizeFile(file);
                        FileObject fo = FileUtil.toFileObject(file);
                        return createModelSource(fo, isEditable(file));
                    }
                } else {
                    //a relative URI, try resolving relative
                    if(modelSource != null){
                        //source is needed for resolution
                        FileObject fo = (FileObject) modelSource.getLookup().lookup(FileObject.class);
                        File file = resolveRelativeURI(locationURI, fo);
                        if(file != null){
                            file = FileUtil.normalizeFile(file);
                            FileObject fobj = FileUtil.toFileObject(file);
                            return createModelSource(fobj, isEditable(file));
                        }
                    }
                }
            }catch (Exception e){
                return null;
            }
        }
        return null;
    }
        
    private boolean isOrphan(){
        if(catalogFileObject == null)
            return true;
        return false;
    }
    
    private boolean isLocalFile(URI locationURI) {
        if(locationURI.isAbsolute() &&
           locationURI.getScheme() != null &&
           "file".equals(locationURI.getScheme()))
            return true;
        
        return false;
    }
            
    
    
    protected File resolveUsingCatalog(URI locationURI, FileObject sourceFileObject
            ) throws CatalogModelException, IOException {
        logger.entering("CatalogModelImpl", "resolveUsingCatalog", locationURI);
        if(locationURI == null)
            return null;
        File result = null;
        result = resolveUsingPublicCatalog(locationURI);
        if(result != null)
            return result;
        if(sourceFileObject != null){
            result = resolveRelativeURI(locationURI,  sourceFileObject);
        }
        if(result != null)
            return result;
        if( (locationURI.isAbsolute()) && locationURI.getScheme().equalsIgnoreCase("file")){
            //try to make a File
            result = new File(locationURI);
            if(result.isFile()){
                logger.exiting("CatalogModelImpl", "resolveUsingCatalog",result);
                return result;
            } else
                throw new FileNotFoundException(locationURI.toString()+": is absolute but "+result.getAbsolutePath()+" Not Found.");
        }
        throw new CatalogModelException(locationURI.toString()+" : Entry is not a relative or absolute and catalog entry not found");
    }
    
    
    private File retrieveCacheAndLookup(URI locationURI, FileObject sourceFileObject) throws IOException, CatalogModelException{
        File result = null;
        if((locationURI.isAbsolute()) && locationURI.getScheme().toLowerCase().
                startsWith("http") && !CatalogFileWrapperDOMImpl.TEST_ENVIRONMENT){
            // for all http and https absolute URI, just attempt downloading the
            // file using the retriever API and store in the private cache.
            //do not attempt this for a test environment.
            boolean res = false;
            try{
                res = Util.retrieveAndCache(locationURI, sourceFileObject,!fetchSynchronous);
            }catch (Exception e){//ignore all exceptions
            }
            if(res){
                //now attempt onec more
                result = resolveUsingPublicCatalog(locationURI);
                if(result != null)
                    return result;
            }
        }
        return result;
    }
    
    protected File resolveUsingPublicCatalog(URI locationURI) throws IOException, CatalogModelException{
        File result = null;
        if(catalogFileObject != null){
            //look up in the catalog
            File publicCatalogFile = FileUtil.toFile(catalogFileObject);
            if(publicCatalogFile.isFile()){
                //return if the file content is empty or just start and end tags
                if(publicCatalogFile.length() < 20)
                    return null;
                URI strRes = resolveUsingApacheCatalog(publicCatalogFile, locationURI.toString());
                if(strRes != null){
                    if(strRes.isAbsolute()){
                        if(strRes.getScheme().equalsIgnoreCase("file")){
                            result = new File(strRes);
                            if(result.isFile()){
                                logger.exiting("CatalogModelImpl", "resolveUsingCatalog",result);
                                return result;
                            } else
                                throw new FileNotFoundException(result.getAbsolutePath()+" Not Found.");
                        }else{
                            File res = resolveProjectProtocol(strRes);
                            if(res != null)
                                return res;
                            throw new CatalogModelException("Catalog contains non-file URI. Catalog Maps URI to a local file only.");
                        }
                    }
                }
            }
        }
        return null;
    }
    
    
    protected File resolveRelativeURI(URI locationURI, FileObject sourceFileObject) throws CatalogModelException, FileNotFoundException{
        File result = null;
        if(!locationURI.isAbsolute()){
            //this might be a relative file location
            if(sourceFileObject == null)
                throw new CatalogModelException(locationURI.toString()+" : Entry is relative but base file now known. Pass base file to the factory");
            File sourceFile = FileUtil.toFile(sourceFileObject);
            //IZ 104753
            //In case of layer.xml defined sourceFileObject, FileUtil.toFile returns null.
            
            if (sourceFile != null) {
                URI sourceFileObjectURI = sourceFile.toURI();
                URI resultURI = sourceFileObjectURI.resolve(locationURI);
                try{
                        result = new File(resultURI);
                } catch(Exception e){
                        throw new CatalogModelException(locationURI.toString()+" : Entry is relative but resolved entry is not absolute");
                }
                if(result.isFile()){
                        logger.exiting("CatalogModelImpl", "resolveUsingCatalog",result);
                        return result;
                } else
                        throw new FileNotFoundException(result.getAbsolutePath()+" Not Found.");
            }
        }
        return null;
    }
    
    
    protected URI resolveUsingApacheCatalog(File catalogFile, String locationURI) throws IOException, CatalogModelException{
        List<File> catalogFileList = new ArrayList<File>();
        catalogFileList.add(catalogFile);
        return resolveUsingApacheCatalog(catalogFileList, locationURI);
    }
    
    
    CatalogResolver catalogResolver;
    Catalog apacheCatalogResolverObj;
    protected URI resolveUsingApacheCatalog(List<File> catalogFileList, String locationURI) throws CatalogModelException, IOException  {
        if((logger.getLevel() != null) && (logger.getLevel().intValue() <= Level.FINER.intValue())){
            Debug debug = CatalogManager.getStaticManager().debug;
            debug.setDebug(logger.getLevel().intValue());
        }
        
        CatalogManager manager = new CatalogManager(null);
        manager.setUseStaticCatalog(false);
        manager.setPreferPublic(false);
        catalogResolver = new CatalogResolver(manager);
        apacheCatalogResolverObj = catalogResolver.getCatalog();
        for(File catFile : catalogFileList){
            apacheCatalogResolverObj.parseCatalog(catFile.toURL());
        }
        
        String result = null;
        try {
            result = apacheCatalogResolverObj.resolveSystem(locationURI);
        } catch (Exception ex) {
            result = "";
        }
        if(result == null){
            result = "";
        }else{
            try {
                //This is a workaround for a bug in resolver module on windows.
                //the String returned by resolver is not an URI style
                result = Utilities.normalizeURI(result);
                URI uri = new URI(result);
                if(uri.isOpaque()){
                    if(uri.getScheme().equalsIgnoreCase("file")){
                        StringBuffer resBuff = new StringBuffer(result);
                        result = resBuff.insert("file:".length(), "/").toString();
                    }
                }
            } catch (URISyntaxException ex) {
                return null;
            }
        }
        if(result.length() > 0 ){
            try {
                URI res =  new URI(result);
                return res;
            } catch (URISyntaxException ex) {
            }
        }
        return null;
    }
    
    
//    long lastModTime = 0;
//    protected boolean reparseRequired(List<File> catalogFileList){
//      /* if((apacheCatalogResolverObj == null) || (lastModTime == 0)){
//           //then parse always
//           lastModTime = catalogFileList.get(0).lastModified(); //bother only public catalog for now
//           //System.out.println("Parsing First time: "+lastModTime);
//           return true;
//       }
//       if((apacheCatalogResolverObj != null) && (lastModTime != 0)){
//           if(lastModTime <  catalogFileList.get(0).lastModified()){
//               //System.out.println("Parsing time diff Old: "+lastModTime+" New:"+catalogFileList.get(0).lastModified());
//               lastModTime = catalogFileList.get(0).lastModified();
//               return true;
//           } else{
//               //System.out.println("NOT Parsing time diff Old: "+lastModTime);
//               return false;
//           }
//       }
//       //System.out.println("Parsing Otherwise: "+lastModTime);*/
//        return true;
//    }
//    
//    
    boolean isEditable(File absResourceFile) {
        return true;
    }
    
    
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        try {
            return getInputSource(new URI(systemId));
        } catch (CatalogModelException ex) {
            throw new IOException(ex.getMessage());
        } catch (URISyntaxException e){
            throw new IOException("SystemID not a URL");
        }
    }
    
    
    private InputSource getInputSource(URI locationURI) throws CatalogModelException, IOException {
        logger.entering("CatalogModelImpl", "getInputSource", locationURI);
        File absResourceFile = resolveUsingCatalog(locationURI, null);
        logger.finer("Found  abs file res:"+absResourceFile);
        InputSource result = new InputSource(new FileInputStream(absResourceFile));
        result.setSystemId(locationURI.toString());
        logger.exiting("CatalogModelImpl", "getInputSource", result);
        return result;
    }
    
    
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURIStr) {
        //check for sanity of the systemID
        if((systemId == null) || (systemId.trim().length() <=0 ))
            return null;
        URI systemIdURI = null;
        try {
            systemIdURI = new URI(systemId);
        } catch (URISyntaxException ex) {
            return null;
        }
        FileObject baseFO = null;
        //get the resolver object
        CatalogModel depRez = null;
        try {
            baseFO = getFileObject(baseURIStr);
            depRez = getResolver(baseFO);
        } catch (CatalogModelException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }
        if(depRez == null)
            return null;
        ModelSource baseMS = null;
        try {
            baseMS = createModelSource(baseFO, false);
        } catch (CatalogModelException ex) {
        }
        //get the model source from it
        ModelSource resultMS = null;
        try {
            resultMS = depRez.getModelSource(systemIdURI, baseMS);
        } catch (CatalogModelException ex) {
            return null;
        }
        if(resultMS == null)
            return null;
        //get file object
        FileObject resultFob = (FileObject) resultMS.getLookup().lookup(FileObject.class);
        if(resultFob == null)
            return null;
        //get file
        File resultFile = FileUtil.toFile(resultFob);
        if(resultFile == null)
            return null;
        //get URI out of file
        URI resultURI = resultFile.toURI();
        //create LSInput object
        DOMImplementation domImpl = null;
        try {
            domImpl =  DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
        } catch (ParserConfigurationException ex) {
            return null;
        }
        DOMImplementationLS dols = (DOMImplementationLS) domImpl.getFeature("LS","3.0");
        LSInput lsi = dols.createLSInput();
        Reader is = getFileStreamFromDocument(resultFile);
        if(is != null)
            lsi.setCharacterStream(is);
        lsi.setSystemId(resultURI.toString());
        return lsi;
    }
    
    
    private FileObject getFileObject(String baseURIStr) throws IOException{
        if(baseURIStr == null)
            return null;
        URI baseURI = null;
        try {
            baseURI = new URI(baseURIStr);
        } catch (URISyntaxException ex) {
            IOException ioe = new IOException();
            ioe.initCause(ex);
            throw ioe;
        }
        if(baseURI.isAbsolute()){
            if(baseURI.getScheme().equalsIgnoreCase("file")){ //NOI18N
                File baseFile = null;
                try{
                    baseFile = new File(baseURI);
                } catch(Exception e){
                    IOException ioe = new IOException();
                    ioe.initCause(e);
                    throw ioe;
                }
                baseFile = FileUtil.normalizeFile(baseFile);
                FileObject baseFileObject = null;
                try{
                    baseFileObject = FileUtil.toFileObject(baseFile);
                }catch(Exception e){
                    IOException ioe = new IOException();
                    ioe.initCause(e);
                    throw ioe;
                }
                return baseFileObject;
            }
        }
        return null;
    }
    
    
    private CatalogModel getResolver(FileObject baseFileObject) throws CatalogModelException{
        if(baseFileObject != null && FileOwnerQuery.getOwner(baseFileObject) != null) {
            return CatalogWriteModelFactory.getInstance().getCatalogWriteModelForProject(baseFileObject);
        }
        return this;
    }
    
    
    private Reader getFileStreamFromDocument(File resultFile) {
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(resultFile));
        if(fo != null){
            DataObject dobj = null;
            try {
                dobj = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
                return null;
            }
            if(dobj.isValid() && dobj.isModified()){
                // DataObjectAdapters does not implement getByteStream
                // so calling this here will effectively return null
                return DataObjectAdapters.inputSource(dobj).getCharacterStream();
            } else{
                //return null so that the validator will use normal file path to access doc
                return null;
            }
        }
        return null;
    }
    
    
    protected File resolveProjectProtocol(URI strRes) {
        File result = null;
        Project prj = FileOwnerQuery.getOwner(this.catalogFileObject);
        if(prj != null){
            ProjectCatalogSupport pcs = (ProjectCatalogSupport) prj.getLookup().lookup(ProjectCatalogSupport.class);
            if(pcs.isProjectProtocol(strRes)){
                FileObject resFO = pcs.resolveProjectProtocol(strRes);
                if(resFO != null){
                    return FileUtil.toFile(resFO);
                }
            }
        }
        return result;
    }
    
    
    private ModelSource getModelSourceFromSystemWideCatalog(URI locationURI,
            ModelSource modelSourceOfSourceDocument) {
        if( locationURI == null)
            return null;
        
        try {
            Lookup.Template templ = new Lookup.Template(CatalogModel.class);
            Lookup.Result res = Lookup.getDefault().lookup(templ);
            Collection impls = res.allInstances();
            for(Object obj : impls){
                CatalogModel cm = (CatalogModel) obj;
                return cm.getModelSource(locationURI,
                        modelSourceOfSourceDocument);
            }
        } catch (CatalogModelException ex) {
            //return null
        }
        
        return null;
    }
}

