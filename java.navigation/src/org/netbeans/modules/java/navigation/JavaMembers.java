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

package org.netbeans.modules.java.navigation;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.openide.ErrorManager;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.swing.JDialog;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Sandip Chitale (Sandip.Chitale@Sun.Com)
 */
public final class JavaMembers {

    /**
     * Show the members of the types in the fileObject.
     * 
     * @param fileObject 
     */
    public static void show(final FileObject fileObject) {
        if (fileObject != null) {
            JavaSource javaSource = JavaSource.forFileObject(fileObject);

            if (javaSource != null) {
                try {
                    javaSource.runUserActionTask(new Task<CompilationController>() {

                            public void run(
                                CompilationController compilationController)
                                throws Exception {
                                compilationController.toPhase(Phase.ELEMENTS_RESOLVED);

                                Trees trees = compilationController.getTrees();
                                CompilationUnitTree compilationUnitTree = compilationController.getCompilationUnit();
                                List<?extends Tree> typeDecls = compilationUnitTree.getTypeDecls();

                                Set<Element> elementsSet = new LinkedHashSet<Element>(typeDecls.size() +
                                        1);

                                for (Tree tree : typeDecls) {
                                    Element element = trees.getElement(trees.getPath(
                                                compilationUnitTree, tree));

                                    if (element != null) {
                                        if (elementsSet.size() == 0) {
                                            Element enclosingElement = element.getEnclosingElement();

                                            if ((enclosingElement != null) &&
                                                    (enclosingElement.getKind() == ElementKind.PACKAGE)) {
                                                // add package
                                                elementsSet.add(enclosingElement);
                                            }
                                        }

                                        elementsSet.add(element);
                                    }
                                }

                                Element[] elements = elementsSet.toArray(JavaMembersModel.EMPTY_ELEMENTS_ARRAY);
                                show(fileObject, elements, compilationController);
                            }
                        }, true);

                    return;
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                }
            }
        }
    }

    public static void show(FileObject fileObject, Element[] elements, CompilationController compilationController) {
        if (fileObject != null) {
            JDialog dialog = ResizablePopup.getDialog();
            dialog.setTitle(NbBundle.getMessage(JavaMembers.class, "TITLE_Members")); // NOI18N
            dialog.setContentPane(new JavaMembersPanel(fileObject, elements, compilationController));
            dialog.setVisible(true);
        }
    }

}
