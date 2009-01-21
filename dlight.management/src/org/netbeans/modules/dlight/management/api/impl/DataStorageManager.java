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
package org.netbeans.modules.dlight.management.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.collector.spi.DataCollector;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata;
import org.netbeans.modules.dlight.storage.spi.DataStorage;
import org.netbeans.modules.dlight.storage.spi.DataStorageType;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.openide.util.Lookup;



public final class DataStorageManager {

  private Collection<? extends DataStorage> allDataStorages;//this is to create new ones
  private List<DataStorage> activeDataStorages = new ArrayList<DataStorage>();
  private static final Logger log = DLightLogger.getLogger(DataStorageManager.class);
  private static final DataStorageManager instance = new DataStorageManager();

  private DataStorageManager() {
    allDataStorages = Lookup.getDefault().lookupAll(DataStorage.class);
    log.info(allDataStorages.size() + " data storage(s) found!"); // NOI18N
  }

  public static DataStorageManager getInstance() {
    return instance;
  }

  public void clearActiveStorages(){
    activeDataStorages.clear();
  }

  /**
   *  Returns previously created or created new instance of DataStorage
   *  for requested schema (if it can be found within all available DataStorages)
   */
  public DataStorage getDataStorageFor(DataCollector collector) {
    List<DataStorageType> supportedTypes = collector.getSupportedDataStorageTypes();
    for (DataStorageType type : supportedTypes) {
      DataStorage storage = getDataStorageFor(type, collector.getDataTablesMetadata());

      if (storage != null) {
        return storage;
      }
      
    }
    return null;
  }

   public DataStorage getDataStorage(DataStorageType storageType){
    return getDataStorageFor(storageType, Collections.<DataTableMetadata>emptyList());
  }

//
//  public void registerDataStorage(DataStorage dataStorage){
//    if (allDataStorages.contains(dataStorage)){
//      return;
//    }
//    if (activeDataStorages.contains(log))
//  }

  private DataStorage getDataStorageFor(DataStorageType storageType, List<? extends DataTableMetadata> tableMetadatas) {
    for (DataStorage storage :activeDataStorages){
      if (storage.supportsType(storageType)) {
        storage.createTables(tableMetadatas);
        return storage;
      }
    }
    //if no storage was created - create the new one
    for (DataStorage storage : allDataStorages) {
      if (storage.supportsType(storageType)) {
        DataStorage newStorage = storage.newInstance();
        newStorage.createTables(tableMetadatas);
        activeDataStorages.add(newStorage);
        return newStorage;
      }
    }
    return null;
  }
}

