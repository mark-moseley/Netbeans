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
package org.netbeans.modules.xml.tax.beans.beaninfo;

import java.beans.*;
import org.netbeans.tax.TreeDTD;
import org.netbeans.modules.xml.tax.beans.customizer.TreeDTDCustomizer;
import org.netbeans.modules.xml.tax.beans.editor.VersionEditor;
import org.netbeans.modules.xml.tax.beans.editor.EncodingEditor;
import org.openide.util.Exceptions;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeDTDBeanInfo extends SimpleBeanInfo {

    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     * 
     * @return BeanDescriptor describing the editable
     * properties of this bean.  May return null if the
     * information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
	return new BeanDescriptor  ( TreeDTD.class , TreeDTDCustomizer.class );
    }

    /**
     * Gets the bean's <code>PropertyDescriptor</code>s.
     * 
     * @return An array of PropertyDescriptors describing the editable
     * properties supported by this bean.  May return null if the
     * information should be obtained by automatic analysis.
     * <p>
     * If a property is indexed, then its entry in the result array will
     * belong to the IndexedPropertyDescriptor subclass of PropertyDescriptor.
     * A client of getPropertyDescriptors can use "instanceof" to check
     * if a given PropertyDescriptor is an IndexedPropertyDescriptor.
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        int PROPERTY_encoding = 0;
        int PROPERTY_version = 1;
        PropertyDescriptor[] properties = new PropertyDescriptor[2];

        try {
            properties[PROPERTY_encoding] = new PropertyDescriptor ( "encoding", TreeDTD.class, "getEncoding", null ); // NOI18N
            properties[PROPERTY_encoding].setDisplayName ( Util.THIS.getString ( "PROP_TreeDTDBeanInfo_encoding" ) );
            properties[PROPERTY_encoding].setShortDescription ( Util.THIS.getString ( "HINT_TreeDTDBeanInfo_encoding" ) );
            properties[PROPERTY_encoding].setPropertyEditorClass ( EncodingEditor.class );

            properties[PROPERTY_version] = new PropertyDescriptor ( "version", TreeDTD.class, "getVersion", null ); // NOI18N
            properties[PROPERTY_version].setDisplayName ( Util.THIS.getString ( "PROP_TreeDTDBeanInfo_version" ) );
            properties[PROPERTY_version].setShortDescription ( Util.THIS.getString ( "HINT_TreeDTDBeanInfo_version" ) );
            properties[PROPERTY_version].setPropertyEditorClass ( VersionEditor.class );
        } catch( IntrospectionException e) {
            Exceptions.printStackTrace(e);
        }
        return properties;
    }

    /**
     * Gets the bean's <code>EventSetDescriptor</code>s.
     * 
     * @return  An array of EventSetDescriptors describing the kinds of 
     * events fired by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public EventSetDescriptor[] getEventSetDescriptors() {
        EventSetDescriptor[] eventSets = new EventSetDescriptor[1];

        try {
            eventSets[0] = new EventSetDescriptor ( org.netbeans.tax.TreeDTD.class, "propertyChangeListener", java.beans.PropertyChangeListener.class, new String[] {"propertyChange"}, "addPropertyChangeListener", "removePropertyChangeListener" ); // NOI18N
        } catch( IntrospectionException e) {
            Exceptions.printStackTrace(e);
        }
	return eventSets;
    }

    /**
     * Gets the bean's <code>MethodDescriptor</code>s.
     * 
     * @return  An array of MethodDescriptors describing the methods 
     * implemented by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public MethodDescriptor[] getMethodDescriptors() {
	return new MethodDescriptor[0];
    }

}
