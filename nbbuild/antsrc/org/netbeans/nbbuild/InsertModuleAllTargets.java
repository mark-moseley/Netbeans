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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.CallTarget;
import org.apache.tools.ant.taskdefs.Echo;
import org.apache.tools.ant.taskdefs.Property;

/**
 * Insert into the live project some all-* targets.
 * This means they do not need to manually written into nbbuild/build.xml.
 * Targets are only added for projectized modules which do not already
 * have all-* entries in the build script.
 * Generally, an all-* target for a projectized module in a named cluster depends on:
 * 1. 'init'
 * 2. The all-* targets for any module which it lists as <build-prerequisite/>s
 *    in project.xml, but which are not included in the same cluster as this module.
 * Therefore cluster dependencies still have to be manually set.
 * An all-* target for a projectized module without a specific cluster depends on:
 * 1. 'init'
 * 2. The all-* targets for any module which it lists as <build-prerequisite/>s
 *    in project.xml.
 * cluster.properties must have already been read for this task to work.
 * @author Jesse Glick
 */
public final class InsertModuleAllTargets extends Task {
    
    public InsertModuleAllTargets() {}
    
    boolean checkModules = false;
    public void setCheckModules( boolean check ) {
        checkModules = check;
    }

    public void execute() throws BuildException {
        try {
            Project project = getProject();
            @SuppressWarnings("unchecked")
            Set<String> existingTargets = project.getTargets().keySet();
            if (existingTargets.contains("all-openide/util")) {
                log("Already seem to have inserted targets into this project; will not do it twice", Project.MSG_VERBOSE);
                return;
            }
            @SuppressWarnings("unchecked")
            Hashtable<String,String> props = project.getProperties();

            if (checkModules) {
                boolean missingModules = false;
                String[] clusters = props.get("nb.clusters.list").split(", *");
                String nb_all = props.get("nb_all");
                if (nb_all == null)
                    throw new BuildException("Can't file 'nb_all' property, probably not in the NetBeans build system");
                File nbRoot = new File(nb_all);
                for( String cluster: clusters) {
                    if (props.get(cluster) == null) 
                        throw new BuildException("Cluster '"+cluster+"' has got empty list of modules. Check configuration of that cluster.",getLocation());
                    String[] clusterModules = props.get(cluster).split(", *");
                    for( String module: clusterModules) {
                        File moduleBuild = new File(nbRoot, module + File.separator + "build.xml");
                        if (!moduleBuild.exists() || !moduleBuild.isFile()) {
                            missingModules = true;
                            log("This module is missing from checkout: " + module + " - at least can't find: " + moduleBuild.getAbsolutePath());
                        }
                    }
                }
                if (missingModules) {
                    String clusterConfig = props.get("cluster.config");
                    throw new BuildException("Some modules according your cluster config '" + clusterConfig + "' are missing from checkout, see messages above.",getLocation());
                }
            }
            
            Map<String,String> clustersOfModules = new HashMap<String,String>();
            for (Map.Entry<String,String> pair : props.entrySet()) {
                String cluster = pair.getKey();
                if (!cluster.startsWith("nb.cluster.") || cluster.endsWith(".depends") || cluster.endsWith(".dir")) {
                    continue;
                }
                for (String module : pair.getValue().split(", *")) {
                    clustersOfModules.put(module, cluster);
                }
            }
            ModuleListParser mlp = new ModuleListParser(props, ParseProjectXml.TYPE_NB_ORG, project);
            SortedMap<String,ModuleListParser.Entry> entries = new TreeMap<String,ModuleListParser.Entry>();
            for (ModuleListParser.Entry entry : mlp.findAll()) {
                String path = entry.getNetbeansOrgPath();
                assert path != null : entry;
                entries.put(path, entry);
            }
           
            for (ModuleListParser.Entry entry : entries.values()) {
                String path = entry.getNetbeansOrgPath();
                assert path != null : entry;
                String target = "all-" + path;
                if (existingTargets.contains(target)) {
                    log("Not adding target " + target + " because one already exists", Project.MSG_INFO);
                    continue;
                }
                String[] prereqsAsCnb = entry.getBuildPrerequisites();
                StringBuffer namedDeps = new StringBuffer("init");
                String myCluster = clustersOfModules.get(path);
                for (String cnb : prereqsAsCnb ) {
                    ModuleListParser.Entry other = mlp.findByCodeNameBase(cnb);
                    if (other == null) {
                        log("Cannot find build prerequisite " + cnb + " of " + entry, Project.MSG_WARN);
                        continue;
                    }
                    String otherPath = other.getNetbeansOrgPath();
                    assert otherPath != null : other;
                    String otherCluster = clustersOfModules.get(otherPath);
                    if (myCluster == null || otherCluster == null || myCluster.equals(otherCluster)) {
                        namedDeps.append(",all-");
                        namedDeps.append(otherPath);
                    }
                }
                String namedDepsS = namedDeps.toString();
                log("Adding target " + target + " with depends=\"" + namedDepsS + "\"", Project.MSG_VERBOSE);
                Target t = new Target();
                t.setName(target);
                t.setLocation(getLocation());
                t.setDepends(namedDepsS);
                project.addTarget(t);
                if (myCluster != null) {
                    CallTarget call = (CallTarget) project.createTask("antcall");
                    call.setTarget("build-one-cluster-dependencies");
                    call.setInheritAll(false);
                    Property param = call.createParam();
                    param.setName("one.cluster.dependencies");
                    param.setValue(props.get(myCluster + ".depends"));
                    param = call.createParam();
                    param.setName("one.cluster.name");
                    param.setValue("this-cluster");
                    t.addTask(call);
                }
                Echo echo = (Echo) project.createTask("echo");
                echo.setMessage("Building " + path + "...");
                t.addTask(echo);
                Ant ant = (Ant) project.createTask("ant");
                ant.setDir(project.resolveFile("../" + path));
                ant.setTarget("netbeans");
                t.addTask(ant);
            }
        } catch (IOException e) {
            throw new BuildException(e.toString(), e, getLocation());
        }
    }
    
}
