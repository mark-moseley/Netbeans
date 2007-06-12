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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.i18n.wizard;

import java.awt.Dialog;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.netbeans.modules.i18n.I18nUtil;

import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;
import org.openide.DialogDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.FileOwnerQuery;

/**
 * Action which runs i18n wizard.
 *
 * @author  Peter Zavadsky
 * @author  Petr Kuzel
 */
public class I18nWizardAction extends NodeAction {

    public I18nWizardAction() {
        putValue("noIconInMenu", Boolean.TRUE);
    }
    
    /** Generated serial version UID. */
    static final long serialVersionUID = 6965968608028644524L;

    /** Weak reference to dialog. */
    private static WeakReference dialogWRef = new WeakReference(null);

    
    /** 
     * We create non-modal but not rentrant dialog. Wait until
     * previous one is closed.
     */
    protected boolean enable(Node[] activatedNodes) {

        if (Util.wizardEnabled(activatedNodes) == false) {
            return false;
        }
        
        Dialog previous = (Dialog) dialogWRef.get();
        if (previous == null) return true;
        return previous.isVisible() == false;
    }
    
    /** 
     * Popup non modal wizard.
     */
    protected void performAction(Node[] activatedNodes) {
        Dialog dialog = (Dialog) dialogWRef.get();
        
        if(dialog != null) {
            dialog.setVisible(false);
            dialog.dispose();
        }

	/* find out the current project from activated nodes */
	Project project = org.netbeans.modules.i18n.Util.getProjectFor(activatedNodes);
	if (project == null) return;
	  
        WizardDescriptor wizardDesc = I18nWizardDescriptor.createI18nWizardDescriptor(
            getWizardIterator(),
            new I18nWizardDescriptor.Settings(Util.createWizardSourceMap(activatedNodes), project)
        );

        initWizard(wizardDesc);
        
        dialog = DialogDisplayer.getDefault().createDialog(wizardDesc);
        dialogWRef = new WeakReference(dialog);
        dialog.setVisible(true);
    }

    /** Gets wizard iterator thru panels used in wizard invoked by this action, 
     * i.e I18N wizard. */
    private WizardDescriptor.Iterator getWizardIterator() {
        ArrayList panels = new ArrayList(4);
        
        panels.add(new SourceWizardPanel.Panel());
        panels.add(new ResourceWizardPanel.Panel());
        panels.add(new AdditionalWizardPanel.Panel());
        panels.add(new HardStringWizardPanel.Panel());
        
        return new WizardDescriptor.ArrayIterator((WizardDescriptor.Panel[])
            panels.toArray(new WizardDescriptor.Panel[panels.size()])
        );
    }

    /** Initializes wizard descriptor. */
    private void initWizard(WizardDescriptor wizardDesc) {
        // Init properties.
        wizardDesc.putProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);    // NOI18N
        wizardDesc.putProperty("WizardPanel_contentDisplayed", Boolean.TRUE);   // NOI18N
        wizardDesc.putProperty("WizardPanel_contentNumbered", Boolean.TRUE);    // NOI18N

        ArrayList contents = new ArrayList(4);
        contents.add(Util.getString("TXT_SelectSourcesHelp"));
        contents.add(Util.getString("TXT_SelectResourceHelp"));
        contents.add(Util.getString("TXT_AdditionalHelp"));
        contents.add(Util.getString("TXT_FoundStringsHelp"));
        
        wizardDesc.putProperty(
            "WizardPanel_contentData",                                          // NOI18N
            (String[])contents.toArray(new String[contents.size()])
        ); 
        
        wizardDesc.setTitle(Util.getString("LBL_WizardTitle"));
        wizardDesc.setTitleFormat(new MessageFormat("{0} ({1})"));              // NOI18N

        wizardDesc.setModal(false);
    }

    /** Gets localized name of action. Overrides superclass method. */
    public String getName() {
        return Util.getString("LBL_WizardActionName");
    }
    
    /** Gets the action's help context. Implemenst superclass abstract method. */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(I18nUtil.HELP_ID_WIZARD);
    }

    protected boolean asynchronous() {
      return false;
    }
    
}
