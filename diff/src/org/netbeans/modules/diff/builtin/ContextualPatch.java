/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

    private final Pattern unifiedRangePattern = Pattern.compile("@@ -(\\d+)(,\\d+)? \\+(\\d+)(,\\d+)? @@");
    private final Pattern baseRangePattern = Pattern.compile("\\*\\*\\* (\\d+)(,\\d+)? \\*\\*\\*\\*");
    private final Pattern modifiedRangePattern = Pattern.compile("--- (\\d+)(,\\d+)? ----");
    private final Pattern normalChangeRangePattern = Pattern.compile("(\\d+),(\\d+)c(\\d+),(\\d+)");
    private final Pattern normalAddRangePattern = Pattern.compile("(\\d+)a(\\d+),(\\d+)");
    private final Pattern normalDeleteRangePattern = Pattern.compile("(\\d+),(\\d+)d(\\d+)");
    
    private final File patchFile;
    private final File context;

    private BufferedReader  patchReader;
    private String          patchLine;
    private boolean         patchLineRead;
    private int             lastPatchedLine;    // the last line that was successfuly patched

    public static ContextualPatch create(File patchFile, File context) {
        return new ContextualPatch(patchFile, context); 
    }
    
    private ContextualPatch(File patchFile, File context) {
        this.patchFile = patchFile;
        this.context = context;
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
            for (SinglePatch patch : patches) {
                try {
                    applyPatch(patch, dryRun);
                    report.add(new PatchReport(patch.targetFile, computeBackup(patch.targetFile), PatchStatus.Patched, null));
                } catch (Exception e) {
                    report.add(new PatchReport(patch.targetFile, null, PatchStatus.Failure, e));
                }
            }
            return report;
        } finally {
            if (patchReader != null) try { patchReader.close(); } catch (IOException e) {}
        }
    }
    
    private void init() throws IOException {
        String MAGIC = "# This patch file was generated by NetBeans IDE"; // NOI18N
        patchReader = new BufferedReader(new FileReader(patchFile));
        String encoding = "ISO-8859-1";
        String line = patchReader.readLine();
        if (MAGIC.equals(line)) {
            encoding = "utf8"; // NOI18N
            line = patchReader.readLine();
            String MAGIC2 = "paths are relative to: "; // NOI18N
            int idx = line.indexOf(MAGIC2); 
            if (idx != -1) {
//                patchContext = line.substring(idx + MAGIC2.length());
            }
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
        patch.targetFile = computeTargetFile(patch, context);
        if (patch.targetFile.exists()) {
            target = readFile(patch.targetFile);
        } else {
            target = new ArrayList<String>();
        }
        for (Hunk hunk : patch.hunks) {
            applyHunk(target, hunk);
        }
        if (!dryRun) {
            backup(patch.targetFile);
            writeFile(patch, target);
        }
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

    private void writeFile(SinglePatch patch, List<String> lines) throws FileNotFoundException, UnsupportedEncodingException {
        patch.targetFile.getParentFile().mkdirs();
        PrintWriter w = new PrintWriter(patch.targetFile, getEncoding(patch.targetFile).name());
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
                String targetLine = target.get(idx);
                if (!targetLine.equals(hunkLine.substring(1))) {
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
            computeTargetPath(base, patch);
        }

        List<Hunk> hunks = new ArrayList<Hunk>();
        Hunk hunk = null;

        for (;;) {
            String line = readPatchLine();
            if (line == null || line.startsWith("Index:")) {
                unreadPatchLine();
                break;
            } else if (line.startsWith("***************")) {
                hunk = new Hunk();
                parseContextRange(hunk, readPatchLine());
                hunks.add(hunk);
            } else if (line.startsWith("--- ")) {
                parseContextRange(hunk, line);
                hunk.lines.add(line);
            } else {
                char c = line.charAt(0);
                if (c == ' ' || c == '+' || c == '-' || c == '!') {
                    hunk.lines.add(line);
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
            computeTargetPath(base, patch);
        }

        List<Hunk> hunks = new ArrayList<Hunk>();
        Hunk hunk = null;
        
        for (;;) {
            String line = readPatchLine();
            if (line == null || line.startsWith("Index:")) {
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
                throw new PatchException("Invalid hunk line: " + line);
            }
        }
        patch.hunks = (Hunk[]) hunks.toArray(new Hunk[hunks.size()]);
    }

    private void computeTargetPath(String base, SinglePatch patch) {
        int pathEndIdx = base.indexOf('\t');
        patch.targetPath = base.substring(4, pathEndIdx).trim();
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
    
    private File computeTargetFile(SinglePatch patch, File context) {
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
    }

    public static enum PatchStatus { Patched, Missing, Failure };

    public static final class PatchReport {

        private File        file;
        private File        originalBackupFile;
        private PatchStatus status;
        private Throwable   failure;

        PatchReport(File file, File originalBackupFile, PatchStatus status, Throwable failure) {
            this.file = file;
            this.originalBackupFile = originalBackupFile;
            this.status = status;
            this.failure = failure;
        }

        public File getFile() {
            return file;
        }

        public File getOriginalBackupFile() {
            return originalBackupFile;
        }

        public PatchStatus getStatus() {
            return status;
        }

        public Throwable getFailure() {
            return failure;
        }
    }
}
