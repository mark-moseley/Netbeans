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

#include "jvmlauncher.h"
#include <cassert>

using namespace std;

const char *JvmLauncher::JDK_KEY = "Software\\JavaSoft\\Java Development Kit";
const char *JvmLauncher::JRE_KEY = "Software\\JavaSoft\\Java Runtime Environment";
const char *JvmLauncher::CUR_VERSION_NAME = "CurrentVersion";
const char *JvmLauncher::JAVA_HOME_NAME = "JavaHome";
const char *JvmLauncher::JAVA_BIN_DIR = "\\bin";
const char *JvmLauncher::JAVA_EXE_FILE = "\\bin\\java.exe";
const char *JvmLauncher::JAVA_CLIENT_DLL_FILE = "\\bin\\client\\jvm.dll";
const char *JvmLauncher::JAVA_SERVER_DLL_FILE = "\\bin\\server\\jvm.dll";
const char *JvmLauncher::JAVA_JRE_PREFIX = "\\jre";
const char *JvmLauncher::JNI_CREATEVM_FUNC = "JNI_CreateJavaVM";


JvmLauncher::JvmLauncher() {
}

JvmLauncher::JvmLauncher(const JvmLauncher& orig) {
}

JvmLauncher::~JvmLauncher() {
}

bool JvmLauncher::checkJava(const char *path, const char *prefix) {
    assert(path);
    assert(prefix);
    logMsg("checkJava(%s)", path);
    javaPath = path;
    if (*javaPath.rbegin() == '\\') {
        javaPath.erase(javaPath.length() - 1, 1);
    }
    javaExePath = javaPath + prefix + JAVA_EXE_FILE;
    javaClientDllPath = javaPath + prefix + JAVA_CLIENT_DLL_FILE;
    javaServerDllPath = javaPath + prefix + JAVA_SERVER_DLL_FILE;
    if (!fileExists(javaClientDllPath.c_str())) {
        javaClientDllPath = "";
    }
    if (!fileExists(javaServerDllPath.c_str())) {
        javaServerDllPath = "";
    }
    javaBinPath = javaPath + prefix + JAVA_BIN_DIR;
    if (fileExists(javaExePath.c_str()) && (!javaClientDllPath.empty() || !javaServerDllPath.empty())) {
        return true;
    }

    javaPath.clear();
    javaBinPath.clear();
    javaExePath.clear();
    javaClientDllPath.clear();
    javaServerDllPath.clear();
    return false;
}

bool JvmLauncher::initialize(const char *javaPathOrMinVersion) {
    logMsg("JvmLauncher::initialize()\n\tjavaPathOrMinVersion: %s", javaPathOrMinVersion);
    assert(javaPathOrMinVersion);
    if (isVersionString(javaPathOrMinVersion)) {
        return findJava(javaPathOrMinVersion);
    } else {
        return (checkJava(javaPathOrMinVersion, JAVA_JRE_PREFIX) || checkJava(javaPathOrMinVersion, ""));
    }
}

bool JvmLauncher::getJavaPath(string &path) {
    logMsg("JvmLauncher::getJavaPath()");
    path = javaPath;
    return !javaPath.empty();
}

bool JvmLauncher::start(const char *mainClassName, list<string> args, list<string> options, bool &separateProcess, DWORD *retCode) {
    assert(mainClassName);
    logMsg("JvmLauncher::start()\n\tmainClassName: %s\n\tseparateProcess: %s",
            mainClassName, separateProcess ? "true" : "false");
    logMsg("  args:");
    for (list<string>::iterator it = args.begin(); it != args.end(); ++it) {
        logMsg("\t%s", it->c_str());
    }
    logMsg("  options:");
    for (list<string>::iterator it = options.begin(); it != options.end(); ++it) {
        logMsg("\t%s", it->c_str());
    }

    if (javaExePath.empty() || (javaClientDllPath.empty() && javaServerDllPath.empty())) {
        if (!initialize("")) {
            return false;
        }
    }

    if (!separateProcess) {
        // both client/server found, check option which should be used
        if (!javaClientDllPath.empty() && !javaServerDllPath.empty()) {
            javaDllPath = findClientOption(options) ? javaClientDllPath : javaServerDllPath;
        } else {
            javaDllPath = javaClientDllPath.empty() ? javaServerDllPath : javaClientDllPath;
        }
        logMsg("Java DLL path: %s", javaDllPath.c_str());
        if (!canLoadJavaDll()) {
            logMsg("Fallbacking to running java in separate process, dll cannot be loaded (64bit dll?).");
            separateProcess = true;
        }
    }

    return separateProcess ? startOutProcJvm(mainClassName, args, options, retCode)
            : startInProcJvm(mainClassName, args, options);
}

bool JvmLauncher::findClientOption(list<string> &options) {
    for (list<string>::iterator it = options.begin(); it != options.end(); ++it) {
        if (*it == "-client") {
            return false;
        }
    }
    return true;
}

bool JvmLauncher::canLoadJavaDll() {
    // be prepared for stupid placement of msvcr71.dll in java installation
    // (in java 1.6/1.7 jvm.dll is dynamically linked to msvcr71.dll which si placed
    // in bin directory)
    PrepareDllPath prepare(javaBinPath.c_str());
    HMODULE hDll = LoadLibrary(javaDllPath.c_str());
    if (hDll) {
        FreeLibrary(hDll);
        return true;
    }
    logErr(true, false, "Cannot load %s.", javaDllPath.c_str());
    return false;
}

bool JvmLauncher::isVersionString(const char *str) {
    char *end = 0;
    strtod(str, &end);
    return *end == '\0';
}

bool JvmLauncher::startInProcJvm(const char *mainClassName, std::list<std::string> args, std::list<std::string> options) {

    class Jvm {
    public:

        Jvm(JvmLauncher *jvmLauncher)
            : hDll(0)
            , jvm(0)
            , env(0)
            , jvmOptions(0)
            , jvmLauncher(jvmLauncher)
        {
        }

        ~Jvm() {
            if (env && env->ExceptionOccurred()) {
                env->ExceptionDescribe();
            }

            if (jvm) {
                logMsg("Destroying JVM");
                jvm->DestroyJavaVM();
            }

            if (jvmOptions) {
                delete[] jvmOptions;
            }

            if (hDll) {
                FreeLibrary(hDll);
            }
        }

        bool init(list<string> options) {
            logMsg("JvmLauncher::Jvm::init()");
            logMsg("LoadLibrary(\"%s\")", jvmLauncher->javaDllPath.c_str());
            PrepareDllPath prepare(jvmLauncher->javaBinPath.c_str());
            hDll = LoadLibrary(jvmLauncher->javaDllPath.c_str());
            if (!hDll) {
                logErr(true, true, "Cannot load %s.", jvmLauncher->javaDllPath.c_str());
                return false;
            }

            CreateJavaVM createJavaVM = (CreateJavaVM) GetProcAddress(hDll, JNI_CREATEVM_FUNC);
            if (!createJavaVM) {
                logErr(true, true, "GetProcAddress for %s failed.", JNI_CREATEVM_FUNC);
                return false;
            }

            logMsg("JVM options:");
            jvmOptions = new JavaVMOption[options.size()];
            int i = 0;
            for (list<string>::iterator it = options.begin(); it != options.end(); ++it, ++i) {
                string &option = *it;
                logMsg("\t%s", option.c_str());
                jvmOptions[i].optionString = (char *) option.c_str();
                jvmOptions[i].extraInfo = 0;
            }

            JavaVMInitArgs jvmArgs;
            jvmArgs.options = jvmOptions;
            jvmArgs.nOptions = options.size();
            jvmArgs.version = JNI_VERSION_1_4;
            jvmArgs.ignoreUnrecognized = JNI_TRUE;

            logMsg("Creating JVM...");
            if (createJavaVM(&jvm, &env, &jvmArgs) < 0) {
                logErr(false, true, "JVM creation failed");
                return false;
            }
            logMsg("JVM created.");
            return true;
        }
        typedef jint (CALLBACK *CreateJavaVM)(JavaVM **jvm, JNIEnv **env, void *args);

        HMODULE hDll;
        JavaVM *jvm;
        JNIEnv *env;
        JavaVMOption *jvmOptions;
        JvmLauncher *jvmLauncher;
    };

    Jvm jvm(this);
    if (!jvm.init(options)) {
        return false;
    }

    jclass mainClass = jvm.env->FindClass(mainClassName);
    if (!mainClass) {
        logErr(false, true, "Cannot find class %s.", mainClassName);
        return false;
    }

    jmethodID mainMethod = jvm.env->GetStaticMethodID(mainClass, "main", "([Ljava/lang/String;)V");
    if (!mainMethod) {
        logErr(false, true, "Cannot get main method.");
        return false;
    }
    
    jclass jclassString = jvm.env->FindClass("java/lang/String");
    if (!jclassString) {
        logErr(false, true, "Cannot find java/lang/String class");
        return false;
    }

    jstring jstringArg = jvm.env->NewStringUTF("");
    if (!jstringArg) {
        logErr(false, true, "NewStringUTF() failed");
        return false;
    }

    jobjectArray mainArgs = jvm.env->NewObjectArray(args.size(), jclassString, jstringArg);
    if (!mainArgs) {
        logErr(false, true, "NewObjectArray() failed");
        return false;
    }
    int i = 0;
    for (list<string>::iterator it = args.begin(); it != args.end(); ++it, ++i) {
        string &arg = *it;
        jstring jstringArg = jvm.env->NewStringUTF(arg.c_str());
        if (!jstringArg) {
            logErr(false, true, "NewStringUTF() failed");
            return false;
        }
        jvm.env->SetObjectArrayElement(mainArgs, i, jstringArg);
    }

    jvm.env->CallStaticVoidMethod(mainClass, mainMethod, mainArgs);
    return true;
}


bool JvmLauncher::startOutProcJvm(const char *mainClassName, std::list<std::string> args, std::list<std::string> options, DWORD *retCode) {
    STARTUPINFO si = {0};
    si.cb = sizeof(STARTUPINFO);
    PROCESS_INFORMATION pi = {0};
    string cmdLine = '\"' + javaExePath + '\"';
    cmdLine.reserve(32*1024);
    for (list<string>::iterator it = options.begin(); it != options.end(); ++it) {
        cmdLine += " \"";
        cmdLine += *it;
        cmdLine += "\"";
    }
    
    // mainClass and args
    cmdLine += ' ';
    cmdLine += mainClassName;
    for (list<string>::iterator it = args.begin(); it != args.end(); ++it) {
        if (javaClientDllPath.empty() && *it == "-client") {
            logMsg("Removing -client option, client java dll not found.");
            // remove client parameter, no client java found
            continue;
        }
        cmdLine += " \"";
        cmdLine += *it;
        cmdLine += "\"";
    }

    logMsg("Command line:\n%s", cmdLine.c_str());
    if (cmdLine.size() >= 32*1024) {
        logErr(false, true, "Command line is too long. Length: %u. Maximum length: %u.", cmdLine.c_str(), 32*1024);
        return false;
    }

    char cmdLineStr[32*1024] = "";
    strcpy(cmdLineStr, cmdLine.c_str());
    if (!CreateProcess(NULL, cmdLineStr, NULL, NULL, FALSE, CREATE_SUSPENDED, NULL, NULL, &si, &pi)) {
        logErr(true, true, "Failed to create process");
        return false;
    }

    disableFolderVirtualization(pi.hProcess);
    ResumeThread(pi.hThread);
    WaitForSingleObject(pi.hProcess, INFINITE);
    if (retCode) {
        GetExitCodeProcess(pi.hProcess, retCode);
    }
    CloseHandle(pi.hProcess);
    CloseHandle(pi.hThread);
    return true;
}

bool JvmLauncher::findJava(const char *minJavaVersion) {
    if (findJava(JDK_KEY, JAVA_JRE_PREFIX, minJavaVersion)) {
        return true;
    }
    if (findJava(JRE_KEY, "", minJavaVersion)) {
        return true;
    }
    javaPath = "";
    javaExePath = "";
    javaClientDllPath = "";
    javaServerDllPath = "";
    javaBinPath = "";
    return false;  
}

bool JvmLauncher::findJava(const char *javaKey, const char *prefix, const char *minJavaVersion) {
    logMsg("JvmLauncher::findJava()\n\tjavaKey: %s\n\tprefix: %s\n\tminJavaVersion: %s", javaKey, prefix, minJavaVersion);
    string value;
    if (getStringFromRegistry(HKEY_LOCAL_MACHINE, javaKey, CUR_VERSION_NAME, value)) {
        if (value >= minJavaVersion) {
            string path;
            if (getStringFromRegistry(HKEY_LOCAL_MACHINE, (string(javaKey) + "\\" + value).c_str(), JAVA_HOME_NAME, path)) {
                if (*path.rbegin() == '\\') {
                    path.erase(path.length() - 1, 1);
                }
                return checkJava(path.c_str(), prefix);
            }
        }
    }
    return false;    
}
