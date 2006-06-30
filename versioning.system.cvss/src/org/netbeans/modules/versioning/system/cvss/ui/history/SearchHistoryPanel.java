/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.history;

import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.awt.Mnemonics;
import org.netbeans.modules.versioning.system.cvss.util.NoContentPanel;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ui.actions.diff.DiffSetupSource;
import org.netbeans.modules.versioning.system.cvss.ui.actions.diff.Setup;
import org.netbeans.lib.cvsclient.command.log.LogInformation;

import javax.swing.*;
import java.io.File;
import java.util.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;

/**
 * Contains all components of the Search History panel.
 *
 * @author Maros Sandor
 */
class SearchHistoryPanel extends javax.swing.JPanel implements ExplorerManager.Provider, PropertyChangeListener, ActionListener, DiffSetupSource {

    private final File[]                roots;
    private final SearchCriteriaPanel   criteria;
    
    private Divider                 divider;
    private Action                  searchAction;
    private SearchExecutor          currentSearch;
    private RequestProcessor.Task   currentSearchTask;

    private boolean                 criteriaVisible;
    private boolean                 searchInProgress;
    private List                    results;
    private List                    dispResults;
    private SummaryView             summaryView;    
    private DiffResultsView         diffView;
    
    private AbstractAction nextAction;
    private AbstractAction prevAction;

    /** Creates new form SearchHistoryPanel */
    public SearchHistoryPanel(File [] roots, SearchCriteriaPanel criteria) {
        this.roots = roots;
        this.criteria = criteria;
        criteriaVisible = true;
        explorerManager = new ExplorerManager ();
        initComponents();
        setupComponents();
        refreshComponents(true);
    }

    private void setupComponents() {
        remove(jPanel1);

        divider = new Divider(this);
        java.awt.GridBagConstraints gridBagConstraints;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        add(divider, gridBagConstraints);

        searchCriteriaPanel.add(criteria);
        searchAction = new AbstractAction(NbBundle.getMessage(SearchHistoryPanel.class,  "CTL_Search")) {
            {
                putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(SearchHistoryPanel.class, "TT_Search"));
            }
            public void actionPerformed(ActionEvent e) {
                search();
            }
        };
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "search"); // NOI18N
        getActionMap().put("search", searchAction); // NOI18N
        bSearch.setAction(searchAction);
        Mnemonics.setLocalizedText(bSearch, NbBundle.getMessage(SearchHistoryPanel.class,  "CTL_Search"));
        
        Dimension d1 = tbSummary.getPreferredSize();
        Dimension d2 = tbDiff.getPreferredSize();
        if (d1.width > d2.width) {
            tbDiff.setPreferredSize(d1);
        }
        
        nextAction = new AbstractAction(null, new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/system/cvss/resources/icons/diff-next.png"))) { // NOI18N
            {
                putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/diff/Bundle").
                                                   getString("CTL_DiffPanel_Next_Tooltip"));                
            }
            public void actionPerformed(ActionEvent e) {
                diffView.onNextButton();
            }
        };
        prevAction = new AbstractAction(null, new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/system/cvss/resources/icons/diff-prev.png"))) { // NOI18N
            {
                putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/diff/Bundle").
                                                   getString("CTL_DiffPanel_Prev_Tooltip"));                
            }
            public void actionPerformed(ActionEvent e) {
                diffView.onPrevButton();
            }
        };
        bNext.setAction(nextAction);
        bPrev.setAction(prevAction);

        getActionMap().put("jumpNext", nextAction); // NOI18N
        getActionMap().put("jumpPrev", prevAction); // NOI18N
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getID() == Divider.DIVIDER_CLICKED) {
            criteriaVisible = !criteriaVisible;
            refreshComponents(false);
        }
    }

    private ExplorerManager             explorerManager;

    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, this);
            if (tc == null) return;
            tc.setActivatedNodes((Node[]) evt.getNewValue());
        }
    }

    public void addNotify() {
        super.addNotify();
        explorerManager.addPropertyChangeListener(this);
    }

    public void removeNotify() {
        explorerManager.removePropertyChangeListener(this);
        super.removeNotify();
    }
    
    public ExplorerManager getExplorerManager () {
        return explorerManager;
    }
    
    final void refreshComponents(boolean refreshResults) {
        if (refreshResults) {
            resultsPanel.removeAll();
            if (results == null) {
                if (searchInProgress) {
                    resultsPanel.add(new NoContentPanel(NbBundle.getMessage(SearchHistoryPanel.class, "LBL_SearchHistory_Searching")));
                } else {
                    resultsPanel.add(new NoContentPanel(NbBundle.getMessage(SearchHistoryPanel.class, "LBL_SearchHistory_NoResults")));
                }
            } else {
                if (tbSummary.isSelected()) {
                    if (summaryView == null) {
                        summaryView = new SummaryView(this, dispResults);
                    }
                    resultsPanel.add(summaryView.getComponent());
                } else {
                    if (diffView == null) {
                        diffView = new DiffResultsView(this, dispResults);
                    }
                    resultsPanel.add(diffView.getComponent());
                }
            }
            resultsPanel.revalidate();
            resultsPanel.repaint();
        }
        nextAction.setEnabled(!tbSummary.isSelected() && diffView != null && diffView.isNextEnabled());
        prevAction.setEnabled(!tbSummary.isSelected() && diffView != null && diffView.isPrevEnabled());

        divider.setArrowDirection(criteriaVisible ? Divider.UP : Divider.DOWN);
        searchCriteriaPanel.setVisible(criteriaVisible);
        bSearch.setVisible(criteriaVisible);
        revalidate();
        repaint();
    }
    
    public void setResults(List newResults) {
        setResults(newResults, false);
    }

    private void setResults(List newResults, boolean searching) {
        this.results = newResults;
        if (results != null) this.dispResults = createDisplayList(results);
        this.searchInProgress = searching;
        summaryView = null;
        diffView = null;
        refreshComponents(true);
    }
    
    public File[] getRoots() {
        return roots;
    }

    public SearchCriteriaPanel getCriteria() {
        return criteria;
    }

    private synchronized void search() {
        if (currentSearchTask != null) {
            currentSearchTask.cancel();
        }
        setResults(null, true);
        currentSearch = new SearchExecutor(this);
        currentSearchTask = RequestProcessor.getDefault().create(currentSearch);
        currentSearchTask.schedule(0);
    }
    
    private static List createDisplayList(List list) {
        List dispResults = new ArrayList();
        List results = new ArrayList(list);
        Collections.sort(results, new ByRemotePathRevisionNumberComparator());

        int n = results.size();
        if (n == 0) return dispResults;
        
        for (int i = 0; i < n; i++) {
            LogInformation.Revision revision = (LogInformation.Revision) results.get(i);
            results.set(i, new DispRevision(revision));
        }
        
        ResultsContainer currentContainer = null;
        
        currentContainer = new ResultsContainer(((DispRevision) results.get(0)).getRevision().getLogInfoHeader());
        dispResults.add(currentContainer);
        
        for (int i = 0; i < n; i++) {
            DispRevision revision = (DispRevision) results.get(i);
            if (currentContainer.getHeader() != revision.getRevision().getLogInfoHeader()) {
                if (currentContainer.getRevisions().size() < 1) {
                    dispResults.remove(currentContainer);
                    if (currentContainer.getRevisions().size() == 1) {
                        dispResults.add(currentContainer.getRevisions().get(0));
                    }
                }
                currentContainer = new ResultsContainer(revision.getRevision().getLogInfoHeader());
                dispResults.add(currentContainer);
            }
            DispRevision parent = getParentRevision(results, revision);
            if (parent != null) {
                parent.addRevision(revision);
            } else {
                currentContainer.add(revision);
            }
        }
        if (currentContainer.getRevisions().size() < 1) {
            dispResults.remove(currentContainer);
            if (currentContainer.getRevisions().size() == 1) {
                dispResults.add(currentContainer.getRevisions().get(0));
            }
        }
        return dispResults;
    }

    private static DispRevision getParentRevision(List results, DispRevision revision) {
        String number = revision.getRevision().getNumber();
        for (;;) {
            int idx = number.lastIndexOf('.', number.lastIndexOf('.') - 1);
            if (idx == -1) return null;
            number = number.substring(0, idx);
            LogInformation.Revision parentRev = revision.getRevision().getLogInfoHeader().new Revision();
            parentRev.setNumber(number);
            int index = Collections.binarySearch(results, new DispRevision(parentRev), revisionsComparator);
            if (index >= 0) return (DispRevision) results.get(index);
        }
    }
    
    private static final Comparator revisionsComparator = new ByRemotePathRevisionNumberComparator() {
        public int compare(Object o1, Object o2) {
            return super.compare(((DispRevision) o1).getRevision(), ((DispRevision) o2).getRevision());
        }
    };

    void executeSearch() {
        search();
    }

    void showDiff(DispRevision revision) {
        tbDiff.setSelected(true);
        refreshComponents(true);
        diffView.select(revision);
    }

    public void showDiff(ResultsContainer container) {
        tbDiff.setSelected(true);
        refreshComponents(true);
        diffView.select(container);
    }

    /**
     * Return diff setup describing shown history.
     * It return empty collection on non-atomic
     * revision ranges. XXX move this logic to clients?
     */
    public Collection getSetups() {
        if (dispResults == null) {
            return Collections.EMPTY_SET;
        }
        List setups = new ArrayList(dispResults.size());
        Iterator it = dispResults.iterator();
        while (it.hasNext()) {
            ResultsContainer entry = (ResultsContainer) it.next();
            File file = entry.getHeader().getFile();

            boolean atomicRange = true;
            String prev = null;
            List revisions = entry.getRevisions();
            Iterator revs = revisions.iterator();
            while (revs.hasNext()) {
                DispRevision revision = (DispRevision) revs.next();
                String rev = revision.getRevision().getNumber();
                if (prev != null) {
                    if (prev.equals(Utils.previousRevision(rev)) == false) {
                        atomicRange = false;
                        break;
                    }
                }
                prev = rev;
            }

            if (atomicRange == false) {
                return Collections.EMPTY_SET;
            }

            String eldest = entry.getEldestRevision();
            String newest = entry.getNewestRevision();
            Setup setup = new Setup(file, eldest, newest);
            setups.add(setup);
        }
        return setups;
    }

    public String getSetupDisplayName() {
        return null;
    }

    static class ResultsContainer {
        
        private List revisions = new ArrayList(2);
        private String name;
        private String path;
        private final LogInformation header;

        public ResultsContainer(LogInformation header) {
            this.header = header;
            File file = header.getFile();
            try {
                name = CvsVersioningSystem.getInstance().getAdminHandler().getRepositoryForDirectory(file.getParentFile().getAbsolutePath(), "") + "/" + file.getName(); // NOI18N
            } catch (Exception e) {
                name = header.getRepositoryFilename();
                if (name.endsWith(",v")) name = name.substring(0, name.lastIndexOf(",v")); // NOI18N
            }
            path = name.substring(0, name.lastIndexOf('/'));
            name = name.substring(path.length() + 1); 
        }

        public LogInformation getHeader() {
            return header;
        }

        public void add(DispRevision revision) {
            revisions.add(revisions.size(), revision);
        }
        
        public String getName() {
            return name;
        }

        public List getRevisions() {
            return revisions;
        }

        public String getEldestRevision() {
            DispRevision rev = (DispRevision) revisions.get(revisions.size() - 1);
            return Utils.previousRevision(rev.getRevision().getNumber());
        }

        public String getNewestRevision() {
            return ((DispRevision) revisions.get(0)).getRevision().getNumber();
        }
        
        public String getPath() {
            return path;
        }

        public File getFile() {
            return header.getFile();
        }

        /** Goes into clipboard */
        public String toString() {
            return getName() + "    " + getPath(); // NOI18N
        }
    }

    static class DispRevision {
        
        private final LogInformation.Revision revision;
        private String name;
        private List  children;
        private String path;
        private int indentation;

        public DispRevision(LogInformation.Revision revision) {
            this.revision = revision;
            File file = revision.getLogInfoHeader().getFile();
            try {
                name = CvsVersioningSystem.getInstance().getAdminHandler().getRepositoryForDirectory(file.getParentFile().getAbsolutePath(), "") + "/" + file.getName(); // NOI18N
            } catch (Exception e) {
                name = revision.getLogInfoHeader().getRepositoryFilename();
                if (name.endsWith(",v")) name = name.substring(0, name.lastIndexOf(",v")); // NOI18N
            }
            path = name.substring(0, name.lastIndexOf('/'));            
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }

        public LogInformation.Revision getRevision() {
            return revision;
        }

        public void addRevision(DispRevision revision) {
            if (children == null) {
                children = new ArrayList();
            }
            children.add(revision);
        }

        public List getChildren() {
            return children;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DispRevision)) return false;
            return revision.equals(((DispRevision) o).revision);
        }

        public int hashCode() {
            return revision.hashCode();
        }

        public int getIndentation() {
            return indentation;
        }
        
        public void setIndentation(int indentation) {
            this.indentation = indentation;
        }

        /** Goes into clipboard */
        public String toString() {
            StringBuffer indent = new StringBuffer("  ");  // NOI18N
            for (int i = 0; i<getIndentation(); i++) {
                indent.append("  "); // NOI18N
            }
            StringBuffer text = new StringBuffer();
            text.append(indent).append(getRevision().getNumber()).append("\t").append(getRevision().getDateString()).append(" ").append(getRevision().getAuthor()); // NOI18N
            text.append("\n"); // NOI18N
            text.append(getRevision().getMessage());

            return text.toString();
        }

    }

    /**
     * Sorts found commits by 1. Filename 2. revision number
     */ 
    private static class ByRemotePathRevisionNumberComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            LogInformation.Revision r1 = (LogInformation.Revision) o1;
            LogInformation.Revision r2 = (LogInformation.Revision) o2;
            int namec = r1.getLogInfoHeader().getFile().getName().compareToIgnoreCase(r2.getLogInfoHeader().getFile().getName());
            if (namec != 0) return namec;
            namec = r1.getLogInfoHeader().getRepositoryFilename().compareToIgnoreCase(r2.getLogInfoHeader().getRepositoryFilename());
            if (namec != 0) return namec;
            return compareRevisions(r1.getNumber(), r2.getNumber());
        }
    }
    
    public static int compareRevisions(String r1, String r2) {
        StringTokenizer st1 = new StringTokenizer(r1, "."); // NOI18N
        StringTokenizer st2 = new StringTokenizer(r2, "."); // NOI18N
        for (;;) {
            if (!st1.hasMoreTokens()) {
                return st2.hasMoreTokens() ? -1 : 0;
            }
            if (!st2.hasMoreTokens()) {
                return st1.hasMoreTokens() ? 1 : 0;
            }
            int n1 = Integer.parseInt(st1.nextToken());
            int n2 = Integer.parseInt(st2.nextToken());
            if (n1 != n2) return n2 - n1;
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        searchCriteriaPanel = new javax.swing.JPanel();
        bSearch = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        tbSummary = new javax.swing.JToggleButton();
        tbDiff = new javax.swing.JToggleButton();
        jSeparator2 = new javax.swing.JSeparator();
        bNext = new javax.swing.JButton();
        bPrev = new javax.swing.JButton();
        resultsPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 0, 8));
        searchCriteriaPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        add(searchCriteriaPanel, gridBagConstraints);

        bSearch.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("TT_Search"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(bSearch, gridBagConstraints);

        jPanel1.setPreferredSize(new java.awt.Dimension(10, 6));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(jPanel1, gridBagConstraints);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        buttonGroup1.add(tbSummary);
        tbSummary.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(tbSummary, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("CTL_ShowSummary"));
        tbSummary.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("TT_Summary"));
        tbSummary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onViewToggle(evt);
            }
        });

        jToolBar1.add(tbSummary);

        buttonGroup1.add(tbDiff);
        org.openide.awt.Mnemonics.setLocalizedText(tbDiff, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("CTL_ShowDiff"));
        tbDiff.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("TT_ShowDiff"));
        tbDiff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onViewToggle(evt);
            }
        });

        jToolBar1.add(tbDiff);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator2.setMaximumSize(new java.awt.Dimension(2, 32767));
        jToolBar1.add(jSeparator2);

        bNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/system/cvss/resources/icons/diff-next.png")));
        jToolBar1.add(bNext);
        bNext.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("ACSN_NextDifference"));

        bPrev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/system/cvss/resources/icons/diff-prev.png")));
        jToolBar1.add(bPrev);
        bPrev.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("ACSN_PrevDifference"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jToolBar1, gridBagConstraints);

        resultsPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        add(resultsPanel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void onViewToggle(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onViewToggle
        refreshComponents(true);
    }//GEN-LAST:event_onViewToggle
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bNext;
    private javax.swing.JButton bPrev;
    private javax.swing.JButton bSearch;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JPanel resultsPanel;
    private javax.swing.JPanel searchCriteriaPanel;
    private javax.swing.JToggleButton tbDiff;
    private javax.swing.JToggleButton tbSummary;
    // End of variables declaration//GEN-END:variables
    
}
