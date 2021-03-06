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

package org.netbeans.modules.html.palette.items;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.html.palette.HTMLPaletteUtilities;
import org.openide.text.ActiveEditorDrop;


/**
 *
 * @author Libor Kotouc
 */
public class FORM implements ActiveEditorDrop {

    public static final String METHOD_GET = "GET"; // NOI18N
    public static final String METHOD_POST = "POST"; // NOI18N

    public static final String ENC_URLENC = "application/x-www-form-urlencoded"; // NOI18N
    public static final String ENC_MULTI = "multipart/form-data"; // NOI18N
    
    private static final String METHOD_DEFAULT = METHOD_GET;
    private static final String ENC_DEFAULT = ENC_URLENC;
    
    private String action = "";
    private String method = METHOD_DEFAULT;
    private String enc = ENC_DEFAULT;
    private String name = "";
    
    public FORM() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        FORMCustomizer c = new FORMCustomizer(this, targetComponent);
        boolean accept = c.showDialog();
        if (accept) {
            String body = createBody();
            try {
                HTMLPaletteUtilities.insert(body, targetComponent);
            } catch (BadLocationException ble) {
                accept = false;
            }
        }
        
        return accept;
    }

    private String createBody() {
        
        String strAction = "";
        if (action.length() > 0)
            strAction = " action=\"" + action + "\""; // NOI18N
        
        String strMethod = "";
        if (!method.equals(METHOD_DEFAULT))
            strMethod = " method=\"" + method + "\""; // NOI18N

        String strEnc = "";
        if (!enc.equals(ENC_DEFAULT))
            strEnc = " enctype=\"" + enc + "\""; // NOI18N

        String strName = "";
        if (name.length() > 0)
            strName = " name=\"" + name + "\""; // NOI18N

        String formBody = "<form" + strName + strAction + strMethod + strEnc + ">\n</form>"; // NOI18N
        
        return formBody;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getEnc() {
        return enc;
    }

    public void setEnc(String enc) {
        this.enc = enc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
        
}
