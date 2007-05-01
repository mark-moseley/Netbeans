/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package  org.netbeans.modules.cnd.editor.parser;

import org.openide.cookies.EditorCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.text.Line;
import org.openide.cookies.LineCookie;
import org.openide.loaders.DataObject;
import org.openide.ErrorManager;

import javax.swing.JEditorPane;

import org.openide.text.NbDocument;

public abstract class ViewNode extends AbstractNode {
    private Line line = null;
    private DataObject dao = null;
    private int lineno = 0;
    private char kind;
    private String scope;
    private int scopeCluster;
    private int cluster;
    private String sortName;
    private int scopeLevels = 0;
    
    public ViewNode(String name, DataObject dao, int lineno, char kind, String scope, int scopeCluster, int cluster) {
        super(Children.LEAF);
        this.dao = dao;
        this.lineno = lineno;
        this.kind = kind;
        this.scope = scope;
        this.scopeCluster = scopeCluster;
        this.cluster = cluster;
	setName(name);
	if (scope != null) {
	    sortName = createSortScope(scope) + "::" + cluster + name; // NOI18N
	    setDisplayName(name);
	}
	else {
	    sortName = cluster + name;
	    setDisplayName(name);
        }
    }

    /**
     * Creates a scope thet can be sorted alphabetically using the following form:
     * C++ Class scope:
     *   "ccc1::ccc11::ccc111" returns "7ccc1::7ccc11::7ccc111" where 7 is the 'class' cluster id
     * and 
     * C++ Namespace scope:
     *    "nnn1::nnn12" returns "8nnn1::8nnn12" where 8 is the 'namespace' cluster id.
     * The scop will later be combined with the name and the name's cluster id in the following
     * fashion:
     *    7ccc1::7ccc11::7ccc111:5yyy
     *    7ccc1::7ccc11::7ccc111:6xxx
     *    8nnn1::8nnn12
     *    8nnn1::8nnn12::3zzz
     * When sorting the strings, 7 comes before 8 so classes come before namespaces, and 5 comes before 6 so
     * variables come before methods
     */
    private String createSortScope(String scope) {
	String sortScope = ""; // NOI18N
	if (scope == null) {
	    scopeLevels = 0;
	    sortScope = ""; // NOI18N
	}
	else {
	    scopeLevels = 1;
	    int startIndex = 0;
	    int index;
	    while ((index = scope.indexOf("::", startIndex)) >= 0) { // NOI18N
		sortScope = sortScope + scopeCluster + scope.substring(startIndex, index) + "::"; // NOI18N
		scopeLevels++;
		startIndex = index + 2;
	    }
	    sortScope = sortScope + scopeCluster + scope.substring(startIndex);
	}
	return sortScope;
    }

    public String getSortName() {
	return sortName;
    }

    public int getScopeLevel() {
        return scopeLevels;
    }

    public String getScope() {
	return scope;
    }

    public int getScopeCluster() {
	return scopeCluster;
    }

    public int getCluster() {
	return cluster;
    }
    
    public Line getLine() {
        if (line == null)
            line = lineNumberToLine();
	if (line == null) {
	    ErrorManager.getDefault().notify(new Exception("No Line info for line " + lineno + " in " + dao.getPrimaryFile().getNameExt())); // NOI18N
	}
        return line;
    }
    
    public int getLineNo() {
        return lineno;
    }
    
    public void goToLine() {
        getLine().show(Line.SHOW_GOTO);
    }
    
    private Line lineNumberToLine() {
        LineCookie lc = (LineCookie)dao.getCookie(LineCookie.class);
	Line l = null;
        if (lc != null) {
            Line.Set ls = lc.getLineSet();
            if (ls != null) {
                l = ls.getCurrent(lineno-1);
            }
        }
        return l;
    }

    public int getLineOffset() {
        EditorCookie editor = (EditorCookie) dao.getCookie(EditorCookie.class);
        assert editor != null;
	return NbDocument.findLineOffset(editor.getDocument(), lineno-1);
    }

    public void goToOffset(JEditorPane jEditorPane) {
	jEditorPane.getCaret().setDot(getLineOffset());
	jEditorPane.requestFocus();
    }

    public char getKind() {
	return kind;
    }

    public DataObject getDataObject() {
	return dao;
    }
}

