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

package org.netbeans.lib.lexer.inc;

import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchyEventType;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.lexer.LexerApiPackageAccessor;
import org.netbeans.lib.lexer.LexerSpiPackageAccessor;
import org.netbeans.lib.lexer.TokenHierarchyOperation;
import org.netbeans.spi.lexer.MutableTextInput;

/**
 * Shared information for all the token list changes
 * for a single token hierarchy event.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenHierarchyEventInfo {

    private final TokenHierarchyOperation<?,?> tokenHierarchyOperation;

    private final TokenHierarchyEventType type;
    
    private TokenChange<? extends TokenId> tokenChange;

    private final int modificationOffset;

    private final int removedLength;

    private final CharSequence removedText;

    private final int insertedLength;
    
    private final int diffLengthOrZero;

    private OriginalText originalText;
    
    private int affectedStartOffset;
    
    private int affectedEndOffset;

    public TokenHierarchyEventInfo(TokenHierarchyOperation<?,? extends TokenId> tokenHierarchyOperation,
    TokenHierarchyEventType type, int modificationOffset, int removedLength, CharSequence removedText, int insertedLength) {
        // Initial checks
        if (modificationOffset < 0) {
            throw new IllegalArgumentException("modificationOffset=" + modificationOffset + " < 0"); // NOI18N
        }
        if (removedLength < 0) {
            throw new IllegalArgumentException("removedLength=" + removedLength + " < 0"); // NOI18N
        }
        if (insertedLength < 0) {
            throw new IllegalArgumentException("insertedLength=" + insertedLength + " < 0"); // NOI18N
        }

        this.tokenHierarchyOperation = tokenHierarchyOperation;
        this.type = type;
        this.modificationOffset = modificationOffset;
        this.removedLength = removedLength;
        this.removedText = removedText;
        this.insertedLength = insertedLength;
        this.diffLengthOrZero = Math.max(0, insertedLength - removedLength);
        this.affectedStartOffset = modificationOffset;
        this.affectedEndOffset = modificationOffset + diffLengthOrZero;
    }

    public TokenHierarchyOperation<?,? extends TokenId> tokenHierarchyOperation() {
        return tokenHierarchyOperation;
    }

    public TokenHierarchyEventType type() {
        return type;
    }
    
    public TokenChange<? extends TokenId> tokenChange() {
        return tokenChange;
    }
    
    public void setTokenChangeInfo(TokenChangeInfo<? extends TokenId> info) {
        this.tokenChange = LexerApiPackageAccessor.get().createTokenChange(info);
    }
    
    public int affectedStartOffset() {
        return affectedStartOffset;
    }
    
    public void setMinAffectedStartOffset(int affectedStartOffset) {
        if (affectedStartOffset < this.affectedStartOffset) {
            this.affectedStartOffset = affectedStartOffset;
        }
    }

    public int affectedEndOffset() {
        return affectedEndOffset;
    }
    
    public void setMaxAffectedEndOffset(int affectedEndOffset) {
        if (affectedEndOffset > this.affectedEndOffset) {
            this.affectedEndOffset = affectedEndOffset;
        }
    }

    public int modificationOffset() {
        return modificationOffset;
    }

    public int removedLength() {
        return removedLength;
    }

    public CharSequence removedText() {
        return removedText;
    }

    public int insertedLength() {
        return insertedLength;
    }
    
    public CharSequence insertedText() {
        return currentText().subSequence(modificationOffset(), modificationOffset() + insertedLength());
    }
    
    /**
     * Get <code>Math.max(0, insertedLength() - removedLength())</code>.
     */
    public int diffLengthOrZero() {
        return diffLengthOrZero;
    }

    public OriginalText originalText() {
        if (originalText == null) {
            if (removedLength != 0 && removedText == null) {
                throw new IllegalStateException("Cannot obtain removed text for " // NOI18N
                        + tokenHierarchyOperation.mutableInputSource()
                        + " which breaks token snapshots operation and" // NOI18N
                        + " token text retaining after token's removal." // NOI18N
                        + " Valid removedText in TokenHierarchyControl.textModified()" // NOI18N
                        + " should be provided." // NOI18N
                        );
            }
            originalText = new OriginalText(currentText(),
                    modificationOffset, removedText, insertedLength);
        }
        return originalText;
    }
    
    public CharSequence currentText() {
        return tokenHierarchyOperation.text();
    }
    
    public String modificationDescription(boolean detail) {
        StringBuilder sb = new StringBuilder(originalText().length() + 300);
        if (removedLength() > 0) {
            sb.append("TEXT REMOVED <").append(modificationOffset()).append(","). // NOI18N
                    append(modificationOffset() + removedLength()).append('>');
            sb.append(':').append(removedLength());
            if (removedText() != null) {
                sb.append(" \"");
                CharSequenceUtilities.debugText(sb, removedText());
                sb.append('"');
            }
            sb.append('\n');
        }
        if (insertedLength() > 0) {
            sb.append("TEXT INSERTED <").append(modificationOffset()).append(","). // NOI18N
                    append(modificationOffset() + insertedLength()).append(">:"). // NOI18N
                    append(insertedLength()).append(" \""); // NOI18N
            CharSequenceUtilities.debugText(sb, insertedText());
            sb.append("\"\n");
        }
        if (detail) {
            sb.append("\n\n----------------- ORIGINAL TEXT -----------------\n" + // NOI18N
                originalText() +
                "\n----------------- BEFORE-CARET TEXT -----------------\n" + // NOI18N
                originalText().subSequence(0, modificationOffset()) +
                "|<--CARET\n" // NOI18N
            );
        }
        return sb.toString();
    }
    
    public String dumpAffected() {
        StringBuilder sb = new StringBuilder("Affected(");
        sb.append(affectedStartOffset());
        sb.append(",");
        sb.append(affectedEndOffset());
        sb.append(')');
        return sb.toString();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("modOffset="); // NOI18N
        sb.append(modificationOffset());
        if (removedLength() > 0) {
            sb.append(", removedLength=");
            sb.append(removedLength());
        }
        if (insertedLength() > 0) {
            sb.append(", insertedLength=");
            sb.append(insertedLength()); // NOI18N
        }
        sb.append(", ").append(dumpAffected());
        return sb.toString();
    }

}
