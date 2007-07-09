/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

#include <windows.h>
#include <wchar.h>
#include <stdio.h>
#include <stdlib.h>
#include <shellapi.h>
#include "FileUtils.h"
#include "StringUtils.h"
#include "JavaUtils.h"
#include "RegistryUtils.h"
#include "Launcher.h"
#include "ProcessUtils.h"
#include "StringUtils.h"
#include "ExtractUtils.h"
#include "Main.h"

const DWORD NUMBER_OF_HELP_ARGUMENTS = 9;

const WCHAR * outputFileArg       = L"--output";
const WCHAR * javaArg             = L"--javahome";
const WCHAR * debugArg            = L"--verbose";
const WCHAR * tempdirArg          = L"--tempdir";
const WCHAR * classPathPrepend    = L"--classpath-prepend";
const WCHAR * classPathAppend     = L"--classpath-append";
const WCHAR * extractArg          = L"--extract";
const WCHAR * helpArg             = L"--help";
const WCHAR * helpOtherArg        = L"/?";
const WCHAR * silentArg           = L"--silent";
const WCHAR * nospaceCheckArg     = L"--nospacecheck";

const WCHAR * javaParameterPrefix = L"-J";

const WCHAR * NEW_LINE            = L"\n";

const WCHAR * CLASSPATH_SEPARATOR = L";";
const WCHAR * CLASS_SUFFIX = L".class";


DWORD isLauncherArgument(LauncherProperties * props, WCHAR * value) {
    DWORD i=0;
    for(i=0;i<props->launcherCommandArguments->size;i++) {
        if(lstrcmpW(props->launcherCommandArguments->items[i], value)==0) {
            return 1;
        }
    }
    return 0;
}

DWORD getArgumentIndex(LauncherProperties * props, const WCHAR *arg, DWORD removeArgument) {
    WCHARList *cmd = props->commandLine;
    DWORD i=0;
    for(i=0;i<cmd->size;i++) {
        if(cmd->items[i]!=NULL) { // argument has not been cleaned yet
            if(lstrcmpW(arg, cmd->items[i])==0) { //argument is the same as the desired
                if(removeArgument) FREE(cmd->items[i]); // free it .. we don`t need it anymore
                return i;
            }
        }
    }
    return cmd->size;
}

DWORD argumentExists(LauncherProperties * props, const WCHAR *arg, DWORD removeArgument) {
    DWORD index = getArgumentIndex(props, arg, removeArgument);
    return (index < props->commandLine->size);
    
}
WCHAR * getArgumentValue(LauncherProperties * props, const WCHAR *arg, DWORD removeArgument, DWORD mandatory) {
    WCHARList *cmd = props->commandLine;
    WCHAR * result = NULL;
    DWORD i = getArgumentIndex(props, arg, removeArgument);
    if((i+1) < cmd->size) {
        //we have at least one more argument
        if(mandatory || !isLauncherArgument(props, cmd->items[i+1])) {
            result = appendStringW(NULL, cmd->items[i+1]);
            if(removeArgument) FREE(cmd->items[i+1]);
        }
    }
    return result;
}


void setOutputFile(LauncherProperties * props, WCHAR *path) {
    HANDLE out = INVALID_HANDLE_VALUE ;
    
    out = CreateFileW(path, GENERIC_WRITE | GENERIC_READ, FILE_SHARE_READ | FILE_SHARE_WRITE, 0, CREATE_ALWAYS, 0, 0);
    if(out!=INVALID_HANDLE_VALUE) {
        SetStdHandle(STD_OUTPUT_HANDLE, out);
        SetStdHandle(STD_ERROR_HANDLE, out);
        props->stdoutHandle = out;
        props->stderrHandle = out;
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "[CMD Argument] Redirect output to file : ", 0);
        writeMessageW(props, OUTPUT_LEVEL_DEBUG, 0, path, 1);
    } else  {
        WCHAR * err = NULL;
        DWORD code = GetLastError();
        props->status = ERROR_INPUTOUPUT;
        writeErrorA(props, OUTPUT_LEVEL_DEBUG, 1, "[CMD Argument] Can`t create file: ", path, code);
        err = getErrorDescription(code);
        showMessageW(props, L"Can`t redirect output to file!\n\nRequested file : %s\n%s", 2, path, err);
        FREE(err);
    }
}

void setOutput(LauncherProperties * props) {
    WCHAR * file = props->userDefinedOutput;
    if(file!=NULL) {
        DWORD exists = fileExists(file);
        if((exists && !isDirectory(file) )|| !exists) {
            setOutputFile(props, file);
        }
    }
    
    writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0,
            (props->outputLevel == OUTPUT_LEVEL_DEBUG) ?
                "[CMD Argument] Using debug output." :
                "Using normal output." , 1);
                
}


void loadLocalizationStrings(LauncherProperties *props) {
    
    // load localized messages
    writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "Loading I18N Strings.", 1);
    loadI18NStrings(props);
    
    if(!isOK(props)) {
        writeMessageA(props, OUTPUT_LEVEL_NORMAL, 1, "Error! Can`t load i18n strings!!", 1);
        showErrorW(props, INTEGRITY_ERROR_PROP, 1, props->exeName);
    }
}

void createTMPDir(LauncherProperties * props) {
    WCHAR * argTempDir = NULL;
    DWORD createRndSubDir = 1;
    
    if((argTempDir = props->userDefinedExtractDir) !=NULL) {
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 1, "[CMD Argument] Extract data to directory: ", 0);
        writeMessageW(props, OUTPUT_LEVEL_DEBUG, 1, argTempDir, 1);
        createRndSubDir = 0;
    } else if((argTempDir = props->userDefinedTempDir) !=NULL) {
        writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "[CMD Argument] Using tmp directory: ", 0);
        writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, argTempDir, 1);
    }
    
    createTempDirectory(props, argTempDir, createRndSubDir);
    if(!isOK(props)) {
        showErrorW(props, CANT_CREATE_TEMP_DIR_PROP, 1, props->tmpDir);
    }
}

void checkExtractionStatus(LauncherProperties *props) {
    if(props->status == ERROR_FREESPACE) {
        double d = int64ttoDouble(props->bundledSize) / (1024.0 * 1024.0) + 1.0;
        DWORD dw = (DWORD) d;
        WCHAR * size = DWORDtoWCHAR(dw);
        
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 1, "Error! Not enought free space !", 1);
        showErrorW(props, NOT_ENOUGH_FREE_SPACE_PROP, 2, size, tempdirArg);
        FREE(size);
    }
    else if(props->status == ERROR_INTEGRITY) {
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 1, "Error! Can`t extract data from bundle. Seems to be integrirty error!", 1);
        showErrorW(props, INTEGRITY_ERROR_PROP, 1, props->exeName);
    }
}


void trySetCompatibleJava(WCHAR * location, LauncherProperties * props) {
    if(isTerminated(props)) return;
    if(location!=NULL) {
        JavaProperties * javaProps = NULL;
        
        if(inList(props->alreadyCheckedJava, location)) {
            writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "... already checked location ", 0);
            writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, location, 1);
            // return here and don`t proceed with private jre checking since it`s already checked as well
            return;
        } else {
            props->alreadyCheckedJava = addStringToList(props->alreadyCheckedJava, location);
        }
        
        getJavaProperties(location, props, &javaProps);
        
        if(isOK(props)) {
            writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "... some java at ", 0);
            writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, location, 1);
            // some java there, check compatibility
            writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "... checking compatibility of java : ", 0);
            writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, javaProps->javaHome, 1);
            if(isJavaCompatible(javaProps, props->compatibleJava, props->compatibleJavaNumber)) {
                writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "... compatible", 1);
                props->java = javaProps;
            } else {
                props->status = ERROR_JVM_UNCOMPATIBLE;
                writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "... uncompatible", 1);
                freeJavaProperties(&javaProps);
            }
        } else {
            writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "... no java at ", 0);
            writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, location, 1);
            if (props->status==ERROR_INPUTOUPUT) {
                props->status = ERROR_JVM_NOT_FOUND;
            }
        }
        
        if(props->status == ERROR_JVM_NOT_FOUND) { // check private JRE
            //DWORD privateJreStatus = props->status;
            WCHAR * privateJre = appendStringW(NULL, location);
            privateJre = appendStringW(privateJre, L"\\jre");
            writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "... check private jre at ", 0);
            writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, privateJre, 1);
            
            if(inList(props->alreadyCheckedJava, privateJre)) {
                writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "... already checked location ", 0);
                writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, privateJre, 1);
            } else {
                props->alreadyCheckedJava = addStringToList(props->alreadyCheckedJava, privateJre);
                
                getJavaProperties(privateJre, props, &javaProps);
                if(isOK(props)) {
                    writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "... checking compatibility of private jre : ", 0);
                    writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, javaProps->javaHome, 1);
                    if(isJavaCompatible(javaProps, props->compatibleJava, props->compatibleJavaNumber)) {
                        props->java = javaProps;
                        props->status = ERROR_OK;
                    } else {
                        freeJavaProperties(&javaProps);
                        props->status = ERROR_JVM_UNCOMPATIBLE;
                    }
                } else if (props->status==ERROR_INPUTOUPUT) {
                    props->status = ERROR_JVM_NOT_FOUND;
                }
            }
            FREE(privateJre);
        }
    } else {
        props->status = ERROR_JVM_NOT_FOUND;
    }
}

void resolveTestJVM(LauncherProperties * props) {
    WCHAR * testJVMFile = NULL;
    WCHAR * testJVMClassPath = NULL;
    
    writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "Resolving testJVM classpath...", 1);
    writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... first step : ", 0);
    writeMessageW(props, OUTPUT_LEVEL_DEBUG, 0, props->testJVMFile->path, 1);
    resolvePath(props, props->testJVMFile);
    testJVMFile = props->testJVMFile->resolved;
    
    writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... second     : ", 0);
    writeMessageW(props, OUTPUT_LEVEL_DEBUG, 0, props->testJVMFile->resolved, 1);
    
    
    if(isDirectory(testJVMFile)) { // the directory of the class file is set
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... testJVM is : directory ", 1);
        testJVMClassPath = appendStringW(NULL, testJVMFile);
    } else { // testJVMFile is either .class file or .jar/.zip file with the neccessary class file
        WCHAR * dir = getParentDirectory(testJVMFile);
        WCHAR * ptr = testJVMFile;
        do {
            ptr = wcsstr(ptr, CLASS_SUFFIX); // check if ptr contains .class
            if(ptr==NULL) { // .jar or .zip file
                writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... testJVM is : ZIP/JAR file", 1);
                testJVMClassPath = appendStringW(NULL, testJVMFile);
                break;
            }
            ptr += getLengthW(CLASS_SUFFIX); // shift to the right after the ".class"
            
            if(ptr==NULL || getLengthW(ptr)==0) { // .class was at the end of the ptr
                writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... testJVM is : .class file ", 1);
                testJVMClassPath = appendStringW(NULL, dir);
                break;
            }
        } while(1);
        FREE(dir);
    }
    
    FREE(props->testJVMFile->resolved);
    props->testJVMFile->resolved = testJVMClassPath;
    writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... resolved   : ", 0);
    writeMessageW(props, OUTPUT_LEVEL_DEBUG, 0, props->testJVMFile->resolved, 1);
}

void findSuitableJava(LauncherProperties * props) {
    if(!isOK(props)) return;
    
    //resolve testJVM file
    resolveTestJVM(props);
    
    if(!fileExists(props->testJVMFile->resolved)) {
        writeMessageA(props, OUTPUT_LEVEL_NORMAL, 1, "Can`t find TestJVM classpath : ", 0);
        writeMessageW(props, OUTPUT_LEVEL_NORMAL, 1, props->testJVMFile->resolved, 1);
        showErrorW(props, JVM_NOT_FOUND_PROP, 1, javaArg);
        props->status = ERROR_JVM_NOT_FOUND;
        return;
    } else if(!isTerminated(props)) {
        
        // try to get java location from command line arguments
        writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "", 1);
        writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "Finding JAVA...", 1);
        
        //WCHAR * java = NULL;
        
        if(props->userDefinedJavaHome!=NULL) { // using user-defined JVM via command-line parameter
            writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "[CMD Argument] Try to use java from ", 0);
            writeMessageW(props, OUTPUT_LEVEL_NORMAL, 0, props->userDefinedJavaHome, 1);
            
            trySetCompatibleJava(props->userDefinedJavaHome, props);
            if( props->status == ERROR_JVM_NOT_FOUND || props->status == ERROR_JVM_UNCOMPATIBLE) {
                const char * prop = (props->status == ERROR_JVM_NOT_FOUND) ?
                    JVM_USER_DEFINED_ERROR_PROP :
                    JVM_UNSUPPORTED_VERSION_PROP;
                    showErrorW(props, prop, 1, props->userDefinedJavaHome);
            }
        } else { // no user-specified java argument
            findSystemJava(props);
            if( props->java ==NULL) {
                showErrorW(props, JVM_NOT_FOUND_PROP, 1, javaArg);
                props->status = ERROR_JVM_NOT_FOUND;
            }
        }
        
        if(props->java!=NULL) {
            writeMessageA(props, OUTPUT_LEVEL_NORMAL, 1, "Compatible jvm is found on the system", 1);
            printJavaProperties(props, props->java);
        } else {
            writeMessageA(props, OUTPUT_LEVEL_NORMAL, 1, "No compatible jvm was found on the system", 1);
        }
    }
    return;
}

void resolvePath(LauncherProperties * props, LauncherResource * file) {
    WCHAR * result = NULL;
    DWORD i=0;
    
    if(file==NULL) return;
    if(file->resolved!=NULL) return;
    
    switch (file->type) {
        case 2:
            if(props->java!=NULL) {
                result = appendStringW(NULL, props->java->javaHome); // relative to javahome
            }
            break;
        case 3:
            // relative to user home
            result = getCurrentUserHome();
            break;
        case 4:
            result = appendStringW(NULL, props->exeDir); // launcher parent
            break;
        case 5:
            result = appendStringW(NULL, props->tmpDir); // launcher tmpdir
            break;
        case 0: // absolute path, nothing to add
        case 1: // bundled file with full path path, nothing to add
        default:
            break; // the same as absolute, nothing to add
    }
    if(result!=NULL) {
        result = appendStringW(result, L"\\");
    }
    file->resolved = appendStringW(result, file->path);
    
    for(i=0;i<getLengthW(file->resolved);i++) {
        if(file->resolved[i]==L'/') {
            file->resolved[i]=L'\\';
        }
    }
}

void setClasspathElements(LauncherProperties * props) {
    if(isOK(props)) {
        WCHAR * preCP = NULL;
        WCHAR * appCP = NULL;
        WCHAR *tmp = NULL;
        DWORD i = 0 ;
        
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "Modifying classpath ...", 1);
        // add some libraries to the beginning of the classpath
        while((preCP = getArgumentValue(props, classPathPrepend, 1, 1))!=NULL) {
            writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... adding entry to the beginning of classpath : ", 0);
            writeMessageW(props, OUTPUT_LEVEL_DEBUG, 0, preCP, 1);
            if (props->classpath != NULL) {
                preCP = appendStringW(preCP, CLASSPATH_SEPARATOR);
            }
            //WCHAR *last = props->classpath;
            tmp = appendStringW(preCP, props->classpath);
            FREE(props->classpath);
            props->classpath = tmp;
        }
        
        
        for(i=0;i<props->jars->size;i++) {
            WCHAR * resolvedCpEntry = NULL;
            resolvePath(props, props->jars->items[i]);
            resolvedCpEntry = props->jars->items[i]->resolved;
            if(!fileExists(resolvedCpEntry)) {
                props->status = EXTERNAL_RESOURCE_MISSING;
                showErrorW(props, EXTERNAL_RESOURE_LACK_PROP, 1, resolvedCpEntry);
                return;
            }
            if (props->classpath != NULL) {
                props->classpath = appendStringW(props->classpath, CLASSPATH_SEPARATOR);
            }
            props->classpath = appendStringW(props->classpath, resolvedCpEntry);
        }
        
        // add some libraries to the end of the classpath
        while((appCP = getArgumentValue(props, classPathAppend, 1, 1))!=NULL) {
            writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... adding entry to the end of classpath : ", 0);
            writeMessageW(props, OUTPUT_LEVEL_DEBUG, 0, appCP, 1);
            if (props->classpath != NULL) {
                props->classpath = appendStringW(props->classpath, CLASSPATH_SEPARATOR);
            }
            props->classpath = appendStringW(props->classpath, appCP);
            FREE(appCP);
        }
        
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... finished", 1);
    }
}

void setAdditionalArguments(LauncherProperties * props) {
    if(isOK(props)) {
        WCHARList * cmd = props->commandLine;
        WCHAR ** javaArgs;
        WCHAR ** appArgs;
        DWORD i=0;
        DWORD jArg = 0; // java arguments number
        DWORD aArg = 0; // app arguments number
        
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0,
                "Parsing rest of command line arguments to add them to java or application parameters... ", 1);
        
        // get number for array creation
        for(i=0;i<cmd->size;i++) {
            if(cmd->items[i]!=NULL) {
                if(wcsstr(cmd->items[i], javaParameterPrefix)!=NULL) {
                    jArg++;
                } else {
                    aArg++;
                }
            }
        }
        //fill the array
        if(jArg>0) {
            javaArgs = newppWCHAR(jArg + props->jvmArguments->size);
            //DWORD j=0;
            for (i=0;i<props->jvmArguments->size;i++) {
                javaArgs[i] = props->jvmArguments->items[i];
            }
            FREE(props->jvmArguments->items);
        } else {
            javaArgs = NULL;
        }
        
        if(aArg>0) {
            appArgs = newppWCHAR(aArg + props->appArguments->size);
            for (i=0; i < props->appArguments->size; i++) {
                appArgs [i]= props->appArguments->items[i];
            }
            FREE(props->appArguments->items);
        } else {
            appArgs = NULL;
        }
        jArg = aArg = 0;
        
        for(i=0;i<cmd->size;i++) {
            if(cmd->items[i]!=NULL) {
                if(wcsstr(cmd->items[i], javaParameterPrefix)!=NULL) {
                    javaArgs [ props->jvmArguments->size + jArg] = appendStringW(NULL, cmd->items[i] + getLengthW(javaParameterPrefix));
                    writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... adding JVM argument : ", 0);
                    writeMessageW(props, OUTPUT_LEVEL_DEBUG, 0, javaArgs [ props->jvmArguments->size + jArg], 1);
                    jArg ++ ;
                } else {
                    appArgs  [ props->appArguments->size + aArg] = appendStringW(NULL, cmd->items[i]);
                    writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... adding APP argument : ", 0);
                    writeMessageW(props, OUTPUT_LEVEL_DEBUG, 0, appArgs  [ props->appArguments->size + aArg], 1);
                    aArg++;
                }
                FREE(cmd->items[i]);
            }
        }
        props->appArguments->size  = props->appArguments->size + aArg;
        props->jvmArguments->size  = props->jvmArguments->size + jArg;
        if(props->jvmArguments->items==NULL) props->jvmArguments->items = javaArgs;
        if(props->appArguments->items==NULL) props->appArguments->items = appArgs;
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... finished parsing parameters", 1);
    }
}
void appendCommandLineArgument( WCHAR ** command, const WCHAR * arg) {
    if(wcsstr(arg, L" ")) {
        *command = appendStringW(*command, L"\"");
        *command = appendStringW(*command, arg);
        *command = appendStringW(*command, L"\"");
    } else {
        *command = appendStringW(*command, arg);
    }
    *command = appendStringW(*command, L" ");
}

void setLauncherCommand(LauncherProperties *props) {
    if(!isOK(props)) return;
    
    if(props->java==NULL) {
        props->status = ERROR_JVM_NOT_FOUND;
        return;
    } else {
        WCHAR * command = NULL;
        WCHAR * javaIOTmpdir = NULL;
        DWORD i = 0;
        
        appendCommandLineArgument(&command, props->java->javaExe);
        command = appendStringW(command, L"-Djava.io.tmpdir=");
        javaIOTmpdir = getParentDirectory(props->tmpDir);
        appendCommandLineArgument(&command, javaIOTmpdir);
        FREE(javaIOTmpdir);
        
        
        for(i=0;i<props->jvmArguments->size;i++) {
            appendCommandLineArgument(&command, props->jvmArguments->items[i]);
        }
        
        appendCommandLineArgument(&command, L"-classpath");
        appendCommandLineArgument(&command, props->classpath);
        appendCommandLineArgument(&command, props->mainClass);
        
        for(i=0;i<props->appArguments->size; i++) {
            appendCommandLineArgument(&command, props->appArguments->items[i]);
        }
        props->command = command;
    }
}

void executeMainClass(LauncherProperties * props) {
    if(isOK(props) && !isTerminated(props)) {        
        int64t * minSize = newint64_t(0, 0);        
        writeMessageA(props, OUTPUT_LEVEL_NORMAL, 0, "Executing main class", 1);
        checkFreeSpace(props, props->tmpDir, minSize);
        if(isOK(props)) {
            HANDLE hErrorRead;
            HANDLE hErrorWrite;
            char * error = NULL;
            
            CreatePipe(&hErrorRead, &hErrorWrite, NULL, 0);            
            hideLauncherWindows(props);
            executeCommand(props, props->command, NULL, INFINITE, props->stdoutHandle, hErrorWrite, NORMAL_PRIORITY_CLASS);
            if(!isOK(props)) {
                writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... an error occured during JVM running main class", 1);
                props->exitCode = props->status;
            } else {
                char * s = DWORDtoCHAR(props->exitCode);
                writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... main class has finished his work. Exit code is ", 0);                
                writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, s, 1);
                FREE(s);
            }
            
            error = readHandle(hErrorRead);
            if(getLengthA(error)>1) {
                DWORD showMessage = 0;
                char * ptr = error;
                while(ptr!=NULL) {
                    if(strstr(ptr, "Picked up ") == NULL && getLengthA(ptr) > 1) {
                        showMessage = 1;
                        break;
                    }
                    ptr = strstr(ptr, "\n");
                    if(ptr!=NULL) ptr++;
                }
                
                if(showMessage) {
                    WCHAR * errorW = toWCHAR(error);
                    showMessageW(props, getI18nProperty(props, JAVA_PROCESS_ERROR_PROP), 1, errorW);
                    FREE(errorW);
                }
            }
            CloseHandle(hErrorWrite);
            CloseHandle(hErrorRead);
            FREE(error);
            Sleep(1);
        } else {
            props->status = ERROR_FREESPACE;
            props->exitCode = props->status;
            writeMessageA(props, OUTPUT_LEVEL_DEBUG, 1, "... there is not enough space in tmp dir to execute main jar", 1);
        }
        FREE(minSize);
    }
}

DWORD isOnlyHelp(LauncherProperties * props) {
    if(argumentExists(props, helpArg, 1) || argumentExists(props, helpOtherArg, 1)) {
        
        WCHARList * help = newWCHARList(NUMBER_OF_HELP_ARGUMENTS);        
        DWORD counter = 0;
        WCHAR * helpString = NULL;
        
        help->items[counter++] = formatMessageW(getI18nProperty(props, ARG_JAVA_PROP), 1, javaArg);
        help->items[counter++] = formatMessageW(getI18nProperty(props, ARG_TMP_PROP), 1, tempdirArg);
        help->items[counter++] = formatMessageW(getI18nProperty(props, ARG_EXTRACT_PROP), 1, extractArg);
        help->items[counter++] = formatMessageW(getI18nProperty(props, ARG_OUTPUT_PROPERTY), 1, outputFileArg);
        help->items[counter++] = formatMessageW(getI18nProperty(props, ARG_DEBUG_PROP), 1, debugArg);
        help->items[counter++] = formatMessageW(getI18nProperty(props, ARG_CPA_PROP), 1, classPathAppend);
        help->items[counter++] = formatMessageW(getI18nProperty(props, ARG_CPP_PROP), 1, classPathPrepend);
        help->items[counter++] = formatMessageW(getI18nProperty(props, ARG_DISABLE_SPACE_CHECK), 1, nospaceCheckArg);
        help->items[counter++] = formatMessageW(getI18nProperty(props, ARG_HELP_PROP), 1, helpArg);
        
        
        for(counter=0;counter<NUMBER_OF_HELP_ARGUMENTS;counter++) {
            helpString = appendStringW(appendStringW(helpString, help->items[counter]), NEW_LINE);
        }
        freeWCHARList(&help);
        showMessageW(props, helpString, 0);
        FREE(helpString);
        return 1;
    }
    return 0;
}

DWORD isSilent(LauncherProperties * props) {
    return props->silentMode;
}

WCHARList * getCommandlineArguments() {
    int argumentsNumber = 0;
    int i=0;
    WCHAR ** commandLine = CommandLineToArgvW(GetCommandLineW(), &argumentsNumber);
    // the first is always the running program..  we don`t need it
    // it is that same as GetModuleFileNameW says
    WCHARList * commandsList = newWCHARList((DWORD) (argumentsNumber - 1) );
    for(i=0;i<argumentsNumber - 1;i++) {
        commandsList->items[i] = appendStringW(NULL, commandLine[i + 1]);
    }
    
    LocalFree(commandLine);
    return commandsList;
}


LauncherProperties * createLauncherProperties() {
    LauncherProperties *props = (LauncherProperties*)LocalAlloc(LPTR,sizeof(LauncherProperties));
    DWORD c = 0;
    props->launcherCommandArguments = newWCHARList(11);
    props->launcherCommandArguments->items[c++] = appendStringW(NULL, outputFileArg);
    props->launcherCommandArguments->items[c++] = appendStringW(NULL, javaArg);
    props->launcherCommandArguments->items[c++] = appendStringW(NULL, debugArg);
    props->launcherCommandArguments->items[c++] = appendStringW(NULL, tempdirArg);
    props->launcherCommandArguments->items[c++] = appendStringW(NULL, classPathPrepend);
    props->launcherCommandArguments->items[c++] = appendStringW(NULL, classPathAppend);
    props->launcherCommandArguments->items[c++] = appendStringW(NULL, extractArg);
    props->launcherCommandArguments->items[c++] = appendStringW(NULL, helpArg);
    props->launcherCommandArguments->items[c++] = appendStringW(NULL, helpOtherArg);
    props->launcherCommandArguments->items[c++] = appendStringW(NULL, silentArg);
    props->launcherCommandArguments->items[c++] = appendStringW(NULL, nospaceCheckArg);
    
    props->jvmArguments = NULL;
    props->appArguments = NULL;
    props->extractOnly  = 0;
    props->mainClass    = NULL;
    props->testJVMClass = NULL;
    props->classpath    = NULL;
    props->jars         = NULL;
    props->testJVMFile  = NULL;
    props->tmpDir       = NULL;
    props->tmpDirCreated = 0;
    props->compatibleJava=NULL;
    props->compatibleJavaNumber=0;
    props->java    = NULL;
    props->command = NULL;
    props->jvms    = NULL;
    props->alreadyCheckedJava = NULL;
    props->exePath = getExePath();
    props->exeName = getExeName();
    props->exeDir  = getExeDirectory();
    props->handler = CreateFileW(props->exePath, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
    props->bundledSize = newint64_t(0, 0);
    props->bundledNumber = 0;
    props->commandLine   = getCommandlineArguments();
    props->status       = ERROR_OK;
    props->exitCode     = 0;
    props->outputLevel  = argumentExists(props, debugArg, 1) ? OUTPUT_LEVEL_DEBUG : OUTPUT_LEVEL_NORMAL;
    props->stdoutHandle = GetStdHandle(STD_OUTPUT_HANDLE);
    props->stderrHandle = GetStdHandle(STD_ERROR_HANDLE);
    props->bufsize = 65536;
    props->restOfBytes = createSizedString();
    props->I18N_PROPERTIES_NUMBER = 0;
    props->i18nMessages = NULL;
    props->userDefinedJavaHome    = getArgumentValue(props, javaArg, 1, 1);
    props->userDefinedTempDir     = getArgumentValue(props, tempdirArg, 1, 1);
    
    props->userDefinedExtractDir  = NULL;
    props->extractOnly = 0;
    
    if(argumentExists(props, extractArg, 0)) {
        props->userDefinedExtractDir = getArgumentValue(props, extractArg, 1, 0);
        if(props->userDefinedExtractDir==NULL) {// next argument is null or another launcher argument
            props->userDefinedExtractDir = getCurrentDirectory();
        }
        props->extractOnly = 1;
    }
    props->userDefinedOutput      = getArgumentValue(props, outputFileArg, 1, 1);
    props->checkForFreeSpace      = !argumentExists(props, nospaceCheckArg, 1);
    props->silentMode             = argumentExists(props, silentArg, 0);
    props->launcherSize = getFileSize(props->exePath);
    props->isOnlyStub = (compare(props->launcherSize, STUB_FILL_SIZE) < 0);
    return props;
}
void freeLauncherResourceList(LauncherResourceList ** list) {
    if(*list!=NULL) {
        if((*list)->items!=NULL) {
            DWORD i=0;
            for(i=0;i<(*list)->size;i++) {
                freeLauncherResource(&((*list)->items[i]));
            }
            FREE((*list)->items);
        }
        FREE((*list));
    }
}


void freeLauncherProperties(LauncherProperties **props) {
    if((*props)!=NULL) {        
        DWORD i=0;
        writeMessageA(*props, OUTPUT_LEVEL_DEBUG, 0, "Closing launcher properties", 1);
        freeWCHARList(& ( (*props)->appArguments));
        freeWCHARList(& ( (*props)->jvmArguments));
        
        FREE((*props)->mainClass);
        FREE((*props)->testJVMClass);
        FREE((*props)->classpath);
        freeLauncherResourceList(&((*props)->jars));
        
        freeLauncherResourceList(&((*props)->jvms));
        
        freeLauncherResource(&((*props)->testJVMFile));
        
        FREE((*props)->tmpDir);
        for(i=0;i<(*props)->compatibleJavaNumber;i++) {
            JavaCompatible * jc = (*props)->compatibleJava[i];
            if(jc!=NULL) {
                FREE(jc->minVersion);
                FREE(jc->maxVersion);
                FREE(jc->vendor);
                FREE(jc->osName);
                FREE(jc->osArch);
                FREE((*props)->compatibleJava[i]);
            }
        }
        freeStringList(&((*props)->alreadyCheckedJava));
        FREE((*props)->compatibleJava);
        freeJavaProperties(&((*props)->java));
        FREE((*props)->userDefinedJavaHome);
        FREE((*props)->userDefinedTempDir);
        FREE((*props)->userDefinedExtractDir);
        FREE((*props)->userDefinedOutput);
        FREE((*props)->command);
        FREE((*props)->exePath);
        FREE((*props)->exeDir);
        FREE((*props)->exeName);
        FREE((*props)->bundledSize);
        FREE((*props)->launcherSize);
        freeSizedString(&((*props)->restOfBytes));
        
        flushHandle((*props)->stdoutHandle);
        flushHandle((*props)->stderrHandle);
        CloseHandle((*props)->stdoutHandle);
        CloseHandle((*props)->stderrHandle);
        
        freeI18NMessages((*props));
        freeWCHARList(& ((*props)->launcherCommandArguments));
        freeWCHARList(& ((*props)->commandLine));
        CloseHandle((*props)->handler);
        
        FREE((*props));
    }
    return;
}
void printStatus(LauncherProperties * props) {
    char * s = DWORDtoCHAR(props->status);
    writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... EXIT status : ", 0);
    writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, s, 1);
    FREE(s);
    s = DWORDtoCHAR(props->exitCode);
    writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... EXIT code : ", 0);
    writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, s, 1);
    FREE(s);
}

void processLauncher(LauncherProperties * props) {
    setOutput(props);
    if(!isOK(props) || isTerminated(props)) return;
    
    setProgressRange(props, props->launcherSize);
    if(!isOK(props) || isTerminated(props)) return;
    
    skipStub(props);
    if(!isOK(props) || isTerminated(props)) return;
    
    loadLocalizationStrings(props);
    if(!isOK(props) || isTerminated(props)) return;
    
    if(isOnlyHelp(props)) return;
    
    setProgressTitleString(props, getI18nProperty(props, MSG_PROGRESS_TITLE));
    setMainWindowTitle(props, getI18nProperty(props, MAIN_WINDOW_TITLE));
    showLauncherWindows(props);
    if(!isOK(props) || isTerminated(props)) return;
    
    readLauncherProperties(props);
    checkExtractionStatus(props);
    if(!isOK(props) || isTerminated(props)) return;
    
    if(props->bundledNumber > 0) {
        createTMPDir(props);
        if(isOK(props)) {
            checkFreeSpace(props, props->tmpDir, props->bundledSize);
            checkExtractionStatus(props);
        }
    }
    
    if (isOK(props) ){
        extractJVMData(props);
        checkExtractionStatus(props);
        if (isOK(props) && !props->extractOnly && !isTerminated(props)) {
            findSuitableJava(props);
        }
        
        if (isOK(props) && !isTerminated(props)) {
            extractData(props);
            checkExtractionStatus(props);
            if (isOK(props) && (props->java!=NULL)  && !isTerminated(props)) {
                setClasspathElements(props);
                if(isOK(props) && (props->java!=NULL)  && !isTerminated(props)) {
                    setAdditionalArguments(props);
                    setLauncherCommand(props);
                    Sleep(500);
                    executeMainClass(props);
                }
            }
        }
    }
    
    if(!props->extractOnly && props->tmpDirCreated) {
        writeMessageA(props, OUTPUT_LEVEL_DEBUG, 0, "... deleting temporary directory ", 1);
        deleteDirectory(props, props->tmpDir);
    }
    
}
