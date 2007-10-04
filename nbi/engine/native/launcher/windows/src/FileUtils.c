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

#include <windows.h>
#include <wchar.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <userenv.h>
#include "FileUtils.h"
#include "StringUtils.h"

HANDLE stdoutHandle = INVALID_HANDLE_VALUE;
HANDLE stderrHandle = INVALID_HANDLE_VALUE;

DWORD newLine = 1;
const WCHAR * FILE_SEP = L"\\";

char TIME_STRING [30];

void writeTimeStamp(HANDLE hd, DWORD need) {
    DWORD written;
    if(need==1) {
        SYSTEMTIME t;
		DWORD i=0;
        GetLocalTime(&t);		
        sprintf(TIME_STRING, "[%02u-%02u-%02u %02u:%02u:%02u.%03u]> ", t.wYear, t.wMonth, t.wDay, t.wHour, t.wMinute, t.wSecond, t.wMilliseconds);
        WriteFile(hd, TIME_STRING, sizeof(char) * getLengthA(TIME_STRING), & written, NULL);
	for(i=0;i<getLengthA(TIME_STRING);i++) {
		TIME_STRING[i]=' ';
	}
    }
}

void writeMessageA(LauncherProperties * props, DWORD level, DWORD isErr,  const char * message, DWORD needEndOfLine) {
    if(level>=props->outputLevel) {
        HANDLE hd = (isErr) ? props->stderrHandle : props->stdoutHandle;
        DWORD written;
        writeTimeStamp(hd, newLine);
        WriteFile(hd, message, sizeof(char) * getLengthA(message), & written, NULL);
        if(needEndOfLine>0) {
            newLine = 0;
            while((needEndOfLine--)>0) {
                writeMessageA(props, level, isErr, "\r\n", 0);
                newLine = 1;
            }
            flushHandle(hd);
        } else {
            newLine = 0;
        }
    }
}

void writeMessageW(LauncherProperties * props, DWORD level, DWORD isErr,  const WCHAR * message, DWORD needEndOfLine) {
    if ( level >= props->outputLevel ) {
        char * msg = toChar(message);
        writeMessageA(props, level, isErr, msg, needEndOfLine);
        FREE(msg);
    }
}
void writeDWORD(LauncherProperties * props, DWORD level,  DWORD isErr,  const char * message, DWORD value, DWORD needEndOfLine) {
    char * dwordStr = DWORDtoCHAR(value);
    writeMessageA(props, level, isErr, message, 0);
    writeMessageA(props, level, isErr, dwordStr, needEndOfLine);
    FREE(dwordStr);
}

void writeint64t(LauncherProperties * props, DWORD level, DWORD isErr, const char * message, int64t * value, DWORD needEndOfLine) {
    char * str = int64ttoCHAR(value);
    writeMessageA(props, level, isErr, message, 0);
    writeMessageA(props, level, isErr, str, needEndOfLine);
    FREE(str);
}
void writeErrorA(LauncherProperties * props, DWORD level, DWORD isErr, const char * message, const WCHAR * param, DWORD errorCode) {
    WCHAR * err = getErrorDescription(errorCode);
    writeMessageA(props, level, isErr, message, 0);
    writeMessageW(props, level, isErr, param, 1);
    writeMessageW(props, level, isErr, err, 1);
    FREE(err);
}

void flushHandle(HANDLE hd) {
    FlushFileBuffers(hd);
}

int64t * getFreeSpace(WCHAR *path) {
    int64t bytes;
    int64t * result = newint64_t(0, 0);
    WCHAR * dst = appendStringW(NULL, path);
    
    while(!fileExists(dst)) {
        WCHAR * parent = getParentDirectory(dst);
        FREE(dst);
        dst = parent;
        if(dst==NULL) break;
    }
    if(dst==NULL) return result; // no parent ? strange
    if (GetDiskFreeSpaceExW(dst, (PULARGE_INTEGER) &bytes, NULL, NULL)) {
        result->High = bytes.High;
        result->Low  = bytes.Low;
    }
    FREE(dst);
    return result;
}

void checkFreeSpace(LauncherProperties * props, WCHAR * tmpDir, int64t * size) {
    if(props->checkForFreeSpace) {
        int64t * space = getFreeSpace(tmpDir);
        DWORD   result = 0;
        result = ((space->High > size->High) ||
                (space->High == size->High && space->Low >= size->Low));
        FREE(space);
        if(!result) {
            props->status = ERROR_FREESPACE;
        }
    } else {
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... free space checking is disabled", 1);
    }
}

DWORD fileExists(WCHAR *path) {
    WIN32_FILE_ATTRIBUTE_DATA attrs;
    return GetFileAttributesExW(path, GetFileExInfoStandard, &attrs);
}

DWORD isDirectory(WCHAR *path) {
    WIN32_FILE_ATTRIBUTE_DATA attrs;
    if(GetFileAttributesExW(path, GetFileExInfoStandard, &attrs)) {
        return (attrs.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY);
    }
    else {
        return 0;
    }
}
WCHAR * getParentDirectory(WCHAR * dir) {
    WCHAR * ptr = dir;
    WCHAR * res = NULL;
    while(1) {
        if(wcsstr(ptr, FILE_SEP)==NULL) {
            break;
        }
        ptr = wcsstr(ptr, FILE_SEP) + 1;
    }
    res = appendStringNW(NULL, 0, dir, getLengthW(dir) - getLengthW(ptr) - 1);
    return res;
}
WCHAR * normalizePath(WCHAR * dir) {
    WCHAR * directory = NULL;
    WCHAR * ptr1, *ptr2;
    DWORD i=0;
    DWORD len;
    ptr1 = wcsstr(dir, L":\\");
    ptr2 = wcsstr(dir, L":/");
    if(ptr1==NULL && ptr2==NULL) { //relative path
        directory = appendStringW(getCurrentDirectory(), FILE_SEP);
        directory = appendStringW(directory, dir);
    } else {
        directory = appendStringW(NULL, dir);
    }
    len = getLengthW(directory);
    
    for(i=0;i<len;i++) {
        if(directory[i]=='/') directory[i] = '\\';
    }
    return directory;
}

void createDirectory(LauncherProperties * props, WCHAR * directory) {
    
    WCHAR * parent;
    // now directory is the absolute path with normalized slashes
    writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "Getting parent directory of ", 0);
    writeMessageW(props, OUTPUT_LEVEL_DEBUG, 0, directory, 1);
    
    parent = getParentDirectory(directory);
    
    if(parent!=NULL) {
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "    parent = ", 0);
        writeMessageW(props, OUTPUT_LEVEL_DEBUG, 0, parent, 1);
        
        if(!fileExists(parent)) {
            writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... doesn`t exist. Create it...", 1);
            createDirectory(props, parent);
        } else {
            writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "    ... exist. ", 1);
        }
        if(isOK(props)) {
            DWORD parentAttrs = GetFileAttributesW(parent);
            if(parentAttrs == INVALID_FILE_ATTRIBUTES) {
                props-> status = ERROR_INPUTOUPUT;
            } else  {
                SECURITY_ATTRIBUTES secattr;
                int64t * minSize = newint64_t(0, 0);
                
                secattr.nLength = sizeof(SECURITY_ATTRIBUTES);
                secattr.lpSecurityDescriptor = 0;
                secattr.bInheritHandle = 1;
                writeMessageA(props, OUTPUT_LEVEL_DEBUG, 1, "... creating directory itself... ", 0);
                writeMessageW(props, OUTPUT_LEVEL_DEBUG, 1, directory, 1);
                
                checkFreeSpace(props, parent, minSize);
                FREE(minSize);
                
                if(isOK(props)) {
                    props->status = (CreateDirectoryExW(parent, directory, &secattr)) ? ERROR_OK : ERROR_INPUTOUPUT;
                    if(!isOK(props)) {
                        props->status = (CreateDirectoryW(directory, &secattr)) ? ERROR_OK : ERROR_INPUTOUPUT;
                    }
                    props->status = (fileExists(directory)) ? ERROR_OK : ERROR_INPUTOUPUT;
                    
                    if(isOK(props)) {
                        SetFileAttributesW(directory, parentAttrs);
                    } else {
                        writeErrorA(props, OUTPUT_LEVEL_DEBUG, 1, "Error! Can`t create directory : ", directory, GetLastError());
                    }
                }
            }
        }
        FREE(parent);
    } else {
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "    parent is NULL ", 1);
        props-> status = ERROR_INPUTOUPUT;
    }
}

WCHAR newRandDigit() {
	WCHAR digit = '0';
	digit += (WCHAR) rand()%10;
    return digit;
}
void createTempDirectory(LauncherProperties * props, WCHAR * argTempDir, DWORD createRndSubDir) {
    WCHAR * t = (argTempDir!=NULL) ? appendStringW(NULL, argTempDir) : getSystemTemporaryDirectory();
    
    WCHAR * nbiTmp = normalizePath(t);
    FREE(t);
    if(createRndSubDir) {
        WCHAR * randString = newpWCHAR(6);
        DWORD i=0;
        nbiTmp = appendStringW(nbiTmp, L"\\NBI");
        srand(GetTickCount());
        
        for(i=0;i<5;i++) {
            randString[i]=newRandDigit();
        }
        nbiTmp = appendStringW(appendStringW(nbiTmp, randString), L".tmp");
        FREE(randString);
    }
    
    writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "Using temp directory for extracting data : ", 0);
    writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, nbiTmp, 1);
    
    if(fileExists(nbiTmp) ) {
        if(!isDirectory(nbiTmp)) {
            props->status = ERROR_INPUTOUPUT;
            writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, ".. exists but not a directory", 1);
        } else {
            writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "Output directory already exist so don`t create it", 1);
        }
    }
    else {
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "creating directory...", 1);
        createDirectory(props, nbiTmp);
        if(isOK(props)) {
            props->tmpDirCreated = 1;
            writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "Directory created : ", 0);
            writeMessageW(props, OUTPUT_LEVEL_DEBUG, 0, nbiTmp, 1);
            // set hidden attribute
            if(createRndSubDir) {
                writeMessageA(props, OUTPUT_LEVEL_DEBUG, 1, "Setting hidden attributes to ", 0);
                writeMessageW(props, OUTPUT_LEVEL_DEBUG, 1, nbiTmp, 1);
                
                SetFileAttributesW(nbiTmp, GetFileAttributesW(nbiTmp) | FILE_ATTRIBUTE_HIDDEN);
            }
        } else {
            writeMessageA(props, OUTPUT_LEVEL_DEBUG, 1, "Error! Can`t create nbi temp directory : ", 0);
            writeMessageW(props, OUTPUT_LEVEL_DEBUG, 1, nbiTmp, 1);
        }
    }
    props->tmpDir = nbiTmp;
    return;
}


void deleteDirectory(LauncherProperties * props, WCHAR * dir) {
    DWORD attrs = GetFileAttributesW(dir);
    DWORD dwError;
    DWORD count = 0 ;
    if(attrs==INVALID_FILE_ATTRIBUTES) {
        writeErrorA(props, OUTPUT_LEVEL_DEBUG, 1, "Error! Can`t get attributes of the dir : ", dir, GetLastError());
        return;
    }
    if(!SetFileAttributesW(dir, attrs & FILE_ATTRIBUTE_NORMAL)) {
        writeErrorA(props, OUTPUT_LEVEL_DEBUG, 1, "Error! Can`t set attributes of the dir : ", dir, GetLastError());
    }
    
    
    if(attrs & FILE_ATTRIBUTE_DIRECTORY) {
        WIN32_FIND_DATAW FindFileData;
        HANDLE hFind = INVALID_HANDLE_VALUE;
        
        WCHAR * DirSpec = appendStringW(appendStringW(NULL, dir), L"\\*" );
        
        // Find the first file in the directory.
        hFind = FindFirstFileW(DirSpec, &FindFileData);
        
        if (hFind == INVALID_HANDLE_VALUE) {
            writeErrorA(props, OUTPUT_LEVEL_DEBUG, 1, "Error! Can`t file with pattern ", DirSpec, GetLastError());
        }
        else {
            // List all the other files in the directory.
            while (FindNextFileW(hFind, &FindFileData) != 0) {
                if(lstrcmpW(FindFileData.cFileName, L".")!=0 &&
                        lstrcmpW(FindFileData.cFileName, L"..")!=0 ) {
                    WCHAR * child = appendStringW(appendStringW(appendStringW(NULL, dir), FILE_SEP), FindFileData.cFileName);
                    deleteDirectory(props, child);
                    FREE(child);
                }
            }
            
            dwError = GetLastError();
            FindClose(hFind);
            if (dwError != ERROR_NO_MORE_FILES) {
                writeErrorA(props, OUTPUT_LEVEL_DEBUG, 1, "Error! Can`t find file with pattern : ", DirSpec, dwError);
            }
        }
        
        // 20 tries in 2 seconds to delete the directory
        while(!RemoveDirectoryW(dir) && count++ < 20) Sleep(100);
        FREE(DirSpec);
    }
    else {
        // 20 tries in 2 seconds to delete the file
        while(!DeleteFileW(dir) && count++ < 20) Sleep(100);
    }
}




WCHAR * getSystemTemporaryDirectory() {
    WCHAR * expanded = newpWCHAR(MAX_PATH);
    DWORD result;
    if(GetTempPathW(MAX_PATH, expanded)!=0) {
        return expanded;
    }
    
    result = GetEnvironmentVariableW(L"TEMP", expanded, MAX_PATH);
    if(result<=0 || result>MAX_PATH) {
        //writeOutputLn("Can`t find variable TEMP");
        result = GetEnvironmentVariableW(L"TMP", expanded, MAX_PATH);
        if(result<=0 || result>MAX_PATH) {
            //writeOutputLn("Can`t find variable TMP");
            result = GetEnvironmentVariableW(L"USERPROFILE", expanded, MAX_PATH);
            if(result>0 && result<=MAX_PATH) {
                expanded = appendStringW(expanded, L"\\Local Settings\\Temp");
            } else{
                WCHAR * curdir = getCurrentDirectory();
                ZERO(expanded, sizeof(WCHAR) * MAX_PATH);
                lstrcpynW(expanded, curdir, MAX_PATH);
                FREE(curdir);
            }
        }
    }
    return expanded;
}

WCHAR * getExePath() {
    WCHAR szPath[MAX_PATH];
    
    if( !GetModuleFileNameW( NULL, szPath, MAX_PATH ) ) {
        return NULL;
    } else {
        return appendStringNW(NULL, 0, szPath, getLengthW(szPath));
    }
}


WCHAR * getExeName() {
    WCHAR szPath[MAX_PATH];
    if(GetModuleFileNameW( NULL, szPath, MAX_PATH )) {
        WCHAR * lastSlash = szPath;
        while(wcsstr(lastSlash, FILE_SEP)!=NULL) {
            lastSlash = wcsstr(lastSlash, FILE_SEP) + 1;
        }
        return appendStringW(NULL, lastSlash);
    } else {
        return NULL;
    }
}

WCHAR * getExeDirectory() {
    WCHAR szPath[MAX_PATH];
    if(GetModuleFileNameW( NULL, szPath, MAX_PATH )) {
        WCHAR * lastSlash = szPath;
        while(wcsstr(lastSlash, FILE_SEP)!=NULL) {
            lastSlash = wcsstr(lastSlash, FILE_SEP) + 1;
        }
        
        return appendStringNW(NULL, 0 , szPath,
                getLengthW(szPath) - getLengthW(lastSlash) - 1);
    } else {
        return NULL;
    }
}

WCHAR * getCurrentDirectory() {
    WCHAR * buf = newpWCHAR(MAX_PATH);
    
    if(GetCurrentDirectoryW(MAX_PATH, buf)!=0) {
        return buf;
    } else {
        FREE(buf);
        return NULL;
    }
}
WCHAR * getCurrentUserHome() {
    HANDLE hUser;
    WCHAR * buf = NULL;
    if (OpenProcessToken(GetCurrentProcess(), TOKEN_QUERY, &hUser)) {
        DWORD size = MAX_PATH;
        buf = newpWCHAR(MAX_PATH);
        GetUserProfileDirectoryW(hUser, buf, &size);
        if (size > MAX_PATH) {
            FREE(buf);
            buf = newpWCHAR(size + 1);
            if(!GetUserProfileDirectoryW(hUser, buf, &size)) {
                FREE(buf);
            }
        }
        CloseHandle(hUser);
    }
    if(buf==NULL) {
        DWORD size = MAX_PATH;
        buf = newpWCHAR(size);
        size = GetEnvironmentVariableW(L"USERPROFILE", buf, MAX_PATH);
        if( size > MAX_PATH) {
            FREE(buf);
            buf = newpWCHAR(size + 1);
            GetEnvironmentVariableW(L"USERPROFILE", buf, size);
        } else if (size==0) {
            FREE(buf);
        }
    }
    return buf;
}
int64t * getFileSize(WCHAR * path) {
    WIN32_FILE_ATTRIBUTE_DATA wfad;
    int64t * res = newint64_t(0, 0);
    if (GetFileAttributesExW(path,
            GetFileExInfoStandard,
            &wfad)) {
        res->Low  = wfad.nFileSizeLow;
        res->High = wfad.nFileSizeHigh ;
    }
    return res;
}
