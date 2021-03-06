/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.api.db.explorer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.db.explorer.actions.AddDriverAction;
import org.netbeans.modules.db.explorer.driver.JDBCDriverConvertor;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 * This class manages the list of JDBC drivers registered in the Database Explorer.
 */
public final class JDBCDriverManager {
    
    /**
     * The JDBCDriverManager singleton instance.
     */
    private static JDBCDriverManager DEFAULT = null;
    
    private Lookup.Result result = getLookupResult();
    
    /**
     * The list of listeners.
     */
    private List/*<JDBCDriverListener>*/ listeners = new ArrayList(1);
    
    /**
     * 
     * Gets the JDBCDriverManager singleton instance.
     * 
     * @return the JDBCDriverManager singleton instance.
     */
    public static synchronized JDBCDriverManager getDefault() {
        if (DEFAULT == null) {
            JDBCDriverConvertor.importOldDrivers();
            DEFAULT = new JDBCDriverManager();
        }
        return DEFAULT;
    }

    /**
     * Private constructor.
     */
    private JDBCDriverManager() {
        // issue 75204: forces the DataObject's corresponding to the JDBCDriver's
        // to be initialized and held strongly so the same JDBCDriver is
        // returns as long as it is held strongly
        result.allInstances(); 

        result.addLookupListener(new LookupListener() {
            public void resultChanged(LookupEvent e) {
                fireListeners();
            }
        });
    }
    
    /** 
     * Gets the registered JDBC drivers.
     *
     * @return a non-null array of JDBCDriver instances.
     */
    public JDBCDriver[] getDrivers() {
        Collection drivers = result.allInstances();
        return (JDBCDriver[])drivers.toArray(new JDBCDriver[drivers.size()]);
    }
    
    /**
     * Gets the registered JDBC drivers with the specified class name.
     *
     * @param drvClass driver class name; must not be null.
     *
     * @return a non-null array of JDBCDriver instances with the specified class name.
     *
     * @throws NullPointerException if the specified class name is null.
     */
    public JDBCDriver[] getDrivers(String drvClass) {
        if (drvClass == null) {
            throw new NullPointerException();
        }
        LinkedList result = new LinkedList();
        JDBCDriver[] drvs = getDrivers();
        for (int i = 0; i < drvs.length; i++) {
            if (drvClass.equals(drvs[i].getClassName())) {
                result.add(drvs[i]);
            }
        }
        return (JDBCDriver[])result.toArray(new JDBCDriver[result.size()]);
    }

    /**
     * Adds a new JDBC driver.
     * 
     * @param driver the JDBCDriver instance describing the driver to be added;
     * must not be null.
     *
     * @throws NullPointerException if the specified driver is null.
     *         DatabaseException if an error occurred while adding the driver.
     */
    public void addDriver(JDBCDriver driver) throws DatabaseException {
        if (driver == null) {
            throw new NullPointerException();
        }
        try {
            JDBCDriverConvertor.create(driver);
        } catch (IOException ioe) {
            throw new DatabaseException(ioe);
        }
    }
    
    /**
     * Removes a JDBC driver.
     * 
     * @param driver the JDBCDriver instance to be removed.
     *
     * @throws DatabaseException if an error occurred while adding the driver.
     */
    public void removeDriver(JDBCDriver driver) throws DatabaseException {
        try {
            JDBCDriverConvertor.remove(driver);
        } catch (IOException ioe) {
            throw new DatabaseException(ioe);
        }
    }
    
    /**
     * Shows the Add Driver dialog, allowing the user to add a new JDBC driver.
     */
    public void showAddDriverDialog() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new AddDriverAction.AddDriverDialogDisplayer().showDialog();
                }
            });
        } else {
            new AddDriverAction.AddDriverDialogDisplayer().showDialog();
        }
    }
    
    /**
     * Registers a JDBCDriverListener.
     */
    public void addDriverListener(JDBCDriverListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    /**
     * Unregisters the specified JDBCDriverListener.
     */
    public void removeDriverListener(JDBCDriverListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    private void fireListeners() {
        List/*<JDBCDriverListener>*/ listenersCopy;
        
        synchronized (listeners) {
            listenersCopy = new ArrayList(listeners);
        }
        
        for (Iterator i= listenersCopy.iterator(); i.hasNext();) {
            JDBCDriverListener listener = (JDBCDriverListener)i.next();
            listener.driversChanged();
        }
    }
    
    private synchronized Lookup.Result getLookupResult() {
        return Lookups.forPath(JDBCDriverConvertor.DRIVERS_PATH).lookupResult(JDBCDriver.class);
    }
}
