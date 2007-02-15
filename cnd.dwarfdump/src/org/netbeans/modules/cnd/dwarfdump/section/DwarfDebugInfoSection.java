/*
 * DwarfDebugInfoSection.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.dwarfdump.elf.SectionHeader;
import org.netbeans.modules.cnd.dwarfdump.reader.ElfReader;

/**
 *
 * @author ak119685
 */
public class DwarfDebugInfoSection extends ElfSection {
    List<CompilationUnit> compilationUnits = new ArrayList<CompilationUnit>();
    
    public DwarfDebugInfoSection(DwarfReader reader, int sectionIdx) {
        super(reader, sectionIdx);
    }

    public DwarfDebugInfoSection(ElfReader reader, int sectionIdx, SectionHeader header, String sectionName) {
        super(reader, sectionIdx, header, sectionName);
    }
    
    public int getCompilationUnitsNumber() {
        List<CompilationUnit> compilationUnits = null;
        
        try {
            compilationUnits = getCompilationUnits();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return (compilationUnits == null) ? 0 : compilationUnits.size();
    }
    
    public CompilationUnit getCompilationUnit(long unit_offset) {
        for (CompilationUnit unit : compilationUnits) {
            if (unit.unit_offset == unit_offset) {
                return unit;
            }
        }
        
        return null;
    }
    
    public List<CompilationUnit> getCompilationUnits() throws IOException {
        if (compilationUnits.size() == 0) {
            int cuOffset = 0;
            while (cuOffset != header.sh_size) {
                ((DwarfReader)reader).seek(header.getSectionOffset() + cuOffset);
                if (reader.readDWlen()==0) {
                    break;
                }
                CompilationUnit unit = new CompilationUnit((DwarfReader)reader, header.getSectionOffset(), cuOffset);
                compilationUnits.add(unit);
                cuOffset += unit.getUnitTotalLength();
            }
        }
        
        
        return compilationUnits;
    }
    
    public void dump(PrintStream out) {
        try {
            for (CompilationUnit unit : getCompilationUnits()) {
                unit.dump(out);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
