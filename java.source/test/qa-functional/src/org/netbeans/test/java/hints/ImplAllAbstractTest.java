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
package org.netbeans.test.java.hints;

import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;

/**
 *
 * @author Jiri Prox
 */
public class ImplAllAbstractTest extends HintsTestCase{

    public ImplAllAbstractTest(String name) {
        super(name);
    }
    
    public void testImplementAbstract() {
        String file = "AllAbs";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(4,1);
        new EventTool().waitNoEvent(750);
        String pattern = ".*public void run\\(\\) \\{.*throw new UnsupportedOperationException\\(\"Not supported yet.\"\\);.*\\}.*";
        useHint("Implement",new String[]{"Implement all abstract methods"},pattern);
    }
    
    public void testImplementAbstract2() {
        String file = "AllAbs2";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(6,1);
        new EventTool().waitNoEvent(750);
        String pattern = ".*public int getRowCount\\(\\) \\{.*" +
                "throw new UnsupportedOperationException\\(\"Not supported yet.\"\\);.*" +
                "\\}.*" +
                "public int getColumnCount\\(\\) \\{.*" +
                "throw new UnsupportedOperationException\\(\"Not supported yet.\"\\);.*" +
                "\\}.*"+
                "public Object getValueAt\\(int rowIndex, int columnIndex\\) \\{.*"+
                "throw new UnsupportedOperationException\\(\"Not supported yet.\"\\);.*" +
                "\\}.*";
        useHint("Implement",new String[]{"Implement all abstract methods"},pattern);
    }
    
    public void testImplementAbstract3() {
        String file = "AllAbs2";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(11,1);
        new EventTool().waitNoEvent(750);
        String pattern = ".*\\{.*public int compareTo\\(T o\\) \\{.*throw new UnsupportedOperationException\\(\"Not supported yet.\"\\);.*\\}.*\\}.*";
        useHint("Implement",new String[]{"Implement all abstract methods"},pattern);
    }
    
    public static void main(String[] args) {
        TestRunner.run(ImplAllAbstractTest.class);
    }
    
}
