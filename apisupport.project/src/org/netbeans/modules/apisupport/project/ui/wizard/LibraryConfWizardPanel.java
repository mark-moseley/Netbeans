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

package org.netbeans.modules.apisupport.project.ui.wizard;

import java.awt.Component;
import org.netbeans.modules.apisupport.project.Util;
import org.openide.util.HelpCtx;

/**
 * Third panel of Library Wrapper wizard.
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
final class LibraryConfWizardPanel extends BasicWizardPanel.NewTemplatePanel {

    /** Representing visual component for this step. */
    private BasicConfVisualPanel visualPanel;
    
    LibraryConfWizardPanel(final NewModuleProjectData data) {
        super(data);
    }
    
    void reloadData() {
        if (getData().getCodeNameBase() == null) {
            String dotName = BasicConfVisualPanel.EXAMPLE_BASE_NAME + getData().getProjectName();
            getData().setCodeNameBase(Util.normalizeCNB(dotName));
        }
        if (getData().getProjectDisplayName() == null) {
            getData().setProjectDisplayName(getData().getProjectName());
        }
        visualPanel.refreshData();
    }
    
    void storeData() {
        visualPanel.storeData();
    }
    
    
    public Component getComponent() {
        if (visualPanel == null) {
            visualPanel = new BasicConfVisualPanel(getData());
            visualPanel.addPropertyChangeListener(this);
            visualPanel.setName(getMessage("LBL_BasicConfigPanel_Title")); // NOI18N
        }
        return visualPanel;
    }
    
    public @Override HelpCtx getHelp() {
        return new HelpCtx(LibraryConfWizardPanel.class);
    }
    
    
    
}
