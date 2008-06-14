/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.tools.impl;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import org.netbeans.modules.web.client.javascript.debugger.api.NbJSFileObjectLocation;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSURILocation;
import org.netbeans.modules.web.client.tools.api.JSLocation;
import org.netbeans.modules.web.client.tools.api.JSToNbJSLocationMapper;
import org.netbeans.modules.web.client.tools.api.LocationMappersFactory;
import org.netbeans.modules.web.client.tools.api.NbJSLocation;
import org.netbeans.modules.web.client.tools.api.NbJSToJSLocationMapper;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Quy Nguyen <quynguyen@netbeans.org>
 */
public final class IdentityLocationMappersFactory implements LocationMappersFactory {
    static final String[] SUPPORTED_MIME_TYPES = {
        "text/html",       // NOI18N
        "text/javascript"  // NOI18N
    };
    
    public NbJSToJSLocationMapper getNbJSToJSLocationMapper(FileObject documentBase, URI applicationContext, Map<String, Object> extendedInfo) {
        return new NbJSToJSLocationMapperImpl(documentBase, applicationContext, extendedInfo);
    }

    public JSToNbJSLocationMapper getJSToNbJSLocationMapper(FileObject documentBase, URI applicationContext, Map<String, Object> extendedInfo) {
        return new JSToNbJSLocationMapperImpl(documentBase, applicationContext, extendedInfo);
    }
    
    private static boolean hasSupportedMIMEType(FileObject fo) {
        if (fo == null) {
            return false;
        }
        String mime = fo.getMIMEType();
        for (String supportedType : SUPPORTED_MIME_TYPES) {
            if (supportedType.equals(mime)) {
                return true;
            }
        }

        return false;
    }
    
    private static final class JSToNbJSLocationMapperImpl implements JSToNbJSLocationMapper {
        private final String serverPrefix;
        private final FileObject documentBase;
        private final FileObject welcomeFile;
        
        public JSToNbJSLocationMapperImpl(FileObject documentBase, URI applicationContext, Map<String,Object> extendedInfo) {
            String prefix = applicationContext.toString();
            
            if (prefix.endsWith("/")) { // NOI18N
                prefix = prefix.substring(0, prefix.length() - 1);
            }

            this.serverPrefix = prefix;
            this.documentBase = documentBase;

            String welcomePath = null;
            if (extendedInfo != null) {
                Object r = extendedInfo.get("welcome-file"); // NOI18N
                if (r instanceof String) {
                    welcomePath = (String)r;
                }
            }
            
            if (welcomePath != null) {
                this.welcomeFile = this.documentBase.getFileObject(welcomePath);
            } else {
                this.welcomeFile = null;
            }
        }
        
        public NbJSLocation getNbJSLocation(JSLocation jsLocation, Lookup lookup) {
            if (jsLocation instanceof JSURILocation) {
                JSURILocation jsURILocation = (JSURILocation) jsLocation;
                FileObject fo = uriToFO(jsURILocation.getURI());

                if (fo == null || !hasSupportedMIMEType(fo)) {
                    return null;
                }

                return new NbJSFileObjectLocation(fo, jsURILocation.getLineNumber(), jsURILocation.getColumnNumber());
            }
            return null;
        }

        FileObject uriToFO(URI hostUri) {
            String urlPath = externalFormWithoutQuery(hostUri);

            if (urlPath != null && urlPath.startsWith(serverPrefix)) {
                String relativePath = urlPath.substring(serverPrefix.length());

                // do welcome-file substitution for URLs referencing the application root
                if (welcomeFile != null && (relativePath.length() == 0 ||
                        (relativePath.length() == 1 && relativePath.charAt(0) == '/'))) {

                    return welcomeFile;
                }

                return documentBase.getFileObject(relativePath);
            } else {
                return null;
            }
        }
    
        private String externalFormWithoutQuery(URI uri) {
            try {
                URL u = uri.toURL();

                if (u == null) {
                    return "";
                // compute length of StringBuffer
                }
                int len = u.getProtocol().length() + 1;
                if (u.getAuthority() != null && u.getAuthority().length() > 0) {
                    len += 2 + u.getAuthority().length();
                }
                if (u.getPath() != null) {
                    len += u.getPath().length();
                }
                if (u.getQuery() != null) {
                    len += 1 + u.getQuery().length();
                }
                if (u.getRef() != null) {
                    len += 1 + u.getRef().length();
                }
                StringBuffer result = new StringBuffer(len);
                result.append(u.getProtocol());
                result.append(":");
                if (u.getAuthority() != null && u.getAuthority().length() > 0) {
                    result.append("//");
                    result.append(u.getAuthority());
                }
                if (u.getPath() != null) {
                    result.append(u.getPath());
                }

                return result.toString();
            } catch (MalformedURLException ex) {
                return null;
            }
        }        
    }
    
    private static final class NbJSToJSLocationMapperImpl implements NbJSToJSLocationMapper {
        private final String serverPrefix;
        private final FileObject documentBase;
        
        public NbJSToJSLocationMapperImpl(FileObject documentBase, URI applicationContext, Map<String,Object> extendedInfo) {
            String prefix = applicationContext.toString();
            
            if (prefix.endsWith("/")) { // NOI18N
                prefix = prefix.substring(0, prefix.length() - 1);
            }

            this.serverPrefix = prefix;
            this.documentBase = documentBase;
        }
        
        public JSLocation getJSLocation(NbJSLocation nbJSLocation, Lookup lookup) {
            if (nbJSLocation instanceof NbJSFileObjectLocation) {
                NbJSFileObjectLocation nbJSFileObjectLocation = (NbJSFileObjectLocation) nbJSLocation;
                FileObject fo = nbJSFileObjectLocation.getFileObject();
                if (!hasSupportedMIMEType(fo)) {
                    return null;
                }

                URI uri = fileObjectToUri(fo);
                if (uri != null) {
                    return new JSURILocation(uri, nbJSFileObjectLocation.getLineNumber(), nbJSFileObjectLocation.getColumnNumber());
                } else {
                    return null;
                }
            }
            return null;
        }
        
        URI fileObjectToUri(FileObject fo) {
            String basePath = documentBase.getPath();
            String filePath = fo.getPath();

            if (filePath.startsWith(basePath)) {
                String relativePath = filePath.substring(basePath.length());
                String urlPath;
                if (relativePath.length() > 0 && relativePath.charAt(0) == '/') {
                    urlPath = serverPrefix + relativePath;
                } else {
                    urlPath = serverPrefix + "/" + relativePath; // NOI18N

                }

                try {
                    return new URI(urlPath);
                } catch (URISyntaxException ex) {
                    return null;
                }
            }

            return null;
        }
        
    }
}
