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

package org.netbeans.api.java.source.ui;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.ui.ElementHeaderFormater;

/**
 *
 * @author phrebejk
 */
public final class ElementHeaders {

    private ElementHeaders() {
    }
    
    public static final String ANNOTATIONS = "%annotations%"; //NOI18N
    public static final String NAME = "%name%"; //NOI18N
    public static final String TYPE = "%type%"; //NOI18N
    public static final String THROWS = "%throws%"; //NOI18N
    public static final String IMPLEMENTS = "%implements%"; //NOI18N
    public static final String EXTENDS = "%extends%"; //NOI18N
    public static final String TYPEPARAMETERS = "%typeparameters%"; //NOI18N
    public static final String FLAGS = "%flags%"; //NOI18N
    public static final String PARAMETERS = "%parameters%"; //NOI18N
    
    
    /** Formats header of a tree. The tree must represent an element e.g. type
     * method, field, ...
     * <BR>
     * example of formatString:
     * <CODE>"method " + NAME + PARAMETERS + " has return type " + TYPE</CODE>
     * @param treePath TreePath to the tree header is required for
     * @param info CompilationInfo
     * @param formatString Formating string
     * @return Formated header of the tree
     */
    public static String getHeader(TreePath treePath, CompilationInfo info, String formatString) {
        assert info != null;
        assert treePath != null;
        Element element = info.getTrees().getElement(treePath);
        if (element!=null)
            return getHeader(element, info, formatString);
        return null;
    }

    /** Formats header of an element.
     * <BR>
     * example of formatString:
     * <CODE>"method " + NAME + PARAMETERS + " has return type " + TYPE</CODE>
     * @param element Element to be formated
     * @param info Compilation info
     * @param formatString Formating string
     * @return Formated header of the element
     */
    public static String getHeader(Element element, CompilationInfo info, String formatString) {
        assert element != null;
        assert info != null;
        assert formatString != null;
        TreePath tp = info.getTrees().getPath(element);
        Tree tree = tp.getLeaf();
        if (tree != null) {
            if (tree.getKind() == Tree.Kind.METHOD) {
                while (tp != null && tp.getLeaf().getKind() != Tree.Kind.CLASS) {
                    tp = tp.getParentPath();
                }
                Name enclosingClassName = tp != null ? ((ClassTree) tp.getLeaf()).getSimpleName() : null;
                return ElementHeaderFormater.getMethodHeader((MethodTree) tree, enclosingClassName, info, formatString);
            } else if (tree.getKind() == Tree.Kind.CLASS) {
                return ElementHeaderFormater.getClassHeader((ClassTree)tree, info, formatString);
            } else if (tree.getKind() == Tree.Kind.VARIABLE) {
                return ElementHeaderFormater.getVariableHeader((VariableTree)tree, info, formatString);
            }
        }
        return formatString.replaceAll(NAME, element.getSimpleName().toString()).replaceAll("%[a-z]*%", ""); //NOI18N
    }
    
     /** Computes distance between strings
      * @param s First string
      * @param t Second string
      * @return Distance between the strings. (Number of changes which have to 
      *         be done to get from <CODE>s</CODE> to <CODE>t</CODE>.
      */
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
  

    // Private methods ---------------------------------------------------------
    
    
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
    
    

}
