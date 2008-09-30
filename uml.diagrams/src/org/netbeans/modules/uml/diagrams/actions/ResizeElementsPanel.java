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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.uml.diagrams.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;


public class ResizeElementsPanel extends javax.swing.JPanel
    implements DocumentListener, VetoableChangeListener, ActionListener
{
    private int resizeHeight = 0;
    private int resizeWidth = 0;
    
    private String statusMsg = " "; // NOI18N
    private boolean enableOk = false;
    
    
    public ResizeElementsPanel(int width, int height)
    {
        resizeHeight = height;
        resizeWidth = width;

        initComponents();
        
        statusLabel.setText(statusMsg);
        heightText.setText(String.valueOf(resizeHeight));
        widthText.setText(String.valueOf(resizeWidth));
        
        heightText.getDocument().addDocumentListener(this);
        widthText.getDocument().addDocumentListener(this);
    }

    @Override
    public void requestFocus()
    {
        heightText.requestFocus();
        updateValidStatus();
    }


    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        heightLabel = new javax.swing.JLabel();
        heightText = new javax.swing.JTextField();
        widthLabel = new javax.swing.JLabel();
        widthText = new javax.swing.JTextField();
        statusLabel = new javax.swing.JLabel();
        widthText1 = new javax.swing.JTextField();

        heightLabel.setLabelFor(heightText);
        org.openide.awt.Mnemonics.setLocalizedText(heightLabel, org.openide.util.NbBundle.getMessage(ResizeElementsPanel.class, "LBL_HeightLabel")); // NOI18N

        heightText.setColumns(10);
        heightText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                heightTextFocusGained(evt);
            }
        });

        widthLabel.setLabelFor(widthText);
        org.openide.awt.Mnemonics.setLocalizedText(widthLabel, org.openide.util.NbBundle.getMessage(ResizeElementsPanel.class, "LBL_WidthLabel")); // NOI18N

        widthText.setColumns(10);
        widthText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                widthTextFocusGained(evt);
            }
        });

        statusLabel.setForeground(new java.awt.Color(255, 0, 0));
        statusLabel.setText("<error status>");
        statusLabel.setMaximumSize(new java.awt.Dimension(573, 54));

        widthText1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                widthText1FocusGained(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(statusLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(widthLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(widthText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(heightLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(heightText, 0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(widthText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(167, 167, 167)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(heightLabel)
                    .add(heightText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(widthLabel)
                    .add(widthText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(statusLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        heightLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ResizeElementsPanel.class, "ACSD_HeightLabel_Name")); // NOI18N
        heightLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ResizeElementsPanel.class, "ACSD_HeightLabel_Description")); // NOI18N
        widthLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ResizeElementsPanel.class, "ACSD_WidthLabel_Name")); // NOI18N
        widthLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ResizeElementsPanel.class, "ACSD_WidthLabel_Description")); // NOI18N
        statusLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ResizeElementsPanel.class, "ACSD_STATUS_InvalidDimensions_Name")); // NOI18N
        statusLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ResizeElementsPanel.class, "ACSD_STATUS_InvalidDimensions_Description")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void widthTextFocusGained(java.awt.event.FocusEvent evt)//GEN-FIRST:event_widthTextFocusGained
    {//GEN-HEADEREND:event_widthTextFocusGained
        widthText.selectAll();
    }//GEN-LAST:event_widthTextFocusGained

    private void heightTextFocusGained(java.awt.event.FocusEvent evt)//GEN-FIRST:event_heightTextFocusGained
    {//GEN-HEADEREND:event_heightTextFocusGained
        heightText.selectAll();
    }//GEN-LAST:event_heightTextFocusGained

private void widthText1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_widthText1FocusGained
// TODO add your handling code here:
}//GEN-LAST:event_widthText1FocusGained

    public int getResizeHeight()
    {
        return resizeHeight;
    }

    public int getResizeWidth()
    {
        return resizeWidth;
    }
    
    public void insertUpdate(DocumentEvent evt)
    {
        updateSizeFields(evt);
    }

    public void removeUpdate(DocumentEvent evt)
    {
        updateSizeFields(evt);
    }

    public void changedUpdate(DocumentEvent evt)
    {
        updateSizeFields(evt);
    }

    private void updateSizeFields(DocumentEvent evt)
    {
        Document doc = evt.getDocument(); 
        
        if (doc == heightText.getDocument())
        {
            if (validNumber(heightText.getText()))
                resizeHeight = Integer.valueOf(heightText.getText()).intValue();
            
            else
                resizeHeight = 0;
    
            updateValidStatus();
        }
        
        else if (doc == widthText.getDocument())
        {
            if (validNumber(widthText.getText()))
                resizeWidth = Integer.valueOf(widthText.getText()).intValue();
            
            else
                resizeWidth = 0;

            updateValidStatus();
        }
    }


    private boolean validNumber(String dimStrVal)
    {
        if (dimStrVal == null || dimStrVal.length() == 0)
            return false;
            
        try
        {
            return (Integer.valueOf(dimStrVal) > 0);
        }
        
        catch (NumberFormatException ex)
        {
           return false;
        }
    }
    
    private boolean updateValidStatus()
    {
        enableOk = true;
        statusMsg = " "; // NOI18N
        
        if (resizeHeight < 5 || resizeWidth < 5)
        {
           enableOk = Boolean.FALSE;
           statusMsg = "MSG_STATUS_InvalidDimensions"; // NOI18N

           return notifyPropertyListeners();
        }
        
        return notifyPropertyListeners();
    }
    
    private boolean notifyPropertyListeners()
    {
        firePropertyChange(
            NotifyDescriptor.PROP_VALID, !enableOk, enableOk);
        
        if (!statusMsg.equals(" ")) // NOI18N
            statusMsg = NbBundle.getMessage(
                ResizeElementsPanel.class, statusMsg);
        
        statusLabel.setText(statusMsg);
        return enableOk;
    }

    public void vetoableChange(PropertyChangeEvent evt) 
        throws PropertyVetoException
    {
        if (evt.getSource() == heightText)
        {
            if (heightText.getText() != null && 
                heightText.getText().length() > 0)
            {
                if (!validNumber(heightText.getText()))
                    throw new PropertyVetoException(
                        "Must be positive, whole number less then 2147483647", evt); // NOI18N
            }
        }
        
        else if (evt.getSource() == widthText)
        {
            if (widthText.getText() != null && 
                widthText.getText().length() > 0)
            {
                if (!validNumber(widthText.getText()))
                    throw new PropertyVetoException(
                        "Must be positive, whole number less then 2147483647", evt); // NOI18N
            }
        }
    }

    public void actionPerformed(ActionEvent e)
    {
    }

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel heightLabel;
    private javax.swing.JTextField heightText;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel widthLabel;
    private javax.swing.JTextField widthText;
    private javax.swing.JTextField widthText1;
    // End of variables declaration//GEN-END:variables
   
}
