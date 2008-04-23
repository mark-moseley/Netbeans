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

package org.netbeans.modules.bpel.mapper.predicates.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.mapper.cast.AbstractTypeCast;
import org.netbeans.modules.bpel.mapper.predicates.AbstractPredicate;
import org.netbeans.modules.bpel.mapper.tree.models.VariableDeclarationWrapper;
import org.netbeans.modules.bpel.mapper.tree.spi.RestartableIterator;
import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.support.XPathBpelVariable;
import org.netbeans.modules.bpel.model.api.support.BpelXPathModelFactory;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xpath.ext.AbstractLocationPath;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.StepNodeNameTest;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathModelFactory;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;
import org.netbeans.modules.xml.xpath.ext.schema.SchemaModelsStack;
import org.netbeans.modules.xml.xpath.ext.spi.CastSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.SimpleSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.VariableSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathVariable;

/**
 * The auxiliary class to convert tree path or path iterator to other forms.
 * 
 * @author nk160297
 */
public class PathConverter {

    private static enum ParsingStage {
        SCHEMA, PART, VARIABLE;
    };

    /**
     * Constructs a new list, which contains the schema elements, predicates, 
     * special steps, cast objects, part and variable from the specified iterator pathItr. 
     * The first object taken from iterator will be at the beginning of the list 
     * if the parameter sameOrder is true. The order will be oposite otherwise.
     * If the iterator has incompatible content then the null is returned. 
     * 
     * @param pathItr
     * @return
     */
    public static List<Object> constructObjectLocationtList(
            RestartableIterator<Object> pathItr, boolean sameOrder) {
        //
        pathItr.restart();
        //
        LinkedList<Object> treeItemList = new LinkedList<Object>();
        //
        // Process the path
        ParsingStage stage = null;
        boolean needBreak = false;
        while (pathItr.hasNext()) {
            Object obj = pathItr.next();
            Object toAdd = null;
            if (obj instanceof SchemaComponent) {
                if (!(stage == null || stage == ParsingStage.SCHEMA)) {
                    return null;
                }
                stage = ParsingStage.SCHEMA;
                toAdd = obj;
            } else if (stage != null && obj instanceof AbstractPredicate) {
                if (!(stage == null || stage == ParsingStage.SCHEMA)) {
                    return null;
                }
                stage = ParsingStage.SCHEMA;
                toAdd = obj;
            } else if (obj instanceof Part) {
                if (!(stage == null || stage == ParsingStage.SCHEMA)) {
                    return null;
                }
                stage = ParsingStage.PART;
                toAdd = obj;
            } else if (obj instanceof AbstractVariableDeclaration) {
                if (!(stage == null || 
                        stage == ParsingStage.SCHEMA || 
                        stage == ParsingStage.PART)) {
                    return null;
                }
                //
                AbstractVariableDeclaration var = (AbstractVariableDeclaration)obj;
                //
                VariableDeclaration varDecl = null;
                if (var instanceof VariableDeclaration) {
                    varDecl = (VariableDeclaration)var;
                } else if (var instanceof VariableDeclarationWrapper) {
                    varDecl = ((VariableDeclarationWrapper)var).getDelegate();
                }
                //
                if (varDecl == null) {
                    return null;
                }
                //
                stage = ParsingStage.VARIABLE;
                toAdd = varDecl;
                //
                // Everything found!
                needBreak = true;
            } else {
                if (stage == null) {
                    return null;
                }
                needBreak = true;
            }
            //
            if (toAdd == null) {
                needBreak = true;
            } else {
                if (sameOrder) {
                    treeItemList.addFirst(toAdd); 
                } else {
                    treeItemList.addLast(toAdd);
                }
            }
            //
            if (needBreak) {
                break;
            }
        }
        //
        return treeItemList;
    }

    public static XPathBpelVariable constructXPathBpelVariable(
            List<Object> pathList) {
        //
        AbstractVariableDeclaration var = null;
        Part part = null;
        //
        // Process the path
        ParsingStage stage = null;
        for (Object obj: pathList) {
            if (obj instanceof Part) {
                if (stage != null) {
                    return null;
                }
                stage = ParsingStage.PART;
                part = (Part)obj;
            } else if (obj instanceof AbstractVariableDeclaration) {
                if (!(stage == null || stage == ParsingStage.PART)) {
                    return null;
                }
                //
                var = (AbstractVariableDeclaration)obj;
                stage = ParsingStage.VARIABLE;
                //
                // Everything found!
                break;
            } else {
                return null;
            }
        }
        if (var == null) {
            return null;
        }
        //
        return new XPathBpelVariable(var, part);
    }

    public static List<Object> constructObjectLocationtList(
            XPathExpression exprPath) {
        //
        ArrayList<Object> treeItemList = new ArrayList<Object>();
        //
        if (exprPath instanceof AbstractLocationPath) {
            LocationStep[] stepArr = ((AbstractLocationPath)exprPath).getSteps();
            for (int index = stepArr.length - 1; index >= 0; index--) {
                LocationStep step = stepArr[index];
                XPathSchemaContext sContext = step.getSchemaContext();
                if (sContext != null) {
                    SchemaComponent sComp = XPathSchemaContext.Utilities.
                            getSchemaComp(sContext);
                    if (sComp != null) {
                        treeItemList.add(sComp);
                        continue;
                    }
                }
                //
                // Unresolved step --> the location list can't be built
                return null;
            }
        }
        //
        XPathVariableReference varRefExpr = null;
        if (exprPath instanceof XPathExpressionPath) {
            XPathExpression expr = ((XPathExpressionPath)exprPath).getRootExpression();
            if (expr instanceof XPathVariableReference) {
                varRefExpr = (XPathVariableReference)expr;
            }
        } else if (exprPath instanceof XPathVariableReference) {
            varRefExpr = (XPathVariableReference)exprPath;
        }
        //
        if (varRefExpr != null) {
            XPathVariable var = varRefExpr.getVariable();
            assert var instanceof XPathBpelVariable;
            XPathBpelVariable bpelVar = (XPathBpelVariable)var;
            //
            Part part = bpelVar.getPart();
            if (part != null) {
                treeItemList.add(part);
            } 
            //
            AbstractVariableDeclaration varDecl = bpelVar.getVarDecl();
            if (varDecl != null) {
                treeItemList.add(varDecl);
            }
        }
        //
        return treeItemList;
    }
    
    public static XPathExpression constructXPath(BpelEntity base, 
            RestartableIterator<Object> pathItr) {
        //
        // It's necessary to have the order, oposite to the iterator. 
        // It's required for correct buildeing the XPath expression
        List<Object> objList = constructObjectLocationtList(pathItr, false);
        //
        if (objList == null || objList.isEmpty()) {
            return null;
        }
        //
        XPathModel xPathModel = BpelXPathModelFactory.create(base); 
        XPathModelFactory factory = xPathModel.getFactory();
        //
        VariableDeclaration varDecl = null;
        Part part = null;
        LinkedList<LocationStep> stepList = new LinkedList<LocationStep>();
        SchemaModelsStack sms = new SchemaModelsStack();
        //
        // Process the path
        for (Object obj : objList) {
            SchemaComponent sComp = null;
            if (obj instanceof SchemaComponent) {
                sComp = (SchemaComponent)obj;
                StepNodeNameTest nodeTest = 
                        new StepNodeNameTest(xPathModel, sComp, sms);
                LocationStep ls = factory.newLocationStep(null, nodeTest, null);
                stepList.add(0, ls);
            } else if (obj instanceof AbstractPredicate) {
                AbstractPredicate pred = (AbstractPredicate)obj;
                sComp = pred.getSComponent();
                StepNodeNameTest nodeTest = 
                        new StepNodeNameTest(xPathModel, sComp, sms);
                LocationStep ls = factory.newLocationStep(
                        null, nodeTest, pred.getPredicates());
                stepList.add(0, ls);
            } else if (obj instanceof LocationStep) {
                stepList.add(0, (LocationStep)obj);
            } else if (obj instanceof Part) {
                part = (Part)obj;
            } else if (obj instanceof AbstractVariableDeclaration) {
                AbstractVariableDeclaration var = (AbstractVariableDeclaration)obj;
                //
                if (var instanceof VariableDeclaration) {
                    varDecl = (VariableDeclaration)var;
                } else if (var instanceof VariableDeclarationWrapper) {
                    varDecl = ((VariableDeclarationWrapper)var).getDelegate();
                }
                //
                if (varDecl == null) {
                    return null;
                }
            } else {
                break;
            }
            //
            if (sComp != null) {
                sms.appendSchemaComponent(sComp);
            } else {
                sms.discard();
            }
        }
        //
        XPathBpelVariable xPathVar = new XPathBpelVariable(varDecl, part);
        QName varQName = xPathVar.constructXPathName();
        XPathVariableReference xPathVarRef = 
                xPathModel.getFactory().newXPathVariableReference(varQName);
        //
        if (stepList.isEmpty()) {
            return xPathVarRef;
        } else {
            LocationStep[] steps = stepList.toArray(new LocationStep[stepList.size()]);
            XPathExpressionPath result = factory.newXPathExpressionPath(xPathVarRef, steps);
            return result;
        } 
    }
    
    public static String toString(RestartableIterator<Object> pathItr) {
        LinkedList<Object> list = new LinkedList<Object>();
        pathItr.restart();
        while (pathItr.hasNext()) {
            list.addFirst(pathItr.next());
        }
        //
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (Object obj : list) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append("/"); // NOI18N
            }
            sb.append(obj.toString());
        }
        //
        return sb.toString();
    }

    public static XPathSchemaContext constructContext(
            RestartableIterator<Object> pathItr) {
        //
        SchemaContextBuilder builder = new SchemaContextBuilder();
        return builder.constructContext(pathItr, null);
    }
    
    public static class SchemaContextBuilder {
        Part part = null;
        AbstractVariableDeclaration var = null;
        
        /**
         * Builds an XPathSchemaContext by a RestartableIterator. 
         * It is implied that the RestartableIterator provides a collection of 
         * tree items' data objects in order from leafs to the tree root.
         * 
         * @param pathItr
         * @return
         */
        public XPathSchemaContext constructContext(
                RestartableIterator<Object> pathItr, 
                XPathSchemaContext initialContext) {
            //
            pathItr.restart();
            //
            return constructContextImpl(pathItr, initialContext);
        }

        private VariableSchemaContext buildVariableSchemaContext() {
            VariableDeclaration varDecl = null;
            if (var instanceof VariableDeclaration) {
                varDecl = (VariableDeclaration)var;
            } else if (var instanceof VariableDeclarationWrapper) {
                varDecl = ((VariableDeclarationWrapper)var).getDelegate();
            }
            XPathBpelVariable xPathVariable = new XPathBpelVariable(varDecl, part);
            if (xPathVariable != null) {
                return new VariableSchemaContext(xPathVariable);
            }
            //
            return null;
        }
        
        private XPathSchemaContext constructContextImpl(Iterator<Object> pathItr, 
                XPathSchemaContext baseContext) {
            //
            if (!pathItr.hasNext()) {
                return baseContext;
            }
            //
            Object obj = pathItr.next(); 
            return constructContext(obj, pathItr, baseContext);
        }
        
        private XPathSchemaContext constructContext(Object obj, Iterator<Object> pathItr, 
                XPathSchemaContext baseContext) {
            //
            if (obj instanceof SchemaComponent) {
                return new SimpleSchemaContext(
                        constructContextImpl(pathItr, baseContext), 
                        (SchemaComponent)obj);
            } else if (obj instanceof AbstractPredicate) {
                return new SimpleSchemaContext(
                        constructContextImpl(pathItr, baseContext), 
                        ((AbstractPredicate)obj).getSComponent());
            } else if (obj instanceof AbstractVariableDeclaration) {
                var = (AbstractVariableDeclaration)obj;
                return buildVariableSchemaContext();
            } else if (obj instanceof Part) {
                part = (Part)obj;
                return constructContextImpl(pathItr, baseContext);
            } else if (obj instanceof AbstractTypeCast) {
                AbstractTypeCast typeCast = (AbstractTypeCast)obj;
                Object castedObj = typeCast.getCastedObject();
                XPathSchemaContext castedContext = constructContext(
                        castedObj, pathItr, 
                        constructContextImpl(pathItr, baseContext));
                return new CastSchemaContext(castedContext, typeCast);
            } else {
                return baseContext;
            }
        }
        
    }
    
}
