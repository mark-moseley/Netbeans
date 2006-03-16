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

package org.apache.tools.ant.module;

import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;

/**
 * Contains information about Abbreviations Panel, and creates a new 
 * instance of it.
 *
 * @author Jan Jancura
 */
public final class AntOption extends AdvancedOption {

    private static String loc (String key) {
        return NbBundle.getMessage (AntOption.class, key);
    }

    
    @Override
    public String getDisplayName () {
        return loc ("Ant");
    }

    @Override
    public String getTooltip () {
        return loc ("Ant_Tooltip");
    }

    @Override
    public OptionsPanelController create () {
        return new AntPanelController ();
    }
}
