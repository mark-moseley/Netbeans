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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.ui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.modules.autoupdate.ui.wizards.OperationWizardModel.OperationType;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.modules.ModuleInfo;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author  Jiri Rechtacek, Radek Matous
 */
public class UnitTab extends javax.swing.JPanel {
    private UnitTable table = null;
    private UnitDetails details = null;
    private UnitCategoryTableModel model = null;
    private DocumentListener dlForSearch;
    private String filter = "";
    private PluginManagerUI manager = null;
    private PopupActionSupport popupActionsSupport;
    private RowTabAction activateAction;
    private RowTabAction deactivateAction;
    private TabAction refreshAction;
    
    private RowTabAction removeLocallyDownloaded = new RemoveLocallyDownloadedAction ();
    private PopupAction popupAction = new PopupAction();
    
    
    private static final RequestProcessor RP = new RequestProcessor ();
    private final RequestProcessor.Task searchTask = RP.create (new Runnable (){
        public void run () {
            if (filter != null) {
                model.setFilter (filter);
            }
        }
    });
    private final Logger log = Logger.getLogger ("org.netbeans.modules.autoupdate.ui.UnitTab");
    
    /** Creates new form UnitTab */
    public UnitTab (UnitTable table, UnitDetails details, PluginManagerUI manager) {
        this.table = table;
        this.details = details;
        this.manager = manager;
        TableModel m = table.getModel ();
        assert m instanceof UnitCategoryTableModel : m + " instanceof UnitCategoryTableModel.";
        this.model = (UnitCategoryTableModel) m;
        table.getSelectionModel ().setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        initComponents ();
        spTab.setLeftComponent (new JScrollPane (table));
        spTab.setRightComponent (new JScrollPane (details,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        initTab ();
        listenOnSelection ();
    }
    
    UnitCategoryTableModel getModel() {
        return model;
    }
    
    void setWaitingState (boolean waitingState) {
        boolean enabled = !waitingState;
        Component[] all = getComponents ();
        for (Component component : all) {
            if (component == bTabAction) {
                if (enabled) {
                    component.setEnabled (model.getMarkedUnits ().size () > 0);
                } else {
                    component.setEnabled(enabled);
                }                
            } else {
                if (component == spTab) {
                    spTab.getLeftComponent().setEnabled(enabled);
                    spTab.getRightComponent().setEnabled(enabled);
                    details.setEnabled(enabled);
                    table.setEnabled(enabled);
                } else {
                    component.setEnabled(enabled);                
                }
            }
        }
        if (refreshAction != null) {
            refreshAction.setEnabled (enabled);
        }
        Component parent = getParent ();
        Component rootPane = getRootPane ();
        if (parent != null) {
            parent.setEnabled (enabled);
        }
        if (rootPane != null) {
            if (enabled) {
                rootPane.setCursor (null);
            } else {
                rootPane.setCursor (Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
            }
        }
    }
    
    private void addToToolbar(Action action) {
        JButton button = new JButton(action);
        button.setToolTipText((String)action.getValue(JComponent.TOOL_TIP_TEXT_KEY));
        tbActions.add(button);
    }
            
    @Override
    public void addNotify () {
        super.addNotify ();
        if (dlForSearch == null) {
            tfSearch.getDocument ().addDocumentListener (getDocumentListener ());
        }        
    }
    
    @Override
    public void removeNotify () {
        super.removeNotify ();
        if (dlForSearch != null) {
            tfSearch.getDocument ().removeDocumentListener (getDocumentListener ());
        }
        dlForSearch = null;
    }
    
    
    public void refreshState () {
        Collection<Unit> units = model.getMarkedUnits ();
        int downloadSize = model.getDownloadSize ();
        popupActionsSupport.tableDataChanged ();
        
        if (units.size () == 0) {
            cleanSelectionInfo ();
        } else {
            setSelectionInfo (Utilities.getDownloadSizeAsString (downloadSize), units.size ());
        }
        getDefaultAction ().setEnabled (units.size () > 0);
    }
    
    public TabAction getDefaultAction () {
        return (TabAction)bTabAction.getAction ();
    }
    private void initTab () {
        TabAction[] forPopup = null;
        switch (model.getType ()) {
        case INSTALLED :
            setTabActionDescription ("UnitTab_lTabActionDescription_Text_INSTALLED");
            {
                RowTabAction checkCategoryAction = new CheckCategoryAction ();
                RowTabAction uncheckCategoryAction = new UncheckCategoryAction ();
                RowTabAction checkAllAction = new CheckAllAction ();
                RowTabAction uncheckAllAction = new UncheckAllAction ();
                RowTabAction activateCategoryAction = new ActivateCategoryAction ();
                RowTabAction deactivateCategoryAction = new DeactivateCategoryAction ();
                
                activateAction = new ActivateAction ();
                deactivateAction = new DeactivateAction ();
                
                forPopup = new TabAction[] {
                    activateAction, deactivateAction,activateCategoryAction,deactivateCategoryAction,
                    checkCategoryAction, uncheckCategoryAction,
                    checkAllAction, uncheckAllAction, new CheckAction()
                };
            }
            bTabAction.setAction (new UninstallAction ());
            addToToolbar (refreshAction = new ReloadAction ());
            table.setEnableRenderer (new EnableRenderer ());
            break;
        case UPDATE :
            setTabActionDescription ("UnitTab_lTabActionDescription_Text_UPDATE");
            {
                RowTabAction selectCategoryAction = new CheckCategoryAction ();
                RowTabAction deselectCategoryAction = new UncheckCategoryAction ();
                RowTabAction selectAllAction = new CheckAllAction ();
                RowTabAction deselectAllAction = new UncheckAllAction ();
                
                forPopup = new TabAction[] {
                    selectCategoryAction, deselectCategoryAction,
                    selectAllAction, deselectAllAction, new CheckAction()
                };
            }
            bTabAction.setAction (new UpdateAction ());
            addToToolbar (refreshAction = new ReloadAction ());
            break;
        case AVAILABLE :
            setTabActionDescription ("UnitTab_lTabActionDescription_Text_AVAILABLE");
            {
                RowTabAction selectCategoryAction = new CheckCategoryAction ();
                RowTabAction deselectCategoryAction = new UncheckCategoryAction ();
                RowTabAction selectAllAction = new CheckAllAction ();
                RowTabAction deselectAllAction = new UncheckAllAction ();
                
                forPopup = new TabAction[] {
                    selectCategoryAction, deselectCategoryAction,
                    selectAllAction, deselectAllAction, new CheckAction()
                };
            }
            bTabAction.setAction (new AvailableAction ());
            addToToolbar (refreshAction = new ReloadAction ());
            break;
        case LOCAL :
            setTabActionDescription ("UnitTab_lTabActionDescription_Text_LOCAL");
            {
                forPopup = new TabAction[] {
                    removeLocallyDownloaded, new CheckAction()
                };
            }
            bTabAction.setAction (new LocalUpdateAction ());
            addToToolbar (new AddLocallyDownloadedAction ());
            addToToolbar (removeLocallyDownloaded);
            break;
        }
        model.addTableModelListener (new TableModelListener () {
            public void tableChanged (TableModelEvent e) {
                refreshState ();
            }
        });
        table.addMouseListener (popupActionsSupport = new PopupActionSupport (forPopup));
        
        getDefaultAction ().setEnabled (model.getMarkedUnits ().size () > 0);
    }
    
    private void setTabActionDescription (String key) {
        Mnemonics.setLocalizedText (lTabActionDescription, NbBundle.getMessage (UnitTab.class, key));
    }
    
    private void setTabActionName (String key) {
        Mnemonics.setLocalizedText (bTabAction, NbBundle.getMessage (UnitTab.class, key));
    }
    
    private void cleanSelectionInfo () {
        lSelectionInfo.setText ("");
    }
    
    private void setSelectionInfo (String downloadSize, int count) {
        String key = null;
        switch (model.getType ()) {
        case INSTALLED :
            key = "UnitTab_lHowManySelected_Text_INSTALLED";
            break;
        case UPDATE :
            key = "UnitTab_lHowManySelected_Text_UPDATE";
            break;
        case AVAILABLE :
            key = "UnitTab_lHowManySelected_Text_AVAILABLE";
            break;
        case LOCAL :
            key = "UnitTab_lHowManySelected_Text_LOCAL";
            break;
        }
        if (UnitCategoryTableModel.Type.INSTALLED == model.getType () || UnitCategoryTableModel.Type.LOCAL == model.getType ()) {
            lSelectionInfo.setText ( (NbBundle.getMessage (UnitTab.class, key, count)));
        } else {
            lSelectionInfo.setText (NbBundle.getMessage (UnitTab.class, "UnitTab_lHowManySelected_TextFormatWithSize",
                    NbBundle.getMessage (UnitTab.class, key, count), downloadSize));
        }
    }
    
    private void showDetailsAtRow (int row) {
        if (row == -1) {
            details.setUnit (null);
        } else {
            Unit u = model.isCategoryAtRow (row) ? null : model.getUnitAtRow (row);
            if (u == null) {
                UnitCategory category = model.getCategoryAtRow (row);
                details.setUnitCategory (category);
            } else {
                details.setUnit (u);
            }
        }
    }
    
    private void listenOnSelection () {
        table.getSelectionModel ().addListSelectionListener (new ListSelectionListener () {
            public void valueChanged (ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting ()) return;
                ListSelectionModel lsm =
                        (ListSelectionModel)e.getSource ();
                if (lsm.isSelectionEmpty ()) {
                    //no rows are selected
                    showDetailsAtRow (-1);
                    popupActionsSupport.rowChanged (-1);
                } else {
                    int selectedRow = lsm.getMinSelectionIndex ();
                    popupActionsSupport.rowChanged (selectedRow);
                    //selectedRow is selected
                    showDetailsAtRow (selectedRow);
                    
                }
            }
        });
    }
    
    public void addUpdateUnitListener (UpdateUnitListener l) {
        model.addUpdateUnitListener (l);
    }
    
    public void removeUpdateUnitListener (UpdateUnitListener l) {
        model.removeUpdateUnitListener (l);
    }
    
    void fireUpdataUnitChange () {
        model.fireUpdataUnitChange ();
    }
    
    DocumentListener getDocumentListener () {
        if (dlForSearch == null) {
            dlForSearch = new DocumentListener () {
                public void insertUpdate (DocumentEvent arg0) {
                    filter = tfSearch.getText ().trim ();
                    searchTask.schedule (350);
                }
                
                public void removeUpdate (DocumentEvent arg0) {
                    insertUpdate (arg0);
                }
                
                public void changedUpdate (DocumentEvent arg0) {
                    insertUpdate (arg0);
                }
                
            };
        }
        return dlForSearch;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        lSelectionInfo = new javax.swing.JLabel();
        bTabAction = new javax.swing.JButton();
        lTabActionDescription = new javax.swing.JLabel();
        lSearch = new javax.swing.JLabel();
        tfSearch = new javax.swing.JTextField();
        spTab = new javax.swing.JSplitPane();
        tbActions = new MyToolBar();

        lTabActionDescription.setLabelFor(table);
        org.openide.awt.Mnemonics.setLocalizedText(lTabActionDescription, org.openide.util.NbBundle.getMessage(UnitTab.class, "lHeader.text")); // NOI18N

        lSearch.setLabelFor(tfSearch);
        org.openide.awt.Mnemonics.setLocalizedText(lSearch, org.openide.util.NbBundle.getMessage(UnitTab.class, "lSearch1.text")); // NOI18N

        spTab.setBorder(null);
        spTab.setDividerLocation(370);
        spTab.setResizeWeight(0.5);
        spTab.setOneTouchExpandable(true);

        tbActions.setBorder(null);
        tbActions.setFloatable(false);
        tbActions.setRollover(true);
        tbActions.setOpaque(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(bTabAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 113, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lSelectionInfo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 162, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(375, 375, 375))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(tbActions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lTabActionDescription, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lSearch)
                        .add(4, 4, 4)
                        .add(tfSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 114, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(spTab, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 662, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(12, 12, 12)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(tfSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lSearch)
                    .add(tbActions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lTabActionDescription))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(spTab, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER, false)
                    .add(bTabAction, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 12, Short.MAX_VALUE)
                    .add(lSelectionInfo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 12, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    private LocalDownloadSupport getLocalDownloadSupport () {
        return (model instanceof LocallyDownloadedTableModel) ? ((LocallyDownloadedTableModel)model).getLocalDownloadSupport () : null;
    }
    
    private Task reloadTask (final boolean force) {
        final Runnable checkUpdates = new Runnable (){
            public void run () {
                manager.initTask.waitFinished ();                
                setWaitingState (true);
                final int row = getSelectedRow();
                final Map<String, Boolean> state = UnitCategoryTableModel.captureState(model.getUnitData());
                Utilities.presentRefreshProviders (manager, force);
                SwingUtilities.invokeLater (new Runnable () {
                    public void run () {
                        fireUpdataUnitChange ();
                        UnitCategoryTableModel.restoreState(model.getUnitData(), state, model.isMarkedAsDefault());
                        setSelectedRow(row);
                        refreshState();
                        setWaitingState (false);
                    }
                });
            }
        };
        NetworkProblemPanel.setPerformAgain (checkUpdates);
        
        return Utilities.startAsWorkerThread (checkUpdates);
    }
    
    private class PopupActionSupport extends MouseAdapter {
        private final TabAction[] actions;
        
        PopupActionSupport (TabAction[] actions) {
            this.actions = actions;
        }
        
        void rowChanged (int row) {
            Unit u = null;
            if (row > -1) {
                u = model.getUnitAtRow (row);
            }
            
            for (TabAction action : actions) {
                if (action instanceof RowTabAction) {
                    RowTabAction rowAction = (RowTabAction)action;
                    rowAction.unitChanged (u);
                }
            }
        }
        
        void tableDataChanged () {
            Collection<Unit> units = model.getMarkedUnits ();
            for (TabAction action : actions) {
                action.tableDataChanged (units);
            }
        }
        
        private JPopupMenu createPopup () {
            JPopupMenu popup = new JPopupMenu ();
            popup.removeAll ();
            Set<String> categories2 = new HashSet<String>();
            List<String> categories = new ArrayList<String>();
            for (TabAction action : actions) {
                String categoryName = action.getActionCategory ();
                if (categories2.add (categoryName)) {
                    categories.add (categoryName);
                }
            }
            for (String categoryName : categories) {
                boolean addSeparator = popup.getSubElements ().length > 0;
                for (TabAction action : actions) {
                    String actionCategory = action.getActionCategory ();
                    if ((categoryName != null && categoryName.equals (actionCategory)) || (categoryName == null && actionCategory == null)) {
                        if (action instanceof RowTabAction) {
                            RowTabAction rowAction = (RowTabAction)action;
                            if (rowAction.isVisible ()) {
                                if (addSeparator) {
                                    addSeparator = false;
                                    popup.addSeparator ();
                                }
                                popup.add (new JMenuItem (action));
                            }
                        } else {
                            if (addSeparator) {
                                addSeparator = false;
                                popup.addSeparator ();
                            }
                            popup.add (new JMenuItem (action));
                        }
                    }
                }
            }
            return popup;
        }
        
        @Override
        public void mousePressed (MouseEvent e) {
            maybeShowPopup (e);
        }
        
        @Override
        public void mouseReleased (MouseEvent e) {
            maybeShowPopup (e);
        }
        
        @Override
        public void mouseClicked (MouseEvent e) {
            maybeShowPopup (e);
        }
        
        private void maybeShowPopup (MouseEvent e) {
            if (e.isPopupTrigger ()) {
                showPopup(e.getPoint(), e.getComponent());
            } /*else if (org.openide.awt.MouseUtils.isDoubleClick (e) && model.getType ().equals (UnitCategoryTableModel.Type.INSTALLED)) {
                if (activateAction.isEnabled ()) {
                    activateAction.performAction ();
                } else if (deactivateAction.isEnabled ()) {
                    deactivateAction.performAction ();
                }
            }*/
        }
    }
    
    
    private void showPopup(Point e, Component invoker) {
        int row = UnitTab.this.table.rowAtPoint(e);
        if (row >= 0) {
            table.getSelectionModel().setSelectionInterval(row, row);
            final JPopupMenu popup = popupActionsSupport.createPopup();
            if (popup != null && popup.getComponentCount() > 0) {                
                popup.show(invoker,e.x, e.y);
                    
            }
        }
    }
    
    int getSelectedRow() {
        return table.getSelectedRow();
    }
    void setSelectedRow(int row) {
        if (row < 0) {
            row = 0;
        }
        for(int temp = row; temp >= 0; temp--) {
            if (temp < table.getRowCount() && temp > -1) {
                table.getSelectionModel().setSelectionInterval(temp, temp);
                break;
            }
        }
    }
    
    
    private static String textForKey (String key) {
        JButton jb = new JButton ();
        Mnemonics.setLocalizedText (jb, NbBundle.getMessage (UnitTab.class, key));
        return jb.getText ();
    }
    
    private  abstract class TabAction extends AbstractAction {
        private String name;
        private String actionCategory;
        public TabAction (String nameKey, String actionCategoryKey) {
            super (textForKey (nameKey));
            this.actionCategory = actionCategoryKey;//(actionCategoryKey != null) ? NbBundle.getMessage(UnitTab.class, actionCategoryKey) : null;
            putValue (MNEMONIC_KEY, mnemonicForKey (nameKey));
            name = (String)getValue (NAME);
            putIntoActionMap (UnitTab.this);
        }
        
        public TabAction (String key, KeyStroke accelerator, String actionCategoryKey) {
            this (key, actionCategoryKey);
            putValue (ACCELERATOR_KEY, accelerator);
            putIntoActionMap (UnitTab.this);
        }
        
        protected String getActionName () {
            return name;
        }
        
        public String getActionCategory () {                    
            return (Utilities.modulesOnly ()) ? getActionCategoryImpl() : "";//NOI18N
        }
        
        protected String getActionCategoryImpl () {            
            return actionCategory;
        }
        
        protected void setContextName (String name) {
            putValue (NAME, name);
        }
        
        public void putIntoActionMap (JComponent component) {
            KeyStroke ks = (KeyStroke)getValue (ACCELERATOR_KEY);
            Object key = getValue (NAME);
            if (ks == null) {
                ks = KeyStroke.getKeyStroke ((Integer)getValue (MNEMONIC_KEY), KeyEvent.VK_ALT);
            }
            if (ks != null && key != null) {
                component.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW).put (ks, key);
                component.getActionMap ().put (key,this);
            }
        }
        
        public final void performAction () {
            if (isEnabled ()) {
                actionPerformed (null);
            }
        }
        public final void actionPerformed (ActionEvent e) {
            int row = getSelectedRow ();
            try {
                if (isSynchronous()) {                
                    before();
                }
                performerImpl ();
            } finally {
                if (isSynchronous()) {                
                    after();
                    setSelectedRow(row);
                }                
            }
        }
        public boolean isSynchronous() {
            return true;
        } 
        public void before() {
        }
        public void postRefresh() {
            fireUpdataUnitChange ();
        }

        public void after() {
            postRefresh();
        }
                        
        public void tableDataChanged () {
            tableDataChanged (model.getMarkedUnits ());
        }
        
        public void tableDataChanged (Collection<Unit> units) {
            setEnabled (units.size () > 0);
        }
        
                
        public abstract void performerImpl ();
        
        private int mnemonicForKey (String key) {
            JButton jb = new JButton ();
            Mnemonics.setLocalizedText (jb, NbBundle.getMessage (UnitTab.class, key));
            return jb.getMnemonic ();
        }
    }
    
    private abstract class RowTabAction extends TabAction {
        private Unit u;
        public RowTabAction (String nameKey, String actionCategoryKey) {
            super (nameKey, actionCategoryKey);
        }
        
        public RowTabAction (String nameKey, KeyStroke accelerator, String actionCategoryKey) {
            super (nameKey, accelerator, actionCategoryKey);
        }
        public void unitChanged (Unit u) {
            this.u = u;
            unitChanged ();
        }
        public final boolean isVisible (){
            return (u != null) ? isVisible (u) : false;
        }
        public void rowChanged (int row) {
            if (row > -1) {
                unitChanged (model.getUnitAtRow (row));
            } else {
                unitChanged (null);
            }
        }
        private final void unitChanged () {
            if (u != null) {
                setEnabled (isEnabled (u));
                setContextName (getContextName (u));
            } else {
                setEnabled (false);
                setContextName (getActionName ());
            }
        }
        
        @Override
        public void tableDataChanged () {
            unitChanged ();
        }

        @Override
        public void tableDataChanged(Collection<Unit> units) {
            unitChanged ();
        }
        
        public final  void performerImpl () {
            performerImpl (u);
        }
        protected boolean isVisible (Unit u) {
            return u != null;
        }
        public abstract void performerImpl (Unit u);
        protected abstract boolean isEnabled (Unit u);
        protected abstract String getContextName (Unit u);
    }

    private class CheckAction extends RowTabAction {
        public CheckAction () {
            super ("UnitTab_CheckAction", KeyStroke.getKeyStroke (KeyEvent.VK_SPACE, 0), null);
        }
        
        public void performerImpl(Unit u) {
            if (u != null && u.canBeMarked()) {
                u.setMarked(!u.isMarked());
            }
        }

        protected boolean isEnabled(Unit u) {
            return u != null && u.canBeMarked ();
        }

        protected String getContextName(Unit u) {
            return getActionName();
        }

        @Override
        protected boolean isVisible(Unit u) {
            return false;
        }
        
        @Override
        public void after() {
            model.fireTableDataChanged();
        }                
    }    
    
    private class PopupAction extends RowTabAction {
        public PopupAction () {
            super ("UnitTab_PopUpAction", KeyStroke.getKeyStroke (KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK), null);
        }
        
        @Override
        public void after() {
            model.fireTableDataChanged();
        }

        public void performerImpl(Unit u) {
            int row = getSelectedRow();
            if(row > 0) { 
                Point e = table.getCellRect(row, 1, enabled).getLocation();
                showPopup(e, table);
            }            
        }

        protected boolean isEnabled(Unit u) {
            return u != null;
        }

        protected String getContextName(Unit u) {
            return getActionName();
        }

        @Override
        protected boolean isVisible(Unit u) {
            return false;
        }        
    }
    
    private class UninstallAction extends TabAction {
        public UninstallAction () {
            super ("UnitTab_bTabAction_Name_INSTALLED", null);
        }
        
        public void performerImpl () {
            boolean wizardFinished = false; 
            final Map<String, Boolean> state = UnitCategoryTableModel.captureState(model.getUnitData());            
            UninstallUnitWizard wizard = new UninstallUnitWizard ();
            try {
                wizardFinished = wizard.invokeWizard ();
            } finally {
                Containers.forUninstall ().removeAll ();
                after();
                if (!wizardFinished) {
                    UnitCategoryTableModel.restoreState(model.getUnitData(), state, model.isMarkedAsDefault());
                }
                refreshState();
            }
        }
    }
    
    private class UpdateAction extends TabAction {
        public UpdateAction () {
            super ("UnitTab_bTabAction_Name_UPDATE", null);
        }
        
        public void performerImpl () {
            boolean wizardFinished = false; 
            final Map<String, Boolean> state = UnitCategoryTableModel.captureState(model.getUnitData());            
            try {
                wizardFinished = new InstallUnitWizard ().invokeWizard (OperationType.UPDATE);
            } finally {
                after();
                if (!wizardFinished) {
                    UnitCategoryTableModel.restoreState(model.getUnitData(), state, model.isMarkedAsDefault());
                }                
                refreshState();
            }            
        }
    }
    
    private class AvailableAction extends TabAction {
        public AvailableAction () {
            super ("UnitTab_bTabAction_Name_AVAILABLE", null);
        }
        
        public void performerImpl () {
            boolean wizardFinished = false; 
            final Map<String, Boolean> state = UnitCategoryTableModel.captureState(model.getUnitData());            
            try {
                wizardFinished = new InstallUnitWizard ().invokeWizard (OperationType.INSTALL);
            } finally {
                after();
                if (!wizardFinished) {
                    UnitCategoryTableModel.restoreState(model.getUnitData(), state, model.isMarkedAsDefault());
                }              
                refreshState();
            }
        }
    }
    
    private class LocalUpdateAction extends TabAction {
        public LocalUpdateAction() {
            super("UnitTab_bTabAction_Name_LOCAL", null);
        }
        public void performerImpl() {
            boolean wizardFinished = false;
            final Map<String, Boolean> state = UnitCategoryTableModel.captureState(model.getUnitData());
            
            try {
                wizardFinished = new InstallUnitWizard ().invokeWizard (OperationType.LOCAL_DOWNLOAD);
            } finally {
                after(); 
                if (!wizardFinished) {
                    UnitCategoryTableModel.restoreState(model.getUnitData(), state, model.isMarkedAsDefault());
                }
                refreshState();                
            }
        }
    }
    
    private class CheckCategoryAction extends RowTabAction {
        protected CheckCategoryAction (String nameKey,KeyStroke stroke, String actionCategoryKey) {
            super (nameKey, stroke, actionCategoryKey);
        }
        public CheckCategoryAction () {
            super ("UnitTab_CheckCategoryAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),*/ "Check");
        }
        protected boolean isEnabled (Unit u) {
            boolean retval = false;
            if (Utilities.modulesOnly ()) {
                String category = u.getCategoryName ();
                List<Unit> units = model.getUnitData ();
                for (Unit unit : units) {
                    if (unit != null && category.equals (unit.getCategoryName ()) && !unit.isMarked ()) {
                        retval = true;
                        break;
                    }
                }
            }
            return retval;
        }
        protected String getContextName (Unit u) {
            return getActionName () + " \"" + u.getCategoryName ()+"\"";//NOI18N
        }
        public void performerImpl (Unit u) {
            String category = u.getCategoryName ();
            int count = model.getRowCount ();
            for (int i = 0; i < count; i++) {
                u = model.getUnitAtRow (i);
                if (u != null && category.equals (u.getCategoryName ()) && !u.isMarked () && u.canBeMarked ()) {
                    u.setMarked (true);
                }
            }
        }

        @Override
        public void postRefresh() {
            model.fireTableDataChanged();            
        }
        
        @Override
        protected boolean isVisible (Unit u) {
            if (Utilities.modulesOnly ()) {
                return super.isVisible (u);
            }
            return false;
        }
    }
    
    private class ActivateAction extends RowTabAction {
        public ActivateAction () {
            super ("UnitTab_ActivateAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK),*/ "EnableDisable");
        }
        
        protected boolean isEnabled (Unit u) {
            boolean retval = false;
            if ((u != null) && (u instanceof Unit.Installed)) {
                Unit.Installed i = (Unit.Installed)u;
                if (!i.getRelevantElement ().isEnabled ()) {
                    retval = Unit.Installed.isOperationAllowed (u.updateUnit, u.getRelevantElement (), Containers.forEnable ());
                }
            }
            return  retval;
        }
        protected String getContextName (Unit u) {
            /*if ((u != null) && (u instanceof Unit.Installed)) {
                return getActionName ()+ " " + u.getDisplayName ();
            }*/
            return getActionName ();
        }
        public void performerImpl (Unit u) {
            Unit.Installed unit = (Unit.Installed)u;
            
            if (!unit.getRelevantElement ().isEnabled ()) {
                OperationInfo info = Containers.forEnable ().add (unit.updateUnit, unit.getRelevantElement ());
                assert info != null;
                UninstallUnitWizard wizard = new UninstallUnitWizard ();
                wizard.invokeWizard (true);
                Containers.forEnable ().removeAll ();
            }
        }
        
        @Override
        protected boolean isVisible (Unit u) {
            return  isEnabled();
        }
    }
    
    private class ActivateCategoryAction extends RowTabAction {
        public ActivateCategoryAction () {
            super ("UnitTab_ActivateCategoryAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK),*/ "EnableDisable");
        }
        
        protected boolean isEnabled (Unit uu) {
            boolean retval = false;
            
            if (Utilities.modulesOnly ()) {
                String category = uu.getCategoryName ();
                List<Unit> units = model.getUnitData ();
                for (Unit u : units) {
                    if ((u != null) && (u instanceof Unit.Installed) && category.equals (u.getCategoryName ())) {
                        Unit.Installed installed = (Unit.Installed)u;
                        if (!installed.getRelevantElement ().isEnabled ()) {
                            retval = Unit.Installed.isOperationAllowed (installed.updateUnit, installed.getRelevantElement (), Containers.forEnable ());
                        }
                    }
                }
            }
            
            return  retval;
        }
        protected String getContextName (Unit u) {
            if ((u != null) && (u instanceof Unit.Installed)) {
                return getActionName ()+ " \"" + u.getCategoryName () + "\"";
            }
            return getActionName ();
        }
        public void performerImpl (Unit uu) {
            Unit.Installed unit = (Unit.Installed)uu;
            
            String category = unit.getCategoryName ();
            int count = model.getRowCount ();
            for (int i = 0; i < count; i++) {
                Unit u = model.getUnitAtRow (i);
                if ((u != null) && (u instanceof Unit.Installed) && category.equals (u.getCategoryName ())) {
                    Unit.Installed installed = (Unit.Installed)u;
                    if (!installed.getRelevantElement ().isEnabled ()) {
                        OperationInfo info = Containers.forEnable ().add (installed.updateUnit, installed.getRelevantElement ());
                        assert info != null;
                    }
                }
            }
            
            if (Containers.forEnable ().listAll ().size () > 0) {
                UninstallUnitWizard wizard = new UninstallUnitWizard ();
                wizard.invokeWizard (true);
                Containers.forEnable ().removeAll ();
            }
        }
        
        @Override
        protected boolean isVisible (Unit u) {
            if (Utilities.modulesOnly ()) {
                return  isEnabled();
            }
            return false;
        }
    }
    
    
    private class DeactivateAction extends RowTabAction {
        public DeactivateAction () {
            super ("UnitTab_DeactivateAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK),*/ "EnableDisable");
        }
        
        protected boolean isEnabled (Unit u) {
            boolean retval = false;
            if ((u != null) && (u instanceof Unit.Installed)) {
                Unit.Installed i = (Unit.Installed)u;
                if (i.getRelevantElement ().isEnabled ()) {
                    retval = Unit.Installed.isOperationAllowed (u.updateUnit, u.getRelevantElement (), Containers.forDisable ());
                }
            }
            return  retval;
        }
        protected String getContextName (Unit u) {
            /*if ((u != null) && (u instanceof Unit.Installed)) {
                return getActionName ()+ " " + u.getDisplayName ();
            }*/
            return getActionName ();
        }
        public void performerImpl (Unit u) {
            Unit.Installed unit = (Unit.Installed)u;
            
            if (unit.getRelevantElement ().isEnabled ()) {
                OperationInfo info = Containers.forDisable ().add (unit.updateUnit, unit.getRelevantElement ());
                assert info != null;
                UninstallUnitWizard wizard = new UninstallUnitWizard ();
                wizard.invokeWizard (false);
                Containers.forDisable ().removeAll ();
            }
        }
        
        @Override
        protected boolean isVisible (Unit u) {
            return  isEnabled();
        }
    }
    
    private class DeactivateCategoryAction extends RowTabAction {
        public DeactivateCategoryAction () {
            super ("UnitTab_DeactivateCategoryAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK),*/ "EnableDisable");
        }
        
        protected boolean isEnabled (Unit uu) {
            boolean retval = false;
            
            if (Utilities.modulesOnly ()) {
                String category = uu.getCategoryName ();
                List<Unit> units = model.getUnitData ();
                for (Unit u : units) {
                    if ((u != null) && (u instanceof Unit.Installed) && category.equals (u.getCategoryName ())) {
                        Unit.Installed installed = (Unit.Installed)u;
                        if (installed.getRelevantElement ().isEnabled ()) {
                            retval = Unit.Installed.isOperationAllowed (installed.updateUnit, installed.getRelevantElement (), Containers.forDisable ());
                        }
                    }
                }
            }
            
            return  retval;
        }
        protected String getContextName (Unit u) {
            if ((u != null) && (u instanceof Unit.Installed)) {
                return getActionName ()+ " \"" + u.getCategoryName () + "\"";//NOI18N
            }
            return getActionName ();
        }
        public void performerImpl (Unit uu) {
            Unit.Installed unit = (Unit.Installed)uu;
            
            String category = unit.getCategoryName ();
            int count = model.getRowCount ();
            for (int i = 0; i < count; i++) {
                Unit u = model.getUnitAtRow (i);
                if ((u != null) && (u instanceof Unit.Installed) && category.equals (u.getCategoryName ())) {
                    Unit.Installed installed = (Unit.Installed)u;
                    if (installed.getRelevantElement ().isEnabled ()) {
                        OperationInfo info = Containers.forDisable ().add (installed.updateUnit, installed.getRelevantElement ());
                        assert info != null;
                    }
                }
            }
            
            if (Containers.forDisable ().listAll ().size () > 0) {
                UninstallUnitWizard wizard = new UninstallUnitWizard ();
                wizard.invokeWizard (false);
                Containers.forDisable ().removeAll ();
            }
        }
        @Override
        protected boolean isVisible (Unit u) {
            if (Utilities.modulesOnly ()) {
                return  isEnabled();
            }
            return false;
        }
    }
    
    private class UncheckCategoryAction extends RowTabAction {
        public UncheckCategoryAction () {
            super ("UnitTab_UncheckCategoryAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),*/ "Uncheck");
        }
        @Override
        public void postRefresh() {
            model.fireTableDataChanged();            
        }
        
        @Override
        protected boolean isEnabled (Unit u) {
            boolean retval = false;
            
            if (Utilities.modulesOnly ()) {
                String category = u.getCategoryName ();
                List<Unit> units = model.getUnitData ();
                for (Unit uu : units) {
                    if (uu != null && category.equals (uu.getCategoryName ()) && uu.isMarked ()) {
                        retval = true;
                        break;
                    }
                }
            }
            return retval;
        }
        
        @Override
        public void performerImpl (Unit u) {
            String category = u.getCategoryName ();
            int count = model.getRowCount ();
            for (int i = 0; i < count; i++) {
                u = model.getUnitAtRow (i);
                if (u != null && category.equals (u.getCategoryName ()) && u.isMarked () && u.canBeMarked ()) {
                    u.setMarked (false);
                }
            }
        }
        
        @Override
        protected boolean isVisible (Unit u) {
            if (Utilities.modulesOnly ()) {
                return super.isVisible (u);
            }
            return false;
        }

        protected String getContextName(Unit u) {
            return getActionName() + " \"" + u.getCategoryName()+"\""; //NOI18N           
        }
    }
    private class CheckAllAction extends RowTabAction {
        public CheckAllAction () {
            super ("UnitTab_CheckAllAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),*/"Check");
        }
        
        public void performerImpl (Unit uu) {
            int count = model.getRowCount ();
            for (int i = 0; i < count; i++) {
                Unit u = model.getUnitAtRow (i);
                if (u != null && !u.isMarked () &&  u.canBeMarked ()) {
                    u.setMarked (true);
                }
            }
        }
        @Override
        public void postRefresh() {
            model.fireTableDataChanged();            
        }
        
        protected boolean isEnabled (Unit uu) {
            return true;
        }
        protected String getContextName (Unit u) {
            return getActionName ();
        }        
    }
    
    private class UncheckAllAction extends RowTabAction {
        public UncheckAllAction () {
            super ("UnitTab_UncheckAllAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), */"Uncheck");
        }
        public void performerImpl (Unit uu) {
            int count = model.getRowCount ();
            for (int i = 0; i < count; i++) {
                Unit u = model.getUnitAtRow (i);
                if (u != null && u.isMarked ()  && u.canBeMarked ()) {
                    u.setMarked (false);
                }
            }
        }
        @Override
        public void postRefresh() {
            model.fireTableDataChanged();            
        }
        
        protected boolean isEnabled (Unit uu) {
            return true;
        }
        
        protected String getContextName (Unit u) {
            return getActionName ();
        }        
    }
    
    private class ReloadAction extends TabAction {
        Task reloadTask = null;
        public ReloadAction () {
            super ("UnitTab_RefreshAction", KeyStroke.getKeyStroke (KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK), null);
            String picture = "newUpdates.gif";//NOI18N
            String tooltip = NbBundle.getMessage (UnitTab.class, "UnitTab_Tooltip_RefreshAction");//NOI18N
            StringBuilder sb = new StringBuilder ("/org/netbeans/modules/autoupdate/ui/resources/");//NOI18N
            sb.append (picture);
            //putValue(LARGE_ICON_KEY, new javax.swing.ImageIcon(getClass().getResource(sb.toString())));
            putValue (SMALL_ICON, new javax.swing.ImageIcon (getClass ().getResource (sb.toString ())));
            putValue (TOOL_TIP_TEXT_KEY, tooltip);
            putValue (NAME, "");//NOI18N
            setEnabled (false);
        }
        
        public void performerImpl () {
            setEnabled (false);
            reloadTask = reloadTask (true);
        }

        @Override
        public boolean isSynchronous() {
            return false;
        }
    }
    
    private class AddLocallyDownloadedAction extends TabAction {
        public AddLocallyDownloadedAction () {
            super ("UnitTab_AddLocallyDownloadedAction", KeyStroke.getKeyStroke (KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK), null);
            String picture = "add.png";//NOI18N
            String tooltip = NbBundle.getMessage (UnitTab.class, "UnitTab_Tooltip_AddAction_LOCAL");//NOI18N
            StringBuilder sb = new StringBuilder ("/org/netbeans/modules/autoupdate/ui/resources/");//NOI18N
            sb.append (picture);
            //putValue(LARGE_ICON_KEY, new javax.swing.ImageIcon(getClass().getResource(sb.toString())));
            putValue (SMALL_ICON, new javax.swing.ImageIcon (getClass ().getResource (sb.toString ())));
            putValue (TOOL_TIP_TEXT_KEY, tooltip);
            putValue (NAME, "");//NOI18N
        }
        
        public void performerImpl() {
            if (getLocalDownloadSupport().selectNbmFiles()) {
                before();                 
                final Map<String, Boolean> state = UnitCategoryTableModel.captureState(model.getUnitData());                
                final Runnable addUpdates = new Runnable(){
                    public void run() {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                after();
                                UnitCategoryTableModel.restoreState(model.getUnitData(), state, model.isMarkedAsDefault());
                                LocallyDownloadedTableModel downloadedTableModel = ((LocallyDownloadedTableModel)model);
                                List<UpdateUnit> installed = downloadedTableModel.getAlreadyInstalled();
                                if (!installed.isEmpty())  {
                                    showMessage(installed);
                                }
                                refreshState();
                                setWaitingState(false);
                            }
                        });
                    }

                };                
                setWaitingState(true);
                Utilities.startAsWorkerThread(addUpdates, 250);
            }
        }                
        
        void showMessage(List<UpdateUnit> installed) {
            if (!installed.isEmpty())  {
                StringBuilder pluginNames = new StringBuilder();
                for (UpdateUnit updateUnit : installed) {
                    if (pluginNames.length() > 0) {
                        pluginNames.append(',').append(' ');//NOI18N
                    }
                    List<UpdateElement> elements = updateUnit.getAvailableUpdates();
                    if (elements.size() > 0) {
                        pluginNames.append(elements.get(0).getDisplayName());
                    } else {
                        ModuleInfo m = ModuleProvider.getInstalledModules().get(updateUnit.getCodeName());
                        pluginNames.append(m != null ? m.getDisplayName() : updateUnit.getCodeName());
                    }                    
                }
                if (installed.size() == 1) {
                    pluginNames = new StringBuilder(NbBundle.getMessage(UnitTab.class, "NotificationOneAlreadyInstalled",pluginNames.toString()));//NOI18N
                } else {
                    pluginNames = new StringBuilder(NbBundle.getMessage(UnitTab.class, "NotificationMoreAlreadyInstalled",pluginNames.toString()));//NOI18N
                }                
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(pluginNames.toString(),NotifyDescriptor.INFORMATION_MESSAGE));
            } 
        }

        @Override
        public boolean isSynchronous() {
            return false;
        }                        
    }
    
    private class RemoveLocallyDownloadedAction extends RowTabAction {
        public RemoveLocallyDownloadedAction () {
            super ("UnitTab_AddLocallyDownloadedAction", KeyStroke.getKeyStroke (KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK), null);
            String picture = "remove.png";//NOI18N
            String tooltip = NbBundle.getMessage (UnitTab.class, "UnitTab_Tooltip_RemoveAction_LOCAL");//NOI18N
            StringBuilder sb = new StringBuilder ("/org/netbeans/modules/autoupdate/ui/resources/");//NOI18N
            sb.append (picture);
            //putValue(LARGE_ICON_KEY, new javax.swing.ImageIcon(getClass().getResource(sb.toString())));
            putValue (SMALL_ICON, new javax.swing.ImageIcon (getClass ().getResource (sb.toString ())));
            putValue (TOOL_TIP_TEXT_KEY, tooltip);
            putValue (NAME, "");//NOI18N
        }
        
        protected boolean isEnabled (Unit uu) {
            return uu != null && (model.getType ().equals (UnitCategoryTableModel.Type.LOCAL));
        }
        
        @Override
        public boolean isEnabled () {
            return table.getSelectedRow () > -1;
        }
        
        public void performerImpl (final Unit unit) {
            final Runnable removeUpdates = new Runnable (){
                public void run() {                                        
                    try {
                        if (unit.isMarked()) {
                            //this removes it from container
                            unit.setMarked(false);
                        }
                        getLocalDownloadSupport().remove(unit.updateUnit);
                    } finally {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                after();
                                refreshState();
                                setWaitingState(false);
                            }
                        });
                    }
                }
            };
            setWaitingState(true);
            Utilities.startAsWorkerThread(removeUpdates, 250);
        }
                        
        protected String getContextName (Unit u) {
            return "";//NOI18N
        }
        
        @Override
        protected String getActionName () {
            return "";//NOI18N
        }
        
        @Override
        protected boolean isVisible (Unit u) {
            return false;
        }

        @Override
        public boolean isSynchronous() {
            return false;
        }        
    }        
    class EnableRenderer extends  DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent (
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel renderComponent = (JLabel)super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof Boolean) {
                Unit u = model.getUnitAtRow (row);
                Boolean state = (Boolean)value;
                if (state.booleanValue ()) {
                    if (deactivateAction.isEnabled (u)) {
                        renderComponent.setIcon (new javax.swing.ImageIcon (getClass ().getResource ("/org/netbeans/modules/autoupdate/ui/resources/active.png"))); // NOI18N
                    } else {
                        renderComponent.setIcon (new javax.swing.ImageIcon (getClass ().getResource ("/org/netbeans/modules/autoupdate/ui/resources/active2.png"))); // NOI18N
                    }
                    
                } else {
                    if (activateAction.isEnabled (u)) {
                        renderComponent.setIcon (new javax.swing.ImageIcon (getClass ().getResource ("/org/netbeans/modules/autoupdate/ui/resources/inactive.png"))); // NOI18N
                    } else {
                        renderComponent.setIcon (new javax.swing.ImageIcon (getClass ().getResource ("/org/netbeans/modules/autoupdate/ui/resources/inactive2.png"))); // NOI18N
                    }
                }
                renderComponent.setText ("");
                renderComponent.setHorizontalAlignment (SwingConstants.CENTER);
                
            }
            Component retval = renderComponent;
            return retval;
        }
    }
    
    private static class MyToolBar extends JToolBar {
        @Override
        protected void paintComponent (Graphics g) {
            //no painting;
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bTabAction;
    private javax.swing.JLabel lSearch;
    private javax.swing.JLabel lSelectionInfo;
    private javax.swing.JLabel lTabActionDescription;
    private javax.swing.JSplitPane spTab;
    private javax.swing.JToolBar tbActions;
    private javax.swing.JTextField tfSearch;
    // End of variables declaration//GEN-END:variables
    
}
