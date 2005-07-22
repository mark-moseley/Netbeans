/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx;

import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Parameter;

/**
 * class representing a MBean Attribute.
 * @author tl156378
 */
public class MBeanAttribute {
    
    private String name;
    private String typeName;
    private String description;
    private String access;
    private Method getter;
    private Method setter;
    private boolean isReadable;
    private boolean isWritable;
    
    /** Creates a new instance of MBeanAttribute */
    public MBeanAttribute(String name, String description, 
            Method getter, Method setter) {
        this.name = name;
        this.description = description;
        this.getter = getter;
        this.setter = setter;
        if (getter != null) {
            this.typeName = getter.getType().getName();
            this.isReadable = true;
            if (setter != null)
                this.access = WizardConstants.ATTR_ACCESS_READ_WRITE;
            else
                this.access = WizardConstants.ATTR_ACCESS_READ_ONLY;
        } else {
            this.typeName = 
                ((Parameter) setter.getParameters().get(0)).getType().getName();
            this.access = WizardConstants.ATTR_ACCESS_WRITE_ONLY;
        }
    }
    
    /**
     * Constructor
     * @param attrName the attribute name
     * @param attrType the attribute type
     * @param attrAccess the attribute access mode
     * @param attrDescription the attribute description
     */
    public MBeanAttribute(String attrName, String attrType, String attrAccess,
            String attrDescription) {
        this.name = attrName;
        this.typeName = attrType;
        this.access = attrAccess;
        this.description = attrDescription;
        this.getter = null;
        this.setter = null;
    }
    
    /**
     * Constructor
     * @param attrName the attribute name
     * @param attrType the attribute type
     * @param attrAccess the attribute access mode
     */
    public MBeanAttribute(String attrName, String attrType, String attrAccess) {
        this.name = attrName;
        this.typeName = attrType;
        this.access = attrAccess;
        this.description = ""; // NOI18N
        this.getter = null;
        this.setter = null;
    }
    
    /**
     * Returns the name of the MBean attribute.
     * @return String the name of the MBean
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the attribute name
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isReadable() {
        return (WizardConstants.ATTR_ACCESS_READ_ONLY.equals(access) ||
                WizardConstants.ATTR_ACCESS_READ_WRITE.equals(access));
    }
    
    public boolean isWritable() {
        return (WizardConstants.ATTR_ACCESS_WRITE_ONLY.equals(access) ||
                WizardConstants.ATTR_ACCESS_READ_WRITE.equals(access));
    }
    
    /**
     * Sets the attribute description.
     * @param descr the attribute description to set
     */
    public void setDescription(String descr) {
        this.description = descr;
    }
    
    /**
     * Returns the description for the MBean.
     * @return String the description for the MBean
     */
    public String getDescription() {
        return description;
    }

    public Method getGetter() {
        return getter;
    }

    public Method getSetter() {
        return setter;
    }

    public String getTypeName() {
        return typeName;
    }
    
    /**
     * Sets the attribute access mode.
     * @param access the access mode to set (RO or R/W)
     */
    public void setAccess(String access) {
        this.access = access;
    }
    
    /**
     * Returns the access permission for the MBean.
     * @return String the access permission for the MBean
     */
    public String getAccess() {
        return access;
    }
    
    /**
     * Sets the attribute type name.
     * @param type the type to set
     */
    public void setTypeName(String type) {
        this.typeName = type;
    }
    
}
