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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

/**
 * @author pfiala
 */
public class BeanEnvironmentNode extends EjbSectionNode {

    BeanEnvironmentNode(SectionNodeView sectionNodeView, Ejb ejb) {
        super(sectionNodeView, false, ejb, Utils.getBundleMessage("LBL_BeanEnvironment"), Utils.ICON_BASE_MISC_NODE);
        addChild(new EjbReferencesNode(sectionNodeView, ejb));
        addChild(new EnvironmentEntriesNode(sectionNodeView, ejb));
        addChild(new ResourceReferencesNode(sectionNodeView, ejb));
        addChild(new ResourceEnvironmentReferencesNode(sectionNodeView, ejb));
        if (ejb instanceof EntityAndSession) {
            addChild(new SecurityRoleReferencesNode(sectionNodeView, (EntityAndSession) ejb));
        }
    }

    protected SectionNodeInnerPanel createNodeInnerPanel() {
        return null;
    }
}
