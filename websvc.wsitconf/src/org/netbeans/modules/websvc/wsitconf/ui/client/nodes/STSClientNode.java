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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.ui.client.nodes;

import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 */
public class STSClientNode extends org.openide.nodes.AbstractNode {
       
    public STSClientNode() {
        super(org.openide.nodes.Children.LEAF);
        setDisplayName(NbBundle.getMessage(STSClientNode.class, "LBL_Section_STS_Client"));//NOI18N
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(STSClientNode.class); //NOI18N
    }

    public String getPanelId() {
        return "stsconfig"; //NOI18N
    }
}
