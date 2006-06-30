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

package org.netbeans.spi.editor.completion;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.text.JTextComponent;

/**
 * The interface representing a single item of the result list that can be displayed
 * in the completion popup.
 *
 * @author Miloslav Metelka, Dusan Balek
 * @version 1.01
 */

public interface CompletionItem {

    /**
     * Gets invoked when user presses <code>VK_ENTER</code> key
     * or when she double-clicks on this item with the mouse cursor.
     * <br/>
     * This method gets invoked from AWT thread.
     *
     * @param component non-null text component for which the completion was invoked.
     */
    void defaultAction(JTextComponent component);

    /**
     * Process the key pressed when this completion item was selected
     * in the completion popup window.
     * <br/>
     * This method gets invoked from AWT thread.
     *
     * @param evt non-null key event of the pressed key. It should be consumed
     *  in case the item is sensitive to the given key. The source of this 
     *  event is the text component to which the corresponding action should
     *  be performed.
     */
    void processKeyEvent(KeyEvent evt);
    
    /**
     * Get the preferred visual width of this item.
     * <br>
     * The visual height of the item is fixed to 16 points.
     *
     * @param g graphics that can be used for determining the preferred width
     *  e.g. getting of the font metrics.
     * @param defaultFont default font used for rendering.
     */
    int getPreferredWidth(Graphics g, Font defaultFont);

    /**
     * Render this item into the given graphics.
     *
     * @param g graphics to render the item into.
     * @param defaultFont default font used for rendering.
     * @param defaultColor default color used for rendering.
     * @param backgroundColor color used for background.
     * @param width width of the area to render into.
     * @param height height of the are to render into.
     * @param selected whether this item is visually selected in the list
     *  into which the items are being rendered.
     */
    void render(Graphics g, Font defaultFont, Color defaultColor,
    Color backgroundColor, int width, int height, boolean selected);

    /**
     * Returns a task used to obtain a documentation associated with the item if there
     * is any.
     */
    CompletionTask createDocumentationTask();

    /**
     * Returns a task used to obtain a tooltip hint associated with the item if there
     * is any.
     */
    CompletionTask createToolTipTask();
    
    /**
     * When enabled for the item the instant substitution should process the item
     * in the same way like when the item is displayed and Enter key gets pressed
     * by the user.
     * <br>
     * Instant substitution is invoked when there would be just a single item 
     * displayed in the completion popup window.
     * <br>
     * The implementation can invoke the {@link #defaultAction(JTextComponent)}
     * if necessary.
     * <br/>
     * This method gets invoked from AWT thread.
     *
     * @param component non-null text component for which the completion was invoked.
     * @return <code>true</code> if the instant substitution was successfully done.
     *  <code>false</code> means that the instant substitution should not be done
     *  for this item and the completion item should normally be displayed.
     */
    boolean instantSubstitution(JTextComponent component);
    
    /**
     * Returns the item's priority. A lower value means a lower index of the item
     * in the completion result list.
     */
    int getSortPriority();

    /**
     * Returns a text used to sort items alphabetically.
     */
    CharSequence getSortText();

    /**
     * Returns a text used for finding of a longest common prefix
     * after the <i>TAB</i> gets pressed or when the completion is opened explicitly.
     * <br>
     * The completion infrastructure will evaluate the insert prefixes
     * of all the items present in the visible result and finds the longest
     * common prefix.
     *
     * <p>
     * Generally the returned text does not need to contain all the information
     * that gets inserted when the item is selected.
     * <br>
     * For example in java completion the field name should be returned for fields
     * or a method name for methods (but not parameters)
     * or a non-FQN name for classes.
     *
     * @return non-null character sequence containing the insert prefix.
     *  <br>
     *  Returning an empty string will effectively disable the TAB completion
     *  as the longest common prefix will be empty.
     *
     * @since 1.4
     */
    CharSequence getInsertPrefix();

}
