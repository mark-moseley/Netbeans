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
package org.netbeans.modules.dlight.indicator.api;

import java.awt.event.ActionListener;
import org.netbeans.modules.dlight.indicator.api.impl.IndicatorActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.modules.dlight.indicator.api.impl.IndicatorAccessor;
import org.netbeans.modules.dlight.storage.api.DataRow;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata.Column;
import org.netbeans.modules.dlight.visualizer.api.VisualizerConfiguration;

/**
 * Indicator is a small, graphical, real-time monitor
 * which shows some piece of info provided by {@link IndicatorDataProvider}
 */
public abstract class Indicator {

  private IndicatorMetadata metadata;
  private String toolName;
  private final List<IndicatorActionListener> listeners;
  private Collection<ActionListener> actionListeners;

  

  static {
    IndicatorAccessor.setDefault(new IndicatorAccessorImpl());
  }
  private VisualizerConfiguration visualizerConfiguraiton;

  public final void setVisualizerConfiguration(VisualizerConfiguration configuration) {
    this.visualizerConfiguraiton = configuration;
    getComponent().addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent e) {
        notifyListeners();
      }
    });
  }

  protected final void notifyListeners(){
    for (IndicatorActionListener l : listeners){
      l.mouseClickedOnIndicator(this);
    }
  }

  protected Indicator(IndicatorMetadata metadata) {
    listeners = Collections.synchronizedList(new ArrayList<IndicatorActionListener>());
    this.metadata = metadata;
  }

  VisualizerConfiguration getVisualizerConfiguration(){
    return visualizerConfiguraiton;
  }

  void addIndicatorActionListener(IndicatorActionListener l){
    if (!listeners.contains(l)){
      listeners.add(l);
    }
  }

  void setToolName(String toolName){
    this.toolName = toolName;
  }

  void removeIndicatorActionListener(IndicatorActionListener l){
    listeners.remove(l);
  }

  /**
   * Invoked when new data is occurred in {@link org.netbeans.dlight.core.indicator.model.IndicatorDataProvider}.
   * @param dataProvider data provider new data is occurred
   * @param data data added
   */
  public abstract void updated(List<DataRow> data);

  /**
   * Resets to the initial state
   */
  public abstract void reset();

  /**
   * Returns
   * @return
   */
  public IndicatorMetadata getMetadata() {
    return metadata;
  }

  public List<Column> getMetadataColumns() {
    return metadata.getColumns();
  }

  public String getMetadataColumnName(int idx) {
    Column col = metadata.getColumns().get(idx);
    return col.getColumnName();
  }

  /**
   * Returns component this indicator will paint data at
   * @return component this indicator will paint own data at
   */
  public abstract JComponent getComponent();

  private static class IndicatorAccessorImpl extends IndicatorAccessor {

    @Override
    public List<Column> getMetadataColumns(Indicator indicator) {
      return indicator.getMetadataColumns();
    }

    @Override
    public String getMetadataColumnName(Indicator indicator, int idx) {
      return indicator.getMetadataColumnName(idx);
    }

    @Override
    public VisualizerConfiguration getVisualizerConfiguration(Indicator indicator) {
      return indicator.getVisualizerConfiguration();
    }

    @Override
    public void addIndicatorActionListener(Indicator indicator, IndicatorActionListener l) {
      indicator.addIndicatorActionListener(l);
    }

    @Override
    public void removeIndicatorActionListener(Indicator indicator, IndicatorActionListener l) {
      indicator.removeIndicatorActionListener(l);
    }

    @Override
    public void setToolName(Indicator indicator, String toolName) {
      indicator.setToolName(toolName);
    }

    @Override
    public String getToolName(Indicator ind) {
      return ind.toolName;
    }
  }
}
