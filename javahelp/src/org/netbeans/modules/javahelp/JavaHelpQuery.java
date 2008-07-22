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

package org.netbeans.modules.javahelp;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.help.SearchHit;
import javax.help.SearchTOCItem;
import javax.help.search.SearchEngine;
import javax.help.search.SearchEvent;
import javax.help.search.SearchItem;
import javax.help.search.SearchListener;
import javax.help.search.SearchQuery;
import org.openide.util.Lookup;
import org.netbeans.api.javahelp.Help;
import org.openide.util.NbBundle;

/**
 * Search Java Help for given string.
 * 
 * @author S. Aubrecht
 */
class JavaHelpQuery implements Comparator<SearchTOCItem> {

    private Thread searchThread;
    private static JavaHelpQuery theInstance;
    private SearchEngine engine;
    
    private JavaHelpQuery() {
    }
    
    public static JavaHelpQuery getDefault() {
        if( null == theInstance )
            theInstance = new JavaHelpQuery();
        return theInstance;
    }
    
    public List<SearchTOCItem> search( String searchString ) {
        synchronized( this ) {
            if( null == engine ) {
                engine = createSearchEngine();
            }
        }
        abort();
        List<SearchTOCItem> res = new ArrayList<SearchTOCItem>();
        searchThread = new Thread( createSearch( searchString, res ) );
        searchThread.start();
        try {
            //the first search can take a moment before all the helpsets are merged
            searchThread.join(60*1000); 
        } catch( InterruptedException iE ) {
            //ignore
        }
        return res;
    }
    
    private void abort() {
        if( null == searchThread ) {
            return;
        }
        
        searchThread.interrupt();
        searchThread = null;
    }
    
    private Runnable createSearch( final String searchString, final List<SearchTOCItem> items ) {
        Runnable res = new Runnable() {

            public void run() {
                if( null == engine ) {
                    return;
                }
                SearchQuery query = engine.createQuery();
                final Object SEARCH_DONE = new Object();
                query.addSearchListener( new SearchListener() {

                    public void itemsFound(SearchEvent arg0) {
                        addItemsToList( arg0.getSearchItems(), items );
                    }

                    public void searchStarted(SearchEvent arg0) {
                    }

                    public void searchFinished(SearchEvent arg0) {
                        synchronized( SEARCH_DONE ) {
                            SEARCH_DONE.notifyAll();
                        }
                    }
                });
                query.start(searchString, Locale.getDefault());
                synchronized( SEARCH_DONE ) {
                    try {
                        SEARCH_DONE.wait();
                    } catch (InterruptedException ex) {
                        //ignore
                    }
                }
                //sort the result by their relevance
                Collections.sort( items, JavaHelpQuery.this );
            }
        };
        return res;
    }
    
    private void addItemsToList( Enumeration searchItems, List<SearchTOCItem> results ) {
        if( null == searchItems )
            return;
        while( searchItems.hasMoreElements() ) {
            SearchItem si = (SearchItem) searchItems.nextElement();
            URL url;
            try {
                url = new URL(si.getBase(), si.getFilename());
            } catch( MalformedURLException murlE ) {
                Logger.getLogger(JavaHelpQuery.class.getName()).log(Level.FINE, "Invalid URL in SearchItem: " + si.getTitle(), murlE); //NOI18N
                continue;
            }
            boolean foundToc = false;
            for( SearchTOCItem toc : results ) {
                URL testURL = toc.getURL();
                if (testURL != null && url != null && url.sameFile(testURL)) {
                    toc.addSearchHit( new SearchHit(si.getConfidence(), si.getBegin(), si.getEnd()) );
                    foundToc = true;
                    break;
                }
            }
            if( !foundToc ) {
                SearchTOCItem toc = new SearchTOCItem(si);
                results.add( toc );
            }
        }
    }

    public int compare(SearchTOCItem o1, SearchTOCItem o2) {
        int res = o2.hitCount() - o1.hitCount() ;
        if( 0 == res ) {
            double conf1 = o1.getConfidence();
            double conf2 = o2.getConfidence();
            if( conf1 < conf2 )
                res = -11;
            else if( conf1 > conf2 )
                res = 1;
        }
        return res;
    }
    
    private SearchEngine createSearchEngine() {
        SearchEngine se = null;
        Help h = (Help)Lookup.getDefault().lookup(Help.class);
        if (h != null && h instanceof JavaHelp ) {
            JavaHelp jh = (JavaHelp)h;
            se = jh.createSearchEngine();
            if( null == se ) {
                Logger.getLogger(JavaHelpQuery.class.getName()).log(Level.INFO, 
                        NbBundle.getMessage(JavaHelpQuery.class, "Err_CreateJavaHelpSearchEngine")); //NOI18N
                se = new DummySearchEngine();
            }
        }
        return se;
    }
    
    private static class DummySearchEngine extends SearchEngine {
        @Override
        public SearchQuery createQuery() throws IllegalStateException {
            return new DummySearchQuery( this );
        }
    }
    
    private static class DummySearchQuery extends SearchQuery {
        
        private List<SearchListener> listeners = new ArrayList<SearchListener>(1);
        
        public DummySearchQuery( DummySearchEngine se ) {
            super( se );
        }

        @Override
        public void addSearchListener(SearchListener arg0) {
            listeners.add( arg0 );
        }

        @Override
        public void removeSearchListener(SearchListener arg0) {
            listeners.remove( arg0 );
        }

        @Override
        public void start(String arg0, Locale arg1) throws IllegalArgumentException, IllegalStateException {
            SearchEvent se = new SearchEvent( this, "", false );
            for( SearchListener sl : listeners ) {
                sl.searchStarted(se);
                sl.searchFinished(se);
            }
        }

        @Override
        public void stop() throws IllegalStateException {
            //do nothing
        }
        
        @Override
        public boolean isActive() {
            return false;
        }
        
    }
}
