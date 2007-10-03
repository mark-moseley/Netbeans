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

package org.netbeans.modules.db.sql.execute;

/**
 *
 * @author Andrei Badea
 */
public class StatementInfo {

    private final String sql;
    private final int rawStartOffset;
    private final int startOffset;
    private final int startLine;
    private final int startColumn;
    private final int rawEndOffset;
    private final int endOffset;

    public StatementInfo(String sql, int rawStartOffset, int startOffset, int startLine, int startColumn, int endOffset, int rawEndOffset) {
        this.sql = sql;
        this.rawStartOffset = rawStartOffset;
        this.startOffset = startOffset;
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endOffset = endOffset;
        this.rawEndOffset = rawEndOffset;
    }

    /**
     * Returns the SQL text statement with comments and leading and trailing
     * whitespace removed.
     */
    public String getSQL() {
        return sql;
    }

    /**
     * Returns the start offset of the raw SQL text (including comments and leading whitespace).
     */
    public int getRawStartOffset() {
        return rawStartOffset;
    }

    /**
     * Returns the start offset of the text returned by {@link #getSQL}.
     */
    public int getStartOffset() {
        return startOffset;
    }

    /**
     * Returns the zero-based number of the line corresponding to {@link #getStartOffset}.
     */
    public int getStartLine() {
        return startLine;
    }

    /**
     * Returns the zero-based number of the column corresponding to {@link #getStartOffset}.
     */
    public int getStartColumn() {
        return startColumn;
    }

    /**
     * Returns the end offset of the text returned by {@link #getSQL}.
     */
    public int getEndOffset() {
        return endOffset;
    }

    /**
     * Returns the end offset of the raw SQL text (including comments and trailing whitespace).
     */
    public int getRawEndOffset() {
        return rawEndOffset;
    }
}
