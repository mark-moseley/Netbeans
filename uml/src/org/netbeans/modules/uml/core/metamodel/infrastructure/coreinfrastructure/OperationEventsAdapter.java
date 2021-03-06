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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 *
 * @author Trey Spiva
 */
public class OperationEventsAdapter implements IOperationEventsSink
{

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IOperationEventsSink#onConditionPreAdded(com.embarcadero.describe.coreinfrastructure.IOperation, com.embarcadero.describe.foundation.IConstraint, boolean, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onConditionPreAdded(
      IOperation oper,
      IConstraint condition,
      boolean isPreCondition,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IOperationEventsSink#onConditionAdded(com.embarcadero.describe.coreinfrastructure.IOperation, com.embarcadero.describe.foundation.IConstraint, boolean, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onConditionAdded(
      IOperation oper,
      IConstraint condition,
      boolean isPreCondition,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IOperationEventsSink#onConditionPreRemoved(com.embarcadero.describe.coreinfrastructure.IOperation, com.embarcadero.describe.foundation.IConstraint, boolean, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onConditionPreRemoved(
      IOperation oper,
      IConstraint condition,
      boolean isPreCondition,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IOperationEventsSink#onConditionRemoved(com.embarcadero.describe.coreinfrastructure.IOperation, com.embarcadero.describe.foundation.IConstraint, boolean, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onConditionRemoved(
      IOperation oper,
      IConstraint condition,
      boolean isPreCondition,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IOperationEventsSink#onPreQueryModified(com.embarcadero.describe.coreinfrastructure.IOperation, boolean, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreQueryModified(
      IOperation oper,
      boolean proposedValue,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IOperationEventsSink#onQueryModified(com.embarcadero.describe.coreinfrastructure.IOperation, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onQueryModified(IOperation oper, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IOperationEventsSink#onRaisedExceptionPreAdded(com.embarcadero.describe.coreinfrastructure.IOperation, com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onRaisedExceptionPreAdded(
      IOperation oper,
      IClassifier pException,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IOperationEventsSink#onRaisedExceptionAdded(com.embarcadero.describe.coreinfrastructure.IOperation, com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onRaisedExceptionAdded(
      IOperation oper,
      IClassifier pException,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IOperationEventsSink#onRaisedExceptionPreRemoved(com.embarcadero.describe.coreinfrastructure.IOperation, com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onRaisedExceptionPreRemoved(
      IOperation oper,
      IClassifier pException,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IOperationEventsSink#onRaisedExceptionRemoved(com.embarcadero.describe.coreinfrastructure.IOperation, com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onRaisedExceptionRemoved(
      IOperation oper,
      IClassifier pException,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IOperationEventsSink#onPreOperationPropertyModified(com.embarcadero.describe.coreinfrastructure.IOperation, int, boolean, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreOperationPropertyModified(
      IOperation oper,
      int nKind,
      boolean proposedValue,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IOperationEventsSink#onOperationPropertyModified(com.embarcadero.describe.coreinfrastructure.IOperation, int, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onOperationPropertyModified(
      IOperation oper,
      int nKind,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

}
