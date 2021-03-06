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
package org.netbeans.xml.test.core;

import java.awt.event.KeyEvent;
import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.xml.test.core.wizardoperator.DTDOptionsWizardOperator;
import org.netbeans.xml.test.core.wizardoperator.NewXMLFileTestTypeWizardOperator;
import org.netbeans.xml.test.core.wizardoperator.NewXMLFileWizardOperator;
import org.netbeans.xml.test.core.wizardoperator.XSDOptionsWizardOperator;

/**
 * <P>
 * <P>
 * <FONT COLOR="#CC3333" FACE="Courier New, Monospaced" SIZE="+1">
 * <B>
 * <BR> XML Module Jemmy Test: NewFromTemplate
 * </B>
 * </FONT>
 * <BR><BR><B>What it tests:</B><BR>
 *
 * This test tests New From Template action on all XML's templates.
 *
 * <BR><BR><B>How it works:</B><BR>
 *
 * 1) create new documents from template<BR>
 * 2) write the created documents to output<BR>
 * 3) close source editor<BR>
 *
 * <BR><BR><B>Settings:</B><BR>
 * none<BR>
 *
 * <BR><BR><B>Output (Golden file):</B><BR>
 * Set XML documents.<BR>
 *
 * <BR><B>To Do:</B><BR>
 * none<BR>
 *
 * <P>Created on Januar 09, 2001, 12:33 PM
 * <P>
 */

/**
 *
 * @author jindra
 */
public class CoreTemplatesTest extends XMLTest {
    
    protected static boolean DEBUG = false;
    private static final String packageName = "pack";
    private static final String folder = "web";
    private static final String projectName = "CoreTemplatesTestProject";
    private static final String category = Bundle.getString(CORE_BUNDLE, "OpenIDE-Module-Display-Category");
    private static final String wizardTitle = Bundle.getString(UI_BUNDLE, "LBL_NewFileWizard_Title");
    private static final int WELL_FORMED = 0;
    private static final int DTD_FORMED = 1;
    private static final int XSD_FORMED = 2;
    private boolean generateGoldenFiles = false;
    
    
    
    /** Creates new CoreTemplatesTest */
    public CoreTemplatesTest(String testName) {
        super(testName);
    }
    
    //----------------- TESTS ------------------//
    
    public void testNewXML() throws Exception{
        create(WELL_FORMED);
    }
    
    public void testNewXMLDTDFormed() throws Exception{
        create(DTD_FORMED);
    }
    
    public void testNewXMLXSDFormed() throws Exception{
        create(XSD_FORMED);
    }
    
    public void testNewDTD() throws Exception{
        NewFileWizard(Bundle.getString(CORE_BUNDLE, "Templates/XML/emptyDTD.dtd"), false);
    }
    
    public void testNewXMLSchema() throws Exception{
        NewFileWizard(Bundle.getString(XMLSchema_BUNDLE, "Templates/XML/XMLSchema.xsd"), false);
    }
    
    public void testNewCSS() throws Exception{
        NewFileWizard(Bundle.getString(CSS_BUNDLE, "Templates/XML/CascadeStyleSheet.css"), false);
    }
    
    public void testNewXSLStyleSheet() throws Exception{
        NewFileWizard(Bundle.getString(CORE_BUNDLE, "Templates/XML/StyleSheet.xsl"), false);
    }
    
    public void testNewXMLEntity() throws Exception {
        NewFileWizard(Bundle.getString(CORE_BUNDLE, "Templates/XML/xml_entity.ent"), false);
    }
    
    
    // ------------- LIB --------------------------//
    
    private void NewFileWizard(String fileType, boolean next) throws IOException{
        String fileName = this.getName();
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke(wizardTitle);
        nfwo.selectProject(projectName);
        nfwo.selectCategory(category);
        nfwo.selectFileType(fileType);
        nfwo.next();
        NewXMLFileWizardOperator op = new NewXMLFileWizardOperator("New " + fileType);
        op.folder().setText(folder);
        op.fileName().setText(fileName);
        if (next) op.next();
        else{
            op.finish();
            removeComment();
            ending();
        }
    }
    
    
    private void create(int formed) throws IOException{
        // select file type, category, folder and name
        String fileType =Bundle.getString(CORE_BUNDLE, "Templates/XML/XMLDocument.xml");
        NewFileWizard(fileType, true);
        NewXMLFileTestTypeWizardOperator ttOp = new NewXMLFileTestTypeWizardOperator();
        switch (formed){
        case WELL_FORMED:
            ttOp.wellFormed().clickMouse();
            ttOp.finish();
            break;
        case XSD_FORMED:
            ttOp.xsdFormed().clickMouse();
            ttOp.next();
            XSDOptionsWizardOperator opXSD = new XSDOptionsWizardOperator();
            opXSD.rootElement().typeText("basic");
            opXSD.namespace().typeText("namespace");
            opXSD.namespace().pushKey(KeyEvent.VK_TAB);
            opXSD.uri().setText("soubor.xsd");
            opXSD.finish();
            break;
        case DTD_FORMED:
            ttOp.dtdFormed().clickMouse();
            ttOp.next();
            DTDOptionsWizardOperator opDTD = new DTDOptionsWizardOperator();
            opDTD.publicID().selectItem("-//NetBeans//DTD Mode Properties 2.2//EN");
            opDTD.systemID().typeText("systemID");
            opDTD.documentRoot().selectItem(1);
            opDTD.finish();
            break;
        }
        removeComment();
        ending();
    }
    
    private void removeComment(){
        String author = "Author.*";
        String created = "Created.*";
        EditorOperator eo = new EditorOperator(this.getName());
        String text = eo.getText();
        
        //        int begin = text.indexOf(author);
        //        int end = text.indexOf("\n", begin);
        text = text.replaceFirst(author, author);
        //        eo.replace(eo.getText().substring(begin, end), "");// remove author because of different names
        //        begin = text.indexOf(created);
        //        end = text.indexOf("\n", begin);
        text = text.replaceFirst(created, created);
        //        eo.replace(eo.getText().substring(begin, end), "");// remove cretated because of date
        ref(text);
    }
    
    public boolean generateGoldenFiles(){
        return generateGoldenFiles;
    }
    
    // ----------------------- MAIN ---------------------------//
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        initialization(projectName);
        suite.addTest(new CoreTemplatesTest("testNewXML"));
        suite.addTest(new CoreTemplatesTest("testNewXMLDTDFormed"));
        suite.addTest(new CoreTemplatesTest("testNewXMLXSDFormed"));
        suite.addTest(new CoreTemplatesTest("testNewDTD"));
        suite.addTest(new CoreTemplatesTest("testNewXMLSchema"));
        suite.addTest(new CoreTemplatesTest("testNewXSLStyleSheet"));
        suite.addTest(new CoreTemplatesTest("testNewXMLEntity"));
        suite.addTest(new CoreTemplatesTest("testNewCSS"));
        return suite;
    }
    
    public static void main(String[] args) throws Exception {
        //DEBUG = true;
        //JemmyProperties.getCurrentTimeouts().loadDebugTimeouts();
        TestRunner.run(suite());
    }
    
}
