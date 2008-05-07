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
package org.netbeans.modules.groovy.grailsproject.actions;

import java.awt.event.ActionEvent;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import javax.swing.AbstractAction;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.groovy.grails.api.ExecutionSupport;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grails.api.GrailsRuntime;
import org.netbeans.modules.groovy.grailsproject.execution.DefaultDescriptor;
import org.netbeans.modules.groovy.grailsproject.execution.ExecutionService;
import org.netbeans.modules.groovy.grailsproject.execution.LineSnooper;
import org.openide.filesystems.FileObject;

public class ShellAction extends AbstractAction {

    private static final Logger LOG = Logger.getLogger(ShellAction.class.getName());

    private final Project prj;

    public ShellAction(Project prj) {
        super("Open Shell");
        this.prj = prj;
    }

    public boolean isEnabled() {
        return true;
    }

    public void actionPerformed(ActionEvent e) {
        final GrailsRuntime runtime = GrailsRuntime.getInstance();
        if (!runtime.isConfigured()) {
            ConfigSupport.showConfigurationWarning(runtime);
            return;
        }
        
        Callable<Process> callable = ExecutionSupport.getInstance().createSimpleCommand("shell",
                GrailsProjectConfig.forProject(prj));
        ProjectInformation inf = prj.getLookup().lookup(ProjectInformation.class);
        String displayName = inf.getDisplayName() + " (shell)"; // NOI18N
        ExecutionService service = new ExecutionService(callable, displayName,
                new DefaultDescriptor(prj, true));

        service.run();
    }
}
