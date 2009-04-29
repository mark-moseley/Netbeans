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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.management.api.DLightSession;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageFactory;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorageFactory;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.openide.util.Lookup;

public final class DataStorageManager {

    private Collection<? extends DataStorageFactory> dataStorageFactories;//this is to create new ones
    private Map<DLightSession, List<DataStorage>> activeDataStorages = new HashMap<DLightSession, List<DataStorage>>();
   private Map<DLightSession, List<ServiceInfoDataStorage>> activeServiceInfoDataStorages = new HashMap<DLightSession, List<ServiceInfoDataStorage>>();
    private static final Logger log = DLightLogger.getLogger(DataStorageManager.class);
    private static final DataStorageManager instance = new DataStorageManager();
    private DLightSession lastSession;

    private DataStorageManager() {
        dataStorageFactories = Lookup.getDefault().lookupAll(DataStorageFactory.class);
        log.info(dataStorageFactories.size() + " data storage(s) found!"); // NOI18N
    }

    public static DataStorageManager getInstance() {
        return instance;
    }

    public List<DataStorage> closeSession(DLightSession session) {
        if (session == null) {
            return null;
        }
        List<DataStorage> storages = activeDataStorages.get(session);
        if (storages != null) {
            for (DataStorage storage : storages) {
                if (!storage.shutdown()) {
                    log.finest("DataStorage " + storage + " is not closed");//NOI18N
                } else {
                    log.finest("DataStorage " + storage + " successfully closed");//NOI18N
                }
            }
        }
        List<ServiceInfoDataStorage> serviceInfoStorages = activeServiceInfoDataStorages.get(session);
        if (serviceInfoStorages != null) {
            for (ServiceInfoDataStorage storage : serviceInfoStorages) {
                if (storages == null || !storages.contains(storage)){
                    if (!storage.shutdown()) {
                        log.finest("ServiceInfoDataStorage " + storage + " is not closed");//NOI18N
                    } else {
                        log.finest("ServiceInfoDataStorage " + storage + " successfully closed");//NOI18N
                    }
                }
            }
        }
        return activeDataStorages.remove(session);
    }

    public void clearActiveStorages(DLightSession session) {
        List<DataStorage> storages = activeDataStorages.get(session);
        if (storages != null) {
            storages.clear();
        }
        lastSession = session;
    }

    public ServiceInfoDataStorage getServiceInfoDataStorage(DLightSession session) {
        if (session == null) {
            return null;
        }
        List<ServiceInfoDataStorage> activeSessionInfoStorages = activeServiceInfoDataStorages.get(session);
        if (activeSessionInfoStorages != null && activeSessionInfoStorages.size() > 0){
            return activeSessionInfoStorages.get(0);
        }
        List<DataStorage> activeSessionStorages = activeDataStorages.get(session);
        if (activeSessionStorages != null && activeSessionStorages.size() > 0) {
            ServiceInfoDataStorage result = activeSessionStorages.get(0);
            activeServiceInfoDataStorages.put(session, Arrays.asList(result));
            return result;
        }
        //we can get any
        ServiceInfoDataStorageFactory  serInfo = Lookup.getDefault().lookup(ServiceInfoDataStorageFactory.class);
        if (serInfo == null){
            return null;
        }
        ServiceInfoDataStorage result = serInfo.createStorage();
        activeServiceInfoDataStorages.put(session, Arrays.asList(result));
        return result;
    }

    /**
     *  Returns previously created or created new instance of DataStorage
     *  for requested schema (if it can be found within all available DataStorages)
     */
    public DataStorage getDataStorageFor(DLightSession session, DataCollector<?> collector) {
        Collection<DataStorageType> supportedTypes = collector.getSupportedDataStorageTypes();
        for (DataStorageType type : supportedTypes) {
            DataStorage storage = getDataStorageFor(session, type, collector.getDataTablesMetadata());

            if (storage != null) {
                return storage;
            }

        }
        return null;
    }

    public DataStorage getDataStorage(DataStorageType storageType) {
        return getDataStorageFor(lastSession, storageType, Collections.<DataTableMetadata>emptyList());
    }

//
//  public void registerDataStorage(DataStorage dataStorage){
//    if (allDataStorages.contains(dataStorage)){
//      return;
//    }
//    if (activeDataStorages.contains(log))
//  }
    private DataStorage getDataStorageFor(DLightSession session, DataStorageType storageType, List<DataTableMetadata> tableMetadatas) {
        if (session == null) {
            return null;
        }
        List<DataStorage> activeSessionStorages = activeDataStorages.get(session);
        if (activeSessionStorages != null) {
            for (DataStorage storage : activeSessionStorages) {
                if (storage.supportsType(storageType)) {
                    storage.createTables(tableMetadatas);
                    return storage;
                }
            }
        }
        //if no storage was created - create the new one
        for (DataStorageFactory storage : dataStorageFactories) {
            if (storage.getStorageTypes().contains(storageType)) {
                DataStorage newStorage = storage.createStorage();
                if (newStorage != null) {
                    newStorage.createTables(tableMetadatas);
                    if (activeSessionStorages == null) {
                        activeSessionStorages = new ArrayList<DataStorage>();
                    }
                    activeSessionStorages.add(newStorage);
                    activeDataStorages.put(session, activeSessionStorages);
                    return newStorage;
                }
            }
        }
        return null;
    }
}

