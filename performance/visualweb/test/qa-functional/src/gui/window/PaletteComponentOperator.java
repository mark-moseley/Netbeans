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

package gui.window;

import java.awt.Component;
import java.awt.Container;

import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.PaletteOperator;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class PaletteComponentOperator extends PaletteOperator {
    
    /** Creates a new instance of PaletteComponentOperator */
    public PaletteComponentOperator() {
        super();
    }
    
    /**
     * expands selected category node in palette
     * @param categoryName sets the name of category need to be expanded in palette
     * @throws Exception
    */
    public void expandCategory(String categoryName) throws Exception {
        JCheckBoxOperator cat = new JCheckBoxOperator(this,categoryName);
        cat.pushNoBlock();
    }
    
    public JListOperator getCategoryListOperator(String categoryName) {
        //Find Checkbox operator at first
        JCheckBoxOperator cbo =  new JCheckBoxOperator(this,categoryName);
        //Parent component for this checkbox
        Container cbp = cbo.getParent();
        //Find List in this container
        ContainerOperator cto = new ContainerOperator(cbp);
        Component expected = cto.findSubComponent(new CategoryListChooser());
        
        return new JListOperator((javax.swing.JList) expected);
    }
    public static PaletteComponentOperator invoke() {
        PaletteComponentOperator testOp = null;
        try {
            testOp = new PaletteComponentOperator();
        } catch (TimeoutExpiredException tex) {
            MainWindowOperator mv = MainWindowOperator.getDefault();
            JMenuBarOperator menuBar = mv.menuBar();
            JMenuItemOperator item = menuBar.showMenuItem("Window|Palette");
            item.clickMouse();
            testOp = new PaletteComponentOperator();
        }       
        return testOp;        
    }
    private static class CategoryListChooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return (comp.getClass().getName().equals("org.netbeans.modules.palette.ui.CategoryList"));
        }

        public String getDescription() {
            return "Category List Component";
        }
    }
    
}
