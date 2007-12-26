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

package org.netbeans.modules.cnd.apt.support;

import antlr.CommonToken;
import antlr.Token;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.utils.cache.TextCache;

/**
 * token to be used in APT infrastructure
 * @author Vladimir Voskresensky
 */
public class APTBaseToken extends CommonToken implements APTToken {
    private static final long serialVersionUID = 2834353662691067170L;
    
    private int offset;
    /**
     * Creates a new instance of APTBaseToken
     */
    public APTBaseToken() {
    }

    public APTBaseToken(Token token) {
        this(token, token.getType());
    }
    
    public APTBaseToken(Token token, int ttype) {
        this.setColumn(token.getColumn());
        this.setFilename(token.getFilename());
        this.setLine(token.getLine());
        
        // This constructor is used with the existing tokens so do not use setText here, 
        // because we do not need to go through APTStringManager once again
        text = token.getText();
        
        this.setType(ttype);
        if (token instanceof APTToken) {
            APTToken aptToken = (APTToken)token;
            this.setOffset(aptToken.getOffset());
            this.setEndOffset(aptToken.getEndOffset());
            this.setEndColumn(aptToken.getEndColumn());
            this.setEndLine(aptToken.getEndLine());
            this.setTextID(aptToken.getTextID());
        }
    }
    
    public APTBaseToken(String text) {
        this.setText(text);
    }
    
    public int getOffset() {
        return this.offset;
    }

    public void setOffset(int o) {
        this.offset = o;
    }

    public int getEndOffset() {
        return getOffset() + getText().length();
    }

    public void setEndOffset(int end) {
        // do nothing
    }
    
    public int getTextID() {
//        return textID;
        return -1;
    }
    
    public void setTextID(int textID) {
//        this.textID = textID;
    }
  
    public String getText() {
        // TODO: get from shared string map
        String res = super.getText();
        return res;
    }
    
    public void setText(String t) {
        if (APTTraceFlags.APT_SHARE_TEXT) {
            t = TextCache.getString(t).toString();
        }
        super.setText(t);
    }
    
    public String toString() {
        return "[\"" + getText() + "\",<" + getType() + ">,line=" + getLine() + ",col=" + getColumn() + "]" + ",offset="+getOffset()+",file="+getFilename(); // NOI18N
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
        // do nothing
    }
}
