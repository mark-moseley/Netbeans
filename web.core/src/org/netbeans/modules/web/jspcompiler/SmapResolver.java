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

package org.netbeans.modules.web.jspcompiler;

import java.util.*;
import java.io.IOException;

//TODO - add support for other tags, e.g. server-specific -> make sure they do not block this code

/**
 *
 * @author  mg116726
 */
public class SmapResolver {
    
    /** header of SMAP file - must be on the first line
     */
    private static final String SMAP_HEADER = "SMAP"; // NOI18N
    
    /** default stratum should be set to JSP
     */
    private static final String DEFAULT_STRATUM = "JSP"; //NOI18N
    
    /** this is how JSP stratum section beginning look like
     */
    private static final String STRATUM_SECTION = "*S JSP"; // NOI18N
    
    /** line section begins with this mark
     */
    private static final String LINE_SECTION = "*L"; // NOI18N
    
    /** file section begins with this mark
     */
    private static final String FILE_SECTION = "*F"; // NOI18N
        
    /** smap file ends with this mark
     */
    private static final String END_SECTION = "*E"; // NOI18N
    
    /** this hash sign divides fileid from line number
     */
    private static final String FID_DELIM = "#"; // NOI18N

    /** reader that was used to create this smapresolver
     */
    private SmapReader reader = null;
    
    /** this one is true only if the smap read by the reader has been successfully resolved 
     */
    private boolean resolved = false;
    
    /** default stratum set in the smap file
     */
    private String defaultStratum = null;
    
    /** outputFileName ==> servlet name set in the smap file
     */
    private String outputFileName = null;
    
    /** contains hashmap of fileid's & filenames in the jsp
     */
    private Hashtable fsection = new Hashtable(3);
    
    /** contains jsp -> servlet line mappings
     */
    private Map jsp2java = new TreeMap();
    
    /** contains servlet -> jsp line mappings
     */
    private Map java2jsp = new TreeMap();

    /** Creates a new instance of SmapResolver
     * @param reader reader provides readSmap() method which returns SMAP iformation as a String
     */
    public SmapResolver(SmapReader reader) {
        this.resolved = resolve(reader.readSmap());
        this.reader = reader;
    }

    public String toString() {
       return reader.toString();
    }
        
    /** Reads the smap file and stores all the data into corresponding variables and maps.
     * At the end calls sanitycheck to check whether the file has been resolved successfuly
     * @return true if resolved successfuly, false if not
     * @param smap SMAP information as a string
     */
    private boolean resolve(String smap) {
        
        boolean fileSection = false;        // whether we are in file section
        boolean lineSection = false;        // whether we are in line section
        boolean jspStratumSection = false;  // there may (theoretically) be more stratum sections in a file
        
        String currentSection = "";
        
        if (smap == null) return false;
        
        // tokenize the smap file by endlines
        StringTokenizer st = new StringTokenizer(smap, "\n", false);
        
        boolean beginning = true;
        int sectionCounter = 0; // counts items in the sections
        
        /** to which file current indexes belong (there are more of them - includes)
         */
        String fileIndex = null;
        
        boolean cont = true;
        
        while (st.hasMoreTokens() && cont) {
            String token = st.nextToken();
            
            //this tough IF..ELSE is responsible for tracking which section is currently read
            if (beginning) {         // SMAP file begins with 'SMAP' header
                if (!SMAP_HEADER.equals(token)) {
                    return false;
                }
                beginning = false;
                currentSection = SMAP_HEADER;
                continue;
            } else if (STRATUM_SECTION.equals(token)) {
                currentSection = STRATUM_SECTION;
                continue;
            } else if (FILE_SECTION.equals(token)) {
                currentSection = FILE_SECTION;
                sectionCounter = 0;
                continue;
            } else if (LINE_SECTION.equals(token)) {
                currentSection = LINE_SECTION;
                sectionCounter = 0;
                fileIndex = "0";
                continue;
            } else if (END_SECTION.equals(token)) {
                currentSection = END_SECTION;
                cont = false;
                sectionCounter = 0;
                break;
            }

            //read info from header
            if (SMAP_HEADER.equals(currentSection)) {
                if (sectionCounter == 0) {    // outputFileName
                    outputFileName = token;
                }
                if (sectionCounter == 1) {    // defaultStratum follows
                    defaultStratum = token;
                }
            }
            
            //read the file section
            if (FILE_SECTION.equals(currentSection)) {
                if (token.startsWith("+")) {
                    sectionCounter++;
                    storeFile(token, token = st.nextToken());
                } else {
                    storeFile(token, null);
                }
            }
            
            if (LINE_SECTION.equals(currentSection)) { 
                int hashPresent = token.indexOf(FID_DELIM);
                if (hashPresent > -1) { // there's a hash => there's a fileid indicator
                    fileIndex = token.substring(hashPresent + 1, token.indexOf(':'));
                }
                storeLine(token, fileIndex);
            }
            sectionCounter++;
        }
        
        //perform sanity check - report error (return false) if unsuccessful
        this.resolved = sanityCheck();
        return this.resolved;
    }
    
    /** stores file name and file index into the fsection map
     */
    private void storeFile(String token, String token2) {
//        System.err.println("storeFile: " + token + ", " + token2);
        if ((token != null) && (token.startsWith("+"))) {
            token = token.substring(2);
        }
        int spaceIndex = token.indexOf(" ");
        String id = token.substring(0, spaceIndex);
        String filename = token.substring(spaceIndex+1);
        fsection.put(id, filename);
    }

    /** stores line mappings into both java->jsp->java maps
     */
    private void storeLine(String token, String fileIndex) {
//        System.err.println("storeLine: " + token + ", " + fileIndex);
        int delimIndex = token.indexOf(":");
        
        String jspLine = token.substring(0, delimIndex);
        String javaLine = token.substring(delimIndex+1);
        
        int hashPresent = jspLine.indexOf(FID_DELIM);
        int commaPresent = jspLine.indexOf(',');

        int jspIndex = 0;    
        int repeatCount = 0;

        if (commaPresent != -1) {
            repeatCount = Integer.parseInt(jspLine.substring(commaPresent+1));
            if (hashPresent == -1) {
                jspIndex = Integer.parseInt(jspLine.substring(0, commaPresent));
            } else {
                jspIndex = Integer.parseInt(jspLine.substring(0, hashPresent));
            }
        } else {
            if (hashPresent == -1) {
                jspIndex = Integer.parseInt(jspLine);
            } else {
                jspIndex = Integer.parseInt(jspLine.substring(0, hashPresent));
            }
            repeatCount = 1;
        }
        
        commaPresent = javaLine.indexOf(',');
        
        int outputIncrement;
        int javaIndex;
        if (commaPresent != -1) {
            outputIncrement = Integer.parseInt(javaLine.substring(commaPresent+1));
            javaIndex = Integer.parseInt(javaLine.substring(0, commaPresent));
        } else {
            outputIncrement = 1;
            javaIndex = Integer.parseInt(javaLine);
        }
        
        for (int i=0; i < repeatCount; i++) {
            int jspL = jspIndex + i;
            int javaL = javaIndex + (i * outputIncrement);
            
            // fill in table for jsp->java mappings
            jspLine = Integer.toString(jspL).concat(FID_DELIM).concat(fileIndex);
            javaLine = Integer.toString(javaL);
            if (!jsp2java.containsKey(jspLine)) { // the first rule is the right one
                jsp2java.put(jspLine, javaLine);
            }
            
            // fill in table for java->jsp mappings
            jspLine = Integer.toString(jspL).concat("#").concat(fileIndex);
            javaLine = Integer.toString(javaL);
            if (!java2jsp.containsKey(javaLine)) { // the first rule is the right one
                java2jsp.put(javaLine, jspLine);
            }
        }
    }
    
    /** check whether the file has been resolved correctly
     */
    private boolean sanityCheck() {   
        if (!DEFAULT_STRATUM.equals(defaultStratum)) return false;
        if (!(outputFileName.endsWith(".java"))) return false;
        if (fsection.isEmpty()) return false;
        if (jsp2java.isEmpty()) return false;   // TODO: check how this is done for empty jsps
        if (java2jsp.isEmpty()) return false;   // TODO: check how this is done for empty jsps
        return true;
    }
    
    /** access file name by index in the SMAP
     * @param index Index of the file in the SMAP
     * @return filename
     */
    private String getFileNameByIndex(String index) {
        return (String)fsection.get(index);
    }
    
    /** access index by the filename
     * @param fname filename to find index for
     * @return index of the file in SMAP
     */
    private String getIndexByFileName(String fname) {
        Set s = fsection.entrySet();
        Iterator i = s.iterator();
        while (i.hasNext()) {
            Map.Entry mentry = (Map.Entry)i.next();
            String value = (String)mentry.getValue();
            if (value.equalsIgnoreCase(fname)) {
                return mentry.getKey().toString();
            }
        }
        return null;
    }

    /**
     * @return Whether the SMAP info file is resolved or not
     */
    public boolean isResolved() {
        return this.resolved;
    }
    
    /** 
     * get all the filenames in the SMAP
     */
    public Map getFileNames() {
        Hashtable h = new Hashtable(fsection.size());
        Collection c = fsection.values();
        Iterator i = c.iterator();
        int counter = 0;
        while (i.hasNext()) {
            h.put(new Integer(counter++), i.next());
        }
        return h;
    }
    
    /** 
     * get primary jsp filename
     */
    public String getPrimaryJspFileName() {
        TreeMap tm = new TreeMap(fsection);
        Object o = tm.firstKey();
        String s = (String)fsection.get(o);
        System.out.println("s: " + s);
        return s;
    }

    /** if there are included files in the jsp or not 
     */
    public boolean hasIncludedFiles() {
        return (fsection.size() > 1);
    }
    
    public String getJavaLineType(int line, int col) {
        //line type is not included in the SMAP mapping information - therefore this is not supported in JSR45
        return null;
    }
    
    /** whether the jsp file is completely empty - without any line
     */
    public boolean isEmpty() {
        return jsp2java.isEmpty();  // TODO - check if this really works
    }

    /** returns jsp name for corresponding servlet line
     */
    public String getJspFileName(int line, int col) throws IOException {
        String key = Integer.toString(line);
        String value = (String)java2jsp.get(key);
        if (value == null) return null;
        String index = value.substring(value.indexOf(FID_DELIM)+1);
        return getFileNameByIndex(index);
    }
    
    public int mangle(String jspFileName, int line, int col) {
        String fileIndex = getIndexByFileName(jspFileName);
        if (fileIndex == null) return -1;
        String key = "".concat(Integer.toString(line)).concat("#").concat(fileIndex);
        String value = (String)jsp2java.get(key);
        if (value == null) return -1;
        return Integer.parseInt(value);
    }

    public int unmangle(int line, int col) {
        String key = Integer.toString(line);
        String value = (String)java2jsp.get(key);
        if (value == null) return -1;
        int jspline = Integer.parseInt(value.substring(0, value.indexOf("#")));
        return jspline;
    }
    
//    public static void main(String[] args) {
//        SmapResolver sr = new SmapResolver(new SmapFileReader(
//            new java.io.File("/Users/mg116726/WebApplication1/build/generated/src/org/apache/jsp/index_jsp.class.smap")));
//        System.err.println("resolved: " + sr.isResolved());
//    }
}
