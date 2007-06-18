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
import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class JavaCodeTemplateFilter implements CodeTemplateFilter, Task<CompilationController> {
    
    private static final Logger LOG = Logger.getLogger(JavaCodeTemplateFilter.class.getName());
    
    private int startOffset;
    private int endOffset;
    private Tree.Kind ctx = null;
    
    private JavaCodeTemplateFilter(JTextComponent component, int offset) {
        this.startOffset = offset;
        this.endOffset = component.getSelectionStart() == offset ? component.getSelectionEnd() : -1;            
        JavaSource js = JavaSource.forDocument(component.getDocument());
        if (js != null) {
            try {
                if (SourceUtils.isScanInProgress()) {
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(JavaCodeTemplateFilter.class, "JCT-scanning-in-progress")); //NOI18N
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    js.runUserActionTask(this, true);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public synchronized boolean accept(CodeTemplate template) {
        return ctx != null && getTemplateContexts(template).contains(ctx);
    }
    

    public synchronized void run(CompilationController controller) throws IOException {
        controller.toPhase(Phase.PARSED);
        Tree tree = controller.getTreeUtilities().pathFor(startOffset).getLeaf();
        if (endOffset >= 0 && startOffset != endOffset) {
            if (controller.getTreeUtilities().pathFor(endOffset).getLeaf() != tree)
                return;
        }
        ctx = tree.getKind();
    }

    private EnumSet<Tree.Kind> getTemplateContexts(CodeTemplate template) {
        List<String> contexts = template.getContexts();
        List<Tree.Kind> kinds = new ArrayList<Tree.Kind>();
        
        if (contexts != null) {
            for(String ctx : contexts) {
                Tree.Kind kind = Tree.Kind.valueOf(ctx);
                if (kind != null) {
                    kinds.add(kind);
                } else {
                    LOG.warning("Invalid code template context '" + ctx + "', ignoring."); //NOI18N
                }
            }
        }
        
        if (kinds.size() > 0) {
            return EnumSet.copyOf(kinds);
        } else {
            return EnumSet.noneOf(Tree.Kind.class);
        }
    }

    public static final class Factory implements CodeTemplateFilter.Factory {
        
        public CodeTemplateFilter createFilter(JTextComponent component, int offset) {
            return new JavaCodeTemplateFilter(component, offset);
        }
    }
}
