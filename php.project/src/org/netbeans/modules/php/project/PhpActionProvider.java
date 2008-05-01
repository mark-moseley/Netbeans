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
package org.netbeans.modules.php.project;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.modules.php.project.ui.actions.Command;
import org.netbeans.modules.php.project.ui.actions.CopyCommand;
import org.netbeans.modules.php.project.ui.actions.DebugCommand;
import org.netbeans.modules.php.project.ui.actions.DebugLocalCommand;
import org.netbeans.modules.php.project.ui.actions.DebugSingleCommand;
import org.netbeans.modules.php.project.ui.actions.DeleteCommand;
import org.netbeans.modules.php.project.ui.actions.Displayable;
import org.netbeans.modules.php.project.ui.actions.MoveCommand;
import org.netbeans.modules.php.project.ui.actions.RenameCommand;
import org.netbeans.modules.php.project.ui.actions.RunCommand;
import org.netbeans.modules.php.project.ui.actions.RunLocalCommand;
import org.netbeans.modules.php.project.ui.actions.RunSingleCommand;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.LifecycleManager;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * @author Radek Matous
 */
public class PhpActionProvider implements ActionProvider {
    private final Map<String, Command> commands;

    PhpActionProvider(PhpProject project) {
        commands = new LinkedHashMap<String, Command>();
        Command[] commandArray = new Command[] {
            new RunCommand(project),
            new DebugCommand(project),
            new RunSingleCommand(project),
            new DebugSingleCommand(project),
            new RunLocalCommand(project),
            new DebugLocalCommand(project),
            new DeleteCommand(project),
            new CopyCommand(project),
            new MoveCommand(project),
            new RenameCommand(project)
        };
        for (Command command : commandArray) {
            commands.put(command.getCommandId(), command);
        }
    }

    public String[] getSupportedActions() {
        Set<String> commandIds = commands.keySet();
        return commandIds.toArray(new String[commandIds.size()]);
    }

    public void invokeAction(final String commandId, final Lookup lookup) throws IllegalArgumentException {
        final Command command = getCommand(commandId);
        command.getProject().getCopySupport().waitFinished();
        if (command.saveRequired()) {
            LifecycleManager.getDefault().saveAll();
        }
        if (!command.asyncCallRequired()) {
            command.invokeAction(lookup);
        } else {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    command.invokeAction(lookup);
                }
            });
        }
    }

    public boolean isActionEnabled(String commandId, Lookup lookup) throws IllegalArgumentException {
        return getCommand(commandId).isActionEnabled(lookup);
    }

    public Command getCommand(String commandId) {
        Command retval = commands.get(commandId);
        assert retval != null : commandId;
        return retval;
    }

    public Action getAction(String commandId) {
        Command command = getCommand(commandId);
        assert command != null;
        assert command instanceof Displayable;
        return ProjectSensitiveActions.projectCommandAction(command.getCommandId(),
                ((Displayable) command).getDisplayName(), null);
    }
}