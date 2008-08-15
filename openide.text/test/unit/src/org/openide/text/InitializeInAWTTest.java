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


import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.openide.awt.UndoRedo;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/** Checks that the default impl of Documents UndoRedo really locks
 * the document first on all of its methods.
 *
 * @author  Jarda Tulach
 */
public class InitializeInAWTTest extends NbTestCase implements CloneableEditorSupport.Env {
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
    private java.util.List<java.beans.PropertyChangeListener> propL = new java.util.ArrayList<java.beans.PropertyChangeListener>();
    private java.beans.VetoableChangeListener vetoL;
    private FindActionCheck find;
    
    
    /** Creates new UndoRedoTest */
    public InitializeInAWTTest(String m) {
        super(m);
    }

    @Override
    protected boolean runInEQ() {
        return getName().contains("AWT");
    }

    @Override
    protected void setUp() throws Exception {
        support = new CES(this, Lookup.EMPTY);
        find = new FindActionCheck();
    }

    @Override
    protected void tearDown() throws Exception {
        find.assertAction();
    }
    

    public void testInitializeOnBackground() throws Exception {
        assertFalse("Running out of AWT", SwingUtilities.isEventDispatchThread());
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

        assertKit(r.p.getEditorKit());
    }
    
    private static void assertKit(EditorKit kit) {
        if (kit instanceof NbLikeEditorKit) {
            NbLikeEditorKit nb = (NbLikeEditorKit) kit;
            assertNull("the kit's call mehtod is not called due to backward compat", nb.callThread);
        } else {
            fail("Should use NbLikeEditorKit: " + kit);
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

        assertKit(r.p.getEditorKit());
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
        
        assertKit(r.p.getEditorKit());
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
        
        assertKit(r.p.getEditorKit());
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

        @Override
        protected javax.swing.text.EditorKit createEditorKit() {
            return new K();
        }

        @Override
        protected CloneableEditor createCloneableEditor() {
            CloneableEditor ed = super.createCloneableEditor();
            ed.putClientProperty("oldInitialize", Boolean.TRUE);
            return ed;
        }
        
        
    } // end of CES

    private static final class MyAction extends CallbackSystemAction {
        @Override
        public String getName() {
            return "MyAction";
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

    }

    static final class K extends NbLikeEditorKit  {
        @Override
        public javax.swing.text.Document createDefaultDocument() {
            return new EdDoc();
        }

        @Override
        public Action[] getActions() {
            List<Action> arr = new ArrayList<Action>(Arrays.asList(super.getActions()));
            Action a = MyAction.get(MyAction.class);
            arr.add(a);
            return arr.toArray(new Action[0]);
        }

        private final class EdDoc extends Doc implements NbDocument.CustomEditor {
            public Component createEditor(JEditorPane j) {
                j.getActionMap().put("MyAction", MyAction.get(MyAction.class));
                return j;
            }

        }

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

    static final class FindActionCheck implements LookupListener {
        private Result<ActionMap> res;

        private Action last;

        FindActionCheck() {
            res = Utilities.actionsGlobalContext().lookupResult(ActionMap.class);
            resultChanged(null);
            res.addLookupListener(this);

            TopComponent tc =new TopComponent();
            tc.open();
            tc.requestActive();

            assertEquals("No action provided now", null, last);
        }

        public void resultChanged(LookupEvent ev) {
            if (!res.allItems().isEmpty()) {
                ActionMap m = res.allInstances().iterator().next();
                last = m.get("MyAction");
            } else {
                last = null;
            }
        }

        public void assertAction() {
            res.removeLookupListener(this);
            assertNotNull("Result found", last);
        }
    }
}
