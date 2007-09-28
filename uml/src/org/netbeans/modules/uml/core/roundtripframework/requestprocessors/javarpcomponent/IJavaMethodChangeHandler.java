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

/*
 * Created on Nov 5, 2003
 *
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author aztec
 *
 */
public interface IJavaMethodChangeHandler extends IJavaChangeHandler 
{
	public void handleRequest(IRequestValidator request);
	
   public void transformToEnumeration (IRequestValidator requestValidator, 
                                       IOperation pOperation, 
								               IClassifier pClass, 
                                       boolean doAbstractChange);
                                       
	public void transformToClass (IRequestValidator pRequest, IOperation pOperation, IClassifier pClass, boolean doAbstractChange);
	
	public void transformToInterface(IRequestValidator request, IOperation pOperation, 
										IClassifier pClass, boolean doAbstractChange);
	
	public void deleted(IOperation oper);
	
	public void added(IRequestValidator requestValidator, IOperation pOperation, IClassifier pClass);
	
	public void parameterNameChange(IRequestValidator requestValidator, IParameter pParameter);
	
	public void parameterAdded(IRequestValidator request, IParameter pParameter );
	
	public void parameterChange(IRequestValidator request, IParameter pParameter);
	
	public void parameterChange(IParameter pParameter);
	
	public void abstractChange(IRequestValidator request, IOperation pOperation);
	
	public void abstractChange(IRequestValidator request, IOperation pOperation, IClassifier pClass);
	
	public void nameChange(IOperation pOperation);
	
	public void moved(IRequestValidator request, IOperation pOperation, 
					  IClassifier pFromClass, IClassifier pToClass);
					  
	public void parameterDeleted(IOperation pOperation, IParameter pParameter);
	
	public void typeChange(IRequestValidator request, IOperation pOperation);
	
	public void typeChange(IOperation pOperation);
	
	public void addDependency ( IRequestValidator request, IOperation pOperation );
	
	public void addDependency ( IRequestValidator request, IOperation pOperation, IClassifier pDependentClass );
	
	public void addDependency ( IRequestValidator request, IParameter pParameter, IClassifier pDependentClass );
	
	public void addDependency ( IRequestValidator request, IParameter pParameter, IOperation pOperationOfDependentClass );

	public void deleteList(ETList<IOperation> ops, boolean queryOnce);
}



