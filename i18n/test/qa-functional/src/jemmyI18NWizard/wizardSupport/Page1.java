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

import org.netbeans.test.oo.gui.jello.JelloWizard;
import org.netbeans.test.oo.gui.jello.JelloBundle;
import org.netbeans.test.oo.gui.jello.JelloUtilities;
import org.netbeans.test.oo.gui.jam.JamButton;
import org.netbeans.test.oo.gui.jam.Jemmy;
import org.netbeans.jemmy.operators.JTableOperator;

public class Page1 extends JelloWizard {
    protected JamButton selectAllButton;
    protected JamButton selectResourceButton;
    protected JTableOperator sourceTable;
    
    private static final String wizardBundle = "org.netbeans.modules.i18n.wizard.Bundle";
    
    private static final String selectAllButtonLabel = JelloBundle.getString(wizardBundle, "CTL_SelectResourceAll");
    private static final String selectResourceButtonLabel = JelloBundle.getString(wizardBundle, "CTL_SelectResource");
    
    
    public Page1() {
        super(JelloUtilities.getForteFrame(), JelloBundle.getString(wizardBundle, "LBL_WizardTitle"));
        selectAllButton = this.getJamButton(selectAllButtonLabel);
        selectResourceButton = this.getJamButton(selectResourceButtonLabel);
        sourceTable = new JTableOperator(Jemmy.getOp(this));
    }
    
    public void selectAll() {
        selectAllButton.doClickNoBlock();
    }
    
    public void selectResource() {
        selectResourceButton.doClickNoBlock();
    }
    
    public void selectRow(int row) {
        sourceTable.addRowSelectionInterval(row,row);
    }
    
    public void clearSelection() {
        sourceTable.clearSelection();
    }
    
    /** Dummy here. */
    protected void updatePanel(int panelIndex) {
    }
    
}


