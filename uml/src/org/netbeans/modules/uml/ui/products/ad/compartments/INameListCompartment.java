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



package org.netbeans.modules.uml.ui.products.ad.compartments;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;

public interface INameListCompartment extends IADListCompartment {

	// This is the name compartment used to lookup into essentialconfig.etc
	public void setNameCompartment(String sNameCompartmentName);

	// Attaches this compartment to a IElement
	public void attach(IElement pElement);

	//Updates the properties, stereotype and package import compartments
	public boolean updateAllOptionalCompartments(IElement pElement);

	// Returns the stereotype compartment that is a child of this list compartment. 
	public IStereotypeCompartment getStereotypeCompartment();

	// Returns the tagged values compartment that is a child of this list compartment. 
	public ITaggedValuesCompartment getTaggedValuesCompartment();

	// Returns the package import compartment that is a child of this list compartment. 
	public IPackageImportCompartment getPackageImportCompartment();

	// Returns the name compartment that is a child of this list compartment. 
	public IADNameCompartment getNameCompartment();

	// TRUE to enable the template parameters compartment in this name list
	public boolean getTemplateParameterCompartmentEnabled();

	// TRUE to enable the template parameters compartment in this name list
	public void setTemplateParameterCompartmentEnabled(boolean pEnabled);

	// TRUE to enable the package import compartment in this name list.  Default is true
	public boolean getPackageImportCompartmentEnabled();

	// TRUE to enable the package import compartment in this name list.  Default is true
	public void setPackageImportCompartmentEnabled(boolean bEnabled);

	// TRUE to resize this node when the associated model element might change.
	public boolean getResizeToFitCompartments();

	// TRUE to resize this node when the associated model element might change.
	public void setResizeToFitCompartments(boolean bResizeToFit);

	// This adds a static text compartment to the top of the name list 
	public void addStaticText(String sStaticText);

	//The template compartment overlaps the compartment below and sticks out to the right. 
	//	HRESULT GetTemplateParameterRect( [in]IDrawInfo* pInfo, 
	//									  [out]IETRect* *pNonTemplateRect, 
	//									  [out]IETRect* *pTemplateRect,
	//									  [out]long* pXOverlap, 
	//									  [out]long* pYOverlap);

	// Overrides the default resource ID for an optional compartment. Allows drawengines to control optional compartment drawing behavior.
	//	HRESULT SetChildDefaultResourceID( [in] OptionalCompartment nCompartmentKind, [in] ResourceIDKind nKind, [in] BSTR sName );

	// Returns the default resource ID for an optional compartment.
	//	HRESULT GetChildDefaultResourceID( [in] OptionalCompartment nCompartmentKind, [in] ResourceIDKind nKind, [out] BSTR* sName );

	//	// Returns the subcompartments that are standard with this type of list compartment 
	//	HRESULT BreakAppartListCompartment([out] IADStaticTextCompartment** pStaticTextCompartment,
	//									   [out] IStereotypeCompartment** pStereotypeCompartment,
	//									   [out] ITaggedValuesCompartment** pTaggedValuesCompartment,
	//									   [out] IPackageImportCompartment** pPackageImportCompartment,
	//									   [out] IADNameCompartment** pNameCompartment);

}
