/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * Utilities.java
 *
 * Created on September 24, 2002, 11:11 AM
 */

package gui;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import javax.swing.text.StyledDocument;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.junit.AssertionFailedErrorException;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;

/**
 *
 * @author  jb105785
 */
public class Utilities {
    
    /** Creates a new instance of Utilities */
    public Utilities() {
    }

    /** Mounts <userdir>/sampledir through API
     * @return absolute path of mounted dir
     */
    public static boolean mountSampledir() {
        String userdir = System.getProperty("netbeans.user"); // NOI18N
        String mountPoint = userdir+File.separator+"sampledir"; // NOI18N
        mountPoint = mountPoint.replace('\\', '/');
        FileSystem fs = Repository.getDefault().findFileSystem(mountPoint);
        if (fs == null) {
            try {
                LocalFileSystem lfs= new LocalFileSystem();
                lfs.setRootDirectory(new File(mountPoint));
                Repository.getDefault().addFileSystem(lfs);
                return true;
            } catch (IOException ioe) {
                throw new JemmyException("Mounting FS: "+mountPoint+" failed.", ioe);
            } catch (PropertyVetoException pve) {
                throw new JemmyException("Mounting FS: "+mountPoint+" failed.", pve);
            }
        }
        return true;
    }
    
    
    public static void delete(String file) {
        FileObject fileObject = Repository.getDefault().findResource(file);
        if (fileObject==null) return;
        try {
            DataObject.find(fileObject).delete();
        } catch (java.io.IOException e) {
        }
    }
    
    /** Removes time and author's name
     * @param result
     * @return
     *
     */
    public static String unify(String result) {
        int left=result.indexOf("* Created on");
        int right;
        if (left>=0) {
            right=result.indexOf('\n',left);
            result=result.substring(0,left+"* Created on".length())+result.substring(right);
        }
        
        if (left>=0) {
            left=result.indexOf("@author");
            right=result.indexOf('\n',left);
            result=result.substring(0,left+"@author".length())+result.substring(right);
        }
        return result;
    }
    
    public static String getAsString(String file) {
        String result;
        try {
            FileObject testFile = Repository.getDefault().findResource(file);
            DataObject DO = DataObject.find(testFile);
            
            EditorCookie ec=(EditorCookie)(DO.getCookie(EditorCookie.class));
            StyledDocument doc=ec.openDocument();
            result=doc.getText(0, doc.getLength());
            //            result=Common.unify(result);
        } catch (Exception e){
            throw new AssertionFailedErrorException(e);
        }
        return result;
    }
    
}
