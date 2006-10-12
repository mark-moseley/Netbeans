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
 * CustomizerDialogDialogOperator.java
 *
 * Created on 11/13/03 4:23 PM
 */
package org.netbeans.jellytools.modules.xml.catalog;

import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.properties.PropertySheetOperator;

/** Class implementing all necessary methods for handling "Customizer Dialog" NbDialog.
 *
 * @author ms113234
 * @version 1.0
 */
public class OASISCatalogCustomizerDialogOperator extends NbDialogOperator {

    /** Creates new CustomizerDialogDialogOperator that can handle it.
     */
    public OASISCatalogCustomizerDialogOperator() {
        super("Customizer Dialog");
    }

    private JLabelOperator _lblCatalogURL;
    private JTextFieldOperator _txtCatalogURL;
    private JButtonOperator _btBrowse;
    private JCheckBoxOperator _cbPreferPublicID;
    private JTextAreaOperator _txtJTextArea;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Catalog URL:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblCatalogURL() {
        if (_lblCatalogURL==null) {
            _lblCatalogURL = new JLabelOperator(this, "Catalog URL:");
        }
        return _lblCatalogURL;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtCatalogURL() {
        if (_txtCatalogURL==null) {
            _txtCatalogURL = new JTextFieldOperator(this);
        }
        return _txtCatalogURL;
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowse() {
        if (_btBrowse==null) {
            _btBrowse = new JButtonOperator(this, "Browse...");
        }
        return _btBrowse;
    }

    /** Tries to find "Prefer Public ID" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbPreferPublicID() {
        if (_cbPreferPublicID==null) {
            _cbPreferPublicID = new JCheckBoxOperator(this, "Prefer Public ID");
        }
        return _cbPreferPublicID;
    }

    /** Tries to find null JTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtJTextArea() {
        if (_txtJTextArea==null) {
            _txtJTextArea = new JTextAreaOperator(this);
        }
        return _txtJTextArea;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** gets text for txtCatalogURL
     * @return String text
     */
    public String getCatalogURL() {
        return txtCatalogURL().getText();
    }

    /** sets text for txtCatalogURL
     * @param text String text
     */
    public void setCatalogURL(String text) {
        txtCatalogURL().setText(text);
    }

    /** types text for txtCatalogURL
     * @param text String text
     */
    public void typeCatalogURL(String text) {
        txtCatalogURL().typeText(text);
    }

    /** clicks on "Browse..." JButton
     */
    public void browse() {
        btBrowse().push();
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkPreferPublicID(boolean state) {
        if (cbPreferPublicID().isSelected()!=state) {
            cbPreferPublicID().push();
        }
    }

    /** gets text for txtJTextArea
     * @return String text
     */
    public String getJTextArea() {
        return txtJTextArea().getText();
    }

    /** sets text for txtJTextArea
     * @param text String text
     */
    public void setJTextArea(String text) {
        txtJTextArea().setText(text);
    }

    /** types text for txtJTextArea
     * @param text String text
     */
    public void typeJTextArea(String text) {
        txtJTextArea().typeText(text);
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of CustomizerDialogDialogOperator by accessing all its components.
     */
    public void verify() {
        lblCatalogURL();
        txtCatalogURL();
        btBrowse();
        cbPreferPublicID();
        txtJTextArea();
    }

    /** Performs simple test of CustomizerDialogDialogOperator
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new OASISCatalogCustomizerDialogOperator().verify();
        System.out.println("CustomizerDialogDialogOperator verification finished.");
    }
}

