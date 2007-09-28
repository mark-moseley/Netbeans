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


package org.netbeans.modules.uml.core.metamodel.diagrams;

public interface IDiagramValidation
{
	/**
	 * Reset the validation and response flags.  Done on create automatically.
	*/
	public void reset();

	/**
	 * Should we validate nodes?
	*/
	public boolean getValidateNodes();

	/**
	 * Should we validate nodes?
	*/
	public void setValidateNodes( boolean value );

	/**
	 * Should we validate links?
	*/
	public boolean getValidateLinks();

	/**
	 * Should we validate links?
	*/
	public void setValidateLinks( boolean value );

	/**
	 * Add something that needs validation
	*/
	public void addValidationKind( /* DiagramValidateKind */ int nKind );

	/**
	 * Remove an item from validation
	*/
	public void removeValidationKind( /* DiagramValidateKind */ int nKind );

	/**
	 * Should we validate this item?
	*/
	public boolean getValidationKind( /* DiagramValidateKind */ int nKind );

	/**
	 * Create a IGraphObjectValidation based on the kinds in this object
	*/
	public IGraphObjectValidation createGraphObjectValidation();

	/**
	 * Add an action we should take after validation has finished.
	*/
	public void addValidationResponse( /* DiagramValidateResponse */ int nKind );

	/**
	 * Remove a response
	*/
	public void removeValidationResponse( /* DiagramValidateResponse */ int nKind );

	/**
	 * Get a validation response.  What should the diagram do after the validation has completed?
	*/
	public boolean getValidationResponse( /* DiagramValidateResponse */ int nKind );

}
