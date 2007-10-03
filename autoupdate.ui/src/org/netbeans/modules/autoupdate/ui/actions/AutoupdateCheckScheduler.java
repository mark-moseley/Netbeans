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

package org.netbeans.modules.autoupdate.ui.actions;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.modules.autoupdate.ui.InstallUnitWizard;
import org.netbeans.modules.autoupdate.ui.PluginManagerUI;
import org.netbeans.modules.autoupdate.ui.Unit;
import org.netbeans.modules.autoupdate.ui.UnitCategory;
import org.netbeans.modules.autoupdate.ui.Utilities;
import org.netbeans.modules.autoupdate.ui.wizards.InstallUnitWizardModel;
import org.netbeans.modules.autoupdate.ui.wizards.OperationWizardModel.OperationType;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateCheckScheduler {
    private static RequestProcessor.Task regularlyCheck = null;    
    private static final RequestProcessor REGULARLY_CHECK_TIMER = 
        new RequestProcessor("auto-checker-reqularly-timer", 1, true); // NOI18N
    private static final Logger err = Logger.getLogger ("org.netbeans.modules.autoupdate.services.AutoupdateCheckScheduler"); // NOI18N

    private AutoupdateCheckScheduler () {
    }
    
    public static void signOn () {
        AutoupdateSettings.getIdeIdentity ();
        
        if (timeToCheck ()) {
            // schedule refresh providers
            // install update checker when UI is ready (main window shown)
            WindowManager.getDefault().invokeWhenUIReady(new Runnable () {
                public void run () {
                    RequestProcessor.getDefault ().post (doCheck, 5000);
                }
            });
        } else {
            // install update checker when UI is ready (main window shown)
            WindowManager.getDefault().invokeWhenUIReady(new Runnable () {
                public void run () {
                    RequestProcessor.getDefault ().post (doCheckAvailableUpdates, 5000);
                }
            });
        }
    }
    
    private static void scheduleRefreshProviders () {
        assert ! SwingUtilities.isEventDispatchThread () : "Cannot run refreshProviders in EQ!";
        for (UpdateUnitProvider p : UpdateUnitProviderFactory.getDefault ().getUpdateUnitProviders (true)) {
            try {
                p.refresh (null, true);
                PluginManagerUI pluginManagerUI = PluginManagerAction.getPluginManagerUI ();
                if (pluginManagerUI != null) {
                    pluginManagerUI.updateUnitsChanged();
                }
            } catch (IOException ioe) {
                err.log (Level.INFO, ioe.getMessage (), ioe);
            }
        }
        RequestProcessor.getDefault ().post (doCheckAvailableUpdates, 500);
    }
    
    private static Runnable doCheckAvailableUpdates = new Runnable () {
        public void run () {
            if (SwingUtilities.isEventDispatchThread ()) {
                RequestProcessor.getDefault ().post (doCheckAvailableUpdates);
            }
            // check
            /*ProgressHandle handle = ProgressHandleFactory.createHandle (
                    NbBundle.getMessage (AutoupdateCheckScheduler.class, "AutoupdateCheckScheduler_CheckingForUpdates"));
            handle.setInitialDelay (0);
            handle.start ();
            try {*/
            List<UpdateUnit> units = UpdateManager.getDefault ().getUpdateUnits (Utilities.getUnitTypes ());
            List<UnitCategory> cats = Utilities.makeUpdateCategories (units, false);
            if (cats == null || cats.isEmpty ()) {
                return ;
            }
            OperationContainer oc = OperationContainer.createForUpdate ();
            Collection<UpdateElement> updates = new HashSet<UpdateElement> ();
            for (UnitCategory cat : cats) {
                for (Unit u : cat.getUnits ()) {
                    UpdateElement element = ((Unit.Update) u).getRelevantElement ();
                    UpdateUnit unit = element.getUpdateUnit ();
                    if (oc.canBeAdded (unit, element)) {
                        oc.add (element);
                        updates.add (element);
                    }
                }
            }
            /*} finally {
                if (handle != null) {
                    handle.finish ();
                }
            }*/
            
            // if any then notify updates
            notifyUpdates (updates);
        }
    };
    
    public static boolean timeToCheck () {
        if (getReqularlyTimerTask () != null) {
            // if time is off then is time to check
            if (getReqularlyTimerTask ().getDelay () <= 0 && getWaitPeriod () > 0) {
                // schedule next check
                getReqularlyTimerTask ().schedule (getWaitPeriod ());
                return true;
            }
        }
        
        // If this is the first time always check
        if (AutoupdateSettings.getLastCheck () == null) {
            return true;
        }
        
        switch (AutoupdateSettings.getPeriod ()) {
            case AutoupdateSettings.EVERY_STARTUP:
                return true;
            case AutoupdateSettings.EVERY_NEVER:
                return false;
            default:
                Date lastCheck = AutoupdateSettings.getLastCheck();
                GregorianCalendar calendar = new GregorianCalendar();
                calendar.setTime (lastCheck);

                calendar.set (Calendar.HOUR, 0);
                calendar.set (Calendar.AM_PM, 0);
                calendar.set (Calendar.MINUTE, 0);
                calendar.set (Calendar.SECOND, 0);
                calendar.set (Calendar.MILLISECOND, 0);

                switch (AutoupdateSettings.getPeriod ()) {
                    case AutoupdateSettings.EVERY_DAY:
                        calendar.add (GregorianCalendar.DATE, 1);
                        break;
                    case AutoupdateSettings.EVERY_WEEK:
                        calendar.add (GregorianCalendar.WEEK_OF_YEAR, 1);
                        break;
                    case AutoupdateSettings.EVERY_2WEEKS:
                        calendar.add (GregorianCalendar.WEEK_OF_YEAR, 2);
                        break;
                    case AutoupdateSettings.EVERY_MONTH:
                        calendar.add (GregorianCalendar.MONTH, 1);
                        break;
                }
                return calendar.getTime ().before (new Date ());
        }
    }
    
    private static RequestProcessor.Task getReqularlyTimerTask () {
        if (regularlyCheck == null) {
            // only for ordinary periods
            if (getWaitPeriod () > 0) {
                int waitPeriod = getWaitPeriod ();
                int restTime = waitPeriod;
                // calculate rest time to check
                if (AutoupdateSettings.getLastCheck () != null) {
                    restTime = waitPeriod - (int)(System.currentTimeMillis () - AutoupdateSettings.getLastCheck ().getTime ());
                }
                
                // if restTime < 0 then schedule next round by given period
                if (restTime <= 0) {
                    restTime = waitPeriod;
                }
                
                regularlyCheck = REGULARLY_CHECK_TIMER.post (doCheck, restTime, Thread.MIN_PRIORITY);
                
            }
        }
        return regularlyCheck;
    }
    
    private static Runnable doCheck = new Runnable () {
        public void run() {
            if (SwingUtilities.isEventDispatchThread ()) {
                RequestProcessor.getDefault ().post (doCheck);
            }
            scheduleRefreshProviders ();
        }
    };

    private static int getWaitPeriod () {
        switch (AutoupdateSettings.getPeriod ()) {
            case AutoupdateSettings.EVERY_NEVER:
                return 0;
            case AutoupdateSettings.EVERY_STARTUP:
                return 0;
            case AutoupdateSettings.EVERY_DAY:
                return 1000 * 3600 * 24;
            case AutoupdateSettings.EVERY_WEEK:
                return 1000 * 3600 * 24 * 7;
            case AutoupdateSettings.EVERY_2WEEKS:
                return 1000 * 3600 * 24 * 14;
            case AutoupdateSettings.EVERY_MONTH:
                return Integer.MAX_VALUE; // 1000 * 3600 * 24 * 28 is close but too big 
            default:
                return 0;
        }
    }
    
    private static AvailableUpdatesNotification.UpdatesFlasher flasher;
    
    private static void notifyUpdates (final Collection<UpdateElement> elems) {
        // Some modules found
        Runnable onMouseClick = new Runnable () {
            @SuppressWarnings("unchecked")
            public void run () {
                boolean wizardFinished = false;
                try {
                    OperationContainer oc = OperationContainer.createForUpdate ();
                    oc.add (elems);
                    wizardFinished = new InstallUnitWizard ().invokeWizard (new InstallUnitWizardModel (OperationType.UPDATE, oc));
                } finally {
                    if (wizardFinished) {
                        PluginManagerUI pluginManagerUI = PluginManagerAction.getPluginManagerUI ();
                        if (pluginManagerUI != null) {
                            pluginManagerUI.updateUnitsChanged();
                        }
                        if (flasher != null) {
                            flasher.disappear ();
                        }
                    } else {
                        // store available updates for future
                    }
                }
            }
        };
        flasher = AvailableUpdatesNotification.getFlasher (onMouseClick);
        assert flasher != null : "Updates Flasher cannot be null.";
        flasher.setToolTipText (NbBundle.getMessage (AutoupdateCheckScheduler.class, "AutoupdateCheckScheduler_UpdatesFound_ToolTip"));
        flasher.startFlashing ();
    }
    
}
