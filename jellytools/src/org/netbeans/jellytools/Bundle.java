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

package org.netbeans.jellytools;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.netbeans.jemmy.JemmyException;
import org.openide.util.NbBundle;

/** Helper class to get strings from NetBeans Bundle.properties files.
 * <br>
 * Everytime someone wants to identify a component by its title, label, caption or whatever,
 * he should not use hard coded string in his test case but he should use
 * <code>Bundle.getString(bundleName, key)</code> to obtain string from bundle.
 * Then test cases can be executed on different than English locale because
 * <code>getString()</code> methods returns string according to current locale.
 * <br><br>
 * Usage:
 * <br><pre>
 *        // "OK"
 *        Bundle.getString("org.netbeans.core.Bundle", "OK_OPTION_CAPTION");
 *        // "Properties of AnObject"
 *        Bundle.getString("org.netbeans.core.Bundle", "CTL_FMT_LocalProperties", new Object[] {new Integer(1), "AnObject"});
 *        // "View"
 *        Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/View");
 * </pre>
 */
public class Bundle {
    
    /** Placeholder to disallow creating of instances. */
    private Bundle() {
        throw new Error("Bundle is just a container for static methods");
    }
    
    /** Returns ResourceBundle from specified path.
     * @param bundle path to bundle (e.g. "org.netbeans.core.Bundle")
     * @return ResourceBundle instance
     */
    public static ResourceBundle getBundle(String bundle) {
        try {
            return NbBundle.getBundle(bundle);
        } catch (NullPointerException e) {
            throw new JemmyException("\"" + bundle + "\" bundle has not been found", e);
        } catch (MissingResourceException e) {
            throw new JemmyException("\"" + bundle + "\" bundle has not been found", e);
        }
    }
    
    /** Gets string from specified ResourceBundle.
     * @param bundle instance of ResourceBundle
     * @param key key of requested string
     * @return string from bundle in current locale
     */
    public static String getString(ResourceBundle bundle, String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            throw new JemmyException("\"" + key + "\" key has not been found", e);
        } catch (NullPointerException npe) {
            throw new JemmyException("Cannot accept null parameter.", npe);
        }
    }
    
    /** Gets string from bundle specified by path to bundle and format it.
     * @param bundle path to bundle (e.g. "org.netbeans.core.Bundle")
     * @param key key of requested string
     * @param params parameters to be formatted
     * @return string from bundle in current locale with formatted parameters
     */
    public static String getString(ResourceBundle bundle, String key, Object[] params) {
        return java.text.MessageFormat.format(getString(bundle, key), params);
    }
    
    /** Gets string from bundle specified by path to bundle.
     * @param bundle path to bundle (e.g. "org.netbeans.core.Bundle")
     * @param key key of requested string
     * @return string from bundle in current locale
     */
    public static String getString(String bundle, String key) {
        return getString(getBundle(bundle), key);
    }
    
    /** Gets string from bundle, removes '&' from it and cuts parameters
     * like {0} from the end.
     * @param bundle path to bundle (e.g. "org.netbeans.core.Bundle")
     * @param key key of requested string
     * @return string from bundle in current locale. Char '&' is removed and
     * parameter patterns are also removed starting by first '{'.
     */
    public static String getStringTrimmed(String bundle, String key) {
        return trim(getString(getBundle(bundle), key));
    }
    
    /** Gets string from bundle specified by path to bundle and format it.
     * @param bundle path to bundle (e.g. "org.netbeans.core.Bundle")
     * @param key key of requested string
     * @param params parameter to be formatted
     * @return string from bundle in current locale with formatted parameters
     */
    public static String getString(String bundle, String key, Object[] params) {
        return java.text.MessageFormat.format(getString(bundle, key), params);
    }
    
    /** Removes '&' and cut parameters like {0} from the end.
     * @param value string to modify
     * @return string with removed '&' and parameters like {0} from the end.
     */
    private static String trim(String value) {
        // remove '&'
        if(value.indexOf('&')!=-1)
            value = new StringBuffer(value).deleteCharAt(value.indexOf('&')).toString();
        // cut parameters like {0} from string
        if(value.indexOf('{')!=-1)
            value = value.substring(0, value.indexOf('{'));
        return value;
    }
    
}
