/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.windows;


import org.netbeans.core.NbTopManager;


/**
 * Implementation of WindowSystem interface (declared in core NbTopManager).
 *
 * @author  Peter Zavadsky
 */
public class WindowSystemImpl implements NbTopManager.WindowSystem {
    
    /** Creates a new instance of WindowSystemImpl */
    public WindowSystemImpl() {
    }
    
    
    // Persistence
    /** Implements <code>NbTopManager.WindowSystem</code> interface method.
     * Loads window system persistent data. */
    public void load() {
        WindowManagerImpl.assertEventDispatchThread();
        
        PersistenceHandler.getDefault().load();
    }
    /** Implements <code>NbTopManager.WindowSystem</code> interface method. 
     * Saves window system persistent data. */
    public void save() {
        WindowManagerImpl.assertEventDispatchThread();
        
        PersistenceHandler.getDefault().save();
    }
    
    // GUI
    /** Implements <code>NbTopManager.WindowSystem</code> interface method. 
     * Shows window system. */
    public void show() {
        WindowManagerImpl.assertEventDispatchThread();
        
        ShortcutAndMenuKeyEventProcessor.install();
        WindowManagerImpl.getInstance().setVisible(true);
    }
    /** Implements <code>NbTopManager.WindowSystem</code> interface method. 
     * Hides window system. */
    public void hide() {
        WindowManagerImpl.assertEventDispatchThread();
        
        WindowManagerImpl.getInstance().setVisible(false);
        ShortcutAndMenuKeyEventProcessor.uninstall();
    }
    
    // Projects>>
    public void loadProjectData() {
        WindowManagerImpl.assertEventDispatchThread();
        PersistenceHandler.getDefault().loadProjectData();
    }
    
    public void saveProjectData() {
        WindowManagerImpl.assertEventDispatchThread();
        PersistenceHandler.getDefault().saveProjectData();
    }
    
    public void setProjectName(String projectName) {
        WindowManagerImpl.assertEventDispatchThread();
        WindowManagerImpl.getInstance().setProjectName(projectName);
    }
    // Projects<<
    
}

