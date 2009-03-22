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
package org.netbeans.modules.maven.codegen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginContainer;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Milos Kleint
 */
public class PluginGenerator implements CodeGenerator {

    public static class Factory implements CodeGenerator.Factory {
        
        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> toRet = new ArrayList<CodeGenerator>();
            POMModel model = context.lookup(POMModel.class);
            JTextComponent component = context.lookup(JTextComponent.class);
            if (model != null) {
                toRet.add(new PluginGenerator(model, component));
            }
            return toRet;
        }
    }

    private POMModel model;
    private JTextComponent component;
    
    /** Creates a new instance of PluginGenerator */
    private PluginGenerator(POMModel model, JTextComponent component) {
        this.model = model;
        this.component = component;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(PluginGenerator.class, "NAME_Plugin");
    }

    public void invoke() {
        if (!model.getState().equals(State.VALID)) {
            //TODO report somehow, status line?
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(PluginGenerator.class, "MSG_Cannot_Parse"));
            return;
        }

        FileObject fo = model.getModelSource().getLookup().lookup(FileObject.class);
        assert fo != null;
        org.netbeans.api.project.Project prj = FileOwnerQuery.getOwner(fo);
        assert prj != null;

        NewPluginPanel pluginPanel = new NewPluginPanel();
        DialogDescriptor dd = new DialogDescriptor(pluginPanel,
                NbBundle.getMessage(PluginGenerator.class, "TIT_Add_plugin"));
        if (DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION) {
            NBVersionInfo vi = pluginPanel.getPlugin();
            if (vi != null) {
                //boolean pomPackaging = "pom".equals(model.getProject().getPackaging()); //NOI18N
                model.startTransaction();
                int pos = component.getCaretPosition();
                PluginContainer container = findContainer(pos, model);

                Plugin plug = model.getFactory().createPlugin();
                plug.setGroupId(vi.getGroupId());
                plug.setArtifactId(vi.getArtifactId());
                plug.setVersion(vi.getVersion());

                if (pluginPanel.isConfiguration()) {
                    Configuration config = model.getFactory().createConfiguration();
                    config.setSimpleParameter("foo", "bar");
                    plug.setConfiguration(config);
                }

                if (pluginPanel.getGoals() != null && pluginPanel.getGoals().size() > 0) {
                    for (String goal : pluginPanel.getGoals()) {
                        plug.addGoal(goal);
                    }
                }

                container.addPlugin(plug);
                model.endTransaction();
                try {
                    model.sync();
                    pos = model.getAccess().findPosition(plug.getPeer());
                    component.setCaretPosition(pos);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    private PluginContainer findContainer(int pos, POMModel model) {
        Component dc = model.findComponent(pos);
        while (dc != null) {
            if (dc instanceof PluginContainer) {
                return (PluginContainer) dc;
            }
            dc = dc.getParent();
        }
        Build bld = model.getProject().getBuild();
        if (bld == null) {
            bld = model.getFactory().createBuild();
            model.getProject().setBuild(bld);
        }
        return bld;
    }

}
