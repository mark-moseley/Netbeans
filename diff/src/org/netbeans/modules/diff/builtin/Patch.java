/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.diff.builtin;

import java.io.BufferedReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import org.netbeans.api.diff.Difference;

import org.netbeans.modules.diff.cmdline.CmdlineDiffProvider;

/**
 * Utility class for patch application.
 *
 * @author  Martin Entlicher
 */
public class Patch extends Reader {
    
    private static final int CONTEXT_DIFF = 0;
    private static final int NORMAL_DIFF = 1;
    
    private Difference[] diffs;
    private PushbackReader source;
    private int currDiff = 0;
    private int line = 1;
    private String newLine = null; // String, that is used to separate lines
    private StringBuffer buff = new StringBuffer();
    
    /** Creates a new instance of Patch */
    private Patch(Difference[] diffs, Reader source) {
        this.diffs = diffs;
        this.source = new PushbackReader(new BufferedReader(source), 1);
    }
    
    /**
     * Apply the patch to the source.
     * @param diffs The differences to patch
     * @param source The source stream
     * @return The patched stream
     * @throws IOException When reading from the source stread fails
     * @throws ParseException When the source does not match the patch to be applied
     */
    public static Reader apply(Difference[] diffs, Reader source) {//throws IOException, ParseException {
        return new Patch(diffs, source);
    }
    
    /**
     * Parse the differences.
     *
    public static Difference[] parse(Reader source) throws IOException {
        return parseContextDiff(source);
    }
     */
    
    /**
     * Parse the differences and corresponding file names.
     */
    public static FileDifferences[] parse(Reader source) throws IOException {
        List fileDifferences = new ArrayList();
        //int pushBackLimit = DIFFERENCE_DELIMETER.length();
        //PushbackReader recognizedSource = new PushbackReader(source, pushBackLimit);
        Patch.SinglePatchReader patchReader = new Patch.SinglePatchReader(source);
        int[] diffType = new int[1];
        String[] fileName = new String[1];
        while (patchReader.hasNextPatch(diffType, fileName)) {
            //System.out.println("Have a next patch of name '"+fileName[0]+"'");
            Difference[] diffs = null;
            switch (diffType[0]) {
                case CONTEXT_DIFF:
                    diffs = parseContextDiff(patchReader);
                    break;
                case NORMAL_DIFF:
                    diffs = parseNormalDiff(patchReader);
                    break;
            }
            if (diffs != null) {
                fileDifferences.add(new FileDifferences(fileName[0].intern(), diffs));
            }
        }
        return (FileDifferences[]) fileDifferences.toArray(new FileDifferences[fileDifferences.size()]);
    }
    
    public int read(char[] cbuf, int off, int length) throws java.io.IOException {
        if (buff.length() < length) {
            doRetrieve(length - buff.length());
        }
        int ret = Math.min(buff.length(), length);
        if (ret == 0) return -1;
        String retStr = buff.substring(0, ret);
        char[] retChars = retStr.toCharArray();
        System.arraycopy(retChars, 0, cbuf, off, ret);
        buff.delete(0, ret);
        return ret;
    }
    
    public void close() throws java.io.IOException {
        source.close();
    }
    
    private void doRetrieve(int length) throws IOException {
        for (int size = 0; size < length; line++) {
            if (currDiff < diffs.length &&
                ((Difference.ADD == diffs[currDiff].getType() &&
                  line == (diffs[currDiff].getFirstStart() + 1)) ||
                 (Difference.ADD != diffs[currDiff].getType() &&
                  line == diffs[currDiff].getFirstStart()))) {
                if (compareText(source, diffs[currDiff].getFirstText())) {
                    String text = convertNewLines(diffs[currDiff].getSecondText(), newLine);
                    buff.append(text);
                    currDiff++;
                } else {
                    throw new IOException("Patch not applicable.");
                }
            }
            StringBuffer newLineBuffer = null;
            if (newLine == null) {
                newLineBuffer = new StringBuffer();
            }
            String lineStr = readLine(source, newLineBuffer);
            if (newLineBuffer != null) newLine = newLineBuffer.toString();
            if (lineStr == null) break;
            buff.append(lineStr);
            buff.append(newLine);
        }
    }
    
    /** Reads a line and returns the char sequence for newline */
    private static String readLine(PushbackReader r, StringBuffer nl) throws IOException {
        StringBuffer line = new StringBuffer();
        int ic = r.read();
        if (ic == -1) return null;
        char c = (char) ic;
        while (c != '\n' && c != '\r') {
            line.append(c);
            ic = r.read();
            if (ic == -1) break;
            c = (char) ic;
        }
        if (nl != null) {
            nl.append(c);
        }
        if (c == '\r') {
            try {
                ic = r.read();
                if (ic != -1) {
                    c = (char) ic;
                    if (c != '\n') r.unread(c);
                    else if (nl != null) nl.append(c);
                }
            } catch (IOException ioex) {}
        }
        return line.toString();
    }
    
    private static String convertNewLines(String text, String newLine) {
        if (newLine == null) return text;
        StringBuffer newText = new StringBuffer();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') newText.append(newLine);
            else if (c == '\r') {
                if ((i + 1) < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                    newText.append(newLine);
                }
            } else newText.append(c);
        }
        return newText.toString();
    }
    
    private boolean compareText(PushbackReader source, String text) throws IOException {
        if (text == null || text.length() == 0) return true;
        text = adjustTextNL(text);
        char[] chars = new char[text.length()];
        int pos = 0;
        int n;
        String readStr = "";
        do {
            n = source.read(chars, 0, chars.length - pos);
            if (n > 0) {
                pos += n;
                readStr = readStr + new String(chars, 0, n);
            }
            if (readStr.endsWith("\r")) {
                try {
                    char c = (char) source.read();
                    if (c != '\n') source.unread(c);
                } catch (IOException ioex) {}
            }
            readStr = adjustTextNL(readStr);
            pos = readStr.length();
        } while (n > 0 && pos < chars.length);
        readStr.getChars(0, readStr.length(), chars, 0);
        line += numChars('\n', chars);
        //System.out.println("Comparing text of the diff:\n'"+text+"'\nWith the read text:\n'"+readStr+"'\n");
        //System.out.println("  EQUALS = "+readStr.equals(text));
        return readStr.equals(text);
    }
    
    /**
     * When comparing the two texts, it's important to ignore different line endings.
     * This method assures, that only '\n' is used as the line ending.
     */
    private String adjustTextNL(String text) {
        text = org.openide.util.Utilities.replaceString(text, "\r\n", "\n");
        text = org.openide.util.Utilities.replaceString(text, "\n\r", "\n");
        text = org.openide.util.Utilities.replaceString(text, "\r", "\n");
        return text;
    }
    
    private static int numChars(char c, char[] chars) {
        int n = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == c) n++;
        }
        return n;
    }
    
    private static final String CONTEXT_MARK1B = "*** ";
    private static final String CONTEXT_MARK1E = " ****";
    private static final String CONTEXT_MARK2B = "--- ";
    private static final String CONTEXT_MARK2E = " ----";
    private static final String CONTEXT_MARK_DELIMETER = ",";
    private static final String DIFFERENCE_DELIMETER = "***************";
    private static final String LINE_PREP = "  ";
    private static final String LINE_PREP_ADD = "+ ";
    private static final String LINE_PREP_REMOVE = "- ";
    private static final String LINE_PREP_CHANGE = "! ";
    
    private static Difference[] parseContextDiff(Reader in) throws IOException {
        BufferedReader br = new BufferedReader(in);
        ArrayList diffs = new ArrayList();
        String line;
        do {
            do {
                line = br.readLine();
            } while (line != null && !DIFFERENCE_DELIMETER.equals(line));
            int[] firstInterval = new int[2];
            line = br.readLine();
            if (line != null && line.startsWith(CONTEXT_MARK1B)) {
                try {
                    readNums(line, CONTEXT_MARK1B.length(), firstInterval);
                } catch (NumberFormatException nfex) {
                    throw new IOException(nfex.getLocalizedMessage());
                }
            } else continue;
            ArrayList firstChanges = new ArrayList(); // List of intervals and texts
            line = fillChanges(firstInterval, br, CONTEXT_MARK2B, firstChanges);
            int[] secondInterval = new int[2];
            if (line != null && line.startsWith(CONTEXT_MARK2B)) {
                try {
                    readNums(line, CONTEXT_MARK2B.length(), secondInterval);
                } catch (NumberFormatException nfex) {
                    throw new IOException(nfex.getLocalizedMessage());
                }
            } else continue;
            ArrayList secondChanges = new ArrayList(); // List of intervals and texts
            line = fillChanges(secondInterval, br, DIFFERENCE_DELIMETER, secondChanges);
            mergeChanges(firstInterval, secondInterval, firstChanges, secondChanges, diffs);
        } while (line != null);
        return (Difference[]) diffs.toArray(new Difference[diffs.size()]);
    }
    
    private static void readNums(String str, int off, int[] values) throws NumberFormatException {
        int end = str.indexOf(CONTEXT_MARK_DELIMETER, off);
        if (end > 0) {
            values[0] = Integer.parseInt(str.substring(off, end).trim());
        } else throw new NumberFormatException("Missing comma.");
        off = end + 1;
        end = str.indexOf(' ', off);
        if (end > 0) {
            values[1] = Integer.parseInt(str.substring(off, end).trim());
        } else throw new NumberFormatException("Missing final space.");
    }

    private static String fillChanges(int[] interval, BufferedReader br,
                                      String untilStartsWith, List changes) throws IOException {
        String line = br.readLine();
        for (int pos = interval[0]; pos <= interval[1]; pos++) {
            if (line == null || line.startsWith(untilStartsWith)) break;
            if (line.startsWith(LINE_PREP_ADD)) {
                int[] changeInterval = new int[3];
                changeInterval[0] = pos;
                changeInterval[2] = Difference.ADD;
                StringBuffer changeText = new StringBuffer();
                changeText.append(line.substring(LINE_PREP_ADD.length()));
                changeText.append('\n');
                do {
                    line = br.readLine();
                    if (line.startsWith(LINE_PREP_ADD)) {
                        changeText.append(line.substring(LINE_PREP_ADD.length()));
                        changeText.append('\n');
                    } else {
                        break;
                    }
                    pos++;
                } while (true);
                changeInterval[1] = pos;
                changes.add(changeInterval);
                changes.add(changeText.toString());
            } else if (line.startsWith(LINE_PREP_REMOVE)) {
                int[] changeInterval = new int[3];
                changeInterval[0] = pos;
                changeInterval[2] = Difference.DELETE;
                StringBuffer changeText = new StringBuffer();
                changeText.append(line.substring(LINE_PREP_REMOVE.length()));
                changeText.append('\n');
                do {
                    line = br.readLine();
                    if (line.startsWith(LINE_PREP_REMOVE)) {
                        changeText.append(line.substring(LINE_PREP_REMOVE.length()));
                        changeText.append('\n');
                    } else {
                        break;
                    }
                    pos++;
                } while (true);
                changeInterval[1] = pos;
                changes.add(changeInterval);
                changes.add(changeText.toString());
            } else if (line.startsWith(LINE_PREP_CHANGE)) {
                int[] changeInterval = new int[3];
                changeInterval[0] = pos;
                changeInterval[2] = Difference.CHANGE;
                StringBuffer changeText = new StringBuffer();
                changeText.append(line.substring(LINE_PREP_CHANGE.length()));
                changeText.append('\n');
                do {
                    line = br.readLine();
                    if (line.startsWith(LINE_PREP_CHANGE)) {
                        changeText.append(line.substring(LINE_PREP_CHANGE.length()));
                        changeText.append('\n');
                    } else {
                        break;
                    }
                    pos++;
                } while (true);
                changeInterval[1] = pos;
                changes.add(changeInterval);
                changes.add(changeText.toString());
            } else {
                line = br.readLine();
            }
        }
        return line;
    }
    
    private static void mergeChanges(int[] firstInterval, int[] secondInterval,
                              List firstChanges, List secondChanges, List diffs) {
        int p1, p2;
        int n1 = firstChanges.size();
        int n2 = secondChanges.size();
        //System.out.println("mergeChanges(("+firstInterval[0]+", "+firstInterval[1]+"), ("+secondInterval[0]+", "+secondInterval[1]+"))");
        //System.out.println("firstChanges.size() = "+n1);
        //System.out.println("secondChanges.size() = "+n2);
        for (p1 = p2 = 0; p1 < n1 || p2 < n2; ) {
            boolean isAddRemove = true;
            while (isAddRemove && p1 < n1) {
                int[] interval = (int[]) firstChanges.get(p1);
                isAddRemove = interval[2] == Difference.ADD || interval[2] == Difference.DELETE;
                if (isAddRemove) {
                    diffs.add(new Difference(interval[2], interval[0], interval[1],
                                             secondInterval[0] + interval[0] - firstInterval[0],
                                             secondInterval[0] + interval[1] - firstInterval[0],
                                             (String) firstChanges.get(p1 + 1), ""));
                    p1 += 2;
                }
            }
            isAddRemove = true;
            while (isAddRemove && p2 < n2) {
                int[] interval = (int[]) secondChanges.get(p2);
                isAddRemove = interval[2] == Difference.ADD || interval[2] == Difference.DELETE;
                if (isAddRemove) {
                    diffs.add(new Difference(interval[2],
                                             firstInterval[0] + interval[0] - secondInterval[0],
                                             firstInterval[0] + interval[1] - secondInterval[0],
                                             interval[0], interval[1],
                                             "", (String) secondChanges.get(p2 + 1)));
                    p2 += 2;
                }
            }
            // Change is remaining
            if (p1 < n1 && p2 < n2) {
                int[] interval1 = (int[]) firstChanges.get(p1);
                int[] interval2 = (int[]) secondChanges.get(p2);
                diffs.add(new Difference(interval1[2], interval1[0], interval1[1],
                                         interval2[0], interval2[1],
                                         (String) firstChanges.get(p1 + 1),
                                         (String) secondChanges.get(p2 + 1)));
                p1 += 2;
                p2 += 2;
            }
        }
    }
    
    private static Difference[] parseNormalDiff(Reader in) throws IOException {
        RE normRegexp;
        try {
            normRegexp = new RE(CmdlineDiffProvider.DIFF_REGEXP);
        } catch (RESyntaxException rsex) {
            normRegexp = null;
        }
        StringBuffer firstText = new StringBuffer();
        StringBuffer secondText = new StringBuffer();
        BufferedReader br = new BufferedReader(in);
        ArrayList diffs = new ArrayList();
        String line;
        while ((line = br.readLine()) != null) {
            CmdlineDiffProvider.outputLine(line, normRegexp, diffs, firstText, secondText);
        }
        CmdlineDiffProvider.setTextOnLastDifference(diffs, firstText, secondText);
        return (Difference[]) diffs.toArray(new Difference[diffs.size()]);
    }
    
    /**
     * A reader, that will not read more, than a single patch content
     * from the supplied reader with possibly more patches.
     */
    private static class SinglePatchReader extends Reader {
        
        private static final int BUFF_SIZE = 512;
        private PushbackReader in;
        private char[] buffer = new char[BUFF_SIZE];
        private int buffLength = 0;
        private int buffPos = 0;
        private boolean isAtEndOfPatch = false;
        
        public SinglePatchReader(Reader in) {
            this.in = new PushbackReader(in, BUFF_SIZE);
        }
        
        public int read(char[] values, int offset, int length) throws java.io.IOException {
            //System.out.println("SinglePatchReader.read("+offset+", "+length+")");
            int totRead = 0;
            while (length > 0) {
                int buffCopyLength;
                if (length < buffLength) {
                    buffCopyLength = length;
                    length = 0;
                } else {
                    if (buffLength > 0) {
                        buffCopyLength = buffLength;
                        length -= buffLength;
                    } else {
                        if (isAtEndOfPatch) {
                            length = 0;
                            buffCopyLength = -1;
                        } else {
                            buffLength = readTillEndOfPatch(buffer);
                            buffPos = 0;
                            if (buffLength <= 0) {
                                buffCopyLength = -1;
                            } else {
                                buffCopyLength = Math.min(length, buffLength);
                                length -= buffCopyLength;
                            }
                        }
                    }
                }
                if (buffCopyLength > 0) {
                    System.arraycopy(buffer, buffPos, values, offset, buffCopyLength);
                    offset += buffCopyLength;
                    buffLength -= buffCopyLength;
                    buffPos += buffCopyLength;
                    totRead += buffCopyLength;
                } else {
                    length = 0;
                }
            }
            if (totRead == 0) totRead = -1;
            //System.out.println("  read = '"+((totRead >= 0) ? new String(values, 0, totRead) : "NOTHING")+"', totRead = "+totRead);
            return totRead;
        }
        
        private int readTillEndOfPatch(char[] buffer) throws IOException {
            int length = in.read(buffer);
            String input = new String(buffer);
            int end = 0;
            if (input.startsWith(FILE_INDEX) || ((end = input.indexOf("\n"+FILE_INDEX))) >= 0) {
                isAtEndOfPatch = true;
            } else {
                end = input.lastIndexOf('\n');
                if (end >= 0) end++;
            }
            if (end >= 0 && end < length) {
                in.unread(buffer, end, length - end);
                length = end;
            }
            if (end == 0) length = -1;
            return length;
        }
        
        public void close() throws java.io.IOException {
            // Do nothing!
        }
        
        private static final String FILE_INDEX = "Index: "; // NOI18N
        
        private boolean hasNextPatch(int[] diffType, String[] fileName) throws IOException {
            isAtEndOfPatch = false; // We're prepared for the next patch
            PushbackReader patchSource = in;
            char[] buff = new char[DIFFERENCE_DELIMETER.length()];
            int length;
            RE normRegexp;
            try {
                normRegexp = new RE(CmdlineDiffProvider.DIFF_REGEXP);
            } catch (RESyntaxException rsex) {
                normRegexp = null;
            }
            while ((length = patchSource.read(buff)) > 0) {
                String input = new String(buff, 0, length);
                int nl = input.indexOf('\n');
                if (nl >= 0) {
                    input = input.substring(0, nl);
                    if (nl + 1 < length) {
                        patchSource.unread(buff, nl + 1, length - (nl + 1));
                        length = nl + 1;
                    }
                }
                if (input.equals(DIFFERENCE_DELIMETER)) {
                    diffType[0] = CONTEXT_DIFF;
                    patchSource.unread(buff, 0, length);
                    return true;
                } else if (input.startsWith(FILE_INDEX)) {
                    StringBuffer name = new StringBuffer(input.substring(FILE_INDEX.length()));
                    if (nl < 0) {
                        int r;
                        char c;
                        while ((c = (char) (r = patchSource.read())) != '\n' && r != -1) {
                            name.append(c);
                        }
                    }
                    fileName[0] = name.toString();
                } else if (input.startsWith(CONTEXT_MARK1B)) {
                    StringBuffer name = new StringBuffer(input.substring(CONTEXT_MARK1B.length()));
                    String sname = name.toString();
                    int spaceIndex = sname.indexOf('\t');
                    if (spaceIndex > 0) {
                        name = name.delete(spaceIndex, name.length());
                    }
                    if (nl < 0) {
                        int r = 0;
                        char c = 0;
                        if (spaceIndex < 0) {
                            while ((c = (char) (r = patchSource.read())) != '\n' && c != '\t' && r != -1) {
                                name.append(c);
                            }
                        }
                        if (c != '\n' && r != -1) {
                            while (((char) (r = patchSource.read())) != '\n' && r != -1) ; // Read the rest of the line
                        }
                    }
                    fileName[0] = name.toString();
                } else if (normRegexp != null && normRegexp.match(input)) {
                    diffType[0] = NORMAL_DIFF;
                    patchSource.unread(buff, 0, length);
                    return true;
                } else { // Read the rest of the garbaged line
                    if (nl < 0) {
                        int r;
                        while (((char) (r = patchSource.read())) != '\n' && r != -1) ;
                    }
                }
            }
            return false;
        }
        
    }
    
    public static class FileDifferences extends Object {
        
        private String fileName;
        private Difference[] diffs;
        
        public FileDifferences(String fileName, Difference[] diffs) {
            this.fileName = fileName;
            this.diffs = diffs;
        }
        
        public final String getFileName() {
            return fileName;
        }
        
        public final Difference[] getDifferences() {
            return diffs;
        }
    }
    
}
