/*
 * ExportEjbDataSource.java
 *
 * Created on August 30, 2004, 3:38 PM
 */

package org.netbeans.modules.visualweb.ejb.actions;

import java.util.ArrayList;
import java.util.Collection;

import org.netbeans.modules.visualweb.ejb.nodes.EjbGroupNode;
import org.netbeans.modules.visualweb.ejb.ui.ExportEjbDataSourceDialog;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * To export EJB datasources
 *
 * @author  cao
 */
public class ExportEjbDataSourceAction extends NodeAction {
    
    /** Creates a new instance of ExportEjbDataSource */
    public ExportEjbDataSourceAction() {
    }
    
    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        return true;
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        // TODO help needed
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return NbBundle.getMessage( ExportEjbDataSourceAction.class, "EXPORT_EJB_DATASOURCE" );
    }
    
    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        
        Collection grpNames = new ArrayList();
        for( int i = 0; i < activatedNodes.length; i ++ )
        {
            Node node;
            if (activatedNodes[i] instanceof FilterNode) {
                node = (Node) activatedNodes[i].getCookie(EjbGroupNode.class);
            } else {
                node = activatedNodes[i];
            }

            grpNames.add(((EjbGroupNode) node).getEjbGroup().getName());
        }
        
        ExportEjbDataSourceDialog exportDialog = new ExportEjbDataSourceDialog( grpNames );
        exportDialog.showDialog();
    }
    
    /** @return <code>false</code> to be performed in event dispatch thread */
    protected boolean asynchronous() {
        return false;
    }
}
