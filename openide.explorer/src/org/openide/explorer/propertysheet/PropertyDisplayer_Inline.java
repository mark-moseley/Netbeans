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
/*
 * PropertyDisplayer_Inline.java
 * Refactored from PropertyDisplayer.Inline to avoid making the class public
 * Created on December 13, 2003, 7:22 PM
 */
package org.openide.explorer.propertysheet;


/** Basic interface for a property displayer which displays an inline
 * editor component, including methods for the various UI options
 * appropriate to such a component
 * @author Tim Boudreau
 */
interface PropertyDisplayer_Inline extends PropertyDisplayer {
    /** Set whether the instance should the custom editor button if
     * the property's property editor supports a custom editor */
    public void setShowCustomEditorButton(boolean val);

    /** Determine if the custom editor button should be shown for properties
     * whose editors support custom editors.  The default should be true */
    public boolean isShowCustomEditorButton();

    /** Set whether components should have borders as is typical for use
     * in a panel, or should show no borders, as is appropriate for uses
     * in tables and trees.  This may also affect behavior on focus -
     * components with popup windows should show their popup immediately
     * on receiving focus if table ui has been set to true */
    public void setTableUI(boolean val);

    /** Determine if the instance will show borders.  The default should
     * be false */
    public boolean isTableUI();

    /** Set a threshold for editors which would normally be rendered as
     * a combo box - below the threshold number of possible values, a
     * panel containing radio buttons should be used; above it a combo
     * box should be used */
    public void setRadioButtonMax(int max);

    /** Get the threshold number of components above which a combo box
     * should be used, and below which a panel containing radio buttons
     * should be used.  Default should be 0. */
    public int getRadioButtonMax();

    /** Sets the policy for using labels.  Certain types of inline editors,
     * notably checkboxes and radio button panels, are most usable if they
     * show the title of the property (in the case of radio buttons, using
     * TitledBorder, and in the case of checkboxes, as the checkbox
     * caption (which also serves to indicate keyboard focus in many look
     * and feels.  If this value is true, the component supplied will
     * display the title of a component if the component is one that can
     * benefit from this.  Setting this property to true does not guarantee
     * that a title will be displayed - that depends on the type of editor
     * used - for cases like combo boxes, proper alignment of labels with
     * combo boxes cannot be done automatically (taking into account the
     * other components on the form), but for cases like checkboxes and
     * radio buttons it is useful.  If the component is indeed displaying
     * a lable, isTitleDisplayed() will return true. */
    public void setUseLabels(boolean val);

    /** Determine if, in appropriate cases, the component should show the
     * title of the property being edited.  The default is false. */
    public boolean isUseLabels();

    /** Returns true if isUseLabels() is true and the displayed component
     * does indeed show the title */
    public boolean isTitleDisplayed();

    /** Returns true if the displayer is set to use radio buttons for
     * boolean properties instead of a checkbox */
    public boolean isRadioBoolean();

    /** Set whether boolean properties should be represented by a checkbox
     * or a pair of radio buttons */
    public void setRadioBoolean(boolean b);

    /**
     * If this ever becomes API, delete this method and find another way.
     * @return The ReusablePropertyEnv instance this displayer reconfigures on
     *  the fly to display different properties
     */
    public ReusablePropertyEnv getReusablePropertyEnv();
}
