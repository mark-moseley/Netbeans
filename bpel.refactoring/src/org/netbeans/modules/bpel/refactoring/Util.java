/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.refactoring;

import java.util.List;
import javax.xml.namespace.QName;

import org.openide.nodes.Node;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaModel;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

import org.netbeans.modules.bpel.editors.api.nodes.FactoryAccess;
import static org.netbeans.modules.print.api.PrintUI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.06.27
 */
public final class Util {

  private Util() {}
  
  public static void visit( 
    NamedComponentReference<GlobalType> type,
    NamedComponentReference<GlobalElement> element,
    Referenceable target,
    Component component,
    List<Element> usage)
  {
    visit(type, target, component, usage);
    visit(element, target, component, usage);
  }        
          
  public static void visit(
    Reference reference,
    Referenceable target,
    Component component,
    List<Element> usage)
  {
    if (reference == null || reference.get() == null) {
      return;
    }
//out();
//out("   Visit: " + getName(reference.get()));
//out("  target: " + target.getName());
    if (target.equals(reference.get())) {
//out();
//out("AdD: " + getName(component));
      usage.add(new Element(component));
    }
  }

  public static void visit(
    QName qName,
    Referenceable target,
    Component component,
    List<Element> usage)
  {
//out();
//out("VISIT: " + qName);
    if (target instanceof Named && contains(qName, (Named) target)) {
//out();
//out("ADd: " + getName(component));
      usage.add(new Element(component));
    }
  }

  private static boolean contains(QName qName, Named target) {
    if (qName == null) {
//out("qName is null");
      return false;
    }
    String part = qName.getLocalPart();

    if ( !part.equals(target.getName())) {
//out("Target name != part");
//out("         part: " + part);
//out("  Target name: " + target.getName());
      return false;
    }
    return qName.getNamespaceURI().equals(getNamespace(target.getModel()));
  }

  private static String getNamespace(Model model) {
    if (model instanceof SchemaModel) {
      return ((SchemaModel) model).getSchema().getTargetNamespace();
    }
    if (model instanceof WSDLModel) {
      return ((WSDLModel) model).getDefinitions().getTargetNamespace();
    }
    return null;
  }

  public static String getName(Object component) {
    if (component == null) {
      return null;
    }
    if (component instanceof Named) {
      String name = ((Named) component).getName();

      if (name != null) {
        return name;
      }
    }
    return component.getClass().getName();
  }

  public static Node getDisplayNode(Component component) {
    return FactoryAccess.getRefactoringNodeFactory().createNode(component);
  }
}
