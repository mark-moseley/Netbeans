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

import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;


/**
 *
 * @author Trey Spiva
 */
public class ClassifierFeatureEventsAdapter
   implements IClassifierFeatureEventsSink
{

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreAdded(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IFeature, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onFeaturePreAdded(
      IClassifier classifier,
      IFeature feature,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureAdded(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IFeature, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onFeatureAdded(
      IClassifier classifier,
      IFeature feature,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreRemoved(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IFeature, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onFeaturePreRemoved(
      IClassifier classifier,
      IFeature feature,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureRemoved(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IFeature, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onFeatureRemoved(
      IClassifier classifier,
      IFeature feature,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreMoved(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IFeature, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onFeaturePreMoved(
      IClassifier classifier,
      IFeature feature,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureMoved(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IFeature, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onFeatureMoved(
      IClassifier classifier,
      IFeature feature,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreDuplicatedToClassifier(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IFeature, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onFeaturePreDuplicatedToClassifier(
      IClassifier classifier,
      IFeature feature,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureDuplicatedToClassifier(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IFeature, com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IFeature, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onFeatureDuplicatedToClassifier(
      IClassifier pOldClassifier,
      IFeature pOldFeature,
      IClassifier pNewClassifier,
      IFeature pNewFeature,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onPreAbstractModified(com.embarcadero.describe.coreinfrastructure.IClassifier, boolean, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreAbstractModified(
      IClassifier feature,
      boolean proposedValue,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onAbstractModified(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onAbstractModified(IClassifier feature, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onPreLeafModified(com.embarcadero.describe.coreinfrastructure.IClassifier, boolean, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreLeafModified(
      IClassifier feature,
      boolean proposedValue,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onLeafModified(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onLeafModified(IClassifier feature, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onPreTransientModified(com.embarcadero.describe.coreinfrastructure.IClassifier, boolean, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreTransientModified(
      IClassifier feature,
      boolean proposedValue,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onTransientModified(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onTransientModified(IClassifier feature, IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onPreTemplateParameterAdded(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IParameterableElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreTemplateParameterAdded(
      IClassifier pClassifier,
      IParameterableElement pParam,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onTemplateParameterAdded(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IParameterableElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onTemplateParameterAdded(
      IClassifier pClassifier,
      IParameterableElement pParam,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onPreTemplateParameterRemoved(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IParameterableElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onPreTemplateParameterRemoved(
      IClassifier pClassifier,
      IParameterableElement pParam,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.coreinfrastructure.IClassifierFeatureEventsSink#onTemplateParameterRemoved(com.embarcadero.describe.coreinfrastructure.IClassifier, com.embarcadero.describe.coreinfrastructure.IParameterableElement, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onTemplateParameterRemoved(
      IClassifier pClassifier,
      IParameterableElement pParam,
      IResultCell cell)
   {
      // TODO Auto-generated method stub
   }

    public void onEnumerationLiteralAdded(
        IClassifier classifier, IEnumerationLiteral enumLit, IResultCell cell)
    {
      // TODO Auto-generated method stub
    }

    public void onEnumerationLiteralPreAdded(
        IClassifier classifier, IEnumerationLiteral enumLit, IResultCell cell)
    {
      // TODO Auto-generated method stub
    }
}
