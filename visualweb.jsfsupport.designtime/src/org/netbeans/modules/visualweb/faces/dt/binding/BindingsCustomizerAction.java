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
package org.netbeans.modules.visualweb.faces.dt.binding;

import com.sun.rave.designtime.*;
import com.sun.rave.designtime.impl.*;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;

public class BindingsCustomizerAction extends BasicDisplayAction {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(BindingsCustomizerAction.class);

    public BindingsCustomizerAction(DesignBean bean) {
        super(bundle.getMessage("propBindingsEllipse")); //NOI18N
        this.bean = bean;
    }

    protected DesignBean bean;
    public Result invoke() {
        BindingsCustomizer blc = new BindingsCustomizer(bean);
        return new CustomizerResult(bean, blc);
    }
}
