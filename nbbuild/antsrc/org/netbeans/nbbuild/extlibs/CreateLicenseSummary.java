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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.nbbuild.extlibs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.netbeans.nbbuild.JUnitReportWriter;

/**
 * Creates a list of external binaries and their licenses.
 */
public class CreateLicenseSummary extends Task {

    private File nball;
    public void setNball(File nball) {
        this.nball = nball;
    }

    private File build;
    public void setBuild(File build) {
        this.build = build;
    }

    private File summary;
    public void setSummary(File summary) {
        this.summary = summary;
    }

    private File reportFile;
    public void setReport(File report) {
        this.reportFile = report;
    }

    private Map<String,String> pseudoTests;

    public @Override void execute() throws BuildException {
        pseudoTests = new LinkedHashMap<String,String>();
        try {
            Map<Long,Map<String,String>> crc2License = findCrc2LicenseHeaderMapping();
            Map<String,Map<String,String>> binaries2LicenseHeaders = new TreeMap<String,Map<String,String>>();
            StringBuilder testBinariesAreUnique = new StringBuilder();
            List<String> ignoredPatterns = VerifyLibsAndLicenses.loadPatterns("ignored-binary-overlaps");
            findBinaries(build, binaries2LicenseHeaders, crc2License, new HashMap<Long,String>(), "", testBinariesAreUnique, ignoredPatterns);
            pseudoTests.put("testBinariesAreUnique", testBinariesAreUnique.length() > 0 ? "Some binaries are duplicated (edit nbbuild/antsrc/org/netbeans/nbbuild/extlibs/ignored-binary-overlaps as needed)" + testBinariesAreUnique : null);
            OutputStream os = new FileOutputStream(summary);
            try {
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
                pw.println("DO NOT TRANSLATE OR LOCALIZE.");
                pw.println();
                Set<String> licenseNames = new TreeSet<String>();
                pw.printf("%-60s %10s %-40s %s\n", "NAME", "SIZE", "SHA-1 HASH", "LICENSE");
                for (Map.Entry<String,Map<String,String>> entry : binaries2LicenseHeaders.entrySet()) {
                    String binary = entry.getKey();
                    File f = new File(build, binary.replace('/', File.separatorChar));
                    MessageDigest digest;
                    try {
                        digest = MessageDigest.getInstance("SHA-1");
                    } catch (NoSuchAlgorithmException x) {
                        throw new BuildException(x, getLocation());
                    }
                    FileInputStream is = new FileInputStream(f);
                    try {
                        digest.update(is.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, f.length()));
                    } finally {
                        is.close();
                    }
                    Map<String,String> headers = entry.getValue();
                    pw.printf("%-60s %10d %040X %s\n", binary, f.length(), new BigInteger(1, digest.digest()), getMaybeMissing(headers, "License"));
                    String license = headers.get("License");
                    if (license != null) {
                        licenseNames.add(license);
                    }
                }
                String[] otherHeaders = {"Name", "Version", "Description", "OSR", "Origin"};
                Map<Map<String,String>,Set<String>> licenseHeaders2Binaries = new LinkedHashMap<Map<String,String>,Set<String>>();
                for (Map.Entry<String,Map<String,String>> entry : binaries2LicenseHeaders.entrySet()) {
                    Map<String,String> headers = new HashMap<String,String>(entry.getValue());
                    headers.keySet().retainAll(Arrays.asList(otherHeaders));
                    Set<String> binaries = licenseHeaders2Binaries.get(headers);
                    if (binaries == null) {
                        binaries = new TreeSet<String>();
                        licenseHeaders2Binaries.put(headers, binaries);
                    }
                    binaries.add(entry.getKey());
                }
                for (Map.Entry<Map<String,String>,Set<String>> entry : licenseHeaders2Binaries.entrySet()) {
                    pw.println();
                    for (String binary : entry.getValue()) {
                        pw.println(binary);
                    }
                    for (String header : otherHeaders) {
                        pw.printf("%s: %s\n", header, getMaybeMissing(entry.getKey(), header));
                    }
                }
                File licenses = new File(new File(nball, "nbbuild"), "licenses");
                for (String licenseName : licenseNames) {
                    if (licenseName == null) {
                        continue;
                    }
                    File license = new File(licenses, licenseName);
                    if (!license.isFile()) {
                        continue;
                    }
                    pw.println();
                    pw.println("=========== " + licenseName + " ===========");
                    InputStream is = new FileInputStream(license);
                    try {
                        BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                        String line;
                        while ((line = r.readLine()) != null) {
                            pw.println(line);
                        }
                    } finally {
                        is.close();
                    }
                }
                pw.flush();
            } finally {
                os.close();
            }
            log(summary + ": written");
        } catch (IOException x) {
            throw new BuildException(x, getLocation());
        }
        JUnitReportWriter.writeReport(this, null, reportFile, pseudoTests);
    }
    private String getMaybeMissing(Map<String,String> headers, String headerName) {
        if (headers.containsKey(headerName)) {
            return headers.get(headerName);
        } else {
            return "<unknown>";
        }
    }

    private Map<Long,Map<String,String>> findCrc2LicenseHeaderMapping() throws IOException {
        Map<Long,Map<String,String>> crc2LicenseHeaders = new HashMap<Long,Map<String,String>>();
        for (String cluster : getProject().getProperty("nb.clusters.list").split("[, ]+")) {
            for (String module : getProject().getProperty(cluster).split("[, ]+")) {
                File d = new File(new File(nball, module), "external");
                Set<String> hgFiles = VerifyLibsAndLicenses.findHgControlledFiles(d);
                Map<String,Map<String,String>> binary2License = findBinary2LicenseHeaderMapping(hgFiles, d);
                for (String n : hgFiles) {
                    if (!n.endsWith(".jar") && !n.endsWith(".zip")) {
                        continue;
                    }
                    Map<String,String> headers = binary2License.get(n);
                    if (headers == null) {
                        continue;
                    }
                    File f = new File(d, n);
                    InputStream is = new FileInputStream(f);
                    try {
                        crc2LicenseHeaders.put(computeCRC32(is), headers);
                    } finally {
                        is.close();
                    }
                    ZipFile zf = new ZipFile(f);
                    try {
                        Enumeration<? extends ZipEntry> entries = zf.entries();
                        while (entries.hasMoreElements()) {
                            ZipEntry entry = entries.nextElement();
                            String innerName = entry.getName();
                            if (!innerName.endsWith(".jar") && !innerName.endsWith(".zip")) {
                                continue;
                            }
                            is = zf.getInputStream(entry);
                            try {
                                crc2LicenseHeaders.put(computeCRC32(is), headers);
                            } finally {
                                is.close();
                            }
                        }
                    } finally {
                        zf.close();
                    }
                }
            }
        }
        return crc2LicenseHeaders;
    }

    private long computeCRC32(InputStream is) throws IOException {
        byte[] buf = new byte[4096];
        CRC32 crc32 = new CRC32();
        int read;
        while ((read = is.read(buf)) != -1) {
            crc32.update(buf, 0, read);
        }
        return crc32.getValue();
    }

    private Map<String,Map<String,String>> findBinary2LicenseHeaderMapping(Set<String> cvsFiles, File d) throws IOException {
        Map<String,Map<String,String>> binary2LicenseHeaders = new HashMap<String,Map<String,String>>();
        for (String n : cvsFiles) {
            if (!n.endsWith("-license.txt")) {
                continue;
            }
            Map<String,String> headers = new HashMap<String,String>();
            InputStream is = new FileInputStream(new File(d, n));
            try {
                BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;
                while ((line = r.readLine()) != null && line.length() > 0) {
                    Matcher m = Pattern.compile("([a-zA-Z]+): (.+)").matcher(line);
                    if (m.matches()) {
                        headers.put(m.group(1), m.group(2));
                    }
                }
            } finally {
                is.close();
            }
            String files = headers.remove("Files");
            if (files != null) {
                for (String file : files.split("[, ]+")) {
                    binary2LicenseHeaders.put(file, headers);
                }
            } else {
                binary2LicenseHeaders.put(n.replaceFirst("-license\\.txt$", ".jar"), headers);
                binary2LicenseHeaders.put(n.replaceFirst("-license\\.txt$", ".zip"), headers);
            }
        }
        return binary2LicenseHeaders;
    }

    private void findBinaries(File d, Map<String,Map<String,String>> binaries2LicenseHeaders, Map<Long,Map<String,String>> crc2LicenseHeaders,
            Map<Long,String> crc2Binary, String prefix, StringBuilder testBinariesAreUnique, List<String> ignoredPatterns) throws IOException {
        String[] kids = d.list();
        Arrays.sort(kids);
        for (String n : kids) {
            File f = new File(d, n);
            if (f.isDirectory()) {
                findBinaries(f, binaries2LicenseHeaders, crc2LicenseHeaders, crc2Binary, prefix + n + "/", testBinariesAreUnique, ignoredPatterns);
            } else if (n.endsWith(".jar") || n.endsWith(".zip")) {
                InputStream is = new FileInputStream(f);
                try {
                    long crc = computeCRC32(is);
                    Map<String,String> headers = crc2LicenseHeaders.get(crc);
                    if (headers != null) {
                        String path = prefix + n;
                        binaries2LicenseHeaders.put(path, headers);
                        String otherPath = crc2Binary.put(crc, path);
                        if (otherPath != null) {
                            boolean ignored = false;
                            for (String pattern : ignoredPatterns) {
                                String[] parts = pattern.split(" ");
                                assert parts.length == 2 : pattern;
                                if (SelectorUtils.matchPath(parts[0], otherPath) && SelectorUtils.matchPath(parts[1], path)) {
                                    ignored = true;
                                    break;
                                }
                            }
                            if (!ignored) {
                                testBinariesAreUnique.append("\n" + otherPath + " and " + path + " are identical");
                            }
                        }
                    }
                } finally {
                    is.close();
                }
            }
        }
    }

}
