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
package org.netbeans.tax;

import java.util.ResourceBundle;
import java.text.MessageFormat;

/**
 *
 * @author Libor Kramolis
 * @version 0.2
 */
public abstract class AbstractUtil {
    /** */
    private static ResourceBundle bundle;

    
    /**
     * Kind of equals that treat null-ed object as equvivalent.
     * Suitable while testing for property equalence before firing.
     * return true if these are same
     */
    public static boolean equals (Object a, Object b) {
        if ( a != null ) {
            return (a.equals (b));
        } else {
            return (a == b);
        }
    }

    /**
     * Just for debugging purposes.
     */
    public final void debug (String message, Throwable ex) {
        if ( (Integer.getInteger (this.getPackageName(), -1)).intValue() >= 0 ) {
            System.err.println ("[" + getPackageName() + "] " + message);
            if ( ex != null ) {
                ex.printStackTrace (System.err);
            }
        }
    }
    
    
    /**
     * Just for debugging purposes.
     */
    public final void debug (String message) {
        debug (message, null);
    }


    /**
     * @return package name of this instance
     */
    private final String getPackageName () {
        return this.getClass().getPackage().getName();
    }


    /**
     * @return bundle for this instance package
     */
    protected final synchronized ResourceBundle getBundle () {
        if ( bundle != null ) {
            return bundle;
        }
        String bundleName = getPackageName() + ".Bundle"; // NOI18N
        bundle = ResourceBundle.getBundle (bundleName);

        return bundle;
    }
    
    /** Get localized string.
     * @param key key of localized value.
     * @return localized value.
     */
    public final String getString (String key) {
        return getBundle ().getString (key);
    }
    
    /** Get localized string by passing parameter.
     * @param key key of localized value.
     * @param param argument to use when formating the message
     * @return localized value.
     */
    public final String getString (String key, Object param) {
        return MessageFormat.format (getBundle().getString (key), new Object[] {param});
    }
    
    /** Get localized character. Usually used on mnemonic.
     * @param key key of localized value.
     * @return localized value.
     */
    public final char getChar (String key) {
        return getString (key).charAt (0);
    }

}
