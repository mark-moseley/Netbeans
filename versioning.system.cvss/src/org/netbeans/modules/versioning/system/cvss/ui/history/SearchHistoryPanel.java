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

import org.openide.util.RequestProcessor;

import javax.swing.*;
import java.awt.Dimension;
import java.io.File;

/**
 * Contains all components of the Search History panel.
 *
 * @author Maros Sandor
 */
class SearchHistoryPanel extends javax.swing.JPanel {

    private final File[]                roots;
    private final SearchCriteriaPanel   criteria;
    
    private SearchExecutor          currentSearch;
    private RequestProcessor.Task   currentSearchTask;

    /** Creates new form SearchHistoryPanel */
    public SearchHistoryPanel(File [] roots, SearchCriteriaPanel criteria) {
        this.roots = roots;
        this.criteria = criteria;
        initComponents();
        setupComponents();
    }

    private void setupComponents() {
        int maxWidth = tbSummary.getPreferredSize().width;
        if (tbDiff.getPreferredSize().width > maxWidth) maxWidth = tbDiff.getPreferredSize().width; 
        if (bSearch.getPreferredSize().width > maxWidth) maxWidth = bSearch.getPreferredSize().width;
        tbSummary.setPreferredSize(new Dimension(maxWidth, tbSummary.getPreferredSize().height));
        tbDiff.setPreferredSize(new Dimension(maxWidth, tbDiff.getPreferredSize().height));
        bSearch.setPreferredSize(new Dimension(maxWidth, bSearch.getPreferredSize().height));
        tbSummary.setSelected(true);
    }

    public void setTopPanel(JComponent panel) {
        searchCriteriaPanel.removeAll();
        searchCriteriaPanel.add(panel);
        revalidate();
    }

    private synchronized void search() {
        if (currentSearchTask != null) {
            currentSearchTask.cancel();
        }
        currentSearch = new SearchExecutor(roots, criteria);
        currentSearchTask = RequestProcessor.getDefault().create(currentSearch);
        currentSearchTask.schedule(0);
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
        jSeparator1 = new javax.swing.JSeparator();
        tbSummary = new javax.swing.JToggleButton();
        tbDiff = new javax.swing.JToggleButton();
        bNext = new javax.swing.JButton();
        bPrev = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        resultsPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 0, 11)));
        searchCriteriaPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        add(searchCriteriaPanel, gridBagConstraints);

        bSearch.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("CTL_Search"));
        bSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onSearch(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(bSearch, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        add(jSeparator1, gridBagConstraints);

        buttonGroup1.add(tbSummary);
        tbSummary.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("CTL_ShowSummary"));
        tbSummary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onViewToggle(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        add(tbSummary, gridBagConstraints);

        buttonGroup1.add(tbDiff);
        tbDiff.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("CTL_ShowDiff"));
        tbDiff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onViewToggle(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(tbDiff, gridBagConstraints);

        bNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/system/cvss/resources/icons/diff-next.png")));
        bNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onNext(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(bNext, gridBagConstraints);

        bPrev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/system/cvss/resources/icons/diff-prev.png")));
        bPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onPrev(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        add(bPrev, gridBagConstraints);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        add(jSeparator2, gridBagConstraints);

        resultsPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(resultsPanel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void onPrev(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onPrev
// TODO add your handling code here:
    }//GEN-LAST:event_onPrev

    private void onNext(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onNext
// TODO add your handling code here:
    }//GEN-LAST:event_onNext

    private void onViewToggle(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onViewToggle
// TODO add your handling code here:
    }//GEN-LAST:event_onViewToggle

    private void onSearch(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onSearch
        search();
    }//GEN-LAST:event_onSearch
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bNext;
    private javax.swing.JButton bPrev;
    private javax.swing.JButton bSearch;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JPanel resultsPanel;
    private javax.swing.JPanel searchCriteriaPanel;
    private javax.swing.JToggleButton tbDiff;
    private javax.swing.JToggleButton tbSummary;
    // End of variables declaration//GEN-END:variables
    
}
