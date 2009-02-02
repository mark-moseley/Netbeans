/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.test.refactoring;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.modules.test.refactoring.actions.FindUsagesAction;
import org.netbeans.modules.test.refactoring.operators.FindUsagesClassOperator;
import org.netbeans.modules.test.refactoring.operators.RefactoringResultOperator;

/**
 *
 * @author Jiri Prox Jiri.Prox@Sun.COM
 */
public class FindUsagesTestCase extends RefactoringTestCase {

    public static final String projectName = "RefactoringTest";
    public static final int SEARCH_IN_COMMENTS = 1 << 0;
    public static final int NOT_SEARCH_IN_COMMENTS = 1 << 1;
    public static final int FIND_USAGES = 1 << 2;
    public static final int FIND_DIRECT_SUBTYPES = 1 << 3;
    public static final int FIND_ALL_SUBTYPES = 1 << 4;
    public static final int FIND_OVERRIDING = 1 << 5;
    public static final int SEARCH_FROM_BASECLASS = 1 << 6;
    public static final int SEARCH_IN_ALL_PROJ = 1 << 7;
    public static final int SEARCH_ACTUAL_PROJ = 1 << 8;
    public static final int FIND_USAGES_METHOD = 1 << 9;
    public static final int NOT_FIND_USAGES_METHOD = 1 << 10;
    public static final int NOT_SEARCH_FROM_BASECLASS = 1 << 11;
    
    
    private static boolean browseChild = true;

    public FindUsagesTestCase(String name) {
        super(name);
    }

    public String getProjectName() {
        return projectName;
    }
        
    protected void findUsages(String packName,String fileName, int row, int col, int modifiers) {
        openSourceFile(packName, fileName);
        EditorOperator editor = new EditorOperator(fileName);
        editor.setCaretPosition(row, col);
        new FindUsagesAction().perform(editor);
        new EventTool().waitNoEvent(1000);
        FindUsagesClassOperator findUsagesClassOperator = new FindUsagesClassOperator();
        if ((modifiers & SEARCH_IN_COMMENTS) != 0)
            findUsagesClassOperator.getSearchInComments().setSelected(true);
        if ((modifiers & NOT_SEARCH_IN_COMMENTS) != 0)
            findUsagesClassOperator.getSearchInComments().setSelected(false);
        if ((modifiers & FIND_USAGES) != 0)
            findUsagesClassOperator.getFindUsages().setSelected(true);
        if ((modifiers & FIND_USAGES_METHOD) != 0)
            findUsagesClassOperator.getFindMethodUsage().setSelected(true);
        if ((modifiers & NOT_FIND_USAGES_METHOD) != 0)
            findUsagesClassOperator.getFindMethodUsage().setSelected(false);
        if ((modifiers & FIND_ALL_SUBTYPES) != 0)
            findUsagesClassOperator.getFindAllSubtypes().setSelected(true);
        if ((modifiers & FIND_DIRECT_SUBTYPES) != 0)
            findUsagesClassOperator.getFindDirectSubtypes().setSelected(true);
        if ((modifiers & FIND_OVERRIDING) != 0)
            findUsagesClassOperator.getFindOverridding().setSelected(true);
        if ((modifiers & SEARCH_FROM_BASECLASS) != 0)
            findUsagesClassOperator.getFindFromBaseClass().setSelected(true);
        if ((modifiers & NOT_SEARCH_FROM_BASECLASS) != 0)
            findUsagesClassOperator.getFindFromBaseClass().setSelected(false);
        if ((modifiers & SEARCH_IN_ALL_PROJ) != 0)
            findUsagesClassOperator.setScope(null);
        if ((modifiers & SEARCH_ACTUAL_PROJ) != 0)
            findUsagesClassOperator.setScope(projectName);

        findUsagesClassOperator.getFind().pushNoBlock();
        new EventTool().waitNoEvent(2000);
        if (browseChild) {
            RefactoringResultOperator test = new RefactoringResultOperator();
            JTree tree = test.getPreviewTree();
            TreeModel model = tree.getModel();
            Object root = model.getRoot();
            browseChildren(model, root, 0);
            
           
        }
    }
    
    protected void refMap(Map<String, List<String>> map) {
        String[] keys = map.keySet().toArray(new String[]{""});
        Arrays.sort(keys);
        
        for (String key : keys) {
            ref("File: "+key+"\n");
            List<String> list = map.get(key);
            for (String row : list) {
                ref(row+"\n");
            }
        }                
    }

    /**
     * @param browseChild the browseChild to set
     */    
    public static void setBrowseChild(boolean browseChild) {        
        FindUsagesTestCase.browseChild = browseChild;
    }
    
}
