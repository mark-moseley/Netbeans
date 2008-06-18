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
package org.netbeans.modules.favorites.templates;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.netbeans.junit.*;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.test.MockLookup;

/**
 * Tests creating/renaming/removing templates via TemplatesPanel.
 *
 * @author Jiri Rechtacek
 */
public class TemplatesPanelTest extends NbTestCase {
    File popural;
    FileObject templateFolder;
    DataFolder f;

    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public TemplatesPanelTest(String s) {
        super(s);
    }
    
    protected void setUp () {
        MockLookup.setInstances(new Repository(FileUtil.createMemoryFileSystem()));
        try {
            templateFolder = Repository.getDefault ().getDefaultFileSystem ().getRoot ().createFolder ("TestTemplates");
        } catch (IOException ioe) {
            fail (ioe.getMessage ());
        }
        assertNotNull ("TestTemplates folder exists on SFS", templateFolder);
        try {
            popural = getWorkDir ().createTempFile ("popural", "java");
        } catch (IOException ioe) {
            fail (ioe.getMessage ());
        }
        assertTrue ("popural.tmp exists", popural.exists ());
        
        f = DataFolder.findFolder (templateFolder);
        
        assertNotNull ("DataFolder found for FO " + templateFolder, f);
        
    }
    
    protected void tearDown() {
        try {
            FileLock l = templateFolder.lock ();
            templateFolder.delete (l);
            l.releaseLock ();
        } catch (IOException ioe) {
            fail (ioe.getMessage ());
        }
    }
    
    public void testNewTemplateFromFile () throws Exception {
        DataObject dobj = TemplatesPanel.createTemplateFromFile (popural, f);
        assertNotNull ("New DataObject found.", dobj);
        assertTrue ("Is template.", dobj.isTemplate ());
        assertEquals ("Template is in the preffered folder.", f, dobj.getFolder ());
    }
    
    public void testTwiceNewFromTemplate () throws Exception {
        testNewTemplateFromFile ();
        testNewTemplateFromFile ();
    }
    
    public void testDuplicateTemplate () {
        DataObject dobj = TemplatesPanel.createTemplateFromFile (popural, f);
        DataObject dupl = TemplatesPanel.createDuplicateFromNode (dobj.getNodeDelegate ());
        assertNotNull ("Duplicate DataObject found.", dobj);
        assertTrue ("Duplicate is template.", dobj.isTemplate ());
        assertEquals ("Template is in same folder as original.", dobj.getFolder (), dupl.getFolder ());
        assertTrue ("Name is derived from original.", dupl.getNodeDelegate ().getName ().startsWith (dobj.getNodeDelegate ().getName ()));
    }
    public void testIgnoresSimplefolders() throws Exception {
        FileObject root = Repository.getDefault ().getDefaultFileSystem ().getRoot ();
        FileObject fo = FileUtil.createFolder(root, "Templates/SimpleFolder");
        fo.setAttribute("simple", Boolean.FALSE);
        Node n = TemplatesPanel.getTemplateRootNode();
        Node[] arr = n.getChildren().getNodes(true);
        assertEquals("Empty: " + Arrays.asList(arr), 0, arr.length);
    }
    public void testIgnoresSimpleNonFolders() throws Exception {
        FileObject root = Repository.getDefault ().getDefaultFileSystem ().getRoot ();
        FileObject fo = FileUtil.createData(root, "Templates/SimpleFolder.java");
        fo.setAttribute("simple", Boolean.FALSE);
        fo.setAttribute("template", Boolean.TRUE);
        Node n = TemplatesPanel.getTemplateRootNode();
        Node[] arr = n.getChildren().getNodes(true);
        assertEquals("Empty: " + Arrays.asList(arr), 0, arr.length);
    }
}
