package org.netbeans.test.gui.web.extbrowser.settings; 

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.ExplorerOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.NbDialogOperator;

import org.netbeans.jellytools.properties.ComboBoxProperty;
import org.netbeans.jellytools.properties.PropertySheetTabOperator;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jellytools.properties.editors.FileCustomEditorOperator;




import org.netbeans.test.gui.web.util.BrowserUtils;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.junit.NbTestSuite;
import java.io.File;

public class SystemSettings extends JellyTestCase {
    private static String fSep = System.getProperty("file.separator");
    private static String iSep = "|";
   

    public SystemSettings(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
         
    //method required by JUnit
    public static junit.framework.Test suite() {
	return new NbTestSuite(SystemSettings.class);
    }

    /**
       System settings : External Browser(Unix)
     **/
    public void testEBU() {
	String newVal = Bundle.getString("org.netbeans.modules.extbrowser.Bundle" ,"Services/Browsers/UnixWebBrowser.settings");
	testSystemValue(newVal);
    }

    /**
       System settings : External Browser(Command Line)
    **/
    public void testEBCL() {
	String newVal = Bundle.getString("org.netbeans.modules.extbrowser.Bundle" ,"Services/Browsers/SimpleExtBrowser.settings");
	testSystemValue(newVal);
    }

    /**
       System settings : Swing HTML Browser
    **/
    public void testSwing() {
	String newVal = Bundle.getString("org.netbeans.core.ui.Bundle" ,"Services/Browsers/SwingBrowser.ser");
	testSystemValue(newVal);
    }


    private void testSystemValue(String newVal) {
	OptionsOperator oo = OptionsOperator.invoke();
	String ideConfiguration = Bundle.getString("org.netbeans.core.Bundle", "UI/Services/IDEConfiguration");
	String sys = Bundle.getString("org.netbeans.core.Bundle", "UI/Services/IDEConfiguration/System");
	String sett = Bundle.getString("org.netbeans.core.Bundle","Services/org-netbeans-core-IDESettings.settings");
	oo.selectOption(ideConfiguration + iSep + sys + iSep + sett);
	PropertySheetOperator pso = PropertySheetOperator.invoke();
        PropertySheetTabOperator psto = new PropertySheetTabOperator(pso);
	String pnameWebBrowser = Bundle.getString("org.netbeans.modules.web.core.Bundle" ,"PROP_WWWBrowser");
	ComboBoxProperty pr = new ComboBoxProperty(psto, pnameWebBrowser);
	pr.setValue(newVal);
	if (!pr.getValue().equals(newVal)) {
	    fail("Web Browser is not changed");
	}
	oo.close();
	oo = OptionsOperator.invoke();
	oo.selectOption(ideConfiguration + iSep + sys + iSep + sett);
	pso = PropertySheetOperator.invoke();
        psto = new PropertySheetTabOperator(pso);
	pr = new ComboBoxProperty(psto, pnameWebBrowser);
	if (!pr.getValue().equals(newVal)) {
	    fail("Web Browser property not saved");
	}
    }
}








