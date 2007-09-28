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



package org.netbeans.modules.uml.ui.products.ad.viewfactory;

import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineArrowheadKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETArrowHead;
import com.tomsawyer.editor.TSEColor;
import com.tomsawyer.editor.graphics.TSEGraphics;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;

import java.awt.Color;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.GeneralPath;

/*
 * 
 * @author KevinM
 * 
 *  < ><-----
 *
 */
public class ETUnFilledDiamondNavigable extends ETUnFilledDiamond
{

	IETArrowHead m_pUnfilledArrow = ETArrowHeadFactory.create(DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW);
	
	public ETUnFilledDiamondNavigable()
	{
		super(DrawEngineArrowheadKindEnum.DEAK_UNFILLEDDIAMOND_NAVIGABLE);
	}

	protected ETUnFilledDiamondNavigable(int kind)
	{
		super(kind);
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETArrowHead#draw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, com.tomsawyer.util.TSConstPoint, com.tomsawyer.util.TSConstPoint, com.tomsawyer.editor.TSEColor)
	 */
	public boolean draw(IDrawInfo pInfo, TSConstPoint fromPt, TSConstPoint toPt, TSEColor color)
	{		
		// Draw the Diamond
		Shape diamondShape = super.getShape(pInfo, fromPt, toPt);
		if (diamondShape != null)
		{
			TSEGraphics graphics = pInfo.getTSEGraphics();
			Color prevColor = graphics.getColor();
			Color fillColor = this.isFilled() ? color.getColor() : Color.WHITE;
			graphics.setColor(fillColor);
			graphics.fill(diamondShape);
			graphics.setColor(color);
			graphics.draw(diamondShape);
			// Now Draw the arrowHead.			
			Shape arrowShape = m_pUnfilledArrow.getShape(pInfo, fromPt, getDiamondTip(pInfo, fromPt, toPt));
			if (arrowShape != null)
			{
				graphics.draw(arrowShape);
			}
			graphics.setColor(prevColor);
			return true;
		}
		return false;
	}

	/*
	 * Returns the from tip of the diamond used to calculate the to point of the unfilled arrow.
	 */
	protected TSConstPoint getDiamondTip(IDrawInfo pInfo, TSConstPoint fromPt, TSConstPoint toPt)
	{
		TSTransform transform = pInfo.getTSTransform();
		double fromX = fromPt.getX();
		double fromY = fromPt.getY();
		double toX = toPt.getX();
		double toY = toPt.getY();

		float edgeVectorX = transform.widthToDevice(toX - fromX);
		float edgeVectorY = transform.heightToDevice(toY - fromY);
		float edgeLength = (float) Math.sqrt((edgeVectorX * edgeVectorX) + (edgeVectorY * edgeVectorY));

		// avoid divide-by-zero.

		if (edgeLength == 0.0f)
		{
			return null;
		}

		int lineFromX = transform.xToDevice(fromX);
		int lineFromY = transform.yToDevice(fromY);
		int lineToX = transform.xToDevice(toX);
		int lineToY = transform.yToDevice(toY);

		float halfArrowWidth = transform.widthToDevice(this.getWidth()) / 2;
		float arrowHeight = transform.heightToDevice(this.getHeight());

		// normalize the edge vector components.

		edgeVectorX /= edgeLength;
		edgeVectorY /= edgeLength;

		// calculate the vector from the center of the base to
		// the arrow tip

		float arrowHeightX = arrowHeight * edgeVectorX;
		float arrowHeightY = arrowHeight * edgeVectorY;

		// calculate the vector from the base center point to
		// the edge of the base

		float halfBaseVectorX = halfArrowWidth * edgeVectorY;
		float halfBaseVectorY = halfArrowWidth * edgeVectorX;
		return transform.pointToWorld(new Point(lineToX - (int) (arrowHeightX * 2), lineToY + (int) (arrowHeightY * 2)));
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IETArrowHead#getShape(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, com.tomsawyer.util.TSConstPoint, com.tomsawyer.util.TSConstPoint)
	 */
	public GeneralPath getShape(IDrawInfo pInfo, TSConstPoint fromPt, TSConstPoint toPt)
	{
		GeneralPath diamond = super.getShape(pInfo, fromPt, toPt);
		
		TSConstPoint newToPt = getDiamondTip(pInfo, fromPt, toPt);
		Shape UnfilledPath = m_pUnfilledArrow.getShape(pInfo, fromPt, newToPt);
		GeneralPath finalShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

		if (finalShape != null && diamond != null && UnfilledPath != null)
		{
			// Build the final comp, object.
			finalShape.append(diamond, false);
			finalShape.append(UnfilledPath, false);
		}
		return finalShape;
	}

}
