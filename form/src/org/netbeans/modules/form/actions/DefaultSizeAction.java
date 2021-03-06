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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.form.actions;
import org.openide.util.HelpCtx;
import org.openide.nodes.*;
import org.openide.util.actions.NodeAction;

import org.netbeans.modules.form.*;
import org.netbeans.modules.form.layoutdesign.LayoutDesigner;
import org.netbeans.modules.form.layoutdesign.LayoutModel;

public class DefaultSizeAction extends NodeAction {

    @Override
    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] nodes) {
        for (int i=0; i < nodes.length; i++) {
            if (getValidComponent(nodes[i]) == null)
                return false; // all components must be valid
        }
        return true;
    }

    protected void performAction(Node[] nodes) {
        FormModel formModel = null;
        FormDesigner formDesigner = null;
        LayoutDesigner layoutDesigner = null;
        RADVisualComponent topDesignComponent = null;
        LayoutModel layoutModel = null;
        Object layoutUndoMark = null;
        javax.swing.undo.UndoableEdit layoutUE = null;
        boolean autoUndo = true;

        try {
            for (int i=0; i < nodes.length; i++) {
                RADVisualComponent metacomp = getValidComponent(nodes[i]);
                if (metacomp == null)
                    return; // all components must be valid

                if (layoutDesigner == null) {
                    formModel = metacomp.getFormModel();
                    formDesigner = FormEditor.getFormDesigner(formModel);
                    layoutDesigner = formDesigner.getLayoutDesigner();
                    layoutModel = formModel.getLayoutModel();
                    layoutUndoMark = layoutModel.getChangeMark();
                    layoutUE = layoutModel.getUndoableEdit();
                }
                layoutDesigner.setDefaultSize(metacomp.getId());
                if (metacomp instanceof RADVisualContainer) {
                    formModel.fireContainerLayoutChanged((RADVisualContainer)metacomp, null, null, null);
                    // [should be recursive]
                }
                if (topDesignComponent == null && metacomp == formDesigner.getTopDesignComponent()) {
                    topDesignComponent = metacomp;
                }
                else { // update container the component is in
                    formModel.fireContainerLayoutChanged(metacomp.getParentContainer(), null, null, null);
                }
            }

            if (topDesignComponent != null) {
                formDesigner.resetDesignerSize();
            }
            autoUndo = false;
        } finally  {
            if (layoutUE != null && !layoutUndoMark.equals(layoutModel.getChangeMark())) {
                formModel.addUndoableEdit(layoutUE);
            }
            if (autoUndo) {
                formModel.forceUndoOfCompoundEdit();
            }
        }
    }

    public String getName() {
        return org.openide.util.NbBundle.getBundle(DefaultSizeAction.class)
                .getString("ACT_DefaultSize"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    private static RADVisualComponent getValidComponent(Node node) {
        RADComponentCookie radCookie = node.getCookie(RADComponentCookie.class);
        if (radCookie != null) {
            RADComponent metacomp = radCookie.getRADComponent();
            if (metacomp instanceof RADVisualComponent) {
                RADVisualComponent visualMetaComp = (RADVisualComponent)metacomp;
                RADVisualContainer parent = visualMetaComp.getParentContainer();
                if ((parent != null) && javax.swing.JScrollPane.class.isAssignableFrom(parent.getBeanInstance().getClass())) {
                    visualMetaComp = parent;
                    parent = parent.getParentContainer();
                }
                if (FormUtils.isVisualInDesigner(metacomp) &&
                    ((parent != null && parent.getLayoutSupport() == null
                      && !visualMetaComp.isMenuComponent())
                    || (visualMetaComp instanceof RADVisualContainer
                        && ((RADVisualContainer)visualMetaComp).getLayoutSupport() == null
                        && (!(visualMetaComp instanceof RADVisualFormContainer)
                            || ((RADVisualFormContainer)visualMetaComp).getFormSizePolicy()
                                 != RADVisualFormContainer.GEN_BOUNDS))))
                    return visualMetaComp;
            }
        }
        return null;
    }
}
