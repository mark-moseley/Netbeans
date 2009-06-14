/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.shell;

import org.openide.util.NbBundle;

/**
 *
 * @author Anki R Nelaturu
 */
final class HelpCommand implements Command {

    private String HELP = null;

    public String execute(ShellPanel shellPanel, String[] args) throws ShellException {
        if (args.length == 1) {
            if (HELP == null) {
                StringBuilder sb = new StringBuilder();

                for (Object name: shellPanel.getCommandManager().allCommandNames()) {
                    sb.append(shellPanel.getCommandManager().getCommand(
                            name.toString()).usage()).append("\n"); //NOI18N
                }
                HELP = sb.toString();
            }
            return HELP;
        }
        Command c = shellPanel.getCommandManager().getCommand(args[1]);
        if(c == null) {
            String msg = NbBundle.getMessage (HelpCommand.class,
                    "ERR_UNKNOWN_COMMAND", args[1]); //NOI18N
            throw new ShellException(msg); //NOI18N
        }
        return c.usage();
    }

    public String usage() {
        return APDUSender.getString("USAGE_HELP"); //NOI18N
    }
}
