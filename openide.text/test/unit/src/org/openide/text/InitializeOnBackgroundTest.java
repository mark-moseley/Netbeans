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


package org.openide.text;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.*;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/** Checks that the default impl of Documents UndoRedo really locks
 * the document first on all of its methods.
 *
 * @author  Jarda Tulach
 */
public class InitializeOnBackgroundTest extends NbTestCase implements CloneableEditorSupport.Env {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    /** the method of manager that we are testing */
    
    private CES support;
    // Env variables
    private String content = "";
    private boolean valid = true;
    private boolean modified = false;
    private java.util.Date date = new java.util.Date ();
    private java.util.List/*<java.beans.PropertyChangeListener>*/ propL = new java.util.ArrayList ();
    private java.beans.VetoableChangeListener vetoL;
    
    
    /** Creates new UndoRedoTest */
    public InitializeOnBackgroundTest(String m) {
        super(m);
    }

    @Override
    protected boolean runInEQ() {
        return getName().contains("AWT");
    }

    @Override
    protected void setUp() throws Exception {
        support = new CES(this, Lookup.EMPTY);
    }
    
    

    public void testInitializeOnBackground() throws Exception {
        support.open();
        
        class R implements Runnable {
            JEditorPane p;
            public void run() {
                p = support.getOpenedPanes()[0];
            }
        }
        R r = new R();
        SwingUtilities.invokeAndWait(r);
        assertNotNull(r.p);
        
        if (r.p.getEditorKit() instanceof NbLikeEditorKit) {
            NbLikeEditorKit nb = (NbLikeEditorKit)r.p.getEditorKit();
            assertNotNull("call method called", nb.callThread);
            if (nb.callThread.getName().contains("AWT")) {
                fail("wrong thread: " + nb.callThread);
            }
        } else {
            fail("Should use NbLikeEditorKit: " + r.p.getEditorKit());
        }
    }
    
    public void testInitializeOnBackgroundInAWT() throws Exception {
        assertTrue("Running in AWT", SwingUtilities.isEventDispatchThread());
        
        support.open();
        
        class R implements Runnable {
            JEditorPane p;
            public void run() {
                p = support.getOpenedPanes()[0];
            }
        }
        R r = new R();
        r.run();
        assertNotNull(r.p);
        
        if (r.p.getEditorKit() instanceof NbLikeEditorKit) {
            NbLikeEditorKit nb = (NbLikeEditorKit)r.p.getEditorKit();
            assertNotNull("call method called", nb.callThread);
            if (nb.callThread.getName().contains("AWT")) {
                fail("wrong thread: " + nb.callThread);
            }
        } else {
            fail("Should use NbLikeEditorKit: " + r.p.getEditorKit());
        }
    }

    public void testInitializeAndBlockInAWT() throws Exception {
        assertTrue("Running in AWT", SwingUtilities.isEventDispatchThread());
        
        class R implements PropertyChangeListener {
            JEditorPane p;
            public void run() {
                p = support.getOpenedPanes()[0];
            }

            public void propertyChange(PropertyChangeEvent evt) {
                if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
                    run();
                }
            }
        }
        R r = new R();
        WindowManager.getDefault().getRegistry().addPropertyChangeListener(r);
        
        final Object LOCK = new JPanel().getTreeLock();
        synchronized (LOCK) {
            support.open();
            assertNotNull(r.p);
        }
        
        if (r.p.getEditorKit() instanceof NbLikeEditorKit) {
            NbLikeEditorKit nb = (NbLikeEditorKit)r.p.getEditorKit();
            assertNotNull("call method called", nb.callThread);
            if (nb.callThread.getName().contains("AWT")) {
                fail("wrong thread: " + nb.callThread);
            }
        } else {
            fail("Should use NbLikeEditorKit: " + r.p.getEditorKit());
        }
    }
    
    
    public void testQueryDocumentInAWT() throws Exception {
        assertTrue("Running in AWT", SwingUtilities.isEventDispatchThread());
        
        class R implements PropertyChangeListener {
            JEditorPane p;
            Document doc;
            
            public void propertyChange(PropertyChangeEvent evt) {
                if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
                   CloneableEditor ed = (CloneableEditor)WindowManager.getDefault().getRegistry().getActivated();
                   p = ed.getEditorPane();
                   doc = ed.getEditorPane().getDocument();
                }
            }
        }
        R r = new R();
        WindowManager.getDefault().getRegistry().addPropertyChangeListener(r);
        
        final Object LOCK = new JPanel().getTreeLock();
        synchronized (LOCK) {
            support.open();
            assertNotNull(r.p);
        }
        
        if (r.p.getEditorKit() instanceof NbLikeEditorKit) {
            NbLikeEditorKit nb = (NbLikeEditorKit)r.p.getEditorKit();
            assertNotNull("call method called", nb.callThread);
            if (nb.callThread.getName().contains("AWT")) {
                fail("wrong thread: " + nb.callThread);
            }
        } else {
            fail("Should use NbLikeEditorKit: " + r.p.getEditorKit());
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

        protected javax.swing.text.EditorKit createEditorKit() {
            return new K();
        }
    } // end of CES

    private static final class K extends NbLikeEditorKit {
/* Uncomment this code to simulate the deadlock with mimelookup that uses two locks
        @Override
        public synchronized Document createDefaultDocument() {
            return super.createDefaultDocument();
        }

        @Override
        public synchronized Void call() throws Exception {
            synchronized (new JPanel().getTreeLock()) {
            }
            return super.call();
        }
 */
    }
}
