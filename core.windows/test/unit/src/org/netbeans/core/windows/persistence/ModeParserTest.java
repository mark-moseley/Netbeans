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

package org.netbeans.core.windows.persistence;

import java.awt.Frame;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.SplitConstraint;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/** Functionality tests for saving and loading Mode configuration data
 *
 * @author Marek Slama
 */
public class ModeParserTest extends NbTestCase {
    
    public ModeParserTest() {
        super("");
    }
    
    public ModeParserTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTest suite() {
        NbTestSuite suite = new NbTestSuite(ModeParserTest.class);
        return suite;
    }

    protected void setUp () throws Exception {
//        File localRoot;
//        try {
//            File tempFile = File.createTempFile("test", null);
//            File parent = tempFile.getParentFile();
//            localRoot = new File(parent, tempFile.getName().substring(0, tempFile.getName().length() - 4));
//            if (!localRoot.exists()) {
//                localRoot.mkdirs();
//            }
//            System.out.println("dir created=" + localRoot);
//        } catch (IOException exc) {
//            throw exc;
//        }
//        FileObject fo = FileUtil.toFileObject(localRoot);
//        PersistenceManager.getDefault().setRootLocalFolder(fo);
    }
    
    ////////////////////////////////
    //Testing VALID data
    ////////////////////////////////
    /** Test of loaded data
     */
    public void testLoadMode01 () throws Exception {
        System.out.println("");
        System.out.println("ModeParserTest.testLoadMode01 START");
        
        ModeParser modeParser = createModeParser("data/valid/Windows/Modes","mode01");
        
        ModeConfig modeCfg = modeParser.load();
        
        //Check loaded data
        assertNotNull("Could not load data.",modeCfg);
        
        InternalConfig internalCfg = modeParser.getInternalConfig();
        
        assertNotNull("Could not load internal data.",internalCfg);
        
        //Check internal data
        assertEquals("moduleCodeNameBase","org.netbeans.core.ui",internalCfg.moduleCodeNameBase);
        assertEquals("moduleCodeNameBase","1",internalCfg.moduleCodeNameRelease);
        assertEquals("moduleCodeNameBase","1.2",internalCfg.moduleSpecificationVersion);
        
        //Check data
        assertEquals("Mode state",Constants.MODE_STATE_JOINED,modeCfg.state);
        
        assertNotNull("Mode constraints are null",modeCfg.constraints);
        assertEquals("Mode constraints are not empty",0,modeCfg.constraints.length);
        
        assertEquals("Mode type",Constants.MODE_KIND_EDITOR,modeCfg.kind);
        
        assertNull("Mode bounds are not null",modeCfg.bounds);
        assertNull("Mode relative bounds are not null",modeCfg.relativeBounds);
        
        assertNotNull("Active TC is null",modeCfg.selectedTopComponentID);
        assertEquals("Active TC is not empty","",modeCfg.selectedTopComponentID);
        assertNotNull("Previous active TC is null",modeCfg.previousSelectedTopComponentID);
        assertEquals("Previous active TC is not empty","",modeCfg.previousSelectedTopComponentID);
        assertTrue("Permanent",modeCfg.permanent);
        
        System.out.println("ModeParserTest.testLoadMode01 FINISH");
    }
    
    /** Test of loaded data
     */
    public void testLoadMode02 () throws Exception {
        System.out.println("");
        System.out.println("ModeParserTest.testLoadMode02 START");
        
        ModeParser modeParser = createModeParser("data/valid/Windows/Modes","mode02");
        
        ModeConfig modeCfg = modeParser.load();
        
        //Check loaded data
        assertNotNull("Could not load data.", modeCfg);
        
        //Check data
        assertEquals("Mode state",Constants.MODE_STATE_JOINED,modeCfg.state);
        
        assertNotNull("Mode constraints are null",modeCfg.constraints);
        assertEquals("Mode constraints array has incorrect size",2,modeCfg.constraints.length);
        SplitConstraint item;
        item = modeCfg.constraints[0];
        assertEquals("Mode constraint 0 - orientation",Constants.VERTICAL,item.orientation);
        assertEquals("Mode constraint 0 - index",1,item.index);
        assertEquals("Mode constraint 0 - weight",0.3,item.splitWeight,0.0);
        
        item = modeCfg.constraints[1];
        assertEquals("Mode constraint 1 - orientation",Constants.HORIZONTAL,item.orientation);
        assertEquals("Mode constraint 1 - index",0,item.index);
        assertEquals("Mode constraint 1 - weight",0.5,item.splitWeight,0.0);
        
        assertNull("Mode bounds are not null",modeCfg.bounds);
        assertNull("Mode relative bounds are not null",modeCfg.relativeBounds);
        
        assertEquals("Mode type",Constants.MODE_KIND_VIEW,modeCfg.kind);
        assertEquals("Active TC","output",modeCfg.selectedTopComponentID);
        assertFalse("Permanent",modeCfg.permanent);
        
        System.out.println("ModeParserTest.testLoadMode02 FINISH");
    }
    
    /** Test of loaded data
     */
    public void testLoadMode03 () throws Exception {
        System.out.println("");
        System.out.println("ModeParserTest.testLoadMode03 START");
        
        ModeParser modeParser = createModeParser("data/valid/Windows/Modes","mode03");
        
        ModeConfig modeCfg = modeParser.load();
        
        //Check loaded data
        assertNotNull("Could not load data.", modeCfg);
        
        //Check data
        assertEquals("Mode state",Constants.MODE_STATE_SEPARATED,modeCfg.state);
        
        assertNotNull("Mode constraints are null",modeCfg.constraints);
        assertEquals("Mode constraints are not empty",0,modeCfg.constraints.length);
        
        assertNotNull("Mode bounds are null",modeCfg.bounds);
        
        assertEquals("Mode bounds x",0,modeCfg.bounds.x);
        assertEquals("Mode bounds y",0,modeCfg.bounds.y);
        assertEquals("Mode bounds width",400,modeCfg.bounds.width);
        assertEquals("Mode bounds height",200,modeCfg.bounds.height);
        
        assertNull("Mode relative bounds are not null",modeCfg.relativeBounds);
        
        assertEquals("Mode frame state",Frame.NORMAL,modeCfg.frameState);
        
        assertEquals("Active TC","output",modeCfg.selectedTopComponentID);
        assertTrue("Permanent",modeCfg.permanent);
        
        System.out.println("ModeParserTest.testLoadMode03 FINISH");
    }
    
    /** Test of loaded data
     */
    public void testLoadMode04 () throws Exception {
        System.out.println("");
        System.out.println("ModeParserTest.testLoadMode04 START");
        
        ModeParser modeParser = createModeParser("data/valid/Windows/Modes","mode04");
        
        ModeConfig modeCfg = modeParser.load();
        
        //Check loaded data
        assertNotNull("Could not load data.", modeCfg);
        
        //Check data
        assertEquals("Mode state",Constants.MODE_STATE_SEPARATED,modeCfg.state);
        
        assertNotNull("Mode constraints are null",modeCfg.constraints);
        assertEquals("Mode constraints are not empty",0,modeCfg.constraints.length);
        
        assertNull("Mode bounds are not null",modeCfg.bounds);
        
        assertNotNull("Mode relative bounds are null",modeCfg.relativeBounds);
        
        assertEquals("Mode relative bounds x",0,modeCfg.relativeBounds.x);
        assertEquals("Mode relative bounds y",0,modeCfg.relativeBounds.y);
        assertEquals("Mode relative bounds width",40,modeCfg.relativeBounds.width);
        assertEquals("Mode relative bounds height",20,modeCfg.relativeBounds.height);
        
        assertEquals("Mode frame state",Frame.MAXIMIZED_BOTH,modeCfg.frameState);
        
        assertEquals("Active TC","output",modeCfg.selectedTopComponentID);
        assertTrue("Permanent",modeCfg.permanent);
        
        System.out.println("ModeParserTest.testLoadMode04 FINISH");
    }

        /** Test of loaded data
     */
    public void testLoadMode05 () throws Exception {
        System.out.println("");
        System.out.println("ModeParserTest.testLoadMode05 START");
        
        ModeParser modeParser = createModeParser("data/valid/Windows/Modes","mode05");
        
        ModeConfig modeCfg = modeParser.load();
        
        //Check loaded data
        assertNotNull("Could not load data.", modeCfg);
        
        //Check data
        assertEquals("Mode kind",Constants.MODE_KIND_SLIDING,modeCfg.kind);
        assertEquals("Mode sliding side",Constants.LEFT, modeCfg.side);
//        assertEquals("Mode state",Constants.MODE_STATE_SEPARATED,modeCfg.state);
        
//        assertNotNull("Mode constraints are null",modeCfg.constraints);
//        assertEquals("Mode constraints are not empty",0,modeCfg.constraints.length);
        
//        assertNull("Mode bounds are not null",modeCfg.bounds);
        
//        assertNotNull("Mode relative bounds are null",modeCfg.relativeBounds);
        
//        assertEquals("Mode relative bounds x",0,modeCfg.relativeBounds.x);
//        assertEquals("Mode relative bounds y",0,modeCfg.relativeBounds.y);
//        assertEquals("Mode relative bounds width",40,modeCfg.relativeBounds.width);
//        assertEquals("Mode relative bounds height",20,modeCfg.relativeBounds.height);
        
//        assertEquals("Mode frame state",Frame.MAXIMIZED_BOTH,modeCfg.frameState);
        
        assertEquals("Active TC","output",modeCfg.selectedTopComponentID);
        assertTrue("Permanent",modeCfg.permanent);
        
        System.out.println("ModeParserTest.testLoadMode05 FINISH");
    }

        /** Test of sliding mode with pre-defined slide-in sizes
     */
    public void testLoadMode06 () throws Exception {
        System.out.println("");
        System.out.println("ModeParserTest.testLoadMode06 START");
        
        ModeParser modeParser = createModeParser("data/valid/Windows/Modes","mode06");
        
        ModeConfig modeCfg = modeParser.load();
        
        //Check loaded data
        assertNotNull("Could not load data.", modeCfg);
        
        //Check data
        assertEquals("Mode kind",Constants.MODE_KIND_SLIDING,modeCfg.kind);
        assertEquals("Mode sliding side",Constants.LEFT, modeCfg.side);
        
        assertEquals("Active TC","output",modeCfg.selectedTopComponentID);
        assertTrue("Permanent",modeCfg.permanent);
        
        Map<String,Integer> slideInSizes = modeCfg.slideInSizes;
        assertNotNull( slideInSizes );
        assertEquals( 2, slideInSizes.size() );
        assertEquals( Integer.valueOf(123), slideInSizes.get( "output" ) );
        assertEquals( Integer.valueOf(321), slideInSizes.get( "someOtherTopComponentId" ) );
        
        System.out.println("ModeParserTest.testLoadMode06 FINISH");
    }

    /** Test of a mode with with previously active tc 
     */
    public void testLoadMode07 () throws Exception {
        System.out.println("");
        System.out.println("ModeParserTest.testLoadMode07 START");
        
        ModeParser modeParser = createModeParser("data/valid/Windows/Modes","mode07");
        
        ModeConfig modeCfg = modeParser.load();
        
        //Check loaded data
        assertNotNull("Could not load data.",modeCfg);
        
        InternalConfig internalCfg = modeParser.getInternalConfig();
        
        assertNotNull("Could not load internal data.",internalCfg);
        
        //Check data
        assertEquals("Mode state",Constants.MODE_STATE_JOINED,modeCfg.state);
        
        assertEquals("Mode type",Constants.MODE_KIND_VIEW,modeCfg.kind);
        
        assertEquals("Active TC","filesystems",modeCfg.selectedTopComponentID);
        assertEquals("Previous active TC","explorer",modeCfg.previousSelectedTopComponentID);
        assertTrue("Permanent",modeCfg.permanent);
        
        System.out.println("ModeParserTest.testLoadMode07 FINISH");
    }
    
    /** Test of saving
     */
    public void testSaveMode01 () throws Exception {
        System.out.println("");
        System.out.println("ModeParserTest.testSaveMode01 START");
        
        ModeParser modeParser = createModeParser("data/valid/Windows/Modes","mode01");
        
        ModeConfig modeCfg1 = modeParser.load();
        
        modeParser.save(modeCfg1);
        
        ModeConfig modeCfg2 = modeParser.load();
        
        //Compare data
        assertTrue("Compare configuration data",modeCfg1.equals(modeCfg2));
                
        System.out.println("ModeParserTest.testSaveMode01 FINISH");
    }
    
    /** Test of saving
     */
    public void testSaveMode02 () throws Exception {
        System.out.println("");
        System.out.println("ModeParserTest.testSaveMode02 START");
        
        ModeParser modeParser = createModeParser("data/valid/Windows/Modes","mode02");
        
        ModeConfig modeCfg1 = modeParser.load();
        
        modeParser.save(modeCfg1);
        
        ModeConfig modeCfg2 = modeParser.load();
        
        //Compare data
        assertTrue("Compare configuration data",modeCfg1.equals(modeCfg2));
                
        System.out.println("ModeParserTest.testSaveMode02 FINISH");
    }
    
    /** Test of saving
     */
    public void testSaveMode03 () throws Exception {
        System.out.println("");
        System.out.println("ModeParserTest.testSaveMode03 START");
        
        ModeParser modeParser = createModeParser("data/valid/Windows/Modes","mode03");
        
        ModeConfig modeCfg1 = modeParser.load();
        
        modeParser.save(modeCfg1);
        
        ModeConfig modeCfg2 = modeParser.load();
        
        //Compare data
        assertTrue("Compare configuration data",modeCfg1.equals(modeCfg2));
                
        System.out.println("ModeParserTest.testSaveMode03 FINISH");
    }
    
    /** Test of saving
     */
    public void testSaveMode04 () throws Exception {
        System.out.println("");
        System.out.println("ModeParserTest.testSaveMode04 START");
        
        ModeParser modeParser = createModeParser("data/valid/Windows/Modes","mode04");
        
        ModeConfig modeCfg1 = modeParser.load();
        
        modeParser.save(modeCfg1);
        
        ModeConfig modeCfg2 = modeParser.load();
        
        //Compare data
        assertTrue("Compare configuration data",modeCfg1.equals(modeCfg2));
                
        System.out.println("ModeParserTest.testSaveMode04 FINISH");
    }
    
    
    /** Test of saving sliding mode with predefined slide-in sizes.
     */
    public void testSaveMode06 () throws Exception {
        System.out.println("");
        System.out.println("ModeParserTest.testSaveMode06 START");
        
        ModeParser modeParser = createModeParser("data/valid/Windows/Modes","mode06");
        
        ModeConfig modeCfg1 = modeParser.load();
        
        modeParser.save(modeCfg1);
        
        ModeConfig modeCfg2 = modeParser.load();
        
        //Compare data
        assertTrue("Compare configuration data",modeCfg1.equals(modeCfg2));
                
        System.out.println("ModeParserTest.testSaveMode06 FINISH");
    }
    
    /** Test of saving a mode with previously active tc.
     */
    public void testSaveMode07 () throws Exception {
        System.out.println("");
        System.out.println("ModeParserTest.testSaveMode07 START");
        
        ModeParser modeParser = createModeParser("data/valid/Windows/Modes","mode07");
        
        ModeConfig modeCfg1 = modeParser.load();
        
        modeParser.save(modeCfg1);
        
        ModeConfig modeCfg2 = modeParser.load();
        
        //Compare data
        assertTrue("Compare configuration data",modeCfg1.equals(modeCfg2));
                
        System.out.println("ModeParserTest.testSaveMode07 FINISH");
    }
    
    ////////////////////////////////
    //Testing INVALID data
    ////////////////////////////////
    /** Test of missing file
     */
    public void testLoadMode01Invalid () throws Exception {
        System.out.println("");
        System.out.println("ModeParserTest.testLoadMode01Invalid START");
        
        ModeParser modeParser = createModeParser("data/invalid/Windows/Modes","mode01");
        
        try {
            modeParser.load();
        } catch (FileNotFoundException exc) {
            //Missing file detected
            //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
            System.out.println("ModeParserTest.testLoadMode01Invalid FINISH");
            return;
        }
        
        fail("Missing file was not detected.");
    }
    
    /** Test of empty file
     */
    public void testLoadMode02Invalid () throws Exception {
        System.out.println("");
        System.out.println("ModeParserTest.testLoadMode02Invalid START");
        
        ModeParser modeParser = createModeParser("data/invalid/Windows/Modes","mode02");
        
        try {
            modeParser.load();
        } catch (IOException exc) {
            //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
            System.out.println("ModeParserTest.testLoadMode02Invalid FINISH");
            return;
        }
        
        fail("Empty file was not detected.");
    }
    
    /** Test of missing required attribute "unique" of element "name".
     */
    public void testLoadMode03Invalid () throws Exception {
        System.out.println("");
        System.out.println("ModeParserTest.testLoadMode03Invalid START");
        
        ModeParser modeParser = createModeParser("data/invalid/Windows/Modes","mode03");
        
        try {
            modeParser.load();
        } catch (IOException exc) {
            //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
            System.out.println("ModeParserTest.testLoadMode03Invalid FINISH");
            return;
        }
        
        fail("Missing required attribute \"unique\" of element \"name\" was not detected.");
    }
    
    /** Test of file name and value of attribute "unique" mismatch.
     */
    public void testLoadMode04Invalid () throws Exception {
        System.out.println("");
        System.out.println("ModeParserTest.testLoadMode04Invalid START");
        
        ModeParser modeParser = createModeParser("data/invalid/Windows/Modes","mode04");
        
        try {
            modeParser.load();
        } catch (IOException exc) {
            //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
            System.out.println("ModeParserTest.testLoadMode04Invalid FINISH");
            return;
        }
        
        fail("Mismatch of file name and value of attribute \"unique\" of element \"name\" was not detected.");
    }
    
    private ModeParser createModeParser (String path, String name) {
        URL url;
        url = ModeParserTest.class.getResource(path);
        assertNotNull("url not found.",url);
        
        FileObject parentFolder = URLMapper.findFileObject(url);
        assertNotNull("Test parent folder not found. ParentFolder is null.",url);

        Set setLocal = new HashSet();
        ModeParser modeParser = new ModeParser(name,setLocal);
        modeParser.setInLocalFolder(true);
        modeParser.setLocalParentFolder(parentFolder);
        
        return modeParser;
    }
    
}
