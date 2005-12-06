/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.options.colors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/**
 * Contains information about Font and Colors Panel, and creates a new 
 * instance of it.
 *
 * @author Jan Jancura
 */
public class FontAndColors extends OptionsCategory {

    
    private static String loc (String key) {
        return NbBundle.getMessage (FontAndColors.class, key);
    }


    private static Icon icon;
    
    public Icon getIcon () {
        if (icon == null)
            icon = new ImageIcon (
                Utilities.loadImage 
                    ("org/netbeans/modules/options/resources/colors.png")
            );
        return icon;
    }

    public String getCategoryName () {
        return loc ("CTL_Font_And_Color_Options");
    }

    public String getTitle () {
        return loc ("CTL_Font_And_Color_Options_Title");
    }
    
    public String getDescription () {
        return loc ("CTL_Font_And_Color_Options_Description");
    }

    public OptionsPanelController create () {
        return new FontAndColorsPanelController ();
    }
}
