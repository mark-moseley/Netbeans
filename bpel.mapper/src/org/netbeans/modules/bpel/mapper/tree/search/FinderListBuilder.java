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

package org.netbeans.modules.bpel.mapper.tree.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.bpel.mapper.predicates.AbstractPredicate;
import org.netbeans.modules.bpel.mapper.predicates.XPathPredicate;
import org.netbeans.modules.bpel.mapper.tree.spi.TreeItemFinder;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.support.XPathBpelVariable;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xpath.ext.AbstractLocationPath;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.StepNodeTest;
import org.netbeans.modules.xml.xpath.ext.StepNodeTypeTest;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;
import org.netbeans.modules.xml.xpath.ext.spi.VariableSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathVariable;

/**
 *
 * @author nk160297
 */
public class FinderListBuilder {

    public static List<TreeItemFinder> build(XPathSchemaContext schemaContext) {
        ArrayList<TreeItemFinder> finderList = new ArrayList<TreeItemFinder>();
        //
        LinkedList<Object> result = new LinkedList<Object>();
        //
        XPathSchemaContext context = schemaContext;
        // 
        while (context != null) { 
            if (context instanceof VariableSchemaContext) {
                XPathVariable var = ((VariableSchemaContext)context).getVariable();
                assert var instanceof XPathBpelVariable;
                XPathBpelVariable bpelVar = (XPathBpelVariable)var;
                VariableDeclaration varDecl = bpelVar.getVarDecl();
                VariableFinder varFinder = new VariableFinder(varDecl);
                finderList.add(varFinder);
                //
                Part part = bpelVar.getPart();
                if (part != null) {
                    PartFinder partFinder = new PartFinder(part);
                    finderList.add(partFinder);
                }
            } else {
                SchemaComponent sComp = XPathSchemaContext.Utilities.
                        getSchemaComp(context);
                if (sComp == null) {
                    return null;
                }
                result.addFirst(sComp);
            }
            //
            context = context.getParentContext();
        }
        //
        if (!result.isEmpty()) {
            PathFinder pathFinder = new PathFinder(result);
            finderList.add(pathFinder);
        }
        //
        return finderList;
    }
    
    public static List<TreeItemFinder> build(XPathVariableReference varRef) {
        ArrayList<TreeItemFinder> finderList = new ArrayList<TreeItemFinder>();
        //
        XPathVariable var = varRef.getVariable();
        // Variable could be deleted but the reference no.
        // issue 128684
        if (var == null) {
            return finderList;
        }
        assert var instanceof XPathBpelVariable;
        XPathBpelVariable bpelVar = (XPathBpelVariable)var;
        VariableDeclaration varDecl = bpelVar.getVarDecl();
        VariableFinder varFinder = new VariableFinder(varDecl);
        finderList.add(varFinder);
        //
        Part part = bpelVar.getPart();
        if (part != null) {
            PartFinder partFinder = new PartFinder(part);
            finderList.add(partFinder);
        }
        return finderList;
    }

    public static List<TreeItemFinder> build(AbstractLocationPath locationPath) {
        ArrayList<TreeItemFinder> finderList = new ArrayList<TreeItemFinder>();
        //
        if (locationPath instanceof XPathExpressionPath) {
            XPathExpression rootExpr = 
                    ((XPathExpressionPath)locationPath).getRootExpression();
            if (rootExpr instanceof XPathVariableReference) {
                finderList.addAll(build((XPathVariableReference)rootExpr));
            }
        }
        //
        ArrayList<Object> result = new ArrayList<Object>();
        LocationStep[] stepArr = locationPath.getSteps();
        //
        for (LocationStep step : stepArr) {
            //
            StepNodeTest stepNodeTest = step.getNodeTest();
            if (stepNodeTest instanceof StepNodeTypeTest) {
                result.add(step);
                continue;
            }
            //
            XPathSchemaContext sContext = step.getSchemaContext();
            if (sContext == null) {
                // it didn't manage to resolve a schema context for 
                // the step
                return Collections.EMPTY_LIST;
            }
            SchemaComponent stepSchemaComp = 
                    XPathSchemaContext.Utilities.getSchemaComp(sContext);
            if (stepSchemaComp == null) {
                return Collections.EMPTY_LIST;
            }
            //
            XPathPredicateExpression[] predArr = step.getPredicates();
            if (predArr == null || predArr.length == 0) {
                result.add(stepSchemaComp);
            } else {
                AbstractPredicate pred = new XPathPredicate(step);
                result.add(pred);
            }
        }
        //
        if (!result.isEmpty()) {
            PathFinder pathFinder = new PathFinder(result);
            finderList.add(pathFinder);
        }
        //
        return finderList;
    }
    
}
