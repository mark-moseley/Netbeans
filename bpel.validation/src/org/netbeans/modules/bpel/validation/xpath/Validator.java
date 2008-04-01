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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.bpel.validation.xpath;

import org.netbeans.modules.bpel.model.api.BooleanExpr;
import org.netbeans.modules.bpel.model.api.Branches;
import org.netbeans.modules.bpel.model.api.Condition;
import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.DeadlineExpression;
import org.netbeans.modules.bpel.model.api.DurationExpression;
import org.netbeans.modules.bpel.model.api.FinalCounterValue;
import org.netbeans.modules.bpel.model.api.For;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.Query;
import org.netbeans.modules.bpel.model.api.RepeatEvery;
import org.netbeans.modules.bpel.model.api.StartCounterValue;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableReference;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.support.Utils;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.bpel.model.api.PartReference;
import org.netbeans.modules.bpel.model.api.support.PathValidationContext;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.bpel.validation.core.BpelValidator;
import org.netbeans.modules.bpel.model.api.support.ValidationVisitor;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation.ValidationUtil;
import org.netbeans.modules.soa.ui.util.Duration;
import org.netbeans.modules.soa.ui.util.DurationUtil;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.02.08
 */
public final class Validator extends BpelValidator implements ValidationVisitor {

  @Override
  public void visit(Copy copy)
  {
//out();
//out("Assign: " + ((Named) copy.getParent()).getName());
    Component fromType = getTypeOfElement(getType(copy.getFrom()));
//out("FROM: " + fromType);
    Component toType = getTypeOfElement(getType(copy.getTo()));
//out("  TO: " + toType);

    if (fromType == null || toType == null) {
      return;
    }
    String fromName = ((Named) fromType).getName();
//out("  form name: " + fromName);
    String toName = ((Named) toType).getName();
//out("    to name: " + fromName);

    if (fromName == null || toName == null) {
      return;
    }
    if (fromName.equals(toName)) {
      return;
    }
    if (fromName.equals("anyType") || toName.equals("anyType")) { // NOI18N
      return;
    }
    if (ValidationUtil.getBasedSimpleType(fromType) != ValidationUtil.getBasedSimpleType(toType)) {
      addWarning("FIX_TYPE_IN_COPY", copy, getTypeName(fromType), getTypeName(toType)); // NOI18N
    }
  }

  // # 131658
  @Override
  public void visit(To to)
  {
    String value = to.getContent();
//out();
//out("to: " + value);
//out();
    if (value == null) {
      return;
    }
    value = value.trim();

    if ( !value.startsWith("$")) { // NOI18N
      addError("FIX_To_Value", to, value); // NOI18N
    }
  }

  private Component getType(From from) {
    if (from == null) {
      return null;
    }
    Component variableType = getVariableType(from);

    if (variableType != null) {
      Component partType = getPartType(from);

      if (partType == null) {
        return variableType;
      }
      else {
        return partType;
      }
    }
    return checkXPath(from);
  }

  private Component getType(To to) {
    if (to == null) {
      return null;
    }
    Component variableType = getVariableType(to);

    if (variableType != null) {
      Component partType = getPartType(to);

      if (partType == null) {
        return variableType;
      }
      else {
        return partType;
      }
    }
    return checkXPath(to);
  }

  private Component getVariableType(VariableReference reference) {
    BpelReference<VariableDeclaration> ref = reference.getVariable();

    if (ref == null) {
      return null;
    }
    VariableDeclaration declaration = ref.get();

    if (declaration == null) {
      return null;
    }
    // message type
    WSDLReference<Message> wsdlRef = declaration.getMessageType();

    if (wsdlRef != null) {
      Message message = wsdlRef.get();

      // # 130764
      if (message != null) {
        return message;
      }
    }
    // element
    SchemaReference<GlobalElement> elementRef = declaration.getElement();

    if (elementRef != null) {
      GlobalElement element = elementRef.get();

      if (element != null) {
        return element;
      }
    }
    // type
    SchemaReference<GlobalType> typeRef = declaration.getType();

    if (typeRef != null) {
      GlobalType type = typeRef.get();

      if (type != null) {
        return type;
      }
    }
    return null;
  }

  private SchemaComponent getPartType(PartReference reference) {
//out("get part type");
    WSDLReference<Part> ref = reference.getPart();

    if (ref == null) {
      return null;
    }
    return getPartType(ref.get());
  }

  private SchemaComponent getPartType(Part part) {
    if (part == null) {
      return null;
    }
    // element
    NamedComponentReference<GlobalElement> elementRef = part.getElement();

    if (elementRef != null) {
      GlobalElement element = elementRef.get();

      if (element != null) {
        return element;
      }
    }
    // type
    NamedComponentReference<GlobalType> typeRef = part.getType();

    if (typeRef != null) {
      GlobalType type = typeRef.get();

      if (type != null) {
        return type;
      }
    }
    return null;
  }
  
  @Override
  public void visit(BooleanExpr bool) {
      checkXPath(bool);
  }

  @Override
  public void visit(Branches branches) {
    checkXPath(branches);
  }

  @Override
  public void visit(Condition condition) {
    checkXPath(condition);
  }
  
  @Override
  public void visit(DeadlineExpression deadline) {
    checkXPath(deadline);
  }
  
  @Override
  public void visit(FinalCounterValue counter) {
    checkXPath(counter);
  }
  
  @Override
  public void visit(For fo) {
    checkXPath(fo);
    checkDuration(fo);
  }
  
  @Override
  public void visit(RepeatEvery repeatEvery) {
    checkXPath(repeatEvery);
    checkDuration(repeatEvery);
    // # 117688
    checkNegative(repeatEvery);
  }

  private void checkNegative(RepeatEvery repeatEvery) {
    String value = repeatEvery.getContent();

    try {
      Duration duration = DurationUtil.parseDuration(value, true);

      if (duration.hasMinus() || isZero(duration)) {
        addError("FIX_Negative_RepeatEvery", repeatEvery); // NOI18N
      }
    }
    catch (IllegalArgumentException e) {}
  }

  private boolean isZero(Duration duration) {
//out("duration: " + duration);
    return
      duration.getYears() == 0 &&
      duration.getMonths() == 0 &&
      duration.getDays() == 0 &&
      duration.getHours() == 0 &&
      duration.getMinutes() == 0 &&
      duration.getSeconds() == 0.0;
  }
  
  @Override
  public void visit(Query query) {
    checkXPath(query);
  }

  @Override
  public void visit(StartCounterValue counter) {
    checkXPath(counter);
  }

  private SchemaComponent checkXPath(ContentElement element) {
    return Utils.checkXPathExpression(element, new PathValidationContext(this, this, element));
  }

  // # 117689
  private void checkDuration(DurationExpression duration) {
    String value = duration.getContent();

    try {
      DurationUtil.parseDuration(value, true);
    }
    catch (IllegalArgumentException e) {
      addError("FIX_Duration", duration, e.getMessage()); // NOI18N
    }
  }
  
  private static void out() {
    System.out.println();
  }

  private void out(Object object) {
    System.out.println("*** " + object); // NOI18N
  }
}
