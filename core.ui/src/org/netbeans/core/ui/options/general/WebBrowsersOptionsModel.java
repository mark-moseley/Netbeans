/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.core.ui.options.general;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.DefaultListModel;
import org.openide.cookies.InstanceCookie;
import org.openide.explorer.propertysheet.PropertyPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Model for web browsers options panel accessible from General options panel
 * 
 * @author Milan Kubec
 */
public class WebBrowsersOptionsModel extends DefaultListModel {
    
    private static final String BROWSERS_FOLDER = "Services/Browsers"; // NOI18N
    private static final String BROWSER_TEMPLATE = "Templates/Services/Browsers/ExtWebBrowser.settings"; // NOI18N
    
    private static final String EA_HIDDEN = "hidden"; // NOI18N
    
    private enum ChangeStatus { NONE, REMOVED, ADDED }
    
    private List<WebBrowserDesc> browsersList = new ArrayList<WebBrowserDesc>();
    private Map<Integer,WebBrowserDesc> index2desc = new TreeMap<Integer,WebBrowserDesc>();
    
    private boolean isAdjusting = false;
    private Object selectedValue = null;
    
    public WebBrowsersOptionsModel() {
        
        FileObject servicesBrowsers = Repository.getDefault().getDefaultFileSystem().findResource(BROWSERS_FOLDER);
            
        if (servicesBrowsers != null) {
            
            DataFolder folder = DataFolder.findFolder(servicesBrowsers);
            DataObject[] browserSettings = folder.getChildren();
            
            for (DataObject browserSetting : browserSettings) {
                
                InstanceCookie cookie = browserSetting.getCookie(InstanceCookie.class);
                FileObject primaryFile = browserSetting.getPrimaryFile();
                
                if (cookie != null && !Boolean.TRUE.equals(primaryFile.getAttribute(EA_HIDDEN))) {
                    WebBrowserDesc browserDesc = new WebBrowserDesc(browserSetting);
                    browsersList.add(browserDesc);
                }
                
            }
            
        }
        
        int index = 0;
        for (WebBrowserDesc desc : browsersList) {
            addElement(desc.getOrigName());
            index2desc.put(index++, desc);
        }
        
    }
    
    public void addBrowser() {
        WebBrowserDesc desc = new WebBrowserDesc();
        desc.setChangeStatus(ChangeStatus.ADDED);
        browsersList.add(desc);
        adjustListItems();
    }
    
    public void removeBrowser(int idx) {
        index2desc.get(idx).setChangeStatus(ChangeStatus.REMOVED);
        adjustListItems();
    }
    
    private void adjustListItems() {
        isAdjusting = true;
        removeAllElements();
        index2desc.clear();
        int index = 0;
        for (WebBrowserDesc desc : browsersList) {
            if (!desc.getChangeStatus().equals(ChangeStatus.REMOVED)) {
                String newName = desc.getNewName();
                if (newName != null) {
                    addElement(newName);
                } else {
                    addElement(desc.getOrigName());
                }
                index2desc.put(index++, desc);
            }
        }
        isAdjusting = false;
    }
    
    public boolean isAdjusting() {
        return isAdjusting;
    }
    
    public void updateList() {
        adjustListItems();
    }
    
    public PropertyPanel getPropertyPanel(int index) {
        return index2desc.get(index).getPropertyPanel();
    }
    
    public String getPropertyPanelID(int index) {
        return index2desc.get(index).getPropertyPanelID();
    }
    
    public String getBrowserName(int index) {
        String retVal = null;
        String newName = index2desc.get(index).getNewName();
        if (newName != null) {
            retVal = newName;
        } else {
            retVal = index2desc.get(index).getOrigName();
        }
        return retVal;
    }
    
    public void setBrowserName(int index, String name) {
        index2desc.get(index).setNewName(name);
    }
    
    public void setSelectedValue(Object obj) {
        selectedValue = obj;
    }
    
    public Object getSelectedValue() {
        return selectedValue;
    }
    
    public void applyChanges() {
        // call applyChanges on all web browser desc
        for (WebBrowserDesc desc : browsersList) {
            desc.applyChanges();
        }
    }
    
    public void discardChanges() {
        // discard already created setting objects
        for (WebBrowserDesc desc : browsersList) {
            desc.discardChanges();
        }
    }
    
    @SuppressWarnings("unchecked")
    public List<PropertyPanelDesc> getPropertyPanels() {
        List list = new ArrayList<PropertyPanelDesc>();
        Collection<WebBrowserDesc> col = index2desc.values();
        for (WebBrowserDesc wbd : col) {
            list.add(new PropertyPanelDesc(wbd.getPropertyPanel(), wbd.getPropertyPanelID()));
        }
        return list;
    }
    
    public static class PropertyPanelDesc {
        public PropertyPanel panel;
        public String id;
        public PropertyPanelDesc(PropertyPanel pp, String s) {
            panel = pp;
            id = s;
        }
    }
    
    // -------------------------------------------------------------------------
    
    private static class WebBrowserDesc {
        
        private String origName = null;
        private String newName = null;
        
        private ChangeStatus changeStatus = ChangeStatus.NONE;
        
        private DataObject browserSettings;
        
        private PropertyPanel propertyPanel = null;
        private String propertyPanelID;
        
        private static int propertyPanelIDCounter = 0;
        
        public WebBrowserDesc() {
            newName = NbBundle.getBundle(WebBrowsersOptionsModel.class).getString("LBL_ExternalBrowser_Name");
            browserSettings = createNewBrowserSettings(newName);
            findPropertyPanel();
        }
        
        public WebBrowserDesc(DataObject brSettings) {
            browserSettings = brSettings;
            origName = browserSettings.getNodeDelegate().getDisplayName();
            findPropertyPanel();
        }
        
        private void findPropertyPanel() {
            
            try {
                
                InstanceCookie cookie = browserSettings.getCookie(InstanceCookie.class);
                PropertyDescriptor[] propDesc = Introspector.getBeanInfo(cookie.instanceClass()).getPropertyDescriptors();
                
                PropertyDescriptor fallbackProp = null;
                
                for (PropertyDescriptor pd : propDesc ) {
                    
                    if (fallbackProp == null && !pd.isExpert() && !pd.isHidden()) {
                        fallbackProp = pd;
                    }
                    
                    if (pd.isPreferred() && !pd.isExpert() && !pd.isHidden()) {
                        propertyPanel = new WebBrowsersPropertyPanel(cookie.instanceCreate(), 
                                pd.getName(), PropertyPanel.PREF_CUSTOM_EDITOR);
                        propertyPanelID = "PROPERTY_PANEL_" + propertyPanelIDCounter++;
                        break;
                    }
                    
                }
                
                if (propertyPanel == null) {
                    propertyPanel = new WebBrowsersPropertyPanel(cookie.instanceCreate(), 
                            fallbackProp.getName(), PropertyPanel.PREF_CUSTOM_EDITOR);
                    propertyPanelID = "PROPERTY_PANEL_" + propertyPanelIDCounter++;
                }
                
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            
        }
        
        public PropertyPanel getPropertyPanel() {
            return propertyPanel;   
        }
        
        public String getPropertyPanelID() {
            return propertyPanelID;
        }
        
        public String getOrigName() {
            return origName;
        }
        
        public String getNewName() {
            return newName;
        }
        
        public void setNewName(String name) {
            newName = name;
        }
        
        public void setChangeStatus(ChangeStatus stat) {
            changeStatus = stat;
        }
        
        public ChangeStatus getChangeStatus() {
            return changeStatus;
        }
        
        public void applyChanges() {
            
            // save changed values
            propertyPanel.updateValue();
            
            // delete settings from disk for removed browsers
            if (getChangeStatus() == ChangeStatus.REMOVED) {
                if (browserSettings != null) {
                    try {
                        browserSettings.delete();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            
            // update name of the browser
            if (newName != null && !newName.equals(origName)) {
                try {
                    browserSettings.rename(newName);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
        }
        
        // Called if user cancels the customizer dialog
        // newly created settings must be deleted
        public void discardChanges() {
            
            ChangeStatus status = getChangeStatus();
            
            if (status == ChangeStatus.ADDED) {
                
                if (browserSettings != null) {
                    try {
                        browserSettings.delete();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                
            }
            
        }
        
        private DataObject createNewBrowserSettings(String name) {
            
            DataObject createdSettings = null;
            
            try {
                
                FileObject extWebBrowserTemplate = Repository.getDefault().getDefaultFileSystem().findResource(BROWSER_TEMPLATE);
                FileObject browsersFolderFO = Repository.getDefault().getDefaultFileSystem().findResource(BROWSERS_FOLDER);
                
                if (extWebBrowserTemplate == null) {
                    return null;
                }

                DataObject templateDO = DataObject.find(extWebBrowserTemplate);
                DataFolder browsersFolderDF = DataFolder.findFolder(browsersFolderFO);
                createdSettings = templateDO.createFromTemplate(browsersFolderDF, name); 
                    
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            
            return createdSettings;
        }
        
    }
    
    private static class WebBrowsersPropertyPanel extends PropertyPanel {

        private WebBrowsersPropertyPanel(Object obj, String nm, int pref) {
            super(obj, nm, pref);
        }
        
        @Override
        public void removeNotify() {
            // disabled super.removeNotify() to be able to call updateValue to save changed value
        }
        
    }
    
}
