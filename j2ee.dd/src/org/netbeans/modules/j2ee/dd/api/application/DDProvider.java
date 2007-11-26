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

package org.netbeans.modules.j2ee.dd.api.application;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.impl.application.ApplicationProxy;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.Common;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Provides access to Deployment Descriptor root ({@link Application} object)
 *
 * @author  Milan Kuchtiak
 */
public final class DDProvider {
    
    private static final String APP_13_DOCTYPE = "-//Sun Microsystems, Inc.//DTD J2EE Application 1.3//EN"; //NOI18N
    private static final DDProvider ddProvider = new DDProvider();
    
    private final Map ddMap;

    private static final Logger LOGGER = Logger.getLogger(DDProvider.class.getName());

    static ResourceBundle bundle = ResourceBundle.getBundle("org/netbeans/modules/j2ee/dd/Bundle");
    
    private DDProvider() {
        //ddMap=new java.util.WeakHashMap(5);
        ddMap = new HashMap(5);
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
     * The method is useful for clients planning to read only the deployment descriptor
     * or to listen to the changes.
     * @param fo FileObject representing the application.xml file
     * @return Application object - root of the deployment descriptor bean graph
     */
    public synchronized Application getDDRoot(FileObject fo) throws IOException {
        if (fo == null) {
            return null;
        }
        ApplicationProxy applicationProxy = null;
        synchronized (ddMap) {
            applicationProxy = getFromCache (fo);
            if (applicationProxy!=null) {
                return applicationProxy;
            }
        }
        
        fo.addFileChangeListener(new FileChangeAdapter() {
            @Override
            public void fileChanged(FileEvent evt) {
                FileObject fo=evt.getFile();
                try {
                    synchronized (ddMap) {
                        ApplicationProxy applicationProxy = getFromCache (fo);
                        String version = null;
                        if (applicationProxy!=null) {
                            try {
                                DDParse parseResult = parseDD(fo);
                                version = parseResult.getVersion();
                                setProxyErrorStatus(applicationProxy, parseResult);
                                Application newValue = createApplication(parseResult);
                                // replacing original file in proxy EjbJar
                                if (!version.equals(applicationProxy.getVersion().toString())) {
                                    applicationProxy.setOriginal(newValue);
                                } else {// the same version
                                    // replacing original file in proxy EjbJar
                                    if (applicationProxy.getOriginal()==null) {
                                        applicationProxy.setOriginal(newValue);
                                    } else {
                                        applicationProxy.getOriginal().merge(newValue,Application.MERGE_UPDATE);
                                    }
                                }
                            } catch (SAXException ex) {
                                if (ex instanceof SAXParseException) {
                                    applicationProxy.setError((SAXParseException)ex);
                                } else if ( ex.getException() instanceof SAXParseException) {
                                    applicationProxy.setError((SAXParseException)ex.getException());
                                }
                                applicationProxy.setStatus(Application.STATE_INVALID_UNPARSABLE);
                                // cbw if the state of the xml file transitions from
                                // parsable to unparsable this could be due to a user
                                // change or cvs change. We would like to still
                                // receive events when the file is restored to normal
                                // so lets not set the original to null here but wait
                                // until the file becomes parsable again to do a merge
                                //ejbJarProxy.setOriginal(null);
                                applicationProxy.setProxyVersion(version);
                            }
                        }
                    }
                } catch (IOException ex){
                    LOGGER.log(Level.INFO, "Merging of Application graphs failed", ex); //NOI18N
                }
            }
        });
        
        try {
            DDParse parseResult = parseDD(fo);
            Application original = createApplication(parseResult);
            applicationProxy = new ApplicationProxy(original,parseResult.getVersion());
            setProxyErrorStatus(applicationProxy, parseResult);
        } catch (SAXException ex) {
            // XXX lets throw an exception here
            applicationProxy = new ApplicationProxy(org.netbeans.modules.j2ee.dd.impl.application.model_1_4.Application.createGraph(),"2.0");
            applicationProxy.setStatus(Application.STATE_INVALID_UNPARSABLE);
            if (ex instanceof SAXParseException) {
                applicationProxy.setError((SAXParseException)ex);
            } else if ( ex.getException() instanceof SAXParseException) {
                applicationProxy.setError((SAXParseException)ex.getException());
            }
        }
        synchronized(ddMap){
            ApplicationProxy cached = getFromCache(fo);
            if (cached != null){
                return cached;
            }
            ddMap.put(fo, /*new WeakReference*/ (applicationProxy));
        }
        return applicationProxy;
    }

    /**
     * Returns the root of deployment descriptor bean graph for given file object.
     * The method is useful for clients planning to modify the deployment descriptor.
     * Finally the {@link Application#write(org.openide.filesystems.FileObject)} should be used
     * for writing the changes.
     * @param fo FileObject representing the application.xml file
     * @return Application object - root of the deployment descriptor bean graph
     */
    public Application getDDRootCopy(FileObject fo) throws IOException {
        return (Application)getDDRoot(fo).clone();
    }

    private ApplicationProxy getFromCache (FileObject fo) {
        return (ApplicationProxy) ddMap.get(fo);
    }
    
    /**
     * Returns the root of deployment descriptor bean graph for java.io.File object.
     *
     * @param is source representing the application.xml file
     * @return Application object - root of the deployment descriptor bean graph
     */    
    public Application getDDRoot(InputSource is) throws IOException, SAXException {
        DDParse parse = parseDD(is);
        Application application = createApplication(parse);
        ApplicationProxy proxy = new ApplicationProxy(application, application.getVersion().toString());
        setProxyErrorStatus(proxy, parse);
        return proxy;
    }
    
    // PENDING j2eeserver needs BaseBean - this is a temporary workaround to avoid dependency of web project on DD impl
    /** 
     * Convenient method for getting the BaseBean object from CommonDDBean object.
     */
    public BaseBean getBaseBean(CommonDDBean bean) {
        if (bean instanceof BaseBean) {
            return (BaseBean)bean;
        } else if (bean instanceof ApplicationProxy) {
            return (BaseBean) ((ApplicationProxy)bean).getOriginal();
        }
        return null;
    }

    private static void setProxyErrorStatus(ApplicationProxy applicationProxy, DDParse parse) {
        SAXParseException error = parse.getWarning();
        applicationProxy.setError(error);
        if (error!=null) {
            applicationProxy.setStatus(Application.STATE_INVALID_PARSABLE);
        } else {
            applicationProxy.setStatus(Application.STATE_VALID);
        }
    }
    
    private static Application createApplication(DDParse parse) {        
          Application jar = null;
          String version = parse.getVersion();
          if (Application.VERSION_1_4.equals(version)) {
              return new org.netbeans.modules.j2ee.dd.impl.application.model_1_4.Application(parse.getDocument(),  Common.USE_DEFAULT_VALUES);
          } else if (Application.VERSION_1_3.equals(version)) {
              return new org.netbeans.modules.j2ee.dd.impl.application.model_1_3.Application(parse.getDocument(),  Common.USE_DEFAULT_VALUES);
          } else if (Application.VERSION_5.equals(version)) {
              return new org.netbeans.modules.j2ee.dd.impl.application.model_5.Application(parse.getDocument(),  Common.USE_DEFAULT_VALUES);
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
            if (APP_13_DOCTYPE.equals(publicId)) {
                  // return a special input source
             return new InputSource("nbres:/org/netbeans/modules/j2ee/dd/impl/resources/application_1_3.dtd"); //NOI18N
            } else if ("http://java.sun.com/xml/ns/j2ee/application_1_4.xsd".equals(systemId)) {
                return new InputSource("nbres:/org/netbeans/modules/j2ee/dd/impl/resources/application_1_4.xsd"); //NOI18N
            } else if ("http://java.sun.com/xml/ns/javaee/application_5.xsd".equals(systemId)) {
                return new InputSource("nbres:/org/netbeans/modules/javaee/dd/impl/resources/application_5.xsd"); //NOI18N
            } else {
                // use the default behaviour
                return null;
            }
        }
    }
    
    private static class ErrorHandler implements org.xml.sax.ErrorHandler {
        private int errorType=-1;
        SAXParseException error;

        public void warning(SAXParseException sAXParseException) throws SAXException {
            if (errorType<0) {
                errorType=0;
                error=sAXParseException;
            }
            //throw sAXParseException;
        }
        public void error(SAXParseException sAXParseException) throws SAXException {
            if (errorType<1) {
                errorType=1;
                error=sAXParseException;
            }
            //throw sAXParseException;
        }        
        public void fatalError(SAXParseException sAXParseException) throws SAXException {
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
    throws SAXException, IOException {
        DDParse parseResult = parseDD(fo);
        return parseResult.getWarning();
    }
    
    private DDParse parseDD (FileObject fo) 
    throws SAXException, IOException {
        return parseDD(fo.getInputStream());
    }
    
    private DDParse parseDD (InputStream is) 
    throws SAXException, IOException {
        return parseDD(new InputSource(is));
    }
    
    private DDParse parseDD (InputSource is) 
    throws SAXException, IOException {
        DDProvider.ErrorHandler errorHandler = new DDProvider.ErrorHandler();
        
        DocumentBuilder parser=null;
        try {
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            parser = fact.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new SAXException(ex.getMessage());
        }
        parser.setErrorHandler(errorHandler);
        parser.setEntityResolver(DDResolver.getInstance());
        Document d = parser.parse(is);
        SAXParseException error = errorHandler.getError();
        return new DDParse(d, error);
    }
    
    /**
     * This class represents one parse of the deployment descriptor
     */
    private static class DDParse {
        private final Document document;
        private final SAXParseException saxException;
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
            // This is the default version
            version = Application.VERSION_5;
            
            // first check the doc type to see if there is one
            DocumentType dt = document.getDoctype();

            if(dt == null) {
                //check application node version attribute
                NodeList nl = document.getElementsByTagName("application");//NOI18N
                if(nl != null && nl.getLength() > 0) {
                    Node appNode = nl.item(0);
                    NamedNodeMap attrs = appNode.getAttributes();
                    Node vNode = attrs.getNamedItem("version");//NOI18N
                    if(vNode != null) {
                        String versionValue = vNode.getNodeValue();
                        if(Application.VERSION_1_4.equals(versionValue)) {
                            version = Application.VERSION_1_4;
                        } else if(Application.VERSION_1_3.equals(versionValue)) {
                            version = Application.VERSION_1_3;
                        } else {
                            version = Application.VERSION_5; //default
                        }
                    }
                }
            } else {
                if (APP_13_DOCTYPE.equals(dt.getPublicId())) {
                    version = Application.VERSION_1_3;
                }
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
