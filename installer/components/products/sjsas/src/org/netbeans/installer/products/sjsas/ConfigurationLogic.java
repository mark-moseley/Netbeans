/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */
package org.netbeans.installer.products.sjsas;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.applications.GlassFishUtils;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.components.ProductConfigurationLogic;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.helper.ShortcutLocationType;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.netbeans.installer.wizard.components.panels.JdkLocationPanel;
import org.netbeans.installer.products.sjsas.wizard.panels.ASPanel;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.helper.Shortcut;

/**
 *
 * @author Kirill Sorokin
 */
public class ConfigurationLogic extends ProductConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private List<WizardComponent> wizardComponents;
    
    // constructor //////////////////////////////////////////////////////////////////
    public ConfigurationLogic() throws InitializationException {
        wizardComponents = Wizard.loadWizardComponents(
                WIZARD_COMPONENTS_URI,
                getClass().getClassLoader());
    }
    
    // configuration logic implementation ///////////////////////////////////////////
    public void install(final Progress progress)
    throws InstallationException {
        final File directory = getProduct().getInstallationLocation();
        
        final String username  = getProperty(ASPanel.USERNAME_PROPERTY);
        final String password  = getProperty(ASPanel.PASSWORD_PROPERTY);
        final String httpPort  = getProperty(ASPanel.HTTP_PORT_PROPERTY);
        final String httpsPort = getProperty(ASPanel.HTTPS_PORT_PROPERTY);
        final String adminPort = getProperty(ASPanel.ADMIN_PORT_PROPERTY);
        
        final File javaHome =
                new File(getProperty(JdkLocationPanel.JDK_LOCATION_PROPERTY));
        
        final FilesList list = getProduct().getInstalledFiles();
        
        /////////////////////////////////////////////////////////////////////////////
        
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.replace.tokens")); // NOI18N
            
            final Map<String, Object> map = new HashMap<String, Object>();
            
            map.put(INSTALL_HOME_TOKEN, directory);
            map.put(INSTALL_HOME_F_TOKEN, directory.getPath().replace(StringUtils.BACK_SLASH, StringUtils.FORWARD_SLASH));
            
            map.put(JAVA_HOME_TOKEN,  javaHome);
            map.put(JAVA_HOME_F_TOKEN, javaHome.getPath().replace(StringUtils.BACK_SLASH, StringUtils.FORWARD_SLASH));
            
            map.put(HOST_NAME_TOKEN,  SystemUtils.getHostName());
            map.put(ADMIN_USERNAME_TOKEN, username);
            map.put(HTTP_PORT_TOKEN,httpPort);
            map.put(ADMIN_PORT_TOKEN,adminPort);
            
            FileUtils.modifyFile(new File(directory, BIN_SUBDIR),map);
            FileUtils.modifyFile(new File(directory, CONFIG_SUBDIR),map);
            FileUtils.modifyFile(new File(directory, DOCS_SUBDIR),map);
            FileUtils.modifyFile(new File(directory, IMQ_SUBDIR), map);
            FileUtils.modifyFile(new File(directory, JBI_SUBDIR),map);
            FileUtils.modifyFile(new File(directory, DERBY_SUBDIR),map);
            FileUtils.modifyFile(new File(directory, SAMPLES_SUBDIR), map);
            FileUtils.modifyFile(new File(directory, BLUEPRINTS_SUBDIR), map);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.replace.tokens"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.irrelevant.files")); // NOI18N
            
            SystemUtils.removeIrrelevantFiles(directory);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.irrelevant.files"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.files.permissions")); // NOI18N
            
            SystemUtils.correctFilesPermissions(directory);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.files.permissions"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.create.domain")); // NOI18N
            
            GlassFishUtils.createDomain(
                    directory,
                    DOMAIN_NAME,
                    username,
                    password,
                    httpPort,
                    httpsPort,
                    adminPort);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.create.domain"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.extra.files")); // NOI18N
            list.add(new File(directory, DERBY_LOG));
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.extra.files"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.ide.integration")); // NOI18N
            
            final List<Product> ides =
                    Registry.getInstance().getProducts("nb-base");
            for (Product ide: ides) {
                if (ide.getStatus() == Status.INSTALLED) {
                    final File nbLocation = ide.getInstallationLocation();
                    
                    if (nbLocation != null) {
                        NetBeansUtils.setJvmOption(
                                nbLocation,
                                JVM_OPTION_NAME,
                                directory.getAbsolutePath(),
                                true);
                    }
                }
            }
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.ide.integration"),  // NOI18N
                    e);
        }
        try {
            progress.setDetail(getString("CL.install.shortcuts.creation")); // NOI18N
            list.add(modifyASLauncherFiles(directory, true));
        } catch (NativeException e) {
            throw new InstallationException(
                    getString("CL.install.error.shortcuts.creation"),  // NOI18N
                    e);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.shortcuts.creation"),  // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }
    
    public void uninstall(final Progress progress)
    throws UninstallationException {
        File directory = getProduct().getInstallationLocation();
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            try {
                progress.setDetail(getString("CL.uninstall.shortcuts.delete")); // NOI18N
                modifyASLauncherFiles(directory, false);
            } catch (NativeException e) {
                throw new UninstallationException(
                        getString("CL.uninstall.error.shortcuts.delete"),  // NOI18N
                        e);
            }
            
            progress.setDetail(getString("CL.uninstall.ide.integration")); // NOI18N
            
            final List<Product> ides =
                    Registry.getInstance().getProducts("nb-base");
            for (Product ide: ides) {
                if (ide.getStatus() == Status.INSTALLED) {
                    final File nbLocation = ide.getInstallationLocation();
                    
                    if (nbLocation != null) {
                        final String value = NetBeansUtils.getJvmOption(
                                nbLocation,
                                JVM_OPTION_NAME);
                        
                        if ((value != null) &&
                                (value.equals(directory.getAbsolutePath()))) {
                            NetBeansUtils.removeJvmOption(
                                    nbLocation,
                                    JVM_OPTION_NAME);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new UninstallationException(
                    getString("CL.uninstall.error.ide.integration"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.uninstall.delete.domain")); // NOI18N
            
            GlassFishUtils.deleteDomain(directory, DOMAIN_NAME);
        } catch (IOException e) {
            throw new UninstallationException(
                    getString("CL.uninstall.error.delete.domain"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }
    
    public List<WizardComponent> getWizardComponents() {
        return wizardComponents;
    }
    
    @Override
    public boolean allowModifyMode() {
        return false;
    }
    
    private FilesList modifyASLauncherFiles(File location, boolean isCreate) throws NativeException {
        FilesList list = new FilesList();
        try {
            LogManager.log("creating the application server launchers");//NOI18N
            File asadminLocation = GlassFishUtils.getAsadmin(location);
            
            File launchersDir     = new File(location, BIN_SUBDIR);
            
            File javaeeDocsFile   = new File(launchersDir, LAUNCHER_JAVAEE_DOCS + EXEC_EXT);
            File adminConsoleFile = new File(launchersDir, LAUNCHER_ADMIN_CONSOLE + EXEC_EXT);
            File startDomainFile  = new File(launchersDir, LAUNCHER_START_DOMAIN + EXEC_EXT);
            File startDerbyFile   = new File(launchersDir, LAUNCHER_START_DB + EXEC_EXT);
            File stopDomainFile   = new File(launchersDir, LAUNCHER_STOP_DOMAIN + EXEC_EXT);
            File stopDerbyFile    = new File(launchersDir, LAUNCHER_STOP_DB + EXEC_EXT);
            
            File aboutFile        = new File(location, AS_ABOUT_LOCATION);
            File samplesFile      = new File(location, AS_SAMPLES_LOCATION);
            File quickStartFile   = new File(location, AS_QUICK_START_LOCATION);
            
            if(isCreate) {
                String execCommandStub = (SystemUtils.isWindows()) ?
                    BAT_CALLER_STUB :
                    SHELL_SCRIPT_STUB;
                
                String runUrlStub = null;
                runUrlStub = SystemUtils.isWindows() ? BAT_STARTER_STUB :
                    (SystemUtils.isMacOS() ? SHELL_SCRIPT_BROWSER_STUB_MACOS :
                        SHELL_SCRIPT_BROWSER_STUB_UNIX);
                
                list.add(FileUtils.writeFile(javaeeDocsFile,
                        StringUtils.format(runUrlStub, JAVAEE_DOCS_URL)));
                
                String adminConsoleUrl = StringUtils.format(ADMIN_CONSOLE_URL,
                        LOCALHOST,
                        getProperty(ASPanel.ADMIN_PORT_PROPERTY));
                
                list.add(FileUtils.writeFile(adminConsoleFile,
                        StringUtils.format(runUrlStub, adminConsoleUrl)));
                
                String startDomainContents = StringUtils.format(
                        execCommandStub,
                        StringUtils.joinCommand(
                        asadminLocation.getAbsolutePath(),
                        GlassFishUtils.START_DOMAIN_COMMAND,
                        DOMAIN_NAME));
                
                
                list.add(FileUtils.writeFile(startDomainFile, startDomainContents));
                
                String startDbContents = StringUtils.format(
                        execCommandStub,
                        StringUtils.joinCommand(
                        asadminLocation.getAbsolutePath(),
                        GlassFishUtils.START_DATABASE_COMMAND));
                
                list.add(FileUtils.writeFile(startDerbyFile, startDbContents));
                
                String stopDomainContents = StringUtils.format(
                        execCommandStub,
                        StringUtils.joinCommand(
                        asadminLocation.getAbsolutePath(),
                        GlassFishUtils.STOP_DOMAIN_COMMAND,
                        DOMAIN_NAME));
                
                list.add(FileUtils.writeFile(stopDomainFile, stopDomainContents));
                
                
                String stopDbContents = StringUtils.format(
                        execCommandStub,
                        StringUtils.joinCommand(
                        asadminLocation.getAbsolutePath(),
                        GlassFishUtils.STOP_DATABASE_COMMAND));
                
                list.add(FileUtils.writeFile(stopDerbyFile, stopDbContents));
                
                SystemUtils.correctFilesPermissions(launchersDir);
            }
            
            if(SystemUtils.isWindows()) {
                modifyShortcut(getIconShortcut(adminConsoleFile, AS_ICON_ADMIN_CONSOLE, AS_ADMIN_CONSOLE_NAME, location),
                        isCreate);
                modifyShortcut(getIconShortcut(javaeeDocsFile, AS_ICON_JAVAEE_DOCS, AS_JAVAEE_DOCS_NAME, location),
                        isCreate);
                modifyShortcut(getIconShortcut(quickStartFile, AS_ICON_QUICK_START, AS_QUICK_START_NAME, location),
                        isCreate);
                modifyShortcut(getIconShortcut(startDomainFile, AS_ICON_START_DOMAIN, AS_START_DOMAIN_NAME, location),
                        isCreate);
                modifyShortcut(getIconShortcut(startDerbyFile, AS_ICON_START_DERBY, AS_START_DERBY_NAME, location),
                        isCreate);
                modifyShortcut(getIconShortcut(stopDomainFile, AS_ICON_STOP_DOMAIN, AS_STOP_DOMAIN_NAME, location),
                        isCreate);
                modifyShortcut(getIconShortcut(stopDerbyFile, AS_ICON_STOP_DERBY, AS_STOP_DERBY_NAME, location),
                        isCreate);
                modifyShortcut(getIconShortcut(samplesFile, AS_ICON_SAMPLES, AS_SAMPLES_NAME, location),
                        isCreate);
                modifyShortcut(getIconShortcut(aboutFile, AS_ICON_ABOUT, AS_ABOUT_NAME, location),
                        isCreate);
            }
        } catch (IOException e) {
            throw new NativeException("Can`t create application server launchers", e);
        } catch (NativeException e) {
            throw new NativeException("Can`t create application server launchers", e);
        }
        return list;
    }
    private void modifyShortcut(Shortcut shortcut, boolean create) throws NativeException {
        if(create) {
            SystemUtils.createShortcut(shortcut, ShortcutLocationType.CURRENT_USER_START_MENU);
        } else {
            SystemUtils.removeShortcut(shortcut, ShortcutLocationType.CURRENT_USER_START_MENU, true);
        }
    }
    private Shortcut getIconShortcut(File file, String icon, String resourceName, File asLocation) {
        Shortcut shortcut = new Shortcut(
                getString(resourceName), file);
        shortcut.setIcon(new File(asLocation,
                ICONS_SUBDIR + SystemUtils.getFileSeparator() + icon));
        shortcut.setWorkingDirectory(asLocation);
        shortcut.setRelativePath(getString(AS_RELATIVE_SHORTCUT_LOCATION));
        return shortcut;
    }
    
/////////////////////////////////////////////////////////////////////////////////
// Constants
    public static final String WIZARD_COMPONENTS_URI =
            FileProxy.RESOURCE_SCHEME_PREFIX +
            "org/netbeans/installer/products/sjsas/wizard.xml"; // NOI18N
    public static final String DOMAIN_NAME =
            "domain1"; // NOI18N
    public static final String CONFIG_SUBDIR =
            "config"; // NOI18N
    public static final String LIB_SUBDIR =
            "lib"; // NOI18N
    public static final String IMQ_SUBDIR =
            "imq"; // NOI18N
    public static final String DOMAINS_SUBDIR =
            "domains"; // NOI18N
    public static final String DERBY_SUBDIR =
            "javadb"; // NOI18N
    public static final String UC_INSTALL_HOME_SUBDIR =
            "updatecenter"; //NOI18N
    public static final String BIN_SUBDIR =
            "bin"; // NOI18N
    public static final String DOCS_SUBDIR =
            "docs"; // NOI18N
    public static final String JBI_SUBDIR =
            "jbi"; // NOI18N
    public static final String SAMPLES_SUBDIR =
            "samples"; // NOI18N
    public static final String BLUEPRINTS_SUBDIR =
            "blueprints"; // NOI18N
    public static final String ICONS_SUBDIR =
            "icons"; // NOI18N
    
    public static final String INSTALL_HOME_TOKEN =
            "%INSTALL_HOME%"; // NOI18N
    public static final String INSTALL_HOME_F_TOKEN =
            "%INSTALL_HOME_F%"; // NOI18N
    public static final String JAVA_HOME_TOKEN =
            "%JAVA_HOME%"; // NOI18N
    public static final String JAVA_HOME_F_TOKEN =
            "%JAVA_HOME_F%"; // NOI18N
    public static final String HTTP_PORT_TOKEN =
            "%HTTP_PORT%"; //NOI18N
    public static final String ADMIN_PORT_TOKEN =
            "%ADMIN_PORT%"; //NOI18N
    public static final String HOST_NAME_TOKEN =
            "%HOST_NAME%";//N0I18N
    public static final String ADMIN_USERNAME_TOKEN =
            "%ADMIN_USER_NAME%";//N0I18N
    
    public static final String DERBY_LOG =
            "derby.log"; // NOI18N
    
    private static final String EXEC_EXT =
            SystemUtils.isWindows() ? ".bat" :
                (SystemUtils.isMacOS() ? ".command" : ".sh");
    public static final String LAUNCHER_JAVAEE_DOCS   =
            "javaee-docs"; //NOI18N
    public static final String LAUNCHER_STOP_DOMAIN   =
            "stop-default-domain"; //NOI18N
    public static final String LAUNCHER_START_DOMAIN  =
            "start-default-domain"; //NOI18N
    public static final String LAUNCHER_START_DB      =
            "start-derby"; //NOI18N
    public static final String LAUNCHER_STOP_DB       =
            "stop-derby"; //NOI18N
    public static final String LAUNCHER_ADMIN_CONSOLE =
            "admin-console"; //NOI18N
    public static final String ADMIN_CONSOLE_URL      =
            "http://{0}:{1}/"; //NOI18N
    
    // bat file caller stub
    public static final String BAT_CALLER_STUB =
            "call {0}"; //NOI18N
    
    // bat file starter stub
    public static final String BAT_STARTER_STUB =
            "start {0}"; //NOI18N
    
    public static final String SHELL_SCRIPT_STUB =
            "#!/bin/sh\n" + //NOI18N
            "\n" + //NOI18N
            "{0}"; //NOI18N
    
    // open browser shell script stub
    public static final String SHELL_SCRIPT_BROWSER_STUB_UNIX =
            "#!/bin/sh\n" + //NOI18N
            "\n" + //NOI18N
            "browsers=\"firefox mozilla netscape opera konqueror /usr/swf/lib/mozilla/mozilla-bin\"\n" + //NOI18N
            "\n" + //NOI18N
            "for i in $browsers\n" + //NOI18N
            "do\n" + //NOI18N
            "    type $i 1>/dev/null 2>/dev/null\n" + //NOI18N
            "    if [ $? -eq 0 ]; then\n" + //NOI18N
            "        browser=$i\n" + //NOI18N
            "        break\n" + //NOI18N
            "    fi\n" + //NOI18N
            "done\n" + //NOI18N
            "\n" + //NOI18N
            "if [ ! -z \"$browser\" ]; then\n" + //NOI18N
            "    $browser {0} &\n" + //NOI18N
            "else\n" + //NOI18N
            "    echo ERROR: could not find an installed browser\n" + //NOI18N
            "fi"; //NOI18N
    
    public static final String SHELL_SCRIPT_BROWSER_STUB_MACOS =
            "#!/bin/sh\n" + //NOI18N
            "\n" + //NOI18N
            "browsers=\"" +
            "/Applications/Firefox.app/Contents/MacOS/firefox " +
            "/Applications/Safari.app/Contents/MacOS/Safari\"\n" + //NOI18N
            "\n" + //NOI18N
            "for i in $browsers\n" + //NOI18N
            "do\n" + //NOI18N
            "    type $i 1>/dev/null 2>/dev/null\n" + //NOI18N
            "    if [ $? -eq 0 ]; then\n" + //NOI18N
            "        browser=$i\n" + //NOI18N
            "        break\n" + //NOI18N
            "    fi\n" + //NOI18N
            "done\n" + //NOI18N
            "\n" + //NOI18N
            "if [ ! -z \"$browser\" ]; then\n" + //NOI18N
            "    $browser {0} &\n" + //NOI18N
            "else\n" + //NOI18N
            "    echo ERROR: could not find an installed browser\n" + //NOI18N
            "fi"; //NOI18N
    
    
    public static final String AS_ABOUT_NAME =
            "CL.start.menu.as.about";
    public static final String AS_QUICK_START_NAME =
            "CL.start.menu.as.quick.start";
    public static final String AS_JAVAEE_DOCS_NAME =
            "CL.start.menu.as.javaee.documentation";
    public static final String AS_ADMIN_CONSOLE_NAME =
            "CL.start.menu.as.admin.console";
    public static final String AS_START_DOMAIN_NAME =
            "CL.start.menu.as.start.domain";
    public static final String AS_START_DERBY_NAME =
            "CL.start.menu.as.start.derby";
    public static final String AS_STOP_DOMAIN_NAME =
            "CL.start.menu.as.stop.domain";
    public static final String AS_STOP_DERBY_NAME =
            "CL.start.menu.as.stop.derby";
    public static final String AS_SAMPLES_NAME =
            "CL.start.menu.as.samples";
    public static final String AS_RELATIVE_SHORTCUT_LOCATION =
            "CL.start.menu.as.relative";
    
    public static final String AS_ICON_ADMIN_CONSOLE =
            "startadmin.ico";
    public static final String AS_ICON_JAVAEE_DOCS =
            "onlinedoc.ico";
    public static final String AS_ICON_ABOUT =
            "about.ico";
    public static final String AS_ICON_QUICK_START =
            "onlinedoc.ico";
    public static final String AS_ICON_SAMPLES =
            "smple_app.ico";
    public static final String AS_ICON_START_DOMAIN =
            "startAppserv.ico";
    public static final String AS_ICON_STOP_DOMAIN =
            "stopAppserv.ico";
    public static final String AS_ICON_START_DERBY =
            "startDerby.ico";
    public static final String AS_ICON_STOP_DERBY =
            "stopDerby.ico";
    public static final String LOCALHOST = "localhost";
    
    public static final String AS_ABOUT_LOCATION =
            "docs/about.html";
    
    public static final String AS_SAMPLES_LOCATION =
            "samples/index.html";
    
    public static final String AS_QUICK_START_LOCATION =
            "docs/QuickStart.html";
    
    public static final String JAVAEE_DOCS_URL =
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.as.javaee.docs.url"); //NOI18N
    
    public static final String JVM_OPTION_NAME =
            "-Dcom.sun.aas.installRoot"; // NOI18N
}
