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

package org.netbeans.modules.web.core.palette.items;

import javax.swing.JSplitPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.web.core.palette.JSPPaletteUtilities;
import org.openide.text.ActiveEditorDrop;


/**
 *
 * @author Libor Kotouc
 */
public class ForEach implements ActiveEditorDrop {

    private String variable = "";
    private String collection = "";
    private boolean fixed = false;
    private String begin = "";
    private String end = "";
    private String step = "";

    public ForEach() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        ForEachCustomizer c = new ForEachCustomizer(this, targetComponent);
        boolean accept = c.showDialog();
        if (accept) {
            String body = createBody();
            try {
                JSPPaletteUtilities.insert(body, targetComponent);
            } catch (BadLocationException ble) {
                accept = false;
            }
        }

        return accept;
    }

    private String createBody() {

        if (variable.equals("")) {// NOI18N
            variable = JSPPaletteUtilities.CARET;
        } else if (collection.equals("")) {// NOI18N
            collection = JSPPaletteUtilities.CARET;
        }
        String strVariable = " var=\"" + variable + "\""; // NOI18N
        String strCollection = " items=\"" + collection + "\""; // NOI18N
        String strBegin = "";
        String strEnd = "";
        String strStep = "";
        if (fixed) {
            if (begin.length() > 0) {
                strBegin = " begin=\"" + begin + "\""; // NOI18N
            }
            if (end.length() > 0) {
                strEnd = " end=\"" + end + "\""; // NOI18N
            }
            if (step.length() > 0) {
                strStep = " step=\"" + step + "\""; // NOI18N
            }
        }

        String fe = "<c:forEach" + strVariable + strCollection + strBegin + strEnd + strStep + ">\n" + "</c:forEach>"; // NOI18N
        return fe;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public void setStep(String step) {
        this.step = step;
    }
}