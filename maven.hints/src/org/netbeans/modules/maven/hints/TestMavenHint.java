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
 * Contributor(s): theanuradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.hints;

import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Anuradha
 */
public class TestMavenHint extends AbstractHint {

    private static final List<Fix> NO_FIXES = Collections.<Fix>emptyList();
    private static final Set<Tree.Kind> TREE_KINDS =
            EnumSet.<Tree.Kind>of(Tree.Kind.METHOD_INVOCATION);

    public TestMavenHint() {
        super(true, true, AbstractHint.HintSeverity.WARNING);
    }

    public Set<Kind> getTreeKinds() {

        return TREE_KINDS;
    }

    public List<ErrorDescription> run(CompilationInfo info, TreePath treePath) {

        Tree t = treePath.getLeaf();
       
        Element el = info.getTrees().getElement(treePath);
        String name = el.getSimpleName().toString();

        if (name.equals("getOnlineEmbedder")) {
            return Collections.<ErrorDescription>singletonList(
                    ErrorDescriptionFactory.createErrorDescription(
                    getSeverity().toEditorSeverity(),
                    getDisplayName(),
                    NO_FIXES,
                    info.getFileObject(),
                    (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), t),
                    (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), t)));

        }

        return null;
    }

    public void cancel() {
        // Does nothing
    }

    public String getId() {
        return "MAVEN_TEST"; // NOI18N

    }

    public String getDisplayName() {
        return "Maven test hint :using getOnlineEmbedder";
    }

    public String getDescription() {
        return "You are using MavenEmbedder from EmbedderFactory :-)";
    }
}