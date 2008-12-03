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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Task which checks content of an update center to make sure module dependencies
 * are internally consistent. Can optionally also check against a previous update
 * center snapshot to make sure that updates of modules marked as newer (i.e. with
 * newer specification versions) would result in a consistent snapshot as well.
 * If there are any modules which cannot be loaded, the build fails with a description.
 * <p>
 * Actual NBMs are not downloaded. Everything necessary is present just in
 * the update center XML descriptor.
 * <p>
 * You must specify a classpath to load the NB module system from.
 * It should suffice to include those JARs in the NB platform cluster's <code>lib</code> folder.
 * @author Jesse Glick
 */
public final class VerifyUpdateCenter extends Task {

    public VerifyUpdateCenter() {}

    private URI updates;
    public void setUpdates(File f) {
        updates = f.toURI();
    }
    public void setUpdatesURL(URI u) {
        updates = u;
    }

    private URI oldUpdates;
    public void setOldUpdates(File f) {
        if (f.isFile()) {
            oldUpdates = f.toURI();
        } else {
            log("No such file: " + f, Project.MSG_WARN);
        }
    }
    public void setOldUpdatesURL(URI u) {
        if (u.toString().length() > 0) {
            oldUpdates = u;
        }
    }

    private Set<String> disabledAutoloads;
    /**
     * Comma/space-separated list of CNBs of autoloads which are expected to be disabled.
     * Generally should refer to "placeholder" modules which need to be installed but have no runtime component.
     * If specified (even if empty), any other autoloads which are not enabled result in a build failure.
     */
    public void setDisabledAutoloads(String s) {
        disabledAutoloads = new HashSet<String>(Arrays.asList(s.split("[, ]+")));
    }

    private boolean checkAutoUpdateVisibility;
    /** Turn on to check that all regular modules are marked for AU as either visible or essential. */
    public void setCheckAutoUpdateVisibility(boolean checkAutoUpdateVisibility) {
        this.checkAutoUpdateVisibility = checkAutoUpdateVisibility;
    }

    private Path classpath = new Path(getProject());
    public void addConfiguredClasspath(Path p) {
        classpath.append(p);
    }

    private File reportFile;
    /** JUnit-format XML result file to generate, rather than halting the build. */
    public void setReport(File report) {
        this.reportFile = report;
    }

    public @Override void execute() throws BuildException {
        if (updates == null) {
            throw new BuildException("you must specify updates");
        }
        Map<String,String> pseudoTests = new LinkedHashMap<String,String>();
        ClassLoader loader = new AntClassLoader(getProject(), classpath);
        Set<Manifest> manifests = loadManifests(updates);
        if (checkAutoUpdateVisibility) {
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
            pseudoTests.put("testAutoUpdateVisibility", auVisibilityProblems.length() > 0 ?
                "Some regular modules (that no one depends on) neither AutoUpdate-Show-In-Client nor AutoUpdate-Essential-Module" + auVisibilityProblems : null);
        }
        checkForProblems(findInconsistencies(manifests, loader, disabledAutoloads), "Inconsistency(ies) in " + updates, "synchronicConsistency", pseudoTests);
        if (pseudoTests.get("synchronicConsistency") == null) {
            log(updates + " is internally consistent", Project.MSG_INFO);
            if (oldUpdates != null) {
                Map<String,Manifest> updated = new HashMap<String,Manifest>();
                for (Manifest m : loadManifests(oldUpdates)) {
                    updated.put(findCNB(m), m);
                }
                if (!findInconsistencies(new HashSet<Manifest>(updated.values()), loader, null).isEmpty()) {
                    log(oldUpdates + " is already inconsistent, skipping update check", Project.MSG_WARN);
                    JUnitReportWriter.writeReport(this, null, reportFile, pseudoTests);
                    return;
                }
                SortedSet<String> updatedCNBs = new TreeSet<String>();
                Set<String> newCNBs = new HashSet<String>();
                for (Manifest m : manifests) {
                    String cnb = findCNB(m);
                    newCNBs.add(cnb);
                    boolean doUpdate = true;
                    Manifest old = updated.get(cnb);
                    if (old != null) {
                        String oldspec = old.getMainAttributes().getValue("OpenIDE-Module-Specification-Version");
                        String newspec = m.getMainAttributes().getValue("OpenIDE-Module-Specification-Version");
                        doUpdate = specGreaterThan(newspec, oldspec);
                    }
                    if (doUpdate) {
                        updated.put(cnb, m);
                        updatedCNBs.add(cnb);
                    }
                }
                SortedMap<String,SortedSet<String>> updateProblems = findInconsistencies(new HashSet<Manifest>(updated.values()), loader, null);
                updateProblems.keySet().retainAll(newCNBs); // ignore problems in now-deleted modules
                checkForProblems(updateProblems, "Inconsistency(ies) in " + updates + " relative to " + oldUpdates, "diachronicConsistency", pseudoTests);
                if (pseudoTests.get("diachronicConsistency") == null) {
                    log(oldUpdates + " after updating " + updatedCNBs + " from " + updates + " remains consistent");
                }
            }
        }
        JUnitReportWriter.writeReport(this, null, reportFile, pseudoTests);
    }

    @SuppressWarnings("unchecked")
    private SortedMap<String,SortedSet<String>> findInconsistencies(Set<Manifest> manifests, ClassLoader loader, Set<String> disabledAutoloads) throws BuildException {
        try {
            return (SortedMap) loader.loadClass("org.netbeans.core.startup.ConsistencyVerifier").
                    getMethod("findInconsistencies", Set.class, Set.class).invoke(null, manifests, disabledAutoloads);
        } catch (Exception x) {
            throw new BuildException(x, getLocation());
        }
    }

    private Set<Manifest> loadManifests(URI u) throws BuildException {
        try {
            Document doc = XMLUtil.parse(new InputSource(u.toString()), false, false, null, new EntityResolver() {
                public InputSource resolveEntity(String pub, String sys) throws SAXException, IOException {
                    if (pub.contains("DTD Autoupdate Catalog")) {
                        return new InputSource(new StringReader(""));
                    } else {
                        return null;
                    }
                }
            });
            Set<Manifest> manifests = new HashSet<Manifest>();
            NodeList nl = doc.getElementsByTagName("manifest");
            for (int i = 0; i < nl.getLength(); i++) {
                Element m = (Element) nl.item(i);
                Manifest mani = new Manifest();
                NamedNodeMap map = m.getAttributes();
                for (int j = 0; j < map.getLength(); j++) {
                    Attr a = (Attr) map.item(j);
                    mani.getMainAttributes().putValue(a.getName(), a.getValue());
                }
                Element module = (Element) m.getParentNode();
                for (String pseudoAttr : new String[] {"autoload", "eager"}) {
                    if (module.getAttribute(pseudoAttr).equals("true")) {
                        mani.getMainAttributes().putValue(pseudoAttr, "true");
                    }
                }
                manifests.add(mani);
            }
            return manifests;
        } catch (Exception x) {
            throw new BuildException("Could not load " + u, x, getLocation());
        }
    }

    private static String findCNB(Manifest m) {
        String name = m.getMainAttributes().getValue("OpenIDE-Module");
        if (name == null) {
            throw new IllegalArgumentException();
        }
        return name.replaceFirst("/\\d+$", "");
    }

    private static boolean specGreaterThan(String newspec, String oldspec) {
        if (newspec == null) {
            return false;
        }
        if (oldspec == null) {
            return true;
        }
        String[] olddigits = oldspec.split("\\.");
        String[] newdigits = newspec.split("\\.");
        int oldlen = olddigits.length;
        int newlen = newdigits.length;
        int max = Math.max(oldlen, newlen);
        for (int i = 0; i < max; i++) {
            int oldd = (i < oldlen) ? Integer.parseInt(olddigits[i]) : 0;
            int newd = (i < newlen) ? Integer.parseInt(newdigits[i]) : 0;
            if (oldd != newd) {
                return newd > oldd;
            }
        }
        return false;
    }

    private void checkForProblems(SortedMap<String,SortedSet<String>> problems, String msg, String testName, Map<String,String> pseudoTests) {
        if (!problems.isEmpty()) {
            StringBuffer message = new StringBuffer(msg);
            for (Map.Entry<String, SortedSet<String>> entry : problems.entrySet()) {
                message.append("\nProblems found for module " + entry.getKey() + ": " + entry.getValue());
            }
            pseudoTests.put(testName, message.toString());
        } else {
            pseudoTests.put(testName, null);
        }
    }

}
