/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.javadoc.search;

import javax.swing.ImageIcon;

import org.openide.util.Utilities;

/** <DESCRIPTION>

 @author Petr Hrebejk
*/
class DocSearchIcons extends Object {

    public static final int ICON_NOTRESOLVED = 0;
    public static final int ICON_PACKAGE = ICON_NOTRESOLVED + 1 ;
    public static final int ICON_CLASS = ICON_PACKAGE + 1 ;
    public static final int ICON_INTERFACE = ICON_CLASS + 1;
    public static final int ICON_EXCEPTION = ICON_INTERFACE + 1;
    public static final int ICON_ERROR = ICON_EXCEPTION + 1;
    public static final int ICON_CONSTRUCTOR = ICON_ERROR + 1;
    public static final int ICON_METHOD = ICON_CONSTRUCTOR + 1;
    public static final int ICON_METHOD_ST = ICON_METHOD + 1;
    public static final int ICON_VARIABLE = ICON_METHOD_ST + 1;
    public static final int ICON_VARIABLE_ST = ICON_VARIABLE + 1;
    public static final int ICON_NOT_FOUND = ICON_VARIABLE_ST + 1;
    public static final int ICON_WAIT = ICON_NOT_FOUND + 1;

    private static ImageIcon[] icons = new ImageIcon[ ICON_WAIT + 1 ];

    static {
        try {
            icons[ ICON_NOTRESOLVED ] = new ImageIcon (Utilities.loadImage("org/openide/resources/pending.gif")); // NOI18N
            icons[ ICON_PACKAGE ] = new ImageIcon (Utilities.loadImage ("org/openide/resources/defaultFolder.gif")); // NOI18N
            icons[ ICON_CLASS ] = new ImageIcon (Utilities.loadImage ("org/openide/resources/src/class.gif")); // NOI18N
            icons[ ICON_INTERFACE ] = new ImageIcon (Utilities.loadImage ("org/openide/resources/src/interface.gif")); // NOI18N
            icons[ ICON_EXCEPTION ] = new ImageIcon (Utilities.loadImage ("org/netbeans/modules/javadoc/resources/exception.gif")); // NOI18N
            icons[ ICON_ERROR ] = new ImageIcon (Utilities.loadImage ("org/netbeans/modules/javadoc/resources/error.gif")); // NOI18N
            icons[ ICON_CONSTRUCTOR ] = new ImageIcon (Utilities.loadImage ("org/openide/resources/src/constructorPublic.gif")); // NOI18N
            icons[ ICON_METHOD ] = new ImageIcon (Utilities.loadImage ("org/openide/resources/src/methodPublic.gif")); // NOI18N
            icons[ ICON_METHOD_ST ] = new ImageIcon (Utilities.loadImage ("org/openide/resources/src/methodStPublic.gif")); // NOI18N
            icons[ ICON_VARIABLE ] = new ImageIcon (Utilities.loadImage ("org/openide/resources/src/variablePublic.gif")); // NOI18N
            icons[ ICON_VARIABLE_ST ] = new ImageIcon (Utilities.loadImage ("org/openide/resources/src/variableStPublic.gif")); // NOI18N
            icons[ ICON_NOT_FOUND ] = new ImageIcon (Utilities.loadImage ("org/netbeans/modules/javadoc/resources/notFound.gif")); // NOI18N
            icons[ ICON_WAIT ] = new ImageIcon (Utilities.loadImage ("org/openide/resources/src/wait.gif")); // NOI18N
        }
        catch (Throwable w) {
            w.printStackTrace ();
        }
    }

    static ImageIcon getIcon( int index ) {
        return icons[ index ];
    }

}
