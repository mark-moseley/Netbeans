/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.utils;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.netbeans.installer.Installer;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.helper.NbiThread;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.helper.UiMode;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_NO_CANCEL_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.NO_OPTION;
import static javax.swing.JOptionPane.CANCEL_OPTION;
import static org.netbeans.installer.utils.SystemUtils.getCurrentPlatform;

/**
 *
 * @author Kirill Sorokin
 * @author Dmitry Lipin
 */
public final class UiUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static boolean lookAndFeelInitialized = false;
    private static LookAndFeelType lookAndFeelType = null;
    
    public static void showMessageDialog(
            final String message,
            final String title,
            final MessageType messageType) {
        try {
            initializeLookAndFeel();
        } catch (InitializationException  e) {
            ErrorManager.notifyWarning(e.getMessage(), e.getCause());
        }
        
        switch (UiMode.getCurrentUiMode()) {
            case SWING:
                int intMessageType = JOptionPane.INFORMATION_MESSAGE;
                if (messageType == MessageType.WARNING) {
                    intMessageType = JOptionPane.WARNING_MESSAGE;
                } else if (messageType == MessageType.ERROR) {
                    intMessageType = JOptionPane.ERROR_MESSAGE;
                } else if (messageType == MessageType.CRITICAL) {
                    intMessageType = JOptionPane.ERROR_MESSAGE;
                }
                
                JOptionPane.showMessageDialog(
                        null,
                        message,
                        title,
                        intMessageType);
                break;
            case SILENT:
                LogManager.log(message);
                System.err.println(message);
                break;
        }
    }
    /**
     * 
     * @param title The title of the dialog
     * @param message The message of the dialog
     * @return true if user click YES option. 
     * If installer is running silently then false is returned.
     */
    public static boolean showYesNoDialog(
            final String title,
            final String message) {
        return showYesNoDialog(title, message, false);
    }
    /**
     * @param title The title of the dialog
     * @param message The message of the dialog
     * @param silentDefault The default return value if installer is running silently
     * @return true if user click YES option. In silent mode return <code>silentDefault</code>
     */
    public static boolean showYesNoDialog(
            final String title,
            final String message,
            final boolean silentDefault) {
        
        switch (UiMode.getCurrentUiMode()) {
            case SWING:
                final int result = JOptionPane.showConfirmDialog(
                        null,
                        message,
                        title,
                        YES_NO_OPTION);
                return result == YES_OPTION;
                
            case SILENT:
                LogManager.log(message);                
                final String option = StringUtils.format(
                        ResourceUtils.getString(UiUtils.class,
                        silentDefault ? 
                            RESOURCE_SILENT_DEFAULT_YES : 
                            RESOURCE_SILENT_DEFAULT_NO));
                System.err.println(message);
                System.err.println(option);
                LogManager.log(message);
                LogManager.log(option);
                return silentDefault;
        }
        //never get this line...
        return true;
    }
    /**
     * @param title The title of the dialog
     * @param message The message of the dialog
     * @param silentDefault The dafault return value if installer is running silently
     * @return true if user click YES option. In silent mode return <code>silentDefault</code>
     */
    public static int showYesNoCancelDialog(
            final String title,
            final String message,
            final int silentDefault) {
        
        switch (UiMode.getCurrentUiMode()) {
            case SWING:
                return JOptionPane.showConfirmDialog(
                        null,
                        message,
                        title,
                        YES_NO_CANCEL_OPTION);                
                
            case SILENT:
                LogManager.log(message);               
                String resource;
                switch(silentDefault) {
                    case YES_OPTION : 
                        resource = RESOURCE_SILENT_DEFAULT_YES; 
                        break;
                    case NO_OPTION : 
                        resource = RESOURCE_SILENT_DEFAULT_NO; 
                        break;
                    case CANCEL_OPTION : 
                        resource = RESOURCE_SILENT_DEFAULT_CANCEL; 
                        break;
                    default:
                        resource = StringUtils.EMPTY_STRING;
                        break;
                }
                        
                final String option = StringUtils.format(
                        ResourceUtils.getString(UiUtils.class,resource));
                System.err.println(message);
                System.err.println(option);
                LogManager.log(message);
                LogManager.log(option);
                return silentDefault;
        }
        //never get this line...
        return silentDefault;
    }
    
    public static void initializeLookAndFeel() throws InitializationException {
        if (lookAndFeelInitialized) {
            return;
        }
        
        try {
            LogManager.log("... initializing look and feel");
            LogManager.indent();
            switch (UiMode.getCurrentUiMode()) {
                case SWING:
                    String className = System.getProperty(LAF_CLASS_NAME_PROPERTY);
                    if (className == null) {
                        LogManager.log("... custom look and feel class name was not specified, using system default");
                        className = UiUtils.getDefaultLookAndFeelClassName();
                    }
                    
                    LogManager.log("... class name: " + className);
                    
                    if (Boolean.getBoolean(LAF_DECORATED_WINDOWS_PROPERTY)) {
                        JFrame.setDefaultLookAndFeelDecorated(true);
                    }
                    
                    try {
                        Thread jdkFileChooserWarningLogThread = null;
                        try {
                            // this helps to avoid some GTK L&F bugs for some locales
                            LogManager.log("... get installed L&Fs");
                            UIManager.getInstalledLookAndFeels();
                            LogManager.log("... set specified L&F");
                            UIManager.setLookAndFeel(className);
                            LogManager.log("... check headless");                            
                            if (GraphicsEnvironment.isHeadless()) {
                                HeadlessException e = new HeadlessException();
                                System.err.println(e.getMessage());
                                throw new InitializationException(
                                        ResourceUtils.getString(UiUtils.class, 
                                        RESOURCE_FAILED_TO_INIT_UI), e);
                            }
                            if (SystemUtils.isWindows()) {
                                // workaround for the issue with further using JFileChooser
                                // in case of missing system icons
                                // Java Issue :
                                // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6210674
                                // NBI Issue :
                                // http://www.netbeans.org/issues/show_bug.cgi?id=105065
                                // it also a workaround for two more bugs
                                // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6449933
                                // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6489447
                                LogManager.log("... creating JFileChooser object to check possible issues with UI");
                                
                                if (System.getProperty("java.version").startsWith("1.6")) {
                                    File desktop = new File(SystemUtils.getUserHomeDirectory(), "Desktop");
                                    File[] zips = null;
                                    final List<String> names = new ArrayList<String>();
                                    if (FileUtils.exists(desktop)) {
                                        zips = desktop.listFiles(new FileFilter() {
                                            public boolean accept(File pathname) {
                                                boolean result = pathname.getName().endsWith(".zip") && pathname.length() > 1000000L;
                                                if (result) {
                                                    names.add(pathname.getName());
                                                }
                                                return result;
                                            }
                                        });
                                    }
                                    if (zips != null && zips.length > 0) {
                                        jdkFileChooserWarningLogThread = new NbiThread() {
                                            @Override
                                            public void run() {
                                                try {
                                                    sleep(8000); //8 seconds
                                                } catch (InterruptedException e) {
                                                    return;
                                                }
                                                final File lock = new File(Installer.getInstance().getLocalDirectory(), Installer.LOCK_FILE_NAME);
                                                LogManager.log("\n... !!! WARNING !!!");
                                                LogManager.log("... There are some big zip files on your desktop: " + StringUtils.asString(names));
                                                LogManager.log("... In case installer UI does not appear for a long time:");
                                                LogManager.log("...    1) kill the installer process");
                                                LogManager.log("...    2) move those zip files somewhere from the desktop");
                                                LogManager.log("...    3) delete " + lock);
                                                LogManager.log("...    4) run installer again");                                                
                                                LogManager.log("... For more details see the following bugs descriptions: ");
                                                LogManager.log("... http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6372808");
                                                LogManager.log("... http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5050516");
                                            }
                                        };
                                        jdkFileChooserWarningLogThread.start();
                                    }
                                }
                                
                                
                                new JFileChooser();                                
                                if(jdkFileChooserWarningLogThread!=null) {
                                    jdkFileChooserWarningLogThread.interrupt();
                                    jdkFileChooserWarningLogThread = null;
                                }
                                
                                LogManager.log("... getting default Toolkit to check possible issues with UI");
                                Toolkit.getDefaultToolkit();
                                
                                // workaround for JDK issue with JProgressBar using StyleXP
                                // http://www.netbeans.org/issues/show_bug.cgi?id=106876
                                // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6337517
                                LogManager.log("... creating JProgressBar object to check possible issues with UI");
                                new JProgressBar().getMaximumSize();
                                
                                LogManager.log("... all UI checks done");
                            }
                            LogManager.log("... L&F is set");
                        } catch (Throwable e) {
                            if(jdkFileChooserWarningLogThread!=null) {
                                jdkFileChooserWarningLogThread.interrupt();
                                jdkFileChooserWarningLogThread = null;
                            }
                            // we're catching Throwable here as pretty much anything can happen
                            // while setting the look and feel and we have no control over it
                            // if something wrong happens we should fall back to the default
                            // cross-platform look and feel which is assumed to be working
                            // correctly
                            LogManager.log("... could not activate defined L&F, initializing cross-platfrom one", e);
                            if (e instanceof InternalError) {
                                System.err.println(e.getMessage());
                            } else if (e instanceof ExceptionInInitializerError) {
                                final Throwable cause = e.getCause();
                                
                                if ((cause != null) &&
                                        (cause instanceof HeadlessException)) {
                                    System.err.println(cause.getMessage());
                                }
                            }                            
                            
                            className = UIManager.getCrossPlatformLookAndFeelClassName();
                            LogManager.log("... cross-platform L&F class-name : " + className);
                            
                            UIManager.setLookAndFeel(className);
                            
                            if(System.getProperty(LAF_CLASS_NAME_PROPERTY)!=null) {
                                // Throw exception only if user specified custom L&F, 
                                // otherwise just go to initialization of cross-platfrom L&F 
                                //     (Exception e is already logged above)
                                // See also http://www.netbeans.org/issues/show_bug.cgi?id=122557                                
                                // This exception would be thrown only if cross-platform LAF is successfully installed
                                throw new InitializationException(
                                    ResourceUtils.getString(UiUtils.class, 
                                    RESOURCE_FAILED_TO_ACTIVATE_DEFINED_LAF), e);
                            }                            
                        }
                    } catch (NoClassDefFoundError e) {
                        throw new InitializationException(
                                ResourceUtils.getString(UiUtils.class,
                                RESOURCE_FAILED_TO_ACTIVATE_CROSSPLATFORM_LAF), e);
                    } catch (ClassNotFoundException e) {
                        throw new InitializationException(ResourceUtils.getString(UiUtils.class,
                                RESOURCE_FAILED_TO_ACTIVATE_CROSSPLATFORM_LAF), e);
                    } catch (InstantiationException e) {
                        throw new InitializationException(ResourceUtils.getString(UiUtils.class,
                                RESOURCE_FAILED_TO_ACTIVATE_CROSSPLATFORM_LAF), e);
                    } catch (IllegalAccessException e) {
                        throw new InitializationException(ResourceUtils.getString(UiUtils.class,
                                RESOURCE_FAILED_TO_ACTIVATE_CROSSPLATFORM_LAF), e);
                    } catch (UnsupportedLookAndFeelException e) {
                        throw new InitializationException(ResourceUtils.getString(UiUtils.class,
                                RESOURCE_FAILED_TO_ACTIVATE_CROSSPLATFORM_LAF), e);
                    }
                    break;
            }
        } finally {
            LogManager.unindent();
            LogManager.log("... initializing L&F finished");
            lookAndFeelInitialized = true;
            lookAndFeelType = getLAF();
        }
    }
    
    public static String getDefaultLookAndFeelClassName(
            ) {
        switch (UiMode.getCurrentUiMode()) {
            case SWING:
                String className = UIManager.getSystemLookAndFeelClassName();
                
                // if the default look and feel is the cross-platform one, we might
                // need to correct this choice. E.g. - KDE, where GTK look and feel
                // would be much more appropriate
                if (className.equals(UIManager.getCrossPlatformLookAndFeelClassName())) {
                    
                    // if the current platform is Linux and the desktop manager is
                    // KDE, then we should try to use the GTK look and feel
                    try {
                        if (getCurrentPlatform().isCompatibleWith(Platform.LINUX) &&
                                (System.getenv("KDE_FULL_SESSION") != null)) {
                            // check whether the GTK look and feel class is
                            // available -- we'll get CNFE if it is not and it will
                            // not be set
                            Class.forName("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
                            
                            className = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
                        }
                    } catch (ClassNotFoundException e) {
                        ErrorManager.notifyDebug(ResourceUtils.getString(UiUtils.class,
                                RESOURCE_FAILED_TO_FORCE_GTK), e);
                    }
                }
                
                return className;
            default:
                return null;
        }
    }
    public static final LookAndFeelType getLAF() {
        if(lookAndFeelType==null) {
            try {
                initializeLookAndFeel();
            } catch (InitializationException e) {
                LogManager.log(e);
            }
            lookAndFeelType = LookAndFeelType.DEFAULT;
            
            if(UiMode.getCurrentUiMode() == UiMode.SWING) {
                LookAndFeel laf = UIManager.getLookAndFeel();
                if(laf!=null) {
                    String id = laf.getID();
                    if(id.equals("Windows")) {
                        final Object object = Toolkit.
                                getDefaultToolkit().
                                getDesktopProperty(WINDOWS_XP_THEME_MARKER_PROPERTY);
                        boolean xpThemeActive = false;
                        if (object != null) {
                            xpThemeActive = (Boolean) object;
                        }
                        lookAndFeelType = (xpThemeActive) ? LookAndFeelType.WINDOWS_XP :
                            LookAndFeelType.WINDOWS_CLASSIC;
                    } else if(id.equals("GTK")){
                        lookAndFeelType = LookAndFeelType.GTK;
                    } else if(id.equals("Motif")){
                        lookAndFeelType = LookAndFeelType.MOTIF;
                    }else if(id.equals("Metal")){
                        lookAndFeelType = LookAndFeelType.METAL;
                    } else if(id.equals("Aqua")){
                        lookAndFeelType = LookAndFeelType.AQUA;
                    }
                }
            }
        }
        return lookAndFeelType;
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private UiUtils() {
        // does nothing
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    
    public static enum MessageType {
        INFORMATION,
        WARNING,
        ERROR,
        CRITICAL
    }
    
    public enum LookAndFeelType {
        WINDOWS_XP("win.xp"),
        WINDOWS_CLASSIC("win.classic"),
        MOTIF("motif"),
        GTK("gtk"),
        METAL("metal"),
        AQUA("aqua"),
        DEFAULT("default");
        
        private String name;
        @Override
        public String toString() {
            return name;
        }
        private LookAndFeelType(String name) {
            this.name = name;
        }
    };
    
    public static int getDimension(Properties props, final String defaultPropertyName, final int defaultValue) {
        int dimension = defaultValue;
        String propertyName = defaultPropertyName;
        if (props.getProperty(propertyName + "." + UiUtils.getLAF()) != null) {
            propertyName = propertyName + "." + UiUtils.getLAF();
        }
        
        if (props.getProperty(propertyName) != null) {
            try {
                dimension = Integer.parseInt(
                        props.getProperty(propertyName).trim());
            } catch (NumberFormatException e) {
                final String warning = ResourceUtils.getString(
                        UiUtils.class,
                        RESOURCE_FAILED_TO_PARSE_SYSTEM_PROPERTY,
                        propertyName,
                        props.getProperty(propertyName));
                
                ErrorManager.notifyWarning(warning, e);
            }
        } 
        return dimension;
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    
    /**
     * Name of the system property, which contains the look and feel class name that
     * should be used by the wizard.
     */
    public static final String LAF_CLASS_NAME_PROPERTY =
            "nbi.look.and.feel"; // NOI18N
    
    /**
     * Name of the system property, which tells the UiUtils whether the wizard
     * windows should be decorated by the current look and feel or the system
     * window manager.
     */
    public static final String LAF_DECORATED_WINDOWS_PROPERTY =
            "nbi.look.and.feel.decorate.windows"; // NOI18N
    
    public static final String WINDOWS_XP_THEME_MARKER_PROPERTY =
            "win.xpstyle.themeActive"; // NOI18N
     /**
     * Name of a resource bundle entry.
     */
    private static final String RESOURCE_FAILED_TO_PARSE_SYSTEM_PROPERTY =
            "UI.error.failed.to.parse.property"; // NOI18N
    private static final String RESOURCE_FAILED_TO_ACTIVATE_CROSSPLATFORM_LAF =
            "UI.error.failed.to.activate.crossplatform.laf"; // NOI18N
    private static final String RESOURCE_FAILED_TO_ACTIVATE_DEFINED_LAF =
            "UI.error.failed.to.activate.defined.laf";//NOI18N
    private static final String RESOURCE_FAILED_TO_INIT_UI = 
            "UI.error.failed.to.init.ui";//NOI18N
    private static final String RESOURCE_FAILED_TO_FORCE_GTK =
            "UI.error.failed.to.force.gtk";//NOI18N
    private static final String RESOURCE_SILENT_DEFAULT_YES = 
            "UI.silent.default.yes";//NOI18N
    private static final String RESOURCE_SILENT_DEFAULT_NO = 
            "UI.silent.default.no";//NOI18N
    private static final String RESOURCE_SILENT_DEFAULT_CANCEL = 
            "UI.silent.default.cancel";//NOI18N
}
