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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.jellytools.modules.debugger.actions;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.nodes.Node;

/** Used to call "Run|Run File|Debug "MyClass.java"" main menu item,
 * "Debug File" popup menu item or CTRL+Shift+F5 shortcut.
 * @see org.netbeans.jellytools.actions.Action
 * @author Jiri.Skrivanek@sun.com
 */
public class DebugJavaFileAction extends Action {

    // "Run"
    private static final String runItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject");
    // "Run File"
    //private static final String runFileItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject/RunOther");
    // "Debug File"
    private static final String POPUP_PATH = 
            Bundle.getStringTrimmed("org.netbeans.modules.java.project.Bundle", "LBL_DebugFile_Action");
    private static final KeyStroke keystroke = System.getProperty("os.name").toLowerCase().indexOf("mac") > -1 ?
            KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.META_MASK|KeyEvent.SHIFT_MASK) :
            KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.CTRL_MASK|KeyEvent.SHIFT_MASK);
    
    /** Creates new DebugAction instance. */
    public DebugJavaFileAction() {
        super(null, POPUP_PATH, keystroke);
    }
    
    /** Performs action through main menu. 
     * @param node node to be selected before action
     */
    @Override
    public void performMenu(Node node) {
        this.menuPath = runItem+"|"+
                Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle",
                                        "LBL_DebugSingleAction_Name",
                                        new Object[] {new Integer(1), node.getText()});
        super.performMenu(node);
    }

    /** Performs action through popup menu. 
     * @param node node to be selected before action
     */
    @Override
    public void performPopup(Node node) {
        this.popupPath =
                Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle",
                                        "LBL_DebugSingleAction_Name",
                                        new Object[] {new Integer(1), node.getText()});
        super.performPopup(node);
    }
}
