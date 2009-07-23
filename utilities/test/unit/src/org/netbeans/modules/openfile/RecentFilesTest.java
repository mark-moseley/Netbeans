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

package org.netbeans.modules.openfile;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.modules.openfile.RecentFiles.HistoryItem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.Lookups;
import org.openide.windows.CloneableTopComponent;


/**
 * Tests for RecentFiles support, tests list modification policy.
 *
 * @author Dafe Simonek
 */
public class RecentFilesTest extends TestCase {
    
    public RecentFilesTest(String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(RecentFilesTest.class);
    }
    
    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testGetRecentFiles () throws Exception {
        System.out.println("Testing RecentFiles.getRecentFiles...");
        URL url = RecentFilesTest.class.getResource("resources/recent_files/");
        assertNotNull("url not found.", url);
        
        FileObject folder = URLMapper.findFileObject(url);
        FileObject[] files = folder.getChildren();
        List<EditorLikeTC> tcs = new ArrayList<EditorLikeTC>();
        
        RecentFiles.getPrefs().clear();
        RecentFiles.init();
        
        for (FileObject curFo : files) {
            EditorLikeTC curTC = new EditorLikeTC(curFo);
            tcs.add(0, curTC);
            curTC.open();
        }

        // close top components and check if they were added correctly to
        // recent files list
        for (EditorLikeTC curTC : tcs) {
            curTC.close();
        }
        int i = 0;
        List<HistoryItem> recentFiles = RecentFiles.getRecentFiles();
        assertTrue("Expected " + files.length + " recent files, got " + recentFiles.size(), files.length == recentFiles.size());
        for (FileObject fo : files) {
            assertEquals(RecentFiles.convertFile2URL(fo), recentFiles.get(i).getURL());
            i++;
        }

        // reopen first component again and check that it was removed from
        // recent files list
        tcs.get(0).open();
        recentFiles = RecentFiles.getRecentFiles();
        assertTrue(files.length == (recentFiles.size() + 1));
        
    }
    
    public void testPersistence () throws Exception {
        System.out.println("Testing perfistence of recent files history...");
        URL url = RecentFilesTest.class.getResource("resources/recent_files/");
        assertNotNull("url not found.", url);
        
        FileObject folder = URLMapper.findFileObject(url);
        FileObject[] files = folder.getChildren();
        
        RecentFiles.getPrefs().clear();
        
        // store, load and check for equality
        for (int i=0; i < files.length; i++) {
            FileObject file = files[i];
            RecentFiles.addFile(RecentFiles.convertFile2URL(file));
            Thread.sleep(100);
        }
        RecentFiles.store();
        List<HistoryItem> loaded = RecentFiles.load();
        assertTrue("Persistence failed, " + files.length + " stored items, " + loaded.size() + " loaded.", files.length == loaded.size());
        int i = files.length - 1;
        for (FileObject fileObject : files) {
            assertTrue("File #" + (i + 1) + " differs", 
                    fileObject.equals(RecentFiles.convertURL2File(loaded.get(i--).getURL())));
        }
    }
    
    public void test87252 () throws Exception {
        System.out.println("Testing fix for 87252...");
        URL url = RecentFilesTest.class.getResource("resources/recent_files/");
        assertNotNull("url not found.", url);
        
        FileObject folder = URLMapper.findFileObject(url);
        FileObject fo = folder.createData("ToBeDeleted.txt");
        
        RecentFiles.getPrefs().clear();
        RecentFiles.init();
        
        EditorLikeTC tc = new EditorLikeTC(fo);
        tc.open();
        tc.close();
        
        // delete file and check that recent files *doesn't* contain the file
        fo.delete();
        List<HistoryItem> recentFiles = RecentFiles.getRecentFiles();
        boolean contained = false;
        for (HistoryItem historyItem : recentFiles) {
            if (fo.equals(RecentFiles.convertURL2File(historyItem.getURL()))) {
                contained = true;
                break;
            }
        }
        assertFalse("Deleted file should not be in recent files", contained);
    }
    

    /** Special TopComponent subclass which imitates TopComponents used for documents, editors */
    private static class EditorLikeTC extends CloneableTopComponent {
        
        public EditorLikeTC (FileObject fo) throws Exception {
            DataObject dObj = DataObject.find(fo);
            associateLookup(Lookups.singleton(dObj));
        }
        
    }

}
