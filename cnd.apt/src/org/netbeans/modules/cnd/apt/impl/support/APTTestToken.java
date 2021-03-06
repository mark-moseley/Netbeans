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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.apt.impl.support;

import antlr.Token;
import antlr.TokenImpl;
import org.netbeans.modules.cnd.apt.support.APTToken;

/**
 * lightweigth Token implementation (to reduce memory used by APT)
 * @author Vladimir Voskresensky
 */
public class APTTestToken extends TokenImpl implements APTToken {

    private int offset;
    private int textID;

    public APTTestToken() {

    }

    public APTTestToken(Token token) {
        this(token, token.getType());
    }
    
    public APTTestToken(Token token, int ttype) {
        this.setColumn(token.getColumn());
        this.setFilename(token.getFilename());
        this.setLine(token.getLine());
        this.setText(token.getText());
        this.setType(ttype);
        if (token instanceof APTToken) {
            APTToken aptToken = (APTToken)token;
            this.setOffset(aptToken.getOffset());
            this.setEndOffset(aptToken.getEndOffset());
            this.setTextID(aptToken.getTextID());
        }        
    }
    
    public int getOffset() {
        return offset;
    }
      
    public void setOffset(int o) {
        offset = o;
    }
    
    public int getEndOffset() {
        return getOffset() + getText().length();
    }

    public void setEndOffset(int end) {
        // do nothing
    }
    
    public int getTextID() {
        return textID;
    }
    
    public void setTextID(int textID) {
        this.textID = textID;
    }
  
    public String getText() {
        // TODO: use shared string map
        String res = super.getText();
        return res;
    }
    
    public void setText(String text) {
        // TODO: use shared string map
        super.setText(text);
    }    
     
    public String toString() {
        return "[\"" + getText() + "\",<" + getType() + ">,line=" + getLine() + ",col=" + getColumn() + "]"+",offset="+getOffset();//+",file="+getFilename(); // NOI18N
    }

    public int getEndColumn() {
        return getColumn() + getText().length();
    }

    public void setEndColumn(int c) {
        // do nothing
    }

    public int getEndLine() {
        return getLine();
    }

    public void setEndLine(int l) {
        // do nothin
    }
}
