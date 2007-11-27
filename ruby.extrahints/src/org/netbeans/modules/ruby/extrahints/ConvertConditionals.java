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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.extrahints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import org.jruby.ast.IfNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.hints.spi.AstRule;
import org.netbeans.modules.ruby.hints.spi.Description;
import org.netbeans.modules.ruby.hints.spi.Fix;
import org.netbeans.modules.ruby.hints.spi.HintSeverity;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Convert conditionals of the form "if foo; bar; end" to "bar if foo".
 * Inspired by the excellent blog entry
 *   http://langexplr.blogspot.com/2007/11/creating-netbeans-ruby-hints-with-scala_24.html
 * by Luis Diego Fallas.
 * 
 * @author Tor Norbye
 */
public class ConvertConditionals implements AstRule {

    public Set<Integer> getKinds() {
        return Collections.singleton(NodeTypes.IFNODE);
    }

    public void run(CompilationInfo info, Node node, AstPath path, int caretOffset,
                     List<Description> result) {
        IfNode ifNode = (IfNode) node;
        Node body = ifNode.getThenBody();
        Node elseNode = ifNode.getElseBody();

        int start = ifNode.getPosition().getStartOffset();
        if (body != null && (
                // Can't convert blocks with multiple statements
                body.nodeId == NodeTypes.BLOCKNODE ||
                // Already a statement modifier?
                body.getPosition().getStartOffset() <= start)) {
            return;
        } else if (elseNode != null && (
                elseNode.nodeId == NodeTypes.BLOCKNODE ||
                elseNode.getPosition().getStartOffset() <= start)) {
            return;
        }
        
        try {
            BaseDocument doc = (BaseDocument) info.getDocument();
            int keywordOffset = ConvertIfToUnless.findKeywordOffset(info, ifNode);
            if (keywordOffset == -1 || keywordOffset > doc.getLength() - 1) {
                return;
            }

            char k = doc.getText(keywordOffset, 1).charAt(0);
            if (!(k == 'i' || k == 'u')) {
                return; // Probably ternary operator, ?:
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        
        
        // If statement that is not already a statement modifier
        OffsetRange range = AstUtilities.getRange(node);

        Fix fix = new ConvertToModifier(info, ifNode);
        List<Fix> fixes = Collections.singletonList(fix);

        String displayName = NbBundle.getMessage(ConvertConditionals.class, "ConvertConditionals");
        Description desc = new Description(this, displayName, info.getFileObject(), range,
                fixes, 500);
        result.add(desc);
    }

    public String getId() {
        return "ConvertConditionals"; // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(ConvertConditionals.class, "ConvertConditionalsDesc");
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    public boolean appliesTo(CompilationInfo info) {
        // Skip for RHTML files for now - isn't implemented properly
        return info.getFileObject().getMIMEType().equals("text/x-ruby");
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ConvertConditionals.class, "ConvertConditionals");
    }

    public boolean showInTasklist() {
        return false;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.CURRENT_LINE_WARNING;
    }
    
    private class ConvertToModifier implements Fix {
        private CompilationInfo info;
        private IfNode ifNode;

        public ConvertToModifier(CompilationInfo info, IfNode ifNode) {
            this.info = info;
            this.ifNode = ifNode;
        }

        public String getDescription() {
            return NbBundle.getMessage(ConvertConditionals.class, "ConvertConditionalsFix");
        }

        public void implement() throws Exception {
            BaseDocument doc = (BaseDocument) info.getDocument();
            
            Node bodyNode = ifNode.getThenBody();
            boolean isIf = bodyNode != null;
            if (bodyNode == null) {
                bodyNode = ifNode.getElseBody();
            }
            OffsetRange bodyRange = AstUtilities.getRange(bodyNode);
            String body = doc.getText(bodyRange.getStart(), bodyRange.getLength()).trim();
            if (body.endsWith(";")) {
                body = body.substring(0, body.length()-1);
            }
            StringBuilder sb = new StringBuilder();
            sb.append(body);
            sb.append(" ");
            sb.append(isIf ? "if" : "unless"); // NOI18N
            sb.append(" ");
            OffsetRange range = AstUtilities.getRange(ifNode.getCondition());
            sb.append(doc.getText(range.getStart(), range.getLength()));
            
            OffsetRange ifRange = AstUtilities.getRange(ifNode);
            try {
                doc.atomicLock();
                doc.replace(ifRange.getStart(), ifRange.getLength(), sb.toString(), null);
            } finally {
                doc.atomicUnlock();
            }
        }

        public boolean isSafe() {
            return true;
        }

        public boolean isInteractive() {
            return false;
        }
    }
}
