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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.rest.model.api;

/**
 * JSR-311 annotation constants
 * 
 * @author nam
 */
public class RestConstants {

    public static final String PATH_ANNOTATION = "Path"; //NOI18N
    
    public static final String PATH_PARAM_ANNOTATION = "PathParam";       //NOI18N
    
    public static final String QUERY_PARAM_ANNOTATION = "QueryParam";       //NOI18N
    
    public static final String DEFAULT_VALUE_ANNOTATION = "DefaultValue";       //NOI18N
    
    public static final String GET_ANNOTATION = "GET";   //NOI18N
    
    public static final String POST_ANNOTATION = "POST";   //NOI18N
    
    public static final String PUT_ANNOTATION = "PUT";   //NOI18N
    
    public static final String DELETE_ANNOTATION = "DELETE";   //NOI18N
    
    public static final String PRODUCE_MIME_ANNOTATION = "ProduceMime"; //NOI18N
    
    public static final String CONSUME_MIME_ANNOTATION = "ConsumeMime"; //NOI18N
    
    public static final String CONTEXT_ANNOTATION = "Context";     //NOI18N
    
    public static final String REST_API_PACKAGE = "javax.ws.rs.";       //NOI18N
    
    public static final String PATH = REST_API_PACKAGE + PATH_ANNOTATION;
    
    public static final String GET = REST_API_PACKAGE + GET_ANNOTATION;
    
    public static final String POST = REST_API_PACKAGE + POST_ANNOTATION;
    
    public static final String PUT = REST_API_PACKAGE + PUT_ANNOTATION;
    
    public static final String DELETE = REST_API_PACKAGE + DELETE_ANNOTATION;
    
    public static final String PRODUCE_MIME = REST_API_PACKAGE + PRODUCE_MIME_ANNOTATION;
    
    public static final String CONSUME_MIME = REST_API_PACKAGE + CONSUME_MIME_ANNOTATION;
    
    public static final String PATH_PARAM = REST_API_PACKAGE + PATH_PARAM_ANNOTATION;
    
    public static final String QUERY_PARAM = REST_API_PACKAGE + QUERY_PARAM_ANNOTATION;
    
    public static final String DEFAULT_VALUE = REST_API_PACKAGE + DEFAULT_VALUE_ANNOTATION;
    
}
