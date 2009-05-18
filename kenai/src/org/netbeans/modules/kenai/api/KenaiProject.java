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

package org.netbeans.modules.kenai.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.kenai.FeatureData;
import org.netbeans.modules.kenai.ProjectData;
import org.netbeans.modules.kenai.ProjectFeatureCreateData;
import org.netbeans.modules.kenai.api.KenaiService.Type;

/**
 * IDE-side representation of a Kenai project.
 *
 * @author Maros Sandor
 * @author Jan Becicka
 */
public final class KenaiProject {

    /**
     * getSource() returns project being refreshed
     * values are undefined
     */
    public static final String PROP_PROJECT_CHANGED = "project_change";


    private java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);

    private ProjectData     data;
    
    private KenaiFeature[] features;

    /**
     * I assume that this constructor does NOT provide full project information. If it does then
     * call fillInfo() just after the object is created.
     *
     * @param p
     */
    private KenaiProject(ProjectData p) {
        fillInfo(p);
    }

    static KenaiProject get(ProjectData p) {
        final Kenai kenai = Kenai.getDefault();
        synchronized (kenai.projectsCache) {
            WeakReference<KenaiProject> wr = kenai.projectsCache.get(p.name);
            KenaiProject result = null;
            if (wr == null || (result = wr.get()) == null) {
                result = new KenaiProject(p);
                kenai.projectsCache.put(p.name, new WeakReference<KenaiProject>(result));
            } else {
                result = wr.get();
                result.fillInfo(p);
            }
            return result;
        }
    }

    /**
     * getProject from cache
     * @param name
     * @return returns null if project does not exist in cachce
     */
    static KenaiProject get(String name) {
        final Kenai kenai = Kenai.getDefault();
        synchronized (kenai.projectsCache) {
            WeakReference<KenaiProject> wr = kenai.projectsCache.get(name);
            if (wr == null) {
                return null;
            }
            return wr.get();
        }
    }


    /**
     * Unique name of project
     * @return project name
     */
    public synchronized String getName() {
        return data.name;
    }

    /**
     * web location of this project
     * @return web location of this project
     */
    public synchronized URL getWebLocation() {
        try {
            return new URL(data.web_url);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Display name of this project
     * @return display name
     */
    public synchronized String getDisplayName() {
        return data.display_name;
    }

    /**
     * Description of this project
     * @return project description
     */
    public synchronized String getDescription() throws KenaiException {
        fetchDetailsIfNotAvailable();
        return data.description;
    }

    /**
     * @return tags separated by space
     *
     */
    public synchronized String getTags() throws KenaiException {
        fetchDetailsIfNotAvailable();
        return data.tags;
    }

    public synchronized boolean isPrivate() throws KenaiException {
        fetchDetailsIfNotAvailable();
        return data.private_hidden;
    }

    private static Pattern repositoryPattern = Pattern.compile("(https|http)://(testkenai|kenai)\\.com/(svn|hg)/(\\S*)~(.*)");

    /**
     * Looks up a project by repository location.
     * The current implementation does not work for external repositories.
     * @param uri location of repository; for example SVN HTTP URL;
     *            typically gotten from {@code ProvidedExtensions.RemoteLocation} file attribute of project directory
     * @return Kenai project associated with that repository, or null
     * @throws KenaiException if the project cannot be loaded
     */
    public static KenaiProject forRepository(String uri) throws KenaiException {
        Matcher m = repositoryPattern.matcher(uri);
        if (m.matches()) {
            return Kenai.getDefault().getProject(m.group(4));
        }

        return null;
    }

    /**
     * @return features of given project
     * @see KenaiFeature
     */
    public synchronized KenaiFeature[] getFeatures() throws KenaiException {
        fetchDetailsIfNotAvailable();
        if (features==null) {
            features=new KenaiFeature[data.features.length];
            int i=0;
            for (FeatureData feature:data.features) {
                features[i++] = new KenaiFeature(feature);
            }
        }
        return features;
    }

    /**
     * get features of given type
     * @param type
     * @return array of KenaiFetaures of given type
     * @throws KenaiException 
     */
    public synchronized KenaiFeature[] getFeatures(Type type) throws KenaiException {
        ArrayList<KenaiFeature> fs= new ArrayList();
        for (KenaiFeature f:getFeatures()) {
            if (f.getType().equals(type)) {
                fs.add(f);
            }
        }
        return fs.toArray(new KenaiFeature[fs.size()]);
    }

    /**
     * Creates new feateru for this project
     * @param name
     * @param display_name
     * @param description
     * @param service
     * @param url
     * @param repository_url
     * @param browse_url
     * @return project feature
     * @throws org.netbeans.modules.kenai.api.KenaiException
     * @see KenaiFeature
     */
    public KenaiFeature createProjectFeature(
            String name,
            String display_name,
            String description,
            String service,
            String url,
            String repository_url,
            String browse_url
            ) throws KenaiException {
        KenaiFeature feature = Kenai.getDefault().createProjectFeature(getName(), name, display_name, description, service, url, repository_url, browse_url);
        refresh();
        return feature;
    }

    /**
     * Checks weather proposed name is unique and valid
     * @param name proposed name
     * @return Error message or null, if name is valid
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    public static String checkName(String name) throws KenaiException {
        return Kenai.getDefault().checkName(name);
    }

    void fillInfo(ProjectData prj) {
        synchronized (this) {
            if (prj.updated_at==null && this.data!=null && this.data.updated_at!=null) {
                return;
            }
            this.data = prj;
            features = null;
        }
        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, PROP_PROJECT_CHANGED, null, null));
    }

    synchronized ProjectData getData() {
        return data;
    }

    private void fetchDetailsIfNotAvailable() throws KenaiException {
        if (this.data.updated_at != null) {
            return;
        }
        refresh();
    }

    /**
     * Reloads project from kenai.com server
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    private void refresh() throws KenaiException {
        fillInfo(Kenai.getDefault().getDetails(getName()));
    }

    @Override
    public synchronized boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final KenaiProject other = (KenaiProject) obj;
        if ((this.data.name == null) ? (other.data.name != null) : !this.data.name.equals(other.data.name)) {
            return false;
        }
        return true;
    }

    @Override
    public synchronized int hashCode() {
        int hash = 5;
        hash = 13 * hash + (this.data.name != null ? this.data.name.hashCode() : 0);
        return hash;
    }

    @Override
    public synchronized String toString() {
        return "KenaiProject " + getName();
    }

    /**
     * Adds listener to Kenai instance
     * @param l
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    /**
     * Adds listener to Kenai instance
     * @param name
     * @param l
     */
    public synchronized void addPropertyChangeListener(String name, PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(name,l);
    }

    /**
     * Removes listener from Kenai instance
     * @param l
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    /**
     * Removes listener from Kenai instance
     * @param name
     * @param l
     */
    public synchronized void removePropertyChangeListener(String name, PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(name, l);
    }
}
