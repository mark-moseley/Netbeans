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

package org.netbeans.modules.exceptions;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jindrich Sedek
 */
public class ExceptionsSettings {
    
    private static final String userProp = "UserName";       // NOI18N
    private static final String passwdProp = "Passwd";
    private static final String guestProp = "Guest";


    /** Creates a new instance of ExceptionsSettings */
    public ExceptionsSettings() {
    }

    private Preferences prefs() {
        return NbPreferences.forModule(ExceptionsSettings.class);
    }
    
    public String getUserName() {
        return prefs().get(userProp, "");
    }

    public void setUserName(String userName) {
        prefs().put(userProp, userName);
    }
        
    public String getPasswd() {
        return prefs().get(passwdProp, "");
    }

    public void setPasswd(String passwd) {
        prefs().put(passwdProp, passwd);
    }
    
    public boolean isGuest() {
        String isGuest = prefs().get(guestProp, "false");
        return Boolean.parseBoolean(isGuest);
    }

    public void setGuest(Boolean guest){
        prefs().put(guestProp, guest.toString());
    }
    
}