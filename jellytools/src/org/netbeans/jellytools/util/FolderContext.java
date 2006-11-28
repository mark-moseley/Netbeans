/*
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
 */

package org.netbeans.jellytools.util;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * The FolderContext class contains methods for finding, creating, removing Data
 * Objects, File Objects and Nodes in a given folder (e.g. 'data'). All objects
 * are addresed relatively to the folder.
 * <p>
 * Usage:<br>
 * <pre>
 * public class TestTest extends JellyTestCase {
 *    ...
 *    public void test() throws Exception {
 *        // find data folder
 *        FolderContext df = FolderContext.getDataFolder(this);
 *        // create folder
 *        df.createFolder("resources");
 *        // create file with given content
 *        DataObject bundle = df.createDataObject("resources/Bundle.properties", "OpenIDE-Module-Name=My Module\n");
 *        // new from template
 *        NewWizardOperator.create("Java Package", df.getTreePath("resources"), "org.netbeans.modules");
 *        Node node = df.getJellyNode("resources|Bundle");
 *        ...
 *        // remove garbage
 *        df.deleteDataObject("resources");
 *        ...
 *        // find work dir, create and mount it if necesarry
 *        FolderContext wd = FolderContext.getWorkDir(this);
 *        ...
 * </pre>
 *
 * @author  ms113234
 */
public class FolderContext {
    private URL _base;
    
    /** Creates a new instance of FolderContext.
     * @param url folder's URL
     */
    @SuppressWarnings("deprecation")
    public FolderContext(URL url) {
        FileObject[] fos = URLMapper.findFileObjects(url);
        if (fos == null || fos.length == 0) {
            throw new IllegalStateException("Cannot find file object: " + url);
        }
        _base = url;
    }
    
    /** Returns data folder context for given test.
     * @param test the test
     * @return FolderContext
     */
    public static FolderContext getDataFolder(JellyTestCase test) {
        URL url = test.getClass().getResource("data/");
        if (url == null) {
            throw new IllegalStateException("Cannot find 'data' folder for: " + test.getClass().getName());
        }
        return new FolderContext(url);
    }
    
    /** Returns working directory context for given test. If working directory
     * is not available method tries to creat it.
     * @param test the test
     * @throws IOException if the directory cannot be created
     * @return FolderContext
     */
    @SuppressWarnings("deprecation")
    public static FolderContext getWorkDir(JellyTestCase test) throws IOException {
        String wd = test.getWorkDir().getAbsolutePath();
        FileSystem fs = Repository.getDefault().findFileSystem(wd);
        /* RepositoryTabOperator removed from jellytools
        if (fs == null) {
            RepositoryTabOperator.invoke().mountLocalDirectoryAPI(wd);
        }
        fs = Repository.getDefault().findFileSystem(wd);
         */
        if (fs == null) {
            throw new IllegalStateException("Cannot mount: " + wd);
        }
        URL url = fs.getRoot().getURL();
        if (url == null) {
            throw new IllegalStateException("Cannot find work dir for: " + test.getClass().getName());
        }
        return new FolderContext(url);
    }
    
    /** Finds the File Object. The spec is
     * a "/" separated relative path that identifies the File Object.
     * @param spec File Object's path
     * @return FileObject
     */
    @SuppressWarnings("deprecation")
    public FileObject getFileObject(String spec) {
        FileObject fo = null;
        URL url = null;
        if (spec == null) {
            url = _base;
        } else {
            url = makeURL(spec);
        }
        FileObject[] fos = URLMapper.findFileObjects(url);
        if (fos == null || fos.length == 0) {
            return null;
        } else  {
            return fos[0];
        }
    }
    
    /** Creates folder.
     * @param spec folder's relative path separated by '/'
     * @throws IOException if the folder cannot be created
     * @return DataObject
     */
    public DataObject createFolder(String spec) throws IOException {
        return createDataObject(spec, null);
    }
    /** Creates Data Object.
     * @param spec Data Object's relative path separated by '/'
     * @param content Data Object's content
     * @throws IOException if the Data Object cannot be created
     * @return DataObject
     */
    public DataObject createDataObject(String spec, final String content) throws IOException {
        File file = new File(spec);
        final String path = file.getParent();
        final String name = file.getName();
        
        final FileObject parent = getFileObject(path);
        if (parent == null) {
            throw new IllegalStateException("Cannot find folder: " + makeURL(path));
        }
        FileSystem filesystem = parent.getFileSystem();
        
        final FileObject[] fileObject = new FileObject[1];
        AtomicAction fsAction = new AtomicAction() {
            public void run() throws IOException {
                if (content == null) {
                    // create folder
                    fileObject[0] = parent.createFolder(name);
                } else {
                    // create file
                    FileObject fo = parent.createData(name);
                    FileLock lock = null;
                    try {
                        lock = fo.lock();
                        OutputStream out = fo.getOutputStream(lock);
                        out = new BufferedOutputStream(out);
                        Writer writer = new OutputStreamWriter(out, "UTF8");
                        writer.write(content);
                        writer.close();
                        // return DataObject
                        lock.releaseLock();
                        lock = null;
                        fileObject[0] = fo;
                    } finally {
                        if (lock != null) lock.releaseLock();
                    }
                }
            }
        };
        
        filesystem.runAtomicAction(fsAction);
        return DataObject.find(fileObject[0]);
    }
    
    /** Finds the Data Object. The spec is
     * a "/" separated relative path that identifies the Data Object.
     * @return Data Object or 'null' if the Data Object does not exist
     * @param spec Data Object's path
     */
    public DataObject getDataObject(String spec) {
        DataObject dto = null;
        FileObject fo = getFileObject(spec);
        if (fo != null) {
            try {
                dto = DataObject.find(fo);
            } catch (DataObjectNotFoundException nfe) {
                /* dto = null; */
            }
        }
        return dto;
    }
    
    /** Deletes the File Object. The spec is
     * a "/" separated relative path that identifies the File Object.
     * @param spec File Object's path
     * @throws IOException if something fails
     */
    public void deleteDataObject(String spec) throws IOException {
        DataObject dataObject = getDataObject(spec);
        if (dataObject == null) {
            // nothing to delete
            return;
        }
        dataObject.getNodeDelegate().destroy();
    }
    
    /** Finds Jelly Node.
     * @param path Jelly Node's relative path separated by '|'
     * @return Node
     */
    /* RepositoryTabOperator removed from jellytools
    public Node getJellyNode(String path) {
        Node node = null;
        String treePath = getTreePath(path) ;
        JTreeOperator tree = RepositoryTabOperator.invoke().tree();
        node = new Node(tree, treePath);
        return node;
    }
     */
    
    /** Returns absolute Tree Path.
     * @param path relative path separated by '|'
     * @return String
     */
    @SuppressWarnings("deprecation")
    public String getTreePath(String path) {
        String tp = null;
        FileObject fo = getFileObject("");
        try {
            tp = fo.getFileSystem().getDisplayName() + '|' + fo.getPackageName('|') + '|' + path;
        } catch (FileStateInvalidException fsie){
            throw new JemmyException("Cannot get filesystem for: " + fo.getPackageNameExt('/', '.'), fsie);
        }
        return tp;
    }
    
    // LIB /////////////////////////////////////////////////////////////////////
    
    /** Creates URL from _base context and relative identifier 'spec' */
    private URL makeURL(String spec) {
        if (spec == null || spec.equals("")) {
            return _base;
        }
        // Emits leading whitespaces and '/'.
        spec.trim();
        while (spec.startsWith("/")) {
            spec = spec.substring(1);
        }
        URL url = _base;
        try {
            url = new URL(_base, spec);
        } catch (MalformedURLException mue) { /* url = _base; */ }
        return url;
    }
}
