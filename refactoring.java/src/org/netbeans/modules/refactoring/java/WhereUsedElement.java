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
package org.netbeans.modules.refactoring.java;

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.io.IOException;
import javax.swing.text.Position.Bias;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.refactoring.java.plugins.JavaWhereUsedQueryPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImpl;
import org.netbeans.modules.refactoring.java.ui.tree.ElementGripFactory;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import static org.netbeans.modules.refactoring.java.RetoucheUtils.*;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionRef;

public class WhereUsedElement extends SimpleRefactoringElementImpl {
    private PositionBounds bounds;
    private String displayText;
    private FileObject parentFile;
    public WhereUsedElement(PositionBounds bounds, String displayText, FileObject parentFile, TreePath tp, CompilationInfo info) {
        this.bounds = bounds;
        this.displayText = displayText;
        this.parentFile = parentFile;
        ElementGripFactory.getDefault().put(parentFile, tp, info);
    }

    public String getDisplayText() {
        return displayText;
    }

    public Object getComposite() {
        Object composite = ElementGripFactory.getDefault().get(parentFile, bounds.getBegin().getOffset());
        if (composite==null) 
            composite = parentFile;
        return composite;
    }

    public PositionBounds getPosition() {
        return bounds;
    }

    public String getText() {
        return displayText;
    }

    public void performChange() {
    }

    public FileObject getParentFile() {
        return parentFile;
    }
    
    public static WhereUsedElement create(CompilationInfo compiler, TreePath tree) {
        CompilationUnitTree unit = tree.getCompilationUnit();
        CharSequence content = null;
        try {
            content = unit.getSourceFile().getCharContent(true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        SourcePositions sp = compiler.getTrees().getSourcePositions();
        int start = (int)sp.getStartPosition(unit, tree.getLeaf());
        int end   = (int)sp.getEndPosition(unit, tree.getLeaf());
        Tree t= tree.getLeaf();
        if (end == -1) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new RuntimeException("Cannot get end position for " + t.getClass().getName()+ " " + t + " file:" + compiler.getFileObject().getPath()));
            end=start;
        }
        if (t.getKind() == Tree.Kind.CLASS) {
            //this is strange - how to get position of start of name?
            start = start + t.toString().trim().indexOf(((ClassTree)t).getSimpleName().toString());
            end = start + ((ClassTree)t).getSimpleName().toString().length();
        }
        if (t.getKind() == Tree.Kind.METHOD) {
            start = start + t.toString().indexOf(((MethodTree)t).getName().toString())+1;
            end = start + ((MethodTree)t).getName().toString().length();
        }
        LineMap lm = tree.getCompilationUnit().getLineMap();
        long line = lm.getLineNumber(start);
        long sta = lm.getStartPosition(line);
        long en = lm.getStartPosition(line+1)-1;
        StringBuffer sb = new StringBuffer();
        sb.append(RetoucheUtils.getHtml(content.subSequence((int)sta,(int)start).toString().trim()));
        sb.append(" <b>");
        sb.append(content.subSequence((int)start,(int)end));
        sb.append("</b> ");
        sb.append(RetoucheUtils.getHtml(content.subSequence((int)end,(int)en).toString().trim()));
        
        DataObject dob = null;
        try {
            dob = DataObject.find(compiler.getFileObject());
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
        }
        CloneableEditorSupport ces = JavaWhereUsedQueryPlugin.findCloneableEditorSupport(dob);
        PositionRef ref1 = ces.createPositionRef(start, Bias.Forward);
        PositionRef ref2 = ces.createPositionRef(end, Bias.Forward);
        PositionBounds bounds = new PositionBounds(ref1, ref2);
        TreePath tr = getEnclosingTree(tree);
        return new WhereUsedElement(bounds, sb.toString().trim(), compiler.getFileObject(), tr, compiler);
    }
    
    private static TreePath getEnclosingTree(TreePath tp) {
        while(tp != null) {
            Tree tree = tp.getLeaf();
            if (tree.getKind() == Tree.Kind.CLASS || tree.getKind() == Tree.Kind.METHOD || tree.getKind() == Tree.Kind.IMPORT) {
                return tp;
            } 
            tp = tp.getParentPath();
        }
        return null;
    }

}
