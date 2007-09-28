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

import java.awt.Point;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETDrawEngineFactory;
import org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.openide.util.NbBundle;

/**
 * @author Embarcadero Technologies Inc.
 *
 * 
 */
public class ETRedefinedOperationListCompartment extends ETOperationListCompartment implements IADRedefinedOperationListCompartment{

    public static ElementLocator locator = new ElementLocator();
    private IElement element;
    
	public ETRedefinedOperationListCompartment() {
		super();
		this.init();
	}

	public ETRedefinedOperationListCompartment(IDrawEngine pDrawEngine) {
		super(pDrawEngine);
		this.init();
	}

	private void init() {
		this.initResources();
	}

	public void initResources() {
		super.initResources();
//		this.setName("Redefined Operations");
	}

	// TODO need to use IElement instead of ICompartment
	public boolean handleLeftMouseDrop(IETPoint pCurrentPos, List pCompartments, boolean bMoving) {
		boolean eventHandled = false;
		int insertionPoint = -1;

		if (this.getReadOnly())
			return true;

		INodeDrawEngine nodeDrawEngine = (INodeDrawEngine) this.getEngine();
		IClassifier targetClassifier = nodeDrawEngine.getParentClassifier();

		ICompartment targetCompartment = this.getCompartmentAtPoint(pCurrentPos);

		if (targetCompartment != null) {
			insertionPoint = this.getCompartmentIndex(targetCompartment);
		}

		Iterator iterator = pCompartments.iterator();
		while (iterator.hasNext()) {
			ICompartment sourceCompartment = (ICompartment) iterator.next();

			// Insert only compartments of the same kind
			if (sourceCompartment instanceof ETClassOperationCompartment) {
				try {

					IElement sourceElement = sourceCompartment.getModelElement();

					if (sourceElement instanceof IFeature) {

						IFeature sourceFeature = (IFeature) sourceElement;

						// check if we're dropping on ourselves
						IClassifier sourceClassifier = sourceFeature.getFeaturingClassifier();

						// dropping on ourselves, perform an index move instead
						if (sourceClassifier.getXMIID().equals(targetClassifier.getXMIID())) {
							ICompartment foundCompartment = this.findCompartmentContainingElement(sourceElement);
							this.moveCompartment(foundCompartment,insertionPoint, false);

						} else {
							if (bMoving) {
								sourceFeature.moveToClassifier(targetClassifier);
								// refresh source node affected by the move operation
								sourceCompartment.getEngine().init();
								sourceCompartment.getEngine().invalidate();
							} else {
								sourceFeature.duplicateToClassifier(targetClassifier);
							}
							//refresh the target node affected by the move/copy operation;
							this.getEngine().init();
							this.getEngine().invalidate();
						}
					}
					eventHandled = true;
				} catch (ETException e) {
					e.printStackTrace();
				}
			}
		}

		return eventHandled;
	}

	public void addModelElement(IElement pElement, int pIndex) {
		try {
			ICompartment newCompartment = ETDrawEngineFactory.createCompartment(ETDrawEngineFactory.CLASS_OPERATION_COMPARTMENT);
			if (newCompartment != null) {
				newCompartment.setEngine(this.getEngine());
				newCompartment.addModelElement(pElement, -1);
				this.addCompartment(newCompartment, pIndex, false);
			}
		} catch (ETException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called when the context menu is about to be displayed.  The compartment should add whatever buttons
	 * it might need.
	 *
	 * @param pContextMenu[in] The context menu about to be displayed
	 * @param logicalX[in] The logical x location of the context menu event
	 * @param logicalY[in] The logical y location of the context menu event
	 */
	public void onContextMenu(IMenuManager manager)
	{
		 if (getEnableContextMenu())
		 {
			 Point point = manager.getLocation();
                         
                         // (LLS) Adding the buildContext logic to support A11Y issues.  The
                         // user should be able to use the CTRL-F10 keystroke to activate
                         // the context menu.  In the case of the keystroke the location
                         // will not be valid.  Therefore, we have to just check if the
                         // compartment is selected.
                         //
                         // A list compartment can not be selected.  Therefore, when
                         // CTRL-F10 is pressed, we must always show the list compartment
                         // menu items.
                         boolean buildMenu = true;
                         if(point != null)
                         {
                             buildMenu = containsPoint(point);
                         }
                         
                         if (buildMenu == true)
			 {
				 int count = getNumCompartments();
				 for (int i=0; i<count; i++)
				 {
					 ICompartment pComp = getCompartment(i);
					 pComp.onContextMenu(manager);
				 }
   				
   				 //allow only delete of redefined operations.
				 manager.add(createMenuAction(loadString("IDS_POPUP_DELETE_OPERATION"), "MBK_DELETE_OPERATION"));
			 }
		 }
	}

	/**
	 * This is the name of the drawengine used when storing and reading from the product archive.
	 *
	 * @param sID[out,retval] The unique name for this compartment.  Used when reading and writing the
	 * product archive (etlp file).
	 */
	public String getCompartmentID()
	{
		return "ADRedefinedOperationListCompartment";
	}
        
        public String getName()
        {
            if (getModelElementXMIID()==null || getModelElementXMIID().trim().equals(""))
                return super.getName();
            
            String name = "";
            if (element == null)
                element = locator.findElementByID(getModelElementXMIID());
            if (element != null && element instanceof INamedElement)
                name = ((INamedElement)element).getNameWithAlias();
            
            return NbBundle.getMessage(ETRedefinedOperationListCompartment.class,
                    "IDS_REDEFINED_OPERATIONS", name);
        }      
}
