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
package org.netbeans.modules.dlight.spi.indicator;

import org.netbeans.modules.dlight.indicator.api.Indicator;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.dlight.execution.api.DLightTargetListener;
import org.netbeans.modules.dlight.indicator.api.IndicatorDataProviderConfiguration;
import org.netbeans.modules.dlight.spi.indicator.impl.IndicatorDataProviderFactoryAccessor;
import org.netbeans.modules.dlight.storage.api.DataRow;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata;

/**
 * Provided information for {@link org.netbeans.dlight.core.indicator.model.Indicator}.
 * As indicators are supposed to be small and really fast real time UI,
 * data provider for indicators doesn't have to place any data to storage it
 * can use {@link #notifyIndicators(java.util.List)} method with newly real-time data
 * to notify all indicators subscribed to it using {@link #subscribe(org.netbeans.dlight.core.indicator.model.Indicator) }
 */
public abstract class IndicatorDataProvider<T extends IndicatorDataProviderConfiguration> implements DLightTargetListener {

  private final List<Indicator> listeners = new ArrayList<Indicator>();

  public abstract String getID();

  /**
   * Factory method
   * @param configuration
   * @return
   */
  public abstract IndicatorDataProvider<T> create(T configuration);
  
  private void addIndicatorDataProviderListener(Indicator l) {
    if (!listeners.contains(l)) {
      listeners.add(l);
    }
  }

  private void removeIndicatorDataProviderListener(Indicator l) {
    listeners.remove(l);
  }

  /**
   * Try to subscibe indicator to this Indicator DataProvider.
   * To successfuly subscribe indicator {@link #getDataTablesMetadata()} should contain
   * columns which are required by indicator(the result of
   * {@link Indicator#getMetadataColumns()} method)
   * @param indicator indicator to subscribe
   * @return <code>true</code> if indicator was successfuly subscribed,
   * <code>false</code> otherwise
   */
  public final boolean subscribe(Indicator indicator) {
    List<DataTableMetadata.Column> indicatorColumns = indicator.getMetadataColumns();

    // if this provider provides at least one column of information
    // that indicator can display - subscribe it.
    // TODO: ???

    for (DataTableMetadata tdm : getDataTablesMetadata()) {
      List<DataTableMetadata.Column> providedColumns = tdm.getColumns();
      for (DataTableMetadata.Column pcol : providedColumns) {
        for (DataTableMetadata.Column icol : indicatorColumns) {
          if (icol.equals(pcol)) {
            addIndicatorDataProviderListener(indicator);
            return true;
          }
        }
      }
    }

    return false;
  }

  /**
   * Use this method to unsubscribe from this data provider
   * @param indicator indicator to unsubscribe
   */
  public final void unsubscribe(Indicator indicator) {
    removeIndicatorDataProviderListener(indicator);
  }

  protected final void resetIndicators() {
    for (Indicator l : listeners) {
      l.reset();
    }

  }

  protected final void notifyIndicators(List<DataRow> data) {
    for (Indicator l : listeners) {
      l.updated(data);
    }
  }

  /**
   * Returns the list of {@link org.netbeans.dlight.core.storage.model.DataTableMetadata}
   * this data provider can return information about
   * @return list of {@link org.netbeans.dlight.core.storage.model.DataTableMetadata}
   * this data provider can return information about
   */
  public abstract List<? extends DataTableMetadata> getDataTablesMetadata();

  private static final class IndicatorDataProviderAccessorImpl extends IndicatorDataProviderFactoryAccessor{

    @Override
    public IndicatorDataProvider create(IndicatorDataProviderConfiguration configuraiton) {
      throw new UnsupportedOperationException("Not supported yet.");
    }
    
  }
}
