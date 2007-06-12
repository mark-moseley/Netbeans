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
package org.netbeans.modules.sql.framework.ui.view;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.util.List;
import javax.swing.Action;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
        
import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.etl.ui.ETLDataObject;
import org.netbeans.modules.etl.ui.ETLEditorSupport;
import org.netbeans.modules.model.database.DBTable;
import org.netbeans.modules.sql.framework.common.utils.FlatfileDBMarker;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.ui.editor.property.impl.PropertyViewManager;
import org.netbeans.modules.sql.framework.ui.graph.ICommand;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.model.CollabSQLUIModel;
import org.netbeans.modules.sql.framework.ui.model.SQLUIModel;
import org.netbeans.modules.sql.framework.ui.view.conditionbuilder.ConditionBuilderUtil;
import org.netbeans.modules.sql.framework.ui.view.conditionbuilder.ConditionBuilderView;
import org.netbeans.modules.sql.framework.ui.view.graph.BasicSQLViewFactory;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLBasicTableArea;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLCollaborationView;
import org.netbeans.modules.sql.framework.ui.view.join.JoinMainDialog;
import org.netbeans.modules.sql.framework.ui.view.join.JoinUtility;
import org.netbeans.modules.sql.framework.ui.view.property.FFSourceTableProperties;
import org.netbeans.modules.sql.framework.ui.view.property.FFTargetTableProperties;
import org.netbeans.modules.sql.framework.ui.view.property.SQLResourceManager;
import org.netbeans.modules.sql.framework.ui.view.property.SourceTableProperties;
import org.netbeans.modules.sql.framework.ui.view.property.TargetTableProperties;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.Logger;
import org.netbeans.modules.etl.ui.view.ETLOutputWindowTopComponent;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;

/**
 * Main view of SQL Framework
 *
 * @author Wei Han
 * @version $Revision$
 */
public abstract class BasicTopView extends JPanel implements IGraphViewContainer, IOutputViewContainer {
    
    protected static abstract class ConditionValidator implements ActionListener {
        
        static final class DataValidation extends ConditionValidator {
            private SourceTable mTable;
            
            public DataValidation(SQLBasicTableArea gNode, SourceTable table, ConditionBuilderView view, Dialog dlg, CollabSQLUIModel sqlModel) {
                super(gNode, view, dlg, sqlModel);
                mTable = table;
            }
            
            protected void setCondition(SQLCondition cond) {
                SQLCondition oldCondition = mTable.getExtractionCondition();
                if (cond != null) {
                    if (!cond.equals(oldCondition)) {
                        mTable.setDataValidationCondition(cond);
                        mSqlModel.setDirty(true);
                    }
                }
            }
        }
        
        static final class ExtractionFilter extends ConditionValidator {
            private SourceTable mTable;
            
            public ExtractionFilter(SQLBasicTableArea gNode, SourceTable table, ConditionBuilderView view, Dialog dlg, CollabSQLUIModel sqlModel) {
                super(gNode, view, dlg, sqlModel);
                mTable = table;
            }
            
            protected void setCondition(SQLCondition cond) {
                SQLCondition oldCondition = mTable.getExtractionCondition();
                if (cond != null) {
                    if (!cond.equals(oldCondition)) {
                        mTable.setExtractionCondition(cond);
                        mSqlModel.setDirty(true);
                    }
                }
            }
        }
        
        static final class TargetJoinConditioon extends ConditionValidator {
            private TargetTable mTable;
            
            public TargetJoinConditioon(SQLBasicTableArea gNode, TargetTable table, ConditionBuilderView view, Dialog dlg, CollabSQLUIModel sqlModel) {
                super(gNode, view, dlg, sqlModel);
                mTable = table;
            }
            
            protected void setCondition(SQLCondition cond) {
                SQLCondition oldCondition = mTable.getJoinCondition();
                if (cond != null) {
                    if (!cond.equals(oldCondition)) {
                        mTable.setJoinCondition(cond);
                        mSqlModel.setDirty(true);
                    }
                }
            }
        }
        
        static final class TargetFilterCondition extends ConditionValidator {
            private TargetTable mTable;
            
            public TargetFilterCondition(SQLBasicTableArea gNode, TargetTable table, ConditionBuilderView view, Dialog dlg, CollabSQLUIModel sqlModel) {
                super(gNode, view, dlg, sqlModel);
                mTable = table;
            }
            
            protected void setCondition(SQLCondition cond) {
                SQLCondition oldCondition = mTable.getFilterCondition();
                if (cond != null) {
                    if (!cond.equals(oldCondition)) {
                        mTable.setFilterCondition(cond);
                        mSqlModel.setDirty(true);
                    }
                }
            }
        }
        
        protected Dialog mDialog;
        protected SQLBasicTableArea mTableNode;
        protected ConditionBuilderView mView;
        protected CollabSQLUIModel mSqlModel;
        
        protected ConditionValidator(SQLBasicTableArea gNode, ConditionBuilderView view, Dialog dialog, CollabSQLUIModel sqlModel) {
            mTableNode = gNode;
            mView = view;
            mDialog = dialog;
            mSqlModel = sqlModel;
        }
        
        public void actionPerformed(ActionEvent e) {
            if (NotifyDescriptor.OK_OPTION.equals(e.getSource())) {
                if (!mView.isConditionValid()) {
                    NotifyDescriptor confirmDlg = new NotifyDescriptor.Confirmation(NbBundle.getMessage(BasicTopView.class,
                            "ERR_close_on_invalid_condition"), mDialog.getTitle(), NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(confirmDlg);
                    if (confirmDlg.getValue() != NotifyDescriptor.YES_OPTION) {
                        return;
                    }
                }
                
                setCondition((SQLCondition) mView.getPropertyValue());
                if(mTableNode != null) {
                    mTableNode.setConditionIcons();
                }
            }
            
            mDialog.dispose();
        }
        
        protected abstract void setCondition(SQLCondition cond);
    }
    
    private static final String LOG_CATEGORY = BasicTopView.class.getName();
    
    protected SQLCollaborationView collabView;
    protected CollabSQLUIModel sqlModel;
    private HashMap outputDataViewMap = new HashMap();
    
    private SQLOutputView outputView;
    private PropertyViewManager pvMgr;
    private HashMap rejectionDataViewMap = new HashMap();
    private HashMap sqlViewMap = new HashMap();
    
    /**
     * New instance
     *
     * @param propertyMgr - PropertyViewManager
     * @param model - CollabSQLUIModelImpl
     */
    public BasicTopView(PropertyViewManager propertyMgr, CollabSQLUIModel model) {
        this.pvMgr = propertyMgr;
        this.sqlModel = model;
        initGui();
    }
    
    /**
     * Is editable
     *
     * @return boolean - true/false
     */
    public boolean canEdit() {
        return true;
    }
    
    public void enableToolBarActions(boolean b) {
        List actions = this.getToolBarActions();
        Iterator it = actions.iterator();
        while (it.hasNext()) {
            Action action = (Action) it.next();
            if (action != null) {
                action.setEnabled(b);
            }
        }
    }
    
    /**
     * Execute a command
     *
     * @param command - command
     * @param args - arguments
     */
    public Object[] execute(String command, Object[] args) {
        if (command.equals(ICommand.SHOW_SQL_CMD)) {
            showSql((SQLObject) args[0]);
        } else if (command.equals(ICommand.SHOW_DATA_CMD)) {
            showDataOutputView((SQLObject) args[0]);
        } else if (command.equals(ICommand.SHOW_REJECTION_DATA_CMD)) {
            showRejectionDataOutputView((SQLObject) args[0]);
        } else if (command.equals(ICommand.SHOW_PROPERTY_CMD)) {
            IGraphNode graphNode = (IGraphNode) args[0];
            Boolean bool = (Boolean) args[1];
            this.showPropertiesDialog(graphNode, bool.booleanValue());
        } else if (command.equals(ICommand.CONFIG_CMD)) {
            // Integer tableType = (Integer) args[0];
        } else if (command.equals(ICommand.EDIT_JOINVIEW)) {
            editJoinView((SQLJoinView) args[0]);
        } else if (command.equals(ICommand.DATA_VALIDATION)) {
            SQLBasicTableArea graphNode = (SQLBasicTableArea) args[0];
            doDataValidation(graphNode, (SourceTable) args[1]);
        } else if (command.equals(ICommand.DATA_EXTRACTION)) {
            SQLBasicTableArea graphNode = (SQLBasicTableArea) args[0];
            showDataExtraction(graphNode, (SourceTable) args[1]);
        } else if (command.equals(ICommand.SHOW_TARGET_JOIN_CONDITION_CMD)) {
            SQLBasicTableArea graphNode = (SQLBasicTableArea) args[0];
            showTargetJoinCondition(graphNode, (TargetTable) args[1]);
        } else if (command.equals(ICommand.SHOW_TARGET_FILTER_CONDITION_CMD)) {
            SQLBasicTableArea graphNode = (SQLBasicTableArea) args[0];
            showTargetFilterCondition(graphNode, (TargetTable) args[1]);
        }
        
        return null;
    }
    
    /**
     * Document this
     *
     * @param dataObj - data object
     * @return - IGraphNode
     */
    public IGraphNode findGraphNode(Object dataObj) {
        return this.collabView.findGraphNode(dataObj);
    }
    
    /**
     * Return SQLCollaborationView
     *
     * @return SQLCollaborationView
     */
    public SQLCollaborationView getCollaborationView() {
        return this.collabView;
    }
    
    /**
     * Return actions for popup menu of graph area
     *
     * @return a list of actions
     */
    public abstract List getGraphActions();
    
    /**
     * Return SQLGraphView
     *
     * @return SQLGraphView
     */
    public IGraphView getGraphView() {
        return this.collabView.getGraphView();
    }
    
    /**
     * Return the operator folder name
     *
     * @return operator folder name
     */
    public abstract String getOperatorFolder();
    
    /**
     * Return actions for toolbar
     *
     * @return a list of actions
     */
    public abstract List getToolBarActions();
    
    /**
     * get initial zoom factor
     *
     * @return initial zoom factor
     */
    public double getZoomFactor() {
        return this.collabView.getZoomFactor();
    }
    
    /**
     * Hides output view from bottom portion of a split pane.
     */
    public void hideSplitPaneView() {
        // close the output panel.
        ETLOutputWindowTopComponent topComp = ETLOutputWindowTopComponent.findInstance();
        if(topComp.isVisible()) {
            topComp.setVisible(false);
        }        
    }
    
    public void setModifiable(boolean b) {
        this.collabView.getGraphView().setModifiable(b);
        enableToolBarActions(b);
    }
    
    /**
     * set the zoom factor
     *
     * @param factor zoom factor
     */
    public void setZoomFactor(double factor) {
        this.collabView.setZoomFactor(factor);
    }
    
    /**
     * Shows output view in bottom portion of a split pane.
     *
     * @param c - component
     */
    public void showSplitPaneView(Component c) {
        // add to output.
        ETLOutputWindowTopComponent topComp = ETLOutputWindowTopComponent.findInstance();
        if(!topComp.isOpened()) {
            topComp.open();
        }
        topComp.setVisible(true);
        topComp.addComponent(c);
    }
    
    public void setDirty(boolean dirty) {
        sqlModel.setDirty(dirty);
        SQLUIModel model = (SQLUIModel) getGraphView().getGraphModel();
        model.setDirty(dirty);
    }
    
    protected SQLStatementPanel getOrCreateSQLStatementPanel(SQLObject obj) {
        SQLStatementPanel c = (SQLStatementPanel) sqlViewMap.get(obj.getId());
        if (c == null) {
            c = new SQLStatementPanel(this, obj);
            sqlViewMap.put(obj.getId(), c);
        } else {
            c.updateSQLObject(obj);
        }
        return c;
    }
    
    /**
     * show properties dialog
     */
    protected void showPropertiesDialog(IGraphNode gNode, boolean modal) {
        String template = null;
        SQLObject bean = (SQLObject) gNode.getDataObject();
        
        if (bean == null) {
            return;
        }
        
        Object pBean = null;
        boolean isFlatFileTable = false;
        if( (((DBTable)bean).getParent().getSource()) instanceof FlatfileDBMarker) {
            isFlatFileTable = true;
        }
        
        if (bean.getObjectType() == SQLConstants.SOURCE_TABLE) {
            SourceTableProperties srcTableBaen = new SourceTableProperties(this, (SQLBasicTableArea) gNode, (SourceTable) bean);
            if(isFlatFileTable) {
                template = "FFSourceTable";
                pBean = new FFSourceTableProperties(srcTableBaen);
            } else {
                template = "SourceTable";
                pBean = srcTableBaen;
            }
            
        } else if (bean.getObjectType() == SQLConstants.TARGET_TABLE) {
            TargetTableProperties trgtTableBaen = new TargetTableProperties(this, (SQLBasicTableArea) gNode, (TargetTable) bean);
            if(isFlatFileTable) {
                template = "FFTargetTable";
                pBean = new FFTargetTableProperties(trgtTableBaen);
            } else {
                template = "TargetTable";
                pBean = trgtTableBaen;
            }
        }
        
        if (template == null || pBean == null) {
            return;
        }
        
        if (pvMgr == null) {
            InputStream stream = null;
            try {
                stream = BasicTopView.class.getClassLoader().getResourceAsStream("org/netbeans/modules/sql/framework/ui/resources/sql_properties.xml");
                pvMgr = new PropertyViewManager(stream, new SQLResourceManager());
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ignore) {
                        // ignore
                    }
                }
            }
        }
        
        pvMgr.showNBDialog(pBean, template, modal);
        updateActions();
    }
    
    /**
     * Generates and displays associated SQL statement for the given SQLObject.
     *
     * @param obj SQLObject whose SQL statement is to be displayed
     */
    protected void showSql(SQLObject obj) {
        SQLStatementPanel c = getOrCreateSQLStatementPanel(obj);
        c.refreshSql();
        showSplitPaneView(c);
    }
    
    private void doDataValidation(SQLBasicTableArea gNode, SourceTable table) {
        ConditionBuilderView cView = ConditionBuilderUtil.getValidationConditionBuilderView(table,
                (IGraphViewContainer) this.getGraphView().getGraphViewContainer());
        String title = NbBundle.getMessage(BasicTopView.class, "LBL_validation_condition");
        
        // Create a Dialog that defers decision-making on whether to close the dialog to
        // an ActionListener.
        DialogDescriptor dd = new DialogDescriptor(cView, title, true, NotifyDescriptor.OK_CANCEL_OPTION, null, null);
        
        // Pushes closing logic to ActionListener impl
        dd.setClosingOptions(new Object[0]);
        
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        ActionListener dlgListener = new ConditionValidator.DataValidation(gNode, table, cView, dlg, sqlModel);
        dd.setButtonListener(dlgListener);
        
        dlg.setModal(true);
        dlg.setVisible(true);
    }
    
    private void editJoinView(SQLJoinView jView) {
        JoinMainDialog.showJoinDialog(sqlModel.getSQLDefinition().getJoinSources(), jView, this.getGraphView());
        if (JoinMainDialog.getClosingButtonState() == JoinMainDialog.OK_BUTTON) {
            SQLJoinView modifiedJoinView = JoinMainDialog.getSQLJoinView();
            if (!jView.equals(modifiedJoinView)) {
                sqlModel.setDirty(true);
            }
            List tableNodes = JoinMainDialog.getTableColumnNodes();
            try {
                JoinUtility.editJoinView(jView, modifiedJoinView, modifiedJoinView.getSourceTables(), tableNodes, this.getGraphView());
            } catch (BaseException ex) {
                Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, "editJoinView", "Caught Exception while commiting join view edits.", ex);
                
                NotifyDescriptor d = new NotifyDescriptor.Message(ex.toString(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        }
        updateActions();
    }
    
    private void initGui() {        
        BasicSQLViewFactory viewFactory = new BasicSQLViewFactory(sqlModel, this, this.getGraphActions(), this.getToolBarActions());
        this.collabView = new SQLCollaborationView(viewFactory);
        // create output view
        outputView = new SQLOutputView(this);
        setLayout(new BorderLayout());
        add(this.collabView, BorderLayout.CENTER);
    }
    
    private void showDataExtraction(SQLBasicTableArea gNode, SourceTable table) {
        ConditionBuilderView cView = ConditionBuilderUtil.getConditionBuilderView(table,
                (IGraphViewContainer) this.getGraphView().getGraphViewContainer());
        String title = NbBundle.getMessage(BasicTopView.class, "LBL_extraction_condition");
        
        // Create a Dialog that defers decision-making on whether to close the dialog to
        // an ActionListener.
        DialogDescriptor dd = new DialogDescriptor(cView, title, true, NotifyDescriptor.OK_CANCEL_OPTION, null, null);
        
        // Pushes closing logic to ActionListener impl
        dd.setClosingOptions(new Object[0]);
        
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        ActionListener dlgListener = new ConditionValidator.ExtractionFilter(gNode, table, cView, dlg, sqlModel);
        dd.setButtonListener(dlgListener);
        
        dlg.setModal(true);
        dlg.setVisible(true);
        updateActions();
    }
    
    /**
     * simply show the data of all the rows and column of the given table
     *
     * @param table - table
     */
    private void showDataOutputView(final SQLObject table) {
        SQLUIModel model = (SQLUIModel) getGraphView().getGraphModel();
        if (!(model instanceof CollabSQLUIModel)) {
            return;
        }
        
        SQLDefinition def = ((CollabSQLUIModel) model).getSQLDefinition();
        DataOutputPanel dataView = (DataOutputPanel) outputDataViewMap.get(table.getId());
        
        if (dataView == null) {
            if (table.getObjectType() == SQLConstants.TARGET_TABLE) {
                dataView = new DataOutputPanel.TargetQuery((TargetTable) table, def);
            } else if (table.getObjectType() == SQLConstants.SOURCE_TABLE) {
                dataView = new DataOutputPanel.SourceQuery((SourceTable) table, def);
            } else if(table.getObjectType() == SQLConstants.JOIN_VIEW) {
                dataView = new DataOutputPanel.JoinViewQuery((SQLJoinView) table, def);
            } else if(table.getObjectType() == SQLConstants.JOIN) {
                dataView = new DataOutputPanel.JoinOperatorQuery((SQLJoinOperator) table, def);
            }
            
            outputDataViewMap.put(table.getId(), dataView);
        }
        
        dataView.generateResult(table);
        showSplitPaneView(dataView);
    }
    
    /**
     * simply show the data of all the rows and column of the given table
     *
     * @param table - table
     */
    private void showRejectionDataOutputView(final SQLObject table) {
        SQLUIModel model = (SQLUIModel) getGraphView().getGraphModel();
        if (!(model instanceof CollabSQLUIModel)) {
            return;
        }
        
        SQLDefinition def = ((CollabSQLUIModel) model).getSQLDefinition();
        DataOutputPanel view = (DataOutputPanel) rejectionDataViewMap.get(table.getId());
        if (view == null) {
            view = new DataOutputPanel.RejectedRows(table, def);
            rejectionDataViewMap.put(table.getId(), view);
        }
        
        view.generateResult(table);
        showSplitPaneView(view);
    }
    
    private void showTargetJoinCondition(final SQLBasicTableArea gNode, final TargetTable table) {
        ConditionBuilderView cView = ConditionBuilderUtil.getJoinConditionBuilderView(table,
                (IGraphViewContainer) this.getGraphView().getGraphViewContainer());
        String title = NbBundle.getMessage(BasicTopView.class, "LBL_target_join_condition");
        
        // Create a Dialog that defers decision-making on whether to close the dialog to
        // an ActionListener.
        DialogDescriptor dd = new DialogDescriptor(cView, title, true, NotifyDescriptor.OK_CANCEL_OPTION, null, null);
        
        // Pushes closing logic to ActionListener impl
        dd.setClosingOptions(new Object[0]);
        
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        ActionListener dlgListener = new ConditionValidator.TargetJoinConditioon(gNode, table, cView, dlg, sqlModel);
        dd.setButtonListener(dlgListener);
        
        dlg.setModal(true);
        dlg.setVisible(true);
        updateActions();
    }
    
    private void showTargetFilterCondition(final SQLBasicTableArea gNode, final TargetTable table) {
        ConditionBuilderView cView = ConditionBuilderUtil.getFilterConditionBuilderView(table,
                (IGraphViewContainer) this.getGraphView().getGraphViewContainer());
        String title = NbBundle.getMessage(BasicTopView.class, "LBL_target_filter_condition");
        
        // Create a Dialog that defers decision-making on whether to close the dialog to
        // an ActionListener.
        DialogDescriptor dd = new DialogDescriptor(cView, title, true, NotifyDescriptor.OK_CANCEL_OPTION, null, null);
        
        // Pushes closing logic to ActionListener impl
        dd.setClosingOptions(new Object[0]);
        
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        ActionListener dlgListener = new ConditionValidator.TargetFilterCondition(gNode, table, cView, dlg, sqlModel);
        dd.setButtonListener(dlgListener);
        
        dlg.setModal(true);
        dlg.setVisible(true);
        updateActions();
    }
    
    private boolean isDirty() {
        return sqlModel.isDirty();
    }
    private void updateActions() {
        if( isDirty() ) {
            //SQLUIModel model = (SQLUIModel) getGraphView().getGraphModel();
            /*IToolBar toolBar = this.getToolBar();
            if (toolBar == null) {
                return;
            }
             
            Action undoAction = toolBar.getAction(UndoAction.class);
            Action redoAction = toolBar.getAction(RedoAction.class);
            UndoManager undoManager = model.getUndoManager();
            if (undoManager != null && undoAction != null && redoAction != null) {
                undoAction.setEnabled(undoManager.canUndo());
                redoAction.setEnabled(undoManager.canRedo());
            }*/
            try{
                ETLDataObject etlDataObject = DataObjectProvider.getProvider().getActiveDataObject();
                ETLEditorSupport editor = etlDataObject.getETLEditorSupport();
                editor.synchDocument();
            } catch(Exception e){
                //ignore
            }
        }
    }
}
