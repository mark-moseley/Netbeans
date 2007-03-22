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

package org.netbeans.modules.cnd.repository.testbench.sfs;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;

/**
 * PersistentFactory implementation
 * @author Vladimir Kvashin
 */
public class TestFactory implements PersistentFactory {

    private static final TestFactory instance = new TestFactory();
    
    public static TestFactory instance() {
	return instance;
    }
    
    public void write(DataOutput out, Persistent obj) throws IOException {
	if( obj instanceof TestObject ) {
	    ((TestObject) obj).write(out);
	}
    }
    
    public Persistent read(DataInput in) throws IOException {
	TestObject obj = new TestObject(in);
	return obj;
    }    

    public boolean canWrite(Persistent obj) {
	return obj instanceof TestObject;
    }
    
}
