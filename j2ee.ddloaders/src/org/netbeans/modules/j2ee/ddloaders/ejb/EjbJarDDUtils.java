/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.ejb;

import java.io.IOException;
import java.io.InputStream;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.impl.ejb.EjbJarProxy;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 *
 * @author  mkuchtiak
 */
public class EjbJarDDUtils {
     private EjbJarDDUtils() {}
     
    /** Finds a name similar to requested that uniquely identifies 
     *  element between the other elements of the same name.
     *
     * @param elements checked elements
     * @param identifier name of tag that contains identification value
     * @param o object to be checked
     * @return a free element name
     */
    public static String findFreeName (CommonDDBean[] elements, String identifier, String name) {
        if (checkFreeName (elements, identifier, name)) {
            return name;
        }
        for (int i = 1;;i++) {
            String destName = name + "_"+i; // NOI18N
            if (checkFreeName (elements, identifier, destName)) {
                return destName;
            }
        }
    }
    
    /** Test if given name is free in given context.
     * @param elements checked elements
     * @param identifier name of tag that contains identification value
     * @param o object to be checked
     * @return true, if such name does not exists
     */
    private static boolean checkFreeName (CommonDDBean [] elements, String identifier, Object o) {
        for (int i=0; i<elements.length; i++) {
            Object val = elements[i].getValue (identifier);
            if (val != null && val.equals (o)) {
                return false;
            }
        }
        return true;
    }

    /**  Convenient method for getting the BaseBean object from CommonDDBean object
    */
    public static BaseBean getBaseBean(CommonDDBean bean) {
        if (bean instanceof BaseBean) return (BaseBean)bean;
        else if (bean instanceof EjbJarProxy) return (BaseBean) ((EjbJarProxy)bean).getOriginal();
        return null;
    }
    
    public static EjbJar createEjbJar(InputSource is) throws IOException, SAXException {
        return DDProvider.getDefault().getDDRoot(is);
    }
}
