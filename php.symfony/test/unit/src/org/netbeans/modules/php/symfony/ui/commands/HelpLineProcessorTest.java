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

package org.netbeans.modules.php.symfony.ui.commands;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import static org.junit.Assert.*;

/**
 * @author Tomas Mysik
 */
public class HelpLineProcessorTest extends NbTestCase {

    public HelpLineProcessorTest(String name) {
        super(name);
    }

    public void testCommands() {
        SymfonyCommandSupport.HelpLineProcessor processor = new SymfonyCommandSupport.HelpLineProcessor();
        for (String s : getCommands()) {
            processor.processLine(s);
        }
        List<SymfonyCommand> commands = processor.getCommands();
        assertEquals(8, commands.size());

        SymfonyCommand command = commands.get(0);
        assertEquals("app:routes", command.getCommand());
        assertEquals("app:routes", command.getDisplayName());
        assertEquals("Displays current routes for an application", command.getDescription());

        command = commands.get(3);
        assertEquals("configure:database", command.getCommand());
        assertEquals("configure:database", command.getDisplayName());
        assertEquals("Configure database DSN", command.getDescription());

        command = commands.get(7);
        assertEquals("test:unit", command.getCommand());
        assertEquals("test:unit", command.getDisplayName());
        assertEquals("Launches unit tests (test-unit)", command.getDescription());
    }

    private List<String> getCommands() {
        List<String> commands = new LinkedList<String>();
        commands.add("Usage:");
        commands.add("  symfony [options] task_name [arguments]");
        commands.add("");
        commands.add("Options:");
        commands.add("  --dry-run     -n  Do a dry run without executing actions.");
        commands.add("  --help        -H  Display this help message.");
        commands.add("  --version     -V  Display the program version.");
        commands.add("");
        commands.add("Available tasks:");
        commands.add("  :help                        Displays help for a task (h)");
        commands.add("  :list                        Lists tasks");
        commands.add("");
        commands.add("app");
        commands.add("  :routes                      Displays current routes for an application");
        commands.add("");
        commands.add("cache");
        commands.add("  :clear                       Clears the cache (cc, clear-cache)");
        commands.add("");
        commands.add("configure");
        commands.add("  :author                      Configure project author");
        commands.add("  :database                    Configure database DSN");
        commands.add("");
        commands.add("test");
        commands.add("  :all                         Launches all tests (test-all)");
        commands.add("  :coverage                    Outputs test code coverage");
        commands.add("  :functional                  Launches functional tests (test-functional)");
        commands.add("  :unit                        Launches unit tests (test-unit)");
        return commands;
    }
}
