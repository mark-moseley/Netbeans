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

package org.netbeans.modules.vmd.game.preview;

import org.netbeans.modules.vmd.game.model.StaticTile;
import org.netbeans.modules.vmd.game.view.ImagePreviewComponent;


public class StaticTilePreview extends javax.swing.JPanel {
	
	private StaticTile staticTile;
	private ImagePreviewComponent imagePreviewComponent;
	
	/** Creates new form SpritePreview */
	public StaticTilePreview(StaticTile staticTile) {
		this.staticTile = staticTile;
		this.imagePreviewComponent = new ImagePreviewComponent(true);
		this.imagePreviewComponent.setPreviewable(this.staticTile);
		initComponents();
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelSprite = new javax.swing.JLabel();
        textFieldSprite = new javax.swing.JTextField();
        panelImagePreview = new javax.swing.JPanel();
        labelFrames = new javax.swing.JLabel();
        textFieldFrames = new javax.swing.JTextField();
        labelDelay = new javax.swing.JLabel();
        textFieldDelay = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();

        setMinimumSize(new java.awt.Dimension(10, 180));

        labelSprite.setText(org.openide.util.NbBundle.getMessage(StaticTilePreview.class, "StaticTilePreview.labelTileIndex.txt")); // NOI18N

        textFieldSprite.setEditable(false);
        textFieldSprite.setText(Integer.toString(this.staticTile.getIndex()));
        textFieldSprite.setBorder(null);

        panelImagePreview.setBackground(new java.awt.Color(255, 255, 255));
        panelImagePreview.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        this.panelImagePreview.add(this.imagePreviewComponent);
        panelImagePreview.setLayout(new java.awt.BorderLayout());

        labelFrames.setText(org.openide.util.NbBundle.getMessage(StaticTilePreview.class, "StaticTilePreview.labelWidth.txt")); // NOI18N

        textFieldFrames.setEditable(false);
        textFieldFrames.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        textFieldFrames.setText(Integer.toString(this.staticTile.getWidth()));
        textFieldFrames.setBorder(null);

        labelDelay.setText(org.openide.util.NbBundle.getMessage(StaticTilePreview.class, "StaticTilePreview.labelHeight.txt")); // NOI18N

        textFieldDelay.setEditable(false);
        textFieldDelay.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        textFieldDelay.setText(Integer.toString(this.staticTile.getHeight()));
        textFieldDelay.setBorder(null);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(StaticTilePreview.class, "StaticTilePreview.labelImage.txt")); // NOI18N

        jTextField1.setEditable(false);
        jTextField1.setText(this.staticTile.getImageResource().getURL().toString());
        jTextField1.setBorder(null);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(labelFrames))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(textFieldFrames, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(labelDelay, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 44, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(textFieldDelay, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 8, Short.MAX_VALUE))
                            .add(jTextField1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE))
                        .add(24, 24, 24))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, panelImagePreview, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(labelSprite)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(textFieldSprite, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelSprite)
                    .add(textFieldSprite, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panelImagePreview, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelFrames)
                    .add(textFieldFrames, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(textFieldDelay, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(labelDelay))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel jLabel1;
    public javax.swing.JTextField jTextField1;
    public javax.swing.JLabel labelDelay;
    public javax.swing.JLabel labelFrames;
    public javax.swing.JLabel labelSprite;
    public javax.swing.JPanel panelImagePreview;
    public javax.swing.JTextField textFieldDelay;
    public javax.swing.JTextField textFieldFrames;
    public javax.swing.JTextField textFieldSprite;
    // End of variables declaration//GEN-END:variables

}
