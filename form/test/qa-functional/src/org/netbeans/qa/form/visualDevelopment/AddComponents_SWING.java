/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.qa.form.visualDevelopment;


import org.netbeans.junit.NbTestSuite;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

import org.netbeans.jellytools.*;
import org.netbeans.jellytools.modules.form.*;
import org.netbeans.jellytools.modules.form.actions.FormEditorViewAction;
import org.netbeans.jellytools.modules.form.properties.editors.*;
import org.netbeans.jellytools.nodes.*;
import org.netbeans.jellytools.properties.*;
import org.netbeans.jellytools.actions.*;

import org.netbeans.jemmy.operators.*;
import java.util.*;
import org.netbeans.qa.form.*;
import java.io.*;

/**
 *<P>
 *<B><BR> Test create frame.</B>
 *
 *<BR><BR><B>What it tests:</B><BR>
 *  Frame containing all components from Component Palette SWING category try compile.
 *<BR><BR><B>How it works:</B><BR>
 *  Find tested form file, add all components from SWING category and compile created frame (check compile resolution).
 *
 *<BR><BR><B>Settings:</B><BR>
 *  Jemmy/Jelly classes, VisualDevelopmentSupport class in the classpath.
 *
 *<BR><BR><B>Resources:</B><BR>
 *  File (Resources.) clear_Frame(java/form) generated by NBr32(37).
 *
 *<BR><B>Possible reasons of failure</B>
 * <BR><U>jelly didn't find menu or popup menu</U>
 * <BR><U>is impossible add component or components in SWING category is another as in NB r3.2 (37)</U>
 * <BR><U>component was't add correctly or generated source code is wrong</U>
 *
 * @author  Marian.Mirilovic@czech.sun.com
 * @version
 */
public class AddComponents_SWING extends JellyTestCase {
    MainWindowOperator mainWindow;
    public String fileName = "clear_JFrame";
    String packageName = "data";
    String fileSystem;
    
    public AddComponents_SWING(String testName) {
        super(testName);
    }
    
    protected void setUp() {
        mainWindow = MainWindowOperator.getDefault();
        mainWindow.setSDI();
        mainWindow.switchToGUIEditingWorkspace();
        FilesystemNode node = new FilesystemNode("src");
        fileSystem = node.getTreePath().getPathComponent(1).toString();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new AddComponents_SWING("testAddAndCompile"));
        suite.addTest(new AddComponents_SWING("testFormFile"));
        suite.addTest(new AddComponents_SWING("testJavaFile"));
        return suite;
    }
    
    
    /** Run test.
     */
    public void testAddAndCompile() {

        String categoryName_1 = "Swing";
        String categoryName_2 = "Swing (Other)";
        
        
        FormNode formnode = new FormNode("src|" + packageName + "|" + fileName);
        formnode.open();
        log("Try to find Form Editor window ");
        FormEditorOperator formeditor = new FormEditorOperator();
        formeditor.selectForm(fileName);        
        log("\t - Form Editor Window finded OK");
                
        log("Try to find Form Designer ");        
        FormDesignerOperator formDesigner = formeditor.designer();        
        log("\t - Form Designer finded OK");
        
        
        // add all beans from tab to form
        ComponentPaletteOperator palette = formeditor.palette();
        PaletteUtil paletteUtil = new PaletteUtil(palette);        
        
        JListOperator list = palette.selectPage(categoryName_1);
        for (int i=0;i<list.getModel().getSize();i++) {
            org.netbeans.modules.form.palette.PaletteItemNode comp =
                (org.netbeans.modules.form.palette.PaletteItemNode)(list.getModel().getElementAt(i));
            String component = comp.getDisplayName();
            System.out.println("component: " + component);
            formeditor.addComponent(categoryName_1, component, formDesigner.componentLayer().getSource()  );            
        }        
        
        list = palette.selectPage(categoryName_2);
        for (int i=0;i<list.getModel().getSize();i++) {
            org.netbeans.modules.form.palette.PaletteItemNode comp =
                (org.netbeans.modules.form.palette.PaletteItemNode)(list.getModel().getElementAt(i));
            String component = comp.getDisplayName();
            System.out.println("component: " + component);
            formeditor.addComponent(categoryName_2, component, formDesigner.componentLayer().getSource()  );            
        }        
        
        // close form editor window
        log("Try to close Form Editor window ");
        formeditor.close();
        log(" - ok");
        
        // try compile created source file and check compile errors
        formnode.compile();
        
        log("All components from Component Palette : " + categoryName_1 + " - were added to " + fileName);
        log("All components from Component Palette : " + categoryName_2 + " - were added to " + fileName);
    }
    
    
    /** Run test.
     */
    public void testFormFile() {
        //VisualDevelopmentSupport.fileToOut(VisualDevelopmentSupport.Resources, fileName, "form", getRef() );
        try {
            getRef().print(
                VisualDevelopmentUtil.readFromFile(                
                fileSystem + File.separatorChar + packageName + File.separatorChar + fileName + ".form")
                );
        } catch (Exception e) {
            fail("Fail during create reffile: " + e.getMessage());
        }
        
        System.out.println("reffile: " + this.getName()+".ref");
        try {
            System.out.println("workdir: " + getWorkDir());
        } catch (Exception e) {
            System.out.println("e:" + e.getMessage() );
        }
        if (System.getProperty("java.version").startsWith("1.3")) {
            compareReferenceFiles(this.getName()+".ref",this.getName()+"_13.pass",this.getName()+".diff");
        } else
            compareReferenceFiles();
    }
    
    /** Run test.
     */
    public void testJavaFile() {
        //VisualDevelopmentSupport.fileToOut(VisualDevelopmentSupport.Resources, fileName, "java", getRef());
        try {
            getRef().print(
                VisualDevelopmentUtil.readFromFile(
                fileSystem + File.separatorChar + packageName + File.separatorChar + fileName + ".java")
                );
        } catch (Exception e) {
            fail("Fail during create reffile: " + e.getMessage());
        }
        
            
        
        if (System.getProperty("java.version").startsWith("1.3")) {
            compareReferenceFiles(this.getName()+".ref",this.getName()+"_13.pass",this.getName()+".diff");
        } else
            compareReferenceFiles();
    }
    
    
    
    /** Test could be executed internaly in Forte
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        System.setProperty("nbjunit.workdir","c:/z");
        junit.textui.TestRunner.run(suite());
    }
    
}
