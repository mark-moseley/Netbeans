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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.util.TreePath;
import javax.lang.model.element.Element;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.ExtractInterfaceRefactoring;
import org.netbeans.modules.refactoring.java.ui.ExtractInterfaceAction;
import org.netbeans.modules.refactoring.java.ui.ExtractInterfacePanel;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Refactoring UI object for Extract Interface refactoring.
 *
 * @author Martin Matula, Jan Becicka, Jan Pokorsky
 */
public final class ExtractInterfaceRefactoringUI implements RefactoringUI {
    // reference to extract interface refactoring this UI object corresponds to
    private final ExtractInterfaceRefactoring refactoring;
    // source type
    private final TreePathHandle sourceType;
    // UI panel for collecting parameters
    private ExtractInterfacePanel panel;
    private String name;
    
    /** Creates a new instance of ExtractInterfaceRefactoringUI
     * @param selectedElement Elements the refactoring action was invoked on.
     */
    public ExtractInterfaceRefactoringUI(TreePathHandle selectedElement, CompilationInfo info) {
        // compute source type
        TreePath path = selectedElement.resolve(info);
        path = RetoucheUtils.findEnclosingClass(info, path, true, true, true, true, false);
        
        this.name = UiUtils.getHeader(path, info, UiUtils.PrintPart.NAME);
        this.sourceType = TreePathHandle.create(path, info);
        // create an instance of pull up refactoring object
        refactoring = new ExtractInterfaceRefactoring(sourceType);
        refactoring.getContext().add(info.getClasspathInfo());
    }
    
    // --- IMPLEMENTATION OF RefactoringUI INTERFACE ---------------------------
    
    public boolean isQuery() {
        return false;
    }

    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new ExtractInterfacePanel(refactoring, parent);
        }
        return panel;
    }

    public Problem setParameters() {
        captureParameters();
        return refactoring.checkParameters();
    }
    
    public Problem checkParameters() {
        captureParameters();
        return refactoring.fastCheckParameters();
    }

    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    public String getDescription() {
        return NbBundle.getMessage(ExtractInterfaceAction.class, "DSC_ExtractInterface", name); // NOI18N
    }

    public String getName() {
        return NbBundle.getMessage(ExtractInterfaceAction.class, "LBL_ExtractInterface"); // NOI18N
    }

    public boolean hasParameters() {
        return true;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ExtractInterfaceRefactoringUI.class.getName());
    }
    
    // --- PRIVATE HELPER METHODS ----------------------------------------------
    
    /** Gets parameters from the refactoring panel and sets them
     * to the refactoring object.
     */
    private void captureParameters() {
        panel.storeSettings();
    }
    
}