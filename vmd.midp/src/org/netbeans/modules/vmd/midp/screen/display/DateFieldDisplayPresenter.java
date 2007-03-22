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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.midp.screen.display;

import java.util.Date;
import javax.swing.JLabel;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.items.DateFieldCD;

/**
 *
 * @author Anton Chechel
 * @version 1.0
 */
public class DateFieldDisplayPresenter extends ItemDisplayPresenter {
    
    private JLabel label;

    public DateFieldDisplayPresenter() {
        label = new JLabel();
        setContentComponent(label);
    }
    
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);
        
        PropertyValue pv = getComponent().readProperty(DateFieldCD.PROP_DATE);
        String text = "<date>"; // NOI18N
        if (pv.getKind() == PropertyValue.Kind.VALUE) { // not user code
            text = new Date(MidpTypes.getLong(pv)).toString(); // TODO use formatter
        }
        label.setText(text);
    }
}
