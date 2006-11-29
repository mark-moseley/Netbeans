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
package org.netbeans.modules.refactoring.java.ui;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.SingleCopyRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.ui.CopyClassPanel;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

/** Refactoring UI object for Copy Class refactoring.
 *
 * @author Jan Becicka
 */
public class CopyClassRefactoringUI implements RefactoringUI {
    // reference to pull up refactoring this UI object corresponds to
    private final SingleCopyRefactoring refactoring;
    // UI panel for collecting parameters
    private CopyClassPanel panel;
    private FileObject resource;
    private FileObject targetFolder;
    private PasteType paste;
    
    public CopyClassRefactoringUI(FileObject resource, FileObject target, PasteType paste) {
        refactoring = new SingleCopyRefactoring(resource);
        this.resource = resource;
        this.targetFolder = target;
        this.paste=paste;
    }
    
    // --- IMPLEMENTATION OF RefactoringUI INTERFACE ---------------------------
    
    public boolean isQuery() {
        return false;
    }

    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new CopyClassPanel(parent,
                    getName() + " - " + resource.getName(), 
                    RetoucheUtils.getPackageName(targetFolder!=null?targetFolder:resource.getParent()), 
                    resource);
            panel.setCombosEnabled(!(targetFolder!=null));
        }
        return panel;
    }

    public Problem setParameters() {
        setupRefactoring();
        return refactoring.checkParameters();
    }
    
    public Problem checkParameters() {
        if (panel==null)
            return null;
        setupRefactoring();
        return refactoring.fastCheckParameters();
    }
    
    private void setupRefactoring() {
        refactoring.setNewName(panel.getNewName());
        URL url = URLMapper.findURL(panel.getRootFolder(), URLMapper.EXTERNAL);
        try {
            refactoring.setTarget(new URL(url.toExternalForm() + "/" + panel.getPackageName().replace('.','/')));
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }

    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    public String getDescription() {
        return NbBundle.getMessage(CopyClassRefactoringUI.class, "DSC_CopyClass", refactoring.getNewName()); // NOI18N
    }

    public String getName() {
        return NbBundle.getMessage(CopyClassRefactoringUI.class, "LBL_CopyClass"); // NOI18N
    }

    public boolean hasParameters() {
        return true;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(CopyClassRefactoringUI.class.getName());
    }
}