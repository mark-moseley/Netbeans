package org.netbeans.modules.sql.framework.ui.graph.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.etl.ui.view.ETLCollaborationTopComponent;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


public final class PrintAction extends AbstractAction{ 
    
    private static final String LOG_CATEGORY = PrintAction.class.getName();
    
    public String getName() {
        return NbBundle.getMessage(PrintAction.class, "CTL_Print");
    }
    
    protected String iconResource() {
        return "org/netbeans/modules/sql/framework/ui/resources/images/print.gif";
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }

    public void actionPerformed(ActionEvent e) {
        ETLCollaborationTopComponent topComp = null;
        try {
            topComp = DataObjectProvider.getProvider().getActiveDataObject().getETLEditorTC();
        } catch (Exception ex) {
            // ignore
        }

        IGraphView graphView = topComp.getGraphView();
        graphView.printView();        
    }    
}