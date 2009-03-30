
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
package org.netbeans.editor.ext.html;

import javax.swing.text.Document;
import java.util.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.html.lexer.HtmlTokenId;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.dtd.DTD.Element;
import org.netbeans.editor.ext.html.dtd.InvalidateEvent;
import org.netbeans.editor.ext.html.dtd.InvalidateListener;
import org.netbeans.spi.editor.completion.CompletionItem;


/**
 * Support methods for HTML document syntax analyzes
 *
 * @author Petr Nejedly
 * @author Marek Fukala
 */
public class HtmlSyntaxSupport extends ExtSyntaxSupport implements InvalidateListener {
    private static final String FALLBACK_DOCTYPE =
            "-//W3C//DTD HTML 4.01 Transitional//EN";  // NOI18N
    
    private DTD dtd;
    private String docType;
    
    public static synchronized HtmlSyntaxSupport get(Document doc) {
        HtmlSyntaxSupport sup = (HtmlSyntaxSupport)doc.getProperty(HtmlSyntaxSupport.class);
        if(sup == null) {
            sup = new HtmlSyntaxSupport((BaseDocument)doc);
            doc.putProperty(HtmlSyntaxSupport.class, sup);
        }
        return sup;
    }
    
    /** Creates new HtmlSyntaxSupport */
    private HtmlSyntaxSupport( BaseDocument doc ) {
        super(doc);
    }
    
    /** Reset our cached DTD if no longer valid.
     */
    public void dtdInvalidated(InvalidateEvent evt) {
        if( dtd != null && evt.isInvalidatedIdentifier( docType ) ) {
            dtd = null;
        }
    }
    
    public DTD getDTD() {
        String type = getDocType();
        if( type == null ) type = FALLBACK_DOCTYPE;
        
        if( dtd != null && type == docType ) return dtd;
        
        docType = type;
        dtd = org.netbeans.editor.ext.html.dtd.Registry.getDTD( docType, null );
        
        if(dtd == null) {
            //use default for unknown doctypes
            dtd = org.netbeans.editor.ext.html.dtd.Registry.getDTD( FALLBACK_DOCTYPE, null );
        }
        return dtd;
    }
    
    String getDocType() {
        try {
            SyntaxElement elem = getElementChain( 0 );
            
            if( elem == null ) return null; // empty document
            
            int type = elem.getType();
            
            while( type != SyntaxElement.TYPE_DECLARATION
                    && type != SyntaxElement.TYPE_TAG ) {
                elem = elem.getNext();
                if( elem == null ) break;
                type = elem.getType();
            }
            
            if( type == SyntaxElement.TYPE_DECLARATION )
                return ((SyntaxElement.Declaration)elem).getPublicIdentifier();
            
            return null;
        } catch( BadLocationException e ) {
            return null;
        }
    }
    
    public static boolean checkOpenCompletion(Document document, int dotPos, String typedText) {
        BaseDocument doc = (BaseDocument)document;
        switch( typedText.charAt( typedText.length()-1 ) ) {
            case '/':
                if (dotPos >= 2) { // last char before inserted slash
                    try {
                        String txtBeforeSpace = doc.getText(dotPos-2, 2);
                        if( txtBeforeSpace.equals("</") )  // NOI18N
                            return true;
                    } catch (BadLocationException e) {}
                }
                break;
            case ' ':
                doc.readLock();
                try {
                    TokenSequence ts = getJoinedHtmlSequence(doc);
                    if(ts == null) {
                        //no suitable token sequence found
                        return false;
                    }

                    ts.move(dotPos-1);
                    if(ts.moveNext() || ts.movePrevious()) {
                        if(ts.token().id() == HtmlTokenId.WS) {
                            return true;
                        }
                    }
                }finally {
                    doc.readUnlock();
                }
                break;
            case '<':
            case '&':
                return true;
//            case ';':
//                return COMPLETION_HIDE;

        }
        return false;

    }
    
    /** The way how to get previous SyntaxElement in document. It is not intended
     * for direct usage, and thus is not public. Usually, it is called from
     * SyntaxElement's method getPrevious()
     */
    SyntaxElement getPreviousElement( int offset ) throws BadLocationException {
        return offset == 0 ? null : getElementChain( offset - 1 );
    }
      
    /** Returns SyntaxElement instance for block of tokens, which is either
     * surrounding given offset, or is just after the offset.
     *
     * @param offset offset in document where to search for SyntaxElement
     * @return SyntaxElement surrounding or laying after the offset
     * or <CODE>null</CODE> if there is no element there (end of document)
     */
    SyntaxElement getElementChain( int offset ) throws BadLocationException {
        getDocument().readLock();
        try {
            TokenHierarchy hi = TokenHierarchy.get(getDocument());
            TokenSequence<HtmlTokenId> ts = getJoinedHtmlSequence(getDocument());
            
            if(ts == null) {
                return  null;
            }
            //html token found
            ts.move(offset);
            if(!ts.moveNext() && !ts.movePrevious()) return null; //no token found
            
            Token item = ts.token();
            
            int beginning = ts.offset();
            
            if(beginning > offset) {
                //the offset is not in html content, the next token begins after the offset
                return null;
            }
            
            if( item.id() == HtmlTokenId.CHARACTER ) {
                do {
                    item = ts.token();
                    beginning = ts.offset();
                } while(item.id() == HtmlTokenId.CHARACTER && ts.movePrevious());
                
                // now item is either HTMLSyntax.VALUE or we're in text, or at BOF
                if( item.id() != HtmlTokenId.VALUE && item.id() != HtmlTokenId.TEXT ) {
                    return getNextElement(  beginning );
                } // else ( for VALUE or TEXT ) fall through
            }
            
            if( item.id() == HtmlTokenId.WS || item.id() == HtmlTokenId.ARGUMENT ||     // these are possible only in Tags
                    item.id() == HtmlTokenId.OPERATOR || item.id() == HtmlTokenId.VALUE ) { // so find boundary
                //find beginning of the tag
                while(ts.movePrevious() && !isTagSymbol(item = ts.token())) {
                    //just skip tokens
                };
                return getNextElement(  item.offset(hi) );       // TAGC
            }
            
            if( item.id() == HtmlTokenId.TEXT ) {
                do {
                    beginning = ts.offset();
                } while ( ts.movePrevious() && (ts.token().id() == HtmlTokenId.TEXT || ts.token().id() == HtmlTokenId.CHARACTER));
                
                return getNextElement(  beginning ); // from start of Commment
            }
            
            if( item.id() == HtmlTokenId.SCRIPT) {
                //we have just one big token for script
                return getNextElement(  ts.token().offset(hi));
            }
            
            if( item.id() == HtmlTokenId.STYLE) {
                //we have just one big token for script
                return getNextElement(  ts.token().offset(hi));
            }
            
            
            if( isTag(item)) {
                if( item.id() == HtmlTokenId.TAG_OPEN ||
                        item.id() == HtmlTokenId.TAG_OPEN_SYMBOL)  return getNextElement(  item.offset(hi) );  // TAGO/ETAGO // NOI18N
                else {
                    do {
                        if(!ts.movePrevious()) {
                            return getNextElement( item.offset(hi));
                        }
                        item = ts.token();
                    } while( item.id() != HtmlTokenId.TAG_OPEN_SYMBOL);
                    
                    return getNextElement(  item.offset(hi) );       // TAGC
                }
            }
            
            if( item.id() == HtmlTokenId.ERROR )
                return new SyntaxElement( this, item.offset(hi), getTokenEnd( hi, item ), SyntaxElement.TYPE_ERROR );
            
            if( item.id() == HtmlTokenId.BLOCK_COMMENT ) {
                while( item.id() == HtmlTokenId.BLOCK_COMMENT && !item.text().toString().startsWith( "<!--" ) && ts.movePrevious()) { // NOI18N
                    item = ts.token();
                }
                return getNextElement(  item.offset(hi)); // from start of Commment
            }
            
            if( item.id() == HtmlTokenId.DECLARATION || item.id() == HtmlTokenId.SGML_COMMENT ) {
                while((item.id() != HtmlTokenId.DECLARATION || !item.text().toString().startsWith( "<!" )) && ts.movePrevious()) { // NOI18N
                    item = ts.token();
                }
                return getNextElement(  item.offset(hi) ); // from start of Commment
            }
        } finally {
            getDocument().readUnlock();
        }
        return null;
    }
    
    
    SyntaxElement getNextElement(int offset) throws javax.swing.text.BadLocationException {
        getDocument().readLock();
        try {
            TokenHierarchy hi = TokenHierarchy.get(getDocument());
            TokenSequence ts = getJoinedHtmlSequence(getDocument());
            if(ts == null) {
                return  null;
            }
            
            ts.move(offset);
            if (!ts.moveNext())
                return null;
            org.netbeans.api.lexer.Token item = ts.token();
            int lastOffset = getTokenEnd(hi, item);
            
            if (item.id() == org.netbeans.api.html.lexer.HtmlTokenId.BLOCK_COMMENT) {
                do {
                    lastOffset = getTokenEnd(hi, ts.token());
                } while (ts.token().id() ==
                        org.netbeans.api.html.lexer.HtmlTokenId.BLOCK_COMMENT &&
                        ts.moveNext());
                return new SyntaxElement(this, offset, lastOffset,
                        SyntaxElement.TYPE_COMMENT);
            }
            if (item.id() == org.netbeans.api.html.lexer.HtmlTokenId.DECLARATION) {
                java.lang.StringBuffer sb = new java.lang.StringBuffer(item.text());
                
                while (item.id() == HtmlTokenId.DECLARATION ||
                       item.id() == HtmlTokenId.SGML_COMMENT ||
                       item.id() == HtmlTokenId.WS) {
                    lastOffset = getTokenEnd(hi, item);
                    if (!ts.moveNext()) {
                        break;
                    }
                    item = ts.token();
                    if (item.id() == HtmlTokenId.DECLARATION || item.id() == HtmlTokenId.WS) {
                        sb.append(item.text().toString());
                    }
                }
                java.lang.String image = sb.toString();
                
                if (!image.startsWith("<!DOCTYPE"))
                    return new org.netbeans.editor.ext.html.SyntaxElement.Declaration(this,
                            offset,
                            lastOffset,
                            null,
                            null,
                            null);
                image = image.substring(9).trim();
                int index = image.indexOf(' ');
                
                if (index < 0)
                    return new org.netbeans.editor.ext.html.SyntaxElement.Declaration(this,
                            offset,
                            lastOffset,
                            null,
                            null,
                            null);
                java.lang.String rootElem = image.substring(0, index);
                
                image = image.substring(index).trim();
                if (image.startsWith("PUBLIC")) {
                    image = image.substring(6).trim();
                    sb = new java.lang.StringBuffer(image);
                    java.lang.String pi = getQuotedString(sb);
                    
                    if (pi != null) {
                        java.lang.String si = getQuotedString(sb);
                        
                        return new org.netbeans.editor.ext.html.SyntaxElement.Declaration(this,
                                offset,
                                lastOffset,
                                rootElem,
                                pi,
                                si);
                    }
                } else if (image.startsWith("SYSTEM")) {
                    image = image.substring(6).trim();
                    sb = new java.lang.StringBuffer(image);
                    java.lang.String si = getQuotedString(sb);
                    
                    if (si != null) {
                        return new org.netbeans.editor.ext.html.SyntaxElement.Declaration(this,
                                offset,
                                lastOffset,
                                rootElem,
                                null,
                                si);
                    }
                }
                return new org.netbeans.editor.ext.html.SyntaxElement.Declaration(this,
                        offset,
                        lastOffset,
                        null,
                        null,
                        null);
            }
            if (item.id() == org.netbeans.api.html.lexer.HtmlTokenId.ERROR)
                return new SyntaxElement(this, item.offset(hi), lastOffset,
                        SyntaxElement.TYPE_ERROR);
            if (item.id() == org.netbeans.api.html.lexer.HtmlTokenId.TEXT ||
                    item.id() == org.netbeans.api.html.lexer.HtmlTokenId.CHARACTER) {
                do {
                    lastOffset = getTokenEnd(hi, item);
                    item = ts.token();
                } while (ts.moveNext() &&
                        (item.id() == org.netbeans.api.html.lexer.HtmlTokenId.TEXT ||
                        item.id() ==
                        org.netbeans.api.html.lexer.HtmlTokenId.CHARACTER));
                return new SyntaxElement(this, offset, lastOffset,
                        SyntaxElement.TYPE_TEXT);
            }
            if (item.id() == org.netbeans.api.html.lexer.HtmlTokenId.SCRIPT) {
                return new SyntaxElement(this, offset, getTokenEnd(hi, item),
                        SyntaxElement.TYPE_SCRIPT);
            }
            if (item.id() == org.netbeans.api.html.lexer.HtmlTokenId.STYLE) {
                return new SyntaxElement(this, offset, getTokenEnd(hi, item),
                        SyntaxElement.TYPE_STYLE);
            }
            if (item.id() == org.netbeans.api.html.lexer.HtmlTokenId.TAG_CLOSE || (item.id() ==
                    org.netbeans.api.html.lexer.HtmlTokenId.TAG_OPEN_SYMBOL &&
                    item.text().toString().equals("</"))) {
                java.lang.String name = item.text().toString();
                
                if (item.id() ==
                        org.netbeans.api.html.lexer.HtmlTokenId.TAG_OPEN_SYMBOL) {
                    ts.moveNext();
                    name = ts.token().text().toString();
                }
                ts.moveNext();
                item = ts.token();
                do {
                    item = ts.token();
                    lastOffset = getTokenEnd(hi, item);
                } while (item.id() == org.netbeans.api.html.lexer.HtmlTokenId.WS &&
                        ts.moveNext());
                if (item.id() ==
                        org.netbeans.api.html.lexer.HtmlTokenId.TAG_CLOSE_SYMBOL) {
                    return new org.netbeans.editor.ext.html.SyntaxElement.Named(this,
                            offset,
                            getTokenEnd(hi,
                            item),
                            SyntaxElement.TYPE_ENDTAG,
                            name);
                } else {
                    return new org.netbeans.editor.ext.html.SyntaxElement.Named(this,
                            offset,
                            lastOffset,
                            SyntaxElement.TYPE_ENDTAG,
                            name);
                }
            }
            if (item.id() == org.netbeans.api.html.lexer.HtmlTokenId.TAG_OPEN ||
                    (item.id() == org.netbeans.api.html.lexer.HtmlTokenId.TAG_OPEN_SYMBOL &&
                    !item.text().toString().equals("</"))) {
                java.lang.String name = item.text().toString();
                ArrayList<SyntaxElement.TagAttribute> attrs = new ArrayList<SyntaxElement.TagAttribute>();
                
                if (item.id() == org.netbeans.api.html.lexer.HtmlTokenId.TAG_OPEN_SYMBOL) {
                    ts.moveNext();
                    name = ts.token().text().toString();
                }
                ts.moveNext();
                item = ts.token();
                
                //find tag attributes
                Token attrNameToken = null;
                do {
                    item = ts.token();
                    if (item.id() == HtmlTokenId.ARGUMENT) {
                        //attribute name
                        attrNameToken = item;
                    } else if (item.id() == HtmlTokenId.VALUE && attrNameToken != null) {
                        //found attribute value after attribute name
                        
                        //there may be entity reference inside the attribute value
                        //e.g. onclick="alert('hello&nbsp;world');" which divides the value
                        //into more html tokens
                        StringBuffer value = new StringBuffer();
                        Token t = null;
                        do {
                            t = ts.token();
                            value.append(t.text().toString());
                        } while (ts.moveNext() && (ts.token().id() == HtmlTokenId.VALUE || ts.token().id() == HtmlTokenId.CHARACTER));
                        
                        SyntaxElement.TagAttribute tagAttr =
                                new SyntaxElement.TagAttribute(attrNameToken.text().toString(),
                                value.toString(),
                                attrNameToken.offset(hi),
                                item.offset(hi));
                        attrs.add(tagAttr);
                        attrNameToken = null;
                    }
                    lastOffset = getTokenEnd(hi, item);
                } while ((item.id() == org.netbeans.api.html.lexer.HtmlTokenId.WS ||
                        item.id() == org.netbeans.api.html.lexer.HtmlTokenId.ARGUMENT ||
                        item.id() == org.netbeans.api.html.lexer.HtmlTokenId.OPERATOR ||
                        item.id() == org.netbeans.api.html.lexer.HtmlTokenId.VALUE ||
                        item.id() == org.netbeans.api.html.lexer.HtmlTokenId.CHARACTER) &&
                        ts.moveNext());
                
                if (item.id() == org.netbeans.api.html.lexer.HtmlTokenId.TAG_CLOSE_SYMBOL) {
                    return new org.netbeans.editor.ext.html.SyntaxElement.Tag(this,
                            offset,
                            getTokenEnd(hi,
                            item),
                            name,
                            attrs,
                            item.text().toString().equals("/>"));
                } else {
                    return new org.netbeans.editor.ext.html.SyntaxElement.Tag(this,
                            offset,
                            lastOffset,
                            name,
                            attrs);
                }
            }
            
        } finally {
            getDocument().readUnlock();
        }
        return null;
    }
    
    private static boolean isTagSymbol(Token t) {
        return (( t.id() == HtmlTokenId.TAG_OPEN_SYMBOL) ||
                ( t.id() == HtmlTokenId.TAG_CLOSE_SYMBOL));
    }
    
    private static boolean isTag(Token t) {
        return (( t.id() == HtmlTokenId.TAG_OPEN ) ||
                ( t.id() == HtmlTokenId.TAG_CLOSE ) ||
                ( t.id() == HtmlTokenId.TAG_OPEN_SYMBOL) ||
                ( t.id() == HtmlTokenId.TAG_CLOSE_SYMBOL));
    }
    
    private static int getTokenEnd( TokenHierarchy thi, Token item ) {
        List<Token> parts = item.joinedParts();
        if(parts != null) {
            //non continuos token, take end offset from the last token part
            item = parts.get(parts.size() - 1);
        } 
        return item.offset(thi) + item.text().length();
    }
    
    /**
     * Beware, changes data
     */
    private static String getQuotedString( StringBuffer data ) {
        int startIndex = 0;
        if (data == null || data.length() == 0) return null;
        while( data.charAt( startIndex ) == ' ' ) startIndex++;
        
        char stopMark = data.charAt( startIndex++ );
        if( stopMark == '"' || stopMark == '\'' ) {
            for( int index = startIndex; index < data.length(); index++ )
                if( data.charAt( index ) == stopMark ) {
                    String quoted = data.substring( startIndex, index );
                    data.delete( 0, index + 1 );
                    return quoted;
                }
        }
        
        return null;
    }
    
    List getPossibleEndTags( int offset, String prefix ) throws BadLocationException {
        prefix = prefix.toUpperCase();
        int prefixLen = prefix.length();
        SyntaxElement elem = getElementChain( offset );
        Stack stack = new Stack();
        List result = new ArrayList();
        Set found = new HashSet();
        DTD dtd = getDTD();
        
        if(elem == null) {
            if(offset > 0) {
                elem = getElementChain( offset - 1);
                if(elem == null) return result;
            } else return result;
        }
        
        int itemsCount = 0;
        for( elem= elem.getPrevious() ; elem != null; elem = elem.getPrevious() ) {
            if( elem.getType() == SyntaxElement.TYPE_ENDTAG) { // NOI18N
                String tagName = ((SyntaxElement.Named)elem).getName().trim();
                //HACK: if there is just close tag opening symbol (</) a syntax element of
                //end tag is created for it. Such SE has empty name
                if(tagName.length() == 0) {
                    continue;
                } 
                DTD.Element tag = dtd.getElement( tagName.toUpperCase() );
                if(tag != null) {
                    stack.push( ((SyntaxElement.Named)elem).getName().toUpperCase() );
                } else {
                    stack.push(tagName); //non-html tag, store it with the original case
                }
            } else if(elem.getType() == SyntaxElement.TYPE_TAG) { //now </ and > are returned as SyntaxElement.TAG so I need to filter them  NOI18N
                if(((SyntaxElement.Tag)elem).isEmpty() ) {
                    continue; // ignore empty Tags - they are like start and imediate end
                }
                
                String tagName = ((SyntaxElement.Named)elem).getName();
                DTD.Element tag = dtd.getElement( tagName.toUpperCase() );
                
                if(tag != null) {
                    tagName = tag.getName();
                    
                    if(tag.isEmpty() && tag.hasOptionalEnd()) {
                        continue; //forbidden end tag
                    }
                }
                
                if( stack.empty() ) {           // empty stack - we are on the same tree deepnes - can close this tag
                    if( tagName.startsWith( prefix ) && !found.contains( tagName ) ) {    // add only new items
                        found.add( tagName );
                        if(tag != null) {
                            //html item
                            result.add( new HtmlCompletionQuery.EndTagItem( tagName, offset-2-prefixLen, prefixLen+2, tagName, itemsCount ) );
                        } else {
                            //non html item
                            result.add( new HtmlCompletionQuery.NonHtmlEndTagItem( tagName, offset-2-prefixLen, prefixLen+2, itemsCount ) );
                        }
                    }
                    if( tag != null && !tag.hasOptionalEnd() ) break;  // If this tag have required EndTag, we can't go higher until completing this tag
                } else {                        // not empty - we match content of stack
                    if( stack.peek().equals( tagName ) ) { // match - close this branch of document tree
                        stack.pop();
                    } else if( tag != null && ! tag.hasOptionalEnd() ) break; // we reached error in document structure, give up
                }
            }
        }
        
        return result;
    }
    
    List getAutocompletedEndTag(int offset) {
        List l = new ArrayList();
        try {
            SyntaxElement elem = getElementChain( offset - 1);
            if(elem != null && elem.getType() == SyntaxElement.TYPE_TAG) {
                String tagName = ((SyntaxElement.Named)elem).getName();
                //check if the tag has required endtag
                Element dtdElem = getDTD().getElement(tagName.toUpperCase());
                if(!((SyntaxElement.Tag)elem).isEmpty() && (dtdElem == null || !dtdElem.isEmpty())) {
                    CompletionItem eti = new HtmlCompletionQuery.AutocompleteEndTagItem(tagName, offset, dtdElem != null);
                    l.add(eti);
                }
            }
        }catch(BadLocationException e) {
            //just ignore
        }
        return l;
    }
    
    private static LanguagePath findTopMostHtml(Document doc) {
        TokenHierarchy th = TokenHierarchy.get(doc);
        for(LanguagePath path : (Set<LanguagePath>)th.languagePaths()) {
            if(path.innerLanguage() == HtmlTokenId.language()) { //is this always correct???
                return path;
            }
        }
        return null;
    }
    
    /** returns top most joined html token seuence for the document. */
    public static TokenSequence<HtmlTokenId> getJoinedHtmlSequence(Document doc) {
         LanguagePath path = findTopMostHtml(doc);
         if(path == null) {
             return null;
         }
         
         return getJoinedHtmlSequence(doc, path);
    }
    
    /*
     * supposes html tokens are always joined - just one joined sequence over the document!
     */
    private static TokenSequence<HtmlTokenId> getJoinedHtmlSequence(Document doc, LanguagePath languagePath) {
        //find html token sequence, in joined version if embedded
        TokenHierarchy th = TokenHierarchy.get(doc);
        List<TokenSequence> tslist = th.tokenSequenceList(languagePath, 0, Integer.MAX_VALUE);
        if(tslist.isEmpty()) {
            return  null; //no such sequence
        }
        TokenSequence first = tslist.get(0);
        first.moveStart(); 
        first.moveNext(); //should return true
        
        List<TokenSequence> embedded = th.embeddedTokenSequences(first.offset(), false);
        TokenSequence sequence = null;
        for (TokenSequence ts : embedded) {
            if (ts.language() == HtmlTokenId.language()) {
                if (sequence == null) {
                    //html is top level
                    sequence = ts;
                    break;
                } else {
                    //the sequence is my master language
                    //get joined html sequence from it
                    sequence = sequence.embeddedJoined(HtmlTokenId.language());
                    assert sequence != null;
                    break;
                }
            }
            sequence = ts;
        }
        return sequence;
    }
    
}
