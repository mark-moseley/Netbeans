/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda;

import java.util.HashSet;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LaunchingDICookie;
import org.netbeans.spi.debugger.SessionProvider;


/**
 *
 * @author Jan Jancura
 */
public class LaunchingSessionProvider extends SessionProvider {
    
    private DebuggerInfo info;
    private LaunchingDICookie launchingCookie;
    
    public LaunchingSessionProvider (DebuggerInfo info) {
        this.info = info;
        launchingCookie = (LaunchingDICookie) info.lookupFirst 
            (LaunchingDICookie.class);
    };
    
    public String getSessionName () {
        String processName = (String) info.lookupFirst (String.class);
        if (processName != null)
            return processName;
        String sessionName = launchingCookie.getClassName ();
        int i = sessionName.lastIndexOf ('.');
        if (i >= 0) 
            sessionName = sessionName.substring (i + 1);
        return findUnique (sessionName);
    };
    
    public String getLocationName () {
        return "localhost";
    }
    
    public String getTypeID () {
        return JPDADebugger.SESSION_ID;
    }
    
    public Object[] getServices () {
        return new Object [0];
    }
    
    static String findUnique (String sessionName) {
        DebuggerManager cd = DebuggerManager.getDebuggerManager ();
        Session[] ds = cd.getSessions ();
        
        // 1) finds all already used indexes and puts them to HashSet
        int i, k = ds.length;
        HashSet m = new HashSet ();
        for (i = 0; i < k; i++) {
            String pn = ds [i].getName ();
            if (!pn.startsWith (sessionName)) continue;
            if (pn.equals (sessionName)) {
                m.add (new Integer (0));
                continue;
            }

            try {
                int t = Integer.parseInt (pn.substring (sessionName.length ()));
                m.add (new Integer (t));
            } catch (Exception e) {
            }
        }
        
        // 2) finds first unused index in m
        k = m.size ();
        for (i = 0; i < k; i++)
           if (!m.contains (new Integer (i)))
               break;
        if (i > 0) sessionName = sessionName + i;
        return sessionName;
    };
}

