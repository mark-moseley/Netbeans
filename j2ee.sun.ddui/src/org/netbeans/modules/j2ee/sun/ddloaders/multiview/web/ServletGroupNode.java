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

package org.netbeans.modules.j2ee.sun.ddloaders.multiview.web;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class ServletGroupNode extends NamedBeanGroupNode {

    private SunWebApp sunWebApp;
    
    public ServletGroupNode(SectionNodeView sectionNodeView, SunWebApp sunWebApp, ASDDVersion version) {
        super(sectionNodeView, sunWebApp, Servlet.SERVLET_NAME, 
                NbBundle.getMessage(ServletGroupNode.class, "LBL_ServletGroupHeader"), // NOI18N
                ICON_BASE_SERVLET_NODE, version);
        
        this.sunWebApp = sunWebApp;
        enableAddAction(NbBundle.getMessage(ServletGroupNode.class, "LBL_AddServlet")); // NOI18N
    }

    protected SectionNode createNode(CommonDDBean bean) {
        return new ServletNode(getSectionNodeView(), (Servlet) bean, version);
    }

    protected CommonDDBean [] getBeansFromModel() {
        return sunWebApp.getServlet();
    }

    protected CommonDDBean addNewBean() {
        Servlet newServlet = sunWebApp.newServlet();
        sunWebApp.addServlet(newServlet);
        newServlet.setServletName("servlet" + getNewBeanId()); // NOI18N
        return newServlet;
    }

    protected void removeBean(CommonDDBean bean) {
        Servlet servlet = (Servlet) bean;
        sunWebApp.removeServlet(servlet);
    }
    
}