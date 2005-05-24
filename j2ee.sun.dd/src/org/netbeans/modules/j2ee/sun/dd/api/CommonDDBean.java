/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.dd.api;

import org.netbeans.modules.schema2beans.BaseBean;

/**
 * Parent of all DD API interfaces.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 *
 * @author Milan Kuchtiak
 */
public interface CommonDDBean {
    
    public static final int MERGE_INTERSECT = BaseBean.MERGE_INTERSECT;
    public static final int MERGE_UNION	= BaseBean.MERGE_UNION;
    public static final int MERGE_UPDATE = BaseBean.MERGE_UPDATE;
        
    public void merge(CommonDDBean root, int mode); 
    
    /**
     * Adds property change listener to particular CommonDDBean object (WebApp object).
     * @param pcl property change listener
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener pcl);
    /**
     * Removes property change listener from CommonDDBean object.
     * @param pcl property change listener
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener pcl);
    /**
     * Returns the CommonDDBean object or array of CommonDDBean object for given property.<br>
     * E.g. for retrieving the servlet-class value on Servlet object he <b>getValue("ServletClass");</b> can be used.
     * @param propertyName name of the property the value is looking for
     * @return the bean/array of beans related to given property
     */      
    public Object getValue(String propertyName);
    
    public Object[] getValues(String name);
    
    public Object getValue(String name, int index);
    
    public void setValue(String name, Object value);
    
    public void setValue(String name, Object[] value);
    
    public void setValue(String name, int index, Object value);
    
    public String getAttributeValue(String name);
    
    public String getAttributeValue(String propName, String name);
    
    public String getAttributeValue(String propName, int index, String name);
    
    public void setAttributeValue(String name, String value);
    
    public void setAttributeValue(String propName, int index, String name, String value);
    
    public void setAttributeValue(String propName, String name, String value);
    
    public String[] findPropertyValue(String propName, Object value);
    
    public int addValue(String name, Object value);
    
    public int removeValue(String name, Object value);
    
    public void removeValue(String name, int index);
    
    public int size(String name);
         
    
    /**
     * Writes the whole DD or its fraction (element related to CommonDDBean) to output stream.<br>
     * For DD root object there is more convenient to use the {@link org.netbeans.modules.j2ee.dd.api.common.RootInterface#write} method.<br>
     * The correct usage with file objects is :<pre>
WebApp ejb;
FileObject fo;
...
//  code that initializes and modifies the ejb object
...
FileLock lock;
try {
    lock=fo.lock();
} catch (FileAlreadyLockedException e) {
    // handling the exception
}
if (lock!=null) {
    try {
        OutputStream os=fo.getOutputStream(lock);
        try {
            ejb.write(os);
        } finally {
            os.close();
        }
    } finally {
        lock.releaseLock();
    }
}
...
     *</pre>
     * @param os output stream for writing
     */  
    public void write(java.io.OutputStream os) throws java.io.IOException;

    public void write(java.io.Writer w) throws java.io.IOException, org.netbeans.modules.j2ee.sun.dd.api.DDException;
        
    public Object clone();
  
    /**
     * Used to convert an graph to the corresponding XML in String form.
     */
    public String dumpBeanNode(); 
    
    public CommonDDBean getPropertyParent(String name);
    
}
