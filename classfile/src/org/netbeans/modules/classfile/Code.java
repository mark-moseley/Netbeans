/*
 * Code.java
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * Contributor(s): Thomas Ball
 *
 * Version: $Revision$
 */

package org.netbeans.modules.classfile;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * The Code attribute of a method.
 *
 * @author  Thomas Ball
 */
public final class Code {
    private static final boolean debug = false;

    private int maxStack;
    private int maxLocals;
    private byte[] byteCodes;
    private ExceptionTableEntry[] exceptionTable;
    private int[] lineNumberTable;
    private LocalVariableTableEntry[] localVariableTable;
    private LocalVariableTypeTableEntry[] localVariableTypeTable;
    private StackMapFrame[] stackMapTable;

    /** Creates a new Code object */
    /* package-private */ Code(DataInputStream in, ConstantPool pool) throws IOException {
        if (in == null)
            throw new IllegalArgumentException("input stream not specified");
        if (pool == null)
            throw new IllegalArgumentException("constant pool not specified");
        try {
            loadCode(in, pool);
        } catch (IndexOutOfBoundsException e) {
            throw new InvalidClassFileAttributeException("invalid code attribute", e);
        }
    }

    private void loadCode(DataInputStream in, ConstantPool pool) 
      throws IOException {
        maxStack = in.readUnsignedShort();
        maxLocals = in.readUnsignedShort();
        int len = in.readInt();
        byteCodes = new byte[len];
        in.readFully(byteCodes);
        exceptionTable = ExceptionTableEntry.loadExceptionTable(in, pool);
        loadCodeAttributes(in, pool);
    }
        
    private void loadCodeAttributes(DataInputStream in, ConstantPool pool) 
      throws IOException {
        int count = in.readUnsignedShort();
        for (int i = 0; i < count; i++) {
            Object o = pool.get(in.readUnsignedShort());
            if (!(o instanceof CPUTF8Info))
                throw new InvalidClassFormatException();
            CPUTF8Info entry = (CPUTF8Info)o;
            int len = in.readInt();
            String name = entry.getName();
            if (name.equals("LineNumberTable")) //NOI18N
                loadLineNumberTable(in, pool);
            else if (name.equals("LocalVariableTable")) //NOI18N
                localVariableTable = 
                    LocalVariableTableEntry.loadLocalVariableTable(in, pool);
            else if (name.equals("LocalVariableTypeTable")) //NOI18N
                localVariableTypeTable = 
                    LocalVariableTypeTableEntry.loadLocalVariableTypeTable(in, pool);
            else if (name.equals("StackMapTable")) //NOI18N
                stackMapTable = StackMapFrame.loadStackMapTable(in, pool);
            else {
		if (debug)
		    System.out.println("skipped unknown code attribute: " + name);
                // ignore unknown attribute...
                int n;
		while ((n = (int)in.skip(len)) > 0 && n < len)
		    len -= n;
            }
        }
        if (lineNumberTable == null)
            lineNumberTable = new int[0];
        if (localVariableTable == null)
            localVariableTable = new LocalVariableTableEntry[0];
        if (localVariableTypeTable == null)
            localVariableTypeTable = new LocalVariableTypeTableEntry[0];
        if (stackMapTable == null)
            stackMapTable = new StackMapFrame[0];
    }
    
    private void loadLineNumberTable(DataInputStream in, ConstantPool pool) throws IOException {
        int n = in.readUnsignedShort();
        lineNumberTable = new int[n * 2];
        for (int i = 0; i < n; i++) {
            lineNumberTable[i * 2] = in.readUnsignedShort();     // start_pc
            lineNumberTable[i * 2 + 1] = in.readUnsignedShort(); // line_number
        }
    }

    public final int getMaxStack() {
        return maxStack;
    }
    
    public final int getMaxLocals() {
        return maxLocals;
    }
    
    public final byte[] getByteCodes() {
        return byteCodes.clone();
    }
    
    public final ExceptionTableEntry[] getExceptionTable() {
        return exceptionTable.clone();
    }

    /**
     * Returns an array of int pairs consisting of a start_pc and a 
     * line_number.  For example, [0] = first pc, [1] = first line, 
     * [2] = second pc, etc.
     */
    public final int[] getLineNumberTable() {
        return lineNumberTable.clone();
    }

    /**
     * Returns the local variable table for this code.
     */
    public final LocalVariableTableEntry[] getLocalVariableTable() {
        return localVariableTable.clone();
    }

    /**
     * Returns the local variable type table for this code, which 
     * describes the generic reference type for those variables which
     * are generic.
     */
    public final LocalVariableTypeTableEntry[] getLocalVariableTypeTable() {
        return localVariableTypeTable.clone();
    }
    
    /**
     * Returns the stack map table for this code, which defines the stack frame
     * information needed by the new classfile verifier in Java 6.
     */
    public final StackMapFrame[] getStackMapTable() {
        return stackMapTable.clone();
    }

    public String toString() {
	StringBuffer sb = new StringBuffer("Code: bytes=");
	sb.append(byteCodes.length);
	sb.append(", stack=");
	sb.append(maxStack);
	sb.append(", locals=");
	sb.append(maxLocals);
	return sb.toString();
    }
}
