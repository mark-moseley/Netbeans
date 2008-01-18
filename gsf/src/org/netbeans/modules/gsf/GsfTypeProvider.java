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

package org.netbeans.modules.gsf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.api.gsf.Element;
import org.netbeans.api.gsf.Index;
import org.netbeans.api.gsf.NameKind;
import org.netbeans.api.gsf.TypeSearcher;
import org.netbeans.api.gsfpath.classpath.ClassPath;
import org.netbeans.api.gsfpath.queries.SourceForBinaryQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.napi.gsfret.source.ClasspathInfo;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.napi.gsfret.source.UiUtils;
import org.netbeans.modules.gsfret.navigation.Icons;
import org.netbeans.modules.gsfret.source.usages.ClassIndexManager;
import org.netbeans.modules.gsfret.source.usages.RepositoryUpdater;
import org.netbeans.spi.gsfpath.classpath.support.ClassPathSupport;
import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.netbeans.spi.jumpto.type.TypeProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Tor Norbye
 */
public class GsfTypeProvider implements TypeProvider, TypeSearcher.Helper {
    private static final Logger LOGGER = Logger.getLogger(GsfTypeProvider.class.getName());
    private static final ClassPath EMPTY_CLASSPATH = ClassPathSupport.createClassPath( new FileObject[0] );
    private Set<CacheItem> cache;
    private volatile boolean isCancelled = false;

    //@Override
    public void cleanup() {
        cache = null;
    }

    static class CacheItem {

        public final boolean isBinary;
        public final FileObject fileObject;
        public final ClasspathInfo classpathInfo;
        public String projectName;
        public Icon projectIcon;
        private ClassPath.Entry defEntry;
        
        public CacheItem ( FileObject fileObject, ClasspathInfo classpathInfo, boolean isBinary ) {
            this.isBinary = isBinary;
            this.fileObject = fileObject;
            this.classpathInfo = classpathInfo;
        }
        
//        Removed because of bad performance To reenable see diff between 1.15 and 1.16
//        
//        public ClassPath.Entry getDefiningEntry () {
//            if (defEntry == null) {
//                ClassPath defCp = ClassPath.getClassPath(fileObject, ClassPath.SOURCE);                    
//                if (defCp != null) {
//                    for (ClassPath.Entry e : defCp.entries()) {
//                        if (fileObject.equals(e.getRoot())) {
//                            defEntry = e;
//                            break;
//                        }
//                    }
//                }
//            }
//            return defEntry;
//        }
        
        @Override
        public int hashCode () {
            return this.fileObject == null ? 0 : this.fileObject.hashCode();
        }
        
        @Override
        public boolean equals (Object other) {
            if (other instanceof CacheItem) {
                CacheItem otherItem = (CacheItem) other;
                return this.fileObject == null ? otherItem.fileObject == null : this.fileObject.equals(otherItem.fileObject);
            }
            return false;
        }
    
        public FileObject getRoot() {
            return fileObject;
        }
        
        public boolean isBinary() {
            return isBinary;
        }
        
        public synchronized String getProjectName() {
            if (projectName == null) {
            try {
                java.net.URL url = fileObject.getURL();
                if (ClassIndexManager.getDefault().isBootRoot(url)) {
                    projectName = "Ruby Lib";
                }
            }
            catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
            }
            if ( !isBinary && projectName == null) {
                initProjectInfo();
            }
            return projectName;
        }
        
        public synchronized Icon getProjectIcon() {
            if ( !isBinary && projectIcon == null ) {
                initProjectInfo();
            }
            return projectIcon;
        }
        
        private void initProjectInfo() {
            Project p = FileOwnerQuery.getOwner(fileObject);                    
            if (p != null) {
                ProjectInformation pi = ProjectUtils.getInformation( p );
                projectName = pi.getDisplayName();
                projectIcon = pi.getIcon();
            }
        }
        
    }

    
    public GsfTypeProvider() {
    }
   
//    // This is essentially the code from OpenDeclAction
//    // TODO: Was OpenDeclAction used for anything else?
//    public void gotoType(TypeDescriptor type) {
//    //public void actionPerformed(ActionEvent e) {
//        Lookup lkp = WindowManager.getDefault().getRegistry().getActivated().getLookup();
//        DataObject activeFile = (DataObject) lkp.lookup(DataObject.class);
//        Element value = (Element) lkp.lookup(Element.class);
//        if (activeFile != null && value != null) {
//            JavaSource js = JavaSource.forFileObject(activeFile.getPrimaryFile());
//            if (js != null) {
//                ClasspathInfo cpInfo = js.getClasspathInfo();
//                assert cpInfo != null;
//                UiUtils.open(cpInfo,value);
//            }
//        }
//    }
    

    //@Override
       public void computeTypeNames(Context context, Result res) {
            String text = context.getText();
            SearchType nameKind = context.getSearchType();
        
            long time;
            
            long cp, gss, gsb, sfb, gtn, add, sort;
            cp = gss = gsb = sfb = gtn = add = sort = 0;
            
            if (cache == null) {
                LOGGER.fine("GoToTypeAction.getTypeNames recreates cache\n");
                // Sources
                time = System.currentTimeMillis();
                ClassPath scp = RepositoryUpdater.getDefault().getScannedSources();
                FileObject roots[] = scp.getRoots();
                gss += System.currentTimeMillis() - time; 
                FileObject root[] = new FileObject[1];
                Set<CacheItem> sources = new HashSet<CacheItem>( roots.length );
                for (int i = 0; i < roots.length; i++ ) {                    
                    root[0] = roots[i];
                    time = System.currentTimeMillis();                
                    ClasspathInfo ci = ClasspathInfo.create( EMPTY_CLASSPATH, EMPTY_CLASSPATH, ClassPathSupport.createClassPath(root));               //create(roots[i]);
                    LOGGER.fine("GoToTypeAction.getTypeNames created ClasspathInfo for source: " + FileUtil.getFileDisplayName(roots[i])+"\n");
//                    if ( isCanceled ) {
                    if ( isCancelled ) {
                        return;
                    }
                    else {
                        sources.add( new CacheItem( roots[i], ci, false ) );
                    }                        
                    cp += System.currentTimeMillis() - time;
                }
                     
                
                
                // Binaries
                time = System.currentTimeMillis();                
                scp = RepositoryUpdater.getDefault().getScannedBinaries();
                roots = scp.getRoots(); 
                gsb += System.currentTimeMillis() - time;
                root = new FileObject[1];
                for (int i = 0; i < roots.length; i++ ) {
                    try {
                        time = System.currentTimeMillis();
                        SourceForBinaryQuery.Result result = SourceForBinaryQuery.findSourceRoots(roots[i].getURL());
                        if ( result.getRoots().length == 0 ) {
                            continue;
                        }       
                        sfb += System.currentTimeMillis() - time;                        
                        time = System.currentTimeMillis();                        
                        root[0] = roots[i];
                        ClasspathInfo ci = ClasspathInfo.create(ClassPathSupport.createClassPath(root), EMPTY_CLASSPATH, EMPTY_CLASSPATH );//create(roots[i]);                                
                        LOGGER.fine("GoToTypeAction.getTypeNames created ClasspathInfo for binary: " + FileUtil.getFileDisplayName(roots[i])+"\n");
                        sources.add( new CacheItem( roots[i], ci, true ) );                                                
                        
                        cp += System.currentTimeMillis() - time;
                    }
                    catch ( FileStateInvalidException e ) {
                        continue;
                    }                        
                }
//                if ( !isCanceled ) {
                if ( !isCancelled ) {
                    cache = sources;
                }
                else {
                    return;
                }
                
            }
            LOGGER.fine("GoToTypeAction.getTypeNames collected : " + cache.size() +" elements\n");
            
            //ArrayList<GsfTypeDescription> types = new ArrayList<GsfTypeDescription>(cache.size() * 20);
            ArrayList<TypeDescriptor> types = new ArrayList<TypeDescriptor>(cache.size() * 20);

            NameKind indexNameKind;
            switch (nameKind) {
            case CAMEL_CASE: indexNameKind = NameKind.CAMEL_CASE; break;
            case CASE_INSENSITIVE_PREFIX: indexNameKind = NameKind.CASE_INSENSITIVE_PREFIX; break;
            case CASE_INSENSITIVE_REGEXP: indexNameKind = NameKind.CASE_INSENSITIVE_REGEXP; break;
            case PREFIX: indexNameKind = NameKind.PREFIX; break;
            case REGEXP: indexNameKind = NameKind.REGEXP; break;
            case EXACT_NAME: indexNameKind = NameKind.EXACT_NAME; break;
	    case CASE_INSENSITIVE_EXACT_NAME: indexNameKind = NameKind.EXACT_NAME; break;
            default: throw new RuntimeException("Unexpected name kind: " + nameKind);
            }
            
            for( CacheItem ci : cache ) {    
                time = System.currentTimeMillis();
                
                String textForQuery;
                switch( nameKind ) {
                    case REGEXP:
                    case CASE_INSENSITIVE_REGEXP:
                        String pattern = text + "*"; // NOI18N
                        pattern = pattern.replace( "*", ".*" ).replace( '?', '.' );
                        textForQuery = pattern;
                        break;
                    default:
                        textForQuery = text;
                }
                LOGGER.fine("GoToTypeAction.getTypeNames queries usages of: " + ci.classpathInfo+"\n");
                
                Index index = ci.classpathInfo.getClassIndex();
                
                //Set<? extends Element/*Handle<Element>*/> names = getTypes(index, textForQuery, indexNameKind,  EnumSet.of(ci.isBinary ? Index.SearchScope.DEPENDENCIES : Index.SearchScope.SOURCE ));
                Set<? extends TypeDescriptor> names = getTypes(index, textForQuery, indexNameKind,  EnumSet.of(ci.isBinary ? Index.SearchScope.DEPENDENCIES : Index.SearchScope.SOURCE ));
                //Set<ElementHandle<TypeElement>> names = ci.classpathInfo.getClassIndex().getDeclaredTypes(textForQuery, indexNameKind, EnumSet.of( ci.isBinary ? ClassIndex.SearchScope.DEPENDENCIES : ClassIndex.SearchScope.SOURCE ));
//                if ( isCanceled ) {
                if ( isCancelled ) {
                    return;
                }
                
                gtn += System.currentTimeMillis() - time;            
                time = System.currentTimeMillis();
                
//              Removed because of bad performance To reenable see diff between 1.15 and 1.16
//              ClassPath.Entry defEntry = ci.getDefiningEntry();
                //for (Element/*Handle<Element>*/ name : names) {
                for (TypeDescriptor td : names) {
//                    Removed because of bad performance To reenable see diff between 1.15 and 1.16
//                    if (defEntry.includes(convertToSourceName(name.getBinaryName()))) {
                        //GsfTypeDescription td = new GsfTypeDescription(ci, name );
                        types.add(td);
//                    }
//                    if ( isCanceled ) {
                    if ( isCancelled ) {
                        return;
                    }
                }
                add += System.currentTimeMillis() - time;
            }
            
            if ( !isCancelled ) {
                time = System.currentTimeMillis();
                // Sorting is now done on the Go To Tpe dialog side
                // Collections.sort(types);
                sort += System.currentTimeMillis() - time;
                LOGGER.fine("PERF - " + " GSS:  " + gss + " GSB " + gsb + " CP: " + cp + " SFB: " + sfb + " GTN: " + gtn + "  ADD: " + add + "  SORT: " + sort ); 
                res.addResult(types);
            }
        }

       private Collection<? extends TypeSearcher> searchers;

  //      private Set<? extends /*ElementHandle<*/Element/*>*/> getTypes(Index index, String textForQuery, NameKind kind, EnumSet<Index.SearchScope> scope) {
        private Set<? extends TypeDescriptor> getTypes(Index index, String textForQuery, NameKind kind, EnumSet<Index.SearchScope> scope) {
            if (searchers == null) {
                // XXX Will this do a newInstance every time? That will break my caching...
                searchers = Lookup.getDefault().lookupAll(TypeSearcher.class);
            }

            if (searchers != null) {
//                if (searchers.size() == 1) {
//                    return searchers.iterator().next().getDeclaredTypes(index, textForQuery, kind, scope);
//                } else {
//                    Set<? extends Element/*Handle<Element>*/> items = new HashSet<Element/*Handle<Element>*/>();
//                    for (TypeSearcher searcher : searchers) {
//                        Set<? extends /*ElementHandle<*/Element/*>*/> set = searcher.getDeclaredTypes(index, textForQuery, kind, scope);
//                        Set s = items;
//                        s.addAll(set);
//                    }
//
//                    return items;
//                }
                if (searchers.size() == 1) {
                    return searchers.iterator().next().getDeclaredTypes(index, textForQuery, kind, scope, this);
                } else {
                    Set<? extends TypeDescriptor> items = new HashSet<TypeDescriptor>();
                    for (TypeSearcher searcher : searchers) {
                        Set<? extends TypeDescriptor> set = searcher.getDeclaredTypes(index, textForQuery, kind, scope, this);
                        Set s = items;
                        s.addAll(set);
                    }

                    return items;
                }
            } else {
                return Collections.emptySet();
            }
        }

    public Icon getIcon(Element element) {
        return Icons.getElementIcon(element.getKind(), element.getModifiers());
    }

    public void open(FileObject fileObject, Element element) {
        Source js = Source.forFileObject(fileObject);
        UiUtils.open(js, element);
    }

    public String name() {
        return "ruby"; // NOI18N
    }

    public String getDisplayName() {
        // TODO - i18n
        return "Ruby Classes";
    }

    public void cancel() {
        isCancelled = true;
    }

}
