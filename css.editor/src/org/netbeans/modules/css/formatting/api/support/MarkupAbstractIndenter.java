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

package org.netbeans.modules.css.formatting.api.support;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.css.formatting.api.embedding.JoinedTokenSequence;
import org.netbeans.modules.css.formatting.api.LexUtilities;
import org.netbeans.modules.editor.indent.spi.Context;

/**
 * Implementation of AbstractIndenter for tag based languages.
 *
 * @since org.netbeans.modules.css.editor/1 1.3
 */
abstract public class MarkupAbstractIndenter<T1 extends TokenId> extends AbstractIndenter<T1> {

    private Stack<MarkupItem> stack = null;
    private List<EliminatedTag> eliminatedTags;
    private boolean inOpeningTagAttributes;
    private boolean inUnformattableTagContent;
    private String unformattableTagName = null;
    private int attributesIndent;
    private int firstPreservedLineIndent = -1;

    public MarkupAbstractIndenter(Language<T1> language, Context context) {
        super(language, context);
    }

    abstract protected boolean isOpenTagNameToken(Token<T1> token);
    abstract protected boolean isCloseTagNameToken(Token<T1> token);
    /**  <   */
    abstract protected boolean isStartTagSymbol(Token<T1> token);
    /**  </   */
    abstract protected boolean isStartTagClosingSymbol(Token<T1> token);
    /**  >    */
    abstract protected boolean isEndTagSymbol(Token<T1> token);
    /**  />    */
    abstract protected boolean isEndTagClosingSymbol(Token<T1> token);

    abstract protected boolean isTagArgumentToken(Token<T1> token);

    abstract protected boolean isBlockCommentToken(Token<T1> token);

    abstract protected boolean isTagContentToken(Token<T1> token);

    abstract protected boolean isClosingTagOptional(String tagName);

    abstract protected boolean isOpeningTagOptional(String tagName);

    abstract protected Boolean isEmptyTag(String tagName);

    abstract protected boolean isTagContentUnformattable(String tagName);

    abstract protected Set<String> getTagChildren(String tagName);

    abstract protected boolean isPreservedLine(Token<T1> token, IndenterContextData<T1> context);

    abstract protected int getPreservedLineInitialIndentation(JoinedTokenSequence<T1> ts) throws BadLocationException;

    protected boolean isStableFormattingStartToken(Token<T1> token, JoinedTokenSequence<T1> ts) {
        return false;
    }

    abstract protected boolean isForeignLanguageStartToken(Token<T1> token, JoinedTokenSequence<T1> ts);

    abstract protected boolean isForeignLanguageEndToken(Token<T1> token, JoinedTokenSequence<T1> ts);

    private Stack<MarkupItem> getStack() {
        return stack;
    }

    @Override
    protected void reset() {
        stack = new Stack<MarkupItem>();
        inOpeningTagAttributes = false;
        inUnformattableTagContent = false;
        attributesIndent = 0;
        eliminatedTags = new ArrayList<EliminatedTag>();
    }

    @Override
    protected int getFormatStableStart(JoinedTokenSequence<T1> ts, int startOffset, int endOffset,
            AbstractIndenter.OffsetRanges rangesToIgnore) throws BadLocationException {

        // find open tag (with manadatory close tag) we are inside and use it
        // as formatting start; by "we are inside" is meant that all tags between
        // startOffset and endOffset lies within it - that's why we start searching
        // form endOffset here:
        ts.move(endOffset, false);

        // go backwards and find a tag in which reformatting area lies:
        while (ts.movePrevious()) {
            Token<T1> tk = ts.token();
            if (isStableFormattingStartToken(tk, ts) && ts.offset() <= startOffset) {
                break;
            }
            // if closing tag was found jump to opening one but
            // only if both opening and closing tags are mandatory - not doing
            // so can result in wrong pair matching:
            if (isCloseTagNameToken(tk) && !isClosingTagOptional(getTokenName(tk)) && !isOpeningTagOptional(getTokenName(tk))) {
                // find tag open and keep searching backwards ignoring it:
                moveToOpeningTag(ts);
                continue;
            }
            if (isOpenTagNameToken(tk) && !isClosingTagOptional(getTokenName(tk)) &&
                    ts.offset() < startOffset) {
                break;
            }
        }
        // now go backward and find opening tag on the beginning of line:
        int foundOffset = -1;
        do {
            Token tk = ts.token();
            if (tk == null) {
                break;
            }

            if (isStartTagSymbol(tk) || isStableFormattingStartToken(tk, ts)) {
                    int firstNonWhite = Utilities.getRowFirstNonWhite(getDocument(), ts.offset());
                    if (firstNonWhite != -1 && firstNonWhite == ts.offset()) {
                        foundOffset = ts.offset();
                        break;
                    }
            }
        } while (ts.movePrevious());
        if (foundOffset == -1) {
            foundOffset = LexUtilities.getTokenSequenceStartOffset(ts);
        }
        eliminateUnnecessaryTags(ts, startOffset, foundOffset, rangesToIgnore);
        return foundOffset;
    }

    private void eliminateUnnecessaryTags(JoinedTokenSequence<T1> ts, int from, int to, AbstractIndenter.OffsetRanges rangesToIgnore) {
        ts.move(from, false);

        // go backwards and find any closed tags with in given range and eliminate them:
        while (ts.movePrevious()) {
            Token<T1> tk = ts.token();
            String tag;
            // if closing tag was found jump to opening one but
            // only if both opening and closing tags are mandatory - not doing
            // so can result in wrong pair matching:
            if (isCloseTagNameToken(tk) && !isClosingTagOptional(getTokenName(tk)) && !isOpeningTagOptional(getTokenName(tk))) {
                tag = tk.text().toString();
                int rangeEnd;
                // if document is being editted end tag symbol might be accidentally missing:
                if (ts.moveNext() && isEndTagSymbol(ts.token())) {
                    // add ">":
                    assert isEndTagSymbol(ts.token()) : "token="+ts.token()+" ts="+ts;
                    rangeEnd = ts.offset()+getTokenName(ts.token()).length();
                    ts.movePrevious();
                } else {
                    rangeEnd = ts.offset()+getTokenName(ts.token()).length();
                }
                // find tag open and keep searching backwards ignoring it:
                if (moveToOpeningTag(ts)) {
                    assert ts.token().text().toString().equals(tag) : "tag="+tag+" token="+ts.token();
                    int rangeStart;
                    // if document is being editted end tag symbol might be accidentally missing:
                    if (ts.movePrevious() && isStartTagSymbol(ts.token())) {
                        // add "<"
                        assert isStartTagSymbol(ts.token()) : "token="+ts.token()+" ts="+ts;
                        rangeStart = ts.offset();
                    } else {
                        rangeStart = ts.offset();
                    }
                    if (rangeStart < rangeEnd) {
                        rangesToIgnore.add(rangeStart, rangeEnd);
                        eliminatedTags.add(new EliminatedTag(rangeStart, rangeEnd, tag));
                    }
                }
            }
        }

    }

    private final MarkupItem createMarkupItem(Token<T1> token, boolean openingTag, int indentation) {
        String tagName = getTokenName(token);
        if (openingTag) {
            boolean optionalEnd = isClosingTagOptional(getTokenName(token));
            Set<String> children = null;
            Boolean empty = isEmptyTag(tagName);
            if (optionalEnd && empty != null && !empty.booleanValue()) {
                children = getTagChildren(tagName);
            }
            return new MarkupItem(tagName, true, indentation, optionalEnd, children, empty != null ? empty.booleanValue() : false, false, false);
        } else {
            return new MarkupItem(tagName, false, indentation, false, null, false, false, false);
        }

    }

    private static MarkupItem createVirtualMarkupItem(String tagName, boolean empty) {
        return new MarkupItem(tagName, false, -1, false, null, empty, true, false);
    }

    private static MarkupItem createEliminatedMarkupItem(String tagName, boolean openingTag) {
        return new MarkupItem(tagName, openingTag, -1, false, null, false, false, true);
    }

    private boolean moveToOpeningTag(JoinedTokenSequence<T1> tokenSequence) {
        int originalIndex = tokenSequence.index();

        String searchedTagName = getTokenName(tokenSequence.token());
        int balance = 0;

        while (tokenSequence.movePrevious()) {
            Token tk = tokenSequence.token();
            if (!isOpenTagNameToken(tk) && !isCloseTagNameToken(tk)) {
                continue;
            }
            if (searchedTagName.equalsIgnoreCase(getTokenName(tk))) {
                if (isOpenTagNameToken(tk)) {
                    if (balance == 0) {
                        return true;
                    }
                    balance--;
                } else if (isCloseTagNameToken(tk)) {
                    balance++;
                }
            }
        }

        tokenSequence.moveIndex(originalIndex);
        tokenSequence.movePrevious();
        return false;
    }

    private void getIndentFromState(List<IndentCommand> iis, boolean updateState, int lineStartOffset) {
        Stack<MarkupItem> fileStack = getStack();

        // find index of last stack item which was not processed:
        int lastUnprocessedItem = fileStack.size();
        for (int i = fileStack.size()-1; i>=0; i--) {
            if (!fileStack.get(i).processed) {
                lastUnprocessedItem = i;
            } else {
                break;
            }

        }
        // iterate over stack state and generated indent command for current line:
        MarkupItem prevItem = null;
        for (int i=lastUnprocessedItem; i< fileStack.size(); i++) {
            MarkupItem item = fileStack.get(i);
            assert !item.processed : item;
            if (i+1 == fileStack.size() && inOpeningTagAttributes) {
                // if we are in tag attributes then last stack item must
                // be opening tag which has to be ignored for now, eg.:
                //
                // 01: <sometag a=b
                // 02:          x=y>
                //
                // when line 1 is processed there will be MarkupItem for sometag
                // but INDENT command for such MarkupItem should be processed
                // after inOpeningTagAttributes is false, that is on line 2.
                assert item.openingTag;
                break;
            }
            if (!item.empty) {
                // eliminate opening and closing sequence on one line:
                IndentCommand ic = new IndentCommand(item.openingTag ? IndentCommand.Type.INDENT : IndentCommand.Type.RETURN,
                    lineStartOffset);
                addIndentationCommand(iis, ic, item, prevItem);
            }
            if (updateState) {
                item.processed = true;
            }
            prevItem = item;
        }
        if (inOpeningTagAttributes) {
            IndentCommand ii = new IndentCommand(IndentCommand.Type.CONTINUE, lineStartOffset);
            if (getAttributesIndent() != -1) {
                ii.setFixedIndentSize(getAttributesIndent());
            }
            iis.add(ii);
        }
        if (updateState) {
            removeFullyProcessedTags();
        }
    }

    private void addIndentationCommand(List<IndentCommand> iis, IndentCommand ic, MarkupItem item, MarkupItem prevItem) {
        if (ic.getType() == IndentCommand.Type.RETURN && iis.size() > 0 && prevItem != null) {
            IndentCommand prev = iis.get(iis.size()-1);
            if (prev.getType() == IndentCommand.Type.INDENT &&
                    prevItem.tagName.equals(item.tagName) &&
                    prevItem.openingTag && !item.openingTag) {
                // tag was opened and closed on the same line and therfore do
                // not generate INDENT and RETURN commands for them;
                iis.remove(iis.size()-1);
                return;
            }
        }
        iis.add(ic);
    }

    @Override
    protected List<IndentCommand> getLineIndent(IndenterContextData<T1> context, List<IndentCommand> preliminaryNextLineIndent)
            throws BadLocationException {

        // check if there are any tags which were eliminated prior to this line 
        // and use them to close any tags with optional end tag:
        processEliminatedTags(context.getLineStartOffset());

        Stack<MarkupItem> fileStack = getStack();
        List<IndentCommand> iis = new ArrayList<IndentCommand>();
        getIndentFromState(iis, true, context.getLineStartOffset());

        JoinedTokenSequence<T1> ts = context.getJoinedTokenSequences();
        ts.move(context.getLineStartOffset());

        List<MarkupItem> lineItems = new ArrayList<MarkupItem>();

        String lastOpenTagName = null;

        boolean unformattableTagContent = isInUnformattableTagContent();

        while (!context.isBlankLine() && ts.moveNext() &&
            ((ts.isCurrentTokenSequenceVirtual() && ts.offset() < context.getLineEndOffset()) ||
                    ts.offset() <= context.getLineEndOffset()) ) {

            Token<T1> token = (Token<T1>)ts.token();
            if (token == null || ts.embedded() != null) {
                continue;
            }

            if (isOpenTagNameToken(token)) {
                lineItems.add(createMarkupItem(token, true, getIndentationSize()));
                setInOpeningTagAttributes(true);
                lastOpenTagName = getTokenName(token);
            } else if (isTagArgumentToken(token) && getAttributesIndent() == -1) {
                int index = ts.index();
                int offset = ts.offset();
                ts.movePrevious();
                Token<T1> tk = findPreviousNonWhiteSpaceToken(ts);
                if (isOpenTagNameToken(tk)) {
                    setAttributesIndent(offset - context.getLineNonWhiteStartOffset());
                }
                ts.moveIndex(index);
                ts.moveNext();
            } else if (isCloseTagNameToken(token)) {
                lineItems.add(createMarkupItem(token, false, getIndentationSize()));
                String tokenName = getTokenName(token);
                // unformattable tags can be nested (eg. textarea within pre) so
                // make sure we close unformattable section only by corresponing tag:
                if (isTagContentUnformattable(tokenName) && tokenName.equals(unformattableTagName)) {
                    setInUnformattableTagContent(false);
                    if (unformattableTagContent) {
                        // if line starts with for example "</pre>" (that is there is no
                        // other text before closing token) then we can indent that line:
                        if (context.getLineStartOffset()+2 == ts.offset()) {
                            unformattableTagContent = false;
                        }
                    }
                }
                lastOpenTagName = null;
            } else if (isEndTagSymbol(token) || isEndTagClosingSymbol(token)) {
                if (isInOpeningTagAttributes()) {
                    setInOpeningTagAttributes(false);
                }
                if (isEndTagClosingSymbol(token)) {
                    MarkupItem item = null;
                    if (lineItems.size() > 0) {
                        item = lineItems.get(lineItems.size()-1);
                    } else if (fileStack.size() > 0) {
                        item = fileStack.peek();
                    }
                    if (item != null) {
                        lineItems.add(createVirtualMarkupItem(item.tagName, item.empty));
                    } else {
                        assert false : "token:"+token+" ts="+ts;
                    }
                } else {
                    // now we are within tag's content; check if it is unformattable
                    if (lastOpenTagName != null && !isInUnformattableTagContent() &&
                            isTagContentUnformattable(lastOpenTagName)) {
                        setInUnformattableTagContent(true, lastOpenTagName);
                    }
                }
            }
            if (isPreservedLine(token, context)) {
                if (firstPreservedLineIndent == -1) {
                    firstPreservedLineIndent = getPreservedLineInitialIndentation(ts);
                }
                IndentCommand ic = new IndentCommand(IndentCommand.Type.PRESERVE_INDENTATION, context.getLineStartOffset());
                ic.setFixedIndentSize(firstPreservedLineIndent);
                iis.add(ic);
            } else {
                firstPreservedLineIndent = -1;
            }
            if (isForeignLanguageStartToken(token, ts)) {
                iis.add(new IndentCommand(IndentCommand.Type.BLOCK_START, context.getLineStartOffset()));
            } else if (isForeignLanguageEndToken(token, ts)) {
                iis.add(new IndentCommand(IndentCommand.Type.BLOCK_END, context.getLineStartOffset()));
            }
        }

        // are we within a content of a tag which is not formattable:
        if (unformattableTagContent) {
            // ignore this line
            iis.add(new IndentCommand(IndentCommand.Type.DO_NOT_INDENT_THIS_LINE, context.getLineStartOffset()));
        }

        int index = fileStack.size();
        addTags(lineItems);

        // if first item on line is closing/opening tag then test whether line needs back-indent:
        if (!context.isBlankLine()) {
            ts.move(context.getLineNonWhiteStartOffset());
            if (ts.moveNext()) {
                if (isStartTagSymbol(ts.token()) || isStartTagClosingSymbol(ts.token())) {
                    boolean closingTag = isStartTagClosingSymbol(ts.token());
                    if (ts.moveNext()) {
                        String tokenName = getTokenName(ts.token());
                        List<IndentCommand> iis2 = new ArrayList<IndentCommand>();
                        // there can be multiple virtual closing tags before 'tokenName' one:
                        for (int i=index; i< fileStack.size(); i++) {
                            MarkupItem item = fileStack.get(i);
                            if (item.empty) {
                                continue;
                            }
                            assert !item.processed : item;
                            if (item.virtual) {
                                assert !item.openingTag : "only closing tag item is expected: "+item;
                                iis.add(new IndentCommand(IndentCommand.Type.RETURN,
                                    context.getLineStartOffset()));
                                item.processed = true;
                            } else {
                                if (closingTag) {
                                    assert item.tagName.equalsIgnoreCase(tokenName) : "was expecting tag "+tokenName+" but was "+item+ ": "+fileStack+" index="+index;
                                    iis.add(new IndentCommand(IndentCommand.Type.RETURN,
                                        context.getLineStartOffset()));
                                    item.processed = true;
                                }
                                break;
                            }
                        }
                        if (iis2.size() > 0) {
                            iis.addAll(iis2);
                        }
                    }
                }
            }
        }

        if (iis.isEmpty()) {
            iis.add(new IndentCommand(IndentCommand.Type.NO_CHANGE, context.getLineStartOffset()));
        }

        if (context.getNextLineStartOffset() != -1) {
            getIndentFromState(preliminaryNextLineIndent, false, context.getNextLineStartOffset());
            if (preliminaryNextLineIndent.size() == 0) {
                preliminaryNextLineIndent.add(new IndentCommand(IndentCommand.Type.NO_CHANGE, context.getNextLineStartOffset()));
            }
        }

        return iis;
    }

    private void processEliminatedTags(int lineStartOffset) {
        List<MarkupItem> items = new ArrayList<MarkupItem>();
        generateVirtualMarkupItemsForEliminatedTags(items, lineStartOffset);
        if (items.size() > 0) {
            addTags(items);
        }
    }

    private void generateVirtualMarkupItemsForEliminatedTags(List<MarkupItem> lineItems, int lineStartOffset) {
        Iterator<EliminatedTag> it = eliminatedTags.iterator();
        while (it.hasNext()) {
            EliminatedTag et = it.next();
            if (et.end <= lineStartOffset) {
                // it is enough to add opening tag:
                lineItems.add(createEliminatedMarkupItem(et.tag, true));
                it.remove();
            } else {
                break;
            }
        }
    }

    private String getTokenName(Token<T1> token) {
        //
        // trim() is here intentionally to get rid of new line character
        // from tag name in case of JSP like:
        //
        // 01: <jsp:useBean
        // 02:     scope="application">
        //
        // at the moment tag is: T[ 1]: "jsp:useBean\n" <1,13> TAG[3] DefT, st=5, IHC=19351667
        //
        return token.text().toString().trim();
    }

    private Token<T1> findPreviousNonWhiteSpaceToken(JoinedTokenSequence<T1> ts) {
        while (isWhiteSpaceToken(ts.token()) && ts.movePrevious()) {}
        return ts.token();
    }

    private static class MarkupItem {
        public String tagName;
        public boolean openingTag;
        public int indentLevel;
        public boolean processed;
        public boolean optionalClosingTag;
        public Set<String> children;
        public boolean virtual;
        public boolean empty;
        public boolean eliminated;

        public MarkupItem(String tagName, boolean openingTag, int indentLevel,
                boolean optionalClosingTag, Set<String> children, boolean empty, boolean virtual, boolean eliminated) {
            this.tagName = tagName;
            this.openingTag = openingTag;
            this.indentLevel = indentLevel;
            this.optionalClosingTag = optionalClosingTag;
            this.processed = false;
            this.children = children;
            this.empty = empty;
            this.virtual = virtual;
            this.eliminated = eliminated;
        }

        @Override
        public String toString() {
            return "HtmlStackItem[" +
                    (openingTag ? "<" : "</") +
                    "tagName="+tagName+"," +
                    "indent="+indentLevel+"," +
                    "optionalClosingTag="+optionalClosingTag+"," +
                    "processed="+processed+"," +
                    //"children="+children+"," +
                    "virtual="+virtual+"," +
                    "empty="+empty+"]";
        }

    }


    private void addTags(List<MarkupItem> lineItems) {

//  for now disable this: it does not work in case when opened and closed tag
//  closes a previous tag with option end; in that scenario if opened and closed
//  tags are ignored we are missing knowledge that option end should have been generated
//
//        // if a tag was opened and closed within one line then it can be ignored:
//        lineItems = eliminateTagsOpenedAndClosedOnOneLine(lineItems);

        for (MarkupItem newItem : lineItems) {
            if (!newItem.virtual) {
                if (newItem.openingTag) {
                    getStack().addAll(calculateAllVirtualCloseTagsForOpenTag(newItem));
                } else {
                    getStack().addAll(calculateAllVirtualCloseTagsForCloseTag(newItem));
                }
            }
            if (!newItem.eliminated) {
                getStack().push(newItem);
            }
        }
    }

    private List<MarkupItem> eliminateTagsOpenedAndClosedOnOneLine(List<MarkupItem> lineItems) {
        List<MarkupItem> newItems = new ArrayList<MarkupItem>();
        for (int i=lineItems.size()-1; i>=0; i--) {
            MarkupItem item = lineItems.get(i);
            // found a closing tag -> jump before corresponding open tag if there is one:
            if (!item.openingTag) {
                int index = indexOfOpenTag(lineItems, item, i);
                if (index != -1) {
                    i = index;
                    continue;
                }
            }
            newItems.add(0, item);
        }
        return newItems;
    }

    private List<MarkupItem> calculateAllVirtualCloseTagsForOpenTag(MarkupItem newItem) {
            // iterate backwards over existing state items and:
            // if tag is closing tag then jump before its opening tag and continue
            // if tag is open tag then:
            //   if this tag has mandatory close tag then everything is OK - break;
            //   else check if newItem.tagName is acceptable child?
            //     if it is then everything is OK - break;
            //     else add virtual close tag and continue going backwards
        List<MarkupItem> newItems = new ArrayList<MarkupItem>();
        LOOP: for (int i=getStack().size()-1; i>=0; i--) {
            MarkupItem item = getStack().get(i);
            // found a closing tag -> jump before corresponding open tag
            if (!item.openingTag) {
                int index = indexOfOpenTag(getStack(), item, i);
                if (index != -1) {
                    i = index;
                } else if (DEBUG) {
                    System.err.println("WARNING: cannot find open tag for "+item+" before index "+i+": "+(getStack().size() < 30 ? getStack() : "[too many items]"));
                }
                continue;
            } else {
                if (!item.optionalClosingTag) {
                    // everything is OK;
                    break;
                } else if (item.children != null) {
                    if (item.children.contains(newItem.tagName.toUpperCase())) {
                        // everything is OK;
                        break;
                    } else {
                        // We found open tag which has optional closing tag and open
                        // tag we are evaluating (newItem) is not legel child of it.
                        // That means that open tag with optional closing tag was actually
                        // closed. BUT that's not true in case when tag's children has
                        // optional start tag. For example in HTML snippet:
                        //
                        //   <html>
                        //   <table>
                        //
                        // when <table> is indented we find that <html> has optional end tag and
                        // it can have two children: <head> and <body>. Because <table> is not a child
                        // of <html> perhaps <html> was closed? Not really because both children has optional
                        // opening tag and in such a case just do nothing and do not try to close <html>.
                        for (String s : item.children) {
                            if (isOpeningTagOptional(s)) {
                                // one of the children of 'item' has optional start which means
                                // we cannot assume that tag 'item' should be closed.

                                // everything is OK;
                                break LOOP;
                            }
                        }
                        newItems.add(MarkupAbstractIndenter.createVirtualMarkupItem(item.tagName, item.empty));
                    }
                } else {
                    newItems.add(MarkupAbstractIndenter.createVirtualMarkupItem(item.tagName, item.empty));
                }
            }
        }
        return newItems;
    }

    private List<MarkupItem> calculateAllVirtualCloseTagsForCloseTag(MarkupItem newItem) {
        // iterate backwards over existing state items and:
        // if tag is closing tag then jump before its opening tag and continue
        // if tag is open tag then:
        //   if it matches tag being closed then everything is OK - break;
        //   else if tag has optional close tag then CLOSE it with virtual tag and continue going backwards.
        //   else if tag does not match and is not mandatory: something wrong so nothing break;
        int lastFailureSize = -1;
        List<MarkupItem> newItems = new ArrayList<MarkupItem>();
        for (int i=getStack().size()-1; i>=0; i--) {
            MarkupItem item = getStack().get(i);
            // found a closing tag -> jump before corresponding open tag
            if (!item.openingTag) {
                int index = indexOfOpenTag(getStack(), item, i);
                if (index != -1) {
                    i = index;
                } else if (AbstractIndenter.DEBUG) {
                    System.err.println("WARNING: cannot find open tag for "+item+" before index "+i+": "+(getStack().size() < 30 ? getStack() : "[too many items]"));
                }
                continue;
            } else {
                if (item.tagName.equalsIgnoreCase(newItem.tagName)) {
                    lastFailureSize = -1;
                    // nothing to do:
                    break;
                } else if (item.optionalClosingTag) {
                    newItems.add(MarkupAbstractIndenter.createVirtualMarkupItem(item.tagName, item.empty));
                } else {
                    if (lastFailureSize == -1) {
                        // recovery attempt: ignore this tag and keep looking backwards
                        lastFailureSize = newItems.size();
                        newItems.add(MarkupAbstractIndenter.createVirtualMarkupItem(item.tagName, item.empty));
                        continue;
                    } else {
                        // recovery failed; rollback to where we were before recovery and exit
                        if (AbstractIndenter.DEBUG) {
                            System.err.println("WARNING: cannot find opening tag for "+newItem+": "+getStack()+" stopped searching at "+item);
                        }
                        break;
                    }

                }
            }
        }
        if (lastFailureSize != -1) {
            while (newItems.size() > lastFailureSize) {
                newItems.remove(newItems.size()-1);
            }
        }
        return newItems;
    }

    private static int indexOfOpenTag(List<MarkupItem> list, MarkupItem closeTag, int i) {
        assert !closeTag.openingTag : closeTag;
        int balance = 0;
        for (int index=i-1; index >= 0; index--) {
            MarkupItem item = list.get(index);
            if (item.tagName.equalsIgnoreCase(closeTag.tagName)) {
                if (item.openingTag) {
                    if (balance == 0) {
                        return index;
                    } else {
                        balance--;
                    }
                } else {
                    balance++;
                }
            }
        }
        return -1;
    }

    private void removeFullyProcessedTags() {
        // XXX: TODO: impl this
    }

    private boolean isInOpeningTagAttributes() {
        return inOpeningTagAttributes;
    }

    private void setInOpeningTagAttributes(boolean inOpeningTagAttributes) {
        this.inOpeningTagAttributes = inOpeningTagAttributes;
        attributesIndent = -1;
    }

    private int getAttributesIndent() {
        return attributesIndent;
    }

    private void setAttributesIndent(int attributesIndent) {
        this.attributesIndent = attributesIndent;
    }

    private boolean isInUnformattableTagContent() {
        return inUnformattableTagContent;
    }

    private void setInUnformattableTagContent(boolean inUnformattableTagContent, String unformattableTagName) {
        this.inUnformattableTagContent = inUnformattableTagContent;
        this.unformattableTagName = unformattableTagName;
    }

    private void setInUnformattableTagContent(boolean inUnformattableTagContent) {
        this.inUnformattableTagContent = inUnformattableTagContent;
        this.unformattableTagName = null;
    }

    private static class EliminatedTag {
        private int start;
        private int end;
        private String tag;

        public EliminatedTag(int start, int end, String tag) {
            this.start = start;
            this.end = end;
            this.tag = tag;
        }

    }
}
