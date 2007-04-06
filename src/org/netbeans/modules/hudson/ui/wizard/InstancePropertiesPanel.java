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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.hudson.ui.wizard;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.hudson.impl.HudsonManagerImpl;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Michal Mocnak
 */
public class InstancePropertiesPanel implements WizardDescriptor.Panel, InstanceWizardConstants, ChangeListener {
    
    private final static String HTTP_PREFIX = "http://";
    private final static String HTTPS_PREFIX = "https://";
    
    private InstancePropertiesVisual component;
    
    private WizardDescriptor wizard;
    
    private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    public Component getComponent() {
        if (component == null) {
            component = new InstancePropertiesVisual();
            component.addChangeListener(this);
        }
        
        return component;
    }
    
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public void readSettings(Object o) {
        if (o instanceof WizardDescriptor)
            wizard = (WizardDescriptor) o;
    }
    
    public void storeSettings(Object o) {}
    
    public boolean isValid() {
        String name = getInstancePropertiesVisual().getDisplayName();
        String url = getInstancePropertiesVisual().getUrl();
        String sync = getInstancePropertiesVisual().isSync() ? getInstancePropertiesVisual().getSyncTime() : "0";
        
        if (name.length() == 0) {
            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(InstancePropertiesPanel.class,
                    "MSG_EmptyName"));
            return false;
        }
        
        if (HudsonManagerImpl.getDefault().getInstanceByName(name) != null) {
            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(InstancePropertiesPanel.class,
                    "MSG_ExistName"));
            return false;
        }
        
        if (url.length() == 0) {
            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(InstancePropertiesPanel.class,
                    "MSG_EmptyUrl"));
            return false;
        }
        
        if (!(url.startsWith(HTTP_PREFIX) || url.startsWith(HTTPS_PREFIX)))
            url = HTTP_PREFIX + url;
        
        
        if (HudsonManagerImpl.getDefault().getInstance(url) != null) {
            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(InstancePropertiesPanel.class,
                    "MSG_ExistUrl"));
            return false;
        }
        
        wizard.putProperty(PROP_ERROR_MESSAGE, null);
        wizard.putProperty(PROP_DISPLAY_NAME, name);
        wizard.putProperty(PROP_URL, url);
        wizard.putProperty(PROP_SYNC, sync);
        
        return true;
    }
    
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    private void fireChangeEvent() {
        ArrayList<ChangeListener> tempList;
        
        synchronized (listeners) {
            tempList = new ArrayList<ChangeListener>(listeners);
        }
        
        ChangeEvent event = new ChangeEvent(this);
        
        for (ChangeListener l : tempList) {
            l.stateChanged(event);
        }
    }
    
    public void stateChanged(ChangeEvent arg0) {
        fireChangeEvent();
    }
    
    private InstancePropertiesVisual getInstancePropertiesVisual() {
        return (InstancePropertiesVisual) getComponent();
    }
}