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

package org.netbeans.modules.vmd.game.editor.tiledlayer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import javax.swing.JScrollPane;
import org.netbeans.modules.vmd.game.dialog.NewAnimatedTileDialog;
import org.netbeans.modules.vmd.game.model.Layer;
import org.netbeans.modules.vmd.game.model.Tile;
import org.netbeans.modules.vmd.game.model.TiledLayer;
import org.netbeans.modules.vmd.game.model.TiledLayerListener;
/**
 *
 * @author  kaja
 */
public class TiledLayerEditor extends javax.swing.JPanel implements TiledLayerListener, PropertyChangeListener {
	
	private TiledLayer tiledLayer;
	private TiledLayerEditorComponent editorComponent;
	private JScrollPane editorScroll;
	
	/**
     * Creates new form TiledLayerEditor
     */
	public TiledLayerEditor(TiledLayer tiledLayer) {
		this.tiledLayer = tiledLayer;
		this.tiledLayer.addTiledLayerListener(this);
		this.tiledLayer.addPropertyChangeListener(this);
		this.editorComponent = new TiledLayerEditorComponent(this.tiledLayer);
		initComponents();
		this.buttonAddAnimatedTile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new NewAnimatedTileDialog(TiledLayerEditor.this.tiledLayer.getImageResource());
			}
		});
		this.editorScroll.setColumnHeaderView(this.editorComponent.rulerHorizontal);
		this.editorScroll.setRowHeaderView(this.editorComponent.rulerVertical);
		JScrollPane scrollAnimTiles = new JScrollPane(new AnimatedTileList(editorComponent));
		scrollAnimTiles.setBorder(null);
		this.panelAnimatedTiles.add(scrollAnimTiles, BorderLayout.CENTER);
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        textFieldName = new javax.swing.JTextField();
        textFieldCols = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        textFieldRows = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        panelAnimatedTiles = new javax.swing.JPanel();
        buttonAddAnimatedTile = new javax.swing.JButton();

        jLabel1.setText("Tiled Layer:");

        textFieldName.setEditable(false);
        textFieldName.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        textFieldName.setText(this.tiledLayer.getName());
        textFieldName.setBorder(null);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(textFieldName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jLabel1)
                .add(textFieldName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        textFieldCols.setEditable(false);
        textFieldCols.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        textFieldCols.setText(Integer.toString(this.tiledLayer.getColumnCount()));
        textFieldCols.setBorder(null);

        jLabel3.setText("Cols:");

        textFieldRows.setEditable(false);
        textFieldRows.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        textFieldRows.setText(Integer.toString(this.tiledLayer.getRowCount()));
        textFieldRows.setBorder(null);

        jLabel2.setText("Rows:");

        jSplitPane1.setBorder(null);
        jSplitPane1.setResizeWeight(1.0);
        jSplitPane1.setContinuousLayout(true);
        jSplitPane1.setOneTouchExpandable(true);
        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setPreferredSize(new java.awt.Dimension(10000, 10000));
        this.editorScroll = new JScrollPane(this.editorComponent);
        this.editorScroll.getViewport().setBackground(Color.WHITE);
        this.jPanel2.add(this.editorScroll);
        jSplitPane1.setLeftComponent(jPanel2);

        panelAnimatedTiles.setLayout(new java.awt.BorderLayout());

        panelAnimatedTiles.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        buttonAddAnimatedTile.setText("New AnimatedTile");
        panelAnimatedTiles.add(buttonAddAnimatedTile, java.awt.BorderLayout.NORTH);

        jSplitPane1.setRightComponent(panelAnimatedTiles);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                .add(jPanel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel2)
                .add(6, 6, 6)
                .add(textFieldRows, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel3)
                .add(6, 6, 6)
                .add(textFieldCols, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(textFieldCols, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel3)
                        .add(textFieldRows, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel2)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE))
        );
    }
    // </editor-fold>//GEN-END:initComponents
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton buttonAddAnimatedTile;
    public javax.swing.JLabel jLabel1;
    public javax.swing.JLabel jLabel2;
    public javax.swing.JLabel jLabel3;
    public javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    public javax.swing.JSplitPane jSplitPane1;
    public javax.swing.JPanel panelAnimatedTiles;
    public javax.swing.JTextField textFieldCols;
    public javax.swing.JTextField textFieldName;
    public javax.swing.JTextField textFieldRows;
    // End of variables declaration//GEN-END:variables
	
	public void setPaintTile(Tile tile) {
		this.editorComponent.setPaintTileIndex(tile.getIndex());
	}
	
    public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == this.tiledLayer) {
			if (evt.getPropertyName().equals(Layer.PROPERTY_NAME)) {
				textFieldName.setText(this.tiledLayer.getName());
			}
		}
    }

    public void updateTextLabels() {
		textFieldRows.setText(Integer.toString(this.tiledLayer.getRowCount()));
		textFieldCols.setText(Integer.toString(this.tiledLayer.getColumnCount()));
    }

    public void tileChanged(TiledLayer t, int row, int col) {
    }

    public void tilesChanged(TiledLayer t, Set positions) {
    }

    public void columnsInserted(TiledLayer t, int index, int count) {
		this.updateTextLabels();
    }

    public void columnsRemoved(TiledLayer t, int index, int count) {
		this.updateTextLabels();
    }

    public void rowsInserted(TiledLayer t, int index, int count) {
		this.updateTextLabels();
    }

    public void rowsRemoved(TiledLayer t, int index, int count) {
		this.updateTextLabels();
    }

}
