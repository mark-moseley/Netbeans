/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard;

import java.awt.Component;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * First panel of <code>NewNbModuleWizardIterator</code>. Allow user to enter
 * basic module information:
 *
 * <ul>
 *  <li>Project name</li>
 *  <li>Project Location</li>
 *  <li>Project Folder</li>
 *  <li>If should be set as a Main Project</li>
 *  <li>NetBeans Platform (for standalone modules)</li>
 *  <li>Module Suite (for suite modules)</li>
 * </ul>
 *
 * @author Martin Krauskopf
 */
final class BasicInfoWizardPanel extends BasicWizardPanel {
    
    /** Representing visual component for this step. */
    private BasicInfoVisualPanel visualPanel;
    
    private int wizardType;
    
    /** Creates a new instance of BasicInfoWizardPanel */
    public BasicInfoWizardPanel(final WizardDescriptor settings, final int wizType) {
        super(settings);
        wizardType = wizType;
    }
    
    public void readSettings(Object settings) {
        visualPanel.refreshData();
    }
    
    public void storeSettings(Object settings) {
        visualPanel.storeData();
    }
    
    public Component getComponent() {
        if (visualPanel == null) {
            visualPanel = new BasicInfoVisualPanel(getSettings(), wizardType);
            visualPanel.addPropertyChangeListener(this);
            visualPanel.setName(getMessage("LBL_BasicInfoPanel_Title")); // NOI18N
            visualPanel.updateAndCheck();
        }
        return visualPanel;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(BasicInfoWizardPanel.class.getName() + "_" + getWizardTypeString(wizardType)); // NOI18N
    }
    
}
