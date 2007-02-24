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
 * Created on Jul 15, 2003
 *
 */
package org.netbeans.modules.uml.ui.controls.newdialog;

import java.util.Vector;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.ui.support.NewElementKind;

/**
 * @author sumitabhk
 *
 */
public class NewDialogElementDetails implements INewDialogElementDetails
{
	private String m_Name;
	private INamespace m_Namespace;
	private Vector<IElement> m_AdditionalNamespaces;
	private int /*NewElementKind*/ m_Kind;

	/**
	 * 
	 */
	public NewDialogElementDetails()
	{
		super();
		m_Kind = NewElementKind.NEK_NONE;
	}

	/**
	 * Name of the element.
	 *
	 * @param pVal[out]
	 * 
	 * @return HRESULT
	 */
	public String getName()
	{
		return m_Name;
	}

	/**
	 * Name of the element.
	 *
	 * @param pVal[in]
	 * 
	 * @return HRESULT
	 */
	public void setName(String value)
	{
		m_Name = value;
	}

	/**
	 * The selected namespace.
	 *
	 * @param pVal[out]
	 * 
	 * @return HRESULT
	 */
	public INamespace getNamespace()
	{
		return m_Namespace;
	}

	/**
	 * The selected namespace.
	 *
	 * @param pVal[in]
	 * 
	 * @return HRESULT
	 */
	public void setNamespace(INamespace value)
	{
		m_Namespace = value;
	}

	/**
	 * Add an additional namespace to our list of possible namespaces.
	 *
	 * @param pNamespace[in]
	 * 
	 * @return HRESULT
	 */
	public long addNamespace(INamespace pNamespace)
	{
		if (pNamespace instanceof IElement)
		{
			if (m_AdditionalNamespaces == null)
			{
				m_AdditionalNamespaces = new Vector<IElement>();
			}
			if (m_AdditionalNamespaces != null)
			{
				IElement pElement = (IElement)pNamespace;
				m_AdditionalNamespaces.add(pElement);
			}
		}
		return 0;
	}

	/**
	 * The kind of element to create.
	 *
	 * @param pVal[out]
	 * 
	 * @return HRESULT
	 */
	public int getElementKind()
	{
		return m_Kind;
	}

	/**
	 * The kind of element to create.
	 *
	 * @param pVal[in]
	 * 
	 * @return HRESULT
	 */
	public void setElementKind(int value)
	{
		m_Kind = value;
	}

}




