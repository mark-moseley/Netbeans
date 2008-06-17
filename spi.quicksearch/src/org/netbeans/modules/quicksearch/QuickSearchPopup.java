/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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


package org.netbeans.modules.quicksearch;

import java.awt.Dimension;
import javax.swing.JList;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.quicksearch.recent.RecentSearches;
import org.netbeans.modules.quicksearch.ResultsModel.ItemResult;

/**
 * Component representing drop down for quick search
 * @author  Jan Becicka
 */
public class QuickSearchPopup extends javax.swing.JPanel implements ListDataListener {
    
    private QuickSearchComboBar comboBar;
    
    private ResultsModel rModel;

    /** Creates new form SilverPopup */
    public QuickSearchPopup (QuickSearchComboBar comboBar) {
        this.comboBar = comboBar;
        initComponents();
        rModel = ResultsModel.getInstance();
        jList1.setModel(rModel);
        jList1.setCellRenderer(new SearchResultRender());
        rModel.addListDataListener(this);
    }

    void invoke() {
        ItemResult result = ((ItemResult) jList1.getModel().getElementAt(jList1.getSelectedIndex()));
        if (result != null) {
            RecentSearches.getDefault().add(result);
            result.getAction().run();
        }
    }

    void selectNext() {
        jList1.setSelectedIndex(jList1.getSelectedIndex()+1);
    }
    
    void selectPrev() {
        jList1.setSelectedIndex(jList1.getSelectedIndex()-1);
    }

    public JList getList() {
        return jList1;
    }
    
    void update(String text) {
        // TBD - fast coming evaluation requests coalescing
        CommandEvaluator.evaluate(text, rModel);
    }
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();

        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane1.setViewportView(jList1);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    
    /*** impl of reactions to results data change */
    
    public void intervalAdded(ListDataEvent e) {
        updatePopup();
    }

    public void intervalRemoved(ListDataEvent e) {
        updatePopup();
    }

    public void contentsChanged(ListDataEvent e) {
        updatePopup();
    }

    /**
     * Runnable implementation, updates popup.
     * This "updatePopup" is invoked using SwingUtilities.invokeLater to let
     * JList compute its preferred size well. 
     */
    private void updatePopup() {
        JWindow popup = comboBar.getPopup();
        if (popup == null) {
            return;
        }
        
        int modelSize = rModel.getSize();
        if (modelSize > 0) {
            // hack to make jList.getpreferredSize work correctly
            // JList is listening on ResultsModel same as us and order of listeners
            // is undefined, so we have to force update of JList's layout data            
            jList1.setFixedCellHeight(15);
            jList1.setFixedCellHeight(-1);
            // end of hack
            jList1.setVisibleRowCount(modelSize);
            Dimension preferredSize = jList1.getPreferredSize();
            popup.setSize(preferredSize.width + 3, preferredSize.height + 3);
            if (!popup.isVisible()) {
                jList1.setSelectedIndex(0);
                popup.setVisible(true);
                comboBar.getCommand().requestFocus();
            }
        } else {
            popup.setVisible(false);
        }
    }

}
