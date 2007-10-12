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

package org.netbeans.modules.websvc.rest.codegen;

import javax.lang.model.element.Modifier;

/**
 *
 * @author PeterLiu
 */
public class Constants {
    
    public static final String REST_API_PACKAGE = "javax.ws.rs.";       //NOI18N
    
    public static final String WEB_APPLICATION_EXCEPTION = REST_API_PACKAGE + "WebApplicationException";
    
    public static final String URI_TEMPLATE = REST_API_PACKAGE + "UriTemplate";        //NOI18N
    
    public static final String HTTP_METHOD = REST_API_PACKAGE + "HttpMethod";         //NOI18N
    
    public static final String PRODUCE_MIME = REST_API_PACKAGE + "ProduceMime";        //NOI18N
    
    public static final String CONSUME_MIME = REST_API_PACKAGE + "ConsumeMime";    //NOI18N
    
    public static final String URI_PARAM = REST_API_PACKAGE + "UriParam";  //NOI18N
    
    public static final String QUERY_PARAM = REST_API_PACKAGE + "QueryParam";  //NOI18N
    
    public static final String DEFAULT_VALUE = REST_API_PACKAGE + "DefaultValue";       //NOI18N
    
    public static final String HTTP_RESPONSE = REST_API_PACKAGE + "core.Response"; //NOI18N
    
    public static final String RESPONSE_BUILDER = REST_API_PACKAGE + "core.Response.Builder";       //NOI8N
    
    public static final String ENTITY_TYPE = REST_API_PACKAGE + "Entity";
    
    public static final String HTTP_CONTEXT = REST_API_PACKAGE + "core.HttpContext";    //NOI18N
    
    public static final String URI_INFO = REST_API_PACKAGE + "core.UriInfo";     //NOI18N
    
    public static final String URI_TYPE = "java.net.URI";       //NOI18N
    
    public static final String QUERY_TYPE = "javax.persistence.Query";       //NOI18N
    
    public static final String ENTITY_MANAGER_TYPE = "javax.persistence.EntityManager";       //NOI18N
    
    public static final String ENTITY_MANAGER_FACTORY = "javax.persistence.EntityManagerFactory";       //NOI18N
    
    public static final String ENTITY_TRANSACTION = "javax.persistence.EntityTransaction";
    
    public static final String PERSISTENCE = "javax.persistence.Persistence";
    
    public static final String NO_RESULT_EXCEPTION = "javax.persistence.NoResultException";        //NOI18N
    
    public static final String XML_ROOT_ELEMENT = "javax.xml.bind.annotation.XmlRootElement";             //NOI18N
    
    public static final String XML_ELEMENT = "javax.xml.bind.annotation.XmlElement";                 //NOI18N
    
    public static final String XML_ATTRIBUTE = "javax.xml.bind.annotation.XmlAttribute";                 //NOI18N
    
    public static final String XML_TRANSIENT = "javax.xml.bind.annotation.XmlTransient";                 //NOI18N
    
    public static final String VOID = "void";           //NOI18N
    
    public static final String COLLECTION_TYPE = "java.util.Collection"; //NOI18N
    
    public static final String COLLECTIONS_TYPE = "java.util.Collections";  //NOI18N
    
    public static final String ARRAY_LIST_TYPE = "java.util.ArrayList"; //NOI18N
    
    public static final String HTTP_CONTEXT_ANNOTATION = "HttpContext";     //NOI18N
    
    public static final String URI_TEMPLATE_ANNOTATION = "UriTemplate"; //NOI18N
    
    public static final String URI_PARAM_ANNOTATION = "UriParam";       //NOI18N
    
    public static final String QUERY_PARAM_ANNOTATION = "QueryParam";       //NOI18N
    
    public static final String DEFAULT_VALUE_ANNOTATION = "DefaultValue";       //NOI18N
    
    public static final String HTTP_METHOD_ANNOTATION = "HttpMethod";   //NOI18N
    
    public static final String PRODUCE_MIME_ANNOTATION = "ProduceMime"; //NOI18N
    
    public static final String CONSUME_MIME_ANNOTATION = "ConsumeMime"; //NOI18N
    
    public static final String XML_TRANSIENT_ANNOTATION = "XmlTransient"; //NOI18N
   
    public static final String XML_ROOT_ELEMENT_ANNOTATION = "XmlRootElement";  //NOI18N
    
    public static final String XML_ELEMENT_ANNOTATION = "XmlElement";  //NOI18N
    
    public static final String XML_ATTRIBUTE_ANNOTATION = "XmlAttribute";  //NOI18N
    
    public static final Modifier[] PUBLIC = new Modifier[] { Modifier.PUBLIC };
    
    public static final Modifier[] PRIVATE = new Modifier[] { Modifier.PRIVATE };
    
    public static final Modifier[] PROTECTED = new Modifier[] { Modifier.PROTECTED };
    
    public static final Modifier[] PRIVATE_STATIC = new Modifier[] {
        Modifier.PRIVATE, Modifier.STATIC };
    
    public static final Modifier[] PUBLIC_STATIC = new Modifier[] {
        Modifier.PUBLIC, Modifier.STATIC
    };
   
    public static final Modifier[] PUBLIC_STATIC_FINAL = new Modifier[] {
        Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL
    };
   
    public static final String JAVA_EXT = "java"; //NI18N
    
    public enum MimeType {
        XML("application/xml", "Xml"),      //NOI18N
        JSON("application/json", "Json"),   //NOI18N
        TEXT("text/plain", "Text"),         //NOI18N
        HTML("text/html", "Html");          //NOI18N
        
        private String value;
        private String suffix;
        
        MimeType(String value, String suffix) {
            this.value = value;
            this.suffix = suffix;
        }
        
        public String value() {
            return value;
        }
        
        public String suffix() {
            return suffix;
        }
        
        public static MimeType find(String value) {
            for(MimeType m:values()) {
                if(m.value().equals(value))
                    return m;
            }
            return null;
        }
        
        public String toString() {
            return value;
        }
    }
    
    public enum HttpMethodType {
        GET("get"),             //NOI18N
        PUT("put"),             //NOI18N
        POST("post"),           //NOI18N
        DELETE("delete");       //NOI18N
        
        private String prefix; 
        
        HttpMethodType(String prefix) {
            this.prefix = prefix;
        }
        
        public String value() {
            return name();
        }
        
        public String prefix() {
            return prefix;
        }
    }
    
    public static final String REST_STUBS = "resources"; //NOI18N
    
    public static final String PASSWORD = "password"; //NOI18N
}
