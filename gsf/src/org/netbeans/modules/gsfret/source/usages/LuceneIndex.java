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

package org.netbeans.modules.gsfret.source.usages;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.RAMDirectory;
import org.netbeans.api.gsf.Index.SearchResult;
import org.netbeans.api.gsf.NameKind;
import org.netbeans.napi.gsfret.source.ClassIndex;
import org.netbeans.modules.gsfret.source.util.LowMemoryEvent;
import org.netbeans.modules.gsfret.source.util.LowMemoryListener;
import org.netbeans.modules.gsfret.source.util.LowMemoryNotifier;
import org.openide.util.Exceptions;

/**
 * Lucene interface - Responsible for storing and and querying at the lowest level.
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 * 
 * Rip out the old query stuff.
 * 
 * @todo Find a faster or more efficient "batch" operation for storing tons of documents
 *   at startup (When scanning the boot path).
 * @todo Can deletion be better?
 * 
 * @author Tomas Zezula
 * @author Tor Norbye
 */
class LuceneIndex extends Index {
    
    private static final boolean debugIndexMerging = Boolean.getBoolean("LuceneIndex.debugIndexMerge");     // NOI18N
    static final String REFERENCES = "gsf";    // NOI18N
    
    private static final Logger LOGGER = Logger.getLogger(LuceneIndex.class.getName());
    
    private final Directory directory;
    private Long rootTimeStamp;
    
    private IndexReader reader; //Cache, do not use this dirrectly, use getReader
    private Set<String> rootPkgCache;   //Cache, do not use this dirrectly
    
    // For debugging purposes only
    private ClassIndexImpl classIndex;
    private File cacheRoot;
        
    private static Set<File> indices = new HashSet<File>();
    
    public static Index create (final File cacheRoot, ClassIndexImpl classIndex) throws IOException { 

        LockFactory lockFactory = Index.isTest() ? NoLockFactory.getNoLockFactory() : new NBLockFactory();

        // As of Lucene 2.1.0, Lucene will complain bitterly if it is being asked to create multiple
        // FSDirectories over the same object IFF the lock manager is different. So if that is the
        // case pass in null as the lock manager (as the assertion says) - it will then use the
        // original lock manager.
        synchronized (LuceneIndex.class) {
            if (indices.contains(cacheRoot)) {
                // Uh oh! This index is in use. Use the same lock as the first time.
                // This is a Lucene 2.1.0 thing.
                lockFactory = null;
            }
            indices.add(cacheRoot);
        }
        
        assert cacheRoot != null && cacheRoot.exists() && cacheRoot.canRead() && cacheRoot.canWrite();
        LuceneIndex index = new LuceneIndex (getReferencesCacheFolder(cacheRoot), lockFactory);
        
        // For debugging (lucene browser) only
        index.classIndex = classIndex;
        index.cacheRoot = cacheRoot;
        
        return index;
    }

    /** Creates a new instance of LuceneIndex */
    private LuceneIndex (final File refCacheRoot, LockFactory lockFactory) throws IOException {
        assert refCacheRoot != null;
        this.directory = FSDirectory.getDirectory(refCacheRoot, lockFactory);
    }

    private void regExpSearch (final Pattern pattern, final Term startTerm, final IndexReader in, final Set<Term> toSearch) throws IOException {        
        final String startText = startTerm.text();
        final StringBuilder startBuilder = new StringBuilder ();
        startBuilder.append(startText.charAt(0));
        for (int i=1; i<startText.length(); i++) {
            char c = startText.charAt(i);
            if (!Character.isJavaIdentifierPart(c)) {
                break;
            }
            startBuilder.append(c);
        }
        final String startPrefix = startBuilder.toString();
        final String camelField = startTerm.field();
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
    
    private void prefixSearch (Term nameTerm, final IndexReader in, final Set<Term> toSearch) throws IOException {
        final String prefixField = nameTerm.field();
        final String name = nameTerm.text();
        final TermEnum en = in.terms(nameTerm);
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

    public boolean isUpToDate(String resourceName, long timeStamp) throws IOException {
        // Don't do anything for preindexed filesystems
        if (Index.isPreindexed(cacheRoot)) {
            return true;
        }
        
        if (!isValid(false)) {
            return false;
        }        
        Searcher searcher = new IndexSearcher (this.getReader());
        try {
            Hits hits;
            if (resourceName == null) {
                synchronized (this) {
                    if (this.rootTimeStamp != null) {
                        return rootTimeStamp.longValue() >= timeStamp;
                    }
                }
                hits = searcher.search(new TermQuery(DocumentUtil.rootDocumentTerm()));
            }
            else {
                hits = searcher.search(DocumentUtil.binaryNameQuery(resourceName));
            }

            assert hits.length() <= 1;
            if (hits.length() == 0) {
                return false;
            }
            else {                    
                try { 
                    Hit hit = (Hit) hits.iterator().next();
                    long cacheTime = DocumentUtil.getTimeStamp(hit.getDocument());
                    if (resourceName == null) {
                        synchronized (this) {
                            this.rootTimeStamp = new Long (cacheTime);
                        }
                    }
                    return cacheTime >= timeStamp;
                } catch (ParseException pe) {
                    throw new IOException ();
                }
            }
        } catch (java.io.FileNotFoundException fnf) {
            this.clear();
            return false;
        } finally {
            searcher.close();
        }
    }
        
    public boolean isValid (boolean tryOpen) throws IOException {  
        boolean res = IndexReader.indexExists(this.directory);
        if (res && tryOpen) {
            try {
                getReader();
            } catch (java.io.IOException e) {
                res = false;
                clear();
            }
        }
        return res;
     }    

    public synchronized void clear () throws IOException {
        this.close ();
        final String[] content = this.directory.list();
        for (String file : content) {
            directory.deleteFile(file);
        }
    }
    
    public synchronized void close () throws IOException {
        if (this.reader != null) {
            this.reader.close();
            this.reader = null;
        }
    }
    
    public @Override String toString () {
        return this.directory.toString();
    }
    
    private synchronized IndexReader getReader () throws IOException {
        if (this.reader == null) {            
            this.reader = IndexReader.open(this.directory);
        }        
        return this.reader;
    }
    
    private synchronized IndexWriter getWriter (final boolean create) throws IOException {
        if (this.reader != null) {
            this.reader.close();
            this.reader = null;
        }
        IndexWriter writer = new IndexWriter (this.directory,new KeywordAnalyzer(), create);
        return writer;
    }
    
    private static File getReferencesCacheFolder (final File cacheRoot) throws IOException {
        File refRoot = new File (cacheRoot,REFERENCES);
        if (!refRoot.exists()) {
            refRoot.mkdir();
        }
        return refRoot;
    }
    
    private static class LMListener implements LowMemoryListener {        
        
        private AtomicBoolean lowMemory = new AtomicBoolean (false);
        
        public void lowMemory(LowMemoryEvent event) {
            lowMemory.set(true);
        }        
    }
    

    // BEGIN TOR MODIFICATIONS
    public void gsfStore(final Set<Map<String,String>> fieldsSet,  Set<Map<String,String>> noIndexFields, final Map<String,String> toDelete) throws IOException {
        this.rootPkgCache = null;
        boolean create = !isValid(false);
        if (!create) {
            IndexReader in = getReader();
            
            if (toDelete.size() > 0) {
                final Searcher searcher = new IndexSearcher (in);
                try {
                    for (String key : toDelete.keySet()) {
                        BooleanQuery query = new BooleanQuery ();
                        String value = toDelete.get(key);
                        query.add (new TermQuery (new Term (key, value)),BooleanClause.Occur.MUST);

                        Hits hits = searcher.search(query);
                        //if (hits.length()>1) {
                        //    // Uhm -- don't we put MULTIPLE documents into the same item now?
                        //    // This isn't abnormal, is it?
                        //    LOGGER.getLogger("global").warning("Multiple(" + hits.length() + ") index entries for key: " + key + " where value: " + value + " where cacheRoot=" + cacheRoot); //NOI18N
                        //}
                        for (int i=0; i<hits.length(); i++) {
                            in.deleteDocument (hits.id(i));
                        }
                    }
                    in.deleteDocuments (DocumentUtil.rootDocumentTerm());
                } finally {
                    searcher.close();
                }
            } else {
                in.deleteDocuments (DocumentUtil.rootDocumentTerm());
            }
        }
        long timeStamp = System.currentTimeMillis();
        gsfStore(fieldsSet, noIndexFields, create, timeStamp);
    }    

    private void gsfStore (final Set<Map<String,String>> fieldsSet, Set<Map<String,String>> noIndexFields, final boolean create, final long timeStamp) throws IOException {        
        final IndexWriter out = getWriter(create);
        try {
            if (debugIndexMerging) {
                out.setInfoStream (System.err);
            }
            final LuceneIndexMBean indexSettings = LuceneIndexMBeanImpl.getDefault();
            if (indexSettings != null) {
                out.setMergeFactor(indexSettings.getMergeFactor());
                out.setMaxMergeDocs(indexSettings.getMaxMergeDocs());
                out.setMaxBufferedDocs(indexSettings.getMaxBufferedDocs());
            }        
            LowMemoryNotifier lm = LowMemoryNotifier.getDefault();
            LMListener lmListener = new LMListener ();
            lm.addLowMemoryListener (lmListener);        
            Directory memDir = null;
            IndexWriter activeOut = null;        
            if (lmListener.lowMemory.getAndSet(false)) {
                activeOut = out;
            }
            else {
                memDir = new RAMDirectory ();
                activeOut = new IndexWriter (memDir,new KeywordAnalyzer(), true);
            }        
            try {
                activeOut.addDocument (DocumentUtil.createRootTimeStampDocument (timeStamp));

                Document newDoc = new Document ();
                newDoc.add(new Field (DocumentUtil.FIELD_TIME_STAMP,DateTools.timeToString(timeStamp,DateTools.Resolution.MILLISECOND),Field.Store.YES,Field.Index.NO));

                for (Map<String,String> fields : fieldsSet) {
                    for (Iterator<Map.Entry<String,String>> it = fields.entrySet().iterator(); it.hasNext();) {
                        Map.Entry<String,String> fieldEntry = it.next();
                        it.remove(); // Uhm.. why?
                        String key = fieldEntry.getKey();
                        String value = fieldEntry.getValue();
                        Field field = new Field(key, value, Field.Store.YES, Field.Index.UN_TOKENIZED);
                        newDoc.add(field);
                    }
                }

                for (Map<String,String> fields : noIndexFields) {
                    for (Iterator<Map.Entry<String,String>> it = fields.entrySet().iterator(); it.hasNext();) {
                        Map.Entry<String,String> fieldEntry = it.next();
                        it.remove();
                        String key = fieldEntry.getKey();
                        String value = fieldEntry.getValue();
                        assert key != null && value != null : "key=" + key + ", value=" + value;
                        Field field = new Field(key, value, Field.Store.YES, Field.Index.NO);
                        newDoc.add(field);
                    }
                }

                activeOut.addDocument(newDoc);
                if (memDir != null && lmListener.lowMemory.getAndSet(false)) {                       
                    activeOut.close();
                    out.addIndexes(new Directory[] {memDir});                        
                    memDir = new RAMDirectory ();        
                    activeOut = new IndexWriter (memDir,new KeywordAnalyzer(), true);
                }
                if (memDir != null) {
                    activeOut.close();
                    out.addIndexes(new Directory[] {memDir});   
                    activeOut = null;
                    memDir = null;
                }
                synchronized (this) {
                    this.rootTimeStamp = new Long (timeStamp);
                }
            } finally {
                lm.removeLowMemoryListener (lmListener);  
            }
        } finally {
            out.close();
        }
    }
    
    
    @SuppressWarnings ("unchecked") // NOI18N, unchecked - lucene has source 1.4
    public void gsfSearch(final String primaryField, final String name, final NameKind kind, final Set<ClassIndex.SearchScope> scope, 
            final Set<SearchResult> result) throws IOException {
        if (!isValid(false)) {
            LOGGER.fine(String.format("LuceneIndex[%s] is invalid!\n", this.toString()));
            return;
        }

        assert name != null;                
        final Set<Term> toSearch = new TreeSet<Term> (new Comparator<Term>(){
            public int compare (Term t1, Term t2) {
                int ret = t1.field().compareTo(t2.field());
                if (ret == 0) {
                    ret = t1.text().compareTo(t2.text());
                }
                return ret;
            }
        });
                
        final IndexReader in = getReader();
        switch (kind) {
            case EXACT_NAME:
                {
                    toSearch.add(new Term (primaryField, name));
                    break;
                }
            case PREFIX:
                if (name.length() == 0) {
                    //Special case (all) handle in different way
                    gsfEmptyPrefixSearch(in, result, primaryField);
                    return;
                }
                else {
                    final Term nameTerm = new Term (primaryField, name);
                    prefixSearch(nameTerm, in, toSearch);
                    break;
                }
            case CASE_INSENSITIVE_PREFIX:
                if (name.length() == 0) {
                    //Special case (all) handle in different way
                    gsfEmptyPrefixSearch(in, result, primaryField);
                    return;
                }
                else {                    
                    final Term nameTerm = new Term (primaryField, name.toLowerCase());
                    prefixSearch(nameTerm, in, toSearch);
                    break;
                }
            case CAMEL_CASE:
                if (name.length() == 0) {
                    throw new IllegalArgumentException ();
                }        
                {
                final StringBuilder patternString = new StringBuilder ();                        
                char startChar = 0;
                for (int i=0; i<name.length(); i++) {
                    char c = name.charAt(i);
                    //todo: maybe check for upper case, I18N????
                    if (i == 0) {
                        startChar = c;
                    }
                    patternString.append(c);
                    if (i == name.length()-1) {
                        patternString.append("\\w*");  // NOI18N
                    }
                    else {
                        patternString.append("[\\p{Lower}\\p{Digit}]*");  // NOI18N
                    }
                }
                final Pattern pattern = Pattern.compile(patternString.toString());
                Term t = new Term (primaryField, Character.toString(startChar));
                regExpSearch(pattern, t, in, toSearch);
                break;
                }
            case CASE_INSENSITIVE_REGEXP:
                if (name.length() == 0 || !Character.isJavaIdentifierStart(name.charAt(0))) {
                    throw new IllegalArgumentException ();
                }
                {   
                    final Pattern pattern = Pattern.compile(name,Pattern.CASE_INSENSITIVE);
                    final Term nameTerm = new Term (primaryField, name.toLowerCase());
                    regExpSearch(pattern, nameTerm, in, toSearch);      //XXX: Locale
                    break;
                }
            case REGEXP:
                if (name.length() == 0 || !Character.isJavaIdentifierStart(name.charAt(0))) {
                    throw new IllegalArgumentException (name);
                }                
                {   
                    final Pattern pattern = Pattern.compile(name);                    
                    final Term nameTerm = new Term (primaryField, name);
                    regExpSearch(pattern, nameTerm, in, toSearch);
                    break;
                }
            default:
                throw new UnsupportedOperationException (kind.toString());
        }           
        TermDocs tds = in.termDocs();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("LuceneIndex.getDeclaredTypes[%s] returned %d elements\n",this.toString(), toSearch.size()));
        }
        final Iterator<Term> it = toSearch.iterator();        
        Set<Integer> docNums = new TreeSet<Integer>();
        Map<Integer,List<String>> matches = new HashMap<Integer,List<String>>();
        while (it.hasNext()) {
            Term next = it.next();
            tds.seek(next);
            while (tds.next()) {
                Integer docNum = Integer.valueOf(tds.doc());
                List<String> matchTerms = matches.get(docNum);
                if (matchTerms == null) {
                    matchTerms = new ArrayList<String>();
                    matches.put(docNum, matchTerms);
                }
                matchTerms.add(next.text());
                docNums.add(docNum);
            }
        }
        for (Integer docNum : docNums) {
            final Document doc = in.document(docNum);
            
            List<String> matchList = matches.get(docNum);
            FilteredDocumentSearchResult map = new FilteredDocumentSearchResult(doc, primaryField, matchList, docNum);
            result.add(map);
        }
    }
    
    // TODO: Create a filtered DocumentSearchResult here which
    // contains matches for a given document.
    
    private class DocumentSearchResult implements SearchResult {
        private Document doc;
        private int docId;
        
        private DocumentSearchResult(Document doc, int docId) {
            this.doc = doc;
            this.docId = docId;
        }

        public String getValue(String key) {
            return doc.get(key);
        }

        public String[] getValues(String key) {
            return doc.getValues(key);
        }
        
        public String toString() {
            StringBuilder sb = new StringBuilder();
            Enumeration en = doc.fields();
            while (en.hasMoreElements()) {
                Field f = (Field)en.nextElement();
                sb.append(f.name());
                sb.append(":");
                sb.append(f.stringValue());
                sb.append("\n");
            }
            
            return sb.toString();
        }
    
        public int getDocumentNumber() {
            return docId;
        }

        public Object getDocument() {
            return doc;
        }

        public Object getIndexReader() {
            try {
                return getReader();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
                return null;
            }
        }

        public Object getIndex() {
            return LuceneIndex.this.classIndex;
        }

        public File getSegment() {
            return LuceneIndex.this.cacheRoot;
        }
    }
    
    private class FilteredDocumentSearchResult implements SearchResult {
        private Document doc;
        private int docId;
        private String primaryKey;
        private List<String> primaryValues;
        
        private FilteredDocumentSearchResult(Document doc, String primaryKey, List<String> primaryValues, int docId) {
            this.doc = doc;
            this.primaryKey = primaryKey;
            this.primaryValues = primaryValues;
            this.docId = docId;
        }

        public String getValue(String key) {
            if (key.equals(primaryKey)) {
                if (primaryValues.size() > 0) {
                    return primaryValues.get(0);
                } else {
                    return null;
                }
            }
            return doc.get(key);
        }

        public String[] getValues(String key) {
            if (key.equals(primaryKey)) {
                return primaryValues.toArray(new String[primaryValues.size()]);
            }
            return doc.getValues(key);
        }
        
        public String toString() {
            StringBuilder sb = new StringBuilder();
            Enumeration en = doc.fields();
            while (en.hasMoreElements()) {
                Field f = (Field)en.nextElement();
                if (f.name().equals(primaryKey)) {
                    sb.append(primaryKey);
                    sb.append(":");
                    sb.append(primaryValues.toString());
                } else {
                    sb.append(f.name());
                    sb.append(":");
                    sb.append(f.stringValue());
                }
                sb.append("\n");
            }
            
            return sb.toString();
        }
    
        public int getDocumentNumber() {
            return docId;
        }

        public Object getDocument() {
            return doc;
        }
        
        public Object getIndexReader() {
            try {
                return getReader();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
                return null;
            }
        }

        public Object getIndex() {
            return LuceneIndex.this.classIndex;
        }

        public File getSegment() {
            return LuceneIndex.this.cacheRoot;
        }
    }
    
    private <T> void gsfEmptyPrefixSearch (final IndexReader in, final Set<SearchResult> result, 
                                        final String primaryField) throws IOException {        
        final int bound = in.maxDoc();        
        for (int i=0; i<bound; i++) {
            if (!in.isDeleted(i)) {
                final Document doc = in.document(i);
                if (doc != null) {
                    SearchResult map = new DocumentSearchResult(doc, i);
                    result.add(map);
                }
            }
        }
    }
    
    // For symbol dumper only
    public IndexReader getDumpIndexReader() throws IOException {
        return getReader();
    }
}
