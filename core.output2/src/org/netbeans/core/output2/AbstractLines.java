/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.core.output2;

import org.openide.windows.OutputListener;
import org.openide.ErrorManager;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract Lines implementation with handling for getLine wrap calculations, etc.
 */
abstract class AbstractLines implements Lines {
    /** A collections-like lineStartList that maps file positions to getLine numbers */
    IntList lineStartList;
    /** Maps output listeners to the lines they are associated with */
    IntMap linesToListeners;
    private int longestLine = 0;
    private static Boolean unitTestUseCache = null;
    private int knownCharCount = -1;
    private SparseIntList knownLogicalLineCounts = null;
    private int lastCharCountForWrapCalculation = -1;
    private int lastWrappedLineCount = -1;
    private int lastCharCountForWrapAboveCalculation = -1;
    private int lastWrappedAboveLineCount = -1;
    private int lastWrappedAboveLine = -1;
    private IntList errLines = null;


    AbstractLines() {
        if (Controller.log) Controller.log ("Creating a new AbstractLines");
        init();
    }

    protected abstract Storage getStorage();

    protected abstract boolean isDisposed();

    protected abstract boolean isTrouble();

    protected abstract Object readLock();

    protected abstract void handleException (Exception e);

    public char[] getText (int start, int end, char[] chars) {
        if (isDisposed() || isTrouble()) {
             //There is a breif window of opportunity for a window to display
             //a disposed document.  Should never appear on screen, but it can
             //be requested to calculate the preferred size this will
             //make sure it's noticable if it does.
             if (Controller.log) {
                 Controller.log (this + "  !!!!!REQUEST FOR SUBRANGE " + start + "-" + end + " AFTER OUTWRITER HAS BEEN DISPOSED!!");

             }
             char[] msg = "THIS OUTPUT HAS BEEN DISPOSED! ".toCharArray();
             if (chars == null) {
                 chars = new char[end - start];
             }
             int pos = 0;
             for (int i=0; i < chars.length; i++) {
                 if (pos == msg.length - 1) {
                     pos = 0;
                 }
                 chars[i] = msg[pos];
                 pos++;
             }
             return chars;
        }
        if (end < start) {
            throw new IllegalArgumentException ("Illogical text range from " +
                start + " to " + end);
        }
        synchronized(readLock()) {
            int fileStart = AbstractLines.toByteIndex(start);
            int byteCount = AbstractLines.toByteIndex(end - start);
            if (chars.length < end - start) {
                chars = new char[end-start];
            }
            try {
                getStorage().getReadBuffer(fileStart, byteCount).asCharBuffer().get(chars, 0, end - start);
                return chars;
            } catch (Exception e) {
                handleException (e);
                return new char[0];
            }
        }
    }

    public String getText (int start, int end) {
        if (isDisposed() || isTrouble()) {
            return new String (new char[end - start]);
        }
        if (end < start) {
            throw new IllegalArgumentException ("Illogical text range from " +
                start + " to " + end);
        }
        synchronized(readLock()) {
            int fileStart = AbstractLines.toByteIndex(start);
            int byteCount = AbstractLines.toByteIndex(end - start);
            int available = getStorage().size();
            if (available < fileStart + byteCount) {
                throw new ArrayIndexOutOfBoundsException ("Bytes from " +
                    fileStart + " to " + (fileStart + byteCount) + " requested, " +
                    "but storage is only " + available + " bytes long");
            }
            try {
                return getStorage().getReadBuffer(fileStart, byteCount).asCharBuffer().toString();
            } catch (Exception e) {
                handleException (e);
                return new String(new char[end - start]);
            }
        }
    }

    void markErr() {
        if (isTrouble() || getStorage().isClosed()) {
            return;
        }
        if (errLines == null) {
            errLines = new IntList(20);
        }
        errLines.add(getLineCount() == 0 ? 0 : getLineCount()-1);
    }

    public boolean isErr(int line) {
        return errLines != null ? errLines.contains(line) : false;
    }

    public boolean hasHyperlinks() {
        return firstListenerLine() != -1;
    }

    public boolean isHyperlink (int line) {
        return getListenerForLine(line) != null;
    }

    private void init() {
        knownLogicalLineCounts = null;
        lineStartList = new IntList(100);
        linesToListeners = new IntMap();
        setLastWrappedLineCount(-1);
        longestLine = 0;
        errLines = null;
        matcher = null;
    }

    public int[] allListenerLines() {
        return linesToListeners.getKeys();
    }

    void clear() {
        init();
    }

    public int getCharCount() {
        if (isDisposed() || isTrouble()) return 0;
        Storage storage = getStorage();
        return storage == null ? 0 : AbstractLines.toCharIndex(getStorage().size());
    }

    /**
     * Get a single getLine as a string.
     */
    public String getLine (int idx) throws IOException {
        if (isDisposed() || isTrouble()) {
            return ""; //NOI18N
        }
        int lineStart = lineStartList.get(idx);
        int lineEnd;
        if (idx != lineStartList.size()-1) {
            lineEnd = lineStartList.get(idx+1);
        } else {
            lineEnd = getStorage().size();
        }
        CharBuffer cb = getStorage().getReadBuffer(lineStart,
            lineEnd - lineStart).asCharBuffer();

        char chars[] = new char[cb.limit()];
        cb.get (chars);
        return new String (chars);
    }

    public boolean isLineStart (int chpos) {
        int bpos = toByteIndex(chpos);
        return lineStartList.contains (bpos);
    }

    /**
     * Returns the length of the specified lin characters.
     *
     * @param idx A getLine number
     * @return The number of characters
     */
    public int length (int idx) {
        if (isDisposed() || isTrouble()) {
            return 0;
        }
        if (lineStartList.size() == 0) {
            return 0;
        }
        int lineStart = lineStartList.get(idx);
        int lineEnd;
        if (idx != lineStartList.size()-1) {
            lineEnd = lineStartList.get(idx+1);
        } else {
            lineEnd = getStorage().size();
        }
        return toCharIndex(lineEnd - lineStart);
    }

    /**
     * Get the <strong>character</strong> index of a getLine as a position in
     * the output file.
     */
    public int getLineStart (int line) {
        if (isDisposed() || isTrouble()) return 0;
        return toCharIndex(lineStartList.get(line));
    }

    /** Get the getLine number of a <strong>character</strong> index in the
     * file (as distinct from a byte position)
     */
    public int getLineAt (int position) {
        if (isDisposed() || isTrouble()) return -1;
        int bytePos = toByteIndex (position);
        int i = lineStartList.indexOf (bytePos);
        if (i != -1) {
            return i;
        }
        i = lineStartList.findNearest(bytePos);
        return i;
    }

    public int getLineCount() {
        if (isDisposed() || isTrouble()) return 0;
        return lineStartList.size();
    }

    public OutputListener getListenerForLine (int line) {
        return (OutputListener) linesToListeners.get(line);
    }

    public int firstListenerLine () {
        if (isDisposed() || isTrouble()) return -1;
        return linesToListeners.isEmpty() ? -1 : linesToListeners.first();
    }

    public int nearestListenerLine (int line, boolean backward) {
        if (isDisposed() || isTrouble()) return -1;
        return linesToListeners.nearest (line, backward);
    }

    public int getLongestLineLength() {
        return toCharIndex(longestLine);
    }


     public void toLogicalLineIndex (final int[] physIdx, int charsPerLine) {
         int physicalLine = physIdx[0];
         physIdx[1] = 0;

         if (physicalLine == 0) {
             //First getLine never has lines above it
             physIdx[1] = 0;
             physIdx[2] = (length(physicalLine) / charsPerLine);
         }

         if (charsPerLine >= getLongestLineLength() || (getLineCount() <= 1)) {
             //The doc is empty, or there are no lines long enough to wrap anyway
             physIdx[1] = 0;
             physIdx[2] = 1;
             return;
         }

         int logicalLine =
             findFirstLineWithoutMoreLinesAboveItThan (physicalLine, charsPerLine);

         int linesAbove = getLogicalLineCountAbove(logicalLine, charsPerLine);

         int len = length(logicalLine);

         int wrapCount = len > charsPerLine ? (len / charsPerLine) + 1 : 1;

         physIdx[0] = logicalLine;
         int lcount = linesAbove + wrapCount;
         physIdx[1] = (wrapCount - (lcount - physicalLine));

         physIdx[2] = wrapCount;
     }

     /**
      * Uses a divide-and-conquer approach to quickly locate a getLine which has
      * the specified number of logical lines above it.  For large output, this
      * data is cached in OutWriter in a sparse int array.  This method is called
      * from viewToModel, so it must be very, very fast - it may be called once
      * every time the mouse is moved, to determine if the cursor should be
      * updated.
      */
     private int findFirstLineWithoutMoreLinesAboveItThan (int target, int charsPerLine) {
         int start = 0;
         int end = getLineCount();
         int midpoint = start + ((end - start) >> 1);
         int linesAbove = getLogicalLineCountAbove(midpoint, charsPerLine);
         int result = divideAndConquer (target, start, midpoint, end, charsPerLine, linesAbove);

         return Math.min(end, result) -1;
     }
     /**
      * Recursively search for the getLine number with the smallest number of lines
      * above it, greater than the passed target number of lines.  This is
      * effectively a binary search - divides the range of lines in half and
      * checks if the middle value is greater than the target; then recurses on
      * itself with whatever half of the range of lines has a better chance at
      * containing a smaller value.
      * <p>
      * It is primed with an initial call with the start, midpoint and end values.
      */
     private int divideAndConquer (int target, int start, int midpoint, int end, int charsPerLine, int midValue) {
         //We have an exact match - we're done
         if (midValue == target) {
             return midpoint + 1;
         }

         //In any of these conditions, the search has run out of gas - the
         //end value must be the match
         if (end - start <= 1 || midpoint == start || midpoint == end) {
             return end;
         }

         if (midValue > target) {
             //The middle value is greater than the target - look for a closer
             //match between the first and the middle getLine

             int upperMidPoint = start + ((midpoint - start) >> 1);
             if ((midpoint - start) % 2 != 0) {
                 upperMidPoint++;
             }
             int upperMidValue = getLogicalLineCountAbove(upperMidPoint, charsPerLine);
             return divideAndConquer (target, start, upperMidPoint, midpoint, charsPerLine, upperMidValue);
         } else {
             //The middle value is less than the target - look for a match
             //between the midpoint and the last getLine

             int lowerMidPoint = ((end - start) >> 2) + midpoint;
             if ((end - midpoint) % 2 != 0) {
                 lowerMidPoint++;
             }
             int lowerMidValue = getLogicalLineCountAbove(lowerMidPoint, charsPerLine);
             return divideAndConquer (target, midpoint, lowerMidPoint, end, charsPerLine, lowerMidValue);
         }
     }


    /**
     * Get the number of logical lines if character wrapped at the specified
     * width.  Calculates on the fly below 100000 characters, above that builds
     * a cache on the first call and uses that.
     */
    public int getLogicalLineCountAbove (int line, int charCount) {
        if (line == 0) {
            return 0;
        }
        if (toByteIndex(charCount) > longestLine) {
            return line;
        }

        //See OutputDocumentTest.testWordWrapping
        if (unitTestUseCache != null) {
            if (Boolean.TRUE.equals(unitTestUseCache)) {
                return dynLogicalLineCountAbove(line, charCount);
            } else {
                return cachedLogicalLineCountAbove (line, charCount);
            }
        }

        if (getStorage().size() < 100000) {
            return dynLogicalLineCountAbove(line, charCount);
        } else {
            return cachedLogicalLineCountAbove (line, charCount);
        }
    }

    /**
     * Get the number of logical lines above a given physical getLine if character
     * wrapped at the specified
     * width.  Calculates on the fly below 100000 characters, above that builds
     * a cache on the first call and uses that.
     */
    public int getLogicalLineCountIfWrappedAt (int charCount) {
        if (toByteIndex(charCount) > longestLine) {
            return getLineCount();
        }

        if (unitTestUseCache != null) {
            if (Boolean.TRUE.equals(unitTestUseCache)) {
                return dynLogicalLineCountIfWrappedAt(charCount);
            } else {
                return cachedLogicalLineCountIfWrappedAt(charCount);
            }
        }

        if (getStorage().size() < 100000) {
            return dynLogicalLineCountIfWrappedAt(charCount);
        } else {
            return cachedLogicalLineCountIfWrappedAt(charCount);
        }
    }

    public void addListener (int line, OutputListener l) {
        linesToListeners.put(line, l);
    }

    /**
     * A minor hook to let unit tests decide if caching is on or off.
     * @param val
     */
    static void unitTestUseCache (Boolean val) {
        unitTestUseCache = val;
    }

    private int cachedLogicalLineCountAbove (int line, int charCount) {
        if (charCount != knownCharCount || knownLogicalLineCounts == null) {
            knownCharCount = charCount;
            calcCharCounts(charCount);
        }
        return knownLogicalLineCounts.get(line);
    }

    private int cachedLogicalLineCountIfWrappedAt (int charCount) {
        int lineCount = getLineCount();
        if (charCount == 0 || lineCount == 0) {
            return 0;
        }
        synchronized (readLock()) {
            if (charCount != knownCharCount || knownLogicalLineCounts == null) {
                knownCharCount = charCount;
                calcCharCounts(charCount);
            }
            int result = knownLogicalLineCounts.get(lineCount-1);
            int len = length (lineCount - 1);
            result += (len / charCount) + 1;
            return result;
        }
    }

    /**
     * Builds a cache of lines which are longer than the last known width, which
     * can be retained for future lookups.  Finding the logical position of a
     * getLine when wrapped means iterating all the lines above it.  Above a
     * threshold, it is much preferable to cache it.  We use SparseIntList to
     * create a cache which only actually holds the counts for lines that *are*
     * wrapped, and interpolates the rest, so we don't need to create an int[]
     * as big as the number of lines we have.  This presumes that most lines
     * don't wrap.
     */
    private void calcCharCounts(int width) {
        synchronized (readLock()) {
            int lineCount = getLineCount();
            knownLogicalLineCounts = new SparseIntList(30);

            int val = 0;
            for (int i=1; i < lineCount; i++) {
                int len = length(i);

                if (len > width) {
                    val += (len / width) + 1;
                    knownLogicalLineCounts.add(val, i);
                } else {
                    val++;
                }
            }

            knownCharCount = width;
        }
    }

    /**
     * Gets the number of lines the document will require if getLine wrapped at the
     * specified character index.
     */

    private int dynLogicalLineCountIfWrappedAt (int charCount) {

        synchronized (readLock()) {
            int bcount = toByteIndex(charCount);
            if (longestLine <= bcount) {
                return lineStartList.size();
            }
            if (charCount == lastCharCountForWrapCalculation && lastWrappedLineCount != -1) {
                return lastWrappedLineCount;
            }
            if (lineStartList.size() == 0) {
                return 0;
            }
            int max = lineStartList.size();
            int prev = 0;
            int lineCount = 1;
            for (int i=1; i < max; i++) {
                int curr = lineStartList.get(i);
                if (curr - prev > bcount) {
                    lineCount += (((curr - prev) / bcount) + 1);
                } else {
                    lineCount++;
                }
                prev = curr;
            }
            setLastCharCountForWrapCalculation(charCount);
            lastWrappedLineCount = lineCount;
            return lineCount;
        }
    }

    /**
     * Gets the number of lines that occur *above* a given getLine if wrapped at the
     * specified char count.
     *
     * @param line The getLine in question
     * @param charCount The number of characters at which to wrap
     * @return The number of logical wrapped lines above the passed getLine
     */
    private int dynLogicalLineCountAbove (int line, int charCount) {
        int lineCount = getLineCount();
        if (line == 0 || lineCount == 0) {
            return 0;
        }
        if (charCount == lastCharCountForWrapAboveCalculation && lastWrappedAboveLineCount != -1 && line == lastWrappedAboveLine) {
            return lastWrappedAboveLineCount;
        }

        synchronized (readLock()) {
            lastWrappedAboveLineCount = 0;

            for (int i=0; i < line; i++) {
                int len = length(i);
                if (len > charCount) {
                    lastWrappedAboveLineCount += (len / charCount) + 1;
                } else {
                    lastWrappedAboveLineCount++;
                }
            }

            lastWrappedAboveLine = line;
            setLastCharCountForWrapAboveCalculation(charCount);
            return lastWrappedAboveLineCount;
        }
    }

    public void lineWritten(int start, int lineLength) {
        if (Controller.log) Controller.log ("AbstractLines.lineWritten " + start + " to " + lineLength);
        synchronized (readLock()) {
            setLastWrappedLineCount(-1);
            setLastCharCountForWrapAboveCalculation(-1);
            longestLine = Math.max (longestLine, lineLength);
            lineStartList.add (start);
            matcher = null;

            //If we already have enough lines that we need to cache logical getLine
            //lengths, update the cache - rebuilding it is very expensive
            if (knownLogicalLineCounts != null) {
                //This is the index of the getLine we just added
                int lastline = lineStartList.size()-1;
                //Get the length of the getLine
                int len = length(lastline);

                //We only need to add if it will wrap - SparseIntList's get()
                //semantics takes care of non-wrapped lines
                if (len > knownCharCount) {
                    int aboveLineCount;
                    if (knownLogicalLineCounts.lastIndex() != -1) {
                        //If the cache already has some entries, calculate the
                        //values from the last entry - this is less expensive
                        //than looking it up
                        aboveLineCount = (lastline - (knownLogicalLineCounts.lastIndex() + 1)) + knownLogicalLineCounts.lastAdded();
                    } else {
                        //Otherwise, it's just the number of lines above this
                        //one - it's the first entry
                        aboveLineCount = Math.max(0, lastline-1);
                    }
                    //Add in the number of times this getLine will wrap
                    aboveLineCount += (len / knownCharCount) + 1;
                    knownLogicalLineCounts.add(aboveLineCount, lastline);
                }
            }
        }
    }

    /** Convert an index from chars to byte count (*2).  Simple math, but it
     * makes the intent clearer when encountered in code */
    static int toByteIndex (int charIndex) {
        return charIndex << 1;
    }

    /** Convert an index from bytes to chars (/2).  Simple math, but it
     * makes the intent clearer when encountered in code */
    static int toCharIndex (int byteIndex) {
        assert byteIndex % 2 == 0 : "bad index: " + byteIndex;  //NOI18N
        int result = byteIndex >> 1;
        return result;
    }

    public void saveAs(String path) throws IOException {
        if (getStorage()== null) {
            throw new IOException ("Data has already been disposed"); //NOI18N
        }
        File f = new File (path);
        CharBuffer cb = getStorage().getReadBuffer(0, getStorage().size()).asCharBuffer();

        FileOutputStream fos = new FileOutputStream (f);
        String encoding = System.getProperty ("file.encoding"); //NOI18N
        if (encoding == null) {
            encoding = "UTF-8"; //NOI18N
        }
        Charset charset = Charset.forName (encoding); //NOI18N
        CharsetEncoder encoder = charset.newEncoder ();
        ByteBuffer bb = encoder.encode (cb);
        FileChannel ch = fos.getChannel();
        ch.write(bb);
        ch.close();
    }

    private String lastSearchString = null;
    private Matcher matcher = null;
    public Matcher getForwardMatcher() {
        return matcher;
    }

    public Matcher getReverseMatcher() {
        try {
            Storage storage = getStorage();
            if (storage == null) {
                return null;
            }
            if (matcher != null && lastSearchString != null && lastSearchString.length() > 0 && storage.size() > 0) {
                StringBuffer sb = new StringBuffer (lastSearchString);
                sb.reverse();
                CharBuffer buf = storage.getReadBuffer(0, storage.size()).asCharBuffer();
                //This could be very slow for large amounts of data
                StringBuffer data = new StringBuffer (buf.toString());
                data.reverse();

                Pattern pat = Pattern.compile (sb.toString());
                return pat.matcher(data);
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
        return null;
    }

    public Matcher find(String s) {
        if (Controller.log) Controller.log (this + ": Executing find for string " + s + " on " );
        Storage storage = getStorage();
        if (storage == null) {
            return null;
        }
        if (matcher != null && s.equals(lastSearchString)) {
            return matcher;
        }
        try {
            int size = storage.size();
            if (size > 0) {
                Pattern pat = Pattern.compile (s, Pattern.CASE_INSENSITIVE);
                CharBuffer buf = storage.getReadBuffer(0, size).asCharBuffer();
                Matcher m = pat.matcher(buf);
                if (!m.find(0)) {
                    return null;
                }
                matcher = m;
                lastSearchString = s;
                return matcher;
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
        return null;
    }

    private void setLastCharCountForWrapCalculation(int lastCharCountForWrapCalculation) {
        this.lastCharCountForWrapCalculation = lastCharCountForWrapCalculation;
    }

    private void setLastWrappedLineCount(int lastWrappedLineCount) {
        this.lastWrappedLineCount = lastWrappedLineCount;
    }

    private void setLastCharCountForWrapAboveCalculation(int lastCharCountForWrapAboveCalculation) {
        this.lastCharCountForWrapAboveCalculation = lastCharCountForWrapAboveCalculation;
    }

    public String toString() {
        return lineStartList.toString();
    }
}
