/*
 * Copyright 2005, 2007 Nokia Corporation. All rights reserved.
 *  
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). See LICENSE.TXT for exact terms.
 * You may not use this file except in compliance with the License.  You can obtain a copy of the
 * License at http://www.netbeans.org/cddl.html
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.nokia.phone.deploy;

/**
 * This class offers interface for the native methods in CONA API. CONA api
 * offers Application package deployment to Terminal either by using BT, IrDA,
 * Serial or USB communication. Native methods declared in this class available
 * in the ConnJNI.dll. The ConnJNI.dll will itself use methods available in
 * ConnAPI.dll that has been taken from PC Suite Connectivity API 1.1 which
 * is freely downloadable from Forum Nokia.
 * 
 */
public class CONA {
    // Connection Media Types:
    public static final int CONAPI_MEDIA_ALL              = 0x01;
    public static final int CONAPI_MEDIA_IRDA             = 0x02;
    public static final int CONAPI_MEDIA_SERIAL           = 0x04;
    public static final int CONAPI_MEDIA_BLUETOOTH        = 0x08;
    public static final int CONAPI_MEDIA_USB              = 0x10;


    // The next define values used to define which type of struct is used:
    public static final int CONA_APPLICATION_TYPE_SIS     = 0x00000001;   // Use when struct type is CONAPI_APPLICATION_SIS
    public static final int CONA_APPLICATION_TYPE_JAVA    = 0x00000002;   // Use when struct type is CONAPI_APPLICATION_JAVA


    // FSFunction values:
    public static final int CONARefreshDeviceMemoryValues = 0x00000001;
    public static final int CONASetCurrentFolder          = 0x00000002;   // also used for state type
    public static final int CONAFindBegin                 = 0x00000004;
    public static final int CONACreateFolder              = 0x00000008;   // also used for state type
    public static final int CONADeleteFolder              = 0x00000010;
    public static final int CONARenameFolder              = 0x00000020;
    public static final int CONAGetFileInfo               = 0x00000040;
    public static final int CONADeleteFile                = 0x00000080;
    public static final int CONAMoveFile                  = 0x00000100;
    public static final int CONACopyFile                  = 0x00000200;   // also used for state type
    public static final int CONARenameFile                = 0x00000400;
    public static final int CONAReadFile                  = 0x00000800;
    public static final int CONAWriteFile                 = 0x00001000;
    public static final int CONAConnectionLost            = 0x00002000;
    public static final int CONAInstallApplication        = 0x00004000;   // also used for state type


    private static CONA     instance                      = null;
    private static boolean  connAPIdllFound                      = false;
    private static boolean  connJNIdllFound                      = false;
    
    private static boolean OSSupportsDeployment = true;

    /**
     * Try to load the native library only if running in a Windows environment.
     */
    static {
        if (!System.getProperty("os.name").toLowerCase().startsWith("windows")) { //$NON-NLS-1$ //$NON-NLS-2$
            OSSupportsDeployment = false;
        }
    }

    private CONA() {}

    public static CONA getInstance() {
        if (instance == null) {
            instance = new CONA();
            try {
                // ConnJNI.dll is linked so that is presumes that ConnAPI.dll is
                // located
                // in the start up folder. We must load the ConnAPI.dll to
                // memory before
                // loading the ConnJNI.dll.
                System.loadLibrary("ConnAPI"); // Installed in a system
                                                // directory by PC-Suite
                connAPIdllFound = true;
            } catch (UnsatisfiedLinkError ulError) {
                ulError.printStackTrace();
                connAPIdllFound = false;
            } catch (Exception ex) {
                ex.printStackTrace();
                connAPIdllFound = false;
            }

            try {
                
                System.loadLibrary("ConnJNI");

                connJNIdllFound = true;
            } catch (UnsatisfiedLinkError ulError) {
                ulError.printStackTrace();
                connAPIdllFound = false;
            } catch (Exception ex) {
                ex.printStackTrace();
                connAPIdllFound = false;
            }
        }

        return instance;
    }

    public boolean isConnAPIDllFound() {
        return connAPIdllFound;
    }

    public boolean isConnJNIDllFound() {
        return connAPIdllFound;
    }

    public boolean isOSSupportsDeployment() {
        return OSSupportsDeployment;
    }

    // Device callback macros
    public static int getConnapiStatus(int status) {
        return (0x0000FFFF & status);
    }

    public static int getConnapiInfo(int status) {
        return ((0x00FF0000 & status) >> 16);
    }

    public static int getConnapiInfoData(int status) {
        return ((0xFF000000 & status) >> 24);
    }

    /**
     * This method is called from native code to inform of device operations i.e.
     * device connect, device disconnect.
     * Nothing needed to be done in this implementation.
     */
    public void fireDeviceNotify(int status, int deviceId) {
        // do nothing
    }
    
    /**
     * This method is called from native code to inform of file operations.
     * Nothing needed to be done in this implementation.
     */
    public void fireFileOperationNotify(int function, int state,
                                        int transferredBytes, int allBytes) {
        // do nothing
    }

    /*
     * Native methods available in the ConnJNI.dll.  ConnJNI.dll will
     * itself require ConnAPI.dll that has been taken from PC Suite.
     */
    private native String native_getVersion();
    private native boolean native_connectServiceLayer();
    private native boolean native_updateDeviceList();
    private native String native_getDeviceType(int id);
    private native String native_getDevices(int mediaType);
    private native boolean native_openConnectionTo(int id);
    private native boolean native_installFile(String filepath, String filename, String jad, int filetype, boolean defaultfolder);
    private native boolean native_setCurrentFolder(String folder);
    private native boolean native_createFolder(String folder);
    private native boolean native_putFile(String srcPath, String dstPath, String name);
    private native int native_getStatus(int type);
    private native boolean native_closeConnection();
    private native boolean native_disconnectServiceLayer();

    public String getVersion() {
        if (connAPIdllFound && connJNIdllFound) {
            return native_getVersion();
        }

        return null;
    }

    /**
     * Returns device media type: one of: {"IRDA", "RS232", "BLUETOOTH", "USB", NULL};
     * id is device ID.
     */
    public String getDeviceType(int id) {
        if (connAPIdllFound && connJNIdllFound) {
            return native_getDeviceType(id);
        }

        return null;
    }

    public boolean connect() {
        if (connAPIdllFound && connJNIdllFound) {
            return native_connectServiceLayer();
        }

        return false;
    }

    public boolean disconnect() {
        if (connAPIdllFound && connJNIdllFound) {
            return native_disconnectServiceLayer();
        }

        return false;
    }

    public boolean updateDeviceList() {
        if (connAPIdllFound && connJNIdllFound) {
            return native_updateDeviceList();
        }

        return false;
    }

    /**
     * Returns comma-separated list of device names/IDs.
     * MediaType is one of {CONAPI_MEDIA_ALL, CONAPI_MEDIA_IRDA, CONAPI_MEDIA_SERIAL, CONAPI_MEDIA_BLUETOOTH, CONAPI_MEDIA_USB}
     * Each device listed with the format: "%s (ID:%i)"
     */
    public String getDevices(int mediaType) {
        if ((mediaType < CONAPI_MEDIA_ALL) || (mediaType > CONAPI_MEDIA_USB)) {
            return null;
        }

        if (connAPIdllFound && connJNIdllFound) {
            return native_getDevices(mediaType);
        }

        return null;
    }

    public boolean installApplication(String filepath, String filename,
                                      String jad, int filetype, boolean defaultFolder) {
        if ((filepath == null) || (filename == null))
            return false;

        if ((filetype != CONA_APPLICATION_TYPE_JAVA)
         && (filetype != CONA_APPLICATION_TYPE_SIS))
            return false;

        if (connAPIdllFound && connJNIdllFound) {
            return native_installFile(filepath, filename, jad, filetype,
                    defaultFolder);
        }

        return false;
    }
    
    // id is device ID
    public boolean openFileSystem(int id) {
        if (connAPIdllFound && connJNIdllFound) {
            return native_openConnectionTo(id);
        }

        return false;
    }

    public boolean closeFileSystem() {
        if (connAPIdllFound && connJNIdllFound) {
            return native_closeConnection();
        }

        return false;
    }
}   // End of CONA