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

package org.netbeans.modules.j2ee.dd.api.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashMap;
import org.netbeans.modules.j2ee.dd.impl.web.WebAppProxy;
import org.netbeans.modules.j2ee.dd.impl.web.WebParseUtils;
import org.netbeans.modules.j2ee.dd.impl.common.DDUtils;
import org.openide.filesystems.*;
import org.xml.sax.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 * Provides access to Deployment Descriptor root ({@link org.netbeans.modules.j2ee.dd.api.web.WebApp} object)
 *
 * @author  Milan Kuchtiak
 */

public final class DDProvider {
    private static DDProvider ddProvider;
    private Map ddMap;
    private Map baseBeanMap;
    private Map errorMap;
    private FCA fileChangeListener;
    private Map musMap;

    private static final Logger LOG = Logger.getLogger(DDProvider.class.getName());
    
    /** Creates a new instance of WebModule */
    private DDProvider() {
        ddMap=new java.util.HashMap(5);
        baseBeanMap=new java.util.HashMap(5);
        errorMap=new java.util.HashMap(5);
        musMap=new HashMap(5);
        fileChangeListener = new FCA();
    }
    
    /**
     * Accessor method for DDProvider singleton
     * @return DDProvider object
     */
    public static synchronized DDProvider getDefault() {
        if (ddProvider==null) ddProvider = new DDProvider();
        return ddProvider;
    }
    
    public WebApp getDDRoot(FileObject fo) throws java.io.IOException {
        WebAppProxy webApp = null;
        
        synchronized (ddMap) {
            webApp = getFromCache(fo);
            if (webApp!=null) {
                return webApp;
            }
        }
        
        fo.addFileChangeListener(fileChangeListener);
        
        String version = null;
        SAXParseException error = null;
        try {
            WebApp original = null;
            synchronized (baseBeanMap) {
                original = getOriginalFromCache(fo);
                if (original == null) {
                    version = WebParseUtils.getVersion(fo.getInputStream());
                    // preparsing
                    error = parse(fo);
                    original = DDUtils.createWebApp(fo.getInputStream(), version);
                    baseBeanMap.put(fo.getURL(), new WeakReference(original));
                    errorMap.put(fo.getURL(), error);
                } else {
                    version = original.getVersion();
                    error = (SAXParseException) errorMap.get(fo.getURL());
                }
            }
            webApp = new WebAppProxy(original, version);
            if (error != null) {
                webApp.setStatus(WebApp.STATE_INVALID_PARSABLE);
                webApp.setError(error);
            }
        } catch (SAXException ex) {
            webApp = new WebAppProxy(null, version);
            webApp.setStatus(WebApp.STATE_INVALID_UNPARSABLE);
            if (ex instanceof SAXParseException) {
                webApp.setError((SAXParseException) ex);
            } else if (ex.getException() instanceof SAXParseException) {
                webApp.setError((SAXParseException) ex.getException());
            }
        } catch (FileNotFoundException fnfe) {
            // see #116571. not pretty, but at least allows users to open a web.xml similar 
            // to the one in the aforementioned issue without NPEs. handling of the declared IOE in the
            // clients of this method is a can of worms that i do not want to open right now. moreover,
            // the situation when the FNFE is thrown is similar to when a SAXException is thrown.
            LOG.log(Level.INFO,
                    "A file referenced from [" +
                    FileUtil.getFileDisplayName(fo) + "] could not be found",
                    fnfe); // NO18N
            webApp = new WebAppProxy(null, version);
            webApp.setStatus(WebApp.STATE_INVALID_UNPARSABLE);
            webApp.setError(new SAXParseException(fnfe.getMessage(), null, fnfe));
        }
        synchronized(ddMap){
            WebApp cached = getFromCache(fo);
            if (cached != null) {
                return cached;
            }
            ddMap.put(fo.getURL(), new WeakReference(webApp));
        }
        return webApp;
    }
    
    /**
     * Returns the root of deployment descriptor bean graph for given file object.
     * The method is useful for clients planning to modify the deployment descriptor.
     * Finally the {@link org.netbeans.modules.j2ee.dd.api.web.WebApp#write(org.openide.filesystems.FileObject)} should be used
     * for writing the changes.
     * @param fo FileObject representing the web.xml file
     * @return WebApp object - root of the deployment descriptor bean graph
     */
    public WebApp getDDRootCopy(FileObject fo) throws java.io.IOException {
        return (WebApp)getDDRoot(fo).clone();
    }
    
    private WebAppProxy getFromCache(FileObject fo) throws java.io.IOException {
        if (fo == null) {
            return null;
        }
        WeakReference wr = (WeakReference) ddMap.get(fo.getURL());
        if (wr == null) {
            return null;
        }
        WebAppProxy webApp = (WebAppProxy) wr.get();
        if (webApp == null) {
            ddMap.remove(fo.getURL());
        }
        return webApp;
    }
    
    private WebApp getOriginalFromCache(FileObject fo) throws java.io.IOException {
        WeakReference wr = (WeakReference) baseBeanMap.get(fo.getURL());
        if (wr == null) {
            return null;
        }
        WebApp webApp = (WebApp) wr.get();
        if (webApp == null) {
            baseBeanMap.remove(fo.getURL());
            errorMap.remove(fo.getURL());
        }
        return webApp;
    }
    
    /**
     * Returns the root of deployment descriptor bean graph for java.io.File object.
     *
     * @param f File representing the web.xml file
     * @return WebApp object - root of the deployment descriptor bean graph
     */
    public WebApp getDDRoot(File f) throws IOException, SAXException {
        return DDUtils.createWebApp(new FileInputStream(f), WebParseUtils.getVersion(new FileInputStream(f)));
    }
    
    /**  Convenient method for getting the BaseBean object from CommonDDBean object.
     * The j2eeserver module needs BaseBean to implement jsr88 API.
     * This is a temporary workaround until the implementation of jsr88 moves into ddapi
     * or the implementation in j2eeserver gets changed.
     * @deprecated do not use - temporary workaround that exposes the schema2beans implementation
     */
    public org.netbeans.modules.schema2beans.BaseBean getBaseBean(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean bean) {
        if (bean instanceof org.netbeans.modules.schema2beans.BaseBean) return (org.netbeans.modules.schema2beans.BaseBean)bean;
        else if (bean instanceof WebAppProxy) return (org.netbeans.modules.schema2beans.BaseBean) ((WebAppProxy)bean).getOriginal();
        return null;
    }
    
    public SAXParseException parse(FileObject fo)
    throws org.xml.sax.SAXException, java.io.IOException {
        return WebParseUtils.parse(fo);
    }
  
    
    /**
     * Removes the entries associated with the given <code>fo</code> from
     * the various caches that this class utilizes.
     * @param fo
     */
    private void removeFromCache(FileObject fo){
        try{
            URL foUrl = fo.getURL();
            synchronized (ddMap){
                ddMap.remove(foUrl);
            }
            baseBeanMap.remove(foUrl);
            errorMap.remove(foUrl);
            musMap.remove(fo);
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private class FCA extends FileChangeAdapter {
        public void fileChanged(FileEvent evt) {
            FileObject fo=evt.getFile();
            try {
                if (DataObject.find(fo) != null) {
                    return;
                }
            } catch (DataObjectNotFoundException e) {
            }
            try {
                synchronized (ddMap) {
                    synchronized (baseBeanMap) {
                        WebAppProxy webApp = getFromCache(fo);
                        WebApp orig = getOriginalFromCache(fo);
                        if (webApp!=null) {
                            String version = null;
                            try {
                                version = WebParseUtils.getVersion(fo.getInputStream());
                                // preparsing
                                SAXParseException error = parse(fo);
                                if (error!=null) {
                                    webApp.setError(error);
                                    webApp.setStatus(WebApp.STATE_INVALID_PARSABLE);
                                } else {
                                    webApp.setError(null);
                                    webApp.setStatus(WebApp.STATE_VALID);
                                }
                                WebApp original = DDUtils.createWebApp(fo.getInputStream(), version);
                                baseBeanMap.put(fo.getURL(), new WeakReference(original));
                                errorMap.put(fo.getURL(), webApp.getError());
                                webApp.merge(original, WebApp.MERGE_UPDATE);
                            } catch (SAXException ex) {
                                if (ex instanceof SAXParseException) {
                                    webApp.setError((SAXParseException)ex);
                                } else if ( ex.getException() instanceof SAXParseException) {
                                    webApp.setError((SAXParseException)ex.getException());
                                }
                                webApp.setStatus(WebApp.STATE_INVALID_UNPARSABLE);
                                webApp.setOriginal(null);
                                webApp.setProxyVersion(version);
                            }
                        } else if (orig != null) {
                            String version = null;
                            try {
                                version = WebParseUtils.getVersion(fo.getInputStream());
                                WebApp original = DDUtils.createWebApp(fo.getInputStream(), version);
                                if (original.getClass().equals(orig.getClass())) {
                                    orig.merge(original,WebApp.MERGE_UPDATE);
                                } else {
                                    baseBeanMap.put(fo.getURL(), new WeakReference(original));
                                }
                            } catch (SAXException ex) {
                                baseBeanMap.remove(fo.getURL());
                            }
                        }
                    }
                }
            } catch (java.io.IOException ex){}
        }
        
        public void fileDeleted(FileEvent fe) {
            // need to remove cache entries, see #76431.
            removeFromCache(fe.getFile());
        }
        
        
    }
    
}
