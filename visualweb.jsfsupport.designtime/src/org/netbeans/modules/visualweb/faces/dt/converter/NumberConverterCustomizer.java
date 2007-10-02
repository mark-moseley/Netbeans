/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.visualweb.faces.dt.converter;

import java.awt.*;
import com.sun.rave.designtime.*;
import com.sun.rave.designtime.impl.*;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;

/**
 * @author joe and gowri
 */

public class NumberConverterCustomizer extends BasicCustomizer2 {
    private static final ComponentBundle bundle = ComponentBundle.getBundle(NumberConverterCustomizer.class);

    public NumberConverterCustomizer() {
        super(NumberConverterCustomizerPanel.class, bundle.getMessage("numberFormat"), null, "projrave_ui_elements_dialogs_number_converter_db");   //NOI18N
        setApplyCapable(true);
    }

    protected NumberConverterCustomizerPanel nclcp = null;

    public Component getCustomizerPanel(DesignBean designBean) {
        nclcp = new NumberConverterCustomizerPanel(designBean);
        return nclcp;
    }

    public boolean isModified() {
        if (nclcp != null) {
            return nclcp.isModified();
        }
       // return super.isModified();
        return false;
    }

    public Result applyChanges() {
        nclcp.customizerApply();
        return super.applyChanges();
    }
}
