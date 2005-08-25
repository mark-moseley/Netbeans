/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.search;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openidex.search.SearchGroup;
import org.openidex.search.SearchType;


/**
 * Task performing search.
 *
 * @author  Peter Zavadsky
 * @author  Marian Petras
 */
final class SearchTask implements Runnable, Cancellable {

    /** nodes to search */
    private final Node[] nodes;
    /** */
    private final SearchType[] customizedSearchTypes;
    /** */
    private final List searchTypeList;
    /** ResultModel result model. */
    private ResultModel resultModel;
    /** <code>SearchGroup</code> to search on. */
    private SearchGroup searchGroup;
    /**
     * listener which listens for the search group's notifications of found
     * objects
     */
    private PropertyChangeListener searchGroupListener;
    /** attribute used by class <code>Manager</code> */
    private boolean notifyWhenFinished = true;
    /** */
    private volatile boolean interrupted = false;
    /** */
    private volatile boolean finished = false;
    
    
    /**
     * Creates a new <code>SearchTask</code>.
     *
     * @param 
     * @param 
     * @param 
     */
    public SearchTask(final Node[] nodes,
                      final List searchTypeList,
                      final SearchType[] customizedSearchTypes) {
        this.nodes = nodes;
        this.searchTypeList = searchTypeList;
        this.customizedSearchTypes = customizedSearchTypes;
    }

    
    /** Runs the search task. */
    public void run() {
        
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(ResultView.class,"TEXT_SEARCHING___"), this);
        progressHandle.start();
        
        /* Start the actual search: */
        ensureResultModelExists();
        if (searchGroup == null) {
            return;
        }

        //Set of search types to be used able to search on the same object type.
        searchGroup.addPropertyChangeListener(WeakListeners.propertyChange(
            searchGroupListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (SearchGroup.PROP_FOUND.equals(evt.getPropertyName())) {
                        matchingObjectFound(evt.getNewValue());
                    }
                }
            }, searchGroup)
        );

        searchGroup.setSearchRootNodes(nodes);
        try {
            searchGroup.search();
        } catch (RuntimeException ex) {
            resultModel.searchException(ex);
        }
        finished = true;
        
        progressHandle.finish();
    }

    /**
     */
    ResultModel getResultModel() {
        ensureResultModelExists();
        return resultModel;
    }
    
    /**
     */
    private void ensureResultModelExists() {
        if (resultModel == null) {
            SearchGroup[] groups
                    = SearchGroup.createSearchGroups(customizedSearchTypes);
            searchGroup = (groups.length != 0) ? groups[0] : null;
            resultModel = new ResultModel(searchTypeList, searchGroup);
        }
    }

    /**
     * Called when a matching object is found by the <code>SearchGroup</code>.
     * Notifies the result model of the found object and stops searching
     * if number of the found objects reached the limit.
     *
     * @param  object  found matching object
     */
    private void matchingObjectFound(Object object) {
        boolean canContinue = resultModel.objectFound(object);
        if (!canContinue) {
            searchGroup.stopSearch();
        }
    }
    
    /**
     * Stops this search task.
     * This method also sets a value of attribute
     * <code>notifyWhenFinished</code>. This method may be called multiple
     * times (even if this task is already stopped) to change the value
     * of the attribute.
     *
     * @param  notifyWhenFinished  new value of attribute
     *                             <code>notifyWhenFinished</code>
     */
    void stop(boolean notifyWhenFinished) {
        if (notifyWhenFinished == false) {     //allow only change true -> false
            this.notifyWhenFinished = notifyWhenFinished;
        }
        stop();
    }
    
    /**
     * Stops this search task.
     *
     * @see  #stop(boolean)
     */
    void stop() {
        if (!finished) {
            interrupted = true;
        }
        if (searchGroup != null) {
            searchGroup.stopSearch();
        }
    }
    
    /** 
     * Cancel processing of the task. 
     *
     * @return true if the task was succesfully cancelled, false if job
     *         can't be cancelled for some reason
     * @see org.openide.util.Cancellable#cancel
     */
    public boolean cancel() {
        stop();
        return true;
    }

    /**
     * Returns value of attribute <code>notifyWhenFinished</code>.
     *
     * @return  current value of the attribute
     */
    boolean notifyWhenFinished() {
        return notifyWhenFinished;
    }
    
    /**
     * Was this search task interrupted?
     *
     * @return  <code>true</code> if this method has been interrupted
     *          by calling {@link #stop()} or {@link #stop(boolean)}
     *          during the search; <code>false</code> otherwise
     */
    boolean wasInterrupted() {
        return interrupted;
    }

}
