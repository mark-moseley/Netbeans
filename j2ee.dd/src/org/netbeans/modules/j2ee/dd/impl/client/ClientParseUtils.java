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

package org.netbeans.modules.j2ee.dd.impl.client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.dd.impl.common.ParseUtils;
import org.openide.filesystems.FileObject;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;


/** Class that collects XML parsing utility methods for appclient applications. It is 
 * implementation private for this module, however it is also intended to be used by 
 * the DDLoaders modules, which requires tighter coupling with ddapi and has an 
 * implementation dependency on it.
 *
 * @author Petr Jiricka
 */
public class ClientParseUtils {
  
    private static final Logger LOGGER = Logger.getLogger(ClientParseUtils.class.getName());

    /** Parsing just for detecting the version  SAX parser used
     */
    public static String getVersion(java.io.InputStream is) throws java.io.IOException, SAXException {
        return ParseUtils.getVersion(is, new VersionHandler(), DDResolver.getInstance());
    }
    
    /** Parsing just for detecting the version  SAX parser used
    */
    public static String getVersion(InputSource is) throws IOException, SAXException {
        return ParseUtils.getVersion(is, new VersionHandler(), DDResolver.getInstance());
    }
    
    private static class VersionHandler extends DefaultHandler {
        public void startElement(String uri, String localName, String rawName, Attributes atts) throws SAXException {
            if ("application-client".equals(rawName)) { //NOI18N
                String version = parseVersion(atts.getValue("version")); //NOI18N
                String msg = version != null ? ParseUtils.EXCEPTION_PREFIX + version : "Invalid version " +version; //NO18N
                throw new SAXException(msg);
            }
        }
        
        private String parseVersion(String version){
            if (version == null){
                return null;
            }
            try {
                Double.valueOf(version);
                return version;
            } catch (NumberFormatException nfe) {
                LOGGER.log(Level.INFO, "Not a valid version: " + version, nfe); //NO18N
                return null;
            }
        }
    }
    
    //XXX: note that this resolver does not handle entities from included schemas
    // correctly. See #116379.
    private static class DDResolver implements EntityResolver {
        static DDResolver resolver;
        static synchronized DDResolver getInstance() {
            if (resolver==null) {
                resolver=new DDResolver();
            }
            return resolver;
        }        
        public InputSource resolveEntity(String publicId, String systemId) {
            if ("-//Sun Microsystems, Inc.//DTD J2EE Application Client 1.3//EN".equals(publicId)) {
                return new InputSource("nbres:/org/netbeans/modules/j2ee/dd/impl/resources/application-client_1_3.dtd"); //NOI18N
            } else if ("http://java.sun.com/xml/ns/j2ee/application-client_1_4.xsd".equals(systemId)) {
                return new InputSource("nbres:/org/netbeans/modules/j2ee/dd/impl/resources/application-client_1_4.xsd"); //NOI18N
            } else if ("http://java.sun.com/xml/ns/javaee/application-client_5.xsd".equals(systemId)) {
                return new InputSource("nbres:/org/netbeans/modules/j2ee/dd/impl/resources/application-client_5.xsd"); //NOI18N
            } else {
                // use the default behaviour
                return null;
            }
        }
    }
    
    
    public static SAXParseException parse(FileObject fo)
    throws org.xml.sax.SAXException, java.io.IOException {
        // no need to close the stream, will be closed by the parser, see @org.xml.sax.InputSource
        return parse(new InputSource(fo.getInputStream()));
    }
    
    public static SAXParseException parse (InputSource is) 
            throws org.xml.sax.SAXException, java.io.IOException {
        return ParseUtils.parseDD(is, DDResolver.getInstance());
    }

    /**
     * Parses the given <code>inputSource</code> using the given <code>resolver</code>.
     * @param inputSource the source to parse.
     * @param resolver the resolver to use for parsing.
     * @return the SAX exception encountered during parsing or null if there was
     * no exception.
     */
    public static SAXParseException parse(InputSource inputSource, EntityResolver resolver)
            throws org.xml.sax.SAXException, java.io.IOException {
        return ParseUtils.parseDD(inputSource, resolver);
    }
}
