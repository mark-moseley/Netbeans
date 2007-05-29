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

package org.netbeans.modules.j2ee.sun.ddloaders.multiview;

import org.netbeans.modules.xml.multiview.ItemEditorHelper;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;

/**
 * @author pfiala,pcw
 */
public abstract class TextItemEditorModel extends ItemEditorHelper.ItemEditorModel {

    protected XmlMultiViewDataSynchronizer synchronizer;
    private boolean emptyAllowed;
    private boolean emptyIsNull;

    protected TextItemEditorModel(XmlMultiViewDataSynchronizer synchronizer, boolean emptyAllowed) {
        this(synchronizer, emptyAllowed, false);
    }
    
    protected TextItemEditorModel(XmlMultiViewDataSynchronizer synchronizer, boolean emptyAllowed, boolean emptyIsNull) {
        this.synchronizer = synchronizer;
        this.emptyAllowed = emptyAllowed;
        this.emptyIsNull = emptyIsNull;
    }

    protected boolean validate(String value) {
        return emptyAllowed ? true : value != null && value.length() > 0;
    }

    protected abstract void setValue(String value);

    protected abstract String getValue();

    public final boolean setItemValue(String value) {
        if (emptyAllowed && emptyIsNull && value != null) {
            while (value.length() > 0 && value.charAt(0) == ' ') {
                value = value.substring(1);
            }
            if (value.length() == 0) {
                value = null;
            }
        }
        if (validate(value)) {
            String currentValue = getValue();
            if (!(value == currentValue || value != null && value.equals(currentValue))) {
                setValue(value);
                synchronizer.requestUpdateData();
            }
            return true;
        } else {
            return false;
        }
    }

    public final String getItemValue() {
        String value = getValue();
        return value == null ? "" : value;
    }

    public void documentUpdated() {
        setItemValue(getEditorText());
    }
}
