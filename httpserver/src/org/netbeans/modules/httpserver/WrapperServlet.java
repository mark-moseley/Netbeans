/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.httpserver;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.openide.execution.NbfsURLConnection;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;

import javax.servlet.ServletOutputStream;

/**
 *
 * @author Radim Kubacki
 */
public class WrapperServlet extends NbBaseServlet {

    private static final long serialVersionUID = 8009602136746998361L;
    
    /** Creates new WrapperServlet */
    public WrapperServlet () {
    }

    /**
     * Transforms URL to another URL that uses HTTP or FTP protocol 
     * and can be used from outside of IDE
     *
     * @param url original URL
     * @return translated URL or null if processing failed
     */ 
    public static URL createHttpURL (URL url) {
        if (url == null)
            return null;
        
        if ("http".equals (url.getProtocol ())   // NOI18N
        ||  "ftp".equals (url.getProtocol ()))   // NOI18N
            return url;
        
        try {
            URL newURL;
            
            if (NbfsURLConnection.PROTOCOL.equals (url.getProtocol ())) {
                FileObject fo = NbfsURLConnection.decodeURL (url);
                if (fo != null) {
                    URL fsurl = URLMapper.findURL (fo, URLMapper.NETWORK);
                    if (fsurl != null && "jar".equals (fsurl.getProtocol ()))   // NOI18N
                        fsurl = null;

                    if (fsurl == null) 
                        fsurl = URLMapper.findURL (fo, URLMapper.EXTERNAL);
                    if (fsurl != null && "jar".equals (fsurl.getProtocol ()))   // NOI18N
                        fsurl = null;

                    if (fsurl != null)
                        return fsurl;
                }
            }

            String orig = url.toString ();
            int slash = orig.indexOf ('/');
            if (slash >= 0 && orig.charAt (slash+1) == '/') {
                slash = orig.indexOf (slash+2);
            }
            String path;
            if (slash >=0)
                path = java.net.URLEncoder.encode (orig.substring (0, slash))+orig.substring (slash);
            else
                path = orig;

            HttpServerSettings settings = (HttpServerSettings)SharedClassObject.findObject(HttpServerSettings.class, true);
            settings.setRunning (true);
            newURL = new URL ("http",   // NOI18N
                              InetAddress.getLocalHost ().getHostName (), 
                              settings.getPort (),
                              settings.getWrapperBaseURL () + path);
            return newURL;
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    /** Processes the request for both HTTP GET and POST methods
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest (HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, java.io.IOException {
        if (!checkAccess(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                               NbBundle.getBundle(NbBaseServlet.class).getString("MSG_HTTP_FORBIDDEN"));
            return;
        }
        
        // output your page here
        String path = request.getPathInfo ();
        ServletOutputStream out = response.getOutputStream ();
        try {
            
            // resource name
            if (path.startsWith ("/")) path = path.substring (1); // NOI18N
            
            String internalUrl;
            int slash = path.indexOf ('/');
            if (slash >= 0)
                internalUrl = java.net.URLDecoder.decode (path.substring (0,slash))+path.substring (slash);
            else
                internalUrl = java.net.URLDecoder.decode (path);
            URL innerURL = new URL (internalUrl);
            URLConnection conn = innerURL.openConnection ();
            
            response.setContentType(conn.getContentType ());   // NOI18N
            // PENDING: copy all info - headers, length, encoding, ...
            
            InputStream in = conn.getInputStream ();
            byte [] buff = new byte [256];
            int len;

            while ((len = in.read (buff)) != -1) {
                out.write (buff, 0, len);
                out.flush();
            }
            in.close ();

        }
        catch (java.net.MalformedURLException ex) {
            try {
                response.sendError (HttpServletResponse.SC_NOT_FOUND,
                                   NbBundle.getBundle(NbBaseServlet.class).getString("MSG_HTTP_NOT_FOUND"));
            }
            catch (java.io.IOException ex2) {}
        }
        catch (java.io.IOException ex) {
            try {
                response.sendError (HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            catch (java.io.IOException ex2) {}
        }
        finally {
            try { out.close(); } catch (Exception ex) {}
        }
    }



    /**
    * Returns a short description of the servlet.
    */
    public String getServletInfo() {
        return NbBundle.getBundle(ClasspathServlet.class).getString("MSG_WrapperServletDescr");
    }

}
