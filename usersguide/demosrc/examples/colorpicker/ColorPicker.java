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

package examples.colorpicker;

/** This class is an entry point of the color picker sample application.
 * It creates and shows the main application frame.
 */
public class ColorPicker extends javax.swing.JFrame {

    /** Color Picker constructor.
     * It initializes all GUI components.
     */
    public ColorPicker() {
        initComponents();
        pack();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        sliderPanel = new javax.swing.JPanel();
        redSlider = new javax.swing.JSlider();
        greenSlider = new javax.swing.JSlider();
        blueSlider = new javax.swing.JSlider();
        colorPreviewPanel = new javax.swing.JPanel();
        colorPreview1 = new examples.colorpicker.ColorPreview();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        sliderPanel.setLayout(new javax.swing.BoxLayout(sliderPanel, javax.swing.BoxLayout.Y_AXIS));

        redSlider.setMaximum(255);
        redSlider.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(null, new java.awt.Color(134, 134, 134)), "Red"));
        redSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                redSliderStateChanged(evt);
            }
        });

        sliderPanel.add(redSlider);
        redSlider.getAccessibleContext().setAccessibleName("Red Slider");
        redSlider.getAccessibleContext().setAccessibleDescription("Red slider.");

        greenSlider.setMaximum(255);
        greenSlider.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(null, new java.awt.Color(134, 134, 134)), "Green"));
        greenSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                greenSliderStateChanged(evt);
            }
        });

        sliderPanel.add(greenSlider);
        greenSlider.getAccessibleContext().setAccessibleName("Green Slider");
        greenSlider.getAccessibleContext().setAccessibleDescription("Green slider.");

        blueSlider.setMaximum(255);
        blueSlider.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(null, new java.awt.Color(134, 134, 134)), "Blue"));
        blueSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                blueSliderStateChanged(evt);
            }
        });

        sliderPanel.add(blueSlider);
        blueSlider.getAccessibleContext().setAccessibleName("Blue Slider");
        blueSlider.getAccessibleContext().setAccessibleDescription("Blue slider.");

        getContentPane().add(sliderPanel, java.awt.BorderLayout.NORTH);

        colorPreviewPanel.setLayout(new java.awt.BorderLayout());

        colorPreviewPanel.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(null, new java.awt.Color(134, 134, 134)), "Color Preview"));
        colorPreviewPanel.add(colorPreview1, java.awt.BorderLayout.CENTER);
        colorPreview1.getAccessibleContext().setAccessibleName("Color Preview Component");
        colorPreview1.getAccessibleContext().setAccessibleDescription("Color preview component.");

        getContentPane().add(colorPreviewPanel, java.awt.BorderLayout.CENTER);
        colorPreviewPanel.getAccessibleContext().setAccessibleName("Color Preview Panel");
        colorPreviewPanel.getAccessibleContext().setAccessibleDescription("Color preview panel.");

    }//GEN-END:initComponents

    /** This method is called when blue slider position is changed.
     * It sets the current blue color value.
     * @param evt ChangeEvent instance passed from stateChanged event.
     */
    private void blueSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_blueSliderStateChanged
        colorPreview1.setBlue(blueSlider.getValue());
    }//GEN-LAST:event_blueSliderStateChanged

    /** This method is called when green slider position is changed.
     * It sets the current green color value.
     * @param evt ChangeEvent instance passed from stateChanged event.
     */
    private void greenSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_greenSliderStateChanged
        colorPreview1.setGreen(greenSlider.getValue());
    }//GEN-LAST:event_greenSliderStateChanged

    /** This method is called when red slider position is changed.
     * It sets the current red color value.
     * @param evt ChangeEvent instance passed from stateChanged event.
     */
    private void redSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_redSliderStateChanged
        colorPreview1.setRed(redSlider.getValue());
    }//GEN-LAST:event_redSliderStateChanged

    /** This method is called when the application frame is closed.
     * @param evt WindowEvent instance passed from windowClosing event.
     */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSlider blueSlider;
    private examples.colorpicker.ColorPreview colorPreview1;
    private javax.swing.JPanel colorPreviewPanel;
    private javax.swing.JSlider greenSlider;
    private javax.swing.JSlider redSlider;
    private javax.swing.JPanel sliderPanel;
    // End of variables declaration//GEN-END:variables


    /** Starts the application.
     * @param args Application arguments.
     */    
    public static void main(java.lang.String[] args) {
        new ColorPicker().show();
    }

}
