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
 * single choiceget of license, a recipient has the option to distribute
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

package org.netbeans.modules.bugzilla.repository;

import org.netbeans.modules.bugzilla.*;
import java.awt.Image;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.bugzilla.core.RepositoryConfiguration;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.util.IssueCache;
import org.netbeans.modules.bugzilla.commands.BugzillaCommand;
import org.netbeans.modules.bugzilla.commands.BugzillaExecutor;
import org.netbeans.modules.bugzilla.commands.GetMultiTaskDataCommand;
import org.netbeans.modules.bugzilla.commands.PerformQueryCommand;
import org.netbeans.modules.bugzilla.query.QueryController;
import org.netbeans.modules.bugzilla.util.BugzillaConstants;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaRepository extends Repository {

    private static final String ICON_PATH = "org/netbeans/modules/bugtracking/ui/resources/repository.png"; // NOI18N

    private String name;
    private TaskRepository taskRepository;
    private RepositoryController controller;
    private Set<Query> queries = null;
    private IssueCache cache;
    private BugzillaExecutor executor;
    private Image icon;
    private BugzillaConfiguration bc;
    private RequestProcessor refreshProcessor;

    private final Set<String> issuesToRefresh = new HashSet<String>(5);
    private final Set<BugzillaQuery> queriesToRefresh = new HashSet<BugzillaQuery>(3);
    private Task refreshIssuesTask;
    private Task refreshQueryTask;

    public BugzillaRepository() {
        icon = ImageUtilities.loadImage(ICON_PATH, true);
    }

    public BugzillaRepository(String repoName, String url, String user, String password, String httpUser, String httpPassword) {
        this();
        name = repoName;
        if(user == null) {
            user = "";                                                          // NOI18N
        }
        if(password == null) {
            password = "";                                                      // NOI18N
        }
        taskRepository = createTaskRepository(name, url, user, password, httpUser, httpPassword);
    }

    public TaskRepository getTaskRepository() {
        return taskRepository;
    }

    @Override
    public Query createQuery() {
        if(getConfiguration() == null) {
            // invalid connection data?
            return null;
        }
        BugzillaQuery q = new BugzillaQuery(this);        
        return q;
    }

    @Override
    public Issue createIssue() {
        if(getConfiguration() == null) {
            // invalid connection data?
            return null;
        }
        TaskAttributeMapper attributeMapper =
                Bugzilla.getInstance()
                    .getRepositoryConnector()
                    .getTaskDataHandler()
                    .getAttributeMapper(taskRepository);
        TaskData data =
                new TaskData(
                    attributeMapper,
                    taskRepository.getConnectorKind(),
                    taskRepository.getRepositoryUrl(),
                    ""); // NOI18N
        return new BugzillaIssue(data, this);
    }

    @Override
    public void remove() {
        Query[] qs = getQueries();
        for (Query q : qs) {
            removeQuery((BugzillaQuery) q);
        }
        resetRepository();
        Bugzilla.getInstance().removeRepository(this);
    }

    synchronized void resetRepository() {
        bc = null;
        if(getTaskRepository() != null) {
            Bugzilla.getInstance()
                    .getRepositoryConnector()
                    .getClientManager()
                    .repositoryRemoved(getTaskRepository());
        }
    }

    void setName(String name) {
        this.name = name;
    }
    
    @Override
    public void fireQueryListChanged() {
        super.fireQueryListChanged();
    }

    public String getDisplayName() {
        return name;
    }

    @Override
    public String getTooltip() {
        return name + " : " + taskRepository.getCredentials(AuthenticationType.REPOSITORY).getUserName() + "@" + taskRepository.getUrl(); // NOI18N
    }

    @Override
    public Image getIcon() {
        return icon;
    }

    public String getUsername() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.REPOSITORY);
        return c != null ? c.getUserName() : ""; // NOI18N
    }

    public String getPassword() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.REPOSITORY);
        return c != null ? c.getPassword() : ""; // NOI18N
    }

    public String getHttpUsername() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.HTTP);
        return c != null ? c.getUserName() : ""; // NOI18N
    }

    public String getHttpPassword() {
        AuthenticationCredentials c = getTaskRepository().getCredentials(AuthenticationType.HTTP);
        return c != null ? c.getPassword() : ""; // NOI18N
    }

    public Issue getIssue(final String id) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        TaskData taskData = BugzillaUtil.getTaskData(BugzillaRepository.this, id);
        if(taskData == null) {
            return null;
        }
        try {
            return getIssueCache().setIssueData(id, taskData);
        } catch (IOException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    // XXX create repo wih product if kenai project and use in queries
    public Issue[] simpleSearch(final String criteria) {
        assert taskRepository != null;
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        String[] keywords = criteria.split(" ");                                // NOI18N

        final List<Issue> issues = new ArrayList<Issue>();
        TaskDataCollector collector = new TaskDataCollector() {
            public void accept(TaskData taskData) {
                BugzillaIssue issue = new BugzillaIssue(taskData, BugzillaRepository.this);
                issues.add(issue); // we don't cache this issues
                                   // - the retured taskdata are partial
                                   // - and we need an as fast return as possible at this place

            }
        };

        if(keywords.length == 1 && isInteger(keywords[0])) {
            // only one search criteria -> might be we are looking for the bug with id=keywords[0]
            TaskData taskData = BugzillaUtil.getTaskData(this, keywords[0], false);
            if(taskData != null) {
                BugzillaIssue issue = new BugzillaIssue(taskData, BugzillaRepository.this);
                issues.add(issue); // we don't cache this issues
                                   // - the retured taskdata are partial
                                   // - and we need an as fast return as possible at this place
            }
        }

        StringBuffer url = new StringBuffer();
        url.append(BugzillaConstants.URL_ADVANCED_BUG_LIST + "&short_desc_type=allwordssubstr&short_desc="); // NOI18N
        for (int i = 0; i < keywords.length; i++) {
            String val = keywords[i].trim();
            if(val.equals("")) continue;                                        // NOI18N
            try {
                val = URLEncoder.encode(val, getTaskRepository().getCharacterEncoding());
            } catch (UnsupportedEncodingException ueex) {
                Bugzilla.LOG.log(Level.INFO, null, ueex);
                try {
                    val = URLEncoder.encode(val, "UTF-8"); // NOI18N
                } catch (UnsupportedEncodingException ex) {
                    // should not happen
                }
            }
            url.append(val);
            if(i < keywords.length - 1) {
                url.append("+");                                                // NOI18N
            }
        }
        PerformQueryCommand queryCmd = new PerformQueryCommand(this, url.toString(), collector);
        getExecutor().execute(queryCmd);
        if(queryCmd.hasFailed()) {
            return new Issue[0];
        }
        return issues.toArray(new BugzillaIssue[issues.size()]);
    }

    @Override
    public BugtrackingController getController() {
        if(controller == null) {
            controller = new RepositoryController(this);
        }
        return controller;
    }

    @Override
    public Query[] getQueries() {
        Set<Query> l = getQueriesIntern();
        return l.toArray(new Query[l.size()]);
    }

    public IssueCache getIssueCache() {
        if(cache == null) {
            cache = new Cache();
        }
        return cache;
    }

    public void removeQuery(BugzillaQuery query) {
        BugzillaConfig.getInstance().removeQuery(this, query);
        getIssueCache().removeQuery(name);
        getQueriesIntern().remove(query);
        stopRefreshing(query);
    }

    public void saveQuery(BugzillaQuery query) {
        assert name != null;
        BugzillaConfig.getInstance().putQuery(this, query); // XXX display name ????
        getQueriesIntern().add(query);
    }

    private Set<Query> getQueriesIntern() {
        if(queries == null) {
            queries = new HashSet<Query>(10);
            String[] qs = BugzillaConfig.getInstance().getQueries(name);
            for (String queryName : qs) {
                BugzillaQuery q = BugzillaConfig.getInstance().getQuery(this, queryName);
                if(q != null ) {
                    queries.add(q);
                } else {
                    Bugzilla.LOG.warning("Couldn't find query with stored name " + queryName); // NOI18N
                }
            }
        }
        return queries;
    }

    protected void setTaskRepository(String name, String url, String user, String password, String httpUser, String httpPassword) {
        taskRepository = createTaskRepository(name, url, user, password, httpUser, httpPassword);
        Bugzilla.getInstance().addRepository(this);
        resetRepository(); // only on url, user or passwd change        
    }

    static TaskRepository createTaskRepository(String name, String url, String user, String password, String httpUser, String httpPassword) {
        TaskRepository repository = new TaskRepository(Bugzilla.getInstance().getRepositoryConnector().getConnectorKind(), url);
        AuthenticationCredentials authenticationCredentials = new AuthenticationCredentials(user, password);
        repository.setCredentials(AuthenticationType.REPOSITORY, authenticationCredentials, false);
        
        if(httpUser != null || httpPassword != null) {
            httpUser = httpUser != null ? httpUser : "";                        // NOI18N
            httpPassword = httpPassword != null ? httpPassword : "";            // NOI18N
            authenticationCredentials = new AuthenticationCredentials(httpUser, httpPassword);
            repository.setCredentials(AuthenticationType.HTTP, authenticationCredentials, false);
        }

        // XXX need proxy settings from the IDE
        
        return repository;
    }

    @Override
    public String getUrl() {
        return taskRepository != null ? taskRepository.getUrl() : null;
    }

    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
        }
        return false;
    }

    public BugzillaExecutor getExecutor() {
        if(executor == null) {
            executor = new BugzillaExecutor(this);
        }
        return executor;
    }

    public boolean authenticate(String errroMsg) {
        return BugtrackingUtil.editRepository(this, errroMsg);
    }

    private class Cache extends IssueCache {
        Cache() {
            super(BugzillaRepository.this.getUrl());
        }
        protected Issue createIssue(TaskData taskData) {
            return new BugzillaIssue(taskData, BugzillaRepository.this);
        }
        protected void setTaskData(Issue issue, TaskData taskData) {
            ((BugzillaIssue)issue).setTaskData(taskData); 
        }
    }

    /**
     * Returns the bugzilla configuration or null if not available
     * 
     * @return
     */
    public synchronized BugzillaConfiguration getConfiguration() {
        if(bc == null) {
            bc = createConfiguration();
        }
        return bc;
    }

    protected BugzillaConfiguration createConfiguration() {
        RepositoryConfiguration rc = getRepositoryConfiguration();
        if(rc != null) {
            return new BugzillaConfiguration(rc);
        }
        return null;
    }

    protected RepositoryConfiguration getRepositoryConfiguration() {
        final RepositoryConfiguration[] rc = new RepositoryConfiguration[1];
        BugzillaCommand cmd = new BugzillaCommand() {
            @Override
            public void execute() throws CoreException, IOException, MalformedURLException {
                rc[0] = Bugzilla.getInstance().getRepositoryConfiguration(BugzillaRepository.this);
            }
        };
        getExecutor().execute(cmd);
        if(!cmd.hasFailed()) {
            return rc[0];
        }
        return null;
    }

    private void setupIssueRefreshTask() {
        if(refreshIssuesTask == null) {
            refreshIssuesTask = getRefreshProcessor().create(new Runnable() {
                public void run() {
                    Set<String> ids;
                    synchronized(issuesToRefresh) {
                        ids = new HashSet<String>(issuesToRefresh);
                    }
                    if(ids.size() == 0) {
                        Bugzilla.LOG.log(Level.FINE, "no issues to refresh {0}", new Object[] {name}); // NOI18N
                        return;
                    }
                    Bugzilla.LOG.log(Level.FINER, "preparing to refresh {0} - {1}", new Object[] {name, ids}); // NOI18N
                    GetMultiTaskDataCommand cmd = new GetMultiTaskDataCommand(BugzillaRepository.this, ids, new IssuesCollector());
                    getExecutor().execute(cmd, false);
                    scheduleIssueRefresh();
                }
            });
            scheduleIssueRefresh();
        }
    }

    private void setupQueryRefreshTask() {
        if(refreshQueryTask == null) {
            refreshQueryTask = getRefreshProcessor().create(new Runnable() {
                public void run() {
                    Set<BugzillaQuery> queries;
                    synchronized(refreshQueryTask) {
                        queries = new HashSet<BugzillaQuery>(queriesToRefresh);
                    }
                    if(queries.size() == 0) {
                        Bugzilla.LOG.log(Level.FINE, "no queries to refresh {0}", new Object[] {name}); // NOI18N
                        return;
                    }
                    for (BugzillaQuery q : queries) {
                        Bugzilla.LOG.log(Level.FINER, "preparing to refresh query {0} - {1}", new Object[] {q.getDisplayName(), name}); // NOI18N
                        QueryController qc = q.getController();
                        qc.autoRefresh();
                    }

                    scheduleQueryRefresh();
                }
            });
            scheduleQueryRefresh();
        }
    }

    private void scheduleIssueRefresh() {
        int delay = BugzillaConfig.getInstance().getIssueRefreshInterval();
        Bugzilla.LOG.log(Level.FINE, "scheduling issue refresh for repository {0} in {1} minute(s)", new Object[] {name, delay}); // NOI18N
        refreshIssuesTask.schedule(delay * 60 * 1000); // given in minutes
    }

    private void scheduleQueryRefresh() {
        int delay = BugzillaConfig.getInstance().getQueryRefreshInterval();
        Bugzilla.LOG.log(Level.FINE, "scheduling query refresh for repository {0} in {1} minute(s)", new Object[] {name, delay}); // NOI18N
        refreshQueryTask.schedule(delay * 60 * 1000); // given in minutes
    }

    public void scheduleForRefresh(String id) {
        Bugzilla.LOG.log(Level.FINE, "scheduling issue {0} for refresh on repository {0}", new Object[] {id, name}); // NOI18N
        synchronized(issuesToRefresh) {
            issuesToRefresh.add(id);
        }
        setupIssueRefreshTask();
    }

    public void stopRefreshing(String id) {
        Bugzilla.LOG.log(Level.FINE, "removing issue {0} from refresh on repository {1}", new Object[] {id, name}); // NOI18N
        synchronized(issuesToRefresh) {
            issuesToRefresh.remove(id);
        }
    }

    public void scheduleForRefresh(BugzillaQuery query) {
        Bugzilla.LOG.log(Level.FINE, "scheduling query {0} for refresh on repository {1}", new Object[] {query.getDisplayName(), name}); // NOI18N
        synchronized(queriesToRefresh) {
            queriesToRefresh.add(query);
        }
        setupQueryRefreshTask();
    }

    public void stopRefreshing(BugzillaQuery query) {
        Bugzilla.LOG.log(Level.FINE, "removing query {0} from refresh on repository {1}", new Object[] {query.getDisplayName(), name}); // NOI18N
        synchronized(queriesToRefresh) {
            queriesToRefresh.remove(query);
        }
    }

    public void refreshAllQueries() {
        Query[] qs = getQueries();
        for (Query q : qs) {
            Bugzilla.LOG.log(Level.FINER, "preparing to refresh query {0} - {1}", new Object[] {q.getDisplayName(), name}); // NOI18N
            QueryController qc = ((BugzillaQuery) q).getController();
            qc.onRefresh();
        }
    }

    private class IssuesCollector extends TaskDataCollector {
        public void accept(TaskData taskData) {
            String id = BugzillaIssue.getID(taskData);
            Bugzilla.LOG.log(Level.FINE, "refreshed issue {0} - {1}", new Object[] {name, id}); // NOI18N
            try {
                getIssueCache().setIssueData(id, taskData);
            } catch (IOException ex) {
                Bugzilla.LOG.log(Level.SEVERE, null, ex);
                return;
            }
        }
    };

    private RequestProcessor getRefreshProcessor() {
        if(refreshProcessor == null) {
            refreshProcessor = new RequestProcessor("Bugzilla refresh - " + name); // NOI18N
        }
        return refreshProcessor;
    }

    @Override
    public String toString() {
        return super.toString() + " (" + getDisplayName() + ')';        //NOI18N
    }

}
