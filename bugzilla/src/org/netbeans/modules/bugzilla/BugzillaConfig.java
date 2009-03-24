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

package org.netbeans.modules.bugzilla;

import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaConfig {

    private static BugzillaConfig instance = null;
    private static final String LAST_CHANGE_FROM    = "bugzilla.last_change_from"; // XXX
    private static final String REPO_NAME           = "bugzilla.repository_";
    private static final String QUERY_NAME          = "bugzilla.query_";
    private static final String QUERY_REFRESH_INT   = "bugzilla.query_refresh";
    private static final String QUERY_AUTO_REFRESH  = "bugzilla.query_auto_refresh_";
    private static final String ISSUE_REFRESH_INT   = "bugzilla.issue_refresh";
    private static final String DELIMITER           = "<=>";

    private BugzillaConfig() { }

    public static BugzillaConfig getInstance() {
        if(instance == null) {
            instance = new BugzillaConfig();
        }
        return instance;
    }

    public Preferences getPreferences() {
        return NbPreferences.forModule(BugzillaConfig.class);
    }

    public void setQueryRefreshInterval(int i) {
        getPreferences().putInt(QUERY_REFRESH_INT, i);
    }

    public void setIssueRefreshInterval(int i) {
        getPreferences().putInt(ISSUE_REFRESH_INT, i);
    }

    public void setQueryAutoRefresh(String queryName, boolean refresh) {
        getPreferences().putBoolean(QUERY_AUTO_REFRESH + queryName, refresh);
    }

    public int getQueryRefreshInterval() {
        return getPreferences().getInt(QUERY_REFRESH_INT, 30);
    }

    public int getIssueRefreshInterval() {
        return getPreferences().getInt(ISSUE_REFRESH_INT, 15);
    }

    public boolean getQueryAutoRefresh(String queryName) {
        return getPreferences().getBoolean(QUERY_AUTO_REFRESH + queryName, false);
    }

    public void putQuery(BugzillaRepository repository, BugzillaQuery query) {
        getPreferences().put(getQueryKey(repository.getDisplayName(), query.getDisplayName()), query.getUrlParameters() + DELIMITER + query.getLastRefresh());
    }

    public void removeQuery(BugzillaRepository repository, BugzillaQuery query) {
        getPreferences().remove(getQueryKey(repository.getDisplayName(), query.getDisplayName()));
    }

    public BugzillaQuery getQuery(BugzillaRepository repository, String queryName) {
        String value = getStoredQuery(repository, queryName);
        if(value == null) {
            return null;
        }
        String[] values = value.split(DELIMITER);
        assert values.length == 2;
        String urlParams = values[0];
        long lastRefresh = Long.parseLong(values[1]);
        return new BugzillaQuery(queryName, repository, urlParams, lastRefresh);
    }

    public String getUrlParams(BugzillaRepository repository, String queryName) {
        String value = getStoredQuery(repository, queryName);
        if(value == null) {
            return null;
        }
        String[] values = value.split(DELIMITER);
        assert values.length == 2;
        return values[0];
    }

    public String[] getQueries(String repoName) {        
        return getKeysWithPrefix(QUERY_NAME + repoName + DELIMITER);
    }

    public void putRepository(String repoName, BugzillaRepository repository) {
        String user = repository.getUsername();
        String password = BugtrackingUtil.scramble(repository.getPassword());
        // XXX AuthenticationType.HTTP, AuthenticationType.PROXY
        String url = repository.getUrl();
        getPreferences().put(REPO_NAME + repoName, url + DELIMITER + user + DELIMITER + password);
    }

    public BugzillaRepository getRepository(String repoName) {
        String repoString = getPreferences().get(REPO_NAME + repoName, "");
        if(repoString.equals("")) {
            return null;
        }
        String[] values = repoString.split(DELIMITER);
        assert values.length == 3;
        String url = values[0];
        String user = values[1];
        String password = BugtrackingUtil.descramble(values[2]);
        // XXX AuthenticationType.HTTP, AuthenticationType.PROXY

        return new BugzillaRepository(repoName, url, user, password);
    }

    public String[] getRepositories() {
        return getKeysWithPrefix(REPO_NAME);
    }

    public void removeRepository(String name) {
        getPreferences().remove(REPO_NAME + name);
    }

    private String[] getKeysWithPrefix(String prefix) {
        String[] keys = null;
        try {
            keys = getPreferences().keys();
        } catch (BackingStoreException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex); // XXX
        }
        if (keys == null || keys.length == 0) {
            return new String[0];
        }
        List<String> ret = new ArrayList<String>();
        for (String key : keys) {
            if (key.startsWith(prefix)) {
                ret.add(key.substring(prefix.length()));
            }
        }
        return ret.toArray(new String[ret.size()]);
    }

    private String getQueryKey(String repositoryName, String queryName) {
        return QUERY_NAME + repositoryName + DELIMITER + queryName;
    }

    private String getStoredQuery(BugzillaRepository repository, String queryName) {
        String value = getPreferences().get(getQueryKey(repository.getDisplayName(), queryName), null);
        return value;
    }

    public void setLastChangeFrom(String value) {
        getPreferences().put(LAST_CHANGE_FROM, value);
    }

    public String getLastChangeFrom() {
        return getPreferences().get(LAST_CHANGE_FROM, "");
    }
}
