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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.hudson.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.netbeans.modules.hudson.constants.HudsonInstanceConstants.*;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 * Instance properties for Hudson instance
 *
 * @author Michal Mocnak
 */
public class HudsonInstanceProperties extends HashMap<String,String> {
    
    private Sheet.Set set;
    
    protected final List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
    
    public HudsonInstanceProperties(String name, String url) {
        this(name, url, "0");
    }
    
    public HudsonInstanceProperties(String name, String url, String sync) {
        put(INSTANCE_NAME, name);
        put(INSTANCE_URL, url);
        put(INSTANCE_SYNC, sync);
    }
    
    public HudsonInstanceProperties(Map<String,String> properties) {
        super(properties);
    }

    @Override
    public synchronized String put(String key, String value) {
        String o = super.put(key, value);
        
        firePropertyChangeListeners( key, value);
        
        return o;
    }
    
    @Override
    public synchronized String remove(Object key) {
        String o = super.remove(key);
        
        firePropertyChangeListeners((String) key, null);
        
        return o;
    }
    
    public Sheet.Set getSheetSet() {
        if (null == set) {
            set = Sheet.createPropertiesSet();
            
            // Set display name
            set.setDisplayName(get(INSTANCE_NAME));
            
            // Put properties in
            set.put(new PropertySupport[] {
                new HudsonInstanceProperty(INSTANCE_NAME,
                        NbBundle.getMessage(HudsonInstanceProperties.class, "TXT_Instance_Prop_Name"),
                        NbBundle.getMessage(HudsonInstanceProperties.class, "DESC_Instance_Prop_Name"),
                        true, false),
                        new HudsonInstanceProperty(INSTANCE_URL,
                        NbBundle.getMessage(HudsonInstanceProperties.class, "TXT_Instance_Prop_Url"),
                        NbBundle.getMessage(HudsonInstanceProperties.class, "DESC_Instance_Prop_Url"),
                        true, false),
                        new HudsonInstanceProperty(INSTANCE_SYNC,
                        NbBundle.getMessage(HudsonInstanceProperties.class, "TXT_Instance_Prop_Sync"),
                        NbBundle.getMessage(HudsonInstanceProperties.class, "DESC_Instance_Prop_Sync"),
                        true, true)
            });
        }
        
        return set;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        listeners.add(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        listeners.remove(l);
    }
    
    private void firePropertyChangeListeners(String key, Object value) {
        PropertyChangeEvent event = new PropertyChangeEvent(this, key, value, value);
        ArrayList<PropertyChangeListener> tempList;
        
        synchronized (listeners) {
            tempList = new ArrayList<PropertyChangeListener>(listeners);
        }
        
        for (PropertyChangeListener l : tempList) {
            l.propertyChange(event);
        }
    }
    
    private class HudsonInstanceProperty extends PropertySupport<String> {
        
        private String key;
        
        public HudsonInstanceProperty(String key, String name, String desc, boolean read, boolean write) {
            super(key, String.class, name, desc, read, write);
            
            this.key = key;
        }
        
        @Override
        public void setValue(String value) {
            put(key, value);
        }
        
        @Override
        public String getValue() {
            return get(key);
        }
    }
}