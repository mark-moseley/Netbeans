/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.debugger.Session;

import org.netbeans.modules.cnd.debugger.gdb.GdbDebuggerImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
* Implementation of breakpoint on method.
*
* @author   Gordon Prieur (copied from Jan Jancura's JPDA implementation)
*/
public class LineBreakpointImpl extends BreakpointImpl {

    private LineBreakpoint      breakpoint;
    private int                 lineNumber;
    private BreakpointsReader   reader;

    
    public LineBreakpointImpl(LineBreakpoint breakpoint, BreakpointsReader reader,
                GdbDebuggerImpl debugger, Session session) {
        super(breakpoint, reader, debugger, session);
        this.reader = reader;
        this.breakpoint = breakpoint;
        lineNumber = breakpoint.getLineNumber();
        set();
    }
    
    void fixed() {
        lineNumber = breakpoint.getLineNumber();
        super.fixed();
    }
    
    protected void setRequests() {
        lineNumber = breakpoint.getLineNumber();
        String path = getDebugger().getProjectRelativePath(breakpoint.getPath());
        getDebugger().getGdbProxy().break_insert(path + ":" + lineNumber); // NOI18N
        breakpoint.setPending();
    }


    /**
     * Normalizes the given path by removing unnecessary "." and ".." sequences.
     * This normalization is needed because the compiler stores source paths like
     * N"foo/../inc.jsp" into .class files. 
     * Such paths are not supported by our ClassPath API.
     * TODO: compiler bug? report to JDK?
     * 
     * @param path path to normalize
     * @return normalized path without "." and ".." elements
     */ 
    private static String normalize(String path) {
	Pattern thisDirectoryPattern = Pattern.compile("(/|\\A)\\./");
	Pattern parentDirectoryPattern = Pattern.compile("(/|\\A)([^/]+?)/\\.\\./");

	for (Matcher m = thisDirectoryPattern.matcher(path); m.find(); ) {
	    path = m.replaceAll("$1");
	    m = thisDirectoryPattern.matcher(path);
	}
	for (Matcher m = parentDirectoryPattern.matcher(path); m.find(); ) {
	    if (!m.group(2).equals("..")) {
		path = path.substring(0, m.start()) + m.group(1) + path.substring(m.end());
		m = parentDirectoryPattern.matcher(path);        
	    }
	}
	return path;
    }
}

