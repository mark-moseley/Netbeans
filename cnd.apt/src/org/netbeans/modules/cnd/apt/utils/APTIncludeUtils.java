/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.apt.utils;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;

/**
 *
 * @author Vladimir Voskresensky
 */
public class APTIncludeUtils {
    
    private APTIncludeUtils() {
    }
    
    /** 
     * finds file relatively to the baseFile 
     * caller must check that resolved path is not the same as base file
     * to prevent recursive inclusions 
     */
    public static String resolveFilePath(String file, String baseFile) {
        if (baseFile != null) {
            File fileFromBasePath = new File(new File(baseFile).getParent(), file);
            if (!isDirectory(fileFromBasePath) && exists(fileFromBasePath)) {
                return fileFromBasePath.getAbsolutePath();
            }
        }
        return null;
    }
    
//    /** 
//     * finds file in a list of paths 
//     * if next param is true look for the file itself, skip it and use next match
//     * caller must check that resolved path is not the same as base file
//     * to prevent recursive inclusions 
//     */
//    public static String resolveFilePath(String file, Collection paths, String baseFile, boolean next) {
//        boolean stopOnResolved = !next;
//        for (Iterator it = paths.iterator(); it.hasNext();) {
//            String sysPrefix = (String) it.next();
//            File fileFromPath = new File(new File(sysPrefix), file);
//            if (exists(fileFromPath)) {
//                String path = fileFromPath.getAbsolutePath();
//                if (stopOnResolved) {
//                    return path;
//                } else {
//                    if (path.equalsIgnoreCase(baseFile)) {
//                        stopOnResolved = true;
//                    }
//                }
//            }
//        }
//        return null;
//    }  
    
    /** 
     * finds file in a list of paths 
     * if next param is true look for the file itself, skip it and use next match
     * caller must check that resolved path is not the same as base file
     * to prevent recursive inclusions 
     */
    public static String resolveFilePath(String file, Collection paths, String baseFile, boolean next) {
        Iterator it = paths.iterator();
        if( next ) {
            String baseFilePath = new File(baseFile).getParent();
            while( it.hasNext() ) {
                String sysPrefix = (String) it.next();
                if( sysPrefix.equals(baseFilePath) ) {
                    break;
                }
            }
        }
        return resolveFilePath(it, file);
    }  
    
    public static void dispose() {
        mapRef.clear();
        mapFoldersRef.clear();
    }
    
    private static String resolveFilePath(Iterator it, String file) {
        if (APTTraceFlags.APT_ABSOLUTE_INCLUDES) {
            File absFile = new File(file);
            if (absFile.isAbsolute() && exists(absFile)) {
                return absFile.getAbsolutePath();
            }
        }
        while( it.hasNext() ) {
            String sysPrefix = (String) it.next();
            File fileFromPath = new File(new File(sysPrefix), file);
            if (!isDirectory(fileFromPath) && exists(fileFromPath)) {
                return fileFromPath.getAbsolutePath();
            }
        }
        return null;
    }
    
    private static boolean exists(File file) {
        if( APTTraceFlags.OPTIMIZE_INCLUDE_SEARCH ) {
            //calls++;
            String path = file.getAbsolutePath();
            Boolean exists;
            synchronized (mapRef) {
                Map/*<File, Boolean>*/ files = getFilesMap();
                exists = (Boolean) files.get(path);
                if( exists == null ) {
                    exists = Boolean.valueOf(file.exists());
                    files.put(FilePathCache.getString(path), exists);
                } else {
                    //hits ++;
                }
            }
            return exists.booleanValue();
        } else {
            return file.exists();
        }
    }
    
    private static boolean isDirectory(File file) {
        if( APTTraceFlags.OPTIMIZE_INCLUDE_SEARCH ) {
            //calls++;
            String path = file.getAbsolutePath();
            Boolean exists;
            synchronized (mapFoldersRef) {
                Map/*<File, Boolean>*/ dirs = getFoldersMap();                
                exists = (Boolean) dirs.get(path);
                if( exists == null ) {
                    exists = Boolean.valueOf(file.isDirectory());
                    dirs.put(FilePathCache.getString(path), exists);
                } else {
                    //hits ++;
                }
            }
            return exists.booleanValue();
        } else {
            return file.isDirectory();
        }
    }
    
//    public static String getHitRate() {
//	return "" + hits + "/" + calls; // NOI18N
//    }   
//    private static int calls = 0;
//    private static int hits = 0;

    private static Map/*<File, Boolean>*/ getFilesMap() {
        Map/*<File, Boolean>*/ map = (Map/*<File, Boolean>*/) mapRef.get();
        if( map == null ) {
            map = new HashMap/*<File, Boolean>*/();
            mapRef = new SoftReference(map);
        }
        return map;
    }
    
    private static Map/*<File, Boolean>*/ getFoldersMap() {
        Map/*<File, Boolean>*/ map = (Map/*<File, Boolean>*/) mapFoldersRef.get();
        if( map == null ) {
            map = new HashMap/*<File, Boolean>*/();
            mapFoldersRef = new SoftReference(map);
        }
        return map;
    }  
    
    private static SoftReference mapRef = new SoftReference(new HashMap/*<File, Boolean>*/());
    //private static Map/*<File, Boolean>*/ files = new HashMap/*<File, Boolean>*/();
    private static SoftReference mapFoldersRef = new SoftReference(new HashMap/*<File, Boolean>*/());
}
