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

package org.netbeans.modules.cnd.editor.parser;

import org.openide.ErrorManager;

public class CppFoldRecord {

    // Fold types
    public final static int INITIAL_COMMENT_FOLD = CppFile.INITIAL_COMMENT_FOLD;
    public final static int BLOCK_COMMENT_FOLD = CppFile.BLOCK_COMMENT_FOLD;
    public final static int COMMENTS_FOLD = CppFile.COMMENTS_FOLD;    
    public final static int INCLUDES_FOLD = CppFile.INCLUDES_FOLD;
    public final static int IFDEF_FOLD = CppFile.IFDEF_FOLD;
    public final static int CLASS_FOLD = CppFile.CLASS_FOLD;
    public final static int FUNCTION_FOLD = CppFile.FUNCTION_FOLD;
    public final static int CONSTRUCTOR_FOLD = CppFile.CONSTRUCTOR_FOLD;
    public final static int DESTRUCTOR_FOLD = CppFile.DESTRUCTOR_FOLD;
    public final static int NAMESPACE_FOLD = CppFile.NAMESPACE_FOLD;
    
    private int type;
    private int startLnum;
    private int endLnum;
    private int startOffset;
    private int endOffset;

    private static final ErrorManager log =
		ErrorManager.getDefault().getInstance("CppFoldTracer"); // NOI18N

    public CppFoldRecord(int type, int startLnum, int startOffset, int endLnum, int endOffset)
    {
	this.type = type;
	this.startLnum = startLnum;
	this.startOffset = startOffset;
	this.endLnum = endLnum;
	this.endOffset = endOffset;
	log.log(toString() + " [" + Thread.currentThread().getName() + "]"); // NOI18N
    }

    public CppFoldRecord(int type, int startOffset, int endOffset)
    {
	this.type = type;
	this.startLnum = -1;
	this.startOffset = startOffset;
	this.endLnum = -1;
	this.endOffset = endOffset;
    }
    
    public void setLines(int start, int end) {
        this.startLnum = start;
        this.endLnum = end;
    }
    
    public int getType() {
	return type;
    }

    public int getStartOffset() {
	return startOffset;
    }

    public int getEndOffset() {
	return endOffset;
    }

    public int getStartLine() {
        assert startLnum != -1;
        return startLnum;
    }
    
    public int getEndLine() {
        assert endLnum != -1;
        return endLnum;
    }
    
    @Override
    public String toString() {
        // I'm considering this as a debug function and making all
	// strings NOI18N
        
        String kind = "Unknown Fold"; // NOI18N
        switch (type) {
            case INITIAL_COMMENT_FOLD:
                kind = "INITIAL_COMMENT_FOLD"; // NOI18N
                break;
            case BLOCK_COMMENT_FOLD:
                kind = "BLOCK_COMMENT_FOLD"; // NOI18N
                break;
            case COMMENTS_FOLD:
                kind = "COMMENTS_FOLD"; // NOI18N
                break;
            case INCLUDES_FOLD:
                kind = "INCLUDES_FOLD"; // NOI18N
                break;
            case IFDEF_FOLD:
                kind = "IFDEF_FOLD"; // NOI18N
                break;
            case CLASS_FOLD:
                kind = "CLASS_FOLD"; // NOI18N
                break;
            case FUNCTION_FOLD:
                kind = "FUNCTION_FOLD"; // NOI18N
                break;
            case CONSTRUCTOR_FOLD:
                kind = "CONSTRUCTOR_FOLD"; // NOI18N
                break;
            case DESTRUCTOR_FOLD:
                kind = "DESTRUCTOR_FOLD"; // NOI18N
                break;
            case NAMESPACE_FOLD:
                kind = "NAMESPACE_FOLD"; // NOI18N
                break;
            default:
        }
	return kind + " (" + startLnum + // NOI18N
		", " + startOffset + ", " + endLnum + ", " + endOffset + ")"; // NOI18N
    }
}
