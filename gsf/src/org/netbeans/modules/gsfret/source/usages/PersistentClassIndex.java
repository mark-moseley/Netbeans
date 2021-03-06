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
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.gsf.Index.SearchResult;
import org.netbeans.api.gsf.CancellableTask;
import org.netbeans.api.gsf.NameKind;
import org.netbeans.napi.gsfret.source.CompilationController;
import org.netbeans.napi.gsfret.source.CompilationInfo;
import org.netbeans.api.gsfpath.queries.SourceForBinaryQuery;
import org.netbeans.napi.gsfret.source.ClassIndex;
import org.netbeans.napi.gsfret.source.Phase;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.modules.gsfret.source.SourceAccessor;
import static org.netbeans.modules.gsfret.source.usages.ClassIndexImpl.UsageType.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Exceptions;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 *
 * @author Petr Hrebejk, Tomas Zezula
 */
public class PersistentClassIndex extends ClassIndexImpl {    
    
    private final Index index;
    private final URL root;
    private final boolean isSource;
    private WeakReference<Source> dirty;
    private static final Logger LOGGER = Logger.getLogger(PersistentClassIndex.class.getName());
    
    /** Creates a new instance of ClassesAndMembersUQ */
    private PersistentClassIndex(final URL root, final File cacheRoot, final boolean source) 
        throws IOException, IllegalArgumentException {
        assert root != null;
        this.root = root;
        this.index = LuceneIndex.create (cacheRoot, this);
        this.isSource = source;
    // BEGIN TOR MODIFICATIONS
        this.cacheRoot = cacheRoot;
    // END TOR MODIFICATIONS
    }
    
    public BinaryAnalyser getBinaryAnalyser () {
        return new BinaryAnalyser (this.index);
    }
    
    public SourceAnalyser getSourceAnalyser () {        
        return new SourceAnalyser (this.index);        
    }
    
    public FileObject[] getSourceRoots () {
        FileObject[] rootFos;
        if (isSource) {
            FileObject rootFo = URLMapper.findFileObject (this.root);
            rootFos = rootFo == null ? new FileObject[0]  : new FileObject[] {rootFo};
        }
        else {
            rootFos = SourceForBinaryQuery.findSourceRoots(this.root).getRoots();
        }
        return rootFos;
    }
    

    // Factory method
    
    public static ClassIndexImpl create(URL root, final File cacheRoot, final boolean indexNow) 
        throws IOException, IllegalArgumentException {        
        return new PersistentClassIndex(root, cacheRoot, indexNow);
    }
    
    public synchronized void setDirty (final Source js) {        
        if (js == null) {
            this.dirty = null;
        }
        else if (this.dirty == null || this.dirty.get() != js) {
            this.dirty = new WeakReference (js);
        }
    }
    
    public @Override String toString () {
        return "CompromiseUQ["+this.root.toExternalForm()+"]";     // NOI18N
    }
    
    //Protected methods --------------------------------------------------------
    protected final void close () throws IOException {
        this.index.close();
    }
    
    
    // Private methods ---------------------------------------------------------                          
    
    private void updateDirty () {
        WeakReference<Source> jsRef;        
        synchronized (this) {
            jsRef = this.dirty;
        }
        if (jsRef != null) {
            final Source js = jsRef.get();
            if (js != null) {
                final long startTime = System.currentTimeMillis();
                if (SourceAccessor.INSTANCE.isDispatchThread()) {
                    //Already under javac's lock
                    try {
                        ClassIndexManager.getDefault().writeLock(
                            new ClassIndexManager.ExceptionAction<Void>() {
                                public Void run () throws IOException {
                                    CompilationInfo compilationInfo = SourceAccessor.INSTANCE.getCurrentCompilationInfo (js, Phase.RESOLVED);
                                    if (compilationInfo != null) {
                                        //Not cancelled
                                        final SourceAnalyser sa = getSourceAnalyser();
                                        long st = System.currentTimeMillis();
                                        sa.analyseUnitAndStore(compilationInfo.getParserResult(), SourceAccessor.INSTANCE.getParserTask(compilationInfo));
                                        long et = System.currentTimeMillis();
                                    }
                                    return null;
                                }
                        });                                        
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
                else {
                    try {
                        js.runUserActionTask(new CancellableTask<CompilationController>() {
                            public void run (final CompilationController controller) {
                                try {                            
                                    ClassIndexManager.getDefault().writeLock(
                                        new ClassIndexManager.ExceptionAction<Void>() {
                                            public Void run () throws IOException {
                                                controller.toPhase(Phase.RESOLVED);
                                                final SourceAnalyser sa = getSourceAnalyser();
                                                long st = System.currentTimeMillis();
                                                sa.analyseUnitAndStore(controller.getParserResult(), SourceAccessor.INSTANCE.getParserTask(controller));
                                                long et = System.currentTimeMillis();
                                                return null;
                                            }
                                    });
                                } catch (IOException ioe) {
                                    Exceptions.printStackTrace(ioe);
                                }
                            }

                            public void cancel () {}
                        }, true);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
                synchronized (this) {
                    this.dirty = null;
                }
                final long endTime = System.currentTimeMillis();
                LOGGER.fine("PersistentClassIndex.updateDirty took: " + (endTime-startTime)+ " ms");     //NOI18N
            }
        }
    }
        
    // BEGIN TOR MODIFICATIONS
    public void gsfSearch(final String primaryField, final String name, final NameKind kind, 
            final Set<ClassIndex.SearchScope> scope, final Set<SearchResult> result) throws IOException {
        updateDirty();
        try {
            ClassIndexManager.getDefault().readLock(new ClassIndexManager.ExceptionAction<Void> () {
                public Void run () throws IOException {
                    index.gsfSearch(primaryField, name, kind, scope, result);
                    return null;
                }                    
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
    
    /** For development purposes only */
    public File getSegment() {
        return cacheRoot;
    }
    
    private File cacheRoot;
    
    public URL getRoot() {
        return root;
    }
    
    // For the symbol dumper only
    public org.apache.lucene.index.IndexReader getDumpIndexReader() throws IOException {
        if (index instanceof LuceneIndex) {
            try {
                return ((LuceneIndex)index).getDumpIndexReader();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        
        return null;
    }
// END TOR MODIFICATIONS
    
}
