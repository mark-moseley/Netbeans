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



package org.netbeans.modules.uml.ui.swing.drawingarea;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.dom.DOMDocument;
import java.util.List;

/**
 * @author KevinM
 *
 * This class hides the complexity of looking up valid ElementType Diagram Drop Targets.
 */
public class ValidDropTargets {

	/**
	 *
	 */
	protected ValidDropTargets() {
		super();
		fill();
	}
	
	public static ValidDropTargets instance()
	{
		if (m_Instance == null)
		{
			m_Instance = new ValidDropTargets();
		}
		return m_Instance;
	}

	/*
	 * Returns true if the input element type is allowed on the diagram.
	 */
	public boolean isValidDropTarget(String ElementType, String diagramShortName) 
	{
		if (m_ValidDropDiagrams != null)
		{
			String vaildDropTargets = (String) m_ValidDropDiagrams.get(ElementType);
			if (vaildDropTargets != null) 
			{
				return vaildDropTargets.indexOf(diagramShortName) >= 0 || vaildDropTargets.indexOf("ALL") >= 0;
			}
		}
		return false;
	}

	protected void fill() {
		try {
			IConfigManager config = ProductHelper.getConfigManager();

			if (config != null) 
			{
				String location = config.getDefaultConfigLocation();
				location += "ProjectTreeEngine.etc";

				Document pDocument = XMLManip.getDOMDocument(location);
				if (pDocument != null) {
					// Query the displayed items
					String query = new String("//DisplayedItems");

					List pDisplayedItems = pDocument.selectNodes(query);
					if (pDisplayedItems != null) 
					{
						Iterator < Node > domNodeIter = pDisplayedItems.iterator();
						while (domNodeIter.hasNext()) 
						{
							Node pNode = domNodeIter.next();

							Element pElement = pNode instanceof Element ? (Element) pNode : null;
							if (pElement != null) 
							{
								String name = pElement.attributeValue("name");
								String validDropDiagrams = pElement.attributeValue("dragAndDropDiagrams");

								if (name != null && validDropDiagrams != null) 
								{
									if (validDropDiagrams.length() > 0 && !validDropDiagrams.equals(",,")) 
									{
										m_ValidDropDiagrams.put(name, validDropDiagrams);
									}
								}
							}
						}
					}
				} else {
					//  UMLMessagingHelper messageService(_Module.GetModuleInstance(), IDS_MESSAGINGFACILITY);
					//  _VH(messageService.SendWarningMessage(_Module.GetModuleInstance(), IDS_COULDNOTLOADENGINE ) );
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/// Identifies the valid drop diagram for this element type
	/* The element type */ /* Comma delimited list of drop diagrams */;
	protected HashMap m_ValidDropDiagrams = new HashMap();
	private static ValidDropTargets m_Instance = null;

}
