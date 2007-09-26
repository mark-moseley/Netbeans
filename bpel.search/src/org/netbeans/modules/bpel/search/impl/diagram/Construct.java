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
package org.netbeans.modules.bpel.search.impl.diagram;

import java.util.List;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.bpel.editors.api.Diagram;
import org.netbeans.modules.bpel.editors.api.DiagramElement;

import org.netbeans.modules.xml.search.api.SearchException;
import org.netbeans.modules.xml.search.api.SearchOption;
import static org.netbeans.modules.print.api.PrintUtil.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.12.05
 */
public final class Construct extends Engine {

  public void search(SearchOption option) throws SearchException {
    Diagram diagram = (Diagram) option.getSource();
    diagram.clearHighlighting();
//out();
    fireSearchStarted(option);
    search(diagram, option.useSelection());
    fireSearchFinished(option);
  }
  
  private void search(Diagram diagram, boolean useSelection) {
    List<DiagramElement> elements = diagram.getElements(useSelection);

    for (DiagramElement element : elements) {
      Component component = element.getComponent();

      if (acceptsAttribute(component) || acceptsComponent(component)) {
//out(indent + "      add.");
        fireSearchFound(new Element(element));
      }
    }
  }
  
  private boolean acceptsAttribute(Component component) {
    if ( !(component instanceof DocumentComponent)) {
      return false;
    }
    NamedNodeMap attributes = ((DocumentComponent) component).
      getPeer().getAttributes();

    for (int i=0; i < attributes.getLength(); i++) {
      Node attribute = attributes.item(i);
     
      if (accepts(attribute.getNodeName())) {
        return true;
      }
      if (accepts(attribute.getNodeValue())) {
        return true;
      }
    }
    return false;
  }

  private boolean acceptsComponent(Component component) {
    if ( !(component instanceof DocumentComponent)) {
      return false;
    }
    return accepts(((DocumentComponent) component).getPeer().getTagName());
  }

  public String getDisplayName() {
    return i18n(Engine.class, "CTL_Construct_Display_Name"); // NOI18N
  }

  public String getShortDescription() {
    return i18n(Engine.class, "CTL_Construct_Short_Description"); // NOI18N
  }
}
