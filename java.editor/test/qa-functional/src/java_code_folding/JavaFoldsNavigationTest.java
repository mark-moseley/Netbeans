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

package java_code_folding;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;

/**
 * Test behavior of navigation through java code folds.
 *
 * Test covers following actions:
 * caret-forward [RIGHT]
 * caret-backward [LEFT]
 * caret-down [DOWN]
 * caret-up [UP]
 * selection-forward [SHIFT-RIGHT]
 * selection-backward [SHIFT-LEFT]
 * selection-down [SHIFT-DOWN]
 * selection-up [SHIFT-UP]
 * caret-begin-line [HOME]
 * caret-end-line [END]
 * selection-begin-line [SHIFT-HOME]
 * selection-end-line [SHIFT-END]
 *
 * Actions:
 * caret-next-word [CTRL-RIGHT]
 * caret-previous-word [CTRL-LEFT]
 * selection-next-word [CTRL-SHIFT-RIGHT]
 * selection-previous-word [CTRL-SHIFT-LEFT]
 * should be added to testcase after issue #47454 will be fixed
 *
 * @author Martin Roskanin
 */
  public class JavaFoldsNavigationTest extends JavaCodeFoldingTest {

    private JEditorPaneOperator txtOper;
    private EditorOperator editor;
     
    /** Creates a new instance of Main */
    public JavaFoldsNavigationTest(String testMethodName) {
        super(testMethodName);
    }
    
    private ValueResolver getResolver(final JEditorPaneOperator txtOper, final int etalon){
        ValueResolver resolver = new ValueResolver(){
            public Object getValue(){
                int newCaretPos = txtOper.getCaretPosition();
                return (newCaretPos == etalon) ? Boolean.TRUE : Boolean.FALSE;
            }
        };
        
        return resolver;
    }
    
    private void checkActionByKeyStroke(int key, int mod, int caretPosToSet, int etalon, boolean checkSelection){
        if (caretPosToSet == -1){
            caretPosToSet = txtOper.getCaretPosition();
        }else{
            editor.setCaretPosition(caretPosToSet);
            txtOper.getCaret().setMagicCaretPosition(null);
        }
        txtOper.pushKey(key,mod);
        waitMaxMilisForValue(3500, getResolver(txtOper, etalon), Boolean.TRUE);
        int newCaretOffset = txtOper.getCaretPosition();
        if (checkSelection){
            int selectionStart = txtOper.getSelectionStart();
            int selectionEnd = txtOper.getSelectionEnd(); 
            if (selectionStart != Math.min(caretPosToSet, etalon) ||
                    selectionEnd != Math.max(caretPosToSet, etalon)){
                String keyString = KeyStroke.getKeyStroke(key, mod).toString();
                //fail(keyString+": Action failed: [etalon/newCaretOffset/selectionStart/selectionEnd]: ["+etalon+"/"+
                //        newCaretOffset+"/"+selectionStart+"/"+selectionEnd+"]");
                System.out.println(keyString+": Action failed: [etalon/newCaretOffset/selectionStart/selectionEnd]: ["+etalon+"/"+newCaretOffset+"/"+selectionStart+"/"+selectionEnd+"]");
            }
        }else{
            if (etalon != newCaretOffset){
                String keyString = KeyStroke.getKeyStroke(key, mod).toString();
                //fail(keyString+": Action failed: [etalon/newCaretOffset]: ["+etalon+"/"+
                //        newCaretOffset+"]");
                System.out.println(keyString+": Action failed: [etalon/newCaretOffset]: ["+etalon+"/"+ newCaretOffset+"]");
            }
        }
    }
    
    public void testJavaFoldsNavigation(){
        openDefaultProject();
        openDefaultSampleFile();
        try {            
            editor = getDefaultSampleEditorOperator();
            JTextComponentOperator txtCompOper = new JTextComponentOperator(editor);
            JTextComponent target = (JTextComponent)txtCompOper.getSource();
            txtOper = editor.txtEditorPane();

            // wait max. 6 second for code folding initialization
            waitForFolding(target, 6000);

            //01 collapse initial comment fold. [ */|]
            // check caret left action
            collapseFoldAtCaretPosition(editor, 4, 4); // 4,4 -caret offset 70
            
            // check left
            checkActionByKeyStroke(KeyEvent.VK_LEFT, 0, 70, 0, false);
            
            //check selectin
            checkActionByKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK, 70, 0, true);
            
            // check caret right action
            checkActionByKeyStroke(KeyEvent.VK_RIGHT, 0, 0, 70, false);
            
            // check caret right action, selection
            checkActionByKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK, 0, 70, true);
            
            // check home action
            checkActionByKeyStroke(KeyEvent.VK_HOME, 0, 70, 0, false);
            
            // check home action, selection
            checkActionByKeyStroke(KeyEvent.VK_HOME, KeyEvent.SHIFT_DOWN_MASK, 70, 0, true);

            // check end action
            checkActionByKeyStroke(KeyEvent.VK_END, 0, 0, 70, false);
            
            // check end action, selection
            checkActionByKeyStroke(KeyEvent.VK_END, KeyEvent.SHIFT_DOWN_MASK, 0, 70, true);
            
            // check up action
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 71, 0, false);
            
            // check up action, selection
            checkActionByKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK, 71, 0, true);
            
            // check down action
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, 0, 71, false);
            
            // check down action, selection
            checkActionByKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK, 0, 71, true);

            // checking end of fold
            // check up action
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 78, 70, false);
            
            // check up action, selection
            checkActionByKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK, 78, 70, true);
            
            // check down action
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, 70, 78, false);
            
            // check down action, selection
            checkActionByKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK, 70, 78, true);
            
            // check magic position
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 80, 70, false);
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, -1, 80, false);
            
            
            // ------------------------------------------------------------------------
            
            
            // check actions on one-line fold
            collapseFoldAtCaretPosition(editor, 25, 13); // 25,13 - caret offset 422

            
            // check left
            checkActionByKeyStroke(KeyEvent.VK_LEFT, 0, 454, 414, false);
            
            //check selectin
            checkActionByKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK, 454, 414, true);
            
            // check caret right action
            checkActionByKeyStroke(KeyEvent.VK_RIGHT, 0, 414, 454, false);
            
            // check caret right action, selection
            checkActionByKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK, 414, 454, true);
            
            // check home action
            checkActionByKeyStroke(KeyEvent.VK_HOME, 0, 454, 414, false);
            
            // check home action, selection
            checkActionByKeyStroke(KeyEvent.VK_HOME, KeyEvent.SHIFT_DOWN_MASK, 454, 414, true);

            // check end action
            checkActionByKeyStroke(KeyEvent.VK_END, 0, 414, 454, false);
            
            // check end action, selection
            checkActionByKeyStroke(KeyEvent.VK_END, KeyEvent.SHIFT_DOWN_MASK, 414, 454, true);
            
            // check up action
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 459, 414, false);
            
            // check up action, selection
            checkActionByKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK, 459, 414, true);
            
            // check down action
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, 414, 459, false);
            
            // check down action, selection
            checkActionByKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK, 414, 459, true);

            // checking end of fold
            // check up action
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 467, 454, false);
            
            // check up action, selection
            checkActionByKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK, 467, 454, true);
            
            // check down action
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, 454, 467, false);
            
            // check down action, selection
            checkActionByKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK, 454, 467, true);
            
            // check magic position
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 469, 454, false);
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, -1, 469, false);
            
            //----------------------------------------------------------------
            //check multi fold on line
                       
            collapseFoldAtCaretPosition(editor, 36, 86); // 36,84 -caret offset 920
                                   
            // check left
            checkActionByKeyStroke(KeyEvent.VK_LEFT, 0, 920, 917, false);
                       
            //check selectin
            checkActionByKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK, 920, 917, true);
            
            // check caret right action
            checkActionByKeyStroke(KeyEvent.VK_RIGHT, 0, 917, 920, false);
            
            // check caret right action, selection
            checkActionByKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK, 917, 920, true);
            
            // check home action
            checkActionByKeyStroke(KeyEvent.VK_HOME, 0, 920, 839, false);
            
            // check home action, selection
            checkActionByKeyStroke(KeyEvent.VK_HOME, KeyEvent.SHIFT_DOWN_MASK, 920, 839, true);

            // check end action
            checkActionByKeyStroke(KeyEvent.VK_END, 0, 917, 949, false);
            
            // check end action, selection
            checkActionByKeyStroke(KeyEvent.VK_END, KeyEvent.SHIFT_DOWN_MASK, 917, 949, true);
                                    
            // check up action
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 1032, 917, false);
            
            // check up action, selection
            checkActionByKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK, 1032, 917, true);
            
            // check down action
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, 917, 1032, false);
            
            // check down action, selection
            checkActionByKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK, 917, 1032, true);
             
            // checking end of fold
            // check up action
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 1035, 920, false);
            
            // check up action, selection
            checkActionByKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK, 1035, 920, true);
            
            // check down action
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, 920, 1037, false);
            
            // check down action, selection
            checkActionByKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK, 920, 1037, true);
            
            // check magic position
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 1033, 917, false);
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, -1, 1033, false);
            
            
        } finally{
            closeFileWithDiscard();    
        }
    }
    
    public static void main(String[] args) {
        TestRunner.run(JavaFoldsNavigationTest.class);
    }

    
}
