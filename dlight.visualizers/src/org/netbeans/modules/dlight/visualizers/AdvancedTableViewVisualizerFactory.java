/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.visualizers;

import org.netbeans.modules.dlight.spi.impl.TableDataProvider;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerDataProvider;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerFactory;
import org.netbeans.modules.dlight.visualizers.api.AdvancedTableViewVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.impl.VisualizerConfigurationIDsProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mt154047
 */
@ServiceProvider(service=org.netbeans.modules.dlight.spi.visualizer.VisualizerFactory.class)
public class AdvancedTableViewVisualizerFactory implements VisualizerFactory<AdvancedTableViewVisualizerConfiguration> {

    public String getID() {
        return VisualizerConfigurationIDsProvider.ADVANCED_TABLE_VISUALIZER;
    }

    public Visualizer<AdvancedTableViewVisualizerConfiguration> create(AdvancedTableViewVisualizerConfiguration configuration, VisualizerDataProvider provider) {
        if (!(provider instanceof TableDataProvider)){
            return null;
        }
        return new AdvancedTableViewVisualizer((TableDataProvider)provider, configuration);
    }

}
