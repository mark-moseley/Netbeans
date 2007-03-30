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
/*
 * Created on Mar 16, 2004
 *
 * To change the template for this generated file go to Window&gt;Preferences&gt;Java&gt;Code
 * Generation&gt;Code and Comments
 */
package org.netbeans.modules.visualweb.text.actions;

import org.netbeans.modules.visualweb.api.designer.HtmlDomProvider;
import java.awt.event.ActionEvent;

import javax.swing.UIManager;

import org.openide.util.NbBundle;

import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.text.DesignerPaneBase;


/*
 * Deletes the character of content that precedes the current caret position.
 * Note the part about "character of content"; this may actually delete multiple
 * characters in the DOM. I've implemented this to pick the previous -visual-
 * location in the document (e.g. the location that pressing the left arrow key
 * would take you to) and removing the characters in between.
 */
public class DeletePrevCharAction extends TextAction {
    /**
     * Creates this object with the appropriate identifier.
     */
    public DeletePrevCharAction() {
        super(DesignerPaneBase.deletePrevCharAction);
    }

    /**
     * The operation to perform when this action is triggered.
     *
     * @param e
     *            the action event
     */
    public void actionPerformed(ActionEvent e) {
        DesignerPaneBase target = getTextComponent(e);
        boolean beep = true;

        if ((target != null) /* && (target.isEditable()) */) {
//            Document doc = target.getDocument();
//            WebForm webform = doc.getWebForm();
            WebForm webform = target.getWebForm();

//            if (webform.getModel().getLiveUnit() == null) {
            // XXX This seems to be wrong. Model should handle it.
            if (!webform.isAlive()) {
                // Can't mutate document
                UIManager.getLookAndFeel().provideErrorFeedback(target);

                return;
            }

//            DesignerCaret caret = target.getCaret();
//            if (caret == null) {
            if (!target.hasCaret()) {
                return;
            }

////            UndoEvent undoEvent = webform.getModel().writeLock(NbBundle.getMessage(DeleteNextCharAction.class, "DeleteText")); // NOI18N
//            HtmlDomProvider.WriteLock writeLock = webform.writeLock(NbBundle.getMessage(DeleteNextCharAction.class, "DeleteText")); // NOI18N
//            try {
//                doc.writeLock(NbBundle.getMessage(DeleteNextCharAction.class, "DeleteText")); // NOI18N
//                beep = !caret.removePreviousChar();
                beep = !target.removePreviousChar();
//            } finally {
////                doc.writeUnlock();
////                webform.getModel().writeUnlock(undoEvent);
//                webform.writeUnlock(writeLock);
//            }
        }

        if (beep) {
            UIManager.getLookAndFeel().provideErrorFeedback(target);
        }
    }
}
