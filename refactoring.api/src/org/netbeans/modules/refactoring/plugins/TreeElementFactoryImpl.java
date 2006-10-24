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

package org.netbeans.modules.refactoring.plugins;

import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactoryImplementation;
import org.openide.filesystems.FileObject;


/**
 *
 * @author Jan Becicka
 */
public class TreeElementFactoryImpl implements TreeElementFactoryImplementation {

    private Map<Object, TreeElement> map = new WeakHashMap();

    public TreeElement getTreeElement(Object o) {
        TreeElement result = map.get(o);
        if (result!= null)
            return result;
        if (o instanceof RefactoringElement) {
            if (((RefactoringElement) o).getComposite() instanceof FileObject) {
                result = new RefactoringTreeElement((RefactoringElement) o);
            }
        }
        if (result != null) {
            map.put(o, result);
        }
        return result;
    }

    public void cleanUp() {
        map.clear();
    }
}
