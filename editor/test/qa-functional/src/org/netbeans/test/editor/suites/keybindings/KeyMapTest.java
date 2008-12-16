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
package org.netbeans.test.editor.suites.keybindings;

import java.awt.event.KeyEvent;
import java.util.Vector;
import junit.framework.Test;
import org.netbeans.test.editor.lib.EditorTestCase.ValueResolver;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.HelpOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.editor.KeyMapOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author Petr Dvorak Petr.Dvorak@Sun.COM
 */
public class KeyMapTest extends JellyTestCase {
    public static final String PROFILE_DEFAULT = "NetBeans";

    public static final String SRC_PACKAGES_PATH = Bundle.getString("org.netbeans.modules.java.j2seproject.Bundle", "NAME_src.dir");
    private static String PROJECT_NAME;
    private static EditorOperator editor;

    /** Creates a new instance of KeyMapTest
     * @param name Test name
     */
    public KeyMapTest(String name) {
        super(name);
    }

    public void prepareFileInEditor() {
        PROJECT_NAME = "keymapTestProject";
        NewProjectWizardOperator newProjectOper = NewProjectWizardOperator.invoke();
        newProjectOper.selectCategory("Java");
        newProjectOper.selectProject("Java Application");
        newProjectOper.next();
        NewProjectNameLocationStepOperator npnlso = new NewProjectNameLocationStepOperator();
        new JTextFieldOperator(npnlso, 0).setText(PROJECT_NAME); // NOI18N
        new JTextFieldOperator(npnlso, 1).setText(getDataDir().getAbsolutePath()); // NOI18N
        newProjectOper.finish();
        newProjectOper.waitClosed();
        ProjectSupport.waitScanFinished();

        new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME).select();
        editor = new EditorOperator("Main.java");
    }

    public void closeProject() {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        rootNode.performPopupActionNoBlock("Delete");
        NbDialogOperator ndo = new NbDialogOperator("Delete");
        JCheckBoxOperator cb = new JCheckBoxOperator(ndo, "Also");
        cb.setSelected(true);
        ndo.yes();
        ndo.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        ndo.waitClosed();
    }

    @Override
    protected void setUp() throws Exception {
        System.out.println("----");
        System.out.println("Starting: " + getName());
        System.out.println("----");
    }

    @Override
    protected void tearDown() throws Exception {
        System.out.println("----");
        System.out.println("Finished: " + getName());
        System.out.println("----");
    }

// TODO: Verify if all shortcuts are contained in profile...
/*
    public void testAllKeyMapNetbeans() throws IOException {
    }
    public void testAllKeyMapNetbeans55() throws IOException {
    }
    public void testAllKeyMapEmacs() throws IOException {
    }
    public void testAllKeyMapEclipse() throws IOException {
    }
     */
    public void testVerify() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.verify();
            kmo.ok().push();
            closed = true;
        } catch (Exception e) {
            System.out.println("ERROR: testVerify");
            e.printStackTrace();
            fail();
        } finally {
            if (!closed && kmo != null) {
                kmo.cancel().push();
                editor.close(false);
            }
        }
    }

    public void testAddShortcut() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.selectProfile(PROFILE_DEFAULT);
            kmo.assignShortcutToAction("select line", true, true, false, KeyEvent.VK_G);
            Vector<String> shortcuts = kmo.getAllShortcutsForAction("select line");
            checkListContents(shortcuts, "Ctrl+Shift+G");
            kmo.ok().push();
            closed = true;
            new EventTool().waitNoEvent(2000);
            editor.requestFocus();
            new EventTool().waitNoEvent(100);
            editor.setCaretPosition(12, 1);
            ValueResolver vr = new ValueResolver() {

                public Object getValue() {
                    editor.pushKey(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
                    String selected = editor.txtEditorPane().getSelectedText();
                    new EventTool().waitNoEvent(100);
                    if (selected == null) {
                        return false;
                    }
                    return selected.startsWith("public class Main {");
                }
            };
            waitMaxMilisForValue(3000, vr, Boolean.TRUE);
            String text = editor.txtEditorPane().getSelectedText();
            assertEquals("public class Main {", text.trim());
        } catch (Exception e) {
            System.out.println("ERROR: testAddShortcut");
            e.printStackTrace();
            fail();
        } finally {
            if (!closed && kmo != null) {
                kmo.cancel().push();
                editor.close(false);
            }
        }
    }

    public void testAssignAlternativeShortcut() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            // invoke keymap operator and mark it is open
            kmo = KeyMapOperator.invoke();
            closed = false;
            // select netbeans  profile
            kmo.selectProfile(PROFILE_DEFAULT);
            // assign one normal and one alternative shortcut to the "select line" action
            kmo.assignShortcutToAction("select line", true, true, false, KeyEvent.VK_G);
            kmo.assignAlternativeShortcutToAction("select line", true, false, true, KeyEvent.VK_M);
            // retrieve all assigned shortcuts and compare it to expected list of shortcuts
            Vector<String> shortcuts = kmo.getAllShortcutsForAction("select line");
            checkListContents(shortcuts, "ctrl+shift+g", "ctrl+alt+m");
            // confirm Options dialog, press OK and mark that OD was closed
            kmo.ok().push();
            closed = true;
            // Wait + focus the editor
            new EventTool().waitNoEvent(2000);
            editor.requestFocus();
            new EventTool().waitNoEvent(100);
            // Check Ctrl+Alt+M works for select line
            editor.setCaretPosition(12, 1);
            ValueResolver vr = new ValueResolver() {

                public Object getValue() {
                    editor.pushKey(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK);
                    String selected = editor.txtEditorPane().getSelectedText();
                    new EventTool().waitNoEvent(100);
                    if (selected == null) {
                        return false;
                    }
                    return selected.startsWith("public class Main {");
                }
            };
            waitMaxMilisForValue(3000, vr, Boolean.TRUE);
            String text = editor.txtEditorPane().getSelectedText();
            assertEquals("public class Main {", text.trim());
            // Check Ctrl+Shift+G works for select line
            editor.setCaretPosition(12, 1);
            vr = new ValueResolver() {

                public Object getValue() {
                    editor.pushKey(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
                    String selected = editor.txtEditorPane().getSelectedText();
                    new EventTool().waitNoEvent(100);
                    if (selected == null) {
                        return false;
                    }
                    return selected.startsWith("public class Main {");
                }
            };
            waitMaxMilisForValue(3000, vr, Boolean.TRUE);
            text = editor.txtEditorPane().getSelectedText();
            assertEquals("public class Main {", text.trim());
        } catch (Exception e) {
            System.out.println("ERROR: testAssignAlternativeShortcut");
            e.printStackTrace();
            fail();
        } finally {
            if (!closed && kmo != null) {
                kmo.cancel().push();
                editor.close(false);
            }
        }
    }

    public void testUnassign() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            for (int i = 0; i < 2; i++) {
                // invoke keymap operator and mark it is open
                kmo = KeyMapOperator.invoke();
                closed = false;
                // select netbeans  profile
                kmo.selectProfile(PROFILE_DEFAULT);
                // assign one normal shortcut to the "select line" action
                kmo.assignShortcutToAction("select line", true, true, false, KeyEvent.VK_G);
                // retrieve all assigned shortcuts and compare it to expected list of shortcuts
                Vector<String> shortcuts = kmo.getAllShortcutsForAction("select line");
                checkListContents(shortcuts, "ctrl+shift+g");
                kmo.ok().push();
                closed = true;
                new EventTool().waitNoEvent(2000);
                editor.requestFocus();
                new EventTool().waitNoEvent(100);
                // Check Ctrl+Shift+G works for select line
                editor.setCaretPosition(12, 1);
                ValueResolver vr = new ValueResolver() {

                    public Object getValue() {
                        editor.pushKey(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
                        String selected = editor.txtEditorPane().getSelectedText();
                        new EventTool().waitNoEvent(100);
                        if (selected == null) {
                            return false;
                        }
                        return selected.startsWith("public class Main {");
                    }
                };
                waitMaxMilisForValue(3000, vr, Boolean.TRUE);
                String text = editor.txtEditorPane().getSelectedText();
                assertEquals("public class Main {", text.trim());
                kmo = KeyMapOperator.invoke();
                closed = false;
                kmo.unassignAlternativeShortcutToAction("select line", "ctrl+shift+g");
                kmo.ok().push();
                closed = true;
                new EventTool().waitNoEvent(2000);
                editor.requestFocus();
                new EventTool().waitNoEvent(100);
                // Check Ctrl+Alt+M works for select line
                editor.setCaretPosition(12, 2);
                sleep(200);
                editor.setCaretPosition(12, 1);
                waitMaxMilisForValue(3000, vr, Boolean.TRUE);
                text = editor.txtEditorPane().getSelectedText();
                if (text == null) {
                    text = "";
                }
                assertNotSame("public class Main {", text.trim());
            }
        } catch (Exception e) {
            System.out.println("ERROR: testUnassign");
            e.printStackTrace();
            fail();
        } finally {
            if (!closed && kmo != null) {
                kmo.cancel().push();
                editor.close(false);
            }
        }
    }

    public void testAddDuplicateCancel() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.selectProfile(PROFILE_DEFAULT);
            Vector<String> shortcuts = kmo.getAllShortcutsForAction("select line");
            kmo.assignShortcutToAction("select line", true, true, false, KeyEvent.VK_F9, true, false);
            shortcuts.equals(kmo.getAllShortcutsForAction("select line"));
            kmo.ok().push();
            closed = true;
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.selectProfile(PROFILE_DEFAULT);
            kmo.assignShortcutToAction("select line", true, true, false, KeyEvent.VK_F9, true, true);
            kmo.ok().push();
            closed = true;
            new EventTool().waitNoEvent(2000);
            editor.requestFocus();
            new EventTool().waitNoEvent(100);
            // Check Ctrl+Shift+G works for select line
            editor.setCaretPosition(12, 1);
            ValueResolver vr = new ValueResolver() {

                public Object getValue() {
                    editor.pushKey(KeyEvent.VK_F9, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
                    String selected = editor.txtEditorPane().getSelectedText();
                    new EventTool().waitNoEvent(100);
                    if (selected == null) {
                        return false;
                    }
                    return selected.startsWith("public class Main {");
                }
            };
            waitMaxMilisForValue(3000, vr, Boolean.TRUE);
            String text = editor.txtEditorPane().getSelectedText();
            assertEquals("public class Main {", text.trim());
        } catch (Exception e) {
            System.out.println("ERROR: testAddDuplicateCancel");
            e.printStackTrace();
            fail();
        } finally {
            if (!closed && kmo != null) {
                kmo.cancel().push();
                editor.close(false);
            }
        }
    }

    public void testCancelAdding() {
    }

    public void testCancelOptions() {
    }

    public void testHelp() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.help().push();
            final HelpOperator help = new HelpOperator();
            ValueResolver vr = new ValueResolver() {

                public Object getValue() {
                    return help.getContentText().contains("Options Window: Keymap");
                }
            };
            waitMaxMilisForValue(5000, vr, Boolean.TRUE);
            boolean ok = help.getContentText().contains("Options Window: Keymap");
            if (!ok) {
                log(help.getContentText());
            }
            assertTrue("Wrong help page opened", ok);
            help.close();
        } finally {
            if (!closed && kmo != null) {
                kmo.cancel().push();
            }
        }
    }

    public void testProfileSwitch() {
    }

    public void testProfileDuplicte() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        String prFrom = PROFILE_DEFAULT;
        String prTo = "NetBeans New";
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.selectProfile(PROFILE_DEFAULT);
            kmo.assignShortcutToAction("select line", true, false, true, KeyEvent.VK_M);
            kmo.duplicateProfile(prFrom, "NetBeans New");
            kmo.selectProfile("NetBeans New");
            if (!kmo.getAllShortcutsForAction("select line").contains("ctrl+alt+m")) {
                fail("Profile cloning failed: " + prFrom + " -> " + prTo);
            }
            kmo.checkProfilesPresent("Eclipse", "Emacs", "NetBeans", "NetBeans New", "NetBeans 5.5");
            kmo.selectProfile(PROFILE_DEFAULT);
            kmo.ok().push();
            closed = true;
        } finally {
            if (!closed && kmo != null) {
                kmo.cancel().push();
            }
        }
    }

    public void testProfileRestore() {
        KeyMapOperator kmo = null;
        boolean closed = true;
        try {
            kmo = KeyMapOperator.invoke();
            closed = false;
            Vector<String> shortcuts = kmo.getAllShortcutsForAction("Preview Design");
            kmo.assignShortcutToAction("Preview Design", true, true, false, KeyEvent.VK_W, true, true);
            if (shortcuts.equals(kmo.getAllShortcutsForAction("Preview Design"))) {
                fail("Problem with assigning shortcut to Preview Design");
            }
            kmo.ok().push();
            closed = true;
            kmo = KeyMapOperator.invoke();
            closed = false;
            kmo.actionSearchByName().setText("Preview Design");
            kmo.restoreProfile("NetBeans");
            Vector<String> sc = kmo.getAllShortcutsForAction("Preview Design");
            if (!shortcuts.equals(sc)) {
                // This test currently fails: http://www.netbeans.org/issues/show_bug.cgi?id=151254
                fail("Problem with restoring NetBeans profile (http://www.netbeans.org/issues/show_bug.cgi?id=151254) - \"Preview Design\" action: " + shortcuts.toString() + " vs. " + sc.toString());
            }
            kmo.ok().push();
            closed = true;
        } finally {
            if (!closed && kmo != null) {
                kmo.cancel().push();
            }
        }
    }

    protected boolean waitMaxMilisForValue(int maxMiliSeconds, ValueResolver resolver, Object requiredValue) {
        int time = maxMiliSeconds / 100;
        while (time > 0) {
            Object resolvedValue = resolver.getValue();
            if (requiredValue == null && resolvedValue == null) {
                return true;
            }
            if (requiredValue != null && requiredValue.equals(resolvedValue)) {
                return true;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                time = 0;
            }
            time--;
        }
        return false;
    }

    protected void sleep(int miliseconds) {
        try {
            Thread.sleep(miliseconds);
        } catch (Throwable t) {
            // Thread.sleep() failed for some reason
        }
    }

    private void checkListContents(Vector<String> scList, String... expList) {
        assertEquals("List does not contains expected number of items", expList.length, scList.size());
        for (int i = 0; i < scList.size(); i++) {
            scList.set(i, scList.get(i).toLowerCase());
        }
        for (String string : expList) {
            assertTrue(scList.contains(string.toLowerCase()));
        }
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(KeyMapTest.class)
                    .addTest("prepareFileInEditor")
                    .addTest("testVerify")
                    .addTest("testAddDuplicateCancel")
                    .addTest("testAddShortcut")
                    .addTest("testUnassign")
                    .addTest("testAssignAlternativeShortcut")
                    //.addTest("testProfileRestore")//fails due to issue 151254
                    .addTest("testProfileDuplicte")
                    .addTest("testHelp")
                    .addTest("closeProject")
                .enableModules(".*").clusters(".*"));
    }
}
