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

package org.netbeans.modules.web.jspparser;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;

/**
 * Simple <code>ServletContext</code> implementation without
 * HTTP-specific methods.
 *
 * @author Peter Rossbach (pr@webapp.de)
 */

public class ParserServletContext implements ServletContext {
    
    public static final String JSP_TAGLIBRARY_CACHE = "com.sun.jsp.taglibraryCache";
    public static final String JSP_TAGFILE_JAR_URLS_CACHE = "com.sun.jsp.tagFileJarUrlsCache";
    
    private static final Logger LOGGER = Logger.getLogger(ParserServletContext.class.getName());
    
    // ----------------------------------------------------- Instance Variables
    
    
    /**
     * Servlet context attributes.
     */
    protected Hashtable<String, Object> myAttributes;
    
    
    /**
     * The base FileObject (document root) for this context.
     */
    protected FileObject wmRoot;
    
    
    protected JspParserAPI.WebModule myWm;
    
    /** If true, takes the data from the editor; otherwise
     * from the disk.
     */
    protected boolean useEditorVersion;
    
    
    // ----------------------------------------------------------- Constructors
    
    
    /**
     * Create a new instance of this ServletContext implementation.
     *
     * @param wmRoot Resource base FileObject
     * @param wm JspParserAPI.WebModule in which we are parsing the file - this is used to
     *    find the editor for objects which are open in the editor
     */
    public ParserServletContext(FileObject wmRoot, JspParserAPI.WebModule wm, boolean useEditor) {
        LOGGER.log(Level.FINE, "ParserServletContext created");
        myAttributes = new Hashtable<String, Object>();
        this.wmRoot = wmRoot;
        this.myWm = wm;
        this.useEditorVersion = useEditor;
        
        setAttribute(JSP_TAGLIBRARY_CACHE, new ConcurrentHashMap<String, TagLibraryInfo>());
        setAttribute(JSP_TAGFILE_JAR_URLS_CACHE, new ConcurrentHashMap<String, URL>());
    }
    
    
    // --------------------------------------------------------- Public Methods
    
    
    /**
     * Return the specified context attribute, if any.
     *
     * @param name Name of the requested attribute
     */
    public Object getAttribute(String name) {
        LOGGER.log(Level.FINE, "getAttribute({0}) = {1}", new Object[]{name, myAttributes.get(name)});
        return myAttributes.get(name);
    }
    
    
    /**
     * Return an enumeration of context attribute names.
     */
    public Enumeration<String> getAttributeNames() {
        
        return myAttributes.keys();
        
    }
    
    
    /**
     * Return the servlet context for the specified path.
     *
     * @param uripath Server-relative path starting with '/'
     */
    public ServletContext getContext(String uripath) {
        
        return (null);
        
    }
    
    
    /**
     * Return the specified context initialization parameter.
     *
     * @param name Name of the requested parameter
     */
    public String getInitParameter(String name) {
        
        return (null);
        
    }
    
    
    /**
     * Return an enumeration of the names of context initialization
     * parameters.
     */
    public Enumeration getInitParameterNames() {
        
        return (new Vector().elements());
        
    }
    
    
    /**
     * Return the Servlet API major version number.
     */
    public int getMajorVersion() {
        
        return (2);
        
    }
    
    
    /**
     * Return the MIME type for the specified filename.
     *
     * @param file Filename whose MIME type is requested
     */
    public String getMimeType(String file) {
        
        return (null);
        
    }
    
    
    /**
     * Return the Servlet API minor version number.
     */
    public int getMinorVersion() {
        
        return (3);
        
    }
    
    
    /**
     * Return a request dispatcher for the specified servlet name.
     *
     * @param name Name of the requested servlet
     */
    public RequestDispatcher getNamedDispatcher(String name) {
        
        return (null);
        
    }
    
    /** Returns a FileObject representation of the specified context-relative
     * virtual path.
     */
    protected FileObject getResourceAsObject(String path) {
        LOGGER.log(Level.FINE,  "getResourceAsObject({0})", path);
        FileObject fileObject = ContextUtil.findRelativeFileObject(wmRoot, path);
        if (fileObject == null && path != null) {
            int index = path.toLowerCase().indexOf("web-inf");
            if (index > -1) {
                String newPath = path.substring(index + 7);
                fileObject = ContextUtil.findRelativeFileObject(myWm.getWebInf(), newPath);
            }
            else {
                fileObject = ContextUtil.findRelativeFileObject(myWm.getWebInf(), path);
            }
        }
        return fileObject;
    }
    
    
    /**
     * Return the real path for the specified context-relative
     * virtual path.
     *
     * @param path The context-relative virtual path to resolve
     */
    public String getRealPath(String path) {
        LOGGER.log(Level.FINE,  "getRealPath({0})", path);
        if (!path.startsWith("/")) {
            return (null);
        }
        FileObject fo = getResourceAsObject(path);
        if (fo != null) {
            File ff = FileUtil.toFile(fo);
            if (ff != null) {
                return ff.getAbsolutePath();
            }
        }
        
        return null;
    }
    
    
    /**
     * Return a request dispatcher for the specified context-relative path.
     *
     * @param path Context-relative path for which to acquire a dispatcher
     */
    public RequestDispatcher getRequestDispatcher(String path) {
        
        return (null);
        
    }
    
    
    /**
     * Return a URL object of a resource that is mapped to the
     * specified context-relative path.
     *
     * @param path Context-relative path of the desired resource
     *
     * @exception MalformedURLException if the resource path is
     *  not properly formed
     */
    public URL getResource(String path) throws MalformedURLException {
        
        LOGGER.log(Level.FINE,  "getResource({0})", path);
        if (!path.startsWith("/"))
            throw new MalformedURLException(NbBundle.getMessage(ParserServletContext.class,
                    "EXC_PathMustStartWithSlash", path));
        
        FileObject fo = getResourceAsObject(path);
        if (fo == null) {
            return null;
        }
        return URLMapper.findURL(fo, URLMapper.EXTERNAL);
        
    }
    
    
    /**
     * Return an InputStream allowing access to the resource at the
     * specified context-relative path.
     *
     * @param path Context-relative path of the desired resource
     */
    public InputStream getResourceAsStream(String path) {
        LOGGER.log(Level.FINE,  "getResourceAsStream({0})", path);
        // first try from the opened editor - if fails read from file
        if (myWm != null) {
            FileObject fo = getResourceAsObject(path);
            if ((fo != null) && (useEditorVersion)) {
                // reading from the editor
                InputStream result = myWm.getEditorInputStream(fo);
                if (result != null) {
                    return result;
                }
            }
        }
        
        // read from the file by default
        try {
            URL url = getResource(path);
            if (url == null) {
                return null;
            } else {
                return url.openStream();
            }
        } catch (Throwable t) {
            Logger.getLogger("global").log(Level.INFO, null, t);
            return (null);
        }
        
    }
    
    
    /**
     * Return the set of resource paths for the "directory" at the
     * specified context path.
     *
     * @param path Context-relative base path
     */
    public Set<String> getResourcePaths(String path) {
        
        LOGGER.log(Level.FINE,  "getResourcePaths({0})", path);
        Set<String> thePaths = new HashSet<String>();
        if (!path.endsWith("/"))
            path += "/";
        String basePath = getRealPath(path);
        if (basePath == null)
            return (thePaths);
        File theBaseDir = new File(basePath);
        if (!theBaseDir.exists() || !theBaseDir.isDirectory())
            return (thePaths);
        String theFiles[] = theBaseDir.list();
        for (int i = 0; i < theFiles.length; i++) {
            File testFile = new File(basePath + File.separator + theFiles[i]);
            if (testFile.isFile())
                thePaths.add(path + theFiles[i]);
            else if (testFile.isDirectory())
                thePaths.add(path + theFiles[i] + "/");
        }
        return thePaths;
        
    }
    
    
    /**
     * Return descriptive information about this server.
     */
    public String getServerInfo() {
        
        return ("NB.ParserServletContext/1.0");
        
    }
    
    
    /**
     * Return a null reference for the specified servlet name.
     *
     * @param name Name of the requested servlet
     *
     * @deprecated This method has been deprecated with no replacement
     */
    public Servlet getServlet(String name) throws ServletException {
        
        return (null);
        
    }
    
    
    /**
     * Return the name of this servlet context.
     */
    public String getServletContextName() {
        
        return (getServerInfo());
        
    }
    
    
    /**
     * Return an empty enumeration of servlet names.
     *
     * @deprecated This method has been deprecated with no replacement
     */
    public Enumeration getServletNames() {
        
        return (new Vector().elements());
        
    }
    
    
    /**
     * Return an empty enumeration of servlets.
     *
     * @deprecated This method has been deprecated with no replacement
     */
    public Enumeration getServlets() {
        
        return (new Vector().elements());
        
    }
    
    
    /**
     * Log the specified message.
     *
     * @param message The message to be logged
     */
    public void log(String message) {
        Logger.getLogger("global").log(Level.INFO, message);
    }
    
    
    /**
     * Log the specified message and exception.
     *
     * @param exception The exception to be logged
     * @param message The message to be logged
     *
     * @deprecated Use log(String,Throwable) instead
     */
    public void log(Exception exception, String message) {
        
        log(message, exception);
        
    }
    
    
    /**
     * Log the specified message and exception.
     *
     * @param message The message to be logged
     * @param exception The exception to be logged
     */
    public void log(String message, Throwable exception) {
        Logger.getLogger("global").log(Level.INFO, message);
        Logger.getLogger("global").log(Level.INFO, null, exception);
    }
    
    
    /**
     * Remove the specified context attribute.
     *
     * @param name Name of the attribute to remove
     */
    public void removeAttribute(String name) {
        myAttributes.remove(name);
        
    }
    
    
    /**
     * Set or replace the specified context attribute.
     *
     * @param name Name of the context attribute to set
     * @param value Corresponding attribute value
     */
    public void setAttribute(String name, Object value) {
        myAttributes.put(name, value);
        
    }
    
    
    public String getContextPath(){
        return "";
    }
    
}
