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



package org.netbeans.xtest.util;

/**
 *
 * @author  mb115822
 */
public class OSNameMappingTable {
    
    private static final String [][] OSNAMES_MAPPING = { 
        // solaris for sparc
        {"Solaris 2.6","SunOS","5.6","sparc"},
        {"Solaris 7","SunOS","5.7","sparc"},
        {"Solaris 8","SunOS","5.8","sparc"},
        {"Solaris 9","SunOS","5.9","sparc"},
        // solaris for intel
        {"Solaris 2.6 x86","SunOS","5.6","x86"},
        {"Solaris 7 x86","SunOS","5.7","x86"},
        {"Solaris 8 x86","SunOS","5.8","x86"},
        {"Solaris 9 x86","SunOS","5.9","x86"},
        // linux (redhat)
        {"RedHat Linux 7.2","Linux","2.4.7-10","i386"},
        {"RedHat Linux 7.2","Linux","2.4.7-10","x86"},
        // linux (Sun Linux)
        {"Sun Linux 5.0","Linux","2.4.9-31enterprise","i386"},        
        // Windows NT 4
        {"Windows NT 4.0","Windows NT","4.0","x86"},
        // Windows 2000
        {"Windows 2000","Windows 2000","5.0","x86"},
        // Windows XP
        {"Windows XP","Windows 2000","5.1","x86"},
        {"Windows XP","Windows XP","5.1","x86"}                
    };
    
    public static final String UNKNOWN_OS = "Unknown";
    
    /** static methods class */
    private OSNameMappingTable() {
    }
    
    public static String getFullOSName(String osName, String osVersion, String osArch) {
        for (int i=0; i < OSNAMES_MAPPING.length; i++) {
            String[] mappingRow = OSNAMES_MAPPING[i];
            if (mappingRow[1].equalsIgnoreCase(osName)) {
                if (mappingRow[2].equalsIgnoreCase(osVersion)) {
                    if (mappingRow[3].equalsIgnoreCase(osArch)) {
                        return  mappingRow[0];
                    }
                }
            }
        }
        // return unknown
        return UNKNOWN_OS;
    }
    
}
