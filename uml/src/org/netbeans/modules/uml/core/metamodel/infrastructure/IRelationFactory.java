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


package org.netbeans.modules.uml.core.metamodel.infrastructure;

import java.util.ArrayList;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationReference;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IReference;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IDerivation;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDirectedRelationship;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IAutonomousElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.AssociationKindEnum;
import org.netbeans.modules.uml.core.metamodel.structure.IAssociationClass;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IRelationFactory 
{
	///*[out]*/IInterface** outSupplier, [out] IDependency dep 
	public ETPairT<IInterface, IDependency> createImplementation(INamedElement client, INamedElement supplier, INamespace space); 
	public IPresentationReference createPresentationReference(IElement referencingElement, IPresentationElement referredElement);
	public IReference createReference(IElement referencingElement,IElement referredElement);
	
	public IGeneralization createGeneralization(IClassifier parent, IClassifier child); 
	public IAssociation createAssociation(IClassifier start, IClassifier end, INamespace space);
	public IAssociation createAssociation2(IClassifier start, IClassifier end, int kind, boolean startNavigable, boolean endNavigable, INamespace space); 
	public IAssociationClass createAssociationClass(IClassifier start, IClassifier end, int kind, boolean startNavigable, boolean endNavigable, INamespace space);
	
	public IDependency createDependency(INamedElement client, INamedElement supplier, INamespace space); 
	public IDependency createDependency2(INamedElement client, INamedElement supplier, String depType, INamespace space);
	
	/*
	 TODO: Create IRelationProxies
	 */
	public ETList<IRelationProxy> determineCommonRelations(ETList<IElement> elements); 
	public ETList<IRelationProxy> determineCommonRelations2(String pDiagramXMIID, ETList<IElement> elements); 
	public ETList<IRelationProxy> determineCommonRelations3(ETList<IElement> elements, ETList<IElement> elementsOnDiagram);

	public IDerivation createDerivation(IClassifier instanciation, IClassifier actualTemplate);
	public IDirectedRelationship createImport(IElement importingElement, IAutonomousElement elementToImport);
}
