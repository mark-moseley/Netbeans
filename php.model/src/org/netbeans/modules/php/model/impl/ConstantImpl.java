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
package org.netbeans.modules.php.model.impl;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.model.ClassMemberReference;
import org.netbeans.modules.php.model.Constant;
import org.netbeans.modules.php.model.PhpModelVisitor;
import org.netbeans.modules.php.model.Reference;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.impl.builders.StaticExpressionBuilder;


/**
 * @author ads
 *
 */
public class ConstantImpl extends SourceElementImpl implements Constant {

    public ConstantImpl( SourceElement parent, ASTNode node, 
            ASTNode realNode ,TokenSequence sequence ) {
        super(parent, node, realNode , sequence);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.SourceElement#getElementType()
     */
    public Class<? extends SourceElement> getElementType() {
        return Constant.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.Acceptor#accept(org.netbeans.modules.php.model.PhpModelVisitor)
     */
    public void accept( PhpModelVisitor visitor ) {
        visitor.visit( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.Constant#getClassConstant()
     */
    public ClassMemberReference<SourceElement> getClassConstant() {
        String nt = getNarrowNode().getNT();
        if ( nt.equals( StaticExpressionBuilder.CLASS_STATIC ) && 
                getNarrowNode().getTokenType( Utils.OPERATOR ) != null ) 
        {
            return new ClassMemberReferenceImpl<SourceElement>( this , getNarrowNode() );
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.Constant#getSourceElement()
     */
    public Reference<SourceElement> getSourceElement() {
        String id = getTokenText();
        if ( id == null ) {
            return null;
        }
        return new ConstantReferenceImpl( this , id );
    }

    private String getTokenText() {
        String nt = getNarrowNode().getNT();
        if ( nt.equals( StaticExpressionBuilder.CLASS_STATIC ) && 
                getNarrowNode().getChildren().size() ==1 ) 
        {
            ASTItem item = getNarrowNode().getChildren().get( 0 );
            assert item instanceof ASTToken;
            assert ((ASTToken) item).getTypeName().equals( Utils.IDENTIFIER );
            return ((ASTToken) item).getIdentifier();
        }
        return null;
    }

}