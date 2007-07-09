/*
 * SvnConfigFilesTest.java
 * JUnit based test
 *
 * Created on June 26, 2007, 3:50 PM
 */

package org.netbeans.modules.subversion.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.URI;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import org.ini4j.Ini;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.subversion.util.ProxySettings;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.netbeans.modules.subversion.TestKit;

/**
 *
 * @author Peter Pis
 */
public class SvnConfigFilesTest extends NbTestCase {

    private static String SYSTEM_PROXY_HOST = "system.cache.org";
    private static String SYSTEM_PROXY_PORT = "777";
    private static String USER_PROXY_HOST = "my.webcache";
    private static String USER_PROXY_PORT = "8080";

    private Preferences proxyPreferences;
    private ProxySelector selector;
    
    String svnUserPath = "";
    String svnNbPath = "";
    String svnGoldenPath = "";

    public SvnConfigFilesTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        System.setProperty("netbeans.t9y.svn.user.config.path", "");
        System.setProperty("netbeans.t9y.svn.nb.config.path", "");
    }

    public void testSubversionConfig() {
        String[] wordsActual = {""};
        String[] wordsExpected = {""};
        String[] proxy = {"my.proxy", "my.proxy", "my.proxy", "", ""};
        int result = -1;
        
        for (int i = 1; i < proxy.length + 1; i++) {
            //changeSvnConfigLocation("svn" + i, "golden" + i, "my.proxy", "8080");
            changeSvnConfigLocation("svn" + i, "golden" + i, proxy[i-1], "8080");
            String generatedConfig = getContent(svnNbPath + java.io.File.separator + "config");
            String generatedServers = getContent(svnNbPath + java.io.File.separator + "servers");
            String goldenConfig = getContent(svnGoldenPath + java.io.File.separator + "config");
            String goldenServers = getContent(svnGoldenPath + java.io.File.separator + "servers");

            //Compare and verify
            //config file
            Pattern p = Pattern.compile("(\\s)+");
            wordsActual = p.split(generatedConfig);
            //printArray(wordsActual);
            wordsExpected = p.split(goldenConfig);
            //printArray(wordsExpected);
            result = org.netbeans.modules.subversion.TestKit.compareThem(wordsExpected, wordsActual, false);
            assertEquals(wordsExpected.length, result);
            System.out.println("Config ok!");

            //servers file
            wordsActual = p.split(generatedServers);
            //printArray(wordsActual);
            wordsExpected = p.split(goldenServers);
            //printArray(wordsExpected);
            result = org.netbeans.modules.subversion.TestKit.compareThem(wordsExpected, wordsActual, false);
            assertEquals(wordsExpected.length, result);
            System.out.println("Servers ok!");            
        } 
    } /* Test of getNBConfigPath method, of class SvnConfigFiles. */

    /*
     * Method changes svn config files location
     * @param source defines the directory that contain test subversion 'config' and 'servers' file of DATA folder
     * @param proxyHost defines a proxy host
     * @param proxyPort defines a proxy port
     *
     */
    private void changeSvnConfigLocation(String source, String golden, String proxyHost, String proxyPort) {
        //set svn user config path - DATA test folder + source
        try {
            svnUserPath = getDataDir().getCanonicalPath() + File.separator + "subversion" + File.separator + source;
            svnGoldenPath = getDataDir().getCanonicalPath() + File.separator + "subversion" + File.separator + golden;
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.setProperty("netbeans.t9y.svn.user.config.path", svnUserPath);

        //create temporary location for svn config files generated by the IDE
        svnNbPath = "/tmp" + File.separator + "svn" + File.separator + "config" + System.currentTimeMillis();
        //System.out.println("work: " + svnNbPath);
        final File tmp = new File(svnNbPath);
        
        Thread t = new Thread(new Runnable() {
           public void run() {
            tmp.mkdirs();
                while(!tmp.isDirectory()) {
                    try {
                        Thread.currentThread().sleep(1000);
                    } catch (InterruptedException e) {}    
               }
           } 
        });
        t.start();
        try {
            t.join(3000);
        } catch (InterruptedException e) {
            
        }
        //tmp.deleteOnExit();
        System.setProperty("netbeans.t9y.svn.nb.config.path", svnNbPath);

        //Proxy
        setProxy(proxyHost, proxyPort);
        Preferences pref = org.openide.util.NbPreferences.root().node("org/netbeans/core");
        SvnConfigFiles scf = SvnConfigFiles.getInstance();
        try {
            if (proxyHost.length() == 0 || proxyPort.length() == 0) {
                proxyPreferences.putInt("proxyType", 0);
            } else {
                proxyPreferences.putInt("proxyType", 2);
            }    
            scf.setProxy(new SVNUrl("http://peterp.czech.sun.com/svn"));
        } catch (MalformedURLException me) {
        }

    }

    private void setProxy(String proxyHost, String proxyPort) {
        System.setProperty("netbeans.system_http_proxy", proxyHost + ":" + proxyPort);
        System.setProperty("netbeans.system_socks_proxy", proxyHost + ":" + proxyPort);
        System.setProperty("netbeans.system_http_non_proxy_hosts", "*.other.org");
        System.setProperty("http.nonProxyHosts", "*.netbeans.org");
        selector = ProxySelector.getDefault();
        proxyPreferences = NbPreferences.root().node("/org/netbeans/core");
        proxyPreferences.put("proxyHttpHost", proxyHost);
        proxyPreferences.put("proxyHttpPort", proxyPort);
        proxyPreferences.put("proxySocksHost", proxyHost);
        proxyPreferences.put("proxySocksPort", proxyPort);
    }

    private String getContent(String fileName) {
        StringBuffer content = new StringBuffer("");
        BufferedReader br = null;
        
        try {
            br = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
                content.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }    
            }
        }
        
        return content.toString();
    }
    
    public void printArray(String[] array) {
        System.out.println("=== " + array.length + " ===");
        for (String string : array) {
            System.out.println(string);
        }
        System.out.println("===");
    }
}
