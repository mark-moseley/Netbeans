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

package org.netbeans.modules.bugtracking.spi;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.bugtracking.util.IssueCache;

/**
 * 
 * Represents a bug tracking repository (server)
 * 
 * @author Tomas Stupka
 */
public abstract class Repository {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    /**
     * a query was saved or removed
     */
    public static String EVENT_QUERY_LIST_CHANGED = "bugtracking.repository.queries.changed";

    /**
     * Returns the icon for this repository
     * @return
     */
    public abstract Image getIcon();

    /**
     * Returns the display name for this repository
     * @return
     */
    public abstract String getDisplayName();

    /**
     * Returs the tooltip for this repository
     * @return
     */
    public abstract String getTooltip();

    /**
     * Returns the repositories url
     * @return
     */
    public abstract String getUrl();

    /**
     * Returns an issue with the given ID
     * @param id
     * @return
     */
    public abstract Issue getIssue(String id);

    /**
     * Removes this repository from its conector
     *
     */
    public abstract void remove();

    /**
     * Returns the {@link BugtrackignController} for this repository
     * @return
     */
    public abstract BugtrackingController getController();

    /**
     * Creates a new query instance.
     * @return
     */
    public abstract Query createQuery(); 

    /**
     * Creates an issue
     * @return
     */
    public abstract Issue createIssue();

    /**
     * Returns all saved queries
     * @return
     */
    public abstract Query[] getQueries();

    /**
     * Runs a query against the bugtracking repository to get all issues
     * which applies that their ID or summary contains the given criteria string
     *
     * @param criteria
     */
    public abstract Issue[] simpleSearch(String criteria);

    /**
     * Returns the {@link IssueCache} for the repository
     * @return
     */
    protected abstract IssueCache getIssueCache();

    IssueCache getCache() {
        return getIssueCache();
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    protected void fireQueryListChanged() {
        support.firePropertyChange(EVENT_QUERY_LIST_CHANGED, null, null);
    }

}
