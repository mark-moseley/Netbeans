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


import java.awt.Dialog;
import java.io.IOException;
import javax.swing.JEditorPane;
import junit.framework.*;
import org.netbeans.junit.*;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.UserCancelException;
import org.openide.util.lookup.*;
import org.openide.windows.CloneableTopComponent;


/** Testing different features of CloneableEditorSupport
 *
 * @author Jaroslav Tulach
 */
public class CloneableEditorSupportPaneTest extends NbTestCase implements CloneableEditorSupport.Env {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    /** the support to work with */
    private CloneableEditorSupport support;
    private CloneableEditorSupport support2;
    /** the content of lookup of support */
    private InstanceContent ic;

    
    // Env variables
    private String content = "";
    private boolean valid = true;
    private boolean modified = false;
    /** if not null contains message why this document cannot be modified */
    private String cannotBeModified;
    private java.util.Date date = new java.util.Date ();
    private java.util.List/*<java.beans.PropertyChangeListener>*/ propL = new java.util.ArrayList ();
    private java.beans.VetoableChangeListener vetoL;

    
    public CloneableEditorSupportPaneTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(CloneableEditorSupportPaneTest.class);
        
        return suite;
    }
    

    protected void setUp () {
        ic = new InstanceContent ();
        support = new CES (this, new AbstractLookup (ic));
        support2 = new CES2(this, new AbstractLookup(new InstanceContent ()));
    }
    
    public void testGetOpenedPanes () throws Exception {
        content = "Ahoj\nMyDoc";
        javax.swing.text.Document doc = support.openDocument ();
        support.open();
        Line line = support.getLineSet().getCurrent(0);
        line.show(Line.SHOW_SHOW);
        JEditorPane[] panes = support.getOpenedPanes();
        assertNotNull(panes);
        assertEquals(1, panes.length);
        assertNotNull(instance);
        assertTrue(instance.activated);
                
    }
  
     public void testGetOpenedPanes2ForSeparatePane() throws Exception {
        content = "Ahoj\nMyDoc";
        javax.swing.text.Document doc = support2.openDocument ();
        support2.open();
        Line line = support2.getLineSet().getCurrent(0);
        line.show(Line.SHOW_SHOW);
        JEditorPane[] panes = support2.getOpenedPanes();
        assertNotNull(panes);
        assertEquals(1, panes.length);
        assertNotNull(instance2);
                
    }

         
    public void testDocumentSaveCancelledByUser() throws Exception {
        //register DialogDisplayer which "pushes" Yes option in the document save dialog
        MockServices.setServices(DD.class);
        
        content = "Ahoj\nMyDoc";
        CloneableEditorSupport sup = new CES3 (this, new AbstractLookup(new InstanceContent ()));
        javax.swing.text.Document doc = sup.openDocument ();
        
        //modify the document
        doc.insertString(0, "Kuk", null);
        
        //open the document
        sup.open();
        Line line = sup.getLineSet().getCurrent(0);
        line.show(Line.SHOW_SHOW);
        
        //check document opened
        assertTrue(sup.isDocumentLoaded());
        
        //close the document, this should invoke the save dialog, YES option will be choosen.
        sup.close();
        
        //the CES3 implementation of saveDocument() throws UserCancelException so the file is not saved
        
        //document still opened
        assertTrue(sup.isDocumentLoaded());
        
    }
    
    public void testCreateCloneableTopComponent() throws Exception {
        CloneableTopComponent comp = support.createCloneableTopComponent();
        assertNotNull(comp);
        assertEquals(MyPane.class, comp.getClass());
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
//        assertNull ("This is the first veto listener", vetoL);
        vetoL = l;
    }
    public void removeVetoableChangeListener(java.beans.VetoableChangeListener l) {
//        assertEquals ("Removing the right veto one", vetoL, l);
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
        if (cannotBeModified != null) {
            IOException e = new IOException ();
            Exceptions.attachLocalizedMessage(e, cannotBeModified);
            throw e;
        }
        
        modified = true;
    }
    
    public void unmarkModified() {
        modified = false;
    }

    protected boolean runInEQ() {
        return true;
    }
    
    /** Implementation of the CES */
    private static class CES extends CloneableEditorSupport {
        public CES (Env env, Lookup l) {
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
        
        protected org.openide.text.CloneableEditorSupport.Pane createPane() {
            instance = new MyPane();
            return instance;
        }
        
    }
    
    private static MyPane instance;
    
    private static final class MyPane extends CloneableTopComponent implements CloneableEditorSupport.Pane {
        
        private CloneableTopComponent tc;
        private JEditorPane pane;
        
        MyPane() {
            pane = new JEditorPane();
            
        }
        
        public org.openide.windows.CloneableTopComponent getComponent() {
            return this;
        }
        
        public javax.swing.JEditorPane getEditorPane() {
            return pane;
        }
        
        public void updateName() {
        }
        
        public boolean activated = false;
        public void requestActive() {
            super.requestActive();
            activated = true;
        }
        
       /**
         * callback for the Pane implementation to adjust itself to the openAt() request.
         */
        public void ensureVisible() {
            open();
            requestVisible();
        }        
    }
    
    
//-------------------------------------------------------------------------------
//-------------------------------------------------------------------------------
    
    private static class CES2 extends CES {
        public CES2 (Env env, Lookup l) {
            super (env, l);
        }
        
        protected org.openide.text.CloneableEditorSupport.Pane createPane() {
            instance2 = new MyPaneNonNonTC();
            return instance2;
        }
    }
    
    private static class CES3 extends CES {
        public CES3 (Env env, Lookup l) {
            super (env, l);
        }

        @Override
        public void saveDocument() throws IOException {
            throw new UserCancelException();
        }
    }
    
    public static class DD extends org.openide.DialogDisplayer {

        public java.awt.Dialog createDialog(org.openide.DialogDescriptor descriptor) {
            throw new IllegalStateException ("Not implemented");
        }
        
        public Object notify(org.openide.NotifyDescriptor descriptor) {
            return descriptor.getOptions()[0];
        }
        
    }
    
    private static MyPaneNonNonTC instance2;
    
    
    private static final class MyPaneNonNonTC implements CloneableEditorSupport.Pane {
        
        private CloneableTopComponent tc;
        private JEditorPane pane;
        
        MyPaneNonNonTC() {
            pane = new JEditorPane();
            tc = new TC();
            
        }
        
        public org.openide.windows.CloneableTopComponent getComponent() {
            return tc;
        }
        
        public javax.swing.JEditorPane getEditorPane() {
            return pane;
        }
        
        public void updateName() {
        }
        
       public void ensureVisible() {
            tc.open();
            tc.requestVisible();
        }                
        
    }
    
    private static class TC extends CloneableTopComponent {
        
        
        public void requestActive() {
            super.requestActive();
        }

    }
    
}
