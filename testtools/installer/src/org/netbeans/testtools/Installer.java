/*
 * Installer.java
 *
 * Created on February 7, 2003, 9:46 AM
 */

package org.netbeans.testtools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class Installer {
    
    private static boolean ignoreCVS=Boolean.getBoolean("ignoreCVS");

    private static String targetFolder=System.getProperty("nbroot", ".");
    
    private static final String jemmyJAR = "netbeans/modules/ext/jemmy.jar";
    private static final String jellyJAR = "netbeans/modules/ext/jelly-nb.jar";
    private static final String jelly2JAR = "netbeans/modules/ext/jelly2-nb.jar";
    private static final String xtestFolder = "netbeans/xtest-distribution/";
    
    private static final String jemmyTarget = "../nbextra/jemmy/jemmy.jar";
    private static final String jellyTarget = "../nbextra/jellytools/jelly-nb.jar";
    private static final String jelly2Target = "../nbextra/jellytools/jelly2-nb.jar";
    private static final String xtestTarget = "xtest/";
    
    private static final ZipInputStream jemmyNBM=getStream("jemmy.nbm");
    private static final ZipInputStream jellyNBM=getStream("jellytools.nbm");
    private static final ZipInputStream xtestNBM=getStream("xtest.nbm");
    
    private static final byte buff[] = new byte[65536];
    
    private static void err(String message) {
        System.err.println("TestTools Installer error: "+message);
        if (jemmyNBM!=null) try {jemmyNBM.close();} catch (IOException ioe) {}
        if (jellyNBM!=null) try {jemmyNBM.close();} catch (IOException ioe) {}
        if (xtestNBM!=null) try {xtestNBM.close();} catch (IOException ioe) {}
        System.exit(-1);
    }
    
    private static ZipInputStream getStream(String fileName) {
        InputStream is=Installer.class.getClassLoader().getResourceAsStream(fileName);
        if (is==null) err("Missing "+fileName+" !");
        return new ZipInputStream(is);
    }
    
    private static void testTarget(String target) {
        File test=new File(targetFolder, target);
        if (test.isFile()) test=test.getParentFile();
        if (new File(test, "CVS").isDirectory()) {
            System.err.println("Folder "+test.getAbsolutePath()+" contains CVS information !");
            if (!ignoreCVS) {
                System.err.println("Overriding files from CVS repository may cause collisions during next update !");
                System.err.println("Do you want to continue (Y/n) ?");
                try {
                    if (Character.toUpperCase((char)System.in.read())=='Y') ignoreCVS=true;
                    else err("Installation interrupted !");
                } catch (IOException ioe) {
                    err(ioe.getMessage());
                }
            }
        }
    }
    
    private static void unzipFile(ZipInputStream in, String target) {
        File file=new File(targetFolder, target);
        createFolder(file.getParentFile());
        FileOutputStream out=null;
        try {
            out=new FileOutputStream(file);
            int i;
            while ((i=in.read(buff))>0) {
                out.write(buff, 0, i);
            }
            System.out.println(file.getAbsolutePath());
        } catch (FileNotFoundException fnfe) {
            err("Error creating "+file.getAbsolutePath()+" "+fnfe.getMessage());
        } catch (IOException ioe) {
            err("IOException during extraction of "+file.getAbsolutePath()+" "+ioe.getMessage());
        } finally {
            if (out!=null)  try {out.close();} catch (IOException ioe) {}
        }
    }
    
    private static void createFolder(String folder) {
        createFolder(new File(targetFolder, folder));
    }
    
    private static void createFolder(File dir) {
        if (!dir.exists()) {
            if (dir.mkdirs()) System.out.println(dir.getAbsolutePath());
            else err("Could not create directory "+dir.getAbsolutePath()+" !");
        } else if (!dir.isDirectory()) err(dir.getAbsolutePath()+" is not a directory !");
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length>0) {
            targetFolder=args[0];
        } else {
            System.out.println("NetBeans root directory (\"nb_all\") is not defined as command-line argument, using "+new File(targetFolder).getAbsolutePath());
        }
        testTarget(jemmyTarget);
        testTarget(jellyTarget);
        testTarget(jelly2Target);
        testTarget(xtestTarget);
        try {
            ZipEntry entry=jemmyNBM.getNextEntry();
            while (entry!=null && !entry.getName().equals(jemmyJAR)) entry=jemmyNBM.getNextEntry();
            if (entry==null) err("Missing "+jemmyJAR+" in jemmy.nbm !");
            unzipFile(jemmyNBM, jemmyTarget);
            
            entry=jellyNBM.getNextEntry();
            while (entry!=null && !entry.getName().equals(jellyJAR) && !entry.getName().equals(jelly2JAR)) entry=jellyNBM.getNextEntry();
            if (entry==null) err("Missing "+jellyJAR+" and "+jelly2JAR+" in jellytools.nbm !");
            String next;
            if (entry.getName().equals(jellyJAR)) {
                next=jelly2JAR;
                unzipFile(jellyNBM, jellyTarget);
            } else {
                next=jellyJAR;
                unzipFile(jellyNBM, jelly2Target);
            }
            while (entry!=null && !entry.getName().equals(next)) entry=jellyNBM.getNextEntry();
            if (entry==null) err("Missing "+next+" in jellytools.nbm !");
            if (next.equals(jellyJAR)) {
                unzipFile(jellyNBM, jellyTarget);
            } else {
                unzipFile(jellyNBM, jelly2Target);
            }

            boolean found=false;
            while ((entry=xtestNBM.getNextEntry())!=null) {
                if (entry.getName().startsWith(xtestFolder)) {
                    found=true;
                    if (entry.isDirectory()) {
                        createFolder(xtestTarget+entry.getName().substring(xtestFolder.length()));
                    } else {
                        unzipFile(xtestNBM, xtestTarget+entry.getName().substring(xtestFolder.length()));
                    }
                }
            }
            if (!found) err("Missing "+xtestFolder+" in xtest.nbm !");
            
            jemmyNBM.close();
            jellyNBM.close();
            xtestNBM.close();
            System.out.println("Finished.");
        } catch (IOException ioe) {
            err(ioe.getMessage());
        }
    }
    
}
