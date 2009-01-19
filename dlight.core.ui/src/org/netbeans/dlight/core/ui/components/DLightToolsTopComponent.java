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

package org.netbeans.dlight.core.ui.components;

import java.io.Serializable;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.Utilities;

/**
 * Top component which displays something.
 */
public final class DLightToolsTopComponent extends TopComponent {

    private static DLightToolsTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";

    private static final String PREFERRED_ID = "DLightToolsTopComponent";

    private DLightToolsTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(DLightToolsTopComponent.class, "CTL_DLightToolsTopComponent"));
        setToolTipText(NbBundle.getMessage(DLightToolsTopComponent.class, "HINT_DLightToolsTopComponent"));
//        setIcon(Utilities.loadImage(ICON_PATH, true));
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jLabel1 = new javax.swing.JLabel();

    setLayout(new java.awt.BorderLayout());

    jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DLightToolsTopComponent.class, "DLightToolsTopComponent.jLabel1.text")); // NOI18N
    jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
    add(jLabel1, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel jLabel1;
  // End of variables declaration//GEN-END:variables
  /**
   * Gets default instance. Do not use directly: reserved for *.settings files only,
   * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
   * To obtain the singleton instance, use {@link #findInstance}.
   */
  public static synchronized DLightToolsTopComponent getDefault() {
    if (instance == null) {
      instance = new DLightToolsTopComponent();
    }
    return instance;
  }

  /**
   * Obtain the DLightToolsTopComponent instance. Never call {@link #getDefault} directly!
   */
  public static synchronized DLightToolsTopComponent findInstance() {
    TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
    if (win == null) {
      Logger.getLogger(DLightToolsTopComponent.class.getName()).warning(
              "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
      return getDefault();
    }
    if (win instanceof DLightToolsTopComponent) {
      return (DLightToolsTopComponent) win;
    }
    Logger.getLogger(DLightToolsTopComponent.class.getName()).warning(
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
    // TODO add custom code on component opening
    }

  @Override
  public void componentClosed() {
    // TODO add custom code on component closing
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

  static final class ResolvableHelper implements Serializable {

    private static final long serialVersionUID = 1L;

    public Object readResolve() {
      return DLightToolsTopComponent.getDefault();
    }
  }
}
