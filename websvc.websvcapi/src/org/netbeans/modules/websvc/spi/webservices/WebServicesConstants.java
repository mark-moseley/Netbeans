/*
 * WebServicesConstants.java
 *
 * Created on November 5, 2004, 3:32 PM
 */

package org.netbeans.modules.websvc.spi.webservices;

/**
 *
 * @author  rico
 */
public interface WebServicesConstants {
    public static final String WEBSERVICES_DD = "webservices";//NOI18N
    public static final String WEB_SERVICES =     "web-services";//NOI18N
    public static final String WEB_SERVICE  =     "web-service";//NOI18N
    public static final String WEB_SERVICE_NAME = "web-service-name";//NOI18N
    public static final String CONFIG_PROP_SUFFIX = ".config.name";//NOI18N
    public static final String MAPPING_PROP_SUFFIX = ".mapping";//NOI18N
    public static final String MAPPING_FILE_SUFFIX = "-mapping.xml";//NOI18N
    public static final String WebServiceServlet_PREFIX = "WSServlet_";//NOI18N

    public static final String WSDL_FOLDER = "wsdl"; // NOI18N
    public static final String WEB_SERVICE_CLIENTS = "web-service-clients"; //NOI18N
    public static final String WEB_SERVICE_CLIENT = "web-service-client"; //NOI18N
    public static final String WEB_SERVICE_CLIENT_NAME = "web-service-client-name"; //NOI18N
    public static final String WEB_SERVICE_STUB_TYPE = "web-service-stub-type"; //NOI18N
    public static final String WSCOMPILE_CLASSPATH = "wscompile.classpath"; //NOI18N
    public static final String WSCOMPILE_TOOLS_CLASSPATH = "wscompile.tools.classpath"; //NOI18N
    public static final String WEBSVC_GENERATED_DIR = "websvc.generated.dir"; // NOI18N
    public static final String [] WSCOMPILE_JARS = {
        "${libs.j2ee14.classpath}",
        "${libs.jaxrpc11.classpath}",
        "${libs.saaj12.classpath}",
        "${wscompile.tools.classpath}"
    };
}
