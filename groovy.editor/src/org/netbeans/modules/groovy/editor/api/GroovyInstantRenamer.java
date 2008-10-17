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

package org.netbeans.modules.groovy.editor.api;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.InstantRenamer;
import org.netbeans.modules.gsf.api.OffsetRange;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Variable;
import org.netbeans.editor.BaseDocument;
import org.openide.util.NbBundle;

/**
 *
 * @author schmidtm
 */
public class GroovyInstantRenamer implements InstantRenamer {

    private final Logger LOG = Logger.getLogger(GroovyInstantRenamer.class.getName());

    public GroovyInstantRenamer() {
        LOG.setLevel(Level.OFF);
    }

    public boolean isRenameAllowed(CompilationInfo info, int caretOffset, String[] explanationRetValue) {
        LOG.log(Level.FINEST, "isRenameAllowed()"); //NOI18N

        AstPath path = getPathUnderCaret(info, caretOffset);

        if (path != null) {
            final ASTNode closest = path.leaf();

            if (closest instanceof Variable) {
                return true;
            } else {
                explanationRetValue[0] = NbBundle.getMessage(GroovyInstantRenamer.class, "OnlyRenameLocalVars");
                return false;
            }
        }

        return false;
    }

    public Set<OffsetRange> getRenameRegions(CompilationInfo info, int caretOffset) {
        LOG.log(Level.FINEST, "getRenameRegions()"); //NOI18N

        AstPath path = getPathUnderCaret(info, caretOffset);

        Set<OffsetRange> regions = new HashSet<OffsetRange>();

        markOccurences(path, regions, (BaseDocument) info.getDocument(), caretOffset);

        return regions;
    }

    private AstPath getPathUnderCaret(CompilationInfo info, int caretOffset) {
        ModuleNode rootNode = AstUtilities.getRoot(info);
        if (rootNode == null) {
            return null;
        }
        int astOffset = AstUtilities.getAstOffset(info, caretOffset);
        if (astOffset == -1) {
            return null;
        }
        BaseDocument document = (BaseDocument) info.getDocument();
        if (document == null) {
            LOG.log(Level.FINEST, "Could not get BaseDocument. It's null"); //NOI18N
            return null;
        }

        return new AstPath(rootNode, astOffset, document);

    }

    private static void markOccurences(AstPath path, Set<OffsetRange> regions, BaseDocument document, int cursorOffset) {
        ASTNode root = path.root();
        assert root instanceof ModuleNode;
        ModuleNode moduleNode = (ModuleNode) root;
        VariableScopeVisitor scopeVisitor = new VariableScopeVisitor(moduleNode.getContext(), path, document, cursorOffset);
        scopeVisitor.collect();
        for (ASTNode astNode : scopeVisitor.getOccurrences()) {
            OffsetRange range = AstUtilities.getRange(astNode, document);
            regions.add(range);
        }
    }


}
