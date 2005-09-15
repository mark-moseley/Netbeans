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
import org.netbeans.modules.apisupport.project.Util;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * Second panel of <code>NewNbModuleWizardIterator</code>. Allow user to enter
 * basic configuration:
 *
 * <ul>
 *  <li>Code Name Base</li>
 *  <li>Module Display Name</li>
 *  <li>Localizing Bundle</li>
 *  <li>XML Layer</li>
 * </ul>
 *
 * @author Martin Krauskopf
 */
final class LibraryConfWizardPanel extends BasicWizardPanel {
    
    /** Representing visual component for this step. */
    private BasicConfVisualPanel visualPanel;
    
    /** Creates a new instance of BasicConfWizardPanel */
    public LibraryConfWizardPanel(WizardDescriptor settings) {
        super(settings);
    }
    
    public void readSettings(Object settings) {
        WizardDescriptor wizSettings = (WizardDescriptor)settings;
        NewModuleProjectData data = (NewModuleProjectData)wizSettings.getProperty(
                NewModuleProjectData.DATA_PROPERTY_NAME);
                
        if (data.getCodeNameBase() == null) {
            String dotName = BasicConfVisualPanel.EXAMPLE_BASE_NAME + data.getProjectName();
            data.setCodeNameBase(Util.normalizeCNB(dotName));
        }
        if (data.getProjectDisplayName() == null) {
            data.setProjectDisplayName(data.getProjectName());
        }
        visualPanel.refreshData();
    }
    
    public void storeSettings(Object settings) {
        visualPanel.storeData();
    }
    
    public Component getComponent() {
        if (visualPanel == null) {
            visualPanel = new BasicConfVisualPanel(getSettings(), true);
            visualPanel.addPropertyChangeListener(this);
            visualPanel.setName(getMessage("LBL_BasicConfigPanel_Title")); // NOI18N
        }
        return visualPanel;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(LibraryConfWizardPanel.class);
    }
    
}
