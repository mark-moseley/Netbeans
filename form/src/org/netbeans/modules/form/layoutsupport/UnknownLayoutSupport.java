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

package org.netbeans.modules.form.layoutsupport;

import org.netbeans.modules.form.codestructure.CodeGroup;

/**
 * This class is used internally to provide basic support for containers
 * with unknown layout. Used when no suitable LayoutSupportDelegate is found.
 *
 * @author Tomas Pavek
 */

class UnknownLayoutSupport extends AbstractLayoutSupport {

    public Class getSupportedClass() {
        return null;
    }

    @Override
    protected void readLayoutCode(CodeGroup layoutCode) {
    }
}
