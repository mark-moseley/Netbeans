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

package org.netbeans.modules.web.monitor.client; 

import java.io.*;
import java.text.*;
import java.util.Enumeration;
import java.util.StringTokenizer;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.swing.SwingUtilities;

import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

import org.netbeans.modules.web.monitor.server.Constants;

/*
 * Put a transaction
 */
  
public class PutTransaction extends HttpServlet {

    private static FileObject currDir = null;
    private static boolean debug = false;
     
    private ServletConfig servletConfig = null;

    public void doPost(HttpServletRequest req, HttpServletResponse res) 
	throws ServletException, IOException {
	 
	if(debug) log("doPost"); //NOI18N
	if(currDir == null) {
	    try { 
		currDir = Controller.getCurrDir();
	    }
	    catch(FileNotFoundException ex) {
		// PENDING report this error properly
		if(debug) log("Couldn't write the transaction data");  //NOI18N
		return;
	    }
	}

	// As soon as you get the parameters, you've gotten an input
	// string for this. Don't do that. 

	String id = req.getQueryString(); 
	if(id == null || id.length() == 0) { 
	    if(debug) log("Bad request, exiting..."); //NOI18N
	    return; 
	}

	id = id.substring(0, id.indexOf(Constants.Punctuation.itemSep));

	if(debug) log(" Trying to add the transaction"); //NOI18N
	FileObject fo = null;
	 
	try {
	    if(debug) log(" Before creating the file"); //NOI18N
	    fo = currDir.createData(id, "xml"); //NOI18N
	    if(debug) log(" After creating the file"); //NOI18N
	}
	catch(IOException ioex) { 
	    if(debug) log(" Could not create the file, exiting..."); 
	    return;
	} 
	FileLock lock = null;
	try { 
	    lock = fo.lock();
	    if(debug) log(" Got the lock"); //NOI18N
	} 
	catch(FileAlreadyLockedException falex) { 
	    if(debug) log(" Couldn't get a file lock, exiting..."); //NOI18N
	    return; 
	} 

	PrintWriter fout = null, out = null;
	InputStreamReader isr = null;
	boolean success = false;
	try { 

	    fout = new PrintWriter(fo.getOutputStream(lock));
	    isr = new InputStreamReader(req.getInputStream());

	    char[] charBuf = new char[4096];
	    int numChars;
	     
	    while((numChars = isr.read(charBuf, 0, 4096)) != -1) {
		fout.write(charBuf, 0, numChars);
	    }
	    success = true;
 	    if(debug) log("...success"); //NOI18N
	}
	catch(IOException ioex) {
	    if (debug) { 
		log("Failed to read/write the record:"); 
		log(ioex);
	    }
	}
	finally {
	    lock.releaseLock(); 

	    try { 
		res.setContentType("text/plain");  //NOI18N	    
		out = res.getWriter();
		out.println(Constants.Comm.ACK); 
	    }
	    catch(Exception ex) {
		// It doesn't actually matter if this goes wrong
	    } 

	    try {out.close(); }
	    catch(Exception ex2) { }

	    try { isr.close();}
	    catch(Exception ex3) { }

	    try { fout.close(); }
	    catch(Exception ex4) { }
	}
        final boolean success2 = success;
        final String id2 = id;
        // window system code must be run in AWT thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run () {
                if(success2) {
		    MonitorAction.addTransaction(id2); 
		}
	    }});
    }

    // PENDING - deal better with this
    public void doGet(HttpServletRequest req, HttpServletResponse res) 
	throws ServletException, IOException {

	if(debug) log("doGet");  //NOI18N

	PrintWriter out = res.getWriter();
	try { 
	    //out.println(id); 
	    out.println("Shouldn't use GET for this!");  //NOI18N
	}
	catch (Exception e) { 
	    if(debug) log(e.getMessage());
	}
	try { out.close(); } catch(Exception ex) {}
    }


    /**
     * Init method for this filter 
     *
     */
    public void init(ServletConfig servletConfig) { 

	this.servletConfig = servletConfig;
	if(debug) log("init");  //NOI18N
    }
    
    public void log(String msg) {
	System.out.println("PutTransaction::" + msg); //NOI18N
	
    }

    public void log(Throwable t) {
	log(getStackTrace(t));
    }


    public static String getStackTrace(Throwable t) {

	String stackTrace = null;
	    
	try {
	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    t.printStackTrace(pw);
	    pw.close();
	    sw.close();
	    stackTrace = sw.getBuffer().toString();
	}
	catch(Exception ex) {}
	return stackTrace;
    }

} //PutTransaction.java



