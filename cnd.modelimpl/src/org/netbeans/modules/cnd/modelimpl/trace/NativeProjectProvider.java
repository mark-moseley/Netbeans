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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectItemsListener;
import org.netbeans.modules.cnd.loaders.CCDataLoader;
import org.netbeans.modules.cnd.loaders.CCDataObject;
import org.netbeans.modules.cnd.loaders.CDataLoader;
import org.netbeans.modules.cnd.loaders.CDataObject;
import org.netbeans.modules.cnd.loaders.CndDataObject;
import org.netbeans.modules.cnd.loaders.HDataLoader;
import org.netbeans.modules.cnd.loaders.HDataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * 
 * @author vv159170
 */
public final class NativeProjectProvider {
    
    /** Creates a new instance of NativeProjectProvider */
    private NativeProjectProvider() {
    }
    
    public static NativeProject createProject(String projectRoot, List<File> files,
	    List<String> sysIncludes, List<String> usrIncludes,
	    List<String> sysMacros, List<String> usrMacros, boolean pathsRelCurFile) {
	
        NativeProjectImpl project = new NativeProjectImpl(projectRoot, 
		sysIncludes, usrIncludes, sysMacros, usrMacros, pathsRelCurFile);
	
	project.addFiles(files);
	
        return project;
    }
    
    public static void fireAllFilesChanged(NativeProject nativeProject) {
	if( nativeProject instanceof NativeProjectImpl) {
	    ((NativeProjectImpl) nativeProject).fireAllFilesChanged();
	}
    }
    
    private static final class NativeProjectImpl implements NativeProject {
	
	private final List<String> sysIncludes;
	private final List<String> usrIncludes;
	private final List<String> sysMacros;
	private final List<String> usrMacros;
	    
        private final List<NativeFileItem> headers = new ArrayList<NativeFileItem>();
        private final List<NativeFileItem> sources = new ArrayList<NativeFileItem>();
	
        private final String projectRoot;
	private boolean pathsRelCurFile;
	
	private List<NativeProjectItemsListener> listeners = new ArrayList<NativeProjectItemsListener>();
	private Object listenersLock = new Object();

	public NativeProjectImpl(String projectRoot,
		List<String> sysIncludes, List<String> usrIncludes, 
		List<String> sysMacros, List<String> usrMacros) {
	    
	    this(projectRoot, sysIncludes, usrIncludes, sysMacros, usrMacros, false);
	}
	
	public NativeProjectImpl(String projectRoot,
		List<String> sysIncludes, List<String> usrIncludes, 
		List<String> sysMacros, List<String> usrMacros,
		boolean pathsRelCurFile) {

	    this.projectRoot = projectRoot;
	    this.pathsRelCurFile = pathsRelCurFile;
	    
	    this.sysIncludes = createIncludes(sysIncludes);
	    this.usrIncludes = createIncludes(usrIncludes);
	    this.sysMacros = new ArrayList<String>(sysMacros);
	    this.usrMacros = new ArrayList<String>(usrMacros);
	}
	
	private List<String> createIncludes(List<String> src) {
	    if( pathsRelCurFile ) {
		return new ArrayList<String>(src);
	    }
	    else {
		List<String> result = new ArrayList<String>(src.size());
		for( String path : src ) {
		    File file = new File(path);
		    result.add(file.getAbsolutePath());
		}
		return result;
	    }
	}
	
	private void addFiles(List<File> files) {
	    for( File file : files ) {
		addFile(file);
	    }
	}
	
        public Object getProject() {
            return null;
        }
                
        public String getProjectRoot() {
            return this.projectRoot;
        }

        public String getProjectDisplayName() {
            return getProjectRoot();
        }

        public List<NativeFileItem> getAllSourceFiles() {
            return Collections.unmodifiableList(this.sources);
        }

        public List<NativeFileItem> getAllHeaderFiles() {
            return Collections.unmodifiableList(this.headers);
        }

        public void addProjectItemsListener(NativeProjectItemsListener listener) {
            synchronized( listenersLock ) {
		listeners.add(listener);
	    }
        }

        public void removeProjectItemsListener(NativeProjectItemsListener listener) {
            synchronized( listenersLock ) {
		listeners.remove(listener);
	    }
        }
	
	private void fireAllFilesChanged() {
	    List<NativeProjectItemsListener> listenersCopy;
	    synchronized( listenersLock ) {
		listenersCopy = new ArrayList<NativeProjectItemsListener>(listeners);
	    }
	    List<NativeFileItem> items = new ArrayList<NativeFileItem>(sources);
	    items.addAll(headers);
	    for( NativeProjectItemsListener listener : listenersCopy ) {
		listener.filesPropertiesChanged(items);
	    }
	}

        public NativeFileItem findFileItem(File file) {
            NativeFileItem item = findItem(file, sources);
            return item == null ? findItem(file, headers) : item;
        }

        public List<String> getSystemIncludePaths() {
            return this.sysIncludes;
        }

        public List<String> getUserIncludePaths() {
            return this.usrIncludes;
        }

        public List<String> getSystemMacroDefinitions() {
            return this.sysMacros;
        }

        public List<String> getUserMacroDefinitions() {
            return this.usrMacros;
        }
        
        private NativeFileItem findItem(File file, List<NativeFileItem> col) {
            String path = file.getAbsolutePath();
            for (NativeFileItem item : col) {
                if (item.getFile().getAbsolutePath().equalsIgnoreCase(path)) {
                    return item;
                }
            }
            return null;
        }
        
	private void addFile(File file) {
            DataObject dobj = getDataObject(file);
	    NativeFileItem.Language lang = getLanguage(file, dobj);
	    NativeFileItem item = new NativeFileItemImpl(file, this, lang);
	    //TODO: put item in loockup of DataObject
            // registerItemInDataObject(dobj, item);
            List<NativeFileItem> data = (item.getLanguage() == NativeFileItem.Language.C_HEADER) ? this.headers : this.sources;
	    data.add(item);
	}
	
	NativeFileItem.Language getLanguage(File file, DataObject dobj) {
	    if (dobj == null) {
		String path = file.getAbsolutePath();
		if (CCDataLoader.getInstance().getExtensions().isRegistered(path)) {
		    return NativeFileItem.Language.CPP;
		} else if (CDataLoader.getInstance().getExtensions().isRegistered(path)) {
		    return NativeFileItem.Language.C;
		} else if (HDataLoader.getInstance().getExtensions().isRegistered(path)) {
		    return NativeFileItem.Language.C_HEADER;
		} else {
		    return NativeFileItem.Language.OTHER;
		}
	    } else if (dobj instanceof CCDataObject) {
		return NativeFileItem.Language.CPP;
	    } else if (dobj instanceof HDataObject) {
		return NativeFileItem.Language.C_HEADER;
	    } else if (dobj instanceof CDataObject) {
		return NativeFileItem.Language.C;
	    } else {
		return NativeFileItem.Language.OTHER;
	    }
	}
	
//        /*package*/ void addHeaders(List<String> files) {
//            addFiles(files, this.sources, NativeFileItem.Language.C_HEADER);
//        }
//        
//        /*package*/ void addSources(List<String> files) {
//            addFiles(files, this.sources, NativeFileItem.Language.CPP);
//        }
//        
//        private void addFiles(List<String> files, List<NativeFileItem> dest, NativeFileItem.Language lang) {
//            for (String path : files) {
//                NativeFileItem item = new NativeFileItemImpl(path, this, lang);
//                dest.add(item);
//            }
//        }

        public List<NativeProject> getDependences() {
            return Collections.<NativeProject>emptyList();
        }
    }    


    private static DataObject getDataObject(File file) {

        DataObject dobj = null;
        try {
            FileObject fo = FileUtil.toFileObject(file.getCanonicalFile());
            if (fo != null) {
                try {
                    dobj = DataObject.find(fo);
                } catch (DataObjectNotFoundException ex) {
                    // skip;
                }
            }
        } catch (IOException ioe) {
            // skip;
        }

        return dobj;
    }
        
    private static void registerItemInDataObject(DataObject obj, NativeFileItem item) {
        if (obj != null) {
            NativeFileItemSet set = obj.getLookup().lookup(NativeFileItemSet.class);
            if (set == null) {
                set = new NativeFileItemSetImpl();
                if (obj instanceof CndDataObject) {
                    ((CndDataObject)obj).addCookie(set);
                }
            }
            set.add(item);
        }
    }
    
    private static class NativeFileItemSetImpl extends HashSet<NativeFileItem> implements NativeFileItemSet {
    }
    
    private static final class NativeFileItemImpl implements NativeFileItem {
	
        private final File file;
        private final NativeProjectImpl project;
        private final NativeFileItem.Language lang;

        public NativeFileItemImpl(File file, NativeProjectImpl project, NativeFileItem.Language language) {
	    
            this.project = project;
            this.file = file;
            this.lang = language;
        }
        
        public NativeProject getNativeProject() {
            return project;
        }

        public File getFile() {
            return file;
        }

        public List<String> getSystemIncludePaths() {
	    List<String> result = project.getSystemIncludePaths();
	    return project.pathsRelCurFile ? toAbsolute(result) : result;
        }

        public List<String> getUserIncludePaths() {
	    List<String> result = project.getUserIncludePaths();
            return project.pathsRelCurFile ? toAbsolute(result) : result;
        }
	
	private List<String> toAbsolute(List<String> orig) {
	    File base = file.getParentFile();
	    List<String> result = new ArrayList<String>(orig.size());
	    for( String path : orig ) {
		File pathFile = new File(path);
		if( pathFile.isAbsolute() ) {
		    result.add(path);
		}
		else {
		    pathFile = new File(base, path);
		    result.add(pathFile.getAbsolutePath());
		}
	    }
	    return result;
	}

        public List<String> getSystemMacroDefinitions() {
            return project.getSystemMacroDefinitions();
        }

        public List<String> getUserMacroDefinitions() {
            return project.getUserMacroDefinitions();
        }

        public NativeFileItem.Language getLanguage() {
            return lang;
        }

        public NativeFileItem.LanguageFlavor getLanguageFlavor() {
            return NativeFileItem.LanguageFlavor.GENERIC;
        }

        public boolean isExcluded() {
            return false;
        }
    }
}
