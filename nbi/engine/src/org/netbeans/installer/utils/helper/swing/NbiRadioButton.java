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

import javax.swing.JRadioButton;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;

/**
 *
 * @author Kirill Sorokin
 */
public class NbiRadioButton extends JRadioButton {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public NbiRadioButton() {
        super();
        
        setText(DEFAULT_TEXT);
        setMnemonic(DEFAULT_MNEMONIC);
    }
    
    public void setText(String text) {
        super.setText(StringUtils.stripMnemonic(text));
        
        if (!SystemUtils.isMacOS()) {
            super.setMnemonic(StringUtils.fetchMnemonic(text));
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TEXT =
            ""; // NOI18N
    
    public static final char DEFAULT_MNEMONIC =
            '\u0000';
}
