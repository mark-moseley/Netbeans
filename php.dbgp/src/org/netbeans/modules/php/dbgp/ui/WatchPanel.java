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
package org.netbeans.modules.php.dbgp.ui;

import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import java.util.*;
import java.awt.BorderLayout;

/**
 * A GUI panel for customizing a Watch.
 * Stolen from org.netbeans.modules.debugger.ui.WatchPanel
 *
 * @author ads
 */
public class WatchPanel {
    
    private static final String A11_WATCH_NAME = "ACSD_CTL_Watch_Name"; // NOI18N

    private static final String CENTER = "Center";                      // NOI18N

    private static final String WEST = "West";                          // NOI18N

    private static final String WATCH_NAME = "CTL_Watch_Name";          // NOI18N

    private static final String WATCH_PANEL = "ACSD_WatchPanel";        // NOI18N

    public WatchPanel(String expression) {
        myExpression = expression;
    }

    public JComponent getPanel() {
        if (myPanel != null) return myPanel;

        myPanel = new JPanel();
        ResourceBundle bundle = NbBundle.getBundle(WatchPanel.class);

        myPanel.getAccessibleContext ().setAccessibleDescription(
                bundle.getString (WATCH_PANEL));
        JLabel textLabel = new JLabel();
        Mnemonics.setLocalizedText(textLabel, bundle.getString (WATCH_NAME));
        textLabel.setBorder (new EmptyBorder (0, 0, 0, 10));
        myPanel.setLayout (new BorderLayout ());
        myPanel.setBorder (new EmptyBorder (11, 12, 1, 11));
        myPanel.add (WEST, textLabel);
        myPanel.add (CENTER, myTextField = new JTextField (25));
        myTextField.getAccessibleContext ().setAccessibleDescription(
                bundle.getString (A11_WATCH_NAME));
        myTextField.setBorder (
            new CompoundBorder (myTextField.getBorder (),
            new EmptyBorder (2, 0, 2, 0))
        );
        
        myTextField.setText (myExpression);
        myTextField.selectAll ();

        textLabel.setLabelFor (myTextField);
        myTextField.requestFocus ();
        return myPanel;
    }

    public String getExpression() {
        return myTextField.getText().trim();
    }
    
    private JPanel myPanel;
    
    private JTextField myTextField;
    
    private String myExpression;
}
