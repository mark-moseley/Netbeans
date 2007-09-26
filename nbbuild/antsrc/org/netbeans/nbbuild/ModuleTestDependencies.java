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
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Produces a list of all compile-time dependencies between unit tests of modules.
 */
public class ModuleTestDependencies extends Task {

    private File output;

    /**
     * Configures the output file.
     * @param output a file to generate
     */
    public void setOutput(File output) {
        this.output = output;
    }

    /**
     * Runs the task.
     * @throws BuildException for the usual reasons
     */
    public @Override void execute() throws BuildException {
        try {
            @SuppressWarnings("unchecked")
            Hashtable<String,String> props = getProject().getProperties();
            ModuleListParser mlp = new ModuleListParser(props, ParseProjectXml.TYPE_NB_ORG, getProject());
            SortedMap<String,SortedSet<String>> deps = new TreeMap<String,SortedSet<String>>();
            File nball = new File(props.get("nb_all"));
            for (ModuleListParser.Entry entry : mlp.findAll()) {
                String myCnb = entry.getCnb();
                String myCluster = entry.getClusterName().replaceFirst("\\d+$", "");
                if (myCluster.equals("extra")) {
                    continue;
                }
                String myCnbAndCluster = myCnb + " (" + myCluster + ")";
                File projectXml = new File(nball, (entry.getNetbeansOrgPath() + "/nbproject/project.xml").replace('/', File.separatorChar));
                Document pDoc = XMLUtil.parse(new InputSource(projectXml.toURI().toString()), false, true, /*XXX*/null, null);
                Element config = getConfig(projectXml, pDoc);
                Element td = ParseProjectXml.findNBMElement(config, "test-dependencies");
                if (td != null) {
                    for (Element depGroup : XMLUtil.findSubElements(td)) {
                        String testType = ParseProjectXml.findTextOrNull(depGroup, "name");
                        if (testType != null && !testType.equals(ParseProjectXml.TestDeps.UNIT)) {
                            continue;
                        }
                        for (Element dep : XMLUtil.findSubElements(depGroup)) {
                            if (ParseProjectXml.findNBMElement(dep, "test") == null) {
                                continue;
                            }
                            if (ParseProjectXml.findNBMElement(dep, "compile-dependency") == null) {
                                continue;
                            }
                            String targetCnb = ParseProjectXml.findTextOrNull(dep, "code-name-base");
                            if (targetCnb.equals(myCnb)) {
                                continue;
                            }
                            SortedSet<String> target = deps.get(myCnbAndCluster);
                            if (target == null) {
                                target = new TreeSet<String>();
                                deps.put(myCnbAndCluster, target);
                            }
                            String targetCluster = mlp.findByCodeNameBase(targetCnb).getClusterName().replaceFirst("\\d+$", "");
                            target.add(targetCnb + " (" + targetCluster + ")");
                        }
                    }
                }
            }
            log("Generating test dependencies to " + output);
            PrintWriter pw = new PrintWriter(output);
            for (Map.Entry<String,SortedSet<String>> entry : deps.entrySet()) {
                pw.printf("MODULE %s\n", entry.getKey());
                for (String dep : entry.getValue()) {
                    pw.printf("  REQUIRES %s\n", dep);
                }
            }
            pw.flush();
            pw.close();
        } catch (Exception x) {
            throw new BuildException(x);
        }
    }

    private Element getConfig(File projectXml, Document pDoc) throws BuildException {
        Element e = pDoc.getDocumentElement();
        Element c = XMLUtil.findElement(e, "configuration", ParseProjectXml.PROJECT_NS);
        if (c == null) {
            throw new BuildException("No <configuration>", getLocation());
        }
        Element d = ParseProjectXml.findNBMElement(c, "data");
        if (d == null) {
            throw new BuildException("No <data> in " + projectXml, getLocation());
        }
        return d;
    }
    
}
