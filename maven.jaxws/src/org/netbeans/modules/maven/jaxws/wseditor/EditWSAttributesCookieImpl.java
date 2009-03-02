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

/*
 * EditWSAttributesCookieImpl.java
 *
 * Created on April 12, 2006, 10:13 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.maven.jaxws.wseditor;

import org.netbeans.modules.websvc.api.support.EditWSAttributesCookie;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.wseditor.WSEditor;
import org.netbeans.modules.websvc.spi.wseditor.WSEditorProvider;
import org.netbeans.modules.websvc.api.wseditor.WSEditorProviderRegistry;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
 *
 * @author rico
 */
public class EditWSAttributesCookieImpl implements EditWSAttributesCookie {

    /** Creates a new instance of EditWSAttributesCookieImpl */
    public EditWSAttributesCookieImpl(Node node) {
        this.node = node;
    }

    @Override
    public void openWSAttributesEditor() {
        if (SwingUtilities.isEventDispatchThread()) {  //Ensure it is in AWT thread
            openEditor();
        } else {
            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    openEditor();
                }
            });
        }
    }
    
    private void openEditor() {
        final JFrame mainWin = (JFrame) WindowManager.getDefault().getMainWindow();
        final Cursor origCursor = mainWin.getGlassPane().getCursor();
        mainWin.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        mainWin.getGlassPane().setVisible(true);

        tc = cachedTopComponents.get(node);
        if (tc == null) {
            //populate the editor registry if needed
            populateWSEditorProviderRegistry();
            //get all providers
            providers = WSEditorProviderRegistry.getDefault().getEditorProviders();
            tc = new EditWSAttributesPanel();
            cachedTopComponents.put(this, tc);
        }
        populatePanels();
        tc.addTabs(editors, node, jaxWsModel);
        DialogDescriptor dialogDesc = new DialogDescriptor(tc, node.getName());
        dialogDesc.setHelpCtx(new HelpCtx(EditWSAttributesCookieImpl.class));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
        dialog.getAccessibleContext().setAccessibleDescription(dialog.getTitle());
        dialog.setVisible(true);

        mainWin.getGlassPane().setCursor(origCursor);
        mainWin.getGlassPane().setVisible(false);


        if (dialogDesc.getValue() == NotifyDescriptor.OK_OPTION) {
            for (WSEditor editor : editors) {
                editor.save(node);
            }
        } else {
            for (WSEditor editor : editors) {
                editor.cancel(node);
            }
        }
    }

    class DialogWindowListener extends WindowAdapter {

        Set<WSEditor> editors;

        public DialogWindowListener(Set<WSEditor> editors) {
            this.editors = editors;
        }

        public void windowClosing(WindowEvent e) {
            for (WSEditor editor : editors) {
                editor.cancel(node);
            }
        }
    }

    private Set getWSEditorProviders() {
        return providers;
    }

    private void populatePanels() {
        editors = new HashSet<WSEditor>();
        for (WSEditorProvider provider : providers) {
            if (provider.enable(node)) {
                //for each provider, create a WSAttributesEditor
                WSEditor editor = provider.createWSEditor(node.getLookup());
                if (editor != null) {
                    editors.add(editor);
                }
            }
        }
    }

    private void populateWSEditorProviderRegistry() {
        WSEditorProviderRegistry registry = WSEditorProviderRegistry.getDefault();
        if (registry.getEditorProviders().isEmpty()) {
            Lookup.Result<WSEditorProvider> results = Lookup.getDefault().lookup(new Lookup.Template<WSEditorProvider>(WSEditorProvider.class));
            Collection<? extends WSEditorProvider> services = results.allInstances();
            //System.out.println("###number of editors: " + services.size());
            for (WSEditorProvider provider : services) {
                registry.register(provider);
            }
        }
    }
    private Set<WSEditorProvider> providers;
    private Set<WSEditor> editors;
    private static Map<EditWSAttributesCookie, EditWSAttributesPanel> cachedTopComponents = new WeakHashMap<EditWSAttributesCookie, EditWSAttributesPanel>();
    private EditWSAttributesPanel tc;
    private Node node;
    private JaxWsModel jaxWsModel;
    private DialogWindowListener windowListener;
}