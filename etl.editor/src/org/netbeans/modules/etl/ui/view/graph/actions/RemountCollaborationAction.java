/*
 *
 *          Copyright (c) 2003-2005, SeeBeyond Technology Corporation,
 *          All Rights Reserved
 *
 *          This program, and all the routines referenced herein,
 *          are the proprietary properties and trade secrets of
 *          SEEBEYOND TECHNOLOGY CORPORATION.
 *
 *          Except as provided for by license agreement, this
 *          program shall not be duplicated, used, or disclosed
 *          without  written consent signed by an officer of
 *          SEEBEYOND TECHNOLOGY CORPORATION.
 *
 */
package org.netbeans.modules.etl.ui.view.graph.actions;

import org.netbeans.modules.sql.framework.model.SQLDBTable;
import java.awt.event.ActionEvent;
import java.net.URL;
import javax.swing.Action;
import java.util.Iterator;
import javax.swing.ImageIcon;
import org.netbeans.modules.sql.framework.model.DBMetaDataFactory;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.model.CollabSQLUIModel;
import org.netbeans.modules.sql.framework.ui.graph.actions.GraphAction;
import org.openide.awt.StatusDisplayer;

/**
 * @author Nithya Radhakrishnan
 * @version $Revision$
 */
public class RemountCollaborationAction extends GraphAction {

    private static final URL remountImgUrl = RemountCollaborationAction.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/redo.png");

    public RemountCollaborationAction() {
        //action name
        this.putValue(Action.NAME, NbBundle.getMessage(RemountCollaborationAction.class, "ACTION_REMOUNT"));

        //action icon
        this.putValue(Action.SMALL_ICON, new ImageIcon(remountImgUrl));

        //action tooltip
        this.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(RemountCollaborationAction.class, "ACTION_REMOUNT_TOOLTIP"));
    }

    /**
     * called when this action is performed in the ui
     *
     * @param ev event
     */
    public void actionPerformed(ActionEvent ev) {
        IGraphView graphView = (IGraphView) ev.getSource();
        CollabSQLUIModel model = (CollabSQLUIModel) graphView.getGraphModel();
        Iterator targetTables = model.getSQLDefinition().getTargetTables().iterator();
        while (targetTables.hasNext()) {
            try {
                SQLDBTable table = (SQLDBTable) targetTables.next();
                SQLDBModel dbmodel = (SQLDBModel) table.getParent();
                if (dbmodel.getETLDBConnectionDefinition().getDBType().equals(DBMetaDataFactory.AXION) ||
                    dbmodel.getETLDBConnectionDefinition().getDBType().equalsIgnoreCase("Internal")) {
                    SQLObjectUtil.dropTable(table, (SQLDBModel) table.getParent());
                    SQLObjectUtil.createTable(table, (SQLDBModel) table.getParent());
                    SQLObjectUtil.setOrgProperties(table);
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                StatusDisplayer.getDefault().setStatusText("Unable to remount :" + ex.getMessage());
            }
        }

        Iterator sourceTables = model.getSQLDefinition().getSourceTables().iterator();
        while (sourceTables.hasNext()) {
            try {
                SQLDBTable table = (SQLDBTable) sourceTables.next();
                SQLDBModel dbmodel = (SQLDBModel) table.getParent();
                if (dbmodel.getETLDBConnectionDefinition().getDBType().equals(DBMetaDataFactory.AXION) ||
                    dbmodel.getETLDBConnectionDefinition().getDBType().equalsIgnoreCase("Internal")) {
                    SQLObjectUtil.dropTable(table, (SQLDBModel) table.getParent());
                    SQLObjectUtil.createTable(table, (SQLDBModel) table.getParent());
                    SQLObjectUtil.setOrgProperties(table);
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                StatusDisplayer.getDefault().setStatusText("Unable to remount :" + ex.getMessage());
            }
        }
    } // end of jOptionPane
}
