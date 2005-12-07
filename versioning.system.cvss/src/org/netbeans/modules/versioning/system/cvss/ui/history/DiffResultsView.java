/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.history;

import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.util.NoContentPanel;
import org.netbeans.modules.versioning.system.cvss.ui.actions.diff.DiffStreamSource;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.Diff;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Cancellable;
import org.openide.ErrorManager;

import javax.swing.*;
import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;
import java.util.*;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.awt.*;

/**
 * Shows Search History results in a table with Diff pane below it.
 * 
 * @author Maros Sandor
 */
class DiffResultsView implements AncestorListener, PropertyChangeListener {

    private final SearchHistoryPanel parent;
    private final List              results;

    private DiffTreeTable treeView;
    private JSplitPane    diffView;
    
    private ShowDiffTask            currentTask;
    private RequestProcessor.Task   currentShowDiffTask;
    
    private DiffView                currentDiff;
    private int                     currentDifferenceIndex;
    private int                     currentIndex;
    private boolean                 dividerSet;

    public DiffResultsView(SearchHistoryPanel parent, List results) {
        this.parent = parent;
        this.results = results;
        treeView = new DiffTreeTable();
        treeView.setResults(results);
        treeView.addAncestorListener(this);

        diffView = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        diffView.setTopComponent(treeView);
        setBottomComponent(new NoContentPanel(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_NoRevisions")));
    }

    public void ancestorAdded(AncestorEvent event) {
        ExplorerManager em = ExplorerManager.find(treeView);
        em.addPropertyChangeListener(this);
        if (!dividerSet) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    dividerSet = true;
                    diffView.setDividerLocation(0.33);
                }
            });
        }
    }

    public void ancestorMoved(AncestorEvent event) {
    }

    public void ancestorRemoved(AncestorEvent event) {
        ExplorerManager em = ExplorerManager.find(treeView);
        em.removePropertyChangeListener(this);
        cancelBackgroundTasks();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            final Node [] nodes = (Node[]) evt.getNewValue();
            currentDifferenceIndex = 0;
            if (nodes.length == 0) {
                showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_NoRevisions"));
                parent.refreshComponents(false);
                return;
            }
            else if (nodes.length > 2) {
                showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_TooManyRevisions"));
                parent.refreshComponents(false);
                return;
            }

            // invoked asynchronously becase treeView.getSelection() may not be ready yet
            Runnable runnable = new Runnable() {
                public void run() {
                    SearchHistoryPanel.ResultsContainer container1 = (SearchHistoryPanel.ResultsContainer) nodes[0].getLookup().lookup(SearchHistoryPanel.ResultsContainer.class);
                    SearchHistoryPanel.DispRevision r1 = (SearchHistoryPanel.DispRevision) nodes[0].getLookup().lookup(SearchHistoryPanel.DispRevision.class);
                    try {
                        currentIndex = treeView.getSelection()[0];
                        if (nodes.length == 1) {
                            if (container1 != null) {
                                showContainerDiff(container1, onSelectionshowLastDifference);
                            }
                            else if (r1 != null) {
                                showRevisionDiff(r1, onSelectionshowLastDifference);
                            }
                        } else if (nodes.length == 2) {
                            SearchHistoryPanel.DispRevision r2 = (SearchHistoryPanel.DispRevision) nodes[1].getLookup().lookup(SearchHistoryPanel.DispRevision.class);
                            if (r2.getRevision().getLogInfoHeader() != r1.getRevision().getLogInfoHeader()) {
                                throw new Exception();
                            }
                            String revision2 = r1.getRevision().getNumber();
                            String revision1 = r2.getRevision().getNumber();
                            showDiff(r1.getRevision().getLogInfoHeader(), revision1, revision2, false);
                        }
                    } catch (Exception e) {
                        showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_IllegalSelection"));
                        parent.refreshComponents(false);
                        return;
                    }
                }
            };
            SwingUtilities.invokeLater(runnable);
        }
    }

    private void showDiffError(String s) {
        setBottomComponent(new NoContentPanel(s));
    }

    private void setBottomComponent(Component component) {
        int dl = diffView.getDividerLocation();
        diffView.setBottomComponent(component);
        diffView.setDividerLocation(dl);
    }

    private void showDiff(LogInformation header, String revision1, String revision2, boolean showLastDifference) {
        synchronized(this) {
            cancelBackgroundTasks();
            currentTask = new ShowDiffTask(header, revision1, revision2, showLastDifference);
            currentShowDiffTask = RequestProcessor.getDefault().create(currentTask);
            currentShowDiffTask.schedule(0);
        }
    }

    private synchronized void cancelBackgroundTasks() {
        if (currentShowDiffTask != null && !currentShowDiffTask.isFinished()) {
            currentShowDiffTask.cancel();  // it almost always late it's enqueued, so:
            currentTask.cancel();
        }
    }

    private boolean onSelectionshowLastDifference;

    private void setDiffIndex(int idx, boolean showLastDifference) {
        currentIndex = idx;
        onSelectionshowLastDifference = showLastDifference;
        treeView.setSelection(idx);
    }

    private void showRevisionDiff(SearchHistoryPanel.DispRevision rev, boolean showLastDifference) {
        String revision2 = rev.getRevision().getNumber();
        String revision1 = Utils.previousRevision(revision2);
        showDiff(rev.getRevision().getLogInfoHeader(), revision1, revision2, showLastDifference);
    }

    private void showContainerDiff(SearchHistoryPanel.ResultsContainer container, boolean showLastDifference) {
        List revs = container.getRevisions();
        SearchHistoryPanel.DispRevision newest = (SearchHistoryPanel.DispRevision) revs.get(0);
        showDiff(newest.getRevision().getLogInfoHeader(), container.getEldestRevision(), newest.getRevision().getNumber(), showLastDifference);
    }

    void onNextButton() {
        if (currentDiff != null) {
            if (++currentDifferenceIndex >= currentDiff.getDifferenceCount()) {
                if (++currentIndex >= treeView.getRowCount()) currentIndex = 0;
                setDiffIndex(currentIndex, false);
            } else {
                currentDiff.setCurrentDifference(currentDifferenceIndex);
            }
        } else {
            if (++currentIndex >= treeView.getRowCount()) currentIndex = 0;
            setDiffIndex(currentIndex, false);
        }
    }

    void onPrevButton() {
        if (currentDiff != null) {
            if (--currentDifferenceIndex < 0) {
                if (--currentIndex < 0) currentIndex = treeView.getRowCount() - 1;
                setDiffIndex(currentIndex, true);
            } else {
                currentDiff.setCurrentDifference(currentDifferenceIndex);
            }
        } else {
            if (--currentIndex < 0) currentIndex = treeView.getRowCount() - 1;
            setDiffIndex(currentIndex, true);
        }
    }

    boolean isNextEnabled() {
        if (currentDiff != null) {
            return currentIndex < treeView.getRowCount() - 1 || currentDifferenceIndex < currentDiff.getDifferenceCount() - 1;
        } else {
            return false;
        }
    }

    boolean isPrevEnabled() {
        return currentIndex > 0 || currentDifferenceIndex > 0;
    }
    
    /**
     * Selects given revision in the view as if done by the user.
     *
     * @param revision revision to select
     */
    void select(SearchHistoryPanel.DispRevision revision) {
        treeView.requestFocusInWindow();
        treeView.setSelection(revision);
    }

    void select(SearchHistoryPanel.ResultsContainer container) {
        treeView.requestFocusInWindow();
        treeView.setSelection(container);
    }

    private class ShowDiffTask implements Runnable, Cancellable {
        
        private final LogInformation header;
        private final String revision1;
        private final String revision2;
        private boolean showLastDifference;
        private volatile boolean cancelled;
        private Thread thread;

        public ShowDiffTask(LogInformation header, String revision1, String revision2, boolean showLastDifference) {
            this.header = header;
            this.revision1 = revision1;
            this.revision2 = revision2;
            this.showLastDifference = showLastDifference;
        }

        public void run() {
            thread = Thread.currentThread();
            final Diff diff = Diff.getDefault();
            final DiffStreamSource s1 = new DiffStreamSource(header.getFile(), revision1, revision1);
            final DiffStreamSource s2 = new DiffStreamSource(header.getFile(), revision2, revision2);

            // it's enqueued at ClientRuntime queue and does not return until previous request handled
            s1.getMIMEType();  // triggers s1.init()
            if (cancelled) {
                return;
            }

            s2.getMIMEType();  // triggers s2.init()
            if (cancelled) {
                return;
            }

            if (currentTask != this) return;

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        if (cancelled) {
                            return;
                        }
                        final DiffView view = diff.createDiff(s1, s2);
                        if (currentTask == ShowDiffTask.this) {
                            currentDiff = view;
                            setBottomComponent(currentDiff.getComponent());
                            if (currentDiff.getDifferenceCount() > 0) {
                                currentDifferenceIndex = showLastDifference ? currentDiff.getDifferenceCount() - 1 : 0;
                                currentDiff.setCurrentDifference(currentDifferenceIndex);
                            }
                            parent.refreshComponents(false);
                        }
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            });
        }

        public boolean cancel() {
            cancelled = true;
            if (thread != null) {
                thread.interrupt();
            }
            return true;
        }
    }
    
    public JComponent getComponent() {
        return diffView;
    }
}

