/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.zip.CRC32;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

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
        log("Setting outputfile to " + s, Project.MSG_DEBUG);
    }

    public FileSet createFileSet() {
        return (fs = new FileSet());
    }
    
    /** Sets the module file */
    public void setModule(String s) {
        moduleName = s;
        log("Setting moduleName to " + s, Project.MSG_DEBUG);
    }

    public void setTargetName(String t) {
        pok = false;
        log("<"+this.getTaskName()+"> attribute targetname has been DEPRECATED");
    }

    public void execute () throws BuildException {
        if (!pok) throw new BuildException("Use the fileset to specify the content of the NBM");
        if ( outputFile == null ) throw new BuildException( "You have to specify output directoty" );
        if ( moduleName == null ) throw new BuildException( "You have to specify the main module's file" );
        if ( fs == null ) throw new BuildException( "You have to specify the fileset of files included in this module" );

        log ("Generating information for Auto Update...");
        
        UpdateTracking track = new UpdateTracking( outputFile.getAbsolutePath() );
        Attributes attr;
        JarFile jar = null;
        File module = new File( outputFile, moduleName );
        try {
            jar = new JarFile(module);
            attr = jar.getManifest().getMainAttributes();
        } catch (IOException ex) {
            throw new BuildException("Can't get manifest attributes for module jar file "+module.getAbsolutePath(), ex, getLocation());
        } finally {
            try {
                if (jar != null) jar.close();
            } catch( IOException ex1 ) {
                String exmsg = ex1.getMessage();
                if (exmsg == null) exmsg = "Unknown error";
                log("Caught I/O Exception (msg:\""+exmsg+"\") when trying to close jar file "+module.getAbsolutePath(),Project.MSG_WARN);
            }
        }
        
        String codename = attr.getValue("OpenIDE-Module"); //NOI18N
        if (codename == null) {
            throw new BuildException("Manifest in jar file "+module.getAbsolutePath()+" does not contain OpenIDE-Module", getLocation());
        }
        
        String versionSpecNum = attr.getValue("OpenIDE-Module-Specification-Version"); //NOI18N
        if (versionSpecNum == null) {
            log("Manifest in jar file "+module.getAbsolutePath()+" does not contain tag OpenIDE-Module-Specification-Version");
            return;
        }
        
        UpdateTracking.Version version = track.addNewModuleVersion( codename, versionSpecNum );

        fs.createInclude().setName("config" + File.separator + "Modules" + File.separator + track.getTrackingFileName()); //NOI18N
        
        // get directory scanner for "default" files
        DirectoryScanner ds = fs.getDirectoryScanner( this.getProject() );
        ds.scan();
        
        // check if we need also localized and branded files
        String lmnl = this.getProject().getProperty("locmakenbm.locales"); // NOI18N
        String lmnb = this.getProject().getProperty("locmakenbm.brands"); // NOI18N
        
        if ((!(lmnl == null)) && (!(lmnl.trim().equals("")))) { // NOI18N
            // property locmakenbm.locales is set, let's update the included fileset for locales
            // defined in that property

            java.util.StringTokenizer tokenizer = new StringTokenizer( lmnl, ", ") ; //NOI18N
            int cntTok = tokenizer.countTokens();
            String[] lmnLocales = new String[cntTok];
            for (int j=0; j < cntTok; j++) {
                String s = tokenizer.nextToken();
                lmnLocales[j] = s;
                log("  lmnLocales[j] == "+lmnLocales[j], Project.MSG_DEBUG); // NOI18N
            }

            // handle brandings   
            String[] lmnBrands = null;
            if ((!(lmnb == null)) && (!(lmnb.trim().equals("")))) { // NOI18N
                tokenizer = new StringTokenizer( lmnb, ", ") ; //NOI18N
                cntTok = tokenizer.countTokens();
                lmnBrands = new String[cntTok];
                for (int j=0; j < cntTok; j++) {
                    String s = tokenizer.nextToken();
                    lmnBrands[j] = s;
                    log("  lmnBrands[j] == "+lmnBrands[j], Project.MSG_DEBUG); // NOI18N
                }
            }

            // update fileset for localized/branded files
        
            String[] englishFiles = ds.getIncludedFiles();
            int sepPos, extPos;
            String dirName, fname, filename, fext, newinc, ei_codename;
            String moduleJar = null;
            boolean skipLocaleDir = false;
            for (int k=0; k < englishFiles.length; k++) {
                // skip records for already localized/branded files
                if ((englishFiles[k].lastIndexOf("/locale/") >= 0) || // NOI18N
                     (englishFiles[k].lastIndexOf(File.separator+"locale"+File.separator) >= 0)) {  // NOI18N
                    skipLocaleDir=true;
                } else {
                    skipLocaleDir=false;
                }
                log("Examining file " + englishFiles[k], Project.MSG_DEBUG);
                sepPos = englishFiles[k].lastIndexOf(File.separator);
                if (sepPos < 0) {
                    dirName = ""; //NOI18N
                    filename = englishFiles[k];
                } else {
                    dirName = englishFiles[k].substring(0,sepPos);
                    filename = englishFiles[k].substring(sepPos+File.separator.length());
                }
                extPos = filename.lastIndexOf('.'); //NOI18N
                if (extPos < 0) {
                    fname = filename;
                    fext = ""; //NOI18N
                } else {
                    fname = filename.substring(0, extPos);
                    fext = filename.substring(extPos);
                }
                for (int j=0; j < lmnLocales.length; j++) {
                    // localized files
                    if (skipLocaleDir) {
                    	newinc = dirName + File.separator + fname + "_"+lmnLocales[j]+"*" + fext; //NOI18N
                    } else {
                        newinc = dirName + File.separator + "locale" + File.separator + fname + "_"+lmnLocales[j]+"*" + fext; //NOI18N
                    }
                    log("  adding include mask \""+newinc+"\"", Project.MSG_DEBUG);
                    fs.setIncludes( newinc );
                    // localized & branded files
                    if (!(lmnBrands == null)) {
                    	for (int i=0; i < lmnBrands.length; i++) {
                    	    if (skipLocaleDir) {
                    	        newinc = dirName + File.separator + fname + "_"+lmnBrands[i]+"_"+lmnLocales[j]+"*" + fext; //NOI18N
                            } else {
                    	        newinc = dirName + File.separator + "locale" + File.separator + fname + "_"+lmnBrands[i]+"_"+lmnLocales[j]+"*" + fext; //NOI18N
                    	    }
                            log("  adding include mask \""+newinc+"\"", Project.MSG_DEBUG);
                            fs.setIncludes( newinc );
                    	}
                    }
                }
            }
            // update directory scanner
            ds = fs.getDirectoryScanner(this.getProject());
            ds.scan();
        }
        
        String include[] = ds.getIncludedFiles();
        for( int j=0; j < include.length; j++ ){
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
                version.addFileWithCrc(abs.substring(prefix.length()).replace(File.separatorChar, '/'), Long.toString( crc.getValue() ) );
            } catch (IOException ex) {
                log( ex.toString() );
            }
        }
        track.write();
        String moduleName = this.getProject().getProperty("module.name"); //NOI18N
        if (moduleName == null) {
            // external module?
            return;
        }
        String[] inc = new String[include.length+2];
        for (int i=0; i < include.length; i++)
            inc[i] = include[i];
        inc[include.length] = "config" + File.separator + "Modules" + File.separator + track.getTrackingFileName(); // NOI18N
        inc[include.length+1] = UpdateTracking.TRACKING_DIRECTORY + File.separator + track.getTrackingFileName();
        // Hack. We assume outputFile is a direct subdir of the dest dir.
        ModuleTracking moduleTracking = new ModuleTracking(outputFile.getParentFile().getAbsolutePath());
        String nbmfilename = this.getProject().getProperty("nbm"); // NOI18N
        String nbmhomepage = this.getProject().getProperty("nbm.homepage"); // NOI18N
        String nbmneedsrestart = this.getProject().getProperty("nbm.needs.restart"); // NOI18N
        String nbmreleasedate = this.getProject().getProperty("nbm.release.date"); // NOI18N
	String nbmmoduleauthor = this.getProject().getProperty("nbm.module.author"); // NOI18N
	String nbmisglobal = this.getProject().getProperty("nbm.is.global"); // NOI18N
	String nbmtargetcluster = this.getProject().getProperty("nbm.target.cluster"); // NOI18N
        // ...and again here.
        moduleTracking.putModule(moduleName, codename, outputFile.getName(), nbmfilename, nbmhomepage, nbmneedsrestart, nbmreleasedate, nbmmoduleauthor, nbmisglobal, nbmtargetcluster, inc);
        moduleTracking.write();
    }
}
