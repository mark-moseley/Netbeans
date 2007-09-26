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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Task to sort the list of modules in a suite by their declared build dependencies.
 * @author Jesse Glick
 */
public class SortSuiteModules extends Task {
    private boolean sortTests;
    private Path unsortedModules;
    /**
     * Set a list of modules in the suite.
     * Each entry should be a project base directory.
     */
    public void setUnsortedModules(Path unsortedModules) {
        this.unsortedModules = unsortedModules;
    }
    
    private String sortedModulesProperty;
    /**
     * Set a property name in which to store a sorted path of module base directories.
     */
    public void setSortedModulesProperty(String sortedModulesProperty) {
        this.sortedModulesProperty = sortedModulesProperty;
    }
    
    /** Is enabled sorting test dependencies?
     */
    public boolean isSortTests() {
        return sortTests;
    }

    /** Enable or disable sorting test dependenciens. Default value is false.
     */
    public void setSortTests(boolean sortTests) {
        this.sortTests = sortTests;
    }
    
    public SortSuiteModules() {}
    
    public void execute() throws BuildException {
        if (unsortedModules == null) {
            throw new BuildException("Must set unsortedModules");
        }
        if (sortedModulesProperty == null) {
            throw new BuildException("Must set sortedModulesProperty");
        }
        Map<String,File> basedirsByCNB = new TreeMap<String,File>();
        Map<String,List<String>> buildDeps = new HashMap<String,List<String>>();
        String[] pieces = unsortedModules.list();
        for (int i = 0; i < pieces.length; i++) {
            File d = new File(pieces[i]);
            File projectXml = new File(d, "nbproject" + File.separatorChar + "project.xml");
            if (!projectXml.isFile()) {
                throw new BuildException("Cannot open " + projectXml, getLocation());
            }
            Document doc;
            try {
                doc = XMLUtil.parse(new InputSource(projectXml.toURI().toString()), false, true, null, null);
            } catch (IOException e) {
                throw new BuildException("Error parsing " + projectXml + ": " + e, e, getLocation());
            } catch (SAXException e) {
                throw new BuildException("Error parsing " + projectXml + ": " + e, e, getLocation());
            }
            Element config = XMLUtil.findElement(doc.getDocumentElement(), "configuration", ParseProjectXml.PROJECT_NS);
            if (config == null) {
                throw new BuildException("Malformed project file " + projectXml, getLocation());
            }
            Element data = ParseProjectXml.findNBMElement(config, "data");
            if (data == null) {
                log("Skipping " + projectXml + " as it does not look like a module project", Project.MSG_WARN);
                continue;
            }
            Element cnbEl = ParseProjectXml.findNBMElement(data, "code-name-base");
            if (cnbEl == null) {
                throw new BuildException("Malformed project file " + projectXml, getLocation());
            }
            String cnb = XMLUtil.findText(cnbEl);
            basedirsByCNB.put(cnb, d);
            List<String> deps = new LinkedList<String>();
            Element depsEl = ParseProjectXml.findNBMElement(data, "module-dependencies");
            if (depsEl == null) {
                throw new BuildException("Malformed project file " + projectXml, getLocation());
            }
            Iterator it = XMLUtil.findSubElements(depsEl).iterator();
            while (it.hasNext()) {
                Element dep = (Element) it.next();
                if (ParseProjectXml.findNBMElement(dep, "build-prerequisite") == null) {
                    continue;
                }
                Element cnbEl2 = ParseProjectXml.findNBMElement(dep, "code-name-base");
                if (cnbEl2 == null) {
                    throw new BuildException("Malformed project file " + projectXml, getLocation());
                }
                String cnb2 = XMLUtil.findText(cnbEl2);
                deps.add(cnb2);
            }
            buildDeps.put(cnb, deps);
            
            // create test dependencies
            if (isSortTests()) {
                Element testDepsEl = ParseProjectXml.findNBMElement(data,"test-dependencies");
                if (testDepsEl != null) {
                    // <test-type>
                    Iterator itTType = XMLUtil.findSubElements(testDepsEl).iterator();
                    while (itTType.hasNext()) {
                        Iterator itt = XMLUtil.findSubElements((Element)itTType.next()).iterator();
                        while (itt.hasNext()) {
                            Element dep = (Element) itt.next();
                            if (ParseProjectXml.findNBMElement(dep, "test") == null) {
                                continue;
                            }
                            Element cnbEl2 = ParseProjectXml.findNBMElement(dep, "code-name-base");
                            if (cnbEl2 == null) {
                                throw new BuildException("No cobase found for test-dependency");
                            }
                            String cnb2 = XMLUtil.findText(cnbEl2);
                            deps.add(cnb2);
                        }
                    }
                }
            }
        }
        for (List<String> deps: buildDeps.values()) {
            deps.retainAll(basedirsByCNB.keySet());
        }
        // Stolen from org.openide.util.Utilities.topologicalSort, with various simplifications:
        List<String> cnbs = new ArrayList<String>();
        List<String> cRev = new ArrayList<String>(basedirsByCNB.keySet());
        Map<String,Boolean> finished = new HashMap<String,Boolean>();
        for (String s: cRev) {
            if (!visit(s, buildDeps, finished, cnbs)) {
                throw new BuildException("Cycles detected in dependency graph, cannot sort", getLocation());
            }
        }
        StringBuffer path = new StringBuffer();
        for (String cnb: cnbs) {
            assert basedirsByCNB.containsKey(cnb);
            if (path.length() > 0) {
                path.append(File.pathSeparatorChar);
            }
            path.append(basedirsByCNB.get(cnb).getAbsolutePath());
        }
        getProject().setNewProperty(sortedModulesProperty, path.toString());
    }
    
    private static <String> boolean visit(String node, Map<String,List<String>> edges, Map<String,Boolean> finished, List<String> r) {
        Boolean b = finished.get(node);
        if (b != null) {
            return b.booleanValue();
        }
        List<String> e = edges.get(node);
        if (e != null) {
            finished.put(node, Boolean.FALSE);
            for (String s: e) {
                if (!visit(s, edges, finished, r)) {
                    return false;
                }
            }
        }
        finished.put(node, Boolean.TRUE);
        r.add(node);
        return true;
    }
    
}
