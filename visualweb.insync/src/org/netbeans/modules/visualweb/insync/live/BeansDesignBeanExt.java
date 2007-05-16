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
package org.netbeans.modules.visualweb.insync.live;

import com.sun.rave.designtime.DesignInfo;
import com.sun.rave.designtime.ext.DesignBeanExt;
import java.beans.BeanInfo;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.visualweb.insync.beans.Bean;
import org.netbeans.modules.visualweb.insync.java.ClassUtil;

/**
 *
 * @author jdeva
 */
public class BeansDesignBeanExt extends BeansDesignBean implements DesignBeanExt{

    public BeansDesignBeanExt(LiveUnit unit, BeanInfo beanInfo, DesignInfo liveBeanInfo,
                         SourceDesignBean parent, Object instance, Bean bean) {
        super(unit, beanInfo, liveBeanInfo, parent, instance, bean);
    }

    public Type[] getTypeParameters() throws ClassNotFoundException { 
        List<Type> typeList = new ArrayList<Type>();
        for(String typeParamName : bean.getTypeParameterNames()) {
            Class type = ClassUtil.getClass(typeParamName);
            typeList.add(type);
        }
        return typeList.toArray(new Type[0]);
    }
}
