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
package org.netbeans.modules.java.j2seplatform.libraries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.api.project.ProjectManager;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.FileObject;

import java.beans.Customizer;
import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.Iterator;
import java.net.URL;
import java.net.URI;
import java.util.HashSet;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public final class J2SELibraryTypeProvider implements LibraryTypeProvider {

    private J2SELibraryTypeProvider () {
    }

    private static final String LIB_PREFIX = "libs.";
    public static final String LIBRARY_TYPE = "j2se";       //NOI18N
    public static final String VOLUME_TYPE_CLASSPATH = "classpath";       //NOI18N
    public static final String VOLUME_TYPE_SRC = "src";       //NOI18N
    public static final String VOLUME_TYPE_JAVADOC = "javadoc";       //NOI18N
    public static final String VOLUME_TYPE_MAVEN_POM = "maven-pom"; //NOI18N
    public static final String[] VOLUME_TYPES = new String[] {
        VOLUME_TYPE_CLASSPATH,
        VOLUME_TYPE_SRC,
        VOLUME_TYPE_JAVADOC,
        VOLUME_TYPE_MAVEN_POM
    };
    
    private static final Set<String> VOLUME_TYPES_REQUIRING_FOLDER = new HashSet<String>(Arrays.asList(new String[] {
        VOLUME_TYPE_CLASSPATH,
        VOLUME_TYPE_SRC,
        VOLUME_TYPE_JAVADOC,
    }));

    public String getLibraryType() {
        return LIBRARY_TYPE;
    }
    
    public String getDisplayName () {
        return NbBundle.getMessage (J2SELibraryTypeProvider.class,"TXT_J2SELibraryType");
    }

    public String[] getSupportedVolumeTypes () {
        return VOLUME_TYPES;
    }

    public LibraryImplementation createLibrary() {
        return new J2SELibraryImpl ();
    }


    public void libraryCreated(final LibraryImplementation libraryImpl) {
        assert libraryImpl != null;
        ProjectManager.mutex().postWriteRequest(
                new Runnable () {
                    public void run () {
                        try {
                            EditableProperties props = PropertyUtils.getGlobalProperties();
                            boolean save = addLibraryIntoBuild(libraryImpl,props);
                            if (save) {
                                PropertyUtils.putGlobalProperties (props);
                            }
                        } catch (IOException ioe) {
                            ErrorManager.getDefault().notify (ioe);
                        }
                    }
                }
        );
    }

    public void libraryDeleted(final LibraryImplementation libraryImpl) {
        assert libraryImpl != null;
        ProjectManager.mutex().postWriteRequest(new Runnable () {
                public void run() {
                    try {
                        EditableProperties props = PropertyUtils.getGlobalProperties();
                        for (int i=0; i < VOLUME_TYPES.length; i++) {
                            String property = LIB_PREFIX + libraryImpl.getName() + '.' + VOLUME_TYPES[i];  //NOI18N
                            props.remove(property);
                        }
                        PropertyUtils.putGlobalProperties(props);
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify (ioe);
                    }
                }
            });
    }

    public Customizer getCustomizer(String volumeType) {
        if (VOLUME_TYPES[0].equals(volumeType)||
            VOLUME_TYPES[1].equals(volumeType)||
            VOLUME_TYPES[2].equals(volumeType)) {
            return new J2SEVolumeCustomizer (volumeType);
        }
        else {
            return null;
        }
    }
    

    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    public static LibraryTypeProvider create () {
        return new J2SELibraryTypeProvider();
    }

    private static boolean addLibraryIntoBuild(LibraryImplementation impl, EditableProperties props) {
        boolean modified = false;
        for (int i=0; i<VOLUME_TYPES.length; i++) {
            String propName = LIB_PREFIX + impl.getName() + '.' + VOLUME_TYPES[i];     //NOI18N
            List roots = impl.getContent (VOLUME_TYPES[i]);
            if (roots == null) {
                //Non valid library, but try to recover
                continue;
            }
            StringBuffer propValue = new StringBuffer();
            boolean first = true;
            for (Iterator rootsIt=roots.iterator(); rootsIt.hasNext();) {
                URL url = (URL) rootsIt.next();
                if ("jar".equals(url.getProtocol())) {
                    url = FileUtil.getArchiveFile (url);
                    // XXX check whether this is really the root
                }
                File f = null;
                FileObject fo = URLMapper.findFileObject(url);
                if (fo != null) {
                    f = FileUtil.toFile(fo);
                }
                else if ("file".equals(url.getProtocol())) {    //NOI18N
                    //If the file does not exist (eg library from cleaned project)
                    // and it is a file protocol URL, add it.
                    URI uri = URI.create (url.toExternalForm());
                    if (uri != null) {
                        f = new File (uri);
                    }
                }
                if (f != null) {
                    if (!first) {
                        propValue.append(File.pathSeparatorChar);
                    }
                    first = false;
                    f = FileUtil.normalizeFile(f);
                    propValue.append (f.getAbsolutePath());
                }
                else {
                    ErrorManager.getDefault().log ("J2SELibraryTypeProvider: Can not resolve URL: "+url);
                }
            }
            String oldValue = props.getProperty (propName);
            String newValue = propValue.toString();
            if (!newValue.equals(oldValue)) {
                    props.setProperty (propName, newValue);
                    modified = true;
            }
        }
        return modified;
    }
    
    //Like DefaultLibraryTypeProvider but in addition checks '/' on the end of folder URLs.
    private static class J2SELibraryImpl implements LibraryImplementation {
        private String description;

        private Map<String,List<URL>> contents;

        // library 'binding name' as given by user
        private String name;

        private String localizingBundle;

        private List<PropertyChangeListener> listeners;

        /**
         * Create new LibraryImplementation supporting given <tt>library</tt>.
         */
        public J2SELibraryImpl () {
            this.contents = new HashMap<String,List<URL>>();
            for (String vtype : VOLUME_TYPES) {
                this.contents.put(vtype, Collections.<URL>emptyList());
            }
        }


        public String getType() {
            return LIBRARY_TYPE;
        }

        public void setName(final String name) throws UnsupportedOperationException {
            String oldName = this.name;
            this.name = name;
            this.firePropertyChange (PROP_NAME, oldName, this.name);
        }

        public String getName() {
            return name;
        }

        public List<URL> getContent(String contentType) throws IllegalArgumentException {
            List<URL> content = contents.get(contentType);
            if (content == null)
                throw new IllegalArgumentException ();
            return Collections.unmodifiableList (content);
        }

        public void setContent(final String contentType, List<URL> path) throws IllegalArgumentException {
            if (path == null) {
                throw new IllegalArgumentException ();
            }
            if (this.contents.keySet().contains(contentType)) {
                if (VOLUME_TYPES_REQUIRING_FOLDER.contains(contentType)) {
                    path = check (path);
                }
                this.contents.put(contentType, new ArrayList<URL>(path));
                this.firePropertyChange(PROP_CONTENT,null,null);
            } else {
                throw new IllegalArgumentException ("Volume '"+contentType+
                    "' is not support by this library. The only acceptable values are: "+contents.keySet());
            }
        }

        private static List<URL> check (final List<? extends URL> resources) {
            final List<URL> checkedResources = new ArrayList<URL>(resources.size());
            for (URL u : resources) {
                final String surl = u.toString();
                if (!surl.endsWith("/")) {              //NOI18N
                    try {
                        u = new URL (surl+'/');         //NOI18N
                    } catch (MalformedURLException e) {
                        //Never thrown
                        Exceptions.printStackTrace(e);
                    }
                }
                checkedResources.add(u);
            }
            return checkedResources;
        }

        public String getDescription () {
                return this.description;
        }

        public void setDescription (String text) {
            String oldDesc = this.description;
            this.description = text;
            this.firePropertyChange (PROP_DESCRIPTION, oldDesc, this.description);
        }

        public String getLocalizingBundle() {
            return this.localizingBundle;
        }

        public void setLocalizingBundle(String resourceName) {
            this.localizingBundle = resourceName;
        }

        public synchronized void addPropertyChangeListener (PropertyChangeListener l) {
            if (this.listeners == null)
                this.listeners = new ArrayList<PropertyChangeListener>();
            this.listeners.add (l);
        }

        public synchronized void removePropertyChangeListener (PropertyChangeListener l) {
            if (this.listeners == null)
                return;
            this.listeners.remove (l);
        }

        public @Override String toString() {
            return this.getClass().getName()+"[" + name + "]"; // NOI18N
        }

        private void firePropertyChange (String propName, Object oldValue, Object newValue) {
            List<PropertyChangeListener> ls;
            synchronized (this) {
                if (this.listeners == null)
                    return;
                ls = new ArrayList<PropertyChangeListener>(listeners);
            }
            PropertyChangeEvent event = new PropertyChangeEvent (this, propName, oldValue, newValue);
            for (PropertyChangeListener l : ls) {
                l.propertyChange(event);
            }
        }
    }
    
}
