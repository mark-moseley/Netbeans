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
import org.netbeans.modules.j2ee.sun.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanNode;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.ServletPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;


/**
 * @author Peter Williams
 */
public class ServletNode extends NamedBeanNode {

//    private final ServletGroupNode.ServletData servletData;
    
    public ServletNode(SectionNodeView sectionNodeView, final DDBinding binding, final ASDDVersion version) {
        super(sectionNodeView, binding, Servlet.SERVLET_NAME, ICON_BASE_SERVLET_NODE, version);

//        this.servletData = data;
        enableRemoveAction();
    }
    
    protected SectionNodeInnerPanel createNodeInnerPanel() {
        return new ServletPanel(getSectionNodeView(), this, version);
    }
    
//    public ServletGroupNode.ServletData getServletData() {
//        return servletData;
//    }
//    
//    public boolean addVirtualBean() {
//        if(servletData.isVirtual()) {
//            Node parentNode = getParentNode();
//            if(parentNode instanceof ServletGroupNode) {
//                ServletGroupNode groupNode = (ServletGroupNode) parentNode;
//                servletData.clearVirtual();
//                groupNode.addBean(servletData.sunServlet);
//                
//                SunDescriptorDataObject dataObject = (SunDescriptorDataObject) getSectionNodeView().getDataObject();
//                XmlMultiViewDataSynchronizer synchronizer = dataObject.getModelSynchronizer();
//                synchronizer.requestUpdateData();
//                return true;
//            }
//        }
//        return false;
//    }
}
