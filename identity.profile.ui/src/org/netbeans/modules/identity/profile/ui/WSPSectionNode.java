/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.identity.profile.ui;

import org.netbeans.modules.identity.profile.ui.editor.*;
import org.netbeans.modules.identity.profile.ui.support.J2eeProjectHelper;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.util.NbBundle;

/**
 * Represents the WSP section node in the web service attribute editor.
 *
 * Created on April 14, 2006, 4:59 PM
 *
 * @author ptliu
 */
public class WSPSectionNode extends SecuritySectionNode {
    private static String WSP = "wsp";      //NOI18N
    
    private J2eeProjectHelper helper;
    private WSPSecurityPanel currentPanel;
    
    /** Creates a new instance of WSPSectionNode */
    public WSPSectionNode(SectionNodeView sectionNodeView,
            J2eeProjectHelper helper) {
        super(sectionNodeView, WSP,
                NbBundle.getMessage(WSPSectionNode.class, "TTL_WSPSecurityConfiguration"),
                "org/netbeans/modules/identity/profile/ui/resources/MiscNodeIcon"); // NOI18N
          
        this.helper = helper;
    }
    
    public void save() {
        if (currentPanel != null) {
            currentPanel.save();
        }
    }
    
    protected SectionNodeInnerPanel createNodeInnerPanel() {
        currentPanel = new WSPSecurityPanel(getSectionNodeView(), helper);
        return currentPanel;
    }
}
