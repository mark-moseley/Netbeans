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

package org.netbeans.modules.project.ui;

import java.awt.Component;
import java.awt.Container;
import java.lang.ref.WeakReference;
import javax.swing.JEditorPane;
import org.netbeans.junit.NbTestCase;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Children;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class TemplatesPanelTest extends NbTestCase implements TemplatesPanelGUI.Builder {
    public TemplatesPanelTest(String testName) {
        super(testName);
    }

    private static Object editor;
    public void testTemplatesPanel() {
        TemplatesPanelGUI inst;
        inst = new TemplatesPanelGUI(this);
        
        inst.addNotify();
        editor = find(inst, JEditorPane.class, true);
        WeakReference<Object> ref = new WeakReference<Object>(inst);
        
        inst.removeNotify();
        
        inst = null;
        assertGC("Panel does not hold ref", ref);
    }
    
    private static Component find(Component c, Class<?> clazz, boolean fail) {
        if (clazz.isInstance(c)) {
            return c;
        }
        if (c instanceof Container) {
            Container cont = (Container)c;
            for (Component p : cont.getComponents()) {
                Component r = find(p, clazz, false);
                if (r != null) {
                    return r;
                }
            }
        }
        if (fail) {
            fail("Not found " + clazz + " in children of " + c);
        }
        return null;
    }
    
    public Children createCategoriesChildren(DataFolder folder) {
        return Children.LEAF;
    }

    public Children createTemplatesChildren(DataFolder folder) {
        return Children.LEAF;
    }

    public String getCategoriesName() {
        return "";
    }

    public String getTemplatesName() {
        return "";
    }

    public void fireChange() {
    }
}
