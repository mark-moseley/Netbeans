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
 */

#include <windows.h>
#include <stdio.h>
#include <stdlib.h>
#include <io.h>
#include <fcntl.h>
#include <process.h>
#include <commdlg.h>
#include <errno.h>

#define PROG_FULLNAME "Error"
#define IDE_MAIN_CLASS "org/netbeans/Main"
#define UPDATER_MAIN_CLASS "org/netbeans/updater/UpdaterFrame"

#define JDK_KEY "Software\\JavaSoft\\Java Development Kit"
#define JRE_KEY "Software\\JavaSoft\\Java Runtime Environment"

#define RUN_NORMAL "-run_normal"
#define RUN_UPDATER "-run_updater"

#define BAD_OPTION_MSG "Wrong option."

// #define DEBUG 1

static char jdkhome[MAX_PATH];
static char plathome[MAX_PATH];
static char userdir[MAX_PATH];

static char clusters[MAX_PATH * 20];

static char *bootclass = NULL;

static int runnormal = 0;
static int runupdater = 0;

static char classpath[1024 * 16];
static char classpathBefore[1024 * 16];
static char classpathAfter[1024 * 16];

static char **options;
static int numOptions, maxOptions;

static char *progArgv[1024];
static int progArgc = 0;

static void runClass(char *mainclass, bool deleteAUClustersFile);

static void fatal(const char *str);
static char *findJavaExeInDirectory(char *dir);
static int findJdkFromRegistry(const char* keyname, char jdkhome[]);

static void addJdkJarsToClassPath(const char *jdkhome);
static void addLauncherJarsToClassPath(const char *plathome);

static void addToClassPath(const char *pathprefix, const char *path);
static void addToClassPathIfExists(const char *pathprefix, const char *path);
static void addAllFilesToClassPath(const char *dir, const char *pattern);

static void parseArgs(int argc, char *argv[]);
static void addOption(char *str);

static int fileExists(const char* path);

static void normalizePath(char *userdir);
static bool runAutoUpdater(bool firstStart, const char * root);
static bool runAutoUpdaterOnClusters(bool firstStart);

static int findHttpProxyFromRegistry(char **proxy, char **nonProxy);
static int findHttpProxyFromEnv(char **proxy, char **nonProxy);

static char* processAUClustersList(char *userdir);
static int removeAUClustersListFile(char *userdir);

int checkForNewUpdater(const char *basePath);

int main(int argc, char *argv[]) {
    char exepath[1024 * 4];
    char buf[1024 * 8], *pc;
  
    GetModuleFileName(0, buf, sizeof buf);
    strcpy(exepath, buf);

#ifdef DEBUG
    printf("argc = %d\n", argc);
    printf("exepath = %s\n", exepath);
#endif

    pc = strrchr(buf, '\\');
    if (pc != NULL) {             // always holds
        strlwr(pc + 1);
        *pc = '\0';	// remove .exe filename
    }

    pc = strrchr(buf, '\\');
    if (pc != NULL && 0 == stricmp("\\lib", pc))
        *pc = '\0';
    strcpy(plathome, buf);
    strcpy(clusters, buf);

#ifdef DEBUG
    printf("plathome = %s\n", plathome);
#endif

    if (0 != findJdkFromRegistry(JDK_KEY, jdkhome))
        findJdkFromRegistry(JRE_KEY, jdkhome);

#ifdef DEBUG
    printf("jdkhome = %s\n", jdkhome);
#endif
    
    parseArgs(argc - 1, argv + 1); // skip progname

    if (!runnormal && !runupdater) {
        char **newargv = (char**) malloc((argc+8) * sizeof (char*));
        int i;
            
        if (userdir[0] == '\0') {
            fatal("Need to specify userdir using command line option --userdir");
            exit(1);
        }
        
        sprintf(buf, "\"%s\"", argv[0]);
        newargv[0] = strdup(buf);

#ifdef DEBUG
        printf("newargv[0] = %s\n", newargv[0]);
#endif
        
        for (i = 1; i < argc; i++) {
            sprintf(buf, "\"%s\"", argv[i]);
            newargv[i+1] = strdup(buf);
#ifdef DEBUG
            printf("newargv[%d] = %s\n", i+1, newargv[i+1]);
#endif
        }
        i++;

        newargv[i] = NULL;
            
        // check for patches first (for updater first)
        checkForNewUpdater(plathome);
        bool runUpdater = runAutoUpdaterOnClusters(true);

        if (runUpdater) {
            newargv[1] = RUN_UPDATER;
            _spawnv(_P_WAIT, exepath, newargv);
        }

      AGAIN:

        // run IDE

        newargv[1] = RUN_NORMAL;
        _spawnv(_P_WAIT, exepath, newargv);

        // check for patches again (for updater first)
        checkForNewUpdater(plathome);
        runUpdater = runAutoUpdaterOnClusters(false);
        if (runUpdater) {
            newargv[1] = RUN_UPDATER;
            _spawnv(_P_WAIT, exepath, newargv);
            
            goto AGAIN;
        }
    } else if (runnormal) {
        argc -= 2;
        argv += 2;
        if (bootclass != NULL) {
            runClass(bootclass, TRUE);
        }
        else {
            runClass(IDE_MAIN_CLASS, TRUE);
        }
    } else if (runupdater) {
        argc -= 2;
        argv += 2;
        runClass(UPDATER_MAIN_CLASS, FALSE);
    }

    return 0;
}

bool runAutoUpdaterOnClusters(bool firstStart) {
    bool runUpdater = false;
    char *sClusters = processAUClustersList(userdir);
    if (sClusters == NULL) {
        sClusters = strdup(clusters);
    }
    char *pc;

    runUpdater = runAutoUpdater(firstStart, plathome);
    
    pc = strtok(sClusters, ";");
    while (pc != NULL) {
        runUpdater |= runAutoUpdater(firstStart, pc);
        pc = strtok(NULL, ";");
    }
    runUpdater |= runAutoUpdater(firstStart, userdir);
    
#ifdef DEBUG
    printf("runAutoUpdaterOnClusters returning %d (platform %s, clusters %s, userdir %s)\n", 
        runUpdater, plathome, sClusters, userdir);
#endif
    free(sClusters);
    return runUpdater;
}

bool runAutoUpdater(bool firstStart, const char * root) {
    WIN32_FIND_DATA ffd;
    char tmp [MAX_PATH];

    // The logic is following:
    // if there is an NBM for installation then run updater 
    // unless it is not a first start and we asked to install later (on next start)
    
    // then also check if last run left list of modules to disable/uninstall and
    // did not mark them to be deactivated later (on next start)
    strcpy(tmp, root);
    strcat(tmp, "\\update\\download\\*.nbm");
    HANDLE nbmFiles = FindFirstFile(tmp, &ffd);

    strcpy(tmp, root);
    strcat(tmp, "\\update\\download\\install_later.xml");
    HANDLE laterFile = FindFirstFile(tmp, &ffd);

    if (INVALID_HANDLE_VALUE != nbmFiles && (firstStart || INVALID_HANDLE_VALUE == laterFile))
        return true;

    strcpy(tmp, root);
    strcat(tmp, "\\update\\deactivate\\deactivate_later.txt");
    laterFile = FindFirstFile(tmp, &ffd);
    if (firstStart || (INVALID_HANDLE_VALUE == laterFile)) {
        strcpy(tmp, root);
        strcat(tmp, "\\update\\deactivate\\to_disable.txt");
        laterFile = FindFirstFile(tmp, &ffd);
        if (INVALID_HANDLE_VALUE != laterFile) {
            return true;
        }
        strcpy(tmp, root);
        strcat(tmp, "\\update\\deactivate\\to_uninstall.txt");
        laterFile = FindFirstFile(tmp, &ffd);
        if (INVALID_HANDLE_VALUE != laterFile) {
            return true;
        }
    }
    
    return false;
}

void runClass(char *mainclass, bool deleteAUClustersFile) {
    char buf[10240];

#ifdef DEBUG
    printf("Running class %s\n", mainclass);
#endif
    if (jdkhome[0] == '\0')
        fatal("JDK 5.0 or newer cannot be found on your machine.");

    strcat(strcpy(buf, "-Djdk.home="), jdkhome);
    addOption(buf);
    
    strcat(strcpy(buf, "-Dnetbeans.home="), plathome);
    addOption(buf);

    char *sAUclusters = processAUClustersList(userdir);
    if (sAUclusters != NULL) {
        strcat(strcpy(buf, "-Dnetbeans.dirs="), sAUclusters);
    }
    else {
        strcat(strcpy(buf, "-Dnetbeans.dirs="), clusters);
    }
    addOption(buf);
    if (deleteAUClustersFile) {
        removeAUClustersListFile(userdir);
    }
    
    if (userdir[0] != '\0') {
        strcat(strcpy(buf, "-Dnetbeans.user="), userdir);
        addOption(buf);
    }
  
    if (classpathBefore[0] != '\0')
        strcpy(classpath, classpathBefore);
    else
        classpath[0] = '\0';
  
    addLauncherJarsToClassPath(plathome);
    addJdkJarsToClassPath(jdkhome);

    char *proxy, *nonProxyHosts;
    if (0 == findHttpProxyFromEnv(&proxy, &nonProxyHosts)) {
        sprintf(buf, "-Dnetbeans.system_http_proxy=%s", proxy);
        addOption(buf);
        sprintf(buf, "-Dnetbeans.system_http_non_proxy_hosts=%s", nonProxyHosts);
        addOption(buf);
        free(proxy);
        free(nonProxyHosts);
    }
    else if (0 == findHttpProxyFromRegistry(&proxy, &nonProxyHosts)) {
        sprintf(buf, "-Dnetbeans.system_http_proxy=%s", proxy);
        addOption(buf);
        sprintf(buf, "-Dnetbeans.system_http_non_proxy_hosts=%s", nonProxyHosts);
        addOption(buf);
        free(proxy);
        free(nonProxyHosts);
    }

    // see BugTraq #5043070
    addOption("-Dsun.awt.keepWorkingSetOnMinimize=true");
    
    char *javapath = findJavaExeInDirectory(jdkhome);
    if (javapath == NULL) {
        sprintf(buf, "Cannot find java.exe.\nNeither %s%s nor %s%s exists.", 
            jdkhome, "\\jre\\bin\\java.exe", 
            jdkhome, "\\bin\\java.exe"
            ); 
        fatal(buf);
    }

    int argc = numOptions + 3 + progArgc + 1;
    char **args = (char**) malloc(argc * sizeof(char*));
    char **p = args;

    sprintf(buf, "\"%s\"", javapath);
    *p++ = strdup(buf);

    int i;    
    for (i = 0; i < numOptions; i++) {
#ifdef DEBUG
        printf("runClass[%d] = %s\n", i, options[i]);
#endif
        sprintf(buf, "\"%s\"", options[i]);
        *p++ = strdup(buf);
    }

    *p++ = "-cp";

    if (classpathAfter[0] != '\0') {
        if (classpath[strlen(classpath)] != ';')
            strcat(classpath, ";");
        strcat(classpath, classpathAfter);
    }
    sprintf(buf, "\"%s\"", classpath);
    *p++ = strdup(buf);
    
    *p++ = mainclass;

    for (i = 0; i < progArgc; i++) {
        sprintf(buf, "\"%s\"", progArgv[i]);
        *p++ = strdup(buf);
    }
    *p++ = NULL;

    // Prevents Windows from bringing up a "No floppy in drive A:" dialog.
    // The error mode should be inherited by the java process
    SetErrorMode(SetErrorMode(0) | SEM_FAILCRITICALERRORS | SEM_NOOPENFILEERRORBOX);
    
#ifdef DEBUG
    fflush(stdout);
#endif
    _spawnv(_P_WAIT, javapath, args);
}

//////////

/*
 * Returns string data for the specified registry value name, or
 * NULL if not found.
 */
static char * GetStringValue(HKEY key, const char *name)
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

// returns non zero if version is acceptable to run the application
// currently it means at least JDK 1.5
static int isAcceptable( const char *version) {
    if (version == NULL)
    	return 0;
    
    // should work for 1.4, 1.5, 1.5.0, 1.5.0_xy, 1.6, 1.6.0, 1.7
    return (strcmp(version, "1.5") >= 0)? 1: 0;
}

/* Looks for jre\bin\java.exe or bin\java.exe in given directory
   and return newly allocated char array pointing to this .exe file
   or NULL
*/
static char *findJavaExeInDirectory(char *dir) {
    char *javapath = (char *)malloc(strlen(dir)+20);
    WIN32_FIND_DATA ffd;

    strcat(strcpy(javapath, dir), "\\jre\\bin\\java.exe");
    if (INVALID_HANDLE_VALUE == FindFirstFile(javapath, &ffd)) {
        strcat(strcpy(javapath, dir), "\\bin\\java.exe");
        if (INVALID_HANDLE_VALUE == FindFirstFile(javapath, &ffd)) {
            free(javapath);
            return NULL;
        }
    }
    return javapath;
}

static int findJdkFromRegistry(const char* keyname, char jdkhome[])
{
    HKEY hkey = NULL, subkey = NULL;
    char *ver = NULL;
    int rc = 1;
  
    if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, keyname, 0, KEY_READ, &hkey) == 0) {
        ver = GetStringValue(hkey, "CurrentVersion");
        // try default Java
        if (isAcceptable(ver)) {
            if (RegOpenKeyEx(hkey, ver, 0, KEY_READ, &subkey) == 0) {
                char *home = GetStringValue(subkey, "JavaHome");
                if (home != NULL) {
                    strcpy(jdkhome, home);
                    if (fileExists(home)) {
#ifdef DEBUG
  printf("Found Java in registry (default, version=%s, path=%s)\n", ver, home);
#endif
                       rc = 0;
                    }
                    free(home);
                }
                if (subkey != NULL)
                    RegCloseKey(subkey);
            }
#ifdef DEBUG
  else {
    printf("Failed to open key %s under %s, error %ld\n", ver, keyname, GetLastError());
  }
#endif
        }
        if (rc != 0) {
            // try Java SE 6
            if (RegOpenKeyEx(hkey, "1.6", 0, KEY_READ, &subkey) == 0) {
                char *home = GetStringValue(subkey, "JavaHome");
                if (home != NULL) {
                    strcpy(jdkhome, home);
                    if (fileExists(home)) {
#ifdef DEBUG
  printf("Found Java in registry (1.6, path=%s)\n", home);
#endif
                       rc = 0;
                    }
                    free(home);
                }
                if (subkey != NULL)
                    RegCloseKey(subkey);
            }
        }
        if (rc != 0) {
            // try JDK 1.5
            if (RegOpenKeyEx(hkey, "1.5", 0, KEY_READ, &subkey) == 0) {
                char *home = GetStringValue(subkey, "JavaHome");
                if (home != NULL) {
                    strcpy(jdkhome, home);
                    if (fileExists(home)) {
#ifdef DEBUG
  printf("Found Java in registry (1.6, path=%s)\n", home);
#endif
                       rc = 0;
                    }
                    free(home);
                }
                if (subkey != NULL)
                    RegCloseKey(subkey);
            }
        }
    }

    if (ver != NULL)
        free(ver);
    if (hkey != NULL)
        RegCloseKey(hkey);
    return rc;
}

int findHttpProxyFromRegistry(char **proxy, char **nonProxy)
{
    HKEY hkey = NULL;
    char *proxyServer = NULL;
    char *proxyOverrides = NULL;
    int rc = 1;
    *proxy = NULL; *nonProxy = NULL;
  
    if (RegOpenKeyEx(HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Internet settings", 0, KEY_READ, &hkey) == 0) {
        DWORD proxyEnable, size = sizeof proxyEnable;

        if (RegQueryValueEx(hkey, "ProxyEnable", 0, 0, (unsigned char*) &proxyEnable, &size) == 0) {
            if (proxyEnable) {
                proxyServer = GetStringValue(hkey, "ProxyServer");
                if (proxyServer == NULL)
                    goto quit;

                if (strstr(proxyServer, "=") == NULL) {
                    *proxy = strdup(proxyServer);
                    rc = 0;
                } else {
                    char *pc = strstr(proxyServer, "http=");
                    if (pc != NULL) {
                        pc += strlen("http=");
                        char *qc = strstr(pc, ";");
                        if (qc != NULL)
                            *qc = '\0';
                        *proxy = strdup(pc);
                        rc = 0;
                    }
                }
                // ProxyOverride contains semicolon delimited list of host name prefixes 
                // to connect them directly w/o proxy
                // optionally last entry is <local> to bypass local addresses
                // *acme.com also works there
                proxyOverrides = GetStringValue(hkey, "ProxyOverride");
                if (proxyOverrides != NULL)
                    *nonProxy = strdup(proxyOverrides);
                else 
                    *nonProxy = strdup("");
            }
	    else {
	        *proxy = strdup("DIRECT");
	        *nonProxy = strdup("");
	        rc = 0;
            } 
        }
    }

  quit:
    if (proxyServer != NULL)
        free(proxyServer);
    if (proxyOverrides != NULL)
        free(proxyOverrides);
    if (hkey != NULL)
        RegCloseKey(hkey);
    return rc;
}

/* Reads value of http_proxy environment variable to use it as proxy setting
 * Returns 0 if it exists and is successfully parsed, other value if there
 * is some problem.
 */
int findHttpProxyFromEnv(char **proxy, char **nonProxy)
{
    char *proxyServer = NULL;
    char *proxyOverrides = NULL;
    int rc = 1;
    *proxy = NULL; *nonProxy = NULL;
  
    char *envvar = getenv("http_proxy");
    if (envvar != NULL) {
        // is it URL?
        if (strncmp(envvar, "http://", strlen("http://")) == 0
        && envvar[strlen(envvar)-1] == '/'
        && strlen(envvar) > strlen("http://")) {
            // trim URL part to keep only 'host[:port]'
            *proxy = strdup(envvar + strlen("http://"));
            (*proxy)[strlen(*proxy)-1] = '\0';
            *nonProxy = strdup("");
            rc = 0;
        }
    }
    return rc;
}

void addToClassPath(const char *pathprefix, const char *path) {
    char buf[1024];
    
    strcpy(buf, pathprefix);
    if (path != NULL)
        strcat(strcat(buf, "\\"), path);

    if (classpath[0] != '\0')
        strcat(classpath, ";");
    strcat(classpath, buf);
}

void addToClassPathIfExists(const char *pathprefix, const char *path) {
    char buf[1024];
 
    strcpy(buf, pathprefix);
    if (path != NULL)
        strcat(strcat(buf, "\\"), path);

    if (fileExists(buf) != 0)
        addToClassPath(pathprefix, path);
}

  
void addJdkJarsToClassPath(const char *jdkhome)
{
    addToClassPathIfExists(jdkhome, "lib\\dt.jar");
    addToClassPathIfExists(jdkhome, "lib\\tools.jar");
}

void addLauncherJarsToClassPath(const char *plathome)
{
    char buf[1024];

    strcat(strcpy(buf, plathome), "\\lib");
    addAllFilesToClassPath(buf, "*.jar");
    addAllFilesToClassPath(buf, "*.zip");

    strcat(strcpy(buf, plathome), "\\lib\\locale");
    addAllFilesToClassPath(buf, "*.jar");
    addAllFilesToClassPath(buf, "*.zip");

    if (runupdater) {
        char userUpdater[MAX_PATH] = "";
        _snprintf(userUpdater, MAX_PATH, "%s\\modules\\ext\\updater.jar", userdir);
        const char *baseUpdaterPath = plathome;
        if (fileExists(userUpdater))
            baseUpdaterPath = userdir;
        addToClassPath(baseUpdaterPath, "\\modules\\ext\\updater.jar");
        strcat(strcpy(buf, baseUpdaterPath), "\\modules\\ext\\locale");
        addAllFilesToClassPath(buf, "updater_*.jar");
    }
}

void addAllFilesToClassPath(const char *dir,
                            const char *pattern) {
    char buf[1024];
    struct _finddata_t fileinfo;
    long hFile;

    strcat(strcat(strcpy(buf, dir), "\\"), pattern);
  
    if ((hFile = _findfirst(buf, &fileinfo)) != -1L) {
        addToClassPath(dir, fileinfo.name);

        while (0 == _findnext(hFile, &fileinfo))
            addToClassPath(dir, fileinfo.name);
    
        _findclose(hFile);
    }
}

void fatal(const char *str)
{
//#ifdef WINMAIN
    ::MessageBox(NULL, str, PROG_FULLNAME, MB_ICONSTOP | MB_OK);
//#else  
//  fprintf(stderr, "%s\n", str);
//#endif
    exit(255);
}

/*
 * Adds a new VM option with the given given name and value.
 *
 * Doesn't modify the input string, creates its copy.
 *
 */
void addOption(char *str)
{
    /*
     * Expand options array if needed to accomodate at least one more
     * VM option.
     */
    if (numOptions >= maxOptions) {
        if (options == 0) {
            maxOptions = 4;
            options = (char**) malloc(maxOptions * sizeof(char*));
        } else {
            char** tmp;
            maxOptions *= 2;
            tmp = (char**)malloc(maxOptions * sizeof(char*));
            memcpy(tmp, options, numOptions * sizeof(char*));
            free(options);
            options = tmp;
        }
    }
    options[numOptions++] = strdup(str);
}

void parseArgs(int argc, char *argv[]) {
    char *arg;

    while (argc > 0 && (arg = *argv) != 0) {
        argv++;
        argc--;

#ifdef DEBUG
            printf("parseArgs - processing %s\n", arg);
#endif

        if ((strcmp("-h", arg) == 0
            || strcmp("-help", arg) == 0
            || strcmp("--help", arg) == 0
            || strcmp("/?", arg) == 0
            ) && runnormal) {
            fprintf(stdout, "Usage: launcher {options} arguments\n\
\n\
General options:\n\
  --help                show this help\n\
  --jdkhome <path>      path to JDK\n\
  -J<jvm_option>        pass <jvm_option> to JVM\n\
\n\
  --cp:p <classpath>    prepend <classpath> to classpath\n\
  --cp:a <classpath>    append <classpath> to classpath\n\
\n");  
            fflush(stdout);
            arg = "--help";
        }
        

        if (0 == strcmp("--userdir", arg)) {
            if (argc > 0) {
                arg = *argv;
                argv++;
                argc--;
                if (arg != 0) {
                    strcpy(userdir, arg);
                    normalizePath(userdir);
                }
            }
            else {
                fatal(BAD_OPTION_MSG);
            }
        } else if (0 == strcmp("--clusters", arg)) {
            if (argc > 0) {
                arg = *argv;
                argv++;
                argc--;
                if (arg != 0) {
                    strcpy(clusters, arg);
                }
            }
            else {
                fatal(BAD_OPTION_MSG);
            }
        } else if (0 == strcmp("--bootclass", arg)) {
            if (argc > 0) {
                arg = *argv;
                argv++;
                argc--;
                if (arg != 0) {
                    bootclass = strdup(arg);
                }
            }
            else {
                fatal(BAD_OPTION_MSG);
            }
        } else if (0 == strcmp(RUN_NORMAL, arg)) {
            runnormal = 1;
            runupdater = 0;
        } else if (0 == strcmp(RUN_UPDATER, arg)) {
            runnormal = 0;
            runupdater = 1;
        } else if (0 == strcmp("--jdkhome", arg)) {
            if (argc > 0) {
                arg = *argv;
                argv++;
                argc--;
                if (arg != 0 && findJavaExeInDirectory(arg) != NULL) {
                    strcpy(jdkhome, arg);
                }
            }
            else {
                fatal(BAD_OPTION_MSG);
            }
        } else if (0 == strcmp("-cp:p", arg) || 0 == strcmp("--cp:p", arg)) {
            if (argc > 0) {
                arg = *argv;
                argv++;
                argc--;
                if (arg != 0) {
                    if (classpathBefore[0] != '\0'
                        && classpathBefore[strlen(classpathBefore)] != ';')
                        strcat(classpathBefore, ";");
                    strcat(classpathBefore, arg);
                }
            }
            else {
                fatal(BAD_OPTION_MSG);
            }
        } else if (0 == strcmp("-cp", arg) || 0 == strcmp("-cp:a", arg) || 0 == strcmp("--cp", arg) || 0 == strcmp("--cp:a", arg)) {
            if (argc > 0) {
                arg = *argv;
                argv++;
                argc--;
                if (arg != 0) {
                    if (classpathAfter[0] != '\0'
                        && classpathAfter[strlen(classpathAfter)] != ';')
                        strcat(classpathAfter, ";");
                    strcat(classpathAfter, arg);
                }
            }
            else {
                fatal(BAD_OPTION_MSG);
            }
        } else if (0 == strncmp("-J", arg, 2)) {
            addOption(arg + 2);
        } else {
            progArgv[progArgc++] = arg;
        }
    }
}

void normalizePath(char *userdir) {
    char buf[MAX_PATH], *pc;

    // absolutize userdir
    if (NULL == _fullpath(buf, userdir, MAX_PATH))
        return;
    
    userdir[0] = '\0';

    if (buf[0] == '\\' && buf[1] == '\\') { // UNC share
        userdir[0] = '\\';
        userdir[1] = '\\';
        userdir[2] = '\0';
        pc = strtok(buf + 2, "/\\");
    } else {
        pc = strtok(buf, "/\\");
    }
  
    while (pc != NULL) {
        if (*pc != '\0') {
            if (userdir[0] != '\0' && userdir[strlen(userdir) - 1] != '\\')
                strcat(userdir, "\\");
            strcat(userdir, pc);
        }
        pc = strtok(NULL,  "/\\");
    }
    if (userdir[1] == ':' && userdir[2] == '\0')
        strcat(userdir, "\\");
}

int fileExists(const char* path) {
    WIN32_FIND_DATA ffd;
    HANDLE ffh;
    
    memset(&ffd, 0, sizeof ffd);
    ffh = FindFirstFile(path, &ffd);
    if (ffh != INVALID_HANDLE_VALUE) {
        FindClose(ffh);
        return 1;
    } else {
        return 0;
    }
}

/** Looks for ${userdir}\update\download\netbeans.dirs and reads 
 *  list of clusters.
 *  @return newly allocated string with file content if file exists 
 *          or NULL if it is not found
 */
char* processAUClustersList(char *userdir) {
    if (userdir == NULL || userdir[0] == '\0') 
        return NULL;
    char *pPath = (char*)malloc(strlen(userdir)+32);
    if (pPath == NULL) fatal("Cannot allocate memory (processAUClustersList).");

    strcpy(pPath, userdir);
    strcat(pPath, "\\update\\download\\netbeans.dirs");

#ifdef DEBUG
    if (fileExists(pPath)) printf("Found file with new clusters %s\n", pPath);
#endif
    FILE* fin = fopen(pPath, "r");
    if (fin == NULL)
        return 0;

    long len = _filelength(_fileno(fin));
    char *pClusters = (char*)malloc(len+1);
    if (fgets(pClusters, len+1, fin)) {
        while(strlen(pClusters) > 1 && 
          (pClusters[strlen(pClusters) - 1] == '\r' || pClusters[strlen(pClusters) - 1] == '\n')) {
            pClusters[strlen(pClusters) - 1] = '\0';
        }
        fclose(fin);
#ifdef DEBUG
        printf("Overriden clusters %s\n", pClusters);
#endif
        return pClusters;
    }
    fclose(fin);
    return NULL;
}

/** Deletes ${userdir}\update\download\netbeans.dirs if it exists.
 */
int removeAUClustersListFile(char* userdir) {
    if (userdir == NULL || userdir[0] == '\0') 
        return 0;
    char *pPath = (char*)malloc(strlen(userdir)+32);
    if (pPath == NULL) fatal("Cannot allocate memory (removeAUClustersListFile).");

    strcpy(pPath, userdir);
    strcat(pPath, "\\update\\download\\netbeans.dirs");
    if (remove(pPath) != 0) {
        if (errno != ENOENT) return -1; // an error while deleting
    }
    return 0;
}

// check if new updater exists, if exists install it (replace old one) and remove ...\new_updater directory
int checkForNewUpdater(const char *basePath)
{
    char srcPath[MAX_PATH] = "";
    _snprintf(srcPath, MAX_PATH, "%s\\update\\new_updater\\updater.jar", basePath);
    WIN32_FIND_DATA fd = {0};
    HANDLE hFind = FindFirstFile(srcPath, &fd);
    if (hFind != INVALID_HANDLE_VALUE)
    {
        FindClose(hFind);
        char destPath[MAX_PATH] = "";
        _snprintf(destPath, MAX_PATH, "%s\\modules\\ext", basePath);
        if (!CreateDirectory(destPath, 0) && GetLastError() != ERROR_ALREADY_EXISTS)
                return -1;
        strncat(destPath, "\\updater.jar", MAX_PATH - strlen(destPath));
        if (!MoveFileEx(srcPath, destPath, MOVEFILE_REPLACE_EXISTING | MOVEFILE_WRITE_THROUGH))
            return -1;
        _snprintf(srcPath, MAX_PATH, "%s\\update\\new_updater", basePath);
        RemoveDirectory(srcPath);
    }
    return 0;
}
