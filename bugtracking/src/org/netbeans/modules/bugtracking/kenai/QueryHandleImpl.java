/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.kenai;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.ui.query.QueryAction;
import org.netbeans.modules.kenai.ui.spi.QueryHandle;
import org.netbeans.modules.kenai.ui.spi.QueryResultHandle;

/**
 *
 * @author Tomas Stupka
 */
public class QueryHandleImpl extends QueryHandle implements ActionListener, PropertyChangeListener {
    private final Query query;
    private final PropertyChangeSupport changeSupport;
    private Issue[] issues = new Issue[0];

    public QueryHandleImpl(Query query) {
        this.query = query;
        changeSupport = new PropertyChangeSupport(query);
        query.addPropertyChangeListener(this);
        registerIssues();
    }

    @Override
    public String getDisplayName() {
        return query.getDisplayName();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(l);
    }

    public void actionPerformed(ActionEvent e) {
        QueryAction.openQuery(query);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Query.EVENT_QUERY_ISSUES_CHANGED)) {
            registerIssues();
            changeSupport.firePropertyChange(new PropertyChangeEvent(this, PROP_QUERY_RESULT, null, getQueryResults())); // XXX add result handles
        } else if(evt.getPropertyName().equals(Issue.EVENT_ISSUE_SEEN_CHANGED)) {
            changeSupport.firePropertyChange(new PropertyChangeEvent(this, PROP_QUERY_RESULT, null, getQueryResults())); // XXX add result handles
        }
    }

    public List<QueryResultHandle> getQueryResults() {
        List<QueryResultHandle> ret = new ArrayList<QueryResultHandle>();
        QueryResultHandle qh = QueryResultHandleImpl.forStatus(query, Issue.ISSUE_STATUS_ALL);
        if(qh != null) {
            ret.add(qh);
        }
        qh = QueryResultHandleImpl.forStatus(query, Issue.ISSUE_STATUS_NOT_SEEN);
        if(qh != null) {
            ret.add(qh);
        }
        qh = QueryResultHandleImpl.forStatus(query, Issue.ISSUE_STATUS_NEW);
        if(qh != null) {
            ret.add(qh);
        }
        return ret;
    }

    private void registerIssues() {
        for (Issue issue : issues) {
            issue.removePropertyChangeListener(this);
        }
        issues = query.getIssues(Issue.ISSUE_STATUS_ALL);
        for (Issue issue : issues) {
            issue.addPropertyChangeListener(this);
        }
    }

}
