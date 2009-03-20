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

package org.netbeans.modules.parsing.spi.indexing.support;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.impl.indexing.IndexDocumentImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class IndexResult {

    private static final Logger LOG = Logger.getLogger(IndexResult.class.getName());
    
    private final IndexDocumentImpl spi;
    private final URL root;

    private volatile URL cachedUrl;
    private volatile FileObject cachedFile;

    IndexResult (final IndexDocumentImpl spi, final URL root) {
        assert spi != null;
        assert root != null;
        this.spi = spi;
        this.root = root;
    }

    public String getValue (final String key) {
        Parameters.notEmpty("key", key); //NOI18N
        return this.spi.getValue (key);
    }

    public String[] getValues (final String key) {
        Parameters.notEmpty("key", key); //NOI18N
        return this.spi.getValues (key);
    }

    public URL getUrl() {
        if (cachedUrl == null) {
            URL url = null;
            try {
                url = new URL(root, spi.getSourceName());
            } catch (MalformedURLException ex) {
                LOG.log(Level.WARNING, null, ex);
            }

            synchronized(this) {
                if (cachedUrl == null) {
                    cachedUrl = url;
                }
            }
        }
        return cachedUrl;
    }

    public FileObject getFile () {
        if (cachedFile == null) {
//            final String path = spi.getSourceName();
//            final FileObject rootFo = URLMapper.findFileObject(root);
//            FileObject resource = null;
//            if (rootFo != null) {
//                resource = rootFo.getFileObject(path);
//            }
            FileObject resource = null;
            final URL url = getUrl();
            if (url != null) {
                resource = URLMapper.findFileObject(url);
            }
            synchronized (this) {
                if (cachedFile == null) {
                    cachedFile = resource;
                }
            }
        }
        return cachedFile;
    }
}
