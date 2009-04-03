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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.maven.model.settings.Mirror;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Milos Kleint
 */
public class MirrorGenerator implements CodeGenerator {

    public static class Factory implements CodeGenerator.Factory {
        
        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> toRet = new ArrayList<CodeGenerator>();
            SettingsModel model = context.lookup(SettingsModel.class);
            JTextComponent component = context.lookup(JTextComponent.class);
            if (model != null) {
                toRet.add(new MirrorGenerator(model, component));
            }
            return toRet;
        }
    }

    private SettingsModel model;
    private JTextComponent component;
    
    /** Creates a new instance of ProfileGenerator */
    private MirrorGenerator(SettingsModel model, JTextComponent component) {
        this.model = model;
        this.component = component;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(MirrorGenerator.class, "NAME_Mirror");
    }

    public void invoke() {
        try {
            model.sync();
        } catch (IOException ex) {
            Logger.getLogger(MirrorGenerator.class.getName()).log(Level.INFO, "Error while syncing the editor document with model for pom.xml file", ex); //NOI18N
        }
        if (!model.getState().equals(State.VALID)) {
            //TODO report somehow, status line?
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(MirrorGenerator.class, "MSG_Cannot_Parse"));
            return;
        }
        NewMirrorPanel panel = new NewMirrorPanel(model);
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(MirrorGenerator.class, "TIT_Add_mirror"));
        panel.attachDialogDisplayer(dd);
        Object ret = DialogDisplayer.getDefault().notify(dd);
        if (ret == DialogDescriptor.OK_OPTION) {
            String id = panel.getMirrorId();
            Mirror mirror = model.getSettings().findMirrorById(id);
            if (mirror == null) {
                try {
                    model.startTransaction();
                    mirror = model.getFactory().createMirror();
                    mirror.setId(id);
                    mirror.setUrl(panel.getMirrorUrl());
                    mirror.setMirrorOf(panel.getMirrorOf());
                    model.getSettings().addMirror(mirror);
                } finally {
                    model.endTransaction();
                }
                int pos = mirror.getModel().getAccess().findPosition(mirror.getPeer());
                component.setCaretPosition(pos);
            }
        }
    }
}
