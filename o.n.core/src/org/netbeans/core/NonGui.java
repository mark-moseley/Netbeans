/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Set;
import java.util.Iterator;
import java.text.MessageFormat;
import javax.swing.*;
import javax.swing.border.*;
import java.lang.reflect.Method;

import org.openide.util.datatransfer.ExClipboard;
import org.openide.*;
import org.openide.cookies.ExecCookie;
import org.openide.loaders.*;
import org.openide.actions.*;
import org.openide.actions.PropertiesAction;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;
import org.openide.windows.*;
import org.openide.explorer.*;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;
import org.openide.util.RequestProcessor;
import org.openide.util.UserCancelException;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.SystemAction;
import org.openide.util.io.*;
import org.openide.nodes.*;
import org.openide.util.Utilities;

import org.netbeans.core.actions.*;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.projects.ModuleLayeredFileSystem;
import org.netbeans.core.perftool.StartLog;
import org.netbeans.core.modules.ModuleSystem;
import org.netbeans.core.xml.XML;
import org.netbeans.core.execution.ExecutionSettings;
import org.netbeans.core.execution.TopSecurityManager;

/** This class is a TopManager for Corona environment.
*
* @author Ales Novak, Jaroslav Tulach, Ian Formanek, Petr Hamernik, Jan Jancura
*/
public class NonGui extends NbTopManager implements Runnable {

    /** directory for modules */
    static final String DIR_MODULES = "modules"; // NOI18N
    
    /** name of system folder to be located in the USER_DIR and HOME_DIR */
    private static final String SYSTEM_FOLDER = "system"; // NOI18N


    /* The class of the UIManager to be used for netbeans - can be set by command-line argument -ui <class name> */
    protected static Class uiClass;

    /* The size of the fonts in the UI - 0 pt, the default value is set in NbTheme (for Metal L&F), for other L&Fs is set
       in the class Main. The value can be changed in Themes.xml in system directory or by command-line argument -fontsize <size> */
    protected static int uiFontSize = 0;

    /** The netbeans home dir - acquired from property netbeans.home */
    private static String homeDir;
    /** The netbeans user dir - acquired from property netbeans.user / or copied from homeDir if such property does not exist */
    private static String userDir;
    /** The netbeans system dir - it is netbeans.home/system or a folder specified via -system option */
    private static String systemDir;

    /** system file system */
    private static FileSystem systemFileSystem;
    
    /** module subsystem */
    private static ModuleSystem moduleSystem;

    /** The flag whether to create the log - can be set via -nologging
    * command line option */
    private static boolean noLogging = false;

    /** Flag telling if the system clipboard should be used or not.
    * This helps avoid crashes and hangs on Unixes.
    */
    private static boolean noSysClipboard = false;

    /** The flag whether to show the Splash screen on the startup */
    protected  static boolean noSplash = false;

    /** The Class that logs the IDE events to a log file */
    private static TopLogging logger;

    /** The flag for accessibility */
    public static boolean accessibility = false;
    
    /** Getter for home directory. */
    protected static String getHomeDir () {
        if (homeDir == null) {
            homeDir = System.getProperty ("netbeans.home");
            if (homeDir == null) {
                System.err.println(NbBundle.getBundle("org.netbeans.core.Bundle",java.util.Locale.getDefault(),Main.class.getClassLoader()).getString("CTL_Netbeanshome_property"));
                doExit (1);
            }
        }
        return homeDir;
    }

    /** Getter for user home directory. */
    protected static String getUserDir () {
        if (userDir == null) {
            userDir = System.getProperty ("netbeans.user");
            
            if (userDir == null) {
                userDir = getHomeDir ();
                System.setProperty("netbeans.user", homeDir); // NOI18N                                
            }
            
            /** #11735. Relative userDir is converted to absolute*/
            userDir = new File(userDir).getAbsolutePath();
            // #21085: userDir might contain ../ sequences which should be removed
            // Note that the meaning of ".." is defined on Windows and Unix but may
            // be quite different on other OSs, so this is just a heuristic.
            if (userDir.indexOf("..") != -1) { // NOI18N
                try {
                    userDir = new File(userDir).getCanonicalPath();
                } catch (IOException ioe) {
                    // No harm done; leave it non-canonicalized.
                }
            }
            System.setProperty("netbeans.user", userDir); // NOI18N
            
            File systemDirFile = new File (userDir, SYSTEM_FOLDER);
            if (!systemDirFile.isDirectory ()) {
                // try to create it
                makedir (systemDirFile);
                if (! userDir.equals (homeDir)) {
                    // Need to set up a multiuser user directory. Formerly in launcher.

                    touch (new File (systemDirFile, "project.last_hidden")); // NOI18N
                    touch (new File (systemDirFile, "project.basic_hidden")); // NOI18N
                    File projDir = new File (systemDirFile, "Projects"); // NOI18N
                    makedir (projDir);
                    touch (new File (projDir, "workspace.ser_hidden")); // NOI18N
                    makedir (new File (new File (userDir, DIR_MODULES), "autoload")); // NOI18N
                    makedir (new File (new File (userDir, DIR_MODULES), "eager")); // NOI18N
                    File libDir = new File (userDir, "lib"); // NOI18N
                    makedir (libDir);
                    makedir (new File (libDir, "ext")); // NOI18N
                    makedir (new File (libDir, "patches")); // NOI18N
                }
            }
            systemDir = systemDirFile.getAbsolutePath ();
        }
        return userDir;
    }

    private static void makedir (File f) {
        if (f.isFile ()) {
            Object[] arg = new Object[] {f};
            System.err.println (new MessageFormat(getString("CTL_CannotCreate_text")).format(arg));
            doExit (6);
        }
        if (! f.exists ()) {
            if (! f.mkdirs ()) {
                Object[] arg = new Object[] {f};
                System.err.println (new MessageFormat(getString("CTL_CannotCreateSysDir_text")).format(arg));
                doExit (7);
            }
        }
    }

    private static void touch (File f) {
        try {
            new FileOutputStream (f).close ();
        } catch (IOException ioe) {
            System.err.println (ioe);
            doExit (8);
        }
    }

    /** System directory getter.
    */
    protected static String getSystemDir () {
        getUserDir ();
        return systemDir;
    }

    //
    // Protected methods that are provided for subclasses (Main)
    // to plug-in better implementation
    //
    protected FileSystem createDefaultFileSystem () {
        if (systemFileSystem != null) {
            return systemFileSystem;
        }

        // -----------------------------------------------------------------------------------------------------
        // 1. Initialization and checking of netbeans.home and netbeans.user directories

        File homeDirFile = new File (getHomeDir ());
        File userDirFile = new File (getUserDir ());
        if (!homeDirFile.exists ()) {
            System.err.println (getString("CTL_Netbeanshome_notexists"));
            doExit (2);
        }
        if (!homeDirFile.isDirectory ()) {
            System.err.println (getString("CTL_Netbeanshome1"));
            doExit (3);
        }
        if (!userDirFile.exists ()) {
            System.err.println (getString("CTL_Netbeanshome2"));
            doExit (4);
        }
        if (!userDirFile.isDirectory ()) {
            System.err.println (getString("CTL_Netbeanshome3"));
            doExit (5);
        }

        // -----------------------------------------------------------------------------------------------------
        // 7. Initialize FileSystems

        setStatusText (getString("MSG_FSInit"));
        // system FS !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        {
            Exception exc = null;
            try {
                File u = new File (userDirFile, SYSTEM_FOLDER);
                File h = new File (homeDirFile, SYSTEM_FOLDER);
                systemFileSystem = org.netbeans.core.projects.SessionManager.getDefault().create(u, h);
            } catch (IOException ex) {
                exc = ex;
            } catch (java.beans.PropertyVetoException ex) {
                exc = ex;
            }

            if (exc != null) {
                exc.printStackTrace ();
                Object[] arg = new Object[] {systemDir};
                System.err.println (new MessageFormat(getString("CTL_Cannot_mount_systemfs")).format(arg));
                doExit (3);
            }
        }

        return systemFileSystem;
    }

    /** Subclasses will do better. */
    protected void setStatusTextImpl(String text) {}

    /** Everything is interactive */
    public boolean isInteractive (int il) {
        return true;
    }

    /** Creates error logger.
     */
    protected PrintWriter createErrorLogger (int minLogSeverity) {
       if (minLogSeverity < -1) {
            return new PrintWriter (System.err);
        } else {
            return new PrintWriter (TopLogging.getLogOutputStream ());
        }
    }
    
    protected static void showHelp() {
        System.out.println(getString("CTL_Cmd_options"));
        System.out.println(getString("CTL_System_option"));
        System.out.println("                      " + getString("CTL_System_option2"));
        System.out.println(getString("CTL_UI_option"));
        System.out.println(getString("CTL_FontSize_option"));
        System.out.println(getString("CTL_Locale_option"));
        System.out.println(getString("CTL_Branding_option"));
        System.out.println(getString("CTL_Nologging_option"));
        System.out.println(getString("CTL_Nosysclipboard_option"));
    }

    public static void parseCommandLine(String[] args) {
        boolean specifiedBranding = false;

        // let's go through the command line
        for(int i = 0; i < args.length; i++)
        {
            if (args[i].equalsIgnoreCase("-nogui")) { // NOI18N
                System.getProperties().put (
                    "org.openide.TopManager", // NOI18N
                    "org.netbeans.core.NonGui" // NOI18N
                );
            } else if (args[i].equalsIgnoreCase("-nosplash")) // NOI18N
                noSplash = true;
            else if (args[i].equalsIgnoreCase("-accessibility")) // NOI18N
            {
                accessibility = true;
                java.lang.System.setProperty("netbeans.accessibility","true");
            }
            else if (args[i].equalsIgnoreCase("-noinfo")) { // NOI18N
                // obsolete switch, ignore
            }
            else if (args[i].equalsIgnoreCase("-nologging")) // NOI18N
                noLogging = true;
            else if (args[i].equalsIgnoreCase("-nosysclipboard")) // NOI18N
                noSysClipboard = true;
            else if (args[i].equalsIgnoreCase("-system")) { // NOI18N
                systemDir = args[++i];
            }
            else if (args[i].equalsIgnoreCase("-ui")) { // NOI18N
                try {
                    uiClass = Class.forName(args[++i]);
                } catch(ArrayIndexOutOfBoundsException e) {
                    System.out.println(getString("ERR_UIExpected"));
                } catch (ClassNotFoundException e2) {
                    System.out.println(getString("ERR_UINotFound"));
                }
            } else if (args[i].equalsIgnoreCase("-fontsize")) { // NOI18N
                try {
                    uiFontSize = Integer.parseInt (args[++i]);
                } catch(ArrayIndexOutOfBoundsException e) {
                    System.out.println(getString("ERR_FontSizeExpected"));
                } catch (NumberFormatException e2) {
                    System.out.println(getString("ERR_BadFontSize"));
                }

            } else if (args[i].equalsIgnoreCase("-locale")) { // NOI18N
                String localeParam = args[++i];
                String language;
                String country = ""; // NOI18N
                String variant = ""; // NOI18N
                int index1 = localeParam.indexOf(":"); // NOI18N
                if (index1 == -1)
                    language = localeParam;
                else {
                    language = localeParam.substring(0, index1);
                    int index2 = localeParam.indexOf(":", index1+1); // NOI18N
                    if (index2 != -1) {
                        country = localeParam.substring(index1+1, index2);
                        variant = localeParam.substring(index2+1);
                    }
                    else
                        country = localeParam.substring(index1+1);
                }
                java.util.Locale.setDefault(new java.util.Locale(language, country, variant));
            } else if (args[i].equalsIgnoreCase ("-branding")) { // NOI18N
                String branding = args[++i];
                if (branding.equals ("-")) branding = null; // NOI18N
                try {
                    NbBundle.setBranding (branding);
                } catch (IllegalArgumentException iae) {
                    iae.printStackTrace ();
                }
                specifiedBranding = true;
            }
            else if (args[i].equalsIgnoreCase("-?") || args[i].equalsIgnoreCase("-help")) { // NOI18N
                showHelp();
                doExit(0);
            }
            else {
                System.out.println(getString("ERR_UnknownOption")+": "+args[i]);
                showHelp();
                doExit(0);
            }
        }

        if (!noLogging) {
            try {
                logger = new TopLogging(getSystemDir());
            } catch (IOException e) {
                System.err.println("Cannot create log file. Logging disabled."); // NOI18N
                e.printStackTrace ();
            }
        }
        StartLog.logProgress ("TopLogging initialized"); // NOI18N
        
        if (! specifiedBranding) {
            // Read default branding from file "lib/branding" in installation.
            File branding = new File (getUserDir (), "lib" + File.separator + "branding"); // NOI18N
            if (! branding.exists ())
                branding = new File (getHomeDir (), "lib" + File.separator + "branding"); // NOI18N
            if (branding.exists ()) {
                try {
                    InputStream is = new FileInputStream (branding);
                    try {
                        BufferedReader rd = new BufferedReader (new InputStreamReader (is));
                        String line = rd.readLine ();
                        if (line == null || line.equals ("")) // NOI18N
                            throw new IOException ("empty branding file"); // NOI18N
                        if (rd.readLine () != null)
                            throw new IOException ("branding file more than one line"); // NOI18N
                        line = line.trim ();
                        if (line.equals ("-")) line = null; // NOI18N
                        try {
                            NbBundle.setBranding (line);
                        } catch (IllegalArgumentException iae) {
                            iae.printStackTrace ();
                        }
                    } finally {
                        is.close ();
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace ();
                }
            }
        }
    }
    
    /** Lazily loads classes */ // #9951
    private final Class getKlass(String cls) {
        try {
            return Class.forName(cls, false, getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getLocalizedMessage());
        }
    }

    /** Initialization of the manager.
    */
    public void run () {
        StartLog.logStart ("TopManager initialization (org.netbeans.core.NonGui.run())"); //NOI18N
        
        // because of KL Group components, we define a property netbeans.design.time
        // which serves instead of Beans.isDesignTime () (which returns false in the IDE)
        System.getProperties ().put ("netbeans.design.time", "true"); // NOI18N

        // Initialize beans - [PENDING - better place for this ?]
        //                    [PENDING - can PropertyEditorManager garbage collect ?]
        java.beans.Introspector.setBeanInfoSearchPath (new String[] {
                    "org.netbeans.beaninfo", // NOI18N
                    "org.netbeans.beaninfo.awt", // NOI18N
                    "org.netbeans.beaninfo.swing", // NOI18N
                    "sun.beans.infos" // NOI18N
                });
        java.beans.PropertyEditor pe = java.beans.PropertyEditorManager.findEditor(java.lang.Byte.TYPE); // to enforce initialization of registering PE for primitive types
        java.beans.PropertyEditorManager.setEditorSearchPath (
            new String[] { "org.netbeans.beaninfo.editors", "org.openide.explorer.propertysheet.editors" }); // NOI18N
        java.beans.PropertyEditorManager.registerEditor (getKlass("[Ljava.lang.String;"), getKlass("org.openide.explorer.propertysheet.editors.StringArrayEditor"));
        java.beans.PropertyEditorManager.registerEditor (getKlass("[Lorg.openide.src.MethodParameter;"), getKlass("org.openide.explorer.propertysheet.editors.MethodParameterArrayEditor"));
        java.beans.PropertyEditorManager.registerEditor (getKlass("[Lorg.openide.src.Identifier;"), getKlass("org.openide.explorer.propertysheet.editors.IdentifierArrayEditor"));
        java.beans.PropertyEditorManager.registerEditor (java.lang.Character.TYPE, getKlass("org.netbeans.beaninfo.editors.CharEditor"));
        StartLog.logProgress ("PropertyEditors registered"); // NOI18N

        // -----------------------------------------------------------------------------------------------------

        setStatusText (getString("MSG_IDEInit"));


        // -----------------------------------------------------------------------------------------------------
        // 7. Initialize FileSystems
        getRepository ();
        StartLog.logProgress ("Repository initialized"); // NOI18N
        
        // -----------------------------------------------------------------------------------------------------
        // Upgrade
        /*try {
            if ((System.getProperty ("netbeans.full.hack") == null) && (System.getProperty ("netbeans.close") == null)) {
                System.setProperty("import.canceled", "false"); // NOI18N
                
                
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        // Original code
                        //boolean canceled = org.netbeans.core.upgrade.UpgradeWizard.showWizard(getSplash());
                        //System.setProperty("import.canceled", new Boolean(canceled).toString()); // NOI18N
                        
                        // Let's use reflection
                        System.setProperty("import.canceled", new Boolean( false ).toString()); // NOI18N
                        try {
                            Class wizardClass = Class.forName( "org.netbeans.core.upgrade.UpgradeWizard" ); // NOI18N
                            Method showMethod = wizardClass.getMethod( "showWizard", new Class[] { Splash.SplashOutput.class } ); // NOI18N
                            
                            Boolean canceled = (Boolean)showMethod.invoke( null, new Object[] { getSplash() } );
                            System.setProperty("import.canceled", canceled.toString()); // NOI18N
                        }
                        catch ( ClassNotFoundException e ) {
                            // Assume there is no UpgradeWizard to run
                        }
                        catch ( NoSuchMethodException e ) {
                            // Assume there is no UpgradeWizard to run
                        }
                        catch ( IllegalAccessException e ) {
                            // Assume there is no UpgradeWizard to run
                        }
                        catch ( java.lang.reflect.InvocationTargetException e ) {
                            // Assume there is no UpgradeWizard to run
                        }
                        
                    }
                });
                if (Boolean.getBoolean("import.canceled"))
                    TopSecurityManager.exit(0);
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
        finally {
            showSplashAgain();
        }
        StartLog.logProgress ("Upgrade wizzard consulted"); // NOI18N
        */
        // -----------------------------------------------------------------------------------------------------
        // 9. Modules

        {
    	    StartLog.logStart ("Modules initialization"); // NOI18N

            FileSystem sfs = getRepository().getDefaultFileSystem();
            File moduleDirHome = new File(homeDir, DIR_MODULES);
            File moduleDirUser;
            if (homeDir.equals(userDir)) {
                moduleDirUser = null;
            } else {
                moduleDirUser = new File(userDir, DIR_MODULES);
            }
            try {
                moduleSystem = new ModuleSystem(getRepository().getDefaultFileSystem(), moduleDirHome, moduleDirUser);
            } catch (IOException ioe) {
                // System will be screwed up.
                IllegalStateException ise = new IllegalStateException("Module system cannot be created"); // NOI18N
                getErrorManager().annotate(ise, ioe);
                throw ise;
            }
    	    StartLog.logProgress ("ModuleSystem created"); // NOI18N
            fireSystemClassLoaderChange();

            moduleSystem.loadBootModules();
            moduleSystem.readList();
            Main.addAndSetSplashMaxSteps(40); // additional steps after loading all modules
            moduleSystem.scanForNewAndRestore();
    	    StartLog.logEnd ("Modules initialization"); // NOI18N
        }

        
        // autoload directories
        org.openide.util.Task automount = AutomountSupport.initialize ();
        StartLog.logProgress ("Automounter fired"); // NOI18N
        Main.incrementSplashProgressBar();
        
        // -----------------------------------------------------------------------------------------------------
        // 10. Initialization of project (because it can change loader pool and it influences main window menu)
        try {
            NbProjectOperation.openOrCreateProject ();
        } catch (IOException e) {
            getErrorManager ().notify (ErrorManager.INFORMATIONAL, e);
        }
        StartLog.logProgress ("Project opened"); // NOI18N
        Main.incrementSplashProgressBar(10);

        // load neccessary SystemOptions because of ExecuteAction setup
        SharedClassObject.findObject (ExecutionSettings.class, true);
        StartLog.logProgress ("ExecutionSettings loaded"); // NOI18N
        Main.incrementSplashProgressBar(10);

        LoaderPoolNode.installationFinished ();
        StartLog.logProgress ("LoaderPool notified"); // NOI18N
        Main.incrementSplashProgressBar(10);

        // -----------------------------------------------------------------------------------------------------
        // 15. Install new modules
        moduleSystem.installNew();
        StartLog.logProgress ("New modules installed"); // NOI18N
        Main.incrementSplashProgressBar(10);

        //-------------------------------------------------------------------------------------------------------
        // setup wizard
        /*try {
            if ((System.getProperty ("netbeans.full.hack") == null) && (System.getProperty ("netbeans.close") == null)) {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        org.netbeans.core.ui.SetupWizard.showSetupWizard(false, getSplash());
                    }
                });
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
        finally {
            showSplashAgain();
        }
        StartLog.logProgress ("SetupWizard done"); // NOI18N
        */
        Main.incrementSplashProgressBar(10);
        // wait until mounting really occurs
        automount.waitFinished ();
        StartLog.logProgress ("Automounter done"); // NOI18N
        Main.incrementSplashProgressBar(10);

        //---------------------------------------------------------------------------------------------------------
        // initialize main window AFTER the setup wizard is finished

        initializeMainWindow ();
        StartLog.logProgress ("Main window initialized"); // NOI18N
        StartLog.logEnd ("TopManager initialization (org.netbeans.core.NonGui.run())"); //NOI18N
        Main.incrementSplashProgressBar();

        // -----------------------------------------------------------------------------------------------------
        // 8. Advance Policy

        java.security.Policy.getPolicy().getPermissions(new java.security.CodeSource(null, null)).implies(new java.security.AllPermission());

        // set security manager

        org.netbeans.core.execution.TopSecurityManager secman =
            new org.netbeans.core.execution.TopSecurityManager();

        // XXX(-trung) workaround for IBM JDK 1.3 Linux bug in
        // java.net.URLClassLoader.findClass().  The IBM implementation of this
        // method is not reentrant. The problem happens when findClass()
        // indirectly calls methods of TopSecurityManager for the first time.
        // This may trigger other classes to be loaded, thus findClass() is
        // re-entered.
        //
        // We try to force dependent classes of TopSecurityManager to be loaded
        // before setting it as system's SecurityManager
        
        try {
            secman.checkRead("xxx"); // NOI18N
        }
        catch (RuntimeException ex) {
            // ignore
        }
        
        System.setSecurityManager(secman);

        // install java.net.Authenticator
        java.net.Authenticator.setDefault (new NbAuthenticator ());
        StartLog.logProgress ("Security managers installed"); // NOI18N
        Main.incrementSplashProgressBar();

        // run classes int Startup folder
        
        startFolder (getDefault ().getPlaces ().folders ().startup ());
        StartLog.logProgress ("StartFolder content started"); // NOI18N
        Main.incrementSplashProgressBar();
    }


    /** Method to initialize the main window.
    */
    protected void initializeMainWindow () {
    }

    /** Getter for a text from resource.
    * @param resName resource name
    * @return string with resource
    */
    static String getString (String resName) {
        return NbBundle.getBundle (Main.class).getString (resName);
    }

    /** Getter for a text from resource with one argument.
    * @param resName resource name
    * @return string with resource
    * @param arg the argument
    */
    static String getString (String resName, Object arg) {
        MessageFormat mf = new MessageFormat (getString (resName));
        return mf.format (new Object[] { arg });
    }

    /** Getter for a text from resource with one argument.
    * @param resName resource name
    * @return string with resource
    * @param arg1 the argument
    * @param arg2 the argument
    */
    static String getString (String resName, Object arg1, Object arg2) {
        MessageFormat mf = new MessageFormat (getString (resName));
        return mf.format (new Object[] { arg1, arg2 });
    }

    /** Exits from the VM.
    */
    static void doExit (int code) {
        TopSecurityManager.exit(code);
    }



    /** Starts a folder by executing all of its executable children
    * @param f the folder
    */
    private static void startFolder (DataFolder f) {
        DataObject[] obj = f.getChildren ();
        org.openide.actions.ExecuteAction.execute(obj, true);
    }

    /** Get the module subsystem.  */
    public ModuleSystem getModuleSystem() {
        return moduleSystem;
    }

    /** This is a notification about hiding wizards 
     * during startup (Import, Setup). It is used in subclass 
     * for showing the splash screen again, when wizard disappears.
     *
     * It does nothing in NonGui implementation.
     */
    protected void showSplashAgain() {
    }

    /** Return splash screen if available.
     */
    protected Splash.SplashOutput getSplash() {
        return null;
    }
    
}
