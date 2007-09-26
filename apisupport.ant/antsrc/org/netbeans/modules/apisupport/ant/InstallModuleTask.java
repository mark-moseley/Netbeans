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

package org.netbeans.modules.apisupport.ant;

import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.netbeans.core.startup.TestModuleDeployer;

// Note that Ant tasks in general are not internationalized.

public class InstallModuleTask extends Task {

    private File module = null;
    private String action = null;

    public static class Action extends EnumeratedAttribute {
        public String[] getValues () {
            return new String[] { /*XXX: "install", "uninstall",*/ "reinstall" }; // NOI18N
        }
    }

    public void setModule (File f) {
        module = f;
    }

    public void setAction (Action a) {
        action = a.getValue ();
    }

    public void execute () throws BuildException {
        if (module == null) throw new BuildException ("Required attribute: module", getLocation()); // NOI18N
        if (action == null) throw new BuildException ("Required attribute: action", getLocation()); // NOI18N
        try {
            if (action.equals ("reinstall")) { // NOI18N
                TestModuleDeployer.deployTestModule(module);
            } else {
                throw new BuildException ("Unsupported action: " + action, getLocation()); // NOI18N
            }
        } catch (IOException ioe) {
            throw new BuildException (ioe, getLocation());
        }
    }

}
