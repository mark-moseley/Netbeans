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

package org.netbeans.modules.jumpto.file;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;

/**
 *
 * @author vita
 */
public final class FileIndexer extends CustomIndexer {

    // -----------------------------------------------------------------------
    // public implementation
    // -----------------------------------------------------------------------

    public static final String ID = "org-netbeans-modules-jumpto-file-FileIndexer"; //NOI18N
    public static final int VERSION = 1;
    
    public static final String FIELD_NAME = "file-name"; //NOI18N
    public static final String FIELD_CASE_INSENSITIVE_NAME = "ci-file-name"; //NOI18N
    public static final String FIELD_MIME_TYPE = "mime-type"; //NOI18N

    // used from the XML layer
    public static final class Factory extends CustomIndexerFactory {

        @Override
        public CustomIndexer createIndexer() {
            return new FileIndexer();
        }

        @Override
        public void filesDeleted(Collection<? extends Indexable> deleted, Context context) {
            try {
                IndexingSupport is = IndexingSupport.getInstance(context);
                for(Indexable i : deleted) {
                    is.removeDocuments(i);
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.finest("removed " + i.getURL() + "/" + i.getRelativePath());
                    }
                }
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
            }
        }

        @Override
        public void filesDirty(Collection<? extends Indexable> dirty, Context context) {
            // no need to do anything, we are not indexing anythong from inside of the file
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return true;
        }

        @Override
        public String getIndexerName() {
            return ID;
        }

        @Override
        public int getIndexVersion() {
            return VERSION;
        }
    } // End of CustomIndexerFactory class

    // -----------------------------------------------------------------------
    // CustomIndexer implementation
    // -----------------------------------------------------------------------

    @Override
    protected void index(Iterable<? extends Indexable> files, Context context) {
        try {
            long tm1 = System.currentTimeMillis();
            int cnt = 0;
            IndexingSupport is = IndexingSupport.getInstance(context);
            for(Indexable i : files) {
                if (context.isCancelled()) {
                    LOG.fine("Indexer cancelled"); //NOI18N
                    break;
                }
                cnt++;
                String nameExt = getNameExt(i);
                if (nameExt.length() > 0) {
                    IndexDocument d = is.createDocument(i);
                    d.addPair(FIELD_NAME, nameExt, true, true);
                    d.addPair(FIELD_CASE_INSENSITIVE_NAME, nameExt.toLowerCase(Locale.ENGLISH), true, true);
                    String mimeType = getMimeType(i);
                    if (mimeType != null) {
                        d.addPair(FIELD_MIME_TYPE, mimeType, false, true);
                    }
                    is.addDocument(d);
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.finest("added " + i.getURL() + "/" + i.getRelativePath());
                    }
                }
            }
            long tm2 = System.currentTimeMillis();
            
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Processed " + cnt + " files in " + (tm2 - tm1) + "ms."); //NOI18N
            }
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
        }
    }

    // -----------------------------------------------------------------------
    // private implementation
    // -----------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(FileIndexer.class.getName());

    private static String getNameExt(Indexable i) {
        String path = i.getRelativePath();
        int lastSlash = path.lastIndexOf('/'); //NOI18N
        if (lastSlash != -1) {
            return path.substring(lastSlash + 1);
        } else {
            return i.getRelativePath();
        }
    }

    private static String getMimeType(Indexable i) {
//        FileObject f = URLMapper.findFileObject(i.getURL());
//        if (f != null) {
            String mimeType = i.getMimeType();
            if (!mimeType.equals("content/unknown")) { //NOI18N
                return mimeType;
            }
//        }
        return null;
    }
}
