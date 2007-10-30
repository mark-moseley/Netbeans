/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.websvc.rest.codegen.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author PeterLiu
 */
public class EntityResourceBeanModel {
    private EntityResourceModelBuilder builder;
    private Map<String, EntityResourceBean> containerResourceBeanMap;
    private Map<String, EntityResourceBean> itemResourceBeanMap;
    private boolean valid;
    
    
    /** Creates a new instance of ResourceBeanModel */
    public EntityResourceBeanModel(EntityResourceModelBuilder builder) {
        this.builder = builder;
        containerResourceBeanMap = new HashMap<String, EntityResourceBean>();
        itemResourceBeanMap = new HashMap<String, EntityResourceBean>();
    }
    
    public void addContainerResourceBean(EntityResourceBean bean) {
        containerResourceBeanMap.put(bean.getEntityClassInfo().getType(), bean);
    }
    
    public void addItemResourceBean(EntityResourceBean bean) {
        itemResourceBeanMap.put(bean.getEntityClassInfo().getType(), bean);
    }
    
    public EntityResourceBean getContainerResourceBean(EntityClassInfo info) {
        return containerResourceBeanMap.get(info);
    }
   
    public EntityResourceBean getContainerResourceBean(String entityType) {
        return containerResourceBeanMap.get(entityType);
    }
    
    public EntityResourceBean getItemResourceBean(EntityClassInfo info) {
        return getItemResourceBean(info.getEntity().getClass2());
    }
    
    public EntityResourceBean getItemResourceBean(String entityType) {
        return itemResourceBeanMap.get(entityType);
    }
   
    public Collection<EntityResourceBean> getResourceBeans() {
        ArrayList<EntityResourceBean> resourceBeans = new ArrayList<EntityResourceBean>();
        
        resourceBeans.addAll(containerResourceBeanMap.values());
        resourceBeans.addAll(itemResourceBeanMap.values());
        
        return resourceBeans;
    }
    
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean v) {
        valid = v;
    }
    
    public EntityResourceModelBuilder getBuilder() {
        return builder;
    }
}
