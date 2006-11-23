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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 *
 * @author Jiri Rechtacek
 */
public class ProxySettings {
    
    public static final String PROXY_HTTP_HOST = "proxyHttpHost";
    public static final String PROXY_HTTP_PORT = "proxyHttpPort";
    public static final String PROXY_HTTPS_HOST = "proxyHttpsHost";
    public static final String PROXY_HTTPS_PORT = "proxyHttpsPort";
    public static final String PROXY_SOCKS_HOST = "proxySocksHost";
    public static final String PROXY_SOCKS_PORT = "proxySocksPort";
    public static final String NOT_PROXY_HOSTS = "proxyNonProxyHosts";
    public static final String PROXY_TYPE = "proxyType";
    public static final String USE_PROXY_AUTHENTICATION = "useProxyAuthentication";
    public static final String PROXY_AUTHENTICATION_USERNAME = "proxyAuthenticationUsername";
    public static final String PROXY_AUTHENTICATION_PASSWORD = "proxyAuthenticationPassword";
    
    // XXX Old stuff
    /** proxy host VM property key */
    public static final String KEY_PROXY_HOST = "http.proxyHost"; // NOI18N
    /** proxy port VM property key */
    public static final String KEY_PROXY_PORT = "http.proxyPort"; // NOI18N
    /** non proxy hosts VM property key */
    public static final String KEY_NON_PROXY_HOSTS = "http.nonProxyHosts"; // NOI18N
    /** https proxy host VM property key */
    public static final String KEY_HTTPS_PROXY_HOST = "https.proxyHost"; // NOI18N
    /** https proxy port VM property key */
    public static final String KEY_HTTPS_PROXY_PORT = "https.proxyPort"; // NOI18N    

    private static String presetNonProxyHosts;
    private static String defaultUserNonProxyHosts;
    
    /** No proxy is used to connect. */
    public static final int DIRECT_CONNECTION = 0;
    
    /** Proxy setting is automaticaly detect in OS. */
    public static final int AUTO_DETECT_PROXY = 1; // as default
    
    /** Manualy set proxy host and port. */
    public static final int MANUAL_SET_PROXY = 2;
    
    {
        presetNonProxyHosts = System.getProperty (KEY_NON_PROXY_HOSTS, "");
        defaultUserNonProxyHosts = getModifiedNonProxyHosts (getSystemNonProxyHosts ());
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.forModule (ProxySettings.class);
    }
    
    public static String getHttpHost () {
        return getPreferences ().get (PROXY_HTTP_HOST, "");
    }
    
    public static String getHttpPort () {
        return getPreferences ().get (PROXY_HTTP_PORT, "");
    }
    
    public static String getHttpsHost () {
        return getPreferences ().get (PROXY_HTTPS_HOST, "");
    }
    
    public static String getHttpsPort () {
        return getPreferences ().get (PROXY_HTTPS_PORT, "");
    }
    
    public static String getSocksHost () {
        return getPreferences ().get (PROXY_SOCKS_HOST, "");
    }
    
    public static String getSocksPort () {
        return getPreferences ().get (PROXY_SOCKS_PORT, "");
    }
    
    public static String getNonProxyHosts () {
        return getPreferences ().get (NOT_PROXY_HOSTS, defaultUserNonProxyHosts);
    }
    
    public static int getProxyType () {
        return getPreferences ().getInt (PROXY_TYPE, AUTO_DETECT_PROXY);
    }
    
    public static boolean useAuthentication () {
        return getPreferences ().getBoolean (USE_PROXY_AUTHENTICATION, false);
    }
    
    public static String getAuthenticationUsername () {
        return getPreferences ().get (PROXY_AUTHENTICATION_USERNAME, "");
    }
    
    public static char[] getAuthenticationPassword () {
        return getPreferences ().get (PROXY_AUTHENTICATION_PASSWORD, "").toCharArray ();
    }
    
    static void addPreferenceChangeListener (PreferenceChangeListener l) {
        getPreferences ().addPreferenceChangeListener (l);
    }
    
    static void removePreferenceChangeListener (PreferenceChangeListener l) {
        getPreferences ().removePreferenceChangeListener (l);
    }
    
    static class SystemProxySettings extends ProxySettings {
        
        public static String getHttpHost () {
            if (isSystemProxyDetect ()) {
                return getSystemProxyHost ();
            } else {
                return "";
            }
        }

        public static String getHttpPort () {
            if (isSystemProxyDetect ()) {
                return getSystemProxyPort ();
            } else {
                return "";
            }
        }

        public static String getHttpsHost () {
            if (isSystemProxyDetect ()) {
                return getSystemProxyHost ();
            } else {
                return "";
            }
        }

        public static String getHttpsPort () {
            if (isSystemProxyDetect ()) {
                return getSystemProxyPort ();
            } else {
                return "";
            }
        }

        public static String getSocksHost () {
            if (isSystemSocksServerDetect ()) {
                return getSystemSocksServerHost ();
            } else {
                return "";
            }
        }

        public static String getSocksPort () {
            if (isSystemSocksServerDetect ()) {
                return getSystemSocksServerPort ();
            } else {
                return "";
            }
        }

        public static String getNonProxyHosts () {
            return getModifiedNonProxyHosts (getSystemNonProxyHosts ());
        }

        // helper methods
        private static boolean isSystemProxyDetect () {
            return System.getProperty ("netbeans.system_http_proxy") != null; // NOI18N
        }

        private static String getSystemProxyHost () {
            String systemProxy = System.getProperty ("netbeans.system_http_proxy"); // NOI18N
            if (systemProxy == null) {
                return ""; // NOI18N
            }

            int i = systemProxy.lastIndexOf (":"); // NOI18N
            if (i <= 0 || i >= systemProxy.length () - 1) {
                return ""; // NOI18N
            }

            return systemProxy.substring (0, i);
        }

        private static String getSystemProxyPort () {
            String systemProxy = System.getProperty ("netbeans.system_http_proxy"); // NOI18N
            if (systemProxy == null) {
                return ""; // NOI18N
             }

            int i = systemProxy.lastIndexOf (":"); // NOI18N
            if (i <= 0 || i >= systemProxy.length () - 1) {
                return ""; // NOI18N
            }

            return systemProxy.substring (i+1);
        }

        private static boolean isSystemSocksServerDetect () {
            // no SOCKS detection yet
            return false;
        }
        
        private static String getSystemSocksServerHost () {
            assert false : "SOCKS Server cannot be detected yet.";
            return "";
        }

        private static String getSystemSocksServerPort () {
            assert false : "SOCKS Server cannot be detected yet.";
            return "";
        }

    }

    private static String getSystemNonProxyHosts () {
        String systemProxy = System.getProperty ("netbeans.system_http_non_proxy_hosts"); // NOI18N

        return systemProxy == null ? "" : systemProxy;
    }

    private static String getModifiedNonProxyHosts (String systemPreset) {
        String fromSystem = systemPreset.replaceAll (";", "|").replaceAll (",", "|"); //NOI18N
        String fromUser = presetNonProxyHosts == null ? "" : presetNonProxyHosts.replaceAll (";", "|").replaceAll (",", "|"); //NOI18N
        if (Utilities.isWindows ()) {
            fromSystem = addReguralToNonProxyHosts (fromSystem);
        }
        String nonProxy = fromUser + (fromUser.length () == 0 ? "" : "|") + fromSystem + (fromSystem.length () == 0 ? "" : "|") + "localhost|127.0.0.1"; // NOI18N
        String localhost = ""; // NOI18N
        try {
            localhost = InetAddress.getLocalHost().getHostName();
            if (!"localhost".equals(localhost)) { // NOI18N
                nonProxy = nonProxy + "|" + localhost; // NOI18N
            } else {
                // Avoid this error when hostname == localhost:
                // Error in http.nonProxyHosts system property:  sun.misc.REException: localhost is a duplicate
            }
        }
        catch (UnknownHostException e) {
            // OK. Sometimes a hostname is assigned by DNS, but a computer
            // is later pulled off the network. It may then produce a bogus
            // name for itself which can't actually be resolved. Normally
            // "localhost" is aliased to 127.0.0.1 anyway.
        }
        /* per Milan's agreement it's removed. See issue #89868
        try {
            String localhost2 = InetAddress.getLocalHost().getCanonicalHostName();
            if (!"localhost".equals(localhost2) && !localhost2.equals(localhost)) { // NOI18N
                nonProxy = nonProxy + "|" + localhost2; // NOI18N
            } else {
                // Avoid this error when hostname == localhost:
                // Error in http.nonProxyHosts system property:  sun.misc.REException: localhost is a duplicate
            }
        }
        catch (UnknownHostException e) {
            // OK. Sometimes a hostname is assigned by DNS, but a computer
            // is later pulled off the network. It may then produce a bogus
            // name for itself which can't actually be resolved. Normally
            // "localhost" is aliased to 127.0.0.1 anyway.
        }
         */
        return compactNonProxyHosts (nonProxy);
    }


    // avoid duplicate hosts
    private static String compactNonProxyHosts (String nonProxyHost) {
        StringTokenizer st = new StringTokenizer (nonProxyHost, "|"); //NOI18N
        Set<String> s = new HashSet<String> (); 
        StringBuilder compactedProxyHosts = new StringBuilder();
        while (st.hasMoreTokens ()) {
            String t = st.nextToken ();
            if (s.add (t.toLowerCase (Locale.US))) {
                if (compactedProxyHosts.length() > 0)
                    compactedProxyHosts.append('|');
                compactedProxyHosts.append(t);
            }
        }
        return compactedProxyHosts.toString();
    }

    private static String addReguralToNonProxyHosts (String nonProxyHost) {
        StringTokenizer st = new StringTokenizer (nonProxyHost, "|");
        StringBuilder reguralProxyHosts = new StringBuilder();
        while (st.hasMoreTokens ()) {
            String t = st.nextToken ();
            if (t.indexOf ('*') == -1) { //NOI18N
                t = t + '*'; //NOI18N
            }
            if (reguralProxyHosts.length() > 0) 
                reguralProxyHosts.append('|');
            reguralProxyHosts.append(t);
        }

        return reguralProxyHosts.toString();
    }

}
