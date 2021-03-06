/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.xml.schema.actions;

import java.awt.Dialog;
import org.netbeans.modules.xml.schema.SchemaDataObject;
import org.netbeans.modules.xml.schema.ui.basic.SchemaModelCookie;
import org.netbeans.modules.xml.schema.wizard.SampleXMLGeneratorWizardIterator;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

public final class GenerateXMLAction extends CookieAction {
    
    private static final Class[] COOKIE_ARRAY =
            new Class[] {SchemaModelCookie.class};

    protected void performAction(Node[] activatedNodes) {
         assert activatedNodes.length==1:
            "Length of nodes array should be 1";
        
         if(activatedNodes[0] == null)
             return;
         
         SchemaDataObject sdo = activatedNodes[0].getCookie(SchemaDataObject.class);
         if(sdo == null)
             return;
         SampleXMLGeneratorWizardIterator wizard = new SampleXMLGeneratorWizardIterator(sdo);
         wizard.show();
               
    }

    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    public String getName() {
        return NbBundle.getMessage(GenerateXMLAction.class, "CTL_GenerateXMLAction");
    }

    protected Class[] cookieClasses() {
        return COOKIE_ARRAY;
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() Javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}

