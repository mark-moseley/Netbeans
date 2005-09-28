/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project;

import java.util.Date;
import junit.framework.Assert;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;

/** NbTestCase logging error manager.
 *
 * @author Jaroslav Tulach
 */
public class ErrorManagerImpl extends ErrorManager {
	static NbTestCase running;
    
    private String prefix;
	
    /** Creates a new instance of ErrorManagerImpl */
    public ErrorManagerImpl() {
        this("[em]");
    }
    
    private ErrorManagerImpl(String p) {
        this.prefix = p;
    }

    public static void registerCase(NbTestCase r) {
		running = r;
	}
    

	public Throwable attachAnnotations(Throwable t, Annotation[] arr) {
        return t;
	}

	public Annotation[] findAnnotations(Throwable t) {
        return null;
	}

	public Throwable annotate(Throwable t, int severity, String message, String localizedMessage, Throwable stackTrace, Date date) {
        return t;
	}

	public void notify(int severity, Throwable t) {
        java.io.StringWriter w = new java.io.StringWriter();
        w.write(prefix);
        w.write(' ');
        t.printStackTrace(new java.io.PrintWriter(w));
        
        System.err.println(w.toString());
        
        if (running == null) {
            return;
        }
        running.getLog().println(w.toString());
	}

	public void log(int severity, String s) {
        String msg = prefix + ' ' + s;
        if (severity != INFORMATIONAL) {
            System.err.println(msg);
        }
        
        if (running == null) {
            return; 
        }
        running.getLog().println(msg);
	}

	public ErrorManager getInstance(String name) {
        return new ErrorManagerImpl(name);
	}
    
}
