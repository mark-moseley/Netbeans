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
package org.netbeans.modules.dlight.core.ui.components;

import java.awt.BorderLayout;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.modules.dlight.management.api.ExecutionContextEvent;
import org.netbeans.modules.dlight.management.api.ExecutionContextListener;
import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.management.api.DLightSession;
import org.netbeans.modules.dlight.management.api.DLightSession.SessionState;
import org.netbeans.modules.dlight.management.api.DLightSessionListener;
import org.netbeans.modules.dlight.management.api.SessionStateListener;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.Utilities;

/**
 * Top component which displays something.
 */
final class DLightSessionsTopComponent extends TopComponent 
        implements DLightSessionListener, ExecutionContextListener, SessionStateListener {

    private static DLightSessionsTopComponent instance;
    private transient DLightSessionsViewPanel view = null;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/netbeans/modules/dlight/core/ui/resources/dlight_sessions_small.png";
    private static final String PREFERRED_ID = "DLightSessionsTopComponent";

    private DLightSessionsTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(DLightSessionsTopComponent.class, "CTL_DLightSessionsTopComponent"));
        setToolTipText(NbBundle.getMessage(DLightSessionsTopComponent.class, "HINT_DLightSessionsTopComponent"));
        setIcon(Utilities.loadImage(ICON_PATH, true));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    setLayout(new java.awt.BorderLayout());
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized DLightSessionsTopComponent getDefault() {
        if (instance == null) {
            instance = new DLightSessionsTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the DLightSessionsTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized DLightSessionsTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(DLightSessionsTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof DLightSessionsTopComponent) {
            return (DLightSessionsTopComponent) win;
        }
        Logger.getLogger(DLightSessionsTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        if (view == null) {
            view = new DLightSessionsViewPanel();
        }

        DLightManager.getDefault().addDLightSessionListener(this);

        view.startup();
        removeAll();


        if (DLightManager.getDefault().getSessionsList().isEmpty()) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            JLabel label = new JLabel(loc("DLightSessionsViewPanel.emptyContent.text")); // NOI18N
            label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            add(label);
        } else {
            setLayout(new BorderLayout());
            add(view, BorderLayout.CENTER);
        }

        revalidate();
    }

    @Override
    public void componentClosed() {
        DLightManager.getDefault().removeDLightSessionListener(this);
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public void contextChanged(ExecutionContextEvent event) {
        view.updateContent(event.getObj());
    }

    public void activeSessionChanged(DLightSession oldSession, DLightSession newSession) {
        view.updateContent(null);
    }

    public void sessionAdded(DLightSession newSession) {
        newSession.addSessionStateListener(this);
        newSession.addExecutionContextListener(this);
        view.updateContent(null);
    }

    public void sessionRemoved(DLightSession removedSession) {
        removedSession.removeSessionStateListener(this);
        removedSession.removeExecutionContextListener(this);
        view.updateContent(null);
    }

    public void sessionStateChanged(DLightSession session, SessionState oldState, SessionState newState) {
        view.updateContent(session);
    }

    static final class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return DLightSessionsTopComponent.getDefault();
        }
    }

    private static String loc(String key) {
        return NbBundle.getMessage(DLightSessionsViewPanel.class, key);
    }
}
