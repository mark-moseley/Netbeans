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

import java.awt.Color;
import java.awt.event.ActionEvent;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IExtend;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.support.NodeEndKindEnum;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineArrowheadKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineLineKindEnum;

/**
 * @author jingmingm
 *
 */
public class ETExtendEdgeDrawEngine extends ETEdgeDrawEngine
{
	public String getElementType()
	{
		String type = super.getElementType();
		if (type == null)
		{
			type = new String("Extend");
		}
		return type;
	}

	public void doDraw(IDrawInfo drawInfo)
	{
		super.doDraw(drawInfo);

	}
	
	protected int getLineKind() 
	{
		return DrawEngineLineKindEnum.DELK_DASH;	 
	}
	
	protected int getEndArrowKind()
	{
		return DrawEngineArrowheadKindEnum.DEAK_UNFILLEDARROW;
	}

	public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass)
	{
		boolean bFlag = handleStandardLabelSensitivityAndCheck(id, pClass);
		if (!bFlag)
		{
			bFlag = super.setSensitivityAndCheck(id, pClass);
		}
		
		return bFlag;
	}
	
	public boolean onHandleButton(ActionEvent e, String id)
	{
		boolean handled = handleStandardLabelSelection(e, id);
		if (!handled)
		{
			handled = super.onHandleButton(e, id);
		}
		return handled;
	}

	public void onContextMenu(IMenuManager manager)
	{
		// Add the stereotype label pullright
		addStandardLabelsToPullright(StandardLabelKind.SLK_ALL, manager);
		
		super.onContextMenu(manager);
	}

	/**
	 * This is the name of the drawengine used when storing and reading from the product archive
	 *
	 * @param sID A unique identifier for this draw engine.  Used when persisting to the etlp file.
	 */
	public String getDrawEngineID() 
	{
		return "ExtendEdgeDrawEngine";
	}

	/**
	 * Verify the ends are correct
	 */
	public void verifyEdgeEnds()
	{
		IEdgePresentation pPE = getIEdgePresentation();
		IElement modEle = pPE != null ? pPE.getFirstSubject() : null;
		if (modEle instanceof IExtend)
		{
			IExtend pInclude = (IExtend)modEle;
			IUseCase pBase = pInclude.getBase();
			IUseCase pExtension = pInclude.getExtension();
			
			// This edge is a dashed line with an arrowhead on the base end
			int endKind = NodeEndKindEnum.NEK_UNKNOWN;
			endKind = pPE.getNodeEnd(pExtension);
			
			if (endKind == NodeEndKindEnum.NEK_TO)
			{
				if (!isParentDiagramReadOnly())
				{
					// This is wrong.  It ends up with an incorrect parent/child relationship.  
					// Switch it unless the diagram is readonly
					postSwapEdgeEnds();
				}
			}
		}
	}
	
	/**
	 * Is this draw engine valid for the element it is representing?
	 *
	 * @param bIsValid[in] true if this draw engine can correctly represent the attached model element.
	 */
	public boolean isDrawEngineValidForModelElement()
	{
		boolean valid = false;
		String metaType = getMetaTypeOfElement();
		if (metaType != null && metaType.equals("Extend"))
		{
			valid = true;
		}
		return valid;
	}

	/**
	 * Returns the metatype of the label manager we should use
	 *
	 * @param return The metatype in essentialconfig.etc that defines the label manager
	 */
	public String getManagerMetaType(int nManagerKind)
	{
		String sManager = null;
		if (nManagerKind == MK_LABELMANAGER)
		{
			sManager = "SimpleStereotypeAndNameLabelManager";
		}
		return sManager;
	}

	/**
	 * The forced stereotype text on the label manager
	 *
	 * @param return The stereotype text that should appear in a label - readonly
	 */
	protected String getForcedStereotypeText()
	{
		String text = "";
		IElement modEle = getFirstModelElement();
		if (modEle != null)
		{
			String metaType = modEle.getElementType();
			if (metaType.equals("Extend"))
			{
				text = "extend";
			}
		}
		return text;
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
	 */
	public void initResources()
	{
		this.setLineColor("extendedgecolor", Color.BLACK);
		super.initResources();
	}
}
