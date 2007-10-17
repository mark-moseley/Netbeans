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
package org.netbeans.performance.antext;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.taskdefs.*;
import java.util.*;
import java.io.File;

/** Call a target for each file in filesets
 * @author Jesse Glick, Tim Boudreau
 * @see CallTask
 */
public class ForEachFileTask extends Task {

    private String subTarget;
    public void setTarget(String t) {
        subTarget = t;
    }

    private List filesets = new LinkedList(); // List<FileSet>
    public void addFileset(FileSet fs) {
        filesets.add(fs); 
    }
    
    public static class Param {
        private String name, value; 
        public String getName() {return name;}
        public void setName(String s) {name = s;}
        public String getValue() {return value;}
        public void setValue(String s) {value = s;}
        public void setLocation(File f) {value = f.getAbsolutePath();}
    }
    private List properties = new LinkedList(); // List<Param>
    public void addParam(Param p) {
        properties.add(p);
    }
    
    public void execute() throws BuildException {
        if (subTarget == null) throw new BuildException("No subtarget set.");
        if (filesets.isEmpty()) throw new BuildException("No files to process - fileset is empty");
        Iterator it = filesets.iterator();
        while (it.hasNext()) {
            FileSet fs = (FileSet)it.next();
            DirectoryScanner ds = fs.getDirectoryScanner(project);
            File basedir = ds.getBasedir();
            String[] files = ds.getIncludedFiles();
            for (int i = 0; i < files.length; i++) {
                Ant callee = (Ant)project.createTask("ant");
                callee.setOwningTarget(target);
                callee.setTaskName(getTaskName());
                callee.setLocation(location);
                callee.init();
                Property p=callee.createProperty();
                p.setName ("configfile");
                File f = new File(basedir, files[i]);
                p.setLocation(f);
                
                p=callee.createProperty();
                p.setName ("configfile_unqualified");
                p.setValue(files[i]);
                
                System.out.println("Testing config file " + files[i]);
                Iterator props = properties.iterator();
                while (props.hasNext()) {
                    Param p1 = (Param)props.next();
                    Property p2 = callee.createProperty();
                    p2.setName(p1.getName());
                    p2.setValue(p1.getValue());
                }
                callee.setDir(project.getBaseDir());
                callee.setAntfile(project.getProperty("ant.file"));
                callee.setTarget(subTarget);
                //callee.setInheritAll(true);
                callee.execute();
            }
        }
    }
    
}
