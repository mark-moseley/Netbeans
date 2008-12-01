/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.netbeans.modules.cnd.discovery.api.DiscoveryUtils;

/**
 *
 * @author Alexander Simon
 */
public class DwarfSourceTest extends TestCase {

    /**
     * Assert whether the document available through {@link #getDocument()}
     * has a content equal to <code>expectedText</code>.
     */
    protected void assertDocumentText(String line, String expResult, String result) {
        if (expResult.equals(result)) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Parsing line:");
        sb.append(line);
        sb.append("\nExpected:\n");
        sb.append(expResult);
        sb.append("\nFound:\n");
        sb.append(result);
        int startLine = 1;
        for (int i = 0; i < result.length() && i < expResult.length(); i++) {
            if (expResult.charAt(i) == '\n') {
                startLine++;
            }
            if (expResult.charAt(i) != result.charAt(i)) {
                sb.append("Diff starts in line " + startLine + "\n");
                String context = expResult.substring(i);
                if (context.length() > 40) {
                    context = context.substring(0, 40);
                }
                sb.append("Expected " + context + "\n");
                context = result.substring(i);
                if (context.length() > 40) {
                    context = context.substring(0, 40);
                }
                sb.append("Found " + context + "\n");
                break;
            }
        }
        assertFalse(sb.toString(), true);
    }

    /**
     * Test of scanCommandLine method, of class DwarfSource.
     */
    public void testScanCommandLine() {
        String line = "/set/c++/bin/5.9/intel-S2/prod/bin/CC -c -g -DHELLO=75 -Idist  main.cc -Qoption ccfe -prefix -Qoption ccfe .XAKABILBpivFlIc.";
        String expResult = "Source:main.cc\nMacros:\nHELLO=75\nPaths:\ndist";
        String result = processLine(line, true);
        assertDocumentText(line, expResult, result);
    }

    /**
     * Test of scanCommandLine method, of class DwarfSource.
     */
    public void testScanCommandLine2() {
        String line = "/opt/SUNWspro/bin/cc -xarch=amd64 -Ui386 -U__i386 -xO3 ../../intel/amd64/ml/amd64.il " +
                "../../i86pc/ml/amd64.il -D_ASM_INLINES -Xa -xspace -Wu,-xmodel=kernel -Wu,-save_args -v " +
                "-xildoff -g -xc99=%all -W0,-noglobal -g3 -gdwarf-2 -g3 -gdwarf-2 -errtags=yes -errwarn=%all " +
                "-W0,-xglobalstatic -xstrconst -DDIS_MEM -D_KERNEL -D_SYSCALL32 -D_SYSCALL32_IMPL -D_ELF64 " +
                "-I../../i86pc -I/export/opensolaris/testws77/usr/src/common -I../../intel -Y I,../../common " +
                "-c -o debug64/cpupm.o ../../i86pc/os/cpupm.c";
        String expResult = "Source:../../i86pc/os/cpupm.c\n" +
                "Macros:\n" +
                "DIS_MEM\n" +
                "_ASM_INLINES\n" +
                "_ELF64\n" +
                "_KERNEL\n" +
                "_SYSCALL32\n" +
                "_SYSCALL32_IMPL\n" +
                "Paths:\n" +
                "../../i86pc\n" +
                "/export/opensolaris/testws77/usr/src/common\n" +
                "../../intel" +
                "\n../../common";
        String result = processLine(line, true);
        assertDocumentText(line, expResult, result);
    }

    /**
     * Test of scanCommandLine method, of class DwarfSource.
     */
    public void testScanCommandLine3() {
        String line =
                "+ /opt/SUNWspro/bin/cc -xO3 -xarch=amd64 -Ui386 -U__i386 -K pic -Xa -xildoff -errtags=yes " +
                "-errwarn=%all -erroff=E_EMPTY_TRANSLATION_UNIT -erroff=E_STATEMENT_NOT_REACHED " +
                "-erroff=E_UNRECOGNIZED_PRAGMA_IGNORED -xc99=%all -D_XOPEN_SOURCE=600 " +
                "-D__EXTENSIONS__=1 -W0,-xglobalstatic -v -xstrconst -g -xc99=%all " +
                "-D_XOPEN_SOURCE=600 -D__EXTENSIONS__=1 -W0,-noglobal -xdebugformat=stabs " +
                "-DTEXT_DOMAIN=\"SUNW_OST_OSLIB\" -D_TS_ERRNO -Isrc/cmd/ksh93 " +
                "-I../common/include -I/export/home/thp/opensolaris/proto/root_i386/usr/include/ast " +
                "-DKSHELL -DSHOPT_BRACEPAT -DSHOPT_CMDLIB_BLTIN=0 -DSH_CMDLIB_DIR=\"/usr/ast/bin\" " +
                "-DSHOPT_CMDLIB_HDR=\"solaris_cmdlist.h\" -DSHOPT_DYNAMIC -DSHOPT_ESH " +
                "-DSHOPT_FILESCAN -DSHOPT_HISTEXPAND -DSHOPT_KIA -DSHOPT_MULTIBYTE " +
                "-DSHOPT_NAMESPACE -DSHOPT_OPTIMIZE -DSHOPT_PFSH -DSHOPT_RAWONLY " +
                "-DSHOPT_SUID_EXEC -DSHOPT_SYSRC -DSHOPT_VSH -D_BLD_shell " +
                "-D_PACKAGE_ast -DERROR_CONTEXT_T=Error_context_t " +
                "-DUSAGE_LICENSE= \"[-author?David Korn <dgk@research.att.com>]\" \"[-copyright?Copyright (c) 1982-2007 AT&T Knowledge Ventures]\" \"[-license?http://www.opensource.org/licenses/cpl1.0.txt]\" \"[--catalog?libshell]\" " +
                "-DPIC -D_REENTRANT -c -o pics/data/builtins.o ../common/data/builtins.c";
        String expResult = "Source:../common/data/builtins.c\n" +
                "Macros:\n" +
                "ERROR_CONTEXT_T=Error_context_t\n" +
                "KSHELL\n" +
                "PIC\n" +
                "SHOPT_BRACEPAT\n" +
                "SHOPT_CMDLIB_BLTIN=0\n" +
                "SHOPT_CMDLIB_HDR=\"solaris_cmdlist.h\"\n" +
                "SHOPT_DYNAMIC\n" +
                "SHOPT_ESH\n" +
                "SHOPT_FILESCAN\n" +
                "SHOPT_HISTEXPAND\n" +
                "SHOPT_KIA\n" +
                "SHOPT_MULTIBYTE\n" +
                "SHOPT_NAMESPACE\n" +
                "SHOPT_OPTIMIZE\n" +
                "SHOPT_PFSH\n" +
                "SHOPT_RAWONLY\n" +
                "SHOPT_SUID_EXEC\n" +
                "SHOPT_SYSRC\n" +
                "SHOPT_VSH\n" +
                "SH_CMDLIB_DIR=\"/usr/ast/bin\"\n" +
                "TEXT_DOMAIN=\"SUNW_OST_OSLIB\"\n" +
                "USAGE_LICENSE=\"[-author?David Korn <dgk@research.att.com>]\" \"[-copyright?Copyright (c) 1982-2007 AT&T Knowledge Ventures]\" \"[-license?http://www.opensource.org/licenses/cpl1.0.txt]\" \"[--catalog?libshell]\"\n" +
                "_BLD_shell\n" +
                "_PACKAGE_ast\n" +
                "_REENTRANT\n" +
                "_TS_ERRNO\n" +
                "_XOPEN_SOURCE=600\n" +
                "__EXTENSIONS__=1\n" +
                "Paths:\n" +
                "src/cmd/ksh93\n" +
                "../common/include\n" +
                "/export/home/thp/opensolaris/proto/root_i386/usr/include/ast";

        String result = processLine(line, false);
        assertDocumentText(line, expResult, result);
    }

    /**
     * Test of scanCommandLine method, of class DwarfSource.
     */
    public void testScanCommandLine4() {
        String line = "/opt/onbld/bin/i386/cw -_cc -xO3 -xarch=amd64 -Ui386 -U__i386 -K pic  -Xa  " +
                "-xildoff -errtags=yes -errwarn=%all -erroff=E_EMPTY_TRANSLATION_UNIT " +
                "-erroff=E_STATEMENT_NOT_REACHED -erroff=E_UNRECOGNIZED_PRAGMA_IGNORED -xc99=%all " +
                "-D_XOPEN_SOURCE=600 -D__EXTENSIONS__=1    -W0,-xglobalstatic -v  -xstrconst -g " +
                "-xc99=%all -D_XOPEN_SOURCE=600 -D__EXTENSIONS__=1 -W0,-noglobal -_gcc=-fno-dwarf2-indirect-strings " +
                "-xdebugformat=stabs -DTEXT_DOMAIN=\"SUNW_OST_OSLIB\" -D_TS_ERRNO  " +
                "-Isrc/cmd/ksh93  -I../common/include  -I/export/home/thp/opensolaris/proto/root_i386/usr/include/ast  " +
                "-DKSHELL  -DSHOPT_BRACEPAT  -DSHOPT_CMDLIB_BLTIN=0  '-DSH_CMDLIB_DIR=\"/usr/ast/bin\"'  " +
                "'-DSHOPT_CMDLIB_HDR=\"solaris_cmdlist.h\"'  -DSHOPT_DYNAMIC  -DSHOPT_ESH  -DSHOPT_FILESCAN  " +
                "-DSHOPT_HISTEXPAND  -DSHOPT_KIA -DSHOPT_MULTIBYTE  -DSHOPT_NAMESPACE  -DSHOPT_OPTIMIZE  " +
                "-DSHOPT_PFSH  -DSHOPT_RAWONLY  -DSHOPT_SUID_EXEC  -DSHOPT_SYSRC  -DSHOPT_VSH  -D_BLD_shell  " +
                "-D_PACKAGE_ast  -DERROR_CONTEXT_T=Error_context_t  " +
                "'-DUSAGE_LICENSE= \"[-author?David Korn <dgk@research.att.com>]\" " +
                "\"[-copyright?Copyright (c) 1982-2007 AT&T Knowledge Ventures]\" " +
                "\"[-license?http://www.opensource.org/licenses/cpl1.0.txt]\" \"[--catalog?libshell]\"' " +
                "-DPIC -D_REENTRANT -c -o pics/data/builtins.o ../common/data/builtins.c";
        String expResult = "Source:../common/data/builtins.c\n" +
                "Macros:\n" +
                "ERROR_CONTEXT_T=Error_context_t\n" +
                "KSHELL\n" +
                "PIC\n" +
                "SHOPT_BRACEPAT\n" +
                "SHOPT_CMDLIB_BLTIN=0\n" +
                "SHOPT_CMDLIB_HDR=\"solaris_cmdlist.h\"\n" +
                "SHOPT_DYNAMIC\n" +
                "SHOPT_ESH\n" +
                "SHOPT_FILESCAN\n" +
                "SHOPT_HISTEXPAND\n" +
                "SHOPT_KIA\n" +
                "SHOPT_MULTIBYTE\n" +
                "SHOPT_NAMESPACE\n" +
                "SHOPT_OPTIMIZE\n" +
                "SHOPT_PFSH\n" +
                "SHOPT_RAWONLY\n" +
                "SHOPT_SUID_EXEC\n" +
                "SHOPT_SYSRC\n" +
                "SHOPT_VSH\n" +
                "SH_CMDLIB_DIR=\"/usr/ast/bin\"\n" +
                "TEXT_DOMAIN=\"SUNW_OST_OSLIB\"\n" +
                "USAGE_LICENSE=\"[-author?David Korn <dgk@research.att.com>]\" \"[-copyright?Copyright (c) 1982-2007 AT&T Knowledge Ventures]\" \"[-license?http://www.opensource.org/licenses/cpl1.0.txt]\" \"[--catalog?libshell]\"\n" +
                "_BLD_shell\n" +
                "_PACKAGE_ast\n" +
                "_REENTRANT\n" +
                "_TS_ERRNO\n" +
                "_XOPEN_SOURCE=600\n" +
                "__EXTENSIONS__=1\n" +
                "Paths:\n" +
                "src/cmd/ksh93\n" +
                "../common/include\n" +
                "/export/home/thp/opensolaris/proto/root_i386/usr/include/ast";
        String result = processLine(line, true);
        assertDocumentText(line, expResult, result);
    }

    /**
     * Test of scanCommandLine method, of class DwarfSource.
     */
    public void testGccLine() {
        String line = "/bin/sh ./libtool --tag=CXX --mode=compile /export/home/gcc/gccobj/gcc/xgcc -shared-libgcc -B/export/home/gcc/gccobj/gcc/ -nostdinc++ -L/export/home/gcc/gccobj/i386-pc-solaris2.11/libstdc++-v3/src -L/export/home/gcc/gccobj/i386-pc-solaris2.11/libstdc++-v3/src/.libs -B/usr/local/i386-pc-solaris2.11/bin/ -B/usr/local/i386-pc-solaris2.11/lib/ -isystem /usr/local/i386-pc-solaris2.11/include -isystem /usr/local/i386-pc-solaris2.11/sys-include -DHAVE_CONFIG_H -I. -I../../../libjava -I./include -I./gcj -I../../../libjava -Iinclude -I../../../libjava/include -I/export/home/gcc/boehm-gc/include  -DGC_SOLARIS_THREADS=1 -DGC_SOLARIS_PTHREADS=1 -DSOLARIS25_PROC_VDB_BUG_FIXED=1 -DSILENT=1 -DNO_SIGNALS=1 -DALL_INTERIOR_POINTERS=1 -DJAVA_FINALIZATION=1 -DGC_GCJ_SUPPORT=1 -DATOMIC_UNCOLLECTABLE=1   -I../../../libjava/libltdl -I../../../libjava/libltdl  -I../../../libjava/.././libjava/../gcc -I../../../libjava/../zlib -I../../../libjava/../libffi/include -I../libffi/include  -O2 -g3 -gdwarf-2 -fno-rtti -fnon-call-exceptions  -fdollars-in-identifiers -Wswitch-enum -ffloat-store  -I/usr/openwin/include -W -Wall -D_GNU_SOURCE -DPREFIX=\"\\\"/usr/local\\\"\" -DLIBDIR=\"\\\"/usr/local/lib\\\"\" -DBOOT_CLASS_PATH=\"\\\"/usr/local/share/java/libgcj-3.4.3.jar\\\"\" -g3 -gdwarf-2 -MD -MT gnu/gcj/natCore.lo -MF gnu/gcj/natCore.pp -c -o gnu/gcj/natCore.lo ../../../libjava/gnu/gcj/natCore.cc";
        String expResult ="Source:../../../libjava/gnu/gcj/natCore.cc\n" +
                "Macros:\n" +
                "ALL_INTERIOR_POINTERS=1\n" +
                "ATOMIC_UNCOLLECTABLE=1\n" +
                "BOOT_CLASS_PATH=\"\\\"/usr/local/share/java/libgcj-3.4.3.jar\\\"\"\n" +
                "GC_GCJ_SUPPORT=1\n" +
                "GC_SOLARIS_PTHREADS=1\n" +
                "GC_SOLARIS_THREADS=1\n" +
                "HAVE_CONFIG_H\n" +
                "JAVA_FINALIZATION=1\n" +
                "LIBDIR=\"\\\"/usr/local/lib\\\"\"\n" +
                "NO_SIGNALS=1\n" +
                "PREFIX=\"\\\"/usr/local\\\"\"\n" +
                "SILENT=1\n" +
                "SOLARIS25_PROC_VDB_BUG_FIXED=1\n" +
                "_GNU_SOURCE\n" +
                "Paths:\n" +
                "/usr/local/i386-pc-solaris2.11/include\n" +
                "/usr/local/i386-pc-solaris2.11/sys-include\n" +
                ".\n" +
                "../../../libjava\n" +
                "./include\n" +
                "./gcj\n" +
                "../../../libjava\n" +
                "include\n" +
                "../../../libjava/include\n" +
                "/export/home/gcc/boehm-gc/include\n" +
                "../../../libjava/libltdl\n" +
                "../../../libjava/libltdl\n" +
                "../../../libjava/.././libjava/../gcc\n" +
                "../../../libjava/../zlib\n" +
                "../../../libjava/../libffi/include\n" +
                "../libffi/include\n" +
                "/usr/openwin/include";
        String result = processLine(line, true);
        assertDocumentText(line, expResult, result);
    }

    public void testMSVCCompilerInvocation() {
        String line = "cl -Zi -Od -MDd -I/ws/cheetah/output/win32_mvm_debug/javacall/inc "+
                "/Zm1000 -DENABLE_CDC=0 -DENABLE_MIDP_MALLOC=1 -DENABLE_IMAGE_CACHE=1 "+
                "-DENABLE_ICON_CACHE=1 -DENABLE_I3_TEST=0    -DENABLE_NUTS_FRAMEWORK=0    "+
                "-DENABLE_NETWORK_INDICATOR=1 -DENABLE_MULTIPLE_ISOLATES=1 -DENABLE_MULTIPLE_DISPLAYS=0 "+
                "-DENABLE_JAVA_DEBUGGER=1 -DENABLE_NATIVE_APP_MANAGER=0 -DENABLE_NAMS_TEST_SERVICE=0 "+
                "-DENABLE_NATIVE_INSTALLER=0 -DENABLE_NATIVE_SUITE_STORAGE=0 -DENABLE_NATIVE_RMS=0 "+
                "-DENABLE_NATIVE_PTI=0 -DENABLE_MESSAGE_STRINGS=0 -DENABLE_CLDC_11=1 -DENABLE_VM_PROFILES=0 "+
                "-DENABLE_MONET=0 -DENABLE_SERVER_SOCKET=1 -DENABLE_JPEG=0 -DENABLE_DIRECT_DRAW=0 "+
                "-DENABLE_FILE_SYSTEM=1 -DENABLE_ON_DEVICE_DEBUG=1 -DENABLE_WTK_DEBUG=0 -DENABLE_AMS_FOLDERS=0 "+
                "-DENABLE_OCSP=0 -DENABLE_DYNAMIC_COMPONENTS=0 -DPROJECT_NAME='\"Sun Java Wireless Client\"' "+
                "-DAZZERT=1 -DENABLE_DEBUG=1 -DENABLE_CONTROL_ARGS_FROM_JAD=0 -DRELEASE='\"ap160621:11.28.08-19:33\"' "+
                "-DIMPL_VERSION='\"\"' -DFULL_VERSION='\"ap160621:11.28.08-19:33\"' -DROMIZING "+
                "-I/ws/cheetah/output/win32_mvm_debug/javacall/inc -DWIN32 -D_WINDOWS -D_DEBUG "+
                "-DAZZERT /W3 /nologo  -DHARDWARE_LITTLE_ENDIAN=1 -DHOST_LITTLE_ENDIAN=1 "+
                "/D ROMIZING -DJVM_RELEASE_VERSION='1.1' -DJVM_BUILD_VERSION='internal' "+
                "-DJVM_NAME='phoneME Feature VM' /MDd /Zi /Od  -DREQUIRES_JVMCONFIG_H=1 "+
                "-DENABLE_JSR_135=1 -DENABLE_MEDIA_RECORD                                       "+
                "-I/ws/cheetah/midp/src/protocol/socket/include       "+
                "-I/ws/cheetah/output/win32_mvm_debug/cldc/javacall_i386_vc/dist/include "+
                "-I/ws/cheetah/output/win32_mvm_debug/midp -I/ws/cheetah/output/win32_mvm_debug/pcsl/javacall_i386/inc "+
                "-I/ws/cheetah/abstractions/src/share/include -I/ws/cheetah/abstractions/src/cldc_application/native/include "+
                "-I/ws/cheetah/abstractions/src/cldc_application/native/javacall "+
                "-I/ws/cheetah/output/win32_mvm_debug/midp/generated  -I/ws/cheetah/midp/src/configuration/properties_port/include "+
                "-I/ws/cheetah/midp/src/core/suspend_resume/sr_main/include -I/ws/cheetah/midp/src/core/suspend_resume/sr_vm/include "+
                "-I/ws/cheetah/midp/src/core/suspend_resume/sr_port/include  -I/ws/cheetah/midp/src/core/crc32/include "+
                "-I/ws/cheetah/midp/src/core/jarutil/include -I/ws/cheetah/midp/src/core/global_status/include  "+
                "-I/ws/cheetah/midp/src/core/kni_util/include -I/ws/cheetah/midp/src/core/libc_ext/include "+
                "-I/ws/cheetah/midp/src/core/log/javacall/include -I/ws/cheetah/midp/src/core/native_thread/include "+
                "-I/ws/cheetah/midp/src/core/native_thread/stubs/include  -I/ws/cheetah/midp/src/core/resource_manager/include "+
                "-I/ws/cheetah/midp/src/core/timer_queue/include -I/ws/cheetah/midp/src/core/timer_queue/reference/include "+
                "-I/ws/cheetah/midp/src/core/timezone/include -I/ws/cheetah/midp/src/core/vm_services/include "+
                "-I/ws/cheetah/midp/src/core/memory/include -I/ws/cheetah/midp/src/core/storage/include "+
                "-I/ws/cheetah/midp/src/core/string/include -I/ws/cheetah/midp/src/events/eventqueue/include "+
                "-I/ws/cheetah/midp/src/events/eventqueue_port/include -I/ws/cheetah/midp/src/events/eventsystem/include "+
                "-I/ws/cheetah/midp/src/events/mastermode_port/include  -I/ws/cheetah/midp/src/ams/ams_base/include "+
                "-I/ws/cheetah/midp/src/ams/ams_base_cldc/include -I/ws/cheetah/midp/src/ams/platform_request/include "+
                "-I/ws/cheetah/midp/src/ams/suitestore/internal_api/include -I/ws/cheetah/midp/src/ams/suitestore/internal_api/reference/native "+
                "-I/ws/cheetah/midp/src/ams/suitestore/common_api/include -I/ws/cheetah/midp/src/ams/suitestore/common_api/reference/native "+
                "-I/ws/cheetah/midp/src/ams/suitestore/task_manager_api/include -I/ws/cheetah/midp/src/ams/suitestore/task_manager_api/reference/native "+
                "-I/ws/cheetah/midp/src/ams/suitestore/installer_api/include -I/ws/cheetah/midp/src/ams/suitestore/installer_api/reference/native "+
                "-I/ws/cheetah/midp/src/ams/suitestore/recordstore_api/include -I/ws/cheetah/midp/src/ams/suitestore/recordstore_api/reference/native "+
                "-I/ws/cheetah/midp/src/ams/suitestore/secure_api/include -I/ws/cheetah/midp/src/ams/suitestore/secure_api/reference/native "+
                "-I/ws/cheetah/midp/src/ams/appmanager_ui_resources/include -I/ws/cheetah/midp/src/ams/example/ams_common/include "+
                "-I/ws/cheetah/midp/src/ams/example/ams_common_port/include -I/ws/cheetah/midp/src/ams/example/jams/include "+
                "-I/ws/cheetah/midp/src/ams/example/jams_port/javacall/native -I/ws/cheetah/midp/src/ams/example/ams_params/include "+
                "-I/ws/cheetah/midp/src/ams/example/ams_common/include -I/ws/cheetah/midp/src/ams/example/ams_common/include "+
                "-I/ws/cheetah/midp/src/ams/example/ams_common_port/include -I/ws/cheetah/midp/src/ams/example/javacall_common/include "+
                "-I/ws/cheetah/midp/src/ams/example/jams_port/include      -I/ws/cheetah/midp/src/push/push_server/include "+
                "-I/ws/cheetah/midp/src/push/push_timer/include -I/ws/cheetah/midp/src/push/push_timer/javacall/include "+
                "-I/ws/cheetah/midp/src/i18n/i18n_main/include -I/ws/cheetah/midp/src/i18n/i18n_port/include "+
                "-I/ws/cheetah/midp/src/highlevelui/annunciator/include -I/ws/cheetah/midp/src/highlevelui/keymap/include  "+
                "-I/ws/cheetah/midp/src/highlevelui/lcdlf/include -I/ws/cheetah/midp/src/highlevelui/lcdlf/lfjava/include "+
                "-I/ws/cheetah/midp/src/highlevelui/lfjport/include  -I/ws/cheetah/midp/src/highlevelui/nim_port/include "+
                "-I/ws/cheetah/midp/src/highlevelui/javacall_application/javacall_app_common/include "+
                "-I/ws/cheetah/midp/src/highlevelui/javacall_application/javacall_mode_port/include  "+
                "-I/ws/cheetah/midp/src/lowlevelui/graphics_api/include -I/ws/cheetah/midp/src/lowlevelui/putpixel_port/include "+
                "-I/ws/cheetah/midp/src/lowlevelui/graphics/include -I/ws/cheetah/midp/src/lowlevelui/graphics/gx_putpixel/include "+
                "-I/ws/cheetah/midp/src/lowlevelui/graphics/gx_putpixel/native -I/ws/cheetah/midp/src/lowlevelui/image_api/include "+
                "-I/ws/cheetah/midp/src/lowlevelui/image/include  -I/ws/cheetah/midp/src/lowlevelui/image_decode/reference/native "+
                "-I/ws/cheetah/midp/src/lowlevelui/image_decode/include    -I/ws/cheetah/midp/src/rms/record_store/include "+
                "-I/ws/cheetah/midp/src/rms/record_store/file_based/native -I/ws/cheetah/midp/src/security/crypto/include "+
                "-I/ws/cheetah/midp/src/security/file_digest/include -I/ws/cheetah/midp/src/protocol/gcf/include "+
                "-I/ws/cheetah/midp/src/protocol/file/include    -I/ws/cheetah/midp/src/protocol/serial_port/include "+
                "-I/ws/cheetah/midp/src/protocol/socket/include    -I/ws/cheetah/midp/src/protocol/udp/include  "+
                "-I/ws/cheetah/midp/src/links/include     -I/ws/cheetah/jsr135/src/cldc_application/native/common "+
                "-I/ws/cheetah/jsr135/src/share/components/direct-player/native  -c "+
                "-Fo/ws/cheetah/output/win32_mvm_debug/midp/obj_g/i386/socketProtocol.o "+
                "`echo  /ws/cheetah/midp/src/protocol/socket/reference/native/socketProtocol.c | xargs -n1 cygpath -w` 	"+
                "> /ws/cheetah/output/win32_mvm_debug/midp/makelog.out 2>&1; status=$?; cat /ws/cheetah/output/win32_mvm_debug/midp/makelog.out | "+
                "tee -a /ws/cheetah/output/win32_mvm_debug/midp/make.out; if [ $status -ne 0 ]; then false; else true; fi";
        String expResult ="Source:/ws/cheetah/midp/src/protocol/socket/reference/native/socketProtocol.c\n"+
                "Macros:\n"+
                "AZZERT\n"+
                "ENABLE_AMS_FOLDERS=0\n"+
                "ENABLE_CDC=0\n"+
                "ENABLE_CLDC_11=1\n"+
                "ENABLE_CONTROL_ARGS_FROM_JAD=0\n"+
                "ENABLE_DEBUG=1\n"+
                "ENABLE_DIRECT_DRAW=0\n"+
                "ENABLE_DYNAMIC_COMPONENTS=0\n"+
                "ENABLE_FILE_SYSTEM=1\n"+
                "ENABLE_I3_TEST=0\n"+
                "ENABLE_ICON_CACHE=1\n"+
                "ENABLE_IMAGE_CACHE=1\n"+
                "ENABLE_JAVA_DEBUGGER=1\n"+
                "ENABLE_JPEG=0\n"+
                "ENABLE_JSR_135=1\n"+
                "ENABLE_MEDIA_RECORD\n"+
                "ENABLE_MESSAGE_STRINGS=0\n"+
                "ENABLE_MIDP_MALLOC=1\n"+
                "ENABLE_MONET=0\n"+
                "ENABLE_MULTIPLE_DISPLAYS=0\n"+
                "ENABLE_MULTIPLE_ISOLATES=1\n"+
                "ENABLE_NAMS_TEST_SERVICE=0\n"+
                "ENABLE_NATIVE_APP_MANAGER=0\n"+
                "ENABLE_NATIVE_INSTALLER=0\n"+
                "ENABLE_NATIVE_PTI=0\n"+
                "ENABLE_NATIVE_RMS=0\n"+
                "ENABLE_NATIVE_SUITE_STORAGE=0\n"+
                "ENABLE_NETWORK_INDICATOR=1\n"+
                "ENABLE_NUTS_FRAMEWORK=0\n"+
                "ENABLE_OCSP=0\n"+
                "ENABLE_ON_DEVICE_DEBUG=1\n"+
                "ENABLE_SERVER_SOCKET=1\n"+
                "ENABLE_VM_PROFILES=0\n"+
                "ENABLE_WTK_DEBUG=0\n"+
                "FULL_VERSION='\"ap160621:11.28.08-19:33\"'\n"+
                "HARDWARE_LITTLE_ENDIAN=1\n"+
                "HOST_LITTLE_ENDIAN=1\n"+
                "IMPL_VERSION='\"\"'\n"+
                "JVM_BUILD_VERSION='internal'\n"+
                "JVM_NAME='phoneME Feature VM'\n"+
                "JVM_RELEASE_VERSION='1.1'\n"+
                "PROJECT_NAME='\"Sun Java Wireless Client\"'\n"+
                "RELEASE='\"ap160621:11.28.08-19:33\"'\n"+
                "REQUIRES_JVMCONFIG_H=1\n"+
                "ROMIZING\n"+
                "WIN32\n"+
                "_DEBUG\n"+
                "_WINDOWS\n"+
                "Paths:\n"+
                "/ws/cheetah/output/win32_mvm_debug/javacall/inc\n"+
                "/ws/cheetah/output/win32_mvm_debug/javacall/inc\n"+
                "/ws/cheetah/midp/src/protocol/socket/include\n"+
                "/ws/cheetah/output/win32_mvm_debug/cldc/javacall_i386_vc/dist/include\n"+
                "/ws/cheetah/output/win32_mvm_debug/midp\n"+
                "/ws/cheetah/output/win32_mvm_debug/pcsl/javacall_i386/inc\n"+
                "/ws/cheetah/abstractions/src/share/include\n"+
                "/ws/cheetah/abstractions/src/cldc_application/native/include\n"+
                "/ws/cheetah/abstractions/src/cldc_application/native/javacall\n"+
                "/ws/cheetah/output/win32_mvm_debug/midp/generated\n"+
                "/ws/cheetah/midp/src/configuration/properties_port/include\n"+
                "/ws/cheetah/midp/src/core/suspend_resume/sr_main/include\n"+
                "/ws/cheetah/midp/src/core/suspend_resume/sr_vm/include\n"+
                "/ws/cheetah/midp/src/core/suspend_resume/sr_port/include\n"+
                "/ws/cheetah/midp/src/core/crc32/include\n"+
                "/ws/cheetah/midp/src/core/jarutil/include\n"+
                "/ws/cheetah/midp/src/core/global_status/include\n"+
                "/ws/cheetah/midp/src/core/kni_util/include\n"+
                "/ws/cheetah/midp/src/core/libc_ext/include\n"+
                "/ws/cheetah/midp/src/core/log/javacall/include\n"+
                "/ws/cheetah/midp/src/core/native_thread/include\n"+
                "/ws/cheetah/midp/src/core/native_thread/stubs/include\n"+
                "/ws/cheetah/midp/src/core/resource_manager/include\n"+
                "/ws/cheetah/midp/src/core/timer_queue/include\n"+
                "/ws/cheetah/midp/src/core/timer_queue/reference/include\n"+
                "/ws/cheetah/midp/src/core/timezone/include\n"+
                "/ws/cheetah/midp/src/core/vm_services/include\n"+
                "/ws/cheetah/midp/src/core/memory/include\n"+
                "/ws/cheetah/midp/src/core/storage/include\n"+
                "/ws/cheetah/midp/src/core/string/include\n"+
                "/ws/cheetah/midp/src/events/eventqueue/include\n"+
                "/ws/cheetah/midp/src/events/eventqueue_port/include\n"+
                "/ws/cheetah/midp/src/events/eventsystem/include\n"+
                "/ws/cheetah/midp/src/events/mastermode_port/include\n"+
                "/ws/cheetah/midp/src/ams/ams_base/include\n"+
                "/ws/cheetah/midp/src/ams/ams_base_cldc/include\n"+
                "/ws/cheetah/midp/src/ams/platform_request/include\n"+
                "/ws/cheetah/midp/src/ams/suitestore/internal_api/include\n"+
                "/ws/cheetah/midp/src/ams/suitestore/internal_api/reference/native\n"+
                "/ws/cheetah/midp/src/ams/suitestore/common_api/include\n"+
                "/ws/cheetah/midp/src/ams/suitestore/common_api/reference/native\n"+
                "/ws/cheetah/midp/src/ams/suitestore/task_manager_api/include\n"+
                "/ws/cheetah/midp/src/ams/suitestore/task_manager_api/reference/native\n"+
                "/ws/cheetah/midp/src/ams/suitestore/installer_api/include\n"+
                "/ws/cheetah/midp/src/ams/suitestore/installer_api/reference/native\n"+
                "/ws/cheetah/midp/src/ams/suitestore/recordstore_api/include\n"+
                "/ws/cheetah/midp/src/ams/suitestore/recordstore_api/reference/native\n"+
                "/ws/cheetah/midp/src/ams/suitestore/secure_api/include\n"+
                "/ws/cheetah/midp/src/ams/suitestore/secure_api/reference/native\n"+
                "/ws/cheetah/midp/src/ams/appmanager_ui_resources/include\n"+
                "/ws/cheetah/midp/src/ams/example/ams_common/include\n"+
                "/ws/cheetah/midp/src/ams/example/ams_common_port/include\n"+
                "/ws/cheetah/midp/src/ams/example/jams/include\n"+
                "/ws/cheetah/midp/src/ams/example/jams_port/javacall/native\n"+
                "/ws/cheetah/midp/src/ams/example/ams_params/include\n"+
                "/ws/cheetah/midp/src/ams/example/ams_common/include\n"+
                "/ws/cheetah/midp/src/ams/example/ams_common/include\n"+
                "/ws/cheetah/midp/src/ams/example/ams_common_port/include\n"+
                "/ws/cheetah/midp/src/ams/example/javacall_common/include\n"+
                "/ws/cheetah/midp/src/ams/example/jams_port/include\n"+
                "/ws/cheetah/midp/src/push/push_server/include\n"+
                "/ws/cheetah/midp/src/push/push_timer/include\n"+
                "/ws/cheetah/midp/src/push/push_timer/javacall/include\n"+
                "/ws/cheetah/midp/src/i18n/i18n_main/include\n"+
                "/ws/cheetah/midp/src/i18n/i18n_port/include\n"+
                "/ws/cheetah/midp/src/highlevelui/annunciator/include\n"+
                "/ws/cheetah/midp/src/highlevelui/keymap/include\n"+
                "/ws/cheetah/midp/src/highlevelui/lcdlf/include\n"+
                "/ws/cheetah/midp/src/highlevelui/lcdlf/lfjava/include\n"+
                "/ws/cheetah/midp/src/highlevelui/lfjport/include\n"+
                "/ws/cheetah/midp/src/highlevelui/nim_port/include\n"+
                "/ws/cheetah/midp/src/highlevelui/javacall_application/javacall_app_common/include\n"+
                "/ws/cheetah/midp/src/highlevelui/javacall_application/javacall_mode_port/include\n"+
                "/ws/cheetah/midp/src/lowlevelui/graphics_api/include\n"+
                "/ws/cheetah/midp/src/lowlevelui/putpixel_port/include\n"+
                "/ws/cheetah/midp/src/lowlevelui/graphics/include\n"+
                "/ws/cheetah/midp/src/lowlevelui/graphics/gx_putpixel/include\n"+
                "/ws/cheetah/midp/src/lowlevelui/graphics/gx_putpixel/native\n"+
                "/ws/cheetah/midp/src/lowlevelui/image_api/include\n"+
                "/ws/cheetah/midp/src/lowlevelui/image/include\n"+
                "/ws/cheetah/midp/src/lowlevelui/image_decode/reference/native\n"+
                "/ws/cheetah/midp/src/lowlevelui/image_decode/include\n"+
                "/ws/cheetah/midp/src/rms/record_store/include\n"+
                "/ws/cheetah/midp/src/rms/record_store/file_based/native\n"+
                "/ws/cheetah/midp/src/security/crypto/include\n"+
                "/ws/cheetah/midp/src/security/file_digest/include\n"+
                "/ws/cheetah/midp/src/protocol/gcf/include\n"+
                "/ws/cheetah/midp/src/protocol/file/include\n"+
                "/ws/cheetah/midp/src/protocol/serial_port/include\n"+
                "/ws/cheetah/midp/src/protocol/socket/include\n"+
                "/ws/cheetah/midp/src/protocol/udp/include\n"+
                "/ws/cheetah/midp/src/links/include\n"+
                "/ws/cheetah/jsr135/src/cldc_application/native/common\n"+
                "/ws/cheetah/jsr135/src/share/components/direct-player/native";
        String result = processLine(line, true);
        assertDocumentText(line, expResult, result);
        LogReader.LineInfo li = LogReader.testCompilerInvocation(line);
        assert li.compilerType == LogReader.CompilerType.CPP;
    }


    private String processLine(String line, boolean isScriptOutput) {
        List<String> userIncludes = new ArrayList<String>();
        Map<String, String> userMacros = new TreeMap<String, String>();
        line = LogReader.trimBackApostropheCalls(line, null);
        Pattern pattern = Pattern.compile(";|\\|\\||&&"); // ;, ||, && //NOI18N
        String[] cmds = pattern.split(line);
        String what = DiscoveryUtils.gatherCompilerLine(cmds[0], isScriptOutput, userIncludes, userMacros,null);
        StringBuilder res = new StringBuilder();
        res.append("Source:"+what+"\n");
        res.append("Macros:");
        for (Map.Entry<String, String> entry : userMacros.entrySet()) {
            res.append("\n");
            res.append(entry.getKey());
            if (entry.getValue() != null) {
                res.append("=");
                res.append(entry.getValue());
            }
        }
        res.append("\nPaths:");
        for (String path : userIncludes) {
            res.append("\n");
            res.append(path);
        }
        return res.toString();
    }
}
