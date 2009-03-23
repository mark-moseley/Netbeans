/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hudson.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.hudson.spi.ConnectionAuthenticator;
import org.openide.util.Lookup;

/**
 * Creates an HTTP connection to Hudson.
 * Handles redirects and authentication.
 */
public final class ConnectionBuilder {

    private static final Logger LOG = Logger.getLogger(ConnectionBuilder.class.getName());

    private URL home;
    private URL url;
    private final Map<String,String> requestHeaders = new LinkedHashMap<String,String>();
    private byte[] postData;
    private Map<String,List<String>> responseHeaders;

    /**
     * Prepare a connection.
     * You must also specify a location, and if possible an associated instance or job.
     */
    public ConnectionBuilder() {}

    /**
     * Specify the location to connect to.
     * @param url location to open
     * @return this builder
     */
    public ConnectionBuilder url(URL url) {
        this.url = url;
        return this;
    }

    /**
     * Specify the location to connect to.
     * @param url location to open
     * @return this builder
     */
    public ConnectionBuilder url(String url) throws MalformedURLException {
        return url(new URL(url));
    }

    /**
     * Specify the home URL.
     * Useful for login authentication.
     * @param url the base URL of the Hudson instance
     * @return this builder
     */
    public ConnectionBuilder homeURL(URL url) {
        this.home = url;
        return this;
    }

    /**
     * Specify the Hudson instance as per {@link #homeURL}.
     * @param instance a Hudson instance
     * @return this builder
     */
    public ConnectionBuilder instance(HudsonInstance instance) {
        try {
            this.home = new URL(instance.getUrl());
        } catch (MalformedURLException x) {
            LOG.warning(x.toString());
        }
        return this;
    }

    /**
     * Specify the job, and hence the Hudson instance as per {@link #homeURL}.
     * @param job an arbitrary job in an instance
     * @return this builder
     */
    public ConnectionBuilder job(HudsonJob job) {
        HudsonInstance instance = job.getInstance();
        if (instance != null) {
            instance(instance);
        }
        return this;
    }

    /**
     * Define an HTTP request header.
     * @param key header key
     * @param value header value
     * @return this builder
     */
    public ConnectionBuilder header(String key, String value) {
        requestHeaders.put(key, value);
        return this;
    }

    /**
     * Post data to the connection.
     * @param data bytes to post
     * @return this builder
     */
    public ConnectionBuilder postData(byte[] data) {
        postData = data;
        return this;
    }

    /**
     * Collect headers from the response.
     * @param responseHeaders a map which will be populated on the <em>initial</em> connection
     * @return this builder
     */
    public ConnectionBuilder collectResponseHeaders(Map<String,List<String>> responseHeaders) {
        this.responseHeaders = responseHeaders;
        return this;
    }

    /**
     * Actually try to open the connection.
     * May need to retry to handle redirects and/or authentication.
     * @return an open and valid connection, ready for {@link URLConnection#getInputStream},
     *         {@link URLConnection#getHeaderField(String)}, etc.
     * @throws IOException for various reasons, including non-200 response code
     */
    public URLConnection connection() throws IOException {
        if (url == null) {
            throw new IllegalArgumentException("You must call the url method!");
        }
        URLConnection conn = url.openConnection();
        RETRY: while (true) {
            if (conn instanceof HttpURLConnection) {
                ((HttpURLConnection) conn).setInstanceFollowRedirects(false);
            }
            LOG.log(Level.FINER, "Trying to open {0}", conn.getURL());
            if (home != null) {
                for (ConnectionAuthenticator auth : Lookup.getDefault().lookupAll(ConnectionAuthenticator.class)) {
                    auth.prepareRequest(conn, home);
                }
            }
            if (postData != null) {
                conn.setDoOutput(true);
            }
            for (Map.Entry<String,String> header : requestHeaders.entrySet()) {
                conn.setRequestProperty(header.getKey(), header.getValue());
            }
            conn.connect();
            if (postData != null) {
                OutputStream os = conn.getOutputStream();
                try {
                    os.write(postData);
                } finally {
                    os.close();
                }
            }
            if (!(conn instanceof HttpURLConnection)) {
                break;
            }
            if (responseHeaders != null) {
                responseHeaders.putAll(conn.getHeaderFields());
                LOG.log(Level.FINER, "  => {0}", responseHeaders);
                responseHeaders = null;
            }
            int responseCode = ((HttpURLConnection) conn).getResponseCode();
            LOG.log(Level.FINER, "  => {0}", responseCode);
            switch (responseCode) {
            // Workaround for JDK bug #6810084; HttpURLConnection.setInstanceFollowRedirects does not work.
            case HttpURLConnection.HTTP_MOVED_PERM:
            case HttpURLConnection.HTTP_MOVED_TEMP:
                conn = new URL(conn.getHeaderField("Location")).openConnection();
                continue RETRY;
            case HttpURLConnection.HTTP_FORBIDDEN:
                if (home != null) {
                    for (ConnectionAuthenticator auth : Lookup.getDefault().lookupAll(ConnectionAuthenticator.class)) {
                        URLConnection retry = auth.forbidden(conn, home);
                        if (retry != null) {
                            LOG.log(Level.FINER, "Retrying after auth from {0}", auth);
                            conn = retry;
                            continue RETRY;
                        }
                    }
                }
                throw new IOException("Must log in to access " + url);
            case HttpURLConnection.HTTP_NOT_FOUND:
                throw new FileNotFoundException(conn.getURL().toString());
            case HttpURLConnection.HTTP_OK:
                break RETRY;
            default:
                // XXX are there other legitimate response codes?
                throw new IOException("Server rejected connection to " + conn.getURL() + " with code " + responseCode);
            }
        }
        return conn;
    }

    /**
     * Like {@link #connection} but coerced to an HTTP connection.
     * @throws IOException for the usual reasons, or if a non-HTTP connection resulted
     */
    public HttpURLConnection httpConnection() throws IOException {
        URLConnection c = connection();
        if (c instanceof HttpURLConnection) {
            return (HttpURLConnection) c;
        } else {
            throw new IOException("Not an HTTP connection: " + c);
        }
    }

}
