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

package org.netbeans.modules.cnd.editor.fortran.reformat;

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.FortranTokenId;
import org.netbeans.modules.cnd.editor.fortran.reformat.FortranDiffLinkedList.DiffResult;
import org.netbeans.modules.cnd.editor.fortran.reformat.FortranReformatter.Diff;
import static org.netbeans.cnd.api.lexer.FortranTokenId.*;

/**
 *
 * @author Alexander Simon
 */
public class FortranExtendedTokenSequence {
    private final TokenSequence<FortranTokenId> ts;
    private final FortranDiffLinkedList diffs;
    private int tabSize;
    /*package local*/ FortranExtendedTokenSequence(TokenSequence<FortranTokenId> ts, FortranDiffLinkedList diffs, int tabSize){
        this.ts = ts;
        this.diffs = diffs;
        this.tabSize = tabSize;
    }

    /*package local*/ Diff replacePrevious(Token<FortranTokenId> previous, int newLines, int spaces, boolean isIndent){
        String old = previous.text().toString();
        if (!Diff.equals(old, newLines, spaces, isIndent)){
            return diffs.addFirst(ts.offset() - previous.length(),
                                  ts.offset(), newLines, spaces, isIndent);
        }
        return null;
    }

    /*package local*/ Diff addBeforeCurrent(int newLines, int spaces, boolean isIndent){
        if (newLines+spaces>0) {
            return diffs.addFirst(ts.offset(),
                                  ts.offset(), newLines, spaces, isIndent);
        }
        return null;
    }

    /*package local*/ Diff replaceCurrent(Token<FortranTokenId> current, int newLines, int spaces, boolean isIndent){
        String old = current.text().toString();
        if (!Diff.equals(old, newLines, spaces, isIndent)){
            return diffs.addFirst(ts.offset(),
                                  ts.offset() + current.length(), newLines, spaces, isIndent);
        }
        return null;
    }

    /*package local*/ Diff addAfterCurrent(Token<FortranTokenId> current, int newLines, int spaces, boolean isIndent){
        if (newLines+spaces>0) {
            return diffs.addFirst(ts.offset() + current.length(),
                                  ts.offset() + current.length(), newLines, spaces, isIndent);
        }
        return null;
    }

    /*package local*/ Diff replaceNext(Token<FortranTokenId> current, Token<FortranTokenId> next, int newLines, int spaces, boolean isIndent){
        String old = next.text().toString();
        if (!Diff.equals(old, newLines, spaces, isIndent)){
            return diffs.addFirst(ts.offset()+current.length(),
                                  ts.offset()+current.length()+next.length(), newLines, spaces, isIndent);
        }
        return null;
    }

    /*package local*/ int getTokenPosition(){
        int index = ts.index();
        try {
            int column = 0;
            boolean first = true;
            while(ts.movePrevious()){
                DiffResult diff = diffs.getDiffs(this, 0);
                if (diff != null){
                    if (diff.before != null){
                        column+=diff.before.spaceLength();
                        if (diff.before.hasNewLine()) {
                            return column;
                        }
                    }
                    if (diff.replace != null){
                        column+=diff.replace.spaceLength();
                        if (diff.replace.hasNewLine()) {
                            return column;
                        }
                        continue;
                    }
                    if (first && diff.after != null){
                        column+=diff.after.spaceLength();
                        if (diff.after.hasNewLine()) {
                            return column;
                        }
                    }
                }
                first = false;
                switch (ts.token().id()) {
                    case NEW_LINE:
                    case PREPROCESSOR_DIRECTIVE:
                         return column;
                    case LINE_COMMENT_FIXED:
                    case LINE_COMMENT_FREE:
                    {
                        String text = ts.token().text().toString();
                        int i = text.lastIndexOf('\n');
                        if (i < 0){
                            column+=text.length();
                            break;
                        }
                        column += text.length()-i+1;
                        return column;
                    }
                    case WHITESPACE:
                    {
                        String text = ts.token().text().toString();
                        for(int i = 0; i < text.length(); i++){
                            char c = text.charAt(i);
                            if (c == '\t'){
                                column = (column/tabSize+1)* tabSize;
                            } else {
                                column+=1;
                            }
                        }
                        break;
                    }
                    default:
                        column+=ts.token().length();
                        break;
                }
            }
            return column;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ Token<FortranTokenId> lookNextImportant(){
        int index = ts.index();
        try {
            while(ts.moveNext()){
                switch (ts.token().id()) {
                    case WHITESPACE:
                    case NEW_LINE:
                    case LINE_COMMENT_FIXED:
                    case LINE_COMMENT_FREE:
                    case PREPROCESSOR_DIRECTIVE:
                        break;
                    default:
                        return ts.token();
                }
            }
            return null;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ Token<FortranTokenId> lookNextImportant(int i){
        int index = ts.index();
        try {
            while(ts.moveNext()){
                switch (ts.token().id()) {
                    case WHITESPACE:
                    case NEW_LINE:
                    case LINE_COMMENT_FIXED:
                    case LINE_COMMENT_FREE:
                    case PREPROCESSOR_DIRECTIVE:
                        break;
                    default:
                        i--;
                        if (i <= 0) {
                            return ts.token();
                        }
                        break;
                }
            }
            return null;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ Token<FortranTokenId> lookNextLineImportant(){
        int index = ts.index();
        try {
            while(ts.moveNext()){
                switch (ts.token().id()) {
                    case LINE_COMMENT_FIXED:
                    case LINE_COMMENT_FREE:
                    case PREPROCESSOR_DIRECTIVE:
                    case NEW_LINE:
                        return null;
                    case WHITESPACE:
                        break;
                    default:
                        return ts.token();
                }
            }
            return null;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ Token<FortranTokenId> lookNextLineImportant(int i){
        int index = ts.index();
        try {
            while(ts.moveNext()){
                switch (ts.token().id()) {
                    case LINE_COMMENT_FIXED:
                    case LINE_COMMENT_FREE:
                    case PREPROCESSOR_DIRECTIVE:
                    case NEW_LINE:
                        return null;
                    case WHITESPACE:
                        break;
                    default:
                        i--;
                        if (i == 0) {
                            return ts.token();
                        }
                        break;
                }
            }
            return null;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ Token<FortranTokenId> lookNextLineImportantAfter(FortranTokenId tokenId){
        int index = ts.index();
        boolean find = ts.token().id() == tokenId;
        try {
            while(ts.moveNext()){
                switch (ts.token().id()) {
                    case LINE_COMMENT_FIXED:
                    case LINE_COMMENT_FREE:
                    case PREPROCESSOR_DIRECTIVE:
                    case NEW_LINE:
                        return null;
                    case WHITESPACE:
                        break;
                    default:
                        if (ts.token().id() == tokenId) {
                            find = true;
                            break;
                        } else if (find) {
                            return ts.token();
                        }
                        break;
                }
            }
            return null;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ boolean hasLineToken(FortranTokenId tokenId){
        int index = ts.index();
        try {
            while(ts.moveNext()){
                switch (ts.token().id()) {
                    case LINE_COMMENT_FIXED:
                    case LINE_COMMENT_FREE:
                    case PREPROCESSOR_DIRECTIVE:
                    case NEW_LINE:
                        return false;
                    case WHITESPACE:
                        break;
                    default:
                        if (ts.token().id() == tokenId) {
                            return true;
                        }
                        break;
                }
            }
            return false;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ FortranTokenId hasLineToken(FortranTokenId ... tokenId){
        int index = ts.index();
        try {
            while(ts.moveNext()){
                switch (ts.token().id()) {
                    case LINE_COMMENT_FIXED:
                    case LINE_COMMENT_FREE:
                    case PREPROCESSOR_DIRECTIVE:
                    case NEW_LINE:
                        return null;
                    case WHITESPACE:
                        break;
                    default:
                        for(FortranTokenId t: tokenId){
                            if (ts.token().id() == t) {
                                return t;
                            }
                        }
                        break;
                }
            }
            return null;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ Token<FortranTokenId> lookPreviousStatement(){
        int index = ts.index();
        int balance = 0;
        if (ts.token().id() == RPAREN){
            balance = 1;
        }
        try {
            while(ts.movePrevious()){
                switch(ts.token().id()) {
                    case LPAREN:
                        if (balance == 0) {
                            return null;
                        }
                        balance--;
                        break;
                    case RPAREN:
                        balance++;
                        break;
                    case WHITESPACE:
                    case NEW_LINE:
                    case LINE_COMMENT_FIXED:
                    case LINE_COMMENT_FREE:
                    case PREPROCESSOR_DIRECTIVE:
                        break;
                    default:
                        if (balance == 0) {
                            return ts.token();
                        }
                        break;
                }
            }
            return null;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ Token<FortranTokenId> lookNext(){
        if (ts.moveNext()) {
            Token<FortranTokenId> next = ts.token();
            ts.movePrevious();
            return next;
        }
        return null;
    }

    /*package local*/ Token<FortranTokenId> lookNext(int i){
        int index = ts.index();
        try {
            while(i-- > 0) {
                if (!ts.moveNext()){
                    return null;
                }
            }
            return ts.token();
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ boolean isLastLineToken(){
        int index = ts.index();
        try {
            while(true) {
                if (!ts.moveNext()){
                    return true;
                }
                DiffResult diff = diffs.getDiffs(this, 0);
                if (diff != null){
                    if (diff.replace != null){
                        if (diff.replace.hasNewLine()) {
                            return true;
                        }
                        continue;
                    }
                }
                FortranTokenId id = ts.token().id();
                switch (id){
                    case NEW_LINE:
                    case LINE_COMMENT_FIXED:
                    case LINE_COMMENT_FREE:
                        return true;
                    case WHITESPACE:
                        break;
                    default:
                        return false;
                }
            }
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ Token<FortranTokenId> lookPreviousLineImportant(){
        int index = ts.index();
        try {
            while(ts.movePrevious()){
                switch (ts.token().id()) {
                    case LINE_COMMENT_FIXED:
                    case LINE_COMMENT_FREE:
                    case PREPROCESSOR_DIRECTIVE:
                    case NEW_LINE:
                        return null;
                    case WHITESPACE:
                        break;
                    default:
                        return ts.token();
                }
            }
            return null;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ Token<FortranTokenId> lookPreviousImportant(){
        int index = ts.index();
        try {
            while(ts.movePrevious()){
                switch (ts.token().id()) {
                    case WHITESPACE:
                    case NEW_LINE:
                    case LINE_COMMENT_FIXED:
                    case LINE_COMMENT_FREE:
                    case PREPROCESSOR_DIRECTIVE:
                        break;
                    default:
                        return ts.token();
                }
            }
            return null;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ Token<FortranTokenId> lookPreviousImportant(int i){
        int index = ts.index();
        try {
            while(ts.movePrevious()){
                switch (ts.token().id()) {
                    case WHITESPACE:
                    case NEW_LINE:
                    case LINE_COMMENT_FIXED:
                    case LINE_COMMENT_FREE:
                    case PREPROCESSOR_DIRECTIVE:
                        break;
                    default:
                        i--;
                        if (i <= 0 ) {
                            return ts.token();
                        }
                }
            }
            return null;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ Token<FortranTokenId> lookPrevious(){
        if (ts.movePrevious()) {
            Token<FortranTokenId> previous = ts.token();
            ts.moveNext();
            return previous;
        }
        return null;
    }

    /*package local*/ Token<FortranTokenId> lookPrevious(int i){
        int index = ts.index();
        try {
            while(i-- > 0) {
                if (!ts.movePrevious()){
                    return null;
                }
            }
            return ts.token();
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ boolean isFirstLineToken(){
        int index = ts.index();
        try {
            while(true) {
                if (!ts.movePrevious()){
                    return true;
                }
                DiffResult diff = diffs.getDiffs(this, 0);
                if (diff != null){
                    if (diff.after != null){
                        if (diff.after.hasNewLine()) {
                            return true;
                        }
                    }
                    if (diff.replace != null){
                        if (diff.replace.hasNewLine()) {
                            return true;
                        }
                        continue;
                    }
                }
                if (ts.token().id() == NEW_LINE ||
                    ts.token().id() == PREPROCESSOR_DIRECTIVE){
                    return true;
                } else if (ts.token().id() != WHITESPACE){
                    return false;
                }
            }
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ Token<FortranTokenId> findOpenParenToken(int parenDepth) {
        int index = ts.index();
        try {
            while(true) {
                if (!ts.movePrevious()){
                    return null;
                }
                if (ts.token().id() == LPAREN){
                    parenDepth--;
                    if (parenDepth == 0){
                        return lookPreviousImportant();
                    }
                } else if (ts.token().id() == RPAREN){
                    parenDepth++;
                }
            }
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ int openParenIndent(int parenDepth) {
        return openBraceIndent(parenDepth, LPAREN, RPAREN);
    }

    private int openBraceIndent(int braceDepth, FortranTokenId open, FortranTokenId close) {
        int index = ts.index();
        try {
            while(true) {
                if (!ts.movePrevious()){
                    return -1;
                }
                if (ts.token().id() == open){
                    braceDepth--;
                    if (braceDepth == 0){
                        ts.moveNext();
                        return getTokenPosition();
                    }
                } else if (ts.token().id() == close){
                    braceDepth++;
                }
            }
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    /*package local*/ int[] getNewLinesBeforeDeclaration(int start) {
        int res[] = new int[] {-1,-1, 0};
        int index = ts.index();
        try {
            boolean hasDoc = false;
            ts.moveIndex(start);
            while(true) {
                if (!ts.movePrevious()){
                    res[0] = 0;
                    return res;
                }
                if (ts.token().id() == NEW_LINE || ts.token().id() == WHITESPACE){
                    if(res[1]==-1){
                        res[1] = ts.index();
                    }
                    res[0] = ts.index();
                } else if (ts.token().id() == LINE_COMMENT_FIXED ||
                           ts.token().id() == LINE_COMMENT_FREE){
                    if (isFirstLineToken()) {
                        if (hasDoc) {
                            // second block comment?
                            res[2] = 1;
                            return res;
                        }
                        res[0] = -1;
                        res[1] = -1;
                        hasDoc = true;
                    } else {
                        res[2] = 1;
                        return res;
                    }
                } else if (ts.token().id() == PREPROCESSOR_DIRECTIVE){
                    if (res[0] == -1) {
                        res[0] = ts.index()+1;
                        res[1] = ts.index();
                    }
                    return res;
                } else {
                    res[2] = 1;
                    return res;
                }
            }
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }


    /* Stab implementation */

    public Language<FortranTokenId> language() {
        return ts.language();
    }

    public LanguagePath languagePath() {
        return ts.languagePath();
    }

    public Token<FortranTokenId> token() {
        return ts.token();
    }

    public Token<FortranTokenId> offsetToken() {
        return ts.offsetToken();
    }

    public int offset() {
        return ts.offset();
    }

    public int index() {
        return ts.index();
    }

    public <ET extends TokenId> TokenSequence<ET> embedded(Language<ET> embeddedLanguage) {
        return ts.embedded(embeddedLanguage);
    }

    public boolean moveNext() {
        return ts.moveNext();
    }

    public boolean movePrevious() {
        return ts.movePrevious();
    }

    public int moveIndex(int index) {
        return ts.moveIndex(index);
    }

    public void moveStart() {
        ts.moveStart();
    }

    public void moveEnd() {
        ts.moveEnd();
    }

    public int move(int offset) {
        return ts.move(offset);
    }

    public boolean isEmpty() {
        return ts.isEmpty();
    }

    public int tokenCount() {
        return ts.tokenCount();
    }

    @Override
    public String toString() {
        //return ts.toString();
        return apply(diffs, this);
    }

    /*package local*/ static String apply(FortranDiffLinkedList diffs, FortranExtendedTokenSequence ts) {
        int index = ts.index();
        StringBuilder buf = new StringBuilder();
        try {
            ts.moveStart();
            while(ts.moveNext()){
                buf.append(ts.token().text());
            }
            int startOffset = 0;
            int endOffset = buf.length();
            for (Diff diff : diffs.getStorage()) {
                int start = diff.getStartOffset();
                int end = diff.getEndOffset();
                String text = diff.getText();
                if (startOffset > end || endOffset < start) {
                    System.err.println("What?" + startOffset + ":" + start + "-" + end);// NOI18N
                    continue;
                }
                if (endOffset < end) {
                    if (text != null && text.length() > 0) {
                        text = end - endOffset >= text.length() ? null : text.substring(0, text.length() - end + endOffset);
                    }
                    end = endOffset;
                }
                if (end - start > 0) {
                    buf.delete(start, end);
                }
                if (text != null && text.length() > 0) {
                    buf.insert(start, text);
                }
            }
            return buf.toString().replaceAll("\n", "\\\\n\n"); //NOI18N
            //return buf.toString();
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }
}
