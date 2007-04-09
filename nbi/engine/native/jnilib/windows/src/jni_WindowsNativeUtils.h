/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_netbeans_installer_utils_system_WindowsNativeUtils */

#ifndef _Included_org_netbeans_installer_utils_system_WindowsNativeUtils
#define _Included_org_netbeans_installer_utils_system_WindowsNativeUtils

#ifdef __cplusplus
extern "C" {
#endif

#undef org_netbeans_installer_utils_system_WindowsNativeUtils_MIN_UID_INDEX
#define org_netbeans_installer_utils_system_WindowsNativeUtils_MIN_UID_INDEX 1L

#undef org_netbeans_installer_utils_system_WindowsNativeUtils_MAX_UID_INDEX
#define org_netbeans_installer_utils_system_WindowsNativeUtils_MAX_UID_INDEX 100L

/*
 * Class:     org_netbeans_installer_utils_system_WindowsNativeUtils
 * Method:    isCurrentUserAdmin0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_isCurrentUserAdmin0
  (JNIEnv *, jobject);

/*
 * Class:     org_netbeans_installer_utils_system_WindowsNativeUtils
 * Method:    getFreeSpace0
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_getFreeSpace0
  (JNIEnv *, jobject, jstring);

/*
 * Class:     org_netbeans_installer_utils_system_WindowsNativeUtils
 * Method:    createShortcut0
 * Signature: (Lorg/netbeans/installer/utils/SystemUtils/Shortcut;)V
 */
JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_createShortcut0
  (JNIEnv *, jobject, jobject);

/*
 * Class:     org_netbeans_installer_utils_system_WindowsNativeUtils
 * Method:    deleteFileOnReboot0
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_deleteFileOnReboot0
  (JNIEnv *, jobject, jstring);

/*
 * Class:     org_netbeans_installer_utils_system_WindowsNativeUtils
 * Method:    notifyAssociationChanged0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_notifyAssociationChanged0
  (JNIEnv *, jobject);


/*
 * Class:     org_netbeans_installer_utils_system_WindowsNativeUtils
 * Method:    checkAccessTokenAccessLevel0
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_checkAccessTokenAccessLevel0
  (JNIEnv *, jobject, jstring, jint);


/*
 * Class:     org_netbeans_installer_utils_system_WindowsNativeUtils
 * Method:    notifyEnvironmentChanged0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_netbeans_installer_utils_system_WindowsNativeUtils_notifyEnvironmentChanged0
  (JNIEnv *, jobject);


#ifdef __cplusplus
}
#endif

#endif
