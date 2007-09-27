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

package org.netbeans.modules.cnd.dwarfdump.dwarf;

import org.netbeans.modules.cnd.dwarfdump.section.FileEntry;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 *
 * @author ak119685
 */
public class DwarfStatementList {
    public long total_length;
    public int version;
    public long prologue_length;
    public int minimum_instruction_length;
    public int default_is_stmt;
    public int line_base;
    public int line_range;
    public int opcode_base;
    public long[] standard_opcode_lengths;
    public ArrayList<String> includeDirs = new ArrayList<String>();
    public ArrayList<FileEntry> fileEntries = new ArrayList<FileEntry>();    
    
    private long offset;
    

    /** Creates a new instance of DwarfStatementList */
    public DwarfStatementList(long offset) {
        this.offset = offset;
    }

    public ArrayList<String> getIncludeDirectories() {
        return includeDirs;
    }

    public ArrayList<String> getFilePaths() {
        ArrayList<String> result = new ArrayList<String>();
        
        for (int idx = 1; idx <= fileEntries.size(); idx++) {
            String filepath = getFilePath(idx);
            if (filepath != null) {
                result.add(filepath);
            }
        }
        
        return result;
    }
    
    public ArrayList<FileEntry> getFileEntries() {
        return fileEntries;
    }
    
    public void dump(PrintStream out) {
        out.printf("\nPrologue Statement List (offset = %d [0x%08x]):\n\n", offset, offset); // NOI18N
        out.printf("  %-28s %s\n", "Length:", total_length); // NOI18N
        out.printf("  %-28s %s\n", "DWARF Version:", version); // NOI18N
        out.printf("  %-28s %s\n", "Prologue Length:", prologue_length); // NOI18N
        out.printf("  %-28s %s\n", "Minimum Instruction Length:", minimum_instruction_length); // NOI18N
        out.printf("  %-28s %s\n", "Initial value of 'is_stmt':", default_is_stmt); // NOI18N
        out.printf("  %-28s %s\n", "Line Base:", line_base); // NOI18N
        out.printf("  %-28s %s\n", "Line Range:", line_range); // NOI18N
        out.printf("  %-28s %s\n", "Opcode Base:", opcode_base); // NOI18N
        
        out.println("\n Opcodes:"); // NOI18N
        
        for (int i = 0; i < standard_opcode_lengths.length; i++) {
            out.printf("  Opcode %d has %d args\n", i + 1, standard_opcode_lengths[i]); // NOI18N
        }
        
        out.println("\n The Directory Table:\n"); // NOI18N
        
        int idx = 0;
        
        out.println(" Entry Path"); // NOI18N
        for (String includeDir : includeDirs) {
            out.printf(" %-6d%s\n", ++idx, includeDir); // NOI18N
        }
        
        out.println("\n The File Name Table:\n"); // NOI18N
        out.println(" Entry Dir   Time  Size  Name"); // NOI18N

        idx = 0;
        for (FileEntry fileEntry : fileEntries) {
            out.printf(" %-6d%-6d%-6d%-6d%s\n", ++idx, fileEntry.dirIndex, fileEntry.modifiedTime, fileEntry.fileSize, fileEntry.fileName); // NOI18N
        }
    }
    
    public int getDirectoryIndex(String dirname) {
        int idx = 0;
        
        if (dirname == null) {
            return 0;
        }
                
        for (String dir : includeDirs) {
            idx++;
            if (dir.equals(dirname)) {
                return idx;
            }
        }
        
        return 0;
    }

    public int getFileEntryIdx(String fileName) {
        File file = new File(fileName);
        String dirname = file.getParent();
        String fname = file.getName();
        return getFileEntryIdx(getDirectoryIndex(dirname), fname);
    }

    private int getFileEntryIdx(int dirIdx, String fname) {
        int idx = 0;
        
        for (FileEntry fileEntry : fileEntries) {
            idx++;
            if (fileEntry.dirIndex == dirIdx && fileEntry.fileName.equals(fname)) { 
                return idx;
            }
        }
        
        return 0;
    }
    
    public String getFilePath(int idx) {
        FileEntry fileEntry = fileEntries.get(idx - 1);
        
        if (fileEntry == null || fileEntry.fileName.equals("<internal>") || fileEntry.fileName.equals("<built-in>")) { // NOI18N
            return null;
        }

        String result;
        
        if (fileEntry.dirIndex == 0) {
            result = "." + File.separator + fileEntry.fileName; // NOI18N
        } else {
            result = includeDirs.get(fileEntry.dirIndex - 1) + File.separator + fileEntry.fileName;
        }        
        
        return result;
    }

    public ArrayList<String> getPathsForFile(String fname) {
        ArrayList<String> result = new ArrayList<String>();
        
        for (FileEntry fileEntry : fileEntries) {
            if (fileEntry.fileName.equals(fname)) {
                String dir = (fileEntry.dirIndex == 0) ? "." : includeDirs.get(fileEntry.dirIndex - 1); // NOI18N
                result.add(dir);
            }
        }
        
        return result;
    }
}
