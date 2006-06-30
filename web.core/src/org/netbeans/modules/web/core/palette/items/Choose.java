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
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.web.core.palette.JSPPaletteUtilities;
import org.openide.text.ActiveEditorDrop;

/**
 *
 * @author Libor Kotouc
 */
public class Choose implements ActiveEditorDrop {

    public static final int DEFAULT_WHENS = 1;

    private int whens = DEFAULT_WHENS;
    private boolean otherwise = true;

    public Choose() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        ChooseCustomizer c = new ChooseCustomizer(this, targetComponent);
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
        
        String cBody = generateChooseBody();
        String body = 
                "<c:choose>\n" + // NOI18N
                cBody +
                "</c:choose>\n"; // NOI18N
        
        return body;
    }
    
    private String generateChooseBody() {
        
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < whens; i++)
            sb.append("<c:when test=\"\">\n</c:when>\n"); // NOI18N
        
        if (otherwise)
            sb.append("<c:otherwise>\n</c:otherwise>\n"); // NOI18N
                
        String cBody = sb.toString();
        
        return cBody;
    }

    public int getWhens() {
        return whens;
    }

    public void setWhens(int whens) {
        this.whens = whens;
    }

    public boolean isOtherwise() {
        return otherwise;
    }

    public void setOtherwise(boolean otherwise) {
        this.otherwise = otherwise;
    }
    
}
