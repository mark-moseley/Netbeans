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
package org.netbeans.modules.j2ee.weblogic9.ui.wizard;

import java.util.*;
import java.io.*;
import javax.swing.event.*;

import org.openide.*;
import org.openide.util.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;

import org.netbeans.modules.j2ee.weblogic9.*;

/**
 * The main class of the custom wizard for registering a new server instance.
 * It performs all the orchestration of the panels and actually creates the
 * instance.
 *
 * @author Kirill Sorokin
 */
public class WLInstantiatingIterator  implements WizardDescriptor.InstantiatingIterator {
    
    /**
     * Since the WizardDescriptor does not expose the property name for the 
     * error message label, we have to keep it here also
     */
    private final static String PROP_DISPLAY_NAME = "ServInstWizard_displayName"; // NOI18N
    
    /**
     * The default debugger port for the instance, it will be assigned to it
     * at creation time and can be changed via the properties sheet
     */
    private static final String DEFAULT_DEBUGGER_PORT = "8787"; // NOI18N
    
    /**
     * The parent wizard descriptor
     */
    private WizardDescriptor wizardDescriptor;
    
    /**
     * A misterious method whose purpose is obviously in freeing the resources 
     * obtained by the wizard during instance registration. We do not need such 
     * functionality, thus we do not implement it.
     */
    public void uninitialize(WizardDescriptor wizardDescriptor) {
        // do nothing as we do not need to release any resources
    }

    /**
     * This method initializes the wizard. AS for us the only thing we should 
     * do is save the wizard descriptor handle.
     * 
     * @param wizardDescriptor the parent wizard descriptor
     */
    public void initialize(WizardDescriptor wizardDescriptor) {
        this.wizardDescriptor = wizardDescriptor;
    }
    
    /**
     * Returns the name for the wizard. I failed to find a place where it
     * could be used, so we do not return anything sensible
     * 
     * @return the wizard name
     */
    public String name() {
        return ""; // NOI18N
    }

    /**
     * This methos actually creates the instance. It fetches all the required 
     * parameters, builds the URL and calls 
     * InstanceProperties.createInstamceProperties(), which registers the 
     * instance.
     * 
     * @return a set of created instance properties
     */
    public Set instantiate() throws IOException {
        // initialize the resulting set
        Set result = new HashSet();
        
        // build the URL
        String url = WLDeploymentFactory.URI_PREFIX + this.host + ":" + this.port + ":" + serverRoot; // NOI18N
        
        // set the username and password
        String username = this.username;
        String password = this.password;
        
        String displayName = (String)wizardDescriptor.getProperty(PROP_DISPLAY_NAME);
        
        // set the additional properties of the instance: server installation 
        // directory, profile root directory, whether the server is local or
        // remote and the instance name
        String serverRoot = this.serverRoot;
        String domainRoot = this.domainRoot;
        String isLocal = this.isLocal;
        
        // if all the data is normally validated - create the instance and 
        // attach the additional properties
        InstanceProperties ip = InstanceProperties.createInstanceProperties(url, username, password, displayName);
        ip.setProperty(WLPluginProperties.SERVER_ROOT_ATTR, serverRoot);
        ip.setProperty(WLPluginProperties.DOMAIN_ROOT_ATTR, domainRoot);
        ip.setProperty(WLPluginProperties.IS_LOCAL_ATTR, isLocal);
        ip.setProperty(WLPluginProperties.DEBUGGER_PORT_ATTR, DEFAULT_DEBUGGER_PORT);

        // add the created instance properties to the result set
        result.add(ip);
        
        // return the result
        return result;
    }
    
    // the main and additional instance properties
    private String serverRoot;
    private String domainRoot;
    private String isLocal;
    private String host;
    private String port;
    private String username;
    private String password;
    
    /**
     * Setter for the server installation directory
     *
     * @param serverRoot the new server installation directory path
     */
    public void setServerRoot(String serverRoot) {
        this.serverRoot = serverRoot;
        
        // reinit the instances list
        serverPropertiesPanel.updateInstancesList();
    }
    
    /**
     * Getter for the server installation directory
     *
     * @return the server installation directory path
     */
    public String getServerRoot() {
        return this.serverRoot;
    }
    
    /**
     * Setter for the profile root directory
     *
     * @param domainRoot the new profile root directory path
     */
    public void setDomainRoot(String domainRoot) {
        this.domainRoot = domainRoot;
    }

    /**
     * Getter for the profile root directory
     *
     * @return the profile root directory path
     */
    public String getDomainRoot() {
        return domainRoot;
    }

    /**
     * Getter for the server host
     *
     * @return the server host
     */
    public String getHost() {
        return host;
    }

    /**
     * Setter for the server host
     *
     * @param host the new server host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Getter for the server port
     *
     * @return the server port
     */
    public String getPort() {
        return port;
    }

    /**
     * Setter for the server port
     *
     * @param port the new server port
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * Getter for the username
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Setter for the username
     *
     * @param username the new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Getter for the password
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter for the password
     *
     * @param password the new password
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Getter for the isLocal property
     *
     * @return "true" if the server is local, "false" otherwise
     */
    public String getIsLocal() {
        return this.isLocal;
    }
    
    /**
     * Setter for the isLocal property
     *
     * @param isLocal "true" if the server is local, "false" otherwise
     */
    public void setIsLocal(String isLocal) {
        this.isLocal = isLocal;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Panels section
    ////////////////////////////////////////////////////////////////////////////
    /**
     * The steps names for the wizard: Server Location & Instance properties
     */
    private Vector steps = new Vector();
    {
        steps.add(NbBundle.getMessage(ServerPropertiesPanel.class, "SERVER_LOCATION_STEP")); // NOI18N
        steps.add(NbBundle.getMessage(ServerPropertiesPanel.class, "SERVER_PROPERTIES_STEP")); // NOI18N
    }
    
    /**
     * The wizard's panels
     */
    private WizardDescriptor.Panel[] panels;
    private ServerLocationPanel serverLocationPanel;
    private ServerPropertiesPanel serverPropertiesPanel;
    
    /**
     * Index of the currently shown panel
     */
    private int index = 0;
    
    /**
     * Tells whether the wizard has previous panels. Basically controls the 
     * Back button
     */
    public boolean hasPrevious() {
        return index > 0;
    }

    /**
     * Reverts the wizard to the previous panel if available.
     * If the previous panel is not available a NoSuchElementException will be 
     * thrown.
     */
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    /**
     * Tells whether the wizard has next panels. Basically controls the 
     * Next button
     */
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    /**
     * Proceeds the wizard to the next panel if available.
     * If the next panel is not available a NoSuchElementException will be 
     * thrown.
     */
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }
    
    /**
     * Returns the current panel of the wizard
     * 
     * @return current panel of the wizard
     */
    public WizardDescriptor.Panel current() {
        getPanels();
        return panels[index];
    }
    
    protected final WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = createPanels();
        }
        return panels;
    }
    
    protected WizardDescriptor.Panel[] createPanels() {

        serverLocationPanel = new ServerLocationPanel((String[]) steps.toArray(new String[steps.size()]), 0, new IteratorListener(), this);
        serverPropertiesPanel = new ServerPropertiesPanel((String[]) steps.toArray(new String[steps.size()]), 1, new IteratorListener(), this);

        return new WizardDescriptor.Panel[] { serverLocationPanel, serverPropertiesPanel };
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Listeners section
    ////////////////////////////////////////////////////////////////////////////
    /**
     * The registered listeners
     */
    private Vector listeners = new Vector();
    
    /**
     * Removes an already registered listener in a synchronized manner
     * 
     * @param listener a listener to be removed
     */
    public void removeChangeListener(ChangeListener listener) {
        if (listeners != null) {
            synchronized (listeners) {
                listeners.remove(listener);
            }
        }
    }

    /**
     * Registers a new listener in a synchronized manner
     * 
     * @param listener a listener to be registered
     */
    public void addChangeListener(ChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    /**
     * Notifies all the listeners of the supplied event
     * 
     * @param event the event to be passed to the listeners
     */
    private void fireChangeEvent(ChangeEvent event) {
        // copy the registered listeners, to avoid conflicts if the listeners'
        // list changes
        Vector targetListeners;
        synchronized (listeners) {
            targetListeners = (Vector) listeners.clone();
        }
        
        // notify each listener of the event
        for (int i = 0; i < targetListeners.size(); i++) {
            ChangeListener listener = (ChangeListener) targetListeners.elementAt(i);
            listener.stateChanged(event);
        }
    }
    
    /**
     * A simple listener that only notifies the parent iterator of all the 
     * events that come to it
     * 
     * @author Kirill Sorokin
     */
    private class IteratorListener implements ChangeListener {
        /**
         * Notifies the parent iterator of the supplied event
         * 
         * @param event the event to be passed to the parent iterator
         */
        public void stateChanged(ChangeEvent event) {
            fireChangeEvent(event);
        }
    }
    
}
