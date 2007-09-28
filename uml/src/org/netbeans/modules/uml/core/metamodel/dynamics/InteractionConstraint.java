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


package org.netbeans.modules.uml.core.metamodel.dynamics;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.Constraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementChangeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpressionListener;

public class InteractionConstraint extends Constraint
        implements IInteractionConstraint, IExpressionListener
{
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionConstraint#getMaxInt()
     */
    public IExpression getMaxInt()
    {
        return new ElementCollector<IExpression>()
            .retrieveSingleElement( 
                m_Node, "UML:InteractionConstraint.maxint/*", IExpression.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionConstraint#setMaxInt(org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression)
     */
    public void setMaxInt(IExpression exp)
    {
        addChild( "UML:InteractionConstraint.maxint",
                  "UML:InteractionConstraint.maxint", 
                  exp );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionConstraint#getMinInt()
     */
    public IExpression getMinInt()
    {
        return new ElementCollector<IExpression>()
            .retrieveSingleElement( 
                m_Node, "UML:InteractionConstraint.minint/*", IExpression.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionConstraint#setMinInt(org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression)
     */
    public void setMinInt(IExpression exp)
    {
        addChild( "UML:InteractionConstraint.minint",
                  "UML:InteractionConstraint.minint", 
                  exp );
    }


   // IExpressionListener 
   public boolean onPreBodyModified( IExpression exp, String proposedValue )
   {
      boolean proceed = true;

      EventDispatchRetriever ret = EventDispatchRetriever.instance();
      IElementChangeEventDispatcher disp = (IElementChangeEventDispatcher)ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
      if (disp != null)
      {
         IEventPayload payload = disp.createPayload("PreDefaultBodyModified");
         proceed = disp.fireElementPreModified( this, payload);
      }
      
      return proceed;
   }
   
   public void onBodyModified( IExpression exp )
   {
      EventDispatchRetriever ret = EventDispatchRetriever.instance();
      IElementChangeEventDispatcher disp = (IElementChangeEventDispatcher)ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
      if (disp != null)
      {
         IEventPayload payload = disp.createPayload("DefaultBodyModified");
         disp.fireElementModified( this, payload);
      }
   }
   
   public boolean onPreLanguageModified( IExpression exp, String proposedValue )
   {
      // Do nothing
      return true;
   }
   
   public void onLanguageModified( IExpression exp )
   {
      // Do nothing
   }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Constraint#establishNodePresence(org.dom4j.Document, org.dom4j.Node)
     */
    public void establishNodePresence(Document doc, Node parent)
    {
        buildNodePresence("UML:InteractionConstraint", doc, parent);
    }
}