/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.kenai.ui.spi;

import java.awt.event.ActionListener;
import java.util.List;

/**
 * Main access point to Kenai's Query&Issues API.
 * All methods except those returning an Action or ActionListener are allowed
 * to block indefinetely as they will be called outside AWT thread.
 * However the Dashboard UI may declare appropriate service(s) as unreachable
 * after some configurable time out interval.
 *
 * @author S. Aubrecht
 */
public abstract class QueryAccessor {

    /**
     * Retrieve the list of queries defined for given project.
     * @param project
     * @return
     */
    public abstract List<QueryHandle> getQueries( ProjectHandle project );

    /**
     * Execute given query and retrieve the results.
     * @param query
     * @return
     */
    public abstract List<QueryResultHandle> getQueryResults( QueryHandle query );


    /**
     *
     * @param project
     * @return Action to invoke when user clicks 'Find Issue...' button.
     */
    public abstract ActionListener getFindIssueAction( ProjectHandle project );

    /**
     *
     * @param result
     * @return Action to invoke when user clicks given query result link.
     */
    public abstract ActionListener getOpenQueryResultAction( QueryResultHandle result );

    /**
     *
     * @param query
     * @return Action to invoke when user pressed Enter key on given query line.
     */
    public abstract ActionListener getDefaultAction( QueryHandle query );

    /**
     * Notify listeners registered in given Project that the list of project queries
     * has changed.
     * 
     * @param project
     * @param newQueryList
     */
    protected final void fireQueryListChanged( ProjectHandle project, List<QueryHandle> newQueryList ) {
        project.firePropertyChange(ProjectHandle.PROP_QUERY_LIST, null, newQueryList);
    }
}
