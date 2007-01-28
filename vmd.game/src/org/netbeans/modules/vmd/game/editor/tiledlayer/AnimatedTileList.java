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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.MouseAdapter;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.vmd.game.model.AnimatedTile;
import org.netbeans.modules.vmd.game.model.ImageResource;
import org.netbeans.modules.vmd.game.model.ImageResourceListener;
import org.netbeans.modules.vmd.game.model.Sequence;
import org.netbeans.modules.vmd.game.model.SequenceContainer;
import org.netbeans.modules.vmd.game.model.SequenceContainerListener;
import org.netbeans.modules.vmd.game.model.Tile;
import org.netbeans.modules.vmd.game.model.TileTransferable;
import org.netbeans.modules.vmd.game.view.main.MainView;
/**
 *
 * @author kherink
 */
public class AnimatedTileList extends JList {
	
	public static final boolean DEBUG = false;
	
	private TiledLayerEditorComponent editorComponent;
	private ImageResource imageResource;
	private AnimatedTileListDataModel model;
	
	/** Creates a new instance of AnimatedTileList */
	public AnimatedTileList(TiledLayerEditorComponent editorComponent) {
		this.editorComponent = editorComponent;
		this.imageResource = this.editorComponent.getTiledLayer().getImageResource();
		this.model = new AnimatedTileListDataModel();
		this.init();
		this.imageResource.addImageResourceListener(model);
		this.setModel(model);
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.setCellRenderer(new AnimatedTileListCellRenderer());
		this.setMinimumSize(new Dimension(this.imageResource.getCellWidth(), 20));
		this.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;
				AnimatedTileList.this.updatePaintTile();
			}
		});
		this.addFocusListener(new FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
               AnimatedTileList.this.updatePaintTile();
            }
		});
		this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() >= 2) {
					AnimatedTile tile = (AnimatedTile) AnimatedTileList.this.getSelectedValue();
					MainView.getInstance().requestEditing(tile);
				}
            }
		});
		//DnD
		DragSource dragSource = new DragSource();
		DragGestureRecognizer dragGestureRecognizer = dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, new DGL());

	}
	
	private void updatePaintTile() {
		Tile tile = (Tile) this.getSelectedValue();
		if (tile == null)
			return;
		MainView.getInstance().requestPreview(tile);
		this.editorComponent.setPaintTileIndex(tile.getIndex());
	}
	
	private void init() {
		List animatedTiles = this.imageResource.getAnimatedTiles();
		for (Iterator iter = animatedTiles.iterator(); iter.hasNext();) {
			AnimatedTile tile = (AnimatedTile) iter.next();
			tile.addSequenceContainerListener(this.model);
			this.model.addElement(tile);
		}
	}

	public Dimension getMaximumSize() {
		return this.getPreferredSize();
	}

	private class DGL extends DragSourceAdapter implements DragGestureListener {

		public void dragGestureRecognized(DragGestureEvent dge) {
			Point dragOrigin = dge.getDragOrigin();
			int row = AnimatedTileList.this.locationToIndex(dragOrigin);
			Tile tile = (Tile) AnimatedTileList.this.getModel().getElementAt(row);
			TileTransferable payload = new TileTransferable();
			payload.getTiles().add(tile);
			dge.startDrag(null, payload, this);
		}
		
		public void dragDropEnd(DragSourceDropEvent dsde) {
			super.dragDropEnd(dsde);
			if (dsde.getDropSuccess()) {
				if (DEBUG) System.out.println("Drop successful");
			}
			else {
				if (DEBUG) System.out.println("Drop unsuccessful");				
			}
		}	
	}

	private class AnimatedTileListDataModel extends DefaultListModel implements ImageResourceListener, SequenceContainerListener {
				
		//ImageResourceListener
		public void animatedTileAdded(ImageResource source, AnimatedTile tile) {
			this.addElement(tile);
			tile.addSequenceContainerListener(this);
		}
		public void animatedTileRemoved(ImageResource source, AnimatedTile tile) {
			tile.removeSequenceContainerListener(this);
			this.removeElement(tile);
		}

		public void sequenceAdded(ImageResource source, Sequence sequence) {
		}

		public void sequenceRemoved(ImageResource source, Sequence sequence) {
		}

		//SequenceContainerListener
        public void sequenceAdded(SequenceContainer source, Sequence sequence, int index) {
			int tileIndex = this.indexOf(source);
			this.fireContentsChanged(this, tileIndex, tileIndex);
        }

        public void sequenceRemoved(SequenceContainer source, Sequence sequence, int index) {
			int tileIndex = this.indexOf(source);
			this.fireContentsChanged(this, tileIndex, tileIndex);
        }

        public void sequenceMoved(SequenceContainer source, Sequence sequence, int indexOld, int indexNew) {
			int tileIndex = this.indexOf(source);
			this.fireContentsChanged(this, tileIndex, tileIndex);
        }
	}
}
