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

package org.netbeans.modules.vmd.midp.propertyeditors.timezone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import javax.swing.*;

/**
 *
 * @author Anton Chechel
 */
class TimeZoneComboboxModel extends DefaultComboBoxModel {
    
    public TimeZoneComboboxModel() {
        fill();
    }
    
    private void fill() {
        List<String> list = new ArrayList<String>();
        String[] ids = TimeZone.getAvailableIDs();
        for (String id : ids) {
            list.add(id);
        }
        Collections.sort(list);
        for (String id : list) {
            addElement(id);
        }
    }
}
