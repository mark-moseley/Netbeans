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
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.web.refactoring.RefactoringUtil;
import org.openide.filesystems.FileObject;

/**
 * Handles package rename.
 *
 * @author Erno Mononen
 */
public class WebXmlPackageRename extends BaseWebXmlRename{
    
    /**
     * The folder or package being renamed.
     */
    private final FileObject pkg;
    private final RenameRefactoring rename;
    
    public WebXmlPackageRename(FileObject webDD, WebApp webModel, FileObject pkg, RenameRefactoring rename) {
        super(webDD, webModel);
        this.pkg = pkg;
        this.rename = rename;
    }
    
    protected List<RenameItem> getRenameItems() {
        List<RenameItem> result = new ArrayList<RenameItem>();
        List<FileObject> fos = new ArrayList<FileObject>();
        RefactoringUtil.collectChildren(pkg, fos);
        for (FileObject each : fos){
            String oldFqn = RefactoringUtil.getQualifiedName(each);
            String newFqn = RefactoringUtil.constructNewName(each, rename);
            result.add(new RenameItem(newFqn, oldFqn));
        }
        return result;
    }
    
    protected AbstractRefactoring getRefactoring() {
        return rename;
    }
    
}
