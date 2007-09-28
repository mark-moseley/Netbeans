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



package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 *
 * @author Trey Spiva
 */
public class AttributeEventsAdapter implements IAttributeEventsSink
{

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IAttributeEventsSink#onDefaultPreModified(com.embarcadero.describe.coreinfrastructure.IAttribute, com.embarcadero.describe.foundation.IExpression, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onDefaultPreModified(
      IAttribute attr,
      IExpression proposedValue,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IAttributeEventsSink#onDefaultModified(com.embarcadero.describe.coreinfrastructure.IAttribute, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onDefaultModified(IAttribute attr, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IAttributeEventsSink#onPreDefaultBodyModified(com.embarcadero.describe.coreinfrastructure.IAttribute, java.lang.String, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreDefaultBodyModified(
      IAttribute feature,
      String bodyValue,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IAttributeEventsSink#onDefaultBodyModified(com.embarcadero.describe.coreinfrastructure.IAttribute, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onDefaultBodyModified(IAttribute feature, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IAttributeEventsSink#onPreDefaultLanguageModified(com.embarcadero.describe.coreinfrastructure.IAttribute, java.lang.String, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreDefaultLanguageModified(
      IAttribute feature,
      String language,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IAttributeEventsSink#onDefaultLanguageModified(com.embarcadero.describe.coreinfrastructure.IAttribute, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onDefaultLanguageModified(IAttribute feature, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IAttributeEventsSink#onPreDerivedModified(com.embarcadero.describe.coreinfrastructure.IAttribute, boolean, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreDerivedModified(
      IAttribute feature,
      boolean proposedValue,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IAttributeEventsSink#onDerivedModified(com.embarcadero.describe.coreinfrastructure.IAttribute, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onDerivedModified(IAttribute feature, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IAttributeEventsSink#onPrePrimaryKeyModified(com.embarcadero.describe.coreinfrastructure.IAttribute, boolean, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPrePrimaryKeyModified(
      IAttribute feature,
      boolean proposedValue,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IAttributeEventsSink#onPrimaryKeyModified(com.embarcadero.describe.coreinfrastructure.IAttribute, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPrimaryKeyModified(IAttribute feature, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

}
