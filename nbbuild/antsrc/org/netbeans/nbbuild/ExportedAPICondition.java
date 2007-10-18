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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.nbbuild;

import java.io.IOException;
import java.util.Hashtable;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.taskdefs.condition.Condition;

/**
 * Condition that determines whether a given module exports an API.
 * This is true if any of the following hold:
 * <ol>
 * <li>The module lists public API packages.
 * <li>The module lists friend API packages, and at least one friend is external (not known in module universe).
 * <li>The module lists friend API packages, and at least one friend is located in a different cluster.
 * </ol>
 * This condition must be called from a module build, which is why it accepts no parameters:
 * various properties are picked up from the project. Only works for nb.org modules.
 */
public class ExportedAPICondition extends ProjectComponent implements Condition {

    public boolean eval() throws BuildException {
        @SuppressWarnings("unchecked")
        Hashtable<String,String> props = getProject().getProperties();
        if (props.get("public.packages").equals("-")) {
            log("No exported packages", Project.MSG_VERBOSE);
            return false;
        }
        String friends = props.get("friends");
        if (friends == null) {
            log("Public API", Project.MSG_VERBOSE);
            return true;
        }
        ModuleListParser mlp;
        try {
            mlp = new ModuleListParser(props, ParseProjectXml.TYPE_NB_ORG, getProject());
        } catch (IOException x) {
            throw new BuildException(x, getLocation());
        }
        String mycluster = mlp.findByCodeNameBase(props.get("code.name.base.dashes").replace('-', '.')).getClusterName();
        for (String friend : friends.split(", ")) {
            ModuleListParser.Entry entry = mlp.findByCodeNameBase(friend);
            if (entry == null) {
                log("External friend " + friend, Project.MSG_VERBOSE);
                return true;
            }
            String cluster = entry.getClusterName();
            if (!mycluster.equals(cluster)) {
                log("Friend " + friend + " is in cluster " + cluster + " rather than " + mycluster, Project.MSG_VERBOSE);
                return true;
            }
        }
        log("No friends outside cluster", Project.MSG_VERBOSE);
        return false;
    }

}
