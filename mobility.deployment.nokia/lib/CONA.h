// Copyright 2005, 2007 Nokia Corporation. All rights reserved.
//
// The contents of this file are subject to the terms of the Common
// Development and Distribution License (the License). See LICENSE.TXT for exact terms.
// You may not use this file except in compliance with the License.  You can obtain a copy of the
// License at http://www.netbeans.org/cddl.html
//
// When distributing Covered Code, include this CDDL Header Notice in each
// file and include the License. If applicable, add the following below the
// CDDL Header, with the fields enclosed by brackets [] replaced by your own
// identifying information:
// "Portions Copyrighted [year] [name of copyright owner]"

/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_nokia_phone_deploy_CONA */

#ifndef _Included_com_nokia_phone_deploy_CONA
#define _Included_com_nokia_phone_deploy_CONA
#ifdef __cplusplus
extern "C" {
#endif
#undef com_nokia_phone_deploy_CONA_CONAPI_MEDIA_ALL
#define com_nokia_phone_deploy_CONA_CONAPI_MEDIA_ALL 1L
#undef com_nokia_phone_deploy_CONA_CONAPI_MEDIA_IRDA
#define com_nokia_phone_deploy_CONA_CONAPI_MEDIA_IRDA 2L
#undef com_nokia_phone_deploy_CONA_CONAPI_MEDIA_SERIAL
#define com_nokia_phone_deploy_CONA_CONAPI_MEDIA_SERIAL 4L
#undef com_nokia_phone_deploy_CONA_CONAPI_MEDIA_BLUETOOTH
#define com_nokia_phone_deploy_CONA_CONAPI_MEDIA_BLUETOOTH 8L
#undef com_nokia_phone_deploy_CONA_CONAPI_MEDIA_USB
#define com_nokia_phone_deploy_CONA_CONAPI_MEDIA_USB 16L
#undef com_nokia_phone_deploy_CONA_CONA_APPLICATION_TYPE_SIS
#define com_nokia_phone_deploy_CONA_CONA_APPLICATION_TYPE_SIS 1L
#undef com_nokia_phone_deploy_CONA_CONA_APPLICATION_TYPE_JAVA
#define com_nokia_phone_deploy_CONA_CONA_APPLICATION_TYPE_JAVA 2L
#undef com_nokia_phone_deploy_CONA_CONARefreshDeviceMemoryValues
#define com_nokia_phone_deploy_CONA_CONARefreshDeviceMemoryValues 1L
#undef com_nokia_phone_deploy_CONA_CONASetCurrentFolder
#define com_nokia_phone_deploy_CONA_CONASetCurrentFolder 2L
#undef com_nokia_phone_deploy_CONA_CONAFindBegin
#define com_nokia_phone_deploy_CONA_CONAFindBegin 4L
#undef com_nokia_phone_deploy_CONA_CONACreateFolder
#define com_nokia_phone_deploy_CONA_CONACreateFolder 8L
#undef com_nokia_phone_deploy_CONA_CONADeleteFolder
#define com_nokia_phone_deploy_CONA_CONADeleteFolder 16L
#undef com_nokia_phone_deploy_CONA_CONARenameFolder
#define com_nokia_phone_deploy_CONA_CONARenameFolder 32L
#undef com_nokia_phone_deploy_CONA_CONAGetFileInfo
#define com_nokia_phone_deploy_CONA_CONAGetFileInfo 64L
#undef com_nokia_phone_deploy_CONA_CONADeleteFile
#define com_nokia_phone_deploy_CONA_CONADeleteFile 128L
#undef com_nokia_phone_deploy_CONA_CONAMoveFile
#define com_nokia_phone_deploy_CONA_CONAMoveFile 256L
#undef com_nokia_phone_deploy_CONA_CONACopyFile
#define com_nokia_phone_deploy_CONA_CONACopyFile 512L
#undef com_nokia_phone_deploy_CONA_CONARenameFile
#define com_nokia_phone_deploy_CONA_CONARenameFile 1024L
#undef com_nokia_phone_deploy_CONA_CONAReadFile
#define com_nokia_phone_deploy_CONA_CONAReadFile 2048L
#undef com_nokia_phone_deploy_CONA_CONAWriteFile
#define com_nokia_phone_deploy_CONA_CONAWriteFile 4096L
#undef com_nokia_phone_deploy_CONA_CONAConnectionLost
#define com_nokia_phone_deploy_CONA_CONAConnectionLost 8192L
#undef com_nokia_phone_deploy_CONA_CONAInstallApplication
#define com_nokia_phone_deploy_CONA_CONAInstallApplication 16384L
/*
 * Class:     com_nokia_phone_deploy_CONA
 * Method:    native_getVersion
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_nokia_phone_deploy_CONA_native_1getVersion
  (JNIEnv *, jobject);

/*
 * Class:     com_nokia_phone_deploy_CONA
 * Method:    native_connectServiceLayer
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_nokia_phone_deploy_CONA_native_1connectServiceLayer
  (JNIEnv *, jobject);

/*
 * Class:     com_nokia_phone_deploy_CONA
 * Method:    native_updateDeviceList
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_nokia_phone_deploy_CONA_native_1updateDeviceList
  (JNIEnv *, jobject);

/*
 * Class:     com_nokia_phone_deploy_CONA
 * Method:    native_getDeviceType
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_nokia_phone_deploy_CONA_native_1getDeviceType
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_nokia_phone_deploy_CONA
 * Method:    native_getDevices
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_nokia_phone_deploy_CONA_native_1getDevices
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_nokia_phone_deploy_CONA
 * Method:    native_openConnectionTo
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_nokia_phone_deploy_CONA_native_1openConnectionTo
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_nokia_phone_deploy_CONA
 * Method:    native_installFile
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IZ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_nokia_phone_deploy_CONA_native_1installFile
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jboolean);

/*
 * Class:     com_nokia_phone_deploy_CONA
 * Method:    native_setCurrentFolder
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_nokia_phone_deploy_CONA_native_1setCurrentFolder
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_nokia_phone_deploy_CONA
 * Method:    native_createFolder
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_nokia_phone_deploy_CONA_native_1createFolder
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_nokia_phone_deploy_CONA
 * Method:    native_putFile
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_nokia_phone_deploy_CONA_native_1putFile
  (JNIEnv *, jobject, jstring, jstring, jstring);

/*
 * Class:     com_nokia_phone_deploy_CONA
 * Method:    native_getStatus
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_nokia_phone_deploy_CONA_native_1getStatus
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_nokia_phone_deploy_CONA
 * Method:    native_closeConnection
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_nokia_phone_deploy_CONA_native_1closeConnection
  (JNIEnv *, jobject);

/*
 * Class:     com_nokia_phone_deploy_CONA
 * Method:    native_disconnectServiceLayer
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_nokia_phone_deploy_CONA_native_1disconnectServiceLayer
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
