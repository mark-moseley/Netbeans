/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.editor.ext.html;

import java.util.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;
import org.netbeans.editor.ext.html.dtd.*;


/**
 *
 * @author  Petr Nejedly
 * @version 0.9
 */
public class HTMLSyntaxSupport extends ExtSyntaxSupport implements InvalidateListener {
    private static final String FALLBACK_DOCTYPE =
        "-//W3C//DTD HTML 4.01 Transitional//EN";  // NOI18N
    
    private DTD dtd;
    private String docType;
    
    /** Creates new HTMLSyntaxSupport */
    public HTMLSyntaxSupport( BaseDocument doc ) {
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
        return dtd;
    }
    
    protected String getDocType() {
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

    
    
    /** Find matching tags with the current position.
     * @param offset position of the starting tag
     * @param simple whether the search should skip comment and possibly other areas.
     *  This can be useful when the speed is critical, because the simple
     *  search is faster.
     * @return array of integers containing starting and ending position
     *  of the block in the document. Null is returned if there's
     *  no matching block.
     */ 
    public int[] findMatchingBlock(int offset, boolean simpleSearch)
	throws BadLocationException {	   
	    // TODO - replanning to the other thread. Now it's in awt thread
	    TokenItem token = getTokenChain(offset, offset+1);
            
            // if the carret is after HTML tag ( after char '>' ), ship inside the tag
            if (token != null && token.getTokenID().getNumericID() == HTMLTokenContext.TAG_CLOSE_SYMBOL_ID)
                token = token.getPrevious();
            boolean isInside = false;  // flag, whether the carret is somewhere in a HTML tag
            if (token != null && isTag(token)) {
                    isInside = true; // the carret is somewhere in '<htmltag' or '</htmltag' 
            }
            else {  
                // find out whether the carret is inside an HTML tag
                if (token != null){
                    token = token.getPrevious();
                    //try to find the beginning of the tag. 
                    while (token!=null && !isTag(token) && token.getTokenID().getNumericID() != HTMLTokenContext.TAG_CLOSE_SYMBOL_ID)
                        token = token.getPrevious();
                    if (token!=null && isTag(token))
                        isInside = true;
                }
            }
            
            
	    if (token != null && isTag(token) && isInside){
		int start; // possition where the matched tag starts
		int end;   // possition where the matched tag ends
		int poss = -1; // how many the same tags is inside the mathed tag
                
                //test whether we are in a close tag
		if (token.getTokenID() == HTMLTokenContext.TAG_CLOSE) {
                    //we are in a close tag
		    String tag = token.getImage().trim().toLowerCase();
		    while ( token != null){
			if (isTag(token)) {
			    if (token.getImage().trim().toLowerCase().equals(tag) &&
                                token.getTokenID() == HTMLTokenContext.TAG_OPEN){
                                //it's an open tag
				if (poss == 0){  
                                    //get offset of previous token: < or </
				    start = token.getPrevious().getOffset();
				    end = token.getOffset()+token.getImage().length()+1;
				    token = token.getNext();

				    while (token != null && token.getTokenID().getNumericID() != HTMLTokenContext.TAG_CLOSE_SYMBOL_ID){
					token = token.getNext();
				    }
				    if (token != null)
					end = token.getOffset()+token.getImage().length();
				    return new int[] {start, end};
				}
				else{
				    poss--;
				}
			    }
			    else {
                                //test whether the tag is a close tag for the 'tag' tagname
				if (token.getImage().toLowerCase().indexOf(tag) > -1){				    
				    poss++;
				}
			    }
			}
			token = token.getPrevious();			
		    }
       
		}
		else{
                    //we are in an open tag
		    if (token.getImage().charAt(0) == '>')
			return null;
		    String tag = token.getImage().toLowerCase();
		    while ( token != null){
			if (isTag(token)) {
                            if (token.getImage().trim().toLowerCase().equals(tag) &&
                                token.getTokenID() == HTMLTokenContext.TAG_CLOSE){
				if (poss == 0) {
                                    //get offset of previous token: < or </
				    start = token.getPrevious().getOffset();
				    end = token.getOffset()+token.getImage().length()+1;
				    token = token.getNext();
				    while (token != null && token.getTokenID().getNumericID() != HTMLTokenContext.TAG_CLOSE_SYMBOL_ID){
					token = token.getNext();
				    }
				    if (token != null)
					end = token.getOffset()+token.getImage().length();
				    return new int[] {start, end};
				}
				else
				    poss--;
			    }
			    else{
				if (token.getImage().toLowerCase().equals(tag))
				    poss++;
			    }
			}
			token = token.getNext();
		    }
		}
	    }
	    return null;
    }
    
    
    private final int getTokenEnd( TokenItem item ) {
        return item.getOffset() + item.getImage().length();
    }

    /** Returns SyntaxElement instance for block of tokens, which is either
     * surrounding given offset, or is just after the offset.
     * @param offset offset in document where to search for SyntaxElement
     * @return SyntaxElement surrounding or laying after the offset
     * or <CODE>null</CODE> if there is no element there (end of document)
     */
    public SyntaxElement getElementChain( int offset ) throws BadLocationException {
        TokenItem first = getTokenChain( offset, Math.min( offset + 10, getDocument().getLength() ) ); 
        TokenItem item = first;

        while (item != null && !item.getTokenContextPath().contains(HTMLTokenContext.contextPath) ){
            item = item.getPrevious();
        }
        
        if( item == null ) return null; // on End of document
        TokenID id = item.getTokenID();

        int beginning = item.getOffset();

        if( id == HTMLTokenContext.CHARACTER ) {
            while( id != null && id == HTMLTokenContext.CHARACTER ) {
                beginning = item.getOffset();
                item = item.getPrevious();
                id = item == null ? null : item.getTokenID();
            }

            // now item is either HTMLSyntax.VALUE or we're in text, or at BOF
            if( id != HTMLTokenContext.VALUE && id != HTMLTokenContext.TEXT ) {
                return getNextElement( beginning );
            } // else ( for VALUE or TEXT ) fall through
        }

        if( id == HTMLTokenContext.WS || id == HTMLTokenContext.ARGUMENT ||     // these are possible only in Tags
            id == HTMLTokenContext.OPERATOR || id == HTMLTokenContext.VALUE ) { // so find boundary
                do { 
                    item = item.getPrevious();      // Can't get null here, there IS TAG before WS|ARGUMENT|OPERATOR|VALUE
                    id = item.getTokenID();
                } while( !isTag(item) );            
                return getNextElement( item.getOffset() );       // TAGC
        }

        if( id == HTMLTokenContext.TEXT ) {
            while( id != null && (id == HTMLTokenContext.TEXT || id == HTMLTokenContext.CHARACTER) ) { 
                beginning = item.getOffset();
                item = item.getPrevious();
                id = item == null ? null : item.getTokenID();
            }
            return getNextElement( beginning ); // from start of Commment
        }

        if( isTag(item)) {
            if( item.getTokenID() == HTMLTokenContext.TAG_OPEN || 
                    item.getTokenID() == HTMLTokenContext.TAG_OPEN_SYMBOL)  return getNextElement( item.getOffset() );  // TAGO/ETAGO // NOI18N
            else {
                do { 
                    item = item.getPrevious();
                    id = item.getTokenID();
                } while( !isTag(item) );
                return getNextElement( item.getOffset() );       // TAGC
            }
        }

        if( id == HTMLTokenContext.ERROR )
            return new SyntaxElement( this, item.getOffset(), getTokenEnd( item ), SyntaxElement.TYPE_ERROR );

        if( id == HTMLTokenContext.BLOCK_COMMENT ) {
            while( id == HTMLTokenContext.BLOCK_COMMENT && !item.getImage().startsWith( "<!--" ) ) { // NOI18N
                item = item.getPrevious();
                id = item.getTokenID();
            }
            return getNextElement( item.getOffset() ); // from start of Commment
        }


        if( id == HTMLTokenContext.DECLARATION || id == HTMLTokenContext.SGML_COMMENT ) {
            while( id != HTMLTokenContext.DECLARATION || !item.getImage().startsWith( "<!" ) ) { // NOI18N
                item = item.getPrevious();
                id = item.getTokenID();
            }
            return getNextElement( item.getOffset() ); // from start of Commment
        }
        return null;
    }

    /** The way how to get previous SyntaxElement in document. It is not intended
     * for direct usage, and thus is not public. Usually, it is called from
     * SyntaxElement's method getPrevious()
     */
    SyntaxElement getPreviousElement( int offset ) throws BadLocationException {
        return offset == 0 ? null : getElementChain( offset - 1 );
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
    
   /** Get the next element from given offset. Should only be called from
    * SyntaxElements obtained by getElementChain, or by getElementChain itself.
    * @return SyntaxElement startting at offset, or null, if EoD
    */
    public SyntaxElement getNextElement( int offset ) throws BadLocationException {
        TokenItem item = getTokenChain( offset, Math.min( offset + 10, getDocument().getLength() ) );
        if( item == null ) return null; // on End of Document
        TokenID id = item.getTokenID();

        int lastOffset = getTokenEnd( item );
        if( id == HTMLTokenContext.BLOCK_COMMENT ) {
            while( id == HTMLTokenContext.BLOCK_COMMENT ) {
                lastOffset = getTokenEnd( item );
                item = item.getNext();
                if( item == null ) break; //EoD
                id = item.getTokenID();
            }
            return new SyntaxElement( this, offset, lastOffset, SyntaxElement.TYPE_COMMENT );
        }

        if( id == HTMLTokenContext.DECLARATION ) {
            // Compose whole declaration, leaving out included comments
            StringBuffer sb = new StringBuffer( item.getImage() );

            while( id == HTMLTokenContext.DECLARATION || id == HTMLTokenContext.SGML_COMMENT ) {
                lastOffset = getTokenEnd( item );
                item = item.getNext();
                if( item == null ) break; //EoD
                id = item.getTokenID();
                if( id == HTMLTokenContext.DECLARATION )
                    sb.append( item.getImage() );
            }
            
            String image = sb.toString();

            // not a DOCTYPE declaration
            if( ! image.startsWith( "<!DOCTYPE" ) )     // NOI18N
                return new SyntaxElement.Declaration( this, offset, lastOffset,
                                null, null, null );
            
            // Cut off the <!DOCTYPE substring and possible ws
            image = image.substring( 9 ).trim();
            
            int index = image.indexOf( ' ' );
            if( index < 0 ) return new SyntaxElement.Declaration(
                        this, offset, lastOffset, null, null, null );

            String rootElem = image.substring( 0, index );

            image = image.substring( index ).trim();
            
            if( image.startsWith( "PUBLIC" ) ) {    // NOI18N   Public ID
                image = image.substring( 6 ).trim();
                sb = new StringBuffer( image );
                String pi = getQuotedString( sb );
                if( pi != null ) {
                    String si = getQuotedString( sb );
                    return new SyntaxElement.Declaration(
                            this, offset, lastOffset, rootElem, pi, si );
                }
            } else if( image.startsWith( "SYSTEM" ) ) { // NOI18N   System ID
                image = image.substring( 6 ).trim();
                sb = new StringBuffer( image );
                String si = getQuotedString( sb );
                if( si != null ) {
                        return new SyntaxElement.Declaration(
                                this, offset, lastOffset, rootElem, null, si );
                }
            }
            return new SyntaxElement.Declaration(
                    this, offset, lastOffset, null, null, null );
        }

        if( id == HTMLTokenContext.ERROR )
            return new SyntaxElement( this, item.getOffset(), lastOffset, SyntaxElement.TYPE_ERROR );

        if( id == HTMLTokenContext.TEXT || id == HTMLTokenContext.CHARACTER ) {
            while( id == HTMLTokenContext.TEXT || id == HTMLTokenContext.CHARACTER ) {
                lastOffset = getTokenEnd( item );
                item = item.getNext();
                if( item == null ) break; //EoD
                id = item.getTokenID();
            }
            return new SyntaxElement( this, offset, lastOffset, SyntaxElement.TYPE_TEXT );
        }

        String text = item.getImage();
        if( id == HTMLTokenContext.TAG_CLOSE || id == HTMLTokenContext.TAG_CLOSE_SYMBOL) {
                // endtag // NOI18N
                String name = text;
                item = item.getNext();
                id = item == null ? null : item.getTokenID();

                while( id == HTMLTokenContext.WS ) {
                    lastOffset = getTokenEnd( item );
                    item = item.getNext();
                    id = item == null ? null : item.getTokenID();
                }

                if( id == HTMLTokenContext.TAG_CLOSE_SYMBOL) {   // with this tag // NOI18N
                    return new SyntaxElement.Named( this, offset, getTokenEnd( item ), SyntaxElement.TYPE_ENDTAG, name );
                } else {                                                            // without this tag
                    return new SyntaxElement.Named( this, offset, lastOffset, SyntaxElement.TYPE_ENDTAG, name );
                }
            }
        
        if( id == HTMLTokenContext.TAG_OPEN || id == HTMLTokenContext.TAG_OPEN_SYMBOL) {
                // starttag
                String name = text;
                ArrayList attrs = new ArrayList();

                item = item.getNext();
                id = item == null ? null : item.getTokenID();

                while( id == HTMLTokenContext.WS || 
                       id == HTMLTokenContext.ARGUMENT || id == HTMLTokenContext.OPERATOR ||
                       id == HTMLTokenContext.VALUE || id == HTMLTokenContext.CHARACTER
                ) {
                    if( id == HTMLTokenContext.ARGUMENT ) attrs.add( item.getImage() );  // log all attributes
                    lastOffset = getTokenEnd( item );
                    item = item.getNext();
                    id = item == null ? null : item.getTokenID();
                }
                if( id == HTMLTokenContext.TAG_CLOSE_SYMBOL) {   // with this tag // NOI18N
                    return new SyntaxElement.Tag( this, offset, getTokenEnd( item ), name, attrs );
                } else {                                                            // without this tag
                    return new SyntaxElement.Tag( this, offset, lastOffset, name, attrs );
                }             

            }
        

        throw new BadLocationException( "Misuse at " + offset, offset ); // NOI18N
    }
        
    public List getPossibleEndTags( int offset, String prefix ) throws BadLocationException {
        prefix = prefix.toUpperCase();
        int prefixLen = prefix.length();
        SyntaxElement elem = getElementChain( offset );
        Stack stack = new Stack();
        List result = new ArrayList();
        Set found = new HashSet();
        DTD dtd = getDTD();
        
        if(elem == null) return result;
        
        for( ; elem != null; elem = elem.getPrevious() ) {
            if( elem.getType() == SyntaxElement.TYPE_ENDTAG && elem.getText().endsWith(">") ) {
                stack.push( ((SyntaxElement.Named)elem).getName().toUpperCase() );
            } else if( (elem.getType() == SyntaxElement.TYPE_TAG) && (elem.getText().indexOf("<") == -1)) { //now </ and > are returned as SyntaxElement.TAG so I need to filter them
                DTD.Element tag = dtd.getElement( ((SyntaxElement.Tag)elem).getName().toUpperCase() );

                if( tag == null ) continue; // Unknown tag - ignore
                if( tag.isEmpty() ) continue; // ignore empty Tags - they are like start and imediate end

                String name = tag.getName();

                if( stack.empty() ) {           // empty stack - we are on the same tree deepnes - can close this tag
                    if( name.startsWith( prefix ) && !found.contains( name ) ) {    // add only new items
                        found.add( name );
                        result.add( new HTMLCompletionQuery.EndTagItem( name, offset-2-prefixLen, prefixLen+2 ) );
                    }
                    if( ! tag.hasOptionalEnd() ) break;  // If this tag have required EndTag, we can't go higher until completing this tag
                } else {                        // not empty - we match content of stack
                    if( stack.peek().equals( name ) ) { // match - close this branch of document tree
                        stack.pop();
                    } else if( ! tag.hasOptionalEnd() ) break; // we reached error in document structure, give up
                }
            }
        }
        
        return result;
    }
   
    public int checkCompletion(JTextComponent target, String typedText, boolean visible ) {
        if( !visible ) {
            int retVal = COMPLETION_CANCEL;
            switch( typedText.charAt( typedText.length()-1 ) ) {
                case '/':
                    int dotPos = target.getCaret().getDot();
                    BaseDocument doc = (BaseDocument)target.getDocument();
                    if (dotPos >= 2) { // last char before inserted slash
                        try {
                            String txtBeforeSpace = doc.getText(dotPos-2, 2);
                            if( txtBeforeSpace.equals("</") )  // NOI18N
                                retVal = COMPLETION_POPUP;
                        } catch (BadLocationException e) {}
                    }
                    break;

                case ' ':
                case '<':
                case '&':
                    retVal = COMPLETION_POPUP;
                    break;
            }
            return retVal;
        } else { // the pane is already visible
            switch (typedText.charAt(0)) {
                case '>':
                case ';':
                    return COMPLETION_HIDE;
            }
            return COMPLETION_POST_REFRESH;
        }
    }
        
    public static boolean isTag(TokenItem ti) {
        return (( ti.getTokenID() == HTMLTokenContext.TAG_OPEN ) ||
                ( ti.getTokenID() == HTMLTokenContext.TAG_CLOSE ) ||
                ( ti.getTokenID() == HTMLTokenContext.TAG_OPEN_SYMBOL) ||
                ( ti.getTokenID() == HTMLTokenContext.TAG_CLOSE_SYMBOL));
    }
    
}
