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

package org.netbeans.modules.extbrowser;

import java.beans.*;
import org.openide.ErrorManager;

import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Utilities;

/** Factory and descriptions for external browser
 */

public class ExtWebBrowser implements HtmlBrowser.Factory, java.io.Serializable, PropertyChangeListener {

    private static final long serialVersionUID = -3021027901671504127L;
        
    public static final String PROP_NAME = "name"; // NOI18N
    
    /** Browser executable property name */
    public static final String PROP_BROWSER_EXECUTABLE = "browserExecutable"; // NOI18N
    
    /** DDE server property name */
    public static final String PROP_DDESERVER = "dDEServer";   // NOI18N
    
//    /** Browser start timeout property name */
//    public static final String PROP_BROWSER_START_TIMEOUT = "browserStartTimeout";   // NOI18N

    /** DDE activate timeout property name */
    public static final String PROP_DDE_ACTIVATE_TIMEOUT = "activateTimeout";   // NOI18N
    
    /** DDE openURL timeout property name */
    public static final String PROP_DDE_OPENURL_TIMEOUT = "openurlTimeout";     // NOI18N
    
    /** Name of DDE server corresponding to Netscape Navigator 4.x */
    public static final String NETSCAPE = "NETSCAPE";   // NOI18N
    /** Name of DDE server corresponding to Internet Explorer */
    public static final String IEXPLORE = "IEXPLORE";   // NOI18N
    /** Name of DDE server corresponding to Mozilla */
    public static final String MOZILLA  = "MOZILLA";    // NOI18N
    /** Name of DDE server corresponding to Firefox */
    public static final String FIREFOX  = "FIREFOX";    // NOI18N
    /** Name of DDE server corresponding to Netscape 6.x */
    public static final String NETSCAPE6 = "NETSCAPE6";   // NOI18N
    
//    /** Default timeout for starting the browser */
//    protected static final int DEFAULT_BROWSER_START_TIMEOUT = 5000;
    
    /** Default for DDE activate timeout property */
    protected static final int DEFAULT_ACTIVATE_TIMEOUT = 2000;

    /** Default for DDE openURL timeout property */
    protected static final int DEFAULT_OPENURL_TIMEOUT = 3000;
    
    /** storage for DDE server property */
    protected String ddeServer;
    
    /** storage for DDE activate timeout property */
    protected int activateTimeout = DEFAULT_ACTIVATE_TIMEOUT;

//    /** storage for starting browser timeout property */
//    protected int browserStartTimeout = DEFAULT_BROWSER_START_TIMEOUT;

    /** storage for DDE openURL timeout property */
    protected int openurlTimeout = DEFAULT_OPENURL_TIMEOUT;

    /** Logger for extbrowser module. */
    private static ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.extbrowser");   // NOI18N
    
    protected String name;
    
    public static ErrorManager getEM () {
        return err;
    }
        
    /** Holds value of property browserExecutable. */
    protected NbProcessDescriptor browserExecutable;
    
    protected transient PropertyChangeSupport pcs;

    /** Creates new Browser */
    public ExtWebBrowser () {
        init();
    }

    /** initialize object */
    private void init () {
        if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
            err.log(ErrorManager.INFORMATIONAL, getClass().getName() + " " + System.currentTimeMillis() + "> init");
        }
        pcs = new PropertyChangeSupport(this);
        if (Utilities.isWindows()) {
            pcs.addPropertyChangeListener(this);
        }
    }

    /**
     * Gets DDE server name
     * @return server name of DDEserver.
     *         <CODE>null</CODE> when no server is selected (means default web browser).
     */
    public String getDDEServer () {
        return ddeServer;
    }
    
    /**
     * Sets DDE server name
     * @param ddeServer name of DDE server or <CODE>null</CODE>
     */
    public void setDDEServer (String ddeServer) {
        if ((ddeServer != null) && !ddeServer.equals(this.ddeServer)) {
            String old = this.ddeServer;
            this.ddeServer = ddeServer;
            pcs.firePropertyChange (PROP_DDESERVER, old, ddeServer);
            getEM().log("DDEServer changed to: " + ddeServer);                  // NOI18N
        }
    }
   
//    /** Getter for property browserStartTimeout.
//     * @return Value of property browserStartTimeout.
//     *
//     */
//    public int getBrowserStartTimeout() {
//        return browserStartTimeout;
//    }
//    
//    /** Setter for property browserStartTimeout.
//     * @param browserStartTimeout New value of property browserStartTimeout.
//     *
//     */
//    public void setBrowserStartTimeout(int browserStartTimeout) {
//        if (browserStartTimeout != this.browserStartTimeout) {
//            int oldVal = this.browserStartTimeout;
//            this.browserStartTimeout = browserStartTimeout;
//            pcs.firePropertyChange(PROP_BROWSER_START_TIMEOUT, oldVal, browserStartTimeout);
//        }
//    }
    
    /** Getter for property openurlTimeout.
     * @return Value of property openurlTimeout.
     *
     */
    public int getOpenurlTimeout() {
        return openurlTimeout;
    }
    
    /** Setter for property openurlTimeout.
     * @param openurlTimeout New value of property openurlTimeout.
     *
     */
    public void setOpenurlTimeout(int openurlTimeout) {
        if (openurlTimeout != this.openurlTimeout) {
            int oldVal = this.openurlTimeout;
            this.openurlTimeout = openurlTimeout;
            pcs.firePropertyChange(PROP_DDE_OPENURL_TIMEOUT, oldVal, openurlTimeout);
        }
    }
    
    /** Getter for property activeTimeout.
     * @return Value of property activeTimeout.
     *
     */
    public int getActivateTimeout() {
        return activateTimeout;
    }
    
    /** Setter for property activeTimeout.
     * @param activeTimeout New value of property activeTimeout.
     *
     */
    public void setActivateTimeout(int activateTimeout) {
        if (activateTimeout != this.activateTimeout) {
            int oldVal = this.activateTimeout;
            this.activateTimeout = activateTimeout;
            pcs.firePropertyChange(PROP_DDE_ACTIVATE_TIMEOUT, oldVal, activateTimeout);
        }
    }

    // getter for browser name - should be overriden in subclasses
    public String getName() {
        return name;
    }
    
    /** Setter for browser name
     */
    public void setName(String name) {
        if ((name != null) && (!name.equals(this.name))) {
            String oldVal = this.name;
            this.name = name;
            pcs.firePropertyChange(PROP_NAME, oldVal, name);
        }
    }
    
    /** Getter for property browserExecutable.
     * @return Value of property browserExecutable.
     */
    public NbProcessDescriptor getBrowserExecutable () {
        if (browserExecutable == null || "".equals(browserExecutable.getProcessName())) { // NOI18N
            return defaultBrowserExecutable();
        }
        return browserExecutable;
    }

    /** Setter for property browserExecutable.
     * @param browserExecutable New value of property browserExecutable.
     */
    public void setBrowserExecutable (NbProcessDescriptor browserExecutable) {
        if ((browserExecutable != null) && (!browserExecutable.equals(this.browserExecutable))) {
            NbProcessDescriptor oldVal = this.browserExecutable;
            this.browserExecutable = browserExecutable;
            pcs.firePropertyChange(PROP_BROWSER_EXECUTABLE, oldVal, browserExecutable);
        }
        if (browserExecutable == null) {
            NbProcessDescriptor oldVal = this.browserExecutable;
            this.browserExecutable = defaultBrowserExecutable();
            pcs.firePropertyChange(PROP_BROWSER_EXECUTABLE, oldVal, browserExecutable);
        }
    }

    /* when browserExecutable is changed, ddeServer has to be changed accordingly */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(ExtWebBrowser.PROP_BROWSER_EXECUTABLE)) {
            Object np = evt.getNewValue();
            if ((np != null) && (np instanceof NbProcessDescriptor)) {
                String processName = ((NbProcessDescriptor)np).getProcessName();
                if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    err.log(ErrorManager.INFORMATIONAL, "" + System.currentTimeMillis() + "> propertychange: " + processName);
                }
                if ((processName != null) && (processName.trim().length() > 0)) {
                    if (processName.toUpperCase().indexOf("IEXPLORE.EXE") > -1) {       // NOI18N
                        setDDEServer(IEXPLORE);
                    } else if (processName.toUpperCase().indexOf("MOZILLA.EXE") > -1) { // NOI18N
                        setDDEServer(MOZILLA);
                    } else if (processName.toUpperCase().indexOf("FIREFOX.EXE") > -1) { // NOI18N
                        setDDEServer(FIREFOX);
                    } else if (processName.toUpperCase().indexOf("NETSCP6.EXE") > -1) { // NOI18N
                        setDDEServer(NETSCAPE6);
                    } else if (processName.toUpperCase().indexOf("NETSCP.EXE") > -1) {  // NOI18N
                        setDDEServer(NETSCAPE6);
                    } else if (processName.toUpperCase().indexOf("NETSCAPE.EXE") > -1) { // NOI18N
                        setDDEServer(NETSCAPE);
                    } else {
                        setDDEServer(null);
                    }
                }
            }
        }
    }

    /** Default command for browser execution.
     *  Can be overriden to return browser that suits to platform and settings.
     *
     * @return netscape without any argument.
     */
    protected NbProcessDescriptor defaultBrowserExecutable () {
        String b = "netscape";  // NOI18N
        if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
            err.log(ErrorManager.INFORMATIONAL, "" + System.currentTimeMillis() + "> ExtBrowser: defaultBrowserExecutable: ");
        }
        if (Utilities.isWindows()) {
            b = "iexplore";                                             // NOI18N
            String params = "";                                         // NOI18N
            try {
                // finds HKEY_CLASSES_ROOT\\".html" and respective HKEY_CLASSES_ROOT\\<value>\\shell\\open\\command
                // we will ignore all params here
                b = NbDdeBrowserImpl.getDefaultOpenCommand ();
                String [] args = Utilities.parseParameters(b);

                if (args == null || args.length == 0) {
                    throw new NbBrowserException ();
                }
                b = args[0];
                if (args[0].toUpperCase().indexOf("IEXPLORE.EXE") > -1) {       // NOI18N
                    setDDEServer(IEXPLORE);
                    params = "-nohome ";                                         // NOI18N
                } else if (args[0].toUpperCase().indexOf("MOZILLA.EXE") > -1) { // NOI18N
                    setDDEServer(MOZILLA);
                } else if (args[0].toUpperCase().indexOf("FIREFOX.EXE") > -1) { // NOI18N
                    setDDEServer(FIREFOX);
                } else if (args[0].toUpperCase().indexOf("NETSCP6.EXE") > -1) { // NOI18N
                    setDDEServer(NETSCAPE6);
                } else if (args[0].toUpperCase().indexOf("NETSCP.EXE") > -1) {  // NOI18N
                    setDDEServer(NETSCAPE6);
                } else if (args[0].toUpperCase().indexOf("NETSCAPE.EXE") > -1) { // NOI18N
                    setDDEServer(NETSCAPE);
                }
                params += "{" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}";
                return new NbProcessDescriptor(b, params);
                
            } catch (NbBrowserException e) {
                try {
                    b = NbDdeBrowserImpl.getBrowserPath("IEXPLORE"); // NOI18N
                    if ((b != null) && (b.trim().length() > 0)) {
                        setDDEServer(IEXPLORE);
                        params += "{" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}";
                        return new NbProcessDescriptor(b, params);
                    }

                    b = NbDdeBrowserImpl.getBrowserPath("MOZILLA"); // NOI18N
                    if ((b != null) && (b.trim().length() > 0)) {
                        setDDEServer(MOZILLA);
                        params += "{" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}";
                        return new NbProcessDescriptor(b, params);
                    }

                    b = NbDdeBrowserImpl.getBrowserPath("FIREFOX"); // NOI18N
                    if ((b != null) && (b.trim().length() > 0)) {
                        setDDEServer(FIREFOX);
                        params += "{" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}";
                        return new NbProcessDescriptor(b, params);
                    }

                    b = NbDdeBrowserImpl.getBrowserPath("Netscp"); // NOI18N
                    if ((b != null) && (b.trim().length() > 0)) {
                        setDDEServer(NETSCAPE6);
                        params += "{" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}";
                        return new NbProcessDescriptor(b, params);
                    }
                    
                    b = NbDdeBrowserImpl.getBrowserPath("Netscp6"); // NOI18N
                    if ((b != null) && (b.trim().length() > 0)) {
                        setDDEServer(NETSCAPE6);
                        params += "{" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}";
                        return new NbProcessDescriptor(b, params);
                    }

                    b = NbDdeBrowserImpl.getBrowserPath("Netscape"); // NOI18N
                    if ((b != null) && (b.trim().length() > 0)) {
                        setDDEServer(NETSCAPE);
                        params += "{" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}";
                        return new NbProcessDescriptor(b, params);
                    }
                    
                } catch (NbBrowserException e2) {
                    setDDEServer(IEXPLORE);
                    b = "C:\\Program Files\\Internet Explorer\\iexplore.exe";     // NOI18N            
                }
            } catch (UnsatisfiedLinkError e) {
                // someone is customizing this on non-Win platform
                b = "iexplore";     // NOI18N
            }
            params += "{" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}";
            return new NbProcessDescriptor (b, params);

        // Unix but not MacOSX
        } else if (Utilities.isUnix() && (Utilities.getOperatingSystem() != Utilities.OS_MAC)) {
            
            // Linux -> Mozilla should be default
            if (Utilities.getOperatingSystem() == Utilities.OS_LINUX) {
                b = "mozilla";                                                    // NOI18N
                java.io.File f = new java.io.File ("/usr/local/mozilla/mozilla"); // NOI18N
                if (f.exists()) {
                    b = f.getAbsolutePath();
                } else {
                    f = new java.io.File ("/usr/bin/firefox"); // NOI18N
                    if (f.exists()) {
                        b = f.getAbsolutePath();
                    }
                }
            // Solaris -> Netscape should be default
            } else if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
                b = "netscape";                                                 // NOI18N
                java.io.File f = new java.io.File ("/usr/dt/bin/sun_netscape"); // NOI18N
                if (f.exists()) {
                    b = f.getAbsolutePath();
                }
            }

            return new NbProcessDescriptor( b,
                "-remote \"openURL({" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "})\"", // NOI18N
                NbBundle.getMessage(ExtWebBrowser.class, "MSG_BrowserExecutorHint")
            );
            
        // OS/2
        } else if (Utilities.getOperatingSystem () == Utilities.OS_OS2) {
            return new NbProcessDescriptor(
                "Netscape.exe", // NOI18N
                // {URL}
                " {" + UnixBrowserFormat.TAG_URL + "}", // NOI18N
                NbBundle.getBundle(ExtWebBrowser.class).getString("MSG_BrowserExecutorHint")
            );
            
        // Mac OS
        } else if (Utilities.getOperatingSystem () == Utilities.OS_MAC) {
            return new NbProcessDescriptor(
                "/usr/bin/open", // NOI18N
                // {URL}
                " {" + UnixBrowserFormat.TAG_URL + "}", // NOI18N
                NbBundle.getBundle(ExtWebBrowser.class).getString("MSG_BrowserExecutorHint")
            );
            
        // Other
        } else {
            return new NbProcessDescriptor(
                // empty string for process
                "", // NOI18N
                // {URL}
                " {" + UnixBrowserFormat.TAG_URL + "}", // NOI18N
                NbBundle.getBundle(ExtWebBrowser.class).getString("MSG_BrowserExecutorHint")
            );
        }
    }

    /**
     * Returns a new instance of BrowserImpl implementation.
     */
    public HtmlBrowser.Impl createHtmlBrowserImpl() {
        return new DelegatingWebBrowserImpl(this);
    }

    /**
     * @param l new PropertyChangeListener */    
    public void addPropertyChangeListener (PropertyChangeListener l) {
        if (pcs == null) {
            pcs = new PropertyChangeSupport(this);
        }
        pcs.addPropertyChangeListener (l);
    }
    
    /**
     * @param l PropertyChangeListener to be removed */    
    public void removePropertyChangeListener (PropertyChangeListener l) {
        if (pcs != null) {
            pcs.removePropertyChangeListener (l);
        }
    }
    
    private void readObject (java.io.ObjectInputStream ois) 
    throws java.io.IOException, ClassNotFoundException {
        ois.defaultReadObject ();
        if (browserExecutable != null && browserExecutable.getArguments() != null) {
            // replace old {params} with {URL}
            String args = browserExecutable.getArguments();
            int idx = args.indexOf("{params}"); // NOI18N
            if (idx >= 0) {
                browserExecutable = new NbProcessDescriptor (
                    browserExecutable.getProcessName(),
                    args.substring(0, idx)+"-remote \"openURL({URL})"+args.substring(idx+8), // NOI18N
                    NbBundle.getMessage (ExtWebBrowser.class, "MSG_BrowserExecutorHint")
                );
            }
        }
        init();
    }

    /** Default format that can format tags related to execution. 
     * Currently this is only the URL.
     */
    public static class UnixBrowserFormat extends org.openide.util.MapFormat {
        
        /** SVUID for serialization. */
        private static final long serialVersionUID = -699340388834127437L;
        
        /** Tag used to pass URL */
        public static final String TAG_URL = "URL"; // NOI18N
        
        /** Creates UnixBrowserFormat for URL.
         * @param url to specify URL
         */
        public UnixBrowserFormat (String url) {
            super(new java.util.HashMap ());
            java.util.Map map = getMap ();        
            map.put (TAG_URL, url);
        }    
    }
    
}
