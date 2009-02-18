/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Author: Tomas Holy
 */

#include "utilsfuncs.h"
#include "argnames.h"
#include <tlhelp32.h>

using namespace std;

bool disableFolderVirtualization(HANDLE hProcess) {
    OSVERSIONINFO osvi = {0};
    osvi.dwOSVersionInfoSize = sizeof (OSVERSIONINFO);
    if (GetVersionEx(&osvi) && osvi.dwMajorVersion == 6) // check it is Win VISTA
    {
        HANDLE hToken;
        if (OpenProcessToken(hProcess, TOKEN_ALL_ACCESS, &hToken)) {
            DWORD tokenInfoVal = 0;
            if (!SetTokenInformation(hToken, (TOKEN_INFORMATION_CLASS) 24, &tokenInfoVal, sizeof (DWORD))) {
                // invalid token information class (24) is OK, it means there is no folder virtualization on current system
                if (GetLastError() != ERROR_INVALID_PARAMETER) {
                    logErr(true, true, "Failed to set token information.");
                    return false;
                }
            }
            CloseHandle(hToken);
        } else {
            logErr(true, true, "Failed to open process token.");
            return false;
        }
    }
    return true;
}

bool getStringFromRegistry(HKEY rootKey, const char *keyName, const char *valueName, string &value) {
    logMsg("getStringFromRegistry()\n\tkeyName: %s\n\tvalueName: %s", keyName, valueName);
    HKEY hKey = 0;
    if (RegOpenKeyEx(rootKey, keyName, 0, KEY_READ, &hKey) == ERROR_SUCCESS) {
        DWORD valSize = 4096;
        DWORD type = 0;
        char val[4096] = "";
        if (RegQueryValueEx(hKey, valueName, 0, &type, (BYTE *) val, &valSize) == ERROR_SUCCESS
                && type == REG_SZ) {
            logMsg("%s: %s", valueName, val);
            RegCloseKey(hKey);
            value = val;
            return true;
        } else {
            logErr(true, false, "RegQueryValueEx() failed.");
        }
        RegCloseKey(hKey);
    } else {
        logErr(true, false, "RegOpenKeyEx() failed.");
    }
    return false;
}

bool getDwordFromRegistry(HKEY rootKey, const char *keyName, const char *valueName, DWORD &value) {
    logMsg("getDwordFromRegistry()\n\tkeyName: %s\n\tvalueName: %s", keyName, valueName);
    HKEY hKey = 0;
    if (RegOpenKeyEx(rootKey, keyName, 0, KEY_READ, &hKey) == ERROR_SUCCESS) {
        DWORD valSize = sizeof(DWORD);
        DWORD type = 0;
        if (RegQueryValueEx(hKey, valueName, 0, &type, (BYTE *) &value, &valSize) == ERROR_SUCCESS
                && type == REG_DWORD) {
            logMsg("%s: %u", valueName, value);
            RegCloseKey(hKey);
            return true;
        } else {
            logErr(true, false, "RegQueryValueEx() failed.");
        }
        RegCloseKey(hKey);
    } else {
        logErr(true, false, "RegOpenKeyEx() failed.");
    }
    return false;
}

bool dirExists(const char *path) {
    WIN32_FIND_DATA fd = {0};
    HANDLE hFind = 0;
    hFind = FindFirstFile(path, &fd);
    if (hFind == INVALID_HANDLE_VALUE) {
        logMsg("Dir \"%s\" does not exist", path);
        return false;
    }
    logMsg("Dir \"%s\" exists", path);
    FindClose(hFind);
    return (fd.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) != 0;
}

bool fileExists(const char *path) {
    WIN32_FIND_DATA fd = {0};
    HANDLE hFind = 0;
    hFind = FindFirstFile(path, &fd);
    if (hFind == INVALID_HANDLE_VALUE) {
        logMsg("File \"%s\" does not exist", path);
        return false;
    }

    logMsg("File \"%s\" exists", path);
    FindClose(hFind);
    return true;
}

bool normalizePath(char *path, int len) {
    char tmp[MAX_PATH] = "";
    int i = 0;
    while (path[i] && i < MAX_PATH - 1) {
        tmp[i] = path[i] == '/' ? '\\' : path[i];
        i++;
    }
    tmp[i] = '\0';
    return _fullpath(path, tmp, len) != NULL;
}

bool createPath(const char *path) {
    logMsg("Creating directory \"%s\"", path);
    char dir[MAX_PATH] = "";
    const char *sep = strchr(path, '\\');
    while (sep) {
        strncpy(dir, path, sep - path);
        if (!CreateDirectory(dir, 0) && GetLastError() != ERROR_ALREADY_EXISTS) {
            logErr(true, false, "Failed to create directory %s", dir);
            return false;
        }
        sep = strchr(sep + 1, '\\');
    }
    return true;
}


char * getCurrentModulePath(char *path, int pathLen) {
    MEMORY_BASIC_INFORMATION mbi;
    static int dummy;
    VirtualQuery(&dummy, &mbi, sizeof (mbi));
    HMODULE hModule = (HMODULE) mbi.AllocationBase;
    GetModuleFileName(hModule, path, pathLen);
    return path;
}

char * skipWhitespaces(char *str) {
    while (*str != '\0' && (*str == ' ' || *str == '\t' || *str == '\n' || *str == '\r')) {
        str++;
    }
    return str;
}

char * trimWhitespaces(char *str) {
    char *end = str + strlen(str) - 1;
    while (end >= str && (*end == ' ' || *end == '\t' || *end == '\n' || *end == '\r')) {
        *end = '\0';
        end--;
    }
    return end;
}

char* getSysError(char *str, int strSize) {
    int err = GetLastError();
    LPTSTR lpMsgBuf;
    FormatMessage(
            FORMAT_MESSAGE_ALLOCATE_BUFFER |
            FORMAT_MESSAGE_FROM_SYSTEM |
            FORMAT_MESSAGE_IGNORE_INSERTS,
            NULL,
            err,
            MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
            (LPTSTR) & lpMsgBuf,
            0,
            NULL
            );
    LPTSTR tmp = strchr(lpMsgBuf, '\r');
    if (tmp != NULL) {
        *tmp = '\0';
    }

    _snprintf(str, strSize, " %s (%u)", lpMsgBuf, err);
    LocalFree(lpMsgBuf);
    return str;
}

string gLogFileName;

void logV(bool appendSysError, bool showMsgBox, const char *format, va_list args) {
    char msg[4096] = "";
    vsnprintf(msg, 4096, format, args);

    if (appendSysError) {
        char sysErr[512] = "";
        getSysError(sysErr, 512);
        strncat(msg, sysErr, 4096 - strlen(msg));
    }

    if (!gLogFileName.empty()) {
        FILE *file = fopen(gLogFileName.c_str(), "a");
        if (file) {
            fprintf(file, "%s\n", msg);
            fclose(file);
        }
    }

    if (showMsgBox) {
        ::MessageBox(NULL, msg, "Error", MB_OK | MB_ICONSTOP);
    }
}

void logErr(bool appendSysError, bool showMsgBox, const char *format, ...) {
    va_list args;
    va_start(args, format);
    logV(appendSysError, showMsgBox, format, args);
}

void logMsg(const char *format, ...) {
    va_list args;
    va_start(args, format);
    logV(false, false, format, args);
}

bool restarting(int argc, char *argv[]) {
    for (int i = 0; i < argc; i++) {
        if (strcmp(ARG_NAME_LA_START_APP, argv[i]) == 0 || strcmp(ARG_NAME_LA_START_AU, argv[i]) == 0) {
            return true;
        }
    }
    return false;
}

bool checkLoggingArg(int argc, char *argv[], bool delFile) {
    for (int i = 0; i < argc; i++) {
        if (strcmp(ARG_NAME_LAUNCHER_LOG, argv[i]) == 0) {
            if (i + 1 == argc) {
                logErr(false, true, "Argument is missing for \"%s\" option.", argv[i]);
                return false;
            }
            gLogFileName = argv[++i];
            // if we are restarting, keep log file
            if (delFile && !restarting(argc, argv)) {
                DeleteFile(gLogFileName.c_str());
            }
            break;
        }
    }
    return true;
}

bool setupProcess(int &argc, char *argv[], DWORD &parentProcID, const char *attachMsg) {
#define CHECK_ARG \
    if (i+1 == argc) {\
        logErr(false, true, "Argument is missing for \"%s\" option.", argv[i]);\
        return false;\
    }

    parentProcID = 0;
    DWORD cmdLineArgPPID = 0;
    for (int i = 0; i < argc; i++) {
        if (strcmp(ARG_NAME_CONSOLE, argv[i]) == 0) {
            CHECK_ARG;
            if (strcmp("new", argv[i + 1]) == 0){
                AllocConsole();
            } else if (strcmp("suppress", argv[i + 1]) == 0) {
                // nothing, no console should be attached
            } else {
                logErr(false, true, "Invalid argument for \"%s\" option.", argv[i]);
                return false;
            }
            // remove options
            for (int k = i + 2; k < argc; k++) {
                argv[k-2] = argv[k];
            }
            argc -= 2;
            return true;
        } else if (strcmp(ARG_NAME_LA_PPID, argv[i]) == 0) {
            CHECK_ARG;
            char *end = 0;
            cmdLineArgPPID = strtoul(argv[++i], &end, 10);
            if (cmdLineArgPPID == 0 && *end != '\0') {
                logErr(false, true, "Invalid parameter for option %s", ARG_NAME_LA_PPID);
                return false;
            }
            logMsg("Command line arg PPID: %u", cmdLineArgPPID);
            break;
        }
    }
#undef CHECK_ARG

    // default, attach to parent process console if exists
    // AttachConsole exists since WinXP, so be nice and do it dynamically
    typedef BOOL (WINAPI *LPFAC)(DWORD  dwProcessId);
    HINSTANCE hKernel32 = GetModuleHandle("kernel32");
    if (hKernel32) {
        LPFAC attachConsole = (LPFAC) GetProcAddress(hKernel32, "AttachConsole");
        if (attachConsole) {
            if (cmdLineArgPPID) {
                if (!attachConsole(cmdLineArgPPID)) {
                    logErr(true, false, "AttachConsole of PPID: %u failed.", cmdLineArgPPID);
                }
            } else {
                if (!attachConsole((DWORD) -1)) {
                    logErr(true, false, "AttachConsole of PP failed.");
                } else {
                    getParentProcessID(parentProcID);
                    if (attachMsg) {
                        printToConsole(attachMsg);
                    }
                }
            }
        } else {
            logErr(true, false, "GetProcAddress() for AttachConsole failed.");
        }
    }
    return true;
}

bool isConsoleAttached() {
    typedef HWND (WINAPI *GetConsoleWindowT)();
    HINSTANCE hKernel32 = GetModuleHandle("kernel32");
    if (hKernel32) {
        GetConsoleWindowT getConsoleWindow = (GetConsoleWindowT) GetProcAddress(hKernel32, "GetConsoleWindow");
        if (getConsoleWindow) {
            if (getConsoleWindow() != NULL) {
                logMsg("Console is attached.");
                return true;
            }
        } else {
            logErr(true, false, "GetProcAddress() for GetConsoleWindow failed.");
        }
    }
    return false;
}

bool printToConsole(const char *msg) {
    FILE *console = fopen("CON", "a");
    if (!console) {
        return false;
    }
    fprintf(console, "%s", msg);
    fclose(console);
    return false;
}

bool getParentProcessID(DWORD &id) {
    typedef HANDLE (WINAPI * CreateToolhelp32SnapshotT)(DWORD, DWORD);
    typedef BOOL (WINAPI * Process32FirstT)(HANDLE, LPPROCESSENTRY32);
    typedef BOOL (WINAPI * Process32NextT)(HANDLE, LPPROCESSENTRY32);

    HINSTANCE hKernel32 = GetModuleHandle("kernel32");
    if (!hKernel32) {
        return false;
    }

    CreateToolhelp32SnapshotT createToolhelp32Snapshot = (CreateToolhelp32SnapshotT) GetProcAddress(hKernel32, "CreateToolhelp32Snapshot");
    Process32FirstT process32First = (Process32FirstT) GetProcAddress(hKernel32, "Process32First");
    Process32NextT process32Next = (Process32NextT) GetProcAddress(hKernel32, "Process32Next");

    if (createToolhelp32Snapshot == NULL || process32First == NULL || process32Next == NULL) {
        logErr(true, false, "Failed to obtain Toolhelp32 functions.");
        return false;
    }

    HANDLE hSnapshot = createToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
    if (hSnapshot == INVALID_HANDLE_VALUE) {
        logErr(true, false, "Failed to obtain process snapshot.");
        return false;
    }

    PROCESSENTRY32 entry = {0};
    entry.dwSize = sizeof (PROCESSENTRY32);
    if (!process32First(hSnapshot, &entry)) {
        CloseHandle(hSnapshot);
        return false;
    }

    DWORD curID = GetCurrentProcessId();
    logMsg("Current process ID: %u", curID);

    do {
        if (entry.th32ProcessID == curID) {
            id = entry.th32ParentProcessID;
            logMsg("Parent process ID: %u", id);
            CloseHandle(hSnapshot);
            return true;
        }
    } while (process32Next(hSnapshot, &entry));

    CloseHandle(hSnapshot);
    return false;
}
