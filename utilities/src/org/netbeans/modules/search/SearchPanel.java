/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.search;


import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.netbeans.modules.search.types.FullTextType;
import org.netbeans.modules.search.types.ModificationDateType;
import org.netbeans.modules.search.types.ObjectNameType;
import org.netbeans.modules.search.types.ObjectTypeType;

import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openidex.search.SearchType;


/**
 * Panel which shows all enabled search types for user allowing her to
 * select appropriate criteria for new search.
 *
 * @author  Peter Zavadsky
 * @see SearchTypePanel
 */
public class SearchPanel extends JPanel implements PropertyChangeListener {

    /** Return status code - returned if Cancel button has been pressed. */
    public static final int RET_CANCEL = 0;
    
    /** Return status code - returned if OK button has been pressed. */
    public static final int RET_OK = 1;

    /** Dialog descriptor. */
    private DialogDescriptor dialogDescriptor;

    /** OK button. */
    private JButton okButton;
    
    /** Cancel button. */
    private JButton cancelButton;

    /** Java equivalent. */
    private Dialog dialog;

    /** Return status. */
    private int returnStatus = RET_CANCEL;

    /** Ordered list of <code>SearchTypePanel</code>'s. */
    private List orderedSearchTypePanels;

    /** Whether some criterion is customized. */
    private boolean customized;
    
    
    /** Creates new <code>SearchPanel</code>.
     * @param searchTypeList list of <code>SearchType</code> to use */
    public SearchPanel(List searchTypeList) {
        this(searchTypeList, false);
    }
    
    /** Creates new <code>SearchPanel</code>. 
     * @param searchTypeList list of <code>SearchType</code> to use 
     * @param isCustomized sets customized flag indicating there is
     * at least one from <code>SearchType</code>s already set and
     * seach - okButton should be enabled */
    public SearchPanel(List searchTypeList, boolean isCustomized) {
        this.orderedSearchTypePanels = new ArrayList(searchTypeList.size());
        this.customized = isCustomized;

        // Default values of criterions.
        Iterator it = searchTypeList.iterator();

        while(it.hasNext()) {
            SearchType searchType = (SearchType)it.next();

            SearchTypePanel searchTypePanel = new SearchTypePanel(searchType);
            
            if(orderedSearchTypePanels.contains(searchTypePanel)) {
                continue;
            }
            
            searchTypePanel.addPropertyChangeListener(this);
                
            orderedSearchTypePanels.add(searchTypePanel);
        }
        
        initComponents();

        // For each search type create one tab as its search type panel.
        it = orderedSearchTypePanels.iterator();

        while(it.hasNext()) {
            tabbedPane.add((Component)it.next());
        }

        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabbedPaneStateChanged(evt);
            }
        });
        
        setName(NbBundle.getBundle(SearchPanel.class).getString("TEXT_TITLE_CUSTOMIZE")); // NOI18N

        okButton = new JButton(NbBundle.getBundle(SearchPanel.class).getString("TEXT_BUTTON_SEARCH")); // NOI18N
        okButton.setEnabled(isCustomized());

        cancelButton = new JButton(NbBundle.getBundle(SearchPanel.class).getString("TEXT_BUTTON_CANCEL")); // NOI18N

        Object options[] = new Object[] {okButton, cancelButton};

        // Creates representing dialog descriptor.
        dialogDescriptor = new DialogDescriptor(
            this, 
            getName(), 
            true, 
            options, 
            options[0],
            DialogDescriptor.BOTTOM_ALIGN, 
            getHelpCtx(),
            new ActionListener() {
                public void actionPerformed(final ActionEvent evt) {
                    if(evt.getSource() == okButton) {
                        doClose(RET_OK);
                    } else {
                        doClose(RET_CANCEL);
                    }
                }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        tabbedPane = new javax.swing.JTabbedPane();

        setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(tabbedPane, gridBagConstraints);

    }//GEN-END:initComponents

    private void tabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {
        Component component = getTypeCustomizer(tabbedPane.getSelectedIndex());
        if(component != null)
            component.requestFocus();
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables

    /** @return true if some criterion customized. */
    public boolean isCustomized() {
        return customized;
    }
    
    /** Gets ordered criterion panels.
     * @return iterator over properly ordered <code>SearchTypePanel</code>'s. */
    private List getOrderedSearchTypePanels() {
        return new ArrayList(orderedSearchTypePanels);
    }

    /** @return name of criterion at index is modified. */
    private String getTabText(int index) {
        try {
            return ((SearchTypePanel)getOrderedSearchTypePanels().get(index)).getName(); 
        } catch (ArrayIndexOutOfBoundsException ex) {
            return null;
        }
    }

    /** Gets array of customized search types. 
     * @return current state of customized search types. */
    public SearchType[] getCustomizedSearchTypes() {
        
        List searchTypeList = new ArrayList(orderedSearchTypePanels.size());
        
        for(Iterator it = orderedSearchTypePanels.iterator(); it.hasNext(); ) {
            SearchTypePanel searchTypePanel = (SearchTypePanel)it.next(); 
            
            if(searchTypePanel.isCustomized())
                searchTypeList.add(searchTypePanel.getSearchType());
        }
        
        return (SearchType[])searchTypeList.toArray(new SearchType[searchTypeList.size()]);
    }
    
    /** Getter for return status property. 
     * @return the return status of this dialog - one of RET_OK or RET_CANCEL */
    public int getReturnStatus () {
        return returnStatus;
    }

    /** Closes dialog. */
    private void doClose(int returnStatus) {
        this.returnStatus = returnStatus;

        dialog.setVisible(false);
        dialog.dispose();
    }

    /** Shows dialog created from <code>DialogDescriptor</code> which wraps this instance. */
    public void showDialog()  {
        dialog = TopManager.getDefault().createDialog(dialogDescriptor);
        dialog.setModal(true);
        dialog.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent evt) {
                int selectedIndex = tabbedPane.getSelectedIndex();
                if(selectedIndex < 0) selectedIndex = 0;
                
                Component component = getTypeCustomizer(selectedIndex);
                if (component != null)
                    component.requestFocus();
                dialog.removeComponentListener(this);
            }
        });
        
        dialog.pack();
        dialog.show();
    }

    /** Implements <code>PropertyChangeListener</code> interface. */
    public void propertyChange(PropertyChangeEvent event) {
        if(SearchTypePanel.PROP_CUSTOMIZED.equals(event.getPropertyName())) {
            customized = getCustomizedSearchTypes().length != 0;            
            
            okButton.setEnabled(isCustomized());
        }

        for(int i = 0; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setTitleAt(i, getTabText(i));
            tabbedPane.setIconAt(i, null);
        }
    }

   
    /** Gets help context. */
    private HelpCtx getHelpCtx() {
        int index = tabbedPane.getModel().getSelectedIndex(); 
        SearchTypePanel panel = (SearchTypePanel)getOrderedSearchTypePanels().get(index);
        
        return panel.getHelpCtx();
    }    
    
    /** Gets type of customizer. 
     * @param index index of tab we need. */
    private Component getTypeCustomizer(int index) {
        SearchTypePanel searchTypePanel = null; 
        
        Iterator it = getOrderedSearchTypePanels().iterator();
        while(index >= 0 && it.hasNext()) {
            searchTypePanel = (SearchTypePanel)it.next();
            index--;
        }
        
        return searchTypePanel != null ? searchTypePanel.getComponent() : null;
    }
    
}
