/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

/*
 * Created on Jun 10, 2003
 *
 */
package org.netbeans.modules.uml.designpattern;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * @author sumitabhk
 *
 */
public class RoleCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer
{

	/**
	 *
	 */
	public RoleCellRenderer()
	{
		super();
	}

	public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus,
				int row, int column)
	{
		setFont(table.getFont());

		if (value instanceof String){
			setIcon(null);
			setText((String)value);
		}
		else if (value instanceof Icon){
			setIcon((Icon)value);
			setText(null);
		}
		Color background;
		Color foreground;

		if(isSelected)
		{
			background = table.getSelectionBackground();
			foreground = table.getSelectionForeground();
		}
		else
		{
			background = table.getBackground();
			foreground = table.getForeground();
		}
		Border highlightBorder = null;
		if (hasFocus)
		{
			highlightBorder = UIManager.getBorder
						  ("Table.focusCellHighlightBorder");
		}
		TableCellRenderer tcr = table.getCellRenderer(row, column);
		if (tcr instanceof DefaultTableCellRenderer)
		{
			DefaultTableCellRenderer dtcr = ((DefaultTableCellRenderer)tcr);
			dtcr.setBackground(background);
			dtcr.setForeground(foreground);
		}
		setBorder(new MyBorder());
		return this;
	}

	public class MyBorder extends LineBorder {

		public MyBorder()
		{
			super(Color.GRAY);
		}

		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			Color oldColor = g.getColor();
			int i;

		/// PENDING(klobad) How/should do we support Roundtangles?
			g.setColor(lineColor);

			g.setColor(new Color(232,228,232));
			g.drawLine(x, y, x, y+height);
			g.drawLine(x, y, x+width, y);
			g.drawLine(x+width, y, x+width, y+height);

			g.setColor(oldColor);
		}
	}
}



