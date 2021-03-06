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


package org.netbeans.modules.i18n.wizard;

import org.openide.util.NbBundle;
import javax.swing.JPanel;


/**
 * Panel to monitor long tasks in i18n wizard. Namely searchnig
 * for hard codes strings and replacing the hard coded string
 * with internationalized ones.
 *
 * @author  Peter Zavadsky
 */
final class ProgressWizardPanel extends JPanel {

    /** Creates new form ProgressPanel */
    public ProgressWizardPanel(boolean withSubProgress) {
        initComponents();
	this.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(AdditionalWizardPanel.class).getString("ACS_ProgressWizardPanel"));                
        
        if(!withSubProgress) {
            remove(subLabel);
            remove(subProgress);
        }
        
        setPreferredSize(I18nWizardDescriptor.PREFERRED_DIMENSION);
    }

    
    /** Sets text of main label. */
    public void setMainText(String text) {
        mainLabel.setText(text);
    }
    
    /** Sets text of sub label. */
    public void setSubText(String text) {
        subLabel.setText(text);
    }

    /** Sets main progress bar value. */
    public void setMainProgress(int value) {
        mainProgress.setValue(value);
    }
    
    /** Sets sub progress bar value. */
    public void setSubProgress(int value) {
        subProgress.setValue(value);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        mainLabel = new javax.swing.JLabel();
        mainProgress = new javax.swing.JProgressBar();
        subLabel = new javax.swing.JLabel();
        subProgress = new javax.swing.JProgressBar();
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(mainLabel, gridBagConstraints1);
        
        
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(7, 0, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints1.weightx = 1.0;
        add(mainProgress, gridBagConstraints1);
        
        
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.insets = new java.awt.Insets(11, 0, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(subLabel, gridBagConstraints1);
        
        
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(7, 0, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints1.weightx = 1.0;
        add(subProgress, gridBagConstraints1);
        
    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel mainLabel;
    private javax.swing.JProgressBar mainProgress;
    private javax.swing.JLabel subLabel;
    private javax.swing.JProgressBar subProgress;
    // End of variables declaration//GEN-END:variables

}

