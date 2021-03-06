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

package org.netbeans.modules.cnd.dwarfdump.elf;

import java.io.PrintStream;

/**
 *
 * @author ak119685
 */
public class SectionHeader {
    public String name;            /* section name */
    public long sh_name = 0;       /* section name */
    public long sh_type = 0;       /* SHT_... */
    public long sh_flags = 0;      /* SHF_... */
    public long sh_addr = 0;       /* x virtual address */
    public long sh_offset = 0;     /* x file offset */
    public long sh_size = 0;       /* x section size */
    public long sh_link = 0;       /* misc info */
    public long sh_info = 0;       /* misc info */
    public long sh_addralign = 0;  /* x memory alignment */
    public long sh_entsize = 0;    /* x entry size if table */

    public long getSectionSize() {
        return sh_size;
    }
    
    public long getSectionOffset() {
        return sh_offset;
    }
    
    public long getSectionEntrySize() {
        return sh_entsize;
    }
    
    public String getSectionName(){
        return name;
    }
    
    public void dump(PrintStream out) {
        out.println("Elf section header:"); // NOI18N
        out.printf("  %-20s %s\n", "Offset:", sh_offset); // NOI18N
        out.printf("  %-20s %s\n", "Length:", sh_size); // NOI18N
        out.printf("  %-20s %s\n", "Memory alignment:", sh_addralign); // NOI18N
        out.println();
    }
}
