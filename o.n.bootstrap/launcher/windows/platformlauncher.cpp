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
#include "platformlauncher.h"
#include "argnames.h"

using namespace std;

const char *PlatformLauncher::HELP_MSG =
"\nUsage: launcher {options} arguments\n\
\n\
General options:\n\
  --help                show this help\n\
  --jdkhome <path>      path to JDK\n\
  -J<jvm_option>        pass <jvm_option> to JVM\n\
\n\
  --cp:p <classpath>    prepend <classpath> to classpath\n\
  --cp:a <classpath>    append <classpath> to classpath\n\
\n\
  --fork-java           run java in separate process\n\
  --trace <path>        path for launcher log (for trouble shooting)\n\
\n";

const char *PlatformLauncher::REQ_JAVA_VERSION = "1.5";

const char *PlatformLauncher::OPT_JDK_HOME = "-Djdk.home=";
const char *PlatformLauncher::OPT_NB_PLATFORM_HOME = "-Dnetbeans.home=";
const char *PlatformLauncher::OPT_NB_CLUSTERS = "-Dnetbeans.dirs=";
const char *PlatformLauncher::OPT_NB_USERDIR = "-Dnetbeans.user=";
const char *PlatformLauncher::OPT_HTTP_PROXY = "-Dnetbeans.system_http_proxy=";
const char *PlatformLauncher::OPT_HTTP_NONPROXY = "-Dnetbeans.system_http_non_proxy_hosts=";
const char *PlatformLauncher::OPT_SOCKS_PROXY = "-Dnetbeans.system_socks_proxy=";
const char *PlatformLauncher::OPT_HEAP_DUMP = "-XX:+HeapDumpOnOutOfMemoryError";
const char *PlatformLauncher::OPT_HEAP_DUMP_PATH = "-XX:HeapDumpPath=";
const char *PlatformLauncher::OPT_KEEP_WORKING_SET_ON_MINIMIZE = "-Dsun.awt.keepWorkingSetOnMinimize=true";
const char *PlatformLauncher::OPT_CLASS_PATH = "-Djava.class.path=";

const char *PlatformLauncher::REG_PROXY_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Internet settings";
const char *PlatformLauncher::REG_PROXY_ENABLED_NAME = "ProxyEnable";
const char *PlatformLauncher::REG_PROXY_SERVER_NAME = "ProxyServer";
const char *PlatformLauncher::REG_PROXY_OVERRIDE_NAME = "ProxyOverride";
const char *PlatformLauncher::PROXY_DIRECT = "DIRECT";
const char *PlatformLauncher::HEAP_DUMP_PATH =  "\\var\\log\\heapdump.hprof";

const char *PlatformLauncher::UPDATER_MAIN_CLASS = "org/netbeans/updater/UpdaterFrame";
const char *PlatformLauncher::IDE_MAIN_CLASS = "org/netbeans/Main";

PlatformLauncher::PlatformLauncher()
    : separateProcess(false) {
}

PlatformLauncher::PlatformLauncher(const PlatformLauncher& orig) {
}

PlatformLauncher::~PlatformLauncher() {
}

bool PlatformLauncher::start(char* argv[], int argc, DWORD *retCode) {
    if (!checkLoggingArg(argc, argv, false) || !initPlatformDir() || !parseArgs(argc, argv)) {
        return false;
    }
    disableFolderVirtualization(GetCurrentProcess());

    if (jdkhome.empty()) {
        if (!jvmLauncher.initialize(REQ_JAVA_VERSION)) {
            logErr(false, true, "Cannot find Java %s or higher.", REQ_JAVA_VERSION);
            return false;
        }
    }
    jvmLauncher.getJavaPath(jdkhome);

    prepareOptions();

    if (nextAction.empty()) {
        if (shouldAutoUpdateClusters(true)) {
            // run updater
            if (!run(true, retCode)) {
                return false;
            }
        }

        while (true) {
            // run app
            if (!run(false, retCode)) {
                return false;
            }

            if (shouldAutoUpdateClusters(false)) {
                // run updater
                if (!run(true, retCode)) {
                    return false;
                }
            } else {
                break;
            }
        }
    } else {
        if (nextAction == ARG_NAME_LA_START_APP) {
            return run(false, retCode);
        } else if (nextAction == ARG_NAME_LA_START_AU) {
            return run(true, retCode);
        } else {
            logErr(false, true, "We should not get here.");
            return false;
        }
    }

    return true;
}

bool PlatformLauncher::run(bool updater, DWORD *retCode) {
    constructClassPath(updater);
    const char *mainClass;
    if (updater) {
        mainClass = UPDATER_MAIN_CLASS;
        nextAction = ARG_NAME_LA_START_APP;
    } else {
        mainClass = bootclass.empty() ? IDE_MAIN_CLASS : bootclass.c_str();
        nextAction = ARG_NAME_LA_START_AU;
    }

    string option = OPT_CLASS_PATH;
    option += classPath;
    javaOptions.push_back(option);
    bool rc = jvmLauncher.start(mainClass, progArgs, javaOptions, separateProcess, retCode);
    javaOptions.pop_back();
    return rc;
}



bool PlatformLauncher::initPlatformDir() {
    char path[MAX_PATH] = "";
    getCurrentModulePath(path, MAX_PATH);
    logMsg("Module: %s", path);
    char *bslash = strrchr(path, '\\');
    if (!bslash) {
        return false;
    }
    *bslash = '\0';
    bslash = strrchr(path, '\\');
    if (!bslash) {
        return false;
    }
    *bslash = '\0';
    clusters = platformDir = path;
    logMsg("Platform dir: %s", platformDir.c_str());
    return true;
}

bool PlatformLauncher::parseArgs(int argc, char *argv[]) {
#define CHECK_ARG \
    if (i+1 == argc) {\
        logErr(false, true, "Argument is missing for \"%s\" option.", argv[i]);\
        return false;\
    }

    logMsg("Parsing arguments:");
    for (int i = 0; i < argc; i++) {
        logMsg("\t%s", argv[i]);
    }

    for (int i = 0; i < argc; i++) {
        if (strcmp(ARG_NAME_SEPAR_PROC, argv[i]) == 0) {
            separateProcess = true;
            logMsg("Run Java in separater process");
        } else if (strcmp(ARG_NAME_LAUNCHER_LOG, argv[i]) == 0) {
            CHECK_ARG;
            i++;
        } else if (strcmp(ARG_NAME_LA_START_APP, argv[i]) == 0
                || strcmp(ARG_NAME_LA_START_AU, argv[i]) == 0) {
            nextAction = argv[i++];
            logMsg("Next launcher action: %s", nextAction.c_str());
        } else if (strcmp(ARG_NAME_USER_DIR, argv[i]) == 0) {
            CHECK_ARG;
            char tmp[MAX_PATH + 1] = {0};
            strncpy(tmp, argv[++i], MAX_PATH);
            if (!normalizePath(tmp)) {
                logErr(false, true, "User directory path \"%s\" is not valid.", argv[i]);
                return false;
            }
            userDir = tmp;
            logMsg("User dir: %s", userDir.c_str());
        } else if (strcmp(ARG_NAME_CLUSTERS, argv[i]) == 0) {
            CHECK_ARG;
            clusters = argv[++i];
        } else if (strcmp(ARG_NAME_BOOTCLASS, argv[i]) == 0) {
            CHECK_ARG;
            bootclass = argv[++i];
        } else if (strcmp(ARG_NAME_JDKHOME, argv[i]) == 0) {
            CHECK_ARG;
            jdkhome = argv[++i];
            if (!jvmLauncher.initialize(jdkhome.c_str())) {
                logMsg("Cannot locate java installation in specified jdkhome: %s", jdkhome.c_str());
                string errMsg = "Cannot locate java installation in specified jdkhome:\n";
                errMsg += jdkhome;
                errMsg += "\nDo you want to try to use default version?";
                jdkhome = "";
                if (::MessageBox(NULL, errMsg.c_str(), "Invalid jdkhome specified", MB_ICONQUESTION | MB_YESNO) == IDNO) {
                    return false;
                }
            }
        } else if (strcmp(ARG_NAME_CP_PREPEND, argv[i]) == 0
                || strcmp(ARG_NAME_CP_PREPEND + 1, argv[i]) == 0) {
            CHECK_ARG;
            cpBefore += argv[++i];
        } else if (strcmp(ARG_NAME_CP_APPEND, argv[i]) == 0
                || strcmp(ARG_NAME_CP_APPEND + 1, argv[i]) == 0
                || strncmp(ARG_NAME_CP_APPEND + 1, argv[i], 3) == 0
                || strncmp(ARG_NAME_CP_APPEND, argv[i], 4) == 0) {
            CHECK_ARG;
            cpAfter += argv[++i];
        } else if (strncmp("-J", argv[i], 2) == 0) {
            javaOptions.push_back(argv[i] + 2);
        } else {
            if (strcmp(argv[i], "-h") == 0
                    || strcmp(argv[i], "-help") == 0
                    || strcmp(argv[i], "--help") == 0
                    || strcmp(argv[i], "/?") == 0) {
                FILE *console = fopen("CON", "a");
                if (console) {
                    fprintf(console, "%s", HELP_MSG);
                    fclose(console);
                }
            }
            progArgs.push_back(argv[i]);
        }
    }
    return true;
}

bool PlatformLauncher::processAutoUpdateCL() {
    auClusters = "";
    logMsg("processAutoUpdateCL()...");
    if (userDir.empty()) {
        logMsg("\tuserdir empty, quiting");
        return false;
    }
    string listPath = userDir;
    listPath += "\\update\\download\\netbeans.dirs";

    WIN32_FIND_DATA fd = {0};
    HANDLE hFind = 0;
    hFind = FindFirstFile(listPath.c_str(), &fd);
    if (hFind == INVALID_HANDLE_VALUE) {
        logMsg("File \"%s\" does not exist", listPath.c_str());
        return false;
    }
    FindClose(hFind);

    FILE *file = fopen(listPath.c_str(), "r");
    if (!file) {
        logErr(true, false, "Cannot open file %s", listPath.c_str());
        return false;
    }

    int len = fd.nFileSizeLow + 1;
    char *str = new char[len];
    if (!fgets(str, len, file)) {
        fclose(file);
        delete[] str;
        logErr(true, false, "Cannot read from file %s", listPath.c_str());
        return false;
    }
    len = strlen(str) - 1;
    if (str[len] == '\n') {
        str[len] = '\0';
    }

    auClusters = str;
    fclose(file);
    delete[] str;
    return true;
}

// check if new updater exists, if exists install it (replace old one) and remove ...\new_updater directory
bool PlatformLauncher::checkForNewUpdater(const char *basePath) {
    logMsg("checkForNewUpdater() at %s", basePath);
    string srcPath = basePath;
    srcPath += "\\update\\new_updater\\updater.jar";
    WIN32_FIND_DATA fd = {0};
    HANDLE hFind = FindFirstFile(srcPath.c_str(), &fd);
    if (hFind != INVALID_HANDLE_VALUE) {
        logMsg("New updater found: %s", srcPath.c_str());
        FindClose(hFind);
        string destPath = basePath;
        destPath += "\\modules\\ext\\updater.jar";
        createPath(destPath.c_str());

        if (!MoveFileEx(srcPath.c_str(), destPath.c_str(), MOVEFILE_REPLACE_EXISTING | MOVEFILE_WRITE_THROUGH)) {
            logErr(true, false, "Failed to move \"%s\" to \"%s\"", srcPath.c_str(), destPath.c_str());
            return false;
        }

        srcPath.erase(srcPath.rfind('\\'));
        logMsg("Removing directory \"%s\"", srcPath.c_str());
        if (!RemoveDirectory(srcPath.c_str())) {
            logErr(true, false, "Failed to remove directory \"%s\"", srcPath.c_str());
        }
    } else {
        logMsg("No new updater at %s", srcPath.c_str());
    }
    return true;
}

bool PlatformLauncher::shouldAutoUpdate(bool firstStart, const char *basePath) {
    // The logic is following:
    // if there is an NBM for installation then run updater
    // unless it is not a first start and we asked to install later (on next start)

    // then also check if last run left list of modules to disable/uninstall and
    // did not mark them to be deactivated later (on next start)
    string path = basePath;
    path += "\\update\\download\\*.nbm";
    logMsg("Checking for updates: %s", path.c_str());
    WIN32_FIND_DATA fd;
    HANDLE hFindNbms = FindFirstFile(path.c_str(), &fd);
    if (hFindNbms != INVALID_HANDLE_VALUE) {
        logMsg("Some updates found.");
        FindClose(hFindNbms);
    }

    path = basePath;
    path += "\\update\\download\\install_later.xml";
    HANDLE hFind = FindFirstFile(path.c_str(), &fd);
    if (hFind != INVALID_HANDLE_VALUE) {
        logMsg("install_later.xml found: %s", path.c_str());
        FindClose(hFind);
    }

    if (hFindNbms != INVALID_HANDLE_VALUE && (firstStart || hFind == INVALID_HANDLE_VALUE)) {
        return true;
    }

    path = basePath;
    path += "\\update\\deactivate\\deactivate_later.txt";
    hFind = FindFirstFile(path.c_str(), &fd);
    if (hFind != INVALID_HANDLE_VALUE) {
        logMsg("deactivate_later.txt found: %s", path.c_str());
        FindClose(hFind);
    }

    if (firstStart || hFind == INVALID_HANDLE_VALUE) {
        path = basePath;
        path += "\\update\\deactivate\\to_disable.txt";
        hFind = FindFirstFile(path.c_str(), &fd);
        if (hFind != INVALID_HANDLE_VALUE) {
            logMsg("to_disable.txt found: %s", path.c_str());
            FindClose(hFind);
            return true;
        }

        path = basePath;
        path += "\\update\\deactivate\\to_uninstall.txt";
        hFind = FindFirstFile(path.c_str(), &fd);
        if (hFind != INVALID_HANDLE_VALUE) {
            logMsg("to_uninstall.txt found: %s", path.c_str());
            FindClose(hFind);
            return true;
        }
    }

    return false;
}

bool PlatformLauncher::shouldAutoUpdateClusters(bool firstStart) {
    bool runUpdater = false;
    string cl = processAutoUpdateCL() ? auClusters : clusters;
    checkForNewUpdater(platformDir.c_str());
    runUpdater = shouldAutoUpdate(firstStart, platformDir.c_str());

    const char delim = ';';
    string::size_type start = cl.find_first_not_of(delim, 0);
    string::size_type end = cl.find_first_of(delim, start);
    while (string::npos != end || string::npos != start) {
        string cluster = cl.substr(start, end - start);
        checkForNewUpdater(cluster.c_str());
        if (!runUpdater) {
            runUpdater = shouldAutoUpdate(firstStart, cluster.c_str());
        }
        start = cl.find_first_not_of(delim, end);
        end = cl.find_first_of(delim, start);
    }

    checkForNewUpdater(userDir.c_str());
    if (!runUpdater) {
        runUpdater = shouldAutoUpdate(firstStart, userDir.c_str());
    }
    return runUpdater;
}

void PlatformLauncher::prepareOptions() {
    string option = OPT_JDK_HOME;
    option += jdkhome;
    javaOptions.push_back(option);

    option = OPT_NB_PLATFORM_HOME;
    option += platformDir;
    javaOptions.push_back(option);

    option = OPT_NB_CLUSTERS;
    option += auClusters.empty() ? clusters : auClusters;
    javaOptions.push_back(option);

    option = OPT_NB_USERDIR;
    option += userDir;
    javaOptions.push_back(option);

    option = OPT_HEAP_DUMP;
    javaOptions.push_back(option);

    option = OPT_HEAP_DUMP_PATH;
    option += userDir;
    option += HEAP_DUMP_PATH;
    javaOptions.push_back(option);

    string proxy, nonProxy, socksProxy;
    if (!findHttpProxyFromEnv(proxy)) {
        findProxiesFromRegistry(proxy, nonProxy, socksProxy);
    }
    if (!proxy.empty()) {
        option = OPT_HTTP_PROXY;
        option += proxy;
        javaOptions.push_back(option);
    }
    if (!nonProxy.empty()) {
        option = OPT_HTTP_NONPROXY;
        option += nonProxy;
        javaOptions.push_back(option);
    }
    if (!socksProxy.empty()) {
        option = OPT_SOCKS_PROXY;
        option += socksProxy;
        javaOptions.push_back(option);
    }

    option = OPT_KEEP_WORKING_SET_ON_MINIMIZE;
    javaOptions.push_back(option);
}

// Reads value of http_proxy environment variable to use it as proxy setting
bool PlatformLauncher::findHttpProxyFromEnv(string &proxy) {
    logMsg("findHttpProxyFromEnv()");
    char *envVar = getenv("http_proxy");
    if (envVar) {
        // is it URL?
        int prefixLen = strlen("http://");
        if (strncmp(envVar, "http://", prefixLen) == 0 && envVar[strlen(envVar) - 1] == '/'
                && strlen(envVar) > strlen("http://")) {
            // trim URL part to keep only 'host[:port]'
            proxy = envVar + prefixLen;
            proxy.erase(proxy.size() - 1);
            logMsg("Found proxy in environment variable: %s", proxy.c_str());
            return true;
        }
    }
    return false;
}

bool PlatformLauncher::findProxiesFromRegistry(string &proxy, string &nonProxy, string &socksProxy) {
    logMsg("findProxiesFromRegistry()");
    socksProxy = nonProxy = proxy = "";
    DWORD proxyEnable = 0;
    if (!getDwordFromRegistry(HKEY_CURRENT_USER, REG_PROXY_KEY, REG_PROXY_ENABLED_NAME, proxyEnable)) {
        return false;
    }

    if (!proxyEnable) {
        logMsg("Proxy disabled");
        proxy = PROXY_DIRECT;
        return true;
    }

    string proxyServer;
    if (!getStringFromRegistry(HKEY_CURRENT_USER, REG_PROXY_KEY, REG_PROXY_SERVER_NAME, proxyServer)) {
        return false;
    }

    if (proxyServer.find('=') == string::npos) {
        proxy = proxyServer;
    } else {
        string::size_type pos = proxyServer.find("socks=");
        if (pos != string::npos) {
            if (proxyServer.size() > pos + 1 && proxyServer.at(pos) != ';') {
                string::size_type endPos = proxyServer.find(';', pos);
                socksProxy = proxyServer.substr(pos, endPos == string::npos ? string::npos : endPos - pos);
            }
        }
        pos = proxyServer.find("http=");
        if (pos != string::npos) {
            string::size_type endPos = proxyServer.find(';', pos);
            proxy = proxyServer.substr(pos, endPos == string::npos ? string::npos : endPos - pos);
        }
    }
    logMsg("Proxy servers:\n\tproxy: %s\n\tsocks proxy: %s\n\tnonProxy: %s", proxy.c_str(), socksProxy.c_str(), nonProxy.c_str());
    getStringFromRegistry(HKEY_CURRENT_USER, REG_PROXY_KEY, REG_PROXY_OVERRIDE_NAME, nonProxy);
    return true;
}

string & PlatformLauncher::constructClassPath(bool runUpdater) {
    logMsg("constructClassPath()");
    addedToCP.clear();
    classPath = cpBefore;

    addJarsToClassPathFrom(userDir.c_str());
    addJarsToClassPathFrom(platformDir.c_str());

    if (runUpdater) {
        const char *baseUpdaterPath = userDir.c_str();
        string updaterPath = userDir + "\\modules\\ext\\updater.jar";

        // if user updater does not exist, use updater from platform
        if (!fileExists(updaterPath.c_str())) {
            baseUpdaterPath = platformDir.c_str();
            updaterPath = platformDir + "\\modules\\ext\\updater.jar";
        }

        addToClassPath(updaterPath.c_str(), false);
        addFilesToClassPath(baseUpdaterPath, "\\modules\\ext\\locale", "updater_*.jar");
    }

    addToClassPath((jdkhome + "\\lib\\dt.jar").c_str(), true);
    addToClassPath((jdkhome + "\\lib\\tools.jar").c_str(), true);

    classPath += cpAfter;
    logMsg("ClassPath: %s", classPath.c_str());
    return classPath;
}

void PlatformLauncher::addJarsToClassPathFrom(const char *dir) {
    addFilesToClassPath(dir, "lib\\patches", "*.jar");
    addFilesToClassPath(dir, "lib\\patches", "*.zip");

    addFilesToClassPath(dir, "lib", "*.jar");
    addFilesToClassPath(dir, "lib", "*.zip");

    addFilesToClassPath(dir, "lib\\locale", "*.jar");
    addFilesToClassPath(dir, "lib\\locale", "*.zip");
}

void PlatformLauncher::addFilesToClassPath(const char *dir, const char *subdir, const char *pattern) {
    logMsg("addFilesToClassPath()\n\tdir: %s\n\tsubdir: %s\n\tpattern: %s", dir, subdir, pattern);
    string path = dir;
    path += '\\';
    path += subdir;
    path += '\\';

    WIN32_FIND_DATA fd = {0};
    string patternPath = path + pattern;
    HANDLE hFind = FindFirstFile(patternPath.c_str(), &fd);
    if (hFind == INVALID_HANDLE_VALUE) {
        logMsg("Nothing found (%s)", patternPath.c_str());
        return;
    }
    do {
        string name = subdir;
        name += fd.cFileName;
        string fullName = path + fd.cFileName;
        if (addedToCP.insert(name).second) {
            addToClassPath(fullName.c_str());
        } else {
            logMsg("\"%s\" already added, skipping \"%s\"", name.c_str(), fullName.c_str());
        }
    } while (FindNextFile(hFind, &fd));
    FindClose(hFind);
}

void PlatformLauncher::addToClassPath(const char *path, bool onlyIfExists) {
    logMsg("addToClassPath()\n\tpath: %s\n\tonlyIfExists: %s", path, onlyIfExists ? "true" : "false");
    if (onlyIfExists && !fileExists(path)) {
        return;
    }

    if (!classPath.empty()) {
        classPath += ';';
    }
    classPath += path;
}

void PlatformLauncher::onExit() {
    logMsg("onExit()");
    if (separateProcess) {
        logMsg("JVM in separate process, no need to restart");
        return;
    }
    if (nextAction == ARG_NAME_LA_START_APP || (nextAction == ARG_NAME_LA_START_AU && shouldAutoUpdateClusters(false))) {
        string cmdLine = GetCommandLine();
        logMsg("Old command line: %s", cmdLine.c_str());
        string::size_type bslashPos = cmdLine.find_last_of('\\');
        string::size_type pos = cmdLine.find(ARG_NAME_LA_START_APP);
        if (bslashPos < pos && pos != string::npos) {
            cmdLine.erase(pos, strlen(ARG_NAME_LA_START_APP));
        }
        pos = cmdLine.find(ARG_NAME_LA_START_AU);
        if (bslashPos < pos && pos != string::npos) {
            cmdLine.erase(pos, strlen(ARG_NAME_LA_START_AU));
        }
        if (*cmdLine.rbegin() != ' ') {
            cmdLine += ' ';
        }
        cmdLine += nextAction;
        logMsg("New command line: %s", cmdLine.c_str());

        char cmdLineStr[32 * 1024] = "";
        strcpy(cmdLineStr, cmdLine.c_str());
        STARTUPINFO si = {0};
        PROCESS_INFORMATION pi = {0};
        si.cb = sizeof(STARTUPINFO);
        if (!CreateProcess(NULL, cmdLineStr, NULL, NULL, FALSE, 0, NULL, NULL, &si, &pi)) {
            logErr(true, true, "Failed to create process.");
            return;
        }
        // wait for a while so our child process can attach our console
        Sleep(1000);
        CloseHandle(pi.hThread);
        CloseHandle(pi.hProcess);
    }
}
