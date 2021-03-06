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
            if (!view.isOpened()) {
                view.open();
            }
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
