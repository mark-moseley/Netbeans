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
package org.netbeans.modules.diff.builtin;

import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.filesystems.FileUtil;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.nio.charset.Charset;

/**
 * Applies contextual patches to files. The patch file can contain patches for multiple files. 
 * 
 * @author Maros Sandor
 */
public final class ContextualPatch {

    public static final String MAGIC = "# This patch file was generated by NetBeans IDE"; // NOI18N    
    
    // first seen in mercurial diffs: characters after the second @@ - ignore them  
    private final Pattern unifiedRangePattern = Pattern.compile("@@ -(\\d+)(,\\d+)? \\+(\\d+)(,\\d+)? @@(\\s.*)?");
    private final Pattern baseRangePattern = Pattern.compile("\\*\\*\\* (\\d+)(,\\d+)? \\*\\*\\*\\*");
    private final Pattern modifiedRangePattern = Pattern.compile("--- (\\d+)(,\\d+)? ----");
    private final Pattern normalChangeRangePattern = Pattern.compile("(\\d+),(\\d+)c(\\d+),(\\d+)");
    private final Pattern normalAddRangePattern = Pattern.compile("(\\d+)a(\\d+),(\\d+)");
    private final Pattern normalDeleteRangePattern = Pattern.compile("(\\d+),(\\d+)d(\\d+)");
    private final Pattern binaryHeaderPattern = Pattern.compile("MIME: (.*?); encoding: (.*?); length: (-?\\d+?)"); 
    
    private final File patchFile;
    private final File suggestedContext;

    private File            context;
    private BufferedReader  patchReader;
    private String          patchLine;
    private boolean         patchLineRead;
    private int             lastPatchedLine;    // the last line that was successfuly patched

    public static ContextualPatch create(File patchFile, File context) {
        return new ContextualPatch(patchFile, context); 
    }
    
    private ContextualPatch(File patchFile, File context) {
        this.patchFile = patchFile;
        this.suggestedContext = context;
    }

    /**
     * 
     * @param dryRun true if the method should not make any modifications to files, false otherwise
     * @return
     * @throws PatchException
     * @throws IOException
     */
    public List<PatchReport> patch(boolean dryRun) throws PatchException, IOException {
        List<PatchReport> report = new ArrayList<PatchReport>();
        init();
        try {
            patchLine = patchReader.readLine();
            List<SinglePatch> patches = new ArrayList<SinglePatch>(); 
            for (;;) {
                SinglePatch patch = getNextPatch();
                if (patch == null) break;
                patches.add(patch);
            }
            computeContext(patches);
            for (SinglePatch patch : patches) {
                try {
                    applyPatch(patch, dryRun);
                    report.add(new PatchReport(patch.targetFile, computeBackup(patch.targetFile), patch.binary, PatchStatus.Patched, null));
                } catch (Exception e) {
                    report.add(new PatchReport(patch.targetFile, null, patch.binary, PatchStatus.Failure, e));
                }
            }
            return report;
        } finally {
            if (patchReader != null) try { patchReader.close(); } catch (IOException e) {}
        }
    }
    
    private void init() throws IOException {
        patchReader = new BufferedReader(new FileReader(patchFile));
        String encoding = "ISO-8859-1";
        String line = patchReader.readLine();
        if (MAGIC.equals(line)) {
            encoding = "utf8"; // NOI18N
            line = patchReader.readLine();
        }
        patchReader.close();

        byte[] buffer = new byte[MAGIC.length()];
        InputStream in = new FileInputStream(patchFile);
        int read = in.read(buffer);
        in.close();
        if (read != -1 && MAGIC.equals(new String(buffer, "utf8"))) {  // NOI18N
            encoding = "utf8"; // NOI18N
        }
        patchReader = new BufferedReader(new InputStreamReader(new FileInputStream(patchFile), encoding));
    }
    
    private void applyPatch(SinglePatch patch, boolean dryRun) throws IOException, PatchException {
        lastPatchedLine = 1;
        List<String> target;
        patch.targetFile = computeTargetFile(patch);
        if (patch.targetFile.exists() && !patch.binary) {
            target = readFile(patch.targetFile);
            if (patchCreatesNewFileThatAlreadyExists(patch, target)) return;
        } else {
            target = new ArrayList<String>();
        }
        if (!patch.binary) {
            for (Hunk hunk : patch.hunks) {
                applyHunk(target, hunk);
            }
        }
        if (!dryRun) {
            backup(patch.targetFile);
            writeFile(patch, target);
        }
    }

    private boolean patchCreatesNewFileThatAlreadyExists(SinglePatch patch, List<String> originalFile) throws PatchException {
        if (patch.hunks.length != 1) return false;
        Hunk hunk = patch.hunks[0];
        if (hunk.baseStart != 0 || hunk.baseCount != 0 || hunk.modifiedStart != 1 || hunk.modifiedCount != originalFile.size()) return false;

        List<String> target = new ArrayList<String>(hunk.modifiedCount);
        applyHunk(target, hunk);
        return target.equals(originalFile);
    }

    private void backup(File target) throws IOException {
        if (target.exists()) {
            copyStreamsCloseAll(new FileOutputStream(computeBackup(target)), new FileInputStream(target));
        }
    }

    private File computeBackup(File target) {
        return new File(target.getParentFile(), target.getName() + ".original~");
    }

    private void copyStreamsCloseAll(OutputStream writer, InputStream reader) throws IOException {
        byte [] buffer = new byte[4096];
        int n;
        while ((n = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, n);
        }
        writer.close();
        reader.close();
    }

    private void writeFile(SinglePatch patch, List<String> lines) throws IOException {
        patch.targetFile.getParentFile().mkdirs();
        if (patch.binary) {
            if (patch.hunks.length == 0) {
                patch.targetFile.delete();
            } else {
                byte [] content = Base64.decode(patch.hunks[0].lines);
                copyStreamsCloseAll(new FileOutputStream(patch.targetFile), new ByteArrayInputStream(content));
            }
        } else {
            PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(patch.targetFile), getEncoding(patch.targetFile))); 
            try {
                if (lines.size() == 0) return;
                for (String line : lines.subList(0, lines.size() - 1)) {
                    w.println(line);
                }
                w.print(lines.get(lines.size() - 1));
                if (!patch.noEndingNewline) {
                    w.println();
                }
            } finally {
                w.close();
            }
        }
    }

    private void applyHunk(List<String> target, Hunk hunk) throws PatchException {
        int idx = findHunkIndex(target, hunk);
        if (idx == -1) throw new PatchException("Cannot apply hunk @@ " + hunk.baseCount);
        applyHunk(target, hunk, idx, false);
    }

    private int findHunkIndex(List<String> target, Hunk hunk) throws PatchException {
        int idx = hunk.modifiedStart;  // first guess from the hunk range specification
        if (idx >= lastPatchedLine && applyHunk(target, hunk, idx, true)) {
            return idx;
        } else {
            // try to search for the context
            for (int i = idx - 1; i >= lastPatchedLine; i--) {
                if (applyHunk(target, hunk, i, true)) {
                    return i;
                }
            }
            for (int i = idx + 1; i < target.size(); i++) {
                if (applyHunk(target, hunk, i, true)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * @return true if the application succeeded
     */
    private boolean applyHunk(List<String> target, Hunk hunk, int idx, boolean dryRun) throws PatchException {
        idx--; // indices in the target list are 0-based
        for (String hunkLine : hunk.lines) {
            boolean isAddition = isAdditionLine(hunkLine);
            if (!isAddition) {
                String targetLine = target.get(idx).trim();
                if (!targetLine.equals(hunkLine.substring(1).trim())) { // be optimistic, compare trimmed context lines
                    if (dryRun) {
                        return false;
                    } else {
                        throw new PatchException("Unapplicable hunk @@ " + hunk.baseStart);
                    }
                }
            }
            if (dryRun) {
                if (isAddition) {
                    idx--;
                }
            } else {
                if (isAddition) {
                    target.add(idx, hunkLine.substring(1));
                } else if (isRemovalLine(hunkLine)) {
                    target.remove(idx);
                    idx--;
                }
            }
            idx++;
        }
        idx++; // indices in the target list are 0-based
        lastPatchedLine = idx;
        return true;
    }

    private boolean isAdditionLine(String hunkLine) {
        return hunkLine.charAt(0) == '+';
    }

    private boolean isRemovalLine(String hunkLine) {
        return hunkLine.charAt(0) == '-';
    }

    private Charset getEncoding(File file) {
        try {
            return FileEncodingQuery.getEncoding(FileUtil.toFileObject(file));
        } catch (Throwable e) { // TODO: workaround for #108850
            // return default
        }
        return Charset.defaultCharset();
    }

    private List<String> readFile(File target) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(target), getEncoding(target)));
        if (r == null) {
            r = new BufferedReader(new FileReader(target));
        }
        try {
            List<String> lines = new ArrayList<String>();
            String line;
            while ((line = r.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        } finally {
            if (r != null) try { r.close(); } catch (IOException e) {}
        }
    }

    private SinglePatch getNextPatch() throws IOException, PatchException {
        SinglePatch patch = new SinglePatch();
        for (;;) {
            String line = readPatchLine();
            if (line == null) return null;
            
            if (line.startsWith("Index:")) {
                patch.targetPath = line.substring(6).trim();
            } else if (line.startsWith("MIME: application/octet-stream;")) {
                unreadPatchLine();
                readBinaryPatchContent(patch);
                break;
            } else if (line.startsWith("--- ")) {
                unreadPatchLine();
                readPatchContent(patch);
                break;
            } else if (line.startsWith("*** ")) {
                unreadPatchLine();
                readContextPatchContent(patch);
                break;
            } else if (isNormalDiffRange(line)) {
                unreadPatchLine();
                readNormalPatchContent(patch);
                break;
            }
        }
        return patch;
    }

    private boolean isNormalDiffRange(String line) {
        return normalAddRangePattern.matcher(line).matches()
            || normalChangeRangePattern.matcher(line).matches()
            || normalDeleteRangePattern.matcher(line).matches();
    }

    /**
     * Reads binary diff hunk.
     */
    private void readBinaryPatchContent(SinglePatch patch) throws PatchException, IOException {
        List<Hunk> hunks = new ArrayList<Hunk>();
        Hunk hunk = new Hunk();
        for (;;) {
            String line = readPatchLine();
            if (line == null || line.startsWith("Index:") || line.length() == 0) {
                unreadPatchLine();
                break;
            }
            if (patch.binary) {
                hunk.lines.add(line);
            } else {
                Matcher m = binaryHeaderPattern.matcher(line);
                if (m.matches()) {
                    patch.binary = true;
                    int length = Integer.parseInt(m.group(3));
                    if (length == -1) break;
                    hunks.add(hunk);
                }
            }
        }
        patch.hunks = (Hunk[]) hunks.toArray(new Hunk[hunks.size()]);
    }
    
    /**
     * Reads normal diff hunks.
     */
    private void readNormalPatchContent(SinglePatch patch) throws IOException, PatchException {
        List<Hunk> hunks = new ArrayList<Hunk>();
        Hunk hunk = null;
        Matcher m;
        for (;;) {
            String line = readPatchLine();
            if (line == null || line.startsWith("Index:")) {
                unreadPatchLine();
                break;
            }
            if ((m = normalAddRangePattern.matcher(line)).matches()) {
                hunk = new Hunk();
                hunks.add(hunk);
                parseNormalRange(hunk, m);
            } else if ((m = normalChangeRangePattern.matcher(line)).matches()) {
                hunk = new Hunk();
                hunks.add(hunk);
                parseNormalRange(hunk, m);
            } else if ((m = normalDeleteRangePattern.matcher(line)).matches()) {
                hunk = new Hunk();
                hunks.add(hunk);
                parseNormalRange(hunk, m);
            } else {
                if (line.startsWith("> ")) {
                    hunk.lines.add("+" + line.substring(2));
                } else if (line.startsWith("< ")) {
                    hunk.lines.add("-" + line.substring(2));
                } else if (line.startsWith("---")) {
                    // ignore
                } else {
                    throw new PatchException("Invalid hunk line: " + line);
                }
            }
        }
        patch.hunks = (Hunk[]) hunks.toArray(new Hunk[hunks.size()]);
    }

    private void parseNormalRange(Hunk hunk, Matcher m) {
        if (m.pattern() == normalAddRangePattern) {
            hunk.baseStart = Integer.parseInt(m.group(1));
            hunk.baseCount = 0;
            hunk.modifiedStart = Integer.parseInt(m.group(2));
            hunk.modifiedCount = Integer.parseInt(m.group(3)) - hunk.modifiedStart + 1;
        } else if (m.pattern() == normalDeleteRangePattern) {
            hunk.baseStart = Integer.parseInt(m.group(1));
            hunk.baseCount = Integer.parseInt(m.group(2)) - hunk.baseStart + 1;
            hunk.modifiedStart = Integer.parseInt(m.group(3));
            hunk.modifiedCount = 0;
        } else {
            hunk.baseStart = Integer.parseInt(m.group(1));
            hunk.baseCount = Integer.parseInt(m.group(2)) - hunk.baseStart + 1;
            hunk.modifiedStart = Integer.parseInt(m.group(3));
            hunk.modifiedCount = Integer.parseInt(m.group(4)) - hunk.modifiedStart + 1;
        }
    }

    /**
     * Reads context diff hunks.
     */
    private void readContextPatchContent(SinglePatch patch) throws IOException, PatchException {
        String base = readPatchLine();
        if (base == null || !base.startsWith("*** ")) throw new PatchException("Invalid context diff header: " + base);
        String modified = readPatchLine();
        if (modified == null || !modified.startsWith("--- ")) throw new PatchException("Invalid context diff header: " + modified);
        if (patch.targetPath == null) {
            computeTargetPath(base, modified, patch);
        }

        List<Hunk> hunks = new ArrayList<Hunk>();
        Hunk hunk = null;

        int lineCount = -1;
        for (;;) {
            String line = readPatchLine();
            if (line == null || line.length() == 0 || line.startsWith("Index:")) {
                unreadPatchLine();
                break;
            } else if (line.startsWith("***************")) {
                hunk = new Hunk();
                parseContextRange(hunk, readPatchLine());
                hunks.add(hunk);
            } else if (line.startsWith("--- ")) {
                lineCount = 0;
                parseContextRange(hunk, line);
                hunk.lines.add(line);
            } else {
                char c = line.charAt(0);
                if (c == ' ' || c == '+' || c == '-' || c == '!') {
                    if (lineCount < hunk.modifiedCount) {
                        hunk.lines.add(line);
                        if (lineCount != -1) {
                            lineCount++;
                        }
                    }
                } else {
                    throw new PatchException("Invalid hunk line: " + line);
                }
            }
        }
        patch.hunks = (Hunk[]) hunks.toArray(new Hunk[hunks.size()]);
        convertContextToUnified(patch);
    }

    private void convertContextToUnified(SinglePatch patch) throws PatchException {
        Hunk [] unifiedHunks = new Hunk[patch.hunks.length];
        int idx = 0;
        for (Hunk hunk : patch.hunks) {
            unifiedHunks[idx++] = convertContextToUnified(hunk);
        }
        patch.hunks = unifiedHunks;
    }

    private Hunk convertContextToUnified(Hunk hunk) throws PatchException {
        Hunk unifiedHunk = new Hunk();
        unifiedHunk.baseStart = hunk.baseStart;
        unifiedHunk.modifiedStart = hunk.modifiedStart;
        int split = -1;
        for (int i = 0; i < hunk.lines.size(); i++) {
            if (hunk.lines.get(i).startsWith("--- ")) {
                split = i;
                break;
            }
        }
        if (split == -1) throw new PatchException("Missing split divider in context patch");

        int baseIdx = 0;
        int modifiedIdx = split + 1;
        List<String> unifiedLines = new ArrayList<String>(hunk.lines.size());
        for (; baseIdx < split || modifiedIdx < hunk.lines.size(); ) {
            String baseLine = baseIdx < split ? hunk.lines.get(baseIdx) : "~";
            String modifiedLine = modifiedIdx < hunk.lines.size() ? hunk.lines.get(modifiedIdx) : "~";
            if (baseLine.startsWith("- ")) {
                unifiedLines.add("-" + baseLine.substring(2));
                unifiedHunk.baseCount++;
                baseIdx++;
            } else if (modifiedLine.startsWith("+ ")) {
                unifiedLines.add("+" + modifiedLine.substring(2));
                unifiedHunk.modifiedCount++;
                modifiedIdx++;
            } else if (baseLine.startsWith("! ")) {
                unifiedLines.add("-" + baseLine.substring(2));
                unifiedHunk.baseCount++;
                baseIdx++;
            } else if (modifiedLine.startsWith("! ")) {
                unifiedLines.add("+" + modifiedLine.substring(2));
                unifiedHunk.modifiedCount++;
                modifiedIdx++;
            } else if (baseLine.startsWith("  ") && modifiedLine.startsWith("  ")) {
                unifiedLines.add(baseLine.substring(1));
                unifiedHunk.baseCount++;
                unifiedHunk.modifiedCount++;
                baseIdx++;
                modifiedIdx++;
            } else if (baseLine.startsWith("  ")) {
                unifiedLines.add(baseLine.substring(1));
                unifiedHunk.baseCount++;
                unifiedHunk.modifiedCount++;
                baseIdx++;
            } else if (modifiedLine.startsWith("  ")) {
                unifiedLines.add(modifiedLine.substring(1));
                unifiedHunk.baseCount++;
                unifiedHunk.modifiedCount++;
                modifiedIdx++;
            } else {
                throw new PatchException("Invalid context patch: " + baseLine);
            }
        }
        unifiedHunk.lines = unifiedLines;
        return unifiedHunk;
    }

    /**
     * Reads unified diff hunks.
     */
    private void readPatchContent(SinglePatch patch) throws IOException, PatchException {
        String base = readPatchLine();
        if (base == null || !base.startsWith("--- ")) throw new PatchException("Invalid unified diff header: " + base);
        String modified = readPatchLine();
        if (modified == null || !modified.startsWith("+++ ")) throw new PatchException("Invalid unified diff header: " + modified);
        if (patch.targetPath == null) {
            computeTargetPath(base, modified, patch);
        }

        List<Hunk> hunks = new ArrayList<Hunk>();
        Hunk hunk = null;
        
        for (;;) {
            String line = readPatchLine();
            if (line == null || line.length() == 0 || line.startsWith("Index:")) {
                unreadPatchLine();
                break;
            }
            char c = line.charAt(0); 
            if (c == '@') {
                hunk = new Hunk();
                parseRange(hunk, line);
                hunks.add(hunk);
            } else if (c == ' ' || c == '+' || c == '-') {
                hunk.lines.add(line);
            } else if (line.equals(Hunk.ENDING_NEWLINE)) {
                patch.noEndingNewline = true;
            } else {
                // first seen in mercurial diffs: be optimistic, this is probably the end of this patch  
                unreadPatchLine();
                break;
            }
        }
        patch.hunks = (Hunk[]) hunks.toArray(new Hunk[hunks.size()]);
    }

    private void computeTargetPath(String base, String modified, SinglePatch patch) {
        base = base.substring("+++ ".length());
        modified = modified.substring("--- ".length());
        // first seen in mercurial diffs: base and modified paths are different: base starts with "a/" and modified starts with "b/"
        if (base.startsWith("a/") && modified.startsWith("b/")) {
            base = base.substring(2);
        }
        int pathEndIdx = base.indexOf('\t');
        patch.targetPath = base.substring(0, pathEndIdx).trim();
    }

    private void parseRange(Hunk hunk, String range) throws PatchException {
        Matcher m = unifiedRangePattern.matcher(range);
        if (!m.matches()) throw new PatchException("Invalid unified diff range: " + range);
        hunk.baseStart = Integer.parseInt(m.group(1));
        hunk.baseCount = m.group(2) != null ? Integer.parseInt(m.group(2).substring(1)) : 1;
        hunk.modifiedStart = Integer.parseInt(m.group(3));
        hunk.modifiedCount = m.group(4) != null ? Integer.parseInt(m.group(4).substring(1)) : 1;
    }

    private void parseContextRange(Hunk hunk, String range) throws PatchException {
        if (range.charAt(0) == '*') {
            Matcher m = baseRangePattern.matcher(range);
            if (!m.matches()) throw new PatchException("Invalid context diff range: " + range);
            hunk.baseStart = Integer.parseInt(m.group(1));
            hunk.baseCount = m.group(2) != null ? Integer.parseInt(m.group(2).substring(1)) : 1;
            hunk.baseCount -= hunk.baseStart - 1;
        } else {
            Matcher m = modifiedRangePattern.matcher(range);
            if (!m.matches()) throw new PatchException("Invalid context diff range: " + range);
            hunk.modifiedStart = Integer.parseInt(m.group(1));
            hunk.modifiedCount = m.group(2) != null ? Integer.parseInt(m.group(2).substring(1)) : 1;
            hunk.modifiedCount -= hunk.modifiedStart - 1;
        }
    }

    private String readPatchLine() throws IOException {
        if (patchLineRead) {
            patchLine = patchReader.readLine();
        } else {
            patchLineRead = true;
        }
        return patchLine;
    }

    private void unreadPatchLine() {
        patchLineRead = false;
    }
    
    private void computeContext(List<SinglePatch> patches) {
        File bestContext = suggestedContext;
        int bestContextMatched = 0;
        for (context = suggestedContext; context != null; context = context.getParentFile()) {
            int patchedFiles = 0;
            for (SinglePatch patch : patches) {
                try {
                    applyPatch(patch, true);
                    patchedFiles++;
                } catch (Exception e) {
                    // patch failed to apply
                }
            }
            if (patchedFiles > bestContextMatched) {
                bestContextMatched = patchedFiles;
                bestContext = context;
                if (patchedFiles == patches.size()) break;
            }
        }
        context = bestContext;
    }

    private File computeTargetFile(SinglePatch patch) {
        if (patch.targetPath == null) {
            patch.targetPath = context.getAbsolutePath();
        }
        if (context.isFile()) return context;
        return new File(context, patch.targetPath);
    }

    private class SinglePatch {
        String      targetIndex;
        String      targetPath;
        Hunk []     hunks;
        boolean     targetMustExist = true;     // == false if the patch contains one hunk with just additions ('+' lines)
        File        targetFile;                 // computed later
        boolean     noEndingNewline;            // resulting file should not end with a newline
        boolean     binary;                  // binary patches contain one encoded Hunk
    }

    public static enum PatchStatus { Patched, Missing, Failure };

    public static final class PatchReport {

        private File        file;
        private File        originalBackupFile;
        private boolean     binary;
        private PatchStatus status;
        private Throwable   failure;

        PatchReport(File file, File originalBackupFile, boolean binary, PatchStatus status, Throwable failure) {
            this.file = file;
            this.originalBackupFile = originalBackupFile;
            this.binary = binary;
            this.status = status;
            this.failure = failure;
        }

        public File getFile() {
            return file;
        }

        public File getOriginalBackupFile() {
            return originalBackupFile;
        }

        public boolean isBinary() {
            return binary;
        }

        public PatchStatus getStatus() {
            return status;
        }

        public Throwable getFailure() {
            return failure;
        }
    }
}
