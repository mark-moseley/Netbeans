/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.guards;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

/**
 * Represents an advanced guarded block.
 * It consists of three pieces: a header, body, and footer.
 * The header and footer are guarded but the body is not.
 */
public final class InteriorSectionImpl extends GuardedSectionImpl {
    /** Text range of the header. */
    private PositionBounds header;

    /** Text range of the header. */
    private PositionBounds body;

    /** Text range of the footer. */
    private PositionBounds footer;

    /**
     * Creates new section.
     * @param name Name of the new section.
     */
    InteriorSectionImpl(String name, PositionBounds header, PositionBounds body, PositionBounds footer, GuardedSectionsImpl guards) {
        super(name, guards);
        this.header = header;
        this.body = body;
        this.footer = footer;
    }

    /**
     * Set the text of the body.
     * @param text the new text
     */
    public void setBody(String text) {
        setText(body, text, false);
    }

    public String getBody() {
        String s = null;
        if (isValid()) {
            try {
                s = body.getText();
            } catch (BadLocationException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return s;
    }

    /**
     * Set the text of the header.
     * @param text the new text
     */
    public void setHeader(String text) {
        setText(header, text, true);
    }

    /**
     * Returns the contents of the header part of the section. If the
     * section is invalid the method returns null.
     * @return contents of the header or null, if the section is not valid.
     */
    public String getHeader() {
        String s = null;
        if (isValid()) {
            try {
                s = header.getText();
            } catch (BadLocationException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return s;
    }

    /**
     * Set the text of the footer.
     * Note that the footer of the section must have exactly one line.
     * So, all interior newline characters will be replaced by spaces.
     *
     * @param text the new text
     */
    public void setFooter(String text) {
        boolean endsWithEol = text.endsWith("\n"); // NOI18N
        int firstEol = text.indexOf('\n');
        int lastEol = text.lastIndexOf('\n');

        if ((firstEol != lastEol) || (endsWithEol && (firstEol != -1))) {
            if (endsWithEol) {
                text = text.substring(0, text.length() - 1);
            }
            text = text.replace('\n', ' ');
        }
        setText(footer, text, true);
    }

    /**
     * Returns the contents of the footer part of the guarded section.
     * The method will return null, if the section is not valid.
     * @return contents of the footer part, or null if the section is not valid.
     */
    public String getFooter() {
        String s = null;
        if (isValid()) {
            try {
                s = footer.getText();
            } catch (BadLocationException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return s;
    }

    /**
     * Gets the begin of section. To this position is set the cursor
     * when section is open in the editor.
     * @return the begin position of the body section - the place where
     *         is possible to edit.
     */
    public Position getCaretPosition() {
        return body.getBegin();
    }

    /**
     * Marks the section as guarded.
     * @param doc The styled document where this section placed in.
     */
    void markGuarded(StyledDocument doc) {
        markGuarded(doc, header, true);
        markGuarded(doc, footer, true);
    }

    /**
     * Unmarks the section as guarded.
     * @param doc The styled document where this section placed in.
     */
    void unmarkGuarded(StyledDocument doc) {
        markGuarded(doc, header, false);
        markGuarded(doc, footer, false);
    }

    /**
     * Gets the text contained in the section.
     * @return The text contained in the section.
     */
    public String getText() {
        StringBuffer buf = new StringBuffer();
        try {
            buf.append(header.getText());
            buf.append(body.getText());
            buf.append(footer.getText());
        } catch (Exception e) {
        }
        return buf.toString();
    }

    /*
    public String toString() {
      StringBuffer buf = new StringBuffer("InteriorSection:"+name); // NOI18N
      try {
        buf.append("HEADER:\""); // NOI18N
        buf.append(header.getText());
        buf.append("\"");
        buf.append("BODY:\""); // NOI18N
        buf.append(body.getText());
        buf.append("\"");
        buf.append("BOTTOM:\""); // NOI18N
        buf.append(footer.getText());
        buf.append("\"");
      }
      catch (Exception e) {
        buf.append("EXCEPTION:"); // NOI18N
        buf.append(e.getMessage());
      }
      return buf.toString();
    }*/
    public boolean contains(Position pos, boolean allowHoles) {
        if (!allowHoles) {
    		return header.getBegin().getOffset() <= pos.getOffset() &&
    	    footer.getEnd().getOffset() >= pos.getOffset();
        } else {
    	if (header.getBegin().getOffset() <= pos.getOffset() &&
    	    header.getEnd().getOffset() >= pos.getOffset()) {
    	    return true;
    	}
    	return footer.getBegin().getOffset() <= pos.getOffset() &&
    	    footer.getEnd().getOffset() >= pos.getOffset();
        }
    }

    public Position getStartPosition() {
        return header.getBegin();
    }

    public Position getEndPosition() {
        return footer.getEnd();
    }
    
    public Position getBodyStartPosition() {
        return body.getBegin();
    }
    
    public Position getBodyEndPosition() {
        return body.getEnd();
    }

    public void resolvePositions() throws BadLocationException {
        header.resolvePositions();
        body.resolvePositions();
        footer.resolvePositions();
    }

    // just for to unit testing purposes
    public PositionBounds getHeaderBounds() {
        return this.header;
    }
    
    public PositionBounds getBodyBounds() {
        return this.body;
    }
    
    public PositionBounds getFooterBounds() {
        return this.footer;
    }
}
