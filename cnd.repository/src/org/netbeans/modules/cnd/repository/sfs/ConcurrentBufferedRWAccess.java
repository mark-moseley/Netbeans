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

package org.netbeans.modules.cnd.repository.sfs;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author Nickolay Dalmatov
 */
public class ConcurrentBufferedRWAccess extends BufferedRWAccess implements ConcurrentFileRWAccess {
    private ReentrantReadWriteLock fileLock ;
    
    /** Creates a new instance of ConcurrentBufferedRWAccess */
    public ConcurrentBufferedRWAccess(File file) throws IOException {
        super(file);
        fileLock = new ReentrantReadWriteLock(true);
    }
    
    public Lock readLock() {
        return fileLock.readLock();
    }
    
    public Lock writeLock() {
        return fileLock.writeLock();
    }
    
}
