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

package org.netbeans.modules.java.source.usages;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.usages.ClassIndexImpl.UsageType;

/**
 *
 * @author Tomas Zezula
 */
public class LuceneIndexTest extends NbTestCase {
    
    //Copied from DocumentUtil, nneds to be synchronized when changed
    private static final String FIELD_RESOURCE_NAME = "resourceName";   //NOI18N
    private static final String FIELD_REFERENCES = "references";        //NOI18N
    
    private static final int REF_SIZE = UsageType.values().length;
    
    private File indexFolder1;
    private File indexFolder2;
    
    public LuceneIndexTest (String testName) {
        super (testName);                
    }
    
    protected @Override void setUp() throws Exception {
        super.setUp();
	this.clearWorkDir();
        //Prepare indeces
    }
    
    public void testIndeces () throws Exception {
        if (indexFolder1 != null && indexFolder2 != null) {
            assertTrue (indexFolder1.isDirectory());
            assertTrue (indexFolder1.canRead());
            assertTrue (IndexReader.indexExists(indexFolder1));
            assertTrue (indexFolder2.isDirectory());
            assertTrue (indexFolder2.canRead());
            assertTrue (IndexReader.indexExists(indexFolder2));
            compareIndeces (indexFolder1, indexFolder2);
        }
    }
    
    
    private static void compareIndeces (final File file1, final File file2) throws IOException {
        Directory dir1 = FSDirectory.getDirectory(file1,false);
        Directory dir2 = FSDirectory.getDirectory(file2,false);
        IndexReader in1 = IndexReader.open(dir1);
        try {
            IndexReader in2 = IndexReader.open(dir2);
            try {
                int len1 = in1.numDocs();
                int len2 = in2.numDocs();
                assertEquals("Indeces have different size",len1, len2);
                Searcher search = new IndexSearcher(in2);
                try {
                    for (int i=0; i< len1; i++) {
                        Document doc1 = in1.document(i);
                        String name1 = doc1.getField(FIELD_RESOURCE_NAME).stringValue();
                        if (name1.equals("/")) {
                            continue;
                        }
                        Hits hits = search.search(new TermQuery( new Term(FIELD_RESOURCE_NAME,name1)));
                        assertEquals("No document for " + name1, 1, len2);                        
                        Document doc2 = ((Hit)hits.iterator().next()).getDocument();
                        compare(doc1,doc2);
                    }
                } finally {
                    search.close();
                }
            } finally {
                in2.close();
            }
        } finally {
            in1.close();
        }
    }
    
    private static void compare (Document doc1, Document doc2) throws IOException {                        
        Field[] f1 = doc1.getFields(FIELD_REFERENCES);
        Field[] f2 = doc2.getFields(FIELD_REFERENCES);
        assertEquals("Reference size not equal for:" + doc1 + " and: " + doc2, f1.length,f2.length);
        Map<String,String> m1 = fill (f1);
        Map<String,String> m2 = fill (f2);
        for (Map.Entry<String,String> e : m1.entrySet()) {
            String key = e.getKey();
            String value1 = e.getValue();
            String value2= m2.get(key);
            assertNotNull("Unknown reference: " + key,value2);            
            assertEquals("Different usage types",value1,value2);            
        }
    }
    
    private static Map<String,String> fill (Field[] fs) {
        Map<String,String> m1 = new HashMap<String, String> ();
        for (Field f : fs) {
            String ru = f.stringValue();
            int index = ru.length() - REF_SIZE;
            String key = ru.substring(0,index);
            String value = ru.substring(index);
            m1.put (key, value);
        }
        return m1;
    }
    
}
