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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.bpel.validation.reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclarationScope;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.MappedReference;
import org.netbeans.modules.bpel.model.api.references.ReferenceCollection;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.ExpressionUpdater;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.bpel.validation.core.BpelValidator;
import static org.netbeans.modules.soa.ui.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.05.03
 */
public final class Validator extends BpelValidator {

  @Override
  public void visit(Process process) {
    processEntity(process);
  }

  private void processEntity(BpelEntity entity) {
    checkReferenceCollection(entity);
    checkExpressions(entity);
    List<BpelEntity> children = entity.getChildren();

    for (BpelEntity child : children) {
      processEntity(child);
    }
  }

  private void checkExpressions(BpelEntity entity) {
    if ( !(entity instanceof ContentElement)) {
      return;
    }
    String expression = ((ContentElement) entity).getContent();
    Collection<String> collection = ExpressionUpdater.getInstance().getUsedVariables(expression);
    Set<String> set = new HashSet<String>(collection);
    findDeclarationsAscendant(entity, set);

    if (set.size() == 0) {
      return;
    }
    StringBuilder builder = new StringBuilder();

    for (String string : set) {
      builder.append( string );
      builder.append(", "); // NOI18N
    }
    String key;

    if (set.size() > 1) {
      key = "FIX_Variables";
    }
    else {
      key = "FIX_Variable";
    }
    addError(key, entity, builder.substring(0, builder.length()-2), expression.trim());
  }
  
  private void findDeclarationsAscendant(BpelEntity entity, Set<String> set) {
      if (set.size() == 0) {
          return;
      }
      if (entity instanceof VariableDeclarationScope) {
          findDeclarationsDescendant(entity, set);
      }
      // # 81027
      BpelContainer parent = entity.getParent();

      if (parent != null) {
        findDeclarationsAscendant(parent, set);
      }
  }
  
  private void findDeclarationsDescendant(BpelEntity entity, Set<String> set) {
      if (entity instanceof VariableDeclaration) {
          String name = ((VariableDeclaration)entity).getVariableName();
          set.remove(name);
      }
      List<Variable> list = entity.getChildren(Variable.class);

      if (list != null) {
          for (Variable variable : list) {
              String name = variable.getVariableName();
              set.remove( name );
          }
      }
      if (entity instanceof VariableDeclarationScope) {
          List<VariableDeclarationScope> scopes = entity.getChildren(VariableDeclarationScope.class);

          if (scopes == null) {
              return;
          }
          for (VariableDeclarationScope scope : scopes) {
              findDeclarationsDescendant(scope, set);
          }
      }
  }

  @SuppressWarnings("unchecked")
  private void checkReferenceCollection(BpelEntity entity) {
    if ( !(entity instanceof ReferenceCollection)) {
      return;
    }
    ReferenceCollection collection = (ReferenceCollection) entity;
    Reference[] refs = collection.getReferences();

    for (Reference reference : refs) {
      if (reference == null) {
        continue;
      }
      if ( !reference.isBroken()) {
        continue;
      }
      String tag = entity.getPeer().getLocalName();
      String attr = ((MappedReference) reference).getAttribute().getName();

      addQuickFix("FIX_Reference", entity, tag, attr, QuickFix.get(entity, (Reference<Referenceable>) reference));
    }
  }
}
