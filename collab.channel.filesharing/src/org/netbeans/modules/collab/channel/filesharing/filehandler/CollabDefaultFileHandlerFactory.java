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
package org.netbeans.modules.collab.channel.filesharing.filehandler;

import org.openide.util.*;

import java.util.*;


/**
 * Default FileHandler Factory
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class CollabDefaultFileHandlerFactory extends Object implements CollabFileHandlerFactory {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* FileHandlerFactory ID */
    public static final String DEFAULT_FILEHANDLER_FACTORY_ID = "defaultfilehandler"; // NOI18N

    /* mime-type of the instance created by this factory */
    public static final String CONTENT_UNKNOWN = "content/unknown"; // NOI18N

    /* mapping of file extensions to content-types */
    private java.util.Dictionary map = new java.util.Hashtable();

    /**
     * constructor
     *
     */
    public CollabDefaultFileHandlerFactory() {
        super();
        setMIMEType("", "content/unknown"); // NOI18N
        setMIMEType("uu", "application/octet-stream"); // NOI18N
        setMIMEType("exe", "application/octet-stream"); // NOI18N
        setMIMEType("ps", "application/postscript"); // NOI18N
        setMIMEType("zip", "application/zip"); // NOI18N
        setMIMEType("class", "application/octet-stream"); // Sun uses application/java-vm // NOI18N
        setMIMEType("jar", "application/x-jar"); // NOI18N
        setMIMEType("sh", "application/x-shar"); // NOI18N
        setMIMEType("tar", "application/x-tar"); // NOI18N
        setMIMEType("snd", "audio/basic"); // NOI18N
        setMIMEType("au", "audio/basic"); // NOI18N
        setMIMEType("wav", "audio/x-wav"); // NOI18N
        setMIMEType("gif", "image/gif"); // NOI18N
        setMIMEType("jpg", "image/jpeg"); // NOI18N
        setMIMEType("jpeg", "image/jpeg"); // NOI18N
        setMIMEType("ra", "audio/x-pn-realaudio"); // NOI18N
        setMIMEType("ram", "audio/x-pn-realaudio"); // NOI18N
        setMIMEType("rm", "audio/x-pn-realaudio"); // NOI18N
        setMIMEType("rpm", "audio/x-pn-realaudio"); // NOI18N
        setMIMEType("mov", "video/quicktime"); // NOI18N		
    }

    /**
     * getter for ID
     *
     * @return        ID
     */
    public String getID() {
        return DEFAULT_FILEHANDLER_FACTORY_ID;
    }

    /**
     * getter for displayName
     *
     * @return        displayName
     */
    public String getDisplayName() {
        return NbBundle.getMessage(
            CollabDefaultFileHandlerFactory.class, "LBL_CollabDefaultFileHandlerFactory_DisplayName"
        ); // NOI18N
    }

    /**
     * create FileHandler instance
     *
     * @return        CollabFileHandler
     */
    public CollabFileHandler createCollabFileHandler() {
        return new CollabDefaultFileHandler();
    }

    /**
     *
     * @return
     */
    public static CollabDefaultFileHandlerFactory getDefault() {
        return getDefault(true);
    }

    /**
     *
     * @return
     */
    public static CollabDefaultFileHandlerFactory getDefaultNoLookup() {
        return getDefault(false);
    }

    /**
     *
     * @return FileHandler Factory
     */
    private static CollabDefaultFileHandlerFactory getDefault(boolean doIDELookup) {
        if (doIDELookup) {
            return (CollabDefaultFileHandlerFactory) Lookup.getDefault().lookup(CollabDefaultFileHandlerFactory.class);
        } else {
            return new CollabDefaultFileHandlerFactory();
        }
    }

    /**
     * test if the factory object support the given mimeType or fileExt
     *
     * @param mimeType
     * @param fileExt
     * @return
     */
    public boolean canHandleMIMEType(String mimeType, String fileExt) {
        return canHandleMIMEType(mimeType) || canHandleFileExt(fileExt);
    }

    /**
     * test if the factory object support the given mimeType
     *
     * @param mimeType
     * @return
     */
    public boolean canHandleMIMEType(String mimeType) {
        if (mimeType == null) {
            return false;
        }

        Enumeration values = map.elements();

        while (values.hasMoreElements()) {
            String supportedMimeType = (String) values.nextElement();

            if (supportedMimeType.equals(mimeType)) {
                return true;
            }
        }

        return false;
    }

    /**
     *
     *
     */
    private boolean canHandleFileExt(String fileExt) {
        if ((fileExt == null) || fileExt.equals("")) {
            return false;
        }

        String mimeType = getMIMEType(fileExt);

        if (mimeType == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * getter for contentType
     *
     * @return        contentType
     */
    public String getContentType() {
        return CONTENT_UNKNOWN;
    }

    /** Obtain MIME type for a well-known extension.
    * If there is a case-sensitive match, that is used, else will fall back
    * to a case-insensitive match.
    * @param ext the extension: <code>"jar"</code>, <code>"zip"</code>, etc.
    * @return the MIME type for the extension, or <code>null</code> if the extension is unrecognized
    * @deprecated in favour of {@link #getMIMEType(FileObject) getMIMEType(FileObject)} as MIME cannot
    * be generaly detected by a file object extension.
    */
    public String getMIMEType(String ext) {
        String s = (String) map.get(ext);

        if (s != null) {
            return s;
        } else {
            return (String) map.get(ext.toLowerCase());
        }
    }

    /**
     * Register MIME type for a new extension.
     * Note that you may register a case-sensitive extension if that is
     * relevant (for example <samp>*.C</samp> for C++) but if you register
     * a lowercase extension it will by default apply to uppercase extensions
     * too (for use on Windows or generally for situations where filenames
     * become accidentally upcased).
     * @param ext the file extension (should be lowercase unless you specifically care about case)
     * @param mimeType the new MIME type
     * @throws IllegalArgumentException if this extension was already registered with a <em>different</em> MIME type
     * @see #getMIMEType
     * @deprecated You should instead use the more general {@link MIMEResolver} system.
     */
    public void setMIMEType(String ext, String mimeType) {
        synchronized (map) {
            String old = (String) map.get(ext);

            if (old == null) {
                map.put(ext, mimeType);
            } else {
                if (!old.equals(mimeType)) {
                    throw new IllegalArgumentException(
                        "Cannot overwrite existing MIME type mapping for " + //NOI18N
                        "extension `" + ext + "' with " + mimeType + //NOI18N
                        " (was " + old + ")"
                    ); // NOI18N
                }
            }
        }
    }
}
