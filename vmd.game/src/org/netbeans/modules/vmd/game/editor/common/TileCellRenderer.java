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
package org.netbeans.modules.vmd.game.editor.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.modules.vmd.game.model.StaticTile;
import org.netbeans.modules.vmd.game.view.ColorConstants;
import org.openide.util.NbBundle;

public class TileCellRenderer extends JComponent implements ListCellRenderer {

	private static final int MIN_TILE_SIZE = 30;
	
	private int padX;
	private int padY;

	//TODO : replace those colors with the correct color resources from L&F
	private final static Color MOST_COLOR = ColorConstants.COLOR_OUTLINE_SELECTED;
	private final static Color NO_COLOR = ColorConstants.COLOR_OUTLINE_PLAIN;
	private final static Color MEDIUM_COLOR = ColorConstants.COLOR_OUTLINE_HILITE;
	
	private StaticTile tile;
	private boolean isSelected;
	private boolean hasFocus;
	
	public TileCellRenderer(int padX, int padY) {
		this.padX = padX;
		this.padY = padY;
	}
	
	public String getToolTipText(MouseEvent e) {
		return NbBundle.getMessage(TileCellRenderer.class, "TileCellRenderer.tooltip", this.tile.getIndex());
	}
	
	public void paintComponent(Graphics g) {
		Color c = this.isSelected && this.hasFocus ? MOST_COLOR : (this.isSelected ? MEDIUM_COLOR : NO_COLOR);
		g.setColor(c);
		g.fillRect(0, 0, padX, this.getHeight()); //left vertical
		g.fillRect(this.getWidth() - padX, 0, padX, this.getHeight()); //right vertical
		g.fillRect(padX, 0, this.getWidth() - 2 * padX, padY); //top horizontal
		g.fillRect(padX, this.getHeight() - padY, this.getWidth() - 2 * padX, padY); //bottom horizontal
		if (this.tile != null) {
			this.tile.paint((Graphics2D) g, padX, padY, this.getWidth() - 2*padX, this.getHeight() - 2*padY);
		}
		g.setColor(Color.WHITE);
		g.drawRect(0, 0, this.getWidth() -1, this.getHeight() -1);
		this.paintBorder(g);
	}
	
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
		if (value instanceof StaticTile) {
			StaticTile tile = (StaticTile) value;
			this.tile = tile;
			this.isSelected = isSelected;
			this.hasFocus = hasFocus;
			
			int tileW = tile.getWidth();
			int tileH = tile.getHeight();
			
			int width = tileW;
			int height = tileH;
			
			if (tileW < tileH && tileW < MIN_TILE_SIZE) {
				float ratio = (float) MIN_TILE_SIZE / (float) tileW;
				width = MIN_TILE_SIZE;
				height *= ratio;
			}
			else if (tileH < tileW && tileH < MIN_TILE_SIZE) {
				float ratio = (float) MIN_TILE_SIZE / (float) tileH;
				height = MIN_TILE_SIZE;
				width *= ratio;				
			}
			else if (tileH == tileW && tileH < MIN_TILE_SIZE) {
				width = MIN_TILE_SIZE;
				height = MIN_TILE_SIZE;
			}
			
			Dimension dimension = new Dimension(width + 2*padX, height + 2*padY);
			this.setPreferredSize(dimension);
			return this;
		}
		throw new IllegalArgumentException("Only org.netbeans.mobility.game.model.Tile or java.lang.Integer can be rendered."); // NOI18N
	}
}
