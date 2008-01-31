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
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.masterfs.filebasedfs.utils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.openide.util.Lookup;

/**
 *
 * @author Radek Matous
 */
public class FileChangedManager extends SecurityManager {
    private static  FileChangedManager INSTANCE;
    private Map<Integer,Boolean> hints = new ConcurrentHashMap<Integer,Boolean>();
    public FileChangedManager() {
        INSTANCE = this;
    }
    
    public static FileChangedManager getInstance() {
        if (INSTANCE == null) {
            Lookup.getDefault().lookup(SecurityManager.class);
            assert INSTANCE != null;
        }
        return INSTANCE;
    }
    
    @Override
    public void checkDelete(String file) {
        put(file, false);
    }

    @Override
    public void checkWrite(String file) {
        put(file, true);
    }    
    
    public boolean impeachExistence(File f, boolean expectedExixts) {
        Boolean hint = get(getKey(f));
        boolean retval = (hint == null) ? false : !hint.equals(expectedExixts);
        if (retval) {
            System.out.println("!!!! impeachExistence: " + f.getAbsolutePath());
        }
        return retval;
    }    
    
    public boolean createNewFile(File file) throws IOException {
        boolean retval = file.createNewFile();
        if (retval) {
            put(file, retval);
        }
        return retval;
    }
    
    public boolean mkdir(File file) throws IOException {
        boolean retval = file.mkdir();
        if (retval) {
            File f = file;
            while(f != null) {
                put(f, retval);
                f = f.getParentFile();
            }
        }
        return retval;
    }

    public boolean mkdirs(File file) throws IOException {
        boolean retval = file.mkdirs();
        if (retval) {
            File f = file;
            while(f != null) {
                put(f, retval);
                f = f.getParentFile();
            }
        }
        return retval;
    }
    
    public boolean exists(File file) {
        boolean retval = file.exists();
        put(file,retval);
        return retval;
    }

    public boolean canWrite(File file) {
        int id = getKey(file);
        Boolean hint =get(id);
        boolean retval = file.canWrite();
        //no hint - revert
        if (hint == null) {
            remove(id);
        } else {
            put(file, hint);
        }
        return retval;
    }
    
    private static int getKey(File f) {
        return NamingFactory.createID(f);
    }
    private static int getKey(String f) {
        return getKey(new File(f));
    }  

    private Boolean put(String f, boolean value) {
        if (value && f.endsWith("JavaApplication166/build")) {
            Boolean b = get(getKey(f));
            if (b != null && b.equals(false)) {
                new Exception(f).printStackTrace();
            }
        }
        return put(getKey(f), value);
    }
    
    private Boolean put(File f, boolean value) {
        if (value && f.getAbsolutePath().endsWith("JavaApplication166/build")) {
            Boolean b = get(getKey(f));
            if (b != null && b.equals(false)) {
                new Exception(f.getAbsolutePath()).printStackTrace();
            }           
        }        
        return put(getKey(f), value);
    }
    
    private Boolean put(int id, boolean value) {
        return hints.put(id, value);
    }
    
    private Boolean get(int id) {
        return hints.get(id);
    }

    private Boolean remove(int id) {
        return hints.remove(id);
    }    
}
