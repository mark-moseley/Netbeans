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

package org.netbeans.modules.bpel.mapper.predicates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.netbeans.modules.bpel.mapper.predicates.editor.PathConverter;
import org.netbeans.modules.bpel.mapper.tree.spi.RestartableIterator;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;

/**
 * The class collects all predicate expressions which are in the edited 
 * XPath expression. Each predicate is declared in the XPath location step.
 * The location step is bound to the specific schema element or attribute.
 * The predicate manager keeps the location of predicate in term of
 * schema components' path.
 *
 * The set of predicates is populated from XPath model(s). The manager can
 * be populated entirely or can track small model's changes only.
 * It produces notification messages about adding, editing or deleting of
 * the predicates set.
 *
 * The main intention of the predicate manager is to provide showing of
 * predicates in the mapper source and destination trees.
 * 
 * TODO: remove unused code later. It is commented now.
 *
 * @author nk160297
 */
public class PredicateManager {
    
    // The cache of predicates.
    private LinkedList<CachedPredicate> mPredicates;
    
    public PredicateManager() {
        mPredicates = new LinkedList<CachedPredicate>();
    }
    
    public List<AbstractPredicate> getPredicates(
            RestartableIterator<Object> parentPath, SchemaComponent sComp) {
        //    
        ArrayList<AbstractPredicate> result = new ArrayList<AbstractPredicate>();
        
        for (CachedPredicate cPred : mPredicates) {
            if (cPred.hasSameBase(sComp) && cPred.hasSameLocation(parentPath)) {
                result.add(cPred.getPredicate());
            }
        }
        //
        return result;
    }
    
    public void addPredicate(List<Object> parentPath, AbstractPredicate pred) {
        CachedPredicate cPredicate = new CachedPredicate(parentPath, pred);
        mPredicates.add(cPredicate);
    }

    public boolean addPredicate(RestartableIterator<Object> parentItr, 
            AbstractPredicate pred) {
        //
        List<Object> parentPath = 
                PathConverter.constructPredicateLocationtList(parentItr);
        //
        if (parentPath != null) {
            CachedPredicate cPredicate = new CachedPredicate(parentPath, pred);
            mPredicates.add(cPredicate);
            return true;
        }
        //
        return false;
    }
    
    public void removePredicate(AbstractPredicate predToDelete) {
        for (CachedPredicate cPred : mPredicates) {
            AbstractPredicate pred = cPred.getPredicate();
            if (pred.equals(predToDelete)) {
                mPredicates.remove(pred);
                break;
            }
        }
    }
    
    public static String toString(XPathPredicateExpression[] predicatesArr) {
        if (predicatesArr != null && predicatesArr.length != 0) {
            StringBuilder sb = new StringBuilder();
            for (XPathPredicateExpression predicate : predicatesArr) {
                sb.append(predicate.getExpressionString());
            }
            return sb.toString();
        } else {
            return "";
        }
    }
    
    /**
     * This class holds the predicate itself (PredicatedSchemaComp) + 
     * its location and the flag persistent. 
     * 
     * ATTENTION!
     * The location is the different notion relative to the XPathSchemaContext.
     * The schema context consists from SchemaComponent objects only. 
     * The location can contain a Variable, a Part, SchemaComponent and 
     * PredicatedSchemaComp objects. 
     */
    public static class CachedPredicate {
        // The list contains data objects from which a tree path consists of
        // It is implied that it can contain a set of SchemaComponents and 
        // PredicatedSchemaComp. And there is either a variable or 
        // a variable with a part at the end. 
        // The first element is the parent of the predicate!
        // The predicated schema component isn't in the list itself.
        // It it held in the separate attribute mPredSComp.
        private List<Object> mParentPath;

        // The Schema component which is the base for the predicate.
        private AbstractPredicate mPred;
        
        // Persistense means that the instance should not be automatically
        // deleted from the cache if it is not used.
        // The predicates which are not persistent will be removed from
        // the cache automatically.
        private boolean isPersistent;

        public CachedPredicate(List<Object> parentPath, AbstractPredicate pred) {
            mParentPath = parentPath;
            mPred = pred;
        }
        
        public boolean isPersistent() {
            return isPersistent;
        }
        
        public void setPersistent(boolean newValue) {
            isPersistent = newValue;
        }
        
        public SchemaComponent getBaseType() {
            return mPred.getSComponent();
        }
        
        public AbstractPredicate getPredicate() {
            return mPred;
        }
        
        /**
         * Returns the list of data objects which indicate the location of 
         * the predicate in the tree. The first element of the list points 
         * to the parent element of the predicate.
         */
        public List getParentPath() {
            return mParentPath;
        }
        
        public boolean hasSameBase(SchemaComponent baseSchemaComp) {
            return getPredicate().getSComponent().equals(baseSchemaComp);
        }
        
        /**
         * Check if the cached predicate has the same schema component 
         * and the same predicates.
         */
        public boolean hasSameParams(SchemaComponent schemaComp,
                XPathPredicateExpression[] predArr) {
            AbstractPredicate pComp = getPredicate();
            return pComp.getSComponent().equals(schemaComp) &&
                    pComp.hasSamePredicates(predArr);
        }
        
        public boolean hasSameParams(AbstractPredicate pred) {
            AbstractPredicate pComp = getPredicate();
            return pComp.equals(pred);
        }
        
        public boolean hasSameLocation(RestartableIterator parentPathItr) {
            parentPathItr.restart();
            //
            Iterator internalItr = mParentPath.iterator();
            boolean theSame = true;
            while (parentPathItr.hasNext() && internalItr.hasNext()) {
                Object parentPathStep = parentPathItr.next();
                Object internalPathStep = internalItr.next();
                if (!parentPathStep.equals(internalPathStep)) {
                    theSame = false;
                    break;
                }
            }
            //
            if (theSame && internalItr.hasNext()) {
                // internal location path longer then required. 
                // It isn't allowed. It can be shorter only. 
                return false;
            }
            //
            return theSame;
        }
        
        @Override
        public String toString() {
            String endText = mPred.toString() + " persistent=" + isPersistent; // NOI18N
            //
            String parentPath = locationToString();
            if (parentPath == null || parentPath.length() == 0) {
                return endText;
            } else {
                return parentPath + " " + endText;
            }
        }
        
        private String locationToString() {
            StringBuilder sb = new StringBuilder();
            ListIterator itr = mParentPath.listIterator(mParentPath.size());
            boolean isFirst = true;
            while (itr.hasPrevious()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    sb.append("/");
                }
                //
                Object stepObj = itr.previous();
                sb.append(stepObj.toString());
            }
            return sb.toString();
        }
        
        @Override
        public boolean equals(Object obj2) {
            if (!(obj2 instanceof CachedPredicate)) {
                return false;
            }
            //
            CachedPredicate pred2 = (CachedPredicate)obj2;
            if (!pred2.getPredicate().equals(mPred)) {
                return false;
            }
            //
            List path2 = pred2.getParentPath();
            if (path2.size() != mParentPath.size()) {
                // Pathes have diferrent length
                return false;
            }
            //
            Iterator itr = mParentPath.listIterator();
            Iterator itr2 = path2.listIterator();
            //
            while (itr.hasNext()) { // Pathes have the same length!
                Object dataObj = itr.next();
                Object dataObj2 = itr2.next();
                if (!(dataObj.equals(dataObj2))) {
                    return false;
                }
            }
            //
            return true;
        }
    }
    
}
  
