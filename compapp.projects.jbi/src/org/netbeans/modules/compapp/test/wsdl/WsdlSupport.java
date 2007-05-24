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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.compapp.test.wsdl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.xmlbeans.XmlException;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import java.util.logging.Logger;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModelFactory;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * WsdlSupport.java
 *
 * Created on February 2, 2006, 11:48 AM
 *
 */
public class WsdlSupport {
    private static final Logger mLog =Logger.getLogger("org.netbeans.modules.compapp.test.wsdl.WsdlSupport"); // NOI18N
    
    private String mWsdlUrl;
    private WSDLModel mWsdlModel;
    private SchemaTypeLoader mSchemaTypeLoader;
    private String wsdlSupportErrStr = ""; //NOI18N
    
    /** Creates a new instance of WsdlSupport */
    public WsdlSupport(FileObject wsdlFile) {
        mWsdlUrl = "file:" + FileUtil.toFile(wsdlFile).getPath();
        
        try {
            ModelSource wsdlModelSource = Utilities.createModelSource(wsdlFile, false);                
            mWsdlModel = WSDLModelFactory.getDefault().getModel(wsdlModelSource);
        
            mSchemaTypeLoader = loadSchemaTypes(mWsdlUrl);
        } catch (Exception e) {
            String msg = NbBundle.getMessage(WsdlSupport.class, "LBL_Fail_to_load_schema_types", mWsdlUrl); // NOI18N
            if (e.getMessage() != null) {
                msg += "\n" + e.getMessage();  // NOI18N
            }
            wsdlSupportErrStr += msg;
            mLog.log(Level.SEVERE, msg, e); // NOI18N
            
//            NotifyDescriptor d =
//                    new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
//            DialogDisplayer.getDefault().notify(d);
        }
    }
    
    public WSDLModel getWsdlModel() {
        return mWsdlModel;
    }
        
    public SchemaTypeLoader getSchemaTypeLoader() {
        return mSchemaTypeLoader;
    }
    
    public String getWsdlSupportError() {
        return wsdlSupportErrStr;
    }
    
    public void setWsdlSupportError(String s) {
        wsdlSupportErrStr = s;
    }
    
    private static SchemaTypeLoader loadSchemaTypes(String wsdlUrl) 
            throws XmlException, SchemaException {
        
        Map<String, XmlObject> schemaTable = new HashMap<String, XmlObject>();        
        getSchemas(wsdlUrl, schemaTable, new ArrayList<String>());
        
        List<XmlObject> schemaList = 
                new ArrayList<XmlObject>(schemaTable.values());        
        for (XmlObject schema : schemaList) {
            removeImportAndInclude(schema);
        }
        
        SchemaTypeLoader schemaTypes = loadSchemaTypes(schemaList);
        return schemaTypes;
    }
    
    private static SchemaTypeLoader loadSchemaTypes(List schemaList) throws XmlException {
        XmlOptions options = new XmlOptions();
        options.setCompileNoValidation();
        options.setCompileNoPvrRule();
        options.setCompileDownloadUrls();
        options.setCompileNoUpaRule();
        options.setValidateTreatLaxAsSkip();
        
        options.setLoadStripProcinsts();
        options.setCompileNoAnnotations();
        options.setLoadStripComments();
        options.setLoadStripWhitespace();
        options.setLoadTrimTextBuffer();
        
        ArrayList errorList = new ArrayList();
        options.setErrorListener(errorList);
        
        try {
            schemaList.add(XmlObject.Factory.parse(WsdlSupport.class.getResource(
                    "/org/netbeans/modules/compapp/test/wsdl/resources/soapEncoding.xsd"))); // NOI18N
            XmlObject[] schemaArray = (XmlObject[])schemaList.toArray(new XmlObject[0]);
            return XmlBeans.loadXsd(schemaArray, options);
        } catch (IOException e) {
            //throw new SchemaException(e, errorList);
            e.printStackTrace();
            return null;
        }
    }
    
    private static void getSchemas(
            String wsdlUrl,
            Map<String, XmlObject> schemaTable,
            List<String> visitedSchemaWsdls)
            throws SchemaException {
        
        if(schemaTable.containsKey(wsdlUrl)) {
            return;
        }
        ArrayList errorList = new ArrayList();
        
        Map<String, XmlObject> result = new HashMap<String, XmlObject>();
        
        try {
            XmlOptions options = new XmlOptions();
            options.setCompileNoValidation();
            options.setSaveUseOpenFrag();
            options.setErrorListener(errorList);
            
            options.setLoadStripProcinsts();
            options.setCompileNoAnnotations();
            options.setLoadStripComments();
            options.setLoadStripWhitespace();
            options.setLoadTrimTextBuffer();
            
            options.setSaveSyntheticDocumentElement(
                    new QName("http://www.w3.org/2001/XMLSchema", "schema")); // NOI18N
            
            XmlObject xmlObject = XmlObject.Factory.parse(new URL(wsdlUrl), options);
            
            Document dom = (Document) xmlObject.getDomNode();
            Node domNode = dom.getDocumentElement();
            if (domNode.getLocalName().equals("schema") && // NOI18N
                domNode.getNamespaceURI().equals(
                    "http://www.w3.org/2001/XMLSchema")) { // NOI18N
                result.put(wsdlUrl, xmlObject);
            } else {
                XmlObject[] schemas = xmlObject.selectPath(
                        "declare namespace s='http://www.w3.org/2001/XMLSchema' .//s:schema"); // NOI18N
                
                for (int i = 0; i < schemas.length; i++) {
                    XmlCursor xmlCursor = schemas[i].newCursor();
                    String xmlText = xmlCursor.getObject().xmlText(options);
                    schemas[i] = XmlObject.Factory.parse(xmlText, options);
                    schemas[i].documentProperties().setSourceName(wsdlUrl);
                    
                    result.put(wsdlUrl + "@" + (i+1), schemas[i]); // NOI18N
                }
                
                XmlObject[] wsdlImports = xmlObject.selectPath(
                        "declare namespace s='http://schemas.xmlsoap.org/wsdl/' .//s:import/@location"); // NOI18N
                for (int i = 0; i < wsdlImports.length; i++) {
                    String location = ((SimpleValue) wsdlImports[i]).getStringValue();
                    if (location != null) {
                        if (!location.startsWith("file:") && location.indexOf("://") <= 0) { // NOI18N
                            location = resolveRelativeUrl(wsdlUrl, location);
                        }
                        getSchemas(location, schemaTable, visitedSchemaWsdls);                        
                    }
                }
            }
            
            XmlObject[] schemas = (XmlObject[])result.values().toArray(
                    new XmlObject[result.size()]);
            
            for (int c = 0; c < schemas.length; c++) {
                xmlObject = schemas[c];
                
                XmlObject[] schemaImports = xmlObject.selectPath(
                        "declare namespace s='http://www.w3.org/2001/XMLSchema' .//s:import/@schemaLocation"); // NOI18N
                XmlObject[] schemaIncludes = xmlObject.selectPath(
                        "declare namespace s='http://www.w3.org/2001/XMLSchema' .//s:include/@schemaLocation"); // NOI18N
                List<XmlObject> schemaImportsList = Arrays.asList(schemaImports);
                List<XmlObject> schemaIncludesList = Arrays.asList(schemaIncludes);
                List<XmlObject> schemaImportsAndIncludes = new ArrayList<XmlObject>();
                schemaImportsAndIncludes.addAll(schemaImportsList);
                schemaImportsAndIncludes.addAll(schemaIncludesList);
                
                for (XmlObject schemaImportOrInclude : schemaImportsAndIncludes) {
                    String location = ((SimpleValue)schemaImportOrInclude).getStringValue();
                    if (location != null &&
                            // We will be adding soap encoding later. This is to
                            // avoid duplicate global type definition error.
                            !location.equals("http://schemas.xmlsoap.org/soap/encoding/")) { // NOI18N
                        if (!location.startsWith("file:") && location.indexOf("://") <= 0) { // NOI18N
                            location = resolveRelativeUrl(wsdlUrl, location);
                        }
                        if (!visitedSchemaWsdls.contains(location)) {
                            visitedSchemaWsdls.add(location);
                            getSchemas(location, schemaTable, visitedSchemaWsdls);
                        }
                    }
                }
            }
            schemaTable.putAll(result);
        } catch (Exception e) {
            throw new SchemaException(e, errorList);
        }
    }
    
    private static void removeImportAndInclude(XmlObject xmlObject) throws XmlException {
        XmlObject[] imports = xmlObject.selectPath(
                "declare namespace s='http://www.w3.org/2001/XMLSchema' .//s:import"); // NOI18N
        
        for(int c = 0; c < imports.length; c++) {
            imports[c].newCursor().removeXml();
        }
        
        XmlObject[] includes = xmlObject.selectPath(
                "declare namespace s='http://www.w3.org/2001/XMLSchema' .//s:include"); // NOI18N
        
        for(int c = 0; c < includes.length; c++) {
            includes[c].newCursor().removeXml();
        }
    }
    
    private static String resolveRelativeUrl(String baseUrl, String url) {
        if (baseUrl.startsWith("file:")) { // NOI18N
            FileObject fo = FileUtil.toFileObject(new File(baseUrl.substring(5)));
            
            try {
                ModelSource modelSourceOfSourceDocument = 
                        Utilities.createModelSource(fo, false);  

                CatalogWriteModel model = 
                        CatalogWriteModelFactory.getInstance().getCatalogWriteModelForProject(fo);

                ModelSource modelSource = 
                        model.getModelSource(new URI(url), modelSourceOfSourceDocument);
                FileObject targetFO = modelSource.getLookup().lookup(FileObject.class);
                System.out.println(targetFO);
                return "file:" + targetFO.getPath();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        baseUrl = baseUrl.replaceAll("\\\\", "/"); // NOI18N
        int ix = baseUrl.lastIndexOf('/'); // NOI18N
        if (ix == -1) {
            ix = baseUrl.lastIndexOf('/'); // NOI18N
        }
        
        while(url.startsWith("../")) { // NOI18N
            int ix2 = baseUrl.lastIndexOf('/', ix-1); // NOI18N
            if(ix2 == -1) {
                break;
            }
            baseUrl = baseUrl.substring(0, ix2+1);
            ix = ix2;
            
            url = url.substring(3);
        }
        
        return baseUrl.substring(0, ix+1) + url;
    }
    
}
