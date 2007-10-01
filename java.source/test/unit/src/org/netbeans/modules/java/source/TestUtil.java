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

package org.netbeans.modules.java.source;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import junit.framework.TestCase;
import org.netbeans.junit.NbTestCase;


/**
 *
 * @author Petr Hrebejk
 */
public class TestUtil {
        
    public static final String RT_JAR = "jre/lib/rt.jar";
    public static final String SRC_ZIP = "src.zip";
    
    /** Creates a new instance of TestUtil */
    private TestUtil() {
    }
    
    public static void copyFiles( File destDir, String... resourceNames ) throws IOException {
        copyFiles(getDataDir(), destDir, resourceNames);
    }
    
    public static void copyFiles( File sourceDir, File destDir, String... resourceNames ) throws IOException {

        for( String resourceName : resourceNames ) {
            
            File src = new File( sourceDir, resourceName ); 
            
            if ( !src.canRead() ) {
                TestCase.fail( "The test requires the file: " + resourceName + " to be readable and stored in: " + sourceDir );
            }
            
            InputStream is = new FileInputStream( src );            
            BufferedInputStream bis = new BufferedInputStream( is );
                        
            File dest = new File( destDir, resourceName );            
            File parent = dest.getParentFile();
            
            if ( !parent.exists() ) {
                parent.mkdirs();
            }
            
            dest.createNewFile();
            BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( dest ) );
            
            copyFile( bis, bos );
        }
    }
    
    public static void unzip( ZipFile zip, File dest ) throws IOException {
        
        for( Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements(); ) {
            ZipEntry entry = e.nextElement();
            File f = new File( dest, entry.getName() );
            if ( entry.isDirectory() ) {
                f.mkdirs();
            }
            else {
                f.getParentFile().mkdirs();
                f.createNewFile();
                BufferedInputStream bis = new BufferedInputStream( zip.getInputStream( entry ) );            
                BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( f ) );            
                copyFile( bis, bos );
            }
        }
        
    }
    
    public static File createWorkFolder() throws IOException {
        File tempFile = File.createTempFile( "TestWorkDir", null );
        tempFile.delete();
        tempFile.mkdir();
        return tempFile;
    }
    
    public static FileFilter createExtensionFilter( boolean folders, final String ... extensions ) {
        return new ExtensionFileFilter( folders, extensions );            
    }
    
    public static void removeWorkFolder( File file ) {
        deleteRecursively( file );        
    }
    
    /** Good for debuging content of large collections.
     * Prints out readable diff of the collections passed as parameters.
     */    
    public static String collectionDiff( Iterable c1, Iterable c2 ) {
        return collectionDiff( c1.iterator(), c2.iterator() );
    }
    
    public static String collectionDiff( Iterator it1, Iterator it2 ) {
        
          StringBuilder sb = new StringBuilder();  
        
          int index = 0;
          boolean printing = false;
          while( it1.hasNext() ) {
                 
             Object o1 = it1.next();
             
             Object o2 = it2.hasNext() ? it2.next() : null ; 

             if ( !o1.equals( o2 ) ) {
                 if ( !printing ) {
                     printing = true;
                     sb.append("\n");
                 }
                 sb.append( index + " " + o1 + " -> " + ( o2 == null ? "NULL" : o2 )  + "\n" );
             } 
             else if ( printing ) {
                 printing = false;
             }
             
             index++;
          }
          
          if ( it2.hasNext() ) {
              sb.append( "\n" );
          }
          while( it2.hasNext() ) {
              sb.append( index + "   [NULL]" + " -> " + it2.next()  + "\n" );
              index ++;
          }
                  
          return sb.toString();
    }
    
    public static String fileToString( File file ) throws IOException {
        
        BufferedReader reader = new BufferedReader( new FileReader( file ) );
        StringBuffer sb = new StringBuffer();
        
        for( String line = reader.readLine(); line != null; line = reader.readLine() ) {
            //System.out.println("L " + line);
            sb.append( line ).append( "\n" ); 
        }
        
        return sb.toString();
    }
    
    /** Returns the tests data folder. Containing sample classes
     */
    public static File getDataDir() {
        return TemporaryTestCase.getDataFolder();
    }
    
    /** Returns current jdk directory
     */    
    public static File getJdkDir() {
	
	Properties p = System.getProperties();	
	String javaHomeProp = p.getProperty( "java.home" );
	
	if ( javaHomeProp == null ) {
	    throw new IllegalStateException( "Can't find java.home property ");
	}
	else {
	    File jre = new File( javaHomeProp );
	    if ( !jre.canRead() ) {
		throw new IllegalStateException( "Can't read " + jre );
	    }
	    File dir = jre.getParentFile();
	    if ( !jre.canRead() ) {
		throw new IllegalStateException( "Can't read " + dir);
	    }
	    return dir;
	}
    }
    
    
    /** Returns given JDK file 
     * @param path Relative path to the JDK file.
     * @return the file 
     * @throws IllegalArgumentException if the file can't be found or read.
     */
    public static File getJdkFile( String path ) {
	File dir = getJdkDir();
	
	File f = new File( dir, path );
	
	if ( f.canRead() ) {
	    return f;
	}  
	else {
	    throw new IllegalArgumentException( "Can't read file " + f );
	}
		
    }
    
    
    // Private methods ---------------------------------------------------------
    
    private static int BLOCK_SIZE = 16384;
    
    private static void copyFile( InputStream is, OutputStream os ) throws IOException {
        byte[] b = new byte[ BLOCK_SIZE ];   
        int count = is.read(b);     

        while (count != -1)
        {
         os.write(b, 0, count);
         count = is.read(b);
        }

        is.close();
        os.close();
    }
    
    /** Recursively deletes the complete folder srtucture
     */
    private static void deleteRecursively( File file ) {
        
        if ( file.isDirectory() ) {        
            File[] files = file.listFiles();
            for( File f : files ) {
                deleteRecursively( f );
            }
        }
        
        file.delete();
    }
    
    
//    public static void printInsane( Object... roots ) {
//        
//        System.gc(); 
//        System.gc();
//        System.out.println( "FREE MEMORY :" + ( Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ) );
//        System.out.println("");
//        
//        final CountingVisitor cv = new CountingVisitor();
//        try {
//            ScannerUtils.scan( null, cv, Arrays.asList( roots ), true );
//        }
//        catch ( Exception e ) {
//            e.printStackTrace();
//        }
//
//        Set ordered = new TreeSet(new Comparator() {
//            public int compare(Object c1, Object c2) {
//                int diff = cv.getSizeForClass((Class)c2)
//                           cv.getSizeForClass((Class)c1);
//
//                if (diff != 0 || c1 == c2) return diff;
//                return ((Class)c1).getName().compareTo(((Class)c2).getName());
//            }
//        });
//
//        ordered.addAll(cv.getClasses());
//
//        System.out.println("Usage: [instances class.Name: totalSizeInBytes]");
//        for (Iterator it = ordered.iterator(); it.hasNext();) {
//            Class cls = (Class)it.next();
//            System.out.println(cv.getCountForClass(cls) + " " +
//                            cls.getName() + ": " + cv.getSizeForClass(cls));
//        }
//
//        System.out.println("total: " + cv.getTotalSize() + " in " +
//            cv.getTotalCount() + " objects.");
//        System.out.println("Classes:" + cv.getClasses().size());
//
//            
//        
//        
//    }
    
    
      // Private innerclasses --------------------------------------------------
    
      private static class ExtensionFileFilter implements FileFilter {
          
          private boolean folders;
          private String[] extensions;
          
          public ExtensionFileFilter( boolean folders, String... extensions ) {
              this.folders = folders;
              this.extensions = extensions;
          }
          
                    
          public boolean accept( File file ) {
          
              if ( folders && file.isDirectory() ) {
                  return true;
              }
                            
              for( String ext : extensions ) {
                  if ( file.getName().endsWith( ext ) ) {
                      return true;
                  }                  
              }
              
              return false;
          }
          
          
      }
      
      private static class TemporaryTestCase extends NbTestCase {
    
          private static TemporaryTestCase INSTANCE = new TemporaryTestCase();
          
          TemporaryTestCase() {
              super( TemporaryTestCase.class.toString() );
          }
          
          public static File getDataFolder() {
              return INSTANCE.getDataDir();
          }
          
      }
      
    
}
