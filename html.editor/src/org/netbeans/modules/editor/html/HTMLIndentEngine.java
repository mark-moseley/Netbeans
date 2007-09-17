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

package org.netbeans.modules.editor.html;

import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.ext.html.HTMLFormatter;
import org.netbeans.modules.editor.FormatterIndentEngine;

public class HTMLIndentEngine extends FormatterIndentEngine {

    private static final long serialVersionUID = -4461748971820599978L;


    /** Creates a new instance of HTMLIndentEngine */
    public HTMLIndentEngine() {
        setAcceptedMimeTypes(new String[] { HTMLKit.HTML_MIME_TYPE });
    }

    protected ExtFormatter createFormatter() {
        // this functionality is now handled by the IndentTask
        return null;//new HTMLFormatter(HTMLKit.class);
    }

}
