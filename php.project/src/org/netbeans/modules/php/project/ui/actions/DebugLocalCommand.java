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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui.actions;

import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.modules.php.project.spi.XDebugStarter;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
/**
 * @author Radek Matous
 */
public class DebugLocalCommand  extends RunLocalCommand {
    public static final String ID = "debug.local"; //NOI18N

    public DebugLocalCommand(PhpProject project) {
        super(project);
    }

    @Override
    public void invokeAction(final Lookup context) throws IllegalArgumentException {
        Runnable runnable = new Runnable() {
            public void run() {
                DebugLocalCommand.super.invokeAction(context);
            }
        };
        //temporary; after narrowing deps. will be changed
        XDebugStarter dbgStarter =  XDebugStarterFactory.getInstance();
        if (dbgStarter != null) {
            dbgStarter.start(getProject(), runnable, 
                    (context == null) ? fileForProject() : fileForContext(context), useInterpreter());
        }
    }

    @Override
    public boolean isActionEnabled(Lookup context) throws IllegalArgumentException {
        return ((context == null) ? fileForProject() : fileForContext(context)) != null && XDebugStarterFactory.getInstance() != null;
    }

    @Override
    public String getCommandId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(RunCommand.class, "LBL_DebugLocalCommand");
    }

    @Override
    protected void initProcessBuilder(ProcessBuilder processBuilder) {
        super.initProcessBuilder(processBuilder);
        processBuilder.environment().put("XDEBUG_CONFIG", "idekey=" + PhpSourcePath.DEBUG_SESSION); //NOI18N
    }
}
