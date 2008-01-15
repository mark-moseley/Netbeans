package org.netbeans.modules.etl.ui.view.graph.actions;

import java.awt.event.ActionEvent;
import java.net.URL;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.axiondb.ExternalConnectionProvider;
import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.etl.ui.view.ETLCollaborationTopPanel;
import org.netbeans.modules.sql.framework.ui.graph.actions.GraphAction;
import org.netbeans.modules.sql.framework.ui.utils.AxionExternalConnectionProvider;
import org.openide.util.NbBundle;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.logger.LogUtil;


public final class TestRunAction extends GraphAction {
    
    private static final URL runIconUrl = TestRunAction.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/runCollaboration.png");    
    private static transient final Logger mLogger = LogUtil.getLogger(TestRunAction.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    
    public TestRunAction() {
        //action name
        this.putValue(Action.NAME, NbBundle.getMessage(TestRunAction.class, "ACTION_TESTRUN"));
        
        //action icon
        this.putValue(Action.SMALL_ICON, new ImageIcon(runIconUrl));
        
        //action tooltip
        this.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(EditDbModelAction.class, "ACTION_TESTRUN_TOOLTIP"));
        
    }
    
    public void actionPerformed(ActionEvent ev) {
        ETLCollaborationTopPanel topComp = null;
        try {
            topComp = DataObjectProvider.getProvider().getActiveDataObject().getETLEditorTopPanel();
            Thread.currentThread().getContextClassLoader().loadClass(AxionExternalConnectionProvider.class.getName());
        } catch (Exception ex) {
             mLogger.errorNoloc(mLoc.t("PRSR026: Error loading class:{0}",TestRunAction.class.getName()),ex);
            //Logger.printThrowable(Logger.ERROR, TestRunAction.class.getName(), null, "Error loading class:", ex);
        }
        System.setProperty(ExternalConnectionProvider.EXTERNAL_CONNECTION_PROVIDER_PROPERTY_NAME, AxionExternalConnectionProvider.class.getName());
        
        if (topComp != null) {
            topComp.run();
        }
    }
}