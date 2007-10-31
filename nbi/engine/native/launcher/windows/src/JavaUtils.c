/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

#include <wchar.h>
#include <stdio.h>
#include "JavaUtils.h"
#include "RegistryUtils.h"
#include "StringUtils.h"
#include "FileUtils.h"
#include "ProcessUtils.h"
#include "Launcher.h"
#include "Main.h"

const DWORD JAVA_VERIFICATION_PROCESS_TIMEOUT = 10000; // 10sec
const DWORD UNPACK200_EXTRACTION_TIMEOUT = 20000; //20 seconds on each file
const DWORD JAVA_VERIFICATION_PROCESS_PRIORITY = NORMAL_PRIORITY_CLASS;
const WCHAR * JAVA_EXE_SUFFIX = L"\\bin\\java.exe";
const WCHAR * UNPACK200_EXE_SUFFIX = L"\\bin\\unpack200.exe";
const WCHAR * JAVA_LIB_SUFFIX = L"\\lib";
const WCHAR * PACK_GZ_SUFFIX  = L".pack.gz";
const WCHAR * JAR_PACK_GZ_SUFFIX = L".jar.pack.gz";

const DWORD JVM_EXTRACTION_TIMEOUT = 60000;  //60sec

WCHAR * JAVA_REGISTRY_KEYS [] = {
    L"SOFTWARE\\JavaSoft\\Java Runtime Environment",
    L"SOFTWARE\\JavaSoft\\Java Development Kit",
    L"SOFTWARE\\JRockit\\Java Runtime Environment",
    L"SOFTWARE\\JRockit\\Java Development Kit",
    L"SOFTWARE\\IBM\\Java Runtime Environment",
    L"SOFTWARE\\IBM\\Java2 Runtime Environment",
    L"SOFTWARE\\IBM\\Java Development Kit",
};
WCHAR * JAVA_HOME = L"JavaHome";
WCHAR * CURRENT_VERSION = L"CurrentVersion";

WCHAR * getJavaHomeValue(WCHAR *parentkey, WCHAR *subkey) {
    return getStringValuePC(HKEY_LOCAL_MACHINE, parentkey, subkey, JAVA_HOME);
}


WCHAR * getTestJVMFileName(WCHAR * testJVMFile) {
    WCHAR * filePtr = testJVMFile;
    WCHAR * testJavaClass = NULL;
    
    if(filePtr!=NULL) {
        WCHAR * dotClass = NULL;
        while(wcsstr(filePtr, L"\\")!=NULL) {
            filePtr = wcsstr(filePtr, L"\\");
            filePtr++;
        }
        dotClass = wcsstr(filePtr, L".class");
        
        if(dotClass!=NULL) {
            testJavaClass = appendStringNW(NULL, 0, filePtr, getLengthW(filePtr) - getLengthW(dotClass));
        }
    }
    return testJavaClass;
}


//returns 0 if equals, 1 if first > second, -1 if first < second
char compareJavaVersion(JavaVersion * first, JavaVersion * second) {
    if(first==NULL) return (second==NULL) ? 0 : -1;
    if(second==NULL) return -1;
    if(first->major == second->major) {
        if(first->minor == second->minor) {
            if(first->micro == second->micro) {
                if(first->update == second->update) return 0;
                return (first->update > second->update) ? 1 : -1;
            }
            return (first->micro > second->micro) ? 1 : -1;
        }
        return (first->minor > second->minor) ? 1 : -1;
    } else {
        return (first->major > second->major) ? 1 : -1;
    }
}
DWORD isJavaCompatible(JavaProperties *currentJava, JavaCompatible ** compatibleJava, DWORD number) {
    JavaVersion * current = currentJava->version;
    DWORD i = 0 ;
    
    for(i=0;i<number;i++) {
        DWORD check = 1;
        
        check = (compareJavaVersion(current, compatibleJava[i]->minVersion) >= 0 &&
                compareJavaVersion(current, compatibleJava[i]->maxVersion) <= 0) ? check : 0;
                
                if (check) {
                    if(compatibleJava[i]->vendor!=NULL) {
                        check = (strstr(currentJava->vendor, compatibleJava[i]->vendor) != NULL) ? check : 0;
                    }
                    if (compatibleJava[i]->osName!=NULL) {
                        check = (strstr(currentJava->osName, compatibleJava[i]->osName)!=NULL) ? check : 0;
                    }
                    
                    if (compatibleJava[i]->osArch!=NULL) {
                        check = (strstr(currentJava->osArch, compatibleJava[i]->osArch)!=NULL) ? check : 0;
                    }
                    if(check) {
                        return 1;
                    }
                }
    }
    return 0;
}

JavaVersion * getJavaVersionFromString(char * string, DWORD * result) {
    JavaVersion *vers = NULL;
    if(getLengthA(string)>=3) {
        //hope that at least we "major.minor" : 1.5
        if(string[1]=='.') {
            char c = string[0];
            if(c>='0' && c<='9') {
                long major = c - '0';
                c = string[2];
                if(c>='0' && c<='9') {
                    char *p = string + 3;
                    long minor = c - '0';
                    *result = ERROR_OK;
                    vers = (JavaVersion*) LocalAlloc(LPTR, sizeof(JavaVersion));
                    vers->major  = major;
                    vers->minor  = minor;
                    vers->micro  = 0;
                    vers->update = 0;
                    ZERO(vers->build, 128);
                    if(p!=NULL) {
                        if(p[0]=='.') { // micro...
                            p++;
                            while(p!=NULL) {
                                char c = p[0];
                                if(c>='0' && c<='9') {
                                    vers->micro = (vers->micro) * 10 + c - '0';
                                    p++;
                                    continue;
                                }
                                else if(c=='_') {//update
                                    p++;
                                    while(p!=NULL) {
                                        c = p[0];
                                        p++;
                                        if(c>='0' && c<='9') {
                                            vers->update = (vers->update) * 10 + c - '0';
                                            continue;
                                        } else {
                                            break;
                                        }
                                    }
                                } else {
                                    if(p!=NULL) p++;
                                }
                                if(c=='-' && p!=NULL) { // build number
                                    lstrcpyn(vers->build, p, min(127, getLengthA(p)));
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
    return vers;
}

DWORD getJavaPropertiesFromOutput(LauncherProperties * props, char *str, JavaProperties ** javaProps) {
    DWORD separators = getLineSeparatorNumber(str);
    DWORD result = ERROR_INPUTOUPUT;
    * javaProps = NULL;
    if(separators == TEST_JAVA_PARAMETERS) {
        char * start;
        char * end;
        char * javaVersion;
        char * javaVmVersion;
        char * javaVendor;
        char * osName;
        char * osArch;
        char * string;
        JavaVersion * vers;
        
        start = str;
        end = strstr(start, "\n");
        
        javaVersion = appendStringN(NULL, 0, start, getLengthA(start) - getLengthA(end)-1);
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "    java.version =  ", 0);
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, javaVersion, 1);
        start = end + 1;
        end = strstr(start, "\n");
        
        
        javaVmVersion = appendStringN(NULL, 0, start, getLengthA(start) - getLengthA(end)-1);
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "    java.vm.version = ", 0);
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, javaVmVersion, 1);
        start = end + 1;
        end = strstr(start, "\n");
        
        javaVendor = appendStringN(NULL, 0, start, getLengthA(start) - getLengthA(end)-1);
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "    java.vendor = ", 0);
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, javaVendor, 1);
        start = end + 1;
        end = strstr(start, "\n");
        
        osName = appendStringN(NULL, 0, start, getLengthA(start) - getLengthA(end)-1);
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "    os.name = ", 0);
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, osName, 1);
        start = end + 1;
        end = strstr(start, "\n");
        
        osArch = appendStringN(NULL, 0, start, getLengthA(start) - getLengthA(end)-1);
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "    os.arch = ", 0);
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, osArch, 2);
        
        string = javaVersion;
        
        
        if(javaVmVersion!=NULL) {
            string = strstr(javaVmVersion, javaVersion);
            if(string==NULL) {
                string = javaVersion;
            }
        }
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... getting java version from string : ", 0);
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, string, 1);
        
        vers = getJavaVersionFromString(string, & result);
        if(javaProps != NULL) {
            writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... some java there", 1);
            * javaProps = (JavaProperties *) LocalAlloc(LPTR, sizeof(JavaProperties));
            (*javaProps)->version = vers;
            (*javaProps)->vendor   = javaVendor;
            (*javaProps)->osName   = osName;
            (*javaProps)->osArch   = osArch;
            (*javaProps)->javaHome = NULL;
            (*javaProps)->javaExe  = NULL;
        } else {
            writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... no java  there", 1);
            FREE(javaVendor);
            FREE(osName);
            FREE(osArch);
        }
        FREE(javaVmVersion);
        FREE(javaVersion);
    }
    return result;
}


void getJavaProperties(WCHAR * location, LauncherProperties * props, JavaProperties ** javaProps) {
    WCHAR *testJavaClass  = props->testJVMClass;
    WCHAR *javaExecutable = getJavaResource(location, JAVA_EXE_SUFFIX);
    WCHAR *libDirectory   = getJavaResource(location, JAVA_LIB_SUFFIX);
    
    if(fileExists(javaExecutable) && testJavaClass!=NULL && isDirectory(libDirectory)) {
        WCHAR * command = NULL;
        HANDLE hRead;
        HANDLE hWrite;
        
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... java hierarchy there", 1);
        // <location>\bin\java.exe exists
        
        
        appendCommandLineArgument(&command, javaExecutable);
        appendCommandLineArgument(&command, L"-classpath");
        appendCommandLineArgument(&command, props->testJVMFile->resolved);
        appendCommandLineArgument(&command, testJavaClass);
        
        
        CreatePipe(&hRead, &hWrite, NULL, 0);
        // Start the child process.
        executeCommand(props, command, NULL, JAVA_VERIFICATION_PROCESS_TIMEOUT, hWrite, INVALID_HANDLE_VALUE, JAVA_VERIFICATION_PROCESS_PRIORITY);
        if(props->status!= ERROR_ON_EXECUTE_PROCESS && props->status!= ERROR_PROCESS_TIMEOUT) {
            char * output = readHandle(hRead);
            writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "           output :\n", 0);
            writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, output, 1);
            
            props->status = getJavaPropertiesFromOutput(props, output, javaProps);
            if(props->status == ERROR_OK) {
                (*javaProps)->javaHome = appendStringW(NULL, location);
                (*javaProps)->javaExe  = appendStringW(NULL, javaExecutable);
            }
            FREE(output);
        } else if(props->status == ERROR_PROCESS_TIMEOUT) {
            // java verification process finished by time out
            props->status = ERROR_INPUTOUPUT;
        }
        FREE(command);
        CloseHandle(hWrite);
        CloseHandle(hRead);
    } else {
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... not a java hierarchy", 1);
        props->status = ERROR_INPUTOUPUT;
    }
    FREE(libDirectory);
    FREE(javaExecutable);
}



char * getJavaVersionFormatted(const JavaProperties * javaProps) {
    char *result = NULL;
    if(javaProps!=NULL) {
        JavaVersion * version = javaProps->version;
        if(version!=NULL) {
            result = newpChar(256);
            sprintf(result, "%ld.%ld.%ld",  version->major, version->minor, version->micro);
            
            if(version->update!=0) {
                sprintf(result, "%s_%02ld", result, version->update);
            }
            if(strncmp(version->build, "", 127)!=0) {
                sprintf(result, "%s-%s", result, version->build);
            }
        }
    }
    return result;
}


JavaCompatible * newJavaCompatible() {
    JavaCompatible * props = (JavaCompatible *) LocalAlloc(LPTR, sizeof(JavaCompatible));
    props->minVersion = NULL;
    props->maxVersion = NULL;
    props->vendor = NULL;
    props->osName = NULL;
    return props;
}

void freeJavaProperties(JavaProperties ** props) {
    if(*props!=NULL) {
        FREE((*props)->version);
        FREE((*props)->javaHome);
        FREE((*props)->javaExe);
        FREE((*props)->vendor);
        FREE(*props);
    }
}

WCHAR * getJavaResource(WCHAR * location, const WCHAR * suffix) {
    return appendStringW(appendStringW(NULL, location), suffix);
}


void searchCurrentJavaRegistry(LauncherProperties * props) {
    DWORD i=0;
    WCHAR ** keys = JAVA_REGISTRY_KEYS;
    DWORD k=0;
    WCHAR buffer [MAX_LEN_VALUE_NAME];
    HKEY rootKeys [2] = {HKEY_LOCAL_MACHINE, HKEY_CURRENT_USER};
    DWORD rootKeysNumber = sizeof(rootKeys)/sizeof(HKEY);
    DWORD keysNumber = sizeof(JAVA_REGISTRY_KEYS)/sizeof(WCHAR*);
    DWORD status = ERROR_OK;
    
    writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "Search java in CurrentVersion values...", 1);
    
    
    for ( k = 0; k < rootKeysNumber; k++) {
        for(i=0; i < keysNumber;i++) {
            if(isTerminated(props)) {
                return;
            }
            else {
                
                WCHAR * value = getStringValue(rootKeys[k], keys[i], CURRENT_VERSION);
                if(value!=NULL) {
                    WCHAR *javaHome = getStringValuePC(rootKeys[k], keys[i], value, JAVA_HOME);
                    writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "... ", 0);
                    writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, (rootKeys[k]==HKEY_LOCAL_MACHINE) ? "HKEY_LOCAL_MACHINE" : "HKEY_CURRENT_USER", 0);
                    writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "\\", 0);
                    writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, keys[i], 0);
                    writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "\\", 0);
                    writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, CURRENT_VERSION, 0);
                    writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "->", 0);
                    writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, value, 0);
                    writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "[", 0);
                    writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, JAVA_HOME, 0);
                    writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "] = ", 0);
                    writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, javaHome, 1);
                    
                    FREE(value);
                    trySetCompatibleJava(javaHome, props);
                    FREE(javaHome);
                    if(props->java!=NULL) {
                        return;
                    }
                }
            }
        }
    }
    
    
    // we found no CurrentVersion java... just search for other possible keys
    writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "Search java in other values...", 1);
    
    
    for(k=0;k<rootKeysNumber;k++) {
        for(i=0;i<keysNumber;i++) {
            HKEY  hkey = 0;
            DWORD   index  = 0 ;
            if (RegOpenKeyExW(rootKeys[k], keys[i], 0, KEY_READ, &hkey) == ERROR_SUCCESS) {
                DWORD number = 0;
                if (RegQueryInfoKeyW(hkey, NULL, NULL, NULL, &number, NULL, NULL, NULL, NULL, NULL, NULL, NULL) == ERROR_SUCCESS) {
                    DWORD err = 0;
                    do {
                        
                        DWORD size = MAX_LEN_VALUE_NAME;
                        buffer[0]  = 0;
                        err = RegEnumKeyExW(hkey, index, buffer, &size, NULL, NULL, NULL, NULL);
                        if (err == ERROR_SUCCESS) {
                            WCHAR  * javaHome = getJavaHomeValue(keys[i], buffer);
                            status = ERROR_OK;
                            
                            writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, (rootKeys[k]==HKEY_LOCAL_MACHINE) ? "HKEY_LOCAL_MACHINE" : "HKEY_CURRENT_USER", 0);
                            writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "\\", 0);
                            writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, keys[i], 0);
                            writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "\\", 0);
                            writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, buffer, 0);
                            writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "[", 0);
                            writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, JAVA_HOME, 0);
                            writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "] = ", 0);
                            writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, javaHome, 1);
                            
                            trySetCompatibleJava(javaHome, props);
                            FREE(javaHome);
                            if(props->java!=NULL) {
                                i = keysNumber; // to the end of cycles
                                k = rootKeysNumber;
                                break;
                            }
                            
                        }
                        index++;
                    } while (err == ERROR_SUCCESS);
                }
            }
            if (hkey != 0) {
                RegCloseKey(hkey);
            }
        }
    }
    return;
}


void searchJavaFromEnvVariables(LauncherProperties * props) {
    static WCHAR * ENVS [] = {
        L"JAVA_HOME",
        L"JAVAHOME",
        L"JAVA_PATH",
        L"JDK_HOME",
        L"JDKHOME",
        L"ANT_JAVA",
        L"JAVA",
        L"JDK"
    };
    
    WCHAR buffer [MAX_PATH];
    
    int size = sizeof(ENVS)/sizeof(WCHAR *);
    int i=0;
    int ret;
    
    for(i=0;i<size;i++) {
        if(isTerminated(props)) return;
        buffer[0]='\0';
        ret = GetEnvironmentVariableW((WCHAR *) ENVS[i], (WCHAR *) buffer, MAX_PATH);
        if (ret > 0 && ret <= MAX_PATH) {
            writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "    <", 0);
            writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, ENVS[i], 0);
            writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "> = ", 0);
            writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, buffer, 1);
            trySetCompatibleJava(buffer, props);
            if(props->java!=NULL) {
                break;
            }
        }
    }
}




void unpackJars(LauncherProperties * props, WCHAR * jvmDir, WCHAR * startDir, WCHAR * unpack200exe) {
    DWORD attrs;
    DWORD dwError;
    DWORD count = 0 ;
    
    if(!isOK(props)) return;
    attrs = GetFileAttributesW(startDir);
    if(attrs==INVALID_FILE_ATTRIBUTES) {
        writeErrorA(props, OUTPUT_LEVEL_DEBUG, 1, "Error! Can`t get attributes of the file : ", startDir, GetLastError());
        return;
    }
    if(attrs & FILE_ATTRIBUTE_DIRECTORY) { // is directory
        WIN32_FIND_DATAW FindFileData;
        HANDLE hFind = INVALID_HANDLE_VALUE;
        
        WCHAR * DirSpec = appendStringW(appendStringW(NULL, startDir), L"\\*" );
        
        // Find the first file in the directory.
        hFind = FindFirstFileW(DirSpec, &FindFileData);
        
        if (hFind == INVALID_HANDLE_VALUE) {
            writeErrorA(props, OUTPUT_LEVEL_DEBUG, 1, "Error! Can`t file with pattern ", DirSpec, GetLastError());
        }
        else {
            // List all the other files in the directory.
            writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... listing directory ", 0);
            writeMessageW(props, OUTPUT_LEVEL_DEBUG, 0, startDir, 1);
            
            while (FindNextFileW(hFind, &FindFileData) != 0 && isOK(props)) {
                if(lstrcmpW(FindFileData.cFileName, L".")!=0 &&
                        lstrcmpW(FindFileData.cFileName, L"..")!=0) {
                    WCHAR * child = NULL;
                    writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... next item : ", 0);
                    writeMessageW(props, OUTPUT_LEVEL_DEBUG, 0, FindFileData.cFileName, 1);
                    child = appendStringW(appendStringW(appendStringW(NULL, startDir), FILE_SEP), FindFileData.cFileName);
                    if(isDirectory(child)) {
                        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... is directory ", 1);
                        unpackJars(props, jvmDir, child, unpack200exe);
                    } else  if(wcsstr(FindFileData.cFileName, JAR_PACK_GZ_SUFFIX)!=NULL) {
                        WCHAR * jarName = appendStringW(appendStringW(
                                appendStringW(NULL, startDir), FILE_SEP),
                                appendStringNW(NULL, 0, FindFileData.cFileName,
                                getLengthW(FindFileData.cFileName) - getLengthW(PACK_GZ_SUFFIX)));
                        WCHAR * unpackCommand = NULL;
                        
                        
                        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... it is a packed jar ", 1);
                        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... jar name : ", 0);
                        writeMessageW(props, OUTPUT_LEVEL_DEBUG, 0, jarName, 1);
                        
                        
                        appendCommandLineArgument(&unpackCommand, unpack200exe);
                        appendCommandLineArgument(&unpackCommand, child);
                        appendCommandLineArgument(&unpackCommand, jarName);
                        
                        executeCommand(props, unpackCommand, NULL, UNPACK200_EXTRACTION_TIMEOUT, props->stdoutHandle, props->stderrHandle, NORMAL_PRIORITY_CLASS);
                        FREE(unpackCommand);
                        if(!isOK(props)) {
                            if(props->status==ERROR_PROCESS_TIMEOUT) {
                                writeMessageA(props, OUTPUT_LEVEL_DEBUG, 1, "... could not unpack file : timeout", 1);
                            } else {
                                writeMessageA(props, OUTPUT_LEVEL_DEBUG, 1, "... an error occured unpacking the file", 1);
                            }
                            props->exitCode = props->status;
                        }
                        FREE(jarName);
                    }
                    FREE(child);
                }
            }
            
            dwError = GetLastError();
            FindClose(hFind);
            if (dwError != ERROR_NO_MORE_FILES) {
                writeErrorA(props, OUTPUT_LEVEL_DEBUG, 1, "Error! Can`t find file with pattern : ", DirSpec, dwError);
            }
        }
        FREE(DirSpec);
    }
    
}
void installJVM(LauncherProperties * props, LauncherResource *jvm) {    
    WCHAR * command = NULL;
    WCHAR * jvmDir = getParentDirectory(jvm->resolved);
    
    jvmDir = appendStringW(jvmDir, L"\\_jvm");
    createDirectory(props, jvmDir);
    if(!isOK(props)) {
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 1, "... cannot create dir for JVM extraction :", 0);
        writeMessageW(props, OUTPUT_LEVEL_DEBUG, 1, jvmDir, 1);
        FREE(jvmDir);
        return;
    }
    
    appendCommandLineArgument(&command, jvm->resolved);
    appendCommandLineArgument(&command, L"-d");
    appendCommandLineArgument(&command, jvmDir);
    
    executeCommand(props, command, jvmDir, JVM_EXTRACTION_TIMEOUT, props->stdoutHandle, props->stderrHandle, NORMAL_PRIORITY_CLASS);
    FREE(command);
    if(!isOK(props)) {
        if(props->status==ERROR_PROCESS_TIMEOUT) {
            writeMessageA(props, OUTPUT_LEVEL_DEBUG, 1, "... could not extract JVM : timeout", 1);
        } else {
            writeMessageA(props, OUTPUT_LEVEL_DEBUG, 1, "... an error occured during running JVM extraction file", 1);
        }
        props->exitCode = props->status;
    } else {
        WCHAR * unpack200exe = appendStringW(appendStringW(NULL, jvmDir), UNPACK200_EXE_SUFFIX);
        if(fileExists(unpack200exe)) {
            unpackJars(props, jvmDir, jvmDir, unpack200exe);
        } else {
            writeMessageA(props, OUTPUT_LEVEL_DEBUG, 1, "... no unpack200 command", 1);
            props->status = ERROR_BUNDLED_JVM_EXTRACTION;
        }
        if(!isOK(props)) {
            writeMessageA(props, OUTPUT_LEVEL_DEBUG, 1, "Could not unpack200 the JVM jars", 1);
        }
        FREE(unpack200exe);
    }
    FREE(jvm->resolved);
    jvm->resolved = jvmDir;
}
void findSystemJava(LauncherProperties * props) {
    if ( props->jvms->size > 0 ) {
        DWORD i=0;
        writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "Search jvm using some predefined locations", 1);
        for(i=0;i<props->jvms->size && !isTerminated(props);i++) {
            resolvePath(props, props->jvms->items[i]);
            if(props->jvms->items[i]->type==0) { // bundled
                installJVM(props, props->jvms->items[i]);
                if(!isOK(props)) {
                    writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "... error occured during JVM extraction", 1);
                    props->status = ERROR_BUNDLED_JVM_EXTRACTION;
                    return;
                }
            }
            if(isTerminated(props)) return;
            if(isOK(props)) {
                trySetCompatibleJava(props->jvms->items[i]->resolved, props);
                if(props->java!=NULL) {
                    break;
                } else if(props->jvms->items[i]->type==0) {
                    props->status = ERROR_BUNDLED_JVM_VERIFICATION;
                    return;
                }
            }
        }
    }
    
    // search JVM in the registry
    if(isTerminated(props)) return;
    if(props->java==NULL) {
        writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "Search java in registry", 1);
        searchCurrentJavaRegistry(props);
    }
    
    if(isTerminated(props)) return;
    // search JVM in the environment
    if(props->java==NULL) {
        writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "Search java in environment variables", 1);
        searchJavaFromEnvVariables(props);
    }
}


void printJavaProperties(LauncherProperties * props, JavaProperties * javaProps) {
    if(javaProps!=NULL) {
        char * jv = getJavaVersionFormatted(javaProps);
        writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "Current Java:", 1);
        writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "       javaHome: ", 0);
        writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, javaProps->javaHome, 1);
        writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "        javaExe: ", 0);
        writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, javaProps->javaExe, 1);
        writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "        version: ", 0);
        writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, jv, 1);
        writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "         vendor: ", 0);
        writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, javaProps->vendor, 1);
        writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "        os.name: ", 0);
        writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, javaProps->osName, 1);
        writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "        os.arch: ", 0);
        writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, javaProps->osArch, 1);
        FREE(jv);
    }
}
