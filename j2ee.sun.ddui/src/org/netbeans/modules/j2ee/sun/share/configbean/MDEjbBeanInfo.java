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
package org.netbeans.modules.j2ee.sun.share.configbean;

import java.beans.*;

public class MDEjbBeanInfo extends SimpleBeanInfo {

    // Bean descriptor//GEN-FIRST:BeanDescriptor
    /*lazy BeanDescriptor*/
    private static BeanDescriptor getBdescriptor(){
        BeanDescriptor beanDescriptor = new BeanDescriptor  ( MDEjb.class , org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.MDEjbCustomizer.class );//GEN-HEADEREND:BeanDescriptor

        // Here you can add code for customizing the BeanDescriptor.

        return beanDescriptor;         }//GEN-LAST:BeanDescriptor
    
    
    // Property identifiers//GEN-FIRST:Properties
    private static final int PROPERTY_beanPool = 0;
    private static final int PROPERTY_ejbName = 1;
    private static final int PROPERTY_iorSecurityConfig = 2;
    private static final int PROPERTY_jndiName = 3;
    private static final int PROPERTY_maxMessageLoad = 4;
    private static final int PROPERTY_mdbConnectionFactory = 5;
    private static final int PROPERTY_mdbResourceAdapter = 6;
    private static final int PROPERTY_passByReference = 7;
    private static final int PROPERTY_subscriptionName = 8;

    // Property array 
    /*lazy PropertyDescriptor*/
    private static PropertyDescriptor[] getPdescriptor(){
        PropertyDescriptor[] properties = new PropertyDescriptor[9];
    
        try {
            properties[PROPERTY_beanPool] = new PropertyDescriptor ( "beanPool", MDEjb.class, "getBeanPool", "setBeanPool" );
            properties[PROPERTY_ejbName] = new PropertyDescriptor ( "ejbName", MDEjb.class, "getEjbName", null );
            properties[PROPERTY_iorSecurityConfig] = new PropertyDescriptor ( "iorSecurityConfig", MDEjb.class, "getIorSecurityConfig", "setIorSecurityConfig" );
            properties[PROPERTY_jndiName] = new PropertyDescriptor ( "jndiName", MDEjb.class, "getJndiName", "setJndiName" );
            properties[PROPERTY_maxMessageLoad] = new PropertyDescriptor ( "maxMessageLoad", MDEjb.class, "getMaxMessageLoad", "setMaxMessageLoad" );
            properties[PROPERTY_maxMessageLoad].setHidden ( true );
            properties[PROPERTY_mdbConnectionFactory] = new PropertyDescriptor ( "mdbConnectionFactory", MDEjb.class, "getMdbConnectionFactory", "setMdbConnectionFactory" );
            properties[PROPERTY_mdbResourceAdapter] = new PropertyDescriptor ( "mdbResourceAdapter", MDEjb.class, "getMdbResourceAdapter", "setMdbResourceAdapter" );
            properties[PROPERTY_passByReference] = new PropertyDescriptor ( "passByReference", MDEjb.class, "getPassByReference", "setPassByReference" );
            properties[PROPERTY_subscriptionName] = new PropertyDescriptor ( "subscriptionName", MDEjb.class, "getSubscriptionName", "setSubscriptionName" );
            properties[PROPERTY_subscriptionName].setHidden ( true );
        }
        catch( IntrospectionException e) {}//GEN-HEADEREND:Properties
        
        // Here you can add code for customizing the properties array.
        
        return properties;         }//GEN-LAST:Properties
    
    // EventSet identifiers//GEN-FIRST:Events
    private static final int EVENT_propertyChangeListener = 0;
    private static final int EVENT_vetoableChangeListener = 1;

    // EventSet array
    /*lazy EventSetDescriptor*/
    private static EventSetDescriptor[] getEdescriptor(){
        EventSetDescriptor[] eventSets = new EventSetDescriptor[2];
    
            try {
            eventSets[EVENT_propertyChangeListener] = new EventSetDescriptor ( org.netbeans.modules.j2ee.sun.share.configbean.MDEjb.class, "propertyChangeListener", java.beans.PropertyChangeListener.class, new String[] {"propertyChange"}, "addPropertyChangeListener", "removePropertyChangeListener" );
            eventSets[EVENT_vetoableChangeListener] = new EventSetDescriptor ( org.netbeans.modules.j2ee.sun.share.configbean.MDEjb.class, "vetoableChangeListener", java.beans.VetoableChangeListener.class, new String[] {"vetoableChange"}, "addVetoableChangeListener", "removeVetoableChangeListener" );
        }
        catch( IntrospectionException e) {}//GEN-HEADEREND:Events
        
        // Here you can add code for customizing the event sets array.
        
        return eventSets;         }//GEN-LAST:Events
    
    // Method identifiers//GEN-FIRST:Methods

    // Method array 
    /*lazy MethodDescriptor*/
    private static MethodDescriptor[] getMdescriptor(){
        MethodDescriptor[] methods = new MethodDescriptor[0];//GEN-HEADEREND:Methods
        
        // Here you can add code for customizing the methods array.
        
        return methods;         }//GEN-LAST:Methods
    
    
    private static final int defaultPropertyIndex = -1;//GEN-BEGIN:Idx
    private static final int defaultEventIndex = -1;//GEN-END:Idx
    
    
//GEN-FIRST:Superclass
    
    // Here you can add code for customizing the Superclass BeanInfo.
    
//GEN-LAST:Superclass
    
    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     *
     * @return BeanDescriptor describing the editable
     * properties of this bean.  May return null if the
     * information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
        return getBdescriptor();
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
        return getPdescriptor();
    }
    
    /**
     * Gets the bean's <code>EventSetDescriptor</code>s.
     *
     * @return  An array of EventSetDescriptors describing the kinds of
     * events fired by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public EventSetDescriptor[] getEventSetDescriptors() {
        return getEdescriptor();
    }
    
    /**
     * Gets the bean's <code>MethodDescriptor</code>s.
     *
     * @return  An array of MethodDescriptors describing the methods
     * implemented by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public MethodDescriptor[] getMethodDescriptors() {
        return getMdescriptor();
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

    /** Return an appropriate icon (currently, only 16x16 color is available)
     */
    public java.awt.Image getIcon(int iconKind) {
            return loadImage("resources/MessageBean.png");	// NOI18N
    }
}

