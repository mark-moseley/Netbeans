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

package org.netbeans.modules.maven.hints.pom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.hints.pom.spi.Configuration;
import org.netbeans.modules.maven.hints.pom.spi.POMErrorFixProvider;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.BuildBase;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.Profile;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.text.Line;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class OverridePluginManagementError implements POMErrorFixProvider {
    private Configuration configuration;

    public OverridePluginManagementError() {
        configuration = new Configuration("OverridePluginManagementError", //NOI18N
                NbBundle.getMessage(OverridePluginManagementError.class, "TIT_OverridePluginManagementError"),
                NbBundle.getMessage(OverridePluginManagementError.class, "DESC_OverridePluginManagementError"),
                true, Configuration.HintSeverity.WARNING);
    }


    public List<ErrorDescription> getErrorsForDocument(POMModel model, Project prj) {
        assert model != null;
        List<ErrorDescription> toRet = new ArrayList<ErrorDescription>();
        Map<String, String> managed = collectManaged(prj);
        if (managed.size() == 0) {
            return toRet;
        }

        Build bld = model.getProject().getBuild();
        if (bld != null) {
            checkPluginList(bld.getPlugins(), model, toRet, managed);
        }
        List<Profile> profiles = model.getProject().getProfiles();
        if (profiles != null) {
            for (Profile prof : profiles) {
                BuildBase base = prof.getBuildBase();
                if (base != null) {
                    checkPluginList(base.getPlugins(), model, toRet, managed);
                }
            }
        }
        return toRet;

    }

    private void checkPluginList(List<Plugin> plugins, POMModel model, List<ErrorDescription> toRet, Map<String, String> managed) {
        if (plugins != null) {
            for (Plugin plg : plugins) {
                String ver = plg.getVersion();
                if (ver != null) {
                    String art = plg.getArtifactId();
                    String gr = plg.getGroupId();
                    gr = gr != null ? gr : Constants.GROUP_APACHE_PLUGINS;
                    String key = gr + ":" + art; //NOI18N
                    if (managed.keySet().contains(key)) {
                        int position = plg.findChildElementPosition(model.getPOMQNames().VERSION.getQName());
                        Line line = NbEditorUtilities.getLine(model.getBaseDocument(), position, false);
                        String managedver = managed.get(key);
                        toRet.add(ErrorDescriptionFactory.createErrorDescription(
                                       configuration.getSeverity(configuration.getPreferences()).toEditorSeverity(),
                                NbBundle.getMessage(OverridePluginManagementError.class, "TXT_OverridePluginManagementError", managedver),
                                Collections.<Fix>emptyList(), //Collections.<Fix>singletonList(new ReleaseFix(plg)),
                                model.getBaseDocument(), line.getLineNumber() + 1));
                    }
                }
            }
        }
    }


    public JComponent getCustomizer(Preferences preferences) {
        return null;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    private Map<String, String> collectManaged(Project prj) {
        NbMavenProject project = prj.getLookup().lookup(NbMavenProject.class);
        @SuppressWarnings("unchecked")
        List<org.apache.maven.model.Plugin> plugins = project.getMavenProject().getPluginManagement().getPlugins();
        Map<String, String> toRet = new HashMap<String, String>();
        for (org.apache.maven.model.Plugin plg : plugins) {
            toRet.put(plg.getKey(), plg.getVersion());
        }
        return toRet;
    }

}
