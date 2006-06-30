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

package org.netbeans.modules.editor.errorstripe.privatespi;

import java.awt.Color;

/**Provides description of a mark that should be displayed in the Error Stripe.
 *
 * @author Jan Lahoda
 */
public interface Mark {

    /**Mark that is error-like. The mark will be shown as
     * a thin horizontal line.
     */
    public static final int TYPE_ERROR_LIKE = 1;

    public static final int TYPE_CARET = 2;

    /**Default priority.
     */
    public static final int PRIORITY_DEFAULT = 1000;

    /**Return of what type is this mark. Currently only one type
     * exists: {@link #TYPE_ERROR_LIKE}. Other types may be
     * introduced later.
     *
     * @return {@link #TYPE_ERROR_LIKE}
     */
    public int getType();
    
    /**Returns status that represents this mark.
     *
     *@return status representing this mark
     */
    public Status getStatus();
    
    /**Returns priority of this mark. The priority prioritizes the marks in the same
     * status. The smaller number, the greater priority.
     *
     * @return priority of this mark
     * @see #PRIORITY_DEFAULT
     */
    public int getPriority();
    
    /**Returns enhanced (non-standard) color of this mark. If null, default color
     * for given status will be used.
     *
     * @return Color or null if default should be used.
     */
    public Color  getEnhancedColor();
    
    /**Returns line span which represents this mark.
     *
     * @return an array of size two, the first item represents starting line of the span,
     *         the second item ending line of the span. Both lines are inclusive.
     */
    public int[]  getAssignedLines();
    
    /**Return some human readable short description to be shown for
     * example in tooltips.
     *
     * @return a short description.
     */
    public String getShortDescription();
    
}
