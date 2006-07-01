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

package org.netbeans.core;

import java.awt.Dialog;
import java.awt.Toolkit;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.TopSecurityManager;
import org.netbeans.core.startup.MainLookup;
import org.netbeans.core.startup.ModuleSystem;
import org.netbeans.core.startup.layers.SessionManager;
import org.netbeans.core.ui.SwingBrowser;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Main switchboard for the NetBeans core.
 * Manages startup sequence, holds references to important objects, etc.
 */
public abstract class NbTopManager {
    /* masks to define the interactivity level */

    /** initialize the main window?
    * if not set the main window is not create nor shown.
    */
    public static final int IL_MAIN_WINDOW = 0x0001;
    /** initialize window system?
    * if not set the selected node is taken from the top manager.
    */
    public static final int IL_WINDOWS = 0x0002;
    /** initialize workspaces when not created?
    */
    public static final int IL_WORKSPACES = 0x0004;


    /** Initialize everything.
    */
    public static final int IL_ALL = 0xffff;
    
    /** Constructs a new manager.
    */
    public NbTopManager() {
        assert defaultTopManager == null : "Only one instance allowed"; // NOI18N
        defaultTopManager = this;
        
        Lookup lookup = Lookup.getDefault();
        if (!(lookup instanceof MainLookup)) {
            throw new ClassCastException("Wrong Lookup impl found: " + lookup);
        }
        ((MainLookup)lookup).startedNbTopManager();
    }

    /** Getter for instance of this manager.
    */
    public static NbTopManager get () {
        assert defaultTopManager != null : "Must be initialized already"; // NOI18N
        return defaultTopManager;
    }
    
    /** Danger method for clients who think they want an NbTM but don't actually
     * care whether it is ready or not. Should be removed eventually by getting
     * rid of useless protected methods in this class, and using Lookup to find
     * each configurable piece of impl.
     * @return a maybe half-constructed NbTM
     */
    public static NbTopManager getUninitialized() {
        return get();
    }
        
    private static NbTopManager defaultTopManager; 

    /**
     * Checks whether the top manager has been loaded already.
     * Used during early startup sequence.
     */
    public static synchronized boolean isInitialized () {
        return defaultTopManager != null;
    }
    
    /** Test method to check whether some level of interactivity is enabled.
     * XXX this method is unused; can it be deleted?
    * @param il mask composed of the constants of IL_XXXX
    * @return true if such level is enabled
    */
    public abstract boolean isInteractive (int il);
    
    //
    // The main method allows access to registration service
    //
    
    /** Register new instance.
     */
    public final void register (Object obj) {
        MainLookup.register (obj);
    }
    
    /** Register new instance.
     * @param obj source
     * @param conv convertor which postponing an instantiation
     */
    public final <T,R> void register(T obj, InstanceContent.Convertor<T,R> conv) {
        MainLookup.register (obj, conv);
    }
    
    /** Unregisters the service.
     */
    public final void unregister (Object obj) {
        MainLookup.unregister (obj);
    }
    /** Unregisters the service registered with a convertor.
     */
    public final <T,R> void unregister (T obj, InstanceContent.Convertor<T,R> conv) {
        MainLookup.unregister (obj, conv);
    }
    
    
    //
    // Implementation of methods from TopManager
    //

    /** Shows a specified HelpCtx in IDE's help window.
    * @param helpCtx thehelp to be shown
     * @deprecated Better to use org.netbeans.api.javahelp.Help
    */
    public void showHelp(HelpCtx helpCtx) {
        // Awkward but should work.
        try {
            Class<?> c = (Lookup.getDefault().lookup(ClassLoader.class)).loadClass("org.netbeans.api.javahelp.Help"); // NOI18N
            Object o = Lookup.getDefault().lookup(c);
            if (o != null) {
                Method m = c.getMethod("showHelp", new Class[] {HelpCtx.class}); // NOI18N
                m.invoke(o, new Object[] {helpCtx});
                return;
            }
        } catch (ClassNotFoundException cnfe) {
            // ignore - maybe javahelp module is not installed, not so strange
        } catch (Exception e) {
            // potentially more serious
            Logger.global.log(Level.WARNING, null, e);
        }
        // Did not work.
        Toolkit.getDefaultToolkit().beep();
    }

    /**
     * Implementation of URL displayer, which shows documents in the configured web browser.
     */
    public static final class NbURLDisplayer extends HtmlBrowser.URLDisplayer {
        /** Default constructor for lookup. */
        public NbURLDisplayer() {}
        /** WWW browser window. */
        private NbBrowser htmlViewer;
        public void showURL(final URL u) {
            Mutex.EVENT.readAccess(new Runnable() {
                public void run() {
                    if (htmlViewer == null) {
                        htmlViewer = new NbBrowser();
                    }
                    htmlViewer.showUrl(u);
                }
            });
        }
    }


    /**
     * Default status displayer implementation; GUI is in StatusLine.
     */
    public static final class NbStatusDisplayer extends org.openide.awt.StatusDisplayer {
        /** Default constructor for lookup. */
        public NbStatusDisplayer() {}
        private List<ChangeListener> listeners = null;
        private String text = ""; // NOI18N
        public void setStatusText(String text) {
            ChangeListener[] _listeners;
            synchronized (this) {
                if (text.equals(this.text)) return;
                this.text = text;
                if (listeners == null || listeners.isEmpty()) {
                    return;
                } else {
                    _listeners = listeners.toArray(new ChangeListener[listeners.size()]);
                }
            }
            ChangeEvent e = new ChangeEvent(this);
            for (int i = 0; i < _listeners.length; i++) {
                _listeners[i].stateChanged(e);
            }
        }
        public synchronized String getStatusText() {
            return text;
        }
        public synchronized void addChangeListener(ChangeListener l) {
            if (listeners == null) listeners = new ArrayList<ChangeListener>();
            listeners.add(l);
        }
        public synchronized void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
    }

    /** saves all opened objects */
    private static void saveAll () {
        DataObject dobj = null;
        ArrayList<DataObject> bad = new ArrayList<DataObject> ();
        DataObject[] modifs = DataObject.getRegistry ().getModified ();
        if (modifs.length == 0) {
            // Do not show MSG_AllSaved
            return;
        }
        for (int i = 0; i < modifs.length; i++) {
            try {
                dobj = modifs[i];
                SaveCookie sc = (SaveCookie) dobj.getCookie(SaveCookie.class);

                if (sc != null) {
                    org.openide.awt.StatusDisplayer.getDefault().setStatusText(
                        MessageFormat.format(
                            NbBundle.getBundle(NbTopManager.class).getString(
                                "CTL_FMT_SavingMessage"
                            ), new Object[]{dobj.getName()}
                    ));
                    sc.save();
                }
            }
            catch (IOException ex) {
                Logger.global.log(Level.WARNING, null, ex);
                bad.add(dobj);
            }
        }
        NotifyDescriptor descriptor;
        //recode this part to show only one dialog?
        Iterator<DataObject> ee = bad.iterator ();
        while (ee.hasNext ()) {
            descriptor = new NotifyDescriptor.Message(
                        MessageFormat.format (
                            NbBundle.getBundle (NbTopManager.class).getString("CTL_Cannot_save"),
                            new Object[] { ee.next().getPrimaryFile().getName() }
                        )
                    );
            org.openide.DialogDisplayer.getDefault().notify (descriptor);
        }
        // notify user that everything is done
        org.openide.awt.StatusDisplayer.getDefault().setStatusText(
            NbBundle.getBundle (NbTopManager.class).getString ("MSG_AllSaved"));
    }
    
    /** Interface describing basic control over window system. 
     * @since 1.15 */
    public interface WindowSystem {
        void show();
        void hide();
        void load();
        void save();
        void clear();
        /**
         * This is used by projects/projectui to track the project selection.
         * @since 1.20
         */
        void setProjectName(String projectName);
    } // End of WindowSystem interface.
    
    public static boolean isModalDialogPresent() {
        return hasModalDialog(WindowManager.getDefault().getMainWindow())
            // XXX Trick to get the shared frame instance.
            || hasModalDialog(new JDialog().getOwner());
    }
    
    private static boolean hasModalDialog(Window w) {
        Window[] ws = w.getOwnedWindows();
        for(int i = 0; i < ws.length; i++) {
            if(ws[i] instanceof Dialog && ((Dialog)ws[i]).isModal()) {
                return true;
            } else if(hasModalDialog(ws[i])) {
                return true;
            }
        }
        
        return false;
    }


    
    private static boolean doingExit=false;
    public static void exit ( ) {
        // #37160 So there is avoided potential clash between hiding GUI in AWT
        // and accessing AWTTreeLock from saving routines (winsys).
        if(SwingUtilities.isEventDispatchThread()) {
            doExit();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    doExit();
                }
            });
        }
    }
    
    /**
     * @return True if the IDE is shutting down.
     */
    public static boolean isExiting() {
        return doingExit;
    }
    
    private static void doExit() {
        if (doingExit) {
            return ;
        }
        doingExit = true;
        // save all open files
        try {
            if ( System.getProperty ("netbeans.close") != null || ExitDialog.showDialog() ) {
     
                final WindowSystem windowSystem = Lookup.getDefault().lookup(WindowSystem.class);
                
                // #29831: hide frames between closing() and close()
                Runnable hideFrames = new Runnable() {
                    public void run() {
                        org.netbeans.CLIHandler.stopServer ();
                
                        if(windowSystem != null) {
                            windowSystem.hide();
                            windowSystem.save();
                        }
                        if (Boolean.getBoolean("netbeans.close.when.invisible")) {
                            // hook to permit perf testing of time to *apparently* shut down
                            TopSecurityManager.exit(0);
                        }
                    }
                };
                
                if (org.netbeans.core.startup.Main.getModuleSystem().shutDown(hideFrames)) {
                    try {
                        try {
                            LoaderPoolNode.store();
                        } catch (IOException ioe) {
                            Logger.global.log(Level.WARNING, null, ioe);
                        }
//#46940 -saving just once..                        
//                        // save window system, [PENDING] remove this after the winsys will
//                        // persist its state automaticaly
//                        if (windowSystem != null) {
//                            windowSystem.save();
//                        }
                        try {
                            ((MainLookup)Lookup.getDefault()).storeCache();
                        } catch (IOException ioe) {
                            Logger.global.log(Level.WARNING, null, ioe);
                        }
                        SessionManager.getDefault().close();
                    } catch (ThreadDeath td) {
                        throw td;
                    } catch (Throwable t) {
                        // Do not let problems here prevent system shutdown. The module
                        // system is down; the IDE cannot be used further.
                        Exceptions.printStackTrace(t);
                    }
                    // #37231 Someone (e.g. Jemmy) can install its own EventQueue and then
                    // exit is dispatched through that proprietary queue and it
                    // can be refused by security check. So, we need to replan
                    // to RequestProcessor to avoid security problems.
                    Task exitTask = new Task(new Runnable() {
                        public void run() {
                            TopSecurityManager.exit(0);
                        }
                    });
                    RequestProcessor.getDefault().post(exitTask);
                    exitTask.waitFinished();
                }
            }
        } finally {
            doingExit = false; 
        }
    }

    /**
     * Default implementation of the lifecycle manager interface that knows
     * how to save all modified DataObject's, and to exit the IDE safely.
     */
    public static final class NbLifecycleManager extends LifecycleManager {
        /** Default constructor for lookup. */
        public NbLifecycleManager() {}
        public void saveAll() {
            NbTopManager.saveAll();
        }
        public void exit() {
            NbTopManager.exit();
        }
    }

    /** Get the module subsystem. */
    public abstract ModuleSystem getModuleSystem();
    
    public static Lookup getModuleLookup() {
        return org.netbeans.core.startup.Main.getModuleSystem().getManager().getModuleLookup();
    }

    public static List getModuleJars() {
        return org.netbeans.core.startup.Main.getModuleSystem().getModuleJars();
    }

    /**
     * Able to reuse HtmlBrowserComponent.
     */
    public static class NbBrowser {
        
        private HtmlBrowserComponent brComp;
        private PropertyChangeListener idePCL;
        private static Lookup.Result factoryResult;
        
        static {            
            factoryResult = Lookup.getDefault().lookupResult(HtmlBrowser.Factory.class);
            factoryResult.allItems();
            factoryResult.addLookupListener(new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    ((NbURLDisplayer)org.openide.awt.HtmlBrowser.URLDisplayer.getDefault()).htmlViewer = null;
                }
            });                                    
        }
        
        public NbBrowser() {
            IDESettings settings = IDESettings.findObject(IDESettings.class, true);
            HtmlBrowser.Factory browser = settings.getWWWBrowser();
            if (browser == null) {
                // Fallback.
                browser = new SwingBrowser();
            }
            // try if an internal browser is set and possibly try to reuse an 
            // existing component
            if (browser.createHtmlBrowserImpl().getComponent() != null) {
                brComp = findOpenedBrowserComponent();
            }
            if (brComp == null) {
                brComp = new HtmlBrowserComponent(browser, true, true);
                brComp.putClientProperty("TabPolicy", "HideWhenAlone"); // NOI18N
            }
            setListener();
        }

        /** 
         * Tries to find already opened <code>HtmlBrowserComponent</code>. In
         * the case of success returns the instance, null otherwise.         
         */
        private HtmlBrowserComponent findOpenedBrowserComponent() {
            for (Iterator it = WindowManager.getDefault().getModes().iterator(); it.hasNext(); ) {
                Mode m = (Mode) it.next();
                if ("editor".equals(m.getName())) { // NOI18N
                    TopComponent[] tcs = m.getTopComponents();
                    for (int i = 0; i < tcs.length; i++) {
                        if (tcs[i] instanceof HtmlBrowserComponent) {
                            return (HtmlBrowserComponent) tcs[i];
                        }
                    }
                    break;
                }
            }
            return null;
        }

        /** Show URL in browser
         * @param url URL to be shown
         */
        private void showUrl(URL url) {
            brComp.open();
            brComp.requestActive();
            brComp.setURL(url);
        }

        /**
         *  Sets listener that invalidates this as main IDE's browser if user changes the settings
         */
        private void setListener () {
            if (idePCL != null) {
                return;
            }
            try {                
                // listen on preffered browser change
                idePCL = new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        String name = evt.getPropertyName();

                        if (name == null)
                            return;
                        if (name.equals(IDESettings.PROP_WWWBROWSER)) {
                            ((NbURLDisplayer) HtmlBrowser.URLDisplayer.getDefault()).htmlViewer = null;
                            if (idePCL != null) {
                                (IDESettings.findObject(IDESettings.class, true)).removePropertyChangeListener(idePCL);
                                idePCL = null;
                                brComp = null;
                            }
                        }
                    }
                };
                (IDESettings.findObject(IDESettings.class, true)).addPropertyChangeListener(idePCL);
            }
            catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
