/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.kenai.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiProjectFeature;
import org.netbeans.modules.kenai.ui.GetSourcesFromKenaiPanel.GetSourcesInfo;
import org.netbeans.modules.kenai.ui.SourceAccessorImpl.ProjectAndFeature;
import org.netbeans.modules.mercurial.api.Mercurial;
import org.netbeans.modules.subversion.api.Subversion;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public final class GetSourcesFromKenaiAction implements ActionListener {

    private ProjectAndFeature prjAndFeature;

    private String dialogTitle = NbBundle.getMessage(GetSourcesFromKenaiAction.class, "GetSourcesFromKenaiTitle");
    private String getOption = NbBundle.getMessage(GetSourcesFromKenaiAction.class, "GetSourcesFromKenaiAction.GetFromKenai.option");
    private String cancelOption = NbBundle.getMessage(GetSourcesFromKenaiAction.class, "GetSourcesFromKenaiAction.Cancel.option");

    public GetSourcesFromKenaiAction(ProjectAndFeature prjFtr) {
        prjAndFeature = prjFtr;
    }

    public GetSourcesFromKenaiAction() {
        this(null);
    }

    public void actionPerformed(ActionEvent e) {

        Object options[] = new Object[2];
        options[0] = getOption;
        options[1] = cancelOption;

        GetSourcesFromKenaiPanel getSourcesPanel = new GetSourcesFromKenaiPanel(prjAndFeature);

        DialogDescriptor dialogDesc = new DialogDescriptor(getSourcesPanel, dialogTitle,
            true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null);

        Object option = DialogDisplayer.getDefault().notify(dialogDesc);
        
        if (options[0].equals(option)) {
            
            // Run the checkout with progress !!!
            final PasswordAuthentication passwdAuth = Kenai.getDefault().getPasswordAuthentication();
            final GetSourcesInfo sourcesInfo = getSourcesPanel.getSelectedSourcesInfo();
            final KenaiProjectFeature feature = sourcesInfo.feature;

            if (Utilities.SVN_REPO.equals(feature.getService())) { // XXX service or name
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {
                            Subversion.checkoutRepositoryFolder(feature.getLocation().toExternalForm(), sourcesInfo.relativePaths,
                                new File(sourcesInfo.localFolderPath), passwdAuth.getUserName(), passwdAuth.getPassword().toString(), true);
                        } catch (MalformedURLException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            } else if (Utilities.HG_REPO.equals(sourcesInfo.feature.getService())) { // XXX service or name
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {
                            Mercurial.cloneRepository(feature.getLocation().toExternalForm(), new File(sourcesInfo.localFolderPath),
                                    "", "", "", passwdAuth.getUserName(), passwdAuth.getPassword().toString());
                        } catch (MalformedURLException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            }
            // XXX store the project in preferrences, it will be shown as first for next Get From Kenai
        }

    }
    
}
