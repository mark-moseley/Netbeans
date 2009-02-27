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
 * or nbbuild/services/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/services/CDDL-GPL-2-CP.  Sun designates this
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

package org.netbeans.modules.kenai;

import java.io.IOException;
import java.net.URL;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.kenai.api.KenaiException;
import org.codeviation.pojson.*;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiErrorMessage;

/**
 * Talks to remote Kenai server via Web services API.
 *
 * @author Maros Sandor
 * @author Jan Becicka
 */
public class KenaiREST extends KenaiImpl {

    private URL baseURL;

    public KenaiREST(URL baseURL) {
        this.baseURL = baseURL;
    }

    @Override
    public boolean isAuthorized(String projectName, String feature, String activity) throws KenaiException {
        String [][] params = {
            new String [] { "feature_id", feature },
            new String [] { "activity_id", activity },
            new String [] { "person_id", Kenai.getDefault().getPasswordAuthentication().getUserName()},
            new String [] { "project_id", projectName }
        };
        RestConnection conn = new RestConnection(baseURL.toString() + "/api/login/authorize", params);
        RestResponse resp = null;
        try {
            resp = conn.get(null);
        } catch (IOException iOException) {
            throw new KenaiException(iOException);
        }
        return resp.getResponseCode() == 200;
    }

    @Override
    public ProjectData getProject(String name) throws KenaiException {
        RestConnection conn = new RestConnection(baseURL.toString() + "/api/projects/" + name + ".json");
        RestResponse resp = null;
        try {
            resp = conn.get(null);
        } catch (IOException iOException) {
            throw new KenaiException(iOException);
        }

        if (resp.getResponseCode() != 200)
            throw new KenaiErrorMessage(resp.getResponseMessage(),resp.getDataAsString());

        String sss = resp.getDataAsString();

        PojsonLoad pload = PojsonLoad.create();
        return pload.load(sss, ProjectData.class);
    }

    @Override
    public Collection<ProjectData> searchProjects(String pattern) throws KenaiException {
        ProjectsListData pld = loadPage(baseURL.toString() + "/api/projects.json?q=" + pattern, ProjectsListData.class);
        return new LazyList(pld, ProjectsListData.class);

    }

    @Override
    public Collection<ProjectData> getMyProjects() throws KenaiException {
        ProjectsListData pld = loadPage(baseURL.toString() + "/api/projects/mine.json", ProjectsListData.class);
        return new LazyList(pld, ProjectsListData.class);
    }

    private static <T> T loadPage(String url, Class<T> clazz) throws KenaiException {

        RestConnection conn = new RestConnection(url);
        RestResponse resp = null;
        try {
            resp = conn.get(null);
        } catch (IOException iOException) {
            throw new KenaiException(iOException);
        }
        if (resp.getResponseCode() != 200)
            throw new KenaiErrorMessage(resp.getResponseMessage(),resp.getDataAsString());
        String responseString = resp.getDataAsString();

        PojsonLoad pload = PojsonLoad.create();
        T data = pload.load(responseString, clazz);

        return data;
    }

    @Override
    public Collection<LicensesListData.LicensesListItem> getLicenses() throws KenaiException {
        LicensesListData pld = loadPage(baseURL.toString() + "/api/licenses.json", LicensesListData.class);
        return new LazyList(pld, LicensesListData.class);
    }

    @Override
    public Collection<ServicesListData.ServicesListItem> getServices() throws KenaiException {
        ServicesListData pld = loadPage(baseURL.toString() + "/api/services.json", ServicesListData.class);
        return new LazyList(pld, ServicesListData.class);
    }

    @Override
    public String checkName(String name) throws KenaiException {
        CheckNameData cnd = loadPage(baseURL.toString() + "/projects/check_unique.json?name="+name, CheckNameData.class);
        return cnd.is_unique?null:cnd.message;
    }

    private class LazyList<COLLECTION extends ListData, ITEM> extends AbstractCollection<ITEM> {

        private COLLECTION col;
        private Class<COLLECTION> clazz;


        public LazyList(COLLECTION col, Class<COLLECTION> clazz) {
            this.col=col;
            this.clazz=clazz;
        }

        @Override
        public Iterator<ITEM> iterator() {
            return new LazyIterator<COLLECTION, ITEM>(col, clazz);
        }

        @Override
        public int size() {
            return col.total;
        }

    }
    private static int PAGE_SIZE = 10;

    private class LazyIterator<COLLECTION extends ListData, ITEM> implements Iterator<ITEM> {

        private COLLECTION col;
        private int currentIndex = 0;
        private Class<COLLECTION> clazz;

        public LazyIterator(COLLECTION col, Class<COLLECTION> clazz) {
            this.col = col;
            this.clazz = clazz;
        }


        public boolean hasNext() {
            if (col.total>currentIndex) {
                return true;
            }
            return col.next!=null;
        }

        public ITEM next() {
            try {
                if (currentIndex==PAGE_SIZE) {
                    currentIndex-=PAGE_SIZE;
                    col = loadPage(col.next, clazz);
                }
                return colToItem(currentIndex++);
            } catch (KenaiException ex) {
                throw new RuntimeException(ex);
            }
        }

        public void remove() {
            throw new UnsupportedOperationException("Cannot remove items");
        }

        private ITEM colToItem(int index) throws KenaiException {
            if (col instanceof ProjectsListData) {
                return (ITEM) getProject(((ProjectsListData) col).projects[index].name);
            } else if (col instanceof ServicesListData) {
                return (ITEM) ((ServicesListData) col).services[index];
            } else if (col instanceof LicensesListData) {
                return (ITEM) ((LicensesListData) col).licenses[index];
            }
            throw new IllegalStateException();
        }

    }


    @Override
    public ProjectData createProject(
            String name,
            String displayName,
            String description,
            String[] licenses,
            String tags
            ) throws KenaiException {
        RestConnection conn = new RestConnection(baseURL.toString() + "/api/projects.json");
        RestResponse resp = null;
        PojsonSave<ProjectCreateData> save = PojsonSave.create(ProjectCreateData.class);
        ProjectCreateData prdata = new ProjectCreateData();
        prdata.project.name = name;
        prdata.project.display_name = displayName;
        prdata.project.description = description;
        prdata.project.licenses = licenses;
        prdata.project.tags = tags;

        try {
            resp = conn.post(null, save.asString(prdata));
        } catch (IOException iOException) {
            throw new KenaiException(iOException);
        }

        if (resp.getResponseCode() != 201)
            throw new KenaiErrorMessage(resp.getResponseMessage(),resp.getDataAsString());

        String response = resp.getDataAsString();

        PojsonLoad pload = PojsonLoad.create();
        return pload.load(response, ProjectData.class);
    }

    @Override
    public FeatureData createProjectFeature(
            String projectName,
            String name,
            String display_name,
            String description,
            String url,
            String repository_url,
            String browse_url,
            String service
            ) throws KenaiException {
        
        RestConnection conn = new RestConnection(baseURL.toString() + "/api/projects/"+projectName+"/features.json");
        RestResponse resp = null;
        PojsonSave<ProjectFeatureCreateData> save = PojsonSave.create(ProjectFeatureCreateData.class);
        ProjectFeatureCreateData fdata = new ProjectFeatureCreateData();
        fdata.feature.name = name;
        fdata.feature.display_name = display_name;
        fdata.feature.description = description;
        fdata.feature.service = service;
        fdata.feature.url = url;
        fdata.feature.repository_url = repository_url;
        fdata.feature.browse_url = browse_url;
        try {
            resp = conn.post(null, save.asString(fdata));
        } catch (IOException iOException) {
            throw new KenaiException(iOException);
        }

        if (resp.getResponseCode() != 201)
            throw new KenaiErrorMessage(resp.getResponseMessage(),resp.getDataAsString());

        String response = resp.getDataAsString();

        PojsonLoad pload = PojsonLoad.create();
        return pload.load(response, FeatureData.class);
    }

    @Override
    public void register(String username, char[] password) throws KenaiException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void verify(String username, char[] password) throws KenaiException {
        String [][] params = {
            new String [] { "username", username },
            new String [] { "password", new String(password) }
        };
        RestConnection conn = new RestConnection(baseURL.toString() + "/api/login/authenticate", params);
        RestResponse resp = null;
        try {
            resp = conn.get(null);
        } catch (IOException iOException) {
            throw new KenaiException(iOException);
        }
        if (resp.getResponseCode() != 200) {
            throw new KenaiException("Authentication failed");
        }
    }
//        DateFormat df = new SimpleDateFormat("y-M-d'T'H:m:s'Z'");
}
