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
package org.netbeans.jellytools.actions;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import org.netbeans.jellytools.Bundle;

/** Used to call "Debug" popup menu item on project's root node,
 * "Run|Debug Main Project" main menu item or Ctrl+F5 shortcut.
 * @see Action
 * @see org.netbeans.jellytools.nodes.ProjectRootNode
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class DebugProjectAction extends Action {

    // "Debug"
    private static final String debugProjectPopup = 
            Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle",
                                    "LBL_DebugProjectActionOnProject_Name");
    // "Run|Debug Main Project"
    private static final String debugProjectMenu = 
            Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject")+
            "|"+
            Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "LBL_DebugMainProjectAction_Name");
    private static final KeyStroke KEYSTROKE = System.getProperty("os.name").toLowerCase().indexOf("mac") > -1 ?
            KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.META_MASK) :
            KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.CTRL_MASK);
    
    /** creates new DebugProjectAction instance */    
    public DebugProjectAction() {
        super(debugProjectMenu, debugProjectPopup, KEYSTROKE);
    }
}
