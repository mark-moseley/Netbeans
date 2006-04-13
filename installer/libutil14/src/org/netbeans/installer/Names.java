/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.installer;

/**
 * Used for bean ID and property name constants.
 */
public class Names {
    //Product bean IDs
    public static final String PRODUCT_ROOT_ID = "beanProduct";
    public static final String CORE_IDE_ID = "beanCoreIDE";
    public static final String UNPACK_JARS_ID = "beanUnpackJars";
    public static final String STORAGE_BUILDER_ID = "beanStorageBuilder";
    public static final String APP_SERVER_ID = "beanAppServer";
    public static final String J2SE_ID = "beanJ2SE";
    
    //Used to distinguish installers
    public static final String INSTALLER_TYPE = "InstallerType";
    public static final String INSTALLER_NB = "NetBeansInstaller";
    public static final String INSTALLER_AS_BUNDLE = "ASBundleInstaller";
    public static final String INSTALLER_JDK_BUNDLE = "JDKBundleInstaller";
            
    /** Creates a new instance of Names */
    private Names() {
    }
    
}
