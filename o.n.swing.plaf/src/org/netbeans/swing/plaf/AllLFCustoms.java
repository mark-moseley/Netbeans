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

package org.netbeans.swing.plaf;

import org.netbeans.swing.plaf.util.GuaranteedValue;
import org.netbeans.swing.plaf.util.RelativeColor;
import org.netbeans.swing.plaf.util.UIUtils;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import java.awt.*;

/** Customization for all LFs. */
final class AllLFCustoms extends LFCustoms {

    public Object[] createApplicationSpecificKeysAndValues () {
        ColorUIResource errorColor = new ColorUIResource(89, 79, 191);
        
        Object[] uiDefaults = {

            ERROR_FOREGROUND, errorColor,
            ERROR_FOREGROUND2, errorColor,


            //Tab control in case of unknown look and feel
            TAB_ACTIVE_SELECTION_BACKGROUND,
                new GuaranteedValue (new String[] {"Table.selectionBackground",
                "info"}, Color.BLUE.brighter()),

            TAB_ACTIVE_SELECTION_FOREGROUND,
                new GuaranteedValue ("Table.selectionForeground",
                Color.WHITE),

            TAB_SELECTION_FOREGROUND,
                new GuaranteedValue("textText", Color.BLACK),

            //Likely to be the same for all look and feels - doesn't do anything
            //exciting
            EDITOR_TABBED_CONTAINER_UI,
                "org.netbeans.swing.tabcontrol.plaf.DefaultTabbedContainerUI",

            SLIDING_TAB_DISPLAYER_UI,
                "org.netbeans.swing.tabcontrol.plaf.BasicSlidingTabDisplayerUI",
            
            SLIDING_TAB_BUTTON_UI,
                "org.netbeans.swing.tabcontrol.plaf.SlidingTabDisplayerButtonUI",

            SLIDING_BUTTON_UI, "org.netbeans.swing.tabcontrol.SlidingButtonUI", //NOI18N
                
        
            //XXX convert to derived colors
            "tab_unsel_fill", UIUtils.adjustColor (
                new GuaranteedValue("InternalFrame.inactiveTitleGradient",
                    Color.GRAY).getColor(),
                -12, -15, -22),

            "tab_sel_fill", new GuaranteedValue("text", Color.WHITE),

            "tab_bottom_border", UIUtils.adjustColor (
                new GuaranteedValue("InternalFrame.borderShadow",
                    Color.GRAY).getColor(),
                20, 17, 12),


             "winclassic_tab_sel_gradient",
                new RelativeColor (
                    new Color(7, 28, 95),
                    new Color(152, 177, 208),
                    "InternalFrame.activeTitleBackground"),

            SCROLLPANE_BORDER_COLOR, new Color(127, 157, 185),


        }; //NOI18N
        return uiDefaults;
    }

    public Object[] createGuaranteedKeysAndValues () {
        int fontsize = 11;
        Integer in = (Integer) UIManager.get(CUSTOM_FONT_SIZE); //NOI18N
        boolean hasCustomFontSize = in != null;
        if (hasCustomFontSize) {
            fontsize = in.intValue();
        }
        Object[] uiDefaults = {
            //XXX once jdk 1.5 b2 is out, these can be deleted
            
            "control", new GuaranteedValue ("control", Color.LIGHT_GRAY),
            "controlShadow", new GuaranteedValue ("controlShadow", Color.GRAY),
            "controlDkShadow", new GuaranteedValue ("controlDkShadow", Color.DARK_GRAY),
            "textText", new GuaranteedValue ("textText", Color.BLACK),
            "controlFont", new GuaranteedValue ("controlFont",
                new Font ("Dialog", Font.PLAIN, fontsize)),
            
            DEFAULT_FONT_SIZE, new Integer(11),
            //Used by tab popup
            "ComboBox.background",
               new GuaranteedValue ("ComboBox.background", Color.WHITE),

            "ComboBox.foreground",
               new GuaranteedValue ("ComboBox.foreground", Color.BLACK),

            "ComboBox.selectionBackground",
               new GuaranteedValue ("ComboBox.selectionBackground",
               Color.BLUE),

            "ComboBox.selectionForeground",
               new GuaranteedValue ("ComboBox.selectionForeground",
               Color.WHITE),

            "ComboBox.font",
               new GuaranteedValue ("ComboBox.font",
               new Font ("Dialog", Font.PLAIN, fontsize)),

             "InternalFrame.activeTitleBackground",
                new GuaranteedValue("InternalFrame.activeTitleBackground",
                Color.BLUE),
            //Colors below are used by windows UI which is used for unknown
            //look and feels
            "InternalFrame.borderShadow",
                new GuaranteedValue("InternalFrame.borderShadow", Color.gray),

            "InternalFrame.borderHighlight",
                new GuaranteedValue("InternalFrame.borderHighlight",
                Color.white),

            "InternalFrame.borderDarkShadow",
                new GuaranteedValue("InternalFrame.borderDarkShadow",
                Color.darkGray),

            "InternalFrame.borderLight",
                new GuaranteedValue("InternalFrame.borderLight",
                Color.lightGray),

            "TabbedPane.background",
                new GuaranteedValue("TabbedPane.background", Color.LIGHT_GRAY),

            "TabbedPane.focus",
                new GuaranteedValue("TabbedPane.focus", Color.GRAY),

            "TabbedPane.highlight",
                new GuaranteedValue("TabbedPane.highlight", Color.WHITE),

        };
        return uiDefaults;
    }

    public static void initCustomFontSize (int uiFontSize) {
        Font nbDialogPlain = new FontUIResource("Dialog", Font.PLAIN, uiFontSize); // NOI18N
        Font nbDialogBold = new FontUIResource("Dialog", Font.BOLD, uiFontSize); // NOI18N
        Font nbSerifPlain = new FontUIResource("Serif", Font.PLAIN, uiFontSize); // NOI18N
        Font nbSansSerifPlain = new FontUIResource("SansSerif", Font.PLAIN, uiFontSize); // NOI18N
        Font nbMonospacedPlain = new FontUIResource("Monospaced", Font.PLAIN, uiFontSize); // NOI18N
        UIManager.put("controlFont", nbDialogPlain); // NOI18N
        UIManager.put("Button.font", nbDialogPlain); // NOI18N
        UIManager.put("ToggleButton.font", nbDialogPlain); // NOI18N
        UIManager.put("RadioButton.font", nbDialogPlain); // NOI18N
        UIManager.put("CheckBox.font", nbDialogPlain); // NOI18N
        UIManager.put("ColorChooser.font", nbDialogPlain); // NOI18N
        UIManager.put("ComboBox.font", nbDialogPlain); // NOI18N
        UIManager.put("Label.font", nbDialogPlain); // NOI18N
        UIManager.put("List.font", nbDialogPlain); // NOI18N
        UIManager.put("MenuBar.font", nbDialogPlain); // NOI18N
        UIManager.put("MenuItem.font", nbDialogPlain); // NOI18N
        UIManager.put("MenuItem.acceleratorFont", nbDialogPlain); // NOI18N
        UIManager.put("RadioButtonMenuItem.font", nbDialogPlain); // NOI18N
        UIManager.put("CheckBoxMenuItem.font", nbDialogPlain); // NOI18N
        UIManager.put("Menu.font", nbDialogPlain); // NOI18N
        UIManager.put("PopupMenu.font", nbDialogPlain); // NOI18N
        UIManager.put("OptionPane.font", nbDialogPlain); // NOI18N
        UIManager.put("Panel.font", nbDialogPlain); // NOI18N
        UIManager.put("ProgressBar.font", nbDialogPlain); // NOI18N
        UIManager.put("ScrollPane.font", nbDialogPlain); // NOI18N
        UIManager.put("Viewport.font", nbDialogPlain); // NOI18N
        UIManager.put("TabbedPane.font", nbDialogPlain); // NOI18N
        UIManager.put("Table.font", nbDialogPlain); // NOI18N
        UIManager.put("TableHeader.font", nbDialogPlain); // NOI18N
        UIManager.put("TextField.font", nbSansSerifPlain); // NOI18N
        UIManager.put("PasswordField.font", nbMonospacedPlain); // NOI18N
        UIManager.put("TextArea.font", nbDialogPlain); // NOI18N
        UIManager.put("TextPane.font", nbDialogPlain); // NOI18N
        UIManager.put("EditorPane.font", nbSerifPlain); // NOI18N
        UIManager.put("TitledBorder.font", nbDialogPlain); // NOI18N
        UIManager.put("ToolBar.font", nbDialogPlain); // NOI18N
        UIManager.put("ToolTip.font", nbSansSerifPlain); // NOI18N
        UIManager.put("Tree.font", nbDialogPlain); // NOI18N
        UIManager.put("InternalFrame.titleFont", nbDialogBold); // NOI18N
        UIManager.put("windowTitleFont", nbDialogBold); // NOI18N
    }


}
