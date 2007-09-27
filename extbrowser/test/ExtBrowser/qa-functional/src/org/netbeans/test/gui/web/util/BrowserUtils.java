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

package org.netbeans.test.gui.web.util;

import org.netbeans.jellytools.actions.OptionsViewAction;


import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.Bundle;

import org.netbeans.jellytools.properties.ComboBoxProperty;
import org.netbeans.jellytools.properties.TextFieldProperty;
import org.netbeans.jellytools.properties.PropertySheetTabOperator;
import org.netbeans.jellytools.properties.PropertySheetOperator;

import org.netbeans.jemmy.operators.JTextComponentOperator;

import java.io.File;

public class BrowserUtils {
    private static String iSep = "|";
    private String sampleUserAgentNS7="User-Agent: Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.0.2) Gecko/20030208 Netscape/7.02";
    private String sampleUserAgentNS4="User-Agent: Mozilla/4.78 [en] (X11; U; Linux 2.4.7-10smp i686)";
    private String sampleUserAgentIE6="User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)";

    public static final int NETSCAPE4 = 0;
    public static final int NETSCAPE6 = 1;
    public static final int NETSCAPE7 = 2;
    public static final int MSIE6     = 3;
    public static final int UNKNOWN = 32;


    public BrowserUtils() {
    }

    public static void setExternalUnixBrowser() {
	String browser = Bundle.getString("org.netbeans.modules.extbrowser.Bundle","CTL_UnixBrowserName");
	setBrowser(browser);
    }
    public static void setExternalWinBrowser() {
	String browser = Bundle.getString("org.netbeans.modules.extbrowser.Bundle","CTL_WinBrowserName");
	setBrowser(browser);
    }
    public static void setSwingBrowser() {
	String browser = Bundle.getString("org.netbeans.beaninfo.Bundle","CTL_SwingBrowser");
	setBrowser(browser);
    }
    public static void setCLBrowser() {
	String browser = Bundle.getString("org.netbeans.modules.extbrowser.Bundle","CTL_SimpleExtBrowser");
	setBrowser(browser);
    }
    public static void setCLBrowserCommand(String command) {
	OptionsOperator oo = OptionsOperator.invoke();
	String ideConfiguration = Bundle.getString("org.netbeans.core.Bundle", "UI/Services/IDEConfiguration");
	String sets = Bundle.getString("org.netbeans.core.Bundle", "UI/Services/IDEConfiguration/ServerAndExternalToolSettings");
	String browsers = Bundle.getString("org.netbeans.core.Bundle", "Services/Browsers");
	String cl = Bundle.getString("org.netbeans.modules.extbrowser.Bundle","CTL_SimpleExtBrowser");
	oo.selectOption(ideConfiguration + iSep + sets + iSep + browsers + iSep + cl);
	PropertySheetOperator pso = PropertySheetOperator.invoke();
        PropertySheetTabOperator psto = new PropertySheetTabOperator(pso);
	String pnameBrowserExecutable = Bundle.getString("org.netbeans.modules.extbrowser.Bundle" ,"PROP_browserExecutable");
	TextFieldProperty pr = new TextFieldProperty(psto, pnameBrowserExecutable);
	if (!pr.getValue().equals(command)) {
	    pr.setValue(command);
	}
    }
    

    public static void setEBUBrowserCommand(String command) {
	OptionsOperator oo = OptionsOperator.invoke();
	String ideConfiguration = Bundle.getString("org.netbeans.core.Bundle", "UI/Services/IDEConfiguration");
	String sets = Bundle.getString("org.netbeans.core.Bundle", "UI/Services/IDEConfiguration/ServerAndExternalToolSettings");
	String browsers = Bundle.getString("org.netbeans.core.Bundle", "Services/Browsers");
	String cl = Bundle.getString("org.netbeans.modules.extbrowser.Bundle","CTL_UnixBrowserName");
	oo.selectOption(ideConfiguration + iSep + sets + iSep + browsers + iSep + cl);
	PropertySheetOperator pso = PropertySheetOperator.invoke();
        PropertySheetTabOperator psto = new PropertySheetTabOperator(pso);
	String pnameBrowserExecutable = Bundle.getString("org.netbeans.modules.extbrowser.Bundle" ,"PROP_browserExecutable");
	TextFieldProperty pr = new TextFieldProperty(psto, pnameBrowserExecutable);
	if (!pr.getValue().equals(command)) {
	    pr.setValue(command);
	}
    }


    public static  void setBrowser(String name) {
	//new OptionsViewAction().perform();
	OptionsOperator oo = OptionsOperator.invoke();
	String ideConfiguration = Bundle.getString("org.netbeans.core.Bundle", "UI/Services/IDEConfiguration");
	String system = Bundle.getString("org.netbeans.core.Bundle", "UI/Services/IDEConfiguration/System");
	String systemSettings = Bundle.getString("org.netbeans.core.Bundle", "Services/org-netbeans-core-IDESettings.settings");
	oo.selectOption(ideConfiguration + iSep + system + iSep + systemSettings);
	PropertySheetOperator pso = PropertySheetOperator.invoke();
        PropertySheetTabOperator psto = new PropertySheetTabOperator(pso);
	String pnameWebBrowser = Bundle.getString("org.netbeans.core.Bundle" ,"PROP_WWW_BROWSER");
	ComboBoxProperty pr = new ComboBoxProperty(psto, pnameWebBrowser);
	if (!pr.getValue().equals(name)) {
	    pr.setValue(name);
	}
    }
    public static boolean handleSwingBrowserDialog() {
	String title = Bundle.getString("org.openide.Bundle","NTF_InformationTitle");
	try {
	    NbDialogOperator dialog = new NbDialogOperator(title);
	    dialog.ok();
	    return true;
	}catch(Exception e) {
	    return false;
	}
    }

    



    /**
       DDE servers section
    */
    public static void setDDEServerExplorer() {
	System.out.println("#####\nNot implemented yet\n#####");
    }
    public static void setDDEServerNetscape() {
	System.out.println("#####\nNot implemented yet\n#####");
    }
    
    public static void setDDEServerNetscape6() {
	System.out.println("#####\nNot implemented yet\n#####");
    }
    /* End of DDE servers section */

    /**
       Get Browsers section
     */

    public static String getIEInPath() {
	System.out.println("#####\nNot implemented yet\n#####");
	return null;
    }
    public static String getIEFullPath() {
	System.out.println("#####\nNot implemented yet\n#####");
	return null;
    }
    public static String getNetscapeFullPath() {
	String[] paths = null;
	String command = null;
	if(System.getProperty("os.name").indexOf("Windows")!=-1) {
            System.out.println("##########\nThis test must be extended for Windows platform\n#######");
	    return null;
        }else {
	    paths = new String[] {"/usr/bin/netscape","/usr/local/bin/netscape","/bin/netscape"};
	}
	for(int i=0;i<paths.length;i++) {
	    if((new File(paths[i])).exists()) {
		command = paths[i] + " {URL}";
		i = paths.length;
	    }
	}
	if(command == null) {
	    StringBuffer reason = new StringBuffer("Nothing of following commands found on your system : ");
	    for(int i=0;i<paths.length;i++) {
		reason.append(paths[i] + ";");
	    }
	    System.out.println("##########\n" + reason.toString() + "\n##########");
	    return null;
	}
	return command;
    }
    public static String getNetscapeInPath() {
	String netscapeWin = "netscape.exe";
	String netscapeUx = "netscape";
	if(System.getProperty("os.name").indexOf("Windows")!=-1) {
	    return netscapeWin;
        }else {
	    return netscapeUx;
	}
    }
    public static String getNetscape6FullPath() {
	String[] paths = null;
	String command = null;
	if(System.getProperty("os.name").indexOf("Windows")!=-1) {
            System.out.println("##########\nThis test must be extended for Windows platform\n#######");
	    return null;
        }else {
	    paths = new String[] {"/usr/local/netscape6/netscape","/usr/bin/netscape6","/usr/local/bin/netscape6","/bin/netscape6"};
	}
	for(int i=0;i<paths.length;i++) {
	    if((new File(paths[i])).exists()) {
		command = paths[i] + " {URL}";
		i = paths.length;
	    }
	}
	if(command == null) {
	    StringBuffer reason = new StringBuffer("Nothing of following commands found on your system : ");
	    for(int i=0;i<paths.length;i++) {
		reason.append(paths[i] + ";");
	    }
	    System.out.println("##########\n" + reason.toString() + "\n##########");
	    return null;
	}
	return command;
    }
    public static String getNetscape6InPath() {
	String netscapeWin = "netscp6.exe";
	String netscapeUx = "netscape6";
	if(System.getProperty("os.name").indexOf("Windows")!=-1) {
	    return netscapeWin;
        }else {
	    return netscapeUx;
	}
    }



    /* Errors handling section */

    /**
       Handle error in CL browser configuration
    */
    public static boolean handleErrorInCLBrowser() {
	String title = Bundle.getString("org.openide.Bundle","NTF_WarningTitle");
	String text = Bundle.getString("org.netbeans.modules.extbrowser.Bundle","EXC_Invalid_Processor");
	NbDialogOperator op = null;
	try {
	    op = new NbDialogOperator(title);
	}catch(Exception e) {
	    System.out.println(title + " dialog not found");
	    return false;
	}
	//	JTextComponentOperator tco = new JTextComponentOperator(op,0);
	//	System.out.println("#####TEXT:" + tco.getText());
	op.ok();
	return true;
    }
    /**
       Handle error in CL browser configuration
    */
    public static boolean handleErrorInUnixBrowser() {
	String title = Bundle.getString("org.openide.Bundle","NTF_WarningTitle");
	String text = Bundle.getString("org.netbeans.modules.extbrowser.Bundle","MSG_Cant_run_netscape"); //{0} - name of command, for example netscape88
	try {
	    NbDialogOperator op = new NbDialogOperator(title);
	    op.ok();
	    return true;
	}catch(Exception e) {
	    return false;
	}
    }

    public static int getBrowserVersion(String userAgent) {
	if(userAgent.indexOf("MSIE 6")!=-1) {
	    return MSIE6;
	}
	if(userAgent.indexOf("Mozilla/4")!=-1) {
	    return NETSCAPE4;
	}
	if(userAgent.indexOf("Mozilla/5")!=-1) {
	    if(userAgent.indexOf("Netscape/7")!=-1) {
		return NETSCAPE7;
	    }
	    if(userAgent.indexOf("Netscape6/6")!=-1) {
		return NETSCAPE6;
	    }
	}
	return UNKNOWN;
    }


    public static String getBrowserDescription(int version) {
	String descr = null; 
	switch(version) {
	case NETSCAPE4: 
	    descr = "Netscape4.x browser";
	    break;
	case NETSCAPE6: 
	    descr = "Netscape6.x browser";
	    break;
	case NETSCAPE7:
	    descr = "Netscape7.x browser";
	    break;
	case MSIE6:
	    descr = "Internet Explorer 6";
	    break;
	case UNKNOWN:
	    descr = "This browser is unknown by BrowserUtils";
	    break;
	default:
	    descr = "Can't understand version " + version;
	}
	return descr;
    }
}

