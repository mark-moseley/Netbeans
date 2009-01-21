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
package org.netbeans.modules.dlight.visualizers;

import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.netbeans.modules.dlight.visualizer.spi.Visualizer;
import org.netbeans.modules.dlight.visualizer.spi.VisualizerContainer;
import org.openide.awt.TabbedPaneFactory;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
public final class VisualizerTopComponentTopComponent extends TopComponent implements VisualizerContainer {

  private static VisualizerTopComponentTopComponent instance;
  private static final String PREFERRED_ID = "VisualizerTopComponentTopComponent";
  //private List<JComponent> visualizerComponents = new ArrayList<JComponent>();
  private CloseListener closeListener = new CloseListener();
  private JPanel performanceMonitorViewsArea = new JPanel();
  private JTabbedPane tabbedPane = null;
  private HashMap<String, Visualizer> visualizerComponents = new HashMap<String, Visualizer>();

  private VisualizerTopComponentTopComponent() {
    initComponents();
    initPerformanceMonitorViewComponents();
    setName(NbBundle.getMessage(VisualizerTopComponentTopComponent.class, "CTL_VisualizerTopComponentTopComponent"));
    setToolTipText(NbBundle.getMessage(VisualizerTopComponentTopComponent.class, "HINT_VisualizerTopComponentTopComponent"));
//        setIcon(Utilities.loadImage(ICON_PATH, true));
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
  public static synchronized VisualizerTopComponentTopComponent getDefault() {
    if (instance == null) {
      instance = new VisualizerTopComponentTopComponent();
    }
    return instance;
  }

  /**
   * Obtain the VisualizerTopComponentTopComponent instance. Never call {@link #getDefault} directly!
   */
  public static synchronized VisualizerTopComponentTopComponent findInstance() {
    TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
    if (win == null) {
      Logger.getLogger(VisualizerTopComponentTopComponent.class.getName()).warning(
          "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
      return getDefault();
    }
    if (win instanceof VisualizerTopComponentTopComponent) {
      return (VisualizerTopComponentTopComponent) win;
    }
    Logger.getLogger(VisualizerTopComponentTopComponent.class.getName()).warning(
        "There seem to be multiple components with the '" + PREFERRED_ID +
        "' ID. That is a potential source of errors and unexpected behavior.");
    return getDefault();
  }

  @Override
  public int getPersistenceType() {
    return TopComponent.PERSISTENCE_NEVER;
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

  public void addVisualizer(String toolName, Visualizer view) {
    JComponent viewComponent = view.getComponent();
    if (visualizerComponents.get(toolName) == null) {//no component - add new one
      tabbedPane.addTab(toolName, viewComponent);
//      visualizerComponents.put(tool, view);
    } else {
      //we should remove the tabs and
      closePerformanceMonitor(visualizerComponents.get(toolName));
      tabbedPane.addTab(toolName, viewComponent);
    }

    visualizerComponents.put(toolName, view);
    validate();
    repaint();
  }

  public void showup() {
    open();
    requestActive();
  }

  public void removeVisualizer(Visualizer v) {
    closePerformanceMonitor(v);
  }

  static final class ResolvableHelper implements Serializable {

    private static final long serialVersionUID = 1L;

    public Object readResolve() {
      return VisualizerTopComponentTopComponent.getDefault();
    }
  }

  private void addPerformanceMonitorView(Visualizer view) {
  }

  private void initPerformanceMonitorViewComponents() {
    setLayout(new BorderLayout());
    tabbedPane = TabbedPaneFactory.createCloseButtonTabbedPane();
    tabbedPane.addPropertyChangeListener(closeListener);
    performanceMonitorViewsArea.setLayout(new BorderLayout());
    performanceMonitorViewsArea.add(tabbedPane, BorderLayout.CENTER);
    this.add(performanceMonitorViewsArea, BorderLayout.CENTER);
  }

  public void closePerformanceMonitor(Visualizer view) {
//    view.stopMonitor();
    JComponent viewComponent = view.getComponent();
    closePerformanceMonitor(viewComponent);
  }

  private void closePerformanceMonitor(JComponent viewComponent) {
    visualizerComponents.remove(viewComponent);

    if (tabbedPane != null) {
      tabbedPane.remove(viewComponent);
//      if (tabbedPane.getTabCount() == 1) {
//        JComponent tabComponent = (JComponent) tabbedPane.getComponent(0);
//        tabbedPane.remove(tabComponent);
//        performanceMonitorViewsArea.remove(tabbedPane);
//        tabbedPane = null;
//        performanceMonitorViewsArea.add(tabComponent);
//      }
    } else {
      performanceMonitorViewsArea.remove(viewComponent);
    }

    validate();
    repaint();
  }

  private class CloseListener implements PropertyChangeListener {

    public void propertyChange(java.beans.PropertyChangeEvent evt) {
      if (TabbedPaneFactory.PROP_CLOSE.equals(evt.getPropertyName())) {
        closePerformanceMonitor((JComponent) evt.getNewValue());
      }
    }
  }

}
