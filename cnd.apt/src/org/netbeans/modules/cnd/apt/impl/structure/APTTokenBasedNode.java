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

package org.netbeans.modules.cnd.apt.impl.structure;

import antlr.Token;
import java.io.Serializable;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * base impl for nodes with associated token
 * @author Vladimir Voskresensky
 */
public abstract class APTTokenBasedNode extends APTBaseNode 
                                        implements Serializable {
    private static final long serialVersionUID = -1540565849389504039L;
    // seems, all will have sibling and token, but not all childs
    transient private APT next;
    private APTToken token;
    
    /** Copy constructor */
    /**package*/APTTokenBasedNode(APTTokenBasedNode orig) {
        super(orig);
        this.token = orig.token;
        // clear tree structure information
        this.next = null;
    }
    
    /** constructor for serialization **/
    protected APTTokenBasedNode() {
    }
    
    /** Creates a new instance of APTTokenBasedNode */
    protected APTTokenBasedNode(Token token) {
        this.token = (APTToken) token;
    }
    
    public void dispose() {
        this.token = APTUtils.EOF_TOKEN;
    }
    
    public Token getToken() {
        return token;
    }   
        
        
    public int getOffset() {
        if (token != null && token != APTUtils.EOF_TOKEN) {
            return token.getOffset();
        }
        return 0;
    }
    
    public int getEndOffset() {
        if (token != null && token != APTUtils.EOF_TOKEN) {
            return token.getEndOffset();
        }
        return 0;        
    }
    
    public abstract APT getFirstChild();
    
    public APT getNextSibling() {
        return next;
    }       
        
    public String getText() {
        return "TOKEN{" + (getToken()!= null ? getToken().toString() : "") + "}"; // NOI18N
    }
    ////////////////////////////////////////////////////////////////////////////
    // implementation details
    
    /** 
     * sets next sibling element
     */
    public final void setNextSibling(APT next) {
        assert (next != null) : "null sibling, what for?"; // NOI18N
        assert (this.next == null) : "why do you change immutable APT?"; // NOI18N
        this.next = next;
    } 
    
    public abstract void setFirstChild(APT child);
}
