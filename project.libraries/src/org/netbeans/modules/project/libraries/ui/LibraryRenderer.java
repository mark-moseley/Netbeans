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
package org.netbeans.modules.project.libraries.ui;

import org.netbeans.spi.project.libraries.LibraryImplementation;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

class LibraryRenderer extends DefaultListCellRenderer {

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        LibraryImplementation impl = (LibraryImplementation) value;
        return super.getListCellRendererComponent(list,
                LibrariesCustomizer.getLocalizedString(impl.getLocalizingBundle(),impl.getName()),
                index, isSelected, cellHasFocus);
    }

}
