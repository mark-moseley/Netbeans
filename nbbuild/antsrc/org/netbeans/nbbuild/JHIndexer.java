/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.types.Path;

// ToDo:
// stopwords configuration
// add branding suffix to each generated file
// verbose mode

/** Task to run JavaHelp search indexer.
 * Creates the proper binary search database from source HTML.
 * @author Jesse Glick
 * @see <a href="http://java.sun.com/products/javahelp/">JavaHelp home page</a>
 */
public class JHIndexer extends MatchingTask {

    private Path classpath;
    private File db;
    private File basedir;
    private String locale;

    /** Set the location of <samp>jhall.jar</samp> (JavaHelp tools library). */
    public Path createClasspath() {
        // JavaHelp release notes say jhtools.jar is enough, but class NoClassDefFoundError
        // on javax.help.search.IndexBuilder when I tried it...
        if (classpath == null) {
            classpath = new Path(getProject());
        }
        return classpath.createPath();
    }

    /** Set the location of the output database.
     * E.g. <samp>JavaHelpSearch</samp>).
     * <strong>Warning:</strong> the directory will be deleted and recreated.
     */
    public void setDb (File db) {
        this.db = db;
    }

    /** Set the base directory from which to scan files.
     * This should be the directory containing the helpset for the database to work correctly.
     */
    public void setBasedir (File basedir) {
        this.basedir = basedir;
    }
    public void setLocale (String locale) {
        this.locale = locale;
    }
    
    /** @deprecated Use {@link #createClasspath} instead. */
    public void setJhall(File f) {
        log("The 'jhall' attribute to <jhindexer> is deprecated. Use a nested <classpath> instead.", Project.MSG_WARN);
        createClasspath().setLocation(f);
    }

    public void execute () throws BuildException {
        if (classpath == null) throw new BuildException ("Must specify the classpath attribute to find jhall.jar");
        if (db == null) throw new BuildException ("Must specify the db attribute");
        if (basedir == null) throw new BuildException ("Must specify the basedir attribute");
        FileScanner scanner = getDirectoryScanner (basedir);
        scanner.scan ();
        String[] files = scanner.getIncludedFiles ();
        // First, an up-to-date check. ;-)
        if (basedir.exists () && db.exists ()) {
            long lastModified = Long.MIN_VALUE;
            // First scan output dir for any files.
            FileScanner output = new DirectoryScanner ();
            output.setBasedir (db);
            output.scan ();
            String[] outfiles = output.getIncludedFiles ();
            if (outfiles.length > 0) {
                for (int i = 0; i < outfiles.length; i++) {
                    long mod = new File (db, outfiles[i]).lastModified ();
                    if (mod > lastModified) {
                        lastModified = mod;
                    }
                }
                // Now check to see if any source files are newer.
                boolean ok = true;
                for (int i = 0; i < files.length; i++) {
                    long mod = new File (basedir, files[i]).lastModified ();
                    if (mod > lastModified) {
                        ok = false;
                        break;
                    }
                }
                if (ok) {
                    // No need to rebuild.
                    return;
                }
            }
        }
        Delete delete = (Delete) project.createTask ("delete");
        delete.setDir (db);
        delete.init ();
        delete.setLocation (location);
        delete.execute ();
        Mkdir mkdir = (Mkdir) project.createTask ("mkdir");
        mkdir.setDir (db);
        mkdir.init ();
        mkdir.setLocation (location);
        mkdir.execute ();
        log ("Running JavaHelp search database indexer...");
        try {
            File config = File.createTempFile ("jhindexer-config", ".txt");
            try {
                OutputStream os = new FileOutputStream (config);
                try {
                    PrintWriter pw = new PrintWriter (os);
                    pw.println ("IndexRemove " + basedir + File.separator);
                    String message = "Files to be indexed:";
                    for (int i = 0; i < files.length; i++) {
                        // [PENDING] JavaHelp docs say to use / as file sep for File directives;
                        // so what should the complete path be? Someone should test this on Windoze...
                        String path = basedir + File.separator + files[i];
                        pw.println ("File " + path);
                        message += "\n\t" + path;
                    }
                    log (message, Project.MSG_VERBOSE);
                    pw.flush ();
                } finally {
                    os.close ();
                }
                Java java = (Java) project.createTask ("java");
                java.setClasspath(classpath);
                java.setClassname ("com.sun.java.help.search.Indexer");
                java.createArg ().setValue ("-c");
                java.createArg ().setFile (config);
                java.createArg ().setValue ("-db");
                java.createArg ().setFile (db);
                if (locale != null) {
                    java.createArg ().setValue("-locale");
                    java.createArg ().setValue(locale);
                }
                java.setFailonerror (true);
                // Does not work when run using Ant support internally to the IDE:
                // IllegalAccessError since some classes are
                // loaded from jhall.jar, some from startup jh.jar, and some loaded from
                // jh.jar are package private, thus attempts to access them from classes
                // loaded from jhall.jar is illegal. So we fork.
                if (System.getProperty ("org.openide.version") != null) {
                    java.setFork (true);
                }
                java.init ();
                java.setLocation (location);
                java.execute ();
                // A failed attempt to make it work inside the IDE with internal execution.
                // For unknown reasons (I do not have full JavaHelp source), running internally
                // throws FileNotFoundException: ...../JavaHelpSearch/TMAP (No such file or directory)
                // from MemoryRAFFile constructor in the indexer.
                /*
                Path classpath = new Path (project);
                classpath.createPathElement ().setLocation (jhall);
                AntClassLoader loader = new AntClassLoader (project, classpath);
                loader.addLoaderPackageRoot ("javax.help");
                loader.addLoaderPackageRoot ("com.sun.java.help");
                try {
                    Class clazz = loader.loadClass ("com.sun.java.help.search.Indexer");
                    Method main = clazz.getMethod ("main", new Class[] { String[].class });
                    try {
                        main.invoke (null, new Object[] { new String[] { "-c", config.getAbsolutePath (), "-db", db.getAbsolutePath () } });
                    } catch (SecurityException se) {
                        // Ignore, probably just System.exit() being called or something.
                        se.printStackTrace ();//XXX
                    }
                } catch (InvocationTargetException ite) {
                    throw new BuildException ("Could not run indexer", ite.getTargetException (), location);
                } catch (Exception e) { // ClassNotFoundException, NoSuchMethodException, ...
                    throw new BuildException ("Could not run indexer", e, location);
                }
                 */
            } finally {
                config.delete ();
            }
        } catch (IOException ioe) {
            throw new BuildException ("Could not make temporary config file", ioe, location);
        }
    }

}
