/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.autoupdate.ui;

import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.modules.autoupdate.ui.wizards.UninstallUnitWizard;
import org.netbeans.modules.autoupdate.ui.wizards.InstallUnitWizard;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.autoupdate.ui.wizards.OperationWizardModel.OperationType;
import org.openide.awt.Mnemonics;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.TaskListener;

/**
 *
 * @author  Jiri Rechtacek, Radek Matous
 */
public class UnitTab extends javax.swing.JPanel {
    static final String PROP_LAST_CHECK = "lastCheckTime"; // NOI18N    
    private PreferenceChangeListener preferenceChangeListener;    
    private UnitTable table = null;
    private UnitDetails details = null;
    private UnitCategoryTableModel model = null;
    private DocumentListener dlForSearch;
    private FocusListener flForSearch;
    private String filter = "";
    private PluginManagerUI manager = null;
    private PopupActionSupport popupActionsSupport;
    private TabAction activateAction;
    private TabAction deactivateAction;
    private TabAction reloadAction;
    private RowTabAction moreAction;
    private RowTabAction lessAction;
    private RowTabAction removeLocallyDownloaded;
    
    private static Boolean isWaitingForExternal = false;
    
    private static final RequestProcessor SEARCH_PROCESSOR = new RequestProcessor ("search-processor");
    private final RequestProcessor.Task searchTask = SEARCH_PROCESSOR.create (new Runnable (){
        public void run () {
            if (filter != null) {
                int row = getSelectedRow();
                final Unit u = (row >= 0) ? getModel().getUnitAtRow(row) : null;
                final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits ());
                Runnable runAftreWards = new Runnable (){
                    public void run () {
                        if (u != null) {
                            int row = findRow(u.updateUnit.getCodeName());
                            restoreSelectedRow(row);
                        }                        
                        UnitCategoryTableModel.restoreState (model.getUnits (), state, model.isMarkedAsDefault ());
                        refreshState ();
                    }
                };
                model.setFilter (filter, runAftreWards);
            }
        }
    });
    private static final RequestProcessor DOWNLOAD_SIZE_PROCESSOR = new RequestProcessor ("download-size-processor", 1, true);
    private Task getDownloadSizeTask = null;
    
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
        lWarning.setVisible(false);//#164953
        //TODO: for WINDOWS - don't paint background and let visible the native look
        /*
        if (UIManager.getLookAndFeel().getName().toLowerCase().startsWith("windows")) {//NOI18N
            setOpaque(false);
        }
         */
        spTab.setLeftComponent (new JScrollPane (table));
        spTab.setRightComponent (new JScrollPane (details,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        initTab ();
        listenOnSelection ();
        addComponentListener (new ComponentAdapter (){
            @Override
            public void componentShown (ComponentEvent e) {
                super.componentShown (e);
                focusTable ();
                
            }
        });
        addUpdateUnitListener(new UpdateUnitListener() {
            public void updateUnitsChanged() {
                UnitTab.this.manager.updateUnitsChanged();
            }

            public void buttonsChanged() {
                UnitTab.this.manager.buttonsChanged();
            }

            public void filterChanged() {
                model.fireTableDataChanged();
                UnitTab.this.manager.decorateTabTitle(UnitTab.this.table);                
                refreshState();
            }
        });
        table.getInputMap ().put (
                KeyStroke.getKeyStroke (KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK),
                "org.netbeans.modules.autoupdate.ui.UnitTab.PopupActionSupport"); // NOI18N
        table.getActionMap ().put (
                "org.netbeans.modules.autoupdate.ui.UnitTab.PopupActionSupport", // NOI18N
                popupActionsSupport.popupOnF10);

    }
    
    void focusTable () {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                table.requestFocusInWindow ();
            }
        });
    }
    
    UnitCategoryTableModel getModel () {
        return model;
    }
    
    UnitTable getTable () {
        return table;
    }
    
    public String getHelpId () {
        return UnitTab.class.getName () + '.' + model.getType (); // NOI18N
    }
    
    void setWaitingState (boolean waitingState) {
        boolean enabled = !waitingState;
        Component[] all = getComponents ();
        for (Component component : all) {
            if (component == bTabAction || component == bTabAction1 || component == bTabAction2) {
                if (enabled) {
                    TabAction a = (TabAction) ((AbstractButton)component).getAction();
                    component.setEnabled (a == null ? false : a.isEnabled());
                } else {
                    component.setEnabled (enabled);
                }
            } else {
                if (component == spTab) {
                    spTab.getLeftComponent ().setEnabled (enabled);
                    spTab.getRightComponent ().setEnabled (enabled);
                    details.setEnabled (enabled);
                    table.setEnabled (enabled);
                } else {
                    component.setEnabled (enabled);
                }
            }
        }
        if (reloadAction != null) {
            reloadAction.setEnabled (enabled);
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
        focusTable ();
    }
    
    private void prepareTopButton (Action action) {
        topButton.setToolTipText ((String)action.getValue (JComponent.TOOL_TIP_TEXT_KEY));
        topButton.setAction (action);
    }
    
    @Override
    public void addNotify () {
        super.addNotify ();
        if (dlForSearch == null) {
            tfSearch.getDocument ().addDocumentListener (getDocumentListener ());            
        }
        
        if (flForSearch == null) {
            flForSearch = new FocusListener() {
                public void focusGained(FocusEvent e) {
                    tfSearch.selectAll();
                }

                public void focusLost(FocusEvent e) {
                    tfSearch.select(0, 0);
                }
            };
            tfSearch.addFocusListener(flForSearch);
        }
        RequestProcessor.Task runningTask = PluginManagerUI.getRunningTask ();
        synchronized (isWaitingForExternal) {
            if (runningTask != null && ! runningTask.isFinished () && ! isWaitingForExternal) {
                isWaitingForExternal = true;
                runningTask.addTaskListener (new TaskListener () {
                    public void taskFinished (org.openide.util.Task task) {
                        reloadTask (false).schedule (10);
                        isWaitingForExternal = false;
                    }
                });
            }
        }
    }
    
    @Override
    public void removeNotify () {
        super.removeNotify ();
        if (dlForSearch != null) {
            tfSearch.getDocument ().removeDocumentListener (getDocumentListener ());
        }
        dlForSearch = null;
        if (flForSearch != null) {
            tfSearch.removeFocusListener(flForSearch);
        }
        flForSearch = null;
        Preferences p = NbPreferences.root().node("/org/netbeans/modules/autoupdate");//NOI18N
        if (preferenceChangeListener != null) {
            p.removePreferenceChangeListener (preferenceChangeListener);
            preferenceChangeListener = null;
        }
    }
    
    private Collection<Unit> oldUnits = Collections.emptySet ();
    
    public void refreshState () {
        detailView.setVisible(this.model.supportsTwoViews());
        final Collection<Unit> units = model.getMarkedUnits ();
        if (oldUnits.equals (units)) {
            return ;
        }
        oldUnits = units;
        popupActionsSupport.tableDataChanged ();
        
        if (units.size () == 0) {
            cleanSelectionInfo ();
        } else {
            setSelectionInfo (null, units.size ());
        }
        getDefaultAction ().tableDataChanged(units);
        boolean alreadyScheduled = false;
        if (getDownloadSizeTask != null) {
            if (getDownloadSizeTask.getDelay () > 0) {
                getDownloadSizeTask.schedule (1000);
                alreadyScheduled = true;
            } else if (! getDownloadSizeTask.isFinished ()) {
                getDownloadSizeTask.cancel ();
            }
        }
        if (units.size () > 0 && ! alreadyScheduled) {
            getDownloadSizeTask = DOWNLOAD_SIZE_PROCESSOR.post (new Runnable () {
                public void run () {
                    int downloadSize = model.getDownloadSize ();
                    if (Thread.interrupted ()) {
                        return ;
                    }
                    if (model.getMarkedUnits ().size () == 0) {
                        cleanSelectionInfo ();
                    } else {
                        setSelectionInfo (Utilities.getDownloadSizeAsString (downloadSize), model.getMarkedUnits ().size ());
                    }
                }
            }, 150);
        }
    }
    
    private TabAction getDefaultAction () {
        return (TabAction)bTabAction.getAction ();
    }
    
    private void initTab () {
        TabAction[] forPopup = null;
        switch (model.getType ()) {
        case INSTALLED :
        {
            RowTabAction checkCategoryAction = new CheckCategoryAction ();
            RowTabAction uncheckCategoryAction = new UncheckCategoryAction ();
            RowTabAction checkAllAction = new CheckAllAction ();
            RowTabAction uncheckAllAction = new UncheckAllAction ();
            RowTabAction activateCategoryAction = new ActivateCategoryAction ();
            RowTabAction deactivateCategoryAction = new DeactivateCategoryAction ();
            
            activateAction = new ActivateAction ();
            deactivateAction = new DeactivateAction ();
            UninstallAction uninstall = new UninstallAction();
            
            forPopup = new TabAction[] {
                activateAction, deactivateAction,activateCategoryAction,deactivateCategoryAction,
                checkCategoryAction, uncheckCategoryAction,
                checkAllAction, uncheckAllAction, new CheckAction (), uninstall
            };
            bTabAction1.setVisible(true);
            bTabAction2.setVisible(true);
            bTabAction.setAction(activateAction);
            bTabAction1.setAction(deactivateAction);
            bTabAction2.setAction (uninstall);
            prepareTopButton (reloadAction = new ReloadAction ());
            table.setEnableRenderer (new EnableRenderer ());
            initReloadTooltip();
            break;
        }
        case UPDATE :
        {
            RowTabAction selectCategoryAction = new CheckCategoryAction ();
            RowTabAction deselectCategoryAction = new UncheckCategoryAction ();
            RowTabAction selectAllAction = new CheckAllAction ();
            RowTabAction deselectAllAction = new UncheckAllAction ();
            moreAction = new MoreAction();
            lessAction = new LessAction();
            
            forPopup = new TabAction[] {
                selectCategoryAction, deselectCategoryAction,
                selectAllAction, deselectAllAction, new CheckAction (),
                moreAction, lessAction
            };
        }
        bTabAction.setAction (new UpdateAction ());
        bTabAction1.setVisible(false);
        bTabAction2.setVisible(false);
        prepareTopButton (reloadAction = new ReloadAction ());
         initReloadTooltip();
        break;
        case AVAILABLE :
        {
            RowTabAction selectCategoryAction = new CheckCategoryAction ();
            RowTabAction deselectCategoryAction = new UncheckCategoryAction ();
            RowTabAction selectAllAction = new CheckAllAction ();
            RowTabAction deselectAllAction = new UncheckAllAction ();
            moreAction = new MoreAction();
            lessAction = new LessAction();
            
            forPopup = new TabAction[] {
                selectCategoryAction, deselectCategoryAction,
                selectAllAction, deselectAllAction, new CheckAction (),
                moreAction, lessAction                
            };
        }
        bTabAction.setAction (new AvailableAction ());
        bTabAction1.setVisible(false);
        bTabAction2.setVisible(false);
        prepareTopButton (reloadAction = new ReloadAction ());
        table.setEnableRenderer (new SourceCategoryRenderer ());
         initReloadTooltip();
        break;
        case LOCAL :
            removeLocallyDownloaded = new RemoveLocallyDownloadedAction ();
            {
                forPopup = new TabAction[] {
                    removeLocallyDownloaded, new CheckAction ()
                };
            }
            bTabAction.setAction (new LocalUpdateAction ());
            bTabAction1.setVisible(false);
            bTabAction2.setVisible(false);
            prepareTopButton (new AddLocallyDownloadedAction ());
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
    
    
    private void cleanSelectionInfo () {
        lSelectionInfo.setText ("");
        lWarning.setText ("");
        lWarning.setIcon (null);
    }
    
    private void setSelectionInfo (String downloadSize, int count) {
        String operationNameKey = null;
        switch (model.getType ()) {
        case INSTALLED :
            operationNameKey = "UnitTab_OperationName_Text_INSTALLED";
            break;
        case UPDATE :
            operationNameKey = "UnitTab_OperationName_Text_UPDATE";
            break;
        case AVAILABLE :
            operationNameKey = "UnitTab_OperationName_Text_AVAILABLE";
            break;
        case LOCAL :
            operationNameKey = "UnitTab_OperationName_Text_LOCAL";
            break;
        }
        String key = count == 1 ? "UnitTab_lHowManySelected_Single_Text" : "UnitTab_lHowManySelected_Many_Text";
        if (UnitCategoryTableModel.Type.INSTALLED == model.getType () || UnitCategoryTableModel.Type.LOCAL == model.getType ()) {
            lSelectionInfo.setText ((NbBundle.getMessage (UnitTab.class, key, count)));
        } else {
            if (downloadSize == null) {
                lSelectionInfo.setText ((NbBundle.getMessage (UnitTab.class, key, count)));
            } else {
                lSelectionInfo.setText (NbBundle.getMessage (UnitTab.class, "UnitTab_lHowManySelected_TextFormatWithSize",
                        NbBundle.getMessage (UnitTab.class, key, count), downloadSize));
            }
        }
        if (model.needsRestart ()) {
            Icon warningIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/autoupdate/ui/resources/warning.gif", false); // NOI18N
            lWarning.setIcon (warningIcon);
            lWarning.setText (NbBundle.getMessage (UnitTab.class, "UnitTab_lWarning_Text", NbBundle.getMessage (UnitTab.class, operationNameKey))); // NOI18N
        }
    }
    
    private void showDetailsAtRow (int row) {
        showDetailsAtRow (row, null);
    }
    
    private void showDetailsAtRow (int row, Action action) {
        if (row == -1) {
            details.setUnit (null);
        } else {
            Unit u = model.isExpansionControlAtRow(row) ? null : model.getUnitAtRow (row);
            if (u == null) {
                //TODO: add details about more ... or les ...
            } else {
                details.setUnit (u, action);
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
                    Action action = null;
                    if (activateAction != null && activateAction.isEnabled ()) {
                        //action = activateAction;
                    } else if (deactivateAction != null && deactivateAction.isEnabled ()) {
                        //action = deactivateAction;
                    } else if (removeLocallyDownloaded != null && removeLocallyDownloaded.isEnabled ()) {
                        action = removeLocallyDownloaded;
                    }
                    showDetailsAtRow (selectedRow, action);
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lSelectionInfo = new javax.swing.JLabel();
        bTabAction = new javax.swing.JButton();
        lSearch = new javax.swing.JLabel();
        tfSearch = new javax.swing.JTextField();
        spTab = new javax.swing.JSplitPane();
        topButton = new javax.swing.JButton();
        lWarning = new javax.swing.JLabel();
        detailView = new javax.swing.JCheckBox();
        bTabAction1 = new javax.swing.JButton();
        bTabAction2 = new javax.swing.JButton();

        lSearch.setLabelFor(tfSearch);
        org.openide.awt.Mnemonics.setLocalizedText(lSearch, org.openide.util.NbBundle.getMessage(UnitTab.class, "lSearch1.text")); // NOI18N

        spTab.setBorder(null);
        spTab.setDividerLocation(370);
        spTab.setResizeWeight(0.5);
        spTab.setOneTouchExpandable(true);

        org.openide.awt.Mnemonics.setLocalizedText(topButton, "jButton1");

        detailViewInit();
        org.openide.awt.Mnemonics.setLocalizedText(detailView, org.openide.util.NbBundle.getBundle(UnitTab.class).getString("UnitTab.detailView.text")); // NOI18N
        detailView.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                detailViewItemStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(topButton)
                        .add(18, 18, 18)
                        .add(detailView)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 375, Short.MAX_VALUE)
                        .add(lSearch)
                        .add(4, 4, 4)
                        .add(tfSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 114, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(spTab, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 712, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(bTabAction)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(bTabAction1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(bTabAction2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lSelectionInfo)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(lWarning, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 482, Short.MAX_VALUE)
                        .add(99, 99, 99)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(tfSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lSearch)
                    .add(topButton)
                    .add(detailView))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(spTab, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER, false)
                    .add(bTabAction)
                    .add(lSelectionInfo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(lWarning)
                    .add(bTabAction1)
                    .add(bTabAction2))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {lSelectionInfo, lWarning}, org.jdesktop.layout.GroupLayout.VERTICAL);

        lSearch.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UnitTab.class, "ACD_Search")); // NOI18N
        topButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(UnitTab.class, "ACN_Reload")); // NOI18N
        topButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UnitTab.class, "ACD_Reload")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void detailViewItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_detailViewItemStateChanged
        if (this.model.supportsTwoViews()) {
            manager.setDetailView(detailView.isSelected());
            manager.updateUnitsChanged();
            System.setProperty(PluginManagerUI.DETAIL_VIEW_SELECTED_PROP, "" + detailView.isSelected());
        }
}//GEN-LAST:event_detailViewItemStateChanged

    private void detailViewInit() {
        detailView.setVisible(this.model.supportsTwoViews());
        if (this.model.supportsTwoViews()) {            
            detailView.setSelected(Boolean.getBoolean(PluginManagerUI.DETAIL_VIEW_SELECTED_PROP));
        }
    }
    
    private LocalDownloadSupport getLocalDownloadSupport () {
        return (model instanceof LocallyDownloadedTableModel) ? ((LocallyDownloadedTableModel)model).getLocalDownloadSupport () : null;
    }
    
    private Task reloadTask (final boolean force) {
        final Runnable checkUpdates = new Runnable (){
            public void run () {
                ProgressHandle handle = ProgressHandleFactory.createHandle (NbBundle.getMessage (UnitTab.class,  ("UnitTab_ReloadAction")));
                JComponent progressComp = ProgressHandleFactory.createProgressComponent (handle);
                JLabel detailLabel = new JLabel (NbBundle.getMessage (UnitTab.class, "UnitTab_PrepareReloadAction"));
                manager.setProgressComponent (detailLabel, progressComp);
                handle.setInitialDelay (0);
                handle.start ();
                manager.initTask.waitFinished ();
                setWaitingState (true);
                if (getDownloadSizeTask != null && ! getDownloadSizeTask.isFinished ()) {
                    if (getDownloadSizeTask.getDelay () > 0) {
                        getDownloadSizeTask.cancel ();
                    } else {
                        getDownloadSizeTask.waitFinished ();
                    }
                }
                final int row = getSelectedRow ();
                final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits ());
                if (model instanceof LocallyDownloadedTableModel) {
                    ((LocallyDownloadedTableModel) model).removeInstalledUnits ();
                    ((LocallyDownloadedTableModel) model).setUnits (null);
                }
                manager.unsetProgressComponent (detailLabel, progressComp);
                Utilities.presentRefreshProviders (manager, force);
                SwingUtilities.invokeLater (new Runnable () {
                    public void run () {
                        fireUpdataUnitChange ();
                        UnitCategoryTableModel.restoreState (model.getUnits (), state, model.isMarkedAsDefault ());
                        restoreSelectedRow (row);
                        refreshState ();                        
                        setWaitingState (false);
                    }
                });
            }
        };
        return Utilities.startAsWorkerThread (checkUpdates);
    }
    
    class PopupActionSupport extends MouseAdapter implements Runnable {
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
                    rowAction.unitChanged (row, u);
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
            if (!maybeShowPopup (e)) {
                int row = UnitTab.this.table.rowAtPoint(e.getPoint());
                if (model.isExpansionControlAtRow(row)) {
                    moreAction.unitChanged(row, null);
                    lessAction.unitChanged(row, null);
                    if (moreAction != null && moreAction.isEnabled()) {
                        moreAction.performAction();
                    } else if (lessAction != null && lessAction.isEnabled()) {
                        lessAction.performAction();
                    }                    
                }
            }
        }
        
        private boolean maybeShowPopup (MouseEvent e) {
            if (e.isPopupTrigger ()) {  
                focusTable ();
                showPopup (e.getPoint (), e.getComponent ());
                return true;
            }
            return false;
        }

        public void run () {
            Point p = getPositionForPopup();

            if (p != null) {
                showPopup (p, table);
            }
        }
        
        private Point getPositionForPopup () {
            int r = table.getSelectedRow ();
            int c = table.getSelectedColumn ();

            if (r < 0 || c < 0) {
                return null;
            }

            Rectangle rect = table.getCellRect (r, c, false);

            if (rect == null) {
                return null;
            }

            return SwingUtilities.convertPoint (table, rect.x, rect.y, table);
        }

        public final Action popupOnF10 = new AbstractAction () {

            public void actionPerformed (ActionEvent evt) {
                popupActionsSupport.run ();
            }

            @Override
            public boolean isEnabled () {
                return table.isFocusOwner ();
            }
        };

    }
    
    private void showPopup (Point e, Component invoker) {
        int row = UnitTab.this.table.rowAtPoint (e);
        if (row >= 0) {
            table.getSelectionModel ().setSelectionInterval (row, row);
            final JPopupMenu finalPopup = popupActionsSupport.createPopup ();
            if (finalPopup != null && finalPopup.getComponentCount () > 0) {
                finalPopup.show (invoker,e.x, e.y);
                
            }
        }
    }
    
    private int getSelectedRow () {
        return table.getSelectedRow ();
    }
    private void restoreSelectedRow (int row) {
        if (row < 0) {
            row = 0;
        }
        for(int temp = row; temp >= 0; temp--) {
            if (temp < table.getRowCount () && temp > -1) {
                table.getSelectionModel ().setSelectionInterval (temp, temp);
                break;
            }
        }
    }    
    int findRow(String codeName) {
        for (int i = 0; i < model.getRowCount();i++) {
            Unit u = model.getUnitAtRow(i);
            if (u != null && codeName.equals(u.updateUnit.getCodeName())) {
                return i;
            }
        }
        return -1;
    } 
    
    
    private void initReloadTooltip() {
        Preferences p = NbPreferences.root().node("/org/netbeans/modules/autoupdate");//NOI18N
        long lastTime = p.getLong(PROP_LAST_CHECK, 0);
        if (lastTime > 0) {
            topButton.setToolTipText("<html>"+NbBundle.getMessage(UnitTab.class, "UnitTab_ReloadTime", //NOI18N
                    "<b>"+new SimpleDateFormat().format(new Date(lastTime)) + "</b>")+"</html>");
        } else {
            String never = NbBundle.getMessage(UnitTab.class, "UnitTab_ReloadTime_Never");//NOI18N
            topButton.setToolTipText("<html>"+NbBundle.getMessage(UnitTab.class, "UnitTab_ReloadTime", "<b>"+never+"</b>") + "/<html>");//NOI18N
        }
        if (preferenceChangeListener == null) {
            preferenceChangeListener = new PreferenceChangeListener() {

                public void preferenceChange(PreferenceChangeEvent evt) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                             initReloadTooltip();
                        }
                    });
                }
            };
            p.addPreferenceChangeListener(preferenceChangeListener);
        }
    }
    
    
    static String textForKey (String key) {
        JButton jb = new JButton ();
        Mnemonics.setLocalizedText (jb, NbBundle.getMessage (UnitTab.class, key));
        return jb.getText ();
    }
    
    static int mnemonicForKey(String key) {
        JButton jb = new JButton();
        Mnemonics.setLocalizedText(jb, NbBundle.getMessage(UnitTab.class, key));
        return jb.getMnemonic();
    }
    
    private  abstract class TabAction extends AbstractAction {
        private String name;
        private String actionCategory;
        public TabAction (String nameKey, String actionCategoryKey) {
            super (textForKey (nameKey));
            this.actionCategory = actionCategoryKey;//(actionCategoryKey != null) ? NbBundle.getMessage(UnitTab.class, actionCategoryKey) : null;
            putValue (MNEMONIC_KEY, mnemonicForKey (nameKey));
            name = (String)getValue (NAME);
            putIntoActionMap (table);
        }
        
        public TabAction (String key, KeyStroke accelerator, String actionCategoryKey) {
            this (key, actionCategoryKey);
            putValue (ACCELERATOR_KEY, accelerator);
            putIntoActionMap (table);
        }
        
        protected String getActionName () {
            return name;
        }
        
        public String getActionCategory () {
            return getActionCategoryImpl ();//NOI18N
        }
        
        protected String getActionCategoryImpl () {
            return actionCategory;
        }
        
        protected void setContextName (String name) {
            putValue (NAME, name);
        }
        
        @Override
        public void setEnabled (boolean enabled) {
            if (isEnabled () != enabled) {
                if (enabled) {
                    RequestProcessor.Task t = PluginManagerUI.getRunningTask ();
                    if (t != null && ! t.isFinished ()) {
                        t.addTaskListener (new TaskListener () {
                            public void taskFinished (org.openide.util.Task task) {
                                setEnabled (true);
                            }
                        });
                    } else {
                        super.setEnabled (true);
                    }
                } else {
                    super.setEnabled (false);
                }
            }
        }
        
        public void putIntoActionMap (JComponent component) {
            KeyStroke ks = (KeyStroke)getValue (ACCELERATOR_KEY);
            Object key = getValue (NAME);
            if (ks == null) {
                ks = KeyStroke.getKeyStroke ((Integer)getValue (MNEMONIC_KEY), KeyEvent.VK_ALT);
            }
            if (ks != null && key != null) {
                component.getInputMap (JComponent.WHEN_FOCUSED).put (ks, key);
                component.getActionMap ().put (key,this);
            }
        }
        
        public final void performAction () {
            if (isEnabled ()) {
                actionPerformed (null);
            }
        }
        public final void actionPerformed (ActionEvent e) {
            try {
                performerImpl ();
            } finally {
            }
        }
        
        
        public void tableDataChanged () {
            tableDataChanged (model.getMarkedUnits ());
        }
        
        public void tableDataChanged (Collection<Unit> units) {
            setEnabled (units.size () > 0);
        }
        
        
        public abstract void performerImpl ();
    }
    
    private abstract class RowTabAction extends TabAction {
        private Unit u;
        private int row;
        public RowTabAction (String nameKey, String actionCategoryKey) {
            super (nameKey, actionCategoryKey);
        }
        
        public RowTabAction (String nameKey, KeyStroke accelerator, String actionCategoryKey) {
            super (nameKey, accelerator, actionCategoryKey);
        }
        public void unitChanged (int row, Unit u) {
            this.u = u;
            this.row = row;
            unitChanged();
            }
        public final boolean isVisible (){
            return (u != null) ? isVisible (u) : isVisible(row);
        }
        private final void unitChanged () {
            if (u != null) {
                setEnabled (isEnabled (u));
                setContextName (getContextName (u));
            } else {
                setEnabled (isEnabled(row));
                setContextName (getContextName(row));
            }
        }
        
        @Override
        public void tableDataChanged () {
            unitChanged ();
        }
        
        @Override
        public void tableDataChanged (Collection<Unit> units) {
            unitChanged ();
        }
        
        public final  void performerImpl () {
            performerImpl (u);
        }
        protected boolean isVisible (Unit u) {
            return u != null;
        }
        protected boolean isVisible (int row) {
            return false;
        }        
        public abstract void performerImpl (Unit u);
        protected abstract boolean isEnabled (Unit u);
        protected boolean isEnabled (int row) {
            return false;
        }
        protected abstract String getContextName (Unit u);
        protected String getContextName (int row) {
            return getActionName();
        }
    }
    
    private class CheckAction extends RowTabAction {
        public CheckAction () {
            super ("UnitTab_CheckAction", KeyStroke.getKeyStroke (KeyEvent.VK_SPACE, 0), null);
        }
        
        public void performerImpl (Unit u) {
            final int row = getSelectedRow();
            if (model.isExpansionControlAtRow(row)) {
                if (moreAction != null && moreAction.isEnabled()) {
                    moreAction.performAction();
                } else if (lessAction != null && lessAction.isEnabled()) {
                    lessAction.performAction();
                }
            } else if (u != null && u.canBeMarked ()) {
                u.setMarked (!u.isMarked ());
            } 
            model.fireTableDataChanged ();
            restoreSelectedRow(row);
        }

        
        protected boolean isEnabled (Unit u) {
            return u != null && u.canBeMarked ();
        }
        
        @Override
        protected boolean isEnabled (int row) {
            return model.isExpansionControlAtRow(row);
        }
        
        
        protected String getContextName (Unit u) {
            return getActionName ();
        }
        
        @Override
        protected boolean isVisible (Unit u) {
            return false;
        }        
        @Override
        protected boolean isVisible (int row) {
            return false;
        }        
        
    }
    
    private class UninstallAction extends TabAction {
        public UninstallAction () {
            super ("UnitTab_bTabAction_Name_INSTALLED", null);
        }
        
        public void performerImpl () {
            boolean wizardFinished = false;
            final int row = getSelectedRow ();
            final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits ());
            UninstallUnitWizard wizard = new UninstallUnitWizard ();
            try {
                wizardFinished = wizard.invokeWizard ();
            } finally {
                Containers.forUninstall ().removeAll ();
                fireUpdataUnitChange ();
                if (!wizardFinished) {
                    UnitCategoryTableModel.restoreState (model.getUnits (), state, model.isMarkedAsDefault ());
                }
                restoreSelectedRow(row);
                refreshState ();
                focusTable ();
            }
        }
    }
    
    private class UpdateAction extends TabAction {
        public UpdateAction () {
            super ("UnitTab_bTabAction_Name_UPDATE", null);
        }
        
        public void performerImpl () {
            boolean wizardFinished = false;
            final int row = getSelectedRow();
            final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits());
            try {
                wizardFinished = new InstallUnitWizard ().invokeWizard (OperationType.UPDATE, manager);
            } finally {
                //must be called before restoreState
                fireUpdataUnitChange ();
                if (!wizardFinished) {
                    UnitCategoryTableModel.restoreState (model.getUnits (), state, model.isMarkedAsDefault ());
                }
                restoreSelectedRow(row);                
                refreshState ();
                focusTable ();
            }
        }
    }
    
    private class AvailableAction extends TabAction {
        public AvailableAction () {
            super ("UnitTab_bTabAction_Name_AVAILABLE", null);
        }
        
        public void performerImpl () {
            boolean wizardFinished = false;
            final int row = getSelectedRow();
            final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits());
            try {
                wizardFinished = new InstallUnitWizard ().invokeWizard (OperationType.INSTALL, manager);
            } finally {
                if (manager != null) {
                    fireUpdataUnitChange ();
                }
                if (!wizardFinished) {
                    UnitCategoryTableModel.restoreState (model.getUnits (), state, model.isMarkedAsDefault ());
                }
                restoreSelectedRow(row);
                refreshState ();
                focusTable ();
            }
        }
    }
    
    private class LocalUpdateAction extends TabAction {
        public LocalUpdateAction () {
            super ("UnitTab_bTabAction_Name_LOCAL", null);
        }
        public void performerImpl () {
            boolean wizardFinished = false;
            final int row = getSelectedRow();
            final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits ());
            
            try {
                wizardFinished = new InstallUnitWizard ().invokeWizard (OperationType.LOCAL_DOWNLOAD, manager);
            } finally {
                // fireUpdataUnitChange ();
                if (wizardFinished) {
                    reloadTask (false).schedule (10);
                } else {
                    UnitCategoryTableModel.restoreState (model.getUnits (), state, model.isMarkedAsDefault ());
                    restoreSelectedRow(row);                
                    refreshState ();
                }
                focusTable ();
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
            String category = u.getCategoryName();
            List<Unit> units = model.getUnits();
            for (Unit unit : units) {
                if (unit != null && category.equals(unit.getCategoryName()) && !unit.isMarked()) {
                    retval = true;
                    break;
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
            final int row = getSelectedRow();        
            for (int i = 0; i < count; i++) {
                u = model.getUnitAtRow (i);
                if (u != null && category.equals (u.getCategoryName ()) && !u.isMarked () && u.canBeMarked ()) {
                    u.setMarked (true);
                }
            }
            model.fireTableDataChanged ();
            restoreSelectedRow(row);
        }
                
        @Override
        protected boolean isVisible (Unit u) {
              return super.isVisible (u);
        }
    }
    
    private class ActivateAction extends TabAction {
        public ActivateAction () {
            super ("UnitTab_ActivateAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK),*/ "EnableDisable");
        }

        @Override
        public void tableDataChanged(Collection<Unit> units) {
            if (units.isEmpty()) {
                setEnabled(false);
                return;
            }
            for (Unit u : units) {
                if (!isEnabled(u)) {
                    setEnabled(false);
                    return;
                }
            }
            setEnabled(true);
        }

        @Override
        public void performerImpl() {
            final int row = getSelectedRow ();
            final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits ());
            OperationContainer<OperationSupport> c = Containers.forEnable();
            for (Unit u : model.getUnits()) {
                if (u.isMarked()) {
                    c.add(u.updateUnit, u.getRelevantElement());
                }
            }
            UninstallUnitWizard wizard = new UninstallUnitWizard ();
            wizard.invokeWizard (true);
            Containers.forEnable ().removeAll ();
            restoreSelectedRow(row);
            refreshState ();
            focusTable ();
        }
        /*
        public void performerImpl (Unit u) {
            Unit.Installed unit = (Unit.Installed)u;
            final int row = getSelectedRow();

            if (!unit.getRelevantElement ().isEnabled ()) {
                OperationInfo info = Containers.forEnable ().add (unit.updateUnit, unit.getRelevantElement ());
                assert info != null;
                UninstallUnitWizard wizard = new UninstallUnitWizard ();
                wizard.invokeWizard (true);
                Containers.forEnable ().removeAll ();
            }
            fireUpdataUnitChange ();
            restoreSelectedRow(row);
            focusTable();
        }
         */
        
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
    }
    
    private class ActivateCategoryAction extends RowTabAction {
        public ActivateCategoryAction () {
            super ("UnitTab_ActivateCategoryAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK),*/ "EnableDisable");
        }
        
        protected boolean isEnabled (Unit uu) {
            boolean retval = false;
            
            String category = uu.getCategoryName();
            List<Unit> units = model.getUnits();
            for (Unit u : units) {
                if ((u != null) && (u instanceof Unit.Installed) && category.equals(u.getCategoryName())) {
                    Unit.Installed installed = (Unit.Installed) u;
                    if (!installed.getRelevantElement().isEnabled()) {
                        retval = Unit.Installed.isOperationAllowed(installed.updateUnit, installed.getRelevantElement(), Containers.forEnable());
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
            final int row = getSelectedRow();
            
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
            fireUpdataUnitChange ();
            restoreSelectedRow(row);
            focusTable();
        }
                
        @Override
        protected boolean isVisible (Unit u) {
            return isEnabled();
        }
    }
    
    
    private class DeactivateAction extends TabAction {
        public DeactivateAction () {
            super ("UnitTab_DeactivateAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK),*/ "EnableDisable");
        }
        
        @Override
        public void tableDataChanged(Collection<Unit> units) {
            if (units.isEmpty()) {
                setEnabled(false);
                return;
            }

            for (Unit u : units) {
                if (!isEnabled(u)) {
                    setEnabled(false);
                    return;
                }
            }
            setEnabled(true);
        }

        @Override
        public void performerImpl() {
            final int row = getSelectedRow ();
            OperationContainer<OperationSupport> c = Containers.forDisable();
            for (Unit u : model.getUnits()) {
                if (u.isMarked()) {
                    c.add(u.updateUnit, u.getRelevantElement());
                }
            }
            UninstallUnitWizard wizard = new UninstallUnitWizard ();
            if (wizard.invokeWizard (false)) {
                Containers.forUninstall().removeAll();
            }
            Containers.forDisable().removeAll();
            restoreSelectedRow(row);
            refreshState ();
            focusTable ();
        }
        /*
            Unit.Installed unit = (Unit.Installed)u;
            final int row = getSelectedRow();

            if (unit.getRelevantElement ().isEnabled ()) {
                OperationInfo info = Containers.forDisable ().add (unit.updateUnit, unit.getRelevantElement ());
                assert info != null;
                UninstallUnitWizard wizard = new UninstallUnitWizard ();
                if (wizard.invokeWizard (false)) {
                    Containers.forUninstall ().remove (unit.getRelevantElement ());
                }
                Containers.forDisable ().removeAll ();
            }
            fireUpdataUnitChange ();
            restoreSelectedRow(row);
            focusTable ();
        */
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
    }
    
    private class DeactivateCategoryAction extends RowTabAction {
        public DeactivateCategoryAction () {
            super ("UnitTab_DeactivateCategoryAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK),*/ "EnableDisable");
        }
        
        protected boolean isEnabled (Unit uu) {
            boolean retval = false;
            
            String category = uu.getCategoryName();
            List<Unit> units = model.getUnits();
            for (Unit u : units) {
                if ((u != null) && (u instanceof Unit.Installed) && category.equals(u.getCategoryName())) {
                    Unit.Installed installed = (Unit.Installed) u;
                    if (installed.getRelevantElement().isEnabled()) {
                        retval = Unit.Installed.isOperationAllowed(installed.updateUnit, installed.getRelevantElement(), Containers.forDisable());
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
            final int row = getSelectedRow();
            
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
            fireUpdataUnitChange ();
            restoreSelectedRow(row);
            focusTable ();            
        }
        @Override
        protected boolean isVisible (Unit u) {
            return isEnabled();
        }
    }
    
    private class UncheckCategoryAction extends RowTabAction {
        public UncheckCategoryAction () {
            super ("UnitTab_UncheckCategoryAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),*/ "Uncheck");
        }
        @Override
        protected boolean isEnabled (Unit u) {
            boolean retval = false;
            
            String category = u.getCategoryName();
            List<Unit> units = model.getUnits();
            for (Unit uu : units) {
                if (uu != null && category.equals(uu.getCategoryName()) && uu.isMarked()) {
                    retval = true;
                    break;
                }
            }
            return retval;
        }
        
        @Override
        public void performerImpl (Unit u) {
            String category = u.getCategoryName ();
            final int row = getSelectedRow();
            
            int count = model.getRowCount ();
            for (int i = 0; i < count; i++) {
                u = model.getUnitAtRow (i);
                if (u != null && category.equals (u.getCategoryName ()) && u.isMarked () && u.canBeMarked ()) {
                    u.setMarked (false);
                }
            }
            model.fireTableDataChanged ();
            restoreSelectedRow(row);
            focusTable();
        }
        
        @Override
        protected boolean isVisible (Unit u) {
            return super.isVisible(u);
        }
        
        protected String getContextName (Unit u) {
            return getActionName () + " \"" + u.getCategoryName ()+"\""; //NOI18N
        }
    }
    private class CheckAllAction extends RowTabAction {
        public CheckAllAction () {
            super ("UnitTab_CheckAllAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),*/"Check");
        }
        
        public void performerImpl (Unit uu) {
            final int row = getSelectedRow();
            Collection<Unit> allUnits = model.getUnits();
            for (Unit u : allUnits) {
                if (u != null && !u.isMarked () &&  u.canBeMarked ()) {
                    u.setMarked (true);
                }                
            }
            model.fireTableDataChanged ();
            restoreSelectedRow(row);
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
            final int row = getSelectedRow();
            Collection<Unit> markedUnits = model.getMarkedUnits();
            for (Unit u : markedUnits) {
                if (u != null && u.isMarked ()  && u.canBeMarked ()) {
                    u.setMarked (false);
                }                
            }
            model.fireTableDataChanged ();            
            restoreSelectedRow(row);
        }
        
        protected boolean isEnabled (Unit uu) {
            return true;
        }
        
        protected String getContextName (Unit u) {
            return getActionName ();
        }
    }
    
    private class MoreAction extends RowTabAction {
        public MoreAction () {
            super ("UnitTab_MoreAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), */"Expand");//NOI18N
        }
        public void performerImpl (Unit uu) {
            try {
                setWaitingState(true);
                final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits ());            
                model.setExpanded(true);
                fireUpdataUnitChange ();
                UnitCategoryTableModel.restoreState (model.getUnits (), state, model.isMarkedAsDefault ());
                focusTable();
            } finally {
                setWaitingState(false);
            }
        }

        @Override
        protected boolean isVisible(Unit u) {
            return isEnabled(u);
        }

        @Override
        protected boolean isVisible(int row) {
            return !model.isExpansionControlAtRow(row) && isEnabled(row);
        }
                
        protected boolean isEnabled (Unit uu) {
            return uu != null && model.isExpansionControlPresent() && model.isCollapsed();
        }

        @Override
        protected boolean isEnabled (int row) {
            return model.isExpansionControlPresent() && model.isCollapsed();
        }
        
        protected String getContextName (Unit u) {
            return getActionName();
        }
    }    
    
    private class LessAction extends RowTabAction {
        public LessAction () {
            super ("UnitTab_LessAction", /*KeyStroke.getKeyStroke (KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), */"Expand");//NOI18N
        }
        public void performerImpl (Unit uu) {
            try {
                setWaitingState(true);
                final Map<String, Boolean> state = UnitCategoryTableModel.captureState(model.getUnits());
                model.setExpanded(false);
                fireUpdataUnitChange();
                UnitCategoryTableModel.restoreState(model.getUnits(), state, model.isMarkedAsDefault());
                focusTable();
            } finally {
                setWaitingState(false);
            }            
        }

        @Override
        protected boolean isVisible(Unit u) {
            return isEnabled(u);
        }

        @Override
        protected boolean isVisible(int row) {
            return !model.isExpansionControlAtRow(row) && isEnabled(row);
        }
                
        protected boolean isEnabled (Unit uu) {
            return uu != null && model.isExpansionControlPresent() && model.isExpanded();
        }

        @Override
        protected boolean isEnabled (int row) {
            return model.isExpansionControlPresent() && model.isExpanded();
        }
                
        protected String getContextName (Unit u) {
            return getActionName();
        }
    }    
    
    
    private class ReloadAction extends TabAction {
        Task reloadTask = null;
        public ReloadAction () {
            super ("UnitTab_ReloadAction", KeyStroke.getKeyStroke (KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK), null);
            String tooltip = NbBundle.getMessage (UnitTab.class, "UnitTab_Tooltip_RefreshAction");//NOI18N
            putValue (TOOL_TIP_TEXT_KEY, tooltip);
            setEnabled (false);
        }

        public void performerImpl () {
            setEnabled (false);
            reloadTask = reloadTask (true);
        }

        @Override
        public void setEnabled (boolean enabled) {
            super.setEnabled (enabled);
            super.firePropertyChange ("enabled", !isEnabled (), isEnabled ());
        }
    }
    
    private class AddLocallyDownloadedAction extends TabAction {
        public AddLocallyDownloadedAction () {
            super ("UnitTab_bAddLocallyDownloads_Name", null);
            topButton.getAccessibleContext ().setAccessibleName (NbBundle.getMessage(UnitTab.class, "UnitTab_bAddLocallyDownloads_ACN")); // NOI18N
            topButton.getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage(UnitTab.class, "UnitTab_bAddLocallyDownloads_ACD")); // NOI18N
            String tooltip = NbBundle.getMessage (UnitTab.class, "UnitTab_Tooltip_AddAction_LOCAL");//NOI18N
            putValue (TOOL_TIP_TEXT_KEY, tooltip);
        }
        
        public void performerImpl () {
            final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits ());
            if (getLocalDownloadSupport ().chooseNbmFiles ()) {
                
                final Runnable addUpdates = new Runnable (){
                    public void run () {
                        final LocallyDownloadedTableModel downloadedTableModel = ((LocallyDownloadedTableModel) model);
                        List<UpdateUnit> empty = Collections.emptyList();
                        downloadedTableModel.setUnits(empty);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run () {
                                fireUpdataUnitChange();
                                UnitCategoryTableModel.restoreState (model.getUnits (), state, model.isMarkedAsDefault ());                                
                                refreshState ();
                                setWaitingState (false);
                            }
                        });
                    }
                    
                };
                setWaitingState (true);
                Utilities.startAsWorkerThread (addUpdates, 250);
            }
        }
        
        @Override
        public void setEnabled (boolean enabled) {
            super.setEnabled (enabled);
            super.firePropertyChange ("enabled", !isEnabled (), isEnabled ());
        }
    }
    
    private class RemoveLocallyDownloadedAction extends RowTabAction {
        public RemoveLocallyDownloadedAction () {
            super ("UnitTab_RemoveLocallyDownloadedAction",  null);
            String tooltip = NbBundle.getMessage (UnitTab.class, "UnitTab_Tooltip_RemoveAction_LOCAL");//NOI18N
            putValue (TOOL_TIP_TEXT_KEY, tooltip);
        }
        
        protected boolean isEnabled (Unit uu) {
            return uu != null && (model.getType ().equals (UnitCategoryTableModel.Type.LOCAL));
        }
        
        @Override
        public boolean isEnabled () {
            if (super.isEnabled ()) {
                return table.getSelectedRow () > -1;
            } else {
                return false;
            }
        }
        
        @Override
        public void setEnabled (boolean enabled) {
            super.setEnabled (enabled);
            super.firePropertyChange ("enabled", !isEnabled (), isEnabled ());
        }
        public void performerImpl (final Unit unit) {
            final Runnable removeUpdates = new Runnable (){
                public void run () {
                    final Map<String, Boolean> state = UnitCategoryTableModel.captureState (model.getUnits ());
                    try {
                        if (unit.isMarked ()) {
                            //this removes it from container
                            unit.setMarked (false);
                        }
                        getLocalDownloadSupport ().remove (unit.updateUnit);
                        getLocalDownloadSupport ().getUpdateUnits ();
                    } finally {
                        SwingUtilities.invokeLater (new Runnable () {
                            public void run () {
                                fireUpdataUnitChange();
                                UnitCategoryTableModel.restoreState (model.getUnits (), state, model.isMarkedAsDefault ());
                                refreshState ();
                                setWaitingState (false);
                            }
                        });
                    }
                }
            };
            setWaitingState (true);
            Utilities.startAsWorkerThread (removeUpdates, 250);
        }
        
        protected String getContextName (Unit u) {
            return getActionName ();//NOI18N
        }
        
        @Override
        protected boolean isVisible (Unit u) {
            return false;
        }        
    }
    private class EnableRenderer extends  DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent (
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel renderComponent = (JLabel)super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof Boolean) {
                Unit u = model.getUnitAtRow (row);
                if (u != null && u.getRelevantElement ().getUpdateUnit ().isPending ()) {
                    renderComponent.setIcon (ImageUtilities.loadImageIcon("org/netbeans/modules/autoupdate/ui/resources/restart.png", false)); // NOI18N
                } else {
                    Boolean state = (Boolean)value;
                    if (state.booleanValue()) {
                        renderComponent.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/autoupdate/ui/resources/active.png", false)); // NOI18N
                    } else {
                        renderComponent.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/autoupdate/ui/resources/inactive.png", false)); // NOI18N
                    }
                }
                renderComponent.setText ("");
                renderComponent.setHorizontalAlignment (SwingConstants.CENTER);
                
            }
            Component retval = renderComponent;
            return retval;
        }
    }
     
    class SourceCategoryRenderer extends  DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent (
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel renderComponent = (JLabel)super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof CATEGORY) {
                Unit u = model.getUnitAtRow (row);
                if (u instanceof Unit.Available) {
                    Unit.Available a = (Unit.Available)u;
                    CATEGORY state = a.getSourceCategory();
                    URL icon = Utilities.getCategoryIcon(state);
                    renderComponent.setIcon(new ImageIcon(icon));
                    renderComponent.setText ("");
                    renderComponent.setHorizontalAlignment (SwingConstants.CENTER);
                }
                
            }
            Component retval = renderComponent;
            return retval;
        }
    }    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bTabAction;
    private javax.swing.JButton bTabAction1;
    private javax.swing.JButton bTabAction2;
    private javax.swing.JCheckBox detailView;
    private javax.swing.JLabel lSearch;
    private javax.swing.JLabel lSelectionInfo;
    private javax.swing.JLabel lWarning;
    private javax.swing.JSplitPane spTab;
    private javax.swing.JTextField tfSearch;
    private javax.swing.JButton topButton;
    // End of variables declaration//GEN-END:variables
    
}
