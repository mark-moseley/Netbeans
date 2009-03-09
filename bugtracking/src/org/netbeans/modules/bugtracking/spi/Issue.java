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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.ui.issue.IssueTopComponent;

/**
 * Represens a bugtracking Issue
 *
 * @author Tomas Stupka
 */
public abstract class Issue {

    private final PropertyChangeSupport support;
    
    /**
     * Seen property id
     */
    public static String LABEL_NAME_SEEN = "issue.seen";
    
    /**
     * Recetn Changes property id
     */
    public static String LABEL_RECENT_CHANGES = "issue.recent_changes";

    /**
     * issue data were changed
     */
    public static final String EVENT_ISSUE_DATA_CHANGED = "issue.data_changed";

    /**
     * issues seen state changed
     */
    public static final String EVENT_ISSUE_SEEN_CHANGED = "issue.seen_changed";

    /**
     * No information available
     */
    public static final int ISSUE_STATUS_UNKNOWN        = 0;

    /**
     * Issue was seen
     */
    public static final int ISSUE_STATUS_SEEN           = 2;

    /**
     * Issue wasn't seen yet
     */
    public static final int ISSUE_STATUS_NEW            = 4;

    /**
     * Issue was remotely modified since the last time it was seen
     */
    public static final int ISSUE_STATUS_MODIFIED       = 8;

    /**
     * Seen, New or Modified
     */
    public static final int ISSUE_STATUS_ALL   =
                                ISSUE_STATUS_NEW |
                                ISSUE_STATUS_MODIFIED |
                                ISSUE_STATUS_SEEN;
    /**
     * New or modified
     */
    public static final int ISSUE_STATUS_NOT_SEEN   =
                                ISSUE_STATUS_NEW |
                                ISSUE_STATUS_MODIFIED;

    private Repository repository;
    
    /**
     * Creates an issue
     */
    public Issue(Repository repository) {
        support = new PropertyChangeSupport(this);
        this.repository = repository;
    }

    /**
     * Returns this issues display name
     * @return
     */
    public abstract String getDisplayName();

    /**
     * Returns this issues tooltip
     * @return
     */
    public abstract String getTooltip();

    /**
     * Refreshes this Issues data from its bugtracking repositry
     */
    public abstract void refresh();


    // XXX throw exception
    public abstract void addComment(String comment, boolean closeAsFixed);

    // XXX throw exception; attach Patch or attachFile?
    public abstract void attachPatch(File file, String description);

    /**
     * Returns this issues controller
     * @return
     */
    public abstract BugtrackingController getController();

    /**
     * Opens this issue in the IDE
     */
    final public void open() {
        final ProgressHandle handle = ProgressHandleFactory.createHandle("Opening Issue..." + getID());
        BugtrackingManager.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                handle.start();
                try {
                    try {
                        Issue.this.refresh();
                        Issue.this.setSeen(true);
                    } catch (IOException ex) {
                        BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            final IssueTopComponent tc = IssueTopComponent.find(Issue.this);
                            tc.open();
                            tc.requestActive();
                        }
                    });
                } finally {
                    handle.finish();
                }
            }
        });
    }

    /**
     * Returns a NOde representing this issue
     * @return
     */
    public abstract IssueNode getNode();

    /**
     * Returns this issues unique ID
     * @return
     */
    public abstract String getID();

    /**
     * Returns this issues summary
     * @return
     */
    public abstract String getSummary();

    /**
     * 
     */
    public abstract String getRecentChanges();

    /**
     * Returns true if issue was already seen or marked as seen by the user
     * @return
     */
    public boolean wasSeen() {
        return repository.getCache().wasSeen(getID());
    }

    /**
     * Sets the seen flag
     * @param seen
     */
    public void setSeen(boolean seen) throws IOException {
        boolean oldValue = wasSeen();
        repository.getCache().setSeen(getID(), seen);
        fireSeenChanged(oldValue, seen);
    }

    public abstract Map<String, String> getAttributes();

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    protected void fireDataChanged() {
        support.firePropertyChange(EVENT_ISSUE_DATA_CHANGED, null, null);
    }

    protected void fireSeenChanged(boolean oldSeen, boolean newSeen) {
        support.firePropertyChange(EVENT_ISSUE_SEEN_CHANGED, oldSeen, newSeen);
    }
}
