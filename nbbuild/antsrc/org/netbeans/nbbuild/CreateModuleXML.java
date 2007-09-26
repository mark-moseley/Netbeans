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

package org.netbeans.nbbuild;

import java.util.*;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.*;
import java.util.jar.*;
import java.io.*;
import java.util.zip.ZipEntry;

/** Create XML files corresponding to the set of known modules
 * without actually running the IDE.
 * @author Jesse Glick
 */
public class CreateModuleXML extends Task {

    private final List<FileSet> enabled = new ArrayList<FileSet>(1);
    private final List<FileSet> disabled = new ArrayList<FileSet>(1);
    private final List<FileSet> autoload = new ArrayList<FileSet>(1);
    private final List<FileSet> eager = new ArrayList<FileSet>(1);
    private final List<FileSet> hidden = new ArrayList<FileSet>(1);
    
    /** Add a set of module JARs that should be enabled.
     */
    public void addEnabled(FileSet fs) {
        enabled.add(fs);
    }
    
    /** Add a set of module JARs that should be disabled.
     */
    public void addDisabled(FileSet fs) {
        disabled.add(fs);
    }
    
    /** Add a set of module JARs that should be autoloads.
     */
    public void addAutoload(FileSet fs) {
        autoload.add(fs);
    }
    
    /** Add a set of module JARs that should be eager modules.
     */
    public void addEager(FileSet fs) {
        eager.add(fs);
    }
    
    /** Add a set of module JARs that should be hidden.
     */
    public void addHidden(FileSet fs) {
        hidden.add(fs);
    }
    
    private File xmldir = null;
    
    /** Set the modules directory where XML will be stored.
     * Normally the system/Modules/ directory in an installation.
     */
    public void setXmldir(File f) {
        xmldir = f;
    }
    
    private List<String> enabledNames = new ArrayList<String>(50);
    private List<String> disabledNames = new ArrayList<String>(10);
    private List<String> autoloadNames = new ArrayList<String>(10);
    private List<String> eagerNames = new ArrayList<String>(10);
    private List<String> hiddenNames = new ArrayList<String>(10);
    
    public void execute() throws BuildException {
        if (xmldir == null) throw new BuildException("Must set xmldir attribute", getLocation());
        if (!xmldir.exists ()) {
            if (!xmldir.mkdirs()) throw new BuildException("Cannot create directory " + xmldir, getLocation());
        }
        if (enabled.isEmpty() && disabled.isEmpty() && autoload.isEmpty() && eager.isEmpty() && hidden.isEmpty()) {
            log("Warning: <createmodulexml> with no modules listed", Project.MSG_WARN);
        }
        for (FileSet fs : enabled) {
            scanModules(fs, true, false, false, false, enabledNames);
        }
        for (FileSet fs : disabled) {
            scanModules(fs, false, false, false, false, disabledNames);
        }
        for (FileSet fs : autoload) {
            scanModules(fs, false, true, false, false, autoloadNames);
        }
        for (FileSet fs : eager) {
            scanModules(fs, false, false, true, false, eagerNames);
        }
        for (FileSet fs : hidden) {
            scanModules(fs, false, false, false, true, hiddenNames);
        }
        Collections.sort(enabledNames);
        Collections.sort(disabledNames);
        Collections.sort(autoloadNames);
        Collections.sort(eagerNames);
        Collections.sort(hiddenNames);
        if (!enabledNames.isEmpty()) {
            log("Enabled modules: " + enabledNames);
        }
        if (!disabledNames.isEmpty()) {
            log("Disabled modules: " + disabledNames);
        }
        if (!autoloadNames.isEmpty()) {
            log("Autoload modules: " + autoloadNames);
        }
        if (!eagerNames.isEmpty()) {
            log("Eager modules: " + eagerNames);
        }
        if (!hiddenNames.isEmpty()) {
            log("Hidden modules: " + hiddenNames);
        }
    }
    
    private void scanModules(FileSet fs, boolean isEnabled, boolean isAutoload, boolean isEager, boolean isHidden, List<String> names) throws BuildException {
        FileScanner scan = fs.getDirectoryScanner(getProject());
        File dir = scan.getBasedir();
        for (String kid : scan.getIncludedFiles()) {
            File module = new File(dir, kid);
            if (!module.exists()) throw new BuildException("Module file does not exist: " + module, getLocation());
            if (!module.getName().endsWith(".jar")) throw new BuildException("Only *.jar may be listed, check the fileset: " + module, getLocation());
            try {
                JarFile jar = new JarFile(module);
                try {
                    Manifest m = jar.getManifest();
                    Attributes attr = m.getMainAttributes();
                    String codename = attr.getValue("OpenIDE-Module");
                    if (codename == null) {
                        throw new BuildException("Missing manifest tag OpenIDE-Module; " + module + " is not a module", getLocation());
                    }
                    if (codename.endsWith(" ") || codename.endsWith("\t")) { // #62887
                        throw new BuildException("Illegal trailing space in OpenIDE-Module value from " + module, getLocation());
                    }
                    int idx = codename.lastIndexOf('/');
                    String codenamebase;
                    int rel;
                    if (idx == -1) {
                        codenamebase = codename;
                        rel = -1;
                    } else {
                        codenamebase = codename.substring(0, idx);
                        try {
                            rel = Integer.parseInt(codename.substring(idx + 1));
                        } catch (NumberFormatException e) {
                            throw new BuildException("Invalid OpenIDE-Module '" + codename + "' in " + module, getLocation());
                        }
                    }
                    File xml = new File(xmldir, codenamebase.replace('.', '-') + ".xml");
                    if (xml.exists()) {
                        // XXX should check that the old file actually matches what we would have written
                        log("Will not overwrite " + xml + "; skipping...", Project.MSG_VERBOSE);
                        continue;
                    }
                    String displayname = attr.getValue("OpenIDE-Module-Name");
                    if (displayname == null) {
                        String bundle = attr.getValue("OpenIDE-Module-Localizing-Bundle");
                        if (bundle != null) {
                            // Display name actually found in a bundle, not manifest.
                            ZipEntry entry = jar.getEntry(bundle);
                            InputStream is;
                            if (entry != null) {
                                is = jar.getInputStream(entry);
                            } else {
                                File moduleloc = new File(new File(module.getParentFile(), "locale"), module.getName());
                                if (! moduleloc.isFile()) {
                                    throw new BuildException("Expecting localizing bundle: " + bundle + " in: " + module);
                                }
                                JarFile jarloc = new JarFile(moduleloc);
                                try {
                                    ZipEntry entry2 = jarloc.getEntry(bundle);
                                    if (entry2 == null) {
                                        throw new BuildException("Expecting localizing bundle: " + bundle + " in: " + module);
                                    }
                                    is = jarloc.getInputStream(entry2);
                                } finally {
                                    jarloc.close();
                                }
                            }
                            try {
                                Properties p = new Properties();
                                p.load(is);
                                displayname = p.getProperty("OpenIDE-Module-Name");
                            } finally {
                                is.close();
                            }
                        }
                    }
                    if (displayname == null) displayname = codename;
                    names.add(displayname);
                    String spec = attr.getValue("OpenIDE-Module-Specification-Version");
                    
                    if (isHidden) {
                        File h = new File(xml.getParentFile(), xml.getName() + "_hidden");
                        h.createNewFile();
                    }
                    
                    if (isEager || isAutoload || isEnabled) {
                        OutputStream os = new FileOutputStream(xml);
                        try {
                            PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
                            // Please make sure formatting matches what the IDE actually spits
                            // out; it could matter.
                            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                            pw.println("<!DOCTYPE module PUBLIC \"-//NetBeans//DTD Module Status 1.0//EN\"");
                            pw.println("                        \"http://www.netbeans.org/dtds/module-status-1_0.dtd\">");
                            pw.println("<module name=\"" + codenamebase + "\">");
                            pw.println("    <param name=\"autoload\">" + isAutoload + "</param>");
                            pw.println("    <param name=\"eager\">" + isEager + "</param>");
                            if (!isAutoload && !isEager) {
                                pw.println("    <param name=\"enabled\">" + isEnabled + "</param>");
                            }
                            pw.println("    <param name=\"jar\">" + kid.replace(File.separatorChar, '/') + "</param>");
                            if (rel != -1) {
                                pw.println("    <param name=\"release\">" + rel + "</param>");
                            }
                            pw.println("    <param name=\"reloadable\">false</param>");
                            if (spec != null) {
                                pw.println("    <param name=\"specversion\">" + spec + "</param>");
                            }
                            pw.println("</module>");
                            pw.flush();
                            pw.close();
                        } finally {
                            os.close();
                        }
                    }
                } finally {
                    jar.close();
                }
            } catch (IOException ioe) {
                throw new BuildException("Caught while processing " + module + ": " + ioe, ioe, getLocation());
            }
        }
    }
    
}
