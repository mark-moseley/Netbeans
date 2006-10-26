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

package org.netbeans.modules.editor.java;

import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.DrawLayer;
import org.netbeans.editor.DrawLayerFactory;
import org.netbeans.modules.editor.NbEditorDocument;

/**
 *
 * @author Jan Lahoda
 */
public class JavaDocument extends NbEditorDocument {

    public JavaDocument(Class kitClass) {
        super(kitClass);
        putProperty(Language.class, JavaTokenId.language());
    }

    @Override
    public boolean addLayer(DrawLayer layer, int visibility) {
        if (DrawLayerFactory.SyntaxLayer.class.equals(layer.getClass()))
            return false;
        
        return super.addLayer(layer, visibility);
    }
    
}
