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

package org.netbeans.swing.plaf.gtk;

import org.netbeans.swing.plaf.LFCustoms;
import org.netbeans.swing.plaf.metal.MetalLFCustoms;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import org.netbeans.swing.plaf.util.UIBootstrapValue;
import org.netbeans.swing.plaf.util.UIUtils;

/** UI customizations for GTK look and feel
 *
 * @author  Tim Boudreau  
 */
public class GtkLFCustoms extends LFCustoms {
    private Object light = new ThemeValue (ThemeValue.REGION_PANEL, ThemeValue.WHITE, Color.GRAY);
    private Object control = new ThemeValue (ThemeValue.REGION_PANEL, ThemeValue.MID, Color.GRAY);
    private Object controlFont = new ThemeValue (ThemeValue.REGION_TAB, new FontUIResource ("Dialog", Font.PLAIN, 11)); //NOI18N
    

    //Background colors for winsys tabs


    public Object[] createApplicationSpecificKeysAndValues () {
        Border lowerBorder = new AdaptiveMatteBorder (false, true, true, true, 3);
        //Avoid using ThemeValue if it can't work - mainly due to testing issues when trying to run GTK UI customizations
        //on the Mac, which doesn't have a GTKLookAndFeel

        Object selBg = ThemeValue.functioning() ? new ThemeValue (ThemeValue.REGION_BUTTON, ThemeValue.DARK, Color.CYAN) : (Object) Color.CYAN;
        Object selFg = ThemeValue.functioning() ? new ThemeValue (ThemeValue.REGION_BUTTON, ThemeValue.TEXT_FOREGROUND, Color.BLACK) : (Object) Color.BLACK;

        if (!ThemeValue.functioning()) {
            Integer i = (Integer) UIManager.get("customFontSize"); //NOI18N
            int sz = 11;
            if (i != null) {
                sz = i.intValue();
            }
            controlFont = new Font ("Dialog", Font.PLAIN, sz); //NOI18N
        }

        Object[] result = {
            PROPSHEET_SELECTION_BACKGROUND, selBg,
            PROPSHEET_SELECTION_FOREGROUND, selFg,
            PROPSHEET_SELECTED_SET_BACKGROUND, selBg,
            PROPSHEET_SELECTED_SET_FOREGROUND, selFg,
            PROPSHEET_BUTTON_COLOR, selFg,
            
            PROPSHEET_SET_BACKGROUND, ThemeValue.functioning() ? (Object) control : (Object) Color.CYAN,
            PROPSHEET_DISABLED_FOREGROUND, new Color(161,161,146),
            "Table.selectionBackground", selBg, //NOI18N
            "Table.selectionForeground", selFg, //NOI18N
            PROPSHEET_BACKGROUND, Color.WHITE,
            "window", light,
            
            VIEW_TAB_OUTER_BORDER, BorderFactory.createEmptyBorder(),
            VIEW_TAB_TABS_BORDER, new PartialEdgeBorder(4),
            VIEW_TAB_CONTENT_BORDER, lowerBorder,
            EDITOR_TAB_OUTER_BORDER, BorderFactory.createEmptyBorder(),
            EDITOR_TAB_CONTENT_BORDER, lowerBorder,
            EDITOR_TAB_TABS_BORDER, BorderFactory.createEmptyBorder(),
            
            EDITOR_STATUS_LEFT_BORDER, new InsetBorder (false, true),
            EDITOR_STATUS_RIGHT_BORDER, new InsetBorder (true, false),
            EDITOR_STATUS_ONLYONEBORDER, new InsetBorder (true, true),
            EDITOR_STATUS_INNER_BORDER, BorderFactory.createEmptyBorder(),
            
            
            OUTPUT_BACKGROUND, control,
            OUTPUT_HYPERLINK_FOREGROUND, selFg,
            OUTPUT_SELECTION_BACKGROUND, selBg,
            
            "controlFont", controlFont, //NOI18N

            //UI Delegates for the tab control
            EDITOR_TAB_DISPLAYER_UI, 
                "org.netbeans.swing.tabcontrol.plaf.WinClassicEditorTabDisplayerUI", //NOI18N
            VIEW_TAB_DISPLAYER_UI, 
                "org.netbeans.swing.tabcontrol.plaf.WinClassicViewTabDisplayerUI", //NOI18N
            SLIDING_TAB_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.SlidingTabDisplayerButtonUI", //NOI18N
            SLIDING_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.GtkSlidingButtonUI", //NOI18N


            DESKTOP_BACKGROUND, ThemeValue.functioning() ? new ThemeValue (ThemeValue.REGION_BUTTON, ThemeValue.LIGHT, Color.GRAY) : (Object) Color.GRAY,
            EXPLORER_MINISTATUSBAR_BORDER, BorderFactory.createEmptyBorder(),

            TOOLBAR_UI, "org.netbeans.swing.plaf.gtk.GtkToolbarUI", //NOI18N
            
        };
        return result;
    }
    
    public Object[] createLookAndFeelCustomizationKeysAndValues() {
        if (ThemeValue.functioning()) {
            return new Object[] {
                //XXX once the JDK team has integrated support for standard
                //UIManager keys into 1.5 (not there as of b47), these can 
                //probably be deleted, resulting in a performance improvement:
                "control", control,
                "controlHighlight", new ThemeValue (ThemeValue.REGION_PANEL, ThemeValue.LIGHT, Color.LIGHT_GRAY), //NOI18N
                "controlShadow", new ThemeValue (ThemeValue.REGION_PANEL, ThemeValue.DARK, Color.DARK_GRAY), //NOI18N
                "controlDkShadow", new ThemeValue (ThemeValue.REGION_PANEL, ThemeValue.BLACK, Color.BLACK), //NOI18N
                "controlLtHighlight", new ThemeValue (ThemeValue.REGION_PANEL, ThemeValue.WHITE, Color.WHITE), //NOI18N
                "textText", new ThemeValue (ThemeValue.REGION_PANEL, ThemeValue.TEXT_FOREGROUND, Color.BLACK), //NOI18N
                "text", new ThemeValue (ThemeValue.REGION_PANEL, ThemeValue.TEXT_BACKGROUND, Color.GRAY), //NOI18N
                
                "tab_unsel_fill", control,
                 
                "SplitPane.dividerSize", new Integer (2),
                
                SYSTEMFONT, controlFont, //NOI18N
                USERFONT, controlFont, //NOI18N
                MENUFONT, controlFont, //NOI18N
                LISTFONT, controlFont, //NOI18N
                "Label.font", controlFont, //NOI18N
                "Panel.font", controlFont //NOI18N
            };
        } else {
            Object[] result = new Object[] {
                TOOLBAR_UI, new UIDefaults.ProxyLazyValue("org.netbeans.swing.plaf.gtk.GtkToolbarUI"), //NOI18N
            };
            return result;
        }
    }
}
