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

package org.netbeans.modules.cnd.editor;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.CodeFoldingSideBar;
import org.openide.ErrorManager;

/**
 *  C/C++ Code Folding Side Bar. Component responsible for drawing folding signs and
 *  responding  on user fold/unfold action.
 */
public class CppCodeFoldingSideBar extends CodeFoldingSideBar {

    private int startPos;
    private int endPos;
    private List elems = new ArrayList();
    private static final ErrorManager log = ErrorManager.getDefault().getInstance(
		"CppFoldTracer"); // NOI18N
    
    public CppCodeFoldingSideBar(){
	log.log("CppCodeFoldingSidebar<Init_1>: Creating sidebar component"); // NOI18N
    }
    
    /** Creates a new instance of NbCodeFoldingSideBar */
    public CppCodeFoldingSideBar(JTextComponent target) {
        super(target);
	log.log("CppCodeFoldingSidebar<Init_2>: Creating sidebar component"); // NOI18N
    }
    
    public JComponent createSideBar(JTextComponent target) {
	log.log("CppCodeFoldingSidebar.createSideBar: Sidebar factory"); // NOI18N
        return new CppCodeFoldingSideBar(target);
    }

}
