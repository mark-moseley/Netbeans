/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.util;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 * Utility methods.
 * @author Tomas Mysik
 */
public final class PhpProjectUtils {

    private PhpProjectUtils() {
    }

    /**
     * Return <code>true</code> if the String is not <code>null</code>
     * and has any character after trimming.
     * @param input input String.
     * @return <code>true</code> if the String is not <code>null</code>
     *         and has any character after trimming.
     */
    public static boolean hasText(String input) {
        return input != null && input.trim().length() > 0;
    }

    /**
     * Get a PHP project for the given node.
     * @return a PHP project or <code>null</code>.
     */
    public static PhpProject getPhpProject(Node node) {
        return getPhpProject(CommandUtils.getFileObject(node));
    }

    /**
     * Get a PHP project for the given FileObject.
     * @return a PHP project or <code>null</code>.
     */
    public static PhpProject getPhpProject(FileObject fo) {
        assert fo != null;

        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null) {
            return null;
        }
        return project.getLookup().lookup(PhpProject.class);
    }
}
