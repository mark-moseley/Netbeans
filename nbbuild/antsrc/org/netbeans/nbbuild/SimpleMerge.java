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
import java.util.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;

/** Copies content of subdirectories of defined name from set of directories
 * to a certain location.
 *
 * @author Jesse Glick, Rudolf Balada
 *
 * Copied and changed from NbMerge.java
 */
public class SimpleMerge extends Task {
    
    private File dest;
    private List<String> modules = new ArrayList<String>();
    private List<File> topdirs = new ArrayList<File>();
    private List<String> subdirs = new ArrayList<String>();
    
    /** Target directory to unpack to (top of IDE installation). */
    public void setDest (File f) {
        dest = f;
    }
    
    /** Comma-separated list of modules to include. */
    public void setModules (String s) {
        StringTokenizer tok = new StringTokenizer (s, ", ");
        modules = new ArrayList<String>();
        while (tok.hasMoreTokens ())
            modules.add(tok.nextToken ());
    }
    
    /** Set the top directory.
     * There should be subdirectories under this for each named module.
     */
    public void setTopdir (File t) {
        topdirs.add (t);
    }

    /** Nested topdir addition. */
    public class Topdir {
        /** Path to an extra topdir. */
        public void setPath (File t) {
            topdirs.add (t);
        }
    }
    /** Add a nested topdir.
     * If there is more than one topdir total, build products
     * may be taken from any of them, including from multiple places
     * for the same module. (Later topdirs may override build
     * products in earlier topdirs.)
     */
    public Topdir createTopdir () {
        return new Topdir ();
    }

    /** Set the subdirectory.
     * There should be these subdirectories in each named module.
     */
    public void setSubdir (String t) {
        subdirs.add (t);
    }

    /** Nested subdir addition. */
    public class Subdir {
        /** Path to an extra Subdir. */
        public void setPath (String t) {
            subdirs.add (t);
        }
    }
    /** Add a nested subdir.
     * If there is more than one subdir total, build products
     * may be taken from any of them, including from multiple places
     * for the same module. (Later subdirs may override build
     * products in earlier subdirs.)
     */
    public Subdir createSubdir () {
        return new Subdir ();
    }

    public void execute () throws BuildException {
        if (topdirs.isEmpty ()) {
            throw new BuildException("You must set at least one topdir attribute", getLocation());
        }
        
        if (subdirs.isEmpty ()) {
            throw new BuildException("You must set at least one subdir attribute", getLocation());
        }

        log ( "Starting merge to " + dest.getAbsolutePath() );
        for (File topdir : topdirs) {
            for (String module : modules) {
                for (String sdir : subdirs) {
                    File subdir = new File(new File(topdir, module), sdir);
                    if (!subdir.exists()) {
                        log("Dir " + subdir + " does not exist, skipping...", Project.MSG_WARN);
                        continue;
                    }
                    Copy copy = (Copy) getProject().createTask("copy");
                    FileSet fs = new FileSet();
                    fs.setDir(subdir);
                    copy.addFileset(fs);
                    copy.setTodir(dest);
                    copy.setIncludeEmptyDirs(true);
                    copy.init();
                    copy.setLocation(getLocation());
                    copy.execute();
                }
            }
        }
        log ( "Merge finished" );
    }
}
