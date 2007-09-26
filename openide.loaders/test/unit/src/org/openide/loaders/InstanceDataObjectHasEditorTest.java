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

import java.awt.Button;
import java.util.Date;
import junit.framework.*;
import java.io.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.openide.*;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.*;
import org.openide.modules.ModuleInfo;
import org.openide.util.*;
import org.openide.util.Utilities;
import org.openide.util.actions.*;
import org.openide.nodes.*;
import java.util.List;

/**
 *
 * @author Jaroslav Tulach
 */
public class InstanceDataObjectHasEditorTest extends org.netbeans.junit.NbTestCase {
    private FileObject fo;
    
    
    public InstanceDataObjectHasEditorTest (String testName) {
        super (testName);
    }
    
    protected void setUp () throws java.lang.Exception {
        clearWorkDir ();

        // initialize modules
        Lookup.getDefault().lookup(ModuleInfo.class);
    }

    public void testSettingsFileOnSFSShouldHaveEditor () throws Exception {
        FileObject set = createSettings (Repository.getDefault ().getDefaultFileSystem ().getRoot (), "x.settings");
        DataObject obj = DataObject.find (set);
        assertEquals (InstanceDataObject.class, obj.getClass ());
        assertNull ("It does not have edit cookie", obj.getCookie (EditCookie.class));
        assertNull ("It does not have open cookie", obj.getCookie (OpenCookie.class));
        assertNull ("It does not have editor cookie", obj.getCookie (EditorCookie.class));
        
        Object o = obj.getNodeDelegate ().getPreferredAction ();
        Class c = o == null ? Object.class : o.getClass ();
        
        if (c == org.openide.actions.OpenAction.class) {
            fail ("Default actions should not be open on SFS: " + o);
        }
    }
    
    public void testSettingsFileOnNonSFSShouldHaveEditor () throws Exception {
        clearWorkDir ();
        LocalFileSystem lfs = new LocalFileSystem ();
        lfs.setRootDirectory (getWorkDir ());
        
        FileObject set = createSettings (lfs.getRoot (), "x.settings");
        DataObject obj = DataObject.find (set);
        assertEquals (InstanceDataObject.class, obj.getClass ());
        assertNotNull ("It has edit cookie", obj.getCookie (EditCookie.class));
        assertNotNull ("It has open cookie", obj.getCookie (OpenCookie.class));
        assertNotNull ("It has editor cookie", obj.getCookie (EditorCookie.class));

        Object o = obj.getNodeDelegate ().getPreferredAction ();
        Class c = o == null ? Object.class : o.getClass ();
        
        assertEquals ("Default actions should be open on non-SFS", org.openide.actions.OpenAction.class, c);
    }
    
    public void testSettingsFileOnNonSFSAfterCopyShouldHaveEditor () throws Exception {
        clearWorkDir ();
        LocalFileSystem lfs = new LocalFileSystem ();
        lfs.setRootDirectory (getWorkDir ());
        
        FileObject set = createSettings (lfs.getRoot (), "x.settings");
        DataObject old = DataObject.find (set);
        Date d = set.lastModified();
        
        /* This code would work only with core/settings, so moving the test there
        InstanceCookie ic = (InstanceCookie)old.getCookie(InstanceCookie.class);
        assertNotNull ("The cookie is there", ic);
        Object instance = ic.instanceCreate();
        assertNotNull ("It produces a result", instance);
        assertEquals ("It is Button", Button.class, instance.getClass ());
         */
        
        FileObject tgt = FileUtil.createFolder(lfs.getRoot (), "moved");
        DataFolder fld = DataFolder.findFolder (tgt);
        
        DataObject obj = old.copy (fld);
        
        assertEquals ("No change in modifications", d, set.lastModified());
        assertEquals ("The same name", obj.getPrimaryFile().getNameExt (), set.getNameExt());
        
        assertEquals (InstanceDataObject.class, obj.getClass ());
        assertNotNull ("It has edit cookie", obj.getCookie (EditCookie.class));
        assertNotNull ("It has open cookie", obj.getCookie (OpenCookie.class));
        assertNotNull ("It has editor cookie", obj.getCookie (EditorCookie.class));

        Object o = obj.getNodeDelegate ().getPreferredAction ();
        Class c = o == null ? Object.class : o.getClass ();
        
        assertEquals ("Default actions should be open on non-SFS", org.openide.actions.OpenAction.class, c);
    }

    private FileObject createSettings (FileObject root, String name) throws IOException {
        FileObject set = FileUtil.createData (root, name);

        FileLock lock = set.lock ();
        PrintStream os = new PrintStream (set.getOutputStream (lock));
        
        os.println ("<?xml version=\"1.0\"?>");
        os.println ("<!DOCTYPE settings PUBLIC \"-//NetBeans//DTD Session settings 1.0//EN\" \"http://www.netbeans.org/dtds/sessionsettings-1_0.dtd\">");
        os.println ("<settings version=\"1.0\">");
//        os.println ("<module name=\"org.apache.tools.ant.module/3\" spec=\"3.15\"/>");
        os.println ("<instanceof class=\"java.io.Serializable\"/>");
        os.println ("<instanceof class=\"java.lang.Object\"/>");
        os.println ("<instanceof class=\"java.awt.Component\"/>");
        os.println ("<instance class=\"java.awt.Button\"/>");
        os.println ("</settings>");
        
        os.close ();
        lock.releaseLock();
        return set;
    }
}
