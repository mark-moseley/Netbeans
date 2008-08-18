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

package org.netbeans.modules.editor.mimelookup.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Martin Roskanin
 */
public class TestUtilities {

    private TestUtilities(){
    }

    /** Method will wait max. <code> maxMiliSeconds </code> miliseconds for the <code> requiredValue </code>
     *  gathered by <code> resolver </code>.
     *
     *  @param maxMiliSeconds maximum time to wait for requiredValue
     *  @param resolver resolver, which is gathering an actual value
     *  @param requiredValue if resolver value equals requiredValue the wait cycle is finished
     *
     *  @return false if the given maxMiliSeconds time elapsed and the requiredValue wasn't obtained
     */
    public static boolean waitMaxMilisForValue(int maxMiliSeconds, ValueResolver resolver, Object requiredValue){
        int time = maxMiliSeconds / 100;
        while (time > 0) {
            Object resolvedValue = resolver.getValue();
            if (requiredValue == null && resolvedValue == null){
                return true;
            }
            if (requiredValue != null && requiredValue.equals(resolvedValue)){
                return true;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                time=0;
            }
            time--;
        }
        return false;
    }
    
    /** Interface for value resolver needed for i.e. waitMaxMilisForValue method.  
     *  For more details, please look at {@link #waitMaxMilisForValue()}.
     */
    public static interface ValueResolver{
        /** Returns checked value */
        Object getValue();
    }
    
    public static void deleteFile(final File workDir, final String path) throws IOException {
        File f = new File(workDir, path);
        FileObject fo = FileUtil.toFileObject(f);
        if (fo == null) {
            throw new FileNotFoundException(f.getAbsolutePath());
        }
        fo.delete();
    }

    public static void createFile(final File workDir, final String path) throws IOException {
        FileObject fo = FileUtil.toFileObject(workDir);
        if (fo == null) {
            throw new IOException("Can't map '" + workDir.getAbsolutePath() + "' to the filesystem repository.");
        }
        if (path.endsWith("/")) {
            FileUtil.createFolder(fo, path.substring(0, path.length() - 1));
        } else {
            FileUtil.createData(fo, path);
        }
    }

    public static void sleepForWhile() {
        try {
            Thread.sleep(321);
        } catch (InterruptedException ex) {
            // ignore
        }
    }

    public static void consumeAllMemory() {
        List<byte[]> list = new ArrayList<byte[]>();
        long size = 0;
        try {
            for(int i = 0; i < 1000000; i++) {
                byte [] padding = new byte[100000];
                list.add(padding);
                size += padding.length;
            }
            throw new IllegalStateException("Can't run out of memory! The VM's heap size is too big.");
        } catch (OutOfMemoryError e) {
            // ok the VM's just run out of memory
            // release everything we've allocated
            list = null;
            System.out.println("OutOfMemory after allocating " + size + " bytes.");
        }
    }
    
    public static void gc() {
        for (int i = 0; i < 10; i++) {
            System.gc();
            try {
                Thread.sleep(123);
            } catch (InterruptedException ex) {
                // ignore
            }
        }
    }
}
