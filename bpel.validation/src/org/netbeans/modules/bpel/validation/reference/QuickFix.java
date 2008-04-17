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
package org.netbeans.modules.bpel.validation.reference;

import java.util.List;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.Referenceable;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableReference;
import org.netbeans.modules.bpel.model.api.references.ReferenceCollection;
import org.netbeans.modules.bpel.validation.core.QuickFix.Adapter;
import org.netbeans.modules.bpel.validation.core.Util;
import static org.netbeans.modules.soa.ui.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.12.06
 */
class QuickFix {

  static Adapter get(BpelEntity entity, Reference<Referenceable> reference) {
//out();
//out("reference: " + reference.getClass().getName());
//out("         : " + reference.getType().getName());
    if (reference.getType().isAssignableFrom(VariableDeclaration.class)) {
      if (entity instanceof VariableReference) {
//out("  VAR");
        return new SET_VARIABLE((VariableReference) entity, reference.getRefString());
      }
    }
//out("  null");
    return null;
  }

  // ----------------------------------------
  static class SET_VARIABLE extends Adapter {
  
    SET_VARIABLE(VariableReference reference, String name) {
      myReference = reference;
      myName = name;
    }

    public boolean canFix() {
      myVariables = getAppropriateVariables();

      if (myVariables == null) {
        return false;
      }
      if (myVariables.length == 0) {
        return false;
      }
      if (myVariables.length != 1) { // todo m
        return false;
      }
      return true;
    }

    public void doFix() {
      Variable variable = myVariables [0];

      myDescription = i18n(QuickFix.class, "QUICK_FIX_Change_varibale_name", variable.getName()); // NOI18N

      myReference.setVariable(
        ((ReferenceCollection) myReference).createReference(variable, VariableDeclaration.class));
    }

    public String getDescription() {
      return myDescription; 
    }

    private Variable [] getAppropriateVariables() {
      BpelModel model = ((BpelEntity) myReference).getBpelModel();

      if (model == null) {
        return null;
      }
      Process process = model.getProcess();

      if (process == null) {
        return null;
      }
      VariableContainer container = process.getVariableContainer();
      
      if (container == null) {
        return null;
      }
      Variable [] variables = container.getVariables();

      if (variables == null) {
        return null;
      }
      List<NamedElement> named = Util.getAppropriate(variables, myName);
      Variable [] appropriate = new Variable [named.size()];

      for (int i=0; i < named.size(); i++) {
        appropriate [i] = (Variable) named.get(i);
      }
      return appropriate;
    }

    private String myName;
    private String myDescription;
    private Variable [] myVariables;
    private VariableReference myReference;
  }
}
