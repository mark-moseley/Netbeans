/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tax.beans.beaninfo;

import java.beans.*;

import org.netbeans.tax.TreeDocument;
import org.netbeans.modules.xml.tax.beans.customizer.TreeDocumentCustomizer;
import org.netbeans.modules.xml.tax.beans.editor.VersionEditor;
import org.netbeans.modules.xml.tax.beans.editor.StandaloneEditor;
import org.netbeans.modules.xml.tax.beans.editor.EncodingEditor;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeDocumentBeanInfo extends SimpleBeanInfo {

    // Bean descriptor //GEN-FIRST:BeanDescriptor
    private static BeanDescriptor beanDescriptor = new BeanDescriptor  ( TreeDocument.class , TreeDocumentCustomizer.class );

    static {//GEN-HEADEREND:BeanDescriptor

	// Here you can add code for customizing the BeanDescriptor.

    }//GEN-LAST:BeanDescriptor

    // Property identifiers //GEN-FIRST:Properties
    private static final int PROPERTY_encoding = 0;
    private static final int PROPERTY_version = 1;
    private static final int PROPERTY_standalone = 2;

    // Property array 
    private static PropertyDescriptor[] properties = new PropertyDescriptor[3];

    static {
        try {
            properties[PROPERTY_encoding] = new PropertyDescriptor ( "encoding", TreeDocument.class, "getEncoding", "setEncoding" ); // NOI18N
            properties[PROPERTY_encoding].setDisplayName ( Util.THIS.getString ( "PROP_TreeDocumentBeanInfo_encoding" ) );
            properties[PROPERTY_encoding].setShortDescription ( Util.THIS.getString ( "HINT_TreeDocumentBeanInfo_encoding" ) );
            properties[PROPERTY_encoding].setPropertyEditorClass ( EncodingEditor.class );

            properties[PROPERTY_version] = new PropertyDescriptor ( "version", TreeDocument.class, "getVersion", "setVersion" ); // NOI18N
            properties[PROPERTY_version].setDisplayName ( Util.THIS.getString ( "PROP_TreeDocumentBeanInfo_version" ) );
            properties[PROPERTY_version].setShortDescription ( Util.THIS.getString ( "HINT_TreeDocumentBeanInfo_version" ) );
            properties[PROPERTY_version].setPropertyEditorClass ( VersionEditor.class );

            properties[PROPERTY_standalone] = new PropertyDescriptor ( "standalone", TreeDocument.class, "getStandalone", "setStandalone" ); // NOI18N
            properties[PROPERTY_standalone].setDisplayName ( Util.THIS.getString ( "PROP_TreeDocumentBeanInfo_standalone" ) );
            properties[PROPERTY_standalone].setShortDescription ( Util.THIS.getString ( "HINT_TreeDocumentBeanInfo_standalone" ) );
            properties[PROPERTY_standalone].setPropertyEditorClass ( StandaloneEditor.class );
        } catch( IntrospectionException e) {}//GEN-HEADEREND:Properties

	// Here you can add code for customizing the properties array.

    }//GEN-LAST:Properties

    // EventSet identifiers//GEN-FIRST:Events
    private static final int EVENT_propertyChangeListener = 0;

    // EventSet array
    private static EventSetDescriptor[] eventSets = new EventSetDescriptor[1];

    static {
        try {
            eventSets[EVENT_propertyChangeListener] = new EventSetDescriptor ( org.netbeans.tax.TreeDocument.class, "propertyChangeListener", java.beans.PropertyChangeListener.class, new String[] {"propertyChange"}, "addPropertyChangeListener", "removePropertyChangeListener" ); // NOI18N
        }
        catch( IntrospectionException e) {}//GEN-HEADEREND:Events

        // Here you can add code for customizing the event sets array.

    }//GEN-LAST:Events

    // Method identifiers //GEN-FIRST:Methods

    // Method array 
    private static MethodDescriptor[] methods = new MethodDescriptor[0];
    //GEN-HEADEREND:Methods

    // Here you can add code for customizing the methods array.
    
    //GEN-LAST:Methods

    private static final int defaultPropertyIndex = -1;//GEN-BEGIN:Idx
    private static final int defaultEventIndex = -1;//GEN-END:Idx


    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     * 
     * @return BeanDescriptor describing the editable
     * properties of this bean.  May return null if the
     * information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
	return beanDescriptor;
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
        return methods;
    }

    /**
     * A bean may have a "default" property that is the property that will
     * mostly commonly be initially chosen for update by human's who are 
     * customizing the bean.
     * @return  Index of default property in the PropertyDescriptor array
     * 		returned by getPropertyDescriptors.
     * <P>	Returns -1 if there is no default property.
     */
    public int getDefaultPropertyIndex() {
        return defaultPropertyIndex;
    }

    /**
     * A bean may have a "default" event that is the event that will
     * mostly commonly be used by human's when using the bean. 
     * @return Index of default event in the EventSetDescriptor array
     *		returned by getEventSetDescriptors.
     * <P>	Returns -1 if there is no default event.
     */
    public int getDefaultEventIndex() {
        return defaultEventIndex;
    }

}
