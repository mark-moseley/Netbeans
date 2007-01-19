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

package org.netbeans.spi.editor.highlighting;

import javax.swing.text.Position;

/**
 * The container of highlighted areas and their attributes.
 *
 * @author vita
 */
public interface HighlightsContainer {

    /**
     * The attribute key for highlights that need to span across a whole line.
     *
     * <p>Typically highlights only affect rendering of a small part of text
     * (perhaps just several characters). Some layers, however, need to highlight
     * a whole line in an editor window regardless of how much text the line
     * contains. The highlighting of a line with a caret is an example of such a layer.
     *
     * <p>If you want a highlight that spans accross the whole editor pane you
     * can add this attribute key to the highlight's <code>AttributeSet</code>
     * and set its value to <code>Boolean.TRUE</code>. The highlighted area must
     * contain the new-line character at the end of the line.
     */
    static final String ATTR_EXTENDS_EOL = new String("org.netbeans.spi.editor.highlighting.HighlightsContainer.ATTR_EXTENDS_EOL"); //NOI18N
    
    /**
     * The attribute key for highlights that need to show up on empty lines.
     * 
     * <p>If you use this key for a highlight which contains the new-line character
     * at the end of an empty line and set the value of this attribute to
     * <code>Boolean.TRUE</code> then the highlight will be drawn as
     * a half-character-wide stripe at the beginning of the line.
     */
    static final String ATTR_EXTENDS_EMPTY_LINE = new String("org.netbeans.spi.editor.highlighting.HighlightsContainer.ATTR_EXTENDS_EMPTY_LINE"); //NOI18N
    
    /**
     * Provides the list of highlighted areas that should be used for rendering
     * a document.
     *
     * <p>The returned highlighted areas (highlights) must obey the following rules:
     * <ul>
     * <li>The starting and ending offsets of each highlight should be
     * within the range specified by the <code>startOffset</code> and <code>endOffset</code>
     * parameters. Any highlights outside of this range will be clipped by the
     * rendering infrastructure.
     * <li>The highlights must not overlap. The infrastructure may ignore or trim
     * any overlapping highlights.
     * <li>The list of highlights must be sorted by their
     * starting offsets ascendingly (i.e. the smallest offset first).
     * </ul>
     *
     * <p>The editor infrastructure will log any problems it may encounter with
     * provided implementations of this interface. Although the infrastructure
     * will try to do its best to render all highlights supplied by the implementors,
     * if the above rules are violated the results can't be garanteed.
     * 
     * @param startOffset The starting offset of the area which the caller
     *  attempts to repaint (or create views for). The staring offset is always >=0.
     * @param endOffset The ending offset of the rendered area. The <code>Integer.MAX_VALUE</code>
     *  can be passed in if the end offset is unknown to the caller.
     *  The highlights container is then expected to return all highlights
     *  up to the end of the document.
     *
     * @return non-null iterator of highlights sorted by offsets.
     */
    HighlightsSequence getHighlights(int startOffset, int endOffset);
    
    /**
     * Adds a listener to this highlights container.
     *
     * @param listener    The listener to add.
     */
    void addHighlightsChangeListener(HighlightsChangeListener listener);
    
    /**
     * Removes a listener from this highlights container.
     *
     * @param listener    The listener to remove.
     */
    void removeHighlightsChangeListener(HighlightsChangeListener listener);
    
}
