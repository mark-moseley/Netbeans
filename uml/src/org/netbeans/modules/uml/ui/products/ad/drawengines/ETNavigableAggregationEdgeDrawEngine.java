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


package org.netbeans.modules.uml.ui.products.ad.drawengines;

import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineArrowheadKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;

public class ETNavigableAggregationEdgeDrawEngine extends ETAggregationEdgeDrawEngine {
	protected int getStartArrowKind() {
		return DrawEngineArrowheadKindEnum.DEAK_UNFILLEDDIAMOND_NAVIGABLE;
	}

	protected int getEndArrowKind() {
		return DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW;
	}

	public void doDraw(IDrawInfo info)
	{
		drawEdge(info, getStartArrowKind(), getEndArrowKind(), this.getLineKind());
	}

	public String getMetaTypeInitString()
	{
		return AG_NA_META_TYPE;
	}
}
