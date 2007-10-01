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
package org.netbeans.modules.java.hints;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.IOException;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.java.hints.infrastructure.HintAction;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class ConvertAnonymousToInnerAction extends HintAction {

    public ConvertAnonymousToInnerAction() {
        putValue(NAME, NbBundle.getMessage(ConvertAnonymousToInnerAction.class, "CTL_ConvertAnonymousToInner"));
    }

    protected void perform(JavaSource js,final int[] selection) {
        final Fix[] f = new Fix[1];
        String error = null;
        
        if (selection[0] == selection[1]) {
            try {
                js.runUserActionTask(new Task<CompilationController>() {

                    public void run(CompilationController parameter) throws Exception {
                        parameter.toPhase(JavaSource.Phase.RESOLVED);
                        TreePath path = parameter.getTreeUtilities().pathFor(selection[0]);
                        
                        while (path != null && path.getLeaf().getKind() != Kind.NEW_CLASS)
                            path = path.getParentPath();
                        
                        if (path == null)
                            return ;
                        
                        f[0] = ConvertAnonymousToInner.computeFix(parameter, path, -1);
                    }
                }, true);
                
                if (f[0] == null) {
                    error = "ERR_CaretNotInAnonymousInnerclass";
                }
            } catch (IOException e) {
                error = "ERR_SelectionNotSupported";
                Exceptions.printStackTrace(e);
            }
        } else {
            error = "ERR_SelectionNotSupported";
        }
        
        if (f[0] != null) {
            try {
                f[0].implement();
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
            
            return ;
        }
        
        if (error != null) {
            String errorText = NbBundle.getMessage(ConvertAnonymousToInnerAction.class, error);
            NotifyDescriptor nd = new NotifyDescriptor.Message(errorText, NotifyDescriptor.ERROR_MESSAGE);
            
            DialogDisplayer.getDefault().notifyLater(nd);
        }
    }

    @Override
    protected boolean requiresSelection() {
        return false;
    }

}
