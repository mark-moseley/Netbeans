/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.ui.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.modules.autoupdate.ui.Utilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateSettings {

    private static String tempIdeIdentity = null;
    private static final Logger err = Logger.getLogger (AutoupdateSettings.class.getName ());
    private static final String PROP_IDE_IDENTITY = "ideIdentity"; // NOI18N
    private static final String PROP_PERIOD = "period"; // NOI18N
    private static final String PROP_LAST_CHECK = "lastCheckTime"; // NOI18N
    
    public static final int EVERY_STARTUP = 0;
    public static final int EVERY_DAY = 1;
    public static final int EVERY_WEEK = 2;
    public static final int EVERY_2WEEKS = 3;
    public static final int EVERY_MONTH = 4;
    public static final int NEVER = 5;
    public static final int CUSTOM_CHECK_INTERVAL = 6;
    
    private static final String [][] KNOWN = {
        {"EVERY_STARTUP", "0"},
        {"EVERY_DAY", "1"},
        {"EVERY_WEEK", "2"},
        {"EVERY_2WEEKS", "3"},
        {"EVERY_MONTH", "4"},
        {"NEVER", "5"},
    };
    
    private static int checkInterval = 0;

    private AutoupdateSettings () {
    }
    
    public static String getIdeIdentity () {
        if (tempIdeIdentity instanceof String) {
            return tempIdeIdentity;
        }
        Object oldIdeIdentity = getPreferences ().get (PROP_IDE_IDENTITY, null);
        String newIdeIdentity = null;
        if (oldIdeIdentity == null) {
            newIdeIdentity = modifyIdeIdentityIfNeeded (generateNewId ());
        } else {
            newIdeIdentity = modifyIdeIdentityIfNeeded ((String) oldIdeIdentity);
        }
        tempIdeIdentity = newIdeIdentity;
        if (! newIdeIdentity.equals (oldIdeIdentity)) {
            err.log (Level.FINE, "Put new value of PROP_IDE_IDENTITY to " + newIdeIdentity);
            getPreferences ().put (PROP_IDE_IDENTITY, newIdeIdentity);
        }
        return tempIdeIdentity;
    }
    
    public static int getPeriod () {
        boolean stillDefault = getPreferences ().get (PROP_PERIOD, null) == null;
        Integer defaultCheckInterval = null;
        if (stillDefault) {
            defaultCheckInterval = parse (Utilities.getCustomCheckIntervalInMinutes ());
        }
        if (defaultCheckInterval == null) {
            defaultCheckInterval = EVERY_WEEK;
        }
        err.log (Level.FINEST, "getPeriod () returns " + getPreferences ().getInt (PROP_PERIOD, defaultCheckInterval));
        return getPreferences ().getInt (PROP_PERIOD, defaultCheckInterval);
    }

    public static int getCheckInterval () {
        err.log (Level.FINEST, "getCheckInterval () returns " + checkInterval + "ms");
        return checkInterval;
    }
    
    public static void setPeriod (int period) {
        err.log (Level.FINEST, "Called setPeriod (" + period +")");
        getPreferences ().putInt (PROP_PERIOD, period);
    }
    
    public static Date getLastCheck() {        
        long t = getPreferences ().getLong (PROP_LAST_CHECK, -1);
        return (t > 0) ? new Date (t) : null;

    }

    public static void setLastCheck (Date lastCheck) {
        err.log (Level.FINER, "Set the last check to " + lastCheck);
        if (lastCheck != null) {
            getPreferences().putLong (PROP_LAST_CHECK, lastCheck.getTime ());
        } else {
            getPreferences().remove (PROP_LAST_CHECK);
        }
    }
    
    private static Preferences getPreferences () {
        return NbPreferences.root ().node ("/org/netbeans/modules/autoupdate");
    }    
    
    // helper methods
    private static String modifyIdeIdentityIfNeeded (String oldIdeIdentity) {
        int idx = oldIdeIdentity.indexOf ('0');
        String [] ideIdentityArr = oldIdeIdentity.split ("\\d"); // NOI18N
        String id = null;
        String oldPrefix = null;
        
        // easy way -> no need to modify
        if (ideIdentityArr.length == 0 || idx == 0) {
            id = oldIdeIdentity;
            oldPrefix = "";
        // a way for UUID    
        } else if (idx != -1 && oldIdeIdentity.substring (ideIdentityArr [0].length ()).startsWith ("0")) {
            oldPrefix = oldIdeIdentity.substring (0, idx);
            id = oldIdeIdentity.substring (oldPrefix.length ());
        // old way for stored IDs Random.nextInt()
        } else {
            oldPrefix = ideIdentityArr [0];
            id = oldIdeIdentity.substring (oldPrefix.length ());
        }
        err.log (Level.FINER, "Old IDE Identity Prefix: " + oldPrefix); // NOI18N
        err.log (Level.FINER, "Old IDE Identity ID: " + id); // NOI18N
        String newPrefix = "";
        try {
            FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource("/productid"); // NOI18N
            if (fo != null) {
                InputStream is = fo.getInputStream();
                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader (is));
                    newPrefix = r.readLine().trim();
                } finally {
                    is.close();
                }
            }
        } catch (IOException ignore) {
            err.log (Level.FINER, ignore.getMessage(), ignore);
        }
        if (!newPrefix.equals (oldPrefix)) {
            err.log (Level.FINER, "New IDE Identity Prefix: " + newPrefix); // NOI18N
        } else {
            err.log (Level.FINER, "No new prefix."); // NOI18N
        }
        return newPrefix + id;
    }

    private static String generateNewId () {
        return "0" + UUID.randomUUID ().toString ();
    }
    
    private static Integer parse (String s) {
        if (s == null || s.trim ().length () == 0) {
            return null;
        }
        Integer period = null;
        for (String [] pair: KNOWN) {
            if (pair [0].equalsIgnoreCase (s)) {
                try {
                    period = Integer.parseInt (pair[1]);
                } catch (NumberFormatException nfe) {
                    assert false : "Invalid value " + pair + " throws " + nfe;
                }
            }
        }
        if (period == null) {
            try {
                checkInterval = Long.parseLong (s) * 1000 * 60 > Integer.MAX_VALUE ? Integer.MAX_VALUE : Integer.parseInt (s) * 1000 * 60;
                period = CUSTOM_CHECK_INTERVAL;
                err.log (Level.FINE, "Custom value of " + Utilities.PLUGIN_MANAGER_CHECK_INTERVAL + " is " + s + " minutes.");
            } catch (NumberFormatException nfe) {
                err.log (Level.FINE, "Invalid value " + s + " of " + Utilities.PLUGIN_MANAGER_CHECK_INTERVAL + " throws " + nfe);
            }
        } else {
            err.log (Level.FINE, "Custom value of " + Utilities.PLUGIN_MANAGER_CHECK_INTERVAL + " is " + s);
        }
        return period;
    }
    
}
