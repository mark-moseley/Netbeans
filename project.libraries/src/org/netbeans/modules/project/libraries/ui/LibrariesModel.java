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

package org.netbeans.modules.project.libraries.ui;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.project.libraries.LibraryAccessor;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryProvider;
import org.netbeans.modules.project.libraries.WritableLibraryProvider;
import org.netbeans.spi.project.libraries.ArealLibraryProvider;
import org.netbeans.spi.project.libraries.LibraryImplementation2;
import org.netbeans.spi.project.libraries.LibraryStorageArea;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

public class LibrariesModel implements PropertyChangeListener {

    public static final LibraryStorageArea GLOBAL_AREA = new LibraryStorageArea() {
        public String getDisplayName() {
            return NbBundle.getMessage(LibrariesModel.class, "LBL_global");
        }
        public URL getLocation() {
            throw new AssertionError();
        }
    };

    private static final Logger LOG = Logger.getLogger(LibrariesModel.class.getName());

    /**
     * Set of areas which have been explicitly created/loaded in this IDE session (thus static).
     * Keep only URL, <em>not</em> LibraryStorageArea, to avoid memory leaks.
     * Could also be modified to persist a LRU in NbPreferences, etc.
     */
    public static final Set<URL> createdAreas = Collections.synchronizedSet(new HashSet<URL>());

    private final Map<LibraryImplementation,LibraryStorageArea> library2Area = new HashMap<LibraryImplementation,LibraryStorageArea>();
    private final Map<LibraryStorageArea,ArealLibraryProvider> area2Storage = new HashMap<LibraryStorageArea,ArealLibraryProvider>();
    private final Map<LibraryImplementation,LibraryProvider> storageByLib = new HashMap<LibraryImplementation,LibraryProvider>();
    private final Map<LibraryStorageArea,LibraryProvider> area2Provider = new HashMap<LibraryStorageArea,LibraryProvider>();
    private final Collection<LibraryImplementation> actualLibraries = new TreeSet<LibraryImplementation>(new LibrariesComparator());
    private final List<LibraryImplementation> addedLibraries;
    private final List<LibraryImplementation> removedLibraries;
    private final List<ProxyLibraryImplementation> changedLibraries;
    private WritableLibraryProvider writableProvider;
    private final ChangeSupport cs = new ChangeSupport(this);

    public LibrariesModel () {
        this.addedLibraries = new ArrayList<LibraryImplementation>();
        this.removedLibraries = new ArrayList<LibraryImplementation>();
        this.changedLibraries = new ArrayList<ProxyLibraryImplementation>();
        for (LibraryProvider lp : Lookup.getDefault().lookupAll(LibraryProvider.class)) {
            lp.addPropertyChangeListener(WeakListeners.propertyChange(this, lp));
            if (writableProvider == null && lp instanceof WritableLibraryProvider) {
                writableProvider = (WritableLibraryProvider) lp;
            }
        }
        for (ArealLibraryProvider alp : Lookup.getDefault().lookupAll(ArealLibraryProvider.class)) {
            alp.addPropertyChangeListener(WeakListeners.propertyChange(this, alp));
        }
        this.computeLibraries();
    }
    
    public synchronized Collection<? extends LibraryImplementation> getLibraries() {
        return actualLibraries;
    }

    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }

    public LibraryStorageArea createArea() {
        for (ArealLibraryProvider alp : Lookup.getDefault().lookupAll(ArealLibraryProvider.class)) {
            LibraryStorageArea area = alp.createArea();
            if (area != null) {
                createdAreas.add(area.getLocation());
                area2Storage.put(area, alp);
                propertyChange(null); // recompute libraries & fire change
                return area;
            }
        }
        return null;
    }

    public LibraryImplementation createArealLibrary(String type, String name, LibraryStorageArea area) {
        LibraryImplementation impl = new DummyArealLibrary(type, name);
        library2Area.put(impl, area);
        return impl;
    }

    public Collection<? extends LibraryStorageArea> getAreas() {
        Set<LibraryStorageArea> areas = new HashSet<LibraryStorageArea>();
        for (ArealLibraryProvider alp : Lookup.getDefault().lookupAll(ArealLibraryProvider.class)) {
            for (LibraryStorageArea area : LibraryAccessor.getOpenAreas(alp)) {
                area2Storage.put(area, alp);
                areas.add(area);
            }
        }
        for (ArealLibraryProvider alp : Lookup.getDefault().lookupAll(ArealLibraryProvider.class)) {
            for (URL location : createdAreas) {
                LibraryStorageArea area = alp.loadArea(location);
                if (area != null) {
                    assert area.getLocation().equals(location) : "Bad location " + area.getLocation() + " does not match " + location + " from " + alp.getClass().getName();
                    area2Storage.put(area, alp);
                    areas.add(area);
                }
            }
        }
        return areas;
    }

    public LibraryStorageArea getArea(LibraryImplementation library) {
        LibraryStorageArea area = getAreaOrNull(library);
        return area != null ? area : GLOBAL_AREA;
    }
    private LibraryStorageArea getAreaOrNull(LibraryImplementation library) {
        if (library instanceof ProxyLibraryImplementation) {
            library = ((ProxyLibraryImplementation) library).getOriginal();
        }
        return library2Area.get(library);
    }

    public void addLibrary (LibraryImplementation impl) {
        synchronized (this) {
            addedLibraries.add(impl);
            actualLibraries.add(impl);
        }
        cs.fireChange();
    }

    public void removeLibrary (LibraryImplementation impl) {
        synchronized (this) {
            if (addedLibraries.contains(impl)) {
                addedLibraries.remove(impl);
            } else {
                removedLibraries.add(((ProxyLibraryImplementation) impl).getOriginal());
            }
            actualLibraries.remove(impl);
        }
        cs.fireChange();
    }

    public void modifyLibrary(ProxyLibraryImplementation impl) {
        synchronized (this) {
            if (!addedLibraries.contains(impl) && !changedLibraries.contains(impl)) {
                changedLibraries.add(impl);
            }
        }
        cs.fireChange();
    }

    public boolean isLibraryEditable (LibraryImplementation impl) {
        if (this.addedLibraries.contains(impl))
            return true;
        LibraryProvider provider = storageByLib.get
                (((ProxyLibraryImplementation)impl).getOriginal());
        return provider == writableProvider || getAreaOrNull(impl) != null;
    }

    public void apply () throws IOException {
        for (LibraryImplementation impl : removedLibraries) {
            LibraryProvider storage = storageByLib.get(impl);
            if (storage == this.writableProvider) {
                this.writableProvider.removeLibrary (impl);
            } else {
                assert impl instanceof LibraryImplementation2;
                LibraryStorageArea area = getAreaOrNull(impl);
                if (area != null) {
                    LibraryAccessor.remove(area2Storage.get(area), (LibraryImplementation2)impl);
                } else {
                    throw new IOException("Cannot find storage for library: " + impl.getName()); // NOI18N
                }
            }
        }
        for (LibraryImplementation impl : addedLibraries) {
            LibraryStorageArea area = getAreaOrNull(impl);
            if (area != null) {
                ArealLibraryProvider alp = area2Storage.get(area);
                assert alp != null : area;
                LibraryAccessor.createLibrary(alp, impl.getType(), impl.getName(), area, ((DummyArealLibrary) impl).contents);
            } else if (writableProvider != null) {
                writableProvider.addLibrary(impl);
            } else {
                throw new IOException("Cannot add libraries, no WritableLibraryProvider."); // NOI18N
            }
        }
        for (ProxyLibraryImplementation proxy : changedLibraries) {
            LibraryImplementation orig = proxy.getOriginal();
            LibraryProvider storage = storageByLib.get(orig);
            if (storage == this.writableProvider) {
                this.writableProvider.updateLibrary(orig, proxy);
            } else {
                LibraryStorageArea area = library2Area.get(orig);
                if (area != null) {
                    if (proxy instanceof ProxyLibraryImplementation.ProxyLibraryImplementation2) {
                        ProxyLibraryImplementation.ProxyLibraryImplementation2 proxy2 = 
                                (ProxyLibraryImplementation.ProxyLibraryImplementation2)proxy;
                        LibraryImplementation2 orig2 = proxy2.getOriginal2();
                        if (proxy2.newURIContents != null) {
                            for (Map.Entry<String,List<URI>> entry : proxy2.newURIContents.entrySet()) {
                                orig2.setURIContent(entry.getKey(), entry.getValue());
                            }
                        }
                    } else if (proxy.newContents != null) {
                        for (Map.Entry<String,List<URL>> entry : proxy.newContents.entrySet()) {
                            orig.setContent(entry.getKey(), entry.getValue());
                        }
                    }
                } else {
                    throw new IOException("Cannot find storage for library: " + orig.getName()); // NOI18N
                }
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        // compute libraries later in AWT thread and not in calling thread
        // to prevent deadlocks
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                computeLibraries();
                cs.fireChange();
            }
        });
    }

    private ProxyLibraryImplementation findModified (LibraryImplementation impl) {
        for (ProxyLibraryImplementation proxy : changedLibraries) {
            if (proxy.getOriginal().equals (impl)) {
                return proxy;
            }
        }
        return null;
    }

    private synchronized void computeLibraries() {
        actualLibraries.clear();
        for (LibraryProvider storage : Lookup.getDefault().lookupAll(LibraryProvider.class)) {
            for (LibraryImplementation lib : storage.getLibraries()) {
                ProxyLibraryImplementation proxy = findModified(lib);
                if (proxy != null) {
                    actualLibraries.add(proxy);
                } else {
                    actualLibraries.add(proxy = ProxyLibraryImplementation.createProxy(lib, this));
                }
                storageByLib.put(lib, storage);
                LOG.log(Level.FINER, "computeLibraries: storage={0} lib={1} proxy={2}", new Object[] {storage, lib, proxy});
            }
        }
        for (LibraryStorageArea area : getAreas()) {
            ArealLibraryProvider alp = area2Storage.get(area);
            assert alp != null : area;
            LibraryProvider prov = area2Provider.get(area);
            if (prov == null) {
                prov = LibraryAccessor.getLibraries(alp, area);
                prov.addPropertyChangeListener(this); // need not be weak, we just created the source
                area2Provider.put(area, prov);
            }
            for (LibraryImplementation lib : prov.getLibraries()) {
                ProxyLibraryImplementation proxy = findModified(lib);
                if (proxy != null) {
                    actualLibraries.add(proxy);
                } else {
                    actualLibraries.add(proxy = ProxyLibraryImplementation.createProxy(lib, this));
                }
                library2Area.put(lib, area);
                LOG.log(Level.FINER, "computeLibraries: alp={0} area={1} lib={2} proxy={3}", new Object[] {alp, area, lib, proxy});
            }
        }
        actualLibraries.addAll(addedLibraries);
        LOG.log(Level.FINE, "computeLibraries: actualLibraries={0} library2Area={1}", new Object[] {actualLibraries, library2Area});
    }

    private static class LibrariesComparator implements Comparator<LibraryImplementation> {
        public int compare(LibraryImplementation lib1, LibraryImplementation lib2) {
            String name1 = LibrariesCustomizer.getLocalizedString(lib1.getLocalizingBundle(), lib1.getName());
            String name2 = LibrariesCustomizer.getLocalizedString(lib2.getLocalizingBundle(), lib2.getName());
            int r = name1.compareToIgnoreCase(name2);
            return r != 0 ? r : System.identityHashCode(lib1) - System.identityHashCode(lib2);
        }
    }

    private static final class DummyArealLibrary implements LibraryImplementation2 {

        private final String type, name;
        final Map<String,List<URI>> contents = new HashMap<String,List<URI>>();
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

        public DummyArealLibrary(String type, String name) {
            this.type = type;
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return null;
        }

        public String getLocalizingBundle() {
            return null;
        }

        public List<URL> getContent(String volumeType) throws IllegalArgumentException {
            return convertURIsToURLs(getURIContent(volumeType));
        }
        
        public List<URI> getURIContent(String volumeType) throws IllegalArgumentException {
            List<URI> content = contents.get(volumeType);
            if (content != null) {
                return content; 
            } else {
                return Collections.emptyList();
            }
        }

        public void setName(String name) {
            throw new UnsupportedOperationException();
        }

        public void setDescription(String text) {
            throw new UnsupportedOperationException();
        }

        public void setLocalizingBundle(String resourceName) {
            throw new UnsupportedOperationException();
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }

        public void setContent(String volumeType, List<URL> path) throws IllegalArgumentException {
            setURIContent(volumeType, convertURLsToURIs(path));
        }

        public void setURIContent(String volumeType, List<URI> path) throws IllegalArgumentException {
            contents.put(volumeType, path);
            pcs.firePropertyChange(LibraryImplementation.PROP_CONTENT, null, null);
        }

        @Override
        public String toString() {
            return "DummyArealLibrary[" + name + "]"; // NOI18N
        }

    }

    public static List<URL> convertURIsToURLs(List<URI> uris) {
        List<URL> content = new ArrayList<URL>();
        for (URI uri : uris) {
            try {
                content.add(uri.toURL());
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return content;
    }

    public static List<URI> convertURLsToURIs(List<URL> entry) {
        List<URI> content = new ArrayList<URI>();
        for (URL url : entry) {
            content.add(URI.create(url.toExternalForm()));
        }
        return content;
    }


}
