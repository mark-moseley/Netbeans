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
package org.netbeans.modules.php.project.ui.actions.support;

import java.util.concurrent.Callable;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.project.spi.XDebugStarter;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;

/**
 * @author Radek Matous, Tomas Mysik
 */
public class DebugScript  extends RunScript {
    private final Provider provider;

    public DebugScript(Provider provider) {
        super(provider);
        this.provider = provider;
    }

    @Override
    public void run() {
        //temporary; after narrowing deps. will be changed
        Callable<Cancellable> callable = getCallable();
        XDebugStarter dbgStarter =  XDebugStarterFactory.getInstance();
        assert dbgStarter != null;
        if (dbgStarter.isAlreadyRunning()) {
            if (CommandUtils.warnNoMoreDebugSession()) {
                dbgStarter.stop();
                run();
            }
        } else {
            dbgStarter.start(provider.getProject(), callable, provider.getStartFile(), true);
        }
    }

    @Override
    protected boolean isControllable() {
        return false;
    }

    @Override
    protected String getOutputTabTitle() {
        return String.format("%s %s", super.getOutputTabTitle(), NbBundle.getMessage(DebugScript.class, "MSG_Suffix_Debug"));
    }

    @Override
    protected ExternalProcessBuilder getProcessBuilder() {
        return super.getProcessBuilder()
                .addEnvironmentVariable("XDEBUG_CONFIG", "idekey=" + PhpOptions.getInstance().getDebuggerSessionId()); // NOI18N
    }

    public interface Provider extends RunScript.Provider {
        Project getProject();
        FileObject getStartFile();
    }
}
