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



package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETCompartment;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETNodeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * @author KevinM
 * The ETEllipseCompartment provides drawing support for a compartment that looks like
 * an Box.
 */
public class ETEllipseCompartment extends ETCompartment implements IEllipseCompartment {

	protected int m_ellipseKind = ISupportEnums.EK_UNKNOWN;

	/**
	 * 
	 */
	public ETEllipseCompartment() {
		super();
	}

	/**
	 * @param pDrawEngine
	 */
	public ETEllipseCompartment(IDrawEngine pDrawEngine) {
		super(pDrawEngine);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IEllipseCompartment#getEllipseKind()
	 */
	public int getEllipseKind() {
		return m_ellipseKind;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IEllipseCompartment#setEllipseKind(boolean)
	 */
	public void setEllipseKind(int ellipseKind) {
		m_ellipseKind = ellipseKind;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#calculateOptimumSize(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, boolean)
	 */
	public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct) {
		try {
			IETSize sizeAtOneHundred  = new ETSize(7, 7);
			internalSetOptimumSize(sizeAtOneHundred);
			return pDrawInfo == null ? getOptimumSize(bAt100Pct) : this.scaleSize(sizeAtOneHundred, pDrawInfo.getTSTransform());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Color getBorderColor() {
		INodeDrawEngine drawEngine = this.getEngine() instanceof INodeDrawEngine ? (INodeDrawEngine) getEngine() : null;
		return drawEngine != null ? drawEngine.getBorderColor() : Color.BLACK;
	}

	public Color getBkColor() {
		INodeDrawEngine drawEngine = this.getEngine() instanceof INodeDrawEngine ? (INodeDrawEngine) getEngine() : null;
		return drawEngine != null ? drawEngine.getFillColor() : Color.DARK_GRAY;
	}

	public Color getLightGradientBkColor() {
		ETNodeDrawEngine drawEngine = this.getEngine() instanceof ETNodeDrawEngine ? (ETNodeDrawEngine) getEngine() : null;
		return drawEngine != null ? drawEngine.getLightGradientFillColor() : getBkColor();
	}


	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#draw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, org.netbeans.modules.uml.core.support.umlsupport.IETRect)
	 */
	public void draw(IDrawInfo pDrawInfo, IETRect pBoundingRect) {
		try {
			Rectangle boundingRect = pBoundingRect != null ? pBoundingRect.getRectangle() : null;
			if (pDrawInfo != null && boundingRect != null) {
				// Call the base class first
				super.draw(pDrawInfo, pBoundingRect);

				// perform color overrides
				//			   Color crBorder = GetColorDefaultText( CK_BORDERCOLOR, pDrawInfo.getTSEGraphics().getGraphics() );
				Color crBorder = getBorderColor();
				Color crFill = getBkColor();
                                Color crLightFill = getLightGradientBkColor();

                                float centerX = (float) pBoundingRect.getCenterX();
                                GradientPaint paint = new GradientPaint(centerX, pBoundingRect.getBottom(), crFill, centerX, pBoundingRect.getTop(), crLightFill);
				//	   CBrush* pBrush       = GetBrush( CK_BORDERCOLOR );
				//	   CBrush* pFilledBrush = GetBrush( CK_FILLCOLOR );

				switch (getEllipseKind()) {
					case ISupportEnums.EK_UNKNOWN :
						{
							GDISupport.drawEllipse(pDrawInfo.getTSEGraphics(), boundingRect, crBorder, paint);
							break;
						}
					case ISupportEnums.EK_CIRCLE_INSIDE_CIRCLE_CENTER_FILLED :
						{
							// Draw the outside circle
							GDISupport.drawEllipse(pDrawInfo.getTSEGraphics(), boundingRect, crBorder, Color.WHITE);

							// Make the center circle 1/2 the diameter
							ETRect newRect = new ETRect(boundingRect);

							newRect.deflateRect((int) boundingRect.getWidth() / 4, (int) boundingRect.getHeight() / 4);

							// Make the inner circle border and fill both the fill color
							//				const COLORREF crFill = GetColorDefaultText( CK_FILLCOLOR, pDrawInfo.getTSEGraphics().getGraphics() );
							GDISupport.drawEllipse(pDrawInfo.getTSEGraphics(), newRect, crBorder, paint);
							break;
						}
					case ISupportEnums.EK_CIRCLE_WITH_X :
						{
							// Draw the outside circle
							GDISupport.drawEllipse(pDrawInfo.getTSEGraphics(), boundingRect, crBorder, paint);

							// Draw the x
							Point topLeft = pBoundingRect.getTopLeft();
							Point bottomLeft = pBoundingRect.getBottomLeft();
							Point topRight = pBoundingRect.getTopRight();
							Point bottomRight = pBoundingRect.getBottomRight();

							GDISupport.drawLine(pDrawInfo.getTSEGraphics(), topLeft, bottomRight, crBorder, 1);
							GDISupport.drawLine(pDrawInfo.getTSEGraphics(), topRight, bottomLeft, crBorder, 1);
							break;
						}
					case ISupportEnums.EK_CIRCLE_INSIDE_FILLED :
						{
							// Draw the circle filled
							GDISupport.drawEllipse(pDrawInfo.getTSEGraphics(), boundingRect, crBorder, paint);
							break;
						}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#getCompartmentID()
	 */
	public String getCompartmentID() {
		return "EllipseCompartment";
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#readFromArchive(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive, org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement)
	 */
	public void readFromArchive(IProductArchive pProductArchive, IProductArchiveElement pCompartmentElement) {
		try {
			super.readFromArchive(pProductArchive, pCompartmentElement);
			setEllipseKind((int) pCompartmentElement.getAttributeLong("EllipseKind"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#writeToArchive(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive, org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement)
	 */
	public IProductArchiveElement writeToArchive(IProductArchive pProductArchive, IProductArchiveElement pEngineElement) {
		try {
			IProductArchiveElement pCompartmentElement = super.writeToArchive(pProductArchive, pEngineElement);
			if (pCompartmentElement != null) {
				// Write out our stuff
				pCompartmentElement.addAttributeLong("EllipseKind", this.getEllipseKind());
			}
			return pCompartmentElement;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#initResources()
	 */
	public void initResources() {
		// TODO Auto-generated method stub
		super.initResources();
	}

}

