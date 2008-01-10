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
 */

package org.netbeans.modules.masterfs.filebasedfs;

import org.netbeans.modules.masterfs.*;
import java.io.File;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 * @author pzajac
 */
public class Deadlock54741 extends NbTestCase {
    
    private static class DelFileChangeListener implements FileChangeListener {
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        public void fileChanged(FileEvent fe) {
        }

        public void fileDataCreated(FileEvent fe) {
        }

        public void fileDeleted(FileEvent fe) {
            try {
                synchronized (this) {
                    wait(); 
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void fileFolderCreated(FileEvent fe) {
        }

        public void fileRenamed(FileRenameEvent fe) {
        }
        
    }
    
    private static class DeleteRunnable implements Runnable {
        FileObject fo;
        public DeleteRunnable(FileObject fo) {
            this.fo = fo;
        }
        public void run() {
            System.out.println("start delete");
            try {
               fo.getFileSystem().addFileChangeListener(new DelFileChangeListener());
               FileSystem fs = fo.getFileSystem(); 
               FileUtil.toFile(fo).delete();
               fs.refresh(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("end delete");
        } 
    }
    
    
    public Deadlock54741(String testName) {
        super(testName);
    }
    
    public void testDeadLock () throws Exception {
        File f = File.createTempFile("fff","fsdfsd");
        FileObject tmpFo = FileUtil.toFileObject(f.getParentFile()); 
        assertNotNull(tmpFo);
      
        FileObject fo = null;
        fo = tmpFo.createData("ssss");   
        Runnable deleteRunnable = new DeleteRunnable(fo); 
        Thread thread = new Thread(deleteRunnable);
        thread.start();
            
        try {
            Thread.currentThread().sleep(2000);
            boolean isDeadlock [] = new boolean[1]; 
             makeDeadlock(tmpFo,f, isDeadlock);   
            Thread.sleep(2000);
            boolean isD = isDeadlock[0];
            // finish -> unlock thread
            deleteRunnable.notify();
            assertFalse("deadlock!!!",isD);    
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        
    }
    private Thread makeDeadlock(final FileObject fo, final File f,final boolean isDeadLock[]) {  
        isDeadLock[0] = true;
        Thread t = new Thread () {
            public void run() {
                   fo.getFileObject(f.getName());
                   isDeadLock[0] = false;
        }};     
        t.start(); 
        return t;  
    }  
        
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(Deadlock54741.class);
         
        return new FileBasedFileSystemTest(suite);
    }
}
