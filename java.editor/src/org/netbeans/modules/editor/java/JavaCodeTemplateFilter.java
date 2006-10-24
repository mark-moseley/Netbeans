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

package org.netbeans.modules.editor.java;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.EnumSet;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter;
import org.openide.ErrorManager;

/**
 *
 * @author Dusan Balek
 */
public class JavaCodeTemplateFilter implements CodeTemplateFilter, CancellableTask<CompilationController> {
    
    private boolean initialized = false;
    private int offset;
    private Tree.Kind ctx = null;
    
    private JavaCodeTemplateFilter(JTextComponent component, int offset) {
        this.offset = offset;
        JavaSource js = JavaSource.forDocument(component.getDocument());
        try {
            js.runUserActionTask(this, true);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    public synchronized boolean accept(CodeTemplate template) {
        try {
            while(!initialized)
                wait();
        } catch (InterruptedException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return ctx == null || getTemplateContexts(template).contains(ctx);
    }
    
    public void cancel() {
    }

    public synchronized void run(CompilationController controller) {
        try {
            controller.toPhase(Phase.PARSED);
            TreePath path = controller.getTreeUtilities().pathFor(offset);
            ctx = path.getLeaf().getKind();
            initialized = true;
            notifyAll();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    private EnumSet<Tree.Kind> getTemplateContexts(CodeTemplate template) {
        //TODO: rewrite this method when contexts are provided by templates
        String abbrev = template.getAbbreviation().toLowerCase();
        if (abbrev.equals("runn"))
            return EnumSet.of(Tree.Kind.BLOCK, Tree.Kind.EXPRESSION_STATEMENT);
        if (abbrev.startsWith("for") || abbrev.startsWith("while") || abbrev.equals("inst") || abbrev.startsWith("if") || abbrev.startsWith("do"))
            return EnumSet.of(Tree.Kind.BLOCK);
        return EnumSet.noneOf(Tree.Kind.class);
    }

    public static final class Factory implements CodeTemplateFilter.Factory {
        
        public CodeTemplateFilter createFilter(JTextComponent component, int offset) {
            return new JavaCodeTemplateFilter(component, offset);
        }
    }
}
