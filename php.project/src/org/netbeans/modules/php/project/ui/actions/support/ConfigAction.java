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

package org.netbeans.modules.php.project.ui.actions.support;

import java.util.logging.Logger;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.customizer.CompositePanelProviderImpl;
import org.netbeans.modules.php.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Common action for all the possible Run Configurations of a PHP project.
 * <p>
 * Meant to be stateless, so thread safe.
 * @author Tomas Mysik
 */
public abstract class ConfigAction {
    public static enum Type {
        LOCAL,
        REMOTE,
        SCRIPT,
        TEST,
    }

    protected static final Logger LOGGER = Logger.getLogger(ConfigAction.class.getName());
    protected final PhpProject project;

    protected ConfigAction(PhpProject project) {
        assert project != null;
        this.project = project;
    }

    public static Type convert(PhpProjectProperties.RunAsType runAsType) {
        Type type = null;
        switch (runAsType) {
            case LOCAL:
                type = Type.LOCAL;
                break;
            case REMOTE:
                type = Type.REMOTE;
                break;
            case SCRIPT:
                type = Type.SCRIPT;
                break;
            default:
                assert false : "Unknown type: " + runAsType;
                break;
        }
        return type;
    }

    public static ConfigAction get(Type type, PhpProject project) {
        assert type != null;
        ConfigAction action = null;
        switch (type) {
            case LOCAL:
                action = new ConfigActionLocal(project);
                break;
            case REMOTE:
                action = new ConfigActionRemote(project);
                break;
            case SCRIPT:
                action = new ConfigActionScript(project);
                break;
            case TEST:
                action = new ConfigActionTest(project);
                break;
            default:
                assert false : "Unknown type: " + type;
                break;
        }
        assert action != null;
        return action;
    }

    public boolean isRunProjectEnabled() {
        return true;
    }

    public boolean isDebugProjectEnabled() {
        return XDebugStarterFactory.getInstance() != null;
    }

    public abstract boolean isValid(boolean indexFileNeeded);

    public abstract boolean isRunFileEnabled(Lookup context);
    public abstract boolean isDebugFileEnabled(Lookup context);

    public abstract void runProject();
    public abstract void debugProject();

    public abstract void runFile(Lookup context);
    public abstract void debugFile(Lookup context);

    protected void showCustomizer() {
        project.getLookup().lookup(CustomizerProviderImpl.class).showCustomizer(CompositePanelProviderImpl.RUN);
    }

    protected boolean isIndexFileValid(FileObject baseDirectory) {
        assert baseDirectory != null;
        String indexFile = ProjectPropertiesSupport.getIndexFile(project);
        if (indexFile == null || indexFile.trim().length() == 0 || baseDirectory.getFileObject(indexFile) == null) {
            return false;
        }
        return true;
    }
}
