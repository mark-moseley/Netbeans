/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package jemmyI18NWizard.wizardSupport;

import org.netbeans.test.oo.gui.jam.JamButton;
import org.netbeans.test.oo.gui.jam.JamList;
import org.netbeans.test.oo.gui.jello.JelloWizard;
import org.netbeans.test.oo.gui.jello.JelloBundle;
import org.netbeans.test.oo.gui.jello.JelloUtilities;
import org.netbeans.test.oo.gui.jelly.MainFrame;

public class Page0 extends JelloWizard {
    protected JamButton addSourceButton;
    protected JamButton removeSourceButton;
    protected JamList sourceList;
    
    private static final String wizardBundle = "org.netbeans.modules.i18n.wizard.Bundle";
    private static final String i18nBundle = "org.netbeans.modules.i18n.Bundle";

    
    /* extracted from bundle */
    private static final String addSourceButtonLabel = JelloBundle.getString(wizardBundle, "CTL_AddSource");
    private static final String removeSourceButtonLabel = JelloBundle.getString(wizardBundle, "CTL_RemoveSource");
    
    
    public Page0() {
        super(JelloUtilities.getForteFrame(), JelloBundle.getString(wizardBundle, "LBL_WizardTitle"));
        sourceList = this.getJamList(1);
        removeSourceButton = this.getJamButton(removeSourceButtonLabel);
        addSourceButton = this.getJamButton(addSourceButtonLabel);
    }
    
    public void init() {
        String wizardSubmenuItem = JelloBundle.getString(wizardBundle, "LBL_WizardActionName");
        int position = wizardSubmenuItem.indexOf('&');
        if(position != -1) {
            StringBuffer sb = new StringBuffer(wizardSubmenuItem);
            sb.deleteCharAt(position);
            wizardSubmenuItem = sb.toString();
        }
        MainFrame.getMainFrame().pushToolsMenuNoBlock(JelloBundle.getString(i18nBundle, "LBL_I18nGroupActionName") + "|" + wizardSubmenuItem);
    }
    
    public void addSource() {
        addSourceButton.doClickNoBlock();
    }
    
    public void removeSource() {
        removeSourceButton.doClick();
    }
    
    public int getItemCount() {
        return sourceList.getSize();
    }
    
    
    public boolean selectItem(int index) {
        return sourceList.selectItem(index);
    }
    
    public boolean selectItem(String item) {
        return sourceList.selectItem(item);
    }
    
    public String getSelectedItem() {
        return sourceList.getSelectedItem();
    }
    
    /** Dummy here. */
    protected void updatePanel(int panelIndex) {
    }
    
}


