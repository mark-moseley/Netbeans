/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

#include <windows.h>
#include <stdio.h>
#include <stdlib.h>
#include <io.h>
#include <fcntl.h>
#include <process.h>
#include <commdlg.h>

static char* getUserHomeFromRegistry(char* userhome);
static char * GetStringValue(HKEY key, const char *name);

#ifdef WINMAIN

int WINAPI
    WinMain (HINSTANCE hSelf, HINSTANCE hPrev, LPSTR cmdline, int nShow) {
#else
    int main(int argc, char* argv[]) {
        char cmdline[10240] = "";
        
        for (int i = 1; i < argc; i++) {
            char buf[10240];
            sprintf(buf, "\"%s\" ", argv[i]);
            strcat(cmdline, buf);
        }
#endif    

    char topdir[MAX_PATH], userdir[MAX_PATH] = "c:\\nbuser", options[4098] = "", dirs[4098] = "";
    char jdkswitch[MAX_PATH] = "";
    char buf[MAX_PATH], *pc;
  
    GetModuleFileName(0, buf, sizeof buf);

    pc = strrchr(buf, '\\');
    if (pc != NULL) {             // always holds
        strlwr(pc + 1);
        *pc = '\0';	// remove .exe filename
    }

    pc = strrchr(buf, '\\');
    if (pc != NULL && 0 == stricmp("\\bin", pc))
        *pc = '\0';
    strcpy(topdir, buf);

    sprintf(buf, "%s\\etc\\netbeans.conf", topdir);
    
    FILE* fin = fopen(buf, "r");
    if (fin == NULL)
        exit(1);
    
    char line[2048];
    while (NULL != fgets(line, sizeof line, fin)) {
        for (pc = line; *pc != '\0' && (*pc == ' ' || *pc == '\t' || *pc == '\n' || *pc == '\r'); pc++)
            ;
        if (*pc == '#')
            continue;
        if (strstr(pc, "netbeans_default_userdir=") == pc) {
            char *q = strstr(pc, "=") + 1;
            pc = line + strlen(line) - 1;
            while (*pc == '\n' || *pc == '\r' || *pc == '\t' || *pc == ' ')
                pc--;

            if (*q == '"' && *pc == '"') {
                q++;
                pc--;
            }
                
            *(pc+1) = '\0';
            if (strstr(q, "${HOME}") == q) {
                char userhome[MAX_PATH];
                strcpy(userdir, getUserHomeFromRegistry(userhome));
                strcat(userdir, q + strlen("${HOME}"));
            } else {
                strcpy(userdir, q);
            }
        } else if (strstr(pc, "netbeans_default_options=") == pc) {
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
        } else if (strstr(pc, "netbeans_clusters=") == pc) {
            char *q = strstr(pc, "=") + 1;
            pc = line + strlen(line) - 1;
            while (*pc == '\n' || *pc == '\r' || *pc == '\t' || *pc == ' ')
                pc--;
            
            if (*q == '"' && *pc == '"') {
                q++;
                pc--;
            }
            
            *(pc+1) = '\0';
            strcpy(dirs, q);
        } else if (strstr(pc, "netbeans_jdkhome=") == pc) {
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

    char nbexec[MAX_PATH];
    char cmdline2[10240];
    
    if (dirs[0] == '\0') {
        int rc = 0;
        WIN32_FIND_DATA ffd;
        HANDLE ffh;
        
        sprintf(buf, "%s\\extra", topdir);
    
        memset(&ffd, 0, sizeof ffd);
        ffd.dwFileAttributes = FILE_ATTRIBUTE_DIRECTORY;
        ffh = FindFirstFile(buf, &ffd);
        if (ffh != INVALID_HANDLE_VALUE) {
            FindClose(ffh);
            sprintf(dirs, "%s\\%s;%s\\%s;%s\\%s",
                    topdir, "\\nb4.0", topdir, "\\ide4", topdir, "\\extra");
        } else {
            sprintf(dirs, "%s\\%s;%s\\%s",
                    topdir, "\\nb4.0", topdir, "\\ide4" );
        }
    }
    sprintf(nbexec, "%s\\platform4\\launcher\\nbexec.exe", topdir);
    sprintf(cmdline2, "\"%s\" %s --branding nb --clusters \"%s\" --userdir \"%s\" %s %s",
            nbexec,
            jdkswitch,
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
    
    if (!CreateProcess (NULL, cmdline2,
                        NULL, NULL, TRUE, NORMAL_PRIORITY_CLASS,
                        NULL, NULL,
                        &start,
                        &pi)) {
        MessageBox(NULL, "Cannot start the IDE", "Error", MB_ICONSTOP | MB_OK);
        exit(1);
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

    char *path = GetStringValue(key, "Desktop");
    RegCloseKey(key);
    
    char *pc = strrchr(path, '\\');
    if (pc == NULL) {
	return NULL;
    }
    *pc = '\0';
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
