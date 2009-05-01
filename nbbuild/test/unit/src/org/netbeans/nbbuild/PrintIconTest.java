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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.util.Arrays;
import org.netbeans.junit.NbTestCase;

/**
 * @author Jaroslav Tulach
 */
public class PrintIconTest extends NbTestCase {

    public PrintIconTest(String testName) {
        super(testName);
    }
    
    protected @Override void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
    }

    public void testPrintOutSameIcons() throws Exception {
        File img = PublicPackagesInProjectizedXMLTest.extractResource("data/instanceBroken.gif");
        File img2 = PublicPackagesInProjectizedXMLTest.extractResource("data/instanceObject.gif");
        File img3 = PublicPackagesInProjectizedXMLTest.extractResource("data/instanceBroken.gif");
        File out = PublicPackagesInProjectizedXMLTest.extractString("");
        out.delete();
        
        File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version='1.0' encoding='UTF-8'?>" +
            "<project name='Test Arch' basedir='.' default='all' >" +
            "  <taskdef name='printicon' classname='org.netbeans.nbbuild.PrintIcon' classpath='${nb_all}/nbbuild/nbantext.jar'/>" +
            "<target name='all' >" +
            "  <printicon duplicates='" + out + "'>" +
            "    <firstpool dir='" + img.getParent() + "'>" +
            "       <include name='" + img.getName() + "'/>" +
            "       <include name='" + img2.getName() + "'/>" +
            "    </firstpool>" +
            "    <secondpool dir='" + img3.getParent() + "'>" +
            "       <include name='" + img3.getName() + "'/>" +
            "    </secondpool>" +
            "  </printicon>" +
            "</target>" +
            "</project>"
        );
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { "-verbose" });
        
        assertTrue("Exists: " + out, out.canRead());
        
        String file = PublicPackagesInProjectizedXMLTest.readFile(out);
        
        String[] threeParts = file.split("\\s+");
        assertEquals(file, 6, threeParts.length);

        {
            long hash = Long.parseLong(threeParts[0], 16);
            assertEquals("Hash code is ee2ab8d3:\n" + file, 0xee2ab8d3L, hash);
            assertEquals("Name is from img:\n" + file, img.getName(), threeParts[1]);
            assertEquals("Full name is img:\n" + file, img.toURL().toExternalForm(), threeParts[2]);
        }
        
        {
            long hash = Long.parseLong(threeParts[3], 16);
            assertEquals("Hash code is ee2ab8d3:\n" + file, 0xee2ab8d3L, hash);
            assertEquals("Name is from img:\n" + file, img3.getName(), threeParts[4]);
            assertEquals("Full name is img:\n" + file, img3.toURL().toExternalForm(), threeParts[5]);
        }
        
    }
    
    
    public void testDuplicatesFromTheSameSet() throws Exception {
        File img = PublicPackagesInProjectizedXMLTest.extractResource("data/instanceBroken.gif");
        File img2 = PublicPackagesInProjectizedXMLTest.extractResource("data/instanceObject.gif");
        File img3 = PublicPackagesInProjectizedXMLTest.extractResource("data/instanceBroken.gif");
        File out = PublicPackagesInProjectizedXMLTest.extractString("");
        out.delete();
        
        File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version='1.0' encoding='UTF-8'?>" +
            "<project name='Test Arch' basedir='.' default='all' >" +
            "  <taskdef name='printicon' classname='org.netbeans.nbbuild.PrintIcon' classpath='${nb_all}/nbbuild/nbantext.jar'/>" +
            "<target name='all' >" +
            "  <printicon duplicates='" + out + "'>" +
            "    <firstpool dir='" + img.getParent() + "'>" +
            "       <include name='" + img.getName() + "'/>" +
            "       <include name='" + img2.getName() + "'/>" +
            "       <include name='" + img3.getName() + "'/>" +
            "    </firstpool>" +
            "  </printicon>" +
            "</target>" +
            "</project>"
        );
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { "-verbose" });
        
        assertTrue("Exists: " + out, out.canRead());
        
        String file = PublicPackagesInProjectizedXMLTest.readFile(out);
        
        String[] threeParts = file.split("\\s+");
        assertEquals(file, 6, threeParts.length);

        {
            long hash = Long.parseLong(threeParts[0], 16);
            assertEquals("Hash code is ee2ab8d3:\n" + file, 0xee2ab8d3L, hash);
            assertEquals("Name is from img:\n" + file, img.getName(), threeParts[1]);
            assertEquals("Full name is img:\n" + file, img.toURL().toExternalForm(), threeParts[2]);
        }
        
        {
            long hash = Long.parseLong(threeParts[3], 16);
            assertEquals("Hash code is ee2ab8d3:\n" + file, 0xee2ab8d3L, hash);
            assertEquals("Name is from img:\n" + file, img3.getName(), threeParts[4]);
            assertEquals("Full name is img:\n" + file, img3.toURL().toExternalForm(), threeParts[5]);
        }
        
    }

    public void testBrokenImageThatCould() throws Exception {
        doBrokenImageTest("data/columnIndex.gif");
    }
    public void testBrokenImageThatCould2() throws Exception {
        doBrokenImageTest("data/Category.png");
    }
    
    private void doBrokenImageTest(String res) throws Exception {
        File img = PublicPackagesInProjectizedXMLTest.extractResource(res);
        File img2 = PublicPackagesInProjectizedXMLTest.extractResource("data/instanceObject.gif");
        File img3 = PublicPackagesInProjectizedXMLTest.extractResource(res);
        File out = PublicPackagesInProjectizedXMLTest.extractString("");
        out.delete();
        
        File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version='1.0' encoding='UTF-8'?>" +
            "<project name='Test Arch' basedir='.' default='all' >" +
            "  <taskdef name='printicon' classname='org.netbeans.nbbuild.PrintIcon' classpath='${nb_all}/nbbuild/nbantext.jar'/>" +
            "<target name='all' >" +
            "  <printicon duplicates='" + out + "'>" +
            "    <firstpool dir='" + img.getParent() + "'>" +
            "       <include name='" + img.getName() + "'/>" +
            "       <include name='" + img2.getName() + "'/>" +
            "       <include name='" + img3.getName() + "'/>" +
            "    </firstpool>" +
            "  </printicon>" +
            "</target>" +
            "</project>"
        );
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { "-verbose" });
        
        assertTrue("Exists: " + out, out.canRead());
        
        String file = PublicPackagesInProjectizedXMLTest.readFile(out);
        
        String[] threeParts = file.split("\\s+");
        assertEquals(file + " " + Arrays.toString(threeParts), 6, threeParts.length);

        long prevHash;
        {
            prevHash = Long.parseLong(threeParts[0], 16);
            assertEquals("Name is from img:\n" + file, img.getName(), threeParts[1]);
            assertEquals("Full name is img:\n" + file, img.toURL().toExternalForm(), threeParts[2]);
        }
        
        {
            long hash = Long.parseLong(threeParts[3], 16);
            assertEquals("Hash code is the same:\n" + file, prevHash, hash);
            assertEquals("Name is from img:\n" + file, img3.getName(), threeParts[4]);
            assertEquals("Full name is img:\n" + file, img3.toURL().toExternalForm(), threeParts[5]);
        }
        
    }
    
    public void testPrintExtra() throws Exception {
        File img = PublicPackagesInProjectizedXMLTest.extractResource("data/instanceBroken.gif");
        File img2 = PublicPackagesInProjectizedXMLTest.extractResource("data/instanceObject.gif");
        File img3 = PublicPackagesInProjectizedXMLTest.extractResource("data/instanceBroken.gif");
        File out = PublicPackagesInProjectizedXMLTest.extractString("");
        out.delete();
        
        File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version='1.0' encoding='UTF-8'?>" +
            "<project name='Test Arch' basedir='.' default='all' >" +
            "  <taskdef name='printicon' classname='org.netbeans.nbbuild.PrintIcon' classpath='${nb_all}/nbbuild/nbantext.jar'/>" +
            "<target name='all' >" +
            "  <printicon difference='" + out + "'>" +
            "    <firstpool dir='" + img.getParent() + "'>" +
            "       <include name='" + img.getName() + "'/>" +
            "       <include name='" + img2.getName() + "'/>" +
            "    </firstpool>" +
            "    <secondpool dir='" + img3.getParent() + "'>" +
            "       <include name='" + img3.getName() + "'/>" +
            "    </secondpool>" +
            "  </printicon>" +
            "</target>" +
            "</project>"
        );
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { "-verbose" });
        
        assertTrue("Exists: " + out, out.canRead());
        
        String file = PublicPackagesInProjectizedXMLTest.readFile(out);
        
        if (!file.startsWith("-")) {
            fail("Should start with - as one icon is missing in new version:\n" + file);
        } else {
            file = file.substring(1);
        }
        
        String[] threeParts = file.split("\\s+");
        assertEquals(file, 3, threeParts.length);
        
        long hash = Long.parseLong(threeParts[0], 16);
        assertEquals("Hash code is 10ba4f25:\n" + file, 0x10ba4f25L, hash);
        assertEquals("Name is from img2:\n" + file, img2.getName(), threeParts[1]);
        assertEquals("Full name is img2:\n" + file, img2.toURL().toExternalForm(), threeParts[2]);
    }
}
