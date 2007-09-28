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

package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IREOperation extends IREClassFeature, IREHasMultiplicity
{
	/**
	 * Specifies whether the operation must be defined by a descendent. True indicates that the operation must be defined by a descendent. False indicates that a descendent is not required to define the operation.
	*/
	public boolean getIsAbstract();

	/**
	 * Retrieves the operations parameters
	*/
	public ETList<IREParameter> getParameters();

	/**
	 * Specifies if the attribute is a primitive attribute or an object instance.
	*/
	public boolean getIsPrimitive();

	/**
	 * Specifies if the operation is a constructor of the owner class.
	*/
	public boolean getIsConstructor();

	/**
	 * Creates a data-wise clone of this IREOperation and returns it in pOperation (an IOperation object).  Does not add pOperation to pClassifier (an IClassifier object).
	*/
	public IOperation clone( IClassifier pClassifier );

	/**
	 * Returns a list of names of Exceptions that this operation may raise.
	*/
	public IStrings getRaisedExceptions();

	/**
	 * Specific to the Java language.
	*/
	public boolean getIsStrictFP();

	/**
	 * Specific to the Java language.
	*/
	public boolean getIsNative();

	/**
	 * property Concurrency
	*/
	public int getConcurrency();
    
    public ETList<IREMultiplicityRange> getMultiplicity();
 
    public void setMultiplicity(ETList<IREMultiplicityRange> mul);
}