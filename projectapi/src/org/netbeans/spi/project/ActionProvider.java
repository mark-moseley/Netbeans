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

package org.netbeans.spi.project;

import org.openide.util.Lookup;

/**
 * Ability for a project to have various actions (e.g. Build) invoked on it.
 * Should be registered in a project's lookup and will be used by UI infrastructure.
 * @see org.netbeans.api.project.Project#getLookup
 * @see <a href="@org-apache-tools-ant-module@/org/apache/tools/ant/module/api/support/ActionUtils.html"><code>ActionUtils</code></a>
 * @see <a href="@org-netbeans-modules-projectuiapi@/org/netbeans/spi/project/ui/support/ProjectSensitiveActions.html#projectCommandAction(java.lang.String,%20java.lang.String,%20javax.swing.Icon)"><code>ProjectSensitiveActions.projectCommandAction(...)</code></a>
 * @author Jesse Glick
 */
public interface ActionProvider {
    
    /**
     * Standard command to incrementally build the project.
     */
    String COMMAND_BUILD = "build"; // NOI18N
    
    /** 
     * Standard command for compiling set of files
     */
    String COMMAND_COMPILE_SINGLE = "compile.single"; // NOI18N
        
    /**
     * Standard command to clean build products.
     */
    String COMMAND_CLEAN = "clean"; // NOI18N
    
    /**
     * Standard command to do a "clean" (forced) rebuild.
     */
    String COMMAND_REBUILD = "rebuild"; // NOI18N
        
    /** 
     * Standard command for running the project
     */
    String COMMAND_RUN = "run"; // NOI18N

    /** 
     * Standard command for running one file
     */
    String COMMAND_RUN_SINGLE = "run.single"; // NOI18N
    
    /** 
     * Standard command for running tests on given projects
     */
    String COMMAND_TEST = "test"; // NOI18N
    
    /** 
     * Standard command for running one test file
     */    
    String COMMAND_TEST_SINGLE = "test.single";  // NOI18N
    
    /**
     * Standard command for running the project in debugger
     */    
    String COMMAND_DEBUG = "debug"; // NOI18N
    
    /**
     * Standard command for running single file in debugger
     */    
    String COMMAND_DEBUG_SINGLE = "debug.single"; // NOI18N
    
    /** 
     * Standard command for running one test in debugger
     */
    String COMMAND_DEBUG_TEST_SINGLE = "debug.test.single"; // NOI18N
    
    /** 
     * Standard command for starting app in debugger and stopping at the 
     * beginning of app whatever that means.
     */
    String COMMAND_DEBUG_STEP_INTO = "debug.stepinto"; // NOI18N
    
    /**
     * Standard command for deleting the project.
     *
     * @since 1.6
     */
    String COMMAND_DELETE = "delete"; // NOI18N
    
    /**
     * Standard command for deleting the project.
     *
     * @since 1.7
     */
    String COMMAND_COPY = "copy"; // NOI18N
    
    /**
     * Standard command for moving the project.
     *
     * @since 1.7
     */
    String COMMAND_MOVE = "move"; // NOI18N

    /**
     * Standard command for renaming the project.
     *
     * @since 1.7
     */
    String COMMAND_RENAME = "rename"; // NOI18N
    
    /**
     * Get a list of all commands which this project supports.
     * @return a list of command names suitable for {@link #invokeAction}
     * @see #COMMAND_BUILD
     * @see #COMMAND_CLEAN
     * @see #COMMAND_REBUILD
     */
    String[] getSupportedActions();
    
    /**
     * Run a project command.
     * Will be invoked in the event thread.
     * The context may be ignored by some commands, but some may need it in order
     * to get e.g. the selected source file to build by itself, etc.
     * @param command a predefined command name (must be among {@link #getSupportedActions})
     * @param context any action context, e.g. for a node selection
     *                (as in {@link ContextAwareAction})
     * @throws IllegalArgumentException if the requested command is not supported
     */
    void invokeAction(String command, Lookup context) throws IllegalArgumentException;
    
    /**
     * Tells whether the command can be invoked in given context and thus if
     * actions representing this command should be enabled or disabled.
     * The context may be ignored by some commands, but some may need it in order
     * to get e.g. the selected source file to build by itself, etc.
     * @param command a predefined command name (must be among {@link #getSupportedActions})
     * @param context any action context, e.g. for a node selection
     *                (as in {@link ContextAwareAction})
     * @throws IllegalArgumentException if the requested command is not supported
     */
    boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException;
    
}
