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
package org.netbeans.jellytools.modules.debugger.actions;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.Action;

/** Used to call "Run|Toggle Breakpoint" main menu item,
 * "Toggle Breakpoint" popup menu item or CTRL+F8 shortcut.
 * @see org.netbeans.jellytools.actions.Action
 * @author Jiri.Skrivanek@sun.com
 */
public class ToggleBreakpointAction extends Action {

    // "Toggle Breakpoint"
    private static String toggleBreakpointItem = Bundle.getStringTrimmed(
                                "org.netbeans.modules.debugger.ui.actions.Bundle",
                                "CTL_Toggle_breakpoint");
    // "Run"
    private static final String runItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject");
    // "Run|Toggle Breakpoint"
    private static final String mainMenuPath = runItem+"|"+toggleBreakpointItem;
    private static final KeyStroke keystroke = System.getProperty("os.name").toLowerCase().indexOf("mac") > -1 ?
            KeyStroke.getKeyStroke(KeyEvent.VK_F8, KeyEvent.META_MASK) :
            KeyStroke.getKeyStroke(KeyEvent.VK_F8, KeyEvent.CTRL_MASK);

    /** Creates new ToggleBreakpointAction instance. */
    public ToggleBreakpointAction() {
        super(mainMenuPath, toggleBreakpointItem, keystroke);
    }
}
