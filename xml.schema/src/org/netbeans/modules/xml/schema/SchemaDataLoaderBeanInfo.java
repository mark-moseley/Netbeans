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
package org.netbeans.modules.xml.schema;

import java.beans.*;
import java.awt.Image;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import org.openide.util.Utilities;

/**
 * Loader BeanInfo adding metadata missing in org.openide.loaders.MultiFileLoaderBeanInfo.
 *
 * @author Petr Kuzel
 */
public class SchemaDataLoaderBeanInfo extends SimpleBeanInfo {

    private static final String ICON_DIR_BASE = "org/netbeans/modules/xml/schema/resources/"; // NOI18N

    private static final int PROPERTY_extensions = 0;
    private static final int PROPERTY_displayName = 1;
    private static final int PROPERTY_representationClass = 2;

    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     *
     * @return BeanDescriptor describing the editable
     * properties of this bean.  May return null if the
     * information should be obtained by automatic analysis.
     */
    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor beanDescriptor = new BeanDescriptor  ( SchemaDataLoader.class , null );
        beanDescriptor.setDisplayName ( NbBundle.getMessage(SchemaDataLoaderBeanInfo.class, "LBL_SchemaDataLoader_name") );
        beanDescriptor.setShortDescription ( NbBundle.getMessage(SchemaDataLoaderBeanInfo.class, "LBL_SchemaDataLoader_desc") );//GEN-HEADEREND:BeanDescriptor
        
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
    @Override public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] properties = new PropertyDescriptor[3];
    
        try {
            properties[PROPERTY_extensions] = new PropertyDescriptor ( "extensions", SchemaDataLoader.class, "getExtensions", "setExtensions" );
            properties[PROPERTY_extensions].setPreferred ( true );
            properties[PROPERTY_extensions].setDisplayName ( NbBundle.getMessage(SchemaDataLoaderBeanInfo.class, "PROP_SchemaDataLoader_extensions_name") );
            properties[PROPERTY_extensions].setShortDescription ( NbBundle.getMessage(SchemaDataLoaderBeanInfo.class, "PROP_SchemaDataLoader_extensions_desc") );
            properties[PROPERTY_displayName] = new PropertyDescriptor ( "displayName", SchemaDataLoader.class, "getDisplayName", null );
            properties[PROPERTY_displayName].setDisplayName ( NbBundle.getMessage(SchemaDataLoaderBeanInfo.class, "PROP_SchemaDataLoader_dname_name") );
            properties[PROPERTY_displayName].setShortDescription ( NbBundle.getMessage(SchemaDataLoaderBeanInfo.class, "PROP_SchemaDataLoader_dname_desc") );
            properties[PROPERTY_representationClass] = new PropertyDescriptor ( "representationClass", SchemaDataLoader.class, "getRepresentationClass", null );
            properties[PROPERTY_representationClass].setExpert ( true );
            properties[PROPERTY_representationClass].setDisplayName ( NbBundle.getMessage(SchemaDataLoaderBeanInfo.class, "PROP_SchemaDataLoader_class_name") );
            properties[PROPERTY_representationClass].setShortDescription ( NbBundle.getMessage(SchemaDataLoaderBeanInfo.class, "PROP_SchemaDataLoader_class_desc") );
        }
        catch( IntrospectionException e) {
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
        return new EventSetDescriptor[0];
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
    
    /**
     * This method returns an image object that can be used to
     * represent the bean in toolboxes, toolbars, etc.   Icon images
     * will typically be GIFs, but may in future include other formats.
     * <p>
     * Beans aren't required to provide icons and may return null from
     * this method.
     * <p>
     * There are four possible flavors of icons (16x16 color,
     * 32x32 color, 16x16 mono, 32x32 mono).  If a bean choses to only
     * support a single icon we recommend supporting 16x16 color.
     * <p>
     * We recommend that icons have a "transparent" background
     * so they can be rendered onto an existing background.
     *
     * @param  iconKind  The kind of icon requested.  This should be
     *    one of the constant values ICON_COLOR_16x16, ICON_COLOR_32x32,
     *    ICON_MONO_16x16, or ICON_MONO_32x32.
     * @return  An image object representing the requested icon.  May
     *    return null if no suitable icon is available.
     */
    public Image getIcon (int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
            (type == java.beans.BeanInfo.ICON_MONO_16x16)) {

            return Utilities.loadImage (ICON_DIR_BASE + "Schema_File.gif"); // NOI18N
        } 
	
	return null;
    }
    
}
