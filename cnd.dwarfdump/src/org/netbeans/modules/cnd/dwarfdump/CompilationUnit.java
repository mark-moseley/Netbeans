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

package org.netbeans.modules.cnd.dwarfdump;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfAbbriviationTable;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfAbbriviationTableEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfMacinfoTable;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfNameLookupTable;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfStatementList;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ATTR;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.SECTIONS;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfAbbriviationTableSection;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfAttribute;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfLineInfoSection;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfMacroInfoSection;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfNameLookupTableSection;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ElfConstants;

/**
 *
 * @author ak119685
 */
public class CompilationUnit {
    private DwarfReader reader;
    
    public long debugInfoSectionOffset;
    public long unit_offset;
    public long unit_length;
    public long unit_total_length;
    public int  version;
    public long debug_abbrev_offset;
    public long info_offset;
    public byte address_size;
    public DwarfEntry root = null;
    
    private DwarfAbbriviationTable abbr_table = null;
    private DwarfStatementList statement_list = null;
    private DwarfMacinfoTable macrosTable = null;
    private DwarfNameLookupTable pubnamesTable = null;
    private long debugInfoOffset;
    
    private Map<Long, Long> specifications = new HashMap<Long, Long>();
    private Map<Long, DwarfEntry> entries = new HashMap<Long, DwarfEntry>();
    
    /** Creates a new instance of CompilationUnit */
    public CompilationUnit(DwarfReader reader, long sectionOffset, long unitOffset) throws IOException {
        this.reader = reader;
        this.debugInfoSectionOffset = sectionOffset;
        this.unit_offset = unitOffset;
        readCompilationUnitHeader();
        root = getDebugInfo(false);
    }
    
    public String getProducer() {
        return (String)root.getAttributeValue(ATTR.DW_AT_producer);
    }
    
    public String getCompilationDir() {
        return (String)root.getAttributeValue(ATTR.DW_AT_comp_dir);
    }
    
    public String getSourceFileName() {
        return (String)root.getAttributeValue(ATTR.DW_AT_name);
    }
    
    public String getCommandLine() {
        Object cl = root.getAttributeValue(ATTR.DW_AT_SUN_command_line);
        return (cl == null) ? null : (String)cl;
    }
    
    public String getSourceFileFullName() {
        String result = null;
        
        try {
            String dir = getCompilationDir();
            String name = getSourceFileName();
            if (dir != null) {
                if (name.startsWith("/")) { // NOI18N
                    result = new File(name).getCanonicalPath();
                } else {
                    result = new File(dir + File.separator + name).getCanonicalPath();
                }
            } else {
                result = new File(name).getCanonicalPath();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return result;
    }

    public String getSourceFileAbsolutePath() {
        String result = null;
        
        String dir = getCompilationDir();
        String name = getSourceFileName();
        if (dir != null) {
            if (name.startsWith("/")) { // NOI18N
                result = new File(name).getAbsolutePath();
            } else {
                result = new File(dir + File.separator + name).getAbsolutePath();
            }
        } else {
            result = new File(name).getAbsolutePath();
        }
        
        return result;
    }
    
    public String getSourceLanguage() {
        if (root != null) {
            Object lang = root.getAttributeValue(ATTR.DW_AT_language);
            if (lang != null) {
                return lang.toString();
            }
        }
        return null;
    }
    
    public String getType(DwarfEntry entry) {
        TAG entryKind = entry.getKind();
        
        if (entryKind.equals(TAG.DW_TAG_unspecified_parameters)) {
            return "null"; // NOI18N
        }
        
        Integer typeRef = (Integer)entry.getAttributeValue(ATTR.DW_AT_type);
        
        if (typeRef == null) {
            return "void"; // NOI18N
        }
        
        DwarfEntry typeEntry = getEntry(typeRef);
        TAG kind = typeEntry.getKind();
        
        if (kind.equals(TAG.DW_TAG_base_type)) {
            String name = typeEntry.getName();
            
            // TODO: Is it OK?
            if (name.equals("long unsigned int")) { // NOI18N
                name = "unsigned long"; // NOI18N
            } else if (name.equals("long int")) { // NOI18N
                name = "long"; // NOI18N
            }
            
            return name;
        }
        
        if (kind.equals(TAG.DW_TAG_structure_type) ||
                kind.equals(TAG.DW_TAG_enumeration_type) ||
                kind.equals(TAG.DW_TAG_union_type) ||
                kind.equals(TAG.DW_TAG_typedef) ||
                kind.equals(TAG.DW_TAG_class_type)) {
            return typeEntry.getName();
        }
        
        if (kind.equals(TAG.DW_TAG_const_type)) {
            // TODO: Check algorithm!
            
            Object atType = typeEntry.getAttributeValue(ATTR.DW_AT_type);
            
            if (atType == null) {
                return "const void"; // NOI18N
            }
            
            if (atType instanceof Integer) {
                Integer constTypeRef = (Integer)typeEntry.getAttributeValue(ATTR.DW_AT_type);
                
                DwarfEntry refTypeEntry = getEntry(constTypeRef);
                typeEntry.getKind();
                if (refTypeEntry.getKind().equals(TAG.DW_TAG_reference_type) ||
                        refTypeEntry.getKind().equals(TAG.DW_TAG_array_type)) {
                    return getType(typeEntry);
                } else {
		    if( refTypeEntry.getKind() == TAG.DW_TAG_pointer_type ) {
			return getType(typeEntry) + " const"; // NOI18N
		    }
		    else {
			return "const " + getType(typeEntry); // NOI18N
		    }
                }
            }
            
//            return "const " + getType(typeEntry); // NOI18N
            
        }
        
        if (kind.equals(TAG.DW_TAG_reference_type)) {
            return getType(typeEntry) + "&"; // NOI18N
        }
        
        if (kind.equals(TAG.DW_TAG_array_type)) {
            return getType(typeEntry) + "[]"; // NOI18N
        }
        
        if (kind.equals(TAG.DW_TAG_pointer_type) || kind.equals(TAG.DW_TAG_ptr_to_member_type)) {
            return getType(typeEntry) + "*"; // NOI18N
        }
        
        if (kind.equals(TAG.DW_TAG_subroutine_type)) {
            return getType(typeEntry);
        }
        
        if (kind.equals(TAG.DW_TAG_volatile_type)) {
            return getType(typeEntry);
        }
        
        if (kind.equals(TAG.DW_TAG_union_type)) {
            return getType(typeEntry);
        }
        
        return "<" + kind + ">"; // NOI18N
    }
    
    public DwarfEntry getEntry(long sectionOffset) {
        //return entryLookup(getDebugInfo(true), sectionOffset);
        DwarfEntry entry = entries.get(sectionOffset);
        
        if (entry == null) {
            entry = entryLookup(getDebugInfo(true), sectionOffset);
            entries.put(sectionOffset, entry);
        }
        
        return entry;
    }
    
    public DwarfEntry getDefinition(DwarfEntry entry) {
        Long ref = specifications.get(entry.getRefference());
        if( ref != null ) {
            return getEntry(ref);
        }
        return null;
    }
    
    private DwarfEntry entryLookup(DwarfEntry entry, long refference) {
        if (entry == null) {
            return null;
        }
        
        if (entry.getRefference() == refference) {
            return entry;
        }
        
        for (DwarfEntry child : entry.getChildren()) {
            DwarfEntry res = entryLookup(child, refference);
            if (res != null) {
                return res;
            }
        }
        
        return null;
    }
    
    public DwarfEntry getRoot() {
        return root;
    }
    
    public DwarfEntry getTypedefFor(Integer typeRef) {
        // TODO: Rewrite not to iterate every time.
        
        for (DwarfEntry entry : getDebugInfo(true).getChildren()) {
            if (entry.getKind().equals(TAG.DW_TAG_typedef)) {
                Object entryTypeRef = entry.getAttributeValue(ATTR.DW_AT_type);
                if (entryTypeRef != null && ((Integer)entryTypeRef).equals(typeRef)) {
                    return entry;
                }
            }
        }
        
        return null;
    }
    
    /**
     * unit_length represents the length of the .debug_info contribution for
     * this compilation unit, not including the length field itself. So this
     * method returns unit_length + sizeof(unit_length field). I.e. 4 or 4 + 8.
     * @return the total bytes number occupied by this CU.
     */
    
    public long getUnitTotalLength() {
        return unit_total_length;
    }
    
    private void readCompilationUnitHeader() throws IOException {
        reader.seek(debugInfoSectionOffset + unit_offset);
        
        unit_length         = reader.readDWlen();
        // The total length of this CU is unit_lenght + sizeof(unit_lenght field).
        
        long pos = reader.getFilePointer();
        unit_total_length = unit_length + pos - (debugInfoSectionOffset + unit_offset);
        
        version             = reader.readShort();
        debug_abbrev_offset = reader.read3264();
        address_size        = (byte)(0xff & reader.readByte());
        
        // GNU writes debug info using 32-bit mode even in elf64
        // It's a hack. Check if we have a meaningful address_size. 
        // If not and we are in 64-bit mode => try to fallback into 32-bit mode.
        if (address_size != 4 && address_size != 8 && reader.is64Bit()) {
            reader.setFileClass(ElfConstants.ELFCLASS32);
            reader.seek(reader.getFilePointer() - 9);
            debug_abbrev_offset = reader.read3264();
            address_size        = (byte)(0xff & reader.readByte());
        }
        
        debugInfoOffset = reader.getFilePointer();

        reader.setAddressSize(address_size);
        
        DwarfAbbriviationTableSection abbrSection = (DwarfAbbriviationTableSection)reader.getSection(SECTIONS.DEBUG_ABBREV);
        abbr_table = abbrSection.getAbbriviationTable(debug_abbrev_offset);
    }
    
    public DwarfStatementList getStatementList() {
        if (statement_list == null) {
            initStatementList();
        }
        
        return statement_list;
    }
    
    public DwarfMacinfoTable getMacrosTable() {
        if (macrosTable == null) {
            initMacrosTable();
        }
        
        return macrosTable;
    }
    
    private DwarfNameLookupTable getPubnamesTable() {
        if (pubnamesTable == null) {
            initPubnamesTable();
        }
        
        return pubnamesTable;
    }
    
    
    private DwarfEntry getDebugInfo(boolean readChildren) {
        if (root == null || (readChildren && root.getChildren().size() == 0)) {
            try {
                //getPubnamesTable();
                long currPos = reader.getFilePointer();
                reader.seek(debugInfoOffset);
                root = readEntry(0, readChildren);
                reader.seek(currPos);

                if (readChildren) {
                    setSpecializations(root);
                }
                
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        return root;
    }
    
    private void setSpecializations(DwarfEntry entry) {
        Object o = entry.getAttributeValue(ATTR.DW_AT_specification);
        
        if (o instanceof Integer) {
            specifications.put(new Long(((Integer) o).intValue()), entry.getRefference());
        }
        
        for (DwarfEntry child : entry.getChildren()) {
            setSpecializations(child);
        }
    }
    
    private DwarfEntry readEntry(int level, boolean readChildren) throws IOException {
        long refference = reader.getFilePointer() - debugInfoSectionOffset - unit_offset;
        long idx = reader.readUnsignedLEB128();
        
        if (idx == 0) {
            return null;
        }
        
        DwarfAbbriviationTableEntry abbreviationEntry = abbr_table.getEntry(idx);
        
        if (abbreviationEntry == null) {
            return null;
        }
        
        DwarfEntry entry = new DwarfEntry(this, abbreviationEntry, refference, level);
        entries.put(refference, entry);
        
        for (int i = 0; i < abbreviationEntry.getAttributesCount(); i++) {
            DwarfAttribute attr = abbreviationEntry.getAttribute(i);
            entry.addValue(reader.readAttrValue(attr));
        }

        if (readChildren == true && entry.hasChildren()) {
            DwarfEntry child;
            while ((child = readEntry(level + 1, true)) != null) {
                entry.addChild(child);
            }
        }
        
        return entry;
    }
    
    private void initStatementList() {
        DwarfLineInfoSection lineInfoSection = (DwarfLineInfoSection)reader.getSection(SECTIONS.DEBUG_LINE);
        
        if (root == null) {
            return;
        }
        
        Number statementListOffset = (Number)root.getAttributeValue(ATTR.DW_AT_stmt_list);
        if (statementListOffset != null) {
            statement_list = lineInfoSection.getStatementList(statementListOffset.longValue());
        }
    }
    
    private void initMacrosTable() {
        DwarfMacroInfoSection macroInfoSection = (DwarfMacroInfoSection)reader.getSection(SECTIONS.DEBUG_MACINFO); // NOI18N
        
        if (macroInfoSection == null) {
            return;
        }
        
        Integer macroInfoOffset = (Integer)root.getAttributeValue(ATTR.DW_AT_macro_info);
        
        if (macroInfoOffset == null) {
            return;
        }
        
        macrosTable = macroInfoSection.getMacinfoTable(macroInfoOffset);
    }
    
    private void initPubnamesTable() {
        DwarfNameLookupTableSection dwarfNameLookupTableSection = (DwarfNameLookupTableSection)reader.getSection(SECTIONS.DEBUG_PUBNAMES); 
        
        if (dwarfNameLookupTableSection != null) {
            pubnamesTable = dwarfNameLookupTableSection.getNameLookupTableFor(unit_offset);
        }
    }
    
    public List<DwarfEntry> getDeclarations() {
        return getDeclarations(true);
    }
    
    public List<DwarfEntry> getEntries() {
        // Read pubnames section first
        getPubnamesTable();
        return getDebugInfo(true).getChildren();
    }
    
    /**
     * Used to get a list of declarations defined/used in this CU.
     * @param limitedToFile <code>true</code> means return declarations defined in the current source file only
     * @return returns a list of declarations defined/used in this CU.
     */
    public List<DwarfEntry> getDeclarations(boolean limitedToFile) {
        boolean reportExcluded = false;
        int fileEntryIdx = 0;
        
        // make sure that pubnames table has been read ...
        getPubnamesTable();
        
        ArrayList<DwarfEntry> result = new ArrayList<DwarfEntry>();
        
        if (limitedToFile) {
            fileEntryIdx = getStatementList().getFileEntryIdx(getSourceFileName());
        }
        
        for (DwarfEntry child : getEntries()) {
            if ((!limitedToFile) || (limitedToFile && child.isEntryDefinedInFile(fileEntryIdx))) {
                // TODO: Check algorythm
                // Do not add definitions that have DW_AT_abstract_origin attribute.
                // Do not add entries that's names start with _GLOBAL__F | _GLOBAL__I | _GLOBAL__D
                
                if (!child.hasAbastractOrigin()) {
                    String qname = child.getQualifiedName();
                    if (qname != null && !qname.startsWith("_GLOBAL__")) { // NOI18N
                        result.add(child);
                    } else if (reportExcluded) {
                        System.out.println("Exclude declaration: " + child.getDeclaration()); // NOI18N
                    }
                }
            }
        }
        
        return result;
    }
    
    
    public void dump(PrintStream out) {
        if (root == null) {
            out.println("*** No compilation units for " + reader.getFileName()); // NOI18N
            return;
        }
        
        out.println("*** " + getSourceFileFullName() + " ***"); // NOI18N
        out.println("  Compilation Unit @ offset " + Long.toHexString(unit_offset) + ":"); // NOI18N
        out.println("    Length: " + unit_length); // NOI18N
        out.println("    Version: " + version); // NOI18N
        out.println("    Abbrev Offset: " + debug_abbrev_offset); // NOI18N
        out.println("    Pointer Size: " + address_size); // NOI18N
        
        /*
         * getPubnamesTable() will not only set pubnamesTable (if not set yet)
         * but also setup qualified names from appropriate pubnames table.
         */
        
        getPubnamesTable();
        
        getDebugInfo(true).dump(out);
        getStatementList().dump(out);
        
        // Still pubnamesTable could be null (if not present for this
        // Compilation Unit)
        
        if (pubnamesTable != null) {
            pubnamesTable.dump(out);
        }
        
        DwarfMacinfoTable macinfoTable = getMacrosTable();
        if( macinfoTable != null ) {
            macinfoTable.dump(out);
        }
        
        out.println();
    }
    
    
    
}
