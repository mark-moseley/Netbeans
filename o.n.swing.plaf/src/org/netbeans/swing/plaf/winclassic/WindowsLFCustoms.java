/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.swing.plaf.winclassic;

import org.netbeans.swing.plaf.LFCustoms;
import org.netbeans.swing.plaf.util.GuaranteedValue;
import org.netbeans.swing.plaf.util.UIBootstrapValue;
import org.netbeans.swing.plaf.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


/** Default system-provided customizer for Windows LF
 * Public only to be accessible by ProxyLazyValue, please don't abuse.
 */
public final class WindowsLFCustoms extends LFCustoms {

    public Object[] createLookAndFeelCustomizationKeysAndValues() {
        int fontsize = 11;
        Integer in = (Integer) UIManager.get(CUSTOM_FONT_SIZE); //NOI18N
        if (in != null) {
            fontsize = in.intValue();
        }

        Object[] result = new Object[] {
            //Workaround for help window selection color
            "EditorPane.selectionBackground", new Color (157, 157, 255), //NOI18N

            //Work around a bug in windows which sets the text area font to
            //"MonoSpaced", causing all accessible dialogs to have monospaced text
            "TextArea.font", new GuaranteedValue ("Label.font", new Font("Dialog", Font.PLAIN, fontsize)), //NOI18N
        };
        return result;
    }

    public Object[] createApplicationSpecificKeysAndValues () {
        Object propertySheetColorings = new WinClassicPropertySheetColorings();
        Object[] result = {
            DESKTOP_BORDER, new EmptyBorder(4, 2, 1, 2),
            SCROLLPANE_BORDER, UIManager.get("ScrollPane.border"),
            EXPLORER_STATUS_BORDER, new StatusLineBorder(StatusLineBorder.TOP),
            EXPLORER_FOLDER_ICON , UIUtils.loadImage("org/netbeans/swing/plaf/resources/win-explorer-folder.gif"),
            EXPLORER_FOLDER_OPENED_ICON, UIUtils.loadImage("org/netbeans/swing/plaf/resources/win-explorer-opened-folder.gif"),
            EDITOR_STATUS_LEFT_BORDER, new StatusLineBorder(StatusLineBorder.TOP | StatusLineBorder.RIGHT),
            EDITOR_STATUS_RIGHT_BORDER, new StatusLineBorder(StatusLineBorder.TOP | StatusLineBorder.LEFT),
            EDITOR_STATUS_INNER_BORDER, new StatusLineBorder(StatusLineBorder.TOP | StatusLineBorder.LEFT | StatusLineBorder.RIGHT),
            EDITOR_TOOLBAR_BORDER, new EditorToolbarBorder(),
            EDITOR_STATUS_ONLYONEBORDER, new StatusLineBorder(StatusLineBorder.TOP),

            PROPERTYSHEET_BOOTSTRAP, propertySheetColorings,

            EDITOR_TAB_CONTENT_BORDER, new WinClassicCompBorder(),
            EDITOR_TAB_TABS_BORDER, new WinClassicTabBorder(),
            VIEW_TAB_CONTENT_BORDER, new WinClassicCompBorder(),
            VIEW_TAB_TABS_BORDER, new WinClassicTabBorder(),

            DESKTOP_SPLITPANE_BORDER, BorderFactory.createEmptyBorder(4, 2, 1, 2),

            //UI Delegates for the tab control
            EDITOR_TAB_DISPLAYER_UI, "org.netbeans.swing.tabcontrol.plaf.WinClassicEditorTabDisplayerUI",
            SLIDING_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.WindowsSlidingButtonUI",
            VIEW_TAB_DISPLAYER_UI, "org.netbeans.swing.tabcontrol.plaf.WinClassicTabDisplayerUI",
        }; //NOI18N

        return result;
    }

    private class WinClassicPropertySheetColorings extends UIBootstrapValue.Lazy {
        public WinClassicPropertySheetColorings () {
            super (null);
        }

        public Object[] createKeysAndValues() {
            return new Object[] {
            //Property sheet settings as defined by HIE
            PROPSHEET_SELECTION_BACKGROUND, new Color(10,36,106),
            PROPSHEET_SELECTION_FOREGROUND, Color.WHITE,
            PROPSHEET_SET_BACKGROUND, new Color(237,233,225),
            PROPSHEET_SET_FOREGROUND, Color.BLACK,
            PROPSHEET_SELECTED_SET_BACKGROUND, new Color(10,36,106),
            PROPSHEET_SELECTED_SET_FOREGROUND, Color.WHITE,
            PROPSHEET_DISABLED_FOREGROUND, new Color(128,128,128),
            PROPSHEET_BUTTON_COLOR, UIManager.getColor("control"),
            };
        }
    }
}
