/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject.applet;

import java.io.*;
import java.net.*;
import java.util.*;

import org.openide.*;
import org.openide.modules.SpecificationVersion;
import org.openide.filesystems.*;
import org.openide.util.*;

import org.netbeans.api.java.classpath.*;
import org.netbeans.spi.java.classpath.support.*;
import org.netbeans.api.java.queries.*;
import org.netbeans.api.project.*;

import org.netbeans.api.java.platform.*;

import org.openide.loaders.*;
import org.openide.cookies.*;
import org.openide.src.*;

/** Support for execution of applets.
*
* @author Ales Novak, Martin Grebac
*/
public class AppletSupport {

    // JDK issue #6193279: Appletviewer does not accept encoded URLs
    private static final SpecificationVersion JDK_15 = new SpecificationVersion("1.5"); // NOI18N

    /** constant for html extension */
    private static final String HTML_EXT = "html"; // NOI18N
    /** constant for class extension */
    private static final String CLASS_EXT = "class"; // NOI18N

    private AppletSupport() {}

    // Used only from unit tests to suppress detection of applet. If value
    // is different from null it will be returned instead.
    public static Boolean unitTestingSupport_isApplet = null;
    
    public static boolean isApplet(FileObject file) {
        if (file == null) {
            return false;
        }
        // support for unit testing
        if (unitTestingSupport_isApplet != null) {
            return unitTestingSupport_isApplet.booleanValue();
        }
        try {
            DataObject classDO = DataObject.find(file);
            return (getAppletClassName(classDO.getCookie(SourceCookie.class)) != null);
        } catch (DataObjectNotFoundException ex) {
            // just ignore
        }
        return false;
    }
    
    public static String getAppletClassName(Object obj) {
        if ((obj == null) || !(obj instanceof SourceCookie)) {
            return null;
        }
        SourceCookie cookie = (SourceCookie) obj;
        String fullName = null;
        SourceElement source = cookie.getSource ();
        ClassElement[] classes = source.getClasses();
        boolean isApplet = false;
        for (int i = 0; i < classes.length; i++) {
            if (classes[i].isDeclaredAsApplet()) {
                fullName = classes[i].getName().getFullName ();
                return fullName;
            }
        }
        return null;
    }
    
    /**
    * @return html file with the same name as applet
    */
    private static FileObject generateHtml(FileObject appletFile, FileObject buildDir, FileObject classesDir) throws IOException {
        FileObject htmlFile = buildDir.getFileObject(appletFile.getName(), HTML_EXT);
        
        if (htmlFile == null) {
            htmlFile = buildDir.createData(appletFile.getName(), HTML_EXT);
        }
        
        FileLock lock = htmlFile.lock();
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(htmlFile.getOutputStream(lock));
            ClassPath cp = ClassPath.getClassPath(appletFile, ClassPath.EXECUTE);
            ClassPath sp = ClassPath.getClassPath(appletFile, ClassPath.SOURCE);
            String path = FileUtil.getRelativePath(sp.findOwnerRoot(appletFile), appletFile);
            path = path.substring(0, path.length()-5);
            fillInFile(writer, path + "." + CLASS_EXT, "codebase=\"" + classesDir.getURL() + "\""); // NOI18N
        } finally {
            lock.releaseLock();
            if (writer != null)
                writer.close();
        }
        return htmlFile;
    }

    /**
    * @return URL of the html file with the same name as sibling
    */
    public static URL generateHtmlFileURL(FileObject appletFile, FileObject buildDir, FileObject classesDir, String activePlatform) throws FileStateInvalidException {
        FileObject html = null;
        IOException ex = null;
        if ((appletFile == null) || (buildDir == null) || (classesDir == null)) {
            return null;
        }
        try {
            html = generateHtml(appletFile, buildDir, classesDir);
        } catch (IOException iex) {
            ex = iex;
        }
        URL url = null;
        try {
            if (ex == null) {
                // JDK issue #6193279: Appletviewer does not accept encoded URLs
                JavaPlatformManager pm = JavaPlatformManager.getDefault();
                JavaPlatform platform = null;
                if (activePlatform == null) {
                    platform = pm.getDefaultPlatform();
                }
                JavaPlatform[] installedPlatforms = pm.getPlatforms(null, new Specification ("j2se",null));   //NOI18N
                for (int i=0; i<installedPlatforms.length; i++) {
                    String antName = (String) installedPlatforms[i].getProperties().get("platform.ant.name");        //NOI18N
                    if (antName != null && antName.equals(activePlatform)) {
                        platform = installedPlatforms[i];
                    }
                }

                boolean workAround6193279 = platform != null    //In case of nonexisting platform don't use the workaround 
                        && platform.getSpecification().getVersion().compareTo(JDK_15)>=0; //JDK1.5 and higher
                if (workAround6193279) {
                    File f = FileUtil.toFile(html);
                    try {
                        url = new URL ("file",null,f.getAbsolutePath());
                    } catch (MalformedURLException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                }
                else {
                    url = html.getURL();
                }
            }
        } catch (FileStateInvalidException f) {
            throw new FileStateInvalidException();
        }
        return url;
    }

    /** fills in file with html source so it is html file with applet
    * @param file is a file to be filled
    * @param name is name of the applet                                     
    */
    private static void fillInFile(PrintWriter writer, String name, String codebase) {
        ResourceBundle bundle = NbBundle.getBundle(AppletSupport.class);

        writer.println("<HTML>"); // NOI18N
        writer.println("<HEAD>"); // NOI18N

        writer.print("   <TITLE>"); // NOI18N
        writer.print(bundle.getString("GEN_title"));
        writer.println("</TITLE>"); // NOI18N

        writer.println("</HEAD>"); // NOI18N
        writer.println("<BODY>\n"); // NOI18N

        writer.print(bundle.getString("GEN_warning"));

        writer.print("<H3><HR WIDTH=\"100%\">"); // NOI18N
        writer.print(bundle.getString("GEN_header"));
        writer.println("<HR WIDTH=\"100%\"></H3>\n"); // NOI18N

        writer.println("<P>"); // NOI18N
//        String codebase = getCodebase (name);
        if (codebase == null)
            writer.print("<APPLET code="); // NOI18N
        else
            writer.print("<APPLET " + codebase + " code="); // NOI18N
        writer.print ("\""); // NOI18N

        writer.print(name);
        writer.print ("\""); // NOI18N

        writer.println(" width=350 height=200></APPLET>"); // NOI18N
        writer.println("</P>\n"); // NOI18N

        writer.print("<HR WIDTH=\"100%\"><FONT SIZE=-1><I>"); // NOI18N
        writer.print(bundle.getString("GEN_copy"));
        writer.println("</I></FONT>"); // NOI18N

        writer.println("</BODY>"); // NOI18N
        writer.println("</HTML>"); // NOI18N
        writer.flush();
    }
}
