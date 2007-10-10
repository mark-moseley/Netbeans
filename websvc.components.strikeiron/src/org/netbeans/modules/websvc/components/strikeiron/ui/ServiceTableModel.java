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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.components.strikeiron.ui;

import com.strikeiron.search.AUTHENTICATIONSTYLE;
import com.strikeiron.search.ArrayOfMarketPlaceService;
import com.strikeiron.search.LicenseInfo;
import com.strikeiron.search.MarketPlaceService;
import com.strikeiron.search.ObjectFactory;
import com.strikeiron.search.RegisteredUser;
import com.strikeiron.search.SISearchService;
import com.strikeiron.search.SISearchServiceSoap;
import com.strikeiron.search.SORTBY;
import com.strikeiron.search.SearchOutPut;
import com.sun.xml.ws.developer.WSBindingProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.components.ServiceData;
import org.netbeans.modules.websvc.manager.api.WebServiceDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author nam
 */
public class ServiceTableModel extends DefaultTableModel {
    public static final QName SI_SEARCH_SERVICE = new QName("http://www.strikeiron.com", "SISearchService");
    public static final String STRIKE_IRON_HOME = WebServiceDescriptor.WEBSVC_HOME + "/strikeiron";
    public static final String SEARCH_PROPERTIES = "search.properties";
    public static final String WSDL_LOCATION = "wsdlLocation";
    public static final String USERID = "userId";
    public static final String PASSWORD = "password";
    private static final String DEFAULT_USERID = "Sun_Search@strikeiron.com";
    private static final String DEFAULT_PASSWORD = "SearchSun.01";
    private static final String DEFAULT_URL = "http://ws.strikeiron.com/Searchsunsi01.StrikeIron/MarketplaceSearch?WSDL";
    
    private static final int COLUMN_WS_NAME = 0;
    private static final int COLUMN_SELECT = 2;
    private static final int COLUMN_PROVIDER = 1;
    
    private String wsdlLocation;
    private String userId = "Sun_Search@strikeiron.com";
    private String password = "SearchSun.01";
    private AUTHENTICATIONSTYLE authenticationStyle = AUTHENTICATIONSTYLE.SOAP_HEADER;
    private Boolean useCustomWSDL = Boolean.TRUE;
    private SORTBY sortBy = SORTBY.NAME;
    private SISearchService sservice;
    private List<? extends ServiceData> result;

    private String status;
    private Set<Integer> selectedRows = new HashSet<Integer>();
    private RequestProcessor.Task searchTask;

    public ServiceTableModel() {
        init();
    }
    
    private void init() {
        Properties p = new Properties();
        File propFile = new File(STRIKE_IRON_HOME, SEARCH_PROPERTIES);
        if (propFile.isFile()) {
            try {
                p.load(new FileInputStream(propFile));
            } catch(IOException ioe) {
                // OK
            }
        }
        wsdlLocation = p.getProperty(WSDL_LOCATION);
        if (wsdlLocation == null) {
            wsdlLocation = DEFAULT_URL;
        }
        userId = p.getProperty(USERID);
        if (userId == null) {
            userId = DEFAULT_USERID;
        }
        password =  p.getProperty(PASSWORD);
        if (password == null) {
            password = DEFAULT_PASSWORD;
        }
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String value) {
        userId = value;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String value) {
        password = value;
    }
    
    public String getSearchServiceUrl() {
        return getWsdlLocation().toExternalForm();
    }

    public ServiceData getService(int row) {
        return result.get(row);
    }
    
    public static final String SEARCH_COMPLETE = "searchCompleted";
    public static final String SEARCH_CANCELLED = "searchCancelled";
    public static final String SEARCH_ERROR = "searchError";
    
    public interface SearchListener extends EventListener {
        void searchCompleted(ChangeEvent e);
    }
    
    List<SearchListener> listeners = new ArrayList<SearchListener>();
    public void addEventListener(SearchListener listener) {
        listeners.add(listener);
    }
    public void removeEventListener(SearchListener listener) {
        listeners.remove(listener);
    }
    private void fireSearchEnded() {
        for (SearchListener l : listeners) {
            l.searchCompleted(new ChangeEvent(this));
        }
    }
    
    public void doSearch(final String searchTerm, final SORTBY sortBy) {
        cancelSearch();
        searchTask = RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                callSearch(searchTerm, sortBy);
            }
        });
        searchTask.run();
    }
    
    public void cancelSearch() {
        if (searchTask != null) {
            searchTask.cancel();
            fireSearchEnded();
            searchTask = null;
        }
    }
    
    public Set<? extends ServiceData> getSelectedServices() {
        Set<ServiceData> selection = new HashSet<ServiceData>();
        for (int i : selectedRows) {
            selection.add(result.get(i));
        }
        return selection;
    }
    
    public String getStatus() {
        return status;
    }

    private URL getWsdlLocation() {
        if (wsdlLocation == null) {
            return null;
        }
        try {
            return new URL(wsdlLocation);
        } catch(Exception ex) {
            Logger.global.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            return null;
        }
    }
    
    private List<? extends ServiceData> convertResult(List<MarketPlaceService> rawResult) {
        List<ServiceData> converted = new ArrayList<ServiceData>();
        if (rawResult != null) {
            for (MarketPlaceService service : rawResult) {
                ServiceData raw = new SiServiceData(service);
                converted.add(raw);
            }
        }
        return converted;
    }
    
    private void callSearch(String searchTerm, SORTBY sortBy) {
        status = null;
        selectedRows = new HashSet<Integer>();
        result = new ArrayList<ServiceData>();
        fireTableDataChanged();
        
        if (sortBy == null) {
            sortBy = this.sortBy;
        }
        try {
            // init service only when needed to avoid unecessary internet access
            if (sservice == null) {
                URL url = getWsdlLocation();
                if (url == null) {
                    sservice = new SISearchService();
                } else {
                    sservice = new SISearchService(url, SI_SEARCH_SERVICE);
                }
            }
            SISearchServiceSoap port = sservice.getSISearchServiceSoap();
            setHeaderParameters(port);
            SearchOutPut output = port.search(searchTerm, sortBy, useCustomWSDL, authenticationStyle);
            if (output != null) {
                ArrayOfMarketPlaceService amps = output.getStrikeIronWebServices();
                if (amps != null) {
                    result = convertResult(output.getStrikeIronWebServices().getMarketPlaceService());
                }
            }
            if (output != null && output.getServiceStatus() != null) {
                status = output.getServiceStatus().getStatusDescription();
            }
            fireTableDataChanged();
        } catch (Exception ex) {
            status = ex.getLocalizedMessage();
        } finally {
            fireSearchEnded();
            searchTask = null;
        }
    }
    
    private void setHeaderParameters(SISearchServiceSoap port) {
        RegisteredUser ru = new RegisteredUser();
        ru.setUserID(userId);
        ru.setPassword(password);
        LicenseInfo li = new LicenseInfo();
        li.setRegisteredUser(ru);
        WSBindingProvider bp = (WSBindingProvider) port;
        bp.setOutboundHeaders(new ObjectFactory().createLicenseInfo(li));
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Class getColumnClass(int column) {
        if (column == COLUMN_SELECT) {
            return Boolean.class;
        }
        return String.class;
    }
    
    @Override
    public String getColumnName(int column) {
        switch(column) {
        case COLUMN_WS_NAME:
            return NbBundle.getMessage(ServiceTableModel.class, "LBL_ServiceName");
        case COLUMN_SELECT:
            return NbBundle.getMessage(ServiceTableModel.class, "LBL_Select");
        case COLUMN_PROVIDER:
            return NbBundle.getMessage(ServiceTableModel.class, "LBL_ProviderName");
        }
        throw new IllegalArgumentException("column > 2"); //NOI18N
    }

    public boolean isSearching() {
        return searchTask != null && result ==  null;
    }
    
    @Override
    public int getRowCount() {
        if (isSearching()) {
            return 1;
        } else {
            return result != null ? result.size() : 0;
        }
    }
    
    @Override
    public Object getValueAt(int row, int column) {
        if (isSearching()) {
            return NbBundle.getMessage(ServiceTableModel.class, "MSG_Searching");
        } else if (result == null) {
            throw new IllegalStateException("Search has not started or has no results");
        }
        
        ServiceData mps = result.get(row);
        switch(column) {
        case COLUMN_WS_NAME:
            return mps.getServiceName();
        case COLUMN_SELECT:
            return selectedRows.contains(row);
        case COLUMN_PROVIDER:
            return mps.getProviderName();
        default:
            throw new IllegalArgumentException("column = "+column); //NOI18N
        }
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        if (column == COLUMN_SELECT && value instanceof Boolean) {
            boolean selected = (Boolean) value;
            if (selected) {
                selectedRows.add(row);
            } else {
                selectedRows.remove(row);
            }
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        switch(column) {
        case COLUMN_SELECT:
            return true;
        default:
            return false;
        }
        
    }
    
}
