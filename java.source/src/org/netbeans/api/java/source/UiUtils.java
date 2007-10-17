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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.api.java.source;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.util.Context;
import java.io.IOException;
import java.util.Collection;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.swing.Icon;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.java.source.pretty.VeryPretty;
import org.netbeans.modules.java.ui.Icons;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;

/** This class contains various methods bound to visualization of Java model 
 * elements. It was formerly included under SourceUtils
 *
 * XXX - needs cleanup
 *
 * @author Jan Lahoda
 */
@Deprecated
public final class  UiUtils {    
    
    private UiUtils() {}
    
    /** Gets correct icon for given ElementKind.
     *@param modifiers Can be null for empty modifiers collection
     */
    @Deprecated
    public static Icon getElementIcon( ElementKind elementKind, Collection<Modifier> modifiers ) {
        return Icons.getElementIcon(elementKind, modifiers);
    }
    
    // XXX Remove
    @Deprecated
    public static Icon getDeclarationIcon(Element element) {
        return getElementIcon(element.getKind(), element.getModifiers());
    }
    
    
    /**
     * Opens given {@link Element}.
     * 
     * @param cpInfo fileobject whose {@link ClasspathInfo} will be used
     * @param el    declaration to open
     * @return true if and only if the declaration was correctly opened,
     *                false otherwise
     */
    @Deprecated
    public static boolean open(final ClasspathInfo cpInfo, final Element el) {
	Object[] openInfo = getOpenInfo (cpInfo, el);
	if (openInfo != null) {
	    assert openInfo[0] instanceof FileObject;
	    assert openInfo[1] instanceof Integer;
	    return doOpen((FileObject)openInfo[0],(Integer)openInfo[1]);
	}
	return false;
    }
    
    @Deprecated
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
    
    private static String getMethodHeader(MethodTree tree, CompilationInfo info, String s) {
        Context context = info.getJavacTask().getContext();
        VeryPretty veryPretty = new VeryPretty(info);
        return veryPretty.getMethodHeader(tree, s);
    }

    private static String getClassHeader(ClassTree tree, CompilationInfo info, String s) {
        Context context = info.getJavacTask().getContext();
        VeryPretty veryPretty = new VeryPretty(info);
        return veryPretty.getClassHeader(tree, s);
    }
    private static String getVariableHeader(VariableTree tree, CompilationInfo info, String s) {
        Context context = info.getJavacTask().getContext();
        VeryPretty veryPretty = new VeryPretty(info);
        return veryPretty.getVariableHeader(tree, s);
    }
    
    @Deprecated
    public static final class PrintPart {
        private PrintPart() {}
        public static final String ANNOTATIONS = "%annotations"; //NOI18N
        public static final String NAME = "%name%"; //NOI18N
        public static final String TYPE = "%type%"; //NOI18N
        public static final String THROWS = "%throws%"; //NOI18N
        public static final String IMPLEMENTS = "%implements%"; //NOI18N
        public static final String EXTENDS = "%extends%"; //NOI18N
        public static final String TYPEPARAMETERS = "%typeparameters%"; //NOI18N
        public static final String FLAGS = "%flags%"; //NOI18N
        public static final String PARAMETERS = "%parameters%"; //NOI18N
    }
    
    /**
     * example of formatString:
     * "method " + PrintPart.NAME + PrintPart.PARAMETERS + " has return type " + PrintPart.TYPE
     */
    @Deprecated
    public static String getHeader(TreePath treePath, CompilationInfo info, String formatString) {
        assert info != null;
        assert treePath != null;
        Element element = info.getTrees().getElement(treePath);
        if (element!=null)
            return getHeader(element, info, formatString);
        return null;
    }

    /**
     * example of formatString:
     * "method " + PrintPart.NAME + PrintPart.PARAMETERS + " has return type " + PrintPart.TYPE
     */
    @Deprecated
    public static String getHeader(Element element, CompilationInfo info, String formatString) {
        assert element != null;
        assert info != null;
        assert formatString != null;
        Tree tree = info.getTrees().getTree(element);
        if (tree != null) {
            if (tree.getKind() == Tree.Kind.METHOD) {
                return getMethodHeader((MethodTree) tree, info, formatString);
            } else if (tree.getKind() == Tree.Kind.CLASS) {
                return getClassHeader((ClassTree)tree, info, formatString);
            } else if (tree.getKind() == Tree.Kind.VARIABLE) {
                return getVariableHeader((VariableTree)tree, info, formatString);
            }
        }
        return formatString.replaceAll(PrintPart.NAME, element.getSimpleName().toString()).replaceAll("%[a-z]*%", ""); //NOI18N
    }
    
    /**
     * Opens given {@link Element}.
     * 
     * @param fo fileobject whose {@link ClasspathInfo} will be used
     * @param offset  offset with fileobject
     * @return true if and only if the declaration was correctly opened,
     *                false otherwise
     */
    public @Deprecated static boolean open(final FileObject fo, final int offset) {
        return doOpen(fo, offset);
    }
    
    static Object[] getOpenInfo (final ClasspathInfo cpInfo, final Element el) {
        FileObject fo = SourceUtils.getFile(el, cpInfo);
        if (fo != null) {
            return getOpenInfo(fo, ElementHandle.create(el));
        } else {
            return null;
        }
    }
    
    static Object[] getOpenInfo(final FileObject fo, final ElementHandle<? extends Element> handle) {
        assert fo != null;
        
        try {
            int offset = getOffset(fo, handle);
            return new Object[] {fo, offset};
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
    }
    
    /** Computes dostance between strings
     */
    @Deprecated
    public static int getDistance(String s, String t) {
        int d[][]; // matrix
        int n; // length of s
        int m; // length of t
        int i; // iterates through s
        int j; // iterates through t
        char s_i; // ith character of s
        char t_j; // jth character of t
        int cost; // cost

        // Step 1

        n = s.length ();
        m = t.length ();
        if (n == 0) {
          return m;
        }
        if (m == 0) {
          return n;
        }
        d = new int[n+1][m+1];

        // Step 2

        for (i = 0; i <= n; i++) {
          d[i][0] = i;
        }

        for (j = 0; j <= m; j++) {
          d[0][j] = j;
        }

        // Step 3

        for (i = 1; i <= n; i++) {

          s_i = s.charAt (i - 1);

          // Step 4

          for (j = 1; j <= m; j++) {

            t_j = t.charAt (j - 1);

            // Step 5

            if (s_i == t_j) {
              cost = 0;
            }
            else {
              cost = 1;
            }

            // Step 6
            d[i][j] = min (d[i-1][j]+1, d[i][j-1]+1, d[i-1][j-1] + cost);

          }

        }

        // Step 7

        return d[n][m];        
    }
  
    private static int min (int a, int b, int c) {
        int mi;
               
        mi = a;
        if (b < mi) {
          mi = b;
        }
        if (c < mi) {
          mi = c;
        }
        return mi;

   }
    
    // Private methods ---------------------------------------------------------
                    
    private static boolean doOpen(FileObject fo, int offset) {
        try {
            DataObject od = DataObject.find(fo);
            EditorCookie ec = (EditorCookie) od.getCookie(EditorCookie.class);
            LineCookie lc = (LineCookie) od.getCookie(LineCookie.class);
            
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
                            return true;
                        }
                    }
                }
            }
            
            OpenCookie oc = (OpenCookie) od.getCookie(OpenCookie.class);
            
            if (oc != null) {
                oc.open();                
                return true;
            }
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        
        return false;
    }
    
    private static int getOffset(FileObject fo, final ElementHandle<? extends Element> handle) throws IOException {
        assert handle != null;
        final int[]  result = new int[] {-1};
        
        
        JavaSource js = JavaSource.forFileObject(fo);
        js.runUserActionTask(new Task<CompilationController>() {
                        
            public void run(CompilationController info) {
                try {
                    info.toPhase(JavaSource.Phase.RESOLVED);
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
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
    
        //JL: will anybody need this?:
//    public static Action createOpenAction(FileObject context, Declaration el) {
//        return new OpenAction(context, el);
//    }
//    
//    private static final class OpenAction extends AbstractAction {
//        
//        private FileObject context;
//        private Declaration el;
//        
//        public OpenAction(FileObject context, Declaration el) {
//            this.context = context;
//            this.el = el;
//            
//            putValue(NAME, getDisplayName(el));
//        }
//        
//        public void actionPerformed(ActionEvent e) {
//            open(context, el);
//        }
//        
//    }

    
    
}
