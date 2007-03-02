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
package org.netbeans.modules.visualweb.faces.dt.std.table;

import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.CustomizerResult;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.impl.BasicDisplayAction;

public class HtmlDataTableCustomizerAction extends BasicDisplayAction {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(
        HtmlDataTableCustomizerAction.class);

    public HtmlDataTableCustomizerAction(DesignBean bean) {
        super(bundle.getMessage("tblLayoutEllipse")); //NOI18N
        setHelpKey("projrave_ui_elements_dialogs_data_table_layout_db"); //NOI18N
        this.bean = bean;
    }

    protected DesignBean bean;

    public Result invoke() {
        return new CustomizerResult(bean, new HtmlDataTableCustomizer());
    }
}
