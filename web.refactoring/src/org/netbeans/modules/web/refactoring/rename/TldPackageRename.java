/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.refactoring.rename;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.web.refactoring.RefactoringUtil;
import org.openide.filesystems.FileObject;

/**
 * Handles package and folder rename in tld files.
 *
 * @author Erno Mononen
 */
public class TldPackageRename extends BaseTldRename{
    
    private final RenameRefactoring rename;
    
    public TldPackageRename(RenameRefactoring rename, FileObject source) {
        super(source);
        this.rename = rename;
    }

    
    protected List<RenameItem> getAffectedClasses() {
        List<FileObject> affectedClasses = new ArrayList<FileObject>();
        RefactoringUtil.collectChildren(source, affectedClasses);
        List<RenameItem> result = new ArrayList<RenameItem>();
        for (FileObject affected : affectedClasses){
            String oldName = RefactoringUtil.getQualifiedName(affected);
            String newName = RefactoringUtil.constructNewName(affected, rename);
            result.add(new RenameItem(newName, oldName));
        }
        return result;
    }

    protected AbstractRefactoring getRefactoring() {
        return rename;
    }
    
}
