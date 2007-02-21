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
package org.netbeans.modules.bpel.nodes.actions;

import java.util.concurrent.Callable;
import javax.swing.KeyStroke;
import javax.swing.text.StyledDocument;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.nodes.navigator.Util;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.ErrorManager;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Vitaly Bychkov
 * @version 21 April 2006
 */
public class GoToSourceAction extends BpelNodeAction {
    private static final long serialVersionUID = 1L;
    public static final String GOTOSOURCE_KEYSTROKE = 
            NbBundle.getMessage(GoToSourceAction.class,"ACT_GoToSourceAction");// NOI18N
    
    public GoToSourceAction() {
        super();
        putValue(GoToSourceAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(GOTOSOURCE_KEYSTROKE));
    }
    
    protected String getBundleName() {
        return NbBundle.getMessage(getClass(), "CTL_GoToSourceAction"); // NOI18N
    }
    
    
    public ActionType getType() {
        return ActionType.GO_TO_SOURCE;
    }
    
    protected void performAction(BpelEntity[] bpelEntities) {
        if (bpelEntities != null && bpelEntities.length > 0) {
            org.netbeans.modules.bpel.editors.api.utils.Util.goToSource(bpelEntities[0]);
        }
    }
    
    public boolean isChangeAction() {
        return false;
    }
}
