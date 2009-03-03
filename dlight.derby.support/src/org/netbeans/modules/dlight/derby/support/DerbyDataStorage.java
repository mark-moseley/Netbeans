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
package org.netbeans.modules.dlight.derby.support;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.core.stack.api.FunctionMetric;
import org.netbeans.modules.dlight.core.stack.storage.SQLStackStorage;
import org.netbeans.modules.dlight.core.stack.storage.StackDataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.impl.SQLDataStorage;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.util.Util;

/**
 *
 */
public class DerbyDataStorage extends SQLDataStorage implements StackDataStorage {

  
  private static final String SQL_QUERY_DELIMETER = "";
  static private int dbIndex = 1;
  private static final Logger logger = DLightLogger.getLogger(DerbyDataStorage.class);
  private static boolean driverLoaded = false;
  private SQLStackStorage stackStorage;
  private final List<DataStorageType> supportedStorageTypes = new ArrayList<DataStorageType>();


  static {
    Util.deleteLocalDirectory(new File("/tmp/derby_dlight"));
       try {
        String systemDir = "/tmp/derby_dlight";
        System.setProperty("derby.system.home", systemDir);
        Class driver = Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        logger.info("Driver for Derby(JavaDB) (" + driver.getName() + ") Loaded ");
      } catch (Exception ex) {
        logger.log(Level.SEVERE, null, ex);
      }

  }

  /**
   * Empty constructor, used by Lookup
   */
  public DerbyDataStorage() {
    this("jdbc:derby:DerbyDlight" + (dbIndex++) + ";create=true;user=dbuser;password=dbuserpswd");
  
  }

  private void initStorageTypes(){
    supportedStorageTypes.add(DataStorageTypeFactory.getInstance().getDataStorageType(DerbyDataStorageFactory.DERBY_DATA_STORAGE_TYPE));
    supportedStorageTypes.add(DataStorageTypeFactory.getInstance().getDataStorageType(StackDataStorage.STACK_DATA_STORAGE_TYPE_ID));
    supportedStorageTypes.addAll(super.getStorageTypes());

  }

  private DerbyDataStorage(String url) {
    super(url);
    try {
      initStorageTypes();
      stackStorage = new SQLStackStorage(this);
      connection.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
    } catch (SQLException ex) {
      logger.log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      logger.log(Level.SEVERE, null, ex);
    }
  }

  @Override
  protected String classToType(Class clazz) {
    if (clazz == Integer.class){
      return "integer";
    }
    return super.classToType(clazz);
  }

  @Override
  protected void connect(String dburl) {
    try {
      connection = DriverManager.getConnection(dburl);
    } catch (SQLException ex) {
      logger.log(Level.SEVERE, null, ex);
    }
  //super.connect(dburl);
  }

  @Override
  public Collection<DataStorageType> getStorageTypes() {
    return supportedStorageTypes;
  }

  @Override
  public boolean createTablesImpl(List<DataTableMetadata> tableMetadatas) {
    for (DataTableMetadata tdmd : tableMetadatas) {
      if (tdmd.getName().equals(STACK_METADATA_VIEW_NAME)) {
        if (!tables.containsKey(STACK_METADATA_VIEW_NAME)) {
          tables.put(STACK_METADATA_VIEW_NAME, tdmd);
        }
        continue;
      }
      if (!createTable(tdmd)) {
        return false;
      }
    }

    return true;
  }

  public int putStack(List<CharSequence> stack, long sampleDuration) {
    return stackStorage.putStack(stack, sampleDuration);
  }

  public void flush() {
    try {
      stackStorage.flush();
    } catch (InterruptedException ex) {
      logger.log(Level.SEVERE, null, ex);
    }
  }

  public List<Long> getPeriodicStacks(long startTime, long endTime, long interval) {
    try {
      return stackStorage.getPeriodicStacks(startTime, endTime, interval);
    } catch (SQLException ex) {
      logger.log(Level.SEVERE, null, ex);
      return null;
    }
  }

  public List<FunctionMetric> getMetricsList() {
    return stackStorage.getMetricsList();
  }

  public List<FunctionCall> getCallers(FunctionCall[] path, boolean aggregate) {
    try {
      return stackStorage.getCallers(path, aggregate);
    } catch (SQLException ex) {
      logger.log(Level.SEVERE, null, ex);
      return new ArrayList<FunctionCall>();
    }
  }

  public List<FunctionCall> getCallees(FunctionCall[] path, boolean aggregate) {
    try {
      return stackStorage.getCallees(path, aggregate);
    } catch (SQLException ex) {
      logger.log(Level.SEVERE, null, ex);
      return new ArrayList<FunctionCall>();
    }
  }

  public List<FunctionCall> getHotSpotFunctions(FunctionMetric metric, int limit) {
    try {
      return stackStorage.getHotSpotFunctions(metric, limit);
    } catch (SQLException ex) {
      logger.log(Level.SEVERE, null, ex);
      return new ArrayList<FunctionCall>();
    }
  }

  @Override
  protected String getSQLQueriesDelimeter() {
    return SQL_QUERY_DELIMETER;
  }
}
