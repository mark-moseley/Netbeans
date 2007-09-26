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

package org.openide.loaders;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.*;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.text.Document;
import org.netbeans.junit.*;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;


/** Simulates the deadlock from issue 60917
 * @author Jaroslav Tulach
 */
public class Sample60M7ProblemWithGetDataObjectTest extends NbTestCase {
    
    public Sample60M7ProblemWithGetDataObjectTest(String name) {
        super(name);
    }
    
	
    protected void setUp() throws Exception {
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
        assertEquals(Lkp.class, Lookup.getDefault().getClass());
    }
    
    public void testHasDataObjectInItsLookup() throws Exception {
        FileObject sample = FileUtil.createData(FileUtil.createMemoryFileSystem().getRoot(), "sample/S.sample");
        DataObject obj = DataObject.find(sample);
        assertEquals(Sample60M6DataLoader.class, obj.getLoader().getClass());
        
        assertEquals("Object is in its own node's lookup", obj, obj.getNodeDelegate().getLookup().lookup(DataObject.class));
        assertEquals("Object is in its own lookup", obj, obj.getLookup().lookup(DataObject.class));
        assertEquals("Object is own node's cookie", obj, obj.getNodeDelegate().getCookie(DataObject.class));
        assertEquals("Object is own cookie", obj, obj.getCookie(DataObject.class));
    }
    
    static class Sample60M6DataObject extends MultiDataObject
    implements Lookup.Provider {

        public Sample60M6DataObject(FileObject pf, Sample60M6DataLoader loader) throws DataObjectExistsException, IOException {
            super(pf, loader);
            CookieSet cookies = getCookieSet();
            cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
        }

        protected Node createNodeDelegate() {
            return new Sample60M6DataNode(this, getLookup());
        }

        public Lookup getLookup() {
            return getCookieSet().getLookup();
        }
    }

    public static class Sample60M6DataLoader extends UniFileLoader {

        public static final String REQUIRED_MIME = "text/x-sample";

        private static final long serialVersionUID = 1L;

        public Sample60M6DataLoader() {
            super("org.openide.loaders.Sample60M6DataObject");
        }

        protected String defaultDisplayName() {
            return NbBundle.getMessage(Sample60M6DataLoader.class, "LBL_Sample60M6_loader_name");
        }

        protected void initialize() {
            super.initialize();
            getExtensions().addExtension("sample");
        }

        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new Sample60M6DataObject(primaryFile, this);
        }

        protected String actionsContext() {
            return "Loaders/" + REQUIRED_MIME + "/Actions";
        }

    }
    static class Sample60M6DataNode extends DataNode {
        public Sample60M6DataNode(Sample60M6DataObject obj) {
            super(obj, Children.LEAF);
            //        setIconBaseWithExtension(IMAGE_ICON_BASE);
        }
        Sample60M6DataNode(Sample60M6DataObject obj, Lookup lookup) {
            super(obj, Children.LEAF, lookup);
            //        setIconBaseWithExtension(IMAGE_ICON_BASE);
        }

        //    /** Creates a property sheet. */
        //    protected Sheet createSheet() {
        //        Sheet s = super.createSheet();
        //        Sheet.Set ss = s.get(Sheet.PROPERTIES);
        //        if (ss == null) {
        //            ss = Sheet.createPropertiesSet();
        //            s.put(ss);
        //        }
        //        // TODO add some relevant properties: ss.put(...)
        //        return s;
        //    }

    }
    
    public static final class Lkp extends AbstractLookup {
        public Lkp() {
            this(new InstanceContent());
        }
        private Lkp(InstanceContent ic) {
            super(ic);
            ic.add(Sample60M6DataLoader.getLoader(Sample60M6DataLoader.class));
        }
    }
}
