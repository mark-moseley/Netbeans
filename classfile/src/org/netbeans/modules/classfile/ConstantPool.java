/*
 * ConstantPool.java
 *
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 *
 * Contributor(s): Thomas Ball
 *
 * Version: $Revision$
 */

package org.netbeans.modules.classfile;

import java.io.*;
import java.util.*;

/**
 * Class representing a Java class file constant pool.
 *
 * @author Thomas Ball
 */
public final class ConstantPool {

    private static final int CONSTANT_POOL_START = 1;

    // Constant Type enums (JVM spec table 4.3)
    static final int CONSTANT_Utf8 = 1;
    static final int CONSTANT_Integer = 3;
    static final int CONSTANT_Float = 4;
    static final int CONSTANT_Long = 5;
    static final int CONSTANT_Double = 6;
    static final int CONSTANT_Class = 7;
    static final int CONSTANT_String = 8;
    static final int CONSTANT_FieldRef = 9;
    static final int CONSTANT_MethodRef = 10;
    static final int CONSTANT_InterfaceMethodRef = 11;
    static final int CONSTANT_NameAndType = 12;

    CPEntry[] cpEntries;
    
    int constantPoolCount = 0;

    /**
     * Create a ConstantPool object from a stream of bytes.
     *
     * @param size number of entries in this constant pool.
     * @param bytes a stream of bytes defining the constant pool.
     */
    /* package-private */ ConstantPool(int size, InputStream bytes) 
      throws IOException {
        if (size < 0)
            throw new IllegalArgumentException("size cannot be negative");
        if (bytes == null)
            throw new IllegalArgumentException("byte stream not specified");
        constantPoolCount = size;
        cpEntries = new CPEntry[constantPoolCount];
        load(bytes);
    }
    
    /**
     * Create a new ConstantPool object with no entries.
     * NOTE: not supported until classfile writing is.
     */
    /*public*/ ConstantPool() {
        constantPoolCount = CONSTANT_POOL_START;
        cpEntries = new CPEntry[constantPoolCount];
    }

    /**
     * Get the CPEntry at a specific constant pool index.
     *
     * @param index the constant pool index for the entry
     */
    public final CPEntry get(int index) {
        if (index <= 0 || index >= cpEntries.length)
            throw new IndexOutOfBoundsException(Integer.toString(index));
        return cpEntries[index];
    }

    /**
     * Get the CPClassInfo at a specific index.
     *
     * @param index the constant pool index for the entry
     */
    public final CPClassInfo getClass(int index) {
        if (index <= 0)
            throw new IndexOutOfBoundsException(Integer.toString(index));
        return (CPClassInfo)get(index);
    }

    /* Return an array of all constants of a specified class type.
     *
     * @param type   the constant pool type to return.
     */
    public final Collection<CPEntry> getAllConstants(Class<?> classType) {
        return Collections.unmodifiableCollection(
		   getAllConstantsImpl(classType));
    }

    private Collection<CPEntry> getAllConstantsImpl(Class<?> classType) {
        int n = cpEntries.length;
        Collection<CPEntry> c = new ArrayList<CPEntry>(n);
        for (int i = CONSTANT_POOL_START; i < n; i++) {
            if (cpEntries[i] != null && 
                cpEntries[i].getClass().equals(classType)) {
                c.add(cpEntries[i]);
            }
        }
        return c;
    }

    /* Return the collection of all unique class references in pool.
     *
     * @return a Set of ClassNames specifying the referenced classnames.
     *
     * @deprecated use <code>ClassFile.getAllClassNames()</code>,
     * as all class references cannot be reliably determined from just
     * the constant pool structure.
     */
    public final Set<ClassName> getAllClassNames() {
        Set<ClassName> set = new HashSet<ClassName>();

        // include all class name constants
        Collection<CPEntry> c = getAllConstantsImpl(CPClassInfo.class);
        for (Iterator<CPEntry> i = c.iterator(); i.hasNext();) {
            CPClassInfo ci = (CPClassInfo)i.next();
            set.add(ci.getClassName());
        }
        return Collections.unmodifiableSet(set);
    }

    final String getString(int index) {
    	CPUTF8Info utf = (CPUTF8Info)cpEntries[index];
    	return utf.getName();
    }
    
    private void load(InputStream cpBytes) throws IOException {
        try {
	    ConstantPoolReader cpr = new ConstantPoolReader(cpBytes);

            // Read in pool entries.
            for (int i = CONSTANT_POOL_START; i < constantPoolCount; i++) {
                CPEntry newEntry = getConstantPoolEntry(cpr);
                cpEntries[i] = newEntry;
        
                if (newEntry.usesTwoSlots())
                    i++;
            }
    
            // Resolve pool entries.
            for (int i = CONSTANT_POOL_START; i < constantPoolCount; i++) {
                CPEntry entry = cpEntries[i];
                if (entry == null) {
                    continue;
                }
                entry.resolve(cpEntries);
            }
        } catch (IllegalArgumentException ioe) {
            throw new InvalidClassFormatException(ioe);
	} catch (IndexOutOfBoundsException iobe) {
            throw new InvalidClassFormatException(iobe);
        }
    }

    private CPEntry getConstantPoolEntry(ConstantPoolReader cpr)
            throws IOException {
        CPEntry newEntry;
        byte type = cpr.readByte();
        switch (type) {
          case CONSTANT_Utf8:
              newEntry = new CPUTF8Info(this, cpr.readRawUTF());
              break;

          case CONSTANT_Integer:
              newEntry = new CPIntegerInfo(this, cpr.readInt());
              break;

          case CONSTANT_Float:
              newEntry = new CPFloatInfo(this, cpr.readFloat());
              break;

          case CONSTANT_Long:
              newEntry = new CPLongInfo(this, cpr.readLong());
              break;

          case CONSTANT_Double:
              newEntry = new CPDoubleInfo(this, cpr.readDouble());
              break;

          case CONSTANT_Class: {
              int nameIndex = cpr.readUnsignedShort();
              newEntry = new CPClassInfo(this, nameIndex);
              break;
          }

          case CONSTANT_String: {
              int nameIndex = cpr.readUnsignedShort();
              newEntry = new CPStringInfo(this, nameIndex);
              break;
          }

          case CONSTANT_FieldRef: {
              int classIndex = cpr.readUnsignedShort();
              int natIndex = cpr.readUnsignedShort();
              newEntry = new CPFieldInfo(this, classIndex, natIndex);
              break;
          }

          case CONSTANT_MethodRef: {
              int classIndex = cpr.readUnsignedShort();
              int natIndex = cpr.readUnsignedShort();
              newEntry = new CPMethodInfo(this, classIndex, natIndex);
              break;
          }

          case CONSTANT_InterfaceMethodRef: {
              int classIndex = cpr.readUnsignedShort();
              int natIndex = cpr.readUnsignedShort();
              newEntry = new CPInterfaceMethodInfo(this, classIndex, natIndex);
              break;
          }

          case CONSTANT_NameAndType: {
              int nameIndex = cpr.readUnsignedShort();
              int descIndex = cpr.readUnsignedShort();
              newEntry = new CPNameAndTypeInfo(this, nameIndex, descIndex);
              break;
          }

          default:
              throw new IllegalArgumentException(
                          "invalid constant pool type: " + type);
        }

        return newEntry;
    }
}
