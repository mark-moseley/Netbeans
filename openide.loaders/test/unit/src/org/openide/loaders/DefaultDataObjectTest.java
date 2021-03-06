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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.openide.loaders;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.util.Enumeration;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;
import junit.framework.TestCase;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.text.DataEditorSupport;
import org.openide.util.Enumerations;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class DefaultDataObjectTest extends NbTestCase {
    private FileSystem lfs;
    private DataObject obj;
    
    public DefaultDataObjectTest(String testName) {
        super(testName);
    }            
    
    @Override
    protected void setUp() throws Exception {
        MockServices.setServices(Pool.class);
        
        clearWorkDir();

        String fsstruct [] = new String [] {
            "AA/a.test"
        };
        

        lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);
        Repository.getDefault().addFileSystem(lfs);

        FileObject fo = lfs.findResource("AA/a.test");
        assertNotNull("file not found", fo);
        obj = DataObject.find(fo);

        assertEquals("The right class", obj.getClass(), DefaultDataObject.class);

        assertFalse("Designed to run outside of AWT", SwingUtilities.isEventDispatchThread());
        
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testRenameName() throws Exception {
        Node node = obj.getNodeDelegate();
        
        class L extends NodeAdapter implements Runnable, VetoableChangeListener {
            StyledDocument doc;
            @Override
            public void nodeDestroyed(NodeEvent ev) {
                assertEquals(1, JspLoader.cnt);
                try {
                    DataObject nobj = DataObject.find(obj.getPrimaryFile());
                    assertEquals(JspLoader.class, nobj.getLoader().getClass());
                    EditorCookie ec = nobj.getLookup().lookup(EditorCookie.class);
                    assertNotNull("Cookie found", ec);
                    doc =ec.openDocument();
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }

            public void run() {
                try {
                    obj.rename("x.jsp");
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }

            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            }
        }
        L listener = new L();
//        node.addNodeListener(listener);
        JspLoader.nodeListener = listener;
        obj.addVetoableChangeListener(listener);
        
        SwingUtilities.invokeAndWait(listener);
        assertEquals("One object created", 1, JspLoader.cnt);
        
        DataObject nobj = DataObject.find(obj.getPrimaryFile());
        assertEquals(JspLoader.class, nobj.getLoader().getClass());
        
        assertFalse("Invalidated", obj.isValid());
        
        assertNotNull("Document can be created", listener.doc);
    }

    public static final class Pool extends DataLoaderPool {

        @Override
        protected Enumeration<? extends DataLoader> loaders() {
            return Enumerations.singleton(JspLoader.getLoader(JspLoader.class));
        }
        
    }

    public static final class JspLoader extends UniFileLoader {
        
        static int cnt; 
        static NodeListener nodeListener;
        
        public JspLoader() {
            super(MultiDataObject.class.getName());
        }

        @Override
        protected void initialize() {
            super.initialize();
            
            getExtensions().addExtension("jsp");
        }
        
        @Override
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            MultiDataObject obj = new MultiDataObject(primaryFile, this);
            cnt++;
            obj.getCookieSet().assign(EditorCookie.class, DataEditorSupport.create(obj, obj.getPrimaryEntry(), obj.getCookieSet()));
            
            nodeListener.nodeDestroyed(null);
            
            return obj;
        }
        
    }
}
