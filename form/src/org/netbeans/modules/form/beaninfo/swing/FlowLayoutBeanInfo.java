/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.beaninfo.swing;

import java.beans.*;
import java.awt.FlowLayout;

public class FlowLayoutBeanInfo extends BISupport {

    public FlowLayoutBeanInfo() {
        super("flowLayout", java.awt.FlowLayout.class); // NOI18N
    }

    protected PropertyDescriptor[] createPropertyDescriptors() throws IntrospectionException {
        PropertyDescriptor[] pds = new PropertyDescriptor[] {
            createRW(FlowLayout.class, "alignment"), // NOI18N
            createRW(FlowLayout.class, "hgap"), // NOI18N
            createRW(FlowLayout.class, "vgap"), // NOI18N
        };
        pds[0].setPropertyEditorClass(AlignmentPropertyEditor.class);
        return pds;
    }

    
    
    public static class AlignmentPropertyEditor extends BISupport.TaggedPropertyEditor {
        public AlignmentPropertyEditor() {
            super(
                new int[] {
                    FlowLayout.CENTER,
                    FlowLayout.LEFT,
                    FlowLayout.RIGHT
                },
                new String[] {
                    "java.awt.FlowLayout.CENTER", // NOI18N
                    "java.awt.FlowLayout.LEFT", // NOI18N
                    "java.awt.FlowLayout.RIGHT" // NOI18N
                },
                new String[] {
                    "VALUE_AlignmentCenter", // NOI18N
                    "VALUE_AlignmentLeft", // NOI18N
                    "VALUE_AlignmentRight", // NOI18N
                }
            );
        }
    }

}
