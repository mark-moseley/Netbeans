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

package org.netbeans.nbbuild;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.types.*;
import java.io.*;
import java.util.zip.CRC32;
import java.lang.reflect.Method;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.jar.JarFile;

/**
 * Create an update tracking file automatically.
 * Requires a build script target containing one (and only one)
 * occurrence of the <code>&lt;makenbm&gt;</code> task.
 * @author  Michal Zlamal
 */
public class MakeListOfNBM extends Task {
    File outputFile = null;
    String moduleName = null;
    boolean pok = true;
    FileSet fs = null;
    
    /** Sets the directory used to create the NBM list file */
    public void setOutputfiledir(File s) {
        outputFile = s;
    }

    public FileSet createFileSet() {
        return (fs = new FileSet());
    }
    
    /** Sets the module file */
    public void setModule(String s) {
        moduleName = s;
    }

    public void setTargetName(String t) {
        pok = false;
    }

    public void execute () throws BuildException {
        if (!pok) throw new BuildException("Use the fileset to specify the content of the NBM");
        if ( outputFile == null ) throw new BuildException( "You have to specify output directoty" );
        if ( moduleName == null ) throw new BuildException( "You have to specify the main module's file" );
        if ( fs == null ) throw new BuildException( "You have to specify the fileset of files of this module" );

        log ("Generating information for Auto Update...");
        
/*        if (!outputFile.exists()) {
            outputFile.mkdirs();
        }
*/        
        UpdateTracking track = new UpdateTracking( outputFile.getAbsolutePath() );
        Attributes attr;
        JarFile jar = null;
        File module = new File( outputFile, moduleName );
        try {
            jar = new JarFile(module);
            attr = jar.getManifest().getMainAttributes();
        } catch (IOException ex) {
            throw new BuildException( "Can't get manifest attributes", ex, location );
        } finally {
            try {
                if (jar != null) jar.close();
            } catch( IOException ex1 ) {}
        }
      
        String codename = attr.getValue("OpenIDE-Module");
        if (codename == null)
            throw new BuildException("invalid manifest, does not contain OpenIDE-Module", location);
        
        String versionSpecNum = attr.getValue("OpenIDE-Module-Specification-Version");
        if (versionSpecNum == null) {
            log("manifest does not contain OpenIDE-Module-Specification-Version");
            return;
        }
        
        UpdateTracking.Version version = track.addNewModuleVersion( codename, versionSpecNum );
        
        DirectoryScanner ds = fs.getDirectoryScanner( this.getProject() );
        String excludes[]={"Info/info.xml", "main/**"};
        ds.setExcludes( excludes );
        ds.scan();
        
        String include[] = ds.getIncludedFiles();
        for( int j=0; j < include.length; j++ ){
            if (include[j].equals("Info/info.xml") || include[j].startsWith("main/")) continue;
            try {
                File inFile = new File( ds.getBasedir(), include[j] );
                FileInputStream inFileStream = new FileInputStream( inFile );
                byte array[] = new byte[ (int) inFile.length() ];
                CRC32 crc = new CRC32();
                inFileStream.read( array );
                inFileStream.close();
                crc.update( array );
                String abs = inFile.getAbsolutePath();
                String prefix = ds.getBasedir().getAbsolutePath() + File.separatorChar;
                if (! abs.startsWith(prefix)) throw new IllegalStateException(abs);
                version.addFileWithCrc(abs.substring(prefix.length()), Long.toString( crc.getValue() ) );
            } catch (IOException ex) {
                log( ex.toString() );
            }
        }
        track.write();
        String absolutePath = outputFile.getAbsolutePath();
        String clusterDir = this.getProject().getProperty("cluster.dir");
        String moduleName = this.getProject().getProperty("module.name");
        String outputPath = absolutePath.substring(0,absolutePath.length() - clusterDir.length());
        ModuleTracking moduleTracking = new ModuleTracking( outputPath );
        moduleTracking.putModule(moduleName, absolutePath, include);
        moduleTracking.write();
    }
}
