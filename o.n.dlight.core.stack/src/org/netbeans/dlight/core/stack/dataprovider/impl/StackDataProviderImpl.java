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
package org.netbeans.dlight.core.stack.dataprovider.impl;

import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.dlight.dataprovider.api.DataModelScheme;
import org.netbeans.modules.dlight.spi.dataprovider.DataProvider;
import org.netbeans.dlight.core.stack.dataprovider.StackDataModel;
import org.netbeans.dlight.core.stack.dataprovider.StackDataProvider;
import org.netbeans.dlight.core.stack.model.FunctionCall;
import org.netbeans.dlight.core.stack.model.FunctionMetric;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata.Column;
import org.netbeans.dlight.core.stack.storage.StackDataStorage;
import org.netbeans.dlight.core.stack.dataprovider.FunctionCallTreeTableNode;
import org.netbeans.modules.dlight.spi.storage.DataStorageTypeFactory;

/**
 * @author Alexey Vladykin
 */
public class StackDataProviderImpl implements StackDataProvider {

  private final List<FunctionMetric> metricsList = Arrays.<FunctionMetric>asList(
          FunctionMetric.CpuTimeInclusiveMetric, FunctionMetric.CpuTimeExclusiveMetric);
  private final List<DataStorageType> supportedStorageTypes = Arrays.asList(DataStorageTypeFactory.getInstance().getDataStorageType(StackDataStorage.STACK_DATA_STORAGE_TYPE_ID));
  private StackDataStorage storage;

  public String getID() {
    return "Stack Data Provider";
  }

  public List<? extends DataModelScheme> getProvidedDataModelScheme() {
    return Arrays.asList(StackDataModel.instance);
  }

  public boolean provides(DataModelScheme dataModel) {
    return getProvidedDataModelScheme().contains(dataModel);
  }

  public List<DataStorageType> getSupportedDataStorageTypes() {
    return supportedStorageTypes;
  }

  public void attachTo(DataStorage storage) {
    this.storage = (StackDataStorage) storage;
  }

  public DataProvider newInstance() {
    return new StackDataProviderImpl();
  }

  public List<FunctionMetric> getMetricsList() {
    return metricsList;
  }

  public List<FunctionCall> getCallers(FunctionCall[] path, boolean aggregate) {
    return storage.getCallers(path, aggregate);
  }

  public List<FunctionCall> getCallees(FunctionCall[] path, boolean aggregate) {
    return storage.getCallees(path, aggregate);
  }

  public List<FunctionCall> getHotSpotFunctions(List<Column> columns, List<Column> orderBy, int limit) {
    return storage.getHotSpotFunctions(FunctionMetric.CpuTimeInclusiveMetric, limit);
  }

  public List<FunctionCallTreeTableNode> getTableView(List<Column> columns, List<Column> orderBy, int limit) {
    return FunctionCallTreeTableNode.getFunctionCallTreeTableNodes(getHotSpotFunctions(null, null, limit));
  }

  public List<FunctionCallTreeTableNode> getChildren(List<FunctionCallTreeTableNode> path) {
    return FunctionCallTreeTableNode.getFunctionCallTreeTableNodes(getCallers(FunctionCallTreeTableNode.getFunctionCalls(path).toArray(new FunctionCall[0]), false));
  }

  public FunctionCallTreeTableNode getValueAt(int row) {
    //throw new UnsupportedOperationException("Not supported yet.");
    return null;
  }

  public String getTableValueAt(Column column, int row) {
    return null;
  }
}
