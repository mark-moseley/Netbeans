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
/*
 * GTKColor.java
 *
 * Created on April 7, 2004, 1:01 AM
 */

package org.netbeans.swing.plaf.gtk;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Value which will look something up via reflection from the GTK theme.
 *
 * @author  Tim Boudreau
 */
final class ThemeValue implements UIDefaults.ActiveValue {
    private final Object fallback;
    private final Object aRegion;
    private Object aColorType = null;
    private boolean darken = false;
    
    private Object value = null;
    
    private static Boolean functioning = null;
    
    /** Creates a new instance of GTKColor */
    public ThemeValue(Object region, Object colorType, Object fallback) {
        this.fallback = fallback;
        this.aRegion = region;
        this.aColorType = colorType;
        register(this);
    }

    /** Creates a new instance of GTKColor */
    public ThemeValue(Object region, Object colorType, Object fallback, boolean darken) {
        this.fallback = fallback;
        this.aRegion = region;
        this.aColorType = colorType;
        this.darken = darken;
        register(this);
    }
    
    public ThemeValue (Object region, Font fallback) {
        this.fallback = fallback;
        this.aRegion = region;
        register(this);
    }
    
    public Object createValue(UIDefaults table) {
        if (value == null) {
            if (!functioning()) {
                value = fallback;
            } else {
                if (fallback instanceof Font) {
                    Object val = getFont();
                    if (ct++ < 4) {
                        //Wrong values returned if GTK not yet initialized
                        return val;
                    }
                    value = val;
                } else {
                    value = getColor();
                }
            }
        }
        return value != null ? value : fallback;
    }
    
    private int ct = 0;
    
    void clear() {
        value = null;
    }
    
    public Font getFont() {
        Object style = getSynthStyle (aRegion);
        if (Boolean.TRUE.equals(functioning)) {
            try {
                Font result = (Font) synthStyle_getFontForState.invoke (style,
                    new Object [] {
                        getSynthContext ()
                    });
                if (result == null) {
                    result = (Font) fallback;
                }
                return result;
            } catch (Exception e) {
                functioning = Boolean.FALSE;
                if (log) {
                    e.printStackTrace();
                }
            }
        }
        //This will only happen once, after which functioning will be false
        return null; 
    }
    
    private static boolean log = Boolean.getBoolean ("themeValue.log");

    public Color getColor () {
        Object style = getSynthStyle (aRegion);
        if (Boolean.TRUE.equals(functioning)) {
            try {
                Color result = (Color) synthStyle_getColorForState.invoke (style,
                    new Object [] {
                        getSynthContext (),
                        aColorType
                    });
                if (result == null) {
                    result = (Color) fallback;
                }
                if (darken) {
                    result = result.darker();
                }
                return result;
            } catch (Exception e) {
                functioning = Boolean.FALSE;
                if (log) {
                    e.printStackTrace();
                }
            }
        }
        //This will only happen once, after which functioning will be false
        return null;
    }    
    
    public static boolean functioning() {
        if (functioning == null) {
            checkFunctioning();
        }
        return functioning.booleanValue();
    }

    private static void checkFunctioning() {
        functioning = Boolean.FALSE;
        try {
            gtkLookAndFeel = Class.forName ("com.sun.java.swing.plaf.gtk.GTKLookAndFeel"); //NOI18N 
            synthLookAndFeel = Class.forName ("javax.swing.plaf.synth.SynthLookAndFeel"); //NOI18N
            region = Class.forName ("javax.swing.plaf.synth.Region"); //NOI18N
            synthStyle = Class.forName ("javax.swing.plaf.synth.SynthStyle"); //NOI18N
            synthContext = Class.forName ("javax.swing.plaf.synth.SynthContext"); //NOI18N
            colorType = Class.forName ("javax.swing.plaf.synth.ColorType"); //NOI18N
            gtkColorType = Class.forName ("com.sun.java.swing.plaf.gtk.GTKColorType"); //NOI18N
            synthUI = Class.forName ("sun.swing.plaf.synth.SynthUI"); //NOI18N


            synthContextConstructor = synthContext.getDeclaredConstructor(new Class[] {
                JComponent.class,
                region, synthStyle,
                Integer.TYPE
            });
            synthContextConstructor.setAccessible(true);

            synthStyle_getColorForState = synthStyle.getDeclaredMethod ("getColorForState", new Class[] { //NOI18N
                 synthContext, colorType });
                 
            synthStyle_getColorForState.setAccessible(true);
            
            synthStyle_getFontForState = synthStyle.getDeclaredMethod ("getFontForState", new Class[] { //NOI18N
                synthContext });
                
            synthStyle_getFontForState.setAccessible(true);
            

            LIGHT = valueOfField (gtkColorType, "LIGHT"); //NOI18N
            DARK = valueOfField (gtkColorType, "DARK"); //NOI18N
            MID = valueOfField (gtkColorType, "MID"); //NOI18N
            BLACK = valueOfField (gtkColorType, "BLACK"); //NOI18N
            WHITE = valueOfField (gtkColorType, "WHITE"); //NOI18N
            TEXT_FOREGROUND = valueOfField (colorType, "TEXT_FOREGROUND"); //NOI18N
            TEXT_BACKGROUND = valueOfField (colorType, "TEXT_BACKGROUND"); //NOI18N
            FOCUS = valueOfField (colorType, "FOCUS"); //NOI18N

            synthContext_getContext = synthContext.getDeclaredMethod ("getContext",
                    new Class[] {
                        Class.class, JComponent.class, region, synthStyle, Integer.TYPE
                    });
            synthContext_getContext.setAccessible(true);

            synthLookAndFeel_getStyle = synthLookAndFeel.getDeclaredMethod ("getStyle",
                    new Class[] {
                        JComponent.class, region
                    });
            synthLookAndFeel_getStyle.setAccessible (true);

            REGION_BUTTON = valueOfField (region, "BUTTON"); //NOI18N
            REGION_PANEL = valueOfField (region, "PANEL"); //NOI18N
            REGION_SCROLLBAR_THUMB = valueOfField (region, "SCROLL_BAR_THUMB"); //NOI18N
            REGION_TAB = valueOfField (region, "TABBED_PANE_TAB"); //NOI18N
            REGION_INTFRAME = valueOfField (region, "INTERNAL_FRAME_TITLE_PANE"); //NOI18N
            
            synthUI_getContext = synthUI.getDeclaredMethod ("getContext", new Class[] { JComponent.class} ); //NOI18N

            functioning = Boolean.TRUE;
        } catch (Exception e) {
            System.err.println ("Cannot initialize GTK colors - using hardcoded defaults " + e.getMessage()); //NOI18N
            if (log) {
                e.printStackTrace();
            }
            return;
        }
    }

    private static JButton getDummyButton() {
        if (dummyButton == null) {
            dummyButton = new JButton();
            CellRendererPane crp = new CellRendererPane();
            crp.add (dummyButton);
        }
        ButtonModel mdl = dummyButton.getModel();
        return dummyButton;
    }
    
    private static JButton dummyButton = null;
    
    private static Object getSynthContext () {
        try {
            JButton dummyButton = getDummyButton();
            
            if (synthUI.isAssignableFrom(dummyButton.getUI().getClass())) {
                return synthUI_getContext.invoke (dummyButton.getUI(), new Object[] {dummyButton});
            } else {
               throw new IllegalStateException ("I don't have a SynthButtonUI to play with"); //NOI18N
            }
        } catch (Exception e) {
            functioning = Boolean.FALSE;
            if (log) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static Object getSynthStyle (Object region) {
        try {
            return synthLookAndFeel_getStyle.invoke (null, new Object[] { getDummyButton(), region} );
        } catch (Exception e) {
            functioning = Boolean.FALSE;
            if (log) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static Object valueOfField (Class clazz, String field) throws NoSuchFieldException, IllegalAccessException {
        Field f = clazz.getDeclaredField(field);
        f.setAccessible(true);
        return f.get(null);
    }

    private static Class synthLookAndFeel = null;
    private static Class gtkLookAndFeel = null;
    private static Class colorType = null;
    private static Class region = null;
    private static Class synthStyle = null;
    private static Class synthContext = null;
    private static Class gtkColorType = null;
    private static Class synthUI = null;

    private static Constructor synthContextConstructor;
    private static Method synthStyle_getColorForState = null;
    private static Method synthStyle_getFontForState = null;
    private static Method synthLookAndFeel_getStyle = null;

    private static Method synthContext_getContext = null;
    private static Method synthUI_getContext = null;

    //XXX should be some to delete here once done experimenting
    static Object /* <Region> */ REGION_BUTTON = null;
    static Object /* <Region> */ REGION_PANEL = null;
    static Object /* <Region> */ REGION_SCROLLBAR_THUMB = null;
    static Object /* <Region> */ REGION_TAB = null;
    static Object /* <Region> */ REGION_INTFRAME = null;

    static Object /* <GTKColorType> */ LIGHT = null;
    static Object /* <GTKColorType> */ DARK = null;
    static Object /* <GTKColorType> */ BLACK = null;
    static Object /* <GTKColorType> */ WHITE = null;
    static Object /* <GTKColorType> */ MID = null;
    static Object /* <ColorType> */ TEXT_FOREGROUND = null;
    static Object /* <ColorType> */ TEXT_BACKGROUND = null;
    static Object /* <ColorType> */ FOCUS = null;    
    
    
    private static HashSet instances = null;
    /** 
     * Unbeautiful caching - the reflection lookup has serious performance
     * issues - we will cache values instead.  */
    private static synchronized void register (ThemeValue value) {
        if (instances == null) {
            instances = new HashSet();
            registerPcl();
        }
        instances.add (value);
    }
    
    private static void registerPcl() {
        PropertyChangeListener l = new Listener();
        UIManager.addPropertyChangeListener(l);
        
        //Thanks to Scott Violet for how to do this.  See also
        //com.sun.java.swing.plaf.gtk.GtkLookAndFeel.WeakPCL
        
        Toolkit.getDefaultToolkit().addPropertyChangeListener(
            "gnome.Gtk/FontName", l); //NOI18N
        Toolkit.getDefaultToolkit().addPropertyChangeListener(
            "gnome.Xft/DPI", l); //NOI18N
        Toolkit.getDefaultToolkit().addPropertyChangeListener(
            "gnome.Net/ThemeName", l); //NOI18N
        
    }
    
    private static class Listener implements PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent pce) {
            if (pce.getSource() instanceof UIManager && "lookAndFeel".equals( //NOI18N
                pce.getPropertyName())) { 
                    
                String s = UIManager.getLookAndFeel().getClass().getName();
                if (s.indexOf("gtk") < 0) { //NOI18N
                    //We have changed look and feels somehow.  Unregister.
                    UIManager.removePropertyChangeListener(this);
                    Toolkit.getDefaultToolkit().removePropertyChangeListener(
                        "gnome.Gtk/FontName", this); //NOI18N
                    Toolkit.getDefaultToolkit().removePropertyChangeListener(
                        "gnome.Xft/DPI", this); //NOI18N
                    Toolkit.getDefaultToolkit().removePropertyChangeListener(
                        "gnome.Net/ThemeName", this); //NOI18N
                }
            } else {
                Iterator i = instances.iterator();
                
                while (i.hasNext()) {
                    ((ThemeValue)i.next()).clear();
                }
            }
        }
    }
    
    static {
        //This must be called to initialize the fields before anyone tries
        //to construct a ThemeValue passing, say, ThemeValue.LIGHT.  These are
        //populated with values from GTKLookAndFeel by reflection
        functioning();
    }
}
