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

package org.netbeans.modules.xml.text.syntax;

import java.util.*;

import org.w3c.dom.*;

import javax.swing.text.*;

import org.netbeans.editor.ext.*;
import org.netbeans.editor.*;

/**
 *
 * Instances are produced by {@link XMLSyntaxSupport}.
 * <p>
 * <b>Warning:</b> class is public only for private purposes!
 *
 * @author  Petr Nejedly - original HTML design
 * @author  Sandeep Randhawa - XML port
 * @author  Petr Kuzel - DOM Nodes
 *
 * @version 1.0
 */
public abstract class SyntaxElement {
    
// to do do not handle prolog as text!
// support PIs
    
    protected XMLSyntaxSupport support;  // it produced us
    protected TokenItem first;     // cached first token chain item

    private SyntaxElement previous;    // cached previous element
    private SyntaxElement next;        // cached next element
    
    // let it be visible by static inner classes extending us
    int offset;     // original position in document //??? use item instead
    int length;     // original lenght in document
    

    /** Creates new SyntaxElement */
    public SyntaxElement(XMLSyntaxSupport support, TokenItem first, int to)  {
        
        this.support = support;
        this.first = first;        
        this.offset = first.getOffset();
        this.length = to-offset;
    }

    public int getElementOffset() {

        return offset;
    }

    public int getElementLength() {
        return length;
    }

    /** 
     * Get previous SyntaxElement. Cache results.
     * @return previous SyntaxElement or <code>null</code> at document begining
     * or illegal location.
     */
    public SyntaxElement getPrevious() {
        try {
            if( previous == null ) {
                if (first.getOffset() == 0) return null;
                previous = support.getElementChain( getElementOffset() - 1 );
                if( previous != null ) {
                    previous.next = this;
                    if (previous.first.getOffset() == first.getOffset()) {
                        throw new IllegalStateException("Previous cannot be the same as current element!");
                    }                    
                }
            }
            return previous;
        } catch (BadLocationException ex) {
            return null;
        }
    }

    /** 
     * Get next SyntaxElement. Cache results.
     * @return next SyntaxElement or <code>null</code> at document end
     * or illegal location.
     */
    public SyntaxElement getNext() {
        try {
            if( next == null ) {
                next = support.getElementChain( offset+length + 1 );
                if( next != null ) {
                    next.previous = this;
                    if (next.first.getOffset() == first.getOffset()) {
                        throw new IllegalStateException("Next cannot be the same as current element!");
                    }
                }
            }
            return next;
        } catch (BadLocationException ex) {
            return null;
        }
    }

    /**
     * Print element content for debug purposes.
     */
    public String toString() {
        return "Node[" + offset + "," + (offset+length-1) + "]";
    }
    
    /**
     * 
     */
    public int hashCode() {
        return super.hashCode() ^ offset ^ length;
    }

    public boolean equals(Object obj) {
        if (obj instanceof SyntaxElement) {
            if (((SyntaxElement)obj).offset == offset) return true;
        }
        return false;
    }
    
        
    // Particular non-DOM syntax elements ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            

    /** 
     * doctype and all follow ups 
     */
    public static class Declaration extends SyntaxElement {
        
        public Declaration( XMLSyntaxSupport support, TokenItem from, int to) {
            super( support, from, to);
        }
       
        public String toString() {
            return "Declaration" + super.toString();                            // NOI18N
        }                
    }


    /**
     * It may stop some DOM traversing.  //!!!
     */
    public static class Error extends SyntaxElement {
        
        public Error( XMLSyntaxSupport support, TokenItem from, int to ) {
            super( support, from, to );
        }

        public String toString() {
            return "Error" + super.toString();                                  // NOI18N
        }        
    }

}
