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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.refactoring;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Variable;

import org.netbeans.modules.xml.refactoring.UsageGroup;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexContentDefinition;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.ComplexTypeDefinition;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.SequenceDefinition;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor;

import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

import org.netbeans.modules.xml.xpath.AbstractXPathModelHelper;
import org.netbeans.modules.xml.xpath.LocationStep;
import org.netbeans.modules.xml.xpath.StepNodeNameTest;
import org.netbeans.modules.xml.xpath.XPathCoreFunction;
import org.netbeans.modules.xml.xpath.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.XPathException;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xml.xpath.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.XPathLocationPath;
import org.netbeans.modules.xml.xpath.XPathModel;
import org.netbeans.modules.xml.xpath.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.XPathVariableReference;
import org.netbeans.modules.xml.xpath.visitor.AbstractXPathVisitor;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.06.30
 */
final class XPath extends AbstractXPathVisitor {

  XPath(UsageGroup usage, Named target, String oldName) {
    myOldName = oldName;
    myTarget = target;
    myUsage = usage;
  }

  void visit(String content, Component component) {
//Log.out();
//Log.out("XPATH visit: " + content);
    visit(content, component, false);
  }

  String rename(String content, Component component) {
//out();
//out("XPATH rename");
    return visit(content, component, true);
  }

  private String visit(String content, Component component, boolean doRename) {
    if (content == null || content.length() == 0) {
      return content;
    }
    XPathModel model = AbstractXPathModelHelper.getInstance().newXPathModel();
    myVisitedComplexType = new LinkedList<ComplexType>();
    myExpressions = new LinkedList<XPathExpression>();
    myComponent = component;
    myDoRename = doRename;

    try {
      XPathExpression expression = model.parseExpression(content);
      expression.accept(this);

      if (myDoRename) {
//out("  do rename: " + myTarget.getName());
        rename(myTarget.getName());
      }
//out("  new content: " + expression.getExpressionString());
      return expression.getExpressionString();
    }
    catch (XPathException e) {
//out("  exception: " + e.getMessage());
      return content;
    }
  }

  private void rename(String newName) {
//Log.out();
//Log.out("----------------------------------");
//Log.out();
//Log.out("To rename:");

    for(XPathExpression expression: myExpressions) {
//Log.out("See: " + expression);
      if (expression instanceof LocationStep) {
        LocationStep step = (LocationStep) expression;
        step.setNodeTest(new StepNodeNameTest(
          createName(step.getString(), newName)));
      }
      else if (expression instanceof XPathVariableReference) {
        XPathVariableReference reference = (XPathVariableReference) expression;
        reference.setVariableName(createName(reference.getVariableName(), newName));
      }
    }
//Log.out();
//Log.out("----------------------------------");
//Log.out();
  }

  private String createName(String oldName, String newName) {
    int k = oldName.indexOf(":"); // NOI18N

    if (k == -1) {
      return newName;
    }
    return oldName.substring(0, k+1) + newName;
  }

  private String createName(QName qName, String part) {
    String name = qName.getLocalPart();
    int k = name.indexOf("."); // NOI18N

    if (k == -1) {
      return name;
    }
    return name.substring(0, k + 1) + part;
  }

  @Override
  public void visit(XPathExpressionPath expressionPath)
  {
//Log.out();
//Log.out("EXPRESION: " + expressionPath);
    XPathExpression rootExpression = expressionPath.getRootExpression();
    myVariable = null;
    myVariableReference = null;

    if ( !expressionPath.equals(rootExpression)) {
///Log.out("     root: " + expression);
      rootExpression.accept(this);
    }
    if (myVariable == null) {
//Log.out("Variable is not found");
      return;
    }
//Log.out(" Variable: " + myVariable.getName());
    LocationStep[] locations = expressionPath.getSteps();
    List<LocationStep> steps = new LinkedList<LocationStep>();
    steps.add(null); // first step is fake
    
    if (locations == null) {
      return;
    }
    for (LocationStep location : locations) {
      steps.add(location);
    }
    visit(steps);
    visit(locations);
  }

  private void visit(LocationStep[] locations) {
    if (locations == null) {
      return;
    }
    for (LocationStep location : locations) {
      location.accept(this);
    }
  }

  private void visit(List<LocationStep> steps) {
    visitReference(myVariable.getMessageType(), createList(steps), ""); // NOI18N
    visitReference(myVariable.getElement(), createList(steps), ""); // NOI18N
    visitReference(myVariable.getType(), createList(steps), ""); // NOI18N
  }

  private List<LocationStep> createList(List<LocationStep> steps) {
    List<LocationStep> list = new LinkedList<LocationStep>();
    
    for (int i=0; i < steps.size(); i++) {
      list.add(steps.get(i));
    }
    return list;
  }

  private void visitPart(Part part, List<LocationStep> steps) {
    visitReference(part.getElement(), createList(steps), ""); // NOI18N
    visitReference(part.getType(), createList(steps), ""); // NOI18N
  }

  private void visitReference(
    NamedComponentReference reference,
    List<LocationStep> steps,
    String indent)
  {
    if (reference != null) {
      visitComponent(reference.get(), steps, indent);
    }
  }

  private void visitComponent(
    Object object,
    List<LocationStep> steps,
    String indent)
  {
    if (object instanceof ComplexType) {
//Log.out("  visit complex");
      visitComplexType((ComplexType) object, steps, indent);
    }
    else if (object instanceof SimpleType) {
//Log.out("  visit simple");
      visitSimpleType((SimpleType) object, steps, indent);
    }
    else if (object instanceof Element) {
      visitElement((Element) object, steps, indent);
    }
    else if (object instanceof Message) {
      visitMessage((Message) object, steps, indent);
    }
    else {
      if (object == null) {
        return;
      }
//Log.out(indent + " unknown !!!: " + object);
    }
  }

  private void visitMessage(
    Message message,
    List<LocationStep> steps,
    String indent)
  {
    Iterator<Part> parts = message.getParts().iterator();

    while (parts.hasNext()) {
      Part part = parts.next();
//Log.out();
//Log.out("  see part: " + part.getName());
//Log.out("      part: " + myPartName);
      if (part.equals(myTarget)) {
        addItem();

        if (myDoRename) {
          myExpressions.add(myVariableReference);
        }
      }
      if (myPartName.equals(part.getName())) {
//Log.out("==== PART: " + myPartName);
//Log.out();
        visitPart(part, createList(steps));
      }
    }
  }

  private void visitElement(
    Element element,
    List<LocationStep> steps,
    String indent)
  {
///Log.out();
//Log.out(indent + "ELEMENT: " + Util.getName(element));
    if (checkUsages(element, steps, false)) {
      return;
    }
    myTypeReference = null;

    element.accept(new DeepSchemaVisitor() {
      @Override
      public void visit(ComplexExtension extension)
      {
        myTypeReference = extension.getBase();
      }
    });
    visitReference(myTypeReference, steps, indent + INDENT);

    if (element instanceof TypeContainer) {
      TypeContainer container = (TypeContainer) element;
  
      visitReference(container.getType(),
        createList(steps), indent + INDENT);

      visitComponent(container.getInlineType(),
        createList(steps), indent + INDENT);
    }
  }

  private void visitComplexType(
    ComplexType type,
    List<LocationStep> steps,
    String indent)
  {
//Log.out(indent + "CM.TYPE: " + Util.getName(type));
    if (myVisitedComplexType.contains(type)) {
      return;
    }
    myVisitedComplexType.add(type);

    if (checkUsages(type, steps, true)) {
      return;
    }
    ComplexTypeDefinition definition = type.getDefinition();
    
    if (definition instanceof Sequence) {
      visitSequence((Sequence) definition, steps, indent + INDENT);
    }
    else if (definition instanceof Choice) {
      visitChoice((Choice) definition, steps, indent);
    }
    else if (definition instanceof ComplexContent) {
      visitComplexContent((ComplexContent) definition, steps, indent);
    }
    else {
//Log.out(indent + "unk !!: " + definition);
      return;
    }
  }

  private void visitComplexContent(
    ComplexContent content,
    List<LocationStep> steps,
    String indent)
  {
    ComplexContentDefinition definition = content.getLocalDefinition();

    if (definition instanceof ComplexExtension) {
      visitReference(((ComplexExtension) definition).getBase(), steps, indent);
    }
  }

  private void visitChoice(Choice choice, List<LocationStep> steps, String indent) {
//Log.out(indent + " [choice] ===================");
    Iterator<Choice> choices = choice.getChoices().iterator();

    while (choices.hasNext()) {
      visitChoice(choices.next(), createList(steps), indent + INDENT);
    }
    Iterator<Sequence> sequences = choice.getSequences().iterator();

    while (sequences.hasNext()) {
      visitSequence(sequences.next(), createList(steps), indent + INDENT);
    }
    Iterator<LocalElement> elements = choice.getLocalElements().iterator();

    while (elements.hasNext()) {
      visitElement(elements.next(), createList(steps), indent + INDENT);
    }
    Iterator<ElementReference> references = choice.getElementReferences().iterator();

    while (references.hasNext()) {
      visitReference(references.next().getRef(), createList(steps), indent + INDENT);
    }
//Log.out(indent + " [===========================");
  }

  private void visitSequence(
    Sequence sequence,
    List<LocationStep> steps,
    String indent)
  {
//Log.out(indent + " [sequnce] ==================");
    List<SequenceDefinition> content = sequence.getContent();

    if (content == null) {
      return;
    }
    for (SequenceDefinition definition : content) {
///Log.out(indent + "      see: " + Util.getName(definition));
      if (definition instanceof Element) {
        visitElement((Element) definition,
          createList(steps), indent + INDENT);
      }
      else if (definition instanceof ComplexType) {
        visitComplexType((ComplexType) definition,
          createList(steps), indent + INDENT);
      }
      else if (definition instanceof SimpleType) {
        visitSimpleType((SimpleType) definition,
          createList(steps), indent + INDENT);
      }
      else if (definition instanceof ElementReference) {
        visitReference(((ElementReference) definition).getRef(),
          createList(steps), indent + INDENT);
      }
      else {
//Log.out(indent + "    error !!!: " + definition);
        return;
      }
    }
//Log.out(indent + " [===========================");
  }

  private void visitSimpleType(
    SimpleType type,
    List<LocationStep> steps,
    String indent)
  {
//Log.out(indent + "SM.TYPE: " + Util.getName(type));
    checkUsages(type, steps, true);
  }

  @Override
  public void visit(XPathVariableReference reference)
  {
    QName qName = reference.getVariableName();
//Log.out("VAR REFER: " + qName);
    String name = qName.getLocalPart();
    String part = ""; // NOI18N
    int k = name.indexOf("."); // NOI18N

    if (k != -1) {
      part = name.substring(k + 1);
      name = name.substring(0, k);
    }
    if ( !(myComponent instanceof BpelEntity)) {
      return;
    }
    Variable[] variables = ((BpelModel) ((BpelEntity) myComponent).getModel()).
      getProcess().getVariableContainer().getVariables();

    if (variables == null) {
      return;
    }
    for (Variable variable : variables) {
//Log.out("  see: " + variable.getName());

      if (variable.getName().equals(name)) {
//Log.out("   this.");
        myVariable = variable;
        myVariableReference = reference;
        myPartName = part;
        break;
      }
    }
  }

  @Override
  public void visit(LocationStep locationStep)
  {
//Log.out(" LCL STEP: " + locationStep);
    XPathPredicateExpression[] expressions = locationStep.getPredicates();

    if (expressions == null) {
      return;
    }
    for (XPathPredicateExpression expression : expressions) {
      expression.accept(this);
    }
  }

  @Override
  public void visit(XPathCoreFunction coreFunction)
  {
//Log.out("CORE FUNC: " + coreFunction);
    visitChildren(coreFunction);
  }

  @Override
  public void visit(XPathCoreOperation coreOperation)
  {
//Log.out("CORE OPER: " + coreOperation);
    visitChildren(coreOperation);
  }

  @Override
  public void visit(XPathExtensionFunction extensionFunction)
  {
//Log.out("EXT  FUNC: " + extensionFunction);
    visitChildren(extensionFunction);
  }

  @Override
  public void visit(XPathLocationPath locationPath)
  {
//Log.out("LOCA PATH: " + locationPath);
    visit(locationPath.getSteps());
  }

  private boolean checkUsages(
    Component component,
    List<LocationStep> steps,
    boolean nextStep)
  {
    if (component == null) {
      return false;
    }
//Log.out();
//Log.out("  chk compnt: "+ Util.getName(component));
//Log.out("      target: "+ myTarget.getName());
//Log.out("        step: "+ (steps.size() == 0 ? null : steps.get(0)));

    if (steps.size() == 0) {
//Log.out("        size: 0");
//Log.out("      return: false");
//Log.out();
      return false;
    }
    LocationStep step = steps.get(0);

    if (myTarget.equals(component)) {
//Log.out();
//Log.out("name: " + myOldName);
//Log.out("step: " + step.getString());
      if (step != null && equalsIgnorePrefix(myOldName, step.getString())) {
        addItem();

        if (myDoRename) {
          myExpressions.add(step);
//Log.out("!! ==== STEP: " + step.getString());
        }
//Log.out("      return: true");
//Log.out();
        return true;
      }
    }
    else {
      if (nextStep) {
//Log.out("      remove: " + step);
        steps.remove(0);
      }
    }
//Log.out("      return: false");
//Log.out();
    return false;
  }

  private boolean equalsIgnorePrefix(String name, String step) {
    if (name == null || step == null) {
      return false;
    }
    int k = step.indexOf(":"); // NOI18N

    if (k == -1) {
      return name.equals(step);
    }
    return name.equals(step.substring(k+1));
  }

  private void addItem() {
    if (myUsage != null) {
      myUsage.addItem(myComponent);
//Log.out("!! ===== ADD: " + Util.getName(myComponent));
    }
  }

  private Named myTarget;
  private String myOldName;
  private String myPartName;
  private UsageGroup myUsage;
  private boolean myDoRename;
  private Variable myVariable;
  private Component myComponent;
  private List<XPathExpression> myExpressions;
  private List<ComplexType> myVisitedComplexType;
  private XPathVariableReference myVariableReference;
  private NamedComponentReference<GlobalType> myTypeReference;
  private static final String INDENT = "  "; // NOI18N
}
