/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.multiview.ui;

import org.openide.util.NbBundle;

/** RefreshSaveDialog.java
 *
 * Created on November 28, 2004, 7:18 PM
 * @author mkuchtiak
 */
public class RefreshSaveDialog extends org.openide.DialogDescriptor {
    public static final Integer OPTION_FIX=new Integer(0);
    public static final Integer OPTION_REFRESH=new Integer(1);
    public static final Integer OPTION_SAVE=new Integer(2);
    
    private static final String[] OPTIONS = new String[] {
        NbBundle.getMessage(RefreshSaveDialog.class,"OPT_FixNow"),
        NbBundle.getMessage(RefreshSaveDialog.class,"OPT_Refresh"),
        NbBundle.getMessage(RefreshSaveDialog.class,"OPT_Save")
    };

    /** Creates a new instance of RefreshSaveDialog */
    public RefreshSaveDialog(ErrorPanel errorPanel) {
        this (errorPanel, errorPanel.getErrorMessage());
    }
   
    /** Creates a new instance of RefreshSaveDialog */
    public RefreshSaveDialog(ErrorPanel errorPanel, String errorMessage ) {
        super (
            errorMessage,
            NbBundle.getMessage(RefreshSaveDialog.class,"TTL_warning"),
            true,
            OPTIONS,
            OPTIONS[0],
            BOTTOM_ALIGN,
            null,
            null
        );
        setButtonListener(new DialogListener(errorPanel));
        setClosingOptions(null);  
    }
    public Object getValue() {
        Object ret = super.getValue();
        if (ret.equals(OPTIONS[1])) return OPTION_REFRESH;
        else if (ret.equals(OPTIONS[2])) return OPTION_SAVE;
        else return OPTION_FIX;
    }
    
    private class DialogListener implements java.awt.event.ActionListener {
        private ErrorPanel errorPanel;
        DialogListener(ErrorPanel errorPanel) {
            this.errorPanel=errorPanel;
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource().equals(OPTIONS[1]) || evt.getSource().equals(OPTIONS[2])) {
                errorPanel.clearError();
            }
        }
    }
}
