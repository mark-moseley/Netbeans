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
 * The Original Software is the Accelerators module.
 * The Initial Developer of the Original Software is Andrei Badea.
 * Portions Copyright 2005-2006 Andrei Badea.
 * All Rights Reserved.
 *
 * Contributor(s): Andrei Badea
 *                 Petr Hrebejk
 */

package org.netbeans.modules.jumpto.file;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author Andrei Badea, Petr Hrebejk
 */
public class FileSearchOptions  {
        
    private static final String CASE_SENSITIVE = "caseSensitive"; // NOI18N
    private static final String SHOW_HIDDEN_FILES = "showHiddenFiles"; // NOI18N 
    private static final String PREFER_MAIN_PROJECT = "preferMainProject"; // NOI18N    
    private static final String WIDTH = "width"; // NOI18N
    private static final String HEIGHT = "height"; // NOI18N

    private static Preferences node;

    public static boolean getCaseSensitive() {
        return getNode().getBoolean(CASE_SENSITIVE, false);
    }

    public static void setCaseSensitive( boolean caseSensitive) {
        getNode().putBoolean(CASE_SENSITIVE, caseSensitive);
    }
    
    public static boolean getShowHiddenFiles() {
        return getNode().getBoolean(SHOW_HIDDEN_FILES, false);
    }

    public static void setShowHiddenFiles( boolean showHiddenFiles) {
        getNode().putBoolean(SHOW_HIDDEN_FILES, showHiddenFiles);
    }

    public static boolean getPreferMainProject() {
        return getNode().getBoolean(PREFER_MAIN_PROJECT, true);
    }

    public static void setPreferMainProject( boolean preferMainProject) {
        getNode().putBoolean(PREFER_MAIN_PROJECT, preferMainProject);
    }
    
    public static int getHeight() {
        return getNode().getInt(HEIGHT, 460);
    }

    public static void setHeight( int height ) {
        getNode().putInt(HEIGHT, height);
    }

    public static int getWidth() {
        return getNode().getInt(WIDTH, 740);
    }

    public static void setWidth( int width ) {
        getNode().putInt(WIDTH, width);
    }

    static void flush() {
        try {
            getNode().flush();
        }
        catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private static synchronized Preferences getNode() {
        if ( node == null ) {                
            node = NbPreferences.forModule(FileSearchOptions.class);
        }
        return node;
    }
    
}
