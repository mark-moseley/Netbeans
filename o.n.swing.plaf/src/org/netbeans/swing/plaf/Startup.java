/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.swing.plaf;

import java.awt.Toolkit;
import org.netbeans.swing.plaf.aqua.AquaLFCustoms;
import org.netbeans.swing.plaf.gtk.GtkLFCustoms;
import org.netbeans.swing.plaf.metal.MetalLFCustoms;
import org.netbeans.swing.plaf.util.NbTheme;
import org.netbeans.swing.plaf.util.RelativeColor;
import org.netbeans.swing.plaf.util.UIBootstrapValue;
import org.netbeans.swing.plaf.util.UIUtils;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.swing.plaf.winclassic.WindowsLFCustoms;
import org.netbeans.swing.plaf.winxp.XPLFCustoms;

/** Singleton, manages customizers for various LFs. Installs, uninstalls them on LF change.
 * LF customizers works with Swing UIManager, putting info about various UI elements
 * in it. Other modules then can query UIManager to get UI elements to get needed
 * visual design and behaviour.
 *
 * @author  Dafe Simonek, Tim Boudreau
 */
public final class Startup {
    //originally LFCustomsManager

    /** For debugging purposes, enable forcing the customizations for, i.e.,
     * Windows look and feel on a platform that doesn't support it */
    private static final String FORCED_CUSTOMS = System.getProperty("nb.forceui"); //NOI18N
    
    /** Provides the ability to disable customizations for applications which, for example, provide their own
     * subclass of MetalLookAndFeel.  See issue XXX
     */
    private static final boolean NO_CUSTOMIZATIONS = Boolean.getBoolean("netbeans.plaf.disable.ui.customizations"); //NOI18N

    /** Constants for default system-provided LF customizers */
    private static final String MetalCustomsKey = "Nb.MetalLFCustoms"; //NOI18N
    private static final String MetalCustomsClass = "org.netbeans.swing.plaf.metal.MetalLFCustoms"; //NOI18N
    
    private static final String WinCustomsKey = "Nb.WindowsLFCustoms"; //NOI18N
    private static final String WinCustomsClass = "org.netbeans.swing.plaf.winclassic.WindowsLFCustoms"; //NOI18N
    
    private static final String XPCustomsKey = "Nb.WindowsXPLFCustoms"; //NOI18N
    private static final String XPCustomsClass = "org.netbeans.swing.plaf.winxp.XPLFCustoms"; //NOI18N
    
    private static final String AquaCustomsKey = "Nb.AquaLFCustoms"; //NOI18N
    private static final String AquaCustomsClass = "org.netbeans.swing.plaf.aqua.AquaLFCustoms"; //NOI18N
    
    private static final String GtkCustomsKey = "Nb.GTKLFCustoms"; //NOI18N
    private static final String GtkCustomsClass = "org.netbeans.swing.plaf.gtk.GtkLFCustoms"; //NOI18N
    
    /** Singleton instance */
    private static Startup instance = null;
    
    /** Currently used LF customizer */
    private LFCustoms curCustoms = null;
    private LFCustoms globalCustoms = null;

    private static URL themeURL = null;
    private static Class uiClass = null;

    private boolean installed = false;

    /** Starts handling of LF customizers. Called only from getInstance. */
    private Startup() {
        initialize();
    }

    /** Initializes defaulf customs for all LFs and fills UIManager with
     * references to LF customizers for supported LFs.
     */
    private void initialize() {
        LookAndFeel lf = getLookAndFeel();
        if (lf instanceof MetalLookAndFeel) {
            //Metal theme must be assigned before using the look and feel
            installTheme(lf);
        }
        // overall defaults for all LFs
        // defaults for supported LFs

        try {
            if (lf != UIManager.getLookAndFeel()) {
                UIManager.setLookAndFeel (lf);
            }
        } catch (Exception e) {
            System.err.println ("Could not install look and feel " + lf);
        }
    }

    private LookAndFeel getLookAndFeel() {
      if (uiClass == null) {
          String uiClassName;
          if (isWindows()) {
              uiClassName = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"; //NOI18N
          } else if (isMac()){
              uiClassName = "apple.laf.AquaLookAndFeel";
          } else if (shouldUseMetal()) {
              uiClassName = "javax.swing.plaf.metal.MetalLookAndFeel"; //NOI18N
          } else {
              //Should get us metal where it doesn't get us GTK
              uiClassName = UIManager.getSystemLookAndFeelClassName();
              
              //XXX Temporarily disabled GTK by default until JDK 1.5 beta 2
              if (uiClassName.indexOf("gtk") >= 0 && !Boolean.getBoolean("useGtk")) {
                  uiClassName = "javax.swing.plaf.metal.MetalLookAndFeel";
              }
          }
          try {
              uiClass = Class.forName(uiClassName);
          } catch (ClassNotFoundException e) {
              System.err.println("Custom UI class " + uiClassName + " not on classpath."); // NOI18N
          } catch (Exception e) {
              System.err.println("While loading: " + uiClassName); // NOI18N
              e.printStackTrace();
          }
      }
      LookAndFeel result = null;
      if (uiClass != null) {
          try {

              LookAndFeel lf = UIManager.getLookAndFeel();
              if (uiClass != lf.getClass()) {
                  result = (LookAndFeel) uiClass.newInstance();
                  UIManager.setLookAndFeel(lf);
              } else {
                  result = UIManager.getLookAndFeel();
              }
          } catch (Exception e) {
              System.err.println("Cannot load custom UI class " + uiClass); //NOI18N
              e.printStackTrace();
              result = UIManager.getLookAndFeel();
          }
      }
      return result;
    }

    private void installTheme(LookAndFeel lf) {
        //Load the theme
        if (themeURL != null) {
          NbTheme nbTheme = new NbTheme(themeURL, lf);
          MetalLookAndFeel.setCurrentTheme(nbTheme);
        }
    }

    /** Enables, installs LF customization.  */
    private void install () {
        if (installed) {
            return;
        }
        if (globalCustoms == null) {
            globalCustoms = new AllLFCustoms();
            installLFCustoms (globalCustoms);
        }
        installPerLFDefaults();
        installTheme(UIManager.getLookAndFeel());
        attachListener();
    }

    private void installPerLFDefaults() {
        boolean isLFChange = curCustoms != null;
        
        curCustoms = findCustoms();
        if (curCustoms != null) {
            Integer in = (Integer) UIManager.get(LFCustoms.CUSTOM_FONT_SIZE); //NOI18N
            if (in == null && UIManager.getLookAndFeel().getClass() == MetalLookAndFeel.class) {
                in = new Integer (11);
            }

            if (in != null) {
                AllLFCustoms.initCustomFontSize (in.intValue());
            }
            installLFCustoms (curCustoms);
            if (isLFChange) {
                //make sure UIBootstrapValue.Lazy instances really get a chance
                //to replace their values
                loadAllLazyValues (curCustoms);
            }
            curCustoms.disposeValues();
        }
    }
    
    private void loadAllLazyValues (LFCustoms customs) {
        if (globalCustoms != null) {
            loadLazy (globalCustoms.getApplicationSpecificKeysAndValues());
            loadLazy (globalCustoms.getGuaranteedKeysAndValues());
            loadLazy (globalCustoms.getLookAndFeelCustomizationKeysAndValues());
        }
        loadLazy (customs.getApplicationSpecificKeysAndValues());
        loadLazy (customs.getGuaranteedKeysAndValues());
        loadLazy (customs.getLookAndFeelCustomizationKeysAndValues());
    }
    
    private void loadLazy (Object[] o) {
        if (o.length > 0) {
            UIDefaults uidefaults = UIManager.getDefaults();
            for (int i=1; i < o.length; i+=2) {
                if (o[i] instanceof UIBootstrapValue.Lazy) {
                    ((UIBootstrapValue.Lazy) o[i]).createValue(uidefaults);
                }
                if (o[i] instanceof RelativeColor) {
                    ((RelativeColor) o[i]).clear();
                }
            }
        }
    }

    private void uninstallPerLFDefaults() {
        assert globalCustoms != null;

        if (curCustoms != null) {
            Set keep = new HashSet (Arrays.asList(globalCustoms.allKeys()));
            Object[] arr = curCustoms.allKeys();

            for (int i=0; i < arr.length; i++) {
                Object key = arr[i];
                if (!keep.contains(key)) {
                    UIManager.put (key, null);
                }
            }
        }
    }

    private void attachListener() {
        assert listener == null;
        listener = new LFListener();
        UIManager.addPropertyChangeListener(listener);
        Toolkit.getDefaultToolkit().addPropertyChangeListener(
            "win.xpstyle.themeActive", listener); //NOI18N
    }

    private void installLFCustoms (LFCustoms customs) {
        UIDefaults defaults = UIManager.getDefaults();
        //Install values that some look and feels may leave out, which should
        //be included
        defaults.putDefaults (customs.getGuaranteedKeysAndValues());
        //Install entries for custom NetBeans components, such as borders and
        //colors
        defaults.putDefaults (customs.getApplicationSpecificKeysAndValues());
        
        if (!NO_CUSTOMIZATIONS) {
            //See issue nnn - Nokia uses a custom metal-based look and feel,
            //and do not want fonts or other things customized
            defaults.putDefaults (customs.getLookAndFeelCustomizationKeysAndValues());
        }
    }

    /** Finds and returns instance of LF customizer which is suitable for
     * current look and feel.
     */
    private LFCustoms findCustoms () {
        if (FORCED_CUSTOMS != null) {
            System.err.println("Using explicitly set UI customizations: " + //NOI18N
                FORCED_CUSTOMS);
            if ("XP".equals(FORCED_CUSTOMS)) { //NOI18N
                return new XPLFCustoms();
            } else if ("Aqua".equals(FORCED_CUSTOMS)) { //NOI18N
                return new AquaLFCustoms();
            } else if ("Metal".equals(FORCED_CUSTOMS)) { //NOI18N
                return new MetalLFCustoms();
            } else if ("Windows".equals(FORCED_CUSTOMS)) { //NOI18N
                return new WindowsLFCustoms();
            } else if ("GTK".equals(FORCED_CUSTOMS)) { //NOI18N
                return new GtkLFCustoms();
            } else {
                try {
                    return (LFCustoms) Class.forName(FORCED_CUSTOMS).newInstance();
                } catch (Exception e) {
                    System.err.println("UI customizations class not found: " //NOI18N
                        + FORCED_CUSTOMS); //NOI18N
                }
            }
        }
        
        StringBuffer buf = new StringBuffer(40);
        buf.append("Nb."); //NOI18N
        buf.append(UIManager.getLookAndFeel().getID());
        if (UIUtils.isXPLF()) {
            buf.append("XPLFCustoms"); //NOI18N
        } else {
            buf.append("LFCustoms"); //NOI18N
        }

        LFCustoms result = null;
        try {
            result = (LFCustoms)UIManager.get(buf.toString());
        } catch (ClassCastException cce) {
            //do nothing - the look and feel happens to have something matching
            //our generated key there
        }
        if (result == null) {
            String[] knownLFs = new String[] {
                    "Metal", "Windows", "Aqua", "GTK" //NOI18N
                };
            switch (Arrays.asList(knownLFs).indexOf(UIManager.getLookAndFeel().getID())) {
                case 1 :
                    if (UIUtils.isXPLF()) {
                        result = new XPLFCustoms();
                    } else {
                        result = new WindowsLFCustoms();
                    }
                    break;
                case 0 :
                    result = new MetalLFCustoms();
                    break;
                case 2 :
                    result = new AquaLFCustoms();
                    break;
                case 3 :
                    result = new GtkLFCustoms();
                    break;
                default :
                    result = new WindowsLFCustoms();
            }
        }
        return result;
    }
    
    /**
     * Initialize values in UIDefaults which need to be there for NetBeans' components; apply customizations such
     * as setting up a custom font size and loading a theme.
     *
     * @param uiClass The UI class which should be used for the look and feel
     * @param uiFontSize A custom fontsize, or 0.  This will be retrievable via UIManager.get("customFontSize") after this method has returned
     *          if non 0.  If non zero, all of the standard Swing font keys in UIDefaults will be customized to
     *          provide a font with the requested size.  Results are undefined for values less than 0 or greater
     *          than any hard limit the platform imposes on font size.
     * @param themeURL An optional URL for a theme file, or null. Theme file format documentation can be found
     *        <a href="ui.netbeans.org/project/ui/docs/ui/themes/themes.html">here</a>.
     */
    public static void run (Class uiClass, int uiFontSize, URL themeURL) {
        if (instance == null) {
          // Modify default font size to the font size passed as a command-line parameter
            if(uiFontSize>0) {
                Integer customFontSize = new Integer (uiFontSize);
                UIManager.put ("customFontSize", customFontSize);
            }
            Startup.uiClass = uiClass;
            Startup.themeURL = themeURL;
            instance = new Startup();
            instance.install();
        }
    }

    private boolean isWindows() {
        String osName = System.getProperty ("os.name");
        return osName.startsWith("Windows");
    }

    private boolean isMac() {
        String osName = System.getProperty ("os.name");
        boolean result = osName.startsWith ("Darwin") || "Mac OS X".equals(osName);
        return result;
    }

    /** If it is solaris or linux, we can use GTK where supported by getting
     * the platform specific look and feel.  Also check to make sure under no
     * circumstances do we use Motif look and feel.
     *
     * @return If metal L&F should be used
     */
    private boolean shouldUseMetal() {
        String osName = System.getProperty ("os.name");
        boolean result = !"Solaris".equals (osName) &&
            !osName.startsWith ("SunOS") &&
            !osName.endsWith ("Linux") ||
            UIManager.getSystemLookAndFeelClassName().indexOf("Motif") > -1;
        return result;
    }

    private LFListener listener = null;
    private class LFListener implements PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent pcl) {
            if ("lookAndFeel".equals(pcl.getPropertyName()) || "win.xpstyle.themeActive".equals(pcl.getPropertyName())) { //NOI18N
                uninstallPerLFDefaults();
                installPerLFDefaults();
            }
        }
    }
    
}
