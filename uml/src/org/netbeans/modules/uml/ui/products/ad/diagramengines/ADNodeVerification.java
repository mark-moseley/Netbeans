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


package org.netbeans.modules.uml.ui.products.ad.diagramengines;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETNode;
import org.netbeans.modules.uml.ui.support.relationshipVerification.NodeVerificationImpl;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
//import com.tomsawyer.util.TSPoint;
import com.tomsawyer.drawing.geometry.TSPoint;

/**
 * 
 * @author Trey Spiva
 */
public class ADNodeVerification extends NodeVerificationImpl
{
   public static int LOCATION_SLOP = 20;
   
   /**
    * Creates the appropriate metatype for this node.  
    * 
    * @param pDiagram The current diagram
    * @param pCreatedNode The node that just got created
    * @param pNamespace The namespace the new node should be in
    * @param metaTypeString The metatype string of the new element
    * @param sInitializationString The initialization string of the node that was just created.
    * @return A pair that contains that conitains the IElement and a 
    *         presentation reference relationship is created between the 
    *         referencing presentation element and the PresentationElement to
    *         be created.  If no referencing presentation element exist then
    *         <code>null</code> will be returned.
    */
   public ETPairT < IElement, IPresentationElement > createAndVerify(IDiagram pDiagram, 
                                                                     ETNode pCreatedNode, 
                                                                     INamespace pNamespace, 
                                                                     String metaTypeString, 
                                                                     String sInitializationString)
   {
      IPresentationElement retPresentation = null; 
      IElement             retElement      = null;
      
      if((pDiagram != null)     && 
         (pCreatedNode != null) && 
         (pNamespace != null)   && 
         (metaTypeString.length() > 0))
      {
//         INamespace nsToUse = pNamespace;
//         
//         IDrawingAreaControl drawingArea = null;
//         if (pDiagram instanceof IDrawingAreaControl)
//         {
//            drawingArea = (IDrawingAreaControl)pDiagram;
//            
//            if(metaTypeString.equals("Port") == true)
//            {
//               TSPoint nearestPoint = new TSPoint(pCreatedNode.getCenter());    
//               retPresentation = getNearestNode(drawingArea, nearestPoint, "ComponentDrawEngine");      
//               
//               if(retPresentation != null)
//               {
//                  IElement pElement = TypeConversions.getElement(retPresentation);
//                  if (pElement instanceof INamespace)
//                  {
//                     nsToUse = (INamespace)pElement;
//                  }                  
//               }
//            }
//         }
//         
//         if(nsToUse != null)
//         {
//            retElement = GraphObjectPresentationFactory.retrieveModelElement(metaTypeString);
//            if (retElement instanceof INamedElement)
//            {
//               nsToUse.addOwnedElement((INamedElement)retElement);               
//            }
//            else
//            {
//               nsToUse.addElement(retElement);
//            }
//         }
//         
//         if(retElement)
//         {
//            postCreate(retElement, sInitializationString);
//         }
      }
      
      return new ETPairT < IElement, IPresentationElement >(retElement, retPresentation);
   }

   /**
    * Verifies that this node is valid at this point.  Fired by the diagram 
    * add node tool.  If the node requires a node to node relationship the
    * actual location will be different then the location passed into the method. 
    * 
    * @param pDiagram The diagram to verify the location.
    * @param location The location to verify.
    * @return <code>true</code> if the location if valid, <code>false</code>
    *         otherwise.
    */
   public boolean verifyCreationLocation(IDiagram pDiagram, ETPoint location)
   {
      return false;
   }

   /**
    * During the creation process this is fired when the node is dragged around.
    * Fired by the diagram add node tool.
    */
   public void verifyDragDuringCreation(IDiagram pDiagram, 
                                        ETNode pCreatedNode, 
                                        ETPoint location)
   {      
   }
   
   //**************************************************
   // Helper Methods
   //**************************************************
   
   /**
    * Returns the nearest node of this type and the x,y location of the 
    * nearest side
    *
    * @param pDiagram The current diagram
    * @param nearestPoint The current location of the node, use this as a 
    *                     return variable to change the location
    * @param drawEngineID The type of draw engine to search for
    * @return The found PE that matches the criteria of an drawEngineID 
    *         within LOCATION_SLOP.
    */
   protected IPresentationElement getNearestNode(IDrawingAreaControl control,
                                                 TSPoint             nearestPoint,
                                                 String              drawEngineID)
   {
      IPresentationElement retVal = null;
      
//      if((nearestPoint != null) && (control != null)) 
//      {
//         ETRect searchRect = new ETRect(nearestPoint.getX() - (LOCATION_SLOP/2),
//                                        nearestPoint.getY() - (LOCATION_SLOP/2),
//                                        LOCATION_SLOP, LOCATION_SLOP );
//                                   
//         // See if there's a component nearby.  First get all components 
//         // within a certain slop (LOCATION_SLOP)                                     
//         IPresentationElement[] elements = control.getAllNodesViaRect(searchRect, true);
//         
//         ArrayList foundElements = new ArrayList();
//         
//         for (int index = 0; index < elements.length; index++)
//         {
//            IDrawEngine engine = TypeConversions.getDrawEngine(elements[index]);
//            if(engine != null)
//            {
//               if(drawEngineID.equals(engine.getDrawEngineID()) == true)
//               {
//                  foundElements.add(elements[index]);
//               }
//            }
//         }
//         
//         // Go through each presentation element and grab the closest one
//         double minDistance = 999.9;
//         for (Iterator iter = foundElements.iterator(); iter.hasNext();)
//         {
//            IPresentationElement element = (IPresentationElement)iter.next();
//            
//         }
//      }
      
      return retVal;                      
   }
}
