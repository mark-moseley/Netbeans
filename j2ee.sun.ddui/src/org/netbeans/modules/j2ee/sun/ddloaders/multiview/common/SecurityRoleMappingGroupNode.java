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

package org.netbeans.modules.j2ee.sun.ddloaders.multiview.common;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.api.app.SunApplication;
import org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class SecurityRoleMappingGroupNode extends NamedBeanGroupNode {

    public SecurityRoleMappingGroupNode(SectionNodeView sectionNodeView, RootInterface rootDD, ASDDVersion version) {
        super(sectionNodeView, rootDD, SecurityRoleMapping.ROLE_NAME, 
                NbBundle.getMessage(SecurityRoleMappingGroupNode.class, "LBL_SecurityRoleMappingGroupHeader"),
                ICON_BASE_SECURITY_ROLE_MAPPING_NODE, version);
    }

    protected SectionNode createNode(CommonDDBean bean) {
        return new SecurityRoleMappingNode(getSectionNodeView(), (SecurityRoleMapping) bean, version);
    }

    protected CommonDDBean [] getBeansFromModel() {
        SecurityRoleMapping [] mappings = null;
        
        // TODO find a better way to do this for common beans.
        if(rootDD instanceof SunWebApp) {
            mappings = ((SunWebApp) rootDD).getSecurityRoleMapping();
        } else if(rootDD instanceof SunEjbJar) {
            mappings = ((SunEjbJar) rootDD).getSecurityRoleMapping();
        } else if(rootDD instanceof SunApplication) {
            mappings = ((SunApplication) rootDD).getSecurityRoleMapping();
        }
        return mappings;
    }

}
