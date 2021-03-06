/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.packaging;

import org.netbeans.modules.cnd.makeproject.api.PackagerFileElement;
import java.io.BufferedWriter;
import java.io.IOException;
import org.netbeans.modules.cnd.makeproject.api.PackagerInfoElement;
import org.netbeans.modules.cnd.makeproject.api.PackagerDescriptor;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.PackagingConfiguration;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.openide.util.NbBundle;

/**
 *
 * @author thp
 */
public class SVR4Packager implements PackagerDescriptor {

    public static final String PACKAGER_NAME = "SVR4"; // NOI18N

    public String getName() {
        return PACKAGER_NAME;
    }

    public String getDisplayName() {
        return getString("SCR4Package"); // FIXUP: typo...
    }

    public boolean hasInfoList() {
        return true;
    }

    public List<PackagerInfoElement> getDefaultInfoList(MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration) {
        String defArch;
        if (makeConfiguration.getPlatform().getValue() == Platform.PLATFORM_SOLARIS_INTEL) {
            defArch = "i386"; // NOI18N
        } else if (makeConfiguration.getPlatform().getValue() == Platform.PLATFORM_SOLARIS_SPARC) {
            defArch = "sparc"; // NOI18N
        } else {
            // Anything else ?
            defArch = "i386"; // NOI18N
        }
        List<PackagerInfoElement> infoList = new ArrayList<PackagerInfoElement>();
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "PKG", packagingConfiguration.getOutputName(), true, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "NAME", "Package description ...", true, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "ARCH", defArch, true, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "CATEGORY", "application", true, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "VERSION", "1.0", true, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "BASEDIR", "/opt", false, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "PSTAMP", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()), false, true)); // NOI18N
        infoList.add(new PackagerInfoElement(PACKAGER_NAME, "CLASSES", "none", false, true)); // NOI18N

        return infoList;
    }

    public List<String> getOptionalInfoList() {
        List<String> entryComboBox = new ArrayList<String>();

        entryComboBox.add("BASEDIR"); // NOI18N == PackagingConfiguration.TYPE_SVR4_PACKAGE
        entryComboBox.add("CLASSES"); // NOI18N
        entryComboBox.add("DESC"); // NOI18N
        entryComboBox.add("EMAIL"); // NOI18N
        entryComboBox.add("HOTLINE"); // NOI18N
        entryComboBox.add("INTONLY"); // NOI18N
        entryComboBox.add("ISTATES"); // NOI18N
        entryComboBox.add("MAXINST"); // NOI18N
        entryComboBox.add("ORDER"); // NOI18N
        entryComboBox.add("PSTAMP"); // NOI18N
        entryComboBox.add("RSTATES"); // NOI18N
        entryComboBox.add("SUNW_ISA"); // NOI18N
        entryComboBox.add("SUNW_LOC"); // NOI18N
        entryComboBox.add("SUNW_PKG_DIR"); // NOI18N
        entryComboBox.add("SUNW_PKG_ALLZONES"); // NOI18N
        entryComboBox.add("SUNW_PKG_HOLLOW"); // NOI18N
        entryComboBox.add("SUNW_PKG_THISZONE"); // NOI18N
        entryComboBox.add("SUNW_PKGLIST"); // NOI18N
        entryComboBox.add("SUNW_PKGTYPE"); // NOI18N
        entryComboBox.add("SUNW_PKGVERS"); // NOI18N
        entryComboBox.add("SUNW_PRODNAME"); // NOI18N
        entryComboBox.add("SUNW_PRODVERS"); // NOI18N
        entryComboBox.add("ULIMIT"); // NOI18N
        entryComboBox.add("VENDOR"); // NOI18N
        entryComboBox.add("VSTOCK"); // NOI18N

        return entryComboBox;
    }

    public String getDefaultOptions() {
        return ""; // NOI18N
    }

    public String getDefaultTool() {
        return "pkgmk"; // NOI18N
    }

    public boolean isOutputAFolder() {
        return true;
    }

    public String getOutputFileName(MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration) {
        return null;
    }

    public String getOutputFileSuffix() {
        return null;
    }

    public String getTopDir(MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration) {
        return packagingConfiguration.findInfoValueName("PKG"); // NOI18N
    }

    public boolean supportsGroupAndOwner() {
        return true;
    }
    
    public ShellSciptWriter getShellFileWriter() {
        return new ScriptWriter();
    }

    public class ScriptWriter implements ShellSciptWriter {

        public void writeShellScript(BufferedWriter bw, MakeConfiguration makeConfiguration, PackagingConfiguration packagingConfiguration) throws IOException {
            writePackagingScriptBodySVR4(bw, makeConfiguration);
        }

        private List<String> findUndefinedDirectories(PackagingConfiguration packagingConfiguration) {
            List<PackagerFileElement> fileList = packagingConfiguration.getFiles().getValue();
            HashSet<String> set = new HashSet<String>();
            ArrayList<String> list = new ArrayList<String>();

            // Already Defined
            for (PackagerFileElement elem : fileList) {
                if (elem.getType() == PackagerFileElement.FileType.DIRECTORY) {
                    String path = packagingConfiguration.expandMacros(elem.getTo());
                    if (path.endsWith("/")) { // NOI18N
                        path = path.substring(0, path.length() - 1);
                    }
                    set.add(path);
                }
            }
            // Do all sub dirrectories
            for (PackagerFileElement elem : fileList) {
                if (elem.getType() == PackagerFileElement.FileType.FILE || elem.getType() == PackagerFileElement.FileType.SOFTLINK) {
                    String path = IpeUtils.getDirName(packagingConfiguration.expandMacros(elem.getTo()));
                    String base = ""; // NOI18N
                    if (path != null && path.length() > 0) {
                        StringTokenizer tokenizer = new StringTokenizer(path, "/"); // NOI18N
                        while (tokenizer.hasMoreTokens()) {
                            if (base.length() > 0) {
                                base += "/"; // NOI18N
                            }
                            base += tokenizer.nextToken();
                            if (!set.contains(base)) {
                                set.add(base);
                                list.add(base);
                            }
                        }
                    }
                }
            }
            return list;
        }

        private void writePackagingScriptBodySVR4(BufferedWriter bw, MakeConfiguration conf) throws IOException {
            PackagingConfiguration packagingConfiguration = conf.getPackagingConfiguration();
            String packageName = packagingConfiguration.findInfoValueName("PKG"); // NOI18N // FIXUP: what is null????

            bw.write("# Create pkginfo and prototype files\n"); // NOI18N
            bw.write("PKGINFOFILE=${TMPDIR}/pkginfo\n"); // NOI18N
            bw.write("PROTOTYPEFILE=${TMPDIR}/prototype\n"); // NOI18N
            bw.write("rm -f $PKGINFOFILE $PROTOTYPEFILE\n"); // NOI18N
            bw.write("\n"); // NOI18N        
            bw.write("cd \"${TOP}\"\n"); // NOI18N
            List<PackagerInfoElement> infoList = packagingConfiguration.getHeaderSubList(PACKAGER_NAME);
            for (PackagerInfoElement elem : infoList) {
                bw.write("echo \'" + elem.getName() + "=\"" + packagingConfiguration.expandMacros(elem.getValue()) + "\"\'" + " >> $PKGINFOFILE\n"); // NOI18N
            }
            bw.write("\n"); // NOI18N       
            bw.write("cd \"${TOP}\"\n"); // NOI18N
            bw.write("echo \"i pkginfo=pkginfo\" >> $PROTOTYPEFILE\n"); // NOI18N
            bw.write("\n"); // NOI18N     
            List<String> dirList = findUndefinedDirectories(packagingConfiguration);
            for (String dir : dirList) {
                bw.write("echo \"");// NOI18N
                bw.write("d"); // NOI18N
                bw.write(" none"); // Classes // NOI18N
                bw.write(" " + dir); // NOI18N
                bw.write(" 0" + MakeOptions.getInstance().getDefExePerm()); // NOI18N
                bw.write(" " + MakeOptions.getInstance().getDefOwner()); // NOI18N
                bw.write(" " + MakeOptions.getInstance().getDefGroup()); // NOI18N
                bw.write("\""); // NOI18N
                bw.write(" >> $PROTOTYPEFILE\n"); // NOI18N

            }

            bw.write("\n"); // NOI18N
            List<PackagerFileElement> fileList = packagingConfiguration.getFiles().getValue();
            for (PackagerFileElement elem : fileList) {
                bw.write("echo \"");// NOI18N
                if (elem.getType() == PackagerFileElement.FileType.DIRECTORY) {
                    bw.write("d");// NOI18N
                } else if (elem.getType() == PackagerFileElement.FileType.FILE) {
                    bw.write("f");// NOI18N
                } else if (elem.getType() == PackagerFileElement.FileType.SOFTLINK) {
                    bw.write("s");// NOI18N
                } else {
                    assert false;
                }
                bw.write(" none"); // Classes // NOI18N
                bw.write(" " + elem.getTo());// NOI18N
                if (elem.getFrom().length() > 0) {
                    String from = elem.getFrom();
                    if (IpeUtils.isPathAbsolute(from)) {
                        from = IpeUtils.toRelativePath(conf.getBaseDir(), from);
                    }
                    bw.write("=" + from);// NOI18N
                }
                if (elem.getType() != PackagerFileElement.FileType.SOFTLINK) {
                    bw.write(" 0" + elem.getPermission());// NOI18N
                    bw.write(" " + elem.getOwner());// NOI18N
                    bw.write(" " + elem.getGroup());// NOI18N
                }
                bw.write("\""); // NOI18N
                bw.write(" >> $PROTOTYPEFILE\n"); // NOI18N
            }
            bw.write("\n"); // NOI18N
            bw.write("# Make package\n"); // NOI18N  
            bw.write("cd \"${TOP}\"\n"); // NOI18N
            bw.write(packagingConfiguration.getToolValue() + " " + packagingConfiguration.getOptionsValue() + " -o -f $PROTOTYPEFILE -r . -d $TMPDIR\n"); // NOI18N
            bw.write("checkReturnCode\n"); // NOI18N
//        bw.write("pkgtrans -s ${TMPDIR} tmp.pkg " + packageName + "\n"); // NOI18N
//        bw.write("checkReturnCode\n"); // NOI18N
            bw.write("rm -rf " + packagingConfiguration.getOutputValue() + "/" + packageName + "\n"); // NOI18N
            bw.write("mv ${TMPDIR}/" + packageName + " " + packagingConfiguration.getOutputValue() + "\n"); // NOI18N
            bw.write("checkReturnCode\n"); // NOI18N
            bw.write("echo Solaris SVR4: " + packagingConfiguration.getOutputValue() + "/" + packageName + "\n"); // NOI18N
            bw.write("\n"); // NOI18N
        }
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(PackagingConfiguration.class, s); // FIXUP: Using Bundl in .../api.configurations. Too latet to move bundles around
    }
}
