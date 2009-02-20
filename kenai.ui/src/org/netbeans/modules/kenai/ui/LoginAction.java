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
import javax.swing.AbstractAction;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiEvent;
import org.netbeans.modules.kenai.api.KenaiListener;
import org.netbeans.modules.kenai.ui.spi.UIUtils;
import org.openide.util.NbBundle;


/**
 * @author Jan Becicka
 */
public final class LoginAction extends AbstractAction {

    private static LoginAction instance;
    private boolean logout;

    private LoginAction() {
        setLogout(Kenai.getDefault().getPasswordAuthentication()!=null);
    }

    public static synchronized LoginAction getDefault() {
        if (instance==null) {
            instance=new LoginAction();
            Kenai.getDefault().addKenaiListener(new KenaiListener() {

                public void stateChanged(KenaiEvent e) {
                    if (e.LOGIN == e.getType()) {
                        if (e.getSource()==null) {
                            instance.setLogout(false);
                        } else {
                            instance.setLogout(true);
                        }
                    }
                }
            });
        }
        return instance;
    }

    public void actionPerformed(ActionEvent e) {
        if (logout) {
            Kenai.getDefault().logout();
        } else {
            if (!UIUtils.showLogin()) {
                return;
            }
            final KenaiTopComponent kenaiTC = KenaiTopComponent.getDefault();
            kenaiTC.open();
            kenaiTC.requestActive();
        }
    }

    private void setLogout(boolean b) {
        this.logout=b;
        if (b) {
            putValue(NAME, NbBundle.getMessage(LoginAction.class, "CTL_LogoutAction"));
        } else {
            putValue(NAME, NbBundle.getMessage(LoginAction.class, "CTL_LoginAction"));
        }
    }
}
