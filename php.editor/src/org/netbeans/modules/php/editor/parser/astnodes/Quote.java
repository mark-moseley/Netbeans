/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.php.editor.parser.astnodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents complex qoute(i.e. qoute that includes string and variables).
 * Also represents heredoc
 * <pre>e.g.<pre> 
 * "this is $a quote",
 * "'single ${$complex->quote()}'"
 * >>>Heredoc\n  This is here documents \nHeredoc;\n 
 * 
 * Note: "This is".$not." a quote node",
 *       'This is $not a quote too'
 */
public class Quote extends Expression {

    public enum Type {
        QUOTE,
        SINGLE,
        HEREDOC
    }
    
    private final ArrayList<Expression> expressions = new ArrayList<Expression>();
    private Quote.Type quoteType;

    public Quote(int start, int end, Expression[] expressions, Quote.Type type) {
        super(start, end);

        for (Expression expression : expressions) {
            this.expressions.add(expression);
        }
        this.quoteType = type;
    }

    public Quote(int start, int end, List expressions, Quote.Type type) {
        this(start, end, expressions == null ? null : (Expression[]) expressions.toArray(new Expression[expressions.size()]), type);
    }

    /**
     * @return expression list of the echo statement
     */
    public List<Expression> getExpressions() {
        return this.expressions;
    }

    /**
     * The quote type 
     * @return quote type
     */
    public Quote.Type getQuoteType() {
        return quoteType;
    }
    
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
