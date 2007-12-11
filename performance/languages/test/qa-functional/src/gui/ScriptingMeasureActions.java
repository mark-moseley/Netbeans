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

package gui;

import gui.action.CreateRubyProject;
import gui.action.EditorMenuPopup;
import gui.action.OpenRubyProject;
import gui.action.ScriptingExpandFolder;
import gui.action.ScriptingNodePopup;
import gui.action.ScriptingProjectNodePopup;
import gui.action.TypingInScriptingEditor;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class ScriptingMeasureActions {
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        /*
        suite.addTest(new CreateRubyProject("testCreateRubyProject","Create Ruby project"));
        suite.addTest(new CreateRubyProject("testCreateRubyOnRailsProject","Create Ruby on Rails project"));
        
        suite.addTest(new OpenRubyProject("testOpenRubyProject","Open Ruby Project"));
        suite.addTest(new OpenRubyProject("testOpenRailsProject","Open Ruby on Rails Project"));

       
        
        suite.addTest(new ScriptingProjectNodePopup("testRubyProjectNodePopupMenu","Ruby Project node popup test"));
        suite.addTest(new ScriptingProjectNodePopup("testRubyProjectNodePopupMenu","Rails Project node popup test"));
        
        suite.addTest(new ScriptingExpandFolder("testExpandRubyProjectNode","testExpandRubyProjectNode"));
        suite.addTest(new ScriptingExpandFolder("testExpandFolderWith100RubyFiles","testExpandFolderWith100RubyFiles"));        
        suite.addTest(new ScriptingExpandFolder("testExpandRailsProjectNode(","testExpandRailsProjectNode("));
        suite.addTest(new ScriptingExpandFolder("testExpandFolderWith100RailsFiles","testExpandFolderWith100RailsFiles"));
        suite.addTest(new ScriptingExpandFolder("testExpandFolderWith100JSFiles","testExpandFolderWith100JSFiles"));
        suite.addTest(new ScriptingExpandFolder("testExpandFolderWith100CssFiles","testExpandFolderWith100CssFiles"));
        
        suite.addTest(new ScriptingNodePopup("test_RB_NodePopup"," Ruby file node popup test"));
        suite.addTest(new ScriptingNodePopup("test_RHTML_NodePopup","RHTML file node popup test"));
        suite.addTest(new ScriptingNodePopup("test_YML_NodePopup","YML file node popup test"));        
        suite.addTest(new ScriptingNodePopup("test_JS_NodePopup","Java Script file node popup test"));
        suite.addTest(new ScriptingNodePopup("test_JSON_NodePopup","JSON file node popup test"));
        suite.addTest(new ScriptingNodePopup("test_CSS_NodePopup","CSS file node popup test"));
        suite.addTest(new ScriptingNodePopup("test_BAT_NodePopup","Batch script file node popup test"));
        suite.addTest(new ScriptingNodePopup("test_DIFF_NodePopup","Diff file node popup test"));
        suite.addTest(new ScriptingNodePopup("test_MANIFEST_NodePopup","Manifest file node popup test"));
        suite.addTest(new ScriptingNodePopup("test_SH_NodePopup","Shell Script file node popup test"));
        
        suite.addTest(new EditorMenuPopup("test_RB_EditorPopup"," Ruby editor node popup test"));
        suite.addTest(new EditorMenuPopup("test_RHTML_EditorPopup","RHTML editor node popup test"));
        suite.addTest(new EditorMenuPopup("test_JS_EditorPopup","Java Script editor node popup test"));
        suite.addTest(new EditorMenuPopup("test_JSON_EditorPopup","JSON editor node popup test"));
        suite.addTest(new EditorMenuPopup("test_CSS_EditorPopup","CSS editor node popup test"));
        suite.addTest(new EditorMenuPopup("test_YML_EditorPopup","YML editor node popup test"));
        suite.addTest(new EditorMenuPopup("test_BAT_EditorPopup","BAT editor node popup test"));
        suite.addTest(new EditorMenuPopup("test_DIFF_EditorPopup"," editor node popup test"));
        suite.addTest(new EditorMenuPopup("test_MANIFEST_EditorPopup","MANIFEST editor node popup test"));
        suite.addTest(new EditorMenuPopup("test_SH_EditorPopup","Shell Script editor node popup test"));
        
        suite.addTest(new TypingInScriptingEditor("test_RB_EditorTyping","test_RB_EditorTyping"));
        suite.addTest(new TypingInScriptingEditor("test_RHTML_EditorTyping","test_RHTML_EditorTyping"));
        suite.addTest(new TypingInScriptingEditor("test_JScript_EditorTyping","test_JScript_EditorTyping"));
        
        */
        return suite;        
    }
}
