/*
 * ExceptionTableEntry.java
 *
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
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
 * An entry in the exception table of a method's code attribute.
 *
 * @author  Thomas Ball
 */
public class ExceptionTableEntry extends Object {
    
    int startPC;
    int endPC;
    int handlerPC;
    CPClassInfo catchType;  // may be null for "finally" exception handler

    static ExceptionTableEntry[] loadExceptionTable(DataInputStream in, ConstantPool pool) 
      throws IOException {
        int n = in.readUnsignedShort();
        ExceptionTableEntry[] exceptions = new ExceptionTableEntry[n];
        for (int i = 0; i < n; i++)
            exceptions[i] = new ExceptionTableEntry(in, pool);
        return exceptions;
    }

    /** Creates new ExceptionTableEntry */
    ExceptionTableEntry(DataInputStream in, ConstantPool pool) 
      throws IOException {
        loadExceptionEntry(in, pool);
    }

    private void loadExceptionEntry(DataInputStream in, ConstantPool pool) 
      throws IOException {
        startPC = in.readUnsignedShort();
        endPC = in.readUnsignedShort();
        handlerPC = in.readUnsignedShort();
        int typeIndex = in.readUnsignedShort();
        if (typeIndex != 0) // may be 0 for "finally" exception handler
            catchType = pool.getClass(typeIndex);
    }
    
    /**
     * @return the beginning offset into the method's bytecodes of this
     * exception handler.
     */
    public final int getStartPC() {
        return startPC;
    }
    
    /**
     * @return the ending offset into the method's bytecodes of this
     * exception handler, or the length of the bytecode array if the
     * handler supports the method's last bytecodes (JVM 4.7.3).
     */
    public final int getEndPC() {
        return endPC;
    }
    
    /**
     * @return the starting offset into the method's bytecodes of the 
     * exception handling code.
     */
    public final int getHandlerPC() {
        return handlerPC;
    }
    
    /**
     * @return the type of exception handler, or <code>null</code>
     * if this handler catches all exceptions, such as an exception
     * handler for a "<code>finally</code>" clause (JVM 4.7.3).
     */
    public final CPClassInfo getCatchType() {
        return catchType;
    }
}
