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

package org.netbeans.spi.java.project.runner;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import org.netbeans.api.java.project.runner.JavaRunner;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Implementation of {@link JavaRunner}. Looked-up in the default {@link Lookup}.
 *
 * @since 1.22
 * 
 * @author Jan Lahoda
 */
public interface JavaRunnerImplementation {

    /**
     * Implementation of {@link JavaRunner#isSupported(java.lang.String, java.util.Map)}.
     * 
     * @param command command name
     * @param toRun either the file that would be executed, or the project folder
     * @return true if and only if the given command is supported for given file/folder
     *
     * @since 1.22
     */
    public boolean isSupported(String command, Map<String, ?> properties);

    /**
     * Implementation of {@link JavaRunner#execute(java.lang.String, java.util.Map)}.
     *
     * @param command command to execute
     * @param props properties
     * @param toRun file to run
     * @throws java.io.IOException if execution fails
     *
     * @since 1.22
     */
    public ExecutorTask execute(String command, Map<String, ?> properties) throws IOException;
    
}
