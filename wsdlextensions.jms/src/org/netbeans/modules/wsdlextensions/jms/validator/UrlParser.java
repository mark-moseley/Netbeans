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

package org.netbeans.modules.wsdlextensions.jms.validator;

import java.util.Properties;
import java.util.StringTokenizer;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

/**
 * URLParser
 * A generic parser for the following pattern:
 * protocol://host[:port][/path]
 * path :== file?query | ?query | file
 *
 * Examples:
 * p://h
 * p://h:p
 * p://h/file?query
 * p://h?query
 *
 */
public class UrlParser extends ConnectionUrl {
    private String mUrl;
    private boolean mParsed;
    private String mProtocol;
    private int mPort;
    private String mHost;
    private String mPath;
    private String mFile;
    private String mQuery;

    /**
     * Constructor
     *
     * @param url String
     */
    public UrlParser(String url) {
        mUrl = url;
    }

    private void parse() throws ValidationException {
        if (mParsed) {
            return;
        }

        String r = mUrl;
        mParsed = true;

        // Protocol
        int i = r.indexOf("://");
        if (i < 0) {
            throw new ValidationException("Invalid URL [" + mUrl + "]: no protocol specified");
        }
        mProtocol = r.substring(0, i);
        r = r.substring(i + "://".length());

        // host[:port]
        i = r.indexOf('/');
        if (i < 0) {
            i = r.indexOf('?');
        }
        String server = i >= 0 ? r.substring(0, i) : r;
        r = i >= 0 ? r.substring(i) : "";

        i = server.indexOf(':');
        if (i >= 0) {
            mHost = server.substring(0, i);
            String port = server.substring(i + 1);
            if (port.length() > 0) {
                mPort = Integer.parseInt(port);
            } else {
                mPort = -1;
            }
        } else {
            mHost = server;
            mPort = -1;
        }

        // path
        if (r.length() > 0) {
            mFile = r.substring(0);
        } else {
            mFile = "";
        }

        // file
        if (!r.startsWith("/")) {
            mPath = "";
        } else {
            i = r.indexOf('?');
            if (i >= 0) {
                mPath = r.substring(0, i);
                r = r.substring(i);
            } else {
                mPath = r;
                r = "";
            }
        }

        // query
        if (r.startsWith("?")) {
            mQuery = r.substring(1);
        }
    }

    /**
     * Changes the port
     *
     * @param port int
     */
    public void setPort(int port) throws ValidationException {
        parse();
        mPort = port;
        mUrl = null;
    }

    /**
     * Changes the server portion
     * 
     * @param host String 
     */
    public void setHost(String host) throws ValidationException {
       parse();
       mHost = host;
       mUrl = null;
    }

    /**
     * Returns the URL in full string form
     *
     * @return String
     */
    public String toString() {
        if (mUrl == null) {
            StringBuffer url = new StringBuffer();
            url.append(mProtocol).append("://").append(mHost);
            if (mPort != -1) {
                url.append(":").append(mPort);
            }
            url.append(mFile);
            mUrl = url.toString();
        }
        return mUrl;
    }

    /**
     * Gets the protocol name of this <code>URL</code>.
     *
     * @return  the protocol of this <code>URL</code>.
     */
    public String getProtocol() throws ValidationException {
        parse();
        return mProtocol;
    }

    /**
     * Gets the host name of this <code>URL</code>
     *
     * @return  the host name of this <code>URL</code>.
     */
    public String getHost() throws ValidationException {
        parse();
        return mHost;
    }

    /**
     * Gets the port number of this <code>URL</code>.
     *
     * @return  the port number, or -1 if the port is not set
     */
    public int getPort() throws ValidationException {
        parse();
        return mPort;
    }

    /**
     * Gets the file name of this <code>URL</code>.
     * The returned file portion will be
     * the same as <CODE>getPath()</CODE>, plus the concatenation of
     * the value of <CODE>getQuery()</CODE>, if any. If there is
     * no query portion, this method and <CODE>getPath()</CODE> will
     * return identical results.
     *
     * @return  the file name of this <code>URL</code>,
     * or an empty string if one does not exist
     */
    public String getFile() throws ValidationException {
        parse();
        return mFile;
    }

    /**
     * Gets the path part of this <code>URL</code>.
     *
     * @return  the path part of this <code>URL</code>, or an
     * empty string if one does not exist
     */
    public String getPath() throws ValidationException {
        parse();
        return mPath;
    }

    /**
     * Gets the query part of this <code>URL</code>.
     *
     * @return  the query part of this <code>URL</code>,
     * or <CODE>null</CODE> if one does not exist
     */
    public String getQuery() throws ValidationException {
        parse();
        return mQuery;
    }

    /**
     * Extracts the key value pairs from the query string
     *
     * @param toAddTo Properties key-value pairs will be added to this properties set
     */
    public void getQueryProperties(Properties toAddTo) throws ValidationException {
        String q = getQuery();
        getQueryProperties(q, toAddTo);
    }

    /**
     * Tool function: queries a query string and adds the key/value pairs to the specified
     * properties map
     *
     * @param q String
     * @param toAddTo Properties
     */
    public static void getQueryProperties(String q, Properties toAddTo) throws ValidationException {
        if (q == null || q.length() == 0) {
            return;
        }
        for (StringTokenizer iter = new StringTokenizer(q, "&");
            iter.hasMoreElements();/*-*/) {
            String pair = (String) iter.nextToken();
            int split = pair.indexOf('=');
            if (split <= 0) {
                throw new ValidationException("Invalid pair [" + pair
                    + "] in query string [" + q + "]");
            } else {
                String key = pair.substring(0, split);
                String value = pair.substring(split + 1);
                try {
                    key = URLDecoder.decode(key, "UTF-8");
                    value = URLDecoder.decode(value, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new ValidationException("Invalid encoding in [" + pair
                        + "] in query string [" + q + "]",
                        e);
                }
                toAddTo.setProperty(key, value);
            }
        }
    }
}
