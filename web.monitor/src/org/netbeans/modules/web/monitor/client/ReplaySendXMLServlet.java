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

package org.netbeans.modules.web.monitor.client; 

import java.io.*;
import java.text.*;
import java.util.Enumeration;
import java.util.StringTokenizer;
import javax.servlet.*;
import javax.servlet.http.*;

import org.netbeans.modules.web.monitor.data.MonitorData;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;

import org.openide.execution.NbClassPath;

/*
 * Send the xml file for a transaction back to the replay filter/interceptor.
 */
  
public class ReplaySendXMLServlet extends HttpServlet {


    private static FileObject currDir = null;
    private static FileObject saveDir = null;
    private static FileObject replayDir = null;
    private final static boolean debug = false;
     

    //
    // The action is really going to happen in the GET??
    //
    public void doPost(HttpServletRequest req, HttpServletResponse res) 
	throws ServletException, IOException {

	if(debug) 
	    System.out.println("\n\nReplaySendXMLServlet:  DoPost.\n\n"); //NOI18N
	PrintWriter out = res.getWriter();
	try { 
	    out.println("Shouldn't use POST for this!");  //NOI18N
	}
	catch (Exception e) { 
	}
	try { out.close(); } catch(Exception ex) {}
    }

    // Return the desired transaction file in the response.
    //
    public void doGet(HttpServletRequest req, HttpServletResponse res) 
	throws ServletException, IOException {

	if(debug) System.out.println("\n\nReplaySendXMLServlet:  DoGet.\n\n"); //NOI18N

	String status = null;
	String id = null;
	
	try {
	    status = req.getParameter("status");  //NOI18N
	    id = req.getParameter("id");  //NOI18N
	    if(debug) 
		System.out.println("\n\nReplaySendXMLServlet: id=" +  //NOI18N
				   id + " ,status=" + status);  //NOI18N
	}
	catch(Exception ex) {
	    // PENDING - deal 
	    return;
	}

	Controller controller = MonitorAction.getController();
	MonitorData md = controller.retrieveMonitorData(id, status);
	if(md != null) {

	    Util.setSessionCookieHeader(md);
	    String method =
		md.getRequestData().getAttributeValue("method");  //NOI18N
	    
	    if(method.equals("POST")) {  //NOI18N
		Util.removeParametersFromQuery(md.getRequestData());
	    }
	    else if(method.equals("GET")) {  //NOI18N
		Util.composeQueryString(md.getRequestData());
	    }

	    res.addHeader("Content-type", //NOI18N
			  "text/plain;charset=\"UTF-8\"");  //NOI18N

	    PrintWriter out = res.getWriter();
	    try {
		md.write(out);
	    }
	    catch(NullPointerException npe) {
		if(debug) npe.printStackTrace();
	    }
	    catch(IOException ioe) {
		if(debug) ioe.printStackTrace();
	    }
	    catch(Throwable t) {
		if(debug) t.printStackTrace();
	    }
	    finally {
		// Do we need to close out? 
		try {
		    out.close();
		}
		catch(Exception ex) {
		}
	    }
	}
	if(debug) {
	    try {
		StringBuffer buf = new StringBuffer
		    (System.getProperty("java.io.tmpdir")); // NOI18N
		buf.append(System.getProperty("file.separator")); // NOI18N
		buf.append("replay-servlet.xml"); // NOI18N
		File file = new File(buf.toString()); 
		log("Writing replay data to " // NOI18N
		    + file.getAbsolutePath()); 		
		FileOutputStream fout = new FileOutputStream(file);
		PrintWriter pw2 = new PrintWriter(fout);
		md.write(pw2);
		pw2.close();
		fout.close();

	    }
	    catch(Throwable t) {
	    }   
	}
	
	if(debug) 
	    System.out.println("ReplaySendXMLServlet doGet exiting...");  //NOI18N
    }
} //ReplaySendXMLServlet.java



