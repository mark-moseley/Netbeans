/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Jaroslav Tulach
 */
public class CheckLicenseTest extends NbTestCase {
    
    public CheckLicenseTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        //return new CheckLicenseTest("testReplaceHTMLLicense");
        return new NbTestSuite(CheckLicenseTest.class);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testWeCanSearchForSunPublicLicense() throws Exception {
        java.io.File license = PublicPackagesInProjectizedXMLTest.extractString(
            "<!-- Sun Public License -->\n" +
            "<head></head><body>\n" +
            "<a href=\"http://www.netbeans.org/download/dev/javadoc/OpenAPIs/index.hml\">Forbidden link</a>\n" +
            "</body>"
        );
      
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checkl\" classname=\"org.netbeans.nbbuild.CheckLicense\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <checkl fragment='Sun Public' >" +
            "   <fileset dir='" + license.getParent() + "'>" +
            "    <include name=\"" + license.getName () + "\" />" +
            "   </fileset>\n" +
            "  </checkl>" +
            "</target>" +
            "</project>"
        );
        // success
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { });

        if (PublicPackagesInProjectizedXMLTest.getStdErr().indexOf(license.getPath()) > - 1) {
            fail("file name shall not be there: " + PublicPackagesInProjectizedXMLTest.getStdErr());
        }
        if (PublicPackagesInProjectizedXMLTest.getStdErr().indexOf("no license") > - 1) {
            fail("warning shall not be there: " + PublicPackagesInProjectizedXMLTest.getStdErr());
        }
    }        

    public void testTheTaskFailsIfItIsPresent() throws Exception {
        java.io.File license = PublicPackagesInProjectizedXMLTest.extractString(
            "<!-- Sun Public License -->\n" +
            "<head></head><body>\n" +
            "<a href=\"http://www.netbeans.org/download/dev/javadoc/OpenAPIs/index.hml\">Forbidden link</a>\n" +
            "</body>"
        );
        java.io.File license2 = PublicPackagesInProjectizedXMLTest.extractString(
            "<!-- Sun Public License -->\n" +
            "<head></head><body>\n" +
            "<a href=\"http://www.netbeans.org/download/dev/javadoc/OpenAPIs/index.hml\">Forbidden link</a>\n" +
            "</body>"
        );
        assertEquals(license.getParent(), license2.getParent());
      
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checkl\" classname=\"org.netbeans.nbbuild.CheckLicense\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <checkl fragment='Sun Public' fail='whenpresent' >" +
            "   <fileset dir='" + license.getParent() + "'>" +
            "    <include name=\"" + license.getName () + "\" />" +
            "    <include name=\"" + license2.getName () + "\" />" +
            "   </fileset>\n" +
            "  </checkl>" +
            "</target>" +
            "</project>"
        );
        try {
            PublicPackagesInProjectizedXMLTest.execute (f, new String[] { });
            fail("Should fail as the license is missing");
        } catch (PublicPackagesInProjectizedXMLTest.ExecutionError ex) {
            // ok
        }
        
        String out = PublicPackagesInProjectizedXMLTest.getStdErr();
        if (out.indexOf(license.getName()) == -1) {
            fail(license.getName() + " should be there: " + out);
        }
        if (out.indexOf(license2.getName()) == -1) {
            fail(license2.getName() + " should be there: " + out);
        }
    }        
    
    public void testTheTaskReportsIfItIsMissing() throws Exception {
        java.io.File license = PublicPackagesInProjectizedXMLTest.extractString(
            "<head></head><body>\n" +
            "<a href=\"http://www.netbeans.org/download/dev/javadoc/OpenAPIs/index.hml\">Forbidden link</a>\n" +
            "</body>"
        );
      
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checkl\" classname=\"org.netbeans.nbbuild.CheckLicense\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <checkl fragment='Sun Public' >" +
            "   <fileset dir='" + license.getParent() + "'>" +
            "    <include name=\"" + license.getName () + "\" />" +
            "   </fileset>\n" +
            "  </checkl>" +
            "</target>" +
            "</project>"
        );
        // success
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { });
        
        if (PublicPackagesInProjectizedXMLTest.getStdErr().indexOf(license.getPath()) == - 1) {
            fail("file name shall be there: " + PublicPackagesInProjectizedXMLTest.getStdErr());
        }
        if (PublicPackagesInProjectizedXMLTest.getStdErr().indexOf("no license") == - 1) {
            fail("warning shall be there: " + PublicPackagesInProjectizedXMLTest.getStdErr());
        }
    }        

    public void testNoReportsWhenInFailMode() throws Exception {
        java.io.File license = PublicPackagesInProjectizedXMLTest.extractString(
            "<head></head><body>\n" +
            "<a href=\"http://www.netbeans.org/download/dev/javadoc/OpenAPIs/index.hml\">Forbidden link</a>\n" +
            "</body>"
        );
      
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checkl\" classname=\"org.netbeans.nbbuild.CheckLicense\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <checkl fragment='Sun Public' fail='whenpresent'>" +
            "   <fileset dir='" + license.getParent() + "'>" +
            "    <include name=\"" + license.getName () + "\" />" +
            "   </fileset>\n" +
            "  </checkl>" +
            "</target>" +
            "</project>"
        );
        // success
        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { });
        
        if (PublicPackagesInProjectizedXMLTest.getStdErr().indexOf(license.getPath()) != - 1) {
            fail("file name shall not be there: " + PublicPackagesInProjectizedXMLTest.getStdErr());
        }
        if (PublicPackagesInProjectizedXMLTest.getStdErr().indexOf("no license") != - 1) {
            fail("warning shall not be there: " + PublicPackagesInProjectizedXMLTest.getStdErr());
        }
    }        
    
    public void testTheTaskFailsIfItIsMissing() throws Exception {
        java.io.File license = PublicPackagesInProjectizedXMLTest.extractString(
            "<head></head><body>\n" +
            "<a href=\"http://www.netbeans.org/download/dev/javadoc/OpenAPIs/index.hml\">Forbidden link</a>\n" +
            "</body>"
        );
      
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checkl\" classname=\"org.netbeans.nbbuild.CheckLicense\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <checkl fragment='Sun Public' fail='whenmissing' >" +
            "   <fileset dir='" + license.getParent() + "'>" +
            "    <include name=\"" + license.getName () + "\" />" +
            "   </fileset>\n" +
            "  </checkl>" +
            "</target>" +
            "</project>"
        );
        try {
            PublicPackagesInProjectizedXMLTest.execute (f, new String[] { });
            fail("Should fail as the license is missing");
        } catch (PublicPackagesInProjectizedXMLTest.ExecutionError ex) {
            // ok
        }
    }        
    
    public void testReplaceJavaLicense() throws Exception {
        java.io.File tmp = PublicPackagesInProjectizedXMLTest.extractString(
"/*\n" + 
" *                 Sun Public License Notice\n" +
" *\n" +
" * The contents of this file are subject to the Sun Public License\n" +
" * Version 1.0 (the \"License\"). You may not use this file except in\n" +
" * compliance with the License. A copy of the License is available at\n" +
" * http://www.sun.com/\n" +
" *\n" +
" * The Original Code is NetBeans. The Initial Developer of the Original\n" +
" * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun\n" +
" * Microsystems, Inc. All Rights Reserved.\n" +
" */\n" +
"\n" +            
"package test;\n" +
"public class MyTest {\n" +
"  public static int FIELD = 1;\n" +
"}\n" +
"\n"
        );
        File java = new File(tmp.getParentFile(), "MyTest.java");
        tmp.renameTo(java);
        assertTrue("File exists", java.exists());
      
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractResource("CheckLicenseAnt.xml");

        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { 
            "-verbose", 
            "-Ddir=" + java.getParent(),  
            "-Dinclude=" + java.getName(),
        });
        
        if (PublicPackagesInProjectizedXMLTest.getStdOut().indexOf(java.getPath()) == - 1) {
            fail("file name shall be there: " + PublicPackagesInProjectizedXMLTest.getStdOut());
        }
        
        
        assertTrue("Still exists", java.exists());
        
        String content = PublicPackagesInProjectizedXMLTest.readFile(java);
        {
            Matcher m = Pattern.compile("\\* *Ahoj *\\* *Jardo").matcher(content.replace('\n', ' '));
            if (!m.find()) {
                fail("Replacement shall be there together with prefix:\n" + content);
            }
        }
        
        {
            Matcher m = Pattern.compile("^ \\*New. \\*Warning", Pattern.MULTILINE | Pattern.DOTALL).matcher(content);
            if (!m.find()) {
                fail("warning shall be there:\n" + content);
            }
        }
        
        {
            String[] lines = content.split("\n");
            if (lines.length < 5) {
                fail("There should be more than five lines: " + content);
            }
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].length() == 0) {
                    fail("There is an empty line: " + content);
                }
                if (lines[i].equals(" */")) {
                    break;
                }
                if (lines[i].endsWith(" ")) {
                    fail("Ends with space: '" + lines[i] + "' in:\n" + content);
                }
            }
        }
    }        

    
    public void testReplaceHTMLLicense() throws Exception {
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractResource("CheckLicenseAnt.xml");

        java.io.File tmp = PublicPackagesInProjectizedXMLTest.extractResource("CheckLicenseHtmlExample.xml");
        File html = new File(tmp.getParentFile(), "MyTest.html");
        tmp.renameTo(html);
        assertTrue("File exists", html.exists());
      

        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { 
            "-verbose", 
            "-Ddir=" + html.getParent(),  
            "-Dinclude=" + html.getName(),
        });
        
        if (PublicPackagesInProjectizedXMLTest.getStdOut().indexOf(html.getPath()) == - 1) {
            fail("file name shall be there: " + PublicPackagesInProjectizedXMLTest.getStdOut());
        }
        
        
        assertTrue("Still exists", html.exists());
        
        String content = PublicPackagesInProjectizedXMLTest.readFile(html);
        {
            Matcher m = Pattern.compile(" *- *Ahoj *- *Jardo").matcher(content.replace('\n', ' '));
            if (!m.find()) {
                fail("Replacement shall be there together with prefix:\n" + content);
            }
        }
        
        {
            Matcher m = Pattern.compile("^ *-New. *-Warning", Pattern.MULTILINE | Pattern.DOTALL).matcher(content);
            if (!m.find()) {
                fail("warning shall be there:\n" + content);
            }
        }
        
        {
            String[] lines = content.split("\n");
            if (lines.length < 5) {
                fail("There should be more than five lines: " + content);
            }
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].length() == 0) {
                    fail("There is an empty line: " + content);
                }
                if (lines[i].indexOf("-->") >= 0) {
                    break;
                }
                if (lines[i].endsWith(" ")) {
                    fail("Ends with space: '" + lines[i] + "' in:\n" + content);
                }
            }
        }
    }        

    public void testNoReplaceWhenNoHTMLLicense() throws Exception {
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractResource("CheckLicenseAnt.xml");

        java.io.File tmp = PublicPackagesInProjectizedXMLTest.extractString(
            "<head></head><body>\n" +
            "<a href=\"http://www.netbeans.org/download/dev/javadoc/OpenAPIs/index.hml\">Forbidden link</a>\n" +
            "1997-2006" +
            "</body>"
        );
        File html = new File(tmp.getParentFile(), "MyTest.html");
        tmp.renameTo(html);
        assertTrue("File exists", html.exists());
      

        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { 
            "-Ddir=" + html.getParent(),  
            "-Dinclude=" + html.getName(),
        });
        
        if (PublicPackagesInProjectizedXMLTest.getStdOut().indexOf(html.getPath()) != - 1) {
            fail("file name shall not be there: " + PublicPackagesInProjectizedXMLTest.getStdOut());
        }
        
    }        

    public void testMayReplaces() throws Exception {
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractResource("CheckLicenseAnt.xml");

        java.io.File tmp = PublicPackagesInProjectizedXMLTest.extractString(
            "<head></head><body>\n" +
            "<a href=\"http://www.netbeans.org/download/dev/javadoc/OpenAPIs/index.hml\">Forbidden link</a>\n" +
            "Original Code\n" +
            "Original Code\n" +
            "Original Code\n" +
            "Original Code\n" +
            "</body>"
        );
        File html = new File(tmp.getParentFile(), "MyTest.html");
        tmp.renameTo(html);
        assertTrue("File exists", html.exists());
      

        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { 
            "-Ddir=" + html.getParent(),  
            "-Dinclude=" + html.getName(),
        });
        
        if (PublicPackagesInProjectizedXMLTest.getStdOut().indexOf("Original Code") != - 1) {
            fail("Original Code shall not be there: " + PublicPackagesInProjectizedXMLTest.getStdOut());
        }

        String out = PublicPackagesInProjectizedXMLTest.readFile(html);
        int first = out.indexOf("Original Software");
        if (first == - 1) {
            fail("Original Software shall be there: " + out);
        }
        if (out.indexOf("Original Software", first + 1) == - 1) {
            fail("Original Software shall be there: " + out);
        }
    }    
    
    
    public void testWrongLineBeginningsWhenNoPrefix() throws Exception {
        String txt = "<!--\n" +
        "                 Sun Public License Notice\n" +
        "\n" +
        "The contents of this file are subject to the Sun Public License\n" +
        "Version 1.0 (the 'License'). You may not use this file except in\n" +
        "compliance with the License. A copy of the License is available at\n" +
        "http://www.sun.com/\n" +
        "\n" +
        "The Original Code is NetBeans. The Initial Developer of the Original\n" +
        "Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun\n" +
        "Microsystems, Inc. All Rights Reserved.\n" +
        "-->\n";
        String script = createScript();
        
    
        File fileScript = PublicPackagesInProjectizedXMLTest.extractString(script);
        File fileTxt = PublicPackagesInProjectizedXMLTest.extractString(txt);
        
        PublicPackagesInProjectizedXMLTest.execute (fileScript, new String[] { 
            "-Ddir=" + fileTxt.getParent(),  
            "-Dinclude=" + fileTxt.getName(),
        });
        
        if (PublicPackagesInProjectizedXMLTest.getStdOut().indexOf("Original Code") != - 1) {
            fail("Original Code shall not be there: " + PublicPackagesInProjectizedXMLTest.getStdOut());
        }

        String out = PublicPackagesInProjectizedXMLTest.readFile(fileTxt);
        
        String[] arr = out.split("\n");
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].endsWith(" ")) {
                fail("Ends with space: '" + arr[i] + "' in:\n" + out);
            }
            if (arr[i].length() < 2) {
                continue;
            }
            if (arr[i].charAt(0) != ' ') {
                continue;
            }
            
            fail("This line seems to start with space:\n" + arr[i] + "\nwhich is wrong in whole output:\n" + out);
        }
    }

    private static String createScript() {
        String script =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<project name=\"Test\" basedir=\".\" default=\"all\" >" +
    "  <taskdef name=\"checklicense\" classname=\"org.netbeans.nbbuild.CheckLicense\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
    "<target name=\"all\" >" +
"        <checklicense >\n" +
"            <fileset dir='${dir}'>\n" +
"              <include name='${include}'/>" + 
"            </fileset>\n" +
"\n" +            
"    <convert \n" +
"        token='^([ \\t]*[^ \\n]+[ \\t]?)?[ \\t]*Sun Public License Notice' \n" +
"        prefix='true'\n" +
"    >\n" +
"        <line text='The contents of this file are subject to the terms of the Common Development'/>\n" +
"        <line text='and Distribution License (the License). You may not use this file except in'/>\n" +
"        <line text='compliance with the License.'/>\n" +
"    </convert>\n" +
"    <convert \n" +
"     token='The *contents *of *this *file *are\n" +
" *subject *to *the *Sun *Public.*available.*at.*([hH][tT][tT][pP]://www.sun.com/|http://jalopy.sf.net/license-spl.html)'\n" +
"    >\n" +
"        <line text='You can obtain a copy of the License at http://www.netbeans.org/cddl.html'/>\n" +
"        <line text='or http://www.netbeans.org/cddl.txt.'/>\n" +
"        <line text=''/>\n" +
"        <line text='When distributing Covered Code, include this CDDL Header Notice in each file'/>\n" +
"        <line text='and include the License file at http://www.netbeans.org/cddl.txt.'/>\n" +
"        <line text='If applicable, add the following below the CDDL Header, with the fields'/>\n" +
"        <line text='enclosed by brackets [] replaced by your own identifying information:'/>\n" +
"        <line text='\"Portions Copyrighted [year] [name of copyright owner]\"'/>\n" +
"   </convert>\n" +
"   <convert token='1997-[0-2][09][09][0-9]' replace='1997-2006'/>\n" +
"   <convert token='Original\\n[^A-Za-z]*Code' replace='Original\\nSoftware' replaceall='true'/>\n" +
"   <convert token='Original Code' replace='Original Software' replaceall='true'/>\n" +
"        </checklicense>\n" +
"     </target>\n" +
"   </project>\n";
        return script;
    }
    
    public void testReplacesTextSeparatedByNewLine() throws Exception {
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractResource("CheckLicenseAnt.xml");

        java.io.File tmp = PublicPackagesInProjectizedXMLTest.extractString(
            "/*\n" +
            " *                 Sun Public License Notice\n" +
            " * \n" +
            " * The contents of this file are subject to the Sun Public License\n" +
            " * Version 1.0 (the 'License'). You may not use this file except in\n" +
            " * compliance with the License. A copy of the License is available at\n" +
            " * http://www.sun.com/\n" +
            " * \n" +
            " * The Original Code is NetBeans. The Initial Developer of the Original\n" +
            " * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun\n" +
            " * Microsystems, Inc. All Rights Reserved.\n" +
            " */\n" +
            "\n" +
            "\n" +
            "package org.openide.text;\n"
        );
        File java = new File(tmp.getParentFile(), "MyTest.html");
        tmp.renameTo(java);
        assertTrue("File exists", java.exists());
      

        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { 
            "-Ddir=" + java.getParent(),  
            "-Dinclude=" + java.getName(),
        });
        
        if (PublicPackagesInProjectizedXMLTest.getStdOut().indexOf("Code") != - 1) {
            fail("Original Code shall not be there: " + PublicPackagesInProjectizedXMLTest.getStdOut());
        }

        String out = PublicPackagesInProjectizedXMLTest.readFile(java);
        int first = out.indexOf("Original Software");
        if (first == - 1) {
            fail("Original Software shall be there: " + out);
        }
        if (out.indexOf("Software", first + 25) == - 1) {
            fail("Original Software shall be there: " + out);
        }
        
        String[] lines = out.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].length() > 80) {
                fail("Too long line:\n" + lines[i] + "\n in file:\n" + out);
            }
            if (lines[i].endsWith(" ")) {
                fail("Ends with space: '" + lines[i] + "' in:\n" + out);
            }
        }
    }    
    
    
    
    public void testWorksOnEmptyFile() throws Exception {
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractResource("CheckLicenseAnt.xml");

        java.io.File tmp = PublicPackagesInProjectizedXMLTest.extractString("");
        File html = new File(tmp.getParentFile(), "MyTest.html");
        tmp.renameTo(html);
        assertTrue("File exists", html.exists());
      

        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { 
            "-Ddir=" + html.getParent(),  
            "-Dinclude=" + html.getName(),
        });
        
        if (PublicPackagesInProjectizedXMLTest.getStdOut().indexOf(html.getPath()) != - 1) {
            fail("file name shall not be there: " + PublicPackagesInProjectizedXMLTest.getStdOut());
        }
        
    }        
    
    public void testReplacePropertiesLicense() throws Exception {
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractResource("CheckLicenseAnt.xml");

        java.io.File tmp = PublicPackagesInProjectizedXMLTest.extractResource("CheckLicensePropertiesExample.properties");
        File html = new File(tmp.getParentFile(), "MyTest.html");
        tmp.renameTo(html);
        assertTrue("File exists", html.exists());
      

        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { 
            "-verbose", 
            "-Ddir=" + html.getParent(),  
            "-Dinclude=" + html.getName(),
        });
        
        if (PublicPackagesInProjectizedXMLTest.getStdOut().indexOf(html.getPath()) == - 1) {
            fail("file name shall be there: " + PublicPackagesInProjectizedXMLTest.getStdOut());
        }
        
        
        assertTrue("Still exists", html.exists());
        
        
        String content = PublicPackagesInProjectizedXMLTest.readFile(html);
        
        if (!content.startsWith("#")) {
            fail("Shall start with #:\n" + content);
        }
        
        {
            Matcher m = Pattern.compile(" *\\# *Ahoj *\\# *Jardo").matcher(content.replace('\n', ' '));
            if (!m.find()) {
                fail("Replacement shall be there together with prefix:\n" + content);
            }
        }
        
        {
            Matcher m = Pattern.compile("^ *\\#New. *\\#Warning", Pattern.MULTILINE | Pattern.DOTALL).matcher(content);
            if (!m.find()) {
                fail("warning shall be there:\n" + content);
            }
        }
        
        {
            String[] lines = content.split("\n");
            if (lines.length < 5) {
                fail("There should be more than five lines: " + content);
            }
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].endsWith(" ")) {
                    fail("Ends with space: '" + lines[i] + "' in:\n" + content);
                }
                if (lines[i].length() == 0) {
                    fail("There is an empty line: " + content);
                }
                if (lines[i].indexOf("All Rights") >= 0) {
                    break;
                }
            }
        }
        
        {
            if (content.indexOf("2002") != -1) {
                fail("No reference to year 2002:\n" + content);
            }
            if (content.indexOf("2006") == -1) {
                fail("There should be a ref to 2006:\n" + content);
            }
        }
    }        

    
    public void testReplaceXMLLicense() throws Exception {
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractResource("CheckLicenseAnt.xml");

        java.io.File tmp = PublicPackagesInProjectizedXMLTest.extractResource("CheckLicenseXmlExample.xml");
        File xml = new File(tmp.getParentFile(), "MyTest.xml");
        tmp.renameTo(xml);
        assertTrue("File exists", xml.exists());
      

        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { 
            "-verbose", 
            "-Ddir=" + xml.getParent(),  
            "-Dinclude=" + xml.getName(),
        });
        
        if (PublicPackagesInProjectizedXMLTest.getStdOut().indexOf(xml.getPath()) == - 1) {
            fail("file name shall be there: " + PublicPackagesInProjectizedXMLTest.getStdOut());
        }
        
        
        assertTrue("Still exists", xml.exists());
        
        
        String content = PublicPackagesInProjectizedXMLTest.readFile(xml);
        
        if (!content.startsWith("<")) {
            fail("Shall start with <:\n" + content);
        }
        
        {
            Matcher m = Pattern.compile(" *Ahoj *Jardo").matcher(content.replace('\n', ' '));
            if (!m.find()) {
                fail("Replacement shall be there together with prefix:\n" + content);
            }
        }
        
        {
            Matcher m = Pattern.compile("^ *New. *Warning", Pattern.MULTILINE | Pattern.DOTALL).matcher(content);
            if (!m.find()) {
                fail("warning shall be there:\n" + content);
            }
        }
        
        {
            if (content.indexOf("2002") != -1) {
                fail("No reference to year 2002:\n" + content);
            }
            if (content.indexOf("2006") == -1) {
                fail("There should be a ref to 2006:\n" + content);
            }
        }
    }        

    public void testProblemsWithTermEmulator() throws Exception {
        String txt =  
            "/*   \n" +
            " *			Sun Public License Notice\n" +
            " *\n" +
            " * The contents of this file are subject to the Sun Public License Version\n" +
            " * 1.0 (the \"License\"). You may not use this file except in compliance\n" +
            " * with the License. A copy of the License is available at\n" +
            " * http://www.sun.com/\n" +
            " * \n" +
            " * The Original Code is Terminal Emulator.\n" +
            " * The Initial Developer of the Original Code is Sun Microsystems, Inc..\n" +
            " * Portions created by Sun Microsystems, Inc. are Copyright (C) 2001.\n" +
            " * All Rights Reserved.\n" +
            " *\n" +
            " * Contributor(s): Ivan Soleimanipour.\n" +
            " */\n";
        String script = createScript();
        
    
        File fileScript = PublicPackagesInProjectizedXMLTest.extractString(script);
        File fileTxt = PublicPackagesInProjectizedXMLTest.extractString(txt);
        
        PublicPackagesInProjectizedXMLTest.execute (fileScript, new String[] { 
            "-Ddir=" + fileTxt.getParent(),  
            "-Dinclude=" + fileTxt.getName(),
        });
        
        if (PublicPackagesInProjectizedXMLTest.getStdOut().indexOf("Original Code") != - 1) {
            fail("Original Code shall not be there: " + PublicPackagesInProjectizedXMLTest.getStdOut());
        }

        String out = PublicPackagesInProjectizedXMLTest.readFile(fileTxt);


        if (out.indexOf("Sun Public") >= 0) {
            fail(out);
        }
    }

    
    public void testDoubleHtmlComments() throws Exception {
        java.io.File f = PublicPackagesInProjectizedXMLTest.extractString(createScript());

        java.io.File tmp = PublicPackagesInProjectizedXMLTest.extractString(
"<!--\n" +
"  --                 Sun Public License Notice\n" +
"  --\n" +
"  -- The contents of this file are subject to the Sun Public License\n" +
"  -- Version 1.0 (the \"License\"). You may not use this file except in\n" +
"  -- compliance with the License. A copy of the License is available at\n" +
"  -- http://www.sun.com/\n" +
"  --\n" +
"  -- The Original Code is NetBeans. The Initial Developer of the Original\n" +
"  -- Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun\n" +
"  -- Microsystems, Inc. All Rights Reserved.\n" +
"  -->\n"
        );
        File file = new File(tmp.getParentFile(), "MyTest.html");
        tmp.renameTo(file);
        assertTrue("File exists", file.exists());
      

        PublicPackagesInProjectizedXMLTest.execute (f, new String[] { 
            "-Ddir=" + file.getParent(),  
            "-Dinclude=" + file.getName(),
        });
        
        String out = PublicPackagesInProjectizedXMLTest.readFile(file);
        int first = out.indexOf("Sun Public");
        if (first != - 1) {
            fail("Sun Public shall not  be there:\n" + out);
        }
    }    
    
    public void testDoNotReplaceSpacesBeyondTheLicense() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append('A');
        for (int i = 0; i < 10000; i++) {
            sb.append(' ');
        }
        sb.append('B');
        
        java.io.File license = PublicPackagesInProjectizedXMLTest.extractString(
            "<!-- Sun Public License Notice -->\n" +
            "<head></head><body>\n" +
            "<a href=\"http://www.netbeans.org/download/dev/javadoc/OpenAPIs/index.hml\">Forbidden link</a>\n" +
            "</body>" +
            sb
        );
        String script = createScript();
        
    
        PublicPackagesInProjectizedXMLTest.execute (
            PublicPackagesInProjectizedXMLTest.extractString(script), 
            new String[] { 
            "-Ddir=" + license.getParent(),  
            "-Dinclude=" + license.getName(),
        });
        
        String out = PublicPackagesInProjectizedXMLTest.readFile(license);


        if (out.indexOf("Sun Public") >= 0) {
            fail(out);
        }
        
        Matcher m = Pattern.compile("A( *)B").matcher(out);
        if (!m.find()) {
            fail("There should be long line:\n" + out);
        }
        if (m.group(1).length() != 10000) {
            fail("There should be 10000 spaces, but is only: " + m.group(1).length() + "\n" + out);
        }
    }    
}

      