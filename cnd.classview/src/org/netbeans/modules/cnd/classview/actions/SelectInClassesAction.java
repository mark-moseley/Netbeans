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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.classview.actions;

import javax.swing.JMenuItem;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.classview.ClassViewTopComponent;
import org.netbeans.modules.cnd.classview.resources.I18n;
import org.netbeans.modules.cnd.loaders.CCDataObject;
import org.netbeans.modules.cnd.loaders.CDataObject;
import org.netbeans.modules.cnd.loaders.HDataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author Alexander Simon
 */
public class SelectInClassesAction extends CookieAction {
    
    protected void performAction(Node[] activatedNodes) {
        CsmOffsetableDeclaration decl = ContextUtils.getContext(activatedNodes);
        if (decl != null){
            ClassViewTopComponent view = ClassViewTopComponent.findDefault();
            view.open();
            view.requestActive();
            view.selectInClasses(decl);
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenuItem item = super.getPopupPresenter();
        item.setText(I18n.getMessage("CTL_SelectInClasses")); // NOI18N
        return item;
    }

    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }
    
    public String getName() {
        return I18n.getMessage("CTL_NavigateSelectInClasses"); // NOI18N
    }
    
    protected Class[] cookieClasses() {
        return new Class[] {
            CDataObject.class, CCDataObject.class, HDataObject.class
        };
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
}
