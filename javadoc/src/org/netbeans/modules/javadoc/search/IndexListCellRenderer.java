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

package org.netbeans.modules.javadoc.search;

import java.awt.Component;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.DefaultListCellRenderer;

/** Just sets the right icon to IndexItem

 @author Petr Hrebejk
*/
class IndexListCellRenderer extends DefaultListCellRenderer {

    static final long serialVersionUID =543071118545614229L;
    public Component getListCellRendererComponent( JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        JLabel cr = (JLabel)super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );

        cr.setIcon( DocSearchIcons.getIcon( ((DocIndexItem)value).getIconIndex() ) );

        try {
            if (  ((DocIndexItem)value).getURL() == null )
                setForeground (java.awt.SystemColor.textInactiveText);
        }
        catch ( java.net.MalformedURLException e ) {
            setForeground (java.awt.SystemColor.textInactiveText);
        }
        return cr;
    }
}
