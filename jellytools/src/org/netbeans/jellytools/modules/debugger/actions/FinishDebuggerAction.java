/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.modules.debugger.actions;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.Action.Shortcut;
import org.netbeans.jemmy.EventTool;

/** Used to call "Run|Finish Debugger Session" main menu item or Shift+F5 shortcut.
 * @see org.netbeans.jellytools.actions.Action
 * @author Jiri.Skrivanek@sun.com
 */
public class FinishDebuggerAction extends Action {
    
    // "Run|Finish Debugger Session"
    private static final String mainMenuPath =
            Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject")+
            "|"+
            Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_KillAction_name");
    private static final Shortcut shortcut = new Shortcut(KeyEvent.VK_F5, KeyEvent.SHIFT_MASK);
    
    /** Creates new FinishDebuggerAction instance. */
    public FinishDebuggerAction() {
        super(mainMenuPath, null, null, shortcut);
    }
    
    /** Performs action through main menu. */
    public void performMenu() {
        // This is a workaround of issue 70731 (Main menu item not enabled when already shown)
        for (int i = 0; i < 10; i++) {
            if(MainWindowOperator.getDefault().menuBar().showMenuItem(mainMenuPath).isEnabled()) {
                break;
            }
            MainWindowOperator.getDefault().menuBar().closeSubmenus();
            new EventTool().waitNoEvent(300);
        }
        super.performMenu();
    }
}