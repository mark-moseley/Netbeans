/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Analyzes build.properties and cluster.properties and tries to diagnose any problems.
 * Also produces a summary of moduleconfig contents which is written to a golden file.
 * @author Jesse Glick
 */
public final class CheckModuleConfigs extends Task {
    
    private File nbroot;
    
    public CheckModuleConfigs() {}
    
    public void setNbroot(File f) {
        nbroot = f;
    }
    
    public void execute() throws BuildException {
        if (nbroot == null) {
            throw new BuildException("Must define 'nbroot' param", getLocation());
        }
        File buildPropertiesFile = new File(nbroot, "nbbuild" + File.separatorChar + "build.properties");
        File clusterPropertiesFile = new File(nbroot, "nbbuild" + File.separatorChar + "cluster.properties");
        File goldenFile = new File(nbroot, "ide" + File.separatorChar + "golden" + File.separatorChar + "moduleconfigs.txt");
        File masterProjectXml = new File(nbroot, "nbbuild" + File.separatorChar + "nbproject" + File.separatorChar + "project.xml");
        Map/*<String,String>*/ properties = getProject().getProperties();
        Map/*<String,Set<String>>*/ configs = loadModuleConfigs(properties, buildPropertiesFile);
        Map/*<String,Set<String>>*/ clusters = loadModuleClusters(properties, clusterPropertiesFile);
        Set/*<String>*/ allClusterModules = new TreeSet();
        Iterator it = clusters.values().iterator();
        while (it.hasNext()) {
            allClusterModules.addAll((Set) it.next());
        }
        try {
            writeModuleConfigs(goldenFile, configs, buildPropertiesFile);
        } catch (IOException e) {
            throw new BuildException("Could not write to " + goldenFile, e, getLocation());
        }
        try {
            writeMasterProjectXml(masterProjectXml, allClusterModules);
        } catch (SAXException e) {
            throw new BuildException("Could not write to " + masterProjectXml, e, getLocation());
        } catch (IOException e) {
            throw new BuildException("Could not write to " + masterProjectXml, e, getLocation());
        }
        // Check that stable != daily-alpha-nbms:
        Set/*<String>*/ s = new TreeSet((Set) configs.get("stable"));
        s.retainAll((Set) configs.get("daily-alpha-nbms"));
        if (!s.isEmpty()) {
            log(buildPropertiesFile + ": warning: stable and daily-alpha-nbms configs overlap: " + s);
        }
        // Check that stable-au <= daily-alpha-nbms (is this necessary BTW?):
        s = new TreeSet((Set) configs.get("stable-au"));
        s.removeAll((Set) configs.get("daily-alpha-nbms"));
        if (!s.isEmpty()) {
            log(buildPropertiesFile + ": warning: stable-au config contains entries not in daily-alpha-nbms config: " + s);
        }
        /* This is not actually desired; just includes everything:
        // Check that sigtest <= javadoc:
        s = new TreeSet((Set) configs.get("sigtest"));
        s.removeAll((Set) configs.get("javadoc"));
        if (!s.isEmpty()) {
            log(buildPropertiesFile + ": warning: sigtest config contains entries not in javadoc config: " + s);
        }
        */
        // Check that platform-javadoc <= javadoc:
        s = new TreeSet((Set) configs.get("platform-javadoc"));
        s.removeAll((Set) configs.get("javadoc"));
        if (!s.isEmpty()) {
            log(buildPropertiesFile + ": warning: platform-javadoc config contains entries not in javadoc config: " + s);
        }
        // Check that javadoc <= stable + daily-alpha-nbms:
        s = new TreeSet((Set) configs.get("javadoc"));
        s.removeAll((Set) configs.get("stable"));
        s.removeAll((Set) configs.get("daily-alpha-nbms"));
        if (!s.isEmpty()) {
            log(buildPropertiesFile + ": warning: javadoc config contains entries not in stable and daily-alpha-nbms configs: " + s);
        }
        // Check that platform-javadoc = javadoc where module in platform cluster:
        Set/*<String>*/ platformJavadoc = (Set) configs.get("platform-javadoc");
        Set/*<String>*/ platformClusterJavadoc = (Set) configs.get("javadoc");
        platformClusterJavadoc.retainAll((Set) clusters.get("nb.cluster.platform"));
        s = new TreeSet(platformJavadoc);
        s.removeAll(platformClusterJavadoc);
        if (!s.isEmpty()) {
            log(buildPropertiesFile + ": warning: platform-javadoc config not equal to javadoc config for platform cluster modules: " + s);
        }
        s = new TreeSet(platformClusterJavadoc);
        s.removeAll(platformJavadoc);
        if (!s.isEmpty()) {
            log(buildPropertiesFile + ": warning: platform-javadoc config not equal to javadoc config restricted to platform cluster modules: " + s);
        }
        // Check that stable = modules in enumerated clusters:
        Set/*<String>*/ stable = (Set) configs.get("stable");
        s = new TreeSet(stable);
        s.removeAll(allClusterModules);
        if (!s.isEmpty()) {
            log(buildPropertiesFile + ": warning: stable config not equal to listed cluster modules: " + s);
        }
        s = new TreeSet(allClusterModules);
        s.removeAll(stable);
        if (!s.isEmpty()) {
            log(buildPropertiesFile + ": warning: stable config not equal to listed cluster modules: " + s);
        }
        // Check that platform = modules in platform cluster:
        Set/*<String>*/ platform = (Set) configs.get("platform");
        Set/*<String>*/ platformCluster = (Set) clusters.get("nb.cluster.platform");
        s = new TreeSet(platform);
        s.removeAll(platformCluster);
        if (!s.isEmpty()) {
            log(buildPropertiesFile + ": warning: platform config not equal to platform cluster modules: " + s);
        }
        s = new TreeSet(platformCluster);
        s.removeAll(platform);
        if (!s.isEmpty()) {
            log(buildPropertiesFile + ": warning: platform config not equal to platform cluster modules: " + s);
        }
    }
    
    private Set/*<String>*/ split(String list) {
        return new HashSet(Collections.list(new StringTokenizer(list, ", ")));
    }
    
    private Map/*<String,Set<String>>*/ loadModuleConfigs(Map/*<String,String>*/ buildProperties, File buildPropertiesFile) {
        Map/*<String,Set<String>>*/ configs = new TreeMap();
        Iterator it = buildProperties.keySet().iterator();
        while (it.hasNext()) {
            String k = (String) it.next();
            String prefix = "config.modules.";
            if (!k.startsWith(prefix)) {
                continue;
            }
            String config = k.substring(prefix.length());
            Set/*<String>*/ modules = new TreeSet(split((String) buildProperties.get(k)));
            String fixedK = "config.fixedmodules." + config;
            String fixed = (String) buildProperties.get(fixedK);
            if (fixed != null) {
                modules.addAll(split(fixed));
            } else {
                log(buildPropertiesFile + ": warning: have " + k + " but no " + fixedK, Project.MSG_WARN);
            }
            configs.put(config, modules);
        }
        return configs;
    }

    private void writeModuleConfigs(File goldenFile, Map/*<String,Set<String>>*/ configs, File buildPropertiesFile) throws IOException {
        log("Writing moduleconfigs " + configs.keySet() + " from " + buildPropertiesFile + " to " + goldenFile);
        Writer w = new FileWriter(goldenFile); // default encoding OK
        try {
            PrintWriter pw = new PrintWriter(w);
            pw.println("# To update, run: ant -f nbbuild/build.xml check-module-configs");
            Iterator it = configs.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String config = (String) entry.getKey();
                Set/*<String>*/ modules = (Set) entry.getValue();
                Iterator it2 = modules.iterator();
                while (it2.hasNext()) {
                    String module = (String) it2.next();
                    pw.println(config + ':' + module);
                }
            }
            pw.flush();
        } finally {
            w.close();
        }
    }

    private Map/*<String,Set<String>>*/ loadModuleClusters(Map/*<String,String>*/ clusterProperties, File clusterPropertiesFile) {
        String l = (String) clusterProperties.get("nb.clusters.list");
        if (l == null) {
            log(clusterPropertiesFile + ": warning: no definition for nb.clusters.list", Project.MSG_WARN);
            return Collections.EMPTY_MAP;
        }
        Map/*<String,Set<String>>*/ clusters = new TreeMap();
        Iterator it = split(l).iterator();
        while (it.hasNext()) {
            String cluster = (String) it.next();
            l = (String) clusterProperties.get(cluster);
            if (l == null) {
                log(clusterPropertiesFile + ": warning: no definition for " + cluster, Project.MSG_WARN);
                continue;
            }
            clusters.put(cluster, new TreeSet(split(l)));
        }
        return clusters;
    }

    private void writeMasterProjectXml(File masterProjectXml, Set/*<String>*/ allClusterModules) throws IOException, SAXException {
        log("Writing module list  to " + masterProjectXml);
        Document doc = XMLUtil.parse(new InputSource(masterProjectXml.toURI().toString()), false, true, null, null);
        NodeList nl = doc.getElementsByTagName("subprojects");
        if (nl.getLength() != 1) {
            throw new IOException("No or multiple <subprojects>");
        }
        Element sp  = (Element) nl.item(0);
        nl = sp.getChildNodes();
        while (nl.getLength() > 0) {
            sp.removeChild(nl.item(0));
        }
        sp.appendChild(doc.createComment(" To update, run: ant -f nbbuild/build.xml check-module-configs "));
        Iterator it = allClusterModules.iterator();
        while (it.hasNext()) {
            String module = (String) it.next();
            if (new File(nbroot, (module + "/nbproject/project.xml").replace('/', File.separatorChar)).isFile()) {
                Element e = doc.createElementNS("http://www.netbeans.org/ns/freeform-project/1", "project");
                e.appendChild(doc.createTextNode("../" + module));
                sp.appendChild(e);
            } else {
                sp.appendChild(doc.createComment(" Unprojectized: " + module + " "));
            }
        }
        OutputStream os = new FileOutputStream(masterProjectXml);
        try {
            XMLUtil.write(doc, os);
        } finally {
            os.close();
        }
    }
    
}
