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

#include <windows.h>
#include <stdio.h>
#include <stdlib.h>
#include <io.h>
#include <fcntl.h>
#include <process.h>
#include <commdlg.h>
#include <tchar.h>

static char* getUserHomeFromRegistry(char* userhome);
static char* GetStringValue(HKEY key, const char *name);
static DWORD GetDWordValue(HKEY key, const char *name);
static void parseConfigFile(const char* path);
static int readClusterFile(const char* path);
static void parseArgs(int argc, char *argv[]);
static int dirExists(const char* path);
static void ErrorExit(LPTSTR lpszMessage, LPTSTR lpszFunction);

static char userdir[MAX_PATH] = "c:\\nbuser";
static char options[4098] = "";
static char dirs[4098] = "", extradirs[4098];
static char jdkswitch[MAX_PATH] = "";
static char appname[MAX_PATH] = "";
static char branding[128] = "";

static char* defaultDirs[512];

#ifdef WINMAIN

static void parseCommandLine(char *argstr);

int WINAPI
    WinMain (HINSTANCE /* hSelf */, HINSTANCE /* hPrev */, LPSTR cmdline, int /* nShow */) {
#else
    int main(int argc, char* argv[]) {
        char cmdline[10240] = "";
        
        for (int i = 1; i < argc; i++) {
            char buf[10240];
            sprintf(buf, "\"%s\" ", argv[i]);
            strcat(cmdline, buf);
        }
#endif    

    char topdir[MAX_PATH];
    char buf[MAX_PATH * 10], *pc;
  
    GetModuleFileName(0, buf, sizeof buf);

    pc = strrchr(buf, '\\');
    if (pc != NULL) {             // always holds
        strlwr(pc + 1);
        strcpy(appname, pc + 1);  // store the app name
        *pc = '\0';	// remove .exe filename
    }

    pc = strrchr(appname, '.');
    if (pc != NULL) {
        *pc = '\0';
    }

    // printf("appname = %s\n", appname);

    pc = strrchr(appname, '_');
    if (pc != NULL) {
      if (!strcmp(pc, "_w")) {
	*pc = '\0';
      }
    }

    

    pc = strrchr(buf, '\\');
    if (pc != NULL && ((0 == stricmp("\\bin", pc)) || (0 == stricmp("\\launchers", pc))))
        *pc = '\0';
    strcpy(topdir, buf);

    strcpy(branding, appname);

    sprintf(buf, "%s\\etc\\%s.conf", topdir, appname);
    parseConfigFile(buf);

#ifdef WINMAIN
    parseCommandLine(cmdline);
#else
    parseArgs(argc - 1, argv + 1); // skip progname
#endif    

    char olduserdir[MAX_PATH];
    strcpy(olduserdir, userdir);
    sprintf(buf, "%s\\etc\\%s.conf", userdir, appname);
    parseConfigFile(buf);
    strcpy(userdir, olduserdir);

    char clusterFileName[MAX_PATH];
    sprintf(clusterFileName, "%s\\etc\\%s.clusters", topdir, appname);

    if (!readClusterFile(clusterFileName)) {
        ErrorExit("Cannot read cluster file!", NULL);
    }

    char nbexec[MAX_PATH];
    char cmdline2[10240];

    for (char **pdir = defaultDirs; *pdir != NULL; pdir++) {
        sprintf(buf, "%s\\%s", topdir, *pdir);
        if (dirExists(buf)) {
            if (dirs[0] != '\0') {
                sprintf(buf, "%s;%s\\%s", dirs, topdir, *pdir);
            }
            strcpy(dirs, buf);
        }
    }

    char *p = NULL;
    char *q = NULL;

    // printf("extradirs = %s\n", extradirs);

    p = extradirs;
    while (*p != '\0') {
      if ((q = strchr(p, ';')) != NULL) {
        *q = '\0';
      }
      if (*(p + 1) == ':') {
	sprintf(buf, "%s;%s", dirs, p);
      } else {
        sprintf(buf, "%s;%s\\%s", dirs, topdir, p);
      }
      strcpy(dirs, buf);
      if (q == NULL) {
	break;
      } else {
        p = q + 1;
      }
    }
    // printf("%s\n", dirs);
    // printf("%s\n", userdir);

    char pattern [MAX_PATH];
    WIN32_FIND_DATA ffd;
    HANDLE hFind;
    BOOL bNext = TRUE;

    sprintf(pattern, "%s\\platform*", topdir);

    hFind = FindFirstFile(pattern, &ffd);
    do {
        if (hFind == INVALID_HANDLE_VALUE || bNext == FALSE) {
            ErrorExit("Cannot find 'platform*' folder!", NULL);
        }
        if (ffd.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) {
            sprintf(nbexec, "%s\\%s\\lib\\nbexec.exe", topdir, ffd.cFileName);
	    break;
        }
        bNext = FindNextFile(hFind, &ffd);
    } while (1);   

    sprintf(cmdline2, "\"%s\" %s --branding %s --clusters \"%s\" --userdir \"%s\" %s %s",
            nbexec,
            jdkswitch,
	    branding,
            dirs,
            userdir,
            options,
            cmdline);

    STARTUPINFO start;
    PROCESS_INFORMATION pi;

    memset (&start, 0, sizeof (start));
    start.cb = sizeof (start);

#ifdef WINMAIN
    start.dwFlags = STARTF_USESHOWWINDOW;
    start.wShowWindow = SW_HIDE;
#endif
    
    // printf("Cmdline: >%s<\ncwd >%s<\n", cmdline2, topdir);
    if (!CreateProcess (NULL, cmdline2,
                        NULL, NULL, TRUE, NORMAL_PRIORITY_CLASS,
                        NULL, 
                        _T(topdir), // lpCurrentDirectory
                        &start,
                        &pi)) {
        sprintf(buf, "Cannot start %s", appname);
        ErrorExit(buf, "CreateProcess");
    } else {
        // Wait until child process exits.
        WaitForSingleObject( pi.hProcess, INFINITE );

        // Close process and thread handles. 
        CloseHandle( pi.hProcess );
        CloseHandle( pi.hThread );        
        exit(0);
    }
    return 0;
}
    
char* getUserHomeFromRegistry(char* userhome)
{
    HKEY key;

    if (RegOpenKeyEx(
            HKEY_CURRENT_USER,
            "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders",
            0,
            KEY_READ,
            &key) != 0)
        return NULL;

    char *path = GetStringValue(key, "AppData");
    RegCloseKey(key);
    
    strcpy(userhome, path);
    return userhome;
}



char * GetStringValue(HKEY key, const char *name)
{
    DWORD type, size;
    char *value = 0;

    if (RegQueryValueEx(key, name, 0, &type, 0, &size) == 0 && type == REG_SZ) {
        value = (char*) malloc(size);
        if (RegQueryValueEx(key, name, 0, 0, (unsigned char*)value, &size) != 0) {
            free(value);
            value = 0;
        }
    }
    return value;
}


DWORD GetDWordValue(HKEY key, const char *name)
{
    DWORD type, size;
    DWORD value = 0;
    
    if (RegQueryValueEx(key, name, 0, &type, 0, &size) == 0 && type == REG_DWORD) {
        if (RegQueryValueEx(key, name, 0, 0, (LPBYTE)&value, &size) != 0) {
            return 0;
        }
    }
    return value;
}


void parseConfigFile(const char* path) {
    FILE* fin = fopen(path, "r");
    if (fin == NULL)
        return;
    
    char line[2048], *pc;
    
    while (NULL != fgets(line, sizeof line, fin)) {
        for (pc = line; *pc != '\0' && (*pc == ' ' || *pc == '\t' || *pc == '\n' || *pc == '\r'); pc++)
            ;
        if (*pc == '#')
            continue;
        if (strstr(pc, "default_userdir=") == pc) {
            char *q = strstr(pc, "=") + 1;
            pc = line + strlen(line) - 1;
            while (*pc == '\n' || *pc == '\r' || *pc == '\t' || *pc == ' ')
                pc--;

            if (*q == '"' && *pc == '"') {
                q++;
                pc--;
            }
                
            *(pc+1) = '\0';
	    *userdir = '\0';

            if (strstr(q, "${HOME}") == q) {
                char userhome[MAX_PATH];
                strcpy(userdir, getUserHomeFromRegistry(userhome));
		q = q + strlen("${HOME}");
            }
	    char *r = NULL;
	    if ((r = strstr(q, "${APPNAME}")) != NULL) {
	        strncat(userdir, q, r - q);
		strcat(userdir, appname);
		q = r + strlen("${APPNAME}");
	    }
            strcat(userdir, q);
        } else if (strstr(pc, "default_options=") == pc) {
            char *q = strstr(pc, "=") + 1;
            pc = line + strlen(line) - 1;
            while (*pc == '\n' || *pc == '\r' || *pc == '\t' || *pc == ' ')
                pc--;
            
            if (*q == '"' && *pc == '"') {
                q++;
                pc--;
            }
            
            *(pc+1) = '\0';
            strcpy(options, q);
        } else if (strstr(pc, "extra_clusters=") == pc) {
            char *q = strstr(pc, "=") + 1;
            pc = line + strlen(line) - 1;
            while (*pc == '\n' || *pc == '\r' || *pc == '\t' || *pc == ' ')
                pc--;
            
            if (*q == '"' && *pc == '"') {
                q++;
                pc--;
            }
            
            *(pc+1) = '\0';
            strcpy(extradirs, q);
        } else if (strstr(pc, "jdkhome=") == pc) {
            char *q = strstr(pc, "=") + 1;
            pc = line + strlen(line) - 1;
            while (*pc == '\n' || *pc == '\r' || *pc == '\t' || *pc == ' ')
                pc--;
            
            if (*q == '"' && *pc == '"') {
                q++;
                pc--;
            }
            
            *(pc+1) = '\0';
            sprintf(jdkswitch, "--jdkhome \"%s\"", q);
        }
    }
    fclose(fin);
}


int readClusterFile(const char* path) {

    char **dirs = defaultDirs;

    FILE* fin = fopen(path, "r");
    if (fin == NULL)
        return 0;
    
    char line[2048], *pc;
    
    while (NULL != fgets(line, sizeof line, fin)) {
        for (pc = line; *pc != '\0' && (*pc == ' ' || *pc == '\t' || *pc == '\n' || *pc == '\r'); pc++)
            ;
        if (*pc == '#')
            continue;

        char *s = pc;

	while (*pc != '\0' && *pc != '\t' && *pc != '\n' && *pc != '\r')
	    pc++;

	*pc = '\0';

	*dirs = strdup(s);
	dirs++;
    }
    *dirs = NULL;
    fclose(fin);

    return 1;
}


#ifdef WINMAIN

void parseCommandLine(char *argstr) {
    char **argv = (char**) malloc(2048 * sizeof (char*));
    int argc = 0;

#define START 0
#define NORMAL 1
#define IN_QUOTES 2
#define IN_APOS 3

    char *p, *q;
    int state = NORMAL;
    char token[1024 * 64];
    int eof;
  
    q = argstr;
    p = token;
    state = START;
    eof = 0;
  
    while (!eof) {
        if (*q == '\0')
            eof = 1;
    
        switch (state) {
            case START:
                if (*q == ' ' || *q == '\r' || *q == '\n' || *q == '\t') {
                    q++;
                    continue;
                }

                p = token;
                *p = '\0';
      
                if (*q == '"') {
                    state = IN_QUOTES;
                    q++;
                    continue;
                }
                if (*q == '\'') {
                    state = IN_APOS;
                    q++;
                    continue;
                }
      
                state = NORMAL;
                continue;

            case NORMAL:
                if (*q == ' ' || *q == '\r' || *q == '\n' || *q == '\t' || *q == '\0') {
                    *p = '\0';
                    argv[argc] = strdup(token);
                    argc++;
                    state = START;
                } else {
                    *p++ = *q++;
                }
                break;

            case IN_QUOTES:
                if (*q == '"') {
                    if (*(q+1) == '"') {
                        *p++ = '"';
                        q += 2;
                    } else {
                        *p = '\0';
          
                        argv[argc] = strdup(token);
                        argc++;

                        q++;
                        state = START;
                    }
                } else if (*q == '\0') {
                    *p = '\0';
                    argv[argc] = strdup(token);
                    argc++;
                    state = START;
                } else {
                    *p++ = *q++;
                }
                break;
      
            case IN_APOS:
                if (*q == '\'') {
                    if (*(q+1) == '\'') {
                        *p++ = '\'';
                        q += 2;
                    } else {
                        *p = '\0';
          
                        argv[argc] = strdup(token);
                        argc++;

                        q++;
                        state = START;
                    }
                } else if (*q == '\0') {
                    *p = '\0';
                    argv[argc] = strdup(token);
                    argc++;
                    state = START;
                } else {
                    *p++ = *q++;
                }
                break;
        }
    }
  
    parseArgs(argc, argv);

}
#endif // WINMAIN

void parseArgs(int argc, char *argv[]) {
    char *arg;

    while (argc > 0 && (arg = *argv) != 0) {
        argv++;
        argc--;

        if (0 == strcmp("-userdir", arg) || 0 == strcmp("--userdir", arg)) {
            if (argc > 0) {
                arg = *argv;
                argv++;
                argc--;
                if (arg != 0) {
                    strcpy(userdir, arg);
                }
            }
        }
    }
}

int dirExists(const char* path) {
    WIN32_FIND_DATA ffd;
    HANDLE ffh;
    
    memset(&ffd, 0, sizeof ffd);
    ffd.dwFileAttributes = FILE_ATTRIBUTE_DIRECTORY;
    ffh = FindFirstFile(path, &ffd);
    if (ffh != INVALID_HANDLE_VALUE) {
        FindClose(ffh);
        return 1;
    } else {
        return 0;
    }
}

// Show error dialog and exits the program
// lpszMessage - error message to be printed
// lpszFunction - name of last called method that return fail status
//                can be NULL
void ErrorExit(LPTSTR lpszMessage, LPTSTR lpszFunction) 
{ 
    LPVOID lpMsgBuf;
    LPVOID lpDisplayBuf;
    DWORD dw = GetLastError(); 

    FormatMessage(
        FORMAT_MESSAGE_ALLOCATE_BUFFER | 
        FORMAT_MESSAGE_FROM_SYSTEM,
        NULL,
        dw,
        MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
        (LPTSTR) &lpMsgBuf,
        0, NULL );

    if (lpszFunction != NULL ) {
        lpDisplayBuf = (LPVOID)LocalAlloc(LMEM_ZEROINIT, 
            (lstrlen((LPCTSTR)lpMsgBuf)+lstrlen((LPCTSTR)lpszFunction)+40)*sizeof(TCHAR)); 
        wsprintf((LPTSTR)lpDisplayBuf, 
            TEXT("%s\n%s failed with error %d: %s"), 
            lpszMessage, lpszFunction, dw, lpMsgBuf); 
    }
    else {
        lpDisplayBuf = (LPVOID)LocalAlloc(LMEM_ZEROINIT, 
            lstrlen((LPCTSTR)lpMsgBuf)); 
        wsprintf((LPTSTR)lpDisplayBuf, 
            TEXT("%s"), 
            lpszMessage); 
    }
    	
    MessageBox(NULL, (LPCTSTR)lpDisplayBuf, TEXT("Error"), MB_ICONSTOP | MB_OK); 

    LocalFree(lpMsgBuf);
    LocalFree(lpDisplayBuf);
    ExitProcess( (dw != 0)? dw: 1); 
}

