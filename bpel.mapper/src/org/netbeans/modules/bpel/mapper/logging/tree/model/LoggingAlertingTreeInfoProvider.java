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
package org.netbeans.modules.bpel.mapper.logging.tree.model;

import javax.swing.Icon;
import org.netbeans.modules.bpel.mapper.logging.tree.LoggingTreeItem;
import org.netbeans.modules.bpel.mapper.tree.models.SimpleTreeInfoProvider;
import org.netbeans.modules.soa.ui.tree.TreeItem;

/**
 * 
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class LoggingAlertingTreeInfoProvider extends SimpleTreeInfoProvider {

    private static LoggingAlertingTreeInfoProvider INSTANCE 
            = new LoggingAlertingTreeInfoProvider();
    
    public static LoggingAlertingTreeInfoProvider getInstance() {
        return INSTANCE;
    }
    
    @Override
    public String getDisplayName(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        if (dataObj instanceof LoggingTreeItem) {
            return ((LoggingTreeItem)dataObj).getDisplayName();
        }
        return super.getDisplayName(treeItem);
    }

    @Override
    public Icon getIcon(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        Icon icon = null;
        if (dataObj instanceof LoggingTreeItem) {
            icon = ((LoggingTreeItem)dataObj).getIcon();
        } else {
            icon = super.getIcon(treeItem);
        }
        return icon;
    }
}
