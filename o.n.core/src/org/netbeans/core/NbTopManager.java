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

package org.netbeans.core;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.*;
import org.openide.awt.HtmlBrowser.BrowserComponent;
import org.openide.loaders.*;
import org.openide.actions.*;
import org.openide.cookies.SaveCookie;
import org.openide.modules.Dependency;
import org.openide.modules.SpecificationVersion;
import org.openide.explorer.*;
import org.openide.util.*;
import org.openide.util.io.*;
import org.openide.nodes.*;
import org.openide.util.lookup.*;
import org.openide.windows.WindowManager;

import org.netbeans.core.actions.*;
import org.netbeans.TopSecurityManager;
import org.netbeans.core.modules.Module;
import org.netbeans.core.perftool.StartLog;
import org.netbeans.core.modules.ModuleSystem;
import org.netbeans.core.projects.TrivialProjectManager;
import org.openide.awt.HtmlBrowser;
import org.openide.modules.ModuleInfo;

/** This class is a TopManager for Corona environment.
*
* @author Ales Novak, Jaroslav Tulach, Ian Formanek, Petr Hamernik, Jan Jancura, Jesse Glick
*/
public abstract class NbTopManager /*extends TopManager*/ {
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
    
    /** inner access to dynamic lookup service for this top mangager */
    private InstanceContent instanceContent;
    /** dynamic lookup service for this top mangager */
    private Lookup instanceLookup;

    /** initializes properties about builds etc. */
    static {
        // Set up module-versioning properties, which logger prints.
        // The package here must be one which exists only in a central openide-*.jar, not e.g. openide-deprecated.jar:
        Object ignoreme = SpecificationVersion.class; // DO NOT DELETE UNUSED VAR
        Package p = Package.getPackage("org.openide.modules"); // NOI18N
        
        // Normally the defaults here should not be used. openide.jar, not just openide/src/,
        // must be in your classpath (this applies to tests too, even openide/test/build.xml).
        // When that is done correctly, the package will be defined and these properties will
        // be defined too. Otherwise there is no manifest and they will all be null.
        putSystemProperty ("org.openide.specification.version", p.getSpecificationVersion (), "99.9"); // NOI18N
        putSystemProperty ("org.openide.version", p.getImplementationVersion (), "OwnBuild"); // NOI18N
        putSystemProperty ("org.openide.major.version", p.getSpecificationTitle (), "IDE/1"); // NOI18N
        putSystemProperty ("netbeans.buildnumber", p.getImplementationVersion (), "OwnBuild"); // NOI18N
        
        // Enforce JDK 1.4+ since we would not work without it.
        if (Dependency.JAVA_SPEC.compareTo(new SpecificationVersion("1.4")) < 0) { // NOI18N
            System.err.println("The IDE requires JDK 1.4 or higher to run."); // XXX I18N?
            TopSecurityManager.exit(1);
        }

        // In the past we derived ${jdk.home} from ${java.home} by appending
        // "/.." to the end of ${java.home} assuming that JRE is under JDK
        // directory.  It does not always work.  On MacOS X JDK and JRE files
        // are mixed together, thus ${jdk.home} == ${java.home}.  In several
        // Linux distros JRE and JDK are installed at the same directory level
        // with ${jdk.home}/jre a symlink to ${java.home}, which means
        // ${java.home}/.. != ${jdk.home}.
        //
        // Now the launcher can set ${jdk.home} explicitly because it knows
        // best where the JDK is.
        
        String jdkHome = System.getProperty("jdk.home");  // NOI18N
        
        if (jdkHome == null) {
            jdkHome = System.getProperty("java.home");  // NOI18N
            
            if (Utilities.getOperatingSystem() != Utilities.OS_MAC) {
                jdkHome += File.separator + "..";  // NOI18N
            }
                
            System.setProperty("jdk.home", jdkHome);  // NOI18N
        }

        // read environment properties from external file, if any
        try {
            readEnvMap ();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // initialize the URL factory
        URLStreamHandlerFactory fact = new NbURLStreamHandlerFactory();
        try {
            URL.setURLStreamHandlerFactory(fact);
        } catch (Error e) {
            // Can happen if we try to start NB twice in the same VM.
            // Print the error but try to continue.
            System.err.println("While calling URL.setURLStreamHandlerFactory, got: " + e);
        }
    }
    
    /** Puts a property into the system ones, but only if the value is not null.
     * @param propName name of property
     * @param value value to assign or null
     * @param failbackValue value to assign if the previous value is null
     */
    private static void putSystemProperty (
        String propName, String value, String failbackValue
    ) {
        if (System.getProperty (propName) == null) {
            // only set it if not null
            if (value != null) {
                System.setProperty (propName, value);
            } else {
                if (!Boolean.getBoolean("netbeans.suppress.sysprop.warning")) {
                    System.err.println(
                        "Warning: Versioning property \"" + propName + // NOI18N
                        "\" is not set. Defaulting to \"" + failbackValue + '"' // NOI18N
                    ); 
                    System.err.println("(to suppress this message run with -Dnetbeans.suppress.sysprop.warning=true)"); // NOI18N
                }
                System.setProperty (propName, failbackValue);
            }
        }
    }

    /** Constructs a new manager.
    */
    public NbTopManager() {
        instanceContent = new InstanceContent ();
        instanceLookup = new AbstractLookup (instanceContent);
        Lookup lookup = Lookup.getDefault();
        if (!(lookup instanceof Lkp)) {
            throw new ClassCastException("Wrong Lookup impl found: " + lookup);
        }
        ((Lkp)lookup).startedNbTopManager();
    }

    /** Getter for instance of this manager.
    */
    public static NbTopManager get () {
//        return (NbTopManager)TopManager.getDefault ();
        return getNbTopManager();
    }
    
    /** Danger method for clients who think they want an NbTM but don't actually
     * care whether it is ready or not. Should be removed eventually by getting
     * rid of useless protected methods in this class, and using Lookup to find
     * each configurable piece of impl.
     * @return a maybe half-constructed NbTM
     */
    public static NbTopManager getUninitialized() {
        if (defaultTopManager != null) {
            return defaultTopManager;
        }
        // Not even started - so synch and get it.
        return get();
    }
        
    private static NbTopManager defaultTopManager; 
    
    public static synchronized boolean isInitialized () {
        return defaultTopManager != null;
    }
    
    private static synchronized NbTopManager getNbTopManager () {
        if (defaultTopManager == null) {

            String className = System.getProperty(
                                   "org.openide.TopManager", // NOI18N
                                   "org.netbeans.core.Plain" // NOI18N
                               );

            try {
                Class c = Class.forName(className);
                defaultTopManager = (NbTopManager)c.newInstance();
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new IllegalStateException();
            }

            // late initialization of the manager if needed
            if (defaultTopManager instanceof Runnable) {
                ((Runnable)defaultTopManager).run ();
            }
        }

        return defaultTopManager;
    }

    /** Test method to check whether some level of interactivity is enabled.
    * @param il mask composed of the constants of IL_XXXX
    * @return true if such level is enabled
    */
    public abstract boolean isInteractive (int il);
    
    // XXX Seems to be needless, was used in RegistryImpl, but was dummy.
//    /** Allows subclasses to override this method and return different default set of nodes
//    * the should be "selected". If no top component is active then this method is called to
//    * allow the top manager to decide which nodes should be pointed as selected.
//    *
//    * @param activated true if the result cannot be null
//    * @return the array of nodes to return from TopComponent.getRegistry ().getSelectedNodes or
//    *    getActivatedNodes ()
//    */
//    public Node[] getDefaultNodes (boolean activated) {
//        return activated ? new Node[0] : null;
//    }
    
    //
    // The main method allows access to registration service
    //
    
    
    /** Register new instance.
     */
    public final void register (Object obj) {
        instanceContent.add (obj);
    }
    
    /** Register new instance.
     * @param obj source
     * @param conv convertor which postponing an instantiation
     */
    public final void register(Object obj, InstanceContent.Convertor conv) {
        instanceContent.add(obj, conv);
    }
    
    /** Unregisters the service.
     */
    public final void unregister (Object obj) {
        instanceContent.remove (obj);
    }
    /** Unregisters the service registered with a convertor.
     */
    public final void unregister (Object obj, InstanceContent.Convertor conv) {
        instanceContent.remove (obj, conv);
    }
    
    /** Private get instance lookup.
     */
    private final Lookup getInstanceLookup () {
        return instanceLookup;
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
            Class c = ((ClassLoader)Lookup.getDefault().lookup(ClassLoader.class)).loadClass("org.netbeans.api.javahelp.Help"); // NOI18N
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
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        // Did not work.
        Toolkit.getDefaultToolkit().beep();
    }

    public static final class NbURLDisplayer extends org.openide.awt.HtmlBrowser.URLDisplayer {
        /** WWW browser window. */
        HtmlBrowser.BrowserComponent htmlViewer;
        public void showURL(URL u) {
            if (htmlViewer == null) htmlViewer = new NbBrowser ();
            ((NbBrowser)htmlViewer).showUrl(u);
        }
    }

    /** Adds new explorer manager that will rule the selection of current
    * nodes until the runnable is running.
     * <b>Attention: This method is no longer supported and is going to 
     * be removed soon, see http://www.netbeans.org/issues/show_bug.cgi?id=28804.</b>
    *
    * @param run runnable to execute (till it is running the explorer manager is in progress)
    * @param em explorer manager 
    */
    public void attachExplorer (Runnable run, ExplorerManager em) {
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
            new UnsupportedOperationException("Method NbTopManager.attachExplorer" // NOI18N
            + " is no more supported and is going to be removed, see more at:" // NOI18N
            + " http://www.netbeans.org/issues/show_bug.cgi?id=28804")); // NOI18N
    }


    /** Opens specified project. Asks to save the previously opened project.
    * @exception IOException if error occurs accessing the project
    * @exception UserCancelException if the selection is interrupted by the user
    */
//    public void openProject (ProjectCookie project) throws IOException, UserCancelException {
//        if (ExitDialog.showDialog (null, true)) {
//            NbProjectOperation.setOpeningProject (project);
//        }
//        else {
//            throw new UserCancelException ();
//        }
//    } 


    public static final class NbStatusDisplayer extends org.openide.awt.StatusDisplayer {
        private List listeners = null;
        private String text = ""; // NOI18N
        public void setStatusText(String text) {
            ChangeListener[] _listeners;
            synchronized (this) {
                if (text.equals(this.text)) return;
                this.text = text;
                if (listeners == null || listeners.isEmpty()) {
                    return;
                } else {
                    _listeners = (ChangeListener[])listeners.toArray(new ChangeListener[listeners.size()]);
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
            if (listeners == null) listeners = new ArrayList();
            listeners.add(l);
        }
        public synchronized void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
    }

    /** saves all opened objects */
    public void saveAll () {
        DataObject dobj = null;
        ArrayList bad = new ArrayList ();
        DataObject[] modifs = DataObject.getRegistry ().getModified ();
        for (int i = 0; i < modifs.length; i++) {
            try {
                dobj = modifs[i];
                SaveCookie sc = (SaveCookie)dobj.getCookie(SaveCookie.class);
                if (sc != null) {
                    org.openide.awt.StatusDisplayer.getDefault().setStatusText (
                        java.text.MessageFormat.format (
                            NbBundle.getBundle (NbTopManager.class).getString ("CTL_FMT_SavingMessage"),
                            new Object[] { dobj.getName () }
                        )
                    );
                    sc.save();
                }
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                bad.add (dobj);
            }
        }
        NotifyDescriptor descriptor;
        //recode this part to show only one dialog?
        Iterator ee = bad.iterator ();
        while (ee.hasNext ()) {
            descriptor = new NotifyDescriptor.Message(
                        MessageFormat.format (
                            NbBundle.getBundle (NbTopManager.class).getString("CTL_Cannot_save"),
                            new Object[] { ((DataObject)ee.next()).getPrimaryFile().getName() }
                        )
                    );
            org.openide.DialogDisplayer.getDefault().notify (descriptor);
        }
        // notify user that everything is done
        org.openide.awt.StatusDisplayer.getDefault().setStatusText(
            NbBundle.getBundle (NbTopManager.class).getString ("MSG_AllSaved"));
    }
    
    // XXX
    /** Interface describing basic control over window system. 
     * @since 1.15 */
    public interface WindowSystem {
        public void show();
        public void hide();
        public void load();
        public void save();
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


    
    private boolean doingExit=false;
    public void exit ( ) {
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
    
    private void doExit() {
        if (doingExit) {
            return ;
        }
        doingExit = true;
        // save all open files
        try {
            if ( System.getProperty ("netbeans.close") != null || ExitDialog.showDialog(null) ) {
                
                final WindowSystem windowSystem = (WindowSystem)Lookup.getDefault().lookup(WindowSystem.class);
                
                // #29831: hide frames between closing() and close()
                Runnable hideFrames = new Runnable() {
                    public void run() {
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
                
                if (getModuleSystem().shutDown(hideFrames)) {
                    try {
                        try {
                            LoaderPoolNode.store();
                        } catch (IOException ioe) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                        }
                        boolean isWinsysSaved = false;
                        // save project, if applicable
                        try {
                            TrivialProjectManager tpm
                                = (TrivialProjectManager)Lookup.getDefault()
                                    .lookup(TrivialProjectManager.class);
                            // XXX #29159 Empty trivial prj manager doesn't store anything.
                            if(!(tpm instanceof TrivialProjectManager.Empty)) {
                                tpm.store();
                                isWinsysSaved = true;
                            }
                        } catch (IOException ioe) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                        }
                        // save window system, [PENDING] remove this after the winsys will
                        // persist its state automaticaly
                        if (!isWinsysSaved) {
                            if(windowSystem != null) {
                                windowSystem.save();
                            }
                        }
                        org.netbeans.core.projects.XMLSettingsHandler.saveOptions();
                        try {
                            ((Lkp)Lookup.getDefault()).storeCache();
                        } catch (IOException ioe) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                        }
                        org.netbeans.core.projects.SessionManager.getDefault().close();
                    } catch (ThreadDeath td) {
                        throw td;
                    } catch (Throwable t) {
                        // Do not let problems here prevent system shutdown. The module
                        // system is down; the IDE cannot be used further.
                        ErrorManager.getDefault().notify(t);
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
    
    public static final class NbLifecycleManager extends LifecycleManager {
        public void saveAll() {
            NbTopManager.get().saveAll();
        }
        public void exit() {
            NbTopManager.get().exit();
        }
    }

    /** Shows exit dialog for activated File system nodes
    * after unmounting filesystem(s)
    * @return result of dialog (mount or unmount)
    */    
    public static boolean showExitDialog (Node[] activatedNodes) {
        return ExitDialog.showDialog(activatedNodes);
    }
    
    /** Get the module subsystem. */
    public abstract ModuleSystem getModuleSystem();

    /** Reads system properties from a file on a disk and stores them 
     * in System.getPropeties ().
     */
    private static void readEnvMap () throws IOException {
        java.util.Properties env = System.getProperties ();
        String envfile = System.getProperty("netbeans.osenv"); // NOI18N
        if (envfile != null) {
                // XXX is any non-ASCII encoding even defined? unclear...
                BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(envfile)));
                // #30621: use \0 when possible, \n as a fallback
                char sep = Boolean.getBoolean("netbeans.osenv.nullsep") ? '\0' : '\n';
                StringBuffer key = new StringBuffer(100);
                StringBuffer value = new StringBuffer(1000);
                boolean inkey = true;
                while (true) {
                    int c = in.read();
                    if (c == -1) {
                        break;
                    }
                    char cc = (char)c;
                    if (inkey) {
                        if (cc == sep) {
                            throw new IOException("null-term'd key"); // NOI18N
                        } else if (cc == '=') {
                            inkey = false;
                        } else {
                            key.append(cc);
                        }
                    } else {
                        if (cc == sep) {
                            // [pnejedly] These new String() calls are intentional
                            // because of memory consumption. Don't touch them
                            // unless you know what you're doing
                            inkey = true;
                            String k = key.toString();
                            String v = new String(value.toString());
                            env.put(new String("Env-" + k), v); // NOI18N
                            // E.g. on Turkish Unix, want env-display not env-d\u0131splay:
                            env.put(new String("env-" + k.toLowerCase(Locale.US)), v); // NOI18N
                            key.setLength(0);
                            value.setLength(0);
                        } else {
                            value.append(cc);
                        }
                    }
                }
        }
    }


    /**
    * For externalization of HTMLBrowser.
    */
    public static class NbBrowser extends HtmlBrowser.BrowserComponent {

        static final long serialVersionUID =5000673049583700380L;

        private transient PropertyChangeListener idePCL = null;
        /**
        * For externalization.
        */
        public NbBrowser () {
            super (((IDESettings)IDESettings.findObject (IDESettings.class, true)).getWWWBrowser (), true, true);
            putClientProperty("TabPolicy", "HideWhenAlone"); // NOI18N
            setListener ();
        }
        
        /** 
         * Release resources and also allow to create new browser later using another implementation
         * @return result from ancestor is returned 
         */
        protected boolean closeLast () {
            if (idePCL != null) {
                ((IDESettings)IDESettings.findObject (IDESettings.class, true)).removePropertyChangeListener (idePCL);
                idePCL = null;
            }
            ((NbURLDisplayer)org.openide.awt.HtmlBrowser.URLDisplayer.getDefault()).htmlViewer = null;
            return super.closeLast ();
        }

	/** Show URL in browser
	 * @param url URL to be shown 
	 */
	private void showUrl (URL url) {
            open ();
            requestFocus ();
            setURL (url);
	}
        
        /* Deserialize this top component.
        * @param in the stream to deserialize from
        */
        public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal (in);
            setListener ();
            ((NbURLDisplayer)org.openide.awt.HtmlBrowser.URLDisplayer.getDefault()).htmlViewer = this;
        }

        /**
         *  Sets listener that invalidates this as main IDE's browser if user changes the settings
         */
        private void setListener () {
            if (idePCL != null)
                return;
            try {
                // listen on preffered browser change
                idePCL = new PropertyChangeListener () {
                    public void propertyChange (PropertyChangeEvent evt) {
                        String name = evt.getPropertyName ();
                        if (name == null) return;
                        if (name.equals (IDESettings.PROP_WWWBROWSER)) {
                            ((NbURLDisplayer)org.openide.awt.HtmlBrowser.URLDisplayer.getDefault()).htmlViewer = null;
                            if (idePCL != null) {
                                ((IDESettings)IDESettings.findObject (IDESettings.class, true))
                                .removePropertyChangeListener (idePCL);
                                idePCL = null;
                            }
                        }
                    }
                };
                ((IDESettings)IDESettings.findObject (IDESettings.class, true)).addPropertyChangeListener (idePCL);
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
    
    /** The default lookup for the system.
     */
    public static final class Lkp extends ProxyLookup {
        private static boolean started = false;
        /** currently effective ClassLoader */
        private static ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        
        /** Someone called NbTopManager.get().
         * That means that subsequent calls to lookup on ModuleInfo
         * need not try to get it again.
         */
        public static void startedNbTopManager() {
            started = true;
        }
        
        /** Initialize the lookup to delegate to NbTopManager.
        */
        public Lkp () {
            super (new Lookup[] {
                       // #14722: pay attention also to META-INF/services/class.Name resources:
                       Lookups.metaInfServices(classLoader),
                       Lookups.singleton(classLoader),
                       Lookup.EMPTY, // will be moduleLookup
                   });
        }
                
        /** Called when a system classloader changes.
         */
        public static final void systemClassLoaderChanged (ClassLoader nue) {
            if (classLoader != nue) {
                classLoader = nue;
                Lkp l = (Lkp)Lookup.getDefault();
                Lookup[] delegates = l.getLookups();
                Lookup[] newDelegates = (Lookup[])delegates.clone();
                // Replace classloader.
                newDelegates[0] = Lookups.metaInfServices(classLoader);
                newDelegates[1] = Lookups.singleton(classLoader);
                l.setLookups(newDelegates);
            }
        }

        /** Called when modules are about to be turned on.
         */
        public static final void moduleClassLoadersUp() {
            Lkp l = (Lkp)Lookup.getDefault();
            Lookup[] newDelegates = null;
            Lookup[] delegates = l.getLookups();
            newDelegates = (Lookup[])delegates.clone();
            newDelegates[0] = Lookups.metaInfServices(classLoader);
            l.setLookups(newDelegates);
        }

        /** Called when Lookup<ModuleInfo> is ready from the ModuleManager.
         * @see "#28465"
         */
        public static final void moduleLookupReady(Lookup moduleLookup) {
            Lkp l = (Lkp)Lookup.getDefault();
            Lookup[] newDelegates = (Lookup[])l.getLookups().clone();
            newDelegates[2] = moduleLookup;
            l.setLookups(newDelegates);
        }

        /** When all module classes are accessible thru systemClassLoader, this
         * method is called to initialize the FolderLookup.
         */
	    
        public static final void modulesClassPathInitialized () {
            //System.err.println("mCPI");
	    //StartLog.logStart ("NbTopManager$Lkp: initialization of FolderLookup"); // NOI18N

            // replace the lookup by new one
            Lookup lookup = Lookup.getDefault ();
	    StartLog.logProgress ("Got Lookup"); // NOI18N

            ((Lkp)lookup).doInitializeLookup ();
        }
        
        private final void doInitializeLookup () {
            //System.err.println("doInitializeLookup");

            // extend the lookup
            Lookup[] arr = new Lookup[] {
                getLookups()[0], // metaInfServicesLookup
                getLookups()[1], // ClassLoader lookup
                getLookups()[2], // ModuleInfo lookup
                // XXX figure out how to put this ahead of MetaInfServicesLookup (for NonGuiMain):
                NbTopManager.get ().getInstanceLookup (),
                LookupCache.load(),
            };
            StartLog.logProgress ("prepared other Lookups"); // NOI18N

            setLookups (arr);
            StartLog.logProgress ("Lookups set"); // NOI18N

	    //StartLog.logEnd ("NbTopManager$Lkp: initialization of FolderLookup"); // NOI18N
        }
        
        public void storeCache() throws IOException {
            Lookup[] ls = getLookups();
            if (ls.length == 5) {
                // modulesClassPathInitialized has been called, so store folder lookup
                LookupCache.store(ls[4]);
            }
        }
        
        protected void beforeLookup(Lookup.Template templ) {
            Class type = templ.getType();
            
            // Force module system to be initialize by looking up ModuleInfo.
            // Good for unit tests, etc.
            if (!started && (type == ModuleInfo.class || type == Module.class)) {
                NbTopManager.get();
            }
            
            super.beforeLookup(templ);
        }
    }
    
}
