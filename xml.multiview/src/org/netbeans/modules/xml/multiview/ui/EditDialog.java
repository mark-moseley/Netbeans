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

package org.netbeans.modules.xml.multiview.ui;
import org.openide.DialogDescriptor;

import org.openide.util.NbBundle;

/** EditDialog.java
 *
 * Created on November 28, 2004, 7:18 PM
 * @author mkuchtiak
 */
public abstract class EditDialog extends DialogDescriptor {
    private javax.swing.JPanel panel;

    /** Creates a new instance of EditDialog */
    public EditDialog(javax.swing.JPanel panel, String title, boolean adding) {
        super (new InnerPanel(panel),getTitle(title,adding),true,
              DialogDescriptor.OK_CANCEL_OPTION,
              DialogDescriptor.OK_OPTION,
              DialogDescriptor.BOTTOM_ALIGN,
              null,
              null);
        this.panel=panel;
    }
   
    /** Creates a new instance of EditDialog */
    public EditDialog(javax.swing.JPanel panel, String title) {
        this(panel, title,false);
    }
    
    private static String getTitle(String title, boolean adding) {
        return (adding?NbBundle.getMessage(EditDialog.class,"TTL_ADD",title):
                NbBundle.getMessage(EditDialog.class,"TTL_EDIT",title));
    }
    /** Returns the dialog panel 
    * @return dialog panel
    */
    public final javax.swing.JPanel getDialogPanel() {
        return panel;
    }
    
    /** Calls validation of panel components, displays or removes the error message
    * Should be called from listeners listening to component changes. 
    */
    public final void checkValues() {
        String errorMessage = validate();
        if (errorMessage==null) {
            setValid(true);
        } else {
            setValid(false);
        }
        javax.swing.JLabel errorLabel = ((InnerPanel)getMessage()).getErrorLabel();
        errorLabel.setText(errorMessage==null?" ":errorMessage);
    }
    
    /** Provides validation for panel components */
    protected abstract String validate();
    
    private static class InnerPanel extends javax.swing.JPanel {
        javax.swing.JLabel errorLabel;
        InnerPanel(javax.swing.JPanel panel) {
            super(new java.awt.BorderLayout());
            errorLabel = new javax.swing.JLabel(" ");
            errorLabel.setBorder(new javax.swing.border.EmptyBorder(12,12,0,0));
            errorLabel.setForeground(SectionVisualTheme.getErrorLabelColor());
            add(panel, java.awt.BorderLayout.CENTER);
            add(errorLabel, java.awt.BorderLayout.SOUTH);
            getAccessibleContext().setAccessibleName(panel.getAccessibleContext().getAccessibleName());
            getAccessibleContext().setAccessibleDescription(panel.getAccessibleContext().getAccessibleDescription());
        }
        
        void setErrorMessage(String message) {
            errorLabel.setText(message);
        }
        
        javax.swing.JLabel getErrorLabel() {
            return errorLabel;
        }
    }
    
    /** Useful DocumentListener class that can be added to the panel's text compoents */
    public static class DocListener implements javax.swing.event.DocumentListener {
        EditDialog dialog;
        
        public DocListener(EditDialog dialog) {
            this.dialog=dialog;
        }
        /**
         * Method from DocumentListener
         */
        public void changedUpdate(javax.swing.event.DocumentEvent evt) {
            dialog.checkValues();
        }

        /**
         * Method from DocumentListener
         */
        public void insertUpdate(javax.swing.event.DocumentEvent evt) {
            dialog.checkValues();
        }

        /**
         * Method from DocumentListener
         */
        public void removeUpdate(javax.swing.event.DocumentEvent evt) {
            dialog.checkValues();
        }
    }
}
