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

package org.netbeans.qa.form;

import javax.swing.JDialog;
import org.netbeans.jemmy.operators.*;

/**
 * Class implementing all necessary methods for handling "Select Palette Category" NbDialog.
 * Most parts of code are generated by jelly tools.* 
 *
 * @author Jiri Vagner
 */
public class SelectPaletteCategoryOperator extends JDialogOperator {

    private JLabelOperator _lblPaletteCategories;
    private JListOperator _lstPaletteCategories;
    public static final String ITEM_SWINGCONTAINERS = "Swing Containers"; // NOI18N
    public static final String ITEM_SWINGCONTROLS = "Swing Controls"; // NOI18N
    public static final String ITEM_SWINGMENUS = "Swing Menus"; // NOI18N
    public static final String ITEM_SWINGWINDOWS = "Swing Windows"; // NOI18N
    public static final String ITEM_AWT = "AWT"; // NOI18N
    public static final String ITEM_BORDERS = "Borders"; // NOI18N
    public static final String ITEM_BEANS = "Beans"; // NOI18N
    public static final String ITEM_LOOKANDFEELS = "Look and Feels"; // NOI18N
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;

    /** Creates new SelectPaletteCategory that can handle it.
     */
    public SelectPaletteCategoryOperator() {
        super("Select Palette Category"); // NOI18N
    }
    
    /**
     * Creates new SelectPaletteCategory using title name
     * @param title
     */
    public SelectPaletteCategoryOperator(String title) {
        super(title);
    }
    
    /**
     * Creates new instance using existing JDialog operator
     * @param wrapper 
     */
    public SelectPaletteCategoryOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }

    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Palette Categories:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblPaletteCategories() {
        if (_lblPaletteCategories==null) {
            _lblPaletteCategories = new JLabelOperator(this, "Palette Categories:");  // NOI18N
        }
        return _lblPaletteCategories;
    }

    /** Tries to find null ListView$NbList in this dialog.
     * @return JListOperator
     */
    public JListOperator lstPaletteCategories() {
        if (_lstPaletteCategories==null) {
            _lstPaletteCategories = new JListOperator(this);
        }
        return _lstPaletteCategories;
    }

    /** Tries to find "OK" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator(this, "OK"); // NOI18N
        }
        return _btOK;
    }

    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel"); // NOI18N
        }
        return _btCancel;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** clicks on "OK" JButton
     */
    public void ok() {
        btOK().push();
    }

    /** clicks on "Cancel" JButton
     */
    public void cancel() {
        btCancel().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of SelectPaletteCategory by accessing all its components.
     */
    public void verify() {
        lblPaletteCategories();
        lstPaletteCategories();
        btOK();
        btCancel();
    }
}

