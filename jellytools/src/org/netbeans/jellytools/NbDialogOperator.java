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

package org.netbeans.jellytools;

import javax.swing.JDialog;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 * Handle generic NetBeans dialog. The dialog can include Yes, No, OK, 
 * Cancel, Close or Help buttons. The dialog is identified by its title.
 */
public class NbDialogOperator extends JDialogOperator {
    
    private JButtonOperator _btYes;
    private JButtonOperator _btNo;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;
    private JButtonOperator _btClose;
    private JButtonOperator _btHelp;
    
    
    /** Waits until dialog with requested title is found. Title is compared
     * on partial match and case non-sensitive. If dialog is not found, runtime
     * exception is thrown.
     * @param title  title of window
     */
    public NbDialogOperator(String title) {
        super(title);
    }
    
    /** Created NbDialogOperator with given dialog
     * @param dialog JDialog instance */
    public NbDialogOperator(JDialog dialog) {
        super(dialog);
    }
    
    /** Returns operator of "Yes" button.
     * @return  JButtonOperator instance of "Yes" button
     */
    public JButtonOperator btYes() {
        if (_btYes == null) {
            String yesCaption = Bundle.getString("org.netbeans.core.Bundle", "YES_OPTION_CAPTION");
            _btYes = new JButtonOperator(this, yesCaption);
        }
        return _btYes;
    }

    /** Returns operator of "No" button.
     * @return  JButtonOperator instance of "No" button
     */
    public JButtonOperator btNo() {
        if (_btNo == null) {
            String noCaption = Bundle.getString("org.netbeans.core.Bundle", "NO_OPTION_CAPTION");
            _btNo = new JButtonOperator(this, noCaption);
        }
        return _btNo;
    }

    /** Returns operator of "OK" button.
     * @return  JButtonOperator instance of "OK" button
     */
    public JButtonOperator btOK() {
        if (_btOK == null) {
            String oKCaption = Bundle.getString("org.netbeans.core.Bundle", "OK_OPTION_CAPTION");
            _btOK = new JButtonOperator(this, oKCaption);
        }
        return _btOK;
    }

    /** Returns operator of "Cancel" button.
     * @return  JButtonOperator instance of "Cancel" button
     */
    public JButtonOperator btCancel() {
        if (_btCancel == null) {
            String cancelCaption = Bundle.getString("org.netbeans.core.Bundle", "CANCEL_OPTION_CAPTION");
            _btCancel = new JButtonOperator(this, cancelCaption);
        }
        return _btCancel;
    }

    /** Returns operator of "Close" button.
     * @return  JButtonOperator instance of "Close" button
     */
    public JButtonOperator btClose() {
        if (_btClose == null) {
            String closeCaption = Bundle.getString("org.netbeans.core.Bundle", "CLOSED_OPTION_CAPTION");
            _btClose = new JButtonOperator(this, closeCaption);
        }
        return _btClose;
    }
    
    /** Returns operator of "Help" button.
     * @return  JButtonOperator instance of "Help" button
     */
    public JButtonOperator btHelp() {
        if (_btHelp == null) {
            String helpCaption = Bundle.getString("org.netbeans.core.Bundle", "HELP_OPTION_CAPTION");
            _btHelp = new JButtonOperator(this, helpCaption);
        }
        return _btHelp;
    }
    
    /** Pushes "Yes" button. */
    public void yes() {
        btYes().push();
    }
    
    /** Pushes "No" button. */
    public void no() {
        btNo().push();
    }
    
    /** Pushes "OK" button. */
    public void ok() {
        btOK().push();
    }
    
    /** Pushes "Cancel" button. */
    public void cancel() {
        btCancel().push();
    }

    /** Pushes "Close" button. Using {@link JDialogOperator#close() close()}
     * will close dialog default way like on demand of a window manager.
     */
    public void closeByButton() {
        btClose().push();
    }
    
    /** Pushes "Help" button. */
    public void help() {
        btHelp().push();
    }
}
