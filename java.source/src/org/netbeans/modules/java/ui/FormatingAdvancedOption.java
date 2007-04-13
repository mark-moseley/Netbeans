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

package org.netbeans.modules.java.ui;

import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;

/**
 *
 * @author phrebejk
 */
public class FormatingAdvancedOption extends AdvancedOption {

    OptionsPanelController panelController;
    
    public String getDisplayName() {
        return NbBundle.getMessage(FormatingAdvancedOption.class, "CTL_Formating_DisplayName"); // NOI18N
    }

    public String getTooltip() {
        return NbBundle.getMessage(FormatingAdvancedOption.class, "CTL_Formating_ToolTip"); // NOI18N
    }

    public synchronized OptionsPanelController create() {
        
        if ( panelController == null ) {
            panelController = new FormatingOptionsPanelController();
        }
        
        return panelController;
    }

}
