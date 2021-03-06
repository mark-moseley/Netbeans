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

package org.openide.filesystems;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.StringTokenizer;

/**
 * @author Radek Matous
 */
@SuppressWarnings("deprecation") // needs to use old Repository methods for compat
final class NbfsUtil {
    /** url separator */
    private static final char SEPARATOR = '/';

    /**
     * Gets URL with nbfs protocol for passes fo
     * @param fo
     * @return url with nbfs protocol
     * @throws FileStateInvalidException if FileObject somehow corrupted
     */
    static URL getURL(FileObject fo) throws FileStateInvalidException {
        final String fsPart = encodeFsPart(fo);
        final String foPart = encodeFoPart(fo);

        final String host = "nbhost"; //NOI18N
        final String file = combine(fsPart, foPart);

        // #13038: the URL constructor accepting a handler is a security-sensitive
        // operation. Sometimes a user class loaded internally (customized bean...),
        // which has no privileges, needs to make and use an nbfs: URL, since this
        // may be the URL used by e.g. ClassLoader.getResource for resources.
        try {
            return AccessController.doPrivileged(
                new PrivilegedExceptionAction<URL>() {
                    public URL run() throws Exception {
                        // #30397: the fsPart name cannot be null
                        return new URL(FileURL.PROTOCOL, host, -1, file, FileURL.HANDLER); // NOI18N
                    }
                }
            );
        } catch (PrivilegedActionException pae) {
            // MalformedURLException is declared but should not happen.
            IllegalStateException ise = new IllegalStateException(pae.toString());
            ExternalUtil.annotate(ise, pae);
            throw ise;
        }
    }

    private static String combine(final String host, final String file) {
        StringBuffer sb = new StringBuffer();
        sb.append(SEPARATOR).append(host);
        sb.append(file);

        return sb.toString();
    }

    private static String[] split(URL url) {
        String file = url.getFile();
        int idx = file.indexOf("/", 1);
        String fsPart = "";
        String foPart = file;

        if (idx > 1) {
            fsPart = file.substring(1, idx);
            foPart = file.substring(idx + 1);
        }

        return new String[] { fsPart, foPart };
    }

    /**
     *  Gets FileObject for passed url.
     * @param url
     * @return appropriate FileObject. Can return null for other protocol than nbfs or
     * if such FileObject isn't reachable via Repository.
     */
    static FileObject getFileObject(URL url) {
        if (!url.getProtocol().equals(FileURL.PROTOCOL)) {
            return null;
        }

        if (isOldEncoding(url)) {
            return oldDecode(url);
        }

        String[] urlParts = split(url);

        String fsName = decodeFsPart(urlParts[0]);
        String foName = decodeFoPart(urlParts[1]);

        FileSystem fsys = ExternalUtil.getRepository().findFileSystem(fsName);

        return (fsys == null) ? null : fsys.findResource(foName);
    }

    private static String encodeFsPart(FileObject fo) throws FileStateInvalidException {
        FileSystem fs = fo.getFileSystem();

        return encoder(fs.getSystemName());
    }

    private static String encodeFoPart(FileObject fo) {
        StringTokenizer elemsEnum;
        StringBuffer sBuff = new StringBuffer();
        elemsEnum = new StringTokenizer(fo.getPath(), String.valueOf(SEPARATOR));

        while (elemsEnum.hasMoreElements()) {
            sBuff.append(SEPARATOR);
            sBuff.append(encoder((String) elemsEnum.nextElement()));
        }

        String retVal = sBuff.toString();

        if ((retVal.length() == 0) || (fo.isFolder() && (retVal.charAt(retVal.length() - 1) != SEPARATOR))) {
            retVal += SEPARATOR;
        }

        return retVal;
    }

    private static String decodeFsPart(String encodedStr) {
        return decoder(encodedStr);
    }

    private static String decodeFoPart(String encodedStr) {
        if (encodedStr == null) {
            return ""; //NOI18N
        }

        StringTokenizer elemsEnum;
        StringBuffer sBuff = new StringBuffer();
        elemsEnum = new StringTokenizer(encodedStr, String.valueOf(SEPARATOR));

        while (elemsEnum.hasMoreElements()) {
            sBuff.append(SEPARATOR);
            sBuff.append(decoder((String) elemsEnum.nextElement()));
        }

        return sBuff.toString();
    }

    private static String encoder(String elem) {
        try {
            return URLEncoder.encode(elem, "UTF-8"); // NOI18N
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    private static String decoder(String elem) {
        try {
            return URLDecoder.decode(elem, "UTF-8"); // NOI18N
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    // backward compatibility
    private static boolean isOldEncoding(URL url) {
        String host = url.getHost();

        return (host == null) || (host.length() == 0);
    }

    private static FileObject oldDecode(URL u) {
        String resourceName = u.getFile();

        if (resourceName.startsWith("/")) {
            resourceName = resourceName.substring(1); // NOI18N
        }

        // first part is FS name
        int first = resourceName.indexOf('/'); // NOI18N

        if (first == -1) {
            return null;
        }

        String fileSystemName = oldDecodeFSName(resourceName.substring(0, first));
        resourceName = resourceName.substring(first);

        FileSystem fsys = ExternalUtil.getRepository().findFileSystem(fileSystemName);

        return (fsys == null) ? null : fsys.findResource(resourceName);
    }

    /** Decodes name to FS one.
     * @param name encoded name
     * @return original name of the filesystem
     */
    private static String oldDecodeFSName(String name) {
        StringBuffer sb = new StringBuffer();
        int i = 0;
        int len = name.length();

        while (i < len) {
            char ch = name.charAt(i++);

            if ((ch == 'Q') && (i < len)) {
                switch (name.charAt(i++)) {
                case 'B':
                    sb.append('/');

                    break;

                case 'C':
                    sb.append(':');

                    break;

                case 'D':
                    sb.append('\\');

                    break;

                case 'E':
                    sb.append('#');

                    break;

                default:
                    sb.append('Q');

                    break;
                }
            } else {
                // not Q
                sb.append(ch);
            }
        }

        return sb.toString();
    }
}
