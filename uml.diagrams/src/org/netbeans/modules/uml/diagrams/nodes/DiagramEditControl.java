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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uml.diagrams.nodes;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import org.netbeans.api.visual.action.InplaceEditorProvider.EditorController;
import org.netbeans.modules.uml.ui.controls.editcontrol.EditControlImpl;

/**
 *
 * @author treyspiva
 */
public class DiagramEditControl extends EditControlImpl
{

    private EditorController controller = null;

    public DiagramEditControl(Object parent, EditorController editor)
    {
        this(parent, false, editor);
    }
    
    public DiagramEditControl(Object parent, 
                              boolean multiline, 
                              EditorController editor)
    {
        super(parent, multiline);

//        KeyStroke ctrlEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
//                KeyEvent.CTRL_MASK);
        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);

        AbstractAction commitAction = new CommitAction();
        AbstractAction escapeAction = new CancelAction();

//        assignAction(ctrlEnter, DefaultEditorKit.insertBreakAction);
        assignAction(enter, commitAction, "commit");
        assignAction(escape, escapeAction, "cancel-edit");  

        this.controller = editor;
    }

    private class CommitAction extends AbstractAction
    {

        public CommitAction()
        {
        }

        public void actionPerformed(ActionEvent event)
        {
            // The Visual Library requires the edit controlto have focus, for it
            // to tell the parent to request focus.  However, since our edit 
            // control is a panel that ownes a coupld of other children, one
            // of its children actually has focus.  I have tried to force
            // the EditControl to have focus, but no luck.  The child 
            // continues the hold on to the focus.  Therefore, I will just
            // set the focus back to the parent.
            //
            // This fixes issue http://www.netbeans.org/issues/show_bug.cgi?id=127512
            Container parent = getParent();
            controller.closeEditor(true);
            
            parent.requestFocusInWindow();
        }
    }

    private class CancelAction extends AbstractAction
    {

        public CancelAction()
        {
        }

        public void actionPerformed(ActionEvent event)
        {
            // The Visual Library requires the edit controlto have focus, for it
            // to tell the parent to request focus.  However, since our edit 
            // control is a panel that ownes a coupld of other children, one
            // of its children actually has focus.  I have tried to force
            // the EditControl to have focus, but no luck.  The child 
            // continues the hold on to the focus.  Therefore, I will just
            // set the focus back to the parent.
            //
            // This fixes issue http://www.netbeans.org/issues/show_bug.cgi?id=127512
            Container parent = getParent();
            controller.closeEditor(false);
            
            parent.requestFocusInWindow();
        }
    }
}
