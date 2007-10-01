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

/*
 * JavadocSearchEngineImpl.java
 *
 * Created on 18. ?erven 2001, 14:55
 */

package org.netbeans.modules.javadoc.search;

import java.util.ArrayList;
import org.openide.ErrorManager;

import org.openide.filesystems.FileObject;

/**
 *
 * @author  Petr Suchomel
 * @version 0.1
 */
class JavadocSearchEngineImpl extends JavadocSearchEngine {

    private ArrayList tasks;

    private IndexSearchThread.DocIndexItemConsumer diiConsumer;

    /** Used to search for set elements in javadoc repository
     * @param items to search for
     * @throws NoJavadocException if no javadoc directory is mounted, nothing can be searched
     */
    public void search(String[] items, final SearchEngineCallback callback) throws NoJavadocException {
        FileObject docRoots[] = JavadocRegistry.getDefault().getDocRoots();
        tasks = new ArrayList( docRoots.length );

        diiConsumer = new IndexSearchThread.DocIndexItemConsumer() {
                          public void addDocIndexItem( final DocIndexItem dii ) {
                              callback.addItem(dii);
                          }

                          public void indexSearchThreadFinished( IndexSearchThread t ) {
                              tasks.remove( t );
                              if ( tasks.isEmpty() )
                                  callback.finished();
                          }
                      };
                      
        if ( docRoots.length <= 0 ) {            
            callback.finished();
            throw new NoJavadocException();            
        }
        String toFind = items[0];
        
        for( int i = 0; i < docRoots.length; i++ ) {
            
            JavadocSearchType st = JavadocRegistry.getDefault().findSearchType( docRoots[i] );
            if (st == null) {
                ErrorManager.getDefault().log ("NO Search type for " + docRoots[i]);
                continue;
            }
            FileObject indexFo = st.getDocFileObject( docRoots[i] );
            if (indexFo == null) {
                ErrorManager.getDefault().log ("NO Index files fot " + docRoots[i] );
                continue;
            }            
            
            IndexSearchThread searchThread = st.getSearchThread( toFind, indexFo, diiConsumer );

            tasks.add( searchThread );
            searchThread.go();            
        }
        //callback.finished();
    }
    
    /** Stops execution of Javadoc search thread
     */
    public void stop() {
        for( int i = 0; i < tasks.size(); i++ ) {
            SearchThreadJdk12 searchThread = (SearchThreadJdk12)tasks.get( i );
            searchThread.finish();
        }
    }    
}
