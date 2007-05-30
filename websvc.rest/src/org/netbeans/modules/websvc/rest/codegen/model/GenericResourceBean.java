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
package org.netbeans.modules.websvc.rest.codegen.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.websvc.rest.codegen.Constants;
import org.netbeans.modules.websvc.rest.codegen.model.GenericResourceBean.HttpMethodType;

/**
 * Meta model for generic REST resource class.
 * 
 * @author nam
 */
public class GenericResourceBean {
    public static enum HttpMethodType { GET, PUT, POST, DELETE }
    public static String MIME_TYPE_TEXT = "text/plain";
    public static String MIME_TYPE_TEXT_HTML = "text/html";
    public static String MIME_TYPE_XML = "application/xml";
    public static String MIME_TYPE_JASON = "application/json";
    public static String[] supportedMimeTypes = new String[] { 
        MIME_TYPE_TEXT, 
        MIME_TYPE_TEXT_HTML,
        //MIME_TYPE_XML
    };
    
    private String name;
    private String packageName;

    private String uriTemplate;
    private Set<String> mimeTypes;
    private Set<HttpMethodType> methodTypes;

    public GenericResourceBean(String name, String packageName, String uriTemplate) {
         this(name, packageName, uriTemplate, supportedMimeTypes, HttpMethodType.values());
     }
    
    private GenericResourceBean(String name, String packageName, String uriTemplate, 
            String[] mediaTypes, 
            HttpMethodType[] methodTypes) {
        this.name = name;
        this.packageName = packageName;
        this.uriTemplate = uriTemplate;
        this.mimeTypes = new HashSet(Arrays.asList(mediaTypes));
        this.methodTypes = new HashSet(Arrays.asList(methodTypes));
    }

    public static String[] getSupportedMimeTypes() {
        return supportedMimeTypes;
    }
    
    public String getName() {
        return name;
    }

    public String getPackageName() { 
        return packageName;
    }

    public String getUriTemplate() {
        return uriTemplate;
    }

    public void setUriTemplate(String uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    public Set<String> getMimeTypes() {
        return mimeTypes;
    }

    public void addMimeType(String mimeType) {
        this.mimeTypes.add(mimeType);
    }

    public Set<HttpMethodType> getMethodTypes() {
        return methodTypes;
    }

    public void addMethodType(HttpMethodType type) {
        methodTypes.add(type);
    }

    private String[] uriParams = null;
    public String[] getUriParams() {
        if (uriParams == null) {
            uriParams = getUriParams(uriTemplate);
        }
        return uriParams;
    }
    
    public static String[] getUriParams(String template) {
        if (template == null) {
            return new String[0];
        }
        String[] segments = template.split("/");
        List<String> res = new ArrayList<String>();
        for (String segment : segments) {
            if (segment.startsWith("{")) {
                if (segment.length() > 2 && segment.endsWith("}")) {
                    res.add(segment.substring(1, segment.length()-1));
                } else {
                    throw new IllegalArgumentException(template);
                }
            }
        }
        return res.toArray(new String[res.size()]);
    }
    
    public String getQualifiedClassName() {
        return getPackageName() + "." + getName();
    }

}
