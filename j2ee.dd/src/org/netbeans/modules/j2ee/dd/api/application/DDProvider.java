/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.dd.api.application;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.netbeans.modules.j2ee.dd.impl.application.ApplicationProxy;
import org.netbeans.modules.schema2beans.Common;
import org.openide.filesystems.*;
import org.xml.sax.*;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

/**
 * Provides access to Deployment Descriptor root ({@link org.netbeans.modules.j2ee.dd.api.ejb.EjbJar} object)
 *
 * @author  Milan Kuchtiak
 */

public final class DDProvider {
    private static final String APP_13_DOCTYPE = "-//Sun Microsystems, Inc.//DTD J2EE Application 1.3//EN"; //NOI18N
    //private static final String EJB_11_DOCTYPE = "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 1.1//EN"; //NOI18N
    private static final DDProvider ddProvider = new DDProvider();
    private Map ddMap;
    
    static java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/dd/Bundle");
    
    /** Creates a new instance of EjbModule */
    private DDProvider() {
        //ddMap=new java.util.WeakHashMap(5);
        ddMap = new java.util.HashMap(5);
    }
    
    /**
    * Accessor method for DDProvider singleton
    * @return DDProvider object
    */
    public static DDProvider getDefault() {
        return ddProvider;
    }
    
    /**
     * Returns the root of deployment descriptor bean graph for given file object.
     * The method is useful for clints planning to read only the deployment descriptor
     * or to listen to the changes.
     * @param fo FileObject representing the ejb-jar.xml file
     * @return EjbJar object - root of the deployment descriptor bean graph
     */
    public synchronized Application getDDRoot(FileObject fo) throws java.io.IOException {
        ApplicationProxy ejbJarProxy = getFromCache (fo);
        if (ejbJarProxy!=null) {
            return ejbJarProxy;
        }
        
        fo.addFileChangeListener(new FileChangeAdapter() {
            public void fileChanged(FileEvent evt) {
                FileObject fo=evt.getFile();
                try {
                    ApplicationProxy ejbJarProxy = getFromCache (fo);
                    String version = null;
                    if (ejbJarProxy!=null) {
                        try {
                            DDParse parseResult = parseDD(fo);
                            version = parseResult.getVersion();
                            setProxyErrorStatus(ejbJarProxy, parseResult);
                            Application newValue = createApplication(parseResult);
                            // replacing original file in proxy EjbJar
                            if (!version.equals(ejbJarProxy.getVersion().toString())) {
                                ejbJarProxy.setOriginal(newValue);
                            } else {// the same version
                                // replacing original file in proxy EjbJar
                                if (ejbJarProxy.getOriginal()==null) {
                                    ejbJarProxy.setOriginal(newValue);
                                } else {
                                    ejbJarProxy.getOriginal().merge(newValue,Application.MERGE_UPDATE);
                                }
                            }
                        } catch (SAXException ex) {
                            if (ex instanceof SAXParseException) {
                                ejbJarProxy.setError((SAXParseException)ex);
                            } else if ( ex.getException() instanceof SAXParseException) {
                                ejbJarProxy.setError((SAXParseException)ex.getException());
                            }
                            ejbJarProxy.setStatus(Application.STATE_INVALID_UNPARSABLE);
                            // cbw if the state of the xml file transitions from
                            // parsable to unparsable this could be due to a user
                            // change or cvs change. We would like to still
                            // receive events when the file is restored to normal
                            // so lets not set the original to null here but wait
                            // until the file becomes parsable again to do a merge
                            //ejbJarProxy.setOriginal(null);
                            ejbJarProxy.setProxyVersion(version);
                        }
                    }
                } catch (java.io.IOException ex){}
            }
        });
        
        try {
            DDParse parseResult = parseDD(fo);
            SAXParseException error = parseResult.getWarning();
            Application original = createApplication(parseResult);
            ejbJarProxy = new ApplicationProxy(original,parseResult.getVersion());
            setProxyErrorStatus(ejbJarProxy, parseResult);
        } catch (SAXException ex) {
            // XXX lets throw an exception here
            ejbJarProxy = new ApplicationProxy(org.netbeans.modules.j2ee.dd.impl.application.model_1_4.Application.createGraph(),"2.0");
            ejbJarProxy.setStatus(Application.STATE_INVALID_UNPARSABLE);
            if (ex instanceof SAXParseException) {
                ejbJarProxy.setError((SAXParseException)ex);
            } else if ( ex.getException() instanceof SAXParseException) {
                ejbJarProxy.setError((SAXParseException)ex.getException());
            }
        }
        ddMap.put(fo, /*new WeakReference*/ (ejbJarProxy));
        return ejbJarProxy;
    }

    /**
     * Returns the root of deployment descriptor bean graph for given file object.
     * The method is useful for clients planning to modify the deployment descriptor.
     * Finally the {@link org.netbeans.modules.j2ee.dd.api.ejb.EjbJar#write(org.openide.filesystems.FileObject)} should be used
     * for writing the changes.
     * @param fo FileObject representing the ejb-jar.xml file
     * @return EjbJar object - root of the deployment descriptor bean graph
     */
    public Application getDDRootCopy(FileObject fo) throws java.io.IOException {
        return (Application)getDDRoot(fo).clone();
    }

    private ApplicationProxy getFromCache (FileObject fo) {
 /*       WeakReference wr = (WeakReference) ddMap.get(fo);
        if (wr == null) {
            return null;
        }
        EjbJarProxy ejbJarProxy = (EjbJarProxy) wr.get ();
        if (ejbJarProxy == null) {
            ddMap.remove (fo);
        }
        return ejbJarProxy;*/
        return (ApplicationProxy) ddMap.get(fo);
    }
    
    /**
     * Returns the root of deployment descriptor bean graph for java.io.File object.
     *
     * @param is source representing the ejb-jar.xml file
     * @return EjbJar object - root of the deployment descriptor bean graph
     */    
    public Application getDDRoot(InputSource is) throws IOException, SAXException {
        DDParse parse = parseDD(is);
        Application ejbJar = createApplication(parse);
        ApplicationProxy proxy = new ApplicationProxy(ejbJar, ejbJar.getVersion().toString());
        setProxyErrorStatus(proxy, parse);
        return proxy;
    }
    
    // PENDING j2eeserver needs BaseBean - this is a temporary workaround to avoid dependency of web project on DD impl
    /**  Convenient method for getting the BaseBean object from CommonDDBean object
     * 
     */
    public org.netbeans.modules.schema2beans.BaseBean getBaseBean(org.netbeans.api.web.dd.common.CommonDDBean bean) {
        if (bean instanceof org.netbeans.modules.schema2beans.BaseBean) return (org.netbeans.modules.schema2beans.BaseBean)bean;
        else if (bean instanceof ApplicationProxy) return (org.netbeans.modules.schema2beans.BaseBean) ((ApplicationProxy)bean).getOriginal();
        return null;
    }

    private static void setProxyErrorStatus(ApplicationProxy ejbJarProxy, DDParse parse) {
        SAXParseException error = parse.getWarning();
        ejbJarProxy.setError(error);
        if (error!=null) {
            ejbJarProxy.setStatus(Application.STATE_INVALID_PARSABLE);
        } else {
            ejbJarProxy.setStatus(Application.STATE_VALID);
        }
    }
    
    private static Application createApplication(DDParse parse) {        
          Application jar = null;
          String version = parse.getVersion();
          if (Application.VERSION_1_4.equals(version)) {
              return new org.netbeans.modules.j2ee.dd.impl.application.model_1_4.Application(parse.getDocument(),  Common.USE_DEFAULT_VALUES);
          } else if (Application.VERSION_1_3.equals(version)) {
              return new org.netbeans.modules.j2ee.dd.impl.application.model_1_3.Application(parse.getDocument(),  Common.USE_DEFAULT_VALUES);
          } 
          
          return jar;
    }
  
    private static class DDResolver implements EntityResolver {
        static DDResolver resolver;
        static synchronized DDResolver getInstance() {
            if (resolver==null) {
                resolver=new DDResolver();
            }
            return resolver;
        }        
        public InputSource resolveEntity (String publicId, String systemId) {
//            if (EJB_11_DOCTYPE.equals(publicId)) { 
                  // return a special input source
//             return new InputSource("nbres:/org/netbeans/modules/j2ee/dd/impl/resources/ejb-jar_1_1.dtd"); //NOI18N
//            } else 
            if (APP_13_DOCTYPE.equals(publicId)) {
                  // return a special input source
             return new InputSource("nbres:/org/netbeans/modules/j2ee/dd/impl/resources/application_1_3.dtd"); //NOI18N
            } else if ("http://java.sun.com/xml/ns/j2ee/application_1_4.xsd".equals(systemId)) {
                return new InputSource("nbres:/org/netbeans/modules/j2ee/dd/impl/resources/application_1_4.xsd"); //NOI18N
            } else {
                // use the default behaviour
                return null;
            }
        }
    }
    
    private static class ErrorHandler implements org.xml.sax.ErrorHandler {
        private int errorType=-1;
        SAXParseException error;

        public void warning(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
            if (errorType<0) {
                errorType=0;
                error=sAXParseException;
            }
            //throw sAXParseException;
        }
        public void error(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
            if (errorType<1) {
                errorType=1;
                error=sAXParseException;
            }
            //throw sAXParseException;
        }        
        public void fatalError(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
            errorType=2;
            throw sAXParseException;
        }
        
        public int getErrorType() {
            return errorType;
        }
        public SAXParseException getError() {
            return error;
        }        
    }

    public SAXParseException parse(FileObject fo) 
    throws org.xml.sax.SAXException, java.io.IOException {
        DDParse parseResult = parseDD(fo);
        return parseResult.getWarning();
    }
    
    private DDParse parseDD (FileObject fo) 
    throws SAXException, java.io.IOException {
        return parseDD(fo.getInputStream());
    }
    
    private DDParse parseDD (InputStream is) 
    throws SAXException, java.io.IOException {
        return parseDD(new InputSource(is));
    }
    
    private DDParse parseDD (InputSource is) 
    throws SAXException, java.io.IOException {
        DDProvider.ErrorHandler errorHandler = new DDProvider.ErrorHandler();
        org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();
        parser.setErrorHandler(errorHandler);
        parser.setEntityResolver(DDProvider.DDResolver.getInstance());
        // XXX do we need validation here, if no one is using this then
        // the dependency on xerces can be removed and JAXP can be used
        parser.setFeature("http://xml.org/sax/features/validation", true); //NOI18N
        parser.setFeature("http://apache.org/xml/features/validation/schema", true); // NOI18N
        parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true); //NOI18N
        parser.parse(is);
        Document d = parser.getDocument();
        SAXParseException error = errorHandler.getError();
        return new DDParse(d, error);
    }
    
    /**
     * This class represents one parse of the deployment descriptor
     */
    private static class DDParse {
        private Document document;
        private SAXParseException saxException;
        private String version;
        public DDParse(Document d, SAXParseException saxEx) {
            document = d;
            saxException = saxEx;
            extractVersion();
        }
        
        /**
         * @return document from last parse
         */
        public Document getDocument() {
            return document;
        }
        
        /**
         * @return version of deployment descriptor. 
         */
        private void extractVersion () {
            // first check the doc type to see if there is one
            DocumentType dt = document.getDoctype();
            // This is the default version
            version = Application.VERSION_1_4;
            if (dt != null) {
                if (APP_13_DOCTYPE.equals(dt.getPublicId())) {
                    version = Application.VERSION_1_3;
                }
                //if (EJB_11_DOCTYPE.equals(dt.getPublicId())){
                //    version = Application.VERSION_1_1;
                //}
            }
        }
        
        public String getVersion() {
            return version;
        }
        
        /** 
         * @return validation error encountered during the parse
         */
        public SAXParseException getWarning() {
            return saxException;
        }
    }
    
}
