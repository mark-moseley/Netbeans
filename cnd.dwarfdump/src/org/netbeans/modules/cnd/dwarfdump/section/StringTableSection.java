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

/*
 * StringTableSection.java
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import org.netbeans.modules.cnd.dwarfdump.reader.ElfReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 *
 * @author ak119685
 */
public class StringTableSection extends ElfSection {
    byte[] stringtable = null;
    
    public StringTableSection(ElfReader reader, int sectionIdx) {
        super(reader, sectionIdx);
        read();
    }

    public StringTableSection(ElfReader reader, byte[] stringtable) {
        super(null, 0, null, null);
        this.stringtable = stringtable;
    }
    
    @Override
    public StringTableSection read() {
        try {
            long filePos = reader.getFilePointer();
            reader.seek(header.getSectionOffset());
            stringtable = new byte[(int)header.getSectionSize()];
            reader.read(stringtable);
            reader.seek(filePos);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return this;
    }
    
    public byte[] getStringTable() {
        return stringtable;
    }
    
    public String getString(long offset) {
        StringBuilder str = new StringBuilder();
        
        for (int i = (int)offset; i < stringtable.length; i++) {
            if (stringtable[i] == 0) {
                break;
            }
            str.append((char)stringtable[i]);
        }
        
        return str.toString();
    }
    
    @Override
    public void dump(PrintStream out) {
        super.dump(out);
        
        if (stringtable == null) {
            out.println("<Empty table>"); // NOI18N
            return;
        }
        
        int offset = 1;
        int idx = 0;

        out.printf("No.\tOffset\tString\n"); // NOI18N
        
        while (offset < stringtable.length) {
            String string = getString(offset);
            out.printf("%d.\t%d\t%s\n", ++idx, offset, string); // NOI18N
            offset += string.length() + 1;
        }
    }
}
