/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.infra.build.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.Utils;
import org.netbeans.installer.infra.build.ant.utils.FileEntry;

/**
 * This class is an ant task which is capable of properly packaging a directory into
 * an archive. In addition to simply jarring the directory, it pack200-packages the
 * jar files present in the source directory and composes a files list which
 * contains some useful metadata about the files in the archive, such as the
 * checksums, sizes, etc.
 *
 * @author Kirill Sorokin
 */
public class Package extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * The target archive file.
     */
    private File file;
    
    /**
     * The sources directory.
     */
    private File directory;
    
    /**
     * List of {@link FileEntry} objects, which represent the metadata for the files
     * which should be included into the archive.
     */
    private List<FileEntry> entries;
    
    private long directoriesCount;
    private long filesCount;
    
    // constructor //////////////////////////////////////////////////////////////////
    /**
     * Constructs a new instance of the {@link Package} task. It simply sets the
     * default values for the attributes.
     */
    public Package() {
        entries = new LinkedList<FileEntry>();
    }
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the 'file' property.
     *
     * @param path New value for the 'file' property.
     */
    public void setFile(final String path) {
        file = new File(path);
        if (!file.equals(file.getAbsoluteFile())) {
            file = new File(getProject().getBaseDir(), path);
        }
    }
    
    /**
     * Setter for the 'directory' property.
     *
     * @param path New value for the 'directory' property.
     */
    public void setDirectory(final String path) {
        directory = new File(path);
        if (!directory.equals(directory.getAbsoluteFile())) {
            directory = new File(getProject().getBaseDir(), path);
        }
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    /**
     * Executes the task. The source directory is recursively browsed, its files are
     * examined, packaged and added to the archive; some additional metadata is
     * calculated and then added to the files list.
     *
     * @throws org.apache.tools.ant.BuildException if an I/O error occurs.
     */
    public void execute() throws BuildException {
        Utils.setProject(getProject());
        
        JarOutputStream output = null;
        try {
            output = new JarOutputStream(new FileOutputStream(file));
            output.setLevel(9);
            
            log("browsing, packing, archiving directory"); // NOI18N
            browse(directory.getCanonicalFile(),
                    output,
                    directory.getCanonicalPath().length());
            
            log("adding manifest and files list"); // NOI18N
            output.putNextEntry(new JarEntry(METAINF_ENTRY));
            
            output.putNextEntry(new JarEntry(MANIFEST_ENTRY));
            output.write("Manifest-Version: 1.0\n\n".getBytes("UTF-8")); // NOI18N
            
            output.putNextEntry(new JarEntry(FILES_LIST_ENTRY));
            OutputStreamWriter writer =
                    new OutputStreamWriter(output, "UTF-8"); // NOI18N
            
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); // NOI18N
            writer.write("<files-list>\n"); // NOI18N
            for (FileEntry entry: entries) {
                writer.write("    <entry "); // NOI18N
                if (entry.isDirectory()) {
                    writer.write("type=\"directory\" "); // NOI18N
                    writer.write("empty=\"" + // NOI18N
                            entry.isEmpty() + "\" "); // NOI18N
                    writer.write("modified=\"" + // NOI18N
                            entry.getLastModified() + "\" "); // NOI18N
                    writer.write("permissions=\"" + // NOI18N
                            entry.getPermissions() + "\""); // NOI18N
                } else {
                    writer.write("type=\"file\" "); // NOI18N
                    writer.write("size=\"" + // NOI18N
                            entry.getSize() + "\" "); // NOI18N
                    writer.write("md5=\"" + // NOI18N
                            entry.getMd5() + "\" "); // NOI18N
                    writer.write("jar=\"" + // NOI18N
                            entry.isJarFile() + "\" "); // NOI18N
                    writer.write("packed=\"" + // NOI18N
                            entry.isPackedJarFile() + "\" "); // NOI18N
                    writer.write("signed=\"" + // NOI18N
                            entry.isSignedJarFile() + "\" "); // NOI18N
                    writer.write("modified=\"" + // NOI18N
                            entry.getLastModified() + "\" "); // NOI18N
                    writer.write("permissions=\"" + // NOI18N
                            entry.getPermissions() + "\""); // NOI18N
                }
                
                writer.write(">" + entry. // NOI18N
                        getName().
                        replace("&", "&amp;"). // NOI18N
                        replace("<", "&lt;"). // NOI18N
                        replace(">", "&gt;") + "</entry>\n"); // NOI18N
            }
            writer.write("</files-list>\n"); // NOI18N
            
            writer.flush();
            writer.close();
            
            output.flush();
            output.close();
            
            log("archived " + directoriesCount + // NOI18N
                    " directories and " + filesCount + " files"); // NOI18N
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private void browse(
            final File parent,
            final JarOutputStream output,
            final int offset) throws IOException {
        FileInputStream fis = null;
        final List<File> toSkip = new LinkedList<File>();
        
        for (File child: parent.listFiles()) {
            if (toSkip.contains(child)) {
                log("    skipping " + child); // NOI18N
                continue;
            }
            
            log("    visiting " + child); // NOI18N
            
            final String path = child.getAbsolutePath();
            String name = path.substring(offset + 1).replace('\\', '/');    // NOMAGI
            
            FileEntry entry;
            JarEntry jarEntry;
            
            if (child.isDirectory()) {
                log("        archiving directory: " + name); // NOI18N
                
                name  = name + "/"; // NOI18N
                entry = new FileEntry(child, name);
                
                output.putNextEntry(new JarEntry(name));
                
                directoriesCount++;
                
                browse(child, output, offset);
            } else {
                // if the source file comes in already packed, we need to unpack it
                // first and then process normally
                if (child.getName().endsWith(".jar.pack.gz")) { // NOI18N
                    log("        it is a packed jar - unpacking"); // NOI18N
                    File unpacked = new File(child.getPath().substring(
                            0,
                            child.getPath().length() - 8));
                    File temp = null;
                    
                    if (unpacked.exists()) {
                        temp = File.createTempFile(
                                "xxx", // NOI18N
                                null,
                                child.getParentFile());
                        temp.delete();
                        unpacked.renameTo(temp);
                    }
                    
                    if (Utils.unpack(child, unpacked)) {
                        child.delete();
                        if (temp != null) {
                            temp.delete();
                        }
                        child = unpacked.getAbsoluteFile();
                        
                        log("        successfully unpacked - processing " + // NOI18N
                                "file: " + child); // NOI18N
                    } else {
                        unpacked.delete();
                        if (temp != null) {
                            temp.renameTo(unpacked);
                        }
                    }
                }
                
                entry = new FileEntry(child, name);
                
                if (entry.isJarFile() && !entry.isSignedJarFile()) {
                    File backup = new File(child.getPath() + ".bak"); // NOI18N
                    File packed = new File(child.getPath() + ".pack.gz"); // NOI18N
                    
                    // if the packed form of this jar already exists, we need to
                    // clean it up
                    if (packed.exists()) {
                        log("        packed jar already exists - " + // NOI18N
                                "deleting it"); // NOI18N
                        packed.delete();
                        toSkip.add(packed);
                    }
                    
                    Utils.copy(child, backup);
                    
                    if (Utils.pack(child, packed) &&
                            Utils.unpack(packed, child) &&
                            Utils.verify(child)) {
                        name  = packed.getPath().
                                substring(offset + 1).replace('\\', '/');   // NOMAGI
                        
                        entry = new FileEntry(child, name);
                        entry.setJarFile(true);
                        entry.setPackedJarFile(true);
                        entry.setSignedJarFile(false);
                        
                        child.delete();
                        backup.delete();
                        
                        child = packed;
                    } else {
                        Utils.copy(backup, child);
                        
                        packed.delete();
                        backup.delete();
                    }
                }
                
                log("        archiving file: " + name); // NOI18N
                jarEntry = new JarEntry(name);
                jarEntry.setTime(entry.getLastModified());
                jarEntry.setSize(entry.getSize());
                output.putNextEntry(jarEntry);
                
                fis = new FileInputStream(child);
                Utils.copy(fis, output);
                fis.close();
                
                filesCount++;
            }
            
            entries.add(entry);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String METAINF_ENTRY = 
            "META-INF/"; // NOI18N
    private static final String FILES_LIST_ENTRY = 
            "META-INF/files.list"; // NOI18N
    private static final String MANIFEST_ENTRY = 
            "META-INF/manifest.mf"; // NOI18N
}
