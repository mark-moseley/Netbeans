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
package org.netbeans.modules.xml.tax.beans.editor;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import java.beans.FeatureDescriptor;

import org.openide.nodes.Node;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
abstract class NullChoicePropertyEditor extends NullStringEditor implements EnhancedPropertyEditor {

    /** */
    private String[] tags;


    //
    // init
    //

    /** Creates new NullChoicePropertyEditor */
    public NullChoicePropertyEditor (String[] tags) {
        super();

        this.tags = tags;
    }


    //
    // PropertyEditor
    //    
    
    /**
     */
    public boolean supportsCustomEditor () {
        return false;
    }
    
    /**
     */
    public String[] getTags () {
        return tags;
    }
    

    //
    // EnhancedPropertyEditor
    //
    
    /**
     */
    public boolean hasInPlaceCustomEditor () {
        return false;
    }

    /**
     */
    public Component getInPlaceCustomEditor () {
        return null;
    }

    /**
     */
    public boolean supportsEditingTaggedValues () {
        return false;
    }

}
