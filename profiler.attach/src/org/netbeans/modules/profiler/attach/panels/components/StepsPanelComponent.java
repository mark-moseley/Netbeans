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

package org.netbeans.modules.profiler.attach.panels.components;

import java.awt.Color;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.UIManager;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.modules.profiler.attach.spi.IntegrationProvider;

/**
 *
 * @author  Jaroslav Bachorik
 */
public class StepsPanelComponent extends javax.swing.JPanel {
  private final ResourceBundle messages = ResourceBundle.getBundle("org/netbeans/modules/profiler/attach/panels/components/StepsPanelComponent"); // NOI18N
  private final String STEP = messages.getString("STEP"); // NOI18N
  private final String WARNINGS = messages.getString("WARNINGS"); // NOI18N
  private final String NOTES = messages.getString("NOTES"); // NOI18N
  private final String WARNING = messages.getString("WARNING"); //NOI18N
  private final String NOTE = messages.getString("NOTE"); // NOI18N
  
  private final String EMPTY_HINT = "<html>\n  <head>\n    \n  </head>\n  <body>\n  </body>\n</html>\n"; // NOI18N
  
  private final int LOCATION_TOP = 1;
  private final int LOCATION_MIDDLE = 2;
  private final int LOCATION_BOTTOM = 3;
  
  private boolean warningsFirst = true;
  private boolean isLabelHidden = false;
  
  private double lastLabelHeight = -1d;
  
  /**
   * Creates new form StepsPanelComponent
   */
  public StepsPanelComponent() {
    initComponents();
  }
  
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        stepsScroller = new javax.swing.JScrollPane();
        areaSteps = new org.netbeans.lib.profiler.ui.components.HTMLTextArea();
        label = new javax.swing.JLabel();
        labelHints = new org.netbeans.lib.profiler.ui.components.HTMLLabel();

        setMaximumSize(new java.awt.Dimension(800, 600));
        setMinimumSize(new java.awt.Dimension(400, 100));
        setPreferredSize(new java.awt.Dimension(400, 200));
        setRequestFocusEnabled(false);
        setLayout(new java.awt.BorderLayout());

        stepsScroller.setMaximumSize(new java.awt.Dimension(800, 600));
        stepsScroller.setMinimumSize(new java.awt.Dimension(300, 100));
        stepsScroller.setPreferredSize(new java.awt.Dimension(380, 100));
        stepsScroller.setViewportView(areaSteps);

        add(stepsScroller, java.awt.BorderLayout.CENTER);

        label.setLabelFor(areaSteps);
        org.openide.awt.Mnemonics.setLocalizedText(label, org.openide.util.NbBundle.getMessage(StepsPanelComponent.class, "StepsPanelComponent.label.text")); // NOI18N
        label.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        add(label, java.awt.BorderLayout.NORTH);

        labelHints.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        labelHints.setText(org.openide.util.NbBundle.getMessage(StepsPanelComponent.class, "StepsPanelComponent.labelHints.text")); // NOI18N
        labelHints.setMaximumSize(new java.awt.Dimension(300, 100));
        labelHints.setMinimumSize(new java.awt.Dimension(300, 0));
        labelHints.setPreferredSize(new java.awt.Dimension(300, 60));
        Color panelBackground = UIManager.getColor("Panel.background"); // NOI18N;
        Color hintBackground = UIUtils.getSafeColor(panelBackground.getRed() - 10, panelBackground.getGreen() - 10, panelBackground.getBlue() - 10);
        labelHints.setDisabledTextColor(Color.darkGray);
        labelHints.setBackground(hintBackground);
        add(labelHints, java.awt.BorderLayout.SOUTH);
        labelHints.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(StepsPanelComponent.class, "StepsPanelComponent.labelHints.AccessibleContext.accessibleName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
  
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.netbeans.lib.profiler.ui.components.HTMLTextArea areaSteps;
    private javax.swing.JLabel label;
    private org.netbeans.lib.profiler.ui.components.HTMLLabel labelHints;
    private javax.swing.JScrollPane stepsScroller;
    // End of variables declaration//GEN-END:variables
  
  /**
   * Getter for property title.
   * @return Value of property title.
   */
  public String getTitle() {
    return label.getText();
  }
  
  /**
   * Setter for property title.
   * @param title New value of property title.
   */
  public void setTitle(String title) {
    org.openide.awt.Mnemonics.setLocalizedText(label, title);
  }
  
  /**
   * Holds value of property steps.
   */
  private IntegrationProvider.IntegrationHints steps;
  
  /**
   * Getter for property steps.
   * @return Value of property steps.
   */
  public IntegrationProvider.IntegrationHints getSteps() {
    return this.steps;
  }
  
  /**
   * Setter for property steps.
   * @param steps New value of property steps.
   */
  public void setSteps(IntegrationProvider.IntegrationHints steps) {
    this.steps = steps;
    
    if (steps == null) {
      areaSteps.setText(""); // NOI18N
      return;
    }
    
    StringBuffer hintText = new StringBuffer();
    if (steps.isWarningsFirst()) {
      appendWarnings(hintText);
    }
    appendSteps(hintText);
    if (!steps.isWarningsFirst()) {
      appendWarnings(hintText);
    }
    appendHints(hintText);
    
    hintText.append("<br>"); // NOI18N
    
    areaSteps.setText(hintText.toString());
    areaSteps.setCaretPosition(0); // scroll to the top of the text
  }
  
  private void appendHints(final StringBuffer hintText) {
    if (this.steps.getHints().size() > 0) {
      exportAsHtmlList(this.steps.getHints(), NOTE, true, hintText);
    }
  }
  
  private void appendWarnings(final StringBuffer hintText) {
    final boolean isLast = !steps.isWarningsFirst() && steps.getHints().size() == 0;
    if (this.steps.getWarnings().size() > 0) {
      exportAsHtmlList(this.steps.getWarnings(), WARNING, "#594FBF", isLast, hintText); // NOI18N
    }
  }
  
  private void appendSteps(final StringBuffer buffer) {
    final boolean isLast = (steps.isWarningsFirst() && steps.getHints().size() == 0) || (steps.getHints().size() == 0 && steps.getWarnings().size() == 0);
    exportAsHtmlList(this.steps.getSteps(), STEP, isLast, buffer);
  }
  
  private void exportAsHtmlList(final List listToExport, final String prefix, final boolean isLast, StringBuffer buffer) {
    exportAsHtmlList(listToExport, prefix, null, isLast, buffer);
  }
  
  private void exportAsHtmlList(final List listToExport, final String prefix, final String color, final boolean isLast, StringBuffer buffer) {
    int stepCounter = 1;
    for (Iterator it = listToExport.iterator(); it.hasNext();) {
      buffer.append("<p>").append("<b>").append(MessageFormat.format(prefix, new Object[]{new Integer(stepCounter++)})).append("</b>").append(" "); // NOI18N
      StringBuffer innerMessage = new StringBuffer();
      innerMessage.append(it.next()).append("</p>"); // NOI18N
      if (color != null) {
        buffer.append("<span style='color:").append(color).append("'>").append(innerMessage).append("</span>"); // NOI18N
      } else {
        buffer.append(innerMessage);
      }
    }
    if (!isLast) {
      buffer.append("<br><hr>"); // NOI18N
    }
  }
  
  /**
   * Getter for property hintText.
   * @return Value of property hintText.
   */
  public String getHintText() {
    return labelHints.getText();
  }
  
  /**
   * Setter for property hintText.
   * @param hintText New value of property hintText.
   */
  public void setHintText(String hintText) {
    labelHints.setText(hintText);
    if (hintText == null || hintText.length() == 0 || labelHints.getText().equals(EMPTY_HINT)) {
      labelHints.setVisible(false);
    } else {
      labelHints.setVisible(true);
    }
    revalidate();
  }
}
