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


package org.openide.text;
import java.io.StringWriter;
import javax.swing.text.*;
import javax.swing.text.StyledDocument;

import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;

/**
 * Simulates issue 46981. Editor locks the document, but somebody else closes it
 * while it is working on it and a deadlock occurs.
 * @author  Petr Nejedly, Jaroslav Tulach
 */
public class DocumentCannotBeClosedWhenAWTBlockedTest extends NbTestCase implements CloneableEditorSupport.Env {
    /** the support to work with */
    private CES support;
    // Env variables
    private String content = "Hello";
    private boolean valid = true;
    private boolean modified = false;
    private java.util.Date date = new java.util.Date ();
    private java.util.List/*<java.beans.PropertyChangeListener>*/ propL = new java.util.ArrayList ();
    private java.beans.VetoableChangeListener vetoL;
    
    
    /** lock to use for communication between AWT & main thread */
    private Object LOCK = new Object ();
    
    /** Creates new TextTest */
    public DocumentCannotBeClosedWhenAWTBlockedTest(String s) {
        super(s);
    }
    
    protected void setUp () throws Exception {
		System.setProperty("org.openide.util.Lookup", DocumentCannotBeClosedWhenAWTBlockedTest.class.getName() + "$Lkp");
		
        super.setUp();
		
		Lookup l = Lookup.getDefault();
		if (!(l instanceof Lkp)) {
			fail("Wrong lookup: " + l);
		}
		
		clearWorkDir();
		
        support = new CES (this, org.openide.util.Lookup.EMPTY);
		
		ErrManager.messages.setLength(0);
    }
    
    public void testModifyAndBlockAWTAndTryToClose () throws Exception {
        StyledDocument doc = support.openDocument ();
		doc.insertString(0, "Ble", null);
		
		assertTrue("Modified", support.isModified());
			
		class Block implements Runnable {
			public synchronized void run () {
				try {
					wait();
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}
		
		Block b = new Block();
        javax.swing.SwingUtilities.invokeLater(b);
		
		boolean success = support.canClose();
		
		synchronized (b) { 
			b.notifyAll();
		}
		
		assertFalse("Support cannot close as we cannot ask the user", success);
		
		if (ErrManager.messages.indexOf("InterruptedException") == -1) {
			fail("InterruptedException exception should be reported: " + ErrManager.messages);
		}
    }

	
    public void testBlockingAWTForFiveSecIsOk() throws Exception {
        StyledDocument doc = support.openDocument ();
		doc.insertString(0, "Ble", null);
		
		assertTrue("Modified", support.isModified());
			
		class Block implements Runnable {
			public synchronized void run () {
				try {
					wait(5000);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}
		
		Block b = new Block();
        javax.swing.SwingUtilities.invokeLater(b);
		
		boolean success = support.canClose();
		
		synchronized (b) { 
			b.notifyAll();
		}
		
		assertTrue("Ok, we managed to ask the question", success);
		
		if (ErrManager.messages.length() > 0) {
			fail("No messages should be reported: " + ErrManager.messages);
		}
    }
	
    //
    // Implementation of the CloneableEditorSupport.Env
    //
    
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propL.add (l);
    }    
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propL.remove (l);
    }
    
    public synchronized void addVetoableChangeListener(java.beans.VetoableChangeListener l) {
        assertNull ("This is the first veto listener", vetoL);
        vetoL = l;
    }
    public void removeVetoableChangeListener(java.beans.VetoableChangeListener l) {
        assertEquals ("Removing the right veto one", vetoL, l);
        vetoL = null;
    }
    
    public org.openide.windows.CloneableOpenSupport findCloneableOpenSupport() {
        return support;
    }
    
    public String getMimeType() {
        return "text/plain";
    }
    
    public java.util.Date getTime() {
        return date;
    }
    
    public java.io.InputStream inputStream() throws java.io.IOException {
        return new java.io.ByteArrayInputStream (content.getBytes ());
    }
    public java.io.OutputStream outputStream() throws java.io.IOException {
        class ContentStream extends java.io.ByteArrayOutputStream {
            public void close () throws java.io.IOException {
                super.close ();
                content = new String (toByteArray ());
            }
        }
        
        return new ContentStream ();
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public boolean isModified() {
        return modified;
    }

    public void markModified() throws java.io.IOException {
        modified = true;
    }
    
    public void unmarkModified() {
        modified = false;
    }

    /** Implementation of the CES */
    private final class CES extends CloneableEditorSupport {
        
        public CES (Env env, org.openide.util.Lookup l) {
            super (env, l);
        }
        
        protected String messageName() {
            return "Name";
        }
        
        protected String messageOpened() {
            return "Opened";
        }
        
        protected String messageOpening() {
            return "Opening";
        }
        
        protected String messageSave() {
            return "Save";
        }
        
        protected String messageToolTip() {
            return "ToolTip";
        }        
        
        protected EditorKit createEditorKit () {
            return new NbLikeEditorKit ();
        }
    } // end of CES
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        public Lkp() {
            this(new org.openide.util.lookup.InstanceContent());
        }
        
        private Lkp(org.openide.util.lookup.InstanceContent ic) {
            super(ic);
			ic.add(new DD());
			ic.add(new ErrManager());
        }
    }
    
    /** Our own dialog displayer.
     */
    private static final class DD extends org.openide.DialogDisplayer {
        public static Object[] options;
        public static Object toReturn;
        public static boolean disableTest;
        
        public java.awt.Dialog createDialog(org.openide.DialogDescriptor descriptor) {
            throw new IllegalStateException ("Not implemented");
        }
        
        public Object notify(org.openide.NotifyDescriptor descriptor) {
			return descriptor.getOptions()[0];
        }
        
    } // end of DD
    private static final class ErrManager extends org.openide.ErrorManager {
        static final StringBuffer messages = new StringBuffer();
        static int nOfMessages;
        static final String DELIMITER = ": ";
        static final String WARNING_MESSAGE_START = WARNING + DELIMITER;
        /** setup in setUp */
        static java.io.PrintStream log = System.err;
        
        private String prefix;
        
        public ErrManager () {
            prefix = "";
        }
        
        private ErrManager (String pr) {
            this.prefix = pr;
        }
        
        static void resetMessages() {
            messages.delete(0, ErrManager.messages.length());
            nOfMessages = 0;
        }
        
        public void log(int severity, String s) {
		}
		
		
        private void logImpl(int severity, String s) {
            synchronized (ErrManager.messages) {
                nOfMessages++;
                messages.append('['); log.print ('[');
                messages.append(prefix); log.print (prefix);
                messages.append("] - "); log.print ("] - ");
                messages.append(s); log.println (s);
                messages.append('\n'); 
            }
        }
        
        public Throwable annotate(Throwable t, int severity,
                String message, String localizedMessage,
                Throwable stackTrace, java.util.Date date) {
            return t;
        }
        
        public Throwable attachAnnotations(Throwable t, Annotation[] arr) {
            return t;
        }
        
        public org.openide.ErrorManager.Annotation[] findAnnotations(Throwable t) {
            return null;
        }
        
        public org.openide.ErrorManager getInstance(String name) {
            return new ErrManager (name);
        }
        
        public void notify(int severity, Throwable t) {
            StringWriter w = new StringWriter ();
            t.printStackTrace (new java.io.PrintWriter (w));
            logImpl (severity, w.toString ());
        }

		
		
		public boolean isNotifiable(int severity) {
			return true;
		}

		/** no logging */
		public boolean isLoggable(int severity) {
			return false;
		}
    } // end of ErrManager
}
