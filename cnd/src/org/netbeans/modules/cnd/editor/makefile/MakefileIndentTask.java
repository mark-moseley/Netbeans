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

package org.netbeans.modules.cnd.editor.makefile;

import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;

/**
 * Indentation support for Makefiles.
 *
 * @author Alexey Vladykin
 */
class MakefileIndentTask implements IndentTask {

    private static final int INDENT = 8;

    private final Context context;

    public MakefileIndentTask(Context context) {
        this.context = context;
    }

    public ExtraLock indentLock() {
        return null; // no extra locking
    }

    public void reindent() throws BadLocationException {
        if (context.isIndent()) {
            int caretOffset = context.caretOffset();
            if (isRuleOrActionLine((BaseDocument) context.document(), caretOffset)) {
                context.modifyIndent(context.lineStartOffset(caretOffset), INDENT);
            }
        }
    }

    private static boolean isRuleOrActionLine(BaseDocument doc, int offset) throws BadLocationException {
        int start = Utilities.getRowStart(doc, offset - 1);
        String line = doc.getText(start, offset - start);
        int colon = line.indexOf(':'); // NOI18N
        int pound = line.indexOf('#'); // NOI18N
        return (line.charAt(0) == '\t') // NOI18N
                || (colon > 0 && pound == -1)
                || (colon > 0 && colon < pound);
    }

}
