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

package org.netbeans.modules.options.indentation;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/**
 * Contains information about Indentation Panel, and creates a new
 * instance of it.
 *
 * @author Jan Jancura
 */
public final class Indentation extends OptionsCategory {

    private static String loc (String key) {
        return NbBundle.getMessage (Indentation.class, key);
    }
 

    private static Icon icon;
    
    public Icon getIcon () {
        if (icon == null)
            icon = new ImageIcon (
                Utilities.loadImage 
                    ("org/netbeans/modules/options/resources/indentation.png")
            );
        return icon;
    }
    
    public String getCategoryName () {
        return loc ("CTL_Indentation");
    }

    public String getTitle () {
        return loc ("CTL_Indentation_Title");
    }
    
    public String getDescription () {
        return loc ("CTL_Indentation_Description");
    }

    public OptionsPanelController create () {
        return new IndentationPanelController ();
    }
}
