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


package org.netbeans.performance.visualweb.dialogs;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class ComponentStyleDialog extends JSFComponentOptionsDialog {
    
    private PropertySheetOperator pto;
    private Property property;
    private NbDialogOperator styleDialog;
    private String componentID;
    
    /**
     * @param testName
     */
    public ComponentStyleDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=3000;
        categoryName = "Woodstock Basic"; // NOI18N
        componentName = "Table"; // NOI18N
        addPoint = new java.awt.Point(50,50);
    }
    
    /**
     * @param testName
     * @param performanceDataName
     */
    public ComponentStyleDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=3000;
        categoryName = "Woodstock Basic"; // NOI18N
        componentName = "Button"; // NOI18N
        addPoint = new java.awt.Point(50,50);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new ComponentStyleDialog("testButtonStyleDialog","Button Style Dialog Open test"));
        suite.addTest(new ComponentStyleDialog("testTableStyleDialog","Table Style Dialog Open test"));
        suite.addTest(new ComponentStyleDialog("testLisbBoxStyleDialog","Listbox Style Dialog Open test"));
        return suite;
    }
    
    public void testButtonStyleDialog() {
        categoryName = "woodstock Basic"; // NOI18N
        componentName = "Button"; // NOI18N
        doMeasurement();
    }
    
    public void testTableStyleDialog() {
        categoryName = "Woodstock Basic"; // NOI18N
        componentName = "Table"; // NOI18N
        doMeasurement();
    }
    
    public void testLisbBoxStyleDialog() {
        categoryName = "Woodstock Basic"; // NOI18N
        componentName = "Listbox"; // NOI18N
        doMeasurement();
    }
    
    @Override
    public void initialize() {
        super.initialize();
        pto =  PropertySheetOperator.invoke();
        surface.clickOnSurface(addPoint.x+5, addPoint.y+5);
        componentID = new Property(pto,"id").getValue();
        property = new Property(pto,"style"); // NOI18N
    }
    
    @Override
    public ComponentOperator open() {
        log(":: open");
        property.openEditor();
        styleDialog = new NbDialogOperator(componentID);
        return null;
    }
    
    public void close() {
        log(":: close");
        styleDialog.close();
        super.close();
    }
    
    protected void shutdown() {
        super.shutdown();
        try {
            pto.close();
        } catch (Exception ex) {
            System.out.println("Catched exception at shutdown: "+ex.getMessage());
        }
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
