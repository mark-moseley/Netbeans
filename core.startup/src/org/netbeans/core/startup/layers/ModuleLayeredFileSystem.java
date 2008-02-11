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

package org.netbeans.core.startup.layers;

import java.beans.PropertyVetoException;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.Stamps;
import org.netbeans.core.startup.StartLog;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;

/** Layered file system serving itself as either the user or installation layer.
 * Holds one layer of a writable system directory, and some number
 * of module layers.
 * @author Jesse Glick, Jaroslav Tulach
 */
public class ModuleLayeredFileSystem extends MultiFileSystem 
implements LookupListener {
    /** serial version UID */
    private static final long serialVersionUID = 782910986724201983L;
    
    private static final String LAYER_STAMP = "layer-stamp.txt";
    
    static final Logger err = Logger.getLogger("org.netbeans.core.projects"); // NOI18N

    /** lookup result we listen on */
    private static Lookup.Result<FileSystem> result = Lookup.getDefault().lookupResult(FileSystem.class);
    
    /** current list of URLs - r/o; or null if not yet set */
    private List<URL> urls;
    /** cache manager */
    private LayerCacheManager manager;
    /** writable layer */
    private final FileSystem writableLayer;
    /** cache layer */
    private FileSystem cacheLayer;
    /** other layers */
    private final FileSystem[] otherLayers;
    /** addLookup */
    private final boolean addLookup;

    /** Create layered filesystem based on a supplied writable layer.
     * @param userDir is this layer for modules from userdir or not?
     * @param writableLayer the writable layer to use, typically a LocalFileSystem
     * @param otherLayers some other layers to use, e.g. LocalFileSystem[]
     * @param cacheDir a directory in which to store a cache, or null for no caching
     */
    ModuleLayeredFileSystem (FileSystem writableLayer, boolean userDir, FileSystem[] otherLayers, boolean mgr) throws IOException {
        this(writableLayer, userDir, otherLayers, LayerCacheManager.manager(mgr));
    }
    
    private ModuleLayeredFileSystem(FileSystem writableLayer, boolean addLookup, FileSystem[] otherLayers, LayerCacheManager mgr) throws IOException {
        this(writableLayer, addLookup, otherLayers, mgr, loadCache(mgr));
    }
    
    private ModuleLayeredFileSystem(FileSystem writableLayer, boolean addLookup, FileSystem[] otherLayers, LayerCacheManager mgr, FileSystem cacheLayer) throws IOException {
        super(appendLayers(writableLayer, addLookup, otherLayers, cacheLayer));
        this.manager = mgr;
        this.writableLayer = writableLayer;
        this.otherLayers = otherLayers;
        this.cacheLayer = cacheLayer;
        this.addLookup = addLookup;
        
        // Wish to permit e.g. a user-installed module to mask files from a
        // root-installed module, so propagate masks up this high.
        // SystemFileSystem leaves this off, so that the final file system
        // will not show them if there are some left over.
        setPropagateMasks (true);
        
        urls = null;

        if (addLookup) {
            result.addLookupListener(this);
            result.allItems();
        }
        
    }
    
    private static FileSystem loadCache(LayerCacheManager mgr) throws IOException {
        String location = mgr.cacheLocation();
        FileSystem fs = null;
        
        if (location != null) {
            setStatusText(NbBundle.getMessage(ModuleLayeredFileSystem.class, "MSG_start_load_cache"));

            ByteBuffer bb = Stamps.getModulesJARs().asMappedByteBuffer(location);
            if (bb != null) {
                StartLog.logStart("Loading layers"); // NOI18N
                fs = mgr.load(mgr.createEmptyFileSystem(), bb);
                setStatusText(
                    NbBundle.getMessage(ModuleLayeredFileSystem.class, "MSG_end_load_cache"));
                StartLog.logEnd("Loading layers"); // NOI18N
            }
        }
        return fs != null ? fs : mgr.createEmptyFileSystem();
    }
    
    private static FileSystem[] appendLayers(FileSystem fs1, boolean addLookup, FileSystem[] fs2s, FileSystem fs3) {
        List<FileSystem> l = new ArrayList<FileSystem>(fs2s.length + 2);
        l.add(fs1);
        if (addLookup) {
            Collection<? extends FileSystem> fromLookup = result.allInstances();
            l.addAll(fromLookup);
        }
        l.addAll(Arrays.asList(fs2s));
        l.add(fs3);
        return l.toArray(new FileSystem[l.size()]);
    }

    /** Get all layers.
     * @return all filesystems making layers
     */
    public/*but just for debugging*/ final FileSystem[] getLayers () {
        return getDelegates ();
    }

    /** Get the writable layer.
     * @return the writable layer
     */
    final FileSystem getWritableLayer () {
        return writableLayer;
    }
    
    /** Get the installation layer.
     * You can take advantage of the specialized return type
     * if working within the core.
     */
    public static ModuleLayeredFileSystem getInstallationModuleLayer () {
        FileSystem fs = Repository.getDefault ().getDefaultFileSystem();
        SystemFileSystem sfs = (SystemFileSystem)fs;            
        ModuleLayeredFileSystem home = sfs.getInstallationLayer ();
        if (home != null)
            return home;
        else
            return sfs.getUserLayer ();
    }    
    
    /** Get the user layer.
     * You can take advantage of the specialized return type
     * if working within the core.
     */
    public static ModuleLayeredFileSystem getUserModuleLayer () {
        SystemFileSystem sfs = (SystemFileSystem)
            Repository.getDefault().getDefaultFileSystem();
        return sfs.getUserLayer ();
    }

    /** Change the list of module layers URLs.
     * @param urls the urls describing module layers to use. List<URL>
     */
    public void setURLs (final List<URL> urls) throws Exception {
        if (urls.contains(null)) throw new NullPointerException("urls=" + urls); // NOI18N
        if (err.isLoggable(Level.FINE)) {
            err.fine("setURLs: " + urls);
        }
        if (this.urls != null && urls.equals(this.urls)) {
            err.fine("no-op");
            return;
        }
        
        StartLog.logStart("setURLs"); // NOI18N

        class Updater implements AtomicAction, Stamps.Updater {
            private byte[] data;
            
            public void flushCaches(DataOutputStream os) throws IOException {
                err.log(Level.FINEST, "flushing layers");
                os.write(data);
                err.log(Level.FINEST, "layers flushed");
            }
            public void cacheReady() {
                try {
                    err.log(Level.FINEST, "cache is ready");
                    cacheLayer = loadCache(manager);
                    err.log(Level.FINEST, "update delegates for userdir:" + addLookup + " manager: " + manager);
                    setDelegates(appendLayers(writableLayer, addLookup, otherLayers, cacheLayer));
                    err.log(Level.FINEST, "delegates updated");
                } catch (IOException ex) {
                    err.log(Level.INFO, "Cannot re-read cache", ex); // NOI18N
                }
            }
            public void run() throws IOException {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                synchronized (ModuleLayeredFileSystem.this) {
                    try {
                        err.log(Level.FINEST, "storing to memory {0}", urls);
                        manager.store(cacheLayer, urls, os);
                        data = os.toByteArray();
                        ByteBuffer bb = ByteBuffer.wrap(data);
                        err.log(Level.FINEST, "reading from memory, size {0}", bb.limit());
                        cacheLayer = manager.load(cacheLayer, bb.order(ByteOrder.LITTLE_ENDIAN));
                    } catch (IOException ioe) {
                        err.log(Level.WARNING, null, ioe);
                        XMLFileSystem fallback = new XMLFileSystem();
                        try {
                            fallback.setXmlUrls(urls.toArray(new URL[0]));
                        } catch (PropertyVetoException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        cacheLayer = fallback;
                    }
                }
                err.log(Level.FINEST, "changing delegates");
                setDelegates(appendLayers(writableLayer, addLookup, otherLayers, cacheLayer));
                err.log(Level.FINEST, "delegates changed");
                err.log(Level.FINEST, "scheduling save");
                Stamps.getModulesJARs().scheduleSave(this, manager.cacheLocation(), false);
            }

        }
        Updater u = new Updater();
        runAtomicAction(u);
        
        this.urls = urls;
        firePropertyChange ("layers", null, null); // NOI18N
        
        StartLog.logEnd("setURLs"); // NOI18N
    }
    
    /** Layers are OK when its cache is OK.
     * 
     * @return true if layers are already OK
     */
    public boolean isLayersOK() {
        return Stamps.getModulesJARs().exists(manager.cacheLocation());
    }
    
    /** Adds few URLs.
     */
    public void addURLs(Collection<URL> urls) throws Exception {
        if (urls.contains(null)) throw new NullPointerException("urls=" + urls); // NOI18N
        // Add to the front: #23609.
        ArrayList<URL> arr = new ArrayList<URL>(urls);
        if (this.urls != null) arr.addAll(this.urls);
        setURLs(arr);
    }
    
    /** Removes few URLs.
     */
    public void removeURLs(Collection<URL> urls) throws Exception {
        if (urls.contains(null)) throw new NullPointerException("urls=" + urls); // NOI18N
        ArrayList<URL> arr = new ArrayList<URL>();
        if (this.urls != null) arr.addAll(this.urls);
        arr.removeAll(urls);
        setURLs(arr);
    }
    
    /** Refresh layers */
    public void resultChanged(LookupEvent ev) {
        setDelegates(appendLayers(writableLayer, addLookup, otherLayers, cacheLayer));
    }
    
    private static void setStatusText (String msg) {
        org.netbeans.core.startup.Main.setStatusText(msg);
    }

}
