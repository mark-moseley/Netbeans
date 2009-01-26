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

package org.netbeans.modules.csl.core;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.IndexSearcher;
import org.netbeans.modules.csl.navigation.Icons;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.netbeans.spi.jumpto.symbol.SymbolProvider;
import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.netbeans.spi.jumpto.type.TypeProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author vita
 */
public class TypeAndSymbolProvider {

    public String name() {
        return typeProvider ? "CSL-TypeProvider" : "CSL-SymbolProvider"; //NOI18N
    }

    public String getDisplayName() {
        return GsfTaskProvider.getAllLanguageNames();
    }

    public void cancel() {
        synchronized (this) {
            cancelled = true;
        }
    }

    public void cleanup() {
//        synchronized (this) {
//            cachedRoots = null;
//            cachedRootsProjectRef = null;
//        }
    }

    public static final class TypeProviderImpl extends TypeAndSymbolProvider implements TypeProvider {
        public TypeProviderImpl() {
            super(true);
        }

        public void computeTypeNames(Context context, Result result) {
            Set<? extends IndexSearcher.Descriptor> descriptors = compute(
                    context.getText(),
                    context.getSearchType(),
                    context.getProject()
            );

            if (descriptors != null) {
                for(IndexSearcher.Descriptor d : descriptors) {
                    result.addResult(new TypeWrapper(d));
                }
            }
        }
    } // End of TypeProviderProxy class

    public static final class SymbolProviderImpl extends TypeAndSymbolProvider implements SymbolProvider {
        public SymbolProviderImpl() {
            super(false);
        }
        
        public void computeSymbolNames(Context context, Result result) {
            Set<? extends IndexSearcher.Descriptor> descriptors = compute(
                    context.getText(),
                    context.getSearchType(),
                    context.getProject()
            );

            if (descriptors != null) {
                for(IndexSearcher.Descriptor d : descriptors) {
                    result.addResult(new SymbolWrapper(d));
                }
            }
        }
    } // End of SymbolProviderProxy class
    // ------------------------------------------------------------------------
    // Private implementation
    // ------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(TypeAndSymbolProvider.class.getName());
    private static final IndexSearcher.Helper HELPER = new IndexSearcher.Helper() {
        public Icon getIcon(ElementHandle element) {
            return Icons.getElementIcon(element.getKind(), element.getModifiers());
        }

        public void open(FileObject fileObject, ElementHandle element) {
            Source js = Source.create(fileObject);
            if (js != null) {
                UiUtils.open(js, element);
            }
        }
    };
    
    private final boolean typeProvider;

    private boolean cancelled;
//    private Map<Language, Collection<FileObject>> cachedRoots = null;
//    private Reference<Project> cachedRootsProjectRef = null;

    private TypeAndSymbolProvider(boolean typeProvider) {
        this.typeProvider = typeProvider;
    }

    protected final Set<? extends IndexSearcher.Descriptor> compute(String text, SearchType searchType, Project project) {
        resume();

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Looking for '" + text + "', searchType=" + searchType + ", project=" + project); //NOI18N
        }
        
        Set<IndexSearcher.Descriptor> results = new HashSet<IndexSearcher.Descriptor>();
        for(Language language : LanguageRegistry.getInstance()) {
            if (isCancelled()) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Search '" + text + "', searchType=" + searchType + ", project=" + project + " cancelled"); //NOI18N
                }
                return null;
            }
            
            IndexSearcher searcher = language.getIndexSearcher();
            if (searcher == null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("No IndexSearcher for " + language); //NOI18N
                }
                continue;
            }

            Set<? extends IndexSearcher.Descriptor> languageResults;
//            Collection<FileObject> searchRoots = getRoots(project, language);
//            if (LOG.isLoggable(Level.FINE)) {
//                if (typeProvider) {
//                    LOG.fine("Querying " + searcher + " for types in " + searchRoots); //NOI18N
//                } else {
//                    LOG.fine("Querying " + searcher + " for symbols in " + searchRoots); //NOI18N
//                }
//            }

            if (typeProvider) {
                languageResults = searcher.getTypes(project, text, t2t(searchType), HELPER);
            } else {
                languageResults = searcher.getSymbols(project, text, t2t(searchType), HELPER);
            }

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(searcher + " found " + languageResults); //NOI18N
            }
            
            if (languageResults != null) {
                results.addAll(languageResults);
            }
        }

        return results;
    }

    private void resume() {
        synchronized (this) {
            cancelled = false;
        }
    }

    private boolean isCancelled() {
        synchronized (this) {
            return cancelled;
        }
    }

//    private Collection<FileObject> getRoots(Project project, Language language) {
//        synchronized (this) {
//            if (cachedRoots != null) {
//                Project cachedRootsProject = cachedRootsProjectRef != null ? cachedRootsProjectRef.get() : null;
//                if (cachedRootsProject == project) {
//                    Collection<FileObject> roots = cachedRoots.get(language);
//                    if (roots != null) {
//                        return roots;
//                    }
//                } else {
//                    cachedRoots = null;
//                    cachedRootsProjectRef = null;
//                }
//            }
//        }
//
//        Collection<FileObject> roots = GsfUtilities.getRoots(project, language.getSourcePathIds(), language.getBinaryPathIds());
//
//        synchronized (this) {
//            if (cancelled) {
//                return null;
//            }
//
//            if (cachedRoots == null) {
//                cachedRoots = new HashMap<Language, Collection<FileObject>>();
//                cachedRootsProjectRef = project == null ? null : new WeakReference<Project>(project);
//            }
//
//            cachedRoots.put(language, roots);
//            return roots;
//        }
//    }

    private static QuerySupport.Kind t2t(SearchType searchType) {
        switch(searchType) {
            case EXACT_NAME:
                return QuerySupport.Kind.EXACT;
            case PREFIX:
                return QuerySupport.Kind.PREFIX;
            case CASE_INSENSITIVE_PREFIX:
                return QuerySupport.Kind.CASE_INSENSITIVE_PREFIX;
            case REGEXP:
                return QuerySupport.Kind.REGEXP;
            case CASE_INSENSITIVE_REGEXP:
                return QuerySupport.Kind.CASE_INSENSITIVE_REGEXP;
            case CAMEL_CASE:
                return QuerySupport.Kind.CAMEL_CASE;
            default:
                throw new IllegalStateException("Can't translate " + searchType + " to QuerySupport.Kind"); //NOI18N
        }
    }

    private static final class TypeWrapper extends TypeDescriptor {

        private final IndexSearcher.Descriptor delegated;

        public TypeWrapper(IndexSearcher.Descriptor delegated) {
            this.delegated = delegated;
        }

        @Override
        public String getSimpleName() {
            return delegated.getSimpleName();
        }

        @Override
        public String getOuterName() {
            return delegated.getOuterName();
        }

        @Override
        public String getTypeName() {
            return delegated.getTypeName();
        }

        @Override
        public String getContextName() {
            String s = delegated.getContextName();
            if (s != null) {
                return " (" + s + ")"; //NOI18N
            } else {
                return null;
            }
        }

        @Override
        public Icon getIcon() {
            return delegated.getIcon();
        }

        @Override
        public String getProjectName() {
            return delegated.getProjectName();
        }

        @Override
        public Icon getProjectIcon() {
            return delegated.getProjectIcon();
        }

        @Override
        public FileObject getFileObject() {
            return delegated.getFileObject();
        }

        @Override
        public int getOffset() {
            return delegated.getOffset();
        }

        @Override
        public void open() {
            delegated.open();
        }
    } // End of TypeWrapper class

    private static final class SymbolWrapper extends SymbolDescriptor {
        private final IndexSearcher.Descriptor delegated;

        private SymbolWrapper(IndexSearcher.Descriptor delegated) {
            this.delegated = delegated;
        }

        @Override
        public Icon getIcon() {
            return delegated.getIcon();
        }

        @Override
        public String getProjectName() {
            return delegated.getProjectName();
        }

        @Override
        public Icon getProjectIcon() {
            return delegated.getProjectIcon();
        }

        @Override
        public FileObject getFileObject() {
            return delegated.getFileObject();
        }

        @Override
        public int getOffset() {
            return delegated.getOffset();
        }

        @Override
        public void open() {
            delegated.open();
        }

        @Override
        public String getSymbolName() {
            return delegated.getSimpleName();
        }

        @Override
        public String getOwnerName() {
            String owner = delegated.getContextName();
            if (owner == null) {
                owner = ""; //NOI18N
            }
            return owner;
        }
    } // End of SymbolWrapper class
}
