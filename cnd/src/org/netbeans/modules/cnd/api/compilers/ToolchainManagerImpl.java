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

package org.netbeans.modules.cnd.api.compilers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.modules.cnd.api.utils.Path;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Alexander Simon
 */
/*package-local*/ final class ToolchainManagerImpl extends ToolchainManager {

    private static final boolean TRACE = Boolean.getBoolean("cnd.toolchain.personality.trace"); // NOI18N
    private static final boolean CREATE_SHADOW = Boolean.getBoolean("cnd.toolchain.personality.create_shadow"); // NOI18N
    /*package-local*/ static final String CONFIG_FOLDER = "CND/ToolChain"; // NOI18N
    private List<ToolchainDescriptor> descriptors = new ArrayList<ToolchainDescriptor>();
    private Logger log = Logger.getLogger("cnd.toolchain.logger"); // NOI18N

    /*package-local*/ ToolchainManagerImpl() {
        initToolchainManager();
    }

    private void initToolchainManager() {
        try {
            Map<Integer, CompilerVendor> vendors = new TreeMap<Integer, CompilerVendor>();
            Map<String, String> cache = new HashMap<String, String>();
            FileObject folder = FileUtil.getConfigFile(CONFIG_FOLDER); 
            int indefinedID = Integer.MAX_VALUE / 2;
            if (folder != null && folder.isFolder()) {
                FileObject[] files = folder.getChildren();
                for (FileObject file : files) {
                    CompilerVendor v = new CompilerVendor(file.getNameExt());
                    Integer position = (Integer) file.getAttribute("position"); // NOI18N
                    if (position == null || vendors.containsKey(position)) {
                        position = indefinedID++;
                    }
                    if (read(file, files, v, new HashSet<FileObject>(), cache)) {
                        vendors.put(position, v);
                    }
                }
            }
            if (TRACE) {
                System.err.println("Declared vendors:");
            } // NOI18N
            for (CompilerVendor v : vendors.values()) {
                if (TRACE) {
                    System.err.println(v.toString());
                }
                descriptors.add(new ToolchainDescriptorImpl(v));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (CREATE_SHADOW) {
            writeToolchains();
        }
    }

    /**
     * available in package for testing only
     */
    /*package-local*/ void reinitToolchainManager() {
        descriptors.clear();
        initToolchainManager();
    }

    ToolchainDescriptor getToolchain(String name, int platform) {
        for (ToolchainDescriptor d : descriptors) {
            if (name.equals(d.getName()) && (isPlatforSupported(platform, d)
                        || isPlatforSupported(PlatformTypes.PLATFORM_NONE, d))) {
                return d;
            }
        }
        for (ToolchainDescriptor d : descriptors) {
            if (isPlatforSupported(PlatformTypes.PLATFORM_NONE, d)) {
                return d;
            }
        }
        return null;
    }

    List<ToolchainDescriptor> getAllToolchains() {
        return new ArrayList<ToolchainDescriptor>(descriptors);
    }

    List<ToolchainDescriptor> getToolchains(int platform) {
        List<ToolchainDescriptor> res = new ArrayList<ToolchainDescriptor>();
        for (ToolchainDescriptor d : descriptors) {
            if (isPlatforSupported(platform, d)) {
                res.add(d);
            }
        }
        return res;
    }

    boolean isPlatforSupported(int platform, ToolchainDescriptor d) {
        switch (platform) {
            case PlatformTypes.PLATFORM_SOLARIS_SPARC:
                for (String p : d.getPlatforms()) {
                    if ("sun_sparc".equals(p)) { // NOI18N
                        return true;
                    }
                }
                break;
            case PlatformTypes.PLATFORM_SOLARIS_INTEL:
                for (String p : d.getPlatforms()) {
                    if ("sun_intel".equals(p)) { // NOI18N
                        return true;
                    }
                }
                break;
            case PlatformTypes.PLATFORM_LINUX:
                for (String p : d.getPlatforms()) {
                    if ("linux".equals(p)) { // NOI18N
                        return true;
                    }
                }
                break;
            case PlatformTypes.PLATFORM_WINDOWS:
                for (String p : d.getPlatforms()) {
                    if ("windows".equals(p)) { // NOI18N
                        return true;
                    }
                }
                break;
            case PlatformTypes.PLATFORM_MACOSX:
                for (String p : d.getPlatforms()) {
                    if ("mac".equals(p)) { // NOI18N
                        return true;
                    }
                }
                break;
            case PlatformTypes.PLATFORM_GENERIC:
                for (String p : d.getPlatforms()) {
                    if ("unix".equals(p)) { // NOI18N
                        return true;
                    }
                }
                break;
            case PlatformTypes.PLATFORM_NONE:
                for (String p : d.getPlatforms()) {
                    if ("none".equals(p)) { // NOI18N
                        return true;
                    }
                }
                break;
        }
        return false;
    }

    boolean isMyFolder(String path, ToolchainDescriptor d, int platform, boolean known) {
        boolean res = isMyFolderImpl(path, d, platform, known);
        if (TRACE && res) {
            System.err.println("Path [" + path + "] belongs to tool chain " + d.getName());
        } // NOI18N
        return res;
    }

    private boolean isMyFolderImpl(String path, ToolchainDescriptor d, int platform, boolean known) {
        CompilerDescriptor c = d.getC();
        if (c == null || c.getNames().length == 0) {
            return false;
        }
        Pattern pattern = null;
        if (!known) {
            if (c.getPathPattern() != null) {
                if (platform == PlatformTypes.PLATFORM_WINDOWS) {
                    pattern = Pattern.compile(c.getPathPattern(), Pattern.CASE_INSENSITIVE);
                } else {
                    pattern = Pattern.compile(c.getPathPattern());
                }
            }
            if (pattern != null) {
                if (!pattern.matcher(path).find()) {
                    String f = c.getExistFolder();
                    if (f == null) {
                        return false;
                    }
                    File folder = new File(path + "/" + f); // NOI18N
                    if (!folder.exists() || !folder.isDirectory()) {
                        return false;
                    }
                }
            }
        }
        File file = new File(path + "/" + c.getNames()[0]); // NOI18N
        if (!file.exists()) {
            file = new File(path + "/" + c.getNames()[0] + ".exe"); // NOI18N
            if (!file.exists()) {
                file = new File(path + "/" + c.getNames()[0] + ".exe.lnk"); // NOI18N
                if (!file.exists()) {
                    return false;
                }
            }
        }
        String flag = c.getVersionFlags();
        if (flag == null) {
            return true;
        }
        if (c.getVersionPattern() == null) {
            return true;
        }
        pattern = Pattern.compile(c.getVersionPattern());
        String s = getCommandOutput(path, path + "/" + c.getNames()[0] + " " + flag, true); // NOI18N
        boolean res = pattern.matcher(s).find();
        if (TRACE && !res) {
            System.err.println("No match for pattern [" + c.getVersionPattern() + "]:");
        } // NOI18N
        if (TRACE && !res) {
            System.err.println("Run " + path + "/" + c.getNames()[0] + " " + flag + "\n" + s);
        } // NOI18N
        return res;
    }

    String getBaseFolder(ToolchainDescriptor d, int platform) {
        if (platform != PlatformTypes.PLATFORM_WINDOWS) {
            return null;
        }
        String pattern = d.getBaseFolderPattern();
        String key = d.getBaseFolderKey();
        if (key == null || pattern == null) {
            return null;
        }
        String base = readRegistry(key, pattern);
        if (base != null && d.getBaseFolderSuffix() != null) {
            base += "/" + d.getBaseFolderSuffix(); // NOI18N
        }
        return base;
    }

    String getCommandFolder(ToolchainDescriptor d, int platform) {
        if (platform != PlatformTypes.PLATFORM_WINDOWS) {
            return null;
        }
        String pattern = d.getCommandFolderPattern();
        String key = d.getCommandFolderKey();
        if (key == null || pattern == null) {
            return null;
        }
        String base = readRegistry(key, pattern);
        if (base != null && d.getCommandFolderSuffix() != null) {
            base += "\\" + d.getCommandFolderSuffix(); // NOI18N
        }
        // search for unregistered msys
        if (base == null) {
            pattern = d.getCommandFolderPathPattern();
            if (pattern != null && pattern.length() > 0) {
                Pattern p = Pattern.compile(pattern);
                for (String dir : Path.getPath()) {
                    if (p.matcher(dir).find()) {
                        base = dir;
                        break;
                    }
                }
            }
        }
        return base;
    }

    private String readRegistry(String key, String pattern) {
        List<String> list = new ArrayList<String>();
        list.add("C:/Windows/System32/reg.exe"); // NOI18N
        list.add("query"); // NOI18N
        list.add(key);
        list.add("/s"); // NOI18N
        ProcessBuilder pb = new ProcessBuilder(list);
        pb.redirectErrorStream(true);
        String base = null;
        try {
            if (TRACE) {
                System.err.println("Read registry " + key);
            } // NOI18N
            Process process = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            Pattern p = Pattern.compile(pattern);
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (TRACE) {
                    System.err.println("\t" + line);
                } // NOI18N
                Matcher m = p.matcher(line);
                if (m.find() && m.groupCount() == 1) {
                    base = m.group(1).trim();
                    if (TRACE) {
                        System.err.println("\tFound " + base);
                    } // NOI18N
                }
            }
        } catch (Exception ex) {
            if (TRACE) {
                ex.printStackTrace();
            }
        }
        if (base == null && key.startsWith("hklm\\")) { // NOI18N
            // Cygwin on my Vista system has this information in HKEY_CURRENT_USER
            base = readRegistry("hkcu\\" + key.substring(5), pattern); // NOI18N
        }
        return base;
    }

    private static String getCommandOutput(String path, String command, boolean stdout) {
        StringBuilder buf = new StringBuilder();
        if (path == null) {
            path = ""; // NOI18N
        }
        ArrayList<String> envp = new ArrayList<String>();
        for (String key : System.getenv().keySet()) {
            String value = System.getenv().get(key);
            if (key.equals(Path.getPathName())) {
                envp.add(Path.getPathName() + "=" + path + File.pathSeparatorChar + value); // NOI18N
            } else {
                String entry = key + "=" + (value != null ? value : ""); // NOI18N
                envp.add(entry);
            }
        }
        try {
            Process process = Runtime.getRuntime().exec(command, envp.toArray(new String[envp.size()])); // NOI18N
            InputStream is = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    buf.append(line);
                    buf.append('\n'); // NOI18N
                }
            } catch (IOException ex) {
                if (TRACE) {
                    ex.printStackTrace();
                }
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                if (TRACE) {
                    ex.printStackTrace();
                }
            }
            is = process.getErrorStream();
            reader = new BufferedReader(new InputStreamReader(is));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    buf.append(line);
                    buf.append('\n'); // NOI18N
                }
            } catch (IOException ex) {
                if (TRACE) {
                    ex.printStackTrace();
                }
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                if (TRACE) {
                    ex.printStackTrace();
                }
            }
        } catch (IOException ex) {
            if (TRACE) {
                ex.printStackTrace();
            }
        }
        return buf.toString();
    }

    private boolean read(FileObject file, FileObject[] files, CompilerVendor v, Set<FileObject> antiloop, Map<String, String> cache) {
        if (antiloop.contains(file)) {
            return false;
        }
        antiloop.add(file);
        String baseName = (String) file.getAttribute("extends"); // NOI18N
        if (baseName != null && baseName.length() > 0) {
            for (FileObject base : files) {
                if (baseName.equals(base.getName())) {
                    if (!read(base, files, v, antiloop, cache)) {
                        return false;
                    }
                }
            }
        }
        try {
            read(file.getInputStream(), v, cache);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private boolean read(InputStream inputStream, CompilerVendor v, Map<String, String> cache) {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(false);
        XMLReader xmlReader = null;
        try {
            SAXParser saxParser = spf.newSAXParser();
            xmlReader = saxParser.getXMLReader();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        SAXHandler handler = new SAXHandler(v, cache);
        xmlReader.setContentHandler(handler);

        try {
            InputSource source = new InputSource(inputStream);
            xmlReader.parse(source);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private String unsplit(String[] array) {
        if (array == null) {
            return ""; // NOI18N
        }
        StringBuilder buf = new StringBuilder();
        for (String s : array) {
            if (buf.length() > 0) {
                buf.append(',');
            }
            buf.append(s);
        }
        return buf.toString();
    }

    /**
     * available in package for testing only
     */
    /*package-local for testing*/ void writeToolchains() {
        FileObject folder = FileUtil.getConfigFile(CONFIG_FOLDER);
        if (folder != null && folder.isFolder()) {
            FileObject[] files = folder.getChildren();
            for (FileObject file : files) {
                String name = file.getNameExt();
                for (ToolchainDescriptor descriptor : descriptors) {
                    if (name.equals(descriptor.getFileName())) {
                        //System.err.println("Found file " + file.getNameExt()); // NOI18N
                        Document doc = XMLUtil.createDocument("toolchaindefinition", "http://www.netbeans.org/ns/cnd-toolchain-definition/1", null, null); // NOI18N
                        Element root = doc.getDocumentElement();
                        Element element;
                        element = doc.createElement("toolchain"); // NOI18N
                        element.setAttribute("name", descriptor.getName()); // NOI18N
                        element.setAttribute("display", descriptor.getDisplayName()); // NOI18N
                        element.setAttribute("family", unsplit(descriptor.getFamily())); // NOI18N
                        if (descriptor.getQmakeSpec() != null) {
                            element.setAttribute("qmakespec", descriptor.getQmakeSpec()); // NOI18N
                        }
                        root.appendChild(element);

                        element = doc.createElement("platforms"); // NOI18N
                        element.setAttribute("stringvalue", unsplit(descriptor.getPlatforms())); // NOI18N
                        root.appendChild(element);

                        if (descriptor.getDriveLetterPrefix() != null) {
                            element = doc.createElement("drive_letter_prefix"); // NOI18N
                            element.setAttribute("stringvalue", descriptor.getDriveLetterPrefix()); // NOI18N
                            root.appendChild(element);
                        }

                        if (descriptor.getMakefileWriter() != null) {
                            element = doc.createElement("makefile_writer"); // NOI18N
                            element.setAttribute("class", descriptor.getMakefileWriter()); // NOI18N
                            root.appendChild(element);
                        }

                        if (descriptor.getBaseFolderKey() != null ||
                                descriptor.getBaseFolderPattern() != null ||
                                descriptor.getBaseFolderPathPattern() != null ||
                                descriptor.getBaseFolderSuffix() != null) {
                            element = doc.createElement("base_folder"); // NOI18N
                            if (descriptor.getBaseFolderKey() != null) {
                                element.setAttribute("regestry", descriptor.getBaseFolderKey()); // NOI18N
                            }
                            if (descriptor.getBaseFolderPattern() != null) {
                                element.setAttribute("pattern", descriptor.getBaseFolderPattern()); // NOI18N
                            }
                            if (descriptor.getBaseFolderPathPattern() != null) {
                                element.setAttribute("path_patern", descriptor.getBaseFolderPathPattern()); // NOI18N
                            }
                            if (descriptor.getBaseFolderSuffix() != null) {
                                element.setAttribute("suffix", descriptor.getBaseFolderSuffix()); // NOI18N
                            }
                            root.appendChild(element);
                        }

                        if (descriptor.getCommandFolderKey() != null ||
                                descriptor.getCommandFolderPattern() != null ||
                                descriptor.getCommandFolderPathPattern() != null ||
                                descriptor.getCommandFolderSuffix() != null) {
                            element = doc.createElement("command_folder"); // NOI18N
                            if (descriptor.getCommandFolderKey() != null) {
                                element.setAttribute("regestry", descriptor.getCommandFolderKey()); // NOI18N
                            }
                            if (descriptor.getCommandFolderPattern() != null) {
                                element.setAttribute("pattern", descriptor.getCommandFolderPattern()); // NOI18N
                            }
                            if (descriptor.getCommandFolderPathPattern() != null) {
                                element.setAttribute("path_patern", descriptor.getCommandFolderPathPattern()); // NOI18N
                            }
                            if (descriptor.getCommandFolderSuffix() != null) {
                                element.setAttribute("suffix", descriptor.getCommandFolderSuffix()); // NOI18N
                            }
                            root.appendChild(element);
                        }
                        if (descriptor.getDefaultLocations() != null) {
                            element = doc.createElement("default_locations"); // NOI18N
                            root.appendChild(element);
                            for (Map.Entry<String, String> e : descriptor.getDefaultLocations().entrySet()) {
                                Element p = doc.createElement("platform"); // NOI18N
                                p.setAttribute("os", e.getKey()); // NOI18N
                                p.setAttribute("directory", e.getValue()); // NOI18N
                                element.appendChild(p);
                            }
                        }
                        CompilerDescriptor compiler;
                        compiler = descriptor.getC();
                        if (compiler != null) {
                            element = doc.createElement("c"); // NOI18N
                            writeCompiler(doc, element, compiler);
                            root.appendChild(element);
                        }

                        compiler = descriptor.getCpp();
                        if (compiler != null) {
                            element = doc.createElement("cpp"); // NOI18N
                            writeCompiler(doc, element, compiler);
                            root.appendChild(element);
                        }

                        compiler = descriptor.getFortran();
                        if (compiler != null) {
                            element = doc.createElement("fortran"); // NOI18N
                            writeCompiler(doc, element, compiler);
                            root.appendChild(element);
                        }

                        compiler = descriptor.getAssembler();
                        if (compiler != null) {
                            element = doc.createElement("assembler"); // NOI18N
                            writeCompiler(doc, element, compiler);
                            root.appendChild(element);
                        }

                        ScannerDescriptor scanner = descriptor.getScanner();
                        if (scanner != null) {
                            element = doc.createElement("scanner"); // NOI18N
                            writeScanner(doc, element, scanner);
                            root.appendChild(element);
                        }

                        LinkerDescriptor linker = descriptor.getLinker();
                        if (linker != null) {
                            element = doc.createElement("linker"); // NOI18N
                            writeLinker(doc, element, linker);
                            root.appendChild(element);
                        }

                        MakeDescriptor make = descriptor.getMake();
                        if (make != null) {
                            element = doc.createElement("make"); // NOI18N
                            writeMake(doc, element, make);
                            root.appendChild(element);
                        }

                        DebuggerDescriptor debugger = descriptor.getDebugger();
                        if (debugger != null) {
                            element = doc.createElement("debugger"); // NOI18N
                            writeDebugger(doc, element, debugger);
                            root.appendChild(element);
                        }
                        try {
                            FileLock lock = file.lock();
                            try {
                                OutputStream os = file.getOutputStream(lock);
                                try {
                                    XMLUtil.write(doc, os, "UTF-8"); // NOI18N
                                    file.setAttribute("extends", ""); // NOI18N
                                } finally {
                                    os.close();
                                }
                            } finally {
                                lock.releaseLock();
                            }
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        }
    }

    private void writeCompiler(Document doc, Element element, CompilerDescriptor compiler) {
        Element e;
        e = doc.createElement("compiler"); // NOI18N
        e.setAttribute("name", unsplit(compiler.getNames())); // NOI18N
        if (compiler.skipSearch()) {
            e.setAttribute("skip", "true"); // NOI18N
        }
        element.appendChild(e);
        if (compiler.getPathPattern() != null ||
                compiler.getExistFolder() != null) {
            e = doc.createElement("recognizer"); // NOI18N
            if (compiler.getPathPattern() != null) {
                e.setAttribute("pattern", compiler.getPathPattern()); // NOI18N
            }
            if (compiler.getExistFolder() != null) {
                e.setAttribute("or_exist_folder", compiler.getExistFolder()); // NOI18N
            }
            element.appendChild(e);
        }
        if (compiler.getVersionFlags() != null ||
                compiler.getVersionPattern() != null) {
            e = doc.createElement("version"); // NOI18N
            if (compiler.getVersionFlags() != null) {
                e.setAttribute("flags", compiler.getVersionFlags()); // NOI18N
            }
            if (compiler.getVersionPattern() != null) {
                e.setAttribute("pattern", compiler.getVersionPattern()); // NOI18N
            }
            element.appendChild(e);
        }
        writeAlternativePath(doc, element, compiler);
        if (compiler.getIncludeFlags() != null ||
                compiler.getIncludeParser() != null ||
                compiler.getRemoveIncludePathPrefix() != null ||
                compiler.getRemoveIncludeOutputPrefix() != null) {
            e = doc.createElement("system_include_paths"); // NOI18N
            if (compiler.getIncludeFlags() != null) {
                e.setAttribute("flags", compiler.getIncludeFlags()); // NOI18N
            }
            if (compiler.getIncludeParser() != null) {
                e.setAttribute("parser", compiler.getIncludeParser()); // NOI18N
            }
            if (compiler.getRemoveIncludePathPrefix() != null) {
                e.setAttribute("remove_in_path", compiler.getRemoveIncludePathPrefix()); // NOI18N
            }
            if (compiler.getRemoveIncludeOutputPrefix() != null) {
                e.setAttribute("remove_in_output", compiler.getRemoveIncludeOutputPrefix()); // NOI18N
            }
            element.appendChild(e);
        }
        if (compiler.getUserIncludeFlag() != null) {
            e = doc.createElement("user_include"); // NOI18N
            e.setAttribute("flags", compiler.getUserIncludeFlag()); // NOI18N
            element.appendChild(e);
        }
        if (compiler.getMacroFlags() != null ||
                compiler.getMacroParser() != null ||
                compiler.getPredefinedMacros() != null) {
            e = doc.createElement("system_macros"); // NOI18N
            if (compiler.getMacroFlags() != null) {
                e.setAttribute("flags", compiler.getMacroFlags()); // NOI18N
            }
            if (compiler.getMacroParser() != null) {
                e.setAttribute("parser", compiler.getMacroParser()); // NOI18N
            }
            element.appendChild(e);
            if (compiler.getPredefinedMacros() != null) {
                for(PredefinedMacro p : compiler.getPredefinedMacros()){
                    Element ee = doc.createElement("macro"); // NOI18N
                    ee.setAttribute("stringvalue", p.getMacro()); // NOI18N
                    if (p.getFlags() != null) {
                        ee.setAttribute("flags", p.getFlags()); // NOI18N
                    }
                    e.appendChild(ee);
                }
            }
        }
        if (compiler.getUserMacroFlag() != null) {
            e = doc.createElement("user_macro"); // NOI18N
            e.setAttribute("flags", compiler.getUserMacroFlag()); // NOI18N
            element.appendChild(e);
        }
        writeDevelopmentMode(doc, element, compiler);
        writeWarningLevel(doc, element, compiler);
        writeArchitecture(doc, element, compiler);
        if (compiler.getStripFlag() != null) {
            e = doc.createElement("strip"); // NOI18N
            e.setAttribute("flags", compiler.getStripFlag()); // NOI18N
            element.appendChild(e);
        }
        writeMultithreading(doc, element, compiler);
        writeStandard(doc, element, compiler);
        writeLanguageExtension(doc, element, compiler);
        writeLibrary(doc, element, compiler);
        if (compiler.getOutputObjectFileFlags() != null) {
            e = doc.createElement("output_object_file"); // NOI18N
            e.setAttribute("flags", compiler.getOutputObjectFileFlags()); // NOI18N
            element.appendChild(e);
        }
        if (compiler.getDependencyGenerationFlags() != null) {
            e = doc.createElement("dependency_generation"); // NOI18N
            e.setAttribute("flags", compiler.getDependencyGenerationFlags()); // NOI18N
            element.appendChild(e);
        }
        if (compiler.getPrecompiledHeaderFlags() != null ||
                compiler.getPrecompiledHeaderSuffix() != null) {
            e = doc.createElement("precompiled_header"); // NOI18N
            if (compiler.getPrecompiledHeaderFlags() != null) {
                e.setAttribute("flags", compiler.getPrecompiledHeaderFlags()); // NOI18N
            }
            if (compiler.getPrecompiledHeaderSuffix() != null) {
                e.setAttribute("suffix", compiler.getPrecompiledHeaderSuffix()); // NOI18N
            }
            if (compiler.getPrecompiledHeaderSuffixAppend()) {
                e.setAttribute("append", "true"); // NOI18N
            }
            element.appendChild(e);
        }
    }

    private void writeDevelopmentMode(Document doc, Element element, CompilerDescriptor compiler) {
        String[] flags = compiler.getDevelopmentModeFlags();
        if (flags == null) {
            return;
        }
        int def = 0;
        if (compiler instanceof CompilerDescriptorImpl) {
            def = ((CompilerDescriptorImpl) compiler).tool.developmentMode.default_selection;
        }
        Element e = doc.createElement("development_mode"); // NOI18N
        element.appendChild(e);
        Element c;
        String[] names = new String[]{"fast_build", "debug", "performance_debug", // NOI18N
            "test_coverage", "diagnosable_release", "release", // NOI18N
            "performance_release"}; // NOI18N
        for (int i = 0; i < flags.length; i++) {
            c = doc.createElement(names[i]);
            c.setAttribute("flags", flags[i]); // NOI18N
            if (def == i) {
                c.setAttribute("default", "true"); // NOI18N
            }
            e.appendChild(c);
        }
    }

    private void writeWarningLevel(Document doc, Element element, CompilerDescriptor compiler) {
        String[] flags = compiler.getWarningLevelFlags();
        if (flags == null) {
            return;
        }
        int def = 0;
        if (compiler instanceof CompilerDescriptorImpl) {
            def = ((CompilerDescriptorImpl) compiler).tool.warningLevel.default_selection;
        }
        Element e = doc.createElement("warning_level"); // NOI18N
        element.appendChild(e);
        Element c;
        String[] names = new String[]{"no_warnings", "default", "more_warnings", // NOI18N
            "warning2error"}; // NOI18N
        for (int i = 0; i < flags.length; i++) {
            c = doc.createElement(names[i]);
            c.setAttribute("flags", flags[i]); // NOI18N
            if (def == i) {
                c.setAttribute("default", "true"); // NOI18N
            }
            e.appendChild(c);
        }
    }

    private void writeArchitecture(Document doc, Element element, CompilerDescriptor compiler) {
        String[] flags = compiler.getArchitectureFlags();
        if (flags == null) {
            return;
        }
        int def = 0;
        if (compiler instanceof CompilerDescriptorImpl) {
            def = ((CompilerDescriptorImpl) compiler).tool.architecture.default_selection;
        }
        Element e = doc.createElement("architecture"); // NOI18N
        element.appendChild(e);
        Element c;
        String[] names = new String[]{"default", "bits_32", "bits_64"}; // NOI18N
        for (int i = 0; i < flags.length; i++) {
            c = doc.createElement(names[i]);
            c.setAttribute("flags", flags[i]); // NOI18N
            if (def == i) {
                c.setAttribute("default", "true"); // NOI18N
            }
            e.appendChild(c);
        }
    }

    private void writeMultithreading(Document doc, Element element, CompilerDescriptor compiler) {
        String[] flags = compiler.getMultithreadingFlags();
        if (flags == null) {
            return;
        }
        int def = 0;
        if (compiler instanceof CompilerDescriptorImpl) {
            def = ((CompilerDescriptorImpl) compiler).tool.multithreading.default_selection;
        }
        Element e = doc.createElement("multithreading"); // NOI18N
        element.appendChild(e);
        Element c;
        String[] names = new String[]{"none", "safe", "automatic", "open_mp"}; // NOI18N
        for (int i = 0; i < flags.length; i++) {
            c = doc.createElement(names[i]);
            c.setAttribute("flags", flags[i]); // NOI18N
            if (def == i) {
                c.setAttribute("default", "true"); // NOI18N
            }
            e.appendChild(c);
        }
    }

    private void writeStandard(Document doc, Element element, CompilerDescriptor compiler) {
        String[] flags = compiler.getStandardFlags();
        if (flags == null) {
            return;
        }
        int def = 0;
        if (compiler instanceof CompilerDescriptorImpl) {
            def = ((CompilerDescriptorImpl) compiler).tool.standard.default_selection;
        }
        Element e = doc.createElement("standard"); // NOI18N
        element.appendChild(e);
        Element c;
        String[] names = new String[]{"old", "legacy", "default", "modern"}; // NOI18N
        for (int i = 0; i < flags.length; i++) {
            c = doc.createElement(names[i]);
            c.setAttribute("flags", flags[i]); // NOI18N
            if (def == i) {
                c.setAttribute("default", "true"); // NOI18N
            }
            e.appendChild(c);
        }
    }

    private void writeLanguageExtension(Document doc, Element element, CompilerDescriptor compiler) {
        String[] flags = compiler.getLanguageExtensionFlags();
        if (flags == null) {
            return;
        }
        int def = 0;
        if (compiler instanceof CompilerDescriptorImpl) {
            def = ((CompilerDescriptorImpl) compiler).tool.languageExtension.default_selection;
        }
        Element e = doc.createElement("language_extension"); // NOI18N
        element.appendChild(e);
        Element c;
        String[] names = new String[]{"none", "default", "all"}; // NOI18N
        for (int i = 0; i < flags.length; i++) {
            c = doc.createElement(names[i]);
            c.setAttribute("flags", flags[i]); // NOI18N
            if (def == i) {
                c.setAttribute("default", "true"); // NOI18N
            }
            e.appendChild(c);
        }
    }

    private void writeLibrary(Document doc, Element element, CompilerDescriptor compiler) {
        String[] flags = compiler.getLibraryFlags();
        if (flags == null) {
            return;
        }
        int def = 0;
        if (compiler instanceof CompilerDescriptorImpl) {
            def = ((CompilerDescriptorImpl) compiler).tool.library.default_selection;
        }
        Element e = doc.createElement("library"); // NOI18N
        element.appendChild(e);
        Element c;
        String[] names = new String[]{"none", "runtime", "classic", // NOI18N
            "binary_standard", "conforming_standard"}; // NOI18N
        for (int i = 0; i < flags.length; i++) {
            c = doc.createElement(names[i]);
            c.setAttribute("flags", flags[i]); // NOI18N
            if (def == i) {
                c.setAttribute("default", "true"); // NOI18N
            }
            e.appendChild(c);
        }
    }

    private void writeScanner(Document doc, Element element, ScannerDescriptor scanner) {
        Element c;
        for (ScannerPattern pattern : scanner.getPatterns()) {
            c = doc.createElement(pattern.getSeverity());
            c.setAttribute("pattern", pattern.getPattern()); // NOI18N
            if (pattern.getLanguage() != null) {
                c.setAttribute("language", pattern.getLanguage()); // NOI18N
            }
            element.appendChild(c);
        }
        if (scanner.getStackHeaderPattern() != null) {
            c = doc.createElement("stack_header"); // NOI18N
            c.setAttribute("pattern", scanner.getStackHeaderPattern()); // NOI18N
            element.appendChild(c);
        }
        if (scanner.getStackNextPattern() != null) {
            c = doc.createElement("stack_next"); // NOI18N
            c.setAttribute("pattern", scanner.getStackNextPattern()); // NOI18N
            element.appendChild(c);
        }
        if (scanner.getEnterDirectoryPattern() != null) {
            c = doc.createElement("enter_directory"); // NOI18N
            c.setAttribute("pattern", scanner.getEnterDirectoryPattern()); // NOI18N
            element.appendChild(c);
        }
        if (scanner.getChangeDirectoryPattern() != null) {
            c = doc.createElement("change_directory"); // NOI18N
            c.setAttribute("pattern", scanner.getChangeDirectoryPattern()); // NOI18N
            element.appendChild(c);
        }
        if (scanner.getLeaveDirectoryPattern() != null) {
            c = doc.createElement("leave_directory"); // NOI18N
            c.setAttribute("pattern", scanner.getLeaveDirectoryPattern()); // NOI18N
            element.appendChild(c);
        }
    }

    private void writeLinker(Document doc, Element element, LinkerDescriptor linker) {
        Element c;
        if (linker.getLibraryPrefix() != null) {
            c = doc.createElement("library_prefix"); // NOI18N
            c.setAttribute("stringvalue", linker.getLibraryPrefix()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getLibrarySearchFlag() != null) {
            c = doc.createElement("library_search"); // NOI18N
            c.setAttribute("flags", linker.getLibrarySearchFlag()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getDynamicLibrarySearchFlag() != null) {
            c = doc.createElement("dynamic_library_search"); // NOI18N
            c.setAttribute("flags", linker.getDynamicLibrarySearchFlag()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getLibraryFlag() != null) {
            c = doc.createElement("library_flag"); // NOI18N
            c.setAttribute("flags", linker.getLibraryFlag()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getPICFlag() != null) {
            c = doc.createElement("PIC"); // NOI18N
            c.setAttribute("flags", linker.getPICFlag()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getStaticLibraryFlag() != null) {
            c = doc.createElement("static_library"); // NOI18N
            c.setAttribute("flags", linker.getStaticLibraryFlag()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getDynamicLibraryFlag() != null) {
            c = doc.createElement("dynamic_library"); // NOI18N
            c.setAttribute("flags", linker.getDynamicLibraryFlag()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getDynamicLibraryBasicFlag() != null) {
            c = doc.createElement("dynamic_library_basic"); // NOI18N
            c.setAttribute("flags", linker.getDynamicLibraryBasicFlag()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getOutputFileFlag() != null) {
            c = doc.createElement("output_file"); // NOI18N
            c.setAttribute("flags", linker.getOutputFileFlag()); // NOI18N
            element.appendChild(c);
        }
    }

    private void writeMake(Document doc, Element element, MakeDescriptor make) {
        Element c;
        c = doc.createElement("tool"); // NOI18N
        c.setAttribute("name", unsplit(make.getNames())); // NOI18N
        if (make.skipSearch()) {
            c.setAttribute("skip", "true"); // NOI18N
        }
        element.appendChild(c);

        if (make.getVersionFlags() != null ||
                make.getVersionPattern() != null) {
            c = doc.createElement("version"); // NOI18N
            if (make.getVersionFlags() != null) {
                c.setAttribute("flags", make.getVersionFlags()); // NOI18N
            }
            if (make.getVersionPattern() != null) {
                c.setAttribute("pattern", make.getVersionPattern()); // NOI18N
            }
            element.appendChild(c);
        }
        writeAlternativePath(doc, element, make);
        if (make.getDependencySupportCode() != null) {
            c = doc.createElement("dependency_support"); // NOI18N
            c.setAttribute("code", make.getDependencySupportCode()); // NOI18N
            element.appendChild(c);
        }
    }

    private void writeDebugger(Document doc, Element element, DebuggerDescriptor debugger) {
        Element c;
        c = doc.createElement("tool"); // NOI18N
        c.setAttribute("name", unsplit(debugger.getNames())); // NOI18N
        if (debugger.skipSearch()) {
            c.setAttribute("skip", "true"); // NOI18N
        }
        element.appendChild(c);
        if (debugger.getVersionFlags() != null ||
                debugger.getVersionPattern() != null) {
            c = doc.createElement("version"); // NOI18N
            if (debugger.getVersionFlags() != null) {
                c.setAttribute("flags", debugger.getVersionFlags()); // NOI18N
            }
            if (debugger.getVersionPattern() != null) {
                c.setAttribute("pattern", debugger.getVersionPattern()); // NOI18N
            }
            element.appendChild(c);
        }
        writeAlternativePath(doc, element, debugger);
    }

    private void writeAlternativePath(Document doc, Element element, ToolDescriptor tool){
        AlternativePath[] paths = tool.getAlternativePath();
        if (paths != null) {
            Element c = doc.createElement("alternative_path"); // NOI18N
            element.appendChild(c);
            for(AlternativePath path : paths){
                Element p = doc.createElement("path"); // NOI18N
                c.appendChild(p);
                switch(path.getKind()){
                    case PATH:
                        p.setAttribute("directory", path.getPath()); // NOI18N
                        break;
                    case TOOL_FAMILY:
                        p.setAttribute("toolchain_family", path.getPath()); // NOI18N
                        break;
                    case TOOL_NAME:
                        p.setAttribute("toolchain_name", path.getPath()); // NOI18N
                        break;
                }

            }
        }
    }

    /**
     * class package-local for testin only
     */
    static final class CompilerVendor {

        final String toolChainFileName;
        String toolChainName;
        String toolChainDisplay;
        Map<String, String> default_locations;
        String family;
        String platforms;
        String driveLetterPrefix;
        String baseFolderKey;
        String baseFolderPattern;
        String baseFolderSuffix;
        String baseFolderPathPattern;
        String commandFolderKey;
        String commandFolderPattern;
        String commandFolderSuffix;
        String commandFolderPathPattern;
        String qmakespec;
        String makefileWriter;
        Compiler c = new Compiler();
        Compiler cpp = new Compiler();
        Compiler fortran = new Compiler();
        Compiler assembler = new Compiler();
        Scanner scanner = new Scanner();
        Linker linker = new Linker();
        Make make = new Make();
        Debugger debugger = new Debugger();

        private CompilerVendor(String fileName) {
            toolChainFileName = fileName;
        }

        public boolean isValid() {
            return toolChainName != null && toolChainName.length() > 0 &&
                    (c.isValid() || cpp.isValid() || fortran.isValid());
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append("Toolchain [" + toolChainName + "/" + family + "] " + toolChainDisplay + "\n"); // NOI18N
            buf.append("\tPlatforms [" + platforms + "]\n"); // NOI18N
            buf.append("\tDrive Letter Prefix [" + driveLetterPrefix + "]\n"); // NOI18N
            buf.append("\tBase Folder Key [" + baseFolderKey + "] Pattern [" + baseFolderPattern + // NOI18N
                    "] Suffix [" + baseFolderSuffix + "] Path Pattern[" + baseFolderPattern + "] \n"); // NOI18N
            buf.append("\tCommand Folder Key [" + commandFolderKey + "] Pattern [" + commandFolderPattern + // NOI18N
                    "] Suffix [" + commandFolderSuffix + "] Path Pattern[" + commandFolderPattern + "] \n"); // NOI18N
            buf.append("C compiler [" + c.name + "] Recognize path [" + c.pathPattern + // NOI18N
                    "] Version [" + c.versionFlags + ";" + c.versionPattern + "]\n"); // NOI18N
            buf.append("\tInclude flags [" + c.includeFlags + "] parser [" + c.includeOutputParser + // NOI18N
                    "] remove from path[" + c.removeIncludePathPrefix + "] remove from output [" + c.removeIncludeOutputPrefix + "]\n"); // NOI18N
            buf.append("\tMacros flags [" + c.macrosFlags + "] parser [" + c.macrosOutputParser + "]\n"); // NOI18N
            buf.append("\tDevelopment mode " + c.developmentMode + "\n"); // NOI18N
            buf.append("\tWarning Level " + c.warningLevel + "\n"); // NOI18N
            buf.append("\tArchitecture " + c.architecture + "\n"); // NOI18N
            buf.append("\tStrip [" + c.strip + "]\n"); // NOI18N
            if (c.multithreading.isValid()) {
                buf.append("\tMultithreading [" + c.multithreading + "]\n");// NOI18N
            }
            if (c.standard.isValid()) {
                buf.append("\tStandard [" + c.standard + "]\n");// NOI18N
            }
            if (c.languageExtension.isValid()) {
                buf.append("\tLanguage [" + c.languageExtension + "]\n");// NOI18N
            }
            if (c.library.isValid()) {
                buf.append("\tLibrary [" + c.library + "]\n");// NOI18N
            }
            buf.append("C++ compiler [" + cpp.name + "] Recognize path [" + cpp.pathPattern + // NOI18N
                    "] Version [" + cpp.versionFlags + ";" + cpp.versionPattern + "]\n"); // NOI18N
            buf.append("\tInclude flags [" + cpp.includeFlags + "] parser [" + cpp.includeOutputParser + // NOI18N
                    "] remove from path[" + cpp.removeIncludePathPrefix + "] remove from output [" + cpp.removeIncludeOutputPrefix + "]\n"); // NOI18N
            buf.append("\tMacros flags [" + cpp.macrosFlags + "] parser [" + cpp.macrosOutputParser + "]\n"); // NOI18N
            buf.append("\tDevelopment mode " + cpp.developmentMode + "\n"); // NOI18N
            buf.append("\tWarning Level " + cpp.warningLevel + "\n"); // NOI18N
            buf.append("\tArchitecture " + cpp.architecture + "\n"); // NOI18N
            buf.append("\tStrip [" + cpp.strip + "]\n"); // NOI18N
            buf.append("\tDependency generation flags [" + cpp.dependencyGenerationFlags + "]\n"); // NOI18N
            if (cpp.multithreading.isValid()) {
                buf.append("\tMultithreading " + cpp.multithreading + "\n");// NOI18N
            }
            if (cpp.standard.isValid()) {
                buf.append("\tStandard " + cpp.standard + "\n"); // NOI18N
            }
            if (cpp.languageExtension.isValid()) {
                buf.append("\tLanguage " + cpp.languageExtension + "\n"); // NOI18N
            }
            if (cpp.library.isValid()) {
                buf.append("\tLibrary " + cpp.library + "\n"); // NOI18N
            }
            if (fortran.isValid()) {
                buf.append("Fortran compiler [" + fortran.name + "] Recognize path [" + fortran.pathPattern + // NOI18N
                        "] Version [" + fortran.versionFlags + ";" + fortran.versionPattern + "]\n"); // NOI18N
                buf.append("\tDevelopment mode " + fortran.developmentMode + "\n"); // NOI18N
                buf.append("\tWarning Level " + fortran.warningLevel + "\n"); // NOI18N
                buf.append("\tArchitecture " + fortran.architecture + "\n"); // NOI18N
                buf.append("\tStrip [" + fortran.strip + "]\n"); // NOI18N
            }
            buf.append("Scanner\n"); // NOI18N
            buf.append("\tChange Directory [" + scanner.changeDirectoryPattern + "]\n"); // NOI18N
            buf.append("\tEnter Directory [" + scanner.enterDirectoryPattern + "]\n"); // NOI18N
            buf.append("\tLeave Directory [" + scanner.leaveDirectoryPattern + "]\n"); // NOI18N
            buf.append("\tStack Header [" + scanner.stackHeaderPattern + "]\n"); // NOI18N
            buf.append("\tStack Next [" + scanner.stackNextPattern + "]\n"); // NOI18N
            for (ErrorPattern p : scanner.patterns) {
                buf.append("\tPattern [" + p.pattern + "] Level [" + p.severity + "] Language [" + p.language + "]\n"); // NOI18N
            }
            buf.append("Linker\n"); // NOI18N
            buf.append("\tLibrary prefix [" + linker.library_prefix + "]\n"); // NOI18N
            buf.append("\tLibrary search [" + linker.librarySearchFlag + "]\n"); // NOI18N
            buf.append("\tDynamic library search [" + linker.dynamicLibrarySearchFlag + "]\n"); // NOI18N
            buf.append("\tLibrary [" + linker.libraryFlag + "]\n"); // NOI18N
            buf.append("\tPIC [" + linker.PICFlag + "]\n"); // NOI18N
            buf.append("\tStatic library [" + linker.staticLibraryFlag + "]\n"); // NOI18N
            buf.append("\tDynamic library [" + linker.dynamicLibraryFlag + "]\n"); // NOI18N
            buf.append("\tDynamic library basic [" + linker.dynamicLibraryBasicFlag + "]\n"); // NOI18N
            buf.append("Make [" + make.name + "] Version [" + make.versionFlags + "; " + make.versionPattern + "]\n");  // NOI18N
            buf.append("\tDependency support code [" + make.dependencySupportCode + "]\n"); // NOI18N
            buf.append("Debugger [" + debugger.name + "] Version [" + debugger.versionFlags + "; " + debugger.versionPattern + "]\n"); // NOI18N
            return buf.toString();
        }
    }

    /**
     * class package-local for testing only
     */
    static class Tool {

        String name;
        String versionFlags;
        String versionPattern;
        boolean skipSearch;
        List<AlternativePath> alternativePath;
    }

    static class Alternative implements AlternativePath {
        String path;
        AlternativePath.PathKind kind;
        Alternative(String path, AlternativePath.PathKind kind){
            this.path = path;
            this.kind = kind;
        }

        public String getPath() {
            return path;
        }

        public PathKind getKind() {
            return kind;
        }
    }

    /**
     * class package-local for testing only
     */
    static final class Compiler extends Tool {

        String pathPattern;
        String existFolder;
        String includeFlags;
        String includeOutputParser;
        String removeIncludePathPrefix;
        String removeIncludeOutputPrefix;
        String userIncludeFlag;
        String macrosFlags;
        String macrosOutputParser;
        String userMacroFlag;
        List<PredefinedMacro> predefinedMacros;
        String outputObjectFileFlags;
        String dependencyGenerationFlags;
        String precompiledHeaderFlags;
        String precompiledHeaderSuffix;
        boolean precompiledHeaderSuffixAppend;
        DevelopmentMode developmentMode = new DevelopmentMode();
        WarningLevel warningLevel = new WarningLevel();
        Architecture architecture = new Architecture();
        String strip;
        MultiThreading multithreading = new MultiThreading();
        Standard standard = new Standard();
        LanguageExtension languageExtension = new LanguageExtension();
        Library library = new Library();

        public boolean isValid() {
            return name != null && name.length() > 0;
        }
    }

    /**
     * class package-local for testing only
     */
    static final class Scanner {

        List<ErrorPattern> patterns = new ArrayList<ErrorPattern>();
        String changeDirectoryPattern;
        String enterDirectoryPattern;
        String leaveDirectoryPattern;
        String stackHeaderPattern;
        String stackNextPattern;
    }

    /**
     * class package-local for testing only
     */
    static final class ErrorPattern {

        String pattern;
        String severity;
        String language;
    }

    /**
     * class package-local for testing only
     */
    static final class Linker {

        String library_prefix;
        String librarySearchFlag;
        String dynamicLibrarySearchFlag;
        String libraryFlag;
        String PICFlag;
        String staticLibraryFlag;
        String dynamicLibraryFlag;
        String dynamicLibraryBasicFlag;
        String outputFileFlag;
    }

    /**
     * class package-local for testing only
     */
    static final class Make extends Tool {

        String dependencySupportCode;
    }

    /**
     * class package-local for testing only
     */
    static final class Debugger extends Tool {
    }

    /**
     * class package-local for testing only
     */
    static final class DevelopmentMode {

        String fast_build;
        String debug;
        String performance_debug;
        String test_coverage;
        String diagnosable_release;
        String release;
        String performance_release;
        int default_selection = 0;

        @Override
        public String toString() {
            return "[" + fast_build + ";" + debug + ";" + performance_debug + ";" + test_coverage + ";" + // NOI18N
                    diagnosable_release + ";" + release + ";" + performance_release + "] default " + default_selection; // NOI18N
        }

        public boolean isValid() {
            return fast_build != null && debug != null && performance_debug != null && test_coverage != null &&
                    diagnosable_release != null && release != null && performance_release != null;
        }

        public String[] values() {
            if (isValid()) {
                return new String[]{fast_build, debug, performance_debug, test_coverage,
                            diagnosable_release, release, performance_release};
            }
            return null;
        }
    }

    /**
     * class package-local for testing only
     */
    static final class WarningLevel {

        String no_warnings;
        String default_level;
        String more_warnings;
        String warning2error;
        int default_selection = 0;

        @Override
        public String toString() {
            return "[" + no_warnings + ";" + default_level + ";" + more_warnings + ";" + warning2error + "] default " + default_selection; // NOI18N
        }

        public boolean isValid() {
            return no_warnings != null && default_level != null && more_warnings != null && warning2error != null;
        }

        public String[] values() {
            if (isValid()) {
                return new String[]{no_warnings, default_level, more_warnings, warning2error};
            }
            return null;
        }
    }

    /**
     * class package-local for testing only
     */
    static final class Architecture {

        String default_architecture;
        String bits_32;
        String bits_64;
        int default_selection = 0;

        @Override
        public String toString() {
            return "[" + default_architecture + ";" + bits_32 + ";" + bits_64 + "] default " + default_selection; // NOI18N
        }

        public boolean isValid() {
            return default_architecture != null && bits_32 != null && bits_64 != null;
        }

        public String[] values() {
            if (isValid()) {
                return new String[]{default_architecture, bits_32, bits_64};
            }
            return null;
        }
    }

    /**
     * class package-local for testing only
     */
    static final class MultiThreading {

        String none;
        String safe;
        String automatic;
        String open_mp;
        int default_selection = 0;

        @Override
        public String toString() {
            return "[" + none + ";" + safe + ";" + automatic + ";" + open_mp + "] default " + default_selection; // NOI18N
        }

        public boolean isValid() {
            return none != null && safe != null && automatic != null && open_mp != null;
        }

        public String[] values() {
            if (isValid()) {
                return new String[]{none, safe, automatic, open_mp};
            }
            return null;
        }
    }

    /**
     * class package-local for testing only
     */
    static final class Standard {

        String old;
        String legacy;
        String default_standard;
        String modern;
        int default_selection = 0;

        @Override
        public String toString() {
            return "[" + old + ";" + legacy + ";" + default_standard + ";" + modern + "] default " + default_selection; // NOI18N
        }

        public boolean isValid() {
            return old != null && legacy != null && default_standard != null && modern != null;
        }

        public String[] values() {
            if (isValid()) {
                return new String[]{old, legacy, default_standard, modern};
            }
            return null;
        }
    }

    /**
     * class package-local for testing only
     */
    static final class LanguageExtension {

        String none;
        String default_extension;
        String all;
        int default_selection = 0;

        @Override
        public String toString() {
            return "[" + none + ";" + default_extension + ";" + all + "] default " + default_selection; // NOI18N
        }

        public boolean isValid() {
            return none != null && default_extension != null && all != null;
        }

        public String[] values() {
            if (isValid()) {
                return new String[]{none, default_extension, all};
            }
            return null;
        }
    }

    /**
     * class package-local for testing only
     */
    static final class Library {

        String none;
        String runtime;
        String classic;
        String binary_standard;
        String conforming_standard;
        int default_selection = 0;

        @Override
        public String toString() {
            return "[" + none + ";" + runtime + ";" + classic + ";" + binary_standard + ";" + conforming_standard + "] default " + default_selection; // NOI18N
        }

        public boolean isValid() {
            return none != null && runtime != null && classic != null && binary_standard != null && conforming_standard != null;
        }

        public String[] values() {
            if (isValid()) {
                return new String[]{none, runtime, classic, binary_standard, conforming_standard};
            }
            return null;
        }
    }

    private static final class SAXHandler extends DefaultHandler {

        private String path;
        private CompilerVendor v;
        private boolean isScanerOverrided = false;
        private int version = 1;
        private Map<String, String> cache;

        private SAXHandler(CompilerVendor v, Map<String, String> cache) {
            this.v = v;
            this.cache = cache;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (path != null) {
                if (path.equals(qName)) {
                    path = null;
                } else {
                    path = path.substring(0, path.length() - qName.length() - 1);
                }
            }
        }

        private String getValue(org.xml.sax.Attributes attributes, String key){
            String res = attributes.getValue(key);
            if (res != null) {
                String c = cache.get(res);
                if (c == null){
                    cache.put(res, res);
                } else {
                    res = c;
                }
            }
            return res;
        }

        private String getValue(String res){
            String c = cache.get(res);
            if (c == null){
                cache.put(res, res);
            } else {
                res = c;
            }
            return res;
        }

        @Override
        public void startElement(String uri, String lname, String name, org.xml.sax.Attributes attributes) throws SAXException {
            super.startElement(uri, lname, name, attributes);
            if (path == null) {
                path = name;
            } else {
                path += "." + name; // NOI18N
            }
            if (path.equals("toolchaindefinition")) { // NOI18N
                String xmlns = attributes.getValue("xmlns"); // NOI18N
                if (xmlns != null) {
                    int lastSlash = xmlns.lastIndexOf('/'); // NOI18N
                    if (lastSlash >= 0 && (lastSlash + 1 < xmlns.length())) {
                        String versionStr = xmlns.substring(lastSlash + 1);
                        if (versionStr.length() > 0) {
                            try {
                                version = Integer.parseInt(versionStr);
                            } catch (NumberFormatException ex) {
                                // skip
                                if (TRACE) {
                                    System.err.println("Incorrect version information:" + xmlns);
                                } // NOI18N
                            }
                        }
                    } else {
                        if (TRACE) {
                            System.err.println("Incorrect version information:" + xmlns);
                        } // NOI18N
                    }
                }
                return;
            } else if (path.endsWith(".toolchain")) { // NOI18N
                v.toolChainName = getValue(attributes, "name"); // NOI18N
                v.toolChainDisplay = getValue(attributes, "display"); // NOI18N
                v.family = getValue(attributes, "family"); // NOI18N
                v.qmakespec = getValue(attributes, "qmakespec"); // NOI18N
                return;
            } else if (path.endsWith(".platforms")) { // NOI18N
                v.platforms = getValue(attributes, "stringvalue"); // NOI18N
                return;
            } else if (path.endsWith(".drive_letter_prefix")) { // NOI18N
                v.driveLetterPrefix = getValue(attributes, "stringvalue"); // NOI18N
                return;
            } else if (path.endsWith(".makefile_writer")) { // NOI18N
                v.makefileWriter = getValue(attributes, "class"); // NOI18N
                return;
            } else if (path.endsWith(".base_folder")) { // NOI18N
                v.baseFolderKey = getValue(attributes, "regestry"); // NOI18N
                v.baseFolderPattern = getValue(attributes, "pattern"); // NOI18N
                v.baseFolderSuffix = getValue(attributes, "suffix"); // NOI18N
                v.baseFolderPathPattern = getValue(attributes, "path_patern"); // NOI18N
                return;
            } else if (path.endsWith(".command_folder")) { // NOI18N
                v.commandFolderKey = getValue(attributes, "regestry"); // NOI18N
                v.commandFolderPattern = getValue(attributes, "pattern"); // NOI18N
                v.commandFolderSuffix = getValue(attributes, "suffix"); // NOI18N
                v.commandFolderPathPattern = getValue(attributes, "path_patern"); // NOI18N
                return;
            } else if (path.indexOf(".default_locations.") > 0) { // NOI18N
                if (path.endsWith(".platform")) { // NOI18N
                    String os_attr = getValue(attributes, "os"); // NOI18N
                    String dir_attr = getValue(attributes, "directory"); // NOI18N
                    if (os_attr != null && dir_attr != null) {
                        if (v.default_locations == null) {
                            v.default_locations = new HashMap<String, String>();
                        }
                        v.default_locations.put(os_attr, dir_attr);
                    }
                }
                return;
            }
            if (path.indexOf(".linker.") > 0) { // NOI18N
                Linker l = v.linker;
                if (path.endsWith(".library_prefix")) { // NOI18N
                    l.library_prefix = getValue(attributes, "stringvalue"); // NOI18N
                    return;
                } else if (path.endsWith(".library_search")) { // NOI18N
                    l.librarySearchFlag = getValue(attributes, "flags"); // NOI18N
                    return;
                } else if (path.endsWith(".dynamic_library_search")) { // NOI18N
                    l.dynamicLibrarySearchFlag = getValue(attributes, "flags"); // NOI18N
                    return;
                } else if (path.endsWith(".library_flag")) { // NOI18N
                    l.libraryFlag = getValue(attributes, "flags"); // NOI18N
                    return;
                } else if (path.endsWith(".PIC")) { // NOI18N
                    l.PICFlag = getValue(attributes, "flags"); // NOI18N
                    return;
                } else if (path.endsWith(".static_library")) { // NOI18N
                    l.staticLibraryFlag = getValue(attributes, "flags"); // NOI18N
                    return;
                } else if (path.endsWith(".dynamic_library")) { // NOI18N
                    l.dynamicLibraryFlag = getValue(attributes, "flags"); // NOI18N
                    return;
                } else if (path.endsWith(".dynamic_library_basic")) { // NOI18N
                    l.dynamicLibraryBasicFlag = getValue(attributes, "flags"); // NOI18N
                    return;
                } else if (path.endsWith(".output_file")) { // NOI18N
                    l.outputFileFlag = getValue(attributes, "flags"); // NOI18N
                    return;
                }
                return;
            }
            if (path.indexOf(".make.") > 0) { // NOI18N
                Make m = v.make;
                if (path.endsWith(".tool")) { // NOI18N
                    m.name = getValue(attributes, "name"); // NOI18N
                    m.skipSearch = "true".equals(getValue(attributes, "skip")); // NOI18N
                } else if (path.endsWith(".version")) { // NOI18N
                    m.versionFlags = getValue(attributes, "flags"); // NOI18N
                    m.versionPattern = getValue(attributes, "pattern"); // NOI18N
                } else if (path.endsWith(".alternative_path")) { // NOI18N
                    m.alternativePath = new ArrayList<AlternativePath>();
                } else if (path.endsWith(".dependency_support")) { // NOI18N
                    m.dependencySupportCode = getValue(getValue(attributes, "code").replace("\\n", "\n")); // NOI18N
                } else if (checkAlternativePath(attributes, m.alternativePath)) {
                }
                return;
            }
            if (path.indexOf(".debugger.") > 0) { // NOI18N
                Debugger d = v.debugger;
                if (path.endsWith(".tool")) { // NOI18N
                    d.name = getValue(attributes, "name"); // NOI18N
                    d.skipSearch = "true".equals(getValue(attributes, "skip")); // NOI18N
                } else if (path.endsWith(".version")) { // NOI18N
                    d.versionFlags = getValue(attributes, "flags"); // NOI18N
                    d.versionPattern = getValue(attributes, "pattern"); // NOI18N
                } else if (path.endsWith(".alternative_path")) { // NOI18N
                    d.alternativePath = new ArrayList<AlternativePath>();
                } else if (checkAlternativePath(attributes, d.alternativePath)) {
                }
                return;
            }
            if (path.indexOf(".scanner.") > 0) { // NOI18N
                if (!isScanerOverrided) {
                    v.scanner = new Scanner();
                    isScanerOverrided = true;
                }
                Scanner s = v.scanner;
                if (path.endsWith(".error")) { // NOI18N
                    ErrorPattern e = new ErrorPattern();
                    s.patterns.add(e);
                    e.severity = "error"; // NOI18N
                    e.pattern = getValue(attributes, "pattern"); // NOI18N
                    e.language = getValue(attributes, "language"); // NOI18N
                } else if (path.endsWith(".warning")) { // NOI18N
                    ErrorPattern e = new ErrorPattern();
                    s.patterns.add(e);
                    e.severity = "warning"; // NOI18N
                    e.pattern = getValue(attributes, "pattern"); // NOI18N
                    e.language = getValue(attributes, "language"); // NOI18N
                } else if (path.endsWith(".change_directory")) { // NOI18N
                    s.changeDirectoryPattern = getValue(attributes, "pattern"); // NOI18N
                } else if (path.endsWith(".enter_directory")) { // NOI18N
                    s.enterDirectoryPattern = getValue(attributes, "pattern"); // NOI18N
                } else if (path.endsWith(".leave_directory")) { // NOI18N
                    s.leaveDirectoryPattern = getValue(attributes, "pattern"); // NOI18N
                } else if (path.endsWith(".stack_header")) { // NOI18N
                    s.stackHeaderPattern = getValue(attributes, "pattern"); // NOI18N
                } else if (path.endsWith(".stack_next")) { // NOI18N
                    s.stackNextPattern = getValue(attributes, "pattern"); // NOI18N
                }
                return;
            }
            Compiler c;
            if (path.indexOf(".c.") > 0) { // NOI18N
                c = v.c;
            } else if (path.indexOf(".cpp.") > 0) { // NOI18N
                c = v.cpp;
            } else if (path.indexOf(".fortran.") > 0) { // NOI18N
                c = v.fortran;
            } else if (path.indexOf(".assembler.") > 0) { // NOI18N
                c = v.assembler;
            } else {
                return;
            }
            if (path.endsWith(".compiler")) { // NOI18N
                c.name = getValue(attributes, "name"); // NOI18N
                c.skipSearch = "true".equals(getValue(attributes, "skip")); // NOI18N
                return;
            } else if (path.endsWith(".recognizer")) { // NOI18N
                c.pathPattern = getValue(attributes, "pattern"); // NOI18N
                c.existFolder = getValue(attributes, "or_exist_folder"); // NOI18N
                return;
            } else if (path.endsWith(".version")) { // NOI18N
                c.versionPattern = getValue(attributes, "pattern"); // NOI18N
                c.versionFlags = getValue(attributes, "flags"); // NOI18N
                return;
            } else if (path.endsWith(".alternative_path")) { // NOI18N
                c.alternativePath = new ArrayList<AlternativePath>();
                return;
            } else if (checkAlternativePath(attributes, c.alternativePath)) {
                return;
            } else if (path.endsWith(".system_macros.macro")) { // NOI18N
                if (c.predefinedMacros == null) {
                    c.predefinedMacros = new ArrayList<PredefinedMacro>();
                }
                PredefinedMacro m = new PredefinedMacroImpl(getValue(attributes, "stringvalue"), getValue(attributes, "flags")); // NOI18N
                c.predefinedMacros.add(m);
                return;
            }
            String flags = getValue(attributes, "flags"); // NOI18N
            if (flags == null) {
                return;
            }
            boolean isDefault = "true".equals(getValue(attributes, "default")); // NOI18N
            if (path.endsWith(".system_include_paths")) { // NOI18N
                c.includeFlags = flags;
                c.includeOutputParser = getValue(attributes, "parser"); // NOI18N
                c.removeIncludePathPrefix = getValue(attributes, "remove_in_path"); // NOI18N
                c.removeIncludeOutputPrefix = getValue(attributes, "remove_in_output"); // NOI18N
            } else if (path.endsWith(".user_include")) { // NOI18N
                c.userIncludeFlag = flags;
            } else if (path.endsWith(".system_macros")) { // NOI18N
                c.macrosFlags = flags;
                c.macrosOutputParser = getValue(attributes, "parser"); // NOI18N
            } else if (path.endsWith(".user_macro")) { // NOI18N
                c.userMacroFlag = flags;
            } else if (path.indexOf(".development_mode.") > 0) { // NOI18N
                DevelopmentMode d = c.developmentMode;
                if (path.endsWith(".fast_build")) { // NOI18N
                    d.fast_build = flags;
                    if (isDefault) {
                        d.default_selection = 0;
                    }
                } else if (path.endsWith(".debug")) { // NOI18N
                    d.debug = flags;
                    if (isDefault) {
                        d.default_selection = 1;
                    }
                } else if (path.endsWith(".performance_debug")) { // NOI18N
                    d.performance_debug = flags;
                    if (isDefault) {
                        d.default_selection = 2;
                    }
                } else if (path.endsWith(".test_coverage")) { // NOI18N
                    d.test_coverage = flags;
                    if (isDefault) {
                        d.default_selection = 3;
                    }
                } else if (path.endsWith(".diagnosable_release")) { // NOI18N
                    d.diagnosable_release = flags;
                    if (isDefault) {
                        d.default_selection = 4;
                    }
                } else if (path.endsWith(".release")) { // NOI18N
                    d.release = flags;
                    if (isDefault) {
                        d.default_selection = 5;
                    }
                } else if (path.endsWith(".performance_release")) { // NOI18N
                    d.performance_release = flags;
                    if (isDefault) {
                        d.default_selection = 6;
                    }
                }
            } else if (path.indexOf(".warning_level.") > 0) { // NOI18N
                WarningLevel w = c.warningLevel;
                if (path.endsWith(".no_warnings")) { // NOI18N
                    w.no_warnings = flags;
                    if (isDefault) {
                        w.default_selection = 0;
                    }
                } else if (path.endsWith(".default")) { // NOI18N
                    w.default_level = flags;
                    if (isDefault) {
                        w.default_selection = 1;
                    }
                } else if (path.endsWith(".more_warnings")) { // NOI18N
                    w.more_warnings = flags;
                    if (isDefault) {
                        w.default_selection = 2;
                    }
                } else if (path.endsWith(".warning2error")) { // NOI18N
                    w.warning2error = flags;
                    if (isDefault) {
                        w.default_selection = 3;
                    }
                }
            } else if (path.indexOf(".architecture.") > 0) { // NOI18N
                Architecture a = c.architecture;
                if (path.endsWith(".default")) { // NOI18N
                    a.default_architecture = flags;
                    if (isDefault) {
                        a.default_selection = 0;
                    }
                } else if (path.endsWith(".bits_32")) { // NOI18N
                    a.bits_32 = flags;
                    if (isDefault) {
                        a.default_selection = 1;
                    }
                } else if (path.endsWith(".bits_64")) { // NOI18N
                    a.bits_64 = flags;
                    if (isDefault) {
                        a.default_selection = 2;
                    }
                }
            } else if (path.endsWith(".strip")) { // NOI18N
                c.strip = flags;
            } else if (path.endsWith(".output_object_file")) { // NOI18N
                c.outputObjectFileFlags = flags;
            } else if (path.endsWith(".dependency_generation")) { // NOI18N
                c.dependencyGenerationFlags = flags;
            } else if (path.endsWith(".precompiled_header")) { // NOI18N
                c.precompiledHeaderFlags = flags;
                c.precompiledHeaderSuffix = getValue(attributes, "suffix"); // NOI18N
                c.precompiledHeaderSuffixAppend = Boolean.valueOf(getValue(attributes, "append")); // NOI18N
            } else if (path.indexOf(".multithreading.") > 0) { // NOI18N
                MultiThreading m = c.multithreading;
                if (path.endsWith(".none")) { // NOI18N
                    m.none = flags;
                    if (isDefault) {
                        m.default_selection = 0;
                    }
                } else if (path.endsWith(".safe")) { // NOI18N
                    m.safe = flags;
                    if (isDefault) {
                        m.default_selection = 1;
                    }
                } else if (path.endsWith(".automatic")) { // NOI18N
                    m.automatic = flags;
                    if (isDefault) {
                        m.default_selection = 2;
                    }
                } else if (path.endsWith(".open_mp")) { // NOI18N
                    m.open_mp = flags;
                    if (isDefault) {
                        m.default_selection = 3;
                    }
                }
            } else if (path.indexOf(".standard.") > 0) { // NOI18N
                Standard s = c.standard;
                if (path.endsWith(".old")) { // NOI18N
                    s.old = flags;
                    if (isDefault) {
                        s.default_selection = 0;
                    }
                } else if (path.endsWith(".legacy")) { // NOI18N
                    s.legacy = flags;
                    if (isDefault) {
                        s.default_selection = 1;
                    }
                } else if (path.endsWith(".default")) { // NOI18N
                    s.default_standard = flags;
                    if (isDefault) {
                        s.default_selection = 2;
                    }
                } else if (path.endsWith(".modern")) { // NOI18N
                    s.modern = flags;
                    if (isDefault) {
                        s.default_selection = 3;
                    }
                }
            } else if (path.indexOf(".language_extension.") > 0) { // NOI18N
                LanguageExtension e = c.languageExtension;
                if (path.endsWith(".none")) { // NOI18N
                    e.none = flags;
                    if (isDefault) {
                        e.default_selection = 0;
                    }
                } else if (path.endsWith(".default")) { // NOI18N
                    e.default_extension = flags;
                    if (isDefault) {
                        e.default_selection = 1;
                    }
                } else if (path.endsWith(".all")) { // NOI18N
                    e.all = flags;
                    if (isDefault) {
                        e.default_selection = 2;
                    }
                }
            } else if (path.indexOf(".library.") > 0) { // NOI18N
                Library l = c.library;
                if (path.endsWith(".none")) { // NOI18N
                    l.none = flags;
                    if (isDefault) {
                        l.default_selection = 0;
                    }
                } else if (path.endsWith(".runtime")) { // NOI18N
                    l.runtime = flags;
                    if (isDefault) {
                        l.default_selection = 1;
                    }
                } else if (path.endsWith(".classic")) { // NOI18N
                    l.classic = flags;
                    if (isDefault) {
                        l.default_selection = 2;
                    }
                } else if (path.endsWith(".binary_standard")) { // NOI18N
                    l.binary_standard = flags;
                    if (isDefault) {
                        l.default_selection = 3;
                    }
                } else if (path.endsWith(".conforming_standard")) { // NOI18N
                    l.conforming_standard = flags;
                    if (isDefault) {
                        l.default_selection = 4;
                    }
                }
            }
        }

        private boolean checkAlternativePath(org.xml.sax.Attributes attributes, List<AlternativePath> alternativePath){
            if (path.endsWith(".alternative_path.path") && alternativePath != null) { // NOI18N
                String s = getValue(attributes, "directory"); // NOI18N
                if (s != null) {
                    alternativePath.add(new Alternative(s, AlternativePath.PathKind.PATH));
                    return true;
                }
                s = getValue(attributes, "toolchain_family"); // NOI18N
                if (s != null) {
                    alternativePath.add(new Alternative(s, AlternativePath.PathKind.TOOL_FAMILY));
                    return true;
                }
                s = getValue(attributes, "toolchain_name"); // NOI18N
                if (s != null) {
                    alternativePath.add(new Alternative(s, AlternativePath.PathKind.TOOL_NAME));
                    return true;
                }
                return true;
            }
            return false;
        }
    }
    /**
     * class package-local for testing only
     */
    static final class ToolchainDescriptorImpl implements ToolchainDescriptor {

        CompilerVendor v;
        private CompilerDescriptor c;
        private CompilerDescriptor cpp;
        private CompilerDescriptor fortran;
        private CompilerDescriptor assembler;
        private LinkerDescriptor linker;
        private ScannerDescriptor scanner;
        private MakeDescriptor make;
        private DebuggerDescriptor debugger;

        private ToolchainDescriptorImpl(CompilerVendor v) {
            this.v = v;
        }

        public String getFileName() {
            return v.toolChainFileName;
        }

        public String getName() {
            return v.toolChainName;
        }

        public String getDisplayName() {
            return v.toolChainDisplay;
        }

        public String[] getFamily() {
            if (v.family != null && v.family.length() > 0) {
                return v.family.split(","); // NOI18N
            }
            return new String[]{};
        }

        public String[] getPlatforms() {
            if (v.platforms != null && v.platforms.length() > 0) {
                return v.platforms.split(","); // NOI18N
            }
            return new String[]{};
        }

        public String getDriveLetterPrefix() {
            return v.driveLetterPrefix;
        }

        public String getMakefileWriter() {
            return v.makefileWriter;
        }

        public CompilerDescriptor getC() {
            if (c == null && v.c.isValid()) {
                c = new CompilerDescriptorImpl(v.c);
            }
            return c;
        }

        public String getBaseFolderKey() {
            return v.baseFolderKey;
        }

        public String getBaseFolderPattern() {
            return v.baseFolderPattern;
        }

        public String getBaseFolderSuffix() {
            return v.baseFolderSuffix;
        }

        public String getBaseFolderPathPattern() {
            return v.baseFolderPathPattern;
        }

        public String getCommandFolderKey() {
            return v.commandFolderKey;
        }

        public String getCommandFolderPattern() {
            return v.commandFolderPattern;
        }

        public String getCommandFolderSuffix() {
            return v.commandFolderSuffix;
        }

        public String getCommandFolderPathPattern() {
            return v.commandFolderPathPattern;
        }

        public String getQmakeSpec() {
            return v.qmakespec;
        }

        public CompilerDescriptor getCpp() {
            if (cpp == null && v.cpp.isValid()) {
                cpp = new CompilerDescriptorImpl(v.cpp);
            }
            return cpp;
        }

        public CompilerDescriptor getFortran() {
            if (fortran == null && v.fortran.isValid()) {
                fortran = new CompilerDescriptorImpl(v.fortran);
            }
            return fortran;
        }

        public CompilerDescriptor getAssembler() {
            if (assembler == null && v.assembler.isValid()) {
                assembler = new CompilerDescriptorImpl(v.assembler);
            }
            return assembler;
        }

        public ScannerDescriptor getScanner() {
            if (scanner == null) {
                scanner = new ScannerDescriptorImpl(v.scanner);
            }
            return scanner;
        }

        public LinkerDescriptor getLinker() {
            if (linker == null) {
                linker = new LinkerDescriptorImpl(v.linker);
            }
            return linker;
        }

        public MakeDescriptor getMake() {
            if (make == null) {
                make = new MakeDescriptorImpl(v.make);
            }
            return make;
        }

        public Map<String, String> getDefaultLocations() {
            return v.default_locations;
        }

        public DebuggerDescriptor getDebugger() {
            if (debugger == null) {
                debugger = new DebuggerDescriptorImpl(v.debugger);
            }
            return debugger;
        }

        @Override
        public String toString() {
            return v.toolChainName + "/" + v.family + "/" + v.platforms; // NOI18N
        }
    }

    private static class ToolDescriptorImpl<T extends Tool> implements ToolDescriptor {

        protected T tool;

        public ToolDescriptorImpl(T tool) {
            this.tool = tool;
        }

        public String[] getNames() {
            if (tool.name != null && tool.name.length() > 0) {
                return tool.name.split(","); // NOI18N
            }
            return new String[]{};
        }

        public String getVersionFlags() {
            return tool.versionFlags;
        }

        public String getVersionPattern() {
            return tool.versionPattern;
        }

        public AlternativePath[] getAlternativePath() {
            if (tool.alternativePath != null) {
                return tool.alternativePath.toArray(new AlternativePath[tool.alternativePath.size()] );
            }
            return null;
        }

        public boolean skipSearch() {
            return tool.skipSearch;
        }
    }

    private static final class CompilerDescriptorImpl
            extends ToolDescriptorImpl<Compiler> implements CompilerDescriptor {

        private CompilerDescriptorImpl(Compiler compiler) {
            super(compiler);
        }

        public String getPathPattern() {
            return tool.pathPattern;
        }

        public String getExistFolder() {
            return tool.existFolder;
        }

        public String getIncludeFlags() {
            return tool.includeFlags;
        }

        public String getIncludeParser() {
            return tool.includeOutputParser;
        }

        public String getRemoveIncludePathPrefix() {
            return tool.removeIncludePathPrefix;
        }

        public String getRemoveIncludeOutputPrefix() {
            return tool.removeIncludeOutputPrefix;
        }

        public String getUserIncludeFlag() {
            return tool.userIncludeFlag;
        }

        public String getMacroFlags() {
            return tool.macrosFlags;
        }

        public String getMacroParser() {
            return tool.macrosOutputParser;
        }

        public List<PredefinedMacro> getPredefinedMacros() {
            return tool.predefinedMacros;
        }

        public String getUserMacroFlag() {
            return tool.userMacroFlag;
        }

        public String[] getDevelopmentModeFlags() {
            return tool.developmentMode.values();
        }

        public String[] getWarningLevelFlags() {
            return tool.warningLevel.values();
        }

        public String[] getArchitectureFlags() {
            return tool.architecture.values();
        }

        public String getStripFlag() {
            return tool.strip;
        }

        public String[] getMultithreadingFlags() {
            return tool.multithreading.values();
        }

        public String[] getStandardFlags() {
            return tool.standard.values();
        }

        public String[] getLanguageExtensionFlags() {
            return tool.languageExtension.values();
        }

        public String[] getLibraryFlags() {
            return tool.library.values();
        }

        public String getOutputObjectFileFlags() {
            return tool.outputObjectFileFlags;
        }

        public String getDependencyGenerationFlags() {
            return tool.dependencyGenerationFlags;
        }

        public String getPrecompiledHeaderFlags() {
            return tool.precompiledHeaderFlags;
        }

        public String getPrecompiledHeaderSuffix() {
            return tool.precompiledHeaderSuffix;
        }

        public boolean getPrecompiledHeaderSuffixAppend() {
            return tool.precompiledHeaderSuffixAppend;
        }

    }
    
    private static final class PredefinedMacroImpl implements PredefinedMacro {
        String macro;
        String flags;

        PredefinedMacroImpl(String macro, String flags){
            this.macro = macro;
            this.flags = flags;
        }

        public String getMacro() {
            return macro;
        }

        public String getFlags() {
            return flags;
        }

    }

    private static final class ScannerDescriptorImpl implements ScannerDescriptor {

        private Scanner s;
        private List<ScannerPattern> patterns;

        private ScannerDescriptorImpl(Scanner s) {
            this.s = s;
        }

        public List<ScannerPattern> getPatterns() {
            if (patterns == null) {
                patterns = new ArrayList<ScannerPattern>();
                for (ErrorPattern p : s.patterns) {
                    patterns.add(new ScannerPatternImpl(p));
                }
            }
            return patterns;
        }

        public String getChangeDirectoryPattern() {
            return s.changeDirectoryPattern;
        }

        public String getEnterDirectoryPattern() {
            return s.enterDirectoryPattern;
        }

        public String getLeaveDirectoryPattern() {
            return s.leaveDirectoryPattern;
        }

        public String getStackHeaderPattern() {
            return s.stackHeaderPattern;
        }

        public String getStackNextPattern() {
            return s.stackNextPattern;
        }
    }

    private static final class ScannerPatternImpl implements ScannerPattern {

        private ErrorPattern e;

        private ScannerPatternImpl(ErrorPattern e) {
            this.e = e;
        }

        public String getPattern() {
            return e.pattern;
        }

        public String getSeverity() {
            return e.severity;
        }

        public String getLanguage() {
            return e.language;
        }
    }

    private static final class LinkerDescriptorImpl implements LinkerDescriptor {

        private Linker l;

        private LinkerDescriptorImpl(Linker l) {
            this.l = l;
        }

        public String getLibrarySearchFlag() {
            return l.librarySearchFlag;
        }

        public String getDynamicLibrarySearchFlag() {
            return l.dynamicLibrarySearchFlag;
        }

        public String getLibraryFlag() {
            return l.libraryFlag;
        }

        public String getLibraryPrefix() {
            return l.library_prefix;
        }

        public String getPICFlag() {
            return l.PICFlag;
        }

        public String getStaticLibraryFlag() {
            return l.staticLibraryFlag;
        }

        public String getDynamicLibraryFlag() {
            return l.dynamicLibraryFlag;
        }

        public String getDynamicLibraryBasicFlag() {
            return l.dynamicLibraryBasicFlag;
        }

        public String getOutputFileFlag() {
            return l.outputFileFlag;
        }
    }

    private static final class MakeDescriptorImpl
            extends ToolDescriptorImpl<Make> implements MakeDescriptor {

        private MakeDescriptorImpl(Make make) {
            super(make);
        }

        public String getDependencySupportCode() {
            return tool.dependencySupportCode;
        }
    }

    private static final class DebuggerDescriptorImpl
            extends ToolDescriptorImpl<Debugger> implements DebuggerDescriptor {

        public DebuggerDescriptorImpl(Debugger debugger) {
            super(debugger);
        }
    }
}
