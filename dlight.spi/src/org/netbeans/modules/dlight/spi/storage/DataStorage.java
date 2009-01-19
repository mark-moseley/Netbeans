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
package org.netbeans.modules.dlight.spi.storage;

import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata;
import org.netbeans.modules.dlight.storage.api.DataRow;
import java.util.ArrayList;
import java.util.List;

/**
 * DataStorage stores information collected by DataCollectors.
 *
 * {@link org.netbeans.dlight.core.storage.model.DataStorageType} is
 * used to identify the way of communication with DataStorage
 * thus {@link org.netbeans.dlight.core.collector.model.DataCollector}
 * that whant to store some information into the DataStorage
 * need to support the same {@link org.netbeans.dlight.core.storage.model.DataStorageType}.
 * The same is for {@link org.netbeans.dlight.core.dataprovider.model.DataProvider},
 * that wants to read data from the DataStorage.
 */
public abstract class DataStorage {

  private final List<DataTableMetadata> tablesMetadata;

  protected DataStorage() {
    tablesMetadata = new ArrayList<DataTableMetadata>();
  }

  /**
   * Checks if storage contains data described by <param>data</param>
   * @param data data to check in storage. In case <param>data</param>
   * describes some virtual table and its {@link org.netbeans.dlight.core.storage.model.DataTableMetadata#getSourceTables()} method
   * returns not empty list, this method will check if all source tables this virtual table(view)
   * is built from  exists in the storage.
   * @return <code>true</code> if storage contains <param>data</param>,
   * <code>false</code> otherwise
   */
  public final boolean hasData(DataTableMetadata data) {
    List<DataTableMetadata> sourceTables = data.getSourceTables();
    if (sourceTables != null) {
      for (DataTableMetadata md : tablesMetadata) {
        for (DataTableMetadata t : sourceTables){
          if (!t.getName().equals(md.getName())){
            return false;
          }
        }
      }
      return true;
    }
    for (DataTableMetadata md : tablesMetadata) {
      if (md.getName().equals(data.getName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Factory method
   * @return
   */
  public abstract DataStorage newInstance();

  public abstract String getID();

  /**
   * Please be sure this method will return not null for the object
   * which will be registered in Services (using default public constructor 
   * without parameter)
   * @return storage types 
   */
  public abstract List<? extends DataStorageType> getStorageTypes();

  protected abstract boolean createTablesImpl(List<? extends DataTableMetadata> tableMetadatas);

  public abstract void addData(String tableName, List<DataRow> data);

  public boolean supportsType(DataStorageType storageType) {
    return getStorageTypes() != null && getStorageTypes().contains(storageType);
  }

  public void createTables(List<? extends DataTableMetadata> tableMetadatas) {
    if (createTablesImpl(tableMetadatas)) {      
        tablesMetadata.addAll(tableMetadatas);
     }
  }
}
