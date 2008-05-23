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

package org.netbeans.modules.php.editor.verification;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Hint;
import org.netbeans.modules.gsf.api.HintSeverity;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.php.editor.PHPLanguage;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class AccidentalAssignementRule extends PHPRule {

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    public String getId() {
        return "accidental.assignment"; //NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(AccidentalAssignementRule.class, "AccidentalAssignementDesc");
    }

    @Override
    public void run(PHPRuleContext context, List<Hint> result) {
       CompilationInfo info = context.compilationInfo;
       
       for (PHPParseResult parseResult : ((List<PHPParseResult>)info.getEmbeddedResults(PHPLanguage.PHP_MIME_TYPE))){
           AccidentalAssignementVisitor visitor = new AccidentalAssignementVisitor(result, info);
           
           if (parseResult.getProgram() != null) {
               parseResult.getProgram().accept(visitor);
               result.addAll(visitor.result);
           }
       }
    }
    
    class AccidentalAssignementVisitor extends DefaultVisitor{
        private List<Hint> result;
        private CompilationInfo info;
        
        AccidentalAssignementVisitor(List<Hint> result, CompilationInfo info){
            this.result = result;
            this.info = info;
        }
                
        @Override
        public void visit(IfStatement node) {
            check(node.getCondition());
            super.visit(node);
        }

        @Override
        public void visit(DoStatement node) {
            check(node.getCondition());
            super.visit(node);
        }

        @Override
        public void visit(ForStatement node) {
            for (Expression expr : node.getConditions()){
                check(expr);
            }
            
            super.visit(node);
        }

        @Override
        public void visit(WhileStatement node) {
            check(node.getCondition());
            super.visit(node);
        }
        
        private void check(Expression expr){
            if (!(expr instanceof Assignment)){
                return;
            }
            
            OffsetRange range = new OffsetRange(expr.getStartOffset(), expr.getEndOffset());
            
            Hint hint = new Hint(AccidentalAssignementRule.this, getDescription(),
                    info.getFileObject(), range, null, 500);
            
            result.add(hint);
        }
    }

    public Set<? extends Object> getKinds() {
        return Collections.singleton("general"); //todo: figure out what is it for
    }
}
