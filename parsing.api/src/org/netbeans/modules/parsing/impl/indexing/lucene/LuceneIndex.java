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

package org.netbeans.modules.parsing.impl.indexing.lucene;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.store.RAMDirectory;
import org.netbeans.modules.parsing.impl.indexing.IndexDocumentImpl;
import org.netbeans.modules.parsing.impl.indexing.IndexImpl;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public class LuceneIndex implements IndexImpl {

    // -----------------------------------------------------------------------
    // IndexImpl implementation
    // -----------------------------------------------------------------------
    
    /**
     * Adds document
     * @param document
     */
    public void addDocument(final IndexDocumentImpl document) {
        final boolean forceFlush;

        synchronized (this) {
            assert document instanceof LuceneDocument;

            toAdd.add((LuceneDocument) document);
            forceFlush = toAdd.size() > MAX_DOCS || lmListener.isLowMemory();
        }

        if (forceFlush) {
            try {
                LOGGER.fine("Extra flush forced"); //NOI18N
                store();
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, null, annotateException(ioe, indexFolder));
            }
        }
    }

    /**
     * Removes all documents for given path
     * @param relativePath
     */
    public void removeDocument(final String relativePath) {
        final boolean forceFlush;

        synchronized (this) {
            toRemove.add(relativePath);
            forceFlush = toAdd.size() > MAX_DOCS || lmListener.isLowMemory();
        }

        if (forceFlush) {
            try {
                LOGGER.fine("Extra flush forced"); //NOI18N
                store();
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, null, annotateException(ioe, indexFolder));
            }
        }
    }

    public void store() throws IOException {
        LuceneIndexManager.getDefault().writeAccess(new LuceneIndexManager.Action<Void>() {
            public Void run() throws IOException {
                checkPreconditions();

                final List<LuceneDocument> toAdd;
                final List<String> toRemove;

                synchronized (LuceneIndex.this) {
                    toAdd = new LinkedList<LuceneDocument>(LuceneIndex.this.toAdd);
                    toRemove = new LinkedList<String>(LuceneIndex.this.toRemove);

                    LuceneIndex.this.toAdd.clear();
                    LuceneIndex.this.toRemove.clear();
                    for(LuceneDocument ldoc : toAdd) {
                        LuceneIndex.this.staleFiles.remove(ldoc.getSourceName());
                    }
                    LuceneIndex.this.staleFiles.removeAll(toRemove);
                }

                if (toAdd.size() > 0 || toRemove.size() > 0) {
                    flush(indexFolder, toAdd, toRemove, LuceneIndex.this.directory, lmListener);
                }
                
                return null;
            }
        });
    }

    public Collection<? extends IndexDocumentImpl> query(
            final String fieldName,
            final String value,
            final QuerySupport.Kind kind,
            final String... fieldsToLoad
    ) throws IOException {
        assert fieldName != null;
        assert value != null;
        assert kind != null;

        return LuceneIndexManager.getDefault().readAccess(new LuceneIndexManager.Action<List<IndexDocumentImpl>>() {
            public List<IndexDocumentImpl> run() throws IOException {
                checkPreconditions();
                
                final IndexReader r = getReader(false);
                if (r != null) {
                    // index exists
                    return _query(r, fieldName, value, kind, fieldsToLoad);
                } else {
                    // no index
                    return Collections.<IndexDocumentImpl>emptyList();
                }
            }
        });        
    }

    public void fileModified(String relativePath) {
        synchronized (this) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(this + ", adding stale file: " + relativePath); //NOI18N
            }
            staleFiles.add(relativePath);
        }
    }

    public Collection<? extends String> getStaleFiles() {
        synchronized (this) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(this + ", stale files: " + staleFiles); //NOI18N
            }
            return new LinkedList<String>(staleFiles);
        }
    }

    // -----------------------------------------------------------------------
    // Public implementation
    // -----------------------------------------------------------------------

    public LuceneIndex(final URL root) throws IOException {
        assert root != null;
        try {
            indexFolder = new File(root.toURI());
            directory = FSDirectory.getDirectory(indexFolder, NoLockFactory.getNoLockFactory());
        } catch (URISyntaxException e) {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        }
    }

    public void clear() throws IOException {
        checkPreconditions();
        LuceneIndexManager.getDefault().writeAccess(new LuceneIndexManager.Action<Void>() {
            public Void run() throws IOException {
                _clear();
                return null;
            }
        });
    }
    
    public void close() throws IOException {
        checkPreconditions();
        LuceneIndexManager.getDefault().writeAccess(new LuceneIndexManager.Action<Void>() {
            public Void run() throws IOException {
                _close();
                return null;
            }
        });
    }

    // -----------------------------------------------------------------------
    // Private implementation
    // -----------------------------------------------------------------------

    private static final Logger LOGGER = Logger.getLogger(LuceneIndex.class.getName());
    private static final boolean debugIndexMerging = Boolean.getBoolean("LuceneIndex.debugIndexMerge");     // NOI18N

    /* package */ static final int VERSION = 1;
    private static final int MAX_DOCS = 2000;

    private final File indexFolder;

    //@GuardedBy (LuceneIndexManager.writeAccess)
    private volatile Directory directory;
    private volatile IndexReader reader; //Cache, do not use this directly, use getReader
    private volatile boolean closed;

    private static final LMListener lmListener = new LMListener();

    //@GuardedBy (this)
    private final List<LuceneDocument> toAdd = new LinkedList<LuceneDocument>();
    private final List<String> toRemove = new LinkedList<String>();
    private final Set<String> staleFiles = new HashSet<String>();

    // called under LuceneIndexManager.writeAccess
    private void _clear() throws IOException {
        _close();
        try {
            final String[] content = this.directory.list();
            boolean dirty = false;
            for (String file : content) {
                try {
                    directory.deleteFile(file);
                } catch (IOException e) {
                    //Some temporary files
                    if (directory.fileExists(file)) {
                        dirty = true;
                    }
                }
            }
            if (dirty) {
                //Try to delete dirty files and log what's wrong
                final File cacheDir = ((FSDirectory)this.directory).getFile();
                final File[] children = cacheDir.listFiles();
                if (children != null) {
                    for (final File child : children) {
                        if (!child.delete()) {
                            final Class c = this.directory.getClass();
                            int refCount = -1;
                            try {
                                final Field field = c.getDeclaredField("refCount"); //NOI18N
                                field.setAccessible(true);
                                refCount = field.getInt(this.directory);
                            } catch (NoSuchFieldException e) {/*Not important*/}
                              catch (IllegalAccessException e) {/*Not important*/}

                            throw new IOException("Cannot delete: " + child.getAbsolutePath() + "(" +   //NOI18N
                                    child.exists()  +","+                                               //NOI18N
                                    child.canRead() +","+                                               //NOI18N
                                    child.canWrite() +","+                                              //NOI18N
                                    cacheDir.canRead() +","+                                            //NOI18N
                                    cacheDir.canWrite() +","+                                           //NOI18N
                                    refCount+")");                                                      //NOI18N
                        }
                    }
                }
            }
        } finally {
            //Need to recreate directory, see issue: #148374
            this.directory = FSDirectory.getDirectory(indexFolder, NoLockFactory.getNoLockFactory());      //Locking controlled by rwlock
            closed = false;
        }
    }

    // called under LuceneIndexManager.writeAccess
    private void _close() throws IOException {
        try {
            if (reader != null) {
                reader.close();
                reader = null;
            }
        } finally {
           directory.close();
           closed = true;
        }
    }

    // called under LuceneIndexManager.readAccess
    private static List<IndexDocumentImpl> _query(
            final IndexReader in,
            final String fieldName,
            final String value,
            final QuerySupport.Kind kind,
            final String... fieldsToLoad
    ) throws IOException {
        
        final List<IndexDocumentImpl> result = new LinkedList<IndexDocumentImpl>();
        final Set<Term> toSearch = new TreeSet<Term> (new TermComparator());

        switch (kind) {
            case EXACT:
                {
                    toSearch.add(new Term (fieldName,value));
                    break;
                }
            case PREFIX:
                if (value.length() == 0) {
                    //Special case (all) handle in different way
                    emptyPrefixSearch(in, fieldsToLoad, result);
                    return result;
                }
                else {
                    final Term nameTerm = new Term (fieldName, value);
                    prefixSearch(nameTerm, in, toSearch);
                    break;
                }
            case CASE_INSENSITIVE_PREFIX:
                if (value.length() == 0) {
                    //Special case (all) handle in different way
                    emptyPrefixSearch(in, fieldsToLoad, result);
                    return result;
                }
                else {
                    final Term nameTerm = new Term (fieldName,value.toLowerCase());     //XXX: I18N, Locale
                    prefixSearch(nameTerm, in, toSearch);
                    break;
                }
            case CAMEL_CASE:
                if (value.length() == 0) {
                    throw new IllegalArgumentException ();
                }
                {
                    StringBuilder sb = new StringBuilder();
                    String prefix = null;
                    int lastIndex = 0;
                    int index;
                    do {
                        index = findNextUpper(value, lastIndex + 1);
                        String token = value.substring(lastIndex, index == -1 ? value.length(): index);
                        if ( lastIndex == 0 ) {
                            prefix = token;
                        }
                        sb.append(token);
                        sb.append( index != -1 ?  "[\\p{javaLowerCase}\\p{Digit}_\\$]*" : ".*"); // NOI18N
                        lastIndex = index;
                    }
                    while(index != -1);

                    final Pattern pattern = Pattern.compile(sb.toString());
                    regExpSearch(pattern, new Term (fieldName,prefix),in,toSearch);
                }
                break;
            case CASE_INSENSITIVE_REGEXP:
                if (value.length() == 0) {
                    throw new IllegalArgumentException ();
                }
                else {
                    final Pattern pattern = Pattern.compile(value,Pattern.CASE_INSENSITIVE);
                    if (Character.isJavaIdentifierStart(value.charAt(0))) {
                        regExpSearch(pattern, new Term (fieldName, value.toLowerCase()), in, toSearch);      //XXX: Locale
                    }
                    else {
                        regExpSearch(pattern, new Term (fieldName,""), in, toSearch);      //NOI18N
                    }
                    break;
                }
            case REGEXP:
                if (value.length() == 0) {
                    throw new IllegalArgumentException ();
                } else {
                    final Pattern pattern = Pattern.compile(value);
                    if (Character.isJavaIdentifierStart(value.charAt(0))) {
                        regExpSearch(pattern, new Term (fieldName, value), in, toSearch);
                    }
                    else {
                        regExpSearch(pattern, new Term(fieldName,""), in, toSearch);             //NOI18N
                    }
                    break;
                }
            case CASE_INSENSITIVE_CAMEL_CASE:
                if (value.length() == 0) {
                    //Special case (all) handle in different way
                    emptyPrefixSearch(in, fieldsToLoad, result);
                    return result;
                }
                else {
                    final Term nameTerm = new Term(fieldName,value.toLowerCase());     //XXX: I18N, Locale
                    prefixSearch(nameTerm, in, toSearch);
                    StringBuilder sb = new StringBuilder();
                    String prefix = null;
                    int lastIndex = 0;
                    int index;
                    do {
                        index = findNextUpper(value, lastIndex + 1);
                        String token = value.substring(lastIndex, index == -1 ? value.length(): index);
                        if ( lastIndex == 0 ) {
                            prefix = token;
                        }
                        sb.append(token);
                        sb.append( index != -1 ?  "[\\p{javaLowerCase}\\p{Digit}_\\$]*" : ".*"); // NOI18N
                        lastIndex = index;
                    }
                    while(index != -1);
                    final Pattern pattern = Pattern.compile(sb.toString());
                    regExpSearch(pattern,new Term (fieldName, prefix),in,toSearch);
                    break;
                }
            default:
                throw new UnsupportedOperationException (kind.toString());
        }
        TermDocs tds = in.termDocs();
        final Iterator<Term> it = toSearch.iterator();
        Set<Integer> docNums = new TreeSet<Integer>();
        int[] docs = new int[25];
        int[] freq = new int [25];
        int len;
        while (it.hasNext()) {
            tds.seek(it.next());
            while ((len = tds.read(docs, freq))>0) {
                for (int i = 0; i < len; i++) {
                    docNums.add (docs[i]);
                }
                if (len < docs.length) {
                    break;
                }
            }
        }
        final FieldSelector selector = DocumentUtil.selector(fieldsToLoad);
        for (Integer docNum : docNums) {
            final Document doc = in.document(docNum, selector);
            result.add (new LuceneDocument(doc));
        }
        return result;
    }

    private static void deleteFile (final IndexReader in, final Searcher searcher, final String toRemoveItem) throws IOException {
        Hits hits = searcher.search(DocumentUtil.sourceNameQuery(toRemoveItem));
        //Create copy of hists
        int[] dindx = new int[hits.length()];
        int dindxLength = 0;
        for (int i=0; i<dindx.length; i++) {
            dindx[dindxLength++] = hits.id(i);
        }
        for (int i=0; i<dindxLength; i++) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Deleting from index: " + toRemoveItem + ", lucene document idx=" + dindx[i]); //NOI18N
            }
            in.deleteDocument (dindx[i]);
        }
    }

    // called under LuceneIndexManager.writeAccess
    private void flush(File indexFolder, List<LuceneDocument> toAdd, List<String> toRemove, Directory directory, LMListener lmListener) throws IOException {
        LOGGER.log(Level.FINE, "Flushing: {0}", indexFolder); //NOI18N
        try {
            //assert ClassIndexManager.getDefault().holdsWriteLock();
            //1) delete all documents from to delete and toAdd
            final IndexReader in = getReader(true);
            if (in != null) {
                try {
                    final Searcher searcher = new IndexSearcher (in);
                    try {
                        for (Iterator<String> it = toRemove.iterator(); it.hasNext();) {
                            String toRemoveItem = it.next();
                            it.remove();
                            deleteFile (in, searcher, toRemoveItem);
                        }
                        for (LuceneDocument toRemoveItem : toAdd) {
                            deleteFile(in, searcher, toRemoveItem.getSourceName());
                        }
                    } finally {
                        searcher.close();
                    }
                } finally {
                    in.close();
                }
            }
            
            //2) add all documents form to add
            IndexWriter out = new IndexWriter(
                directory, // index directory
                false, // auto-commit each flush
                new KeywordAnalyzer(),
                in == null // open existing or create new index
            );
            try {
                if (debugIndexMerging) {
                    out.setInfoStream (System.err);
                }

                Directory memDir = null;
                IndexWriter activeOut = null;
                if (lmListener.isLowMemory()) {
                    activeOut = out;
                }
                else {
                    memDir = new RAMDirectory ();
                    activeOut = new IndexWriter (memDir, new KeywordAnalyzer(), true);
                }
                for (Iterator<LuceneDocument> it = toAdd.iterator(); it.hasNext();) {
                    final LuceneDocument doc = it.next();
                    it.remove();
                    activeOut.addDocument(doc.doc);
                    if (memDir != null && lmListener.isLowMemory()) {
                        activeOut.close();
                        out.addIndexes(new Directory[] {memDir});
                        memDir = new RAMDirectory ();
                        activeOut = new IndexWriter (memDir, new KeywordAnalyzer(), true);
                    }
                    LOGGER.log(Level.FINEST, "LuceneDocument merged: {0}", doc); //NOI18N
                }
                if (memDir != null) {
                    activeOut.close();
                    out.addIndexes(new Directory[] {memDir});
                    activeOut = null;
                    memDir = null;
                }
            } finally {
                out.close();
            }
        } finally {
            LOGGER.log(Level.FINE, "Index flushed: {0}", indexFolder); //NOI18N
        }
    }

    // called under LuceneIndexManager.readAccess
    private void checkPreconditions() throws IOException {
        if (closed) {
            throw new IOException("Index already closed: " + indexFolder); //NOI18N
        }
    }

    // called under LuceneIndexManager.readAccess or LuceneIndexManager.writeAccess
    private IndexReader getReader(boolean detach) throws IOException {
        IndexReader r = reader;

        if (r == null) {
            boolean exists = IndexReader.indexExists(this.directory);
            if (exists) {
                //Issue #149757 - logging
                try {
                    //It's important that no Query will get access to original IndexReader
                    //any norms call to it will initialize the HashTable of norms: sizeof (byte) * maxDoc() * max(number of unique fields in document)
                    r = new NoNormsReader(IndexReader.open(this.directory));
                } catch (IOException ioe) {
                    throw annotateException(ioe, indexFolder);
                }

                synchronized (this) {
                    if (reader == null) {
                        reader = r;
                    } else {
                        try {
                            r.close();
                        } catch (IOException ioe) {
                            LOGGER.log(Level.WARNING, null, ioe);
                            // log, but carry on
                        }

                        r = reader;
                    }
                }
            } else {
                LOGGER.fine(String.format("LuceneIndex[%s] does not exist.", this.toString())); //NOI18N
            }
        }

        if (detach) {
            synchronized (this) {
                reader = null;
            }
        }

        return r;
    }

    @Override
    public String toString () {
        return getClass().getSimpleName()+"["+indexFolder.getAbsolutePath()+"]";  //NOI18N
    }

    private static IOException annotateException (final IOException ioe, final File indexFolder) {
        String message;
        File[] children = indexFolder == null ? null : indexFolder.listFiles();
        if (children == null) {
            message = "Non existing index folder"; //NOI18N
        }
        else {
            StringBuilder b = new StringBuilder();
            b.append("Index folder: ").append(indexFolder.getAbsolutePath()).append("\n"); //NOI18N
            for (File c : children) {
                b.append(c.getName()).append(" f: ").append(c.isFile()) //NOI18N
                    .append(" r: ").append(c.canRead()) //NOI18N
                    .append(" w: ").append(c.canWrite()) //NOI18N
                    .append("\n");  //NOI18N
            }
            message = b.toString();
        }
        return Exceptions.attachMessage(ioe, message);
    }

    private static void emptyPrefixSearch (final IndexReader in, final String[] fieldsToLoad, final List<? super IndexDocumentImpl> result) throws IOException {
        final int bound = in.maxDoc();
        for (int i=0; i<bound; i++) {
            if (!in.isDeleted(i)) {
                final Document doc = in.document(i, DocumentUtil.selector(fieldsToLoad));
                if (doc != null) {
                    result.add (new LuceneDocument(doc));
                }
            }
        }
    }

    private static void prefixSearch (final Term valueTerm, final IndexReader in, final Set<? super Term> toSearch) throws IOException {
        final Object prefixField = valueTerm.field(); // It's Object only to silence the stupid hint
        final String name = valueTerm.text();
        final TermEnum en = in.terms(valueTerm);
        try {
            do {
                Term term = en.term();
                if (term != null && prefixField == term.field() && term.text().startsWith(name)) {
                    toSearch.add (term);
                }
                else {
                    break;
                }
            } while (en.next());
        } finally {
            en.close();
        }
    }

    private static void regExpSearch (final Pattern pattern, Term startTerm, final IndexReader in, final Set< ? super Term> toSearch) throws IOException {
        final String startText = startTerm.text();
        String startPrefix;
        if (startText.length() > 0) {
            final StringBuilder startBuilder = new StringBuilder ();
            startBuilder.append(startText.charAt(0));
            for (int i=1; i<startText.length(); i++) {
                char c = startText.charAt(i);
                if (!Character.isJavaIdentifierPart(c)) {
                    break;
                }
                startBuilder.append(c);
            }
            startPrefix = startBuilder.toString();
            startTerm = new Term (startTerm.field(),startPrefix);
        }
        else {
            startPrefix=startText;
        }
        final Object camelField = startTerm.field(); // It's Object only to silence the stupid hint
        final TermEnum en = in.terms(startTerm);
        try {
            do {
                Term term = en.term();
                if (term != null && camelField == term.field() && term.text().startsWith(startPrefix)) {
                    final Matcher m = pattern.matcher(term.text());
                    if (m.matches()) {
                        toSearch.add (term);
                    }
                }
                else {
                    break;
                }
            } while (en.next());
        } finally {
            en.close();
        }
    }

    private static int findNextUpper(String text, int offset ) {

        for( int i = offset; i < text.length(); i++ ) {
            if ( Character.isUpperCase(text.charAt(i)) ) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Expert: Bypass read of norms
     */
    private static final class NoNormsReader extends FilterIndexReader {


        //@GuardedBy (this)
        private byte[] norms;

        public NoNormsReader (final IndexReader reader) {
            super (reader);
        }

        @Override
        public byte[] norms(String field) throws IOException {
            byte[] fakes = fakeNorms ();
            return fakes;
        }

        @Override
        public void norms(String field, byte[] norm, int offset) throws IOException {
            byte[] fakes = fakeNorms ();
            System.arraycopy(fakes, 0, norm, offset, fakes.length);
        }

        @Override
        public boolean hasNorms(String field) throws IOException {
            return false;
        }

        @Override
        protected void doSetNorm(int doc, String field, byte norm) throws CorruptIndexException, IOException {
            //Ignore
        }

        @Override
        protected void doClose() throws IOException {
            synchronized (this)  {
                this.norms = null;
            }
            super.doClose();
        }

        /**
         * Expert: Fakes norms, norms are not needed for Netbeans index.
         */
        private synchronized byte[] fakeNorms() {
            if (this.norms == null) {
                this.norms = new byte[maxDoc()];
                Arrays.fill(this.norms, DefaultSimilarity.encodeNorm(1.0f));
            }
            return this.norms;
        }
    } // End of NoNormsReader class

}
