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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.autoupdate.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Radek Matous
 */
public class SettingsTableModel extends AbstractTableModel {
    private static final String[] COLUMN_NAME_KEYS = new String[] {
        "SettingsTable_ActiveColumn",
        "SettingsTable_NameColumn",
        /*"SettingsTable_URLColumn"*/
    };
    
    private static final Class[] COLUMN_TYPES = new Class[] {
        Boolean.class,
        UpdateUnitProvider.class,
        /*String.class*/
    };
    private List<UpdateUnitProvider> updateProviders;
    private Set<String> originalProviders;
    private PluginManagerUI pluginManager = null;
    
    private final Logger logger = Logger.getLogger ("org.netbeans.modules.autoupdate.ui.SettingsTableModel");
    /** Creates a new instance of SettingsTableModel */
    public SettingsTableModel () {
        refreshModel ();
    }
    
    void setPluginManager (PluginManagerUI manager) {
        this.pluginManager = manager;
    }
    
    PluginManagerUI getPluginManager () {
        return pluginManager;
    }
        
    void refreshModel () {
        Set<String> oldValue = originalProviders;
        Set<String> newValue = new HashSet<String> ();
        final List<UpdateUnitProvider> forRefresh = new ArrayList<UpdateUnitProvider> ();
        List<UpdateUnitProvider> providers = UpdateUnitProviderFactory.getDefault ().getUpdateUnitProviders (false);
        for (UpdateUnitProvider p : providers) {
            if (oldValue != null && !oldValue.contains (p.getName ())) {
                // new one provider
                if (p.isEnabled ()) {
                    forRefresh.add (p);
                }
            }
            newValue.add (p.getName ());
        }
        if (! forRefresh.isEmpty ()) {
            getPluginManager ().setWaitingState (true);
            Utilities.startAsWorkerThread (new Runnable () {
                public void run () {
                    try {
                        Utilities.presentRefreshProviders (forRefresh, getPluginManager (), true);
                        getPluginManager ().updateUnitsChanged ();
                    } finally {
                        getPluginManager ().setWaitingState (false);
                    }
                }
            });
        }
        // check removed providers
        if (oldValue != null && ! oldValue.isEmpty () && ! newValue.containsAll (oldValue)) {
            getPluginManager ().setWaitingState (true);
            Utilities.startAsWorkerThread (new Runnable () {
                public void run () {
                    try {
                        getPluginManager ().updateUnitsChanged ();
                    } finally {
                        getPluginManager ().setWaitingState (false);
                    }
                }
            });
        }
        updateProviders = new ArrayList<UpdateUnitProvider> (providers);
        originalProviders = newValue;
        sortAlphabetically (updateProviders);
        fireTableDataChanged ();
    }
    
    public void remove (int rowIndex) {
        UpdateUnitProvider unitProvider = getUpdateUnitProvider (rowIndex);
        if (unitProvider != null) {
            UpdateUnitProviderFactory.getDefault ().remove (unitProvider);
        }
        getPluginManager ().updateUnitsChanged ();
    }
    
    public void add (String name, String displayName, URL url, boolean state) {
        final UpdateUnitProvider uup = UpdateUnitProviderFactory.getDefault ().create (name, displayName, url);
        uup.setEnable (state);
    }
    
    public UpdateUnitProvider getUpdateUnitProvider (int rowIndex) {
        return (rowIndex >= 0 && rowIndex <  updateProviders.size ()) ? updateProviders.get (rowIndex) : null;
    }
    
    @Override
    public boolean isCellEditable (int rowIndex, int columnIndex) {
        return columnIndex == 0;
    }
    
    public int getRowCount () {
        return updateProviders.size ();
    }
    
    public int getColumnCount () {
        return COLUMN_NAME_KEYS.length;
    }
    
    @Override
    public void setValueAt (Object aValue, int rowIndex, int columnIndex) {
        final UpdateUnitProvider unitProvider = getUpdateUnitProvider (rowIndex);
        switch(columnIndex) {
        case 0:
            boolean oldValue = unitProvider.isEnabled ();
            boolean newValue = ((Boolean) aValue).booleanValue ();
            if (oldValue != newValue) {
                unitProvider.setEnable (newValue);
                if (oldValue) {
                    RequestProcessor.getDefault ().post (new Runnable () {
                        public void run () {
                            // was enabled and won't be more -> remove it from model
                            getPluginManager ().setWaitingState (true);
                            Utilities.startAsWorkerThread (new Runnable () {
                                public void run () {
                                    try {
                                        getPluginManager ().updateUnitsChanged ();
                                    } finally {
                                        getPluginManager ().setWaitingState (false);
                                    }
                                }
                            });
                        }
                    });
                } else {
                    // was enabled and won't be more -> add it from model and read its content
                    getPluginManager ().setWaitingState (true);
                    Utilities.startAsWorkerThread (new Runnable () {
                        public void run () {
                            try {
                                Utilities.presentRefreshProvider (unitProvider, getPluginManager (), false);
                                getPluginManager ().updateUnitsChanged ();
                            } finally {
                                getPluginManager ().setWaitingState (false);
                            }
                        }
                    });
                }
            }
            break;
        }
    }
    
    public Object getValueAt (int rowIndex, int columnIndex) {
        Object retval = null;
        UpdateUnitProvider unitProvider = updateProviders.get (rowIndex);
        switch(columnIndex) {
        case 0: retval = unitProvider.isEnabled ();break;
        case 1: retval = unitProvider;break;
            /*case 2: URL url = unitProvider.getProviderURL();
            retval = (url != null) ? url.toExternalForm() : "";//NOI18N
            break;*/
        }
        return retval;
    }
    
    @Override
    public Class<?> getColumnClass (int columnIndex) {
        return COLUMN_TYPES[columnIndex];
    }
    
    @Override
    public String getColumnName (int columnIndex) {
        return NbBundle.getMessage (SettingsTableModel.class, COLUMN_NAME_KEYS[columnIndex]);
    }
    private static void sortAlphabetically (List<UpdateUnitProvider> res) {
        Collections.sort (res, new Comparator<UpdateUnitProvider>(){
            public int compare (UpdateUnitProvider arg0, UpdateUnitProvider arg1) {
                return arg0.getDisplayName ().compareTo (arg1.getDisplayName ());
            }
        });
        
    }
    
}
