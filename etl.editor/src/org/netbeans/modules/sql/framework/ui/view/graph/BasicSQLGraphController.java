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
package org.netbeans.modules.sql.framework.ui.view.graph;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer;
import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.etl.ui.ETLDataObject;
import org.netbeans.modules.etl.ui.ETLEditorSupport;
import org.netbeans.modules.jdbc.builder.DBMetaData;
import org.netbeans.modules.jdbc.builder.ForeignKeyColumn;
import org.netbeans.modules.jdbc.builder.KeyColumn;
import org.netbeans.modules.jdbc.builder.Table;
import org.netbeans.modules.jdbc.builder.TableColumn;
import org.netbeans.modules.model.database.DBConnectionDefinition;
import org.netbeans.modules.model.database.DBTable;
import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.netbeans.modules.sql.framework.model.GUIInfo;
import org.netbeans.modules.sql.framework.model.RuntimeDatabaseModel;
import org.netbeans.modules.sql.framework.model.RuntimeInput;
import org.netbeans.modules.sql.framework.model.RuntimeOutput;
import org.netbeans.modules.sql.framework.model.SQLCanvasObject;
import org.netbeans.modules.sql.framework.model.SQLCastOperator;
import org.netbeans.modules.sql.framework.model.SQLConnectableObject;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLOperator;
import org.netbeans.modules.sql.framework.model.SQLOperatorArg;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.SourceColumn;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetColumn;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.VisibleSQLLiteral;
import org.netbeans.modules.sql.framework.model.impl.ForeignKeyImpl;
import org.netbeans.modules.sql.framework.model.impl.PrimaryKeyImpl;
import org.netbeans.modules.sql.framework.model.impl.RuntimeDatabaseModelImpl;
import org.netbeans.modules.sql.framework.model.impl.RuntimeOutputImpl;
import org.netbeans.modules.sql.framework.model.impl.SQLCustomOperatorImpl;
import org.netbeans.modules.sql.framework.model.impl.SourceColumnImpl;
import org.netbeans.modules.sql.framework.model.impl.SourceTableImpl;
import org.netbeans.modules.sql.framework.model.impl.TargetColumnImpl;
import org.netbeans.modules.sql.framework.model.impl.TargetTableImpl;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.netbeans.modules.sql.framework.ui.event.SQLDataEvent;
import org.netbeans.modules.sql.framework.ui.graph.IGraphController;
import org.netbeans.modules.sql.framework.ui.graph.IGraphLink;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.graph.IGraphPort;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.netbeans.modules.sql.framework.ui.graph.impl.CustomOperatorNode;
import org.netbeans.modules.sql.framework.ui.model.CollabSQLUIModel;
import org.netbeans.modules.sql.framework.ui.model.SQLUIModel;
import org.netbeans.modules.sql.framework.ui.view.join.JoinMainDialog;
import org.netbeans.modules.sql.framework.ui.view.join.JoinUtility;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.Logger;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class BasicSQLGraphController implements IGraphController {
    
    private static final String NETBEANS_DBTABLE_MIMETYPE = "application/x-java-netbeans-dbexplorer-table;class=org.netbeans.api.db.explorer.DatabaseMetaDataTransfer$Table";
    
    private static final String LOG_CATEGORY = BasicSQLGraphController.class.getName();
    
    private static DataFlavor[] mDataFlavorArray = new DataFlavor[1];
    
    static {
        try {
            mDataFlavorArray[0] = new DataFlavor(NETBEANS_DBTABLE_MIMETYPE);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    
    protected SQLUIModel collabModel;
    protected IGraphView viewC;
    
    private String srcParam = null;
    private String destParam = null;
    
    private transient int tableTypeSelected = SQLConstants.SOURCE_TABLE;
    
    /**
     * Handle drop.
     *
     * @param e DropTargetDropEvent
     */
    public void handleDrop(java.awt.dnd.DropTargetDropEvent e) {
        if (!isEditAllowed()) {
            return;
        }
        boolean dropStatus = false;
        Point loc = e.getLocation();
        if (e.isDataFlavorSupported(mDataFlavorArray[0])) {
            Connection conn = null;
            try {
                Transferable t = e.getTransferable();
                Object o = t.getTransferData(mDataFlavorArray[0]);
                if (o instanceof DatabaseMetaDataTransfer.Table) {
                    DatabaseConnection dbConn = ((DatabaseMetaDataTransfer.Table)o).getDatabaseConnection();
                    conn = ((DatabaseMetaDataTransfer.Table)o).getDatabaseConnection().getJDBCConnection();
                    String tableName = ((DatabaseMetaDataTransfer.Table)o).getTableName();
                    String schema = ((DatabaseMetaDataTransfer.Table)o).getDatabaseConnection().getSchema();
                    String url = ((DatabaseMetaDataTransfer.Table)o).getDatabaseConnection().getDatabaseURL();
                    String catalog = null;
                    try {
                        catalog = conn.getCatalog();
                    } catch (Exception ex) {
                        //ignore
                    }
                    String dlgTitle = null;
                    try {
                        dlgTitle = NbBundle.getMessage(BasicSQLGraphController.class, "TITLE_dlg_table_type");
                    } catch (MissingResourceException mre) {
                        dlgTitle = "Add a table";
                    }
                    
                    CollabSQLUIModel sqlModel = ((CollabSQLUIModel) collabModel);
                    TypeSelectorPanel selectorPnl = new TypeSelectorPanel(tableTypeSelected);
                    DialogDescriptor dlgDesc = null;
                    DBMetaData dbMeta = new DBMetaData();
                    dlgDesc = new DialogDescriptor(selectorPnl, dlgTitle, true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION,
                            DialogDescriptor.DEFAULT_ALIGN, null, null);
                    
                    Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);
                    dlg.setVisible(true);
                    SQLDBTable sTable = null;
                    if (NotifyDescriptor.OK_OPTION == dlgDesc.getValue()) {
                        e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        tableTypeSelected = selectorPnl.getSelectedType();
                        boolean isSource = false;
                        if (SQLConstants.SOURCE_TABLE == tableTypeSelected) {
                            isSource = true;
                        }
                        String[][] tableList = null;
                        try {
                            dbMeta.connectDB(conn);
                            schema = (schema == null)? "" : schema;
                            catalog = (catalog == null)? "" : catalog;
                            tableList = dbMeta.getTablesOnly(catalog, schema, "", false);
                        } catch (Exception ex) {
                            //ignore
                        }
                        Object dbTable = createTable(tableList, tableName, isSource);
                        List tbls = null;
                        if(isSource) {
                            tbls = sqlModel.getSQLDefinition().getSourceTables();
                        } else {
                            tbls = sqlModel.getSQLDefinition().getTargetTables();
                        }
                        
                        ((SQLDBTable)dbTable).setAliasUsed(true);
                        ((SQLDBTable)dbTable).setAliasName(generateTableAliasName(isSource, tbls));
                        DBConnectionDefinition def = null;
                        try {
                            def = SQLModelObjectFactory.getInstance().createDBConnectionDefinition(dbConn.getDisplayName(),
                                    dbMeta.getDBType(), dbConn.getDriverClass(), dbConn.getDatabaseURL(), dbConn.getUser(),
                                    dbConn.getPassword(), "Descriptive info here");
                        } catch (Exception ex) {
                            //ignore
                        }
                        SQLDBModel model = null;
                        if(isSource) {
                            model = SQLModelObjectFactory.getInstance()
                            .createDBModel(SQLConstants.SOURCE_DBMODEL);
                        } else {
                            model = SQLModelObjectFactory.getInstance()
                            .createDBModel(SQLConstants.TARGET_DBMODEL);
                        }
                        model.setModelName(dbConn.getDisplayName());
                        model.setConnectionDefinition(def);
                        dbTable = addTableColumns(dbMeta, dbTable, isSource);
                        ((SQLDBTable)dbTable).setEditable(true);
                        ((SQLDBTable)dbTable).setSelected(true);
                        model.addTable((SQLDBTable)dbTable);
                        if(isSource) {
                            sTable = (SQLDBTable) collabModel.addSourceTable((SQLDBTable)dbTable, loc);
                        } else {
                            sTable = (SQLDBTable) collabModel.addTargetTable((SQLDBTable)dbTable, loc);
                            RuntimeDatabaseModel rtModel = sqlModel.getSQLDefinition().getRuntimeDbModel();
                            if(rtModel == null) {
                                rtModel = new RuntimeDatabaseModelImpl();
                            }
                            RuntimeOutput rtOut = rtModel.getRuntimeOutput();
                            SQLDBColumn column = null;
                            if(rtOut == null) {
                                rtOut = new RuntimeOutputImpl();
                                // add STATUS
                                column = SQLModelObjectFactory.getInstance().createTargetColumn("STATUS", Types.VARCHAR, 0, 0, true);
                                column.setEditable(false);
                                rtOut.addColumn(column);
                                
                                // add STARTTIME
                                column = SQLModelObjectFactory.getInstance().createTargetColumn("STARTTIME", Types.TIMESTAMP, 0, 0, true);
                                column.setEditable(false);
                                rtOut.addColumn(column);
                                
                                // add ENDTIME
                                column = SQLModelObjectFactory.getInstance().createTargetColumn("ENDTIME", Types.TIMESTAMP, 0, 0, true);
                                column.setEditable(false);
                                rtOut.addColumn(column);
                            }
                            String argName = SQLObjectUtil.getTargetTableCountRuntimeOutput(
                                    (TargetTable) sTable);
                            column = SQLModelObjectFactory.getInstance().createTargetColumn(
                                    argName, Types.INTEGER, 0, 0, true);
                            column.setEditable(false);
                            rtOut.addColumn(column);
                            rtModel.addTable(rtOut);
                            sqlModel.getSQLDefinition().addObject(rtModel);
                        }
                        
                        SourceColumn runtimeArg = SQLObjectUtil.createRuntimeInput
                                (sTable, sqlModel.getSQLDefinition());
                        
                        if (runtimeArg != null && (RuntimeInput) runtimeArg.getParent() != null) {
                            RuntimeInput runtimeInput = (RuntimeInput) runtimeArg.getParent();
                            // if runtime input is not in SQL definition then add it
                            if ((sqlModel.getSQLDefinition().isTableExists(runtimeInput)) == null) {
                                sqlModel.getSQLDefinition().addObject((SQLObject)runtimeInput);
                                collabModel.addObject(runtimeInput);
                                SQLDataEvent evt = new SQLDataEvent(collabModel, runtimeInput , runtimeArg);
                                collabModel.fireChildObjectCreatedEvent(evt);
                            }
                        }
                        if(sqlModel.getSQLDefinition().getSourceTables().size() > 1) {
                            if(dbTable instanceof SourceTableImpl) {
                                NotifyDescriptor d = new NotifyDescriptor.Confirmation("Do you want to create a join?",
                                        "Confirm join creation", NotifyDescriptor.YES_NO_OPTION);
                                if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.YES_OPTION) {
                                    JoinMainDialog.showJoinDialog(sqlModel.getSQLDefinition().getJoinSources(), null,
                                            this.viewC, true);
                                    if (JoinMainDialog.getClosingButtonState() == JoinMainDialog.OK_BUTTON) {
                                        SQLJoinView joinView = JoinMainDialog.getSQLJoinView();
                                        try {
                                            if (joinView != null) {
                                                JoinUtility.handleNewJoinCreation(joinView, JoinMainDialog.getTableColumnNodes(), this.viewC);
                                            }
                                        } catch (BaseException ex) {
                                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Error adding join view.", NotifyDescriptor.INFORMATION_MESSAGE));
                                            
                                            Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, "JoinAction.actionPerformed", "error adding join view", ex);
                                        }
                                    }
                                }
                            }
                        }
                        collabModel.setDirty(true);
                        updateActions(collabModel);
                        dropStatus = true;
                    }
                } else {
                    e.rejectDrop();
                }
            } catch (IOException ex) {
                Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, this, "Caught IOException while handling DnD.", ex);
                e.rejectDrop();
            } catch (UnsupportedFlavorException ex) {
                Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, this, "Caught UnsupportedFlavorException while handling DnD.", ex);
                e.rejectDrop();
            } catch (BaseException ex) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ex.getLocalizedMessage(), NotifyDescriptor.WARNING_MESSAGE));
                Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, this, "Caught BaseException while handling DnD.", ex);
                e.rejectDrop();
            } catch(Exception ex) {
                Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, this, "Caught Exception while handling DnD.", ex);
                e.rejectDrop();
            } finally {
                e.dropComplete(dropStatus);
                try {
                    if(conn != null) {
                        conn.close();
                    }
                } catch (SQLException ex) {
                    conn = null;
                }
            }
        } else {
            e.rejectDrop();
        }
    }
    
    /**
     * Handle drop of arbitrary object.
     *
     * @param obj Object dropped onto canvas
     */
    public void handleObjectDrop(Object obj) {
        if (!isEditAllowed()) {
            return;
        }
    }
    
    /**
     * handle new link
     *
     * @param from IGraphPort
     * @param to IGraphPort
     */
    public void handleLinkAdded(IGraphPort from, IGraphPort to) {
        if (!isEditAllowed()) {
            return;
        }
        
        IGraphNode srcGraphNode = null;
        IGraphNode destGraphNode = null;
        
        srcGraphNode = from.getDataNode();
        destGraphNode = to.getDataNode();
        
        if (srcGraphNode != null && destGraphNode != null && srcGraphNode.equals(destGraphNode)) {
            return;
        }
        
        setParameters(from, to, srcGraphNode, destGraphNode);
        
        SQLCanvasObject srcObj = (SQLCanvasObject) srcGraphNode.getDataObject();
        SQLConnectableObject destObj = (SQLConnectableObject) destGraphNode.getDataObject();
        
        if (srcObj == null && destObj == null) {
            return;
        }
        
        SQLInputObject inputObj = destObj.getInput(destParam);
        SQLObject existing = (inputObj != null) ? inputObj.getSQLObject() : null;
        if (existing instanceof TargetColumn) {
            existing = ((TargetColumn) existing).getValue();
        }
        
        if (existing != null) {
            return;
        }
        
        try {
            // do type checking
            boolean userResponse = doTypeChecking(srcObj, destObj, srcParam, destParam);
            
            if (!userResponse) {
                return;
            }
            
            CollabSQLUIModel sqlModel = (CollabSQLUIModel) this.collabModel;
            SourceTable s1 = SQLObjectUtil.getInputSourceTable(srcObj, sqlModel.getSQLDefinition().getAllObjects());
            SourceTable s2 = SQLObjectUtil.getInputSourceTable(destObj, sqlModel.getSQLDefinition().getAllObjects());
            
            TargetTable t1 = SQLObjectUtil.getMappedTargetTable(srcObj, sqlModel.getSQLDefinition().getTargetTables());
            TargetTable t2 = SQLObjectUtil.getMappedTargetTable(destObj, sqlModel.getSQLDefinition().getTargetTables());
            
            if (t1 == null && s1 != null) {
                t1 = SQLObjectUtil.getMappedTargetTable(s1, sqlModel.getSQLDefinition().getTargetTables());
            }
            
            if (t2 == null && s2 != null) {
                t2 = SQLObjectUtil.getMappedTargetTable(s2, sqlModel.getSQLDefinition().getTargetTables());
            }
            
            SQLJoinView jv1 = sqlModel.getJoinView(s1);
            SQLJoinView jv2 = sqlModel.getJoinView(s2);
            
            // join view is not null but a source table of join view which is not
            // directly linked to target is s1 so we need to find target based on join
            // view
            if (jv1 != null && t1 == null) {
                t1 = SQLObjectUtil.getMappedTargetTable(jv1, sqlModel.getSQLDefinition().getTargetTables());
            }
            
            if (jv2 != null && t2 == null) {
                t2 = SQLObjectUtil.getMappedTargetTable(jv2, sqlModel.getSQLDefinition().getTargetTables());
            }
            
            // we have target table but join view is still null, so check target
            // if it has a join view
            if (jv1 == null && t1 != null) {
                jv1 = t1.getJoinView();
            }
            
            if (jv2 == null && t2 != null) {
                jv2 = t2.getJoinView();
            }
            
            // can not map it as both source tables are mapped to different
            // target tables or can not map if both source tables belong to different
            // join view
            if (t1 != null && t2 != null && !t1.equals(t2)) {
                String msg = NbBundle.getMessage(BasicSQLGraphController.class, "ERROR_bad_link_src_mapped_to_tgttbl", srcObj.getDisplayName(),
                        t1.getDisplayName());
                
                if (!(srcObj instanceof SourceTable) && s1 != null) {
                    msg = NbBundle.getMessage(BasicSQLGraphController.class, "ERROR_bad_link_src_mapped_to_mapped_srctbl", srcObj.getDisplayName(),
                            s1.getName(), t1.getDisplayName());
                }
                
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                
                DialogDisplayer.getDefault().notify(d);
                
                return;
            } else if (jv1 != null && jv2 != null && !jv1.equals(jv2)) {
                NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle.getMessage(BasicSQLGraphController.class, "ERROR_bad_joinview_link",
                        jv1.getAliasName(), jv2.getAliasName()), NotifyDescriptor.INFORMATION_MESSAGE);
                
                DialogDisplayer.getDefault().notify(d);
                return;
            }
            
            if (s1 != null && s2 != null && !s1.equals(s2)) {
                // if both source tables are not part of a join view then prompt user for
                // a join view
                if (jv1 == null && jv2 == null) {
                    ArrayList sTables = new ArrayList();
                    sTables.add(s1);
                    sTables.add(s2);
                    this.promptForNewJoinView(srcObj, destObj, sTables);
                    return;
                } else if (jv1 == null && jv2 != null) {
                    this.promptForAddToExistingJoinView(srcObj, destObj, s1, jv2);
                    return;
                } else if (jv1 != null && jv2 == null) {
                    this.promptForAddToExistingJoinView(srcObj, destObj, s2, jv1);
                    return;
                }
            }
            
            // create actual link
            createLink(srcObj, destObj);
            
            if (destObj instanceof TargetTable) {
                SQLTargetTableArea tt = (SQLTargetTableArea) destGraphNode;
                TargetColumn tCol = (TargetColumn) inputObj.getSQLObject();
                if ((tCol != null) && (tCol.isPrimaryKey())) {
                    tt.setConditionIcons();
                }
            }
            updateActions(collabModel);
        } catch (Exception sqle) {
            NotifyDescriptor d = new NotifyDescriptor.Message(sqle.toString(), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }
    
    private void createLink(SQLCanvasObject srcObj, SQLConnectableObject destObj) throws BaseException {
        if (srcObj != null && destObj != null) {
            collabModel.createLink(srcObj, srcParam, destObj, destParam);
        }
    }
    
    private void promptForNewJoinView(final SQLCanvasObject srcObj, final SQLConnectableObject destObj, final List sTables) {
        
        Runnable runDialog = new Runnable() {
            public void run() {
                try {
                    if (promptForNewJoinView(sTables)) {
                        // create actual link
                        createLink(srcObj, destObj);
                    }
                } catch (Exception ex) {
                    NotifyDescriptor d = new NotifyDescriptor.Message(ex.toString(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                }
            }
        };
        
        SwingUtilities.invokeLater(runDialog);
    }
    
    private boolean promptForNewJoinView(List sTables) throws BaseException {
        boolean userResponse = false;
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(NbBundle.getMessage(BasicSQLGraphController.class,
                "ERROR_two_tbl_map_create_join_view"), NotifyDescriptor.WARNING_MESSAGE);
        Object response = DialogDisplayer.getDefault().notify(d);
        
        if (response.equals(NotifyDescriptor.OK_OPTION)) {
            CollabSQLUIModel sqlModel = (CollabSQLUIModel) this.collabModel;
            List joinSources = sqlModel.getSQLDefinition().getJoinSources();
            
            joinSources.removeAll(sTables);
            JoinMainDialog.showJoinDialog(joinSources, sTables, this.viewC, false);
            
            if (JoinMainDialog.getClosingButtonState() == JoinMainDialog.OK_BUTTON) {
                SQLJoinView joinView = JoinMainDialog.getSQLJoinView();
                if (joinView != null) {
                    sqlModel.setDirty(true);
                    JoinUtility.handleNewJoinCreation(joinView, JoinMainDialog.getTableColumnNodes(), this.viewC);
                    userResponse = true;
                }
            }
        }
        
        return userResponse;
    }
    
    private void promptForAddToExistingJoinView(final SQLCanvasObject srcObj, final SQLConnectableObject destObj, final SourceTable sTable,
            final SQLJoinView initJoinView) {
        Runnable runDialog = new Runnable() {
            public void run() {
                try {
                    if (promptForAddToExistingJoinView(sTable, initJoinView)) {
                        // create actual link
                        createLink(srcObj, destObj);
                    }
                } catch (Exception ex) {
                    NotifyDescriptor d = new NotifyDescriptor.Message(ex.toString(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                }
            }
        };
        
        SwingUtilities.invokeLater(runDialog);
        
    }
    
    private boolean promptForAddToExistingJoinView(SourceTable sTable, SQLJoinView initJoinView) throws BaseException {
        boolean userResponse = false;
        
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(NbBundle.getMessage(BasicSQLGraphController.class, "ERROR_add_table_join_view",
                sTable.getName()), NotifyDescriptor.WARNING_MESSAGE);
        Object response = DialogDisplayer.getDefault().notify(d);
        
        if (response.equals(NotifyDescriptor.OK_OPTION)) {
            CollabSQLUIModel sqlModel = (CollabSQLUIModel) this.collabModel;
            List joinSources = sqlModel.getSQLDefinition().getJoinSources();
            joinSources.remove(sTable);
            JoinMainDialog.showJoinDialog(joinSources, sTable, initJoinView, this.viewC);
            if (JoinMainDialog.getClosingButtonState() == JoinMainDialog.OK_BUTTON) {
                SQLJoinView joinView = JoinMainDialog.getSQLJoinView();
                
                if (!initJoinView.equals(joinView)) {
                    sqlModel.setDirty(true);
                }
                // join sources
                List jSources = initJoinView.getSourceTables();
                jSources.add(sTable);
                // call this
                JoinUtility.editJoinView(initJoinView, joinView, jSources, JoinMainDialog.getTableColumnNodes(), this.viewC);
                userResponse = true;
            }
            
        }
        
        return userResponse;
    }
    
    /** Creates a new instance of SQLGraphController */
    public BasicSQLGraphController() {
    }
    
    private boolean doTypeChecking(SQLCanvasObject srcObj, SQLConnectableObject destObj, String srcParam1, String destParam1) throws BaseException {
        
        String msg = null;
        SQLObject input = srcObj;
        
        // get the specific sub object from srcObj which we are trying to link
        input = srcObj.getOutput(srcParam1);
        
        if (!destObj.isInputValid(destParam1, input)) {
            try {
                String srcObjType = TagParserUtility.getDisplayStringFor(input.getObjectType());
                String destObjType = TagParserUtility.getDisplayStringFor(destObj.getObjectType());
                String srcName = destObj.getDisplayName();
                
                if (srcName != null && destParam1 != null) {
                    msg = NbBundle.getMessage(BasicSQLGraphController.class, "ERR_object_check_incompatible_with_argnames", new String[] {
                        srcObjType, destObjType, destObj.getDisplayName(), destParam1});
                } else {
                    msg = NbBundle.getMessage(BasicSQLGraphController.class, "ERR_object_check_incompatible_no_argnames", srcObjType, destObjType);
                }
            } catch (Exception e) {
                Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, this, "Caught Exception while resolving error message.", e);
                msg = "Cannot link these objects together.";
            }
            
            NotifyDescriptor.Message m = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            
            DialogDisplayer.getDefault().notify(m);
            return false;
        }
        
        switch (destObj.isInputCompatible(destParam1, input)) {
            case SQLConstants.TYPE_CHECK_INCOMPATIBLE:
                try {
                    msg = NbBundle.getMessage(BasicSQLGraphController.class, "ERR_type_check_incompatible");
                } catch (MissingResourceException e) {
                    msg = "Incompatible source and target datatypes.";
                }
                
                NotifyDescriptor.Message m = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                
                DialogDisplayer.getDefault().notify(m);
                return false;
                
            case SQLConstants.TYPE_CHECK_DOWNCAST_WARNING:
                try {
                    msg = NbBundle.getMessage(BasicSQLGraphController.class, "ERR_type_check_downcast");
                } catch (MissingResourceException e) {
                    msg = "Connecting these datatypes may result in a loss of " + "precision or data truncation in the target.  Continue?";
                }
                
                String title = null;
                try {
                    title = NbBundle.getMessage(BasicSQLGraphController.class, "TITLE_dlg_type_check_confirm");
                } catch (MissingResourceException e) {
                    title = "Datatype conversion";
                }
                
                NotifyDescriptor.Confirmation d = new NotifyDescriptor.Confirmation(msg, title, NotifyDescriptor.OK_CANCEL_OPTION,
                        NotifyDescriptor.QUESTION_MESSAGE);
                
                return (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION);
                
            case SQLConstants.TYPE_CHECK_COMPATIBLE:
            default:
                return true;
        }
    }
    
    private void setParameters(IGraphPort from, IGraphPort to, IGraphNode srcGraphNode, IGraphNode destGraphNode) {
        
        if (srcGraphNode != null && destGraphNode != null) {
            srcParam = srcGraphNode.getFieldName(from);
            destParam = destGraphNode.getFieldName(to);
        }
        
    }
    
    /**
     * handle link deletion
     *
     * @param link IGraphLink
     */
    public void handleLinkDeleted(IGraphLink link) {
        if (!isEditAllowed()) {
            return;
        }
        
        IGraphPort from = link.getFromGraphPort();
        IGraphPort to = link.getToGraphPort();
        IGraphNode srcGraphNode = from.getDataNode();
        IGraphNode destGraphNode = to.getDataNode();
        
        setParameters(from, to, srcGraphNode, destGraphNode);
        
        // source is always canvas object and destination is always expression object
        SQLCanvasObject srcObj = (SQLCanvasObject) srcGraphNode.getDataObject();
        SQLConnectableObject destObj = (SQLConnectableObject) destGraphNode.getDataObject();
        
        if (srcObj == null && destObj == null) {
            return;
        }
        
        try {
            collabModel.removeLink(srcObj, srcParam, destObj, destParam);
            
            if (destObj instanceof TargetTable) {
                SQLTargetTableArea tt = (SQLTargetTableArea) destGraphNode;
                SQLInputObject inputObj = destObj.getInput(destParam);
                TargetColumn tCol = (TargetColumn) inputObj.getSQLObject();
                if ((tCol != null) && (tCol.isPrimaryKey())) {
                    tt.setConditionIcons();
                }
            }
            
        } catch (Exception e) {
            NotifyDescriptor d = new NotifyDescriptor.Message(e.toString(), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }
    
    /**
     * handle node add
     *
     * @param xmlInfo IOperatorXmlInfo
     * @param dropLocation dropLocation
     */
    public void handleNodeAdded(IOperatorXmlInfo xmlInfo, Point dropLocation) {
        if (!isEditAllowed()) {
            return;
        }
        
        // what object type is dropped
        String className = xmlInfo.getObjectClassName();
        
        try {
            // create object
            SQLCanvasObject sqlObj = collabModel.createObject(className);
            sqlObj.setDisplayName(xmlInfo.getName());
            
            GUIInfo guiInfo = sqlObj.getGUIInfo();
            guiInfo.setX(dropLocation.x);
            guiInfo.setY(dropLocation.y);
            
            // do special processing for following objects
            switch (sqlObj.getObjectType()) {
                case SQLConstants.CAST_OPERATOR:
                    CastAsDialog castDlg = new CastAsDialog(WindowManager.getDefault().getMainWindow(), NbBundle.getMessage(
                            BasicSQLGraphController.class, "TITLE_new_castas"), true);
                    castDlg.show();
                    if (castDlg.isCanceled()) {
                        return;
                    }
                    
                    SQLCastOperator castOp = (SQLCastOperator) sqlObj;
                    castOp.setOperatorXmlInfo(xmlInfo);
                    
                    castOp.setJdbcType(castDlg.getJdbcType());
                    
                    int precision = castDlg.getPrecision();
                    castOp.setPrecision(precision);
                    
                    int scale = castDlg.getScale();
                    castOp.setScale(scale);
                    
                    break;
                    
                case SQLConstants.CUSTOM_OPERATOR:
                    CustomOperatorPane customOptPane = new CustomOperatorPane(new ArrayList());
                    String title = NbBundle.getMessage(BasicSQLGraphController.class, "TITLE_user_function");
                    DialogDescriptor dlgDesc = new DialogDescriptor(customOptPane, title, true, NotifyDescriptor.OK_CANCEL_OPTION,
                            NotifyDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);
                    Dialog customOptDialog = DialogDisplayer.getDefault().createDialog(dlgDesc);
                    customOptDialog.setVisible(true);
                    if (NotifyDescriptor.CANCEL_OPTION == dlgDesc.getValue()) {
                        return;
                    }
                    List inputArgs = customOptPane.getArgList();
                    SQLOperatorArg retType = customOptPane.getReturnType();
                    CustomOperatorNode customOptNode = new CustomOperatorNode(xmlInfo, inputArgs, retType);
                    SQLCustomOperatorImpl custOp = (SQLCustomOperatorImpl) sqlObj;
                    custOp.setOperatorXmlInfo(customOptNode);
                    custOp.setCustomOperatorName(customOptPane.getFunctionName());
                    custOp.getOperatorDefinition().setArgList(inputArgs);
                    custOp.initializeInputs(inputArgs.size());
                    break;
                    
                    
                case SQLConstants.VISIBLE_PREDICATE:
                    ((SQLPredicate) sqlObj).setOperatorXmlInfo(xmlInfo);
                    // fall through to set XML info (using common SQLOperator interface)
                    
                case SQLConstants.GENERIC_OPERATOR:
                case SQLConstants.DATE_ARITHMETIC_OPERATOR:
                    // for operator we need to set the type of operator
                    // ((SQLGenericOperator) sqlObj).setOperatorType(xmlInfo.getName());
                    ((SQLOperator) sqlObj).setOperatorXmlInfo(xmlInfo);
                    sqlObj.setDisplayName(xmlInfo.getDisplayName());
                    break;
                    
                case SQLConstants.VISIBLE_LITERAL:
                    LiteralDialog dlg = new LiteralDialog(WindowManager.getDefault().getMainWindow(), NbBundle.getMessage(
                            BasicSQLGraphController.class, "TITLE_new_literal"), true);
                    dlg.show();
                    
                    // OK button is not pressed so return
                    if (dlg.isCanceled()) {
                        return;
                    }
                    
                    String value = dlg.getLiteral();
                    VisibleSQLLiteral lit = (VisibleSQLLiteral) sqlObj;
                    lit.setJdbcType(dlg.getType());
                    lit.setValue(value);
                    lit.setDisplayName(xmlInfo.getDisplayName());
                    
                    break;
            }
            
            // now add the object
            collabModel.addObject(sqlObj);
        } catch (BaseException e) {
            NotifyDescriptor d = new NotifyDescriptor.Message(e.toString(), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }
    
    /**
     * handle node deletion
     *
     * @param node IGraphNode
     */
    public void handleNodeRemoved(IGraphNode node) {
        if (!isEditAllowed()) {
            return;
        }
        
        try {
            IGraphNode pNode = node.getParentGraphNode();
            // if node has a parent then we should delete it from parent and return
            // we do not need to go to collaboration as node is contained within
            // its parent and deleting it from its parent should remove it
            if (pNode != null) {
                pNode.removeChildNode(node);
                return;
            }
            
            SQLCanvasObject sqlObj = (SQLCanvasObject) node.getDataObject();
            if (sqlObj != null) {
                collabModel.removeObject(sqlObj);
            }
            
            // if a source table is deleted, check if it is flatfile and try to remove
            // its auto generated runtime input argument for file location
            if (sqlObj.getObjectType() == SQLConstants.SOURCE_TABLE || sqlObj.getObjectType() == SQLConstants.TARGET_TABLE) {
                SourceColumn col = SQLObjectUtil.removeRuntimeInput((SQLDBTable) sqlObj, (CollabSQLUIModel) collabModel);
                if(col != null) {
                    SQLDataEvent evt = new SQLDataEvent(collabModel, (RuntimeInput)col.getParent()  , col);
                    collabModel.fireChildObjectDeletedEvent(evt);
                }
            }
            updateActions(collabModel);
        } catch (Exception e) {
            Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, this, "Caught exception while removing object.", e);
            
            NotifyDescriptor d = new NotifyDescriptor.Message(e.toString(), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }
    
    private String generateTableAliasName(boolean isSource, List sTables) {
        int cnt = 1;
        String aliasPrefix = isSource ? "S" : "T";
        String aName = aliasPrefix + cnt;
        while (isTableAliasNameExist(aName, sTables)) {
            cnt++;
            aName = aliasPrefix + cnt;
        }
        
        return aName;
    }
    
    private boolean isTableAliasNameExist(String aName, List sTables) {
        
        Iterator it = sTables.iterator();
        
        while (it.hasNext()) {
            SQLDBTable tTable = (SQLDBTable) it.next();
            String tAlias = tTable.getAliasName();
            if (tAlias != null && tAlias.equals(aName)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Sets the data model which this controller modifies
     *
     * @param newModel new data model
     */
    public void setDataModel(Object newModel) {
        collabModel = (SQLUIModel) newModel;
        
    }
    
    public Object getDataModel() {
        return collabModel;
    }
    
    class TypeSelectorPanel extends JPanel {
        private ButtonGroup bg;
        private JRadioButton source;
        private JRadioButton target;
        
        public TypeSelectorPanel() {
            this(SQLConstants.SOURCE_TABLE);
        }
        
        public TypeSelectorPanel(int newType) {
            super();
            setLayout(new BorderLayout());
            
            JPanel insetPanel = new JPanel();
            insetPanel.setLayout(new BoxLayout(insetPanel, BoxLayout.PAGE_AXIS));
            insetPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 15));
            
            String title = "";
            try {
                title = NbBundle.getMessage(BasicSQLGraphController.class, "TITLE_panel_table_type");
            } catch (MissingResourceException mre) {
                title = "Specify table type:";
            }
            
            insetPanel.add(new JLabel(title));
            
            String sourceLabel = "";
            try {
                sourceLabel = NbBundle.getMessage(BasicSQLGraphController.class, "BTN_table_type_source");
            } catch (MissingResourceException mre) {
                sourceLabel = "Source table";
            }
            
            String targetLabel = "";
            try {
                targetLabel = NbBundle.getMessage(BasicSQLGraphController.class, "BTN_table_type_target");
            } catch (MissingResourceException mre) {
                targetLabel = "Target table";
            }
            
            source = new JRadioButton(sourceLabel);
            target = new JRadioButton(targetLabel);
            
            insetPanel.add(source);
            insetPanel.add(target);
            
            add(insetPanel, BorderLayout.CENTER);
            
            bg = new ButtonGroup();
            bg.add(source);
            bg.add(target);
            
            setSelectedType(newType);
        }
        
        public void setSelectedType(int type) {
            switch (type) {
                case SQLConstants.TARGET_TABLE:
                    bg.setSelected(target.getModel(), true);
                    break;
                    
                case SQLConstants.SOURCE_TABLE:
                default:
                    bg.setSelected(source.getModel(), true);
            }
        }
        
        public int getSelectedType() {
            return target.isSelected() ? SQLConstants.TARGET_TABLE : SQLConstants.SOURCE_TABLE;
        }
        
        public void addNotify() {
            super.addNotify();
            
            switch (getSelectedType()) {
                case SQLConstants.TARGET_TABLE:
                    target.requestFocusInWindow();
                    break;
                    
                case SQLConstants.SOURCE_TABLE:
                default:
                    source.requestFocusInWindow();
                    break;
            }
        }
    }
    
    protected boolean isEditAllowed() {
        if (viewC != null) {
            return viewC.canEdit();
        }
        
        return true;
    }
    
    /**
     * set the view from which this controller interacts
     *
     * @param view view
     */
    public void setView(Object view) {
        viewC = (IGraphView) view;
    }
    
    private void updateActions(SQLUIModel model) {
        try{
            ETLDataObject etlDataObject = DataObjectProvider.getProvider()
            .getActiveDataObject();
            if(model.isDirty()){
                ETLEditorSupport editor = etlDataObject.getETLEditorSupport();
                editor.synchDocument();
            }
        } catch(Exception e){
            //ignore
        }
    }
    
    private Object addTableColumns(DBMetaData dbMeta, Object ffTable, boolean isSource) {
        Table tbl = null;
        try {
            tbl = dbMeta.getTableMetaData(((SQLDBTable)ffTable).getCatalog(),
                    ((SQLDBTable)ffTable).getSchema(), ((SQLDBTable)ffTable).getName(), "TABLE");
        } catch (Exception ex) {
            //ignore
        }
        TableColumn[] cols = tbl.getColumns();
        TableColumn tc = null;
        List pks = tbl.getPrimaryKeyColumnList();
        List pkCols = new ArrayList();
        Iterator it = pks.iterator();
        while(it.hasNext()) {
            KeyColumn kc = (KeyColumn)it.next();
            pkCols.add(kc.getColumnName());
        }
        if(pks.size()!=0) {
            PrimaryKeyImpl pkImpl = new PrimaryKeyImpl(((KeyColumn)tbl.getPrimaryKeyColumnList().get(0)).getName(), pkCols, true);
            if(ffTable instanceof SourceTableImpl) {
                ((SourceTableImpl)ffTable).setPrimaryKey(pkImpl);
            } else {
                ((TargetTableImpl)ffTable).setPrimaryKey(pkImpl);
            }
        }
        List fkList = tbl.getForeignKeyColumnList();
        it = fkList.iterator();
        while(it.hasNext()) {
            ForeignKeyColumn fkCol = (ForeignKeyColumn)it.next();
            ForeignKeyImpl fkImpl = new ForeignKeyImpl((DBTable)ffTable, fkCol.getName(), fkCol.getImportKeyName(),
                    fkCol.getImportTableName(), fkCol.getImportSchemaName(), fkCol.getImportCatalogName(), fkCol.getUpdateRule(),
                    fkCol.getDeleteRule(), fkCol.getDeferrability());
            List fkColumns = new ArrayList();
            fkColumns.add(fkCol.getColumnName());
            String catalog = fkCol.getImportCatalogName();
            if (catalog == null) {
                catalog = "";
            }
            String schema = fkCol.getImportSchemaName();
            if(schema == null) {
                schema = "";
            }
            try {
                pks = new ArrayList();
                pks = dbMeta.getPrimaryKeys(catalog, schema, fkCol.getImportTableName());
            } catch (Exception ex) {
                // ignore
            }
            List pkColumns = new ArrayList();
            Iterator pksIt = pks.iterator();
            while(pksIt.hasNext()) {
                KeyColumn kc = (KeyColumn)pksIt.next();
                pkColumns.add(kc.getColumnName());
            }
            fkImpl.setColumnNames(fkColumns, pkColumns);
            if(ffTable instanceof SourceTableImpl) {
                ((SourceTableImpl)ffTable).addForeignKey(fkImpl);
            } else {
                ((TargetTableImpl)ffTable).addForeignKey(fkImpl);
            }
        }
        SQLDBColumn ffColumn = null;
        for (int j = 0; j < cols.length; j++) {
            tc = cols[j];
            if(isSource) {
                ffColumn = new SourceColumnImpl(tc.getName(), tc
                        .getSqlTypeCode(), tc.getNumericScale(), tc
                        .getNumericPrecision(), tc
                        .getIsPrimaryKey(), tc.getIsForeignKey(),
                        false /* isIndexed */, tc.getIsNullable());
            } else {
                ffColumn = new TargetColumnImpl(tc.getName(), tc
                        .getSqlTypeCode(), tc.getNumericScale(), tc
                        .getNumericPrecision(), tc
                        .getIsPrimaryKey(), tc.getIsForeignKey(),
                        false /* isIndexed */, tc.getIsNullable());
            }
            ((SQLDBTable)ffTable).addColumn(ffColumn);
        }
        return ffTable;
    }
    
    private Object createTable(String[][] tableList, String tableName, boolean isSource) {
        String[] currTable = null;
        Object dbTable = null;
        if (tableList != null) {
            for(int i = 0; i < tableList.length; i ++) {
                currTable = tableList[i];
                if(currTable[DBMetaData.NAME].equals(tableName)) {
                    if(isSource) {
                        dbTable = new SourceTableImpl(currTable[DBMetaData.NAME],
                                currTable[DBMetaData.SCHEMA], currTable[DBMetaData.CATALOG]);
                    } else {
                        dbTable = new TargetTableImpl(currTable[DBMetaData.NAME],
                                currTable[DBMetaData.SCHEMA], currTable[DBMetaData.CATALOG]);
                    }
                    break;
                }
            }
        }
        return dbTable;
    }
    
    class LinkInfo {
        private SQLCanvasObject sObj;
        private SQLConnectableObject eObj;
        private String sParam;
        private String dParam;
        
        LinkInfo(SQLCanvasObject srcObj, SQLConnectableObject expObj, String srcParam, String destParam) {
            this.sObj = srcObj;
            this.eObj = expObj;
            this.sParam = srcParam;
            this.dParam = destParam;
        }
        
        public SQLCanvasObject getSource() {
            return this.sObj;
        }
        
        public SQLConnectableObject getTarget() {
            return this.eObj;
        }
        
        public String getSourceParam() {
            return this.sParam;
        }
        
        public String getTargetParam() {
            return this.dParam;
        }
    }
}

