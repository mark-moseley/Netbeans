/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.indicator.api.impl;

import java.util.List;
import org.netbeans.modules.dlight.indicator.api.Indicator;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata.Column;
import org.netbeans.modules.dlight.visualizer.api.VisualizerConfiguration;

/**
 *
 * @author masha
 */
public abstract class IndicatorAccessor {

  private static volatile IndicatorAccessor DEFAULT;

  public static IndicatorAccessor getDefault() {
    IndicatorAccessor a = DEFAULT;
    if (a != null) {
      return a;
    }

    try {
      Class.forName(Indicator.class.getName(), true, Indicator.class.getClassLoader());//
    } catch (Exception e) {
    }
    return DEFAULT;
  }

  public static void setDefault(IndicatorAccessor accessor) {
    if (DEFAULT != null) {
      throw new IllegalStateException();
    }
    DEFAULT = accessor;
  }

  public IndicatorAccessor() {
  }

  public abstract void setToolName(Indicator ind, String toolName);
  public abstract String getToolName(Indicator ind);
  public abstract List<Column> getMetadataColumns(Indicator indicator);

  public abstract String getMetadataColumnName(Indicator indicator, int idx);

  public abstract VisualizerConfiguration getVisualizerConfiguration(Indicator indicator);

  public abstract void addIndicatorActionListener(Indicator indicator, IndicatorActionListener l);

  public abstract void removeIndicatorActionListener(Indicator indicator, IndicatorActionListener l);
}
