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

package org.netbeans.modules.bpel.mapper.tree;

import javax.swing.Icon;
import javax.swing.JPopupMenu;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.soa.mappercore.DefaultMapperContext;
import org.netbeans.modules.soa.mappercore.model.MapperModel;

/**
 * The default implementation of the MapperContext interface for the BPEL Mapper.
 *
 * @author nk160297
 */
public class BpelMapperContext extends DefaultMapperContext {
    
    @Override
    public String getLeftDysplayText(MapperModel model, Object value) {
        return ((MapperSwingTreeModel) model.getLeftTreeModel())
                .getDisplayName(value);
    }

    @Override
    public Icon getLeftIcon(MapperModel model, Object value, Icon defaultIcon) {
        return ((MapperSwingTreeModel) model.getLeftTreeModel()).getIcon(value);
    }

    @Override
    public JPopupMenu getLeftPopupMenu(MapperModel model, Object value) {
        return ((MapperSwingTreeModel) model.getLeftTreeModel())
                .getPopupMenu(value);
    }

    //==========================================================================
    
    @Override
    public String getRightDysplayText(MapperModel model, Object value) {
        assert model instanceof BpelMapperModel;
        return ((BpelMapperModel)model).getRightTreeModel().getDisplayName(value);
    }

    @Override
    public Icon getRightIcon(MapperModel model, Object value, Icon defaultIcon) {
        assert model instanceof BpelMapperModel;
        return ((BpelMapperModel)model).getRightTreeModel().getIcon(value);
    }

    @Override
    public JPopupMenu getRightPopupMenu(MapperModel model, Object value) {
        return ((BpelMapperModel) model).getRightTreeModel().getPopupMenu(value);
    }
}
