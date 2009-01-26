/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.api.impl;

import org.netbeans.modules.dlight.api.indicator.ConfigurationData;
import org.netbeans.modules.dlight.api.indicator.IndicatorConfiguration;
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;

/**
 *
 * @author mt154047
 */
public abstract class IndicatorConfigurationAccessor {

  private static volatile IndicatorConfigurationAccessor DEFAULT;

  public static IndicatorConfigurationAccessor getDefault() {
    IndicatorConfigurationAccessor a = DEFAULT;
    if (a != null) {
      return a;
    }

    try {
      Class.forName(IndicatorConfiguration.class.getName(), true, IndicatorConfiguration.class.getClassLoader());//
    } catch (Exception e) {
    }
    return DEFAULT;
  }

  public static void setDefault(IndicatorConfigurationAccessor accessor) {
    if (DEFAULT != null) {
      throw new IllegalStateException();
    }
    DEFAULT = accessor;
  }

  public IndicatorConfigurationAccessor() {
  }

  public abstract ConfigurationData getConfigurationData(IndicatorConfiguration configuration);

  public abstract IndicatorMetadata getIndicatorMetadata(IndicatorConfiguration configuration);

  public abstract VisualizerConfiguration getVisualizerConfiguration(IndicatorConfiguration configuration);
}
