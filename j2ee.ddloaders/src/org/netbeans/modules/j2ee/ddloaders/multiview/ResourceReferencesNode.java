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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

/**
 * @author pfiala
 */
public class ResourceReferencesNode extends EjbSectionNode {

    public ResourceReferencesNode(SectionNodeView sectionNodeView, Ejb ejb) {
        super(sectionNodeView, ejb, Utils.getBundleMessage("LBL_ResourceReferences"), Utils.ICON_BASE_MISC_NODE);
    }

    protected SectionInnerPanel createNodeInnerPanel() {
        return new InnerTablePanel(getSectionNodeView(), new ResourceReferencesTableModel((Ejb) key));
    }
}
