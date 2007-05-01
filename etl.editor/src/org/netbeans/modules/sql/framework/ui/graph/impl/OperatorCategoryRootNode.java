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
package org.netbeans.modules.sql.framework.ui.graph.impl;

import org.openide.loaders.DataFolder;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class OperatorCategoryRootNode extends DataNode {

    private DataFolder folder;

    /** Creates a new instance of OperatorCategoryRootNode */
    public OperatorCategoryRootNode(DataObject obj) {
        super(obj, new Children.Array());
        folder = (DataFolder) obj.getCookie(DataFolder.class);
        createOperatorCategories();
    }

    private void createOperatorCategories() {
        DataObject[] children = folder.getChildren();

        for (int i = 0; i < children.length; i++) {
            DataObject obj = children[i];
            OperatorCategoryNode catNode = new OperatorCategoryNode(obj);
            this.getChildren().add(new Node[] { catNode});

        }

    }

    public SystemAction[] getActions() {
        return null;
    }
}

