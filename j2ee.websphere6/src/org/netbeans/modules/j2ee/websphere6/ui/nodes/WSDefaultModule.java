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

package org.netbeans.modules.j2ee.websphere6.ui.nodes;

import java.util.Comparator;
import javax.enterprise.deploy.spi.TargetModuleID;

/**
 *
 * @author Petr Hejl
 */
public class WSDefaultModule {

    public static final Comparator<WSDefaultModule> MODULE_COMPARATOR = new Comparator<WSDefaultModule>() {

        public int compare(WSDefaultModule o1, WSDefaultModule o2) {
            return o1.getConsoleName().compareTo(o2.getConsoleName());
        }
    };

    private static final String PREFIX = "WebSphere:";

    private static final String NAME_ATTRIBUTE = "name";

    private final TargetModuleID moduleID;

    private final boolean running;

    private String consoleName;

    public WSDefaultModule(TargetModuleID moduleID, boolean running) {
        this.moduleID = moduleID;
        this.running = running;
    }

    public final TargetModuleID getModuleID() {
        return moduleID;
    }

    public final boolean isRunning() {
        return running;
    }

    public synchronized String getConsoleName() {
        if (consoleName == null) {
            consoleName = constructName(moduleID.getModuleID(), true);
        }
        return consoleName;
    }

    public synchronized String getRealName() {
        return getConsoleName();
    }

    protected String constructName(String id, boolean suffix) {
        String name = id;
        if (name.startsWith(PREFIX)) {
            name = name.substring(PREFIX.length());
            String[] parts = name.split(",");
            for (String part : parts) {
                String[] pair = part.split("=");
                if (pair.length == 2 && pair[0].trim().equals(NAME_ATTRIBUTE)) {
                    return pair[1].trim();
                }
            }
        }

        return id;
    }
}
