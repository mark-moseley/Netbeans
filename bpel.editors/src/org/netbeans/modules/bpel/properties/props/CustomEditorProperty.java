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
package org.netbeans.modules.bpel.properties.props;

import java.beans.PropertyEditor;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.properties.editors.FormBundle;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.properties.props.editors.NodePropEditor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * This Property is very special.
 * It has associated Custom Editor and this edior is the Custom Editor of the parent Node.
 * So it allows showing Node's Custom editor from the Property Sheet.
 * <p>
 * The value of the property has the type of the parent Node.
 * <p>
 * @author nk160297
 */
public class CustomEditorProperty extends Node.Property {
    
    private BpelNode parentNode;
    
    public CustomEditorProperty(BpelNode parentNode) {
        super(parentNode.getClass());
        //
        setHidden(parentNode.isModelReadOnly());
        //
        String name = NbBundle.getMessage(FormBundle.class, "LBL_Property_Editor"); // NOI18N
        setName(name);
        setDisplayName(name);
        setValue("canEditAsText", Boolean.FALSE); // NOI18N
        this.parentNode = parentNode;
    }
    
    public boolean canRead() {
        return true;
    }
    
    public boolean canWrite() {
        return !parentNode.isModelReadOnly();
    }
    
    public void setValue(Object object) {
    }
    
    public Object getValue() {
        return parentNode;
    }
    
    public PropertyEditor getPropertyEditor() {
        PropertyEditor editor = new NodePropEditor();
        return editor;
    }
    
}
