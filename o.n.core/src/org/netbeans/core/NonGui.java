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
import java.net.URL;
import java.security.*;
import java.text.MessageFormat;
import javax.swing.*;
import javax.swing.border.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.openide.*;
import org.openide.awt.StatusDisplayer;
import org.openide.loaders.*;
import org.openide.actions.*;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;
import org.openide.windows.*;
import org.openide.explorer.*;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;
import org.openide.util.Utilities;
import org.openide.util.io.*;
import org.openide.nodes.*;

import org.netbeans.TopSecurityManager;

import org.netbeans.core.actions.*;
import org.netbeans.core.modules.InstalledFileLocatorImpl;
import org.netbeans.core.perftool.StartLog;
import org.netbeans.core.projects.TrivialProjectManager;
import org.netbeans.core.modules.ModuleSystem;

/** This class is a TopManager for Corona environment.
*
* @author Ales Novak, Jaroslav Tulach, Ian Formanek, Petr Hamernik, Jan Jancura
*/
public class NonGui extends NbTopManager implements Runnable {
    
    static {
        // #27330: installation in directory with hash marks
        // But #29935: first prime it so it need do no more class loading later
        try {
            Utilities.toURL(new File("").getAbsoluteFile()); // NOI18N
        } catch (MalformedURLException mfue) {
            // should not happen
            mfue.printStackTrace();
        }
        org.netbeans.Main.setURLConvertor(new org.netbeans.Main.URLConvertor() {
            public URL toURL(File f) throws MalformedURLException {
                return Utilities.toURL(f);
            }
        });
    }

    /** directory for modules */
    static final String DIR_MODULES = "modules"; // NOI18N
    
    /* The class of the UIManager to be used for netbeans - can be set by command-line argument -ui <class name> */
    protected static Class uiClass;

    /* The size of the fonts in the UI - 0 pt, the default value is set in NbTheme (for Metal L&F), for other L&Fs is set
       in the class Main. The value can be changed in Themes.xml in system directory or by command-line argument -fontsize <size> */
    protected static int uiFontSize = 0;

    /** The netbeans home dir - acquired from property netbeans.home */
    private static String homeDir;
    /** The netbeans user dir - acquired from property netbeans.user */
    private static String userDir;
    /** The netbeans system dir - ${netbeans.user}/system */
    private static String systemDir;

    /** module subsystem */
    private static ModuleSystem moduleSystem;

    /** The flag whether to create the log - can be set via -nologging
    * command line option */
    private static boolean noLogging = false;

    /** The flag whether to show the Splash screen on the startup */
    protected  static boolean noSplash = false;

    /** The Class that logs the IDE events to a log file */
    private static TopLogging logger;

    /** Getter for home directory. */
    protected static String getHomeDir () {
        if (homeDir == null) {
            homeDir = System.getProperty ("netbeans.home");
        }
        return homeDir;
    }

    /** Getter for user home directory. */
    protected static String getUserDir () {
        if (userDir == null) {
            userDir = System.getProperty ("netbeans.user");
            
            if (userDir == null) {
                System.err.println(NbBundle.getMessage(NonGui.class, "ERR_no_user_directory"));
                doExit(1);
            }
            if (userDir.equals(getHomeDir())) {
                System.err.println(NbBundle.getMessage(NonGui.class, "ERR_user_directory_is_home"));
                doExit(1);
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
            
            File systemDirFile = new File (userDir, NbRepository.SYSTEM_FOLDER);
            makedir (systemDirFile);
            systemDir = systemDirFile.getAbsolutePath ();
            makedir (new File (new File (userDir, DIR_MODULES), "autoload")); // NOI18N
            makedir (new File (new File (userDir, DIR_MODULES), "eager")); // NOI18N
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

    /** System directory getter.
    */
    protected static String getSystemDir () {
        getUserDir ();
        return systemDir;
    }

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
        System.out.println(getString("TEXT_help"));
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
            else if (args[i].equalsIgnoreCase("-noinfo")) { // NOI18N
                // obsolete switch, ignore
            }
            else if (args[i].equalsIgnoreCase("-nologging")) // NOI18N
                noLogging = true;
            /* not supported:
            else if (args[i].equalsIgnoreCase("-nosysclipboard")) // NOI18N
                noSysClipboard = true;
            else if (args[i].equalsIgnoreCase("-system")) { // NOI18N
                systemDir = args[++i];
            }
            */
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
                // XXX should use a format
                System.out.println(getString("ERR_UnknownOption")+": "+args[i]);
                showHelp();
                doExit(0);
            }
        }

        if (! specifiedBranding) {
            // Read default branding from file "lib/branding" in installation.
            File branding = new File(new File(getHomeDir(), "lib"), "branding"); // NOI18N
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

        if (!noLogging) {
            try {
                logger = new TopLogging(getSystemDir());
            } catch (IOException e) {
                System.err.println("Cannot create log file. Logging disabled."); // NOI18N
                e.printStackTrace ();
            }
        }
        StartLog.logProgress ("TopLogging initialized"); // NOI18N
        
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
        String[] sysbisp = Introspector.getBeanInfoSearchPath();
        String[] nbbisp = new String[] {
            "org.netbeans.beaninfo", // NOI18N
        };
        String[] allbisp = new String[sysbisp.length + nbbisp.length];
        System.arraycopy(nbbisp, 0, allbisp, 0, nbbisp.length);
        System.arraycopy(sysbisp, 0, allbisp, nbbisp.length, sysbisp.length);
        Introspector.setBeanInfoSearchPath(allbisp);
        String[] syspesp = PropertyEditorManager.getEditorSearchPath();
        String[] nbpesp = new String[] {
            "org.netbeans.beaninfo.editors", // NOI18N
            "org.openide.explorer.propertysheet.editors", // NOI18N
        };
        String[] allpesp = new String[syspesp.length + nbpesp.length];
        System.arraycopy(nbpesp, 0, allpesp, 0, nbpesp.length);
        System.arraycopy(syspesp, 0, allpesp, nbpesp.length, syspesp.length);
        PropertyEditorManager.setEditorSearchPath(allpesp);
        PropertyEditorManager.registerEditor (java.lang.Character.TYPE, getKlass("org.netbeans.beaninfo.editors.CharEditor"));
        PropertyEditorManager.registerEditor(getKlass("[Ljava.lang.String;"), getKlass("org.netbeans.beaninfo.editors.StringArrayEditor")); // NOI18N
        // bugfix #28676, register editor for a property which type is array of data objects
        PropertyEditorManager.registerEditor(getKlass("[Lorg.openide.loaders.DataObject;"), getKlass("org.netbeans.beaninfo.editors.DataObjectArrayEditor")); // NOI18N
        StartLog.logProgress ("PropertyEditors registered"); // NOI18N

        // -----------------------------------------------------------------------------------------------------

        StatusDisplayer.getDefault().setStatusText (getString("MSG_IDEInit"));


        // -----------------------------------------------------------------------------------------------------
        // 7. Initialize FileSystems
        Repository.getDefault();
        StartLog.logProgress ("Repository initialized"); // NOI18N

        // -----------------------------------------------------------------------------------------------------
        // this indirectly sets system properties for proxy servers with values
        // taken from IDESettings
        SharedClassObject.findObject(IDESettings.class, true);
        StartLog.logProgress ("IDE settings loaded"); // NOI18N
         
        // -----------------------------------------------------------------------------------------------------
        // Upgrade
        try {
            boolean dontshowisw = false;
            File dontshowiswfile = new java.io.File(new java.io.File(System.getProperty("netbeans.user"), "system"), "dontshowisw");
            if (dontshowiswfile.exists()) {
                dontshowiswfile.delete();
                dontshowisw = true;
            }
            
            if ((System.getProperty ("netbeans.full.hack") == null) && (System.getProperty ("netbeans.close") == null) && !dontshowisw) {
                System.setProperty("import.canceled", "false"); // NOI18N
                
                
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        // Original code
                        //boolean canceled = org.netbeans.core.upgrade.UpgradeWizard.showWizard(getSplash());
                        //System.setProperty("import.canceled", canceled ? "true" : "false"); // NOI18N
                        
                        // Let's use reflection
                        File coreide = new InstalledFileLocatorImpl().locate("modules/core-ide.jar", null, false); // NOI18N
                        if (coreide != null) {
                            // This module is included in our distro somewhere... may or may not be turned on.
                            // Whatever - try running some classes from it anyway.
                            try {
                                // #30502: don't forget locale variants!
                                List urls = new ArrayList();
                                urls.add(Utilities.toURL(coreide));
                                File localeDir = new File(coreide.getParentFile(), "locale"); // NOI18N
                                if (localeDir.isDirectory()) {
                                    Iterator it = NbBundle.getLocalizingSuffixes();
                                    while (it.hasNext()) {
                                        String suffix = (String)it.next();
                                        File v = new File(localeDir, "core-ide" + suffix + ".jar"); // NOI18N
                                        if (v.isFile()) {
                                            urls.add(Utilities.toURL(v));
                                        }
                                    }
                                }
                                ClassLoader l = new URLClassLoader((URL[])urls.toArray(new URL[urls.size()]), NonGui.class.getClassLoader());
                                Class wizardClass = Class.forName("org.netbeans.core.upgrade.UpgradeWizard", true, l); // NOI18N
                                Method showMethod = wizardClass.getMethod( "showWizard", new Class[] { Splash.SplashOutput.class } ); // NOI18N

                                Boolean canceled = (Boolean)showMethod.invoke( null, new Object[] { getSplash() } );
                                System.setProperty("import.canceled", canceled.toString()); // NOI18N
                            } catch (Exception e) {
                                // If exceptions are thrown, notify them - something is broken.
                                e.printStackTrace();
                            } catch (LinkageError e) {
                                // These too...
                                e.printStackTrace();
                            }
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
        StartLog.logProgress ("Upgrade wizard consulted"); // NOI18N

        // -----------------------------------------------------------------------------------------------------
        // 9. Modules

        {
    	    StartLog.logStart ("Modules initialization"); // NOI18N

            getUserDir();
            File moduleDirHome = new File(homeDir, DIR_MODULES);
            File moduleDirUser;
            if (homeDir.equals(userDir)) {
                moduleDirUser = null;
            } else {
                moduleDirUser = new File(userDir, DIR_MODULES);
            }
            // #27151: ${netbeans.dirs}
            List extradirs = new ArrayList(5); // List<File>
            String nbdirs = System.getProperty("netbeans.dirs");
            if (nbdirs != null) {
                StringTokenizer tok = new StringTokenizer(nbdirs, File.pathSeparator);
                while (tok.hasMoreTokens()) {
                    extradirs.add(new File(tok.nextToken(), "modules")); // NOI18N
                }
            }
            try {
                moduleSystem = new ModuleSystem(Repository.getDefault().getDefaultFileSystem(), moduleDirHome, (File[])extradirs.toArray(new File[extradirs.size()]), moduleDirUser);
            } catch (IOException ioe) {
                // System will be screwed up.
                IllegalStateException ise = new IllegalStateException("Module system cannot be created"); // NOI18N
                ErrorManager.getDefault().annotate(ise, ioe);
                throw ise;
            }
    	    StartLog.logProgress ("ModuleSystem created"); // NOI18N

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
            LoaderPoolNode.load();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
        }
        try {
            ((TrivialProjectManager)Lookup.getDefault().lookup(TrivialProjectManager.class)).load();
        } catch (IOException e) {
            ErrorManager.getDefault().notify (ErrorManager.INFORMATIONAL, e);
        }
        StartLog.logProgress ("Project opened"); // NOI18N
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

        // set security manager
        SecurityManager secman = new TopSecurityManager();

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
        return NbBundle.getMessage(NonGui.class, resName);
    }

    /** Getter for a text from resource with one argument.
    * @param resName resource name
    * @return string with resource
    * @param arg the argument
    */
    static String getString (String resName, Object arg) {
        return NbBundle.getMessage(NonGui.class, resName, arg);
    }

    /** Getter for a text from resource with one argument.
    * @param resName resource name
    * @return string with resource
    * @param arg1 the argument
    * @param arg2 the argument
    */
    static String getString (String resName, Object arg1, Object arg2) {
        return NbBundle.getMessage(NonGui.class, resName, arg1, arg2);
    }

    /** Exits from the VM.
    */
    static void doExit (int code) {
        TopSecurityManager.exit(code);
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
