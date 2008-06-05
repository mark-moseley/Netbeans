/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.utils.cli.options;

import org.netbeans.installer.utils.cli.*;
import java.io.File;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.exceptions.CLIOptionException;
import org.netbeans.installer.utils.cli.CLIArgumentsList;
import org.netbeans.installer.utils.helper.ExecutionMode;

/**
 *
 * @author Dmitry Lipin
 */
public class CreateBundleOption extends CLIOptionOneArgument {

    public void execute(CLIArgumentsList arguments) throws CLIOptionException {
        File targetFile = new File(arguments.next()).getAbsoluteFile();
        if (targetFile.exists()) {
            throw new CLIOptionException(ResourceUtils.getString(
                    CreateBundleOption.class,
                    WARNING_BUNDLE_FILE_EXISTS_KEY,
                    CREATE_BUNDLE_ARG,
                    targetFile));
        } else {
            ExecutionMode.setCurrentExecutionMode(
                    ExecutionMode.CREATE_BUNDLE);
            System.setProperty(
                    Registry.CREATE_BUNDLE_PATH_PROPERTY,
                    targetFile.getAbsolutePath());
        }
    }

    public String getName() {
        return CREATE_BUNDLE_ARG;
    }

      @Override
    protected String getLackOfArgumentsMessage() {
        return ResourceUtils.getString(
                CreateBundleOption.class,
                WARNING_BAD_CREATE_BUNDLE_ARG_KEY,
                CREATE_BUNDLE_ARG);
    }
    public static final String CREATE_BUNDLE_ARG =
            "--create-bundle";// NOI18N
    private static final String WARNING_BUNDLE_FILE_EXISTS_KEY =
            "O.warning.bundle.file.exists"; // NOI18N
    private static final String WARNING_BAD_CREATE_BUNDLE_ARG_KEY =
            "O.warning.bad.create.bundle.arg"; // NOI18N
}
