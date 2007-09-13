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

package org.netbeans.modules.cnd.refactoring.ui.tree;

import java.beans.BeanInfo;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.netbeans.modules.refactoring.spi.ui.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * TreeElement to represent Files
 * 
 * @author Vladimir Voskresensky
 */
public class FileTreeElement implements TreeElement {

    private final FileObject fo;
    private final CsmFile csmFile;
    FileTreeElement(FileObject fo, CsmFile csmFile) {
        this.fo = fo;
        this.csmFile = csmFile;
    }


    public TreeElement getParent(boolean isLogical) {
        return TreeElementFactory.getTreeElement(csmFile.getProject());
    }

    public Icon getIcon() {
        try {
            return new ImageIcon(DataObject.find(fo).getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));
        } catch (DataObjectNotFoundException ex) {
            return null;
        }
    }

    public String getText(boolean isLogical) {
        return fo.getNameExt();
    }

    public Object getUserObject() {
        return csmFile;
    }
}
