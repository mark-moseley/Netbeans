/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * ErrWriter.java
 *
 * Created on May 9, 2004, 5:06 PM
 */

package org.netbeans.core.output2;

import org.openide.ErrorManager;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

import java.io.IOException;

/**
 * Wrapper OutputWriter for the standard out which marks its lines as being
 * stderr.
 *
 * @author  Tim Boudreau
 */
class ErrWriter extends OutputWriter {
    private OutWriter wrapped;
    /** Creates a new instance of ErrWriter */
    public ErrWriter(OutWriter wrapped) {
        super (new OutWriter.DummyWriter());
        this.wrapped = wrapped;
    }
    
    public void println(String s, OutputListener l) throws java.io.IOException {
        closed = false;
        synchronized (wrapped) {
            wrapped.println (s, l);
            wrapped.markErr();
        }
    }
    
    public void reset() throws IOException {
        ErrorManager.getDefault().log ("Do not call reset() on the error io," +
        " only on the output IO.  Reset on the error io does nothing.");
        closed = false;
    }
    
    public void close() {
        closed = true;
        wrapped.errClosed();
    }

    boolean closed;
    boolean isClosed() {
        return closed;
    }

    void unclose() {
        closed = false;
    }

    public void flush() {
        wrapped.flush();
    }
    
    public boolean checkError() {
        return wrapped.checkError();
    }    
    
    public void write(int c) {
        closed = false;
        synchronized (wrapped) {
            wrapped.write(c);
            wrapped.markErr();
        }
    }
    
    public void write(char buf[], int off, int len) {
        closed = false;
        synchronized (wrapped) {
            wrapped.write (buf, off, len);
            wrapped.markErr();
        }
    }
    
    public void write(String s, int off, int len) {
        closed = false;
        synchronized (wrapped) {
            wrapped.write (s, off, len);
            wrapped.markErr();
        }
    }
    
    public void println(boolean x) {
        closed = false;
        synchronized (wrapped) {
            wrapped.println (x);
            wrapped.markErr();
        }
    }

    public void println(int x) {
        closed = false;
        synchronized (wrapped) {
            wrapped.println(x);
            wrapped.markErr();
        }
    }
    
    public void println(char x) {
        closed = false;
        synchronized (wrapped) {
            wrapped.println(x);
            wrapped.markErr();
        }
    }
    
    public void println(long x) {
        closed = false;
        synchronized (wrapped) {
            wrapped.println(x);
            wrapped.markErr();
        }
    }
    
    public void println(float x) {
        closed = false;
        synchronized (wrapped) {
            wrapped.println(x);
            wrapped.markErr();
        }
    }
    
    public void println(double x) {
        closed = false;
        synchronized (wrapped) {
            wrapped.println(x);
            wrapped.markErr();
        }
    }
    
    public void println(char x[]) {
        closed = false;
        synchronized (wrapped) {
            wrapped.println(x);
            wrapped.markErr();
        }
    }
    
    public void println(String x) {
        closed = false;
        synchronized (wrapped) {
            wrapped.println(x);
            wrapped.markErr();
        }
    }
    
    public void println(Object x) {
        closed = false;
        synchronized (wrapped) {
            wrapped.println(x);
            wrapped.markErr();
        }
    }
}
