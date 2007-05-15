/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.utils.helper.swing;

import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.netbeans.installer.utils.SystemUtils;

/**
 *
 * @author Kirill Sorokin
 */
public class NbiScrollPane extends JScrollPane {
    public NbiScrollPane(final JComponent component) {
        super(component);
        
        setFocusable(true);
        
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        // l&f-specific tweaks
        if (!UIManager.getLookAndFeel().getID().equals("GTK")) {
            setViewportBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
        }
        
        // system-specific tweaks
        if (SystemUtils.isMacOS()) {
            setOpaque(false);
            getViewport().setOpaque(false);
        }
    }
}
