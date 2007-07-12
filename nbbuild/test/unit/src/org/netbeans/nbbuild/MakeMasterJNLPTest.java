/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.junit.NbTestCase;

/** Is generation of Jnlp files correct?
 *
 * @author Jaroslav Tulach
 */
public class MakeMasterJNLPTest extends NbTestCase {
    public MakeMasterJNLPTest (String name) {
        super (name);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
    }
    
    
    public void testGenerateReferenceFilesOnce() throws Exception {
        doGenerateReferenceFiles(1);
    }
    public void testGenerateReferenceFilesThrice() throws Exception {
        doGenerateReferenceFiles(3);
    }
    
    private void doGenerateReferenceFiles(int cnt) throws Exception {
        Manifest m;
        
        m = ModuleDependenciesTest.createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.my.module/3");
        File simpleJar = generateJar (new String[0], m);

        m = ModuleDependenciesTest.createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.second.module/3");
        File secondJar = generateJar (new String[0], m);
        
        File parent = simpleJar.getParentFile ();
        assertEquals("They are in the same folder", parent, secondJar.getParentFile());
        
        File output = new File(parent, "output");
        
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeMasterJNLP\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" + 
            "  <jnlp dir='" + output + "'  >" +
            "    <modules dir='" + parent + "' >" +
            "      <include name='" + simpleJar.getName() + "' />" +
            "      <include name='" + secondJar.getName() + "' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>"
        );
        while (cnt-- > 0) {
            PublicPackagesInProjectizedXMLTest.execute (f, new String[] { "-verbose" });
        }
        
        assertTrue ("Output exists", output.exists ());
        assertTrue ("Output directory created", output.isDirectory());
        
        String[] files = output.list();
        assertEquals("It has two files", 2, files.length);

        java.util.Arrays.sort(files);
        
        assertEquals("The res1 file: " + files[0], "org-my-module.ref", files[0]);
        assertEquals("The res2 file: "+ files[1], "org-second-module.ref", files[1]);
        
        File r1 = new File(output, "org-my-module.ref");
        String res1 = ModuleDependenciesTest.readFile (r1);

        File r2 = new File(output, "org-second-module.ref");
        String res2 = ModuleDependenciesTest.readFile (r2);
        
        assertExt(res1, "org.my.module");
        assertExt(res2, "org.second.module");
    }
    
    private static void assertExt(String res, String module) {
        int ext = res.indexOf("<extension");
        if (ext == -1) {
            fail ("<extension tag shall start there: " + res);
        }
        
        assertEquals("Just one extension tag", -1, res.indexOf("<extension", ext + 1));

        int cnb = res.indexOf(module);
        if (cnb == -1) {
            fail("Cnb has to be there: " + module + " but is " + res);
        }
        assertEquals("Just one cnb", -1, res.indexOf(module, cnb + 1));
        
        String dashcnb = module.replace('.', '-');
        
        int dcnb = res.indexOf(dashcnb);
        if (dcnb == -1) {
            fail("Dash Cnb has to be there: " + dashcnb + " but is " + res);
        }
        assertEquals("Just one dash cnb", -1, res.indexOf(dashcnb, dcnb + 1));
    }

    private final File createNewJarFile () throws IOException {
        int i = 0;
        for (;;) {
            File f = new File (this.getWorkDir(), i++ + ".jar");
            if (!f.exists ()) return f;
        }
    }
    
    protected final File generateJar (String[] content, Manifest manifest) throws IOException {
        File f = createNewJarFile ();
        
        JarOutputStream os = new JarOutputStream (new FileOutputStream (f), manifest);
        
        for (int i = 0; i < content.length; i++) {
            os.putNextEntry(new JarEntry (content[i]));
            os.closeEntry();
        }
        os.closeEntry ();
        os.close();
        
        return f;
    }

}
