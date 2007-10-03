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
package org.netbeans.modules.j2ee.ejbjarproject.classpath;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.Iterator;
import java.util.StringTokenizer;
import org.netbeans.modules.j2ee.ejbjarproject.SourceRoots;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * Implementation of a single classpath that is derived from one Ant property.
 */
final class SourcePathImplementation implements ClassPathImplementation, PropertyChangeListener {
    private static final String DIR_GEN_BINDINGS = "generated/addons"; // NOI18N
    private static RequestProcessor REQ_PROCESSOR = new RequestProcessor(); // No I18N

    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    private List resources;
    private SourceRoots sourceRoots;
    private AntProjectHelper projectHelper;
    private FileChangeListener fcl = null;
    
    /**
     * Construct the implementation.
     * @param sourceRoots used to get the roots information and events
     */
    public SourcePathImplementation(SourceRoots sourceRoots, AntProjectHelper projectHelper) {
        assert sourceRoots != null;
        this.sourceRoots = sourceRoots;
        this.projectHelper=projectHelper;
        this.sourceRoots.addPropertyChangeListener (this);
    }

    private synchronized void createAddOnGenSrcRootsListener(String buildDir, String[] paths){
        if (this.fcl == null){
            // Need to keep reference to fcl.
            // See JavaDoc for org.openide.util.WeakListeners
            FileObject prjFo = this.projectHelper.getProjectDirectory();                        
            this.fcl = new AddOnGeneratedSourceRootListener(prjFo, buildDir, 
                    paths);
            ((AddOnGeneratedSourceRootListener)this.fcl).listenToProjRoot();            
        }
    }
    
    private List<PathResourceImplementation> getAddOnGeneratedSrcRoots(String buildDir, String[] paths){
        List<PathResourceImplementation> ret = 
                new ArrayList<PathResourceImplementation>();
        
        File buidDirFile = projectHelper.resolveFile(buildDir);
        for (String path: paths){
            File genAddOns = new File(buidDirFile, path);
            if (genAddOns.exists() && genAddOns.isDirectory()){
                File[] subDirs = genAddOns.listFiles();
                for (File subDir: subDirs){
                    try {
                        URL url = subDir.toURI().toURL();
                        if (!subDir.exists()) { 
                            assert !url.toExternalForm().endsWith("/"); //NOI18N
                            url = new URL (url.toExternalForm()+'/');   //NOI18N
                        }
                        ret.add(ClassPathSupport.createResource(url));
                    } catch (MalformedURLException ex) {
                            Exceptions.printStackTrace(ex);
                    }                
                }
            } 
        }
        return ret;
    }

    private void invalidate() {
        synchronized (this) {
            this.resources = null;
        }
        this.support.firePropertyChange (PROP_RESOURCES,null,null);        
    } 
    
    public List /*<PathResourceImplementation>*/ getResources() {
        synchronized (this) {
            if (this.resources != null) {
                return this.resources;
            }
        }        
        URL[] roots = this.sourceRoots.getRootURLs();                                
        String buildDir = projectHelper.getStandardPropertyEvaluator().getProperty(EjbJarProjectProperties.BUILD_DIR);
        synchronized (this) {
            if (this.resources == null) {
                List result = new ArrayList (roots.length);
                for (int i = 0; i < roots.length; i++) {
                    PathResourceImplementation res = ClassPathSupport.createResource(roots[i]);
                    result.add (res);
                }
                // adds build/generated/wsimport/client and build/generated/wsimport/service to resources to be available for code completion
                if (projectHelper!=null) {
                    try {
                        if (buildDir!=null) {
                            // generated/wsimport/client
                            File f = new File (projectHelper.resolveFile(buildDir),"generated/wsimport/client"); //NOI18N
                            URL url = f.toURI().toURL();
                            if (!f.exists()) {  //NOI18N
                                assert !url.toExternalForm().endsWith("/");  //NOI18N
                                url = new URL (url.toExternalForm()+'/');   //NOI18N
                            }
                            result.add(ClassPathSupport.createResource(url));
                            // generated/wsimport/service
                            f = new File (projectHelper.resolveFile(buildDir),"generated/wsimport/service"); //NOI18N
                            url = f.toURI().toURL();
                            if (!f.exists()) {  //NOI18N
                                assert !url.toExternalForm().endsWith("/");  //NOI18N
                                url = new URL (url.toExternalForm()+'/');   //NOI18N
                            }
                            result.add(ClassPathSupport.createResource(url));
                            
                            // generated/addons/<subDirs>
                            result.addAll(getAddOnGeneratedSrcRoots(buildDir, 
                                    new String[] {DIR_GEN_BINDINGS}));
                            // Listen for any new Source root creation.
                            createAddOnGenSrcRootsListener(buildDir, 
                                    new String[] {DIR_GEN_BINDINGS});                            
                        }
                    } catch (MalformedURLException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                this.resources = Collections.unmodifiableList(result);
            }
        }
        return this.resources;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener (listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener (listener);
    }


    public void propertyChange(PropertyChangeEvent evt) {
        if (SourceRoots.PROP_ROOTS.equals (evt.getPropertyName())) {
            invalidate();
        }
    }

    /**
     * Thread/task to check newly created source root for each File/Folder create event. 
     **/
    private static class SourceRootScannerTask implements Runnable {
        SourcePathImplementation spi = null;
        FileChangeListener fcl = null;
        List<List<String>> paths = null;
        FileObject parent = null;
        FileObject child = null;
        List<String> listenerAddedDirs = new ArrayList<String>();
        
        public SourceRootScannerTask(SourcePathImplementation s, 
                FileChangeListener origFcl, List<List<String>> pths, 
                FileObject parent, FileObject child){
            this.spi = s;
            this.fcl = origFcl;
            this.paths = pths;
            this.parent = parent;
            this.child = child;   
        }

        private void firePropertyChange(){
            spi.invalidate();
        }
        
        private void addListeners(List<String> path, int cIndx){
            int size = path.size();
            FileObject currParent = this.parent;
            FileObject curr = this.child;
            String relDir = null;
            FileChangeListener weakFcl = null;
            for (int i=cIndx; i < size; i++){
                curr = currParent.getFileObject(path.get(i));
                if ((curr != null) && (curr.isFolder())){
                    relDir = FileUtil.getRelativePath(this.parent, curr);
                    if (! this.listenerAddedDirs.contains(relDir)){
                        this.listenerAddedDirs.add(relDir);
                        weakFcl = FileUtil.weakFileChangeListener(
                                this.fcl, curr);
                        curr.addFileChangeListener(weakFcl);                        
                    }
                    
                    if (i == (size -1)){
                        if (curr.getChildren().length > 0){
                            firePropertyChange();
                        }
                        break;
                    }

                    currParent = curr;
                } else {
                    break;
                }
            }
        }
        
        public void run() {
            Iterator<List<String>> itr = paths.iterator();
            List<String> path = null;
            int cIndx = -1;
            int pIndx = -1;
            boolean lastElem = false;

            while(itr.hasNext()){
                path = itr.next();
                cIndx = path.indexOf(child.getName());
                pIndx = path.indexOf(parent.getName());

                lastElem = ((pIndx + 1) == path.size()) ? true: false ;
                
                if (lastElem){
                    if (cIndx == -1){
                        firePropertyChange();                                            
                    }
                } else{
                    if ((cIndx != -1) && (pIndx == (cIndx - 1))){
                        // Add listener and fire change event if leaf directory 
                        // is created.
                        addListeners(path, cIndx);
                    }
                }                
            }
        }    
    }
    
    private class AddOnGeneratedSourceRootListener extends FileChangeAdapter {
        // Path is relative to project root, starting with project specific
        // build directory.
        private List<List<String>> paths = 
                Collections.synchronizedList(new ArrayList<List<String>>());
        private FileObject projRoot;
        
        AddOnGeneratedSourceRootListener(FileObject pr, String bd, String[] addOnPaths){
            this.projRoot = pr;  
            StringTokenizer stk = null;
            List<String> pathElems = null;
            for (String path : addOnPaths){
                stk = new StringTokenizer(path, "/"); // No I18N
                pathElems = new ArrayList<String>();
                pathElems.add(bd);
                while(stk.hasMoreTokens()){
                    pathElems.add(stk.nextToken());
                }
                this.paths.add(pathElems);                
            }
        }
                
        /**
         * Listen to all the folders from ProjectRoot, build  upto any existing
         * addons dirs.
         **/
        public synchronized void listenToProjRoot(){
            List<String> dirsAdded = new ArrayList<String>();
            String relativePath = null;
            FileObject fo = this.projRoot;
            FileChangeListener weakFcl = 
                    FileUtil.weakFileChangeListener(this, fo);
            fo.addFileChangeListener(weakFcl);  
            FileObject parent = null;
            FileObject child = null;
            for (List<String> path: paths){
                parent = fo;
                for(String pathElem: path){
                    child = parent.getFileObject(pathElem);
                    if (child != null){
                        relativePath = FileUtil.getRelativePath(fo, child);                        
                        if (!dirsAdded.contains(relativePath)){
                            dirsAdded.add(relativePath);
                            weakFcl = FileUtil.weakFileChangeListener(
                                    this, child);
                            child.addFileChangeListener(weakFcl);
                            parent = child;                            
                        }
                    } else {
                        // No need to check further down.
                        break;
                    }
                }
            }
        }
        
        @Override
        public void fileFolderCreated(FileEvent fe) {
            synchronized (this){
                SourceRootScannerTask task = new SourceRootScannerTask(
                        SourcePathImplementation.this,
                        this,
                        this.paths,
                        (FileObject)fe.getSource(),
                        fe.getFile());
                SourcePathImplementation.REQ_PROCESSOR.post(task);
            }
        }                
    }    
}
