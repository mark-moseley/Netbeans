/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.test.gui.web.extbrowser.execution; 

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.ExplorerOperator;
import org.netbeans.jellytools.NbFrameOperator;



import org.netbeans.jellytools.nodes.FolderNode;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.HTMLNode;


import org.netbeans.jellytools.actions.ExecuteAction;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.web.test.util.Utils;


import org.netbeans.jemmy.Waiter;

import org.netbeans.web.test.nodes.JSPNode;
import org.netbeans.web.test.nodes.ServletNode;
import org.netbeans.test.gui.web.util.JSPServletResponseWaitable;
import org.netbeans.test.gui.web.util.HttpRequestWaitable;
import org.netbeans.test.gui.web.util.BrowserUtils;



import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;

public class ExecuteCLBrowser extends JellyTestCase {
    private static String workDir = null;     
    private static String webModule = null;
    private static String wmName = "wm1";
    private static String fSep = System.getProperty("file.separator");
    private static String iSep = "|";
    private static String classes = "Classes";
    private static String servletForExecution = "ServletForExecution";
    private static String htmlForExecution = null;;
    private static String wmForExecution = "wmForExecution"; 
    private static String urlToRedirectFromHTML = "RedirectFromHtmlForExecution.html";
    private static String jspForExecution = null;
    private static String pkg = "execution";
    private static ExplorerOperator explorer = null;
    private static boolean first = true;
    private static String netscape  = "netscape {URL}";
    private String servletId = "cebde3e2-e8f1-4421-8a1c-df11dcc6e79a";
    private String jspId     = "c78eae2b-39f2-4b41-b2be-032e5373d7f4";
    private String wmId      = "9bc4ac0b-0a21-452a-9e51-ca9df3c2fa04";
    private int defaultPort = 1357;
    private int port = 2468;
    private String defaultAnswer = "HTTP/1.0 200 OK\nServer: FFJ Automated Tests SimpleServ\nLast-Modified: Fri, 12 Jul 2002 09:53:56 GMT\nContent-Length: 281\nConnection: close\nContent-Type: text/html\n\n<html>\n<head>\n   <title>Tests passed</title>\n</head>\n<body>\n<center><H1>Request Accepted</H1></center>\n</body>\n</html>";
    private static int defaultBrowser = -1;
    

    public ExecuteCLBrowser(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
         
    //method required by JUnit
    public static junit.framework.Test suite() {
	workDir = System.getProperty("extbrowser.workdir").replace('/', fSep.charAt(0));
	webModule = workDir + fSep + wmName;
	htmlForExecution = webModule + iSep + "html" + iSep + "HtmlFileForExecution";
	jspForExecution = webModule + iSep + "jsp" + iSep + "JSPForExecution";
	pkg = webModule + iSep + "WEB-INF" + iSep + classes + iSep + pkg;
	String defBr = System.getProperty("extbrowser.default");
	if(defBr.equals("ns4"))
	    defaultBrowser = BrowserUtils.NETSCAPE4;
	if(defBr.equals("ns6"))
	    defaultBrowser = BrowserUtils.NETSCAPE6;
	if(defBr.equals("ns7"))
	    defaultBrowser = BrowserUtils.NETSCAPE7;
	if(defBr.equals("ie6"))
	    defaultBrowser = BrowserUtils.MSIE6;

	String wmc = System.getProperty("extbrowser.mountcount");
	int count = 0;
	if(wmc != null) {
	    count = new Integer(wmc).intValue();
	}
	if(first) {
	    while(count >0) {
		Utils.handleDialogAfterNewWebModule();
		count--;
	    }
	    first = false;
	}
	return new NbTestSuite(ExecuteCLBrowser.class);
    }
    
    public void testExecuteHtml() {
	NbFrameOperator fo = null;
	HTMLNode node1 = null;
	BrowserUtils.setCLBrowser();
	BrowserUtils.setCLBrowserCommand(netscape);
	try {
	    node1 = new HTMLNode(htmlForExecution);
	}catch(Exception e) {
	    fail("Not found: " + htmlForExecution);
	}
	new ExecuteAction().perform(node1);
	HttpRequestWaitable hrw = new HttpRequestWaitable(urlToRedirectFromHTML, defaultAnswer, defaultPort);
	Waiter w = new Waiter(hrw);
	try {
	    w.waitAction(hrw);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Looks like browser not started or URL not loaded");
	}
	if(BrowserUtils.getBrowserVersion(hrw.getUserAgent()) != defaultBrowser) {
	    fail("Wrong browser used.\n" + hrw.getUserAgent() + " instead of " + BrowserUtils.getBrowserDescription(defaultBrowser));
	}
    }
    
    public void testExecuteSevlet() {
	ServletNode node1 = null;
	BrowserUtils.setCLBrowser();
	BrowserUtils.setCLBrowserCommand(netscape);
	try {
	    node1 = new ServletNode(pkg + iSep + servletForExecution);
	}catch(Exception e) {
	    fail("Not found: " + servletForExecution);
	}
	JSPServletResponseWaitable jsrw = new JSPServletResponseWaitable(servletId, defaultAnswer, port);
	node1.execute();
	Waiter w = new Waiter(jsrw);
	try {
	    w.waitAction(jsrw);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Looks like browser not started or URL not loaded");
	}
    }

    public void testExecuteJSP() {
	JSPNode node1 = null;
	BrowserUtils.setCLBrowser();
	BrowserUtils.setCLBrowserCommand(netscape);
	try {
	    node1 = new JSPNode(jspForExecution);
	}catch(Exception e) {
	    fail("Not found: " + jspForExecution);
	}
	JSPServletResponseWaitable jsrw = new JSPServletResponseWaitable(jspId, defaultAnswer, port);
	node1.execute();
	Waiter w = new Waiter(jsrw);
	try {
	    w.waitAction(jsrw);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Looks like browser not started or URL not loaded");
	}
    }

    public void testExecuteWebModule() {
	FolderNode node1 = null;
	BrowserUtils.setCLBrowser();
	BrowserUtils.setCLBrowserCommand(netscape);
	try {
	    node1 = new FolderNode(workDir + fSep + wmForExecution + iSep + "WEB-INF");
	}catch(Exception e) {
	    fail("Web Module for execution not found");
	}
	JSPServletResponseWaitable jsrw = new JSPServletResponseWaitable(wmId, defaultAnswer, port);
	new ExecuteAction().perform(node1);
	Waiter w = new Waiter(jsrw);
	try {
	    w.waitAction(jsrw);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Looks like browser not started or URL not loaded");
	}
    }
    
   
}












