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

package org.netbeans.modules.bpel.mapper.tree.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.bpel.mapper.predicates.editor.PathConverter;
import org.netbeans.modules.bpel.mapper.cast.AbstractTypeCast;
import org.netbeans.modules.bpel.mapper.cast.CastManager;
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTcContext;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTreeModel;
import org.netbeans.modules.bpel.mapper.tree.spi.RestartableIterator;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContext;
import org.openide.util.NbBundle;

/**
 *
 * @author nk160297
 */
public class DeleteCastAction extends MapperAction<RestartableIterator<Object>> {
    
    private static final long serialVersionUID = 1L;
    private boolean mInLeftTree;
    private TreePath mTreePath;
    
    public DeleteCastAction(MapperTcContext mapperTcContext, 
            boolean inLeftTree, TreePath treePath, 
            RestartableIterator<Object> doItr) {
        super(mapperTcContext, doItr);
        mTreePath = treePath;
        mInLeftTree = inLeftTree;
        postInit();
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(MapperAction.class, "DELETE_CAST"); // NOI18N
    }
    
    public void actionPerformed(ActionEvent e) {
        RestartableIterator<Object> itr = getActionSubject();
        itr.restart();
        Object nextObj = itr.next();
        assert nextObj instanceof AbstractTypeCast;
        AbstractTypeCast typeCast = (AbstractTypeCast)nextObj;
        //
        XPathSchemaContext sContext = typeCast.getSchemaContext();
        if (sContext == null) {
            sContext = PathConverter.constructContext(itr);
        }
        //
//        PredicateUpdater updater = new PredicateUpdater(mMapperTcContext, 
//                null, typeCast, sContext, mInLeftTree, mTreePath);
//        updater.deletePredicate();
        //
        MapperModel mm = mMapperTcContext.getMapper().getModel();
        assert mm instanceof BpelMapperModel;
        BpelMapperModel mModel = (BpelMapperModel)mm;
        //
        MapperSwingTreeModel treeModel = null;
        if (mInLeftTree) {
            treeModel = mModel.getLeftTreeModel();
        } else {
            treeModel = mModel.getRightTreeModel();
        }
        MapperTreeModel sourceModel = treeModel.getSourceModel();
        //
        // Calculate predicate location index
        // int predIndex = treeModel.getChildIndex(mTreePath.getParentPath(), mPred);
        //
        CastManager castManager = CastManager.getCastManager(sourceModel);
        if (castManager != null) {
            castManager.removeTypeCast(typeCast);
        }
        //
        // Update BPEL model
        if (mInLeftTree) {
            List<TreePath> dependentGraphs = mModel.getDependentGraphs(typeCast);
            for (TreePath graphPath : dependentGraphs) {
                mModel.removeIngoingLinks(graphPath, mTreePath);
            }
            //
            // Modify BPEL model for all changed graphs in one transaction.
            mModel.fireGraphsChanged(dependentGraphs);
        } else {
            mModel.removeNestedGraphs(mTreePath);
        }
        //
        // Remove node from the tree
        treeModel.remove(mTreePath);
    }
    
}
