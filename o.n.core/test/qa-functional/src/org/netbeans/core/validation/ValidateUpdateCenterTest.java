/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.core.validation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.netbeans.core.startup.ConsistencyVerifier;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Checks that all modules in the distribution are suitably visible to Plugin Manager.
 */
public class ValidateUpdateCenterTest extends NbTestCase {

    public ValidateUpdateCenterTest(String n) {
        super(n);
    }

    public void testInvisibleModules() throws Exception {
        Set<Manifest> manifests = loadManifests();
        Set<String> requiredBySomeone = new HashSet<String>();
        for (Manifest m : manifests) {
            String deps = m.getMainAttributes().getValue("OpenIDE-Module-Module-Dependencies");
            if (deps != null) {
                String identifier = "[\\p{javaJavaIdentifierStart}][\\p{javaJavaIdentifierPart}]*";
                Matcher match = Pattern.compile(identifier + "(\\." + identifier + ")*").matcher(deps);
                while (match.find()) {
                    requiredBySomeone.add(match.group());
                }
            }
        }
        StringBuilder auVisibilityProblems = new StringBuilder();
        String[] markers = {"autoload", "eager", "AutoUpdate-Show-In-Client", "AutoUpdate-Essential-Module"};
        MODULE: for (Manifest m : manifests) {
            String cnb = findCNB(m);
            if (requiredBySomeone.contains(cnb)) {
                continue;
            }
            Attributes attr = m.getMainAttributes();
            for (String marker : markers) {
                if ("true".equals(attr.getValue(marker))) {
                    continue MODULE;
                }
            }
            auVisibilityProblems.append("\n" + cnb);
        }
        if (auVisibilityProblems.length() > 0) {
            fail("Some regular modules (that no one depends on) neither AutoUpdate-Show-In-Client nor AutoUpdate-Essential-Module" + auVisibilityProblems);
        }
    }

    public void testDisabledAutoloads() throws Exception {
        Set<Manifest> manifests = loadManifests();
        Set<String> permittedDisabledAutoloads = new HashSet<String>();
        // org.netbeans.lib.terminalemulator is really unused in cnd cluster, yet apparently has to be in the build anyway; contact Thomas Preisler
        permittedDisabledAutoloads.add("org.netbeans.lib.terminalemulator");
        // some pseudomodules used only by tests
        permittedDisabledAutoloads.add("org.netbeans.modules.jellytools");
        permittedDisabledAutoloads.add("org.netbeans.modules.jellytools.platform");
        permittedDisabledAutoloads.add("org.netbeans.modules.jemmy");
        permittedDisabledAutoloads.add("org.netbeans.modules.nbjunit");
        permittedDisabledAutoloads.add("org.netbeans.modules.visualweb.gravy");
        // really unused, yet kept in build for tutorials
        permittedDisabledAutoloads.add("org.netbeans.modules.lexer.editorbridge");
        // for compatibility
        permittedDisabledAutoloads.add("org.openide.util.enumerations");
        // for use by developers
        permittedDisabledAutoloads.add("org.netbeans.spi.actions.support");
        SortedMap<String,SortedSet<String>> problems = ConsistencyVerifier.findInconsistencies(manifests, permittedDisabledAutoloads);
        if (!problems.isEmpty()) {
            StringBuilder message = new StringBuilder("Problems found with autoloads");
            for (Map.Entry<String, SortedSet<String>> entry : problems.entrySet()) {
                message.append("\nProblems found for module " + entry.getKey() + ": " + entry.getValue());
            }
            fail(message.toString());
        }
    }

    private static Set<Manifest> loadManifests() throws Exception {
        // Intentionally does _not_ use NbModuleSuite, as that tries to make all modules regular modules!
        File util = new File(Lookup.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        assertTrue("Util exists: " + util, util.exists());
        File install = util.getParentFile().getParentFile().getParentFile();
        Set<Manifest> manifests = new HashSet<Manifest>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        for (File cluster : install.listFiles()) {
            File configModules = new File(cluster, "config/Modules");
            if (!configModules.isDirectory()) {
                continue;
            }
            for (File xml : configModules.listFiles()) {
                if (!xml.getName().endsWith(".xml")) {
                    continue;
                }
                Document doc = XMLUtil.parse(new InputSource(xml.toURI().toString()), false, false, null, new EntityResolver() {
                    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                        if (publicId.equals("-//NetBeans//DTD Module Status 1.0//EN")) {
                            return new InputSource(new ByteArrayInputStream(new byte[0]));
                        } else {
                            return null;
                        }
                    }
                });
                File jarFile = new File(cluster, xpath.evaluate("//param[@name=\'jar\']", doc));
                if (!jarFile.isFile()) {
                    // Common when processing ergonomics cluster.
                    continue;
                }
                try {
                    JarFile jar = new JarFile(jarFile);
                    try {
                        Manifest m = jar.getManifest();
                        for (String pseudoAttr : new String[] {"autoload", "eager"}) {
                            m.getMainAttributes().putValue(pseudoAttr, xpath.evaluate("//param[@name='" + pseudoAttr + "']", doc));
                        }
                        manifests.add(m);
                    } finally {
                        jar.close();
                    }
                } catch (IOException x) {
                    throw (IOException) new IOException("Could not open " + jarFile + ": " + x).initCause(x);
                }
            }
            // The following have no associated config/Modules/*.xml:
            for (String special : new String[] {"lib", "core"}) {
                File dir = new File(cluster, special);
                if (!dir.isDirectory()) {
                    continue;
                }
                for (File jarFile : dir.listFiles()) {
                    if (!jarFile.getName().endsWith(".jar")) {
                        continue;
                    }
                    try {
                        JarFile jar = new JarFile(jarFile);
                        try {
                            Manifest m = jar.getManifest();
                            if (m.getMainAttributes().getValue("OpenIDE-Module") != null) {
                                manifests.add(m);
                            }
                        } finally {
                            jar.close();
                        }
                    } catch (IOException x) {
                        throw (IOException) new IOException("Could not open " + jarFile + ": " + x).initCause(x);
                    }
                }
            }
        }
        for (Manifest m : manifests) {
            // Could not actually load the bundle anyway, so would just throw an exception:
            m.getMainAttributes().remove(new Attributes.Name("OpenIDE-Module-Localizing-Bundle"));
            // Public packages would be ignored anyway:
            m.getMainAttributes().remove(new Attributes.Name("OpenIDE-Module-Friends"));
        }
        return manifests;
    }

    private static String findCNB(Manifest m) {
        String name = m.getMainAttributes().getValue("OpenIDE-Module");
        if (name == null) {
            throw new IllegalArgumentException();
        }
        return name.replaceFirst("/\\d+$", "");
    }

}
