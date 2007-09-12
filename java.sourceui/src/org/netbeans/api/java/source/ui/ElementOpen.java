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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.api.java.source.ui;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import javax.lang.model.element.Element;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/** Utility class for opening elements in editor.
 *
 * @author Jan Lahoda
 */
public final class ElementOpen {

    private ElementOpen() {
    }
    
    /**
     * Opens given {@link Element}.
     * 
     * @param cpInfo ClasspathInfo which should be used for the search
     * @param el    declaration to open
     * @return true if and only if the declaration was correctly opened,
     *                false otherwise
     */
    public static boolean open(final ClasspathInfo cpInfo, final Element el) {
	Object[] openInfo = getOpenInfo (cpInfo, el);
	if (openInfo != null) {
	    assert openInfo[0] instanceof FileObject;
	    assert openInfo[1] instanceof Integer;
	    return doOpen((FileObject)openInfo[0],(Integer)openInfo[1]);
	}
	return false;
    }
    
    /**
     * Opens given {@link Element}.
     * 
     * @param toSearch fileobject whose {@link ClasspathInfo} will be used
     * @param toOpen   {@link ElementHandle} of the element which should be opened.
     * @return true if and only if the declaration was correctly opened,
     *                false otherwise
     */
    public static boolean open(final FileObject toSearch, final ElementHandle<? extends Element> toOpen) {
        if (toSearch == null || toOpen == null) {
            throw new IllegalArgumentException("null not supported");
        }
        
        Object[] openInfo = getOpenInfo (toSearch, toOpen);
        if (openInfo != null) {
            assert openInfo[0] instanceof FileObject;
            assert openInfo[1] instanceof Integer;
            return doOpen((FileObject)openInfo[0],(Integer)openInfo[1]);
        }
        return false;
    }
    
    
    // Private methods ---------------------------------------------------------
        
    private static Object[] getOpenInfo (final ClasspathInfo cpInfo, final Element el) {
        FileObject fo = SourceUtils.getFile(el, cpInfo);
        if (fo != null) {
            return getOpenInfo(fo, ElementHandle.create(el));
        } else {
            return null;
        }
    }
    
    private static Object[] getOpenInfo(final FileObject fo, final ElementHandle<? extends Element> handle) {
        assert fo != null;
        
        try {
            int offset = getOffset(fo, handle);
            return new Object[] {fo, offset};
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }

                    
    private static boolean doOpen(FileObject fo, int offset) {
        try {
            DataObject od = DataObject.find(fo);
            EditorCookie ec = od.getCookie(org.openide.cookies.EditorCookie.class);
            LineCookie lc = od.getCookie(org.openide.cookies.LineCookie.class);
            
            if (ec != null && lc != null && offset != -1) {                
                StyledDocument doc = ec.openDocument();                
                if (doc != null) {
                    int line = NbDocument.findLineNumber(doc, offset);
                    int lineOffset = NbDocument.findLineOffset(doc, line);
                    int column = offset - lineOffset;
                    
                    if (line != -1) {
                        Line l = lc.getLineSet().getCurrent(line);
                        
                        if (l != null) {
                            l.show(Line.SHOW_GOTO, column);
                            ec.getOpenedPanes()[0].requestFocusInWindow();
                            return true;
                        }
                    }
                }
            }
            
            OpenCookie oc = od.getCookie(org.openide.cookies.OpenCookie.class);
            
            if (oc != null) {
                oc.open();                
                return true;
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        
        return false;
    }
    
    private static int getOffset(FileObject fo, final ElementHandle<? extends Element> handle) throws IOException {
        final int[]  result = new int[] {-1};
        
        
        JavaSource js = JavaSource.forFileObject(fo);
        js.runUserActionTask(new CancellableTask<CompilationController>() {
            
            public void cancel() {
            }
            
            public void run(CompilationController info) {
                try {
                    info.toPhase(JavaSource.Phase.RESOLVED);
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
                Element el = handle.resolve(info);                
                if (el == null)
                    throw new IllegalArgumentException();
                
                FindDeclarationVisitor v = new FindDeclarationVisitor(el, info);
                
                CompilationUnitTree cu = info.getCompilationUnit();

                v.scan(cu, null);                
                Tree elTree = v.declTree;
                
                if (elTree != null)
                    result[0] = (int)info.getTrees().getSourcePositions().getStartPosition(cu, elTree);
            }
        },true);
        return result[0];
    }
    
    // Private innerclasses ----------------------------------------------------
    
    private static class FindDeclarationVisitor extends TreePathScanner<Void, Void> {
        
        private Element element;
        private Tree declTree;
        private CompilationInfo info;
        
        public FindDeclarationVisitor(Element element, CompilationInfo info) {
            this.element = element;
            this.info = info;
        }
        
	@Override
        public Void visitClass(ClassTree tree, Void d) {
            handleDeclaration();
            super.visitClass(tree, d);
            return null;
        }
        
	@Override
        public Void visitMethod(MethodTree tree, Void d) {
            handleDeclaration();
            super.visitMethod(tree, d);
            return null;
        }
        
	@Override
        public Void visitVariable(VariableTree tree, Void d) {
            handleDeclaration();
            super.visitVariable(tree, d);
            return null;
        }
    
        public void handleDeclaration() {
            Element found = info.getTrees().getElement(getCurrentPath());
            
            if ( element.equals( found ) ) {
                declTree = getCurrentPath().getLeaf();
            }
        }
    
    }
    
}
