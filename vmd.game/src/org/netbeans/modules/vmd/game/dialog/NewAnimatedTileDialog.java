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

package org.netbeans.modules.vmd.game.dialog;

import org.netbeans.modules.vmd.game.model.AnimatedTile;
import org.netbeans.modules.vmd.game.model.ImageResource;
import org.netbeans.modules.vmd.game.model.Tile;
import org.netbeans.modules.vmd.game.view.main.MainView;

/**
 *
 * @author kaja
 */
public class NewAnimatedTileDialog extends AbstractNamingDialog {
	
	private ImageResource imageResource;
	
	/** Creates a new instance of NewAnimatedTileDialog */
	public NewAnimatedTileDialog(ImageResource imgRes) {
		this.imageResource = imgRes;
	}

	protected String getInitialStateDescriptionText() {
		return "Enter a Animated Tile name.";
	}
	
	protected String getNameLabelText() {
		return "Animated Tile name:";
	}
	
	protected String getDialogNameText() {
		return "Create a new Animated Tile";
	}
	
	protected String getCurrentStateErrorText() {
		String errMsg = null; 
		String seqName = this.fieldName.getText();
		
		if (this.imageResource.getAnimatedTileByName(seqName) != null) {
			errMsg = "Animated Tile name already exists.";
		}
		return errMsg;
	}
	
	protected void handleOKButton() {
		AnimatedTile tile = this.imageResource.createAnimatedTile(this.fieldName.getText(), Tile.EMPTY_TILE_INDEX);
		MainView.getInstance().requestEditing(tile);
		this.frame.dispose();
	}

}
