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

import java.awt.Rectangle;

import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import com.tomsawyer.editor.graphics.TSEGraphics;
import com.tomsawyer.graph.TSGraphObject;

/**
 * Base class for those objects that deal with transformations.
 *
 * @author Trey Spiva
 */
public abstract class ETTransformOwner extends ETTransform {
	public abstract TSGraphObject getOwnerGraphObject();

	/** 
	 * Returns the rectangle in diagram logical coordinates that this draw 
	 * engine's node occupies
	 */
	public IETRect getBoundingRect() {
		return getWinClientRect();
	}

	/**
	 * Returns the rectangle in diagram logical coordinates that this draw 
	 * engine's node occupies
	 * 
	 * @return The logical bounding rectangle.
	 */
	public IETRect getLogicalBoundingRect() {
		return getTSAbsoluteRect();
	}

	/*
	 * Returns the rectangle in device coordinates, on the owner graph window including zoom.
	 * @return The device bounding rectangle.
	 */
	public IETRect getDeviceBoundingRect() {
		IETGraphObject object = this.getObject();
		return object != null ? new ETDeviceRect(getTransform().boundsToDevice(object.getBounds())) : null;
	}

	/*
	 * Returns the Device Rectangle on the owner graph window graph window including the zoom
	 */
	public Rectangle getDeviceBoundingRectangle() {
		IETRect deviceBounds = getDeviceBoundingRect();
		return deviceBounds != null ? deviceBounds.getRectangle() : null;
	}
	
	/*
	 * Retruns the Device Rectangle on the Given Device
	 */
	public Rectangle getDeviceBoundingRectangle(TSEGraphics graphics) 
	{
		IETRect deviceBounds = getDeviceBounds(graphics);
		return deviceBounds != null ? deviceBounds.getRectangle() : null;
	}
	
	/*
	 * Retruns the Device Rectangle on the Given Device
	 */
	public Rectangle getDeviceBoundingRectangle(IDrawInfo drawInfo) 
	{
		IETRect deviceBounds = getDeviceBounds(drawInfo);
		return deviceBounds != null ? deviceBounds.getRectangle() : null;
	}
	
	/*
	 *  Returns the device bounding rectangle, for the given device, may or may not by the GraphWindow.
	 */
	public IETRect getDeviceBoundingRect(IDrawInfo info)
	{
		return this.getDeviceBounds(info);
	}
	
	/*
	 *  Returns the device bounding rectangle, for the given device, may or may not by the GraphWindow.
	 */	
	public IETRect getDeviceBoundingRect(TSEGraphics graphics)
	{
		return this.getDeviceBounds(graphics);
	}
	
	/*
	 * Returns the device bounding rectangle, for the given device, may or may not by the GraphWindow.
	 */
	public IETRect getDeviceBounds(TSEGraphics graphics)
	{
		IETGraphObject object = getObject();
		return graphics != null && object  != null ?
			new ETDeviceRect(graphics.getTSTransform().boundsToDevice(object.getBounds())) :
			null;
	}

	/*
	 * Returns the
	 */
	public IETRect getDeviceBounds(IDrawInfo drawInfo)
	{
		return getDeviceBounds(drawInfo.getTSEGraphics());
	}
	
	public void clearTransformOwner() {
		setGraphObject(null);
	}

	public IETGraphObject getObject()
	{
		return getOwnerGraphObject() instanceof IETGraphObject ? (IETGraphObject)getOwnerGraphObject() : null;
	}
	
	public IETGraphObjectUI getObjectUI()
	{
		IETGraphObject object = getObject();
		return object != null ? object.getETUI() : null;
	}
		
}
