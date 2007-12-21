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

package org.netbeans.modules.soa.mappercore.vertexitemeditor;

import java.awt.Component;
import javax.swing.JTextField;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.soa.mappercore.utils.MetalTextFieldBorder;

/**
 *
 * @author anjeleevich
 */
public class StringVertexItemEditor extends 
        AbstractTextVertexItemEditor 
{
    public StringVertexItemEditor() {
        MetalTextFieldBorder.installIfItIsNeeded(this);
    }

    
    public Component getVertexItemEditorComponent(Mapper mapper, 
            TreePath treePath, VertexItem vertexItem) 
    {
        Object value = vertexItem.getValue();
        String text = (value == null) ? null : value.toString();
        setText((text == null) ? "" : text);
        return this;
    }

    
    public Object getVertexItemEditorValue() {
        return getText();
    }
}
