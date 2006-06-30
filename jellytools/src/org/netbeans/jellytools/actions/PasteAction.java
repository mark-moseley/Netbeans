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
package org.netbeans.jellytools.actions;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.Bundle;

/** Used to call "Paste" popup menu item, "Edit|Paste" main menu item,
 * "org.openide.actions.PasteAction" or Ctrl+V shortcut.
 * @see Action
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class PasteAction extends Action {

    private static final String pastePopup = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Paste");
    private static final String pasteMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Edit")
                                            + "|" + pastePopup;
    private static final Shortcut pasteShortcut = new Shortcut(KeyEvent.VK_V, KeyEvent.CTRL_MASK);

    /** creates new PasteAction instance */    
    public PasteAction() {
        super(pasteMenu, pastePopup, "org.openide.actions.PasteAction", pasteShortcut);
    }
}