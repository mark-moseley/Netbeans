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

package org.netbeans.api.java.source;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPInputStream;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.source.classpath.GlobalSourcePathTestUtil;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Utilities to aid unit testing java.source module.
 *
 * @author Jaroslav Tulach
 * @author Tom Ball
 * @author Tomas Zezula
 */
public final class TestUtilities {
    
    // do not instantiate
    private TestUtilities() {}
    
    /**
     * Waits for the end of the background scan, this helper method 
     * is designed for tests which require to wait for the end of initial scan.
     * The method can be used as a barrier but it is not garanted that the
     * backgoud scan will not start again after return from this method, the
     * test is responsible for it itself. In general it's safer to use {@link JavaSource#runWhenScanFinished}
     * method and do the critical action inside the run methed.
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return true if the scan finished, false when the timeout elapsed before the end of the scan.
     * @throws InterruptedException is thrown when the waiting thread is interrupted.
     */
    public static boolean waitScanFinished (final long timeout, final TimeUnit unit) throws InterruptedException {
        assert unit != null;
        final ClasspathInfo cpInfo = ClasspathInfo.create(ClassPathSupport.createClassPath(new URL[0]),
                ClassPathSupport.createClassPath(new URL[0]), null);
        assert cpInfo != null;
        final JavaSource js = JavaSource.create(cpInfo);
        assert js != null;
        try {
            Future<Void> future = js.runWhenScanFinished(new Task<CompilationController>() {
                public void run(CompilationController parameter) throws Exception {
                }
            }, true);
            future.get(timeout,unit);
            return true;
        } catch (IOException ioe) {
            //Actually never thrown
        }
        catch (ExecutionException ee) {
            //Actually never thrown
        }
        catch (TimeoutException timeoutEx) {
        }
        return false;
    }
    
    /**
     * Schedules compilation of the folder inside the source root. May be used by tests
     * which need to fill data into {@link ClassIndex} or need to work on the group of java files.
     * @param folder to be compiled, has to be either the root itself or must be subfolder of the root.
     * @param root owning the folder. There must be boot, compile and source ClassPath for it
     * available by {@link ClassPath#getClassPah} method - there must be {@link ClassPathProvider} handling
     * the root registered in the global {@link Lookup}. There must be also source level provided for the root - 
     * {@link SourceLevelImplementation} registered in the global {@link Lookup}.  
     * @return CountDownLatch which may be used to wait to the end of asynchronous compilation, {@see CountDownLatch#await}
     * @throws java.io.IOException when folder or root is not valid
     */
    public static CountDownLatch scheduleCompilation (final FileObject folder, final FileObject root) throws IOException {
        assert folder != null;
        assert root != null;
        assert folder.equals(root) || FileUtil.isParentOf(root, folder);
        return RepositoryUpdater.getDefault().scheduleCompilationAndWait(folder, root);
    }
    
    /**
     * Schedules compilation of the folder inside the source root. May be used by tests
     * which need to fill data into {@link ClassIndex} or need to work on the group of java files.
     * @param folder to be compiled, has to be either the root itself or must be subfolder of the root.
     * @param root owning the folder. There must be boot, compile and source ClassPath for it
     * available by {@link ClassPath#getClassPah} method - there must be {@link ClassPathProvider} handling
     * the root registered in the global {@link Lookup}. There must be also source level provided for the root - 
     * {@link SourceLevelImplementation} registered in the global {@link Lookup}.  
     * @return CountDownLatch which may be used to wait to the end of asynchronous compilation, {@see CountDownLatch#await}
     * @throws java.io.IOException when folder or root is not valid
     */
    public static CountDownLatch scheduleCompilation (final File folder, final File root) throws IOException {
        assert folder != null;
        assert root != null;
        final FileObject rootFo = FileUtil.toFileObject(root);
        final FileObject folderFo = FileUtil.toFileObject(folder);
        return RepositoryUpdater.getDefault().scheduleCompilationAndWait(folderFo, rootFo);
    }
    
    /**
     * Schedules compilation of the folder inside the source root and blocks until it finishes. May be used by tests
     * which need to fill data into {@link ClassIndex} or need to work on the group of java files.
     * @param folder to be compiled, has to be either the root itself or must be subfolder of the root.
     * @param root owning the folder. There must be boot, compile and source ClassPath for it
     * available by {@link ClassPath#getClassPah} method - there must be {@link ClassPathProvider} handling
     * the root registered in the global {@link Lookup}. There must be also source level provided for the root - 
     * a {@link SourceLevelImplementation} registered in the global {@link Lookup}.  
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return false if the timeout elapsed before the end of compilation
     * @throws java.io.IOException when folder or root is not valid
     * @throws InterruptedException when the thread is interrupted
     */
    public static boolean scheduleCompilationAndWait (final FileObject folder, final FileObject root,
            final long timeout, final TimeUnit unit) throws IOException, InterruptedException {
        assert unit != null;
        final CountDownLatch latch = scheduleCompilation(folder, root);
        return latch.await(timeout, unit);
    }
    
    /**
     * Schedules compilation of the folder inside the source root and blocks until it finishes. May be used by tests
     * which need to fill data into {@link ClassIndex} or need to work on the group of java files.
     * @param folder to be compiled, has to be either the root itself or must be subfolder of the root.
     * @param root owning the folder. There must be boot, compile and source ClassPath for it
     * available by {@link ClassPath#getClassPah} method - there must be {@link ClassPathProvider} handling
     * the root registered in the global {@link Lookup}. There must be also source level provided for the root - 
     * a {@link SourceLevelImplementation} registered in the global {@link Lookup}.  
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return false if the timeout elapsed before the end of compilation
     * @throws java.io.IOException when folder or root is not valid
     * @throws InterruptedException when the thread is interrupted
     */
    public static boolean scheduleCompilationAndWait (final File folder, final File root,
            final long timeout, final TimeUnit unit) throws IOException, InterruptedException {
        assert unit != null;
        final CountDownLatch latch = scheduleCompilation(folder, root);
        return latch.await(timeout, unit);
    }
    
    /**
     * Disables use of {@link LibraryManager} in the {@link GlobalSourcePath}. The tests
     * which don't register {@link LibraryProvider} or {@link LibraryTypeProvider} may
     * use this method to disable use of {@link LibraryManager} in the {@link GlobalSourcePath}.
     * @param use false value disables use of {@link LibraryManager}
     */
    public static void setUseLibraries (final boolean use) {
        GlobalSourcePathTestUtil.setUseLibraries(use);
    }
    
    /**
     * Sets a root folder of the java source caches. This method may be used by tests
     * which need to do an initial compilation, they require either {@link ClassIndex} or
     * need to work with a group of related java files.
     * @param cacheFolder the folder used by java infrastructure as a cache,
     * has to exist and must be a folder.
     */
    public static void setCacheFolder (final File cacheFolder) {
        IndexUtil.setCacheFolder(cacheFolder);
    }
    
    /**
     * Creates boot {@link ClassPath} for platform the test is running on,
     * it uses the sun.boot.class.path property to find out the boot path roots.
     * @return ClassPath
     * @throws java.io.IOException when boot path property conatins non valid path
     */
    public static ClassPath createBootClassPath () throws IOException {
        String bootPath = System.getProperty ("sun.boot.class.path");
        String[] paths = bootPath.split(File.pathSeparator);
        List<URL>roots = new ArrayList<URL> (paths.length);
        for (String path : paths) {
            File f = new File (path);            
            if (!f.exists()) {
                continue;
            }
            URL url = f.toURI().toURL();
            if (FileUtil.isArchiveFile(url)) {
                url = FileUtil.getArchiveRoot(url);
            }
            roots.add (url);
        }
        return ClassPathSupport.createClassPath(roots.toArray(new URL[roots.size()]));
    }
    /**
     * Returns a string which contains the contents of a file.
     *
     * @param f the file to be read
     * @return the contents of the file(s).
     */
    public final static String copyFileToString (java.io.File f) throws java.io.IOException {
        int s = (int)f.length ();
        byte[] data = new byte[s];
        int len = new FileInputStream (f).read (data);
        if (len != s)
            throw new EOFException("truncated file");
        return new String (data);
    }
    
    /**
     * Returns a string which contains the contents of a GZIP compressed file.
     *
     * @param f the file to be read
     * @return the contents of the file(s).
     */
    public final static String copyGZipFileToString (java.io.File f) throws java.io.IOException {
        GZIPInputStream is = new GZIPInputStream(new FileInputStream(f));
        byte[] arr = new byte[256 * 256];
        int first = 0;
        for(;;) {
            int len = is.read(arr, first, arr.length - first);
            if (first + len < arr.length) {
                return new String(arr, 0, first + len);
            }
        }
    }
    
    /**
     * Copies a string to a specified file.
     *
     * @param f the file to use.
     * @param content the contents of the returned file.
     * @return the created file
     */
    public final static File copyStringToFile (File f, String content) throws Exception {
        FileOutputStream os = new FileOutputStream(f);
        InputStream is = new ByteArrayInputStream(content.getBytes("UTF-8"));
        FileUtil.copy(is, os);
        os.close ();
        is.close();
            
        return f;
    }
    
    /**
     * Copies a string to a specified file.
     *
     * @param f the {@link FilObject} to use.
     * @param content the contents of the returned file.
     * @return the created file
     */
    public final static FileObject copyStringToFile (FileObject f, String content) throws Exception {
        OutputStream os = f.getOutputStream();
        InputStream is = new ByteArrayInputStream(content.getBytes("UTF-8"));
        FileUtil.copy(is, os);
        os.close ();
        is.close();
            
        return f;
    }   
}
